package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Motion Blur: cinematic camera motion blur. Turning, walking, flying, zooming and Showcase shots
 * smear the world along its motion on screen, like a film camera's shutter; your hand stays sharp.
 * Frame-rate independent (the shutter is set for 60 FPS), capped, and skipped on teleports.
 *
 * <p>State only; {@code render.post.PostFx} draws it after the world ({@code post_motion.fsh}) from a
 * per-pixel reprojection with last frame's camera.</p>
 */
public final class MotionBlurModule extends Module {
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};

	public final NumberSetting strength = add(new NumberSetting("strength", "liquidclient.motion_blur.strength", 10, 200, 5, 50)
			.suffix("%"));
	public final BooleanSetting movement = add(new BooleanSetting("movement", "liquidclient.motion_blur.movement", true));
	public final NumberSetting maxBlur = add(new NumberSetting("max_blur", "liquidclient.motion_blur.max_blur", 1, 15, 0.5, 6)
			.suffix("%"));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.motion_blur.quality", QUALITY, 1));

	public MotionBlurModule() {
		super("motion_blur", Category.RENDER, 0xE145);
	}
}
