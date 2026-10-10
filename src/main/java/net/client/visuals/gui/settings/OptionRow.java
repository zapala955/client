package net.client.visuals.gui.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.BoolBinding;
import net.client.visuals.gui.component.ToggleSwitch;
import net.client.visuals.gui.window.Popover;
import net.client.visuals.gui.window.Slider;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.mixin.access.OptionInstanceAccessor;
import net.client.visuals.mixin.access.TooltipAccessor;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A vanilla {@link OptionInstance} with a native-looking control picked from its value set:
 * booleans get a switch, cycleable sets a pop-up menu, sliderable sets a slider with the value
 * beside it. Values are written exactly the way vanilla's own widgets write them (the set's
 * value setter, deferred slider application, {@code options.save()}), so every side effect –
 * presets, resource reloads, window changes – behaves as in vanilla.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class OptionRow extends SettingRow {
	private enum Kind { SWITCH, CHOICE, SLIDER }

	private static final float SLIDER_W = 132f;
	private static final float VALUE_GAP = 8f;
	private static final float CHOICE_MIN = 84f;
	private static final float CHOICE_MAX = 176f;

	private final OptionInstance<Object> option;
	private final OptionInstanceAccessor access;
	private final String caption;
	private final Kind kind;
	private final @Nullable ToggleSwitch toggle;
	private final @Nullable Slider slider;
	private final TextLabel value = new TextLabel("", LiquidText.TextStyle.BODY);
	private final Smooth choiceHover = new Smooth(0f, Motion.HOVER);
	private @Nullable Object labelled;
	private boolean hasLabel;
	/** Slider value not yet applied (sets that apply on release, e.g. render distance). */
	private @Nullable Object pending;
	private @Nullable Object described;
	private boolean hasDescription;
	private List<TextLabel> descriptionLines = List.of();
	private float choiceX;
	private float choiceY;
	private float choiceW;

	public OptionRow(SettingsScreen host, OptionInstance<?> option) {
		this(host, option, ((OptionInstanceAccessor) (Object) option).liquid$caption().getString());
	}

	public OptionRow(SettingsScreen host, OptionInstance<?> option, String label) {
		super(host, label);
		this.option = (OptionInstance<Object>) option;
		this.access = (OptionInstanceAccessor) (Object) option;
		this.caption = access.liquid$caption().getString();
		OptionInstance.ValueSet<Object> values = this.option.values();
		if (values == (Object) OptionInstance.BOOLEAN_VALUES) {
			kind = Kind.SWITCH;
		} else if (values instanceof OptionInstance.SliderableOrCyclableValueSet both) {
			kind = both.createCycleButton() ? Kind.CHOICE : Kind.SLIDER;
		} else if (values instanceof OptionInstance.CycleableValueSet) {
			kind = Kind.CHOICE;
		} else if (values instanceof OptionInstance.SliderableValueSet) {
			kind = Kind.SLIDER;
		} else {
			kind = Kind.CHOICE;
		}
		if (kind == Kind.SWITCH) {
			toggle = new ToggleSwitch(new BoolBinding() {
				@Override
				public boolean get() {
					return Boolean.TRUE.equals(OptionRow.this.option.get());
				}

				@Override
				public void set(boolean on) {
					write(on);
				}
			});
			slider = null;
		} else if (kind == Kind.SLIDER) {
			OptionInstance.SliderableValueSet<Object> range = (OptionInstance.SliderableValueSet<Object>) values;
			toggle = null;
			slider = new Slider(() -> range.toSliderValue(pending != null ? pending : this.option.get()),
					t -> {
						Object next = range.fromSliderValue(t);
						if (range.applyValueImmediately()) {
							if (!Objects.equals(next, this.option.get())) {
								this.option.set(next);
							}
						} else {
							pending = next;
						}
					},
					() -> {
						if (pending != null) {
							Object apply = pending;
							pending = null;
							if (!Objects.equals(apply, this.option.get())) {
								this.option.set(apply);
							}
						}
						host.optionChanged();
					});
			if ((Object) values instanceof OptionInstance.IntRangeBase ints) {
				int steps = ints.maxInclusive() - ints.minInclusive() + 1;
				slider.ticks(steps <= 11 ? steps : 0);
			}
		} else {
			toggle = null;
			slider = null;
		}
		value.maxWidth(kind == Kind.SLIDER ? 120f : CHOICE_MAX - 34f);
	}

	@Override
	public Object identity() {
		return option;
	}

	@Override
	public boolean matches(String query) {
		return super.matches(query) || caption.toLowerCase(java.util.Locale.ROOT).contains(query);
	}

	/** Vanilla's widget write path: the set's value setter, then save. */
	private void write(Object next) {
		if (option.values() instanceof OptionInstance.CycleableValueSet cycle) {
			cycle.valueSetter().set(option, next);
		} else {
			option.set(next);
		}
		Minecraft.getInstance().options.save();
		host.optionChanged();
	}

	/** The value part of vanilla's "Caption: Value" message. */
	private String valueText(Object v) {
		String text = access.liquid$valueText().apply(v).getString();
		if (text.startsWith(caption + ": ")) {
			return text.substring(caption.length() + 2);
		}
		if (text.startsWith(caption + ":")) {
			return text.substring(caption.length() + 1).trim();
		}
		return text;
	}

	private void updateValueLabel() {
		Object current = pending != null ? pending : option.get();
		if (!hasLabel || !Objects.equals(current, labelled)) {
			hasLabel = true;
			labelled = current;
			value.setText(valueText(current));
		}
	}

	@Override
	protected float controlWidth() {
		if (kind != Kind.SWITCH) {
			updateValueLabel();
		}
		return switch (kind) {
			case SWITCH -> ToggleSwitch.WIDTH;
			case SLIDER -> SLIDER_W + VALUE_GAP + Math.max(28f, value.width());
			case CHOICE -> choiceWidth();
		};
	}

	private float choiceWidth() {
		return Math.clamp(value.width() + 34f, CHOICE_MIN, CHOICE_MAX);
	}

	@Override
	protected void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float rowHover) {
		switch (kind) {
			case SWITCH -> {
				toggle.setBounds(right - ToggleSwitch.WIDTH, cy - ToggleSwitch.HEIGHT * 0.5f, ToggleSwitch.WIDTH, ToggleSwitch.HEIGHT);
				toggle.extract(g, mouseX, mouseY, rowHover);
			}
			case SLIDER -> {
				slider.setEnabled(enabled());
				slider.setBounds(right - SLIDER_W, cy - slider.preferredHeight(SLIDER_W) * 0.5f, SLIDER_W, slider.preferredHeight(SLIDER_W));
				slider.extract(g, mouseX, mouseY);
				LiquidText.draw(g, value, right - SLIDER_W - VALUE_GAP - value.width(), cy - value.capHeight() * 0.5f,
						slider.dragging() ? Ui.TEXT : Ui.TEXT_2, false);
			}
			case CHOICE -> {
				choiceW = choiceWidth();
				choiceX = right - choiceW;
				choiceY = cy - (Ui.CONTROL_H - 2f) * 0.5f;
				boolean over = mouseX >= choiceX && mouseX < choiceX + choiceW && mouseY >= choiceY && mouseY < choiceY + Ui.CONTROL_H - 2f;
				float hv = choiceHover.target(over).update(UiFrame.dt);
				Popover.button(g, value, choiceX, choiceY, choiceW, Ui.CONTROL_H - 2f, hv);
			}
		}
	}

	@Override
	protected List<TextLabel> description() {
		Object current = option.get();
		if (!hasDescription || !Objects.equals(current, described)) {
			hasDescription = true;
			described = current;
			Tooltip tooltip = access.liquid$tooltip().apply(current);
			descriptionLines = tooltip == null ? List.of()
					: Ui.wrap(((TooltipAccessor) tooltip).liquid$message().getString(), 230f, LiquidText.TextStyle.BODY);
		}
		return descriptionLines;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (!enabled() || button != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		switch (kind) {
			case SWITCH -> {
				if (over(mx, my)) {
					toggle.toggle();
					return true;
				}
			}
			case SLIDER -> {
				return slider.mouseClicked(mx, my, button);
			}
			case CHOICE -> {
				if (over(mx, my)) {
					openChoices();
					return true;
				}
			}
		}
		return false;
	}

	private void openChoices() {
		if (!(option.values() instanceof OptionInstance.CycleableValueSet cycle)) {
			return;
		}
		List<Object> choices = new ArrayList<>(cycle.valueListSupplier().getSelectedList());
		List<String> labels = new ArrayList<>(choices.size());
		for (Object choice : choices) {
			labels.add(valueText(choice));
		}
		int current = Popover.indexOf(choices, option.get());
		host.openPopover(new Popover(labels, current, index -> write(choices.get(index)),
				choiceX, choiceY, choiceW, Ui.CONTROL_H - 2f, host.width, host.height));
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		return slider != null && slider.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		return slider != null && slider.mouseReleased(mx, my, button);
	}

	/** Labels can change with the language or with presets; rebuilt on demand. */
	public Component caption() {
		return access.liquid$caption();
	}
}
