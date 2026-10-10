package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.module.utility.ZoomModule;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Zoom: the world FOV the camera computes once per frame (the hand keeps its own FOV). */
@Mixin(Camera.class)
public abstract class CameraZoomMixin {
	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float liquid$zoom(float original) {
		return ZoomModule.fov(original);
	}
}
