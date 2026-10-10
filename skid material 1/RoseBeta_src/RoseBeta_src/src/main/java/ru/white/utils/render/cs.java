package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
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
public class cs {
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/glass_composite");
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/glass_composite");
   private static final int BUFFER_SIZE = 80;
   private GpuBuffer f1389;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private GpuBuffer f1388;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(cs.VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("GlassData", UniformType.field_60031)
         .withSampler("SceneSampler")
         .withSampler("BlurSampler")
         .withSampler("MaskSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private boolean f1391 = false;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/glass_composite");
   private ByteBuffer f1390;

   public void close() {

      if (this.f1388 != null) {
         this.f1388.close();
         this.f1388 = null;
      }

      if (this.f1389 != null) {
         this.f1389.close();
         this.f1389 = null;
      }

      if (this.f1390 != null) {
         MemoryUtil.memFree(this.f1390);
         this.f1390 = null;
      }

      this.f1391 = false;
   }

   private void m1760() {
      if (!this.f1391) {
         this.f1390 = MemoryUtil.memAlloc(80);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         GpuDevice temp1 = RenderSystem.getDevice();
         Supplier temp2 = () -> {
            int var10001x = -1284440950;

            return StrDec.d("RJVDbP/7wfp3LFB/F9C5+Z5HNE0Hzr08sh0Hsi+6hw23MfeVUZk=", var10001x);
         };

         this.f1389 = temp1.createBuffer(temp2, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1391 = true;
      }
   }

   private void m1761(
      int width,
      int height,
      float saturation,
      boolean reflect,
      int tintColor,
      float tintIntensity,
      float edgeGlowIntensity,
      float edgeSoftness,
      float iceIntensity,
      float frostScale,
      float crackIntensity,
      float smokeAmount,
      float smokeScale,
      float smokeSpeed,
      float smokeReach
   ) {
      this.f1390.clear();
      this.f1390.putFloat((float)width);
      this.f1390.putFloat((float)height);
      this.f1390.putFloat(saturation);
      this.f1390.putFloat(reflect ? 1.0F : 0.0F);
      int result = tintColor >> 24;

      float v16 = (float)(result & 255) / 255.0F;
      float v17 = (float)(tintColor >> 16 & 255) / 255.0F;
      float v18 = (float)(tintColor >> 8 & 255) / 255.0F;
      float v19 = (float)(tintColor & 255) / 255.0F;
      this.f1390.putFloat(v17);
      this.f1390.putFloat(v18);
      this.f1390.putFloat(v19);
      this.f1390.putFloat(v16);
      this.f1390.putFloat(tintIntensity);
      this.f1390.putFloat(edgeGlowIntensity);
      this.f1390.putFloat(edgeSoftness);
      this.f1390.putFloat(0.0F);
      float v20 = (float)(System.currentTimeMillis() % (6729447122171463105L + -6729447122171363105L)) / 1000.0F;
      this.f1390.putFloat(v20);
      this.f1390.putFloat(iceIntensity);
      this.f1390.putFloat(frostScale);
      this.f1390.putFloat(crackIntensity);
      this.f1390.putFloat(smokeAmount);
      this.f1390.putFloat(smokeScale);
      this.f1390.putFloat(smokeSpeed);
      this.f1390.putFloat(smokeReach);
      this.f1390.flip();
   }

   public void composite(
      GpuTextureView targetView,
      GpuTextureView sceneView,
      GpuTextureView blurView,
      GpuTextureView maskView,
      int width,
      int height,
      float saturation,
      boolean reflect,
      int tintColor,
      float tintIntensity,
      float edgeGlowIntensity,
      float edgeSoftness,
      float iceIntensity,
      float frostScale,
      float crackIntensity,
      float smokeAmount,
      float smokeScale,
      float smokeSpeed,
      float smokeReach
   ) {
      this.m1760();

      this.m1761(
         width,
         height,
         saturation,
         reflect,
         tintColor,
         tintIntensity,
         edgeGlowIntensity,
         edgeSoftness,
         iceIntensity,
         frostScale,
         crackIntensity,
         smokeAmount,
         smokeScale,
         smokeSpeed,
         smokeReach
      );
      int v20 = this.f1390.remaining();
      if (this.f1388 == null || this.f1388.size() < (long)v20) {
         if (this.f1388 != null) {
            this.f1388.close();
         }

         this.f1388 = RenderSystem.getDevice().createBuffer(() -> {

            return "minecraft:glass_composite_uniform";
         }, 136, (long)v20);
      }

      CommandEncoder v21 = RenderSystem.getDevice().createCommandEncoder();
      v21.writeToBuffer(this.f1388.slice(), this.f1390);
      GpuBufferSlice v22 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      GpuSampler v23 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      RenderPass v24 = v21.createRenderPass(() -> {

         return "minecraft:glass_composite_pass";
      }, targetView, OptionalInt.empty());

      try {
         v24.setPipeline(PIPELINE);
         v24.setVertexBuffer(0, this.f1389);
         v24.bindTexture("SceneSampler", sceneView, v23);
         v24.bindTexture("BlurSampler", blurView, v23);
         int v27 = -295386970;
         int temp2 = v27 * (v27 + 1);

         v24.bindTexture("MaskSampler", maskView, v23);
         RenderSystem.bindDefaultUniforms(v24);
         v24.setUniform("DynamicTransforms", v22);
         v24.setUniform("GlassData", this.f1388);
         v24.draw(0, 6);
      } catch (Throwable v31) {
         if (v24 != null) {
            try {
               v24.close();
            } catch (Throwable v30) {
               v31.addSuppressed(v30);
            }
         }

         throw v31;
      }

      if (v24 != null) {
         v24.close();
      }
   }

   
}
