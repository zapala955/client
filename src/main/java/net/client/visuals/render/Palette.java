package net.client.visuals.render;

/**
 * "Liquid Glass" design tokens as packed ARGB ints (0xAARRGGBB).
 *
 * <p>Primitive ints instead of a Color class means zero allocation and no boxing anywhere in
 * the render path. Text colors are chosen to keep ≥ 4.5:1 contrast against the darkest
 * glass tint so labels stay legible no matter what's blurred behind them.</p>
 */
public final class Palette {
	// ---- Glass surfaces ------------------------------------------------------------------
	/** Slate-900 #0F172A at ~55% – the standard frosted panel. */
	public static final int GLASS_FILL = 0x8C0F172A;
	/** Slightly denser glass for nested surfaces (cards inside a window). */
	public static final int GLASS_FILL_RAISED = 0x661E293B;
	/** Used when blur is unavailable / disabled: more opaque so content stays readable. */
	public static final int GLASS_FILL_OPAQUE = 0xE60F172A;
	/** Top edge of the specular sheen gradient. */
	public static final int GLASS_SHEEN_TOP = 0x1FFFFFFF;
	public static final int GLASS_SHEEN_BOTTOM = 0x00FFFFFF;
	/** Bottom of the gradient rim: rgba(255,255,255,0.05). */
	public static final int GLASS_BORDER = 0x0DFFFFFF;
	/** Top of the gradient rim: rgba(255,255,255,0.20) – light catches the upper edge. */
	public static final int GLASS_BORDER_HIGHLIGHT = 0x33FFFFFF;
	/** Hovered rim: brighter, per the "increase border brightness on hover" rule. */
	public static final int GLASS_BORDER_HOVER = 0x66FFFFFF;
	/** Inset highlight one pixel inside the rim. */
	public static final int GLASS_INNER_HIGHLIGHT = 0x14FFFFFF;
	/** Ambient drop shadow. */
	public static final int SHADOW = 0x66000000;
	/** "Clear" glass (lock-screen controls): white-lifted frost that lets the photo's colour through. */
	public static final int CLEAR_FILL_TOP = 0x3DFFFFFF;
	public static final int CLEAR_FILL_BOTTOM = 0x24FFFFFF;
	/** Added on hover. */
	public static final int CLEAR_FILL_HOVER = 0x1AFFFFFF;
	public static final int CLEAR_RIM_TOP = 0x8CFFFFFF;
	public static final int CLEAR_RIM_BOTTOM = 0x1FFFFFFF;
	public static final int CLEAR_RIM_HOVER = 0xCCFFFFFF;
	public static final int CLEAR_SHADOW = 0x40000000;
	/** Soft shadow under white icons on photos. */
	public static final int ICON_SHADOW = 0x4D000000;
	/** Liquid tint laid over the panorama / world. */
	public static final int BACKDROP_TINT_TOP = 0x660B1120;
	public static final int BACKDROP_TINT_BOTTOM = 0xA6020617;

	// ---- Accents (Apple system colors) ---------------------------------------------------
	public static final int ACCENT_BLUE = 0xFF007AFF;
	public static final int ACCENT_PURPLE = 0xFFAF52DE;
	public static final int ACCENT_PINK = 0xFFFF2D55;
	public static final int ACCENT_GLOW = 0x55007AFF;
	public static final int ACCENT_GREEN = 0xFF30D158;
	public static final int ACCENT_ORANGE = 0xFFFF9F0A;
	public static final int ACCENT_TEAL = 0xFF64D2FF;

	// ---- macOS window controls -----------------------------------------------------------
	public static final int TRAFFIC_RED = 0xFFFF5F57;
	public static final int TRAFFIC_YELLOW = 0xFFFEBC2E;
	public static final int TRAFFIC_GREEN = 0xFF28C840;
	public static final int TRAFFIC_IDLE = 0x40FFFFFF;

	// ---- Text ----------------------------------------------------------------------------
	public static final int TEXT_PRIMARY = 0xFFF8FAFC;   // slate-50
	public static final int TEXT_SECONDARY = 0xCCCBD5E1; // slate-300 @ 80%
	public static final int TEXT_TERTIARY = 0x99A5B4C8;  // muted captions
	public static final int TEXT_SHADOW = 0x59000000;
	/** Uppercase section labels: crisp white at 80%. */
	public static final int TEXT_SECTION = 0xCCF8FAFC;

	// ---- Controls ------------------------------------------------------------------------
	public static final int TOGGLE_TRACK_OFF = 0x33FFFFFF;
	public static final int TOGGLE_KNOB = 0xFFFFFFFF;
	public static final int HOVER_FILL = 0x14FFFFFF;
	public static final int SELECTION_FILL = 0x2E007AFF;

	private Palette() {
	}
}
