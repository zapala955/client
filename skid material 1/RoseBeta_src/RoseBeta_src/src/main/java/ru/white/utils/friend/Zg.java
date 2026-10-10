package ru.white.utils.friend;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleHelper;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.misc.Kp;
import ru.white.utils.render.ColorPalette;
import ru.white.utils.misc.Rp;
import ru.white.utils.render.gl.Vg;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
public class Zg implements vg {
   private void m446(fg aura, LivingEntity target, float[] ranges) {
      boolean v4 = qp.shouldAttack(target, false, true, true, -1482526974361221635L + 1482526974361221535L, ranges);
      Vec3d v5 = kp.getVector3(target);
      float v6 = (float)Math.toDegrees(Math.atan2(-v5.field_1352, v5.field_1350));
      double result = MathHelper.method_15350(-Math.toDegrees(Math.atan2(v5.field_1351, Math.hypot(v5.field_1352, v5.field_1350))), -90.0, 90.0);

      float v7 = (float)result;
      float v8 = fq.randomLerp(80.0F, 90.0F);
      float v9 = fq.randomLerp(80.0F, 90.0F);
      float v10 = (float)Math.cos((double)System.currentTimeMillis() / 30.0);
      float v11 = (float)Math.sin((double)System.currentTimeMillis() / 30.0);
      float v12 = v10 * fq.randomLerp(8.0F, 16.0F);
      float v13 = v11 * fq.randomLerp(8.0F, 16.0F);
      boolean v14 = false;
      if (v4) {
         aura.tick = 1.0F;
      }

      float v22;
      int v19 = (v22 = aura.tick - 0.0F) == 0.0F ? 0 : (v22 < 0.0F ? -1 : 1);

      if (v19 > 0) {
         v14 = true;
         aura.tick--;
      }

      if (!v14) {
         v12 = 0.0F;
         v13 = 0.0F;
         v6 = wd.freeYaw;
         v7 = wd.freePitch;
      }

      Rotation v15 = new Rotation(v6 + v12, v7 + v13);
      xd.update(
         v15,
         v8,
         v9,
         (float)fq.randomInt(90, 180),
         (float)fq.randomInt(90, 180),
         fq.randomInt(0, 3),
         15,
         false
      );
   }

   @Override
   public void onRotation(fg aura, LivingEntity target, float[] ranges, boolean canAttack) {
      boolean result = aura.typeSnap.is("Fov");

      if (result) {

         this.m447(aura, target, canAttack);
      } else {
         this.m446(aura, target, ranges);
      }
   }

   private void m447(fg aura, LivingEntity target, boolean canAttack) {
      float v5;
      float v6;
      float v7;
      float v8;
      label15: {
         Vec3d v4 = rp.getBestVector3dOnEntityBox(target.method_5829(), false)
            .method_1031(
               0.3F * Math.sin((double)System.currentTimeMillis() / 50.0),
               0.1F * Math.sin((double)System.currentTimeMillis() / 50.0) + 0.3F * Math.cos((double)System.currentTimeMillis() / 50.0),
               0.3F * Math.cos((double)System.currentTimeMillis() / 50.0)
            )
            .method_1020(mc.field_1724.method_33571());
         v5 = (float)Math.toDegrees(Math.atan2(-v4.field_1352, v4.field_1350));
         v6 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v4.field_1351, Math.hypot(v4.field_1352, v4.field_1350))), -90.0, 90.0);
         v7 = (float)Math.cos((double)System.currentTimeMillis() / 40.0) * 4.0F;
         v8 = (float)Math.sin((double)System.currentTimeMillis() / 70.0) * 4.0F;
         if (canAttack) {

            if (!(aura.getTargetFov(target) >= (double)aura.fov.getValue().floatValue())) {
               break label15;
            }
         }

         v7 = 0.0F;
         v8 = 0.0F;
         v5 = wd.freeYaw;
         v6 = wd.freePitch;
      }

      xd.update(new Rotation(v5 + v7, v6 + v8), 30.0F, 30.0F, 30.0F, 30.0F, 1, 15, false);
   }

   
}
