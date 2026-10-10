package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.render.camera.PhotoMode;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Showcase and Photo Mode: after vanilla aligns the camera with the player each frame, either may
 * take it over – position, rotation, detached (so the player model is drawn) – and set the lens.
 * Photo Mode comes first (it switches Showcase off). While its camera is rolled, culling uses a
 * wider view so the screen's corners never miss chunks.
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
		if (PhotoMode.apply(entity, level, partialTicks)) {
			setRotation(PhotoMode.yaw(), PhotoMode.pitch());
			setPosition(new Vec3(PhotoMode.x(), PhotoMode.y(), PhotoMode.z()));
			detached = true;
			return;
		}
		int state = Showcase.apply(entity, level, partialTicks);
		if (state != Showcase.VANILLA) {
			setRotation(Showcase.yaw(), Showcase.pitch());
			setPosition(new Vec3(Showcase.x(), Showcase.y(), Showcase.z()));
			detached = state == Showcase.SHOT;
		}
	}

	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float liquid$showcaseLens(float fov) {
		return PhotoMode.active() ? PhotoMode.fov() : Showcase.fov(fov);
	}

	@ModifyArg(method = "createProjectionMatrixForCulling", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;"),
			index = 0)
	private float liquid$photoRollCulling(float fovRadians) {
		return Math.min(3.0f, fovRadians * PhotoMode.cullingFovScale());
	}
}
