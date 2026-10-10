package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleHelper;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.misc.Rp;
import ru.white.utils.PacketUtil;
import ru.white.event.Event;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Hit Boxes",
   desc = "Увеличивает хит бокс игрока",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class HitBoxes extends Module {
   public SliderSetting size = new SliderSetting(this, "Размер хит бокса", 0.3F, 0.1F, 1.0F, 0.1F);
   public static LivingEntity pendingTarget = null;
   public static int attackTimer = 0;
   public ModeSetting type = new ModeSetting(this, "Режим", "Обычный", "Ротация");
   public ModeSetting typeRot = new ModeSetting(this, "Режим ротации", "Постоянный", "При ударе").setVisible(() -> {
      boolean result = this.type.is("Ротация");

      return result;
   });

   public void onHitBox(LivingEntity target) {
      if (this.typeRot.is("Постоянный")) {
         Vec3d v2 = rp.getBestVector3dOnEntityBox(target.method_5829(), false).method_1020(mc.field_1724.method_33571());
         float v3 = (float)Math.toDegrees(Math.atan2(-v2.field_1352, v2.field_1350));
         float v4 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v2.field_1351, Math.hypot(v2.field_1352, v2.field_1350))), -90.0, 90.0);
         float v5 = (float)Math.cos((double)System.currentTimeMillis() / 40.0);
         float v6 = (float)Math.sin((double)System.currentTimeMillis() / 70.0);
         float v7 = v5 * fq.randomLerp(1.0F, 2.0F);
         float v8 = v6 * fq.randomLerp(1.0F, 2.0F);
         Rotation v9 = new Rotation(v3 + v7, wd.freePitch + v8);
         xd.update(
            v9,
            fq.randomLerp(12.0F, 25.0F),
            fq.randomLerp(22.0F, 25.0F),
            fq.randomLerp(24.0F, 26.0F),
            fq.randomLerp(24.0F, 26.0F),
            0,
            15,
            false
         );
      }

      if (this.typeRot.is("При ударе")) {

         Vec3d v12 = target.method_73189()
            .method_1031(0.0, (double)(target.method_17682() / 2.0F), 0.0)
            .method_1020(mc.field_1724.method_33571())
            .method_1029();
         float v13 = (float)Math.toDegrees(Math.atan2(-v12.field_1352, v12.field_1350));
         double result = -Math.toDegrees(Math.atan2(v12.field_1351, Math.hypot(v12.field_1352, v12.field_1350)));

         float v14 = (float)MathHelper.method_15350(result, -90.0, 90.0);
         float v15 = (float)Math.cos((double)System.currentTimeMillis() / 180.0);
         float v16 = (float)Math.sin((double)System.currentTimeMillis() / 180.0);
         float v17 = v15 * fq.randomLerp(2.0F, 4.0F);
         float v18 = v16 * fq.randomLerp(2.0F, 4.0F);
         Rotation v19 = new Rotation(v13 + v17, v14 + v18);
         xd.update(
            v19,
            fq.randomLerp(152.0F, 255.0F),
            fq.randomLerp(125.0F, 255.0F),
            fq.randomLerp(152.0F, 255.0F),
            fq.randomLerp(152.0F, 255.0F),
            0,
            15,
            false
         );
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.type.is("Ротация")) {
         if (this.typeRot.is("Постоянный")) {
            if (!(mc.field_1765 instanceof EntityHitResult v3)) {
               pendingTarget = null;
               return;
            }

            if (!(v3.method_17782() instanceof LivingEntity v5) || mc.field_1724.method_5739(v5) > 4.0F) {
               pendingTarget = null;
               return;
            }

            if (v5 instanceof PlayerEntity v6) {
               pendingTarget = v6;
               this.onHitBox(v6);
            } else {
               pendingTarget = null;
            }
         }

         label45: {
            if (this.typeRot.is("При ударе") && pendingTarget != null) {
               if (!pendingTarget.method_5805()) {
                  break label45;
               }

               if (mc.field_1724.method_5739(pendingTarget) > 4.0F) {
                  break label45;
               }

               this.onHitBox(pendingTarget);

               attackTimer++;
               if (attackTimer >= 3) {
                  pendingTarget = null;
                  attackTimer = 0;
               }
            }

            return;
         }

         pendingTarget = null;
      }
   }

   @EventTarget
   public void onEvent(Event event) {
      if (this.typeRot.is("При ударе")) {
         pendingTarget = (LivingEntity)event.getTarget();

         int temp1 = v2 + 1;

         attackTimer = 0;
      }
   }

   public static lg get() {
      lg result = uq.get(lg.class);

      return result;
   }

   
}
