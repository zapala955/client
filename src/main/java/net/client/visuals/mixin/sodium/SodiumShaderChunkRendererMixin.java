package net.client.visuals.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.water.WaterTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Liquid Water on Sodium: the translucent terrain pipeline – the one Sodium gives an alpha cutoff of
 * 0.01 – also gets the {@code LIQUID_WATER} define, which turns on the water tag in its (patched)
 * shaders. Only {@code createShader}: the improved-transparency pipelines ({@code createOITShader})
 * stay as they are, so with OIT water is vanilla's. Only when both of Sodium's terrain shaders were
 * patched (the fragment stage must never read varyings the vertex stage doesn't write). Skipped when
 * Sodium isn't installed.
 *
 * <p>Never at the cost of the terrain: the tagged pipeline is compiled right away (Sodium compiles it
 * at this point of the frame anyway), and if the driver rejects it, Sodium gets its stock pipeline
 * built from the same builder before the define – water stays vanilla's instead of the game crashing
 * on a missing pipeline.</p>
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer", remap = false)
public abstract class SodiumShaderChunkRendererMixin {
	/** The pipeline being created is the translucent one (render thread, within one createShader). */
	@Unique
	private static boolean liquid$translucent;
	@Unique
	private static boolean liquid$warned;

	@WrapOperation(method = "createShader", at = @At(value = "INVOKE", remap = false,
			target = "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;withShaderDefine(Ljava/lang/String;F)Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;"),
			require = 0, remap = false)
	private RenderPipeline.Builder liquid$waterDefine(RenderPipeline.Builder builder, String name, float value, Operation<RenderPipeline.Builder> original) {
		if ("ALPHA_CUTOUT".equals(name) && value < 0.05f) {
			liquid$translucent = true;
		}
		return original.call(builder, name, value);
	}

	@WrapOperation(method = "createShader", at = @At(value = "INVOKE", remap = false,
			target = "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;build()Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"),
			require = 0, remap = false)
	private RenderPipeline liquid$waterPipeline(RenderPipeline.Builder builder, Operation<RenderPipeline> original) {
		boolean translucent = liquid$translucent;
		liquid$translucent = false;
		RenderPipeline stock = original.call(builder);
		if (!translucent || !WaterTag.sodiumShadersPatched()) {
			return stock;
		}
		try {
			RenderPipeline tagged = original.call(builder.withShaderDefine(WaterTag.DEFINE));
			CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(tagged);
			if (compiled != null && !compiled.isClosed()) {
				WaterTag.sodiumPipelineDefined();
				return tagged;
			}
			liquid$warn(null);
		} catch (RuntimeException e) {
			liquid$warn(e);
		}
		return stock;
	}

	@Unique
	private static void liquid$warn(RuntimeException e) {
		if (!liquid$warned) {
			liquid$warned = true;
			LiquidClient.LOGGER.warn("Liquid Water: Sodium's translucent terrain pipeline didn't compile with the water tag - "
					+ "using Sodium's own, water stays vanilla's", e);
		}
	}
}
