package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.particle.AmbientParticles;
import net.client.visuals.render.particle.ParticleMotion;
import net.client.visuals.render.particle.ParticleShape;
import net.client.visuals.render.particle.ParticleStyle;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Particles: an atmosphere of particles floating in the air around you – fireflies, snow, sakura
 * petals, embers, bokeh lights, runes and more (22 styles), each a shape, a motion and a palette that
 * can be mixed freely, with colour modes, glow, twinkle, wind and turbulence.
 *
 * <p>State only. The particles are spawned and moved by {@code render.particle.AmbientParticles}
 * and drawn through vanilla's particle engine with the client's own distance-field shader
 * ({@code ambient_particle.fsh}). Nothing is sent to the server. Settings that change only how a
 * particle looks (size, colour, glow, opacity, twinkle) apply live to particles already in the air;
 * changing the style, shape or motion fades the old ones out.</p>
 */
public final class ParticlesModule extends Module {
	public static final String[] SHAPES = withAuto(ParticleShape.IDS);
	public static final String[] MOTIONS = withAuto(ParticleMotion.IDS);
	public static final String[] COLORS = {"style", "custom", "gradient", "rainbow", "chroma", "lifetime", "theme", "music"};
	public static final String[] BLENDS = {"auto", "glow", "soft"};
	public static final String[] SHOW = {"always", "night", "day", "rain", "clear"};

	public static final int COLOR_STYLE = 0;
	public static final int COLOR_CUSTOM = 1;
	public static final int COLOR_GRADIENT = 2;
	public static final int COLOR_RAINBOW = 3;
	public static final int COLOR_CHROMA = 4;
	public static final int COLOR_LIFETIME = 5;
	public static final int COLOR_THEME = 6;
	public static final int COLOR_MUSIC = 7;

	public static final int BLEND_GLOW = 1;
	public static final int BLEND_SOFT = 2;

	public static final int SHOW_NIGHT = 1;
	public static final int SHOW_DAY = 2;
	public static final int SHOW_RAIN = 3;
	public static final int SHOW_CLEAR = 4;

	private static final String[] COMPASS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

	private static @Nullable ParticlesModule instance;

	public final ModeSetting style = add(new ModeSetting("style", "liquidclient.particles.style", ParticleStyle.IDS, 0)
			.onChange(AmbientParticles::restyle));
	public final NumberSetting amount = add(new NumberSetting("amount", "liquidclient.particles.amount", 20, 1500, 10, 260));
	public final NumberSetting area = add(new NumberSetting("area", "liquidclient.particles.area", 6, 64, 1, 22)
			.suffix(" blocks"));
	public final NumberSetting size = add(new NumberSetting("size", "liquidclient.particles.size", 25, 400, 5, 100)
			.suffix("%"));
	public final NumberSetting speed = add(new NumberSetting("speed", "liquidclient.particles.speed", 0, 400, 5, 100)
			.suffix("%"));
	public final NumberSetting lifetime = add(new NumberSetting("lifetime", "liquidclient.particles.lifetime", 25, 400, 5, 100)
			.suffix("%"));
	public final ModeSetting shape = add(new ModeSetting("shape", "liquidclient.particles.shape", SHAPES, 0)
			.onChange(AmbientParticles::restyle));
	public final ModeSetting motion = add(new ModeSetting("motion", "liquidclient.particles.motion", MOTIONS, 0)
			.onChange(AmbientParticles::restyle));
	public final ModeSetting color = add(new ModeSetting("color", "liquidclient.particles.color", COLORS, COLOR_STYLE));
	public final NumberSetting hue = add(new NumberSetting("hue", "liquidclient.particles.hue", 0, 360, 1, 200)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting hue2 = add(new NumberSetting("hue2", "liquidclient.particles.hue2", 0, 360, 1, 300)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting saturation = add(new NumberSetting("saturation", "liquidclient.particles.saturation", 0, 100, 1, 70)
			.suffix("%"));
	public final NumberSetting opacity = add(new NumberSetting("opacity", "liquidclient.particles.opacity", 5, 100, 1, 90)
			.suffix("%"));
	public final NumberSetting glow = add(new NumberSetting("glow", "liquidclient.particles.glow", 0, 100, 1, 60)
			.suffix("%"));
	public final NumberSetting twinkle = add(new NumberSetting("twinkle", "liquidclient.particles.twinkle", 0, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting spin = add(new NumberSetting("spin", "liquidclient.particles.spin", 0, 300, 5, 100)
			.suffix("%"));
	public final NumberSetting turbulence = add(new NumberSetting("turbulence", "liquidclient.particles.turbulence", 0, 300, 5, 100)
			.suffix("%"));
	public final NumberSetting wind = add(new NumberSetting("wind", "liquidclient.particles.wind", 0, 100, 1, 0)
			.suffix("%"));
	public final NumberSetting windDirection = add(new NumberSetting("wind_direction", "liquidclient.particles.wind_direction", 0, 360, 5, 45)
			.format(ParticlesModule::compass));
	public final ModeSetting blend = add(new ModeSetting("blend", "liquidclient.particles.blend", BLENDS, 0));
	public final ModeSetting show = add(new ModeSetting("show", "liquidclient.particles.show", SHOW, 0));
	public final BooleanSetting trails = add(new BooleanSetting("trails", "liquidclient.particles.trails", false));
	public final BooleanSetting emissive = add(new BooleanSetting("emissive", "liquidclient.particles.emissive", true));
	public final BooleanSetting follow = add(new BooleanSetting("follow", "liquidclient.particles.follow", false));
	public final BooleanSetting collide = add(new BooleanSetting("collide", "liquidclient.particles.collide", true));
	public final BooleanSetting outdoors = add(new BooleanSetting("outdoors", "liquidclient.particles.outdoors", false));
	public final BooleanSetting cameraFade = add(new BooleanSetting("camera_fade", "liquidclient.particles.camera_fade", true));
	public final BooleanSetting vanillaSetting = add(new BooleanSetting("vanilla_setting", "liquidclient.particles.vanilla_setting", true));

	public ParticlesModule() {
		super("particles", Category.RENDER, 0xE412);
		instance = this;
	}

	public static @Nullable ParticlesModule instance() {
		return instance;
	}

	public ParticleStyle currentStyle() {
		return ParticleStyle.byIndex(style.index());
	}

	/** Effective shape: the override, or the style's. */
	public int currentShape() {
		int i = shape.index();
		return i == 0 ? currentStyle().shape() : i - 1;
	}

	/** Effective motion: the override, or the style's. */
	public int currentMotion() {
		int i = motion.index();
		return i == 0 ? currentStyle().motion() : i - 1;
	}

	private static String compass(double degrees) {
		int d = (int) Math.round(degrees) % 360;
		return String.format(Locale.ROOT, "%d° %s", d, COMPASS[Math.floorMod(Math.round(d / 45f), COMPASS.length)]);
	}

	private static String[] withAuto(String[] ids) {
		String[] all = new String[ids.length + 1];
		all[0] = "auto";
		System.arraycopy(ids, 0, all, 1, ids.length);
		return all;
	}
}
