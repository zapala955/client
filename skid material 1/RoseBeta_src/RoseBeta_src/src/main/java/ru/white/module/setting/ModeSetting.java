package ru.white.module.setting;

import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class ModeSetting extends Setting<String> {
   public List<String> values;
   public Animation animation = new ep(300, 1.0);
   public Animation animation2 = new ep(300, 1.0);
   public boolean opened;
   public Animation animation3 = new ep(300, 1.0);
   private String f221;

   public ModeSetting(Module parent, String name, String... values) {
      super(parent, name, values[0]);
      this.values = List.of(values);
      this.set(values[0]);
      this.f221 = values[0];
   }

   public boolean is(String value) {
      if (this.getValue().equalsIgnoreCase(value)) {

         if (this.getVisible().get()) {
            return true;
         }
      }

      return false;
   }

   public String getValue() {
      if (this.f221 == null) {
         this.f221 = (String)super.getValue();
      }

      return this.f221;
   }

   public ModeSetting set(String value) {

      ModeSetting v2 = (ModeSetting)super.set(value);
      this.f221 = (String)super.getValue();
      return v2;
   }

   public ModeSetting onAction(Runnable action) {
      Setting result = super.onAction(() -> {
         action.run();

         this.f221 = (String)super.getValue();
         
      });

      return (ModeSetting)result;
   }

   public ModeSetting setVisible(Supplier<Boolean> value) {
      ModeSetting result = (ModeSetting)super.setVisible(value);
      int temp2 = v2 + 1;

      int temp1 = v2 * temp2;

      return result;
   }

   public ModeSetting onSetVisible(Runnable action) {

      ModeSetting result = (ModeSetting)super.onSetVisible(action);

      return result;
   }

   public 

      for (String v3 : this.values) {
         if (v3.equalsIgnoreCase(this.getValue())) {
            return v1;
         }

         v1++;
      }

      return 0;
   }

   
}
