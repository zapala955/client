package net.client.visuals.gui.clickgui;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.component.BoolBinding;
import net.client.visuals.gui.component.ModuleCard;
import net.client.visuals.gui.component.SettingComponents;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.gui.window.TextField;
import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The ClickGUI window: macOS title bar with traffic lights (drag handle), a sidebar with a search
 * field and coloured category tiles under a "liquid" selection indicator, and a scrollable card grid
 * fed by the {@link ModuleManager}.
 *
 * <pre>
 *  ┌─● ● ●──────────── Liquid ─────── [Edit HUD] [Right Shift]┐  header (drag handle)
 *  │ ( ⌕ Search      ) │  Render                    7 MODULES  │
 *  │ ▣ Render       5  │  ┌──────────────────┐ ┌──────────────┐│
 *  │ ▣ Utility      1  │  │ ▣ Ambience [K] ◯━│ │ ▣ Removals ◯━││  content (scissored, scrolls)
 *  │ ▣ HUD          4  │  └──────────────────┘ └──────────────┘│
 *  │ ▣ Settings        │                                       │
 *  └───────────────────┴───────────────────────────────────────┘
 * </pre>
 *
 * <p>Typing anywhere searches every category (names and descriptions); Escape clears the search,
 * then closes. A card's key chip puts the window in binding mode: the next key binds, Escape
 * cancels, Backspace / Delete unbinds. Categories without modules are hidden.</p>
 *
 * <p>All geometry is recomputed from animated values every frame (plain float math – no
 * allocation) and snapped to device pixels via {@link HighDpiEngine}, so the window is razor sharp
 * whenever it's at rest at any GUI scale. Positions persist as screen fractions.</p>
 */
public final class ClickGuiWindow {
	private static final float HEADER = 28f;
	private static final float SIDEBAR = 116f;
	private static final float RADIUS = 14f;
	private static final float PAD = 12f;
	private static final float CARD_GAP = 8f;
	private static final float ITEM_HEIGHT = 22f;
	private static final float ITEM_GAP = 3f;
	private static final float SEARCH_TOP = 7f;
	private static final float SEARCH_H = 22f;
	private static final float LIST_TOP = 34f;
	private static final float LIGHT_RADIUS = 4.5f;
	private static final float LIGHT_SPACING = 14f;
	private static final float COMPACT_W = 440f;
	private static final float COMPACT_H = 280f;
	private static final float EXPANDED_W = 560f;
	private static final float EXPANDED_H = 350f;
	private static final float SCREEN_MARGIN = 8f;
	private static final float SIDEBAR_TILE = 15f;
	private static final int[] LIGHT_COLORS = {Palette.TRAFFIC_RED, Palette.TRAFFIC_YELLOW, Palette.TRAFFIC_GREEN};

	private final ClickGuiScreen screen;

	private final Smooth posX = new Smooth(0f, Motion.DRAG_FOLLOW);
	private final Smooth posY = new Smooth(0f, Motion.DRAG_FOLLOW);
	private final Smooth sizeW = new Smooth(COMPACT_W, Motion.TOGGLE);
	private final Smooth sizeH = new Smooth(COMPACT_H, Motion.TOGGLE);
	private final Smooth collapse = new Smooth(0f, Motion.TOGGLE);
	/** Two followers with different speeds = the selection pill stretches like liquid while moving. */
	private final Smooth indicatorLead = new Smooth(0f, 24f);
	private final Smooth indicatorTrail = new Smooth(0f, 12f);
	private final Smooth indicatorDim = new Smooth(1f, Motion.HOVER);
	private final Smooth reveal = new Smooth(1f, 9f);
	private final Smooth scroll = new Smooth(0f, Motion.SCROLL);
	private final Smooth scrollbar = new Smooth(0f, Motion.HOVER);
	private final Smooth lightsHover = new Smooth(0f, Motion.HOVER);
	private final Smooth[] itemHover;

	private final TextLabel title = new TextLabel("Liquid", LiquidText.TextStyle.TITLE);
	private final TextLabel keyHint;
	private final TextLabel emptyTitle;
	private final TextLabel emptyDescription;
	private final TextLabel noResultsTitle;
	private final TextLabel noResultsDescription;
	private final TextLabel searchHeading;
	private final TextLabel searchCount = new TextLabel("", LiquidText.TextStyle.BODY).uppercase().tracking(0.6f);
	/** Sidebar entries: only categories that have cards. */
	private final Category[] visible;
	private final TextLabel[] categoryNames;
	private final TextLabel[] categoryHeadings;
	private final TextLabel[] categoryCounts;
	private final TextLabel[] categoryBadges;
	private final int[] badgeValues;
	private final List<List<ModuleCard>> cards = new ArrayList<>();
	private final List<ModuleCard> results = new ArrayList<>();
	private final TextField search;
	private String query = "";
	private boolean searchFocused;
	private @Nullable ModuleCard binding;

	private float screenW;
	private float screenH;
	private int selected;
	private boolean expanded;
	private boolean collapsed;
	private boolean dragging;
	private double grabX;
	private double grabY;
	private float contentHeight;
	/** Scroll offset to restore once the content height is known (first frame), -1 when none. */
	private float pendingScroll = -1f;
	private final TextLabel hudButton = new TextLabel("Edit HUD", LiquidText.TextStyle.LABEL);
	private final Smooth hudHover = new Smooth(0f, Motion.HOVER);
	private float hudX;
	private float hudY;
	private float hudW;
	private float hudH;

	public ClickGuiWindow(ClickGuiScreen screen) {
		this.screen = screen;

		keyHint = new TextLabel(LiquidClient.OPEN_CLICK_GUI.getTranslatedKeyMessage().getString(), LiquidText.TextStyle.BODY);
		emptyTitle = new TextLabel(I18n.get("liquidclient.clickgui.empty"), LiquidText.TextStyle.LABEL);
		emptyDescription = new TextLabel(I18n.get("liquidclient.clickgui.empty.desc"), LiquidText.TextStyle.BODY);
		noResultsTitle = new TextLabel(I18n.get("liquidclient.clickgui.no_results"), LiquidText.TextStyle.LABEL);
		noResultsDescription = new TextLabel(I18n.get("liquidclient.clickgui.no_results.desc"), LiquidText.TextStyle.BODY);
		searchHeading = new TextLabel(I18n.get("liquidclient.clickgui.search_results"), LiquidText.TextStyle.TITLE);
		search = new TextField(I18n.get("liquidclient.clickgui.search"), true).maxLength(40).onChange(this::onQuery);

		List<Category> shown = new ArrayList<>();
		for (int i = 0; i < Category.count(); i++) {
			Category category = Category.byIndex(i);
			List<ModuleCard> list = createCards(category);
			if (!list.isEmpty()) {
				shown.add(category);
				cards.add(list);
			}
		}
		visible = shown.toArray(new Category[0]);
		int n = visible.length;
		itemHover = new Smooth[n];
		categoryNames = new TextLabel[n];
		categoryHeadings = new TextLabel[n];
		categoryCounts = new TextLabel[n];
		categoryBadges = new TextLabel[n];
		badgeValues = new int[n];
		for (int i = 0; i < n; i++) {
			Category category = visible[i];
			itemHover[i] = new Smooth(0f, Motion.HOVER);
			String name = I18n.get(category.translationKey);
			categoryNames[i] = new TextLabel(name, LiquidText.TextStyle.LABEL);
			categoryHeadings[i] = new TextLabel(name, LiquidText.TextStyle.TITLE);
			int size = cards.get(i).size();
			String count = size + (category == Category.SETTINGS ? " options" : size == 1 ? " module" : " modules");
			categoryCounts[i] = new TextLabel(count, LiquidText.TextStyle.BODY).uppercase().tracking(0.6f);
			categoryBadges[i] = new TextLabel("", LiquidText.TextStyle.CAPTION);
			badgeValues[i] = -1;
		}

		selected = 0;
		for (int i = 0; i < n; i++) {
			if (visible[i].ordinal() == Math.floorMod(LiquidConfig.clickGuiCategory, Category.count())) {
				selected = i;
			}
		}
		indicatorLead.snap(selected);
		indicatorTrail.snap(selected);
		restoreState();
	}

	/** Puts the window back the way it was left: size, collapse, open drawers, search and scroll. */
	private void restoreState() {
		expanded = LiquidConfig.clickGuiExpanded;
		collapsed = LiquidConfig.clickGuiCollapsed;
		collapse.snap(collapsed ? 1f : 0f);
		for (List<ModuleCard> list : cards) {
			for (ModuleCard card : list) {
				card.restoreDrawer(!card.key().isEmpty() && LiquidConfig.clickGuiOpenCards.contains(card.key()));
			}
		}
		if (!LiquidConfig.clickGuiSearch.isEmpty()) {
			search.setValue(LiquidConfig.clickGuiSearch);
		}
		pendingScroll = LiquidConfig.clickGuiScroll;
	}

	private List<ModuleCard> createCards(Category category) {
		List<ModuleCard> list = new ArrayList<>();
		if (category == Category.SETTINGS) {
			addInterfaceCards(list);
			return list;
		}
		for (Module module : ModuleManager.byCategory(category)) {
			list.add(ModuleCard.forModule(module, category.color).onBind(this::beginBinding));
		}
		return list;
	}

	/** Interface preferences (not modules), bound straight to {@link LiquidConfig}. */
	private static void addInterfaceCards(List<ModuleCard> list) {
		list.add(setting("title", 0xE0FF, new BoolBinding() {
			public boolean get() { return LiquidConfig.replaceTitleScreen; }
			public void set(boolean v) { LiquidConfig.replaceTitleScreen = v; LiquidConfig.save(); }
		}, List.of(SettingComponents.create(LiquidConfig.MENU_BACKGROUND, Palette.ACCENT_BLUE))));
		list.add(setting("media_player", 0xE122, new BoolBinding() {
			public boolean get() { return LiquidConfig.menuMediaPlayer; }
			public void set(boolean v) { LiquidConfig.menuMediaPlayer = v; LiquidConfig.save(); }
		}, List.of()));
		list.add(setting("blur", 0xE0B4, new BoolBinding() {
			public boolean get() { return LiquidConfig.backdropBlur; }
			public void set(boolean v) { LiquidConfig.backdropBlur = v; LiquidConfig.save(); }
		}, List.of()));
		list.add(setting("motion", 0xE1BF, new BoolBinding() {
			public boolean get() { return LiquidConfig.reduceMotion; }
			public void set(boolean v) { LiquidConfig.reduceMotion = v; LiquidConfig.save(); }
		}, List.of()));
		list.add(setting("glow", 0xE412, new BoolBinding() {
			public boolean get() { return LiquidConfig.textGlow; }
			public void set(boolean v) { LiquidConfig.textGlow = v; LiquidConfig.save(); }
		}, List.of()));
		list.add(setting("font", 0xE198, new BoolBinding() {
			public boolean get() { return LiquidConfig.customFont; }
			public void set(boolean v) { LiquidConfig.customFont = v; LiquidConfig.save(); }
		}, List.of()));
	}

	private static ModuleCard setting(String key, int icon, BoolBinding binding, List<UiComponent> drawer) {
		return new ModuleCard(I18n.get("liquidclient.setting." + key), I18n.get("liquidclient.setting." + key + ".desc"),
				binding, Palette.ACCENT_BLUE, icon, null, drawer).key("setting." + key);
	}

	// ---- Search / binding ---------------------------------------------------------------------

	private boolean searching() {
		return !query.isEmpty();
	}

	private void onQuery(String value) {
		String q = value.trim().toLowerCase(Locale.ROOT);
		if (q.equals(query)) {
			return;
		}
		query = q;
		results.clear();
		if (!q.isEmpty()) {
			for (List<ModuleCard> list : cards) {
				for (ModuleCard card : list) {
					if (card.matches(q)) {
						results.add(card);
					}
				}
			}
			searchCount.setText(results.size() + (results.size() == 1 ? " result" : " results"));
		}
		reveal.snap(0f);
		scroll.snap(0f);
		pendingScroll = -1f;
		contentHeight = 0f;
	}

	private void focusSearch(boolean focused) {
		if (searchFocused != focused) {
			searchFocused = focused;
			search.setFocused(focused);
		}
	}

	/** A card's key chip was clicked ({@code card}) or toggled off ({@code null}). */
	private void beginBinding(@Nullable ModuleCard card) {
		if (binding != null && binding != card) {
			binding.setListening(false);
		}
		binding = card;
		if (card != null) {
			focusSearch(false);
		}
	}

	private void cancelBinding() {
		if (binding != null) {
			binding.setListening(false);
			binding = null;
		}
	}

	/** Search or binding is consuming the keyboard (the GUI key mustn't close the window). */
	public boolean capturesKeyboard() {
		return binding != null || searchFocused;
	}

	// ---- Layout -------------------------------------------------------------------------------

	/** Called on open and whenever the screen is resized (window resize, GUI scale change). */
	public void layout(float screenWidth, float screenHeight, boolean firstLayout) {
		this.screenW = screenWidth;
		this.screenH = screenHeight;
		if (firstLayout) {
			sizeW.snap(targetWidth());
			sizeH.snap(targetHeight());
			float cx = LiquidConfig.clickGuiX >= 0f ? LiquidConfig.clickGuiX * screenWidth : screenWidth * 0.5f;
			float cy = LiquidConfig.clickGuiY >= 0f ? LiquidConfig.clickGuiY * screenHeight : screenHeight * 0.5f;
			posX.snap(clampX(cx - sizeW.get() * 0.5f, sizeW.get()));
			posY.snap(clampY(cy - sizeH.get() * 0.5f, sizeH.get()));
		} else {
			sizeW.target(targetWidth());
			sizeH.target(targetHeight());
			posX.snap(clampX(posX.get(), targetWidth()));
			posY.snap(clampY(posY.get(), targetHeight()));
		}
	}

	private float targetWidth() {
		return Math.max(Math.min(expanded ? EXPANDED_W : COMPACT_W, screenW - SCREEN_MARGIN * 2f), 260f);
	}

	private float targetHeight() {
		return Math.max(Math.min(expanded ? EXPANDED_H : COMPACT_H, screenH - SCREEN_MARGIN * 2f), 150f);
	}

	private float clampX(float x, float w) {
		return Math.clamp(x, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenW - w - SCREEN_MARGIN));
	}

	private float clampY(float y, float h) {
		return Math.clamp(y, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenH - h - SCREEN_MARGIN));
	}

	public float centerX() {
		return posX.get() + sizeW.get() * 0.5f;
	}

	public float centerY() {
		return posY.get() + visibleHeight() * 0.5f;
	}

	private float visibleHeight() {
		return HEADER + (sizeH.get() - HEADER) * (1f - collapse.get());
	}

	/**
	 * Remembers the window as it is: centre (as a screen fraction), category, size, collapse, scroll,
	 * search and which settings drawers are open. Written to the config by the screen.
	 */
	public void savePosition() {
		if (screenW > 0f && screenH > 0f) {
			LiquidConfig.clickGuiX = (posX.target() + targetWidth() * 0.5f) / screenW;
			LiquidConfig.clickGuiY = (posY.target() + targetHeight() * 0.5f) / screenH;
		}
		if (visible.length > 0) {
			LiquidConfig.clickGuiCategory = visible[selected].ordinal();
		}
		LiquidConfig.clickGuiExpanded = expanded;
		LiquidConfig.clickGuiCollapsed = collapsed;
		LiquidConfig.clickGuiScroll = pendingScroll >= 0f ? pendingScroll : scroll.target();
		LiquidConfig.clickGuiSearch = search.value();
		LiquidConfig.clickGuiOpenCards.clear();
		for (List<ModuleCard> list : cards) {
			for (ModuleCard card : list) {
				if (card.drawerOpen() && !card.key().isEmpty()) {
					LiquidConfig.clickGuiOpenCards.add(card.key());
				}
			}
		}
	}

	// ---- Rendering ----------------------------------------------------------------------------

	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		posX.speed(Motion.speed(Motion.DRAG_FOLLOW));
		posY.speed(Motion.speed(Motion.DRAG_FOLLOW));
		sizeW.speed(Motion.speed(Motion.TOGGLE));
		sizeH.speed(Motion.speed(Motion.TOGGLE));
		collapse.speed(Motion.speed(Motion.TOGGLE));
		posX.update(dt);
		posY.update(dt);
		sizeW.update(dt);
		sizeH.update(dt);
		collapse.target(collapsed).update(dt);

		// Device-pixel snapping: smooth while moving, razor sharp at rest.
		float wx = HighDpiEngine.snap(posX.get());
		float wy = HighDpiEngine.snap(posY.get());
		float w = HighDpiEngine.snap(sizeW.get());
		float h = HighDpiEngine.snap(visibleHeight());

		GlassRenderer.panel(g, wx, wy, w, h, RADIUS, 4f);

		if (h > HEADER + 1f) {
			int clipTop = (int) Math.ceil(wy + HEADER);
			int clipBottom = (int) Math.floor(wy + h);
			// Sidebar tint: same rounded shape as the window, clipped to the sidebar column.
			g.enableScissor((int) Math.floor(wx), clipTop, Math.round(wx + SIDEBAR), clipBottom);
			RenderUtils.fill(g, wx, wy, SIDEBAR + RADIUS * 2f, h, RADIUS, 0x1F020617);
			extractSidebar(g, mouseX, mouseY, wx, wy);
			g.disableScissor();

			RenderUtils.rect(g, wx + SIDEBAR, wy + HEADER, GlassRenderer.hairline(), h - HEADER, 0x14FFFFFF);
			extractContent(g, mouseX, mouseY, wx, wy, w, h);
		}

		RenderUtils.rect(g, wx + 1f, wy + HEADER - GlassRenderer.hairline(), w - 2f, GlassRenderer.hairline(),
				ColorUtil.fade(0x1FFFFFFF, Math.min(1f, (h - HEADER) / 8f)));
		extractHeader(g, mouseX, mouseY, wx, wy, w);
	}

	private void extractHeader(GuiGraphicsExtractor g, int mouseX, int mouseY, float wx, float wy, float w) {
		float cy = wy + HEADER * 0.5f;
		float hovered = lightsHover.target(lightAt(mouseX, mouseY) >= 0).update(UiFrame.dt);
		float line = GlassRenderer.hairline();
		for (int i = 0; i < 3; i++) {
			float lx = lightX(wx, i);
			RenderUtils.circle(g, lx, cy, LIGHT_RADIUS, LIGHT_COLORS[i]);
			RenderUtils.stroke(g, lx - LIGHT_RADIUS, cy - LIGHT_RADIUS, LIGHT_RADIUS * 2f, LIGHT_RADIUS * 2f, LIGHT_RADIUS,
					line, 0x26000000, 0x40000000);
			if (hovered > 0.01f) {
				extractLightGlyph(g, i, lx, cy, ColorUtil.fade(0x99000000, hovered));
			}
		}

		float titleY = cy - title.capHeight() * 0.5f;
		LiquidText.drawGlow(g, title, wx + (w - title.width()) * 0.5f, titleY, Palette.ACCENT_BLUE, 0.75f);
		LiquidText.drawCentered(g, title, wx + w * 0.5f, titleY, Palette.TEXT_PRIMARY, true);

		float pillW = keyHint.width() + 12f;
		float pillH = 14f;
		float px = wx + w - PAD - pillW;
		float titleRight = wx + w * 0.5f + title.width() * 0.5f + 8f;
		if (px > titleRight) {
			RenderUtils.fill(g, px, cy - pillH * 0.5f, pillW, pillH, pillH * 0.5f, 0x14FFFFFF);
			RenderUtils.hairline(g, px, cy - pillH * 0.5f, pillW, pillH, pillH * 0.5f, 0x33FFFFFF, 0x0DFFFFFF);
			LiquidText.draw(g, keyHint, px + 6f, cy - keyHint.capHeight() * 0.5f, Palette.TEXT_TERTIARY, false);
		} else {
			px = wx + w - PAD + 6f;
		}

		// "Edit HUD" – opens the HUD editor.
		hudH = 16f;
		hudW = 8f + 10f + 4f + hudButton.width() + 8f;
		hudX = px - 6f - hudW;
		hudY = cy - hudH * 0.5f;
		if (hudX > titleRight) {
			boolean over = mouseX >= hudX && mouseX < hudX + hudW && mouseY >= hudY && mouseY < hudY + hudH;
			float h = hudHover.target(over).update(UiFrame.dt);
			RenderUtils.fill(g, hudX, hudY, hudW, hudH, hudH * 0.5f, ColorUtil.lerp(0x40007AFF, 0x80007AFF, h));
			RenderUtils.hairline(g, hudX, hudY, hudW, hudH, hudH * 0.5f, ColorUtil.fade(0x99007AFF, 0.6f + h * 0.4f), 0x1A007AFF);
			IconFont.draw(g, 0xE1C1, hudX + 8f + 5f, cy, 10f, 0xFFFFFFFF, 0);
			LiquidText.draw(g, hudButton, hudX + 8f + 10f + 4f, cy - hudButton.capHeight() * 0.5f, Palette.TEXT_PRIMARY, false);
		} else {
			hudW = 0f;
		}
	}

	/** macOS glyphs drawn with SDF capsules: ×, −, +. */
	private static void extractLightGlyph(GuiGraphicsExtractor g, int index, float cx, float cy, int color) {
		float half = 2.3f;
		float thick = 1.1f;
		switch (index) {
			case 0 -> {
				RenderUtils.line(g, cx - half * 0.75f, cy - half * 0.75f, cx + half * 0.75f, cy + half * 0.75f, thick, color);
				RenderUtils.line(g, cx - half * 0.75f, cy + half * 0.75f, cx + half * 0.75f, cy - half * 0.75f, thick, color);
			}
			case 1 -> RenderUtils.line(g, cx - half, cy, cx + half, cy, thick, color);
			default -> {
				RenderUtils.line(g, cx - half, cy, cx + half, cy, thick, color);
				RenderUtils.line(g, cx, cy - half, cx, cy + half, thick, color);
			}
		}
	}

	private void extractSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY, float wx, float wy) {
		float dt = UiFrame.dt;
		search.setBounds(wx + 7f, wy + HEADER + SEARCH_TOP, SIDEBAR - 14f, SEARCH_H);
		search.extract(g, mouseX, mouseY);

		indicatorLead.speed(Motion.speed(24f));
		indicatorTrail.speed(Motion.speed(12f));
		float lead = indicatorLead.target(selected).update(dt);
		float trail = indicatorTrail.target(selected).update(dt);
		float dim = indicatorDim.target(searching() ? 0.3f : 1f).update(dt);

		float step = ITEM_HEIGHT + ITEM_GAP;
		float top = itemsTop(wy);
		float ix = wx + 6f;
		float iw = SIDEBAR - 12f;

		// Liquid selection pill spanning both followers (dimmed while search results are shown).
		float pillTop = top + Math.min(lead, trail) * step;
		float pillBottom = top + Math.max(lead, trail) * step + ITEM_HEIGHT;
		float pillH = pillBottom - pillTop;
		RenderUtils.pushAlpha(dim);
		GlassRenderer.accentGlow(g, ix, pillTop, iw, pillH, 10f, Palette.ACCENT_BLUE, 0.55f);
		RenderUtils.gradient(g, ix, pillTop, iw, pillH, 10f, 0x52007AFF, 0x33007AFF);
		RenderUtils.hairline(g, ix, pillTop, iw, pillH, 10f, 0x4DFFFFFF, 0x1A007AFF);
		RenderUtils.popAlpha();

		for (int i = 0; i < visible.length; i++) {
			Category category = visible[i];
			float iy = top + i * step;
			boolean current = i == selected && !searching();
			boolean over = mouseX >= ix && mouseX < ix + iw && mouseY >= iy && mouseY < iy + ITEM_HEIGHT;
			float hv = itemHover[i].target(over && !current).update(dt);
			if (hv > 0.01f) {
				RenderUtils.fill(g, ix, iy, iw, ITEM_HEIGHT, 10f, ColorUtil.fade(Palette.HOVER_FILL, hv));
			}
			float midY = iy + ITEM_HEIGHT * 0.5f;
			// macOS Settings-style tile: category gradient, white glyph.
			float tx = ix + 5f;
			float ty = midY - SIDEBAR_TILE * 0.5f;
			RenderUtils.gradient(g, tx, ty, SIDEBAR_TILE, SIDEBAR_TILE, 4.5f,
					ColorUtil.lerpRgb(category.color, 0xFFFFFFFF, 0.15f), ColorUtil.lerpRgb(category.color, 0xFF000000, 0.2f));
			IconFont.draw(g, category.icon, tx + SIDEBAR_TILE * 0.5f, midY, 9f, 0xFFFFFFFF, 0x33000000);

			int textColor = current ? Palette.TEXT_PRIMARY : ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, hv);
			TextLabel badge = badge(i);
			float badgeW = badge.text().isEmpty() ? 0f : badge.width() + 6f;
			TextLabel name = categoryNames[i].maxWidth(iw - SIDEBAR_TILE - 14f - badgeW);
			LiquidText.draw(g, name, tx + SIDEBAR_TILE + 6f, midY - name.capHeight() * 0.5f, textColor, current);
			if (badgeW > 0f) {
				LiquidText.draw(g, badge, ix + iw - 6f - badge.width(), midY - badge.capHeight() * 0.5f,
						current ? Palette.TEXT_SECONDARY : Palette.TEXT_TERTIARY, false);
			}
		}
	}

	/** Number of enabled modules in a sidebar entry (text rebuilt only when it changes). */
	private TextLabel badge(int index) {
		int count = 0;
		if (visible[index] != Category.SETTINGS && visible[index] != Category.HUD) {
			List<ModuleCard> list = cards.get(index);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).isOn()) {
					count++;
				}
			}
		}
		if (count != badgeValues[index]) {
			badgeValues[index] = count;
			categoryBadges[index].setText(count == 0 ? "" : Integer.toString(count));
		}
		return categoryBadges[index];
	}

	private void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float wx, float wy, float w, float h) {
		float dt = UiFrame.dt;
		reveal.speed(Motion.speed(9f));
		scroll.speed(Motion.speed(Motion.SCROLL));
		float revealT = reveal.target(1f).update(dt);
		float slide = HighDpiEngine.snap((1f - revealT) * 10f);

		float cx = wx + SIDEBAR;
		float cw = w - SIDEBAR;
		float listTop = wy + LIST_TOP + HEADER;
		float listBottom = wy + h - 6f;
		boolean searching = searching();

		RenderUtils.pushAlpha(revealT);
		int clipTop = (int) Math.ceil(wy + HEADER);
		g.enableScissor(Math.round(cx + 1f), clipTop, (int) Math.floor(wx + w - 1f), (int) Math.floor(wy + h - 2f));
		TextLabel heading = searching ? searchHeading : categoryHeadings[selected];
		LiquidText.draw(g, heading, cx + PAD + slide, wy + HEADER + 12f, Palette.TEXT_PRIMARY, true);
		TextLabel count = searching ? searchCount : categoryCounts[selected];
		LiquidText.draw(g, count, wx + w - PAD - count.width(), wy + HEADER + 14f, Palette.TEXT_SECTION, false);
		g.disableScissor();

		List<ModuleCard> list = searching ? results : cards.get(selected);
		if (list.isEmpty()) {
			if (searching) {
				extractEmptyState(g, cx, cw, listTop, listBottom, noResultsTitle, noResultsDescription, 0xE151, Palette.ACCENT_BLUE);
			} else {
				Category category = visible[selected];
				extractEmptyState(g, cx, cw, listTop, listBottom, emptyTitle, emptyDescription, 0, category.color);
			}
			RenderUtils.popAlpha();
			return;
		}

		float viewport = Math.max(0f, listBottom - listTop);
		float maxScroll = Math.max(0f, contentHeight - viewport + 4f);
		scroll.target(Math.clamp(scroll.target(), 0f, maxScroll));
		float offset = HighDpiEngine.snap(scroll.update(dt));

		boolean mouseInList = mouseY >= listTop && mouseY < listBottom && mouseX >= cx && mouseX < wx + w;
		int mx = mouseInList ? mouseX : -10000;
		int my = mouseInList ? mouseY : -10000;

		int columns = cw - PAD * 2f >= 330f ? 2 : 1;
		float cardW = HighDpiEngine.snap((cw - PAD * 2f - CARD_GAP * (columns - 1)) / columns);
		float rowY = listTop + 2f - offset + slide;
		float start = rowY;

		g.enableScissor(Math.round(cx + 1f), (int) Math.ceil(listTop), (int) Math.floor(wx + w - 1f), (int) Math.floor(listBottom));
		for (int i = 0; i < list.size(); i += columns) {
			float rowH = 0f;
			for (int c = 0; c < columns && i + c < list.size(); c++) {
				rowH = Math.max(rowH, list.get(i + c).preferredHeight(cardW));
			}
			for (int c = 0; c < columns && i + c < list.size(); c++) {
				ModuleCard card = list.get(i + c);
				card.setBounds(cx + PAD + c * (cardW + CARD_GAP), HighDpiEngine.snap(rowY), cardW, card.preferredHeight(cardW));
				card.extract(g, mx, my);
			}
			rowY += rowH + CARD_GAP;
		}
		g.disableScissor();
		contentHeight = rowY - start;
		if (pendingScroll >= 0f) {
			scroll.snap(Math.min(pendingScroll, Math.max(0f, contentHeight - viewport + 4f)));
			pendingScroll = -1f;
		}

		// Overlay scrollbar, visible while there is overflow and the pointer is near / scrolling.
		if (maxScroll > 0.5f) {
			boolean active = mouseInList || !scroll.settled();
			float a = scrollbar.target(active ? 1f : 0.25f).update(dt);
			float trackH = viewport - 8f;
			float thumbH = Math.max(16f, trackH * viewport / (contentHeight + 4f));
			float thumbY = listTop + 4f + (trackH - thumbH) * (offset / maxScroll);
			RenderUtils.fill(g, wx + w - 6f, thumbY, 3f, thumbH, 1.5f, ColorUtil.fade(0x66FFFFFF, a));
		}
		RenderUtils.popAlpha();
	}

	private void extractEmptyState(GuiGraphicsExtractor g, float cx, float cw, float top, float bottom,
			TextLabel titleLabel, TextLabel descriptionLabel, int icon, int color) {
		float boxW = Math.min(cw - PAD * 2f, 250f);
		float boxH = 66f;
		float bx = HighDpiEngine.snap(cx + (cw - boxW) * 0.5f);
		float by = HighDpiEngine.snap(top + Math.max(0f, (bottom - top - boxH) * 0.4f));
		RenderUtils.fill(g, bx, by, boxW, boxH, 12f, 0x0DFFFFFF);
		RenderUtils.hairline(g, bx, by, boxW, boxH, 12f, 0x33FFFFFF, 0x0DFFFFFF);
		float iconY = by + 15f;
		if (icon != 0) {
			IconFont.draw(g, icon, bx + boxW * 0.5f, iconY, 12f, Palette.TEXT_SECONDARY, 0);
		} else {
			RenderUtils.outerGlow(g, bx + boxW * 0.5f - 4f, iconY - 4f, 8f, 8f, 4f, 6f, ColorUtil.fade(color, 0.6f));
			RenderUtils.circle(g, bx + boxW * 0.5f, iconY, 4f, color);
		}
		LiquidText.drawCentered(g, titleLabel, bx + boxW * 0.5f, by + 28f, Palette.TEXT_SECONDARY, true);
		TextLabel desc = descriptionLabel.maxWidth(boxW - 16f);
		LiquidText.drawCentered(g, desc, bx + boxW * 0.5f, by + 44f, Palette.TEXT_TERTIARY, false);
	}

	private static float itemsTop(float wy) {
		return wy + HEADER + SEARCH_TOP + SEARCH_H + 8f;
	}

	private static float lightX(float wx, int index) {
		return wx + 16f + index * LIGHT_SPACING;
	}

	private int lightAt(double mouseX, double mouseY) {
		float cy = posY.get() + HEADER * 0.5f;
		for (int i = 0; i < 3; i++) {
			double dx = mouseX - lightX(posX.get(), i);
			double dy = mouseY - cy;
			if (dx * dx + dy * dy <= (LIGHT_RADIUS + 2f) * (LIGHT_RADIUS + 2f)) {
				return i;
			}
		}
		return -1;
	}

	// ---- Input --------------------------------------------------------------------------------

	public boolean contains(double mouseX, double mouseY) {
		float wx = posX.get();
		float wy = posY.get();
		return mouseX >= wx && mouseX < wx + sizeW.get() && mouseY >= wy && mouseY < wy + visibleHeight();
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		cancelBinding(); // a click anywhere ends binding mode (the chip re-arms it if clicked)
		if (!contains(mouseX, mouseY)) {
			focusSearch(false);
			return false;
		}
		float wx = posX.get();
		float wy = posY.get();
		boolean left = button == InputConstants.MOUSE_BUTTON_LEFT;

		int light = lightAt(mouseX, mouseY);
		if (light >= 0 && left) {
			UiSounds.click();
			switch (light) {
				case 0 -> screen.requestClose();
				case 1 -> collapsed = !collapsed;
				default -> toggleExpanded();
			}
			return true;
		}
		if (left && hudW > 0f && mouseX >= hudX && mouseX < hudX + hudW && mouseY >= hudY && mouseY < hudY + hudH) {
			UiSounds.click();
			screen.openHudEditor();
			return true;
		}
		if (mouseY < wy + HEADER) {
			if (left) {
				dragging = true;
				grabX = mouseX - posX.target();
				grabY = mouseY - posY.target();
			}
			return true;
		}
		if (collapsed) {
			return true;
		}
		if (search.contains(mouseX, mouseY)) {
			focusSearch(true);
			search.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		focusSearch(false);
		if (mouseX < wx + SIDEBAR) {
			int index = (int) Math.floor((mouseY - itemsTop(wy)) / (ITEM_HEIGHT + ITEM_GAP));
			float within = (float) (mouseY - itemsTop(wy)) - index * (ITEM_HEIGHT + ITEM_GAP);
			if (left && index >= 0 && index < visible.length && within <= ITEM_HEIGHT && (index != selected || searching())) {
				select(index);
			}
			return true;
		}
		float listTop = wy + HEADER + LIST_TOP;
		float listBottom = wy + visibleHeight() - 6f;
		if (mouseY >= listTop && mouseY < listBottom) {
			List<ModuleCard> list = searching() ? results : cards.get(selected);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return true;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		boolean handled = false;
		if (dragging && button == InputConstants.MOUSE_BUTTON_LEFT) {
			dragging = false;
			savePosition();
			handled = true;
		}
		List<ModuleCard> list = searching() ? results : cards.get(selected);
		for (int i = 0; i < list.size(); i++) {
			handled |= list.get(i).mouseReleased(mouseX, mouseY, button);
		}
		return handled;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (dragging) {
			posX.target(clampX((float) (mouseX - grabX), sizeW.target()));
			posY.target(clampY((float) (mouseY - grabY), visibleHeightTarget()));
			return true;
		}
		List<ModuleCard> list = searching() ? results : cards.get(selected);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (!contains(mouseX, mouseY) || collapsed) {
			return false;
		}
		if (mouseX >= posX.get() + SIDEBAR) {
			scroll.target(Math.max(0f, scroll.target() - (float) amount * 28f));
		}
		return true;
	}

	/**
	 * Keyboard: binding mode takes the next key; then the search field (editing keys); Escape clears
	 * a search before the screen gets to close.
	 */
	public boolean keyPressed(KeyEvent event) {
		if (binding != null) {
			ModuleCard card = binding;
			binding = null;
			card.finishBinding(event.key());
			return true;
		}
		if (event.isEscape() && (searching() || searchFocused)) {
			search.setValue("");
			focusSearch(false);
			return true;
		}
		if (event.hasControlDownWithQuirk() && event.key() == InputConstants.KEY_F) {
			focusSearch(true);
			return true;
		}
		return searchFocused && search.keyPressed(event);
	}

	/** Typing anywhere searches. */
	public boolean charTyped(CharacterEvent event) {
		if (binding != null || collapsed) {
			return false;
		}
		if (!searchFocused) {
			if (Character.isWhitespace(event.codepoint())) {
				return false;
			}
			focusSearch(true);
		}
		return search.charTyped(event);
	}

	private float visibleHeightTarget() {
		return collapsed ? HEADER : sizeH.target();
	}

	private void select(int index) {
		if (searching()) {
			search.setValue("");
			focusSearch(false);
		}
		selected = index;
		reveal.snap(0f);
		scroll.snap(0f);
		pendingScroll = -1f;
		contentHeight = 0f;
		LiquidConfig.clickGuiCategory = visible[index].ordinal();
		UiSounds.click();
	}

	private void toggleExpanded() {
		float centerX = posX.target() + sizeW.target() * 0.5f;
		float centerY = posY.target() + sizeH.target() * 0.5f;
		expanded = !expanded;
		float w = targetWidth();
		float h = targetHeight();
		sizeW.target(w);
		sizeH.target(h);
		posX.target(clampX(centerX - w * 0.5f, w));
		posY.target(clampY(centerY - h * 0.5f, h));
	}
}
