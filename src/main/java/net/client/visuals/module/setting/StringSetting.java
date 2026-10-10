package net.client.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.client.visuals.module.ModuleManager;

/**
 * A persisted text value. Not shown as a ClickGUI control (no generic text component yet) – used
 * for state a module remembers between sessions, e.g. the browser's last page.
 */
public final class StringSetting extends Setting {
	private final String defaultValue;
	private String value;

	public StringSetting(String id, String translationKey, String defaultValue) {
		super(id, translationKey);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String get() {
		return value;
	}

	public void set(String value) {
		if (value != null && !value.equals(this.value)) {
			this.value = value;
			ModuleManager.markDirty();
		}
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement json) {
		if (json.isJsonPrimitive()) {
			value = json.getAsString();
		}
	}

	@Override
	public void reset() {
		value = defaultValue;
	}
}
