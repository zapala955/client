package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Soul Chain",
   desc = "Физические цепи душ между тобой и целями",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class SoulChain extends Module {
   public final BooleanSetting attackPulse;
   private final Map<Integer, qk.sk> f762;
   public final BooleanSetting glowEffect;
   private static final long KILL_WINDOW_MS = 7000L;
   public final SliderSetting chainPoints;
   public final BooleanSetting showToPlayers;
   private final Random f765;
   public final ModeSetting anchorMode;
   public final SliderSetting chainWidth;
   private final List<qk.rk> f763;
   private static final RenderPipeline CHAIN_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/soul_chain/ribbon"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public final ModeSetting colorMode = new ModeSetting(this, "Цвет", "Тема", "Душа", "Пламя", "Свой");
   private long f767;
   public final SliderSetting gravity;
   public final SliderSetting glowAlpha;
   private int f766;
   private final BufferAllocator f764;
   private static final RenderLayer CHAIN_LAYER = RenderLayer.method_75940(
      "soul_chain_ribbon",
      RenderSetup.method_75927(CHAIN_PIPELINE).method_75937().method_75929(65536).method_75938()
   );
   public final SliderSetting damping;
   public final SliderSetting maxTargets;
   public final BooleanSetting breakOnKill;

   private void m862(qk.sk c) {
      int v2 = this.m865(c, 0.0F);

      for (qk.tk v6 : c.points) {
         if (!(this.f765.nextFloat() < 0.5F)) {
            double v7 = 0.05 + this.f765.nextDouble() * 0.12;
            double v9 = this.f765.nextDouble() * Math.PI * 2.0;
            double v11 = 0.04 + this.f765.nextDouble() * 0.08;
            List result = this.f763;
            qk.rk temp1 = new qk.rk;
            double temp3 = v6.x;
            double temp4 = v6.y;
            double temp5 = v6.z;
            double var10006 = Math.cos(v9) * v7;

            temp1./* $VF: Unable to resugar constructor */<init>(temp3, temp4, temp5, var10006, v11, Math.sin(v9) * v7, v2);
            result.add(temp1);
         }
      }
   }

   public SoulChain() {
      this.chainPoints = new SliderSetting(this, "Звеньев цепи", 16.0F, 8.0F, 32.0F, 1.0F);
      this.chainWidth = new SliderSetting(this, "Ширина цепи", 0.04F, 0.01F, 0.14F, 0.005F);
      this.gravity = new SliderSetting(this, "Гравитация", 0.018F, 0.0F, 0.06F, 0.001F);
      this.damping = new SliderSetting(this, "Демпфирование", 0.88F, 0.6F, 0.98F, 0.01F);
      this.maxTargets = new SliderSetting(this, "Макс. целей", 3.0F, 1.0F, 6.0F, 1.0F);
      this.attackPulse = new BooleanSetting(this, "Импульс при атаке", true);
      this.breakOnKill = new BooleanSetting(this, "Разрыв при убийстве", true);
      this.glowEffect = new BooleanSetting(this, "Свечение", true);
      this.glowAlpha = new SliderSetting(this, "Яркость свечения", 0.45F, 0.05F, 1.0F, 0.05F);
      this.showToPlayers = new BooleanSetting(this, "Только игроки", true);
      this.anchorMode = new ModeSetting(this, "Якорь", "Грудь", "Рука", "Ноги");
      this.f762 = new LinkedHashMap<>();
      this.f763 = new ArrayList<>();
      this.f764 = new BufferAllocator(131072);
      this.f765 = new Random(2490484382080983299L + -2490484378345054740L);
      this.f766 = -1;
      this.f767 = 0L;
   }

   private void m858(qk.sk c) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Vec3d v2 = this.m866();
         qk.tk v3 = c.points[0];
         v3.px = v3.x;
         v3.py = v3.y;
         v3.pz = v3.z;
         v3.x = v2.field_1352;
         v3.y = v2.field_1351;
         v3.z = v2.field_1350;
         v3.pinned = true;
         if (mc.field_1687.method_8469(c.targetId) instanceof LivingEntity v5) {
            Vec3d v6 = this.m867(v5);
            qk.tk v7 = c.points[c.points.length - 1];

            v7.px = v7.x;
            v7.py = v7.y;
            v7.pz = v7.z;
            v7.x = v6.field_1352;
            v7.y = v6.field_1351;
            v7.z = v6.field_1350;
            v7.pinned = true;
         }
      }
   }

   @Override
   protected void onDisable() {
      this.f762.clear();
      this.f763.clear();

      this.f766 = -1;
   }

   private Vec3d m866() {
      ClientPlayerEntity v1 = mc.field_1724;

      
      double v2 = switch (this.anchorMode.getIndex()) {
         case 1 -> (double)v1.method_17682() * 0.75;
         case 2 -> 0.1;
         default -> (double)v1.method_17682() * 0.5;
      };
      return new Vec3d(v1.method_23317(), v1.method_23318() + v2, v1.method_23321());
   }

   private double m860(qk.sk c) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         Entity v2 = mc.field_1687.method_8469(c.targetId);
         if (v2 == null) {
            return 3.0;
         } else {
            double result = (double)mc.field_1724.method_5739(v2);

            return result * 1.05;
         }
      } else {

         return 3.0;
      }
   }

   private static void m859(qk.tk[] pts, double restLen) {
      for (int v3 = 0; v3 < pts.length - 1; v3++) {
         qk.tk v4 = pts[v3];
         qk.tk v5 = pts[v3 + 1];
         double v6 = v5.x - v4.x;
         double v8 = v5.y - v4.y;
         double v10 = v5.z - v4.z;
         double result = v6 * v6;

         double v12 = Math.sqrt(result + v8 * v8 + v10 * v10);
         if (!(v12 < 1.0E-4)) {
            double v14 = (v12 - restLen) / v12 * 0.5;

            double v16 = v6 * v14;

            double v18 = v8 * v14;
            double v20 = v10 * v14;
            if (!v4.pinned) {
               v4.x += v16;
               v4.y += v18;
               v4.z += v20;
            }

            if (!v5.pinned) {
               v5.x -= v16;
               v5.y -= v18;
               v5.z -= v20;
            }
         }
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.f762.clear();

      this.f763.clear();
      this.f766 = -1;
   }

   private void m861(qk.sk c) {
      c.hitFlash = 1.0F;
      qk.tk[] v2 = c.points;
      int v3 = v2.length;

      for (int v4 = 1; v4 < v3 - 1; v4++) {
         float v5 = (float)Math.sin(Math.PI * (double)v4 / (double)(v3 - 1));
         double v6 = (double)v5 * 0.22;
         double v8 = this.f765.nextDouble() * Math.PI * 2.0;
         v2[v4].py -= v6 * 1.4;
         v2[v4].px = v2[v4].px + Math.cos(v8) * v6 * 0.5;
         qk.tk result = v2[v4];

         result.pz = v2[v4].pz + Math.sin(v8) * v6 * 0.5;
      }
   }

   private Vec3d m867(LivingEntity e) {
      Vec3d result = new Vec3d;
      double temp2 = e.method_23317();
      double temp3 = e.method_23318();
      float temp4 = e.method_17682();

      result./* $VF: Unable to resugar constructor */<init>(temp2, temp3 + (double)temp4 * 0.5, e.method_23321());
      return result;
   }

   private static void m864(VertexConsumer buf, Matrix4f mat, Vec3d cam, qk.tk p, Vector3f right, float halfS, int r, int g, int b, int alpha) {
      float v10 = (float)(p.x - cam.field_1352);
      float v11 = (float)(p.y - cam.field_1351);
      float v12 = (float)(p.z - cam.field_1350);
      float v13 = right.x * halfS;
      float v14 = right.z * halfS;
      buf.method_22918(mat, v10 - v13, v11 - halfS, v12 - v14).method_1336(r, g, b, 0);
      float temp2 = v10 + v13;

      buf.method_22918(mat, temp2, v11 - halfS, v12 + v14).method_1336(r, g, b, alpha);
      buf.method_22918(mat, v10 + v13, v11 + halfS, v12 + v14).method_1336(r, g, b, alpha);
      buf.method_22918(mat, v10 - v13, v11 + halfS, v12 - v14).method_1336(r, g, b, 0);
   }

   private static void m863(
      VertexConsumer buf,
      Matrix4f mat,
      Vec3d cam,
      double ax,
      double ay,
      double az,
      double bx,
      double by,
      double bz,
      Vector3f right,
      float halfW,
      int r,
      int g,
      int b,
      int centerA,
      int edgeA
   ) {
      float v22 = (float)(ax - cam.field_1352);
      float v23 = (float)(ay - cam.field_1351);
      int temp1 = v35 + 1;

      float v24 = (float)(az - cam.field_1350);
      float v25 = (float)(bx - cam.field_1352);
      float v26 = (float)(by - cam.field_1351);
      float v27 = (float)(bz - cam.field_1350);
      float v28 = right.x * halfW;
      float v29 = right.y * halfW;
      float v30 = right.z * halfW;
      buf.method_22918(mat, v22 - v28, v23 - v29, v24 - v30).method_1336(r, g, b, edgeA);
      buf.method_22918(mat, v22 + v28, v23 + v29, v24 + v30).method_1336(r, g, b, edgeA);
      buf.method_22918(mat, v25 + v28, v26 + v29, v27 + v30).method_1336(r, g, b, edgeA);
      buf.method_22918(mat, v25 - v28, v26 - v29, v27 - v30).method_1336(r, g, b, edgeA);
      float v31 = halfW * 0.35F;
      float v32 = right.x * v31;
      float v33 = right.y * v31;
      float v34 = right.z * v31;
      buf.method_22918(mat, v22 - v32, v23 - v33, v24 - v34).method_1336(r, g, b, centerA);
      buf.method_22918(mat, v22 + v32, v23 + v33, v24 + v34).method_1336(r, g, b, centerA);
      buf.method_22918(mat, v25 + v32, v26 + v33, v27 + v34).method_1336(r, g, b, centerA);
      buf.method_22918(mat, v25 - v32, v26 - v33, v27 - v34).method_1336(r, g, b, centerA);
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.f762.isEmpty() || !this.f763.isEmpty()) {
            Vec3d v2 = mc.field_1773.method_19418().method_71156();
            MatrixStack v3 = e.getMatrixStack();
            Matrix4f v4 = v3.method_23760().method_23761();
            float v5 = (float)Math.toRadians((double)mc.field_1773.method_19418().method_19330());
            Vector3f result = new Vector3f(MathHelper.method_15362((double)v5), 0.0F, MathHelper.method_15374((double)v5));

            Vector3f v6 = result.normalize();
            Immediate v7 = VertexConsumerProvider.method_22991(this.f764);
            VertexConsumer v8 = v7.method_73477(CHAIN_LAYER);
            float v9 = this.chainWidth.getValue();

            for (qk.sk v11 : this.f762.values()) {
               qk.tk[] v12 = v11.points;
               float v13 = v11.breaking ? MathHelper.method_15363(1.0F - v11.breakProgress * 1.6F, 0.0F, 1.0F) : 1.0F;
               if (!(v13 < 0.02F)) {
                  for (int v14 = 0; v14 < v12.length - 1; v14++) {
                     qk.tk v15 = v12[v14];
                     qk.tk v16 = v12[v14 + 1];
                     float v17 = (float)v14 / (float)(v12.length - 2);
                     int v18 = this.m865(v11, v17);
                     float v19 = v13 * (0.65F + 0.35F * (float)Math.sin((double)v17 * Math.PI));
                     int v20 = v18 >> 16 & 255;
                     int v21 = v18 >> 8 & 255;
                     int v22 = v18 & 255;
                     int v23 = (int)(v19 * 210.0F);
                     int v24 = (int)(v19 * 60.0F);
                     m863(v8, v4, v2, v15.x, v15.y, v15.z, v16.x, v16.y, v16.z, v6, v9, v20, v21, v22, v23, v24);
                     if (this.glowEffect.getValue()) {
                        float v25 = this.glowAlpha.getValue();
                        m863(
                           v8,
                           v4,
                           v2,
                           v15.x,
                           v15.y,
                           v15.z,
                           v16.x,
                           v16.y,
                           v16.z,
                           v6,
                           v9 * 3.5F,
                           v20,
                           v21,
                           v22,
                           (int)(v25 * v13 * 55.0F),
                           0
                        );
                     }

                     if (v14 > 0) {
                        m864(v8, v4, v2, v15, v6, v9 * 1.8F, v20, v21, v22, (int)(v19 * 200.0F));
                     }
                  }
               }
            }

            for (qk.rk v28 : this.f763) {
               float v29 = (float)(v28.x - v2.field_1352);
               float v30 = (float)(v28.y - v2.field_1351);
               float v31 = (float)(v28.z - v2.field_1350);
               int v32 = v28.color >> 16 & 255;
               int v33 = v28.color >> 8 & 255;
               int v34 = v28.color & 255;
               int v35 = (int)(v28.alpha * 200.0F);
               float v36 = v9 * 1.6F;
               v8.method_22918(v4, v29 - v36 * v6.x, v30 + v36, v31 - v36 * v6.z).method_1336(v32, v33, v34, 0);
               v8.method_22918(v4, v29 - v36 * v6.x, v30 - v36, v31 - v36 * v6.z).method_1336(v32, v33, v34, v35);
               v8.method_22918(v4, v29 + v36 * v6.x, v30 - v36, v31 + v36 * v6.z).method_1336(v32, v33, v34, v35);
               v8.method_22918(v4, v29 + v36 * v6.x, v30 + v36, v31 + v36 * v6.z).method_1336(v32, v33, v34, 0);
            }

            v7.method_22993();
         }
      }
   }

   private int m865(qk.sk c, float t) {
      long v4 = System.currentTimeMillis();
      int v3;
      if (this.colorMode.is("Тема")) {

         int v15 = up.getClientColor1((int)(t * 360.0F));

         v3 = v15;
      } else if (this.colorMode.is("Душа")) {
         float v6 = 0.73F + 0.05F * (float)Math.sin((double)v4 * 0.002 + (double)t * Math.PI);
         float v7 = 0.8F;
         float v8 = 0.85F + 0.15F * (float)Math.sin((double)v4 * 0.003 + (double)t * Math.PI * 2.0);
         v3 = Color.HSBtoRGB(v6, v7, v8) | -16777216;
      } else if (this.colorMode.is("Пламя")) {
         double temp2 = (double)v4;

         float v12 = 0.04F + 0.03F * (float)Math.sin(temp2 * 0.002 + (double)t * Math.PI);
         float v13 = 0.9F;
         float v14 = 0.9F + 0.1F * (float)Math.sin((double)v4 * 0.004);
         v3 = Color.HSBtoRGB(v12, v13, v14) | -16777216;
      } else {
         v3 = up.getClientColor1(1);
      }

      if (c.hitFlash > 0.01F) {
         v3 = up.interpolateColor(v3, -1, c.hitFlash * 0.7F);
      }

      return v3;
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (this.breakOnKill.getValue()) {
         if (e.getPacket() instanceof EntityStatusS2CPacket v2) {
            if (v2.method_11470() == 3) {
               if (mc.field_1687 != null) {
                  Entity v10 = v2.method_11469(mc.field_1687);
                  if (v10 == null) {
                     return;
                  }

                  long v4 = System.currentTimeMillis();
                  if (v10.method_5628() == this.f766) {
                     long v11 = v4 - this.f767;
                     long temp1 = -3797230713974105055L + 3797230713974112055L;

                     if (v11 < temp1) {
                        qk.sk v6 = this.f762.get(v10.method_5628());
                        if (v6 != null && !v6.breaking) {
                           v6.breaking = true;
                           this.m862(v6);
                        }
                     }
                  }

                  return;
               }

               
            }
         }
      }
   }

   @EventTarget
   public void onAttack(Event e) {
      if (e.getTarget() instanceof LivingEntity v2) {
         this.f766 = v2.method_5628();
         this.f767 = System.currentTimeMillis();
         if (this.attackPulse.getValue()) {
            qk.sk v5 = this.f762.get(v2.method_5628());
            if (v5 != null) {

               this.m861(v5);
            }

            if (v5 == null) {
               this.m857(v2);
            }
         } else {
            this.m857(v2);
         }
      }
   }

   private void m856() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int v1 = this.maxTargets.getValue().intValue();
         if (this.f766 != -1 && mc.field_1687.method_8469(this.f766) instanceof LivingEntity v3 && !this.f762.containsKey(this.f766)) {
            this.m857(v3);
         }

         while (this.f762.size() > v1) {

            for (Entry v4 : this.f762.entrySet()) {
               if (!((qk.sk)v4.getValue()).breaking) {
                  v6 = (Integer)v4.getKey();
                  break;
               }
            }

            if (v6 == -1) {
               break;
            }

            qk.sk v8 = this.f762.remove(v6);

            this.m862(v8);
         }
      }
   }

   private void m857(LivingEntity target) {

      if (!this.f762.containsKey(target.method_5628())) {
         if (mc.field_1724 != null) {
            if (!this.showToPlayers.getValue() || target instanceof PlayerEntity) {
               if (!((double)mc.field_1724.method_5739(target) > 24.0)) {
                  int v2 = this.chainPoints.getValue().intValue();
                  qk.tk[] v3 = new qk.tk[v2];
                  Vec3d v4 = this.m866();
                  Vec3d v5 = this.m867(target);

                  for (int v6 = 0; v6 < v2; v6++) {
                     float v7 = (float)v6 / (float)(v2 - 1);
                     qk.tk v11 = new qk.tk;
                     double temp4 = (double)v7;

                     temp4 = MathHelper.method_16436(temp4, v4.field_1352, v5.field_1352);
                     double temp5 = (double)v7;

                     v11./* $VF: Unable to resugar constructor */<init>(
                        temp4,
                        MathHelper.method_16436(temp5, v4.field_1351, v5.field_1351),
                        MathHelper.method_16436((double)v7, v4.field_1350, v5.field_1350)
                     );
                     v3[v6] = v11;
                  }

                  v3[0].pinned = true;
                  v3[v2 - 1].pinned = true;
                  this.f762.put(target.method_5628(), new qk.sk(target.method_5628(), v3));
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {

         this.f762.entrySet().removeIf(entry -> {
            qk.sk v2x = entry.getValue();
            if (v2x.breaking && v2x.breakProgress >= 1.0F) {
               return true;
            } else {
               Entity v3x = mc.field_1687.method_8469(entry.getKey());
               if (v3x != null && !v3x.method_31481()) {

                  if (v3x.method_5805()) {
                     double v4x = (double)mc.field_1724.method_5739(v3x);
                     if (v4x > 24.0) {
                        if (!v2x.breaking) {
                           v2x.breaking = true;
                           this.m862(v2x);
                        }

                        return false;
                     }

                     return false;
                  }
               }

               if (!v2x.breaking) {
                  v2x.breaking = true;
                  this.m862(v2x);
               }

               return false;
            }
         });
         if (!this.f762.isEmpty() || this.f766 != -1) {
            this.m856();
         }

         float v2 = this.gravity.getValue();
         float v3 = this.damping.getValue();
         int v4 = this.chainPoints.getValue().intValue();

         for (qk.sk v6 : this.f762.values()) {
            if (v6.breaking) {
               v6.breakProgress += 0.06F;
            } else {
               v6.hitFlash *= 0.82F;
               this.m858(v6);
               qk.tk[] v7 = v6.points;

               for (qk.tk v11 : v7) {
                  if (!v11.pinned) {
                     double v12 = (v11.x - v11.px) * (double)v3;
                     double result = v11.y - v11.py;
                     double v22 = (double)v3;

                     double v14 = result * v22 - (double)v2;
                     double v16 = (v11.z - v11.pz) * (double)v3;
                     v11.px = v11.x;
                     v11.py = v11.y;
                     v11.pz = v11.z;
                     v11.x += v12;
                     v11.y += v14;
                     v11.z += v16;
                  }
               }

               double v20 = this.m860(v6) / (double)(v4 - 1);

               for (int v21 = 0; v21 < 8; v21++) {
                  m859(v7, v20);
                  this.m858(v6);
               }
            }
         }

         this.f763.removeIf(s -> {
            s.x = s.x + s.vx;

            s.y = s.y + s.vy;
            s.z = s.z + s.vz;
            s.vy -= 0.008;
            s.vx *= 0.92;
            s.vy *= 0.92;

            s.vz *= 0.92;
            s.alpha -= 0.04F;

            return s.alpha <= 0.0F;
         });
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class rk {
      double vx;
      int color;
      float alpha;
      double vy;
      double vz;
      double x;
      double y;
      double z;

      rk(double x, double y, double z, double vx, double vy, double vz, int color) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.vx = vx;
         this.vy = vy;
         this.vz = vz;
         this.color = color;
         this.alpha = 1.0F;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class sk {
      final qk.tk[] points;
      float breakProgress;
      final int targetId;
      boolean breaking = false;
      float hitFlash;

      sk(int targetId, qk.tk[] points) {
         this.breakProgress = 0.0F;
         this.hitFlash = 0.0F;
         this.targetId = targetId;
         this.points = points;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class tk {
      double x;
      double px;
      double y;
      double pz;
      boolean pinned;
      double py;
      double z;

      tk(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.px = x;
         this.py = y;
         this.pz = z;
      }

      
   }
}
