package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
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
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class yr {
   private static int f1342 = -1;
   private static GpuTextureView f1338;
   private static final int UNIFORM_SIZE = 256;
   private static ByteBuffer f1336;
   private static int f1341 = -1;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/fog_blur"))
         .withVertexShader(Identifier.method_60655("client", "core/fog_blur"))
         .withFragmentShader(Identifier.method_60655("client", "core/fog_blur"))
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("FogData", UniformType.field_60031)
         .withSampler("DepthTex")
         .withSampler("MinecraftTex")
         .withSampler("BlurTex")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static GpuBuffer f1334;
   private static SimpleFramebuffer f1339;
   private static final float NEAR = 0.05F;
   private static GpuTexture f1337;
   private static ms f1340;
   private static GpuBuffer f1335;

   private static void m1739(float near, float far, float distance, float saturation, boolean clientColor, int color1, int color2, int color3, int color4) {
      f1336.clear();
      f1336.putFloat(near).putFloat(far).putFloat(distance).putFloat(saturation);
      ByteBuffer result = f1336.putFloat(clientColor ? 1.0F : 0.0F).putFloat(0.0F);

      result.putFloat(0.0F).putFloat(0.0F);
      m1740(f1336, color1);
      m1740(f1336, color2);
      m1740(f1336, color3);
      m1740(f1336, color4);

      while (f1336.position() < 256) {
         f1336.put((byte)0);
      }

      f1336.flip();
   }

   private yr() {
   }

   public static boolean draw(float distance, float saturation, boolean clientColor, int color1, int color2, int color3, int color4) {
      if (!m1737()) {
         return false;
      } else {
         MinecraftClient v7 = MinecraftClient.method_1551();
         if (v7 != null && v7.method_1522() != null && v7.method_1522().method_30277() != null) {
            Framebuffer v8 = v7.method_1522();
            int v9 = v8.field_1482;
            int v10 = v8.field_1481;
            m1738(v9, v10);
            if (f1337 != null && f1339 != null && v8.method_30278() != null && f1339.method_30278() != null) {
               CommandEncoder v11 = RenderSystem.getDevice().createCommandEncoder();
               v11.copyTextureToTexture(v8.method_30277(), f1337, 0, 0, 0, 0, 0, v9, v10);
               GpuTexture temp1 = v8.method_30278();
               GpuTexture temp2 = f1339.method_30278();

               v11.copyTextureToTexture(temp1, temp2, 0, 0, 0, 0, 0, v9, v10);
               GpuTextureView v12 = f1339.method_71640();
               if (v12 == null) {
                  return false;
               } else {
                  GpuTextureView v13 = f1340.blur(f1337, f1338, v9, v10, 3, 2.5F);
                  if (v13 == null) {
                     v13 = f1338;
                  }

                  float v14 = (float)((Integer)v7.field_1690.method_42503().method_41753()).intValue() * 16.0F;
                  m1739(0.05F, v14, distance, saturation, clientColor, color1, color2, color3, color4);
                  GpuBufferSlice v15 = f1335.slice(0L, 2861148843085045783L + -2861148843085045527L);
                  v11.writeToBuffer(v15, f1336);

                  try {
                     RenderPass v16 = v11.createRenderPass(() -> {
                        String result = "client:fog_blur";
                        int v0 = 1159607283;
                        int var10001x = v0 * (v0 + 1) % 2;
                        int v1 = 1854686219;

                        if (var10001x != 0) {
                           }

                        return result;
                     }, v8.method_71639(), OptionalInt.empty());

                     boolean v17;
                     try {
                        v16.setPipeline(PIPELINE);
                        v16.setVertexBuffer(0, f1334);
                        v16.bindTexture("DepthTex", v12, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                        v16.bindTexture(
                           "MinecraftTex", f1338, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR)
                        );
                        v16.bindTexture("BlurTex", v13, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                        v16.setUniform("FogData", v15);
                        v16.draw(0, 6);
                        v17 = true;
                     } catch (Throwable v21) {
                        if (v16 != null) {
                           try {
                              v16.close();
                           } catch (Throwable v20) {
                              v21.addSuppressed(v20);
                           }
                        }

                        throw v21;
                     }

                     if (v16 != null) {
                        v16.close();
                     }

                     return v17;
                  } catch (Exception v22) {
                     return false;
                  }
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static void m1738(int width, int height) {
      if (f1337 != null && width == f1341 && height == f1342) {
         
      } else {
         if (f1338 != null) {
            f1338.close();
            f1338 = null;
         }

         if (f1337 != null) {
            f1337.close();
            f1337 = null;
         }

         f1339 = null;
         f1337 = RenderSystem.getDevice().createTexture(() -> {

            int v1 = -1096888298;
            int v3x = v1 * (v1 + 1) % 2;

            if (v3x != 0) {
               }

            return "client:fog_blur_color";
         }, 5, TextureFormat.RGBA8, width, height, 1, 1);
         GpuDevice result = RenderSystem.getDevice();

         f1338 = result.createTextureView(f1337);
         f1339 = new SimpleFramebuffer("client_fog_blur_depth", width, height, true);
         f1341 = width;
         f1342 = height;
      }
   }

   private static boolean m1737() {
      if (f1334 != null && f1335 != null && f1336 != null) {
         if (f1340 == null) {
            f1340 = new ms();
         }

         return true;
      } else {
         try {
            ByteBuffer v0 = MemoryUtil.memAlloc(4);
            v0.putInt(0).flip();
            GpuDevice result = RenderSystem.getDevice();
            int v1 = -1421632666;

            f1334 = result.createBuffer(() -> "client:fog_blur_dummy_vertex", 32, v0);
            MemoryUtil.memFree(v0);
            f1335 = RenderSystem.getDevice().createBuffer(() -> {

               return "client:fog_blur_uniform";
            }, 136, -361237987676771225L + 361237987676771481L);
            f1336 = MemoryUtil.memAlloc(256);
            f1340 = new ms();
            return true;
         } catch (Exception v3) {
            return false;
         }
      }
   }

   private static void m1740(ByteBuffer buf, int argb) {
      buf.putFloat((float)(argb >> 16 & 255) / 255.0F);

      buf.putFloat((float)(argb >> 8 & 255) / 255.0F);
      buf.putFloat((float)(argb & 255) / 255.0F);
      buf.putFloat((float)(argb >>> 24 & 255) / 255.0F);
   }

   
}
