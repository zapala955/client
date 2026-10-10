package ru.white.utils.media;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventPlayerLook;

@Environment(EnvType.CLIENT)
public final class Kr implements IMinecraft {
   public static void setSpeed(double speed) {
      if (mc.field_1724 != null) {
         float v2 = mc.field_1724.method_36454();
         double v3 = Math.toRadians((double)v2);
         double v5 = 0.0;
         double v7 = 0.0;
         Vec2f v9 = mc.field_1724.field_3913.method_3128();

         float v10 = v9.field_1342;
         float v11 = v9.field_1343;
         if (v10 > 0.0F) {
            v5 -= Math.sin(v3) * speed;
            v7 += Math.cos(v3) * speed;
         }

         if (v10 < 0.0F) {
            v5 += Math.sin(v3) * speed;
            v7 -= Math.cos(v3) * speed;
         }

         if (v11 > 0.0F) {
            v5 += Math.cos(v3) * speed;
            v7 += Math.sin(v3) * speed;
         }

         if (v11 < 0.0F) {
            v5 -= Math.cos(v3) * speed;
            v7 -= Math.sin(v3) * speed;
         }

         if (v11 > 0.0F && v10 > 0.0F) {
            v5 = Math.cos(Math.toRadians((double)(v2 + 45.0F))) * speed;
            double result = Math.toRadians((double)(v2 + 45.0F));

            v7 = Math.sin(result) * speed;
         }

         if (v11 < 0.0F && v10 > 0.0F) {
            v5 = -Math.cos(Math.toRadians((double)(v2 - 45.0F))) * speed;
            v7 = -Math.sin(Math.toRadians((double)(v2 - 45.0F))) * speed;
         }

         if (v11 > 0.0F && v10 < 0.0F) {
            v5 = -Math.cos(Math.toRadians((double)(v2 + 135.0F))) * speed;
            v7 = -Math.sin(Math.toRadians((double)(v2 + 135.0F))) * speed;
         }

         if (v11 < 0.0F && v10 < 0.0F) {
            float v15 = v2 - 135.0F;

            v5 = Math.cos(Math.toRadians((double)v15)) * speed;
            v7 = Math.sin(Math.toRadians((double)(v2 - 135.0F))) * speed;
         }

         mc.field_1724.method_18800(v5, mc.field_1724.method_18798().field_1351, v7);
      }
   }

   public static void fixMovementTest(EventPlayerLook event, float yaw, float targetYaw) {
      float v3 = event.getForward();
      float v4 = event.getStrafe();
      double v5 = MathHelper.method_15338(Math.toDegrees(direction(mc.field_1724.method_6128() ? yaw : targetYaw, v3, v4)));

      if (v3 != 0.0F || v4 != 0.0F) {
         float v7 = 0.0F;
         float v8 = 0.0F;
         float v9 = Float.MAX_VALUE;

         for (float v10 = -1.0F; v10 <= 1.0F; v10++) {
            for (float v11 = -1.0F; v11 <= 1.0F; v11++) {
               if (v11 != 0.0F || v10 != 0.0F) {
                  double v12 = MathHelper.method_15338(Math.toDegrees(direction(yaw, v10, v11)));
                  double v14 = Math.abs(v5 - v12);
                  if (v14 < (double)v9) {
                     v9 = (float)v14;

                     v7 = v10;
                     v8 = v11;
                  }
               }
            }
         }

         event.setForward(v7);
         event.setStrafe(v8);
      }
   }

   @Generated
   private Kr() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs, float deadAngle) {
      boolean v4 = input.comp_3159();
      boolean v5 = input.comp_3160();
      boolean v6 = input.comp_3161();
      boolean v7 = input.comp_3162();

      if (dgs >= (double)(-90.0F + deadAngle) && dgs <= (double)(90.0F - deadAngle)) {
         v4 = true;
      } else if (dgs < (double)(-90.0F - deadAngle) || dgs > (double)(90.0F + deadAngle)) {
         v5 = true;
      }

      if (dgs >= (double)(0.0F + deadAngle) && dgs <= (double)(180.0F - deadAngle)) {
         v7 = true;
      } else if (dgs >= (double)(-180.0F + deadAngle) && dgs <= (double)(0.0F - deadAngle)) {

         v6 = true;
      }

      return new PlayerInput(v4, v5, v6, v7, input.comp_3163(), input.comp_3164(), input.comp_3165());
   }

   public static float[] getMovementFromKeys() {
      float v0 = 0.0F;
      float v1 = 0.0F;
      if (InputUtil.method_15987(mc.method_22683(), mc.field_1690.field_1894.method_1429().method_1444())) {
         v0++;
      }

      if (InputUtil.method_15987(mc.method_22683(), mc.field_1690.field_1881.method_1429().method_1444())) {
         v0--;
      }

      Window result = mc.method_22683();
      int temp1 = mc.field_1690.field_1913.method_1429().method_1444();

      if (InputUtil.method_15987(result, temp1)) {
         v1++;
      }

      if (InputUtil.method_15987(mc.method_22683(), mc.field_1690.field_1849.method_1429().method_1444())) {
         v1--;
      }

      return new float[]{v0, v1};
   }

   public static double[] calculateDirection(float forward, float sideways, double distance) {
      float result = mc.field_1724.method_36454();

      float v4 = result;
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            v4 += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            v4 += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double v5 = Math.sin(Math.toRadians((double)(v4 + 90.0F)));
      double v7 = Math.cos(Math.toRadians((double)(v4 + 90.0F)));
      double v9 = (double)forward * distance * v7 + (double)sideways * distance * v5;
      double v11 = (double)forward * distance * v5 - (double)sideways * distance * v7;
      return new double[]{v9, v11};
   }

   public static double[] calculateDirection(double distance) {
      float[] v2 = getMovementFromKeys();

      return calculateDirection(v2[0], v2[1], distance);
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs) {
      return getDirectionalInputForDegrees(input, dgs, 20.0F);
   }

   public static boolean isMoving() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         Vec2f result = mc.field_1724.field_3913.method_3128();

         if (result.field_1342 == 0.0F) {

            if (mc.field_1724.field_3913.method_3128().field_1343 == 0.0F) {
               return false;
            }
         }

         return true;
      }
   }

   public static void fixMovement(EventPlayerLook event, float yaw, float targetYaw) {
      float v3 = event.getForward();
      float v4 = event.getStrafe();
      float result = mc.field_1724.method_6128() ? yaw : targetYaw;

      double v5 = MathHelper.method_15338(Math.toDegrees(direction(result, v3, v4)));
      if (v3 == 0.0F) {

         if (v4 == 0.0F) {
            return;
         }
      }

      float v7 = 0.0F;
      float v8 = 0.0F;
      float v9 = Float.MAX_VALUE;

      for (float v10 = -1.0F; v10 <= 1.0F; v10++) {
         float v11 = -1.0F;

         while (true) {

            if (!(v11 <= 1.0F)) {
               break;
            }

            if (v11 != 0.0F || v10 != 0.0F) {
               double v12 = MathHelper.method_15338(Math.toDegrees(direction(yaw, v10, v11)));
               double v14 = Math.abs(v5 - v12);
               if (v14 < (double)v9) {
                  v9 = (float)v14;
                  v7 = v10;
                  v8 = v11;
               }
            }

            v11++;
         }
      }

      event.setForward(v7);
      event.setStrafe(v8);
   }

   public static double getDegreesRelativeToView(Vec3d positionRelativeToPlayer, float yaw) {
      float v2 = (float)Math.atan2(-positionRelativeToPlayer.field_1352, positionRelativeToPlayer.field_1350);
      double result = Math.toRadians((double)MathHelper.method_15393(yaw));

      double v3 = result;
      result = (double)v2;

      return Math.toDegrees(MathHelper.method_15338(result - v3));
   }

   public static float getDirection() {
      float v0 = mc.field_1724.method_36454();
      float v1 = 0.0F;
      if (mc.field_1724.field_3913.field_54155.comp_3159()) {
         v1 = 1.0F;
      }

      if (mc.field_1724.field_3913.field_54155.comp_3160()) {
         v1 = -1.0F;
      }

      if (v1 == 0.0F) {
         if (mc.field_1724.field_3913.field_54155.comp_3161()) {
            v0 -= 90.0F;
         }

         if (mc.field_1724.field_3913.field_54155.comp_3162()) {

            v0 += 90.0F;
         }
      } else {
         if (mc.field_1724.field_3913.field_54155.comp_3161()) {
            v0 -= 45.0F * v1;
         }

         if (mc.field_1724.field_3913.field_54155.comp_3162()) {
            v0 += 45.0F * v1;
         }
      }

      if (v1 < 0.0F) {
         v0 -= 180.0F;
      }

      return (float)Math.toRadians((double)v0);
   }

   public static void targetMovement() {
      float[] v0 = getMovementFromKeys();
      float v1 = v0[0];
      float v2 = v0[1];
      if (v1 != 0.0F || v2 != 0.0F) {
         Box v3 = fg.target.method_5829();
         double v4 = MathHelper.method_16436(Math.random(), v3.field_1323, v3.field_1320);
         double v6 = MathHelper.method_16436(Math.random(), v3.field_1322, v3.field_1325);

         double v8 = MathHelper.method_16436(Math.random(), v3.field_1321, v3.field_1324);
         v6 = MathHelper.method_15350(v6, fg.target.method_23318() + 0.2, fg.target.method_23318() + (double)fg.target.method_17682() - 0.2);
         Vec3d v10 = new Vec3d(v4, v6, v8);
         Vec3d v11 = v10.method_1020(mc.field_1724.method_33571()).method_1029();
         float v12 = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(v11.field_1350, v11.field_1352)) - 90.0);
         double v13 = MathHelper.method_15338(Math.toDegrees(direction(mc.field_1724.method_70987() ? mc.field_1724.method_36454() : v12, v1, v2)));
         float v15 = 0.0F;
         float v16 = 0.0F;
         float v17 = Float.MAX_VALUE;

         for (float v18 = -1.0F; v18 <= 1.0F; v18++) {
            for (float v19 = -1.0F; v19 <= 1.0F; v19++) {
               if (v19 != 0.0F || v18 != 0.0F) {
                  double v20 = MathHelper.method_15338(Math.toDegrees(direction(mc.field_1724.method_36454(), v18, v19)));
                  double v22 = Math.abs(v13 - v20);
                  if (v22 < (double)v17) {
                     v17 = (float)v22;
                     v15 = v18;
                     v16 = v19;
                  }
               }
            }
         }

         mc.field_1690.field_1894.method_23481(v15 > 0.0F);
         mc.field_1690.field_1881.method_23481(v15 < 0.0F);
         mc.field_1690.field_1913.method_23481(v16 > 0.0F);
         mc.field_1690.field_1849.method_23481(v16 < 0.0F);
      }
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float v3 = 1.0F;

      if (moveForward < 0.0F) {
         v3 = -0.5F;
      }

      if (moveForward > 0.0F) {
         v3 = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * v3;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * v3;
      }

      double result = Math.toRadians((double)rotationYaw);

      return result;
   }

   
}
