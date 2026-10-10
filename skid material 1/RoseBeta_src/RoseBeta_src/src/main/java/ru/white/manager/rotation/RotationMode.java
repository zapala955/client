package ru.white.manager.rotation;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Lp;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public class RotationMode extends ud {
   public static int shakeDuration;
   public static int currentPriority;
   private static int f171;
   public static float currentYawReturnSpeed;
   public static float currentPitchSpeed;
   public static rd.td currentTask = rd.td.IDLE;
   public static int idleTicks;
   public static float resetShakeAmplitudePitch;
   public static float resetShakeAmplitudeYaw;
   public static int currentTimeout;
   private static final Random RNG = new Random();
   public static float shakePitch;
   public static float currentYawSpeed;
   public static float shakeYaw;
   public static float currentPitchReturnSpeed;
   public static Rotation targetRotation;

   public static boolean isRotating() {

      return !currentTask.equals(rd.td.IDLE);
   }

   public static void update(rd.sd cfg) {
      if (currentPriority <= cfg.priority) {
         if (currentTask.equals(rd.td.IDLE) && !cfg.clientRotation) {
            wd.active = true;
         }

         currentYawSpeed = cfg.yawSpeed;
         currentPitchSpeed = cfg.pitchSpeed;
         currentYawReturnSpeed = cfg.yawReturnSpeed;
         currentPitchReturnSpeed = cfg.pitchReturnSpeed;

         currentTimeout = cfg.timeout;
         currentPriority = cfg.priority;
         shakeYaw = cfg.shakeYaw;
         shakePitch = cfg.shakePitch;
         resetShakeAmplitudeYaw = cfg.resetShakeAmplitudeYaw;
         resetShakeAmplitudePitch = cfg.resetShakeAmplitudePitch;
         shakeDuration = cfg.shakeDuration;
         currentTask = rd.td.AIM;
         targetRotation = cfg.target;
         updateRotation(cfg.target, cfg.yawSpeed, cfg.pitchSpeed, cfg.shakeYaw, cfg.shakePitch);
      }
   }

   public static void resetParentTimeout() {
      currentTimeout = 0;
      currentTask = rd.td.IDLE;
      currentPriority = 0;

      wd.setActive(false);
   }

   static boolean updateRotation(Rotation targetRot, float yawSpeed, float pitchSpeed, float sYaw, float sPitch) {

      if (mc.field_1724 == null) {
         return false;
      } else {
         Rotation v5 = new Rotation(mc.field_1724);
         float v6 = MathHelper.method_15393(targetRot.yaw - v5.yaw);
         float v7 = targetRot.pitch - v5.pitch;
         float v8 = Math.min(Math.abs(v6), yawSpeed);
         float v9 = Math.min(Math.abs(v7), pitchSpeed);
         float v10 = lp.getSensitivity(MathHelper.method_15363(v6, -v8, v8)) + sYaw;
         float v11 = lp.getSensitivity(MathHelper.method_15363(v7, -v9, v9)) + sPitch;

         mc.field_1724.method_36456(mc.field_1724.field_6241 += v10);
         mc.field_1724.method_36457(MathHelper.method_15363(mc.field_1724.method_36455() + v11, -90.0F, 90.0F));
         idleTicks = 0;
         return new Rotation(mc.field_1724).getDelta(targetRot) < 1.0F;
      }
   }

   public static void update(
      Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      rd.sd result = new rd.sd(target).speed(yawSpeed, pitchSpeed, yawReturnSpeed, pitchReturnSpeed).timeout(timeout);

      update(result.priority(priority).clientRotation(clientRotation));
   }

   public static void update(Rotation target, float turnSpeed, float returnSpeed, int timeout, int priority) {
      rd.sd result = new rd.sd;

      result./* $VF: Unable to resugar constructor */<init>(target);
      update(result.speed(turnSpeed, returnSpeed).timeout(timeout).priority(priority));
   }

   public void stopRotation() {

      currentTask = rd.td.IDLE;
      currentPriority = 0;
      wd.setActive(false);
   }

   private void m199() {
      Rotation v1 = new Rotation(wd.freeYaw, wd.freePitch);
      float v2 = 0.0F;
      float v3 = 0.0F;
      if (shakeDuration > 0 && f171 <= shakeDuration) {
         float temp1 = (float)f171;
         float temp2 = (float)shakeDuration;

         float v4 = 1.0F - temp1 / temp2;
         v2 = (RNG.nextFloat() * 2.0F - 1.0F) * resetShakeAmplitudeYaw * v4;

         v3 = (RNG.nextFloat() * 2.0F - 1.0F) * resetShakeAmplitudePitch * v4;
      }

      boolean v7 = updateRotation(v1, currentYawReturnSpeed, currentPitchReturnSpeed, v2, v3);
      if (v7 && (shakeDuration <= 0 || f171 > shakeDuration)) {
         this.stopRotation();
      }
   }

   @EventTarget
   public void onEvent(EventRenderModel event) {
      if (currentTask.equals(rd.td.AIM) && idleTicks > currentTimeout) {
         currentTask = rd.td.RESET;
         f171 = 0;
      }

      if (currentTask.equals(rd.td.RESET)) {
         f171++;
         this.m199();
      }

      int result = idleTicks + 1;

      idleTicks = result;
   }

   @Environment(EnvType.CLIENT)
   public static class sd {
      public int shakeDuration;
      public float yawSpeed = 180.0F;
      public float pitchReturnSpeed;
      public Rotation target;
      public float shakePitch;
      public int priority;
      public float yawReturnSpeed;
      public float resetShakeAmplitudePitch;
      public int timeout;
      public float shakeYaw;
      public float pitchSpeed = 180.0F;
      public float resetShakeAmplitudeYaw;
      public boolean clientRotation;

      public rd.sd speed(float turnSpeed, float returnSpeed) {
         this.yawSpeed = turnSpeed;
         int temp1 = v4 * (v4 + 1) % 2;

         if (temp1 != 0) {
            }

         this.pitchSpeed = turnSpeed;
         this.yawReturnSpeed = returnSpeed;
         this.pitchReturnSpeed = returnSpeed;
         return this;
      }

      public sd(Rotation target) {
         this.yawReturnSpeed = 180.0F;
         this.pitchReturnSpeed = 180.0F;
         this.shakeYaw = 0.0F;
         this.shakePitch = 0.0F;
         this.resetShakeAmplitudeYaw = 0.0F;
         this.resetShakeAmplitudePitch = 0.0F;
         this.shakeDuration = 0;
         this.timeout = 1;
         this.priority = 0;
         this.clientRotation = false;
         this.target = target;
      }

      public rd.sd priority(int p) {

         this.priority = p;
         return this;
      }

      public rd.sd clientRotation(boolean v) {
         this.clientRotation = v;

         return this;
      }

      public rd.sd speed(float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed) {

         this.yawSpeed = yawSpeed;
         this.pitchSpeed = pitchSpeed;
         this.yawReturnSpeed = yawReturnSpeed;
         this.pitchReturnSpeed = pitchReturnSpeed;
         return this;
      }

      public rd.sd resetShake(float amplitudeYaw, float amplitudePitch, int durationTicks) {
         this.resetShakeAmplitudeYaw = amplitudeYaw;

         this.resetShakeAmplitudePitch = amplitudePitch;
         this.shakeDuration = durationTicks;
         return this;
      }

      public rd.sd timeout(int ticks) {
         this.timeout = ticks;

         return this;
      }

      public rd.sd shake(float yaw, float pitch) {
         this.shakeYaw = yaw;

         this.shakePitch = pitch;
         return this;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static enum td {
      RESET,
      AIM,
      IDLE;
   }
}
