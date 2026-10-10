package net.client.visuals.render.background;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.shader.BlurEngine;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import org.jspecify.annotations.Nullable;

/**
 * Main-menu wallpapers.
 *
 * <table>
 *   <tr><th>Mode</th><th>Source</th><th>Glass frost</th></tr>
 *   <tr><td>Lake / Dusk</td><td>bundled 3840 px photos, resampled to the exact screen size</td>
 *       <td>pre-blurred copy sampled through each panel's mask</td></tr>
 *   <tr><td>Your image</td><td>PNG/JPG from {@code config/liquidclient/backgrounds/}</td><td>same</td></tr>
 *   <tr><td>Aurora / Nebula</td><td>procedural GLSL, evaluated per device pixel</td>
 *       <td>same shader with high frequencies removed</td></tr>
 *   <tr><td>Panorama</td><td>vanilla rotating panorama</td><td>engine blur pass</td></tr>
 * </table>
 *
 * <p>Photos are drawn as one quad covering exactly the framebuffer (in GUI units
 * {@code device size / gui scale}), so each texel lands on one screen pixel.</p>
 *
 * <h2>Lifecycle</h2>
 * Owns the wallpaper textures. They're released after {@value #IDLE_SECONDS}s without a menu frame,
 * immediately on world join and on client stop; all on the render thread. Procedural modes own no
 * GPU resources (pipelines belong to the engine's pipeline cache).
 */
public final class BackgroundEngine implements GlassRenderer.Backdrop {
	public static final BackgroundEngine INSTANCE = new BackgroundEngine();

	private static final long IDLE_SECONDS = 10L;

	private final WallpaperTexture wallpaper = new WallpaperTexture();
	private @Nullable Wallpaper active;
	/** Screen size in GUI units that maps exactly onto the framebuffer. */
	private float screenWidth = 1f;
	private float screenHeight = 1f;
	private long lastUsedNanos;
	/** Advances at every GUI state reset; frost is only valid in a frame whose wallpaper was drawn. */
	private static int frame;
	private int extractedFrame = -1;

	private BackgroundEngine() {
	}

	/** Called from {@code GuiRenderStateMixin}: pooled wallpaper states are free again. */
	public static void resetPool() {
		BackdropState.resetPool();
		frame++;
	}

	/** Whether the wallpaper was drawn this frame (so frosted patches of it match what's behind). */
	public boolean drawnThisFrame() {
		return extractedFrame == frame;
	}

	/**
	 * Draws the wallpaper for this frame and configures {@link GlassRenderer} accordingly.
	 * Call from a screen's {@code extractBackground}.
	 */
	public void extract(GuiGraphicsExtractor g, int width, int height) {
		lastUsedNanos = System.nanoTime();
		extractedFrame = frame;
		int scale = HighDpiEngine.scale();
		int deviceW = HighDpiEngine.deviceWidth();
		int deviceH = HighDpiEngine.deviceHeight();
		screenWidth = deviceW / (float) scale;
		screenHeight = deviceH / (float) scale;

		Wallpaper resolved = resolve(deviceW, deviceH);
		if (resolved != active) {
			LiquidClient.LOGGER.info("Menu backdrop -> {}", resolved.id);
		}
		active = resolved;

		if (active == Wallpaper.PANORAMA) {
			Minecraft.getInstance().gameRenderer.panorama().extractRenderState(g, width, height);
			GlassRenderer.beginFrame(BlurEngine.frostBackdrop(g), null);
			return;
		}
		submit(g, 0f, 0f, screenWidth, screenHeight, -1f, false, 1f);
		GlassRenderer.beginFrame(false, this);
	}

	/** Wallpaper actually used this frame: degrades gracefully when something isn't available. */
	private Wallpaper resolve(int deviceW, int deviceH) {
		Wallpaper requested = Wallpaper.byIndex(LiquidConfig.MENU_BACKGROUND.index());
		if (requested == Wallpaper.PANORAMA || !ShaderSupport.backdrops()) {
			return Wallpaper.PANORAMA;
		}
		if (requested.isImage()) {
			wallpaper.request(WallpaperTexture.Source.of(requested), deviceW, deviceH);
			return wallpaper.crisp() != null ? requested : Wallpaper.AURORA; // aurora while decoding / if missing
		}
		return ShaderSupport.liveWallpaper(pipelineFor(requested)) ? requested : Wallpaper.AURORA;
	}

	/** The procedural pipeline of a live wallpaper. */
	public static RenderPipeline pipelineFor(Wallpaper live) {
		return switch (live) {
			case NEBULA -> LiquidPipelines.BACKDROP_NEBULA;
			case SILK -> LiquidPipelines.BACKDROP_SILK;
			case GLOW -> LiquidPipelines.BACKDROP_GLOW;
			case LIQUID -> LiquidPipelines.BACKDROP_LIQUID;
			case RIBBONS -> LiquidPipelines.BACKDROP_RIBBONS;
			case DUNES -> LiquidPipelines.BACKDROP_DUNES;
			default -> LiquidPipelines.BACKDROP_AURORA;
		};
	}

	private boolean showsImage() {
		return active != null && active.isImage();
	}

	@Override
	public boolean frost(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius) {
		if (!LiquidConfig.backdropBlur || active == Wallpaper.PANORAMA || !drawnThisFrame()) {
			return false; // in game the wallpaper isn't behind the panel: use the engine blur instead
		}
		// Honours the opacity stack, so frosted glass fades in with its surface instead of popping.
		float opacity = RenderUtils.alpha();
		if (opacity > 0.004f) {
			submit(g, x, y, w, h, Math.max(0f, radius), true, opacity);
		}
		return true;
	}

	/**
	 * "Unblur" overlay for intros: the frosted wallpaper over the whole screen at {@code opacity}.
	 * Fading it out reads like the macOS lock screen waking up.
	 */
	public void frostOverlay(GuiGraphicsExtractor g, float opacity) {
		if (opacity > 0.004f && drawnThisFrame() && showsImage() && wallpaper.frost() != null) {
			submit(g, 0f, 0f, screenWidth, screenHeight, -1f, true, opacity);
		}
	}

	/** Screen-aligned frosted wallpaper for glass effects (the clock), or null if there is none. */
	public @Nullable TextureSetup frostTexture() {
		return showsImage() ? wallpaper.frost() : null;
	}

	public float screenWidth() {
		return screenWidth;
	}

	public float screenHeight() {
		return screenHeight;
	}

	private void submit(GuiGraphicsExtractor g, float x, float y, float w, float h, float maskRadius, boolean frosted, float opacity) {
		BackdropState state = BackdropState.obtain().geometry(g.pose(), g.scissorStack.peek(), x, y, w, h, maskRadius);
		if (showsImage()) {
			TextureSetup texture = frosted ? wallpaper.frost() : wallpaper.crisp();
			if (texture == null) {
				BackdropState.recycleLast();
				return;
			}
			// Textures are already cropped to the screen: uv is simply position / screen size.
			int color = ColorUtil.withAlpha(0xFFFFFF, Math.round(opacity * 255f));
			state.image(texture, color, x / screenWidth, y / screenHeight, (x + w) / screenWidth, (y + h) / screenHeight);
		} else {
			RenderPipeline pipeline = pipelineFor(active == null ? Wallpaper.AURORA : active);
			float time = UiFrame.time * (LiquidConfig.reduceMotion ? 0.15f : 1f) % 3600f;
			int color = ColorUtil.withAlpha(0xFFFFFF, frosted ? 255 : 0);
			state.procedural(pipeline, color, time, screenHeight);
		}
		if (state.updateBounds()) {
			g.guiRenderState.addGuiElement(state);
		} else {
			BackdropState.recycleLast();
		}
	}

	/** Client tick: drop wallpaper textures once no menu has drawn them for a while. */
	public void tick() {
		if (wallpaper.state() != WallpaperTexture.State.IDLE && System.nanoTime() - lastUsedNanos > IDLE_SECONDS * 1_000_000_000L) {
			wallpaper.release();
		}
	}

	/**
	 * The menu was removed: start a short grace period (sub-screens like Options often return within
	 * seconds) after which {@link #tick()} frees the textures unless a menu draws them again.
	 */
	public void onMenuHidden() {
		lastUsedNanos = System.nanoTime() - (IDLE_SECONDS - 3L) * 1_000_000_000L;
	}

	/** Frees everything now (world join, client stop). Render thread. */
	public void release() {
		wallpaper.release();
	}

	/** Re-scan the backgrounds folder on next use (e.g. after the user switches modes). */
	public void reloadWallpaper() {
		wallpaper.release();
	}
}
