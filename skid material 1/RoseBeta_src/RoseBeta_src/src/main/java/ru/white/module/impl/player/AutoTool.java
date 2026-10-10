package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.BlockUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Tool",
   desc = "Автоматически выбирает лучший инструмент",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class AutoTool extends Module {
   private int f591;
   private final BlockUtil f589;
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Обычный", "Незаметный");
   private ci.di state = ci.di.IDLE;
   private int f590;
   private ItemStack f592;
   private static final long SWAP_STOP_MS = 60L;

   private void m566() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.state == ci.di.MINING || this.state == ci.di.PREPARE_RESTORE) {
            if (this.f590 >= 9) {
               this.m560();
               this.m561();
            } else {
               this.m562();
            }
         }

         this.m567(true);
      }

      this.state = ci.di.IDLE;

      this.f590 = -1;

      this.f591 = -1;
      this.f592 = null;
   }

   public static int getVisualSlot() {
      ci result = getInstance();

      ci v0 = result;
      if (v0 != null && isSilentActive()) {

         return v0.f591;
      } else {
         return -1;
      }
   }

   @Override
   public void onDisable() {
      this.m566();
      super.onDisable();

      
   }

   public AutoTool() {
      this.f589 = new BlockUtil();
      this.f590 = -1;
      this.f591 = -1;
      this.f592 = null;
   }

   public static ci getInstance() {
      ci result = uq.get(ci.class);

      return result;
   }

   private void m561() {
      ClientPlayNetworkHandler result = mc.field_1724.field_3944;
      CloseHandledScreenC2SPacket temp1 = new CloseHandledScreenC2SPacket;

      temp1./* $VF: Unable to resugar constructor */<init>(mc.field_1724.field_7498.field_7763);
      result.method_52787(temp1);
      mc.field_1724.method_7346();
   }

   private BlockState m563() {
      if (mc.field_1765 instanceof BlockHitResult result) {

         BlockHitResult v1 = result;
         if (v1.method_17783() == Type.field_1332) {
            return mc.field_1687.method_8320(v1.method_17777());
         }
      }

      return null;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {

         if (mc.field_1761 != null) {
            if (mc.field_1755 != null) {
               return;
            }

            switch (this.state) {
               case MINING:

                  this.m556();
                  break;
               case PREPARE_SWAP:
                  this.m557();
                  break;
               case PREPARE_RESTORE:
                  this.m558();
                  break;
               case IDLE:
                  this.m559();
            }

            return;
         }
      }

      this.state = ci.di.IDLE;
      this.f590 = -1;
      this.f591 = -1;
   }

   private int m564(BlockState blockState) {

      int v2 = mc.field_1724.method_31548().method_67532();
      int v3 = this.m565(mc.field_1724.method_31548().method_5438(v2)) ? v2 : -1;
      float v4 = v3 == -1 ? 1.0F : mc.field_1724.method_31548().method_5438(v3).method_7924(blockState);
      boolean result;
      if (v3 == -1 || blockState.method_29291() && !mc.field_1724.method_31548().method_5438(v3).method_7951(blockState)) {
         result = false;
         
      } else {
         result = true;
      }

      boolean v5 = result;

      for (int v6 = 0; v6 < 36; v6++) {
         ItemStack v7 = mc.field_1724.method_31548().method_5438(v6);
         if (this.m565(v7)) {
            float v8 = v7.method_7924(blockState);
            boolean v9 = !blockState.method_29291() || v7.method_7951(blockState);
            if (v9 && !v5 || v9 == v5 && v8 > v4) {
               v3 = v6;
               v4 = v8;
               v5 = v9;
            }
         }
      }

      return v3;
   }

   private void m560() {
      if (this.f590 >= 9 && this.f591 >= 0 && this.f591 <= 8) {

         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f590, this.f591, SlotActionType.field_7791, mc.field_1724);
      }
   }

   private void m557() {
      if (mc.field_1690.field_1886.method_1434() && this.m563() != null) {
         this.m567(false);
         if (this.f589.hasTimePassed(-3636455588860443051L + 3636455588860443111L)) {
            this.m567(true);

            this.m560();
            this.state = ci.di.MINING;
         }
      } else {
         this.reset();
      }
   }

   private void m559() {
      this.m567(false);
      if (this.f589.hasTimePassed(274607535081480964L + -274607535081480904L)) {
         this.m560();
         this.m561();
         this.reset();

         
      }
   }

   private void m558() {
      BlockState v1 = this.m563();
      if (mc.field_1690.field_1886.method_1434() && v1 != null) {
         if (this.f590 < 9) {
            int v2 = this.m564(v1);
            if (v2 != -1 && v2 < 9 && v2 != mc.field_1724.method_31548().method_67532()) {
               this.f590 = v2;
               mc.field_1724.method_31548().method_61496(v2);
               mc.field_1761.method_2911();
               
            }
         }
      } else {
         if (this.f590 < 9) {
            this.m562();
            this.reset();
         } else {
            this.f589.reset();
            this.state = ci.di.PREPARE_RESTORE;
         }
      }
   }

   private boolean m565(ItemStack stack) {
      if (!stack.method_7960()) {
         if (!stack.method_7963()) {
            return true;
         }

         int result = stack.method_7936();
         int temp1 = stack.method_7919();

         result -= temp1;

         if (result > 1) {
            return true;
         }
      }

      return false;
   }

   private void reset() {
      this.m567(true);
      this.state = ci.di.IDLE;
      this.f590 = -1;
      this.f591 = -1;

      this.f592 = null;
   }

   public static boolean isSilentActive() {
      ci v0 = getInstance();
      if (v0 != null && v0.isEnabled()) {

         if (v0.mode.is("Незаметный") && v0.state != ci.di.IDLE) {
            return true;
         }
      }

      return false;
   }

   private void m562() {
      if (this.f591 >= 0 && this.f591 <= 8) {

         PlayerInventory result = mc.field_1724.method_31548();

         result.method_61496(this.f591);
         mc.field_1761.method_2911();
      }
   }

   private void m556() {
      BlockState v1 = this.m563();
      if (v1 != null) {

         if (mc.field_1690.field_1886.method_1434()) {
            int v2 = this.m564(v1);
            int v3 = mc.field_1724.method_31548().method_67532();
            if (v2 != -1 && v2 != v3) {
               this.f590 = v2;
               this.f591 = v3;
               ItemStack v4 = mc.field_1724.method_31548().method_5438(v3);
               this.f592 = v4.method_7960() ? ItemStack.field_8037 : v4.method_7972();
               if (v2 < 9) {
                  mc.field_1724.method_31548().method_61496(v2);
                  mc.field_1761.method_2911();
                  this.state = ci.di.MINING;
               } else {
                  this.f589.reset();
                  this.state = ci.di.PREPARE_SWAP;
               }

               return;
            }

            return;
         }
      }
   }

   private void m567(boolean state) {
      KeyBinding[] result = new KeyBinding[]{mc.field_1690.field_1894, mc.field_1690.field_1881, null, null, null};

      result[2] = mc.field_1690.field_1913;
      result[3] = mc.field_1690.field_1849;
      result[4] = mc.field_1690.field_1903;
      KeyBinding[] v2 = result;

      for (KeyBinding v6 : v2) {
         label29: {
            if (state) {
               Window v11 = mc.method_22683();

               if (InputUtil.method_15987(v11, v6.method_1429().method_1444())) {
                  v12 = true;
                  break label29;
               }
            }

            v12 = false;
         }

         boolean v7 = v12;
         v6.method_23481(v7);
         
      }
   }

   public static ItemStack getVisualItem() {
      ci v0 = getInstance();
      if (v0 != null && isSilentActive()) {

         return v0.f592;
      } else {

         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum di {
      MINING,
      PREPARE_SWAP,
      PREPARE_RESTORE,
      IDLE;
      // $VF: synthetic field
      private static final ci.di[] $VALUES = m555();
   }
}
