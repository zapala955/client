package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** A pop-up button: shows the current value; clicking opens a {@link Popover} with the choices. */
public final class PopupButton extends UiComponent {
	private final WindowScreen host;
	private final Supplier<List<String>> items;
	private final IntSupplier current;
	private final IntConsumer onPick;
	private final TextLabel value = new TextLabel("", LiquidText.TextStyle.BODY);
	private final Smooth hover = new Smooth(0f, Motion.HOVER);

	public PopupButton(WindowScreen host, Supplier<List<String>> items, IntSupplier current, IntConsumer onPick) {
		this.host = host;
		this.items = items;
		this.current = current;
		this.onPick = onPick;
		this.height = Ui.CONTROL_H - 2f;
	}

	@Override
	public float preferredHeight(float width) {
		return Ui.CONTROL_H - 2f;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		List<String> list = items.get();
		int index = current.getAsInt();
		value.setText(index >= 0 && index < list.size() ? list.get(index) : "—");
		float h = hover.target(contains(mouseX, mouseY)).update(UiFrame.dt);
		Popover.button(g, value, x, y, width, height, h);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
			return false;
		}
		host.openPopover(new Popover(items.get(), current.getAsInt(), onPick, x, y, width, height, host.width, host.height));
		return true;
	}
}
