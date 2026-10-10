package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.OtherClientPlayerEntity;
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
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Kill Effect",
   desc = "Soft beam on killed target",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class KillEffect extends Module implements jf {
   private final BufferAllocator f704;
   public ModeSetting typeColor;
   private int f709;
   private final lf f702;
   public SliderSetting duration;
   public SliderSetting beamWidth;
   private int f707;
   private final List<ak.bk> f703;
   public tf tintColor;
   public SliderSetting beamHeight;
   private long f708;
   private static final long DUPLICATE_WINDOW_MS = 750L;
   private static final RenderLayer BEAM_LAYER = RenderLayer.method_75940(
      "kill_effect_beam",
      RenderSetup.method_75927(ak.BEAM_PIPELINE).method_75929(32768).method_75938()
   );
   private static final byte DEATH_STATUS = 3;
   private long f710;
   private static final long KILL_WINDOW_MS = 6500L;
   public sf previewButton = lf.button(this);
   private static final RenderPipeline BEAM_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "kill_effect_beam"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private ak.ck f706;
   private LivingEntity f705;

   @Override
   public lf previewSettings() {
      int temp2 = v1 + 1;

      return this.f702;
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.m784();
   }

   @Override
   public boolean previewNeedsDummy() {

      return true;
   }

   private static void m780(
      VertexConsumer buf, Matrix4f mat, float x, float y, float z, float h, float halfWidth, float spin, int r, int g, int b, int alpha, int petals
   ) {
      if (alpha > 0 && !(h <= 0.01F)) {
         for (int v13 = 0; v13 < petals; v13++) {
            float v14 = spin + (float)v13 / (float)petals * (float) (Math.PI * 2);
            float v15 = MathHelper.method_15362((double)v14) * halfWidth;
            float v16 = MathHelper.method_15374((double)v14) * halfWidth;

            int v17 = Math.max(0, alpha / 3);
            buf.method_22918(mat, x - v15, y, z - v16).method_1336(r, g, b, v17);
            buf.method_22918(mat, x, y + h * 0.08F, z).method_1336(r, g, b, alpha);

            buf.method_22918(mat, x, y + h, z).method_1336(r, g, b, 0);
            buf.method_22918(mat, x + v15, y, z + v16).method_1336(r, g, b, v17);
         }
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (!this.f703.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         long v4 = (long)(this.duration.getValue() * 1000.0F);
         int v6 = this.getColor();
         int v7 = v6 >> 16 & 255;
         int v8 = v6 >> 8 & 255;
         int result = v6 & 255;

         int v9 = result;
         MatrixStack v10 = e.getMatrixStack();
         Matrix4f v11 = v10.method_23760().method_23761();
         Vec3d v12 = mc.field_1773.method_19418().method_71156();
         Iterator v13 = this.f703.iterator();

         while (true) {
            boolean v26 = v13.hasNext();

            if (!v26) {
               if (this.f703.isEmpty()) {
                  return;
               }

               Immediate v22 = VertexConsumerProvider.method_22991(this.f704);
               VertexConsumer v23 = v22.method_73477(BEAM_LAYER);
               float v24 = 0.9F
                  + 0.1F * MathHelper.method_15374((double)((float)(v2 % (-8245972403823041910L + 8245972403823043510L)) / 1600.0F * (float) (Math.PI * 2)));

               for (ak.bk v18 : this.f703) {
                  this.m779(v23, v11, v12, v18, v7, v8, v9, v24);
               }

               v22.method_22993();
               return;
            }

            ak.bk v14 = (ak.bk)v13.next();
            float v15 = (float)(v2 - v14.start) / (float)v4;

            if (v15 >= 1.0F) {
               v13.remove();
            } else {
               float v16 = MathHelper.method_15363(v15 * 5.5F, 0.0F, 1.0F);
               float v17 = v15 > 0.5F ? 1.0F - (v15 - 0.5F) / 0.5F : 1.0F;
               v14.alpha = m783(v16) * m783(v17);
               v14.progress = v15;
            }
         }
      }
   }

   @Override
   public void previewSpawn(kf ctx) {
      OtherClientPlayerEntity v2 = ctx.dummy();
      ak.ck result;
      if (v2 != null) {

         result = this.point(v2);
      } else {
         result = new ak.ck(ctx.anchor(), 0.65F);
      }

      ak.ck v3 = result;
      List v7 = this.f703;
      ak.bk temp1 = new ak.bk(v3);

      v7.add(temp1);
   }

   private static void m781(VertexConsumer buf, Matrix4f mat, float x, float y, float z, float radius, int r, int g, int b, int alpha) {
      if (alpha > 0 && !(radius <= 0.01F)) {

         for (int v11 = 0; v11 < v10; v11++) {
            float v12 = (float)v11 / (float)v10 * (float) (Math.PI * 2);
            float v13 = (float)(v11 + 1) / (float)v10 * (float) (Math.PI * 2);
            buf.method_22918(mat, x, y, z).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x + MathHelper.method_15362((double)v12) * radius, y, z + MathHelper.method_15374((double)v12) * radius)
               .method_1336(r, g, b, 0);
            buf.method_22918(mat, x + MathHelper.method_15362((double)v13) * radius, y, z + MathHelper.method_15374((double)v13) * radius)
               .method_1336(r, g, b, 0);

            buf.method_22918(mat, x, y, z).method_1336(r, g, b, alpha);
         }
      }
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket v2) {
         if (v2.method_11470() == 3) {

            if (mc.field_1687 != null) {
               if (v2.method_11469(mc.field_1687) instanceof LivingEntity v4) {
                  this.m778(v4, false);
                  return;
               }

               return;
            }
         }
      }
   }

   private void m778(LivingEntity entity, boolean allowCachedPoint) {
      long v3 = System.currentTimeMillis();
      if (entity.method_5628() == this.f707 && v3 - this.f708 <= 7858280024565190591L + -7858280024565184091L) {
         if (this.f709 != entity.method_5628() || v3 - this.f710 >= -3053608419792454506L + 3053608419792455256L) {
            ak.ck v7;
            label24: {
               if (allowCachedPoint) {

                  if (this.f706 != null) {
                     v7 = this.f706;
                     break label24;
                  }
               }

               v7 = this.point(entity);
            }

            ak.ck v5 = v7;
            this.f703.add(new ak.bk(v5));
            this.f709 = entity.method_5628();
            this.f710 = v3;
            this.f705 = null;
            this.f706 = null;
         }
      }
   }

   @Override
   protected void onDisable() {

      this.m784();
   }

   public KillEffect() {
      this.typeColor = new ModeSetting(this, "Color mode", "Theme", "Custom");
      this.tintColor = new tf(this, "Color", -16711681).setVisible(() -> {
         Boolean result = this.typeColor.is("Custom");

         return result;
      });
      this.duration = new SliderSetting(this, "Duration", 2.2F, 1.0F, 5.0F, 0.1F);
      this.beamHeight = new SliderSetting(this, "Beam height", 5.2F, 1.5F, 12.0F, 0.5F);
      this.beamWidth = new SliderSetting(this, "Beam width", 0.42F, 0.08F, 1.2F, 0.02F);
      this.f702 = lf.of(this, 4.0F, 0.0F, 2.5F);
      this.f703 = new ArrayList<>();
      this.f704 = new BufferAllocator(32768);
      this.f707 = -1;
      this.f709 = -1;
   }

   private void m784() {

      this.f703.clear();
      this.f705 = null;
      this.f706 = null;
      this.f707 = -1;
      this.f709 = -1;
   }

   private static void m782(VertexConsumer buf, Matrix4f mat, float x, float y, float z, float radius, float width, int r, int g, int b, int alpha) {
      if (alpha > 0 && !(radius <= width)) {

         for (int v12 = 0; v12 < v11; v12++) {
            float v13 = (float)v12 / (float)v11 * (float) (Math.PI * 2);
            float v14 = (float)(v12 + 1) / (float)v11 * (float) (Math.PI * 2);
            float v15 = MathHelper.method_15362((double)v13);
            float v16 = MathHelper.method_15374((double)v13);
            float v17 = MathHelper.method_15362((double)v14);
            float v18 = MathHelper.method_15374((double)v14);
            VertexConsumer result = buf.method_22918(mat, x + v15 * (radius - width), y, z + v16 * (radius - width));

            result.method_1336(r, g, b, 0);

            buf.method_22918(mat, x + v15 * radius, y, z + v16 * radius).method_1336(r, g, b, alpha);
            buf.method_22918(mat, x + v17 * radius, y, z + v18 * radius).method_1336(r, g, b, alpha);

            buf.method_22918(mat, x + v17 * (radius - width), y, z + v18 * (radius - width)).method_1336(r, g, b, 0);
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (this.f705 != null) {
         long result = System.currentTimeMillis() - this.f708;

         long v6;
         int v5 = (v6 = result - (-1662688515931769441L + 1662688515931775941L)) == 0L ? 0 : (v6 < 0L ? -1 : 1);

         if (v5 > 0) {
            this.f705 = null;
            this.f706 = null;
         } else {
            if (!this.f705.method_5805() || this.f705.method_31481() || this.f705.method_6032() <= 0.0F) {
               this.m778(this.f705, true);
            }
         }
      }
   }

   @Override
   public void previewStop() {

      int temp2 = v1 + 1;

      this.m784();
   }

   @EventTarget
   public void onAttack(Event e) {
      Entity v3 = e.getTarget();
      if (v3 instanceof LivingEntity) {

         LivingEntity v2 = (LivingEntity)v3;
         if (v2 != mc.field_1724) {
            this.f705 = v2;
            this.f707 = v2.method_5628();
            this.f708 = System.currentTimeMillis();

            this.f706 = this.point(v2);
            return;
         }
      }
   }

   private static float m783(float t) {
      t = MathHelper.method_15363(t, 0.0F, 1.0F);
      float result = t * t;

      return result * (3.0F - 2.0F * t);
   }

   private ak.ck point(LivingEntity entity) {
      ak.ck result = new ak.ck;

      result./* $VF: Unable to resugar constructor */<init>(entity.method_73189(), Math.max(0.65F, entity.method_17681()));
      return result;
   }

   public int getColor() {
      if (this.typeColor.is("Theme")) {
         return up.getClientColor1(1);
      } else {
         int result = this.tintColor.getValue();

         return result;
      }
   }

   private void m779(VertexConsumer buf, Matrix4f mat, Vec3d cam, ak.bk beam, int r, int g, int b, float pulse) {
      if (!(beam.alpha <= 0.01F)) {
         float v9 = m783(Math.min(beam.progress * 3.5F, 1.0F));
         float v10 = this.beamHeight.getValue() * v9;
         float v11 = this.beamWidth.getValue() * beam.radius;
         float v12 = (float)(beam.x - cam.field_1352);
         float v13 = (float)(beam.y - cam.field_1351 + 0.035F);
         float v14 = (float)(beam.z - cam.field_1350);
         float v15 = (float)(System.currentTimeMillis() - beam.start) / 1000.0F * 0.85F;
         float v16 = 150.0F * beam.alpha * pulse;
         float v17 = 54.0F * beam.alpha;
         float var10006 = v11 * 0.72F;

         m780(buf, mat, v12, v13, v14, v10, var10006, v15, r, g, b, (int)v16, 6);
         m780(buf, mat, v12, v13, v14, v10 * 0.82F, v11 * 1.28F, -v15 * 0.55F, r, g, b, (int)v17, 8);
         float v18 = 0.55F + 0.45F * v9;
         m781(buf, mat, v12, v13 + 0.01F, v14, v11 * 2.25F * v18, r, g, b, (int)(42.0F * beam.alpha));
         m781(buf, mat, v12, v13 + v10 * 0.98F, v14, v11 * 0.92F * beam.alpha, r, g, b, (int)(28.0F * beam.alpha));

         for (int v19 = 0; v19 < 3; v19++) {
            float v20 = (beam.progress + (float)v19 * 0.22F) % 1.0F;
            float v21 = v11 * (1.05F + v20 * 2.25F);
            int v22 = (int)(52.0F * beam.alpha * (1.0F - v20));
            m782(buf, mat, v12, v13 + (float)v19 * 0.025F, v14, v21, 0.035F + v20 * 0.045F, r, g, b, v22);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static class bk {
      final float radius;
      float progress;
      final double z;
      final long start;
      final double y;
      final double x;
      float alpha;

      bk(ak.ck point) {
         this.x = point.f700.field_1352;
         this.y = point.f700.field_1351;
         this.z = point.f700.field_1350;
         this.radius = point.f701;
         this.start = System.currentTimeMillis();
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static record ck(Vec3d pos, float radius) {
      private final float f701;
      private final Vec3d f700;

      private ck(Vec3d pos, float radius) {
         this.f700 = pos;
         this.f701 = radius;
      }

      public float radius() {
         return this.f701;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",ak.ck,"pos;radius",ak.ck::f700,ak.ck::f701>(this, o);
      }

      public Vec3d pos() {

         return this.f700;
      }

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",ak.ck,"pos;radius",ak.ck::f700,ak.ck::f701>(this);
      }

      
   }
}
