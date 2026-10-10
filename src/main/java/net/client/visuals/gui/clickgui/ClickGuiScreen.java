package net.client.visuals.gui.clickgui;

import net.client.visuals.gui.window.TextInputs;
import net.client.visuals.LiquidClient;
import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.hud.HudEditorScreen;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.Palette;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Easing;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Tween;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.module.ModuleManager;
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

/**
 * Keybind-activated ClickGUI overlay (Right Shift by default).
 *
 * <p>Lifecycle: {@code init} → open tween (scale 0.92 → 1 with a soft overshoot, fade in) →
 * interactive → {@link #requestClose()} → faster eased exit → the parent screen (or gameplay) is
 * restored once the exit tween finishes. Closing mid-open reverses smoothly from the current
 * progress.</p>
 *
 * <p>Does not pause singleplayer, matching how utility-client GUIs behave. Keyboard input goes to the
 * window first (module key binding, type-anywhere search).</p>
 */
public final class ClickGuiScreen extends Screen implements LiquidScreen {
	private final @Nullable Screen parent;
	private final DeltaTimer timer = new DeltaTimer();
	private final Tween transition = new Tween(Motion.OPEN_SECONDS, Easing.OUT_BACK);
	private final ClickGuiWindow window;
	private boolean closing;
	private boolean closeScheduled;
	private boolean laidOut;

	public ClickGuiScreen(@Nullable Screen parent) {
		super(Component.translatable("liquidclient.clickgui.title"));
		this.parent = parent;
		this.window = new ClickGuiWindow(this);
	}

	@Override
	protected void init() {
		ShaderSupport.revalidate();
		window.layout(width, height, !laidOut);
		if (!laidOut) {
			laidOut = true;
			timer.reset();
			UiFrame.time = 0f;
			transition.play(Motion.seconds(Motion.OPEN_SECONDS), Easing.OUT_BACK);
			UiSounds.open();
			// Type-anywhere search: SDL text input stays on while the GUI is open.
			TextInputs.focus(this, this, true);
		}
	}

	@Override
	protected void repositionElements() {
		window.layout(width, height, false);
	}

	/** Opens the HUD editor; closing it comes back to a fresh ClickGUI. */
	public void openHudEditor() {
		window.savePosition();
		minecraft.gui.setScreen(new HudEditorScreen(new ClickGuiScreen(parent)));
	}

	/** Starts the exit animation; the screen actually closes when it finishes. */
	public void requestClose() {
		if (!closing) {
			closing = true;
			UiSounds.close();
			window.savePosition();
			LiquidConfig.save();
			ModuleManager.saveIfDirty();
			transition.reverse(Motion.seconds(Motion.CLOSE_SECONDS), Easing.IN_CUBIC);
		}
	}

	@Override
	public void onClose() {
		requestClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (minecraft.level == null) {
			// Opened from the main menu: same wallpaper (and per-panel frost) as the menu.
			BackgroundEngine.INSTANCE.extract(g, width, height);
		} else {
			// In game: frost the world through the engine's blur pass.
			GlassRenderer.beginFrame(BlurEngine.frostBackdrop(g));
		}
		float t = Easing.clamp01(transition.raw());
		int top = ColorUtil.fade(Palette.BACKDROP_TINT_TOP, t * (GlassRenderer.frosted() ? 0.8f : 1f));
		int bottom = ColorUtil.fade(Palette.BACKDROP_TINT_BOTTOM, t * (GlassRenderer.frosted() ? 0.8f : 1f));
		g.fillGradient(0, 0, width, height, top, bottom);
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		transition.update(UiFrame.dt);
		if (closing && transition.finished()) {
			if (!closeScheduled) {
				// Don't swap screens in the middle of extraction; do it before the next frame.
				closeScheduled = true;
				minecraft.execute(() -> {
					// If vanilla replaced us meanwhile (kicked → DisconnectedScreen), leave it alone.
					if (minecraft.gui.screen() == this) {
						minecraft.gui.setScreen(parent);
					}
				});
			}
			return;
		}

		float eased = transition.value();
		float alpha = Easing.clamp01(transition.raw() * 1.6f);
		float scale = 0.92f + 0.08f * eased;
		float cx = window.centerX();
		float cy = window.centerY();

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy + (1f - eased) * 6f);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);
		RenderUtils.pushAlpha(alpha);
		window.extract(g, mouseX, mouseY);
		RenderUtils.popAlpha();
		pose.popMatrix();

		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	// ---- Input --------------------------------------------------------------------------------

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (closing) {
			return true;
		}
		// Binding mode and the search field get the key first (Escape clears a search before closing).
		if (window.keyPressed(event)) {
			return true;
		}
		if (event.isEscape() || LiquidClient.OPEN_CLICK_GUI.matches(event) && !window.capturesKeyboard()) {
			requestClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return !closing && window.charTyped(event) || super.charTyped(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (closing) {
			return true;
		}
		return window.mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return window.mouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		return window.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return window.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void removed() {
		TextInputs.focus(this, this, false);
		window.savePosition();
		LiquidConfig.save();
		ModuleManager.saveIfDirty();
	}
}
