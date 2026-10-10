package net.client.visuals.mixin.visual;

import net.client.visuals.module.ModuleManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Module keybinds. Hooked exactly where vanilla fires its own key mappings for gameplay – only with
 * no screen open, after debug-key handling – so a bound key never fires while typing in chat or a
 * text field. Action 1 is a fresh press (key repeat doesn't toggle again).
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	private void liquid$moduleKeybinds(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (action == 1) {
			ModuleManager.onKeyPressed(event.key());
		}
	}
}
