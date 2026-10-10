package net.client.visuals.gui.hud;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.component.BoolBinding;
import net.client.visuals.gui.component.SettingComponents;
import net.client.visuals.gui.component.ToggleSwitch;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.HudModule;
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
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.BackgroundEngine;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD editor: the live HUD over the game with drag-to-move (snapping to screen edges, the centre
 * lines and other elements, with guide lines), scroll-to-resize, an element list on the left and the
 * selected element's settings – or the theme's – on the right.
 *
 * <pre>
 *  ┌ Elements ──┐        [⊹ Snap] [▦ Grid] [↺ Reset] [✓ Done]         ┌ Inspector ─┐
 *  │ FPS     ◯━ │                                                      │ Music      │
 *  │ Ping    ◯━ │            ┆ (guide)                                 │ Layout  ⌄  │
 *  │ Music   ◯━ │   ╭──────────────╮                                   │ Width ━━●━ │
 *  │ …          │   │ ♪ Song title │  ← drag, scroll = scale           │ …          │
 *  └────────────┘   ╰──────────────╯                                   └────────────┘
 *              Drag to move · Scroll to resize · Right-click to edit · Del hides
 * </pre>
 */
public final class HudEditorScreen extends Screen implements LiquidScreen {
	private static final float PANEL_W = 168f;
	private static final float MARGIN = 8f;
	private static final float PANEL_PAD = 10f;
	private static final float PANEL_HEADER = 34f;
	private static final float ROW_H = 20f;
	private static final float SNAP = 4.5f;
	private static final float EDGE = 4f;
	private static final float TOOLBAR_H = 22f;
	private static final int ICON_SNAP = 0xE2B5;
	private static final int ICON_GRID = 0xE0E9;
	private static final int ICON_RESET = 0xE148;
	private static final int ICON_DONE = 0xE06C;
	private static final int ICON_THEME = 0xE1DD;

	private final @Nullable Screen parent;
	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth appear = new Smooth(0f, 9f);
	private final Smooth panels = new Smooth(1f, 12f);
	private final Smooth leftScroll = new Smooth(0f, Motion.SCROLL);
	private final Smooth rightScroll = new Smooth(0f, Motion.SCROLL);

	private final List<ElementRow> rows = new ArrayList<>();
	private final TextLabel elementsTitle = new TextLabel("HUD Elements", LiquidText.TextStyle.TITLE);
	private final TextLabel themeRowLabel = new TextLabel("Theme & Style", LiquidText.TextStyle.LABEL);
	private final TextLabel inspectorTitle = new TextLabel("", LiquidText.TextStyle.TITLE);
	private final TextLabel inspectorSub = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel hint = new TextLabel("Drag to move  ·  Scroll to resize  ·  Right-click to edit  ·  Del hides",
			LiquidText.TextStyle.BODY);
	private final TextLabel tag = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel[] toolbarLabels = {
			new TextLabel("Snap", LiquidText.TextStyle.LABEL), new TextLabel("Grid", LiquidText.TextStyle.LABEL),
			new TextLabel("Reset", LiquidText.TextStyle.LABEL), new TextLabel("Done", LiquidText.TextStyle.LABEL)
	};
	private final int[] toolbarIcons = {ICON_SNAP, ICON_GRID, ICON_RESET, ICON_DONE};
	private final Smooth[] toolbarHover = {new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER),
			new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER)};
	private final float[] toolbarX = new float[4];
	private final float[] toolbarW = new float[4];
	private final Smooth themeHover = new Smooth(0f, Motion.HOVER);

	private List<UiComponent> inspectorRows = List.of();
	private @Nullable Module inspected;
	private @Nullable HudModule selected;
	private @Nullable HudModule dragging;
	private @Nullable UiComponent capture;
	private float grabX;
	private float grabY;
	private boolean snap = true;
	private boolean grid = true;
	private float guideX = Float.NaN;
	private float guideY = Float.NaN;
	private float leftContent;
	private float rightContent;
	private float toolbarLeft;
	private float toolbarWidth;
	private String shownTag = "";

	public HudEditorScreen(@Nullable Screen parent) {
		super(Component.literal("HUD Editor"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (rows.isEmpty()) {
			for (HudModule element : HudManager.elements()) {
				rows.add(new ElementRow(element));
			}
			inspect(HudManager.theme());
			timer.reset();
		}
	}

	private void inspect(Module module) {
		if (inspected == module) {
			return;
		}
		inspected = module;
		inspectorTitle.setText(module == HudManager.theme() ? "HUD Theme" : module.displayName());
		inspectorSub.setText(module.description());
		inspectorRows = SettingComponents.build(module.settings(), Palette.ACCENT_BLUE);
		rightScroll.snap(0f);
		rightScroll.target(0f);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void removed() {
		ModuleManager.saveIfDirty();
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (minecraft.level == null) {
			BackgroundEngine.INSTANCE.extract(g, width, height);
		} else {
			GlassRenderer.beginFrame(false);
			RenderUtils.rect(g, 0f, 0f, width, height, 0x38000000);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		float dt = UiFrame.dt;
		float a = appear.target(1f).update(dt);
		float panelAlpha = panels.target(dragging != null ? 0.18f : 1f).update(dt);
		RenderUtils.pushAlpha(a);

		if (grid) {
			extractGrid(g);
		}
		HudContext ctx = HudManager.editorContext(dt);
		HudModule hovered = dragging != null ? dragging : elementAt(mouseX, mouseY);
		for (HudModule element : HudManager.elements()) {
			if (!element.isEnabled()) {
				continue;
			}
			element.layout(ctx);
			HudManager.draw(g, ctx, element, width, height);
		}
		extractSelection(g, hovered);
		extractGuides(g);

		RenderUtils.pushAlpha(panelAlpha);
		boolean panelInput = dragging == null;
		extractElementsPanel(g, panelInput ? mouseX : -1000, panelInput ? mouseY : -1000);
		extractInspector(g, panelInput ? mouseX : -1000, panelInput ? mouseY : -1000);
		extractToolbar(g, panelInput ? mouseX : -1000, panelInput ? mouseY : -1000);
		float hw = hint.width() + 18f;
		float hy = height - 22f;
		RenderUtils.fill(g, (width - hw) * 0.5f, hy, hw, 15f, 7.5f, 0x8C0B1020);
		LiquidText.drawCentered(g, hint, width * 0.5f, hy + 7.5f - hint.capHeight() * 0.5f, Palette.TEXT_SECONDARY, false);
		RenderUtils.popAlpha();

		RenderUtils.popAlpha();
		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	private void extractGrid(GuiGraphicsExtractor g) {
		float px = HighDpiEngine.px();
		for (float x = 32f; x < width; x += 32f) {
			RenderUtils.rect(g, HighDpiEngine.snap(x), 0f, px, height, 0x0FFFFFFF);
		}
		for (float y = 32f; y < height; y += 32f) {
			RenderUtils.rect(g, 0f, HighDpiEngine.snap(y), width, px, 0x0FFFFFFF);
		}
		RenderUtils.rect(g, HighDpiEngine.snap(width * 0.5f), 0f, px, height, 0x1FFFFFFF);
		RenderUtils.rect(g, 0f, HighDpiEngine.snap(height * 0.5f), width, px, 0x1FFFFFFF);
	}

	private void extractSelection(GuiGraphicsExtractor g, @Nullable HudModule hovered) {
		float line = HighDpiEngine.px();
		if (hovered != null && hovered != selected) {
			bounds(hovered);
			RenderUtils.stroke(g, bx - 2f, by - 2f, bw + 4f, bh + 4f, 6f, line, 0x66FFFFFF, 0x33FFFFFF);
		}
		if (selected != null && selected.isEnabled()) {
			bounds(selected);
			RenderUtils.outerGlow(g, bx - 2f, by - 2f, bw + 4f, bh + 4f, 6f, 5f, ColorUtil.fade(Palette.ACCENT_BLUE, 0.35f));
			RenderUtils.stroke(g, bx - 2f, by - 2f, bw + 4f, bh + 4f, 6f, line * 1.5f, Palette.ACCENT_BLUE, ColorUtil.fade(Palette.ACCENT_BLUE, 0.7f));
			String text = selected.displayName() + "  ·  " + Math.round(selected.scale.get()) + "%";
			if (!text.equals(shownTag)) {
				shownTag = text;
				tag.setText(text);
			}
			float tw = tag.width() + 12f;
			float ty = by - 18f >= 2f ? by - 18f : by + bh + 5f;
			float tx = Math.clamp(bx, 2f, Math.max(2f, width - tw - 2f));
			RenderUtils.fill(g, tx, ty, tw, 13f, 6.5f, 0xE6007AFF);
			LiquidText.draw(g, tag, tx + 6f, ty + 6.5f - tag.capHeight() * 0.5f, 0xFFFFFFFF, false);
		}
	}

	private void extractGuides(GuiGraphicsExtractor g) {
		if (dragging == null) {
			return;
		}
		float px = HighDpiEngine.px();
		if (!Float.isNaN(guideX)) {
			RenderUtils.rect(g, HighDpiEngine.snap(guideX) - px * 0.5f, 0f, px, height, 0xCCFF2D95);
		}
		if (!Float.isNaN(guideY)) {
			RenderUtils.rect(g, 0f, HighDpiEngine.snap(guideY) - px * 0.5f, width, px, 0xCCFF2D95);
		}
	}

	// ---- Panels -------------------------------------------------------------------------------

	private float panelHeight() {
		return height - MARGIN * 2f - 26f;
	}

	private void extractElementsPanel(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float x = MARGIN;
		float y = MARGIN;
		float h = Math.min(panelHeight(), PANEL_HEADER + 26f + rows.size() * ROW_H + PANEL_PAD);
		GlassRenderer.panel(g, x, y, PANEL_W, h, 12f, 2f);
		LiquidText.draw(g, elementsTitle, x + PANEL_PAD, y + 11f, Palette.TEXT_PRIMARY, true);

		// "Theme & Style" row opens the theme in the inspector.
		float ty = y + PANEL_HEADER - 4f;
		boolean overTheme = mouseX >= x + 6f && mouseX < x + PANEL_W - 6f && mouseY >= ty && mouseY < ty + 22f;
		float th = themeHover.target(overTheme || inspected == HudManager.theme()).update(UiFrame.dt);
		RenderUtils.fill(g, x + 6f, ty, PANEL_W - 12f, 22f, 7f, ColorUtil.fade(0x26007AFF, 0.4f + th * 0.6f));
		IconFont.draw(g, ICON_THEME, x + PANEL_PAD + 6f, ty + 11f, 11f, 0xFF64B5FF, 0);
		LiquidText.draw(g, themeRowLabel, x + PANEL_PAD + 17f, ty + 11f - themeRowLabel.capHeight() * 0.5f, Palette.TEXT_PRIMARY, false);

		float listTop = ty + 26f;
		float listBottom = y + h - 4f;
		float viewport = listBottom - listTop;
		float maxScroll = Math.max(0f, leftContent - viewport);
		leftScroll.target(Math.clamp(leftScroll.target(), 0f, maxScroll));
		float offset = HighDpiEngine.snap(leftScroll.update(UiFrame.dt));
		g.enableScissor((int) x, (int) Math.ceil(listTop), (int) (x + PANEL_W), (int) Math.floor(listBottom));
		float ry = listTop - offset;
		boolean inList = mouseY >= listTop && mouseY < listBottom;
		for (ElementRow row : rows) {
			row.setBounds(x + 6f, ry, PANEL_W - 12f, ROW_H);
			row.extract(g, inList ? mouseX : -1000, inList ? mouseY : -1000);
			ry += ROW_H;
		}
		g.disableScissor();
		leftContent = rows.size() * ROW_H;
	}

	private void extractInspector(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float w = PANEL_W + 12f;
		float x = width - MARGIN - w;
		float y = MARGIN;
		float h = panelHeight();
		float innerW = w - PANEL_PAD * 2f;
		float content = 0f;
		for (UiComponent row : inspectorRows) {
			content += row.preferredHeight(innerW) + 6f;
		}
		h = Math.min(h, PANEL_HEADER + 14f + content + PANEL_PAD);
		GlassRenderer.panel(g, x, y, w, h, 12f, 2f);
		inspectorTitle.maxWidth(innerW);
		inspectorSub.maxWidth(innerW);
		LiquidText.draw(g, inspectorTitle, x + PANEL_PAD, y + 11f, Palette.TEXT_PRIMARY, true);
		LiquidText.draw(g, inspectorSub, x + PANEL_PAD, y + 26f, Palette.TEXT_TERTIARY, false);

		float top = y + PANEL_HEADER + 8f;
		float bottom = y + h - 6f;
		float maxScroll = Math.max(0f, content - (bottom - top));
		rightScroll.target(Math.clamp(rightScroll.target(), 0f, maxScroll));
		float offset = HighDpiEngine.snap(rightScroll.update(UiFrame.dt));
		boolean inside = mouseY >= top && mouseY < bottom;
		g.enableScissor((int) x, (int) Math.ceil(top - 2f), (int) (x + w), (int) Math.floor(bottom));
		float ry = top - offset;
		for (UiComponent row : inspectorRows) {
			float rh = row.preferredHeight(innerW);
			row.setBounds(x + PANEL_PAD, ry, innerW, rh);
			row.extract(g, inside ? mouseX : -1000, inside ? mouseY : -1000);
			ry += rh + 6f;
		}
		g.disableScissor();
		rightContent = content;
	}

	private void extractToolbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float total = 0f;
		for (int i = 0; i < 4; i++) {
			toolbarW[i] = 8f + 11f + 5f + toolbarLabels[i].width() + 9f;
			total += toolbarW[i];
		}
		total += 3f * 3f + 6f;
		toolbarWidth = total;
		toolbarLeft = HighDpiEngine.snap((width - total) * 0.5f);
		float y = MARGIN;
		GlassRenderer.panel(g, toolbarLeft, y, total, TOOLBAR_H + 6f, (TOOLBAR_H + 6f) * 0.5f, 2f);
		float bx = toolbarLeft + 3f;
		for (int i = 0; i < 4; i++) {
			toolbarX[i] = bx;
			boolean over = mouseX >= bx && mouseX < bx + toolbarW[i] && mouseY >= y + 3f && mouseY < y + 3f + TOOLBAR_H;
			float hv = toolbarHover[i].target(over).update(UiFrame.dt);
			boolean on = i == 0 ? snap : i == 1 && grid;
			int fill = i == 3 ? ColorUtil.lerp(0xCC007AFF, 0xFF1A8CFF, hv) : on ? ColorUtil.lerp(0x40007AFF, 0x66007AFF, hv) : ColorUtil.fade(0x1FFFFFFF, hv);
			if ((fill >>> 24) > 0) {
				RenderUtils.fill(g, bx, y + 3f, toolbarW[i], TOOLBAR_H, TOOLBAR_H * 0.5f, fill);
			}
			int color = i == 3 || on ? 0xFFFFFFFF : Palette.TEXT_SECONDARY;
			IconFont.draw(g, toolbarIcons[i], bx + 8f + 5.5f, y + 3f + TOOLBAR_H * 0.5f, 11f, color, 0);
			LiquidText.draw(g, toolbarLabels[i], bx + 8f + 11f + 5f, y + 3f + TOOLBAR_H * 0.5f - toolbarLabels[i].capHeight() * 0.5f, color, false);
			bx += toolbarW[i] + 3f;
		}
	}

	// ---- Geometry -----------------------------------------------------------------------------

	/** Scratch bounds of the last element passed to {@link #bounds} (no per-frame allocation). */
	private float bx;
	private float by;
	private float bw;
	private float bh;
	private float snapX;
	private float snapY;

	private void bounds(HudModule element) {
		float s = element.scaleFactor();
		bx = HighDpiEngine.snap(element.screenX(width));
		by = HighDpiEngine.snap(element.screenY(height));
		bw = element.width() * s;
		bh = element.height() * s;
	}

	private boolean insideBounds(double px, double py) {
		return px >= bx && px < bx + bw && py >= by && py < by + bh;
	}

	private @Nullable HudModule elementAt(double mouseX, double mouseY) {
		if (overPanels(mouseX, mouseY)) {
			return null;
		}
		List<HudModule> elements = HudManager.elements();
		for (int i = elements.size() - 1; i >= 0; i--) {
			HudModule element = elements.get(i);
			if (element.isEnabled()) {
				bounds(element);
				if (insideBounds(mouseX, mouseY)) {
					return element;
				}
			}
		}
		return null;
	}

	private boolean overElementsPanel(double mouseX, double mouseY) {
		float h = Math.min(panelHeight(), PANEL_HEADER + 26f + rows.size() * ROW_H + PANEL_PAD);
		return mouseX >= MARGIN && mouseX < MARGIN + PANEL_W && mouseY >= MARGIN && mouseY < MARGIN + h;
	}

	private boolean overInspector(double mouseX, double mouseY) {
		float w = PANEL_W + 12f;
		float h = Math.min(panelHeight(), PANEL_HEADER + 14f + rightContent + PANEL_PAD);
		return mouseX >= width - MARGIN - w && mouseX < width - MARGIN && mouseY >= MARGIN && mouseY < MARGIN + h;
	}

	private boolean overToolbar(double mouseX, double mouseY) {
		return mouseX >= toolbarLeft && mouseX < toolbarLeft + toolbarWidth && mouseY >= MARGIN && mouseY < MARGIN + TOOLBAR_H + 6f;
	}

	private boolean overPanels(double mouseX, double mouseY) {
		return dragging == null && (overElementsPanel(mouseX, mouseY) || overInspector(mouseX, mouseY) || overToolbar(mouseX, mouseY));
	}

	/**
	 * Snaps the dragged box's left / centre / right (and top / middle / bottom) to the screen edges,
	 * the centre lines and other elements' edges and centres. Result in {@link #snapX} / {@link #snapY}.
	 */
	private void snap(HudModule moving, float x, float y, float w, float h) {
		guideX = Float.NaN;
		guideY = Float.NaN;
		snapX = x;
		snapY = y;
		if (!snap) {
			return;
		}
		bestX = SNAP + 0.001f;
		bestY = SNAP + 0.001f;
		offX = 0f;
		offY = 0f;
		trySnapX(EDGE, x);
		trySnapX(width - EDGE, x + w);
		trySnapX(width * 0.5f, x + w * 0.5f);
		trySnapY(EDGE, y);
		trySnapY(height - EDGE, y + h);
		trySnapY(height * 0.5f, y + h * 0.5f);
		for (HudModule other : HudManager.elements()) {
			if (other == moving || !other.isEnabled()) {
				continue;
			}
			bounds(other);
			for (int t = 0; t < 3; t++) {
				float tx = bx + bw * t * 0.5f;
				float ty = by + bh * t * 0.5f;
				for (int f = 0; f < 3; f++) {
					trySnapX(tx, x + w * f * 0.5f);
					trySnapY(ty, y + h * f * 0.5f);
				}
			}
		}
		snapX = x + offX;
		snapY = y + offY;
	}

	private float bestX;
	private float bestY;
	private float offX;
	private float offY;

	private void trySnapX(float target, float edge) {
		float d = target - edge;
		if (Math.abs(d) < bestX) {
			bestX = Math.abs(d);
			offX = d;
			guideX = target;
		}
	}

	private void trySnapY(float target, float edge) {
		float d = target - edge;
		if (Math.abs(d) < bestY) {
			bestY = Math.abs(d);
			offY = d;
			guideY = target;
		}
	}

	// ---- Input --------------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		boolean left = button == InputConstants.MOUSE_BUTTON_LEFT;
		if (overToolbar(mx, my)) {
			if (left) {
				for (int i = 0; i < 4; i++) {
					if (mx >= toolbarX[i] && mx < toolbarX[i] + toolbarW[i]) {
						toolbarAction(i);
						return true;
					}
				}
			}
			return true;
		}
		if (overElementsPanel(mx, my)) {
			float ty = MARGIN + PANEL_HEADER - 4f;
			if (left && my >= ty && my < ty + 22f) {
				UiSounds.click();
				selected = null;
				inspect(HudManager.theme());
				return true;
			}
			for (ElementRow row : rows) {
				if (row.mouseClicked(mx, my, button)) {
					return true;
				}
			}
			return true;
		}
		if (overInspector(mx, my)) {
			for (UiComponent row : inspectorRows) {
				if (row.mouseClicked(mx, my, button)) {
					capture = row;
					return true;
				}
			}
			return true;
		}
		HudModule hit = elementAt(mx, my);
		if (hit != null) {
			select(hit);
			if (left) {
				bounds(hit);
				dragging = hit;
				grabX = (float) mx - bx;
				grabY = (float) my - by;
			}
			return true;
		}
		selected = null;
		return super.mouseClicked(event, doubleClick);
	}

	private void select(HudModule element) {
		if (selected != element) {
			UiSounds.click();
		}
		selected = element;
		inspect(element);
	}

	private void toolbarAction(int index) {
		UiSounds.click();
		switch (index) {
			case 0 -> snap = !snap;
			case 1 -> grid = !grid;
			case 2 -> {
				for (HudModule element : HudManager.elements()) {
					element.resetLayout();
				}
			}
			default -> onClose();
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging != null) {
			bounds(dragging);
			float w = bw;
			float h = bh;
			snap(dragging, (float) event.x() - grabX, (float) event.y() - grabY, w, h);
			dragging.moveTo(Math.clamp(snapX, 0f, Math.max(0f, width - w)), Math.clamp(snapY, 0f, Math.max(0f, height - h)), width, height);
			return true;
		}
		if (capture != null) {
			capture.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			guideX = Float.NaN;
			guideY = Float.NaN;
			return true;
		}
		if (capture != null) {
			capture.mouseReleased(event.x(), event.y(), event.button());
			capture = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (overElementsPanel(mouseX, mouseY)) {
			leftScroll.target(leftScroll.target() - (float) scrollY * 24f);
			return true;
		}
		if (overInspector(mouseX, mouseY)) {
			rightScroll.target(rightScroll.target() - (float) scrollY * 28f);
			return true;
		}
		HudModule hit = elementAt(mouseX, mouseY);
		if (hit != null) {
			select(hit);
			hit.scale.set(hit.scale.get() + Math.signum(scrollY) * 5.0);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isEscape()) {
			onClose();
			return true;
		}
		if (selected != null) {
			float step = event.hasShiftDown() ? 10f : 1f;
			float dx = event.isLeft() ? -step : event.isRight() ? step : 0f;
			float dy = event.isUp() ? -step : event.isDown() ? step : 0f;
			if (dx != 0f || dy != 0f) {
				bounds(selected);
				selected.moveTo(Math.clamp(bx + dx, 0f, Math.max(0f, width - bw)), Math.clamp(by + dy, 0f, Math.max(0f, height - bh)), width, height);
				return true;
			}
			if (event.key() == InputConstants.KEY_DELETE || event.key() == InputConstants.KEY_BACKSPACE) {
				selected.setEnabled(false);
				selected = null;
				inspect(HudManager.theme());
				return true;
			}
		}
		return super.keyPressed(event);
	}

	// ---- Element list row ---------------------------------------------------------------------

	/** Name + switch; clicking the name selects (and enables) the element, the switch toggles it. */
	private final class ElementRow extends UiComponent {
		private final HudModule element;
		private final TextLabel name;
		private final ToggleSwitch toggle;
		private final Smooth hover = new Smooth(0f, Motion.HOVER);

		ElementRow(HudModule element) {
			this.element = element;
			this.name = new TextLabel(element.displayName(), LiquidText.TextStyle.BODY);
			this.toggle = new ToggleSwitch(new BoolBinding() {
				@Override
				public boolean get() {
					return element.isEnabled();
				}

				@Override
				public void set(boolean value) {
					element.setEnabled(value);
					if (!value && selected == element) {
						selected = null;
					}
				}
			}).accent(Palette.ACCENT_BLUE);
		}

		@Override
		public float preferredHeight(float width) {
			return ROW_H;
		}

		@Override
		public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			boolean over = contains(mouseX, mouseY);
			float h = hover.target(over || selected == element).update(UiFrame.dt);
			if (h > 0.01f) {
				RenderUtils.fill(g, x, y + 1f, width, height - 2f, 6f, ColorUtil.fade(selected == element ? 0x33007AFF : 0x14FFFFFF, h));
			}
			name.maxWidth(width - ToggleSwitch.WIDTH - 16f);
			int color = element.isEnabled() ? Palette.TEXT_PRIMARY : Palette.TEXT_TERTIARY;
			LiquidText.draw(g, name, x + 6f, y + (height - name.capHeight()) * 0.5f, color, false);
			toggle.setBounds(x + width - ToggleSwitch.WIDTH - 4f, y + (height - ToggleSwitch.HEIGHT) * 0.5f, ToggleSwitch.WIDTH, ToggleSwitch.HEIGHT);
			toggle.extract(g, mouseX, mouseY, h);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!contains(mouseX, mouseY)) {
				return false;
			}
			if (mouseX >= x + width - ToggleSwitch.WIDTH - 8f) {
				toggle.toggle();
			} else {
				if (!element.isEnabled()) {
					element.setEnabled(true);
				}
				select(element);
			}
			return true;
		}
	}
}
