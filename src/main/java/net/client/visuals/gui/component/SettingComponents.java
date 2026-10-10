package net.client.visuals.gui.component;

import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.module.setting.Setting;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

/** Maps module settings to ClickGUI controls (slider / dropdown / switch). */
public final class SettingComponents {
	private SettingComponents() {
	}

	public static List<UiComponent> build(List<Setting> settings, int accent) {
		List<UiComponent> components = new ArrayList<>(settings.size());
		for (Setting setting : settings) {
			if (setting.hidden()) {
				continue;
			}
			UiComponent component = create(setting, accent);
			if (component != null) {
				components.add(component);
			}
		}
		return components;
	}

	public static UiComponent create(Setting setting, int accent) {
		return switch (setting) {
			case NumberSetting number -> new SliderComponent(number, accent);
			case ModeSetting mode -> new DropdownComponent(mode, accent);
			case BooleanSetting bool -> new ToggleRow(I18n.get(bool.translationKey()), new BoolBinding() {
				@Override
				public boolean get() {
					return bool.get();
				}

				@Override
				public void set(boolean value) {
					bool.set(value);
				}
			}, accent);
			default -> null;
		};
	}
}
