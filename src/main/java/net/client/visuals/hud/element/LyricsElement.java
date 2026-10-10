package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.media.Lyrics;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.media.NowPlaying;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.lyrics.LyricFont;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Lyrics of the song playing on the PC, in time with it (see {@code media.Lyrics}).
 *
 * <ul>
 *   <li><b>Karaoke</b> – the line being sung, words filling with light as they're sung (with a glow),
 *       the next line waiting below, lines gliding up as they change.</li>
 *   <li><b>Floating Words</b> – each word pops in as it's sung and drifts up like a particle,
 *       swaying, then dissolves.</li>
 *   <li><b>Glass Bubbles</b> – each word in a Liquid Glass bubble that floats up and pops.</li>
 *   <li><b>Wave</b> – the whole line, every word bouncing as it's sung.</li>
 *   <li><b>Cinema</b> – a calm subtitle on a dark pill, line by line.</li>
 *   <li><b>In the World</b> – words appear in the air in front of you as 3D text and float away
 *       ({@code render.lyrics.WorldLyrics}).</li>
 * </ul>
 *
 * <p>Ten typefaces in four styles ({@link LyricFont}), with optional capitals, letter-spacing and
 * an outline, apply to every display, the 3D one included.</p>
 *
 * <p>Every word's animation is a function of the song position, not of frame-to-frame state, so
 * pausing freezes it and seeking or skipping lines up instantly. Labels are re-laid out only when a
 * line changes. The player source follows the Music element, so the two never fight over it.</p>
 */
public final class LyricsElement extends HudModule {
	public static final String[] DISPLAYS = {"karaoke", "floating", "bubbles", "wave", "cinema", "world"};
	public static final int KARAOKE = 0;
	public static final int FLOATING = 1;
	public static final int BUBBLES = 2;
	public static final int WAVE = 3;
	public static final int CINEMA = 4;
	public static final int WORLD = 5;
	public static final String[] COLORS = {"album", "accent", "white", "rainbow"};

	/** Lyric faces have a 30-unit em; words are drawn scaled to the text size. */
	private static final float FACE = LiquidText.LYRIC_EM;
	/** Synthesised italic: a shear about the word's centre. */
	private static final Matrix3x2f SLANT = new Matrix3x2f(1f, 0f, -LyricFont.SLANT, 1f, 0f, 0f);
	/** Outline taps: unit offsets around the glyphs. */
	private static final float[] OUTLINE_TAPS = {
			1f, 0f, -1f, 0f, 0f, 1f, 0f, -1f,
			0.7071f, 0.7071f, -0.7071f, 0.7071f, 0.7071f, -0.7071f, -0.7071f, -0.7071f,
	};
	private static final int SLOTS = 6;
	/** Space between words, as a fraction of the text size (bubbles need room for their capsules). */
	private static final float WORD_GAP = 0.3f;
	private static final float BUBBLE_GAP = 0.95f;
	/**
	 * Words show this much before their time: the pop-in takes a moment, and lyric timestamps mark
	 * when singing starts.
	 */
	private static final long LEAD_MS = 150L;

	private static @Nullable LyricsElement instance;

	public final ModeSetting display = add(new ModeSetting("display", "liquidclient.hud.lyrics.display", DISPLAYS, KARAOKE));
	public final NumberSetting textSize = add(new NumberSetting("text_size", "liquidclient.hud.lyrics.text_size", 10, 36, 1, 18));
	public final ModeSetting typeface = add(new ModeSetting("font", "liquidclient.hud.lyrics.font", LyricFont.FAMILIES, LyricFont.MODERN));
	public final ModeSetting fontStyle = add(new ModeSetting("font_style", "liquidclient.hud.lyrics.font_style", LyricFont.STYLES, LyricFont.BOLD));
	public final BooleanSetting uppercase = add(new BooleanSetting("uppercase", "liquidclient.hud.lyrics.uppercase", false));
	public final NumberSetting letterSpacing = add(new NumberSetting("letter_spacing", "liquidclient.hud.lyrics.letter_spacing", -5, 30, 1, 0)
			.suffix("%"));
	public final BooleanSetting outline = add(new BooleanSetting("outline", "liquidclient.hud.lyrics.outline", false));
	public final ModeSetting color = add(new ModeSetting("color", "liquidclient.hud.lyrics.color", COLORS, 0));
	public final NumberSetting glow = add(new NumberSetting("glow", "liquidclient.hud.lyrics.glow", 0, 100, 1, 60).suffix("%"));
	public final BooleanSetting nextLine = add(new BooleanSetting("next_line", "liquidclient.hud.lyrics.next_line", true));
	public final NumberSetting offset = add(new NumberSetting("offset", "liquidclient.hud.lyrics.offset", -3000, 3000, 50, 0)
			.format(v -> String.format(Locale.ROOT, "%+.2f s", v / 1000.0)));
	public final NumberSetting worldDistance = add(new NumberSetting("world_distance", "liquidclient.hud.lyrics.world_distance", 2, 12, 0.5, 5)
			.suffix(" blocks"));
	public final BooleanSetting online = add(new BooleanSetting("online", "liquidclient.hud.lyrics.online", true));
	public final BooleanSetting hideIdle = add(new BooleanSetting("hide_idle", "liquidclient.hud.lyrics.hide_idle", true));

	private final Slot[] slots = new Slot[SLOTS];
	private final Smooth change = new Smooth(1f, 9f);
	private final TextLabel statusLoading = new TextLabel("♪  Finding lyrics…", LiquidText.TextStyle.HEADLINE);
	private final TextLabel statusMissing = new TextLabel("No synced lyrics for this song", LiquidText.TextStyle.HEADLINE);
	private final TextLabel statusInstrumental = new TextLabel("♪  Instrumental", LiquidText.TextStyle.HEADLINE);
	private final TextLabel statusOffline = new TextLabel("Lyrics lookup is off", LiquidText.TextStyle.HEADLINE);
	private final TextLabel statusError = new TextLabel("Couldn't reach the lyrics service", LiquidText.TextStyle.HEADLINE);
	private final TextLabel statusIdle = new TextLabel("♪", LiquidText.TextStyle.HEADLINE);
	private @Nullable MusicElement music;

	private Lyrics.@Nullable Track track;
	private long position;
	private int line = -1;
	private int previous = -1;
	private Lyrics.@Nullable Track shownTrack;
	private int shownLine = Integer.MIN_VALUE;
	private boolean active;
	private long frame;
	private LyricFont face = LyricFont.of(LyricFont.MODERN, LyricFont.BOLD);

	public LyricsElement() {
		super("hud_lyrics", false, 0.5f, 0.78f);
		for (int i = 0; i < SLOTS; i++) {
			slots[i] = new Slot();
		}
		instance = this;
	}

	public static @Nullable LyricsElement instance() {
		return instance;
	}

	/** Whether the Music element is set to any player (else Spotify only); shared so the bridge isn't toggled. */
	public boolean anyPlayer() {
		MusicElement m = music;
		if (m == null) {
			m = music = ModuleManager.get(MusicElement.class);
		}
		return m.source.index() == 1;
	}

	/** The chosen typeface and style. */
	public LyricFont face() {
		return LyricFont.of(typeface.index(), fontStyle.index());
	}

	/** Letter-spacing in lyric em units (30 per em). */
	public float tracking() {
		return letterSpacing.getFloat() / 100f * FACE;
	}

	/** Song position for lyrics: the player's, extrapolated, plus the sync offset. */
	public long position(NowPlaying now) {
		return now.positionAt(System.currentTimeMillis()) + LEAD_MS + Math.round(offset.get());
	}

	// ---- Layout -------------------------------------------------------------------------------

	@Override
	public void layout(HudContext ctx) {
		frame++;
		face = face();
		MediaBridge.request(anyPlayer());
		NowPlaying now = MediaBridge.current();
		Lyrics.update(now, online.get());
		Lyrics.Track real = Lyrics.track();
		if (real == null && ctx.editing) {
			track = Lyrics.demo();
			position = (long) (ctx.time * 1000f) % Lyrics.DEMO_LENGTH;
		} else {
			track = real;
			position = real != null ? position(now) : 0L;
		}
		Lyrics.Track t = track;
		line = t != null ? t.lineAt(position) : -1;
		if (t != shownTrack || line != shownLine) {
			previous = t == shownTrack ? shownLine : -1;
			shownTrack = t;
			shownLine = line;
			change.snap(0f);
		}
		change.speed(Motion.speed(9f));
		change.target(1f).update(ctx.dt);

		float size = textSize.getFloat();
		width = size * 17f;
		height = switch (mode(ctx)) {
			case FLOATING, BUBBLES -> size * 4.6f;
			case CINEMA -> size * 2.1f;
			default -> size * 2.7f + (nextLine.get() ? size * 0.95f : 0f);
		};
		active = t != null && singing(t);
	}

	/** The display style; "In the World" previews as Floating Words in the HUD editor. */
	private int mode(HudContext ctx) {
		int m = display.index();
		return m == WORLD && ctx.editing ? FLOATING : m;
	}

	/** Something is (about to be) sung: hide during long gaps and instrumental parts. */
	private boolean singing(Lyrics.Track t) {
		if (line >= 0 && !t.blank(line) && position < t.ends[line] + 2500L) {
			return true;
		}
		int next = line + 1;
		return next < t.size() && !t.blank(next) && t.starts[next] - position < 1200L;
	}

	@Override
	public boolean visible(HudContext ctx) {
		if (ctx.editing) {
			return true;
		}
		if (display.index() == WORLD) {
			return false;
		}
		return active || !hideIdle.get() && MediaBridge.current().active;
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		Lyrics.Track t = track;
		if (t == null || !active && !ctx.editing) {
			status(g);
			return;
		}
		switch (mode(ctx)) {
			case FLOATING -> floating(g, ctx, t, false);
			case BUBBLES -> floating(g, ctx, t, true);
			case WAVE -> lines(g, ctx, t, true);
			case CINEMA -> cinema(g, t);
			default -> lines(g, ctx, t, false);
		}
	}

	private void status(GuiGraphicsExtractor g) {
		TextLabel label = switch (Lyrics.status()) {
			case LOADING -> statusLoading;
			case NOT_FOUND, UNSYNCED -> statusMissing;
			case INSTRUMENTAL -> statusInstrumental;
			case ERROR -> statusError;
			case IDLE -> online.get() ? statusIdle : statusOffline;
			default -> statusIdle;
		};
		LiquidText.drawCenteredSoft(g, label, width * 0.5f, height * 0.5f - label.capHeight() * 0.5f, 0xD9FFFFFF);
	}

	/** Karaoke and Wave: the line being sung, the previous one leaving upwards, the next one waiting. */
	private void lines(GuiGraphicsExtractor g, HudContext ctx, Lyrics.Track t, boolean wave) {
		float size = textSize.getFloat();
		float e = easeOut(change.get());
		float top = size * 0.35f;
		float cx = width * 0.5f;
		float rowH = size * 1.3f;

		if (previous >= 0 && previous < t.size() && !t.blank(previous) && e < 1f) {
			Slot prev = slot(t, previous, size);
			float y = top - e * (prev.height + size * 0.2f);
			drawStatic(g, prev, cx, y, (1f - e) * 0.55f, 0.9f + 0.1f * (1f - e));
		}
		if (line >= 0 && !t.blank(line)) {
			Slot cur = slot(t, line, size);
			float y = top + (1f - e) * rowH * 0.55f;
			drawSung(g, ctx, t, cur, cx, y, e, wave);
			if (nextLine.get() && line + 1 < t.size() && !t.blank(line + 1)) {
				Slot next = slot(t, line + 1, size * 0.62f);
				float ny = top + Math.max(cur.height, rowH) + size * 0.45f + (1f - e) * rowH * 0.4f;
				drawStatic(g, next, cx, ny, 0.42f * e, 1f);
			}
		} else if (line + 1 < t.size() && !t.blank(line + 1)) {
			// Intro: show what comes first, dimmed.
			Slot next = slot(t, line + 1, size);
			drawStatic(g, next, cx, top, 0.4f, 1f);
		}
	}

	/** The current line: dim words light up as they're sung (karaoke sweep, or a bounce in wave mode). */
	private void drawSung(GuiGraphicsExtractor g, HudContext ctx, Lyrics.Track t, Slot slot, float cx, float top, float appear, boolean wave) {
		float size = slot.size;
		float s = size / FACE;
		float capH = face.style.capHeight() * s;
		long[] starts = t.wordStarts[line];
		float glowStrength = glow.getFloat() / 100f;
		for (int i = 0; i < slot.count; i++) {
			float p = wordProgress(t, starts, i);
			float x = cx + slot.x[i];
			float y = top + slot.y[i];
			float w = slot.w[i];
			int highlight = highlight(ctx, i);
			float bounce = (float) Math.sin(Math.min(1f, p * 1.6f) * Math.PI);
			float lift = wave ? bounce * size * 0.3f : (p > 0f && p < 1f ? (float) Math.sin(p * Math.PI) * size * 0.05f : 0f);
			float scale = wave ? 1f + bounce * 0.1f : 1f;
			float wordAlpha = appear;
			if (p > 0f && glowStrength > 0f) {
				float g0 = Math.min(1f, p * 3f) * glowStrength * wordAlpha * (wave ? 0.3f + 0.4f * bounce : 0.32f);
				RenderUtils.glow(g, x - size * 0.2f, y - lift - size * 0.15f, w + size * 0.4f, capH + size * 0.3f, size * 0.3f, size * 0.55f,
						ColorUtil.fade(highlight, g0));
			}
			int dim = ColorUtil.fade(0xFFFFFFFF, 0.42f * wordAlpha);
			if (wave) {
				int lit = ColorUtil.fade(ColorUtil.lerpRgb(0xFFFFFFFF, highlight, bounce * 0.6f), wordAlpha);
				drawWord(g, slot.labels[i], x + w * 0.5f, y + capH * 0.5f - lift, s * scale, 0f, p > 0f ? lit : dim, true);
				continue;
			}
			drawWord(g, slot.labels[i], x + w * 0.5f, y + capH * 0.5f - lift, s, 0f, dim, true);
			if (p > 0f) {
				// The sung part, revealed left to right.
				int lit = ColorUtil.fade(ColorUtil.lerpRgb(0xFFFFFFFF, highlight, 0.18f), wordAlpha);
				if (p < 1f) {
					g.enableScissor((int) Math.floor(x - size * 0.1f), (int) Math.floor(y - lift - size * 0.5f),
							(int) Math.ceil(x + w * p), (int) Math.ceil(y - lift + capH + size * 0.5f));
					drawWord(g, slot.labels[i], x + w * 0.5f, y + capH * 0.5f - lift, s, 0f, lit, false);
					g.disableScissor();
				} else {
					drawWord(g, slot.labels[i], x + w * 0.5f, y + capH * 0.5f - lift, s, 0f, lit, false);
				}
			}
		}
	}

	/** 0 before the word, 1 once it's been sung, gliding in between. */
	private float wordProgress(Lyrics.Track t, long[] starts, int i) {
		long start = starts[i];
		long end = i + 1 < starts.length ? starts[i + 1] : Math.max(start + 250L, Math.min(t.ends[line], start + 900L));
		return Math.clamp((position - start) / (float) Math.max(1L, end - start), 0f, 1f);
	}

	/** A whole line at one opacity (previous / next lines). */
	private void drawStatic(GuiGraphicsExtractor g, Slot slot, float cx, float top, float alpha, float scale) {
		if (alpha <= 0.01f) {
			return;
		}
		float s = slot.size / FACE;
		float capH = face.style.capHeight() * s;
		int c = ColorUtil.fade(0xFFFFFFFF, alpha);
		for (int i = 0; i < slot.count; i++) {
			float x = cx + slot.x[i] * scale;
			drawWord(g, slot.labels[i], x + slot.w[i] * scale * 0.5f, top + slot.y[i] * scale + capH * scale * 0.5f, s * scale, 0f, c, true);
		}
	}

	/**
	 * Floating Words and Glass Bubbles: the newest line sits at the bottom; each word pops in as it's
	 * sung. Lines move as a whole – a slow drift up, and when a new line arrives it pushes the older
	 * ones up by its own height – so sentences stay readable and never overlap, then fade (or pop).
	 * Everything follows from song time.
	 */
	private void floating(GuiGraphicsExtractor g, HudContext ctx, Lyrics.Track t, boolean bubbles) {
		float size = textSize.getFloat();
		float s = size / FACE;
		float capH = face.style.capHeight() * s;
		float cx = width * 0.5f;
		float glowStrength = glow.getFloat() / 100f;
		float gap = bubbles ? BUBBLE_GAP : WORD_GAP;
		float lineGap = size * (bubbles ? 0.7f : 0.4f);
		float push = 0f;
		for (int l = line; l >= Math.max(0, line - 3); l--) {
			if (t.blank(l)) {
				continue;
			}
			Slot slot = slot(t, l, size, gap);
			float lineAge = (position - t.starts[l]) / 1000f;
			float endAge = (position - t.ends[l]) / 1000f;
			float baseY = height - slot.height - size * 0.4f - push - lineAge * size * 0.06f;
			// Older lines make room for this one as it arrives.
			push += easeOut(Math.clamp(lineAge / 0.45f, 0f, 1f)) * (slot.height + lineGap);
			float pop = bubbles ? Math.clamp((endAge - 0.9f) / 0.3f, 0f, 1f) : 0f;
			float fade = bubbles ? 1f - pop : 1f - smoothstep(0.8f, 2f, endAge);
			if (fade <= 0.01f) {
				continue;
			}
			long[] starts = t.wordStarts[l];
			for (int i = 0; i < slot.count; i++) {
				float age = (position - starts[i]) / 1000f;
				if (age < 0f) {
					continue;
				}
				float h = hash(l * 131 + i * 17);
				float in = backOut(Math.min(1f, age / 0.4f));
				float scale = s * (0.55f + 0.45f * in) * (1f + pop * 0.3f);
				float alpha = Math.min(1f, age / 0.1f) * fade;
				float bob = (float) Math.sin(age * (bubbles ? 2.1f : 1.7f) + h * 6.283f) * size * (bubbles ? 0.05f : 0.03f);
				float rotation = bubbles ? 0f : (h - 0.5f) * 0.1f * (1f - Math.min(1f, age / 1f));
				float wx = cx + slot.x[i] + slot.w[i] * 0.5f;
				float wy = baseY + slot.y[i] + capH * 0.5f + bob + (1f - Math.min(1f, in)) * size * 0.15f;
				int highlight = highlight(ctx, i + l * 7);
				if (bubbles) {
					bubble(g, wx, wy, slot.w[i] * scale / s, capH * scale / s, size, alpha, pop, highlight);
				} else if (glowStrength > 0f) {
					float flash = (1f - Math.min(1f, age / 0.8f)) * 0.75f + 0.18f;
					RenderUtils.glow(g, wx - slot.w[i] * 0.5f - size * 0.25f, wy - capH * 0.5f - size * 0.2f, slot.w[i] + size * 0.5f,
							capH + size * 0.4f, size * 0.35f, size * 0.6f, ColorUtil.fade(highlight, flash * glowStrength * alpha * 0.5f));
				}
				int c = ColorUtil.fade(ColorUtil.lerpRgb(highlight, 0xFFFFFFFF, Math.min(1f, age / 0.7f) * (bubbles ? 1f : 0.85f)), alpha);
				drawWord(g, slot.labels[i], wx, wy, scale, rotation, c, true);
			}
		}
	}

	/** A Liquid Glass capsule behind a word, with a ring when it pops. */
	private void bubble(GuiGraphicsExtractor g, float cx, float cy, float textW, float textH, float size, float alpha, float pop, int tint) {
		float w = textW + size * 0.75f;
		float h = textH + size * 0.6f;
		float x = cx - w * 0.5f;
		float y = cy - h * 0.5f;
		float r = h * 0.5f;
		RenderUtils.shadow(g, x, y + size * 0.08f, w, h, r, size * 0.35f, ColorUtil.fade(0x40000000, alpha));
		RenderUtils.gradient(g, x, y, w, h, r, ColorUtil.fade(ColorUtil.lerpRgb(0xFFFFFFFF, tint, 0.25f), 0.3f * alpha),
				ColorUtil.fade(ColorUtil.lerpRgb(0xFFFFFFFF, tint, 0.45f), 0.14f * alpha));
		RenderUtils.light(g, x + r * 0.4f, y + h * 0.08f, w - r * 0.8f, h * 0.38f, r * 0.6f, ColorUtil.fade(0xFFFFFFFF, 0.16f * alpha), 0);
		RenderUtils.hairline(g, x, y, w, h, r, ColorUtil.fade(0xFFFFFFFF, 0.6f * alpha), ColorUtil.fade(0xFFFFFFFF, 0.14f * alpha));
		if (pop > 0f) {
			float grow = pop * size * 0.8f;
			RenderUtils.stroke(g, x - grow, y - grow, w + grow * 2f, h + grow * 2f, r + grow, Math.max(0.5f, (1f - pop) * 1.5f),
					ColorUtil.fade(0xFFFFFFFF, (1f - pop) * 0.5f), ColorUtil.fade(0xFFFFFFFF, (1f - pop) * 0.2f));
		}
	}

	/** Cinema: a calm subtitle on a dark pill, cross-fading line by line. */
	private void cinema(GuiGraphicsExtractor g, Lyrics.Track t) {
		float size = textSize.getFloat();
		float e = easeOut(change.get());
		float cx = width * 0.5f;
		if (previous >= 0 && previous < t.size() && !t.blank(previous) && e < 1f) {
			subtitle(g, slot(t, previous, size), cx, size, 1f - e);
		}
		if (line >= 0 && !t.blank(line)) {
			subtitle(g, slot(t, line, size), cx, size, e);
		}
	}

	private void subtitle(GuiGraphicsExtractor g, Slot slot, float cx, float size, float alpha) {
		if (alpha <= 0.01f) {
			return;
		}
		float padX = size * 0.75f;
		float padY = size * 0.42f;
		float w = slot.width + padX * 2f;
		float h = slot.height - size * 0.3f + padY * 2f;
		float x = cx - w * 0.5f;
		float y = (height - h) * 0.5f;
		RenderUtils.fill(g, x, y, w, h, Math.min(h * 0.5f, size * 0.6f), ColorUtil.fade(0xA6000000, alpha));
		drawStatic(g, slot, cx, y + padY, alpha, 1f);
	}

	// ---- Helpers ------------------------------------------------------------------------------

	private int highlight(HudContext ctx, int index) {
		int accent = ctx.accent(phase());
		return switch (color.index()) {
			case 0 -> AlbumArt.accent(accent);
			case 1 -> accent;
			case 2 -> Palette.TEXT_PRIMARY;
			default -> ColorUtil.hsv(ctx.time * 40f + index * 37f, 0.6f, 1f);
		};
	}

	/**
	 * One word centred on ({@code cx}, {@code cy}), at {@code scale} of the face, with the font's
	 * synthesised slant / bold and the outline. {@code shadow} marks a word's base pass (soft shadow or
	 * outline); overlays such as the karaoke fill skip both.
	 */
	private void drawWord(GuiGraphicsExtractor g, TextLabel label, float cx, float cy, float scale, float rotation, int color,
			boolean shadow) {
		if ((color >>> 24) < 4) {
			return;
		}
		LyricFont f = face;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		if (rotation != 0f) {
			pose.rotate(rotation);
		}
		pose.scale(scale, scale);
		if (f.slant) {
			pose.mul(SLANT);
		}
		float bold = f.embolden ? LyricFont.EMBOLDEN : 0f;
		float x = -(label.width() + bold) * 0.5f;
		float y = -label.capHeight() * 0.5f;
		boolean outlined = shadow && outline.get();
		if (outlined) {
			// ~4.5 % of the em, and at least a device pixel.
			float r = Math.max(1.35f, HighDpiEngine.px() / Math.max(0.05f, scale));
			int dark = ColorUtil.fade(0xFF000000, (color >>> 24) / 255f * 0.8f);
			for (int i = 0; i < OUTLINE_TAPS.length; i += 2) {
				float dx = OUTLINE_TAPS[i] * r;
				float dy = OUTLINE_TAPS[i + 1] * r;
				LiquidText.draw(g, label, x + dx, y + dy, dark, false);
				if (bold > 0f) {
					LiquidText.draw(g, label, x + bold + dx, y + dy, dark, false);
				}
			}
		}
		if (shadow && !outlined) {
			LiquidText.drawSoft(g, label, x, y, color);
		} else {
			LiquidText.draw(g, label, x, y, color, false);
		}
		if (bold > 0f) {
			LiquidText.draw(g, label, x + bold, y, color, false);
		}
		pose.popMatrix();
	}

	private Slot slot(Lyrics.Track t, int index, float size) {
		return slot(t, index, size, WORD_GAP);
	}

	/** The laid-out words of a line at a size and word gap; built on first use and kept while it's on screen. */
	private Slot slot(Lyrics.Track t, int index, float size, float gap) {
		float tracking = tracking();
		boolean upper = uppercase.get();
		Slot free = null;
		for (Slot slot : slots) {
			if (slot.track == t && slot.line == index && slot.size == size && slot.gap == gap && slot.boxWidth == width && slot.face == face
					&& slot.tracking == tracking && slot.upper == upper) {
				slot.used = frame;
				return slot;
			}
			if (free == null || slot.used < free.used) {
				free = slot;
			}
		}
		free.build(t, index, size, gap, width, face, tracking, upper);
		free.used = frame;
		return free;
	}

	/** Words of one line, wrapped into centred rows (x relative to the centre). */
	private static final class Slot {
		Lyrics.@Nullable Track track;
		int line = -1;
		float size;
		float gap;
		float boxWidth;
		@Nullable LyricFont face;
		float tracking;
		boolean upper;
		long used = -1L;
		int count;
		float width;
		float height;
		TextLabel[] labels = new TextLabel[0];
		float[] x = new float[0];
		float[] y = new float[0];
		float[] w = new float[0];

		void build(Lyrics.Track t, int index, float size, float gap, float boxWidth, LyricFont face, float tracking, boolean upper) {
			this.track = t;
			this.line = index;
			this.size = size;
			this.gap = gap;
			this.boxWidth = boxWidth;
			this.face = face;
			this.tracking = tracking;
			this.upper = upper;
			float wrap = boxWidth - size;
			String[] words = t.words[index];
			int n = words.length;
			if (labels.length < n) {
				TextLabel[] grown = new TextLabel[n];
				System.arraycopy(labels, 0, grown, 0, labels.length);
				labels = grown;
				x = new float[n];
				y = new float[n];
				w = new float[n];
			}
			count = n;
			float s = size / FACE;
			float bold = face.embolden ? LyricFont.EMBOLDEN : 0f;
			for (int i = 0; i < n; i++) {
				String word = upper ? words[i].toUpperCase(Locale.ROOT) : words[i];
				if (labels[i] == null || labels[i].style() != face.style) {
					labels[i] = new TextLabel(word, face.style);
				} else {
					labels[i].setText(word);
				}
				labels[i].tracking(tracking);
				w[i] = (labels[i].width() + bold) * s;
			}
			float space = size * gap;
			float rowH = size * 1.3f;
			int rowStart = 0;
			float rowW = 0f;
			float rowY = 0f;
			width = 0f;
			for (int i = 0; i <= n; i++) {
				boolean breakRow = i == n || (i > rowStart && rowW + space + w[i] > wrap);
				if (breakRow) {
					float px = -rowW * 0.5f;
					for (int j = rowStart; j < i; j++) {
						x[j] = px;
						y[j] = rowY;
						px += w[j] + space;
					}
					width = Math.max(width, rowW);
					if (i == n) {
						break;
					}
					rowY += rowH;
					rowStart = i;
					rowW = 0f;
				}
				rowW += (i > rowStart ? space : 0f) + w[i];
			}
			height = rowY + rowH;
		}
	}

	private static float easeOut(float t) {
		float u = 1f - Math.clamp(t, 0f, 1f);
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
