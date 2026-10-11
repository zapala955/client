package net.client.visuals.render.camera;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.player.ShowcaseModule;
import net.client.visuals.module.player.WorldIntroModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.shader.ShaderSupport;
import net.client.visuals.render.storm.Storm;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * World Intro ({@link WorldIntroModule}): an opening drone shot when you join a world (and, if
 * asked, when you change dimension).
 *
 * <h2>Waiting</h2>
 * The request is armed on join. Once the loading screen is gone and the chunks the shot will fly
 * over have loaded, the path is planned and the camera parks at its start behind black, so the
 * renderer builds what it will see from there; after a short settle (or at the latest after a
 * few seconds) the picture fades in.
 *
 * <h2>The path</h2>
 * Each style is an analytic curve around your eyes – Orbit Reveal sweeps 150° around you from high
 * and far, High Drone Descent drops from 60 blocks above while turning, Dolly In pushes in from far
 * behind – that ends a few blocks behind you. It's sampled, lifted clear of the terrain (heightmap,
 * which counts fluids), smoothed, and checked: every sample in a loaded chunk, every segment clear of
 * blocks (raycast), the last one clear to your eyes. If a variant fails the planner tries others
 * (the other side, higher, closer, ending beside you); indoors or in a cave there's no flight, just
 * the title card over your own view.
 *
 * <h2>Playing</h2>
 * The camera follows the path (Catmull-Rom through the samples, eased), looking at you – with a lead
 * along the way early on, to reveal the land – then the hand-over: position and rotation blend into
 * your live eyes and view (so turning the mouse is fine), third person while out, first person near
 * the eyes, like Showcase. Depth of Field (if available) focuses far at first and pulls onto you, then
 * relaxes to sharp as the camera enters your eyes; Motion Blur rests. Any key, click or scroll skips
 * instantly, as does damage, a hostile mob within 12 blocks or a screen opening. Input is never held.
 * Frame-rate independent; nothing here allocates per frame (planning allocates once, behind black).
 */
public final class WorldIntro {
	/** {@link #apply} results, like {@link Showcase}'s. */
	public static final int VANILLA = 0;
	public static final int SHOT = 1;
	public static final int EYES = 2;

	private static final int SAMPLES = 160;
	private static final double HIDE_DISTANCE = 0.85;
	/** Longest wait for the world to be ready, and the settle once it is. */
	private static final float MAX_WAIT = 12f;
	private static final float SETTLE = 1.4f;
	private static final float FADE_IN = 0.9f;
	/** The part of the timeline spent on the path; the hand-over overlaps its end. */
	private static final float PATH_END = 0.86f;
	private static final float HAND_OVER = 0.8f;
	private static final double HOSTILE_RANGE = 12.0;

	private static final int IDLE = 0;
	/** Requested; the loading screen is still up. */
	private static final int ARMED = 1;
	/** The loading screen is gone: black, waiting for the land under the shot to load. */
	private static final int LOADING = 2;
	/** Planned; parked at the start behind black while the renderer catches up. */
	private static final int PARKED = 3;
	private static final int PLAYING = 4;

	private static int state = IDLE;
	private static boolean dimensionChange;
	private static float waited;
	private static float parked;
	private static float played;
	private static long lastNanos = -1L;
	private static boolean flight;
	private static float duration = 10f;
	private static @Nullable ResourceKey<Level> lastDimension;
	private static @Nullable ClientLevel lastLevel;
	private static float lastHealth = -1f;
	private static boolean switched;
	private static @Nullable CameraType previous;

	// The planned path: positions and look targets (world space), one sample per row.
	private static final double[] PATH = new double[SAMPLES * 3];
	private static final double[] LOOK = new double[SAMPLES * 3];
	private static final double[] TMP = new double[3];

	// The camera this frame.
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static float cameraYaw;
	private static float cameraPitch;
	private static float lensMix;
	private static float focusDistance = 64f;
	private static float dofAmount;

	// The world clock as the client last saw it (overworld day clock: day count, time of day).
	private static volatile long clockTicks = -1L;

	// Title card.
	private static final TextLabel TAG = new TextLabel("", LiquidText.TextStyle.CAPTION).uppercase().tracking(1.6f);
	private static final TextLabel TITLE = new TextLabel("", LiquidText.TextStyle.DISPLAY);
	private static final TextLabel TITLE_SMALL = new TextLabel("", LiquidText.TextStyle.TITLE);
	private static final int FACTS = 4;
	private static final TextLabel[] FACT = new TextLabel[FACTS];
	private static final int[] FACT_ICON = new int[FACTS];
	private static int factCount;
	private static boolean bigTitle;

	private static @Nullable WorldIntroModule module;

	static {
		for (int i = 0; i < FACTS; i++) {
			FACT[i] = new TextLabel("", LiquidText.TextStyle.LABEL);
		}
	}

	private WorldIntro() {
	}

	public static WorldIntroModule module() {
		WorldIntroModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(WorldIntroModule.class);
		}
		return m;
	}

	// ---- Requests --------------------------------------------------------------------------------

	/** Joined a world or server (client thread). */
	public static void onJoin() {
		WorldIntroModule m = module();
		lastDimension = null;
		lastLevel = null;
		if (m.isEnabled() && m.play.index() != WorldIntroModule.PLAY_OFF) {
			arm(false);
		}
	}

	/** Play it now (the key), wherever you are. */
	public static void playNow() {
		if (Minecraft.getInstance().level != null) {
			arm(false);
		}
	}

	private static void arm(boolean dimension) {
		end(Minecraft.getInstance());
		state = ARMED;
		dimensionChange = dimension;
		waited = 0f;
		lastHealth = -1f;
	}

	/** Left the world. */
	public static void reset() {
		end(Minecraft.getInstance());
		lastDimension = null;
		lastLevel = null;
		clockTicks = -1L;
	}

	/** The raw total ticks of a world clock the client evaluated (the day clock, in the overworld). */
	public static void observeClock(long ticks) {
		clockTicks = ticks;
	}

	// ---- Client tick -----------------------------------------------------------------------------

	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (level == null || player == null) {
			if (state != IDLE && state != ARMED) {
				end(minecraft);
			}
			return;
		}
		// A dimension change (not a respawn in the same one) plays it too, when asked.
		ResourceKey<Level> dimension = level.dimension();
		if (level != lastLevel) {
			WorldIntroModule m = module();
			if (lastDimension != null && dimension != lastDimension && m.isEnabled()
					&& m.play.index() == WorldIntroModule.PLAY_JOIN_AND_DIMENSION) {
				arm(true);
			}
			lastLevel = level;
			lastDimension = dimension;
		}
		if (state == IDLE) {
			return;
		}
		if (!module().isEnabled() || PhotoMode.active() || ModuleManager.get(ShowcaseModule.class).isEnabled()) {
			end(minecraft);
			return;
		}
		if (active()) {
			// Damage, danger or a screen ends it at once.
			float health = player.getHealth();
			boolean hurt = lastHealth >= 0f && health < lastHealth - 0.01f || player.hurtTime > 0;
			lastHealth = health;
			boolean screen = minecraft.gui.screen() != null;
			if (hurt || screen || hostileNear(level, player)) {
				end(minecraft);
			}
		}
	}

	private static boolean hostileNear(ClientLevel level, LocalPlayer player) {
		return !level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(HOSTILE_RANGE),
				mob -> mob instanceof Enemy && mob.isAlive()).isEmpty();
	}

	/** A key, click or scroll: skips the intro (the input itself goes through untouched). */
	public static void onInput() {
		if (active()) {
			end(Minecraft.getInstance());
		}
	}

	// ---- Camera (render thread, end of vanilla's alignment) -------------------------------------

	public static int apply(@Nullable Entity entity, @Nullable Level level, float partialTicks) {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		Minecraft minecraft = Minecraft.getInstance();
		if (state == IDLE || entity == null || level == null || entity != minecraft.player || !(level instanceof ClientLevel client)) {
			return VANILLA;
		}
		Vec3 eye = entity.getEyePosition(partialTicks);
		float eyeYaw = entity.getViewYRot(partialTicks);
		float eyePitch = entity.getViewXRot(partialTicks);
		if (state == ARMED) {
			// Wait for the loading screen to go (however long the world takes)...
			if (minecraft.gui.screen() != null) {
				return VANILLA;
			}
			state = LOADING;
		}
		if (state == LOADING) {
			// ...then, behind black, for the land under the shot to load – as far as the server lets us see.
			waited += dt;
			if (waited > MAX_WAIT) {
				end(minecraft);
				return VANILLA;
			}
			int reach = Math.clamp((minecraft.options.getEffectiveRenderDistance() - 1) * 16, 16, 80);
			if (!loaded(client, eye, reach)) {
				return VANILLA;
			}
			begin(minecraft, client, entity, eye, eyeYaw);
		}
		if (state == PARKED) {
			parked += dt;
			// Parked at the start behind black: the renderer builds what the shot will see first.
			if (parked >= SETTLE || waited + parked > MAX_WAIT) {
				state = PLAYING;
				played = 0f;
			}
		} else if (!minecraft.isPaused()) {
			played += dt;
		}
		float u = state == PLAYING ? Math.min(1f, played / duration) : 0f;
		if (u >= 1f) {
			end(minecraft);
			return VANILLA;
		}
		if (!flight) {
			// No room to fly: your own view, just the title card and the bars.
			cameraX = eye.x;
			cameraY = eye.y;
			cameraZ = eye.z;
			cameraYaw = eyeYaw;
			cameraPitch = eyePitch;
			lensMix = 0f;
			dofAmount = 0f;
			return VANILLA;
		}

		// Along the path, eased in and out.
		float s = Math.min(1f, u / PATH_END);
		s = 0.5f - 0.5f * Mth.cos(s * Mth.PI);
		sample(PATH, s);
		double px = TMP[0];
		double py = TMP[1];
		double pz = TMP[2];
		sample(LOOK, s);
		double dx = TMP[0] - px;
		double dy = TMP[1] - py;
		double dz = TMP[2] - pz;
		float lookYaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
		float lookPitch = (float) (-Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);

		// The hand-over into your eyes.
		float k = smoothstep((u - HAND_OVER) / (1f - HAND_OVER));
		k = k * k * (3f - 2f * k);
		cameraX = px + (eye.x - px) * k;
		cameraY = py + (eye.y - py) * k;
		cameraZ = pz + (eye.z - pz) * k;
		cameraYaw = lookYaw + Mth.wrapDegrees(eyeYaw - lookYaw) * k;
		cameraPitch = lookPitch + (eyePitch - lookPitch) * k;
		lensMix = 1f - k;

		// Focus: far over the land at first, pulled onto you as the camera comes in, sharp again in your eyes.
		double fx = eye.x - cameraX;
		double fy = eye.y - 0.4 - cameraY;
		double fz = eye.z - cameraZ;
		float toYou = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
		float pull = smoothstep((u - 0.3f) / 0.4f);
		focusDistance = Math.max(0.5f, Mth.lerp(pull, Math.max(toYou * 1.6f, 48f), toYou));
		dofAmount = smoothstep(u / 0.12f) * (1f - smoothstep((u - 0.74f) / 0.18f));

		double sep = Math.sqrt(Mth.lengthSquared(cameraX - eye.x, cameraY - eye.y, cameraZ - eye.z));
		boolean outside = sep > HIDE_DISTANCE;
		thirdPerson(minecraft, outside);
		return outside ? SHOT : EYES;
	}

	/** Catmull-Rom through the samples at s (0..1), into {@link #TMP}. */
	private static void sample(double[] path, float s) {
		float f = s * (SAMPLES - 1);
		int i = Math.clamp((int) Math.floor(f), 0, SAMPLES - 2);
		double t = f - i;
		int i0 = Math.max(i - 1, 0);
		int i3 = Math.min(i + 2, SAMPLES - 1);
		for (int c = 0; c < 3; c++) {
			double p0 = path[i0 * 3 + c];
			double p1 = path[i * 3 + c];
			double p2 = path[(i + 1) * 3 + c];
			double p3 = path[i3 * 3 + c];
			TMP[c] = 0.5 * (2 * p1 + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
		}
	}

	private static void thirdPerson(Minecraft minecraft, boolean on) {
		if (on && !switched) {
			previous = minecraft.options.getCameraType();
			minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			switched = true;
		} else if (!on && switched) {
			restoreCamera(minecraft);
		}
	}

	private static void restoreCamera(Minecraft minecraft) {
		if (switched) {
			switched = false;
			if (previous != null) {
				minecraft.options.setCameraType(previous);
			}
			previous = null;
		}
	}

	private static void end(Minecraft minecraft) {
		state = IDLE;
		flight = false;
		dofAmount = 0f;
		lensMix = 0f;
		restoreCamera(minecraft);
	}

	// ---- Planning (render thread, once, behind black) ----------------------------------------------

	private static void begin(Minecraft minecraft, ClientLevel level, Entity entity, Vec3 eye, float eyeYaw) {
		WorldIntroModule m = module();
		duration = m.duration.getFloat();
		flight = plan(level, entity, eye, eyeYaw, m.style.index());
		if (!flight) {
			// The title card over your own view, a little shorter.
			duration = Math.min(duration, 6f);
		}
		fillCard(minecraft, level, entity);
		state = PARKED;
		parked = 0f;
		played = 0f;
		lastHealth = -1f;
		if (flight) {
			// Start the first frame at the path's start, already looking where it looks.
			cameraX = PATH[0];
			cameraY = PATH[1];
			cameraZ = PATH[2];
		}
	}

	/** Whether every chunk within {@code radius} blocks of the eyes is loaded. */
	private static boolean loaded(ClientLevel level, Vec3 eye, int radius) {
		int cx = SectionPos.blockToSectionCoord(Mth.floor(eye.x));
		int cz = SectionPos.blockToSectionCoord(Mth.floor(eye.z));
		int r = (radius + 15) >> 4;
		for (int z = cz - r; z <= cz + r; z++) {
			for (int x = cx - r; x <= cx + r; x++) {
				if (!level.hasChunk(x, z)) {
					return false;
				}
			}
		}
		return true;
	}

	/** Plans a collision-free path for the style; false when no variant fits (indoors, caves). */
	private static boolean plan(ClientLevel level, Entity entity, Vec3 eye, float eyeYaw, int style) {
		float[] ends = {0f, 45f, -45f, 90f, -90f};
		float[] endRadii = {5.5f, 3.6f, 2.2f};
		float[] lifts = {0f, 12f, 26f};
		float[] scales = {1f, 0.7f, 0.45f};
		for (float endRadius : endRadii) {
			for (float end : ends) {
				// The end point must see your eyes, or no variant through it will do.
				if (!endClear(level, eye, eyeYaw + end, endRadius)) {
					continue;
				}
				for (float side : new float[]{1f, -1f}) {
					for (float lift : lifts) {
						for (float scale : scales) {
							if (tryPath(level, eye, eyeYaw, style, side, lift, scale, end, endRadius)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean endClear(ClientLevel level, Vec3 eye, float yaw, float radius) {
		double yr = Math.toRadians(yaw);
		double x = eye.x + Math.sin(yr) * radius;
		double z = eye.z - Math.cos(yr) * radius;
		return clear(level, eye.x, eye.y, eye.z, x, eye.y + 2.2 * radius / 5.5, z);
	}

	private static boolean tryPath(ClientLevel level, Vec3 eye, float eyeYaw, int style, float side, float lift, float scale, float endYaw,
			float endRadius) {
		double yr = Math.toRadians(eyeYaw + endYaw);
		// Behind (the end direction): yaw 0 looks +z, so behind is -look.
		double behind = Math.atan2(-Math.cos(yr), Math.sin(yr));
		double endHeight = 2.2 * endRadius / 5.5;
		for (int i = 0; i < SAMPLES; i++) {
			double s = i / (double) (SAMPLES - 1);
			double theta;
			double r;
			double h;
			double lead;
			switch (style) {
				case WorldIntroModule.STYLE_DESCENT -> {
					theta = behind + Math.toRadians(70) * side * (1 - s);
					r = endRadius + (14 * scale - endRadius) * (1 - smoothstep((float) s));
					h = endHeight + (64 + lift - endHeight) * Math.pow(1 - smoothstep((float) s), 1.35);
					lead = 0;
				}
				case WorldIntroModule.STYLE_DOLLY -> {
					theta = behind + Math.toRadians(18) * side * Math.sin(Math.PI * s);
					r = endRadius + (62 * scale - endRadius) * Math.pow(1 - s, 1.35);
					h = endHeight + (9 + lift - endHeight) * (1 - smoothstep((float) s));
					lead = 6 * scale * (1 - s);
				}
				default -> {
					theta = behind - Math.toRadians(150) * side * (1 - s);
					r = endRadius + (46 * scale - endRadius) * Math.pow(1 - s, 1.6);
					h = endHeight + (30 + lift - endHeight) * Math.pow(1 - smoothstep((float) s), 1.15);
					lead = 14 * scale * Math.pow(Math.max(0, 1 - s / 0.45), 2);
				}
			}
			double x = eye.x + Math.cos(theta) * r;
			double z = eye.z + Math.sin(theta) * r;
			double y = eye.y + h;
			if (!level.hasChunk(SectionPos.blockToSectionCoord(Mth.floor(x)), SectionPos.blockToSectionCoord(Mth.floor(z)))) {
				return false;
			}
			// Clear of the land (more early on, less as it comes in to you).
			if (s < 0.97) {
				double clearance = 4.0 + (1.6 - 4.0) * smoothstep((float) ((s - 0.6) / 0.4));
				int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
				y = Math.max(y, ground + clearance);
			}
			y = Math.min(y, level.getMaxY() - 2.0);
			PATH[i * 3] = x;
			PATH[i * 3 + 1] = y;
			PATH[i * 3 + 2] = z;
			double tx = -Math.sin(theta) * side;
			double tz = Math.cos(theta) * side;
			LOOK[i * 3] = eye.x + tx * lead;
			LOOK[i * 3 + 1] = eye.y - 0.4;
			LOOK[i * 3 + 2] = eye.z + tz * lead;
		}
		// Smooth the lifted heights (never lower than lifted).
		for (int pass = 0; pass < 3; pass++) {
			double before = PATH[1];
			for (int i = 1; i < SAMPLES - 1; i++) {
				double y = PATH[i * 3 + 1];
				double next = PATH[(i + 1) * 3 + 1];
				PATH[i * 3 + 1] = Math.max(y, (before + 2 * y + next) * 0.25);
				before = y;
			}
		}
		for (int i = 0; i < SAMPLES - 1; i++) {
			if (!clear(level, PATH[i * 3], PATH[i * 3 + 1], PATH[i * 3 + 2], PATH[(i + 1) * 3], PATH[(i + 1) * 3 + 1], PATH[(i + 1) * 3 + 2])) {
				return false;
			}
		}
		int last = (SAMPLES - 1) * 3;
		return clear(level, PATH[last], PATH[last + 1], PATH[last + 2], eye.x, eye.y, eye.z);
	}

	/** No block between the two points, with a little room (the camera is never closer than 0.3 to a wall). */
	private static boolean clear(ClientLevel level, double x0, double y0, double z0, double x1, double y1, double z1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		double dz = z1 - z0;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		double k = len > 1.0e-6 ? 0.3 / len : 0.0;
		Vec3 from = new Vec3(x0 - dx * k, y0 - dy * k, z0 - dz * k);
		Vec3 to = new Vec3(x1 + dx * k, y1 + dy * k, z1 + dz * k);
		HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS;
	}

	// ---- What the rest of the client asks ----------------------------------------------------------

	/** The intro owns the screen (behind black while the world loads and the camera parks, or playing). */
	public static boolean active() {
		return state == LOADING || state == PARKED || state == PLAYING;
	}

	public static boolean hudHidden() {
		return active();
	}

	public static double x() {
		return cameraX;
	}

	public static double y() {
		return cameraY;
	}

	public static double z() {
		return cameraZ;
	}

	public static float yaw() {
		return cameraYaw;
	}

	public static float pitch() {
		return cameraPitch;
	}

	/** A cinematic 60° lens out on the path, yours again in your eyes. */
	public static float fov(float vanilla) {
		return active() && flight ? vanilla + (60f - vanilla) * lensMix : vanilla;
	}

	/** Depth of Field drives the focus while the shot plays (when its shaders are there). */
	public static boolean dofActive() {
		return state == PLAYING && flight && dofAmount > 0.002f && ShaderSupport.dof();
	}

	public static float focusDistance() {
		return focusDistance;
	}

	/** How much of the blur shows (0 = sharp, as in your eyes). */
	public static float dofAmount() {
		return dofAmount;
	}

	// ---- Title card and bars (after the HUD) --------------------------------------------------------

	private static void fillCard(Minecraft minecraft, ClientLevel level, Entity entity) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		ServerData data = minecraft.getCurrentServer();
		String world = server != null ? server.getWorldData().getLevelName() : data != null ? data.name : I18n.get("liquidclient.intro.multiplayer");
		ResourceKey<Level> dimension = level.dimension();
		String title;
		String tag;
		if (dimensionChange) {
			title = dimension == Level.NETHER ? I18n.get("liquidclient.intro.nether")
					: dimension == Level.END ? I18n.get("liquidclient.intro.end") : I18n.get("liquidclient.intro.overworld");
			tag = world;
		} else {
			title = world;
			tag = I18n.get(server != null ? "liquidclient.intro.singleplayer" : "liquidclient.intro.multiplayer");
		}
		TAG.setText(tag);
		TITLE.setText(title);
		TITLE_SMALL.setText(title);
		bigTitle = TITLE.width() < 300f;

		factCount = 0;
		BlockPos pos = entity.blockPosition();
		level.getBiome(pos).unwrapKey().ifPresent(key -> {
			String translationKey = "biome." + key.identifier().getNamespace() + "." + key.identifier().getPath();
			String translated = I18n.get(translationKey);
			// I18n.get hands the key back when there's no translation (modded biomes): prettify the id then.
			String name = translated.equals(translationKey) ? pretty(key.identifier().getPath()) : translated;
			addFact(0xE111, name);
		});
		boolean overworld = dimension == Level.OVERWORLD;
		long ticks = clockTicks;
		boolean night = false;
		if (overworld && ticks >= 0L) {
			long inDay = Math.floorMod(ticks, 24000L);
			float hours = (inDay / 1000f + 6f) % 24f;
			int hh = (int) hours;
			int mm = (int) ((hours - hh) * 60f);
			night = hours >= 19.5f || hours < 5f;
			String period = hours < 5f ? "night" : hours < 7f ? "dawn" : hours < 11f ? "morning" : hours < 13f ? "noon" : hours < 17f ? "afternoon"
					: hours < 19.5f ? "evening" : "night";
			addFact(night ? 0xE11E : 0xE087, String.format(Locale.ROOT, "%02d:%02d · %s", hh, mm, I18n.get("liquidclient.intro.time." + period)));
		}
		if (overworld) {
			float rain = level.getRainLevel(1f);
			float thunder = level.getThunderLevel(1f);
			boolean snow = level.getPrecipitationAt(pos) == Biome.Precipitation.SNOW;
			String weather;
			int icon;
			if (Storm.active() && Storm.presence() > 0.3f || thunder > 0.5f) {
				weather = "thunder";
				icon = 0xE08C;
			} else if (rain > 0.3f) {
				weather = snow ? "snow" : "rain";
				icon = 0xE214;
			} else {
				weather = "clear";
				icon = night ? 0xE11E : 0xE178;
			}
			addFact(icon, I18n.get("liquidclient.intro.weather." + weather));
			if (ticks >= 0L) {
				addFact(0xE063, I18n.get("liquidclient.intro.day", Math.floorDiv(ticks, 24000L) + 1));
			}
		}
	}

	private static void addFact(int icon, String text) {
		if (factCount < FACTS) {
			FACT_ICON[factCount] = icon;
			FACT[factCount].setText(text);
			factCount++;
		}
	}

	private static String pretty(String id) {
		StringBuilder out = new StringBuilder(id.length());
		boolean upper = true;
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (c == '_') {
				out.append(' ');
				upper = true;
			} else {
				out.append(upper ? Character.toUpperCase(c) : c);
				upper = false;
			}
		}
		return out.toString();
	}

	/** After the HUD: the black of the wait and the fade-in, the letterbox bars, the title card. */
	public static void extractOverlay(GuiGraphicsExtractor g) {
		if (!active()) {
			return;
		}
		WorldIntroModule m = module();
		float w = g.guiWidth();
		float h = g.guiHeight();
		float u = state == PLAYING ? Math.min(1f, played / duration) : 0f;
		float t = state == PLAYING ? played : 0f;

		float black = state == PLAYING ? 1f - smoothstep(t / FADE_IN) : 1f;
		float bars = m.letterbox.get() ? 1f - smoothstep((u - 0.82f) / 0.16f) : 0f;
		if (bars > 0.001f) {
			float bar = h * 0.105f * bars;
			RenderUtils.rect(g, 0f, 0f, w, bar, 0xFF000000);
			RenderUtils.rect(g, 0f, h - bar, w, bar, 0xFF000000);
		}
		if (m.titleCard.get() && state == PLAYING) {
			float show = smoothstep((t - FADE_IN * 0.6f) / 0.7f) * (1f - smoothstep((u - 0.66f) / 0.1f));
			if (show > 0.004f) {
				card(g, w, h, h * 0.105f * bars, show, t);
			}
		}
		if (black > 0.001f) {
			RenderUtils.rect(g, 0f, 0f, w, h, ColorUtil.fade(0xFF000000, black));
		}
	}

	private static void card(GuiGraphicsExtractor g, float w, float h, float bar, float show, float t) {
		TextLabel title = bigTitle ? TITLE : TITLE_SMALL;
		float titleH = bigTitle ? 22f : 9f;
		float iconSize = 9f;
		float gap = 14f;
		float factsW = 0f;
		for (int i = 0; i < factCount; i++) {
			factsW += iconSize + 4f + FACT[i].width() + (i > 0 ? gap : 0f);
		}
		float padX = 22f;
		float cw = Math.max(Math.max(title.width(), TAG.width()), factsW) + padX * 2f;
		float ch = 16f + 7f + 9f + titleH + 12f + 9f + 16f;
		float x = (w - cw) * 0.5f;
		float y = h - bar - ch - 18f + (1f - show) * 12f;
		float r = 14f;
		RenderUtils.pushAlpha(show);
		RenderUtils.shadow(g, x, y + 4f, cw, ch, r, 18f, 0x66000000);
		RenderUtils.gradient(g, x, y, cw, ch, r, 0x8C0F172A, 0xA60F172A);
		RenderUtils.light(g, x + 8f, y + 2f, cw - 16f, ch * 0.4f, r * 0.7f, 0x22FFFFFF, 0);
		RenderUtils.hairline(g, x, y, cw, ch, r, 0x2EFFFFFF, 0x0DFFFFFF);
		float cx = w * 0.5f;
		float ty = y + 16f;
		LiquidText.drawCentered(g, TAG, cx, ty, 0xB3A5B4FC, false);
		ty += 7f + 9f;
		LiquidText.drawCenteredSoft(g, title, cx, ty, 0xFFF8FAFC);
		ty += titleH + 12f;
		// The facts, one after another.
		float fx = cx - factsW * 0.5f;
		for (int i = 0; i < factCount; i++) {
			float appear = smoothstep((show * 1.4f) - i * 0.12f);
			if (i > 0) {
				fx += gap;
				RenderUtils.fill(g, fx - gap * 0.5f - 1f, ty + 2.5f, 2f, 2f, 1f, ColorUtil.fade(0x66EBEBF5, appear));
			}
			int color = ColorUtil.fade(0xD9E2E8F0, appear);
			IconFont.draw(g, FACT_ICON[i], fx + iconSize * 0.5f, ty + 3.5f, iconSize, ColorUtil.fade(0xFF8FB7FF, appear), 0);
			fx += iconSize + 4f;
			LiquidText.draw(g, FACT[i], fx, ty, color, false);
			fx += FACT[i].width();
		}
		RenderUtils.popAlpha();
	}

	private static float smoothstep(float x) {
		float c = Math.clamp(x, 0f, 1f);
		return c * c * (3f - 2f * c);
	}
}
