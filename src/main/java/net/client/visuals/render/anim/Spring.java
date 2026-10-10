package net.client.visuals.render.anim;

/**
 * A damped spring: a value pulled towards its target that can overshoot and settle, like UIKit's
 * spring animations – the motion of the Dynamic Island.
 *
 * <p>Tuned the way Apple tunes them: {@code response} is the period of the undamped oscillation in
 * seconds (how quickly it gets there), {@code damping} the damping fraction (1 = no overshoot,
 * 0.7 ≈ one small overshoot). Each step solves the oscillator exactly over {@code dt}, so the motion
 * is the same at 30 and 300 FPS and can't blow up after a long frame. Retargeting keeps the
 * velocity, so an interrupted animation bends smoothly towards its new goal instead of restarting.</p>
 *
 * <p>Mutable on purpose – one instance per animated property, updated in place (no allocation).</p>
 */
public final class Spring {
	private static final float SETTLE = 1.0E-3f;

	private float value;
	private float velocity;
	private float target;
	private float omega;
	private float damping;

	/**
	 * @param initial  starting value (and target)
	 * @param response seconds per (undamped) oscillation; smaller = snappier
	 * @param damping  damping fraction in (0, 1]; 1 = critically damped
	 */
	public Spring(float initial, float response, float damping) {
		this.value = initial;
		this.target = initial;
		tune(response, damping);
	}

	/** Changes the feel (e.g. snappier on collapse than on expand); keeps value and velocity. */
	public Spring tune(float response, float damping) {
		this.omega = (float) (2.0 * Math.PI / Math.max(0.02f, Motion.seconds(response)));
		this.damping = Math.clamp(damping, 0.05f, 1f);
		return this;
	}

	public Spring target(float target) {
		this.target = target;
		return this;
	}

	public float target() {
		return target;
	}

	public float get() {
		return value;
	}

	public float velocity() {
		return velocity;
	}

	/** Adds velocity (units per second) – a kick that springs back, e.g. a bounce on new content. */
	public void kick(float impulse) {
		velocity += impulse;
	}

	/** Jumps to a value at rest. */
	public void snap(float v) {
		value = v;
		target = v;
		velocity = 0f;
	}

	public boolean settled() {
		return value == target && velocity == 0f;
	}

	/** Advances by {@code dt} seconds and returns the new value. */
	public float update(float dt) {
		if (dt <= 0f) {
			return value;
		}
		float x0 = value - target;
		float v0 = velocity;
		float scale = Math.max(1f, Math.abs(target));
		if (Math.abs(x0) < SETTLE * scale && Math.abs(v0) < SETTLE * 10f * scale) {
			value = target;
			velocity = 0f;
			return value;
		}
		double w = omega;
		double z = damping;
		double t = Math.min(dt, 0.25f);
		double x;
		double v;
		if (z < 0.999) {
			// Underdamped: x(t) = e^(-ζωt) (A cos ω_d t + B sin ω_d t)
			double wd = w * Math.sqrt(1.0 - z * z);
			double a = x0;
			double b = (v0 + z * w * x0) / wd;
			double decay = Math.exp(-z * w * t);
			double cos = Math.cos(wd * t);
			double sin = Math.sin(wd * t);
			x = decay * (a * cos + b * sin);
			v = decay * ((b * wd - z * w * a) * cos - (a * wd + z * w * b) * sin);
		} else {
			// Critically damped: x(t) = e^(-ωt) (A + B t)
			double a = x0;
			double b = v0 + w * x0;
			double decay = Math.exp(-w * t);
			x = decay * (a + b * t);
			v = decay * (b - w * (a + b * t));
		}
		value = (float) (target + x);
		velocity = (float) v;
		return value;
	}
}
