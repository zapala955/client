package net.client.visuals.gui.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * Base of the ClickGUI component tree (module cards, toggles – and later sliders, dropdowns,
 * colour pickers).
 *
 * <p>Deliberately lighter than vanilla's widget system: float layout (so components can sit at
 * animated, sub-pixel positions), explicit bounds assigned by the parent each frame, and plain
 * mouse callbacks. Timing comes from {@code UiFrame.dt}.</p>
 *
 * <p>Mouse buttons are compared against {@code InputConstants.MOUSE_BUTTON_*}; SDL (26.3) changed
 * the raw values, so never hard-code 0/1.</p>
 */
public abstract class UiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;

	public void setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	/** Height this component wants at the given width (may be animated). */
	public abstract float preferredHeight(float width);

	public abstract void extract(GuiGraphicsExtractor g, int mouseX, int mouseY);

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return false;
	}

	/** Keyboard input while this component has focus (text fields). */
	public boolean keyPressed(KeyEvent event) {
		return false;
	}

	public boolean charTyped(CharacterEvent event) {
		return false;
	}

	/** Whether clicking this component should give it keyboard focus. */
	public boolean focusable() {
		return false;
	}

	public void setFocused(boolean focused) {
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float width() {
		return width;
	}

	public float height() {
		return height;
	}
}
