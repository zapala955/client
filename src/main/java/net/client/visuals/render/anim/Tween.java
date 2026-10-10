package net.client.visuals.render.anim;

/**
 * A duration-based 0→1 progress driver with an easing curve, for choreographed transitions
 * (screen open / close, staggered entry). Unlike {@link Smooth} it has a defined end time,
 * which lets a screen know exactly when its exit animation has finished.
 */
public final class Tween {
	private float elapsed;
	private float duration;
	/**
	 * The choreography's design length (constructor argument). {@link #staggered} delays are on this
	 * timeline, so when a playback is shortened (Reduce Motion) the whole cascade compresses with it
	 * instead of elements starting after the tween has already ended.
	 */
	private final float nominal;
	private Easing easing;
	private boolean reversed;

	public Tween(float durationSeconds, Easing easing) {
		this.duration = Math.max(1.0E-4f, durationSeconds);
		this.nominal = this.duration;
		this.easing = easing;
	}

	/** Restarts forward playback (0 → 1). */
	public void play(float durationSeconds, Easing easing) {
		this.duration = Math.max(1.0E-4f, durationSeconds);
		this.easing = easing;
		this.elapsed = 0f;
		this.reversed = false;
	}

	/**
	 * Plays backwards (current → 0) from wherever the tween currently is, so interrupting
	 * an opening animation with a close never "jumps".
	 */
	public void reverse(float durationSeconds, Easing easing) {
		float current = raw();
		this.duration = Math.max(1.0E-4f, durationSeconds);
		this.easing = easing;
		this.reversed = true;
		this.elapsed = (1f - current) * this.duration;
	}

	public void update(float dt) {
		elapsed = Math.min(duration, elapsed + dt);
	}

	/** Linear progress in [0, 1], direction-aware. */
	public float raw() {
		float t = elapsed / duration;
		return reversed ? 1f - t : t;
	}

	/** Eased progress in [0, 1]. */
	public float value() {
		float t = elapsed / duration;
		return reversed ? 1f - easing.apply(t) : easing.apply(t);
	}

	/**
	 * Eased progress for an element that starts {@code delay} seconds late and lasts
	 * {@code length} seconds – the building block of staggered entry animations.
	 * While reversed (closing) every element exits together, which reads cleaner than a
	 * reverse cascade.
	 */
	public float staggered(float delay, float length, Easing curve) {
		if (reversed) {
			return value();
		}
		if (elapsed >= duration) {
			return curve.apply(1f); // finished: everything is in, whatever the delays
		}
		float t = elapsed / duration * nominal;
		float local = Easing.clamp01((t - delay) / Math.max(1.0E-4f, length));
		return curve.apply(local);
	}

	public boolean finished() {
		return elapsed >= duration;
	}

	public boolean reversed() {
		return reversed;
	}

	public void finish() {
		elapsed = duration;
	}
}
