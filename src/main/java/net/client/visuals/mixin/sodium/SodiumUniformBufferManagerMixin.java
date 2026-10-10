package net.client.visuals.mixin.sodium;

import net.client.visuals.render.chunk.ChunkAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Chunk Animations on Sodium: while active, the global terrain uniform {@code u_FadePeriodInv} carries
 * the packed animation settings (see {@code ChunkAnimator}). Skipped when Sodium isn't installed.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager", remap = false)
public abstract class SodiumUniformBufferManagerMixin {
	@ModifyArg(method = "update", at = @At(value = "INVOKE", remap = false,
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/UniformBufferManager$GlobalUniforms;<init>(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FFFFFFFFFFFFFI)V"),
			index = 14, require = 0, remap = false)
	private float liquid$packChunkAnimation(float fadePeriodInverse) {
		return ChunkAnimator.sodiumFadeUniform(fadePeriodInverse);
	}
}
