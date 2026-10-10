package net.client.visuals.module.player;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.render.DepthOfFieldModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.post.GradingLook;

import java.util.Locale;

/**
 * Photo Mode: a camera in Minecraft. The world can stand still (singleplayer) while a camera floats
 * free of your character with a gimbal's smoothness – a real lens (12–200 mm, depth of field with
 * tap-to-focus and bokeh), exposure, film filters, the time of day, framing guides, a live histogram
 * and a supersampled shutter that saves clean, anti-aliased photos to the screenshots folder.
 * Toggle with its key (Controls → Liquid Client, F4 by default); Tab opens the studio.
 *
 * <p>State only; {@code render.camera.PhotoMode} runs it. Never on at launch.</p>
 */
public final class PhotoModeModule extends Module {
	public static final String[] FOCUS = {"auto", "point", "manual"};
	public static final int FOCUS_AUTO = 0;
	public static final int FOCUS_POINT = 1;
	public static final int FOCUS_MANUAL = 2;
	public static final String[] WEATHER = {"as_is", "clear", "rain", "snow", "thunder"};
	public static final int WEATHER_AS_IS = 0;
	public static final int WEATHER_CLEAR = 1;
	public static final int WEATHER_RAIN = 2;
	public static final int WEATHER_SNOW = 3;
	public static final int WEATHER_THUNDER = 4;
	public static final String[] ASPECT = {"full", "16_9", "3_2", "4_3", "1_1", "21_9", "9_16"};
	/** Width / height of each aspect (0 = the whole screen). */
	public static final float[] ASPECT_RATIOS = {0f, 16f / 9f, 3f / 2f, 4f / 3f, 1f, 21f / 9f, 9f / 16f};
	public static final String[] GRID = {"off", "thirds", "golden", "cross"};
	public static final int GRID_THIRDS = 1;
	public static final int GRID_GOLDEN = 2;
	public static final int GRID_CROSS = 3;
	public static final String[] QUALITY = {"standard", "fine", "ultra", "extreme"};
	/** Frames averaged per photo for each quality (sub-pixel jittered: anti-aliased, noise-free). */
	public static final int[] SAMPLES = {1, 8, 16, 32};

	// Lens and camera
	public final NumberSetting focal = add(new NumberSetting("focal", "liquidclient.photo.focal", 12, 200, 1, 24).suffix(" mm"));
	public final NumberSetting speed = add(new NumberSetting("speed", "liquidclient.photo.speed", 0.5, 20, 0.5, 4).suffix(" blocks/s"));
	public final NumberSetting smoothing = add(new NumberSetting("smoothing", "liquidclient.photo.smoothing", 0, 100, 5, 60).suffix("%"));
	public final NumberSetting range = add(new NumberSetting("range", "liquidclient.photo.range", 4, 64, 1, 32).suffix(" blocks"));

	// Focus and depth of field
	public final BooleanSetting depth = add(new BooleanSetting("depth_of_field", "liquidclient.photo.depth_of_field", true));
	public final NumberSetting aperture = add(new NumberSetting("aperture", "liquidclient.photo.aperture", 1.2, 22, 0.1, 2.8)
			.format(v -> String.format(Locale.ROOT, "f/%.1f", v)));
	public final ModeSetting focusMode = add(new ModeSetting("focus_mode", "liquidclient.photo.focus_mode", FOCUS, FOCUS_AUTO));
	public final NumberSetting focusDistance = add(new NumberSetting("focus_distance", "liquidclient.photo.focus_distance", 0.3, 200, 0.1, 5)
			.suffix(" m"));
	public final ModeSetting bokeh = add(new ModeSetting("bokeh", "liquidclient.photo.bokeh", DepthOfFieldModule.BOKEH, 0));

	// Exposure and colour
	public final NumberSetting exposure = add(new NumberSetting("exposure", "liquidclient.photo.exposure", -3, 3, 0.1, 0)
			.format(PhotoModeModule::ev));
	public final NumberSetting contrast = add(new NumberSetting("contrast", "liquidclient.photo.contrast", 50, 150, 1, 100).suffix("%"));
	public final ModeSetting filter = add(new ModeSetting("filter", "liquidclient.photo.filter", GradingLook.IDS, 0));
	public final NumberSetting filterStrength = add(new NumberSetting("filter_strength", "liquidclient.photo.filter_strength", 0, 100, 1, 80)
			.suffix("%"));
	public final NumberSetting warmth = add(new NumberSetting("warmth", "liquidclient.photo.warmth", -100, 100, 1, 0));
	public final NumberSetting saturation = add(new NumberSetting("saturation", "liquidclient.photo.saturation", 0, 200, 1, 100).suffix("%"));
	public final NumberSetting vignette = add(new NumberSetting("vignette", "liquidclient.photo.vignette", 0, 100, 1, 20).suffix("%"));
	public final NumberSetting grain = add(new NumberSetting("grain", "liquidclient.photo.grain", 0, 100, 1, 0).suffix("%"));

	// Scene
	public final BooleanSetting freeze = add(new BooleanSetting("freeze", "liquidclient.photo.freeze", true));
	public final BooleanSetting customTime = add(new BooleanSetting("custom_time", "liquidclient.photo.custom_time", false));
	public final NumberSetting time = add(new NumberSetting("time", "liquidclient.photo.time", 0, 24, 0.25, 18).format(PhotoModeModule::clock));
	public final ModeSetting weather = add(new ModeSetting("weather", "liquidclient.photo.weather", WEATHER, WEATHER_AS_IS));
	public final BooleanSetting hidePlayer = add(new BooleanSetting("hide_player", "liquidclient.photo.hide_player", false));

	// Framing
	public final ModeSetting aspect = add(new ModeSetting("aspect", "liquidclient.photo.aspect", ASPECT, 0));
	public final ModeSetting grid = add(new ModeSetting("grid", "liquidclient.photo.grid", GRID, GRID_THIRDS));
	public final BooleanSetting level = add(new BooleanSetting("level", "liquidclient.photo.level", true));
	public final BooleanSetting histogram = add(new BooleanSetting("histogram", "liquidclient.photo.histogram", true));

	// Capture
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.photo.quality", QUALITY, 2));
	public final BooleanSetting sounds = add(new BooleanSetting("sounds", "liquidclient.photo.sounds", true));

	public PhotoModeModule() {
		super("photo_mode", Category.PLAYER, 0xE064);
	}

	@Override
	protected void onEnable() {
		PhotoMode.onModuleEnabled();
	}

	@Override
	protected void onDisable() {
		PhotoMode.onModuleDisabled();
	}

	/** Photo Mode starts on demand; it never comes back on by itself at launch. */
	@Override
	public boolean savesState() {
		return false;
	}

	/** Not listed among the enabled modules on the HUD – the camera hides the HUD anyway. */
	@Override
	public boolean listed() {
		return false;
	}

	/** "+0.3 EV", "−1.0 EV", "0.0 EV". */
	public static String ev(double v) {
		double r = Math.round(v * 10.0) / 10.0;
		if (Math.abs(r) < 0.05) {
			return "0.0 EV";
		}
		return (r > 0 ? "+" : "−") + String.format(Locale.ROOT, "%.1f EV", Math.abs(r));
	}

	/** Hours as a 24-hour clock: 18.5 → "18:30". */
	public static String clock(double hours) {
		int minutes = (int) Math.round(hours * 60.0) % (24 * 60);
		return String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60);
	}
}
