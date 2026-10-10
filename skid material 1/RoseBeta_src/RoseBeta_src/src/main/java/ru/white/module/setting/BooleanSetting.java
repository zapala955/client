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
public class BooleanSetting extends Setting<Boolean> {
   public Animation animation;
   public Animation animation2;
   private Boolean f215;
   public Animation animation3;

   public static BooleanSetting SettingVisibility(String name, Boolean value) {
      return new BooleanSetting(name, value);
   }

   public Boolean getValue() {
      if (this.f215 == null) {
         this.f215 = (Boolean)super.getValue();
      }

      if (this.f215) {

         if (this.getVisible().get()) {
            return true;
         }
      }

      return false;
   }

   public BooleanSetting(String name, Boolean value) {
      super(name, value);
      this.animation = new ep(300, 1.0);
      this.animation2 = new ep(300, 1.0);
      this.animation3 = new ep(300, 1.0);
      this.f215 = value;
   }

   public BooleanSetting onSetVisible(Runnable action) {

      return (BooleanSetting)super.onSetVisible(action);
   }

   public BooleanSetting setVisible(Supplier<Boolean> value) {
      return (BooleanSetting)super.setVisible(value);
   }

   public BooleanSetting set(Boolean value) {
      BooleanSetting v2;
      boolean v4;
      label16: {
         v2 = (BooleanSetting)super.set(value);
         if ((Boolean)super.getValue()) {
            Boolean temp1 = this.getVisible().get();

            if (temp1) {
               v4 = true;
               break label16;
            }
         }

         v4 = false;
      }

      this.f215 = v4;
      return v2;
   }

   public BooleanSetting(Module parent, String name, Boolean value) {
      super(parent, name, value);
      this.animation = new ep(300, 1.0);
      this.animation2 = new ep(300, 1.0);
      this.animation3 = new ep(300, 1.0);
      this.f215 = value;
   }

   public BooleanSetting onAction(Runnable action) {
      Setting result = super.onAction(() -> {
         action.run();
         this.f215 = (Boolean)super.getValue() && this.getVisible().get();
         
      });

      return (BooleanSetting)result;
   }

   
}
