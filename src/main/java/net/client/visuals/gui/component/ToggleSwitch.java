package net.client.visuals.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.Palette;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * iOS-style switch. The knob glides between ends, the track cross-fades to the accent colour and
 * the knob briefly stretches when pressed – the little "squish" that makes Apple's switches feel
 * physical.
 */
public final class ToggleSwitch extends UiComponent {
	public static final float WIDTH = 24f;
	public static final float HEIGHT = 13f;

	private final BoolBinding binding;
	private final Smooth knob;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth squish = new Smooth(0f, Motion.PRESS);
	private int accent = Palette.ACCENT_BLUE;

	public ToggleSwitch(BoolBinding binding) {
		this.binding = binding;
		this.knob = new Smooth(binding.get() ? 1f : 0f, Motion.TOGGLE);
		this.width = WIDTH;
		this.height = HEIGHT;
	}

	public ToggleSwitch accent(int accent) {
		this.accent = accent;
		return this;
	}

	public boolean value() {
		return binding.get();
	}

	public void toggle() {
		boolean next = !binding.get();
		binding.set(next);
		knob.target(next);
		squish.snap(1f);
		squish.target(0f);
		UiSounds.toggle(next);
	}

	@Override
	public float preferredHeight(float width) {
		return HEIGHT;
	}

	/** @param hostHover hover state of the card hosting this switch (switch brightens with it) */
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY, float hostHover) {
		float dt = UiFrame.dt;
		knob.speed(Motion.speed(Motion.TOGGLE));
		knob.target(binding.get());
		float t = knob.update(dt);
		float hv = hover.target(contains(mouseX, mouseY) ? 1f : hostHover * 0.5f).update(dt);
		float sq = squish.update(dt);

		float r = height * 0.5f;
		// Track
		GlassRenderer.accentGlow(g, x, y, width, height, r, accent, t * 0.6f);
		int offTop = ColorUtil.lerp(Palette.TOGGLE_TRACK_OFF, 0x47FFFFFF, hv);
		int onTop = ColorUtil.lerp(accent, 0xFF3D9BFF, hv * 0.5f);
		RenderUtils.gradient(g, x, y, width, height, r,
				ColorUtil.lerp(offTop, onTop, t),
				ColorUtil.lerp(ColorUtil.fade(offTop, 0.8f), ColorUtil.lerp(accent, 0xFF0060DF, 0.4f), t));
		RenderUtils.stroke(g, x, y, width, height, r, GlassRenderer.hairline(), 0x33FFFFFF, 0x14FFFFFF);

		// Knob
		float inset = 1.5f;
		float d = height - inset * 2f;
		float stretch = d * 0.35f * sq;
		float travel = width - inset * 2f - d;
		float kx = x + inset + travel * t - stretch * t;
		float ky = y + inset;
		RenderUtils.shadow(g, kx, ky + 0.75f, d + stretch, d, d * 0.5f, 1.75f, 0x59000000);
		RenderUtils.gradient(g, kx, ky, d + stretch, d, d * 0.5f, Palette.TOGGLE_KNOB, 0xFFE2E8F0);
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		extract(g, mouseX, mouseY, 0f);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT && contains(mouseX, mouseY)) {
			toggle();
			return true;
		}
		return false;
	}
}
