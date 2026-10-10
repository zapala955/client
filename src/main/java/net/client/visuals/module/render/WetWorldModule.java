package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Wet World: rain soaks the world. Everything the rain reaches darkens and takes on a glossy sheen,
 * puddles gather on flat ground and mirror the world (with rings from raindrops while it rains), the
 * sun glints off it all – then it slowly dries.
 *
 * <p>State only; {@code render.post.WetWorld} tracks how wet the world is and {@code render.post.PostFx}
 * draws it over the opaque world, before anything translucent ({@code post_wet.fsh}). Which surfaces
 * the rain reaches comes from the weather columns shared with Liquid Weather.</p>
 */
public final class WetWorldModule extends Module {
	public final NumberSetting darkening = add(new NumberSetting("darkening", "liquidclient.wet_world.darkening", 0, 100, 5, 70)
			.suffix("%"));
	public final NumberSetting puddles = add(new NumberSetting("puddles", "liquidclient.wet_world.puddles", 0, 200, 5, 100)
			.suffix("%"));
	public final BooleanSetting reflections = add(new BooleanSetting("reflections", "liquidclient.wet_world.reflections", true));
	public final BooleanSetting ripples = add(new BooleanSetting("ripples", "liquidclient.wet_world.ripples", true));
	public final NumberSetting dryingTime = add(new NumberSetting("drying_time", "liquidclient.wet_world.drying_time", 30, 600, 10, 180)
			.suffix(" s"));
	public final BooleanSetting alwaysWet = add(new BooleanSetting("always_wet", "liquidclient.wet_world.always_wet", false));

	public WetWorldModule() {
		super("wet_world", Category.RENDER, 0xE55A);
	}
}
