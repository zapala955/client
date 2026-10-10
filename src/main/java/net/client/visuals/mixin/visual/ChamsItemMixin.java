package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.client.visuals.render.hand.HandChams;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hand Chams: item quads take their render type from their material when they're built; for a
 * submission marked by Hand Chams, that type is swapped for the chams one around the same texture
 * (glint variants too – the chams look replaces the glint).
 */
@Mixin(ItemFeatureRenderer.class)
public abstract class ChamsItemMixin {
	private static final String MATERIAL = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;";
	private static final String RETURNS = "()Lnet/minecraft/client/renderer/rendertype/RenderType;";

	@WrapOperation(method = "prepareMainSubmit", at = @At(value = "INVOKE", target = MATERIAL + "itemRenderType" + RETURNS))
	private RenderType liquid$chams(BakedQuad.MaterialInfo material, Operation<RenderType> original,
			@Local(argsOnly = true) ItemFeatureRenderer.Submit submit) {
		return HandChams.swapItem(original.call(material), submit.lightCoords());
	}

	@WrapOperation(method = "prepareMainSubmit", at = @At(value = "INVOKE", target = MATERIAL + "itemGlintRenderType" + RETURNS))
	private RenderType liquid$chamsGlint(BakedQuad.MaterialInfo material, Operation<RenderType> original,
			@Local(argsOnly = true) ItemFeatureRenderer.Submit submit) {
		return HandChams.swapItem(original.call(material), submit.lightCoords());
	}

	@WrapOperation(method = "prepareMainSubmit", at = @At(value = "INVOKE", target = MATERIAL + "itemGlintSpecialRenderType" + RETURNS))
	private RenderType liquid$chamsGlintSpecial(BakedQuad.MaterialInfo material, Operation<RenderType> original,
			@Local(argsOnly = true) ItemFeatureRenderer.Submit submit) {
		return HandChams.swapItem(original.call(material), submit.lightCoords());
	}
}
