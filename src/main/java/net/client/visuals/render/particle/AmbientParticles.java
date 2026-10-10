package net.client.visuals.render.particle;

import net.client.visuals.hud.HudTheme;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.ParticlesModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * Drives {@link ParticlesModule}: keeps the requested number of {@link AmbientParticle}s alive around
 * the player and publishes the module's settings for them once per tick, so particles never look a
 * setting up themselves.
 *
 * <p>Particles are ordinary client particles added straight to vanilla's particle engine – no
 * registry entries (vanilla servers stay compatible), culling, ticking and drawing stay vanilla's.
 * The live count comes from the particles themselves: each one that survives its tick counts
 * itself, so the engine clearing them (world change, resource reload) or its own cap is never out of
 * sync with ours.</p>
 *
 * <p>Enabling, disabling and the "Show" condition fade the whole system in and out ({@link #presence});
 * restyling fades the old particles out while their replacements fade in. Client thread only (the
 * render thread in 26.x), between frames.</p>
 */
public final class AmbientParticles {
	/** Fade speed of the whole system, 1/s: ~0.6 s in or out. */
	private static final float PRESENCE_SPEED = 5f;
	private static final float TICK_SECONDS = 0.05f;
	private static final double TAU = Math.PI * 2.0;
	/** Blocks per tick at 100 % wind (≈ 2.4 blocks/s). */
	private static final float MAX_WIND = 0.12f;
	private static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
	private static final RandomSource RANDOM = RandomSource.create();

	// ---- Published for AmbientParticle (refreshed every tick) -----------------------------------
	static int generation;
	/** Ticks the particle engine has run (chroma's clock). */
	static int clock;
	/** The flow field's clock: advances with the Speed setting, so dragging it never jumps the field. */
	static float flowTime;
	static float presence;
	static float presenceO;
	static boolean customShaders;
	static boolean additive;
	static int glowLevel;
	static float sizeScale = 1f;
	static float opacity = 1f;
	static float twinkleScale = 1f;
	static float spinScale = 1f;
	static float speedScale = 1f;
	static float lifetimeScale = 1f;
	static float turbulenceScale = 1f;
	static float windX;
	static float windZ;
	static float area = 22f;
	static boolean trails;
	static boolean emissive = true;
	static boolean follow;
	static boolean collide = true;
	static boolean cameraFade = true;
	static int colorMode;
	static float hue;
	static float hue2;
	static float saturation;
	/** HUD accent (0 = the HUD's chroma) and album-art colour. */
	static int themeAccent;
	static int musicAccent;

	/** Particles that survived this tick (counted by themselves) and spawns still queued in the engine. */
	static int ticked;
	private static int pending;
	private static @Nullable HudTheme theme;

	private AmbientParticles() {
	}

	/** Style, shape or motion changed: current particles fade out, new ones fade in. */
	public static void restyle() {
		generation++;
	}

	/** End of every client tick, whether or not the module is on (fade-out keeps running). */
	public static void tick(Minecraft minecraft) {
		ParticlesModule m = ParticlesModule.instance();
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (m == null || level == null || player == null) {
			presence = presenceO = 0f;
			ticked = pending = 0;
			return;
		}
		if (minecraft.isPaused() || !level.tickRateManager().runsNormally()) {
			return; // the particle engine is frozen as well, so nothing was counted
		}
		clock++;
		int alive = ticked + pending;
		ticked = 0;
		pending = 0;

		boolean active = m.isEnabled() && visible(m, level);
		float target = active ? 1f : 0f;
		presenceO = presence;
		presence += (target - presence) * (1f - (float) Math.exp(-PRESENCE_SPEED * TICK_SECONDS));
		if (Math.abs(target - presence) < 0.002f) {
			presence = target;
		}
		if (presence <= 0f && alive == 0) {
			return;
		}
		refresh(m);
		flowTime = (flowTime + speedScale) % 1.0e6f;
		if (!active) {
			return;
		}

		int wanted = Math.round(m.amount.getFloat() * vanillaScale(minecraft, m));
		int missing = wanted - alive;
		if (missing <= 0) {
			return;
		}
		// Fill over ~12 ticks; while mostly empty, fill the whole volume instead of only the edges
		// particles normally enter from (snow from above, embers from the ground).
		int budget = Math.min(missing, Math.max(6, wanted / 12));
		boolean prewarm = alive < wanted / 2;
		TextureAtlas atlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
		ParticleStyle style = m.currentStyle();
		int shape = m.currentShape();
		int motion = m.currentMotion();
		boolean outdoors = m.outdoors.get();
		for (int i = 0; i < budget; i++) {
			if (spawn(minecraft, level, player, atlas, style, shape, motion, prewarm, outdoors)) {
				pending++;
			}
		}
	}

	private static boolean visible(ParticlesModule m, ClientLevel level) {
		return switch (m.show.index()) {
			case ParticlesModule.SHOW_NIGHT -> level.isDarkOutside();
			case ParticlesModule.SHOW_DAY -> level.isBrightOutside();
			case ParticlesModule.SHOW_RAIN -> level.getRainLevel(1f) > 0.2f;
			case ParticlesModule.SHOW_CLEAR -> level.getRainLevel(1f) <= 0.2f;
			default -> true;
		};
	}

	private static float vanillaScale(Minecraft minecraft, ParticlesModule m) {
		if (!m.vanillaSetting.get()) {
			return 1f;
		}
		return switch (minecraft.options.particles().get()) {
			case DECREASED -> 0.5f;
			case MINIMAL -> 0.2f;
			default -> 1f;
		};
	}

	private static void refresh(ParticlesModule m) {
		ShaderSupport.ensureValidated();
		customShaders = ShaderSupport.particles() && !ShaderPacks.inUse();
		int blend = m.blend.index();
		additive = blend == ParticlesModule.BLEND_GLOW || blend != ParticlesModule.BLEND_SOFT && m.currentStyle().additive;
		glowLevel = Math.clamp(Math.round(m.glow.getFloat() * 0.15f), 0, 15);
		sizeScale = m.size.getFloat() / 100f;
		opacity = m.opacity.getFloat() / 100f;
		twinkleScale = m.twinkle.getFloat() / 100f;
		spinScale = m.spin.getFloat() / 100f;
		speedScale = m.speed.getFloat() / 100f;
		lifetimeScale = m.lifetime.getFloat() / 100f;
		turbulenceScale = m.turbulence.getFloat() / 100f;
		// Compass bearing the particles drift towards: 0° north (-Z), 90° east (+X).
		float wind = m.wind.getFloat() / 100f * MAX_WIND;
		float bearing = m.windDirection.getFloat() * Mth.DEG_TO_RAD;
		windX = Mth.sin(bearing) * wind;
		windZ = -Mth.cos(bearing) * wind;
		area = m.area.getFloat();
		trails = m.trails.get();
		emissive = m.emissive.get();
		follow = m.follow.get();
		collide = m.collide.get();
		cameraFade = m.cameraFade.get();
		colorMode = m.color.index();
		hue = m.hue.getFloat();
		hue2 = m.hue2.getFloat();
		saturation = m.saturation.getFloat() / 100f;

		HudTheme t = theme;
		if (t == null) {
			t = theme = ModuleManager.get(HudTheme.class);
		}
		int custom = ColorUtil.hsv(t.accentHue.getFloat(), 0.82f, 1f);
		themeAccent = switch (t.accentMode.index()) {
			case 1 -> 0;
			case 2 -> AlbumArt.accent(custom);
			default -> custom;
		};
		musicAccent = AlbumArt.accent(themeAccent != 0 ? themeAccent : ColorUtil.hsv(hue, saturation, 1f));
	}

	// ---- Spawning ------------------------------------------------------------------------------

	private static boolean spawn(Minecraft minecraft, ClientLevel level, LocalPlayer player, TextureAtlas atlas,
			ParticleStyle style, int shape, int motion, boolean prewarm, boolean outdoors) {
		double px = player.getX();
		double py = player.getY();
		double pz = player.getZ();
		double eyeY = player.getEyeY();
		double radius = motion == ParticleMotion.METEOR ? area * 1.25 : area;
		for (int attempt = 0; attempt < 3; attempt++) {
			double r = radius * Math.sqrt(RANDOM.nextDouble());
			double a = RANDOM.nextDouble() * TAU;
			double x = px + Math.cos(a) * r;
			double z = pz + Math.sin(a) * r;
			double y = height(level, style, motion, prewarm, x, py, z);
			double dx = x - px;
			double dy = y - eyeY;
			double dz = z - pz;
			if (motion != ParticleMotion.ORBIT && dx * dx + dy * dy + dz * dz < 2.25) {
				continue; // never pop into existence right in front of the camera
			}
			if (y < level.getMinY() || y > level.getMaxY()) {
				continue;
			}
			POS.set(x, y, z);
			if (!level.isLoaded(POS) || solid(level, POS) || outdoors && !level.canSeeSky(POS)) {
				continue;
			}
			AmbientParticle particle = new AmbientParticle(level, x, y, z, atlas.getSprite(ParticleShape.sprite(shape, RANDOM)),
					style, shape, motion, generation, player);
			minecraft.particleEngine.add(particle);
			return true;
		}
		return false;
	}

	/** Spawn height for the motion: falling things start above, rising things on the ground. */
	private static double height(ClientLevel level, ParticleStyle style, int motion, boolean prewarm, double x, double py, double z) {
		double r = RANDOM.nextDouble();
		return switch (motion) {
			case ParticleMotion.FALL, ParticleMotion.FLUTTER -> prewarm
					? py - 2.0 + r * (14.0 + area * 0.25)
					: py + 4.0 + r * (8.0 + area * 0.25);
			case ParticleMotion.RISE -> ground(level, x, py, z) + r * (prewarm ? 5.0 : 1.5);
			case ParticleMotion.METEOR -> py + 14.0 + r * 14.0;
			case ParticleMotion.ORBIT -> py + 1.0;
			default -> style.low
					? ground(level, x, py, z) + 0.3 + r * 3.2
					: py - area * 0.2 + r * area * 0.55;
		};
	}

	/** The terrain surface when it's near the player (outdoors), else the player's feet (caves). */
	private static double ground(ClientLevel level, double x, double py, double z) {
		int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
		return Math.abs(surface - py) < 6.0 ? surface : py;
	}

	static boolean solid(ClientLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
	}

	// ---- Colour (render thread, per particle per frame; allocation-free) -----------------------

	/** Opaque RGB of a particle for the current colour mode; {@code time} in ticks. */
	static int color(ParticleStyle style, int paletteIndex, float seed, float life, float time, double x, double z) {
		return switch (colorMode) {
			case ParticlesModule.COLOR_CUSTOM -> ColorUtil.hsv(hue + (seed - 0.5f) * 24f, saturation, 1f);
			case ParticlesModule.COLOR_GRADIENT -> ColorUtil.hsv(lerpHue(hue, hue2, seed), saturation, 1f);
			case ParticlesModule.COLOR_RAINBOW -> ColorUtil.hsv(seed * 360f, saturation, 1f);
			// A rainbow wave rolling through space.
			case ParticlesModule.COLOR_CHROMA -> ColorUtil.hsv(time * 3f + (float) ((x + z) % 360.0) * 4f + seed * 30f, saturation, 1f);
			case ParticlesModule.COLOR_LIFETIME -> ColorUtil.hsv(lerpHue(hue, hue2, life), saturation, 1f);
			case ParticlesModule.COLOR_THEME -> vary(themeAccent != 0 ? themeAccent : ColorUtil.hsv(time * 2.5f + seed * 40f, 0.7f, 1f), seed);
			case ParticlesModule.COLOR_MUSIC -> vary(musicAccent, seed);
			default -> {
				int c = style.palette[paletteIndex];
				yield style.fadeTo != 0 ? ColorUtil.lerpRgb(c, style.fadeTo, smoothstep(0.15f, 1f, life)) : c;
			}
		};
	}

	/** Lighter variants of one colour, so a single-colour cloud still has depth. */
	private static int vary(int color, float seed) {
		return ColorUtil.lerpRgb(color, 0xFFFFFFFF, seed * 0.3f);
	}

	/** Hue interpolation along the shorter way round the colour wheel. */
	private static float lerpHue(float from, float to, float t) {
		float d = ((to - from) % 360f + 540f) % 360f - 180f;
		return from + d * t;
	}

	static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
