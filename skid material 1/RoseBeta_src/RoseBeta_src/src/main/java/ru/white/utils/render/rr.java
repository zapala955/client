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
import net.minecraft.client.gl.SamplerCache;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class rr {
   private ByteBuffer f1328;
   private static final RenderPipeline COMPOSITE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/burn_composite"))
         .withVertexShader(Identifier.method_60655("client", "core/burn_composite"))
         .withFragmentShader(Identifier.method_60655("client", "core/burn_composite"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("BurnData", UniformType.field_60031)
         .withSampler("MaskSampler")
         .withSampler("GlowSampler")
         .withSampler("TrailSampler")
         .withBlend(rr.ADDITIVE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/burn_glow"))
         .withVertexShader(Identifier.method_60655("client", "core/burn_glow"))
         .withFragmentShader(Identifier.method_60655("client", "core/burn_glow"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("BurnData", UniformType.field_60031)
         .withSampler("HandSampler")
         .withSampler("MaskSampler")
         .withBlend(rr.REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer f1326;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private boolean f1329 = false;
   private static final int BUFFER_SIZE = 64;
   private static final RenderPipeline TRAIL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/burn_trail"))
         .withVertexShader(Identifier.method_60655("client", "core/burn_trail"))
         .withFragmentShader(Identifier.method_60655("client", "core/burn_trail"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("BurnData", UniformType.field_60031)
         .withSampler("TrailSampler")
         .withSampler("GlowSampler")
         .withSampler("HandSampler")
         .withSampler("MaskSampler")
         .withBlend(rr.REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final BlendFunction ADDITIVE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private GpuBuffer f1327;

   public void composite(GpuTextureView targetView, GpuTextureView maskView, GpuTextureView glowView, GpuTextureView trailView) {
      SamplerCache result = RenderSystem.getSamplerCache();

      GpuSampler v5 = result.method_75294(FilterMode.LINEAR);
      CommandEncoder v6 = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice v7 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v8 = v6.createRenderPass(
         () -> "minecraft:burn_composite_pass", targetView, OptionalInt.empty()
      );

      try {
         v8.setPipeline(COMPOSITE_PIPELINE);
         v8.setVertexBuffer(0, this.f1327);
         v8.bindTexture("MaskSampler", maskView, v5);

         v8.bindTexture("GlowSampler", glowView, v5);
         v8.bindTexture("TrailSampler", trailView, v5);
         RenderSystem.bindDefaultUniforms(v8);
         v8.setUniform("DynamicTransforms", v7);
         v8.setUniform("BurnData", this.f1326);
         v8.draw(0, 6);
      } catch (Throwable v14) {
         if (v8 != null) {
            try {
               v8.close();
            } catch (Throwable v13) {
               v14.addSuppressed(v13);
            }
         }

         throw v14;
      }

      if (v8 != null) {
         v8.close();
      }
   }

   public void renderGlow(GpuTextureView targetView, GpuTextureView handView, GpuTextureView maskView) {
      GpuSampler v4 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);

      CommandEncoder v5 = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice v6 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v7 = v5.createRenderPass(() -> {
         String var10000x = "minecraft:burn_glow_pass";

         return var10000x;
      }, targetView, OptionalInt.of(0));

      try {
         v7.setPipeline(GLOW_PIPELINE);
         v7.setVertexBuffer(0, this.f1327);
         v7.bindTexture("HandSampler", handView, v4);
         v7.bindTexture("MaskSampler", maskView, v4);
         RenderSystem.bindDefaultUniforms(v7);
         v7.setUniform("DynamicTransforms", v6);
         v7.setUniform("BurnData", this.f1326);
         v7.draw(0, 6);
      } catch (Throwable v14) {

         if (v7 != null) {

            try {
               v7.close();
            } catch (Throwable v13) {
               v14.addSuppressed(v13);
            }
         }

         throw v14;
      }

      if (v7 != null) {
         v7.close();
      }
   }

   public void close() {
      if (this.f1326 != null) {
         this.f1326.close();
         this.f1326 = null;
      }

      if (this.f1327 != null) {
         this.f1327.close();
         int v1 = 427013508;

         this.f1327 = null;
      }

      if (this.f1328 != null) {
         MemoryUtil.memFree(this.f1328);
         this.f1328 = null;
      }

      this.f1329 = false;
   }

   public void renderTrail(GpuTextureView targetView, GpuTextureView prevTrailView, GpuTextureView glowView, GpuTextureView handView, GpuTextureView maskView) {
      GpuSampler v6 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      CommandEncoder v7 = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice v8 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      Supplier temp1 = () -> {
         int v0 = 859950792;
         int temp2 = v0 * (v0 + 1) % 2;

         if (temp2 != 0) {
            }

         return "minecraft:burn_trail_pass";
      };

      RenderPass v9 = v7.createRenderPass(temp1, targetView, OptionalInt.of(0));

      try {
         v9.setPipeline(TRAIL_PIPELINE);
         v9.setVertexBuffer(0, this.f1327);
         v9.bindTexture("TrailSampler", prevTrailView, v6);
         v9.bindTexture("GlowSampler", glowView, v6);
         v9.bindTexture("HandSampler", handView, v6);
         v9.bindTexture("MaskSampler", maskView, v6);
         RenderSystem.bindDefaultUniforms(v9);
         v9.setUniform("DynamicTransforms", v8);
         v9.setUniform("BurnData", this.f1326);
         v9.draw(0, 6);
      } catch (Throwable v15) {
         if (v9 != null) {
            try {
               v9.close();
            } catch (Throwable v14) {
               v15.addSuppressed(v14);
            }
         }

         throw v15;
      }

      if (v9 != null) {
         v9.close();
      }
   }

   public void updateUniforms(
      int width,
      int height,
      boolean fillMode,
      float radiusPx,
      float strength,
      float flameSpeed,
      float colorMix,
      float decay,
      float flameHeightPx,
      float flowSpeed,
      float trailStrength,
      int fireColorRgb,
      float glowStrength
   ) {
      this.m1715();
      this.f1328.clear();
      float v14 = (float)(System.currentTimeMillis() % (-5753974165693623473L + 5753974165693723473L)) / 1000.0F;
      this.f1328.putFloat((float)width);
      this.f1328.putFloat((float)height);
      this.f1328.putFloat(v14);
      this.f1328.putFloat(fillMode ? 1.0F : 0.0F);

      this.f1328.putFloat(radiusPx);
      this.f1328.putFloat(strength);
      this.f1328.putFloat(flameSpeed);

      this.f1328.putFloat(colorMix);
      this.f1328.putFloat(decay);
      this.f1328.putFloat(flameHeightPx);
      this.f1328.putFloat(flowSpeed);
      this.f1328.putFloat(trailStrength);
      this.f1328.putFloat((float)(fireColorRgb >> 16 & 255) / 255.0F);
      this.f1328.putFloat((float)(fireColorRgb >> 8 & 255) / 255.0F);
      this.f1328.putFloat((float)(fireColorRgb & 255) / 255.0F);
      this.f1328.putFloat(glowStrength);
      this.f1328.flip();
      int v15 = this.f1328.remaining();
      if (this.f1326 == null || this.f1326.size() < (long)v15) {
         if (this.f1326 != null) {
            this.f1326.close();
         }

         GpuBuffer v21 = RenderSystem.getDevice().createBuffer(() -> {

            return "minecraft:burn_uniform";
         }, 136, (long)v15);

         this.f1326 = v21;
      }

      CommandEncoder v16 = RenderSystem.getDevice().createCommandEncoder();
      v16.writeToBuffer(this.f1326.slice(), this.f1328);
   }

   private void m1715() {
      if (!this.f1329) {
         this.f1328 = MemoryUtil.memAlloc(64);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();

         this.f1327 = RenderSystem.getDevice().createBuffer(() -> {
            int var10001x = 674471169;
            int v1x = -1350153116;
            int temp3 = v1x * (v1x + 1);

            return StrDec.d("z/QA93SagmH8TRb9j6zWWQMkd9+8qvKhKEZY", var10001x);
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1329 = true;
      }
   }

   
}
