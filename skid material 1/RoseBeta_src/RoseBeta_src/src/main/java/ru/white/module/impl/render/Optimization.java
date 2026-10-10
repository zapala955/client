package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "Optimization",
   desc = "Комплексная оптимизация клиента для высокого и стабильного FPS",
   category = Category.RENDER,
   autoEnabled = true
)
@Environment(EnvType.CLIENT)
public class Optimization extends Module {
   public static volatile boolean optParticles = true;
   public BooleanSetting sBlur = new BooleanSetting("Блюр и рендер", true);
   public static volatile boolean optFrustum = true;
   public BooleanSetting sSmartTicks;
   public static volatile boolean optGc = true;
   public BooleanSetting sNameTagsArmor;
   public static volatile boolean optFontCache = true;
   public BooleanSetting sEntityLimit;
   public BooleanSetting sRaycasts;
   public BooleanSetting sFastProj;
   public BooleanSetting sFontCache;
   public BooleanSetting sFrustum;
   public static volatile boolean optNameTagsArmor = true;
   public BooleanSetting sNameTags;
   public static volatile boolean optNameTags = true;
   public BooleanSetting sFastDraw;
   public static volatile boolean optBlur = true;
   public static volatile boolean optGlow = true;
   public static volatile boolean optFastProj = true;
   public static volatile boolean optFastDraw = true;
   public BooleanSetting sEntities = new BooleanSetting("Поиск сущностей", true);
   public static volatile boolean optEntityLimit = true;
   public static volatile boolean active = true;
   public BooleanSetting sGc;
   public MultiSetting features;
   public static volatile boolean optScoreboard = true;
   public BooleanSetting sParticles;
   public static volatile boolean optEntities = true;
   private static ek f711;
   public static volatile boolean optSmartTicks = true;
   public BooleanSetting sScoreboard;
   public BooleanSetting sGlow;
   public static volatile boolean optRaycasts = true;

   @Override
   public void onEnable() {
      super.onEnable();

      this.syncFlags();
   }

   public static ek get() {
      if (f711 == null) {
         int temp1 = v0 + 1;

         f711 = uq.get(ek.class);
      }

      return f711;
   }

   public void syncFlags() {
      boolean v1 = this.isEnabled();
      active = v1;
      if (!v1) {
         optBlur = false;
         optEntities = false;
         optRaycasts = false;
         optGc = false;
         optSmartTicks = false;
         optNameTags = false;
         optFrustum = false;
         optFastProj = false;
         optFontCache = false;
         optParticles = false;
         optNameTagsArmor = false;
         optGlow = false;
         optScoreboard = false;
         optEntityLimit = false;
         optFastDraw = false;
      } else {
         optBlur = this.sBlur.getValue();
         optEntities = this.sEntities.getValue();
         optRaycasts = this.sRaycasts.getValue();
         optGc = this.sGc.getValue();
         optSmartTicks = this.sSmartTicks.getValue();

         optNameTags = this.sNameTags.getValue();
         optFrustum = this.sFrustum.getValue();
         optFastProj = this.sFastProj.getValue();
         optFontCache = this.sFontCache.getValue();
         optParticles = this.sParticles.getValue();
         optNameTagsArmor = this.sNameTagsArmor.getValue();
         optGlow = this.sGlow.getValue();
         optScoreboard = this.sScoreboard.getValue();
         optEntityLimit = this.sEntityLimit.getValue();
         optFastDraw = this.sFastDraw.getValue();
      }
   }

   public static boolean isOptActive(String feature) {
      if (!active) {
         return false;
      } else if (feature != null && !feature.isEmpty()) {
         char v1 = feature.charAt(0);
         if (v1 != 1041 && v1 != 1073) {
            if (v1 != 1055 && v1 != 1087) {
               if (v1 != 1056 && v1 != 1088) {
                  label186:
                  if (v1 != 1057 && v1 != 1089) {
                     if (v1 != 1059) {

                        if (v1 != 1091) {
                           if (v1 != 1054 && v1 != 1086) {
                              if (v1 == 78 || v1 == 110) {
                                 return optNameTags;
                              }

                              if (v1 != 1060 && v1 != 1092) {
                                 if (v1 != 1050 && v1 != 1082) {
                                    if (v1 != 1051 && v1 != 1083) {
                                       if (v1 == 90 || v1 == 122) {
                                          return optFastDraw;
                                       }
                                       break label186;
                                    }

                                    if (feature.startsWith("Лим") || feature.startsWith("лим")) {
                                       return optParticles;
                                    }
                                    break label186;
                                 }

                                 if (feature.startsWith("Кэш") || feature.startsWith("кэш")) {
                                    return optFontCache;
                                 }
                                 break label186;
                              }

                              if (feature.startsWith("Фруст") || feature.startsWith("фруст")) {
                                 return optFrustum;
                              }
                              break label186;
                           }

                           if (feature.contains("Glow") || feature.contains("glow") || feature.contains("Свеч") || feature.contains("свеч")) {
                              return optGlow;
                           }

                           if (feature.contains("NameTag") || feature.contains("nametag")) {
                              return optNameTags;
                           }
                           break label186;
                        }
                     }

                     if (feature.startsWith("Умные тики") || feature.startsWith("умные тики")) {
                        return optSmartTicks;
                     }

                     if (feature.startsWith("Умн") || feature.startsWith("умн")) {
                        return optEntityLimit;
                     }
                  } else {
                     if (feature.startsWith("Сниж") || feature.startsWith("сниж")) {
                        return optGc;
                     }

                     if (feature.startsWith("Скорб") || feature.startsWith("скорб")) {
                        return optScoreboard;
                     }
                  }
               } else if (feature.startsWith("Рейк") || feature.startsWith("рейк")) {
                  return optRaycasts;
               }
            } else if (feature.startsWith("Поиск") || feature.startsWith("поиск")) {
               return optEntities;
            }
         } else {
            if (feature.startsWith("Блюр") || feature.startsWith("блюр")) {
               return optBlur;
            }

            if (feature.startsWith("Быстр") || feature.startsWith("быстр")) {
               return optFastProj;
            }

            if (feature.startsWith("Брон") || feature.startsWith("брон")) {
               return optNameTagsArmor;
            }
         }

         ek v2 = get();
         return v2 != null && v2.isEnabled() && v2.features.getValue(feature);
      } else {
         return true;
      }
   }

   @Override
   public void onDisable() {

      super.onDisable();
      this.syncFlags();
   }

   public Optimization() {
      this.sRaycasts = new BooleanSetting("Рейкасты", true);
      this.sGc = new BooleanSetting("Снижение GC", true);
      this.sSmartTicks = new BooleanSetting("Умные тики", true);
      this.sNameTags = new BooleanSetting("Оптимизация NameTags", true);
      this.sFrustum = new BooleanSetting("Фрустум сущностей", true);
      this.sFastProj = new BooleanSetting("Быстрая проекция", true);
      this.sFontCache = new BooleanSetting("Кэш шрифтов", true);
      this.sParticles = new BooleanSetting("Лимит частиц", true);
      this.sNameTagsArmor = new BooleanSetting("Броня NameTags", true);
      this.sGlow = new BooleanSetting("Оптимизация Glow", true);
      this.sScoreboard = new BooleanSetting("Скорборд и здоровье", true);
      this.sEntityLimit = new BooleanSetting("Умный лимит сущностей", true);
      this.sFastDraw = new BooleanSetting("Zero-GC Draw", true);
      BooleanSetting[] temp5 = new BooleanSetting[15];
      temp5[0] = this.sBlur;
      temp5[1] = this.sEntities;
      temp5[2] = this.sRaycasts;
      temp5[3] = this.sGc;
      temp5[4] = this.sSmartTicks;
      temp5[5] = this.sNameTags;
      temp5[6] = this.sFrustum;
      temp5[7] = this.sFastProj;
      temp5[8] = this.sFontCache;
      temp5[9] = this.sParticles;
      temp5[10] = this.sNameTagsArmor;
      temp5[11] = this.sGlow;
      temp5[12] = this.sScoreboard;
      temp5[13] = this.sEntityLimit;
      temp5[14] = this.sFastDraw;
      this.features = new MultiSetting(this, "Оптимизации", temp5);
      f711 = this;
      this.syncFlags();
      this.features.onAction(this::syncFlags);
   }

   public static boolean isOptActive() {

      return active;
   }

   
}
