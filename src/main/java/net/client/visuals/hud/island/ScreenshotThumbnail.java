package net.client.visuals.hud.island;

import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.GpuImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Notices new screenshots and makes a small thumbnail of the newest for the island.
 *
 * <p>The screenshots folder is checked twice a second (a directory's modification time changes when
 * a file is added, so that's one cheap call); a new picture is read once its size has stopped
 * changing (vanilla writes it on a background thread), decoded and shrunk to {@value #WIDTH} px wide
 * on a worker, and uploaded on the render thread. The texture is freed as soon as the island stops
 * showing it.</p>
 */
final class ScreenshotThumbnail {
	private static final int WIDTH = 192;
	private static final Identifier ID = LiquidClient.id("dynamic/island_screenshot");
	private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Liquid screenshot thumbnail");
		thread.setDaemon(true);
		return thread;
	});

	private long folderStamp = -1L;
	private long newestSeen;
	private @Nullable File candidate;
	private long candidateSize = -1L;
	private @Nullable CompletableFuture<@Nullable NativeImage> pending;
	private @Nullable TextureSetup texture;
	private float aspect = 16f / 9f;
	private long shownAt;

	/**
	 * Client tick, every 10 ticks or so. Returns a screenshot that has just finished saving, or null;
	 * {@link #load} makes its thumbnail.
	 */
	@Nullable File poll(Minecraft minecraft) {
		File folder = new File(minecraft.gameDirectory, "screenshots");
		long stamp = folder.lastModified(); // 0 when the folder doesn't exist (yet)
		if (folderStamp < 0L) {
			folderStamp = stamp;
			newestSeen = System.currentTimeMillis();
			return null;
		}
		if (stamp != folderStamp) {
			folderStamp = stamp;
			File newest = newest(folder);
			if (newest != null) {
				candidate = newest;
				candidateSize = -1L;
			}
		}
		File file = candidate;
		if (file == null) {
			return null;
		}
		long size = file.length();
		if (size <= 0L || size != candidateSize) {
			candidateSize = size; // still being written: look again next time
			return null;
		}
		candidate = null;
		newestSeen = Math.max(newestSeen, file.lastModified());
		return file;
	}

	private @Nullable File newest(File folder) {
		File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));
		File best = null;
		long bestTime = newestSeen;
		if (files != null) {
			for (File f : files) {
				long t = f.lastModified();
				if (t > bestTime) {
					best = f;
					bestTime = t;
				}
			}
		}
		return best;
	}

	/** Starts decoding a thumbnail of {@code file} (replacing the current one). */
	void load(File file) {
		release();
		CompletableFuture<@Nullable NativeImage> stale = pending;
		if (stale != null) {
			stale.thenAccept(image -> {
				if (image != null) {
					image.close();
				}
			});
		}
		pending = CompletableFuture.supplyAsync(() -> decode(file), WORKER);
	}

	/** Render thread: uploads a finished thumbnail. */
	void update() {
		CompletableFuture<@Nullable NativeImage> job = pending;
		if (job == null || !job.isDone()) {
			return;
		}
		pending = null;
		NativeImage image = job.exceptionally(e -> null).join();
		if (image == null) {
			return;
		}
		try (image) {
			GpuImage gpu = new GpuImage("Liquid island screenshot", image);
			Minecraft.getInstance().getTextureManager().register(ID, gpu);
			texture = TextureSetup.singleTexture(gpu.view(), gpu.linearSampler());
			aspect = image.getWidth() / (float) Math.max(1, image.getHeight());
			shownAt = System.currentTimeMillis();
		}
	}

	/** The thumbnail, or null while it's loading (or failed). */
	@Nullable TextureSetup texture() {
		return texture;
	}

	boolean loading() {
		return pending != null;
	}

	float aspect() {
		return aspect;
	}

	/** 0 → 1 over 250 ms after the thumbnail appeared. */
	float reveal() {
		return texture == null ? 0f : Math.min(1f, (System.currentTimeMillis() - shownAt) / 250f);
	}

	/** Render thread: frees the texture. */
	void release() {
		if (texture != null) {
			Minecraft.getInstance().getTextureManager().release(ID);
			texture = null;
		}
	}

	private static @Nullable NativeImage decode(File file) {
		ByteBuffer encoded = null;
		ByteBuffer pixels = null;
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			encoded = MemoryUtil.memAlloc(bytes.length);
			encoded.put(bytes).flip();
			int w;
			int h;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer pw = stack.mallocInt(1);
				IntBuffer ph = stack.mallocInt(1);
				IntBuffer pc = stack.mallocInt(1);
				pixels = STBImage.stbi_load_from_memory(encoded, pw, ph, pc, 4);
				if (pixels == null) {
					throw new IOException(STBImage.stbi_failure_reason());
				}
				w = pw.get(0);
				h = ph.get(0);
			}
			int tw = Math.min(WIDTH, w);
			int th = Math.max(1, Math.round(tw * h / (float) Math.max(1, w)));
			NativeImage image = new NativeImage(tw, th, false);
			ByteBuffer out = MemoryUtil.memByteBuffer(image.getPointer(), tw * th * 4);
			if (STBImageResize.stbir_resize_uint8_srgb(pixels, w, h, w * 4, out, tw, th, tw * 4, STBImageResize.STBIR_RGBA) == null) {
				image.close();
				throw new IOException("resize failed");
			}
			for (int i = 3; i < tw * th * 4; i += 4) {
				out.put(i, (byte) 0xFF);
			}
			return image;
		} catch (IOException | RuntimeException e) {
			LiquidClient.LOGGER.info("Screenshot thumbnail for {} failed: {}", file.getName(), e.toString());
			return null;
		} finally {
			if (pixels != null) {
				STBImage.stbi_image_free(pixels);
			}
			if (encoded != null) {
				MemoryUtil.memFree(encoded);
			}
		}
	}
}
