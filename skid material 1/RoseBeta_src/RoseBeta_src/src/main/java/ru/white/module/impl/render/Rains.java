package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.Heightmap.Type;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.ut;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Rains",
   category = Category.RENDER,
   desc = "Realistic volumetric rain"
)
@Environment(EnvType.CLIENT)
public class Rains extends Module {
   private float f753;
   private final ModeSetting f734;
   private final BooleanSetting f729;
   private static final int MAX_MIST = 90;
   private final SliderSetting f722;
   private final SliderSetting f726;
   private final BooleanSetting f732;
   private final List<ik.kk> f747;
   private final SliderSetting f723;
   private final Mutable f750;
   private final BufferAllocator f744;
   private final List<ik.jk> f745;
   private final SliderSetting f725;
   private final Random f751;
   private static final RenderLayer RAIN_LAYER = RenderLayer.method_75940(
      "rains", RenderSetup.method_75927(ik.RAIN_PIPELINE).method_75929(16384).method_75938()
   );
   private static final double GRAVITY = 22.0;
   private final SliderSetting f727;
   private final SliderSetting f737;
   private final tf f735;
   private final BooleanSetting f731;
   private final ModeSetting f741;
   private int f756;
   private final BooleanSetting f730;
   private long f752;
   private final List<ik.mk> f746;
   private float f754;
   private final SliderSetting f724;
   private final SliderSetting f740;
   private final BooleanSetting f728;
   private final SliderSetting f743;
   private static final RenderPipeline RAIN_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/rains"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final double TERMINAL_VELOCITY = 34.0;
   private final SliderSetting opacity;
   private final BooleanSetting f733;
   private final SliderSetting f738;
   private static final int RIPPLE_SEGMENTS = 14;
   private final ModeSetting f721 = new ModeSetting(this, "Preset", "Drizzle", "Rain", "Downpour", "Storm");
   private static final int MAX_DROPS = 4200;
   private final List<ik.lk> f748;
   private float f755;
   private final BooleanSetting f736;
   private final BooleanSetting f742;
   private final Map<Long, Integer> f749;
   private final SliderSetting f739;

   private void m809(Vec3d center, float dt) {
      int v3 = this.m801();
      this.f745.removeIf(d -> {
         double v3x = d.x - center.field_1352;

         double v5x = d.z - center.field_1350;
         float v7x = this.f723.getValue() + 6.0F;
         if (!d.dead && !(v3x * v3x + v5x * v5x > (double)(v7x * v7x))) {
            double var10000x = d.y;
            double var10001x = center.field_1351;
            float var11x = this.f724.getValue();

            if (!(var10000x < var10001x - (double)var11x - 12.0)) {
               return false;
            }
         }

         return true;
      });
      boolean v4 = this.f745.size() < v3 / 2;
      int v5 = v4 ? Math.min(v3 - this.f745.size(), 900) : Math.min(v3 - this.f745.size(), 260);

      for (int v6 = 0; v6 < v5; v6++) {
         this.m808(center, !v4);
      }

      float v29 = this.f726.getValue();
      float v7 = this.f727.getValue() * this.m799();

      for (int v9 = this.f745.size(); v8 < v9; v8++) {
         ik.jk v10 = this.f745.get(v8);
         double v11 = 0.65
            + 0.35 * Math.sin((double)this.f753 * 0.55 + (double)v10.phase * 0.15)
            + 0.18 * Math.sin((double)this.f753 * 1.7 + (double)v10.phase);
         double v13 = (double)v7 * 3.1 * v11;
         double result = (double)v7 * 1.7;

         double temp1 = (double)this.f753 * 0.31 + (double)v10.phase * 0.05;

         double v15 = result * Math.sin(temp1) * v11;
         double v17 = 1.0 - Math.min(0.92, Math.abs(v10.vy) / (34.0 * v10.mass));
         v10.vy = v10.vy - 22.0 * v10.mass * v17 * (double)dt;
         if (v10.vy < -34.0 * v10.mass) {
            v10.vy = -34.0 * v10.mass;
         }

         v10.vx = v10.vx + (v13 - v10.vx) * Math.min(1.0, (double)dt * 3.2);
         v10.vz = v10.vz + (v15 - v10.vz) * Math.min(1.0, (double)dt * 3.2);
         double v19 = v10.x + v10.vx * (double)dt * (double)v29;
         double v21 = v10.y + v10.vy * (double)dt * (double)v29;
         double v23 = v10.z + v10.vz * (double)dt * (double)v29;
         v10.prevX = v10.x;
         v10.prevY = v10.y;
         v10.prevZ = v10.z;
         boolean v25 = false;
         if (v21 <= v10.ground + 2.5) {
            if (this.m805(v19, v21, v23)) {
               v25 = true;
            } else if (v21 <= v10.ground - 3.0) {
               v10.dead = true;
            }
         }

         if (v25) {
            if (this.f728.getValue()) {
               this.m810(v19, v21, v23, v10);
            }

            v10.dead = true;
         } else {
            v10.x = v19;
            v10.y = v21;
            v10.z = v23;
         }
      }
   }

   private void m813(Vec3d center, float dt) {
      if (!this.f730.getValue()) {
         this.f748.clear();
      } else {
         int v3 = (int)MathHelper.method_15363(22.0F * this.f722.getValue() * this.m798(), 6.0F, 90.0F);

         this.f748.removeIf(m -> {

            m.life += dt;
            double v4x = m.x - center.field_1352;
            double v6x = m.z - center.field_1350;
            float v8x = this.f723.getValue();
            m.x = m.x + m.vx * (double)dt;
            m.z = m.z + m.vz * (double)dt;
            return m.life > m.maxLife || v4x * v4x + v6x * v6x > (double)(v8x * v8x);
         });

         while (this.f748.size() < v3) {
            double v4 = this.f751.nextDouble() * Math.PI * 2.0;
            double v6 = Math.sqrt(this.f751.nextDouble()) * (double)this.f723.getValue().floatValue() * 0.85;
            double v8 = center.field_1352 + Math.cos(v4) * v6;
            double v10 = center.field_1350 + Math.sin(v4) * v6;

            int v12 = this.m804(MathHelper.method_15357(v8), MathHelper.method_15357(v10));
            ik.lk v13 = new ik.lk();
            v13.x = v8;
            v13.z = v10;
            v13.y = (double)((float)v12 + this.m803(0.05F, 0.9F));
            v13.size = this.m803(2.2F, 5.6F);
            v13.maxLife = this.m803(2.4F, 5.5F);
            v13.life = this.m803(0.0F, 1.0F);
            v13.vx = (double)(this.m803(-0.35F, 0.35F) * this.f727.getValue());
            v13.vz = (double)(this.m803(-0.35F, 0.35F) * this.f727.getValue());
            this.f748.add(v13);
         }
      }
   }

   private boolean m806(double x, double y, double z) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         Mutable result = this.f750;
         int temp1 = MathHelper.method_15357(x);

         int v12 = MathHelper.method_15357(y);

         result.method_10103(temp1, v12, MathHelper.method_15357(z));

         try {
            return !mc.field_1687.method_8320(this.f750).method_26227().method_15769();
         } catch (Throwable v11) {
            return false;
         }
      }
   }

   @Override
   protected void onDisable() {
      ut.getInstance().reset();

      int temp2 = v1 + 1;

      this.m797();
   }

   private void m810(double x, double y, double z, ik.jk d) {
      if (this.f746.size() <= 700) {
         double v8 = Math.floor(y) + 1.0005;
         ik.mk v10 = new ik.mk();
         v10.x = x;
         v10.y = v8;
         v10.z = z;
         v10.life = 0.0F;
         v10.maxLife = this.m803(0.3F, 0.52F);
         v10.scale = (float)(0.32 + 0.22 * d.mass);
         v10.water = this.m806(x, y - 0.15, z);
         this.f746.add(v10);
         if (this.f729.getValue() && this.f747.size() <= 1400) {
            int v11 = v10.water ? 4 : 3;

            for (int v12 = 0; v12 < v11; v12++) {
               double result = this.f751.nextDouble() * Math.PI * 2.0;

               double v13 = result;
               double v15 = (double)this.m803(1.1F, 2.6F) * d.mass;
               ik.kk v17 = new ik.kk();
               v17.x = x;
               v17.y = v8 + 0.02;

               v17.z = z;
               v17.vx = Math.cos(v13) * v15 * 0.45 + d.vx * 0.08;
               v17.vz = Math.sin(v13) * v15 * 0.45 + d.vz * 0.08;
               v17.vy = (double)this.m803(1.9F, 4.1F);
               v17.maxLife = this.m803(0.28F, 0.5F);
               this.f747.add(v17);
            }
         }
      }
   }

   private float[] m802() {
      int v1;
      if (this.f734.is("Custom")) {

         v1 = this.f735.getValue();
      } else if (this.f734.is("Client")) {
         v1 = up.getClientColor(1);
      } else {
         v1 = -3613464;
      }

      float v2 = (float)(v1 >> 16 & 255) / 255.0F;
      float v3 = (float)(v1 >> 8 & 255) / 255.0F;
      float v4 = (float)(v1 & 255) / 255.0F;
      return new float[]{v2, v3, v4};
   }

   private void m816(MatrixStack ms, Vec3d cam, float[] rgb, float alphaMul, Immediate immediate) {
      if (!this.f746.isEmpty()) {
         VertexConsumer v6 = immediate.method_73477(RAIN_LAYER);

         for (int v8 = this.f746.size(); v7 < v8; v7++) {
            ik.mk v9 = this.f746.get(v7);
            float v10 = MathHelper.method_15363(v9.life / v9.maxLife, 0.0F, 1.0F);
            float v11 = 1.0F - (1.0F - v10) * (1.0F - v10);
            float v12 = v9.scale * (0.18F + v11 * (v9.water ? 1.35F : 0.85F));
            float v13 = v12 * (0.55F + 0.35F * v11);
            float v14 = alphaMul * (1.0F - v10) * (v9.water ? 0.5F : 0.38F);
            if (!(v14 <= 0.01F)) {
               double v15 = v9.x - cam.field_1352;
               double v17 = v9.y - cam.field_1351;
               double v19 = v9.z - cam.field_1350;
               ms.method_22903();
               ms.method_22904(v15, v17, v19);
               Matrix4f v21 = ms.method_23760().method_23761();

               int v22 = (int)(v14 * 255.0F) & 255;
               int v41 = v22 << 24 | (int)(rgb[0] * 255.0F) << 16;
               float temp1 = rgb[1];

               int v23 = v41 | (int)(temp1 * 255.0F) << 8 | (int)(rgb[2] * 255.0F);
               int v24 = 0 | (int)(rgb[0] * 255.0F) << 16 | (int)(rgb[1] * 255.0F) << 8 | (int)(rgb[2] * 255.0F);

               for (int v25 = 0; v25 < 14; v25++) {
                  double v26 = (Math.PI / 7) * (double)v25;
                  double v28 = (Math.PI / 7) * (double)(v25 + 1);
                  float v30 = (float)(Math.cos(v26) * (double)v13);
                  float v31 = (float)(Math.sin(v26) * (double)v13);
                  float v32 = (float)(Math.cos(v28) * (double)v13);
                  float v33 = (float)(Math.sin(v28) * (double)v13);
                  float v34 = (float)(Math.cos(v26) * (double)v12);
                  float v35 = (float)(Math.sin(v26) * (double)v12);
                  float v36 = (float)(Math.cos(v28) * (double)v12);
                  float v37 = (float)(Math.sin(v28) * (double)v12);
                  v6.method_22918(v21, v30, 0.0F, v31).method_39415(v23);
                  v6.method_22918(v21, v32, 0.0F, v33).method_39415(v23);
                  v6.method_22918(v21, v36, 0.0F, v37).method_39415(v24);
                  v6.method_22918(v21, v34, 0.0F, v35).method_39415(v24);
               }

               ms.method_22909();
            }
         }
      }
   }

   private void m817(MatrixStack ms, Camera camera, Vec3d cam, float[] rgb, float alphaMul, Immediate immediate) {
      if (!this.f746.isEmpty()) {
         VertexConsumer v7 = immediate.method_73477(RAIN_LAYER);

         for (int v9 = this.f746.size(); v8 < v9; v8++) {
            ik.mk v10 = this.f746.get(v8);
            float v11 = MathHelper.method_15363(v10.life / v10.maxLife, 0.0F, 1.0F);
            if (!(v11 > 0.55F)) {
               float v12 = v11 / 0.55F;
               float v13 = v10.scale * 0.55F * (float)Math.sin((double)v12 * Math.PI);
               float v14 = alphaMul * (1.0F - v12) * 0.32F;
               if (!(v14 <= 0.01F) && !(v13 <= 0.005F)) {
                  ms.method_22903();
                  double temp1 = v10.x - cam.field_1352;
                  double temp2 = v10.y - cam.field_1351;

                  ms.method_22904(temp1, temp2, v10.z - cam.field_1350);
                  ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
                  Matrix4f v15 = ms.method_23760().method_23761();
                  float v16 = v10.scale * 0.16F;
                  int v17 = (int)(v14 * 255.0F) & 255;
                  int result = v17 << 24;
                  int v23 = (int)(rgb[0] * 255.0F);

                  int v18 = result | v23 << 16 | (int)(rgb[1] * 255.0F) << 8 | (int)(rgb[2] * 255.0F);
                  int v19 = 0
                     | (int)(rgb[0] * 255.0F) << 16
                     | (int)(rgb[1] * 255.0F) << 8
                     | (int)(rgb[2] * 255.0F);
                  v7.method_22918(v15, -v16 * 0.25F, v13, 0.0F).method_39415(v19);
                  v7.method_22918(v15, v16 * 0.25F, v13, 0.0F).method_39415(v19);
                  v7.method_22918(v15, v16, 0.0F, 0.0F).method_39415(v18);
                  v7.method_22918(v15, -v16, 0.0F, 0.0F).method_39415(v18);
                  ms.method_22909();
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldRender(EventTrace event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1773 != null) {
         if (this.f733.getValue()) {

            if (!mc.field_1687.method_8419()) {
               this.m797();
               return;
            }
         }

         Camera v2 = mc.field_1773.method_19418();
         if (v2 != null) {
            long v3 = System.nanoTime();
            if (this.f752 == 0L) {
               this.f752 = v3;
            }

            float v5 = (float)((double)(v3 - this.f752) / 1.0E9);
            this.f752 = v3;
            v5 = MathHelper.method_15363(v5, 0.0F, 0.05F);
            this.f753 += v5;
            Vec3d v6 = v2.method_71156();
            this.m809(v6, v5);
            this.m812(v5);
            this.m811(v5);
            this.m813(v6, v5);
            this.m814(v5);
            if (!this.f745.isEmpty() || !this.f746.isEmpty() || !this.f747.isEmpty() || !this.f748.isEmpty()) {
               float[] v7 = this.m802();
               float v8 = this.m820(v2);
               float v9 = MathHelper.method_15363(v8 + this.f754 * 0.9F, 0.0F, 1.65F);
               float v10 = this.opacity.getValue() * v9;
               MatrixStack v11 = event.getMatrixStack();

               try {
                  Immediate v12 = VertexConsumerProvider.method_22991(this.f744);
                  if (this.f730.getValue()) {
                     this.m819(v11, v2, v6, v7, v10, v12);
                  }

                  this.m815(v11, v2, v6, v7, v10, false, v12);
                  if (this.f728.getValue()) {
                     this.m816(v11, v6, v7, v10, v12);
                  }

                  VertexConsumer v13 = v12.method_73477(RAIN_LAYER);
                  this.m815(v11, v2, v6, v7, v10, true, v12);
                  if (this.f728.getValue()) {
                     this.m817(v11, v2, v6, v7, v10, v12);
                     if (this.f729.getValue()) {
                        this.m818(v11, v2, v6, v7, v10, v12);
                     }
                  }

                  v12.method_22993();

                  try {
                     Matrix4f v14 = new Matrix4f(event.getMatrixStack().method_23760().method_23761());
                     Matrix4f v15 = new Matrix4f(event.getProjectionMatrix());
                     this.renderWetSurfaceFrame(v2, v14, v15);
                  } catch (Throwable v17) {
                  }

                  GL11.glDepthMask(true);
                  GL11.glColorMask(true, true, true, true);
                  GL11.glEnable(2929);
                  GL11.glEnable(3042);
                  GL11.glBlendFunc(770, 771);
               } catch (Throwable v18) {
               }
            }
         }
      }
   }

   private float m820(Camera camera) {
      try {

         int result = mc.field_1687.method_22339(camera.method_19328());

         int v2 = result;
         return MathHelper.method_15363((float)v2 / 15.0F, 0.3F, 1.0F);
      } catch (Throwable v5) {
         return 1.0F;
      }
   }

   private void m818(MatrixStack ms, Camera camera, Vec3d cam, float[] rgb, float alphaMul, Immediate immediate) {
      if (!this.f747.isEmpty()) {
         VertexConsumer v7 = immediate.method_73477(RAIN_LAYER);
         float v8 = this.f725.getValue();

         for (int v10 = this.f747.size(); v9 < v10; v9++) {
            ik.kk v11 = this.f747.get(v9);
            float v12 = MathHelper.method_15363(v11.life / v11.maxLife, 0.0F, 1.0F);
            float v13 = alphaMul * (1.0F - v12) * 0.5F;
            if (!(v13 <= 0.01F)) {
               double v14 = Math.sqrt(v11.vx * v11.vx + v11.vy * v11.vy + v11.vz * v11.vz);
               float v16 = (float)MathHelper.method_15350(v14 * 0.03 * (double)v8, 0.04, 0.4);
               float v17 = 0.016F * v8;
               float v18 = (float)Math.toRadians((double)camera.method_19330());
               double v19 = v11.vx * Math.cos((double)v18) + v11.vz * Math.sin((double)v18);
               float v21 = (float)Math.toDegrees(Math.atan2(v19, -v11.vy + 1.0E-4));
               ms.method_22903();
               ms.method_22904(v11.x - cam.field_1352, v11.y - cam.field_1351, v11.z - cam.field_1350);
               ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
               ms.method_22907(RotationAxis.field_40718.rotationDegrees(-v21));
               Matrix4f v22 = ms.method_23760().method_23761();
               int v23 = (int)(v13 * 255.0F) & 255;
               int v24 = v23 << 24
                  | (int)(rgb[0] * 255.0F) << 16
                  | (int)(rgb[1] * 255.0F) << 8
                  | (int)(rgb[2] * 255.0F);
               int temp1 = (int)(rgb[0] * 255.0F) << 16;

               int v25 = 0 | temp1 | (int)(rgb[1] * 255.0F) << 8 | (int)(rgb[2] * 255.0F);
               v7.method_22918(v22, -v17 * 0.4F, v16 * 0.5F, 0.0F).method_39415(v25);
               v7.method_22918(v22, v17 * 0.4F, v16 * 0.5F, 0.0F).method_39415(v25);
               v7.method_22918(v22, v17, -v16 * 0.5F, 0.0F).method_39415(v24);
               v7.method_22918(v22, -v17, -v16 * 0.5F, 0.0F).method_39415(v24);
               ms.method_22909();
            }
         }
      }
   }

   private float m800() {
      boolean result = this.f721.is("Drizzle");

      if (result) {
         return 0.55F;
      } else if (this.f721.is("Downpour")) {
         return 1.25F;
      } else {

         result = this.f721.is("Storm");

         return result ? 1.5F : 1.0F;
      }
   }

   private boolean m807(double x, double y, double z) {
      Boolean result = this.f732.getValue();

      if (result && mc.field_1687 != null) {

         this.f750.method_10103(MathHelper.method_15357(x), MathHelper.method_15357(y), MathHelper.method_15357(z));

         try {
            return mc.field_1687.method_8311(this.f750);
         } catch (Throwable v10) {
            return true;
         }
      } else {
         return true;
      }
   }

   private boolean m805(double x, double y, double z) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         this.f750.method_10103(MathHelper.method_15357(x), MathHelper.method_15357(y), MathHelper.method_15357(z));
         if (!mc.field_1687.method_2935().method_12123(this.f750.method_10263() >> 4, this.f750.method_10260() >> 4)) {
            return false;
         } else {
            BlockState v7 = mc.field_1687.method_8320(this.f750);
            if (!v7.method_26227().method_15769()) {
               return true;
            } else {
               boolean result = v7.method_26215();

               if (result) {
                  return false;
               } else {
                  try {
                     return !v7.method_26220(mc.field_1687, this.f750).method_1110();
                  } catch (Throwable v10) {
                     return true;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int result = ++this.f756;

         if (result > 30) {
            this.f756 = 0;
            this.f749.clear();
         }
      } else {
         this.m797();
      }
   }

   private int m801() {
      float v1 = this.f723.getValue();

      float v2 = v1 * v1 * 0.0155F;
      int v3 = (int)(v2 * this.f722.getValue() * this.m798() * 26.0F);

      return MathHelper.method_15340(v3, 40, 4200);
   }

   public void renderWetSurfaceFrame(Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
      if (this.isEnabled() && this.f736.getValue() && mc.field_1724 != null && mc.field_1687 != null) {
         boolean result = this.f733.getValue();

         if (!result || mc.field_1687.method_8419()) {
            ut.wt v4 = new ut.wt();
            v4.wetness = this.f737.getValue() * 0.75F;
            v4.puddleCoverage = this.f738.getValue() * 0.88F;
            v4.reflectionStrength = this.f739.getValue() * 0.95F;
            v4.maxDistance = this.f740.getValue();
            v4.rippleStrength = this.f743.getValue() * 0.85F;
            v4.rainAmount = Math.min(1.45F, Math.max(0.55F, 0.55F + this.f722.getValue() * this.m798() * 0.28F));
            v4.ripples = this.f742.getValue();
            v4.reflectionSteps = this.f741.is("High")
               ? 16
               : (this.f741.is("Performance") ? 6 : 10);
            ut.getInstance().apply(EventItemUse, camera, viewMatrix, projectionMatrix, v4);
         }
      }
   }

   private void m814(float dt) {
      if (!this.f731.getValue()) {
         this.f754 = 0.0F;
      } else {
         this.f754 = Math.max(0.0F, this.f754 - dt * 3.4F);
         this.f755 -= dt;
         if (this.f755 <= 0.0F) {
            this.f754 = this.m803(0.55F, 1.0F);

            this.f755 = this.m803(6.0F, 16.0F);
         }
      }
   }

   public Rains() {
      this.f722 = new SliderSetting(this, "Density", 1.0F, 0.1F, 3.0F, 0.1F);
      this.f723 = new SliderSetting(this, "Radius", 26.0F, 8.0F, 64.0F, 1.0F);
      this.f724 = new SliderSetting(this, "Altitude", 16.0F, 6.0F, 40.0F, 1.0F);
      this.f725 = new SliderSetting(this, "Drop Size", 1.0F, 0.3F, 2.5F, 0.1F);
      this.f726 = new SliderSetting(this, "Fall Speed", 1.0F, 0.3F, 2.5F, 0.1F);
      this.f727 = new SliderSetting(this, "Wind", 1.0F, 0.0F, 3.0F, 0.1F);
      this.opacity = new SliderSetting(this, "Opacity", 1.0F, 0.1F, 2.0F, 0.1F);
      this.f728 = new BooleanSetting(this, "Splashes", true);
      this.f729 = new BooleanSetting(this, "Splash Droplets", true);
      this.f730 = new BooleanSetting(this, "Ground Mist", true);
      this.f731 = new BooleanSetting(this, "Lightning", false);
      this.f732 = new BooleanSetting(this, "Sky Check", true);
      this.f733 = new BooleanSetting(this, "Only When Raining", false);
      this.f734 = new ModeSetting(this, "Color Mode", "Realistic", "Client", "Custom");
      this.f735 = new tf(this, "Color", -4204314);
      this.f736 = new BooleanSetting(this, "Wet Ground", true);
      this.f737 = new SliderSetting(this, "Wetness", 1.25F, 0.1F, 2.0F, 0.1F);
      this.f738 = new SliderSetting(this, "Puddle Coverage", 0.9F, 0.05F, 1.0F, 0.05F);
      this.f739 = new SliderSetting(this, "Reflections", 1.55F, 0.1F, 2.0F, 0.1F);
      this.f740 = new SliderSetting(this, "Reflection Distance", 56.0F, 12.0F, 96.0F, 1.0F);
      this.f741 = new ModeSetting(this, "Reflection Quality", "Performance", "Balanced", "High");
      this.f742 = new BooleanSetting(this, "Puddle Ripples", true);
      this.f743 = new SliderSetting(this, "Ripple Strength", 0.8F, 0.0F, 1.6F, 0.1F);

      try {
         this.f721.set("Rain");
      } catch (Exception v4) {
      }

      try {
         this.f734.set("Realistic");
      } catch (Exception v3) {
      }

      try {
         this.f741.set("Balanced");
      } catch (Exception v2) {
      }

      this.f729.setVisible(() -> {
         Boolean result = this.f728.getValue();

         return result;
      });
      this.f735.setVisible(() -> {

         return this.f734.is("Custom");
      });
      this.f737.setVisible(() -> {

         return this.f736.getValue();
      });
      this.f738.setVisible(() -> {
         Boolean result = this.f736.getValue();

         return result;
      });
      this.f739.setVisible(() -> {

         return this.f736.getValue();
      });
      this.f740.setVisible(() -> {

         return this.f736.getValue();
      });
      this.f741.setVisible(() -> {

         return this.f736.getValue();
      });
      this.f742.setVisible(() -> this.f736.getValue());
      this.f743.setVisible(() -> {
         Boolean result = this.f736.getValue();

         return result && this.f742.getValue();
      });
      this.f744 = new BufferAllocator(262144);
      this.f745 = new ArrayList<>();
      this.f746 = new ArrayList<>();
      this.f747 = new ArrayList<>();
      this.f748 = new ArrayList<>();
      this.f749 = new HashMap<>();
      this.f750 = new Mutable();
      this.f751 = new Random();
      this.f755 = 9.0F;
   }

   private void m815(MatrixStack ms, Camera camera, Vec3d cam, float[] rgb, float alphaMul, boolean highlight, Immediate immediate) {
      if (!this.f745.isEmpty()) {
         VertexConsumer v8 = immediate.method_73477(RAIN_LAYER);
         float v9 = this.f725.getValue();
         float v10 = this.f723.getValue();
         float v11 = v10 * 0.62F;
         Vec3d v12 = camera.method_71156();

         for (int v14 = this.f745.size(); v13 < v14; v13++) {
            ik.jk v15 = this.f745.get(v13);
            double v16 = v15.x - v12.field_1352;
            double v18 = v15.y - v12.field_1351;
            double v20 = v15.z - v12.field_1350;
            double v22 = v16 * v16 + v18 * v18 + v20 * v20;
            double v24 = Math.sqrt(v22);
            if (!(v24 > (double)v10 + 4.0)) {
               float v26 = v24 <= (double)v11
                  ? 1.0F
                  : 1.0F - MathHelper.method_15363((float)((v24 - (double)v11) / (double)(v10 - v11 + 1.0F)), 0.0F, 1.0F);
               if (!(v26 <= 0.02F)) {
                  float v27 = v24 < 0.7 ? (float)(v24 / 0.7) : 1.0F;
                  double v28 = Math.sqrt(v15.vx * v15.vx + v15.vy * v15.vy + v15.vz * v15.vz);
                  if (!(v28 < 1.0E-4)) {
                     float v30 = (float)MathHelper.method_15350(v28 * 0.055 * (double)v9, 0.16, 2.6);
                     float v31 = (float)(0.014 + 0.019 * v15.mass) * v9 * (highlight ? 0.45F : 1.0F);
                     float v32 = alphaMul * v15.brightness * v26 * v27 * (highlight ? 0.3F : 0.42F);
                     if (!(v32 <= 0.006F)) {
                        v32 = Math.min(v32, 0.95F);
                        float v33 = (float)Math.toRadians((double)camera.method_19330());
                        double v34 = Math.cos((double)v33);
                        double v36 = Math.sin((double)v33);
                        double v38 = v15.vx * v34 + v15.vz * v36;
                        float v40 = (float)Math.toDegrees(Math.atan2(v38, Math.abs(v15.vy) + 1.0E-4));
                        v40 = MathHelper.method_15363(v40, -55.0F, 55.0F);
                        ms.method_22903();
                        ms.method_22904(v16, v18, v20);
                        ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
                        ms.method_22907(RotationAxis.field_40718.rotationDegrees(-v40));
                        Matrix4f v41 = ms.method_23760().method_23761();
                        float v42 = v31 * 0.5F;
                        float v43 = v30 * 0.5F;
                        float v44 = -v30 * 0.5F;
                        float v46 = 0.0F;
                        float v48 = v42 * 0.35F;
                        int v49 = (int)(v32 * 255.0F) & 255;
                        int v50 = (int)(v46 * 255.0F) & 255;
                        int v51 = v49 << 24
                           | (int)(rgb[0] * 255.0F) << 16
                           | (int)(rgb[1] * 255.0F) << 8
                           | (int)(rgb[2] * 255.0F);
                        int v52 = v50 << 24
                           | (int)(rgb[0] * 255.0F) << 16
                           | (int)(rgb[1] * 255.0F) << 8
                           | (int)(rgb[2] * 255.0F);
                        v8.method_22918(v41, -v48, v43, 0.0F).method_39415(v52);
                        v8.method_22918(v41, v48, v43, 0.0F).method_39415(v52);
                        v8.method_22918(v41, v42, v44, 0.0F).method_39415(v51);
                        v8.method_22918(v41, -v42, v44, 0.0F).method_39415(v51);
                        if (!highlight) {
                           float v53 = v42 * 2.6F;
                           float v54 = v32 * 0.22F;
                           int v55 = (int)(v54 * 255.0F) & 255;
                           int v56 = v55 << 24
                              | (int)(rgb[0] * 255.0F) << 16
                              | (int)(rgb[1] * 255.0F) << 8
                              | (int)(rgb[2] * 255.0F);
                           int v57 = 0
                              | (int)(rgb[0] * 255.0F) << 16
                              | (int)(rgb[1] * 255.0F) << 8
                              | (int)(rgb[2] * 255.0F);
                           v8.method_22918(v41, -v53 * 0.5F, v43, 0.0F).method_39415(v57);
                           v8.method_22918(v41, v53 * 0.5F, v43, 0.0F).method_39415(v57);
                           v8.method_22918(v41, v53, v44, 0.0F).method_39415(v56);
                           v8.method_22918(v41, -v53, v44, 0.0F).method_39415(v56);
                        } else {
                           float v61 = v44 + v30 * 0.34F;
                           float v62 = v42 * 1.25F;
                           int v64 = (int)(v32 * 0.85F * 255.0F) & 255;

                           int v65 = v64 << 24 | 16777215;
                           v8.method_22918(v41, -v62, v61, 0.0F).method_39415(v63);
                           v8.method_22918(v41, v62, v61, 0.0F).method_39415(v63);
                           v8.method_22918(v41, v62, v44, 0.0F).method_39415(v65);
                           v8.method_22918(v41, -v62, v44, 0.0F).method_39415(v65);
                        }

                        ms.method_22909();
                     }
                  }
               }
            }
         }
      }
   }

   private void m811(float dt) {
      int v2 = this.f747.size() - 1;

      while (v2 >= 0) {
         ik.kk v3 = this.f747.get(v2);
         v3.life += dt;
         if (v3.life >= v3.maxLife) {
            this.f747.remove(v2);
         } else {
            v3.prevX = v3.x;
            v3.prevY = v3.y;
            v3.prevZ = v3.z;
            v3.vy -= 12.100000000000001 * (double)dt;
            v3.x = v3.x + v3.vx * (double)dt;
            v3.y = v3.y + v3.vy * (double)dt;
            v3.z = v3.z + v3.vz * (double)dt;
         }

         v2--;
         
      }
   }

   private float m798() {

      if (this.f721.is("Drizzle")) {
         return 0.45F;
      } else if (this.f721.is("Downpour")) {
         return 1.9F;
      } else {
         return this.f721.is("Storm") ? 2.8F : 1.0F;
      }
   }

   private int m804(int x, int z) {
      long v3 = (long)x << 32 ^ (long)z & 7654912929863454154L + -7654912925568486859L;
      Integer v5 = this.f749.get(v3);
      if (v5 != null) {
         return v5;
      } else {
         int v6;
         try {
            v6 = mc.field_1687.method_8624(Type.field_13202, x, z);
         } catch (Throwable v9) {
            v6 = mc.field_1687.method_31607();
         }

         if (this.f749.size() < 20000) {
            this.f749.put(v3, v6);
         }

         return v6;
      }
   }

   @Override
   protected void onEnable() {
      this.f752 = 0L;

      ut.getInstance().reset();
      this.m797();
   }

   private float m799() {
      if (this.f721.is("Drizzle")) {
         return 0.5F;
      } else if (this.f721.is("Downpour")) {
         return 1.35F;
      } else if (this.f721.is("Storm")) {
         return 2.4F;
      } else {

         return 1.0F;
      }
   }

   private void m812(float dt) {
      int result = this.f746.size();

      for (int v2 = result - 1; v2 >= 0; v2--) {
         ik.mk v6 = this.f746.get(v2);

         ik.mk v3 = v6;
         v3.life += dt;
         if (v3.life >= v3.maxLife) {
            this.f746.remove(v2);
         }
      }
   }

   private void m819(MatrixStack ms, Camera camera, Vec3d cam, float[] rgb, float alphaMul, Immediate immediate) {
      if (!this.f748.isEmpty()) {
         VertexConsumer v7 = immediate.method_73477(RAIN_LAYER);

         for (int v9 = this.f748.size(); v8 < v9; v8++) {
            ik.lk v10 = this.f748.get(v8);
            float v11 = MathHelper.method_15363(v10.life / v10.maxLife, 0.0F, 1.0F);
            float v12 = (float)Math.sin((double)v11 * Math.PI);
            float v13 = alphaMul * v12 * 0.05F;
            if (!(v13 <= 0.004F)) {
               ms.method_22903();
               ms.method_22904(v10.x - cam.field_1352, v10.y - cam.field_1351, v10.z - cam.field_1350);
               ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
               Matrix4f v14 = ms.method_23760().method_23761();
               float v15 = v10.size * 0.5F;
               float v16 = v10.size * 0.42F;
               int v17 = (int)(v13 * 255.0F) & 255;
               int v18 = v17 << 24
                  | (int)(rgb[0] * 255.0F) << 16
                  | (int)(rgb[1] * 255.0F) << 8
                  | (int)(rgb[2] * 255.0F);
               float temp1 = rgb[0];

               int result = 0 | (int)(temp1 * 255.0F) << 16;
               int v22 = (int)(rgb[1] * 255.0F) << 8;

               int v19 = result | v22 | (int)(rgb[2] * 255.0F);
               v7.method_22918(v14, -v15, v16, 0.0F).method_39415(v19);
               v7.method_22918(v14, v15, v16, 0.0F).method_39415(v19);
               v7.method_22918(v14, v15, 0.0F, 0.0F).method_39415(v18);
               v7.method_22918(v14, -v15, 0.0F, 0.0F).method_39415(v18);
               ms.method_22909();
            }
         }
      }
   }

   private void m808(Vec3d center, boolean fromTop) {
      float v3 = this.f723.getValue();
      double v4 = this.f751.nextDouble() * Math.PI * 2.0;
      double v6 = Math.sqrt(this.f751.nextDouble()) * (double)v3;
      double v8 = center.field_1352 + Math.cos(v4) * v6;
      double v10 = center.field_1350 + Math.sin(v4) * v6;
      double v12 = fromTop
         ? center.field_1351 + (double)(this.f724.getValue() * this.m803(0.75F, 1.0F))
         : center.field_1351 + (double)this.m803(-4.0F, this.f724.getValue());

      if (this.m807(v8, v12, v10)) {
         ik.jk v14 = new ik.jk();
         v14.x = v8;
         v14.y = v12;
         v14.z = v10;
         v14.mass = (double)(this.m803(0.6F, 1.4F) * this.m800());
         v14.vy = (double)(-this.m803(6.0F, 11.0F)) * v14.mass;
         v14.phase = this.m803(0.0F, 62.8F);
         v14.brightness = this.m803(0.55F, 1.0F);
         v14.ground = (double)this.m804(MathHelper.method_15357(v8), MathHelper.method_15357(v10));
         this.f745.add(v14);
      }
   }

   private float m803(float min, float max) {

      float v6 = this.f751.nextFloat();
      float v7 = max - min;

      return min + v6 * v7;
   }

   private void m797() {
      synchronized (this.f745) {
         this.f745.clear();
      }

      this.f746.clear();

      this.f747.clear();

      this.f748.clear();
      this.f749.clear();
      this.f754 = 0.0F;
   }

   @Environment(EnvType.CLIENT)
   private static final class jk {
      double z;
      float brightness;
      double prevY;
      double vy;
      double prevZ;
      double mass = 1.0;
      double x;
      double y;
      double vx;
      double ground;
      double prevX;
      boolean dead;
      double vz;
      float phase;

      private jk() {
         this.brightness = 1.0F;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class kk {
      double y;
      double vx;
      float life;
      double x;
      double prevX;
      float maxLife = 0.4F;
      double prevZ;
      double vy;
      double z;
      double vz;
      double prevY;

      
   }

   @Environment(EnvType.CLIENT)
   private static final class lk {
      float life;
      double y;
      double vz;
      double vx;
      float size = 3.0F;
      double x;
      float maxLife = 4.0F;
      double z;

      
   }

   @Environment(EnvType.CLIENT)
   private static final class mk {
      float maxLife = 0.4F;
      double z;
      double x;
      float life;
      float scale = 0.4F;
      boolean water;
      double y;

      
   }
}
