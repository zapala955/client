package net.client.visuals.render.particle;

/** How particles move; also decides where they appear (see {@code AmbientParticles#spawn}). */
public final class ParticleMotion {
	/** Drift on the air currents, bobbing gently. */
	public static final int FLOAT = 0;
	/** Straight down, swaying with the turbulence. */
	public static final int FALL = 1;
	/** Down like a leaf: pendulum sway, tumbling in 3D. */
	public static final int FLUTTER = 2;
	/** Up with a soft wobble. */
	public static final int RISE = 3;
	/** Fireflies: steer towards new headings, held near their height. */
	public static final int WANDER = 4;
	/** A vortex around the player. */
	public static final int SWIRL = 5;
	/** A ring circling the player (always follows). */
	public static final int ORBIT = 6;
	/** Hover in place, barely drifting. */
	public static final int STILL = 7;
	/** Helix rising around its spawn point. */
	public static final int SPIRAL = 8;
	/** Fast diagonal streaks high above (shooting stars). */
	public static final int METEOR = 9;

	public static final String[] IDS = {
			"float", "fall", "flutter", "rise", "wander", "swirl", "orbit", "still", "spiral", "meteor"
	};

	private ParticleMotion() {
	}
}
