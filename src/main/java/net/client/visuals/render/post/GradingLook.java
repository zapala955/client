package net.client.visuals.render.post;

/**
 * The Color Grading module's looks: lift / gamma / gain, contrast, saturation, monochrome and split
 * toning (a shadow and a highlight tone overlaid by luminance), applied in display space by
 * {@code post_grade.fsh}. Ids are stable (saved in the config); the index is the mode.
 */
public final class GradingLook {
	public static final String[] IDS = {
			"none", "cinematic", "golden_hour", "moody", "noir", "pastel", "vintage", "cyberpunk", "arctic",
			"vivid", "sepia", "matrix", "dream"
	};

	/** Per look: lift rgb, gamma rgb, gain rgb, contrast, saturation, monochrome, shadow tone rgb, highlight tone rgb, split strength. */
	private static final float[][] LOOKS = {
			// none
			{0f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0f},
			// cinematic: teal shadows, warm skin-tone highlights
			{0f, 0.02f, 0.05f, 1f, 1f, 1f, 1.06f, 1f, 0.93f, 1.12f, 1.08f, 0f, 0.1f, 0.42f, 0.5f, 0.95f, 0.62f, 0.38f, 0.32f},
			// golden hour: warm, glowing
			{0.03f, 0.015f, 0f, 1.05f, 1f, 0.95f, 1.1f, 1.01f, 0.86f, 1.06f, 1.12f, 0f, 0.42f, 0.32f, 0.22f, 1f, 0.76f, 0.42f, 0.28f},
			// moody: cool, desaturated, deep
			{0f, 0.01f, 0.035f, 0.95f, 0.97f, 1f, 0.95f, 0.98f, 1.03f, 1.2f, 0.72f, 0f, 0.2f, 0.32f, 0.48f, 0.62f, 0.62f, 0.66f, 0.25f},
			// noir
			{0f, 0f, 0f, 0.95f, 0.95f, 0.95f, 1.04f, 1.04f, 1.04f, 1.35f, 1f, 1f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0f},
			// pastel: lifted blacks, soft contrast
			{0.07f, 0.06f, 0.08f, 1.05f, 1.05f, 1.05f, 1f, 0.97f, 1.02f, 0.85f, 0.86f, 0f, 0.55f, 0.5f, 0.62f, 0.98f, 0.78f, 0.86f, 0.2f},
			// vintage: faded film
			{0.07f, 0.055f, 0.035f, 1f, 1f, 0.95f, 0.97f, 0.94f, 0.84f, 0.9f, 0.78f, 0f, 0.22f, 0.36f, 0.36f, 1f, 0.82f, 0.58f, 0.26f},
			// cyberpunk: magenta shadows, cyan highlights
			{0.02f, 0f, 0.05f, 1f, 1f, 1f, 1.04f, 1f, 1.06f, 1.16f, 1.25f, 0f, 0.42f, 0.12f, 0.62f, 0.32f, 0.9f, 1f, 0.45f},
			// arctic: cold and clean
			{0f, 0.02f, 0.05f, 1f, 1.02f, 1.05f, 0.9f, 0.97f, 1.08f, 1.05f, 0.85f, 0f, 0.32f, 0.45f, 0.6f, 0.82f, 0.92f, 1f, 0.2f},
			// vivid
			{0f, 0f, 0f, 1f, 1f, 1f, 1.03f, 1.03f, 1.03f, 1.15f, 1.38f, 0f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0f},
			// sepia
			{0.03f, 0.02f, 0f, 1f, 1f, 1f, 1f, 0.97f, 0.9f, 1.05f, 1f, 0.9f, 0.38f, 0.24f, 0.12f, 1f, 0.84f, 0.6f, 0.62f},
			// matrix: green-tinted, crushed
			{0f, 0.03f, 0f, 1f, 1.05f, 1f, 0.86f, 1.08f, 0.86f, 1.22f, 0.7f, 0f, 0.1f, 0.4f, 0.15f, 0.6f, 1f, 0.6f, 0.3f},
			// dream: soft, pink-lit
			{0.04f, 0.02f, 0.06f, 1.05f, 1f, 1.05f, 1.05f, 1f, 1.06f, 0.9f, 1.05f, 0f, 0.45f, 0.4f, 0.65f, 1f, 0.72f, 0.9f, 0.3f},
	};

	private GradingLook() {
	}

	/** The look's 19 parameters (see {@link #LOOKS}). */
	static float[] get(int index) {
		return LOOKS[Math.floorMod(index, LOOKS.length)];
	}
}
