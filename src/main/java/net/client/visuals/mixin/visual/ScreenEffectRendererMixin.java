package net.client.visuals.mixin.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.module.render.RemovalsModule;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removals: first-person screen effects. "Low fire" lowers the burning overlay out of the centre of
 * view (the collector copies the pose at submit time, so the push / pop around it is safe).
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	@Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
	private static void liquid$fireStart(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		if (RemovalsModule.fireHidden()) {
			ci.cancel();
		} else if (RemovalsModule.fireLow()) {
			poseStack.pushPose();
			poseStack.translate(0f, -0.28f, 0f);
		}
	}

	@Inject(method = "submitFire", at = @At("RETURN"))
	private static void liquid$fireEnd(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		if (RemovalsModule.fireLow()) {
			poseStack.popPose();
		}
	}

	@Inject(method = "submitWater", at = @At("HEAD"), cancellable = true)
	private static void liquid$noWater(PlayerRenderState.WaterOverlay overlay, PoseStack poseStack, SubmitNodeCollector collector,
			CallbackInfo ci) {
		if (RemovalsModule.waterOverlay()) {
			ci.cancel();
		}
	}

	@Inject(method = "submitBlockSprite", at = @At("HEAD"), cancellable = true)
	private static void liquid$noInWall(Identifier atlas, float u0, float v0, float u1, float v1, PoseStack poseStack,
			SubmitNodeCollector collector, int color, CallbackInfo ci) {
		if (RemovalsModule.inWallOverlay()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), cancellable = true)
	private void liquid$noTotem(PlayerRenderState state, PoseStack poseStack, float partialTicks, SubmitNodeCollector collector,
			CallbackInfo ci) {
		if (RemovalsModule.totemAnimation()) {
			ci.cancel();
		}
	}
}
