package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Criticals",
   desc = "ХЗ ЧО ПИСАТь",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class Criticals extends Module {
   public static jg getInstance() {
      Module result = uq.get(jg.class);

      return (jg)result;
   }

   public boolean canCritical() {
      if (this.isEnabled()) {
         double v5;
         int result = (v5 = mc.field_1724.field_6017 - 0.0) == 0.0 ? 0 : (v5 < 0.0 ? -1 : 1);

         if (result <= 0) {

            if (!mc.field_1724.method_24828()) {
               return true;
            }
         }
      }

      return false;
   }

   @EventTarget
   public void onEvent(EventTick e) {
      if (mc.field_1724 != null) {
         float result = mc.field_1724.method_7261(2.0F);

         if (result >= 1.0F && mc.field_1724.method_24828() && fg.target != null) {

            mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.04, mc.field_1724.method_18798().field_1350);
            mc.field_1724.field_64356 = true;
         }
      }
   }

   
}
