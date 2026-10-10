package net.client.visuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.client.visuals.render.chunk.ChunkAnimator;
import net.client.visuals.render.post.PostFx;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <ul>
 *   <li>Vanilla terrain path of Chunk Animations: the per-section visibility written into the
 *       section uniforms carries the animation state while a section animates. With Sodium this
 *       method never runs.</li>
 *   <li>Wet World: drawn over the opaque world at the start of the translucent step, in a pass of
 *       its own ({@code PostFx#beforeTranslucents} closes vanilla's and reopens it).</li>
 *   <li>Atmosphere: hazes the world right before particles are drawn (classic transparency: after
 *       water, before particles, clouds and weather – vanilla submits translucent particles to the
 *       "after terrain" phase), so particles – which write no depth – aren't fogged as if they were as
 *       far away as the terrain behind them. That all happens in one render pass, which
 *       {@code PostFx#splitMainPass} closes and reopens around the haze. Improved transparency (OIT)
 *       has no pass open before its translucent stages. All optional: without them the haze falls
 *       back to one pass after the world.</li>
 * </ul>
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@WrapOperation(method = "extractSectionDrawGroups", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;getVisibility(JJ)F"), require = 0)
	private float liquid$chunkAnimation(SectionRenderDispatcher.RenderSection section, long now, long fadeDuration, Operation<Float> original) {
		return ChunkAnimator.vanillaVisibility(section, now, original.call(section, now, fadeDuration));
	}

	/** Start of the translucent step: the opaque world is complete, nothing translucent drawn yet. */
	@ModifyVariable(method = "executeClassicTransparency", at = @At("HEAD"), argsOnly = true, require = 0)
	private RenderPass liquid$wetBeforeTranslucents(RenderPass renderPass) {
		return PostFx.beforeTranslucents(renderPass);
	}

	/**
	 * Right after water and the other translucent terrain, before particles, clouds and weather: the
	 * profiler {@code pop()} that closes the "translucentTerrain" section (the method's second pop).
	 * It must come before the pass is next loaded as an argument – a variable swap at the
	 * {@code executeTranslucentAfterTerrain} call itself would happen after its argument was already
	 * on the stack, handing it the closed pass.
	 */
	@ModifyVariable(method = "executeClassicTransparency", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 1),
			argsOnly = true, require = 0)
	private RenderPass liquid$hazeBeforeParticles(RenderPass renderPass) {
		return PostFx.splitMainPass(renderPass);
	}

	@Inject(method = "executeClassicTransparency", at = @At("RETURN"), require = 0)
	private void liquid$closeSplitPass(CallbackInfo ci) {
		PostFx.endSplitPass();
	}

	@Inject(method = "executeOit", at = @At("HEAD"), require = 0)
	private void liquid$hazeBeforeOit(CallbackInfo ci) {
		PostFx.beforeOit();
	}
}
