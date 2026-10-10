package net.client.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.client.visuals.module.ModuleManager;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.DoubleFunction;

/** A slider value in {@code [min, max]}, snapped to {@code step}. */
public final class NumberSetting extends Setting {
	/** How the slider track is drawn. */
	public enum Track {
		ACCENT,
		/** Rainbow track for hue values (0..360). */
		HUE
	}

	private final double min;
	private final double max;
	private final double step;
	private final double defaultValue;
	private double value;
	private String suffix = "";
	private int decimals;
	private Track track = Track.ACCENT;
	private @Nullable DoubleFunction<String> formatter;

	/** Formatted value, rebuilt only when the value changes (never per frame). */
	private String display;
	private double displayed = Double.NaN;

	public NumberSetting(String id, String translationKey, double min, double max, double step, double defaultValue) {
		super(id, translationKey);
		this.min = min;
		this.max = max;
		this.step = step;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.decimals = step >= 1 ? 0 : step >= 0.1 ? 1 : 2;
	}

	public NumberSetting suffix(String suffix) {
		this.suffix = suffix;
		this.displayed = Double.NaN;
		return this;
	}

	/** Custom value text (e.g. a clock time); runs only when the value changes. */
	public NumberSetting format(DoubleFunction<String> formatter) {
		this.formatter = formatter;
		this.displayed = Double.NaN;
		return this;
	}

	public NumberSetting track(Track track) {
		this.track = track;
		return this;
	}

	public double get() {
		return value;
	}

	public float getFloat() {
		return (float) value;
	}

	public void set(double value) {
		double snapped = step > 0 ? Math.round((value - min) / step) * step + min : value;
		snapped = Math.clamp(snapped, min, max);
		if (snapped != this.value) {
			this.value = snapped;
			ModuleManager.markDirty();
		}
	}

	/** Value mapped to 0..1 along the slider. */
	public double normalized() {
		return max > min ? (value - min) / (max - min) : 0.0;
	}

	public void setNormalized(double t) {
		set(min + Math.clamp(t, 0.0, 1.0) * (max - min));
	}

	public double min() {
		return min;
	}

	public double max() {
		return max;
	}

	public Track trackStyle() {
		return track;
	}

	public String display() {
		if (value != displayed) {
			displayed = value;
			display = formatter != null ? formatter.apply(value) : String.format(Locale.ROOT, "%." + decimals + "f", value) + suffix;
		}
		return display;
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement json) {
		if (json.isJsonPrimitive()) {
			value = Math.clamp(json.getAsDouble(), min, max);
		}
	}

	@Override
	public void reset() {
		value = defaultValue;
	}
}
