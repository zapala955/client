package net.client.visuals.render;

import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.render.text.SharpLayout;
import net.client.visuals.render.text.SharpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;

/**
 * Text rendering with the bundled Inter typeface.
 *
 * <h2>Crisp at every GUI scale</h2>
 * Labels are drawn by {@link SharpText}: light-hinted, kerned, fractionally positioned glyphs
 * rasterised at exact device size and blended with their full anti-aliasing (see there for why the
 * vanilla renderer can't do this). The left edge and baseline are snapped with
 * {@link HighDpiEngine#snap(float)}.
 *
 * <p>Fallback (FreeType unavailable, or characters Inter lacks): Minecraft's renderer with the
 * per-scale font definitions ({@code body_1x … body_6x}, oversample == GUI scale), which is
 * pixel-exact but full-hinted and unkerned.</p>
 *
 * <h2>Metrics</h2>
 * Fonts are tuned (via {@code shift}) so the cap height starts at the given {@code y}. Layout code
 * vertically centres text with {@link TextStyle#capHeight()}.
 */
public final class LiquidText {
	private static final int MAX_SCALE = 6;
	/** Em size of the lyric faces, in GUI units (drawn scaled to the chosen text size). */
	public static final float LYRIC_EM = 30f;

	/** Renderer choice of a style: the Custom Font setting decides. */
	private static final int FOLLOW = 0;
	/** Always its own typeface: a chosen lyric font is the point of choosing it. */
	private static final int CUSTOM = 1;
	/** Always Minecraft's font (the pixel lyric face). */
	private static final int VANILLA = 2;

	/** Typographic roles, and the Lyrics element's faces. */
	public enum TextStyle {
		BODY("body", SharpText.WEIGHT_REGULAR, 9f, 6.55f, 0.45f),
		LABEL("label", SharpText.WEIGHT_SEMIBOLD, 9f, 6.55f, 0.45f),
		TITLE("title", SharpText.WEIGHT_SEMIBOLD, 12f, 8.7f, 0f),
		/** Card headlines (10.5 units, semibold): song titles. */
		HEADLINE("headline", SharpText.WEIGHT_SEMIBOLD, 10.5f, 7.64f, 0.5f),
		/** Small secondary text (7 units): artists, times, units. */
		CAPTION("caption", SharpText.WEIGHT_REGULAR, 7f, 5.1f, 0.35f),
		/** Bold (700) hero type for the main header. */
		DISPLAY("display", SharpText.WEIGHT_BOLD, 30f, 21.8f, 0f),

		// Lyric faces: 30-unit em, cap top at the origin. Sharp-rendered like Inter; their single
		// vanilla definition (font/lyric_<face>.json) is the fallback and the world-text font.
		LYRIC_MODERN("lyric_modern", SharpText.WEIGHT_REGULAR, 0.7275f, false, false),
		LYRIC_MODERN_BOLD("lyric_modern_bold", SharpText.WEIGHT_BOLD, 0.7267f, true, false),
		LYRIC_ELEGANT("elegant", 0.708f),
		LYRIC_ELEGANT_BOLD("elegant_bold", 0.708f),
		LYRIC_ELEGANT_ITALIC("elegant_italic", 0.708f),
		LYRIC_ELEGANT_BOLD_ITALIC("elegant_bold_italic", 0.708f),
		LYRIC_SCRIPT("script", 0.72f),
		LYRIC_SCRIPT_BOLD("script_bold", 0.721f),
		LYRIC_POSTER("poster", 0.7f),
		LYRIC_ROUNDED("rounded", 0.712f),
		LYRIC_ROUNDED_BOLD("rounded_bold", 0.714f),
		LYRIC_ROUNDED_ITALIC("rounded_italic", 0.712f),
		LYRIC_MONO("mono", 0.73f),
		LYRIC_MONO_BOLD("mono_bold", 0.73f),
		LYRIC_MONO_ITALIC("mono_italic", 0.73f),
		LYRIC_MARKER("marker", 0.7402f),
		LYRIC_FUTURISTIC("futuristic", 0.72f),
		LYRIC_FUTURISTIC_BOLD("futuristic_bold", 0.72f),
		LYRIC_HANDWRITING("handwriting", 0.7067f),
		LYRIC_HANDWRITING_BOLD("handwriting_bold", 0.7195f),
		/** Minecraft's pixel font scaled up to the lyric em, with vanilla's own bold and italic. */
		LYRIC_PIXEL(false, false),
		LYRIC_PIXEL_BOLD(true, false),
		LYRIC_PIXEL_ITALIC(false, true),
		LYRIC_PIXEL_BOLD_ITALIC(true, true);

		/** Face slot for {@link SharpText} (Inter weight or lyric face). */
		final int weight;
		/** Em size in GUI units. */
		final float size;
		final float interCapHeight;
		final float interTopInset;
		final Style[] styles = new Style[MAX_SCALE];
		/** Style when drawn by Minecraft's own font (the custom font off). */
		final Style vanillaStyle;
		private final int renderer;
		private final boolean bold;
		private final boolean italic;

		TextStyle(String name, int weight, float size, float capHeight, float topInset) {
			this.weight = weight;
			this.size = size;
			this.interCapHeight = capHeight;
			this.interTopInset = topInset;
			for (int i = 0; i < MAX_SCALE; i++) {
				styles[i] = Style.EMPTY.withFont(new FontDescription.Resource(LiquidClient.id(name + "_" + (i + 1) + "x")));
			}
			this.vanillaStyle = Style.EMPTY;
			this.renderer = FOLLOW;
			this.bold = false;
			this.italic = false;
		}

		/** A bundled lyric face ({@code font/lyrics/<face>.ttf}). */
		TextStyle(String face, float capRatio) {
			this("lyric_" + face, SharpText.lyricWeight(face), capRatio, face.contains("bold"), face.contains("italic"));
		}

		TextStyle(String name, int weight, float capRatio, boolean bold, boolean italic) {
			this.weight = weight;
			this.size = LYRIC_EM;
			this.interCapHeight = capRatio * LYRIC_EM;
			this.interTopInset = 0f;
			java.util.Arrays.fill(styles, Style.EMPTY.withFont(new FontDescription.Resource(LiquidClient.id(name))));
			this.vanillaStyle = Style.EMPTY;
			this.renderer = CUSTOM;
			this.bold = bold;
			this.italic = italic;
		}

		/** Minecraft's font. */
		TextStyle(boolean bold, boolean italic) {
			this.weight = -1;
			this.size = LYRIC_EM;
			this.interCapHeight = 7f * LYRIC_EM / 9f;
			this.interTopInset = 0f;
			this.vanillaStyle = Style.EMPTY.withBold(bold).withItalic(italic);
			java.util.Arrays.fill(styles, vanillaStyle);
			this.renderer = VANILLA;
			this.bold = bold;
			this.italic = italic;
		}

		/** Whether this style is drawn with its own typeface right now (else Minecraft's font, scaled). */
		public boolean custom() {
			return renderer == CUSTOM || renderer == FOLLOW && LiquidConfig.customFont;
		}

		/** Vanilla bitmap glyphs are 7 units tall at scale 1; larger roles scale them up. */
		float vanillaScale() {
			return size / 9f;
		}

		public float capHeight() {
			return custom() ? interCapHeight : 7f * vanillaScale();
		}

		/** A real bold / italic cut (a lyric face that lacks one gets a synthesised one). */
		public boolean bold() {
			return bold;
		}

		public boolean italic() {
			return italic;
		}

		/** Style for world text (Minecraft's renderer): the finest definition. */
		public Style worldStyle() {
			return custom() ? styles[MAX_SCALE - 1] : vanillaStyle;
		}

		/** Layout units per font unit of {@link #worldStyle()}. */
		public float worldUnits() {
			return custom() ? 1f : vanillaScale();
		}

		/** Cap height in font units of {@link #worldStyle()} (its cap top sits at the text origin). */
		public float worldCapHeight() {
			return custom() ? interCapHeight : 7f;
		}
	}

	private LiquidText() {
	}

	static Font font() {
		return Minecraft.getInstance().font;
	}

	/** Index of the font variant matching the current GUI scale. */
	static int scaleIndex() {
		return Math.clamp(HighDpiEngine.scale(), 1, MAX_SCALE) - 1;
	}

	/** Key that changes whenever a label has to rebuild its glyph sequence. */
	static int variantKey(TextStyle style) {
		return style.custom() ? (HighDpiEngine.scale() << 6 | style.ordinal()) : -1 - style.ordinal();
	}

	static Style styleFor(TextStyle style) {
		return style.custom() ? style.styles[scaleIndex()] : style.vanillaStyle;
	}

	/**
	 * Draws a label with its cap-height box starting at ({@code x}, {@code y}).
	 *
	 * @param shadow subtle one-device-pixel shadow instead of vanilla's heavy one-unit drop shadow
	 */
	public static void draw(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color, boolean shadow) {
		draw(g, label, x, y, color, shadow ? SHADOW_CRISP : SHADOW_NONE);
	}

	/**
	 * Text over photos: a soft, blurred shadow that keeps white text legible on bright clouds without
	 * a hard offset copy. With {@link SharpText} it's one extra run of pre-blurred glyphs; on the
	 * vanilla fallback it's approximated with five faint taps.
	 */
	public static void drawSoft(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color) {
		draw(g, label, x, y, color, SHADOW_SOFT);
	}

	public static void drawCenteredSoft(GuiGraphicsExtractor g, TextLabel label, float centerX, float y, int color) {
		draw(g, label, centerX - label.width() * 0.5f, y, color, SHADOW_SOFT);
	}

	private static final int SHADOW_NONE = 0;
	private static final int SHADOW_CRISP = 1;
	private static final int SHADOW_SOFT = 2;
	/** Blurred shadow tint for the sharp path (the blur spreads it, so it can be denser). */
	private static final int SOFT_SHADOW = 0x6B000000;
	/** Soft shadow taps: device-pixel offset x, y and opacity. */
	private static final float[] SOFT_TAPS = {
			0f, 1f, 0.14f,
			-1f, 1f, 0.09f,
			1f, 1f, 0.09f,
			0f, 2f, 0.09f,
			0f, 0f, 0.09f,
	};

	private static void draw(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color, int shadow) {
		int c = RenderUtils.applyAlpha(color);
		if ((c >>> 24) < 4) {
			return;
		}
		SharpLayout sharp = label.sharpLayout();
		if (sharp != null) {
			drawSharp(g, label, sharp, x, y, c, shadow);
			return;
		}
		Font font = font();
		TextStyle style = label.style();
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		float scale = 1f;
		if (style.custom()) {
			pose.translate(HighDpiEngine.snap(x), HighDpiEngine.snap(y - style.interTopInset));
		} else {
			pose.translate(HighDpiEngine.snap(x), HighDpiEngine.snap(y));
			scale = style.vanillaScale();
			if (scale != 1f) {
				pose.scale(scale, scale);
			}
		}
		if (shadow == SHADOW_CRISP) {
			float off = HighDpiEngine.px() / scale;
			int shadowColor = ColorUtil.fade(Palette.TEXT_SHADOW, (c >>> 24) / 255f);
			pose.translate(0f, off);
			emit(g, font, label, shadowColor, scale);
			pose.translate(0f, -off);
		} else if (shadow == SHADOW_SOFT) {
			float px = HighDpiEngine.px() / scale;
			float alpha = (c >>> 24) / 255f;
			for (int i = 0; i < SOFT_TAPS.length; i += 3) {
				float dx = SOFT_TAPS[i] * px;
				float dy = SOFT_TAPS[i + 1] * px;
				pose.translate(dx, dy);
				emit(g, font, label, ColorUtil.fade(0xFF000000, SOFT_TAPS[i + 2] * alpha), scale);
				pose.translate(-dx, -dy);
			}
		}
		emit(g, font, label, c, scale);
		pose.popMatrix();
	}

	/**
	 * {@link SharpText} path: left edge and baseline snapped to device pixels, so every glyph quad maps
	 * 1:1 onto the framebuffer. The cap top lands on {@code y} (the baseline is {@code y + capHeight}).
	 */
	private static void drawSharp(GuiGraphicsExtractor g, TextLabel label, SharpLayout layout, float x, float y, int c, int shadow) {
		float ox = HighDpiEngine.snap(x);
		float baseline = HighDpiEngine.snap(y + label.style().interCapHeight);
		float alpha = (c >>> 24) / 255f;
		if (shadow == SHADOW_CRISP) {
			SharpText.draw(g, layout, ox, baseline + HighDpiEngine.px(), ColorUtil.fade(Palette.TEXT_SHADOW, alpha));
		} else if (shadow == SHADOW_SOFT) {
			SharpLayout soft = label.sharpShadowLayout();
			if (soft != null) {
				float drop = Math.max(1, Math.round(HighDpiEngine.scale() * 0.5f)) * HighDpiEngine.px();
				SharpText.draw(g, soft, ox, baseline + drop, ColorUtil.fade(SOFT_SHADOW, alpha));
			}
		}
		SharpText.draw(g, layout, ox, baseline, c);
	}

	private static void emit(GuiGraphicsExtractor g, Font font, TextLabel label, int color, float scale) {
		if (!label.tracked()) {
			g.text(font, label.sequence(), 0, 0, color, false);
			return;
		}
		FormattedCharSequence[] glyphs = label.glyphs();
		float[] offsets = label.offsets();
		Matrix3x2fStack pose = g.pose();
		for (int i = 0; i < glyphs.length; i++) {
			// Each character origin on the device-pixel grid too.
			float dx = HighDpiEngine.snap(offsets[i]) / scale;
			pose.translate(dx, 0f);
			g.text(font, glyphs[i], 0, 0, color, false);
			pose.translate(-dx, 0f);
		}
	}

	public static void drawCentered(GuiGraphicsExtractor g, TextLabel label, float centerX, float y, int color, boolean shadow) {
		draw(g, label, centerX - label.width() * 0.5f, y, color, shadow);
	}

	/**
	 * Headline glow: eight faint copies offset around the glyphs, giving a soft accent halo.
	 * Only used for a handful of headlines per frame, and gated by the Text Glow setting.
	 */
	public static void drawGlow(GuiGraphicsExtractor g, TextLabel label, float x, float y, int glowColor, float radius) {
		if (!LiquidConfig.textGlow) {
			return;
		}
		int c = ColorUtil.fade(glowColor, 0.16f);
		final float d = radius * 0.7071f;
		draw(g, label, x - radius, y, c, false);
		draw(g, label, x + radius, y, c, false);
		draw(g, label, x, y - radius, c, false);
		draw(g, label, x, y + radius, c, false);
		draw(g, label, x - d, y - d, c, false);
		draw(g, label, x + d, y - d, c, false);
		draw(g, label, x - d, y + d, c, false);
		draw(g, label, x + d, y + d, c, false);
	}
}
