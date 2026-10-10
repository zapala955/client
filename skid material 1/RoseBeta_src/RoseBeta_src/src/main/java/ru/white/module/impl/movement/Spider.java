package ru.white.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.white.event.EventTarget;
import ru.white.utils.StopWatch;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.event.impl.EventMove;

@ModuleAnnotation(
   name = "Spider",
   desc = "Позволяет лазить по стенам",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Spider extends Module {
   private final StopWatch f564;
   public ModeSetting modeSpider = new ModeSetting(this, "Режим", "Matrix");
   StopWatch placeTimer;

   private int m524() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int v1 = 0; v1 < 9; v1++) {
            ItemStack result = mc.field_1724.method_31548().method_5438(v1);

            ItemStack v2 = result;
            if (v2.method_7909() == Items.field_46991) {
               return v1;
            }
         }

         return -1;
      }
   }

   public Spider() {
      this.f564 = new StopWatch();
      this.placeTimer = new StopWatch();
   }

   public boolean hozColl() {

      return mc.field_1724.field_5976;
   }

   @EventTarget
   public void onMotion(EventMove e) {
      Direction v2 = mc.field_1724.method_5735();
      BlockPos v3 = BlockPos.method_49638(mc.field_1724.method_73189()).method_10093(v2);
      BlockState v4 = mc.field_1687.method_8320(v3);
      Block v5 = v4.method_26204();
      BlockPos v6 = BlockPos.method_49638(mc.field_1724.method_73189());
      BlockState v7 = mc.field_1687.method_8320(v6);
      Block v8 = v7.method_26204();
      boolean v9 = v5 instanceof TrapdoorBlock
         && Boolean.TRUE.equals(v4.method_11654(Properties.field_12537))
         && v4.method_28498(Properties.field_12481);

      boolean v10 = v8 instanceof TrapdoorBlock
         && Boolean.TRUE.equals(v7.method_11654(Properties.field_12537))
         && v7.method_28498(Properties.field_12481);
      if (!(v5 instanceof FenceBlock)
         && !v4.method_26164(BlockTags.field_15504)
         && !(v5 instanceof FenceGateBlock)
         && !(v5 instanceof LanternBlock)
         && !(v5 instanceof LightningRodBlock)
         && !v9) {
         boolean v29 = false;
      } else {
         boolean result = true;
      }

      if (!(v8 instanceof FenceBlock)
         && !v7.method_26164(BlockTags.field_15504)
         && !(v8 instanceof FenceGateBlock)
         && !(v8 instanceof LanternBlock)
         && !(v8 instanceof LightningRodBlock)
         && !v10) {
         boolean v31 = false;
      } else {
         boolean v30 = true;
      }

      if (this.modeSpider.is("Matrix") && this.f564.finished(-6523568980702405310L + 6523568980702405380L) && this.hozColl()) {
         e.ground(true);
         mc.field_1724.method_24830(true);
         mc.field_1724.method_6043();
         mc.field_1724.field_6017 = 0.0;
         this.f564.reset();
      }

      if (this.modeSpider.is("FT - 2 2") && this.hozColl() && !mc.field_1724.method_24828()) {
         xd.update(new Rotation(mc.field_1773.method_19418().method_19330(), 60.0F), 255.0F, 255.0F, 0, 50);
         BlockHitResult v13 = (BlockHitResult)mc.field_1765;
         BlockPos v14 = v13.method_17777();
         BlockState v15 = mc.field_1687.method_8320(v14);
         boolean v16 = v13.method_17780() == Direction.field_11036;
         boolean v17 = !v15.method_45474();
         boolean v18 = mc.field_1687.method_8320(v14.method_10084()).method_45474();
         if (this.placeTimer.hasTimeElapsed(5592188222528674924L + -5592188222528674854L)) {
            mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v13);
            mc.field_1724.method_6104(Hand.field_5808);
            this.placeTimer.reset();
            if (this.f564.finished(7254162280771864177L + -7254162280771864087L)) {
               e.ground(true);
               mc.field_1724.method_24830(true);
               mc.field_1724.method_6043();
               mc.field_1724.field_6017 = 0.0;
               this.f564.reset();
            }
         }
      }

      if (this.modeSpider.is("FT - 2") && this.hozColl() && !mc.field_1724.method_24828()) {
         int v23 = this.m524();
         if (v23 != -1) {
            BlockHitResult v24 = (BlockHitResult)mc.field_1765;
            BlockPos v25 = v24.method_17777();
            BlockState v26 = mc.field_1687.method_8320(v25);
            boolean v27 = v24.method_17780() == Direction.field_11036;
            boolean v28 = !v26.method_45474();
            ClientWorld v32 = mc.field_1687;
            BlockPos v33 = v25.method_10084();

            boolean v19 = v32.method_8320(v33).method_45474();
            if (this.placeTimer.hasTimeElapsed(-5036317657998067416L + 5036317657998067536L)) {
               int v20 = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(v23);
               mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v24);
               mc.field_1724.method_6104(Hand.field_5808);
               mc.field_1724.method_31548().method_61496(v20);
               this.placeTimer.reset();
            }

            if (this.f564.finished(5921802332514237332L + -5921802332514237182L)) {
               e.ground(true);
               mc.field_1724.method_24830(true);
               mc.field_1724.method_6043();
               mc.field_1724.field_6017 = 0.0;
               this.f564.reset();
            }
         }
      }
   }

   
}
