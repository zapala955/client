package ru.white.module.impl.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.utils.misc.Lp;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Aim Bot",
   desc = "Автоприцел для лука и трезубца с предиктом позиции цели и компенсацией гравитации",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AimBot extends Module {
   public BooleanSetting onlyWhenUsing = new BooleanSetting(this, "Только при зарядке", true);
   public BooleanSetting friendsSet;
   public SliderSetting predictMult;
   public SliderSetting smoothH;
   public SliderSetting range;
   public BooleanSetting playersSet = new BooleanSetting(this, "Атаковать игроков", true);
   private final Map<UUID, Vec3d[]> f309;
   public SliderSetting smoothV;
   public BooleanSetting mobsSet = new BooleanSetting(this, "Атаковать мобов", false);
   private LivingEntity f310;

   private boolean m394() {
      if (mc.field_1724 != null && mc.field_1724.method_6115()) {
         Item v1 = mc.field_1724.method_6030().method_7909();
         if (!(v1 instanceof BowItem)) {

            if (!(v1 instanceof CrossbowItem) && !(v1 instanceof TridentItem)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private double m388(double fromY, double ux, double uz, float speed, double elevRad, double targetD) {
      double v12 = Math.cos(elevRad);
      double v14 = Math.sin(elevRad);
      double v16 = ux * v12 * (double)speed;
      double v18 = v14 * (double)speed;
      double v20 = uz * v12 * (double)speed;
      double v22 = 0.0;
      double v24 = fromY;
      double v26 = 0.0;
      double v28 = 0.0;

      while (v30 < 120) {
         double v31 = v22 + v16;
         double v33 = v24 + v18;
         double v35 = v26 + v20;
         v18 -= 0.05;

         v16 *= 0.99;
         v18 *= 0.99;
         v20 *= 0.99;
         double v37 = Math.sqrt(v31 * v31 + v35 * v35);
         if (v37 >= targetD) {

            double v39 = v28 < v37 ? (targetD - v28) / (v37 - v28) : 1.0;
            return v24 + (v33 - v24) * v39;
         }

         v22 = v31;
         v24 = v33;
         v26 = v35;
         v28 = v37;
         if (v18 < -3.0 && v37 < v37 + 0.01) {
            break;
         }

         v30++;
         
      }

      return Double.MIN_VALUE;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (Entity v3 : mc.field_1687.method_18112()) {
            if (v3 instanceof LivingEntity && !(v3 instanceof ClientPlayerEntity)) {
               UUID v4 = v3.method_5667();
               Vec3d v5 = v3.method_73189();
               Vec3d[] v6 = this.f309.computeIfAbsent(v4, k -> {
                  Vec3d[] result = new Vec3d[]{v5, null, null};

                  result[1] = v5;
                  result[2] = v5;
                  return result;
               });
               v6[2] = v6[1];
               v6[1] = v6[0];

               v6[0] = v5;
            }
         }

         if (this.onlyWhenUsing.getValue() && !this.m394()) {
            this.f310 = null;
         } else {
            if (this.f310 == null || !this.m396(this.f310)) {
               this.m395();
            }
         }
      }
   }

   public AimBot() {
      this.friendsSet = new BooleanSetting(this, "Атаковать друзей", false);
      this.range = new SliderSetting(this, "Дальность", 30.0F, 5.0F, 80.0F, 1.0F);
      this.smoothH = new SliderSetting(this, "Плавность горизонталь", 0.15F, 0.01F, 1.0F, 0.01F);
      this.smoothV = new SliderSetting(this, "Плавность вертикаль", 0.15F, 0.01F, 1.0F, 0.01F);
      this.predictMult = new SliderSetting(this, "Сила предикта", 1.0F, 0.0F, 2.0F, 0.05F);
      this.f309 = new HashMap<>();
   }

   private Vec3d m386(Vec3d from, Vec3d targetBase, Vec3d vel, float speed) {
      Vec3d v5 = targetBase;
      float result = this.predictMult.getValue();

      float v6 = result;

      for (int v7 = 0; v7 < 10; v7++) {
         pg.qg v8 = this.m387(from, v5, speed);
         if (v8 == null) {
            break;
         }

         double temp2 = (double)((float)v8.ticks * v6);
         int temp4 = v10 + 1;

         Vec3d v9 = targetBase.method_1019(vel.method_1021(temp2));
         if (v9.method_1025(v5) < 0.001) {
            break;
         }

         v5 = v9;
      }

      return v5;
   }

   @EventTarget
   public void onDisplay(EventReceivePacket event) {
      if (mc.field_1724 != null) {

         if (mc.field_1687 != null && this.f310 != null) {
            if (this.onlyWhenUsing.getValue() && !this.m394()) {
               return;
            }

            if (!this.m396(this.f310)) {
               this.f310 = null;
               return;
            }

            float v2 = this.m393();
            if (v2 <= 0.0F) {
               return;
            }

            Vec3d v3 = mc.field_1724.method_33571();
            Vec3d v4 = this.f310.method_73189();
            Vec3d v5 = v4.method_1031(0.0, (double)this.f310.method_17682() * 0.2, 0.0);
            Vec3d v6 = this.m392(this.f310);
            Vec3d v7 = this.m386(v3, v5, v6, v2);
            v7 = this.m390(v3, v7, v2);
            float v8 = this.m391(v3, v7);
            pg.qg v9 = this.m387(v3, v7, v2);
            if (v9 == null) {
               return;
            }

            float v10 = mc.field_1724.method_36454();
            float v11 = mc.field_1724.method_36455();
            float v18 = MathHelper.method_15393(v8 - v10);

            float v12 = v18;
            float v13 = v9.pitch - v11;
            Rotation v14 = new Rotation(
               lp.applyGCD(v10 + v12 * this.smoothH.getValue(), v10), lp.applyGCD(v11 + v13 * this.smoothV.getValue(), v11)
            );
            xd.update(v14, 500.0F, 500.0F, 0, 20);
            return;
         }
      }
   }

   private float m391(Vec3d from, Vec3d to) {
      double result = -(to.field_1352 - from.field_1352);

      return (float)Math.toDegrees(Math.atan2(result, to.field_1350 - from.field_1350));
   }

   private boolean m396(LivingEntity entity) {
      if (entity instanceof ClientPlayerEntity) {
         return false;
      } else if (!entity.method_5805() || entity.method_5655()) {
         return false;
      } else if (entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity.method_5858(mc.field_1724) > (double)(this.range.getValue() * this.range.getValue())) {
         return false;
      } else {
         if (entity instanceof PlayerEntity v2) {
            if (!this.playersSet.getValue()) {
               return false;
            }

            if (v2.method_68878() || v2.method_7325()) {
               return false;
            }

            if (!this.friendsSet.getValue() && Client.get().friendManager().isFriend(v2.method_5477().getString())) {
               return false;
            }
         } else {
            if (!(entity instanceof Monster) && !(entity instanceof SlimeEntity) && !(entity instanceof AnimalEntity)) {
               return false;
            }

            if (!this.mobsSet.getValue()) {

               return false;
            }
         }

         return true;
      }
   }

   @Override
   public void onDisable() {
      this.f310 = null;
      this.f309.clear();
      
   }

   private Vec3d m390(Vec3d from, Vec3d to, float speed) {
      pg.qg v4 = this.m387(from, to, speed);
      if (v4 == null) {

         return to;
      } else {
         int v5 = v4.ticks;
         double v6 = v5 > 0 ? (1.0 - Math.pow(0.99, (double)v5)) / 0.01 : 0.0;
         Vec3d v8 = mc.field_1724.method_18798();
         double v9 = v8.field_1352 * v6;
         double v11 = mc.field_1724.method_24828() ? 0.0 : v8.field_1351 * v6;

         double v13 = v8.field_1350 * v6;
         return to.method_1023(v9, v11, v13);
      }
   }

   private float m393() {
      if (mc.field_1724 != null && mc.field_1724.method_6115()) {
         Item v1 = mc.field_1724.method_6030().method_7909();
         int v2 = mc.field_1724.method_6048();
         if (v1 instanceof BowItem) {
            float result = (float)v2;

            result /= 20.0F;

            float v3 = result;
            v3 = (v3 * v3 + v3 * 2.0F) / 3.0F;
            v3 = Math.min(v3, 1.0F);
            return Math.max(v3, 0.2F) * 3.0F;
         } else if (v1 instanceof CrossbowItem) {
            return 3.15F;
         } else {
            return v1 instanceof TridentItem ? 2.5F : 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private Vec3d m392(LivingEntity entity) {

      Vec3d[] v2 = this.f309.get(entity.method_5667());
      if (v2 == null) {
         return entity.method_18798();
      } else {

         Vec3d result = v2[0];

         return result.method_1020(v2[2]).method_1021(0.5);
      }
   }

   private int m389(double ux, double uz, float speed, double elevRad, double targetD) {
      double result = Math.cos(elevRad);

      double v10 = result;
      double v12 = Math.sin(elevRad);
      double v14 = ux * v10 * (double)speed;
      double v28 = (double)speed;

      double v16 = v12 * v28;
      double v18 = uz * v10 * (double)speed;
      double v20 = 0.0;
      double v22 = 0.0;

      for (int v24 = 1; v24 <= 120; v24++) {
         v20 += v14;
         v22 += v18;
         v16 -= 0.05;
         v14 *= 0.99;
         v16 *= 0.99;
         v18 *= 0.99;
         if (Math.sqrt(v20 * v20 + v22 * v22) >= targetD) {
            return v24;
         }
      }

      return 40;
   }

   private void m395() {
      LivingEntity v1 = null;
      double v2 = Double.MAX_VALUE;
      Vec3d result = mc.field_1724.method_33571();

      Vec3d v4 = result;
      Vec3d v5 = mc.field_1724.method_5828(1.0F).method_1029();

      for (Entity v7 : mc.field_1687.method_18112()) {
         if (v7 instanceof LivingEntity) {
            LivingEntity v8 = (LivingEntity)v7;
            if (this.m396(v8)) {
               Vec3d v9 = v8.method_73189().method_1031(0.0, (double)v8.method_17682() * 0.2, 0.0);
               double v10 = Math.acos(MathHelper.method_15350(v5.method_1026(v9.method_1020(v4).method_1029()), -1.0, 1.0));

               if (v10 < v2) {
                  v2 = v10;
                  v1 = v8;
               }
            }
         }
      }

      this.f310 = v1;
   }

   private pg.qg m387(Vec3d from, Vec3d to, float speed) {
      double v4 = to.field_1352 - from.field_1352;
      double v6 = to.field_1351 - from.field_1351;
      double v8 = to.field_1350 - from.field_1350;
      double v10 = Math.sqrt(v4 * v4 + v8 * v8);
      double v12 = v10 > 0.001 ? v4 / v10 : 0.0;
      double v14 = v10 > 0.001 ? v8 / v10 : 1.0;
      if (v10 < 0.3) {
         float v27 = v6 > 0.0 ? -89.0F : 89.0F;
         return new pg.qg(v27, 5);
      } else {
         double v16 = -1.5607963267948965;
         double v18 = 1.5607963267948965;

         if (this.m388(from.field_1351, v12, v14, speed, Math.toRadians(75.0), v10) == Double.MIN_VALUE) {
            return null;
         } else {
            for (int v20 = 0; v20 < 40; v20++) {
               double v21 = (v16 + v18) * 0.5;
               double v23 = this.m388(from.field_1351, v12, v14, speed, v21, v10);
               if (v23 == Double.MIN_VALUE) {
                  v16 = v21;
               } else if (v23 < to.field_1351) {
                  v16 = v21;
               } else {
                  v18 = v21;
               }
            }

            double v28 = (v16 + v18) * 0.5;
            double v22 = this.m388(from.field_1351, v12, v14, speed, v28, v10);
            if (v22 != Double.MIN_VALUE && !(Math.abs(v22 - to.field_1351) > 2.0)) {
               float v24 = MathHelper.method_15363((float)(-Math.toDegrees(v28)), -90.0F, 90.0F);
               int v25 = this.m389(v12, v14, speed, v28, v10);
               return new pg.qg(v24, v25);
            } else {
               return null;
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class qg {
      final float pitch;
      final int ticks;

      qg(float pitch, int ticks) {
         this.pitch = pitch;
         this.ticks = ticks;
      }

      
   }
}
