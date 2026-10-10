package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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
import ru.white.manager.rotation.AngleUtil;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Blade Storm",
   desc = "Шлейфовый шторм клинков с выпадами при ударе",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class BladeStorm extends Module {
   public final SliderSetting blades = new SliderSetting(this, "Клинков", 5.0F, 3.0F, 7.0F, 1.0F);
   private long f637;
   private static final RenderPipeline BLADE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "blade_storm"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private double f636;
   public final BooleanSetting trails;
   public final SliderSetting orbitSpeed;
   private final List<ri.si> list;
   private static final int TRAIL = 14;
   public final SliderSetting bladeLen;
   private static final RenderLayer BLADE_LAYER = RenderLayer.method_75940(
      "blade_storm", RenderSetup.method_75927(BLADE_PIPELINE).method_75929(16384).method_75938()
   );
   private static final long RETURN_MS = 380L;
   public final BooleanSetting glow;
   public final SliderSetting orbitRadius = new SliderSetting(this, "Радиус орбиты", 1.5F, 0.8F, 2.4F, 0.1F);
   private static final int SEG = 6;
   private final BufferAllocator f639;
   private static final long LUNGE_MS = 150L;
   private int f638;

   private void m672(VertexConsumer buf, Matrix4f m, Vec3d cam, ri.ti pose, float len, float baseW, int color) {
      if ((color >> 24 & 255) > 0) {
         Vec3d v8 = Vec3d.method_1030(mc.field_1773.method_19418().method_19329(), mc.field_1773.method_19418().method_19330());
         Vec3d v9 = pose.dir.method_1036(v8);
         if (v9.method_1027() < 1.0E-6) {
            v9 = new Vec3d(0.0, 1.0, 0.0).method_1036(pose.dir);
         }

         v9 = v9.method_1029();
         Vec3d v10 = null;

         float v11 = 0.0F;

         for (int v12 = 0; v12 <= 6; v12++) {
            float v13 = (float)v12 / 6.0F;
            Vec3d v14 = pose.f635.method_1019(pose.dir.method_1021((double)(len * v13))).method_1020(cam);
            float v15 = baseW * (1.0F - v13 * 0.92F);
            if (v10 != null) {
               Vec3d v16 = v9.method_1021((double)v11);
               Vec3d v17 = v9.method_1021((double)v15);
               float temp2 = (float)(v10.field_1352 - v16.field_1352);

               buf.method_22918(m, temp2, (float)(v10.field_1351 - v16.field_1351), (float)(v10.field_1350 - v16.field_1350)).method_39415(color);

               buf.method_22918(
                     m,
                     (float)(v14.field_1352 - v17.field_1352),
                     (float)(v14.field_1351 - v17.field_1351),
                     (float)(v14.field_1350 - v17.field_1350)
                  )
                  .method_39415(color);
               buf.method_22918(
                     m,
                     (float)(v14.field_1352 + v17.field_1352),
                     (float)(v14.field_1351 + v17.field_1351),
                     (float)(v14.field_1350 + v17.field_1350)
                  )
                  .method_39415(color);
               buf.method_22918(
                     m,
                     (float)(v10.field_1352 + v16.field_1352),
                     (float)(v10.field_1351 + v16.field_1351),
                     (float)(v10.field_1350 + v16.field_1350)
                  )
                  .method_39415(color);
            }

            v10 = v14;
            v11 = v15;
         }
      }
   }

   private Vec3d m669(ri.si b, Vec3d center) {
      double v3 = this.f636 + b.phase;
      double v5 = (double)this.orbitRadius.getValue().floatValue();
      long v7 = System.currentTimeMillis();
      double v9 = Math.sin((double)v7 / 480.0 + b.phase * 3.0) * 0.22;
      double v11 = Math.sin(b.phase * 2.0) * 0.32;
      Vec3d result = new Vec3d;
      double temp2 = center.field_1352 + Math.cos(v3) * v5;
      double temp3 = center.field_1351 + 0.35 + v9 + v11;

      double v16 = center.field_1350;
      double temp5 = Math.sin(v3);

      result./* $VF: Unable to resugar constructor */<init>(temp2, temp3, v16 + temp5 * v5);
      return result;
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         float v4 = this.f637 == 0L ? 0.05F : Math.min(0.1F, (float)(v2 - this.f637) / 1000.0F);
         this.f637 = v2;

         this.f636 = this.f636 + (double)(this.orbitSpeed.getValue() * v4);
         this.m668();
         Vec3d v5 = this.m667();

         for (ri.si v7 : this.list) {
            v7.flash = Math.max(0.0F, v7.flash - v4 * 2.6F);
            if (v7.state == 1) {
               if (v2 - v7.t0 >= 2182203413986592989L + -2182203413986592839L) {
                  v7.state = 2;
                  v7.t0 = v2;
                  v7.from = v7.lock;
               }
            } else if (v7.state == 2) {
               long v14;
               int result = (v14 = v2 - v7.t0 - (-7955490372770553414L + 7955490372770553794L)) == 0L ? 0 : (v14 < 0L ? -1 : 1);

               if (result >= 0) {
                  v7.state = 0;
               }
            }

            if (v7.lock != null && v7.lock.method_1025(v5) > 3600.0) {
               v7.state = 0;
            }

            Deque v11 = v7.trail;
            ri.ti v13 = this.m671(v7, v5, v2);

            v11.addLast(v13.f635);

            while (v7.trail.size() > 14) {
               v7.trail.pollFirst();
            }
         }
      }
   }

   public BladeStorm() {
      this.orbitSpeed = new SliderSetting(this, "Скорость орбиты", 2.2F, 0.5F, 5.0F, 0.1F);
      this.bladeLen = new SliderSetting(this, "Длина клинка", 1.1F, 0.5F, 1.6F, 0.1F);
      this.trails = new BooleanSetting(this, "Шлейфы", true);
      this.glow = new BooleanSetting(this, "Свечение", true);
      this.list = new ArrayList<>();
      this.f636 = 0.0;
      this.f637 = 0L;
      this.f638 = 0;
      this.f639 = new BufferAllocator(65536);
   }

   @EventTarget
   public void onAttack(Event e) {
      if (mc.field_1724 != null && mc.field_1687 != null && e.getTarget() != null) {
         this.m668();

         for (int v2 = 0; v2 < this.list.size(); v2++) {
            this.f638 = (this.f638 + 1) % this.list.size();
            ri.si v3 = this.list.get(this.f638);
            if (v3.state == 0) {
               Entity v4 = e.getTarget();
               Vec3d temp1 = new Vec3d;

               temp1./* $VF: Unable to resugar constructor */<init>(
                  v4.method_23317(), v4.method_23318() + (double)v4.method_17682() * 0.55, v4.method_23321()
               );
               v3.lock = temp1;
               v3.from = this.m669(v3, this.m667());
               v3.t0 = System.currentTimeMillis();
               v3.state = 1;
               v3.flash = 1.0F;
               break;
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.list.isEmpty()) {
         long v2 = System.currentTimeMillis();
         Vec3d v4 = mc.field_1773.method_19418().method_71156();
         Vec3d v5 = mc.field_1724.method_30950(e.getTickDelta()).method_1031(0.0, 1.1, 0.0);
         MatrixStack v6 = e.getMatrixStack();
         Matrix4f v7 = v6.method_23760().method_23761();
         Immediate v8 = VertexConsumerProvider.method_22991(this.f639);
         if (this.trails.getValue()) {
            VertexConsumer v9 = v8.method_73477(BLADE_LAYER);

            for (int v10 = 0; v10 < this.list.size(); v10++) {
               ri.si v11 = this.list.get(v10);
               if (v11.trail.size() >= 3) {
                  int v12 = up.fade(v10 * (61) + 20);
                  this.m673(v9, v7, v4, v11, v12);
               }
            }

            v8.method_22994(BLADE_LAYER);
         }

         float[] v22 = new float[]{0.115F, 0.07F, 0.028F};
         int[] v23 = new int[]{70, 175, 225};

         for (int v24 = 0; v24 < 3; v24++) {
            VertexConsumer v26 = v8.method_73477(BLADE_LAYER);

            for (int v13 = 0; v13 < this.list.size(); v13++) {
               ri.si v14 = this.list.get(v13);
               ri.ti v15 = this.m671(v14, v5, v2);
               float v16 = v14.state == 1 ? 1.35F : 1.0F;
               int v17 = up.fade(v13 * (61) + 20);
               int v18;
               if (v24 == 2) {
                  v18 = up.replAlpha(
                     up.getColor(255, 255, 255, 255), v23[v24]
                  );
               } else {
                  v18 = up.replAlpha(v17, v23[v24]);
                  if (v14.flash > 0.0F) {
                     v18 = up.overCol(
                        v18,
                        up.getColor(255, 255, 255, 255),
                        v14.flash * 0.65F
                     );
                  }
               }

               this.m672(v26, v7, v4, v15, this.bladeLen.getValue() * v16, v22[v24] * (1.0F + v14.flash * 0.5F), v18);
            }

            v8.method_22994(BLADE_LAYER);
         }

         if (this.glow.getValue()) {
            VertexConsumer v25 = v8.method_73477(
               xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
            );
            Quaternionf v27 = mc.field_1773.method_19418().method_23767();

            for (int v28 = 0; v28 < this.list.size(); v28++) {
               ri.si v29 = this.list.get(v28);
               ri.ti result = this.m671(v29, v5, v2);

               ri.ti v30 = result;
               int v31 = up.fade(v28 * (61) + 20);

               Vec3d v32 = v30.f635
                  .method_1019(v30.dir.method_1021((double)(this.bladeLen.getValue() * (v29.state == 1 ? 1.35F : 1.0F))))
                  .method_1020(v4);
               this.m674(v25, v6, v27, v32, 0.17F + 0.3F * v29.flash, up.replAlpha(v31, 190));
               this.m674(
                  v25,
                  v6,
                  v27,
                  v30.f635.method_1020(v4),
                  0.11F,
                  up.replAlpha(
                     up.getColor(255, 255, 255, 255), 150
                  )
               );
               if (v29.state == 1 && v29.lock != null) {
                  float v33 = MathHelper.method_15363((float)(v2 - v29.t0) / 150.0F, 0.0F, 1.0F);
                  float v19 = (float)Math.sin((double)v33 * Math.PI);
                  this.m674(
                     v25,
                     v6,
                     v27,
                     v29.lock.method_1020(v4),
                     0.15F + 0.55F * v19,
                     up.replAlpha(
                        up.getColor(255, 255, 255, 255), (int)(230.0F * v19)
                     )
                  );
               }
            }

            v8.method_22994(
               xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
            );
         }
      }
   }

   private void m668() {
      int v1 = this.blades.getValue().intValue();

      while (this.list.size() < v1) {
         List result = this.list;
         ri.si temp1 = new ri.si;
         double temp3 = (double)this.list.size();
         double temp4 = (Math.PI * 2) / (double)Math.max(1, v1);

         temp1./* $VF: Unable to resugar constructor */<init>(temp3 * temp4);
         result.add(temp1);
      }

      while (this.list.size() > v1) {
         this.list.remove(this.list.size() - 1);
      }

      for (int v2 = 0; v2 < this.list.size(); v2++) {
         ri.si v5 = this.list.get(v2);
         double v6 = (double)v2 * ((Math.PI * 2) / (double)Math.max(1, v1));

         v5.phase = v6;
      }
   }

   private Vec3d m667() {
      Vec3d result = mc.field_1724.method_73189().method_1031(0.0, 1.1, 0.0);

      return result;
   }

   private void m674(VertexConsumer buf, MatrixStack matrices, Quaternionf camRot, Vec3d rel, float size, int color) {
      if ((color >> 24 & 255) > 0) {
         matrices.method_22903();
         matrices.method_22904(rel.field_1352, rel.field_1351, rel.field_1350);
         matrices.method_22907(camRot);
         Matrix4f v7 = matrices.method_23760().method_23761();
         VertexConsumer result = buf.method_22918(v7, -size, -size, 0.0F)
            .method_39415(color)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444);
         int temp1 = 15728880;

         result.method_60803(temp1).method_22914(0.0F, 0.0F, 1.0F);
         float temp3 = -size;

         buf.method_22918(v7, size, temp3, 0.0F)
            .method_39415(color)
            .method_22913(1.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         result = buf.method_22918(v7, size, size, 0.0F).method_39415(color);

         result.method_22913(1.0F, 0.0F).method_22922(OverlayTexture.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(v7, -size, size, 0.0F)
            .method_39415(color)
            .method_22913(0.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         matrices.method_22909();
      }
   }

   @Override
   protected void onDisable() {
      this.list.clear();

      this.f636 = 0.0;
      super.onDisable();
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.list.clear();
   }

   private ri.ti m671(ri.si b, Vec3d center, long now) {
      if (b.state == 0) {
         return new ri.ti(this.m669(b, center), this.m670(b));
      } else if (b.state == 1) {
         float v12 = MathHelper.method_15363((float)(now - b.t0) / 150.0F, 0.0F, 1.0F);
         Vec3d v13 = b.from.method_35590(b.lock, (double)(v12 * v12));

         Vec3d v14 = b.lock.method_1020(b.from);
         if (v14.method_1027() < 1.0E-6) {
            v14 = this.m670(b);
         }

         return new ri.ti(v13, v14.method_1029());
      } else {
         float v5 = MathHelper.method_15363((float)(now - b.t0) / 380.0F, 0.0F, 1.0F);
         float v6 = 1.0F - (1.0F - v5) * (1.0F - v5);
         Vec3d v7 = this.m669(b, center);
         Vec3d v8 = b.from.method_35590(v7, (double)v6);
         Vec3d v9 = v7.method_1020(b.from);
         if (v9.method_1027() < 1.0E-6) {
            v9 = this.m670(b);
         }

         ri.ti result = new ri.ti;

         result./* $VF: Unable to resugar constructor */<init>(v8, v9.method_1029());
         return result;
      }
   }

   private void m673(VertexConsumer buf, Matrix4f m, Vec3d cam, ri.si b, int calm) {
      Vec3d[] v6 = b.trail.toArray(new Vec3d[0]);
      int v7 = v6.length;
      Vec3d[] v8 = new Vec3d[v7];
      Vec3d[] v9 = new Vec3d[v7];
      Vec3d v10 = new Vec3d(0.0, 1.0, 0.0);

      for (int v11 = 0; v11 < v7; v11++) {
         v8[v11] = v6[v11].method_1020(cam);
      }

      for (int v19 = 0; v19 < v7; v19++) {
         Vec3d v12 = v8[Math.min(v19 + 1, v7 - 1)].method_1020(v8[Math.max(v19 - 1, 0)]);
         Vec3d v13 = v12.method_1036(v8[v19]);
         v9[v19] = v10 = v13.method_1027() < 1.0E-8 ? v10 : v13.method_1029();
      }

      for (int v20 = 0; v20 < v7 - 1; v20++) {
         float v21 = (float)v20 / (float)(v7 - 1);
         float v22 = (float)(v20 + 1) / (float)(v7 - 1);
         int v14 = up.replAlpha(calm, (int)(95.0F * v21 * v21));
         int v15 = up.replAlpha(calm, (int)(95.0F * v22 * v22));
         Vec3d v16 = v9[v20].method_1021(0.012 + 0.05 * (double)v21);
         Vec3d v17 = v9[v20 + 1].method_1021(0.012 + 0.05 * (double)v22);

         buf.method_22918(
               m,
               (float)(v8[v20].field_1352 - v16.field_1352),
               (float)(v8[v20].field_1351 - v16.field_1351),
               (float)(v8[v20].field_1350 - v16.field_1350)
            )
            .method_39415(v14);
         buf.method_22918(
               m,
               (float)(v8[v20 + 1].field_1352 - v17.field_1352),
               (float)(v8[v20 + 1].field_1351 - v17.field_1351),
               (float)(v8[v20 + 1].field_1350 - v17.field_1350)
            )
            .method_39415(v15);
         buf.method_22918(
               m,
               (float)(v8[v20 + 1].field_1352 + v17.field_1352),
               (float)(v8[v20 + 1].field_1351 + v17.field_1351),
               (float)(v8[v20 + 1].field_1350 + v17.field_1350)
            )
            .method_39415(v15);
         buf.method_22918(
               m,
               (float)(v8[v20].field_1352 + v16.field_1352),
               (float)(v8[v20].field_1351 + v16.field_1351),
               (float)(v8[v20].field_1350 + v16.field_1350)
            )
            .method_39415(v14);
      }
   }

   private Vec3d m670(ri.si b) {
      double v2 = this.f636 + b.phase;
      Vec3d result = new Vec3d;

      result./* $VF: Unable to resugar constructor */<init>(-Math.sin(v2), 0.1, Math.cos(v2));
      return result.method_1029();
   }

   @Environment(EnvType.CLIENT)
   private static final class si {
      float flash;
      Vec3d lock;
      final Deque<Vec3d> trail;
      int state = 0;
      double phase;
      Vec3d from;
      long t0 = 0L;

      si(double phase) {
         this.flash = 0.0F;
         this.from = Vec3d.field_1353;
         this.lock = Vec3d.field_1353;
         this.trail = new ArrayDeque<>();
         this.phase = phase;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static record ti(Vec3d hilt, Vec3d dir) {
      private final Vec3d f635;

      public Vec3d dir() {

         return this.dir;
      }

      @Override
      public final 

         return ObjectMethods.bootstrap<"hashCode",ri.ti,"hilt;dir",ri.ti::f635,ri.ti::dir>(this);
      }

      public Vec3d hilt() {
         return this.f635;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",ri.ti,"hilt;dir",ri.ti::f635,ri.ti::dir>(this, o);
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",ri.ti,"hilt;dir",ri.ti::f635,ri.ti::dir>(this);

         return result;
      }

      private ti(Vec3d hilt, Vec3d dir) {
         this.f635 = hilt;
         this.dir = dir;
      }

      
   }
}
