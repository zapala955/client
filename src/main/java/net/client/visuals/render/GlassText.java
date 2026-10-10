package net.client.visuals.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

/**
 * Large "Liquid Glass" text (the lock-screen clock).
 *
 * <h2>Pixel-exact</h2>
 * Minecraft's font atlas works with bitmaps at a fixed oversample and pages of limited size, so
 * very large glyphs would be scaled (soft) or not fit. Instead the string is rasterised with
 * stb_truetype at <b>exactly</b> the device pixel size it will occupy (with kerning and sub-pixel
 * glyph placement) and drawn 1:1. It's re-rasterised only when the text, size or GUI scale changes
 * – once a minute for a clock.
 *
 * <h2>Channels</h2>
 * The mask texture bakes {@code r} coverage, {@code g} a soft bevel height (blurred coverage) and
 * {@code b} a blurred, offset drop shadow, which {@code glass_text.fsh} turns into refraction,
 * a specular rim and the shadow. Without a photo backdrop it falls back to a white glass tint.
 */
public final class GlassText {
	private static final Identifier MASK_ID_PREFIX = LiquidClient.id("dynamic/glass_text_");
	private static int counter;

	private final Identifier maskId = LiquidClient.id(MASK_ID_PREFIX.getPath() + counter++);
	private final Identifier fontResource;
	private final GlassTextState state = new GlassTextState();
	private @Nullable ByteBuffer fontData;
	private @Nullable STBTTFontinfo font;
	private boolean fontFailed;
	private float capUnits;

	private @Nullable GpuImage mask;
	private String text = "";
	private float capHeight;
	private int scale;
	/** Device-pixel padding around the glyphs (room for bevel + shadow). */
	private int padPx;
	private int capTopPx;

	public GlassText(String fontPath) {
		this.fontResource = LiquidClient.id(fontPath);
	}

	/** Width of the current text in GUI units (excluding padding), valid after {@link #update}. */
	public float textWidth() {
		return mask == null ? 0f : (mask.width() - padPx * 2f) / scale;
	}

	/**
	 * Re-rasterises if the text, cap height (GUI units) or GUI scale changed. Render thread.
	 */
	public void update(String newText, float newCapHeight) {
		int guiScale = HighDpiEngine.scale();
		if (newText.equals(text) && Math.abs(newCapHeight - capHeight) < 0.01f && guiScale == scale && mask != null) {
			return;
		}
		text = newText;
		capHeight = newCapHeight;
		scale = guiScale;
		if (!ensureFont()) {
			return;
		}
		NativeImage image = rasterize(newText, newCapHeight * guiScale);
		if (image == null) {
			return;
		}
		try (image) {
			GpuImage created = new GpuImage("Liquid glass text", image);
			Minecraft.getInstance().getTextureManager().register(maskId, created); // closes the previous one
			mask = created;
		}
	}

	/**
	 * Draws the text with its cap top at {@code capTopY}, horizontally centred on {@code centerX}.
	 *
	 * @param backdrop screen-aligned frosted wallpaper, or null for the plain glass fallback
	 */
	public void draw(GuiGraphicsExtractor g, float centerX, float capTopY, int tint, @Nullable TextureSetup backdrop,
			float screenWidth, float screenHeight) {
		GpuImage current = mask;
		if (current == null) {
			return;
		}
		float w = current.width() / (float) scale;
		float h = current.height() / (float) scale;
		float x = HighDpiEngine.snap(centerX - w * 0.5f);
		float y = HighDpiEngine.snap(capTopY - capTopPx / (float) scale);
		int color = RenderUtils.applyAlpha(tint);
		if ((color >>> 24) == 0) {
			return;
		}
		if (ShaderSupport.glassText()) {
			TextureSetup textures = textures(backdrop, current);
			state.configure(LiquidPipelines.GLASS_TEXT, textures, g.pose(), g.scissorStack.peek(), x, y, w, h, color,
					screenWidth, screenHeight);
			if (state.updateBounds()) {
				g.guiRenderState.addGuiElement(state);
			}
		}
	}

	private @Nullable TextureSetup cachedSetup;
	private @Nullable TextureSetup cachedBackdrop;
	private @Nullable GpuImage cachedMask;

	/** Backdrop + mask binding, rebuilt only when either texture changes. */
	private TextureSetup textures(@Nullable TextureSetup backdrop, GpuImage currentMask) {
		if (cachedSetup != null && cachedBackdrop == backdrop && cachedMask == currentMask) {
			return cachedSetup;
		}
		TextureSetup setup;
		if (backdrop != null) {
			setup = TextureSetup.doubleTexture(backdrop.texure0(), backdrop.sampler0(), currentMask.view(), currentMask.linearSampler());
		} else {
			GpuImage behind = fallbackBackdrop();
			setup = TextureSetup.doubleTexture(behind.view(), behind.linearSampler(), currentMask.view(), currentMask.linearSampler());
		}
		cachedSetup = setup;
		cachedBackdrop = backdrop;
		cachedMask = currentMask;
		return setup;
	}

	// ---- Rasterisation ------------------------------------------------------------------------

	private boolean ensureFont() {
		if (font != null) {
			return true;
		}
		if (fontFailed) {
			return false;
		}
		try {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(fontResource);
			if (resource.isEmpty()) {
				throw new IOException("missing " + fontResource);
			}
			byte[] bytes;
			try (InputStream in = resource.get().open()) {
				bytes = in.readAllBytes();
			}
			ByteBuffer data = MemoryUtil.memAlloc(bytes.length);
			data.put(bytes).flip();
			STBTTFontinfo info = STBTTFontinfo.malloc();
			if (!STBTruetype.stbtt_InitFont(info, data)) {
				info.free();
				MemoryUtil.memFree(data);
				throw new IOException("stb_truetype couldn't parse " + fontResource);
			}
			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer x0 = stack.mallocInt(1);
				IntBuffer y0 = stack.mallocInt(1);
				IntBuffer x1 = stack.mallocInt(1);
				IntBuffer y1 = stack.mallocInt(1);
				STBTruetype.stbtt_GetCodepointBox(info, 'H', x0, y0, x1, y1);
				capUnits = Math.max(1, y1.get(0));
			}
			fontData = data; // must outlive the font info
			font = info;
			return true;
		} catch (IOException | RuntimeException e) {
			fontFailed = true;
			LiquidClient.LOGGER.warn("Glass text font unavailable", e);
			return false;
		}
	}

	private @Nullable NativeImage rasterize(String s, float capPx) {
		STBTTFontinfo info = font;
		if (info == null || s.isEmpty() || capPx < 2f) {
			return null;
		}
		float fontScale = capPx / capUnits;
		int pad = Math.round(capPx * 0.24f);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer advance = stack.mallocInt(1);
			IntBuffer lsb = stack.mallocInt(1);
			IntBuffer bx0 = stack.mallocInt(1);
			IntBuffer by0 = stack.mallocInt(1);
			IntBuffer bx1 = stack.mallocInt(1);
			IntBuffer by1 = stack.mallocInt(1);

			// Measure.
			int[] codepoints = s.codePoints().toArray();
			float[] penX = new float[codepoints.length];
			float pen = 0f;
			float minX = Float.MAX_VALUE;
			float maxX = -Float.MAX_VALUE;
			for (int i = 0; i < codepoints.length; i++) {
				int cp = codepoints[i];
				if (i > 0) {
					pen += STBTruetype.stbtt_GetCodepointKernAdvance(info, codepoints[i - 1], cp) * fontScale;
				}
				penX[i] = pen;
				float frac = pen - (float) Math.floor(pen);
				STBTruetype.stbtt_GetCodepointBitmapBoxSubpixel(info, cp, fontScale, fontScale, frac, 0f, bx0, by0, bx1, by1);
				minX = Math.min(minX, (float) Math.floor(pen) + bx0.get(0));
				maxX = Math.max(maxX, (float) Math.floor(pen) + bx1.get(0));
				STBTruetype.stbtt_GetCodepointHMetrics(info, cp, advance, lsb);
				pen += advance.get(0) * fontScale;
			}
			int textW = Math.max(1, (int) Math.ceil(maxX - minX));
			int scaleMul = Math.max(1, scale);
			int width = roundUp(textW + pad * 2, scaleMul);
			int height = roundUp(Math.round(capPx) + pad * 2, scaleMul);
			int originX = (width - textW) / 2 - (int) Math.floor(minX);
			int baseline = pad + Math.round(capPx);
			padPx = (width - textW) / 2;
			capTopPx = baseline - Math.round(capPx);

			// Render glyphs into a coverage canvas (max-blend, glyphs may touch).
			float[] coverage = new float[width * height];
			for (int i = 0; i < codepoints.length; i++) {
				int cp = codepoints[i];
				float frac = penX[i] - (float) Math.floor(penX[i]);
				STBTruetype.stbtt_GetCodepointBitmapBoxSubpixel(info, cp, fontScale, fontScale, frac, 0f, bx0, by0, bx1, by1);
				int gw = bx1.get(0) - bx0.get(0);
				int gh = by1.get(0) - by0.get(0);
				if (gw <= 0 || gh <= 0) {
					continue;
				}
				ByteBuffer glyph = MemoryUtil.memCalloc(gw * gh);
				try {
					STBTruetype.stbtt_MakeCodepointBitmapSubpixel(info, glyph, gw, gh, gw, fontScale, fontScale, frac, 0f, cp);
					int ox = originX + (int) Math.floor(penX[i]) + bx0.get(0);
					int oy = baseline + by0.get(0);
					for (int y = 0; y < gh; y++) {
						int cy = oy + y;
						if (cy < 0 || cy >= height) {
							continue;
						}
						for (int x = 0; x < gw; x++) {
							int cx = ox + x;
							if (cx < 0 || cx >= width) {
								continue;
							}
							float v = (glyph.get(y * gw + x) & 0xFF) / 255f;
							int idx = cy * width + cx;
							if (v > coverage[idx]) {
								coverage[idx] = v;
							}
						}
					}
				} finally {
					MemoryUtil.memFree(glyph);
				}
			}

			// Bevel height and drop shadow.
			float[] heightMap = blur(coverage, width, height, Math.max(1, Math.round(capPx * 0.05f)), 2);
			float[] shifted = new float[width * height];
			int drop = Math.max(1, Math.round(capPx * 0.035f));
			for (int y = drop; y < height; y++) {
				System.arraycopy(coverage, (y - drop) * width, shifted, y * width, width);
			}
			float[] shadow = blur(shifted, width, height, Math.max(1, Math.round(capPx * 0.09f)), 3);

			NativeImage image = new NativeImage(width, height, false);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int i = y * width + x;
					int r = Math.round(Math.clamp(coverage[i], 0f, 1f) * 255f);
					int gch = Math.round(Math.clamp(heightMap[i], 0f, 1f) * 255f);
					int b = Math.round(Math.clamp(shadow[i] * 1.4f, 0f, 1f) * 255f);
					image.setPixel(x, y, 0xFF000000 | r << 16 | gch << 8 | b);
				}
			}
			return image;
		}
	}

	private static int roundUp(int value, int multiple) {
		return ((value + multiple - 1) / multiple) * multiple;
	}

	/** Separable box blur, {@code passes} times (3 passes ≈ gaussian). */
	public static float[] blur(float[] src, int w, int h, int radius, int passes) {
		float[] a = src.clone();
		float[] b = new float[a.length];
		float norm = 1f / (radius * 2 + 1);
		for (int p = 0; p < passes; p++) {
			for (int y = 0; y < h; y++) {
				float sum = 0f;
				for (int k = -radius; k <= radius; k++) {
					sum += a[y * w + Math.clamp(k, 0, w - 1)];
				}
				for (int x = 0; x < w; x++) {
					b[y * w + x] = sum * norm;
					sum += a[y * w + Math.min(w - 1, x + radius + 1)] - a[y * w + Math.max(0, x - radius)];
				}
			}
			for (int x = 0; x < w; x++) {
				float sum = 0f;
				for (int k = -radius; k <= radius; k++) {
					sum += b[Math.clamp(k, 0, h - 1) * w + x];
				}
				for (int y = 0; y < h; y++) {
					a[y * w + x] = sum * norm;
					sum += b[Math.min(h - 1, y + radius + 1) * w + x] - b[Math.max(0, y - radius) * w + x];
				}
			}
		}
		return a;
	}

	// ---- Fallback backdrop (no photo wallpaper) -------------------------------------------------

	private static @Nullable GpuImage fallback;

	private static GpuImage fallbackBackdrop() {
		if (fallback == null) {
			try (NativeImage image = new NativeImage(2, 2, false)) {
				image.fillRect(0, 0, 2, 2, 0xFFB8C4D6);
				fallback = new GpuImage("Liquid glass fallback backdrop", image);
				Minecraft.getInstance().getTextureManager().register(LiquidClient.id("dynamic/glass_fallback"), fallback);
			}
		}
		return fallback;
	}

	/** Frees the mask texture. */
	public void release() {
		if (mask != null) {
			Minecraft.getInstance().getTextureManager().release(maskId);
			mask = null;
		}
		text = "";
	}

	// ---- Render state -------------------------------------------------------------------------

	/** One textured glass quad; reused every frame. */
	private static final class GlassTextState implements GuiElementRenderState {
		private final Matrix3x2f pose = new Matrix3x2f();
		private RenderPipeline pipeline = LiquidPipelines.GLASS_TEXT;
		private TextureSetup textures = TextureSetup.noTexture();
		private @Nullable ScreenRectangle scissor;
		private float x0;
		private float y0;
		private float x1;
		private float y1;
		private int color;
		private float screenW = 1f;
		private float screenH = 1f;
		private @Nullable ScreenRectangle bounds;
		private int bx0 = Integer.MIN_VALUE;
		private int by0;
		private int bx1;
		private int by1;

		void configure(RenderPipeline pipeline, TextureSetup textures, Matrix3x2fc pose, @Nullable ScreenRectangle scissor,
				float x, float y, float w, float h, int color, float screenW, float screenH) {
			this.pipeline = pipeline;
			this.textures = textures;
			this.pose.set(pose);
			this.scissor = scissor;
			this.x0 = x;
			this.y0 = y;
			this.x1 = x + w;
			this.y1 = y + h;
			this.color = color;
			this.screenW = screenW;
			this.screenH = screenH;
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			vertex(consumer, x0, y0, 0f, 0f);
			vertex(consumer, x0, y1, 0f, 1f);
			vertex(consumer, x1, y1, 1f, 1f);
			vertex(consumer, x1, y0, 1f, 0f);
		}

		private void vertex(VertexConsumer consumer, float x, float y, float u, float v) {
			// Wallpaper uv from the vertex's actual screen position, so the glass stays aligned with
			// the backdrop even while the text animates.
			float sx = pose.m00() * x + pose.m10() * y + pose.m20();
			float sy = pose.m01() * x + pose.m11() * y + pose.m21();
			consumer.addVertexWith2DPose(pose, x, y)
					.setColor(color)
					.setUv(u, v)
					.setUv3(sx / screenW, sy / screenH);
		}

		boolean updateBounds() {
			float m00 = pose.m00();
			float m01 = pose.m01();
			float m10 = pose.m10();
			float m11 = pose.m11();
			float m20 = pose.m20();
			float m21 = pose.m21();
			int ix0 = (int) Math.floor(m20 + Math.min(m00 * x0, m00 * x1) + Math.min(m10 * y0, m10 * y1));
			int ix1 = (int) Math.ceil(m20 + Math.max(m00 * x0, m00 * x1) + Math.max(m10 * y0, m10 * y1));
			int iy0 = (int) Math.floor(m21 + Math.min(m01 * x0, m01 * x1) + Math.min(m11 * y0, m11 * y1));
			int iy1 = (int) Math.ceil(m21 + Math.max(m01 * x0, m01 * x1) + Math.max(m11 * y0, m11 * y1));
			if (scissor != null) {
				ix0 = Math.max(ix0, scissor.left());
				iy0 = Math.max(iy0, scissor.top());
				ix1 = Math.min(ix1, scissor.right());
				iy1 = Math.min(iy1, scissor.bottom());
			}
			if (ix1 <= ix0 || iy1 <= iy0) {
				return false;
			}
			if (bounds == null || ix0 != bx0 || iy0 != by0 || ix1 != bx1 || iy1 != by1) {
				bx0 = ix0;
				by0 = iy0;
				bx1 = ix1;
				by1 = iy1;
				bounds = new ScreenRectangle(ix0, iy0, ix1 - ix0, iy1 - iy0);
			}
			return true;
		}

		@Override
		public RenderPipeline pipeline() {
			return pipeline;
		}

		@Override
		public TextureSetup textureSetup() {
			return textures;
		}

		@Override
		public @Nullable ScreenRectangle scissorArea() {
			return scissor;
		}

		@Override
		public @Nullable ScreenRectangle bounds() {
			return bounds;
		}
	}
}
