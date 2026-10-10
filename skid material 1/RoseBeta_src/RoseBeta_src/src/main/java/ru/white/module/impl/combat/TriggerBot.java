package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.misc.Kp;
import ru.white.utils.misc.Pp;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "TriggerBot",
   desc = "Автоматический атакует цель на которую вы навились",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class TriggerBot extends Module {
   public BooleanSetting player;
   public BooleanSetting smartCrit;
   public BooleanSetting hitOfBlock;
   public static LivingEntity targets = null;
   public BooleanSetting golyPlayer;
   public ModeSetting typeSprint;
   public BooleanSetting friend;
   public SliderSetting preRange;
   public SliderSetting attackRange = new SliderSetting(this, "Радиус атаки", 3.0F, 2.5F, 5.0F, 0.1F);
   public BooleanSetting mobs;
   public BooleanSetting nohitead;

   public TriggerBot() {
      this.preRange = new SliderSetting(this, "Радиус обнаружения", 1.0F, 0.0F, 3.0F, 0.1F);
      this.player = new BooleanSetting(this, "Атаковать игроков", true);
      this.golyPlayer = new BooleanSetting(this, "Атаковать голых", true);
      this.mobs = new BooleanSetting(this, "Атаковать мобов", false);
      this.friend = new BooleanSetting(this, "Атаковать друзей", false);
      this.typeSprint = new ModeSetting(this, "Тип спринта", "Packet", "Silent", "Legit");
      this.smartCrit = new BooleanSetting(this, "Умные криты", false);
      this.hitOfBlock = new BooleanSetting(this, "Бить через блоки", false);
      this.nohitead = new BooleanSetting(this, "Не бить при еде", false);
   }

   @EventTarget
   public void onUpdate(EventTick eventUpdate) {
      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            if (this.nohitead.getValue()) {

               boolean v6 = mc.field_1724.method_6115();

               if (v6) {
                  return;
               }
            }

            LivingEntity v2 = this.resolveTarget();
            if (v2 == null) {
               return;
            }

            if (!this.isValidTarget(v2)) {
               return;
            }

            targets = v2;
            this.attackEntity(v2);
            return;
         }

         
      }
   }

   public float[] getRanges() {
      float[] result = new float[2];

      result[0] = this.attackRange.getValue();
      result[1] = this.preRange.getValue();
      return result;
   }

   private boolean isValidTarget(LivingEntity target) {
      if (target instanceof PlayerEntity v2) {
         if (!this.player.getValue()) {
            return false;
         }

         Client result = Client.get();

         if (result.friendManager().isFriend(v2.method_5477().getString()) && !this.friend.getValue()) {
            return false;
         }

         if (v2.method_18396() <= 0.0F && !this.golyPlayer.getValue()) {
            return false;
         }

         if (v2.method_68878()) {
            return false;
         }
      }

      return !(target instanceof MobEntity) || this.mobs.getValue();
   }

   public void attackEntity(LivingEntity target) {
      if (!(kp.getStrictDistance(target) >= (double)this.attackRange.getValue().floatValue())) {
         float[] result = new float[]{this.attackRange.getValue(), this.preRange.getValue(), 0.0F};

         result[2] = this.attackRange.getValue() + this.preRange.getValue();
         float[] v2 = result;
         pp.antiMissesHittingUpdate(target, false, true, false);
         boolean v3 = pp.shouldAttack(target, true, true, true, 0L, v2);
         if (v3) {
            Runnable[] v4 = pp.hitShieldBreakTaskForUse(target, true);
            Runnable[] v5 = pp.resetShieldSilentTaskForUse(true);
            Runnable[] v6 = pp.skipSilentSprintingTaskForUse(this.typeSprint.is("Packet") || this.typeSprint.is("Silent"));
            Runnable v7 = () -> {
               v6[0].run();

               v5[0].run();
               v4[0].run();
               
            };
            Runnable v8 = () -> {

               v4[1].run();
               v5[1].run();

               v6[1].run();
            };
            pp.useEntity(target, v7, v8, Hand.field_5808, false);
         }
      }
   }

   private LivingEntity resolveTarget() {
      if (!this.hitOfBlock.getValue()) {
         if (mc.field_1765 instanceof EntityHitResult result) {

            EntityHitResult v20 = result;
            return v20.method_17782() instanceof LivingEntity v22 ? v22 : null;
         } else {
            return null;
         }
      } else {
         Vec3d v1 = mc.field_1724.method_33571();
         Vec3d v2 = mc.field_1724.method_5828(1.0F).method_1029();
         LivingEntity v3 = null;
         double v4 = Double.MAX_VALUE;
         double v6 = Math.toRadians(10.0);

         for (Entity v9 : mc.field_1687.method_18112()) {
            if (v9 instanceof LivingEntity) {
               LivingEntity v10 = (LivingEntity)v9;
               if (!(v9 instanceof ClientPlayerEntity) && v9.method_5805()) {
                  double v11 = (double)mc.field_1724.method_5739(v9);
                  if (!(v11 > (double)this.attackRange.getValue().floatValue())) {
                     Vec3d v13 = v9.method_33571().method_1020(v1).method_1029();
                     double v14 = MathHelper.method_15350(v2.method_1026(v13), -1.0, 1.0);
                     double v16 = Math.acos(v14);
                     if (v16 <= v6 && v16 < v4) {
                        v4 = v16;
                        v3 = v10;
                     }
                  }
               }
            }
         }

         return v3;
      }
   }

   
}
