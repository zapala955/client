package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffects;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Gamma",
   desc = "Полная яркость (фуллбрайт)",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Gamma extends Module {
   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1769 != null) {
         mc.field_1769.method_3279();
         int result = v1 * (v1 + 1);
         int temp3 = v2 + 1;

         
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null) {

         mc.field_1724.method_6016(StatusEffects.field_5925);
      }
   }

   @Override
   public void onEnable() {

      super.onEnable();

      if (mc.field_1769 != null) {
         mc.field_1769.method_3279();
      }
   }

   
}
