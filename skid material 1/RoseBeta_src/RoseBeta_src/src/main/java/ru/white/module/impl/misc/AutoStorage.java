package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.PacketUtil;
import ru.white.utils.BlockUtil;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auto Storage",
   desc = "Данный модуль был сделан акарачком поэтому может работать через жопу",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoStorage extends Module {
   private long f941;
   public final SettingVisibility bind = new SettingVisibility(this, "Бинд", -1);
   private final BlockUtil f940;
   public final ModeSetting mode = new ModeSetting(
      this,
      "Режим",
      "Только ценные",
      "Все предметы"
   );
   public boolean triggeredByChestStealer;
   private am.bm state = am.bm.IDLE;

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         switch (this.state) {
            case CLOSING:
               if (mc.field_1755 instanceof GenericContainerScreen) {
                  this.state = am.bm.MOVING_ITEMS;
                  this.f940.reset();
                  this.f941 = 0L;
               } else if (this.f940.hasTimePassed(-1764634201466110185L + 1764634201466115185L)) {
                  this.state = am.bm.IDLE;
                  if (this.triggeredByChestStealer) {
                     this.triggeredByChestStealer = false;
                  }
               }
               break;
            case IDLE:
               if (!(mc.field_1755 instanceof GenericContainerScreen v2)) {
                  this.state = am.bm.IDLE;
                  return;
               }

               if (!this.f940.hasTimePassed(this.f941)) {
                  return;
               }

               this.m1211(v2);
               break;
            case WAITING_FOR_OPEN:
               if (mc.field_1755 != null) {
                  mc.field_1755.method_25419();
                  mc.method_1507(null);
               }

               this.state = am.bm.IDLE;
               if (this.triggeredByChestStealer) {
                  this.triggeredByChestStealer = false;
               }
         }
      } else {
         this.state = am.bm.IDLE;
      }
   }

   @Override
   public void onDisable() {
      this.state = am.bm.IDLE;

      super.onDisable();
   }

   @EventTarget
   public void onKey(EventPostMotion e) {
      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            if (e.getKey() == this.bind.get()) {
               this.trigger();
            }

            return;
         }

         
      }
   }

   private void m1211(GenericContainerScreen screen) {
      int result = ((GenericContainerScreenHandler)screen.method_17577()).method_17388();

      int v2 = result;
      int v3 = v2 * (9);
      int v4 = ((GenericContainerScreenHandler)screen.method_17577()).field_7761.size();
      boolean v5 = false;

      for (int v6 = v3; v6 < v4; v6++) {
         Slot v7 = ((GenericContainerScreenHandler)screen.method_17577()).method_7611(v6);
         if (v7.method_7681()) {
            boolean v8 = this.mode.is("Все предметы") || this.isValuableItem(v7.method_7677());
            if (v8) {
               mc.field_1761.method_2906(((GenericContainerScreenHandler)screen.method_17577()).field_7763, v6, 0, SlotActionType.field_7794, mc.field_1724);
               this.f941 = (long)fq.random(120.0, 150.0);
               this.f940.reset();
               v5 = true;
               return;
            }
         }
      }

      if (!v5) {
         this.state = am.bm.CLOSING;
      }
   }

   public boolean isWorking() {
      boolean v5;
      if (this.state != am.bm.IDLE) {

         v5 = true;
      } else {
         v5 = false;
      }

      return v5;
   }

   public AutoStorage() {
      this.f940 = new BlockUtil();
      this.triggeredByChestStealer = false;
      this.f941 = 0L;
   }

   public boolean isValuableItem(ItemStack stack) {
      if (stack.method_7964().getString().contains("★") && stack != Items.field_8107.method_7854()) {
         return true;
      } else {
         Item v2 = stack.method_7909();
         if (v2 != Items.field_8436
            && v2 != Items.field_8712
            && v2 != Items.field_8086
            && v2 != Items.field_8136
            && v2 != Items.field_8477
            && v2 != Items.field_22021
            && v2 != Items.field_22020
            && v2 != Items.field_41946
            && v2 != Items.field_8367
            && v2 != Items.field_8837
            && v2 != Items.field_8449
            && v2 != Items.field_8626
            && v2 != Items.field_8137
            && v2 != Items.field_8463
            && v2 != Items.field_8791) {

            if (v2 != Items.field_8603
               && v2 != Items.field_22022
               && v2 != Items.field_22027
               && v2 != Items.field_22029
               && v2 != Items.field_22028
               && v2 != Items.field_22030
               && v2 != Items.field_8366
               && v2 != Items.field_8668
               && v2 != Items.field_8543
               && v2 != Items.field_8675
               && v2 != Items.field_8288
               && v2 != Items.field_8833
               && v2 != Items.field_38418) {
               return false;
            }
         }

         return true;
      }
   }

   public void trigger() {

      if (mc.field_1724 != null) {
         if (this.state == am.bm.IDLE) {
            mc.field_1724.field_3944.method_45730("clan storage");
            this.state = am.bm.WAITING_FOR_OPEN;

            this.f940.reset();
            this.triggeredByChestStealer = false;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum bm {
      MOVING_ITEMS,
      CLOSING,
      IDLE,
      WAITING_FOR_OPEN;
      // $VF: synthetic field
      private static final am.bm[] $VALUES = m1210();
   }
}
