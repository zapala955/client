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
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.JumpCircle;

@Environment(EnvType.CLIENT)
public final class ls {
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final int BUFFER_SIZE = 128;
   private static final int UNIFORM_RING = 16;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/jump_wave");
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(ls.PIPELINE_ID)
         .withVertexShader(ls.VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("JumpWaveUniforms", UniformType.field_60031)
         .withSampler("SceneSampler")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private ByteBuffer f1486;
   private int f1488 = 0;
   private int f1489 = 0;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/world/jump_wave");
   private GpuTextureView f1487;
   private GpuBuffer[] f1483;
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private boolean f1490 = false;
   private GpuTexture sceneTexture;
   private GpuBuffer f1485;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/jump_wave");
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private int f1484;

   private static float m1812(float value) {
      float v1 = 1.0F - Math.max(0.0F, Math.min(value, 1.0F));
      float temp1 = v1 * v1 * v1;

      return 1.0F - temp1;
   }

   private GpuBuffer m1807() {
      GpuBuffer v1 = this.f1483[this.f1484];
      int temp1 = this.f1484 + 1;

      this.f1484 = temp1 % (16);
      return v1;
   }

   private void m1806() {
      if (!this.f1490) {
         this.f1486 = MemoryUtil.memAlloc(128);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);

         v1.putInt(0).flip();
         this.f1485 = RenderSystem.getDevice().createBuffer(() -> {

            return "client:jump_wave_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1483 = new GpuBuffer[16];
         int v2 = 0;

         while (v2 < 16) {
            int v3 = v2;
            this.f1483[v2] = RenderSystem.getDevice().createBuffer(() -> {
               int v2x = 1209723839;

               int temp1 = v2x * (v2x + 1);

               return "client:jump_wave_ubo_" + v3;
            }, 136, -7758346461268212852L + 7758346461268212980L);
            v2++;
         }

         this.f1490 = true;
      }
   }

   public void renderWaves(List<vj.xj> waves, MatrixStack pose, Matrix4f projectionMatrix, int crestColor) {
      if (waves != null && !waves.isEmpty()) {
         MinecraftClient v5 = MinecraftClient.method_1551();
         if (v5.field_1687 != null && v5.field_1724 != null) {
            Framebuffer v6 = v5.method_1522();
            if (v6 != null && v6.method_30277() != null && v6.method_71639() != null) {
               int v7 = v6.field_1482;
               int v8 = v6.field_1481;
               if (v7 > 1 && v8 > 1) {
                  this.m1806();
                  if (this.m1808(v7, v8)) {
                     CommandEncoder v9 = RenderSystem.getDevice().createCommandEncoder();
                     v9.copyTextureToTexture(v6.method_30277(), this.sceneTexture, 0, 0, 0, 0, 0, v7, v8);
                     Vec3d v10 = v5.field_1773.method_19418().method_71156();
                     float v11 = (float)(crestColor >> 16 & 255) / 255.0F;
                     float v12 = (float)(crestColor >> 8 & 255) / 255.0F;
                     float v13 = (float)(crestColor & 255) / 255.0F;
                     float v14 = (float)(crestColor >> 24 & 255) / 255.0F;
                     if (v14 <= 0.0F) {
                        v14 = 1.0F;
                     }

                     long v15 = System.currentTimeMillis();
                     ArrayList v17 = new ArrayList(waves.size());

                     for (vj.xj v19 : waves) {
                        float v20 = v19.rawProgress(v15);
                        if (!(v20 >= 1.0F)) {
                           float v21 = v19.maxRadius();
                           float v22 = v21 * 1.35F;
                           float v23 = m1812(v20) * 0.74F;
                           float v24 = m1810(v20);
                           float v25 = v19.strength();
                           if (!(v21 <= 0.001F) && !(v25 <= 0.001F) && !(v24 <= 0.003F)) {
                              pose.method_22903();
                              pose.method_22904(
                                 v19.position().field_1352 - v10.field_1352,
                                 v19.position().field_1351 - v10.field_1351 + 0.055,
                                 v19.position().field_1350 - v10.field_1350
                              );
                              Matrix4f v26 = new Matrix4f(pose.method_23760().method_23761());
                              pose.method_22909();
                              Vector3f v27 = new Vector3f(v22, 0.0F, v22).mulPosition(v26);
                              Vector3f v28 = new Vector3f(v22, 0.0F, -v22).mulPosition(v26);
                              Vector3f v29 = new Vector3f(-v22, 0.0F, -v22).mulPosition(v26);
                              Vector3f v30 = new Vector3f(-v22, 0.0F, v22).mulPosition(v26);
                              if (v27.z > -0.05F && v28.z > -0.05F && v29.z > -0.05F) {
                                 float v45;
                                 int result = (v45 = v30.z - -0.05F) == 0.0F ? 0 : (v45 < 0.0F ? -1 : 1);

                                 if (result > 0) {
                                    continue;
                                 }
                              }

                              this.f1486.clear();
                              this.f1486.putFloat(v23);
                              this.f1486.putFloat(v25);
                              this.f1486.putFloat(v24);
                              this.f1486.putFloat(0.0F);
                              this.f1486.putFloat(v11);
                              this.f1486.putFloat(v12);
                              this.f1486.putFloat(v13);
                              this.f1486.putFloat(v14);
                              this.f1486.putFloat(v27.x);
                              this.f1486.putFloat(v27.y);
                              this.f1486.putFloat(v27.z);
                              this.f1486.putFloat(1.0F);
                              this.f1486.putFloat(v28.x);
                              this.f1486.putFloat(v28.y);
                              this.f1486.putFloat(v28.z);
                              this.f1486.putFloat(1.0F);
                              this.f1486.putFloat(v29.x);
                              this.f1486.putFloat(v29.y);
                              this.f1486.putFloat(v29.z);
                              this.f1486.putFloat(1.0F);

                              this.f1486.putFloat(v30.x);
                              this.f1486.putFloat(v30.y);
                              this.f1486.putFloat(v30.z);
                              this.f1486.putFloat(1.0F);
                              this.f1486.flip();
                              GpuBuffer v31 = this.m1807();
                              v9.writeToBuffer(v31.slice(0L, 4561364549472839736L + -4561364549472839640L), this.f1486);
                              v17.add(v31);
                           }
                        }
                     }

                     if (!v17.isEmpty()) {
                        GpuBufferSlice v37 = RenderSystem.getDynamicUniforms()
                           .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
                        GpuSampler v38 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                        GpuTextureView v39 = v6.method_71640();
                        RenderPass v40 = v39 != null ? v9.createRenderPass(() -> {

                           return "client:jump_wave_pass";
                        }, v6.method_71639(), OptionalInt.empty(), v39, OptionalDouble.empty()) : v9.createRenderPass(() -> {
                           int var10001x = -310701864;

                           return StrDec.d("GHxPMqARkagkl0wSUy63nfC0MxBJ", var10001x);
                        }, v6.method_71639(), OptionalInt.empty());

                        try {
                           v40.setPipeline(PIPELINE);
                           v40.setVertexBuffer(0, this.f1485);
                           v40.bindTexture("SceneSampler", this.f1487, v38);
                           RenderSystem.bindDefaultUniforms(v40);
                           v40.setUniform("DynamicTransforms", v37);

                           for (GpuBuffer v42 : v17) {
                              v40.setUniform("JumpWaveUniforms", v42);
                              v40.draw(0, 6);
                           }
                        } catch (Throwable v36) {
                           if (v40 != null) {
                              try {
                                 v40.close();
                                 
                              } catch (Throwable v35) {
                                 v36.addSuppressed(v35);
                              }
                           }

                           throw v36;
                        }

                        if (v40 != null) {
                           v40.close();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean m1808(int width, int height) {
      if (this.sceneTexture != null && width == this.f1488 && height == this.f1489) {

         return true;
      } else {
         this.m1809();

         try {
            this.sceneTexture = RenderSystem.getDevice().createTexture(() -> {

               return "client:jump_wave_scene";
            }, 5, TextureFormat.RGBA8, width, height, 1, 1);
            GpuDevice temp1 = RenderSystem.getDevice();

            this.f1487 = temp1.createTextureView(this.sceneTexture);
            this.f1488 = width;
            this.f1489 = height;
            return true;
         } catch (Throwable v6) {
            this.m1809();
            return false;
         }
      }
   }

   private static float m1811(float value) {
      float v1 = Math.max(0.0F, Math.min(value, 1.0F));

      float v5 = v1 * v1;
      int v3 = 1783223101;
      int temp3 = v3 + 1;

      return v5 * (3.0F - 2.0F * v1);
   }

   public void close() {
      this.m1809();
      if (this.f1485 != null) {
         this.f1485.close();
         this.f1485 = null;
      }

      if (this.f1483 != null) {
         GpuBuffer[] v1 = this.f1483;

         for (GpuBuffer v4 : v1) {
            if (v4 != null) {
               v4.close();
            }
         }

         this.f1483 = null;
      }

      if (this.f1486 != null) {
         MemoryUtil.memFree(this.f1486);
         this.f1486 = null;
      }

      this.f1490 = false;
   }

   private void m1809() {
      if (this.f1487 != null) {
         this.f1487.close();
         this.f1487 = null;
      }

      if (this.sceneTexture != null) {

         this.sceneTexture.close();
         this.sceneTexture = null;
      }

      this.f1488 = 0;
      this.f1489 = 0;
   }

   private static float m1810(float raw) {
      float result = Math.max(0.0F, Math.min(raw, 1.0F));

      float v1 = result;
      float v2 = m1811(v1 / 0.18F);

      float v3 = m1811((1.0F - v1) / 0.82F);
      return Math.max(0.0F, Math.min(v2 * v3, 1.0F));
   }

   
}
