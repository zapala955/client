package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3x2fStack;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.vs;
import ru.white.event.impl.EventReceivePacket;

@ModuleAnnotation(
   name = "Health Alert",
   category = Category.RENDER,
   desc = "Визуальный эффект когда мало здоровья"
)
@Environment(EnvType.CLIENT)
public class HealthAlert extends Module {
   public Animation animation_1;
   public Animation animation_3;
   public Animation animation_2;
   public SliderSetting heath = new SliderSetting(this, "Кол в хп", 5.0F, 1.0F, 15.0F, 1.0F);

   public HealthAlert() {
      this.animation_1 = new Animation();
      this.animation_2 = new Animation();
      this.animation_3 = new Animation();
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      Matrix3x2fStack v2 = e.getDrawContext().method_51448();
      v2.pushMatrix();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float v3 = mc.field_1724.method_6032() + mc.field_1724.method_6067();
         boolean v4 = false;
         if (v3 < this.heath.getValue()) {
            v4 = true;
         }

         this.animation_1.update();
         this.animation_1.run(v4 ? 1.0 : 0.0, 0.15F, ap.SINE_OUT);
         if (this.animation_1.get() > 0.0F) {
            float v5 = (float)(0.6F + 0.15F * Math.sin((double)System.currentTimeMillis() / 150.0));
            float v6 = (float)(0.0 + 0.2F * Math.sin((double)System.currentTimeMillis() / 300.0));
            float temp2 = (float)mc.method_22683().method_4486();
            float temp3 = (float)mc.method_22683().method_4502();
            int[] temp4 = new int[4];

            temp4[0] = up.getColor(0, v5 * this.animation_1.get());
            temp4[1] = 0;
            temp4[2] = 0;
            temp4[3] = 0;
            vs.ys.gradientRect(0.0F, 0.0F, temp2, temp3, temp4, 0.0F);
            vs.ys.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, up.getColor(0, v5 * this.animation_1.get()), 0, 0},
               0.0F
            );
            vs.ys.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, up.getColor(0, v5 * this.animation_1.get()), 0},
               0.0F
            );
            vs.ys.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, 0, 0, up.getColor(0, v5 * this.animation_1.get())},
               0.0F
            );
            vs.ys.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{up.getColor(255, 0, 0, v6 * this.animation_1.get()), 0, 0, 0},
               0.0F
            );
            vs.ys.gradientRect(
               0.0F,
               0.0F,
               (float)mc.method_22683().method_4486(),
               (float)mc.method_22683().method_4502(),
               new int[]{0, up.getColor(255, 0, 0, v6 * this.animation_1.get()), 0, 0},
               0.0F
            );
            temp2 = (float)mc.method_22683().method_4486();
            int v12 = mc.method_22683().method_4502();

            vs.ys.gradientRect(
               0.0F, 0.0F, temp2, (float)v12, new int[]{0, 0, up.getColor(255, 0, 0, v6 * this.animation_1.get()), 0}, 0.0F
            );
            temp2 = (float)mc.method_22683().method_4486();
            temp3 = (float)mc.method_22683().method_4502();
            temp4 = new int[]{0, 0, 0, up.getColor(255, 0, 0, v6 * this.animation_1.get())};

            vs.ys.gradientRect(0.0F, 0.0F, temp2, temp3, temp4, 0.0F);
         }
      }

      v2.popMatrix();
   }

   
}
