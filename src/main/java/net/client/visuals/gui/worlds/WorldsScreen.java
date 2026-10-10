package net.client.visuals.gui.worlds;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.ScrollArea;
import net.client.visuals.gui.window.Segmented;
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
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Singleplayer as a Liquid window: world cards with their icons, game mode / cheats / version and a
 * relative "last played", live search, Recent / Name sorting, a hover play button, keyboard
 * navigation, and Play / Edit / Re-Create / Delete with the exact vanilla flows behind them
 * (backups, upgrades, symlink checks, data-pack handling). Delete confirms in a sheet.
 */
public final class WorldsScreen extends WindowScreen {
	private static final float ROW_H = 46f;
	private static final float ROW_GAP = 4f;
	private static final float FOOTER_H = 46f;
	private static final int ICON_PLUS = 0xE13D;
	private static final int ICON_PLAY = 0xE13C;
	private static final int ICON_FOLDER = 0xE247;
	private static final int ICON_TRASH = 0xE18E;
	private static final int ICON_EDIT = 0xE1F9;
	private static final int ICON_RECREATE = 0xE144;
	private static final int ICON_MOUNTAIN = 0xE231;
	private static final int ICON_SEARCH = 0xE151;
	private static final int ICON_HEART = 0xE0F2;
	private static final int ICON_ALERT = 0xE193;
	private static final int ICON_LOCK = 0xE10B;
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

	private final ScrollArea scroll = new ScrollArea();
	private final List<Row> rows = new ArrayList<>();
	private final List<Row> visible = new ArrayList<>();
	private final TextLabel loadingLabel = new TextLabel("Loading worlds…", LiquidText.TextStyle.BODY);
	private final TextLabel emptyTitle = new TextLabel("No worlds yet", LiquidText.TextStyle.HEADLINE);
	private final TextLabel emptyText = new TextLabel("Create one to start playing.", LiquidText.TextStyle.BODY);
	private final TextLabel noMatches = new TextLabel("", LiquidText.TextStyle.BODY);
	private final List<TextLabel> folderTip = List.of(new TextLabel("Open Saves Folder", LiquidText.TextStyle.BODY));
	private @Nullable CompletableFuture<List<LevelSummary>> pending;
	private boolean loaded;
	private @Nullable String reselect;
	/** Rows were released when the screen was left; reload on the next show. */
	private boolean stale;
	private @Nullable Row selected;
	private int sort;
	private TextField search;
	private PillButton newWorld;
	private Segmented sorter;
	private PillButton play;
	private PillButton edit;
	private PillButton recreate;
	private PillButton delete;
	private PillButton folder;
	private PillButton emptyCreate;
	private float listX;
	private float listY;
	private float listW;
	private float listH;

	public WorldsScreen(@Nullable Screen parent) {
		super(Component.translatable("liquidclient.worlds.title"), parent);
	}

	// ---- Layout --------------------------------------------------------------------------------

	@Override
	protected float preferredWidth() {
		return 540f;
	}

	@Override
	protected float preferredHeight() {
		return 360f;
	}

	@Override
	protected void layoutWindow(boolean first) {
		if (first) {
			search = new TextField("Search", true).onChange(value -> applyFilter());
			newWorld = new PillButton("New World", ICON_PLUS, PillButton.Kind.PRIMARY, this::createWorld);
			sorter = new Segmented(new String[]{"Recent", "Name"}, 0, index -> {
				sort = index;
				applyFilter();
			});
			play = new PillButton("Play", ICON_PLAY, PillButton.Kind.PRIMARY, this::playSelected)
					.enabledWhen(() -> selected != null && selected.summary.primaryActionActive());
			edit = new PillButton("Edit", ICON_EDIT, PillButton.Kind.SECONDARY, this::editSelected)
					.enabledWhen(() -> selected != null && selected.summary.canEdit());
			recreate = new PillButton("Re-Create", ICON_RECREATE, PillButton.Kind.SECONDARY, this::recreateSelected)
					.enabledWhen(() -> selected != null && selected.summary.canRecreate());
			delete = new PillButton("Delete", ICON_TRASH, PillButton.Kind.DESTRUCTIVE, this::confirmDelete)
					.enabledWhen(() -> selected != null && selected.summary.canDelete());
			folder = new PillButton("", ICON_FOLDER, PillButton.Kind.PLAIN, this::openFolder);
			emptyCreate = new PillButton("Create New World", ICON_PLUS, PillButton.Kind.PRIMARY, this::createWorld);
			widgets.add(search);
			widgets.add(newWorld);
			widgets.add(sorter);
			widgets.add(play);
			widgets.add(edit);
			widgets.add(recreate);
			widgets.add(delete);
			widgets.add(folder);
		}
		if (first || stale) {
			stale = false;
			reload();
		}
		float right = wx + ww - 12f;
		float ty = wy + (TITLE_H - Ui.CONTROL_H) * 0.5f;
		newWorld.setBounds(right - newWorld.naturalWidth(), ty, newWorld.naturalWidth(), Ui.CONTROL_H);
		right -= newWorld.naturalWidth() + 8f;
		float searchW = Math.min(150f, Math.max(90f, right - titleRight() - 12f - sorter.width() - 8f));
		search.setBounds(right - searchW, ty, searchW, Ui.CONTROL_H);
		right -= searchW + 8f;
		boolean showSorter = right - sorter.width() > titleRight() + 8f;
		sorter.setBounds(showSorter ? right - sorter.width() : -10000f, ty, sorter.width(), Ui.CONTROL_H);

		float fy = wy + wh - FOOTER_H + (FOOTER_H - Ui.CONTROL_H) * 0.5f;
		float fx = wx + ww - 12f;
		for (PillButton button : new PillButton[]{play, recreate, edit, delete}) {
			float bw = button.naturalWidth();
			fx -= bw;
			button.setBounds(fx, fy, bw, Ui.CONTROL_H);
			fx -= 6f;
		}
		folder.setBounds(wx + 10f, fy, folder.naturalWidth(), Ui.CONTROL_H);

		listX = wx + 10f;
		listY = wy + TITLE_H + 6f;
		listW = ww - 20f;
		listH = wh - TITLE_H - FOOTER_H - 6f;
	}

	// ---- Data ----------------------------------------------------------------------------------

	private void reload() {
		if (selected != null) {
			reselect = selected.summary.getLevelId();
		}
		closeRows();
		loaded = false;
		selected = null;
		LevelStorageSource source = minecraft.getLevelSource();
		try {
			LevelStorageSource.LevelCandidates candidates = source.findLevelCandidates();
			pending = source.loadLevelSummaries(candidates).exceptionally(t -> {
				minecraft.delayCrash(CrashReport.forThrowable(t, "Couldn't load level list"));
				return List.of();
			});
		} catch (LevelStorageException e) {
			LiquidClient.LOGGER.error("Couldn't load level list", e);
			minecraft.gui.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), e.getMessageComponent()));
		}
	}

	private void poll() {
		CompletableFuture<List<LevelSummary>> job = pending;
		if (loaded || job == null || !job.isDone()) {
			return;
		}
		loaded = true;
		List<LevelSummary> summaries = job.getNow(List.of());
		for (LevelSummary summary : summaries) {
			rows.add(new Row(summary));
		}
		setSubtitle(rows.size() == 1 ? "1 world" : rows.size() + " worlds");
		applyFilter();
		Row initial = visible.isEmpty() ? null : visible.getFirst();
		for (Row row : visible) {
			if (row.summary.getLevelId().equals(reselect)) {
				initial = row;
			}
		}
		reselect = null;
		select(initial);
	}

	private void applyFilter() {
		String q = search == null ? "" : search.value().toLowerCase(Locale.ROOT).trim();
		visible.clear();
		for (Row row : rows) {
			if (q.isEmpty() || row.summary.getLevelName().toLowerCase(Locale.ROOT).contains(q)
					|| row.summary.getLevelId().toLowerCase(Locale.ROOT).contains(q)) {
				visible.add(row);
			}
		}
		if (sort == 1) {
			visible.sort(Comparator.comparing(r -> r.summary.getLevelName().toLowerCase(Locale.ROOT)));
		} else {
			visible.sort(Comparator.comparingLong((Row r) -> r.summary.getLastPlayed()).reversed());
		}
		noMatches.setText("No worlds match “" + (search == null ? "" : search.value()) + "”");
		if (selected != null && !visible.contains(selected)) {
			selected = visible.isEmpty() ? null : visible.getFirst();
		}
	}

	private void closeRows() {
		for (Row row : rows) {
			row.close();
		}
		rows.clear();
		visible.clear();
	}

	@Override
	public void removed() {
		super.removed();
		closeRows();
		stale = true;
	}

	/** After a vanilla flow (edit, re-create, cancelled play): come back; the list reloads on show. */
	private void returnHere() {
		minecraft.gui.setScreen(this);
	}

	// ---- Actions (mirroring WorldSelectionList.WorldListEntry) ---------------------------------

	private void createWorld() {
		CreateWorldScreen.openFresh(minecraft, this::returnHere);
	}

	private void playSelected() {
		if (selected != null) {
			play(selected.summary);
		}
	}

	private void play(LevelSummary summary) {
		if (!summary.primaryActionActive()) {
			return;
		}
		if (summary instanceof LevelSummary.SymlinkLevelSummary) {
			minecraft.gui.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.gui.setScreen(this)));
		} else {
			minecraft.createWorldOpenFlows().openWorld(summary.getLevelId(), this::returnHere);
		}
	}

	private void editSelected() {
		if (selected == null) {
			return;
		}
		String levelId = selected.summary.getLevelId();
		minecraft.setScreenAndShow(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
		LevelStorageSource.LevelStorageAccess access;
		try {
			access = minecraft.getLevelSource().validateAndCreateAccess(levelId);
		} catch (IOException e) {
			SystemToast.onWorldAccessFailure(minecraft, levelId);
			LiquidClient.LOGGER.error("Failed to access level {}", levelId, e);
			returnHere();
			return;
		} catch (ContentValidationException e) {
			LiquidClient.LOGGER.warn("{}", e.getMessage());
			minecraft.gui.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.gui.setScreen(this)));
			return;
		}
		try {
			EditWorldScreen screen = EditWorldScreen.create(minecraft, access, result -> {
				access.safeClose();
				returnHere();
			});
			minecraft.gui.setScreen(screen);
		} catch (IOException | NbtException | ReportedNbtException e) {
			access.safeClose();
			SystemToast.onWorldAccessFailure(minecraft, levelId);
			LiquidClient.LOGGER.error("Failed to load world data {}", levelId, e);
			returnHere();
		}
	}

	private void recreateSelected() {
		if (selected == null) {
			return;
		}
		minecraft.setScreenAndShow(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
		try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().validateAndCreateAccess(selected.summary.getLevelId())) {
			Pair<LevelSettings, WorldCreationContext> recreated = minecraft.createWorldOpenFlows().recreateWorldData(access);
			LevelSettings settings = recreated.getFirst();
			WorldCreationContext context = recreated.getSecond();
			Path dataPackDir = CreateWorldScreen.createTempDataPackDirFromExistingWorld(access.getLevelPath(LevelResource.DATAPACK_DIR), minecraft);
			context.validate();
			if (context.options().isOldCustomizedWorld()) {
				minecraft.gui.setScreen(new ConfirmScreen(
						result -> minecraft.gui.setScreen(result
								? CreateWorldScreen.createFromExisting(minecraft, this::returnHere, settings, context, dataPackDir)
								: this),
						Component.translatable("selectWorld.recreate.customized.title"),
						Component.translatable("selectWorld.recreate.customized.text"),
						CommonComponents.GUI_PROCEED,
						CommonComponents.GUI_CANCEL));
			} else {
				minecraft.gui.setScreen(CreateWorldScreen.createFromExisting(minecraft, this::returnHere, settings, context, dataPackDir));
			}
		} catch (ContentValidationException e) {
			LiquidClient.LOGGER.warn("{}", e.getMessage());
			minecraft.gui.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.gui.setScreen(this)));
		} catch (Exception e) {
			LiquidClient.LOGGER.error("Unable to recreate world", e);
			minecraft.gui.setScreen(new AlertScreen(() -> minecraft.gui.setScreen(this),
					Component.translatable("selectWorld.recreate.error.title"), Component.translatable("selectWorld.recreate.error.text")));
		}
	}

	private void confirmDelete() {
		Row row = selected;
		if (row == null) {
			return;
		}
		openSheet(new Sheet(this, "Delete “" + row.summary.getLevelName() + "”?")
				.icon(ICON_TRASH, Ui.DESTRUCTIVE)
				.message("This world will be gone forever (a long time!). This can't be undone.")
				.button("Cancel", PillButton.Kind.SECONDARY, () -> {
				})
				.button("Delete", PillButton.Kind.DANGER, () -> deleteWorld(row)));
	}

	private void deleteWorld(Row row) {
		String levelId = row.summary.getLevelId();
		try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().createAccess(levelId)) {
			access.deleteLevel();
		} catch (IOException e) {
			SystemToast.onWorldDeleteFailure(minecraft, levelId);
			LiquidClient.LOGGER.error("Failed to delete world {}", levelId, e);
		}
		reload();
	}

	private void openFolder() {
		Blaze3D.openPath(minecraft.getLevelSource().getBaseDir());
	}

	private void select(@Nullable Row row) {
		selected = row;
		if (row != null) {
			int index = visible.indexOf(row);
			if (index >= 0) {
				scroll.reveal(index * (ROW_H + ROW_GAP), index * (ROW_H + ROW_GAP) + ROW_H);
			}
		}
	}

	// ---- Rendering -----------------------------------------------------------------------------

	@Override
	protected void extractBody(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		poll();
		float content = visible.size() * (ROW_H + ROW_GAP);
		float offset = scroll.begin(listX, listY, listW, listH, content + 4f);
		Ui.separator(g, wx, wy + wh - FOOTER_H, ww);
		if (folder.contains(mouseX, mouseY)) {
			tooltip(folder, folderTip);
		}

		if (!loaded) {
			float cx = wx + ww * 0.5f;
			float cy = listY + listH * 0.5f;
			Ui.spinner(g, cx, cy - 8f, 8f, 1.6f, 0xFFFFFFFF);
			LiquidText.drawCentered(g, loadingLabel, cx, cy + 10f, Ui.TEXT_2, false);
			return;
		}
		if (rows.isEmpty()) {
			extractEmpty(g, mouseX, mouseY);
			return;
		}
		if (visible.isEmpty()) {
			IconFont.draw(g, ICON_SEARCH, wx + ww * 0.5f, listY + listH * 0.5f - 12f, 18f, Ui.TEXT_3, 0);
			LiquidText.drawCentered(g, noMatches, wx + ww * 0.5f, listY + listH * 0.5f + 6f, Ui.TEXT_2, false);
			return;
		}

		boolean inList = scroll.contains(mouseX, mouseY);
		scroll.scissor(g);
		float y = listY + 2f - offset;
		for (Row row : visible) {
			if (y + ROW_H >= listY && y <= listY + listH) {
				row.extract(g, listX, y, listW, inList ? mouseX : -10000, inList ? mouseY : -10000);
			} else {
				row.idle();
			}
			y += ROW_H + ROW_GAP;
		}
		g.disableScissor();
		scroll.scrollbar(g, mouseX, mouseY);
	}

	private void extractEmpty(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float cx = wx + ww * 0.5f;
		float cy = listY + listH * 0.5f - 16f;
		Ui.tile(g, cx - 24f, cy - 40f, 48f, 0xFF30D158);
		IconFont.draw(g, ICON_MOUNTAIN, cx, cy - 16f, 24f, 0xFFFFFFFF, 0);
		LiquidText.drawCentered(g, emptyTitle, cx, cy + 16f, Ui.TEXT, false);
		LiquidText.drawCentered(g, emptyText, cx, cy + 30f, Ui.TEXT_2, false);
		float bw = emptyCreate.naturalWidth();
		emptyCreate.setBounds(cx - bw * 0.5f, cy + 46f, bw, Ui.CONTROL_H);
		emptyCreate.extract(g, mouseX, mouseY);
	}

	// ---- Input ---------------------------------------------------------------------------------

	private @Nullable Row rowAt(double mx, double my) {
		if (!scroll.contains(mx, my)) {
			return null;
		}
		float y = listY + 2f - scroll.offset();
		for (Row row : visible) {
			if (my >= y && my < y + ROW_H) {
				return row;
			}
			y += ROW_H + ROW_GAP;
		}
		return null;
	}

	@Override
	protected boolean bodyClicked(double mx, double my, int button, boolean doubleClick) {
		if (loaded && rows.isEmpty() && emptyCreate.mouseClicked(mx, my, button)) {
			return true;
		}
		Row row = rowAt(mx, my);
		if (row == null || button != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		if (row.overPlay(mx, my) || doubleClick) {
			UiSounds.click();
			play(row.summary);
			return true;
		}
		if (selected != row) {
			UiSounds.click();
		}
		select(row);
		return true;
	}

	@Override
	protected boolean bodyReleased(double mx, double my, int button) {
		return loaded && rows.isEmpty() && emptyCreate.mouseReleased(mx, my, button);
	}

	@Override
	protected boolean bodyScrolled(double mx, double my, double amount) {
		return scroll.scroll(mx, my, amount);
	}

	@Override
	protected boolean bodyKey(KeyEvent event) {
		if (visible.isEmpty()) {
			return false;
		}
		int index = selected == null ? -1 : visible.indexOf(selected);
		if (event.isDown()) {
			select(visible.get(Math.min(visible.size() - 1, index + 1)));
			return true;
		}
		if (event.isUp()) {
			select(visible.get(Math.max(0, index - 1)));
			return true;
		}
		if ((event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) && selected != null) {
			play(selected.summary);
			return true;
		}
		if (event.key() == InputConstants.KEY_DELETE && selected != null && selected.summary.canDelete()) {
			confirmDelete();
			return true;
		}
		if (event.hasControlDown() && event.key() == InputConstants.KEY_F) {
			focus(search);
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		// Type-to-search: the first printable key focuses the search field.
		if (!hasSheet() && !search.focused() && event.codepoint() > 32) {
			focus(search);
		}
		return super.charTyped(event);
	}

	// ---- Row -----------------------------------------------------------------------------------

	private final class Row {
		final LevelSummary summary;
		final FaviconTexture icon;
		@Nullable TextureSetup iconSetup;
		final TextLabel name;
		final TextLabel detail;
		final TextLabel meta;
		final TextLabel badge;
		final int badgeColor;
		final int badgeIcon;
		final int tileColor;
		final Smooth hover = new Smooth(0f, Motion.HOVER);
		final Smooth chosen = new Smooth(0f, Motion.HOVER);
		final Smooth playHover = new Smooth(0f, Motion.HOVER);
		private float px;
		private float py;

		Row(LevelSummary summary) {
			this.summary = summary;
			this.icon = FaviconTexture.forWorld(minecraft.getTextureManager(), summary.getLevelId());
			loadIcon();
			this.name = new TextLabel(summary.getLevelName(), LiquidText.TextStyle.LABEL);
			// Hardcore is shown as a badge; the line names the game mode.
			StringBuilder detailText = new StringBuilder(summary.getGameMode().getShortDisplayName().getString());
			if (summary.hasCommands()) {
				detailText.append(" · Cheats");
			}
			if (summary.isExperimental()) {
				detailText.append(" · Experimental");
			}
			this.detail = new TextLabel(detailText.toString(), LiquidText.TextStyle.BODY);
			this.meta = new TextLabel(metaText(summary), LiquidText.TextStyle.CAPTION);
			String badgeText = "";
			int color = 0;
			int glyph = 0;
			if (summary.isLocked()) {
				badgeText = "In use";
				color = Ui.WARNING;
				glyph = ICON_LOCK;
			} else if (summary.requiresManualConversion() || !summary.isCompatible()) {
				badgeText = "Incompatible";
				color = Ui.DESTRUCTIVE;
				glyph = ICON_ALERT;
			} else if (summary.isDowngrade()) {
				badgeText = "Newer version";
				color = Ui.DESTRUCTIVE;
				glyph = ICON_ALERT;
			} else if (summary.shouldBackup()) {
				badgeText = "Upgrade";
				color = Ui.WARNING;
				glyph = ICON_ALERT;
			} else if (summary.isHardcore()) {
				badgeText = "Hardcore";
				color = Ui.DESTRUCTIVE;
				glyph = ICON_HEART;
			}
			this.badge = new TextLabel(badgeText, LiquidText.TextStyle.CAPTION);
			this.badgeColor = color;
			this.badgeIcon = glyph;
			this.tileColor = ColorUtil.hsv((summary.getLevelId().hashCode() & 0xFFFF) % 360, 0.55f, 0.85f);
		}

		private String metaText(LevelSummary summary) {
			long lastPlayed = summary.getLastPlayed();
			String when;
			if (lastPlayed <= 0L) {
				when = "Never played";
			} else {
				ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastPlayed), ZoneId.systemDefault());
				LocalDate today = LocalDate.now();
				LocalDate day = time.toLocalDate();
				if (day.equals(today)) {
					when = "Today, " + TIME.format(time);
				} else if (day.equals(today.minusDays(1))) {
					when = "Yesterday, " + TIME.format(time);
				} else {
					when = DATE.format(time);
				}
			}
			String version = summary.levelVersion().minecraftVersionName();
			return version.isEmpty() ? when : when + "  ·  " + version;
		}

		private void loadIcon() {
			Path file = summary.getIcon();
			boolean hasIcon = file != null && Files.isRegularFile(file) && !Files.isSymbolicLink(file);
			if (!hasIcon) {
				icon.clear();
				return;
			}
			try (InputStream stream = Files.newInputStream(file)) {
				icon.upload(NativeImage.read(stream));
				AbstractTexture texture = minecraft.getTextureManager().getTexture(icon.textureLocation());
				iconSetup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
			} catch (Throwable t) {
				LiquidClient.LOGGER.error("Invalid icon for world {}", summary.getLevelId(), t);
				icon.clear();
			}
		}

		/** The icon doubles as the play button (like vanilla, and Apple Music artwork). */
		boolean overPlay(double mx, double my) {
			return summary.primaryActionActive() && mx >= px && mx < px + ROW_H - 12f && my >= py && my < py + ROW_H - 12f;
		}

		void idle() {
			hover.target(0f).update(UiFrame.dt);
			playHover.target(0f).update(UiFrame.dt);
		}

		void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
			float dt = UiFrame.dt;
			boolean over = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H;
			float h = hover.target(over).update(dt);
			float s = chosen.target(selected == this).update(dt);
			Ui.rowHighlight(g, x, y, w, ROW_H, h, s);

			float ix = x + 6f;
			float iy = y + 6f;
			float size = ROW_H - 12f;
			px = ix;
			py = iy;
			if (iconSetup != null) {
				RenderUtils.shadow(g, ix, iy + 1f, size, size, 8f, 3f, 0x59000000);
				ImageDraw.rounded(g, iconSetup, ix, iy, size, size, 8f, 0xFFFFFFFF);
			} else {
				Ui.tile(g, ix, iy, size, tileColor);
				IconFont.draw(g, ICON_MOUNTAIN, ix + size * 0.5f, iy + size * 0.5f, 15f, 0xF2FFFFFF, 0);
			}
			float ph = playHover.target(over && overPlay(mouseX, mouseY)).update(dt);
			if (h > 0.01f && summary.primaryActionActive()) {
				RenderUtils.fill(g, ix, iy, size, size, 8f, ColorUtil.fade(0x8C000000, h * (0.7f + 0.3f * ph)));
				float glyph = 13f + 3f * ph;
				IconFont.draw(g, IconFont.FILLED | 0xE037, ix + size * 0.5f + 0.5f, iy + size * 0.5f,
						glyph, ColorUtil.fade(0xFFFFFFFF, h * (0.85f + 0.15f * ph)), 0);
			}
			RenderUtils.stroke(g, ix, iy, size, size, 8f, HighDpiEngine.px(), 0x26FFFFFF, 0x0AFFFFFF);

			float tx = ix + size + 10f;
			float right = x + w - 12f;
			if (!badge.text().isEmpty()) {
				float bw = badge.width() + 18f;
				float bx = right - bw;
				float by = y + ROW_H * 0.5f - 7f;
				RenderUtils.fill(g, bx, by, bw, 14f, 7f, ColorUtil.fade(badgeColor, 0.18f));
				IconFont.draw(g, badgeIcon, bx + 7f, by + 7f, 7.5f, badgeColor, 0);
				LiquidText.draw(g, badge, bx + 12.5f, by + 7f - badge.capHeight() * 0.5f, badgeColor, false);
				right = bx - 8f;
			}
			float textW = right - tx;
			name.maxWidth(textW);
			detail.maxWidth(textW);
			meta.maxWidth(textW);
			LiquidText.draw(g, name, tx, y + 8.5f, Ui.TEXT, false);
			LiquidText.draw(g, detail, tx, y + 20.5f, Ui.TEXT_2, false);
			LiquidText.draw(g, meta, tx, y + 32.5f, Ui.TEXT_3, false);
		}

		void close() {
			if (!icon.isClosed()) {
				icon.close();
			}
		}
	}
}
