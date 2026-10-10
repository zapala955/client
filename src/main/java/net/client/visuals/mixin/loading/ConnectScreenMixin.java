package net.client.visuals.mixin.loading;

import net.client.visuals.LiquidConfig;
import net.client.visuals.gui.loading.LoadingScene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Connecting to the server…" as the Liquid loading card with the server's name and icon. The
 * vanilla Cancel button stays (it aborts the connection); it's moved under the card and drawn as
 * a glass pill.
 */
@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {
	@Unique
	private static final Component LIQUID$JOINING = Component.translatable("narrator.joining");

	@Shadow
	private Component status;
	@Shadow
	private long lastNarration;

	protected ConnectScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "startConnecting", at = @At("HEAD"))
	private static void liquid$rememberServer(Screen parent, Minecraft minecraft, ServerAddress hostAndPort, ServerData data,
			boolean isQuickPlay, @Nullable TransferState transferState, CallbackInfo ci) {
		LoadingScene.connecting = data;
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
		long now = Util.getMillis();
		if (now - lastNarration > 2000L) {
			lastNarration = now;
			minecraft.getNarrator().saySystemNow(LIQUID$JOINING);
		}
		ServerData server = LoadingScene.connecting;
		AbstractWidget cancel = null;
		for (GuiEventListener child : children()) {
			if (child instanceof AbstractWidget widget) {
				cancel = widget;
				break;
			}
		}
		LoadingScene.INSTANCE.reset()
				.title(server != null && !server.name.isEmpty() ? server.name : "Connecting")
				.status(status)
				.serverIcon(server != null ? server.getIconBytes() : null)
				.button(cancel)
				.extract(graphics, width, height, mouseX, mouseY);
	}
}
