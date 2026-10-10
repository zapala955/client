package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * macOS slider: a thin capsule track filled with the accent up to a white knob. The whole bounds
 * are the hit area; dragging is continuous (the owner snaps to steps), the knob glides to values
 * set from elsewhere and swells slightly while held.
 */
public final class Slider extends UiComponent {
	private static final float TRACK_H = 4f;
	private static final float KNOB = 13f;

	private final DoubleSupplier value;
	private final DoubleConsumer onDrag;
	private final Runnable onRelease;
	private final Smooth shown = new Smooth(0f, 22f);
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth held = new Smooth(0f, Motion.PRESS);
	private boolean dragging;
	private boolean enabled = true;
	private int ticks;

	/**
	 * @param value     current position 0..1
	 * @param onDrag    new position while dragging (0..1)
	 * @param onRelease drag finished (apply deferred values)
	 */
	public Slider(DoubleSupplier value, DoubleConsumer onDrag, Runnable onRelease) {
		this.value = value;
		this.onDrag = onDrag;
		this.onRelease = onRelease;
		this.shown.snap((float) value.getAsDouble());
		this.height = KNOB + 4f;
	}

	/** Small tick marks under the track for discrete ranges with few steps (0 = none). */
	public Slider ticks(int count) {
		this.ticks = count;
		return this;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled) {
			dragging = false;
		}
	}

	public boolean dragging() {
		return dragging;
	}

	@Override
	public float preferredHeight(float width) {
		return KNOB + 4f;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float target = (float) Math.clamp(value.getAsDouble(), 0.0, 1.0);
		if (dragging) {
			shown.snap(target);
		}
		float t = shown.target(target).update(dt);
		float hv = hover.target(enabled && (dragging || contains(mouseX, mouseY))).update(dt);
		float hd = held.target(dragging).update(dt);
		float cy = y + height * 0.5f;
		float left = x + KNOB * 0.5f;
		float span = width - KNOB;
		float kx = left + span * t;

		RenderUtils.fill(g, x, cy - TRACK_H * 0.5f, width, TRACK_H, TRACK_H * 0.5f, ColorUtil.lerp(0x2EFFFFFF, 0x3DFFFFFF, hv));
		if (ticks > 1 && ticks <= 12) {
			for (int i = 0; i < ticks; i++) {
				float tx = left + span * i / (ticks - 1f);
				RenderUtils.rect(g, HighDpiEngine.snap(tx), cy + TRACK_H * 0.5f + 2f, HighDpiEngine.px(), 2.5f, 0x40FFFFFF);
			}
		}
		if (kx > x + 1f) {
			RenderUtils.fill(g, x, cy - TRACK_H * 0.5f, kx - x, TRACK_H, TRACK_H * 0.5f, enabled ? Ui.ACCENT : 0x66FFFFFF);
		}
		float d = KNOB + hd * 1.5f;
		RenderUtils.shadow(g, kx - d * 0.5f, cy - d * 0.5f + 1f, d, d, d * 0.5f, 3f, 0x66000000);
		RenderUtils.circle(g, kx, cy, d * 0.5f, ColorUtil.lerp(0xFFF2F2F7, 0xFFFFFFFF, hv));
		RenderUtils.stroke(g, kx - d * 0.5f, cy - d * 0.5f, d, d, d * 0.5f, HighDpiEngine.px(), 0x14000000, 0x33000000);
	}

	private void dragTo(double mouseX) {
		double t = (mouseX - x - KNOB * 0.5f) / Math.max(1f, width - KNOB);
		onDrag.accept(Math.clamp(t, 0.0, 1.0));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
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
		onRelease.run();
		return true;
	}
}
