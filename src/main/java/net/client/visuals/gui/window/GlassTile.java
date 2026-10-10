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
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * A Control Center module on smoked glass: a colour icon disc and a label (row tile), or, as a
 * hero tile, a larger disc on top with a title and a caption below. Lifts on hover, presses in,
 * fires on release inside. Disabled tiles dim and can explain why in a tooltip.
 */
public final class GlassTile {
	private final TextLabel label;
	private final TextLabel caption = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final int icon;
	private final int color;
	private final Runnable action;
	private BooleanSupplier enabled = () -> true;
	private List<TextLabel> tooltip = List.of();
	private boolean hero;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final Smooth active = new Smooth(1f, Motion.TOGGLE);
	private boolean pressed;
	private float x;
	private float y;
	private float w;
	private float h;

	public GlassTile(String label, int icon, int color, Runnable action) {
		// Vanilla's "Options..." style ellipses read as noise on a tile.
		String clean = label.endsWith("...") ? label.substring(0, label.length() - 3)
				: label.endsWith("…") ? label.substring(0, label.length() - 1) : label;
		this.label = new TextLabel(clean.strip(), LiquidText.TextStyle.LABEL);
		this.icon = icon;
		this.color = color;
		this.action = action;
	}

	public GlassTile hero(String captionText) {
		this.hero = true;
		this.caption.setText(captionText);
		return this;
	}

	public GlassTile enabledWhen(BooleanSupplier condition) {
		this.enabled = condition;
		return this;
	}

	/** Tooltip lines (e.g. why it's disabled); built once by the caller. */
	public GlassTile tooltip(List<TextLabel> lines) {
		this.tooltip = lines;
		return this;
	}

	public List<TextLabel> tooltipLines() {
		return tooltip;
	}

	public boolean over(double mx, double my) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** @return whether the pointer is over the tile */
	public boolean extract(GuiGraphicsExtractor g, float x, float y, float w, float h, int mouseX, int mouseY) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		float dt = UiFrame.dt;
		boolean on = enabled.getAsBoolean();
		boolean over = over(mouseX, mouseY);
		float hv = hover.target(on && over).update(dt);
		float p = press.target(pressed && on && over).update(dt);
		float a = active.target(on ? 1f : 0.4f).update(dt);
		float s = Motion.interactionScale(hv, p, hero ? 0.02f : 0.025f);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x + w * 0.5f, y + h * 0.5f);
		pose.scale(s, s);
		pose.translate(-(x + w * 0.5f), -(y + h * 0.5f));
		RenderUtils.pushAlpha(a);
		float r = hero ? 16f : 14f;
		RenderUtils.fill(g, x, y, w, h, r, ColorUtil.lerp(hero ? 0x1CFFFFFF : 0x14FFFFFF, 0x2BFFFFFF, hv));
		RenderUtils.light(g, x, y, w, h * 0.5f, r, ColorUtil.fade(0x12FFFFFF, 0.6f + 0.4f * hv), 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), ColorUtil.lerp(0x24FFFFFF, 0x3DFFFFFF, hv), 0x08FFFFFF);
		if (hero) {
			float d = 30f;
			float cx = x + 12f + d * 0.5f;
			float cy = y + 12f + d * 0.5f;
			disc(g, cx, cy, d, 15f);
			label.maxWidth(w - 24f);
			caption.maxWidth(w - 24f);
			LiquidText.draw(g, label, x + 12f, y + h - 28f, Ui.TEXT, false);
			LiquidText.draw(g, caption, x + 12f, y + h - 15f, Ui.TEXT_2, false);
		} else {
			float d = 26f;
			float cx = x + 10f + d * 0.5f;
			float cy = y + h * 0.5f;
			disc(g, cx, cy, d, 13f);
			label.maxWidth(w - d - 30f);
			LiquidText.draw(g, label, x + 10f + d + 10f, cy - label.capHeight() * 0.5f, Ui.TEXT, false);
		}
		RenderUtils.popAlpha();
		pose.popMatrix();
		return over;
	}

	private void disc(GuiGraphicsExtractor g, float cx, float cy, float d, float glyph) {
		RenderUtils.shadow(g, cx - d * 0.5f, cy - d * 0.5f + 1.5f, d, d, d * 0.5f, 4f, ColorUtil.fade(color, 0.35f));
		RenderUtils.gradient(g, cx - d * 0.5f, cy - d * 0.5f, d, d, d * 0.5f,
				ColorUtil.lerpRgb(color, 0xFFFFFFFF, 0.14f), ColorUtil.lerpRgb(color, 0xFF000000, 0.1f));
		IconFont.draw(g, icon, cx, cy, glyph, 0xFFFFFFFF, 0);
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

	/** Exposed for tooltips keyed by tile. */
	public @Nullable Object tooltipOwner() {
		return tooltip.isEmpty() ? null : this;
	}
}
