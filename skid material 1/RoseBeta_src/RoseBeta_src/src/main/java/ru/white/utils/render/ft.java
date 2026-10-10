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
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
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
public class ft {
   private int f1557;
   private GpuBuffer f1551;
   private static final int UBO_SIZE = 112;
   private GpuBuffer f1550;
   private final int[] f1548;
   private int f1556;
   private static final RenderPipeline UP_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/shader_esp_up"))
         .withVertexShader(ft.VSH)
         .withFragmentShader(Identifier.method_60655("client", "core/shader_esp_up"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("EspKawaseData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline DOWN_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/shader_esp_down"))
         .withVertexShader(ft.VSH)
         .withFragmentShader(Identifier.method_60655("client", "core/shader_esp_down"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("EspKawaseData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private ByteBuffer f1553;
   private static final int MAX_MIPS = 5;
   private ByteBuffer f1554;
   private static final Identifier VSH = Identifier.method_60655("client", "core/shader_esp_glow");
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private final GpuTextureView[] f1547;
   private final int[] f1549;
   private GpuBuffer f1552;
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final RenderPipeline COMBINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/shader_esp_combine"))
         .withVertexShader(VSH)
         .withFragmentShader(Identifier.method_60655("client", "core/shader_esp_combine"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("EspData", UniformType.field_60031)
         .withSampler("maskTexture")
         .withSampler("glowTexture")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private final GpuTexture[] f1546 = new GpuTexture[5];
   private boolean f1555;

   private void m1877(int argb) {
      this.f1554.putFloat((float)(argb >> 16 & 255) / 255.0F);
      int temp1 = argb >> 8 & 255;

      this.f1554.putFloat((float)temp1 / 255.0F);

      this.f1554.putFloat((float)(argb & 255) / 255.0F);
   }

   private static float m1878(float v, float min, float max) {

      return v < min ? min : Math.min(v, max);
   }

   public void render(GpuTextureView maskView, GpuTextureView targetView, int width, int height, ft.gt p) {
      this.m1872();
      this.m1873(width, height);
      float v6 = m1878(p.glowRadius, 0.0F, 1.0F);
      int v7 = Math.max(1, Math.min(5, 1 + Math.round(v6 * 4.0F)));
      float v8 = 0.5F + v6 * 3.5F;
      CommandEncoder v9 = RenderSystem.getDevice().createCommandEncoder();
      GpuSampler v10 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      GpuTextureView v11 = maskView;
      int v12 = width;
      int v13 = height;

      for (int v14 = 0; v14 < v7; v14++) {
         this.m1875(v9, 0.5F / (float)v12, 0.5F / (float)v13, 1.0F);
         GpuBufferSlice v15 = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         GpuTextureView v16 = v11;
         RenderPass v18 = v9.createRenderPass(() -> {
            int v2 = -322707039;
            int result = v2 * (v2 + 1);

            return "minecraft:shader_esp_down_" + v14;
         }, this.f1547[v14], OptionalInt.of(0));

         try {
            v18.setPipeline(DOWN_PIPELINE);
            v18.setVertexBuffer(0, this.f1552);
            v18.bindTexture("Sampler0", v16, v10);
            RenderSystem.bindDefaultUniforms(v18);
            v18.setUniform("DynamicTransforms", v15);
            v18.setUniform("EspKawaseData", this.f1550);
            v18.draw(0, 6);
         } catch (Throwable v26) {
            if (v18 != null) {
               try {
                  v18.close();
               } catch (Throwable v24) {
                  v26.addSuppressed(v24);
               }
            }

            throw v26;
         }

         if (v18 != null) {
            v18.close();
         }

         v11 = this.f1547[v14];
         v12 = this.f1548[v14];
         v13 = this.f1549[v14];
      }

      for (int v28 = v7 - 1; v28 > 0; v28--) {
         this.m1875(v9, 0.5F / (float)this.f1548[v28], 0.5F / (float)this.f1549[v28], v8);
         GpuBufferSlice v30 = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         int v32 = v28;
         RenderPass v17 = v9.createRenderPass(() -> {
            int v1 = v32 - 1;
            StringBuilder result = new StringBuilder().append("minecraft:shader_esp_up_").append(v1);
            int v2 = -1468717713;
            int var10001x = v2 * (v2 + 1);

            return result.toString();
         }, this.f1547[v32 - 1], OptionalInt.of(0));

         try {
            v17.setPipeline(UP_PIPELINE);
            v17.setVertexBuffer(0, this.f1552);
            v17.bindTexture("Sampler0", this.f1547[v32], v10);
            RenderSystem.bindDefaultUniforms(v17);
            v17.setUniform("DynamicTransforms", v30);
            v17.setUniform("EspKawaseData", this.f1550);
            v17.draw(0, 6);
         } catch (Throwable v25) {
            if (v17 != null) {
               try {
                  v17.close();
               } catch (Throwable v23) {
                  v25.addSuppressed(v23);
               }
            }

            throw v25;
         }

         if (v17 != null) {
            v17.close();
         }
      }

      this.m1876(v9, width, height, p);
      GpuBufferSlice v29 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass v31 = v9.createRenderPass(() -> {

         String result = "minecraft:shader_esp_combine";
         int v0 = -331348945;
         int var10001x = v0 * (v0 + 1);

         return result;
      }, targetView, OptionalInt.empty());

      try {
         v31.setPipeline(COMBINE_PIPELINE);
         v31.setVertexBuffer(0, this.f1552);
         v31.bindTexture("maskTexture", maskView, v10);

         v31.bindTexture("glowTexture", this.f1547[0], v10);
         RenderSystem.bindDefaultUniforms(v31);
         v31.setUniform("DynamicTransforms", v29);
         v31.setUniform("EspData", this.f1551);
         v31.draw(0, 6);
      } catch (Throwable v27) {
         if (v31 != null) {
            try {
               v31.close();
            } catch (Throwable v22) {
               v27.addSuppressed(v22);
            }
         }

         throw v27;
      }

      if (v31 != null) {
         v31.close();
      }
   }

   private static float m1879(float periodSec) {
      long v1 = Math.max(1L, (long)(periodSec * 1000.0F));
      float result = (float)(System.currentTimeMillis() % v1);
      int v3 = -1532622800;
      int temp2 = v3 * (v3 + 1) % 2;

      if (temp2 != 0) {
         }

      return result / (float)v1;
   }

   private void m1874() {
      for (int v1 = 0; v1 < 5; v1++) {
         if (this.f1547[v1] != null) {
            this.f1547[v1].close();
            this.f1547[v1] = null;
         }

         if (this.f1546[v1] != null) {
            this.f1546[v1].close();
            this.f1546[v1] = null;
         }
      }
   }

   public void close() {
      this.m1874();
      if (this.f1550 != null) {
         this.f1550.close();

         this.f1550 = null;
      }

      if (this.f1551 != null) {
         this.f1551.close();
         this.f1551 = null;
      }

      if (this.f1552 != null) {
         this.f1552.close();
         this.f1552 = null;
      }

      if (this.f1553 != null) {
         MemoryUtil.memFree(this.f1553);
         this.f1553 = null;
      }

      if (this.f1554 != null) {

         MemoryUtil.memFree(this.f1554);
         this.f1554 = null;
      }

      this.f1555 = false;
      this.f1556 = 0;
      this.f1557 = 0;
   }

   public ft() {
      this.f1547 = new GpuTextureView[5];
      this.f1548 = new int[5];
      this.f1549 = new int[5];
      this.f1555 = false;
      this.f1556 = 0;
      this.f1557 = 0;
   }

   private void m1872() {
      if (!this.f1555) {
         this.f1553 = MemoryUtil.memAlloc(16);
         this.f1554 = MemoryUtil.memAlloc(112);

         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1552 = RenderSystem.getDevice().createBuffer(() -> {

            return "minecraft:shader_esp_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1550 = RenderSystem.getDevice().createBuffer(() -> {
            int v0 = 58627508;
            int temp3 = v0 + 1;

            return "minecraft:shader_esp_kawase_ubo";
         }, 136, 893952758075120644L + -893952758075120628L);
         this.f1551 = RenderSystem.getDevice().createBuffer(() -> {
            int var10001x = 1981336285;

            String var10000x = StrDec.d("E6MrqajNqT8gGiy+QPHHEfV7Qohg/t7i", var10001x);

            return var10000x;
         }, 136, 4913502679533949112L + -4913502679533949000L);
         this.f1555 = true;
      }
   }

   private void m1876(CommandEncoder encoder, int width, int height, ft.gt p) {
      this.f1554.clear();
      this.m1877(p.color);
      this.f1554.putFloat(m1878(p.opacity, 0.0F, 1.0F));
      this.m1877(p.friendColor);
      float temp1;
      if (p.friendEnabled) {
         temp1 = 1.0F;
         
      } else {
         temp1 = 0.0F;
      }

      this.f1554.putFloat(temp1);
      this.f1554.putFloat(1.0F / (float)width);
      this.f1554.putFloat(1.0F / (float)height);
      this.f1554.putFloat((float)Math.max(0, Math.min(5, p.outlineWidth)));
      this.f1554.putFloat((float)p.outlineMode);
      this.f1554.putFloat(Math.max(0.0F, p.glowStrength));
      this.f1554.putFloat(Math.max(0.05F, p.glowFalloff));
      this.f1554.putFloat(Math.max(0.0F, p.fillOpacity));
      this.f1554.putFloat(Math.max(0.0F, p.innerGlow));
      this.f1554.putFloat(Math.max(0.0F, p.outlineStrength));
      this.f1554.putFloat(m1878(p.outlineWhite, 0.0F, 1.0F));
      this.f1554.putFloat(p.glowEnabled ? 1.0F : 0.0F);
      this.f1554.putFloat(p.fillEnabled ? 1.0F : 0.0F);
      this.f1554.putFloat(m1879(p.shimmerPeriodSec));
      this.f1554.putFloat(p.shimmerEnabled ? 1.0F : 0.0F);

      this.f1554.putFloat(Math.max(0.005F, p.shimmerWidth));

      this.f1554.putFloat(Math.max(0.0F, p.shimmerBrightness));
      this.f1554.putFloat(p.pulseEnabled ? m1878(p.pulseAmount, 0.0F, 1.0F) : 0.0F);
      this.f1554.putFloat(m1879(1.0F / Math.max(0.05F, p.pulseSpeed)));
      this.f1554.putFloat(Math.max(0.0F, p.saturation));
      this.f1554.putFloat(p.outlineEnabled ? 1.0F : 0.0F);
      this.f1554.flip();
      encoder.writeToBuffer(this.f1551.slice(), this.f1554);
   }

   private void m1873(int width, int height) {
      if (width != this.f1556 || height != this.f1557 || this.f1546[0] == null) {
         this.m1874();
         int v3 = width;
         int v4 = height;
         int v5 = 0;

         while (v5 < 5) {
            v3 = Math.max(1, v3 / 2);
            v4 = Math.max(1, v4 / 2);
            this.f1548[v5] = v3;
            this.f1549[v5] = v4;
            int v8 = v5;
            this.f1546[v5] = RenderSystem.getDevice().createTexture(() -> {
               StringBuilder var10000x = new StringBuilder();
               int temp2 = 969789765;

               return var10000x.append(StrDec.d("i7j+5jDWfHC4Afnx2OoSXm1gl8f4/QCyRw==", temp2)).append(v8).toString();
            }, 12, TextureFormat.RGBA8, v3, v4, 1, 1);
            this.f1547[v5] = RenderSystem.getDevice().createTextureView(this.f1546[v5]);
            v5++;
            
         }

         this.f1556 = width;
         this.f1557 = height;
      }
   }

   private void m1875(CommandEncoder encoder, float halfPixelX, float halfPixelY, float offset) {
      this.f1553.clear();
      this.f1553.putFloat(halfPixelX);
      this.f1553.putFloat(halfPixelY);
      this.f1553.putFloat(offset);
      int v5 = -1489338815;

      this.f1553.putFloat(0.0F);
      this.f1553.flip();
      encoder.writeToBuffer(this.f1550.slice(), this.f1553);
   }

   @Environment(EnvType.CLIENT)
   public static final class gt {
      public int color = -16711681;
      public float pulseAmount;
      public int friendColor = -11141291;
      public float opacity;
      public int outlineMode;
      public float glowStrength;
      public float glowFalloff;
      public boolean pulseEnabled;
      public float shimmerPeriodSec;
      public float glowRadius;
      public float shimmerWidth;
      public boolean friendEnabled = true;
      public boolean glowEnabled;
      public float outlineStrength;
      public boolean shimmerEnabled;
      public float fillOpacity;
      public float outlineWhite;
      public float shimmerBrightness;
      public boolean fillEnabled;
      public float innerGlow;
      public float saturation;
      public boolean outlineEnabled;
      public float pulseSpeed;
      public int outlineWidth;

      public gt() {
         this.opacity = 1.0F;
         this.saturation = 1.0F;
         this.glowEnabled = true;
         this.glowRadius = 0.5F;
         this.glowStrength = 1.4F;
         this.glowFalloff = 1.2F;
         this.fillEnabled = true;
         this.fillOpacity = 0.35F;
         this.innerGlow = 0.5F;
         this.outlineEnabled = true;
         this.outlineWidth = 2;
         this.outlineMode = 0;
         this.outlineStrength = 1.5F;
         this.outlineWhite = 0.5F;
         this.pulseEnabled = false;
         this.pulseSpeed = 1.0F;
         this.pulseAmount = 0.3F;
         this.shimmerEnabled = true;
         this.shimmerWidth = 0.04F;
         this.shimmerPeriodSec = 5.0F;
         this.shimmerBrightness = 0.8F;
      }

      
   }
}
