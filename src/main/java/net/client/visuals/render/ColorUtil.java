package net.client.visuals.render;

/** Allocation-free ARGB helpers. All inputs/outputs are packed 0xAARRGGBB ints. */
public final class ColorUtil {
	private ColorUtil() {
	}

	public static int alpha(int argb) {
		return argb >>> 24;
	}

	/** Multiplies the color's existing alpha by {@code factor} (0..1). */
	public static int fade(int argb, float factor) {
		int a = Math.round(alpha(argb) * clamp01(factor));
		return (a << 24) | (argb & 0x00FFFFFF);
	}

	/** Replaces alpha outright. */
	public static int withAlpha(int argb, int alpha) {
		return (Math.clamp(alpha, 0, 255) << 24) | (argb & 0x00FFFFFF);
	}

	/** Per-channel linear interpolation (including alpha). */
	public static int lerp(int from, int to, float t) {
		t = clamp01(t);
		int a = lerpChannel(from >>> 24, to >>> 24, t);
		int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
		int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
		int b = lerpChannel(from & 0xFF, to & 0xFF, t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/** Interpolates RGB only; keeps {@code from}'s alpha. */
	public static int lerpRgb(int from, int to, float t) {
		return (lerp(from, to, t) & 0x00FFFFFF) | (from & 0xFF000000);
	}

	/** HSV → opaque ARGB; {@code hue} in degrees (any range), saturation / value 0..1. */
	public static int hsv(float hue, float saturation, float value) {
		float h = ((hue % 360f) + 360f) % 360f / 60f;
		float s = clamp01(saturation);
		float v = clamp01(value);
		int sector = (int) h;
		float f = h - sector;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r;
		float g;
		float b;
		switch (sector) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return 0xFF000000 | Math.round(r * 255f) << 16 | Math.round(g * 255f) << 8 | Math.round(b * 255f);
	}

	private static int lerpChannel(int a, int b, float t) {
		return a + Math.round((b - a) * t);
	}

	private static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
}
