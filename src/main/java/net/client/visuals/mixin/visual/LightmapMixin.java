package net.client.visuals.mixin.visual;

import net.client.visuals.module.render.AmbienceModule;
import net.client.visuals.module.render.LightingModule;
import net.client.visuals.render.storm.StormSky;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lightmap adjustments, applied after vanilla extracts the lightmap state and only on the frames it
 * actually rebuilds it (once per tick): the Lighting module's colours, a storm's cold light and
 * flashes (Storm Chaser), then Ambience fullbright (night-vision lighting at the chosen strength) on top.
 */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void liquid$lighting(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
		if (!renderState.needsUpdate) {
			return;
		}
		LightingModule.apply(renderState);
		StormSky.applyLightmap(renderState);
		float strength = AmbienceModule.fullbright();
		if (strength > 0f) {
			renderState.nightVisionEffectIntensity = Math.max(renderState.nightVisionEffectIntensity, strength);
		}
	}
}
