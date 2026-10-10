package net.client.visuals.gui.photo;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.module.setting.NumberSetting;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;

/**
 * The ClickGUI slider on a logarithmic track, for camera values that work in ratios: focal length
 * (24 → 50 mm is as far as 50 → 100), focus distance, aperture (one stop is the same distance
 * anywhere on the track). Optional marks under the track for familiar values (lens presets, stops).
 */
final class LogSliderComponent extends UiComponent {
	private static final float HEIGHT = 28f;
	private static final float TRACK_Y = 16f;
	private static final float TRACK_H = 4f;

	private final NumberSetting setting;
	private final TextLabel label;
	private final TextLabel value;
	private final double[] marks;
	private final int accent;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth active = new Smooth(0f, Motion.PRESS);
	private final Smooth knob;
	private String shownValue;
	private boolean dragging;
	private int lastMark = -1;

	LogSliderComponent(NumberSetting setting, int accent, double... marks) {
		this.setting = setting;
		this.accent = accent;
		this.marks = marks;
		this.label = new TextLabel(I18n.get(setting.translationKey()), LiquidText.TextStyle.BODY);
		this.shownValue = setting.display();
		this.value = new TextLabel(shownValue, LiquidText.TextStyle.LABEL);
		this.knob = new Smooth(position(setting.get()), 22f);
	}

	private float position(double v) {
		double lo = Math.log(setting.min());
		double hi = Math.log(setting.max());
		return (float) Math.clamp((Math.log(Math.max(v, setting.min())) - lo) / (hi - lo), 0.0, 1.0);
	}

	private double valueAt(double t) {
		double lo = Math.log(setting.min());
		double hi = Math.log(setting.max());
		return Math.exp(lo + (hi - lo) * Math.clamp(t, 0.0, 1.0));
	}

	@Override
	public float preferredHeight(float width) {
		return HEIGHT;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float h = hover.target(contains(mouseX, mouseY) || dragging).update(dt);
		float a = active.target(dragging).update(dt);
		float t = knob.target(position(setting.get())).update(dt);

		String display = setting.display();
		if (display != shownValue) {
			shownValue = display;
			value.setText(display);
		}
		label.maxWidth(width - value.width() - 8f);
		LiquidText.draw(g, label, x, y + 2f, ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, h * 0.6f), false);
		LiquidText.draw(g, value, x + width - value.width(), y + 2f, Palette.TEXT_PRIMARY, false);

		float ty = HighDpiEngine.snap(y + TRACK_Y);
		float r = TRACK_H * 0.5f;
		float knobX = x + width * t;
		RenderUtils.fill(g, x, ty, width, TRACK_H, r, ColorUtil.lerp(0x26FFFFFF, 0x33FFFFFF, h));
		float fillW = Math.max(TRACK_H, knobX - x);
		GlassRenderer.accentGlow(g, x, ty, fillW, TRACK_H, r, accent, 0.25f + 0.45f * a);
		RenderUtils.gradientH(g, x, ty, fillW, TRACK_H, r, ColorUtil.lerpRgb(accent, 0xFF3A2A00, 0.35f), accent);
		RenderUtils.hairline(g, x, ty, width, TRACK_H, r, 0x1FFFFFFF, 0x0DFFFFFF);
		for (double mark : marks) {
			float mx = HighDpiEngine.snap(x + width * position(mark));
			RenderUtils.rect(g, mx, ty + TRACK_H + 2f, HighDpiEngine.px(), 3f, 0x59FFFFFF);
		}

		float kr = 4.5f + 1.25f * a + 0.5f * h;
		float cy = ty + r;
		RenderUtils.shadow(g, knobX - kr, cy - kr + 0.75f, kr * 2f, kr * 2f, kr, 2f, 0x66000000);
		RenderUtils.gradient(g, knobX - kr, cy - kr, kr * 2f, kr * 2f, kr, Palette.TOGGLE_KNOB, 0xFFE2E8F0);
		if (a > 0.01f) {
			RenderUtils.ring(g, knobX, cy, kr + 1.5f, HighDpiEngine.px() * 2f, ColorUtil.fade(accent, a));
		}
	}

	private void dragTo(double mouseX) {
		double t = (mouseX - x) / Math.max(1f, width);
		double v = valueAt(t);
		// Snap onto a mark when close (lens presets, full stops), like a detent.
		int nearest = -1;
		for (int i = 0; i < marks.length; i++) {
			if (Math.abs(position(marks[i]) - t) * width < 3.0) {
				nearest = i;
				v = marks[i];
			}
		}
		if (nearest >= 0 && nearest != lastMark) {
			UiSounds.tick(position(v));
		}
		lastMark = nearest;
		setting.set(v);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
			return false;
		}
		dragging = true;
		dragTo(mouseX);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging) {
			return false;
		}
		dragTo(mouseX);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) {
			return false;
		}
		dragging = false;
		lastMark = -1;
		return true;
	}
}
