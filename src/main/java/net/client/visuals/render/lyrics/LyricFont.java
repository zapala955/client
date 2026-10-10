package net.client.visuals.render.lyrics;

import net.client.visuals.render.LiquidText.TextStyle;
import net.minecraft.network.chat.Style;

/**
 * The Lyrics element's typefaces: ten families × four styles. A family without a real bold or italic
 * cut gets a synthesised one, so every combination works: a slanted pose or a second, slightly offset
 * pass in the HUD, and vanilla's italic / bold flags for world text.
 */
public final class LyricFont {
	public static final String[] FAMILIES = {
			"modern", "elegant", "script", "poster", "rounded", "mono", "marker", "futuristic", "handwriting", "pixel"
	};
	public static final String[] STYLES = {"regular", "bold", "italic", "bold_italic"};
	public static final int MODERN = 0;
	public static final int BOLD = 1;
	/** Slant of a synthesised italic: x shift per unit of height. */
	public static final float SLANT = 0.2f;
	/** Offset of a synthesised bold's second pass, in lyric em units (30 per em). */
	public static final float EMBOLDEN = 0.9f;

	private static final LyricFont[] TABLE = build();

	public final TextStyle style;
	/** Italic wanted, but the face has none: slant it. */
	public final boolean slant;
	/** Bold wanted, but the face has none: draw it twice, offset. */
	public final boolean embolden;
	/** World text: the face's vanilla definition with vanilla's italic / bold standing in for missing cuts. */
	public final Style world;

	private LyricFont(TextStyle style, boolean italic, boolean bold) {
		this.style = style;
		this.slant = italic && !style.italic();
		this.embolden = bold && !style.bold();
		Style w = style.worldStyle();
		if (slant) {
			w = w.withItalic(true);
		}
		if (embolden) {
			w = w.withBold(true);
		}
		this.world = w;
	}

	public static LyricFont of(int family, int style) {
		int f = Math.clamp(family, 0, FAMILIES.length - 1);
		int s = Math.clamp(style, 0, STYLES.length - 1);
		return TABLE[f * STYLES.length + s];
	}

	private static LyricFont[] build() {
		TextStyle[][] faces = {
				{TextStyle.LYRIC_MODERN, TextStyle.LYRIC_MODERN_BOLD, TextStyle.LYRIC_MODERN, TextStyle.LYRIC_MODERN_BOLD},
				{TextStyle.LYRIC_ELEGANT, TextStyle.LYRIC_ELEGANT_BOLD, TextStyle.LYRIC_ELEGANT_ITALIC, TextStyle.LYRIC_ELEGANT_BOLD_ITALIC},
				{TextStyle.LYRIC_SCRIPT, TextStyle.LYRIC_SCRIPT_BOLD, TextStyle.LYRIC_SCRIPT, TextStyle.LYRIC_SCRIPT_BOLD},
				{TextStyle.LYRIC_POSTER, TextStyle.LYRIC_POSTER, TextStyle.LYRIC_POSTER, TextStyle.LYRIC_POSTER},
				{TextStyle.LYRIC_ROUNDED, TextStyle.LYRIC_ROUNDED_BOLD, TextStyle.LYRIC_ROUNDED_ITALIC, TextStyle.LYRIC_ROUNDED_BOLD},
				{TextStyle.LYRIC_MONO, TextStyle.LYRIC_MONO_BOLD, TextStyle.LYRIC_MONO_ITALIC, TextStyle.LYRIC_MONO_BOLD},
				{TextStyle.LYRIC_MARKER, TextStyle.LYRIC_MARKER, TextStyle.LYRIC_MARKER, TextStyle.LYRIC_MARKER},
				{TextStyle.LYRIC_FUTURISTIC, TextStyle.LYRIC_FUTURISTIC_BOLD, TextStyle.LYRIC_FUTURISTIC, TextStyle.LYRIC_FUTURISTIC_BOLD},
				{TextStyle.LYRIC_HANDWRITING, TextStyle.LYRIC_HANDWRITING_BOLD, TextStyle.LYRIC_HANDWRITING, TextStyle.LYRIC_HANDWRITING_BOLD},
				{TextStyle.LYRIC_PIXEL, TextStyle.LYRIC_PIXEL_BOLD, TextStyle.LYRIC_PIXEL_ITALIC, TextStyle.LYRIC_PIXEL_BOLD_ITALIC},
		};
		LyricFont[] table = new LyricFont[FAMILIES.length * STYLES.length];
		for (int f = 0; f < FAMILIES.length; f++) {
			for (int s = 0; s < STYLES.length; s++) {
				table[f * STYLES.length + s] = new LyricFont(faces[f][s], s >= 2, (s & 1) == 1);
			}
		}
		return table;
	}
}
