package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.Client;
import ru.white.utils.Aq;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
@Mixin({MinecraftClient.class})
public class ClientMixin {
   @Inject(
      method = {"method_15993"},
      at = {@At("TAIL")}
   )
   private void applyDarkMode(CallbackInfo ci) {
      MinecraftClient client = MinecraftClient.method_1551();
      aq.enableImmersiveDarkMode(client.method_22683().method_4490());
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("TAIL")}
   )
   private void publishClientTick(CallbackInfo ci) {
      MinecraftClient client = (MinecraftClient)this;
      if (!client.method_1493()) {
         ClientPlayerEntity player = client.field_1724;
         ClientWorld world = client.field_1687;
         if (player != null && world != null) {
            Client.eventHandler().post(new EventRenderModel());
         }
      }
   }

   @Inject(
      method = {"method_24287"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void getWindowTitle(CallbackInfoReturnable<String> cir) {
      cir.setReturnValue("Minecraft 1.21.11");
   }
}
