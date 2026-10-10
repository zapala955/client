package net.client.visuals.mixin.visual;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.weather.LiquidWeather;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ambience's Liquid Weather in vanilla's weather slot. While it's in charge, vanilla's column
 * extraction is skipped (its lists stay empty, so its own prepare and draw do nothing) and ours is
 * drawn at the start of vanilla's draw – inside the translucent pass, or in each OIT stage.
 */
@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void liquid$extract(ClientLevel level, float partialTicks, Vec3 cameraPos, WeatherRenderState state, CallbackInfo ci) {
		if (LiquidWeather.extract(level, partialTicks, cameraPos, state)) {
			ci.cancel();
		}
	}

	@Inject(method = "prepare", at = @At("HEAD"))
	private void liquid$prepare(Vec3 cameraPos, WeatherRenderState state, CallbackInfo ci) {
		LiquidWeather.prepare(cameraPos);
	}

	@Inject(method = "render(Lnet/minecraft/client/renderer/state/level/WeatherRenderState;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
			at = @At("HEAD"))
	private void liquid$render(WeatherRenderState state, RenderPass pass, CallbackInfo ci) {
		LiquidWeather.render(pass, LiquidPipelines.WEATHER);
	}

	@Inject(method = "renderOit", at = @At("HEAD"))
	private void liquid$renderOit(OitStage stage, WeatherRenderState state, RenderPass pass, CallbackInfo ci) {
		LiquidWeather.render(pass, LiquidPipelines.OIT_WEATHER.getPipeline(stage));
	}
}
