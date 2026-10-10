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
public class Bh implements vg {
   private float f388;
   private float f389;
   private float f387 = 24.0F;
   private float f390;

   public Bh() {
      this.f388 = 6.0F;
      this.f389 = 0.0F;
      this.f390 = 0.0F;
   }

   @Override
   public void onRotation(fg aura, LivingEntity target, float[] ranges, boolean canAttack) {
      if (mc.field_1724 != null && target != null) {
         Vec3d v5 = kp.getVector2(target);

         float v6 = (float)(Math.cos((double)System.currentTimeMillis() / 200.0) * (double)fq.randomLerp(6.0F, 8.0F));
         float v7 = (float)(Math.sin((double)System.currentTimeMillis() / 200.0) * (double)fq.randomLerp(6.0F, 8.0F));

         float v8 = fq.randomLerp(25.0F, 35.0F);
         float v9 = fq.randomLerp(4.0F, 8.0F);
         float v10 = (float)Math.toDegrees(Math.atan2(-v5.field_1352, v5.field_1350));
         float v11 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v5.field_1351, Math.hypot(v5.field_1352, v5.field_1350))), -90.0, 90.0);
         float v12 = 0.3F;
         if (np.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), 5.0, target)) {
            v8 = 0.0F;
            v9 = 0.0F;
         }

         this.f387 = this.f387 + (v8 - this.f387) * v12;

         this.f388 = this.f388 + (v9 - this.f388) * v12;
         xd.update(new Rotation(v10, v11), this.f387, this.f388, fq.random(360.0, 390.0), fq.random(360.0, 390.0), (int)fq.random(3.0, 5.0), 1, false);
      }
   }

   
}
