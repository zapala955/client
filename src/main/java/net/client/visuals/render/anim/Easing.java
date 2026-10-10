package net.client.visuals.render.anim;

/**
 * Stateless easing curves. Implemented as an enum with constant-specific bodies so that
 * selecting / invoking a curve never allocates (no lambdas captured in render code).
 *
 * <p>Motion guideline used throughout the client: <b>decelerate on arrival</b>
 * ({@link #OUT_CUBIC}, {@link #OUT_BACK}) and <b>accelerate on departure</b> ({@link #IN_CUBIC}),
 * with exits roughly 40% faster than entries.</p>
 */
public enum Easing {
	LINEAR {
		@Override
		public float apply(float t) {
			return t;
		}
	},
	OUT_CUBIC {
		@Override
		public float apply(float t) {
			float inv = 1f - t;
			return 1f - inv * inv * inv;
		}
	},
	IN_CUBIC {
		@Override
		public float apply(float t) {
			return t * t * t;
		}
	},
	IN_OUT_SINE {
		@Override
		public float apply(float t) {
			return (float) (-(Math.cos(Math.PI * t) - 1.0) * 0.5);
		}
	},
	OUT_EXPO {
		@Override
		public float apply(float t) {
			return t >= 1f ? 1f : (float) (1.0 - Math.pow(2.0, -10.0 * t));
		}
	},
	/** Slight overshoot – used for the "liquid" pop of windows and cards appearing. */
	OUT_BACK {
		@Override
		public float apply(float t) {
			final float c1 = 1.20158f; // softer than the classic 1.70158 overshoot
			final float c3 = c1 + 1f;
			float x = t - 1f;
			return 1f + c3 * x * x * x + c1 * x * x;
		}
	};

	/** @param t normalized progress in [0, 1] */
	public abstract float apply(float t);

	public static float clamp01(float t) {
		return t < 0f ? 0f : (t > 1f ? 1f : t);
	}
}
