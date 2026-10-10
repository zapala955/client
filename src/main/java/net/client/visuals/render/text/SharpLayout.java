package net.client.visuals.render.text;

/**
 * A laid-out run of glyph quads, owned by a {@code TextLabel} and rebuilt only when its text, style,
 * GUI scale or the atlas changes. Coordinates are GUI units relative to (left edge, baseline); every
 * quad is an exact device-pixel rectangle, drawn 1:1 from the atlas.
 *
 * <p>Arrays only ever grow, so a render state that still references this layout during the frame it
 * was rebuilt never reads out of bounds.</p>
 */
public final class SharpLayout {
	static final int STRIDE = 8; // x0 y0 x1 y1 u0 v0 u1 v1

	int count;
	int[] pages = new int[8];
	float[] quads = new float[8 * STRIDE];
	/** Bit i set when some glyph lives on atlas page i. */
	int pageMask;
	float minX;
	float minY;
	float maxX;
	float maxY;

	void clear() {
		count = 0;
		pageMask = 0;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = -Float.MAX_VALUE;
		maxY = -Float.MAX_VALUE;
	}

	void add(int page, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
		if (count == pages.length) {
			pages = java.util.Arrays.copyOf(pages, count * 2);
			quads = java.util.Arrays.copyOf(quads, count * 2 * STRIDE);
		}
		pages[count] = page;
		int i = count * STRIDE;
		quads[i] = x0;
		quads[i + 1] = y0;
		quads[i + 2] = x1;
		quads[i + 3] = y1;
		quads[i + 4] = u0;
		quads[i + 5] = v0;
		quads[i + 6] = u1;
		quads[i + 7] = v1;
		count++;
		pageMask |= 1 << page;
		minX = Math.min(minX, x0);
		minY = Math.min(minY, y0);
		maxX = Math.max(maxX, x1);
		maxY = Math.max(maxY, y1);
	}

	boolean isEmpty() {
		return count == 0;
	}
}
