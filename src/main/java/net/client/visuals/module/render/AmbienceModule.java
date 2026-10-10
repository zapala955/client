package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.storm.Storm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Ambience: a client-side time of day, weather and brightness, independent of the server.
 *
 * <ul>
 *   <li><b>Time</b> – 26.x drives the sun, sky colours and daylight from world clocks; the clock
 *       instances the client reads are offset to the chosen time of day (the day count is kept, so
 *       moon phases still advance). See {@code ClientClockInstanceMixin}.</li>
 *   <li><b>Weather</b> – rain / thunder levels the client sees ({@code LevelWeatherMixin}), eased
 *       like vanilla's own weather transitions; Snow also makes it snow everywhere.</li>
 *   <li><b>Liquid Weather</b> – rain and snow replaced by {@code render.weather.LiquidWeather}:
 *       motion-blurred streaks, drifting and tumbling snowflakes, splashes and ripples, wind, and
 *       rain / snow stopping on roofs, trees and water. Precipitation can follow the biome or be
 *       forced to rain or snow ({@code ClientLevelWeatherMixin}).</li>
 *   <li><b>Fullbright</b> – night-vision lighting in the lightmap ({@code LightmapMixin}).</li>
 *   <li><b>Storm Chaser</b> – a storm raises the rain and thunder levels to its own where you are
 *       ({@code render.storm.Storm}), rains where the biome wouldn't, and draws its rain with Liquid
 *       Weather (when that's on) even while Ambience itself is off.</li>
 * </ul>
 * Nothing is sent to the server; gameplay is unaffected.
 */
public final class AmbienceModule extends Module {
	public static final String[] WEATHER = {"server", "clear", "rain", "thunder", "snow"};
	private static final int WEATHER_SERVER = 0;
	private static final int WEATHER_RAIN = 2;
	private static final int WEATHER_THUNDER = 3;
	private static final int WEATHER_SNOW = 4;
	public static final String[] PRECIPITATION = {"biome", "rain", "snow"};
	private static final int PRECIPITATION_RAIN = 1;
	private static final int PRECIPITATION_SNOW = 2;
	/** Per-tick change of the eased rain / thunder levels (vanilla uses 0.01). */
	private static final float WEATHER_EASE = 0.02f;

	private static AmbienceModule instance;

	public final BooleanSetting customTime = add(new BooleanSetting("custom_time", "liquidclient.ambience.custom_time", true));
	/** Hours on a 24 h clock (12 = noon). */
	public final NumberSetting time = add(new NumberSetting("time", "liquidclient.ambience.time", 0, 24, 0.25, 19.5)
			.format(AmbienceModule::clock));
	public final ModeSetting weather = add(new ModeSetting("weather", "liquidclient.ambience.weather", WEATHER, 0));
	public final BooleanSetting liquidWeather = add(new BooleanSetting("liquid_weather", "liquidclient.ambience.liquid_weather", true));
	public final ModeSetting precipitation = add(new ModeSetting("precipitation", "liquidclient.ambience.precipitation", PRECIPITATION, 0));
	public final NumberSetting rainDensity = add(new NumberSetting("rain_density", "liquidclient.ambience.rain_density", 25, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting snowDensity = add(new NumberSetting("snow_density", "liquidclient.ambience.snow_density", 25, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting flakeSize = add(new NumberSetting("flake_size", "liquidclient.ambience.flake_size", 50, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting wind = add(new NumberSetting("wind", "liquidclient.ambience.wind", 0, 100, 5, 30).suffix("%"));
	public final BooleanSetting splashes = add(new BooleanSetting("splashes", "liquidclient.ambience.splashes", true));
	public final NumberSetting weatherRange = add(new NumberSetting("weather_range", "liquidclient.ambience.weather_range", 12, 28, 1, 24)
			.suffix(" blocks"));
	public final BooleanSetting fullbright = add(new BooleanSetting("fullbright", "liquidclient.ambience.fullbright", false));
	public final NumberSetting brightness = add(new NumberSetting("brightness", "liquidclient.ambience.brightness", 10, 100, 5, 100)
			.suffix("%"));

	private float rain;
	private float thunder;
	private float serverRain;
	private float serverThunder;
	private boolean overriding;

	public AmbienceModule() {
		super("ambience", Category.RENDER, 0xE216);
		instance = this;
	}

	private static String clock(double hours) {
		int minutes = (int) Math.round(hours * 60.0) % 1440;
		return String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60);
	}

	@Override
	public void clientTick(Minecraft minecraft) {
		int mode = weather.index();
		if (mode == WEATHER_SERVER) {
			overriding = false;
			return;
		}
		if (!overriding) {
			// Start the transition from what the server shows right now.
			rain = serverRain;
			thunder = serverThunder;
			overriding = true;
		}
		float rainTarget = mode == WEATHER_RAIN || mode == WEATHER_THUNDER || mode == WEATHER_SNOW ? 1f : 0f;
		float thunderTarget = mode == WEATHER_THUNDER ? 1f : 0f;
		rain += Math.clamp(rainTarget - rain, -WEATHER_EASE, WEATHER_EASE);
		thunder += Math.clamp(thunderTarget - thunder, -WEATHER_EASE, WEATHER_EASE);
	}

	@Override
	protected void onDisable() {
		overriding = false;
	}

	// ---- Hooks (render / client thread) --------------------------------------------------------

	/** Clock ticks into the day to show (0 = 06:00, as in vanilla), or -1 to keep the server's. */
	public static long dayTicks() {
		long photo = PhotoMode.dayTicks();
		if (photo >= 0L) {
			return photo; // Photo Mode's time of day
		}
		AmbienceModule m = instance;
		if (m == null || !m.isEnabled() || !m.customTime.get()) {
			return -1L;
		}
		// The End runs its own clock (end flashes); only the overworld day is replaced.
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && level.dimension() == Level.END) {
			return -1L;
		}
		double ticks = (m.time.get() - 6.0) * 1000.0;
		return Math.floorMod(Math.round(ticks), 24000L);
	}

	/** Rain level the client renders; {@code original} is the server's. */
	public static float rainLevel(float original) {
		float photo = PhotoMode.rain();
		if (photo >= 0f) {
			return photo;
		}
		AmbienceModule m = instance;
		if (m == null || !m.isEnabled() || !m.overriding) {
			if (m != null) {
				m.serverRain = original;
			}
			return Storm.rainLevel(original);
		}
		return Storm.rainLevel(m.rain);
	}

	public static float thunderLevel(float original) {
		float photo = PhotoMode.thunder();
		if (photo >= 0f) {
			return photo;
		}
		// Storm Chaser follows the server's thunderstorms when asked to.
		Storm.serverThunder(original);
		AmbienceModule m = instance;
		if (m == null || !m.isEnabled() || !m.overriding) {
			if (m != null) {
				m.serverThunder = original;
			}
			return Storm.thunderLevel(original);
		}
		return Storm.thunderLevel(m.thunder * m.rain);
	}

	/**
	 * The module when Liquid Weather should replace vanilla's rain and snow, else null. A storm (Storm
	 * Chaser) draws its rain with it too, with these settings, even while Ambience is off.
	 */
	public static @Nullable AmbienceModule liquidWeather() {
		AmbienceModule m = instance;
		return m != null && (m.isEnabled() || Storm.active()) && m.liquidWeather.get() ? m : null;
	}

	/** What falls everywhere instead of what the biome says, or null to follow the biome. */
	public static Biome.@Nullable Precipitation forcedPrecipitation() {
		Biome.Precipitation photo = PhotoMode.precipitation();
		if (photo != null) {
			return photo;
		}
		AmbienceModule m = instance;
		if (m == null || !m.isEnabled()) {
			return null;
		}
		if (m.weather.index() == WEATHER_SNOW) {
			return Biome.Precipitation.SNOW;
		}
		return switch (m.precipitation.index()) {
			case PRECIPITATION_RAIN -> Biome.Precipitation.RAIN;
			case PRECIPITATION_SNOW -> Biome.Precipitation.SNOW;
			default -> null;
		};
	}

	/** Night-vision strength for the lightmap (0 = off). */
	public static float fullbright() {
		AmbienceModule m = instance;
		return m != null && m.isEnabled() && m.fullbright.get() ? (float) m.brightness.get() / 100f : 0f;
	}
}
