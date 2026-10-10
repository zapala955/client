package net.client.visuals.hud.island;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The island's design tokens (GUI units, at 100 % scale) and the small pieces its activities are
 * built from: text, icon tiles, the switch, progress rings, signal bars. Colours are iOS's dark-mode
 * system colours; text is pure white on the black island, secondary text white at 55 %.
 */
public final class IslandDraw {
	public static final int WHITE = 0xFFFFFFFF;
	public static final int DIM = 0x8CFFFFFF;
	public static final int FAINT = 0x47FFFFFF;
	public static final int TRACK = 0x33FFFFFF;
	public static final int GREEN = 0xFF30D158;
	public static final int RED = 0xFFFF453A;
	public static final int ORANGE = 0xFFFF9F0A;
	public static final int YELLOW = 0xFFFFD60A;
	public static final int BLUE = 0xFF0A84FF;
	public static final int TEAL = 0xFF64D2FF;
	public static final int PURPLE = 0xFFBF5AF2;
	public static final int INDIGO = 0xFF5E5CE6;

	/** Height of the compact pill (and the bubble's diameter). */
	public static final float COMPACT_H = 20f;
	/** The idle pill's width. */
	public static final float IDLE_W = 74f;
	/** Leading glyph box in the compact pill, inset {@link #LEAD_INSET} from the ends. */
	public static final float LEAD = 14f;
	public static final float LEAD_INSET = 3f;
	/** Empty middle of a compact pill (where the iPhone's camera sits). */
	public static final float CENTER = 66f;
	/** Right inset of the trailing accessory (more than the leading's: the pill curves away). */
	public static final float TRAIL_INSET = 8f;
	public static final float BUBBLE_GAP = 6f;

	public static final float ALERT_H = 34f;
	public static final float ALERT_MIN_W = 150f;
	public static final float ALERT_MAX_W = 250f;
	public static final float TILE = 24f;
	public static final float ALERT_PAD = 5f;
	/** Between the leading tile and the text, and before a trailing accessory. */
	public static final float ALERT_GAP = 9f;
	public static final float ALERT_RIGHT = 13f;

	private IslandDraw() {
	}

	/** Width of a compact pill with a trailing accessory this wide. */
	public static float compactWidth(float trailing) {
		return LEAD_INSET + LEAD + CENTER + trailing + TRAIL_INSET;
	}

	/** Width of an alert: leading, the wider of its two lines, an optional accessory; within the min / max. */
	public static float alertWidth(float lead, float text, float accessory) {
		float w = ALERT_PAD + lead + ALERT_GAP + text + (accessory > 0f ? ALERT_GAP + accessory : 0f) + ALERT_RIGHT;
		return Math.clamp(w, ALERT_MIN_W, ALERT_MAX_W);
	}

	/** Room left for an alert's text lines at the maximum width. */
	public static float alertTextRoom(float lead, float accessory) {
		return ALERT_MAX_W - ALERT_PAD - lead - ALERT_GAP - (accessory > 0f ? ALERT_GAP + accessory : 0f) - ALERT_RIGHT;
	}

	public static void text(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color) {
		LiquidText.draw(g, label, x, y, color, false);
	}

	/** Text vertically centred on {@code cy}. */
	public static void textMid(GuiGraphicsExtractor g, TextLabel label, float x, float cy, int color) {
		LiquidText.draw(g, label, x, cy - label.capHeight() * 0.5f, color, false);
	}

	/** An alert's two lines (title, then a caption), placed for a box of height {@code h}. */
	public static void lines(GuiGraphicsExtractor g, TextLabel title, TextLabel subtitle, float x, float h, int subtitleColor) {
		text(g, title, x, h * 0.5f - 7.5f, WHITE);
		text(g, subtitle, x, h * 0.5f + 3.5f, subtitleColor);
	}

	/** An app-icon style tile: the colour as a soft vertical gradient with a white glyph. */
	public static void tile(GuiGraphicsExtractor g, float x, float y, float size, int color, int icon) {
		float r = size * 0.31f;
		RenderUtils.gradient(g, x, y, size, size, r, ColorUtil.lerpRgb(color, 0xFFFFFFFF, 0.14f), ColorUtil.lerpRgb(color, 0xFF000000, 0.16f));
		RenderUtils.stroke(g, x, y, size, size, r, HighDpiEngine.px(), 0x33FFFFFF, 0x08FFFFFF);
		IconFont.draw(g, icon, x + size * 0.5f, y + size * 0.5f, size * 0.54f, WHITE, 0x33000000);
	}

	/** A dark tile for something drawn on top (an item, a face). */
	public static void plainTile(GuiGraphicsExtractor g, float x, float y, float size) {
		float r = size * 0.31f;
		RenderUtils.gradient(g, x, y, size, size, r, 0xFF333336, 0xFF232325);
		RenderUtils.stroke(g, x, y, size, size, r, HighDpiEngine.px(), 0x26FFFFFF, 0x08FFFFFF);
	}

	/** iOS switch, {@code knob} 0 (off) → 1 (on), vertically centred on {@code cy}. 26 × 15. */
	public static void toggle(GuiGraphicsExtractor g, float x, float cy, float knob) {
		final float w = 26f;
		final float h = 15f;
		float y = cy - h * 0.5f;
		RenderUtils.fill(g, x, y, w, h, h * 0.5f, ColorUtil.lerp(0xFF39393D, GREEN, knob));
		float kx = x + h * 0.5f + (w - h) * knob;
		RenderUtils.shadow(g, kx - h * 0.5f + 1.5f, y + 2f, h - 3f, h - 3f, (h - 3f) * 0.5f, 1.5f, 0x40000000);
		RenderUtils.circle(g, kx, cy, h * 0.5f - 1.5f, WHITE);
	}

	/**
	 * A progress ring: a faint track and an arc clockwise from the top. Opaque colours only (the arc
	 * is built from overlapping capsules).
	 */
	public static void ring(GuiGraphicsExtractor g, float cx, float cy, float r, float thickness, float fraction, int color) {
		RenderUtils.ring(g, cx, cy, r, thickness, TRACK);
		float f = Math.clamp(fraction, 0f, 1f);
		if (f > 0.002f) {
			float mid = r - thickness * 0.5f;
			RenderUtils.arc(g, cx, cy, mid, 0f, f * (float) (Math.PI * 2.0), thickness, color);
		}
	}

	/** Four signal bars, the first {@code lit} in {@code color}; 14 wide, bottoms on {@code bottom}. */
	public static void signal(GuiGraphicsExtractor g, float x, float bottom, int lit, int color) {
		for (int i = 0; i < 4; i++) {
			float h = 4f + i * 2.6f;
			RenderUtils.fill(g, x + i * 3.6f, bottom - h, 2.4f, h, 1.2f, i < lit ? color : FAINT);
		}
	}

	/**
	 * Lub-dub: two beats close together, then a rest – 0 at rest, 1 at the peak of the first beat.
	 *
	 * @param bpm beats per minute
	 */
	public static float heartbeat(float time, float bpm) {
		float t = (time * bpm / 60f) % 1f;
		return pulse(t, 0.0f) + 0.6f * pulse(t, 0.17f);
	}

	private static float pulse(float t, float at) {
		float x = (t - at) / 0.11f;
		return x < 0f || x > 1f ? 0f : (float) Math.sin(x * Math.PI);
	}
}
