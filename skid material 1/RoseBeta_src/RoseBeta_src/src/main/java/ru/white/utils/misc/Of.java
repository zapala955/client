package ru.white.utils.misc;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Of extends Setting<Integer> {
   public Animation animation;
   private Integer f212;
   public final boolean allowMouse;

   public SettingVisibility setVisible(Supplier<Boolean> value) {
      SettingVisibility result = (SettingVisibility)super.setVisible(value);
      int v2 = 1178988716;

      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      return result;
   }

   public Of(Module parent, String name) {
      super(parent, name, -1);
      this.animation = new ep(300, 1.0);
      this.allowMouse = true;
      this.f212 = -1;
   }

   public Of(Module parent, String name, Integer value) {
      super(parent, name, value);
      this.animation = new ep(300, 1.0);
      this.allowMouse = true;
      this.f212 = value;
   }

   public SettingVisibility set(Integer value) {
      SettingVisibility result = (SettingVisibility)super.set(value);

      SettingVisibility v2 = result;
      this.f212 = (Integer)super.getValue();
      return v2;
   }

   public SettingVisibility onSetVisible(Runnable action) {

      return (SettingVisibility)super.onSetVisible(action);
   }

   public SettingVisibility onAction(Runnable action) {

      return (SettingVisibility)super.onAction(() -> {
         action.run();

         this.f212 = (Integer)super.getValue();
      });
   }

   public Integer getValue() {
      if (this.f212 == null) {
         Object temp1 = super.getValue();

         temp1 = (Integer)temp1;

         this.f212 = (Integer)temp1;
      }

      return this.f212;
   }

   public Of(Module parent, String name, Integer value, boolean allowMouse) {
      super(parent, name, value);
      this.animation = new ep(300, 1.0);
      this.allowMouse = allowMouse;
      this.f212 = value;
   }

   public Of(Module parent, String name, boolean allowMouse) {
      super(parent, name, -1);
      this.animation = new ep(300, 1.0);
      this.allowMouse = allowMouse;
      this.f212 = -1;
   }

   public Integer get() {
      if (this.f212 == null) {
         Object temp1 = super.getValue();

         this.f212 = (Integer)temp1;
      }

      return this.f212;
   }

   
}
