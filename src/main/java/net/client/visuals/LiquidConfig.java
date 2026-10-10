package net.client.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.render.background.Wallpaper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Interface-level preferences (not gameplay modules). Plain static fields are read directly by
 * the render code – no getters, no boxing, no map lookups per frame.
 *
 * <p>Persisted to {@code config/liquidclient.json}.</p>
 */
public final class LiquidConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("liquidclient.json");

	/** Frosted backdrops (engine blur / wallpaper frost). Off = opaque glass. */
	public static boolean backdropBlur = true;
	/** Accessibility: animations complete near-instantly. */
	public static boolean reduceMotion = false;
	/** Soft accent glow behind headline text. */
	public static boolean textGlow = true;
	/** Use the bundled Inter TTF instead of the vanilla bitmap font. */
	public static boolean customFont = true;
	/** Replace the vanilla title screen. */
	public static boolean replaceTitleScreen = true;
	/** Lock-screen "Now Playing" card on the main menu. */
	public static boolean menuMediaPlayer = true;
	/** Singleplayer, Multiplayer and Options as Liquid windows instead of the vanilla screens. */
	public static boolean liquidMenus = true;
	/** Liquid boot screen while resources load, and Liquid world-loading / connecting screens. */
	public static boolean liquidLoading = true;

	/** Main menu wallpaper; ids are stable config values, the order is {@link Wallpaper}'s. */
	public static final ModeSetting MENU_BACKGROUND = new ModeSetting("menu_background", "liquidclient.setting.background",
			Wallpaper.ids(), Wallpaper.LAKE.ordinal())
			.onChange(() -> {
				if (Wallpaper.byIndex(LiquidConfig.MENU_BACKGROUND.index()) == Wallpaper.IMAGE) {
					BackgroundEngine.INSTANCE.reloadWallpaper(); // re-scan the folder for new files
				}
				save();
			});

	/** Main-menu design generation; bumping it migrates old wallpaper choices to the new default. */
	private static final int MENU_STYLE = 2;
	private static int menuStyle = MENU_STYLE;

	/** Last ClickGUI window position, as a fraction of the screen so it survives GUI-scale changes. */
	public static float clickGuiX = -1f;
	public static float clickGuiY = -1f;
	public static int clickGuiCategory = 0;
	/** The rest of the ClickGUI's state, restored when it opens again (also after a restart). */
	public static boolean clickGuiExpanded;
	public static boolean clickGuiCollapsed;
	public static float clickGuiScroll;
	public static String clickGuiSearch = "";
	/** Cards whose settings drawer was open (module ids, "setting.<key>" for interface cards). */
	public static final Set<String> clickGuiOpenCards = new LinkedHashSet<>();

	private LiquidConfig() {
	}

	public static void load() {
		if (!Files.exists(FILE)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(FILE)) {
			JsonObject json = GSON.fromJson(reader, JsonObject.class);
			if (json == null) {
				return;
			}
			backdropBlur = bool(json, "backdropBlur", backdropBlur);
			reduceMotion = bool(json, "reduceMotion", reduceMotion);
			textGlow = bool(json, "textGlow", textGlow);
			customFont = bool(json, "customFont", customFont);
			replaceTitleScreen = bool(json, "replaceTitleScreen", replaceTitleScreen);
			menuMediaPlayer = bool(json, "menuMediaPlayer", menuMediaPlayer);
			liquidMenus = bool(json, "liquidMenus", liquidMenus);
			liquidLoading = bool(json, "liquidLoading", liquidLoading);
			if (json.has("menuBackground")) {
				MENU_BACKGROUND.load(json.get("menuBackground"));
			}
			menuStyle = json.has("menuStyle") ? json.get("menuStyle").getAsInt() : 1;
			if (menuStyle < MENU_STYLE) {
				// The macOS lock-screen redesign: start everyone on the new photo wallpaper once.
				MENU_BACKGROUND.load(new com.google.gson.JsonPrimitive("lake"));
				menuStyle = MENU_STYLE;
			}
			clickGuiX = json.has("clickGuiX") ? json.get("clickGuiX").getAsFloat() : clickGuiX;
			clickGuiY = json.has("clickGuiY") ? json.get("clickGuiY").getAsFloat() : clickGuiY;
			clickGuiCategory = json.has("clickGuiCategory") ? json.get("clickGuiCategory").getAsInt() : clickGuiCategory;
			clickGuiExpanded = bool(json, "clickGuiExpanded", clickGuiExpanded);
			clickGuiCollapsed = bool(json, "clickGuiCollapsed", clickGuiCollapsed);
			clickGuiScroll = json.has("clickGuiScroll") ? Math.max(0f, json.get("clickGuiScroll").getAsFloat()) : clickGuiScroll;
			clickGuiSearch = json.has("clickGuiSearch") ? json.get("clickGuiSearch").getAsString() : clickGuiSearch;
			clickGuiOpenCards.clear();
			if (json.has("clickGuiOpenCards") && json.get("clickGuiOpenCards").isJsonArray()) {
				for (JsonElement card : json.getAsJsonArray("clickGuiOpenCards")) {
					clickGuiOpenCards.add(card.getAsString());
				}
			}
		} catch (IOException | RuntimeException e) {
			LiquidClient.LOGGER.warn("Failed to read {}, using defaults", FILE, e);
		}
	}

	public static void save() {
		JsonObject json = new JsonObject();
		json.addProperty("backdropBlur", backdropBlur);
		json.addProperty("reduceMotion", reduceMotion);
		json.addProperty("textGlow", textGlow);
		json.addProperty("customFont", customFont);
		json.addProperty("replaceTitleScreen", replaceTitleScreen);
		json.addProperty("menuMediaPlayer", menuMediaPlayer);
		json.addProperty("liquidMenus", liquidMenus);
		json.addProperty("liquidLoading", liquidLoading);
		json.add("menuBackground", MENU_BACKGROUND.save());
		json.addProperty("menuStyle", menuStyle);
		json.addProperty("clickGuiX", clickGuiX);
		json.addProperty("clickGuiY", clickGuiY);
		json.addProperty("clickGuiCategory", clickGuiCategory);
		json.addProperty("clickGuiExpanded", clickGuiExpanded);
		json.addProperty("clickGuiCollapsed", clickGuiCollapsed);
		json.addProperty("clickGuiScroll", clickGuiScroll);
		json.addProperty("clickGuiSearch", clickGuiSearch);
		JsonArray openCards = new JsonArray();
		for (String card : clickGuiOpenCards) {
			openCards.add(card);
		}
		json.add("clickGuiOpenCards", openCards);
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				GSON.toJson(json, writer);
			}
		} catch (IOException e) {
			LiquidClient.LOGGER.warn("Failed to write {}", FILE, e);
		}
	}

	private static boolean bool(JsonObject json, String key, boolean fallback) {
		return json.has(key) ? json.get(key).getAsBoolean() : fallback;
	}
}
