package net.client.visuals.gui.window;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Single-line text input drawn with the Liquid type: rounded field, optional leading icon
 * (search), placeholder, blinking caret that scrolls into view, a clear button, and the usual
 * editing keys (word jumps / deletes with Ctrl, Home / End, select-all, copy, cut, paste).
 *
 * <p>Characters arrive through the screen's {@code charTyped} (SDL text input), so any keyboard
 * layout and IMEs work; the owning {@link WindowScreen} turns text input on while it's focused.</p>
 */
public final class TextField extends UiComponent {
	private static final int ICON_SEARCH = 0xE151;
	private static final int ICON_CLEAR = 0xE1B2;

	private final TextLabel text = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel prefix = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel placeholder;
	private final int icon;
	private final boolean round;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth focusRing = new Smooth(0f, Motion.HOVER);
	private String value = "";
	private int cursor;
	private boolean allSelected;
	private boolean focused;
	private long caretEpoch;
	private float scroll;
	private int shownCursor = -1;
	private String shownValue = "";
	private int maxLength = 256;
	private @Nullable Consumer<String> onChange;
	private @Nullable Runnable onSubmit;

	/**
	 * @param search rounded capsule with a magnifier (toolbar search) instead of a form field
	 */
	public TextField(String placeholder, boolean search) {
		this.placeholder = new TextLabel(placeholder, LiquidText.TextStyle.BODY);
		this.icon = search ? ICON_SEARCH : 0;
		this.round = search;
	}

	public TextField onChange(Consumer<String> listener) {
		this.onChange = listener;
		return this;
	}

	public TextField onSubmit(Runnable listener) {
		this.onSubmit = listener;
		return this;
	}

	public TextField maxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	public String value() {
		return value;
	}

	public void setValue(String newValue) {
		String v = newValue.length() > maxLength ? newValue.substring(0, maxLength) : newValue;
		if (!v.equals(value)) {
			value = v;
			cursor = Math.min(cursor, value.length());
			text.setText(value);
			if (onChange != null) {
				onChange.accept(value);
			}
		}
		cursor = Math.clamp(cursor, 0, value.length());
	}

	public boolean focused() {
		return focused;
	}

	@Override
	public boolean focusable() {
		return true;
	}

	@Override
	public void setFocused(boolean focused) {
		if (focused && !this.focused) {
			caretEpoch = System.currentTimeMillis();
			cursor = value.length();
		}
		if (!focused) {
			allSelected = false;
		}
		this.focused = focused;
	}

	@Override
	public float preferredHeight(float width) {
		return Ui.CONTROL_H;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float h = hover.target(contains(mouseX, mouseY)).update(dt);
		float f = focusRing.target(focused).update(dt);
		float r = round ? height * 0.5f : 7f;
		if (f > 0.01f) {
			RenderUtils.outerGlow(g, x, y, width, height, r, 3f, ColorUtil.fade(Ui.ACCENT, 0.55f * f));
		}
		RenderUtils.fill(g, x, y, width, height, r, ColorUtil.lerp(Ui.FILL_CONTROL, Ui.FILL_CONTROL_HOVER, Math.max(h * 0.6f, f * 0.4f)));
		RenderUtils.stroke(g, x, y, width, height, r, HighDpiEngine.px(),
				ColorUtil.lerp(0x1FFFFFFF, ColorUtil.fade(Ui.ACCENT, 0.9f), f), ColorUtil.lerp(0x0AFFFFFF, ColorUtil.fade(Ui.ACCENT, 0.6f), f));

		float left = x + (icon != 0 ? 22f : 8f);
		if (icon != 0) {
			IconFont.draw(g, icon, x + 12f, y + height * 0.5f, 10f, Ui.TEXT_2, 0);
		}
		boolean showClear = round && !value.isEmpty();
		float right = x + width - (showClear ? 20f : 8f);
		float visible = Math.max(4f, right - left);
		float cy = y + (height - text.capHeight()) * 0.5f;

		// Keep the caret inside the visible window (prefix only re-measured when it changes).
		if (cursor != shownCursor || !value.equals(shownValue)) {
			shownCursor = cursor;
			shownValue = value;
			prefix.setText(value.substring(0, cursor));
		}
		float caretX = prefix.width();
		if (caretX - scroll > visible - 2f) {
			scroll = caretX - visible + 2f;
		} else if (caretX - scroll < 0f) {
			scroll = Math.max(0f, caretX - 6f);
		}
		scroll = Math.clamp(scroll, 0f, Math.max(0f, text.width() - visible + 2f));

		g.enableScissor((int) Math.floor(left - 1f), (int) Math.floor(y), (int) Math.ceil(right + 1f), (int) Math.ceil(y + height));
		if (value.isEmpty()) {
			LiquidText.draw(g, placeholder, left, cy, Ui.TEXT_3, false);
		} else {
			if (allSelected && focused) {
				RenderUtils.fill(g, left - scroll - 1f, y + 4f, text.width() + 2f, height - 8f, 2f, 0x660A84FF);
			}
			LiquidText.draw(g, text, left - scroll, cy, Ui.TEXT, false);
		}
		if (focused && ((System.currentTimeMillis() - caretEpoch) / 530L) % 2L == 0L) {
			float cx = HighDpiEngine.snap(left - scroll + caretX);
			RenderUtils.rect(g, cx, y + 5f, Math.max(HighDpiEngine.px(), 0.75f), height - 10f, Ui.ACCENT_HOVER);
		}
		g.disableScissor();

		if (showClear) {
			float bx = x + width - 11f;
			float by = y + height * 0.5f;
			boolean over = Math.abs(mouseX - bx) <= 6f && Math.abs(mouseY - by) <= 6f;
			RenderUtils.circle(g, bx, by, 5.5f, over ? 0x66FFFFFF : 0x40FFFFFF);
			IconFont.draw(g, ICON_CLEAR, bx, by, 7f, 0xFF1C1C1E, 0);
		}
		if (focused) {
			// IME candidate window next to the caret.
			int caretScreenX = Math.round(left - scroll + caretX);
			Minecraft.getInstance().textInputManager().setTextInputArea(caretScreenX, Math.round(y), caretScreenX + 1, Math.round(y + height));
			TextInputs.caret(this, caretScreenX, Math.round(y), 1, Math.round(height));
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!contains(mouseX, mouseY)) {
			return false;
		}
		if (round && !value.isEmpty() && mouseX >= x + width - 18f) {
			setValue("");
			cursor = 0;
			return true;
		}
		// Caret to the nearest character boundary.
		float left = x + (icon != 0 ? 22f : 8f);
		float target = (float) mouseX - left + scroll;
		int best = 0;
		float bestDistance = Float.MAX_VALUE;
		for (int i = 0; i <= value.length(); i++) {
			prefix.setText(value.substring(0, i));
			float distance = Math.abs(prefix.width() - target);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = i;
			} else {
				break; // widths only grow: past the closest boundary
			}
		}
		shownCursor = -1; // the prefix label was used for measuring
		cursor = best;
		allSelected = false;
		caretEpoch = System.currentTimeMillis();
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!focused) {
			return false;
		}
		caretEpoch = System.currentTimeMillis();
		if (event.isSelectAll()) {
			allSelected = !value.isEmpty();
			cursor = value.length();
			return true;
		}
		if (event.isCopy()) {
			if (allSelected) {
				Minecraft.getInstance().keyboardHandler.setClipboard(value);
			}
			return true;
		}
		if (event.isCut()) {
			if (allSelected) {
				Minecraft.getInstance().keyboardHandler.setClipboard(value);
				replaceAll("");
			}
			return true;
		}
		if (event.isPaste()) {
			insert(Minecraft.getInstance().keyboardHandler.getClipboard());
			return true;
		}
		int key = event.key();
		boolean word = event.hasControlDown();
		switch (key) {
			case InputConstants.KEY_BACKSPACE -> {
				if (allSelected) {
					replaceAll("");
				} else if (cursor > 0) {
					int from = word ? wordStart(cursor) : cursor - 1;
					edit(value.substring(0, from) + value.substring(cursor), from);
				}
				return true;
			}
			case InputConstants.KEY_DELETE -> {
				if (allSelected) {
					replaceAll("");
				} else if (cursor < value.length()) {
					int to = word ? wordEnd(cursor) : cursor + 1;
					edit(value.substring(0, cursor) + value.substring(to), cursor);
				}
				return true;
			}
			case InputConstants.KEY_HOME -> {
				move(0);
				return true;
			}
			case InputConstants.KEY_END -> {
				move(value.length());
				return true;
			}
			case InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
				if (onSubmit != null) {
					onSubmit.run();
				}
				return true;
			}
			default -> {
			}
		}
		if (event.isLeft()) {
			move(word ? wordStart(cursor) : Math.max(0, cursor - 1));
			return true;
		}
		if (event.isRight()) {
			move(word ? wordEnd(cursor) : Math.min(value.length(), cursor + 1));
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!focused || !StringUtil.isAllowedChatCharacter(event.codepoint())) {
			return false;
		}
		insert(event.codepointAsString());
		return true;
	}

	private void insert(String raw) {
		String clean = StringUtil.filterText(raw.replace('\n', ' ').replace('\r', ' '));
		if (clean.isEmpty()) {
			return;
		}
		if (allSelected) {
			replaceAll(clean);
			return;
		}
		int room = maxLength - value.length();
		if (room <= 0) {
			return;
		}
		if (clean.length() > room) {
			clean = clean.substring(0, room);
		}
		edit(value.substring(0, cursor) + clean + value.substring(cursor), cursor + clean.length());
	}

	private void replaceAll(String with) {
		allSelected = false;
		edit(with, with.length());
	}

	private void edit(String newValue, int newCursor) {
		allSelected = false;
		cursor = Math.clamp(newCursor, 0, newValue.length());
		setValue(newValue);
		cursor = Math.clamp(newCursor, 0, value.length());
	}

	private void move(int position) {
		allSelected = false;
		cursor = Math.clamp(position, 0, value.length());
	}

	private int wordStart(int from) {
		int i = from;
		while (i > 0 && value.charAt(i - 1) == ' ') {
			i--;
		}
		while (i > 0 && value.charAt(i - 1) != ' ') {
			i--;
		}
		return i;
	}

	private int wordEnd(int from) {
		int i = from;
		while (i < value.length() && value.charAt(i) == ' ') {
			i++;
		}
		while (i < value.length() && value.charAt(i) != ' ') {
			i++;
		}
		return i;
	}
}
