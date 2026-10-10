package net.client.visuals.render.anim;

import net.client.visuals.LiquidConfig;

/**
 * Shared motion tokens. Every animated property in the client pulls its timing from here so the
 * whole UI moves with one consistent "feel" instead of a different magic number per widget.
 *
 * <p>All speeds feed {@link Smooth}, i.e. {@code lerp(current, target, 1 - e^(-speed·dt))}: the
 * frame-rate independent form of {@code lerp(current, target, delta * speed)}.</p>
 */
public final class Motion {
	/** Hover lift / glow (1/s). ~70 ms time constant – responsive but not twitchy. */
	public static final float HOVER = 14f;
	/** Press contraction – deliberately snappier than hover so clicks feel immediate. */
	public static final float PRESS = 34f;
	/** Toggle knob travel. */
	public static final float TOGGLE = 16f;
	/** Sliding category indicator. */
	public static final float INDICATOR = 15f;
	/** Window follows the cursor while dragging (high = tight, low = floaty). */
	public static final float DRAG_FOLLOW = 30f;
	/** Smooth scrolling. */
	public static final float SCROLL = 18f;
	/** Sky / background cross-fades when a module or mode changes. */
	public static final float CROSSFADE = 3.5f;

	public static final float OPEN_SECONDS = 0.34f;
	/** Exits are faster than entries. */
	public static final float CLOSE_SECONDS = 0.18f;

	/** Generic hover expansion: 1.00 → 1.03. */
	public static final float HOVER_SCALE = 0.03f;
	/** Dock items: 1.00 → 1.04. */
	public static final float DOCK_HOVER_SCALE = 0.04f;
	/** Pressed contraction: scale 0.97. */
	public static final float PRESS_SCALE = 0.97f;

	private Motion() {
	}

	/**
	 * Applies the user's "Reduce Motion" preference: animations still happen, but complete almost
	 * instantly. Speeds are multiplied rather than zeroed so no code path divides by zero.
	 */
	public static float speed(float base) {
		return LiquidConfig.reduceMotion ? base * 8f : base;
	}

	public static float seconds(float base) {
		return LiquidConfig.reduceMotion ? base * 0.15f : base;
	}

	/**
	 * Combined interaction scale: expands with hover, contracts while pressed.
	 *
	 * @param hover 0..1 hover amount
	 * @param press 0..1 press amount
	 * @param hoverScale e.g. {@link #HOVER_SCALE}
	 */
	public static float interactionScale(float hover, float press, float hoverScale) {
		float hovered = 1f + hoverScale * hover;
		return hovered + (PRESS_SCALE - hovered) * press;
	}
}
