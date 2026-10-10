package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A modal sheet over a window (macOS alert / form sheet): optional icon tile, bold title, wrapped
 * message, labelled fields and a right-aligned button row. Enter triggers the default button, Esc
 * the cancel action. The window behind is dimmed and doesn't take input while a sheet is up.
 */
public final class Sheet {
	private static final float W = 290f;
	private static final float PAD = 18f;

	private final WindowScreen host;
	private final TextLabel title;
	private final List<TextLabel> message = new ArrayList<>();
	private final List<TextLabel> fieldLabels = new ArrayList<>();
	private final List<UiComponent> fields = new ArrayList<>();
	private final List<PillButton> buttons = new ArrayList<>();
	private final Smooth open = new Smooth(0f, 14f);
	private int iconCodepoint;
	private int iconColor;
	private @Nullable Runnable onCancel;
	private @Nullable Runnable onDefault;
	private boolean closing;
	private float x;
	private float y;
	private float h;

	public Sheet(WindowScreen host, String title) {
		this.host = host;
		this.title = new TextLabel(title, LiquidText.TextStyle.HEADLINE).maxWidth(W - PAD * 2f);
	}

	public Sheet icon(int codepoint, int color) {
		this.iconCodepoint = codepoint;
		this.iconColor = color;
		return this;
	}

	public Sheet message(String text) {
		message.addAll(wrap(text, W - PAD * 2f));
		return this;
	}

	public Sheet field(String label, UiComponent field) {
		fieldLabels.add(new TextLabel(label, LiquidText.TextStyle.CAPTION).uppercase().tracking(0.4f));
		fields.add(field);
		return this;
	}

	/** Adds a button; the last one added is the default (Enter). Buttons close the sheet. */
	public Sheet button(String text, PillButton.Kind kind, Runnable action) {
		Runnable wrapped = () -> {
			close();
			action.run();
		};
		buttons.add(new PillButton(text, 0, kind, wrapped));
		onDefault = wrapped;
		return this;
	}

	/** Like {@link #button} but enabled only while {@code condition} holds. */
	public Sheet button(String text, PillButton.Kind kind, java.util.function.BooleanSupplier condition, Runnable action) {
		button(text, kind, action);
		PillButton last = buttons.getLast();
		last.enabledWhen(condition);
		Runnable fire = onDefault;
		onDefault = () -> {
			if (condition.getAsBoolean() && fire != null) {
				fire.run();
			}
		};
		return this;
	}

	public Sheet onCancel(Runnable action) {
		this.onCancel = action;
		return this;
	}

	public void close() {
		closing = true;
		host.focus(null);
	}

	public boolean finished() {
		return closing && open.get() < 0.02f;
	}

	public boolean closing() {
		return closing;
	}

	public @Nullable UiComponent firstFocusable() {
		for (UiComponent field : fields) {
			if (field.focusable()) {
				return field;
			}
		}
		return null;
	}

	private void layout() {
		float contentH = PAD;
		if (iconCodepoint != 0) {
			contentH += 34f + 10f;
		}
		contentH += title.capHeight() + 8f;
		contentH += message.size() * 11f + (message.isEmpty() ? 0f : 6f);
		for (int i = 0; i < fields.size(); i++) {
			contentH += 12f + fields.get(i).preferredHeight(W - PAD * 2f) + 8f;
		}
		contentH += 8f + Ui.CONTROL_H + PAD;
		h = contentH;
		x = Math.round((host.width - W) * 0.5f);
		y = Math.round((host.height - h) * 0.5f);
	}

	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		layout();
		float a = open.target(closing ? 0f : 1f).update(UiFrame.dt);
		float s = 0.94f + 0.06f * a;
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(x + W * 0.5f, y + h * 0.5f);
		pose.scale(s, s);
		pose.translate(-(x + W * 0.5f), -(y + h * 0.5f));
		RenderUtils.pushAlpha(a);
		RenderUtils.shadow(g, x, y + 10f, W, h, 16f, 24f, 0x99000000);
		RenderUtils.fill(g, x, y, W, h, 16f, 0xF524252A);
		RenderUtils.light(g, x, y, W, 50f, 16f, 0x0DFFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, W, h, 16f, HighDpiEngine.px(), 0x40FFFFFF, 0x14FFFFFF);
		float cy = y + PAD;
		if (iconCodepoint != 0) {
			Ui.tile(g, x + (W - 34f) * 0.5f, cy, 34f, iconColor);
			IconFont.draw(g, iconCodepoint, x + W * 0.5f, cy + 17f, 16f, 0xFFFFFFFF, 0);
			cy += 34f + 10f;
		}
		LiquidText.drawCentered(g, title, x + W * 0.5f, cy, Ui.TEXT, false);
		cy += title.capHeight() + 8f;
		for (TextLabel line : message) {
			LiquidText.drawCentered(g, line, x + W * 0.5f, cy, Ui.TEXT_2, false);
			cy += 11f;
		}
		if (!message.isEmpty()) {
			cy += 6f;
		}
		for (int i = 0; i < fields.size(); i++) {
			LiquidText.draw(g, fieldLabels.get(i), x + PAD, cy, Ui.TEXT_3, false);
			cy += 12f;
			UiComponent field = fields.get(i);
			float fh = field.preferredHeight(W - PAD * 2f);
			field.setBounds(x + PAD, cy, W - PAD * 2f, fh);
			field.extract(g, mouseX, mouseY);
			cy += fh + 8f;
		}
		cy += 8f;
		float bx = x + W - PAD;
		for (int i = buttons.size() - 1; i >= 0; i--) {
			PillButton button = buttons.get(i);
			float bw = Math.max(70f, button.naturalWidth());
			bx -= bw;
			button.setBounds(bx, cy, bw, Ui.CONTROL_H);
			button.extract(g, mouseX, mouseY);
			bx -= 8f;
		}
		RenderUtils.popAlpha();
		pose.popMatrix();
	}

	public boolean mouseClicked(double mx, double my, int button) {
		if (closing) {
			return true;
		}
		for (UiComponent field : fields) {
			if (field.mouseClicked(mx, my, button)) {
				host.focus(field.focusable() ? field : null);
				return true;
			}
		}
		for (PillButton b : buttons) {
			if (b.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			host.focus(null);
		}
		return true; // modal
	}

	public boolean mouseReleased(double mx, double my, int button) {
		for (PillButton b : buttons) {
			if (b.mouseReleased(mx, my, button)) {
				return true;
			}
		}
		for (UiComponent field : fields) {
			field.mouseReleased(mx, my, button);
		}
		return true;
	}

	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		for (UiComponent field : fields) {
			if (field.mouseDragged(mx, my, button, dx, dy)) {
				return true;
			}
		}
		return true;
	}

	public boolean keyPressed(KeyEvent event, @Nullable UiComponent focus) {
		if (event.isEscape()) {
			close();
			if (onCancel != null) {
				onCancel.run();
			}
			return true;
		}
		if ((event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) && onDefault != null) {
			onDefault.run();
			return true;
		}
		if (event.key() == InputConstants.KEY_TAB) {
			cycleFocus(focus);
			return true;
		}
		return focus != null && focus.keyPressed(event);
	}

	private void cycleFocus(@Nullable UiComponent focus) {
		List<UiComponent> focusable = new ArrayList<>();
		for (UiComponent field : fields) {
			if (field.focusable()) {
				focusable.add(field);
			}
		}
		if (focusable.isEmpty()) {
			return;
		}
		int index = focusable.indexOf(focus);
		host.focus(focusable.get((index + 1) % focusable.size()));
	}

	public boolean charTyped(CharacterEvent event, @Nullable UiComponent focus) {
		return focus != null && focus.charTyped(event);
	}

	static List<TextLabel> wrap(String text, float maxWidth) {
		return Ui.wrap(text, maxWidth, LiquidText.TextStyle.BODY);
	}
}
