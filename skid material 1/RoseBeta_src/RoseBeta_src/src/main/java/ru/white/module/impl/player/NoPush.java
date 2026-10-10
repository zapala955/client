package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.util.math.BlockPos;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventMove;

@ModuleAnnotation(
   name = "No Push",
   desc = "Не даёт другим игрокам и воде толкать вас",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class NoPush extends Module {
   public BooleanSetting block = new BooleanSetting(this, "Блоки", true);
   public BooleanSetting xyiOblamova;
   public BooleanSetting entity = new BooleanSetting(this, "Сущности", true);

   public NoPush() {
      this.xyiOblamova = new BooleanSetting(this, "Вода [пузырьки]", false);
   }

   @EventTarget
   public void onEvent(EventMove event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_5869() && this.xyiOblamova.getValue()) {
         BlockPos v2 = mc.field_1724.method_24515();
         BlockState v3 = mc.field_1687.method_8320(v2);
         boolean v4 = v3.method_26204() == Blocks.field_10422 && !(Boolean)v3.method_11654(BubbleColumnBlock.field_10680);

         if (v4) {
            mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.0, mc.field_1724.method_18798().field_1350);
            boolean v5 = mc.field_1690.field_1832.method_1434();
            boolean v6 = mc.field_1690.field_1903.method_1434();
            float v7 = 0.6F;
            if (v5) {
               mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, (double)(-v7), mc.field_1724.method_18798().field_1350);
            } else if (v6) {
               double temp2 = (double)v7;

               mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, temp2, mc.field_1724.method_18798().field_1350);
            }
         }
      }
   }

   public static ni get() {
      Module result = uq.get(ni.class);

      return (ni)result;
   }

   
}
