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
public class ns {
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/main_menu_grid");
   private GpuBuffer f1507;
   private static final int BUFFER_SIZE = 32;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private ByteBuffer f1509;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/main_menu_grid");
   private GpuBuffer f1508;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private boolean f1510;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/main_menu_grid");
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("MainMenuGridData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );

   private void m1831(MinecraftClient client) {

      int v2 = this.f1509.remaining();
      if (this.f1507 == null || this.f1507.size() < (long)v2) {
         if (this.f1507 != null) {
            this.f1507.close();
         }

         this.f1507 = RenderSystem.getDevice().createBuffer(() -> {
            int v1 = -1341416321;
            int temp4 = v1 + 1;

            int var10001x = 1402485369;

            return StrDec.d("uVasjAE7chGRubGs6ABMM1GJw7T/JEnGTeLkVdw=", var10001x);
         }, 136, (long)v2);
      }

      CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
      v3.writeToBuffer(this.f1507.slice(), this.f1509);
      GpuBufferSlice v4 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v3.createRenderPass(() -> {
         int v1 = 1578334592;

         return "client:main_menu_grid_pass";
      }, client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());

      try {
         v5.setPipeline(PIPELINE);
         v5.setVertexBuffer(0, this.f1508);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("MainMenuGridData", this.f1507);
         v5.draw(0, 6);
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

   public void draw(float alpha) {
      if (!(alpha <= 0.01F)) {
         MinecraftClient v2 = MinecraftClient.method_1551();
         if (v2.method_1522() != null) {
            this.m1830();
            wr.flushPending();
            int v3 = v2.method_22683().method_4489();
            int v4 = v2.method_22683().method_4506();
            float v5 = (float)v3 / 2.0F;
            float v6 = (float)v4 / 2.0F;
            this.f1509.clear();
            this.f1509.putFloat(v5);
            this.f1509.putFloat(v6);
            this.f1509.putFloat(2.0F);
            this.f1509.putFloat(alpha);
            this.f1509.putFloat((float)(System.currentTimeMillis() % (8733595528253377880L + -8733595528253277880L)) / 1000.0F);
            this.f1509.putFloat(0.0F);
            this.f1509.putFloat(0.0F);
            this.f1509.putFloat(0.0F);
            this.f1509.flip();
            int v7 = -1156894546;

            this.m1831(v2);
         }
      }
   }

   private void m1830() {
      if (this.f1510) {
         
      } else {
         this.f1509 = MemoryUtil.memAlloc(32);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1508 = RenderSystem.getDevice().createBuffer(() -> {
            int v0 = -128947972;
            int temp3 = v0 + 1;

            return "client:main_menu_grid_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1510 = true;
      }
   }

   public void close() {
      if (this.f1507 != null) {
         this.f1507.close();

         this.f1507 = null;
      }

      if (this.f1508 != null) {
         this.f1508.close();
         this.f1508 = null;
      }

      if (this.f1509 != null) {
         MemoryUtil.memFree(this.f1509);
         this.f1509 = null;
      }

      this.f1510 = false;
   }

   
}
