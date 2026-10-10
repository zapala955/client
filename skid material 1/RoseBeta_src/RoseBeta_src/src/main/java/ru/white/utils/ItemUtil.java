package ru.white.utils;

import java.util.Iterator;
import java.util.Objects;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import ru.white.utils.helper.Wu;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventStep;

@Environment(EnvType.CLIENT)
public final class ItemUtil implements IMinecraft {
   public static boolean pvpEnd;
   public static int anarchy;
   public static String server = "Local   ";
   private static final wu pvpWatch = new wu();
   public static long timestamp;
   public static float TPS = 20.0F;

   @Generated
   public static int getAnarchy() {
      int v0 = -2091234488;

      return anarchy;
   }

   private static String m1600() {
      if (!nullCheck() && mc.method_1562() != null && mc.method_1562().method_45734() != null && mc.method_1562().method_52790() != null) {
         String v0 = mc.method_1562().method_45734().field_3761.toLowerCase();

         String v1 = mc.method_1562().method_52790().toLowerCase();
         if (v1.contains("botfilter")) {
            return "FunTime";
         } else if (v1.contains("§6spooky§ccore")) {
            return "SpookyTime";
         } else if (v1.contains("holyworld") || v1.contains("vk.com/idwok")) {
            return "HolyWorld";
         } else {
            return v0.contains("reallyworld") ? "ReallyWorld" : v0;
         }
      } else {
         return "Vanilla";
      }
   }

   public static boolean isPvpMessage(String text) {
      if (text != null && !text.isEmpty()) {
         if (!text.contains("вступили в бой") && !text.contains("режим боя")) {

            if (!text.contains("в бою")) {

               if (!text.contains("вошли в бой") && !text.contains("вход в бой") && !text.contains("войти в pvp") && !text.contains("pvp режим")) {

                  if (!text.contains("пвп режим") && !text.contains("combat log")) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void tick() {
      anarchy = m1601();
      server = m1600();

      pvpEnd = m1602();
      if (inPvp()) {
         pvpWatch.reset();
      }
   }

   private static boolean m1602() {
      if (!nullCheck() && mc.field_1705 != null) {
         BossBarHud result = mc.field_1705.method_1740();

         if (result != null) {
            return mc.field_1705.method_1740().field_2060.values().stream().map(c -> {
               Text var10000x = c.method_5414();

               return var10000x.getString().toLowerCase();
            }).anyMatch(s -> (s.contains("pvp") || s.contains("пвп") || s.contains("боя")) && (s.contains("0") || s.contains("1")));
         }
      }

      return false;
   }

   public static boolean nullCheck() {
      if (mc.field_1724 != null) {
         int v1 = -2007382160;

         if (mc.field_1687 != null) {
            return false;
         }
      }

      return true;
   }

   @Generated
   public static boolean isPvpEnd() {
      int v0 = 266117446;

      return pvpEnd;
   }

   public static String getPvpBossBarText() {
      if (!nullCheck() && mc.field_1705 != null && mc.field_1705.method_1740() != null) {

         Iterator v0 = mc.field_1705.method_1740().field_2060.values().iterator();

         String v2;
         while (true) {
            if (!v0.hasNext()) {
               return null;
            }

            ClientBossBar v1 = (ClientBossBar)v0.next();
            v2 = v1.method_5414().getString();
            String v3 = v2.toLowerCase();
            if (v3.contains("pvp") || v3.contains("пвп") || v3.contains("режим боя") || v3.contains("в бою") || v3.contains("бой:")) {
               break;
            }

            if (v3.contains("combat")) {
               
               break;
            }
         }

         return v2.trim();
      } else {
         return null;
      }
   }

   public static boolean inPvp() {
      boolean result = nullCheck();

      if (!result && mc.field_1705 != null) {

         if (mc.field_1705.method_1740() != null) {
            BossBarHud v3 = mc.field_1705.method_1740();

            return v3.field_2060.values().stream().map(c -> {
               String var10000x = c.method_5414().getString();

               return var10000x.toLowerCase();
            }).anyMatch(s -> {
               if (!s.contains("pvp")) {

                  if (!s.contains("пвп") && !s.contains("режим боя")) {

                     if (!s.contains("в бою")) {

                        if (!s.contains("бой:") && !s.contains("combat")) {
                           return false;
                        }
                     }
                  }
               }

               return true;
            });
         }
      }

      return false;
   }

   public static String getWorldType() {
      return mc.field_1687.method_27983().method_29177().method_12832();
   }

   public static String getPvpTitle() {
      String v0 = getPvpBossBarText();
      if (v0 != null && !v0.isEmpty()) {

         return v0;
      } else {
         return "Режим боя";
      }
   }

   public static boolean isHolyWorld() {

      return server.equals("HolyWorld");
   }

   @Generated
   private ItemUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean isReallyWorld() {
      return server.equals("ReallyWorld");
   }

   private static int m1601() {
      Scoreboard v0 = mc.field_1687.method_8428();
      ScoreboardObjective v1 = v0.method_1189(ScoreboardDisplaySlot.field_45157);
      String v2 = server;
      switch (v2) {
         case "FunTime":
            if (v1 != null) {
               String[] v10 = v1.method_1114().getString().split("-");
               if (v10.length > 1) {
                  return Integer.parseInt(v10[1]);
               }
            }
            break;
         case "HolyWorld":

            for (ScoreboardEntry v5 : v0.method_1184(v1)) {
               String v6 = Team.method_1142(v0.method_1164(v5.comp_2127()), v5.method_55387()).getString();
               if (!v6.isEmpty()) {
                  String v7 = StringUtils.substringBetween(v6, "#", " -◆-");
                  if (v7 != null && !v7.isEmpty()) {
                     return Integer.parseInt(v7.replace(" 1.20", ""));
                  }
               }
            }
      }

      return -1;
   }

   public static boolean isVanilla() {
      return server.equals("Vanilla");
   }

   public static void packet(EventStep e) {
      Packet result = e.getPacket();
      Objects.requireNonNull(result);
      Object v1 = result;
      switch (v1) {
         case WorldTimeUpdateS2CPacket v3:
            long v9 = System.nanoTime();
            float v11 = 20.0F;
            float v12 = v11 * (1.0E9F / (float)(v9 - timestamp));

            float v7 = v12;
            TPS = MathHelper.method_15363(v7, 0.0F, v11);
            timestamp = v9;
            break;
         case GameMessageS2CPacket v4:
            String v10 = v4.comp_763().getString().toLowerCase();
            if (isPvpMessage(v10)) {
               pvpWatch.reset();
            }
            break;
         case OverlayMessageS2CPacket v5:
            String v6 = v5.comp_2279().getString().toLowerCase();
            if (isPvpMessage(v6)) {
               pvpWatch.reset();
            }
            break;
      }
   }

   public static boolean isPvp() {
      if (pvpWatch.finished(1500.0)) {

         if (!inPvp()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isFunTime() {

      return server.equals("FunTime");
   }

   public static boolean isCopyTime() {
      int v0 = 1617145408;
      int temp3 = v0 + 1;

      return server.equals("SpookyTime");
   }

   
}
