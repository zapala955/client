package ru.white.utils.misc;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class Np implements IMinecraft {
   public static EntityHitResult traceEntities(Entity shooter, Vec3d startVector, Vec3d endVector, Box boundingBox, Predicate<Entity> filter, double distance) {
      World v7 = shooter.method_73183();
      double v8 = distance;
      Entity v10 = null;
      Vec3d v11 = null;

      for (Entity v13 : v7.method_8333(shooter, boundingBox, filter)) {
         Box result = v13.method_5829().method_1014((double)v13.method_5871());

         Box v14 = result;
         Optional v15 = v14.method_992(startVector, endVector);
         if (v14.method_1006(startVector) || v15.isPresent()) {
            double v16 = v15.<Double>map(startVector::method_1025).orElse(0.0);
            v16 = Math.sqrt(v16);
            double v24;
            int v22 = (v24 = v16 - v8) == 0.0 ? 0 : (v24 < 0.0 ? -1 : 1);

            if ((v22 < 0 || v8 == 0.0) && v13.method_5668() != shooter.method_5668()) {
               v10 = v13;
               v11 = v15.orElse(startVector);
               v8 = v16;
            }
         }
      }

      return v10 == null ? null : new EntityHitResult(v10, v11);
   }

   public static boolean rayTraceEntity(float yaw, float pitch, double distance, Entity entity) {

      float v5 = mc.method_61966().method_60637(true);
      Vec3d v6 = mc.field_1724.method_5836(v5);
      Vec3d v7 = getVectorForRotation(pitch, yaw);
      Vec3d v8 = v6.method_1019(v7.method_1021(distance));
      if (mc.field_1724 != null && mc.field_1724.method_6128() && (double)entity.method_5739(entity) > distance * 3.0) {
         return true;
      } else {
         Box v9 = entity.method_5829();
         return v9.method_1006(v6) || v9.method_992(v6, v8).isPresent();
      }
   }

   @Generated
   private Np() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static HitResult calculateRayTrace(double distance, float yaw, float pitch, Entity entity, boolean ignoreBlocks) {
      float v6 = mc.method_61966().method_60637(true);
      Vec3d v7 = mc.field_1724.method_5836(v6);
      Vec3d v8 = getVectorForRotation(pitch, yaw);
      Vec3d v9 = v7.method_1019(v8.method_1021(distance));
      HitResult v10 = traceBlock(v7, v9, ShapeType.field_17558, FluidHandling.field_1348);
      double v11 = v10.method_17784().method_1025(v7);
      Box v13 = entity.method_5829().method_18804(v8.method_1021(distance)).method_1014(1.0);
      EntityHitResult v14 = ProjectileUtil.method_18075(entity, v7, v9, v13, x -> {
         if (!x.method_7325()) {

            if (x.method_5805() && x.method_5863()) {
               return true;
            }
         }

         return false;
      }, distance * distance);
      if (v14 != null) {
         if (ignoreBlocks) {
            return v14;
         }

         if (v14.method_17784().method_1025(v7) < v11) {
            int v15 = -719158260;

            return v14;
         }
      }

      return v10;
   }

   public static HitResult traceBlock(Vec3d startVec, Vec3d endVec, ShapeType blockMode, FluidHandling fluidMode) {
      ClientWorld result = mc.field_1687;
      RaycastContext temp1 = new RaycastContext;
      int v4 = 389290649;

      temp1./* $VF: Unable to resugar constructor */<init>(startVec, endVec, blockMode, fluidMode, mc.field_1724);

      return result.method_17742(temp1);
   }

   public static Vec3d calculateViewVector(float yaw, float pitch) {
      float v2 = (float)((double)pitch * (Math.PI / 180.0));
      float v3 = (float)((double)(-yaw) * (Math.PI / 180.0));
      float v4 = MathHelper.method_15362((double)v3);

      float v5 = MathHelper.method_15374((double)v3);
      float v6 = MathHelper.method_15362((double)v2);
      float v7 = MathHelper.method_15374((double)v2);
      return new Vec3d((double)(v5 * v6), (double)(-v7), (double)(v4 * v6));
   }

   public static Vec3d getVectorForRotation(float pitch, float yaw) {
      float v2 = -yaw * (float) (Math.PI / 180.0) - (float) Math.PI;
      float result = -pitch;

      float v3 = result * (float) (Math.PI / 180.0);
      float v4 = MathHelper.method_15362((double)v2);
      double v10 = (double)v2;

      float v5 = MathHelper.method_15374(v10);
      float v6 = -MathHelper.method_15362((double)v3);
      float v7 = MathHelper.method_15374((double)v3);
      return new Vec3d((double)(v5 * v6), (double)v7, (double)(v4 * v6));
   }

   
}
