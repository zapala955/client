package ru.white.utils.misc;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Rf extends Setting<Boolean> {
   private Boolean f216;

   public rf set(Boolean value) {
      rf v2 = (rf)super.set(value);
      this.f216 = (Boolean)super.getValue() && this.getVisible().get();

      return v2;
   }

   public Rf(String name, Boolean value) {
      super(name, value);
      this.f216 = value;
   }

   public Boolean getValue() {
      if (this.f216 == null) {
         this.f216 = (Boolean)super.getValue();
      }

      if (this.f216) {
         int v1 = -360621341;
         int temp2 = v1 + 1;

         if (this.getVisible().get()) {
            return true;
         }
      }

      return false;
   }

   public Rf(Module parent, String name, Boolean value) {
      super(parent, name, value);
      this.f216 = value;
   }

   public static rf SettingVisibility(String name, Boolean value) {
      rf result = new rf;

      result./* $VF: Unable to resugar constructor */<init>(name, value);
      return result;
   }

   public BooleanSetting onSetVisible(Runnable action) {

      Setting result = super.onSetVisible(action);

      BooleanSetting v5 = (BooleanSetting)result;

      return v5;
   }

   public rf onAction(Runnable action) {
      Runnable temp1 = () -> {
         boolean v4x;
         label16: {
            action.run();
            if ((Boolean)super.getValue()) {
               Boolean var10001x = this.getVisible().get();

               if (var10001x) {
                  v4x = true;
                  break label16;
               }
            }

            v4x = false;
         }

         this.f216 = v4x;
      };

      rf result = (rf)super.onAction(temp1);

      return result;
   }

   public rf setVisible(Supplier<Boolean> value) {
      Setting result = super.setVisible(value);
      int v2 = -787370392;

      return (rf)result;
   }

   
}
