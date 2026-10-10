package ru.white.module.impl.movement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.StopWatch;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.media.Jr;
import ru.white.utils.media.Kr;
import ru.white.utils.media.Or;
import ru.white.event.impl.EventMove;
import ru.white.event.impl.EventPlayerLook;
import ru.white.event.impl.EventStep;

@ModuleAnnotation(
   name = "Speed",
   desc = "Увеличивает скорость игрока",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Speed extends Module {
   public ModeSetting type = new ModeSetting(this, "Режим", "Vanilla", "Meta");
   private int groundTicks;
   public SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.3F, 2.0F, 0.1F).setVisible(() -> {

      return this.type.is("Vanilla");
   });
   private int ticks = 0;
   public StopWatch timerUtil;
   public float tick;

   private boolean canUseRW() {
      return mc.field_1724 != null
         && mc.field_1687 != null
         && mc.field_1724.field_3944 != null
         && kr.isMoving()
         && !mc.field_1724.method_5765()
         && !mc.field_1724.method_31549().field_7479;
   }

   @Override
   public void onDisable() {
      RenderTickCounter v2 = mc.method_61966();

      if (v2 instanceof jr v1) {
         v1.setSpeed(1.0F);
      }

      this.resetRWState(true);

      or.tick = 20.0F;
      super.onDisable();
      
   }

   private void handleRW() {
      if (mc.method_61966() instanceof jr v1) {
         long v11 = this.timerUtil.getTime();
         float v5;
         if (v11 < -1493469864561042131L + 1493469864561042231L) {
            boolean v4 = true;
            v5 = 1.15F;
         } else {
            long v16;
            int result = (v16 = v11 - (3981225954516448183L + -3981225954516447683L)) == 0L ? 0 : (v16 < 0L ? -1 : 1);

            if (result < 0) {
               v5 = 1.4F;
            } else {
               long v17;
               result = (v17 = v11 - (-9174008796733353585L + 9174008796733354105L)) == 0L ? 0 : (v17 < 0L ? -1 : 1);

               if (result < 0) {
                  v5 = 1.6F;
               } else if (v11 < -6993455270303521336L + 6993455270303521876L) {
                  boolean v12 = false;
                  v5 = 0.25F;
               } else {
                  this.timerUtil.reset();
                  boolean v13 = true;
                  v5 = 0.5F;
               }
            }
         }

         v1.setSpeed(v5);
         double v6 = 0.03;
      }
   }

   public Speed() {
      this.groundTicks = 0;
      this.tick = 1.0F;
      this.timerUtil = new StopWatch();
   }

   @Override
   public void onEnable() {
      this.resetRWState(true);
      super.onEnable();

      
   }

   @EventTarget
   public void onEvent(EventStep e) {
      if (this.type.is("RW")) {

         
      }
   }

   private void resetRWState(boolean resetTimer) {
      this.ticks = 0;

      this.groundTicks = 0;

      if (resetTimer) {
         mc.field_1724.field_28627 = 0.0F;
      }
   }

   @EventTarget
   public void onEvent(EventPlayerLook e) {
   }

   @EventTarget
   public void onEvent(EventMove event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.type.is("Grim Box") && !mc.field_1724.method_5869()) {
            double v2 = 0.02F;
            if (v2 <= 0.0) {
               return;
            }

            Entity v4 = null;
            double v5 = Double.MAX_VALUE;
            double v7 = 0.1F;

            for (Entity v10 : mc.field_1687.method_18112()) {
               if (v10 != mc.field_1724) {
                  if (v10 == fg.target) {
                     double v11 = v10.method_23317() - mc.field_1724.method_23317();
                     double v13 = v10.method_23321() - mc.field_1724.method_23321();
                     double v15 = v11 * v11 + v13 * v13;
                     if (v15 <= v7 && v15 < v5) {
                        v5 = v15;

                        v4 = v10;
                     }
                  }

                  if (v4 != null) {
                     double[] v26 = this.getDirectionToPoint(mc.field_1724.method_73189(), v4.method_73189(), v2);
                     mc.field_1724.method_5762(v26[0], 0.0, v26[1]);
                  }
               }
            }
         }

         if (this.type.is("Meta")) {
            if (mc.field_1724.method_6059(StatusEffects.field_5909)) {
               return;
            }

            String v20 = mc.field_1724.method_6079().method_7964().getString();
            byte[] v3 = v20.getBytes(StandardCharsets.UTF_8);
            String v21 = Arrays.toString(v3);
            boolean v22 = v21.equals("[-48, -88, -48, -80, -47, -128, 32, 75, 73, 78, 71]");
            boolean v6 = v21.equals(
               "[-48, -94, -48, -72, -48, -77, -47, -128, -48, -72, -48, -67, -48, -67, -48, -80, -47, -113, 32, -48, -77, -48, -66, -48, -69, -48, -66, -48, -78, -48, -80]"
            );
            StatusEffectInstance v23 = mc.field_1724.method_6112(StatusEffects.field_5904);

            boolean v8 = mc.field_1724.method_6059(StatusEffects.field_5904);
            boolean v24 = mc.field_1724.method_24828();
            float v25 = (float)mc.field_1724.field_6017;
            if (v8 && v23 != null && v23.method_5578() == 2 && v25 <= 0.2F && !v24) {
               kr.setSpeed(v22 ? 0.72F : (v6 ? 0.7F : 0.58F));
            } else {
               label186: {
                  if (v8) {

                     if (v23 != null && v23.method_5578() == 2 && v24) {
                        kr.setSpeed(v22 ? 0.5 : (v6 ? 0.49F : 0.42F));
                        break label186;
                     }
                  }

                  if (v8 && v23 != null && v23.method_5578() == 3 && v25 <= 0.2F && !v24) {
                     kr.setSpeed(0.7F);
                  } else if (v8 && v23 != null && v23.method_5578() == 3 && v24) {
                     kr.setSpeed(0.49F);
                  } else if (v8 && v23 != null && v23.method_5578() == 1 && v25 <= 0.2F && !v24) {
                     kr.setSpeed(0.52F);
                  } else if (v8 && v23 != null && v23.method_5578() == 1 && v24) {
                     kr.setSpeed(0.36F);
                  } else if (v25 <= 0.2F && !v24 || v25 <= 0.2F && v8) {
                     kr.setSpeed(0.36F);
                  }
               }
            }
         }

         if (this.type.is("Vanilla") && !mc.field_1724.method_5869()) {
            if (mc.field_1724.method_24828()) {
               kr.setSpeed(0.3F);
            } else {
               kr.setSpeed((double)this.speed.getValue().floatValue());
            }
         }

         if (this.type.is("RW")) {
            this.handleRW();
         }
      }
   }

   private double[] getDirectionToPoint(Vec3d from, Vec3d to, double spd) {
      double v5 = to.field_1352 - from.field_1352;
      double v7 = to.field_1350 - from.field_1350;
      double v9 = Math.sqrt(v5 * v5 + v7 * v7);
      if (v9 == 0.0) {

         return new double[]{0.0, 0.0};
      } else {
         double[] result = new double[2];

         result[0] = v5 / v9 * spd;

         result[1] = v7 / v9 * spd;
         return result;
      }
   }

   
}
