package net.client.visuals.render;

import net.client.visuals.render.text.SharpLayout;
import net.client.visuals.render.text.SharpText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * A pre-shaped piece of text. Builds its glyph quads (or vanilla glyph sequences) and width once and
 * only rebuilds when the text, GUI scale, font setting, max width or glyph atlas changes, so drawing a
 * label every frame allocates nothing on our side.
 *
 * <p>With the custom font on, labels are laid out by {@link SharpText} (kerned, fractionally
 * positioned, light-hinted Inter). Text with characters Inter doesn't have falls back to Minecraft's
 * renderer.</p>
 *
 * <ul>
 *   <li>{@link #tracking(float)} – letter-spacing in GUI units (headers, section labels). Minecraft's
 *       text renderer has no tracking, so tracked labels are pre-split into per-character runs with
 *       cached x offsets.</li>
 *   <li>{@link #uppercase()} – Apple-style section labels.</li>
 *   <li>{@link #maxWidth(float)} – truncates with an ellipsis.</li>
 * </ul>
 */
public final class TextLabel {
	private static final String ELLIPSIS = "…";
	private static final FormattedCharSequence[] NO_GLYPHS = new FormattedCharSequence[0];
	private static final float[] NO_OFFSETS = new float[0];

	private final LiquidText.TextStyle style;
	private String text;
	private float maxWidth = Float.MAX_VALUE;
	private float tracking;
	private boolean uppercase;

	private int cachedKey = Integer.MIN_VALUE;
	private float cachedMaxWidth = -1f;
	private int cachedGeneration = -1;
	/** Whether the current layout is drawn by {@link SharpText}. */
	private boolean cachedSharp;
	private @Nullable SharpLayout sharp;
	private @Nullable SharpLayout sharpShadow;
	private boolean shadowValid;
	private String fitted = "";
	private FormattedCharSequence sequence = FormattedCharSequence.EMPTY;
	private FormattedCharSequence[] glyphs = NO_GLYPHS;
	private float[] offsets = NO_OFFSETS;
	private float width;

	public TextLabel(String text, LiquidText.TextStyle style) {
		this.text = text;
		this.style = style;
	}

	public LiquidText.TextStyle style() {
		return style;
	}

	public String text() {
		return text;
	}

	/** Replaces the text (layout-time only – this invalidates the cache). */
	public void setText(String text) {
		if (!this.text.equals(text)) {
			this.text = text;
			invalidate();
		}
	}

	/** Truncate with an ellipsis beyond this width (GUI units). */
	public TextLabel maxWidth(float maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	/** Letter-spacing between characters, in GUI units. */
	public TextLabel tracking(float tracking) {
		this.tracking = tracking;
		invalidate();
		return this;
	}

	public TextLabel uppercase() {
		this.uppercase = true;
		invalidate();
		return this;
	}

	boolean tracked() {
		return tracking != 0f;
	}

	public FormattedCharSequence sequence() {
		ensure();
		return sequence;
	}

	FormattedCharSequence[] glyphs() {
		ensure();
		return glyphs;
	}

	float[] offsets() {
		ensure();
		return offsets;
	}

	/** Rendered width in GUI units (after tracking and truncation). */
	public float width() {
		ensure();
		return width;
	}

	public float capHeight() {
		return style.capHeight();
	}

	/** Sharp glyph quads, or null when this label uses the vanilla renderer. */
	@Nullable SharpLayout sharpLayout() {
		ensure();
		return sharp != null && cachedSharp ? sharp : null;
	}

	/** Blurred shadow quads for {@link #sharpLayout()}, built on first use. */
	@Nullable SharpLayout sharpShadowLayout() {
		ensure();
		if (sharp == null || !cachedSharp) {
			return null;
		}
		if (!shadowValid) {
			if (sharpShadow == null) {
				sharpShadow = new SharpLayout();
			}
			int scale = HighDpiEngine.scale();
			SharpText.layout(sharpShadow, style.weight, style.size * scale, scale, fitted, tracking * scale, true);
			shadowValid = true;
		}
		return sharpShadow;
	}

	private void invalidate() {
		cachedKey = Integer.MIN_VALUE;
	}

	private void ensure() {
		int key = LiquidText.variantKey(style);
		int generation = SharpText.generation();
		if (key == cachedKey && maxWidth == cachedMaxWidth && generation == cachedGeneration) {
			return;
		}
		cachedKey = key;
		cachedMaxWidth = maxWidth;
		cachedGeneration = generation;
		shadowValid = false;
		String source = uppercase ? text.toUpperCase(Locale.ROOT) : text;
		cachedSharp = style.custom() && SharpText.supports(style.weight, source) && layoutSharp(source);
		if (cachedSharp) {
			return;
		}
		Font font = LiquidText.font();
		Style fontStyle = LiquidText.styleFor(style);
		float scale = style.custom() ? 1f : style.vanillaScale();

		String fitted = source;
		if (measure(font, fontStyle, scale, source) > maxWidth && !source.isEmpty()) {
			// Binary search the longest prefix that fits together with the ellipsis.
			int lo = 0;
			int hi = source.length();
			while (lo < hi) {
				int mid = (lo + hi + 1) >>> 1;
				if (measure(font, fontStyle, scale, source.substring(0, mid).stripTrailing() + ELLIPSIS) <= maxWidth) {
					lo = mid;
				} else {
					hi = mid - 1;
				}
			}
			fitted = source.substring(0, lo).stripTrailing() + ELLIPSIS;
		}

		sequence = FormattedCharSequence.forward(fitted, fontStyle);
		if (tracking != 0f) {
			int count = fitted.codePointCount(0, fitted.length());
			glyphs = new FormattedCharSequence[count];
			offsets = new float[count];
			float cursor = 0f;
			int index = 0;
			for (int i = 0; i < fitted.length(); ) {
				int cp = fitted.codePointAt(i);
				String ch = new String(Character.toChars(cp));
				glyphs[index] = FormattedCharSequence.forward(ch, fontStyle);
				offsets[index] = cursor;
				cursor += font.width(glyphs[index]) * scale + tracking;
				index++;
				i += Character.charCount(cp);
			}
			width = Math.max(0f, cursor - tracking);
		} else {
			glyphs = NO_GLYPHS;
			offsets = NO_OFFSETS;
			width = font.width(sequence) * scale;
		}
	}

	private boolean layoutSharp(String source) {
		int scale = HighDpiEngine.scale();
		float emPx = style.size * scale;
		float trackPx = tracking * scale;
		String result = source;
		if (SharpText.measure(style.weight, emPx, source, trackPx) / scale > maxWidth && !source.isEmpty()) {
			if (!SharpText.supports(style.weight, ELLIPSIS)) {
				return false;
			}
			int lo = 0;
			int hi = source.length();
			while (lo < hi) {
				int mid = (lo + hi + 1) >>> 1;
				if (SharpText.measure(style.weight, emPx, source.substring(0, mid).stripTrailing() + ELLIPSIS, trackPx) / scale <= maxWidth) {
					lo = mid;
				} else {
					hi = mid - 1;
				}
			}
			result = source.substring(0, lo).stripTrailing() + ELLIPSIS;
		}
		if (sharp == null) {
			sharp = new SharpLayout();
		}
		fitted = result;
		SharpText.layout(sharp, style.weight, emPx, scale, result, trackPx, false);
		width = SharpText.measure(style.weight, emPx, result, trackPx) / scale;
		sequence = FormattedCharSequence.EMPTY;
		glyphs = NO_GLYPHS;
		offsets = NO_OFFSETS;
		return true;
	}

	private float measure(Font font, Style fontStyle, float scale, String s) {
		float w = font.width(FormattedCharSequence.forward(s, fontStyle)) * scale;
		if (tracking != 0f && !s.isEmpty()) {
			w += tracking * (s.codePointCount(0, s.length()) - 1);
		}
		return w;
	}
}
