package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.ItemUtil;

@ModuleAnnotation(
   name = "Lock Slot",
   desc = "Блокирует смену слота хотбара и защищает предметы от выброса",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class LockSlot extends Module {
   public final BooleanSetting slot4;
   public final BooleanSetting slot1 = new BooleanSetting(this, "Слот 1", false);
   public final BooleanSetting slot3;
   public final BooleanSetting slot2 = new BooleanSetting(this, "Слот 2", false);
   public final BooleanSetting swords;
   private final BooleanSetting[] slots;
   public final BooleanSetting slot9;
   public final BooleanSetting slot8;
   public final BooleanSetting slot5;
   public final BooleanSetting mace;
   public final BooleanSetting shulkers;
   public final BooleanSetting onlyPvp;
   public final BooleanSetting slot7;
   public final BooleanSetting slot6;

   public boolean isDropProtected(ItemStack stack, int inventorySlot) {
      if (!this.isEnabled() || stack == null || stack.method_7960()) {
         return false;
      } else if (this.onlyPvp.getValue() && !iq.isPvp()) {
         return false;
      } else if (this.m626(stack)) {

         return this.shulkers.getValue();
      } else {
         return inventorySlot >= 0 && inventorySlot <= 8
            ? this.swords.getValue() && this.m627(stack) || this.mace.getValue() && stack.method_31574(Items.field_49814)
            : false;
      }
   }

   public LockSlot() {
      this.slot3 = new BooleanSetting(this, "Слот 3", false);
      this.slot4 = new BooleanSetting(this, "Слот 4", false);
      this.slot5 = new BooleanSetting(this, "Слот 5", false);
      this.slot6 = new BooleanSetting(this, "Слот 6", false);
      this.slot7 = new BooleanSetting(this, "Слот 7", false);
      this.slot8 = new BooleanSetting(this, "Слот 8", false);
      this.slot9 = new BooleanSetting(this, "Слот 9", false);
      this.onlyPvp = new BooleanSetting(this, "Только PvP", true);
      this.swords = new BooleanSetting(this, "Мечи", false);
      this.mace = new BooleanSetting(this, "Булава", false);
      this.shulkers = new BooleanSetting(this, "Шалкеры", false);
      BooleanSetting[] temp1 = new BooleanSetting[9];
      temp1[0] = this.slot1;
      temp1[1] = this.slot2;
      temp1[2] = this.slot3;
      temp1[3] = this.slot4;
      temp1[4] = this.slot5;
      temp1[5] = this.slot6;
      temp1[6] = this.slot7;
      temp1[7] = this.slot8;
      temp1[8] = this.slot9;
      this.slots = temp1;
   }

   public boolean isSlotLocked(int slot) {
      if (slot >= 0 && slot <= 8) {
         BooleanSetting result = this.slots[slot];

         return result.getValue();
      } else {
         return false;
      }
   }

   public static li get() {
      return uq.get(li.class);
   }

   private boolean m627(ItemStack stack) {

      Identifier result = Registries.field_41178.method_10221(stack.method_7909());

      return result.method_12832().endsWith("_sword");
   }

   private boolean m626(ItemStack stack) {
      if (stack.method_7909() instanceof BlockItem v2) {

         if (v2.method_7711() instanceof ShulkerBoxBlock) {
            return true;
         }
      }

      return false;
   }

   
}
