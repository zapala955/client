package net.client.visuals.render.background;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A photographic wallpaper, prepared for pixel-perfect display.
 *
 * <h2>Why it's sharp</h2>
 * The source (a bundled 3840×2560 photo, or any PNG/JPG the user drops into
 * {@code config/liquidclient/backgrounds/}) is decoded with stb, cover-cropped and resampled
 * <b>once, on the CPU, to the exact framebuffer size</b> with stb_image_resize's gamma-correct
 * filter. The GPU then draws it 1:1 – one texel per screen pixel – so there is no minification
 * shimmer and no upscaling blur at any resolution. Resizing the window re-runs the preparation.
 *
 * <h2>Frost</h2>
 * A small, heavily blurred copy (with the frosted-glass brighten/desaturate baked in) backs glass
 * panels and the glass clock, sampled screen-aligned through each panel's mask.
 *
 * <h2>Threads & lifetime</h2>
 * Decoding / resampling run on a worker; textures are created on the render thread. The previous
 * textures stay on screen until the new ones are ready (no flash on resize). Results that arrive
 * after {@link #release()} or a newer request are discarded via a generation counter.
 */
final class WallpaperTexture {
	static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("liquidclient/backgrounds");
	private static final Identifier CRISP_ID = LiquidClient.id("dynamic/wallpaper");
	private static final Identifier FROST_ID = LiquidClient.id("dynamic/wallpaper_frost");

	/**
	 * What to show: a bundled photo (Unsplash License) with the vertical crop anchor that keeps its
	 * subject, or the user's own image ({@code resource == null}).
	 */
	record Source(@Nullable String resource, float anchorY) {
		static Source of(Wallpaper wallpaper) {
			return new Source(wallpaper.kind == Wallpaper.Kind.CUSTOM ? null : wallpaper.resource, wallpaper.anchorY);
		}
	}

	enum State { IDLE, LOADING, READY, MISSING }

	private State state = State.IDLE;
	private int generation;
	private @Nullable Source loadedSource;
	private int loadedWidth;
	private int loadedHeight;
	private @Nullable Source pendingSource;
	private int pendingWidth;
	private int pendingHeight;
	private @Nullable TextureSetup crisp;
	private @Nullable TextureSetup frost;

	State state() {
		return state;
	}

	@Nullable TextureSetup crisp() {
		return crisp;
	}

	@Nullable TextureSetup frost() {
		return frost;
	}

	/**
	 * Ensures textures for {@code source} at exactly {@code width}×{@code height} device pixels are
	 * (being) prepared. Cheap to call every frame. Render thread.
	 */
	void request(Source source, int width, int height) {
		if (width <= 0 || height <= 0) {
			return;
		}
		boolean current = source.equals(loadedSource) && width == loadedWidth && height == loadedHeight;
		boolean pending = source.equals(pendingSource) && width == pendingWidth && height == pendingHeight;
		if (current || pending || (state == State.MISSING && source.equals(loadedSource))) {
			return;
		}
		pendingSource = source;
		pendingWidth = width;
		pendingHeight = height;
		if (crisp == null) {
			state = State.LOADING;
		}
		int token = ++generation;
		CompletableFuture.supplyAsync(() -> {
					try {
						return prepare(source, width, height);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}, Util.backgroundExecutor())
				.whenComplete((images, error) -> Minecraft.getInstance().execute(() -> accept(token, source, width, height, images, error)));
	}

	private void accept(int token, Source source, int width, int height, NativeImage @Nullable [] images, @Nullable Throwable error) {
		if (token != generation) {
			close(images);
			return;
		}
		pendingSource = null;
		loadedSource = source;
		loadedWidth = width;
		loadedHeight = height;
		if (error != null || images == null) {
			if (error != null) {
				LiquidClient.LOGGER.warn("Wallpaper {} failed", source, error);
			}
			if (crisp == null) {
				state = State.MISSING;
			}
			return;
		}
		crisp = upload(CRISP_ID, "Liquid wallpaper", images[0]);
		frost = upload(FROST_ID, "Liquid wallpaper (frost)", images[1]);
		state = State.READY;
		LiquidClient.LOGGER.info("Wallpaper: {} prepared at {}x{}", source, width, height);
	}

	private static void close(NativeImage @Nullable [] images) {
		if (images != null) {
			for (NativeImage image : images) {
				image.close();
			}
		}
	}

	private static TextureSetup upload(Identifier id, String label, NativeImage image) {
		DynamicTexture texture = new DynamicTexture(() -> label, image);
		texture.setPixels(new NativeImage(1, 1, false)); // GPU has it; drop the CPU copy
		Minecraft.getInstance().getTextureManager().register(id, texture);
		return TextureSetup.singleTexture(texture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
	}

	/** Frees both textures; the next request starts from scratch (picks up new files). */
	void release() {
		generation++;
		if (crisp != null || frost != null) {
			Minecraft.getInstance().getTextureManager().release(CRISP_ID);
			Minecraft.getInstance().getTextureManager().release(FROST_ID);
		}
		crisp = null;
		frost = null;
		loadedSource = null;
		pendingSource = null;
		state = State.IDLE;
	}

	// ---- Worker thread ------------------------------------------------------------------------

	private static NativeImage @Nullable [] prepare(Source source, int width, int height) throws IOException {
		byte[] encoded = readSource(source);
		if (encoded == null) {
			return null;
		}
		ByteBuffer file = MemoryUtil.memAlloc(encoded.length);
		ByteBuffer pixels;
		int srcW;
		int srcH;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			file.put(encoded).flip();
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			pixels = STBImage.stbi_load_from_memory(file, w, h, comp, 4);
			if (pixels == null) {
				throw new IOException("Couldn't decode wallpaper: " + STBImage.stbi_failure_reason());
			}
			srcW = w.get(0);
			srcH = h.get(0);
		} finally {
			MemoryUtil.memFree(file);
		}
		try {
			// Cover-crop to the screen's aspect ratio around the photo's anchor.
			float target = width / (float) height;
			int cropW = srcW;
			int cropH = Math.round(srcW / target);
			if (cropH > srcH) {
				cropH = srcH;
				cropW = Math.round(srcH * target);
			}
			int cropX = (srcW - cropW) / 2;
			int cropY = Math.round((srcH - cropH) * source.anchorY);
			long cropStart = MemoryUtil.memAddress(pixels) + ((long) cropY * srcW + cropX) * 4L;
			ByteBuffer cropped = MemoryUtil.memByteBuffer(cropStart, (cropH - 1) * srcW * 4 + cropW * 4);

			NativeImage sharp = new NativeImage(width, height, false);
			ByteBuffer sharpPixels = MemoryUtil.memByteBuffer(sharp.getPointer(), width * height * 4);
			if (STBImageResize.stbir_resize_uint8_srgb(cropped, cropW, cropH, srcW * 4, sharpPixels, width, height, width * 4,
					STBImageResize.STBIR_RGBA) == null) {
				sharp.close();
				throw new IOException("Wallpaper resize failed");
			}
			return new NativeImage[]{sharp, frostCopy(sharpPixels, width, height)};
		} finally {
			STBImage.stbi_image_free(pixels);
		}
	}

	private static byte @Nullable [] readSource(Source source) throws IOException {
		if (source.resource != null) {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(LiquidClient.id(source.resource));
			if (resource.isEmpty()) {
				return null;
			}
			try (InputStream in = resource.get().open()) {
				return in.readAllBytes();
			}
		}
		Path file = findCustom();
		return file == null ? null : Files.readAllBytes(file);
	}

	private static @Nullable Path findCustom() throws IOException {
		Files.createDirectories(DIRECTORY);
		for (String preferred : new String[]{"wallpaper.png", "wallpaper.jpg", "wallpaper.jpeg"}) {
			Path candidate = DIRECTORY.resolve(preferred);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
		}
		List<Path> images = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(DIRECTORY, "*.{png,PNG,jpg,JPG,jpeg,JPEG}")) {
			for (Path path : stream) {
				images.add(path);
			}
		}
		images.sort((a, b) -> a.getFileName().toString().toLowerCase(Locale.ROOT).compareTo(b.getFileName().toString().toLowerCase(Locale.ROOT)));
		return images.isEmpty() ? null : images.getFirst();
	}

	/**
	 * Frost: downscale to ~1/8 with stb, 3-pass box blur (≈ gaussian), then the glass "lift"
	 * (frosted glass scatters light: slightly brighter, slightly desaturated).
	 */
	private static NativeImage frostCopy(ByteBuffer sharp, int width, int height) {
		int fw = Math.max(16, width / 8);
		int fh = Math.max(9, height / 8);
		ByteBuffer small = MemoryUtil.memAlloc(fw * fh * 4);
		try {
			STBImageResize.stbir_resize_uint8_srgb(sharp, width, height, width * 4, small, fw, fh, fw * 4, STBImageResize.STBIR_RGBA);
			int[] rgb = new int[fw * fh * 3];
			for (int i = 0; i < fw * fh; i++) {
				rgb[i * 3] = small.get(i * 4) & 0xFF;
				rgb[i * 3 + 1] = small.get(i * 4 + 1) & 0xFF;
				rgb[i * 3 + 2] = small.get(i * 4 + 2) & 0xFF;
			}
			int radius = Math.max(2, fw / 90);
			int[] tmp = new int[rgb.length];
			for (int pass = 0; pass < 3; pass++) {
				boxBlur(rgb, tmp, fw, fh, radius, true);
				boxBlur(tmp, rgb, fw, fh, radius, false);
			}
			NativeImage out = new NativeImage(fw, fh, false);
			for (int y = 0; y < fh; y++) {
				for (int x = 0; x < fw; x++) {
					int i = (y * fw + x) * 3;
					int r = rgb[i];
					int g = rgb[i + 1];
					int b = rgb[i + 2];
					int luma = (r * 77 + g * 150 + b * 29) >> 8;
					r = lift(r, luma);
					g = lift(g, luma);
					b = lift(b, luma);
					out.setPixel(x, y, 0xFF000000 | r << 16 | g << 8 | b);
				}
			}
			return out;
		} finally {
			MemoryUtil.memFree(small);
		}
	}

	private static int lift(int channel, int luma) {
		float v = channel * 0.82f + luma * 0.18f; // a touch of desaturation
		v = v * 1.06f + 10f;                      // scattered light
		return Math.min(255, Math.round(v));
	}

	private static void boxBlur(int[] src, int[] dst, int w, int h, int radius, boolean horizontal) {
		int span = radius * 2 + 1;
		int lines = horizontal ? h : w;
		int length = horizontal ? w : h;
		for (int line = 0; line < lines; line++) {
			for (int c = 0; c < 3; c++) {
				int sum = 0;
				for (int k = -radius; k <= radius; k++) {
					sum += src[index(line, Math.clamp(k, 0, length - 1), w, horizontal) * 3 + c];
				}
				for (int i = 0; i < length; i++) {
					dst[index(line, i, w, horizontal) * 3 + c] = sum / span;
					int add = Math.min(length - 1, i + radius + 1);
					int remove = Math.max(0, i - radius);
					sum += src[index(line, add, w, horizontal) * 3 + c] - src[index(line, remove, w, horizontal) * 3 + c];
				}
			}
		}
	}

	private static int index(int line, int i, int w, boolean horizontal) {
		return horizontal ? line * w + i : i * w + line;
	}
}
