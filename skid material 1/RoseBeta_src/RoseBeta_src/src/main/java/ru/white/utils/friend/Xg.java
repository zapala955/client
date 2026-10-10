package ru.white.utils.friend;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.misc.Kp;
import ru.white.utils.misc.Np;
import ru.white.utils.render.gl.Vg;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
public class Xg implements vg {
   private float f363;
   private float f362 = 12.0F;

   public Xg() {
      this.f363 = 0.0F;
   }

   @Override
   public void onRotation(fg aura, LivingEntity target, float[] ranges, boolean canAttack) {
      Vec3d v5 = kp.getVector2(target);
      float v6 = (float)Math.toDegrees(Math.atan2(-v5.field_1352, v5.field_1350));

      float v7 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v5.field_1351, Math.hypot(v5.field_1352, v5.field_1350))), -90.0, 90.0);
      Rotation result = new Rotation;

      result./* $VF: Unable to resugar constructor */<init>(v6, v7);
      Rotation v8 = result;
      float v9 = fq.random1(480.0F, 625.0F);
      float v10 = fq.random1(455.0F, 555.0F);
      float v11 = 0.5F;
      float v15 = mc.field_1724.method_36454();
      float v17 = mc.field_1724.method_36455();
      double temp2 = (double)aura.attackRange.getValue().floatValue();

      if (np.rayTraceEntity(v15, v17, temp2, target)) {
         v10 = 0.0F;
         v9 = fq.randomLerp(450.0F, 700.0F);
      }

      xd.update(v8, v9, v10, 425.0F, 425.0F, 0, 1, false);
   }

   
}
