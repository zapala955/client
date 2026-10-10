package net.client.visuals.gui.menu;

import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.clickgui.ClickGuiScreen;
import net.client.visuals.gui.widget.DockIcons;
import net.client.visuals.gui.widget.LockEntryButton;
import net.client.visuals.gui.widget.MediaPlayerWidget;
import net.client.visuals.gui.widget.StatusIconButton;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassText;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Easing;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Tween;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.render.shader.ShaderSupport;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Replacement title screen, styled after the macOS lock screen: a sharp photo, the date, a large
 * Liquid Glass clock and a short list of "users" (the main actions) – nothing else.
 *
 * <pre>
 *                                                        noue   ⟳  ▦  ⚙  ⏻   ← status icons
 *
 *                              Thu Oct 2
 *                               8:35                                         ← glass clock
 *
 *
 *                          (●) Singleplayer
 *                          (●) Multiplayer                                   ← user-picker entries
 *                          (●) Options
 *  Minecraft 26.3 · 26 mods
 * </pre>
 *
 * <p>Sharpness: the wallpaper is resampled to the exact framebuffer size and drawn 1:1, the clock is
 * rasterised at exact device resolution, text uses the per-GUI-scale Inter variants and every
 * shape is an SDF – nothing on this screen is a stretched bitmap.</p>
 *
 * <p>Lifecycle: the screen owns only the clock mask and icon textures (freed in {@link #removed()}); the
 * wallpaper textures belong to {@link BackgroundEngine}, which frees them when idle or on world
 * join. No world references are held, so leaving a server and returning here is safe.</p>
 */
public final class CustomMainMenuScreen extends Screen implements LiquidScreen {
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE MMM d", Locale.ENGLISH);
	private static final float INTRO_SECONDS = 1.6f;

	private final DeltaTimer timer = new DeltaTimer();
	private final Tween intro = new Tween(INTRO_SECONDS, Easing.LINEAR);
	private final GlassText clock = new GlassText("clock/nunito-700.ttf");
	private final TextLabel date = new TextLabel("", LiquidText.TextStyle.TITLE).tracking(0.25f);
	private final TextLabel footer = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel user = new TextLabel("", LiquidText.TextStyle.LABEL);
	private boolean introPlayed;
	private long shownMinute = -1L;
	private String clockText = "";

	private final MediaPlayerWidget mediaPlayer = new MediaPlayerWidget();
	private LockEntryButton[] entries = new LockEntryButton[0];
	private StatusIconButton[] icons = new StatusIconButton[0];
	private float dateY;
	private float clockY;
	private float clockCap;

	public CustomMainMenuScreen() {
		super(Component.translatable("narrator.screen.title"));
	}

	@Override
	protected void init() {
		ShaderSupport.revalidate();
		if (!introPlayed) {
			introPlayed = true;
			minecraft.gameRenderer.panorama().startSpin();
			timer.reset();
			UiFrame.time = 0f;
			intro.play(Motion.seconds(INTRO_SECONDS), Easing.LINEAR);
			UiSounds.wake();
			LiquidClient.LOGGER.info("Main menu shown (background={})", LiquidConfig.MENU_BACKGROUND.save().getAsString());
		}
		footer.setText("Minecraft " + SharedConstants.getCurrentVersion().name() + "  ·  " + countUserMods() + " mods");
		user.setText(minecraft.getUser().getName());
		shownMinute = -1L;
		updateClock();
		layout();
	}

	private void layout() {
		clockCap = Math.clamp(height * 0.105f, 28f, 150f);
		dateY = HighDpiEngine.snap(height * 0.14f);
		clockY = HighDpiEngine.snap(dateY + date.capHeight() + Math.max(7f, height * 0.024f));

		// User-picker entries, left-aligned in a centred column.
		LockEntryButton singleplayer = new LockEntryButton(0, 0, Component.translatable("liquidclient.menu.singleplayer"),
				DockIcons.Icon.SINGLEPLAYER, () -> minecraft.gui.setScreen(new SelectWorldScreen(this)));
		LockEntryButton multiplayer = new LockEntryButton(0, 0, Component.translatable("liquidclient.menu.multiplayer"),
				DockIcons.Icon.MULTIPLAYER, () -> {
					Screen next = minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
					minecraft.gui.setScreen(next);
				});
		if (!minecraft.allowsMultiplayer()) {
			multiplayer.active = false;
			multiplayer.setTooltip(Tooltip.create(Component.translatable("title.multiplayer.disabled")));
		}
		LockEntryButton options = new LockEntryButton(0, 0, Component.translatable("liquidclient.menu.options"),
				DockIcons.Icon.OPTIONS, () -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options)));
		entries = new LockEntryButton[]{singleplayer, multiplayer, options};
		int column = 0;
		for (LockEntryButton entry : entries) {
			column = Math.max(column, entry.getWidth());
		}
		int x = Math.round((width - column) * 0.5f);
		int y = Math.round(Math.max(clockY + clockCap + 40f, height * 0.70f));
		y = Math.min(y, height - 18 - entries.length * (LockEntryButton.HEIGHT + 4));
		for (LockEntryButton entry : entries) {
			entry.setX(x);
			entry.setY(y);
			addRenderableWidget(entry);
			y += LockEntryButton.HEIGHT + 4;
		}

		// Status icons, right to left like the macOS menu bar. Reconnect only exists once there is a
		// server to reconnect to (a greyed-out arrow reads as a stuck spinner).
		int ix = width - 8 - StatusIconButton.SIZE;
		int iy = 6;
		StatusIconButton quit = icon(ix, iy, "liquidclient.menu.quit", DockIcons.Icon.QUIT, () -> minecraft.stop());
		ix -= StatusIconButton.SIZE + 2;
		StatusIconButton clickGui = icon(ix, iy, "liquidclient.menu.clickgui", DockIcons.Icon.CLICKGUI,
				() -> minecraft.gui.setScreen(new ClickGuiScreen(this)));
		ix -= StatusIconButton.SIZE + 2;
		StatusIconButton mods = icon(ix, iy, "liquidclient.menu.mods", DockIcons.Icon.MODS, this::openMods);
		if (LiquidClient.lastServer() != null) {
			ix -= StatusIconButton.SIZE + 2;
			StatusIconButton relog = icon(ix, iy, "liquidclient.menu.relog", DockIcons.Icon.RELOG, this::relog);
			icons = new StatusIconButton[]{relog, mods, clickGui, quit};
		} else {
			icons = new StatusIconButton[]{mods, clickGui, quit};
		}
	}

	private StatusIconButton icon(int x, int y, String key, DockIcons.Icon icon, Runnable action) {
		return addRenderableWidget(new StatusIconButton(x, y, Component.translatable(key), icon, action));
	}

	// ---- Actions ------------------------------------------------------------------------------

	private void openMods() {
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			try {
				// Reflective so Mod Menu stays an optional, compile-time-free dependency.
				Class<?> modsScreen = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
				minecraft.gui.setScreen((Screen) modsScreen.getConstructor(Screen.class).newInstance(this));
				return;
			} catch (ReflectiveOperationException | ClassCastException e) {
				LiquidClient.LOGGER.warn("Couldn't open Mod Menu", e);
			}
		}
		SystemToast.addOrUpdate(minecraft.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
				Component.translatable("liquidclient.toast.no_modmenu"),
				Component.translatable("liquidclient.toast.no_modmenu.desc", countUserMods()));
	}

	private void relog() {
		ServerData last = LiquidClient.lastServer();
		if (last != null) {
			ConnectScreen.startConnecting(this, minecraft, ServerAddress.parseString(last.ip), last, false, null);
		}
	}

	/** Mods the player installed – not Fabric API's ~50 sub-modules or built-in "mods". */
	private static int countUserMods() {
		int count = 0;
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			String id = mod.getMetadata().getId();
			if (mod.getContainingMod().isEmpty() && !mod.getMetadata().getType().equals("builtin")
					&& !id.equals("fabricloader") && !id.equals("mixinextras")) {
				count++;
			}
		}
		return count;
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		intro.update(UiFrame.dt);
		BackgroundEngine.INSTANCE.extract(g, width, height);

		// Barely-there vignettes keep the status icons and entries legible over bright photos.
		RenderUtils.gradient(g, 0f, 0f, width, height * 0.16f, 0f, 0x2E000000, 0x00000000);
		RenderUtils.gradient(g, 0f, height * 0.55f, width, height * 0.45f, 0f, 0x00000000, 0x38000000);

		// Wake-up: the photo starts frosted and sharpens, like the macOS lock screen.
		float sharpen = intro.staggered(0f, 1.1f, Easing.OUT_CUBIC);
		BackgroundEngine.INSTANCE.frostOverlay(g, 1f - sharpen);
		float fade = 1f - Easing.clamp01(intro.raw() * 3f);
		if (fade > 0f) {
			RenderUtils.rect(g, 0f, 0f, width, height, ColorUtil.fade(0xFF000000, fade));
		}
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		updateClock();
		float centerX = width * 0.5f;

		// Date + glass clock.
		float t = intro.staggered(0.25f, 0.9f, Easing.OUT_CUBIC);
		float rise = HighDpiEngine.snap((1f - t) * 8f);
		RenderUtils.pushAlpha(t);
		LiquidText.drawCenteredSoft(g, date, centerX, dateY + rise, 0xF2FFFFFF);
		clock.update(clockText, clockCap);
		clock.draw(g, centerX, clockY + rise, 0xFFFFFFFF, BackgroundEngine.INSTANCE.frostTexture(),
				BackgroundEngine.INSTANCE.screenWidth(), BackgroundEngine.INSTANCE.screenHeight());
		RenderUtils.popAlpha();

		for (int i = 0; i < entries.length; i++) {
			float e = intro.staggered(0.55f + i * 0.07f, 0.6f, Easing.OUT_CUBIC);
			entries[i].setAppear(e, (1f - e) * 8f);
		}
		float s = intro.staggered(0.45f, 0.6f, Easing.OUT_CUBIC);
		for (StatusIconButton icon : icons) {
			icon.setAppear(s);
		}

		// Lock-screen "Now Playing" card between the clock and the entries, when there's room.
		if (LiquidConfig.menuMediaPlayer && MediaBridge.supported()) {
			float my = clockY + clockCap + Math.max(18f, height * 0.045f);
			float entriesTop = entries.length > 0 ? entries[0].getY() : height;
			if (my + MediaPlayerWidget.HEIGHT <= entriesTop - 10f) {
				float appear = intro.staggered(0.5f, 0.7f, Easing.OUT_CUBIC);
				mediaPlayer.extract(g, (width - MediaPlayerWidget.WIDTH) * 0.5f, my, mouseX, mouseY, UiFrame.dt, appear);
			}
		}

		// Name next to the status icons, version in the corner.
		RenderUtils.pushAlpha(s);
		if (icons.length > 0) {
			float ux = icons[0].getX() - 8f - user.width();
			LiquidText.drawSoft(g, user, ux, 6f + StatusIconButton.SIZE * 0.5f - user.capHeight() * 0.5f, 0xEBFFFFFF);
		}
		LiquidText.drawSoft(g, footer, 10f, height - 14f, 0x99FFFFFF);
		RenderUtils.popAlpha();

		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	/** Clock "H:mm" and date, re-formatted only when the minute changes. */
	private void updateClock() {
		long now = System.currentTimeMillis();
		long minute = (now + TimeZone.getDefault().getOffset(now)) / 60_000L;
		if (minute != shownMinute) {
			shownMinute = minute;
			int minuteOfDay = (int) Math.floorMod(minute, 1440L);
			int minutes = minuteOfDay % 60;
			clockText = (minuteOfDay / 60) + ":" + (minutes < 10 ? "0" : "") + minutes;
			date.setText(LocalDateTime.now().format(DATE));
		}
	}

	// ---- Input --------------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return mediaPlayer.mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return mediaPlayer.mouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);
	}

	// ---- Lifecycle ----------------------------------------------------------------------------

	@Override
	public void removed() {
		clock.release();
		IconFont.release();
		// Hand wallpaper textures back as soon as nothing needs them (idle timer / world join).
		BackgroundEngine.INSTANCE.onMenuHidden();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	/**
	 * Per the directive's {@code shouldPause() → true} (26.3 name: {@code isPauseScreen}). There's no
	 * world behind the title screen, so this only matters if an integrated server is still winding
	 * down – pausing keeps it from ticking while the menu is up.
	 */
	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
