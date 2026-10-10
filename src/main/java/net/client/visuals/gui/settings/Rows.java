package net.client.visuals.gui.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.BoolBinding;
import net.client.visuals.gui.component.ToggleSwitch;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.Popover;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** The non-vanilla row types of the settings list. */
public final class Rows {
	private static final int ICON_CHEVRON = 0xE06F;
	private static final int ICON_EXTERNAL = 0xE0B9;
	private static final int ICON_CHECK = 0xE06C;

	private Rows() {
	}

	/** A switch bound to any boolean (client config, skin layers). */
	public static final class Switch extends SettingRow {
		private final ToggleSwitch toggle;
		private final List<TextLabel> description;

		public Switch(SettingsScreen host, String label, @Nullable String description, BoolBinding binding) {
			super(host, label);
			this.toggle = new ToggleSwitch(binding);
			this.description = description == null || description.isEmpty() ? List.of()
					: Ui.wrap(description, 230f, LiquidText.TextStyle.BODY);
		}

		@Override
		protected float controlWidth() {
			return ToggleSwitch.WIDTH;
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			toggle.setBounds(right - ToggleSwitch.WIDTH, cy - ToggleSwitch.HEIGHT * 0.5f, ToggleSwitch.WIDTH, ToggleSwitch.HEIGHT);
			toggle.extract(g, mouseX, mouseY, hover);
		}

		@Override
		protected List<TextLabel> description() {
			return description;
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (enabled() && button == InputConstants.MOUSE_BUTTON_LEFT && over(mx, my)) {
				toggle.toggle();
				host.optionChanged();
				return true;
			}
			return false;
		}
	}

	/** A pop-up menu over a fixed list of names. */
	public static final class Choice extends SettingRow {
		private final Supplier<List<String>> items;
		private final IntSupplier current;
		private final IntConsumer pick;
		private final TextLabel value = new TextLabel("", LiquidText.TextStyle.BODY);
		private final Smooth hoverButton = new Smooth(0f, Motion.HOVER);
		private final List<TextLabel> description;
		private int shown = Integer.MIN_VALUE;
		private float bx;
		private float by;
		private float bw;

		public Choice(SettingsScreen host, String label, @Nullable String description, Supplier<List<String>> items,
				IntSupplier current, IntConsumer pick) {
			super(host, label);
			this.items = items;
			this.current = current;
			this.pick = pick;
			this.description = description == null || description.isEmpty() ? List.of()
					: Ui.wrap(description, 230f, LiquidText.TextStyle.BODY);
		}

		@Override
		protected float controlWidth() {
			return Math.clamp(value.width() + 34f, 84f, 176f);
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			int index = current.getAsInt();
			if (index != shown) {
				shown = index;
				List<String> list = items.get();
				value.setText(index >= 0 && index < list.size() ? list.get(index) : "—");
			}
			bw = controlWidth();
			bx = right - bw;
			by = cy - (Ui.CONTROL_H - 2f) * 0.5f;
			boolean over = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + Ui.CONTROL_H - 2f;
			Popover.button(g, value, bx, by, bw, Ui.CONTROL_H - 2f, hoverButton.target(over).update(UiFrame.dt));
		}

		@Override
		protected List<TextLabel> description() {
			return description;
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (!enabled() || button != InputConstants.MOUSE_BUTTON_LEFT || !over(mx, my)) {
				return false;
			}
			host.openPopover(new Popover(items.get(), current.getAsInt(), index -> {
				pick.accept(index);
				shown = Integer.MIN_VALUE;
				host.optionChanged();
			}, bx, by, bw, Ui.CONTROL_H - 2f, host.width, host.height));
			return true;
		}
	}

	/** Opens a screen or page; the whole row is the button, with a chevron (or ↗ for other screens). */
	public static final class Link extends SettingRow {
		private final Runnable action;
		private final boolean external;
		private final @Nullable Supplier<String> valueSource;
		private final TextLabel value = new TextLabel("", LiquidText.TextStyle.BODY);
		private final int icon;
		private final int iconColor;

		public Link(SettingsScreen host, String label, boolean external, Runnable action) {
			this(host, label, 0, 0, external, null, action);
		}

		public Link(SettingsScreen host, String label, int icon, int iconColor, boolean external,
				@Nullable Supplier<String> value, Runnable action) {
			super(host, label);
			this.action = action;
			this.external = external;
			this.valueSource = value;
			this.icon = icon;
			this.iconColor = iconColor;
		}

		@Override
		protected boolean clickableRow() {
			return true;
		}

		@Override
		protected float controlWidth() {
			return 12f + (value.text().isEmpty() ? 0f : value.width() + 6f);
		}

		@Override
		protected void extractLabel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
			if (icon == 0) {
				super.extractLabel(g, x, y, w, h);
				return;
			}
			float size = 18f;
			Ui.tile(g, x + PAD - 2f, y + (h - size) * 0.5f, size, iconColor);
			IconFont.draw(g, icon, x + PAD - 2f + size * 0.5f, y + h * 0.5f, 10.5f, 0xFFFFFFFF, 0);
			label.maxWidth(Math.max(20f, w - PAD * 2f - size - 8f - controlWidth() - 10f));
			LiquidText.draw(g, label, x + PAD + size + 6f, y + (h - label.capHeight()) * 0.5f, Ui.TEXT, false);
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			if (valueSource != null) {
				value.setText(valueSource.get());
			}
			int color = ColorUtil.lerp(Ui.TEXT_3, Ui.TEXT_2, hover);
			IconFont.draw(g, external ? ICON_EXTERNAL : ICON_CHEVRON, right - 4f, cy, external ? 9f : 10f, color, 0);
			if (!value.text().isEmpty()) {
				LiquidText.draw(g, value, right - 14f - value.width(), cy - value.capHeight() * 0.5f, Ui.TEXT_2, false);
			}
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (enabled() && button == InputConstants.MOUSE_BUTTON_LEFT && over(mx, my)) {
				UiSounds.click();
				action.run();
				return true;
			}
			return false;
		}
	}

	/** A read-only value (About section). */
	public static final class Info extends SettingRow {
		private final TextLabel value;

		public Info(SettingsScreen host, String label, String value) {
			super(host, label);
			this.value = new TextLabel(value, LiquidText.TextStyle.BODY);
		}

		@Override
		protected float controlWidth() {
			return Math.min(220f, value.width());
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			value.maxWidth(220f);
			LiquidText.draw(g, value, right - value.width(), cy - value.capHeight() * 0.5f, Ui.TEXT_2, false);
		}
	}

	/** A short explanatory note inside a group (restart warnings, hints), optionally tinted. */
	public static final class Note extends SettingRow {
		private final List<TextLabel> lines;
		private final int icon;
		private final int color;

		public Note(SettingsScreen host, int icon, int color, String text) {
			super(host, "");
			this.icon = icon;
			this.color = color;
			this.lines = Ui.wrap(text, 330f, LiquidText.TextStyle.BODY);
		}

		@Override
		public boolean matches(String query) {
			return false;
		}

		@Override
		public float height() {
			return Math.max(H, lines.size() * 11f + 16f);
		}

		@Override
		protected float controlWidth() {
			return 0f;
		}

		@Override
		protected void extractLabel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
			float tx = x + PAD;
			if (icon != 0) {
				IconFont.draw(g, icon, x + PAD + 5f, y + h * 0.5f, 11f, color, 0);
				tx += 17f;
			}
			float ty = y + (h - (lines.size() * 11f - (11f - 6.55f))) * 0.5f;
			for (TextLabel line : lines) {
				line.maxWidth(w - (tx - x) - PAD);
				LiquidText.draw(g, line, tx, ty, icon != 0 ? ColorUtil.lerpRgb(color, 0xFFFFFFFF, 0.35f) : Ui.TEXT_2, false);
				ty += 11f;
			}
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
		}
	}

	/** One language of the Language page: native name, region below, a check on the chosen one. */
	public static final class Language extends SettingRow {
		private final String code;
		private final TextLabel region;
		private final Supplier<String> chosen;
		private final Runnable select;
		private final boolean current;
		private final TextLabel currentBadge = new TextLabel("Current", LiquidText.TextStyle.CAPTION);
		private final String regionKey;

		public Language(SettingsScreen host, String code, String name, String region, boolean current,
				Supplier<String> chosen, Runnable select) {
			super(host, name);
			this.code = code;
			this.region = new TextLabel(region, LiquidText.TextStyle.CAPTION);
			this.regionKey = region.toLowerCase(java.util.Locale.ROOT) + " " + code;
			this.current = current;
			this.chosen = chosen;
			this.select = select;
		}

		@Override
		public boolean matches(String query) {
			return super.matches(query) || regionKey.contains(query);
		}

		@Override
		protected boolean clickableRow() {
			return true;
		}

		@Override
		protected float controlWidth() {
			return 14f + (current ? currentBadge.width() + 10f : 0f);
		}

		@Override
		protected void extractLabel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
			float max = Math.max(20f, w - PAD * 2f - controlWidth() - 10f);
			label.maxWidth(max);
			region.maxWidth(max);
			LiquidText.draw(g, label, x + PAD, y + 7.5f, Ui.TEXT, false);
			LiquidText.draw(g, region, x + PAD, y + 18.5f, Ui.TEXT_3, false);
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			if (code.equals(chosen.get())) {
				IconFont.draw(g, ICON_CHECK, right - 5f, cy, 11f, Ui.ACCENT, 0);
			}
			if (current) {
				float bw = currentBadge.width() + 10f;
				float bx = right - 16f - bw;
				RenderUtils.fill(g, bx, cy - 6.5f, bw, 13f, 6.5f, Ui.FILL_CONTROL);
				LiquidText.draw(g, currentBadge, bx + 5f, cy - currentBadge.capHeight() * 0.5f, Ui.TEXT_2, false);
			}
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (button == InputConstants.MOUSE_BUTTON_LEFT && over(mx, my)) {
				UiSounds.click();
				select.run();
				return true;
			}
			return false;
		}
	}

	/** A label with a pill button (Apply language, reset actions); the label can change. */
	public static final class Action extends SettingRow {
		private final PillButton button;
		private final @Nullable Supplier<String> labelSource;

		public Action(SettingsScreen host, String label, @Nullable Supplier<String> labelSource, String buttonText,
				PillButton.Kind kind, Runnable action) {
			super(host, label);
			this.labelSource = labelSource;
			this.button = new PillButton(buttonText, 0, kind, action);
		}

		@Override
		protected float controlWidth() {
			return Math.max(64f, button.naturalWidth());
		}

		@Override
		protected void extractLabel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
			if (labelSource != null) {
				label.setText(labelSource.get());
			}
			super.extractLabel(g, x, y, w, h);
		}

		@Override
		protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover) {
			float bw = controlWidth();
			button.setBounds(right - bw, cy - Ui.CONTROL_H * 0.5f, bw, Ui.CONTROL_H);
			button.extract(g, mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			return enabled() && this.button.mouseClicked(mx, my, button);
		}

		@Override
		public boolean mouseReleased(double mx, double my, int button) {
			return this.button.mouseReleased(mx, my, button);
		}
	}
}
