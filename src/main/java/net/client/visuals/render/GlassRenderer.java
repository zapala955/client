package net.client.visuals.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

/**
 * The "Liquid Glass" material, composed from {@link RenderUtils} primitives in a fixed layer order
 * (see the apple-glass-ui skill):
 *
 * <pre>
 *   0  soft drop shadow (under everything, offset by elevation)
 *   1  BACKDROP   – frosted copy of what's behind (engine blur pass or {@link Backdrop})
 *   2  SURFACE    – translucent slate tint rgba(15,23,42,.55) + additive specular sheen
 *   3  RIM        – 1-device-pixel gradient border .20 → .05, plus an inset highlight
 *   4  CONTENT    – text / icons, drawn by the caller afterwards
 * </pre>
 *
 * <p>If nothing can frost the backdrop (blur off / unavailable) the tint switches to an almost
 * opaque slate so text never sits on a busy, unblurred background.</p>
 */
public final class GlassRenderer {
	/** Provides a frosted copy of the background inside a rounded region (layer 1). */
	public interface Backdrop {
		/** @return whether the region is now frosted */
		boolean frost(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius);
	}

	private static boolean frosted = true;
	private static @Nullable Backdrop backdrop;

	private GlassRenderer() {
	}

	/**
	 * Screens call this once per frame before drawing glass.
	 *
	 * @param frostedBackdrop the whole backdrop was blurred by the engine's blur pass
	 * @param regionBackdrop  optional per-panel frost provider (main menu wallpapers)
	 */
	public static void beginFrame(boolean frostedBackdrop, @Nullable Backdrop regionBackdrop) {
		frosted = frostedBackdrop;
		backdrop = regionBackdrop;
	}

	public static void beginFrame(boolean frostedBackdrop) {
		beginFrame(frostedBackdrop, null);
	}

	public static boolean frosted() {
		return frosted;
	}

	/** Border thickness: exactly one device pixel. */
	public static float hairline() {
		return HighDpiEngine.px();
	}

	/** Standard floating glass panel. {@code elevation} (0..4) controls shadow depth. */
	public static void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float elevation) {
		panel(g, x, y, w, h, radius, elevation, 0f);
	}

	/** Glass panel with a hover amount: brighter rim + active inner glow. */
	public static void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float elevation, float hover) {
		float sx = HighDpiEngine.snap(x);
		float sy = HighDpiEngine.snap(y);
		float sw = HighDpiEngine.snapSize(w);
		float sh = HighDpiEngine.snapSize(h);
		boolean frost = frosted;
		if (elevation > 0f) {
			shadow(g, sx, sy, sw, sh, radius, elevation);
		}
		if (backdrop != null && backdrop.frost(g, sx, sy, sw, sh, radius)) {
			frost = true;
		}
		int fill = frost ? Palette.GLASS_FILL : Palette.GLASS_FILL_OPAQUE;
		// Slightly lighter slate at the top edge, as if lit from above.
		layers(g, sx, sy, sw, sh, radius, ColorUtil.lerpRgb(fill, 0xFF1E293B, 0.22f), fill,
				ColorUtil.lerp(Palette.GLASS_BORDER_HIGHLIGHT, Palette.GLASS_BORDER_HOVER, hover), Palette.GLASS_BORDER);
		if (hover > 0.01f) {
			innerGlow(g, sx, sy, sw, sh, radius, 0xFFFFFFFF, hover);
		}
	}

	/**
	 * "Clear" Liquid Glass, as on the macOS lock screen: the frosted backdrop lifted with a white
	 * tint, a bright 1px rim and a faint shadow. Unlike the slate {@link #panel} it keeps the colour of
	 * the photo behind it. Without a frosted backdrop it falls back to the slate tint so white content
	 * stays legible on the unblurred background.
	 */
	public static void clear(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float hover) {
		float sx = HighDpiEngine.snap(x);
		float sy = HighDpiEngine.snap(y);
		float sw = HighDpiEngine.snapSize(w);
		float sh = HighDpiEngine.snapSize(h);
		RenderUtils.shadow(g, sx, sy + 1f, sw, sh, radius, 3f, Palette.CLEAR_SHADOW);
		boolean frost = frosted;
		if (backdrop != null && backdrop.frost(g, sx, sy, sw, sh, radius)) {
			frost = true;
		}
		if (frost) {
			RenderUtils.gradient(g, sx, sy, sw, sh, radius, Palette.CLEAR_FILL_TOP, Palette.CLEAR_FILL_BOTTOM);
			if (hover > 0.01f) {
				RenderUtils.fill(g, sx, sy, sw, sh, radius, ColorUtil.fade(Palette.CLEAR_FILL_HOVER, hover));
			}
		} else {
			RenderUtils.gradient(g, sx, sy, sw, sh, radius, ColorUtil.lerpRgb(Palette.GLASS_FILL_OPAQUE, 0xFF1E293B, 0.22f),
					Palette.GLASS_FILL_OPAQUE);
		}
		RenderUtils.stroke(g, sx, sy, sw, sh, radius, hairline(),
				ColorUtil.lerp(Palette.CLEAR_RIM_TOP, Palette.CLEAR_RIM_HOVER, hover), Palette.CLEAR_RIM_BOTTOM);
	}

	/** Glass surface with explicit colours, for buttons / cards / pills (layers 0, 2, 3). */
	public static void surface(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float elevation,
			int fillTop, int fillBottom, int borderTop, int borderBottom) {
		if (elevation > 0f) {
			shadow(g, x, y, w, h, radius, elevation);
		}
		layers(g, x, y, w, h, radius, fillTop, fillBottom, borderTop, borderBottom);
	}

	private static void shadow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float elevation) {
		int shadow = ColorUtil.fade(Palette.SHADOW, Math.min(1f, 0.5f + elevation * 0.15f));
		RenderUtils.shadow(g, x, y + elevation * 0.75f, w, h, radius, 3f + elevation * 3f, shadow);
	}

	private static void layers(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
			int fillTop, int fillBottom, int borderTop, int borderBottom) {
		// Surface
		RenderUtils.gradient(g, x, y, w, h, radius, fillTop, fillBottom);
		RenderUtils.light(g, x, y, w, h * 0.5f, radius, Palette.GLASS_SHEEN_TOP, Palette.GLASS_SHEEN_BOTTOM);
		// Rim + inset highlight
		float line = hairline();
		RenderUtils.stroke(g, x, y, w, h, radius, line, borderTop, borderBottom);
		if (w > line * 4f && h > line * 4f) {
			RenderUtils.stroke(g, x + line, y + line, w - line * 2f, h - line * 2f, Math.max(0f, radius - line), line,
					Palette.GLASS_INNER_HIGHLIGHT, 0x03FFFFFF);
		}
	}

	/** "Active inner glow" for hovered glass: luminous inset rim + faint lift of the whole surface. */
	public static void innerGlow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color, float intensity) {
		if (intensity <= 0.01f) {
			return;
		}
		float line = hairline();
		RenderUtils.light(g, x, y, w, h, radius, ColorUtil.fade(color, 0.07f * intensity), ColorUtil.fade(color, 0.02f * intensity));
		RenderUtils.stroke(g, x + line, y + line, w - line * 2f, h - line * 2f, Math.max(0f, radius - line), line * 1.5f,
				ColorUtil.fade(color, 0.22f * intensity), ColorUtil.fade(color, 0.06f * intensity));
	}

	/** Coloured bloom around a selected / focused element (outside only, so the glass stays clear). */
	public static void accentGlow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color, float intensity) {
		if (intensity <= 0.01f) {
			return;
		}
		RenderUtils.outerGlow(g, x, y, w, h, radius, 5f + 5f * intensity, ColorUtil.fade(color, 0.6f * intensity));
	}
}
