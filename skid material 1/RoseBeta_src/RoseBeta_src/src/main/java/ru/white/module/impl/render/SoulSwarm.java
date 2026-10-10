package ru.white.module.impl.render;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Soul Swarm",
   desc = "Живой рой светлячков: боится врагов, слетается к цели",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class SoulSwarm extends Module {
   private final float[] f774;
   private final float[] f771;
   private final float[] f777;
   private final float[] f772;
   public final BooleanSetting scareMobs;
   private final BufferAllocator f781;
   private static final int MAX = 320;
   private int f778;
   public final SliderSetting dotSize;
   public final SliderSetting count = new SliderSetting(this, "Количество", 200.0F, 80.0F, 320.0F, 10.0F);
   private final float[] f770;
   private float f779;
   private final float[] f775;
   public final SliderSetting speed;
   private long f780;
   private final float[] f769;
   public final SliderSetting radius = new SliderSetting(this, "Радиус роя", 3.0F, 1.0F, 6.0F, 0.5F);
   private final float[] f768;
   private final float[] f773;
   private final float[] f776;

   private void m870(int i, Vec3d anchor) {
      double v3 = Math.random() * Math.PI * 2.0;
      double result = Math.random();

      double v5 = Math.acos(result * 2.0 - 1.0);
      result = (double)this.radius.getValue().floatValue();
      double temp1 = 0.5 + Math.random() * 0.8;

      double v7 = result * temp1;
      this.f768[i] = (float)(anchor.field_1352 + Math.sin(v5) * Math.cos(v3) * v7);
      this.f769[i] = (float)(anchor.field_1351 + Math.cos(v5) * v7 * 0.7);
      float[] v13 = this.f770;
      double v15 = anchor.field_1350;
      double temp3 = Math.sin(v5);
      double temp4 = Math.sin(v3);

      v13[i] = (float)(v15 + temp3 * temp4 * v7);
      this.f774[i] = (float)(Math.random() - 0.5);
      this.f775[i] = (float)(Math.random() - 0.5);
      this.f776[i] = (float)(Math.random() - 0.5);
      this.f771[i] = this.f768[i];
      this.f772[i] = this.f769[i];
      this.f773[i] = this.f770[i];
      this.f777[i] = (float)(Math.random() * 100.0);
   }

   private void m871(VertexConsumer buf, MatrixStack matrices, Quaternionf camRot, Vec3d rel, float size, int color) {
      if ((color >> 24 & 255) > 0) {
         matrices.method_22903();
         matrices.method_22904(rel.field_1352, rel.field_1351, rel.field_1350);
         matrices.method_22907(camRot);
         Matrix4f v7 = matrices.method_23760().method_23761();
         VertexConsumer result = buf.method_22918(v7, -size, -size, 0.0F);

         result.method_39415(color)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(v7, size, -size, 0.0F)
            .method_39415(color)
            .method_22913(1.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(v7, size, size, 0.0F)
            .method_39415(color)
            .method_22913(1.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(v7, -size, size, 0.0F)
            .method_39415(color)
            .method_22913(0.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         matrices.method_22909();
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.f778 != 0) {
         long v2 = System.currentTimeMillis();
         float v4 = e.getTickDelta();
         Vec3d v5 = mc.field_1773.method_19418().method_71156();
         MatrixStack v6 = e.getMatrixStack();
         Immediate v7 = VertexConsumerProvider.method_22991(this.f781);
         VertexConsumer v8 = v7.method_73477(
            xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
         );
         Quaternionf v9 = mc.field_1773.method_19418().method_23767();
         int v10 = up.getColor(255, 55, 55, 255);

         for (int v11 = 0; v11 < this.f778; v11++) {
            float v12 = this.f771[v11] + (this.f768[v11] - this.f771[v11]) * v4;
            float v13 = this.f772[v11] + (this.f769[v11] - this.f772[v11]) * v4;
            float v14 = this.f773[v11] + (this.f770[v11] - this.f773[v11]) * v4;
            Vec3d v15 = new Vec3d((double)v12 - v5.field_1352, (double)v13 - v5.field_1351, (double)v14 - v5.field_1350);
            int v16 = up.fade(v11 * (47) % (360));
            double temp2 = (double)v2 / 170.0;

            float v17 = 0.7F + 0.3F * (float)Math.sin(temp2 + (double)(this.f777[v11] * 20.0F));
            float v18 = 0.85F + 0.15F * (float)Math.sin((double)v2 / 900.0 + (double)(this.f777[v11] * 3.0F));
            int v19 = up.overCol(v16, v10, MathHelper.method_15363(this.f779, 0.0F, 1.0F));
            float v20 = this.dotSize.getValue() * v18;

            this.m871(v8, v6, v9, v15, v20 * 2.4F, up.replAlpha(v19, (int)(70.0F * v17)));
            this.m871(v8, v6, v9, v15, v20 * 0.9F, up.replAlpha(v19, (int)(215.0F * v17)));
         }

         v7.method_22994(
            xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
         );
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         float result;
         if (this.f780 == 0L) {
            result = 0.05F;
         } else {
            float temp1 = (float)(v2 - this.f780) / 1000.0F;

            result = Math.min(0.1F, temp1);
         }

         float v4 = result;
         this.f780 = v2;
         Vec3d v5 = mc.field_1724.method_33571();
         int v6 = this.count.getValue().intValue();
         if (this.f778 < v6) {
            for (int v7 = this.f778; v7 < v6; v7++) {
               this.m870(v7, v5);
            }

            this.f778 = v6;
         } else if (this.f778 > v6) {
            this.f778 = v6;
         }

         LivingEntity v36 = fg.target;
         boolean v8 = v36 != null && v36.method_5805();
         this.f779 = this.f779 + ((float)(v8 ? 1 : 0) - this.f779) * Math.min(1.0F, v4 * 3.0F);
         Vec3d v9 = v8 ? v36.method_73189().method_1031(0.0, (double)v36.method_17682() * 0.5, 0.0) : null;
         ArrayList v10 = new ArrayList();

         for (Entity v12 : mc.field_1687.method_18112()) {
            if (v12 != mc.field_1724) {
               boolean v13 = false;
               if (v12 instanceof PlayerEntity) {
                  PlayerEntity v14 = (PlayerEntity)v12;
                  if (!v14.method_5805() || v14.method_7325() || Client.get().friendManager().isFriend(v14.method_5477().getString())) {
                     continue;
                  }

                  v13 = true;
               } else if (this.scareMobs.getValue() && v12 instanceof LivingEntity) {
                  LivingEntity v15 = (LivingEntity)v12;
                  if (v15.method_5805()) {
                     v13 = true;
                  }
               }

               if (v13) {
                  if (v12.method_5858(mc.field_1724) < 144.0) {
                     v10.add(v12.method_73189());
                  }

                  if (v10.size() >= 24) {
                     break;
                  }
               }
            }
         }

         float v37 = this.speed.getValue();
         float v38 = this.radius.getValue();
         float v39 = 9.0F * v37;
         float v40 = 1.2F * v37;

         for (int v41 = 0; v41 < this.f778; v41++) {
            this.f771[v41] = this.f768[v41];
            this.f772[v41] = this.f769[v41];
            this.f773[v41] = this.f770[v41];
            float v16 = this.f768[v41] - (float)v5.field_1352;
            float v17 = this.f769[v41] - (float)v5.field_1351;
            float v18 = this.f770[v41] - (float)v5.field_1350;
            float v19 = (float)Math.sqrt((double)(v16 * v16 + v17 * v17 + v18 * v18)) + 1.0E-5F;
            float v20 = (v19 - v38) * 6.0F;
            float v21 = -v16 / v19 * v20;
            float v22 = -v17 / v19 * v20;
            float v23 = -v18 / v19 * v20;
            float v24 = (float)(this.f777[v41] > 50.0F ? 1 : -1) * 3.2F * v37;
            v21 += -v18 / v19 * v24;
            v23 += v16 / v19 * v24;
            v22 = (float)((double)v22 + Math.sin((double)v2 / 500.0 + (double)(this.f777[v41] * 7.0F)) * 1.6F);

            for (int v25 = v41 + 1; v25 < this.f778; v25++) {
               float v26 = this.f768[v41] - this.f768[v25];
               if (!(v26 > 0.6F) && !(v26 < -0.6F)) {
                  float v27 = this.f769[v41] - this.f769[v25];
                  if (!(v27 > 0.6F) && !(v27 < -0.6F)) {
                     float v28 = this.f770[v41] - this.f770[v25];
                     if (!(v28 > 0.6F) && !(v28 < -0.6F)) {
                        float v29 = (float)Math.sqrt((double)(v26 * v26 + v27 * v27 + v28 * v28));
                        if (v29 < 0.3F && v29 > 1.0E-4F) {
                           float v30 = (0.3F - v29) * 22.0F * v4 / v29;
                           float v31 = v26 * v30;
                           float v32 = v27 * v30;
                           float v33 = v28 * v30;
                           this.f774[v41] = this.f774[v41] + v31;
                           this.f775[v41] = this.f775[v41] + v32;
                           this.f776[v41] = this.f776[v41] + v33;
                           this.f774[v25] = this.f774[v25] - v31;
                           this.f775[v25] = this.f775[v25] - v32;
                           this.f776[v25] = this.f776[v25] - v33;
                        }
                     }
                  }
               }
            }

            for (int v45 = 0; v45 < v10.size(); v45++) {
               Vec3d v48 = (Vec3d)v10.get(v45);
               float v51 = this.f768[v41] - (float)v48.field_1352;
               float v54 = this.f769[v41] - (float)v48.field_1351;
               float v56 = this.f770[v41] - (float)v48.field_1350;
               float v58 = (float)Math.sqrt((double)(v51 * v51 + v54 * v54 + v56 * v56));
               if (v58 < 6.0F && v58 > 1.0E-4F) {
                  float v59 = (1.0F - v58 / 6.0F) * 34.0F;
                  v21 += v51 / v58 * v59;
                  v22 += v54 / v58 * v59;
                  v23 += v56 / v58 * v59;
               }
            }

            if (v9 != null) {
               float v46 = (float)v9.field_1352 - this.f768[v41];
               float v49 = (float)v9.field_1351 - this.f769[v41];
               float v52 = (float)v9.field_1350 - this.f770[v41];
               float v55 = (float)Math.sqrt((double)(v46 * v46 + v49 * v49 + v52 * v52)) + 1.0E-4F;
               if (v55 < 24.0F) {
                  float v57 = 5.0F * (1.0F - v55 / 24.0F) + 1.2F;
                  v21 += v46 / v55 * v57;
                  v22 += v49 / v55 * v57;
                  v23 += v52 / v55 * v57;
               }
            }

            this.f774[v41] = this.f774[v41] + v21 * v4;
            this.f775[v41] = this.f775[v41] + v22 * v4;
            this.f776[v41] = this.f776[v41] + v23 * v4;
            float v47 = Math.max(0.0F, 1.0F - 2.2F * v4);
            this.f774[v41] = this.f774[v41] * v47;
            this.f775[v41] = this.f775[v41] * v47;
            this.f776[v41] = this.f776[v41] * v47;
            float v50 = (float)Math.sqrt(
                  (double)(this.f774[v41] * this.f774[v41] + this.f775[v41] * this.f775[v41] + this.f776[v41] * this.f776[v41])
               )
               + 1.0E-6F;
            float v53 = MathHelper.method_15363(v50, v40, v39) / v50;
            this.f774[v41] = this.f774[v41] * v53;
            this.f775[v41] = this.f775[v41] * v53;
            this.f776[v41] = this.f776[v41] * v53;
            this.f768[v41] = this.f768[v41] + this.f774[v41] * v4;
            this.f769[v41] = this.f769[v41] + this.f775[v41] * v4;
            this.f770[v41] = this.f770[v41] + this.f776[v41] * v4;
            if (v19 > 60.0F) {
               this.m870(v41, v5);
            }
         }
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f778 = 0;
   }

   public SoulSwarm() {
      this.speed = new SliderSetting(this, "Скорость", 1.0F, 0.5F, 2.0F, 0.1F);
      this.dotSize = new SliderSetting(this, "Размер", 0.06F, 0.03F, 0.12F, 0.01F);
      this.scareMobs = new BooleanSetting(this, "Бояться мобов", false);
      this.f768 = new float[320];
      this.f769 = new float[320];
      this.f770 = new float[320];
      this.f771 = new float[320];
      this.f772 = new float[320];
      this.f773 = new float[320];
      this.f774 = new float[320];
      this.f775 = new float[320];
      this.f776 = new float[320];
      this.f777 = new float[320];
      this.f778 = 0;
      this.f779 = 0.0F;
      this.f780 = 0L;
      this.f781 = new BufferAllocator(65536);
   }

   @Override
   protected void onDisable() {
      this.f778 = 0;
      this.f779 = 0.0F;
      super.onDisable();
   }

   
}
