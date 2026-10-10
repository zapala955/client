package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.module.render.RemovalsModule;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removals: the hurt camera shake and the nausea / nether-portal projection warp. */
@Mixin(GameRenderer.class)
public abstract class GameRendererEffectsMixin {
	@Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
	private void liquid$noHurtCam(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (RemovalsModule.hurtCamera()) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;portalEffectIntensity:F"))
	private float liquid$noPortalWarp(float original) {
		return RemovalsModule.nauseaEffects() ? 0f : original;
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;nauseaEffectIntensity:F"))
	private float liquid$noNauseaWarp(float original) {
		return RemovalsModule.nauseaEffects() ? 0f : original;
	}
}
