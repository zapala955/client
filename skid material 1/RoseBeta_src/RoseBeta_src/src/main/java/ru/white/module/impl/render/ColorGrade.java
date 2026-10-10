package ru.white.module.impl.render;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.vs;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Color Grade",
   desc = "Цветовая гамма мира (руки не затрагивает)",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ColorGrade extends Module {
   public SliderSetting vignetteStr;
   public BooleanSetting vignetteOn;
   public SliderSetting intensity;
   private static final int[][] PRESETS = new int[][]{
      {255, 110, 40},
      {25, 60, 190},
      {45, 165, 65},
      {135, 25, 215},
      {215, 150, 75}
   };
   public tf tintColor = new tf(this, "Цвет", new Color(80, 130, 255).getRGB());
   public SliderSetting brightness;

   public ColorGrade() {
      this.intensity = new SliderSetting(this, "Сила тинта", 0.25F, 0.02F, 0.7F, 0.01F);
      this.brightness = new SliderSetting(this, "Яркость", 0.0F, -0.4F, 0.4F, 0.01F);
      this.vignetteOn = new BooleanSetting(this, "Виньетка", false);
      this.vignetteStr = new SliderSetting(this, "Сила виньетки", 0.45F, 0.1F, 0.85F, 0.05F).setVisible(() -> {
         Boolean result = this.vignetteOn.getValue();

         return result;
      });
   }

   private static int m678(float v) {
      int result = (int)Math.max(0.0F, Math.min(255.0F, v));

      return result;
   }

   @EventTarget(
      priority = 500
   )
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float v2 = (float)mc.method_22683().method_4489() / 2.0F;

         float v3 = (float)mc.method_22683().method_4506() / 2.0F;
         int v7 = this.tintColor.getValue();
         int v4 = v7 >> 16 & 255;
         int v5 = v7 >> 8 & 255;
         int v6 = v7 & 255;
         int v8 = m678(this.intensity.getValue() * 255.0F);
         vs.ys.rect(0.0F, 0.0F, v2, v3, up.getColor(v4, v5, v6, v8));
         float v9 = this.brightness.getValue();
         if (v9 < -0.005F) {
            vs.ys.rect(0.0F, 0.0F, v2, v3, up.getColor(0, 0, 0, m678(-v9 * 255.0F)));
         } else if (v9 > 0.005F) {
            vs.ys.rect(0.0F, 0.0F, v2, v3, up.getColor(255, 255, 255, m678(v9 * 255.0F)));
         }

         if (this.vignetteOn.getValue()) {
            this.m676(v2, v3, this.vignetteStr.getValue());
         }
      }
   }

   private static int[] m677(String name) {
      byte v2 = -1;
      switch (name.hashCode()) {
         case 1044487:
            if (name.equals("Лес")) {
               v2 = 2;
            }
            break;
         case 32448614:
            if (name.equals("Ночь")) {
               v2 = 1;
            }
            break;
         case 999936563:
            if (name.equals("Закат")) {
               v2 = 0;
            }
            break;
         case 1008405387:
            boolean result = name.equals("Ретро");

            if (result) {

               v2 = 4;
            }
            break;
         case 1180036768:
            if (name.equals("Пурпур")) {
               v2 = 3;
            }
      }

      int[] v6;
      switch (v2) {
         case 0:
            v6 = PRESETS[0];
            break;
         case 1:
            v6 = PRESETS[1];
            break;
         case 2:
            v6 = PRESETS[2];
            break;
         case 3:
            v6 = PRESETS[3];
            break;
         case 4:
            v6 = PRESETS[4];
            break;
         default:
            v6 = new int[]{128, 0, 0};

            v6[1] = 128;
            v6[2] = 128;
      }

      return v6;
   }

   private void m676(float w, float h, float strength) {
      int v4 = m678(strength * 255.0F);
      int v5 = up.getColor(0, 0, 0, v4);
      int v6 = up.getColor(0, 0, 0, 0);
      float v7 = w * 0.52F;
      float v8 = h * 0.52F;
      vs.ys.gradientRect(0.0F, 0.0F, w, v8, new int[]{v5, v5, v6, v6}, 0.0F);
      float temp1 = h - v8;
      int[] temp4 = new int[4];

      temp4[0] = v6;
      temp4[1] = v6;
      temp4[2] = v5;
      temp4[3] = v5;
      vs.ys.gradientRect(0.0F, temp1, w, v8, temp4, 0.0F);
      vs.ys.gradientRect(0.0F, 0.0F, v7, h, new int[]{v5, v6, v6, v5}, 0.0F);
      vs.ys.gradientRect(w - v7, 0.0F, v7, h, new int[]{v6, v5, v5, v6}, 0.0F);
   }

   
}
