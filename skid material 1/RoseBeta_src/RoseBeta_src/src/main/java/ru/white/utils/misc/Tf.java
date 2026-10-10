package ru.white.utils.misc;

import java.awt.Color;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Tf extends Setting<Integer> {
   private float[] f219;
   private Integer f218;
   public boolean pickerOpen = false;
   public Animation pickerAnim;

   public float[] getHsb() {
      int v1 = this.getValue();
      if (this.f219 == null) {
         int temp1 = v1 >> 16 & 255;
         int temp2 = v1 >> 8;

         this.f219 = Color.RGBtoHSB(temp1, temp2 & 255, v1 & 255, null);
      }

      return this.f219;
   }

   public Integer getValue() {

      if (this.f218 == null) {
         Integer v4 = (Integer)super.getValue();

         this.f218 = v4;
      }

      return this.f218;
   }

   public tf setVisible(Supplier<Boolean> value) {
      Setting result = super.setVisible(value);

      tf v5 = (tf)result;
      int v2 = -1005080913;
      int v6 = v2 * (v2 + 1);

      return v5;
   }

   public tf set(Integer value) {
      tf v2 = (tf)super.set(value);
      this.f218 = (Integer)super.getValue();
      if (this.f218 != null && this.f219 != null) {
         int v3 = Color.HSBtoRGB(this.f219[0], this.f219[1], this.f219[2]) & 16777215;
         int result = this.f218 & 16777215;

         int v4 = result;
         if (v3 != v4) {

            this.f219 = Color.RGBtoHSB(
               this.f218 >> 16 & 255,
               this.f218 >> 8 & 255,
               this.f218 & 255,
               null
            );
         }
      }

      return v2;
   }

   public Tf(Module parent, String name, Integer value) {
      super(parent, name, value);
      this.pickerAnim = new ep(220, 1.0);
      this.f218 = value;
   }

   public void setHsb(float h, float s, float b) {
      int v8 = 1165074492;

      h = Math.max(0.0F, Math.min(1.0F, h));
      s = Math.max(0.0F, Math.min(1.0F, s));
      b = Math.max(0.0F, Math.min(1.0F, b));
      float[] v13 = new float[]{h, 0.0F, 0.0F};

      v13[1] = s;
      v13[2] = b;
      this.f219 = v13;
      int v4 = Color.HSBtoRGB(h, s, b);
      int v5 = this.getValue();
      int v6 = v5 & -16777216;
      if (v6 == 0) {
         v6 = -16777216;
      }

      this.set(v6 | v4 & 16777215);
   }

   public boolean updateHsbBar(int bar, float perc) {
      float[] v3 = this.getHsb();
      perc = Math.max(0.0F, Math.min(1.0F, perc));
      float result = v3[bar];

      result = Math.abs(result - perc);

      if (result < 5.0E-4F) {
         return false;
      } else {
         v3[bar] = perc;
         this.setHsb(v3[0], v3[1], v3[2]);
         return true;
      }
   }

   public tf onAction(Runnable action) {

      Runnable temp1 = () -> {
         action.run();
         this.f218 = (Integer)super.getValue();
      };

      return (tf)super.onAction(temp1);
   }

   public tf onSetVisible(Runnable action) {
      int v2 = -2121915866;

      return (tf)super.onSetVisible(action);
   }

   public Tf(Module parent, String name) {
      super(parent, name, up.RED);
      this.pickerAnim = new ep(220, 1.0);
      this.f218 = up.RED;
   }

   
}
