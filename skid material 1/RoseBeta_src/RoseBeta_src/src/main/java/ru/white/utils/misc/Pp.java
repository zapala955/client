package ru.white.utils.misc;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import ru.white.utils.misc.Uq;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.TriggerBot;
import ru.white.utils.IMinecraft;
import ru.white.utils.ItemUtil;

@Environment(EnvType.CLIENT)
public final class Pp implements IMinecraft {
   private static final op cooldownTimer = new op();
   private static final MinecraftClient EventItemUse = MinecraftClient.method_1551();
   public static long hitCounterCPSBypass;
   private static boolean f1255;
   private static int f1256;

   public static Runnable[] resetShieldSilentTaskForUse(boolean enabled) {
      Runnable[] result = new Runnable[]{() -> {
      }, () -> {
      }};
      int v3 = 1106438569;

      int temp1 = v3 * (v3 + 1);

      Runnable[] v1 = result;
      if (enabled && mc.field_1724 != null) {
         if (mc.field_1724.method_6039()) {
            Hand v2 = mc.field_1724.method_6058();
            if (v2 == null) {
               return v1;
            }

            v1[0] = () -> {
               ClientPlayNetworkHandler var10000x = mc.method_1562();
               PlayerActionC2SPacket var10001x = new PlayerActionC2SPacket;

               var10001x./* $VF: Unable to resugar constructor */<init>(Action.field_12974, BlockPos.field_10980, Direction.field_11033);
               var10000x.method_52787(var10001x);
            };
            v1[1] = () -> {
               ClientPlayNetworkHandler var10000x = mc.method_1562();
               PlayerInteractItemC2SPacket var10001x = new PlayerInteractItemC2SPacket;

               var10001x./* $VF: Unable to resugar constructor */<init>(v2, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455());
               var10000x.method_52787(var10001x);
            };
         }

         return v1;
      } else {
         return v1;
      }
   }

   public static boolean cpsBypassTrigger() {
      int v0 = -281075299;
      int temp3 = v0 + 1;

      long result = hitCounterCPSBypass % (612251989219540967L + -612251989219540960L);

      return result == -8985709448960820054L + 8985709448960820057L;
   }

   public static float msCooldownReach() {
      float result = (float)cooldownTimer.elapsedTime();

      return result;
   }

   public static void hitCounterCPSBypassReset() {
      hitCounterCPSBypass = 0L;
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

         if (v7 && rayCast && !anyEntityOnRay(livingTarget, (double)ranges[0])) {
            v7 = false;
            
         }

         return v7;
      }
   }

   public static boolean resetSprintTick(LivingEntity targetIn, float[] ranges) {
      if (targetIn != null && shouldAttack(targetIn, false, false, 8210100324277098099L + -8210100324277098149L, ranges)) {

         if (!mc.field_1724.method_24828()
            && !mc.field_1724.method_5777(FluidTags.field_15517)
            && mc.field_1724.method_18798().field_1351 <= 0.0030162615090425808) {
            return true;
         }
      }

      return false;
   }

   @Generated
   public static op getCooldownTimer() {
      return cooldownTimer;
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
         boolean v1 = jp.isPlayerInCriticalState();

         boolean v2 = m1536().smartCrit.getValue() && !mc.field_1690.field_1903.method_1434();
         int v4 = -712237152;
         int result = v4 * (v4 + 1) % 2;

         if (result != 0) {
            }

         return v2 || v1 || jp.hasMovementRestrictions();
      }
   }

   @Generated
   private Pp() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean msCooldownReached(long msOffset) {
      op result = cooldownTimer;
      long temp1 = getMsCooldown();

      return result.finished((double)(temp1 + msOffset));
   }

   public static Runnable[] hitShieldBreakTaskForUse(LivingEntity livingIn, boolean enabled) {
      Runnable[] v2 = new Runnable[]{() -> {
      }, () -> {
      }};
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
                        mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(v8));
                        int v1 = -1481691652;
                        int v2x = -583046297;
                        int temp3 = v2x * (v2x + 1);

                        
                     }
                  };
                  v2[1] = () -> {
                     if (mc.method_1562() != null) {

                        ClientPlayNetworkHandler result = mc.method_1562();
                        UpdateSelectedSlotC2SPacket v3x = new UpdateSelectedSlotC2SPacket;

                        v3x./* $VF: Unable to resugar constructor */<init>(v9);
                        result.method_52787(v3x);
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

   public static void antiMissesHittingUpdate(LivingEntity targetIn, boolean cpsBypass, boolean rayCastCheck, boolean enabled) {
      label56: {
         if (targetIn != null && f1256 != 0) {

            if (enabled && targetIn.field_6235 == 0) {
               break label56;
            }
         }

         antiMissesHittingReset();
      }

      if (enabled
         && targetIn != null
         && msCooldownHasMs(cpsBypassTrigger() ? 7504621043885484701L + -7504621043885484451L : -6587251417925598794L + 6587251417925598944L)
         && mc.field_1724.field_6252) {
         if (!f1255 && f1256 == 0 && targetIn.field_6235 == 0) {
            f1255 = true;
            f1256 = m1537();
         }

         if (f1255 && f1256 > 0 && targetIn != null && (!rayCastCheck || anyEntityOnRay(targetIn, 6.0)) && useEntity(targetIn, () -> {
         }, () -> {
         }, Hand.field_5808, cpsBypass)) {
            f1256--;
         }
      }
   }

   public static void hitCounterCPSBypassNext() {

      hitCounterCPSBypass++;
   }

   public static boolean resetSprintTickP(LivingEntity targetIn, float[] ranges) {
      if (targetIn != null) {

         if (shouldAttack(targetIn, false, false, 3422436487219151845L + -3422436487219151865L, ranges)
            && !mc.field_1724.method_24828()
            && !mc.field_1724.method_5777(FluidTags.field_15517)) {
            double v4;
            int result = (v4 = mc.field_1724.method_18798().field_1351 - 0.0030162615090425808) == 0.0 ? 0 : (v4 < 0.0 ? -1 : 1);

            if (result <= 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static int m1537() {
      return 3;
   }

   private static TriggerBot m1536() {
      return uq.get(TriggerBot.class);
   }

   public static boolean shouldAttack(LivingEntity livingTarget, boolean rayCast, boolean fallCheck, long cooldownMSOffset, float[] ranges) {
      int v6 = -997707009;

      return shouldAttack(livingTarget, rayCast, true, fallCheck, cooldownMSOffset, ranges);
   }

   public static int getAxeSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int v0 = 0; v0 < 9; v0++) {
            PlayerInventory result = mc.field_1724.method_31548();

            boolean v3 = result.method_5438(v0).method_7909() instanceof AxeItem;

            if (v3) {
               return v0;
            }
         }

         return -1;
      }
   }

   public static long getMsCooldown() {
      if (mc.field_1724 == null) {
         return 4014649248038640155L + -4014649248038639655L;
      } else {
         double v0 = mc.field_1724.method_45325(EntityAttributes.field_23723);
         float v4 = 0.2F;
         long v2 = (long)(1.0 / v0 * 1000.0 * (double)(1.0F - Math.min(v4, 1.0F)));
         if (v0 == 4.0 || v0 == 4.4000000059604645 || v0 == 4.800000011920929) {
            v2 = -2194907616914654321L + 2194907616914654771L;
         }

         long result = Math.max(v2, -5470218727275742609L + 5470218727275743059L);
         int v5 = 685589300;

         v2 = result;
         if (iq.isCopyTime()) {
            if (ThreadLocalRandom.current().nextBoolean()) {
               result = -5769503084694979564L + 5769503084694980114L;
            } else {

               result = -2827645560250182069L + 2827645560250182569L;
            }

            v2 = result;
         }

         return v2;
      }
   }

   public static boolean anyEntityOnRay(LivingEntity livingIn, double range) {
      boolean result;
      if (livingIn != null
         && np.rayTraceEntity(MathHelper.method_15393(mc.field_1724.method_36454()), mc.field_1724.method_36455(), (double)((float)range), livingIn)) {
         result = true;
         
      } else {
         result = false;
      }

      return result;
   }

   public static boolean msCooldownReached() {
      boolean result = msCooldownReached(0L);

      return result;
   }

   public static Runnable[] skipSilentSprintingTaskForUse(boolean enabled) {
      Runnable[] v1 = new Runnable[]{() -> {
      }, () -> {
      }};
      if (enabled) {

         if (mc.field_1724 != null) {

            if (mc.field_1724.method_5624() && !mc.field_1724.method_24828() && !jp.hasMovementRestrictions()) {
               v1[0] = () -> {
                  TriggerBot var10000x = m1536();

                  if (var10000x.typeSprint.is("Silent")) {
                     int v0 = -46122586;
                     int v3x = v0 * (v0 + 1);

                     mc.field_1690.field_1867.method_23481(false);
                  } else {
                     mc.field_1690.field_1867.method_23481(false);
                     mc.field_1724.method_5728(false);
                  }
               };

               v1[1] = () -> {
                  mc.field_1690.field_1867.method_23481(true);
                  int v0 = -1406357156;
                  int var10001x = v0 * (v0 + 1);
                  int v1x = -972993084;
                  int temp3 = v1x * (v1x + 1);

                  mc.field_1724.method_5728(true);
               };
            }

            return v1;
         }
      }

      return v1;
   }

   public static float msCooldownPC01() {
      long result = cooldownTimer.elapsedTime();

      return Math.min((float)result / (float)getMsCooldown(), 1.0F);
   }

   public static boolean msCooldownHasMs(long ms) {
      return cooldownTimer.finished((double)ms);
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

   public static void antiMissesHittingReset() {

      f1255 = false;
      f1256 = 0;
   }

   
}
