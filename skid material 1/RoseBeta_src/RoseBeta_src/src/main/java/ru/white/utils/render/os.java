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
public class os {
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private GpuBuffer f1511;
   private boolean f1514 = false;
   private static final int BUFFER_SIZE = 16;
   private GpuBuffer f1512;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/mask_diff");
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(os.PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(os.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("MaskData", UniformType.field_60031)
         .withSampler("BeforeSampler")
         .withSampler("AfterSampler")
         .withSampler("DepthBeforeSampler")
         .withSampler("DepthAfterSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/mask_diff");
   private ByteBuffer f1513;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/mask_diff");

   private void m1836(int width, int height) {
      this.f1513.clear();
      this.f1513.putFloat((float)width);
      this.f1513.putFloat((float)height);
      this.f1513.putFloat(0.0F);

      this.f1513.putFloat(0.0F);
      this.f1513.flip();
   }

   private void m1835() {
      if (!this.f1514) {
         this.f1513 = MemoryUtil.memAlloc(16);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);

         v1.putInt(0);
         v1.flip();
         this.f1512 = RenderSystem.getDevice().createBuffer(() -> {
            int v0 = -186484899;
            int temp3 = v0 * (v0 + 1) % 2;

            if (temp3 != 0) {
               }

            return "minecraft:mask_diff_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1514 = true;
         
      }
   }

   public void close() {
      if (this.f1511 != null) {
         this.f1511.close();
         this.f1511 = null;
      }

      if (this.f1512 != null) {
         this.f1512.close();
         this.f1512 = null;
      }

      if (this.f1513 != null) {
         int v1 = 2100764983;
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         MemoryUtil.memFree(this.f1513);
         this.f1513 = null;
      }

      this.f1514 = false;
   }

   public void createMask(
      GpuTextureView targetView,
      GpuTextureView beforeView,
      GpuTextureView afterView,
      GpuTextureView depthBeforeView,
      GpuTextureView depthAfterView,
      int width,
      int height
   ) {
      this.m1835();
      CommandEncoder v8 = RenderSystem.getDevice().createCommandEncoder();
      GpuSampler v9 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      GpuSampler v10 = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
      this.m1836(width, height);
      int v11 = this.f1513.remaining();

      if (this.f1511 == null || this.f1511.size() < (long)v11) {
         if (this.f1511 != null) {
            this.f1511.close();
         }

         GpuDevice v21 = RenderSystem.getDevice();
         Supplier temp2 = () -> {

            int v3 = 1711791754;
            int v0 = 568040734;
            int var10002x = v0 * (v0 + 1);

            return StrDec.d("RLc0uf/Zti93Di2nBeriF5RsSLcd8cr7uBJ5", v3);
         };

         this.f1511 = v21.createBuffer(temp2, 136, (long)v11);
      }

      v8.writeToBuffer(this.f1511.slice(), this.f1513);
      GpuBufferSlice v12 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v13 = v8.createRenderPass(() -> {

         return "minecraft:mask_diff_pass";
      }, targetView, OptionalInt.of(0));

      try {
         v13.setPipeline(PIPELINE);
         v13.setVertexBuffer(0, this.f1512);
         v13.bindTexture("BeforeSampler", beforeView, v9);
         v13.bindTexture("AfterSampler", afterView, v9);
         v13.bindTexture("DepthBeforeSampler", depthBeforeView, v10);
         v13.bindTexture("DepthAfterSampler", depthAfterView, v10);
         RenderSystem.bindDefaultUniforms(v13);
         v13.setUniform("DynamicTransforms", v12);
         v13.setUniform("MaskData", this.f1511);
         v13.draw(0, 6);
      } catch (Throwable v20) {

         if (v13 != null) {
            try {
               v13.close();
            } catch (Throwable v19) {
               v20.addSuppressed(v19);
            }
         }

         throw v20;
      }

      if (v13 != null) {
         v13.close();
      }
   }

   
}
