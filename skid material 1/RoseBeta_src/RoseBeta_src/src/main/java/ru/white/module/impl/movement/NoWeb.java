package ru.white.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Kp;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "No Web",
   desc = "хуй знает как обяснить что оно делает",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class NoWeb extends Module {
   @EventTarget
   public void onEvent(EventTick e) {
      if (!kp.nullCheck()) {

         if (kp.isPlayerInWeb()) {
            double[] v2 = kp.calculateDirection(0.5);
            ClientPlayerEntity v6 = mc.field_1724;

            double v7 = v2[0];
            double temp2;
            if (mc.field_1690.field_1903.method_1434()) {
               temp2 = 1.2;
            } else {

               temp2 = mc.field_1690.field_1832.method_1434() ? -2.0 : 0.0;
            }

            v6.method_18800(v7, temp2, v2[1]);
         }
      }
   }

   
}
