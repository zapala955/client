package net.client.visuals.gui.window;

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

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/** A round glass icon button with a tooltip and an optional count badge (Control Center style). */
public final class RoundButton {
	public static final float SIZE = 30f;

	private final int icon;
	private final List<TextLabel> tooltip;
	private final Runnable action;
	private BooleanSupplier enabled = () -> true;
	private IntSupplier badge = () -> 0;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final TextLabel badgeLabel = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private int badgeShown = -1;
	private boolean pressed;
	private float cx;
	private float cy;

	public RoundButton(int icon, String tooltip, Runnable action) {
		this.icon = icon;
		this.tooltip = Ui.wrap(tooltip, 200f, LiquidText.TextStyle.BODY);
		this.action = action;
	}

	public RoundButton enabledWhen(BooleanSupplier condition) {
		this.enabled = condition;
		return this;
	}

	public RoundButton badge(IntSupplier count) {
		this.badge = count;
		return this;
	}

	public List<TextLabel> tooltipLines() {
		return tooltip;
	}

	public boolean over(double mx, double my) {
		double dx = mx - cx;
		double dy = my - cy;
		double r = SIZE * 0.5 + 0.5;
		return dx * dx + dy * dy <= r * r;
	}

	/** @return whether the pointer is over it */
	public boolean extract(GuiGraphicsExtractor g, float cx, float cy, int mouseX, int mouseY) {
		this.cx = cx;
		this.cy = cy;
		float dt = UiFrame.dt;
		boolean on = enabled.getAsBoolean();
		boolean over = over(mouseX, mouseY);
		float hv = hover.target(on && over).update(dt);
		float p = press.target(pressed && on && over).update(dt);
		float r = SIZE * 0.5f * Motion.interactionScale(hv, p, 0.06f);
		RenderUtils.pushAlpha(on ? 1f : 0.4f);
		RenderUtils.circle(g, cx, cy, r, ColorUtil.lerp(0x1AFFFFFF, 0x33FFFFFF, hv));
		RenderUtils.stroke(g, cx - r, cy - r, r * 2f, r * 2f, r, HighDpiEngine.px(), 0x29FFFFFF, 0x0AFFFFFF);
		IconFont.draw(g, icon, cx, cy, 12.5f, ColorUtil.lerp(0xD9FFFFFF, 0xFFFFFFFF, hv), 0);
		int count = on ? badge.getAsInt() : 0;
		if (count > 0) {
			if (count != badgeShown) {
				badgeShown = count;
				badgeLabel.setText(count > 9 ? "9+" : Integer.toString(count));
			}
			float bw = Math.max(12f, badgeLabel.width() + 7f);
			float bx = cx + 7f;
			float by = cy - 14f;
			RenderUtils.fill(g, bx, by, bw, 12f, 6f, Ui.DESTRUCTIVE);
			LiquidText.draw(g, badgeLabel, bx + (bw - badgeLabel.width()) * 0.5f, by + 6f - badgeLabel.capHeight() * 0.5f, 0xFFFFFFFF, false);
		}
		RenderUtils.popAlpha();
		return over;
	}

	public boolean press(double mx, double my) {
		if (enabled.getAsBoolean() && over(mx, my)) {
			pressed = true;
			return true;
		}
		return false;
	}

	public boolean release(double mx, double my) {
		if (!pressed) {
			return false;
		}
		pressed = false;
		if (enabled.getAsBoolean() && over(mx, my)) {
			UiSounds.click();
			action.run();
		}
		return true;
	}
}
