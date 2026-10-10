package ru.white.utils.render;

import java.security.SecureRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class ColorPalette implements IMinecraft {
   private static boolean f1257;
   public static long hitCounterCPSBypass;
   private static final SecureRandom secureRandom = new SecureRandom();
   private static final op cooldownTimer = new op();
   private static final MinecraftClient EventItemUse = MinecraftClient.method_1551();
   private static int f1258;

   public static boolean cpsBypassTrigger() {

      return hitCounterCPSBypass % (2202846400228454750L + -2202846400228454743L) == -4405196371130115034L + 4405196371130115037L;
   }

   public static float msCooldownPC01() {
      long result = cooldownTimer.elapsedTime();

      float v3 = (float)result;

      float v4 = v3 / (float)getMsCooldown();

      return Math.min(v4, 1.0F);
   }

   public static Runnable[] skipSilentSprintingTaskForUse(boolean enabled) {
      Runnable[] v1 = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled && mc.field_1724 != null) {
         if (mc.field_1724.method_5624()) {

            if (!mc.field_1724.method_24828() && !jp.hasMovementRestrictions()) {
               v1[0] = () -> {
                  boolean var10000x = fg.get().typeSprint.is("Silent");

                  if (var10000x) {
                     mc.field_1690.field_1867.method_23481(false);
                     
                  } else {
                     mc.field_1690.field_1867.method_23481(false);
                     mc.field_1724.method_5728(false);
                  }
               };
               Runnable temp2 = () -> {
                  mc.field_1690.field_1867.method_23481(true);

                  mc.field_1724.method_5728(true);
               };

               v1[1] = temp2;
            }
         }

         return v1;
      } else {
         return v1;
      }
   }

   public static boolean msCooldownReached(long msOffset) {
      op result = cooldownTimer;
      long temp1 = getMsCooldown();

      return result.finished((double)(temp1 + msOffset));
   }

   public static boolean randomBoolean(int chance010) {
      float result = (float)secureRandom.nextInt(chance010 + 1);

      return result >= 1.0F * (1.0F / Math.max((float)chance010, 1.0F));
   }

   public static boolean useEntity(LivingEntity livingIn, Runnable preHit, Runnable postHit, Hand hand, boolean cpsBypass) {
      if (preHit != null) {
         preHit.run();
      }

      if (livingIn != null && mc.field_1761 != null && mc.field_1724 != null) {
         mc.field_1761.method_2918(mc.field_1724, livingIn);

         if (hand != null) {
            mc.field_1724.method_6104(hand);
         }

         if (cpsBypass) {
            hitCounterCPSBypassNext();
         } else {
            hitCounterCPSBypassReset();
         }

         cooldownTimer.reset();
      }

      if (postHit != null) {

         postHit.run();
      }

      return livingIn != null;
   }

   private static 

      return 3;
   }

   public static boolean isBestMomentToHit(boolean fallCheck) {
      if (mc.field_1724 == null) {
         return true;
      } else if (mc.field_1724.method_6047().method_7909() == Items.field_49814) {
         return true;
      } else if (!fallCheck) {
         return true;
      } else if (jp.isPlayerInCriticalState()) {
         return true;
      } else {
         boolean v1;
         boolean result;
         label45: {
            v1 = jp.isPlayerInCriticalState();
            if (fg.get().others.getValue("Умные криты")) {

               if (!mc.field_1690.field_1903.method_1434()) {
                  result = true;
                  break label45;
               }
            }

            result = false;
         }

         boolean v2 = result;
         if (!v2 && !v1 && !jp.hasMovementRestrictions()) {
            result = false;
         } else {
            result = true;
            
         }

         boolean v3 = result;

         return v3;
      }
   }

   public static float randomFloat() {

      return randomFloat(0.0F, 1.0F);
   }

   public static boolean shouldAttack(LivingEntity livingTarget, boolean rayCast, boolean fallCheck, long cooldownMSOffset, float[] ranges) {

      int v7 = -1729099462;
      int var10006 = v7 * (v7 + 1);

      return shouldAttack(livingTarget, rayCast, true, fallCheck, cooldownMSOffset, ranges);
   }

   public static PlayerEntity getSelf() {
      int v0 = -64708019;
      int temp1 = v0 * (v0 + 1) % 2;

      if (temp1 != 0) {
         }

      return mc.field_1724;
   }

   public static boolean randomBoolean() {
      boolean v4;
      if (secureRandom.nextInt(2) == 1) {
         int v0 = -2074899793;
         v4 = v0 * (v0 + 1);

         v4 = 1;
      } else {
         v4 = 0;
      }

      return (boolean)v4;
   }

   public static long getMsCooldown() {

      if (mc.field_1724 == null) {
         return 7105302587662469969L + -7105302587662469469L;
      } else {
         double v0 = mc.field_1724.method_45325(EntityAttributes.field_23723);
         float v4 = 0.2F;
         long v2 = (long)(1.0 / v0 * 1000.0 * (double)(1.0F - Math.min(v4, 1.0F)));
         if (v0 == 4.0 || v0 == 4.4000000059604645 || v0 == 4.800000011920929) {
            v2 = -2910280081392297541L + 2910280081392297991L;
         }

         return Math.max(v2, -4208848888562543603L + 4208848888562544053L);
      }
   }

   public static float randomFloat(float min, float max) {
      int v2 = -171284986;

      return secureRandom.nextFloat(min, max);
   }

   @Generated
   private ColorPalette() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean resetSprintTick(LivingEntity targetIn, float[] ranges) {

      if (targetIn != null && shouldAttack(targetIn, false, false, -452901171654610489L + 452901171654610439L, ranges) && !mc.field_1724.method_24828()) {

         if (!mc.field_1724.method_5777(FluidTags.field_15517) && mc.field_1724.method_18798().field_1351 <= 0.0030162615090425808) {
            return true;
         }
      }

      return false;
   }

   public static boolean msCooldownHasMs(long ms) {
      return cooldownTimer.finished((double)ms);
   }

   public static boolean shouldAttack(
      LivingEntity livingTarget, boolean rayCast, boolean distanceCheck, boolean fallCheck, long cooldownMSOffset, float[] ranges
   ) {
      if (distanceCheck && livingTarget != null && !kp.validDistance(livingTarget, ranges[0], true)) {
         return false;
      } else if (!msCooldownReached(cooldownMSOffset)) {
         return false;
      } else {
         boolean v7 = isBestMomentToHit(fallCheck);
         if (v7 && rayCast) {

            if (!anyEntityOnRay(livingTarget, (double)ranges[0])) {
               v7 = false;
            }
         }

         return v7;
      }
   }

   public static int getAxeSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         int v0 = 0;

         while (v0 < 9) {
            boolean v3 = mc.field_1724.method_31548().method_5438(v0).method_7909() instanceof AxeItem;

            if (v3) {
               return v0;
            }

            v0++;
         }

         return -1;
      }
   }

   public static void hitCounterCPSBypassNext() {
      hitCounterCPSBypass++;
   }

   public static boolean resetSprintTickP(LivingEntity targetIn, float[] ranges) {
      if (targetIn != null && shouldAttack(targetIn, false, false, -5428621738054999292L + 5428621738054999272L, ranges)) {

         if (!mc.field_1724.method_24828()
            && !mc.field_1724.method_5777(FluidTags.field_15517)
            && mc.field_1724.method_18798().field_1351 <= 0.0030162615090425808) {
            return true;
         }
      }

      return false;
   }

   public static boolean anyEntityOnRay(LivingEntity livingIn, double range) {
      if (livingIn != null) {
         boolean result = np.rayTraceEntity(
            MathHelper.method_15393(mc.field_1724.method_36454()), mc.field_1724.method_36455(), (double)((float)range), livingIn
         );

         if (result) {
            return true;
         }
      }

      return false;
   }

   public static boolean msCooldownReached() {
      int v0 = 319992953;

      return msCooldownReached(0L);
   }

   public static void antiMissesHittingUpdate(LivingEntity targetIn, boolean cpsBypass, boolean rayCastCheck, boolean enabled) {
      if (targetIn == null || f1258 == 0 || !enabled || targetIn.field_6235 != 0) {
         antiMissesHittingReset();
      }

      if (enabled
         && targetIn != null
         && msCooldownHasMs(cpsBypassTrigger() ? 8757026572712521721L + -8757026572712521471L : 8719317313543917782L + -8719317313543917632L)
         && mc.field_1724.field_6252) {
         if (!f1257 && f1258 == 0 && targetIn.field_6235 == 0) {
            f1257 = true;
            f1258 = m1552();
         }

         if (f1257 && f1258 > 0 && targetIn != null) {
            if (rayCastCheck) {

               if (!anyEntityOnRay(targetIn, 6.0)) {
                  return;
               }
            }

            boolean v6 = useEntity(targetIn, () -> {
            }, () -> {
            }, Hand.field_5808, cpsBypass);

            if (v6) {
               f1258--;
            }
         }
      }
   }

   public static Runnable[] hitShieldBreakTaskForUse(LivingEntity livingIn, boolean enabled) {
      Runnable[] result = new Runnable[]{() -> {
      }, null};

      result[1] = () -> {
      };
      Runnable[] v2 = result;
      if (enabled && mc.field_1724 != null) {
         if (livingIn instanceof PlayerEntity v3) {
            if (!v3.method_6039()) {
               return v2;
            }

            ItemStack v4 = v3.method_6047();
            ItemStack v5 = v3.method_6079();

            Item v6 = v4.method_7960() ? null : v4.method_7909();
            Item v7 = v5.method_7960() ? null : v5.method_7909();

            if (v6 == Items.field_8255 || v7 == Items.field_8255) {
               int v8 = getAxeSlot();
               int v9 = mc.field_1724.method_31548().method_67532();
               if (v8 != -1 && v8 != v9) {
                  v2[0] = () -> {
                     if (mc.method_1562() != null) {

                        ClientPlayNetworkHandler var10000x = mc.method_1562();
                        UpdateSelectedSlotC2SPacket v3x = new UpdateSelectedSlotC2SPacket;

                        v3x./* $VF: Unable to resugar constructor */<init>(v8);
                        var10000x.method_52787(v3x);
                     }
                  };
                  v2[1] = () -> {
                     if (mc.method_1562() != null) {

                        mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(v9));
                     }
                  };
               }
            }
         }

         return v2;
      } else {
         return v2;
      }
   }

   public static Runnable[] resetShieldSilentTaskForUse(boolean enabled) {
      Runnable[] v1 = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled && mc.field_1724 != null) {
         if (mc.field_1724.method_6039()) {
            Hand v2 = mc.field_1724.method_6058();
            if (v2 == null) {
               return v1;
            }

            v1[0] = () -> {
               ClientPlayNetworkHandler result = mc.method_1562();
               PlayerActionC2SPacket temp1 = new PlayerActionC2SPacket(Action.field_12974, BlockPos.field_10980, Direction.field_11033);

               result.method_52787(temp1);
            };
            v1[1] = () -> {
               ClientPlayNetworkHandler result = mc.method_1562();
               PlayerInteractItemC2SPacket temp1 = new PlayerInteractItemC2SPacket;

               temp1./* $VF: Unable to resugar constructor */<init>(v2, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455());
               result.method_52787(temp1);
            };
         }

         return v1;
      } else {
         return v1;
      }
   }

   public static void hitCounterCPSBypassReset() {

      int v0 = -1631650347;

      hitCounterCPSBypass = 0L;
   }

   public static int randomInt1PosibleOrNot() {
      byte v1;
      if (randomBoolean()) {

         v1 = 1;
      } else {
         v1 = -1;
      }

      return v1;
   }

   public static World getWorld() {
      int v0 = -790222214;

      return mc.field_1687;
   }

   public static float msCooldownReach() {
      long result = cooldownTimer.elapsedTime();
      int v0 = -1429105144;
      int temp1 = v0 * (v0 + 1) % 2;

      if (temp1 != 0) {
         }

      return (float)result;
   }

   @Generated
   public static op getCooldownTimer() {
      return cooldownTimer;
   }

   public static float applyGaussianJitter(float rotation) {
      float v1 = 0.2F;

      double v5 = (double)rotation;
      double temp1 = secureRandom.nextGaussian() * 0.2F * 2.0;

      v5 += temp1 - 0.2F;

      return (float)v5;
   }

   public static void antiMissesHittingReset() {
      f1257 = false;
      f1258 = 0;
      int v0 = 646618303;
      int result = v0 * (v0 + 1);

      result %= 2;

      if (result != 0) {
         }
   }

   
}
