package net.client.visuals.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import net.client.visuals.render.water.WaterTag;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Liquid Water on Sodium: marks the meshing thread while it renders a water block's fluid, so the
 * quads it pushes get the water material bit ({@code SodiumMeshBufferMixin}). One renderer per
 * meshing thread; the mark is thread-local. Skipped when Sodium isn't installed or its internals
 * changed – water then stays vanilla's.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer", remap = false)
public abstract class SodiumFluidRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), require = 0, remap = false)
	private void liquid$waterBegin(CallbackInfo ci, @Local(argsOnly = true) FluidState fluid) {
		WaterTag.beginFluid(fluid);
	}

	@Inject(method = "render", at = @At("RETURN"), require = 0, remap = false)
	private void liquid$waterEnd(CallbackInfo ci) {
		WaterTag.endFluid();
	}
}
