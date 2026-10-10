package net.client.visuals.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.minecraft.world.InteractionHand;

/**
 * View Model: where the first-person hands sit and how big they are (main and off hand
 * separately), how fast the swing animation plays, and whether switching items dips the hand.
 *
 * <p>Hooks ({@code FirstPersonHandsMixin}): offsets are applied right after vanilla pushes the
 * hand's pose (so they move arms, maps and items alike, mirrored for the left arm); scale is applied
 * after vanilla positions a held item, so the item shrinks in place instead of towards the camera.
 * Swing speed only changes the local player's animation length ({@code LivingEntitySwingMixin}) –
 * 26.3 has no swing packet, so it's purely visual.</p>
 */
public final class ViewModelModule extends Module {
	private static ViewModelModule instance;

	public final NumberSetting mainX = add(offset("main_x"));
	public final NumberSetting mainY = add(offset("main_y"));
	public final NumberSetting mainZ = add(offset("main_z"));
	public final NumberSetting mainScale = add(scale("main_scale"));
	public final NumberSetting offX = add(offset("off_x"));
	public final NumberSetting offY = add(offset("off_y"));
	public final NumberSetting offZ = add(offset("off_z"));
	public final NumberSetting offScale = add(scale("off_scale"));
	public final NumberSetting swingSpeed = add(new NumberSetting("swing_speed", "liquidclient.view_model.swing_speed", 25, 250, 5, 100)
			.suffix("%"));
	public final BooleanSetting noEquip = add(new BooleanSetting("no_equip", "liquidclient.view_model.no_equip", false));

	public ViewModelModule() {
		super("view_model", Category.RENDER, 0xE1D7);
		instance = this;
	}

	private static NumberSetting offset(String id) {
		return new NumberSetting(id, "liquidclient.view_model." + id, -1, 1, 0.02, 0);
	}

	private static NumberSetting scale(String id) {
		return new NumberSetting(id, "liquidclient.view_model." + id, 30, 160, 5, 100).suffix("%");
	}

	private static boolean active() {
		return instance != null && instance.isEnabled();
	}

	/** Moves the hand; {@code invert} is +1 for the right arm, -1 for the left (vanilla's sign). */
	public static void offset(PoseStack poseStack, InteractionHand hand, int invert) {
		if (!active()) {
			return;
		}
		ViewModelModule m = instance;
		boolean main = hand == InteractionHand.MAIN_HAND;
		float x = (float) (main ? m.mainX : m.offX).get();
		float y = (float) (main ? m.mainY : m.offY).get();
		float z = (float) (main ? m.mainZ : m.offZ).get();
		if (x != 0f || y != 0f || z != 0f) {
			poseStack.translate(invert * x, y, z);
		}
	}

	/** Scales a held item in place. */
	public static void scale(PoseStack poseStack, boolean mainHand) {
		if (!active()) {
			return;
		}
		float s = (float) (mainHand ? instance.mainScale : instance.offScale).get() / 100f;
		if (s != 1f) {
			poseStack.scale(s, s, s);
		}
	}

	/** Local player's swing length in ticks. */
	public static int swingDuration(int original) {
		if (!active()) {
			return original;
		}
		double speed = instance.swingSpeed.get() / 100.0;
		return Math.max(1, (int) Math.round(original / speed));
	}

	/** The re-equip dip of the hands (0 = hand fully up). */
	public static float equipHeight(float original) {
		return active() && instance.noEquip.get() ? 0f : original;
	}
}
