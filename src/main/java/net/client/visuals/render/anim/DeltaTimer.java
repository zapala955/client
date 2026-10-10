package net.client.visuals.render.anim;

/**
 * Wall-clock frame timer. Each screen owns one and calls {@link #tick()} once per frame.
 *
 * <p>Minecraft's partial tick is tied to the 20 TPS game loop (and is frozen while the game is
 * paused), so it's unsuitable for UI motion. Real time keeps animations smooth even when the
 * integrated server is paused behind the ClickGUI.</p>
 */
public final class DeltaTimer {
	/** Clamp so a hitch (world load, GC, alt-tab) never teleports an animation to its end. */
	private static final float MAX_DELTA = 1f / 15f;

	private long lastNanos = -1L;
	private float delta;

	/** @return seconds elapsed since the previous call, clamped to {@link #MAX_DELTA}. */
	public float tick() {
		long now = System.nanoTime();
		if (lastNanos < 0L) {
			delta = 0f;
		} else {
			delta = Math.min(MAX_DELTA, (now - lastNanos) * 1.0E-9f);
		}
		lastNanos = now;
		return delta;
	}

	public float delta() {
		return delta;
	}

	public void reset() {
		lastNanos = -1L;
		delta = 0f;
	}
}
