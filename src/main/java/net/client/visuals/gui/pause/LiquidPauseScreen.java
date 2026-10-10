package net.client.visuals.gui.pause;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.clickgui.ClickGuiScreen;
import net.client.visuals.gui.hud.HudEditorScreen;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.hud.element.MusicElement;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.media.NowPlaying;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.shader.BlurEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.Dialogs;
import net.minecraft.tags.DialogTags;
import net.minecraft.util.CommonLinks;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * The game menu, minimal: over the blurred world, the world's name, one solid
 * white "Back to Game", a grouped glass list (Options, Advancements, Statistics, World Options and
 * the server's own pause-menu entry), Save and Quit on its own, a single row of small monochrome
 * icons for everything secondary (ClickGUI, HUD editor, mods, screenshots, feedback, bug reports,
 * friends, player reporting) and one line naming what's playing.
 *
 * <p>It <em>is</em> a {@link PauseScreen} (vanilla checks {@code instanceof PauseScreen} for
 * toasts, the F3+Esc pause and pausing on focus loss), but builds no vanilla widgets. Every action
 * is vanilla's: the friends-list consent flow, draft-report handling before disconnecting, the
 * server's pause-menu dialogs and links.</p>
 */
public final class LiquidPauseScreen extends PauseScreen implements LiquidScreen {
	private static final float W = 236f;
	private static final float ROW_H = 32f;
	private static final float ICON = 24f;
	private static final int ICON_SETTINGS = 0xE154;
	private static final int ICON_TROPHY = 0xE373;
	private static final int ICON_CHART = 0xE2A3;
	private static final int ICON_EARTH = 0xE1F3;
	private static final int ICON_LINK = 0xE102;
	private static final int ICON_GRID = 0xE0FF;
	private static final int ICON_HUD = 0xE11D;
	private static final int ICON_PACKAGE = 0xE129;
	private static final int ICON_CAMERA = 0xE064;
	private static final int ICON_FEEDBACK = 0xE56F;
	private static final int ICON_BUG = 0xE20C;
	private static final int ICON_FRIENDS = 0xE46E;
	private static final int ICON_FLAG = 0xE0D1;
	private static final int ICON_CHEVRON = 0xE06F;
	private static final int ICON_ALERT = 0xE193;
	private static final int ICON_NOTE = IconFont.FILLED | 0xE405;

	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth appear = new Smooth(0f, 7f);
	private final Smooth tooltipFade = new Smooth(0f, 16f);
	private final Smooth songHover = new Smooth(0f, Motion.HOVER);
	private final TextLabel worldName = new TextLabel("", LiquidText.TextStyle.TITLE);
	private final TextLabel song = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final List<Row> rows = new ArrayList<>();
	private final List<Glyph> glyphs = new ArrayList<>();
	private @Nullable String songKey;
	private boolean songIsMedia;
	private PillButton resume;
	private Row quit;
	private @Nullable Object tooltipOwner;
	private List<TextLabel> tooltipLines = List.of();
	private float tooltipDwell;
	private boolean disconnecting;
	private float scale = 1f;
	private float songX;
	private float songY;
	private float songW;

	public LiquidPauseScreen() {
		super(true);
	}

	// ---- Building ------------------------------------------------------------------------------

	@Override
	protected void init() {
		timer.reset();
		rows.clear();
		glyphs.clear();
		resume = new PillButton(text("menu.returnToGame"), 0, PillButton.Kind.LIGHT, () -> {
			minecraft.gui.setScreen(null);
			minecraft.mouseHandler.grabMouse();
		});

		rows.add(new Row(text("menu.options"), ICON_SETTINGS, () -> open(new OptionsScreen(this, minecraft.options))));
		rows.add(new Row(text("gui.advancements"), ICON_TROPHY,
				() -> open(new AdvancementsScreen(minecraft.player.connection.getAdvancements(), this))));
		rows.add(new Row(text("gui.stats"), ICON_CHART, () -> open(new StatsScreen(this, minecraft.player.getStats()))));
		rows.add(new Row(text("options.worldOptions.button"), ICON_EARTH, () -> {
			if (minecraft.level != null) {
				open(new WorldOptionsScreen(this, minecraft.level));
			}
		}).enabledWhen(() -> minecraft.level != null));
		customAdditions().ifPresent(holder -> rows.add(new Row(holder.value().common().computeExternalTitle().getString(), ICON_LINK,
				() -> minecraft.player.connection.showDialog(holder, this))));
		quit = new Row(CommonComponents.disconnectButtonLabel(minecraft.isLocalServer()).getString(), 0, this::disconnect)
				.destructive().enabledWhen(() -> !disconnecting);

		glyphs.add(new Glyph(ICON_GRID, "ClickGUI", () -> open(new ClickGuiScreen(this))));
		glyphs.add(new Glyph(ICON_HUD, "HUD Editor", () -> open(new HudEditorScreen(this))));
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			glyphs.add(new Glyph(ICON_PACKAGE, "Mods", this::openMods));
		}
		glyphs.add(new Glyph(ICON_CAMERA, "Screenshots", () -> {
			Path folder = minecraft.gameDirectory.toPath().resolve("screenshots");
			try {
				Files.createDirectories(folder);
			} catch (Exception ignored) {
				// Opening works for existing folders.
			}
			Blaze3D.openPath(folder);
		}));
		glyphs.add(new Glyph(ICON_FEEDBACK, text("menu.sendFeedback"), () -> ConfirmLinkScreen.confirmLinkNow(this,
				SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK)));
		glyphs.add(new Glyph(ICON_BUG, text("menu.reportBugs"), () -> ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK))
				.enabledWhen(() -> !SharedConstants.getCurrentVersion().dataVersion().isSideSeries()));
		boolean online = !minecraft.isDemo() && !minecraft.isOfflineDeveloperMode();
		glyphs.add(new Glyph(ICON_FRIENDS, "Friends", () -> OnlineOptionsScreen.confirmFriendsListEnabled(minecraft,
				() -> open(new FriendsOverlayScreen(this)), this))
				.enabledWhen(() -> online)
				.badge(() -> minecraft.getPlayerSocialManager().isFriendListEnabled()
						? minecraft.getPlayerSocialManager().getIncomingRequests().size() : 0));
		boolean reportable = hasPlayersToReport();
		glyphs.add(new Glyph(ICON_FLAG, reportable ? text("menu.playerReporting") : text("menu.playerReporting.no_players"),
				() -> open(new SocialInteractionsScreen(this))).enabledWhen(() -> reportable));
		refreshInfo();
	}

	/** Vanilla label without the "Options..." style ellipsis. */
	private static String text(String key) {
		String s = Component.translatable(key).getString();
		if (s.endsWith("...")) {
			s = s.substring(0, s.length() - 3);
		} else if (s.endsWith("…")) {
			s = s.substring(0, s.length() - 1);
		}
		return s.strip();
	}

	private void open(Screen screen) {
		minecraft.gui.setScreen(screen);
	}

	private void openMods() {
		try {
			Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
			open((Screen) type.getConstructor(Screen.class).newInstance(this));
		} catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
			LiquidClient.LOGGER.warn("Couldn't open Mod Menu", e);
		}
	}

	/** PauseScreen: player reporting needs another player on an integrated server. */
	private boolean hasPlayersToReport() {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null || minecraft.player == null) {
			return true;
		}
		UUID self = minecraft.player.getUUID();
		for (UUID id : minecraft.player.connection.getSeenPlayers().keySet()) {
			if (!id.equals(self)) {
				return true;
			}
		}
		return false;
	}

	/** PauseScreen#getCustomAdditions: the server's pause-menu dialogs, or its links. */
	private Optional<? extends Holder<Dialog>> customAdditions() {
		if (minecraft.player == null) {
			return Optional.empty();
		}
		Registry<Dialog> registry = minecraft.player.connection.registryAccess().lookupOrThrow(Registries.DIALOG);
		Optional<? extends HolderSet<Dialog>> custom = registry.get(DialogTags.PAUSE_SCREEN_ADDITIONS);
		if (custom.isPresent()) {
			HolderSet<Dialog> set = custom.get();
			if (set.size() > 0) {
				return set.size() == 1 ? Optional.of(set.get(0)) : registry.get(Dialogs.CUSTOM_OPTIONS);
			}
		}
		ServerLinks links = minecraft.player.connection.serverLinks();
		return !links.isEmpty() ? registry.get(Dialogs.SERVER_LINKS) : Optional.empty();
	}

	/** PauseScreen's disconnect button: lets the reporting context handle an unsent draft first. */
	private void disconnect() {
		disconnecting = true;
		minecraft.getReportingContext().draftReportHandled(minecraft, this,
				() -> minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
	}

	/** The world's (or server's) name: the menu's only heading. */
	private void refreshInfo() {
		IntegratedServer server = minecraft.getSingleplayerServer();
		ServerData data = minecraft.getCurrentServer();
		worldName.setText(server != null ? server.getWorldData().getLevelName() : data != null ? data.name : "Multiplayer");
	}

	// ---- Rendering -----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (minecraft.gui.screen() == this) {
			GlassRenderer.beginFrame(BlurEngine.frostBackdrop(g));
		}
		RenderUtils.gradient(g, 0f, 0f, width, height, 0f, 0x52000000, 0x94000000);
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	private float contentHeight() {
		return 8.7f + 20f + ROW_H + 12f + rows.size() * ROW_H + 10f + ROW_H + 18f + ICON + 18f + 5.1f;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		float dt = UiFrame.dt;
		float a = appear.target(1f).update(dt);
		float h = contentHeight();
		scale = Math.min(1f, (height - 20f) / h);
		float lw = width / scale;
		float lh = height / scale;
		int mx = Math.round(mouseX / scale);
		int my = Math.round(mouseY / scale);
		float cx = lw * 0.5f;
		float x = HighDpiEngine.snap(cx - W * 0.5f);
		float y = HighDpiEngine.snap((lh - h) * 0.5f);
		Object hovered = null;

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		if (scale < 1f) {
			pose.scale(scale, scale);
		}

		// Heading: just the world's name.
		step(a, 0);
		worldName.maxWidth(W + 60f);
		LiquidText.drawCentered(g, worldName, cx, y, Ui.TEXT, false);
		y += worldName.capHeight() + 20f;
		RenderUtils.popAlpha();

		// The one primary action.
		step(a, 1);
		resume.setBounds(x, y, W, ROW_H);
		resume.extract(g, mx, my);
		RenderUtils.popAlpha();
		y += ROW_H + 12f;

		// Grouped list.
		step(a, 2);
		group(g, x, y, W, rows.size() * ROW_H);
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				RenderUtils.rect(g, x + 40f, HighDpiEngine.snap(y + i * ROW_H), W - 40f, HighDpiEngine.px(), 0x1AFFFFFF);
			}
			rows.get(i).extract(g, x, y + i * ROW_H, W, mx, my);
		}
		RenderUtils.popAlpha();
		y += rows.size() * ROW_H + 10f;

		step(a, 3);
		group(g, x, y, W, ROW_H);
		quit.extract(g, x, y, W, mx, my);
		if (minecraft.getReportingContext().hasDraftReport()) {
			IconFont.draw(g, ICON_ALERT, x + W - 18f, y + ROW_H * 0.5f, 10f, Ui.WARNING, 0);
		}
		RenderUtils.popAlpha();
		y += ROW_H + 18f;

		// Everything secondary, as quiet icons.
		step(a, 4);
		float rowW = glyphs.size() * ICON + (glyphs.size() - 1) * 6f;
		float gx = cx - rowW * 0.5f;
		for (Glyph glyph : glyphs) {
			if (glyph.extract(g, gx + ICON * 0.5f, y + ICON * 0.5f, mx, my)) {
				hovered = glyph;
			}
			gx += ICON + 6f;
		}
		RenderUtils.popAlpha();
		y += ICON + 18f;

		// What's playing (the PC's media player, else the game's music).
		step(a, 5);
		extractSong(g, cx, y, mx, my, dt);
		RenderUtils.popAlpha();

		extractTooltip(g, hovered, mx, my, dt, lw, lh);
		pose.popMatrix();
	}

	/** Each block fades in a moment after the previous one. Pushes an alpha; the caller pops it. */
	private static void step(float a, int index) {
		float t = Math.clamp((a - index * 0.06f) / 0.55f, 0f, 1f);
		RenderUtils.pushAlpha(t * t * (3f - 2f * t));
	}

	private static void group(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		float r = 14f;
		RenderUtils.fill(g, x, y, w, h, r, GlassRenderer.frosted() ? 0x21FFFFFF : 0xB31C1C1E);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), 0x24FFFFFF, 0x0DFFFFFF);
	}

	private void extractSong(GuiGraphicsExtractor g, float cx, float y, int mouseX, int mouseY, float dt) {
		String key = null;
		boolean media = false;
		if (LiquidConfig.menuMediaPlayer && MediaBridge.supported()) {
			MediaBridge.request(ModuleManager.get(MusicElement.class).source.index() == 1);
			NowPlaying now = MediaBridge.current();
			if (now.active && !now.title.isEmpty()) {
				key = now.key;
				media = true;
				if (!key.equals(songKey)) {
					String artist = now.primaryArtist();
					song.setText(artist.isEmpty() ? now.title : now.title + "  —  " + artist);
				}
			}
		}
		if (key == null && rendersNowPlayingToast()) {
			String music = minecraft.getMusicManager().getCurrentMusicTranslationKey();
			if (music != null) {
				key = music;
				if (!music.equals(songKey)) {
					song.setText(Component.translatable(music.replace("/", ".")).getString());
				}
			}
		}
		songKey = key;
		songIsMedia = media;
		if (key == null) {
			songW = 0f;
			return;
		}
		song.maxWidth(W + 40f);
		songW = song.width() + 14f;
		songX = cx - songW * 0.5f;
		songY = y;
		boolean over = media && mouseX >= songX - 4f && mouseX < songX + songW + 4f && mouseY >= y - 4f && mouseY < y + 9f;
		float hv = songHover.target(over).update(dt);
		int color = ColorUtil.lerp(0x80FFFFFF, 0xE6FFFFFF, hv);
		IconFont.draw(g, ICON_NOTE, songX + 4f, y + song.capHeight() * 0.5f, 8.5f, color, 0);
		LiquidText.draw(g, song, songX + 14f, y, color, false);
	}

	private void extractTooltip(GuiGraphicsExtractor g, @Nullable Object hovered, int mouseX, int mouseY, float dt, float lw, float lh) {
		if (hovered != tooltipOwner) {
			tooltipDwell = tooltipFade.get() > 0.5f && hovered != null ? 0.45f : 0f;
			tooltipOwner = hovered;
			tooltipLines = hovered instanceof Glyph glyph ? glyph.tip : List.of();
		}
		tooltipDwell += dt;
		float t = tooltipFade.target(hovered != null && tooltipDwell >= 0.35f).update(dt);
		if (t > 0.01f) {
			Ui.tooltip(g, tooltipLines, mouseX, mouseY, lw, lh, t);
		}
	}

	// ---- Input ---------------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x() / scale;
		double my = event.y() / scale;
		int button = event.button();
		if (resume.mouseClicked(mx, my, button)) {
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			for (Row row : rows) {
				if (row.press(mx, my)) {
					return true;
				}
			}
			if (quit.press(mx, my)) {
				return true;
			}
			for (Glyph glyph : glyphs) {
				if (glyph.press(mx, my)) {
					return true;
				}
			}
			if (songIsMedia && songW > 0f && mx >= songX - 4f && mx < songX + songW + 4f && my >= songY - 4f && my < songY + 9f) {
				UiSounds.click();
				MediaBridge.togglePlayPause();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		double mx = event.x() / scale;
		double my = event.y() / scale;
		boolean handled = resume.mouseReleased(mx, my, event.button());
		for (Row row : rows) {
			handled |= row.release(mx, my);
		}
		handled |= quit.release(mx, my);
		for (Glyph glyph : glyphs) {
			handled |= glyph.release(mx, my);
		}
		return handled || super.mouseReleased(event);
	}

	// ---- Elements ------------------------------------------------------------------------------

	/** A row of the grouped list: thin icon, label, chevron; or a centred red action. */
	private static final class Row {
		final TextLabel label;
		final int icon;
		final Runnable action;
		BooleanSupplier enabled = () -> true;
		boolean destructive;
		final Smooth hover = new Smooth(0f, Motion.HOVER);
		final Smooth press = new Smooth(0f, Motion.PRESS);
		final Smooth active = new Smooth(1f, Motion.TOGGLE);
		boolean pressed;
		float x;
		float y;
		float w;

		Row(String label, int icon, Runnable action) {
			this.label = new TextLabel(label, LiquidText.TextStyle.LABEL);
			this.icon = icon;
			this.action = action;
		}

		Row enabledWhen(BooleanSupplier condition) {
			this.enabled = condition;
			return this;
		}

		Row destructive() {
			this.destructive = true;
			return this;
		}

		boolean over(double mx, double my) {
			return mx >= x && mx < x + w && my >= y && my < y + ROW_H;
		}

		void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
			this.x = x;
			this.y = y;
			this.w = w;
			float dt = UiFrame.dt;
			boolean on = enabled.getAsBoolean();
			boolean over = on && over(mouseX, mouseY);
			float hv = hover.target(over).update(dt);
			float p = press.target(pressed && over).update(dt);
			float a = active.target(on ? 1f : 0.35f).update(dt);
			RenderUtils.pushAlpha(a);
			if (hv > 0.01f) {
				RenderUtils.fill(g, x + 3f, y + 2.5f, w - 6f, ROW_H - 5f, 11f, ColorUtil.fade(0x1AFFFFFF, hv * (1f + p * 0.6f)));
			}
			float cy = y + ROW_H * 0.5f;
			if (destructive) {
				LiquidText.drawCentered(g, label, x + w * 0.5f, cy - label.capHeight() * 0.5f,
						ColorUtil.lerp(0xF2FF6961, 0xFFFF7B74, hv), false);
			} else {
				IconFont.draw(g, icon, x + 20f, cy, 11f, ColorUtil.lerp(0xA6FFFFFF, 0xF2FFFFFF, hv), 0);
				label.maxWidth(w - 70f);
				LiquidText.draw(g, label, x + 40f, cy - label.capHeight() * 0.5f, 0xEBFFFFFF, false);
				IconFont.draw(g, ICON_CHEVRON, x + w - 16f + hv * 1.5f, cy, 10f, ColorUtil.lerp(0x4DFFFFFF, 0x99FFFFFF, hv), 0);
			}
			RenderUtils.popAlpha();
		}

		boolean press(double mx, double my) {
			if (enabled.getAsBoolean() && over(mx, my)) {
				pressed = true;
				return true;
			}
			return false;
		}

		boolean release(double mx, double my) {
			if (!pressed) {
				return false;
			}
			pressed = false;
			if (enabled.getAsBoolean() && over(mx, my)) {
				UiSounds.click();
				action.run();
			}
			return true;
		}
	}

	/** A small monochrome icon button; a soft disc fades in on hover. Its tooltip names it. */
	private static final class Glyph {
		final int icon;
		final List<TextLabel> tip;
		final Runnable action;
		BooleanSupplier enabled = () -> true;
		IntSupplier badge = () -> 0;
		final Smooth hover = new Smooth(0f, Motion.HOVER);
		final Smooth press = new Smooth(0f, Motion.PRESS);
		boolean pressed;
		float cx;
		float cy;

		Glyph(int icon, String tooltip, Runnable action) {
			this.icon = icon;
			this.tip = Ui.wrap(tooltip, 200f, LiquidText.TextStyle.BODY);
			this.action = action;
		}

		Glyph enabledWhen(BooleanSupplier condition) {
			this.enabled = condition;
			return this;
		}

		Glyph badge(IntSupplier count) {
			this.badge = count;
			return this;
		}

		boolean over(double mx, double my) {
			double dx = mx - cx;
			double dy = my - cy;
			return dx * dx + dy * dy <= (ICON * 0.5 + 1) * (ICON * 0.5 + 1);
		}

		/** @return whether the pointer is over it */
		boolean extract(GuiGraphicsExtractor g, float cx, float cy, int mouseX, int mouseY) {
			this.cx = cx;
			this.cy = cy;
			float dt = UiFrame.dt;
			boolean on = enabled.getAsBoolean();
			boolean over = over(mouseX, mouseY);
			float hv = hover.target(on && over).update(dt);
			float p = press.target(pressed && on && over).update(dt);
			RenderUtils.pushAlpha(on ? 1f : 0.35f);
			if (hv > 0.01f) {
				float r = ICON * 0.5f * (1f - p * 0.06f);
				RenderUtils.circle(g, cx, cy, r, ColorUtil.fade(0x24FFFFFF, hv));
			}
			IconFont.draw(g, icon, cx, cy, 11.5f, ColorUtil.lerp(0x8CFFFFFF, 0xFFFFFFFF, hv), 0);
			if (on && badge.getAsInt() > 0) {
				RenderUtils.circle(g, cx + 7f, cy - 7f, 3f, Ui.DESTRUCTIVE);
			}
			RenderUtils.popAlpha();
			return over;
		}

		boolean press(double mx, double my) {
			if (enabled.getAsBoolean() && over(mx, my)) {
				pressed = true;
				return true;
			}
			return false;
		}

		boolean release(double mx, double my) {
			if (!pressed) {
				return false;
			}
			pressed = false;
			if (enabled.getAsBoolean() && over(mx, my)) {
				UiSounds.click();
				action.run();
			}
			return true;
		}
	}
}
