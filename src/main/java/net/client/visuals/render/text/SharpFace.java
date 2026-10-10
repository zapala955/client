package net.client.visuals.render.text;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * One TrueType file, rasterised the way macOS / modern browsers do it rather than the way Minecraft
 * does:
 *
 * <ul>
 *   <li><b>Light hinting</b> ({@code FT_LOAD_TARGET_LIGHT}): only vertical metrics snap to the pixel
 *       grid, so baselines and x-heights are crisp while letter shapes keep their true design.
 *       (Minecraft uses full hinting, which distorts glyphs horizontally.)</li>
 *   <li><b>Fractional positioning</b>: unhinted advances plus outlines shifted by a sub-pixel phase
 *       before rendering, so spacing is exact instead of rounded per glyph.</li>
 *   <li><b>Kerning</b> from GPOS ({@link GposKerning}).</li>
 * </ul>
 *
 * <p>Advance widths are read from {@code hmtx} directly, so measuring text never touches FreeType.
 * Pure LWJGL (no Minecraft types) – the caller supplies the FreeType library and its lock.</p>
 */
final class SharpFace {
	private static final int TAG_HEAD = 0x68656164;
	private static final int TAG_HHEA = 0x68686561;
	private static final int TAG_HMTX = 0x686D7478;

	private final ByteBuffer data;
	private final FT_Face face;
	private final GposKerning kerning;
	private final int unitsPerEm;
	private final int[] advances;
	private long currentSize = -1L;

	private SharpFace(ByteBuffer data, FT_Face face, GposKerning kerning, int unitsPerEm, int[] advances) {
		this.data = data;
		this.face = face;
		this.kerning = kerning;
		this.unitsPerEm = unitsPerEm;
		this.advances = advances;
	}

	/**
	 * @param data font file in native memory; owned by the face from now on (freed by {@link #close})
	 */
	static SharpFace open(ByteBuffer data, long library, Object libraryLock) {
		FT_Face face;
		synchronized (libraryLock) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer out = stack.mallocPointer(1);
				check(FreeType.FT_New_Memory_Face(library, data, 0L, out), "FT_New_Memory_Face");
				face = FT_Face.create(out.get(0));
			}
			int error = FreeType.FT_Select_Charmap(face, FreeType.FT_ENCODING_UNICODE);
			if (error != 0) {
				FreeType.FT_Done_Face(face);
				check(error, "FT_Select_Charmap");
			}
		}
		ByteBuffer be = data.duplicate().order(ByteOrder.BIG_ENDIAN);
		int head = table(be, TAG_HEAD);
		int hhea = table(be, TAG_HHEA);
		int hmtx = table(be, TAG_HMTX);
		int unitsPerEm = head >= 0 ? be.getShort(head + 18) & 0xFFFF : face.units_per_EM() & 0xFFFF;
		int glyphCount = (int) face.num_glyphs();
		int[] advances = new int[Math.max(0, glyphCount)];
		if (hhea >= 0 && hmtx >= 0) {
			int metrics = be.getShort(hhea + 34) & 0xFFFF;
			int last = 0;
			for (int i = 0; i < advances.length; i++) {
				if (i < metrics) {
					last = be.getShort(hmtx + i * 4) & 0xFFFF;
				}
				advances[i] = last;
			}
		}
		return new SharpFace(data, face, GposKerning.parse(data), Math.max(1, unitsPerEm), advances);
	}

	int glyphIndex(int codepoint) {
		return FreeType.FT_Get_Char_Index(face, codepoint);
	}

	/** Unhinted advance in pixels at {@code emPx}. */
	float advance(int glyph, float emPx) {
		return glyph >= 0 && glyph < advances.length ? advances[glyph] * emPx / unitsPerEm : 0f;
	}

	/** Pair kerning in pixels at {@code emPx}. */
	float kern(int left, int right, float emPx) {
		return kerning.kern(left, right) * emPx / unitsPerEm;
	}

	/** A rendered glyph: 8-bit coverage, {@code pitch} bytes per row, plus bearings in pixels. */
	record Coverage(byte[] alpha, int width, int height, int left, int top) {
	}

	/**
	 * Rasterises {@code glyph} at {@code emPx} with the outline shifted right by {@code phase} pixels
	 * (0 ≤ phase < 1). Returns null for blank glyphs (space) or on error.
	 */
	@Nullable Coverage rasterize(int glyph, float emPx, float phase) {
		long size = Math.round(emPx * 64f);
		if (size != currentSize) {
			if (FreeType.FT_Set_Char_Size(face, 0L, size, 72, 72) != 0) {
				return null;
			}
			currentSize = size;
		}
		if (FreeType.FT_Load_Glyph(face, glyph, FreeType.FT_LOAD_TARGET_LIGHT | FreeType.FT_LOAD_NO_BITMAP) != 0) {
			return null;
		}
		FT_GlyphSlot slot = face.glyph();
		if (slot == null) {
			return null;
		}
		long shift = Math.round(phase * 64f);
		if (shift != 0L) {
			FreeType.FT_Outline_Translate(slot.outline(), shift, 0L);
		}
		if (FreeType.FT_Render_Glyph(slot, FreeType.FT_RENDER_MODE_LIGHT) != 0) {
			return null;
		}
		FT_Bitmap bitmap = slot.bitmap();
		int w = bitmap.width();
		int h = bitmap.rows();
		int pitch = bitmap.pitch();
		if (w <= 0 || h <= 0 || bitmap.pixel_mode() != FreeType.FT_PIXEL_MODE_GRAY) {
			return null;
		}
		ByteBuffer buffer = bitmap.buffer(Math.abs(pitch) * h);
		if (buffer == null) {
			return null;
		}
		byte[] alpha = new byte[w * h];
		for (int y = 0; y < h; y++) {
			int row = pitch >= 0 ? y * pitch : (h - 1 - y) * -pitch;
			for (int x = 0; x < w; x++) {
				alpha[y * w + x] = buffer.get(row + x);
			}
		}
		return new Coverage(alpha, w, h, slot.bitmap_left(), slot.bitmap_top());
	}

	void close(Object libraryLock) {
		synchronized (libraryLock) {
			FreeType.FT_Done_Face(face);
		}
		MemoryUtil.memFree(data);
	}

	private static int table(ByteBuffer data, int tag) {
		int numTables = data.getShort(4) & 0xFFFF;
		for (int i = 0; i < numTables; i++) {
			int record = 12 + i * 16;
			if (data.getInt(record) == tag) {
				return data.getInt(record + 8);
			}
		}
		return -1;
	}

	private static void check(int error, String what) {
		if (error != 0) {
			throw new IllegalStateException("FreeType error " + error + " in " + what);
		}
	}
}
