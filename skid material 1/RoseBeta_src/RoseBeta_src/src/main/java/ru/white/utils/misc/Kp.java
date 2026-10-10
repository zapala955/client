package ru.white.utils.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
public final class Kp implements IMinecraft {
   public static Vec3d getClosestVec(Vec3d vec, Entity entity) {
      return getClosestVec(vec, entity.method_5829());
   }

   public static Vec3d getClosestVec(Entity entity) {
      Vec3d v1 = mc.field_1724.method_33571();
      int v2 = 1201896466;

      return getClosestVec(v1, entity).method_1020(v1);
   }

   public static boolean validDistance(Entity entity, float distance, boolean smart) {
      return getStrictDistance(entity) < (double)distance;
   }

   public static Rotation cameraAngle() {
      Rotation result = new Rotation;

      result./* $VF: Unable to resugar constructor */<init>(mc.field_1724.method_36454(), mc.field_1724.method_36455());
      return result;
   }

   public static Vector4f calculateRotationFromCamera(LivingEntity target) {
      float v1 = mc.method_61966().method_60637(false);
      Vec3d v2 = mc.field_1724.method_5836(v1);
      Vec3d result = getClosestTargetPoint(target).method_1020(v2);

      Vec3d v3 = result;
      float v4 = MathHelper.method_15393((float)(Math.toDegrees(Math.atan2(v3.field_1350, v3.field_1352)) - 90.0));
      double v11 = v3.field_1352 * v3.field_1352;

      float v5 = (float)(-Math.toDegrees(Math.atan2(v3.field_1351, Math.sqrt(v11 + v3.field_1350 * v3.field_1350))));
      float v6 = MathHelper.method_15393(v4 - mc.field_1724.method_36454());
      float v7 = v5 - mc.field_1724.method_36455();
      return new Vector4f(v4, v5, v6, v7);
   }

   public static Vec2f getVecTAKSA1(Vec3d targetedEntity) {
      double v1 = targetedEntity.method_10216();
      double v3 = targetedEntity.method_10214();
      double v5 = targetedEntity.method_10215();
      double v7 = v1 - mc.field_1724.method_23317();
      double temp1 = mc.field_1724.method_23318();
      double temp2 = (double)mc.field_1724.method_18381(mc.field_1724.method_18376());

      double v9 = v3 - (temp1 + temp2);
      double result = v5 - mc.field_1724.method_23321();

      double v11 = result;
      double v13 = Math.sqrt(v7 * v7 + v11 * v11);
      float v15 = (float)Math.toDegrees(Math.atan2(v11, v7)) - 90.0F;
      float v16 = (float)(-Math.toDegrees(Math.atan2(v9, v13)));
      Vec2f v20 = new Vec2f;

      v20./* $VF: Unable to resugar constructor */<init>(v15, v16);
      return v20;
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      double result = oldValue + (newValue - oldValue) * interpolationValue;
      int v6 = 1436632502;

      return (float)result;
   }

   public static boolean isPlayerInWeb() {
      Box v0 = mc.field_1724.method_5829();
      BlockPos v1 = mc.field_1724.method_24515();
      Stream result = m1517(v1).stream();

      return result.anyMatch(pos -> m1516(v0, pos));
   }

   public static Vec3d getVector(LivingEntity target) {
      double v1 = (double)(target.method_17681() / 2.0F);
      double v3 = MathHelper.method_15350(target.method_23320() - target.method_23318(), 0.0, (double)target.method_17682());

      double v11 = mc.field_1724.method_23317();

      double v5 = MathHelper.method_15350(v11 - target.method_23317(), -v1, v1);
      double v7 = MathHelper.method_15350(mc.field_1724.method_23321() - target.method_23321(), -v1, v1);
      return new Vec3d(
         target.method_23317() - mc.field_1724.method_23317() + v5,
         target.method_23318() - mc.field_1724.method_23320() + v3,
         target.method_23321() - mc.field_1724.method_23321() + v7
      );
   }

   public static double getStrictDistance(Entity entity) {
      return getClosestVec(entity).method_1033();
   }

   public static double calculateFOVFromCamera(LivingEntity target) {
      Vector4f v1 = calculateRotationFromCamera(target);

      float v2 = v1.z;
      float v3 = v1.w;
      return Math.sqrt((double)(v2 * v2 + v3 * v3));
   }

   public static float[] getMovementFromKeys() {
      float v0 = 0.0F;
      float v1 = 0.0F;
      Window v2 = mc.method_22683();
      if (InputUtil.method_15987(v2, mc.field_1690.field_1894.method_1429().method_1444())) {
         v0++;
      }

      Key temp1 = mc.field_1690.field_1881.method_1429();

      if (InputUtil.method_15987(v2, temp1.method_1444())) {
         v0--;
      }

      int v6 = mc.field_1690.field_1913.method_1429().method_1444();

      if (InputUtil.method_15987(v2, v6)) {
         v1++;
      }

      if (InputUtil.method_15987(v2, mc.field_1690.field_1849.method_1429().method_1444())) {
         v1--;
      }

      float[] result = new float[]{v0, 0.0F};

      result[1] = v1;
      return result;
   }

   public static float wrapAngleTo180(float angle) {
      angle %= 360.0F;

      if (angle >= 180.0F) {
         angle -= 360.0F;
         
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public static Vec3d getClosestTargetPoint(Vec3d vec, Entity entity, float point) {
      if (entity == null) {
         return Vec3d.field_1353;
      } else {
         Box v3 = entity.method_5829().method_1014((double)(-point));
         Vec3d v4 = v3.method_1005();
         Vec3d v5 = null;
         double v6 = Double.MAX_VALUE;

         for (double v8 = 0.0; v8 <= (v3.field_1320 - v3.field_1323) / 2.0; v8 += 0.1) {
            for (double v10 = 0.0; v10 <= (v3.field_1325 - v3.field_1322) / 2.0; v10 += 0.1) {
               for (double v12 = 0.0; v12 <= (v3.field_1324 - v3.field_1321) / 2.0; v12 += 0.1) {
                  for (int v17 : new int[]{-1, 1}) {
                     for (int v21 : new int[]{-1, 1}) {
                        int[] v22 = new int[]{-1, 1};

                        for (int v25 : v22) {
                           double v26 = v4.field_1352 + (double)v17 * v8;
                           double v28 = v4.field_1351 + (double)v21 * v10;
                           double v30 = v4.field_1350 + (double)v25 * v12;
                           Vec3d v32 = new Vec3d(v26, v28, v30);
                           Vector2f v33 = calculate(v32);
                           HitResult v34 = np.calculateRayTrace(6.0, v33.x, v33.y, mc.field_1724, false);
                           if (v34 instanceof EntityHitResult) {
                              EntityHitResult v35 = (EntityHitResult)v34;
                              if (v35.method_17782().equals(entity)) {
                                 double v36 = vec.method_1022(v32);
                                 if (v36 < v6) {
                                    v6 = v36;
                                    v5 = v32;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (v5 != null) {
            return v5;
         } else {
            double v39 = fq.clamp(vec.field_1352, v3.field_1323, v3.field_1320);
            double v40 = fq.clamp(vec.field_1351, v3.field_1322, v3.field_1325);
            double v41 = fq.clamp(vec.field_1350, v3.field_1321, v3.field_1324);
            return new Vec3d(v39, v40, v41);
         }
      }
   }

   public static Vec3d getClosestTargetPoint(Entity entity) {
      int v2 = -40497445;

      float v1 = mc.method_61966().method_60637(false);
      return getClosestTargetPoint(mc.field_1724.method_5836(v1), entity, Math.min(entity.method_17681(), entity.method_17682()) / 4.0F);
   }

   public static double[] calculateDirection(float forward, float sideways, double distance) {
      float v4 = mc.field_1724.method_36454();
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

   public static Vec3d getNearestPoint(LivingEntity entity) {
      Vec3d v1 = mc.field_1724.method_33571();
      Vec3d result = new Vec3d;

      double temp2 = MathHelper.method_15350(v1.field_1352, entity.method_5829().field_1323, entity.method_5829().field_1320);
      double temp3 = v1.field_1351;
      Box temp4 = entity.method_5829();

      result./* $VF: Unable to resugar constructor */<init>(
         temp2,
         MathHelper.method_15350(temp3, temp4.field_1322, entity.method_5829().field_1325),
         MathHelper.method_15350(v1.field_1350, entity.method_5829().field_1321, entity.method_5829().field_1324)
      );
      return result;
   }

   public static Vec3d getVector4(LivingEntity target) {
      double v1 = (double)(target.method_17681() / 2.0F);

      double v3 = MathHelper.method_15350(target.method_23318() - 6.0, 0.0, (double)target.method_17682());
      double v5 = MathHelper.method_15350(mc.field_1724.method_23317() - target.method_23317(), -v1, v1);
      double v7 = MathHelper.method_15350(mc.field_1724.method_23321() - target.method_23321(), -v1, v1);
      Vec3d result = new Vec3d;

      result./* $VF: Unable to resugar constructor */<init>(
         target.method_23317() - mc.field_1724.method_23317() + v5,
         target.method_23318() - mc.field_1724.method_23318() - 0.8F,
         target.method_23321() - mc.field_1724.method_23321() + v7
      );
      return result;
   }

   @Generated
   private Kp() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
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

      return Math.toRadians((double)rotationYaw);
   }

   public static boolean collideWith(LivingEntity entity, float grow) {
      Box v2 = mc.field_1724.method_5829();
      Box v3 = entity.method_5829().method_1009((double)grow, 0.0, (double)grow);
      if (v2.field_1320 > v3.field_1323 && v2.field_1325 > v3.field_1322 && v2.field_1324 > v3.field_1321 && v2.field_1323 < v3.field_1320) {

         if (v2.field_1322 < v3.field_1325) {
            double v8;
            int result = (v8 = v2.field_1321 - v3.field_1324) == 0.0 ? 0 : (v8 < 0.0 ? -1 : 1);

            if (result < 0) {
               return true;
            }
         }
      }

      return false;
   }

   public static Vector2f calculate(Vec3d fromVec, Vec3d toVec) {
      double v2 = 180.0 / Math.PI;

      Vec3d v4 = toVec.method_1020(fromVec);
      double v5 = Math.hypot(v4.field_1352, v4.field_1350);
      double v11 = MathHelper.method_15349(v4.field_1350, v4.field_1352);

      float v7 = (float)(v11 * (180.0 / Math.PI)) - 90.0F;
      float v8 = (float)(-(MathHelper.method_15349(v4.field_1351, v5) * (180.0 / Math.PI)));
      return new Vector2f(v7, v8);
   }

   public static Vec3d getVector3(LivingEntity target) {
      double v1 = MathHelper.method_15350(target.method_23318() - target.method_23318(), 0.0, (double)target.method_17682());
      double v3 = MathHelper.method_15350(mc.field_1724.method_23317() - target.method_23317(), 0.0, 0.0);
      double v5 = MathHelper.method_15350(mc.field_1724.method_23321() - target.method_23321(), 0.0, 0.0);
      Vec3d result = new Vec3d;

      double temp2 = target.method_23317() - mc.field_1724.method_23317() + v3;
      double temp3 = target.method_23318() - mc.field_1724.method_23318() - 0.8F;
      double temp4 = target.method_23321() - mc.field_1724.method_23321();
      int v7 = 940135585;

      result./* $VF: Unable to resugar constructor */<init>(temp2, temp3, temp4 + v5);
      return result;
   }

   public static boolean nullCheck() {
      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            return false;
         }

         
      }

      return true;
   }

   public static Rotation getOffset(Rotation deltaToTarget, float progress) {
      float v2;
      float v3;
      float v4;
      float v5;
      float v6;
      float v7;
      boolean result;
      label46: {
         v2 = 3.0F;
         v3 = 1.0F - (float)Math.cos((double)progress * Math.PI);
         v4 = Math.signum(deltaToTarget.getYaw());
         v5 = Math.signum(deltaToTarget.getPitch());
         v6 = 0.0F;
         v7 = 0.0F;
         if (Math.abs(deltaToTarget.getYaw()) < 1.5F) {

            if (Math.abs(deltaToTarget.getYaw()) > 10.0F) {
               result = true;
               break label46;
            }
         }

         result = false;
      }

      boolean v8 = result;
      if (v8) {
         v6 = v5 * v2 * (1.0F - progress);
         v7 = 0.0F;
      } else if (v5 > 0.0F && v4 >= 0.0F) {
         v6 = -v2 * (1.0F - progress);
         v7 = -v2 * v3;
      } else if (v5 > 0.0F && v4 < 0.0F) {
         v6 = v2 * (1.0F - progress);
         v7 = -v2 * v3;
      } else if (v5 < 0.0F && v4 >= 0.0F) {
         v6 = -v2 * (1.0F - progress);
         v7 = v2 * v3;
      } else if (v5 < 0.0F && v4 < 0.0F) {
         v6 = v2 * (1.0F - progress);
         v7 = v2 * v3;
      }

      return new Rotation(v6, v7);
   }

   private static boolean m1516(Box playerBox, BlockPos pos) {
      boolean result = mc.field_1687.method_8320(pos).method_27852(Blocks.field_10343);

      if (!result) {
         return false;
      } else {
         Box v6 = new Box;

         v6./* $VF: Unable to resugar constructor */<init>(pos);
         Box v2 = v6;

         return playerBox.method_994(v2);
      }
   }

   public static BlockHitResult raycast(Vec3d start, Vec3d end, ShapeType shapeType, Entity entity) {
      ClientWorld result = mc.field_1687;
      RaycastContext temp1 = new RaycastContext;

      temp1./* $VF: Unable to resugar constructor */<init>(start, end, shapeType, FluidHandling.field_1348, entity);
      return result.method_17742(temp1);
   }

   private static List<BlockPos> m1517(BlockPos center) {
      ArrayList v1 = new ArrayList();

      for (int v2 = center.method_10263() - 2; v2 <= center.method_10263() + 2; v2++) {
         for (int v3 = center.method_10264() - 1; v3 <= center.method_10264() + 4; v3++) {
            for (int v4 = center.method_10260() - 2; v4 <= center.method_10260() + 2; v4++) {
               v1.add(new BlockPos(v2, v3, v4));
            }
         }
      }

      return v1;
   }

   public static float getAngleDiff(float targetYaw, float currentYaw) {
      float v2 = targetYaw - currentYaw;

      while (v2 <= -180.0F) {
         v2 += 360.0F;
      }

      while (true) {
         int v3 = 1108475263;

         if (!(v2 > 180.0F)) {
            return Math.abs(v2);
         }

         v2 -= 360.0F;
      }
   }

   public static Vec3d getClosestVec(Vec3d vec, Box AABB) {
      Vec3d result = new Vec3d;
      double temp2 = fq.clamp(vec.field_1352, AABB.field_1323, AABB.field_1320);

      result./* $VF: Unable to resugar constructor */<init>(
         temp2, fq.clamp(vec.field_1351, AABB.field_1322, AABB.field_1325), fq.clamp(vec.field_1350, AABB.field_1321, AABB.field_1324)
      );
      return result;
   }

   public static float calculateCorrectYawOffset(float yaw) {
      double v1 = mc.field_1724.method_23317() - mc.field_1724.field_6014;
      double v3 = mc.field_1724.method_23321() - mc.field_1724.field_5969;
      float v5 = (float)(v1 * v1 + v3 * v3);
      float v6 = mc.field_1724.field_6220;
      float v7 = v6;
      if (v5 > 0.0025000002F) {
         v7 = (float)MathHelper.method_15349(v3, v1) * 180.0F / (float) Math.PI - 90.0F;
      }

      if (mc.field_1724 != null && mc.field_1724.field_6251 > 0.0F) {
         v7 = yaw;
      }

      float v8 = MathHelper.method_15393(yaw - (v6 + MathHelper.method_15393(v7 - v6) * 0.3F));
      v8 = MathHelper.method_15363(v8, -15.0F, 15.0F);

      v6 = yaw - v8;
      if (v8 * v8 > 2500.0F) {
         v6 += v8 * 0.2F;
      }

      return v6;
   }

   public static double[] calculateDirection(double distance) {
      float[] v2 = getMovementFromKeys();
      float result = v2[0];
      float temp1 = v2[1];

      return calculateDirection(result, temp1, distance);
   }

   public static Vector2f calculate(Vec3d toVec) {
      Vec3d result = mc.field_1724.method_73189();
      double temp2 = mc.field_1724.method_23320();

      int v1 = -1306773806;

      return calculate(result.method_1031(0.0, temp2, 0.0), toVec);
   }

   public static Vec3d getVector2(LivingEntity target) {
      double v1 = MathHelper.method_15350(target.method_23320() - target.method_23318(), 0.0, (double)target.method_17682());
      double result = mc.field_1724.method_23317() - target.method_23317();

      double v3 = MathHelper.method_15350(result, 0.0, 0.0);
      double v5 = MathHelper.method_15350(mc.field_1724.method_23321() - target.method_23321(), 0.0, 0.0);
      return new Vec3d(
         target.method_23317() - mc.field_1724.method_23317() + v3,
         target.method_23318() - mc.field_1724.method_23320() + v1,
         target.method_23321() - mc.field_1724.method_23321() + v5
      );
   }

   
}
