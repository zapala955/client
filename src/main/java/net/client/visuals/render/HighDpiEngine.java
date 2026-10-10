package net.client.visuals.render;

import net.minecraft.client.Minecraft;

/**
 * GUI-scale aware geometry helpers ("Retina" rendering).
 *
 * <p>Minecraft lays the GUI out in scaled units: at GUI scale 3 one unit is 3×3 device pixels.
 * Two things make GUIs look soft at high scales, and this class fixes both:</p>
 * <ol>
 *   <li><b>Fractional placement.</b> An anti-aliased edge (or a NEAREST-sampled glyph) that lands
 *       between device pixels is smeared over two of them. {@link #snap(float)} rounds a coordinate
 *       to the nearest <i>device</i> pixel – finer than a GUI unit – so smooth motion stays smooth
 *       while every resting edge is razor sharp.</li>
 *   <li><b>Hairlines.</b> A "1px" border must be one device pixel, i.e. {@code 1 / scale} GUI units,
 *       not one GUI unit (which is 4 device pixels at scale 4). See {@link #px()}.</li>
 * </ol>
 *
 * <p>26.3 note: the scale factor is {@code Window#getGuiScale()} (the Yarn-era name was
 * {@code getScaleFactor()}).</p>
 */
public final class HighDpiEngine {
	private HighDpiEngine() {
	}

	/** Current GUI scale (device pixels per GUI unit), at least 1. */
	public static int scale() {
		return Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
	}

	/** One device pixel in GUI units. */
	public static float px() {
		return 1f / scale();
	}

	/** Rounds a GUI coordinate to the nearest device pixel. */
	public static float snap(float v) {
		int s = scale();
		return Math.round(v * s) / (float) s;
	}

	/** Rounds a length to whole device pixels, never below one pixel. */
	public static float snapSize(float v) {
		int s = scale();
		return Math.max(1, Math.round(v * s)) / (float) s;
	}

	/** Physical framebuffer width / height in device pixels. */
	public static int deviceWidth() {
		return Minecraft.getInstance().getWindow().getWidth();
	}

	public static int deviceHeight() {
		return Minecraft.getInstance().getWindow().getHeight();
	}
}
