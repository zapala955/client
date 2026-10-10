package ru.white.utils;

import java.util.stream.IntStream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class MathUtil implements IMinecraft {
   public static void inventorySwapClick(Item item, boolean rotation) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         int v2 = getItemIndex(item);
         if (v2 != -1) {
            int v3 = mc.field_1724.method_31548().method_67532();
            if (v2 < 9) {
               if (v2 != v3) {
                  mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(v2));
               }

               mc.field_1724
                  .field_3944
                  .method_52787(new PlayerInteractItemC2SPacket(Hand.field_5808, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
               if (v2 != v3) {
                  mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(v3));
               }
            } else {
               mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, v2, v3, SlotActionType.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(v3));
               mc.field_1724
                  .field_3944
                  .method_52787(new PlayerInteractItemC2SPacket(Hand.field_5808, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
               mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(v3));

               mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, v2, v3, SlotActionType.field_7791, mc.field_1724);
            }
         }
      }
   }

   public static void addTaskWithLock(Runnable task, long delayMs) {
      if (mc.field_1724 != null) {

         m1590(false);
         new Thread(() -> {
            try {
               Thread.sleep(-9137304614631527004L + 9137304614631527034L);

               mc.execute(() -> {
                  if (task != null) {

                     task.run();
                     int v2 = -1670196287;

                     
                  }
               });
               Thread.sleep(delayMs);
               mc.execute(() -> {
                  m1590(true);
                  
               });
            } catch (InterruptedException v7) {
               v7.printStackTrace();
               mc.execute(() -> {
                  m1590(true);
                  
               });
            }
         }).start();
      }
   }

   @Generated
   private MathUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static int find(Item item) {
      int v1 = -1;

      for (int v2 = 0; v2 < 36; v2++) {
         ItemStack v3 = mc.field_1724.method_31548().method_5438(v2);
         if (!v3.method_7960() && v3.method_7909() == item) {

            v1 = v2;
            break;
         }
      }

      if (v1 < 9 && v1 != -1) {
         v1 += 36;
      }

      return v1;
   }

   public static boolean doesHotbarHaveItem(Item item) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         int v2 = -1991707122;
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }

         for (int v1 = 0; v1 < 9; v1++) {
            if (mc.field_1724.method_31548().method_5438(v1).method_7909() == item) {

               return true;
            }
         }

         return false;
      }
   }

   public static int getItemIndex(Item item) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int v1 = 0; v1 < mc.field_1724.method_31548().method_5439(); v1++) {

            if (mc.field_1724.method_31548().method_5438(v1).method_7909() == item) {
               return v1;
            }
         }

         return -1;
      }
   }

   private static void m1590(boolean state) {
      KeyBinding[] result = new KeyBinding[6];
      result[0] = mc.field_1690.field_1894;

      result[1] = mc.field_1690.field_1881;
      result[2] = mc.field_1690.field_1913;
      result[3] = mc.field_1690.field_1849;

      result[4] = mc.field_1690.field_1903;
      result[5] = mc.field_1690.field_1867;
      KeyBinding[] v1 = result;
      long v2 = mc.method_22683().method_4490();

      for (KeyBinding v7 : v1) {
         boolean v12;
         if (state && InputUtil.method_15987(mc.method_22683(), v7.method_1429().method_1444())) {
            v12 = true;
         } else {
            v12 = false;
            
         }

         boolean v8 = v12;
         v7.method_23481(v8);
      }
   }

   public static int find(Item item, boolean ignoreEnchanted, boolean onlyEnchanted) {
      int v3 = -1;
      if (onlyEnchanted) {
         for (int v4 = 0; v4 < 36; v4++) {
            PlayerInventory result = mc.field_1724.method_31548();

            ItemStack v5 = result.method_5438(v4);
            if (!v5.method_7960() && v5.method_7909() == item && v5.method_7942()) {
               v3 = v4;
               break;
            }
         }
      } else {

         for (int v9 = 0; v9 < 36; v9++) {
            ItemStack v11 = mc.field_1724.method_31548().method_5438(v9);
            if (!v11.method_7960() && v11.method_7909() == item && !v11.method_7942()) {
               v3 = v9;
               break;
            }
         }

         if (v3 == -1 && !ignoreEnchanted) {
            for (int v10 = 0; v10 < 36; v10++) {
               ItemStack v12 = mc.field_1724.method_31548().method_5438(v10);
               if (!v12.method_7960()) {

                  if (v12.method_7909() == item) {
                     v3 = v10;
                     break;
                  }
               }
            }
         }
      }

      if (v3 < 9 && v3 != -1) {
         v3 += 36;
      }

      return v3;
   }

   public static int find(Item item, boolean ignoreEnchanted) {

      return find(item, ignoreEnchanted, false);
   }

   public static int getItemInHotBar(Item item) {
      return IntStream.range(0, 9).filter(i -> {
         Item result = mc.field_1724.method_31548().method_5438(i).method_7909();

         return result.equals(item);
      }).findFirst().orElse(-1);
   }

   
}
