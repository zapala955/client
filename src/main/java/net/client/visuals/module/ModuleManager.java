package net.client.visuals.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.client.visuals.LiquidClient;
import net.client.visuals.hud.HudTheme;
import net.client.visuals.hud.element.ArmorElement;
import net.client.visuals.hud.element.ClockElement;
import net.client.visuals.hud.element.CompassElement;
import net.client.visuals.hud.element.CoordinatesElement;
import net.client.visuals.hud.element.CpsElement;
import net.client.visuals.hud.element.FpsElement;
import net.client.visuals.hud.element.KeystrokesElement;
import net.client.visuals.hud.element.LyricsElement;
import net.client.visuals.hud.element.MemoryElement;
import net.client.visuals.hud.element.ModuleListElement;
import net.client.visuals.hud.element.MusicElement;
import net.client.visuals.hud.element.NotificationsElement;
import net.client.visuals.hud.element.PingElement;
import net.client.visuals.hud.element.PotionsElement;
import net.client.visuals.hud.element.ServerElement;
import net.client.visuals.hud.element.SpeedElement;
import net.client.visuals.hud.element.TargetHudElement;
import net.client.visuals.hud.island.DynamicIslandElement;
import net.client.visuals.module.render.AmbienceModule;
import net.client.visuals.module.render.AtmosphereModule;
import net.client.visuals.module.render.BloomModule;
import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.module.player.ShowcaseModule;
import net.client.visuals.module.render.AmbientOcclusionModule;
import net.client.visuals.module.render.HandChamsModule;
import net.client.visuals.module.render.MotionBlurModule;
import net.client.visuals.module.render.RainbowsModule;
import net.client.visuals.module.render.LiquidWaterModule;
import net.client.visuals.module.render.StormChaserModule;
import net.client.visuals.module.render.SunRaysModule;
import net.client.visuals.module.render.WetWorldModule;
import net.client.visuals.module.render.WetLensModule;
import net.client.visuals.module.render.ChunkAnimationsModule;
import net.client.visuals.module.render.ColorGradingModule;
import net.client.visuals.module.render.DepthOfFieldModule;
import net.client.visuals.module.render.CustomFogModule;
import net.client.visuals.module.render.KillEffectsModule;
import net.client.visuals.module.render.LightingModule;
import net.client.visuals.module.render.ParticlesModule;
import net.client.visuals.module.render.RemovalsModule;
import net.client.visuals.module.render.SkyboxModule;
import net.client.visuals.module.render.ViewModelModule;
import net.client.visuals.module.render.WavingPlantsModule;
import net.client.visuals.module.utility.BrowserModule;
import net.client.visuals.module.utility.ZoomModule;
import net.client.visuals.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry and persistence for modules ({@code config/liquidclient-modules.json}).
 *
 * <p>Changes only mark the state dirty; the file is written when the ClickGUI closes or the client
 * stops, never per slider tick.</p>
 */
public final class ModuleManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("liquidclient-modules.json");

	private static final List<Module> MODULES = new ArrayList<>();
	private static final Map<Category, List<Module>> BY_CATEGORY = new EnumMap<>(Category.class);
	private static boolean dirty;
	private static boolean loading;

	private ModuleManager() {
	}

	public static void init() {
		for (Category category : Category.values()) {
			BY_CATEGORY.put(category, new ArrayList<>());
		}
		register(new SkyboxModule());
		register(new ChunkAnimationsModule());
		register(new AmbienceModule());
		register(new CustomFogModule());
		register(new RemovalsModule());
		register(new ViewModelModule());
		register(new KillEffectsModule());
		register(new ParticlesModule());
		register(new AtmosphereModule());
		register(new LightingModule());
		register(new ColorGradingModule());
		register(new BloomModule());
		register(new SunRaysModule());
		register(new WetLensModule());
		register(new RainbowsModule());
		register(new WetWorldModule());
		register(new StormChaserModule());
		register(new LiquidWaterModule());
		register(new HandChamsModule());
		register(new MotionBlurModule());
		register(new AmbientOcclusionModule());
		register(new DepthOfFieldModule());
		register(new ShowcaseModule());
		register(new PhotoModeModule());
		register(new WavingPlantsModule());
		register(new HudTheme());
		register(new MusicElement());
		register(new LyricsElement());
		register(new FpsElement());
		register(new PingElement());
		register(new CpsElement());
		register(new CoordinatesElement());
		register(new SpeedElement());
		register(new CompassElement());
		register(new ClockElement());
		register(new MemoryElement());
		register(new ServerElement());
		register(new KeystrokesElement());
		register(new ArmorElement());
		register(new PotionsElement());
		register(new TargetHudElement());
		register(new ModuleListElement());
		register(new NotificationsElement());
		register(new DynamicIslandElement());
		register(new ZoomModule());
		register(new BrowserModule());
		load();
	}

	public static <M extends Module> M register(M module) {
		MODULES.add(module);
		BY_CATEGORY.get(module.category()).add(module);
		return module;
	}

	public static List<Module> all() {
		return Collections.unmodifiableList(MODULES);
	}

	public static List<Module> byCategory(Category category) {
		List<Module> list = BY_CATEGORY.get(category);
		return list == null ? List.of() : Collections.unmodifiableList(list);
	}

	public static <M extends Module> M get(Class<M> type) {
		for (Module module : MODULES) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}
		throw new IllegalArgumentException("Module not registered: " + type.getName());
	}

	/**
	 * A key went down during gameplay (no screen open; from {@code KeyboardHandlerMixin}, exactly
	 * where vanilla fires its own key mappings, so typing in chat never toggles anything).
	 */
	public static void onKeyPressed(int key) {
		if (key == Module.NO_KEY) {
			return;
		}
		for (int i = 0; i < MODULES.size(); i++) {
			Module module = MODULES.get(i);
			if (module.keybind() == key && module.bindable()) {
				module.toggle();
				// The Dynamic Island announces it when it's on; otherwise the Notifications toasts do.
				if (!DynamicIslandElement.onModuleToggled(module)) {
					NotificationsElement.post(module);
				}
			}
		}
	}

	/** End of every client tick. */
	public static void clientTick(Minecraft minecraft) {
		for (int i = 0; i < MODULES.size(); i++) {
			Module module = MODULES.get(i);
			if (module.isEnabled()) {
				module.clientTick(minecraft);
			}
		}
	}

	public static void markDirty() {
		if (!loading) {
			dirty = true;
		}
	}

	public static void saveIfDirty() {
		if (dirty) {
			save();
		}
	}

	private static void load() {
		if (!Files.exists(FILE)) {
			return;
		}
		loading = true;
		try (Reader reader = Files.newBufferedReader(FILE)) {
			JsonObject root = GSON.fromJson(reader, JsonObject.class);
			if (root == null) {
				return;
			}
			for (Module module : MODULES) {
				JsonElement element = root.get(module.id());
				if (element == null || !element.isJsonObject()) {
					continue;
				}
				JsonObject json = element.getAsJsonObject();
				JsonObject settings = json.has("settings") && json.get("settings").isJsonObject() ? json.getAsJsonObject("settings") : new JsonObject();
				for (Setting setting : module.settings()) {
					JsonElement value = settings.get(setting.id());
					if (value != null) {
						setting.load(value);
					}
				}
				if (json.has("enabled") && module.savesState()) {
					module.setEnabled(json.get("enabled").getAsBoolean());
				}
				if (json.has("key") && json.get("key").isJsonPrimitive()) {
					module.setKeybind(json.get("key").getAsInt());
				}
			}
		} catch (IOException | RuntimeException e) {
			LiquidClient.LOGGER.warn("Failed to read {}, using module defaults", FILE, e);
		} finally {
			loading = false;
			dirty = false;
		}
	}

	private static void save() {
		JsonObject root = new JsonObject();
		for (Module module : MODULES) {
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.savesState() && module.isEnabled());
			if (module.keybind() != Module.NO_KEY) {
				json.addProperty("key", module.keybind());
			}
			JsonObject settings = new JsonObject();
			for (Setting setting : module.settings()) {
				settings.add(setting.id(), setting.save());
			}
			json.add("settings", settings);
			root.add(module.id(), json);
		}
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				GSON.toJson(root, writer);
			}
			dirty = false;
		} catch (IOException e) {
			LiquidClient.LOGGER.warn("Failed to write {}", FILE, e);
		}
	}
}
