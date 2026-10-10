package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "SP Joiner",
   desc = "Автоматические заходит на дуэли SpookyTime",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class SPJoiner extends Module {
   @EventTarget
   public void onEvent(EventTick event) {
      MinecraftClient v2 = MinecraftClient.method_1551();
      if (v2.field_1724 != null) {
         Screen v3 = v2.field_1755;
         if (v2.field_1724.method_31548().method_67532() == 0 && v2.field_1724.method_6047().method_7909() == Items.field_8251) {
            this.toggle();
         } else {
            if (v3 instanceof HandledScreen v4) {
               ScreenHandler v5 = v4.method_17577();

               for (int v6 = 0; v6 < v5.field_7761.size(); v6++) {
                  Object result = v5.field_7761.get(v6);

                  Slot v7 = (Slot)result;
                  ItemStack v8 = v7.method_7677();
                  if (v8.method_31574(Items.field_22022)) {
                     v2.field_1761.method_2906(v5.field_7763, v6, 0, SlotActionType.field_7790, v2.field_1724);
                     return;
                  }
               }
            } else {
               selectCompass();
            }
         }
      }
   }

   public static void selectCompass() {
      MinecraftClient v0 = MinecraftClient.method_1551();
      if (v0.field_1724 != null) {
         PlayerInventory v1 = v0.field_1724.method_31548();

         for (int v3 = 0; v3 < 9; v3++) {
            if (v1.method_5438(v3).method_31574(Items.field_8251)) {
               v2 = v3;
               break;
            }
         }

         if (v2 == -1) {
            
         } else {
            v1.method_61496(v2);
            v0.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(v2));
            v0.field_1761.method_2919(v0.field_1724, Hand.field_5808);
         }
      }
   }

   
}
