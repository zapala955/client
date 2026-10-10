package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Photo Mode: the camera's roll and the shutter's sub-pixel jitter go into the level projection right
 * after vanilla multiplies in view bobbing – before anything (vanilla, Sodium, our post effects)
 * reads or uploads it, so the whole world turns and shifts as one. No view bobbing while it's open.
 */
@Mixin(GameRenderer.class)
public abstract class PhotoProjectionMixin {
	@Shadow
	public abstract RenderTarget mainRenderTarget();

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;",
			ordinal = 0, shift = At.Shift.AFTER))
	private void liquid$photoProjection(CallbackInfo ci, @Local Matrix4f projectionMatrix) {
		RenderTarget target = mainRenderTarget();
		PhotoMode.modifyProjection(projectionMatrix, target.width, target.height);
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void liquid$photoNoBob(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (PhotoMode.active()) {
			ci.cancel();
		}
	}
}
