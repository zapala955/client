package net.client.visuals.render.loading;

import net.client.visuals.render.HighDpiEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

/**
 * The resource-loading overlay as the client's splash: the menu wallpaper, frosted and dimmed
 * (fading in as soon as it's decoded), the app icon with a soft shadow and glow, the "Liquid
 * Client" wordmark and a glass capsule progress bar.
 *
 * <p>Timing follows {@code LoadingOverlay}: reloads started in game fade in over the current
 * screen; when the reload is done the bar completes, the mark lifts away, then the frosted backdrop
 * dissolves into the next screen – which, on first launch, is the main menu starting frosted and
 * sharpening, so the hand-over is seamless. Only vanilla pipelines and jar-loaded textures are used
 * (see {@link BootArt}); everything is drawn in device pixels so it's pixel-exact at any scale.</p>
 */
public final class BootScreen {
	private static final float FADE_IN_SECONDS = 0.5f;
	/** Exit timeline (seconds after the reload finished). */
	private static final float BAR_OUT_START = 0.05f;
	private static final float BAR_OUT_END = 0.35f;
	private static final float MARK_OUT_START = 0.18f;
	private static final float MARK_OUT_END = 0.6f;
	private static final float REVEAL_START = 0.38f;
	private static final float REVEAL_END = 1.05f;
	private static final int GLOW_BLUE = 0x3D7BFF;
	private static final int GLOW_VIOLET = 0x9B5CFF;
	/** When the last boot screen finished; the main menu skips its own fade-from-black right after. */
	private static long finishedAtNanos;

	private float shown;
	private float wallpaperIn;
	private long lastNanos = -1L;
	private boolean finished;

	/** Whether a boot screen just handed over (the main menu continues the transition instead). */
	public static boolean justFinished() {
		return finishedAtNanos != 0L && System.nanoTime() - finishedAtNanos < 2_500_000_000L;
	}

	/**
	 * Draws one frame.
	 *
	 * @return {@code true} once the exit animation is over and the overlay should be removed
	 */
	public boolean extract(GuiGraphicsExtractor g, Minecraft minecraft, float actualProgress, boolean fadeIn,
			long fadeInStart, long fadeOutStart, int mouseX, int mouseY, float partialTick) {
		long nanos = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.clamp((nanos - lastNanos) / 1.0e9f, 0f, 0.1f);
		lastNanos = nanos;
		long now = Util.getMillis();
		float out = fadeOutStart > -1L ? (now - fadeOutStart) / 1000f : -1f;
		float in = fadeInStart > -1L ? (now - fadeInStart) / 1000f / FADE_IN_SECONDS : -1f;
		Screen screen = minecraft.gui.screen();

		float background;
		float mark;
		float bar;
		float lift = 0f;
		if (out >= 0f) {
			if (out >= REVEAL_START) {
				if (screen != null) {
					screen.extractRenderStateWithTooltipAndSubtitles(g, 0, 0, partialTick);
				} else {
					minecraft.gui.hud.extractDeferredSubtitles();
				}
				g.nextStratum();
			}
			background = 1f - smooth(REVEAL_START, REVEAL_END, out);
			mark = 1f - smooth(MARK_OUT_START, MARK_OUT_END, out);
			bar = 1f - smooth(BAR_OUT_START, BAR_OUT_END, out);
			lift = smooth(MARK_OUT_START, MARK_OUT_END, out);
		} else if (fadeIn) {
			if (screen != null && in < 1f) {
				screen.extractRenderStateWithTooltipAndSubtitles(g, mouseX, mouseY, partialTick);
			} else {
				minecraft.gui.hud.extractDeferredSubtitles();
			}
			g.nextStratum();
			float t = Math.clamp(in, 0f, 1f);
			background = Math.max(0.15f, 1f - (1f - t) * (1f - t));
			mark = t;
			bar = t;
		} else {
			// First load: nothing behind us yet; clear straight to black.
			ARGB.setVector4fFromARGB32(minecraft.gameRenderer.gameRenderState().guiRenderState.clearColorOverride, 0xFF000000);
			background = 1f;
			mark = 1f;
			bar = 1f;
		}

		float target = out >= 0f ? 1f : actualProgress;
		float speed = out >= 0f ? 16f : 4.5f;
		shown += (Math.clamp(target, 0f, 1f) - shown) * (1f - (float) Math.exp(-speed * dt));

		int scale = HighDpiEngine.scale();
		int width = HighDpiEngine.deviceWidth();
		int height = HighDpiEngine.deviceHeight();
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.scale(1f / scale, 1f / scale); // device pixels from here on

		// Backdrop: black, the frosted wallpaper fading in once decoded, a dimming veil.
		g.fill(0, 0, width, height, alpha(0x000000, background));
		if (BootArt.wallpaper(width, height)) {
			wallpaperIn = Math.min(1f, wallpaperIn + dt / 0.45f);
			float a = background * smooth(0f, 1f, wallpaperIn);
			if (a > 0.004f) {
				g.blit(RenderPipelines.GUI_TEXTURED, BootArt.WALLPAPER, 0, 0, 0f, 0f, width, height,
						BootArt.wallpaperWidth(), BootArt.wallpaperHeight(), BootArt.wallpaperWidth(), BootArt.wallpaperHeight(),
						alpha(0xFFFFFF, a));
				g.fill(0, 0, width, height, alpha(0x07080C, a * 0.42f));
			}
		}

		// Mark: shadow + glow, app tile, wordmark.
		int size = Math.max(40, Math.round(Math.min(width * 0.75f, height) * 0.13f));
		int cx = width / 2;
		int cy = Math.round(height * 0.44f - size * 0.1f - lift * size * 0.12f);
		if (mark > 0.004f) {
			if (BootArt.glow()) {
				int glow = size * 4;
				blit(g, BootArt.GLOW, cx - glow / 2 - size / 4, cy - glow / 2 - size / 5, glow, glow, alpha(GLOW_BLUE, 0.20f * mark));
				blit(g, BootArt.GLOW, cx - glow / 2 + size / 4, cy - glow / 2 + size / 5, glow, glow, alpha(GLOW_VIOLET, 0.16f * mark));
				int shadow = Math.round(size * 1.9f);
				blit(g, BootArt.GLOW, cx - shadow / 2, cy - shadow / 2 + Math.round(size * 0.14f), shadow, shadow, alpha(0x000000, 0.55f * mark));
			}
			if (BootArt.tile(size)) {
				blit(g, BootArt.TILE, cx - size / 2, cy - size / 2, size, size, alpha(0xFFFFFF, mark));
			} else if (BootArt.drop(size)) {
				blit(g, BootArt.DROP, cx - size / 2, cy - size / 2, size, size, alpha(0xFFFFFF, mark));
			}
			int wordH = Math.max(12, Math.round(size * 0.27f));
			int wordW = BootArt.wordmark(wordH);
			if (wordW > 0) {
				blit(g, BootArt.WORDMARK, cx - wordW / 2, cy + size / 2 + Math.round(size * 0.2f), wordW, wordH, alpha(0xFFFFFF, mark));
			}
		}

		// Progress: a glass capsule track with a white fill.
		if (bar > 0.004f) {
			int barW = Math.round(size * 1.75f);
			int barH = Math.max(4, Math.round(height / 216f));
			int bx = cx - barW / 2;
			int by = cy + size / 2 + Math.round(size * 0.2f) + Math.round(size * 0.27f) + Math.round(size * 0.42f);
			if (BootArt.dot(barH)) {
				float a = bar * mark;
				capsule(g, bx, by, barW, barH, alpha(0xFFFFFF, 0.2f * a));
				capsule(g, bx, by, Math.max(barH, Math.round(barW * shown)), barH, alpha(0xFFFFFF, 0.95f * a));
			}
		}
		pose.popMatrix();

		boolean done = out >= REVEAL_END;
		if (done && !finished) {
			finished = true;
			finishedAtNanos = System.nanoTime();
			BootArt.releaseBoot();
		}
		return done;
	}

	private static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int w, int h, int color) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, w, h, w, h, color);
	}

	/** Capsule: the dot's halves as caps, a plain fill between them. */
	private static void capsule(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		if ((color >>> 24) == 0) {
			return;
		}
		w = Math.max(w, h);
		int left = h / 2;
		int right = h - left;
		g.blit(RenderPipelines.GUI_TEXTURED, BootArt.DOT, x, y, 0f, 0f, left, h, h, h, color);
		g.blit(RenderPipelines.GUI_TEXTURED, BootArt.DOT, x + w - right, y, left, 0f, right, h, h, h, color);
		if (w - left - right > 0) {
			g.fill(x + left, y, x + w - right, y + h, color);
		}
	}

	private static int alpha(int rgb, float alpha) {
		return Math.round(Math.clamp(alpha, 0f, 1f) * 255f) << 24 | rgb & 0xFFFFFF;
	}

	private static float smooth(float from, float to, float t) {
		float x = Math.clamp((t - from) / (to - from), 0f, 1f);
		return x * x * (3f - 2f * x);
	}
}
