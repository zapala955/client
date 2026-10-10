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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
public class gs {
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/halftone_dots");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private ByteBuffer f1476;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int BUFFER_SIZE = 64;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/halftone_dots");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/halftone_dots");
   private GpuBuffer f1475;
   private GpuBuffer f1474;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("HalftoneDotsData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private boolean f1477;

   private void m1795() {
      if (!this.f1477) {
         int v2 = -758340029;
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }

         this.f1476 = MemoryUtil.memAlloc(64);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         GpuDevice v6 = RenderSystem.getDevice();

         this.f1475 = v6.createBuffer(() -> "client:halftone_dots_dummy_vertex", 32, v1);
         MemoryUtil.memFree(v1);
         this.f1477 = true;
      }
   }

   private void m1796(MinecraftClient client) {
      int v2 = this.f1476.remaining();
      if (this.f1474 == null || this.f1474.size() < (long)v2) {
         if (this.f1474 != null) {
            this.f1474.close();
         }

         this.f1474 = RenderSystem.getDevice().createBuffer(() -> {

            return "client:halftone_dots_uniform";
         }, 136, (long)v2);
      }

      CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
      v3.writeToBuffer(this.f1474.slice(), this.f1476);
      GpuBufferSlice v4 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v3.createRenderPass(() -> {

         return "client:halftone_dots_pass";
      }, client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());

      try {
         v5.setPipeline(PIPELINE);
         v5.setVertexBuffer(0, this.f1475);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("HalftoneDotsData", this.f1474);
         v5.draw(0, 6);
      } catch (Throwable v10) {

         if (v5 != null) {
            try {
               v5.close();
            } catch (Throwable v9) {
               v10.addSuppressed(v9);
            }
         }

         throw v10;
      }

      if (v5 != null) {
         v5.close();
      }
   }

   public void close() {
      if (this.f1474 != null) {
         this.f1474.close();
         this.f1474 = null;
      }

      if (this.f1475 != null) {
         this.f1475.close();
         this.f1475 = null;
      }

      if (this.f1476 != null) {
         MemoryUtil.memFree(this.f1476);

         this.f1476 = null;
         int v2 = -288615066;
         int result = v2 * (v2 + 1);

         
      }

      this.f1477 = false;
   }

   public void draw(float width, float height, float mouseX, float mouseY, float alpha, int color, float spacing, float minRadius, float maxRadius, float reach) {
      if (!(alpha <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F)) {
         MinecraftClient v11 = MinecraftClient.method_1551();
         if (v11.method_1522() != null) {
            this.m1795();
            wr.flushPending();
            this.f1476.clear();
            ByteBuffer result = this.f1476;
            int temp1 = v11.method_22683().method_4489();

            result.putFloat((float)temp1 / 2.0F);
            this.f1476.putFloat((float)v11.method_22683().method_4506() / 2.0F);
            this.f1476.putFloat(width);
            this.f1476.putFloat(height);
            this.f1476.putFloat(spacing);
            this.f1476.putFloat(minRadius);
            this.f1476.putFloat(maxRadius);
            this.f1476.putFloat(reach);
            this.f1476.putFloat(mouseX);

            this.f1476.putFloat(mouseY);
            this.f1476.putFloat(0.0F);
            this.f1476.putFloat(0.0F);
            this.f1476.putFloat((float)(color >> 16 & 255) / 255.0F);
            this.f1476.putFloat((float)(color >> 8 & 255) / 255.0F);
            this.f1476.putFloat((float)(color & 255) / 255.0F);
            this.f1476.putFloat(Math.max(0.0F, Math.min(1.0F, alpha)));
            this.f1476.flip();
            this.m1796(v11);
         }
      }
   }

   
}
