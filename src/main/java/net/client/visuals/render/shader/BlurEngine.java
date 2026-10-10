package net.client.visuals.render.shader;

import net.client.visuals.LiquidConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

/**
 * Frosted-glass backdrop built on the engine's own {@code minecraft:blur} post chain.
 *
 * <h2>How it works in 26.3</h2>
 * The GUI is deferred: screens <i>extract</i> render states into strata, then
 * {@code GuiRenderer} draws them. A stratum can be flagged "blur before this", and the renderer then
 * draws every earlier stratum (world / panorama / HUD), runs the box-blur post chain over the main
 * target, and draws the remaining strata on top. Panels drawn afterwards with a translucent tint
 * read as frosted glass.
 *
 * <p>We reuse that pass instead of writing a separate blur because it already runs on both the
 * OpenGL and Vulkan backends and handles target resizing. Its radius is the {@code MenuBlurRadius}
 * global uniform, which {@code GameRendererMixin} raises while a Liquid screen is open.</p>
 *
 * <h2>Fallback</h2>
 * Only one blur is allowed per frame. If blur is disabled in config or another mod already
 * claimed the pass, callers get {@code false} and should switch to the opaque glass tint.
 */
public final class BlurEngine {
	/** Blur radius used while a Liquid screen is open (vanilla's slider tops out at 10). */
	public static final int GLASS_RADIUS = 9;

	private BlurEngine() {
	}

	/**
	 * Blurs everything extracted before the current stratum.
	 *
	 * @return whether the backdrop behind subsequent strata is frosted
	 */
	public static boolean frostBackdrop(GuiGraphicsExtractor graphics) {
		if (!LiquidConfig.backdropBlur) {
			return false;
		}
		GuiRenderState state = graphics.guiRenderState;
		if (state.firstStratumAfterBlur != Integer.MAX_VALUE) {
			// Someone else already requested this frame's blur – it still frosts what's behind us.
			return true;
		}
		graphics.blurBeforeThisStratum();
		return true;
	}

	/** Radius override consulted by {@code GameRendererMixin}; {@code vanilla} when not overriding. */
	public static int effectiveRadius(int vanilla, boolean liquidScreenOpen) {
		return liquidScreenOpen && LiquidConfig.backdropBlur ? GLASS_RADIUS : vanilla;
	}
}
