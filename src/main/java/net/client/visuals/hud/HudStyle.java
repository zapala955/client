package net.client.visuals.hud;

import net.client.visuals.render.BlurPanel;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The HUD's visual styles: element backgrounds drawn from SDF primitives at the element's local
 * origin. The Frosted / Vibrant / Liquid styles blur the actual game behind the element
 * ({@link BlurPanel}); the older glass styles are translucent tints with a lit rim.
 */
public final class HudStyle {
	/** Stable ids (config + translation keys); the index is the style number. */
	public static final String[] IDS = {
			"glass", "clear", "frost", "minimal", "pill", "island", "neon", "gradient", "outline", "sidebar", "classic", "chroma",
			"frosted", "vibrant", "liquid"
	};
	public static final int GLASS = 0;
	public static final int CLEAR = 1;
	public static final int FROST = 2;
	public static final int MINIMAL = 3;
	public static final int PILL = 4;
	public static final int ISLAND = 5;
	public static final int NEON = 6;
	public static final int GRADIENT = 7;
	public static final int OUTLINE = 8;
	public static final int SIDEBAR = 9;
	public static final int CLASSIC = 10;
	public static final int CHROMA = 11;
	/** Real blur of the game behind the element, dark tint (the default). */
	public static final int FROSTED = 12;
	/** Real blur, light and saturated – macOS vibrancy. */
	public static final int VIBRANT = 13;
	/** Real blur with lens refraction towards the rim and a lit edge. */
	public static final int LIQUID = 14;

	/** Blur radius in GUI units for the blur styles (theme setting, set every frame). */
	static float blur = 7f;

	private HudStyle() {
	}

	/** Corner radius a style actually uses for a box of height {@code h}. */
	public static float radius(int style, float h, float themeRadius) {
		return switch (style) {
			case PILL, ISLAND -> h * 0.5f;
			case CLASSIC -> 0f;
			case SIDEBAR -> Math.min(themeRadius, 4f);
			default -> Math.min(themeRadius, h * 0.5f);
		};
	}

	/** Whether text needs a shadow to stay legible (no background behind it). */
	public static boolean needsShadow(int style) {
		return style == MINIMAL || style == CLEAR || style == FROST || style == VIBRANT || style == LIQUID;
	}

	/** Device-pixel blur radius for the current GUI scale. */
	private static float blurPx() {
		return Math.min(40f, blur * HighDpiEngine.scale());
	}

	/**
	 * Draws the background of a {@code w × h} box at (0, 0).
	 *
	 * @param opacity 0..1 multiplier for fills (rims and glows stay)
	 * @param time    seconds, for animated styles
	 */
	public static void background(GuiGraphicsExtractor g, int style, float w, float h, float themeRadius, int accent,
			float opacity, float time) {
		float r = radius(style, h, themeRadius);
		float px = HighDpiEngine.px();
		switch (style) {
			case GLASS -> {
				RenderUtils.shadow(g, 0f, 1f, w, h, r, 4f, ColorUtil.fade(0x55000000, opacity));
				RenderUtils.gradient(g, 0f, 0f, w, h, r, ColorUtil.fade(0xA3243147, opacity), ColorUtil.fade(0x8C0F172A, opacity));
				RenderUtils.light(g, 0f, 0f, w, h * 0.5f, r, 0x1AFFFFFF, 0x00FFFFFF);
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x40FFFFFF, 0x0DFFFFFF);
			}
			case CLEAR -> {
				RenderUtils.shadow(g, 0f, 1f, w, h, r, 4f, ColorUtil.fade(0x33000000, opacity));
				RenderUtils.gradient(g, 0f, 0f, w, h, r, ColorUtil.fade(0x40FFFFFF, opacity), ColorUtil.fade(0x26FFFFFF, opacity));
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x99FFFFFF, 0x1FFFFFFF);
			}
			case FROST -> {
				RenderUtils.shadow(g, 0f, 1f, w, h, r, 4f, ColorUtil.fade(0x33000000, opacity));
				RenderUtils.gradient(g, 0f, 0f, w, h, r, ColorUtil.fade(0x5C9CC8F5, opacity), ColorUtil.fade(0x404F86C6, opacity));
				RenderUtils.light(g, 0f, 0f, w, h * 0.5f, r, 0x26FFFFFF, 0x00FFFFFF);
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0xA6D6ECFF, 0x1AFFFFFF);
			}
			case MINIMAL -> {
			}
			case PILL -> {
				RenderUtils.shadow(g, 0f, 1f, w, h, r, 3f, ColorUtil.fade(0x40000000, opacity));
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xE0141619, opacity));
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x24FFFFFF, 0x0AFFFFFF);
			}
			case ISLAND -> {
				RenderUtils.shadow(g, 0f, 1.5f, w, h, r, 5f, ColorUtil.fade(0x73000000, opacity));
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xFA000000, Math.max(opacity, 0.85f)));
			}
			case NEON -> {
				RenderUtils.outerGlow(g, 0f, 0f, w, h, r, 6f, ColorUtil.fade(accent, 0.5f));
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xE00A0D16, opacity));
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px * 1.5f, accent, ColorUtil.fade(accent, 0.55f));
			}
			case GRADIENT -> {
				int deep = ColorUtil.lerpRgb(accent, 0xFF1A0B3D, 0.55f);
				RenderUtils.shadow(g, 0f, 1f, w, h, r, 4f, ColorUtil.fade(0x55000000, opacity));
				RenderUtils.gradientH(g, 0f, 0f, w, h, r, ColorUtil.fade(accent, 0.9f * opacity), ColorUtil.fade(deep, 0.9f * opacity));
				RenderUtils.light(g, 0f, 0f, w, h * 0.5f, r, 0x24FFFFFF, 0x00FFFFFF);
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x4DFFFFFF, 0x0DFFFFFF);
			}
			case OUTLINE -> {
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0x40000000, opacity));
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px * 1.5f, accent, ColorUtil.fade(accent, 0.6f));
			}
			case SIDEBAR -> {
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xC00F172A, opacity));
				RenderUtils.fill(g, 0f, 0f, 2f, h, Math.min(1f, r), accent);
			}
			case CLASSIC -> RenderUtils.rect(g, 0f, 0f, w, h, ColorUtil.fade(0x70000000, opacity));
			case FROSTED -> {
				RenderUtils.shadow(g, 0f, 1.5f, w, h, r, 5f, ColorUtil.fade(0x4D000000, Math.max(opacity, 0.4f)));
				int tint = ColorUtil.withAlpha(0x0C0C0E, Math.round(255f * Math.clamp(opacity * 0.55f, 0f, 1f)));
				if (!BlurPanel.draw(g, 0f, 0f, w, h, r, tint, blurPx(), 1.12f, 0f)) {
					RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xD90C0C0E, Math.max(opacity, 0.5f)));
				}
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x1FFFFFFF, 0x0AFFFFFF);
			}
			case VIBRANT -> {
				RenderUtils.shadow(g, 0f, 1.5f, w, h, r, 5f, ColorUtil.fade(0x33000000, Math.max(opacity, 0.4f)));
				int tint = ColorUtil.withAlpha(0xFFFFFF, Math.round(255f * Math.clamp(opacity * 0.16f, 0f, 1f)));
				if (!BlurPanel.draw(g, 0f, 0f, w, h, r, tint, blurPx(), 1.55f, -0.035f)) {
					RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0x59FFFFFF, opacity));
				}
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x66FFFFFF, 0x14FFFFFF);
			}
			case LIQUID -> {
				RenderUtils.shadow(g, 0f, 2f, w, h, r, 6f, ColorUtil.fade(0x40000000, Math.max(opacity, 0.4f)));
				int tint = ColorUtil.withAlpha(0x10141C, Math.round(255f * Math.clamp(opacity * 0.28f, 0f, 1f)));
				if (!BlurPanel.draw(g, 0f, 0f, w, h, r, tint, blurPx() * 0.75f, 1.3f, 0.45f)) {
					RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xB30F172A, Math.max(opacity, 0.5f)));
				}
				RenderUtils.light(g, 0f, 0f, w, h * 0.45f, r, 0x1FFFFFFF, 0x00FFFFFF);
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px, 0x99FFFFFF, 0x1AFFFFFF);
			}
			case CHROMA -> {
				float hue = time * 60f;
				RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(0xE00A0D16, opacity));
				RenderUtils.stroke(g, 0f, 0f, w, h, r, px * 1.5f, ColorUtil.hsv(hue, 0.75f, 1f), ColorUtil.hsv(hue + 140f, 0.75f, 1f));
			}
			default -> {
			}
		}
	}
}
