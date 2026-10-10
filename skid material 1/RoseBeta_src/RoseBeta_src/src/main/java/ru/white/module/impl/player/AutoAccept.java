package ru.white.module.impl.player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.RunCommand;
import net.minecraft.text.ClickEvent.SuggestCommand;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Accept",
   category = Category.PLAYER,
   desc = "Автоматически принимает запросы на телепортацию и в клан"
)
@Environment(EnvType.CLIENT)
public class AutoAccept extends Module {
   private long f579;
   private final String[] f580;
   public final ModeSetting mode;
   public final MultiSetting type = new MultiSetting(
      this,
      "Принимать",
      new BooleanSetting("Запрос на ТП", true),
      new BooleanSetting("Запрос в клан", true)
   );
   private boolean f578;
   private final String[] f581;
   private boolean f577;
   public final BooleanSetting onlyFriends;

   @EventTarget
   public void onTick(EventTick e) {
      if (this.f577 && mc.field_1724 != null) {

         mc.field_1724.field_3944.method_45730("tpaccept");
         this.f577 = false;
      }
   }

   private String m547(String message, String keyword) {
      String v3 = message.replaceAll("§.", "");
      int v4 = v3.indexOf(keyword);
      if (v4 <= 0) {

         return null;
      } else {
         String v5 = v3.substring(0, v4).trim();
         int v6 = v5.lastIndexOf(32);
         return v6 >= 0 ? v5.substring(v6 + 1).trim() : v5.trim();
      }
   }

   private boolean m548(String message) {
      String v2 = message.toLowerCase();

      return Arrays.stream(this.f580).anyMatch(trigger -> {

         return v2.contains(trigger.toLowerCase());
      });
   }

   public AutoAccept() {
      this.mode = new ModeSetting(this, "Принимать ТП от", "Друзей", "Всех").setVisible(() -> {
         int temp5 = v1 + 1;

         return this.type.get("Запрос на ТП").getValue();
      });
      this.onlyFriends = new BooleanSetting(
            "Принимать запрос в клан только от друзей", true
         )
         .setVisible(() -> {

            return this.type.get("Запрос в клан").getValue();
         });
      this.f578 = false;
      this.f579 = 0L;
      this.f580 = new String[]{
         "has requested teleport",
         "просит телепортироваться",
         "хочет телепортироваться к вам",
         "просит к вам телепортироваться"
      };
      this.f581 = new String[]{
         "приглашает вас в клан",
         "приглашает Вас в клан",
         "invited you to clan"
      };
   }

   @EventTarget
   public void onPacket(EventStep packetEvent) {
      if (mc.field_1724 != null && mc.field_1687 != null && packetEvent.getPacket() instanceof GameMessageS2CPacket v2) {
         Text v9 = v2.comp_763();
         String v4 = v9.getString();
         if (this.f578) {
            if (System.currentTimeMillis() - this.f579 > -5970218951265494113L + 5970218951265499113L) {
               this.f578 = false;
            } else {
               String v5 = this.m544(v9, "Вступить");
               if (v5 != null) {
                  this.m545(v5);

                  this.f578 = false;
                  return;
               }
            }
         }

         if (this.type.getValue("Запрос на ТП") && this.m548(v4)) {
            if (this.mode.is("Всех")) {
               this.f577 = true;
            } else {
               String v10 = v4.toLowerCase();

               for (String v7 : Client.get().friendManager().getFriends()) {
                  if (v10.contains(v7.toLowerCase())) {
                     this.f577 = true;
                     break;
                  }
               }
            }
         }

         if (this.type.getValue("Запрос в клан") && this.m549(v4)) {
            String v11 = this.m547(v4, "приглашает");
            if (v11 != null && !this.m546(v11)) {
               return;
            }

            String v12 = this.m544(v9, "Вступить");
            if (v12 != null) {
               this.m545(v12);
            } else {
               this.f578 = true;
               this.f579 = System.currentTimeMillis();
            }
         }
      }
   }

   private boolean m549(String message) {
      String v2 = message.toLowerCase();
      Stream result = Arrays.stream(this.f581);

      return result.anyMatch(trigger -> {
         boolean var10000x = v2.contains(trigger.toLowerCase());
         int v2x = 1570028773;
         int temp2 = v2x + 1;

         return var10000x;
      });
   }

   private boolean m546(String playerName) {
      return !this.onlyFriends.getValue() ? true : Client.get().friendManager().isFriend(playerName);
   }

   private String m544(Text text, String buttonText) {
      if (text == null) {
         return null;
      } else {
         if (text.method_10866() != null && text.method_10866().method_10970() != null) {
            ClickEvent v3 = text.method_10866().method_10970();
            if (text.getString().contains(buttonText)) {
               if (v3 instanceof RunCommand v9) {
                  return v9.comp_3506();
               }

               if (v3 instanceof SuggestCommand v10) {
                  return v10.comp_3507();
               }
            }
         }

         List v8 = text.method_10855();
         if (v8 != null) {
            for (Text v5 : v8) {

               String v6 = this.m544(v5, buttonText);
               if (v6 != null) {
                  return v6;
               }
            }
         }

         return null;
      }
   }

   private void m545(String cmd) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (cmd.startsWith("/")) {
            mc.field_1724.field_3944.method_45730(cmd.substring(1));
         } else {
            mc.field_1724.field_3944.method_45730(cmd);
         }
      }
   }

   
}
