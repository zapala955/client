package net.client.visuals.render.loading;

import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.render.GpuImage;
import net.client.visuals.render.background.Wallpaper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Textures of the loading screens, produced at the exact device size they're drawn at.
 *
 * <p>The resource-loading overlay is up before the client's shaders, fonts and textures exist (they
 * load in the very reload it shows), so the art is read straight from the mod jar and drawn with
 * vanilla's textured GUI pipeline. The masters are large ({@code tools/loading_art.py}); they're
 * box-filtered down on the CPU (premultiplied, so coloured edges don't fringe) once per size, so
 * edges stay crisp without mipmaps. The wallpaper is decoded and frosted on a worker thread.</p>
 */
public final class BootArt {
	private static final String DIR = "/assets/liquidclient/textures/gui/loading/";
	public static final Identifier DROP = LiquidClient.id("dynamic/boot_drop");
	public static final Identifier DOT = LiquidClient.id("dynamic/boot_dot");
	public static final Identifier GLOW = LiquidClient.id("dynamic/boot_glow");
	public static final Identifier TILE = LiquidClient.id("dynamic/boot_tile");
	public static final Identifier WORDMARK = LiquidClient.id("dynamic/boot_wordmark");
	public static final Identifier WALLPAPER = LiquidClient.id("dynamic/boot_wallpaper");

	private static final Art drop = new Art("drop.png", DROP);
	private static final Art dot = new Art("dot.png", DOT);
	private static final Art tile = new Art("tile.png", TILE);
	private static final Art wordmark = new Art("wordmark.png", WORDMARK);
	private static boolean glowReady;
	private static boolean failed;

	private static volatile @Nullable NativeImage pendingWallpaper;
	private static boolean wallpaperRequested;
	private static boolean wallpaperReady;
	private static int wallpaperW;
	private static int wallpaperH;

	private BootArt() {
	}

	/** An RGBA master, premultiplied, with the size it's currently uploaded at. */
	private static final class Art {
		final String file;
		final Identifier id;
		float @Nullable [] rgba;
		int width;
		int height;
		int uploadedW = -1;
		int uploadedH = -1;

		Art(String file, Identifier id) {
			this.file = file;
			this.id = id;
		}

		/** Uploads the art at {@code w}×{@code h} device pixels if it isn't already. */
		boolean ensure(int w, int h) {
			if (w == uploadedW && h == uploadedH) {
				return true;
			}
			if (rgba == null && !load()) {
				return false;
			}
			if (!upload(id, downscale(this, w, h))) {
				return false;
			}
			uploadedW = w;
			uploadedH = h;
			return true;
		}

		private boolean load() {
			if (failed) {
				return false;
			}
			try (InputStream in = BootArt.class.getResourceAsStream(DIR + file)) {
				if (in == null) {
					throw new IOException("missing " + file);
				}
				try (NativeImage image = NativeImage.read(in)) {
					width = image.getWidth();
					height = image.getHeight();
					float[] data = new float[width * height * 4];
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {
							int argb = image.getPixel(x, y);
							float a = (argb >>> 24) / 255f;
							int i = (y * width + x) * 4;
							data[i] = (argb >> 16 & 0xFF) / 255f * a;
							data[i + 1] = (argb >> 8 & 0xFF) / 255f * a;
							data[i + 2] = (argb & 0xFF) / 255f * a;
							data[i + 3] = a;
						}
					}
					rgba = data;
					return true;
				}
			} catch (IOException | RuntimeException e) {
				failed = true;
				LiquidClient.LOGGER.warn("Loading screen art unavailable", e);
				return false;
			}
		}
	}

	/** Makes {@link #DROP} exactly {@code px} device pixels square. */
	public static boolean drop(int px) {
		return drop.ensure(px, px);
	}

	/** Makes {@link #DOT} (the progress bar's caps) exactly {@code px} device pixels square. */
	public static boolean dot(int px) {
		return dot.ensure(px, px);
	}

	/** The app icon (gradient squircle with the drop), {@code px} device pixels square. */
	public static boolean tile(int px) {
		return tile.ensure(px, px);
	}

	/** The "Liquid Client" wordmark at {@code heightPx}; returns its width (0 if unavailable). */
	public static int wordmark(int heightPx) {
		if (wordmark.rgba == null && !wordmark.load()) {
			return 0;
		}
		int w = Math.round(wordmark.width * heightPx / (float) wordmark.height);
		return wordmark.ensure(w, heightPx) ? w : 0;
	}

	/** The soft glow (linear-filtered when stretched; it's blurry by design). */
	public static boolean glow() {
		if (glowReady) {
			return true;
		}
		Art glow = new Art("glow.png", GLOW);
		if (!glow.ensure(256, 256)) {
			return false;
		}
		glowReady = true;
		return true;
	}

	/**
	 * The menu wallpaper, frosted, for the boot screen. Decoding starts on first call (on a worker
	 * thread); returns true once the texture is up.
	 */
	public static boolean wallpaper(int deviceW, int deviceH) {
		if (!wallpaperRequested) {
			wallpaperRequested = true;
			Thread worker = new Thread(() -> {
				try {
					pendingWallpaper = frostedWallpaper(deviceW, deviceH);
				} catch (Throwable t) {
					LiquidClient.LOGGER.warn("Boot wallpaper unavailable", t);
				}
			}, "Liquid boot wallpaper");
			worker.setDaemon(true);
			worker.start();
		}
		NativeImage ready = pendingWallpaper;
		if (ready != null) {
			pendingWallpaper = null;
			wallpaperW = ready.getWidth();
			wallpaperH = ready.getHeight();
			wallpaperReady = upload(WALLPAPER, ready);
		}
		return wallpaperReady;
	}

	public static int wallpaperWidth() {
		return wallpaperW;
	}

	public static int wallpaperHeight() {
		return wallpaperH;
	}

	// ---- Worker: decode, cover-crop, shrink, blur (same look as the menu's frosted wallpaper) ----

	private static @Nullable NativeImage frostedWallpaper(int screenW, int screenH) throws IOException {
		// The selected wallpaper: its photo, the user's image, or a live wallpaper's pre-rendered
		// frosted first frame, so the splash dissolves into exactly what the menu opens on.
		Wallpaper selected = Wallpaper.byIndex(LiquidConfig.MENU_BACKGROUND.index());
		float anchor = selected.anchorY;
		byte[] encoded = null;
		if (selected == Wallpaper.IMAGE) {
			Path custom = customWallpaper();
			if (custom != null) {
				encoded = Files.readAllBytes(custom);
			}
		}
		if (selected == Wallpaper.PANORAMA) {
			return null;
		}
		if (encoded == null) {
			String path = selected.isLive() ? selected.bootFrame()
					: "/assets/liquidclient/" + (selected.resource != null ? selected.resource : Wallpaper.LAKE.resource);
			if (selected.isLive()) {
				anchor = 0.5f;
			} else if (selected.resource == null) {
				anchor = Wallpaper.LAKE.anchorY;
			}
			try (InputStream in = BootArt.class.getResourceAsStream(path)) {
				if (in == null) {
					return null;
				}
				encoded = in.readAllBytes();
			}
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
				throw new IOException("Couldn't decode boot wallpaper: " + STBImage.stbi_failure_reason());
			}
			srcW = w.get(0);
			srcH = h.get(0);
		} finally {
			MemoryUtil.memFree(file);
		}
		try {
			float aspect = Math.max(1, screenW) / (float) Math.max(1, screenH);
			int cropW = srcW;
			int cropH = Math.round(srcW / aspect);
			if (cropH > srcH) {
				cropH = srcH;
				cropW = Math.round(srcH * aspect);
			}
			int cropX = (srcW - cropW) / 2;
			int cropY = Math.round((srcH - cropH) * anchor);
			long start = MemoryUtil.memAddress(pixels) + ((long) cropY * srcW + cropX) * 4L;
			ByteBuffer cropped = MemoryUtil.memByteBuffer(start, (cropH - 1) * srcW * 4 + cropW * 4);
			int fw = 240;
			int fh = Math.max(9, Math.round(fw / aspect));
			ByteBuffer small = MemoryUtil.memAlloc(fw * fh * 4);
			try {
				STBImageResize.stbir_resize_uint8_srgb(cropped, cropW, cropH, srcW * 4, small, fw, fh, fw * 4, STBImageResize.STBIR_RGBA);
				int[] rgb = new int[fw * fh * 3];
				for (int i = 0; i < fw * fh; i++) {
					rgb[i * 3] = small.get(i * 4) & 0xFF;
					rgb[i * 3 + 1] = small.get(i * 4 + 1) & 0xFF;
					rgb[i * 3 + 2] = small.get(i * 4 + 2) & 0xFF;
				}
				int[] tmp = new int[rgb.length];
				for (int pass = 0; pass < 3; pass++) {
					blur(rgb, tmp, fw, fh, 3, true);
					blur(tmp, rgb, fw, fh, 3, false);
				}
				NativeImage out = new NativeImage(fw, fh, false);
				for (int y = 0; y < fh; y++) {
					for (int x = 0; x < fw; x++) {
						int i = (y * fw + x) * 3;
						int luma = (rgb[i] * 77 + rgb[i + 1] * 150 + rgb[i + 2] * 29) >> 8;
						// Frosted glass scatters light: a touch brighter and less saturated.
						int r = Math.min(255, (rgb[i] * 85 + luma * 15) / 100 + 6);
						int g = Math.min(255, (rgb[i + 1] * 85 + luma * 15) / 100 + 6);
						int b = Math.min(255, (rgb[i + 2] * 85 + luma * 15) / 100 + 6);
						out.setPixel(x, y, 0xFF000000 | r << 16 | g << 8 | b);
					}
				}
				return out;
			} finally {
				MemoryUtil.memFree(small);
			}
		} finally {
			STBImage.stbi_image_free(pixels);
		}
	}

	private static @Nullable Path customWallpaper() throws IOException {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve("liquidclient").resolve("backgrounds");
		if (!Files.isDirectory(dir)) {
			return null;
		}
		for (String preferred : new String[]{"wallpaper.png", "wallpaper.jpg", "wallpaper.jpeg"}) {
			Path candidate = dir.resolve(preferred);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
		}
		List<Path> images = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{png,PNG,jpg,JPG,jpeg,JPEG}")) {
			for (Path path : stream) {
				images.add(path);
			}
		}
		images.sort((a, b) -> a.getFileName().toString().toLowerCase(Locale.ROOT).compareTo(b.getFileName().toString().toLowerCase(Locale.ROOT)));
		return images.isEmpty() ? null : images.getFirst();
	}

	/** One box-blur pass over interleaved RGB, horizontal or vertical, clamped at the edges. */
	private static void blur(int[] src, int[] dst, int w, int h, int radius, boolean horizontal) {
		int span = radius * 2 + 1;
		int lines = horizontal ? h : w;
		int length = horizontal ? w : h;
		for (int line = 0; line < lines; line++) {
			for (int c = 0; c < 3; c++) {
				int sum = 0;
				for (int k = -radius; k <= radius; k++) {
					sum += src[index(line, Math.clamp(k, 0, length - 1), w, horizontal) + c];
				}
				for (int i = 0; i < length; i++) {
					dst[index(line, i, w, horizontal) + c] = sum / span;
					int out = Math.clamp(i - radius, 0, length - 1);
					int in = Math.clamp(i + radius + 1, 0, length - 1);
					sum += src[index(line, in, w, horizontal) + c] - src[index(line, out, w, horizontal) + c];
				}
			}
		}
	}

	private static int index(int line, int i, int w, boolean horizontal) {
		return (horizontal ? line * w + i : i * w + line) * 3;
	}

	// ---- Filtering / upload ----------------------------------------------------------------------

	/** Exact box filter (area average) on premultiplied RGBA, separable, then un-premultiplied. */
	private static NativeImage downscale(Art art, int targetW, int targetH) {
		int sw = art.width;
		int sh = art.height;
		float[] src = art.rgba;
		float kx = sw / (float) targetW;
		float ky = sh / (float) targetH;
		float[] horizontal = new float[targetW * sh * 4];
		for (int tx = 0; tx < targetW; tx++) {
			float x0 = tx * kx;
			float x1 = x0 + kx;
			for (int y = 0; y < sh; y++) {
				for (int c = 0; c < 4; c++) {
					horizontal[(y * targetW + tx) * 4 + c] = span(src, (y * sw) * 4 + c, 4, x0, x1, sw) / kx;
				}
			}
		}
		NativeImage out = new NativeImage(targetW, targetH, false);
		for (int ty = 0; ty < targetH; ty++) {
			float y0 = ty * ky;
			float y1 = y0 + ky;
			for (int tx = 0; tx < targetW; tx++) {
				float r = span(horizontal, tx * 4, targetW * 4, y0, y1, sh) / ky;
				float g = span(horizontal, tx * 4 + 1, targetW * 4, y0, y1, sh) / ky;
				float b = span(horizontal, tx * 4 + 2, targetW * 4, y0, y1, sh) / ky;
				float a = Math.clamp(span(horizontal, tx * 4 + 3, targetW * 4, y0, y1, sh) / ky, 0f, 1f);
				int ia = Math.round(a * 255f);
				int ir = a > 0f ? Math.round(Math.clamp(r / a, 0f, 1f) * 255f) : 255;
				int ig = a > 0f ? Math.round(Math.clamp(g / a, 0f, 1f) * 255f) : 255;
				int ib = a > 0f ? Math.round(Math.clamp(b / a, 0f, 1f) * 255f) : 255;
				out.setPixel(tx, ty, ia << 24 | ir << 16 | ig << 8 | ib);
			}
		}
		return out;
	}

	/** Sum of {@code data[offset + i * stride]} over the continuous range [from, to). */
	private static float span(float[] data, int offset, int stride, float from, float to, int count) {
		float sum = 0f;
		int first = (int) Math.floor(from);
		int last = Math.min(count - 1, (int) Math.ceil(to) - 1);
		for (int i = Math.max(0, first); i <= last; i++) {
			float weight = Math.min(to, i + 1f) - Math.max(from, i);
			if (weight > 0f) {
				sum += data[offset + i * stride] * weight;
			}
		}
		return sum;
	}

	private static boolean upload(Identifier id, NativeImage image) {
		try (image) {
			Minecraft.getInstance().getTextureManager().register(id, new GpuImage(id.toString(), image));
			return true;
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.warn("Couldn't upload {}", id, e);
			return false;
		}
	}

	/** Frees the boot-only textures once the boot screen is gone (the drop stays for the cards). */
	public static void releaseBoot() {
		var textures = Minecraft.getInstance().getTextureManager();
		if (wallpaperReady) {
			textures.release(WALLPAPER);
			wallpaperReady = false;
		}
		wallpaperRequested = false;
		NativeImage pending = pendingWallpaper;
		pendingWallpaper = null;
		if (pending != null) {
			pending.close();
		}
		for (Art art : new Art[]{tile, wordmark}) {
			if (art.uploadedW > 0) {
				textures.release(art.id);
				art.uploadedW = -1;
				art.uploadedH = -1;
			}
			art.rgba = null; // the big masters only matter while booting
		}
	}

	/** Frees everything (client stop). Render thread. */
	public static void release() {
		releaseBoot();
		var textures = Minecraft.getInstance().getTextureManager();
		for (Art art : new Art[]{drop, dot}) {
			if (art.uploadedW > 0) {
				textures.release(art.id);
				art.uploadedW = -1;
				art.uploadedH = -1;
			}
		}
		if (glowReady) {
			textures.release(GLOW);
			glowReady = false;
		}
	}
}
