package net.client.visuals.gui.packs;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.ScrollArea;
import net.client.visuals.gui.window.Sheet;
import net.client.visuals.gui.window.TextField;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.gui.window.WindowScreen;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GpuImage;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.StyledText;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackDetector;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Util;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resource (and data) packs as a Liquid window: Available and Selected columns of pack cards with
 * icon, full-colour title and description, hover buttons to enable, disable and reorder, a lock on
 * required packs, an "Incompatible" pill (with vanilla's confirmation before enabling), search,
 * drag-and-drop install and the pack folder. Selection, ordering and committing go through
 * vanilla's {@link PackSelectionModel}, so behaviour matches {@code PackSelectionScreen} exactly
 * (fixed positions, required packs, high-contrast handling, applying on close). The folder is
 * watched like vanilla does, so packs copied in from outside appear by themselves.
 */
public final class PacksScreen extends WindowScreen {
	private static final float ROW_H = 46f;
	private static final float PITCH = 50f;
	private static final float FOOTER_H = 46f;
	private static final float HEADER_H = 24f;
	private static final float COL_GAP = 10f;
	private static final Identifier NO_ICON = LiquidClient.id("dynamic/pack_icon_none");
	private static final int ICON_FOLDER = 0xE247;
	private static final int ICON_ADD = 0xE13D;
	private static final int ICON_REMOVE = 0xE11C;
	private static final int ICON_UP = 0xE070;
	private static final int ICON_DOWN = 0xE06D;
	private static final int ICON_LOCK = 0xE10B;
	private static final int ICON_LAYERS = 0xE529;
	private static final int ICON_DOWNLOAD = 0xE0B2;
	private static final int ICON_ALERT = 0xE193;
	private static final int ICON_SEARCH = 0xE151;

	private final PackSelectionModel model;
	private final Path packDir;
	private final Map<String, PackRow> rows = new HashMap<>();
	private final List<PackRow> available = new ArrayList<>();
	private final List<PackRow> selected = new ArrayList<>();
	private final Map<String, Identifier> icons = new HashMap<>();
	private final Map<Identifier, TextureSetup> iconSetups = new HashMap<>();
	private final ScrollArea availableScroll = new ScrollArea();
	private final ScrollArea selectedScroll = new ScrollArea();
	private final TextLabel availableTitle = Ui.sectionLabel("Available");
	private final TextLabel selectedTitle = Ui.sectionLabel("Selected");
	private final TextLabel availableCount = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel selectedCount = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel dropHint = new TextLabel("Drop pack files into this window to add them", LiquidText.TextStyle.CAPTION);
	private final TextLabel emptyAvailable = new TextLabel("All packs are enabled", LiquidText.TextStyle.BODY);
	private final TextLabel emptySelected = new TextLabel("No packs enabled", LiquidText.TextStyle.BODY);
	private final TextLabel noMatches = new TextLabel("No matches", LiquidText.TextStyle.BODY);
	private final List<TextLabel> folderTip;
	private @Nullable WatchService watcher;
	private long ticksToReload;
	private boolean committed;
	private int enabledCount;
	private String query = "";
	private TextField search;
	private PillButton folder;
	private PillButton done;
	private float colY;
	private float colH;
	private float leftX;
	private float rightX;
	private float colW;

	public PacksScreen(PackRepository repository, Consumer<PackRepository> output, Path packDir, Component title) {
		super(title, null);
		this.model = new PackSelectionModel(this::onListChanged, this::icon, repository, output);
		this.packDir = packDir;
		this.watcher = openWatcher(packDir);
		this.folderTip = Ui.wrap(I18n.get("pack.folderInfo"), 220f, LiquidText.TextStyle.BODY);
	}

	// ---- Layout --------------------------------------------------------------------------------

	@Override
	protected float preferredWidth() {
		return 640f;
	}

	@Override
	protected float preferredHeight() {
		return 410f;
	}

	@Override
	protected void layoutWindow(boolean first) {
		if (first) {
			search = new TextField("Search", true).onChange(value -> {
				query = value.trim().toLowerCase(Locale.ROOT);
				populate(null);
			});
			folder = new PillButton("Open Folder", ICON_FOLDER, PillButton.Kind.SECONDARY, () -> Blaze3D.openPath(packDir));
			done = new PillButton("Done", 0, PillButton.Kind.PRIMARY, this::onClose).enabledWhen(() -> enabledCount > 0);
			widgets.add(search);
			widgets.add(folder);
			widgets.add(done);
			reload();
		}
		float right = wx + ww - 12f;
		float ty = wy + (TITLE_H - Ui.CONTROL_H) * 0.5f;
		boolean roomy = right - folder.naturalWidth() - 8f - 120f > titleRight() + 12f;
		folder.setBounds(roomy ? right - folder.naturalWidth() : -10000f, ty, folder.naturalWidth(), Ui.CONTROL_H);
		if (roomy) {
			right -= folder.naturalWidth() + 8f;
		}
		float searchW = Math.clamp(right - titleRight() - 12f, 90f, 170f);
		search.setBounds(right - searchW, ty, searchW, Ui.CONTROL_H);

		float dw = Math.max(72f, done.naturalWidth());
		done.setBounds(wx + ww - 12f - dw, wy + wh - FOOTER_H + (FOOTER_H - Ui.CONTROL_H) * 0.5f, dw, Ui.CONTROL_H);

		colY = wy + TITLE_H + 10f;
		colH = wh - TITLE_H - FOOTER_H - 10f;
		colW = (ww - 20f - COL_GAP) * 0.5f;
		leftX = wx + 10f;
		rightX = leftX + colW + COL_GAP;
	}

	// ---- Model ---------------------------------------------------------------------------------

	/** PackSelectionScreen#reload: rescan the repository and the folder. */
	private void reload() {
		model.findNewPacks();
		releaseIcons();
		populate(null);
		ticksToReload = 0L;
	}

	private void onListChanged(PackSelectionModel.@Nullable EntryBase moved) {
		populate(moved == null ? null : moved.getId());
	}

	/** Rebuilds both columns, reusing each pack's row (and its animation state) by id. */
	private void populate(@Nullable String movedId) {
		List<PackSelectionModel.Entry> enabled = model.getSelected().toList();
		List<PackSelectionModel.Entry> disabled = model.getUnselected().toList();
		Set<String> present = new HashSet<>();
		fill(selected, enabled, true, movedId, present);
		fill(available, disabled, false, movedId, present);
		rows.keySet().retainAll(present);
		int total = enabled.size() + disabled.size();
		enabledCount = enabled.size();
		selectedCount.setText(Integer.toString(enabled.size()));
		availableCount.setText(Integer.toString(disabled.size()));
		setSubtitle(enabled.size() + " of " + total + " enabled");
	}

	private void fill(List<PackRow> column, List<PackSelectionModel.Entry> entries, boolean enabled, @Nullable String movedId, Set<String> present) {
		column.clear();
		for (PackSelectionModel.Entry entry : entries) {
			present.add(entry.getId());
			PackRow row = rows.get(entry.getId());
			boolean fresh = row == null;
			if (fresh) {
				row = new PackRow();
				rows.put(entry.getId(), row);
			}
			boolean switched = !fresh && row.enabled != enabled;
			row.update(entry, enabled);
			if (!matches(entry)) {
				continue;
			}
			float target = column.size() * PITCH;
			if (fresh || switched || row.hidden) {
				row.slot.snap(target);
				row.appear.snap(entry.getId().equals(movedId) || switched ? 0f : 1f);
				row.from = switched ? (enabled ? -1f : 1f) : 0f;
			}
			row.hidden = false;
			row.slot.target(target);
			column.add(row);
		}
		for (PackRow row : rows.values()) {
			if (row.enabled == enabled && !column.contains(row)) {
				row.hidden = true;
			}
		}
	}

	private boolean matches(PackSelectionModel.Entry entry) {
		return query.isEmpty() || entry.getId().toLowerCase(Locale.ROOT).contains(query)
				|| entry.getTitle().getString().toLowerCase(Locale.ROOT).contains(query)
				|| entry.getDescription().getString().toLowerCase(Locale.ROOT).contains(query);
	}

	private void enable(PackRow row) {
		PackSelectionModel.Entry entry = row.entry;
		if (entry.getCompatibility().isCompatible()) {
			UiSounds.click();
			entry.select();
			return;
		}
		openSheet(new Sheet(this, I18n.get("pack.incompatible.confirm.title"))
				.icon(ICON_ALERT, Ui.WARNING)
				.message(entry.getCompatibility().getConfirmation().getString())
				.button("Cancel", PillButton.Kind.SECONDARY, () -> {
				})
				.button("Enable", PillButton.Kind.PRIMARY, entry::select));
	}

	@Override
	public void onClose() {
		if (!committed) {
			committed = true;
			model.commit(); // the output callback navigates (and applies the packs)
		}
		closeWatcher();
	}

	@Override
	public void removed() {
		super.removed();
		releaseIcons();
	}

	// ---- Icons ---------------------------------------------------------------------------------

	/** PackSelectionScreen#loadPackIcon, uploaded with linear filtering (icons are drawn scaled). */
	private Identifier icon(Pack pack) {
		return icons.computeIfAbsent(pack.getId(), id -> {
			try (PackMetadataResources resources = pack.openMetadata()) {
				IoSupplier<InputStream> resource = resources.getRootResource("pack.png");
				if (resource == null) {
					return NO_ICON;
				}
				Identifier location = LiquidClient.id("pack/" + Util.sanitizeName(id, Identifier::validPathChar) + "/"
						+ Hashing.sha1().hashUnencodedChars(id) + "/icon");
				try (InputStream stream = resource.get(); NativeImage image = NativeImage.read(stream)) {
					GpuImage texture = new GpuImage(location.toString(), image);
					minecraft.getTextureManager().register(location, texture);
					iconSetups.put(location, TextureSetup.singleTexture(texture.view(), texture.linearSampler()));
					return location;
				}
			} catch (Exception e) {
				LiquidClient.LOGGER.warn("Failed to load icon from pack {}", id, e);
				return NO_ICON;
			}
		});
	}

	private void releaseIcons() {
		for (Identifier id : icons.values()) {
			if (id != NO_ICON) {
				minecraft.getTextureManager().release(id);
			}
		}
		icons.clear();
		iconSetups.clear();
		for (PackRow row : rows.values()) {
			row.icon = null;
			row.iconResolved = false;
		}
	}

	// ---- Folder watching (PackSelectionScreen.Watcher) ----------------------------------------

	private static @Nullable WatchService openWatcher(Path dir) {
		try {
			WatchService service = dir.getFileSystem().newWatchService();
			try {
				watch(service, dir);
				try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
					for (Path path : paths) {
						if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
							watch(service, path);
						}
					}
				}
				return service;
			} catch (Exception e) {
				service.close();
				throw e;
			}
		} catch (Exception e) {
			LiquidClient.LOGGER.warn("Failed to initialize pack directory {} monitoring", dir, e);
			return null;
		}
	}

	private static void watch(WatchService service, Path dir) throws IOException {
		dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	}

	private boolean pollWatcher() throws IOException {
		boolean changed = false;
		WatchKey key;
		while (watcher != null && (key = watcher.poll()) != null) {
			for (WatchEvent<?> event : key.pollEvents()) {
				changed = true;
				if (key.watchable() == packDir && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					Path created = packDir.resolve((Path) event.context());
					if (Files.isDirectory(created, LinkOption.NOFOLLOW_LINKS)) {
						watch(watcher, created);
					}
				}
			}
			key.reset();
		}
		return changed;
	}

	private void closeWatcher() {
		if (watcher != null) {
			try {
				watcher.close();
			} catch (Exception ignored) {
				// Nothing to do: it's going away either way.
			}
			watcher = null;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (watcher != null) {
			try {
				if (pollWatcher()) {
					ticksToReload = 20L;
				}
			} catch (IOException e) {
				LiquidClient.LOGGER.warn("Failed to poll for directory {} changes, stopping", packDir);
				closeWatcher();
			}
		}
		if (ticksToReload > 0L && --ticksToReload == 0L) {
			reload();
		}
	}

	// ---- Drag and drop (PackSelectionScreen#onFilesDrop) --------------------------------------

	@Override
	public void onFilesDrop(List<Path> files) {
		String names = files.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
		openSheet(new Sheet(this, I18n.get("pack.dropConfirm"))
				.icon(ICON_DOWNLOAD, Ui.ACCENT)
				.message(names)
				.button("Cancel", PillButton.Kind.SECONDARY, () -> {
				})
				.button("Add", PillButton.Kind.PRIMARY, () -> addPacks(files)));
	}

	private void addPacks(List<Path> files) {
		List<Path> candidates = new ArrayList<>(files.size());
		Set<Path> leftover = new HashSet<>(files);
		PackDetector<Path> detector = new PackDetector<>(minecraft.directoryValidator()) {
			@Override
			protected Path createZipPack(Path content) {
				return content;
			}

			@Override
			protected Path createDirectoryPack(Path content) {
				return content;
			}
		};
		List<ForbiddenSymlinkInfo> issues = new ArrayList<>();
		for (Path path : files) {
			try {
				Path candidate = detector.detectPackResources(path, issues);
				if (candidate == null) {
					LiquidClient.LOGGER.warn("Path {} does not seem like pack", path);
				} else {
					candidates.add(candidate);
					leftover.remove(candidate);
				}
			} catch (IOException e) {
				LiquidClient.LOGGER.warn("Failed to check {} for packs", path, e);
			}
		}
		if (!issues.isEmpty()) {
			minecraft.gui.setScreen(NoticeWithLinkScreen.createPackSymlinkWarningScreen(() -> minecraft.gui.setScreen(this)));
			return;
		}
		if (!candidates.isEmpty()) {
			copyPacks(candidates);
			reload();
		}
		if (!leftover.isEmpty()) {
			String names = leftover.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
			openSheet(new Sheet(this, I18n.get("pack.dropRejected.title"))
					.icon(ICON_ALERT, Ui.WARNING)
					.message(I18n.get("pack.dropRejected.message", names))
					.button("OK", PillButton.Kind.PRIMARY, () -> {
					}));
		}
	}

	private void copyPacks(List<Path> packs) {
		boolean[] failed = {false};
		for (Path pack : packs) {
			try (Stream<Path> contents = Files.walk(pack)) {
				contents.forEach(path -> {
					try {
						Util.copyBetweenDirs(pack.getParent(), packDir, path);
					} catch (IOException e) {
						LiquidClient.LOGGER.warn("Failed to copy pack file from {} to {}", path, packDir, e);
						failed[0] = true;
					}
				});
			} catch (IOException e) {
				LiquidClient.LOGGER.warn("Failed to copy pack file from {} to {}", pack, packDir);
				failed[0] = true;
			}
		}
		if (failed[0]) {
			SystemToast.onPackCopyFailure(minecraft, packDir.toString());
		}
	}

	// ---- Rendering -----------------------------------------------------------------------------

	@Override
	protected void extractBody(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		Ui.separator(g, wx, wy + wh - FOOTER_H, ww);
		float fy = wy + wh - FOOTER_H * 0.5f;
		IconFont.draw(g, ICON_DOWNLOAD, wx + 22f, fy, 10f, Ui.TEXT_3, 0);
		dropHint.maxWidth(done.x() - wx - 50f);
		LiquidText.draw(g, dropHint, wx + 32f, fy - dropHint.capHeight() * 0.5f, Ui.TEXT_3, false);
		if (folder.contains(mouseX, mouseY)) {
			tooltip(folder, folderTip);
		}
		extractColumn(g, leftX, availableTitle, availableCount, available, availableScroll,
				query.isEmpty() ? emptyAvailable : noMatches, mouseX, mouseY);
		extractColumn(g, rightX, selectedTitle, selectedCount, selected, selectedScroll,
				query.isEmpty() ? emptySelected : noMatches, mouseX, mouseY);
	}

	private void extractColumn(GuiGraphicsExtractor g, float x, TextLabel title, TextLabel count, List<PackRow> column,
			ScrollArea scroll, TextLabel empty, int mouseX, int mouseY) {
		Ui.group(g, x, colY, colW, colH);
		LiquidText.draw(g, title, x + 12f, colY + 9f, Ui.TEXT_3, false);
		float cw = count.width() + 10f;
		RenderUtils.fill(g, x + 12f + title.width() + 6f, colY + 6.5f, cw, 11f, 5.5f, 0x1AFFFFFF);
		LiquidText.draw(g, count, x + 12f + title.width() + 11f, colY + 12f - count.capHeight() * 0.5f, Ui.TEXT_2, false);

		float listY = colY + HEADER_H;
		float listH = colH - HEADER_H - 4f;
		float offset = scroll.begin(x, listY, colW, listH, column.size() * PITCH + 4f);
		if (column.isEmpty()) {
			if (!query.isEmpty()) {
				IconFont.draw(g, ICON_SEARCH, x + colW * 0.5f, listY + listH * 0.5f - 12f, 16f, Ui.TEXT_3, 0);
			}
			LiquidText.drawCentered(g, empty, x + colW * 0.5f, listY + listH * 0.5f + (query.isEmpty() ? -3f : 4f), Ui.TEXT_3, false);
			return;
		}
		boolean inside = scroll.contains(mouseX, mouseY);
		scroll.scissor(g);
		float dt = UiFrame.dt;
		for (PackRow row : column) {
			float y = listY + 2f + row.slot.update(dt) - offset;
			if (y + ROW_H >= listY && y <= listY + listH) {
				row.extract(g, x + 4f, y, colW - 8f, inside ? mouseX : -10000, inside ? mouseY : -10000);
			} else {
				row.drawnY = -100000f;
				row.appear.target(1f).update(dt);
			}
		}
		g.disableScissor();
		scroll.scrollbar(g, mouseX, mouseY);
	}

	// ---- Input ---------------------------------------------------------------------------------

	@Override
	protected boolean bodyClicked(double mx, double my, int button, boolean doubleClick) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		ScrollArea scroll = mx < rightX ? availableScroll : selectedScroll;
		if (!scroll.contains(mx, my)) {
			return false;
		}
		for (PackRow row : mx < rightX ? available : selected) {
			if (row.click(mx, my)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean bodyScrolled(double mx, double my, double amount) {
		return availableScroll.scroll(mx, my, amount) || selectedScroll.scroll(mx, my, amount);
	}

	// ---- Row -----------------------------------------------------------------------------------

	private final class PackRow {
		PackSelectionModel.Entry entry;
		boolean enabled;
		boolean hidden;
		final StyledText title = new StyledText(LiquidText.TextStyle.LABEL, LiquidText.TextStyle.LABEL, 1);
		final StyledText description = new StyledText(LiquidText.TextStyle.BODY, LiquidText.TextStyle.LABEL, 2);
		final TextLabel incompatible = new TextLabel("Incompatible", LiquidText.TextStyle.CAPTION);
		List<TextLabel> incompatibleTip = List.of();
		@Nullable TextureSetup icon;
		boolean iconResolved;
		Component titleSource = Component.empty();
		Component descriptionSource = Component.empty();
		final Smooth slot = new Smooth(0f, 16f);
		final Smooth appear = new Smooth(1f, 9f);
		final Smooth hover = new Smooth(0f, Motion.HOVER);
		final Smooth[] buttonHover = {new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER)};
		/** Side the row slides in from after switching columns (-1 left, 1 right). */
		float from;
		float drawnY = -100000f;
		float x;
		float w;
		/** Button centres (x) of this frame: [primary, up, down]; NaN when hidden. */
		final float[] buttons = new float[3];

		void update(PackSelectionModel.Entry entry, boolean enabled) {
			this.entry = entry;
			this.enabled = enabled;
			// Captured once: getExtendedDescription() builds a new component on every call.
			titleSource = entry.getTitle();
			descriptionSource = entry.getExtendedDescription();
			if (!entry.getCompatibility().isCompatible() && incompatibleTip.isEmpty()) {
				incompatibleTip = Ui.wrap(entry.getCompatibility().getDescription().getString(), 220f, LiquidText.TextStyle.BODY);
			}
		}

		void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
			float dt = UiFrame.dt;
			this.x = x;
			this.w = w;
			drawnY = y;
			float in = appear.target(1f).update(dt);
			boolean over = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H;
			float hv = hover.target(over).update(dt);
			float shift = from * (1f - in) * 18f;
			RenderUtils.pushAlpha(in);
			var pose = g.pose();
			pose.pushMatrix();
			pose.translate(shift, 0f);
			Ui.rowHighlight(g, x, y, w, ROW_H, hv, 0f);

			float size = ROW_H - 12f;
			float ix = x + 6f;
			float iy = y + 6f;
			if (!iconResolved) {
				iconResolved = true;
				Identifier id = entry.getIconTexture();
				icon = id == NO_ICON ? null : iconSetups.get(id);
			}
			if (icon != null) {
				RenderUtils.shadow(g, ix, iy + 1f, size, size, 8f, 3f, 0x59000000);
				ImageDraw.rounded(g, icon, ix, iy, size, size, 8f, 0xFFFFFFFF);
			} else {
				Ui.tile(g, ix, iy, size, 0xFF48484A);
				IconFont.draw(g, ICON_LAYERS, ix + size * 0.5f, iy + size * 0.5f, 15f, 0xD9FFFFFF, 0);
			}
			RenderUtils.stroke(g, ix, iy, size, size, 8f, HighDpiEngine.px(), 0x26FFFFFF, 0x0AFFFFFF);

			// Hover buttons on the right: [up][down][remove] when enabled, [add] when available.
			float right = x + w - 10f;
			buttons[0] = Float.NaN;
			buttons[1] = Float.NaN;
			buttons[2] = Float.NaN;
			float cy = y + ROW_H * 0.5f;
			float controls = 0f;
			if (enabled && !entry.canUnselect()) {
				IconFont.draw(g, ICON_LOCK, right - 6f, cy, 10f, Ui.TEXT_3, 0);
				controls = 16f;
			} else if (hv > 0.01f) {
				buttons[0] = right - 10f;
				controls = 24f;
			}
			if (enabled && hv > 0.01f) {
				float bx = right - controls - 10f;
				if (entry.canMoveDown()) {
					buttons[2] = bx;
					bx -= 22f;
				}
				if (entry.canMoveUp()) {
					buttons[1] = bx;
				}
			}
			for (int i = 0; i < 3; i++) {
				float bx = buttons[i];
				if (Float.isNaN(bx)) {
					buttonHover[i].target(0f).update(dt);
					continue;
				}
				boolean overButton = Math.abs(mouseX - bx) <= 10f && Math.abs(mouseY - cy) <= 10f;
				float bh = buttonHover[i].target(overButton).update(dt);
				int fill = i == 0
						? ColorUtil.lerp(enabled ? 0x33FF453A : 0xCC0A84FF, enabled ? 0xFFFF453A : 0xFF409CFF, bh)
						: ColorUtil.lerp(0x1FFFFFFF, 0x40FFFFFF, bh);
				RenderUtils.pushAlpha(hv);
				RenderUtils.circle(g, bx, cy, 9.5f, fill);
				int glyph = i == 0 ? (enabled ? ICON_REMOVE : ICON_ADD) : i == 1 ? ICON_UP : ICON_DOWN;
				IconFont.draw(g, glyph, bx, cy, i == 0 ? 12f : 11f, 0xFFFFFFFF, 0);
				RenderUtils.popAlpha();
			}
			float reserved = 0f;
			for (float b : buttons) {
				if (!Float.isNaN(b)) {
					reserved = Math.max(reserved, right - b + 12f);
				}
			}
			reserved = Math.max(reserved * hv, controls);

			float tx = ix + size + 10f;
			float textW = right - reserved - 6f - tx;
			if (!entry.getCompatibility().isCompatible()) {
				float pw = incompatible.width() + 10f;
				float px = Math.max(tx, right - reserved - pw);
				float py = y + 6f;
				RenderUtils.fill(g, px, py, pw, 12f, 6f, 0x2EFF453A);
				LiquidText.draw(g, incompatible, px + 5f, py + 6f - incompatible.capHeight() * 0.5f, Ui.DESTRUCTIVE, false);
				if (mouseX >= px && mouseX < px + pw && mouseY >= py && mouseY < py + 12f) {
					tooltip(this, incompatibleTip);
				}
				title.set(titleSource, Math.max(20f, px - 6f - tx));
			} else {
				title.set(titleSource, Math.max(20f, textW));
			}
			description.set(descriptionSource, Math.max(20f, textW));
			title.draw(g, tx, y + 8.5f, 0f, Ui.TEXT);
			description.draw(g, tx, y + 20.5f, 10.5f, Ui.TEXT_2);
			pose.popMatrix();
			RenderUtils.popAlpha();
		}

		/** Card click: the hover buttons, otherwise enable / disable the whole pack. */
		boolean click(double mx, double my) {
			if (my < drawnY || my >= drawnY + ROW_H || mx < x || mx >= x + w) {
				return false;
			}
			float cy = drawnY + ROW_H * 0.5f;
			for (int i = 1; i < 3; i++) {
				float bx = buttons[i];
				if (!Float.isNaN(bx) && Math.abs(mx - bx) <= 10f && Math.abs(my - cy) <= 10f) {
					UiSounds.click();
					if (i == 1) {
						entry.moveUp();
					} else {
						entry.moveDown();
					}
					return true;
				}
			}
			if (enabled) {
				if (entry.canUnselect()) {
					UiSounds.click();
					entry.unselect();
				}
			} else if (entry.canSelect()) {
				enable(this);
			}
			return true;
		}
	}
}
