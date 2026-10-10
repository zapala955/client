package ru.white.module.impl.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.it;
import ru.white.event.impl.EventSync;

@ModuleAnnotation(
   name = "Shader Sky",
   category = Category.RENDER,
   desc = "Shader sky with blur or procedural shader"
)
@Environment(EnvType.CLIENT)
public class ShaderSky extends Module {
   public SliderSetting scale;
   public SliderSetting speed;
   public tf color1;
   private static pk instance;
   public SliderSetting stars;
   public BooleanSetting vanillaSky;
   public ModeSetting typeColor;
   public tf color2;
   public ModeSetting mode;
   public SliderSetting intensity;

   public static int geteColor1() {
      if (getInstance().typeColor.is("Тема")) {
         return up.getClientColor1(1);
      } else {
         Integer result = getInstance().color1.getValue();

         return result;
      }
   }

   @Generated
   public ModeSetting getMode() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.mode;
   }

   @Generated
   public BooleanSetting getVanillaSky() {

      return this.vanillaSky;
   }

   public ShaderSky() {
      String[] temp5 = new String[6];
      temp5[0] = "Aurora";
      temp5[1] = "Night";
      temp5[2] = "Snow";
      temp5[3] = "Sky";
      temp5[4] = "Star";
      temp5[5] = "Glow";
      this.mode = new ModeSetting(this, "Режим", temp5);
      this.intensity = new SliderSetting(this, "Сила", 1.0F, 0.1F, 1.0F, 0.05F);
      this.speed = new SliderSetting(this, "Скорость", 1.0F, 0.1F, 3.0F, 0.1F);
      this.scale = new SliderSetting(this, "Масштаб", 1.1F, 0.45F, 3.0F, 0.05F);
      this.stars = new SliderSetting(this, "Звёзды", 0.75F, 0.0F, 1.0F, 0.05F);
      this.typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
      this.color1 = new tf(this, "Цвет 1", -8788225).setVisible(() -> {
         boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.color2 = new tf(this, "Цвет 2", -34074).setVisible(() -> {
         Boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.vanillaSky = new BooleanSetting(this, "Ванильное небо", false);
      instance = this;
   }

   @Override
   protected void onEnable() {
      it result = it.getInstance();

      it v1 = result;

      v1.invalidate();
      v1.setEnabled(true);
   }

   @Override
   protected void onDisable() {
      it.getInstance().setEnabled(false);
   }

   public static pk getInstance() {

      return instance;
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      if (this.isEnabled()) {
         it v2 = it.getInstance();

         v2.invalidate();
         v2.setEnabled(true);
      }
   }

   @Generated
   public SliderSetting getStars() {

      return this.stars;
   }

   @Generated
   public SliderSetting getSpeed() {

      temp1 %= 2;

      if (temp1 != 0) {
         }

      return this.speed;
   }

   @Generated
   public SliderSetting getScale() {

      return this.scale;
   }

   @Generated
   public tf getColor2() {
      return this.color2;
   }

   public static int geteColor2() {
      if (getInstance().typeColor.is("Тема")) {
         int result = up.getClientColor1(1);

         result = up.multDark(result, 0.25F);

         return result;
      } else {
         return getInstance().color2.getValue();
      }
   }

   @Generated
   public SliderSetting getIntensity() {

      return this.intensity;
   }

   @Generated
   public tf getColor1() {
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.color1;
   }

   @Generated
   public ModeSetting getTypeColor() {

      return this.typeColor;
   }

   
}
