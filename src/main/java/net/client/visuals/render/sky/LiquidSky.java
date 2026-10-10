package net.client.visuals.render.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.client.visuals.LiquidClient;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.SkyboxModule;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.shader.LiquidPipelines;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Renders the Skybox module's sky inside the frame graph's "sky" pass.
 *
 * <h2>Hook</h2>
 * {@code SkyRendererMixin} calls {@link #beforeVanilla} at the head of {@code SkyRenderer#render}
 * and {@link #afterVanilla} at its tail. Fully faded in, we draw and cancel vanilla. While cross-fading
 * we draw translucently over vanilla's sky. Only the overworld skybox is touched.
 *
 * <h2>Lifecycle (no leaks)</h2>
 * GPU resources (cube vertex buffer, uniform ring, galaxy texture) are created lazily on the render
 * thread the first time they're needed. They're released when the module has fully faded out, on
 * disconnect and on client stop, always between frames. The renderer holds no world references,
 * only per-frame {@link SkyRenderState} values.
 *
 * <h2>Allocation</h2>
 * Matrices, the transform record and the uniform block are pre-allocated and mutated in place.
 */
public final class LiquidSky {
	private static final Supplier<String> PASS_LABEL = () -> "Liquid Sky";
	private static final Supplier<String> CUBE_LABEL = () -> "Liquid Sky cube";
	private static final float FULL = 0.999f;

	private static final Smooth FADE = new Smooth(0f, Motion.CROSSFADE);
	private static final SkyUniforms DATA = new SkyUniforms();
	private static final Matrix4f MODEL_VIEW = new Matrix4f();
	private static final Matrix4f IDENTITY = new Matrix4f();
	private static final Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f);
	private static final Vector3f NO_OFFSET = new Vector3f();
	/** One record instance referencing the mutable matrix above – rewritten, never re-created. */
	private static final DynamicGpuData.Transform TRANSFORM = new DynamicGpuData.Transform(MODEL_VIEW, WHITE, NO_OFFSET, IDENTITY);
	private static final GalaxyTexture GALAXY = new GalaxyTexture();

	private static @Nullable GpuBuffer cube;
	private static @Nullable DynamicGpuDataStorageMapped<SkyUniforms> uniforms;
	private static boolean broken;
	private static long lastNanos = -1L;
	private static float time;
	private static float rotation;
	private static int lastMode = -1;
	/** Last sun height / weather seen by the sky pass, for the CPU-side fog colour. */
	private static float lastSunY = 1f;
	private static float lastClear = 1f;

	/**
	 * Horizon colour of each mode at day / night (rgb), mirroring the shader, so terrain fog can
	 * blend into the custom sky. {@code null} = keep vanilla fog (Atmospheric Fog mode).
	 */
	private static final float[][] HORIZON_DAY = {
			{0.03f, 0.03f, 0.08f}, {0.50f, 0.80f, 1.00f}, {1.00f, 0.83f, 0.64f}, null,
			{0.02f, 0.07f, 0.10f}, {0.05f, 0.02f, 0.09f}, {0.75f, 0.22f, 0.40f}, {0.01f, 0.01f, 0.02f},
			{0.38f, 0.10f, 0.06f}, {1.00f, 0.66f, 0.34f}, {0.42f, 0.05f, 0.05f}, {1.00f, 0.72f, 0.86f},
			{0.00f, 0.06f, 0.02f}, {0.00f, 0.00f, 0.00f}, {0.85f, 0.80f, 1.00f}, {0.00f, 0.32f, 0.42f},
			{0.60f, 0.78f, 0.97f}, {0.60f, 0.66f, 0.74f}
	};
	private static final float[][] HORIZON_NIGHT = {
			{0.03f, 0.03f, 0.08f}, {0.06f, 0.05f, 0.16f}, {0.40f, 0.32f, 0.45f}, null,
			{0.02f, 0.07f, 0.10f}, {0.05f, 0.02f, 0.09f}, {0.75f, 0.22f, 0.40f}, {0.01f, 0.01f, 0.02f},
			{0.38f, 0.10f, 0.06f}, {0.20f, 0.12f, 0.15f}, {0.42f, 0.05f, 0.05f}, {0.40f, 0.25f, 0.45f},
			{0.00f, 0.06f, 0.02f}, {0.00f, 0.00f, 0.00f}, {0.20f, 0.15f, 0.30f}, {0.00f, 0.08f, 0.12f},
			{0.045f, 0.055f, 0.12f}, {0.03f, 0.034f, 0.048f}
	};

	private LiquidSky() {
	}

	/**
	 * Head of {@code SkyRenderer#render}.
	 *
	 * @return {@code true} if we drew the full sky and vanilla should be skipped
	 */
	public static boolean beforeVanilla(GpuBufferSlice skyFog, SkyRenderState state) {
		if (state.skybox != DimensionType.Skybox.OVERWORLD || broken) {
			return false;
		}
		float fade = advance();
		if (fade >= FULL) {
			RenderSystem.setShaderFog(skyFog);
			return draw(state, 1f);
		}
		return false;
	}

	/** Tail of {@code SkyRenderer#render}: translucent overlay while cross-fading. */
	public static void afterVanilla(SkyRenderState state) {
		if (state.skybox != DimensionType.Skybox.OVERWORLD || broken) {
			return;
		}
		float fade = FADE.get();
		if (fade > 0.001f && fade < FULL) {
			draw(state, fade);
		}
	}

	/** Advances timers and the cross-fade once per frame; frees resources once fully faded out. */
	private static float advance() {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0E-9f);
		lastNanos = now;

		SkyboxModule module = ModuleManager.get(SkyboxModule.class);
		int mode = module.mode.index();
		if (mode != lastMode) {
			if (module.isEnabled()) {
				LiquidClient.LOGGER.info("Sky mode -> {}", SkyboxModule.MODES[mode]);
			}
			lastMode = mode;
			if (mode != SkyboxModule.MODE_GALAXY) {
				GALAXY.release(); // the 32 MB texture is only kept while it's in use
			}
		}
		boolean wantsGalaxy = module.isEnabled() && mode == SkyboxModule.MODE_GALAXY;
		if (wantsGalaxy) {
			GALAXY.request();
		}
		// While the galaxy loads we stay on vanilla, then fade in once it's ready.
		boolean visible = module.isEnabled() && (!wantsGalaxy || GALAXY.ready());

		FADE.speed(Motion.speed(Motion.CROSSFADE));
		float fade = FADE.target(visible).update(dt);
		if (fade > 0f) {
			time = (time + dt * module.animationSpeed.getFloat()) % 3600f;
			rotation = (rotation + dt * module.rotationSpeed.getFloat() * 0.02f) % (float) (Math.PI * 2.0);
		}
		// Not while the galaxy is still loading: releasing would cancel it, and the next frame would
		// start it all over again.
		if (!visible && !wantsGalaxy && FADE.settled() && fade == 0f) {
			release();
		}
		return fade;
	}

	private static boolean draw(SkyRenderState state, float alpha) {
		SkyboxModule module = ModuleManager.get(SkyboxModule.class);
		boolean galaxy = module.mode.index() == SkyboxModule.MODE_GALAXY && GALAXY.ready();
		RenderPipeline pipeline = galaxy ? LiquidPipelines.SKY_GALAXY : LiquidPipelines.SKY;
		try {
			CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
			if (compiled == null) {
				fail(null);
				return false;
			}
			ensureResources();
			fillUniforms(state, module, alpha);

			DynamicGpuDataStorageMapped<SkyUniforms> ring = uniforms;
			ring.endFrame();
			GpuBufferSlice skySlice = ring.writeData(DATA);
			MODEL_VIEW.set(RenderSystem.getModelViewStack());
			GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(TRANSFORM);

			RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
			try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
					.createRenderPass(PASS_LABEL, target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
				pass.setPipeline(compiled);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("DynamicTransforms", transforms);
				pass.setUniform("LiquidSky", skySlice);
				if (galaxy) {
					DynamicTexture texture = GALAXY.texture();
					pass.setUniform("Sampler0", texture.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR));
				}
				pass.setVertexBuffer(0, cube.slice());
				pass.draw(36, 1, 0, 0);
			}
			return true;
		} catch (RuntimeException e) {
			fail(e);
			return false;
		}
	}

	private static void fillUniforms(SkyRenderState state, SkyboxModule module, float alpha) {
		// Vanilla: rotate Y by -90°, then X by the angle; bodies sit on +Y.
		float sun = state.sunAngle;
		float moon = state.moonAngle;
		SkyUniforms.set(DATA.sunDir, (float) -Math.sin(sun), (float) Math.cos(sun), 0f, state.rainBrightness);
		SkyUniforms.set(DATA.moonDir, (float) -Math.sin(moon), (float) Math.cos(moon), 0f, state.moonPhase.index());

		Vector3fc sky = state.skyColor;
		if (sky != null) {
			SkyUniforms.set(DATA.skyBase, sky.x(), sky.y(), sky.z(), state.starBrightness);
		} else {
			SkyUniforms.set(DATA.skyBase, 0.47f, 0.65f, 1f, state.starBrightness);
		}
		Vector4fc sunset = state.sunriseAndSunsetColor;
		if (sunset != null) {
			SkyUniforms.set(DATA.sunset, sunset.x(), sunset.y(), sunset.z(), sunset.w());
		} else {
			SkyUniforms.set(DATA.sunset, 0f, 0f, 0f, 0f);
		}

		int rgb = hueToRgb(module.tintHue.getFloat());
		SkyUniforms.set(DATA.tint, ((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f,
				module.tintStrength.getFloat() / 100f);
		SkyUniforms.set(DATA.params, time, state.starAngle + rotation,
				module.starDensity.getFloat() / 100f, module.horizonBlend.getFloat() / 100f);
		SkyUniforms.set(DATA.params2, alpha, module.mode.index(), module.clouds.index(), module.stars.index());
		SkyUniforms.set(DATA.params3, module.brightness.getFloat() / 100f, module.cloudCoverage.getFloat() / 100f, 0f, 0f);
		lastSunY = (float) Math.cos(sun);
		lastClear = state.rainBrightness;
	}

	/** Fully saturated colour for a hue in degrees, as packed RGB. */
	private static int hueToRgb(float hueDegrees) {
		float h = ((hueDegrees % 360f) + 360f) % 360f / 60f;
		float x = 1f - Math.abs(h % 2f - 1f);
		float r;
		float g;
		float b;
		if (h < 1f) {
			r = 1f; g = x; b = 0f;
		} else if (h < 2f) {
			r = x; g = 1f; b = 0f;
		} else if (h < 3f) {
			r = 0f; g = 1f; b = x;
		} else if (h < 4f) {
			r = 0f; g = x; b = 1f;
		} else if (h < 5f) {
			r = x; g = 0f; b = 1f;
		} else {
			r = 1f; g = 0f; b = x;
		}
		return Math.round(r * 255f) << 16 | Math.round(g * 255f) << 8 | Math.round(b * 255f);
	}

	private static void ensureResources() {
		if (uniforms == null) {
			uniforms = new DynamicGpuDataStorageMapped<>("Liquid Sky UBO", SkyUniforms.SIZE, GpuBuffer.USAGE_UNIFORM, 1);
		}
		if (cube == null) {
			cube = buildCube();
		}
	}

	/** 12 triangles of a camera-centred cube (inside faces; culling is off anyway). */
	private static GpuBuffer buildCube() {
		float s = 16f;
		float[][] corners = {
				{-s, -s, -s}, {s, -s, -s}, {s, s, -s}, {-s, s, -s},
				{-s, -s, s}, {s, -s, s}, {s, s, s}, {-s, s, s}
		};
		int[] faces = {
				0, 1, 2, 0, 2, 3, // -Z
				5, 4, 7, 5, 7, 6, // +Z
				4, 0, 3, 4, 3, 7, // -X
				1, 5, 6, 1, 6, 2, // +X
				3, 2, 6, 3, 6, 7, // +Y
				4, 5, 1, 4, 1, 0  // -Y
		};
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(faces.length * DefaultVertexFormat.POSITION.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION);
			for (int index : faces) {
				float[] c = corners[index];
				builder.addVertex(c[0], c[1], c[2]);
			}
			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(CUBE_LABEL, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
			}
		}
	}

	private static void fail(@Nullable Throwable error) {
		broken = true;
		LiquidClient.LOGGER.error("Liquid Sky disabled for this session – falling back to the vanilla sky", error);
		release();
	}

	/** Frees every GPU resource. Render thread, between frames. Idempotent. */
	public static void release() {
		if (cube != null || uniforms != null) {
			LiquidClient.LOGGER.info("Sky resources released");
		}
		GALAXY.release();
		if (cube != null) {
			cube.close();
			cube = null;
		}
		if (uniforms != null) {
			uniforms.close();
			uniforms = null;
		}
		lastMode = -1;
	}

	/**
	 * Called at the end of vanilla's fog colour computation ({@code FogRendererMixin}): pulls the
	 * terrain fog towards the active mode's horizon colour, scaled by the cross-fade, so distant
	 * terrain melts into the custom sky instead of vanilla blue. Air only (not water / lava), and
	 * only in dimensions with an overworld skybox.
	 */
	public static void adjustFog(Camera camera, ClientLevel level, Vector4f dest) {
		float fade = FADE.get();
		if (fade <= 0.001f || broken || camera.getFluidInCamera() != FogType.NONE
				|| level.dimensionType().skybox() != DimensionType.Skybox.OVERWORLD) {
			return;
		}
		SkyboxModule module = ModuleManager.get(SkyboxModule.class);
		int mode = module.mode.index();
		if (!module.syncFog.get() || HORIZON_DAY[mode] == null) {
			return;
		}
		float day = smoothstep(-0.22f, 0.22f, lastSunY);
		float[] d = HORIZON_DAY[mode];
		float[] n = HORIZON_NIGHT[mode];
		float r = n[0] + (d[0] - n[0]) * day;
		float g = n[1] + (d[1] - n[1]) * day;
		float b = n[2] + (d[2] - n[2]) * day;
		// Same post-processing as the shader: rain, tint, brightness.
		float lum = r * 0.2126f + g * 0.7152f + b * 0.0722f;
		float rain = (1f - lastClear) * 0.6f;
		r += (lum * 0.6f - r) * rain;
		g += (lum * 0.6f - g) * rain;
		b += (lum * 0.6f - b) * rain;
		float strength = module.tintStrength.getFloat() / 100f;
		if (strength > 0f) {
			int rgb = hueToRgb(module.tintHue.getFloat());
			lum = r * 0.2126f + g * 0.7152f + b * 0.0722f;
			r += (((rgb >> 16) & 0xFF) / 255f * lum * 1.6f + r * 0.15f - r) * strength;
			g += (((rgb >> 8) & 0xFF) / 255f * lum * 1.6f + g * 0.15f - g) * strength;
			b += ((rgb & 0xFF) / 255f * lum * 1.6f + b * 0.15f - b) * strength;
		}
		float bright = module.brightness.getFloat() / 100f;
		dest.set(dest.x + (Math.min(1f, r * bright) - dest.x) * fade,
				dest.y + (Math.min(1f, g * bright) - dest.y) * fade,
				dest.z + (Math.min(1f, b * bright) - dest.z) * fade,
				dest.w);
	}

	/** {@code CloudRendererMixin}: hide vanilla clouds while the Skybox is (mostly) visible. */
	public static boolean hideVanillaClouds() {
		return !broken && FADE.get() > 0.5f && ModuleManager.get(SkyboxModule.class).hideClouds.get();
	}

	private static float smoothstep(float e0, float e1, float v) {
		float t = Math.clamp((v - e0) / (e1 - e0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}

	/**
	 * Client tick: when no overworld sky has been drawn for a while (Nether / End, menus) the sky
	 * pass never runs, so free resources here instead of keeping a 32 MB texture around.
	 */
	public static void tick() {
		boolean holding = cube != null || uniforms != null || GALAXY.ready();
		if (holding && lastNanos > 0L && System.nanoTime() - lastNanos > 2_000_000_000L) {
			FADE.snap(0f);
			lastNanos = -1L;
			release();
		}
	}

	/** World left: reset the fade so re-joining fades in again, and free everything. */
	public static void onWorldExit() {
		FADE.snap(0f);
		lastNanos = -1L;
		release();
	}
}
