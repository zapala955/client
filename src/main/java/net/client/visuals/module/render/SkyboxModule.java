package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Skybox: fully replaces the overworld sky (gradient, sun, moon, stars – everything vanilla draws
 * in the sky pass) with one of 16 shader skies or a 4K galaxy texture, with optional cloud and star
 * pattern layers on top. Terrain fog can follow the sky's horizon colour so distant terrain melts
 * into the custom sky instead of vanilla blue, and vanilla clouds can be hidden.
 *
 * <p>This class only holds state. Rendering lives in {@code render.sky.LiquidSky}
 * ({@code SkyRendererMixin}, {@code FogRendererMixin}, {@code CloudRendererMixin}). Toggling only
 * flips {@link #isEnabled()}: the renderer cross-fades to / from vanilla and releases its GPU
 * resources once fully faded out.</p>
 */
public final class SkyboxModule extends Module {
	/** Stable mode ids; their index is the shader's {@code mode} value. */
	public static final String[] MODES = {
			"galaxy", "procedural", "pastel", "fog", "aurora", "nebula", "synthwave", "blackhole",
			"eclipse", "golden", "storm", "candy", "matrix", "void", "prism", "ocean", "radiant", "silver"
	};
	public static final int MODE_GALAXY = 0;
	public static final int MODE_FOG = 3;

	public static final String[] CLOUDS = {"none", "wispy", "puffy", "streaks", "swirl", "cellular", "ripples"};
	public static final String[] STARS = {"dots", "sparkle", "shooting", "rainbow"};

	public final ModeSetting mode = add(new ModeSetting("mode", "liquidclient.skybox.mode", MODES, 1));
	public final ModeSetting clouds = add(new ModeSetting("clouds", "liquidclient.skybox.clouds", CLOUDS, 0));
	public final ModeSetting stars = add(new ModeSetting("stars", "liquidclient.skybox.stars", STARS, 1));
	public final NumberSetting tintHue = add(new NumberSetting("tint_hue", "liquidclient.skybox.tint_hue", 0, 360, 1, 215)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting tintStrength = add(new NumberSetting("tint_strength", "liquidclient.skybox.tint_strength", 0, 100, 1, 0)
			.suffix("%"));
	public final NumberSetting rotationSpeed = add(new NumberSetting("rotation_speed", "liquidclient.skybox.rotation_speed", 0, 5, 0.05, 0.5)
			.suffix("×"));
	public final NumberSetting animationSpeed = add(new NumberSetting("animation_speed", "liquidclient.skybox.animation_speed", 0, 3, 0.05, 1)
			.suffix("×"));
	public final NumberSetting starDensity = add(new NumberSetting("star_density", "liquidclient.skybox.star_density", 0, 100, 1, 55)
			.suffix("%"));
	public final NumberSetting cloudCoverage = add(new NumberSetting("cloud_coverage", "liquidclient.skybox.cloud_coverage", 0, 100, 1, 50)
			.suffix("%"));
	public final NumberSetting brightness = add(new NumberSetting("brightness", "liquidclient.skybox.brightness", 20, 200, 1, 100)
			.suffix("%"));
	public final NumberSetting horizonBlend = add(new NumberSetting("horizon_blend", "liquidclient.skybox.horizon_blend", 0, 100, 1, 35)
			.suffix("%"));
	public final BooleanSetting syncFog = add(new BooleanSetting("sync_fog", "liquidclient.skybox.sync_fog", true));
	public final BooleanSetting hideClouds = add(new BooleanSetting("hide_clouds", "liquidclient.skybox.hide_clouds", false));

	public SkyboxModule() {
		super("skybox", Category.RENDER);
	}
}
