package net.client.visuals.render.text;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.GlassText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Pixel-exact UI text with the bundled Inter, replacing Minecraft's glyph path for every
 * {@code LiquidText} label.
 *
 * <h2>Why not the vanilla font renderer</h2>
 * Vanilla full-hints TTF glyphs (shapes distorted, every advance rounded to whole pixels), has no
 * kerning, and its text shader {@code discard}s every fragment under 10 % alpha, which chops the
 * anti-aliased fringe off each letter (and erases soft shadows entirely). Here glyphs are
 * light-hinted with fractional positioning and GPOS kerning ({@link SharpFace}), stored in an RGBA
 * atlas as white + coverage alpha, and drawn 1:1 at device resolution through the plain textured GUI
 * pipeline, which blends the full coverage.
 *
 * <h2>Cost</h2>
 * Glyphs are rasterised once per (face, pixel size, ¼-pixel phase) into 1024² atlas pages; labels
 * cache their quads ({@link SharpLayout}). Drawing a label submits one pooled render state per atlas
 * page it touches (normally one) – no per-frame allocation. When the atlas fills up (many GUI scale
 * changes) it's flushed at the next frame boundary and labels re-layout lazily.
 *
 * <p>Codepoints outside Inter (CJK names, emoji…) make that label fall back to the vanilla path, so
 * nothing ever renders as tofu.</p>
 */
public final class SharpText {
	public static final int WEIGHT_REGULAR = 0;
	public static final int WEIGHT_SEMIBOLD = 1;
	public static final int WEIGHT_BOLD = 2;

	/**
	 * Faces of the Lyrics element (font/lyrics/*.ttf, one file each, Latin + Latin Extended-A). Their
	 * weight slot is {@link #LYRIC_BASE} + index; they're opened on first use and optional – a face
	 * that can't be loaded makes its labels fall back to Minecraft's renderer.
	 */
	public static final String[] LYRIC_FACES = {
			"elegant", "elegant_bold", "elegant_italic", "elegant_bold_italic", "script", "script_bold", "poster",
			"rounded", "rounded_bold", "rounded_italic", "mono", "mono_bold", "mono_italic", "marker", "futuristic",
			"futuristic_bold", "handwriting", "handwriting_bold"
	};
	/** Weight slot of the first lyric face (after Inter's three weights). */
	public static final int LYRIC_BASE = 3;

	/** Two files per weight slot: the face, and an optional extension face (Inter's latin-ext). */
	private static final String[] FILES = files();
	/** Sub-pixel positions per pixel. */
	private static final int PHASES = 4;
	private static final int PAGE_SIZE = 1024;
	/** Past this many pages a flush is scheduled; {@link #MAX_PAGES} is the hard cap until it runs. */
	private static final int SOFT_PAGES = 4;
	private static final int MAX_PAGES = 6;
	/**
	 * Coverage → alpha. Blending happens in sRGB space, which makes light text on darker photos look
	 * thinner than designed; a mild curve restores the intended weight (like macOS' stem darkening).
	 */
	private static final int[] COVERAGE_CURVE = new int[256];

	static {
		for (int i = 0; i < 256; i++) {
			COVERAGE_CURVE[i] = Math.round((float) Math.pow(i / 255.0, 0.87) * 255f);
		}
	}

	private static final SharpFace[] FACES = new SharpFace[FILES.length];
	/** Weight slots past Inter's whose faces have been tried (they load lazily). */
	private static final boolean[] TRIED = new boolean[FILES.length / 2];
	private static long library;
	private static boolean loaded;
	private static boolean failed;

	private static final ArrayList<Page> PAGES = new ArrayList<>(MAX_PAGES);
	private static final Long2ObjectOpenHashMap<Glyph> GLYPHS = new Long2ObjectOpenHashMap<>(1024);
	private static int generation;
	private static boolean flushRequested;

	private SharpText() {
	}

	private static String[] files() {
		String[] files = new String[(LYRIC_BASE + LYRIC_FACES.length) * 2];
		String[] inter = {
				"font/inter-latin-400.ttf", "font/inter-latin-ext-400.ttf",
				"font/inter-latin-600.ttf", "font/inter-latin-ext-600.ttf",
				"font/inter-latin-700.ttf", "font/inter-latin-ext-700.ttf",
		};
		System.arraycopy(inter, 0, files, 0, inter.length);
		for (int i = 0; i < LYRIC_FACES.length; i++) {
			files[(LYRIC_BASE + i) * 2] = "font/lyrics/" + LYRIC_FACES[i] + ".ttf";
		}
		return files;
	}

	/** Weight slot of a lyric face by id (see {@link #LYRIC_FACES}). */
	public static int lyricWeight(String id) {
		for (int i = 0; i < LYRIC_FACES.length; i++) {
			if (LYRIC_FACES[i].equals(id)) {
				return LYRIC_BASE + i;
			}
		}
		throw new IllegalArgumentException("Unknown lyric face " + id);
	}

	// ---- Faces --------------------------------------------------------------------------------

	/** Loads the faces on first use; false if FreeType or the font files are unavailable. */
	public static boolean available() {
		if (!loaded && !failed) {
			load();
		}
		return loaded;
	}

	private static void load() {
		try {
			library = FreeTypeUtil.getLibrary();
		} catch (RuntimeException e) {
			fail(e);
			return;
		}
		for (int i = 0; i < LYRIC_BASE * 2; i++) {
			try {
				FACES[i] = SharpFace.open(read(FILES[i]), library, FreeTypeUtil.LIBRARY_LOCK);
			} catch (IOException | RuntimeException e) {
				if (i % 2 == 0) { // the latin faces are required, latin-ext is a bonus
					fail(e);
					return;
				}
				LiquidClient.LOGGER.warn("Sharp text: optional face {} unavailable", FILES[i], e);
			}
		}
		for (int w = 0; w < LYRIC_BASE; w++) {
			TRIED[w] = true;
		}
		loaded = true;
	}

	/** Opens a lyric face the first time it's asked for. */
	private static void loadLazily(int weight) {
		TRIED[weight] = true;
		for (int f = weight * 2; f < weight * 2 + 2; f++) {
			if (FILES[f] == null) {
				continue;
			}
			try {
				FACES[f] = SharpFace.open(read(FILES[f]), library, FreeTypeUtil.LIBRARY_LOCK);
			} catch (IOException | RuntimeException e) {
				LiquidClient.LOGGER.warn("Sharp text: face {} unavailable", FILES[f], e);
			}
		}
	}

	private static void fail(Exception e) {
		failed = true;
		closeFaces();
		LiquidClient.LOGGER.warn("Sharp text unavailable, using Minecraft's font renderer", e);
	}

	private static ByteBuffer read(String path) throws IOException {
		Identifier id = LiquidClient.id(path);
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
		if (resource.isEmpty()) {
			throw new IOException("missing " + id);
		}
		byte[] bytes;
		try (InputStream in = resource.get().open()) {
			bytes = in.readAllBytes();
		}
		ByteBuffer data = MemoryUtil.memAlloc(bytes.length);
		data.put(bytes).flip();
		return data;
	}

	/** @return {@code face << 16 | glyph}, or -1 when no face of this weight has the codepoint */
	private static int resolve(int weight, int codepoint) {
		if (weight < 0 || weight >= TRIED.length) {
			return -1;
		}
		if (!TRIED[weight]) {
			loadLazily(weight);
		}
		for (int f = weight * 2; f < weight * 2 + 2; f++) {
			SharpFace face = FACES[f];
			if (face != null) {
				int glyph = face.glyphIndex(codepoint);
				if (glyph != 0) {
					return f << 16 | glyph;
				}
			}
		}
		return -1;
	}

	// ---- Layout -------------------------------------------------------------------------------

	/** Whether every codepoint of {@code s} can be drawn in this weight. */
	public static boolean supports(int weight, String s) {
		if (!available()) {
			return false;
		}
		for (int i = 0; i < s.length(); ) {
			int cp = s.codePointAt(i);
			if (resolve(weight, cp) < 0) {
				return false;
			}
			i += Character.charCount(cp);
		}
		return true;
	}

	/** Advance width of {@code s} in pixels (kerned, with {@code trackingPx} between characters). */
	public static float measure(int weight, float emPx, String s, float trackingPx) {
		float pen = 0f;
		int prev = -1;
		int count = 0;
		for (int i = 0; i < s.length(); ) {
			int cp = s.codePointAt(i);
			i += Character.charCount(cp);
			int r = resolve(weight, cp);
			if (r < 0) {
				continue;
			}
			SharpFace face = FACES[r >>> 16];
			int glyph = r & 0xFFFF;
			if (prev >= 0 && (prev >>> 16) == (r >>> 16)) {
				pen += face.kern(prev & 0xFFFF, glyph, emPx);
			}
			pen += face.advance(glyph, emPx) + trackingPx;
			prev = r;
			count++;
		}
		return count > 0 ? pen - trackingPx : 0f;
	}

	/**
	 * Lays {@code s} out into {@code out}: quads in GUI units relative to (0, baseline).
	 *
	 * @param shadow use the blurred shadow glyphs instead of the sharp ones
	 */
	public static void layout(SharpLayout out, int weight, float emPx, int scale, String s, float trackingPx, boolean shadow) {
		out.clear();
		float inv = 1f / scale;
		float pen = 0f;
		int prev = -1;
		for (int i = 0; i < s.length(); ) {
			int cp = s.codePointAt(i);
			i += Character.charCount(cp);
			int r = resolve(weight, cp);
			if (r < 0) {
				continue;
			}
			int faceIndex = r >>> 16;
			SharpFace face = FACES[faceIndex];
			int glyphId = r & 0xFFFF;
			if (prev >= 0 && (prev >>> 16) == faceIndex) {
				pen += face.kern(prev & 0xFFFF, glyphId, emPx);
			}
			float whole = (float) Math.floor(pen + 1e-3f);
			int phase = Math.round((pen - whole) * PHASES);
			if (phase >= PHASES) {
				whole += 1f;
				phase = 0;
			}
			Glyph glyph = glyph(faceIndex, glyphId, emPx, phase, shadow, scale);
			if (glyph.width > 0) {
				float x0 = whole + glyph.left;
				float y0 = -glyph.top;
				out.add(glyph.page, x0 * inv, y0 * inv, (x0 + glyph.width) * inv, (y0 + glyph.height) * inv,
						glyph.u0, glyph.v0, glyph.u1, glyph.v1);
			}
			pen += face.advance(glyphId, emPx) + trackingPx;
			prev = r;
		}
	}

	/** Atlas generation; layouts built against an older one must be rebuilt. */
	public static int generation() {
		return generation;
	}

	// ---- Drawing ------------------------------------------------------------------------------

	/** Submits a layout with its left edge at {@code x} and baseline at {@code baselineY} (GUI units). */
	public static void draw(GuiGraphicsExtractor g, SharpLayout layout, float x, float baselineY, int color) {
		if (layout.isEmpty() || (color >>> 24) == 0) {
			return;
		}
		int mask = layout.pageMask;
		while (mask != 0) {
			int page = Integer.numberOfTrailingZeros(mask);
			mask &= mask - 1;
			if (page >= PAGES.size()) {
				continue; // flushed underneath us; the label re-lays out next frame
			}
			RunState state = RunState.obtain();
			if (state.configure(PAGES.get(page).setup, g.pose(), g.scissorStack.peek(), layout, page, x, baselineY, color)) {
				g.guiRenderState.addGuiElement(state);
			} else {
				RunState.recycleLast();
			}
		}
	}

	// ---- Atlas --------------------------------------------------------------------------------

	private static final class Glyph {
		static final Glyph EMPTY = new Glyph(0, 0, 0, 0, 0, 0f, 0f, 0f, 0f);

		final int page;
		final int width;
		final int height;
		final int left;
		final int top;
		final float u0;
		final float v0;
		final float u1;
		final float v1;

		Glyph(int page, int width, int height, int left, int top, float u0, float v0, float u1, float v1) {
			this.page = page;
			this.width = width;
			this.height = height;
			this.left = left;
			this.top = top;
			this.u0 = u0;
			this.v0 = v0;
			this.u1 = u1;
			this.v1 = v1;
		}
	}

	private static final class Page extends AbstractTexture {
		final Identifier id;
		final TextureSetup setup;
		int cursorX;
		int cursorY;
		int shelfHeight;

		Page(int index) {
			GpuDevice device = RenderSystem.getDevice();
			// usage 5 = COPY_DST | TEXTURE_BINDING
			this.texture = device.createTexture(() -> "Liquid sharp text " + index, 5, GpuFormat.RGBA8_UNORM, PAGE_SIZE, PAGE_SIZE, 1, 1);
			this.textureView = device.createTextureView(this.texture);
			this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
			try (NativeImage blank = new NativeImage(PAGE_SIZE, PAGE_SIZE, true)) {
				device.createCommandEncoder().writeToTexture(this.texture, blank);
			}
			this.setup = TextureSetup.singleTexture(this.textureView, this.sampler);
			this.id = LiquidClient.id("dynamic/sharp_text_" + index);
			Minecraft.getInstance().getTextureManager().register(id, this);
		}

		/** Shelf packing; returns {@code x << 16 | y} or -1. */
		int place(int w, int h) {
			if (cursorX + w > PAGE_SIZE) {
				cursorX = 0;
				cursorY += shelfHeight;
				shelfHeight = 0;
			}
			if (cursorY + h > PAGE_SIZE || w > PAGE_SIZE) {
				return -1;
			}
			int slot = cursorX << 16 | cursorY;
			cursorX += w;
			shelfHeight = Math.max(shelfHeight, h);
			return slot;
		}
	}

	private static Glyph glyph(int face, int glyphId, float emPx, int phase, boolean shadow, int scale) {
		long size = Math.min(0xFFFFF, Math.round(emPx * 64f));
		long key = size | (long) glyphId << 20 | (long) phase << 36 | (shadow ? 1L : 0L) << 38 | (long) (face & 63) << 39
				| (long) Math.min(15, scale) << 45;
		Glyph glyph = GLYPHS.get(key);
		if (glyph == null) {
			glyph = bake(face, glyphId, emPx, phase, shadow, scale);
			GLYPHS.put(key, glyph);
		}
		return glyph;
	}

	private static Glyph bake(int face, int glyphId, float emPx, int phase, boolean shadow, int scale) {
		SharpFace.Coverage coverage = FACES[face].rasterize(glyphId, emPx, phase / (float) PHASES);
		if (coverage == null) {
			return Glyph.EMPTY;
		}
		int w = coverage.width();
		int h = coverage.height();
		int left = coverage.left();
		int top = coverage.top();
		float[] alpha;
		if (shadow) {
			// Soft shadow glyph: coverage blurred (≈ gaussian, σ ≈ 0.7 GUI px), padded to fit the tail.
			int radius = Math.max(1, Math.round(scale * 0.5f));
			int pad = radius * 3;
			int sw = w + pad * 2;
			int sh = h + pad * 2;
			float[] src = new float[sw * sh];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					src[(y + pad) * sw + x + pad] = (coverage.alpha()[y * w + x] & 0xFF) / 255f;
				}
			}
			alpha = GlassText.blur(src, sw, sh, radius, 3);
			w = sw;
			h = sh;
			left -= pad;
			top += pad;
		} else {
			alpha = new float[w * h];
			for (int i = 0; i < alpha.length; i++) {
				alpha[i] = COVERAGE_CURVE[coverage.alpha()[i] & 0xFF] / 255f;
			}
		}

		// 1px transparent-white border so linear filtering (while scaled) never bleeds or darkens.
		int pw = w + 2;
		int ph = h + 2;
		int slot = allocate(pw, ph);
		if (slot < 0) {
			return Glyph.EMPTY;
		}
		int page = slot >>> 24;
		int ax = slot >>> 12 & 0xFFF;
		int ay = slot & 0xFFF;
		try (NativeImage image = new NativeImage(pw, ph, false)) {
			image.fillRect(0, 0, pw, ph, 0x00FFFFFF);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int a = Math.round(Math.clamp(alpha[y * w + x], 0f, 1f) * 255f);
					image.setPixel(x + 1, y + 1, a << 24 | 0xFFFFFF);
				}
			}
			RenderSystem.getDevice().createCommandEncoder().writeToTexture(PAGES.get(page).getTexture(), image, 0, 0, ax, ay);
		}
		float inv = 1f / PAGE_SIZE;
		return new Glyph(page, w, h, left, top, (ax + 1) * inv, (ay + 1) * inv, (ax + 1 + w) * inv, (ay + 1 + h) * inv);
	}

	/** @return {@code page << 24 | x << 12 | y}, or -1 when the atlas is full */
	private static int allocate(int w, int h) {
		for (int i = 0; i < PAGES.size(); i++) {
			int slot = PAGES.get(i).place(w, h);
			if (slot >= 0) {
				return i << 24 | (slot >>> 16) << 12 | (slot & 0xFFFF);
			}
		}
		if (PAGES.size() >= SOFT_PAGES) {
			flushRequested = true;
		}
		if (PAGES.size() >= MAX_PAGES) {
			return -1;
		}
		Page page = new Page(PAGES.size());
		PAGES.add(page);
		int slot = page.place(w, h);
		return slot < 0 ? -1 : (PAGES.size() - 1) << 24 | (slot >>> 16) << 12 | (slot & 0xFFFF);
	}

	// ---- Lifecycle ----------------------------------------------------------------------------

	/**
	 * Frame boundary (from {@code GuiRenderStateMixin}): recycles render states and, if the atlas
	 * asked for it, flushes it while nothing references its pages.
	 */
	public static void onFrameReset() {
		RunState.cursor = 0;
		if (flushRequested) {
			flushRequested = false;
			releasePages();
		}
	}

	private static void releasePages() {
		for (Page page : PAGES) {
			Minecraft.getInstance().getTextureManager().release(page.id);
		}
		PAGES.clear();
		GLYPHS.clear();
		generation++;
	}

	/** Frees atlas pages and FreeType faces (client stopping). Render thread. */
	public static void release() {
		releasePages();
		closeFaces();
		java.util.Arrays.fill(TRIED, false);
		loaded = false;
	}

	private static void closeFaces() {
		for (int i = 0; i < FACES.length; i++) {
			if (FACES[i] != null) {
				FACES[i].close(FreeTypeUtil.LIBRARY_LOCK);
				FACES[i] = null;
			}
		}
	}

	// ---- Render state -------------------------------------------------------------------------

	/** All glyph quads of one layout on one atlas page, as a single pooled element. */
	private static final class RunState implements GuiElementRenderState {
		private static final ArrayList<RunState> POOL = new ArrayList<>(64);
		private static int cursor;

		private final Matrix3x2f pose = new Matrix3x2f();
		private TextureSetup textures = TextureSetup.noTexture();
		private @Nullable ScreenRectangle scissor;
		private SharpLayout layout = new SharpLayout();
		private int page;
		private float x;
		private float y;
		private int color;
		private @Nullable ScreenRectangle bounds;
		private int bx0 = Integer.MIN_VALUE;
		private int by0;
		private int bx1;
		private int by1;

		static RunState obtain() {
			if (cursor == POOL.size()) {
				POOL.add(new RunState());
			}
			return POOL.get(cursor++);
		}

		static void recycleLast() {
			cursor--;
		}

		boolean configure(TextureSetup textures, Matrix3x2fc pose, @Nullable ScreenRectangle scissor, SharpLayout layout,
				int page, float x, float y, int color) {
			this.textures = textures;
			this.pose.set(pose);
			this.scissor = scissor;
			this.layout = layout;
			this.page = page;
			this.x = x;
			this.y = y;
			this.color = color;
			return updateBounds(x + layout.minX, y + layout.minY, x + layout.maxX, y + layout.maxY);
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			SharpLayout l = layout;
			int[] pages = l.pages;
			float[] q = l.quads;
			int count = Math.min(l.count, pages.length);
			for (int i = 0; i < count; i++) {
				if (pages[i] != page) {
					continue;
				}
				int k = i * SharpLayout.STRIDE;
				float x0 = x + q[k];
				float y0 = y + q[k + 1];
				float x1 = x + q[k + 2];
				float y1 = y + q[k + 3];
				consumer.addVertexWith2DPose(pose, x0, y0).setUv(q[k + 4], q[k + 5]).setColor(color);
				consumer.addVertexWith2DPose(pose, x0, y1).setUv(q[k + 4], q[k + 7]).setColor(color);
				consumer.addVertexWith2DPose(pose, x1, y1).setUv(q[k + 6], q[k + 7]).setColor(color);
				consumer.addVertexWith2DPose(pose, x1, y0).setUv(q[k + 6], q[k + 5]).setColor(color);
			}
		}

		private boolean updateBounds(float x0, float y0, float x1, float y1) {
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
