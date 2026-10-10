package net.client.visuals.render.background;

import net.client.visuals.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;

/**
 * Textured quads with an anti-aliased rounded-rectangle mask (album art, thumbnails), drawn with the
 * wallpaper pipeline's pooled states. The mask lives in the quad's local space, so it follows pose
 * rotation / scale (spinning vinyl, hover zoom).
 */
public final class ImageDraw {
	private ImageDraw() {
	}

	/**
	 * @param radius corner radius in GUI units; {@code min(w, h) / 2} gives a circle
	 * @param tint   rgb multiplier + opacity (also multiplied by the {@link RenderUtils} alpha stack)
	 */
	public static void rounded(GuiGraphicsExtractor g, TextureSetup texture, float x, float y, float w, float h, float radius,
			int tint) {
		rounded(g, texture, x, y, w, h, radius, tint, 0f, 0f, 1f, 1f);
	}

	/** Same, sampling the sub-rectangle {@code (u0, v0)–(u1, v1)} of the texture. */
	public static void rounded(GuiGraphicsExtractor g, TextureSetup texture, float x, float y, float w, float h, float radius,
			int tint, float u0, float v0, float u1, float v1) {
		int color = RenderUtils.alpha() >= 1f ? tint : net.client.visuals.render.ColorUtil.fade(tint, RenderUtils.alpha());
		if ((color >>> 24) == 0 || w <= 0f || h <= 0f) {
			return;
		}
		BackdropState state = BackdropState.obtain().geometry(g.pose(), g.scissorStack.peek(), x, y, w, h, Math.max(0f, radius));
		state.image(texture, color, u0, v0, u1, v1);
		if (state.updateBounds()) {
			g.guiRenderState.addGuiElement(state);
		} else {
			BackdropState.recycleLast();
		}
	}
}
