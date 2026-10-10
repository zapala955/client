package net.client.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.client.visuals.module.ModuleManager;

/**
 * A choice between fixed modes (shown as a dropdown). Modes are identified by stable string ids in
 * the config so reordering them never corrupts saved settings.
 */
public final class ModeSetting extends Setting {
	private final String[] modeIds;
	private final String[] modeKeys;
	private final int defaultIndex;
	private int index;
	private Runnable listener = () -> {
	};

	/**
	 * @param modeIds  stable ids, also used to build translation keys {@code <translationKey>.<id>}
	 */
	public ModeSetting(String id, String translationKey, String[] modeIds, int defaultIndex) {
		super(id, translationKey);
		this.modeIds = modeIds;
		this.modeKeys = new String[modeIds.length];
		for (int i = 0; i < modeIds.length; i++) {
			modeKeys[i] = translationKey + "." + modeIds[i];
		}
		this.defaultIndex = defaultIndex;
		this.index = defaultIndex;
	}

	/** Called after the selection changes (e.g. persist a config). */
	public ModeSetting onChange(Runnable listener) {
		this.listener = listener;
		return this;
	}

	public int index() {
		return index;
	}

	public void set(int index) {
		int clamped = Math.floorMod(index, modeIds.length);
		if (clamped != this.index) {
			this.index = clamped;
			ModuleManager.markDirty();
			listener.run();
		}
	}

	public int count() {
		return modeIds.length;
	}

	public String modeKey(int i) {
		return modeKeys[i];
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(modeIds[index]);
	}

	@Override
	public void load(JsonElement json) {
		if (!json.isJsonPrimitive()) {
			return;
		}
		String saved = json.getAsString();
		for (int i = 0; i < modeIds.length; i++) {
			if (modeIds[i].equals(saved)) {
				index = i;
				return;
			}
		}
	}

	@Override
	public void reset() {
		index = defaultIndex;
	}
}
