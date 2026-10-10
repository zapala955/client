package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
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
import net.client.visuals.render.shader.BlurEngine;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base of the Liquid windows (Singleplayer, Multiplayer, Settings): a macOS window floating over
 * the lock-screen wallpaper (or the blurred world in game) with traffic lights, a unified title /
 * toolbar, a body the subclass draws, and optional modal {@link Sheet}s and {@link Popover}s.
 *
 * <p>Input order: sheet → pop-up → toolbar widgets → body. Keyboard focus belongs to at most one
 * component; while a text field has it, SDL text input is on for this screen.</p>
 */
public abstract class WindowScreen extends Screen implements LiquidScreen {
	protected static final float TITLE_H = 44f;
	private static final float LIGHT_R = 4.5f;
	private static final int[] LIGHTS = {Palette.TRAFFIC_RED, Palette.TRAFFIC_YELLOW, Palette.TRAFFIC_GREEN};

	protected final @Nullable Screen parent;
	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth appear = new Smooth(0f, 8f);
	private final Smooth lightsHover = new Smooth(0f, Motion.HOVER);
	private final Smooth sheetDim = new Smooth(0f, 12f);
	private final TextLabel titleLabel;
	private final TextLabel subtitleLabel = new TextLabel("", LiquidText.TextStyle.CAPTION);
	/** Toolbar / footer widgets drawn and routed by the base class. */
	protected final List<UiComponent> widgets = new ArrayList<>();
	protected float wx;
	protected float wy;
	protected float ww;
	protected float wh;
	private @Nullable UiComponent focus;
	private @Nullable UiComponent capture;
	private @Nullable Popover popover;
	private @Nullable Sheet sheet;
	private boolean firstInit = true;
	private final Smooth tooltipFade = new Smooth(0f, 16f);
	private @Nullable Object tooltipOwner;
	private @Nullable Object tooltipRequest;
	private List<TextLabel> tooltipLines = List.of();
	private List<TextLabel> tooltipRequestLines = List.of();
	private float tooltipDwell;

	protected WindowScreen(Component title, @Nullable Screen parent) {
		super(title);
		this.parent = parent;
		this.titleLabel = new TextLabel(title.getString(), LiquidText.TextStyle.TITLE);
	}

	// ---- Subclass hooks ------------------------------------------------------------------------

	protected abstract float preferredWidth();

	protected abstract float preferredHeight();

	/** Creates / positions widgets for the current window rect (on open and on resize). */
	protected abstract void layoutWindow(boolean first);

	/** Draws the body (everything below the title bar). */
	protected abstract void extractBody(GuiGraphicsExtractor g, int mouseX, int mouseY);

	protected boolean bodyClicked(double mx, double my, int button, boolean doubleClick) {
		return false;
	}

	protected boolean bodyReleased(double mx, double my, int button) {
		return false;
	}

	protected boolean bodyDragged(double mx, double my, int button, double dx, double dy) {
		return false;
	}

	protected boolean bodyScrolled(double mx, double my, double amount) {
		return false;
	}

	protected boolean bodyKey(KeyEvent event) {
		return false;
	}

	protected void setSubtitle(String text) {
		subtitleLabel.setText(text);
	}

	/** Left edge of the title text (after the traffic lights). */
	protected float titleRight() {
		return wx + 72f + Math.max(titleLabel.width(), subtitleLabel.width());
	}

	// ---- Lifecycle -----------------------------------------------------------------------------

	@Override
	protected void init() {
		ShaderSupport.revalidate();
		ww = Math.min(width - 24f, preferredWidth());
		wh = Math.min(height - 24f, preferredHeight());
		wx = HighDpiEngine.snap((width - ww) * 0.5f);
		wy = HighDpiEngine.snap((height - wh) * 0.5f);
		if (firstInit) {
			timer.reset();
		}
		layoutWindow(firstInit);
		firstInit = false;
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void removed() {
		focus(null);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	// ---- Rendering -----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (minecraft.level == null) {
			BackgroundEngine.INSTANCE.extract(g, width, height);
			RenderUtils.gradient(g, 0f, 0f, width, height, 0f, 0x2E000000, 0x52000000);
		} else {
			GlassRenderer.beginFrame(BlurEngine.frostBackdrop(g));
			RenderUtils.gradient(g, 0f, 0f, width, height, 0f, 0x47000000, 0x73000000);
		}
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		float dt = UiFrame.dt;
		float a = appear.target(1f).update(dt);
		float eased = 1f - (1f - a) * (1f - a) * (1f - a);
		boolean modal = sheet != null;
		int mx = modal ? -10000 : mouseX;
		int my = modal ? -10000 : mouseY;
		if (popover != null && popover.contains(mouseX, mouseY)) {
			mx = -10000;
			my = -10000;
		}

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		float s = 0.965f + 0.035f * eased;
		pose.translate(wx + ww * 0.5f, wy + wh * 0.5f + (1f - eased) * 6f);
		pose.scale(s, s);
		pose.translate(-(wx + ww * 0.5f), -(wy + wh * 0.5f));
		RenderUtils.pushAlpha(Math.min(1f, a * 1.4f));

		Ui.window(g, wx, wy, ww, wh);
		extractTitleBar(g, mx, my);
		extractBody(g, mx, my);
		for (UiComponent widget : widgets) {
			widget.extract(g, mx, my);
		}

		// Sheet: dim the window, then the sheet on top.
		float dim = sheetDim.target(sheet != null && !sheet.closing() ? 1f : 0f).update(dt);
		if (dim > 0.01f) {
			RenderUtils.fill(g, wx, wy, ww, wh, Ui.WINDOW_RADIUS, ColorUtil.fade(0x73000000, dim));
		}
		RenderUtils.popAlpha();
		pose.popMatrix();

		if (sheet != null) {
			boolean covered = popover != null && popover.contains(mouseX, mouseY);
			sheet.extract(g, covered ? -10000 : mouseX, covered ? -10000 : mouseY);
			if (sheet.finished()) {
				sheet = null;
			}
		}
		if (popover != null) {
			popover.extract(g, mouseX, mouseY);
			if (popover.finished()) {
				popover = null;
			}
		}
		extractTooltip(g, mouseX, mouseY, dt);
		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	private void extractTitleBar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float cy = wy + 20f;
		float hovered = lightsHover.target(lightAt(mouseX, mouseY) >= 0).update(UiFrame.dt);
		for (int i = 0; i < 3; i++) {
			float lx = lightX(i);
			RenderUtils.circle(g, lx, cy, LIGHT_R, LIGHTS[i]);
			RenderUtils.stroke(g, lx - LIGHT_R, cy - LIGHT_R, LIGHT_R * 2f, LIGHT_R * 2f, LIGHT_R, HighDpiEngine.px(), 0x26000000, 0x40000000);
			if (hovered > 0.01f) {
				int glyph = ColorUtil.fade(0x99000000, hovered);
				if (i == 0) {
					RenderUtils.line(g, lx - 1.8f, cy - 1.8f, lx + 1.8f, cy + 1.8f, 1f, glyph);
					RenderUtils.line(g, lx - 1.8f, cy + 1.8f, lx + 1.8f, cy - 1.8f, 1f, glyph);
				} else if (i == 1) {
					RenderUtils.line(g, lx - 2.2f, cy, lx + 2.2f, cy, 1f, glyph);
				} else {
					RenderUtils.line(g, lx - 2f, cy, lx + 2f, cy, 1f, glyph);
					RenderUtils.line(g, lx, cy - 2f, lx, cy + 2f, 1f, glyph);
				}
			}
		}
		float tx = wx + 72f;
		if (subtitleLabel.text().isEmpty()) {
			LiquidText.draw(g, titleLabel, tx, cy - titleLabel.capHeight() * 0.5f, Ui.TEXT, false);
		} else {
			LiquidText.draw(g, titleLabel, tx, cy - 7.5f, Ui.TEXT, false);
			LiquidText.draw(g, subtitleLabel, tx, cy + 5f, Ui.TEXT_2, false);
		}
		Ui.separator(g, wx, wy + TITLE_H, ww);
	}

	private float lightX(int i) {
		return wx + 20f + i * 15f;
	}

	private int lightAt(double mx, double my) {
		for (int i = 0; i < 3; i++) {
			double dx = mx - lightX(i);
			double dy = my - (wy + 20f);
			if (dx * dx + dy * dy <= (LIGHT_R + 2.5f) * (LIGHT_R + 2.5f)) {
				return i;
			}
		}
		return -1;
	}

	// ---- Popovers / sheets / focus -------------------------------------------------------------

	public void openPopover(Popover menu) {
		if (popover != null) {
			popover.close();
		}
		popover = menu;
	}

	public void openSheet(Sheet modal) {
		sheet = modal;
		focus(modal.firstFocusable());
	}

	/**
	 * Requests a tooltip for this frame. It appears after the pointer rests on the same
	 * {@code owner} for a moment (like macOS help tags) and fades out when it leaves.
	 * Pass a list built once, not per frame.
	 */
	public void tooltip(Object owner, List<TextLabel> lines) {
		tooltipRequest = owner;
		tooltipRequestLines = lines;
	}

	private void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY, float dt) {
		Object request = tooltipRequest;
		tooltipRequest = null;
		if (request == null || hasSheet()) {
			tooltipDwell = 0f;
			tooltipFade.target(0f);
		} else {
			if (request != tooltipOwner) {
				tooltipDwell = tooltipFade.get() > 0.5f ? 0.45f : 0f; // moving between items keeps it up
				tooltipOwner = request;
			}
			tooltipLines = tooltipRequestLines;
			tooltipDwell += dt;
			tooltipFade.target(tooltipDwell >= 0.45f ? 1f : 0f);
		}
		float a = tooltipFade.update(dt);
		if (a > 0.01f) {
			Ui.tooltip(g, tooltipLines, mouseX, mouseY, width, height, a);
		}
	}

	public boolean hasSheet() {
		return sheet != null && !sheet.closing();
	}

	public void focus(@Nullable UiComponent component) {
		if (focus == component) {
			return;
		}
		if (focus != null) {
			focus.setFocused(false);
			if (focus instanceof TextField) {
				TextInputs.focus(this, focus, false);
			}
		}
		focus = component;
		if (component != null) {
			component.setFocused(true);
		}
		if (component instanceof TextField) {
			TextInputs.focus(this, component, true);
		} else {
			minecraft.onTextInputFocusChange(this, false);
		}
	}

	// ---- Input ---------------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		if (popover != null && popover.mouseClicked(mx, my)) {
			return true;
		}
		if (sheet != null) {
			return sheet.closing() || sheet.mouseClicked(mx, my, button);
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			int light = lightAt(mx, my);
			if (light == 0) {
				UiSounds.click();
				onClose();
				return true;
			}
		}
		for (UiComponent widget : widgets) {
			if (widget.mouseClicked(mx, my, button)) {
				capture = widget;
				focus(widget.focusable() ? widget : null);
				return true;
			}
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && focus != null) {
			focus(null);
		}
		return bodyClicked(mx, my, button, doubleClick) || super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (sheet != null) {
			return sheet.mouseReleased(event.x(), event.y(), event.button());
		}
		UiComponent captured = capture;
		capture = null;
		if (captured != null && captured.mouseReleased(event.x(), event.y(), event.button())) {
			return true;
		}
		return bodyReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (sheet != null) {
			return sheet.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
		}
		if (capture != null && capture.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) {
			return true;
		}
		return bodyDragged(event.x(), event.y(), event.button(), dragX, dragY) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (popover != null && popover.mouseScrolled(mouseX, mouseY, scrollY)) {
			return true;
		}
		if (sheet != null) {
			return true;
		}
		for (UiComponent widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, scrollY)) {
				return true;
			}
		}
		return bodyScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (popover != null && event.isEscape()) {
			popover.close();
			return true;
		}
		if (sheet != null) {
			return sheet.keyPressed(event, focus);
		}
		if (focus != null) {
			if (event.isEscape()) {
				if (focus instanceof TextField field && !field.value().isEmpty()) {
					field.setValue("");
				} else {
					focus(null);
				}
				return true;
			}
			if (focus.keyPressed(event)) {
				return true;
			}
		}
		if (bodyKey(event)) {
			return true;
		}
		if (event.isEscape()) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (sheet != null) {
			return sheet.charTyped(event, focus);
		}
		return focus != null && focus.charTyped(event) || super.charTyped(event);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false; // handled above (sheets, pop-ups and fields first)
	}
}
