package net.client.visuals.mixin;

import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.menu.CustomMainMenuScreen;
import net.client.visuals.gui.packs.PacksScreen;
import net.client.visuals.gui.pause.LiquidPauseScreen;
import net.client.visuals.gui.servers.ServersScreen;
import net.client.visuals.gui.settings.SettingsScreen;
import net.client.visuals.gui.worlds.WorldsScreen;
import net.client.visuals.mixin.access.JoinMultiplayerScreenAccessor;
import net.client.visuals.mixin.access.PackSelectionModelAccessor;
import net.client.visuals.mixin.access.PackSelectionScreenAccessor;
import net.client.visuals.mixin.access.SelectWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Replaces the vanilla title screen ("TitleScreenMixin" in the directive's terms – the hook lives
 * in {@code Gui#setScreen}, where 26.2+ manages screens, so the TitleScreen never even gets
 * initialised).
 *
 * <h2>Why two injection points</h2>
 * {@code setScreen(Screen screen)} receives a TitleScreen in two ways:
 * <ol>
 *   <li>explicitly: {@code setScreen(new TitleScreen())} – handled at {@code HEAD};</li>
 *   <li>implicitly: {@code setScreen(null)} without a level runs {@code screen = new TitleScreen()}
 *       – handled at that {@code STORE}.</li>
 * </ol>
 * Both run <i>before</i> the parameter is next loaded, so {@code this.screen = screen} and
 * {@code screen.init(...)} see the same object.
 *
 * <p><b>Fixed bug:</b> an earlier version modified the variable at the {@code PUTFIELD}. By then
 * {@code aload_1} had already pushed the original TitleScreen, so the field kept an
 * <i>uninitialised</i> TitleScreen while our menu was initialised but never shown. Fabric's screen
 * API then crashed on the next tick ("has not been correctly initialised") – the
 * leave-server → title-screen crash.</p>
 *
 * <p>Exact class check only, so mods' TitleScreen subclasses are left alone.</p>
 *
 * <h2>Liquid windows</h2>
 * With {@link LiquidConfig#liquidMenus} on, the vanilla Singleplayer, Multiplayer, Options and
 * game-menu (pause) and pack-selection screens are swapped the same way for the Liquid windows, keeping their parent screen. That
 * covers every entry point (title screen, pause menu, the multiplayer safety notice, other mods'
 * buttons) without touching each caller. Again exact classes only.</p>
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen liquid$replaceRequestedTitleScreen(Screen screen) {
		return replace(screen);
	}

	@ModifyVariable(method = "setScreen", at = @At("STORE"), argsOnly = true)
	private Screen liquid$replaceDefaultTitleScreen(Screen screen) {
		return replace(screen);
	}

	private static Screen replace(Screen screen) {
		if (screen == null) {
			return null;
		}
		Class<?> type = screen.getClass();
		if (LiquidConfig.replaceTitleScreen && type == TitleScreen.class) {
			return new CustomMainMenuScreen();
		}
		if (LiquidConfig.liquidMenus) {
			if (type == SelectWorldScreen.class) {
				return new WorldsScreen(((SelectWorldScreenAccessor) screen).liquid$lastScreen());
			}
			if (type == JoinMultiplayerScreen.class) {
				return new ServersScreen(((JoinMultiplayerScreenAccessor) screen).liquid$lastScreen());
			}
			if (type == PauseScreen.class && ((PauseScreen) screen).showsPauseMenu()) {
				// F3+Esc (pause without the menu) stays vanilla.
				return new LiquidPauseScreen();
			}
			if (type == PackSelectionScreen.class) {
				// Same repository, result callback and folder; vanilla's watcher is already running.
				PackSelectionScreenAccessor packs = (PackSelectionScreenAccessor) screen;
				PackSelectionModelAccessor model = (PackSelectionModelAccessor) packs.liquid$model();
				packs.liquid$closeWatcher();
				return new PacksScreen(model.liquid$repository(), model.liquid$output(), packs.liquid$packDir(), screen.getTitle());
			}
			if (type == OptionsScreen.class) {
				return new SettingsScreen(((OptionsScreen) screen).getLastScreen(), Minecraft.getInstance().options);
			}
		}
		return screen;
	}
}
