package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Ambient Occlusion: soft contact shadows where light can't reach – in corners, where blocks meet,
 * under leaves and overhangs, around mobs' feet. Horizon-based (HBAO) from the depth buffer, denoised
 * with a depth-aware blur so edges stay crisp, and applied to the opaque world only (water, glass,
 * particles, weather and your hand are untouched).
 *
 * <p>State only; {@code render.post.PostFx} draws it ({@code post_ao.fsh}).</p>
 */
public final class AmbientOcclusionModule extends Module {
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};

	public final NumberSetting strength = add(new NumberSetting("strength", "liquidclient.ambient_occlusion.strength", 0, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting radius = add(new NumberSetting("radius", "liquidclient.ambient_occlusion.radius", 0.5, 4, 0.1, 1.5)
			.suffix(" blocks"));
	public final NumberSetting contrast = add(new NumberSetting("contrast", "liquidclient.ambient_occlusion.contrast", 100, 300, 5, 150)
			.suffix("%"));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.ambient_occlusion.quality", QUALITY, 1));
	public final BooleanSetting preview = add(new BooleanSetting("preview", "liquidclient.ambient_occlusion.preview", false));

	public AmbientOcclusionModule() {
		super("ambient_occlusion", Category.RENDER, 0xE129);
	}
}
