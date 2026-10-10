package net.client.visuals.render.post;

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
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.client.visuals.LiquidClient;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.AtmosphereModule;
import net.client.visuals.module.render.BloomModule;
import net.client.visuals.module.render.AmbientOcclusionModule;
import net.client.visuals.module.render.ColorGradingModule;
import net.client.visuals.module.render.DepthOfFieldModule;
import net.client.visuals.module.render.MotionBlurModule;
import net.client.visuals.module.render.RainbowsModule;
import net.client.visuals.module.render.WetWorldModule;
import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.module.render.SunRaysModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.client.visuals.render.storm.Lightning;
import net.client.visuals.render.storm.Storm;
import net.client.visuals.render.storm.StormRenderer;
import net.client.visuals.render.storm.StormSounds;
import net.client.visuals.render.weather.LiquidWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * The client's own post-processing: Ambient Occlusion, Wet World, Atmosphere (haze and valley mist),
 * Rainbows, Sun Rays, Depth of Field, Motion Blur, Wet Lens, Bloom and Color Grading.
 *
 * <h2>Where in the frame</h2>
 * <ul>
 *   <li>Ambient Occlusion, then Wet World – {@link #beforeTranslucents} / {@link #beforeOit}: over the
 *       opaque world, before water, particles and weather (with classic transparency the main pass is
 *       closed and reopened around them, like the haze below). AO: linear depth at half resolution
 *       with three smaller copies, the horizon search (far taps read the small copies), two
 *       depth-aware blurs, a depth-aware upsample multiplied into the frame.</li>
 *   <li>Atmosphere, in two steps so particles stay crisp (they write no depth, so a single haze
 *       over the finished world would fog them as if they were as far as the terrain behind them):
 *       <ol>
 *         <li>{@link #splitMainPass} / {@link #beforeOit} – right before particles are drawn
 *             ({@code LevelRendererMixin}; with classic transparency that's after water and before
 *             particles, clouds and weather): the depth is copied and everything so far hazed.
 *             Vanilla draws all of it in one render pass, so it's closed and reopened around the
 *             haze (closing is idempotent).</li>
 *         <li>{@link #afterLevel} – after the world, before the hand ({@code GameRendererMixin}):
 *             only pixels that came nearer since (clouds; with OIT also water and glass) are hazed.
 *             If the first step didn't run, the whole frame is hazed here instead.</li>
 *       </ol></li>
 *   <li>Storm Chaser's lightning – in {@link #afterLevel}, after the haze: the flash relighting the world,
 *       then the bolts ({@code render.storm.StormRenderer}). The storm itself advances in
 *       {@link #beginLevel}; its sky is drawn in the sky pass ({@code render.storm.StormSky}).</li>
 *   <li>Rainbows – in {@link #afterLevel}, after the haze and the lightning.</li>
 *   <li>Sun Rays – also in {@link #afterLevel}, after the haze and before the hand (so the hand covers
 *       them): a half-resolution sky mask around the sun, two radial blurs towards it and an additive
 *       composite with the lens flare.</li>
 *   <li>Depth of Field – in {@link #afterLevel}, after the rays: autofocus on the GPU into a 1×1
 *       texture (eased from last frame's, no read-back), a half-resolution scatter-as-gather bokeh
 *       with its foreground tiles dilated, then a full-resolution composite from a copy of the frame.
 *       The hand, drawn after, stays sharp.</li>
 *   <li>Motion Blur – last in {@link #afterLevel}: the world (with its haze, rainbows and rays) is
 *       blurred along each pixel's screen motion, reprojected with last frame's camera; the hand,
 *       drawn after, stays sharp.</li>
 *   <li>Wet Lens – first thing in {@link #beforePostEffects}: rewrites the frame from a copy of itself,
 *       so Bloom then glows through the drops.</li>
 *   <li>{@link #beforePostEffects} – after the hand and entity outlines, before vanilla's post
 *       effects and the GUI: one copy of the frame, the bloom mip chain from it, then one full-screen
 *       pass grades and composites back into the frame. The GUI is never graded.</li>
 * </ul>
 *
 * <p>GPU resources (a frame-sized copy, half-float bloom levels and effect textures, uniform rings) are
 * created on first use, recreated on resize and released after a few seconds without any effect,
 * on world exit and when the client stops – always on the render thread. Each pass is skipped
 * when its pipeline didn't compile ({@link ShaderSupport}); nothing here allocates per frame.</p>
 */
public final class PostFx {
	private static final Supplier<String> HAZE_LABEL = () -> "Liquid atmosphere";
	private static final Supplier<String> HAZE_TRANSLUCENT_LABEL = () -> "Liquid atmosphere (translucents)";
	private static final Supplier<String> REOPENED_LABEL = () -> "Main (translucents, after atmosphere)";
	private static final Supplier<String> PREFILTER_LABEL = () -> "Liquid bloom prefilter";
	private static final Supplier<String> DOWN_LABEL = () -> "Liquid bloom downsample";
	private static final Supplier<String> UP_LABEL = () -> "Liquid bloom upsample";
	private static final Supplier<String> GRADE_LABEL = () -> "Liquid color grading";
	private static final Supplier<String> RAYS_MASK_LABEL = () -> "Liquid sun rays mask";
	private static final Supplier<String> RAYS_BLUR_LABEL = () -> "Liquid sun rays blur";
	private static final Supplier<String> RAYS_COMPOSITE_LABEL = () -> "Liquid sun rays";
	private static final Supplier<String> LENS_LABEL = () -> "Liquid wet lens";
	private static final Supplier<String> WET_LABEL = () -> "Liquid wet world";
	private static final Supplier<String> RAINBOW_LABEL = () -> "Liquid rainbow";
	private static final Supplier<String> MOTION_LABEL = () -> "Liquid motion blur";
	private static final Supplier<String> AO_DEPTH_LABEL = () -> "Liquid ambient occlusion depth";
	private static final Supplier<String> AO_LABEL = () -> "Liquid ambient occlusion";
	private static final Supplier<String> AO_BLUR_LABEL = () -> "Liquid ambient occlusion blur";
	private static final Supplier<String> AO_APPLY_LABEL = () -> "Liquid ambient occlusion apply";
	private static final Supplier<String> DOF_FOCUS_LABEL = () -> "Liquid depth of field autofocus";
	private static final Supplier<String> DOF_LABEL = () -> "Liquid depth of field";
	private static final Supplier<String> DOF_COMPOSITE_LABEL = () -> "Liquid depth of field composite";
	private static final int MAX_LEVELS = 6;
	/** Frames without any effect before the textures are freed (~5 s at 60 FPS). */
	private static final int IDLE_FRAMES = 300;

	private static final FloatBlock HAZE = new FloatBlock(9);
	private static final FloatBlock BLOOM = new FloatBlock(1);
	private static final FloatBlock GRADE = new FloatBlock(9);
	private static final FloatBlock RAYS = new FloatBlock(9);
	private static final FloatBlock LENS = new FloatBlock(3);
	private static final FloatBlock WET = new FloatBlock(16);
	private static final FloatBlock RAINBOW = new FloatBlock(7);
	private static final Matrix4f WET_INVERSE = new Matrix4f();
	private static final FloatBlock MOTION = new FloatBlock(10);
	private static final Matrix4f MOTION_VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f MOTION_INVERSE = new Matrix4f();
	private static final Matrix4f PREVIOUS_VIEW_PROJECTION = new Matrix4f();
	private static final FloatBlock AO = new FloatBlock(9);
	private static final Matrix4f AO_INVERSE = new Matrix4f();
	private static final GpuTexture[] AO_TEXTURES = new GpuTexture[2];
	private static final GpuTextureView[] AO_VIEWS = new GpuTextureView[2];
	/** Linear depth for AO: its own resolution, then 1/2, 1/4 and 1/8 of it. */
	private static final GpuTexture[] AO_DEPTH = new GpuTexture[4];
	private static final GpuTextureView[] AO_DEPTH_VIEWS = new GpuTextureView[4];
	private static final FloatBlock DOF = new FloatBlock(11);
	private static final Matrix4f DOF_INVERSE = new Matrix4f();
	/** Depth of Field, half resolution: the prepared frame (later the filtered blur), the prefiltered frame, the gather. */
	private static final GpuTexture[] DOF_HALF = new GpuTexture[3];
	private static final GpuTextureView[] DOF_HALF_VIEWS = new GpuTextureView[3];
	/** Foreground tiles: the largest near CoC per tile, then dilated. */
	private static final GpuTexture[] DOF_TILES = new GpuTexture[2];
	private static final GpuTextureView[] DOF_TILE_VIEWS = new GpuTextureView[2];
	/** The autofocus, 1×1, ping-ponged with last frame's. */
	private static final GpuTexture[] DOF_FOCUS = new GpuTexture[2];
	private static final GpuTextureView[] DOF_FOCUS_VIEWS = new GpuTextureView[2];
	/** Half-res texels per foreground tile (post_dof.fsh TILE reads 8×8). */
	private static final int DOF_TILE = 8;
	private static final Matrix4f INVERSE_VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f RAYS_INVERSE = new Matrix4f();
	private static final Vector4f LIGHT_CLIP = new Vector4f();
	private static final GpuTexture[] RAY_TEXTURES = new GpuTexture[2];
	private static final GpuTextureView[] RAY_VIEWS = new GpuTextureView[2];
	private static final GpuTexture[] MIPS = new GpuTexture[MAX_LEVELS];
	private static final GpuTextureView[] MIP_VIEWS = new GpuTextureView[MAX_LEVELS];
	private static final long START = System.nanoTime();

	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> hazeRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> bloomRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> gradeRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> raysRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> lensRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> wetRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> rainbowRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> motionRing;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> aoRing;
	private static int aoWidth;
	private static int aoHeight;
	private static int aoIdleFrames;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> dofRing;
	private static int dofWidth;
	private static int dofHeight;
	private static int dofIdleFrames;
	/** Which of {@link #DOF_FOCUS} holds last frame's focus, and whether it's worth easing from. */
	private static int dofFocus;
	private static boolean dofFocusValid;
	private static long dofNanos = -1L;
	/** This frame's level matrices and states ({@link #beginLevel}); null outside the level render. */
	private static @Nullable Matrix4f frameProjection;
	private static @Nullable CameraRenderState frameCamera;
	private static @Nullable SkyRenderState frameSky;
	private static @Nullable GpuBufferSlice hazeSlice;
	private static boolean opaqueHazed;
	private static @Nullable RenderPass reopened;
	private static @Nullable GpuTexture opaqueDepth;
	private static @Nullable GpuTextureView opaqueDepthView;
	private static @Nullable GpuTexture scene;
	private static @Nullable GpuTextureView sceneView;
	private static int mipWidth;
	private static int mipHeight;
	private static int idleFrames;
	private static int raysIdleFrames;
	private static int rayWidth;
	private static int rayHeight;
	private static boolean warned;

	private static @Nullable AtmosphereModule atmosphere;
	private static @Nullable BloomModule bloom;
	private static @Nullable ColorGradingModule grading;
	private static @Nullable SunRaysModule sunRays;
	private static @Nullable MotionBlurModule motionBlur;
	private static @Nullable AmbientOcclusionModule ambientOcclusion;
	private static @Nullable DepthOfFieldModule depthOfField;
	/** Last frame's camera, for Motion Blur's reprojection. */
	private static boolean previousValid;
	private static double previousX;
	private static double previousY;
	private static double previousZ;
	private static float previousYaw;
	private static float previousPitch;
	private static int previousWidth;
	private static int previousHeight;
	private static long motionNanos = -1L;

	private PostFx() {
	}

	// ---- Atmosphere ----------------------------------------------------------------------------

	/** Before the level renders (render thread): this frame's projection (with view bobbing) and states. */
	public static void beginLevel(Matrix4f projection, CameraRenderState camera, SkyRenderState sky) {
		ShaderSupport.ensureValidated();
		frameProjection = projection;
		frameCamera = camera;
		frameSky = sky;
		hazeSlice = null;
		opaqueHazed = false;
		// The storm, its lightning and its thunder advance once per frame, before anything draws them.
		try {
			Storm.frame(camera.pos.x, camera.pos.y, camera.pos.z);
			ClientLevel level = Minecraft.getInstance().level;
			if (level != null) {
				Lightning.frame(level, camera.pos.x, camera.pos.y, camera.pos.z);
				StormSounds.frame(level, camera.pos.x, camera.pos.y, camera.pos.z);
			}
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** This frame's level projection (view bobbing included) while the level renders, else null. */
	public static @Nullable Matrix4f frameProjection() {
		return frameProjection;
	}

	/** This frame's camera while the level renders, else null. */
	public static @Nullable CameraRenderState frameCamera() {
		return frameCamera;
	}

	private static boolean hazeWanted() {
		AtmosphereModule m = atmosphere;
		if (m == null) {
			m = atmosphere = ModuleManager.get(AtmosphereModule.class);
		}
		CameraRenderState camera = frameCamera;
		return m.isEnabled() && camera != null && camera.fogType == FogType.NONE && ShaderSupport.haze() && !ShaderPacks.inUse();
	}

	/**
	 * Classic transparency: vanilla's main pass is about to draw particles, clouds and weather (the
	 * world and water are in). Closes it, hazes what's there, and hands back a reopened pass on the
	 * same targets – or the original pass untouched when there's no haze to draw.
	 */
	public static RenderPass splitMainPass(RenderPass pass) {
		if (opaqueHazed || !hazeWanted() || !ShaderSupport.hazeTranslucent()) {
			return pass;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		if (color == null || depth == null) {
			return pass;
		}
		pass.close(); // vanilla's pass, or the one beforeTranslucents reopened
		hazeOpaque();
		return reopen(color, depth);
	}

	/** A new pass on the main targets in place of the one we closed; closed by {@link #endSplitPass}. */
	private static RenderPass reopen(GpuTextureView color, GpuTextureView depth) {
		RenderPass next = RenderSystem.getDevice().createCommandEncoder()
				.createRenderPass(REOPENED_LABEL, color, Optional.empty(), depth, OptionalDouble.empty());
		RenderSystem.bindDefaultUniforms(next);
		reopened = next;
		return next;
	}

	/**
	 * Classic transparency, start of the translucent step (the opaque world is in): Wet World, in a
	 * pass of its own – vanilla's is closed and reopened around it.
	 */
	public static RenderPass beforeTranslucents(RenderPass pass) {
		boolean ao = aoWanted();
		boolean wet = wetWanted();
		if (!ao && !wet) {
			idleAo();
			return pass;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		if (color == null || depth == null) {
			return pass;
		}
		pass.close();
		beforeTranslucentsUnsplit(ao, wet);
		return reopen(color, depth);
	}

	/** Ambient Occlusion, then Wet World, with no render pass open. */
	private static void beforeTranslucentsUnsplit(boolean ao, boolean wet) {
		try {
			if (ao) {
				drawAo();
			} else {
				idleAo();
			}
			if (wet) {
				drawWet();
			}
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** End of vanilla's classic-transparency step: closes the pass {@link #splitMainPass} opened. */
	public static void endSplitPass() {
		RenderPass pass = reopened;
		if (pass != null) {
			reopened = null;
			pass.close();
		}
	}

	/** Improved transparency (OIT): no pass is open before the translucent stages (water and particles together). */
	public static void beforeOit() {
		beforeTranslucentsUnsplit(aoWanted(), wetWanted());
		if (hazeWanted() && ShaderSupport.hazeTranslucent()) {
			hazeOpaque();
		}
	}

	/** Copies the depth so far (for the translucent pass) and hazes what's been drawn. */
	private static void hazeOpaque() {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture depth = target.getDepthTexture();
		if (depth == null || depth.isClosed()) {
			return;
		}
		try {
			ensureOpaqueDepth(depth);
			RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(depth, opaqueDepth, 0, 0, 0, 0, 0,
					depth.getWidth(0), depth.getHeight(0));
			opaqueHazed = drawHaze(LiquidPipelines.POST_HAZE, HAZE_LABEL, false);
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** After the world, before the hand (render thread). */
	public static void afterLevel() {
		try {
			if (hazeWanted()) {
				if (opaqueHazed) {
					drawHaze(LiquidPipelines.POST_HAZE_TRANSLUCENT, HAZE_TRANSLUCENT_LABEL, true);
				} else {
					drawHaze(LiquidPipelines.POST_HAZE, HAZE_LABEL, false);
				}
			}
			Matrix4f projection = frameProjection;
			CameraRenderState camera = frameCamera;
			if (projection != null && camera != null) {
				StormRenderer.afterLevel(projection, camera);
			}
			drawRainbow();
			if (!drawSunRays() && RAY_TEXTURES[0] != null && ++raysIdleFrames > IDLE_FRAMES) {
				releaseRayTextures();
			}
			if (!drawDof()) {
				// The next time it runs, focus snaps to what's in view instead of easing from a stale value.
				dofFocusValid = false;
				if (DOF_HALF[0] != null && ++dofIdleFrames > IDLE_FRAMES) {
					releaseDofTextures();
				}
			}
			drawMotionBlur();
		} catch (RuntimeException e) {
			warnOnce(e);
		} finally {
			frameProjection = null;
			frameCamera = null;
			frameSky = null;
			opaqueHazed = false;
		}
	}

	private static boolean drawHaze(RenderPipeline pipeline, Supplier<String> label, boolean translucentOnly) {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline compiled = compiled(pipeline);
		Matrix4f projection = frameProjection;
		CameraRenderState camera = frameCamera;
		SkyRenderState sky = frameSky;
		if (color == null || depth == null || compiled == null || projection == null || camera == null || sky == null
				|| translucentOnly && opaqueDepthView == null) {
			return false;
		}
		GpuBufferSlice slice = hazeSlice;
		if (slice == null) {
			writeHaze(atmosphere, projection, camera, sky);
			DynamicGpuDataStorageMapped<FloatBlock> ring = hazeRing;
			if (ring == null) {
				ring = hazeRing = new DynamicGpuDataStorageMapped<>("Liquid haze UBO", HAZE.size(), GpuBuffer.USAGE_UNIFORM, 1);
			}
			ring.endFrame();
			slice = hazeSlice = ring.writeData(HAZE);
		}
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(label, color, Optional.empty())) {
			pass.setPipeline(compiled);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidHaze", slice);
			pass.setUniform("DepthSampler", depth, nearest);
			if (translucentOnly) {
				pass.setUniform("OpaqueDepthSampler", opaqueDepthView, nearest);
			}
			pass.draw(3, 1, 0, 0);
		}
		return true;
	}

	private static void writeHaze(@Nullable AtmosphereModule m, Matrix4f projection, CameraRenderState camera, SkyRenderState sky) {
		if (m == null) {
			return;
		}
		INVERSE_VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix).invert();
		HAZE.mat4(0, INVERSE_VIEW_PROJECTION);

		// Haze takes vanilla's fog colour (time of day, weather, Skybox / Custom Fog) or a tint of it.
		Vector4f fog = camera.fogData.color;
		float r = fog.x;
		float g = fog.y;
		float b = fog.z;
		float luminance = Math.max(0.04f, 0.2126f * r + 0.7152f * g + 0.0722f * b);
		int mode = m.color.index();
		if (mode != 0) {
			int tint = switch (mode) {
				case AtmosphereModule.COLOR_WARM -> 0xFFFFC08A;
				case AtmosphereModule.COLOR_COOL -> 0xFF9EC2FF;
				default -> ColorUtil.hsv(m.hue.getFloat(), 0.45f, 1f);
			};
			float tr = ((tint >> 16) & 0xFF) / 255f;
			float tg = ((tint >> 8) & 0xFF) / 255f;
			float tb = (tint & 0xFF) / 255f;
			float scale = luminance / Math.max(0.05f, 0.2126f * tr + 0.7152f * tg + 0.0722f * tb);
			r = mix(r, Math.min(1f, tr * scale), 0.75f);
			g = mix(g, Math.min(1f, tg * scale), 0.75f);
			b = mix(b, Math.min(1f, tb * scale), 0.75f);
		}
		// A storm thickens the air: heavier haze and mist, and more of it allowed.
		float storm = Storm.haze();
		float density = Math.min(1f, m.density.getFloat() / 100f + storm * 0.25f);
		HAZE.vec4(4, r, g, b, density);
		// Mist reads a little lighter than the haze, like a cloud bank seen from above.
		float mist = (m.mist.getFloat() / 100f + storm * 0.6f) * 0.04f;
		HAZE.vec4(5, mix(r, 1f, 0.22f * luminance + 0.08f), mix(g, 1f, 0.22f * luminance + 0.08f), mix(b, 1f, 0.22f * luminance + 0.08f), mist);

		// Sun glow by day (sunrise / sunset colour when there is one), a faint cool moon glow by night.
		float sunX = (float) -Math.sin(sky.sunAngle);
		float sunY = (float) Math.cos(sky.sunAngle);
		float day = smoothstep(-0.12f, 0.12f, sunY);
		float clear = sky.rainBrightness;
		float glow = m.sunGlow.getFloat() / 100f * clear;
		Vector4fc sunset = sky.sunriseAndSunsetColor;
		float sr = 1f;
		float sg = 0.88f;
		float sb = 0.68f;
		if (sunset != null && sunset.w() > 0f) {
			sr = mix(sr, sunset.x(), sunset.w());
			sg = mix(sg, sunset.y(), sunset.w());
			sb = mix(sb, sunset.z(), sunset.w());
		}
		if (day > 0f) {
			HAZE.vec4(6, mix(r, sr, day), mix(g, sg, day), mix(b, sb, day), glow * day);
			HAZE.vec4(7, sunX, sunY, 0f, (float) camera.pos.y);
		} else {
			float moonX = (float) -Math.sin(sky.moonAngle);
			float moonY = (float) Math.cos(sky.moonAngle);
			HAZE.vec4(6, mix(r, 0.62f, 0.5f), mix(g, 0.7f, 0.5f), mix(b, 0.92f, 0.5f), glow * 0.35f * smoothstep(-0.1f, 0.2f, moonY));
			HAZE.vec4(7, moonX, moonY, 0f, (float) camera.pos.y);
		}
		HAZE.vec4(8, (m.haze.getFloat() / 100f + storm * 0.9f) * 0.012f, m.hazeStart.getFloat() * (1f - 0.6f * storm), m.mistHeight.getFloat(),
				m.mistThickness.getFloat() * (1f + storm));
	}

	// ---- Ambient Occlusion ---------------------------------------------------------------------

	private static boolean aoWanted() {
		AmbientOcclusionModule m = ambientOcclusion;
		if (m == null) {
			m = ambientOcclusion = ModuleManager.get(AmbientOcclusionModule.class);
		}
		return m.isEnabled() && ShaderSupport.ao() && !ShaderPacks.inUse() && frameProjection != null && frameCamera != null;
	}

	private static void idleAo() {
		if (AO_TEXTURES[0] != null && ++aoIdleFrames > IDLE_FRAMES) {
			releaseAoTextures();
		}
	}

	private static void drawAo() {
		AmbientOcclusionModule m = ambientOcclusion;
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture colorTexture = target.getColorTexture();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		Matrix4f projection = frameProjection;
		boolean preview = m != null && m.preview.get();
		CompiledRenderPipeline linearize = compiled(LiquidPipelines.POST_AO_LINEARIZE);
		CompiledRenderPipeline downsample = compiled(LiquidPipelines.POST_AO_DOWNSAMPLE);
		CompiledRenderPipeline compute = compiled(LiquidPipelines.POST_AO);
		CompiledRenderPipeline blurH = compiled(LiquidPipelines.POST_AO_BLUR_H);
		CompiledRenderPipeline blurV = compiled(LiquidPipelines.POST_AO_BLUR_V);
		CompiledRenderPipeline apply = compiled(preview ? LiquidPipelines.POST_AO_PREVIEW : LiquidPipelines.POST_AO_APPLY);
		if (m == null || colorTexture == null || color == null || depth == null || projection == null || linearize == null || downsample == null
				|| compute == null || blurH == null || blurV == null || apply == null) {
			return;
		}
		aoIdleFrames = 0;
		int w = colorTexture.getWidth(0);
		int h = colorTexture.getHeight(0);
		// Half resolution at every quality: with the depth-aware upsample it looks like full resolution
		// at a quarter of the cost (full resolution took ~10 ms on a GTX 1650 at 1080p).
		int aw = Math.max(1, w / 2);
		int ah = Math.max(1, h / 2);
		ensureAoTextures(aw, ah);

		AO_INVERSE.set(projection).invert();
		AO.mat4(0, AO_INVERSE);
		AO.vec4(4, projection.m00(), projection.m11(), aw, ah);
		AO.vec4(5, m.radius.getFloat(), m.strength.getFloat() / 100f, m.contrast.getFloat() / 100f, 0.12f);
		int directions = switch (m.quality.index()) {
			case 0 -> 4;
			case 2 -> 8;
			default -> 6;
		};
		int steps = switch (m.quality.index()) {
			case 0 -> 4;
			case 2 -> 8;
			default -> 6;
		};
		AO.vec4(6, directions, steps, 40f, 72f);
		// Full / AO resolution, and the largest radius on screen: a quarter of its height.
		AO.vec4(7, w, h, 2f, ah * 0.25f);
		AO.vec4(8, PhotoCapture.noiseSeed(), 0f, 0f, 0f);
		DynamicGpuDataStorageMapped<FloatBlock> ring = aoRing;
		if (ring == null) {
			ring = aoRing = new DynamicGpuDataStorageMapped<>("Liquid ambient occlusion UBO", AO.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(AO);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		aoPass(encoder, AO_DEPTH_LABEL, linearize, slice, AO_DEPTH_VIEWS[0], null, depth, nearest);
		for (int i = 1; i < AO_DEPTH.length; i++) {
			aoPass(encoder, AO_DEPTH_LABEL, downsample, slice, AO_DEPTH_VIEWS[i], null, AO_DEPTH_VIEWS[i - 1], nearest);
		}
		try (RenderPass pass = encoder.createRenderPass(AO_LABEL, AO_VIEWS[0], Optional.empty())) {
			pass.setPipeline(compute);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidAo", slice);
			pass.setUniform("DepthSampler", AO_DEPTH_VIEWS[0], nearest);
			pass.setUniform("Depth1Sampler", AO_DEPTH_VIEWS[1], nearest);
			pass.setUniform("Depth2Sampler", AO_DEPTH_VIEWS[2], nearest);
			pass.setUniform("Depth3Sampler", AO_DEPTH_VIEWS[3], nearest);
			pass.draw(3, 1, 0, 0);
		}
		aoPass(encoder, AO_BLUR_LABEL, blurH, slice, AO_VIEWS[1], AO_VIEWS[0], null, nearest);
		aoPass(encoder, AO_BLUR_LABEL, blurV, slice, AO_VIEWS[0], AO_VIEWS[1], null, nearest);
		aoPass(encoder, AO_APPLY_LABEL, apply, slice, color, AO_VIEWS[0], depth, nearest);
	}

	private static void aoPass(CommandEncoder encoder, Supplier<String> label, CompiledRenderPipeline pipeline, GpuBufferSlice params,
			GpuTextureView destination, @Nullable GpuTextureView source, @Nullable GpuTextureView depth, GpuSampler nearest) {
		try (RenderPass pass = encoder.createRenderPass(label, destination, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidAo", params);
			if (source != null) {
				pass.setUniform("InSampler", source, nearest);
			}
			if (depth != null) {
				pass.setUniform("DepthSampler", depth, nearest);
			}
			pass.draw(3, 1, 0, 0);
		}
	}

	private static void ensureAoTextures(int w, int h) {
		if (AO_TEXTURES[0] != null && !AO_TEXTURES[0].isClosed() && aoWidth == w && aoHeight == h) {
			return;
		}
		releaseAoTextures();
		GpuDevice device = RenderSystem.getDevice();
		int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING;
		for (int i = 0; i < 2; i++) {
			AO_TEXTURES[i] = device.createTexture(() -> "Liquid ambient occlusion", usage, GpuFormat.RGBA16_FLOAT, w, h, 1, 1);
			AO_VIEWS[i] = device.createTextureView(AO_TEXTURES[i]);
		}
		for (int i = 0; i < AO_DEPTH.length; i++) {
			AO_DEPTH[i] = device.createTexture(() -> "Liquid ambient occlusion depth", usage, GpuFormat.R32_FLOAT, Math.max(1, w >> i),
					Math.max(1, h >> i), 1, 1);
			AO_DEPTH_VIEWS[i] = device.createTextureView(AO_DEPTH[i]);
		}
		aoWidth = w;
		aoHeight = h;
	}

	private static void releaseAoTextures() {
		closeAll(AO_TEXTURES, AO_VIEWS);
		closeAll(AO_DEPTH, AO_DEPTH_VIEWS);
		aoIdleFrames = 0;
	}

	// ---- Depth of Field ------------------------------------------------------------------------

	/** Draws Depth of Field over the world; false when it's off or can't run. */
	private static boolean drawDof() {
		DepthOfFieldModule m = depthOfField;
		if (m == null) {
			m = depthOfField = ModuleManager.get(DepthOfFieldModule.class);
		}
		Matrix4f projection = frameProjection;
		// In Photo Mode only the camera's own depth of field counts (off = everything sharp).
		boolean wanted = PhotoMode.active() ? PhotoMode.depthOfField() : m.isEnabled();
		if (!wanted || !ShaderSupport.dof() || ShaderPacks.inUse() || projection == null || frameCamera == null) {
			return false;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline focusPass = compiled(LiquidPipelines.POST_DOF_FOCUS);
		CompiledRenderPipeline prepare = compiled(LiquidPipelines.POST_DOF_PREPARE);
		CompiledRenderPipeline tile = compiled(LiquidPipelines.POST_DOF_TILE);
		CompiledRenderPipeline dilate = compiled(LiquidPipelines.POST_DOF_DILATE);
		CompiledRenderPipeline prefilter = compiled(LiquidPipelines.POST_DOF_PREFILTER);
		CompiledRenderPipeline gather = compiled(LiquidPipelines.POST_DOF_GATHER);
		CompiledRenderPipeline filter = compiled(LiquidPipelines.POST_DOF_FILTER);
		CompiledRenderPipeline composite = compiled(LiquidPipelines.POST_DOF_COMPOSITE);
		if (color == null || colorView == null || depth == null || color.isClosed() || focusPass == null || prepare == null || tile == null
				|| dilate == null || prefilter == null || gather == null || filter == null || composite == null) {
			return false;
		}
		dofIdleFrames = 0;
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		int hw = Math.max(1, (w + 1) / 2);
		int hh = Math.max(1, (h + 1) / 2);
		ensureDofTextures(hw, hh);
		ensureScene(color, w, h);
		GpuBufferSlice slice = writeDof(m, projection, w, h, hw, hh);

		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		int current = 1 - dofFocus;
		GpuTextureView focus = DOF_FOCUS_VIEWS[current];
		GpuTextureView prepared = DOF_HALF_VIEWS[0];
		GpuTextureView prefiltered = DOF_HALF_VIEWS[1];
		GpuTextureView gathered = DOF_HALF_VIEWS[2];
		dofPass(encoder, DOF_FOCUS_LABEL, focusPass, slice, focus, DOF_FOCUS_VIEWS[dofFocus], nearest, depth, null, null, null, null);
		dofPass(encoder, DOF_LABEL, prepare, slice, prepared, colorView, nearest, depth, focus, null, null, null);
		dofPass(encoder, DOF_LABEL, tile, slice, DOF_TILE_VIEWS[0], prepared, nearest, null, null, null, null, null);
		dofPass(encoder, DOF_LABEL, dilate, slice, DOF_TILE_VIEWS[1], DOF_TILE_VIEWS[0], nearest, null, null, null, null, null);
		dofPass(encoder, DOF_LABEL, prefilter, slice, prefiltered, prepared, linear, null, null, null, null, null);
		dofPass(encoder, DOF_LABEL, gather, slice, gathered, prefiltered, linear, null, null, null, null, DOF_TILE_VIEWS[1]);
		// The prepared texture is free again: the filtered blur goes there (the CoC lives on in the prefiltered one).
		dofPass(encoder, DOF_LABEL, filter, slice, prepared, gathered, linear, null, null, prefiltered, null, null);
		encoder.copyTextureToTexture(color, scene, 0, 0, 0, 0, 0, w, h);
		dofPass(encoder, DOF_COMPOSITE_LABEL, composite, slice, colorView, sceneView, linear, depth, focus, prefiltered, prepared, null);
		dofFocus = current;
		dofFocusValid = true;
		return true;
	}

	private static GpuBufferSlice writeDof(DepthOfFieldModule m, Matrix4f projection, int w, int h, int hw, int hh) {
		// Photo Mode's camera drives the lens while it's open (its focal length is the field of view itself).
		boolean photo = PhotoMode.depthOfField();
		PhotoModeModule p = PhotoMode.module();

		// Autofocus eases like a focus ring being turned (frame-rate independent); a fresh start snaps.
		long now = System.nanoTime();
		float dt = dofNanos < 0L ? 0f : Math.min(0.25f, (now - dofNanos) * 1.0e-9f);
		dofNanos = now;
		float speed = photo ? 9f : m.focusSpeed.getFloat();
		float ease = !dofFocusValid || dt <= 0f ? 1f : 1f - (float) Math.exp(-speed * dt);

		DOF_INVERSE.set(projection).invert();
		DOF.mat4(0, DOF_INVERSE);
		// A thin lens on a full-frame sensor (24 mm tall): the blur circle's radius is f² / (2 N · 24 mm)
		// image heights per diopter. Zooming in (a narrower field of view than the setting) is a longer lens.
		float focal;
		if (photo) {
			focal = PhotoMode.focal() * 0.001f;
		} else {
			double fov = Math.toRadians(Minecraft.getInstance().options.fov().get());
			float zoom = Math.clamp(Math.abs(projection.m11()) * (float) Math.tan(fov * 0.5), 0.5f, 10f);
			focal = m.focalLength.getFloat() * 0.001f * zoom;
		}
		float aperture = photo ? p.aperture.getFloat() : m.aperture.getFloat();
		float perDiopter = focal * focal / (2f * aperture * 0.024f) * hh;
		float maxCoc = Math.min(48f, (photo ? 4.5f : m.maxBlur.getFloat()) / 100f * hh);
		int mode = photo ? (p.focusMode.index() == PhotoModeModule.FOCUS_MANUAL ? 1 : 0) : m.focusMode.index();
		float distance = photo ? p.focusDistance.getFloat() : m.focusDistance.getFloat();
		DOF.vec4(4, perDiopter, maxCoc, photo || m.nearBlur.get() ? 1f : 0f, mode);
		DOF.vec4(5, 1f / distance, ease, m.tiltPosition.getFloat() / 100f, m.tiltWidth.getFloat() / 200f);
		// Highlights: up to 31× brighter at full white (30 · s^1.5); fringing: ±12 % CoC per channel at most.
		float highlights = photo ? 0.6f : m.highlights.getFloat() / 100f;
		float fringing = photo ? 0.25f : m.fringing.getFloat() / 100f;
		int taps = photo ? (PhotoCapture.holdsCamera() ? 128 : 64) : switch (m.quality.index()) {
			case 0 -> 32;
			case 2 -> 128;
			default -> 64;
		};
		int bokeh = photo ? p.bokeh.index() : m.bokeh.index();
		DOF.vec4(6, bokeh, 30f * highlights * (float) Math.sqrt(highlights), fringing * 0.12f, taps);
		DOF.vec4(7, w, h, hw, hh);
		DOF.vec4(8, (hw + DOF_TILE - 1) / DOF_TILE, (hh + DOF_TILE - 1) / DOF_TILE, DOF_TILE, Math.min(6, (int) Math.ceil(maxCoc / DOF_TILE)));
		DOF.vec4(9, w / (float) Math.max(1, h), 0.75f, photo ? 0f : m.focusAssist.index(), PhotoCapture.noiseSeed());
		DOF.vec4(10, photo ? PhotoMode.focusU() : 0.5f, photo ? PhotoMode.focusV() : 0.5f, 0f, 0f);
		DynamicGpuDataStorageMapped<FloatBlock> ring = dofRing;
		if (ring == null) {
			ring = dofRing = new DynamicGpuDataStorageMapped<>("Liquid depth of field UBO", DOF.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		return ring.writeData(DOF);
	}

	/**
	 * One Depth of Field pass. {@code InSampler} is always bound; the others only when given (each
	 * pipeline's layout names exactly the ones its pass gets). Depth, focus and tiles are read texel
	 * by texel; the CoC between texels too.
	 */
	private static void dofPass(CommandEncoder encoder, Supplier<String> label, CompiledRenderPipeline pipeline, GpuBufferSlice params,
			GpuTextureView destination, GpuTextureView in, GpuSampler inSampler, @Nullable GpuTextureView depth, @Nullable GpuTextureView focus,
			@Nullable GpuTextureView coc, @Nullable GpuTextureView blur, @Nullable GpuTextureView tiles) {
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		try (RenderPass pass = encoder.createRenderPass(label, destination, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidDof", params);
			pass.setUniform("InSampler", in, inSampler);
			if (depth != null) {
				pass.setUniform("DepthSampler", depth, nearest);
			}
			if (focus != null) {
				pass.setUniform("FocusSampler", focus, nearest);
			}
			if (coc != null) {
				pass.setUniform("CocSampler", coc, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			}
			if (blur != null) {
				pass.setUniform("BlurSampler", blur, nearest);
			}
			if (tiles != null) {
				pass.setUniform("TileSampler", tiles, nearest);
			}
			pass.draw(3, 1, 0, 0);
		}
	}

	private static void ensureDofTextures(int hw, int hh) {
		if (DOF_HALF[0] != null && !DOF_HALF[0].isClosed() && dofWidth == hw && dofHeight == hh) {
			return;
		}
		releaseDofTextures();
		GpuDevice device = RenderSystem.getDevice();
		int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING;
		for (int i = 0; i < DOF_HALF.length; i++) {
			DOF_HALF[i] = device.createTexture(() -> "Liquid depth of field", usage, GpuFormat.RGBA16_FLOAT, hw, hh, 1, 1);
			DOF_HALF_VIEWS[i] = device.createTextureView(DOF_HALF[i]);
		}
		int tw = (hw + DOF_TILE - 1) / DOF_TILE;
		int th = (hh + DOF_TILE - 1) / DOF_TILE;
		for (int i = 0; i < 2; i++) {
			DOF_TILES[i] = device.createTexture(() -> "Liquid depth of field tiles", usage, GpuFormat.RGBA16_FLOAT, tw, th, 1, 1);
			DOF_TILE_VIEWS[i] = device.createTextureView(DOF_TILES[i]);
			DOF_FOCUS[i] = device.createTexture(() -> "Liquid depth of field focus", usage, GpuFormat.RGBA16_FLOAT, 1, 1, 1, 1);
			DOF_FOCUS_VIEWS[i] = device.createTextureView(DOF_FOCUS[i]);
		}
		dofWidth = hw;
		dofHeight = hh;
		dofFocusValid = false;
	}

	private static void releaseDofTextures() {
		closeAll(DOF_HALF, DOF_HALF_VIEWS);
		closeAll(DOF_TILES, DOF_TILE_VIEWS);
		closeAll(DOF_FOCUS, DOF_FOCUS_VIEWS);
		dofIdleFrames = 0;
		dofFocusValid = false;
		dofNanos = -1L;
	}

	private static void closeAll(GpuTexture[] textures, GpuTextureView[] views) {
		for (int i = 0; i < textures.length; i++) {
			if (views[i] != null) {
				views[i].close();
				views[i] = null;
			}
			if (textures[i] != null) {
				textures[i].close();
				textures[i] = null;
			}
		}
	}

	// ---- Wet World -----------------------------------------------------------------------------

	private static boolean wetWanted() {
		CameraRenderState camera = frameCamera;
		SkyRenderState sky = frameSky;
		return WetWorld.visible() && ShaderSupport.wet() && !ShaderPacks.inUse() && camera != null && sky != null
				&& frameProjection != null && sky.skybox == DimensionType.Skybox.OVERWORLD;
	}

	private static void drawWet() {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.POST_WET);
		ClientLevel level = Minecraft.getInstance().level;
		Matrix4f projection = frameProjection;
		CameraRenderState camera = frameCamera;
		SkyRenderState sky = frameSky;
		if (color == null || colorView == null || depth == null || pipeline == null || level == null || projection == null || camera == null
				|| sky == null) {
			return;
		}
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		ensureScene(color, w, h);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuTextureView columns = LiquidWeather.columns(level);
		encoder.copyTextureToTexture(color, scene, 0, 0, 0, 0, 0, w, h);
		writeWet(projection, camera, sky, columns != null, w, h);
		DynamicGpuDataStorageMapped<FloatBlock> ring = wetRing;
		if (ring == null) {
			ring = wetRing = new DynamicGpuDataStorageMapped<>("Liquid wet world UBO", WET.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(WET);
		GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		try (RenderPass pass = encoder.createRenderPass(WET_LABEL, colorView, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidWet", slice);
			pass.setUniform("InSampler", sceneView, linear);
			pass.setUniform("DepthSampler", depth, nearest);
			// Without columns any colour texture will do: the shader ignores it (CameraFrac.w = 0).
			pass.setUniform("ColumnSampler", columns != null ? columns : sceneView, nearest);
			pass.draw(3, 1, 0, 0);
		}
	}

	private static void writeWet(Matrix4f projection, CameraRenderState camera, SkyRenderState sky, boolean columns, int w, int h) {
		WetWorldModule m = WetWorld.module();
		VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		WET_INVERSE.set(VIEW_PROJECTION).invert();
		WET.mat4(0, WET_INVERSE);
		WET.mat4(4, VIEW_PROJECTION);
		double x = camera.pos.x;
		double y = camera.pos.y;
		double z = camera.pos.z;
		int bx = (int) Math.floor(x);
		int by = (int) Math.floor(y);
		int bz = (int) Math.floor(z);
		WET.vec4(8, bx, by, bz, ((System.nanoTime() - START) * 1.0e-9f) % 3600f);
		WET.vec4(9, (float) (x - bx), (float) (y - by), (float) (z - bz), columns ? 1f : 0f);
		WET.vec4(10, WetWorld.wet(), WetWorld.puddles(), WetWorld.ripples(), m.darkening.getFloat() / 100f);

		// Sky to reflect: vanilla's sky colour overhead, the fog colour at the horizon.
		Vector4f fog = camera.fogData.color;
		Vector3fc zenith = sky.skyColor;
		float zr = zenith != null ? zenith.x() : fog.x;
		float zg = zenith != null ? zenith.y() : fog.y;
		float zb = zenith != null ? zenith.z() : fog.z;
		WET.vec4(11, zr, zg, zb, m.reflections.get() ? 1f : 0f);
		WET.vec4(12, fog.x, fog.y, fog.z, 32f);

		// Glints from the sun by day, the moon by night; little of either under rain clouds.
		float sunY = (float) Math.cos(sky.sunAngle);
		float day = smoothstep(-0.05f, 0.1f, sunY);
		float clear = sky.rainBrightness * sky.rainBrightness;
		if (day > 0.02f) {
			float r = 1f;
			float g = 0.94f;
			float b = 0.84f;
			Vector4fc sunset = sky.sunriseAndSunsetColor;
			if (sunset != null && sunset.w() > 0f) {
				r = mix(r, sunset.x(), sunset.w());
				g = mix(g, sunset.y(), sunset.w());
				b = mix(b, sunset.z(), sunset.w());
			}
			WET.vec4(13, (float) -Math.sin(sky.sunAngle), sunY, 0f, day * clear);
			WET.vec4(14, r, g, b, 0f);
		} else {
			float moonY = (float) Math.cos(sky.moonAngle);
			WET.vec4(13, (float) -Math.sin(sky.moonAngle), moonY, 0f, 0.35f * smoothstep(0f, 0.2f, moonY) * clear);
			WET.vec4(14, 0.7f, 0.78f, 1f, 0f);
		}
		WET.vec4(15, 1f / Math.max(1, w), 1f / Math.max(1, h), m.puddles.getFloat() / 100f, 0f);
	}

	// ---- Rainbows ------------------------------------------------------------------------------

	private static boolean drawRainbow() {
		RainbowsModule m = Rainbow.module();
		float strength = Rainbow.strength();
		Matrix4f projection = frameProjection;
		CameraRenderState camera = frameCamera;
		SkyRenderState sky = frameSky;
		if (!m.isEnabled() || strength < 0.003f || !ShaderSupport.rainbow() || ShaderPacks.inUse() || projection == null || camera == null
				|| sky == null || camera.fogType != FogType.NONE || sky.skybox != DimensionType.Skybox.OVERWORLD) {
			return false;
		}
		// The bow is a 42° circle around the point opposite the light: a light much higher than 42°
		// puts all of it below the horizon.
		float sunY = (float) Math.cos(sky.sunAngle);
		float lx;
		float ly;
		float r;
		float g;
		float b;
		float saturation;
		if (sunY > -0.02f) {
			strength *= smoothstep(-0.02f, 0.06f, sunY) * (1f - smoothstep(0.58f, 0.74f, sunY));
			lx = (float) -Math.sin(sky.sunAngle);
			ly = sunY;
			r = 1f;
			g = 0.97f;
			b = 0.92f;
			Vector4fc sunset = sky.sunriseAndSunsetColor;
			if (sunset != null && sunset.w() > 0f) {
				// Sunset bows burn orange and red – the blue light is gone.
				r = mix(r, sunset.x() * 1.15f, sunset.w() * 0.9f);
				g = mix(g, sunset.y() * 0.9f, sunset.w() * 0.9f);
				b = mix(b, sunset.z() * 0.6f, sunset.w() * 0.9f);
			}
			saturation = 1f;
		} else if (m.moonbows.get()) {
			float moonY = (float) Math.cos(sky.moonAngle);
			strength *= 0.35f * smoothstep(0f, 0.08f, moonY) * (1f - smoothstep(0.58f, 0.74f, moonY));
			lx = (float) -Math.sin(sky.moonAngle);
			ly = moonY;
			r = 0.82f;
			g = 0.87f;
			b = 1f;
			saturation = 0.25f;
		} else {
			return false;
		}
		strength *= m.intensity.getFloat() / 100f * 0.55f * mix(0.45f, 1f, sky.rainBrightness);
		if (strength < 0.003f) {
			return false;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.POST_RAINBOW);
		if (color == null || depth == null || pipeline == null) {
			return false;
		}
		VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		RAYS_INVERSE.set(VIEW_PROJECTION).invert();
		RAINBOW.mat4(0, RAYS_INVERSE);
		RAINBOW.vec4(4, -lx, -ly, 0f, strength);
		RAINBOW.vec4(5, r, g, b, saturation);
		RAINBOW.vec4(6, m.doubleBow.get() ? 1f : 0f, m.bands.get() ? 1f : 0f, ((System.nanoTime() - START) * 1.0e-9f) % 3600f, 0f);
		DynamicGpuDataStorageMapped<FloatBlock> ring = rainbowRing;
		if (ring == null) {
			ring = rainbowRing = new DynamicGpuDataStorageMapped<>("Liquid rainbow UBO", RAINBOW.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(RAINBOW);
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(RAINBOW_LABEL, color, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidRainbow", slice);
			pass.setUniform("DepthSampler", depth, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			pass.draw(3, 1, 0, 0);
		}
		return true;
	}

	// ---- Motion Blur ---------------------------------------------------------------------------

	/** Blurs along each pixel's motion since last frame; remembers this frame's camera either way. */
	private static void drawMotionBlur() {
		MotionBlurModule m = motionBlur;
		if (m == null) {
			m = motionBlur = ModuleManager.get(MotionBlurModule.class);
		}
		Matrix4f projection = frameProjection;
		CameraRenderState camera = frameCamera;
		if (projection == null || camera == null) {
			return;
		}
		long now = System.nanoTime();
		float dt = motionNanos < 0L ? 0f : (now - motionNanos) * 1.0e-9f;
		motionNanos = now;
		MOTION_VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		double x = camera.pos.x;
		double y = camera.pos.y;
		double z = camera.pos.z;
		try {
			if (color == null || !m.isEnabled() || PhotoMode.active() || !ShaderSupport.motion() || ShaderPacks.inUse() || Minecraft.getInstance().isPaused()
					|| !previousValid || dt <= 0f || dt > 0.25f) {
				return;
			}
			int w = color.getWidth(0);
			int h = color.getHeight(0);
			float dx = (float) (x - previousX);
			float dy = (float) (y - previousY);
			float dz = (float) (z - previousZ);
			// Teleports, respawns, perspective flips: no blur across a cut.
			if (w != previousWidth || h != previousHeight || dx * dx + dy * dy + dz * dz > 16f
					|| Math.abs(Mth.wrapDegrees(camera.yRot - previousYaw)) > 60f || Math.abs(camera.xRot - previousPitch) > 60f) {
				return;
			}
			drawMotion(m, color, w, h, dx, dy, dz, dt);
		} catch (RuntimeException e) {
			warnOnce(e);
		} finally {
			PREVIOUS_VIEW_PROJECTION.set(MOTION_VIEW_PROJECTION);
			previousX = x;
			previousY = y;
			previousZ = z;
			previousYaw = camera.yRot;
			previousPitch = camera.xRot;
			if (color != null) {
				previousWidth = color.getWidth(0);
				previousHeight = color.getHeight(0);
			}
			previousValid = true;
		}
	}

	private static void drawMotion(MotionBlurModule m, GpuTexture color, int w, int h, float dx, float dy, float dz, float dt) {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.POST_MOTION);
		if (colorView == null || depth == null || pipeline == null) {
			return;
		}
		MOTION_INVERSE.set(MOTION_VIEW_PROJECTION).invert();
		MOTION.mat4(0, MOTION_INVERSE);
		MOTION.mat4(4, PREVIOUS_VIEW_PROJECTION);
		// A shutter set for 60 FPS: the same blur at any frame rate.
		float scale = Math.min(3f, m.strength.getFloat() / 100f * (1f / 60f) / dt);
		float movement = m.movement.get() ? 1f : 0f;
		MOTION.vec4(8, dx * movement, dy * movement, dz * movement, scale);
		int samples = switch (m.quality.index()) {
			case 0 -> 8;
			case 2 -> 24;
			default -> 14;
		};
		MOTION.vec4(9, samples, m.maxBlur.getFloat() / 100f, 1f / Math.max(1, w), 1f / Math.max(1, h));
		ensureScene(color, w, h);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.copyTextureToTexture(color, scene, 0, 0, 0, 0, 0, w, h);
		DynamicGpuDataStorageMapped<FloatBlock> ring = motionRing;
		if (ring == null) {
			ring = motionRing = new DynamicGpuDataStorageMapped<>("Liquid motion blur UBO", MOTION.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(MOTION);
		try (RenderPass pass = encoder.createRenderPass(MOTION_LABEL, colorView, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidMotion", slice);
			pass.setUniform("InSampler", sceneView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			pass.setUniform("DepthSampler", depth, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			pass.draw(3, 1, 0, 0);
		}
	}

	// ---- Sun Rays ------------------------------------------------------------------------------

	/** Draws the rays (and flare) for this frame; false when there's nothing to draw. */
	private static boolean drawSunRays() {
		SunRaysModule m = sunRays;
		if (m == null) {
			m = sunRays = ModuleManager.get(SunRaysModule.class);
		}
		Matrix4f projection = frameProjection;
		CameraRenderState camera = frameCamera;
		SkyRenderState sky = frameSky;
		if (!m.isEnabled() || !ShaderSupport.rays() || ShaderPacks.inUse() || projection == null || camera == null || sky == null
				|| camera.fogType != FogType.NONE || sky.skybox != DimensionType.Skybox.OVERWORLD) {
			return false;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture colorTexture = target.getColorTexture();
		GpuTextureView color = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline mask = compiled(LiquidPipelines.POST_RAYS_MASK);
		CompiledRenderPipeline fine = compiled(LiquidPipelines.POST_RAYS_BLUR_FINE);
		CompiledRenderPipeline coarse = compiled(LiquidPipelines.POST_RAYS_BLUR_COARSE);
		CompiledRenderPipeline composite = compiled(LiquidPipelines.POST_RAYS_COMPOSITE);
		if (colorTexture == null || color == null || depth == null || mask == null || fine == null || coarse == null || composite == null) {
			return false;
		}
		if (!writeRays(m, projection, camera, sky, colorTexture.getWidth(0), colorTexture.getHeight(0))) {
			return false;
		}
		raysIdleFrames = 0;
		ensureRayTextures(Math.max(1, colorTexture.getWidth(0) / 2), Math.max(1, colorTexture.getHeight(0) / 2));
		DynamicGpuDataStorageMapped<FloatBlock> ring = raysRing;
		if (ring == null) {
			ring = raysRing = new DynamicGpuDataStorageMapped<>("Liquid sun rays UBO", RAYS.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(RAYS);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		raysPass(encoder, RAYS_MASK_LABEL, mask, slice, color, depth, RAY_VIEWS[0], linear, nearest);
		raysPass(encoder, RAYS_BLUR_LABEL, fine, slice, RAY_VIEWS[0], null, RAY_VIEWS[1], linear, nearest);
		raysPass(encoder, RAYS_BLUR_LABEL, coarse, slice, RAY_VIEWS[1], null, RAY_VIEWS[0], linear, nearest);
		raysPass(encoder, RAYS_COMPOSITE_LABEL, composite, slice, RAY_VIEWS[0], depth, color, linear, nearest);
		return true;
	}

	private static void raysPass(CommandEncoder encoder, Supplier<String> label, CompiledRenderPipeline pipeline, GpuBufferSlice params,
			GpuTextureView source, @Nullable GpuTextureView depth, GpuTextureView destination, GpuSampler linear, GpuSampler nearest) {
		try (RenderPass pass = encoder.createRenderPass(label, destination, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidRays", params);
			pass.setUniform("InSampler", source, linear);
			if (depth != null) {
				pass.setUniform("DepthSampler", depth, nearest);
			}
			pass.draw(3, 1, 0, 0);
		}
	}

	/**
	 * The light (the sun by day, the moon by night), where it is on screen and how strong; false when
	 * it's too weak or behind the camera.
	 */
	private static boolean writeRays(SunRaysModule m, Matrix4f projection, CameraRenderState camera, SkyRenderState sky, int width, int height) {
		float sunX = (float) -Math.sin(sky.sunAngle);
		float sunY = (float) Math.cos(sky.sunAngle);
		float day = smoothstep(-0.04f, 0.12f, sunY);
		float clear = sky.rainBrightness * sky.rainBrightness;
		float dx;
		float dy;
		float r;
		float g;
		float b;
		float strength;
		float tightness;
		float flareGain;
		if (day > 0.02f) {
			dx = sunX;
			dy = sunY;
			switch (m.color.index()) {
				case SunRaysModule.COLOR_GOLDEN -> {
					r = 1f;
					g = 0.72f;
					b = 0.38f;
				}
				case SunRaysModule.COLOR_WARM -> {
					r = 1f;
					g = 0.84f;
					b = 0.62f;
				}
				case SunRaysModule.COLOR_NATURAL -> {
					r = 1f;
					g = 0.9f;
					b = 0.76f;
					Vector4fc sunset = sky.sunriseAndSunsetColor;
					if (sunset != null && sunset.w() > 0f) {
						r = mix(r, sunset.x(), sunset.w() * 0.8f);
						g = mix(g, sunset.y(), sunset.w() * 0.8f);
						b = mix(b, sunset.z(), sunset.w() * 0.8f);
					}
				}
				default -> {
					r = 0.78f;
					g = 0.88f;
					b = 1f;
				}
			}
			strength = day;
			tightness = 48f;
			flareGain = 1f;
		} else if (m.moonRays.get()) {
			dx = (float) -Math.sin(sky.moonAngle);
			dy = (float) Math.cos(sky.moonAngle);
			r = 0.55f;
			g = 0.66f;
			b = 0.95f;
			strength = (1f - day) * 0.3f * smoothstep(-0.05f, 0.25f, dy);
			tightness = 96f;
			flareGain = 0.3f;
		} else {
			return false;
		}
		strength *= clear * m.intensity.getFloat() / 100f;
		if (strength < 0.004f) {
			return false;
		}
		VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		LIGHT_CLIP.set(dx, dy, 0f, 0f).mul(VIEW_PROJECTION);
		float facing = LIGHT_CLIP.w;
		if (facing < 0.02f) {
			return false;
		}
		float lx = LIGHT_CLIP.x / facing * 0.5f + 0.5f;
		float ly = LIGHT_CLIP.y / facing * 0.5f + 0.5f;
		strength *= smoothstep(0.02f, 0.3f, facing);
		RAYS_INVERSE.set(VIEW_PROJECTION).invert();

		int flareMode = m.flare.index();
		float flare = flareMode == 0 ? 0f : (flareMode == 1 ? 0.6f : 1f) * m.flareStrength.getFloat() / 100f * flareGain * clear;
		// Only while the light is in view: off-screen it can't flare.
		float onScreen = smoothstep(-0.05f, 0.05f, Math.min(Math.min(lx, 1f - lx), Math.min(ly, 1f - ly)));
		int taps = switch (m.quality.index()) {
			case 0 -> 10;
			case 2 -> 20;
			default -> 14;
		};
		float gain = strength * 0.85f;
		RAYS.mat4(0, RAYS_INVERSE);
		RAYS.vec4(4, lx, ly, onScreen * Math.min(1f, strength * 2f), width / (float) Math.max(1, height));
		RAYS.vec4(5, dx, dy, 0f, tightness);
		RAYS.vec4(6, r * gain, g * gain, b * gain, flare);
		RAYS.vec4(7, m.length.getFloat() / 100f, (float) Math.pow(0.55, 1.0 / taps), taps, flareMode);
		RAYS.vec4(8, ((System.nanoTime() - START) * 1.0e-9f) % 1000f, 0f, 0f, 0f);
		return true;
	}

	private static void ensureRayTextures(int w, int h) {
		if (RAY_TEXTURES[0] != null && !RAY_TEXTURES[0].isClosed() && rayWidth == w && rayHeight == h) {
			return;
		}
		releaseRayTextures();
		GpuDevice device = RenderSystem.getDevice();
		for (int i = 0; i < 2; i++) {
			RAY_TEXTURES[i] = device.createTexture(() -> "Liquid sun rays", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
					GpuFormat.RGBA16_FLOAT, w, h, 1, 1);
			RAY_VIEWS[i] = device.createTextureView(RAY_TEXTURES[i]);
		}
		rayWidth = w;
		rayHeight = h;
	}

	private static void releaseRayTextures() {
		for (int i = 0; i < 2; i++) {
			if (RAY_VIEWS[i] != null) {
				RAY_VIEWS[i].close();
				RAY_VIEWS[i] = null;
			}
			if (RAY_TEXTURES[i] != null) {
				RAY_TEXTURES[i].close();
				RAY_TEXTURES[i] = null;
			}
		}
		raysIdleFrames = 0;
	}

	// ---- Wet Lens ------------------------------------------------------------------------------

	private static void drawLens(CommandEncoder encoder, GpuTexture color, GpuTextureView colorView, int w, int h, GpuSampler linear) {
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.POST_LENS);
		if (pipeline == null) {
			return;
		}
		encoder.copyTextureToTexture(color, scene, 0, 0, 0, 0, 0, w, h);
		WetLens.write(LENS, w, h);
		DynamicGpuDataStorageMapped<FloatBlock> ring = lensRing;
		if (ring == null) {
			ring = lensRing = new DynamicGpuDataStorageMapped<>("Liquid wet lens UBO", LENS.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(LENS);
		try (RenderPass pass = encoder.createRenderPass(LENS_LABEL, colorView, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidLens", slice);
			pass.setUniform("InSampler", sceneView, linear);
			pass.draw(3, 1, 0, 0);
		}
	}

	// ---- Bloom + Color Grading -----------------------------------------------------------------

	/** After the hand and outlines, before vanilla's post effects and the GUI (render thread). */
	public static void beforePostEffects() {
		ShaderSupport.ensureValidated();
		ColorGradingModule g = grading;
		BloomModule b = bloom;
		if (g == null || b == null) {
			g = grading = ModuleManager.get(ColorGradingModule.class);
			b = bloom = ModuleManager.get(BloomModule.class);
		}
		boolean grade = (g.isEnabled() || PhotoMode.active()) && ShaderSupport.grade();
		boolean glow = b.isEnabled() && ShaderSupport.bloom();
		boolean lens = WetLens.advance(Minecraft.getInstance().isPaused()) && ShaderSupport.lens();
		if (!grade && !glow && !lens) {
			if (scene != null && ++idleFrames > IDLE_FRAMES) {
				releaseTextures();
			}
			return;
		}
		idleFrames = 0;
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		if (color == null || colorView == null || color.isClosed()) {
			return;
		}
		try {
			int w = color.getWidth(0);
			int h = color.getHeight(0);
			ensureScene(color, w, h);
			CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
			GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
			if (lens) {
				drawLens(encoder, color, colorView, w, h, linear);
			}
			CompiledRenderPipeline gradePipeline = compiled(LiquidPipelines.POST_GRADE);
			if (!grade && !glow || gradePipeline == null) {
				return;
			}
			encoder.copyTextureToTexture(color, scene, 0, 0, 0, 0, 0, w, h);

			int levels = glow ? bloom(encoder, b, w, h, linear) : 0;

			writeGrade(grade && g.isEnabled() ? g : null, glow ? b : null, levels);
			DynamicGpuDataStorageMapped<FloatBlock> ring = gradeRing;
			if (ring == null) {
				ring = gradeRing = new DynamicGpuDataStorageMapped<>("Liquid grade UBO", GRADE.size(), GpuBuffer.USAGE_UNIFORM, 1);
			}
			ring.endFrame();
			GpuBufferSlice slice = ring.writeData(GRADE);
			try (RenderPass pass = encoder.createRenderPass(GRADE_LABEL, colorView, Optional.empty())) {
				pass.setPipeline(gradePipeline);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("LiquidGrade", slice);
				pass.setUniform("InSampler", sceneView, linear);
				pass.setUniform("BloomSampler", levels > 0 ? MIP_VIEWS[0] : sceneView, linear);
				pass.draw(3, 1, 0, 0);
			}
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** Builds the bloom chain into {@link #MIPS}[0]; returns the number of levels used (0 if it couldn't run). */
	private static int bloom(CommandEncoder encoder, BloomModule b, int w, int h, GpuSampler linear) {
		CompiledRenderPipeline prefilter = compiled(LiquidPipelines.POST_BLOOM_PREFILTER);
		CompiledRenderPipeline down = compiled(LiquidPipelines.POST_BLOOM_DOWNSAMPLE);
		CompiledRenderPipeline up = compiled(LiquidPipelines.POST_BLOOM_UPSAMPLE);
		if (prefilter == null || down == null || up == null) {
			return 0;
		}
		ensureMips(w, h);
		float radius = b.radius.getFloat() / 100f;
		int levels = 3 + Math.round(radius * 3f);
		while (levels > 1 && (MIPS[levels - 1].getWidth(0) < 2 || MIPS[levels - 1].getHeight(0) < 2)) {
			levels--;
		}
		BLOOM.vec4(0, b.threshold.getFloat() / 100f, b.softness.getFloat() / 100f, 1f + radius,
				b.anamorphic.get() ? 3f : 1f);
		DynamicGpuDataStorageMapped<FloatBlock> ring = bloomRing;
		if (ring == null) {
			ring = bloomRing = new DynamicGpuDataStorageMapped<>("Liquid bloom UBO", BLOOM.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		ring.endFrame();
		GpuBufferSlice slice = ring.writeData(BLOOM);

		pass(encoder, PREFILTER_LABEL, prefilter, slice, sceneView, MIP_VIEWS[0], linear);
		for (int i = 1; i < levels; i++) {
			pass(encoder, DOWN_LABEL, down, slice, MIP_VIEWS[i - 1], MIP_VIEWS[i], linear);
		}
		for (int i = levels - 1; i > 0; i--) {
			pass(encoder, UP_LABEL, up, slice, MIP_VIEWS[i], MIP_VIEWS[i - 1], linear);
		}
		return levels;
	}

	private static void pass(CommandEncoder encoder, Supplier<String> label, CompiledRenderPipeline pipeline, GpuBufferSlice params,
			@Nullable GpuTextureView source, GpuTextureView destination, GpuSampler sampler) {
		try (RenderPass pass = encoder.createRenderPass(label, destination, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidBloom", params);
			pass.setUniform("InSampler", source, sampler);
			pass.draw(3, 1, 0, 0);
		}
	}

	/**
	 * The grade: Color Grading's, with Photo Mode's camera on top while it's open – its filter (when
	 * one is chosen) replaces the look, exposure adds in stops, contrast and saturation multiply,
	 * warmth adds to the temperature, vignette and grain take the stronger of the two.
	 */
	private static void writeGrade(@Nullable ColorGradingModule g, @Nullable BloomModule b, int levels) {
		float bloomIntensity = b != null && levels > 0 ? b.intensity.getFloat() / 100f * 1.6f / levels : 0f;
		// Film grain holds still while a photo exposes, or averaging its frames would wash it out.
		float seed = PhotoCapture.holdsCamera() ? 17.31f : ((System.nanoTime() - START) * 1.0e-9f) % 1000f;
		PhotoModeModule p = PhotoMode.active() ? PhotoMode.module() : null;
		if (g == null && p == null) {
			// Bloom alone: an identity grade that only adds the glow.
			GRADE.vec4(0, 0f, 0f, 0f, 0f);
			GRADE.vec4(1, 1f, 1f, 1f, 1f);
			GRADE.vec4(2, 1f, 1f, 1f, 1f);
			GRADE.vec4(3, 0.5f, 0.5f, 0.5f, 0f);
			GRADE.vec4(4, 0.5f, 0.5f, 0.5f, 0f);
			GRADE.vec4(5, 1f, 1f, 1f, 0f);
			GRADE.vec4(6, 1f, 1f, 1f, 1f);
			GRADE.vec4(7, 0f, 0f, bloomIntensity, seed);
			GRADE.vec4(8, 0f, 0f, 0f, 0f);
			return;
		}
		boolean photoLook = p != null && p.filter.index() != 0;
		int lookIndex = photoLook ? p.filter.index() : g != null ? g.look.index() : 0;
		float[] look = GradingLook.get(lookIndex);
		float strength = lookIndex == 0 ? 0f : (photoLook ? p.filterStrength.getFloat() : g.lookStrength.getFloat()) / 100f;
		GRADE.vec4(0, look[0], look[1], look[2], strength);
		GRADE.vec4(1, look[3], look[4], look[5], look[10]);
		GRADE.vec4(2, look[6], look[7], look[8], look[9]);
		GRADE.vec4(3, look[12], look[13], look[14], look[18]);
		GRADE.vec4(4, look[15], look[16], look[17], look[11]);
		double exposure = (g != null ? g.exposure.get() / 100.0 : 0.0) + (p != null ? p.exposure.get() : 0.0);
		float contrast = (g != null ? g.contrast.getFloat() / 100f : 1f) * (p != null ? p.contrast.getFloat() / 100f : 1f);
		float saturation = (g != null ? g.saturation.getFloat() / 100f : 1f) * (p != null ? p.saturation.getFloat() / 100f : 1f);
		GRADE.vec4(5, (float) Math.pow(2.0, exposure), contrast, saturation, g != null ? g.vibrance.getFloat() / 100f : 0f);
		// White balance: temperature along blue–amber, tint along green–magenta, brightness kept.
		float t = Math.clamp((g != null ? g.temperature.getFloat() : 0f) + (p != null ? p.warmth.getFloat() : 0f), -100f, 100f) / 100f;
		float m = g != null ? g.tint.getFloat() / 100f : 0f;
		float wr = 1f + 0.1f * t + 0.03f * m;
		float wg = 1f - 0.1f * m;
		float wb = 1f - 0.14f * t + 0.03f * m;
		float norm = 1f / (0.2126f * wr + 0.7152f * wg + 0.0722f * wb);
		// Without Color Grading the tone curve stays as it would be without the camera.
		int toneCurve = g != null ? g.toneCurve.index() : bloomIntensity > 0f ? 1 : 0;
		GRADE.vec4(6, wr * norm, wg * norm, wb * norm, toneCurve);
		GRADE.vec4(7, g != null ? g.shadows.getFloat() / 100f : 0f, g != null ? g.highlights.getFloat() / 100f : 0f, bloomIntensity, seed);
		float vignette = Math.max(g != null ? g.vignette.getFloat() : 0f, p != null ? p.vignette.getFloat() : 0f) / 100f;
		float grain = Math.max(g != null ? g.grain.getFloat() : 0f, p != null ? p.grain.getFloat() : 0f) / 100f;
		GRADE.vec4(8, vignette, grain, g != null ? g.aberration.getFloat() / 100f : 0f, g != null ? g.sharpen.getFloat() / 100f : 0f);
	}

	// ---- Resources -----------------------------------------------------------------------------

	private static void ensureScene(GpuTexture color, int w, int h) {
		idleFrames = 0;
		GpuTexture current = scene;
		if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h
				&& current.getFormat() == color.getFormat()) {
			return;
		}
		releaseTextures();
		GpuDevice device = RenderSystem.getDevice();
		scene = device.createTexture(() -> "Liquid post frame", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
				color.getFormat(), w, h, 1, 1);
		sceneView = device.createTextureView(scene);
	}

	private static void ensureOpaqueDepth(GpuTexture depth) {
		GpuTexture current = opaqueDepth;
		int w = depth.getWidth(0);
		int h = depth.getHeight(0);
		if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h
				&& current.getFormat() == depth.getFormat()) {
			return;
		}
		releaseOpaqueDepth();
		GpuDevice device = RenderSystem.getDevice();
		opaqueDepth = device.createTexture(() -> "Liquid opaque depth", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
				depth.getFormat(), w, h, 1, 1);
		opaqueDepthView = device.createTextureView(opaqueDepth);
	}

	private static void releaseOpaqueDepth() {
		if (opaqueDepthView != null) {
			opaqueDepthView.close();
			opaqueDepthView = null;
		}
		if (opaqueDepth != null) {
			opaqueDepth.close();
			opaqueDepth = null;
		}
	}

	private static void ensureMips(int w, int h) {
		if (MIPS[0] != null && !MIPS[0].isClosed() && mipWidth == w && mipHeight == h) {
			return;
		}
		releaseMips();
		GpuDevice device = RenderSystem.getDevice();
		for (int i = 0; i < MAX_LEVELS; i++) {
			int level = i + 1;
			MIPS[i] = device.createTexture(() -> "Liquid bloom 1/" + (1 << level),
					GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA16_FLOAT,
					Math.max(1, w >> level), Math.max(1, h >> level), 1, 1);
			MIP_VIEWS[i] = device.createTextureView(MIPS[i]);
		}
		mipWidth = w;
		mipHeight = h;
	}

	private static @Nullable CompiledRenderPipeline compiled(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
		return compiled == null || compiled.isClosed() ? null : compiled;
	}

	private static void warnOnce(RuntimeException e) {
		if (!warned) {
			warned = true;
			LiquidClient.LOGGER.error("Post-processing pass failed; skipping it this frame", e);
		}
	}

	private static void releaseMips() {
		for (int i = 0; i < MAX_LEVELS; i++) {
			if (MIP_VIEWS[i] != null) {
				MIP_VIEWS[i].close();
				MIP_VIEWS[i] = null;
			}
			if (MIPS[i] != null) {
				MIPS[i].close();
				MIPS[i] = null;
			}
		}
	}

	private static void releaseTextures() {
		releaseMips();
		if (sceneView != null) {
			sceneView.close();
			sceneView = null;
		}
		if (scene != null) {
			scene.close();
			scene = null;
		}
		idleFrames = 0;
	}

	/** Frees every GPU resource (render thread, between frames). Idempotent. */
	public static void release() {
		releaseTextures();
		releaseRayTextures();
		releaseAoTextures();
		releaseDofTextures();
		releaseOpaqueDepth();
		hazeSlice = null;
		if (hazeRing != null) {
			hazeRing.close();
			hazeRing = null;
		}
		if (bloomRing != null) {
			bloomRing.close();
			bloomRing = null;
		}
		if (gradeRing != null) {
			gradeRing.close();
			gradeRing = null;
		}
		if (raysRing != null) {
			raysRing.close();
			raysRing = null;
		}
		if (lensRing != null) {
			lensRing.close();
			lensRing = null;
		}
		if (wetRing != null) {
			wetRing.close();
			wetRing = null;
		}
		if (rainbowRing != null) {
			rainbowRing.close();
			rainbowRing = null;
		}
		if (motionRing != null) {
			motionRing.close();
			motionRing = null;
		}
		if (aoRing != null) {
			aoRing.close();
			aoRing = null;
		}
		if (dofRing != null) {
			dofRing.close();
			dofRing = null;
		}
		previousValid = false;
	}

	private static float mix(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
