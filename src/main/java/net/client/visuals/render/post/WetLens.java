package net.client.visuals.render.post;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.WetLensModule;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.storm.Storm;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * How wet (or frosted) the camera is – the state behind {@code WetLensModule}, drawn by
 * {@link PostFx}.
 *
 * <ul>
 *   <li><b>Rain</b> – under open sky where it rains (the client's precipitation, so Ambience's
 *       override applies), the lens gets wetter, faster when looking up (a storm's wind drives the
 *       rain into it wherever you look); out of the rain it dries over ~10 s.</li>
 *   <li><b>Drench</b> – the moment the camera comes out of water the lens is streaming; the sheet
 *       drains from the top over ~3.5 s.</li>
 *   <li><b>Frost</b> – where it snows the lens frosts from the edges over ~15 s and thaws slowly.</li>
 * </ul>
 * Targets move on the client tick; what's drawn eases towards them every frame (frame-rate
 * independent), so nothing steps at 20 Hz.
 */
public final class WetLens {
	/** Drench length in ticks. */
	private static final float DRENCH_TICKS = 70f;

	private static final Smooth WET = new Smooth(0f, 1.5f);
	private static final Smooth FROST = new Smooth(0f, 1.2f);
	private static float wetTarget;
	private static float frostTarget;
	/** Ticks since surfacing, or a large value when not drenched. */
	private static float drenchAge = DRENCH_TICKS;
	private static float drench;
	private static boolean wasInWater;
	private static double time;
	private static long lastNanos = -1L;
	private static @Nullable WetLensModule module;

	private WetLens() {
	}

	private static WetLensModule module() {
		WetLensModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(WetLensModule.class);
		}
		return m;
	}

	/** Client tick: where the camera is and what's falling there. */
	public static void tick(Minecraft minecraft) {
		WetLensModule m = module();
		ClientLevel level = minecraft.level;
		if (!m.isEnabled() || level == null || minecraft.player == null) {
			wetTarget = 0f;
			frostTarget = 0f;
			drenchAge = DRENCH_TICKS;
			wasInWater = false;
			return;
		}
		Camera camera = minecraft.gameRenderer.mainCamera();
		boolean inWater = camera.getFluidInCamera() == FogType.WATER;
		if (wasInWater && !inWater && m.drench.get()) {
			drenchAge = 0f;
		}
		wasInWater = inWater;
		drenchAge = Math.min(DRENCH_TICKS, drenchAge + 1f);

		Vec3 position = camera.position();
		BlockPos at = BlockPos.containing(position);
		float rain = level.getRainLevel(1f);
		boolean openSky = level.getHeight(Heightmap.Types.MOTION_BLOCKING, at.getX(), at.getZ()) <= at.getY();
		Biome.Precipitation falling = rain > 0f && openSky && !inWater ? level.getPrecipitationAt(at) : Biome.Precipitation.NONE;
		// Looking up catches more rain than looking at the horizon (xRot -90 = straight up).
		float up = Math.clamp(-camera.xRot() / 90f, 0f, 1f);
		wetTarget = m.raindrops.get() && falling == Biome.Precipitation.RAIN ? rain * (0.55f + 0.45f * up) : 0f;
		// A storm's wind drives the rain into the lens wherever you look.
		wetTarget = Math.min(1f, wetTarget * (1f + 0.6f * Math.min(1f, Storm.wind())));
		if (m.alwaysWet.get() && m.raindrops.get()) {
			wetTarget = 1f;
		}
		frostTarget = m.frost.get() && falling == Biome.Precipitation.SNOW ? rain : 0f;
		if (inWater) {
			frostTarget = 0f;
		}
	}

	/** Render thread, once per frame: eases towards the tick targets. Whether anything is on the lens. */
	static boolean advance(boolean paused) {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		if (paused) {
			dt = 0f;
		}
		time = (time + dt) % 3600.0;
		// Wetting is quick, drying slow; frost builds slowly and thaws slower still.
		float target = wetTarget;
		WET.speed(target > WET.get() ? 1.5f : 0.22f);
		WET.target(target).update(dt);
		FROST.speed(frostTarget > FROST.get() ? 0.18f : 0.1f);
		FROST.target(frostTarget).update(dt);
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float age = Math.min(DRENCH_TICKS, drenchAge + partial);
		drench = age >= DRENCH_TICKS ? 0f : 1f - age / DRENCH_TICKS;
		return module().isEnabled() && (WET.get() > 0.003f || FROST.get() > 0.003f || drench > 0f);
	}

	/** Fills the {@code LiquidLens} block (see {@code post_lens.fsh}). */
	static void write(FloatBlock block, int width, int height) {
		WetLensModule m = module();
		block.vec4(0, (float) time, width / (float) Math.max(1, height), WET.get(), drench > 0f ? Math.min(1f, drench * 1.6f) : 0f);
		block.vec4(1, FROST.get(), m.amount.getFloat() / 100f, m.dropSize.getFloat() / 100f, m.refraction.getFloat() / 100f);
		block.vec4(2, 1f / Math.max(1, width), 1f / Math.max(1, height), 1f - drench, 0f);
	}

	/** World left: a dry, clear lens next time. */
	public static void reset() {
		WET.snap(0f);
		FROST.snap(0f);
		wetTarget = 0f;
		frostTarget = 0f;
		drenchAge = DRENCH_TICKS;
		drench = 0f;
		wasInWater = false;
	}
}
