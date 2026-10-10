package net.client.visuals.mixin.visual;

import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Photo Mode: mouse buttons in game belong to the camera (left focuses, right takes the photo, middle
 * levels it) – nothing is attacked, used or picked while it's open.
 */
@Mixin(MouseHandler.class)
public abstract class PhotoMouseMixin {
	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void liquid$photoButtons(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
		if (handle != 0L && handle == Minecraft.getInstance().getWindow().handle() && PhotoMode.onMouseButton(rawButtonInfo.button(), action == 1)) {
			ci.cancel();
		}
	}
}
