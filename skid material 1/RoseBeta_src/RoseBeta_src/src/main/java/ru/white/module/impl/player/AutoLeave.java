package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.BlockUtil;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auto Leave",
   desc = "Данный модуль был сделан акарачком поэтому может работать через жопу",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class AutoLeave extends Module {
   private final BlockUtil f583;
   private long f588;
   public final SliderSetting playerRange;
   public final BooleanSetting onPlayerNearby;
   private boolean f584;
   public final BooleanSetting onPvpEnd;
   private static final long POST_LEAVE_COOLDOWN = 10000L;
   private boolean f585;
   private boolean f586;
   private int f587;
   private static final long ANTI_SPAM_DELAY = 5000L;
   private final BlockUtil f582;
   public final ModeSetting leaveMode = new ModeSetting(this, "Команда", "/hub", "/spawn", "/darena");

   public static bi get() {
      Module result = uq.get(bi.class);

      return (bi)result;
   }

   private void m554(String reason) {
      this.f582.reset();
      this.f588 = System.currentTimeMillis();
      String v2 = this.leaveMode.getValue();
      zp.addChatMessage("§e[AutoLeave] §c" + reason + " §7→ §a" + v2);
      byte v4 = -1;
      switch (v2.hashCode()) {
         case 1503846:
            if (v2.equals("/hub")) {

               v4 = 0;
            }
            break;
         case 1455208620:
            if (v2.equals("/spawn")) {
               v4 = 1;
            }
            break;
         case 1718993426:
            if (v2.equals("/darena")) {
               v4 = 2;
            }
      }

      switch (v4) {
         case 0:
            mc.field_1724.field_3944.method_45730("hub");
            this.f586 = true;
            break;
         case 1:
            mc.field_1724.field_3944.method_45730("spawn");
            this.f586 = true;
            break;
         case 2:
            mc.field_1724.field_3944.method_45730("darena");
            this.f584 = true;
            this.f583.reset();
            this.f587 = 0;
      }
   }

   public AutoLeave() {
      this.onPlayerNearby = new BooleanSetting(this, "Игрок рядом", true);
      this.playerRange = new SliderSetting(this, "Дистанция игрока", 20.0F, 10.0F, 50.0F, 1.0F);
      this.onPvpEnd = new BooleanSetting(this, "Конец PvP", false);
      this.f582 = new BlockUtil();
      this.f583 = new BlockUtil();
      this.f584 = false;
      this.f585 = false;
      this.f586 = false;
      this.f587 = 0;
      this.f588 = 0L;
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.f586 && !this.leaveMode.is("/darena")) {

            double v2 = Math.abs(mc.field_1724.method_23317());
            double v4 = Math.abs(mc.field_1724.method_23321());
            if (v2 <= 5.0 && v4 <= 5.0) {
               zp.addChatMessage(
                  "§e[AutoLeave] §aТелепортация успешна, выключаюсь."
               );
               this.f586 = false;
               this.setEnabled(false);
               return;
            }
         }

         if (this.f584) {
            this.f587++;
            if (mc.field_1755 instanceof GenericContainerScreen v16) {
               GenericContainerScreenHandler v18 = (GenericContainerScreenHandler)v16.method_17577();
               int v21 = v18.method_17388() * (9);

               for (int v22 = 0; v22 < v21; v22++) {
                  Slot v23 = v18.method_7611(v22);
                  if (v23.method_7681() && v23.method_7677().method_7909() == Items.field_8323) {
                     mc.field_1761.method_2906(v18.field_7763, v22, 0, SlotActionType.field_7790, mc.field_1724);
                     this.f584 = false;

                     this.f587 = 0;
                     return;
                  }
               }

               if (this.f587 > 40) {
                  this.f584 = false;
                  this.f587 = 0;
               }
            } else if (this.f587 > 60) {
               this.f584 = false;
               this.f587 = 0;
            }
         } else if (System.currentTimeMillis() - this.f588 >= -2621919982449437879L + 2621919982449447879L) {
            boolean result = this.f582.hasTimePassed(1326106560086948074L + -1326106560086943074L);

            if (result) {
               boolean v15 = false;
               String v3 = "";
               if (this.onPlayerNearby.getValue()) {
                  float v19 = this.playerRange.getValue();

                  for (PlayerEntity v6 : mc.field_1687.method_18456()) {
                     if (v6 != mc.field_1724 && v6.method_5805()) {
                        double v7 = (double)mc.field_1724.method_5739(v6);
                        if (v7 <= (double)v19) {
                           v15 = true;
                           String v24 = v6.method_5477().getString();
                           int v9 = (int)v7;
                           String v10 = v24;
                           v3 = "Игрок " + v10 + " рядом (" + v9 + "м)";
                           break;
                        }
                     }
                  }
               }

               if (!v15 && this.onPvpEnd.getValue()) {
                  try {
                     fi v20 = uq.get(fi.class);
                     if (v20 != null && v20.isEnabled() && v20.isPvpJustEnded()) {
                        v15 = true;
                        v3 = "PvP закончился";
                        v20.clearPvpEnded();
                     }
                  } catch (Exception v14) {
                  }
               }

               if (v15) {
                  this.m554(v3);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.f584 = false;
      this.f587 = 0;
      this.f585 = false;
      this.f586 = false;

      super.onDisable();
   }

   
}
