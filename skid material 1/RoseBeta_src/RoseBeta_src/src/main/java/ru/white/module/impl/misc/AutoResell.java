package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auto Resell",
   desc = "МОДУЛЬ ПИСАЛ АКАР МОЖЕТ РОБОТАТЬ ЧЕРЕЗ ОЧКО И ЧУДА АЛЛАХА",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoResell extends Module {
   private static final int OPEN_TIMEOUT_TICKS = 100;
   private yl.zl state = yl.zl.IDLE;
   private int f938;
   private static final int TICK_DELAY = 6;
   private int f937;
   private int f939;
   private static final int PERIOD_TICKS = 1200;

   private String m1207(ItemStack stack) {
      StringBuilder v2 = new StringBuilder(this.m1208(stack.method_7964().getString()));
      LoreComponent v3 = (LoreComponent)stack.method_57353().method_58694(DataComponentTypes.field_49632);
      if (v3 != null) {
         for (Text v5 : v3.comp_2400()) {

            v2.append((char)(32)).append(this.m1208(v5.getString()));
         }
      }

      return v2.toString();
   }

   @Override
   protected void onDisable() {
      this.state = yl.zl.IDLE;
      this.f937 = 0;

      this.f938 = 0;
      this.f939 = 0;
   }

   private void m1202() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v5 = (GenericContainerScreenHandler)v1.method_17577();
         int v3 = this.m1205(v5);

         if (v3 < 0) {
            this.f938 = 6;
         } else {
            mc.field_1761.method_2906(v5.field_7763, v3, 0, SlotActionType.field_7790, mc.field_1724);
            this.state = yl.zl.CLOSE;
            this.f938 = 6;
         }
      } else {
         this.state = yl.zl.OPEN_AH;
         this.f938 = 6;
      }
   }

   @Override
   protected void onEnable() {
      this.m1199();
      
   }

   private void m1209(String command) {
      ClientPlayNetworkHandler result = mc.field_1724.field_3944;
      String temp1;
      if (command.startsWith("/")) {

         temp1 = command.substring(1);
      } else {
         temp1 = command;
      }

      result.method_45730(temp1);
   }

   private int m1205(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);

      while (true) {

         if (v3 >= v2) {
            return this.m1206(handler, "перевыставить предметы");
         }

         Slot v4 = handler.method_7611(v3);
         if (v4.method_7681()) {
            ItemStack v5 = v4.method_7677();
            String v6 = this.m1207(v5);
            if (v5.method_7909() == Items.field_8557 && v6.contains("перевыстав")) {
               return v3;
            }
         }

         v3++;
      }
   }

   private int m1206(GenericContainerScreenHandler handler, String needle) {
      int v3 = handler.method_17388() * (9);

      for (int v4 = 0; v4 < v3; v4++) {

         Slot result = handler.method_7611(v4);

         Slot v5 = result;
         if (v5.method_7681() && this.m1207(v5.method_7677()).contains(needle)) {
            return v4;
         }
      }

      return -1;
   }

   private String m1208(String text) {
      String result;
      if (text == null) {
         result = "";
      } else {
         result = text.replaceAll("§.", "");

         result = result.trim().toLowerCase();
      }

      return result;
   }

   private void m1199() {
      if (mc.field_1724 == null) {

         this.state = yl.zl.IDLE;
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
            
         }

         this.m1209("ah");
         this.state = yl.zl.OPEN_AH;
         this.f939 = 0;
         this.f938 = 20;
      }
   }

   private void m1203() {
      if (mc.field_1755 != null) {

         mc.field_1724.method_7346();
      }

      this.state = yl.zl.IDLE;
      this.f937 = 1200;
      this.f938 = 0;
      this.f939 = 0;
   }

   private int m1204(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);

      for (int v3 = 0; v3 < v2; v3++) {

         Slot v4 = handler.method_7611(v3);
         if (v4.method_7681() && v4.method_7677().method_7909() == Items.field_8466) {
            return v3;
         }
      }

      return this.m1206(handler, "хранилище");
   }

   private void m1201() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v7 = (GenericContainerScreenHandler)v1.method_17577();

         if (this.m1205(v7) >= 0) {
            this.state = yl.zl.CLICK_RELIST;
            this.f938 = 2;
         } else {
            int v3 = this.m1204(v7);
            if (v3 < 0) {
               this.f938 = 6;
            } else {
               mc.field_1761.method_2906(v7.field_7763, v3, 0, SlotActionType.field_7790, mc.field_1724);
               this.state = yl.zl.CLICK_RELIST;
               this.f938 = 8;
            }
         }
      } else {
         this.state = yl.zl.OPEN_AH;
         this.f938 = 6;
      }
   }

   private void m1198() {
      if (this.f937 > 0) {
         int temp1 = this.f937 - 1;

         this.f937 = temp1;
      } else {
         this.m1199();
      }
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.f938 > 0) {
            this.f938--;
         } else {
            switch (this.state) {
               case CLICK_STORAGE:
                  this.m1198();
                  break;
               case CLICK_RELIST:
                  this.m1200();
                  break;
               case IDLE:
                  this.m1201();
                  break;
               case OPEN_AH:
                  this.m1202();
                  break;
               case CLOSE:
                  this.m1203();
            }
         }
      }
   }

   private void m1200() {
      if (mc.field_1755 instanceof GenericContainerScreen) {
         this.f939 = 0;
         this.state = yl.zl.CLICK_STORAGE;
         this.f938 = 2;
      } else if (++this.f939 >= 100) {
         this.m1209("ah");

         this.f939 = 0;
         this.f938 = 20;
      } else {
         this.f938 = 6;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum zl {
      CLICK_STORAGE,
      CLICK_RELIST,
      IDLE,
      OPEN_AH,
      CLOSE;
      // $VF: synthetic field
      private static final yl.zl[] $VALUES = m1197();
   }
}
