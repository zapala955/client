package net.client.visuals.gui.component;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Label + switch row for boolean settings inside a card drawer. */
public final class ToggleRow extends UiComponent {
	private static final float HEIGHT = 18f;

	private final TextLabel label;
	private final ToggleSwitch toggle;

	public ToggleRow(String label, BoolBinding binding, int accent) {
		this.label = new TextLabel(label, LiquidText.TextStyle.BODY);
		this.toggle = new ToggleSwitch(binding).accent(accent);
	}

	@Override
	public float preferredHeight(float width) {
		return HEIGHT;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		label.maxWidth(width - ToggleSwitch.WIDTH - 8f);
		boolean over = contains(mouseX, mouseY);
		LiquidText.draw(g, label, x, y + (HEIGHT - label.capHeight()) * 0.5f,
				over ? ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, 0.6f) : Palette.TEXT_SECONDARY, false);
		toggle.setBounds(x + width - ToggleSwitch.WIDTH, y + (HEIGHT - ToggleSwitch.HEIGHT) * 0.5f, ToggleSwitch.WIDTH, ToggleSwitch.HEIGHT);
		toggle.extract(g, mouseX, mouseY, over ? 1f : 0f);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (contains(mouseX, mouseY)) {
			toggle.toggle();
			return true;
		}
		return false;
	}
}
