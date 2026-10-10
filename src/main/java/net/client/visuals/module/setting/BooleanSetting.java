package net.client.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.client.visuals.module.ModuleManager;

public final class BooleanSetting extends Setting {
	private final boolean defaultValue;
	private boolean value;

	public BooleanSetting(String id, String translationKey, boolean defaultValue) {
		super(id, translationKey);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean get() {
		return value;
	}

	public void set(boolean value) {
		if (this.value != value) {
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
			value = json.getAsBoolean();
		}
	}

	@Override
	public void reset() {
		value = defaultValue;
	}
}
