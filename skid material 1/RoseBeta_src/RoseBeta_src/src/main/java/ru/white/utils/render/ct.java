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
public class ct {
   private GpuBuffer f1537;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/scanlines");
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(ct.VERTEX_SHADER)
         .withFragmentShader(ct.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("ScanLinesData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final int BUFFER_SIZE = 64;
   private GpuBuffer f1538;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private ByteBuffer f1539;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/scanlines");
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/scanlines");
   private boolean f1540;

   public void close() {

      if (this.f1537 != null) {
         this.f1537.close();
         this.f1537 = null;
      }

      if (this.f1538 != null) {
         this.f1538.close();
         this.f1538 = null;
      }

      if (this.f1539 != null) {
         MemoryUtil.memFree(this.f1539);
         this.f1539 = null;
      }

      this.f1540 = false;
   }

   private void m1867(MinecraftClient client) {

      int v2 = this.f1539.remaining();
      if (this.f1537 == null || this.f1537.size() < (long)v2) {
         if (this.f1537 != null) {
            this.f1537.close();
         }

         GpuDevice v13 = RenderSystem.getDevice();
         int v8 = 211812369;

         this.f1537 = v13.createBuffer(() -> {

            return "client:scanlines_uniform";
         }, 136, (long)v2);
      }

      CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
      v3.writeToBuffer(this.f1537.slice(), this.f1539);
      GpuBufferSlice v4 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v3.createRenderPass(() -> {
         int var10001x = -958660325;
         int v0 = 119215574;
         int v1 = -1342097445;
         int var10003x = v1 * (v1 + 1);

         return StrDec.d("223oGWMANprxivUKjjADoDOllDuK", var10001x);
      }, client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());

      try {
         v5.setPipeline(PIPELINE);
         v5.setVertexBuffer(0, this.f1538);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("ScanLinesData", this.f1537);
         v5.draw(0, 6);
      } catch (Throwable v12) {
         if (v5 != null) {
            try {
               v5.close();
            } catch (Throwable v11) {
               v12.addSuppressed(v11);
            }
         }

         throw v12;
      }

      if (v5 != null) {
         v5.close();
      }
   }

   private void m1866() {
      if (!this.f1540) {
         this.f1539 = MemoryUtil.memAlloc(64);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         int v2 = 426063151;
         int temp1 = v2 * (v2 + 1);

         v1.flip();
         this.f1538 = RenderSystem.getDevice().createBuffer(() -> {
            String result = "client:scanlines_dummy_vertex";
            int v0 = 61835775;

            return result;
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1540 = true;
      }
   }

   public void draw(float width, float height, float alpha, int color, float step, float lineWidth, float edgeFade, float period, float tower) {
      if (!(alpha <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F) && !(step <= 0.0F)) {
         MinecraftClient v10 = MinecraftClient.method_1551();
         if (v10.method_1522() != null) {
            this.m1866();
            wr.flushPending();
            float v11 = (float)(System.currentTimeMillis() % (long)Math.max(1.0F, period)) / Math.max(1.0F, period);
            this.f1539.clear();
            this.f1539.putFloat((float)v10.method_22683().method_4489() / 2.0F);
            this.f1539.putFloat((float)v10.method_22683().method_4506() / 2.0F);
            this.f1539.putFloat(width);
            this.f1539.putFloat(height);
            this.f1539.putFloat(step);
            this.f1539.putFloat(lineWidth);
            this.f1539.putFloat(edgeFade);
            this.f1539.putFloat(tower);
            this.f1539.putFloat((float)(color >> 16 & 255) / 255.0F);

            float v15 = (float)(color >> 8 & 255);

            this.f1539.putFloat(v15 / 255.0F);
            this.f1539.putFloat((float)(color & 255) / 255.0F);
            this.f1539.putFloat(Math.max(0.0F, Math.min(1.0F, alpha)));

            this.f1539.putFloat(v11);
            this.f1539.putFloat(0.0F);
            this.f1539.putFloat(0.0F);
            this.f1539.putFloat(0.0F);
            this.f1539.flip();
            this.m1867(v10);
         }
      }
   }

   
}
