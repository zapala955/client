package net.client.visuals.render.particle;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * Particle shapes. Procedural shapes are distance fields in {@code ambient_particle.fsh} (their ids
 * are the shader's shape numbers); sprite shapes sample the vanilla particle atlas.
 *
 * <p>Each shape also names vanilla sprites for the fallback path (custom pipelines unavailable or
 * an Iris shader pack active), where particles are drawn with vanilla's own particle pipeline.</p>
 */
public final class ParticleShape {
	public static final int ORB = 0;
	public static final int SPARKLE = 1;
	public static final int STAR = 2;
	public static final int BOKEH = 3;
	public static final int RING = 4;
	public static final int SNOWFLAKE = 5;
	public static final int PETAL = 6;
	public static final int LEAF = 7;
	public static final int HEART = 8;
	public static final int DIAMOND = 9;
	public static final int CONFETTI = 10;
	public static final int BUBBLE = 11;
	public static final int FLARE = 12;
	public static final int NOTE = 13;
	public static final int RUNE = 14;
	/** Motion trails (internal): {@code STREAK + thickness level 0..15}, see {@code ambient_particle.fsh}. */
	static final int STREAK = 16;

	/** Stable ids, in shape-number order. */
	public static final String[] IDS = {
			"orb", "sparkle", "star", "bokeh", "ring", "snowflake", "petal", "leaf", "heart", "diamond",
			"confetti", "bubble", "flare", "note", "rune"
	};

	private static final Identifier[] RUNES = new Identifier[26];
	private static final Identifier[] PETALS = new Identifier[12];
	private static final Identifier[] LEAVES = new Identifier[4];
	private static final Identifier GLOW = Identifier.withDefaultNamespace("glow");
	private static final Identifier GLITTER = Identifier.withDefaultNamespace("glitter_2");
	private static final Identifier DOT = Identifier.withDefaultNamespace("generic_2");
	private static final Identifier SQUARE = Identifier.withDefaultNamespace("generic_0");
	private static final Identifier HEART_SPRITE = Identifier.withDefaultNamespace("heart");
	private static final Identifier BUBBLE_SPRITE = Identifier.withDefaultNamespace("bubble");
	private static final Identifier NOTE_SPRITE = Identifier.withDefaultNamespace("note");

	static {
		for (int i = 0; i < RUNES.length; i++) {
			RUNES[i] = Identifier.withDefaultNamespace("sga_" + (char) ('a' + i));
		}
		for (int i = 0; i < PETALS.length; i++) {
			PETALS[i] = Identifier.withDefaultNamespace("cherry_" + i);
		}
		for (int i = 0; i < LEAVES.length; i++) {
			LEAVES[i] = Identifier.withDefaultNamespace("orange_poplar_" + (i + 1));
		}
	}

	private ParticleShape() {
	}

	/** Drawn from the particle atlas even with the custom shader (no distance field). */
	public static boolean isSprite(int shape) {
		return shape == NOTE || shape == RUNE;
	}

	/** Sprites that carry their own colours: the fallback path draws them untinted. */
	static boolean coloredSprite(int shape) {
		return shape == PETAL || shape == LEAF || shape == HEART || shape == BUBBLE;
	}

	/** Vanilla particle sprite for this shape (its own look for sprite shapes, a stand-in otherwise). */
	static Identifier sprite(int shape, RandomSource random) {
		return switch (shape) {
			case SPARKLE, STAR, DIAMOND -> GLITTER;
			case SNOWFLAKE -> DOT;
			case PETAL -> PETALS[random.nextInt(PETALS.length)];
			case LEAF -> LEAVES[random.nextInt(LEAVES.length)];
			case HEART -> HEART_SPRITE;
			case CONFETTI -> SQUARE;
			case BUBBLE -> BUBBLE_SPRITE;
			case NOTE -> NOTE_SPRITE;
			case RUNE -> RUNES[random.nextInt(RUNES.length)];
			default -> GLOW;
		};
	}
}
