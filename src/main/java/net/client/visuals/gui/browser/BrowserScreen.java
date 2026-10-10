package net.client.visuals.gui.browser;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.window.TextInputs;
import net.client.visuals.browser.BrowserEngine;
import net.client.visuals.browser.BrowserInput;
import net.client.visuals.browser.BrowserManager;
import net.client.visuals.browser.BrowserPanel;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.utility.BrowserModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Interaction mode for the in-world browser screen.
 *
 * <p>The world keeps rendering; the mouse cursor is free and every mouse position is turned into a
 * ray from the camera through that pixel, intersected with the screen in the world – so you click,
 * drag, select and scroll exactly where you point, at any distance or angle. The keyboard goes to the
 * page (or to the address bar when it's focused).</p>
 *
 * <pre>
 *  ╭─────────────────────────────────────────────────────────────╮
 *  │ ‹ › ⟳ │ ● example.com  —  Example Domain           │ ⌂ ⊙ ✕ │   glass toolbar (top)
 *  ╰─────────────────────────────────────────────────────────────╯
 *                    (the browser screen in the world)
 *              Esc back to game · Ctrl+L address bar · F5 reload       hint (bottom)
 * </pre>
 *
 * <p>26.3 / SDL: character input only arrives while SDL text input is active, so the screen starts it
 * in {@link #init()} and stops it in {@link #removed()}.</p>
 */
public final class BrowserScreen extends Screen {
	private static final int BTN_BACK = 0;
	private static final int BTN_FORWARD = 1;
	private static final int BTN_RELOAD = 2;
	private static final int BTN_HOME = 3;
	private static final int BTN_MOVE = 4;
	private static final int BTN_CLOSE = 5;
	private static final float BAR_H = 28f;
	private static final float BTN = 22f;

	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth appear = new Smooth(0f, 10f);
	private final Smooth[] buttonHover = new Smooth[6];
	private final Smooth fieldFocus = new Smooth(0f, Motion.HOVER);
	private final TextLabel hint = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel host = new TextLabel("", LiquidText.TextStyle.LABEL);
	private final TextLabel pageTitle = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel fieldText = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel status = new TextLabel("", LiquidText.TextStyle.LABEL);
	private final TextLabel caretProbe = new TextLabel("", LiquidText.TextStyle.BODY);
	private BrowserEngine.Status shownStatus;

	private final StringBuilder field = new StringBuilder();
	private boolean fieldFocused;
	private boolean selectAll;
	private int caret;
	private float caretBlink;

	private int heldButtons;
	private double lastPageX = -1;
	private double lastPageY = -1;
	private int lastMouseX = Integer.MIN_VALUE;
	private int lastMouseY = Integer.MIN_VALUE;
	private String shownUrl = "";
	private String shownTitle = "";

	private float barX;
	private float barY;
	private float barW;

	public BrowserScreen() {
		super(Component.translatable("liquidclient.module.browser"));
		for (int i = 0; i < buttonHover.length; i++) {
			buttonHover[i] = new Smooth(0f, Motion.HOVER);
		}
	}

	private static BrowserEngine engine() {
		return BrowserManager.engine();
	}

	@Override
	protected void init() {
		ShaderSupport.revalidate();
		TextInputs.focus(this, this, true);
		hint.setText(I18n.get("liquidclient.browser.hint.screen"));
		timer.reset();
	}

	@Override
	public void removed() {
		TextInputs.focus(this, this, false);
		// Release anything still held so the page doesn't think a button is stuck.
		if (heldButtons != 0 && lastPageX >= 0) {
			BrowserInput.mouse(engine(), "mouseReleased", lastPageX, lastPageY, "left", 1, 0);
		}
		heldButtons = 0;
		ModuleManager.saveIfDirty();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		// No dimming or blur: the browser lives in the world behind this screen.
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		float dt = UiFrame.dt;
		float a = appear.target(1f).update(dt);
		caretBlink = (caretBlink + dt) % 1.06f;
		GlassRenderer.beginFrame(false);

		// Hover → mousemove on the page (only when the cursor actually moved).
		if (mouseX != lastMouseX || mouseY != lastMouseY) {
			lastMouseX = mouseX;
			lastMouseY = mouseY;
			if (!overToolbar(mouseX, mouseY)) {
				BrowserPanel.Hit hit = pageHit(mouseX, mouseY);
				if (hit != null) {
					movePointer(hit);
				}
			}
		}

		refreshLabels();
		RenderUtils.pushAlpha(a);
		extractToolbar(g, mouseX, mouseY, a);
		extractStatus(g);
		float hintW = hint.width() + 24f;
		float hx = HighDpiEngine.snap((width - hintW) * 0.5f);
		float hy = HighDpiEngine.snap(height - 30f);
		GlassRenderer.panel(g, hx, hy, hintW, 18f, 9f, 1.5f);
		LiquidText.draw(g, hint, hx + 12f, hy + 9f - hint.capHeight() * 0.5f, Palette.TEXT_SECONDARY, false);
		RenderUtils.popAlpha();
	}

	private void refreshLabels() {
		String url = engine().url();
		String title = engine().title();
		if (!url.equals(shownUrl)) {
			shownUrl = url;
			host.setText(hostOf(url));
		}
		if (!title.equals(shownTitle)) {
			shownTitle = title;
			pageTitle.setText(title);
		}
	}

	private void extractToolbar(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		float dt = UiFrame.dt;
		barW = Math.min(660f, width - 24f);
		barX = HighDpiEngine.snap((width - barW) * 0.5f);
		barY = HighDpiEngine.snap(10f - (1f - a) * 8f);
		GlassRenderer.panel(g, barX, barY, barW, BAR_H, 13f, 3f);

		// Left: back / forward / reload. Right: home / move here / close.
		for (int i = 0; i < 6; i++) {
			float bx = buttonX(i);
			float by = barY + (BAR_H - BTN) * 0.5f;
			boolean over = mouseX >= bx && mouseX < bx + BTN && mouseY >= by && mouseY < by + BTN;
			float h = buttonHover[i].target(over).update(dt);
			if (h > 0.01f) {
				RenderUtils.fill(g, bx, by, BTN, BTN, 8f, ColorUtil.fade(i == BTN_CLOSE ? 0x40FF5F57 : Palette.HOVER_FILL, 0.6f + 0.4f * h));
			}
			int color = ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, h);
			extractIcon(g, i, bx + BTN * 0.5f, by + BTN * 0.5f, color);
		}

		// Address field.
		float fx = fieldX();
		float fw = fieldWidth();
		float fy = barY + 4f;
		float fh = BAR_H - 8f;
		float focus = fieldFocus.target(fieldFocused).update(dt);
		RenderUtils.fill(g, fx, fy, fw, fh, 8f, ColorUtil.lerp(0x1AFFFFFF, 0x2EFFFFFF, focus));
		if (focus > 0.01f) {
			GlassRenderer.accentGlow(g, fx, fy, fw, fh, 8f, Palette.ACCENT_BLUE, focus * 0.8f);
			RenderUtils.hairline(g, fx, fy, fw, fh, 8f, ColorUtil.fade(Palette.ACCENT_BLUE, focus), ColorUtil.fade(Palette.ACCENT_BLUE, focus * 0.6f));
		}
		float textY = fy + fh * 0.5f;
		if (fieldFocused) {
			fieldText.maxWidth(fw - 20f);
			float tx = fx + 10f;
			if (selectAll && field.length() > 0) {
				RenderUtils.fill(g, tx - 1f, textY - 6f, fieldText.width() + 2f, 12f, 3f, 0x66007AFF);
			}
			LiquidText.draw(g, fieldText, tx, textY - fieldText.capHeight() * 0.5f, Palette.TEXT_PRIMARY, false);
			if (!selectAll && caretBlink < 0.53f) {
				float cx = tx + caretOffset();
				RenderUtils.rect(g, cx, textY - 5.5f, Math.max(HighDpiEngine.px(), 0.75f), 11f, Palette.ACCENT_BLUE);
			}
		} else {
			boolean secure = shownUrl.startsWith("https://");
			RenderUtils.circle(g, fx + 11f, textY, 2.5f, secure ? Palette.ACCENT_GREEN : 0x99FFFFFF);
			host.maxWidth(fw * 0.45f);
			LiquidText.draw(g, host, fx + 19f, textY - host.capHeight() * 0.5f, Palette.TEXT_PRIMARY, true);
			if (!shownTitle.isBlank()) {
				pageTitle.maxWidth(Math.max(0f, fw - host.width() - 40f));
				LiquidText.draw(g, pageTitle, fx + 27f + host.width(), textY - pageTitle.capHeight() * 0.5f, Palette.TEXT_TERTIARY, false);
			}
		}

		// Loading shimmer under the toolbar.
		if (engine().loading()) {
			float t = (UiFrame.time * 0.8f) % 1f;
			float lw = barW * 0.28f;
			float lx = barX + 12f + (barW - 24f - lw) * t;
			RenderUtils.gradientH(g, lx, barY + BAR_H - 2.5f, lw, 2f, 1f, 0x00007AFF, Palette.ACCENT_BLUE);
		}
	}

	private void extractStatus(GuiGraphicsExtractor g) {
		BrowserEngine.Status s = engine().status();
		if (s == BrowserEngine.Status.READY) {
			return;
		}
		if (s != shownStatus) {
			shownStatus = s;
			status.setText(switch (s) {
				case STARTING -> I18n.get("liquidclient.browser.starting");
				case FAILED -> I18n.get("liquidclient.browser.failed", engine().error());
				default -> I18n.get("liquidclient.browser.stopped");
			});
		}
		float w = status.width() + 28f;
		float x = HighDpiEngine.snap((width - w) * 0.5f);
		float y = HighDpiEngine.snap(height * 0.5f - 12f);
		GlassRenderer.panel(g, x, y, w, 24f, 12f, 2f);
		LiquidText.draw(g, status, x + 14f, y + 12f - status.capHeight() * 0.5f, s == BrowserEngine.Status.FAILED ? 0xFFFF8FA3 : Palette.TEXT_PRIMARY, true);
	}

	private void extractIcon(GuiGraphicsExtractor g, int index, float cx, float cy, int color) {
		float t = 1.4f;
		switch (index) {
			case BTN_BACK -> {
				RenderUtils.line(g, cx + 1.5f, cy - 4f, cx - 2.5f, cy, t, color);
				RenderUtils.line(g, cx - 2.5f, cy, cx + 1.5f, cy + 4f, t, color);
			}
			case BTN_FORWARD -> {
				RenderUtils.line(g, cx - 1.5f, cy - 4f, cx + 2.5f, cy, t, color);
				RenderUtils.line(g, cx + 2.5f, cy, cx - 1.5f, cy + 4f, t, color);
			}
			case BTN_RELOAD -> {
				if (engine().loading()) {
					RenderUtils.line(g, cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, t, color);
					RenderUtils.line(g, cx - 3.5f, cy + 3.5f, cx + 3.5f, cy - 3.5f, t, color);
				} else {
					float r = 4.5f;
					float start = (float) Math.toRadians(45);
					float end = (float) Math.toRadians(330);
					RenderUtils.arc(g, cx, cy, r, start, end, t, color);
					float ex = cx + (float) Math.sin(end) * r;
					float ey = cy - (float) Math.cos(end) * r;
					RenderUtils.line(g, ex, ey, ex + 3f, ey - 0.5f, t, color);
					RenderUtils.line(g, ex, ey, ex + 0.5f, ey + 3f, t, color);
				}
			}
			case BTN_HOME -> {
				RenderUtils.line(g, cx - 5f, cy, cx, cy - 4.5f, t, color);
				RenderUtils.line(g, cx, cy - 4.5f, cx + 5f, cy, t, color);
				RenderUtils.line(g, cx - 3.5f, cy - 1f, cx - 3.5f, cy + 4.5f, t, color);
				RenderUtils.line(g, cx + 3.5f, cy - 1f, cx + 3.5f, cy + 4.5f, t, color);
				RenderUtils.line(g, cx - 3.5f, cy + 4.5f, cx + 3.5f, cy + 4.5f, t, color);
			}
			case BTN_MOVE -> {
				RenderUtils.ring(g, cx, cy, 4.5f, t, color);
				RenderUtils.circle(g, cx, cy, 1.6f, color);
				RenderUtils.line(g, cx, cy - 7f, cx, cy - 5.5f, t, color);
				RenderUtils.line(g, cx, cy + 5.5f, cx, cy + 7f, t, color);
				RenderUtils.line(g, cx - 7f, cy, cx - 5.5f, cy, t, color);
				RenderUtils.line(g, cx + 5.5f, cy, cx + 7f, cy, t, color);
			}
			default -> {
				RenderUtils.line(g, cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, t, color);
				RenderUtils.line(g, cx - 3.5f, cy + 3.5f, cx + 3.5f, cy - 3.5f, t, color);
			}
		}
	}

	// ---- Layout -------------------------------------------------------------------------------

	private float buttonX(int index) {
		float pad = 4f;
		if (index <= BTN_RELOAD) {
			return barX + pad + index * (BTN + 2f);
		}
		int fromRight = BTN_CLOSE - index;
		return barX + barW - pad - BTN - fromRight * (BTN + 2f);
	}

	private float fieldX() {
		return buttonX(BTN_RELOAD) + BTN + 8f;
	}

	private float fieldWidth() {
		return buttonX(BTN_HOME) - 8f - fieldX();
	}

	private boolean overToolbar(double mouseX, double mouseY) {
		return mouseX >= barX && mouseX < barX + barW && mouseY >= barY && mouseY < barY + BAR_H;
	}

	private int buttonAt(double mouseX, double mouseY) {
		for (int i = 0; i < 6; i++) {
			float bx = buttonX(i);
			float by = barY + (BAR_H - BTN) * 0.5f;
			if (mouseX >= bx && mouseX < bx + BTN && mouseY >= by && mouseY < by + BTN) {
				return i;
			}
		}
		return -1;
	}

	private boolean overField(double mouseX, double mouseY) {
		float fx = fieldX();
		return mouseX >= fx && mouseX < fx + fieldWidth() && mouseY >= barY && mouseY < barY + BAR_H;
	}

	private float caretOffset() {
		return Math.min(caretProbe.width(), fieldText.width());
	}

	/** Re-shapes the address text and caret position – only after an edit, never per frame. */
	private void syncField() {
		fieldText.setText(field.toString());
		caretProbe.setText(field.substring(0, Math.min(caret, field.length())));
		caretBlink = 0f;
	}

	// ---- Mouse → world ray → page -------------------------------------------------------------

	/** Ray from the camera through the mouse pixel, intersected with the in-world screen. */
	private BrowserPanel.@Nullable Hit pageHit(double mouseX, double mouseY) {
		BrowserPanel panel = BrowserManager.panel();
		if (panel == null) {
			return null;
		}
		Camera camera = minecraft.gameRenderer.mainCamera();
		Matrix4f projection = minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.projectionMatrix;
		float ndcX = (float) (mouseX / width) * 2f - 1f;
		float ndcY = 1f - (float) (mouseY / height) * 2f;
		float sx = ndcX / projection.m00();
		float sy = ndcY / projection.m11();
		Vector3fc f = camera.forwardVector();
		Vector3fc up = camera.upVector();
		Vector3fc left = camera.leftVector();
		double dx = f.x() - left.x() * sx + up.x() * sy;
		double dy = f.y() - left.y() * sx + up.y() * sy;
		double dz = f.z() - left.z() * sx + up.z() * sy;
		Vec3 origin = camera.position();
		return panel.raycast(origin.x, origin.y, origin.z, dx, dy, dz);
	}

	private void movePointer(BrowserPanel.Hit hit) {
		lastPageX = hit.u() * BrowserInput.width(engine());
		lastPageY = hit.v() * BrowserInput.height(engine());
		BrowserInput.mouse(engine(), "mouseMoved", lastPageX, lastPageY, "none", 0, heldButtons);
	}

	private static String buttonName(int button) {
		if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
			return "right";
		}
		if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
			return "middle";
		}
		return "left";
	}

	private static int buttonBit(int button) {
		if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
			return 2;
		}
		if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
			return 4;
		}
		return 1;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		if (overToolbar(mx, my)) {
			int button = buttonAt(mx, my);
			if (button >= 0 && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
				press(button);
			} else if (overField(mx, my)) {
				focusField();
			}
			return true;
		}
		if (fieldFocused) {
			fieldFocused = false;
		}
		BrowserPanel.Hit hit = pageHit(mx, my);
		if (hit != null) {
			movePointer(hit);
			heldButtons |= buttonBit(event.button());
			BrowserInput.mouse(engine(), "mousePressed", lastPageX, lastPageY, buttonName(event.button()), doubleClick ? 2 : 1, heldButtons);
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		int bit = buttonBit(event.button());
		if ((heldButtons & bit) != 0) {
			heldButtons &= ~bit;
			BrowserPanel.Hit hit = pageHit(event.x(), event.y());
			if (hit != null) {
				lastPageX = hit.u() * BrowserInput.width(engine());
				lastPageY = hit.v() * BrowserInput.height(engine());
			}
			BrowserInput.mouse(engine(), "mouseReleased", lastPageX, lastPageY, buttonName(event.button()), 1, heldButtons);
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		BrowserPanel.Hit hit = pageHit(event.x(), event.y());
		if (hit != null) {
			movePointer(hit);
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		BrowserPanel.Hit hit = pageHit(mouseX, mouseY);
		if (hit != null) {
			BrowserInput.wheel(engine(), hit.u(), hit.v(), -scrollX * 120.0, -scrollY * 120.0);
		}
		return true;
	}

	private void press(int button) {
		UiSounds.click();
		switch (button) {
			case BTN_BACK -> BrowserInput.back(engine());
			case BTN_FORWARD -> BrowserInput.forward(engine());
			case BTN_RELOAD -> BrowserInput.reload(engine());
			case BTN_HOME -> BrowserInput.navigate(engine(), ModuleManager.get(BrowserModule.class).homeUrl());
			case BTN_MOVE -> BrowserManager.moveToPlayer();
			default -> {
				BrowserManager.despawn();
				onClose();
			}
		}
	}

	// ---- Keyboard -----------------------------------------------------------------------------

	private void focusField() {
		fieldFocused = true;
		selectAll = true;
		field.setLength(0);
		field.append(shownUrl.startsWith("about:") ? "" : shownUrl);
		caret = field.length();
		syncField();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = event.input();
		if (event.isEscape()) {
			if (fieldFocused) {
				fieldFocused = false;
			} else {
				onClose();
			}
			return true;
		}
		if (fieldFocused) {
			return fieldKey(event);
		}
		if (event.hasControlDown() && key == InputConstants.KEY_L) {
			focusField();
			return true;
		}
		if (key == InputConstants.KEY_F5) {
			BrowserInput.reload(engine());
			return true;
		}
		if (event.hasAltDown() && (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_RIGHT)) {
			if (key == InputConstants.KEY_LEFT) {
				BrowserInput.back(engine());
			} else {
				BrowserInput.forward(engine());
			}
			return true;
		}
		if (event.hasControlDown() && key == InputConstants.KEY_V) {
			BrowserInput.insertText(engine(), minecraft.keyboardHandler.getClipboard());
			return true;
		}
		if (event.hasControlDown() && key == InputConstants.KEY_C) {
			BrowserInput.copySelection(engine(), text -> minecraft.execute(() -> minecraft.keyboardHandler.setClipboard(text)));
			return true;
		}
		BrowserInput.keyDown(engine(), event);
		return true;
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		if (!fieldFocused) {
			BrowserInput.keyUp(engine(), event);
		}
		return true;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		String text = event.codepointAsString();
		if (fieldFocused) {
			if (selectAll) {
				field.setLength(0);
				caret = 0;
				selectAll = false;
			}
			field.insert(caret, text);
			caret += text.length();
			syncField();
			return true;
		}
		BrowserInput.text(engine(), text);
		return true;
	}

	private boolean fieldKey(KeyEvent event) {
		boolean handled = fieldEdit(event);
		syncField();
		return handled;
	}

	private boolean fieldEdit(KeyEvent event) {
		int key = event.input();
		if (key == InputConstants.KEY_RETURN || key == 88) {
			String target = normalize(field.toString());
			if (target != null) {
				BrowserInput.navigate(engine(), target);
			}
			fieldFocused = false;
			return true;
		}
		if (event.hasControlDown() && key == InputConstants.KEY_A) {
			selectAll = true;
			return true;
		}
		if (event.hasControlDown() && key == InputConstants.KEY_V) {
			if (selectAll) {
				field.setLength(0);
				caret = 0;
				selectAll = false;
			}
			String clip = minecraft.keyboardHandler.getClipboard().replace("\n", " ").replace("\r", "");
			field.insert(caret, clip);
			caret += clip.length();
			return true;
		}
		if (event.hasControlDown() && key == InputConstants.KEY_C) {
			minecraft.keyboardHandler.setClipboard(field.toString());
			return true;
		}
		if (key == InputConstants.KEY_BACKSPACE || key == InputConstants.KEY_DELETE) {
			if (selectAll) {
				field.setLength(0);
				caret = 0;
				selectAll = false;
			} else if (key == InputConstants.KEY_BACKSPACE && caret > 0) {
				field.deleteCharAt(--caret);
			} else if (key == InputConstants.KEY_DELETE && caret < field.length()) {
				field.deleteCharAt(caret);
			}
			return true;
		}
		if (key == InputConstants.KEY_LEFT) {
			selectAll = false;
			caret = Math.max(0, caret - 1);
		} else if (key == InputConstants.KEY_RIGHT) {
			selectAll = false;
			caret = Math.min(field.length(), caret + 1);
		} else if (key == InputConstants.KEY_HOME) {
			selectAll = false;
			caret = 0;
		} else if (key == InputConstants.KEY_END) {
			selectAll = false;
			caret = field.length();
		}
		return true;
	}

	/** URL, bare domain or search query → URL to load. */
	private static @Nullable String normalize(String input) {
		String s = input.trim();
		if (s.isEmpty()) {
			return null;
		}
		if (s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
			return s; // has a scheme (https:, about:, file:...)
		}
		if (!s.contains(" ") && (s.contains(".") || s.startsWith("localhost"))) {
			return "https://" + s;
		}
		String q = URLEncoder.encode(s, StandardCharsets.UTF_8);
		return switch (ModuleManager.get(BrowserModule.class).home.index()) {
			case 1 -> "https://www.google.com/search?q=" + q;
			case 2 -> "https://www.youtube.com/results?search_query=" + q;
			case 3 -> "https://en.wikipedia.org/w/index.php?search=" + q;
			case 4 -> "https://www.bing.com/search?q=" + q;
			default -> "https://duckduckgo.com/?q=" + q;
		};
	}

	private static String hostOf(String url) {
		try {
			String h = URI.create(url).getHost();
			if (h != null && !h.isEmpty()) {
				return h.startsWith("www.") ? h.substring(4) : h;
			}
		} catch (IllegalArgumentException ignored) {
			// not a URI – show as is
		}
		return url;
	}
}
