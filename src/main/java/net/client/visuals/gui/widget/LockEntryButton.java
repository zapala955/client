package net.client.visuals.gui.widget;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

/**
 * A lock-screen entry, modelled on the macOS user picker: a clear-glass "avatar" disc (the frosted
 * photo, lifted with white) holding a line icon, and a name. At rest it's just the disc and text over
 * the wallpaper; hovering raises a clear-glass pill behind it (1.00 → 1.03), pressing contracts to
 * 0.97 and the action fires on release.
 */
public final class LockEntryButton extends AbstractButton {
	public static final int HEIGHT = 28;
	private static final float AVATAR = 22f;
	/** 12 GUI units: Lucide's 2/24 stroke lands on exactly one GUI pixel. */
	private static final float GLYPH = 12f;
	private static final float GAP = 9f;
	private static final float PAD = 3f;

	private final TextLabel label;
	private final DockIcons.Icon icon;
	private final Runnable action;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final Smooth focus = new Smooth(0f, Motion.HOVER);
	private float appear = 1f;
	private float appearOffset;
	private boolean pressed;
	private boolean wasHovered;

	public LockEntryButton(int x, int y, Component message, DockIcons.Icon icon, Runnable action) {
		super(x, y, 10, HEIGHT, message);
		this.label = new TextLabel(message.getString(), LiquidText.TextStyle.LABEL);
		this.icon = icon;
		this.action = action;
		setWidth(Math.round(PAD + AVATAR + GAP + label.width() + 12f));
	}

	public void setAppear(float progress, float offsetY) {
		this.appear = progress;
		this.appearOffset = offsetY;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		pressed = true;
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		boolean fire = pressed && isMouseOver(event.x(), event.y());
		pressed = false;
		if (fire) {
			onPress(event);
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		action.run();
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		UiSounds.click();
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		float dt = UiFrame.dt;
		boolean hovered = isActive() && isHovered();
		if (hovered && !wasHovered) {
			UiSounds.hover();
		}
		wasHovered = hovered;
		boolean keyboardFocus = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
		hover.speed(Motion.speed(Motion.HOVER));
		press.speed(Motion.speed(Motion.PRESS));
		float h = hover.target(hovered || keyboardFocus).update(dt);
		float p = press.target(pressed && hovered).update(dt);
		float f = focus.target(keyboardFocus).update(dt);

		float x = getX();
		float y = getY() + appearOffset;
		float w = getWidth();
		float ht = getHeight();
		float cx = x + w * 0.5f;
		float cy = y + ht * 0.5f;
		float scale = Motion.interactionScale(h, p, Motion.HOVER_SCALE);

		RenderUtils.pushAlpha(appear * (isActive() ? 1f : 0.45f));
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);

		if (h > 0.01f) {
			RenderUtils.pushAlpha(h);
			GlassRenderer.clear(g, x, y, w, ht, ht * 0.5f, h);
			RenderUtils.popAlpha();
		}
		if (f > 0.01f) {
			RenderUtils.stroke(g, x - 2f, y - 2f, w + 4f, ht + 4f, ht * 0.5f + 2f, HighDpiEngine.px() * 2f,
					ColorUtil.fade(Palette.ACCENT_BLUE, f), ColorUtil.fade(Palette.ACCENT_BLUE, f * 0.7f));
		}

		// Avatar: clear-glass disc + line icon.
		float ax = x + PAD;
		float ay = cy - AVATAR * 0.5f;
		float r = AVATAR * 0.5f;
		GlassRenderer.clear(g, ax, ay, AVATAR, AVATAR, r, h);
		DockIcons.glyph(g, icon, ax + r, ay + r, GLYPH, 0xFFFFFFFF, Palette.ICON_SHADOW);

		LiquidText.drawSoft(g, label, ax + AVATAR + GAP, cy - label.capHeight() * 0.5f, Palette.TEXT_PRIMARY);
		pose.popMatrix();
		RenderUtils.popAlpha();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
