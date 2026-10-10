package ru.white.module.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class MultiSetting extends Setting<Map<String, BooleanSetting>> {
   public boolean opened;
   private final Map<String, BooleanSetting> f223 = new LinkedHashMap<>();
   public Animation animation = new Animation();

   public Collection<BooleanSetting> getValues() {
      Map result = this.getValue();

      return result.values();
   }

   public MultiSetting setVisible(Supplier<Boolean> value) {
      Setting result = super.setVisible(value);

      return (MultiSetting)result;
   }

   public boolean isAnyTrue() {
      Iterator result = this.f223.values().iterator();

      Iterator v1 = result;

      while (v1.hasNext()) {
         BooleanSetting v2 = (BooleanSetting)v1.next();
         if (v2.getValue()) {
            return true;
         }
      }

      return false;
   }

   public String getNames() {
      ArrayList v1 = new ArrayList();
      Collection result = this.getValues();

      for (BooleanSetting v3 : result) {
         if (v3.getValue()) {

            v1.add(v3.getName());
         }
      }

      return String.join(", ", v1);
   }

   public BooleanSetting get(String name) {

      return this.f223.get(name.toLowerCase());
   }

   public MultiSetting onAction(Runnable action) {
      Setting result = super.onAction(action);

      return (MultiSetting)result;
   }

   public MultiSetting onSetVisible(Runnable action) {
      return (MultiSetting)super.onSetVisible(action);
   }

   public boolean getValue(String name) {
      BooleanSetting v2 = this.get(name);
      boolean result;
      if (v2 != null && v2.getValue() && this.getVisible().get()) {
         result = true;

         
      } else {
         result = false;
      }

      return result;
   }

   public MultiSetting(Module parent, String name, BooleanSetting... values) {
      super(parent, name, new LinkedHashMap<>());

      for (BooleanSetting v7 : values) {
         this.getValue().put(v7.getName().toLowerCase(), v7);
         this.f223.put(v7.getName().toLowerCase(), v7);
      }
   }

   @Generated
   @Override
   public Animation getAnimation() {

      return this.animation;
   }

   
}
