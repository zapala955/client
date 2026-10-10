package ru.white.module.impl.misc;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.StringSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Oq;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auto Invest",
   desc = "Автоматический инвестиции в клан по бинду или авто-порогу",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoInvest extends Module {
   private int f931;
   public final SliderSetting autoPercent;
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Бинд", "Авто");
   public final StringSetting triggerAmount;
   public final SettingVisibility investKey = new SettingVisibility(this, "Клавиша инвеста", -1).setVisible(() -> {

      boolean result = this.mode.is("Бинд");

      return result;
   });
   public final SliderSetting bindPercent = new SliderSetting(this, "% от баланса (Бинд)", 50.0F, 1.0F, 100.0F, 1.0F)
      .setVisible(() -> {
         Boolean result = this.mode.is("Бинд");

         return result;
      });

   private int m1183() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         Scoreboard v1 = mc.field_1687.method_8428();
         ScoreboardObjective v2 = v1.method_1189(ScoreboardDisplaySlot.field_45157);
         if (v2 == null) {
            return -1;
         } else {
            Iterator v3 = v1.method_1184(v2).iterator();

            int v6;
            do {
               boolean result = v3.hasNext();

               if (!result) {
                  return -1;
               }

               ScoreboardEntry v4 = (ScoreboardEntry)v3.next();
               String v5 = Team.method_1142(v1.method_1164(v4.comp_2127()), v4.method_55387()).getString();
               v6 = this.m1184(v5);
            } while (v6 <= 0);

            return v6;
         }
      } else {
         return -1;
      }
   }

   @EventTarget
   public void onKey(EventPostMotion e) {
      if (mc.field_1724 != null && e.getKey() != -1) {
         if (this.mode.is("Бинд")) {
            if (e.getKey() == this.investKey.get()) {
               int v2 = this.m1183();
               if (v2 <= 0) {
                  this.m1185("§cНе удалось определить баланс!");
                  return;
               }

               float result = (float)v2;
               float temp1 = this.bindPercent.getValue() / 100.0F;

               int v3 = (int)(result * temp1);
               if (v3 <= 0) {
                  this.m1185("§cСумма инвеста = 0!");
                  return;
               }

               mc.field_1724.field_3944.method_45730("clan invest " + v3);
               int v6 = this.bindPercent.getValue().intValue();
               this.m1185("§aИнвестировано §f" + v3 + " §aмонет (" + v6 + "% от " + v2 + ")");
            }
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.mode.is("Авто")) {
            if (this.f931 > 0) {
               this.f931--;
            } else {

               int v2 = this.m1183();
               if (v2 > 0) {
                  int v3;
                  try {
                     String v4 = this.triggerAmount.getValue().trim();
                     v3 = v4.isEmpty() ? 0 : Integer.parseInt(v4);
                  } catch (NumberFormatException v12) {
                     return;
                  }

                  if (v2 >= v3) {
                     int v13 = (int)((float)v2 * (this.autoPercent.getValue() / 100.0F));
                     if (v13 > 0) {

                        mc.field_1724.field_3944.method_45730("clan invest " + v13);
                        int v7 = this.autoPercent.getValue().intValue();
                        this.m1185(
                           "§aАвто-инвест: §f" + v13 + " §aмонет (" + v7 + "% от " + v2 + ")"
                        );
                        this.f931 = 100;
                     }
                  }
               }
            }
         }
      }
   }

   private 

      if (line == null) {
         return -1;
      } else {
         String v2 = line.replaceAll("§.", "").replaceAll("[\\u00A7].", "").toLowerCase();
         if (!v2.contains("монет")) {
            int result = v5 * (v5 + 1);

            if (!v2.contains("баланс")) {
               return -1;
            }
         }

         String v3 = v2.replaceAll("[^0-9]", "");
         if (v3.isEmpty()) {
            return -1;
         } else {
            try {
               return Integer.parseInt(v3);
            } catch (NumberFormatException v8) {
               return -1;
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.f931 = 0;

      super.onDisable();
      
   }

   private void m1185(String text) {

      StringBuilder v6 = new StringBuilder();

      zp.addChatMessage(v6.append("§6[AutoInvest] ").append((Object)text).toString());
      oq.send(text, oq.qq.INFO);
   }

   public AutoInvest() {
      this.triggerAmount = new StringSetting(this, "Триггер монет", "10000000").setVisible(() -> {

         return this.mode.is("Авто");
      });
      this.autoPercent = new SliderSetting(this, "% от баланса (Авто)", 50.0F, 1.0F, 100.0F, 1.0F)
         .setVisible(() -> {

            return this.mode.is("Авто");
         });
      this.f931 = 0;
   }

   
}
