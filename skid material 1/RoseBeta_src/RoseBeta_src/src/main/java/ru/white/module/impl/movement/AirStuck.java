package ru.white.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventMove;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Air Stuck",
   category = Category.MOVEMENT,
   desc = "Заморозка персонажа в воздухе"
)
@Environment(EnvType.CLIENT)
public class AirStuck extends Module {
   @EventTarget
   public void onMotion(EventMove eventMotion) {
      if (!mc.field_1724.method_24828()) {

         eventMotion.ground(false);
      }
   }

   public static th getInstance() {

      th result = uq.get(th.class);

      return result;
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (!mc.field_1724.method_24828() && e.getPacket() instanceof PlayerMoveC2SPacket) {
         e.cancel();
         
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {

      if (!mc.field_1724.method_24828()) {

         mc.field_1724.method_18800(0.0, 0.0, 0.0);
      }
   }

   
}
