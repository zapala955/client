package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Lighting: recolours the light the world is lit with – silver-blue moonlight instead of vanilla's
 * flat lavender-grey nights, warm (or any colour) torchlight with its own flicker, sunlight warmth,
 * and how dark unlit areas get.
 *
 * <p>Everything happens in the lightmap's render state ({@code LightmapMixin}, once per tick when
 * vanilla rebuilds it), so it's free at draw time and every renderer (vanilla, Sodium, Iris
 * without a pack) picks it up. Vanilla's own inputs (time of day, weather, the End flash, potion
 * effects) are kept and only reshaped.</p>
 */
public final class LightingModule extends Module {
	/** Vanilla's night sky-light factor (day 1.0): night amount is measured against it. */
	private static final float NIGHT_FACTOR = 0.24f;

	private static LightingModule instance;

	public final NumberSetting moonlight = add(new NumberSetting("moonlight", "liquidclient.lighting.moonlight", 0, 100, 1, 60)
			.suffix("%"));
	public final NumberSetting moonHue = add(new NumberSetting("moon_hue", "liquidclient.lighting.moon_hue", 0, 360, 1, 212)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting sunWarmth = add(new NumberSetting("sun_warmth", "liquidclient.lighting.sun_warmth", -100, 100, 1, 15)
			.suffix("%"));
	public final NumberSetting skyLight = add(new NumberSetting("sky_light", "liquidclient.lighting.sky_light", 50, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting torchLight = add(new NumberSetting("torch_light", "liquidclient.lighting.torch_light", 50, 200, 5, 110)
			.suffix("%"));
	public final NumberSetting torchHue = add(new NumberSetting("torch_hue", "liquidclient.lighting.torch_hue", 0, 360, 1, 30)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting torchTint = add(new NumberSetting("torch_tint", "liquidclient.lighting.torch_tint", 0, 100, 1, 55)
			.suffix("%"));
	public final NumberSetting flicker = add(new NumberSetting("flicker", "liquidclient.lighting.flicker", 0, 100, 1, 35)
			.suffix("%"));
	public final NumberSetting shadows = add(new NumberSetting("shadows", "liquidclient.lighting.shadows", 0, 300, 5, 100)
			.suffix("%"));
	public final NumberSetting brightness = add(new NumberSetting("brightness", "liquidclient.lighting.brightness", -50, 50, 1, 0)
			.suffix("%"));

	/** Pre-allocated outputs the render state points at (vanilla reassigns its own each update). */
	private final Vector3f sky = new Vector3f();
	private final Vector3f torch = new Vector3f();
	private final Vector3f ambient = new Vector3f();
	private float flickerPhase;

	public LightingModule() {
		super("lighting", Category.RENDER, 0xE11E);
		instance = this;
	}

	/** After vanilla extracted the lightmap state for this tick (render thread). */
	public static void apply(LightmapRenderState state) {
		LightingModule m = instance;
		if (m != null && m.isEnabled()) {
			m.reshape(state);
		}
	}

	private void reshape(LightmapRenderState state) {
		float night = Math.clamp((1f - state.skyFactor) / (1f - NIGHT_FACTOR), 0f, 1f);

		// Sky: warm or cool the daylight, silver-blue moonlight at night.
		Vector3fc base = state.skyLightColor;
		float r = base.x();
		float g = base.y();
		float b = base.z();
		float warmth = sunWarmth.getFloat() / 100f * (1f - night);
		if (warmth > 0f) {
			g *= 1f - 0.08f * warmth;
			b *= 1f - 0.22f * warmth;
		} else if (warmth < 0f) {
			r *= 1f + 0.18f * warmth;
			g *= 1f + 0.06f * warmth;
		}
		float moon = moonlight.getFloat() / 100f * night;
		int moonColor = ColorUtil.hsv(moonHue.getFloat(), 0.32f, 1f);
		r = Mth.lerp(moon * 0.85f, r, ((moonColor >> 16) & 0xFF) / 255f);
		g = Mth.lerp(moon * 0.85f, g, ((moonColor >> 8) & 0xFF) / 255f);
		b = Mth.lerp(moon * 0.85f, b, (moonColor & 0xFF) / 255f);
		state.skyLightColor = sky.set(r, g, b);
		// Moonlit nights are brighter than vanilla's, but stay clearly night.
		state.skyFactor = state.skyFactor * skyLight.getFloat() / 100f + moon * 0.3f;

		// Torches: tint and strength, with a soft irregular flicker on top of vanilla's.
		int torchColor = ColorUtil.hsv(torchHue.getFloat(), 0.8f, 1f);
		float tint = torchTint.getFloat() / 100f;
		Vector3fc vanillaTint = state.blockLightTint;
		state.blockLightTint = torch.set(
				Mth.lerp(tint, vanillaTint.x(), ((torchColor >> 16) & 0xFF) / 255f),
				Mth.lerp(tint, vanillaTint.y(), ((torchColor >> 8) & 0xFF) / 255f),
				Mth.lerp(tint, vanillaTint.z(), (torchColor & 0xFF) / 255f));
		flickerPhase += 0.37f + 0.21f * Mth.sin(flickerPhase * 0.31f);
		float wobble = Mth.sin(flickerPhase) * 0.6f + Mth.sin(flickerPhase * 2.7f + 1.3f) * 0.4f;
		state.blockFactor *= torchLight.getFloat() / 100f * (1f + wobble * 0.12f * flicker.getFloat() / 100f);

		// Unlit areas: vanilla's ambient floor, scaled, with a faint moon-coloured fill at night.
		float shadow = shadows.getFloat() / 100f;
		Vector3fc vanillaAmbient = state.ambientColor;
		float fill = moon * 0.025f;
		state.ambientColor = ambient.set(
				vanillaAmbient.x() * shadow + fill * ((moonColor >> 16) & 0xFF) / 255f,
				vanillaAmbient.y() * shadow + fill * ((moonColor >> 8) & 0xFF) / 255f,
				vanillaAmbient.z() * shadow + fill * (moonColor & 0xFF) / 255f);

		state.brightness = Math.clamp(state.brightness + brightness.getFloat() / 100f, 0f, 1f);
	}
}
