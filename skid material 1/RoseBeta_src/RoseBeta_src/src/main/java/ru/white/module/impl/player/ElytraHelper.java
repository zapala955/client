package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.utils.MathUtil;
import ru.white.utils.ItemUtil;
import ru.white.utils.BlockUtil;
import ru.white.utils.TimerUtil;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Elytra Helper",
   desc = "Помощник элитры: авто-свап и полёт",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ElytraHelper extends Module {
   private fi.gi f612;
   private long f620;
   public final SettingVisibility use;
   private final BlockUtil f613;
   private final BlockUtil f616;
   private boolean f618;
   public final BooleanSetting autoPvpSwap;
   public final BooleanSetting autoFly;
   private int f621;
   public final SettingVisibility swapChestKey = new SettingVisibility(this, "Свап");
   private int f622;
   public static TimerUtil stopWatch = new TimerUtil();
   private boolean f619;
   private ItemStack f614;
   private fi.hi f623;
   private Item f617;
   private boolean f615;
   private final BlockUtil f624;

   private void m615(boolean state) {
      KeyBinding[] v2 = new KeyBinding[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };
      long v3 = mc.method_22683().method_4490();

      for (KeyBinding result : v2) {

         KeyBinding v8 = result;
         boolean v9 = state && InputUtil.method_15987(mc.method_22683(), v8.method_1429().method_1444());

         v8.method_23481(v9);
      }
   }

   public void clearPvpEnded() {

      this.f619 = false;
   }

   @EventTarget
   private void onUpdate(EventTick e) {
      ItemStack v2 = mc.field_1724.method_6118(EquipmentSlot.field_6174);
      Item v3 = v2.method_7909();
      if (v3 == Items.field_8833 && this.f617 != Items.field_8833 && this.autoFly.getValue()) {
         this.f615 = true;
         this.f616.reset();
      }

      this.f617 = v3;
      boolean v4 = iq.isPvp();
      if (this.f618 && !v4) {
         this.f619 = true;
         this.f620 = System.currentTimeMillis();
      }

      this.f618 = v4;
      if (this.autoPvpSwap.getValue() && this.f612 == fi.gi.IDLE && mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8833) {
         boolean v5 = false;
         if (iq.isPvp()) {
            v5 = true;
         }

         if (!v5) {

            for (PlayerEntity v7 : mc.field_1687.method_18456()) {
               if (v7 != mc.field_1724 && v7.method_5805() && !Client.get().friendManager().isFriend(v7.method_5477().getString())) {
                  double v8 = (double)mc.field_1724.method_5739(v7);
                  if (v8 <= 5.0) {
                     v5 = true;
                     break;
                  }
               }
            }
         }

         if (v5) {
            int v13 = this.m613();
            if (v13 >= 0 && (!mc.field_1724.method_6115() || mc.field_1724.method_6079().method_7909() == Items.field_8255)) {
               this.f612 = fi.gi.SWAPPING;
               this.f614 = mc.field_1724.method_6118(EquipmentSlot.field_6174);
               this.f613.reset();
               stopWatch.reset();
            }
         }
      }

      this.m611();
      if (this.f615 && this.f616.hasTimePassed(7421712324069244980L + -7421712324069244580L)) {
         if (this.autoFly.getValue()) {
            mc.field_1724.field_3944.method_45730("fly");
         }

         this.f615 = false;
      }

      if (mc.field_1761 != null && this.f623 != fi.hi.IDLE) {
         this.m615(false);
         switch (this.f623) {
            case SWAP:
               long temp1 = -5470509953116746459L + 5470509953116746509L;

               if (this.f624.hasTimePassed(temp1)) {
                  this.f624.reset();
                  this.f623 = fi.hi.SWAP;
               }
               break;
            case PREPARE:
               if (this.f621 < 9) {
                  mc.field_1724.method_31548().method_61496(this.f621);
                  mc.field_1761.method_2911();
               } else if (!mc.field_1724.method_5624()) {
                  mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f621, this.f622, SlotActionType.field_7791, mc.field_1724);
               }

               if (this.f624.hasTimePassed(-6345421856758076036L + 6345421856758076066L)) {
                  this.f624.reset();

                  this.f623 = fi.hi.THROW;
               }
               break;
            case COOLDOWN:
               xk.interactItem(Hand.field_5808);
               if (this.f624.hasTimePassed(-3278134184523531126L + 3278134184523531156L)) {
                  this.f624.reset();
                  this.f623 = fi.hi.RESTORE;
               }
               break;
            case IDLE:
               if (this.f621 < 9) {
                  mc.field_1724.method_31548().method_61496(this.f622);
                  mc.field_1761.method_2911();
               } else {
                  if (!mc.field_1724.method_5624()) {
                     mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f621, this.f622, SlotActionType.field_7791, mc.field_1724);
                  }

                  mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
               }

               this.f624.reset();
               this.f623 = fi.hi.COOLDOWN;
               break;
            case THROW:
               if (this.f624.hasTimePassed(-2494657331957394759L + 2494657331957394829L)) {
                  this.m615(true);
                  this.f623 = fi.hi.IDLE;
               }
         }
      }
   }

   public boolean isPvpJustEnded() {
      if (this.f619 && System.currentTimeMillis() - this.f620 < 7375849742843066373L + -7375849742843063373L) {
         return true;
      } else {
         this.f619 = false;
         return false;
      }
   }

   @Override
   public void onDisable() {
      label19: {
         stopWatch.reset();
         if (this.f623 == fi.hi.IDLE) {

            if (this.f612 == fi.gi.IDLE) {
               break label19;
            }
         }

         this.m615(true);
      }

      this.f623 = fi.hi.IDLE;

      this.f612 = fi.gi.IDLE;
      super.onDisable();
   }

   private 

      return dq.find(input);
   }

   private int m613() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         Item[] result = new Item[6];
         result[0] = Items.field_22028;
         result[1] = Items.field_8058;
         result[2] = Items.field_8873;
         result[3] = Items.field_8678;
         result[4] = Items.field_8523;
         result[5] = Items.field_8577;
         Item[] v1 = result;

         for (Item v5 : v1) {
            for (int v6 = 0; v6 < 36; v6++) {

               Item v7 = mc.field_1724.method_31548().method_5438(v6).method_7909();
               if (v7 == v5) {
                  return v6 < 9 ? v6 + 36 : v6;
               }
            }
         }

         return -1;
      }
   }

   private void m610() {
      if (mc.field_1724 != null) {
         this.f621 = -1;

         for (int v1 = 0; v1 < 36; v1++) {
            ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
            if (v2.method_7909() == Items.field_8639) {

               this.f621 = v1;
               
               break;
            }
         }

         if (this.f621 == -1) {
            mc.field_1724
               .method_7353(
                  Text.method_43470("Перл не найден!").method_27692(Formatting.field_1061), true
               );
         } else {
            this.f622 = mc.field_1724.method_31548().method_67532();
            this.f624.reset();
            this.f623 = fi.hi.PREPARE;
         }
      }
   }

   private void m612(int slot, int chestSlot) {
      if (mc.field_1755 == null) {
         int v3 = mc.field_1724.method_31548().method_67532();
         if (slot >= 36 && slot <= 44) {
            mc.field_1761
               .method_2906(mc.field_1724.field_7498.field_7763, chestSlot, slot % (9), SlotActionType.field_7791, mc.field_1724);
         } else {

            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slot, v3, SlotActionType.field_7791, mc.field_1724);

            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, chestSlot, v3, SlotActionType.field_7791, mc.field_1724);
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slot, v3, SlotActionType.field_7791, mc.field_1724);
         }
      }
   }

   @EventTarget
   private void onEventKey(EventPostMotion e) {
      if (e.getKey() == this.swapChestKey.get() && (!mc.field_1724.method_6115() || mc.field_1724.method_6079().method_7909() == Items.field_8255)) {
         ItemStack v2 = mc.field_1724.method_6118(EquipmentSlot.field_6174);
         Item v7 = v2.method_7909();

         int v8;
         if (v7 == Items.field_8833) {
            v8 = this.m613();
         } else {
            v8 = this.m614(Items.field_8833);
            
         }

         int v3 = v8;
         if (v3 >= 0 && this.f612 == fi.gi.IDLE) {
            this.f612 = fi.gi.SWAPPING;
            this.f614 = v2;
            this.f613.reset();
            stopWatch.reset();
         }
      } else {
         int result = e.getKey();

         if (result == this.use.get() && !mc.field_1724.method_24828() && this.f623 == fi.hi.IDLE) {
            this.m610();
         }
      }
   }

   public ElytraHelper() {
      this.use = new SettingVisibility(this, "Исп. фейерверк");
      this.autoFly = new BooleanSetting(this, "AutoFly", true);
      this.autoPvpSwap = new BooleanSetting(this, "AutoPvpSwap", false);
      this.f612 = fi.gi.IDLE;
      this.f613 = new BlockUtil();
      this.f614 = ItemStack.field_8037;
      this.f615 = false;
      this.f616 = new BlockUtil();
      this.f617 = Items.field_8162;
      this.f618 = false;
      this.f619 = false;
      this.f620 = 0L;
      this.f621 = -1;
      this.f622 = -1;
      this.f623 = fi.hi.IDLE;
      this.f624 = new BlockUtil();
   }

   private void m611() {
      if (this.f612 != fi.gi.IDLE) {
         if (mc.field_1755 != null) {
            this.f612 = fi.gi.IDLE;
            this.m615(true);
         } else {
            this.m615(false);
            switch (this.f612) {
               case SWAPPING:
                  if (!this.f613.hasTimePassed(-7887610622117387957L + 7887610622117388007L)) {
                     return;
                  }

                  ItemStack v1 = mc.field_1724.method_6118(EquipmentSlot.field_6174);
                  int v2 = this.m614(Items.field_8833);
                  int v3 = this.m613();
                  Item result = v1.method_7909();

                  if (result != Items.field_8833 && v2 >= 0) {
                     this.m612(v2, 6);
                     this.f613.reset();
                     this.f612 = fi.gi.CLOSING;
                  } else if (v1.method_7909() == Items.field_8833 && v3 >= 0) {
                     this.m612(v3, 6);
                     this.f613.reset();
                     this.f612 = fi.gi.CLOSING;
                  } else {
                     this.f612 = fi.gi.IDLE;
                     this.m615(true);
                  }
                  break;
               case SWAPPED:
                  if (!this.f613.hasTimePassed(-5520979998983689497L + 5520979998983689567L)) {
                     return;
                  }

                  mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
                  if (mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8833) {
                     this.f615 = true;
                     this.f616.reset();
                  }

                  this.f612 = fi.gi.IDLE;
                  this.m615(true);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum gi {
      RETURNING,
      SWAPPING,
      CLOSING,
      USING,
      IDLE,
      SWAPPED;
      // $VF: synthetic field
      private static final fi.gi[] $VALUES = m608();
   }

   @Environment(EnvType.CLIENT)
   private static enum hi {
      RESTORE,
      SWAP,
      PREPARE,
      COOLDOWN,
      IDLE,
      THROW;
      // $VF: synthetic field
      private static final fi.hi[] $VALUES = m609();
   }
}
