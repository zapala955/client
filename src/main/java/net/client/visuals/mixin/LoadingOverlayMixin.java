package net.client.visuals.mixin;

import net.client.visuals.LiquidConfig;
import net.client.visuals.render.loading.BootScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The resource-loading overlay ("Mojang Studios" screen) drawn as the Liquid boot screen. Only the
 * drawing is replaced: {@code tick()} (reload completion, exceptions, re-initialising the screen)
 * stays vanilla, and the overlay removes itself the same way when its exit animation ends.
 */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private ReloadInstance reload;
	@Shadow
	@Final
	private boolean fadeIn;
	@Shadow
	private long fadeOutStart;
	@Shadow
	private long fadeInStart;
	@Unique
	private final BootScreen liquid$boot = new BootScreen();

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void liquid$extractBootScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!LiquidConfig.liquidLoading) {
			return;
		}
		ci.cancel();
		if (fadeIn && fadeInStart == -1L) {
			fadeInStart = Util.getMillis();
		}
		if (liquid$boot.extract(graphics, minecraft, reload.getActualProgress(), fadeIn, fadeInStart, fadeOutStart, mouseX, mouseY, partialTick)) {
			minecraft.gui.setOverlay(null);
		}
	}
}
