package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
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
public class fs {
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private int f1471;
   private int f1472;
   private static final int BUFFER_SIZE = 16;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/grayscale");
   private GpuBuffer f1467;
   private GpuTexture sceneTexture;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/grayscale");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private GpuTextureView f1469;
   private GpuBuffer f1468;
   private ByteBuffer f1470;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(fs.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("GrayscaleData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private boolean f1473;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/grayscale");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

   public void draw(float state) {
      if (!(state <= 0.01F)) {
         MinecraftClient v2 = MinecraftClient.method_1551();
         if (v2 != null && v2.method_1522() != null) {
            if (v2.method_1522().method_30277() != null) {
               this.m1789();
               int v3 = v2.method_1522().field_1482;
               Framebuffer result = v2.method_1522();

               int v4 = result.field_1481;

               if (v3 > 0 && v4 > 0) {
                  this.m1790(v3, v4);
                  wr.flushPending();
                  this.f1470.clear();
                  this.f1470.putFloat((float)v2.method_22683().method_4489() / 2.0F);
                  this.f1470.putFloat((float)v2.method_22683().method_4506() / 2.0F);
                  this.f1470.putFloat(2.0F);
                  this.f1470.putFloat(Math.max(0.0F, Math.min(1.0F, state)));
                  this.f1470.flip();
                  CommandEncoder v5 = RenderSystem.getDevice().createCommandEncoder();
                  v5.copyTextureToTexture(v2.method_1522().method_30277(), this.sceneTexture, 0, 0, 0, 0, 0, v3, v4);
                  v5.writeToBuffer(this.f1467.slice(), this.f1470);
                  GpuBufferSlice v6 = RenderSystem.getDynamicUniforms()
                     .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
                  GpuSampler v7 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                  RenderPass v8 = v5.createRenderPass(() -> {

                     return "client:grayscale_pass";
                  }, v2.method_1522().method_71639(), OptionalInt.empty(), v2.method_1522().method_71640(), OptionalDouble.empty());

                  try {
                     v8.setPipeline(PIPELINE);
                     v8.setVertexBuffer(0, this.f1468);
                     v8.bindTexture("Sampler0", this.f1469, v7);
                     RenderSystem.bindDefaultUniforms(v8);
                     v8.setUniform("DynamicTransforms", v6);
                     v8.setUniform("GrayscaleData", this.f1467);
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
            }
         }
      }
   }

   private void m1789() {
      if (!this.f1473) {
         this.f1470 = MemoryUtil.memAlloc(16);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         int v3 = 1067760004;
         int result = v3 * (v3 + 1);

         this.f1468 = RenderSystem.getDevice().createBuffer(() -> {

            return "client:grayscale_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1467 = RenderSystem.getDevice().createBuffer(() -> {

            return "client:grayscale_uniform";
         }, 136, 881262214022023873L + -881262214022023857L);
         this.f1473 = true;
      }
   }

   public void close() {
      if (this.f1469 != null) {
         this.f1469.close();
         this.f1469 = null;
      }

      if (this.sceneTexture != null) {
         this.sceneTexture.close();
         this.sceneTexture = null;
      }

      if (this.f1467 != null) {
         this.f1467.close();
         this.f1467 = null;
      }

      if (this.f1468 != null) {
         this.f1468.close();

         this.f1468 = null;
      }

      if (this.f1470 != null) {
         MemoryUtil.memFree(this.f1470);
         this.f1470 = null;
      }

      this.f1471 = 0;

      this.f1472 = 0;
      this.f1473 = false;
   }

   private void m1790(int width, int height) {
      if (this.sceneTexture != null) {

         if (width == this.f1471 && height == this.f1472) {
            return;
         }
      }

      if (this.f1469 != null) {
         this.f1469.close();
         this.f1469 = null;
      }

      if (this.sceneTexture != null) {
         this.sceneTexture.close();
         this.sceneTexture = null;
      }

      this.sceneTexture = RenderSystem.getDevice().createTexture(() -> {

         return "client:grayscale_scene";
      }, 5, TextureFormat.RGBA8, width, height, 1, 1);
      this.f1469 = RenderSystem.getDevice().createTextureView(this.sceneTexture);
      this.f1471 = width;
      this.f1472 = height;
   }

   
}
