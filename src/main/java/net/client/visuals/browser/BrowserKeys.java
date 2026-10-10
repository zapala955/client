package net.client.visuals.browser;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;

/**
 * Maps Minecraft (SDL scancode) key events to DOM key events for {@code Input.dispatchKeyEvent}.
 *
 * <p>Model (same as Puppeteer): every physical key sends {@code rawKeyDown}/{@code keyUp} with
 * {@code key}, {@code code} and {@code windowsVirtualKeyCode} – that drives shortcuts, games and
 * editing keys (Backspace, arrows...). Printable text is sent separately as {@code char} events from
 * {@code charTyped}, so typing works with any keyboard layout and IME.</p>
 */
final class BrowserKeys {
	/** DOM description of one physical key. */
	record Key(String key, String code, int virtualKey) {
	}

	private static final Key[] BY_SCANCODE = new Key[256];

	static {
		for (int i = 0; i < 26; i++) {
			char c = (char) ('a' + i);
			put(InputConstants.KEY_A + i, String.valueOf(c), "Key" + Character.toUpperCase(c), 'A' + i);
		}
		// SDL: 1..9 = 30..38, 0 = 39
		for (int i = 1; i <= 9; i++) {
			put(29 + i, String.valueOf(i), "Digit" + i, '0' + i);
		}
		put(39, "0", "Digit0", '0');
		put(InputConstants.KEY_RETURN, "Enter", "Enter", 13);
		put(88, "Enter", "NumpadEnter", 13);
		put(InputConstants.KEY_BACKSPACE, "Backspace", "Backspace", 8);
		put(InputConstants.KEY_TAB, "Tab", "Tab", 9);
		put(InputConstants.KEY_SPACE, " ", "Space", 32);
		put(InputConstants.KEY_LEFT, "ArrowLeft", "ArrowLeft", 37);
		put(InputConstants.KEY_UP, "ArrowUp", "ArrowUp", 38);
		put(InputConstants.KEY_RIGHT, "ArrowRight", "ArrowRight", 39);
		put(InputConstants.KEY_DOWN, "ArrowDown", "ArrowDown", 40);
		put(InputConstants.KEY_DELETE, "Delete", "Delete", 46);
		put(InputConstants.KEY_HOME, "Home", "Home", 36);
		put(InputConstants.KEY_END, "End", "End", 35);
		put(InputConstants.KEY_PAGEUP, "PageUp", "PageUp", 33);
		put(InputConstants.KEY_PAGEDOWN, "PageDown", "PageDown", 34);
		put(InputConstants.KEY_LSHIFT, "Shift", "ShiftLeft", 16);
		put(InputConstants.KEY_RSHIFT, "Shift", "ShiftRight", 16);
		put(InputConstants.KEY_LCONTROL, "Control", "ControlLeft", 17);
		put(228, "Control", "ControlRight", 17);
		put(226, "Alt", "AltLeft", 18);
		put(230, "Alt", "AltRight", 18);
		// Punctuation (US positions; the actual characters arrive through charTyped)
		put(45, "-", "Minus", 189);
		put(46, "=", "Equal", 187);
		put(47, "[", "BracketLeft", 219);
		put(48, "]", "BracketRight", 221);
		put(49, "\\", "Backslash", 220);
		put(51, ";", "Semicolon", 186);
		put(52, "'", "Quote", 222);
		put(53, "`", "Backquote", 192);
		put(54, ",", "Comma", 188);
		put(55, ".", "Period", 190);
		put(56, "/", "Slash", 191);
		// F1..F12 = 58..69
		for (int i = 1; i <= 12; i++) {
			put(57 + i, "F" + i, "F" + i, 111 + i);
		}
	}

	private BrowserKeys() {
	}

	private static void put(int scancode, String key, String code, int virtualKey) {
		if (scancode >= 0 && scancode < BY_SCANCODE.length) {
			BY_SCANCODE[scancode] = new Key(key, code, virtualKey);
		}
	}

	static @Nullable Key of(KeyEvent event) {
		int scancode = event.input();
		return scancode >= 0 && scancode < BY_SCANCODE.length ? BY_SCANCODE[scancode] : null;
	}

	/** CDP modifier bitmask: Alt 1, Ctrl 2, Meta 4, Shift 8. */
	static int modifiers(KeyEvent event) {
		int mods = 0;
		if (event.hasAltDown()) {
			mods |= 1;
		}
		if (event.hasControlDown()) {
			mods |= 2;
		}
		if (event.hasShiftDown()) {
			mods |= 8;
		}
		return mods;
	}
}
