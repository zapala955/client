package net.client.visuals.gui.widget;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
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
 * Text glass button. Extends vanilla's {@link AbstractButton} so keyboard focus (Tab / arrows), Enter
 * activation and narration keep working; only the visuals and press timing are replaced.
 *
 * <ul>
 *   <li>hover → scale 1.00 → 1.03, brighter rim, active inner glow, accent bloom</li>
 *   <li>press → contracts to 0.97 while held (snappy), fires on release</li>
 *   <li>keyboard focus → accent focus ring</li>
 * </ul>
 */
public class GlassButton extends AbstractButton {
	public enum Variant {
		PRIMARY,
		SECONDARY,
		CHIP
	}

	private final Variant variant;
	private final TextLabel label;
	private final Runnable action;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final Smooth focus = new Smooth(0f, Motion.HOVER);
	private int accent = Palette.ACCENT_BLUE;
	private float appear = 1f;
	private float appearOffset;
	private boolean pressed;
	private boolean wasHovered;

	public GlassButton(int x, int y, int width, int height, Component message, Variant variant, Runnable action) {
		super(x, y, width, height, message);
		this.variant = variant;
		this.action = action;
		this.label = new TextLabel(message.getString(), variant == Variant.CHIP ? LiquidText.TextStyle.BODY : LiquidText.TextStyle.LABEL);
	}

	public GlassButton accent(int accent) {
		this.accent = accent;
		return this;
	}

	/** Entry-animation hook driven by the owning screen each frame. */
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
		boolean enabled = isActive();
		boolean hovered = enabled && isHovered();
		if (hovered && !wasHovered) {
			UiSounds.hover();
		}
		wasHovered = hovered;
		boolean keyboardFocus = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();

		hover.speed(Motion.speed(Motion.HOVER));
		press.speed(Motion.speed(Motion.PRESS));
		float h = hover.target(hovered).update(dt);
		float p = press.target(pressed && hovered).update(dt);
		float f = focus.target(keyboardFocus).update(dt);

		float x = getX();
		float y = getY() + appearOffset;
		float w = getWidth();
		float ht = getHeight();
		float cx = x + w * 0.5f;
		float cy = y + ht * 0.5f;
		float scale = Motion.interactionScale(h, p, Motion.HOVER_SCALE);
		float radius = variant == Variant.CHIP ? ht * 0.5f : 10f;

		RenderUtils.pushAlpha(appear * (enabled ? 1f : 0.45f) * this.alpha);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);

		GlassRenderer.accentGlow(g, x, y, w, ht, radius, accent, h * (variant == Variant.CHIP ? 0.6f : 1f));
		int fillTop = variant == Variant.PRIMARY ? ColorUtil.lerp(0x40334155, 0x664B6382, h) : ColorUtil.lerp(0x2E334155, 0x55475569, h);
		int fillBottom = variant == Variant.PRIMARY ? ColorUtil.lerp(0x331E293B, 0x55334155, h) : ColorUtil.lerp(0x241E293B, 0x44334155, h);
		GlassRenderer.surface(g, x, y, w, ht, radius, 0.6f + 1.4f * h, fillTop, fillBottom,
				ColorUtil.lerp(Palette.GLASS_BORDER_HIGHLIGHT, Palette.GLASS_BORDER_HOVER, h), Palette.GLASS_BORDER);
		GlassRenderer.innerGlow(g, x, y, w, ht, radius, 0xFFFFFFFF, h);

		if (f > 0.01f) {
			RenderUtils.stroke(g, x - 2f, y - 2f, w + 4f, ht + 4f, radius + 2f, GlassRenderer.hairline() * 2f,
					ColorUtil.fade(accent, f), ColorUtil.fade(accent, f * 0.7f));
		}

		int textColor = ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, Math.max(h, variant == Variant.PRIMARY ? 0.7f : 0.35f));
		LiquidText.drawCentered(g, label, cx, cy - label.capHeight() * 0.5f, textColor, true);

		pose.popMatrix();
		RenderUtils.popAlpha();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
