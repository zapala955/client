package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.IntConsumer;

/** macOS segmented control: equal segments, a raised thumb that slides to the selection. */
public final class Segmented extends UiComponent {
	private final TextLabel[] labels;
	private final IntConsumer onSelect;
	private final Smooth thumb = new Smooth(0f, 20f);
	private int selected;

	public Segmented(String[] options, int selected, IntConsumer onSelect) {
		this.labels = new TextLabel[options.length];
		for (int i = 0; i < options.length; i++) {
			labels[i] = new TextLabel(options[i], LiquidText.TextStyle.LABEL);
		}
		this.selected = selected;
		this.onSelect = onSelect;
		this.thumb.snap(selected);
		float w = 0f;
		for (TextLabel label : labels) {
			w = Math.max(w, label.width());
		}
		this.width = (w + 20f) * options.length + 4f;
		this.height = Ui.CONTROL_H;
	}

	public int selected() {
		return selected;
	}

	@Override
	public float preferredHeight(float width) {
		return Ui.CONTROL_H;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float r = height * 0.5f;
		RenderUtils.fill(g, x, y, width, height, r, Ui.FILL_CONTROL);
		float seg = (width - 4f) / labels.length;
		float t = thumb.target(selected).update(UiFrame.dt);
		float tx = x + 2f + seg * t;
		RenderUtils.shadow(g, tx, y + 2.5f, seg, height - 4f, r - 2f, 3f, 0x59000000);
		RenderUtils.fill(g, tx, y + 2f, seg, height - 4f, r - 2f, 0xFF636366);
		RenderUtils.stroke(g, tx, y + 2f, seg, height - 4f, r - 2f, HighDpiEngine.px(), 0x33FFFFFF, 0x0AFFFFFF);
		for (int i = 0; i < labels.length; i++) {
			TextLabel label = labels[i];
			float closeness = Math.max(0f, 1f - Math.abs(t - i));
			int color = ColorUtil.lerp(Ui.TEXT_2, Ui.TEXT, closeness);
			LiquidText.draw(g, label, x + 2f + seg * i + (seg - label.width()) * 0.5f, y + (height - label.capHeight()) * 0.5f, color, false);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
			return false;
		}
		float seg = (width - 4f) / labels.length;
		int index = Math.clamp((int) ((mouseX - x - 2f) / seg), 0, labels.length - 1);
		if (index != selected) {
			selected = index;
			UiSounds.click();
			onSelect.accept(index);
		}
		return true;
	}
}
