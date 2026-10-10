package net.client.visuals.mixin.sodium;

import net.client.visuals.render.chunk.ChunkAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Sodium doesn't stamp a fade time on sections within 16√3 blocks of the player; with the
 * "Animate nearby chunks" setting those get one too, so they animate as well.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager", remap = false)
public abstract class SodiumRenderRegionManagerMixin {
	@ModifyConstant(method = "uploadResults", constant = @Constant(doubleValue = 768.0), require = 0, remap = false)
	private double liquid$nearbySections(double distanceSq) {
		return ChunkAnimator.sodiumNearDistanceSq(distanceSq);
	}
}
