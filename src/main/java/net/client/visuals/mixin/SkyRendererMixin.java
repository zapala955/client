package net.client.visuals.mixin;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.client.visuals.render.sky.LiquidSky;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sky hook ("WorldRendererMixin" in Yarn terms).
 *
 * <p>In 26.3, {@code LevelRenderer#addSkyPass} schedules a frame-graph pass whose body is
 * {@code skyRenderer.render(skyFog, state)}. Hooking that method (instead of the pass lambda in
 * LevelRenderer, which has a compiler-generated name) is the stable point that sees exactly one call
 * per frame, inside the sky pass, with the sky fog uniform and the extracted sky state.</p>
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void liquid$replaceSky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		if (LiquidSky.beforeVanilla(skyFog, state)) {
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void liquid$crossFadeSky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		LiquidSky.afterVanilla(state);
	}
}
