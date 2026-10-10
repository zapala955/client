package net.client.visuals.mixin.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.render.hand.HandChams;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hand Chams in third person: only what your own player holds – the held-item layer's submission is
 * inside the chams context, the body, armor and cape are not. Other players are never touched.
 */
@Mixin(ItemInHandLayer.class)
public abstract class ChamsHeldItemMixin {
	private static final String SUBMIT = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
			+ "Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V";

	@Inject(method = SUBMIT, at = @At("HEAD"))
	private void liquid$chamsBegin(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ArmedEntityRenderState state,
			float yRot, float xRot, CallbackInfo ci) {
		HandChams.beginThirdPerson(state);
	}

	@Inject(method = SUBMIT, at = @At("RETURN"))
	private void liquid$chamsEnd(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ArmedEntityRenderState state,
			float yRot, float xRot, CallbackInfo ci) {
		HandChams.end(state);
	}
}
