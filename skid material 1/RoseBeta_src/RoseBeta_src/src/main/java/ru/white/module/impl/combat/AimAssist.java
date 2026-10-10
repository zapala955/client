package ru.white.module.impl.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import ru.white.utils.misc.Lp;
import ru.white.utils.misc.Np;
import ru.white.utils.misc.Rp;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventPlayerInput;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Aim Assist",
   desc = "Автоматически наводит прицел на врагов с симуляцией движений человека",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AimAssist extends Module {
   public SliderSetting neuroMaxMouse;
   public BooleanSetting onlyHelpToInput;
   public SliderSetting noiseStrength;
   private double f232;
   public ModeSetting mode = new ModeSetting(this, "Режим", "Обычный", "Нейро");
   public static LivingEntity target;
   public SliderSetting speed;
   private double f231;
   private double f238;
   private int f236;
   private double f233;
   public SliderSetting speed2;
   private long f234;
   public SliderSetting speedRandomness;
   public SliderSetting neuroHumanity;
   public BooleanSetting friend;
   public BooleanSetting useNoise;
   private long f235;
   public BooleanSetting player = new BooleanSetting(this, "Атаковать игроков", true);
   private float f227;
   private double f229;
   public SliderSetting neuroReaction;
   public BooleanSetting DynamicSpeed;
   private double f230;
   private long f237;
   public SliderSetting attackSpeedBoost;
   public BooleanSetting mobs;
   private float f228;
   private double f239;
   public BooleanSetting golyPlayer = new BooleanSetting(this, "Атаковать голых (без брони)", true);
   public SliderSetting noiseSpeed;

   @EventTarget
   public void onEvent(EventTick eventUpdate) {
      label35: {
         if (target != null) {
            if (this.m250(target)) {
               break label35;
            }

            
         }

         this.m249();
      }

      if (fg.get().typeRotation.is("TriggerBot") && fg.get().isEnabled()) {
         target = fg.target;
      }

      if (target == null || mc.field_1724 == null || mc.field_1687 == null) {
         target = null;
      }
   }

   public AimAssist() {
      this.mobs = new BooleanSetting(this, "Атаковать мобов", false);
      this.friend = new BooleanSetting(this, "Атаковать друзей", false);
      this.speed = new SliderSetting(this, "Плавность по горизонтали", 0.08F, 0.01F, 1.0F, 0.01F)
         .setVisible(() -> {

            return this.mode.is("Обычный");
         });
      this.speed2 = new SliderSetting(this, "Плавность по вертикали", 0.08F, 0.0F, 1.0F, 0.01F)
         .setVisible(() -> {
            Boolean result = this.mode.is("Обычный");

            return result;
         });
      this.onlyHelpToInput = new BooleanSetting(
            this,
            "Умная остановка (не дергать при наведении)",
            true
         )
         .setVisible(() -> {

            return this.mode.is("Обычный");
         });
      this.DynamicSpeed = new BooleanSetting(this, "Умная скорость (Реализм)", true)
         .setVisible(() -> {

            return this.mode.is("Обычный");
         });
      this.attackSpeedBoost = new SliderSetting(
            this, "Доводка при ударе (Множитель)", 1.5F, 1.0F, 3.0F, 0.1F
         )
         .setVisible(() -> {

            boolean result;
            if (this.mode.is("Обычный") && this.DynamicSpeed.getValue()) {
               result = true;
               
            } else {
               result = false;
            }

            return result;
         });
      this.speedRandomness = new SliderSetting(
            this, "Случайное изменение скорости", 0.02F, 0.0F, 0.1F, 0.01F
         )
         .setVisible(() -> this.mode.is("Обычный") && this.DynamicSpeed.getValue());
      this.useNoise = new BooleanSetting(this, "Дрожание прицела (Шум)", true).setVisible(() -> {
         int temp2 = v1 * (v1 + 1) % 2;

         if (temp2 != 0) {
            }

         return this.mode.is("Обычный");
      });
      this.noiseStrength = new SliderSetting(this, "Сила дрожания", 0.5F, 0.1F, 3.0F, 0.1F).setVisible(() -> {
         if (this.mode.is("Обычный")) {
            boolean result = this.useNoise.getValue();

            if (result) {
               return true;
            }
         }

         return false;
      });
      this.noiseSpeed = new SliderSetting(this, "Скорость дрожания", 0.3F, 0.1F, 1.0F, 0.05F)
         .setVisible(() -> {
            boolean result;
            if (this.mode.is("Обычный") && this.useNoise.getValue()) {
               result = true;

               
            } else {
               result = false;
            }

            return result;
         });
      this.neuroHumanity = new SliderSetting(this, "Нейро человечность", 0.65F, 0.0F, 1.0F, 0.05F)
         .setVisible(() -> {
            Boolean result = this.mode.is("Нейро");

            return result;
         });
      this.neuroReaction = new SliderSetting(this, "Нейро реакция", 120.0F, 20.0F, 300.0F, 10.0F)
         .setVisible(() -> {

            return this.mode.is("Нейро");
         });
      this.neuroMaxMouse = new SliderSetting(this, "Нейро скорость мыши", 8.0F, 1.0F, 24.0F, 0.5F)
         .setVisible(() -> {

            return this.mode.is("Нейро");
         });
      this.f227 = 0.0F;
      this.f228 = 0.0F;
      this.f229 = 0.0;
      this.f230 = 0.0;
      this.f231 = 0.0;
      this.f232 = 0.5;
      this.f233 = 0.0;
      this.f234 = 0L;
      this.f235 = 0L;
      this.f236 = -1;
      this.f237 = 0L;
      this.f238 = 1.0;
      this.f239 = 0.0;
   }

   @EventTarget
   public void onLook(EventPlayerInput event) {
      if (!this.mode.is("Нейро") || target == null || mc.field_1724 == null || mc.field_1687 == null) {
         this.m248();
      } else if (!this.m250(target)) {
         target = null;
         this.m248();
      } else if (this.onlyHelpToInput.getValue() && np.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), 3.0, target)) {
         this.f229 *= 0.55;
         this.f230 *= 0.55;
      } else {
         long v2 = System.currentTimeMillis();
         if (target.method_5628() != this.f236) {
            this.f236 = target.method_5628();
            this.f235 = v2
               + this.neuroReaction.getValue().longValue()
               + ThreadLocalRandom.current().nextLong(-5800952339338189044L + 5800952339338189064L, -2950404462317344781L + 2950404462317344871L);
            this.f234 = 0L;
            this.f237 = v2;

            this.f238 = ThreadLocalRandom.current().nextBoolean() ? 1.0 : -1.0;
            this.f239 = ThreadLocalRandom.current().nextDouble(0.55, 1.35);
            this.f229 *= 0.25;
            this.f230 *= 0.25;
         }

         this.m247(target, v2);
         if (v2 >= this.f235) {
            Vec3d v4 = target.method_73189()
               .method_1031(this.f231, (double)target.method_17682() * this.f232, this.f233)
               .method_1020(mc.field_1724.method_33571());
            float v5 = (float)Math.toDegrees(Math.atan2(-v4.field_1352, v4.field_1350));
            float v6 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v4.field_1351, Math.hypot(v4.field_1352, v4.field_1350))), -90.0, 90.0);
            double v7 = (double)MathHelper.method_15393(v5 - mc.field_1724.method_36454());
            double v9 = (double)(v6 - mc.field_1724.method_36455());
            double v11 = Math.hypot(v7, v9);
            if (v11 < 0.08) {
               this.f229 *= 0.62;
               this.f230 *= 0.62;
            } else {
               double v13 = (double)this.neuroHumanity.getValue().floatValue();
               double v15 = Math.max(1.0E-4, (double)lp.getGCDValue());
               double v17 = v7 / v15;
               double v19 = v9 / v15;
               double v21 = MathHelper.method_15350(v11 / 35.0, 0.18, 1.0);
               v21 = v21 * v21 * (3.0 - 2.0 * v21);
               double v23 = MathHelper.method_15350(v11 / 8.0, 0.22, 1.0);
               double v25 = (0.07 + 0.21 * v21) * v23 * (1.0 - v13 * 0.28);
               double v27 = (double)this.neuroMaxMouse.getValue().floatValue() * (0.45 + v21 * 0.75);
               if (mc.field_1724.field_6251 > 0.0F) {
                  v25 *= (double)this.attackSpeedBoost.getValue().floatValue();
                  v27 *= 1.25;
               }

               double v29 = (double)v2 / 1000.0;
               double v31 = this.useNoise.getValue() ? (double)this.noiseStrength.getValue().floatValue() * 0.035 * v13 : 0.0;
               double v33 = (Math.sin(v29 * 7.1) + Math.cos(v29 * 3.7)) * v31;

               double v35 = (Math.cos(v29 * 6.4) - Math.sin(v29 * 2.9)) * v31;
               double v37 = MathHelper.method_15350(v11 / 90.0, 0.0, 1.0) * v13;
               v17 += Math.signum(v7) * v37 * 0.65;
               v19 += Math.signum(v9) * v37 * 0.35;
               double v39 = MathHelper.method_15350((double)(v2 - this.f237) / 520.0, 0.0, 1.0);
               double v41 = Math.sin(v39 * Math.PI);
               double v43 = v41 * this.f239 * v13 * MathHelper.method_15350(v11 / 22.0, 0.0, 1.0);
               if (v11 > 0.001) {
                  v17 += -v9 / v11 * v43 * this.f238;
                  v19 += v7 / v11 * v43 * 0.42 * this.f238;
               }

               double v45 = 0.24 + v21 * 0.3 + Math.sin((double)(v2 - this.f237) / 95.0) * 0.035 * v13;
               this.f229 = this.f229 + (v17 * v25 - this.f229) * v45;
               this.f230 = this.f230 + (v19 * v25 - this.f230) * v45 * 0.88;
               this.f229 = this.f229
                  + ThreadLocalRandom.current().nextDouble((double)(-this.speedRandomness.getValue()), (double)this.speedRandomness.getValue().floatValue())
                     * v13;
               this.f230 = this.f230
                  + ThreadLocalRandom.current().nextDouble((double)(-this.speedRandomness.getValue()), (double)this.speedRandomness.getValue().floatValue())
                     * v13;
               double v47 = MathHelper.method_15350(this.f229 + v33, -v27, v27);
               double v49 = MathHelper.method_15350(this.f230 + v35, -v27, v27);
               event.setYaw(event.getYaw() + v47);
               event.setPitch(event.getPitch() + v49);
            }
         }
      }
   }

   private void m248() {
      this.f229 = 0.0;
      this.f230 = 0.0;
      this.f236 = -1;
      this.f234 = 0L;
      this.f235 = 0L;
      this.f237 = 0L;

      this.f239 = 0.0;
   }

   @EventTarget
   public void onEvent(EventReceivePacket eventUpdate) {
      if (!this.mode.is("Нейро")) {
         if (target != null && mc.field_1724 != null && mc.field_1687 != null) {

            if (!this.onlyHelpToInput.getValue() || !np.rayTraceEntity(mc.field_1724.method_36454(), mc.field_1724.method_36455(), 3.0, target)) {
               Vec3d v2 = rp.getBestVector3dOnEntityBox(target.method_5829(), false).method_1020(mc.field_1724.method_33571()).method_1029();
               if (this.useNoise.getValue()) {

                  float v3 = this.noiseStrength.getValue();
                  float v4 = this.noiseSpeed.getValue();
                  this.f227 = this.f227 + (float)ThreadLocalRandom.current().nextDouble((double)(-v4), (double)v4);
                  this.f228 = this.f228 + (float)ThreadLocalRandom.current().nextDouble((double)(-v4), (double)v4);
                  this.f227 = MathHelper.method_15363(this.f227, -v3, v3);
                  this.f228 = MathHelper.method_15363(this.f228, -v3, v3);
               } else {
                  this.f227 = 0.0F;
                  this.f228 = 0.0F;
               }

               float v15 = this.speed.getValue();
               float v17 = this.speed2.getValue();
               if (this.DynamicSpeed.getValue()) {
                  float v5 = (float)ThreadLocalRandom.current()
                     .nextDouble((double)(-this.speedRandomness.getValue()), (double)this.speedRandomness.getValue().floatValue());
                  v15 += v5;
                  v17 += v5;
                  if (mc.field_1724.field_6251 > 0.0F) {
                     v15 *= this.attackSpeedBoost.getValue();
                     v17 *= this.attackSpeedBoost.getValue();
                  }

                  v15 = MathHelper.method_15363(v15, 0.01F, 1.5F);
                  v17 = MathHelper.method_15363(v17, 0.0F, 1.5F);
               }

               float v19 = (float)Math.toDegrees(Math.atan2(-v2.field_1352, v2.field_1350)) + this.f227;
               float v6 = mc.field_1724.method_36454();
               float v7 = MathHelper.method_15393(v19 - v6);
               float v8 = v6 + v7 * v15;
               mc.field_1724.method_36456(lp.applyGCD(v8, v6));
               float v9 = (float)MathHelper.method_15350(
                     -Math.toDegrees(Math.atan2(v2.field_1351, Math.hypot(v2.field_1352, v2.field_1350))), -90.0, 90.0
                  )
                  + this.f228;
               float v10 = mc.field_1724.method_36455();
               float v11 = MathHelper.method_15393(v9 - v10);
               float v12 = v10 + v11 * v17;
               mc.field_1724.method_36457(lp.applyGCD(v12, v10));
            }
         }
      }
   }

   private void m247(LivingEntity entity, long now) {
      if (now >= this.f234) {
         double v4 = (double)this.neuroHumanity.getValue().floatValue();
         double v6 = (double)entity.method_17681() * 0.5;
         this.f231 = ThreadLocalRandom.current().nextDouble(-v6, v6) * 0.55 * v4;
         this.f232 = ThreadLocalRandom.current().nextDouble(0.38, 0.82);

         double v11 = ThreadLocalRandom.current().nextDouble(-v6, v6);

         this.f233 = v11 * 0.55 * v4;
         this.f234 = now + ThreadLocalRandom.current().nextLong(-2063435339196867069L + 2063435339196867209L, -7764516939945135394L + 7764516939945135754L);
      }
   }

   private void m249() {
      LivingEntity v1 = null;
      double v2 = Double.MAX_VALUE;

      Vec3d v4 = mc.field_1724.method_33571();
      Vec3d v5 = mc.field_1724.method_5828(1.0F).method_1029();

      for (Object v16 : mc.field_1687.method_18112()) {

         Entity v7 = (Entity)v16;
         boolean v17 = v7 instanceof LivingEntity;

         if (v17) {
            LivingEntity v8 = (LivingEntity)v7;
            if (this.m250(v8)) {
               Vec3d v9 = v8.method_73189().method_1031(0.0, (double)v8.method_17682() * 0.5, 0.0);
               Vec3d v10 = v9.method_1020(v4).method_1029();
               double v11 = Math.acos(MathHelper.method_15350(v5.method_1026(v10), -1.0, 1.0));
               if (v11 < v2) {
                  v2 = v11;
                  v1 = v8;
               }
            }
         }
      }

      target = v1;
   }

   private boolean m250(LivingEntity entity) {
      if (entity instanceof ClientPlayerEntity) {
         return false;
      } else if (mc.field_1724.method_5739(entity) > 6.0F) {
         return false;
      } else if (!mc.field_1724.method_6057(entity)) {
         return false;
      } else {
         if (entity instanceof PlayerEntity) {

            PlayerEntity v2 = (PlayerEntity)entity;
            if (Client.get().friendManager().isFriend(v2.method_5477().getString()) && !this.friend.getValue()) {
               return false;
            }
         }

         if (entity instanceof PlayerEntity && !this.player.getValue()) {
            return false;
         } else if (entity instanceof PlayerEntity && entity.method_18396() <= 0.0F && !this.golyPlayer.getValue()) {
            return false;
         } else if (entity instanceof PlayerEntity && ((PlayerEntity)entity).method_68878()) {
            return false;
         } else if ((entity instanceof Monster || entity instanceof SlimeEntity || entity instanceof VillagerEntity || entity instanceof AnimalEntity)
            && !this.mobs.getValue()) {
            return false;
         } else {
            if (!entity.method_5655() && entity.method_5805()) {
               boolean v6 = entity instanceof ArmorStandEntity;

               if (!v6) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   
}
