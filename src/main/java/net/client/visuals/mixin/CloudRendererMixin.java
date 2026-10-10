package net.client.visuals.mixin;

import net.client.visuals.render.sky.LiquidSky;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Hide Vanilla Clouds": skips both cloud paths (regular and order-independent-transparency) while
 * the Skybox is visible. {@code require = 0} keeps the game booting if a renderer mod replaces
 * clouds; the option then has no effect.
 */
@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
	private void liquid$hideClouds(CallbackInfo ci) {
		if (LiquidSky.hideVanillaClouds()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderOit", at = @At("HEAD"), cancellable = true, require = 0)
	private void liquid$hideOitClouds(CallbackInfo ci) {
		if (LiquidSky.hideVanillaClouds()) {
			ci.cancel();
		}
	}
}
