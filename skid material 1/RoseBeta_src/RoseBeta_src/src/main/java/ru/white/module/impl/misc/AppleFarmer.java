package ru.white.module.impl.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleHelper;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Apple Farmer",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AppleFarmer extends Module {
   private BlockPos f842;
   private final List<BlockPos> f840;
   private static final int PLANT_DISTANCE = 2;
   private static final int TREE_SCAN_HEIGHT = 8;
   private int f843;
   public final SliderSetting range = new SliderSetting(this, "Дистанция", 4.5F, 3.0F, 4.5F, 0.1F);
   private nl.ol f838 = nl.ol.FIND_SPOT;
   private static final int TREE_SCAN_RADIUS = 4;
   private BlockPos f839 = null;
   private int f844;
   private Direction f841;

   private int m1013(BlockPos pos, BlockState state) {
      if (this.m1009(state)) {
         return 3;
      } else {
         int v3 = this.m1014(pos);

         if (v3 > 0) {
            return 0;
         } else {
            return v3 == 0 ? 1 : 2;
         }
      }
   }

   private Vec3d m1003(BlockPos pos, Direction side) {
      Vec3d result = new Vec3d;
      double temp2 = (double)pos.method_10263() + 0.5 + (double)side.method_10148() * 0.5;

      result./* $VF: Unable to resugar constructor */<init>(
         temp2, (double)pos.method_10264() + 0.5 + (double)side.method_10164() * 0.5, (double)pos.method_10260() + 0.5 + (double)side.method_10165() * 0.5
      );
      return result;
   }

   private BlockPos m1015() {
      Iterator v1 = this.f840.iterator();

      while (v1.hasNext()) {
         BlockPos v2 = (BlockPos)v1.next();
         if (this.m1016(v2) != null) {
            return v2;
         }
      }

      return null;
   }

   private boolean m1007(BlockPos pos) {
      BlockState v2 = mc.field_1687.method_8320(pos);
      boolean result = v2.method_26215();

      if (!result) {
         result = v2.method_45474();

         if (!result) {
            return false;
         }
      }

      return true;
   }

   private BlockHitResult m1016(BlockPos pos) {
      Vec3d v2 = mc.field_1724.method_33571();
      double[] v3 = new double[]{0.5, 0.2, 0.8};

      for (double v7 : v3) {
         for (double v12 : v3) {

            for (double v17 : v3) {
               Vec3d v24 = new Vec3d;

               v24./* $VF: Unable to resugar constructor */<init>(
                  (double)pos.method_10263() + v7, (double)pos.method_10264() + v12, (double)pos.method_10260() + v17
               );
               Vec3d v19 = v24;
               BlockHitResult v20 = mc.field_1687
                  .method_17742(new RaycastContext(v2, v19, ShapeType.field_17559, FluidHandling.field_1348, mc.field_1724));
               if (v20.method_17783() == Type.field_1332 && v20.method_17777().equals(pos)) {
                  return v20;
               }
            }
         }
      }

      return null;
   }

   private Rotation m1004(BlockPos pos) {
      Vec3d temp1 = new Vec3d;
      double temp3 = (double)pos.method_10263() + 0.5;

      double v5 = (double)pos.method_10264();

      v5 += 0.5;
      double v7 = (double)pos.method_10260();

      temp1./* $VF: Unable to resugar constructor */<init>(temp3, v5, v7 + 0.5);
      return this.m1005(temp1);
   }

   private boolean m1011(BlockPos pos) {
      double result = (double)Math.min(this.range.getValue(), 4.5F);

      double v2 = result;
      return mc.field_1724.method_33571().method_1025(Vec3d.method_24953(pos)) <= v2 * v2;
   }

   private int m1014(BlockPos pos) {
      if (this.f839 == null) {
         return 0;
      } else {
         Direction v2 = this.f841.method_10153();
         int result = pos.method_10263();

         int v3 = result - this.f839.method_10263();
         int v4 = pos.method_10260() - this.f839.method_10260();

         return v3 * v2.method_10148() + v4 * v2.method_10165();
      }
   }

   private boolean m1006(BlockPos pos) {
      Block v2 = mc.field_1687.method_8320(pos).method_26204();
      if (v2 != Blocks.field_10219 && v2 != Blocks.field_10566 && v2 != Blocks.field_10253) {
         if (v2 != Blocks.field_10520) {
            return false;
         }

         
      }

      return true;
   }

   private void m997() {
      if (this.f839 == null) {
         this.f838 = nl.ol.FIND_SPOT;
      } else {
         BlockState v1 = mc.field_1687.method_8320(this.f839);
         if (v1.method_26204() == Blocks.field_10394) {
            this.f838 = nl.ol.BONEMEAL;
            this.f843 = 0;
         } else if (!v1.method_45474()) {

            this.f838 = nl.ol.FIND_SPOT;
            this.f843 = 0;
         } else {
            int v2 = this.m1017(Items.field_17535);
            if (v2 == -1) {
               zp.addChatMessage("§c[AppleFarmer] §fНет саженцев");
               this.toggle();
            } else if (this.m1002(this.f839.method_10074())) {
               int v3 = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(v2);
               this.m1001(this.f839.method_10074());
               mc.field_1724.method_31548().method_61496(v3);
               this.f838 = nl.ol.BONEMEAL;
               this.f843 = 0;
            }
         }
      }
   }

   public AppleFarmer() {
      this.f840 = new ArrayList<>();
      this.f841 = Direction.field_11043;
      this.f842 = null;
      this.f843 = 0;
      this.f844 = 0;
   }

   private boolean m1019() {

      for (int v1 = 9; v1 < 36; v1++) {
         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         if (v2.method_7909() == Items.field_8324) {
            mc.field_1761
               .method_2906(mc.field_1724.field_7498.field_7763, v1, mc.field_1724.method_31548().method_67532(), SlotActionType.field_7791, mc.field_1724);
            return true;
         }
      }

      return false;
   }

   @Override
   public void onDisable() {
      xd.currentTask = xd.df.IDLE;
      xd.currentPriority = 0;

      xd.targetRotation = null;
      wd.active = false;
      this.f842 = null;
      super.onDisable();
   }

   private int m1017(Item item) {
      for (int v2 = 0; v2 < 9; v2++) {
         PlayerInventory result = mc.field_1724.method_31548();

         if (result.method_5438(v2).method_7909() == item) {
            return v2;
         }
      }

      return -1;
   }

   private void m999() {
      this.f840.clear();
      BlockPos v1 = this.f839;

      for (int v2 = -4; v2 <= 4; v2++) {

         while (true) {
            int temp1 = 8;

            if (v3 > temp1) {
               break;
            }

            for (int v4 = -4; v4 <= 4; v4++) {
               BlockPos v5 = v1.method_10069(v2, v3, v4);
               BlockState v6 = mc.field_1687.method_8320(v5);
               if (this.m1008(v6) && this.m1011(v5)) {
                  this.f840.add(v5);
               }
            }

            v3++;
         }
      }

      boolean result = this.f840.isEmpty();

      if (result) {
         this.f838 = nl.ol.PLACE;
      } else {
         this.f840.sort(this::m1012);
         this.f842 = null;

         this.f838 = nl.ol.BREAKING;
      }
   }

   private boolean m1002(BlockPos pos) {
      Rotation v2 = this.m1005(this.m1003(pos, Direction.field_11036));
      xd.update(v2, 65.0F, 65.0F, 65.0F, 65.0F, 2, 20, false);
      Rotation result = new Rotation(mc.field_1724);

      float v5 = result.getDelta(v2);

      return v5 <= 6.0F;
   }

   private boolean m1008(BlockState state) {
      if (!this.m1009(state)) {

         if (!this.m1010(state)) {
            return false;
         }
      }

      return true;
   }

   private boolean m1009(BlockState state) {
      boolean v4;
      if (state.method_26204() == Blocks.field_10431) {

         v4 = true;
         
      } else {
         v4 = false;
      }

      return v4;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.f838 = nl.ol.FIND_SPOT;
      this.f839 = null;
      this.f840.clear();
      this.f842 = null;
      this.f844 = 0;

      if (mc.field_1724 != null) {
         this.f841 = mc.field_1724.method_5735();
      }

      this.f843 = 0;
   }

   private boolean m1010(BlockState state) {
      return state.method_26204() == Blocks.field_10503;
   }

   private int m1012(BlockPos first, BlockPos second) {
      BlockState v3 = mc.field_1687.method_8320(first);
      BlockState v4 = mc.field_1687.method_8320(second);
      int v5 = this.m1013(first, v3);
      int v6 = this.m1013(second, v4);
      if (v5 != v6) {
         return Integer.compare(v5, v6);
      } else if (this.m1009(v3) && this.m1009(v4)) {
         return Integer.compare(first.method_10264(), second.method_10264());
      } else {
         int v7 = this.m1014(first);
         int v8 = this.m1014(second);
         if (v7 != v8) {
            return Integer.compare(v8, v7);
         } else {
            int v9 = Integer.compare(first.method_10264(), second.method_10264());
            if (v9 != 0) {
               return v9;
            } else {
               double v10 = first.method_19770(mc.field_1724.method_73189());
               double v12 = second.method_19770(mc.field_1724.method_73189());
               return Double.compare(v10, v12);
            }
         }
      }
   }

   private void m1001(BlockPos pos) {
      Vec3d v2 = this.m1003(pos, Direction.field_11036);
      BlockHitResult result = new BlockHitResult;

      result./* $VF: Unable to resugar constructor */<init>(v2, Direction.field_11036, pos, false);
      BlockHitResult v3 = result;
      mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v3);

      mc.field_1724.method_6104(Hand.field_5808);
   }

   private void m998() {

      if (this.f839 != null) {
         BlockState v1 = mc.field_1687.method_8320(this.f839);
         if (this.m1009(v1)) {
            this.f838 = nl.ol.SCAN_TREE;
         } else if (v1.method_45474()) {
            this.f838 = nl.ol.PLACE;
         } else if (v1.method_26204() != Blocks.field_10394) {
            this.f838 = nl.ol.FIND_SPOT;
         } else {
            int v2 = this.m1017(Items.field_8324);
            if (v2 == -1) {
               if (!this.m1019()) {
                  zp.addChatMessage("§c[AppleFarmer] §fНет костной муки");
                  this.toggle();
                  return;
               }

               v2 = mc.field_1724.method_31548().method_67532();
            }

            if (this.m1002(this.f839)) {
               int v3 = mc.field_1724.method_31548().method_67532();

               mc.field_1724.method_31548().method_61496(v2);
               this.m1001(this.f839);
               mc.field_1724.method_31548().method_61496(v3);
               this.f843 = 0;
            }
         }
      }
   }

   private void m1018(boolean isAxe) {
      ItemStack v3 = mc.field_1724.method_6047();
      if (!isAxe || !(v3.method_7909() instanceof AxeItem)) {
         if (isAxe || !(v3.method_7909() instanceof HoeItem)) {
            for (int v4 = 0; v4 < 9; v4++) {
               ItemStack v5 = mc.field_1724.method_31548().method_5438(v4);
               if (!v5.method_7960()) {
                  if (isAxe && v5.method_7909() instanceof AxeItem) {
                     v2 = v4;
                     break;
                  }

                  if (!isAxe && v5.method_7909() instanceof HoeItem) {
                     v2 = v4;
                     break;
                  }
               }
            }

            if (v2 != -1) {
               PlayerInventory result = mc.field_1724.method_31548();

               result.method_61496(v2);
            }
         }
      }
   }

   private void m1000() {
      this.f840.removeIf(pos -> {

         return !this.m1008(mc.field_1687.method_8320(pos)) || !this.m1011(pos);
      });
      if (this.f840.isEmpty()) {
         this.f838 = nl.ol.PLACE;
         this.f842 = null;
      } else {
         BlockPos v1 = this.m1015();
         if (v1 == null) {
            this.f838 = nl.ol.SCAN_TREE;
            this.f842 = null;
            this.f843 = 0;
         } else {
            BlockState v2 = mc.field_1687.method_8320(v1);
            BlockHitResult v3 = this.m1016(v1);
            if (v3 == null) {
               this.f842 = null;
            } else {
               boolean v4 = this.m1010(v2);
               boolean v5 = this.m1009(v2);
               if (v5) {
                  this.m1018(true);
               } else if (v4) {
                  this.m1018(false);
               }

               Rotation v6 = this.m1005(v3.method_17784());
               xd.update(v6, 65.0F, 65.0F, 65.0F, 65.0F, 2, 20, false);
               if (!(new Rotation(mc.field_1724).getDelta(v6) > 6.0F)) {

                  if (!v1.equals(this.f842)) {
                     mc.field_1761.method_2910(v1, v3.method_17780());
                     this.f842 = v1;
                  } else {
                     mc.field_1761.method_2902(v1, v3.method_17780());
                  }

                  mc.field_1724.method_6104(Hand.field_5808);
               }
            }
         }
      }
   }

   private void m996() {
      this.f841 = mc.field_1724.method_5735();
      BlockPos v1 = mc.field_1724.method_24515();
      BlockPos v2 = v1.method_10093(this.f841);
      BlockPos v3 = v1.method_10079(this.f841, 2);
      BlockState v4 = mc.field_1687.method_8320(v3);
      if (this.m1007(v2) && this.m1007(v2.method_10084())) {
         if (v4.method_26204() == Blocks.field_10394) {
            this.f839 = v3;

            this.f838 = nl.ol.BONEMEAL;
            this.f843 = 0;
         } else if (this.m1009(v4)) {
            this.f839 = v3;
            this.f838 = nl.ol.SCAN_TREE;
            this.f843 = 0;
         } else {
            BlockPos v5 = v3.method_10074();
            if (this.m1006(v5) && v4.method_45474()) {
               this.f839 = v5.method_10084();
               this.f838 = nl.ol.PLACE;
            } else {
               zp.addChatMessage(
                  "§c[AppleFarmer] §fВстаньте напротив места посадки: земля должна быть через один блок перед вами"
               );
               this.toggle();
            }

            this.f843 = 0;
         }
      } else {
         zp.addChatMessage(
            "§c[AppleFarmer] §fМежду вами и местом посадки должен быть свободный блок"
         );
         this.toggle();
         this.f843 = 0;
      }
   }

   private void m1020() {
      for (int v1 = 0; v1 < 9; v1++) {
         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         boolean v3 = v2.method_7909() == Items.field_8324 || v2.method_7909() == Items.field_17535;
         if (v3 && v2.method_7947() < 64) {
            int v5 = v2.method_7947();

            while (v6 < 36) {
               ItemStack v7 = mc.field_1724.method_31548().method_5438(v6);
               if (v7.method_7909() == v2.method_7909() && v7.method_7947() > v5) {
                  v4 = v6;
                  v5 = v7.method_7947();
                  if (v5 == 64) {
                     break;
                  }
               }

               v6++;
               
            }

            if (v4 != -1) {

               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, v4, v1, SlotActionType.field_7791, mc.field_1724);
               this.f844 = 0;
               return;
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.f844++;
         if (this.f844 > 4) {
            this.m1020();
            this.f844 = 0;
         }

         this.f843++;
         if (this.f838 == nl.ol.BREAKING || this.f843 >= 2) {
            switch (this.f838) {
               case SCAN_TREE:
                  this.m996();
                  break;
               case BONEMEAL:
                  this.m997();
                  break;
               case PLACE:
                  this.m998();
                  break;
               case BREAKING:
                  this.m999();
                  int result = v2 * (v2 + 1) % 2;

                  if (result != 0) {

                     }
                  break;
               case FIND_SPOT:
                  this.m1000();
            }
         }
      }
   }

   private Rotation m1005(Vec3d target) {
      if (mc.field_1724 == null) {
         Rotation v17 = new Rotation(0.0F, 0.0F);

         return v17;
      } else {
         Vec3d v2 = mc.field_1724.method_33571();
         double v3 = target.field_1352 - v2.field_1352;

         double v5 = target.field_1351 - v2.field_1351;
         double v7 = target.field_1350 - v2.field_1350;
         double v16 = v3 * v3 + v7 * v7;

         double v9 = Math.sqrt(v16);
         float v11 = (float)Math.toDegrees(Math.atan2(-v3, v7));
         float v12 = (float)(-Math.toDegrees(Math.atan2(v5, v9)));
         return new Rotation(v11, v12);
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum ol {
      SCAN_TREE,
      BONEMEAL,
      PLACE,
      BREAKING,
      FIND_SPOT;
      // $VF: synthetic field
      private static final nl.ol[] $VALUES = m995();
   }
}
