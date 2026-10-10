package ru.white.utils.misc;

import java.lang.runtime.ObjectMethods;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import ru.white.auth.StrDec;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class Zq implements IMinecraft {
   private static final double NEAR_PLANE = 0.05;

   public static Vector4d getVector4D(Entity ent) {
      Vector4d result = getVector4D(ent, mc.method_61966().method_60637(true));

      return result;
   }

   public static double centerX(Vector4d vec) {

      return vec.x + (vec.z - vec.x) / 2.0;
   }

   private static boolean m1648(Vec3d point, Vec3d cameraPos, Camera camera) {
      double v3 = point.field_1352 - cameraPos.field_1352;
      double v5 = point.field_1351 - cameraPos.field_1351;
      double v7 = point.field_1350 - cameraPos.field_1350;
      double v9 = (double)camera.method_19330();
      double v11 = (double)camera.method_19329();
      double v13 = Math.toRadians(v9);
      double v15 = Math.toRadians(v11);
      double v17 = Math.cos(v15);
      double v19 = -Math.sin(v13) * v17;
      double v21 = -Math.sin(v15);
      double v23 = Math.cos(v13) * v17;
      double v25 = v19 * v3 + v21 * v5 + v23 * v7;
      boolean result;
      if (v25 > -10.0) {
         result = true;
         
      } else {
         result = false;
      }

      return result;
   }

   private static Matrix4d m1642(Matrix4f mat) {
      Matrix4d v1 = new Matrix4d();
      v1.m00((double)mat.m00()).m01((double)mat.m01()).m02((double)mat.m02()).m03((double)mat.m03());
      v1.m10((double)mat.m10()).m11((double)mat.m11()).m12((double)mat.m12()).m13((double)mat.m13());

      v1.m20((double)mat.m20()).m21((double)mat.m21()).m22((double)mat.m22()).m23((double)mat.m23());

      v1.m30((double)mat.m30()).m31((double)mat.m31()).m32((double)mat.m32()).m33((double)mat.m33());
      return v1;
   }

   @Generated
   private Zq() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean isInFrontOfCamera(Vec3d worldPos) {
      Camera v1 = mc.field_1773.method_19418();
      if (v1 != null && v1.method_19332()) {
         Vec3d temp1 = v1.method_71156();
         int v2 = 2076889222;

         return m1648(worldPos, temp1, v1);
      } else {
         return false;
      }
   }

   public static boolean cantSee(Vector4d vec) {
      if (vec == null) {
         return true;
      } else {
         double v1 = (double)mc.method_22683().method_4489() / 2.0;
         double v3 = (double)mc.method_22683().method_4506() / 2.0;
         if (!Double.isNaN(vec.x) && !Double.isNaN(vec.y) && !Double.isNaN(vec.z) && !Double.isNaN(vec.w)) {
            if (!Double.isInfinite(vec.x) && !Double.isInfinite(vec.y)) {

               if (!Double.isInfinite(vec.z) && !Double.isInfinite(vec.w)) {
                  if (!(vec.z < -v1) && !(vec.x > v1 * 2.0)) {
                     if (!(vec.w < -v3) && !(vec.y > v3 * 2.0)) {
                        return false;
                     }

                     return true;
                  }

                  return true;
               }
            }

            return true;
         } else {
            return true;
         }
      }
   }

   public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
      Camera v1 = mc.method_1561().field_4686;
      if (v1 == null) {
         return Vec3d.field_1353;
      } else {
         int v2 = mc.method_22683().method_4506();
         int[] v3 = new int[]{0, 0, mc.method_22683().method_4489(), v2};
         Vector3f v4 = new Vector3f();
         double v5 = pos.field_1352 - v1.method_71156().field_1352;
         double v7 = pos.field_1351 - v1.method_71156().field_1351;
         double v9 = pos.field_1350 - v1.method_71156().field_1350;
         Vector4f v11 = new Vector4f((float)v5, (float)v7, (float)v9, 1.0F);
         v11.mul(vs.zs.lastWorldSpaceMatrix);
         Matrix4f v12 = new Matrix4f(vs.zs.lastProjMat);
         Matrix4f v13 = new Matrix4f(vs.zs.lastModMat);
         v12.mul(v13).project(v11.x(), v11.y(), v11.z(), v3, v4);
         Vec3d result = new Vec3d;
         double temp2 = (double)v4.x;

         result./* $VF: Unable to resugar constructor */<init>(temp2 / 2.0, (double)((float)v2 - v4.y) / 2.0, (double)v4.z);
         return result;
      }
   }

   private static double m1646(double x, double screenW) {
      double result = -screenW * 2.0;

      return Math.max(result, Math.min(screenW * 3.0, x));
   }

   public static Vec2f fromVec3d(Vec3d vector) {
      Vec2f result = new Vec2f;
      double temp2 = Math.toDegrees(Math.atan2(vector.field_1350, vector.field_1352));

      float v4 = (float)MathHelper.method_15338(temp2 - 90.0);
      double temp3 = Math.atan2(vector.field_1351, Math.hypot(vector.field_1352, vector.field_1350));

      result./* $VF: Unable to resugar constructor */<init>(v4, (float)MathHelper.method_15338(Math.toDegrees(-temp3)));
      return result;
   }

   public static double getDistanceToGround() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (double v0 = mc.field_1724.method_23318(); v0 > 0.0; v0 -= 0.1) {
            if (!mc.field_1687.method_8320(mc.field_1724.method_24515().method_10087((int)(mc.field_1724.method_23318() - v0 + 1.0))).method_26215()) {
               double result = mc.field_1724.method_23318() - v0;

               return result;
            }
         }

         return 256.0;
      } else {
         return 256.0;
      }
   }

   private static double m1647(double y, double screenH) {

      double result = -screenH;

      return Math.max(result * 2.0, Math.min(screenH * 3.0, y));
   }

   public static boolean canSee(Vec3d vec3d) {
      Camera v1 = mc.field_1773.method_19418();
      if (v1 == null) {

         return false;
      } else {
         Vec2f v2 = fromVec3d(vec3d.method_1020(v1.method_71156()));

         boolean v6;
         if ((
               !(Math.abs(MathHelper.method_15393(v2.field_1343 - v1.method_19330())) < 90.0F)
                  || !(Math.abs(MathHelper.method_15393(v2.field_1342 - v1.method_19329())) < 60.0F)
            )
            && !canSee(new Box(BlockPos.method_49638(vec3d)))) {
            v6 = false;
            
         } else {
            v6 = true;
         }

         return v6;
      }
   }

   public static Vec3d interpolate(Entity entity) {
      float v1 = mc.method_61966().method_60637(true);
      int v2 = 315143412;

      int temp2 = v2 + 1;

      return entity.method_30950(v1);
   }

   private static Vec3d m1645(zq.ar clip, int[] viewport, int displayHeight, double scale) {
      double v5 = clip.f1298;
      if (Math.abs(v5) < 1.0E-14) {
         return new Vec3d((double)viewport[2] / scale / 2.0, (double)displayHeight / scale / 2.0, 0.0);
      } else {
         double v7 = 1.0 / v5;
         double v9 = clip.x * v7;
         double v11 = clip.y * v7;
         double v13 = clip.z * v7;

         double v15 = (v9 * 0.5 + 0.5) * (double)viewport[2] / scale;
         double v17 = ((double)displayHeight - (v11 * 0.5 + 0.5) * (double)viewport[3]) / scale;

         double v19 = v13 * 0.5 + 0.5;
         return new Vec3d(v15, v17, v5 > 0.0 ? v19 : -1.0);
      }
   }

   public static boolean canSee(Box box) {
      if (box != null && mc.field_1773 != null) {
         Camera v1 = mc.field_1773.method_19418();
         if (v1 != null && v1.method_19332()) {
            Vec3d v2 = v1.method_71156();
            Matrix4f v3 = new Matrix4f().rotation(v1.method_23767());
            Matrix4f v4 = mc.field_1773.method_22973(((Integer)mc.field_1690.method_41808().method_41753()).floatValue());
            Frustum result = new Frustum(v3, v4);

            Frustum v5 = result;

            v5.method_23088(v2.field_1352, v2.field_1351, v2.field_1350);
            return v5.method_23093(box);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static Vector4d getVector4D(Entity ent, float tickDelta) {
      if (ent == null) {
         return null;
      } else {
         Camera v2 = mc.method_1561().field_4686;
         if (v2 == null) {
            return null;
         } else {
            Vec3d v3 = v2.method_71156();
            Vec3d v4 = ent.method_30950(tickDelta);
            Vec3d v5 = ent.method_73189();
            Box v6 = ent.method_5829().method_997(v4.method_1020(v5));
            Vec3d v7 = v6.method_1005();
            double v8 = m1643(v7, v3);
            if (v8 < -5.0) {
               return null;
            } else {
               int v10 = mc.method_22683().method_4506();
               int[] v11 = new int[]{0, 0, mc.method_22683().method_4489(), v10};
               double v12 = 2.0;
               double v14 = (double)mc.method_22683().method_4489() / 2.0;
               double v16 = (double)mc.method_22683().method_4506() / 2.0;
               Vec3d[] result = new Vec3d[8];
               result[0] = new Vec3d(v6.field_1323, v6.field_1322, v6.field_1321);
               result[1] = new Vec3d(v6.field_1323, v6.field_1322, v6.field_1324);
               result[2] = new Vec3d(v6.field_1320, v6.field_1322, v6.field_1321);
               result[3] = new Vec3d(v6.field_1320, v6.field_1322, v6.field_1324);
               result[4] = new Vec3d(v6.field_1323, v6.field_1325, v6.field_1321);
               result[5] = new Vec3d(v6.field_1323, v6.field_1325, v6.field_1324);
               result[6] = new Vec3d(v6.field_1320, v6.field_1325, v6.field_1321);
               result[7] = new Vec3d(v6.field_1320, v6.field_1325, v6.field_1324);

               Vec3d[] v18 = result;
               int[][] v62 = new int[12][];
               v62[0] = new int[]{0, 1};
               v62[1] = new int[]{0, 2};
               v62[2] = new int[]{1, 3};
               v62[3] = new int[]{2, 3};
               v62[4] = new int[]{4, 5};
               v62[5] = new int[]{4, 6};
               v62[6] = new int[]{5, 7};
               v62[7] = new int[]{6, 7};
               v62[8] = new int[]{0, 4};

               v62[9] = new int[]{1, 5};
               v62[10] = new int[]{2, 6};
               v62[11] = new int[]{3, 7};
               int[][] v19 = v62;
               zq.ar[] v20 = new zq.ar[8];

               for (int v21 = 0; v21 < 8; v21++) {
                  v20[v21] = m1644(v18[v21], v3);
                  if (v20[v21] == null) {
                     return null;
                  }
               }

               double v51 = Double.MAX_VALUE;
               double v23 = Double.MAX_VALUE;
               double v25 = -Double.MAX_VALUE;
               double v27 = -Double.MAX_VALUE;
               int v29 = 0;

               for (int v30 = 0; v30 < 8; v30++) {
                  zq.ar v31 = v20[v30];
                  if (v31.f1299 > 0.05) {
                     v29++;
                     Vec3d v32 = m1645(v31, v11, v10, v12);
                     double v33 = m1646(v32.field_1352, v14);
                     double v35 = m1647(v32.field_1351, v16);
                     v51 = Math.min(v51, v33);
                     v23 = Math.min(v23, v35);
                     v25 = Math.max(v25, v33);
                     v27 = Math.max(v27, v35);
                  }
               }

               for (int[] v59 : v19) {
                  zq.ar v34 = v20[v59[0]];
                  zq.ar v60 = v20[v59[1]];
                  boolean v36 = v34.f1299 > 0.05;

                  boolean v37 = v60.f1299 > 0.05;
                  if (v36 != v37) {
                     double v38 = v60.f1299 - v34.f1299;
                     if (!(Math.abs(v38) < 1.0E-14)) {
                        double v40 = (0.05 - v34.f1299) / v38;
                        v40 = Math.max(0.0, Math.min(1.0, v40));
                        zq.ar v42 = new zq.ar(
                           v34.x + v40 * (v60.x - v34.x),
                           v34.y + v40 * (v60.y - v34.y),
                           v34.z + v40 * (v60.z - v34.z),
                           v34.f1298 + v40 * (v60.f1298 - v34.f1298),
                           0.05
                        );
                        Vec3d v43 = m1645(v42, v11, v10, v12);
                        double v44 = m1646(v43.field_1352, v14);
                        double v46 = m1647(v43.field_1351, v16);
                        v51 = Math.min(v51, v44);
                        v23 = Math.min(v23, v46);
                        v25 = Math.max(v25, v44);
                        v27 = Math.max(v27, v46);
                     }
                  }
               }

               if (v29 == 0 && v51 == Double.MAX_VALUE) {
                  return null;
               } else if (!(v25 <= v51) && !(v27 <= v23)) {
                  v51 = Math.max(-v14, v51);
                  v23 = Math.max(-v16, v23);
                  v25 = Math.min(v14 * 2.0, v25);
                  v27 = Math.min(v16 * 2.0, v27);
                  return new Vector4d(v51, v23, v25, v27);
               } else {
                  return null;
               }
            }
         }
      }
   }

   private static zq.ar m1644(Vec3d pos, Vec3d cameraPos) {
      double v2 = pos.field_1352 - cameraPos.field_1352;
      double v4 = pos.field_1351 - cameraPos.field_1351;
      double v6 = pos.field_1350 - cameraPos.field_1350;
      Matrix4d v8 = m1642(vs.zs.lastWorldSpaceMatrix);
      Vector4d v9 = new Vector4d(v2, v4, v6, 1.0);
      v8.transform(v9);
      Matrix4d v10 = m1642(vs.zs.lastProjMat);
      Matrix4d v11 = m1642(vs.zs.lastModMat);
      Matrix4d v12 = new Matrix4d(v10).mul(v11);
      double result = v12.m00() * v9.x;

      double v13 = result + v12.m10() * v9.y + v12.m20() * v9.z + v12.m30() * v9.w;
      double v15 = v12.m01() * v9.x + v12.m11() * v9.y + v12.m21() * v9.z + v12.m31() * v9.w;
      double v17 = v12.m02() * v9.x + v12.m12() * v9.y + v12.m22() * v9.z + v12.m32() * v9.w;
      double v19 = v12.m03() * v9.x + v12.m13() * v9.y + v12.m23() * v9.z + v12.m33() * v9.w;
      return new zq.ar(v13, v15, v17, v19, -v9.z);
   }

   public static Vec3d interpolate(Entity entity, float tickDelta) {

      return entity.method_30950(tickDelta);
   }

   private static double m1643(Vec3d pos, Vec3d cameraPos) {

      double v2 = pos.field_1352 - cameraPos.field_1352;
      double v4 = pos.field_1351 - cameraPos.field_1351;

      double v6 = pos.field_1350 - cameraPos.field_1350;
      Matrix4d v8 = m1642(vs.zs.lastWorldSpaceMatrix);
      Vector4d v9 = new Vector4d(v2, v4, v6, 1.0);
      v8.transform(v9);
      return -v9.z;
   }

   @Environment(EnvType.CLIENT)
   private static record ar(double x, double y, double z, double w, double viewZ) {
      private final double f1299;
      private final double f1298;

      public double w() {
         return this.f1298;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",zq.ar,"x;y;z;w;viewZ",zq.ar::x,zq.ar::y,zq.ar::z,zq.ar::f1298,zq.ar::f1299>(this);

         return result;
      }

      private ar(double x, double y, double z, double w, double viewZ) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.f1298 = w;
         this.f1299 = viewZ;
      }

      public double z() {

         return this.z;
      }

      public double viewZ() {

         return this.f1299;
      }

      public double x() {

         return this.x;
      }

      @Override
      public final boolean equals(Object o) {

         int v2 = -590543472;

         return ObjectMethods.bootstrap<"equals",zq.ar,"x;y;z;w;viewZ",zq.ar::x,zq.ar::y,zq.ar::z,zq.ar::f1298,zq.ar::f1299>(this, o);
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",zq.ar,"x;y;z;w;viewZ",zq.ar::x,zq.ar::y,zq.ar::z,zq.ar::f1298,zq.ar::f1299>(this);

         return result;
      }

      
   }
}
