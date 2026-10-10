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

/**
 * A menu-bar status icon (top-right, like macOS): just a white line icon with a soft shadow at rest;
 * hover raises a small clear-glass circle and a name tag underneath. Fires on release.
 */
public final class StatusIconButton extends AbstractButton {
	public static final int SIZE = 20;
	/** Same size as the entry icons: the 2/24 stroke is exactly one GUI pixel. */
	private static final float GLYPH = 12f;

	private final DockIcons.Icon icon;
	private final TextLabel label;
	private final Runnable action;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private float appear = 1f;
	private boolean pressed;

	public StatusIconButton(int x, int y, Component message, DockIcons.Icon icon, Runnable action) {
		super(x, y, SIZE, SIZE, message);
		this.icon = icon;
		this.label = new TextLabel(message.getString(), LiquidText.TextStyle.BODY);
		this.action = action;
	}

	public void setAppear(float progress) {
		this.appear = progress;
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
		boolean keyboardFocus = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
		boolean hovered = isActive() && isHovered();
		hover.speed(Motion.speed(Motion.HOVER));
		press.speed(Motion.speed(Motion.PRESS));
		float h = hover.target(hovered || keyboardFocus).update(dt);
		float p = press.target(pressed && hovered).update(dt);

		float x = getX();
		float y = getY();
		float s = getWidth();
		float cx = x + s * 0.5f;
		float cy = y + s * 0.5f;
		float scale = Motion.interactionScale(h, p, Motion.HOVER_SCALE * 2f);

		RenderUtils.pushAlpha(appear * (isActive() ? 1f : 0.4f));
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);
		if (h > 0.01f) {
			RenderUtils.pushAlpha(h);
			GlassRenderer.clear(g, x, y, s, s, s * 0.5f, h);
			RenderUtils.popAlpha();
		}
		// The baked soft shadow keeps white icons legible over bright clouds.
		DockIcons.glyph(g, icon, cx, cy, GLYPH, ColorUtil.lerp(0xEBFFFFFF, 0xFFFFFFFF, h), Palette.ICON_SHADOW);
		pose.popMatrix();

		if (h > 0.01f) {
			RenderUtils.pushAlpha(h);
			float tw = label.width() + 14f;
			float th = 16f;
			float tx = HighDpiEngine.snap(Math.min(cx - tw * 0.5f, Minecraft.getInstance().getWindow().getGuiScaledWidth() - tw - 6f));
			float ty = HighDpiEngine.snap(y + s + 5f + (1f - h) * -3f);
			GlassRenderer.panel(g, tx, ty, tw, th, th * 0.5f, 1.5f);
			LiquidText.draw(g, label, tx + 7f, ty + th * 0.5f - label.capHeight() * 0.5f, Palette.TEXT_PRIMARY, true);
			RenderUtils.popAlpha();
		}
		RenderUtils.popAlpha();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
