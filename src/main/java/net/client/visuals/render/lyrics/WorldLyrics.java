package net.client.visuals.render.lyrics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.element.LyricsElement;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.media.Lyrics;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.media.NowPlaying;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.Palette;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The Lyrics element's "In the World" mode: each line of the song appears in the air in front of you
 * as a sentence – laid out when the line starts, centred where you're looking, words spaced by their
 * real widths and wrapped into rows – and each word pops into its place as it's sung. Every new line
 * pushes all older ones up by its own height, so lines stack instead of overlapping however fast
 * they come, and a line fades once a second newer one has arrived. Words stay where they appeared,
 * so you can walk around them.
 *
 * <p>Drawn as billboarded world text (vanilla's text feature, like name tags) in the element's typeface
 * ({@link LyricFont}, with its capitals and letter-spacing), outlined in the highlight colour or, with
 * Outline on, in dark. A fixed pool of word particles; text and layout are built
 * when a line starts, never per frame. Everything is timed by the song position, so pausing freezes
 * the words and seeking starts over.</p>
 */
public final class WorldLyrics {
	private static final int POOL = 96;
	/** World units per layout unit at the default 18-unit text size (lyric faces are 30 units). */
	private static final float BASE_SCALE = 0.0125f;
	/** The em size and a word space, in layout units. */
	private static final float EM = 30f;
	private static final float SPACE = 9f;

	private static final double[] X = new double[POOL];
	private static final double[] Y = new double[POOL];
	private static final double[] Z = new double[POOL];
	private static final long[] BORN = new long[POOL];
	private static final long[] LINE_START = new long[POOL];
	private static final long[] LINE_END = new long[POOL];
	private static final int[] LINE = new int[POOL];
	private static final float[] HASH = new float[POOL];
	private static final float[] WIDTH = new float[POOL];
	private static final int[] INDEX = new int[POOL];
	/** Glyph runs of each word (one run, or one per character with letter-spacing) and their x in layout units. */
	private static final FormattedCharSequence[][] RUNS = new FormattedCharSequence[POOL][];
	private static final float[][] RUN_X = new float[POOL][];
	/** Layout units per font unit, and half the cap height in font units (the cap top sits at the origin). */
	private static final float[] UNITS = new float[POOL];
	private static final float[] HALF_CAP = new float[POOL];
	private static final boolean[] LIVE = new boolean[POOL];

	private static Lyrics.@Nullable Track track;
	private static int line = -1;
	private static int spawned;
	private static long lastPosition = Long.MIN_VALUE;
	private static int next;

	/** The current line, laid out: glyph runs, width (layout units) and world position of each word. */
	private static FormattedCharSequence[][] runs = new FormattedCharSequence[0][];
	private static float[][] runX = new float[0][];
	private static float[] widths = new float[0];
	private static float lineUnits = 1f;
	private static float lineHalfCap;
	private static double[] wordX = new double[0];
	private static double[] wordY = new double[0];
	private static double[] wordZ = new double[0];
	private static float lineLift;

	/** Recent lines (index, start, height plus gap in blocks): each pushes the older lines up as it arrives. */
	private static final int HISTORY = 8;
	private static final int[] HISTORY_LINE = new int[HISTORY];
	private static final long[] HISTORY_START = new long[HISTORY];
	private static final float[] HISTORY_LIFT = new float[HISTORY];
	private static int historyHead;
	private static int historySize;

	private WorldLyrics() {
	}

	/** Fabric {@code LevelRenderEvents.COLLECT_SUBMITS} (render thread). */
	public static void collect(LevelRenderContext context) {
		LyricsElement element = LyricsElement.instance();
		Minecraft minecraft = Minecraft.getInstance();
		if (element == null || !element.isEnabled() || element.display.index() != LyricsElement.WORLD
				|| HudManager.theme() == null || !HudManager.theme().isEnabled() || minecraft.player == null) {
			clear();
			return;
		}
		MediaBridge.request(element.anyPlayer());
		NowPlaying now = MediaBridge.current();
		Lyrics.update(now, element.online.get());
		Lyrics.Track t = Lyrics.track();
		if (t == null) {
			clear();
			return;
		}
		long position = element.position(now);
		CameraRenderState camera = context.levelState().cameraRenderState;
		if (t != track) {
			clear();
			track = t;
		} else if (lastPosition != Long.MIN_VALUE && (position < lastPosition - 400L || position > lastPosition + 2500L)) {
			clear(); // seeked: start over from here
			track = t;
		}
		boolean fresh = lastPosition == Long.MIN_VALUE;
		lastPosition = position;
		spawn(element, t, position, camera, fresh);
		draw(element, context, position, camera);
	}

	private static void clear() {
		for (int i = 0; i < POOL; i++) {
			LIVE[i] = false;
			RUNS[i] = null;
			RUN_X[i] = null;
		}
		track = null;
		line = -1;
		spawned = 0;
		lastPosition = Long.MIN_VALUE;
		historySize = 0;
	}

	private static void spawn(LyricsElement element, Lyrics.Track t, long position, CameraRenderState camera, boolean fresh) {
		int current = t.lineAt(position);
		if (current != line) {
			line = current;
			spawned = 0;
			if (current >= 0 && !t.blank(current)) {
				layout(element, t, current, camera);
				if (fresh) {
					// Joining mid-line: words sung a while ago don't pop in all at once.
					long[] starts = t.wordStarts[current];
					while (spawned < starts.length && starts[spawned] < position - 600L) {
						spawned++;
					}
				}
			}
		}
		if (current < 0 || t.blank(current)) {
			return;
		}
		long[] starts = t.wordStarts[current];
		while (spawned < starts.length && starts[spawned] <= position) {
			int i = spawned++;
			int slot = next;
			next = (next + 1) % POOL;
			X[slot] = wordX[i];
			Y[slot] = wordY[i];
			Z[slot] = wordZ[i];
			BORN[slot] = starts[i];
			LINE_START[slot] = t.starts[current];
			LINE_END[slot] = t.ends[current];
			LINE[slot] = current;
			HASH[slot] = hash(current * 131 + i * 17);
			WIDTH[slot] = widths[i];
			INDEX[slot] = current * 7 + i;
			RUNS[slot] = runs[i];
			RUN_X[slot] = runX[i];
			UNITS[slot] = lineUnits;
			HALF_CAP[slot] = lineHalfCap;
			LIVE[slot] = true;
		}
	}

	/**
	 * Lays the line out as a sentence on a vertical plane facing the camera: centred on the view
	 * direction (pitch softened so it stays near eye level), words left to right by their widths,
	 * wrapped into rows no wider than the distance.
	 */
	private static void layout(LyricsElement element, Lyrics.Track t, int index, CameraRenderState camera) {
		Font font = Minecraft.getInstance().font;
		LyricFont face = element.face();
		Style style = face.world;
		float units = face.style.worldUnits();
		float tracking = element.tracking();
		boolean upper = element.uppercase.get();
		lineUnits = units;
		lineHalfCap = face.style.worldCapHeight() * 0.5f;
		String[] text = t.words[index];
		int n = text.length;
		runs = new FormattedCharSequence[n][];
		runX = new float[n][];
		widths = new float[n];
		wordX = new double[n];
		wordY = new double[n];
		wordZ = new double[n];
		float scale = BASE_SCALE * element.textSize.getFloat() / 18f;
		for (int i = 0; i < n; i++) {
			String word = upper ? text[i].toUpperCase(Locale.ROOT) : text[i];
			if (tracking == 0f) {
				runs[i] = new FormattedCharSequence[]{FormattedCharSequence.forward(word, style)};
				runX[i] = new float[]{0f};
				widths[i] = font.width(runs[i][0]) * units;
				continue;
			}
			// Vanilla's text has no letter-spacing: one run per character.
			int count = word.codePointCount(0, word.length());
			runs[i] = new FormattedCharSequence[count];
			runX[i] = new float[count];
			float cursor = 0f;
			for (int c = 0, at = 0; c < count; c++) {
				int cp = word.codePointAt(at);
				int next = at + Character.charCount(cp);
				runs[i][c] = FormattedCharSequence.forward(word.substring(at, next), style);
				runX[i][c] = cursor;
				cursor += font.width(runs[i][c]) * units + tracking;
				at = next;
			}
			widths[i] = Math.max(0f, cursor - tracking);
		}
		double distance = element.worldDistance.get();
		double maxRow = Math.max(2.0, distance * 1.1) / scale; // in font units
		// Rows as word ranges [rowStart[r], rowStart[r + 1]) with their widths.
		int[] rowStart = new int[n + 1];
		float[] rowWidth = new float[n + 1];
		int rows = 0;
		float w = 0f;
		for (int i = 0; i < n; i++) {
			boolean first = i == rowStart[rows];
			if (!first && w + SPACE + widths[i] > maxRow) {
				rowWidth[rows] = w;
				rows++;
				rowStart[rows] = i;
				w = widths[i];
			} else {
				w += (first ? 0f : SPACE) + widths[i];
			}
		}
		rowWidth[rows] = w;
		rows++;
		rowStart[rows] = n;

		float yaw = camera.yRot * Mth.DEG_TO_RAD;
		float pitch = Math.clamp(camera.xRot, -30f, 30f) * 0.7f * Mth.DEG_TO_RAD;
		double cosPitch = Math.cos(pitch);
		double cx = camera.pos.x - Math.sin(yaw) * cosPitch * distance;
		double cy = camera.pos.y - Math.sin(pitch) * distance;
		double cz = camera.pos.z + Math.cos(yaw) * cosPitch * distance;
		// The player's right, horizontally.
		double rightX = -Math.cos(yaw);
		double rightZ = -Math.sin(yaw);
		float rowHeight = EM * 1.25f;
		for (int r = 0; r < rows; r++) {
			float x = -rowWidth[r] * 0.5f;
			float y = ((rows - 1) * 0.5f - r) * rowHeight;
			for (int i = rowStart[r]; i < rowStart[r + 1]; i++) {
				float center = x + widths[i] * 0.5f;
				wordX[i] = cx + rightX * center * scale;
				wordY[i] = cy + y * scale;
				wordZ[i] = cz + rightZ * center * scale;
				x += widths[i] + SPACE;
			}
		}
		lineLift = (rows * rowHeight + EM * 0.6f) * scale;
		HISTORY_LINE[historyHead] = index;
		HISTORY_START[historyHead] = t.starts[index];
		HISTORY_LIFT[historyHead] = lineLift;
		historyHead = (historyHead + 1) % HISTORY;
		historySize = Math.min(HISTORY, historySize + 1);
	}

	private static void draw(LyricsElement element, LevelRenderContext context, long position, CameraRenderState camera) {
		SubmitNodeCollector collector = context.submitNodeCollector();
		PoseStack pose = context.poseStack();
		float scale = BASE_SCALE * element.textSize.getFloat() / 18f;
		float glow = element.glow.getFloat() / 100f;
		int accent = HudManager.theme() != null ? ColorUtil.hsv(HudManager.theme().accentHue.getFloat(), 0.82f, 1f) : Palette.ACCENT_BLUE;
		int mode = element.color.index();
		boolean outlined = element.outline.get();
		float time = (System.nanoTime() % 3_600_000_000_000L) / 1.0e9f;
		for (int i = 0; i < POOL; i++) {
			if (!LIVE[i]) {
				continue;
			}
			float age = (position - BORN[i]) / 1000f;
			float lineAge = (position - LINE_START[i]) / 1000f;
			float endAge = (position - LINE_END[i]) / 1000f;
			// Newer lines that have arrived push this one up; the second one sends it away.
			float push = 0f;
			float stack = 0f;
			for (int h = 0; h < historySize; h++) {
				if (HISTORY_LINE[h] > LINE[i]) {
					float arrive = easeOut(Math.clamp((position - HISTORY_START[h]) / 450f, 0f, 1f));
					push += arrive * HISTORY_LIFT[h];
					stack += arrive;
				}
			}
			float fade = Math.min(endAge < 1.2f ? 1f : 1f - smoothstep(1.2f, 2.6f, endAge), 1f - smoothstep(1f, 1.8f, stack));
			if (fade <= 0.01f || age > 20f) {
				LIVE[i] = false;
				RUNS[i] = null;
				RUN_X[i] = null;
				continue;
			}
			if (age < 0f) {
				continue;
			}
			float in = backOut(Math.min(1f, age / 0.4f));
			float k = scale * (0.55f + 0.45f * in);
			float alpha = Math.min(1f, age / 0.12f) * fade;
			// The whole line drifts together and is pushed up by every newer line.
			double rise = lineAge * 0.05 + push - (1f - Math.min(1f, in)) * 0.06
					+ Math.sin(age * 1.6f + HASH[i] * 6.283f) * 0.015;
			int highlight = switch (mode) {
				case 0 -> AlbumArt.accent(accent);
				case 1 -> accent;
				case 2 -> Palette.TEXT_PRIMARY;
				default -> ColorUtil.hsv(time * 40f + INDEX[i] * 37f, 0.6f, 1f);
			};
			int color = ColorUtil.fade(ColorUtil.lerpRgb(highlight, 0xFFFFFFFF, Math.min(1f, age / 0.8f) * 0.85f), Math.max(alpha, 0.1f));
			int outline = outlined ? ColorUtil.fade(0xFF000000, alpha * 0.85f)
					: glow > 0.01f ? ColorUtil.fade(ColorUtil.lerpRgb(0xFF000000, highlight, 0.35f + 0.4f * glow), alpha) : 0;
			float units = UNITS[i];
			float ku = k * units;
			pose.pushPose();
			pose.translate(X[i] - camera.pos.x, Y[i] + rise - camera.pos.y, Z[i] - camera.pos.z);
			pose.rotate(camera.orientation);
			pose.scale(ku, -ku, ku);
			FormattedCharSequence[] wordRuns = RUNS[i];
			float[] xs = RUN_X[i];
			float left = -WIDTH[i] * 0.5f;
			for (int r = 0; r < wordRuns.length; r++) {
				collector.submitText(pose, (left + xs[r]) / units, -HALF_CAP[i], wordRuns[r], false, Font.DisplayMode.NORMAL,
						LightCoordsUtil.FULL_BRIGHT, color, 0, outline);
			}
			pose.popPose();
		}
	}

	private static float easeOut(float t) {
		float u = 1f - t;
		return 1f - u * u * u;
	}

	private static float backOut(float t) {
		float c1 = 1.70158f;
		float c3 = c1 + 1f;
		float u = t - 1f;
		return 1f + c3 * u * u * u + c1 * u * u;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}

	private static float hash(int n) {
		int h = n * 0x45D9F3B;
		h = (h ^ (h >>> 16)) * 0x45D9F3B;
		h ^= h >>> 16;
		return (h & 0xFFFF) / 65535f;
	}
}
