package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.chunk.FoliageWaving;
import net.minecraft.client.Minecraft;

/**
 * Waving Plants: leaves, vines, grass, flowers and crops sway in the wind – gust fronts roll across
 * fields in the wind's direction, plants lean and bob from fixed roots, two-block plants bend along
 * their whole height, and canopies shimmer without drifting off their trunks.
 *
 * <p>State only. Runs in the terrain vertex shader (Sodium's; see {@code render.chunk.FoliageWaving}
 * and {@code foliage_wave.glsl}), so it costs no CPU time and collisions are untouched. Which blocks
 * sway is decided while chunks are meshed, so changing those switches rebuilds the chunks once; the
 * wind itself is live.</p>
 */
public final class WavingPlantsModule extends Module {
	/** Wind levels 1..7 (the shader's level is index + 1). */
	public static final String[] WIND = {"calm", "light", "gentle", "moderate", "fresh", "strong", "gale"};
	/** "particles" follows the Particles module's wind direction; then 8 compass points. */
	public static final String[] DIRECTIONS = {"particles", "n", "ne", "e", "se", "s", "sw", "w", "nw"};

	public final ModeSetting wind = add(new ModeSetting("wind", "liquidclient.waving.wind", WIND, 2));
	public final ModeSetting direction = add(new ModeSetting("direction", "liquidclient.waving.direction", DIRECTIONS, 0));
	public final BooleanSetting gusts = add(new BooleanSetting("gusts", "liquidclient.waving.gusts", true));
	public final BooleanSetting leaves = add(new BooleanSetting("leaves", "liquidclient.waving.leaves", true));
	public final BooleanSetting vines = add(new BooleanSetting("vines", "liquidclient.waving.vines", true));
	public final BooleanSetting plants = add(new BooleanSetting("plants", "liquidclient.waving.plants", true));
	public final BooleanSetting crops = add(new BooleanSetting("crops", "liquidclient.waving.crops", true));

	public WavingPlantsModule() {
		super("waving_plants", Category.RENDER, 0xE2F4);
	}

	@Override
	public void clientTick(Minecraft minecraft) {
		FoliageWaving.sync(this, minecraft);
	}
}
