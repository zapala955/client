package net.client.visuals.render.water;

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
import net.client.visuals.module.render.LiquidWaterModule;
import net.client.visuals.module.render.ParticlesModule;
import net.client.visuals.render.post.FloatBlock;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.client.visuals.render.storm.Storm;
import net.client.visuals.render.weather.LiquidWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Liquid Water ({@link LiquidWaterModule}): shades the water the terrain shaders tagged
 * ({@link WaterTag}, {@link WaterShaderPatch}) – render thread, from {@code PostFx}.
 *
 * <h2>Where in the frame (classic transparency)</h2>
 * <ol>
 *   <li>Start of the translucent step ({@link #captureBed}): the world as it is before any water –
 *       a copy of the colour and of the depth. That's what's under (and around) every surface.</li>
 *   <li>Translucent terrain draws: water as its lit tint, with the tag in its depth.</li>
 *   <li>Right after it, before particles, clouds and weather ({@link #draw}): a copy of the colour
 *       (the tint), then {@code liquid_water.fsh} – half-resolution reflections (screen-space, or the
 *       sky), light shafts when the camera is under water, and the full-resolution composite over the
 *       water (and, under water, over everything).</li>
 * </ol>
 * The wave field is generated once on the GPU. GPU resources are created on first use, recreated on
 * resize and released after a few seconds without water, on world exit and when the client stops –
 * always on the render thread between frames. Each pass is skipped when its pipeline didn't compile
 * ({@link ShaderSupport#water}), and then tagging never starts. Nothing here allocates per frame.
 */
public final class LiquidWater {
	private static final Supplier<String> NOISE_LABEL = () -> "Liquid water waves";
	private static final Supplier<String> REFLECT_LABEL = () -> "Liquid water reflections";
	private static final Supplier<String> SHAFTS_LABEL = () -> "Liquid water shafts";
	private static final Supplier<String> COMPOSITE_LABEL = () -> "Liquid water";
	/** Frames without water before the textures are freed (~5 s at 60 FPS). */
	private static final int IDLE_FRAMES = 300;
	private static final int NOISE_SIZE = 256;
	/** Absorption per block (rgb) and scattering, and the scattering colour, per style: natural, tropical, deep ocean. */
	private static final float[][] ABSORB = {{0.30f, 0.10f, 0.055f, 0.85f}, {0.24f, 0.06f, 0.045f, 1.0f}, {0.42f, 0.16f, 0.07f, 0.6f}};
	private static final float[][] SCATTER = {{0.07f, 0.26f, 0.5f}, {0.1f, 0.55f, 0.62f}, {0.03f, 0.1f, 0.3f}};
	private static final int[] STEPS = {14, 22, 32};

	private static final FloatBlock DATA = new FloatBlock(24);
	private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f INVERSE = new Matrix4f();
	private static final WaterField FIELD = new WaterField();
	private static final BlockPos.MutableBlockPos SCAN = new BlockPos.MutableBlockPos();

	private static @Nullable LiquidWaterModule module;
	private static @Nullable GpuTexture bedDepth;
	private static @Nullable GpuTextureView bedDepthView;
	private static @Nullable GpuTexture behind;
	private static @Nullable GpuTextureView behindView;
	private static @Nullable GpuTexture gbuffer;
	private static @Nullable GpuTextureView gbufferView;
	private static @Nullable GpuTexture reflect;
	private static @Nullable GpuTextureView reflectView;
	private static @Nullable GpuTexture shafts;
	private static @Nullable GpuTextureView shaftsView;
	private static @Nullable GpuTexture noise;
	private static @Nullable GpuTextureView noiseView;
	private static boolean noiseReady;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> ring;
	private static boolean captured;
	private static int idleFrames;
	private static long lastNanos = -1L;
	private static float time;
	/** The water surface above the camera while it's under water (absolute y), else NaN. Client tick. */
	private static double surfaceY = Double.NaN;
	private static boolean warned;

	private LiquidWater() {
	}

	public static LiquidWaterModule module() {
		LiquidWaterModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(LiquidWaterModule.class);
		}
		return m;
	}

	// ---- Client tick --------------------------------------------------------------------------

	/** Client tick: whether to tag water (and re-mesh on Sodium when that changes), the water field, the surface overhead. */
	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		boolean wanted = level != null && module().isEnabled() && ShaderSupport.water() && !ShaderPacks.inUse()
				&& level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD;
		WaterTag.tick(minecraft, wanted);
		if (level == null || !WaterTag.active()) {
			surfaceY = Double.NaN;
			return;
		}
		Vec3 camera = minecraft.gameRenderer.mainCamera().position();
		FIELD.update(level, Mth.floor(camera.x), Mth.floor(camera.z));
		surfaceY = surfaceAbove(level, camera);
	}

	/** The top of the water the camera is in (scanning up to 64 blocks), or NaN when it isn't in water. */
	private static double surfaceAbove(ClientLevel level, Vec3 camera) {
		int x = Mth.floor(camera.x);
		int y = Mth.floor(camera.y);
		int z = Mth.floor(camera.z);
		FluidState fluid = level.getFluidState(SCAN.set(x, y, z));
		if (!fluid.is(FluidTags.WATER)) {
			return Double.NaN;
		}
		double top = y + fluid.getHeight(level, SCAN);
		for (int up = y + 1; up < y + 64; up++) {
			fluid = level.getFluidState(SCAN.set(x, up, z));
			if (!fluid.is(FluidTags.WATER)) {
				break;
			}
			top = up + fluid.getHeight(level, SCAN);
		}
		return top;
	}

	/** Rain rings need Liquid Weather's rain columns even when Liquid Weather itself is off. */
	public static boolean wantsColumns() {
		return WaterTag.active() && module().rainRipples.get();
	}

	// ---- Frame (render thread) ----------------------------------------------------------------

	/** Whether this frame's water is tagged and gets shaded (the copies must be taken). */
	public static boolean shading() {
		return WaterTag.active() && ShaderSupport.water() && !ShaderPacks.inUse() && Minecraft.getInstance().level != null;
	}

	/** Start of the translucent step, no render pass open: the world before water. */
	public static void captureBed() {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTexture depth = target.getDepthTexture();
		if (color == null || depth == null || color.isClosed() || depth.isClosed()) {
			return;
		}
		try {
			int w = color.getWidth(0);
			int h = color.getHeight(0);
			ensureTextures(color, depth, w, h);
			CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
			encoder.copyTextureToTexture(color, behind, 0, 0, 0, 0, 0, w, h);
			encoder.copyTextureToTexture(depth, bedDepth, 0, 0, 0, 0, 0, w, h);
			captured = true;
			idleFrames = 0;
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** Right after translucent terrain, no render pass open: shades the tagged water. */
	public static void draw(Matrix4f projection, CameraRenderState camera, SkyRenderState sky) {
		if (!captured) {
			return;
		}
		captured = false;
		try {
			drawPasses(projection, camera, sky);
		} catch (RuntimeException e) {
			warnOnce(e);
		}
	}

	/** End of a frame without water: counts towards releasing the textures. */
	public static void idle() {
		captured = false;
		if ((bedDepth != null || noise != null) && ++idleFrames > IDLE_FRAMES) {
			releaseTextures();
		}
	}

	private static void drawPasses(Matrix4f projection, CameraRenderState camera, SkyRenderState sky) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depth = target.getDepthTextureView();
		CompiledRenderPipeline noisePass = compiled(LiquidPipelines.WATER_NOISE);
		CompiledRenderPipeline reflectPass = compiled(LiquidPipelines.WATER_REFLECT);
		CompiledRenderPipeline shaftsPass = compiled(LiquidPipelines.WATER_SHAFTS);
		CompiledRenderPipeline compositePass = compiled(LiquidPipelines.WATER_COMPOSITE);
		if (level == null || color == null || colorView == null || depth == null || color.isClosed() || noisePass == null || reflectPass == null
				|| shaftsPass == null || compositePass == null || bedDepthView == null || behindView == null || gbuffer == null
				|| gbufferView == null || reflectView == null || shaftsView == null || noise == null || noiseView == null) {
			return;
		}
		LiquidWaterModule m = module();
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		GpuSampler repeat = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);

		if (!noiseReady) {
			try (RenderPass pass = encoder.createRenderPass(NOISE_LABEL, noiseView, Optional.empty())) {
				pass.setPipeline(noisePass);
				RenderSystem.bindDefaultUniforms(pass);
				pass.draw(3, 1, 0, 0);
			}
			noiseReady = true;
		}

		// The colour now holds water's tint where it's tagged: the composite reads it from a copy.
		encoder.copyTextureToTexture(color, gbuffer, 0, 0, 0, 0, 0, w, h);
		GpuTextureView field = FIELD.ready(level) ? FIELD.upload() : null;
		GpuTextureView columns = m.rainRipples.get() ? LiquidWeather.columns(level) : null;
		boolean under = camera.fogType == FogType.WATER && !Double.isNaN(surfaceY);
		boolean shaftsOn = under && m.underwater.get();
		GpuBufferSlice slice = write(m, level, projection, camera, sky, w, h, field != null, columns != null, under, shaftsOn);

		try (RenderPass pass = encoder.createRenderPass(REFLECT_LABEL, reflectView, Optional.empty())) {
			pass.setPipeline(reflectPass);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidWater", slice);
			pass.setUniform("DepthSampler", depth, nearest);
			pass.setUniform("BedSampler", bedDepthView, nearest);
			pass.setUniform("BehindSampler", behindView, linear);
			pass.setUniform("WaveSampler", noiseView, repeat);
			pass.setUniform("FieldSampler", field != null ? field : noiseView, nearest);
			pass.draw(3, 1, 0, 0);
		}
		if (shaftsOn) {
			try (RenderPass pass = encoder.createRenderPass(SHAFTS_LABEL, shaftsView, Optional.empty())) {
				pass.setPipeline(shaftsPass);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("LiquidWater", slice);
				pass.setUniform("DepthSampler", depth, nearest);
				pass.setUniform("BedSampler", bedDepthView, nearest);
				pass.setUniform("BehindSampler", behindView, linear);
				pass.setUniform("WaveSampler", noiseView, repeat);
				pass.draw(3, 1, 0, 0);
			}
		}
		try (RenderPass pass = encoder.createRenderPass(COMPOSITE_LABEL, colorView, Optional.empty())) {
			pass.setPipeline(compositePass);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidWater", slice);
			pass.setUniform("DepthSampler", depth, nearest);
			pass.setUniform("BedSampler", bedDepthView, nearest);
			pass.setUniform("BehindSampler", behindView, linear);
			pass.setUniform("WaveSampler", noiseView, repeat);
			pass.setUniform("FieldSampler", field != null ? field : noiseView, nearest);
			pass.setUniform("SceneSampler", gbufferView, nearest);
			pass.setUniform("ReflectSampler", reflectView, nearest);
			// Without shafts this frame the shader never reads them (Under.w = 0): any texture will do.
			pass.setUniform("ShaftSampler", shaftsOn ? shaftsView : reflectView, linear);
			pass.setUniform("ColumnSampler", columns != null ? columns : noiseView, nearest);
			pass.draw(3, 1, 0, 0);
		}
	}

	private static GpuBufferSlice write(LiquidWaterModule m, ClientLevel level, Matrix4f projection, CameraRenderState camera, SkyRenderState sky,
			int w, int h, boolean field, boolean columns, boolean under, boolean shaftsOn) {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		// Waves stand still while the game does (pause, Photo Mode's freeze).
		if (!Minecraft.getInstance().isPaused()) {
			time = (time + dt) % 3600f;
		}

		VIEW_PROJECTION.set(projection).mul(camera.viewRotationMatrix);
		INVERSE.set(VIEW_PROJECTION).invert();
		DATA.mat4(0, INVERSE);
		DATA.mat4(4, VIEW_PROJECTION);
		double x = camera.pos.x;
		double y = camera.pos.y;
		double z = camera.pos.z;
		int bx = Mth.floor(x);
		int by = Mth.floor(y);
		int bz = Mth.floor(z);
		DATA.vec4(8, Math.floorMod(bx, 4096), by, Math.floorMod(bz, 4096), time);
		DATA.vec4(9, (float) (x - bx), (float) (y - by), (float) (z - bz), columns ? 1f : 0f);

		// The sun by day, the moon by night; little of either under rain clouds.
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
			DATA.vec4(10, (float) -Math.sin(sky.sunAngle), sunY, 0f, day * clear);
			DATA.vec4(11, r, g, b, day * clear);
		} else {
			float moonY = (float) Math.cos(sky.moonAngle);
			DATA.vec4(10, (float) -Math.sin(sky.moonAngle), moonY, 0f, 0.4f * smoothstep(0f, 0.2f, moonY) * clear);
			DATA.vec4(11, 0.7f, 0.78f, 1f, 0f);
		}

		// The sky to reflect: vanilla's colour overhead, the fog colour (Skybox modes, the storm) at the horizon.
		Vector4f fog = camera.fogData.color;
		Vector3fc zenith = sky.skyColor;
		float zr = zenith != null ? zenith.x() : fog.x;
		float zg = zenith != null ? zenith.y() : fog.y;
		float zb = zenith != null ? zenith.z() : fog.z;
		float storm = Storm.influence();
		if (storm > 0f) {
			zr = mix(zr, fog.x * 0.7f, storm);
			zg = mix(zg, fog.y * 0.7f, storm);
			zb = mix(zb, fog.z * 0.7f, storm);
		}
		int quality = Math.clamp(m.quality.index(), 0, 2);
		DATA.vec4(12, zr, zg, zb, m.reflections.index());
		DATA.vec4(13, fog.x, fog.y, fog.z, STEPS[quality]);
		DATA.vec4(14, fog.x, fog.y, fog.z, fog.w);
		DATA.vec4(15, camera.fogData.environmentalStart, camera.fogData.environmentalEnd, camera.fogData.renderDistanceStart,
				camera.fogData.renderDistanceEnd);

		// Wind: a breeze, more with rain and thunder, Liquid Weather's and the storm's when they blow.
		float rain = level.getRainLevel(1f);
		float thunder = level.getThunderLevel(1f);
		float wind = 0.3f + 0.45f * rain + 0.5f * thunder;
		double bearing = Math.toRadians(45.0);
		ParticlesModule particles = ParticlesModule.instance();
		if (particles != null) {
			bearing = Math.toRadians(particles.windDirection.get());
		}
		// Clockwise from north (-z).
		float dirX = (float) Math.sin(bearing);
		float dirZ = (float) -Math.cos(bearing);
		float weather = LiquidWeather.windStrength();
		if (weather > 0f) {
			wind = Math.max(wind, weather);
			dirX = LiquidWeather.windX();
			dirZ = LiquidWeather.windZ();
		}
		float stormWind = Storm.wind();
		if (stormWind > 0.05f) {
			wind = Math.max(wind, stormWind);
			float angle = Storm.windAngle();
			float k = Math.min(1f, stormWind * 2f);
			dirX = mix(dirX, Mth.cos(angle), k);
			dirZ = mix(dirZ, Mth.sin(angle), k);
		}
		float len = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
		if (len < 1.0e-4f) {
			dirX = 1f;
			dirZ = 0f;
			len = 1f;
		}
		DATA.vec4(16, dirX / len, dirZ / len, Math.min(2f, wind), m.waves.getFloat() / 100f);
		DATA.vec4(17, m.clarity.getFloat() / 100f, m.refraction.getFloat() / 100f, m.foam.get() ? 1f : 0f, m.caustics.get() ? 1f : 0f);
		int style = Math.clamp(m.style.index(), 0, 2);
		float[] absorb = ABSORB[style];
		float[] scatter = SCATTER[style];
		DATA.vec4(18, absorb[0], absorb[1], absorb[2], absorb[3]);
		DATA.vec4(19, scatter[0], scatter[1], scatter[2], style);
		// Whitecaps with a storm's wind or a thunderstorm; rain on water beyond the columns follows the rain.
		float caps = Math.max(Math.min(1f, Math.max(0f, stormWind - 0.6f)), thunder * 0.6f);
		DATA.vec4(20, rain, caps, m.rainRipples.get() ? 1f : 0f, rain);
		float surface = under ? (float) (surfaceY - y) : 0f;
		DATA.vec4(21, under ? 1f : 0f, surface, m.underwater.get() ? 1f : 0f, shaftsOn ? day * clear : 0f);
		DATA.vec4(22, w, h, Math.max(1, (w + 1) / 2), Math.max(1, (h + 1) / 2));
		float pixelAngle = 2f / (Math.abs(projection.m11()) * Math.max(1, h));
		DATA.vec4(23, quality, pixelAngle, field ? 1f : 0f, 0f);

		DynamicGpuDataStorageMapped<FloatBlock> r = ring;
		if (r == null) {
			r = ring = new DynamicGpuDataStorageMapped<>("Liquid water UBO", DATA.size(), GpuBuffer.USAGE_UNIFORM, 1);
		}
		r.endFrame();
		return r.writeData(DATA);
	}

	// ---- Resources ------------------------------------------------------------------------------

	private static void ensureTextures(GpuTexture color, GpuTexture depth, int w, int h) {
		GpuDevice device = RenderSystem.getDevice();
		if (noise == null || noise.isClosed()) {
			noise = device.createTexture("Liquid water waves", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
					GpuFormat.RGBA16_FLOAT, NOISE_SIZE, NOISE_SIZE, 1, 1);
			noiseView = device.createTextureView(noise);
			noiseReady = false;
		}
		GpuTexture current = bedDepth;
		if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h
				&& current.getFormat() == depth.getFormat() && behind != null && behind.getFormat() == color.getFormat()) {
			return;
		}
		releaseFrameTextures();
		int copy = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING;
		int target = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING;
		int hw = Math.max(1, (w + 1) / 2);
		int hh = Math.max(1, (h + 1) / 2);
		bedDepth = device.createTexture("Liquid water bed depth", copy, depth.getFormat(), w, h, 1, 1);
		bedDepthView = device.createTextureView(bedDepth);
		behind = device.createTexture("Liquid water behind", copy, color.getFormat(), w, h, 1, 1);
		behindView = device.createTextureView(behind);
		gbuffer = device.createTexture("Liquid water tint", copy, color.getFormat(), w, h, 1, 1);
		gbufferView = device.createTextureView(gbuffer);
		reflect = device.createTexture("Liquid water reflections", target, GpuFormat.RGBA16_FLOAT, hw, hh, 1, 1);
		reflectView = device.createTextureView(reflect);
		shafts = device.createTexture("Liquid water shafts", target, GpuFormat.RGBA16_FLOAT, hw, hh, 1, 1);
		shaftsView = device.createTextureView(shafts);
	}

	private static void releaseFrameTextures() {
		bedDepthView = close(bedDepthView);
		bedDepth = close(bedDepth);
		behindView = close(behindView);
		behind = close(behind);
		gbufferView = close(gbufferView);
		gbuffer = close(gbuffer);
		reflectView = close(reflectView);
		reflect = close(reflect);
		shaftsView = close(shaftsView);
		shafts = close(shafts);
	}

	private static void releaseTextures() {
		releaseFrameTextures();
		noiseView = close(noiseView);
		noise = close(noise);
		noiseReady = false;
		FIELD.release();
		idleFrames = 0;
	}

	/** Frees every GPU resource (world exit, client stop). Render thread, between frames. Idempotent. */
	public static void release() {
		releaseTextures();
		if (ring != null) {
			ring.close();
			ring = null;
		}
		captured = false;
	}

	/** Forgets the world (disconnect). */
	public static void reset() {
		FIELD.reset();
		surfaceY = Double.NaN;
		WaterTag.reset();
	}

	private static @Nullable GpuTexture close(@Nullable GpuTexture texture) {
		if (texture != null) {
			texture.close();
		}
		return null;
	}

	private static @Nullable GpuTextureView close(@Nullable GpuTextureView view) {
		if (view != null) {
			view.close();
		}
		return null;
	}

	private static @Nullable CompiledRenderPipeline compiled(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
		return compiled != null && !compiled.isClosed() ? compiled : null;
	}

	private static void warnOnce(Exception e) {
		if (!warned) {
			warned = true;
			LiquidClient.LOGGER.warn("Liquid Water pass failed", e);
		}
	}

	private static float mix(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
