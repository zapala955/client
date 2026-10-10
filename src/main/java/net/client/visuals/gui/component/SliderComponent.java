package net.client.visuals.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
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
 * Glass slider bound to a {@link NumberSetting}.
 *
 * <pre>
 *  Rotation Speed                 0.50×
 *  ━━━━━━━━━━━━●─────────────────────
 * </pre>
 * The knob position animates towards the value (so clicks glide instead of jumping), grows while
 * dragged and picks up an accent ring. Hue settings get a rainbow track.
 */
public final class SliderComponent extends UiComponent {
	private static final float HEIGHT = 24f;
	private static final float TRACK_Y = 16f;
	private static final float TRACK_H = 4f;
	private static final int[] HUE_STOPS = {0xFFFF3B30, 0xFFFFCC00, 0xFF34C759, 0xFF32D2FF, 0xFF007AFF, 0xFFAF52DE, 0xFFFF3B30};

	private final NumberSetting setting;
	private final TextLabel label;
	private final TextLabel value;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth active = new Smooth(0f, Motion.PRESS);
	private final Smooth knob;
	private final int accent;
	private String shownValue;
	private boolean dragging;

	public SliderComponent(NumberSetting setting, int accent) {
		this.setting = setting;
		this.accent = accent;
		this.label = new TextLabel(I18n.get(setting.translationKey()), LiquidText.TextStyle.BODY);
		this.shownValue = setting.display();
		this.value = new TextLabel(shownValue, LiquidText.TextStyle.LABEL);
		this.knob = new Smooth((float) setting.normalized(), 22f);
	}

	@Override
	public float preferredHeight(float width) {
		return HEIGHT;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		hover.speed(Motion.speed(Motion.HOVER));
		active.speed(Motion.speed(Motion.PRESS));
		knob.speed(Motion.speed(22f));
		float h = hover.target(contains(mouseX, mouseY) || dragging).update(dt);
		float a = active.target(dragging).update(dt);
		float t = knob.target((float) setting.normalized()).update(dt);

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
		if (setting.trackStyle() == NumberSetting.Track.HUE) {
			extractHueTrack(g, ty);
		} else {
			RenderUtils.fill(g, x, ty, width, TRACK_H, r, ColorUtil.lerp(0x26FFFFFF, 0x33FFFFFF, h));
			float fillW = Math.max(TRACK_H, knobX - x);
			GlassRenderer.accentGlow(g, x, ty, fillW, TRACK_H, r, accent, 0.25f + 0.45f * a);
			RenderUtils.gradientH(g, x, ty, fillW, TRACK_H, r, ColorUtil.lerpRgb(accent, 0xFF0A2A66, 0.25f), accent);
		}
		RenderUtils.hairline(g, x, ty, width, TRACK_H, r, 0x1FFFFFFF, 0x0DFFFFFF);

		// Knob
		float kr = 4.5f + 1.25f * a + 0.5f * h;
		float cy = ty + r;
		RenderUtils.shadow(g, knobX - kr, cy - kr + 0.75f, kr * 2f, kr * 2f, kr, 2f, 0x66000000);
		RenderUtils.gradient(g, knobX - kr, cy - kr, kr * 2f, kr * 2f, kr, Palette.TOGGLE_KNOB, 0xFFE2E8F0);
		if (a > 0.01f) {
			RenderUtils.ring(g, knobX, cy, kr + 1.5f, HighDpiEngine.px() * 2f, ColorUtil.fade(accent, a));
		}
	}

	/** Rainbow track: one horizontal gradient per hue segment; the end segments carry the rounding. */
	private void extractHueTrack(GuiGraphicsExtractor g, float ty) {
		int segments = HUE_STOPS.length - 1;
		float seg = width / segments;
		float r = TRACK_H * 0.5f;
		for (int i = 0; i < segments; i++) {
			float sx = x + seg * i;
			float sw = seg;
			float radius = 0f;
			if (i == 0) {
				radius = r;
				sw += r; // overlap hidden under the next segment
			} else if (i == segments - 1) {
				radius = r;
				sx -= r;
				sw += r;
			}
			RenderUtils.gradientH(g, sx, ty, sw, TRACK_H, radius, HUE_STOPS[i], HUE_STOPS[i + 1]);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY) || mouseY < y + TRACK_Y - 8f) {
			return false;
		}
		dragging = true;
		apply(mouseX);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging) {
			return false;
		}
		apply(mouseX);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) {
			return false;
		}
		dragging = false;
		return true;
	}

	private void apply(double mouseX) {
		double before = setting.get();
		setting.setNormalized((mouseX - x) / Math.max(1f, width));
		if (setting.get() != before) {
			UiSounds.tick(setting.normalized()); // a detent per step
		}
	}
}
