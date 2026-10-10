package net.client.visuals.media;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;

/**
 * Shared "now playing" presentation for the Music HUD element and the main-menu player: labels that
 * only change with the track / second, marquee scrolling for long titles, artwork with a placeholder
 * and fade-in, progress bar and an animated level meter.
 */
public final class MusicView {
	public static final int ICON_MUSIC = 0xE122;
	public static final int ICON_PLAY = 0xE13C;
	public static final int ICON_PAUSE = 0xE12E;
	public static final int ICON_NEXT = 0xE160;
	public static final int ICON_PREV = 0xE15F;

	public final TextLabel title = new TextLabel("", LiquidText.TextStyle.LABEL);
	public final TextLabel headline = new TextLabel("", LiquidText.TextStyle.HEADLINE);
	public final TextLabel artist = new TextLabel("", LiquidText.TextStyle.BODY);
	public final TextLabel elapsed = new TextLabel("0:00", LiquidText.TextStyle.BODY);
	public final TextLabel remaining = new TextLabel("-0:00", LiquidText.TextStyle.BODY);
	public final TextLabel duration = new TextLabel("0:00", LiquidText.TextStyle.BODY);
	/** Small variants for compact cards: artist and "1:16 / 3:26". */
	public final TextLabel artistSmall = new TextLabel("", LiquidText.TextStyle.CAPTION);
	public final TextLabel timeline = new TextLabel("0:00 / 0:00", LiquidText.TextStyle.CAPTION);
	public final TextLabel elapsedSmall = new TextLabel("0:00", LiquidText.TextStyle.CAPTION);
	public final TextLabel remainingSmall = new TextLabel("-0:00", LiquidText.TextStyle.CAPTION);
	private final Smooth smoothProgress = new Smooth(0f, 9f);
	private final Smooth[] bars = new Smooth[5];
	private final Smooth playing = new Smooth(0f, 8f);
	private String shownKey = "\u0001";
	private long shownSecond = Long.MIN_VALUE;
	private long shownDuration = Long.MIN_VALUE;
	private float trackClock;
	private float angle;
	public NowPlaying track = NowPlaying.NONE;
	public float progress;

	public MusicView() {
		for (int i = 0; i < bars.length; i++) {
			bars[i] = new Smooth(0.2f, 14f);
		}
	}

	/**
	 * Pulls the current track, keeps the bridge and artwork alive and refreshes labels.
	 *
	 * @param anyPlayer   accept any player's session, not just Spotify's
	 * @param onlineArt   look artwork up online
	 * @param idleTitle   shown when nothing plays
	 * @param idleArtist  second line when nothing plays
	 */
	public void update(float dt, boolean anyPlayer, boolean onlineArt, String idleTitle, String idleArtist) {
		MediaBridge.request(anyPlayer);
		NowPlaying now = MediaBridge.current();
		track = now;
		AlbumArt.update(now, onlineArt);
		trackClock += dt;
		String key = now.active ? now.key : "\u0000" + idleTitle;
		if (!key.equals(shownKey)) {
			shownKey = key;
			trackClock = 0f;
			title.setText(now.active ? (now.title.isEmpty() ? "Unknown title" : now.title) : idleTitle);
			headline.setText(title.text());
			artist.setText(now.active ? now.artist : idleArtist);
			artistSmall.setText(now.active ? now.artist : idleArtist);
			smoothProgress.snap(now.active ? now.progressAt(System.currentTimeMillis()) : 0f);
		}
		long millis = System.currentTimeMillis();
		progress = now.active ? now.progressAt(millis) : 0f;
		long second = now.active ? now.positionAt(millis) / 1000L : 0L;
		if (second != shownSecond || now.duration != shownDuration) {
			shownSecond = second;
			shownDuration = now.duration;
			long total = now.duration / 1000L;
			elapsed.setText(format(second));
			remaining.setText("-" + format(Math.max(0L, total - second)));
			duration.setText(format(total));
			timeline.setText(format(second) + " / " + format(total));
			elapsedSmall.setText(elapsed.text());
			remainingSmall.setText(remaining.text());
		}
		smoothProgress.target(progress).update(dt);
		float p = playing.target(now.active && now.playing).update(dt);
		angle = (angle + dt * 1.1f * p) % ((float) Math.PI * 2f);
	}

	public boolean isPlaying() {
		return track.active && track.playing;
	}

	/** Progress eased towards the reported one, so the bar glides instead of ticking. */
	public float smoothProgress() {
		return smoothProgress.get();
	}

	/** Vinyl rotation in radians (advances only while playing). */
	public float angle() {
		return angle;
	}

	private static String format(long seconds) {
		long m = seconds / 60L;
		long s = seconds % 60L;
		return m + (s < 10 ? ":0" : ":") + s;
	}

	// ---- Drawing ------------------------------------------------------------------------------

	/** Album art (fading in over a placeholder) or an accent placeholder with a note glyph. */
	public void art(GuiGraphicsExtractor g, float x, float y, float size, float radius, int accent) {
		TextureSetup texture = AlbumArt.art();
		float reveal = AlbumArt.reveal();
		if (texture == null || reveal < 1f) {
			RenderUtils.gradient(g, x, y, size, size, radius, ColorUtil.lerpRgb(accent, 0xFF1F2937, 0.45f),
					ColorUtil.lerpRgb(accent, 0xFF0B1020, 0.7f));
			IconFont.draw(g, ICON_MUSIC, x + size * 0.5f, y + size * 0.5f, Math.max(8f, size * 0.42f), 0xE6FFFFFF, 0x33000000);
		}
		if (texture != null) {
			ImageDraw.rounded(g, texture, x, y, size, size, radius, ColorUtil.fade(0xFFFFFFFF, reveal));
		}
		RenderUtils.stroke(g, x, y, size, size, radius, net.client.visuals.render.HighDpiEngine.px(), 0x26FFFFFF, 0x0DFFFFFF);
	}

	/**
	 * Cover in the flat card style: a soft white tile, a spinner while the artwork is being looked
	 * up, a note glyph when there is none, and the art fading in on top.
	 */
	public void cover(GuiGraphicsExtractor g, float x, float y, float size, float radius, float time) {
		TextureSetup texture = AlbumArt.art();
		float reveal = AlbumArt.reveal();
		if (texture == null || reveal < 1f) {
			RenderUtils.fill(g, x, y, size, size, radius, 0x38FFFFFF);
			float cx = x + size * 0.5f;
			float cy = y + size * 0.5f;
			if (AlbumArt.loading() && track.active) {
				float head = time * 5.2f;
				float sweep = 1.4f + 1.1f * (float) Math.sin(time * 2.6f);
				RenderUtils.arc(g, cx, cy, size * 0.19f, head, head + sweep, Math.max(1f, size * 0.04f), 0xFFFFFFFF);
			} else {
				IconFont.draw(g, ICON_MUSIC, cx, cy, size * 0.42f, 0xB3FFFFFF, 0);
			}
		}
		if (texture != null) {
			ImageDraw.rounded(g, texture, x, y, size, size, radius, ColorUtil.fade(0xFFFFFFFF, reveal));
		}
	}

	/** Draws {@code label} inside a {@code maxWidth} box, scrolling it back and forth when it doesn't fit. */
	public void clipped(GuiGraphicsExtractor g, TextLabel label, float x, float y, float maxWidth, int color, boolean shadow,
			boolean marquee) {
		float overflow = label.width() - maxWidth;
		if (overflow <= 0.5f) {
			draw(g, label, x, y, color, shadow);
			return;
		}
		float offset = 0f;
		if (marquee) {
			final float speed = 16f;
			final float hold = 2.2f;
			float travel = overflow / speed;
			float t = trackClock % (hold + travel + hold);
			offset = t < hold ? 0f : t < hold + travel ? (t - hold) * speed : overflow;
		}
		g.enableScissor((int) Math.floor(x), (int) Math.floor(y - 3f), (int) Math.ceil(x + maxWidth), (int) Math.ceil(y + label.capHeight() + 4f));
		draw(g, label, x - offset, y, color, shadow);
		g.disableScissor();
	}

	private static void draw(GuiGraphicsExtractor g, TextLabel label, float x, float y, int color, boolean shadow) {
		if (shadow) {
			LiquidText.drawSoft(g, label, x, y, color);
		} else {
			LiquidText.draw(g, label, x, y, color, false);
		}
	}

	/** Rounded progress bar: track + accent fill (+ a small knob when {@code knob}). */
	public void progressBar(GuiGraphicsExtractor g, float x, float y, float w, float h, int accent, boolean knob) {
		float r = h * 0.5f;
		RenderUtils.fill(g, x, y, w, h, r, 0x38FFFFFF);
		float filled = Math.max(h, w * Math.clamp(progress, 0f, 1f));
		if (track.active) {
			RenderUtils.fill(g, x, y, filled, h, r, accent);
			if (knob) {
				RenderUtils.circle(g, x + filled - r, y + r, h * 1.1f, 0xFFFFFFFF);
			}
		}
	}

	/** Level-meter bars (animated while playing, resting low when paused). */
	public void bars(GuiGraphicsExtractor g, float x, float bottom, float maxHeight, float barWidth, float gap, int count, int color,
			float dt, float time) {
		for (int i = 0; i < count && i < bars.length; i++) {
			float target = isPlaying()
					? 0.28f + 0.72f * Math.abs((float) Math.sin(time * (3.1f + i * 1.37f) + i * 1.9f))
					* (0.6f + 0.4f * Math.abs((float) Math.sin(time * 1.3f + i)))
					: 0.18f;
			float v = bars[i].target(target).update(dt);
			float h = Math.max(barWidth, maxHeight * v);
			RenderUtils.fill(g, x + i * (barWidth + gap), bottom - h, barWidth, h, barWidth * 0.5f, color);
		}
	}
}
