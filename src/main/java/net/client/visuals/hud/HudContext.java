package net.client.visuals.hud;

import net.client.visuals.media.AlbumArt;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Per-frame state shared by all HUD elements (one instance, refilled every frame – no allocation):
 * timing, whether the editor is drawing, and the theme resolved into colours and helpers.
 */
public final class HudContext {
	public static final int TEXT = 0xFFF8FAFC;
	public static final int TEXT_DIM = 0x94FFFFFF;
	public static final int GOOD = 0xFF34D399;
	public static final int WARN = 0xFFFBBF24;
	public static final int BAD = 0xFFF87171;

	static final HudContext INSTANCE = new HudContext();

	public float dt;
	public float time;
	/** Drawn by the HUD editor: show placeholders, never hide. */
	public boolean editing;
	/** Partial tick for interpolating entity state (1 in the editor). */
	public float partialTick = 1f;
	public HudTheme theme;
	private int accent;
	private int currentStyle;

	private HudContext() {
	}

	static HudContext begin(HudTheme theme, float dt, float time, boolean editing, float partialTick) {
		HudContext ctx = INSTANCE;
		ctx.partialTick = partialTick;
		ctx.theme = theme;
		ctx.dt = dt;
		ctx.time = time;
		ctx.editing = editing;
		HudStyle.blur = (float) theme.blur.get();
		int custom = ColorUtil.hsv((float) theme.accentHue.get(), 0.82f, 1f);
		ctx.accent = switch (theme.accentMode.index()) {
			case 1 -> 0; // chroma: per element / per call
			case 2 -> AlbumArt.accent(custom);
			default -> custom;
		};
		return ctx;
	}

	/** Accent colour; {@code phase} offsets the hue in chroma mode so elements don't all match. */
	public int accent(float phase) {
		return accent != 0 ? accent : ColorUtil.hsv(time * 50f + phase * 360f, 0.7f, 1f);
	}

	public int accent() {
		return accent(0f);
	}

	/** Effective style of an element (its override, or the theme's). */
	public int style(HudModule element) {
		int own = element.style.index();
		return own == 0 ? theme.style.index() : own - 1;
	}

	/** Draws the element's background and remembers its style for the text helpers below. */
	public void background(GuiGraphicsExtractor g, HudModule element, float w, float h) {
		currentStyle = style(element);
		HudStyle.background(g, currentStyle, w, h, (float) theme.radius.get(), accent(element.phase()),
				(float) theme.opacity.get() / 100f, time);
	}

	/** Same, for sub-boxes (keystroke keys) using an explicit style. */
	public void box(GuiGraphicsExtractor g, int style, float w, float h, float phase) {
		HudStyle.background(g, style, w, h, (float) theme.radius.get(), accent(phase), (float) theme.opacity.get() / 100f, time);
	}

	public float radius(float h) {
		return HudStyle.radius(currentStyle, h, (float) theme.radius.get());
	}

	/** 0 = icon, 1 = text, 2 = none. */
	public int labels() {
		return theme.labels.index();
	}

	/** Text with the theme's shadow (always soft on styles without a solid background). */
	public void text(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color) {
		if (theme.textShadow.get() || HudStyle.needsShadow(currentStyle)) {
			LiquidText.drawSoft(g, label, x, y, color);
		} else {
			LiquidText.draw(g, label, x, y, color, false);
		}
	}

	/** Text vertically centred on {@code cy}. */
	public void textMid(GuiGraphicsExtractor g, TextLabel label, float x, float cy, int color) {
		text(g, label, x, cy - label.capHeight() * 0.5f, color);
	}

	public void icon(GuiGraphicsExtractor g, int codepoint, float cx, float cy, float size, int color) {
		IconFont.draw(g, codepoint, cx, cy, size, color, 0x40000000);
	}
}
