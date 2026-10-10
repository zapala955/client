package net.client.visuals.render.text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Pair kerning from an OpenType {@code GPOS} table.
 *
 * <p>Modern fonts (Inter included) keep kerning only in GPOS – often inside extension lookups –
 * which neither Minecraft's text renderer nor stb_truetype read. This implements the part that
 * matters for UI text: lookups of the {@code kern} feature, PairPos formats 1 (glyph pairs) and 2
 * (class pairs), direct or wrapped in extension lookups (type 9), applying the first glyph's
 * {@code XAdvance}. Lookups are applied in order and summed, like a shaper does for a pair.</p>
 *
 * <p>Only used while laying out text (not per frame); all reads are bounds-checked offsets into the
 * font data, and malformed tables simply yield no kerning.</p>
 */
final class GposKerning {
	private static final int TAG_GPOS = 0x47504F53;
	private static final int TAG_KERN = 0x6B65726E;
	private static final int VALUE_X_ADVANCE = 0x0004;

	private final ByteBuffer data;
	/** Absolute offsets of every PairPos subtable of the kern lookups, grouped per lookup. */
	private final int[][] lookups;

	private GposKerning(ByteBuffer data, int[][] lookups) {
		this.data = data;
		this.lookups = lookups;
	}

	static GposKerning parse(ByteBuffer font) {
		ByteBuffer data = font.duplicate().order(ByteOrder.BIG_ENDIAN);
		try {
			int gpos = findTable(data, TAG_GPOS);
			if (gpos < 0) {
				return new GposKerning(data, new int[0][]);
			}
			int featureList = gpos + u16(data, gpos + 6);
			int lookupList = gpos + u16(data, gpos + 8);
			int lookupCount = u16(data, lookupList);

			// Lookup indices referenced by any 'kern' feature (every script / language).
			boolean[] used = new boolean[lookupCount];
			int featureCount = u16(data, featureList);
			for (int i = 0; i < featureCount; i++) {
				int record = featureList + 2 + i * 6;
				if (data.getInt(record) != TAG_KERN) {
					continue;
				}
				int feature = featureList + u16(data, record + 4);
				int indexCount = u16(data, feature + 2);
				for (int k = 0; k < indexCount; k++) {
					int index = u16(data, feature + 4 + k * 2);
					if (index < lookupCount) {
						used[index] = true;
					}
				}
			}

			int[][] result = new int[lookupCount][];
			int kept = 0;
			for (int i = 0; i < lookupCount; i++) {
				if (!used[i]) {
					continue;
				}
				int lookup = lookupList + u16(data, lookupList + 2 + i * 2);
				int type = u16(data, lookup);
				int subCount = u16(data, lookup + 4);
				int[] subtables = new int[subCount];
				int n = 0;
				for (int s = 0; s < subCount; s++) {
					int sub = lookup + u16(data, lookup + 6 + s * 2);
					int subType = type;
					if (type == 9) { // extension: format, extensionLookupType, Offset32
						subType = u16(data, sub + 2);
						sub = sub + data.getInt(sub + 4);
					}
					if (subType == 2) {
						subtables[n++] = sub;
					}
				}
				if (n > 0) {
					result[kept++] = n == subCount ? subtables : java.util.Arrays.copyOf(subtables, n);
				}
			}
			return new GposKerning(data, java.util.Arrays.copyOf(result, kept));
		} catch (IndexOutOfBoundsException e) {
			return new GposKerning(data, new int[0][]);
		}
	}

	/** Kerning between two glyph ids, in font units (usually negative). */
	int kern(int left, int right) {
		int total = 0;
		try {
			for (int[] lookup : lookups) {
				for (int sub : lookup) {
					int applied = pairAdjust(sub, left, right);
					if (applied != Integer.MIN_VALUE) {
						total += applied;
						break; // first applicable subtable wins within a lookup
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
		return total;
	}

	/** @return the XAdvance adjustment, or MIN_VALUE when this subtable doesn't apply to the pair */
	private int pairAdjust(int sub, int left, int right) {
		int format = u16(data, sub);
		int coverageIndex = coverage(sub + u16(data, sub + 2), left);
		if (coverageIndex < 0) {
			return Integer.MIN_VALUE;
		}
		int valueFormat1 = u16(data, sub + 4);
		int valueFormat2 = u16(data, sub + 6);
		int size1 = Integer.bitCount(valueFormat1 & 0xFF) * 2;
		int size2 = Integer.bitCount(valueFormat2 & 0xFF) * 2;
		if (format == 1) {
			int pairSetCount = u16(data, sub + 8);
			if (coverageIndex >= pairSetCount) {
				return Integer.MIN_VALUE;
			}
			int pairSet = sub + u16(data, sub + 10 + coverageIndex * 2);
			int count = u16(data, pairSet);
			int recordSize = 2 + size1 + size2;
			int lo = 0;
			int hi = count - 1;
			while (lo <= hi) { // records are sorted by second glyph
				int mid = (lo + hi) >>> 1;
				int record = pairSet + 2 + mid * recordSize;
				int second = u16(data, record);
				if (second < right) {
					lo = mid + 1;
				} else if (second > right) {
					hi = mid - 1;
				} else {
					return xAdvance(record + 2, valueFormat1);
				}
			}
			return Integer.MIN_VALUE;
		}
		if (format == 2) {
			int class1 = classOf(sub + u16(data, sub + 8), left);
			int class2 = classOf(sub + u16(data, sub + 10), right);
			int class1Count = u16(data, sub + 12);
			int class2Count = u16(data, sub + 14);
			if (class1 >= class1Count || class2 >= class2Count) {
				return 0;
			}
			int record = sub + 16 + (class1 * class2Count + class2) * (size1 + size2);
			return xAdvance(record, valueFormat1);
		}
		return Integer.MIN_VALUE;
	}

	private int xAdvance(int valueRecord, int valueFormat) {
		if ((valueFormat & VALUE_X_ADVANCE) == 0) {
			return 0;
		}
		// Fields before XAdvance: XPlacement (0x1), YPlacement (0x2).
		int skip = Integer.bitCount(valueFormat & 0x3) * 2;
		return data.getShort(valueRecord + skip);
	}

	private int coverage(int table, int glyph) {
		int format = u16(data, table);
		if (format == 1) {
			int count = u16(data, table + 2);
			int lo = 0;
			int hi = count - 1;
			while (lo <= hi) {
				int mid = (lo + hi) >>> 1;
				int g = u16(data, table + 4 + mid * 2);
				if (g < glyph) {
					lo = mid + 1;
				} else if (g > glyph) {
					hi = mid - 1;
				} else {
					return mid;
				}
			}
		} else if (format == 2) {
			int count = u16(data, table + 2);
			int lo = 0;
			int hi = count - 1;
			while (lo <= hi) {
				int mid = (lo + hi) >>> 1;
				int range = table + 4 + mid * 6;
				int start = u16(data, range);
				int end = u16(data, range + 2);
				if (end < glyph) {
					lo = mid + 1;
				} else if (start > glyph) {
					hi = mid - 1;
				} else {
					return u16(data, range + 4) + glyph - start;
				}
			}
		}
		return -1;
	}

	private int classOf(int table, int glyph) {
		int format = u16(data, table);
		if (format == 1) {
			int start = u16(data, table + 2);
			int count = u16(data, table + 4);
			int i = glyph - start;
			return i >= 0 && i < count ? u16(data, table + 6 + i * 2) : 0;
		}
		if (format == 2) {
			int count = u16(data, table + 2);
			int lo = 0;
			int hi = count - 1;
			while (lo <= hi) {
				int mid = (lo + hi) >>> 1;
				int range = table + 4 + mid * 6;
				int start = u16(data, range);
				int end = u16(data, range + 2);
				if (end < glyph) {
					lo = mid + 1;
				} else if (start > glyph) {
					hi = mid - 1;
				} else {
					return u16(data, range + 4);
				}
			}
		}
		return 0;
	}

	private static int findTable(ByteBuffer data, int tag) {
		int numTables = u16(data, 4);
		for (int i = 0; i < numTables; i++) {
			int record = 12 + i * 16;
			if (data.getInt(record) == tag) {
				return data.getInt(record + 8);
			}
		}
		return -1;
	}

	private static int u16(ByteBuffer data, int offset) {
		return data.getShort(offset) & 0xFFFF;
	}
}
