package net.client.visuals.mixin.visual;

import net.client.visuals.mixin.access.ClientInputAccessor;
import net.client.visuals.render.camera.PhotoMode;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Photo Mode: WASD, jump and sneak fly the camera, so your character's movement input is held (it
 * stands where it was, also on servers where the world can't freeze).
 */
@Mixin(KeyboardInput.class)
public abstract class PhotoPlayerInputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void liquid$photoHoldsPlayer(CallbackInfo ci) {
		if (PhotoMode.blocksGameplay()) {
			ClientInput input = (ClientInput) (Object) this;
			input.keyPresses = Input.EMPTY;
			((ClientInputAccessor) input).liquid$setMoveVector(Vec2.ZERO);
		}
	}
}
