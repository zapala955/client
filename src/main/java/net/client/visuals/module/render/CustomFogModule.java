package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

/**
 * Custom Fog: how far the atmosphere fades out, whether distant chunks fade into the sky, and an
 * optional colour tint – applied to the fog vanilla computes each frame ({@code FogRendererMixin},
 * after the Skybox's horizon sync, so the tint wins). Fog inside water, lava or powder snow and the
 * fog of the Blindness / Darkness effects is left alone.
 */
public final class CustomFogModule extends Module {
	private static CustomFogModule instance;

	public final NumberSetting distance = add(new NumberSetting("distance", "liquidclient.fog.distance", 25, 400, 5, 150)
			.suffix("%"));
	public final BooleanSetting edgeFade = add(new BooleanSetting("edge_fade", "liquidclient.fog.edge_fade", true));
	public final BooleanSetting tint = add(new BooleanSetting("tint", "liquidclient.fog.tint", false));
	public final NumberSetting hue = add(new NumberSetting("hue", "liquidclient.fog.hue", 0, 360, 1, 265)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting strength = add(new NumberSetting("strength", "liquidclient.fog.strength", 0, 100, 1, 55)
			.suffix("%"));

	public CustomFogModule() {
		super("custom_fog", Category.RENDER, 0xE214);
		instance = this;
	}

	/** Adjusts vanilla's fog for this frame in place (render thread). */
	public static FogData apply(FogData fog, Camera camera) {
		CustomFogModule m = instance;
		if (m == null || !m.isEnabled() || camera.getFluidInCamera() != FogType.NONE) {
			return fog;
		}
		Entity entity = camera.entity();
		if (entity instanceof LivingEntity living && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
			return fog;
		}
		float scale = (float) m.distance.get() / 100f;
		fog.environmentalStart *= scale;
		fog.environmentalEnd *= scale;
		if (!m.edgeFade.get()) {
			fog.renderDistanceStart = fog.renderDistanceEnd;
		}
		if (m.tint.get()) {
			int target = ColorUtil.hsv((float) m.hue.get(), 0.55f, 0.85f);
			float t = (float) m.strength.get() / 100f;
			fog.color.x += (((target >> 16) & 0xFF) / 255f - fog.color.x) * t;
			fog.color.y += (((target >> 8) & 0xFF) / 255f - fog.color.y) * t;
			fog.color.z += ((target & 0xFF) / 255f - fog.color.z) * t;
		}
		return fog;
	}
}
