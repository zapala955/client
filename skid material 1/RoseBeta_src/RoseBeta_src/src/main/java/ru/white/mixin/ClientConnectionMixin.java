package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventSound;
import ru.white.event.impl.EventStep;

@Environment(EnvType.CLIENT)
@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
   @Unique
   private static boolean stackOverflowFix;

   @Inject(
      method = {"method_10759"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static <T extends PacketListener> void triggerReceivePacketEvent(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
      EventStep event = new EventStep(packet, qb.rb.RECEIVE);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_10743"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void triggerSendPacketEvent(Packet<?> packet, CallbackInfo ci) {
      if (!stackOverflowFix) {
         EventStep event = new EventStep(packet, qb.rb.SEND);
         event.hook();
         if (event.isCancelled()) {
            ci.cancel();
         }

         Packet<?> newPacket = event.getPacket();
         if (newPacket != packet) {
            ci.cancel();
            stackOverflowFix = true;
            hp.mc.method_1562().method_52787(newPacket);
            stackOverflowFix = false;
         }

         if (packet instanceof CloseHandledScreenC2SPacket closePacket) {
            MinecraftClient EventItemUse = MinecraftClient.method_1551();
            EventSound screenCloseEvent = new EventSound(mc.field_1755, closePacket.method_36168());
            screenCloseEvent.hook();
            if (screenCloseEvent.isCancelled()) {
               ci.cancel();
            }
         }
      }
   }
}
