package net.client.visuals.mixin.loading;

import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.loading.LoadingScene;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** "Saving world", "Reading world data…" and friends as the Liquid loading card with a spinner. */
@Mixin(GenericMessageScreen.class)
public abstract class GenericMessageScreenMixin extends Screen {
	protected GenericMessageScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void liquid$background(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (LiquidConfig.liquidLoading) {
			ci.cancel();
			LoadingScene.INSTANCE.background(graphics, width, height);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (!LiquidConfig.liquidLoading) {
			super.extractRenderState(graphics, mouseX, mouseY, a);
			return;
		}
		LoadingScene.INSTANCE.reset()
				.title(title)
				.status(null)
				.extract(graphics, width, height, mouseX, mouseY);
	}
}
