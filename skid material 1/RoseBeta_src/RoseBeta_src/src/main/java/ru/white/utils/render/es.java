package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class es {
   private static final Identifier DOWN_FSH = Identifier.method_60655("client", "core/glass_outline_down");
   private static final RenderPipeline DOWN_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(es.DOWN_PIPELINE_ID)
         .withVertexShader(es.DOWN_VSH)
         .withFragmentShader(DOWN_FSH)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("KawaseOutlineData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier DOWN_VSH = Identifier.method_60655("client", "core/glass_outline");
   private float f1464;
   private static final Identifier COMBINE_PIPELINE_ID = Identifier.method_60655(
      "client", "pipeline/glass_outline_combine"
   );
   private boolean f1462;
   private static final Identifier DOWN_PIPELINE_ID = Identifier.method_60655(
      "client", "pipeline/glass_outline_down"
   );
   private float f1463;
   private int f1461;
   private GpuBuffer f1454;
   private static final RenderPipeline UP_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(es.UP_PIPELINE_ID)
         .withVertexShader(es.UP_VSH)
         .withFragmentShader(es.UP_FSH)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("KawaseOutlineData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private final GpuTexture[] f1450 = new GpuTexture[3];
   private boolean f1459;
   private int f1466;
   private static final Identifier UP_VSH = Identifier.method_60655("client", "core/glass_outline");
   private static final Identifier COMBINE_FSH = Identifier.method_60655("client", "core/glass_outline_combine");
   private final int[] f1453;
   private static final Identifier UP_FSH = Identifier.method_60655("client", "core/glass_outline_up");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final RenderPipeline COMBINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(COMBINE_PIPELINE_ID)
         .withVertexShader(es.COMBINE_VSH)
         .withFragmentShader(COMBINE_FSH)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("OutlineData", UniformType.field_60031)
         .withSampler("maskTexture")
         .withSampler("glowTexture")
         .withSampler("sceneTexture")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private ByteBuffer f1458;
   private int f1460;
   private GpuBuffer f1455;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private final float[] f1465;
   private static final Identifier COMBINE_VSH = Identifier.method_60655("client", "core/glass_outline");
   private GpuBuffer f1456;
   private final int[] f1452;
   private ByteBuffer f1457;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int MAX_FRIEND_RECTS = 8;
   private static final Identifier UP_PIPELINE_ID = Identifier.method_60655("client", "pipeline/glass_outline_up");
   private final GpuTextureView[] f1451 = new GpuTextureView[3];
   private static final int MIP_LEVELS = 3;

   public es() {
      this.f1452 = new int[3];
      this.f1453 = new int[3];
      this.f1459 = false;
      this.f1460 = 0;
      this.f1461 = 0;
      this.f1462 = true;
      this.f1463 = 0.04F;
      this.f1464 = 5.0F;
      this.f1465 = new float[32];
      this.f1466 = 0;
   }

   public void close() {
      this.m1778();
      if (this.f1454 != null) {
         this.f1454.close();
         this.f1454 = null;
      }

      if (this.f1455 != null) {
         this.f1455.close();

         this.f1455 = null;
      }

      if (this.f1456 != null) {
         this.f1456.close();
         this.f1456 = null;
      }

      if (this.f1457 != null) {
         MemoryUtil.memFree(this.f1457);
         this.f1457 = null;
      }

      if (this.f1458 != null) {
         MemoryUtil.memFree(this.f1458);
         this.f1458 = null;
      }

      this.f1459 = false;
      this.f1460 = 0;
      this.f1461 = 0;
   }

   private void m1781(int argb) {

      this.f1458.putFloat((float)(argb >> 16 & 255) / 255.0F);

      this.f1458.putFloat((float)(argb >> 8 & 255) / 255.0F);

      this.f1458.putFloat((float)(argb & 255) / 255.0F);
      this.f1458.putFloat((float)(argb >> 24 & 255) / 255.0F);
   }

   private void m1779(CommandEncoder encoder, float halfPixelX, float halfPixelY, float offset) {
      this.f1457.clear();

      this.f1457.putFloat(halfPixelX);
      this.f1457.putFloat(halfPixelY);
      this.f1457.putFloat(offset);
      this.f1457.putFloat(0.0F);
      this.f1457.flip();

      encoder.writeToBuffer(this.f1454.slice(), this.f1457);
   }

   public void renderOutline(
      GpuTextureView maskView,
      GpuTextureView targetView,
      int width,
      int height,
      float glowStrength,
      int outlineWidth,
      int glowMode,
      int fillColor,

      this.renderOutline(maskView, targetView, width, height, glowStrength, outlineWidth, glowMode, fillColor, outlineColor, maskView, false);
   }

   private void m1778() {

      for (int v1 = 0; v1 < 3; v1++) {
         if (this.f1451[v1] != null) {
            this.f1451[v1].close();
            this.f1451[v1] = null;
         }

         if (this.f1450[v1] != null) {
            this.f1450[v1].close();
            this.f1450[v1] = null;
         }
      }
   }

   private void m1777(int width, int height) {
      if (width != this.f1460 || height != this.f1461 || this.f1450[0] == null) {
         this.m1778();
         int v3 = width;
         int v4 = height;

         for (int v5 = 0; v5 < 3; v5++) {
            v3 = Math.max(1, v3 / 2);
            v4 = Math.max(1, v4 / 2);

            this.f1452[v5] = v3;
            this.f1453[v5] = v4;
            int v8 = v5;
            GpuTexture[] result = this.f1450;
            GpuDevice temp2 = RenderSystem.getDevice();
            Supplier temp3 = () -> {
               StringBuilder var10000x = new StringBuilder;
               int v2 = 2009842796;

               var10000x./* $VF: Unable to resugar constructor */<init>();
               String v5x = var10000x.append("minecraft:glass_outline_mip_").append(v8).toString();

               return v5x;
            };
            int temp4 = 12;

            result[v5] = temp2.createTexture(temp3, temp4, TextureFormat.RGBA8, v3, v4, 1, 1);
            GpuTextureView[] v12 = this.f1451;
            temp2 = RenderSystem.getDevice();

            v12[v5] = temp2.createTextureView(this.f1450[v5]);
         }

         this.f1460 = width;
         this.f1461 = height;
      }
   }

   private void m1776() {
      if (!this.f1459) {
         int temp1 = 16;

         this.f1457 = MemoryUtil.memAlloc(temp1);
         ByteBuffer v5 = MemoryUtil.memAlloc(208);

         this.f1458 = v5;
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1456 = RenderSystem.getDevice().createBuffer(() -> {
            int var10001x = -1859969136;
            int v0 = -1662555686;

            return StrDec.d("XnYQTuUYkthtzwNdDTPq24i+fnMbMOI1qdRdiCFr2xWpw7O7", var10001x);
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1454 = RenderSystem.getDevice().createBuffer(() -> {

            return "minecraft:glass_outline_kawase_ubo";
         }, 136, -7862589139539895289L + 7862589139539895305L);
         GpuDevice v6 = RenderSystem.getDevice();
         Supplier v8 = () -> {

            return "minecraft:glass_outline_combine_ubo";
         };
         int temp3 = 136;
         long temp4 = -7247708536743440568L + 7247708536743440776L;

         this.f1455 = v6.createBuffer(v8, temp3, temp4);
         this.f1459 = true;
      }
   }

   public void setShimmerEnabled(boolean enabled) {
      this.f1462 = enabled;
   }

   public void setShimmerPeriodSec(float sec) {
      this.f1464 = Math.max(0.5F, sec);
      
   }

   public void setFriendRects(float[] rects, 

      int temp2 = 8;

      this.f1466 = Math.min(count, temp2);
      System.arraycopy(rects, 0, this.f1465, 0, this.f1466 * 4);
   }

   public void setShimmerWidth(float width) {
      int v2 = -2083481701;
      int temp2 = v2 * (v2 + 1);

      this.f1463 = Math.max(0.005F, width);
   }

   private void m1780(
      CommandEncoder encoder,
      int width,
      int height,
      int fillColor,
      int outlineColor,
      int outlineWidth,
      int glowMode,
      float shimmerT,
      boolean useItemColor,
      float itemColorReach
   ) {
      this.f1458.clear();
      this.m1781(fillColor);
      this.m1781(outlineColor);
      this.f1458.putFloat(1.0F / (float)width);
      this.f1458.putFloat(1.0F / (float)height);
      this.f1458.putFloat((float)outlineWidth);
      this.f1458.putFloat((float)this.f1466);
      this.f1458.putFloat((float)glowMode);
      this.f1458.putFloat(0.0F);
      this.f1458.putFloat(shimmerT);
      this.f1458.putFloat(this.f1462 ? 1.0F : 0.0F);
      this.f1458.putFloat(this.f1463);
      this.f1458.putFloat(useItemColor ? 1.0F : 0.0F);
      this.f1458.putFloat(itemColorReach);
      this.f1458.putFloat(0.0F);

      for (int v11 = 0; v11 < 8; v11++) {
         int v12 = v11 * 4;

         if (v11 < this.f1466) {
            this.f1458.putFloat(this.f1465[v12]);
            this.f1458.putFloat(this.f1465[v12 + 1]);
            this.f1458.putFloat(this.f1465[v12 + 2]);
            this.f1458.putFloat(this.f1465[v12 + 3]);

            
         } else {
            this.f1458.putFloat(0.0F);
            this.f1458.putFloat(0.0F);
            this.f1458.putFloat(0.0F);
            this.f1458.putFloat(0.0F);
         }
      }

      this.f1458.flip();
      encoder.writeToBuffer(this.f1455.slice(), this.f1458);
   }

   public void renderOutline(
      GpuTextureView maskView,
      GpuTextureView targetView,
      int width,
      int height,
      float glowStrength,
      int outlineWidth,
      int glowMode,
      int fillColor,
      int outlineColor,
      GpuTextureView sceneView,
      boolean useItemColor
   ) {
      this.m1776();
      this.m1777(width, height);
      CommandEncoder v12 = RenderSystem.getDevice().createCommandEncoder();
      GpuSampler v13 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      GpuTextureView v14 = maskView;
      int v15 = width;
      int v16 = height;

      for (int v17 = 0; v17 < 3; v17++) {
         this.m1779(v12, 0.5F / (float)v15, 0.5F / (float)v16, 1.0F);
         GpuBufferSlice v18 = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         GpuTextureView v19 = v14;
         RenderPass v21 = v12.createRenderPass(() -> {
            StringBuilder var10000x = new StringBuilder;
            int v2 = -413725735;

            var10000x./* $VF: Unable to resugar constructor */<init>();
            return var10000x.append("minecraft:glass_outline_down_").append(v17).toString();
         }, this.f1451[v17], OptionalInt.of(0));

         try {
            v21.setPipeline(DOWN_PIPELINE);
            v21.setVertexBuffer(0, this.f1456);
            v21.bindTexture("Sampler0", v19, v13);
            RenderSystem.bindDefaultUniforms(v21);
            v21.setUniform("DynamicTransforms", v18);
            v21.setUniform("KawaseOutlineData", this.f1454);
            v21.draw(0, 6);
         } catch (Throwable v31) {
            if (v21 != null) {
               try {
                  v21.close();
               } catch (Throwable v28) {
                  v31.addSuppressed(v28);
               }
            }

            throw v31;
         }

         if (v21 != null) {
            v21.close();
         }

         v14 = this.f1451[v17];
         v15 = this.f1452[v17];
         int result = this.f1453[v17];

         v16 = result;
      }

      for (int v32 = 2; v32 > 0; v32--) {
         float v34 = Math.max(0.5F, glowStrength / 4.0F);
         float v35 = Math.min(v34, 4.0F);
         this.m1779(v12, 0.5F / (float)this.f1452[v32], 0.5F / (float)this.f1453[v32], v35);
         GpuBufferSlice v20 = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         int v38 = v32;
         RenderPass v22 = v12.createRenderPass(() -> {

            int v1 = v38 - 1;
            return "minecraft:glass_outline_up_" + v1;
         }, this.f1451[v38 - 1], OptionalInt.of(0));

         try {
            v22.setPipeline(UP_PIPELINE);
            v22.setVertexBuffer(0, this.f1456);
            v22.bindTexture("Sampler0", this.f1451[v38], v13);
            RenderSystem.bindDefaultUniforms(v22);
            v22.setUniform("DynamicTransforms", v20);
            v22.setUniform("KawaseOutlineData", this.f1454);
            v22.draw(0, 6);
         } catch (Throwable v30) {
            if (v22 != null) {
               try {
                  v22.close();
               } catch (Throwable v27) {
                  v30.addSuppressed(v27);
               }
            }

            throw v30;
         }

         if (v22 != null) {
            v22.close();
         }
      }

      long v33 = (long)(this.f1464 * 1000.0F);
      float v36 = (float)(System.currentTimeMillis() % v33) / (float)v33;
      float v37 = Math.max(8.0F, glowStrength + (float)outlineWidth + 6.0F);
      this.m1780(v12, width, height, fillColor, outlineColor, outlineWidth, glowMode, v36, useItemColor, v37);
      GpuBufferSlice v39 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v40 = v12.createRenderPass(
         () -> "minecraft:glass_outline_combine", targetView, OptionalInt.empty()
      );

      try {
         v40.setPipeline(COMBINE_PIPELINE);
         v40.setVertexBuffer(0, this.f1456);
         v40.bindTexture("maskTexture", maskView, v13);
         v40.bindTexture("glowTexture", this.f1451[0], v13);
         v40.bindTexture("sceneTexture", sceneView, v13);
         RenderSystem.bindDefaultUniforms(v40);
         v40.setUniform("DynamicTransforms", v39);
         v40.setUniform("OutlineData", this.f1455);
         v40.draw(0, 6);
      } catch (Throwable v29) {
         if (v40 != null) {
            try {
               v40.close();
            } catch (Throwable v26) {
               v29.addSuppressed(v26);
            }
         }

         throw v29;
      }

      if (v40 != null) {
         v40.close();
      }
   }

   
}
