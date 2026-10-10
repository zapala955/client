package ru.white.module.impl.misc;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import ru.white.event.EventTarget;
import ru.white.utils.StopWatch;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Duel",
   desc = "Автоматически отправляет вызовы на дуэли (ReallyWorld)",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoDuel extends Module {
   private final StopWatch f928;
   private final StopWatch f927;
   private final Map<Integer, String> f930;
   private static final String MENU_SETUP = "setup";
   private double f923;
   private double f921;
   private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
   private static final String MENU_KIT = "kit";
   private final List<String> f924;
   public final ModeSetting mode;
   private int f925;
   private final StopWatch f929;
   private double f922;
   private final StopWatch f926;

   private void m1181(String command) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         ClientPlayNetworkHandler result = mc.field_1724.field_3944;
         String v3;
         if (command.startsWith("/")) {

            v3 = command.substring(1);
         } else {
            v3 = command;
         }

         result.method_45730(v3);
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         List v2 = this.m1182();
         double result = Math.pow(this.f921 - mc.field_1724.method_23317(), 2.0) + Math.pow(this.f922 - mc.field_1724.method_23318(), 2.0);

         double v3 = Math.sqrt(result + Math.pow(this.f923 - mc.field_1724.method_23321(), 2.0));
         if (v3 > 500.0) {
            this.toggle();
         }

         this.f921 = mc.field_1724.method_23317();
         this.f922 = mc.field_1724.method_23318();
         this.f923 = mc.field_1724.method_23321();

         if (this.f927.isReached((6234979201726792180L + -6234979201726791380L) * (long)v2.size())) {
            this.f924.clear();
            this.f925 = 0;
            this.f927.reset();
         }

         this.m1178();
         if (!v2.isEmpty() && this.f926.isReached(7622753761413496749L + -7622753761413495749L)) {
            if (this.f925 >= v2.size()) {
               this.f925 = 0;
            }

            String v5 = (String)v2.get(this.f925);
            if (!this.f924.contains(v5) && !v5.equals(this.m1180())) {
               this.m1181("duel " + v5);
               this.f924.add(v5);
            }

            this.f925++;

            this.f926.reset();
         }
      }
   }

   private int m1179() {
      if (this.mode.is("Щит")) {
         return 0;
      } else if (this.mode.is("Шипы 3")) {
         return 1;
      } else if (this.mode.is("Лук")) {
         return 2;
      } else {

         if (this.mode.is("Тотемы")) {

            return 3;
         } else if (this.mode.is("Нодебафф")) {
            return 4;
         } else if (this.mode.is("Шары")) {
            return 5;
         } else if (this.mode.is("Классик")) {
            return 6;
         } else if (this.mode.is("Читерский рай")) {
            return 7;
         } else {
            return this.mode.is("Незеритка") ? 8 : -1;
         }
      }
   }

   private void m1178() {
      if (mc.field_1724.field_7512 instanceof GenericContainerScreenHandler v1) {
         String v5 = this.f930.get(v1.field_7763);
         if (v5 != null) {
            if (mc.field_1755 != null) {
               mc.method_1507(null);
            }

            if ("kit".equals(v5)) {
               if (this.f928.isReached(-6174695489224663214L + 6174695489224663364L)) {
                  int v3 = this.m1179();
                  if (v3 >= 0) {
                     mc.field_1761.method_2906(v1.field_7763, v3, 0, SlotActionType.field_7794, mc.field_1724);
                     
                  }

                  this.f928.reset();
               }
            } else if ("setup".equals(v5) && this.f929.isReached(2199634606513782065L + -2199634606513781915L)) {
               mc.field_1761.method_2906(v1.field_7763, 0, 0, SlotActionType.field_7794, mc.field_1724);
               this.f929.reset();
            }
         }
      } else {
         this.f930.clear();
      }
   }

   private String m1180() {

      return mc.method_1548().method_1676();
   }

   public AutoDuel() {
      String[] temp5 = new String[9];
      temp5[0] = "Шары";
      temp5[1] = "Щит";
      temp5[2] = "Шипы 3";
      temp5[3] = "Незеритка";
      temp5[4] = "Читерский рай";
      temp5[5] = "Лук";
      temp5[6] = "Классик";
      temp5[7] = "Тотемы";
      temp5[8] = "Нодебафф";
      this.mode = new ModeSetting(this, "Режим", temp5);
      this.f924 = new ArrayList<>();
      this.f925 = 0;
      this.f926 = new StopWatch();
      this.f927 = new StopWatch();
      this.f928 = new StopWatch();
      this.f929 = new StopWatch();
      this.f930 = new HashMap<>();
   }

   @EventTarget
   public void onPacket(EventStep event) {
      if (!event.isSend()) {
         Packet v2 = event.getPacket();
         if (v2 instanceof OpenScreenS2CPacket v3) {
            String v4 = v3.method_17594().getString();

            if (v4.contains("Выбор набора (1/1)")) {
               this.f930.put(v3.method_17592(), "kit");
            } else if (v4.contains("Настройка поединка")) {
               this.f930.put(v3.method_17592(), "setup");
            }
         }

         if (v2 instanceof GameMessageS2CPacket v8) {
            String result = v8.comp_763().getString();

            String v9 = result.toLowerCase();
            if (v9.contains("начало") && v9.contains("через") && v9.contains("секунд!") || v9.isEmpty()) {
               this.toggle();
            }
         }
      }
   }

   private List<String> m1182() {
      ArrayList v1 = new ArrayList();
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         Iterator v2 = mc.field_1724.field_3944.method_2880().iterator();

         while (v2.hasNext()) {
            PlayerListEntry v3 = (PlayerListEntry)v2.next();
            GameProfile v4 = v3.method_2966();
            if (v4 != null) {
               String v5 = v4.name();
               if (v5 != null && pattern.matcher(v5).matches()) {
                  v1.add(v5);
               }
            }
         }

         return v1;
      } else {
         return v1;
      }
   }

   @Override
   protected void onDisable() {

      this.f930.clear();
   }

   
}
