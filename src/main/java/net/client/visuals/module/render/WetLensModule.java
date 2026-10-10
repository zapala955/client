package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Wet Lens: water on the camera. Rain beads on the lens – every bead a tiny lens with the world
 * upside-down inside it – and bigger drops run down leaving trails; climbing out of water leaves
 * the lens streaming until it drains; snow frosts it from the edges.
 *
 * <p>State only; {@code render.post.WetLens} follows the weather where you stand (open sky, rain or
 * snow there, looking up gets wetter) and {@code render.post.PostFx} draws it over the finished
 * frame ({@code post_lens.fsh}), before Bloom so the drops catch the light.</p>
 */
public final class WetLensModule extends Module {
	public final BooleanSetting raindrops = add(new BooleanSetting("raindrops", "liquidclient.wet_lens.raindrops", true));
	public final BooleanSetting drench = add(new BooleanSetting("drench", "liquidclient.wet_lens.drench", true));
	public final BooleanSetting frost = add(new BooleanSetting("frost", "liquidclient.wet_lens.frost", true));
	public final NumberSetting amount = add(new NumberSetting("amount", "liquidclient.wet_lens.amount", 25, 200, 5, 100).suffix("%"));
	public final NumberSetting dropSize = add(new NumberSetting("drop_size", "liquidclient.wet_lens.drop_size", 50, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting refraction = add(new NumberSetting("refraction", "liquidclient.wet_lens.refraction", 0, 200, 5, 100)
			.suffix("%"));
	public final BooleanSetting alwaysWet = add(new BooleanSetting("always_wet", "liquidclient.wet_lens.always_wet", false));

	public WetLensModule() {
		super("wet_lens", Category.RENDER, 0xE0B4);
	}
}
