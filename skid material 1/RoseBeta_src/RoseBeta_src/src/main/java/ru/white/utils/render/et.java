package ru.white.utils.render;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.math.MathHelper;
import ru.white.auth.StrDec;
import ru.white.theme.wo;

@Environment(EnvType.CLIENT)
public class et {
   private static boolean f1544 = false;
   private static GpuTextureView f1541 = null;
   private static boolean f1545 = true;
   private static int f1543 = 0;
   private static final ms pipeline = new ms();
   private static int f1542 = 0;

   private static void m1871(int iterations, float offset) {
      MinecraftClient v2 = MinecraftClient.method_1551();
      if (v2 != null) {
         Framebuffer v3 = v2.method_1522();
         if (v3 != null) {
            wr.flushPending();

            try {
               GpuTexture v4 = v3.method_30277();
               GpuTextureView v5 = v3.method_71639();
               if (v4 != null) {

                  if (v5 != null) {
                     int v6 = v3.field_1482;
                     int v7 = v3.field_1481;
                     if (v6 <= 0 || v7 <= 0) {
                        f1541 = null;
                        return;
                     }

                     f1541 = pipeline.blur(v4, v5, v6, v7, iterations, offset);
                     if (f1541 != null) {
                        f1542 = v6;
                        f1543 = v7;
                     }

                     return;
                  }
               }

               f1541 = null;
            } catch (Exception v9) {
               f1541 = null;
            }
         }
      }
   }

   public static boolean isReady() {
      return f1541 != null;
   }

   public static void frame() {
      boolean v0 = f1544 || f1545;
      f1545 = f1544;
      f1544 = false;
      if (!v0) {
         f1541 = null;
      } else {
         int v1 = wo.getBlur();
         int result = Math.round((float)v1 / 6.5F);

         int v2 = MathHelper.method_15340(result, 1, 5);
         float v3 = MathHelper.method_15363((float)v1 / 7.5F, 0.8F, 4.0F);
         m1871(v2, v3);
      }
   }

   public static 

      return f1543;
   }

   public static void capture(int size) {
      f1545 = true;
      m1871(size, (float)size);
      
   }

   public static void capture() {
      f1545 = true;
      int v0 = wo.getBlur();
      float result = (float)v0;

      int v1 = MathHelper.method_15340(Math.round(result / 6.5F), 1, 5);
      result = (float)v0;

      float v2 = MathHelper.method_15363(result / 7.5F, 0.8F, 4.0F);
      m1871(v1, v2);
   }

   public static void markUsed() {
      int v0 = -1522455423;
      int temp2 = v0 + 1;
      int v1 = 1417727567;

      f1544 = true;
   }

   public static GpuTextureView getBlurredView() {
      return f1541;
   }

   public static void close() {
      int v0 = -989579805;

      f1541 = null;
      pipeline.close();
   }

   public static void invalidate() {
      f1541 = null;
   }

   public static int getCachedWidth() {
      int v0 = 1923961726;
      int temp2 = v0 + 1;

      return f1542;
   }

   
}
