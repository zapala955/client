package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Liquid Water: real water instead of vanilla's flat sheets, in the overworld. Layered waves driven
 * by the wind (Liquid Weather's, a storm's), glassy on calm ponds, flowing down slopes and falls;
 * reflections of the world and of the sky as it's drawn (Skybox modes, the storm); refraction;
 * colour and clarity by depth (turquoise shallows, deep blue, murky swamps – the biome's water colour
 * respected); foam at shores, around things in the water and under falls; caustics on the bed; rain
 * rings; sun and moon glints. Under water: caustics, light shafts, colour absorbed with depth, and
 * the surface overhead with Snell's window. Client-side only.
 *
 * <p>State only. The terrain shaders (Sodium's or vanilla's) tag water while they draw it
 * ({@code render.water.WaterShaderPatch}, {@code WaterTag}); {@code render.water.LiquidWater} shades
 * the tagged pixels right after translucent terrain ({@code PostFx}). Steps aside with an Iris shader
 * pack, and with Improved Transparency (water then stays vanilla's).</p>
 */
public final class LiquidWaterModule extends Module {
	public static final String[] REFLECTIONS = {"off", "sky", "screen"};
	public static final int REFLECTIONS_OFF = 0;
	public static final int REFLECTIONS_SKY = 1;
	public static final int REFLECTIONS_SCREEN = 2;
	public static final String[] STYLE = {"natural", "tropical", "deep_ocean"};
	public static final int STYLE_NATURAL = 0;
	public static final int STYLE_TROPICAL = 1;
	public static final int STYLE_DEEP_OCEAN = 2;
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};

	public final NumberSetting waves = add(new NumberSetting("waves", "liquidclient.water.waves", 0, 200, 5, 100).suffix("%"));
	public final ModeSetting reflections = add(new ModeSetting("reflections", "liquidclient.water.reflections", REFLECTIONS, REFLECTIONS_SCREEN));
	public final NumberSetting refraction = add(new NumberSetting("refraction", "liquidclient.water.refraction", 0, 200, 5, 100).suffix("%"));
	public final BooleanSetting foam = add(new BooleanSetting("foam", "liquidclient.water.foam", true));
	public final BooleanSetting caustics = add(new BooleanSetting("caustics", "liquidclient.water.caustics", true));
	public final BooleanSetting rainRipples = add(new BooleanSetting("rain_ripples", "liquidclient.water.rain_ripples", true));
	public final NumberSetting clarity = add(new NumberSetting("clarity", "liquidclient.water.clarity", 25, 300, 5, 100).suffix("%"));
	public final ModeSetting style = add(new ModeSetting("style", "liquidclient.water.style", STYLE, STYLE_NATURAL));
	public final BooleanSetting underwater = add(new BooleanSetting("underwater", "liquidclient.water.underwater", true));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.water.quality", QUALITY, 1));

	public LiquidWaterModule() {
		super("liquid_water", Category.RENDER, 0xE0B4);
	}
}
