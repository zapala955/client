package net.client.visuals.gui.window;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.BackgroundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Design tokens and shared drawing for the Liquid windows (Singleplayer, Multiplayer, Settings):
 * a dark "smoked glass" material in the macOS dark-mode palette, so every window reads as part of
 * the same system as the lock screen and its media player.
 */
public final class Ui {
	// ---- Palette (macOS dark appearance) ----
	public static final int ACCENT = 0xFF0A84FF;
	public static final int ACCENT_HOVER = 0xFF409CFF;
	public static final int DESTRUCTIVE = 0xFFFF453A;
	public static final int SUCCESS = 0xFF30D158;
	public static final int WARNING = 0xFFFF9F0A;
	public static final int TEXT = 0xFFF5F5F7;
	public static final int TEXT_2 = 0x9EEBEBF5;
	public static final int TEXT_3 = 0x61EBEBF5;
	public static final int SEPARATOR = 0x17FFFFFF;
	public static final int FILL_HOVER = 0x0FFFFFFF;
	public static final int FILL_CONTROL = 0x1AFFFFFF;
	public static final int FILL_CONTROL_HOVER = 0x26FFFFFF;
	public static final int GROUP = 0x0AFFFFFF;
	public static final int SELECTION = 0x3D0A84FF;

	// ---- Metrics ----
	public static final float WINDOW_RADIUS = 18f;
	public static final float ROW_RADIUS = 10f;
	public static final float CONTROL_H = 22f;

	private Ui() {
	}

	/**
	 * The window material: deep soft shadow, frosted wallpaper (menus) or the blurred world (in
	 * game, the screen's background pass), a neutral smoke tint, a faint top sheen and a lit rim.
	 */
	public static void window(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		float r = WINDOW_RADIUS;
		RenderUtils.shadow(g, x, y + 12f, w, h, r, 28f, 0x80000000);
		RenderUtils.shadow(g, x, y + 2f, w, h, r, 6f, 0x40000000);
		boolean frosted = Minecraft.getInstance().level == null && BackgroundEngine.INSTANCE.frost(g, x, y, w, h, r);
		RenderUtils.fill(g, x, y, w, h, r, frosted ? 0xB80C0D11 : 0xD60C0D11);
		RenderUtils.light(g, x, y, w, Math.min(h, 70f), r, 0x0FFFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), 0x3DFFFFFF, 0x0FFFFFFF);
	}

	/** One device-pixel horizontal separator. */
	public static void separator(GuiGraphicsExtractor g, float x, float y, float w) {
		RenderUtils.rect(g, x, HighDpiEngine.snap(y), w, HighDpiEngine.px(), SEPARATOR);
	}

	/** Grouped-list container (macOS inset grouped sections). */
	public static void group(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		RenderUtils.fill(g, x, y, w, h, ROW_RADIUS, GROUP);
		RenderUtils.stroke(g, x, y, w, h, ROW_RADIUS, HighDpiEngine.px(), 0x0FFFFFFF, 0x05FFFFFF);
	}

	/** Row highlight for hover (amount 0..1) and selection. */
	public static void rowHighlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float hover, float selected) {
		if (selected > 0.01f) {
			RenderUtils.fill(g, x, y, w, h, ROW_RADIUS, ColorUtil.fade(SELECTION, selected));
			RenderUtils.stroke(g, x, y, w, h, ROW_RADIUS, HighDpiEngine.px(), ColorUtil.fade(0x660A84FF, selected), ColorUtil.fade(0x330A84FF, selected));
		}
		if (hover > 0.01f) {
			RenderUtils.fill(g, x, y, w, h, ROW_RADIUS, ColorUtil.fade(FILL_HOVER, hover));
		}
	}

	/** macOS-style squircle icon tile (settings sidebar, placeholders). */
	public static void tile(GuiGraphicsExtractor g, float x, float y, float size, int color) {
		float r = size * 0.28f;
		RenderUtils.gradient(g, x, y, size, size, r, ColorUtil.lerpRgb(color, 0xFFFFFFFF, 0.12f), ColorUtil.lerpRgb(color, 0xFF000000, 0.12f));
		RenderUtils.stroke(g, x, y, size, size, r, HighDpiEngine.px(), 0x33FFFFFF, 0x0DFFFFFF);
	}

	/** Indeterminate activity spinner (macOS style rotating arc). */
	public static void spinner(GuiGraphicsExtractor g, float cx, float cy, float r, float thickness, int color) {
		float head = UiFrame.time * 5.2f;
		float sweep = 1.4f + 1.1f * (float) Math.sin(UiFrame.time * 2.6f);
		RenderUtils.ring(g, cx, cy, r, thickness, ColorUtil.fade(color, 0.14f));
		RenderUtils.arc(g, cx, cy, r, head, head + sweep, thickness, color);
	}

	/** Small section label (uppercase caption, tracked) used above grouped lists. */
	public static TextLabel sectionLabel(String text) {
		return new TextLabel(text, LiquidText.TextStyle.CAPTION).uppercase().tracking(0.5f);
	}

	/**
	 * Tooltip bubble near the cursor: a dense smoked capsule with up to a dozen lines, kept on screen.
	 * The first line is drawn brighter (title), the rest secondary.
	 */
	public static void tooltip(GuiGraphicsExtractor g, List<TextLabel> lines, float mouseX, float mouseY, float screenW, float screenH, float alpha) {
		if (lines.isEmpty() || alpha < 0.01f) {
			return;
		}
		float w = 0f;
		for (TextLabel line : lines) {
			w = Math.max(w, line.width());
		}
		w += 16f;
		float lineH = 11f;
		float h = lines.size() * lineH + 10f - (lineH - 6.55f);
		float x = mouseX + 10f;
		float y = mouseY + 12f;
		if (x + w > screenW - 6f) {
			x = mouseX - w - 6f;
		}
		if (y + h > screenH - 6f) {
			y = mouseY - h - 6f;
		}
		x = HighDpiEngine.snap(Math.max(6f, x));
		y = HighDpiEngine.snap(Math.max(6f, y));
		RenderUtils.pushAlpha(alpha);
		RenderUtils.shadow(g, x, y + 3f, w, h, 8f, 10f, 0x73000000);
		RenderUtils.fill(g, x, y, w, h, 8f, 0xF22A2B30);
		RenderUtils.stroke(g, x, y, w, h, 8f, HighDpiEngine.px(), 0x33FFFFFF, 0x0FFFFFFF);
		float ly = y + 5f;
		for (int i = 0; i < lines.size(); i++) {
			LiquidText.draw(g, lines.get(i), x + 8f, ly, i == 0 ? TEXT : TEXT_2, false);
			ly += lineH;
		}
		RenderUtils.popAlpha();
	}

	/** Greedy word wrap (layout-time only); a line feed starts a new paragraph. */
	public static List<TextLabel> wrap(String text, float maxWidth, LiquidText.TextStyle style) {
		List<TextLabel> lines = new java.util.ArrayList<>();
		TextLabel probe = new TextLabel("", style);
		for (String paragraph : text.split("\n")) {
			StringBuilder line = new StringBuilder();
			for (String word : paragraph.split(" ")) {
				if (word.isEmpty()) {
					continue;
				}
				String candidate = line.isEmpty() ? word : line + " " + word;
				probe.setText(candidate);
				if (probe.width() > maxWidth && !line.isEmpty()) {
					lines.add(new TextLabel(line.toString(), style));
					line.setLength(0);
					line.append(word);
				} else {
					line.setLength(0);
					line.append(candidate);
				}
			}
			if (!line.isEmpty()) {
				lines.add(new TextLabel(line.toString(), style));
			}
		}
		return lines;
	}
}
