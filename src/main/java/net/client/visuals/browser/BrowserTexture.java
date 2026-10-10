package net.client.visuals.browser;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.renderer.texture.AbstractTexture;

/**
 * GPU texture the browser frames are streamed into. Unlike {@code DynamicTexture} it keeps no CPU
 * copy (frames are uploaded straight from the decoded image) and samples with LINEAR filtering so
 * text stays readable on a screen viewed at an angle. Registered in the TextureManager so vanilla
 * render types can bind it by id.
 */
final class BrowserTexture extends AbstractTexture {
	private final int width;
	private final int height;

	BrowserTexture(String label, int width, int height) {
		this.width = width;
		this.height = height;
		GpuDevice device = RenderSystem.getDevice();
		// usage 5 = COPY_DST | TEXTURE_BINDING (same as DynamicTexture)
		this.texture = device.createTexture(() -> label, 5, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
		this.textureView = device.createTextureView(this.texture);
		this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
	}

	int width() {
		return width;
	}

	int height() {
		return height;
	}

	/** Render thread, outside a render pass. Image size must match. */
	void upload(NativeImage image) {
		if (texture != null && image.getWidth() == width && image.getHeight() == height) {
			RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image);
		}
	}
}
