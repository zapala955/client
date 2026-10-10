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
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import ru.white.module.impl.render.ShaderSky;

@Environment(EnvType.CLIENT)
public class it {
   private static final RenderPipeline SHADER_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(it.SHADER_PIPELINE_ID)
         .withVertexShader(it.VERTEX_SHADER)
         .withFragmentShader(it.SHADER_FRAGMENT)
         .withVertexFormat(VertexFormats.field_1592, DrawMode.field_27379)
         .withUniform("ShaderSkyData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/shader_sky");
   private boolean enabled;
   private static final Identifier BLUR_FRAGMENT = Identifier.method_60655("client", "core/shader_sky_blur");
   private static final RenderPipeline BLUR_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(it.BLUR_PIPELINE_ID)
         .withVertexShader(it.FULLSCREEN_VERTEX_SHADER)
         .withFragmentShader(BLUR_FRAGMENT)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("ShaderSkyData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier FULLSCREEN_VERTEX_SHADER = Identifier.method_60655(
      "client", "core/shader_sky_fullscreen"
   );
   private static int f1566 = -1;
   private static final Identifier BLUR_PIPELINE_ID = Identifier.method_60655("client", "pipeline/shader_sky_blur");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private final ms f1559 = new ms();
   private static final int BUFFER_SIZE = 96;
   private static final Identifier SHADER_PIPELINE_ID = Identifier.method_60655("client", "pipeline/shader_sky");
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private boolean f1564 = false;
   private GpuBuffer f1560;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private long f1565;
   private static final Identifier SHADER_FRAGMENT = Identifier.method_60655("client", "core/shader_sky");
   private static it instance;
   private ByteBuffer f1563;
   private GpuBuffer f1561;
   private GpuBuffer f1562;
   private final MinecraftClient client;

   private void m1891(Framebuffer framebuffer) {
      GpuDevice result = RenderSystem.getDevice();

      CommandEncoder v2 = result.createCommandEncoder();
      Matrix4f v3 = new Matrix4f(RenderSystem.getModelViewMatrix());
      v3.m30(0.0F);
      v3.m31(0.0F);
      v3.m32(0.0F);
      GpuBufferSlice v4 = RenderSystem.getDynamicUniforms().method_71106(v3, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v5 = v2.createRenderPass(
         () -> "client:shader_sky_pass",
         framebuffer.method_71639(),
         OptionalInt.empty(),
         framebuffer.method_71640(),
         OptionalDouble.empty()
      );

      try {
         v5.setPipeline(SHADER_PIPELINE);
         v5.setVertexBuffer(0, this.f1562);
         RenderSystem.bindDefaultUniforms(v5);
         v5.setUniform("DynamicTransforms", v4);
         v5.setUniform("ShaderSkyData", this.f1560);
         v5.draw(0, getDomeVertexCount());
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

   private static int m1893(String mode) {
      byte v2 = -1;
      switch (mode.hashCode()) {
         case 83201:
            if (mode.equals("Sky")) {
               v2 = 2;
            }
            break;
         case 2222509:
            if (mode.equals("Glow")) {
               v2 = 4;
            }
            break;
         case 2581923:

            if (mode.equals("Snow")) {
               v2 = 1;
            }
            break;
         case 2587250:
            boolean result = mode.equals("Star");

            if (result) {
               v2 = 3;
            }
            break;
         case 75265016:
            if (mode.equals("Night")) {
               v2 = 0;
            }
      }
      return switch (v2) {
         case 0 -> 1;
         case 1 -> 2;
         case 2 -> 3;
         case 3 -> 4;
         case 4 -> 5;
         default -> 0;
      };
   }

   public void renderSky() {
      pk v1 = pk.getInstance();
      if (this.enabled && v1 != null && v1.isEnabled()) {

         if (v1.mode.is("Blur")) {
            Framebuffer v2 = this.client.method_1522();
            if (v2 != null) {

               if (v2.method_71639() != null) {
                  this.m1888();
                  this.m1889(v2, v1);
                  return;
               }
            }
         }
      }
   }

   public static int getDomeVertexCount() {
      if (f1566 < 0) {
         int v0 = 16;

         int v1 = 32;

         int v2 = v1 + (v0 - 1) * v1 * 2;
         f1566 = v2 * 3;
      }

      return f1566;
   }

   private void m1895(Framebuffer framebuffer, RenderPipeline pipeline, GpuTextureView texture) {
      CommandEncoder v4 = RenderSystem.getDevice().createCommandEncoder();
      GpuBufferSlice v5 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v6 = v4.createRenderPass(() -> {

         return "client:shader_sky_pass";
      }, framebuffer.method_71639(), OptionalInt.empty(), framebuffer.method_71640(), OptionalDouble.empty());

      try {
         v6.setPipeline(pipeline);

         v6.setVertexBuffer(0, this.f1561);
         if (texture != null) {
            GpuSampler v7 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            v6.bindTexture("Sampler0", texture, v7);
         }

         RenderSystem.bindDefaultUniforms(v6);
         String temp1 = "DynamicTransforms";

         v6.setUniform(temp1, v5);
         v6.setUniform("ShaderSkyData", this.f1560);
         v6.draw(0, 6);
      } catch (Throwable v13) {
         if (v6 != null) {
            try {
               v6.close();
            } catch (Throwable v12) {

               v13.addSuppressed(v12);
            }
         }

         throw v13;
      }

      if (v6 != null) {
         v6.close();
      }
   }

   public void renderCelestialShader() {
      pk v1 = pk.getInstance();
      if (this.enabled && v1 != null && v1.isEnabled() && !v1.mode.is("Blur")) {
         Framebuffer v2 = this.client.method_1522();

         if (v2 != null && v2.method_71639() != null) {
            this.m1888();
            this.m1890(v2.field_1482, v2.field_1481, v1);
            this.m1891(v2);
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         this.m1888();

         this.f1565 = System.currentTimeMillis();
      }
   }

   private void m1890(int width, int height, pk module) {
      float v4 = m1894(module);
      Color v5 = new Color(pk.geteColor1(), true);
      Color v6 = new Color(pk.geteColor2(), true);
      Color v7 = Color.WHITE;
      this.f1563.clear();
      this.f1563.putFloat((float)width);
      this.f1563.putFloat((float)height);
      ByteBuffer result = this.f1563;
      long temp1 = System.currentTimeMillis();

      result.putFloat((float)(temp1 - this.f1565) / 1000.0F);
      this.f1563.putFloat(v4);
      m1892(this.f1563, v5, v4);
      m1892(this.f1563, v6, v4);
      m1892(this.f1563, v7, v4);
      this.f1563.putFloat((float)m1893(module.mode.getValue()));
      this.f1563.putFloat(module.speed.getValue());
      this.f1563.putFloat(module.scale.getValue());
      this.f1563.putFloat(module.intensity.getValue());
      this.f1563.putFloat(module.stars.getValue());
      this.f1563.putFloat(module.vanillaSky.getValue() ? 1.0F : 0.0F);
      this.f1563.putFloat(0.0F);
      this.f1563.putFloat(0.0F);
      this.f1563.flip();
      RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.f1560.slice(), this.f1563);
   }

   public static it getInstance() {
      if (instance == null) {
         it result = new it;

         result./* $VF: Unable to resugar constructor */<init>();
         instance = result;
      }

      return instance;
   }

   public it() {
      this.enabled = false;
      this.f1565 = System.currentTimeMillis();
      this.client = MinecraftClient.method_1551();
      instance = this;
   }

   public void close() {
      if (this.f1560 != null) {
         this.f1560.close();
         this.f1560 = null;
      }

      if (this.f1561 != null) {
         this.f1561.close();

         this.f1561 = null;
      }

      if (this.f1562 != null) {
         this.f1562.close();
         this.f1562 = null;
      }

      if (this.f1563 != null) {
         MemoryUtil.memFree(this.f1563);

         this.f1563 = null;
      }

      this.f1559.close();
      this.f1564 = false;
   }

   private static ByteBuffer m1896() {
      float v0 = 512.0F;
      int v1 = 16;
      int v2 = 32;
      ArrayList result = new ArrayList;

      result./* $VF: Unable to resugar constructor */<init>();
      ArrayList v3 = result;

      for (int v4 = 0; v4 < v1; v4++) {
         float v5 = (float)v4 / (float)v1 * (float) (Math.PI / 2);
         int v43 = v4 + 1;

         float v6 = (float)v43 / (float)v1 * (float) (Math.PI / 2);
         float v7 = (float)Math.cos((double)v5);
         float v8 = (float)Math.cos((double)v6);
         float v9 = (float)Math.sin((double)v5);
         float v10 = (float)Math.sin((double)v6);

         for (int v11 = 0; v11 < v2; v11++) {
            float v12 = (float)v11 / (float)v2 * (float) (Math.PI * 2);
            float v13 = (float)(v11 + 1) / (float)v2 * (float) (Math.PI * 2);
            float v14 = (float)Math.cos((double)v12);
            float v15 = (float)Math.sin((double)v12);
            float v16 = (float)Math.cos((double)v13);
            float v17 = (float)Math.sin((double)v13);
            float v18 = v9 * v14;
            float v19 = v9 * v15;
            float v20 = v10 * v14;
            float v21 = v10 * v15;
            float v22 = v10 * v16;
            float v23 = v10 * v17;
            float v24 = v9 * v16;
            float v25 = v9 * v17;
            float v26 = v18 * v0;
            float v27 = v7 * v0;
            float v28 = v19 * v0;
            float v29 = v20 * v0;
            float v30 = v8 * v0;
            float v31 = v21 * v0;
            float v32 = v22 * v0;
            float v33 = v8 * v0;
            float v34 = v23 * v0;
            float v35 = v24 * v0;
            float v36 = v7 * v0;
            float v37 = v25 * v0;
            if (v4 == 0) {
               v3.add(v26);
               v3.add(v27);
               v3.add(v28);
               v3.add(v29);
               v3.add(v30);
               v3.add(v31);
               v3.add(v32);
               v3.add(v33);
               v3.add(v34);
            } else {
               v3.add(v26);
               v3.add(v27);
               v3.add(v28);
               v3.add(v29);
               v3.add(v30);
               v3.add(v31);
               v3.add(v32);
               v3.add(v33);
               v3.add(v34);
               v3.add(v26);
               v3.add(v27);
               v3.add(v28);
               v3.add(v32);
               v3.add(v33);
               v3.add(v34);
               v3.add(v35);
               v3.add(v36);
               v3.add(v37);
            }
         }
      }

      ByteBuffer v40 = MemoryUtil.memAlloc(v3.size() * 4);

      for (float v42 : v3) {
         v40.putFloat(v42);
      }

      v40.flip();
      return v40;
   }

   private static void m1892(ByteBuffer buffer, Color color, float opacity) {
      float temp1 = (float)color.getRed();

      buffer.putFloat(temp1 / 255.0F);
      buffer.putFloat((float)color.getGreen() / 255.0F);
      buffer.putFloat((float)color.getBlue() / 255.0F);
      buffer.putFloat((float)color.getAlpha() / 255.0F * opacity);
   }

   public void invalidate() {

      this.enabled = false;
      this.close();
   }

   private void m1889(Framebuffer framebuffer, pk module) {
      if (framebuffer.method_30277() != null && framebuffer.method_71639() != null) {
         ms result = this.f1559;
         GpuTexture temp1 = framebuffer.method_30277();
         GpuTextureView temp2 = framebuffer.method_71639();

         GpuTextureView v3 = result.blur(temp1, temp2, framebuffer.field_1482, framebuffer.field_1481, 3, 3.0F);
         if (v3 != null) {
            this.m1890(framebuffer.field_1482, framebuffer.field_1481, module);

            this.m1895(framebuffer, BLUR_PIPELINE, v3);
         }
      }
   }

   private void m1888() {
      if (!this.f1564) {
         this.f1563 = MemoryUtil.memAlloc(96);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1561 = RenderSystem.getDevice().createBuffer(() -> {
            int temp1 = -1921336362;
            int v0 = -1613326269;

            return StrDec.d("FtdOUq66kNE3MFlIWLuz89gwN3ZZl6cp/WAbjXvo", temp1);
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1560 = RenderSystem.getDevice().createBuffer(() -> {
            int temp1 = 152751778;

            return StrDec.d("YqIu1trP8FVDRTnMLM7Td6xFRukp6dGAkg==", temp1);
         }, 136, 5439893203169677288L + -5439893203169677192L);
         ByteBuffer v2 = m1896();
         this.f1562 = RenderSystem.getDevice().createBuffer(() -> {

            return "client:shader_sky_celestial_vertex";
         }, 32, v2);

         MemoryUtil.memFree(v2);
         this.f1564 = true;
      }
   }

   private static float m1894(pk module) {
      int v1 = -977907367;
      int temp1 = v1 * (v1 + 1);

      if (module.mode.is("Blur")) {

         return module.intensity.getValue();
      } else {
         return module.vanillaSky.getValue() ? 0.78F : 1.0F;
      }
   }

   
}
