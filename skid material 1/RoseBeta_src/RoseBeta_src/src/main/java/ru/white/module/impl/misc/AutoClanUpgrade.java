package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.white.event.EventTarget;
import ru.white.utils.StopWatch;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.MathUtil;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Clan Upgrade",
   desc = "Автоматическая прокачка клана через факела",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoClanUpgrade extends Module {
   private final StopWatch f919 = new StopWatch();
   private boolean f920 = false;

   @EventTarget
   public void onUpdate(EventTick event) {
      int v2 = dq.getItemInHotBar(Items.field_8810);
      if (mc.field_1687.method_27983().method_29177().method_12832().equals("lobby")) {
         if (mc.field_1724.field_6012 % (200) == 0) {
            zp.addChatMessage("Тепнитесь на РТП");
         }
      } else if (v2 == -1) {
         if (mc.field_1724.field_6012 % (200) == 0) {
            zp.addChatMessage("Нужен факел в хотбаре");
         }
      } else {
         BlockPos v3 = mc.field_1724.method_24515().method_10074();
         BlockPos v4 = v3.method_10084();
         if (mc.field_1687.method_8320(v3).method_26212(mc.field_1687, v3)) {
            if (mc.field_1724.field_6012 % 1 == 0) {
               float v5 = 25.0F;
               float v6 = 87.5F;
               xd.update(new Rotation(v5, v6), 255.0F, 255.0F, 0, 100);
            }

            byte v8 = 5;
            if (this.f919.hasReached((double)v8)) {
               this.f919.reset();
               if (mc.field_1687.method_8320(v4).method_26215()) {

                  mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, new BlockHitResult(v3.method_46558(), Direction.field_11036, v3, false));
               }

               if (mc.field_1687.method_8320(v4).method_26204() == Blocks.field_10336) {
                  mc.field_1761.method_2910(v4, Direction.field_11039);
                  mc.field_1724.method_6104(Hand.field_5808);
               }
            }
         }
      }
   }

   
}
