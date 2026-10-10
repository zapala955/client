package net.client.visuals.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.LiquidClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
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
import java.util.ArrayList;
import java.util.Optional;

/**
 * Line icons from a bundled subset of <a href="https://lucide.dev">Lucide</a> (ISC licence): one
 * consistent 2/24 stroke for every glyph, drawn on a 24-unit grid.
 *
 * <p>Like {@link GlassText}, each icon is rasterised with stb_truetype at <b>exactly</b> the device
 * pixel size it's shown at and drawn 1:1 (at 12 GUI units the stroke is exactly one GUI pixel, i.e.
 * {@code scale} device pixels, so lines sit on the pixel grid). The texture holds two cells: the
 * white glyph and a blurred, slightly dropped copy used as a soft shadow, so icons stay legible on
 * bright photos without a hard offset copy.</p>
 *
 * <p>Icons are baked once per (glyph, size) and cached; nothing is allocated per frame. Callers fall
 * back to their SDF glyphs when {@link #draw} returns {@code false} (font missing / cache full).</p>
 */
public final class IconFont {
	/** Line icons (Lucide) and, for codepoints flagged with {@link #FILLED}, solid transport glyphs. */
	private static final Identifier[] FONTS = {LiquidClient.id("icons/lucide.ttf"), LiquidClient.id("icons/transport.ttf")};
	/**
	 * Flag for the filled icon set (Material Symbols Rounded, FILL 1: play, pause, skip, note):
	 * {@code draw(g, IconFont.FILLED | 0xE037, …)}.
	 */
	public static final int FILLED = 0x100000;
	private static final int MAX_ENTRIES = 192;

	private static final Entry[] ENTRIES = new Entry[MAX_ENTRIES];
	private static int count;
	/** Advances at every GuiRenderState reset; entries not used since the last one can be evicted. */
	private static int frame;
	private static int bakedScale;
	private static final @Nullable ByteBuffer[] FONT_DATA = new ByteBuffer[FONTS.length];
	private static final @Nullable STBTTFontinfo[] FONT_INFO = new STBTTFontinfo[FONTS.length];
	private static final boolean[] FONT_FAILED = new boolean[FONTS.length];

	private IconFont() {
	}

	/**
	 * Draws {@code codepoint} centred on ({@code cx}, {@code cy}) in a {@code size}×{@code size} GUI box.
	 *
	 * @param shadowColor tint of the soft shadow (alpha 0 for none)
	 * @return {@code false} if the icon can't be drawn and the caller should use its fallback
	 */
	public static boolean draw(GuiGraphicsExtractor g, int codepoint, float cx, float cy, float size, int color, int shadowColor) {
		int scale = HighDpiEngine.scale();
		if (scale != bakedScale) {
			release(); // GUI scale changed: re-bake at the new device size on demand
			bakedScale = scale;
		}
		int sizePx = Math.max(6, Math.round(size * scale));
		Entry entry = find(codepoint, sizePx);
		if (entry == null) {
			entry = bake(codepoint, sizePx);
		}
		if (entry == null || entry.textures == null) {
			return false;
		}
		entry.lastUsed = frame;
		float cell = entry.cellPx / (float) scale;
		float x = HighDpiEngine.snap(cx - cell * 0.5f);
		float y = HighDpiEngine.snap(cy - cell * 0.5f);
		int shadow = RenderUtils.applyAlpha(shadowColor);
		if ((shadow >>> 24) != 0) {
			submit(g, entry.textures, x, y, cell, 0.5f, 1f, shadow);
		}
		int tint = RenderUtils.applyAlpha(color);
		if ((tint >>> 24) != 0) {
			submit(g, entry.textures, x, y, cell, 0f, 0.5f, tint);
		}
		return true;
	}

	private static void submit(GuiGraphicsExtractor g, TextureSetup textures, float x, float y, float size, float u0, float u1, int color) {
		IconState state = IconState.obtain();
		if (state.configure(textures, g.pose(), g.scissorStack.peek(), x, y, size, u0, u1, color)) {
			g.guiRenderState.addGuiElement(state);
		} else {
			IconState.recycleLast();
		}
	}

	private static @Nullable Entry find(int codepoint, int sizePx) {
		for (int i = 0; i < count; i++) {
			Entry entry = ENTRIES[i];
			if (entry.codepoint == codepoint && entry.sizePx == sizePx) {
				return entry;
			}
		}
		return null;
	}

	// ---- Baking -------------------------------------------------------------------------------

	private static @Nullable Entry bake(int codepoint, int sizePx) {
		if (count == MAX_ENTRIES && !evictUnused()) {
			// Everything is in use this frame (a submitted state may reference it); fall back.
			return null;
		}
		Entry entry = new Entry(codepoint, sizePx);
		ENTRIES[count++] = entry; // cached even on failure so a missing glyph isn't retried every frame
		STBTTFontinfo info = ensureFont(codepoint >= FILLED ? 1 : 0);
		int glyph = codepoint & (FILLED - 1);
		if (info == null || STBTruetype.stbtt_FindGlyphIndex(info, glyph) == 0) {
			return entry;
		}
		NativeImage image = rasterize(info, glyph, sizePx, entry);
		if (image == null) {
			return entry;
		}
		try (image) {
			GpuImage created = new GpuImage("Liquid icon", image);
			Identifier id = LiquidClient.id("dynamic/icon_" + Integer.toHexString(codepoint) + "_" + sizePx);
			Minecraft.getInstance().getTextureManager().register(id, created);
			entry.id = id;
			entry.textures = TextureSetup.singleTexture(created.view(), created.linearSampler());
		}
		return entry;
	}

	private static @Nullable NativeImage rasterize(STBTTFontinfo info, int codepoint, int sizePx, Entry entry) {
		float fontScale = STBTruetype.stbtt_ScaleForMappingEmToPixels(info, sizePx);
		int pad = Math.max(3, Math.round(sizePx * 0.3f));
		int cell = sizePx + pad * 2;
		entry.cellPx = cell;
		float[] coverage = new float[cell * cell];
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer x0 = stack.mallocInt(1);
			IntBuffer y0 = stack.mallocInt(1);
			IntBuffer x1 = stack.mallocInt(1);
			IntBuffer y1 = stack.mallocInt(1);
			STBTruetype.stbtt_GetCodepointBitmapBox(info, codepoint, fontScale, fontScale, x0, y0, x1, y1);
			int gw = x1.get(0) - x0.get(0);
			int gh = y1.get(0) - y0.get(0);
			if (gw <= 0 || gh <= 0) {
				return null;
			}
			// The icon grid is the em box, standing on the baseline (true for both icon fonts).
			int ox = pad + x0.get(0);
			int oy = pad + sizePx + y0.get(0);
			ByteBuffer glyph = MemoryUtil.memCalloc(gw * gh);
			try {
				STBTruetype.stbtt_MakeCodepointBitmap(info, glyph, gw, gh, gw, fontScale, fontScale, codepoint);
				for (int y = 0; y < gh; y++) {
					int cy = oy + y;
					if (cy < 0 || cy >= cell) {
						continue;
					}
					for (int x = 0; x < gw; x++) {
						int cx = ox + x;
						if (cx >= 0 && cx < cell) {
							coverage[cy * cell + cx] = (glyph.get(y * gw + x) & 0xFF) / 255f;
						}
					}
				}
			} finally {
				MemoryUtil.memFree(glyph);
			}
		}

		// Soft shadow: dropped by 1/24 of the size, blurred like a small gaussian.
		int drop = Math.max(1, Math.round(sizePx / 24f));
		float[] shifted = new float[cell * cell];
		System.arraycopy(coverage, 0, shifted, drop * cell, (cell - drop) * cell);
		float[] shadow = GlassText.blur(shifted, cell, cell, Math.max(1, Math.round(sizePx * 0.06f)), 3);

		NativeImage image = new NativeImage(cell * 2, cell, false);
		for (int y = 0; y < cell; y++) {
			for (int x = 0; x < cell; x++) {
				int i = y * cell + x;
				int a = Math.round(Math.clamp(coverage[i], 0f, 1f) * 255f);
				int s = Math.round(Math.clamp(shadow[i] * 1.6f, 0f, 1f) * 255f);
				// White everywhere, so linear filtering never darkens the anti-aliased edge.
				image.setPixel(x, y, a << 24 | 0xFFFFFF);
				image.setPixel(cell + x, y, s << 24 | 0xFFFFFF);
			}
		}
		return image;
	}

	private static @Nullable STBTTFontinfo ensureFont(int index) {
		if (FONT_INFO[index] != null || FONT_FAILED[index]) {
			return FONT_INFO[index];
		}
		Identifier fontId = FONTS[index];
		try {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(fontId);
			if (resource.isEmpty()) {
				throw new IOException("missing " + fontId);
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
				throw new IOException("stb_truetype couldn't parse " + fontId);
			}
			FONT_DATA[index] = data; // must outlive the font info
			FONT_INFO[index] = info;
		} catch (IOException | RuntimeException e) {
			FONT_FAILED[index] = true;
			LiquidClient.LOGGER.warn("Icon font {} unavailable, using vector fallbacks", fontId, e);
		}
		return FONT_INFO[index];
	}

	/** Frees every baked icon texture (screen removal, GUI scale change). Render thread. */
	public static void release() {
		for (int i = 0; i < count; i++) {
			Identifier id = ENTRIES[i].id;
			if (id != null) {
				Minecraft.getInstance().getTextureManager().release(id);
			}
			ENTRIES[i] = null;
		}
		count = 0;
	}

	/**
	 * Frees the entries not drawn since the last GUI state reset. Their textures can't be referenced
	 * by this frame's submitted states (earlier frames were already drawn), so they're safe to drop.
	 *
	 * @return whether room was made
	 */
	private static boolean evictUnused() {
		int kept = 0;
		for (int i = 0; i < count; i++) {
			Entry entry = ENTRIES[i];
			if (entry.lastUsed == frame) {
				ENTRIES[kept++] = entry;
			} else if (entry.id != null) {
				Minecraft.getInstance().getTextureManager().release(entry.id);
			}
		}
		for (int i = kept; i < count; i++) {
			ENTRIES[i] = null;
		}
		count = kept;
		return count < MAX_ENTRIES;
	}

	/** Called when vanilla resets the frame's GuiRenderState (see {@code GuiRenderStateMixin}). */
	public static void resetPool() {
		IconState.cursor = 0;
		frame++;
	}

	private static final class Entry {
		final int codepoint;
		final int sizePx;
		int cellPx;
		int lastUsed;
		@Nullable Identifier id;
		@Nullable TextureSetup textures;

		Entry(int codepoint, int sizePx) {
			this.codepoint = codepoint;
			this.sizePx = sizePx;
		}
	}

	// ---- Render state -------------------------------------------------------------------------

	/** One tinted, textured quad (vanilla {@code gui_textured} pipeline), pooled per frame. */
	private static final class IconState implements GuiElementRenderState {
		private static final ArrayList<IconState> POOL = new ArrayList<>(32);
		private static int cursor;

		private final Matrix3x2f pose = new Matrix3x2f();
		private TextureSetup textures = TextureSetup.noTexture();
		private @Nullable ScreenRectangle scissor;
		private float x0;
		private float y0;
		private float x1;
		private float y1;
		private float u0;
		private float u1;
		private int color;
		private @Nullable ScreenRectangle bounds;
		private int bx0 = Integer.MIN_VALUE;
		private int by0;
		private int bx1;
		private int by1;

		static IconState obtain() {
			if (cursor == POOL.size()) {
				POOL.add(new IconState());
			}
			return POOL.get(cursor++);
		}

		static void recycleLast() {
			cursor--;
		}

		boolean configure(TextureSetup textures, Matrix3x2fc pose, @Nullable ScreenRectangle scissor,
				float x, float y, float size, float u0, float u1, int color) {
			this.textures = textures;
			this.pose.set(pose);
			this.scissor = scissor;
			this.x0 = x;
			this.y0 = y;
			this.x1 = x + size;
			this.y1 = y + size;
			this.u0 = u0;
			this.u1 = u1;
			this.color = color;
			return updateBounds();
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			consumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, 0f).setColor(color);
			consumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, 1f).setColor(color);
			consumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, 1f).setColor(color);
			consumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, 0f).setColor(color);
		}

		private boolean updateBounds() {
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
			return RenderPipelines.GUI_TEXTURED;
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
