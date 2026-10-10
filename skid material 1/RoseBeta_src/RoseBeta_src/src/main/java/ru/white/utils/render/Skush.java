package ru.white.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventTrace;

@Environment(EnvType.CLIENT)
public class Skush implements IMinecraft {
   public static final RenderPipeline TEX_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/skush_tex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .build()
   );
   private static long lastRenderTime = System.currentTimeMillis();
   private static final RenderPipeline LINES_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/skush_lines"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private static final Identifier BLOOM_TEXTURE = Identifier.method_60655(
      "client", "textures/visuals/particles_3.png"
   );
   private static final RenderLayer TRIANGLES_LAYER = RenderLayer.method_75940(
      "skush_triangles",
      RenderSetup.method_75927(Skush.TRIANGLES_PIPELINE).method_75929(16384).method_75938()
   );
   private static final RenderLayer LINES_LAYER = RenderLayer.method_75940(
      "skush_lines", RenderSetup.method_75927(LINES_PIPELINE).method_75929(16384).method_75938()
   );
   private static final BufferAllocator allocator = new BufferAllocator(262144);
   public static final Function<Identifier, RenderLayer> TEXTURE_LAYER = Util.method_34866(texture -> {
      Builder result = RenderSetup.method_75927(TEX_PIPELINE);
      int v2 = -2082637201;

      RenderSetup v1 = result.method_75934("Sampler0", texture).method_75937().method_75929(1536).method_75938();
      return RenderLayer.method_75940("skush_tex", v1);
   });
   private static double speedAccumulator = 0.0;
   private static final RenderPipeline TRIANGLES_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/skush_triangles"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27379)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   private static void renderSymmetricalSpark(
      Matrix4f m, VertexConsumer faceBuf, VertexConsumer edgeBuf, float x, float y, float z, float s, int cFace, int cEdge
   ) {
      Vector3f v9 = new Vector3f(x, y + s, z);
      Vector3f v10 = new Vector3f(x, y - s, z);
      Vector3f v11 = new Vector3f(x + s, y, z);
      Vector3f v12 = new Vector3f(x, y, z + s);
      Vector3f result = new Vector3f;
      float temp2 = x - s;

      result./* $VF: Unable to resugar constructor */<init>(temp2, y, z);
      Vector3f v13 = result;
      Vector3f v14 = new Vector3f(x, y, z - s);
      Vector3f[] v15 = new Vector3f[]{v11, v12, v13, v14};

      for (int v16 = 0; v16 < 4; v16++) {
         Vector3f v17 = v15[v16];
         Vector3f v18 = v15[(v16 + 1) % 4];
         addTri(faceBuf, m, v9, v17, v18, cFace);
         addTri(faceBuf, m, v10, v18, v17, cFace);
         addLin(edgeBuf, m, v9, v17, cEdge);
         addLin(edgeBuf, m, v10, v17, cEdge);
         addLin(edgeBuf, m, v17, v18, cEdge);
      }
   }

   private static void drawCentralCore(Immediate immediate, MatrixStack stack, double cx, double cy, double cz, Vec3d cam, int color, float anim, float rot) {
      VertexConsumer v12 = immediate.method_73477(TRIANGLES_LAYER);
      VertexConsumer v13 = immediate.method_73477(LINES_LAYER);
      Matrix4f v14 = new Matrix4f(stack.method_23760().method_23761());
      v14.translate((float)(cx - cam.field_1352), (float)(cy - cam.field_1351), (float)(cz - cam.field_1350));
      v14.rotate(new Quaternionf().rotateY(rot).rotateX(rot * 0.5F));
      float v15 = 0.055F;
      int v16 = up.replAlpha(color, (int)(anim * 0.7F * 255.0F));
      int v17 = up.replAlpha(color, (int)(anim * 0.95F * 255.0F));
      Vector3f v18 = new Vector3f(0.0F, v15, 0.0F);
      Vector3f v19 = new Vector3f(0.0F, -v15, 0.0F);
      Vector3f[] v20 = new Vector3f[]{
         new Vector3f(v15, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, v15), new Vector3f(-v15, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -v15)
      };

      for (int v21 = 0; v21 < 4; v21++) {
         Vector3f v22 = v20[v21];
         Vector3f v23 = v20[(v21 + 1) % 4];
         addTri(v12, v14, v18, v22, v23, v16);
         addTri(v12, v14, v19, v23, v22, v16);
         addLin(v13, v14, v18, v22, v17);
         addLin(v13, v14, v19, v22, v17);
         addLin(v13, v14, v22, v23, v17);
      }
   }

   private static void drawDoubleHelix(
      Immediate immediate, Matrix4f matrix, Vec3d pos, Vec3d cam, float radius, float height, double timeSec, int color, float anim
   ) {
      int v10 = 36;
      VertexConsumer v11 = immediate.method_73477(LINES_LAYER);
      int v12 = up.replAlpha(color, (int)(anim * 0.8F * 255.0F));
      int v13 = up.replAlpha(color, (int)(anim * 0.3F * 255.0F));
      float v14 = (float)(timeSec * 0.7);
      int v15 = 0;

      while (v15 < v10) {
         float v16 = (float)(pos.field_1351 + 0.05 + (double)((float)v15 / (float)v10 * height));
         float v17 = (float)(pos.field_1351 + 0.05 + (double)((float)(v15 + 1) / (float)v10 * height));
         double result = (double)v15 * 3.0;

         float v18 = (float)(result * Math.PI / (double)v10) + v14;
         float v19 = (float)((double)(v15 + 1) * 3.0 * Math.PI / (double)v10) + v14;
         float v20 = (float)Math.sin((double)((float)v15 / (float)v10) * Math.PI);
         float v21 = radius * (0.88F + v20 * 0.24F);
         float v22 = (float)Math.sin((double)((float)(v15 + 1) / (float)v10) * Math.PI);
         float v23 = radius * (0.88F + v22 * 0.24F);
         float v24 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v18) * (double)v21);
         float v25 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v18) * (double)v21);
         float v26 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v19) * (double)v23);
         float v27 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v19) * (double)v23);
         v11.method_22918(matrix, v24, (float)((double)v16 - cam.field_1351), v25).method_39415(v12);
         v11.method_22918(matrix, v26, (float)((double)v17 - cam.field_1351), v27).method_39415(v12);
         float v28 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v18 + Math.PI) * (double)v21);
         float v29 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v18 + Math.PI) * (double)v21);
         float v30 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v19 + Math.PI) * (double)v23);
         float v31 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v19 + Math.PI) * (double)v23);
         v11.method_22918(matrix, v28, (float)((double)v16 - cam.field_1351), v29).method_39415(v12);
         v11.method_22918(matrix, v30, (float)((double)v17 - cam.field_1351), v31).method_39415(v12);
         if (v15 % 4 == 0) {
            v11.method_22918(matrix, v24, (float)((double)v16 - cam.field_1351), v25).method_39415(v13);
            v11.method_22918(matrix, v28, (float)((double)v16 - cam.field_1351), v29).method_39415(v13);
         }

         v15++;
         
      }
   }

   private static void drawVortexRings(
      Immediate immediate, Matrix4f matrix, Vec3d pos, Vec3d cam, float radius, float height, double timeSec, int color, float anim
   ) {
      int v10 = 32;
      VertexConsumer v11 = immediate.method_73477(LINES_LAYER);
      int v12 = up.replAlpha(color, (int)(anim * 0.4F * 255.0F));
      float[] v13 = new float[]{0.05F, height * 0.5F, height};
      float[] result = new float[3];

      result[0] = radius * 1.02F;
      result[1] = radius * 0.92F;
      result[2] = radius * 0.98F;
      float[] v14 = result;
      result = new float[3];

      result[0] = 0.4F;
      result[1] = -0.55F;
      result[2] = 0.35F;
      float[] v15 = result;

      for (int v16 = 0; v16 < 3; v16++) {
         float v17 = (float)(pos.field_1351 + (double)v13[v16] - cam.field_1351);
         float v18 = v14[v16];
         float v19 = (float)(timeSec * (double)v15[v16]);

         for (int v20 = 0; v20 < v10; v20++) {
            float v21 = (float)((double)v20 * 2.0 * Math.PI / (double)v10) + v19;
            float v22 = (float)((double)(v20 + 1) * 2.0 * Math.PI / (double)v10) + v19;
            float v23 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v21) * (double)v18);
            float v24 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v21) * (double)v18);
            float v25 = (float)(pos.field_1352 - cam.field_1352 + Math.cos((double)v22) * (double)v18);
            float v26 = (float)(pos.field_1350 - cam.field_1350 + Math.sin((double)v22) * (double)v18);
            v11.method_22918(matrix, v23, v17, v24).method_39415(v12);
            v11.method_22918(matrix, v25, v17, v26).method_39415(v12);
         }
      }
   }

   private static Vec3d computeLayerPos(Vec3d center, double timeSec, float radius, float height, int layer, float phase, double timeOffset) {
      double v9 = timeSec - timeOffset;
      switch (layer) {
         case 0:
            double v19 = ((v9 * 0.3 + (double)phase / (Math.PI * 2)) % 1.0 + 1.0) % 1.0;
            float v21 = (float)(center.field_1351 + 0.05 + v19 * (double)height);
            float v22 = radius * (0.85F + (float)Math.sin(v19 * Math.PI) * 0.2F);
            float v23 = (float)(v9 * 0.85 + (double)phase);
            return new Vec3d(
               center.field_1352 + Math.cos((double)v23) * (double)v22, (double)v21, center.field_1350 + Math.sin((double)v23) * (double)v22
            );
         case 1:
            double v18 = 1.0 - ((v9 * 0.24 + (double)phase / (Math.PI * 2)) % 1.0 + 1.0) % 1.0;
            float result = (float)(center.field_1351 + 0.05 + v18 * (double)height);

            float v20 = result;
            float v14 = radius * (1.05F + (float)Math.cos(v18 * Math.PI) * 0.18F);
            double v24 = -v9 * 0.7;

            float v15 = (float)(v24 + (double)phase);
            return new Vec3d(
               center.field_1352 + Math.cos((double)v15) * (double)v14, (double)v20, center.field_1350 + Math.sin((double)v15) * (double)v14
            );
         default:
            float v11 = (float)(center.field_1351 + (double)height * 0.5 + Math.sin(v9 * 0.9 + (double)phase * 2.0) * (double)height * 0.2);
            float v12 = radius * (1.1F + (float)Math.sin(v9 * 1.2 + (double)phase) * 0.14F);
            float v13 = (float)(v9 * 0.5 + (double)phase);
            return new Vec3d(
               center.field_1352 + Math.cos((double)v13) * (double)v12, (double)v11, center.field_1350 + Math.sin((double)v13) * (double)v12
            );
      }
   }

   public static void render(EventTrace event, LivingEntity target, float anim) {
      if (target != null && !(anim <= 0.0F)) {
         long v3 = System.currentTimeMillis();
         long v5 = Math.min(-4258779959771246956L + 4258779959771247006L, Math.max(1L, v3 - lastRenderTime));
         lastRenderTime = v3;
         float v7 = target.field_6235 > 0 ? (float)Math.sin((double)target.field_6235 * (Math.PI / 10)) : 0.0F;

         double v8 = 1.0 + (double)v7 * 1.3;
         speedAccumulator += (double)v5 / 1000.0 * v8;
         float v10 = event.getTickDelta();
         Vec3d v11 = target.method_30950(v10);
         Vec3d v12 = mc.field_1773.method_19418().method_71156();
         int v13 = up.getClientColor1(1);
         int v14 = up.getColor(255, 45, 80, 255);
         int v15 = up.interpolateColor(v13, v14, v7);
         v15 = up.replAlpha(v15, (int)(anim * 255.0F));
         Immediate v16 = VertexConsumerProvider.method_22991(allocator);
         float v17 = Math.max(0.0F, 1.0F - anim * 1.6F);
         if (v17 > 0.005F) {
            Quaternionf v18 = mc.field_1773.method_19418().method_23767();
            drawSpawnBurst(v16, event.getMatrixStack(), v11, v12, target, v15, v17, anim, v18);
         }

         renderCompactVortex(v16, event.getMatrixStack(), v11, v12, target, v15, speedAccumulator, v7, anim);
         v16.method_22993();
      }
   }

   private static void addLin(VertexConsumer buf, Matrix4f m, Vector3f v1, Vector3f v2, int argb) {
      buf.method_22918(m, v1.x, v1.y, v1.z).method_39415(argb);
      buf.method_22918(m, v2.x, v2.y, v2.z).method_39415(argb);
      
   }

   private static void drawCompactSparks(
      Immediate immediate,
      MatrixStack stack,
      Matrix4f baseMatrix,
      Vec3d pos,
      Vec3d cam,
      float radius,
      float height,
      double timeSec,
      int color,
      float anim,
      float hurtFactor
   ) {
      int v12 = 18;
      int v13 = 6;
      float v14 = 0.024F;
      Vec3d[] v15 = new Vec3d[v12];
      Vec3d[][] v16 = new Vec3d[v12][v13 + 1];

      for (int v17 = 0; v17 < v12; v17++) {
         int v18 = v17 / (6);
         int v19 = v17 % (6);
         float v20 = (float)((double)v19 * 2.0 * Math.PI / 6.0);
         v15[v17] = computeLayerPos(pos, timeSec, radius, height, v18, v20, 0.0);
         v16[v17][0] = v15[v17];

         for (int v21 = 1; v21 <= v13; v21++) {
            v16[v17][v21] = computeLayerPos(pos, timeSec, radius, height, v18, v20, (double)((float)v21 * v14));
         }
      }

      VertexConsumer v39 = immediate.method_73477(TEXTURE_LAYER.apply(BLOOM_TEXTURE));
      Quaternionf v40 = mc.field_1773.method_19418().method_23767();
      int[] v41 = up.getRGBA(color);
      int v42 = (int)((float)v41[3] * anim * 0.4F);
      int v43 = up.getColorRaw(v41[0], v41[1], v41[2], v42);
      int v22 = up.getColorRaw(v41[0], v41[1], v41[2], (int)((float)v42 * 0.45F));
      float v23 = 0.16F * anim;

      for (int v24 = 0; v24 < v12; v24++) {
         Vec3d v25 = v15[v24];
         addBloomBillboard(
            v39, stack, v25.field_1352 - cam.field_1352, v25.field_1351 - cam.field_1351, v25.field_1350 - cam.field_1350, v40, v23, v43
         );
         addBloomBillboard(
            v39, stack, v25.field_1352 - cam.field_1352, v25.field_1351 - cam.field_1351, v25.field_1350 - cam.field_1350, v40, v23 * 1.5F, v22
         );
      }

      VertexConsumer v44 = immediate.method_73477(TRIANGLES_LAYER);
      VertexConsumer v45 = immediate.method_73477(LINES_LAYER);
      int v26 = up.replAlpha(color, (int)(anim * 0.7F * 255.0F));

      int v49 = up.replAlpha(color, (int)(anim * 0.95F * 255.0F));

      int v27 = v49;
      float v28 = 0.032F * (1.0F + hurtFactor * 0.2F);

      for (int v29 = 0; v29 < v12; v29++) {
         Vec3d v30 = v15[v29];

         renderSymmetricalSpark(
            baseMatrix,
            v44,
            v45,
            (float)(v30.field_1352 - cam.field_1352),
            (float)(v30.field_1351 - cam.field_1351),
            (float)(v30.field_1350 - cam.field_1350),
            v28,
            v26,
            v27
         );
      }

      VertexConsumer v46 = immediate.method_73477(LINES_LAYER);

      for (int v47 = 0; v47 < v12; v47++) {
         for (int v31 = 0; v31 < v13; v31++) {
            Vec3d v32 = v16[v47][v31];
            Vec3d v33 = v16[v47][v31 + 1];
            float v34 = 1.0F - (float)v31 / (float)v13;
            v34 *= v34;
            int v35 = up.replAlpha(color, (int)(anim * v34 * 0.7F * 255.0F));
            v46.method_22918(
                  baseMatrix,
                  (float)(v32.field_1352 - cam.field_1352),
                  (float)(v32.field_1351 - cam.field_1351),
                  (float)(v32.field_1350 - cam.field_1350)
               )
               .method_39415(v35);
            v46.method_22918(
                  baseMatrix,
                  (float)(v33.field_1352 - cam.field_1352),
                  (float)(v33.field_1351 - cam.field_1351),
                  (float)(v33.field_1350 - cam.field_1350)
               )
               .method_39415(v35);
         }
      }
   }

   private static void renderCompactVortex(
      Immediate immediate, MatrixStack stack, Vec3d renderPos, Vec3d cam, LivingEntity target, int color, double timeSec, float hurtFactor, float anim
   ) {
      float v10 = target.method_17681();
      float result = target.method_17682();

      float v11 = result;

      float v12 = v10 * (0.92F + hurtFactor * 0.12F);
      float v13 = v11 * 0.96F;
      Matrix4f v14 = stack.method_23760().method_23761();
      drawDoubleHelix(immediate, v14, renderPos, cam, v12, v13, timeSec, color, anim);
      drawVortexRings(immediate, v14, renderPos, cam, v12, v13, timeSec, color, anim);
      drawCompactSparks(immediate, stack, v14, renderPos, cam, v12, v13, timeSec, color, anim, hurtFactor);
      double temp3 = renderPos.field_1351 + (double)v11 * 0.5;

      drawCentralCore(immediate, stack, renderPos.field_1352, temp3, renderPos.field_1350, cam, color, anim, (float)(timeSec * 1.2));
   }

   private static void drawSpawnBurst(
      Immediate immediate, MatrixStack stack, Vec3d pos, Vec3d cam, LivingEntity target, int color, float spawnBloom, float anim, Quaternionf camRot
   ) {
      float v9 = target.method_17682();
      float v10 = target.method_17681();
      float v11 = (float)(pos.field_1351 + (double)v9 * 0.5 - cam.field_1351);
      float v12 = (float)(pos.field_1352 - cam.field_1352);
      float v13 = (float)(pos.field_1350 - cam.field_1350);
      Matrix4f v14 = stack.method_23760().method_23761();
      float v15 = (1.8F + v10) * spawnBloom * anim;
      int v16 = (int)(spawnBloom * anim * 220.0F);
      int v17 = up.replAlpha(color, v16);
      int v18 = up.replAlpha(color, (int)((float)v16 * 0.35F));
      VertexConsumer v19 = immediate.method_73477(TEXTURE_LAYER.apply(BLOOM_TEXTURE));
      addBloomBillboard(v19, stack, (double)v12, (double)v11, (double)v13, camRot, v15 * 0.55F, v17);
      addBloomBillboard(v19, stack, (double)v12, (double)v11, (double)v13, camRot, v15 * 1.6F, v18);
      VertexConsumer v20 = immediate.method_73477(LINES_LAYER);
      int v21 = 18;
      float v22 = (v10 * 2.2F + v9 * 0.6F) * spawnBloom;
      int v23 = (int)(spawnBloom * anim * 0.9F * 255.0F);
      int v24 = up.replAlpha(color, v23);
      int v25 = up.replAlpha(color, 0);

      for (int v26 = 0; v26 < v21; v26++) {
         double v27 = (double)v26 * ((Math.PI * 2) / (double)v21) + speedAccumulator * 0.4;
         double v29 = Math.sin((double)v26 * 0.9 + speedAccumulator * 0.3);
         float v31 = (float)v29 * v9 * 0.45F;
         float v32 = (float)(Math.cos(v27) * (double)v22);
         float v33 = (float)(Math.sin(v27) * (double)v22);
         v20.method_22918(v14, v12, v11, v13).method_39415(v24);
         v20.method_22918(v14, v12 + v32, v11 + v31, v13 + v33).method_39415(v25);
      }

      int v45 = 64;
      float v46 = (v10 * 1.5F + v9 * 0.2F) * (0.8F + spawnBloom * 1.2F);
      float v28 = (float)(pos.field_1351 + (double)v9 * 0.5 - cam.field_1351);
      int v47 = (int)(spawnBloom * anim * 0.7F * 255.0F);
      int v30 = up.replAlpha(color, v47);

      for (int v48 = 0; v48 < v45; v48++) {
         double v50 = (double)v48 * ((Math.PI * 2) / (double)v45);
         double v34 = (double)(v48 + 1) * ((Math.PI * 2) / (double)v45);
         float v36 = v12 + (float)(Math.cos(v50) * (double)v46);
         float v37 = v13 + (float)(Math.sin(v50) * (double)v46);
         float v38 = v12 + (float)(Math.cos(v34) * (double)v46);
         float v39 = v13 + (float)(Math.sin(v34) * (double)v46);

         v20.method_22918(v14, v36, v28, v37).method_39415(v30);
         v20.method_22918(v14, v38, v28, v39).method_39415(v30);
      }

      VertexConsumer v49 = immediate.method_73477(TRIANGLES_LAYER);
      int v51 = 12;
      float v52 = (v10 + v9 * 0.3F) * spawnBloom;
      float v53 = 0.045F * spawnBloom;
      int v35 = (int)(spawnBloom * anim * 0.85F * 255.0F);
      int v54 = up.replAlpha(color, v35);
      int v55 = up.replAlpha(color, Math.min(255, (int)((float)v35 * 1.15F)));

      for (int v56 = 0; v56 < v51; v56++) {
         double v57 = (double)v56 * ((Math.PI * 2) / (double)v51) + speedAccumulator * 0.6;
         float v41 = (float)(Math.sin((double)v56 * 1.1 + speedAccumulator * 0.5) * (double)v9 * 0.35F);
         float v42 = v12 + (float)(Math.cos(v57) * (double)v52);
         float v43 = v13 + (float)(Math.sin(v57) * (double)v52);
         renderSymmetricalSpark(v14, v49, v20, v42, v11 + v41, v43, v53, v54, v55);
      }
   }

   private static void addTri(VertexConsumer buf, Matrix4f m, Vector3f v1, Vector3f v2, Vector3f v3, int argb) {
      buf.method_22918(m, v1.x, v1.y, v1.z).method_39415(argb);
      buf.method_22918(m, v2.x, v2.y, v2.z).method_39415(argb);

      buf.method_22918(m, v3.x, v3.y, v3.z).method_39415(argb);
   }

   private static void addBloomBillboard(
      VertexConsumer buffer, MatrixStack stack, double rx, double ry, double rz, Quaternionf camRot, float size, int shadedColor
   ) {
      Matrix4f result = new Matrix4f(stack.method_23760().method_23761());

      Matrix4f v11 = result;
      v11.translate((float)rx, (float)ry, (float)rz);
      v11.rotate(camRot);
      v11.scale(size, size, size);
      buffer.method_22918(v11, -1.0F, -1.0F, 0.0F)
         .method_39415(shadedColor)
         .method_22913(0.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(v11, 1.0F, -1.0F, 0.0F)
         .method_39415(shadedColor)
         .method_22913(1.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      VertexConsumer v15 = buffer.method_22918(v11, 1.0F, 1.0F, 0.0F).method_39415(shadedColor);

      v15.method_22913(1.0F, 1.0F).method_22922(OverlayTexture.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
      VertexConsumer v16 = buffer.method_22918(v11, -1.0F, 1.0F, 0.0F)
         .method_39415(shadedColor)
         .method_22913(0.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880);

      v16.method_22914(0.0F, 0.0F, 1.0F);
   }

   
}
