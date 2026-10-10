package net.client.visuals.gui.photo;

import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.background.ImageDraw;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.post.PhotoCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

import java.util.Locale;

/**
 * Photo Mode's viewfinder, drawn after the (hidden) HUD – never part of the photo, which is taken
 * before the GUI.
 *
 * <ul>
 *   <li>The frame: everything outside the chosen aspect ratio dimmed, a hairline at its edge, the
 *       grid (thirds, golden ratio or a centre cross) inside.</li>
 *   <li>A level: the horizon as the camera sees it between two fixed marks; when it's level they
 *       join into one yellow line, like a phone's camera.</li>
 *   <li>The focus reticle where autofocus looks – it pops and turns yellow when focus is set, then
 *       settles to white – with the distance to what it's on.</li>
 *   <li>A glass status bar (lens, aperture, exposure, focus, quality, frozen time), a live RGB
 *       histogram with clipping warnings, the lens chips, the shutter (its ring fills while the
 *       photo exposes) and the last photo's thumbnail; key hints for the first seconds.</li>
 * </ul>
 * Everything animates frame-rate independently; labels rebuild only when their value changes.
 * The studio ({@link PhotoStudioScreen}) clicks the chips, the shutter and the thumbnail through
 * the hit areas recorded here.
 */
public final class PhotoOverlay {
	private static final int WHITE = 0xFFF8FAFC;
	private static final int DIM = 0x99EBEBF5;
	private static final int YELLOW = 0xFFFFD60A;
	private static final int GLASS = 0x8C0B0C10;
	private static final int MASK = 0xC4000000;
	private static final float SHUTTER = 21f;

	private static final Smooth SHOWN = new Smooth(0f, 9f);
	private static final Smooth INSET = new Smooth(0f, 14f);
	private static final Smooth PRESS = new Smooth(0f, 22f);
	private static final Smooth LEVEL = new Smooth(0f, 16f);
	private static final Smooth THUMB = new Smooth(0f, 10f);
	private static long lastNanos = -1L;

	// Status bar.
	private static final TextLabel LENS = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel APERTURE = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel EXPOSURE = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel FOCUS = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel QUALITY = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel FROZEN = new TextLabel("", LiquidText.TextStyle.CAPTION).uppercase().tracking(1f);
	private static final TextLabel DISTANCE = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private static final TextLabel TOAST = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel[] STATUS = {LENS, APERTURE, EXPOSURE, FOCUS, QUALITY};
	private static final int[] CHANNEL_COLORS = {0x8CFF453A, 0x8C30D158, 0x8C0A84FF};
	private static final TextLabel[] CHIPS = new TextLabel[PhotoMode.LENSES.length];
	private static final TextLabel[] HINT_KEYS = new TextLabel[6];
	private static final TextLabel[] HINT_TEXT = new TextLabel[6];
	private static int shownLens = -1;
	private static int shownAperture = -1;
	private static int shownExposure = Integer.MIN_VALUE;
	private static int shownFocus = -1;
	private static int shownQuality = -1;
	private static int shownDistance = Integer.MIN_VALUE;
	private static int shownChipLens = -1;
	private static int shownNearest = -1;
	private static boolean shownFailed;
	private static boolean labelsReady;

	// Hit areas (GUI units), from the last frame drawn.
	private static float shutterX;
	private static float shutterY;
	private static final float[] CHIP_X = new float[PhotoMode.LENSES.length];
	private static float chipY;
	private static float chipR;
	private static float thumbX;
	private static float thumbY;
	private static float thumbW;
	private static float thumbH;

	private PhotoOverlay() {
	}

	private static void ensureLabels() {
		if (labelsReady) {
			return;
		}
		labelsReady = true;
		for (int i = 0; i < CHIPS.length; i++) {
			CHIPS[i] = new TextLabel(Integer.toString(PhotoMode.LENSES[i]), LiquidText.TextStyle.CAPTION);
		}
		String[][] hints = {
				{"WASD · Space · Shift", "liquidclient.photo.hint.move"},
				{"Scroll · 1–6", "liquidclient.photo.hint.zoom"},
				{"Q / E · R", "liquidclient.photo.hint.tilt"},
				{"Click", "liquidclient.photo.hint.focus"},
				{"Right Click · F2", "liquidclient.photo.hint.shoot"},
				{"Tab · H · F4", "liquidclient.photo.hint.studio"},
		};
		for (int i = 0; i < hints.length; i++) {
			HINT_KEYS[i] = new TextLabel(hints[i][0], LiquidText.TextStyle.LABEL);
			HINT_TEXT[i] = new TextLabel(I18n.get(hints[i][1]), LiquidText.TextStyle.CAPTION);
		}
		FROZEN.setText(I18n.get("liquidclient.photo.frozen"));
	}

	/** After the HUD, every frame. */
	public static void extract(GuiGraphicsExtractor g) {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		float shown = SHOWN.target(PhotoMode.active() ? 1f : 0f).update(dt);
		if (shown < 0.004f) {
			return;
		}
		ensureLabels();
		PhotoModeModule m = PhotoMode.module();
		float w = g.guiWidth();
		float h = g.guiHeight();
		boolean studio = Minecraft.getInstance().gui.screen() instanceof PhotoStudioScreen;
		float inset = INSET.target(studio ? PhotoStudioScreen.PANEL_W + PhotoStudioScreen.MARGIN : 0f).update(dt);

		RenderUtils.pushAlpha(shown);
		float[] frame = frame(m, w, h);
		mask(g, frame, w, h);
		boolean ui = !PhotoMode.uiHidden() || studio;
		if (ui) {
			grid(g, m, frame);
			level(g, m, w * 0.5f, h * 0.5f, dt);
			reticle(g, m, w, h);
			statusBar(g, m, w);
			if (m.histogram.get()) {
				histogram(g, w - inset - 14f - 118f, 14f);
			}
			lensChips(g, m, w * 0.5f - inset * 0.5f, h - 30f);
			shutter(g, w - inset - 30f, h * 0.5f, dt);
			thumbnail(g, w - inset - 30f, h * 0.5f + 46f, dt);
			toast(g, w * 0.5f, 44f);
			hints(g, 14f, h - 14f);
		}
		blink(g, w, h);
		RenderUtils.popAlpha();
	}

	/** The photo's frame on screen: x, y, w, h. */
	private static float[] frame(PhotoModeModule m, float w, float h) {
		float ratio = PhotoModeModule.ASPECT_RATIOS[m.aspect.index()];
		float fw = w;
		float fh = h;
		if (ratio > 0f) {
			if (ratio > w / h) {
				fh = w / ratio;
			} else {
				fw = h * ratio;
			}
		}
		FRAME[0] = (w - fw) * 0.5f;
		FRAME[1] = (h - fh) * 0.5f;
		FRAME[2] = fw;
		FRAME[3] = fh;
		return FRAME;
	}

	private static final float[] FRAME = new float[4];

	private static void mask(GuiGraphicsExtractor g, float[] f, float w, float h) {
		float px = HighDpiEngine.px();
		if (f[1] > 0.5f) {
			RenderUtils.rect(g, 0f, 0f, w, f[1], MASK);
			RenderUtils.rect(g, 0f, f[1] + f[3], w, h - f[1] - f[3], MASK);
		}
		if (f[0] > 0.5f) {
			RenderUtils.rect(g, 0f, f[1], f[0], f[3], MASK);
			RenderUtils.rect(g, f[0] + f[2], f[1], w - f[0] - f[2], f[3], MASK);
		}
		if (f[0] > 0.5f || f[1] > 0.5f) {
			int edge = 0x4DFFFFFF;
			RenderUtils.rect(g, f[0], f[1], f[2], px, edge);
			RenderUtils.rect(g, f[0], f[1] + f[3] - px, f[2], px, edge);
			RenderUtils.rect(g, f[0], f[1], px, f[3], edge);
			RenderUtils.rect(g, f[0] + f[2] - px, f[1], px, f[3], edge);
		}
	}

	private static void grid(GuiGraphicsExtractor g, PhotoModeModule m, float[] f) {
		int mode = m.grid.index();
		if (mode == 0) {
			return;
		}
		float px = HighDpiEngine.px();
		int color = 0x40FFFFFF;
		if (mode == PhotoModeModule.GRID_CROSS) {
			float cx = f[0] + f[2] * 0.5f;
			float cy = f[1] + f[3] * 0.5f;
			RenderUtils.rect(g, cx - 9f, cy - px * 0.5f, 6f, px, 0x80FFFFFF);
			RenderUtils.rect(g, cx + 3f, cy - px * 0.5f, 6f, px, 0x80FFFFFF);
			RenderUtils.rect(g, cx - px * 0.5f, cy - 9f, px, 6f, 0x80FFFFFF);
			RenderUtils.rect(g, cx - px * 0.5f, cy + 3f, px, 6f, 0x80FFFFFF);
			return;
		}
		float a = mode == PhotoModeModule.GRID_GOLDEN ? 0.381966f : 1f / 3f;
		RenderUtils.rect(g, HighDpiEngine.snap(f[0] + f[2] * a), f[1], px, f[3], color);
		RenderUtils.rect(g, HighDpiEngine.snap(f[0] + f[2] * (1f - a)), f[1], px, f[3], color);
		RenderUtils.rect(g, f[0], HighDpiEngine.snap(f[1] + f[3] * a), f[2], px, color);
		RenderUtils.rect(g, f[0], HighDpiEngine.snap(f[1] + f[3] * (1f - a)), f[2], px, color);
	}

	private static void level(GuiGraphicsExtractor g, PhotoModeModule m, float cx, float cy, float dt) {
		float roll = PhotoMode.roll();
		if (!m.level.get() || Math.abs(roll) > 45f) {
			return;
		}
		float level = LEVEL.target(Math.abs(roll) < 0.35f ? 1f : 0f).update(dt);
		int color = ColorUtil.lerp(0xB3FFFFFF, YELLOW, level);
		float t = 1.5f;
		// The fixed marks; joined into one line when level.
		float gap = 34f * (1f - level);
		RenderUtils.line(g, cx - 62f, cy, cx - 30f - gap * 0.2f, cy, t, color);
		RenderUtils.line(g, cx + 30f + gap * 0.2f, cy, cx + 62f, cy, t, color);
		// The horizon as the camera sees it: turned the way the picture turns (y is down on screen).
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate(-roll * Mth.DEG_TO_RAD);
		RenderUtils.line(g, -30f, 0f, 30f, 0f, t, color);
		pose.popMatrix();
	}

	private static void reticle(GuiGraphicsExtractor g, PhotoModeModule m, float w, float h) {
		if (!m.depth.get() || m.focusMode.index() == PhotoModeModule.FOCUS_MANUAL) {
			return;
		}
		boolean point = m.focusMode.index() == PhotoModeModule.FOCUS_POINT;
		float x = PhotoMode.pointX() * w;
		float y = PhotoMode.pointY() * h;
		float since = PhotoMode.sinceFocus();
		float pop = (float) Math.exp(-since * 9f);
		float size = (point ? 34f : 46f) * (1f + 0.35f * pop);
		float hot = 1f - Mth.clamp((since - 1.1f) / 0.5f, 0f, 1f);
		int color = ColorUtil.lerp(0x99FFFFFF, YELLOW, hot);
		float half = size * 0.5f;
		float arm = Math.max(6f, size * 0.22f);
		float t = 1.5f;
		for (int sx = -1; sx <= 1; sx += 2) {
			for (int sy = -1; sy <= 1; sy += 2) {
				float cx = x + sx * half;
				float cy = y + sy * half;
				RenderUtils.line(g, cx, cy, cx - sx * arm, cy, t, color);
				RenderUtils.line(g, cx, cy, cx, cy - sy * arm, t, color);
			}
		}
		float d = PhotoMode.focusDistanceEstimate();
		int tenths = d < 0f ? -1 : Math.round(d * 10f);
		if (tenths != shownDistance) {
			shownDistance = tenths;
			DISTANCE.setText(tenths < 0 ? "∞" : tenths >= 1000 ? (tenths / 10) + " m" : (tenths / 10) + "." + (tenths % 10) + " m");
		}
		LiquidText.draw(g, DISTANCE, x - DISTANCE.width() * 0.5f, y + half + 5f, ColorUtil.fade(color, 0.9f), true);
	}

	private static void statusBar(GuiGraphicsExtractor g, PhotoModeModule m, float w) {
		int lens = Math.round(PhotoMode.focal());
		if (lens != shownLens) {
			shownLens = lens;
			LENS.setText(lens + " mm");
		}
		int aperture = (int) Math.round(m.aperture.get() * 10.0);
		if (aperture != shownAperture) {
			shownAperture = aperture;
			APERTURE.setText(String.format(Locale.ROOT, "f/%.1f", aperture / 10.0));
		}
		int exposure = (int) Math.round(m.exposure.get() * 10.0);
		if (exposure != shownExposure) {
			shownExposure = exposure;
			EXPOSURE.setText(PhotoModeModule.ev(exposure / 10.0));
		}
		int focusKey = !m.depth.get() ? 0 : m.focusMode.index() == PhotoModeModule.FOCUS_MANUAL
				? 10000 + (int) Math.round(m.focusDistance.get() * 10.0) : 1 + m.focusMode.index();
		if (focusKey != shownFocus) {
			shownFocus = focusKey;
			FOCUS.setText(focusKey == 0 ? I18n.get("liquidclient.photo.status.sharp") : focusKey >= 10000
					? String.format(Locale.ROOT, "MF %.1f m", (focusKey - 10000) / 10.0)
					: I18n.get(focusKey == 1 ? "liquidclient.photo.status.af" : "liquidclient.photo.status.af_point"));
		}
		int quality = m.quality.index();
		if (quality != shownQuality) {
			shownQuality = quality;
			int samples = PhotoModeModule.SAMPLES[quality];
			QUALITY.setText(samples <= 1 ? "1×" : samples + "×");
		}
		boolean frozen = PhotoMode.freezesWorld();
		TextLabel[] items = STATUS;
		float gap = 14f;
		float content = 0f;
		for (TextLabel item : items) {
			content += item.width();
		}
		content += gap * (items.length - 1);
		float dot = PhotoCapture.holdsCamera() ? 10f : 0f;
		float frozenW = frozen ? FROZEN.width() + 20f : 0f;
		float bw = content + 28f + dot + frozenW;
		float bh = 22f;
		float x = (w - bw) * 0.5f;
		float y = 12f;
		glass(g, x, y, bw, bh, bh * 0.5f);
		float tx = x + 14f;
		float cy = y + bh * 0.5f;
		if (dot > 0f) {
			// Recording dot while the shutter is open.
			float pulse = 0.6f + 0.4f * Mth.sin(System.nanoTime() * 1.0e-9f * 12f);
			RenderUtils.circle(g, tx + 3f, cy, 3f, ColorUtil.fade(0xFFFF453A, pulse));
			tx += dot;
		}
		for (int i = 0; i < items.length; i++) {
			TextLabel item = items[i];
			int color = i == 0 ? YELLOW : WHITE;
			LiquidText.draw(g, item, tx, cy - item.capHeight() * 0.5f, color, false);
			tx += item.width();
			if (i < items.length - 1) {
				RenderUtils.circle(g, tx + gap * 0.5f, cy, 1.1f, 0x66FFFFFF);
				tx += gap;
			}
		}
		if (frozen) {
			tx += 10f;
			RenderUtils.fill(g, tx, cy - 7f, FROZEN.width() + 10f, 14f, 7f, 0x330A84FF);
			LiquidText.draw(g, FROZEN, tx + 5f, cy - FROZEN.capHeight() * 0.5f, 0xFF64D2FF, false);
		}
	}

	private static void histogram(GuiGraphicsExtractor g, float x, float y) {
		float w = 118f;
		float h = 56f;
		glass(g, x, y, w, h, 11f);
		int max = PhotoCapture.histogramMax();
		if (max <= 1) {
			return;
		}
		float pad = 7f;
		float iw = w - pad * 2f;
		float ih = h - pad * 2f;
		float bin = iw / PhotoCapture.BINS;
		int[] colors = CHANNEL_COLORS;
		for (int c = 0; c < 3; c++) {
			for (int i = 0; i < PhotoCapture.BINS; i++) {
				float v = (float) Math.sqrt(Math.min(1f, PhotoCapture.histogram(c, i) / (float) max));
				if (v > 0.01f) {
					float bh = v * ih;
					RenderUtils.rect(g, x + pad + i * bin, y + pad + ih - bh, bin + 0.05f, bh, colors[c]);
				}
			}
		}
		// Clipping: a red corner where highlights burn out, a blue one where shadows go black.
		if (PhotoCapture.clipped(true) > 0.005f) {
			RenderUtils.fill(g, x + w - pad - 4f, y + pad - 2f, 6f, 6f, 3f, 0xFFFF453A);
		}
		if (PhotoCapture.clipped(false) > 0.005f) {
			RenderUtils.fill(g, x + pad - 2f, y + pad - 2f, 6f, 6f, 3f, 0xFF0A84FF);
		}
	}

	private static void lensChips(GuiGraphicsExtractor g, PhotoModeModule m, float cx, float cy) {
		int n = PhotoMode.LENSES.length;
		float r = 11f;
		float gap = 5f;
		float bw = n * r * 2f + (n - 1) * gap + 8f;
		float bh = r * 2f + 8f;
		float x = cx - bw * 0.5f;
		glass(g, x, cy - bh * 0.5f, bw, bh, bh * 0.5f);
		float focal = PhotoMode.focal();
		int nearest = 0;
		for (int i = 1; i < n; i++) {
			if (Math.abs(Math.log(PhotoMode.LENSES[i] / focal)) < Math.abs(Math.log(PhotoMode.LENSES[nearest] / focal))) {
				nearest = i;
			}
		}
		int exact = Math.round(focal);
		// The selected chip shows the exact focal length (labels rebuild only when it changes).
		if (exact != shownChipLens || nearest != shownNearest) {
			shownChipLens = exact;
			shownNearest = nearest;
			for (int i = 0; i < n; i++) {
				CHIPS[i].setText(Integer.toString(i == nearest ? exact : PhotoMode.LENSES[i]));
			}
		}
		chipY = cy;
		chipR = r;
		for (int i = 0; i < n; i++) {
			float chipX = x + 4f + r + i * (r * 2f + gap);
			CHIP_X[i] = chipX;
			boolean selected = i == nearest;
			TextLabel label = CHIPS[i];
			if (selected) {
				RenderUtils.circle(g, chipX, cy, r, 0x40FFFFFF);
			}
			LiquidText.draw(g, label, chipX - label.width() * 0.5f, cy - label.capHeight() * 0.5f, selected ? YELLOW : DIM, false);
		}
	}

	private static void shutter(GuiGraphicsExtractor g, float cx, float cy, float dt) {
		shutterX = cx;
		shutterY = cy;
		float progress = PhotoCapture.progress();
		boolean busy = PhotoCapture.busy();
		float press = PRESS.target(busy ? 1f : 0f).update(dt);
		RenderUtils.shadow(g, cx - SHUTTER, cy - SHUTTER + 2f, SHUTTER * 2f, SHUTTER * 2f, SHUTTER, 8f, 0x59000000);
		RenderUtils.ring(g, cx, cy, SHUTTER, 2.5f, 0xF2FFFFFF);
		float inner = (SHUTTER - 4.5f) * (1f - 0.12f * press);
		RenderUtils.circle(g, cx, cy, inner, PhotoCapture.settling() ? ColorUtil.fade(0xFFFFFFFF,
				0.65f + 0.35f * Mth.sin(System.nanoTime() * 1.0e-9f * 14f)) : 0xFFFFFFFF);
		if (progress > 0f) {
			RenderUtils.arc(g, cx, cy, SHUTTER - 1.25f, 0f, progress * Mth.TWO_PI, 2.5f, YELLOW);
		}
	}

	private static void thumbnail(GuiGraphicsExtractor g, float cx, float top, float dt) {
		TextureSetup texture = PhotoCapture.thumbnail();
		float shown = THUMB.target(texture != null ? 1f : 0f).update(dt);
		thumbW = 0f;
		if (texture == null || shown < 0.01f) {
			return;
		}
		float since = PhotoCapture.sinceSaved();
		float pop = 1f + 0.18f * (float) Math.exp(-since * 7f) * Mth.sin(Math.min(since * 14f, Mth.PI));
		float th = 30f * pop;
		float tw = Mth.clamp(th * PhotoCapture.thumbnailAspect(), th * 0.6f, th * 1.8f);
		float x = cx - tw * 0.5f;
		float r = 7f;
		thumbX = x;
		thumbY = top;
		thumbW = tw;
		thumbH = th;
		RenderUtils.pushAlpha(shown);
		RenderUtils.shadow(g, x, top + 2f, tw, th, r, 6f, 0x66000000);
		ImageDraw.rounded(g, texture, x, top, tw, th, r, ColorUtil.fade(0xFFFFFFFF, RenderUtils.alpha()));
		RenderUtils.stroke(g, x, top, tw, th, r, 1.5f, 0xE6FFFFFF, 0xB3FFFFFF);
		RenderUtils.popAlpha();
	}

	private static void toast(GuiGraphicsExtractor g, float cx, float y) {
		boolean failed = PhotoCapture.failed();
		float since = PhotoCapture.sinceSaved();
		float alpha = failed ? 1f : since < 2.2f ? Math.min(1f, since * 6f) * (1f - Mth.clamp((since - 1.8f) / 0.4f, 0f, 1f)) : 0f;
		if (alpha < 0.01f) {
			return;
		}
		if (failed != shownFailed || TOAST.width() <= 0f) {
			shownFailed = failed;
			TOAST.setText(I18n.get(failed ? "liquidclient.photo.failed" : "liquidclient.photo.saved"));
		}
		float bw = TOAST.width() + 38f;
		float bh = 22f;
		RenderUtils.pushAlpha(alpha);
		glass(g, cx - bw * 0.5f, y, bw, bh, bh * 0.5f);
		IconFont.draw(g, failed ? 0xE077 : 0xE226, cx - bw * 0.5f + 15f, y + bh * 0.5f, 11f, failed ? 0xFFFF453A : 0xFF30D158, 0);
		LiquidText.draw(g, TOAST, cx - bw * 0.5f + 26f, y + (bh - TOAST.capHeight()) * 0.5f, WHITE, false);
		RenderUtils.popAlpha();
	}

	private static void hints(GuiGraphicsExtractor g, float x, float bottom) {
		float since = PhotoMode.sinceEntered();
		float alpha = 1f - Mth.clamp((since - 7f) / 0.8f, 0f, 1f);
		if (alpha < 0.01f || Minecraft.getInstance().gui.screen() != null) {
			return;
		}
		float keyW = 0f;
		float textW = 0f;
		for (int i = 0; i < HINT_KEYS.length; i++) {
			keyW = Math.max(keyW, HINT_KEYS[i].width());
			textW = Math.max(textW, HINT_TEXT[i].width());
		}
		float line = 13f;
		float pad = 9f;
		float bw = pad * 2f + keyW + 10f + textW;
		float bh = pad * 2f + line * HINT_KEYS.length - 4f;
		float y = bottom - bh;
		RenderUtils.pushAlpha(alpha * Mth.clamp(since * 3f, 0f, 1f));
		glass(g, x, y, bw, bh, 11f);
		for (int i = 0; i < HINT_KEYS.length; i++) {
			float ly = y + pad + i * line;
			LiquidText.draw(g, HINT_KEYS[i], x + pad, ly, WHITE, false);
			LiquidText.draw(g, HINT_TEXT[i], x + pad + keyW + 10f, ly + 1f, DIM, false);
		}
		RenderUtils.popAlpha();
	}

	/** The shutter's blink: the view goes dark for an instant when it fires. */
	private static void blink(GuiGraphicsExtractor g, float w, float h) {
		float t = PhotoCapture.sinceShutter();
		if (t > 0.22f) {
			return;
		}
		float a = t < 0.035f ? t / 0.035f : 1f - (t - 0.035f) / 0.185f;
		RenderUtils.rect(g, 0f, 0f, w, h, ColorUtil.fade(0xFF000000, 0.78f * Mth.clamp(a, 0f, 1f)));
	}

	private static void glass(GuiGraphicsExtractor g, float x, float y, float w, float h, float r) {
		RenderUtils.shadow(g, x, y + 2f, w, h, r, 10f, 0x40000000);
		RenderUtils.fill(g, x, y, w, h, r, GLASS);
		RenderUtils.light(g, x, y, w, Math.min(h, 18f), r, 0x14FFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), 0x33FFFFFF, 0x0DFFFFFF);
	}

	// ---- Hit areas for the studio -----------------------------------------------------------------

	public static boolean overShutter(double mx, double my) {
		double dx = mx - shutterX;
		double dy = my - shutterY;
		return dx * dx + dy * dy <= (SHUTTER + 3f) * (SHUTTER + 3f);
	}

	/** The lens chip under the mouse, or -1. */
	public static int lensAt(double mx, double my) {
		for (int i = 0; i < CHIP_X.length; i++) {
			double dx = mx - CHIP_X[i];
			double dy = my - chipY;
			if (dx * dx + dy * dy <= chipR * chipR) {
				return i;
			}
		}
		return -1;
	}

	public static boolean overThumbnail(double mx, double my) {
		return thumbW > 0f && mx >= thumbX && mx <= thumbX + thumbW && my >= thumbY && my <= thumbY + thumbH;
	}
}
