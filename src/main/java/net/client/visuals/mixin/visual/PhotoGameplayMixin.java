package net.client.visuals.mixin.visual;

import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Photo Mode: no gameplay key mappings while the camera is out (inventory, drop, hotbar, chat,
 * perspective…). Their clicks are drained every tick, so nothing fires late when Photo Mode closes.
 */
@Mixin(Minecraft.class)
public abstract class PhotoGameplayMixin {
	@Shadow
	@Final
	public Options options;

	@Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
	private void liquid$photoHoldsGameplay(CallbackInfo ci) {
		if (PhotoMode.blocksGameplay()) {
			for (KeyMapping mapping : options.keyMappings) {
				while (mapping.consumeClick()) {
					// drained
				}
			}
			ci.cancel();
		}
	}
}
