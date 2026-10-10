package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Photo Mode, Freeze: in singleplayer the game pauses as if the pause menu were open – entities,
 * particles, rain and the clock stand still, game sounds pause – without a menu over the view.
 * ({@code Minecraft} pauses only when the integrated server isn't open to LAN.)
 */
@Mixin(Gui.class)
public abstract class PhotoFreezeMixin {
	@ModifyReturnValue(method = "isPausing", at = @At("RETURN"))
	private boolean liquid$photoFreeze(boolean original) {
		return original || PhotoMode.freezesWorld();
	}
}
