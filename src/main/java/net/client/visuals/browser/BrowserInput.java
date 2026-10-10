package net.client.visuals.browser;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;

import java.util.function.Consumer;

/**
 * Public input facade for GUI code (the engine's API is package-private so only this package and
 * the screen talk to the browser process).
 */
public final class BrowserInput {
	private BrowserInput() {
	}

	public static int width(BrowserEngine engine) {
		return engine.width();
	}

	public static int height(BrowserEngine engine) {
		return engine.height();
	}

	public static void mouse(BrowserEngine engine, String type, double x, double y, String button, int clickCount, int buttons) {
		engine.mouse(type, x, y, button, clickCount, buttons, 0);
	}

	/** Wheel at a page position given as u/v (0..1). */
	public static void wheel(BrowserEngine engine, double u, double v, double deltaX, double deltaY) {
		engine.wheel(u * engine.width(), v * engine.height(), deltaX, deltaY);
	}

	/** Physical key down; Enter additionally types a carriage return so forms submit. */
	public static void keyDown(BrowserEngine engine, KeyEvent event) {
		BrowserKeys.Key key = BrowserKeys.of(event);
		if (key == null) {
			return;
		}
		engine.key("rawKeyDown", key, BrowserKeys.modifiers(event));
		int scancode = event.input();
		if (scancode == InputConstants.KEY_RETURN || scancode == 88) {
			engine.text("\r");
		}
	}

	public static void keyUp(BrowserEngine engine, KeyEvent event) {
		BrowserKeys.Key key = BrowserKeys.of(event);
		if (key != null) {
			engine.key("keyUp", key, BrowserKeys.modifiers(event));
		}
	}

	public static void text(BrowserEngine engine, String text) {
		engine.text(text);
	}

	public static void insertText(BrowserEngine engine, String text) {
		if (!text.isEmpty()) {
			engine.insertText(text);
		}
	}

	public static void copySelection(BrowserEngine engine, Consumer<String> sink) {
		engine.selection().thenAccept(text -> {
			if (!text.isEmpty()) {
				sink.accept(text);
			}
		}).exceptionally(error -> null);
	}

	public static void navigate(BrowserEngine engine, String url) {
		engine.navigate(url);
	}

	public static void back(BrowserEngine engine) {
		engine.back();
	}

	public static void forward(BrowserEngine engine) {
		engine.forward();
	}

	public static void reload(BrowserEngine engine) {
		engine.reload();
	}
}
