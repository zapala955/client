package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Photo Mode: your own character is drawn like in third person – unless Hide Player is on, or the
 * camera is still inside your head (its first moments out of your eyes).
 */
@Mixin(LevelExtractor.class)
public abstract class PhotoPlayerMixin {
	@ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
	private boolean liquid$photoHidesPlayer(boolean detached) {
		return detached && !PhotoMode.hidesPlayer();
	}
}
