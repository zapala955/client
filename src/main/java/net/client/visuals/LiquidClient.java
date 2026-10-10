package net.client.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.browser.BrowserManager;
import net.client.visuals.gui.clickgui.ClickGuiScreen;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.KillEffectsModule;
import net.client.visuals.gui.hud.HudEditorScreen;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.element.TargetHudElement;
import net.client.visuals.hud.island.DynamicIslandElement;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.render.BackdropCapture;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.render.loading.BootArt;
import net.client.visuals.render.chunk.ChunkAnimator;
import net.client.visuals.render.lyrics.WorldLyrics;
import net.client.visuals.render.particle.AmbientParticles;
import net.client.visuals.render.weather.LiquidWeather;
import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.module.player.ShowcaseModule;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.camera.Showcase;
import net.client.visuals.render.hand.HandChams;
import net.client.visuals.render.post.Rainbow;
import net.client.visuals.render.post.WetLens;
import net.client.visuals.render.post.WetWorld;
import net.client.visuals.render.post.PhotoCapture;
import net.client.visuals.render.post.PostFx;
import net.client.visuals.render.sky.LiquidSky;
import net.client.visuals.render.text.SharpText;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint.
 *
 * <h2>Hook map</h2>
 * <ul>
 *   <li><b>Title screen replacement</b> – {@code GuiMixin} swaps any {@code TitleScreen} passed to
 *       {@code Gui#setScreen} before vanilla initialises it (26.2 moved screen management from
 *       {@code Minecraft} to {@code Gui}).</li>
 *   <li><b>ClickGUI keybind</b> – Fabric {@code KeyMappingHelper}, polled in {@code END_CLIENT_TICK}.</li>
 *   <li><b>Module keybinds</b> – {@code KeyboardHandlerMixin}, where vanilla fires gameplay keys.</li>
 *   <li><b>Visual modules</b> – {@code mixin.visual}: removals, ambience (clock / weather /
 *       lightmap), view model, zoom; Kill Effects and the Target HUD via {@code AttackEntityCallback}.</li>
 *   <li><b>Particles</b> – {@code AmbientParticles#tick} below adds client particles to vanilla's
 *       particle engine, which ticks, culls and draws them (no registry entries, no mixin).</li>
 *   <li><b>Sky</b> – {@code SkyRendererMixin} (head/return of {@code SkyRenderer#render}).</li>
 *   <li><b>Primitive pooling</b> – {@code GuiRenderStateMixin} recycles pooled render states whenever
 *       vanilla resets the frame's GUI render state.</li>
 *   <li><b>Blur strength</b> – {@code GameRendererMixin} raises the menu blur radius while a Liquid
 *       screen is open.</li>
 *   <li><b>GPU resource lifecycle</b> – world join / disconnect / client stop events below release
 *       textures and buffers via {@code Minecraft#execute}, i.e. on the render thread between frames,
 *       never in the middle of a frame that might still reference them.</li>
 * </ul>
 */
public final class LiquidClient implements ClientModInitializer {
	public static final String MOD_ID = "liquidclient";
	public static final Logger LOGGER = LoggerFactory.getLogger("Liquid");

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(id(MOD_ID));

	/**
	 * Right Shift by default. 26.3 replaced GLFW with SDL, so key codes must come from vanilla's
	 * {@link InputConstants} – GLFW constants no longer match.
	 */
	public static final KeyMapping OPEN_CLICK_GUI = new KeyMapping(
			"key.liquidclient.clickgui", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, CATEGORY);

	/** Hold to zoom (C, like OptiFine; vanilla only uses C together with a number in creative). */
	public static final KeyMapping ZOOM = new KeyMapping(
			"key.liquidclient.zoom", InputConstants.Type.KEYBOARD, InputConstants.KEY_C, CATEGORY);

	public static final KeyMapping OPEN_HUD_EDITOR = unbound("key.liquidclient.hud_editor");
	/** Showcase: the cinematic third-person camera, on and off. */
	public static final KeyMapping SHOWCASE = new KeyMapping(
			"key.liquidclient.showcase", InputConstants.Type.KEYBOARD, InputConstants.KEY_F6, CATEGORY);
	/** Photo Mode: a camera in Minecraft, on and off (F4; vanilla only uses it with F3). */
	public static final KeyMapping PHOTO_MODE = new KeyMapping(
			"key.liquidclient.photo_mode", InputConstants.Type.KEYBOARD, InputConstants.KEY_F4, CATEGORY);
	public static final KeyMapping MEDIA_PLAY_PAUSE = unbound("key.liquidclient.media_toggle");
	public static final KeyMapping MEDIA_NEXT = unbound("key.liquidclient.media_next");
	public static final KeyMapping MEDIA_PREVIOUS = unbound("key.liquidclient.media_previous");

	private static KeyMapping unbound(String name) {
		return new KeyMapping(name, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY);
	}

	/** Last multiplayer server joined this session, for the main menu's "Relog" action. */
	private static @Nullable ServerData lastServer;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static @Nullable ServerData lastServer() {
		return lastServer;
	}

	@Override
	public void onInitializeClient() {
		LiquidConfig.load();
		ModuleManager.init();
		// Diagnostics: if a session log ends without this line, the process was killed or died natively.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("JVM shutdown hook ran (orderly exit)"), "Liquid shutdown log"));
		KeyMappingHelper.registerKeyMapping(OPEN_CLICK_GUI);
		KeyMappingHelper.registerKeyMapping(ZOOM);
		KeyMappingHelper.registerKeyMapping(OPEN_HUD_EDITOR);
		KeyMappingHelper.registerKeyMapping(SHOWCASE);
		KeyMappingHelper.registerKeyMapping(PHOTO_MODE);
		KeyMappingHelper.registerKeyMapping(MEDIA_PLAY_PAUSE);
		KeyMappingHelper.registerKeyMapping(MEDIA_NEXT);
		KeyMappingHelper.registerKeyMapping(MEDIA_PREVIOUS);
		HudManager.init();
		HudElementRegistry.addLast(id("hud"), HudManager::extract);

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			while (OPEN_CLICK_GUI.consumeClick()) {
				// Only from gameplay: in other screens RShift may be typed into text fields.
				if (minecraft.gui.screen() == null && minecraft.level != null) {
					minecraft.gui.setScreen(new ClickGuiScreen(null));
				}
			}
			while (OPEN_HUD_EDITOR.consumeClick()) {
				if (minecraft.gui.screen() == null && minecraft.level != null) {
					minecraft.gui.setScreen(new HudEditorScreen(null));
				}
			}
			while (SHOWCASE.consumeClick()) {
				if (minecraft.gui.screen() == null && minecraft.level != null && !PhotoMode.active()) {
					ModuleManager.get(ShowcaseModule.class).toggle();
				}
			}
			while (PHOTO_MODE.consumeClick()) {
				// Leaving is handled by Photo Mode itself (the key never reaches here while it's open).
				if (minecraft.gui.screen() == null && minecraft.level != null && minecraft.player != null) {
					ModuleManager.get(PhotoModeModule.class).setEnabled(true);
				}
			}
			while (MEDIA_PLAY_PAUSE.consumeClick()) {
				MediaBridge.togglePlayPause();
			}
			while (MEDIA_NEXT.consumeClick()) {
				MediaBridge.next();
			}
			while (MEDIA_PREVIOUS.consumeClick()) {
				MediaBridge.previous();
			}
			BackgroundEngine.INSTANCE.tick();
			LiquidSky.tick();
			ChunkAnimator.tick();
			AmbientParticles.tick(minecraft);
			LiquidWeather.tick(minecraft);
			WetLens.tick(minecraft);
			WetWorld.tick(minecraft);
			Rainbow.tick(minecraft);
			HudManager.tick(minecraft);
			Showcase.tick(minecraft);
			PhotoMode.tick(minecraft);
			ModuleManager.clientTick(minecraft);
			MediaBridge.tick();
		});

		// Kill Effects and the Target HUD follow what the local player hits. The callback runs on
		// both sides in singleplayer; only the client-side call matters here.
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (level.isClientSide()) {
				KillEffectsModule.onAttack(entity);
				TargetHudElement.onAttack(entity);
				DynamicIslandElement.onAttack(entity);
			}
			return InteractionResult.PASS;
		});

		// Browser module: clicks are taken at START (before vanilla consumes them), the screen is
		// submitted with the level's features, the spawn hint is a HUD element.
		ClientTickEvents.START_CLIENT_TICK.register(BrowserManager::tick);
		LevelRenderEvents.COLLECT_SUBMITS.register(BrowserManager::collect);
		// Lyrics "In the World": words as 3D text in front of the player.
		LevelRenderEvents.COLLECT_SUBMITS.register(WorldLyrics::collect);
		HudElementRegistry.addLast(id("browser_hint"), BrowserManager::extractHud);

		ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> {
			rememberServer(minecraft);
			// Menu wallpapers aren't needed in game; the loading screens still show it while the
			// world loads, so it's released after the usual grace period instead of right away.
			minecraft.execute(BackgroundEngine.INSTANCE::onMenuHidden);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> minecraft.execute(() -> {
			LiquidSky.onWorldExit();
			PostFx.release();
			PhotoCapture.release();
			LiquidWeather.release();
			WetLens.reset();
			WetWorld.reset();
			Rainbow.reset();
			HandChams.release();
			BrowserManager.shutdown(); // the screen belongs to the world it was placed in
		}));

		ClientLifecycleEvents.CLIENT_STOPPING.register(minecraft -> {
			ModuleManager.saveIfDirty();
			LiquidConfig.save();
			LiquidSky.release();
			PostFx.release();
			PhotoCapture.release();
			LiquidWeather.release();
			BackgroundEngine.INSTANCE.release();
			BootArt.release();
			BrowserManager.shutdown();
			SharpText.release();
			MediaBridge.shutdown();
			BackdropCapture.release();
			AlbumArt.release();
		});
	}

	private static void rememberServer(Minecraft minecraft) {
		ServerData current = minecraft.getCurrentServer();
		if (current != null && !minecraft.isLocalServer()) {
			lastServer = current;
		}
	}
}
