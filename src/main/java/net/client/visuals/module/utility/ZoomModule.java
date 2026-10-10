package net.client.visuals.module.utility;

import net.client.visuals.LiquidClient;
import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.Minecraft;

/**
 * Zoom: hold the zoom key (C by default) to narrow the field of view with a smooth, frame-rate
 * independent ease; scroll while zooming to zoom further. Mouse sensitivity follows the zoom so
 * aiming feels the same at every level, and the cinematic camera can optionally kick in.
 *
 * <p>Hooks: {@code CameraZoomMixin} (divides the FOV the camera computes each frame – it also
 * advances the animation, since that's called exactly once per frame), {@code MouseHandlerZoomMixin}
 * (sensitivity, smooth camera, scroll).</p>
 */
public final class ZoomModule extends Module {
	private static final float SCROLL_STEP = 1.18f;
	private static final float MAX_ZOOM = 50f;

	private static ZoomModule instance;

	public final NumberSetting factor = add(new NumberSetting("factor", "liquidclient.zoom.factor", 1.5, 12, 0.5, 4).suffix("×"));
	public final NumberSetting speed = add(new NumberSetting("speed", "liquidclient.zoom.speed", 4, 30, 1, 14));
	public final BooleanSetting scroll = add(new BooleanSetting("scroll", "liquidclient.zoom.scroll", true));
	public final BooleanSetting sensitivity = add(new BooleanSetting("sensitivity", "liquidclient.zoom.sensitivity", true));
	public final BooleanSetting smoothCamera = add(new BooleanSetting("smooth_camera", "liquidclient.zoom.smooth_camera", false));

	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth zoom = new Smooth(1f, 14f);
	private float scrollFactor = 1f;
	private boolean held;

	public ZoomModule() {
		super("zoom", Category.UTILITY, 0xE1B6);
		instance = this;
		setEnabled(true);
	}

	@Override
	public boolean listed() {
		return false; // always-on utility; listing it would only add noise
	}

	private static boolean active() {
		return instance != null && instance.isEnabled();
	}

	/** Called once per frame with the camera's FOV; returns the zoomed FOV. */
	public static float fov(float original) {
		ZoomModule m = instance;
		if (m == null) {
			return original;
		}
		float dt = m.timer.tick();
		Minecraft minecraft = Minecraft.getInstance();
		boolean down = m.isEnabled() && minecraft.gui.screen() == null && LiquidClient.ZOOM.isDown();
		if (!down) {
			m.scrollFactor = 1f;
		}
		m.held = down;
		float target = down ? Math.min(MAX_ZOOM, (float) m.factor.get() * m.scrollFactor) : 1f;
		m.zoom.speed(Motion.speed((float) m.speed.get()));
		float z = m.zoom.target(target).update(dt);
		return z <= 1.0005f ? original : original / z;
	}

	/** The current zoom factor as eased (1 = not zoomed); the Dynamic Island shows it. */
	public static float level() {
		ZoomModule m = instance;
		return m != null && m.isEnabled() ? m.zoom.get() : 1f;
	}

	/** Mouse turn multiplier (1 when not zoomed). */
	public static double turnScale() {
		ZoomModule m = instance;
		if (!active() || !m.sensitivity.get()) {
			return 1.0;
		}
		return 1.0 / Math.max(1f, m.zoom.get());
	}

	public static boolean cinematic() {
		return active() && instance.held && instance.smoothCamera.get();
	}

	/** Mouse wheel during gameplay; true if it zoomed (and the hotbar shouldn't scroll). */
	public static boolean onScroll(double amount) {
		ZoomModule m = instance;
		if (!active() || !m.held || !m.scroll.get() || amount == 0.0) {
			return false;
		}
		float next = m.scrollFactor * (amount > 0 ? SCROLL_STEP : 1f / SCROLL_STEP);
		float base = (float) m.factor.get();
		m.scrollFactor = Math.clamp(next, 1f / base, MAX_ZOOM / base);
		return true;
	}
}
