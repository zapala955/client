package net.client.visuals.render;

import net.client.visuals.LiquidConfig;
import net.client.visuals.render.text.SharpText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Multi-colour text from a chat {@link Component} (server MOTDs, player lists), drawn with the sharp
 * Inter renderer. Minecraft's own renderer handles colours per glyph; {@link TextLabel}s are single
 * colour, so the component is split into same-style runs, word-wrapped to {@code maxLines} lines
 * and laid out once. It's rebuilt only when the component, width or GUI scale changes, so drawing
 * allocates nothing.
 *
 * <ul>
 *   <li>Legacy {@code §} codes inside literal text are honoured, like vanilla's string decomposer.</li>
 *   <li>Colours too dark for the dark glass are lifted (same hue) so {@code §0}/{@code §1} stay legible.</li>
 *   <li>Bold uses the semibold weight; obfuscated text (decorative noise) is left out; each line is
 *       trimmed, since MOTDs pad with spaces to centre themselves in vanilla's list.</li>
 * </ul>
 */
public final class StyledText {
	private static final int DEFAULT = 0;

	private final LiquidText.TextStyle style;
	private final LiquidText.TextStyle boldStyle;
	private final int maxLines;
	private final List<TextLabel> labels = new ArrayList<>();
	private final List<Run> runs = new ArrayList<>();
	private @Nullable Component source;
	private float laidOutWidth = -1f;
	private int laidOutKey = Integer.MIN_VALUE;
	private int lineCount;

	private record Run(TextLabel label, int color, float x, int line) {
	}

	private record Piece(String text, int color, boolean bold) {
	}

	public StyledText(LiquidText.TextStyle style, LiquidText.TextStyle boldStyle, int maxLines) {
		this.style = style;
		this.boldStyle = boldStyle;
		this.maxLines = maxLines;
	}

	/** Lays out {@code text} for {@code maxWidth}; cheap when nothing changed. */
	public StyledText set(Component text, float maxWidth) {
		int key = (LiquidConfig.customFont ? 1 : 0) | HighDpiEngine.scale() << 1 | SharpText.generation() << 5;
		if (text == source && maxWidth == laidOutWidth && key == laidOutKey) {
			return this;
		}
		source = text;
		laidOutWidth = maxWidth;
		laidOutKey = key;
		rebuild(text, maxWidth);
		return this;
	}

	public int lines() {
		return lineCount;
	}

	public boolean isEmpty() {
		return runs.isEmpty();
	}

	/**
	 * @param defaultColor colour of unstyled text (its alpha also applies to styled runs)
	 */
	public void draw(GuiGraphicsExtractor g, float x, float y, float lineHeight, int defaultColor) {
		float alpha = (defaultColor >>> 24) / 255f;
		for (int i = 0; i < runs.size(); i++) {
			Run run = runs.get(i);
			int color = run.color == DEFAULT ? defaultColor : ColorUtil.fade(run.color, alpha);
			LiquidText.draw(g, run.label, x + run.x, y + run.line * lineHeight, color, false);
		}
	}

	// ---- Layout --------------------------------------------------------------------------------

	private void rebuild(Component text, float maxWidth) {
		runs.clear();
		labels.clear();
		lineCount = 0;
		List<List<Piece>> lines = split(text);
		TextLabel probe = new TextLabel("", style);
		TextLabel boldProbe = new TextLabel("", boldStyle);
		int line = 0;
		for (int li = 0; li < lines.size() && line < maxLines; li++) {
			List<Piece> pieces = trim(lines.get(li));
			if (pieces.isEmpty()) {
				continue;
			}
			float cursor = 0f;
			boolean truncated = false;
			for (int pi = 0; pi < pieces.size() && !truncated; pi++) {
				Piece piece = pieces.get(pi);
				TextLabel measure = piece.bold ? boldProbe : probe;
				StringBuilder run = new StringBuilder();
				for (String word : words(piece.text)) {
					measure.setText(run + word);
					if (cursor + measure.width() <= maxWidth || (cursor == 0f && run.isEmpty())) {
						run.append(word);
						continue;
					}
					// The word doesn't fit: flush the run, then wrap or truncate.
					if (!run.isEmpty()) {
						cursor += emit(run.toString().stripTrailing(), piece, cursor, line, maxWidth);
					}
					if (line + 1 >= maxLines) {
						appendEllipsis(piece, cursor, line, maxWidth, word);
						truncated = true;
						break;
					}
					line++;
					cursor = 0f;
					run.setLength(0);
					run.append(word.stripLeading());
				}
				if (!truncated && !run.isEmpty()) {
					cursor += emit(run.toString(), piece, cursor, line, maxWidth);
				}
			}
			line++;
			if (truncated) {
				break;
			}
		}
		lineCount = Math.min(line, maxLines);
	}

	private float emit(String text, Piece piece, float x, int line, float maxWidth) {
		if (text.isEmpty()) {
			return 0f;
		}
		TextLabel label = new TextLabel(text, piece.bold ? boldStyle : style).maxWidth(Math.max(4f, maxWidth - x));
		labels.add(label);
		runs.add(new Run(label, piece.color, x, line));
		return label.width();
	}

	/** Last line overflows: what still fits of the next word, cut with an ellipsis. */
	private void appendEllipsis(Piece piece, float x, int line, float maxWidth, String word) {
		float room = maxWidth - x;
		if (room > 10f) {
			emit(word.stripLeading() + "…", piece, x, line, maxWidth);
		}
	}

	/** Words with their trailing space attached, so runs keep the original spacing. */
	private static List<String> words(String text) {
		List<String> out = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == ' ' && (i + 1 == text.length() || text.charAt(i + 1) != ' ')) {
				out.add(text.substring(start, i + 1));
				start = i + 1;
			}
		}
		if (start < text.length()) {
			out.add(text.substring(start));
		}
		return out;
	}

	private static List<Piece> trim(List<Piece> line) {
		List<Piece> out = new ArrayList<>(line);
		while (!out.isEmpty()) {
			Piece first = out.getFirst();
			String stripped = first.text.stripLeading();
			if (stripped.isEmpty()) {
				out.removeFirst();
			} else {
				out.set(0, new Piece(stripped, first.color, first.bold));
				break;
			}
		}
		while (!out.isEmpty()) {
			Piece last = out.getLast();
			String stripped = last.text.stripTrailing();
			if (stripped.isEmpty()) {
				out.removeLast();
			} else {
				out.set(out.size() - 1, new Piece(stripped, last.color, last.bold));
				break;
			}
		}
		return out;
	}

	// ---- Component flattening -------------------------------------------------------------------

	/** Flattens the component into lines of same-style pieces, applying legacy § codes. */
	private static List<List<Piece>> split(Component text) {
		List<List<Piece>> lines = new ArrayList<>();
		lines.add(new ArrayList<>());
		text.visit((style, content) -> {
			decompose(style, content, lines);
			return Optional.empty();
		}, Style.EMPTY);
		return lines;
	}

	private static void decompose(Style base, String content, List<List<Piece>> lines) {
		Style current = base;
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '§' && i + 1 < content.length()) {
				flush(buffer, current, lines);
				ChatFormatting format = ChatFormatting.getByCode(content.charAt(++i));
				if (format == ChatFormatting.RESET) {
					current = base;
				} else if (format != null) {
					current = current.applyLegacyFormat(format);
				}
				continue;
			}
			if (c == '\n') {
				flush(buffer, current, lines);
				lines.add(new ArrayList<>());
				continue;
			}
			buffer.append(c);
		}
		flush(buffer, current, lines);
	}

	private static void flush(StringBuilder buffer, Style style, List<List<Piece>> lines) {
		if (buffer.isEmpty()) {
			return;
		}
		if (!style.isObfuscated()) {
			TextColor color = style.getColor();
			int argb = color == null ? DEFAULT : legible(0xFF000000 | color.getValue());
			lines.getLast().add(new Piece(buffer.toString(), argb, style.isBold()));
		}
		buffer.setLength(0);
	}

	/** Lifts colours too dark to read on the smoked glass, keeping their hue. */
	static int legible(int argb) {
		float r = (argb >> 16 & 0xFF) / 255f;
		float g = (argb >> 8 & 0xFF) / 255f;
		float b = (argb & 0xFF) / 255f;
		float luma = 0.2126f * r + 0.7152f * g + 0.0722f * b;
		if (luma >= 0.42f) {
			return argb;
		}
		return ColorUtil.lerpRgb(argb, 0xFFFFFFFF, Math.min(0.75f, (0.42f - luma) * 1.35f));
	}
}
