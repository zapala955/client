package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.client.visuals.render.hand.HandChams;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Hand Chams: inside its submission contexts (see {@code HandChams}), models are submitted with the
 * chams render type around their own texture, and items with their light marked so the item renderer
 * swaps each quad's type. {@code SubmitNodeCollection} is where every submission ends up
 * ({@code SubmitNodeStorage} forwards to it).
 */
@Mixin(SubmitNodeCollection.class)
public abstract class ChamsSubmitMixin {
	@WrapMethod(method = "submitModel")
	private void liquid$chamsModel(Model<?> model, Object state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords,
			int tintedColor, @Nullable UvMapping uvMapping, int outlineColor, Operation<Void> original) {
		if (HandChams.capturing()) {
			RenderType swapped = HandChams.swapModel(renderType);
			if (swapped != renderType) {
				original.call(model, state, poseStack, swapped, HandChams.modelLight(lightCoords, renderType), HandChams.overlay(), tintedColor,
						uvMapping, outlineColor);
				return;
			}
		}
		original.call(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, uvMapping, outlineColor);
	}

	@WrapMethod(method = "submitItem")
	private void liquid$chamsItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor,
			int[] tintLayers, ItemQuads quads, ItemStackRenderState.FoilType foilType, Operation<Void> original) {
		if (HandChams.swapsItems()) {
			original.call(poseStack, displayContext, HandChams.itemLight(lightCoords), HandChams.overlay(), outlineColor, tintLayers, quads, foilType);
		} else {
			original.call(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
		}
	}
}
