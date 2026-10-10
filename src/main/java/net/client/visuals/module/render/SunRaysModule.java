package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Sun Rays: shafts of light streaming from the sun through gaps in clouds, leaves and buildings, a
 * lens flare when the sun itself is in view, and faint moonbeams at night.
 *
 * <p>State only; {@code render.post.PostFx} draws it after the world (before the hand, so the hand
 * covers the rays): a sky mask around the sun, two radial blurs towards it at half resolution, and
 * an additive composite ({@code post_rays.fsh}). Bloom, if on, glows on top of the rays. The
 * Skybox's Radiant Sunbeams mode is made for it.</p>
 */
public final class SunRaysModule extends Module {
	public static final String[] COLORS = {"natural", "golden", "warm", "cool"};
	public static final int COLOR_NATURAL = 0;
	public static final int COLOR_GOLDEN = 1;
	public static final int COLOR_WARM = 2;
	public static final String[] FLARES = {"off", "subtle", "cinematic"};
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};

	public final NumberSetting intensity = add(new NumberSetting("intensity", "liquidclient.sun_rays.intensity", 0, 200, 1, 100)
			.suffix("%"));
	public final NumberSetting length = add(new NumberSetting("length", "liquidclient.sun_rays.length", 20, 100, 1, 70).suffix("%"));
	public final ModeSetting color = add(new ModeSetting("color", "liquidclient.sun_rays.color", COLORS, COLOR_NATURAL));
	public final ModeSetting flare = add(new ModeSetting("flare", "liquidclient.sun_rays.flare", FLARES, 1));
	public final NumberSetting flareStrength = add(new NumberSetting("flare_strength", "liquidclient.sun_rays.flare_strength", 0, 200, 1, 100)
			.suffix("%"));
	public final BooleanSetting moonRays = add(new BooleanSetting("moon_rays", "liquidclient.sun_rays.moon_rays", true));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.sun_rays.quality", QUALITY, 1));

	public SunRaysModule() {
		super("sun_rays", Category.RENDER, 0xE47E);
	}
}
