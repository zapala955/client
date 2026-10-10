package net.client.visuals.mixin.loading;

import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.loading.LoadingScene;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Multi-stage progress (saving and leaving a world, optimising) as the Liquid loading card. */
@Mixin(ProgressScreen.class)
public abstract class ProgressScreenMixin extends Screen {
	@Shadow
	private @Nullable Component header;
	@Shadow
	private @Nullable Component stage;
	@Shadow
	private int progress;
	@Shadow
	private boolean stop;
	@Shadow
	@Final
	private boolean clearScreenAfterStop;
	@Unique
	private static final Component LIQUID$WORKING = Component.translatable("menu.working");
	@Unique
	private @Nullable Component liquid$stageShown;
	@Unique
	private int liquid$progressShown = -1;
	@Unique
	private @Nullable Component liquid$status;

	protected ProgressScreenMixin(Component title) {
		super(title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (LiquidConfig.liquidLoading) {
			LoadingScene.INSTANCE.background(graphics, width, height);
		} else {
			super.extractBackground(graphics, mouseX, mouseY, a);
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void liquid$extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (!LiquidConfig.liquidLoading) {
			return;
		}
		ci.cancel();
		if (stop) {
			// Vanilla's own exit, unchanged.
			if (clearScreenAfterStop) {
				minecraft.gui.setScreen(null);
			}
			return;
		}
		if (stage != liquid$stageShown || progress != liquid$progressShown) {
			liquid$stageShown = stage;
			liquid$progressShown = progress;
			liquid$status = stage == null ? null : progress != 0 ? Component.empty().append(stage).append(" " + progress + "%") : stage;
		}
		LoadingScene.INSTANCE.reset()
				.title(header != null ? header : LIQUID$WORKING)
				.status(liquid$status)
				.progress(progress > 0 ? progress / 100f : -1f)
				.extract(graphics, width, height, mouseX, mouseY);
	}
}
