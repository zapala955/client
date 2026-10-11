package net.client.visuals.mixin.visual;

import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.camera.WorldIntro;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World Intro: any key press skips it (the key itself goes through). Photo Mode: its keys (Esc, the shutter, Tab, H, G, F, R, T, 1–6, [ ], − =) are taken before vanilla
 * sees them – Esc leaves Photo Mode instead of opening the pause menu, F2 takes a photo instead of a
 * screenshot. Releases always go through, and movement keys are never taken (they're read as held).
 */
@Mixin(KeyboardHandler.class)
public abstract class PhotoInputMixin {
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void liquid$photoKeys(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (handle != 0L && handle == Minecraft.getInstance().getWindow().handle() && action == 1) {
			WorldIntro.onInput(); // any key skips the intro – and still does what it does
		}
		if (handle != 0L && handle == Minecraft.getInstance().getWindow().handle() && PhotoMode.onKey(event, action)) {
			ci.cancel();
		}
	}
}
