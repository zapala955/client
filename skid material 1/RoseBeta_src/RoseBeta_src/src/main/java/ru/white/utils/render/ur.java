package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
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
public class ur {
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int BUFFER_SIZE = 80;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/clickgui_dots");
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(ur.PIPELINE_ID)
         .withVertexShader(ur.VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("ClickGuiDotsData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer f1330;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/clickgui_dots");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private boolean f1333;
   private GpuBuffer f1331;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/clickgui_dots");
   private ByteBuffer f1332;

   private void m1727(int color, float alphaMul) {
      this.f1332.putFloat((float)(color >> 16 & 255) / 255.0F);
      this.f1332.putFloat((float)(color >> 8 & 255) / 255.0F);
      this.f1332.putFloat((float)(color & 255) / 255.0F);

      this.f1332.putFloat((float)(color >> 24 & 255) / 255.0F * alphaMul);
   }

   public void draw(float mouseX, float mouseY, float alpha, int primaryColor, int secondaryColor) {
      if (!(alpha <= 0.01F)) {
         MinecraftClient v6 = MinecraftClient.method_1551();
         if (v6.method_1522() != null) {
            this.m1726();
            wr.flushPending();
            int v7 = v6.method_22683().method_4489();
            int v8 = v6.method_22683().method_4506();
            float v9 = (float)v7 / 2.0F;
            float result = (float)v8 / 2.0F;

            float v10 = result;
            this.f1332.clear();
            this.f1332.putFloat(0.0F);
            this.f1332.putFloat(0.0F);
            this.f1332.putFloat(v9);
            this.f1332.putFloat(v10);
            this.f1332.putFloat(v9);
            this.f1332.putFloat(v10);
            this.f1332.putFloat(2.0F);
            this.f1332.putFloat(alpha);
            this.f1332.putFloat(mouseX);
            this.f1332.putFloat(mouseY);

            this.f1332.putFloat((float)(System.currentTimeMillis() % (-7612021587049448760L + 7612021587049548760L)) / 1000.0F);
            this.f1332.putFloat(0.0F);
            this.m1727(primaryColor, alpha);
            this.m1727(secondaryColor, alpha);
            this.f1332.flip();
            this.m1728(v6);
         }
      }
   }

   private void m1726() {
      if (!this.f1333) {
         this.f1332 = MemoryUtil.memAlloc(80);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1331 = RenderSystem.getDevice().createBuffer(() -> {
            String result = "client:clickgui_dots_dummy_vertex";

            return result;
         }, 32, v1);
         MemoryUtil.memFree(v1);

         this.f1333 = true;
      }
   }

   private void m1728(MinecraftClient client) {
      int v2 = this.f1332.remaining();
      if (this.f1330 == null || this.f1330.size() < (long)v2) {
         if (this.f1330 != null) {
            this.f1330.close();
         }

         this.f1330 = RenderSystem.getDevice().createBuffer(() -> {
            int v0 = -45462592;
            int temp2 = v0 * (v0 + 1);

            return "client:clickgui_dots_uniform";
         }, 136, (long)v2);
      }

      CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice temp1 = this.f1330.slice();

      v3.writeToBuffer(temp1, this.f1332);
      GpuBufferSlice v4 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v3.createRenderPass(() -> {
         String result = "client:clickgui_dots_pass";
         int v0 = -1990816299;

         return result;
      }, client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());

      try {
         v5.setPipeline(PIPELINE);
         v5.setVertexBuffer(0, this.f1331);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("ClickGuiDotsData", this.f1330);
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
      if (this.f1330 != null) {
         this.f1330.close();
         this.f1330 = null;
      }

      if (this.f1331 != null) {
         this.f1331.close();

         this.f1331 = null;
      }

      if (this.f1332 != null) {
         MemoryUtil.memFree(this.f1332);
         this.f1332 = null;
      }

      this.f1333 = false;
   }

   
}
