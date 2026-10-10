package net.client.visuals.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.AbstractTexture;

/**
 * A GPU texture filled from a {@link NativeImage}, without keeping a CPU copy, sampled LINEAR /
 * clamp. Used for textures that are produced at exact device resolution (glass text masks).
 */
public final class GpuImage extends AbstractTexture {
	private final int width;
	private final int height;

	public GpuImage(String label, NativeImage image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		GpuDevice device = RenderSystem.getDevice();
		// usage 5 = COPY_DST | TEXTURE_BINDING
		this.texture = device.createTexture(() -> label, 5, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
		this.textureView = device.createTextureView(this.texture);
		this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, image);
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public GpuTextureView view() {
		return textureView;
	}

	public GpuSampler linearSampler() {
		return sampler;
	}
}
