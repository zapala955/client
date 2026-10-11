package net.client.visuals.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.client.visuals.render.water.WaterTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Liquid Water on Sodium: water quads get material bit 5 (Sodium's shaders read bits 0–2, Waving
 * Plants uses 3–4). Fluids push their quads with a {@code Material}, whose bits are read here; the
 * bit is only added while {@code SodiumFluidRendererMixin} has marked this thread as meshing water.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder", remap = false)
public abstract class SodiumMeshBufferMixin {
	@ModifyExpressionValue(method = {"push", "writeExternal"}, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;bits()I"), require = 0, remap = false)
	private int liquid$waterBit(int bits) {
		return bits | WaterTag.fluidBits();
	}
}
