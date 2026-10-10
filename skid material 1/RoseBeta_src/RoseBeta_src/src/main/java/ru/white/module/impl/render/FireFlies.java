package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Fire Flies",
   desc = "Светлячки вокруг игрока",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class FireFlies extends Module {
   public ModeSetting colorMode = new ModeSetting(this, "Цвет", "Европа", "Свой", "Тема");
   private static final RenderPipeline FLY_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("mytheria", "pipeline/fireflies"))
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .build()
   );
   private static final long MAX_PART_ALIVE_TIME = 5500L;
   private static final RenderLayer FLY_LAYER = RenderLayer.method_75940(
      "mytheria_fireflies",
      RenderSetup.method_75927(FLY_PIPELINE).method_75934("Sampler0", ej.FIRE_PART_TEX).method_75937().method_75929(8192).method_75938()
   );
   private static final RenderLayer SPARK_LAYER = RenderLayer.method_75940(
      "mytheria_fireflies_spark",
      RenderSetup.method_75927(ej.SPARK_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   private static final Identifier BLOOM_TEX = Identifier.method_60655("client", "textures/particles/glow.png");
   private static final Identifier SPARK_GLOW_TEX = Identifier.method_60655(
      "client", "textures/particles/glow.png"
   );
   private static final RenderLayer BLOOM_LAYER = RenderLayer.method_75940(
      "mytheria_fireflies_bloom",
      RenderSetup.method_75927(FLY_PIPELINE).method_75934("Sampler0", BLOOM_TEX).method_75937().method_75929(8192).method_75938()
   );
   private static final RenderPipeline TRAIL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("mytheria", "pipeline/fireflies_trail"))
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .build()
   );
   private static final RenderPipeline SPARK_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("mytheria", "pipeline/fireflies_spark"))
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .build()
   );
   private static final RenderLayer SPARK_GLOW_LAYER = RenderLayer.method_75940(
      "mytheria_fireflies_spark_glow",
      RenderSetup.method_75927(FLY_PIPELINE).method_75934("Sampler0", SPARK_GLOW_TEX).method_75937().method_75929(8192).method_75938()
   );
   public BooleanSetting darkImprint;
   private static final Identifier FIRE_PART_TEX = Identifier.method_60655(
      "client", "textures/particles/circle.png"
   );
   private static final RenderLayer TRAIL_LAYER = RenderLayer.method_75940(
      "mytheria_fireflies_trail",
      RenderSetup.method_75927(TRAIL_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   private final List<ej.fj> f653;
   public tf pickColor = new tf(this, "Свой цвет", up.getColor(255, 70, 0, 255))
      .setVisible(() -> {
         Boolean result = this.colorMode.is("Свой");

         return result;
      });
   public BooleanSetting lighting;
   public SliderSetting spawnDelay;

   private void m720(VertexConsumer consumer, Matrix4f matrix, ej.fj part, Vec3d cameraPos) {
      if (part.trailParts.size() >= 2) {
         int v5 = m728(part.color, m727(part.color) * part.getAlphaPC());
         if (this.darkImprint.getValue()) {
            v5 = m730(v5, 0.3F);
         }

         double v6 = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);
         float v8 = MathHelper.method_15363(1.0F - ((float)Math.sqrt(v6) - 3.0F) / 20.0F, 0.0F, 1.0F);
         float v9 = 0.012F * v8 + 1.0E-4F;
         int v10 = part.trailParts.size();

         while (true) {
            int temp1 = v10 - 1;

            if (v11 >= temp1) {
               return;
            }

            ej.hj v12 = part.trailParts.get(v11);
            ej.hj v13 = part.trailParts.get(v11 + 1);
            float v14 = (float)v11 / (float)v10;
            float v15 = (float)(v11 + 1) / (float)v10;

            int v16 = this.m721(v5, part.getAlphaPC(), v14);
            int v17 = this.m721(v5, part.getAlphaPC(), v15);

            this.m722(consumer, matrix, v12, v13, cameraPos, v9, v16, v17);
            v11++;
         }
      }
   }

   private void m724(VertexConsumer consumer, Matrix4f matrix, Vec3d pos, Vec3d cameraPos, Vector3f right, Vector3f up, float half, int color) {
      float v9 = (float)(pos.field_1352 - cameraPos.field_1352);
      float v10 = (float)(pos.field_1351 - cameraPos.field_1351);
      float v11 = (float)(pos.field_1350 - cameraPos.field_1350);
      float v12 = right.x * half;
      float v13 = right.y * half;
      float v14 = right.z * half;
      float v15 = up.x * half;
      float v16 = up.y * half;
      float v17 = up.z * half;
      this.m726(consumer, matrix, v9 - v12 - v15, v10 - v13 - v16, v11 - v14 - v17, color);

      this.m726(consumer, matrix, v9 - v12 + v15, v10 - v13 + v16, v11 - v14 + v17, color);
      this.m726(consumer, matrix, v9 + v12 + v15, v10 + v13 + v16, v11 + v14 + v17, color);
      float temp3 = v9 + v12;

      this.m726(consumer, matrix, temp3 - v15, v10 + v13 - v16, v11 + v14 - v17, color);
   }

   private static float m733(float t) {
      float result = 2.0F * t;

      float v1 = result - 1.0F;
      return 1.0F - v1 * v1;
   }

   private 

      if (this.colorMode.is("Европа")) {
         return Color.getHSBColor((float)Math.random(), 1.0F, 1.0F).getRGB();
      } else if (this.colorMode.is("Тема")) {
         return up.client();
      } else {
         int result = this.pickColor.getValue();

         return result;
      }
   }

   private void m723(VertexConsumer consumer, Matrix4f matrix, ej.fj part, Vec3d cameraPos, Vector3f right, Vector3f up, float pTicks) {
      if (part.sparkParts.size() >= 2) {

         double v8 = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);

         float v10 = 0.25F + 5.0F * MathHelper.method_15363(1.0F - ((float)Math.sqrt(v8) - 3.0F) / 10.0F, 0.0F, 1.0F);
         float v11 = v10 * 0.0012F;
         float v12 = part.getAlphaPC();

         for (ej.gj v14 : part.sparkParts) {
            float v15 = (float)v14.timePC();
            int v16 = m731(-1, part.color, v15);
            v16 = m728(v16, m727(part.color) * v12 * (1.0F - v15 * v15 * v15));
            Vec3d v17 = new Vec3d(v14.getRenderPosX(pTicks), v14.getRenderPosY(pTicks), v14.getRenderPosZ(pTicks));
            this.m724(consumer, matrix, v17, cameraPos, right, up, v11, v16);
         }
      }
   }

   private boolean m716(double x, double y, double z) {
      ClientWorld result = mc.field_1687;

      BlockPos temp1 = BlockPos.method_49637(x, y, z);

      BlockState v7 = result.method_8320(temp1);
      if (!v7.method_26215()) {

         if (v7.method_26227().method_15769() && !v7.method_45474()) {
            return true;
         }
      }

      return false;
   }

   private static float m729(int color) {
      int result = color >> 16;
      int temp1 = 255;

      result &= temp1;
      temp1 = color >> 8 & 255;
      int v6 = color & 255;

      float[] v1 = Color.RGBtoHSB(result, temp1, v6, null);
      return v1[2];
   }

   private void m722(VertexConsumer consumer, Matrix4f matrix, ej.hj a, ej.hj b, Vec3d cameraPos, float halfWidth, int c1, int c2) {
      float v9 = (float)(a.x - cameraPos.field_1352);
      float v10 = (float)(a.y - cameraPos.field_1351);
      float result = (float)(a.z - cameraPos.field_1350);

      float v11 = result;
      float v12 = (float)(b.x - cameraPos.field_1352);
      float v13 = (float)(b.y - cameraPos.field_1351);
      float v14 = (float)(b.z - cameraPos.field_1350);
      float v15 = v12 - v9;
      float v16 = v13 - v10;
      float v17 = v14 - v11;
      float v18 = v10 * v17 - v11 * v16;
      float v19 = v11 * v15 - v9 * v17;
      float v20 = v9 * v16 - v10 * v15;
      float v21 = (float)Math.sqrt((double)(v18 * v18 + v19 * v19 + v20 * v20));
      if (!(v21 < 1.0E-6F)) {
         v18 = v18 / v21 * halfWidth;
         v19 = v19 / v21 * halfWidth;
         v20 = v20 / v21 * halfWidth;
         this.m726(consumer, matrix, v9 - v18, v10 - v19, v11 - v20, c1);
         this.m726(consumer, matrix, v9 + v18, v10 + v19, v11 + v20, c1);
         this.m726(consumer, matrix, v12 + v18, v13 + v19, v14 + v20, c2);
         this.m726(consumer, matrix, v12 - v18, v13 - v19, v14 - v20, c2);
      }
   }

   private static float m727(int color) {

      return (float)(color >>> 24 & 255);
   }

   private void m725(VertexConsumer consumer, Matrix4f matrix, Vec3d pos, Vec3d cameraPos, Vector3f right, Vector3f up, float half, int color) {
      float v9 = (float)(pos.field_1352 - cameraPos.field_1352);
      float v10 = (float)(pos.field_1351 - cameraPos.field_1351);
      float v11 = (float)(pos.field_1350 - cameraPos.field_1350);
      float v12 = right.x * half;
      float v13 = right.y * half;
      float v14 = right.z * half;
      float v15 = up.x * half;
      float v16 = up.y * half;
      float v17 = up.z * half;
      float v18 = (float)(color >> 16 & 255) / 255.0F;
      float v19 = (float)(color >> 8 & 255) / 255.0F;
      float v20 = (float)(color & 255) / 255.0F;
      float v21 = (float)(color >>> 24 & 255) / 255.0F;
      float temp2 = v9 - v12;

      consumer.method_22918(matrix, temp2 - v15, v10 - v13 - v16, v11 - v14 - v17)
         .method_22913(0.0F, 0.0F)
         .method_22915(v18, v19, v20, v21);
      consumer.method_22918(matrix, v9 - v12 + v15, v10 - v13 + v16, v11 - v14 + v17)
         .method_22913(0.0F, 1.0F)
         .method_22915(v18, v19, v20, v21);
      consumer.method_22918(matrix, v9 + v12 + v15, v10 + v13 + v16, v11 + v14 + v17)
         .method_22913(1.0F, 1.0F)
         .method_22915(v18, v19, v20, v21);
      consumer.method_22918(matrix, v9 + v12 - v15, v10 + v13 - v16, v11 + v14 - v17)
         .method_22913(1.0F, 0.0F)
         .method_22915(v18, v19, v20, v21);
   }

   private void m719(VertexConsumer consumer, Matrix4f matrix, ej.fj part, Vec3d cameraPos, Vector3f right, Vector3f up, float pTicks, boolean bloomPass) {
      float v9 = part.getAlphaPC();
      float v21;
      int result = (v21 = v9 - 0.004F) == 0.0F ? 0 : (v21 < 0.0F ? -1 : 1);

      if (result > 0) {
         int v10 = m728(part.color, m727(part.color) * v9);
         float v11 = m729(v10);
         float v19 = m727(v10) / 255.0F;

         int v12 = m732(v10, v19 * v11 * 0.3F);
         Vec3d v13 = part.getRenderPosVec(pTicks);
         float v14 = 0.04F * v9;
         if (bloomPass) {
            v12 = m728(v12, m727(v12) * 0.3F);
            float v18 = v14 * 5.0F;

            v14 = v18;
         }

         this.m725(consumer, matrix, v13, cameraPos, right, up, v14, v12);
      }
   }

   private Vec3d m715(double rangeXZ, double rangeY) {
      Vec3d result = mc.field_1724.method_73189();
      double temp1 = (double)m713(-rangeXZ, rangeXZ);
      double temp2 = -rangeY;

      return result.method_1031(temp1, (double)m713(temp2 / 3.0, rangeY), (double)m713(-rangeXZ, rangeXZ));
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1724.field_6012 == 1) {
            this.f653.forEach(ej.fj::setToRemove);
         }

         this.f653.forEach(ej.fj::updatePart);
         this.f653.removeIf(ej.fj::isToRemove);
         int result = mc.field_1724.field_6012;
         Float v5 = this.spawnDelay.getValue();

         if (result % (v5.intValue() + 1) == 0) {
            int v2 = this.m712();
            this.f653.add(new ej.fj(this.m714(10.0, 4.0), 5500.0F, v2));
            this.f653.add(new ej.fj(this.m714(6.0, 5.0), 5500.0F, v2));
         }
      }
   }

   private boolean m717(ej.fj part, Vec3d cameraPos, Vector3f forward) {
      float v4 = (float)(part.pos.field_1352 - cameraPos.field_1352);
      float v5 = (float)(part.pos.field_1351 - cameraPos.field_1351);
      float v6 = (float)(part.pos.field_1350 - cameraPos.field_1350);
      float result = v4 * forward.x;

      return result + v5 * forward.y + v6 * forward.z < -2.0F;
   }

   private static int m728(int color, float alpha) {
      int result = Math.round(alpha);
      int temp2 = 255;

      int v2 = MathHelper.method_15340(result, 0, temp2);
      return color & 16777215 | v2 << 24;
   }

   public FireFlies() {
      this.darkImprint = new BooleanSetting(this, "Тёмный след", false);
      this.lighting = new BooleanSetting(this, "Свечение", false);
      this.spawnDelay = new SliderSetting(this, "Задержка спавна", 3.0F, 1.0F, 10.0F, 1.0F);
      this.f653 = new ArrayList<>();
   }

   private static int m731(int from, int to, float t) {
      t = MathHelper.method_15363(t, 0.0F, 1.0F);
      int v3 = MathHelper.method_48781(t, from >> 16 & 255, to >> 16 & 255);
      int temp1 = from >> 8 & 255;

      int v4 = MathHelper.method_48781(t, temp1, to >> v10 & 255);
      int v5 = MathHelper.method_48781(t, from & 255, to & 255);
      int v6 = MathHelper.method_48781(
         t, from >>> 24 & 255, to >>> 24 & 255
      );
      return v6 << 24 | v3 << 16 | v4 << 8 | v5;
   }

   private void m718(VertexConsumer consumer, Matrix4f matrix, ej.fj part, Vec3d cameraPos, Vector3f right, Vector3f up, float pTicks) {
      if (!part.sparkParts.isEmpty()) {
         double v8 = mc.field_1724.method_5649(part.pos.field_1352, part.pos.field_1351 + 1.6, part.pos.field_1350);
         float v10 = 0.035F + 0.02F * MathHelper.method_15363(1.0F - ((float)Math.sqrt(v8) - 3.0F) / 10.0F, 0.0F, 1.0F);

         for (ej.gj v12 : part.sparkParts) {
            float v13 = (float)v12.timePC();
            int result = part.color;
            float temp1 = m727(part.color);

            int v14 = m728(result, temp1 * (1.0F - v13) * 0.3F);
            Vec3d v18 = new Vec3d;

            v18./* $VF: Unable to resugar constructor */<init>(v12.getRenderPosX(pTicks), v12.getRenderPosY(pTicks), v12.getRenderPosZ(pTicks));
            Vec3d v15 = v18;
            this.m725(consumer, matrix, v15, cameraPos, right, up, v10 * (1.4F - v13), v14);
         }
      }
   }

   private static int m732(int color, float t) {
      int v2 = color >>> 24 & 255;

      float temp2 = MathHelper.method_15363(t, 0.0F, 1.0F);

      int v3 = m731(color, -1, temp2);
      return v3 & 16777215 | v2 << 24;
   }

   @Override
   protected void onDisable() {
      super.onDisable();

      this.f653.clear();
   }

   private static int m730(int color, float factor) {
      int v2 = (int)((float)(color >> 16 & 255) * factor);
      int result = color >> 8;

      int v3 = (int)((float)(result & 255) * factor);
      int v4 = (int)((float)(color & 255) * factor);

      result = color & -16777216;

      return result | v2 << 16 | v3 << 8 | v4;
   }

   @EventTarget
   public void onRender(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.f653.isEmpty()) {
         float v2 = e.getTickDelta();
         Vec3d v3 = mc.field_1773.method_19418().method_71156();
         Quaternionf v4 = mc.field_1773.method_19418().method_23767();
         Vector3f v5 = v4.transform(new Vector3f(1.0F, 0.0F, 0.0F));

         Vector3f v6 = v4.transform(new Vector3f(0.0F, 1.0F, 0.0F));
         Vector3f v7 = v4.transform(new Vector3f(0.0F, 0.0F, -1.0F));
         MatrixStack v8 = e.getMatrixStack();
         Matrix4f v9 = v8.method_23760().method_23761();
         Immediate v10 = mc.method_22940().method_23000();
         VertexConsumer v11 = v10.method_73477(TRAIL_LAYER);

         for (ej.fj v13 : this.f653) {
            if (!this.m717(v13, v3, v7)) {
               this.m720(v11, v9, v13, v3);
            }
         }

         v10.method_22994(TRAIL_LAYER);
         if (this.lighting.getValue()) {
            VertexConsumer v19 = v10.method_73477(SPARK_GLOW_LAYER);

            for (ej.fj v14 : this.f653) {

               if (!this.m717(v14, v3, v7)) {
                  this.m718(v19, v9, v14, v3, v5, v6, v2);
               }
            }

            v10.method_22994(SPARK_GLOW_LAYER);
         }

         VertexConsumer v20 = v10.method_73477(SPARK_LAYER);

         for (ej.fj v24 : this.f653) {
            if (!this.m717(v24, v3, v7)) {
               this.m723(v20, v9, v24, v3, v5, v6, v2);
            }
         }

         v10.method_22994(SPARK_LAYER);
         VertexConsumer v23 = v10.method_73477(FLY_LAYER);

         for (ej.fj v15 : this.f653) {
            if (!this.m717(v15, v3, v7)) {
               this.m719(v23, v9, v15, v3, v5, v6, v2, false);
            }
         }

         v10.method_22994(FLY_LAYER);
         if (this.lighting.getValue()) {
            VertexConsumer v26 = v10.method_73477(BLOOM_LAYER);

            for (ej.fj v16 : this.f653) {
               if (!this.m717(v16, v3, v7)) {
                  this.m719(v26, v9, v16, v3, v5, v6, v2, true);
               }
            }

            v10.method_22994(BLOOM_LAYER);
         }
      }
   }

   private Vec3d m714(double rangeXZ, double rangeY) {
      Vec3d v5 = this.m715(rangeXZ, rangeY);

      for (int v6 = 0; v6 < 30; v6++) {
         double v7 = v5.field_1352 - mc.field_1724.method_23317();
         double v9 = v5.field_1350 - mc.field_1724.method_23321();

         if (!this.m716(v5.field_1352, v5.field_1351, v5.field_1350)) {
            if (!(Math.sqrt(v7 * v7 + v9 * v9) < rangeXZ / 3.0)) {
               continue;
            }

            
         }

         v5 = this.m715(rangeXZ, rangeY);
      }

      return v5;
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.f653.clear();
      
   }

   private static float m713(double min, double max) {
      ThreadLocalRandom result = ThreadLocalRandom.current();
      double temp2 = Math.max(max, min + 1.0E-6);

      return (float)result.nextDouble(min, temp2);
   }

   private int m721(int baseColor, float alphaPC, float sizePC) {
      float v4 = m733(sizePC);

      float temp1 = m727(baseColor);

      int v5 = m728(baseColor, temp1 * alphaPC * v4);
      return m732(v5, m727(v5) / 255.0F * m729(v5) * 0.25F * sizePC);
   }

   private void m726(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int color) {
      VertexConsumer result = consumer.method_22918(matrix, x, y, z);
      float temp1 = (float)(color >> 16 & 255) / 255.0F;
      float temp2 = (float)(color >> 8 & 255) / 255.0F;

      float temp3 = (float)(color & 255) / 255.0F;

      float v11 = (float)(color >>> 24 & 255);

      result.method_22915(temp1, temp2, temp3, v11 / 255.0F);
   }

   @Environment(EnvType.CLIENT)
   private class fj {
      final List<ej.hj> trailParts = new ArrayList<>();
      float moveYaw;
      final int color;
      Vec3d pos;
      float alphaAnim;
      float yMotion;
      float alphaTarget;
      int msChangeSideRate;
      boolean toRemove;
      final List<ej.gj> sparkParts = new ArrayList<>();
      final long startTime;
      final float maxAlive;
      Vec3d prevPos;
      long rateTimer;
      float moveYawSet;
      float speed;

      float getTimePC() {
         return MathHelper.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0F, 1.0F);
      }

      void updatePart() {
         this.alphaAnim = this.alphaAnim + MathHelper.method_15363(this.alphaTarget - this.alphaAnim, -0.02F, 0.02F) * 2.0F;
         this.alphaAnim = MathHelper.method_15363(this.alphaAnim, 0.0F, 1.0F);
         if (System.currentTimeMillis() - this.rateTimer >= (long)this.msChangeSideRate) {
            this.msChangeSideRate = this.getMsChangeSideRate();
            this.rateTimer = System.currentTimeMillis();
            this.moveYawSet = ej.m713(0.0, 360.0);
         }

         this.moveYaw = MathHelper.method_16439(0.065F, this.moveYaw, this.moveYawSet);
         this.speed /= 1.005F;
         float result = -MathHelper.method_15374((double)((float)Math.toRadians((double)this.moveYaw))) * this.speed;

         float v1 = result;

         float v2 = MathHelper.method_15362((double)((float)Math.toRadians((double)this.moveYaw))) * this.speed;
         this.prevPos = this.pos;
         float v3 = 0.1F;
         boolean v4 = !hp.mc
            .field_1687
            .method_18026(
               new Box(
                  this.pos.field_1352 - (double)(v3 / 2.0F),
                  this.pos.field_1351,
                  this.pos.field_1350 - (double)(v3 / 2.0F),
                  this.pos.field_1352 + (double)(v3 / 2.0F),
                  this.pos.field_1351 + (double)v3,
                  this.pos.field_1350 + (double)(v3 / 2.0F)
               )
            );
         float v5 = v4 ? 0.3F : 1.0F;
         this.yMotion /= 1.02F;
         this.pos = this.pos.method_1031((double)(v1 / v5), (double)(this.yMotion / v5), (double)(v2 / v5));
         if (this.getTimePC() >= 1.0F) {
            this.alphaTarget = 0.0F;
            if (this.getAlphaPC() < 0.004F) {
               this.setToRemove();
            }
         }

         this.trailParts.add(new ej.hj(this, 500));
         this.trailParts.removeIf(ej.hj::toRemove);

         for (int v6 = 0; v6 < 2; v6++) {
            this.sparkParts.add(new ej.gj(this, 600));
         }

         this.sparkParts.forEach(ej.gj::motionSparkProcess);
         this.sparkParts.removeIf(ej.gj::toRemove);
      }

      boolean isToRemove() {

         return this.toRemove;
      }

      float getAlphaPC() {

         return this.alphaAnim;
      }

      void setToRemove() {

         int temp3 = v1 + 1;

         this.toRemove = true;
      }

      Vec3d getRenderPosVec(float pTicks) {
         double temp1 = -(this.prevPos.field_1352 - this.pos.field_1352) * (double)pTicks;

         return this.pos
            .method_1031(
               temp1, -(this.prevPos.field_1351 - this.pos.field_1351) * (double)pTicks, -(this.prevPos.field_1350 - this.pos.field_1350) * (double)pTicks
            );
      }

      int getMsChangeSideRate() {
         float result = ej.m713(300.5, 900.5);

         return (int)result;
      }

      fj(Vec3d pos, float maxAlive, int color) {
         this.alphaAnim = 0.0F;
         this.alphaTarget = 1.0F;
         this.msChangeSideRate = this.getMsChangeSideRate();
         this.moveYawSet = ej.m713(0.0, 360.0);
         this.speed = ej.m713(0.08, 0.175);
         this.yMotion = ej.m713(-0.075, 0.1);
         this.moveYaw = this.moveYawSet;
         this.startTime = System.currentTimeMillis();
         this.rateTimer = this.startTime;
         this.color = color;
         this.pos = pos;
         this.prevPos = pos;
         this.maxAlive = maxAlive;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class gj {
      double posZ;
      double speed = Math.random() / 50.0;
      double posX;
      final float radianPitch;
      final float radianYaw = (float)Math.random() * 360.0F;
      double prevPosZ;
      final int maxTime;
      double prevPosX;
      final long startTime;
      double posY;
      double prevPosY;

      void motionSparkProcess() {
         float v1 = (float)Math.toRadians((double)this.radianYaw);
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.posX + (double)MathHelper.method_15374((double)v1) * this.speed;
         double temp1 = this.posY;
         float temp2 = (float)Math.toRadians((double)(this.radianPitch - 90.0F));

         this.posY = temp1 + (double)MathHelper.method_15362((double)temp2) * this.speed;

         this.posZ = this.posZ + (double)MathHelper.method_15362((double)v1) * this.speed;
         this.speed /= 1.2;
      }

      double getRenderPosZ(float pTicks) {
         double temp1 = this.posZ - this.prevPosZ;
         double temp2 = (double)pTicks;

         return this.prevPosZ + temp1 * temp2;
      }

      gj(ej.fj part, int maxTime) {
         this.radianPitch = -45.0F + (float)Math.random() * 90.0F;
         this.startTime = System.currentTimeMillis();
         this.maxTime = maxTime;
         this.posX = part.pos.field_1352;
         this.posY = part.pos.field_1351;
         this.posZ = part.pos.field_1350;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
      }

      double getRenderPosY(float pTicks) {

         return this.prevPosY + (this.posY - this.prevPosY) * (double)pTicks;
      }

      double getRenderPosX(float pTicks) {

         double v5 = this.posX - this.prevPosX;

         return this.prevPosX + v5 * (double)pTicks;
      }

      boolean toRemove() {
         double result = this.timePC();

         return result >= 1.0;
      }

      double timePC() {
         double result = (double)MathHelper.method_15363((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);

         return result;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class hj {
      final double x;
      final long startTime = System.currentTimeMillis();
      final double y;
      final double z;
      final int maxTime;

      hj(ej.fj part, int maxTime) {
         this.maxTime = maxTime;
         this.x = part.pos.field_1352;
         this.y = part.pos.field_1351;
         this.z = part.pos.field_1350;
      }

      boolean toRemove() {
         long v3;
         int result = (v3 = System.currentTimeMillis() - this.startTime - (long)this.maxTime) == 0L ? 0 : (v3 < 0L ? -1 : 1);
         int temp2 = v1 + 1;

         return result >= 0;
      }

      
   }
}
