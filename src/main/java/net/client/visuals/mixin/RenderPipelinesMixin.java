package net.client.visuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.water.WaterTag;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Liquid Water on vanilla terrain (no Sodium): vanilla's translucent pipelines are built with the
 * {@code LIQUID_WATER} define, which turns on the water tag in the (patched) terrain shaders. The
 * define means nothing to any other shader, so every pipeline named "translucent" may get it – the
 * terrain one is among them. Each is built once to read its name, then again with the define.
 * {@code require = 0}: if the pipelines are built elsewhere, water simply stays vanilla's.
 */
@Mixin(RenderPipelines.class)
public abstract class RenderPipelinesMixin {
	@WrapOperation(method = "<clinit>", at = @At(value = "INVOKE",
			target = "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;build()Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"),
			require = 0)
	private static RenderPipeline liquid$waterDefine(RenderPipeline.Builder builder, Operation<RenderPipeline> original) {
		RenderPipeline pipeline = original.call(builder);
		if (pipeline.getLocation().getPath().contains("translucent")) {
			builder.withShaderDefine(WaterTag.DEFINE);
			pipeline = original.call(builder);
			WaterTag.vanillaPipelineDefined();
		}
		return pipeline;
	}
}
