package net.client.visuals.render.anim;

/**
 * A critically-damped style value follower using exponential decay.
 *
 * <p>{@code value += (target - value) * (1 - e^(-speed * dt))} is frame-rate independent:
 * 30 FPS and 300 FPS converge along the same curve in wall-clock time, unlike the naive
 * {@code value += (target - value) * 0.1f} which speeds up with the frame rate.</p>
 *
 * <p>Mutable on purpose – one instance lives per animated property and is updated in place,
 * so the render loop never allocates.</p>
 */
public final class Smooth {
	private static final float SNAP_EPSILON = 1.0E-4f;

	private float value;
	private float target;
	private float speed;

	/**
	 * @param initial starting value (and target)
	 * @param speed   responsiveness in 1/seconds. ~63% of the distance is covered in {@code 1/speed} s.
	 */
	public Smooth(float initial, float speed) {
		this.value = initial;
		this.target = initial;
		this.speed = speed;
	}

	/** Advances the follower by {@code dt} seconds and returns the new value. */
	public float update(float dt) {
		float diff = target - value;
		if (Math.abs(diff) < SNAP_EPSILON) {
			value = target;
		} else {
			value += diff * (1f - (float) Math.exp(-speed * dt));
		}
		return value;
	}

	public float get() {
		return value;
	}

	public float target() {
		return target;
	}

	public Smooth target(float target) {
		this.target = target;
		return this;
	}

	/** Convenience for boolean states (hovered, enabled, selected...). */
	public Smooth target(boolean on) {
		this.target = on ? 1f : 0f;
		return this;
	}

	/** Jumps to a value without animating (used for "reduce motion" and initial layout). */
	public void snap(float v) {
		this.value = v;
		this.target = v;
	}

	public void speed(float speed) {
		this.speed = speed;
	}

	public boolean settled() {
		return value == target;
	}
}
