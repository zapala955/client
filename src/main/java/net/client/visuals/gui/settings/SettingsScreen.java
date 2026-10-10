package net.client.visuals.gui.settings;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.clickgui.ClickGuiScreen;
import net.client.visuals.gui.component.BoolBinding;
import net.client.visuals.gui.hud.HudEditorScreen;
import net.client.visuals.gui.packs.PacksScreen;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.ScrollArea;
import net.client.visuals.gui.window.Sheet;
import net.client.visuals.gui.window.TextField;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.gui.window.WindowScreen;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CommonLinks;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Options as a macOS System Settings window: a floating glass sidebar of coloured panes with
 * search, and grouped inset lists of native-looking controls for every vanilla option (plus the
 * client's own). The rows drive vanilla's {@link OptionInstance}s through their own write paths;
 * the screen reproduces the side effects vanilla's option screens add on top (texture reloads after
 * mipmap / filtering changes, fullscreen mode on close, the unsupported-GPU warning, language
 * reloads, resource-pack application).
 */
public final class SettingsScreen extends WindowScreen {
	private static final float SIDEBAR_ROW = 24f;
	private static final float SECTION_GAP = 16f;
	private static final float CONTENT_PAD = 18f;
	private static final int ICON_SPARKLES = 0xE412;
	private static final int ICON_SETTINGS = 0xE154;
	private static final int ICON_MONITOR = 0xE11D;
	private static final int ICON_VOLUME = 0xE1AB;
	private static final int ICON_GAMEPAD = 0xE0DF;
	private static final int ICON_CHAT = 0xE116;
	private static final int ICON_SHIRT = 0xE1CA;
	private static final int ICON_LANGUAGES = 0xE0FE;
	private static final int ICON_ACCESSIBILITY = 0xE297;
	private static final int ICON_LAYERS = 0xE529;
	private static final int ICON_PALETTE = 0xE1DD;
	private static final int ICON_PACKAGE = 0xE129;
	private static final int ICON_SEARCH = 0xE151;
	private static final int ICON_ALERT = 0xE193;
	private static final int ICON_INFO = 0xE0F9;
	private static final int ICON_KEYBOARD = 0xE284;
	private static final int ICON_LAYOUT = 0xE1C1;
	private static final int ICON_GRID = 0xE0E9;
	private static final int ICON_EXTERNAL = 0xE0B9;
	/** Last pane shown, so reopening Settings returns to it (per session). */
	private static @Nullable String lastPage;

	/** Opens the next Settings window on the pane with this name (e.g. "Language"). */
	public static void preselect(String pageName) {
		lastPage = pageName;
	}

	private final Options options;
	private final List<SettingsPage> pages = new ArrayList<>();
	private SettingsPage current;
	private final ScrollArea content = new ScrollArea();
	private final ScrollArea sidebar = new ScrollArea();
	private final Smooth pageIn = new Smooth(1f, 9f);
	private final Smooth indicator = new Smooth(0f, 20f);
	private final List<SettingRow> drawn = new ArrayList<>();
	private final TextLabel searchTitle = new TextLabel("Search", LiquidText.TextStyle.TITLE);
	private final TextLabel searchSummary = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel noResults = new TextLabel("No Results", LiquidText.TextStyle.HEADLINE);
	private final TextLabel noResultsHint = new TextLabel("Try a different search.", LiquidText.TextStyle.BODY);
	private List<SettingsPage.Section> results = List.of();
	private String query = "";
	private @Nullable SettingRow captured;
	private TextField search;
	private PillButton done;
	private boolean indicatorPlaced;
	/** Language the page labels were built in; a language switch rebuilds them after the reload. */
	private String builtLanguage = "";

	// Vanilla VideoSettingsScreen side effects.
	private int oldMipmaps;
	private int oldAnisotropy;
	private TextureFilteringMethod oldFiltering;

	// Language pane: the choice is applied on demand (a resource reload).
	private String pendingLanguage = "";
	private String pendingLabel = "";

	// Geometry
	private float sbX;
	private float sbY;
	private float sbW;
	private float sbH;
	private float cX;
	private float cY;
	private float cW;
	private float cH;

	public SettingsScreen(@Nullable Screen parent, Options options) {
		super(Component.translatable("liquidclient.settings.title"), parent);
		this.options = options;
		this.oldMipmaps = options.mipmapLevels().get();
		this.oldAnisotropy = options.maxAnisotropyBit().get();
		this.oldFiltering = options.textureFiltering().get();
	}

	// ---- Layout --------------------------------------------------------------------------------

	@Override
	protected float preferredWidth() {
		return 650f;
	}

	@Override
	protected float preferredHeight() {
		return 420f;
	}

	@Override
	protected void layoutWindow(boolean first) {
		if (first) {
			minecraft.getGpuWarnlistManager().resetWarnings();
			buildPages();
			search = new TextField("Search", true).onChange(this::setQuery);
			done = new PillButton("Done", 0, PillButton.Kind.PRIMARY, this::onClose);
			widgets.add(search);
			widgets.add(done);
			current = pages.get(1);
			for (SettingsPage page : pages) {
				if (!page.isShortcut() && page.name.equals(lastPage)) {
					current = page;
				}
			}
		} else if (!builtLanguage.equals(minecraft.getLanguageManager().getSelected())) {
			int index = pages.indexOf(current);
			pages.clear();
			buildPages();
			current = pages.get(Math.clamp(index, 0, pages.size() - 1));
			setQuery("");
			search.setValue("");
		}
		float dw = Math.max(64f, done.naturalWidth());
		done.setBounds(wx + ww - 12f - dw, wy + (TITLE_H - Ui.CONTROL_H) * 0.5f, dw, Ui.CONTROL_H);
		sbW = Math.clamp(ww * 0.29f, 136f, 176f);
		sbX = wx + 8f;
		sbY = wy + TITLE_H + 8f;
		sbH = wh - TITLE_H - 16f;
		search.setBounds(sbX + 8f, sbY + 8f, sbW - 16f, Ui.CONTROL_H);
		cX = wx + sbW + 12f;
		cY = wy + TITLE_H + HighDpiEngine.px();
		cW = wx + ww - cX;
		cH = wh - TITLE_H - HighDpiEngine.px();
	}

	// ---- Pages ---------------------------------------------------------------------------------

	private void buildPages() {
		builtLanguage = minecraft.getLanguageManager().getSelected();
		pages.add(SettingsPage.pane("Liquid Client", "The client's interface, menus and visual effects.",
				ICON_SPARKLES, 0xFFBF5AF2, false, this::liquidPage));
		pages.add(SettingsPage.pane("General", "About this game, field of view, online and folders.",
				ICON_SETTINGS, 0xFF8E8E93, true, this::generalPage));
		pages.add(SettingsPage.pane("Video", "Display, rendering quality and on-screen preferences.",
				ICON_MONITOR, 0xFF32ADE6, false, this::videoPage));
		pages.add(SettingsPage.pane("Audio", "Volume for each sound category, output device and music.",
				ICON_VOLUME, 0xFFFF375F, false, this::audioPage));
		pages.add(SettingsPage.pane("Controls", "Key binds, mouse and how movement keys behave.",
				ICON_GAMEPAD, 0xFF5E5CE6, false, this::controlsPage));
		pages.add(SettingsPage.pane("Chat", "Chat visibility, appearance and safety.",
				ICON_CHAT, 0xFF30D158, false, this::chatPage));
		pages.add(SettingsPage.pane("Skin", "Which layers of your skin others see, and your main hand.",
				ICON_SHIRT, 0xFFFF9F0A, false, this::skinPage));
		pages.add(SettingsPage.pane("Language", "Game language and font options.",
				ICON_LANGUAGES, 0xFF0A84FF, false, this::languagePage));
		pages.add(SettingsPage.pane("Accessibility", "Narration, text legibility and motion effects.",
				ICON_ACCESSIBILITY, 0xFF0A84FF, false, this::accessibilityPage));
		pages.add(SettingsPage.shortcut("Resource Packs", ICON_LAYERS, 0xFFAF52DE, true, this::openResourcePacks));
		if (FabricLoader.getInstance().isModLoaded("iris")) {
			pages.add(SettingsPage.shortcut("Shader Packs", ICON_PALETTE, 0xFFFF6482, false, this::openShaderPacks));
		}
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			pages.add(SettingsPage.shortcut("Mods", ICON_PACKAGE, 0xFFA2845E, false, this::openMods));
		}
	}

	private OptionRow option(OptionInstance<?> option) {
		return new OptionRow(this, option);
	}

	private static BoolBinding config(java.util.function.BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
		return new BoolBinding() {
			@Override
			public boolean get() {
				return get.getAsBoolean();
			}

			@Override
			public void set(boolean value) {
				set.accept(value);
				LiquidConfig.save();
			}
		};
	}

	private void liquidPage(List<SettingsPage.Section> out) {
		out.add(SettingsPage.Section.of("Appearance", List.of(
				new Rows.Switch(this, I18n.get("liquidclient.setting.font"), I18n.get("liquidclient.setting.font.desc"),
						config(() -> LiquidConfig.customFont, v -> LiquidConfig.customFont = v)),
				new Rows.Switch(this, I18n.get("liquidclient.setting.blur"), I18n.get("liquidclient.setting.blur.desc"),
						config(() -> LiquidConfig.backdropBlur, v -> LiquidConfig.backdropBlur = v)),
				new Rows.Switch(this, I18n.get("liquidclient.setting.glow"), I18n.get("liquidclient.setting.glow.desc"),
						config(() -> LiquidConfig.textGlow, v -> LiquidConfig.textGlow = v)),
				new Rows.Switch(this, I18n.get("liquidclient.setting.motion"), I18n.get("liquidclient.setting.motion.desc"),
						config(() -> LiquidConfig.reduceMotion, v -> LiquidConfig.reduceMotion = v)))));
		List<String> wallpapers = new ArrayList<>();
		for (int i = 0; i < LiquidConfig.MENU_BACKGROUND.count(); i++) {
			wallpapers.add(I18n.get(LiquidConfig.MENU_BACKGROUND.modeKey(i)));
		}
		out.add(SettingsPage.Section.of("Menus", List.of(
				new Rows.Switch(this, I18n.get("liquidclient.setting.title"), I18n.get("liquidclient.setting.title.desc"),
						config(() -> LiquidConfig.replaceTitleScreen, v -> LiquidConfig.replaceTitleScreen = v)),
				new Rows.Choice(this, I18n.get("liquidclient.setting.background"), null, () -> wallpapers,
						LiquidConfig.MENU_BACKGROUND::index, LiquidConfig.MENU_BACKGROUND::set),
				new Rows.Switch(this, I18n.get("liquidclient.setting.media_player"), I18n.get("liquidclient.setting.media_player.desc"),
						config(() -> LiquidConfig.menuMediaPlayer, v -> LiquidConfig.menuMediaPlayer = v)),
				new Rows.Switch(this, I18n.get("liquidclient.setting.liquid_menus"), I18n.get("liquidclient.setting.liquid_menus.desc"),
						config(() -> LiquidConfig.liquidMenus, v -> LiquidConfig.liquidMenus = v)),
				new Rows.Switch(this, I18n.get("liquidclient.setting.liquid_loading"), I18n.get("liquidclient.setting.liquid_loading.desc"),
						config(() -> LiquidConfig.liquidLoading, v -> LiquidConfig.liquidLoading = v)))));
		out.add(SettingsPage.Section.of("Tools", List.of(
				new Rows.Link(this, "ClickGUI", ICON_GRID, 0xFFBF5AF2, false, null,
						() -> minecraft.gui.setScreen(new ClickGuiScreen(this))),
				new Rows.Link(this, "HUD Editor", ICON_LAYOUT, 0xFF5E5CE6, false, null,
						() -> minecraft.gui.setScreen(new HudEditorScreen(this))))));
	}

	private void generalPage(List<SettingsPage.Section> out) {
		String clientVersion = FabricLoader.getInstance().getModContainer(LiquidClient.MOD_ID)
				.map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("—");
		out.add(SettingsPage.Section.of("About", List.of(
				new Rows.Info(this, "Minecraft", SharedConstants.getCurrentVersion().name()),
				new Rows.Info(this, "Liquid Client", clientVersion),
				new Rows.Info(this, "Player", minecraft.getUser().getName()),
				new Rows.Info(this, "Mods Loaded", Integer.toString(FabricLoader.getInstance().getAllMods().size())))));
		out.add(SettingsPage.Section.of("Game", List.of(option(options.fov()))));
		out.add(SettingsPage.Section.of("Online", List.of(
				new Rows.Link(this, I18n.get("options.online"), false, () -> minecraft.gui.setScreen(new OnlineOptionsScreen(this, options))),
				option(options.allowServerListing()),
				option(options.realmsNotifications()))));
		Path game = minecraft.gameDirectory.toPath();
		out.add(SettingsPage.Section.of("Folders", List.of(
				new Rows.Link(this, "Game Folder", true, () -> openFolder(game)),
				new Rows.Link(this, "Screenshots", true, () -> openFolder(game.resolve("screenshots"))),
				new Rows.Link(this, "Resource Packs Folder", true, () -> openFolder(minecraft.getResourcePackDirectory())))));
		out.add(SettingsPage.Section.of("More", List.of(
				new Rows.Link(this, I18n.get("options.telemetry"), false,
						() -> minecraft.gui.setScreen(new TelemetryInfoScreen(this, options))).enabledWhen(minecraft::allowsTelemetry),
				new Rows.Link(this, I18n.get("options.credits_and_attribution"), false,
						() -> minecraft.gui.setScreen(new CreditsAndAttributionScreen(this))))));
	}

	private void videoPage(List<SettingsPage.Section> out) {
		boolean sodium = FabricLoader.getInstance().isModLoaded("sodium");
		if (sodium) {
			out.add(SettingsPage.Section.of(null, List.of(
					new Rows.Link(this, "Sodium Video Settings", ICON_MONITOR, 0xFF30D158, false, null, this::openSodium)))
					.footnote("Sodium renders the world; its own page has the performance and advanced options."));
		}
		out.add(SettingsPage.Section.of(null, List.<SettingRow>of(
				new Rows.Note(this, ICON_ALERT, Ui.WARNING, I18n.get("options.restartRequired"))
						.visibleWhen(options::isRestartRequiredToApplyVideoSettings))));

		List<SettingRow> display = new ArrayList<>(List.of(
				option(options.framerateLimit()),
				option(options.enableVsync()),
				option(options.inactivityFpsLimit()),
				option(options.guiScale()),
				option(options.fullscreen()),
				option(options.exclusiveFullscreen())));
		OptionInstance<Integer> resolution = fullscreenResolution();
		if (resolution != null) {
			display.add(option(resolution));
		}
		display.add(option(options.gamma()));
		display.add(option(options.preferredGraphicsBackend()));
		if (MacosUtil.IS_MACOS) {
			display.add(option(options.macFullscreenMenuVisibility()));
		}
		out.add(SettingsPage.Section.of("Display", display));

		out.add(SettingsPage.Section.of("Quality", List.of(
				option(options.graphicsPreset()),
				option(options.renderDistance()),
				option(options.simulationDistance()),
				option(options.prioritizeChunkUpdates()),
				option(options.biomeBlendRadius()),
				option(options.ambientOcclusion()),
				option(options.cloudStatus()),
				option(options.cloudRange()),
				option(options.particles()),
				option(options.mipmapLevels()),
				option(options.entityShadows()),
				option(options.entityDistanceScaling()),
				option(options.cutoutLeaves()),
				option(options.improvedTransparency()),
				option(options.textureFiltering()),
				option(options.maxAnisotropyBit())
						.enabledWhen(() -> options.textureFiltering().get() == TextureFilteringMethod.ANISOTROPIC),
				option(options.weatherRadius()),
				option(options.menuBackgroundBlurriness()))));
		out.add(SettingsPage.Section.of("Preferences", List.of(
				option(options.showAutosaveIndicator()),
				option(options.vignette()),
				option(options.attackIndicator()),
				option(options.chunkSectionFadeInTime()))));
		if (!sodium) {
			out.add(SettingsPage.Section.of(null, List.of(
					new Rows.Link(this, "All Video Settings", false,
							() -> minecraft.gui.setScreen(new VideoSettingsScreen(this, minecraft, options))))));
		}
	}

	/** VideoSettingsScreen's exclusive-fullscreen resolution option, rebuilt the same way. */
	private @Nullable OptionInstance<Integer> fullscreenResolution() {
		Window window = minecraft.getWindow();
		Monitor monitor = window.findBestMonitor();
		if (monitor == null) {
			return null;
		}
		int initial = window.getPreferredFullscreenVideoMode().map(monitor::indexOfMode).orElse(-1);
		return new OptionInstance<>(
				"options.fullscreen.exclusive.mode",
				OptionInstance.cachedConstantTooltip(Component.translatable("options.fullscreen.exclusive.mode.tooltip")),
				(caption, value) -> {
					if (value == -1) {
						return Options.genericValueLabel(caption, Component.translatable("options.fullscreen.current"));
					}
					VideoMode mode = monitor.mode(value);
					return Options.genericValueLabel(caption, Component.translatable("options.fullscreen.entry",
							mode.getWidth(), mode.getHeight(), mode.refreshRateLabel(), mode.getRedBits() + mode.getGreenBits() + mode.getBlueBits()));
				},
				new OptionInstance.IntRange(-1, monitor.modeCount() - 1),
				initial,
				value -> window.setPreferredFullscreenVideoMode(value == -1 ? Optional.empty() : Optional.of(monitor.mode(value))));
	}

	private void audioPage(List<SettingsPage.Section> out) {
		List<SettingRow> volume = new ArrayList<>();
		volume.add(option(options.getSoundSourceOptionInstance(SoundSource.MASTER)));
		for (SoundSource source : SoundSource.values()) {
			if (source != SoundSource.MASTER) {
				volume.add(option(options.getSoundSourceOptionInstance(source)));
			}
		}
		out.add(SettingsPage.Section.of("Volume", volume));
		out.add(SettingsPage.Section.of("Output", List.of(
				option(options.soundDevice()),
				option(options.directionalAudio()),
				option(options.showSubtitles()))));
		out.add(SettingsPage.Section.of("Music", List.of(
				option(options.musicFrequency()),
				option(options.musicToast()))));
	}

	private void controlsPage(List<SettingsPage.Section> out) {
		out.add(SettingsPage.Section.of("Keyboard", List.of(
				new Rows.Link(this, I18n.get("controls.keybinds"), ICON_KEYBOARD, 0xFF8E8E93, false, null,
						() -> minecraft.gui.setScreen(new KeyBindsScreen(this, options))))));
		out.add(SettingsPage.Section.of("Mouse", List.of(
				option(options.sensitivity()),
				option(options.mouseWheelSensitivity()),
				option(options.discreteMouseScroll()),
				option(options.invertMouseX()),
				option(options.invertMouseY()),
				option(options.allowCursorChanges()))));
		out.add(SettingsPage.Section.of("Movement", List.of(
				option(options.toggleCrouch()),
				option(options.toggleSprint()),
				option(options.sprintWindow()),
				option(options.autoJump()))));
		List<SettingRow> interaction = new ArrayList<>(List.of(
				option(options.toggleAttack()),
				option(options.toggleUse())));
		if (InputQuirks.EMULATE_RIGHT_CLICK_WITH_CTRL_KEY) {
			interaction.add(option(options.ctrlClickEmulatesRightClick()));
		}
		interaction.add(option(options.operatorItemsTab()));
		interaction.add(option(options.quitShortcuts()));
		out.add(SettingsPage.Section.of("Interaction", interaction));
	}

	private void chatPage(List<SettingsPage.Section> out) {
		out.add(SettingsPage.Section.of("Chat", List.of(
				option(options.chatVisibility()),
				option(options.chatColors()),
				option(options.chatLinks()),
				option(options.chatLinksPrompt()),
				option(options.autoSuggestions()),
				option(options.saveChatDrafts()),
				option(options.chatDelay()))));
		out.add(SettingsPage.Section.of("Appearance", List.of(
				option(options.chatOpacity()),
				option(options.textBackgroundOpacity()),
				option(options.chatScale()),
				option(options.chatLineSpacing()),
				option(options.chatWidth()),
				option(options.chatHeightFocused()),
				option(options.chatHeightUnfocused()))));
		out.add(SettingsPage.Section.of("Safety", List.of(
				option(options.onlyShowSecureChat()),
				option(options.hideMatchedNames()),
				option(options.reducedDebugInfo()),
				option(options.narrator()))));
	}

	private void skinPage(List<SettingsPage.Section> out) {
		List<SettingRow> layers = new ArrayList<>();
		for (PlayerModelPart part : PlayerModelPart.values()) {
			layers.add(new Rows.Switch(this, part.getName().getString(), null, new BoolBinding() {
				@Override
				public boolean get() {
					return options.isModelPartEnabled(part);
				}

				@Override
				public void set(boolean value) {
					options.setModelPart(part, value);
				}
			}));
		}
		out.add(SettingsPage.Section.of("Skin Layers", layers));
		out.add(SettingsPage.Section.of("Hand", List.of(option(options.mainHand()))));
	}

	private void languagePage(List<SettingsPage.Section> out) {
		LanguageManager languages = minecraft.getLanguageManager();
		String selected = languages.getSelected();
		pendingLanguage = selected;
		out.add(SettingsPage.Section.of(null, List.<SettingRow>of(
				new Rows.Action(this, "", () -> pendingLabel, "Apply", PillButton.Kind.PRIMARY, this::applyLanguage)
						.visibleWhen(() -> !pendingLanguage.equals(languages.getSelected())))));
		out.add(SettingsPage.Section.of("Font", List.of(
				option(options.forceUnicodeFont()),
				option(options.japaneseGlyphVariants()))));
		List<SettingRow> list = new ArrayList<>();
		for (Map.Entry<String, LanguageInfo> entry : languages.getLanguages().entrySet()) {
			String code = entry.getKey();
			LanguageInfo info = entry.getValue();
			list.add(new Rows.Language(this, code, info.name(), info.region(), code.equals(selected), () -> pendingLanguage, () -> {
				pendingLanguage = code;
				pendingLabel = "Switch to " + info.name() + " (" + info.region() + ")";
			}));
		}
		out.add(SettingsPage.Section.of("Languages", list).footnote(I18n.get("options.languageAccuracyWarning")));
	}

	/** LanguageSelectScreen#onDone. */
	private void applyLanguage() {
		LanguageManager languages = minecraft.getLanguageManager();
		if (!pendingLanguage.equals(languages.getSelected())) {
			languages.setSelected(pendingLanguage);
			options.languageCode = pendingLanguage;
			minecraft.reloadResourcePacks();
		}
	}

	private void accessibilityPage(List<SettingsPage.Section> out) {
		out.add(SettingsPage.Section.of("Narration & Text", List.of(
				option(options.narrator()),
				option(options.narratorHotkey()),
				option(options.showSubtitles()),
				option(options.highContrast())
						.enabledWhen(() -> minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")),
				option(options.highContrastBlockOutline()),
				option(options.textBackgroundOpacity()),
				option(options.backgroundForChatOnly()),
				option(options.chatOpacity()),
				option(options.chatLineSpacing()),
				option(options.chatDelay()),
				option(options.notificationDisplayTime()))));
		out.add(SettingsPage.Section.of("Motion & Effects", List.of(
				option(options.bobView()),
				option(options.screenEffectScale()),
				option(options.fovEffectScale()),
				option(options.darknessEffectScale()),
				option(options.damageTiltStrength()),
				option(options.glintSpeed()),
				option(options.glintStrength()),
				option(options.hideLightningFlash()),
				option(options.rotateWithMinecart()).enabledWhen(() -> minecraft.level != null
						&& minecraft.level.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS)))));
		out.add(SettingsPage.Section.of("Menus", List.of(
				option(options.menuBackgroundBlurriness()),
				option(options.darkMojangStudiosBackground()),
				option(options.panoramaSpeed()),
				option(options.hideSplashTexts()))));
		out.add(SettingsPage.Section.of(null, List.of(
				new Rows.Link(this, I18n.get("options.controls"), false, () -> selectPage(pageNamed("Controls"))),
				new Rows.Link(this, I18n.get("options.accessibility.link"), true,
						() -> ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.ACCESSIBILITY_HELP)))));
	}

	private @Nullable SettingsPage pageNamed(String name) {
		for (SettingsPage page : pages) {
			if (page.name.equals(name)) {
				return page;
			}
		}
		return null;
	}

	// ---- Shortcuts -----------------------------------------------------------------------------

	private void openResourcePacks() {
		minecraft.gui.setScreen(new PacksScreen(minecraft.getResourcePackRepository(), repository -> {
			options.updateResourcePacks(repository);
			minecraft.gui.setScreen(this);
		}, minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title")));
	}

	private void openShaderPacks() {
		openReflective("net.irisshaders.iris.gui.screen.ShaderPackScreen", false);
	}

	private void openMods() {
		openReflective("com.terraformersmc.modmenu.gui.ModsScreen", false);
	}

	private void openSodium() {
		openReflective("net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen", true);
	}

	/** Optional mods' screens, without a compile-time dependency. */
	private void openReflective(String className, boolean factory) {
		try {
			Class<?> type = Class.forName(className);
			Object screen = factory
					? type.getMethod("createScreen", Screen.class).invoke(null, this)
					: type.getConstructor(Screen.class).newInstance(this);
			minecraft.gui.setScreen((Screen) screen);
		} catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
			LiquidClient.LOGGER.warn("Couldn't open {}", className, e);
		}
	}

	private void openFolder(Path path) {
		try {
			Files.createDirectories(path);
		} catch (Exception ignored) {
			// Opening still works for existing folders; nothing else to do.
		}
		Blaze3D.openPath(path);
	}

	// ---- Option side effects -------------------------------------------------------------------

	/** Called after any row changed a value. */
	void optionChanged() {
		GpuWarnlistManager gpu = minecraft.getGpuWarnlistManager();
		if (gpu.isShowingWarning() && !hasSheet()) {
			showGpuWarning(gpu);
		}
	}

	/** VideoSettingsScreen#mouseClicked: improved transparency on a warn-listed GPU. */
	private void showGpuWarning(GpuWarnlistManager gpu) {
		Component transparency = Component.translatable("options.improvedTransparency");
		StringBuilder message = new StringBuilder(
				Component.translatable("options.graphics.warning.message", transparency, transparency).getString());
		String renderer = gpu.getRendererWarnings();
		if (renderer != null) {
			message.append('\n').append(I18n.get("options.graphics.warning.renderer", renderer));
		}
		String vendor = gpu.getVendorWarnings();
		if (vendor != null) {
			message.append('\n').append(I18n.get("options.graphics.warning.vendor", vendor));
		}
		String version = gpu.getVersionWarnings();
		if (version != null) {
			message.append('\n').append(I18n.get("options.graphics.warning.version", version));
		}
		Runnable cancel = () -> {
			gpu.dismissWarning();
			options.improvedTransparency().set(false);
		};
		openSheet(new Sheet(this, I18n.get("options.graphics.warning.title"))
				.icon(ICON_ALERT, Ui.WARNING)
				.message(message.toString())
				.button(I18n.get("options.graphics.warning.cancel"), PillButton.Kind.SECONDARY, cancel)
				.button(I18n.get("options.graphics.warning.accept"), PillButton.Kind.PRIMARY, () -> {
					options.improvedTransparency().set(true);
					minecraft.levelExtractor.allChanged();
					gpu.dismissWarning();
				})
				.onCancel(cancel));
	}

	@Override
	public void removed() {
		super.removed();
		options.save();
		// VideoSettingsScreen#removed: mipmap / filtering changes need a texture reload.
		if (options.mipmapLevels().get() != oldMipmaps || options.maxAnisotropyBit().get() != oldAnisotropy
				|| options.textureFiltering().get() != oldFiltering) {
			minecraft.updateMaxMipLevel(options.mipmapLevels().get());
			minecraft.delayTextureReload();
			oldMipmaps = options.mipmapLevels().get();
			oldAnisotropy = options.maxAnisotropyBit().get();
			oldFiltering = options.textureFiltering().get();
		}
	}

	@Override
	public void onClose() {
		minecraft.getWindow().changeFullscreenVideoMode(); // VideoSettingsScreen#onClose
		super.onClose();
	}

	// ---- Search --------------------------------------------------------------------------------

	private void setQuery(String value) {
		String q = value.trim().toLowerCase(Locale.ROOT);
		if (q.equals(query)) {
			return;
		}
		query = q;
		content.reset();
		pageIn.snap(0f);
		if (q.isEmpty()) {
			results = List.of();
			return;
		}
		List<SettingsPage.Section> found = new ArrayList<>();
		Set<Object> seen = new HashSet<>();
		int count = 0;
		for (SettingsPage page : pages) {
			if (page.isShortcut()) {
				continue;
			}
			List<SettingRow> rows = new ArrayList<>();
			for (SettingsPage.Section section : page.sections()) {
				for (SettingRow row : section.rows()) {
					if (row.matches(q) && seen.add(row.identity())) {
						rows.add(row);
					}
				}
			}
			if (!rows.isEmpty()) {
				found.add(SettingsPage.Section.of(page.name, rows));
				count += rows.size();
			}
		}
		results = found;
		searchSummary.setText(count == 0 ? "" : count + (count == 1 ? " result for “" : " results for “") + value.trim() + "”");
	}

	private void selectPage(@Nullable SettingsPage page) {
		if (page == null) {
			return;
		}
		if (page.isShortcut()) {
			page.open();
			return;
		}
		if (!query.isEmpty()) {
			search.setValue("");
		}
		if (page != current) {
			current = page;
			lastPage = page.name;
			content.reset();
			pageIn.snap(0f);
		}
	}

	// ---- Rendering -----------------------------------------------------------------------------

	@Override
	protected void extractBody(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		extractSidebar(g, mouseX, mouseY);
		extractContent(g, mouseX, mouseY);
	}

	private float sidebarListTop() {
		return sbY + 8f + Ui.CONTROL_H + 8f;
	}

	private void extractSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		RenderUtils.fill(g, sbX, sbY, sbW, sbH, 12f, 0x0DFFFFFF);
		RenderUtils.light(g, sbX, sbY, sbW, 40f, 12f, 0x0AFFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, sbX, sbY, sbW, sbH, 12f, HighDpiEngine.px(), 0x1FFFFFFF, 0x0AFFFFFF);

		float top = sidebarListTop();
		float height = sbY + sbH - 6f - top;
		float total = 0f;
		for (int i = 0; i < pages.size(); i++) {
			total += SIDEBAR_ROW + (i > 0 && pages.get(i).groupStart ? 9f : 0f);
		}
		float offset = sidebar.begin(sbX, top, sbW, height, total + 2f);
		boolean inside = sidebar.contains(mouseX, mouseY);
		sidebar.scissor(g);

		// Selection capsule glides between rows.
		float y = top - offset;
		float selectedY = Float.NaN;
		for (int i = 0; i < pages.size(); i++) {
			SettingsPage page = pages.get(i);
			if (i > 0 && page.groupStart) {
				y += 9f;
			}
			if (page == current) {
				selectedY = y + offset;
			}
			y += SIDEBAR_ROW;
		}
		boolean searching = !query.isEmpty();
		if (!Float.isNaN(selectedY)) {
			if (!indicatorPlaced) {
				indicator.snap(selectedY);
				indicatorPlaced = true;
			}
			float iy = indicator.target(selectedY).update(dt) - offset;
			RenderUtils.fill(g, sbX + 6f, iy + 1f, sbW - 12f, SIDEBAR_ROW - 2f, 7f, searching ? 0x1FFFFFFF : Ui.ACCENT);
		}

		y = top - offset;
		for (int i = 0; i < pages.size(); i++) {
			SettingsPage page = pages.get(i);
			if (i > 0 && page.groupStart) {
				y += 9f;
			}
			boolean over = inside && mouseX >= sbX + 6f && mouseX < sbX + sbW - 6f && mouseY >= y && mouseY < y + SIDEBAR_ROW;
			float hv = page.hover.target(over).update(dt);
			boolean selected = page == current && !searching;
			if (!selected && hv > 0.01f) {
				RenderUtils.fill(g, sbX + 6f, y + 1f, sbW - 12f, SIDEBAR_ROW - 2f, 7f, ColorUtil.fade(Ui.FILL_HOVER, hv * 1.6f));
			}
			float tile = 16f;
			float tx = sbX + 12f;
			float ty = y + (SIDEBAR_ROW - tile) * 0.5f;
			Ui.tile(g, tx, ty, tile, page.color);
			IconFont.draw(g, page.icon, tx + tile * 0.5f, ty + tile * 0.5f, 9.5f, 0xFFFFFFFF, 0);
			page.sidebarLabel.maxWidth(sbW - 52f);
			LiquidText.draw(g, page.sidebarLabel, tx + tile + 7f, y + (SIDEBAR_ROW - page.sidebarLabel.capHeight()) * 0.5f,
					selected ? 0xFFFFFFFF : Ui.TEXT, false);
			if (page.isShortcut() && hv > 0.01f) {
				IconFont.draw(g, ICON_EXTERNAL, sbX + sbW - 16f, y + SIDEBAR_ROW * 0.5f, 8f, ColorUtil.fade(Ui.TEXT_3, hv), 0);
			}
			y += SIDEBAR_ROW;
		}
		g.disableScissor();
		sidebar.scrollbar(g, mouseX, mouseY);
	}

	private List<SettingsPage.Section> shownSections() {
		return query.isEmpty() ? current.sections() : results;
	}

	private float headerHeight() {
		if (!query.isEmpty()) {
			return 16f + 12f + (results.isEmpty() ? 0f : 14f) + 14f;
		}
		return 16f + 34f + 14f;
	}

	private float contentHeight(List<SettingsPage.Section> sections) {
		float total = headerHeight();
		for (SettingsPage.Section section : sections) {
			if (!section.hasVisibleRows()) {
				continue;
			}
			if (section.title() != null) {
				total += 14f;
			}
			for (SettingRow row : section.rows()) {
				if (row.visible()) {
					total += row.height();
				}
			}
			if (!section.footer().isEmpty()) {
				total += 5f + section.footer().size() * 9.5f;
			}
			total += SECTION_GAP;
		}
		return total + 4f;
	}

	private void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		List<SettingsPage.Section> sections = shownSections();
		float offset = content.begin(cX, cY, cW, cH, contentHeight(sections));
		boolean inside = content.contains(mouseX, mouseY) || captured != null;
		int mx = inside ? mouseX : -10000;
		int my = inside ? mouseY : -10000;
		float a = pageIn.target(1f).update(dt);
		float eased = 1f - (1f - a) * (1f - a);
		drawn.clear();

		content.scissor(g);
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(0f, (1f - eased) * 8f);
		RenderUtils.pushAlpha(Math.min(1f, a * 1.3f));
		float x = cX + CONTENT_PAD;
		float w = cW - CONTENT_PAD * 2f;
		float y = cY - offset + 16f;

		if (query.isEmpty()) {
			Ui.tile(g, x, y, 34f, current.color);
			IconFont.draw(g, current.icon, x + 17f, y + 17f, 18f, 0xFFFFFFFF, 0);
			current.titleLabel.maxWidth(w - 46f);
			if (current.descriptionLines.isEmpty()) {
				LiquidText.draw(g, current.titleLabel, x + 46f, y + 17f - current.titleLabel.capHeight() * 0.5f, Ui.TEXT, false);
			} else {
				LiquidText.draw(g, current.titleLabel, x + 46f, y + 6f, Ui.TEXT, false);
				TextLabel line = current.descriptionLines.getFirst();
				line.maxWidth(w - 46f);
				LiquidText.draw(g, line, x + 46f, y + 21f, Ui.TEXT_2, false);
			}
			y += 34f + 14f;
		} else {
			LiquidText.draw(g, searchTitle, x, y, Ui.TEXT, false);
			y += 12f;
			if (!results.isEmpty()) {
				LiquidText.draw(g, searchSummary, x, y + 4f, Ui.TEXT_2, false);
				y += 14f;
			}
			y += 14f;
			if (results.isEmpty()) {
				float cx = cX + cW * 0.5f;
				float cy = cY + cH * 0.5f - 10f;
				IconFont.draw(g, ICON_SEARCH, cx, cy - 14f, 22f, Ui.TEXT_3, 0);
				LiquidText.drawCentered(g, noResults, cx, cy + 8f, Ui.TEXT, false);
				LiquidText.drawCentered(g, noResultsHint, cx, cy + 22f, Ui.TEXT_2, false);
			}
		}

		for (SettingsPage.Section section : sections) {
			if (!section.hasVisibleRows()) {
				continue;
			}
			if (section.title() != null) {
				LiquidText.draw(g, section.title(), x + 4f, y + 2f, Ui.TEXT_3, false);
				y += 14f;
			}
			float groupH = 0f;
			for (SettingRow row : section.rows()) {
				if (row.visible()) {
					groupH += row.height();
				}
			}
			Ui.group(g, x, y, w, groupH);
			float ry = y;
			boolean first = true;
			for (SettingRow row : section.rows()) {
				if (!row.visible()) {
					continue;
				}
				if (!first) {
					Ui.separator(g, x + 12f, ry, w - 24f);
				}
				first = false;
				float rh = row.height();
				if (ry + rh >= cY && ry <= cY + cH) {
					row.extract(g, x, ry, w, mx, my);
					drawn.add(row);
				}
				ry += rh;
			}
			y += groupH;
			if (!section.footer().isEmpty()) {
				y += 5f;
				for (TextLabel line : section.footer()) {
					line.maxWidth(w - 8f);
					LiquidText.draw(g, line, x + 4f, y, Ui.TEXT_3, false);
					y += 9.5f;
				}
			}
			y += SECTION_GAP;
		}
		RenderUtils.popAlpha();
		pose.popMatrix();
		g.disableScissor();
		content.scrollbar(g, mouseX, mouseY);
	}

	// ---- Input ---------------------------------------------------------------------------------

	private @Nullable SettingsPage pageAt(double mx, double my) {
		if (!sidebar.contains(mx, my) || mx < sbX + 6f || mx >= sbX + sbW - 6f) {
			return null;
		}
		float y = sidebarListTop() - sidebar.offset();
		for (int i = 0; i < pages.size(); i++) {
			SettingsPage page = pages.get(i);
			if (i > 0 && page.groupStart) {
				y += 9f;
			}
			if (my >= y && my < y + SIDEBAR_ROW) {
				return page;
			}
			y += SIDEBAR_ROW;
		}
		return null;
	}

	@Override
	protected boolean bodyClicked(double mx, double my, int button, boolean doubleClick) {
		SettingsPage page = pageAt(mx, my);
		if (page != null && button == InputConstants.MOUSE_BUTTON_LEFT) {
			if (page != current || !query.isEmpty()) {
				UiSounds.click();
			}
			selectPage(page);
			return true;
		}
		if (!content.contains(mx, my)) {
			return false;
		}
		for (SettingRow row : drawn) {
			if (row.mouseClicked(mx, my, button)) {
				captured = row;
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean bodyDragged(double mx, double my, int button, double dx, double dy) {
		return captured != null && captured.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	protected boolean bodyReleased(double mx, double my, int button) {
		SettingRow row = captured;
		captured = null;
		return row != null && row.mouseReleased(mx, my, button);
	}

	@Override
	protected boolean bodyScrolled(double mx, double my, double amount) {
		return sidebar.scroll(mx, my, amount) || content.scroll(mx, my, amount);
	}

	@Override
	protected boolean bodyKey(KeyEvent event) {
		if (event.hasControlDown() && event.key() == InputConstants.KEY_F) {
			focus(search);
			return true;
		}
		if (query.isEmpty() && (event.isUp() || event.isDown())) {
			int index = pages.indexOf(current);
			int step = event.isDown() ? 1 : -1;
			for (int i = index + step; i >= 0 && i < pages.size(); i += step) {
				if (!pages.get(i).isShortcut()) {
					UiSounds.click();
					selectPage(pages.get(i));
					break;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!hasSheet() && !search.focused() && event.codepoint() > 32) {
			focus(search);
		}
		return super.charTyped(event);
	}
}
