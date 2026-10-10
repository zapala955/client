package net.client.visuals.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.GpuImage;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Photo Mode's shutter and histogram (render thread).
 *
 * <h2>A photo</h2>
 * <ol>
 *   <li><b>Settling</b> – the camera eases to rest first (at most 0.6 s), so nothing is smeared.</li>
 *   <li><b>Exposing</b> – for the quality's frame count, each frame is rendered with a sub-pixel
 *       offset (a Halton (2, 3) pattern, {@link #jitterX()}, applied to the projection by
 *       {@code PhotoMode}) and a new noise seed, and added in linear light to a float sum after
 *       grading, before the GUI ({@code photo.fsh}). The average is anti-aliased and the noise of
 *       Depth of Field and AO is gone. In a frozen world that's all; on a server, whatever moves
 *       leaves a short trail, like a real shutter.</li>
 *   <li><b>Developing</b> – the average is dithered into an 8-bit texture, read back
 *       asynchronously, cropped to the aspect ratio and saved as a PNG in the screenshots folder on
 *       the IO pool, with a small thumbnail for the viewfinder.</li>
 * </ol>
 * Without the shaders a photo is a single frame, read back the same way. The frame is the window's,
 * so photos are at your screen's resolution.
 *
 * <h2>Histogram</h2>
 * Every few frames while idle the finished frame is shrunk to 64 × 36 and read back (9 KB); the
 * viewfinder draws its RGB and luminance distribution.
 */
public final class PhotoCapture {
	private static final int IDLE = 0;
	private static final int SETTLING = 1;
	private static final int EXPOSING = 2;
	private static final int DEVELOPING = 3;

	public static final int BINS = 64;
	private static final int HISTO_W = 64;
	private static final int HISTO_H = 36;
	private static final int HISTO_EVERY = 5;
	private static final float SETTLE_TIMEOUT = 0.6f;
	private static final Identifier THUMBNAIL_ID = LiquidClient.id("dynamic/photo_thumbnail");
	private static final Supplier<String> LABEL = () -> "Liquid photo";

	private static final FloatBlock PARAMS = new FloatBlock(1);
	private static final GpuTexture[] SUM = new GpuTexture[2];
	private static final GpuTextureView[] SUM_VIEWS = new GpuTextureView[2];
	/** R, G, B and luminance, BINS each; written by the readback, read by the viewfinder. */
	private static final int[][] HISTOGRAM = new int[4][BINS];
	private static final int[][] HISTOGRAM_WORK = new int[4][BINS];

	private static int state = IDLE;
	private static int samples;
	private static int index;
	private static float aspect;
	private static long startNanos;
	private static int width;
	private static int height;
	private static @Nullable GpuTexture developed;
	private static @Nullable GpuTextureView developedView;
	private static @Nullable DynamicGpuDataStorageMapped<FloatBlock> ring;

	private static @Nullable GpuTexture histoTexture;
	private static @Nullable GpuTextureView histoView;
	private static @Nullable GpuBuffer histoBuffer;
	private static boolean histoPending;
	private static int histoMax;
	private static long frames;
	private static long ringFrame = -1L;

	private static @Nullable TextureSetup thumbnail;
	private static float thumbnailAspect = 16f / 9f;
	private static long savedNanos = -1L;
	private static long shutterNanos = -1L;
	private static @Nullable File lastFile;
	private static boolean failed;

	private PhotoCapture() {
	}

	// ---- The shutter ------------------------------------------------------------------------------

	/** Press the shutter: {@code frames} averaged, cropped to {@code aspectRatio} (0 = the whole frame). */
	public static void start(int frames, float aspectRatio) {
		if (state != IDLE) {
			return;
		}
		samples = Math.max(1, frames);
		aspect = aspectRatio;
		index = 0;
		failed = false;
		startNanos = System.nanoTime();
		state = SETTLING;
	}

	/** Drops a photo that's still settling or exposing (no GPU calls: the textures go with {@link #releaseTextures()}). */
	public static void cancel() {
		if (state == SETTLING || state == EXPOSING) {
			state = IDLE;
		}
	}

	/** A photo is being taken (another press waits). */
	public static boolean busy() {
		return state != IDLE;
	}

	/** The camera holds still: settling and exposing. */
	public static boolean holdsCamera() {
		return state == SETTLING || state == EXPOSING;
	}

	/** 0 → 1 while exposing (the shutter button's ring), 1 while developing. */
	public static float progress() {
		return switch (state) {
			case EXPOSING -> index / (float) samples;
			case DEVELOPING -> 1f;
			default -> 0f;
		};
	}

	/** This frame's sub-pixel offset (pixels) while exposing more than one frame. */
	public static float jitterX() {
		return state == EXPOSING && samples > 1 ? halton(index + 1, 2) - 0.5f : 0f;
	}

	public static float jitterY() {
		return state == EXPOSING && samples > 1 ? halton(index + 1, 3) - 0.5f : 0f;
	}

	/** A different noise pattern every frame of a photo (0 otherwise), for Depth of Field and AO. */
	public static float noiseSeed() {
		return state == EXPOSING ? index + 1 : 0f;
	}

	private static float halton(int i, int base) {
		float f = 1f;
		float r = 0f;
		while (i > 0) {
			f /= base;
			r += f * (i % base);
			i /= base;
		}
		return r;
	}

	/** The last photo's thumbnail (null until one is saved). */
	public static @Nullable TextureSetup thumbnail() {
		return thumbnail;
	}

	public static float thumbnailAspect() {
		return thumbnailAspect;
	}

	/** Seconds since the shutter last fired (the viewfinder's blink), or a large number. */
	public static float sinceShutter() {
		return shutterNanos < 0L ? 100f : (System.nanoTime() - shutterNanos) * 1.0e-9f;
	}

	/** Waiting for the camera to come to rest before the shutter fires. */
	public static boolean settling() {
		return state == SETTLING;
	}

	/** Seconds since the last photo was saved (the viewfinder's confirmation), or a large number. */
	public static float sinceSaved() {
		return savedNanos < 0L ? 100f : (System.nanoTime() - savedNanos) * 1.0e-9f;
	}

	public static @Nullable File lastFile() {
		return lastFile;
	}

	/** The last photo couldn't be saved. */
	public static boolean failed() {
		return failed;
	}

	/** After grading, before the GUI (render thread): settle, expose, develop; the histogram. */
	public static void frame() {
		frames++;
		if (!PhotoMode.active()) {
			if (state != DEVELOPING && state != IDLE) {
				state = IDLE;
			}
			return;
		}
		try {
			switch (state) {
				case SETTLING -> {
					if (PhotoMode.settled() || (System.nanoTime() - startNanos) * 1.0e-9f > SETTLE_TIMEOUT) {
						// The shutter fires now: its sound and the viewfinder's blink with it.
						state = EXPOSING;
						index = 0;
						shutterNanos = System.nanoTime();
						if (PhotoMode.module().sounds.get()) {
							UiSounds.shutter();
						}
					}
				}
				case EXPOSING -> expose();
				default -> {
				}
			}
			if (state == IDLE && PhotoMode.module().histogram.get()) {
				histogram();
			}
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.error("Photo Mode capture failed", e);
			state = IDLE;
			failed = true;
			releaseSum();
		}
	}

	private static void expose() {
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		GpuTextureView colorView = target.getColorTextureView();
		if (color == null || colorView == null) {
			state = IDLE;
			return;
		}
		int w = color.getWidth(0);
		int h = color.getHeight(0);
		CompiledRenderPipeline first = compiled(LiquidPipelines.PHOTO_ACCUMULATE_FIRST);
		CompiledRenderPipeline add = compiled(LiquidPipelines.PHOTO_ACCUMULATE);
		CompiledRenderPipeline resolve = compiled(LiquidPipelines.PHOTO_RESOLVE);
		if (!ShaderSupport.photo() || first == null || add == null || resolve == null) {
			// No shaders: this frame, as it is.
			readBack(color, w, h);
			return;
		}
		if (index == 0) {
			ensureSum(w, h);
		} else if (w != width || h != height) {
			state = IDLE; // the window was resized mid-exposure
			releaseSum();
			return;
		}
		GpuBufferSlice slice = params(index, 1f / HISTO_W, 1f / HISTO_H);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		GpuTextureView into = SUM_VIEWS[(index + 1) % 2];
		try (RenderPass pass = encoder.createRenderPass(LABEL, into, Optional.empty())) {
			pass.setPipeline(index == 0 ? first : add);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidPhoto", slice);
			pass.setUniform("InSampler", colorView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			if (index > 0) {
				pass.setUniform("AccSampler", SUM_VIEWS[index % 2], RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			}
			pass.draw(3, 1, 0, 0);
		}
		index++;
		if (index < samples) {
			return;
		}
		// Develop: the average into 8 bits, then read it back.
		GpuDevice device = RenderSystem.getDevice();
		if (developed == null || developed.isClosed() || developed.getWidth(0) != w || developed.getHeight(0) != h) {
			releaseDeveloped();
			developed = device.createTexture(() -> "Liquid photo", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING
					| GpuTexture.USAGE_COPY_SRC, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
			developedView = device.createTextureView(developed);
		}
		try (RenderPass pass = encoder.createRenderPass(LABEL, developedView, Optional.empty())) {
			pass.setPipeline(resolve);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidPhoto", slice);
			pass.setUniform("AccSampler", SUM_VIEWS[index % 2], RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			pass.draw(3, 1, 0, 0);
		}
		readBack(developed, w, h);
	}

	/** Copies the developed frame into a mappable buffer; the rest happens when the GPU is done. */
	private static void readBack(GpuTexture source, int w, int h) {
		state = DEVELOPING;
		float crop = aspect;
		GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Liquid photo readback", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
				(long) w * h * 4L);
		RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(source, buffer, 0L, () -> develop(buffer, w, h, crop), 0);
	}

	/** Render thread, a frame or two later: the pixels, cropped and flipped upright; saving goes to the IO pool. */
	private static void develop(GpuBuffer buffer, int w, int h, float crop) {
		NativeImage image;
		try {
			int cw = w;
			int ch = h;
			if (crop > 0f) {
				if (crop > w / (float) h) {
					ch = Math.max(1, Math.round(w / crop));
				} else {
					cw = Math.max(1, Math.round(h * crop));
				}
			}
			int ox = (w - cw) / 2;
			int oy = (h - ch) / 2;
			image = new NativeImage(cw, ch, false);
			try (GpuBufferSlice.MappedView view = buffer.map(true, false)) {
				ByteBuffer data = view.data();
				for (int y = 0; y < ch; y++) {
					// Texture rows run bottom-up.
					int row = h - 1 - (oy + y);
					for (int x = 0; x < cw; x++) {
						image.setPixelABGR(x, y, data.getInt(((ox + x) + row * w) * 4) | 0xFF000000);
					}
				}
			}
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.error("Photo Mode couldn't read the photo back", e);
			failed = true;
			state = IDLE;
			return;
		} finally {
			buffer.close();
			releaseSum();
		}
		NativeImage thumb = shrink(image, 256);
		Minecraft minecraft = Minecraft.getInstance();
		File folder = new File(minecraft.gameDirectory, "screenshots");
		Util.ioPool().execute(() -> {
			File file = null;
			try (image) {
				folder.mkdirs();
				file = uniqueFile(folder);
				image.writeToFile(file);
				LiquidClient.LOGGER.info("Photo saved: {}", file.getName());
			} catch (Exception e) {
				LiquidClient.LOGGER.warn("Couldn't save photo", e);
				file = null;
			}
			File saved = file;
			minecraft.execute(() -> finish(saved, thumb));
		});
	}

	/** Render thread: the thumbnail on screen, ready for the next photo. */
	private static void finish(@Nullable File file, NativeImage thumb) {
		try (thumb) {
			if (file == null) {
				failed = true;
			} else {
				lastFile = file;
				GpuImage gpu = new GpuImage("Liquid photo thumbnail", thumb);
				Minecraft.getInstance().getTextureManager().register(THUMBNAIL_ID, gpu);
				thumbnail = TextureSetup.singleTexture(gpu.view(), gpu.linearSampler());
				thumbnailAspect = thumb.getWidth() / (float) Math.max(1, thumb.getHeight());
				savedNanos = System.nanoTime();
			}
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.warn("Couldn't show the photo's thumbnail", e);
		}
		state = IDLE;
	}

	private static File uniqueFile(File folder) {
		String name = "Photo " + Util.getFilenameFormattedDateTime();
		File file = new File(folder, name + ".png");
		for (int i = 2; file.exists(); i++) {
			file = new File(folder, name + "_" + i + ".png");
		}
		return file;
	}

	/** A box-filtered copy at most {@code maxWidth} wide. */
	private static NativeImage shrink(NativeImage image, int maxWidth) {
		int w = image.getWidth();
		int h = image.getHeight();
		int step = Math.max(1, (int) Math.ceil(w / (double) maxWidth));
		int tw = Math.max(1, w / step);
		int th = Math.max(1, h / step);
		NativeImage out = new NativeImage(tw, th, false);
		for (int ty = 0; ty < th; ty++) {
			for (int tx = 0; tx < tw; tx++) {
				int r = 0;
				int g = 0;
				int b = 0;
				for (int sy = 0; sy < step; sy++) {
					for (int sx = 0; sx < step; sx++) {
						int argb = image.getPixel(tx * step + sx, ty * step + sy);
						r += (argb >> 16) & 0xFF;
						g += (argb >> 8) & 0xFF;
						b += argb & 0xFF;
					}
				}
				int n = step * step;
				out.setPixelABGR(tx, ty, 0xFF000000 | (b / n) << 16 | (g / n) << 8 | r / n);
			}
		}
		return out;
	}

	// ---- Histogram --------------------------------------------------------------------------------

	private static void histogram() {
		if (histoPending || frames % HISTO_EVERY != 0) {
			return;
		}
		CompiledRenderPipeline pipeline = compiled(LiquidPipelines.PHOTO_HISTOGRAM);
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView colorView = target.getColorTextureView();
		if (!ShaderSupport.photo() || pipeline == null || colorView == null) {
			return;
		}
		GpuDevice device = RenderSystem.getDevice();
		if (histoTexture == null || histoTexture.isClosed()) {
			histoTexture = device.createTexture(() -> "Liquid photo histogram", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING
					| GpuTexture.USAGE_COPY_SRC, GpuFormat.RGBA8_UNORM, HISTO_W, HISTO_H, 1, 1);
			histoView = device.createTextureView(histoTexture);
			histoBuffer = device.createBuffer(() -> "Liquid photo histogram readback", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
					(long) HISTO_W * HISTO_H * 4L);
		}
		GpuBufferSlice slice = params(0, 1f / HISTO_W, 1f / HISTO_H);
		CommandEncoder encoder = device.createCommandEncoder();
		try (RenderPass pass = encoder.createRenderPass(LABEL, histoView, Optional.empty())) {
			pass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("LiquidPhoto", slice);
			pass.setUniform("InSampler", colorView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			pass.draw(3, 1, 0, 0);
		}
		GpuBuffer buffer = histoBuffer;
		histoPending = true;
		encoder.copyTextureToBuffer(histoTexture, buffer, 0L, () -> readHistogram(buffer), 0);
	}

	private static void readHistogram(GpuBuffer buffer) {
		histoPending = false;
		if (buffer.isClosed()) {
			return;
		}
		for (int[] channel : HISTOGRAM_WORK) {
			java.util.Arrays.fill(channel, 0);
		}
		try (GpuBufferSlice.MappedView view = buffer.map(true, false)) {
			ByteBuffer data = view.data();
			for (int i = 0; i < HISTO_W * HISTO_H; i++) {
				int abgr = data.getInt(i * 4);
				int r = abgr & 0xFF;
				int g = (abgr >> 8) & 0xFF;
				int b = (abgr >> 16) & 0xFF;
				int l = (r * 54 + g * 183 + b * 19) >> 8;
				HISTOGRAM_WORK[0][r * BINS / 256]++;
				HISTOGRAM_WORK[1][g * BINS / 256]++;
				HISTOGRAM_WORK[2][b * BINS / 256]++;
				HISTOGRAM_WORK[3][l * BINS / 256]++;
			}
		} catch (RuntimeException e) {
			return;
		}
		int max = 1;
		for (int c = 0; c < 4; c++) {
			System.arraycopy(HISTOGRAM_WORK[c], 0, HISTOGRAM[c], 0, BINS);
			// The end bins hold whole clipped areas; they're shown as warnings, not to scale.
			for (int i = 1; i < BINS - 1; i++) {
				max = Math.max(max, HISTOGRAM[c][i]);
			}
		}
		histoMax = max;
	}

	/** Channel 0 R, 1 G, 2 B, 3 luminance; bin 0 dark … BINS - 1 bright. */
	public static int histogram(int channel, int bin) {
		return HISTOGRAM[channel][bin];
	}

	/** The fullest bin (end bins excluded), for scaling. */
	public static int histogramMax() {
		return histoMax;
	}

	/** Share of the frame clipped to black (channel 3, bin 0) or white (last bin). */
	public static float clipped(boolean highlights) {
		return HISTOGRAM[3][highlights ? BINS - 1 : 0] / (float) (HISTO_W * HISTO_H);
	}

	// ---- Resources --------------------------------------------------------------------------------

	private static GpuBufferSlice params(int seed, float tx, float ty) {
		PARAMS.vec4(0, seed, 0f, tx, ty);
		DynamicGpuDataStorageMapped<FloatBlock> r = ring;
		if (r == null) {
			r = ring = new DynamicGpuDataStorageMapped<>("Liquid photo UBO", PARAMS.size(), GpuBuffer.USAGE_UNIFORM, 4);
		}
		// The ring turns once per frame, before this frame's first block.
		if (ringFrame != frames) {
			ringFrame = frames;
			r.endFrame();
		}
		return r.writeData(PARAMS);
	}

	private static void ensureSum(int w, int h) {
		if (SUM[0] != null && !SUM[0].isClosed() && width == w && height == h) {
			return;
		}
		releaseSum();
		GpuDevice device = RenderSystem.getDevice();
		for (int i = 0; i < 2; i++) {
			SUM[i] = device.createTexture(() -> "Liquid photo exposure", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
					GpuFormat.RGBA32_FLOAT, w, h, 1, 1);
			SUM_VIEWS[i] = device.createTextureView(SUM[i]);
		}
		width = w;
		height = h;
	}

	private static void releaseSum() {
		for (int i = 0; i < 2; i++) {
			if (SUM_VIEWS[i] != null) {
				SUM_VIEWS[i].close();
				SUM_VIEWS[i] = null;
			}
			if (SUM[i] != null) {
				SUM[i].close();
				SUM[i] = null;
			}
		}
	}

	private static void releaseDeveloped() {
		if (developedView != null) {
			developedView.close();
			developedView = null;
		}
		if (developed != null) {
			developed.close();
			developed = null;
		}
	}

	/** Everything, the last photo's thumbnail included (render thread; world left, client stopping). */
	public static void release() {
		releaseTextures();
		if (thumbnail != null) {
			Minecraft.getInstance().getTextureManager().release(THUMBNAIL_ID);
			thumbnail = null;
		}
		savedNanos = -1L;
	}

	/** The working textures, kept between photos while Photo Mode is open (render thread; Photo Mode closed). */
	public static void releaseTextures() {
		if (state == DEVELOPING) {
			return; // the readback still needs its texture; released after
		}
		state = IDLE;
		releaseSum();
		releaseDeveloped();
		if (histoView != null) {
			histoView.close();
			histoView = null;
		}
		if (histoTexture != null) {
			histoTexture.close();
			histoTexture = null;
		}
		if (histoBuffer != null && !histoPending) {
			histoBuffer.close();
			histoBuffer = null;
		}
		if (ring != null) {
			ring.close();
			ring = null;
		}
	}

	private static @Nullable CompiledRenderPipeline compiled(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
		return compiled == null || compiled.isClosed() ? null : compiled;
	}
}
