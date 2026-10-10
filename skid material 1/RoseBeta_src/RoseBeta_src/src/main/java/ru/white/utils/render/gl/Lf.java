package ru.white.utils.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Uf;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;
import ru.white.screen.GuiColorPicker;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class Lf {
   public final uf title;
   public final SliderSetting interval;
   public final SliderSetting distance;
   public final SliderSetting height;

   private static boolean m227(Module parent) {
      boolean result = mo.getInstance().isEditing(parent);

      return result;
   }

   public static lf SettingVisibility(Module parent, float distance, float height, float interval) {
      lf result = new lf;
      int v4 = -176756574;

      result./* $VF: Unable to resugar constructor */<init>(parent, distance, height, interval);
      return result;
   }

   public static lf withoutInterval(Module parent, float distance, float height) {
      lf result = new lf;
      Float temp3 = distance;

      result./* $VF: Unable to resugar constructor */<init>(parent, temp3, height, null);
      return result;
   }

   public static sf button(Module parent) {
      sf result = new sf;

      result./* $VF: Unable to resugar constructor */<init>(parent, "Предпоказ", () -> {
         if (hp.mc.field_1755 instanceof ko v1x) {
            v1x.openPreviewEditor(parent);
         } else if (hp.mc.field_1755 instanceof DropDownMenu v2x) {
            v2x.openPreviewEditor(parent);
         } else {
            Screen v9 = hp.mc.field_1755;

            if (v9 instanceof TestGui) {
               int v6 = -763306165;
               int v10 = v6 * (v6 + 1) % 2;

               if (v10 != 0) {
                  }

               TestGui v3x = (TestGui)v9;
               v3x.openPreviewEditor(parent);
            }
         }
      });

      return result.setVisible(() -> {

         return !m227(parent);
      });
   }

   public static lf intervalOnly(Module parent, float interval) {
      lf result = new lf;

      result./* $VF: Unable to resugar constructor */<init>(parent, null, null, interval);
      return result;
   }

   public boolean repeats() {
      return this.interval != null;
   }

   private Lf(Module parent, Float defaultDistance, Float defaultHeight, Float defaultInterval) {
      boolean v5 = defaultDistance != null || defaultHeight != null || defaultInterval != null;
      this.title = !v5 ? null : new uf(parent, "Настройки предпоказа").setVisible(() -> {

         return m227(parent);
      });
      this.distance = defaultDistance == null
         ? null
         : new SliderSetting(parent, "Дистанция спавна", defaultDistance, 1.5F, 14.0F, 0.5F)
            .setVisible(() -> m227(parent));
      this.height = defaultHeight == null
         ? null
         : new SliderSetting(parent, "Высота спавна", defaultHeight, -2.0F, 4.0F, 0.1F).setVisible(() -> {

            return m227(parent);
         });
      this.interval = defaultInterval == null
         ? null
         : new SliderSetting(parent, "Интервал показа", defaultInterval, 0.5F, 10.0F, 0.5F).setVisible(() -> {
            Boolean result = m227(parent);
            int v1 = -1383473970;
            int temp2 = v1 + 1;

            int temp1 = v1 * temp2;

            return result;
         });
   }

   public float distance(float fallback) {
      float v5;
      if (this.distance == null) {

         v5 = fallback;
      } else {
         v5 = this.distance.getValue();

         
      }

      return v5;
   }

   public float height(float fallback) {

      int v2 = 661018601;

      return this.height == null ? fallback : this.height.getValue();
   }

   public static lf none(Module parent) {
      lf result = new lf;
      int v1 = 1888444586;
      int temp4 = v1 * (v1 + 1) % 2;

      if (temp4 != 0) {
         }

      result./* $VF: Unable to resugar constructor */<init>(parent, null, null, null);
      return result;
   }

   public long intervalMs() {
      long result;
      if (this.interval == null) {
         result = 0L;
      } else {
         Float v3 = this.interval.getValue();

         result = (long)(v3 * 1000.0F);
      }

      return result;
   }

   
}
