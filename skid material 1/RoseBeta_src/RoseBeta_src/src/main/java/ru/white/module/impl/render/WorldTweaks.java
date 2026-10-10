package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventRender2D;

@ModuleAnnotation(
   name = "World Tweaks",
   desc = "Мелкие настройки отображения мира",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class WorldTweaks extends Module {
   public tf tintColor;
   public BooleanSetting changeWorldColor;
   public ModeSetting worldColorMode;
   public SliderSetting fog;
   public tf worldColor;
   public SliderSetting time;
   public BooleanSetting times = new BooleanSetting(this, "Менять время", true);
   public BooleanSetting fogs = new BooleanSetting(this, "Менять туман", true);
   public ModeSetting typeColor;

   public 

      return this.worldColorMode.is("Тема") ? up.getClientColor1(1) : this.worldColor.getValue();
   }

   public int getColor() {
      if (this.typeColor.is("Тема")) {
         int v3 = up.getClientColor1(1);

         return v3;
      } else {
         Integer result = this.tintColor.getValue();

         return result;
      }
   }

   public static ml get() {

      return uq.get(ml.class);
   }

   public WorldTweaks() {
      this.time = new SliderSetting(this, "Время", 12.0F, 0.0F, 24.0F, 1.0F).setVisible(() -> {

         return this.times.getValue();
      });
      this.fog = new SliderSetting(this, "Дистанция тумана", 100.0F, 2.0F, 200.0F, 1.0F)
         .setVisible(() -> {
            Boolean result = this.fogs.getValue();

            return result;
         });
      this.typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> this.typeColor.is("Свой"));
      this.changeWorldColor = new BooleanSetting(this, "Цвет мира", false);
      this.worldColorMode = new ModeSetting(this, "Режим цвета мира", "Тема", "Свой").setVisible(() -> {

         return this.changeWorldColor.getValue();
      });
      this.worldColor = new tf(this, "Цвет освещения", -1).setVisible(() -> {
         if (this.changeWorldColor.getValue()) {

            if (this.worldColorMode.is("Свой")) {
               return true;
            }
         }

         return false;
      });
   }

   public boolean usesWorldColor() {

      return this.isEnabled() && this.changeWorldColor.getValue();
   }

   @EventTarget
   public void onFog(EventRender2D e) {
      Boolean result = this.fogs.getValue();

      if (result) {
         e.setDistance(this.fog.getValue());
         e.setColor(this.getColor());
         e.cancel();
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (EventItemUse != null) {

         if (mc.field_1773 != null) {
            LightmapTextureManager v1 = mc.field_1773.method_22974();
            if (v1 != null) {
               v1.method_3314();

               
            }
         }
      }
   }

   
}
