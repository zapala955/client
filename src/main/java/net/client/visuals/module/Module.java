package net.client.visuals.module;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.module.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for client modules.
 *
 * <p>Lifecycle contract: {@link #onEnable()} / {@link #onDisable()} run on the client thread when the
 * state flips (from the ClickGUI, a keybind or when the config loads). Keep them cheap and free of
 * GPU calls – renderers should observe {@link #isEnabled()} each frame and create/release GPU
 * resources on the render thread themselves (see {@code LiquidSky}). That keeps toggling during a
 * disconnect or resource reload safe.</p>
 *
 * <p>Display strings come from {@code liquidclient.module.<id>} and {@code .desc} translation keys.
 * Every module can be bound to a key ({@link #keybind()}, an SDL scancode from
 * {@code InputConstants}); the binding is saved with the module.</p>
 */
public abstract class Module {
	/** Scancode 0 (SDL_SCANCODE_UNKNOWN): not bound. */
	public static final int NO_KEY = InputConstants.UNKNOWN.getValue();

	private final String id;
	private final Category category;
	private final int icon;
	private final List<Setting> settings = new ArrayList<>();
	private final List<Setting> settingsView = Collections.unmodifiableList(settings);
	private boolean enabled;
	private int keybind = NO_KEY;

	protected Module(String id, Category category) {
		this(id, category, category.icon);
	}

	/** @param icon Lucide codepoint shown on the module's card, notifications and the module list */
	protected Module(String id, Category category, int icon) {
		this.id = id;
		this.category = category;
		this.icon = icon;
	}

	protected final <S extends Setting> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	/** A setting that's saved with the module but not shown in the UI. */
	protected final <S extends Setting> S addHidden(S setting) {
		setting.markHidden();
		return add(setting);
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
		ModuleManager.markDirty();
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	public final boolean isEnabled() {
		return enabled;
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	/** End of every client tick while enabled (20 Hz), for state that eases or tracks the world. */
	public void clientTick(Minecraft minecraft) {
	}

	public final int keybind() {
		return keybind;
	}

	public final void setKeybind(int key) {
		if (keybind != key) {
			keybind = key;
			ModuleManager.markDirty();
		}
	}

	/** Whether the module can be bound to a key (HUD elements and the theme can't). */
	public boolean bindable() {
		return category != Category.HUD;
	}

	/** Whether being on is saved and restored at launch (off for modes like Photo Mode, which start on demand). */
	public boolean savesState() {
		return true;
	}

	/** Shown in the HUD's module list (off for HUD elements and other plumbing). */
	public boolean listed() {
		return category != Category.HUD;
	}

	public final String id() {
		return id;
	}

	public final Category category() {
		return category;
	}

	public final int icon() {
		return icon;
	}

	public final List<Setting> settings() {
		return settingsView;
	}

	public String displayName() {
		return I18n.get("liquidclient.module." + id);
	}

	public String description() {
		return I18n.get("liquidclient.module." + id + ".desc");
	}
}
