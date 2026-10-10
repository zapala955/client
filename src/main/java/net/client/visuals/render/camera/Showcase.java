package net.client.visuals.render.camera;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.player.ShowcaseModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


/**
 * The Showcase camera ({@code ShowcaseModule}).
 *
 * <h2>Motion</h2>
 * One progress value runs 0 → 1 over the transition time when the module turns on and back when it
 * turns off. The camera's position follows it with an ease-out (it leaves your head quickly, then
 * settles into the shot; on the way back it drifts first and slips into your eyes at the end), its
 * rotation and the lens with a smoothstep, so the hand-over in both directions is seamless. While
 * it's out, the shot itself moves – turntable, portrait sway, drone sweeps, dolly – from a focus
 * point that follows the player smoothly. Everything is frame-rate independent.
 *
 * <h2>Framing</h2>
 * The shot looks at the focus (a point up the player's body); the camera is pulled in in front of
 * walls (clipped from the focus, quickly in and slowly back out). Mouse drags orbit with a little
 * inertia and the wheel zooms. Vanilla's camera type is third person only while the camera is more
 * than {@link #HIDE_DISTANCE} from the eyes (your player drawn, no first-person hand); closer, it's
 * first person – so leaving and landing never show the inside of your own head, and the switch back
 * is invisible.
 */
public final class Showcase {
	/**
	 * Closer to the eyes than this the camera is first person (your body hidden, your hand shown):
	 * leaving and landing happen there, so your own head never fills the screen.
	 */
	private static final double HIDE_DISTANCE = 0.85;

	private static float progress;
	/** A showcase is running (from the first frame out until the camera is back in the eyes). */
	private static boolean owning;
	/** Vanilla's camera type is set to third person (and {@link #previous} remembered). */
	private static boolean switched;
	private static @Nullable CameraType previous;
	private static long lastNanos = -1L;
	private static float time;
	private static float startYaw;
	private static boolean focusReady;
	private static double focusX;
	private static double focusY;
	private static double focusZ;
	private static float orbit;
	private static float userYaw;
	private static float userPitch;
	private static float userYawTarget;
	private static float userPitchTarget;
	private static float zoom = 1f;
	private static float zoomTarget = 1f;
	private static float clipped = -1f;
	private static double startX;
	private static double startZ;

	// Out: the camera for this frame.
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static float cameraYaw;
	private static float cameraPitch;
	private static float lensMix;

	// Name card.
	private static final TextLabel TAG = new TextLabel("LIQUID · SHOWCASE", LiquidText.TextStyle.CAPTION).uppercase().tracking(1.2f);
	private static final TextLabel NAME = new TextLabel("", LiquidText.TextStyle.HEADLINE);
	private static final TextLabel PLACE = new TextLabel("", LiquidText.TextStyle.CAPTION);

	private static @Nullable ShowcaseModule module;

	private Showcase() {
	}

	private static ShowcaseModule module() {
		ShowcaseModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(ShowcaseModule.class);
		}
		return m;
	}

	/** Client tick: name card text, exits (world left, walking off with Exit On Move). */
	public static void tick(Minecraft minecraft) {
		ShowcaseModule m = module();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			if (m.isEnabled()) {
				m.setEnabled(false);
			}
			reset(minecraft);
			return;
		}
		if (m.isEnabled() && progress > 0.5f && m.exitOnMove.get()) {
			double dx = player.getX() - startX;
			double dz = player.getZ() - startZ;
			if (dx * dx + dz * dz > 0.25) {
				m.setEnabled(false);
			}
		}
		if (progress > 0f) {
			NAME.setText(player.getName().getString());
			ServerData server = minecraft.getCurrentServer();
			PLACE.setText(server != null ? server.name : "Singleplayer");
		}
	}

	/** {@link #apply} results. */
	public static final int VANILLA = 0;
	/** The camera is out: {@link #x()} … {@link #pitch()}, detached (your player is drawn). */
	public static final int SHOT = 1;
	/** The camera is near the eyes: {@link #x()} … {@link #pitch()}, first person (your hand is drawn). */
	public static final int EYES = 2;

	/**
	 * Camera update (render thread, end of vanilla's alignment): {@link #VANILLA}, {@link #SHOT} or
	 * {@link #EYES}; for the latter two the camera is {@link #x()} … {@link #pitch()}.
	 */
	public static int apply(@Nullable Entity entity, @Nullable Level level, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		ShowcaseModule m = module();
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		if (entity == null || level == null || entity != minecraft.player) {
			return VANILLA;
		}
		boolean on = m.isEnabled();
		if (on && progress == 0f) {
			begin(minecraft, entity, partialTicks);
		}
		if (progress == 0f && !owning) {
			return VANILLA;
		}
		progress = Math.clamp(progress + (on ? dt : -dt) / m.transition.getFloat(), 0f, 1f);
		if (progress == 0f) {
			// Back in the eyes: the last frames were already first person at (nearly) the eyes.
			owning = false;
			finish(minecraft);
			return VANILLA;
		}
		if (!minecraft.isPaused()) {
			time = (time + dt) % 3600f;
		}

		// Focus: up the player's body, followed smoothly.
		Vec3 feet = entity.getPosition(partialTicks);
		double fy = feet.y + entity.getBbHeight() * m.focus.getFloat() / 100f;
		if (!focusReady) {
			focusX = feet.x;
			focusY = fy;
			focusZ = feet.z;
			focusReady = true;
		} else {
			double follow = 1.0 - Math.exp(-9.0 * dt);
			focusX += (feet.x - focusX) * follow;
			focusY += (fy - focusY) * follow;
			focusZ += (feet.z - focusZ) * follow;
		}

		// Mouse orbit and zoom, eased.
		float ease = 1f - (float) Math.exp(-11f * dt);
		userYaw += (userYawTarget - userYaw) * ease;
		userPitch += (userPitchTarget - userPitch) * ease;
		zoom += (zoomTarget - zoom) * (1f - (float) Math.exp(-9f * dt));

		// The shot.
		float direction = m.reverse.get() ? -1f : 1f;
		orbit += m.speed.getFloat() * direction * dt;
		float distance = m.distance.getFloat();
		float height = m.height.getFloat();
		float azimuth;
		float dist;
		float lift;
		switch (m.mode.index()) {
			case ShowcaseModule.PORTRAIT -> {
				azimuth = startYaw + 180f + 22f * Mth.sin(time * 0.25f) + orbit * 0.15f;
				dist = distance * 0.72f;
				lift = height * 0.55f + 0.08f * Mth.sin(time * 0.31f);
			}
			case ShowcaseModule.CINEMATIC -> {
				azimuth = startYaw + 25f + orbit * 0.6f + 28f * Mth.sin(time * 0.11f);
				dist = distance * (1f + 0.25f * Mth.sin(time * 0.17f));
				lift = height + 0.9f * Mth.sin(time * 0.09f + 1f);
			}
			case ShowcaseModule.DOLLY -> {
				azimuth = startYaw + 160f + 15f * Mth.sin(time * 0.13f);
				dist = distance * (0.75f + 0.5f * (0.5f + 0.5f * Mth.sin(time * 0.2f - 1.57f)));
				lift = height * 0.5f;
			}
			default -> {
				azimuth = startYaw + orbit;
				dist = distance;
				lift = height;
			}
		}
		azimuth += userYaw;
		dist *= zoom;
		float pitch = userPitch * Mth.DEG_TO_RAD;
		double horizontal = dist * Math.cos(pitch);
		double vertical = lift + dist * Math.sin(pitch);
		float a = azimuth * Mth.DEG_TO_RAD;
		double wantX = focusX + Math.sin(a) * horizontal;
		double wantY = focusY + vertical;
		double wantZ = focusZ - Math.cos(a) * horizontal;

		// Keep in front of walls: pull in fast, ease back out.
		double reach = clip(level, entity, wantX, wantY, wantZ);
		double full = Math.sqrt(Mth.lengthSquared(wantX - focusX, wantY - focusY, wantZ - focusZ));
		if (clipped < 0f) {
			clipped = (float) reach;
		} else {
			float rate = reach < clipped ? 25f : 2.5f;
			clipped += (float) ((reach - clipped) * (1.0 - Math.exp(-rate * dt)));
		}
		double scale = full > 1.0e-4 ? Math.min(1.0, clipped / full) : 1.0;
		double shotX = focusX + (wantX - focusX) * scale;
		double shotY = focusY + (wantY - focusY) * scale;
		double shotZ = focusZ + (wantZ - focusZ) * scale;

		// Looking at the focus, with a breath of handheld drift.
		double dx = focusX - shotX;
		double dy = focusY - shotY;
		double dz = focusZ - shotZ;
		float lookYaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
		float lookPitch = (float) (-Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
		if (m.handheld.get()) {
			lookYaw += 0.35f * Mth.sin(time * 1.3f) + 0.2f * Mth.sin(time * 2.9f + 1f);
			lookPitch += 0.25f * Mth.sin(time * 1.7f + 2f) + 0.12f * Mth.sin(time * 3.7f);
		}

		// Hand-over between the eyes and the shot.
		Vec3 eye = entity.getEyePosition(partialTicks);
		float eyeYaw = entity.getViewYRot(partialTicks);
		float eyePitch = entity.getViewXRot(partialTicks);
		float travel = easeOut(progress);
		float turn = smoothstep(progress);
		cameraX = eye.x + (shotX - eye.x) * travel;
		cameraY = eye.y + (shotY - eye.y) * travel;
		cameraZ = eye.z + (shotZ - eye.z) * travel;
		cameraYaw = eyeYaw + Mth.wrapDegrees(lookYaw - eyeYaw) * turn;
		cameraPitch = eyePitch + (lookPitch - eyePitch) * turn;
		lensMix = turn;

		// Near the eyes it's first person, further out third person: the switch happens where neither
		// your body nor your hand shows a jump.
		double sep = Math.sqrt(Mth.lengthSquared(cameraX - eye.x, cameraY - eye.y, cameraZ - eye.z));
		boolean outside = sep > HIDE_DISTANCE;
		thirdPerson(minecraft, outside);
		return outside ? SHOT : EYES;
	}

	/** Vanilla's camera type: third person while the camera is out, back to what it was near the eyes. */
	private static void thirdPerson(Minecraft minecraft, boolean on) {
		if (on && !switched) {
			previous = minecraft.options.getCameraType();
			minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			switched = true;
		} else if (!on) {
			finish(minecraft);
		}
	}

	private static void begin(Minecraft minecraft, Entity entity, float partialTicks) {
		owning = true;
		startYaw = entity.getViewYRot(partialTicks);
		startX = entity.getX();
		startZ = entity.getZ();
		focusReady = false;
		orbit = 0f;
		userYaw = 0f;
		userPitch = 0f;
		userYawTarget = 0f;
		userPitchTarget = 0f;
		zoom = 1f;
		zoomTarget = 1f;
		clipped = -1f;
		time = 0f;
	}

	private static void finish(Minecraft minecraft) {
		if (switched) {
			switched = false;
			if (previous != null) {
				minecraft.options.setCameraType(previous);
			}
			previous = null;
		}
	}

	private static void reset(Minecraft minecraft) {
		progress = 0f;
		owning = false;
		finish(minecraft);
		lastNanos = -1L;
	}

	/** Distance from the focus to the first wall towards the camera (minus a margin). */
	private static double clip(Level level, Entity entity, double x, double y, double z) {
		Vec3 from = new Vec3(focusX, focusY, focusZ);
		Vec3 to = new Vec3(x, y, z);
		HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
		double full = from.distanceTo(to);
		if (hit.getType() == HitResult.Type.MISS) {
			return full;
		}
		return Math.max(0.35, hit.getLocation().distanceTo(from) - 0.25);
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

	/** The showcase lens blended in. */
	public static float fov(float vanilla) {
		return progress > 0f ? vanilla + (module().lens.getFloat() - vanilla) * lensMix : vanilla;
	}

	/** Mouse look while out: orbits the shot instead of turning the player. True when consumed. */
	public static boolean mouse(double dx, double dy) {
		if (progress <= 0f || !module().isEnabled()) {
			return false;
		}
		if (module().mouseOrbit.get()) {
			userYawTarget += (float) dx * 0.15f;
			userPitchTarget = Math.clamp(userPitchTarget + (float) dy * 0.15f, -55f, 70f);
		}
		return true;
	}

	/** Scroll while out: zoom. True when consumed. */
	public static boolean scroll(double amount) {
		if (progress <= 0f || !module().isEnabled()) {
			return false;
		}
		zoomTarget = Math.clamp(zoomTarget * (1f - (float) amount * 0.12f), 0.45f, 2.6f);
		return true;
	}

	/** Whether the HUD (vanilla's and ours) is hidden. */
	public static boolean hudHidden() {
		return progress > 0.02f && module().hideHud.get();
	}

	/** After the HUD: letterbox bars and the name card. */
	public static void extractOverlay(GuiGraphicsExtractor g) {
		if (progress <= 0f) {
			return;
		}
		ShowcaseModule m = module();
		Minecraft minecraft = Minecraft.getInstance();
		float w = minecraft.getWindow().getGuiScaledWidth();
		float h = minecraft.getWindow().getGuiScaledHeight();
		float shown = smoothstep(progress);
		float bar = 0f;
		if (m.letterbox.get()) {
			bar = h * 0.105f * shown;
			RenderUtils.rect(g, 0f, 0f, w, bar, ColorUtil.fade(0xFF000000, Math.min(1f, shown * 1.4f)));
			RenderUtils.rect(g, 0f, h - bar, w, bar, ColorUtil.fade(0xFF000000, Math.min(1f, shown * 1.4f)));
		}
		LocalPlayer player = minecraft.player;
		if (!m.nameCard.get() || player == null) {
			return;
		}
		float card = smoothstep(Math.clamp((progress - 0.55f) / 0.45f, 0f, 1f));
		if (card <= 0.01f) {
			return;
		}
		float face = 26f;
		float pad = 9f;
		float textW = Math.max(Math.max(NAME.width(), PLACE.width()), TAG.width());
		float cw = pad + face + 10f + textW + pad + 4f;
		float ch = face + pad * 2f;
		float x = 18f;
		float y = h - Math.max(bar, 0f) - ch - 16f + (1f - card) * 10f;
		float r = 13f;
		RenderUtils.pushAlpha(card);
		RenderUtils.shadow(g, x, y + 3f, cw, ch, r, 14f, 0x55000000);
		RenderUtils.gradient(g, x, y, cw, ch, r, 0x7A1A2236, 0x8A0F172A);
		RenderUtils.light(g, x + 6f, y + 2f, cw - 12f, ch * 0.42f, r * 0.7f, 0x24FFFFFF, 0);
		RenderUtils.hairline(g, x, y, cw, ch, r, 0x2EFFFFFF, 0x0DFFFFFF);
		float fx = x + pad;
		float fy = y + pad;
		RenderUtils.fill(g, fx - 1f, fy - 1f, face + 2f, face + 2f, 7f, 0x40000000);
		PlayerFaceExtractor.extractRenderState(g, player.getSkin(), Math.round(fx), Math.round(fy), Math.round(face),
				ColorUtil.fade(0xFFFFFFFF, RenderUtils.alpha()));
		float tx = fx + face + 10f;
		LiquidText.draw(g, TAG, tx, fy + 1f, 0x99A5B4FC, false);
		LiquidText.drawSoft(g, NAME, tx, fy + 9f, 0xFFF8FAFC);
		LiquidText.draw(g, PLACE, tx, fy + 20.5f, 0xB3CBD5E1, false);
		RenderUtils.popAlpha();
	}

	private static float easeOut(float t) {
		float u = 1f - t;
		return 1f - u * u * u;
	}

	private static float smoothstep(float t) {
		float c = Math.clamp(t, 0f, 1f);
		return c * c * (3f - 2f * c);
	}
}
