package net.client.visuals.mixin.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.module.render.ViewModelModule;
import net.client.visuals.render.hand.HandChams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * View Model: first-person hand offsets, held-item scale and the re-equip dip. Hand Chams: everything
 * submitted for the first-person hands and items is inside its context.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsMixin {
	private static final String SUBMIT_ARM = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitArmWithItem("
			+ "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;"
			+ "Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;"
			+ "FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;F"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V";

	/** Right after vanilla pushes this hand's pose: moves arm, map or item alike. */
	@Inject(method = "submitArmWithItem", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
	private void liquid$handOffset(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot,
			InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		AvatarRenderState avatar = playerState.avatarRenderState;
		if (avatar == null) {
			return;
		}
		HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? avatar.mainArm : avatar.mainArm.getOpposite();
		ViewModelModule.offset(poseStack, hand, arm == HumanoidArm.RIGHT ? 1 : -1);
	}

	/** After vanilla positions a held item: scale it in place. */
	@Inject(method = "applyItemArmTransform", at = @At("TAIL"))
	private void liquid$itemScale(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		ViewModelModule.scale(poseStack, player == null || arm == player.getMainArm());
	}

	@Inject(method = "submitHandsWithItems", at = @At("HEAD"))
	private void liquid$chamsBegin(CallbackInfo ci) {
		HandChams.beginFirstPerson();
	}

	@Inject(method = "submitHandsWithItems", at = @At("RETURN"))
	private void liquid$chamsEnd(CallbackInfo ci) {
		HandChams.endFirstPerson();
	}

	@ModifyArg(method = "submitHandsWithItems", at = @At(value = "INVOKE", target = SUBMIT_ARM), index = 7)
	private float liquid$noEquipDip(float inverseArmHeight) {
		return ViewModelModule.equipHeight(inverseArmHeight);
	}
}
