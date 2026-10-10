package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventStep;

@ModuleAnnotation(
   name = "No Slot Change",
   desc = "Запрещает серверу свапать предметы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class NoSlotChange extends Module {
   @EventTarget
   public void onEvent(EventStep e) {
      if (e.getPacket() instanceof UpdateSelectedSlotS2CPacket) {

         e.setCancelled(true);
      }
   }

   
}
