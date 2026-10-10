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
import ru.white.utils.render.ds;
import ru.white.event.impl.EventMotion;
import ru.white.event.impl.EventSync;

@ModuleAnnotation(
   name = "Glass Hands",
   category = Category.RENDER,
   desc = "Делает руки и предметы стеклянными"
)
@Environment(EnvType.CLIENT)
public class GlassHands extends Module {
   public BooleanSetting enableBlur = new BooleanSetting(this, "Блюр", true);
   public tf fireCustomColor;
   public SliderSetting shimmerWidth;
   public SliderSetting edgeSoftness;
   public SliderSetting tintIntensity;
   public SliderSetting iceIntensity;
   public SliderSetting fireSpeed;
   public SliderSetting shimmerPeriod;
   public ModeSetting edgeGlowColorMode;
   public SliderSetting fireHeight;
   public BooleanSetting shimmer;
   private static GlassHands instance;
   public BooleanSetting enableIce;
   public SliderSetting edgeGlowIntensity;
   public SliderSetting fireRadius;
   public BooleanSetting fireBurnThrough;
   public ModeSetting fireColorMode;
   public BooleanSetting enableFire;
   public SliderSetting fireIntensity;
   public ModeSetting typeColor;
   public BooleanSetting enableEdgeGlow;
   public SliderSetting fireTrail;
   public SliderSetting blurIterations;
   public tf edgeGlowCustomColor;
   public tf tintColor;
   public SliderSetting refractReach;
   public SliderSetting saturation;
   public SliderSetting blurRadius = new SliderSetting(this, "Сила размытия", 2.5F, 1.0F, 5.0F, 0.1F).setVisible(() -> {

      return this.enableBlur.getValue();
   });

   @Generated
   public ModeSetting getTypeColor() {

      return this.typeColor;
   }

   @Generated
   public SliderSetting getRefractReach() {
      return this.refractReach;
   }

   @Generated
   public tf getTintColor() {

      return this.tintColor;
   }

   @Generated
   public tf getEdgeGlowCustomColor() {

      return this.edgeGlowCustomColor;
   }

   @Generated
   public ModeSetting getFireColorMode() {

      return this.fireColorMode;
   }

   @Override
   protected void onDisable() {
      ds result = ds.getInstance();

      result.setEnabled(false);
   }

   @Generated
   public BooleanSetting getEnableEdgeGlow() {
      return this.enableEdgeGlow;
   }

   @Generated
   public tf getFireCustomColor() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.fireCustomColor;
   }

   @Generated
   public SliderSetting getShimmerPeriod() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.shimmerPeriod;
   }

   public int getColor() {
      if (this.typeColor.is("Тема")) {

         return up.getClientColor1(1);
      } else {

         return this.tintColor.getValue();
      }
   }

   @Generated
   public BooleanSetting getShimmer() {

      return this.shimmer;
   }

   @Generated
   public BooleanSetting getEnableIce() {

      return this.enableIce;
   }

   @Generated
   public SliderSetting getSaturation() {
      return this.saturation;
   }

   @Generated
   public BooleanSetting getEnableBlur() {

      return this.enableBlur;
   }

   @Generated
   public SliderSetting getBlurRadius() {

      return this.blurRadius;
   }

   @Generated
   public BooleanSetting getFireBurnThrough() {

      return this.fireBurnThrough;
   }

   @Generated
   public BooleanSetting getEnableFire() {

      return this.enableFire;
   }

   public void applyRendererSettings() {
      ds v1 = ds.getInstance();
      v1.setBlurEnabled(this.enableBlur.getValue());
      v1.setBlurRadius(this.blurRadius.getValue());
      v1.setBlurIterations(this.blurIterations.getValue().intValue());
      v1.setSaturation(this.saturation.getValue());
      v1.setEdgeSoftness(this.edgeSoftness.getValue());
      if (this.enableIce.getValue()) {
         v1.setReflect(true);
         v1.setIceIntensity(this.iceIntensity.getValue());
         v1.setFrostScale(this.refractReach.getValue());
         v1.setCrackIntensity(0.5F);
      } else {
         v1.setReflect(false);
         v1.setIceIntensity(0.0F);
         v1.setFrostScale(0.0F);
         v1.setCrackIntensity(0.0F);
      }

      v1.setSmokeAmount(0.0F);
      v1.setFireEnabled(this.enableFire.getValue());
      if (this.enableFire.getValue()) {
         v1.setFireStrength(this.fireIntensity.getValue());
         v1.setFireRadius(this.fireRadius.getValue());
         v1.setFireSpeed(this.fireSpeed.getValue());
         v1.setFireHeight(this.fireHeight.getValue());
         v1.setFireTrailStrength(this.fireTrail.getValue());
         v1.setFireFillMode(this.fireBurnThrough.getValue());
         if (this.fireColorMode.is("Предмет")) {
            v1.setFireColorMix(0.0F);
         } else if (this.fireColorMode.is("Тема")) {
            v1.setFireColor(up.getClientColor1(1) & 16777215);
            v1.setFireColorMix(1.0F);
         } else {
            v1.setFireColor(this.fireCustomColor.getValue() & 16777215);
            v1.setFireColorMix(1.0F);
         }
      }

      if (this.enableBlur.getValue()) {
         v1.setTintColor(this.getColor());
         v1.setTintIntensity(this.tintIntensity.getValue());
      } else {
         v1.setTintColor(0);

         v1.setTintIntensity(0.0F);
      }

      v1.setOutlineEnabled(this.enableEdgeGlow.getValue());
      if (this.enableEdgeGlow.getValue()) {
         float v2 = this.edgeGlowIntensity.getValue() * 20.0F;
         v1.setOutlineGlowStrength(v2);

         if (this.edgeGlowColorMode.is("Тема")) {
            v1.setOutlineUseItemColor(false);
            v1.setOutlineColor(-16777216 | up.getClientColor1(1) & 16777215);
         } else {
            v1.setOutlineUseItemColor(false);
            v1.setOutlineColor(-16777216 | this.edgeGlowCustomColor.getValue() & 16777215);
         }

         v1.setShimmerEnabled(this.shimmer.getValue());
         v1.setShimmerWidth(this.shimmerWidth.getValue());
         v1.setShimmerPeriodSec(this.shimmerPeriod.getValue());
      }
   }

   @Generated
   public SliderSetting getTintIntensity() {

      return this.tintIntensity;
   }

   @Generated
   public SliderSetting getFireSpeed() {

      return this.fireSpeed;
   }

   public static GlassHands getInstance() {
      return instance;
   }

   @Generated
   public SliderSetting getEdgeGlowIntensity() {
      return this.edgeGlowIntensity;
   }

   @Generated
   public ModeSetting getEdgeGlowColorMode() {
      return this.edgeGlowColorMode;
   }

   @Generated
   public SliderSetting getShimmerWidth() {

      return this.shimmerWidth;
   }

   public GlassHands() {
      this.blurIterations = new SliderSetting(this, "Качество", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> this.enableBlur.getValue());
      this.saturation = new SliderSetting(this, "Насыщенность", 0.0F, 0.0F, 2.0F, 0.1F).setVisible(() -> {

         return this.enableBlur.getValue();
      });
      this.tintIntensity = new SliderSetting(this, "Сила оттенка", 0.2F, 0.0F, 1.0F, 0.01F)
         .setVisible(() -> this.enableBlur.getValue());
      this.edgeSoftness = new SliderSetting(this, "Сглаживание краёв", 0.0F, 0.0F, 28.0F, 1.0F)
         .setVisible(() -> {

            return this.enableBlur.getValue();
         });
      this.typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> {

         return this.typeColor.is("Свой");
      });
      this.enableIce = new BooleanSetting(this, "Искажение", false);
      this.iceIntensity = new SliderSetting(this, "Сила искажения", 1.0F, 0.0F, 2.0F, 0.05F)
         .setVisible(() -> this.enableIce.getValue());
      this.refractReach = new SliderSetting(this, "Ширина искажения", 25.0F, 0.0F, 80.0F, 1.0F)
         .setVisible(() -> this.enableIce.getValue());
      this.enableFire = new BooleanSetting(this, "Огонь", false);
      this.fireIntensity = new SliderSetting(this, "Сила огня", 1.18F, 0.2F, 2.0F, 0.02F).setVisible(() -> this.enableFire.getValue());
      this.fireRadius = new SliderSetting(this, "Размер пламени", 15.0F, 4.0F, 40.0F, 1.0F).setVisible(() -> {

         return this.enableFire.getValue();
      });
      this.fireSpeed = new SliderSetting(this, "Скорость огня", 1.1F, 0.2F, 3.0F, 0.05F).setVisible(() -> {

         Boolean result = this.enableFire.getValue();

         return result;
      });
      this.fireHeight = new SliderSetting(this, "Высота дыма", 28.0F, 5.0F, 56.0F, 1.0F).setVisible(() -> {

         return this.enableFire.getValue();
      });
      this.fireTrail = new SliderSetting(this, "Сила дыма", 1.16F, 0.0F, 2.0F, 0.02F).setVisible(() -> {
         Boolean result = this.enableFire.getValue();

         return result;
      });
      this.fireColorMode = new ModeSetting(this, "Цвет огня", "Предмет", "Тема", "Свой").setVisible(() -> {

         return this.enableFire.getValue();
      });
      this.fireCustomColor = new tf(this, "Свой цвет огня", -32730)
         .setVisible(() -> {
            boolean result;
            if (this.enableFire.getValue() && this.fireColorMode.is("Свой")) {

               result = true;
            } else {
               result = false;
            }

            return result;
         });
      this.fireBurnThrough = new BooleanSetting(this, "Прожиг", false).setVisible(() -> {

         return this.enableFire.getValue();
      });
      this.enableEdgeGlow = new BooleanSetting(this, "Свечение краёв", true);
      this.edgeGlowIntensity = new SliderSetting(this, "Сила свечения", 0.2F, 0.0F, 1.0F, 0.01F)
         .setVisible(() -> {

            return this.enableEdgeGlow.getValue();
         });
      this.edgeGlowColorMode = new ModeSetting(this, "Цвет свечения", "Тема", "Свой")
         .setVisible(() -> this.enableEdgeGlow.getValue());
      this.edgeGlowCustomColor = new tf(this, "Свой цвет свечения", -16711681)
         .setVisible(() -> {
            Boolean result = this.enableEdgeGlow.getValue() && this.edgeGlowColorMode.is("Свой");
            int temp2 = v1 + 1;

            return result;
         });
      this.shimmer = new BooleanSetting(this, "Шиммер", true).setVisible(() -> {
         Boolean result = this.enableEdgeGlow.getValue();

         return result;
      });
      this.shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F)
         .setVisible(() -> {
            boolean result;
            if (this.enableEdgeGlow.getValue() && this.shimmer.getValue()) {
               result = true;
               
            } else {
               result = false;
            }

            return result;
         });
      this.shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> {
         if (this.enableEdgeGlow.getValue()) {
            boolean result = this.shimmer.getValue();

            if (result) {
               return true;
            }
         }

         return false;
      });
      instance = this;
   }

   @Generated
   public SliderSetting getEdgeSoftness() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.edgeSoftness;
   }

   @Generated
   public SliderSetting getFireHeight() {

      return this.fireHeight;
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      if (this.isEnabled()) {
         ds v2 = ds.getInstance();

         v2.invalidate();
         v2.setEnabled(true);

         this.applyRendererSettings();
      }
   }

   @Generated
   public SliderSetting getIceIntensity() {

      return this.iceIntensity;
   }

   @EventTarget
   public void onGlassHandsRender(EventMotion event) {
      if (this.isEnabled()) {
         ds v2 = ds.getInstance();
         this.applyRendererSettings();

         ac.bc v6 = event.getPhase();

         if (v6 == ac.bc.PRE) {

            v2.captureSceneBeforeHands();
         } else if (event.getPhase() == ac.bc.POST) {
            v2.captureSceneAfterHands();
            v2.renderGlassEffect();
         }
      }
   }

   @Generated
   public SliderSetting getFireIntensity() {
      return this.fireIntensity;
   }

   @Generated
   public SliderSetting getFireRadius() {

      return this.fireRadius;
   }

   @Generated
   public SliderSetting getFireTrail() {

      return this.fireTrail;
   }

   @Override
   protected void onEnable() {
      ds v1 = ds.getInstance();
      v1.invalidate();
      v1.setEnabled(true);
      this.applyRendererSettings();
   }

   @Generated
   public SliderSetting getBlurIterations() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.blurIterations;
   }

   
}
