package ru.white.module.setting;

import java.util.function.Supplier;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class Setting<Value> implements mf, IMinecraft {
   private Value value;
   private Runnable f206;
   private final Animation f210;
   private Runnable f207;
   private Module f209;
   public final String name;
   private final ru.white.utils.animation.satoshi.Animation f211;
   private Supplier<Boolean> f208 = () -> {

      return true;
   };

   @Generated
   public Runnable getOnSetVisible() {
      return this.f207;
   }

   public Setting(Module parent, String name, Value value) {
      this.f210 = new Animation();
      this.f211 = new ep(300, 1.0);
      this.f209 = parent;
      this.name = name;
      this.value = (Value)value;
      parent.getSettings().add(this);
   }

   @Generated
   public Runnable getOnAction() {
      return this.f206;
   }

   public Setting(String name, Value value) {
      this.f210 = new Animation();
      this.f211 = new ep(300, 1.0);
      this.name = name;
      this.value = (Value)value;
   }

   @Generated
   public Module getParent() {
      return this.f209;
   }

   @Generated
   public Animation getAnimation() {

      return this.f210;
   }

   @Generated
   public ru.white.utils.animation.satoshi.Animation getAnimation1() {

      return this.f211;
   }

   public Setting<?> onSetVisible(Runnable action) {
      this.f207 = action;

      return this;
   }

   @Generated
   public String getName() {

      return this.name;
   }

   public Setting<?> onAction(Runnable action) {

      this.f206 = action;
      return this;
   }

   @Generated
   public Value getValue() {

      return this.value;
   }

   public Setting<?> set(Value value) {
      this.value = (Value)value;
      if (mc.field_1687 != null && mc.field_1724 != null && this.f206 != null) {
         int temp1 = v2 + 1;

         this.f206.run();
      }

      return this;
   }

   @Generated
   public Supplier<Boolean> getVisible() {

      return this.f208;
   }

   @Override
   public Setting<?> setVisible(Supplier<Boolean> value) {
      this.f208 = value;
      if (mc.field_1687 != null && mc.field_1724 != null && this.f207 != null) {

         this.f207.run();
      }

      return this;
   }

   
}
