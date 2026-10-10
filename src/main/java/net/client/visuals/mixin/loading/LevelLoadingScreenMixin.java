package net.client.visuals.mixin.loading;

import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.loading.LoadingScene;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World loading ("Loading terrain…") as the Liquid loading card with a live chunk map. Game code
 * checks {@code instanceof LevelLoadingScreen} and calls {@code update(...)} on it, so the screen
 * itself stays; only its drawing changes. Portal transitions keep vanilla's portal backdrops.
 */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin extends Screen {
	@Unique
	private static final Component LIQUID$PREPARING = Component.literal("Preparing spawn area");
	@Unique
	private static final Component LIQUID$TERRAIN = Component.translatable("multiplayer.downloadingTerrain");

	@Shadow
	private LevelLoadTracker loadTracker;
	@Shadow
	private LevelLoadingScreen.Reason reason;
	@Shadow
	private long lastNarration;

	protected LevelLoadingScreenMixin(Component title) {
		super(title);
	}

	@Unique
	private boolean liquid$active() {
		return LiquidConfig.liquidLoading && reason == LevelLoadingScreen.Reason.OTHER;
	}

	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void liquid$background(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (liquid$active()) {
			ci.cancel();
			LoadingScene.INSTANCE.background(graphics, width, height);
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void liquid$extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (!liquid$active()) {
			return;
		}
		ci.cancel();
		long now = Util.getMillis();
		if (now - lastNarration > 2000L) {
			lastNarration = now;
			triggerImmediateNarration(true);
		}
		boolean progress = loadTracker.hasProgress();
		LoadingScene.INSTANCE.reset()
				.title(liquid$worldName())
				.status(progress ? LIQUID$PREPARING : LIQUID$TERRAIN)
				.progress(progress ? loadTracker.serverProgress() : -1f)
				.chunks(loadTracker.statusView())
				.extract(graphics, width, height, mouseX, mouseY);
	}

	@Unique
	private String liquid$worldName() {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server != null) {
			try {
				return server.getWorldData().getLevelName();
			} catch (RuntimeException ignored) {
				// World data not ready yet.
			}
		}
		ServerData current = minecraft.getCurrentServer();
		return current != null ? current.name : "Loading World";
	}
}
