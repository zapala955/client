package ru.white.module.setting;

import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class StringSetting extends Setting<String> {
   private String f226;
   private final boolean f225;

   public StringSetting setVisible(Supplier<Boolean> value) {
      StringSetting result = (StringSetting)super.setVisible(value);
      int temp2 = v2 + 1;

      return result;
   }

   public StringSetting(Module parent, String name, String value) {
      super(parent, name, value);
      this.f225 = false;
      this.f226 = value;
   }

   public StringSetting set(String value) {

      StringSetting v2 = (StringSetting)super.set(value);

      this.f226 = (String)super.getValue();
      return v2;
   }

   public StringSetting(Module parent, String name, String value, boolean onlyNumber) {
      super(parent, name, value);
      this.f225 = onlyNumber;
      this.f226 = value;
   }

   public StringSetting onAction(Runnable action) {

      return (StringSetting)super.onAction(() -> {
         action.run();
         String var10001x = (String)super.getValue();
         int v2x = 460352061;

         this.f226 = var10001x;
      });
   }

   public String getValue() {

      if (this.f226 == null) {
         this.f226 = (String)super.getValue();
      }

      return this.f226;
   }

   @Generated
   public String getCachedValue() {
      return this.f226;
   }

   @Generated
   public boolean isOnlyNumber() {

      return this.f225;
   }

   public StringSetting onSetVisible(Runnable action) {

      return (StringSetting)super.onSetVisible(action);
   }

   
}
