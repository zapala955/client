package net.client.visuals.module.setting;

import com.google.gson.JsonElement;

/**
 * Base of every module setting. Values are stored as primitives in the subclasses so render code can
 * read them every frame without boxing.
 */
public abstract class Setting {
	private final String id;
	private final String translationKey;
	private boolean hidden;

	protected Setting(String id, String translationKey) {
		this.id = id;
		this.translationKey = translationKey;
	}

	/** Stable key used in the config file. */
	public String id() {
		return id;
	}

	public String translationKey() {
		return translationKey;
	}

	/** Hidden settings persist with the module but get no control (e.g. HUD positions). */
	public boolean hidden() {
		return hidden;
	}

	public void markHidden() {
		hidden = true;
	}

	public abstract JsonElement save();

	public abstract void load(JsonElement json);

	public abstract void reset();
}
