package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.function.BooleanSupplier;

/**
 * A capsule button: primary (accent fill), danger (red fill), secondary (glass), destructive (red label) or plain
 * (label only, for toolbars). Optional leading icon; size follows the label. Hover brightens,
 * press contracts, the action fires on release inside.
 */
public final class PillButton extends UiComponent {
	public enum Kind { PRIMARY, LIGHT, DANGER, SECONDARY, DESTRUCTIVE, PLAIN }

	private final TextLabel label;
	private final int icon;
	private final Kind kind;
	private final Runnable action;
	private BooleanSupplier enabled = () -> true;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final Smooth active = new Smooth(1f, Motion.TOGGLE);
	private boolean pressed;

	public PillButton(String text, int icon, Kind kind, Runnable action) {
		this.label = new TextLabel(text, LiquidText.TextStyle.LABEL);
		this.icon = icon;
		this.kind = kind;
		this.action = action;
		this.width = naturalWidth();
		this.height = Ui.CONTROL_H;
	}

	public PillButton enabledWhen(BooleanSupplier condition) {
		this.enabled = condition;
		return this;
	}

	public void setText(String text) {
		label.setText(text);
	}

	public float naturalWidth() {
		float w = label.text().isEmpty() ? 0f : label.width();
		return (icon != 0 ? 11f + (w > 0f ? 5f : 0f) : 0f) + w + (label.text().isEmpty() ? 11f : 24f);
	}

	@Override
	public float preferredHeight(float width) {
		return Ui.CONTROL_H;
	}

	private boolean isEnabled() {
		return enabled.getAsBoolean();
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		boolean on = isEnabled();
		boolean over = on && contains(mouseX, mouseY);
		float h = hover.target(over).update(dt);
		float p = press.target(pressed && over).update(dt);
		float a = active.target(on ? 1f : 0.38f).update(dt);
		float scale = Motion.interactionScale(h, p, 0.02f);
		float r = height * 0.5f;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x + width * 0.5f, y + height * 0.5f);
		pose.scale(scale, scale);
		pose.translate(-(x + width * 0.5f), -(y + height * 0.5f));
		RenderUtils.pushAlpha(a);
		int textColor;
		switch (kind) {
			case PRIMARY -> {
				RenderUtils.shadow(g, x, y + 1.5f, width, height, r, 4f, 0x4D0A84FF);
				RenderUtils.gradient(g, x, y, width, height, r, ColorUtil.lerp(0xFF2A93FF, 0xFF52A8FF, h), ColorUtil.lerp(Ui.ACCENT, 0xFF2A93FF, h));
				RenderUtils.stroke(g, x, y, width, height, r, HighDpiEngine.px(), 0x47FFFFFF, 0x0AFFFFFF);
				textColor = 0xFFFFFFFF;
			}
			case LIGHT -> {
				// Solid white with dark type: the one obvious action on a dark, minimal screen.
				RenderUtils.shadow(g, x, y + 2f, width, height, r, 6f, 0x40000000);
				RenderUtils.fill(g, x, y, width, height, r, ColorUtil.lerp(0xF0F5F5F7, 0xFFFFFFFF, h));
				textColor = 0xFF1C1C1E;
			}
			case DANGER -> {
				RenderUtils.shadow(g, x, y + 1.5f, width, height, r, 4f, 0x4DFF453A);
				RenderUtils.gradient(g, x, y, width, height, r, ColorUtil.lerp(0xFFFF5A50, 0xFFFF7068, h), ColorUtil.lerp(Ui.DESTRUCTIVE, 0xFFFF5A50, h));
				RenderUtils.stroke(g, x, y, width, height, r, HighDpiEngine.px(), 0x47FFFFFF, 0x0AFFFFFF);
				textColor = 0xFFFFFFFF;
			}
			case SECONDARY, DESTRUCTIVE -> {
				RenderUtils.fill(g, x, y, width, height, r, ColorUtil.lerp(Ui.FILL_CONTROL, Ui.FILL_CONTROL_HOVER, h));
				RenderUtils.stroke(g, x, y, width, height, r, HighDpiEngine.px(), 0x29FFFFFF, 0x0AFFFFFF);
				textColor = kind == Kind.DESTRUCTIVE ? Ui.DESTRUCTIVE : Ui.TEXT;
			}
			default -> {
				if (h > 0.01f) {
					RenderUtils.fill(g, x, y, width, height, r, ColorUtil.fade(Ui.FILL_CONTROL, h));
				}
				textColor = ColorUtil.lerp(Ui.TEXT_2, Ui.TEXT, h);
			}
		}
		float content = (icon != 0 ? 11f : 0f) + (label.text().isEmpty() ? 0f : label.width() + (icon != 0 ? 5f : 0f));
		float cx = x + (width - content) * 0.5f;
		float cy = y + height * 0.5f;
		if (icon != 0) {
			IconFont.draw(g, icon, cx + 5.5f, cy, 10.5f, textColor, 0);
			cx += 11f + 5f;
		}
		if (!label.text().isEmpty()) {
			LiquidText.draw(g, label, cx, cy - label.capHeight() * 0.5f, textColor, false);
		}
		RenderUtils.popAlpha();
		pose.popMatrix();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT && isEnabled() && contains(mouseX, mouseY)) {
			pressed = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!pressed) {
			return false;
		}
		pressed = false;
		if (isEnabled() && contains(mouseX, mouseY)) {
			UiSounds.click();
			action.run();
		}
		return true;
	}
}
