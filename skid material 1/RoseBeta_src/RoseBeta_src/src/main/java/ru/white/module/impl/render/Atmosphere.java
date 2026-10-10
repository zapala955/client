package ru.white.module.impl.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "Atmosphere",
   category = Category.RENDER,
   desc = "Объемный туман, дождь и процедурные молнии"
)
@Environment(EnvType.CLIENT)
public class Atmosphere extends Module {
   public final BooleanSetting rain;
   public final BooleanSetting nightHaze;
   public final BooleanSetting hazeFollowGround;
   public final SliderSetting flashStrength;
   public final BooleanSetting fog = new BooleanSetting(this, "Туман", true);
   public final BooleanSetting lightning;
   public final SliderSetting turbulence;
   public final tf hazeColor;
   public final BooleanSetting ripples;
   public final SliderSetting ambientStrength;
   public final BooleanSetting stormFog;
   public final BooleanSetting syncWithSky;
   public final SliderSetting hazeThickness;
   public final ModeSetting rainType;
   public final SliderSetting strikeDistance;
   private static qi instance;
   public final SliderSetting rippleStrength;
   public final SliderSetting boltWidth;
   public final BooleanSetting hazeNightOnly;
   public final SliderSetting rainSpeed;
   public final tf boltColor;
   public final tf fogColor;
   public final SliderSetting stormPower;
   public final SliderSetting hazeAmount;
   public final SliderSetting skySync;
   public final SliderSetting strikeRate;
   public final SliderSetting sunScatter;
   public final SliderSetting hazeOffset;
   public final SliderSetting rainDensity;
   public final SliderSetting hazeMoonScatter;
   public final BooleanSetting advanced;
   public final ModeSetting fogColorMode;
   public final SliderSetting hazeBanks;
   public final SliderSetting fogHeight;
   public final SliderSetting fogDensity;
   public final tf rainColor;
   public final SliderSetting hazeGlow;
   public final SliderSetting sunsetBoost;
   public final ModeSetting stormSource;
   public final SliderSetting boltBranches;
   public final SliderSetting rainOpacity;
   public final SliderSetting hazeLevel;
   public final ModeSetting hazeColorMode;

   public static qi getInstance() {
      return instance;
   }

   @Generated
   public BooleanSetting getRain() {

      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.rain;
   }

   @Generated
   public tf getBoltColor() {

      return this.boltColor;
   }

   @Generated
   public SliderSetting getSunsetBoost() {

      return this.sunsetBoost;
   }

   @Generated
   public SliderSetting getBoltWidth() {

      return this.boltWidth;
   }

   @Generated
   public ModeSetting getStormSource() {

      return this.stormSource;
   }

   @Generated
   public ModeSetting getRainType() {
      return this.rainType;
   }

   @Generated
   public BooleanSetting getSyncWithSky() {
      return this.syncWithSky;
   }

   @Generated
   public tf getFogColor() {

      return this.fogColor;
   }

   @Generated
   public SliderSetting getStrikeDistance() {

      return this.strikeDistance;
   }

   @Generated
   public SliderSetting getHazeThickness() {
      return this.hazeThickness;
   }

   @Generated
   public SliderSetting getRainDensity() {

      return this.rainDensity;
   }

   @Generated
   public SliderSetting getBoltBranches() {

      return this.boltBranches;
   }

   @Generated
   public SliderSetting getHazeOffset() {

      return this.hazeOffset;
   }

   @Generated
   public SliderSetting getHazeAmount() {

      return this.hazeAmount;
   }

   @Generated
   public BooleanSetting getRipples() {

      return this.ripples;
   }

   public boolean usesFog() {
      return this.fog.getValue();
   }

   @Generated
   public BooleanSetting getNightHaze() {

      return this.nightHaze;
   }

   @Generated
   public SliderSetting getTurbulence() {

      return this.turbulence;
   }

   @Generated
   public BooleanSetting getStormFog() {

      return this.stormFog;
   }

   @Generated
   public SliderSetting getFlashStrength() {

      return this.flashStrength;
   }

   @Generated
   public ModeSetting getHazeColorMode() {

      return this.hazeColorMode;
   }

   @Generated
   public ModeSetting getFogColorMode() {

      return this.fogColorMode;
   }

   public boolean usesAtmosphere() {

      if (this.isEnabled()) {
         if (this.usesFog() || this.usesLightning()) {
            return true;
         }

         if (this.usesRain() || this.usesNightHaze()) {
            return true;
         }
      }

      return false;
   }

   @Generated
   public SliderSetting getRippleStrength() {

      return this.rippleStrength;
   }

   public boolean usesNightHaze() {
      boolean result = this.nightHaze.getValue();

      return result;
   }

   public boolean usesRain() {

      boolean result = this.rain.getValue();

      return result;
   }

   @Generated
   public SliderSetting getFogDensity() {

      return this.fogDensity;
   }

   @Generated
   public tf getHazeColor() {

      return this.hazeColor;
   }

   @Generated
   public BooleanSetting getHazeFollowGround() {

      return this.hazeFollowGround;
   }

   @Generated
   public SliderSetting getStormPower() {

      return this.stormPower;
   }

   @Generated
   public SliderSetting getHazeBanks() {

      return this.hazeBanks;
   }

   @Generated
   public SliderSetting getHazeGlow() {

      return this.hazeGlow;
   }

   @Generated
   public BooleanSetting getAdvanced() {
      return this.advanced;
   }

   @Generated
   public SliderSetting getRainSpeed() {

      return this.rainSpeed;
   }

   @Generated
   public SliderSetting getStrikeRate() {

      return this.strikeRate;
   }

   public boolean usesSnow() {
      int temp3 = v1 + 1;

      return "Снег".equals(this.rainType.getValue());
   }

   @Generated
   public SliderSetting getRainOpacity() {
      return this.rainOpacity;
   }

   @Generated
   public SliderSetting getFogHeight() {
      return this.fogHeight;
   }

   @Generated
   public SliderSetting getSkySync() {

      return this.skySync;
   }

   @Generated
   public BooleanSetting getHazeNightOnly() {

      return this.hazeNightOnly;
   }

   @Generated
   public SliderSetting getHazeLevel() {

      return this.hazeLevel;
   }

   @Generated
   public SliderSetting getSunScatter() {

      return this.sunScatter;
   }

   @Generated
   public tf getRainColor() {

      return this.rainColor;
   }

   @Generated
   public BooleanSetting getLightning() {

      return this.lightning;
   }

   @Generated
   public SliderSetting getHazeMoonScatter() {
      return this.hazeMoonScatter;
   }

   public Atmosphere() {
      this.nightHaze = new BooleanSetting(this, "Ночная дымка", false);
      this.rain = new BooleanSetting(this, "Осадки", true);
      this.rainType = new ModeSetting(this, "Тип осадков", "Дождь", "Снег").setVisible(this.rain::getValue);
      this.lightning = new BooleanSetting(this, "Молнии", true);
      this.advanced = new BooleanSetting(this, "Расширенные", false);
      this.fogColorMode = new ModeSetting(this, "Цвет тумана", "По умолчанию", "Тема", "Свой")
         .setVisible(() -> this.advanced.getValue() && this.fog.getValue());
      this.fogColor = new tf(this, "Свой цвет тумана", -6642232).setVisible(() -> {
         if (this.advanced.getValue() && this.fog.getValue()) {

            if (this.fogColorMode.is("Свой")) {
               return true;
            }
         }

         return false;
      });
      this.fogDensity = new SliderSetting(this, "Плотность тумана", 0.55F, 0.05F, 2.0F, 0.01F)
         .setVisible(() -> {
            if (this.advanced.getValue()) {
               Boolean result = this.fog.getValue();

               if (result) {
                  return true;
               }
            }

            return false;
         });
      this.fogHeight = new SliderSetting(this, "Высота тумана", 48.0F, 8.0F, 160.0F, 1.0F).setVisible(() -> {
         Boolean result = this.advanced.getValue();

         return result && this.fog.getValue();
      });
      this.ambientStrength = new SliderSetting(this, "Освещение тумана", 0.5F, 0.0F, 2.0F, 0.05F)
         .setVisible(() -> {
            boolean result;
            if (this.advanced.getValue() && this.fog.getValue()) {
               result = true;
               
            } else {
               result = false;
            }

            return result;
         });
      this.syncWithSky = new BooleanSetting(this, "Синхронизация с небом", true).setVisible(() -> {
         Boolean result = this.advanced.getValue();

         return result && this.fog.getValue();
      });
      this.skySync = new SliderSetting(this, "Сила синхронизации", 0.65F, 0.0F, 1.0F, 0.05F)
         .setVisible(() -> {
            if (this.advanced.getValue() && this.fog.getValue()) {

               if (this.syncWithSky.getValue()) {
                  return true;
               }
            }

            return false;
         });
      this.sunsetBoost = new SliderSetting(this, "Усиление заката", 1.4F, 0.0F, 3.0F, 0.05F)
         .setVisible(() -> {
            boolean result = this.advanced.getValue();

            return result && this.fog.getValue() && this.syncWithSky.getValue();
         });
      this.turbulence = new SliderSetting(this, "Турбулентность", 0.35F, 0.0F, 1.0F, 0.05F)
         .setVisible(() -> {
            boolean result;
            if (this.advanced.getValue() && this.fog.getValue()) {

               result = true;
            } else {
               result = false;
            }

            Boolean v4 = result;

            return v4;
         });
      this.sunScatter = new SliderSetting(this, "Рассеивание солнца", 0.8F, 0.0F, 2.0F, 0.05F)
         .setVisible(() -> this.advanced.getValue() && this.fog.getValue());
      this.stormFog = new BooleanSetting(this, "Штормовой туман", false).setVisible(() -> {

         return this.advanced.getValue() && this.fog.getValue();
      });
      this.hazeColorMode = new ModeSetting(this, "Цвет дымки", "По умолчанию", "Тема", "Свой")
         .setVisible(() -> {
            if (this.advanced.getValue()) {

               if (this.nightHaze.getValue()) {
                  return true;
               }
            }

            return false;
         });
      this.hazeColor = new tf(this, "Свой цвет дымки", -4930336).setVisible(() -> {
         if (this.advanced.getValue() && this.nightHaze.getValue()) {
            boolean result = this.hazeColorMode.is("Свой");
            int temp2 = v1 + 1;

            if (result) {
               return true;
            }
         }

         return false;
      });
      this.hazeAmount = new SliderSetting(this, "Количество дымки", 0.6F, 0.05F, 2.0F, 0.05F)
         .setVisible(() -> {
            boolean result = this.advanced.getValue();
            int temp1 = v1 * (v1 + 1) % 2;

            if (temp1 != 0) {
               }

            return result && this.nightHaze.getValue();
         });
      this.hazeThickness = new SliderSetting(this, "Толщина дымки", 5.0F, 1.0F, 40.0F, 0.5F).setVisible(() -> {

         return this.advanced.getValue() && this.nightHaze.getValue();
      });
      this.hazeFollowGround = new BooleanSetting(this, "Следовать за землей", true).setVisible(() -> {
         boolean result = this.advanced.getValue();

         return result && this.nightHaze.getValue();
      });
      this.hazeOffset = new SliderSetting(this, "Смещение дымки", 1.5F, -8.0F, 24.0F, 0.5F).setVisible(() -> {

         boolean result;
         if (this.advanced.getValue() && this.nightHaze.getValue() && this.hazeFollowGround.getValue()) {
            result = true;
         } else {
            result = false;
            
         }

         return result;
      });
      this.hazeLevel = new SliderSetting(this, "Уровень дымки", 64.0F, -64.0F, 320.0F, 1.0F).setVisible(() -> {
         if (this.advanced.getValue()) {
            int result = v1 * (v1 + 1);

            if (this.nightHaze.getValue() && !this.hazeFollowGround.getValue()) {
               return true;
            }
         }

         return false;
      });
      this.hazeBanks = new SliderSetting(this, "Клубы дымки", 0.6F, 0.0F, 1.0F, 0.05F).setVisible(() -> {
         boolean v3;
         label20: {
            if (this.advanced.getValue()) {
               v3 = this.nightHaze.getValue();

               if (v3) {
                  v3 = true;
                  break label20;
               }
            }

            v3 = false;
         }

         Boolean v4 = v3;

         return v4;
      });
      this.hazeGlow = new SliderSetting(this, "Свечение дымки", 0.85F, 0.0F, 2.0F, 0.05F).setVisible(() -> {
         if (this.advanced.getValue()) {

            if (this.nightHaze.getValue()) {
               return true;
            }
         }

         return false;
      });
      this.hazeMoonScatter = new SliderSetting(this, "Рассеивание луны", 1.0F, 0.0F, 3.0F, 0.05F)
         .setVisible(() -> {

            return this.advanced.getValue() && this.nightHaze.getValue();
         });
      this.hazeNightOnly = new BooleanSetting(this, "Только ночью", true).setVisible(() -> {

         return this.advanced.getValue() && this.nightHaze.getValue();
      });
      this.rainDensity = new SliderSetting(this, "Плотность осадков", 1.0F, 0.1F, 2.0F, 0.05F)
         .setVisible(() -> {
            if (this.advanced.getValue()) {

               Boolean result = this.rain.getValue();

               if (result) {
                  return true;
               }
            }

            return false;
         });
      this.rainSpeed = new SliderSetting(this, "Скорость осадков", 1.0F, 0.25F, 3.0F, 0.05F)
         .setVisible(() -> {

            Boolean result = this.advanced.getValue() && this.rain.getValue();

            return result;
         });
      this.rainColor = new tf(this, "Цвет осадков", -4009240)
         .setVisible(() -> this.advanced.getValue() && this.rain.getValue());
      this.rainOpacity = new SliderSetting(this, "Прозрачность осадков", 0.55F, 0.1F, 1.0F, 0.05F)
         .setVisible(() -> {

            return this.advanced.getValue() && this.rain.getValue();
         });
      this.ripples = new BooleanSetting(this, "Круги на воде", true).setVisible(() -> {
         if (this.advanced.getValue()) {
            int temp1 = v1 * (v1 + 1) % 2;

            if (temp1 != 0) {
               }

            if (this.rain.getValue() && !this.usesSnow()) {
               return true;
            }
         }

         return false;
      });
      this.rippleStrength = new SliderSetting(this, "Сила кругов", 0.5F, 0.1F, 1.5F, 0.05F).setVisible(() -> {
         Boolean result = this.advanced.getValue();

         return result && this.rain.getValue() && !this.usesSnow() && this.ripples.getValue();
      });
      this.stormSource = new ModeSetting(this, "Источник шторма", "Manual", "Weather").setVisible(() -> {
         if (this.advanced.getValue()) {
            if (this.lightning.getValue()) {
               return true;
            }

            Boolean result = this.fog.getValue();

            if (result && this.stormFog.getValue()) {
               return true;
            }
         }

         return false;
      });
      this.stormPower = new SliderSetting(this, "Сила шторма", 0.5F, 0.0F, 1.0F, 0.05F).setVisible(() -> {
         if (this.advanced.getValue()) {
            boolean result = "Manual".equals(this.stormSource.getValue());

            if (result && (this.lightning.getValue() || this.fog.getValue() && this.stormFog.getValue())) {
               return true;
            }
         }

         return false;
      });
      this.strikeRate = new SliderSetting(this, "Частота ударов", 1.0F, 0.25F, 4.0F, 0.05F).setVisible(() -> {
         Boolean result = this.advanced.getValue();

         return result && this.lightning.getValue();
      });
      this.strikeDistance = new SliderSetting(this, "Дистанция ударов", 220.0F, 60.0F, 500.0F, 10.0F)
         .setVisible(() -> {
            if (this.advanced.getValue()) {
               boolean result = this.lightning.getValue();

               if (result) {
                  return true;
               }
            }

            return false;
         });
      this.boltColor = new tf(this, "Цвет молнии", -3416833).setVisible(() -> {

         Boolean result = this.advanced.getValue();

         return result && this.lightning.getValue();
      });
      this.boltWidth = new SliderSetting(this, "Толщина молнии", 1.6F, 0.5F, 5.0F, 0.1F).setVisible(() -> {
         boolean result = this.advanced.getValue();

         if (result) {

            if (this.lightning.getValue()) {

               return true;
            }
         }

         return false;
      });
      this.boltBranches = new SliderSetting(this, "Ветви молнии", 3.0F, 0.0F, 4.0F, 1.0F).setVisible(() -> {
         boolean result;
         if (this.advanced.getValue() && this.lightning.getValue()) {
            result = true;
         } else {
            result = false;
            
         }

         return result;
      });
      this.flashStrength = new SliderSetting(this, "Вспышка", 1.0F, 0.0F, 2.0F, 0.05F).setVisible(() -> {
         if (this.advanced.getValue()) {

            if (this.lightning.getValue()) {
               return true;
            }
         }

         return false;
      });
      instance = this;
   }

   @Generated
   public BooleanSetting getFog() {

      return this.fog;
   }

   public boolean usesLightning() {
      return this.lightning.getValue();
   }

   @Generated
   public SliderSetting getAmbientStrength() {

      return this.ambientStrength;
   }

   
}
