package net.client.visuals.mixin.visual;

import net.client.visuals.render.hand.HandChams;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hand Chams: the first-person arm's sleeve layer follows the module's setting ({@code renderHand}
 * draws only the first-person arm). The arm itself is swapped like everything else submitted
 * inside the first-person context ({@code ChamsSubmitMixin}).
 */
@Mixin(AvatarRenderer.class)
public abstract class HandChamsMixin {
	@ModifyVariable(method = "renderHand", at = @At("HEAD"), argsOnly = true)
	private boolean liquid$sleeves(boolean hasSleeve) {
		return HandChams.capturing() ? HandChams.sleeves(hasSleeve) : hasSleeve;
	}
}
