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
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.client.visuals.LiquidClient;
import net.client.visuals.module.render.StormChaserModule;
import net.client.visuals.render.post.FloatBlock;
import net.client.visuals.render.post.PostFx;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The storm in the sky ({@code storm_sky.fsh}), drawn in the sky pass over whatever sky is there –
 * vanilla's or any Skybox mode – so it overrides them all; and the storm's light everywhere else:
 * the fog colour and distance, the lightmap's cold storm light, vanilla's clouds hidden.
 *
 * <h2>In the sky pass</h2>
 * {@code LiquidSky} calls {@link #draw} at the end of the sky (or instead of it, when the storm hides
 * the whole sky – {@link #coversSky}). Once per resource set a 256² noise texture is rendered; every
 * frame the storm is rendered at half resolution (or full, the Resolution setting) into a half-float
 * texture – the analytic storm, no volume march – and blended over the sky, premultiplied and
 * dithered. The view comes from this frame's level projection ({@link PostFx#frameProjection}).
 *
 * <p>GPU resources (the two textures, a uniform ring) are created on first use, released after a few
 * seconds without a storm, on world exit and when the client stops – render thread, between frames.
 * Without the shaders the sky stays vanilla's (darkened by the storm's rain and thunder levels).</p>
 */
public final class StormSky {
	private static final Supplier<String> NOISE_LABEL = () -> "Liquid storm noise";
	private static final Supplier<String> SKY_LABEL = () -> "Liquid storm sky";
	private static final Supplier<String> COMPOSITE_LABEL = () -> "Liquid storm sky composite";
	private static final int NOISE_SIZE = 256;
	private static final int IDLE_FRAMES = 300;

	private static final FloatBlock SKY = new FloatBlock(19);
	private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f INVERSE = new Matrix4f();
	private static final Vector3f SKY_LIGHT = new Vector3f();
	private static final Vector3f AMBIENT = new Vector3f();
	/** Storm colours per tint (dark cloud, lit cloud, rain haze), rgb each. */
	private static final float[][] TINTS = {
			{0.07f, 0.078f, 0.09f, 0.2f, 0.215f, 0.23f, 0.30f, 0.33f, 0.37f},
			{0.05f, 0.085f, 0.068f, 0.16f, 0.235f, 0.2f, 0.25f, 0.33f, 0.3f},
			{0.08f, 0.062f, 0.1f, 0.22f, 0.175f, 0.27f, 0.31f, 0.28f, 0.37f}
	};
	/** The light under the storm per tint: cold blue, sickly green, violet. */
	private static final float[][] LIGHT_TINTS = {{0.78f, 0.86f, 1f}, {0.74f, 1f, 0.8f}, {0.88f, 0.76f, 1f}};

	private static @Nullable GpuTexture noise;
	private static @Nullable GpuTextureView noiseView;
	private static boolean noiseReady;
	private static @Nullable GpuTexture sky;
	private static @Nullable GpuTextureView skyView;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> ring;
	private static long drawnFrame = -1L;
	private static int idleFrames;
	private static boolean warned;
	private static float sunAngle;
	private static float day = 1f;
	// The haze colour last written, for the fog.
	private static float hazeR = 0.3f;
	private static float hazeG = 0.33f;
	private static float hazeB = 0.37f;
	private static float flashR = 0.78f;
	private static float flashG = 0.82f;
	private static float flashB = 1f;

	private StormSky() {
	}

	// ---- The sky pass --------------------------------------------------------------------------------

	/** Start of the sky pass: what the sun is doing (also used to pick where a storm comes from). */
	public static void observe(SkyRenderState state) {
		sunAngle = state.sunAngle;
		day = smoothstep(-0.12f, 0.2f, (float) Math.cos(state.sunAngle));
	}

	static float lastSunAngle() {
		return sunAngle;
	}

	private static boolean wanted(SkyRenderState state) {
		CameraRenderState camera = PostFx.frameCamera();
		return Storm.active() && state.skybox == DimensionType.Skybox.OVERWORLD && ShaderSupport.stormSky() && !ShaderPacks.inUse()
				&& camera != null && camera.fogType == FogType.NONE && Storm.front() > -60000f;
	}

	/**
	 * The storm hides the whole sky (deep in the rain core, fully there): the sky underneath needn't
	 * be drawn at all.
	 */
	public static boolean coversSky(SkyRenderState state) {
		return wanted(state) && Storm.presence() > 0.995f && Storm.under() > 0.995f && Storm.rain() > 0.9f;
	}

	/** Draws the storm over the sky (once per frame; render thread, no pass open). */
	public static void draw(SkyRenderState state) {
		long frame = Storm.frameCount();
		if (frame == drawnFrame) {
			return;
		}
		drawnFrame = frame;
		if (!wanted(state)) {
			if (sky != null && ++idleFrames > IDLE_FRAMES) {
				release();
			}
			return;
		}
		Matrix4f projection = PostFx.frameProjection();
		CameraRenderState camera = PostFx.frameCamera();
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		CompiledRenderPipeline noisePipeline = compiled(LiquidPipelines.STORM_NOISE);
		CompiledRenderPipeline skyPipeline = compiled(LiquidPipelines.STORM_SKY);
		CompiledRenderPipeline composite = compiled(LiquidPipelines.STORM_SKY_COMPOSITE);
		if (projection == null || camera == null || color == null || colorView == null || noisePipeline == null || skyPipeline == null
				|| composite == null) {
			return;
		}
		idleFrames = 0;
		try {
			StormChaserModule m = Storm.module();
			int divisor = m.resolutionDivisor();
			int w = Math.max(1, color.getWidth(0) / divisor);
			int h = Math.max(1, color.getHeight(0) / divisor);
			ensure(w, h);
			CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
			if (!noiseReady) {
				try (RenderPass pass = encoder.createRenderPass(NOISE_LABEL, noiseView, Optional.empty())) {
					pass.setPipeline(noisePipeline);
					RenderSystem.bindDefaultUniforms(pass);
					pass.draw(3, 1, 0, 0);
				}
				noiseReady = true;
			}
			write(m, state, projection, camera, h);
			DynamicGpuDataStorageMapped<FloatBlock> uniforms = ring;
			if (uniforms == null) {
				uniforms = ring = new DynamicGpuDataStorageMapped<>("Liquid storm sky UBO", SKY.size(), GpuBuffer.USAGE_UNIFORM, 1);
			}
			uniforms.endFrame();
			GpuBufferSlice slice = uniforms.writeData(SKY);
			try (RenderPass pass = encoder.createRenderPass(SKY_LABEL, skyView, Optional.empty())) {
				pass.setPipeline(skyPipeline);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("LiquidStormSky", slice);
				pass.setUniform("NoiseSampler", noiseView, RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR));
				pass.draw(3, 1, 0, 0);
			}
			try (RenderPass pass = encoder.createRenderPass(COMPOSITE_LABEL, colorView, Optional.empty())) {
				pass.setPipeline(composite);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("InSampler", skyView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				pass.draw(3, 1, 0, 0);
			}
		} catch (RuntimeException e) {
			if (!warned) {
				warned = true;
				LiquidClient.LOGGER.error("Storm sky failed; skipping it this frame", e);
			}
		}
	}

	private static void write(StormChaserModule m, SkyRenderState state, Matrix4f projection, CameraRenderState camera, int height) {
		VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		INVERSE.set(VIEW_PROJECTION).invert();
		SKY.mat4(0, INVERSE);
		float ax = Storm.axisX();
		float az = Storm.axisZ();
		SKY.vec4(4, ax, az, -az, ax);
		SKY.vec4(5, Storm.front(), Storm.back(), Storm.base(), Storm.presence());
		double along = camera.pos.x * ax + camera.pos.z * az;
		double side = -camera.pos.x * az + camera.pos.z * ax;
		SKY.vec4(6, (float) floorMod(along, Storm.NOISE_PERIOD), (float) floorMod(side, Storm.NOISE_PERIOD), (float) Storm.drift(),
				(float) (Storm.time() % 600.0));

		// The light: the sun by day, the moon (dimly) by night.
		float sunY = (float) Math.cos(state.sunAngle);
		float lowSun = smoothstep(0.45f, 0.04f, sunY) * smoothstep(-0.06f, 0.02f, sunY);
		float r = 1f;
		float g = 0.9f;
		float b = 0.78f;
		Vector4fc sunset = state.sunriseAndSunsetColor;
		if (sunset != null && sunset.w() > 0f) {
			r = mix(r, sunset.x(), sunset.w() * 0.7f);
			g = mix(g, sunset.y(), sunset.w() * 0.7f);
			b = mix(b, sunset.z(), sunset.w() * 0.7f);
		}
		if (day > 0.02f) {
			SKY.vec4(7, (float) -Math.sin(state.sunAngle), sunY, 0f, day);
			SKY.vec4(8, r * day, g * day, b * day, lowSun);
		} else {
			float moonY = (float) Math.cos(state.moonAngle);
			SKY.vec4(7, (float) -Math.sin(state.moonAngle), moonY, 0f, 0f);
			SKY.vec4(8, 0.12f, 0.13f, 0.16f, 0f);
		}

		// The storm's colours: darker the deeper the storm, a touch of the sunset by day.
		float[] tint = TINTS[m.tint.index()];
		float gloom = mix(0.11f, 1f, day) * (1f - 0.22f * Storm.darkness());
		float warm = sunset != null ? sunset.w() * day * 0.06f : 0f;
		float sr = sunset != null ? sunset.x() : 0f;
		float sg = sunset != null ? sunset.y() : 0f;
		float sb = sunset != null ? sunset.z() : 0f;
		SKY.vec4(9, tint[0] * gloom, tint[1] * gloom, tint[2] * gloom, Math.min(1f, m.intensity.getFloat() / 100f + 0.15f));
		SKY.vec4(10, tint[3] * gloom + sr * warm, tint[4] * gloom + sg * warm, tint[5] * gloom + sb * warm, 1f);
		hazeR = tint[6] * gloom + sr * warm;
		hazeG = tint[7] * gloom + sg * warm;
		hazeB = tint[8] * gloom + sb * warm;
		SKY.vec4(11, hazeR, hazeG, hazeB, 30000f);

		// Lightning inside the cloud.
		for (int i = 0; i < 4; i++) {
			SKY.vec4(12 + i, Lightning.skyX(i), Lightning.skyY(i), Lightning.skyZ(i), Lightning.skyIntensity(i));
		}
		flashColor(m);
		SKY.vec4(16, flashR, flashG, flashB, Lightning.globalFlash());
		float pixelAngle = 2f / (Math.max(0.1f, Math.abs(projection.m11())) * height);
		SKY.vec4(17, pixelAngle, m.quality.index(), 1f, 0.35f * Storm.influence());
		SKY.vec4(18, Storm.across(), 0f, 0f, 0f);
	}

	/** The flash's colour for the bolt style: blue-white, or violet for neon. */
	private static void flashColor(StormChaserModule m) {
		if (m.boltStyle.index() == StormChaserModule.BOLT_NEON) {
			flashR = 0.92f;
			flashG = 0.66f;
			flashB = 1f;
		} else {
			flashR = 0.78f;
			flashG = 0.82f;
			flashB = 1f;
		}
	}

	private static void ensure(int w, int h) {
		GpuDevice device = RenderSystem.getDevice();
		int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING;
		if (noise == null || noise.isClosed()) {
			releaseNoise();
			noise = device.createTexture(() -> "Liquid storm noise", usage, GpuFormat.RGBA16_FLOAT, NOISE_SIZE, NOISE_SIZE, 1, 1);
			noiseView = device.createTextureView(noise);
			noiseReady = false;
		}
		GpuTexture current = sky;
		if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h) {
			return;
		}
		releaseSky();
		sky = device.createTexture(() -> "Liquid storm sky", usage, GpuFormat.RGBA16_FLOAT, w, h, 1, 1);
		skyView = device.createTextureView(sky);
	}

	// ---- The storm's light elsewhere -------------------------------------------------------------------

	/**
	 * End of vanilla's fog colour ({@code FogRendererMixin}, after the Skybox's): the air turns the
	 * storm's grey, and every flash lights it.
	 */
	public static void adjustFogColor(Camera camera, Vector4f dest) {
		if (!Storm.active() || camera.getFluidInCamera() != FogType.NONE) {
			return;
		}
		float k = Math.clamp(Storm.darkness() * 0.85f + Storm.influence() * 0.15f, 0f, 0.92f);
		float flash = Lightning.globalFlash() * 0.22f;
		dest.set(Math.min(1f, mix(dest.x, hazeR, k) + flashR * flash),
				Math.min(1f, mix(dest.y, hazeG, k) + flashG * flash),
				Math.min(1f, mix(dest.z, hazeB, k) + flashB * flash),
				dest.w);
	}

	/** {@code FogRendererMixin#setupFog}: heavy rain closes in the distance. */
	public static void adjustFog(FogData fog, Camera camera) {
		if (!Storm.active() || camera.getFluidInCamera() != FogType.NONE) {
			return;
		}
		float k = 1f - 0.42f * Storm.rain() * Math.min(1f, Storm.module().intensity.getFloat() / 100f);
		fog.environmentalStart *= k;
		fog.environmentalEnd *= k;
	}

	/**
	 * The lightmap (once per tick, after the Lighting module): darker and colder under the storm –
	 * blue, green or violet with the tint – and a little light from each flash.
	 */
	public static void applyLightmap(LightmapRenderState state) {
		if (!Storm.active()) {
			return;
		}
		StormChaserModule m = Storm.module();
		float dark = Storm.darkness();
		float cold = Math.clamp(dark * 0.55f + Storm.influence() * 0.15f, 0f, 0.7f);
		float[] tint = LIGHT_TINTS[m.tint.index()];
		Vector3fc base = state.skyLightColor;
		state.skyLightColor = SKY_LIGHT.set(base.x() * mix(1f, tint[0], cold), base.y() * mix(1f, tint[1], cold), base.z() * mix(1f, tint[2], cold));
		Vector3fc ambient = state.ambientColor;
		state.ambientColor = AMBIENT.set(ambient.x() * mix(1f, tint[0], cold), ambient.y() * mix(1f, tint[1], cold), ambient.z() * mix(1f, tint[2], cold));
		float flash = Lightning.globalFlash() * (m.relight.get() && ShaderSupport.stormLight() ? 0.12f : 0.3f);
		state.skyFactor = Math.min(1f, state.skyFactor * (1f - 0.28f * dark) + flash);
	}

	/** {@code CloudRendererMixin}: vanilla's white block clouds don't belong under a storm. */
	public static boolean hidesClouds() {
		return Storm.active() && (Storm.influence() > 0.15f || Storm.under() > 0.02f) && ShaderSupport.stormSky() && !ShaderPacks.inUse();
	}

	// ---- Resources ---------------------------------------------------------------------------------------

	private static void releaseNoise() {
		if (noiseView != null) {
			noiseView.close();
			noiseView = null;
		}
		if (noise != null) {
			noise.close();
			noise = null;
		}
		noiseReady = false;
	}

	private static void releaseSky() {
		if (skyView != null) {
			skyView.close();
			skyView = null;
		}
		if (sky != null) {
			sky.close();
			sky = null;
		}
	}

	/** Frees every GPU resource (render thread, between frames). Idempotent. */
	public static void release() {
		releaseNoise();
		releaseSky();
		if (ring != null) {
			ring.close();
			ring = null;
		}
		idleFrames = 0;
	}

	private static @Nullable CompiledRenderPipeline compiled(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
		return compiled == null || compiled.isClosed() ? null : compiled;
	}

	private static double floorMod(double v, double m) {
		return v - Math.floor(v / m) * m;
	}

	private static float mix(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
