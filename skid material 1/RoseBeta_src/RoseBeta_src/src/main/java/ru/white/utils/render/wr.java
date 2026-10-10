package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class wr {
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final ArrayList<wr.xr> active = new ArrayList<>(8);
   private static boolean enabled = false;

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   public static void flushPending() {
      if (!active.isEmpty()) {
         wr.xr[] v0 = active.toArray(new wr.xr[0]);
         active.clear();
         Arrays.sort(v0, Comparator.comparingInt(wr.xr::batchLayer));
         MinecraftClient v1 = MinecraftClient.method_1551();
         if (v1 != null && v1.method_1522() != null) {
            CommandEncoder v23 = RenderSystem.getDevice().createCommandEncoder();

            for (wr.xr v6 : v0) {
               v6.uploadBatch(v23);
            }

            GpuBufferSlice v25 = m1734();
            boolean v19 = false /* VF: Semaphore variable */;

            try {
               v19 = true;
               Supplier temp1 = () -> {
                  String result = "client:2d_batch";

                  return result;
               };
               GpuTextureView temp2 = v1.method_1522().method_71639();
               OptionalInt temp3 = OptionalInt.empty();
               Framebuffer temp4 = v1.method_1522();

               RenderPass v27 = v23.createRenderPass(temp1, temp2, temp3, temp4.method_71640(), OptionalDouble.empty());

               try {
                  RenderSystem.bindDefaultUniforms(v27);
                  v27.setUniform("DynamicTransforms", v25);

                  for (wr.xr v8 : v0) {

                     v8.drawBatch(v27);
                  }
               } catch (Throwable v21) {
                  if (v27 != null) {
                     try {
                        v27.close();
                     } catch (Throwable v20) {
                        v21.addSuppressed(v20);
                     }
                  }

                  throw v21;
               }

               if (v27 != null) {
                  v27.close();
                  v19 = false;
               } else {
                  v19 = false;
               }
            } finally {
               if (v19) {
                  for (wr.xr v13 : v0) {
                     v13.discardBatch();
                  }
               }
            }

            for (wr.xr v34 : v0) {
               v34.discardBatch();
            }
         } else {
            for (wr.xr v5 : v0) {
               v5.discardBatch();
            }
         }
      }
   }

   public static void setEnabled(boolean value) {

      if (!value) {
         flushPending();
      }

      enabled = value;
   }

   public static void register(wr.xr owner) {
      if (!active.contains(owner)) {
         active.add(owner);
      }
   }

   public static void drawImmediate(wr.xr owner, boolean upload) {
      MinecraftClient v2 = MinecraftClient.method_1551();
      if (v2 != null && v2.method_1522() != null) {
         CommandEncoder v3 = RenderSystem.getDevice().createCommandEncoder();
         if (upload) {
            owner.uploadBatch(v3);
         }

         GpuBufferSlice v4 = m1734();

         try {
            Supplier temp1 = () -> {
               int var10001x = -1213759627;

               return StrDec.d("tROTaA1+TaqYyol65EV5y2Pf6w==", var10001x);
            };
            GpuTextureView temp2 = v2.method_1522().method_71639();

            RenderPass v5 = v3.createRenderPass(temp1, temp2, OptionalInt.empty(), v2.method_1522().method_71640(), OptionalDouble.empty());

            try {
               RenderSystem.bindDefaultUniforms(v5);

               v5.setUniform("DynamicTransforms", v4);
               owner.drawBatch(v5);
            } catch (Throwable v16) {
               if (v5 != null) {
                  try {
                     v5.close();
                  } catch (Throwable v15) {
                     v16.addSuppressed(v15);
                  }
               }

               throw v16;
            }

            if (v5 != null) {
               v5.close();
            }
         } finally {
            owner.discardBatch();
         }

         
      } else {
         owner.discardBatch();
      }
   }

   public static boolean isEnabled() {
      return enabled;
   }

   private static GpuBufferSlice m1734() {
      DynamicUniforms result = RenderSystem.getDynamicUniforms();
      Matrix4f temp1 = RenderSystem.getModelViewMatrix();

      return result.method_71106(temp1, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
   }

   public static void drawImmediate(wr.xr owner) {
      drawImmediate(owner, true);

      
   }

   private wr() {
   }

   @Environment(EnvType.CLIENT)
   public interface xr {
      void discardBatch();

      void drawBatch(RenderPass v1);

      default 

         return 0;
      }

      void uploadBatch(CommandEncoder v1);
   }
}
