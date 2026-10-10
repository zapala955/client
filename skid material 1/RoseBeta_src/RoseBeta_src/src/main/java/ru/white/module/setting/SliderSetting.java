package ru.white.module.setting;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class SliderSetting extends Setting<Float> {
   public float increment;
   public Animation animation2;
   public Animation animation3;
   public float min;
   public float max;
   private Float f224;
   public Animation animation = new ep(300, 1.0);

   public SliderSetting(Module parent, String name, float value, float min, float max, float increment) {
      super(parent, name, value);
      this.animation2 = new ep(300, 1.0);
      this.animation3 = new ep(300, 1.0);
      this.min = min;
      this.max = max;
      this.increment = increment;
      this.f224 = value;
   }

   public Float getValue() {

      if (this.f224 == null) {
         this.f224 = (Float)super.getValue();
      }

      return this.f224;
   }

   public SliderSetting set(Float value) {
      SliderSetting v2 = (SliderSetting)super.set(value);
      this.f224 = (Float)super.getValue();

      return v2;
   }

   public SliderSetting onAction(Runnable action) {

      Setting result = super.onAction(() -> {
         action.run();
         this.f224 = (Float)super.getValue();
         
      });

      return (SliderSetting)result;
   }

   public SliderSetting setVisible(Supplier<Boolean> value) {

      Setting result = super.setVisible(value);

      return (SliderSetting)result;
   }

   public SliderSetting onSetVisible(Runnable action) {
      SliderSetting result = (SliderSetting)super.onSetVisible(action);

      return result;
   }

   
}
