package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.storm.Storm;

/**
 * Storm Chaser: on demand, a cinematic thunderstorm rolls in over you. A dark storm front with a
 * shelf cloud builds on the horizon, the wind rises and the light turns cold, heavy rain sweeps in
 * in sheets, lightning peaks – branching bolts with real return-stroke flicker, cloud-to-cloud
 * flashes lighting the deck from inside, every flash relighting the world, thunder arriving at the
 * speed of sound – then the storm passes with mammatus overhead and clears into sun rays and a
 * rainbow. Client-side only: it changes what you see and hear, never gameplay.
 *
 * <p>State only. {@code render.storm.Storm} runs the storm's life (and feeds Ambience's rain and
 * thunder, Liquid Weather, Wet World, Wet Lens, Waving Plants and Atmosphere), {@code StormSky}
 * draws it in the sky pass, {@code Lightning} and {@code StormRenderer} the bolts and flashes,
 * {@code StormSounds} the thunder, rain and wind.</p>
 *
 * <p>Triggers: on demand (switching it on – its key, or Controls → Liquid Client – brings a storm
 * in, switching it off sends it away, and it switches itself off once the sky has cleared), follow
 * the server's thunderstorms, or always.</p>
 */
public final class StormChaserModule extends Module {
	public static final String[] TRIGGER = {"on_demand", "server", "always"};
	public static final int TRIGGER_ON_DEMAND = 0;
	public static final int TRIGGER_SERVER = 1;
	public static final int TRIGGER_ALWAYS = 2;
	public static final String[] BOLT_STYLE = {"natural", "cinematic", "forked", "neon"};
	public static final int BOLT_NATURAL = 0;
	public static final int BOLT_CINEMATIC = 1;
	public static final int BOLT_FORKED = 2;
	public static final int BOLT_NEON = 3;
	public static final String[] TINT = {"natural", "green", "purple"};
	public static final int TINT_GREEN = 1;
	public static final int TINT_PURPLE = 2;
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};
	/** Resolution of the storm's own render textures (the sky, the flash relighting). */
	public static final String[] RESOLUTION = {"half", "full"};
	public static final int RESOLUTION_FULL = 1;

	public final ModeSetting trigger = add(new ModeSetting("trigger", "liquidclient.storm.trigger", TRIGGER, TRIGGER_ON_DEMAND));
	public final NumberSetting intensity = add(new NumberSetting("intensity", "liquidclient.storm.intensity", 25, 150, 5, 100).suffix("%"));
	public final NumberSetting buildUp = add(new NumberSetting("build_up", "liquidclient.storm.build_up", 30, 240, 5, 90).suffix(" s"));
	public final NumberSetting duration = add(new NumberSetting("duration", "liquidclient.storm.duration", 60, 900, 15, 240).suffix(" s"));
	public final NumberSetting frequency = add(new NumberSetting("frequency", "liquidclient.storm.frequency", 0, 300, 5, 100).suffix("%"));
	public final NumberSetting brightness = add(new NumberSetting("brightness", "liquidclient.storm.brightness", 25, 200, 5, 100).suffix("%"));
	public final ModeSetting boltStyle = add(new ModeSetting("bolt_style", "liquidclient.storm.bolt_style", BOLT_STYLE, BOLT_NATURAL));
	public final BooleanSetting relight = add(new BooleanSetting("relight", "liquidclient.storm.relight", true));
	public final NumberSetting thunderVolume = add(new NumberSetting("thunder_volume", "liquidclient.storm.thunder_volume", 0, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting wind = add(new NumberSetting("wind", "liquidclient.storm.wind", 0, 200, 5, 100).suffix("%"));
	public final ModeSetting tint = add(new ModeSetting("tint", "liquidclient.storm.tint", TINT, 0));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.storm.quality", QUALITY, 1));
	/** Half by default (the GPU budget); full renders the sky and the relighting at the frame's own resolution. */
	public final ModeSetting resolution = add(new ModeSetting("resolution", "liquidclient.storm.resolution", RESOLUTION, 0));

	public StormChaserModule() {
		super("storm_chaser", Category.RENDER, 0xE08C);
	}

	/** 2 = half resolution, 1 = full: the divisor for the storm's render textures. */
	public int resolutionDivisor() {
		return resolution.index() == RESOLUTION_FULL ? 1 : 2;
	}

	@Override
	protected void onEnable() {
		Storm.onModuleEnabled();
	}

	@Override
	protected void onDisable() {
		Storm.onModuleDisabled();
	}

	/** A storm on demand doesn't come back by itself at launch; following the server or always does. */
	@Override
	public boolean savesState() {
		return trigger.index() != TRIGGER_ON_DEMAND;
	}
}
