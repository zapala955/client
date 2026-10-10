package ru.white.utils.friend;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.misc.Lp;
import ru.white.utils.misc.Np;
import ru.white.utils.render.gl.Vg;
import ru.white.utils.rotation.Kd;
import ru.white.utils.rotation.Md;
import ru.white.utils.rotation.Qd;

@Environment(EnvType.CLIENT)
public class Yg implements vg {
   private float f374;
   private boolean f366 = false;
   private float f364;
   private boolean f386;
   private Vec3d f370;
   private Vec3d f369;
   private float f368;
   private long f382;
   private final qd f383;
   private float f375;
   private long f371;
   private int f380;
   private float f385;
   private float f373;
   private int f381;
   private float f376;
   private float f372;
   private int f378;
   private int f379;
   private float f367 = 0.0F;
   private float f384;
   private float f377;
   private float f365;

   private void m443(float targetYaw, float targetPitch, float playerYaw, float playerPitch, float maxStepYaw, float maxStepPitch) {
      float v7 = lp.getGCDValue();
      if (v7 <= 1.0E-5F) {
         v7 = 0.001F;
      }

      float v8 = MathHelper.method_15393(targetYaw - this.f364);
      float v9 = targetPitch - this.f365;
      float v10 = v8 / v7;
      float v11 = v9 / v7;
      int v12 = (int)v10;
      int v13 = (int)v11;
      float v14 = v10 - (float)v12;
      float v15 = v11 - (float)v13;
      float v16 = this.f383.sampleStochasticStep();
      float temp1 = this.f367 + v14;

      this.f367 = temp1 * 0.88F + v16 * 0.12F;
      this.f368 = (this.f368 + v15) * 0.88F + v16 * 0.08F;
      if (this.f367 >= 0.52F) {
         v12++;
         this.f367--;
      } else if (this.f367 <= -0.52F) {
         v12--;
         this.f367++;
      }

      if (this.f368 >= 0.52F) {
         v13++;
         this.f368--;
      } else if (this.f368 <= -0.52F) {
         v13--;
         this.f368++;
      }

      float v17 = (float)v12 * v7;
      float v18 = (float)v13 * v7;
      float v19 = this.f364 + v17;
      float v20 = MathHelper.method_15363(this.f365 + v18, -89.5F, 89.5F);
      this.f376 = v17 - this.f372;
      this.f377 = v18 - this.f373;
      this.f374 = this.f372;
      this.f375 = this.f373;
      this.f372 = v17;
      this.f373 = v18;
      this.f364 = v19;
      this.f365 = v20;
      float v21 = Math.max(Math.abs(v17) * 1.5F, 180.0F);
      float v22 = Math.max(Math.abs(v18) * 1.5F, 180.0F);
      Rotation result = new Rotation(this.f364, this.f365);

      xd.update(result, v21, v22, 360.0F, 360.0F, 0, 15, false);
   }

   @Override
   public void onRotation(fg aura, LivingEntity target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null && target.method_5805()) {
         float v5 = mc.field_1724.method_36454();
         float v6 = mc.field_1724.method_36455();
         long v7 = System.currentTimeMillis();
         boolean v9 = target.method_5628() != this.f381;
         if (!this.f366) {
            this.f364 = v5;
            this.f365 = v6;
            this.f367 = 0.0F;
            this.f368 = 0.0F;
            this.f366 = true;
            this.f381 = target.method_5628();
            v9 = true;
         }

         if (v9) {
            this.f381 = target.method_5628();
            this.f372 = 0.0F;
            this.f373 = 0.0F;
            this.f374 = 0.0F;
            this.f375 = 0.0F;
            this.f376 = 0.0F;
            this.f377 = 0.0F;
            this.f386 = false;
            this.f380 = 0;
            this.f382 = v7;
            this.f383.reset();
         }

         if (v7 - this.f382 >= 4156723999394576038L + -4156723999394575928L) {
            if (aura.justAttacked) {
               aura.justAttacked = false;
               this.f379 = 0;
            } else {
               this.f379 = Math.min(this.f379 + 1, 999);
            }

            if (this.f378 > 0) {
               this.f378--;
            }

            float v10 = mc.field_1724.method_7261(0.5F);
            boolean v11 = !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < -0.03;
            if (canAttack && v10 >= 0.85F && v11) {
               this.f378 = 3;
            }

            md v12 = kd.get().getCurrent();
            Vec3d v13 = mc.field_1724.method_33571();
            Vec3d v14 = target.method_5829().method_1005();
            Vec3d v15 = v14.method_1020(v13);
            float v16 = MathHelper.method_15393(m444(v15) - v5);
            float v17 = m445(v15) - v6;
            float[] v18 = kd.get().nextMoveEx(v16, v17, (float)Math.min(this.f379, 60));
            if (v18 != null) {
               float v19 = (float)Math.hypot((double)v16, (double)v17);
               float v20 = (float)Math.hypot((double)v18[2], (double)v18[3]);
               if (v19 <= 22.0F || v20 >= v19 * 0.6F) {
                  float v21 = v5 + v18[0];
                  float v22 = MathHelper.method_15363(v6 + v18[1], -89.5F, 89.5F);
                  float v24 = (float)Math.hypot((double)MathHelper.method_15393(m444(v15) - v21), (double)(m445(v15) - v22));
                  float v25 = (this.f386 ? 1.5F : 0.5F) + (this.f379 <= 6 ? 6.5F : 0.0F);
                  if (v24 <= v19 + v25) {
                     this.m443(v21, v22, v5, v6, 80.0F, 60.0F);
                     this.f386 = true;
                     return;
                  }
               }
            }

            this.f386 = false;
            Box v63 = target.method_5829();
            double v64 = v15.method_1033();
            Vec3d v65 = target.method_18798();
            Vec3d v23 = mc.field_1724.method_18798();
            float v66 = 0.0F;
            float v67 = 0.0F;
            if (v64 > 0.01) {
               Vec3d v26 = v15.method_1021(1.0 / v64);
               Vec3d v27 = new Vec3d(-v26.field_1350, 0.0, v26.field_1352);
               v66 = (float)(v65.method_1026(v27) / v64 * 20.0);
               v67 = (float)(v65.field_1351 / v64 * 20.0);
            }

            float v68 = (float)(target.method_23318() - mc.field_1724.method_23318());
            float v69 = this.f372 - this.f374;
            float v28 = this.f373 - this.f375;
            float v29 = MathHelper.method_15393(m444(v15) - this.f364);
            float v30 = m445(v15) - this.f365;
            double[] v31 = md.features(
               v29,
               v30,
               this.f372,
               this.f373,
               this.f374,
               this.f375,
               v69,
               v28,
               (float)this.f369.field_1352,
               (float)this.f369.field_1351,
               (float)this.f369.field_1350,
               (float)v64,
               v68,
               this.f365,
               m445(v15),
               v66,
               v67,
               (float)v65.field_1352,
               (float)v65.field_1351,
               (float)v65.field_1350,
               (float)v23.field_1351,
               v10,
               (float)this.f379,
               1.0F,
               0.0F
            );
            md.od v32 = kd.get().predictDetailed(v31);
            if (v7 - this.f371
               > ThreadLocalRandom.current().nextLong(300620499796657848L + -300620499796657708L, 7115920073681762152L + -7115920073681761832L)) {
               this.f371 = v7;
               float[] result;
               if (v12 != null) {
                  result = v12.sampleMultiPoint(v29, v30, this.f380);
               } else {
                  result = new float[]{0.5F, 0.0F, 0.0F};

                  result[1] = 0.55F;
                  result[2] = 0.5F;
               }

               float[] v33 = result;
               this.f370 = new Vec3d((double)v33[0], (double)v33[1], (double)v33[2]);
            }

            this.f369 = this.f369.method_1019(this.f370.method_1020(this.f369).method_1021(0.18));
            double v70 = MathHelper.method_16436(this.f369.field_1352, v63.field_1323 + 0.06, v63.field_1320 - 0.06);
            double v35 = MathHelper.method_16436(this.f369.field_1351, v63.field_1322 + 0.12, v63.field_1325 - 0.08);
            double v37 = MathHelper.method_16436(this.f369.field_1350, v63.field_1321 + 0.06, v63.field_1324 - 0.06);
            if (v32 != null) {
               double v39 = 0.07;
               v70 += (double)v32.yawLead * v39 * v65.field_1352;
               v35 += (double)v32.pitchLead * v39 * v65.field_1351;
               v37 += (double)v32.yawLead * v39 * v65.field_1350;
            }

            v70 = MathHelper.method_15350(v70, v63.field_1323 + 0.04, v63.field_1320 - 0.04);
            v35 = MathHelper.method_15350(v35, v63.field_1322 + 0.08, v63.field_1325 - 0.05);
            v37 = MathHelper.method_15350(v37, v63.field_1321 + 0.04, v63.field_1324 - 0.04);
            Vec3d v74 = new Vec3d(v70, v35, v37);
            Vec3d v40 = v74.method_1020(v13);
            float v41 = m444(v40);
            float v42 = m445(v40);
            float v43 = MathHelper.method_15393(v41 - this.f364);
            float v44 = v42 - this.f365;
            float v45 = (float)Math.hypot((double)v43, (double)v44);
            float v46;
            float v47;
            float v48;
            float v49;
            if (v45 > 24.0F) {
               v46 = this.f378 > 0 ? 0.94F : 0.88F;
               v47 = 85.0F;
               v48 = 60.0F;
               v49 = 0.12F;
            } else if (v45 > 8.0F) {
               float v50 = v32 != null ? MathHelper.method_15363(v32.yawSpeed / 75.0F, 0.45F, 0.8F) : 0.65F;
               v46 = this.f378 > 0 ? 0.9F : v50;
               v47 = v32 != null && v32.yawSpeed > 10.0F ? Math.max(30.0F, v32.yawSpeed) : 48.0F;
               v48 = v32 != null && v32.pitchSpeed > 8.0F ? Math.max(20.0F, v32.pitchSpeed) : 34.0F;
               v49 = 0.45F;
               if (v32 != null && v32.overshootRatio > 0.05F && this.f378 == 0) {
                  float v51 = MathHelper.method_15363(v32.overshootRatio, 0.0F, 0.6F);
                  v43 += Math.signum(v43) * v51 * 2.8F;
                  v44 += Math.signum(v44) * v51 * 1.5F;
               }
            } else {
               v46 = this.f378 > 0 ? 0.88F : 0.52F;
               v47 = 28.0F;
               v48 = 20.0F;
               v49 = 1.0F;
            }

            float v75 = v32 != null ? Math.max(v32.yawShake, 0.22F) : 0.35F;
            float v76 = (float)Math.hypot((double)this.f372, (double)this.f373);
            float[] v52 = this.f383.sample(v75 * v49, v76, this.f380);
            float[] v53 = v12 != null ? v12.nextJitter(v76) : new float[]{0.0F, 0.0F};
            float v54 = MathHelper.method_15363(v53[0], -6.0F, 6.0F) * 0.25F * v49;
            float v55 = MathHelper.method_15363(v53[1], -4.0F, 4.0F) * 0.25F * v49;
            this.f384 = this.f384 + (v54 - this.f384) * 0.4F;
            this.f385 = this.f385 + (v55 - this.f385) * 0.4F;
            float v56 = v43 * v46 + v52[0] + this.f384;
            float v57 = v44 * v46 + v52[1] + this.f385;
            v56 = MathHelper.method_15363(v56, -v47, v47);
            v57 = MathHelper.method_15363(v57, -v48, v48);
            float v58 = this.f364 + v56;
            float v59 = MathHelper.method_15363(this.f365 + v57, -89.5F, 89.5F);
            if (v45 < 12.0F && !np.rayTraceEntity(v58, v59, 4.5, target)) {
               float v60 = MathHelper.method_15393(v41 - this.f364);
               float v61 = v42 - this.f365;
               v58 = this.f364 + MathHelper.method_15363(v56 + v60 * 0.35F, -v47, v47);
               v59 = MathHelper.method_15363(this.f365 + v57 + v61 * 0.35F, -89.5F, 89.5F);
            }

            this.m443(v58, v59, v5, v6, v47, v48);
         }
      }
   }

   public Yg() {
      this.f368 = 0.0F;
      this.f369 = new Vec3d(0.5, 0.55, 0.5);
      this.f370 = new Vec3d(0.5, 0.55, 0.5);
      this.f371 = 0L;
      this.f372 = 0.0F;
      this.f373 = 0.0F;
      this.f374 = 0.0F;
      this.f375 = 0.0F;
      this.f376 = 0.0F;
      this.f377 = 0.0F;
      this.f378 = 0;
      this.f379 = 999;
      this.f380 = 0;
      this.f381 = -1;
      this.f382 = 0L;
      this.f383 = new qd();
      this.f384 = 0.0F;
      this.f385 = 0.0F;
      this.f386 = false;
   }

   @Override
   public void onAttack() {

      this.f378 = 3;
      this.f379 = 0;

      this.f380++;
      this.f371 = 0L;
   }

   private static float m445(Vec3d aim) {

      double result = Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350));
      int v2 = 382033089;
      int v4 = v2 * (v2 + 1);

      return (float)MathHelper.method_15350(-Math.toDegrees(result), -90.0, 90.0);
   }

   private static float m444(Vec3d aim) {
      double result = -aim.field_1352;

      return (float)Math.toDegrees(Math.atan2(result, aim.field_1350));
   }

   @Override
   public void reset() {
      this.f366 = false;
      this.f367 = 0.0F;
      this.f368 = 0.0F;

      this.f369 = new Vec3d(0.5, 0.55, 0.5);
      this.f370 = new Vec3d(0.5, 0.55, 0.5);
      this.f371 = 0L;

      this.f378 = 0;
      this.f379 = 999;
      this.f380 = 0;
      this.f372 = 0.0F;
      this.f373 = 0.0F;
      this.f374 = 0.0F;
      this.f375 = 0.0F;
      this.f376 = 0.0F;
      this.f377 = 0.0F;
      this.f381 = -1;
      this.f382 = 0L;
      this.f386 = false;
      this.f384 = 0.0F;
      this.f385 = 0.0F;
      this.f383.reset();
   }

   
}
