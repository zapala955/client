package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.post.GradingLook;

import java.util.Locale;

/**
 * Color Grading: a film look for the whole frame (world and hand, never the GUI) – 13 looks plus
 * exposure, white balance, a tone curve, contrast / saturation / vibrance, shadows and highlights,
 * vignette, film grain, chromatic aberration and sharpening.
 *
 * <p>State only; drawn by {@code render.post.PostFx} in one full-screen pass
 * ({@code post_grade.fsh}), which also composites the Bloom module.</p>
 */
public final class ColorGradingModule extends Module {
	public static final String[] TONE_CURVES = {"off", "filmic", "soft"};

	public final ModeSetting look = add(new ModeSetting("look", "liquidclient.grading.look", GradingLook.IDS, 1));
	public final NumberSetting lookStrength = add(new NumberSetting("look_strength", "liquidclient.grading.look_strength", 0, 100, 1, 70)
			.suffix("%"));
	public final NumberSetting exposure = add(new NumberSetting("exposure", "liquidclient.grading.exposure", -200, 200, 5, 0)
			.format(ColorGradingModule::ev));
	public final ModeSetting toneCurve = add(new ModeSetting("tone_curve", "liquidclient.grading.tone_curve", TONE_CURVES, 1));
	public final NumberSetting contrast = add(new NumberSetting("contrast", "liquidclient.grading.contrast", 50, 150, 1, 100)
			.suffix("%"));
	public final NumberSetting saturation = add(new NumberSetting("saturation", "liquidclient.grading.saturation", 0, 200, 1, 105)
			.suffix("%"));
	public final NumberSetting vibrance = add(new NumberSetting("vibrance", "liquidclient.grading.vibrance", 0, 100, 1, 20)
			.suffix("%"));
	public final NumberSetting temperature = add(new NumberSetting("temperature", "liquidclient.grading.temperature", -100, 100, 1, 0));
	public final NumberSetting tint = add(new NumberSetting("tint", "liquidclient.grading.tint", -100, 100, 1, 0));
	public final NumberSetting shadows = add(new NumberSetting("shadows", "liquidclient.grading.shadows", -100, 100, 1, 0));
	public final NumberSetting highlights = add(new NumberSetting("highlights", "liquidclient.grading.highlights", -100, 100, 1, 0));
	public final NumberSetting vignette = add(new NumberSetting("vignette", "liquidclient.grading.vignette", 0, 100, 1, 30)
			.suffix("%"));
	public final NumberSetting grain = add(new NumberSetting("grain", "liquidclient.grading.grain", 0, 100, 1, 10)
			.suffix("%"));
	public final NumberSetting aberration = add(new NumberSetting("aberration", "liquidclient.grading.aberration", 0, 100, 1, 0)
			.suffix("%"));
	public final NumberSetting sharpen = add(new NumberSetting("sharpen", "liquidclient.grading.sharpen", 0, 100, 1, 20)
			.suffix("%"));

	public ColorGradingModule() {
		super("color_grading", Category.RENDER, 0xE1DD);
	}

	private static String ev(double hundredths) {
		return String.format(Locale.ROOT, "%+.2f EV", hundredths / 100.0);
	}
}
