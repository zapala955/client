package net.client.visuals.gui.window;

import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Turns text input (SDL's character events, and the IME) on while one of our text fields has focus.
 *
 * <p>Normally that's vanilla's {@code Minecraft#onTextInputFocusChange}. With <b>IMBlocker</b>
 * installed, vanilla's switch is cancelled and IMBlocker enables text input only for widgets it
 * knows (vanilla edit boxes and a list of UI libraries) – so our fields would never receive a
 * character. IMBlocker's widget API is plain interfaces ({@code FocusableWidget}), so each focused
 * field is handed to it as a {@link Proxy} implementing that interface, found by reflection: no
 * compile-time dependency, and nothing happens without IMBlocker.</p>
 */
public final class TextInputs {
	private static final String IMBLOCKER = "io.github.reserveword.imblocker.common.gui.";

	/** Where an owner's caret is (GUI units), for IMBlocker's candidate window. */
	private static final class Caret {
		int x;
		int y;
		int width = 1;
		int height = 10;
		@Nullable Object proxy;
	}

	private static final Map<Object, Caret> CARETS = new WeakHashMap<>();
	private static boolean checked;
	private static @Nullable Object container;
	private static @Nullable Class<?> widgetType;
	private static @Nullable Method requestFocus;
	private static @Nullable Method removeFocus;
	private static @Nullable Constructor<?> rectangle;
	private static @Nullable Constructor<?> point;

	private TextInputs() {
	}

	/**
	 * Text input on or off: {@code owner} is what vanilla tracks (the screen), {@code field} what
	 * IMBlocker sees as the focused widget.
	 */
	public static void focus(GuiEventListener owner, Object field, boolean focused) {
		Minecraft.getInstance().onTextInputFocusChange(owner, focused);
		if (!bridge()) {
			return;
		}
		try {
			Caret caret = caret(field);
			if (caret.proxy == null) {
				caret.proxy = Proxy.newProxyInstance(widgetType.getClassLoader(), new Class<?>[]{widgetType}, handler(caret));
			}
			if (focused) {
				requestFocus.invoke(container, caret.proxy);
			} else {
				removeFocus.invoke(container, caret.proxy);
			}
		} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
			LiquidClient.LOGGER.warn("IMBlocker text input bridge failed; typing may not work in Liquid screens", e);
			container = null;
		}
	}

	/** Where the caret of {@code owner} is now (GUI units). */
	public static void caret(Object owner, int x, int y, int width, int height) {
		if (container == null) {
			return;
		}
		Caret caret = caret(owner);
		caret.x = x;
		caret.y = y;
		caret.width = Math.max(1, width);
		caret.height = Math.max(1, height);
	}

	private static Caret caret(Object owner) {
		return CARETS.computeIfAbsent(owner, key -> new Caret());
	}

	private static boolean bridge() {
		if (!checked) {
			checked = true;
			if (FabricLoader.getInstance().isModLoaded("imblocker")) {
				try {
					Class<?> containerType = Class.forName(IMBLOCKER + "FocusContainer");
					widgetType = Class.forName(IMBLOCKER + "FocusableWidget");
					container = containerType.getField("MINECRAFT").get(null);
					requestFocus = containerType.getMethod("requestFocus", widgetType);
					removeFocus = containerType.getMethod("removeFocus", widgetType);
					rectangle = Class.forName(IMBLOCKER + "Rectangle").getConstructor(int.class, int.class, int.class, int.class);
					point = Class.forName(IMBLOCKER + "Point").getConstructor(int.class, int.class);
					LiquidClient.LOGGER.info("IMBlocker found: Liquid text fields register with it for text input");
				} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
					LiquidClient.LOGGER.warn("IMBlocker found but its API changed; typing may not work in Liquid screens", e);
					container = null;
				}
			}
		}
		return container != null;
	}

	private static InvocationHandler handler(Caret caret) {
		return (proxy, method, args) -> switch (method.getName()) {
			case "getFocusContainer" -> container;
			case "getPreferredState", "isRenderable" -> Boolean.TRUE;
			case "getBoundsAbs" -> rectangle.newInstance(caret.x, caret.y, caret.width, caret.height);
			case "getCaretPos" -> point.newInstance(caret.x, caret.y);
			case "getFontHeight" -> caret.height;
			case "equals" -> proxy == args[0];
			case "hashCode" -> System.identityHashCode(proxy);
			case "toString" -> "LiquidTextField";
			default -> method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, args) : null;
		};
	}
}
