package net.client.visuals.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.render.BackdropCapture;
import net.client.visuals.render.hand.HandChams;
import net.client.visuals.render.post.PhotoCapture;
import net.client.visuals.render.post.PostFx;
import net.client.visuals.render.shader.BlurEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <ul>
 *   <li>Blur strength: the blur post-chain reads its radius from the {@code MenuBlurRadius} global
 *       uniform, extracted from {@code Options#getMenuBackgroundBlurriness()} each frame. While a
 *       Liquid screen is open we substitute the glass radius, leaving every other screen on the
 *       player's setting.</li>
 *   <li>Post-processing ({@code PostFx}): the frame's projection (with view bobbing) is captured
 *       before the world renders; Atmosphere finishes right after the world (before the hand, while
 *       the depth buffer holds only the world – its first half runs inside the world render, see
 *       {@code LevelRendererMixin}); Bloom and Color Grading after the hand, before vanilla's post
 *       effects and the GUI.</li>
 *   <li>The frame copy for blurred HUD glass, right before the GUI draws.</li>
 * </ul>
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private GameRenderState gameRenderState;

	@Redirect(
			method = "extractOptions",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getMenuBackgroundBlurriness()I"))
	private int liquid$glassBlurRadius(Options options) {
		// gui can be null for frames rendered while the client is still constructing.
		boolean liquid = minecraft.gui != null && minecraft.gui.screen() instanceof LiquidScreen;
		return BlurEngine.effectiveRadius(options.getMenuBackgroundBlurriness(), liquid);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V"))
	private void liquid$beginLevel(CallbackInfo ci, @Local Matrix4f projectionMatrix, @Local CameraRenderState cameraState) {
		PostFx.beginLevel(projectionMatrix, cameraState, gameRenderState.levelRenderState.skyRenderState);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;render3dHud(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/OptionsRenderState;Z)V"))
	private void liquid$atmosphere(CallbackInfo ci) {
		PostFx.afterLevel();
	}

	/** Hand Chams (Glass): a copy of the frame behind the hands, right before they're drawn. */
	@Inject(method = "render3dHud", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
	private void liquid$handBackdrop(CallbackInfo ci) {
		HandChams.beforeHands();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;applyPostEffects()V"))
	private void liquid$grade(CallbackInfo ci) {
		PostFx.beforePostEffects();
		// Photo Mode exposes the finished, graded frame (before vanilla's post effects and the GUI).
		PhotoCapture.frame();
	}

	/** Frame copy for blurred HUD glass: everything but the GUI is in the main target now. */
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V"))
	private void liquid$captureBackdrop(CallbackInfo ci) {
		BackdropCapture.capture();
	}
}
