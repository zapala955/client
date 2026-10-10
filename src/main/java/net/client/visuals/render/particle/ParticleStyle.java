package net.client.visuals.render.particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in looks for the Particles module: a shape, a motion, a palette and the timing that make one
 * atmosphere. Shape and motion can be overridden in the module; everything else is the style's.
 *
 * <p>Sizes are the quad's half extent in blocks; speeds are blocks per tick at 100 %. Styles are
 * immutable once the class has initialised; their position in {@link #ALL} is the mode index.</p>
 */
public final class ParticleStyle {
	/** Alpha follows a slow random shimmer. */
	public static final int TWINKLE_SMOOTH = 0;
	/** Fireflies: long dark pauses, soft bright pulses. */
	public static final int TWINKLE_BLINK = 1;
	/** Fire: fast irregular flicker. */
	public static final int TWINKLE_FLICKER = 2;
	/** Ice crystals: rare sharp flashes that also swell the particle. */
	public static final int TWINKLE_GLINT = 3;

	private static final List<ParticleStyle> STYLES = new ArrayList<>();

	public static final ParticleStyle FIREFLIES = style("fireflies").shape(ParticleShape.ORB).motion(ParticleMotion.WANDER).low()
			.size(0.09f, 0.4f).life(160, 120).speed(0.022f).twinkle(TWINKLE_BLINK, 0.95f)
			.palette(0xFFD4FF6A, 0xFFF5E663, 0xFFA8FF7A, 0xFFE8FF8C);
	public static final ParticleStyle SNOW = style("snow").shape(ParticleShape.SNOWFLAKE).motion(ParticleMotion.FLUTTER).soft()
			.size(0.07f, 0.6f).life(280, 80).speed(0.04f).twinkle(TWINKLE_SMOOTH, 0.12f).spin(0.02f, 0.9f).alpha(0.95f)
			.palette(0xFFFFFFFF, 0xFFEEF6FF, 0xFFDDEBFF);
	public static final ParticleStyle SAKURA = style("sakura").shape(ParticleShape.PETAL).motion(ParticleMotion.FLUTTER).soft()
			.size(0.085f, 0.4f).life(260, 80).speed(0.032f).spin(0.07f, 1.2f)
			.palette(0xFFFFC2DA, 0xFFFFADCC, 0xFFFFDDE9, 0xFFFF94BE);
	public static final ParticleStyle AUTUMN = style("autumn").shape(ParticleShape.LEAF).motion(ParticleMotion.FLUTTER).soft()
			.size(0.11f, 0.45f).life(260, 80).speed(0.045f).spin(0.09f, 1.25f)
			.palette(0xFFFF8C3B, 0xFFE4572E, 0xFFFFB23F, 0xFFC2410C, 0xFFB45309);
	public static final ParticleStyle STARDUST = style("stardust").shape(ParticleShape.SPARKLE).motion(ParticleMotion.STILL)
			.size(0.08f, 0.6f).life(120, 100).speed(0.006f).twinkle(TWINKLE_SMOOTH, 1f)
			.palette(0xFFFFFFFF, 0xFFCFE3FF, 0xFFFFF1C2, 0xFFDCCBFF);
	public static final ParticleStyle BOKEH = style("bokeh").shape(ParticleShape.BOKEH).motion(ParticleMotion.FLOAT)
			.size(0.38f, 0.6f).life(240, 120).speed(0.008f).twinkle(TWINKLE_SMOOTH, 0.2f).alpha(0.5f)
			.palette(0xFFFF7EB6, 0xFF7FA8FF, 0xFF8BF1FF, 0xFFC7A0FF, 0xFFFFD68A);
	public static final ParticleStyle EMBERS = style("embers").shape(ParticleShape.ORB).motion(ParticleMotion.RISE)
			.size(0.04f, 0.5f).life(70, 60).speed(0.045f).twinkle(TWINKLE_FLICKER, 0.7f).turbulence(1.6f)
			.palette(0xFFFFD27A, 0xFFFFA43A, 0xFFFF7A1A).fadeTo(0xFFC2200B);
	public static final ParticleStyle WISPS = style("wisps").shape(ParticleShape.ORB).motion(ParticleMotion.FLOAT)
			.size(0.14f, 0.4f).life(220, 80).speed(0.014f).twinkle(TWINKLE_SMOOTH, 0.35f)
			.palette(0xFF7DF9FF, 0xFF9B8CFF, 0xFF6EE7B7);
	public static final ParticleStyle BUBBLES = style("bubbles").shape(ParticleShape.BUBBLE).motion(ParticleMotion.RISE).soft()
			.size(0.1f, 0.6f).life(170, 60).speed(0.028f)
			.palette(0xFFCDEEFF, 0xFFA6DCFF, 0xFFE6F8FF);
	public static final ParticleStyle HEARTS = style("hearts").shape(ParticleShape.HEART).motion(ParticleMotion.RISE)
			.size(0.08f, 0.4f).life(150, 60).speed(0.02f).twinkle(TWINKLE_SMOOTH, 0.2f)
			.palette(0xFFFF4D79, 0xFFFF7AA2, 0xFFFF2D55, 0xFFFFB3C7);
	public static final ParticleStyle ARCANE = style("arcane").shape(ParticleShape.RUNE).motion(ParticleMotion.SPIRAL)
			.size(0.075f, 0.3f).life(120, 60).speed(0.03f).twinkle(TWINKLE_SMOOTH, 0.4f)
			.palette(0xFFC08CFF, 0xFF8B6CFF, 0xFFE3B8FF, 0xFF7DD3FC);
	public static final ParticleStyle MOTES = style("motes").shape(ParticleShape.ORB).motion(ParticleMotion.FLOAT)
			.size(0.03f, 0.5f).life(260, 100).speed(0.004f).twinkle(TWINKLE_SMOOTH, 0.35f).turbulence(0.6f).alpha(0.8f)
			.palette(0xFFFFEBA8, 0xFFFFF6D8, 0xFFFFDA8A);
	public static final ParticleStyle HALO = style("halo").shape(ParticleShape.FLARE).motion(ParticleMotion.ORBIT)
			.size(0.1f, 0.3f).life(160, 60).speed(0.035f).twinkle(TWINKLE_SMOOTH, 0.25f)
			.palette(0xFF64D2FF, 0xFF007AFF, 0xFFAF52DE, 0xFFFF2D55);
	public static final ParticleStyle CONFETTI = style("confetti").shape(ParticleShape.CONFETTI).motion(ParticleMotion.FLUTTER).soft()
			.size(0.06f, 0.4f).life(170, 60).speed(0.055f).spin(0.22f, 1.45f)
			.palette(0xFFFF2D55, 0xFFFF9F0A, 0xFFFFD60A, 0xFF30D158, 0xFF64D2FF, 0xFF007AFF, 0xFFAF52DE);
	public static final ParticleStyle DIAMOND_DUST = style("diamond_dust").shape(ParticleShape.DIAMOND).motion(ParticleMotion.STILL)
			.size(0.05f, 0.5f).life(160, 80).speed(0.006f).twinkle(TWINKLE_GLINT, 1f).spin(0.015f, 0f)
			.palette(0xFFE8FBFF, 0xFFB8ECFF, 0xFFFFFFFF);
	public static final ParticleStyle NOTES = style("notes").shape(ParticleShape.NOTE).motion(ParticleMotion.RISE)
			.size(0.09f, 0.3f).life(120, 40).speed(0.022f).twinkle(TWINKLE_SMOOTH, 0.1f)
			.palette(0xFF6FE36F, 0xFF5BC0FF, 0xFFFF6FD8, 0xFFFFD34D, 0xFFFF7A5C);
	public static final ParticleStyle VOID = style("void").shape(ParticleShape.ORB).motion(ParticleMotion.FLOAT).soft()
			.size(0.18f, 0.5f).life(220, 80).speed(0.01f).twinkle(TWINKLE_SMOOTH, 0.2f).alpha(0.75f)
			.palette(0xFF2E1065, 0xFF4C1D95, 0xFF1E1B4B, 0xFF581C87);
	public static final ParticleStyle SHOOTING_STARS = style("shooting_stars").shape(ParticleShape.SPARKLE).motion(ParticleMotion.METEOR)
			.size(0.07f, 0.3f).life(26, 14).speed(0.55f).streak()
			.palette(0xFFFFFFFF, 0xFFCFE3FF, 0xFFFFE9B0);
	public static final ParticleStyle PLANKTON = style("plankton").shape(ParticleShape.ORB).motion(ParticleMotion.WANDER).low()
			.size(0.035f, 0.5f).life(160, 80).speed(0.008f).twinkle(TWINKLE_SMOOTH, 0.8f)
			.palette(0xFF22D3EE, 0xFF2DD4BF, 0xFF38BDF8);
	public static final ParticleStyle LANTERNS = style("lanterns").shape(ParticleShape.ORB).motion(ParticleMotion.RISE)
			.size(0.2f, 0.35f).life(400, 120).speed(0.012f).twinkle(TWINKLE_FLICKER, 0.15f)
			.palette(0xFFFFB347, 0xFFFFC870, 0xFFFF9636);
	public static final ParticleStyle STARLIGHT = style("starlight").shape(ParticleShape.STAR).motion(ParticleMotion.FLOAT)
			.size(0.07f, 0.4f).life(200, 80).speed(0.008f).twinkle(TWINKLE_SMOOTH, 0.6f).spin(0.01f, 0f)
			.palette(0xFFFFE08A, 0xFFFFF6D6, 0xFFFFC94D);
	public static final ParticleStyle AURORA = style("aurora").shape(ParticleShape.FLARE).motion(ParticleMotion.SWIRL)
			.size(0.08f, 0.5f).life(180, 80).speed(0.03f).twinkle(TWINKLE_SMOOTH, 0.3f)
			.palette(0xFF34D399, 0xFF22D3EE, 0xFFA78BFA, 0xFFF472B6);

	/** Every style, in mode order. */
	public static final List<ParticleStyle> ALL = List.copyOf(STYLES);
	/** Stable ids, in mode order. */
	public static final String[] IDS = ALL.stream().map(s -> s.id).toArray(String[]::new);

	public final String id;
	int shape = ParticleShape.ORB;
	int motion = ParticleMotion.FLOAT;
	boolean additive = true;
	/** Fireflies-like: stay near the player's height instead of filling the whole volume. */
	boolean low;
	float size = 0.08f;
	float sizeVariation = 0.4f;
	int life = 160;
	int lifeVariation = 60;
	float speed = 0.02f;
	int twinkle = TWINKLE_SMOOTH;
	float twinkleAmount;
	/** Roll speed (radians per tick) and 3D tumble amplitude (radians). */
	float spin;
	float tumble;
	float turbulence = 1f;
	float alpha = 1f;
	/** Always drawn with a trail (shooting stars). */
	boolean streak;
	int[] palette = {0xFFFFFFFF};
	/** Colour the palette colour fades to over the particle's life, 0 for none. */
	int fadeTo;

	private ParticleStyle(String id) {
		this.id = id;
	}

	private static ParticleStyle style(String id) {
		ParticleStyle style = new ParticleStyle(id);
		STYLES.add(style);
		return style;
	}

	public static ParticleStyle byIndex(int index) {
		return ALL.get(Math.floorMod(index, ALL.size()));
	}

	public int shape() {
		return shape;
	}

	public int motion() {
		return motion;
	}

	private ParticleStyle shape(int shape) {
		this.shape = shape;
		return this;
	}

	private ParticleStyle motion(int motion) {
		this.motion = motion;
		return this;
	}

	private ParticleStyle soft() {
		additive = false;
		return this;
	}

	private ParticleStyle low() {
		low = true;
		return this;
	}

	private ParticleStyle size(float size, float variation) {
		this.size = size;
		this.sizeVariation = variation;
		return this;
	}

	private ParticleStyle life(int ticks, int variation) {
		this.life = ticks;
		this.lifeVariation = variation;
		return this;
	}

	private ParticleStyle speed(float speed) {
		this.speed = speed;
		return this;
	}

	private ParticleStyle twinkle(int kind, float amount) {
		this.twinkle = kind;
		this.twinkleAmount = amount;
		return this;
	}

	private ParticleStyle spin(float spin, float tumble) {
		this.spin = spin;
		this.tumble = tumble;
		return this;
	}

	private ParticleStyle turbulence(float turbulence) {
		this.turbulence = turbulence;
		return this;
	}

	private ParticleStyle alpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	private ParticleStyle streak() {
		streak = true;
		return this;
	}

	private ParticleStyle palette(int... colors) {
		this.palette = colors;
		return this;
	}

	private ParticleStyle fadeTo(int color) {
		this.fadeTo = color;
		return this;
	}
}
