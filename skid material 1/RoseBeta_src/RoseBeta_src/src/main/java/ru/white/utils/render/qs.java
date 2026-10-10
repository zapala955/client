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
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
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
public class qs {
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int BUFFER_SIZE = 64;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/menu_rays");
   private GpuBuffer f1518;
   private boolean f1520;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/menu_rays");
   private GpuBuffer f1517;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(qs.VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("MenuRaysData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/menu_rays");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private ByteBuffer f1519;

   private void m1844(int color) {
      float temp1 = (float)(color >> 16 & 255) / 255.0F;

      this.f1519.putFloat(temp1);

      this.f1519.putFloat((float)(color >> 8 & 255) / 255.0F);
      this.f1519.putFloat((float)(color & 255) / 255.0F);
      this.f1519.putFloat((float)(color >> 24 & 255) / 255.0F);
   }

   private void m1843() {
      if (!this.f1520) {

         this.f1519 = MemoryUtil.memAlloc(64);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();

         GpuDevice v5 = RenderSystem.getDevice();
         Supplier v6 = () -> "client:menu_rays_dummy_vertex";

         this.f1518 = v5.createBuffer(v6, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1520 = true;
      }
   }

   public void close() {
      if (this.f1517 != null) {
         this.f1517.close();
         this.f1517 = null;
      }

      if (this.f1518 != null) {

         this.f1518.close();
         this.f1518 = null;
      }

      if (this.f1519 != null) {
         MemoryUtil.memFree(this.f1519);
         this.f1519 = null;
      }

      this.f1520 = false;
   }

   public void draw(float alpha, int colorA, int colorB, float intensity, float rays, float reach) {
      if (!(alpha <= 0.01F)) {
         MinecraftClient v7 = MinecraftClient.method_1551();
         if (v7.method_1522() != null) {
            this.m1843();
            wr.flushPending();
            int v8 = v7.method_22683().method_4489();
            int v9 = v7.method_22683().method_4506();

            this.f1519.clear();
            float v12 = (float)v8;

            this.f1519.putFloat(v12);
            this.f1519.putFloat((float)v9);
            this.f1519.putFloat(0.0F);
            this.f1519.putFloat(Math.max(0.0F, Math.min(1.0F, alpha)));
            this.f1519.putFloat((float)(System.currentTimeMillis() % (-1658958684242062173L + 1658958684242162173L)) / 1000.0F);
            this.f1519.putFloat(intensity);
            this.f1519.putFloat(rays);
            this.f1519.putFloat(reach);
            this.m1844(colorA);
            this.m1844(colorB);
            this.f1519.flip();
            this.m1845(v7);
         }
      }
   }

   private void m1845(MinecraftClient client) {
      int v2 = this.f1519.remaining();
      if (this.f1517 == null || this.f1517.size() < (long)v2) {
         if (this.f1517 != null) {
            this.f1517.close();
         }

         this.f1517 = RenderSystem.getDevice()
            .createBuffer(() -> "client:menu_rays_uniform", 136, (long)v2);
      }

      CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
      v3.writeToBuffer(this.f1517.slice(), this.f1519);
      DynamicUniforms result = RenderSystem.getDynamicUniforms();
      Matrix4f temp1 = RenderSystem.getModelViewMatrix();

      GpuBufferSlice v4 = result.method_71106(temp1, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v3.createRenderPass(
         () -> "client:menu_rays_pass",
         client.method_1522().method_71639(),
         OptionalInt.empty(),
         client.method_1522().method_71640(),
         OptionalDouble.empty()
      );

      try {
         v5.setPipeline(PIPELINE);
         v5.setVertexBuffer(0, this.f1518);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("MenuRaysData", this.f1517);
         int temp2 = 6;

         v5.draw(0, temp2);
      } catch (Throwable v11) {
         if (v5 != null) {
            try {
               v5.close();
            } catch (Throwable v10) {
               v11.addSuppressed(v10);
            }
         }

         throw v11;
      }

      if (v5 != null) {
         v5.close();
      }
   }

   
}
