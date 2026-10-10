package net.client.visuals.render.post;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.RainbowsModule;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

/**
 * When a rainbow shows – the state behind {@code RainbowsModule}, drawn by {@link PostFx}.
 *
 * <p>A real rain has to have fallen (at least ~15 s of it). As it clears (the client's rain level,
 * so Ambience's weather counts) the bow fades in and stays for the module's duration, fading out over
 * the last third. "During Light Rain" also shows it while rain is light or easing. Where the sun is
 * – the bow needs it low enough to clear the horizon – is decided per frame in {@link PostFx}.</p>
 */
public final class Rainbow {
	private static final float TICK = 0.05f;
	/** Seconds of proper rain before a bow can follow it. */
	private static final float MIN_RAIN = 15f;

	private static final Smooth STRENGTH = new Smooth(0f, 0.45f);
	private static float rainedFor;
	/** Seconds since the rain cleared, or -1 while there's no rainbow to come. */
	private static float since = -1f;
	private static float target;
	private static long lastNanos = -1L;
	private static @Nullable RainbowsModule module;

	private Rainbow() {
	}

	static RainbowsModule module() {
		RainbowsModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(RainbowsModule.class);
		}
		return m;
	}

	/** Client tick. */
	public static void tick(Minecraft minecraft) {
		RainbowsModule m = module();
		ClientLevel level = minecraft.level;
		if (!m.isEnabled() || level == null) {
			target = 0f;
			return;
		}
		float rain = level.getRainLevel(1f);
		if (rain > 0.5f) {
			rainedFor = Math.min(600f, rainedFor + TICK);
			since = -1f;
		} else if (rain < 0.35f && rainedFor >= MIN_RAIN) {
			since = 0f;
			rainedFor = 0f;
		} else if (since >= 0f) {
			since += TICK;
		}
		float duration = m.duration.getFloat();
		float after = 0f;
		if (since >= 0f) {
			if (since > duration) {
				since = -1f;
			} else {
				after = smoothstep(0f, 6f, since) * (1f - smoothstep(duration * 0.66f, duration, since));
			}
		}
		target = switch (m.when.index()) {
			case RainbowsModule.WHEN_ALWAYS -> 1f;
			case RainbowsModule.WHEN_RAIN_AND_AFTER -> Math.max(after, rain > 0.05f && rain < 0.8f ? 0.75f : 0f);
			default -> after;
		};
	}

	/** Render thread, once per frame: 0..1. */
	static float strength() {
		long now = System.nanoTime();
		float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) * 1.0e-9f);
		lastNanos = now;
		return STRENGTH.target(module().isEnabled() ? target : 0f).update(dt);
	}

	/** World left. */
	public static void reset() {
		STRENGTH.snap(0f);
		rainedFor = 0f;
		since = -1f;
		target = 0f;
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
