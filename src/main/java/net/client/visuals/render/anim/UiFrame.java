package net.client.visuals.render.anim;

/**
 * Per-frame timing shared by every component of the active screen.
 *
 * <p>The owning screen ticks its {@link DeltaTimer} once at the top of {@code extractRenderState}
 * and publishes the result here, so widgets don't each need a timer and every animation in a
 * frame advances by exactly the same delta.</p>
 */
public final class UiFrame {
	/** Seconds since the previous frame (clamped). */
	public static float dt;
	/** Seconds since the screen opened – drives ambient, looping motion. */
	public static float time;

	private UiFrame() {
	}

	public static void begin(DeltaTimer timer) {
		dt = timer.tick();
		time += dt;
	}
}
