package net.client.visuals.mixin;

import net.client.visuals.module.render.CustomFogModule;
import net.client.visuals.render.sky.LiquidSky;
import net.client.visuals.render.storm.StormSky;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Terrain fog follows the Skybox: after vanilla computes the fog colour, {@link LiquidSky} pulls it
 * towards the active sky mode's horizon colour, then a storm ({@link StormSky}) towards its grey and
 * its flashes. Custom Fog then reshapes the finished fog (distance, edge fade, tint), and heavy storm
 * rain closes the distance in. {@code require = 0}: if another mod reshapes these methods the features quietly
 * switch off instead of preventing the game from starting.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	@Inject(method = "computeFogColor", at = @At("TAIL"), require = 0)
	private void liquid$skyFogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance,
			float darkenWorldAmount, Vector4f dest, CallbackInfo ci) {
		LiquidSky.adjustFog(camera, level, dest);
		StormSky.adjustFogColor(camera, dest);
	}

	@Inject(method = "setupFog", at = @At("RETURN"), require = 0)
	private void liquid$customFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
			ClientLevel level, CallbackInfoReturnable<FogData> cir) {
		CustomFogModule.apply(cir.getReturnValue(), camera);
		StormSky.adjustFog(cir.getReturnValue(), camera);
	}
}
