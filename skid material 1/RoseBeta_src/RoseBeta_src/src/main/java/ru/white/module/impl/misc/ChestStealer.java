package ru.white.module.impl.misc;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.PacketUtil;
import ru.white.utils.ItemUtil;
import ru.white.utils.BlockUtil;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Chest Stealer",
   desc = "Данный модуль был сделан акарачком поэтому может работать через жопу",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class ChestStealer extends Module {
   private final BlockUtil f946;
   private long f947;
   public final BooleanSetting autoClan;
   private BlockPos f948;
   private final BlockUtil f945;
   private boolean f949;
   private cm.dm f950;
   public final BooleanSetting closeEmpty;
   private final BlockUtil f943;
   public final BooleanSetting autoEc;
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Normal", "Warden", "Danj");
   private final BlockUtil f944;
   public final BooleanSetting autoOpen;
   private final Set<BlockPos> f942;

   private void m1217() {
      switch (this.f950) {
         case WAITING_EC:
            if (mc.field_1755 instanceof GenericContainerScreen) {
               this.f950 = cm.dm.MOVING_TO_EC;
               this.f945.reset();
               this.f947 = (long)fq.random(120.0, 150.0);
            } else if (this.f945.hasTimePassed(-5577334861500426691L + 5577334861500431691L)) {
               this.f950 = cm.dm.IDLE;
            }
            break;
         case IDLE:
            if (!(mc.field_1755 instanceof GenericContainerScreen v1)) {
               this.f950 = cm.dm.IDLE;
               return;
            }

            if (!this.f945.hasTimePassed(this.f947)) {
               return;
            }

            int v9 = ((GenericContainerScreenHandler)v1.method_17577()).method_17388();
            int v3 = v9 * (9);
            int v4 = ((GenericContainerScreenHandler)v1.method_17577()).field_7761.size();
            boolean v5 = false;

            for (int v6 = v3; v6 < v4; v6++) {
               Slot v7 = ((GenericContainerScreenHandler)v1.method_17577()).method_7611(v6);
               if (v7.method_7681() && this.isValuableItem(v7.method_7677())) {
                  mc.field_1761.method_2906(((GenericContainerScreenHandler)v1.method_17577()).field_7763, v6, 0, SlotActionType.field_7794, mc.field_1724);

                  this.f947 = (long)fq.random(120.0, 150.0);
                  this.f945.reset();
                  v5 = true;
                  return;
               }
            }

            if (!v5) {
               this.f950 = cm.dm.CLOSING_EC;
            }
            break;
         case MOVING_TO_EC:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.f950 = cm.dm.IDLE;
      }
   }

   public ChestStealer() {
      this.autoClan = new BooleanSetting(this, "AutoClanStorage", false);
      this.autoEc = new BooleanSetting(this, "Авто /EventAttack", false);
      this.closeEmpty = new BooleanSetting(this, "Закрывать пустой", true);
      this.autoOpen = new BooleanSetting(this, "AutoOpen", true);
      this.f942 = new HashSet<>();
      this.f943 = new BlockUtil();
      this.f944 = new BlockUtil();
      this.f945 = new BlockUtil();
      this.f946 = new BlockUtil();
      this.f947 = 0L;
      this.f948 = null;
      this.f949 = false;
      this.f950 = cm.dm.IDLE;
   }

   private float m1222(float current, float target, float maxStep) {
      float v4 = MathHelper.method_15393(target - current);
      if (v4 > maxStep) {
         v4 = maxStep;
      }

      if (v4 < -maxStep) {
         v4 = -maxStep;
      }

      return current + v4;
   }

   private boolean m1215() {
      String v1 = this.m1213();
      if (this.autoClan.getValue()) {
         int temp2 = 875517746;

         return v1.contains(StrDec.d("Qc2iNfSSL6aBnbgxH4hFkZU+1gIArVt3vmDs9Q==", temp2));
      } else {
         return true;
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.mode.is("Warden") || this.mode.is("Danj")) {
            if (this.autoOpen.getValue()) {
               if (!(mc.field_1755 instanceof GenericContainerScreen)) {
                  double v2 = mc.field_1724.method_23317();
                  double v4 = mc.field_1724.method_23321();
                  boolean v6 = Math.abs(Math.abs(v2) - 2000.0) <= 150.0 && Math.abs(Math.abs(v4) - 2000.0) <= 150.0;
                  if (v6) {
                     BlockPos v7 = this.m1218();
                     if (v7 != null) {
                        float[] v8 = this.m1221(v7);
                        float v9 = this.m1222(mc.field_1724.method_36454(), v8[0], 15.0F);

                        float v10 = this.m1222(mc.field_1724.method_36455(), v8[1], 15.0F);
                        xd.update(new Rotation(v9, v10), 400.0F, 400.0F, 1, 1);
                        if (this.f944.hasTimePassed(3163749777107290592L + -3163749777107290392L)) {
                           double v11 = 16.0;
                           if (mc.field_1724.method_5707(v7.method_46558()) < v11) {
                              BlockHitResult result = new BlockHitResult;

                              result./* $VF: Unable to resugar constructor */<init>(v7.method_46558(), Direction.field_11036, v7, false);
                              BlockHitResult v13 = result;
                              mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v13);
                              mc.field_1724.method_6104(Hand.field_5808);
                              this.f942.add(v7);
                              this.f944.reset();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean m1214() {
      String v1 = this.m1213();

      if (this.mode.is("Warden")) {
         return v1.contains("Сундук");
      } else {
         return this.mode.is("Danj") ? v1.contains("Бочка") : true;
      }
   }

   @EventTarget
   public void onLoadWorld(EventSync event) {
      this.f942.clear();

      this.f944.reset();
      this.f943.reset();
   }

   private float[] m1221(BlockPos pos) {
      Vec3d v2 = mc.field_1724.method_33571();
      Vec3d v3 = pos.method_46558();
      double v4 = v3.field_1352 - v2.field_1352;
      double v6 = v3.field_1351 - v2.field_1351;
      double v8 = v3.field_1350 - v2.field_1350;

      double v10 = Math.sqrt(v4 * v4 + v8 * v8);
      float v12 = (float)(Math.atan2(v8, v4) * 180.0 / Math.PI) - 90.0F;
      float v13 = (float)(-(Math.atan2(v6, v10) * 180.0 / Math.PI));
      return new float[]{v12, v13};
   }

   private void m1216(GenericContainerScreen screen) {
      int v2 = ((GenericContainerScreenHandler)screen.method_17577()).method_17388();
      int v3 = v2 * (9);
      boolean v4 = false;
      int temp2 = v9 + 1;

      boolean v5 = true;

      for (int v6 = 0; v6 < v3; v6++) {
         Slot v7 = ((GenericContainerScreenHandler)screen.method_17577()).method_7611(v6);
         if (v7.method_7681()) {
            v5 = false;
            boolean v8 = true;
            if (this.mode.is("Warden") || this.mode.is("Danj")) {
               v8 = this.isValuableItem(v7.method_7677());
            }

            if (v8) {
               v4 = true;
               mc.field_1761.method_2906(((GenericContainerScreenHandler)screen.method_17577()).field_7763, v6, 0, SlotActionType.field_7794, mc.field_1724);
               this.f943.reset();
               this.f947 = 0L;
               return;
            }
         }
      }

      if (!v4 && (v5 || this.closeEmpty.getValue())) {
         mc.field_1724.method_7346();
         if (this.autoEc.getValue()) {
            this.f950 = cm.dm.WAITING_EC;
            this.f945.reset();
            mc.field_1724.field_3944.method_45730("EventAttack");
         } else if (this.autoClan.getValue() && !iq.isPvp()) {
            am v12 = Client.get().moduleManager().get(am.class);
            if (v12 != null) {
               if (!v12.isEnabled()) {
                  v12.setEnabled(true);
               }

               v12.trigger();
               v12.triggeredByChestStealer = true;
            }
         }
      }
   }

   private boolean m1220(BlockPos pos) {
      Box v2 = new Box(pos).method_1009(0.0, 2.0, 0.0);

      for (ArmorStandEntity v5 : mc.field_1687.method_8390(ArmorStandEntity.class, v2, val -> {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return true;
      })) {
         if (v5.method_16914()) {
            String v6 = v5.method_5797().getString();
            if (v6.matches(".*\\d.*") || v6.contains(":")) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.f942.clear();
      this.f950 = cm.dm.IDLE;
      this.f948 = null;

      this.f949 = false;
   }

   private String m1213() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         String result = v1.method_25440().getString();

         return result;
      } else {
         return "";
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if ((this.mode.is("Warden") || this.mode.is("Danj")) && !this.autoOpen.getValue() && mc.field_1755 instanceof GenericContainerScreen result) {

            GenericContainerScreen v2 = result;
            if (this.f946.hasTimePassed(-3406515050729168870L + 3406515050729169370L)) {
               String v14 = v2.method_25440().getString();
               boolean v4 = v14.contains("0:00") || v14.contains("0.00");
               boolean v5 = this.m1219(v2);
               if (v4 || v5 && this.f949) {
                  mc.field_1724.method_7346();
                  this.f946.reset();
                  this.f949 = true;
                  return;
               }

               if (!v5) {
                  this.f949 = false;
               }
            }
         }

         if (this.autoClan.getValue() && this.autoEc.getValue()) {
            this.autoEc.set(false);
         }

         if (this.f950 != cm.dm.IDLE) {
            this.m1217();
         } else {
            if (mc.field_1755 instanceof GenericContainerScreen v13) {
               am v16 = Client.get().moduleManager().get(am.class);
               if (v16 != null && v16.isWorking()) {
                  return;
               }

               if (this.mode.is("Warden") || this.mode.is("Danj")) {
                  double v17 = mc.field_1724.method_23317();
                  double v6 = mc.field_1724.method_23318();
                  double v8 = mc.field_1724.method_23321();
                  boolean v10 = Math.abs(Math.abs(v17) - 2000.0) <= 150.0 && Math.abs(Math.abs(v8) - 2000.0) <= 150.0;
                  if (!v10) {
                     return;
                  }

                  if (!this.m1214()) {
                     return;
                  }

                  if (((GenericContainerScreenHandler)v13.method_17577()).method_17388() == 6) {
                     return;
                  }
               }

               if (!this.f943.hasTimePassed(this.f947)) {
                  return;
               }

               this.m1216(v13);
               return;
            }

            this.f947 = 0L;
         }
      }
   }

   public boolean isValuableItem(ItemStack stack) {
      Item v2 = stack.method_7909();
      String v3 = stack.method_7964().getString();
      if (v2 == Items.field_8107 || v3.contains("Стрела")) {
         return false;
      } else if (v3.contains("Хлопушка")) {
         return false;
      } else if (v3.contains("★")) {
         return true;
      } else {
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
            && v2 != Items.field_8791
            && v2 != Items.field_8603
            && v2 != Items.field_22022
            && v2 != Items.field_22027
            && v2 != Items.field_22029
            && v2 != Items.field_22028
            && v2 != Items.field_22030
            && v2 != Items.field_8366
            && v2 != Items.field_8668) {

            if (v2 != Items.field_8543 && v2 != Items.field_8675 && v2 != Items.field_8288 && v2 != Items.field_8833 && v2 != Items.field_38418) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean m1219(GenericContainerScreen screen) {
      int v2 = ((GenericContainerScreenHandler)screen.method_17577()).method_17388();
      int v3 = v2 * (9);

      for (int v4 = 0; v4 < v3; v4++) {
         if (((GenericContainerScreenHandler)screen.method_17577()).method_7611(v4).method_7681()) {

            return false;
         }
      }

      return true;
   }

   private BlockPos m1218() {
      BlockPos v1 = mc.field_1724.method_24515();
      BlockPos v2 = null;
      double v3 = 36.0;

      for (int v5 = -6; v5 <= 6; v5++) {
         for (int v6 = -6; v6 <= 6; v6++) {
            for (int v7 = -6; v7 <= 6; v7++) {
               BlockPos result = v1.method_10069(v5, v6, v7);

               BlockPos v8 = result;
               boolean v9 = false;
               if (this.mode.is("Warden")) {
                  v9 = mc.field_1687.method_8320(v8).method_26204() == Blocks.field_10034
                     || mc.field_1687.method_8320(v8).method_26204() == Blocks.field_10380;
               } else if (this.mode.is("Danj")) {
                  v9 = mc.field_1687.method_8320(v8).method_26204() == Blocks.field_16328;
               } else {
                  v9 = mc.field_1687.method_8320(v8).method_26204() == Blocks.field_10034
                     || mc.field_1687.method_8320(v8).method_26204() == Blocks.field_10380
                     || mc.field_1687.method_8320(v8).method_26204() == Blocks.field_16328;
               }

               if (v9) {
                  double v10 = v8.method_46558().method_1025(mc.field_1724.method_73189());
                  if (!(v10 > 36.0) && !this.f942.contains(v8)) {
                     if (this.m1220(v8)) {
                        
                     } else if (v10 < v3) {
                        v3 = v10;
                        v2 = v8;
                     }
                  }
               }
            }
         }
      }

      return v2;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.f942.clear();
      this.f943.reset();
      this.f944.reset();

      this.f950 = cm.dm.IDLE;
      this.f946.reset();
      this.f948 = null;
      int result = v2 * (v2 + 1);

      this.f949 = false;
   }

   @Environment(EnvType.CLIENT)
   private static enum dm {
      CLOSING_EC,
      WAITING_EC,
      IDLE,
      MOVING_TO_EC;
      // $VF: synthetic field
      private static final cm.dm[] $VALUES = m1212();
   }
}
