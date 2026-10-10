package ru.white.utils.friend;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.misc.Lp;
import ru.white.utils.render.gl.Vg;

@Environment(EnvType.CLIENT)
public class Ch implements vg {
   public Vec2f rotate;
   private float f397;
   private int f398;
   private int f399 = 0;
   private boolean f395;
   private float f394;
   private LivingEntity f391;
   private Vec3d f401;
   private Vec3d f400;
   private boolean f396;
   private float f392;
   private float f393;

   public static Vec2f getRotations(double x, double y, double z) {
      if (mc.field_1724 == null) {
         return Vec2f.field_1340;
      } else {
         double v6 = x - mc.field_1724.method_23317();
         double v8 = y - mc.field_1724.method_23320();
         double v10 = z - mc.field_1724.method_23321();
         double v12 = (double)MathHelper.method_15355((float)(v6 * v6 + v10 * v10));

         double v19 = MathHelper.method_15349(v10, v6) * (180.0 / Math.PI) - 90.0;

         float v14 = (float)v19;
         float v15 = (float)(-MathHelper.method_15349(v8, v12) * (180.0 / Math.PI));
         return new Vec2f(v14, v15);
      }
   }

   private boolean m450(Vec3d dir, double range, Box box) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         Vec3d result = mc.field_1724.method_33571();

         Vec3d v5 = result;
         if (!box.method_1006(v5)) {

            Vec3d temp2 = v5.method_1019(dir.method_1021(range));

            if (!box.method_992(v5, temp2).isPresent()) {
               return false;
            }
         }

         return true;
      }
   }

   private Vec3d m449(Entity target, double distance) {
      float v4 = 0.005F;
      float v5 = 0.015F;
      float v6 = 0.0015F;
      float v7 = 0.015F;
      double v8 = target.method_5829().method_17939();
      double v10 = target.method_5829().method_17940();
      double v12 = target.method_5829().method_17941();
      if (this.f401.equals(Vec3d.field_1353)) {
         this.f401 = new Vec3d(m452(-0.02, 0.02), m452(-0.02, 0.02), m452(-0.02, 0.02));
      }

      if (this.f400.equals(Vec3d.field_1353)) {
         this.f400 = new Vec3d(0.0, v10 * 0.5, 0.0);
      }

      this.f400 = this.f400.method_1019(this.f401);
      double v14 = (v8 - 0.1) / 2.0;
      double v16 = (v12 - 0.1) / 2.0;
      if (this.f400.field_1352 >= v14) {
         this.f401 = new Vec3d(-m452((double)v4, (double)v5), this.f401.method_10214(), this.f401.method_10215());
      } else if (this.f400.field_1352 <= -v14) {
         this.f401 = new Vec3d(m452((double)v4, (double)v5), this.f401.method_10214(), this.f401.method_10215());
      }

      if (this.f400.field_1351 >= v10 * 0.75) {
         this.f401 = new Vec3d(this.f401.method_10216(), -m452((double)v6, (double)v7), this.f401.method_10215());
      } else if (this.f400.field_1351 <= v10 * 0.3) {
         Vec3d temp1 = new Vec3d;
         double temp3 = this.f401.method_10216();

         temp1./* $VF: Unable to resugar constructor */<init>(temp3, m452((double)v6, (double)v7), this.f401.method_10215());
         this.f401 = temp1;
      }

      if (this.f400.field_1350 >= v16) {
         this.f401 = new Vec3d(this.f401.method_10216(), this.f401.method_10214(), -m452((double)v4, (double)v5));
      } else if (this.f400.field_1350 <= -v16) {
         this.f401 = new Vec3d(this.f401.method_10216(), this.f401.method_10214(), m452((double)v4, (double)v5));
      }

      if (mc.field_1724 != null && !this.m450(mc.field_1724.method_5720(), distance, target.method_5829())) {
         float v18 = (float)(v8 / 2.0) * 0.8F;

         for (float v19 = -v18; v19 <= v18; v19 += 0.1F) {
            float v20 = -v18;

            while (v20 <= v18) {
               for (float v21 = (float)(v10 * 0.9); (double)v21 >= v10 * 0.3; v21 -= 0.1F) {
                  Vec3d v22 = new Vec3d(target.method_23317() + (double)v19, target.method_23318() + (double)v21, target.method_23321() + (double)v20);
                  Vec2f v23 = getRotations(v22);
                  Vec3d v24 = m451(v23.field_1342, v23.field_1343);
                  if (this.m450(v24, distance, target.method_5829())) {
                     this.f400 = new Vec3d((double)v19, (double)v21, (double)v20);
                     return target.method_73189().method_1019(this.f400);
                  }
               }

               v20 += 0.1F;
            }
         }
      }

      return target.method_73189().method_1019(this.f400);
   }

   private float m453() {
      try {
         float v1 = lp.getGCDValue();
         if (v1 > 0.0F) {
            return v1;
         }
      } catch (Exception v4) {
      }

      return 0.15F;
   }

   private static double m452(double min, double max) {
      double result = ThreadLocalRandom.current().nextDouble() * (max - min);

      return result + min;
   }

   @Override
   public void reset() {
      this.f391 = null;
      this.f394 = 0.0F;
      this.f395 = false;

      this.f397 = 0.0F;
      this.f398 = 0;
      this.f399 = 0;
      this.f396 = mc.field_1724 != null;
      this.f400 = Vec3d.field_1353;
      this.f401 = Vec3d.field_1353;
      if (mc.field_1724 != null) {
         this.f392 = mc.field_1724.method_36454();
         this.f393 = mc.field_1724.method_36455();
      } else {
         this.f392 = 0.0F;
         this.f393 = 0.0F;
      }
   }

   private static Vec3d m451(float pitch, float yaw) {
      float v2 = -yaw * (float) (Math.PI / 180.0) - (float) Math.PI;
      float v3 = -pitch * (float) (Math.PI / 180.0);

      float v4 = MathHelper.method_15362((double)v2);
      float v5 = MathHelper.method_15374((double)v2);
      float v6 = -MathHelper.method_15362((double)v3);
      double v10 = (double)v3;

      float v7 = MathHelper.method_15374(v10);
      return new Vec3d((double)(v5 * v6), (double)v7, (double)(v4 * v6));
   }

   @Override
   public void onRotation(fg aura, LivingEntity target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null) {
         if (target == null) {
            this.reset();
         } else {
            if (this.f399 > 0) {
               this.f399--;
            }

            if (mc.field_1724.method_6039()) {
               this.rotate = new Vec2f(mc.field_1724.method_36454(), mc.field_1724.method_36455());
               this.f392 = this.rotate.field_1343;
               this.f393 = this.rotate.field_1342;
            } else {
               if (!this.f396) {
                  this.f392 = mc.field_1724.method_36454();
                  this.f393 = mc.field_1724.method_36455();
                  this.f396 = true;
               }

               if (this.f391 != target) {
                  this.f391 = target;
                  this.f394 = 0.0F;
                  this.f395 = false;
                  this.f398 = 0;
               }

               boolean v5 = mc.field_1724.method_7261(1.0F) > 0.9F && this.f399 <= 1;
               int v6 = 12;

               for (int v7 = 0; v7 < v6; v7++) {
                  this.f398++;
                  this.f397 = (float)((Math.sin((double)this.f398 * 0.17) * 0.12 + (Math.random() * 0.08 - 0.04)) * 0.7F);
                  Vec3d v8 = this.m448(target);
                  Vec2f v9 = getRotations(v8);
                  float v10 = v9.field_1343;
                  float v11 = v9.field_1342;
                  float v12 = Math.abs(MathHelper.method_15393(v10 - this.f392));
                  if (!this.f395) {
                     float v13 = 0.0055F;
                     if (v12 > 60.0F) {
                        v13 += 0.028800001F;
                     } else if (v12 > 30.0F) {
                        v13 += 0.014400001F;
                     } else {
                        v13 += 0.0072000003F;
                     }

                     if (v5) {
                        v13 += 0.012857143F;
                     }

                     this.f394 = this.f394 + v13 * (1.6F + this.f397);
                     if (this.f394 >= 0.22F) {
                        this.f395 = true;
                     }
                  } else {
                     float v28 = v5 ? 0.045F : 0.008F;
                     this.f394 = this.f394 - v28 * (2.1F + this.f397);
                     if (this.f394 <= -0.04F) {
                        this.f395 = false;
                     }
                  }

                  float v29 = MathHelper.method_15363(this.f394, 0.0F, mc.field_1724.method_6128() ? 0.38F : 0.26F);
                  if (v5) {
                     float result = v29 + 0.1F;
                     float v34;
                     if (mc.field_1724.method_6128()) {

                        v34 = 0.46F;
                     } else {
                        v34 = 0.34F;
                     }

                     v29 = Math.min(result, v34);
                  }

                  v29 += this.f397 * 0.5F;
                  if (this.f398 % (7) == 0) {
                     v29 += 0.03F;
                  }

                  float v14 = MathHelper.method_15393(v10 - this.f392);
                  float v15 = v11 - this.f393;
                  float v16 = mc.field_1724.method_6128() ? 42.0F : (v5 ? 28.0F : 20.0F);
                  float v17 = mc.field_1724.method_6128() ? 12.0F : (v5 ? 4.5F : 2.8F);
                  v14 = MathHelper.method_15363(v14, -v16, v16);
                  v15 = MathHelper.method_15363(v15, -v17, v17);
                  float v18 = v29 * 0.28F;
                  float v19 = v29 * (0.85F + this.f397 * 0.4F);

                  float v20 = this.f392 + v14 * v19;
                  float v21 = this.f393 + v15 * v18;
                  float v22 = this.m453();
                  if (v22 > 0.0F) {
                     v20 = this.f392 + (float)Math.round((v20 - this.f392) / v22) * v22;
                     v21 = this.f393 + (float)Math.round((v21 - this.f393) / v22) * v22;
                  }

                  v21 = MathHelper.method_15363(v21, -89.0F, 89.0F);
                  this.f392 = v20;
                  this.f393 = v21;
               }

               float v26 = mc.field_1724.method_6128() && target.method_6128() ? 360.0F : 180.0F;
               xd.update(new Rotation(this.f392, this.f393), v26, v26, v26, v26, 0, 1, false);
               fg.lastYaw = this.f392;
               fg.lastPitch = this.f393;
               this.rotate = new Vec2f(this.f392, this.f393);
            }
         }
      }
   }

   private Vec3d m448(LivingEntity target) {
      int v3 = -1448408570;
      int v4 = 397765041;

      Vec3d v2 = this.m449(target, 128.0);
      if (v2 == null) {
         v2 = target.method_5829().method_1005();
      }

      return v2;
   }

   @Override
   public void onAttack() {
      int temp1 = 10;

      this.f399 = temp1;
   }

   public static Vec2f getRotations(Vec3d vec3d) {

      return getRotations(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350);
   }

   public Ch() {
      this.rotate = Vec2f.field_1340;
      this.f400 = Vec3d.field_1353;
      this.f401 = Vec3d.field_1353;
   }

   
}
