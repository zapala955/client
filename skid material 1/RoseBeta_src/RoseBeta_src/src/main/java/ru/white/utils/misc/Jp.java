package ru.white.utils.misc;

import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.ItemUtil;
import ru.white.utils.media.Lr;

@Environment(EnvType.CLIENT)
public final class Jp implements IMinecraft {
   private static int count = 0;

   public static boolean isBoxInBlock(Box box, Block block) {
      return isBox(box, pos -> {
         int v2 = 1767391747;
         int temp2 = v2 + 1;

         return mc.field_1687.method_8320(pos).method_26204().equals(block);
      });
   }

   public static boolean hasPreMovementRestrictions(lr simulatedPlayer) {
      if (!simulatedPlayer.hasStatusEffect(StatusEffects.field_5919)
         && !simulatedPlayer.hasStatusEffect(StatusEffects.field_5902)
         && !isBoxInBlock(simulatedPlayer.boundingBox, Blocks.field_10343)
         && !simulatedPlayer.isSubmergedInWater()) {

         if (!simulatedPlayer.isInLava()
            && !simulatedPlayer.isClimbing()
            && (canChangeIntoPose(EntityPose.field_18076) || !mc.field_1724.method_18276())
            && !mc.field_1724.method_31549().field_7479) {
            return false;
         }
      }

      return true;
   }

   public static boolean isPlayerInCriticalState() {
      float v7;
      double result;
      label47: {
         result = mc.field_1724.field_6017;
         if (!iq.isCopyTime()) {
            boolean temp1 = iq.isFunTime();

            if (!temp1) {
               v7 = 0.0F;
               break label47;
            }
         }

         v7 = fq.random(0.01F, 0.08F);
      }

      label40: {
         if (result > (double)v7) {
            double v10;
            int v4 = (v10 = mc.field_1724.field_6017 - 0.08) == 0.0 ? 0 : (v10 < 0.0 ? -1 : 1);

            if (v4 < 0 || !lr.simulateLocalPlayer(1).onGround) {
               v5 = true;
               break label40;
            }
         }

         v5 = false;
      }

      boolean v0 = v5;
      if (!mc.field_1724.method_24828()) {

         if (v0) {
            return true;
         }
      }

      return false;
   }

   public static boolean canChangeIntoPose(EntityPose pose) {
      World result = mc.field_1724.method_73183();
      ClientPlayerEntity temp1 = mc.field_1724;
      Box temp2 = mc.field_1724.method_18377(pose).method_30757(mc.field_1724.method_73189());

      return result.method_8587(temp1, temp2.method_1011(1.0E-7));
   }

   public static boolean isPlayerInBlock(Block block) {
      Box result = mc.field_1724.method_5829();

      return isBoxInBlock(result.method_1014(-0.001), block);
   }

   @Generated
   private Jp() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean isPrePlayerInCriticalState(lr simulatedPlayer) {
      float v9;
      int result = (v9 = simulatedPlayer.fallDistance - 0.0F) == 0.0F ? 0 : (v9 < 0.0F ? -1 : 1);

      label38: {
         label37: {
            if (result > 0) {
               if ((double)simulatedPlayer.fallDistance < 0.08) {
                  break label37;
               }

               if (!lr.simulateLocalPlayer(2).onGround) {
                  
                  break label37;
               }
            }

            v5 = false;
            break label38;
         }

         v5 = true;
      }

      boolean v1 = v5;
      boolean v7;
      if (!simulatedPlayer.onGround && v1) {

         v7 = true;
      } else {
         v7 = false;
      }

      return v7;
   }

   public static void attackEntity(Entity entity) {

      int v1 = 635560114;
      int temp2 = v1 * (v1 + 1);

      mc.field_1761.method_2918(mc.field_1724, entity);
      mc.field_1724.method_6104(Hand.field_5808);
      count++;
   }

   public static boolean isBox(Box box, Predicate<BlockPos> pos) {
      Stream result = BlockPos.method_29715(box);

      return result.anyMatch(pos);
   }

   public static boolean hasMovementRestrictions() {
      if (!mc.field_1724.method_6059(StatusEffects.field_5919)
         && !mc.field_1724.method_6059(StatusEffects.field_5902)
         && !isPlayerInBlock(Blocks.field_10343)
         && !mc.field_1724.method_5869()
         && !mc.field_1724.method_5771()
         && !mc.field_1724.method_6101()) {

         if ((canChangeIntoPose(EntityPose.field_18076) || !mc.field_1724.method_18276()) && !mc.field_1724.method_31549().field_7479) {
            return false;
         }
      }

      return true;
   }

   
}
