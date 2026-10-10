package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventRender3D;

@ModuleAnnotation(
   name = "FOV Changer",
   desc = "Кастомный угол обзора",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class FOVChanger extends Module {
   public final BooleanSetting adaptiveSensitivity;
   public final SliderSetting customFov = new SliderSetting(this, "FOV", 100.0F, 30.0F, 150.0F, 1.0F);
   public final BooleanSetting overrideSprintFov = new BooleanSetting(this, "Disable Sprint FOV", true);

   public static Fov get() {
      Module result = uq.get(Fov.class);

      return (Fov)result;
   }

   public FOVChanger() {
      this.adaptiveSensitivity = new BooleanSetting(
         this, "Адаптивная чувствительность", false
      );
   }

   @EventTarget
   public void onFov(EventRender3D e) {
      if (this.isEnabled()) {
         float v2 = this.customFov.getValue();
         if (this.overrideSprintFov.getValue()) {
            e.setFov((int)v2);
            e.setOverrideSprint(true);
         } else {
            boolean result;
            if (mc.field_1724 != null && mc.field_1724.method_5624()) {
               result = true;
            } else {
               result = false;
               
            }

            boolean v3 = result;
            float v4 = v2 + (v3 ? 5.0F : 0.0F);
            e.setFov((int)v4);
            e.setOverrideSprint(true);
         }

         if (this.adaptiveSensitivity.getValue()) {
            e.setModifySensitivity(true);
         }

         e.cancel();
      }
   }

   
}
