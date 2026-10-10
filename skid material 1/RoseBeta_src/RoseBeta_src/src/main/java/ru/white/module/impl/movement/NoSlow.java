package ru.white.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import ru.white.event.EventTarget;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.EntityUtil;
import ru.white.event.impl.EventBlockBreak;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "No Slow",
   category = Category.MOVEMENT,
   desc = "Позволяет использавать придметы без замедления"
)
@Environment(EnvType.CLIENT)
public class NoSlow extends Module {
   private int f555;
   private int f557;
   public ModeSetting type = new ModeSetting(this, "Режим", "ФанТайм", "Грим", "Тики", "Обычный");
   private boolean f560;
   private int f562;
   private boolean f563;
   private int f554 = 0;
   private final EntityUtil f559;
   private boolean f561;
   private boolean f558;
   private boolean f556;

   @EventTarget
   public void onSlow(EventBlockBreak e) {
      Hand v2 = mc.field_1724.method_6058();
      Hand v3 = v2.equals(Hand.field_5808) ? Hand.field_5810 : Hand.field_5808;
      switch (e.getType()) {
         case 1:
            if (this.type.is("Обычный")) {
               e.cancel();
            }

            if (this.type.is("Тики")) {
               int[] v4 = new int[]{2, 2, 2};
               int v5 = v4[this.f555 % 2];
               if (this.f554 >= v5) {
                  e.cancel();
                  this.f554 = 0;
                  this.f555++;
               }
            }

            if (this.type.is("ФанТайм") && (float)this.f554 > 0.0F) {

               if ((float)mc.field_1724.method_6048() > 1.0F) {
                  boolean v12 = mc.field_1724.method_6047().method_7909() instanceof CrossbowItem;
                  boolean v13 = mc.field_1724.method_6079().method_7909() instanceof CrossbowItem;
                  BlockPos v6 = new BlockPos(
                     (int)Math.floor(mc.field_1724.method_23317()),
                     (int)Math.floor(mc.field_1724.method_23318()),
                     (int)Math.floor(mc.field_1724.method_23321())
                  );
                  BlockState v7 = mc.field_1687.method_8320(v6);
                  Block v8 = v7.method_26204();
                  if (v8 == Blocks.field_10477 && mc.field_1724.method_24828()) {

                     e.cancel();
                  }

                  if (v12 || v13) {
                     e.cancel();
                  }
               }
            }

            if (this.type.is("Грим")
               && (
                  mc.field_1724.method_6079().method_7976().equals(UseAction.field_8952)
                     || mc.field_1724.method_6047().method_7976().equals(UseAction.field_8952)
               )) {
               this.interactItem(v2);
               this.interactItem(v3);
               e.cancel();
            }
      }
   }

   public void interactItem(Hand hand, Vec2f angle) {
      this.sendSequencedPacket(i -> {
         PlayerInteractItemC2SPacket var10000x = new PlayerInteractItemC2SPacket;
         int v3x = -1602235897;
         int var10006 = v3x * (v3x + 1);

         var10000x./* $VF: Unable to resugar constructor */<init>(hand, i, angle.field_1343, angle.field_1342);
         return var10000x;
      });

      
   }

   public void interactItem(Hand hand) {
      this.interactItem(hand, new Vec2f(mc.field_1724.method_36454(), mc.field_1724.method_36455()));
      int temp1 = v2 + 1;

      
   }

   @Override
   public void onDisable() {
      if (this.f556 && mc.field_1724 != null) {
         this.m520(this.f557);

         mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7512.field_7763));
         
      }

      this.m521(true);
      this.f556 = false;
      this.f557 = -1;
      this.f558 = false;
      this.f560 = false;
      this.f561 = false;
      this.f562 = -1;
   }

   private void m521(boolean state) {
      KeyBinding[] result = new KeyBinding[]{mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, null, null};

      result[3] = mc.field_1690.field_1849;
      result[4] = mc.field_1690.field_1903;
      KeyBinding[] v2 = result;

      for (KeyBinding v6 : v2) {

         boolean v7 = state && InputUtil.method_15987(mc.method_22683(), v6.method_1429().method_1444());
         v6.method_23481(v7);
      }
   }

   private void m522(int slot, boolean isRestore) {
      this.f562 = slot;
      this.f563 = isRestore;

      this.f560 = true;

      this.f561 = false;
      this.m521(false);
      this.f559.reset();
   }

   public void sendSequencedPacket(SequencedPacketCreator packetCreator) {

      mc.field_1761.method_41931(mc.field_1687, packetCreator);
   }

   public NoSlow() {
      this.f555 = 0;
      this.f556 = false;
      this.f557 = -1;
      this.f558 = false;
      this.f559 = new EntityUtil();
      this.f560 = false;
      this.f561 = false;
      this.f562 = -1;
      this.f563 = false;
   }

   @EventTarget
   public void onUpdate(EventRenderModel event) {
      if (!mc.field_1724.method_6123()) {
         if (mc.field_1724.method_6115()) {

            this.f554++;
         } else {
            this.f554 = 0;
            this.f555 = 0;
         }
      }

      if (this.type.is("ФанТайм")) {
      }
   }

   private int m519() {

      while (true) {
         int temp1 = 9;

         if (v1 >= temp1) {
            for (int v5 = 9; v5 < 36; v5++) {
               if (((ItemStack)mc.field_1724.method_31548().method_67533().get(v5)).method_7909() instanceof CrossbowItem) {
                  return v5;
               }
            }

            return -1;
         }

         boolean result = ((ItemStack)mc.field_1724.method_31548().method_67533().get(v1)).method_7909() instanceof CrossbowItem;
         int v7 = v2 + 1;

         if (result) {
            return v1;
         }

         v1++;
      }
   }

   private void m520(int inventoryMainSlot) {

      int v2 = inventoryMainSlot < 9 ? 36 + inventoryMainSlot : inventoryMainSlot;
      if (!mc.field_1724.method_5624()) {

         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, v2, 40, SlotActionType.field_7791, mc.field_1724);
      }
   }

   
}
