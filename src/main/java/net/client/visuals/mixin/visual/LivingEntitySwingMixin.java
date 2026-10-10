package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.module.render.ViewModelModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * View Model swing speed: the local player's swing animation length. 26.3 has no serverbound swing
 * packet (servers derive swings from attacks / use), so this only changes what you see.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
	@ModifyReturnValue(method = "getModifiedSwingDuration", at = @At("RETURN"))
	private int liquid$swingSpeed(int original) {
		return (Object) this instanceof LocalPlayer ? ViewModelModule.swingDuration(original) : original;
	}
}
