package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.render.camera.Showcase;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Showcase: after vanilla aligns the camera with the player each frame, the showcase may take it
 * over – position, rotation, detached (so the player model is drawn) – and blend in its lens.
 */
@Mixin(Camera.class)
public abstract class CameraShowcaseMixin {
	@Shadow
	private boolean detached;

	@Shadow
	private @Nullable Entity entity;

	@Shadow
	private @Nullable Level level;

	@Shadow
	protected abstract void setPosition(Vec3 position);

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Inject(method = "alignWithEntity", at = @At("TAIL"))
	private void liquid$showcase(float partialTicks, CallbackInfo ci) {
		int state = Showcase.apply(entity, level, partialTicks);
		if (state != Showcase.VANILLA) {
			setRotation(Showcase.yaw(), Showcase.pitch());
			setPosition(new Vec3(Showcase.x(), Showcase.y(), Showcase.z()));
			detached = state == Showcase.SHOT;
		}
	}

	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float liquid$showcaseLens(float fov) {
		return Showcase.fov(fov);
	}
}
