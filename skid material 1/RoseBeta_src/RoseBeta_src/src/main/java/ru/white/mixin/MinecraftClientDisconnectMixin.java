package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.PvpSafe;

@Environment(EnvType.CLIENT)
@Mixin({MinecraftClient.class})
public class MinecraftClientDisconnectMixin {
   @Inject(
      method = {"method_72100", "method_72099"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pvpSafe$blockLeave(CallbackInfo ci) {
      if (oi.isActive()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_76797"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pvpSafe$blockLeaveTransfer(boolean transferring, CallbackInfo ci) {
      if (oi.isActive()) {
         ci.cancel();
      }
   }
}
