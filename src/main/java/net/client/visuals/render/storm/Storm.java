package net.client.visuals.render.storm;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.StormChaserModule;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The storm's life – the state behind {@code StormChaserModule}, read by everything that draws or
 * sounds it.
 *
 * <h2>One timeline</h2>
 * A storm is a long cell travelling along a horizontal axis. Everything follows from where its front
 * is: far beyond the horizon at first (it emerges from the haze), then a dark wall with a shelf cloud
 * building on the horizon, the shelf passing overhead (gust front: the wind peaks, light rain), the
 * rain core with the most lightning, the trailing deck with mammatus, and finally clear sky as it
 * recedes the other way. The front's position is a function of a timeline {@link #tau}: an eased
 * approach over the build-up time (fast while far, steady at 45 blocks/s when it arrives), then the
 * cell passes over in the duration setting. "Always" and "follow server" hold the timeline at the
 * peak while it should storm. The position is anchored in the world, so you can chase the storm or
 * flee from it; a teleport re-anchors it.
 *
 * <h2>What it feeds</h2>
 * Per frame ({@link #frame}, render thread, before the world renders): the front / back relative to
 * the camera, the rain falling here, darkness, wind and gusts, haze, mammatus and lightning activity.
 * {@code AmbienceModule} raises the client's rain and thunder levels to it (so vanilla's sky
 * darkening, rain sounds, Wet World, Wet Lens and Rainbows follow), {@code LiquidWeather} takes the
 * wind and sheets, {@code FoliageWaving} the gusts, Atmosphere the haze, {@link StormSky} the rest.
 *
 * <p>The storm clock ({@link #time}) stops while the game is paused – Photo Mode's freeze included –
 * so a bolt can be photographed mid-strike. Lengths are blocks; nothing here allocates per frame.</p>
 */
public final class Storm {
	/** The storm's cloud geometry (mirrored in {@code storm_sky.fsh}). */
	public static final float SHELF = 1300f;
	public static final float TRAIL = 6500f;
	public static final float RAIN_LAG = 1100f;
	public static final float LENGTH = 17000f;
	public static final float ARC_R = 30000f;
	/** Where a storm starts: so far away that it emerges from the haze. */
	private static final float FAR = 42000f;
	private static final float ARRIVAL_SPEED = 45f;
	/** The deck's base sits here (world y), but never closer than {@link #MIN_BASE} above the camera. */
	private static final float BASE_Y = 590f;
	private static final float MIN_BASE = 180f;
	/** Fully gone once the back edge is this far past. */
	private static final float GONE = 14000f;
	/** Noise period of the sky texture (blocks): world positions are wrapped to it (storm_sky.fsh). */
	public static final double NOISE_PERIOD = 49152.0;

	private static final Smooth PRESENCE = new Smooth(0f, 0.5f);
	private static final Smooth GUST = new Smooth(0f, 1.6f);
	private static final Smooth WIND_ANGLE = new Smooth(0f, 0.25f);

	private static @Nullable StormChaserModule module;

	// The storm.
	private static boolean running;
	private static boolean pendingStart;
	/** Timeline position (s). */
	private static double tau;
	/** Timeline speed: 1, faster when sent away, negative to retreat over the horizon. */
	private static float rate = 1f;
	/** World coordinate along the axis where the timeline's front position is measured from. */
	private static double anchor;
	/** World coordinate across the axis of the storm's centre line. */
	private static double centre;
	private static float axisX = 1f;
	private static float axisZ;
	private static boolean anchored;
	private static boolean serverThundered;
	private static float serverThunder;

	// Clock.
	private static double clock;
	private static double drift;
	private static long lastNanos = -1L;
	private static float dt;
	private static long frames;
	/** This frame's {@link #peakTau()} (it depends on the settings). */
	private static double peak;

	// This frame, relative to the camera.
	private static float front = -FAR;
	private static float back = -FAR - LENGTH;
	private static float across;
	private static float base = 520f;
	private static float rain;
	private static float darkness;
	private static float influence;
	private static float wind;
	private static float sheets;
	private static float haze;
	private static float mammatus;
	private static float activity;
	private static float under;
	private static boolean overworld;
	private static double lastCameraX;
	private static double lastCameraZ;

	private Storm() {
	}

	public static StormChaserModule module() {
		StormChaserModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(StormChaserModule.class);
		}
		return m;
	}

	// ---- Triggers (client thread) ------------------------------------------------------------------

	public static void onModuleEnabled() {
		StormChaserModule m = module();
		if (running && rate < 0f) {
			rate = 1f; // it was retreating: it comes back
			return;
		}
		if (running && tau > peakTau() + 1.0) {
			pendingStart = true; // the next one comes once this one has passed
			rate = 2.5f;
			return;
		}
		if (!running) {
			start(m.trigger.index() == StormChaserModule.TRIGGER_ALWAYS ? peakTau() : 0.0);
		}
		rate = 1f;
	}

	public static void onModuleDisabled() {
		pendingStart = false;
		if (!running) {
			return;
		}
		// Before it arrives it turns back over the horizon; once here it hurries past.
		rate = tau < buildUp() ? -3f : 3f;
	}

	private static void start(double at) {
		running = true;
		tau = at;
		rate = 1f;
		anchored = false;
		pickAxis();
	}

	/**
	 * The storm comes from the sun's side, so it's backlit on the way in and the sun lights it (and a
	 * rainbow can form on its rain) as it leaves – with some variety; at night or noon, any direction.
	 */
	private static void pickAxis() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double angle = random.nextDouble() * Math.PI * 2.0;
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level != null) {
			float sunAngle = StormSky.lastSunAngle();
			float sunX = -Mth.sin(sunAngle);
			float sunY = Mth.cos(sunAngle);
			if (sunY > 0.05f && sunY < 0.8f && Math.abs(sunX) > 0.2f) {
				// Vanilla's sun moves in the x / y plane: it's east (+x) or west (-x) of you.
				double away = sunX > 0f ? Math.PI : 0.0;
				angle = away + (random.nextDouble() - 0.5) * Math.toRadians(70.0);
			}
		}
		axisX = (float) Math.cos(angle);
		axisZ = (float) Math.sin(angle);
	}

	// ---- Timeline ----------------------------------------------------------------------------------

	private static float buildUp() {
		return module().buildUp.getFloat();
	}

	/** Blocks per second while the cell passes over (the whole cell in the duration setting). */
	private static float passSpeed() {
		return (LENGTH + 2500f) / module().duration.getFloat();
	}

	/** The front's position (blocks along the axis from where it's anchored) at timeline position t. */
	private static float frontAt(double t) {
		float build = buildUp();
		if (t < build) {
			// Fast while far away, steady and continuous (position and speed) when it arrives.
			float u = (float) (1.0 - t / build);
			float linear = ARRIVAL_SPEED * build;
			return -(linear * u + (FAR - linear) * u * u * u);
		}
		float pass = passSpeed();
		// Over the first stretch the speed blends from the arrival speed to the passing speed.
		float s = (float) (t - build);
		float blend = Math.min(s, 20f);
		return ARRIVAL_SPEED * blend + (pass - ARRIVAL_SPEED) * blend * blend / 40f + pass * Math.max(0f, s - 20f);
	}

	/** Where "always" and "follow server" hold the storm: the camera in the middle of the rain core. */
	private static double peakTau() {
		float hold = LENGTH * 0.42f;
		float build = buildUp();
		// Invert frontAt past the arrival (it's monotonic): bisection, it runs rarely.
		double lo = build;
		double hi = build + 4000.0;
		for (int i = 0; i < 40; i++) {
			double mid = 0.5 * (lo + hi);
			if (frontAt(mid) < hold) {
				lo = mid;
			} else {
				hi = mid;
			}
		}
		return hi;
	}

	private static boolean holds(StormChaserModule m) {
		if (!m.isEnabled()) {
			return false;
		}
		return switch (m.trigger.index()) {
			case StormChaserModule.TRIGGER_ALWAYS -> true;
			case StormChaserModule.TRIGGER_SERVER -> serverThunder > 0.3f;
			default -> false;
		};
	}

	// ---- Client tick -------------------------------------------------------------------------------

	/** Client tick: triggers that follow the world (server thunder, always), and the end of a storm. */
	public static void tick(Minecraft minecraft) {
		StormChaserModule m = module();
		ClientLevel level = minecraft.level;
		overworld = level != null && level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD;
		if (level == null) {
			reset();
			return;
		}
		if (m.isEnabled()) {
			int trigger = m.trigger.index();
			if (trigger == StormChaserModule.TRIGGER_SERVER) {
				boolean thunder = serverThunder > 0.3f;
				if (thunder && !serverThundered && !running) {
					start(0.0);
				}
				serverThundered = thunder;
			} else if (trigger == StormChaserModule.TRIGGER_ALWAYS && !running) {
				start(peakTau());
			}
		}
	}

	// ---- Frame (render thread) ---------------------------------------------------------------------

	/**
	 * Once per frame before the world renders (from {@code PostFx#beginLevel}): advances the clock and
	 * the timeline and derives everything else for the camera at (x, y, z).
	 */
	public static void frame(double x, double y, double z) {
		long now = System.nanoTime();
		Minecraft minecraft = Minecraft.getInstance();
		boolean paused = minecraft.isPaused();
		float elapsed = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		dt = paused ? 0f : elapsed;
		frames++;
		StormChaserModule m = module();
		clock = (clock + dt) % 7200.0;
		peak = peakTau();

		if (running) {
			advanceTimeline(m);
		}
		// Re-anchor on teleports (and on the first frame of a storm): the storm comes along.
		double along = x * axisX + z * axisZ;
		double side = -x * axisZ + z * axisX;
		double jumpX = x - lastCameraX;
		double jumpZ = z - lastCameraZ;
		lastCameraX = x;
		lastCameraZ = z;
		if (running && (!anchored || jumpX * jumpX + jumpZ * jumpZ > 2000.0 * 2000.0)) {
			float rel = frontAt(tau);
			anchor = along;
			centre = side;
			anchored = true;
			front = rel;
		}
		if (running && rate > 0f && holds(m) && tau >= peak - 0.5) {
			// Held at the peak: it stays over you wherever you go.
			anchor = along;
			centre = side;
		}
		front = running ? (float) (anchor + frontAt(tau) - along) : -FAR * 2f;
		back = front - LENGTH;
		across = (float) (side - centre);
		base = Math.max(MIN_BASE, BASE_Y - (float) y);

		PRESENCE.speed(0.5f);
		float visible = running && overworld ? Math.clamp((float) (tau / Math.max(1f, buildUp() * 0.3f)), 0f, 1f) : 0f;
		if (!running) {
			visible = 0f;
		}
		PRESENCE.target(visible).update(elapsed);
		derive(m);
	}

	private static void advanceTimeline(StormChaserModule m) {
		boolean hold = holds(m) && rate > 0f;
		double next = tau + dt * rate;
		if (hold && tau <= peak && next > peak) {
			next = peak;
		}
		if (hold && tau >= peak) {
			next = tau; // held
		}
		tau = next;
		if (tau < 0.0) {
			end();
			return;
		}
		// Gone once the back edge is far past.
		if (frontAt(tau) - LENGTH > GONE) {
			end();
		}
	}

	private static void end() {
		running = false;
		tau = 0.0;
		rate = 1f;
		StormChaserModule m = module();
		if (pendingStart && m.isEnabled()) {
			pendingStart = false;
			start(0.0);
			return;
		}
		pendingStart = false;
		// A storm on demand is over: the module switches itself off (the sky has cleared).
		if (m.isEnabled() && m.trigger.index() == StormChaserModule.TRIGGER_ON_DEMAND) {
			m.setEnabled(false);
		}
	}

	/** Everything else, from where the front is relative to the camera. */
	private static void derive(StormChaserModule m) {
		float presence = PRESENCE.get();
		float strength = m.intensity.getFloat() / 100f;
		// Rain at the camera: the core between the trailing deck and the front (same as the sky's curtains),
		// and a few drops from the shelf ahead of it.
		float core = smoothstep(back + TRAIL * 0.3f, back + TRAIL * 0.6f, 0f) * (1f - smoothstep(front - RAIN_LAG - 1800f, front - RAIN_LAG, 0f));
		float shelf = smoothstep(front + SHELF, front, 0f) * (1f - smoothstep(front - 600f, front - 1800f, 0f));
		rain = Math.clamp(Math.max(core, shelf * 0.25f) * presence * Math.min(1f, 0.55f + 0.45f * strength), 0f, 1f);
		// Overhead: under the deck from the shelf's leading edge to the trailing deck's end.
		under = smoothstep(front + SHELF + 300f, front + SHELF - 300f, 0f) * smoothstep(back - 300f, back + TRAIL * 0.4f, 0f);
		// How near the storm is: rises over the last 10 km of its approach, falls as it recedes.
		float approach = 1f - smoothstep(-500f, -10000f, front);
		float recede = 1f - smoothstep(0f, 9000f, back);
		influence = Math.min(approach, recede) * presence;
		darkness = Math.clamp(under * (0.65f + 0.35f * core) + influence * 0.25f, 0f, 1f) * presence;
		// The gust front: the wind peaks as the shelf passes overhead, then stays strong in the core.
		float gustFront = (float) Math.exp(-sq((front + SHELF * 0.4f) / 1500f));
		float t = (float) clock;
		float gusts = 0.6f + 0.25f * Mth.sin(t * 0.37f) * Mth.sin(t * 0.13f + 1.1f) + 0.15f * Mth.sin(t * 1.21f + 2.3f);
		float target = (influence * 0.55f + under * 0.25f + gustFront * 0.6f) * gusts;
		GUST.target(target).update(dt);
		wind = GUST.get() * m.wind.getFloat() / 100f * presence;
		// Sheets of rain sweeping through: strongest in the core.
		sheets = core * presence * (0.55f + 0.45f * Mth.sin(t * 0.11f));
		haze = Math.clamp(influence * 0.35f + core * 0.65f, 0f, 1f) * presence * Math.min(1f, strength);
		// Mammatus: under the trailing deck after the core has passed.
		mammatus = smoothstep(back + TRAIL, back + TRAIL * 0.55f, 0f) * smoothstep(back - 1500f, back + 600f, 0f) * presence;
		// Lightning: the storm matures on the way in, peaks in the core, fades as it goes.
		float maturity = smoothstep(-FAR * 0.8f, -6000f, front) * (1f - smoothstep(2000f, 12000f, back));
		activity = maturity * presence * strength;
		drift = (drift + dt * (14f + 26f * wind)) % NOISE_PERIOD;
		// The wind turns with the storm: it blows along the axis.
		WIND_ANGLE.target((float) Math.atan2(axisZ, axisX)).update(dt);
	}

	/** World left / no level: nothing lingers. */
	public static void reset() {
		running = false;
		pendingStart = false;
		tau = 0.0;
		rate = 1f;
		anchored = false;
		PRESENCE.snap(0f);
		GUST.snap(0f);
		front = -FAR * 2f;
		back = front - LENGTH;
		rain = 0f;
		darkness = 0f;
		influence = 0f;
		wind = 0f;
		sheets = 0f;
		haze = 0f;
		mammatus = 0f;
		activity = 0f;
		under = 0f;
		serverThundered = false;
		lastNanos = -1L;
	}

	// ---- Hooks ---------------------------------------------------------------------------------------

	/** The server's thunder level, before Ambience or the storm change it (from the Ambience hook). */
	public static void serverThunder(float thunderLevel) {
		serverThunder = thunderLevel;
	}

	/** Where a storm rains, it rains – deserts included (only for what you see and hear). */
	public static boolean rainsEverywhere() {
		return active() && rain > 0.001f;
	}

	/** Raises the client's rain level to the storm's rain here. */
	public static float rainLevel(float level) {
		return active() ? Math.max(level, rain) : level;
	}

	/** Raises the thunder level (vanilla darkens the sky and the light with it) to the storm's darkness. */
	public static float thunderLevel(float level) {
		return active() ? Math.max(level, darkness) : level;
	}

	// ---- What the rest of the client reads ---------------------------------------------------------------

	/** Anything of a storm to show (or hear). */
	public static boolean active() {
		return PRESENCE.get() > 0.001f && overworld;
	}

	public static boolean running() {
		return running;
	}

	/** 0..1: fades a storm in from the haze and out with the world. */
	public static float presence() {
		return overworld ? PRESENCE.get() : 0f;
	}

	/** Storm clock (s): stops while the game is paused. */
	public static double time() {
		return clock;
	}

	/** This frame's step of the storm clock (0 while paused). */
	public static float dt() {
		return dt;
	}

	public static long frameCount() {
		return frames;
	}

	public static float front() {
		return front;
	}

	public static float back() {
		return back;
	}

	/** The camera's distance across the axis from the storm's centre line. */
	public static float across() {
		return across;
	}

	public static float axisX() {
		return axisX;
	}

	public static float axisZ() {
		return axisZ;
	}

	/** The deck's base above the camera (blocks). */
	public static float base() {
		return base;
	}

	/** World y of the deck's base. */
	public static float baseY(double cameraY) {
		return (float) cameraY + base;
	}

	/** Rain falling where you are, 0..1. */
	public static float rain() {
		return rain;
	}

	/** 0..1: how dark it is under the storm. */
	public static float darkness() {
		return darkness;
	}

	/** 0..1: how near the storm is (wind, cold light, haze). */
	public static float influence() {
		return influence;
	}

	/** 0..1: the deck overhead. */
	public static float under() {
		return under;
	}

	/** Wind strength 0..2 (the wind setting can double it). */
	public static float wind() {
		return wind;
	}

	/** Wind direction (radians, world x towards z) – along the storm's travel. */
	public static float windAngle() {
		return WIND_ANGLE.get();
	}

	/** 0..1: sheets of rain sweeping through. */
	public static float sheets() {
		return sheets;
	}

	public static float haze() {
		return haze;
	}

	public static float mammatus() {
		return mammatus;
	}

	/** 0..1.5: how active the storm's lightning is now. */
	public static float activity() {
		return activity;
	}

	/** The deck's cloud texture drift along the axis (blocks, wrapped to the noise period). */
	public static double drift() {
		return drift;
	}

	private static float sq(float v) {
		return v * v;
	}

	static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
