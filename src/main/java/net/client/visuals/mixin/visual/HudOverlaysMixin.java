package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.client.visuals.hud.island.DynamicIslandElement;
import net.client.visuals.module.render.RemovalsModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removals: HUD-layer overlays. In {@code extractCameraOverlays} the first texture overlay is the
 * equipped helmet's camera overlay (carved pumpkin) and the second the powder-snow frost.
 *
 * <p>Also makes room for the Dynamic Island: while it sits at the top centre, boss bars are drawn
 * shifted down below it.</p>
 */
@Mixin(Hud.class)
public abstract class HudOverlaysMixin {
	private static final String TEXTURE_OVERLAY =
			"Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V";

	@WrapWithCondition(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = TEXTURE_OVERLAY, ordinal = 0))
	private boolean liquid$pumpkin(Hud hud, GuiGraphicsExtractor graphics, Identifier texture, float alpha) {
		return !RemovalsModule.pumpkinOverlay();
	}

	@WrapWithCondition(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = TEXTURE_OVERLAY, ordinal = 1))
	private boolean liquid$powderSnow(Hud hud, GuiGraphicsExtractor graphics, Identifier texture, float alpha) {
		return !RemovalsModule.powderSnowOverlay();
	}

	@Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
	private void liquid$noVignette(GuiGraphicsExtractor graphics, @Nullable Entity camera, CallbackInfo ci) {
		if (RemovalsModule.vignetteOverlay()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void liquid$noPortalOverlay(GuiGraphicsExtractor graphics, float alpha, CallbackInfo ci) {
		if (RemovalsModule.nauseaEffects()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
	private void liquid$noConfusionOverlay(GuiGraphicsExtractor graphics, float strength, CallbackInfo ci) {
		if (RemovalsModule.nauseaEffects()) {
			ci.cancel();
		}
	}

	@Unique
	private boolean liquid$bossBarsShifted;

	@Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
	private void liquid$noBossBars(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (RemovalsModule.bossBars()) {
			ci.cancel();
			return;
		}
		// Only pushed when the method runs to its end, so the RETURN injector below always pops it.
		float shift = DynamicIslandElement.bossBarShift(graphics.guiWidth(), graphics.guiHeight());
		if (shift > 0.01f) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(0f, shift);
			liquid$bossBarsShifted = true;
		}
	}

	@Inject(method = "extractBossOverlay", at = @At("RETURN"))
	private void liquid$unshiftBossBars(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (liquid$bossBarsShifted) {
			liquid$bossBarsShifted = false;
			graphics.pose().popMatrix();
		}
	}

	@Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	private void liquid$noScoreboard(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (RemovalsModule.scoreboardSidebar()) {
			ci.cancel();
		}
	}
}
