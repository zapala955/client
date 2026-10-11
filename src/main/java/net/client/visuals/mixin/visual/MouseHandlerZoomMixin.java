package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.camera.Showcase;
import net.client.visuals.render.camera.WorldIntro;
import net.client.visuals.module.utility.ZoomModule;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Zoom: sensitivity that follows the zoom level, the optional cinematic camera, scroll-to-zoom.
 * Showcase: while its camera is out, mouse look orbits the shot and the wheel zooms it.
 * Photo Mode: mouse look turns its camera, the wheel zooms its lens.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerZoomMixin {
	@WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void liquid$zoomSensitivity(LocalPlayer player, double xo, double yo, Operation<Void> original) {
		if (PhotoMode.mouse(xo, yo) || Showcase.mouse(xo, yo)) {
			return; // the Showcase camera orbits instead of the player turning
		}
		double scale = ZoomModule.turnScale();
		original.call(player, xo * scale, yo * scale);
	}

	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	private boolean liquid$zoomCinematic(boolean original) {
		return original || ZoomModule.cinematic();
	}

	@Inject(method = "onScroll", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"), cancellable = true)
	private void liquid$zoomScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		WorldIntro.onInput(); // scrolling skips the intro too
		if (PhotoMode.scroll(yoffset) || Showcase.scroll(yoffset) || ZoomModule.onScroll(yoffset)) {
			ci.cancel();
		}
	}
}
