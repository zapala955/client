package net.client.visuals.render.storm;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.client.visuals.LiquidClient;
import net.client.visuals.module.render.StormChaserModule;
import net.client.visuals.render.post.FloatBlock;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.client.visuals.render.weather.LiquidWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Draws the storm's lightning into the world (render thread), from {@code PostFx#afterLevel}: after
 * the world and its haze, before Rainbows, Sun Rays, Depth of Field, Motion Blur and the hand – so the
 * hand stays unlit by the post pass (the lightmap flash lights it a little) and Bloom, later, makes
 * every bolt glow.
 *
 * <ol>
 *   <li><b>Relighting</b> ({@code storm_light.fsh}), while a flash is lit: the light of every stroke
 *       and cloud flash on the world, from its depth and reconstructed normals, at half resolution
 *       (or full, the Resolution setting); a depth-aware upsample multiplies it into the frame.</li>
 *   <li><b>Bolts</b> ({@code storm_bolt.vsh/.fsh}): each live channel as camera-facing glowing ribbons,
 *       depth-tested against the world (reverse-Z, no depth write) and added to the frame. One static
 *       quad buffer for every bolt; each bolt is a uniform block of segments, written once per frame
 *       through its own ring.</li>
 * </ol>
 *
 * <p>Without the bolt shader, {@link Lightning} spawns vanilla's (visual-only) bolt instead; without the
 * relight shader the lightmap flashes harder. GPU resources are released after a few seconds idle,
 * on world exit and when the client stops. No allocation per frame.</p>
 */
public final class StormRenderer {
	private static final Supplier<String> LIGHT_LABEL = () -> "Liquid storm light";
	private static final Supplier<String> APPLY_LABEL = () -> "Liquid storm light apply";
	private static final Supplier<String> BOLT_LABEL = () -> "Liquid storm bolts";
	private static final int IDLE_FRAMES = 300;
	/** Vec4 slots of a bolt's header (the segments follow). */
	private static final int HEADER = 9;

	private static final FloatBlock BOLT = new FloatBlock(HEADER + Lightning.SEGMENTS * 2);
	private static final FloatBlock LIGHT = new FloatBlock(4 + 12 + 5);
	private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f INVERSE = new Matrix4f();
	/** One ring per bolt slot, each written once per frame. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final @Nullable DynamicGpuDataStorageMapped<FloatBlock>[] BOLT_RINGS = new DynamicGpuDataStorageMapped[Lightning.MAX_BOLTS];
	/** Halo width (times the core) and colours per bolt style: natural, cinematic, forked, neon. */
	private static final float[] HALO = {7f, 10f, 7.5f, 9f};
	private static final float[][] GLOW = {{0.55f, 0.62f, 1f}, {0.6f, 0.62f, 1.25f}, {0.55f, 0.62f, 1f}, {1.05f, 0.42f, 1.15f}};
	private static final float[][] CORE = {{1f, 1f, 1f}, {0.96f, 0.98f, 1f}, {1f, 1f, 1f}, {1f, 0.9f, 1f}};

	private static @Nullable GpuBuffer quads;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> lightRing;
	private static @Nullable GpuTexture lightTexture;
	private static @Nullable GpuTextureView lightView;
	private static int idleFrames;
	private static boolean warned;
	private static int nextFallbackId = -0x50000000;

	private StormRenderer() {
	}

	/** After the world and the haze, before the hand ({@code PostFx#afterLevel}). */
	public static void afterLevel(Matrix4f projection, CameraRenderState camera) {
		boolean anything = Storm.active() && camera.fogType == FogType.NONE && !ShaderPacks.inUse();
		if (!anything) {
			idle();
			return;
		}
		try {
			VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
			boolean lit = relight(camera);
			boolean bolts = bolts(projection, camera);
			if (lit || bolts) {
				idleFrames = 0;
			} else {
				idle();
			}
		} catch (RuntimeException e) {
			if (!warned) {
				warned = true;
				LiquidClient.LOGGER.error("Storm lightning pass failed; skipping it this frame", e);
			}
		}
	}

	private static void idle() {
		if ((quads != null || lightTexture != null) && ++idleFrames > IDLE_FRAMES) {
			release();
		}
	}

	// ---- Relighting ------------------------------------------------------------------------------------

	private static boolean relight(CameraRenderState camera) {
		StormChaserModule m = Storm.module();
		int lights = Lightning.lights();
		float sky = Lightning.globalFlash();
		if (!m.relight.get() || !ShaderSupport.stormLight() || lights == 0 && sky < 0.01f) {
			return false;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline light = compiled(LiquidPipelines.STORM_LIGHT);
		CompiledRenderPipeline apply = compiled(LiquidPipelines.STORM_LIGHT_APPLY);
		ClientLevel level = Minecraft.getInstance().level;
		if (color == null || colorView == null || depth == null || light == null || apply == null || level == null) {
			return false;
		}
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		int divisor = m.resolutionDivisor();
		int lw = Math.max(1, w / divisor);
		int lh = Math.max(1, h / divisor);
		ensureLight(lw, lh);
		GpuTextureView columns = LiquidWeather.columns(level);

		INVERSE.set(VIEW_PROJECTION).invert();
		LIGHT.mat4(0, INVERSE);
		for (int i = 0; i < 6; i++) {
			if (i < lights) {
				LIGHT.vec4(4 + i, Lightning.lightX(i), Lightning.lightY(i), Lightning.lightZ(i), Lightning.lightIntensity(i));
				LIGHT.vec4(10 + i, Lightning.lightEndX(i), Lightning.lightEndY(i), Lightning.lightEndZ(i), 0f);
			} else {
				LIGHT.vec4(4 + i, 0f, 0f, 0f, 0f);
				LIGHT.vec4(10 + i, 0f, 0f, 0f, 0f);
			}
		}
		boolean neon = m.boltStyle.index() == StormChaserModule.BOLT_NEON;
		LIGHT.vec4(16, neon ? 0.95f : 0.82f, neon ? 0.75f : 0.88f, 1f, lights);
		// A flash in a dark scene changes everything; in daylight under the storm a little less.
		float day = smoothstep(-0.12f, 0.2f, (float) Math.cos(StormSky.lastSunAngle()));
		float gain = m.brightness.getFloat() / 100f * mix(9f, 2.6f, day * (1f - 0.45f * Storm.darkness()));
		LIGHT.vec4(17, sky * 0.55f, gain, 120f, columns != null ? 1f : 0f);
		double x = camera.pos.x;
		double y = camera.pos.y;
		double z = camera.pos.z;
		int bx = (int) Math.floor(x);
		int by = (int) Math.floor(y);
		int bz = (int) Math.floor(z);
		LIGHT.vec4(18, bx, by, bz, 0f);
		LIGHT.vec4(19, (float) (x - bx), (float) (y - by), (float) (z - bz), 0f);
		LIGHT.vec4(20, w, h, lw, lh);
		DynamicGpuDataStorageMapped<FloatBlock> ring = lightRing;
		if (ring == null) {
			ring = lightRing = new DynamicGpuDataStorageMapped<>("Liquid storm light UBO", LIGHT.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(LIGHT);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		try (RenderPass pass = encoder.createRenderPass(LIGHT_LABEL, lightView, Optional.empty())) {
			pass.setPipeline(light);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidStormLight", slice);
			pass.setUniform("DepthSampler", depth, nearest);
			// Without columns any texture will do: the shader doesn't read it (Sky.w = 0).
			pass.setUniform("ColumnSampler", columns != null ? columns : depth, nearest);
			pass.draw(3, 1, 0, 0);
		}
		try (RenderPass pass = encoder.createRenderPass(APPLY_LABEL, colorView, Optional.empty())) {
			pass.setPipeline(apply);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidStormLight", slice);
			pass.setUniform("DepthSampler", depth, nearest);
			pass.setUniform("InSampler", lightView, nearest);
			pass.draw(3, 1, 0, 0);
		}
		return true;
	}

	private static void ensureLight(int w, int h) {
		GpuTexture current = lightTexture;
		if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h) {
			return;
		}
		releaseLight();
		GpuDevice device = RenderSystem.getDevice();
		lightTexture = device.createTexture(() -> "Liquid storm light", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
				GpuFormat.RGBA16_FLOAT, w, h, 1, 1);
		lightView = device.createTextureView(lightTexture);
	}

	// ---- Bolts -------------------------------------------------------------------------------------------

	/** Whether our bolts can be drawn (else {@link Lightning} falls back to vanilla's bolt entity). */
	static boolean canDrawBolts() {
		return ShaderSupport.stormBolts() && !ShaderPacks.inUse();
	}

	/** Ids for fallback bolt entities: negative, far from vanilla's and Kill Effects'. */
	static int nextFallbackId() {
		return nextFallbackId--;
	}

	private static boolean bolts(Matrix4f projection, CameraRenderState camera) {
		if (!canDrawBolts() || !Lightning.anyBolt()) {
			return false;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.STORM_BOLT);
		if (color == null || colorView == null || depth == null || pipeline == null) {
			return false;
		}
		if (quads == null) {
			quads = buildQuads();
		}
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		indices.requestIndexCount(Lightning.SEGMENTS * 6);
		StormChaserModule m = Storm.module();
		float brightness = m.brightness.getFloat() / 100f;
		int style = m.boltStyle.index();
		float maxDistance = Math.max(64f, camera.fogData.renderDistanceEnd * 0.97f);
		float visibility = 2500f + 20000f * (1f - 0.75f * Storm.rain());
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
				.createRenderPass(BOLT_LABEL, colorView, Optional.empty(), depth, OptionalDouble.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setIndexBuffer(indices.getBuffer(), indices.type());
			pass.setVertexBuffer(0, quads.slice());
			for (int i = 0; i < Lightning.MAX_BOLTS; i++) {
				Lightning.Bolt bolt = Lightning.BOLTS[i];
				if (!bolt.alive || bolt.count == 0 || bolt.channel < 0.002f && bolt.branches < 0.002f && bolt.progress > 1f) {
					continue;
				}
				writeBolt(bolt, camera, brightness, style, maxDistance, visibility, color.getHeight(0), projection.m11());
				DynamicGpuDataStorageMapped<FloatBlock> ring = BOLT_RINGS[i];
				if (ring == null) {
					ring = BOLT_RINGS[i] = new DynamicGpuDataStorageMapped<>("Liquid storm bolt UBO", BOLT.size(), GpuBuffer.USAGE_UNIFORM, 1);
				}
				ring.endFrame();
				GpuBufferSlice slice = ring.writeData(BOLT);
				pass.setUniform("LiquidBolt", slice);
				pass.drawIndexed(bolt.count * 6, 1, 0, 0, 0);
			}
		}
		return true;
	}

	private static void writeBolt(Lightning.Bolt bolt, CameraRenderState camera, float brightness, int style, float maxDistance,
			float visibility, int height, float projectionY) {
		BOLT.mat4(0, VIEW_PROJECTION);
		BOLT.vec4(4, (float) (bolt.x - camera.pos.x), (float) (bolt.y - camera.pos.y), (float) (bolt.z - camera.pos.z), Storm.base());
		float power = bolt.power * brightness;
		BOLT.vec4(5, bolt.channel * power, bolt.branches * power, bolt.progress, bolt.glow * power);
		float[] core = CORE[style];
		float[] glow = GLOW[style];
		BOLT.vec4(6, core[0], core[1], core[2], HALO[style]);
		BOLT.vec4(7, glow[0], glow[1], glow[2], visibility);
		BOLT.vec4(8, height, Math.abs(projectionY), maxDistance, 0f);
		BOLT.copy(HEADER, bolt.segments, 0, bolt.count * 8);
	}

	/** Lightning.SEGMENTS quads; each vertex carries (segment index, corner across, corner along). */
	private static GpuBuffer buildQuads() {
		int vertexSize = DefaultVertexFormat.POSITION.getVertexSize();
		ByteBuffer data = MemoryUtil.memAlloc(Lightning.SEGMENTS * 4 * vertexSize);
		try {
			for (int i = 0; i < Lightning.SEGMENTS; i++) {
				data.putFloat(i).putFloat(0f).putFloat(0f);
				data.putFloat(i).putFloat(1f).putFloat(0f);
				data.putFloat(i).putFloat(1f).putFloat(1f);
				data.putFloat(i).putFloat(0f).putFloat(1f);
			}
			data.flip();
			return RenderSystem.getDevice().createBuffer(() -> "Liquid storm bolt quads", GpuBuffer.USAGE_VERTEX, data);
		} finally {
			MemoryUtil.memFree(data);
		}
	}

	// ---- Resources ---------------------------------------------------------------------------------------

	private static void releaseLight() {
		if (lightView != null) {
			lightView.close();
			lightView = null;
		}
		if (lightTexture != null) {
			lightTexture.close();
			lightTexture = null;
		}
	}

	/** Frees every GPU resource (render thread, between frames). Idempotent. */
	public static void release() {
		releaseLight();
		if (quads != null) {
			quads.close();
			quads = null;
		}
		if (lightRing != null) {
			lightRing.close();
			lightRing = null;
		}
		for (int i = 0; i < BOLT_RINGS.length; i++) {
			if (BOLT_RINGS[i] != null) {
				BOLT_RINGS[i].close();
				BOLT_RINGS[i] = null;
			}
		}
		idleFrames = 0;
	}

	private static @Nullable CompiledRenderPipeline compiled(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
		return compiled == null || compiled.isClosed() ? null : compiled;
	}

	private static float mix(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
