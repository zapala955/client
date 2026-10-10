package ru.white.utils.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import ru.white.auth.StrDec;
import ru.white.utils.animation.ap;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class Rp implements IMinecraft {
   private static double m1571(Vec3d first, Vec3d second) {
      double v2;
      double v4;
      double result = (v2 = first.field_1352 - second.field_1352) * v2 + (v4 = first.field_1351 - second.field_1351) * v4;
      int v8 = 466377993;

      double v6;
      return Math.sqrt(result + (v6 = first.field_1350 - second.field_1350) * v6);
   }

   @Generated
   private Rp() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static HitResult traceBlock(Vec3d startVec, Vec3d endVec, ShapeType blockMode, FluidHandling fluidMode) {
      ClientWorld result = mc.field_1687;
      RaycastContext temp1 = new RaycastContext;

      temp1./* $VF: Unable to resugar constructor */<init>(startVec, endVec, blockMode, fluidMode, mc.field_1724);

      return result.method_17742(temp1);
   }

   public static List<Vec3d> entityBoxVec3dsAlternate(Box aabb) {
      ArrayList v1 = new ArrayList();
      double v2 = 0.01F;
      int v4 = 17;
      byte v5 = 5;
      int v6 = 24;
      int v7 = 6;
      aabb = aabb.method_989(-v2, -v2, -v2);
      double[] v8 = new double[]{aabb.field_1320 - aabb.field_1323, aabb.field_1325 - aabb.field_1322, (aabb.field_1325 - aabb.field_1322) / 1.05};
      double[] v9 = new double[]{aabb.field_1323 + v8[0] / 2.0, aabb.field_1322, aabb.field_1321 + v8[0] / 2.0};
      double[] v10 = new double[]{aabb.field_1323, aabb.field_1322, aabb.field_1321};
      double[] result = new double[]{aabb.field_1320, aabb.field_1325, 0.0};

      result[2] = aabb.field_1324;
      double[] v11 = result;
      double v44 = Math.sqrt(v8[0] * v8[0] + v8[0] * v8[0] + v8[0] * v8[0]);

      float v12 = (float)v44 / 2.0F;
      ClientPlayerEntity v13 = mc.field_1724;
      if (v13 == null) {
         return null;
      } else {
         float v14 = (float)(
            (1.0 - Math.min(v13.method_73189().method_1022(new Vec3d(v9[0], v9[1], v9[2])) / 5.0, 1.0))
               * Math.min(v13.method_73189().method_1022(new Vec3d(v9[0], v13.method_23318(), v9[2])) / 0.6F, 1.0)
         );

         int v15 = lerp(v5, v4, v14);
         int v16 = lerp(v7, v6, v14);
         float v17 = 0.0F;
         int[] v18 = IntStream.range(0, v15).toArray();
         int v19 = v18.length;

         for (int v20 = 0; v20 < v19; v20++) {
            Integer v21 = v18[v20];
            boolean v22 = v21 == 0 || v21 == v15 - 1;
            double v23 = lerp(v10[0], v11[0], (double)((float)v21.intValue() / (float)(v15 - 1)));
            int[] v25 = IntStream.range(0, v15).toArray();
            int v26 = v25.length;

            for (int v27 = 0; v27 < v26; v27++) {
               Integer v28 = v25[v27];
               boolean v29 = v28 == 0 || v28 == v15 - 1;
               double v30 = lerp(v10[2], v11[2], (double)((float)v28.intValue() / (float)(v15 - 1)));
               int[] v32 = IntStream.range(0, v16).toArray();
               int v33 = v32.length;

               for (int v34 = 0; v34 < v33; v34++) {
                  Integer v35 = v32[v34];
                  boolean v36 = v35 == 0 || v35 == v16 - 1;
                  double v37 = lerp(v10[1], v11[1], (double)((float)v35.intValue() / (float)(v16 - 1)));
                  Vec3d v39 = new Vec3d(v23, v37, v30);
                  if ((v22 || v29 || v36)
                     && !(v13.method_73189().method_1022(v39.method_1031(0.0, (double)(-v13.method_18381(EntityPose.field_18076)), 0.0)) < (double)v12)
                     && m1570(v13, v39, v17)
                     && !v1.add(v39)) {
                     break;
                  }
               }
            }
         }

         return v1;
      }
   }

   public static double lerp(double a, double b, double f) {

      double temp1 = f * (b - a);

      return a + temp1;
   }

   public static Vec3d getBestVector3dOnEntityBox(Box aabb, boolean alwaysMultipoints) {
      if (aabb == null) {
         return mc.field_1724.method_33571();
      } else {
         double[] v2 = new double[]{aabb.field_1320 - aabb.field_1323, aabb.field_1325 - aabb.field_1322, (aabb.field_1325 - aabb.field_1322) / 1.1F};
         double[] v3 = new double[]{aabb.field_1323 + v2[0] / 2.0, aabb.field_1322, aabb.field_1321 + v2[0] / 2.0};
         double[] v4 = new double[]{mc.field_1724.method_23318() - v3[1], m1567(mc.field_1724, v3[0], v3[2])};
         double result = clamp(ap.BACK_OUT.ease((v4[1] - v2[0] / 2.0) / (5.0 + v2[0] / 2.0)), 0.1, 0.95);

         double v5 = result;
         double v7 = clamp(v5 * v5, 0.0, 1.0);
         double v9 = clamp(v2[2] / 2.0 * v7 + v2[2] / 2.0 * clamp(v4[0] + v7, 0.0, 1.0), 0.0, v2[2]);
         Vec3d v11 = new Vec3d(v3[0], v3[1] + v9, v3[2]);
         if (!alwaysMultipoints && !m1569(mc.field_1724, v11)) {
            v11 = v11.method_1031(0.0, -v9 / 2.0, 0.0);
         }

         if (!(v2[1] <= 1.0) && (alwaysMultipoints || !m1569(mc.field_1724, v11))) {
            List v12 = entityBoxVec3dsAlternate(aabb);
            float v13 = 1.0F - (float)Math.max(Math.min((v4[1] - 2.0) / 3.0, 1.0), 0.0);
            Vec3d v14 = new Vec3d(
               mc.field_1724.method_23317(), mc.field_1724.method_23318() + 0.6F + lerp(v9, v9 / 2.5, (double)v13), mc.field_1724.method_23321()
            );
            if (v12 != null && v12.size() > 1) {
               v12.sort(Comparator.comparing(vec3 -> {

                  return m1571(v14, vec3);
               }));
            }

            return v12 != null && !v12.isEmpty() ? (Vec3d)v12.getFirst() : v11;
         } else {
            return v11;
         }
      }
   }

   public static Vec3d getVecNewStatic(Box aabb) {
      if (aabb == null) {
         return mc.field_1724.method_33571();
      } else {

         double v1 = aabb.field_1323 + (aabb.field_1320 - aabb.field_1323) / 2.0;
         double v3 = aabb.field_1322 + (aabb.field_1325 - aabb.field_1322) / 2.0;
         double v5 = aabb.field_1321 + (aabb.field_1324 - aabb.field_1321) / 2.0;
         Vec3d v7 = new Vec3d(v1, v3 + 0.25, v5);
         if (!m1569(mc.field_1724, v7)) {
            List v8 = entityBoxVec3dsAlternate(aabb);
            if (v8 != null && !v8.isEmpty()) {
               v8.sort(Comparator.comparing(vec -> {
                  Vec3d temp1 = mc.field_1724.method_33571();

                  return vec.method_1025(temp1);
               }));
               return (Vec3d)v8.get(0);
            }
         }

         return v7;
      }
   }

   private static boolean m1568(ClientPlayerEntity self, double x, double y, double z) {
      Vec3d result = new Vec3d;
      int v8 = 2041594564;
      int temp5 = v8 + 1;
      int v9 = 454661590;

      result./* $VF: Unable to resugar constructor */<init>(x, y, z);
      Vec3d v7 = result;
      return mc.field_1687 != null && traceBlock(self.method_33571(), v7, ShapeType.field_17558, FluidHandling.field_1348).method_17783() != Type.field_1332;
   }

   private static boolean m1569(ClientPlayerEntity self, Vec3d vec) {
      Vec3d result = new Vec3d(self.method_23317(), self.method_23320(), self.method_23321());

      Vec3d v2 = result;
      int v3 = -773058585;
      int v6 = v3 * (v3 + 1);

      return mc.field_1687 != null && traceBlock(v2, vec, ShapeType.field_17558, FluidHandling.field_1348).method_17783() != Type.field_1332;
   }

   public static int lerp(int a, int b, float f) {
      float temp1 = f * (float)(b - a);

      return a + (int)temp1;
   }

   private static boolean m1570(ClientPlayerEntity selfEntity, Vec3d xyz, float scale) {
      boolean result;
      if (scale == 0.0F) {
         result = m1568(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350);
         
      } else {
         result = m1568(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350)
            && m1568(selfEntity, xyz.field_1352, xyz.field_1351 + (double)scale, xyz.field_1350)
            && m1568(selfEntity, xyz.field_1352, xyz.field_1351 - (double)scale, xyz.field_1350)
            && m1568(selfEntity, xyz.field_1352 + (double)scale, xyz.field_1351, xyz.field_1350)
            && m1568(selfEntity, xyz.field_1352 - (double)scale, xyz.field_1351, xyz.field_1350)
            && m1568(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350 + (double)scale)
            && m1568(selfEntity, xyz.field_1352, xyz.field_1351, xyz.field_1350 - (double)scale);
      }

      return result;
   }

   private static double m1567(ClientPlayerEntity self, double x, double z) {
      double v5 = self.method_23317() - x;
      double result = self.method_23321();

      double v7 = result - z;
      int v9 = 1389278090;
      int v12 = v9 * (v9 + 1);

      return (double)MathHelper.method_15355((float)(v5 * v5 + v7 * v7));
   }

   public static Vec3d getBestVector3dOnEntityBox(Box aabb) {

      Vec3d result = getBestVector3dOnEntityBox(aabb, false);

      return result;
   }

   public static double clamp(double value, double min, double max) {
      double result = Math.min(max, Math.max(value, min));

      int v7 = 515126443;

      return result;
   }

   
}
