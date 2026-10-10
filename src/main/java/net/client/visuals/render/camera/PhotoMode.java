package net.client.visuals.render.camera;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.photo.PhotoStudioScreen;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.module.player.ShowcaseModule;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.post.PhotoCapture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * Photo Mode ({@code PhotoModeModule}): a camera in Minecraft.
 *
 * <h2>The camera</h2>
 * It starts in your eyes and floats free like a drone on a gimbal: WASD in the direction you look,
 * Space / Sneak up and down, Sprint for speed, Alt for precision; velocity and rotation ease with the
 * Smoothing setting, so moves start and stop softly and pans glide. Q / E roll it (a Dutch angle,
 * applied in the projection), the wheel zooms the lens (12–200 mm, eased, mouse sensitivity follows),
 * and it never passes through blocks or strays further than the range from your character. Your
 * character stands still – its movement input is held – and in singleplayer the world can freeze.
 *
 * <h2>Everything else</h2>
 * The lens drives the field of view and Depth of Field (focus at the centre, a point you click, or a
 * manual distance), exposure and filters drive Color Grading, the time and weather override Ambience,
 * and {@link PhotoCapture} takes the photo: frames averaged with sub-pixel jitter once the camera has
 * settled. The viewfinder ({@code gui.photo.PhotoOverlay}) and the studio ({@link PhotoStudioScreen})
 * read their state from here. Frame-rate independent throughout; nothing here allocates per frame
 * except the collision rays while the camera moves.
 */
public final class PhotoMode {
	/** Lens presets: number keys 1–6 and the viewfinder's chips (35 mm-equivalent focal lengths). */
	public static final int[] LENSES = {14, 24, 35, 50, 85, 135};
	/** How far the camera may leave your character on a server (singleplayer: the Range setting). */
	private static final double MULTIPLAYER_RANGE = 12.0;
	/** The camera keeps this far from surfaces. */
	private static final double MARGIN = 0.2;
	/** Closer to your eyes than this, your own model is hidden (no inside of your head on the first frames). */
	private static final double SELF_HIDE = 0.85;
	/** Full-frame sensor height (mm): focal length → field of view, the way a real lens does it. */
	private static final float SENSOR = 24f;

	private static @Nullable PhotoModeModule module;
	private static boolean active;
	private static boolean pendingBegin;
	private static @Nullable CameraType previousType;
	/** The world was frozen by us: game sounds were paused and must be resumed when it runs again. */
	private static boolean froze;
	private static long lastNanos = -1L;
	private static long enteredNanos;

	// The camera.
	private static double x;
	private static double y;
	private static double z;
	private static double vx;
	private static double vy;
	private static double vz;
	private static float yaw;
	private static float pitch;
	private static float roll;
	private static float yawTarget;
	private static float pitchTarget;
	private static float rollTarget;
	private static float focal = 24f;
	private static double eyeX;
	private static double eyeY;
	private static double eyeZ;
	private static boolean settled;

	// Focus (GUI-normalised: x right, y down).
	private static float pointX = 0.5f;
	private static float pointY = 0.5f;
	private static long focusNanos = -1L;
	private static float focusDistance = -1f;

	// Viewfinder.
	private static boolean uiHidden;

	private PhotoMode() {
	}

	public static PhotoModeModule module() {
		PhotoModeModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(PhotoModeModule.class);
		}
		return m;
	}

	// ---- Lifecycle ------------------------------------------------------------------------------

	/** The module was switched on: the camera starts on the next frame (where the view is known). */
	public static void onModuleEnabled() {
		pendingBegin = true;
	}

	public static void onModuleDisabled() {
		pendingBegin = false;
		end(Minecraft.getInstance());
	}

	public static boolean active() {
		return active;
	}

	private static void begin(Minecraft minecraft, Entity entity, float partialTicks) {
		pendingBegin = false;
		active = true;
		Vec3 eye = entity.getEyePosition(partialTicks);
		x = eyeX = eye.x;
		y = eyeY = eye.y;
		z = eyeZ = eye.z;
		vx = vy = vz = 0.0;
		yaw = yawTarget = entity.getViewYRot(partialTicks);
		pitch = pitchTarget = entity.getViewXRot(partialTicks);
		roll = rollTarget = 0f;
		focal = module().focal.getFloat();
		pointX = 0.5f;
		pointY = 0.5f;
		focusNanos = -1L;
		uiHidden = false;
		enteredNanos = System.nanoTime();
		lastNanos = -1L;
		previousType = minecraft.options.getCameraType();
		minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		ShowcaseModule showcase = ModuleManager.get(ShowcaseModule.class);
		if (showcase.isEnabled()) {
			showcase.setEnabled(false);
		}
		UiSounds.open();
	}

	private static void end(Minecraft minecraft) {
		if (!active) {
			return;
		}
		active = false;
		PhotoCapture.cancel();
		// GPU resources go on the render thread between frames.
		minecraft.execute(PhotoCapture::releaseTextures);
		if (previousType != null) {
			minecraft.options.setCameraType(previousType);
			previousType = null;
		}
		if (minecraft.gui.screen() instanceof PhotoStudioScreen) {
			minecraft.gui.setScreen(null);
		}
		resumeSounds(minecraft);
		UiSounds.close();
	}

	/** Leaves Photo Mode (Esc, its key, the studio's Done). */
	public static void exit() {
		PhotoModeModule m = module();
		if (m.isEnabled()) {
			m.setEnabled(false);
		} else {
			end(Minecraft.getInstance());
		}
	}

	/** Client tick: leave with the world, keep frozen state and sounds in step, estimate the focus distance. */
	public static void tick(Minecraft minecraft) {
		PhotoModeModule m = module();
		if (minecraft.player == null || minecraft.level == null) {
			if (m.isEnabled()) {
				m.setEnabled(false);
			}
			pendingBegin = false;
			return;
		}
		if (!active) {
			return;
		}
		boolean frozen = freezesWorld();
		if (froze && !frozen) {
			resumeSounds(minecraft);
		}
		froze = frozen;
		focusDistance = estimateFocus(minecraft.level, minecraft.player);
	}

	private static void resumeSounds(Minecraft minecraft) {
		if (froze) {
			froze = false;
			minecraft.getSoundManager().resume();
		}
	}

	// ---- The camera (render thread, every frame) -----------------------------------------------------

	/** End of vanilla's camera alignment: true when Photo Mode owns the camera this frame. */
	public static boolean apply(@Nullable Entity entity, @Nullable Level level, float partialTicks) {
		if (!active && !pendingBegin || entity == null || level == null) {
			return false;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (pendingBegin) {
			begin(minecraft, entity, partialTicks);
		}
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		Vec3 eye = entity.getEyePosition(partialTicks);
		eyeX = eye.x;
		eyeY = eye.y;
		eyeZ = eye.z;

		PhotoModeModule m = module();
		boolean input = minecraft.gui.screen() == null && !PhotoCapture.holdsCamera();
		Options options = minecraft.options;
		float smooth = m.smoothing.getFloat() / 100f;

		// Rotation and roll, eased like a gimbal.
		if (input) {
			float rollInput = (InputConstants.isKeyDown(InputConstants.KEY_E) ? 1f : 0f) - (InputConstants.isKeyDown(InputConstants.KEY_Q) ? 1f : 0f);
			rollTarget = Math.clamp(rollTarget + rollInput * 40f * dt * precision(), -45f, 45f);
		}
		float turn = 1f - (float) Math.exp(-Mth.lerp(smooth, 32f, 7f) * dt);
		yaw += Mth.wrapDegrees(yawTarget - yaw) * turn;
		pitch += (pitchTarget - pitch) * turn;
		roll += (rollTarget - roll) * turn;
		float zoom = 1f - (float) Math.exp(-11f * dt);
		focal = (float) Math.exp(Math.log(focal) + (Math.log(m.focal.getFloat()) - Math.log(focal)) * zoom);

		// Movement: where you look, Space / Sneak straight up and down.
		double tx = 0.0;
		double ty = 0.0;
		double tz = 0.0;
		if (input) {
			float forward = (options.keyUp.isDown() ? 1f : 0f) - (options.keyDown.isDown() ? 1f : 0f);
			float strafe = (options.keyRight.isDown() ? 1f : 0f) - (options.keyLeft.isDown() ? 1f : 0f);
			float lift = (options.keyJump.isDown() ? 1f : 0f) - (options.keyShift.isDown() ? 1f : 0f);
			float yr = yaw * Mth.DEG_TO_RAD;
			float pr = pitch * Mth.DEG_TO_RAD;
			double fx = -Mth.sin(yr) * Mth.cos(pr);
			double fy = -Mth.sin(pr);
			double fz = Mth.cos(yr) * Mth.cos(pr);
			double rx = -Mth.cos(yr);
			double rz = -Mth.sin(yr);
			tx = fx * forward + rx * strafe;
			ty = fy * forward + lift;
			tz = fz * forward + rz * strafe;
			double len = Math.sqrt(tx * tx + ty * ty + tz * tz);
			if (len > 1.0) {
				tx /= len;
				ty /= len;
				tz /= len;
			}
			double speed = m.speed.get() * (options.keySprint.isDown() ? 3.0 : 1.0) * precision();
			tx *= speed;
			ty *= speed;
			tz *= speed;
		}
		double accel = 1.0 - Math.exp(-Mth.lerp(smooth, 20f, 3.2f) * dt);
		vx += (tx - vx) * accel;
		vy += (ty - vy) * accel;
		vz += (tz - vz) * accel;
		if (vx * vx + vy * vy + vz * vz > 1.0e-8) {
			move(level, entity, vx * dt, vy * dt, vz * dt);
		}
		keepInRange(minecraft);
		settled = vx * vx + vy * vy + vz * vz < 1.0e-4 && Math.abs(Mth.wrapDegrees(yawTarget - yaw)) < 0.02f
				&& Math.abs(pitchTarget - pitch) < 0.02f && Math.abs(rollTarget - roll) < 0.02f
				&& Math.abs(focal - m.focal.getFloat()) < 0.05f;
		return true;
	}

	/** Alt slows everything down for precise framing. */
	private static float precision() {
		return InputConstants.isKeyDown(InputConstants.KEY_LALT) ? 0.25f : 1f;
	}

	/** One axis at a time, so the camera slides along walls instead of sticking. */
	private static void move(Level level, Entity entity, double dx, double dy, double dz) {
		if (dx != 0.0 && free(level, x, y, z, x + dx, y, z, dx, 0.0, 0.0)) {
			x += dx;
		} else {
			vx = 0.0;
		}
		if (dy != 0.0 && free(level, x, y, z, x, y + dy, z, 0.0, dy, 0.0)) {
			y += dy;
		} else {
			vy = 0.0;
		}
		if (dz != 0.0 && free(level, x, y, z, x, y, z + dz, 0.0, 0.0, dz)) {
			z += dz;
		} else {
			vz = 0.0;
		}
	}

	private static boolean free(Level level, double x0, double y0, double z0, double x1, double y1, double z1, double dx, double dy, double dz) {
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		Vec3 from = new Vec3(x0, y0, z0);
		Vec3 to = new Vec3(x1 + dx / len * MARGIN, y1 + dy / len * MARGIN, z1 + dz / len * MARGIN);
		HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS;
	}

	private static void keepInRange(Minecraft minecraft) {
		double range = singleplayer(minecraft) ? module().range.get() : Math.min(MULTIPLAYER_RANGE, module().range.get());
		double dx = x - eyeX;
		double dy = y - eyeY;
		double dz = z - eyeZ;
		double d2 = dx * dx + dy * dy + dz * dz;
		if (d2 > range * range) {
			double k = range / Math.sqrt(d2);
			x = eyeX + dx * k;
			y = eyeY + dy * k;
			z = eyeZ + dz * k;
		}
	}

	private static boolean singleplayer(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		return minecraft.hasSingleplayerServer() && server != null && !server.isPublished();
	}

	public static double x() {
		return x;
	}

	public static double y() {
		return y;
	}

	public static double z() {
		return z;
	}

	public static float yaw() {
		return yaw;
	}

	public static float pitch() {
		return pitch;
	}

	/** The camera's roll (degrees, positive = tilted right). */
	public static float roll() {
		return roll;
	}

	/** Whether the camera has come to rest (the shutter waits for it, so photos are never smeared). */
	public static boolean settled() {
		return settled;
	}

	/** The lens now (eased), in millimetres. */
	public static float focal() {
		return focal;
	}

	/** Vertical field of view of a lens on a full-frame sensor, degrees. */
	public static float fovOf(float focalMm) {
		return (float) (2.0 * Math.atan(SENSOR * 0.5 / focalMm) * Mth.RAD_TO_DEG);
	}

	/** The field of view (replaces vanilla's, zoom and Showcase included). */
	public static float fov() {
		return fovOf(focal);
	}

	/** Roll and capture jitter, applied to this frame's level projection in place. */
	public static void modifyProjection(Matrix4f projection, int width, int height) {
		if (!active) {
			return;
		}
		if (Math.abs(roll) > 0.001f) {
			// The camera tilts right (clockwise), so the picture turns the other way.
			projection.rotateZ(roll * Mth.DEG_TO_RAD);
		}
		float jx = PhotoCapture.jitterX();
		float jy = PhotoCapture.jitterY();
		if (jx != 0f || jy != 0f) {
			projection.translateLocal(jx * 2f / Math.max(1, width), jy * 2f / Math.max(1, height), 0f);
		}
	}

	/** Rolled, the screen's corners reach outside the unrolled view: culling uses a wider one. */
	public static float cullingFovScale() {
		if (!active || Math.abs(roll) < 0.01f) {
			return 1f;
		}
		Minecraft minecraft = Minecraft.getInstance();
		float aspect = minecraft.getWindow().getWidth() / (float) Math.max(1, minecraft.getWindow().getHeight());
		float r = Math.abs(roll) * Mth.DEG_TO_RAD;
		return Math.min(1.8f, Mth.cos(r) + aspect * Mth.sin(r));
	}

	// ---- What the rest of the client asks ---------------------------------------------------------

	/** The HUD (vanilla's and ours) steps aside; the viewfinder replaces it. */
	public static boolean hudHidden() {
		return active;
	}

	/** Your own model: hidden on request, and while the camera is still in your head. */
	public static boolean hidesPlayer() {
		if (!active) {
			return false;
		}
		if (module().hidePlayer.get()) {
			return true;
		}
		double dx = x - eyeX;
		double dy = y - eyeY;
		double dz = z - eyeZ;
		return dx * dx + dy * dy + dz * dz < SELF_HIDE * SELF_HIDE;
	}

	/** Gameplay keys, clicks, the hotbar wheel and your character's movement are held. */
	public static boolean blocksGameplay() {
		return active;
	}

	/** Singleplayer with Freeze on: the game pauses (as under the pause menu, without one). */
	public static boolean freezesWorld() {
		return active && module().freeze.get() && singleplayer(Minecraft.getInstance());
	}

	/** Whether the world can be frozen here at all (singleplayer, not opened to LAN). */
	public static boolean canFreeze() {
		return singleplayer(Minecraft.getInstance());
	}

	/** Clock ticks into the day to show, or -1 (Ambience's override or the server's time). */
	public static long dayTicks() {
		PhotoModeModule m = module();
		if (!active || !m.customTime.get()) {
			return -1L;
		}
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && level.dimension() == Level.END) {
			return -1L;
		}
		return Math.floorMod(Math.round((m.time.get() - 6.0) * 1000.0), 24000L);
	}

	/** Rain level to render, or -1 to leave it. */
	public static float rain() {
		if (!active) {
			return -1f;
		}
		return switch (module().weather.index()) {
			case PhotoModeModule.WEATHER_CLEAR -> 0f;
			case PhotoModeModule.WEATHER_RAIN, PhotoModeModule.WEATHER_SNOW, PhotoModeModule.WEATHER_THUNDER -> 1f;
			default -> -1f;
		};
	}

	/** Thunder level to render, or -1 to leave it. */
	public static float thunder() {
		if (!active || module().weather.index() == PhotoModeModule.WEATHER_AS_IS) {
			return -1f;
		}
		return module().weather.index() == PhotoModeModule.WEATHER_THUNDER ? 1f : 0f;
	}

	/** What falls, or null to follow the biome (or Ambience). */
	public static Biome.@Nullable Precipitation precipitation() {
		if (!active) {
			return null;
		}
		return switch (module().weather.index()) {
			case PhotoModeModule.WEATHER_RAIN -> Biome.Precipitation.RAIN;
			case PhotoModeModule.WEATHER_SNOW -> Biome.Precipitation.SNOW;
			default -> null;
		};
	}

	/** Depth of Field is driven by the camera. */
	public static boolean depthOfField() {
		return active && module().depth.get();
	}

	/** The focus point in uv (x right, y up), where autofocus looks. */
	public static float focusU() {
		return module().focusMode.index() == PhotoModeModule.FOCUS_POINT ? pointX : 0.5f;
	}

	public static float focusV() {
		return 1f - (module().focusMode.index() == PhotoModeModule.FOCUS_POINT ? pointY : 0.5f);
	}

	/** The focus point on screen (GUI-normalised, y down). */
	public static float pointX() {
		return module().focusMode.index() == PhotoModeModule.FOCUS_POINT ? pointX : 0.5f;
	}

	public static float pointY() {
		return module().focusMode.index() == PhotoModeModule.FOCUS_POINT ? pointY : 0.5f;
	}

	/** Seconds since focus was last set (the reticle's animation), or a large number. */
	public static float sinceFocus() {
		return focusNanos < 0L ? 100f : (System.nanoTime() - focusNanos) * 1.0e-9f;
	}

	/** Seconds since Photo Mode opened (the key hints fade). */
	public static float sinceEntered() {
		return (System.nanoTime() - enteredNanos) * 1.0e-9f;
	}

	/** Distance to what the focus point is on, in blocks (-1 = sky or unknown). */
	public static float focusDistanceEstimate() {
		return focusDistance;
	}

	public static boolean uiHidden() {
		return uiHidden;
	}

	/** Focus on a point of the screen (GUI-normalised); manual focus switches to point focus. */
	public static void focusAt(float gx, float gy) {
		PhotoModeModule m = module();
		pointX = Math.clamp(gx, 0.02f, 0.98f);
		pointY = Math.clamp(gy, 0.02f, 0.98f);
		if (m.focusMode.index() != PhotoModeModule.FOCUS_POINT) {
			m.focusMode.set(PhotoModeModule.FOCUS_POINT);
		}
		if (!m.depth.get()) {
			m.depth.set(true);
		}
		focusNanos = System.nanoTime();
		if (m.sounds.get()) {
			UiSounds.focus();
		}
	}

	/** Press the shutter (it fires once the camera has come to rest; {@link PhotoCapture} plays it). */
	public static void shoot() {
		if (!active || PhotoCapture.busy()) {
			return;
		}
		PhotoModeModule m = module();
		PhotoCapture.start(PhotoModeModule.SAMPLES[m.quality.index()], PhotoModeModule.ASPECT_RATIOS[m.aspect.index()]);
	}

	/** Lens preset or a nudge (wheel: one notch ≈ 12 %). */
	public static void setFocal(float mm) {
		module().focal.set(Math.clamp(mm, 12f, 200f));
	}

	public static void resetFraming() {
		rollTarget = 0f;
		pointX = 0.5f;
		pointY = 0.5f;
	}

	// ---- Input --------------------------------------------------------------------------------

	/** Mouse look while active and no screen is open: true when consumed. */
	public static boolean mouse(double dx, double dy) {
		if (!active) {
			return false;
		}
		if (PhotoCapture.holdsCamera()) {
			return true;
		}
		// A longer lens turns slower, like a heavy telephoto on a gimbal.
		double scale = Math.tan(fov() * 0.5 * Mth.DEG_TO_RAD) / Math.tan(fovOf(24f) * 0.5 * Mth.DEG_TO_RAD) * precision();
		yawTarget += (float) (dx * 0.15 * scale);
		pitchTarget = Math.clamp(pitchTarget + (float) (dy * 0.15 * scale), -90f, 90f);
		return true;
	}

	/** The wheel: zoom; with Ctrl exposure, with Alt the aperture. */
	public static boolean scroll(double amount) {
		if (!active) {
			return false;
		}
		PhotoModeModule m = module();
		if (InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)) {
			m.exposure.set(m.exposure.get() + Math.signum(amount) * 0.1);
		} else if (InputConstants.isKeyDown(InputConstants.KEY_LALT)) {
			stepAperture(amount > 0 ? -1 : 1);
		} else {
			setFocal((float) (m.focal.get() * Math.pow(1.12, amount)));
		}
		return true;
	}

	/** One third of a stop wider (−1) or narrower (+1). */
	private static void stepAperture(int direction) {
		PhotoModeModule m = module();
		double stops = 2.0 * Math.log(m.aperture.get()) / Math.log(2.0);
		double next = Math.round(stops * 3.0 + direction) / 3.0;
		m.aperture.set(Math.pow(2.0, next / 2.0));
	}

	/**
	 * A key while active and no screen is open (press or repeat). True when consumed – vanilla never
	 * sees it. Movement keys are left alone (they're read as held keys).
	 */
	public static boolean onKey(KeyEvent event, int action) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!active || action == 0 || minecraft.gui.screen() != null) {
			return false;
		}
		int key = event.key();
		boolean press = action == 1;
		if (event.isEscape() || LiquidClient.PHOTO_MODE.matches(event)) {
			if (press) {
				exit();
			}
			return true;
		}
		if (minecraft.options.keyScreenshot.matches(event) || key == InputConstants.KEY_RETURN) {
			if (press) {
				shoot();
			}
			return true;
		}
		PhotoModeModule m = module();
		switch (key) {
			case InputConstants.KEY_TAB -> {
				if (press) {
					minecraft.gui.setScreen(new PhotoStudioScreen());
				}
			}
			case InputConstants.KEY_H -> {
				if (press) {
					uiHidden = !uiHidden;
					UiSounds.click();
				}
			}
			case InputConstants.KEY_G -> {
				if (press) {
					m.grid.set((m.grid.index() + 1) % PhotoModeModule.GRID.length);
					UiSounds.click();
				}
			}
			case InputConstants.KEY_R -> {
				if (press) {
					resetFraming();
					UiSounds.click();
				}
			}
			case InputConstants.KEY_F -> {
				if (press) {
					m.focusMode.set((m.focusMode.index() + 1) % PhotoModeModule.FOCUS.length);
					focusNanos = System.nanoTime();
					UiSounds.click();
				}
			}
			case InputConstants.KEY_T -> {
				if (press && canFreeze()) {
					m.freeze.set(!m.freeze.get());
					UiSounds.toggle(m.freeze.get());
				}
			}
			case InputConstants.KEY_LBRACKET -> stepAperture(-1);
			case InputConstants.KEY_RBRACKET -> stepAperture(1);
			case InputConstants.KEY_MINUS -> m.exposure.set(m.exposure.get() - 0.1);
			case InputConstants.KEY_EQUALS -> m.exposure.set(m.exposure.get() + 0.1);
			default -> {
				int lens = key - InputConstants.KEY_1;
				if (lens < 0 || lens >= LENSES.length) {
					return false;
				}
				if (press) {
					setFocal(LENSES[lens]);
					UiSounds.tick(lens / (LENSES.length - 1.0));
				}
			}
		}
		return true;
	}

	/**
	 * A mouse button while active, no screen open and the mouse captured: left focuses on what the
	 * crosshair is on, right takes the photo, middle levels the camera. True when consumed.
	 */
	public static boolean onMouseButton(int button, boolean pressed) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!active || minecraft.gui.screen() != null || !minecraft.mouseHandler.isMouseGrabbed()) {
			return false;
		}
		if (pressed) {
			if (button == InputConstants.MOUSE_BUTTON_LEFT) {
				focusAt(0.5f, 0.5f);
			} else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
				shoot();
			} else if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
				resetFraming();
				UiSounds.click();
			}
		}
		return true;
	}

	// ---- Focus distance -------------------------------------------------------------------------

	/** What's under the focus point, through the lens (roll included): for the reticle's distance. */
	private static float estimateFocus(Level level, Entity entity) {
		Minecraft minecraft = Minecraft.getInstance();
		float vfov = fov() * Mth.DEG_TO_RAD;
		float aspect = minecraft.getWindow().getWidth() / (float) Math.max(1, minecraft.getWindow().getHeight());
		float tanV = (float) Math.tan(vfov * 0.5f);
		float sx = (pointX() * 2f - 1f) * tanV * aspect;
		float sy = (1f - pointY() * 2f) * tanV;
		// Undo the roll: the projection turns the picture by +roll, so the ray is the screen point turned back.
		float r = roll * Mth.DEG_TO_RAD;
		float cx = sx * Mth.cos(r) + sy * Mth.sin(r);
		float cy = -sx * Mth.sin(r) + sy * Mth.cos(r);
		float yr = yaw * Mth.DEG_TO_RAD;
		float pr = pitch * Mth.DEG_TO_RAD;
		double fx = -Mth.sin(yr) * Mth.cos(pr);
		double fy = -Mth.sin(pr);
		double fz = Mth.cos(yr) * Mth.cos(pr);
		double rx = -Mth.cos(yr);
		double rz = -Mth.sin(yr);
		// up = right × forward
		double ux = -fy * rz;
		double uy = rz * fx - rx * fz;
		double uz = rx * fy;
		double dx = fx + rx * cx + ux * cy;
		double dy = fy + uy * cy;
		double dz = fz + rz * cx + uz * cy;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		double reach = 256.0;
		Vec3 from = new Vec3(x, y, z);
		Vec3 to = new Vec3(x + dx / len * reach, y + dy / len * reach, z + dz / len * reach);
		HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? -1f : (float) hit.getLocation().distanceTo(from);
	}
}
