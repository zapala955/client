package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

import java.util.Locale;

/**
 * Atmosphere: depth for the landscape – distant terrain fades into a soft haze (aerial
 * perspective), mist pools in valleys below a chosen height while peaks rise out of it, and the air
 * glows towards the sun.
 *
 * <p>State only; {@code render.post.PostFx#afterLevel} draws it from the world's depth buffer after
 * the world and before the hand ({@code post_haze.fsh}). Off underwater and in lava, where vanilla's
 * own fog takes over.</p>
 */
public final class AtmosphereModule extends Module {
	public static final String[] COLORS = {"sky", "warm", "cool", "custom"};
	public static final int COLOR_WARM = 1;
	public static final int COLOR_COOL = 2;
	public static final int COLOR_CUSTOM = 3;

	public final NumberSetting haze = add(new NumberSetting("haze", "liquidclient.atmosphere.haze", 0, 100, 1, 35)
			.suffix("%"));
	public final NumberSetting hazeStart = add(new NumberSetting("haze_start", "liquidclient.atmosphere.haze_start", 0, 64, 1, 6)
			.suffix(" blocks"));
	public final NumberSetting mist = add(new NumberSetting("mist", "liquidclient.atmosphere.mist", 0, 100, 1, 40)
			.suffix("%"));
	public final NumberSetting mistHeight = add(new NumberSetting("mist_height", "liquidclient.atmosphere.mist_height", -64, 320, 1, 68)
			.format(v -> String.format(Locale.ROOT, "Y %d", Math.round(v))));
	public final NumberSetting mistThickness = add(new NumberSetting("mist_thickness", "liquidclient.atmosphere.mist_thickness", 2, 48, 1, 12)
			.suffix(" blocks"));
	public final NumberSetting sunGlow = add(new NumberSetting("sun_glow", "liquidclient.atmosphere.sun_glow", 0, 100, 1, 55)
			.suffix("%"));
	public final ModeSetting color = add(new ModeSetting("color", "liquidclient.atmosphere.color", COLORS, 0));
	public final NumberSetting hue = add(new NumberSetting("hue", "liquidclient.atmosphere.hue", 0, 360, 1, 210)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting density = add(new NumberSetting("max_density", "liquidclient.atmosphere.max_density", 10, 100, 1, 85)
			.suffix("%"));

	public AtmosphereModule() {
		super("atmosphere", Category.RENDER, 0xE231);
	}
}
