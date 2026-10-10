package net.client.visuals.render.post;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.WetWorldModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

/**
 * How soaked the world is – the state behind {@code WetWorldModule}, drawn by {@link PostFx}.
 *
 * <p>While it rains (the client's rain level, so Ambience's weather counts), surfaces get wet within
 * ~25 s and puddles fill over ~70 s, faster in heavy rain. When it stops, both dry over the module's
 * drying time (puddles a little slower). Where it rains or snows, and what's under cover, is per
 * block column in the shader.</p>
 */
public final class WetWorld {
	private static final float TICK = 0.05f;

	private static float wet;
	private static float puddles;
	private static float rain;
	private static @Nullable WetWorldModule module;

	private WetWorld() {
	}

	static WetWorldModule module() {
		WetWorldModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(WetWorldModule.class);
		}
		return m;
	}

	/** Client tick. */
	public static void tick(Minecraft minecraft) {
		WetWorldModule m = module();
		ClientLevel level = minecraft.level;
		if (level == null) {
			wet = 0f;
			puddles = 0f;
			rain = 0f;
			return;
		}
		rain = level.getRainLevel(1f);
		if (m.alwaysWet.get()) {
			wet = 1f;
			puddles = 1f;
			return;
		}
		if (rain > 0.2f) {
			wet = Math.min(1f, wet + TICK / 25f * (0.5f + rain));
			puddles = Math.min(1f, puddles + TICK / 70f * rain * rain * 1.5f);
		} else {
			float drying = m.dryingTime.getFloat();
			wet = Math.max(0f, wet - TICK / drying);
			puddles = Math.max(0f, puddles - TICK / (drying * 1.25f));
		}
	}

	/** Whether anything is wet enough to draw (and the weather columns are needed). */
	public static boolean visible() {
		return module().isEnabled() && wet > 0.003f;
	}

	static float wet() {
		return wet;
	}

	static float puddles() {
		return puddles;
	}

	/** Rain falling now, for ripples in the puddles (0 without the setting). */
	static float ripples() {
		return module().ripples.get() ? Math.clamp((rain - 0.15f) / 0.6f, 0f, 1f) : 0f;
	}

	/** World left: a dry world next time. */
	public static void reset() {
		wet = 0f;
		puddles = 0f;
		rain = 0f;
	}
}
