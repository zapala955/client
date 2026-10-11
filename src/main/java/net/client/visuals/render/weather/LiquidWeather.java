package net.client.visuals.render.weather;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.client.visuals.LiquidClient;
import net.client.visuals.module.render.AmbienceModule;
import net.client.visuals.render.post.FloatBlock;
import net.client.visuals.render.post.WetWorld;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.client.visuals.render.storm.Storm;
import net.client.visuals.render.water.LiquidWater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Ambience's Liquid Weather: rain, snow, splashes and ripples in place of vanilla's weather sheets.
 *
 * <h2>Zero per-particle CPU work</h2>
 * One static vertex buffer holds every drop, flake and splash as a quad whose vertices carry only
 * random seeds ({@link #buildQuads()}); {@code liquid_weather.vsh} computes where each one is from
 * the time and a few offsets. Per frame the CPU integrates the fall and wind travel (doubles, wrapped
 * so float precision never degrades), fills one uniform block and issues a draw per layer. Per tick
 * it updates {@link WeatherColumns}, which tells the shader where rain and snow stop (roofs, trees,
 * water), which kind falls where, and how bright it is there.
 *
 * <h2>Layers</h2>
 * Rain and snow each have a dense near box (16 blocks) and a wider far box (twice the Weather Range),
 * so the air right around you is full while the distance stays affordable; splashes appear around
 * you on the ground. Drop positions use the R3 low-discrepancy sequence: evenly spread with no clumps
 * or holes, and any prefix is evenly spread too, so the density setting just draws fewer of them.
 *
 * <h2>Storms</h2>
 * A storm (Storm Chaser) takes over the wind – its direction along the storm's travel, its strength
 * with the gusts, so the rain slants hard – thickens the rain, and sweeps it through in sheets:
 * bands of heavier rain drifting downwind, anchored in the world ({@code liquid_weather.vsh}).
 *
 * <h2>Hooks</h2>
 * {@code WeatherEffectRendererMixin}: {@link #extract} replaces vanilla's column extraction (its
 * lists stay empty, so vanilla draws nothing), {@link #prepare} uploads before the frame's passes,
 * {@link #render} draws inside vanilla's weather slot – the translucent pass, or each OIT stage.
 * Falls back to vanilla when the shader can't be used or an Iris shader pack is active.
 */
public final class LiquidWeather {
	// Layers (mirrors liquid_weather.vsh).
	static final int RAIN_NEAR = 0;
	static final int RAIN_FAR = 1;
	static final int SNOW_NEAR = 2;
	static final int SNOW_FAR = 3;
	static final int SPLASH = 4;
	private static final int LAYERS = 5;
	private static final int[] CAPACITY = {6144, 18432, 4096, 12288, 3072};
	private static final int[] FIRST = new int[LAYERS];
	private static final int QUADS;

	static {
		int total = 0;
		for (int i = 0; i < LAYERS; i++) {
			FIRST[i] = total;
			total += CAPACITY[i];
		}
		QUADS = total;
	}

	private static final float NEAR_SIZE = 16f;
	private static final float SPLASH_SIZE = 26f;
	/** Rain falls at about 9 m/s; snow drifts down at about 1 m/s. */
	private static final float RAIN_SPEED = 9f;
	private static final float SNOW_SPEED = 1.05f;
	/** Streak length = drop speed × exposure (≈ 0.3 blocks): what a camera shutter would smear. */
	private static final float EXPOSURE = 1f / 30f;
	private static final double TIME_WRAP = 3600.0;
	private static final long IDLE_RELEASE_NANOS = 10_000_000_000L;

	private static final Supplier<String> LABEL = () -> "Liquid Weather";
	private static final FloatBlock DATA = new FloatBlock(15);
	private static final Matrix4f MODEL_VIEW = new Matrix4f();
	private static final DynamicGpuData.Transform TRANSFORM = new DynamicGpuData.Transform(MODEL_VIEW, new Vector4f(1f, 1f, 1f, 1f),
			new Vector3f(), new Matrix4f());
	private static final WeatherColumns COLUMNS = new WeatherColumns();
	/** Animation offsets per layer (x, y, z), wrapped at ten periods. */
	private static final double[] ANIM = new double[LAYERS * 3];
	private static final float[] PERIOD_H = new float[LAYERS];
	private static final float[] PERIOD_V = new float[LAYERS];
	private static final int[] DRAW = new int[LAYERS];

	private static @Nullable GpuBuffer quads;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> ring;
	private static @Nullable GpuBufferSlice slice;
	private static boolean active;
	private static boolean broken;
	private static long lastNanos = -1L;
	private static long lastDrawNanos;
	private static long lastColumnsNanos;
	private static double time;
	private static float rainLevel;
	private static float thunderLevel;
	private static float rainWindX;
	private static float rainWindZ;
	private static float windStrength;
	/** The wind's direction and its strength with gusts, for others (Liquid Water's waves); 0 when not drawing. */
	private static float windDirX = 1f;
	private static float windDirZ;
	private static float windNow;
	/** Where the storm's rain sheets are along the wind (blocks, wrapped at 4096: whole periods of both bands). */
	private static double sheetPhase;

	private LiquidWeather() {
	}

	// ---- Frame (render thread) ----------------------------------------------------------------

	/**
	 * Vanilla's weather extraction, replaced. Returns true when Liquid Weather is in charge (vanilla's
	 * columns are skipped), false to let vanilla draw its own weather.
	 */
	public static boolean extract(ClientLevel level, float partialTicks, Vec3 camera, WeatherRenderState state) {
		AmbienceModule settings = AmbienceModule.liquidWeather();
		ShaderSupport.ensureValidated();
		if (settings == null || broken || ShaderPacks.inUse() || !ShaderSupport.weather()) {
			active = false;
			lastNanos = -1L;
			return false;
		}
		float rain = level.getRainLevel(partialTicks);
		state.intensity = rain;
		state.radius = Math.round(settings.weatherRange.getFloat());
		rainLevel = rain;
		thunderLevel = rain > 0f ? level.getThunderLevel(partialTicks) / rain : 0f;
		active = rain > 0f && COLUMNS.ready(level);
		advance(settings, Minecraft.getInstance().isPaused());
		if (active) {
			counts(settings, rain);
		}
		return true;
	}

	/** Moves time, wind and the fall / drift offsets forward. */
	private static void advance(AmbienceModule settings, boolean paused) {
		long now = System.nanoTime();
		float dt = lastNanos < 0L || paused ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		if (!active) {
			return;
		}
		lastDrawNanos = now;
		time = (time + dt) % TIME_WRAP;

		float range = settings.weatherRange.getFloat();
		PERIOD_H[RAIN_NEAR] = NEAR_SIZE;
		PERIOD_V[RAIN_NEAR] = NEAR_SIZE;
		PERIOD_H[RAIN_FAR] = range * 2f;
		PERIOD_V[RAIN_FAR] = Math.max(24f, range * 1.5f);
		PERIOD_H[SNOW_NEAR] = NEAR_SIZE;
		PERIOD_V[SNOW_NEAR] = NEAR_SIZE;
		PERIOD_H[SNOW_FAR] = range * 2f;
		PERIOD_V[SNOW_FAR] = Math.max(24f, range * 1.5f);
		PERIOD_H[SPLASH] = Math.min(range * 2f, SPLASH_SIZE);
		PERIOD_V[SPLASH] = 1f;

		// Wind: the direction wanders slowly, gusts come and go, storms blow harder.
		float t = (float) time;
		windStrength = Math.min(1f, settings.wind.getFloat() / 100f * (1f + 0.6f * thunderLevel));
		float angle = 0.6f + 0.8f * Mth.sin(t * 0.013f) + 0.4f * Mth.sin(t * 0.031f + 1.3f);
		float gust = 0.75f + 0.25f * Mth.sin(t * 0.41f) * Mth.sin(t * 0.17f + 0.7f) + thunderLevel * 0.15f * Mth.sin(t * 1.3f);
		// A storm's wind blows along its travel, as hard as its gusts (up to 1.6: rain slanting ~40°).
		float presence = Storm.presence();
		if (presence > 0f) {
			windStrength = Math.max(windStrength, Math.min(1.6f, Storm.wind()));
			angle += Mth.wrapDegrees((Storm.windAngle() - angle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * presence;
			gust = Mth.lerp(presence, gust, 1f);
		}
		sheetPhase = floorMod(sheetPhase + dt * (5.0 + 9.0 * windStrength), 4096.0);
		float dirX = Mth.cos(angle);
		float dirZ = Mth.sin(angle);
		float rainWind = windStrength * gust * 4.5f;
		float snowWind = windStrength * gust * 2.2f + 0.2f;
		rainWindX = dirX * rainWind;
		rainWindZ = dirZ * rainWind;
		windDirX = dirX;
		windDirZ = dirZ;
		windNow = windStrength * gust;
		float fall = RAIN_SPEED * (1f + 0.12f * thunderLevel);

		for (int layer = RAIN_NEAR; layer <= RAIN_FAR; layer++) {
			move(layer, rainWindX * dt, -fall * dt, rainWindZ * dt);
		}
		for (int layer = SNOW_NEAR; layer <= SNOW_FAR; layer++) {
			move(layer, dirX * snowWind * dt, -SNOW_SPEED * dt, dirZ * snowWind * dt);
		}
	}

	private static void move(int layer, double dx, double dy, double dz) {
		double h = PERIOD_H[layer] * 10.0;
		double v = PERIOD_V[layer] * 10.0;
		int o = layer * 3;
		ANIM[o] = floorMod(ANIM[o] + dx, h);
		ANIM[o + 1] = floorMod(ANIM[o + 1] + dy, v);
		ANIM[o + 2] = floorMod(ANIM[o + 2] + dz, h);
	}

	private static void counts(AmbienceModule settings, float rain) {
		float storm = 1f + 0.25f * thunderLevel + 0.7f * Storm.rain() * Math.min(1.5f, Storm.module().intensity.getFloat() / 100f);
		float rainShare = settings.rainDensity.getFloat() / 200f * rain * storm;
		float snowShare = settings.snowDensity.getFloat() / 200f * rain;
		boolean anyRain = COLUMNS.anyRain();
		boolean anySnow = COLUMNS.anySnow();
		DRAW[RAIN_NEAR] = anyRain ? share(RAIN_NEAR, rainShare) : 0;
		DRAW[RAIN_FAR] = anyRain ? share(RAIN_FAR, rainShare) : 0;
		DRAW[SNOW_NEAR] = anySnow ? share(SNOW_NEAR, snowShare) : 0;
		DRAW[SNOW_FAR] = anySnow ? share(SNOW_FAR, snowShare) : 0;
		DRAW[SPLASH] = anyRain && settings.splashes.get() ? share(SPLASH, rainShare) : 0;
	}

	private static int share(int layer, float share) {
		return Math.clamp(Math.round(CAPACITY[layer] * share), 0, CAPACITY[layer]);
	}

	/** Before the frame's passes: GPU resources, the column upload and this frame's parameters. */
	public static void prepare(Vec3 camera) {
		slice = null;
		if (!active) {
			return;
		}
		try {
			if (quads == null) {
				quads = buildQuads();
			}
			if (ring == null) {
				ring = new DynamicGpuDataStorageMapped<>("Liquid weather UBO", DATA.size(), GpuBuffer.USAGE_UNIFORM, 1);
			}
			COLUMNS.upload();
			fill(camera);
			ring.endFrame();
			slice = ring.writeData(DATA);
			RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).requestIndexCount(QUADS * 6);
		} catch (RuntimeException e) {
			fail(e);
		}
	}

	private static void fill(Vec3 camera) {
		AmbienceModule settings = AmbienceModule.liquidWeather();
		int bx = Mth.floor(camera.x);
		int by = Mth.floor(camera.y);
		int bz = Mth.floor(camera.z);
		DATA.vec4(0, bx, by, bz, (float) time);
		DATA.vec4(1, (float) (camera.x - bx), (float) (camera.y - by), (float) (camera.z - bz),
				Minecraft.getInstance().getWindow().getHeight());
		for (int layer = 0; layer < LAYERS; layer++) {
			float h = PERIOD_H[layer];
			float v = PERIOD_V[layer];
			int o = layer * 3;
			DATA.vec4(2 + layer * 2, (float) ANIM[o], (float) ANIM[o + 1], (float) ANIM[o + 2], h);
			DATA.vec4(3 + layer * 2, (float) floorMod(camera.x, h), (float) floorMod(camera.y, v), (float) floorMod(camera.z, h), v);
		}
		DATA.vec4(12, rainWindX, RAIN_SPEED * (1f + 0.12f * thunderLevel), rainWindZ, Storm.sheets());
		float size = settings != null ? settings.flakeSize.getFloat() / 100f : 1f;
		DATA.vec4(13, 0.3f * (0.75f + 0.25f * rainLevel), 0.9f, size, 0.6f);
		DATA.vec4(14, EXPOSURE, windStrength, thunderLevel, (float) sheetPhase);
	}

	/** Inside vanilla's weather slot: the translucent pass, or one OIT stage. */
	public static void render(RenderPass pass, RenderPipeline pipeline) {
		GpuBufferSlice uniforms = slice;
		GpuBuffer buffer = quads;
		GpuTextureView columns = COLUMNS.view();
		if (!active || uniforms == null || buffer == null || columns == null) {
			return;
		}
		try {
			CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
			if (compiled == null) {
				fail(null);
				return;
			}
			MODEL_VIEW.set(RenderSystem.getModelViewStack());
			GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(TRANSFORM);
			RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
			pass.pushDebugGroup(LABEL);
			pass.setPipeline(compiled);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setUniform("LiquidWeather", uniforms);
			pass.setUniform("ColumnSampler", columns, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			pass.setUniform("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(),
					RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			pass.setIndexBuffer(indices.getBuffer(), indices.type());
			pass.setVertexBuffer(0, buffer.slice());
			for (int layer = 0; layer < LAYERS; layer++) {
				if (DRAW[layer] > 0) {
					pass.drawIndexed(DRAW[layer] * 6, 1, FIRST[layer] * 6, 0, 0);
				}
			}
			pass.popDebugGroup();
		} catch (RuntimeException e) {
			fail(e);
		}
	}

	/** Whether vanilla's rain splash particles should be skipped (ours replace them). */
	public static boolean drawsSplashes() {
		return active && DRAW[SPLASH] > 0;
	}

	// ---- Tick (client thread) -----------------------------------------------------------------

	/**
	 * Keeps the column data current while it may rain (or Wet World needs it to dry), and frees the
	 * GPU side when neither is around any more.
	 */
	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		AmbienceModule settings = AmbienceModule.liquidWeather();
		long now = System.nanoTime();
		boolean drops = level != null && settings != null && !broken && level.getRainLevel(1f) > 0f && ShaderSupport.weather();
		boolean wet = level != null && (WetWorld.visible() || LiquidWater.wantsColumns() && level.getRainLevel(1f) > 0f);
		if ((drops || wet) && !ShaderPacks.inUse()) {
			Vec3 camera = minecraft.gameRenderer.mainCamera().position();
			COLUMNS.update(level, Mth.floor(camera.x), Mth.floor(camera.y), Mth.floor(camera.z));
			lastColumnsNanos = now;
		}
		if (quads != null && now - lastDrawNanos > IDLE_RELEASE_NANOS) {
			releaseDrops();
		}
		if (COLUMNS.allocated() && now - lastColumnsNanos > IDLE_RELEASE_NANOS) {
			COLUMNS.release();
			COLUMNS.reset();
		}
	}

	/** Wind strength with gusts (0..1.6) while Liquid Weather draws, else 0. */
	public static float windStrength() {
		return active && System.nanoTime() - lastDrawNanos < 500_000_000L ? windNow : 0f;
	}

	/** The wind's direction (x), unit length together with {@link #windZ()}. */
	public static float windX() {
		return windDirX;
	}

	public static float windZ() {
		return windDirZ;
	}

	/**
	 * The column texture for other effects (Wet World, Liquid Water's rain rings), uploaded and current; null until the columns
	 * around the camera have been computed for {@code level}. Render thread, outside a render pass.
	 */
	public static @Nullable GpuTextureView columns(ClientLevel level) {
		if (!COLUMNS.ready(level)) {
			return null;
		}
		try {
			COLUMNS.upload();
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.warn("Weather columns upload failed", e);
			return null;
		}
		return COLUMNS.view();
	}

	// ---- Resources ----------------------------------------------------------------------------

	/**
	 * Every quad of every layer: four vertices with the same seeds and their corner. x / y / z follow
	 * the R3 sequence (evenly spread in 3D), w is random (speed class, phases).
	 */
	private static GpuBuffer buildQuads() {
		int vertexSize = LiquidPipelines.WEATHER_FORMAT.getVertexSize();
		ByteBuffer data = MemoryUtil.memAlloc(QUADS * 4 * vertexSize);
		try {
			Random random = new Random(0x5EA5_0F_1A1FL);
			double g = 1.2207440846057596; // plastic number: R3 = (1/g, 1/g², 1/g³)
			double a1 = 1.0 / g;
			double a2 = 1.0 / (g * g);
			double a3 = 1.0 / (g * g * g);
			for (int layer = 0; layer < LAYERS; layer++) {
				double offset = 0.5 + layer * 0.1234;
				for (int n = 0; n < CAPACITY[layer]; n++) {
					float x = (float) frac(offset + a1 * n);
					float y = (float) frac(offset + a2 * n);
					float z = (float) frac(offset + a3 * n);
					byte w = (byte) random.nextInt(256);
					corner(data, x, y, z, 0, 1, layer, w);
					corner(data, x, y, z, 1, 1, layer, w);
					corner(data, x, y, z, 1, 0, layer, w);
					corner(data, x, y, z, 0, 0, layer, w);
				}
			}
			data.flip();
			return RenderSystem.getDevice().createBuffer(() -> "Liquid weather quads", GpuBuffer.USAGE_VERTEX, data);
		} finally {
			MemoryUtil.memFree(data);
		}
	}

	private static void corner(ByteBuffer data, float x, float y, float z, int cornerX, int cornerY, int layer, byte w) {
		data.putFloat(x).putFloat(y).putFloat(z);
		data.put((byte) (cornerX * 255)).put((byte) (cornerY * 255)).put((byte) layer).put(w);
	}

	private static double frac(double v) {
		return v - Math.floor(v);
	}

	private static double floorMod(double v, double m) {
		return v - Math.floor(v / m) * m;
	}

	private static void fail(@Nullable Throwable error) {
		broken = true;
		active = false;
		LiquidClient.LOGGER.error("Liquid Weather disabled for this session – falling back to vanilla rain and snow", error);
		release();
	}

	/** Frees the GPU resources and forgets the columns. Render thread, between frames. Idempotent. */
	public static void release() {
		releaseDrops();
		COLUMNS.release();
		COLUMNS.reset();
	}

	/** The drops' buffers only (the columns may still serve Wet World). */
	private static void releaseDrops() {
		if (quads != null) {
			quads.close();
			quads = null;
		}
		if (ring != null) {
			ring.close();
			ring = null;
		}
		slice = null;
		active = false;
		lastNanos = -1L;
	}
}
