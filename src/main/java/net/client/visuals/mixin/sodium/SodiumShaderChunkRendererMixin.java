package net.client.visuals.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.water.WaterTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Liquid Water on Sodium: the translucent terrain pipeline – the one Sodium gives an alpha cutoff of
 * 0.01 – also gets the {@code LIQUID_WATER} define, which turns on the water tag in its (patched)
 * shaders. Only {@code createShader}: the improved-transparency pipelines ({@code createOITShader})
 * stay as they are, so with OIT water is vanilla's. Skipped when Sodium isn't installed.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer", remap = false)
public abstract class SodiumShaderChunkRendererMixin {
	@WrapOperation(method = "createShader", at = @At(value = "INVOKE", remap = false,
			target = "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;withShaderDefine(Ljava/lang/String;F)Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;"),
			require = 0, remap = false)
	private RenderPipeline.Builder liquid$waterDefine(RenderPipeline.Builder builder, String name, float value, Operation<RenderPipeline.Builder> original) {
		RenderPipeline.Builder result = original.call(builder, name, value);
		if ("ALPHA_CUTOUT".equals(name) && value < 0.05f) {
			result.withShaderDefine(WaterTag.DEFINE);
			WaterTag.sodiumPipelineDefined();
		}
		return result;
	}
}
