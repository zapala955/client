package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Bloom: bright light – the sun, lava, torches, glowing particles – bleeds softly into its
 * surroundings, with an optional anamorphic (horizontal streak) lens.
 *
 * <p>State only; {@code render.post.PostFx} builds a half-float mip chain ({@code post_bloom.fsh})
 * and the grade pass adds it to the frame, so it works with or without Color Grading.</p>
 */
public final class BloomModule extends Module {
	public final NumberSetting intensity = add(new NumberSetting("intensity", "liquidclient.bloom.intensity", 0, 200, 1, 70)
			.suffix("%"));
	public final NumberSetting threshold = add(new NumberSetting("threshold", "liquidclient.bloom.threshold", 0, 100, 1, 72)
			.suffix("%"));
	public final NumberSetting softness = add(new NumberSetting("softness", "liquidclient.bloom.softness", 0, 100, 1, 50)
			.suffix("%"));
	public final NumberSetting radius = add(new NumberSetting("radius", "liquidclient.bloom.radius", 0, 100, 1, 65)
			.suffix("%"));
	public final BooleanSetting anamorphic = add(new BooleanSetting("anamorphic", "liquidclient.bloom.anamorphic", false));

	public BloomModule() {
		super("bloom", Category.RENDER, 0xE178);
	}
}
