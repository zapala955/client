package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
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
import ru.white.module.impl.combat.AntiBot;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Gaze Threads",
   desc = "Светящиеся нити от тех, кто смотрит на тебя",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class GazeThreads extends Module {
   private float f663;
   public final BooleanSetting friends;
   public final SliderSetting range = new SliderSetting(this, "Дальность", 32.0F, 8.0F, 64.0F, 1.0F);
   public final BooleanSetting glow;
   private long f662;
   private final Map<UUID, kj.lj> f660;
   private final BufferAllocator f661;
   private static final RenderLayer RIBBON_LAYER = RenderLayer.method_75940(
      "gaze_threads_ribbon",
      RenderSetup.method_75927(kj.RIBBON_PIPELINE).method_75929(16384).method_75938()
   );
   public final SliderSetting width;
   public final SliderSetting cone = new SliderSetting(this, "Конус взгляда", 25.0F, 10.0F, 60.0F, 1.0F);
   public final BooleanSetting warn;
   public final SliderSetting flowSpeed;
   private static final int MAX_THREADS = 8;
   public final BooleanSetting los;
   private static final int POINTS = 22;
   private static final RenderPipeline RIBBON_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "gaze_threads_ribbon"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.f660.isEmpty()) {
         float v2 = e.getTickDelta();
         long v3 = System.currentTimeMillis();
         Vec3d v5 = mc.field_1773.method_19418().method_71156();
         Vec3d v6 = mc.field_1724.method_30950(v2).method_1031(0.0, (double)mc.field_1724.method_5751(), 0.0);
         ArrayList v7 = new ArrayList();

         for (Entity v9 : mc.field_1687.method_18112()) {
            if (v9 instanceof PlayerEntity) {
               PlayerEntity v10 = (PlayerEntity)v9;
               kj.lj v11 = this.f660.get(v10.method_5667());
               if (v11 != null && !(v11.factor < 0.05F)) {
                  Vec3d v12 = v10.method_30950(v2).method_1031(0.0, (double)v10.method_5751(), 0.0);
                  v7.add(new kj.mj(v12, v11.factor, v11.index));
                  if (v7.size() >= 8) {
                     break;
                  }
               }
            }
         }

         if (!v7.isEmpty()) {
            v7.sort((a, b) -> {

               return Float.compare(b.f658, a.f658);
            });
            MatrixStack v36 = e.getMatrixStack();
            Matrix4f v37 = v36.method_23760().method_23761();
            Immediate v38 = VertexConsumerProvider.method_22991(this.f661);
            double v39 = Math.tan(Math.toRadians((double)((Integer)mc.field_1690.method_41808().method_41753()).floatValue()) / 2.0);
            double v13 = 2.0 * v39 / (double)Math.max(1, mc.method_22683().method_4506());
            float v15 = this.width.getValue();
            VertexConsumer v16 = v38.method_73477(RIBBON_LAYER);

            for (int v17 = 0; v17 < v7.size(); v17++) {
               kj.mj v18 = (kj.mj)v7.get(v17);
               List v19 = this.m735(v18.f657, v6, v17, v3);
               int v20 = v19.size();
               Vec3d[] v21 = new Vec3d[v20];
               Vec3d[] v22 = new Vec3d[v20];
               Vec3d v23 = new Vec3d(0.0, 1.0, 0.0);

               for (int v24 = 0; v24 < v20; v24++) {
                  v21[v24] = ((Vec3d)v19.get(v24)).method_1020(v5);
                  Vec3d v25 = ((Vec3d)v19.get(Math.min(v24 + 1, v20 - 1))).method_1020((Vec3d)v19.get(Math.max(v24 - 1, 0)));
                  Vec3d v26 = v25.method_1036(v21[v24]);
                  v22[v24] = v23 = v26.method_1027() < 1.0E-8 ? v23 : v26.method_1029();
               }

               int v49 = up.fade(v17 * (53) + 10);
               int v51 = up.getColor(255, 60, 60, 255);

               for (int v53 = 0; v53 < v20 - 1; v53++) {
                  float v27 = (float)v53 / (float)(v20 - 1);
                  float v28 = 0.7F
                     + 0.3F * (float)Math.sin((double)v27 * Math.PI * 6.0 - (double)v3 / 1000.0 * 4.0 * (double)this.flowSpeed.getValue().floatValue());
                  int v29 = m737(v49, v51, v18.f658, v28);
                  double temp5 = (double)(v27 + 1.0F / (float)(v20 - 1)) * Math.PI;

                  int v30 = m737(
                     v49,
                     v51,
                     v18.f658,
                     0.7F + 0.3F * (float)Math.sin(temp5 * 6.0 - (double)v3 / 1000.0 * 4.0 * (double)this.flowSpeed.getValue().floatValue())
                  );
                  float v31 = (float)(0.5 * (double)v15 * (0.4 + 0.6 * (double)v18.f658) * v21[v53].method_1033() * v13);
                  float v32 = (float)(0.5 * (double)v15 * (0.4 + 0.6 * (double)v18.f658) * v21[v53 + 1].method_1033() * v13);
                  Vec3d v33 = v22[v53].method_1021((double)v31);
                  Vec3d v34 = v22[v53 + 1].method_1021((double)v32);
                  m738(v16, v37, v21[v53].method_1020(v33), v29);
                  m738(v16, v37, v21[v53 + 1].method_1020(v34), v30);
                  m738(v16, v37, v21[v53 + 1].method_1019(v34), v30);
                  m738(v16, v37, v21[v53].method_1019(v33), v29);
               }
            }

            v38.method_22994(RIBBON_LAYER);
            if (this.glow.getValue()) {
               VertexConsumer v40 = v38.method_73477(
                  xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
               );
               Quaternionf v41 = mc.field_1773.method_19418().method_23767();

               for (int v42 = 0; v42 < v7.size(); v42++) {
                  kj.mj v43 = (kj.mj)v7.get(v42);
                  List v44 = this.m735(v43.f657, v6, v42, v3);

                  for (int v45 = 0; v45 < 2; v45++) {
                     float v47 = (float)(
                        ((double)v3 / 1000.0 * 0.35 * (double)this.flowSpeed.getValue().floatValue() + (double)v45 * 0.5 + (double)v42 * 0.13) % 1.0
                     );
                     Vec3d v50 = this.m736(v44, v47).method_1020(v5);
                     int v52 = up.fade(v42 * (53) + 10);
                     int v54 = up.replAlpha(
                        up.overCol(
                           v52, up.getColor(255, 70, 70, 255), v43.f658
                        ),
                        (int)(200.0F * v43.f658)
                     );
                     this.m739(v40, v36, v41, v50, 0.09F + 0.06F * v43.f658, v54);
                  }

                  Vec3d v46 = v43.f657.method_1020(v5);
                  int v48 = up.replAlpha(
                     up.overCol(
                        up.fade(v42 * (53) + 10),
                        up.getColor(255, 70, 70, 255),
                        v43.f658
                     ),
                     (int)(120.0F * v43.f658)
                  );
                  this.m739(v40, v36, v41, v46, 0.22F + 0.1F * v43.f658, v48);
               }

               v38.method_22994(
                  xd.ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"))
               );
            }
         }
      }
   }

   private static int m737(int calm, int alarm, float factor, float flow) {
      int v4 = up.overCol(calm, alarm, MathHelper.method_15363(factor, 0.0F, 1.0F));
      int v5 = v4 >> 16 & 255;
      int result = v4 >> 8;

      int v6 = result & 255;
      int v7 = v4 & 255;
      float v12 = MathHelper.method_15363(flow, 0.3F, 1.0F);
      float temp1 = 0.35F + 0.65F * factor;

      float v8 = v12 * temp1;
      return (int)(200.0F * v8) << 24 | v5 << 16 | v6 << 8 | v7;
   }

   private void m739(VertexConsumer buf, MatrixStack matrices, Quaternionf camRot, Vec3d rel, float size, int color) {
      if ((color >> 24 & 255) > 0) {
         matrices.method_22903();
         matrices.method_22904(rel.field_1352, rel.field_1351, rel.field_1350);
         matrices.method_22907(camRot);
         Matrix4f v7 = matrices.method_23760().method_23761();
         buf.method_22918(v7, -size, -size, 0.0F)
            .method_39415(color)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         VertexConsumer result = buf.method_22918(v7, size, -size, 0.0F);

         result.method_39415(color)
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

   @Override
   protected void onDisable() {
      this.f660.clear();
      this.f663 = 0.0F;
      super.onDisable();
   }

   public GazeThreads() {
      this.width = new SliderSetting(this, "Толщина нитей", 2.0F, 1.0F, 4.0F, 0.5F);
      this.flowSpeed = new SliderSetting(this, "Скорость потока", 1.2F, 0.2F, 3.0F, 0.1F);
      this.glow = new BooleanSetting(this, "Свечение", true);
      this.friends = new BooleanSetting(this, "Нити друзей", false);
      this.los = new BooleanSetting(this, "Только прямая видимость", true);
      this.warn = new BooleanSetting(this, "Предупреждение", true);
      this.f660 = new LinkedHashMap<>();
      this.f661 = new BufferAllocator(65536);
      this.f662 = 0L;
      this.f663 = 0.0F;
   }

   private Vec3d m736(List<Vec3d> pts, float t) {

      float v3 = MathHelper.method_15363(t, 0.0F, 1.0F) * (float)(pts.size() - 1);
      int result = Math.min(pts.size() - 2, (int)v3);

      int v4 = result;

      return ((Vec3d)pts.get(v4)).method_35590((Vec3d)pts.get(v4 + 1), (double)(v3 - (float)v4));
   }

   private List<Vec3d> m735(Vec3d from, Vec3d to, int index, long now) {
      Vec3d v6 = from.method_35590(to, 0.5);
      double v7 = from.method_1022(to);
      double v9 = v7 * 0.16;
      double v11 = Math.sin((double)now / 700.0 + (double)index * 1.7) * v7 * 0.06;
      Vec3d v13 = to.method_1020(from).method_1029();
      Vec3d v14 = v13.method_1036(new Vec3d(0.0, 1.0, 0.0));
      if (v14.method_1027() < 1.0E-6) {
         Vec3d result = new Vec3d;

         result./* $VF: Unable to resugar constructor */<init>(1.0, 0.0, 0.0);
         v14 = result;
      }

      v14 = v14.method_1029();
      Vec3d v15 = v6.method_1031(0.0, v9, 0.0).method_1019(v14.method_1021(v11));
      ArrayList v16 = new ArrayList(23);

      for (int v17 = 0; v17 <= 22; v17++) {
         float v18 = (float)v17 / 22.0F;
         float v19 = 1.0F - v18;
         v16.add(
            new Vec3d(
               (double)(v19 * v19) * from.field_1352 + (double)(2.0F * v19 * v18) * v15.field_1352 + (double)(v18 * v18) * to.field_1352,
               (double)(v19 * v19) * from.field_1351 + (double)(2.0F * v19 * v18) * v15.field_1351 + (double)(v18 * v18) * to.field_1351,
               (double)(v19 * v19) * from.field_1350 + (double)(2.0F * v19 * v18) * v15.field_1350 + (double)(v18 * v18) * to.field_1350
            )
         );
      }

      return v16;
   }

   private static void m738(VertexConsumer buf, Matrix4f m, Vec3d v, int color) {
      VertexConsumer result = buf.method_22918(m, (float)v.field_1352, (float)v.field_1351, (float)v.field_1350);

      result.method_39415(color);

      
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (this.warn.getValue() && mc.field_1724 != null && !this.f660.isEmpty()) {
         float v2 = 0.0F;

         for (kj.lj v5 : this.f660.values()) {
            if (v5.factor > 0.25F) {
               v3++;
            }

            v2 = Math.max(v2, v5.factor);
         }

         if (!(v2 < 0.55F) && v3 != 0) {
            this.f663 = Math.min(1.0F, this.f663 + 0.08F);
            float v15 = 0.75F + 0.25F * (float)Math.sin((double)System.currentTimeMillis() / 180.0);
            String v16 = "ЗАМЕЧЕН: " + v3;
            float v6 = 9.0F;
            float v7 = hu.sf_regular.getWidth(v16, v6) + 16.0F;
            float v8 = (float)mc.method_22683().method_4486() / 2.0F;
            float v9 = (float)mc.method_22683().method_4502() * 0.22F;
            int v10 = (int)(210.0F * this.f663 * v15);
            float result = v8 - v7 / 2.0F;

            vs.ys.rect(result, v9, v7, v6 + 8.0F, up.getColor(20, 5, 5, (int)(150.0F * this.f663)), 4.0F);
            float temp2 = v8 - v7 / 2.0F + 8.0F;
            float temp3 = v9 + 4.0F;

            int temp5 = 255;

            hu.sf_regular.draw(v16, temp2, temp3, v6, up.getColor(temp5, 90, 90, v10));
         } else {
            this.f663 = Math.max(0.0F, this.f663 - 0.05F);
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         float v4 = this.f662 == 0L ? 0.05F : Math.min(0.25F, (float)(v2 - this.f662) / 1000.0F);
         this.f662 = v2;
         Vec3d v5 = mc.field_1724.method_33571();

         float v6 = Math.min(1.0F, v4 * 6.0F);
         Iterator v7 = mc.field_1687.method_18112().iterator();

         while (true) {
            while (true) {
               if (!v7.hasNext()) {
                  v7 = this.f660.entrySet().iterator();

                  while (v7.hasNext()) {
                     Entry v24 = (Entry)v7.next();
                     kj.lj v25 = (kj.lj)v24.getValue();
                     if (v2 - v25.seen > -4340248757239283715L + 4340248757239284215L) {
                        v25.factor = v25.factor * Math.max(0.0F, 1.0F - v4 * 4.0F);
                        if (v25.factor < 0.02F && v2 - v25.seen > -8902226739528108434L + 8902226739528110434L) {
                           v7.remove();
                        }
                     }
                  }

                  return;
               }

               Entity v8 = (Entity)v7.next();
               if (v8 instanceof PlayerEntity v9
                  && v9 != mc.field_1724
                  && !(v9.method_5858(mc.field_1724) > (double)(this.range.getValue() * this.range.getValue()))
                  && v9.method_5805()
                  && !v9.method_7325()) {
                  try {
                     if (eg.getInstance().isBot(v9)) {
                        continue;
                     }
                  } catch (Exception v22) {
                  }
                  break;
               }
            }

            if (this.friends.getValue() || !Client.get().friendManager().isFriend(v9.method_5477().getString())) {
               Vec3d v10 = v9.method_33571();
               Vec3d v11 = v9.method_5828(1.0F).method_1029();
               Vec3d v12 = v5.method_1020(v10);
               double v13 = v12.method_1033();
               if (!(v13 < 0.001)) {
                  v12 = v12.method_1029();
                  double v15 = MathHelper.method_15350(v11.method_1026(v12), -1.0, 1.0);
                  float v17 = (float)Math.toDegrees(Math.acos(v15));
                  float v18 = v17 < this.cone.getValue() ? 1.0F - v17 / this.cone.getValue() : 0.0F;
                  if (v18 > 0.0F && this.los.getValue()) {

                     if (!v9.method_6057(mc.field_1724)) {
                        v18 = 0.0F;
                     }
                  }

                  kj.lj v19 = this.f660.computeIfAbsent(v9.method_5667(), u -> {
                     kj.lj var10000x = new kj.lj;

                     var10000x./* $VF: Unable to resugar constructor */<init>();
                     return var10000x;
                  });
                  v19.factor = v19.factor + (v18 - v19.factor) * v6;
                  v19.seen = v2;
                  v19.index = v9.method_5628();
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class lj {
      float factor = 0.0F;
      long seen = 0L;
      int index = 0;

      
   }

   @Environment(EnvType.CLIENT)
   private static record mj(Vec3d from, float factor, int index) {
      private final int f659;
      private final float f658;
      private final Vec3d f657;

      public 

         return this.f659;
      }

      private mj(Vec3d from, float factor, int index) {
         this.f657 = from;
         this.f658 = factor;
         this.f659 = index;
      }

      public Vec3d from() {

         return this.f657;
      }

      @Override
      public final String toString() {

         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return ObjectMethods.bootstrap<"toString",kj.mj,"from;factor;index",kj.mj::f657,kj.mj::f658,kj.mj::f659>(this);
      }

      public float factor() {

         return this.f658;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",kj.mj,"from;factor;index",kj.mj::f657,kj.mj::f658,kj.mj::f659>(this, o);
      }

      
   }
}
