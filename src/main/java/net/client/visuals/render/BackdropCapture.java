package net.client.visuals.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import org.jspecify.annotations.Nullable;

/**
 * A copy of the frame as it is right before the GUI is drawn (world, hand, post effects), for HUD
 * glass that blurs what's behind it.
 *
 * <p>Only taken on frames where something asked for it during GUI extraction ({@link #request}),
 * so it costs nothing when no blurred element is on screen. The copy is one GPU-side texture copy;
 * the texture is (re)created at extraction time, never between extraction and drawing, so render
 * states always reference a live texture. Released on resize and client stop.</p>
 */
public final class BackdropCapture {
	private static @Nullable GpuTexture texture;
	private static @Nullable GpuTextureView view;
	private static @Nullable TextureSetup setup;
	private static boolean requested;

	private BackdropCapture() {
	}

	/**
	 * Asks for this frame's capture; call while extracting GUI state.
	 *
	 * @return the texture to sample, or null if the frame can't be captured
	 */
	public static @Nullable TextureSetup request() {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		if (color == null || color.isClosed()) {
			return null;
		}
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		if (texture == null || texture.isClosed() || texture.getWidth(0) != w || texture.getHeight(0) != h
				|| texture.getFormat() != color.getFormat()) {
			release();
			GpuDevice device = RenderSystem.getDevice();
			texture = device.createTexture(() -> "Liquid HUD backdrop", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
					color.getFormat(), w, h, 1, 1);
			view = device.createTextureView(texture);
			setup = TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
		}
		requested = true;
		return setup;
	}

	/** Called right before the GUI renderer draws (see {@code GameRendererMixin}). */
	public static void capture() {
		if (!requested) {
			return;
		}
		requested = false;
		GpuTexture destination = texture;
		GpuTexture color = Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture();
		if (destination == null || destination.isClosed() || color == null || color.isClosed()) {
			return;
		}
		int w = Math.min(color.getWidth(0), destination.getWidth(0));
		int h = Math.min(color.getHeight(0), destination.getHeight(0));
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(color, destination, 0, 0, 0, 0, 0, w, h);
	}

	public static void release() {
		if (view != null) {
			view.close();
			view = null;
		}
		if (texture != null) {
			texture.close();
			texture = null;
		}
		setup = null;
	}
}
