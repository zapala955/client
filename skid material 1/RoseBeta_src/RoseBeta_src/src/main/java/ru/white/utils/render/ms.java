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
public class ms {
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private int[] f1500;
   private static final Identifier UP_FRAGMENT_SHADER = Identifier.method_60655("client", "core/kawase_up");
   private static final Identifier DOWN_PIPELINE_ID = Identifier.method_60655("client", "pipeline/kawase_down");
   private static final RenderPipeline UP_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(ms.UP_PIPELINE_ID)
         .withVertexShader(ms.UP_VERTEX_SHADER)
         .withFragmentShader(UP_FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("KawaseData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private int[] f1499;
   private GpuBuffer f1492;
   private static final Identifier DOWN_VERTEX_SHADER = Identifier.method_60655("client", "core/kawase_down");
   private int f1505;
   private GpuTextureView[] f1495;
   private GpuTexture[] f1496;
   private GpuTexture[] f1494;
   private static final int UNIFORM_DATA_SIZE = 16;
   private GpuTexture f1502;
   private static final RenderPipeline DOWN_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(DOWN_PIPELINE_ID)
         .withVertexShader(DOWN_VERTEX_SHADER)
         .withFragmentShader(ms.DOWN_FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("KawaseData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier UP_PIPELINE_ID = Identifier.method_60655("client", "pipeline/kawase_up");
   private int f1504 = 0;
   private GpuTextureView f1503;
   private static final Identifier UP_VERTEX_SHADER = Identifier.method_60655("client", "core/kawase_up");
   private static final Identifier DOWN_FRAGMENT_SHADER = Identifier.method_60655("client", "core/kawase_down");
   private GpuTextureView[] f1497;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private GpuBuffer[] f1491;
   private static final int MAX_ITERATIONS = 8;
   private ByteBuffer f1493;
   private int[] f1498;
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private int[] f1501;
   private static final int MAX_PASSES = 17;
   private boolean f1506;

   private void m1819(int width, int height) {
      if (width != this.f1504 || height != this.f1505) {
         this.m1820();
         this.f1502 = RenderSystem.getDevice().createTexture(() -> {
            int temp1 = -634247698;

            return StrDec.d("IEwBBZsig5MT9R4bZRv7qsaXcjptCA==", temp1);
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1503 = RenderSystem.getDevice().createTextureView(this.f1502);
         int v3 = width;
         int v4 = height;

         for (int v5 = 0; v5 < 8; v5++) {
            v3 = Math.max(1, v3 / 2);
            v4 = Math.max(1, v4 / 2);
            int v6 = v5;
            this.f1498[v5] = v3;
            this.f1499[v5] = v4;
            this.f1500[v5] = v3;
            this.f1501[v5] = v4;

            this.f1494[v5] = RenderSystem.getDevice().createTexture(() -> {
               StringBuilder result = new StringBuilder;
               int v2 = -879877870;
               int temp1 = v2 * (v2 + 1);

               result./* $VF: Unable to resugar constructor */<init>();
               return result.append("minecraft:kawase_down_").append(v6).toString();
            }, 13, TextureFormat.RGBA8, v3, v4, 1, 1);
            this.f1495[v5] = RenderSystem.getDevice().createTextureView(this.f1494[v5]);
            this.f1496[v5] = RenderSystem.getDevice().createTexture(() -> {

               StringBuilder result = new StringBuilder;

               result./* $VF: Unable to resugar constructor */<init>();
               return result.append("minecraft:kawase_up_").append(v6).toString();
            }, 13, TextureFormat.RGBA8, v3, v4, 1, 1);
            this.f1497[v5] = RenderSystem.getDevice().createTextureView(this.f1496[v5]);
         }

         this.f1504 = width;
         this.f1505 = height;
      }
   }

   private void m1818() {
      if (!this.f1506) {
         this.f1493 = MemoryUtil.memAlloc(16);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1492 = RenderSystem.getDevice().createBuffer(() -> {
            int var10001x = -1868549059;
            int v0 = -491016255;
            int var10002x = v0 * (v0 + 1);

            return StrDec.d("812TT0gzEdnA5IxRtgpp4BWE/HOyDFsdBfjHgY0=", var10001x);
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1491 = new GpuBuffer[17];

         for (int v2 = 0; v2 < 17; v2++) {
            int v3 = v2;
            this.f1491[v2] = RenderSystem.getDevice().createBuffer(() -> {
               StringBuilder result = new StringBuilder;

               result./* $VF: Unable to resugar constructor */<init>();

               return result.append("minecraft:kawase_uniform_").append(v3).toString();
            }, 136, 2630028385968071881L + -2630028385968071865L);
         }

         this.f1494 = new GpuTexture[8];
         this.f1495 = new GpuTextureView[8];
         this.f1496 = new GpuTexture[8];
         this.f1497 = new GpuTextureView[8];
         this.f1498 = new int[8];
         this.f1499 = new int[8];
         this.f1500 = new int[8];
         int[] temp1 = new int[8];

         this.f1501 = temp1;
         this.f1506 = true;
      }
   }

   public ms() {
      this.f1505 = 0;
      this.f1506 = false;
   }

   private void m1821(CommandEncoder encoder, GpuBuffer buf, int width, int height, float offset) {
      this.f1493.clear();
      this.f1493.putFloat((float)width);
      this.f1493.putFloat((float)height);
      this.f1493.putFloat(offset);

      this.f1493.putFloat(0.0F);
      this.f1493.flip();
      encoder.writeToBuffer(buf.slice(), this.f1493);
   }

   public GpuTextureView blur(GpuTexture sourceTexture, GpuTextureView sourceView, int width, int height, int iterations, float offset) {
      if (sourceTexture != null && sourceView != null) {
         boolean result = sourceTexture.isClosed();

         if (!result && !sourceView.isClosed()) {
            this.m1818();
            this.m1819(width, height);
            iterations = Math.min(Math.max(iterations, 1), 8);
            CommandEncoder v7 = RenderSystem.getDevice().createCommandEncoder();
            GpuSampler v8 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            GpuBufferSlice v9 = RenderSystem.getDynamicUniforms()
               .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            GpuTextureView v10 = sourceView;
            int v11 = width;
            int v12 = height;
            int v13 = 0;

            for (int v14 = 0; v14 < iterations; v14++) {
               GpuBuffer v15 = this.f1491[v13++];
               this.m1821(v7, v15, v11, v12, offset);
               RenderPass v17 = v7.createRenderPass(() -> {
                  StringBuilder var10000x = new StringBuilder();

                  return var10000x.append("minecraft:kawase_down_").append(v14).toString();
               }, this.f1495[v14], OptionalInt.empty());

               try {
                  v17.setPipeline(DOWN_PIPELINE);
                  v17.setVertexBuffer(0, this.f1492);
                  v17.bindTexture("Sampler0", v10, v8);
                  RenderSystem.bindDefaultUniforms(v17);
                  v17.setUniform("DynamicTransforms", v9);
                  v17.setUniform("KawaseData", v15);
                  v17.draw(0, 6);
               } catch (Throwable v26) {
                  if (v17 != null) {
                     try {
                        v17.close();
                     } catch (Throwable v23) {
                        v26.addSuppressed(v23);
                     }
                  }

                  throw v26;
               }

               if (v17 != null) {
                  v17.close();
               }

               v10 = this.f1495[v14];
               v11 = this.f1498[v14];
               v12 = this.f1499[v14];
            }

            for (int v28 = iterations - 1; v28 >= 0; v28--) {
               GpuBuffer v30 = this.f1491[v13++];
               this.m1821(v7, v30, v11, v12, offset);
               int v16 = v28;
               RenderPass v32 = v7.createRenderPass(() -> {
                  StringBuilder var10000x = new StringBuilder();
                  int temp2 = 160245972;

                  return var10000x.append(StrDec.d("GkG+1qEvPEAp+KHIXxZEefyJ1Ng=", temp2)).append(v16).toString();
               }, this.f1497[v16], OptionalInt.empty());

               try {
                  v32.setPipeline(UP_PIPELINE);
                  v32.setVertexBuffer(0, this.f1492);
                  v32.bindTexture("Sampler0", v10, v8);
                  RenderSystem.bindDefaultUniforms(v32);
                  v32.setUniform("DynamicTransforms", v9);
                  v32.setUniform("KawaseData", v30);
                  v32.draw(0, 6);
               } catch (Throwable v25) {
                  if (v32 != null) {
                     try {
                        v32.close();
                     } catch (Throwable v22) {
                        v25.addSuppressed(v22);
                     }
                  }

                  throw v25;
               }

               if (v32 != null) {
                  v32.close();
               }

               v10 = this.f1497[v28];
               v11 = this.f1500[v28];
               v12 = this.f1501[v28];
            }

            GpuBuffer v29 = this.f1491[v13];
            this.m1821(v7, v29, v11, v12, offset);
            RenderPass v31 = v7.createRenderPass(() -> {
               String var10000x = "minecraft:kawase_final";

               return var10000x;
            }, this.f1503, OptionalInt.empty());

            try {
               v31.setPipeline(UP_PIPELINE);
               v31.setVertexBuffer(0, this.f1492);
               v31.bindTexture("Sampler0", v10, v8);
               RenderSystem.bindDefaultUniforms(v31);
               v31.setUniform("DynamicTransforms", v9);
               v31.setUniform("KawaseData", v29);
               v31.draw(0, 6);
            } catch (Throwable v24) {
               if (v31 != null) {
                  try {
                     v31.close();
                  } catch (Throwable v21) {
                     v24.addSuppressed(v21);
                  }
               }

               throw v24;
            }

            if (v31 != null) {
               v31.close();
            }

            return this.f1503;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private void m1820() {
      if (this.f1503 != null) {
         this.f1503.close();
         this.f1503 = null;
      }

      if (this.f1502 != null) {
         this.f1502.close();
         this.f1502 = null;
      }

      if (this.f1495 != null) {
         for (int v1 = 0; v1 < 8; v1++) {
            if (this.f1495[v1] != null) {
               this.f1495[v1].close();
               this.f1495[v1] = null;
            }

            if (this.f1494[v1] != null) {

               this.f1494[v1].close();
               this.f1494[v1] = null;
            }

            if (this.f1497[v1] != null) {
               this.f1497[v1].close();
               this.f1497[v1] = null;
            }

            if (this.f1496[v1] != null) {
               this.f1496[v1].close();
               this.f1496[v1] = null;
            }
         }
      }
   }

   public void close() {
      this.m1820();
      if (this.f1491 != null) {
         for (GpuBuffer v4 : this.f1491) {
            if (v4 != null) {
               v4.close();
            }
         }

         this.f1491 = null;
      }

      if (this.f1492 != null) {
         this.f1492.close();
         this.f1492 = null;
      }

      if (this.f1493 != null) {
         MemoryUtil.memFree(this.f1493);
         this.f1493 = null;
      }

      this.f1506 = false;
   }

   
}
