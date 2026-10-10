package net.client.visuals.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.client.visuals.render.chunk.FoliageWaving;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Waving Plants on Sodium: tags each foliage quad while chunks are meshed. The block's sway mode
 * ({@link FoliageWaving#classify}) goes into bits 3–4 of the material byte Sodium writes into every
 * vertex – its own shaders only read bits 0–2. One renderer per meshing thread, so the field is
 * thread-confined. Skipped when Sodium isn't installed or its internals changed.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
public abstract class SodiumBlockRendererMixin {
	@Unique
	private int liquid$wave;

	@Inject(method = "renderModel", at = @At("HEAD"), require = 0, remap = false)
	private void liquid$classify(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		liquid$wave = FoliageWaving.classify(state);
	}

	@ModifyExpressionValue(method = "bufferQuad", at = @At(value = "INVOKE", remap = false,
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;bits()I"), require = 0, remap = false)
	private int liquid$waveBits(int bits) {
		return bits | liquid$wave << 3;
	}
}
