package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Rainbows: when rain clears and the sun comes out, a rainbow arcs across the sky opposite the sun –
 * physically placed (42° around the antisolar point), with its double bow, Alexander's dark band,
 * supernumerary bands, warm sunset bows and pale moonbows at night.
 *
 * <p>State only; {@code render.post.Rainbow} decides when one appears and {@code render.post.PostFx}
 * draws it after the world ({@code post_rainbow.fsh}).</p>
 */
public final class RainbowsModule extends Module {
	public static final String[] WHEN = {"after_rain", "rain_and_after", "always"};
	public static final int WHEN_AFTER_RAIN = 0;
	public static final int WHEN_RAIN_AND_AFTER = 1;
	public static final int WHEN_ALWAYS = 2;

	public final ModeSetting when = add(new ModeSetting("when", "liquidclient.rainbows.when", WHEN, WHEN_AFTER_RAIN));
	public final NumberSetting intensity = add(new NumberSetting("intensity", "liquidclient.rainbows.intensity", 10, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting duration = add(new NumberSetting("duration", "liquidclient.rainbows.duration", 30, 600, 10, 180)
			.suffix(" s"));
	public final BooleanSetting doubleBow = add(new BooleanSetting("double", "liquidclient.rainbows.double", true));
	public final BooleanSetting bands = add(new BooleanSetting("bands", "liquidclient.rainbows.bands", true));
	public final BooleanSetting moonbows = add(new BooleanSetting("moonbows", "liquidclient.rainbows.moonbows", true));

	public RainbowsModule() {
		super("rainbows", Category.RENDER, 0xE10A);
	}
}
