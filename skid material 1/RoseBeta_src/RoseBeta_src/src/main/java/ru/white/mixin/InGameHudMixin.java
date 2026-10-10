package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.AutoTool;
import ru.white.Client;
import ru.white.manager.eb;
import ru.white.module.impl.render.NoRender;
import ru.white.module.impl.render.CrossHair;
import ru.white.utils.render.et;
import ru.white.utils.render.us;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class InGameHudMixin {
   @Inject(
      method = {"method_1753"},
      at = {@At("TAIL")}
   )
   public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      MinecraftClient EventItemUse = MinecraftClient.method_1551();
      if (mc.field_1687 != null) {
         et.frame();
         Screen screen = mc.field_1755;
         if (!this.isLoadingScreen(screen)) {
            context.method_71048();
            us.beginOverlay();
            context.method_51448().pushMatrix();
            EventReceivePacket event = new EventReceivePacket(context, tickCounter.method_60637(false));
            Client.get().componentManager().get(eb.class).post(context.method_51448());
            event.hook();
            Client.get().render2D().flushAll();
            context.method_51448().popMatrix();
            us.endOverlay();
         }
      }
   }

   @Inject(
      method = {"method_1765"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      ci.cancel();
   }

   @Inject(
      method = {"method_1736"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void removeVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      try {
         xi crosshairModule = xi.getInstance();
         if (crosshairModule.isEnabled()) {
            ci.cancel();
         }
      } catch (Exception v5) {
      }
   }

   @Unique
   private boolean isLoadingScreen(Screen screen) {
      if (screen == null) {
         return false;
      } else {
         String className = screen.getClass().getSimpleName().toLowerCase();
         String fullName = screen.getClass().getName().toLowerCase();
         if (className.contains("loading")) {
            return true;
         } else if (className.contains("progress")) {
            return true;
         } else if (className.contains("connecting")) {
            return true;
         } else if (className.contains("downloading")) {
            return true;
         } else if (className.contains("terrain")) {
            return true;
         } else if (className.contains("generating")) {
            return true;
         } else if (className.contains("saving")) {
            return true;
         } else if (className.contains("reload")) {
            return true;
         } else if (className.contains("resource")) {
            return true;
         } else {
            return className.contains("pack") ? true : fullName.contains("mojang");
         }
      }
   }

   @Inject(
      method = {"method_61980"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderNauseaOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_55803"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderScoreboard(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreScoreboard.getValue()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_70837"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderBossBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreBossBar.getValue()) {
         ci.cancel();
      }
   }

   @ModifyExpressionValue(
      method = {"method_1759"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1661;method_67532()I"
      )}
   )
   private int white$spoofHotbarSelectedSlot(int original) {
      if (ci.isSilentActive()) {
         int spoofSlot = ci.getVisualSlot();
         if (spoofSlot >= 0 && spoofSlot < 9) {
            return spoofSlot;
         }
      }

      return original;
   }

   @Inject(
      method = {"method_1749"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void white$hideTooltipOnAutoTool(DrawContext context, CallbackInfo ci) {
      if (ci.isSilentActive()) {
         ci.cancel();
      }
   }
}
