package ru.white.utils.render.shader;

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
import java.awt.Color;
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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class ju {
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/scanworld_composite");
   private static final int BUFFER_SIZE = 256;
   private ByteBuffer f1656;
   private GpuBuffer[] f1653;
   private static final BlendFunction ADD_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ONE, DestFactor.ONE);
   private int f1654;
   private static final Identifier SHADER_ID = Identifier.method_60655("client", "core/scanworld_composite");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(SHADER_ID)
         .withFragmentShader(SHADER_ID)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("ScanData", UniformType.field_60031)
         .withSampler("DepthSampler")
         .withBlend(ADD_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int UNIFORM_RING = 16;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private boolean f1657;
   private GpuBuffer f1655;

   public void render(
      GpuTextureView targetView,
      GpuTextureView depthView,
      int width,
      int height,
      Vec3d cameraPos,
      Vec3d center,
      Matrix4f invView,
      Matrix4f invProj,
      float radius,
      float bandWidth,
      float fade,
      Color outerColor,
      Color midColor,
      Color innerColor,
      Color scanlineColor
   ) {
      this.m1976();
      this.m1978(width, height, cameraPos, center, invView, invProj, radius, bandWidth, fade, outerColor, midColor, innerColor, scanlineColor);
      GpuBuffer v16 = this.m1977();
      CommandEncoder v17 = RenderSystem.getDevice().createCommandEncoder();
      long temp3 = (long)this.f1656.remaining();

      v17.writeToBuffer(v16.slice(0L, temp3), this.f1656);
      GpuBufferSlice v18 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      GpuSampler v19 = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
      RenderPass v20 = v17.createRenderPass(() -> {
         String result = "client:scanworld_pass";

         return result;
      }, targetView, OptionalInt.empty());

      try {
         v20.setPipeline(PIPELINE);
         v20.setVertexBuffer(0, this.f1655);
         v20.bindTexture("DepthSampler", depthView, v19);
         RenderSystem.bindDefaultUniforms(v20);
         v20.setUniform("DynamicTransforms", v18);
         v20.setUniform("ScanData", v16);
         int temp2 = 6;

         v20.draw(0, temp2);
      } catch (Throwable v26) {
         if (v20 != null) {
            try {
               v20.close();
            } catch (Throwable v25) {
               v26.addSuppressed(v25);
            }
         }

         throw v26;
      }

      if (v20 != null) {
         v20.close();
      }
   }

   private void m1978(
      int width,
      int height,
      Vec3d cameraPos,
      Vec3d center,
      Matrix4f invView,
      Matrix4f invProj,
      float radius,
      float bandWidth,
      float fade,
      Color outerColor,
      Color midColor,
      Color innerColor,
      Color scanlineColor
   ) {
      this.f1656.clear();
      m1980(this.f1656, invView);
      m1980(this.f1656, invProj);
      this.f1656.putFloat((float)cameraPos.field_1352);
      this.f1656.putFloat((float)cameraPos.field_1351);
      this.f1656.putFloat((float)cameraPos.field_1350);
      this.f1656.putFloat(0.0F);

      this.f1656.putFloat((float)center.field_1352);
      this.f1656.putFloat((float)center.field_1351);
      this.f1656.putFloat((float)center.field_1350);
      this.f1656.putFloat(radius);
      m1979(this.f1656, outerColor);
      m1979(this.f1656, midColor);
      m1979(this.f1656, innerColor);

      m1979(this.f1656, scanlineColor);
      this.f1656.putFloat((float)width);
      this.f1656.putFloat((float)height);

      this.f1656.putFloat(bandWidth);
      this.f1656.putFloat((float)(System.currentTimeMillis() % (-680932936419194056L + 680932936420194056L)) / 1000.0F);
      this.f1656.putFloat(fade);
      this.f1656.putFloat(0.0F);
      this.f1656.putFloat(0.0F);
      this.f1656.putFloat(0.0F);
      this.f1656.flip();
   }

   public void close() {
      if (this.f1653 != null) {
         for (int v1 = 0; v1 < this.f1653.length; v1++) {

            if (this.f1653[v1] != null) {
               this.f1653[v1].close();

               this.f1653[v1] = null;
            }
         }

         this.f1653 = null;
      }

      this.f1654 = 0;
      if (this.f1655 != null) {
         this.f1655.close();
         this.f1655 = null;
      }

      if (this.f1656 != null) {
         MemoryUtil.memFree(this.f1656);
         this.f1656 = null;
      }

      this.f1657 = false;
   }

   private void m1976() {
      if (!this.f1657) {
         this.f1656 = MemoryUtil.memAlloc(256);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);

         v1.putInt(0).flip();
         this.f1655 = RenderSystem.getDevice().createBuffer(() -> {
            String result = "client:scanworld_dummy_vertex";

            return result;
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1653 = new GpuBuffer[16];
         this.f1657 = true;
      }
   }

   private static void m1979(ByteBuffer buffer, Color color) {
      buffer.putFloat((float)color.getRed() / 255.0F);
      buffer.putFloat((float)color.getGreen() / 255.0F);

      buffer.putFloat((float)color.getBlue() / 255.0F);
      buffer.putFloat((float)color.getAlpha() / 255.0F);

      
   }

   private GpuBuffer m1977() {
      GpuBuffer v1 = this.f1653[this.f1654];
      if (v1 == null) {
         int v2 = this.f1654;
         GpuDevice result = RenderSystem.getDevice();
         Supplier temp1 = () -> {
            StringBuilder var10000x = new StringBuilder();
            int v2x = -83121389;
            int var10001x = v2x * (v2x + 1);

            return var10000x.append("client:scanworld_uniform_").append(v2).toString();
         };

         v1 = result.createBuffer(temp1, 136, -8005629189162133093L + 8005629189162133349L);

         this.f1653[this.f1654] = v1;
      }

      this.f1654 = (this.f1654 + 1) % (16);
      return v1;
   }

   private static void m1980(ByteBuffer buffer, Matrix4f matrix) {
      ByteBuffer result = buffer.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02());
      float temp1 = matrix.m03();

      result.putFloat(temp1);

      buffer.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
      buffer.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
      buffer.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
   }

   
}
