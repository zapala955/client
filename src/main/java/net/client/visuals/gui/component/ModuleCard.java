package net.client.visuals.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.module.Module;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * A module (or interface preference) card: an icon tile that lights up in the category colour while
 * the module is on, name, description, a keybind chip, the switch and a settings drawer with
 * sliders / dropdowns / switches.
 *
 * <pre>
 *  ┌──────────────────────────────────────────────────┐
 *  │ ╭──╮ Zoom                         [ C ]  ⌄  ◯━━ │
 *  │ ╰──╯ Hold a key to zoom in smoothly              │
 *  └──────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>Interaction: left-click toggles (or opens the drawer when there's no switch), right-click or the
 * chevron opens the drawer, the chip starts listening for a key (the window routes the next key to
 * {@link #finishBinding}). Hover lifts the card, brightens the rim and lights the inner glow; a press
 * contracts it towards {@link Motion#PRESS_SCALE} – both only while the drawer is closed, so drawer
 * controls never move under the cursor.</p>
 */
public final class ModuleCard extends UiComponent {
	private static final float BASE_HEIGHT = 40f;
	private static final float PADDING = 10f;
	private static final float SETTING_GAP = 7f;
	private static final float CARD_HOVER_SCALE = 0.012f;
	private static final float CHEVRON_SIZE = 14f;
	private static final float TILE = 24f;
	private static final float CHIP_H = 13f;
	private static final int ICON_KEYBOARD = 0xE284;

	private final TextLabel name;
	private final TextLabel description;
	private final TextLabel keyLabel = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel listenLabel;
	private final @Nullable ToggleSwitch toggle;
	private final @Nullable Module module;
	private final List<UiComponent> settings;
	private final String searchText;
	private final int icon;
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth press = new Smooth(0f, Motion.PRESS);
	private final Smooth expand = new Smooth(0f, Motion.TOGGLE);
	private final Smooth chipHover = new Smooth(0f, Motion.HOVER);
	private final Smooth enabledGlow;
	private final int accent;
	private @Nullable Consumer<ModuleCard> bindHandler;
	/** Stable id for remembering the card's state (module id, or "setting.<key>"). */
	private String key = "";
	private boolean expanded;
	private boolean pressed;
	private boolean listening;
	private int shownKey = Integer.MIN_VALUE;
	/** Vertical offset currently applied by the hover lift; mouse input is shifted by it. */
	private float lift;
	/** Key chip bounds (card-local x, width), 0 width = no chip this frame. */
	private float chipX;
	private float chipW;

	public ModuleCard(String name, String description, @Nullable BoolBinding binding, int accent, int icon,
			@Nullable Module module, List<UiComponent> settings) {
		this.name = new TextLabel(name, LiquidText.TextStyle.LABEL);
		this.description = new TextLabel(description, LiquidText.TextStyle.BODY);
		this.listenLabel = new TextLabel(I18n.get("liquidclient.clickgui.press_key"), LiquidText.TextStyle.CAPTION);
		this.accent = accent;
		this.icon = icon;
		this.module = module;
		this.toggle = binding != null ? new ToggleSwitch(binding).accent(accent) : null;
		this.settings = settings;
		this.searchText = (name + "\n" + description).toLowerCase(Locale.ROOT);
		this.enabledGlow = new Smooth(binding != null && binding.get() ? 1f : 0f, Motion.TOGGLE);
	}

	public static ModuleCard forModule(Module module, int accent) {
		BoolBinding binding = new BoolBinding() {
			@Override
			public boolean get() {
				return module.isEnabled();
			}

			@Override
			public void set(boolean value) {
				module.setEnabled(value);
			}
		};
		return new ModuleCard(module.displayName(), module.description(), binding, accent, module.icon(), module,
				SettingComponents.build(module.settings(), accent)).key(module.id());
	}

	public ModuleCard key(String key) {
		this.key = key;
		return this;
	}

	public String key() {
		return key;
	}

	/** Whether the settings drawer is open (or opening). */
	public boolean drawerOpen() {
		return expanded;
	}

	/** Restores a remembered drawer state at once, without animating. */
	public void restoreDrawer(boolean open) {
		if (!settings.isEmpty()) {
			expanded = open;
			expand.snap(open ? 1f : 0f);
		}
	}

	/** Called when the user clicks the key chip; the owner then routes the next key here. */
	public ModuleCard onBind(Consumer<ModuleCard> handler) {
		this.bindHandler = handler;
		return this;
	}

	/** Whether the card's switch is on. */
	public boolean isOn() {
		return toggle != null && toggle.value();
	}

	/** Whether the (lower-case) query matches this card's name or description. */
	public boolean matches(String query) {
		return searchText.contains(query);
	}

	private boolean bindable() {
		return module != null && module.bindable() && bindHandler != null;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	/**
	 * Applies the key the user pressed while listening: Escape cancels, Backspace / Delete clears the
	 * binding, anything else binds.
	 */
	public void finishBinding(int key) {
		listening = false;
		if (module == null || key == InputConstants.KEY_ESCAPE) {
			return;
		}
		module.setKeybind(key == InputConstants.KEY_BACKSPACE || key == InputConstants.KEY_DELETE ? Module.NO_KEY : key);
		UiSounds.toggle(module.keybind() != Module.NO_KEY);
	}

	private void refreshKeyLabel() {
		int key = module != null ? module.keybind() : Module.NO_KEY;
		if (key != shownKey) {
			shownKey = key;
			keyLabel.setText(key == Module.NO_KEY ? "" : InputConstants.Type.KEYBOARD.getOrCreate(key).getDisplayName().getString());
		}
	}

	private float drawerHeight(float width) {
		float h = SETTING_GAP + 4f;
		for (int i = 0; i < settings.size(); i++) {
			h += settings.get(i).preferredHeight(width - PADDING * 2f) + SETTING_GAP;
		}
		return h;
	}

	@Override
	public float preferredHeight(float width) {
		return BASE_HEIGHT + (settings.isEmpty() ? 0f : drawerHeight(width) * expand.get());
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		hover.speed(Motion.speed(Motion.HOVER));
		press.speed(Motion.speed(Motion.PRESS));
		expand.speed(Motion.speed(Motion.TOGGLE));
		boolean over = contains(mouseX, mouseY);
		float e = expand.target(expanded).update(dt);
		boolean interactive = e < 0.02f;
		float h = hover.target(over && interactive || listening).update(dt);
		float p = press.target(pressed && over && interactive).update(dt);
		float on = enabledGlow.target(toggle != null && toggle.value()).update(dt);

		lift = HighDpiEngine.snap(h * 1.25f);
		float scale = Motion.interactionScale(h, p, CARD_HOVER_SCALE);
		float cx = x + width * 0.5f;
		float cy = y + height * 0.5f;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy - lift);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);

		float r = 11f;
		GlassRenderer.accentGlow(g, x, y, width, height, r, accent, on * 0.4f + h * 0.15f);
		int fillTop = ColorUtil.lerp(Palette.GLASS_FILL_RAISED, 0x80334155, h);
		int fillBottom = ColorUtil.lerp(0x4D0F172A, 0x661E293B, h);
		int rimTop = ColorUtil.lerp(ColorUtil.lerp(Palette.GLASS_BORDER_HIGHLIGHT, Palette.GLASS_BORDER_HOVER, h), ColorUtil.fade(accent, 0.85f), on * 0.55f);
		GlassRenderer.surface(g, x, y, width, height, r, 0.5f + h * 1.5f, fillTop, fillBottom, rimTop, Palette.GLASS_BORDER);
		GlassRenderer.innerGlow(g, x, y, width, height, r, 0xFFFFFFFF, h);

		extractTile(g, x + 8f, y + 8f, on);

		float right = x + width - PADDING;
		int localMouseY = mouseY + Math.round(lift);
		if (toggle != null) {
			toggle.setBounds(right - ToggleSwitch.WIDTH, y + 14f - ToggleSwitch.HEIGHT * 0.5f + 0.5f, ToggleSwitch.WIDTH, ToggleSwitch.HEIGHT);
			toggle.extract(g, mouseX, localMouseY, h);
			right -= ToggleSwitch.WIDTH + 4f;
		}
		if (!settings.isEmpty()) {
			extractChevron(g, right - CHEVRON_SIZE * 0.5f, y + 14.5f, e, chevronHit(mouseX, localMouseY));
			right -= CHEVRON_SIZE + 4f;
		}
		right = extractKeyChip(g, mouseX, localMouseY, right, h);

		float textX = x + 8f + TILE + 8f;
		name.maxWidth(right - textX - 4f);
		description.maxWidth(x + width - PADDING - textX);
		LiquidText.draw(g, name, textX, y + 11f, Palette.TEXT_PRIMARY, true);
		LiquidText.draw(g, description, textX, y + 25f, Palette.TEXT_TERTIARY, false);

		if (e > 0.01f) {
			extractDrawer(g, mouseX, localMouseY, e);
		}
		pose.popMatrix();
	}

	/** Rounded icon tile: frosted grey while off, the category gradient while on. */
	private void extractTile(GuiGraphicsExtractor g, float tx, float ty, float on) {
		int top = ColorUtil.lerp(0x40FFFFFF, ColorUtil.lerpRgb(accent, 0xFFFFFFFF, 0.2f), on);
		int bottom = ColorUtil.lerp(0x1FFFFFFF, ColorUtil.lerpRgb(accent, 0xFF000000, 0.25f), on);
		if (on > 0.01f) {
			RenderUtils.outerGlow(g, tx, ty, TILE, TILE, 7f, 4f, ColorUtil.fade(accent, 0.35f * on));
		}
		RenderUtils.gradient(g, tx, ty, TILE, TILE, 7f, top, bottom);
		RenderUtils.stroke(g, tx, ty, TILE, TILE, 7f, GlassRenderer.hairline(), 0x40FFFFFF, 0x0DFFFFFF);
		IconFont.draw(g, icon, tx + TILE * 0.5f, ty + TILE * 0.5f, 13f, ColorUtil.lerp(0xCCFFFFFF, 0xFFFFFFFF, on), 0x33000000);
	}

	/** Draws the keybind chip ending at {@code right}; returns the new right edge for the title. */
	private float extractKeyChip(GuiGraphicsExtractor g, int mouseX, int mouseY, float right, float cardHover) {
		chipW = 0f;
		if (!bindable()) {
			return right;
		}
		refreshKeyLabel();
		boolean bound = shownKey != Module.NO_KEY;
		float w;
		float visibility;
		if (listening) {
			w = listenLabel.width() + 12f;
			visibility = 1f;
		} else if (bound) {
			w = Math.max(17f, keyLabel.width() + 10f);
			visibility = 1f;
		} else {
			w = 19f;
			visibility = cardHover;
		}
		if (visibility < 0.02f) {
			return right - w - 4f; // keep the title's room stable while the chip is hidden
		}
		chipW = w;
		chipX = right - w;
		float cy = y + 14.5f;
		float top = cy - CHIP_H * 0.5f;
		boolean overChip = mouseX >= chipX && mouseX < chipX + w && mouseY >= top && mouseY < top + CHIP_H;
		float ch = chipHover.target(overChip).update(UiFrame.dt);
		RenderUtils.pushAlpha(visibility);
		if (listening) {
			float pulse = 0.55f + 0.45f * (float) Math.sin(UiFrame.time * 6.0);
			RenderUtils.outerGlow(g, chipX, top, w, CHIP_H, CHIP_H * 0.5f, 3f, ColorUtil.fade(accent, 0.45f * pulse));
			RenderUtils.fill(g, chipX, top, w, CHIP_H, CHIP_H * 0.5f, ColorUtil.fade(accent, 0.35f));
			RenderUtils.stroke(g, chipX, top, w, CHIP_H, CHIP_H * 0.5f, GlassRenderer.hairline(), ColorUtil.fade(accent, pulse), ColorUtil.fade(accent, 0.4f));
			LiquidText.draw(g, listenLabel, chipX + 6f, cy - listenLabel.capHeight() * 0.5f, Palette.TEXT_PRIMARY, false);
		} else {
			RenderUtils.fill(g, chipX, top, w, CHIP_H, 4f, ColorUtil.lerp(0x1AFFFFFF, 0x33FFFFFF, ch));
			RenderUtils.stroke(g, chipX, top, w, CHIP_H, 4f, GlassRenderer.hairline(), 0x40FFFFFF, 0x14FFFFFF);
			// Key-cap bottom edge.
			RenderUtils.fill(g, chipX + 2f, top + CHIP_H - 1.5f, w - 4f, 1f, 0.5f, 0x26000000);
			if (bound) {
				LiquidText.draw(g, keyLabel, chipX + (w - keyLabel.width()) * 0.5f, cy - keyLabel.capHeight() * 0.5f,
						ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, ch), false);
			} else {
				IconFont.draw(g, ICON_KEYBOARD, chipX + w * 0.5f, cy, 9.5f, ColorUtil.lerp(Palette.TEXT_TERTIARY, Palette.TEXT_PRIMARY, ch), 0);
			}
		}
		RenderUtils.popAlpha();
		return chipX - 5f;
	}

	private boolean chipHit(double mouseX, double mouseY) {
		float top = y + 14.5f - CHIP_H * 0.5f;
		return chipW > 0f && mouseX >= chipX && mouseX < chipX + chipW && mouseY >= top && mouseY < top + CHIP_H;
	}

	private float chevronX() {
		float right = x + width - PADDING - (toggle != null ? ToggleSwitch.WIDTH + 4f : 0f);
		return right - CHEVRON_SIZE;
	}

	private boolean chevronHit(double mouseX, double mouseY) {
		float cx = chevronX();
		return !settings.isEmpty() && mouseX >= cx && mouseX < cx + CHEVRON_SIZE && mouseY >= y + 7f && mouseY < y + 22f;
	}

	private static void extractChevron(GuiGraphicsExtractor g, float cx, float cy, float open, boolean hovered) {
		if (hovered) {
			RenderUtils.circle(g, cx, cy, 6.5f, Palette.HOVER_FILL);
		}
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.PI * open);
		float thick = Math.max(1.1f, HighDpiEngine.px() * 2f);
		int color = hovered ? Palette.TEXT_PRIMARY : Palette.TEXT_SECONDARY;
		RenderUtils.line(g, -2.5f, -1f, 0f, 1.5f, thick, color);
		RenderUtils.line(g, 0f, 1.5f, 2.5f, -1f, thick, color);
		pose.popMatrix();
	}

	private void extractDrawer(GuiGraphicsExtractor g, int mouseX, int mouseY, float e) {
		float top = y + BASE_HEIGHT - 2f;
		RenderUtils.pushAlpha(e);
		RenderUtils.rect(g, x + PADDING, top, width - PADDING * 2f, GlassRenderer.hairline(), 0x1FFFFFFF);
		float cy = top + SETTING_GAP + 4f;
		float innerW = width - PADDING * 2f;
		for (int i = 0; i < settings.size(); i++) {
			UiComponent setting = settings.get(i);
			float sh = setting.preferredHeight(innerW);
			setting.setBounds(x + PADDING, cy, innerW, sh);
			setting.extract(g, mouseX, mouseY);
			cy += sh + SETTING_GAP;
		}
		RenderUtils.popAlpha();
	}

	// ---- Input --------------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!contains(mouseX, mouseY)) {
			return false;
		}
		double localY = mouseY + lift;
		if (expanded && localY >= y + BASE_HEIGHT) {
			for (int i = 0; i < settings.size(); i++) {
				if (settings.get(i).mouseClicked(mouseX, localY, button)) {
					return true;
				}
			}
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && bindHandler != null && chipHit(mouseX, localY)) {
			listening = !listening;
			UiSounds.click();
			bindHandler.accept(listening ? this : null);
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_RIGHT || (button == InputConstants.MOUSE_BUTTON_LEFT && chevronHit(mouseX, localY))) {
			toggleDrawer();
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			pressed = true;
			if (toggle != null) {
				toggle.toggle();
			} else {
				toggleDrawer();
			}
			return true;
		}
		return false;
	}

	private void toggleDrawer() {
		if (settings.isEmpty()) {
			return;
		}
		expanded = !expanded;
		UiSounds.click();
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		pressed = false;
		for (int i = 0; i < settings.size(); i++) {
			if (settings.get(i).mouseReleased(mouseX, mouseY + lift, button)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		for (int i = 0; i < settings.size(); i++) {
			if (settings.get(i).mouseDragged(mouseX, mouseY + lift, button, dragX, dragY)) {
				return true;
			}
		}
		return false;
	}
}
