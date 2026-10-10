package net.client.visuals.mixin;

import net.client.visuals.render.BlurPanel;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.RoundedRectState;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.render.text.SharpText;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The single GUI render state is reset before extraction ({@code Gui#extractRenderState}) and after
 * drawing ({@code GuiRenderer#render}). After a reset nothing references our pooled element states
 * any more, so they can be reused for the next frame.
 */
@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin {
	@Inject(method = "reset", at = @At("TAIL"))
	private void liquid$recyclePrimitives(CallbackInfo ci) {
		RoundedRectState.resetPool();
		IconFont.resetPool();
		BlurPanel.resetPool();
		SharpText.onFrameReset();
		BackgroundEngine.resetPool();
	}
}
