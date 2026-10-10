package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.PacketUtil;
import ru.white.utils.TimerUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Crystal Aura",
   desc = "Автоматическая аура под кристалы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class CrystalAura extends Module {
   private final BooleanSetting f300;
   private int f306;
   private final SliderSetting f299;
   private final SliderSetting f298;
   private final TimerUtil f304;
   private final BooleanSetting f301;
   private final SliderSetting f297 = new SliderSetting(this, "Дальность", 5.0F, 1.0F, 10.0F, 0.5F);
   private final TimerUtil f305;
   private final BooleanSetting f303;
   private final SliderSetting f302;

   public CrystalAura() {
      this.f298 = new SliderSetting(this, "Задержка постановки (мс)", 200.0F, 50.0F, 1000.0F, 50.0F);
      this.f299 = new SliderSetting(this, "Задержка взрыва (мс)", 100.0F, 50.0F, 1000.0F, 50.0F);
      this.f300 = new BooleanSetting(this, "Наведение на блок", true);
      this.f301 = new BooleanSetting(this, "Только возле врага", true);
      this.f302 = new SliderSetting(this, "Дальность врага", 6.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> {

         return this.f301.getValue();
      });
      this.f303 = new BooleanSetting(this, "Только обсидиан", false);
      this.f304 = new TimerUtil();
      this.f305 = new TimerUtil();
      this.f306 = -1;
   }

   @Override
   protected void onDisable() {
      this.m374();
      this.f304.reset();
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      this.f305.reset();
   }

   private void m373(int slot) {
      if (mc.field_1724.method_31548().method_67532() != slot) {
         if (this.f306 == -1) {

            this.f306 = mc.field_1724.method_31548().method_67532();
         }

         mc.field_1724.method_31548().method_61496(slot);
         mc.field_1761.method_2911();
      }
   }

   private int m372() {
      for (int v1 = 0; v1 < 9; v1++) {
         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         if (v2.method_7909() == Items.field_8301) {
            return v1;
         }
      }

      return -1;
   }

   private boolean m371(BlockPos pos) {
      Vec3d v2 = Vec3d.method_24953(pos);
      float v3 = this.f302.getValue();

      for (PlayerEntity v5 : mc.field_1687.method_18456()) {
         if (v5 != mc.field_1724) {
            Vec3d result = v5.method_73189();

            if (result.method_1022(v2) <= (double)v3) {
               return true;
            }
         }
      }

      return false;
   }

   private void m376(BlockPos pos) {
      Vec3d v2 = new Vec3d((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 1.0, (double)pos.method_10260() + 0.5);
      BlockHitResult v3 = new BlockHitResult(v2, Direction.field_11036, pos, false);

      mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v3);
      mc.field_1724.method_6104(Hand.field_5808);
   }

   private void m375(BlockPos pos) {
      double v2 = (double)pos.method_10263() + 0.5 - mc.field_1724.method_23317();
      double v4 = (double)pos.method_10264() + 1.0 - (mc.field_1724.method_23318() + (double)mc.field_1724.method_5751());
      double v6 = (double)pos.method_10260() + 0.5 - mc.field_1724.method_23321();
      double v8 = Math.sqrt(v2 * v2 + v6 * v6);
      float v10 = (float)(Math.toDegrees(Math.atan2(v6, v2)) - 90.0);
      float v11 = (float)(-Math.toDegrees(Math.atan2(v4, v8)));
      Rotation result = new Rotation;

      result./* $VF: Unable to resugar constructor */<init>(v10 + fq.random(-1.1248125F, 1.2148125F), v11 + fq.random(-1.1248125F, 1.2148125F));
      xd.update(result, 140.0F, 140.0F, 1, 1);
   }

   private void m369() {
      if (this.f305.isReached(this.f299.getValue().longValue())) {
         EndCrystalEntity v1 = null;
         double v2 = Double.MAX_VALUE;

         for (Entity v5 : mc.field_1687.method_18112()) {
            if (v5 instanceof EndCrystalEntity) {
               EndCrystalEntity v6 = (EndCrystalEntity)v5;
               double v7 = mc.field_1724.method_73189().method_1022(v6.method_73189());
               if (!(v7 > (double)this.f297.getValue().floatValue())) {

                  if (v7 < v2) {
                     v2 = v7;
                     v1 = v6;
                  }
               }
            }
         }

         if (v1 != null) {
            mc.field_1761.method_2918(mc.field_1724, v1);
            mc.field_1724.method_6104(Hand.field_5808);
            this.f305.reset();
         }
      }
   }

   @EventTarget
   public void update(EventTick event) {

      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.m369();
         BlockPos v2 = this.m370();
         if (v2 == null) {
            this.m374();
         } else {
            int v3 = this.m372();
            if (v3 == -1) {
               this.m374();
            } else {
               if (this.f300.getValue()) {
                  this.m375(v2);
               }

               if (this.f304.isReached(this.f298.getValue().longValue())) {
                  this.m373(v3);
                  this.m376(v2);
                  this.f304.reset();
               }
            }
         }
      }
   }

   private BlockPos m370() {
      int v1 = this.f297.getValue().intValue();
      BlockPos v2 = mc.field_1724.method_24515();
      int v3 = v2.method_10264();
      BlockPos v4 = null;
      double v5 = Double.MAX_VALUE;

      for (int v7 = -v1; v7 <= v1; v7++) {
         for (int v8 = -v1; v8 <= v1; v8++) {
            for (int v9 = -v1; v9 <= v1; v9++) {
               BlockPos v10 = v2.method_10069(v7, v8, v9);

               Block v11 = mc.field_1687.method_8320(v10).method_26204();
               boolean v12 = v11 == Blocks.field_10540;
               boolean v13 = !this.f303.getValue() && v11 == Blocks.field_9987;
               if ((v12 || v13)
                  && mc.field_1687.method_8320(v10.method_10084()).method_26215()
                  && mc.field_1687.method_8320(v10.method_10086(2)).method_26215()
                  && (!this.f301.getValue() || this.m371(v10))) {
                  double v14 = mc.field_1724.method_73189().method_1022(Vec3d.method_24953(v10));
                  if (v14 < v5) {
                     v5 = v14;
                     v4 = v10;
                  }
               }
            }
         }
      }

      return v4;
   }

   private void m374() {
      if (this.f306 != -1) {
         PlayerInventory result = mc.field_1724.method_31548();

         result.method_61496(this.f306);
         mc.field_1761.method_2911();
         this.f306 = -1;
      }
   }

   
}
