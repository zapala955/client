package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.client.visuals.render.camera.Showcase;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Showcase: the HUD steps aside while the camera is out (every read of the hidden flag in
 * {@code extractRenderState} – which also hides the 3D crosshair), and the showcase's letterbox bars
 * and name card are drawn after it.
 */
@Mixin(Hud.class)
public abstract class HudShowcaseMixin {
	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;isHidden:Z"))
	private boolean liquid$showcaseHidesHud(boolean hidden) {
		return hidden || Showcase.hudHidden();
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void liquid$showcaseOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Showcase.extractOverlay(graphics);
	}
}
