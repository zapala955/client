package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.ItemUtil;
import ru.white.event.impl.EventStep;

@ModuleAnnotation(
   name = "Pvp Safe",
   category = Category.PLAYER,
   desc = "Во время ПВП блокирует выход с сервера и команды выхода (/hub /limbo /an /anarchy)"
)
@Environment(EnvType.CLIENT)
public class PvpSafe extends Module {
   public static boolean isActive() {
      oi v0 = get();
      boolean result;
      if (v0 != null && v0.isEnabled() && iq.isPvp()) {
         result = true;
         int temp2 = v1 + 1;

         
      } else {
         result = false;
      }

      return result;
   }

   public static boolean isBlocked(String raw) {
      if (raw != null && !raw.isEmpty()) {
         String v1 = raw.toLowerCase().trim();
         if (v1.startsWith("/")) {
            v1 = v1.substring(1).trim();
         }

         String v2 = v1.split("\\s+", 2)[0];
         if (!v2.equals("hub") && !v2.equals("limbo") && !v2.equals("anarchy") && !v2.startsWith("an")) {

            return v1.matches(".*\\b(hub|limbo|anarchy)\\b.*");
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @EventTarget
   public void onPacket(EventStep event) {
      if (this.isEnabled() && event.isSend() && iq.isPvp()) {
         String v2 = null;
         Packet v6 = event.getPacket();

         if (v6 instanceof ChatMessageC2SPacket v3) {
            v2 = v3.comp_945();
         } else if (event.getPacket() instanceof ChatCommandSignedC2SPacket v4) {
            v2 = v4.comp_2532();
         } else if (event.getPacket() instanceof CommandExecutionC2SPacket v5) {
            v2 = v5.comp_808();
         }

         if (v2 != null && isBlocked(v2)) {
            event.cancel();
         }
      }
   }

   public static oi get() {
      Module result = uq.get(oi.class);

      return (oi)result;
   }

   
}
