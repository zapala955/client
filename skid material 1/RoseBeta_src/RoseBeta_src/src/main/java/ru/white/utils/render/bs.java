package ru.white.utils.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class bs {
   private int f1380;
   private boolean f1374;
   private GpuTexture f1366;
   private int f1372;
   private GpuTexture maskTexture;
   private static bs instance;
   private GpuTextureView f1363;
   private float f1381;
   private int f1384;
   private ms f1358;
   private int f1385;
   private boolean f1379;
   private boolean f1373;
   private float f1378;
   private boolean f1382;
   private int f1387;
   private GpuTextureView f1370;
   private int f1371 = 0;
   private os f1360;
   private cs f1359;
   private GpuTexture f1364;
   private int f1386;
   private boolean f1375;
   private GpuTextureView f1369;
   private GpuTexture f1368;
   private float f1376;
   private es f1361;
   private boolean enabled;
   private final MinecraftClient client;
   private float f1383;
   private GpuTextureView f1365;
   private int f1377;
   private GpuTexture f1362;
   private GpuTextureView f1367;

   public void setOutlineColor(int color) {
      this.f1384 = color;

      this.f1385 = color;
   }

   public void setEnabled(boolean enabled) {

      this.enabled = enabled;
      int v3 = -1480025295;

      if (enabled) {
         this.m1752();
      }
   }

   public bs() {
      this.f1372 = 0;
      this.f1373 = false;
      this.enabled = false;
      this.f1374 = false;
      this.f1375 = true;
      this.f1376 = 2.5F;
      this.f1377 = 3;
      this.f1378 = 0.0F;
      this.f1379 = true;
      this.f1380 = 0;
      this.f1381 = 0.0F;
      this.f1382 = true;
      this.f1383 = 4.0F;
      this.f1384 = -1;
      this.f1385 = -1;
      this.f1386 = 1;
      this.f1387 = 2;
      this.client = MinecraftClient.method_1551();
      instance = this;
   }

   public void setTintColor(int color) {
      int v2 = 2105234459;

      this.f1380 = color;
   }

   private void m1754() {
      if (this.f1363 != null) {
         this.f1363.close();
         this.f1363 = null;
      }

      if (this.f1362 != null) {
         this.f1362.close();
         this.f1362 = null;
      }

      if (this.f1365 != null) {
         this.f1365.close();
         this.f1365 = null;
      }

      if (this.f1364 != null) {
         this.f1364.close();
         this.f1364 = null;
      }

      if (this.f1367 != null) {
         this.f1367.close();
         this.f1367 = null;
      }

      if (this.f1366 != null) {
         this.f1366.close();
         this.f1366 = null;
      }

      if (this.f1369 != null) {
         this.f1369.close();
         this.f1369 = null;
      }

      if (this.f1368 != null) {

         this.f1368.close();
         this.f1368 = null;
      }

      if (this.f1370 != null) {
         this.f1370.close();
         this.f1370 = null;
      }

      if (this.maskTexture != null) {
         this.maskTexture.close();
         this.maskTexture = null;
      }
   }

   public void setShimmerPeriodSec(float sec) {
      if (this.f1361 != null) {
         int v2 = -1667395776;
         int temp2 = v2 + 1;

         this.f1361.setShimmerPeriodSec(sec);
         
      }
   }

   public void setBlurIterations(int iterations) {
      int temp2 = 8;

      this.f1377 = Math.max(1, Math.min(temp2, iterations));
   }

   public void setSaturation(float saturation) {
      this.f1378 = saturation;
      
   }

   public void renderGlassEffect() {
      if (this.enabled && this.f1373) {
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null) {
            this.f1360.createMask(this.f1370, this.f1363, this.f1365, this.f1367, this.f1369, this.f1371, this.f1372);
            GpuTextureView v2 = this.f1363;
            if (this.f1375) {
               v2 = this.f1358.blur(this.f1362, this.f1363, this.f1371, this.f1372, this.f1377, this.f1376);
               if (v2 == null) {
                  this.f1373 = false;
                  return;
               }
            }

            cs result = this.f1359;
            GpuTextureView temp1 = v1.method_71639();

            result.composite(
               temp1,
               this.f1363,
               v2,
               this.f1370,
               this.f1371,
               this.f1372,
               this.f1378,
               this.f1379,
               this.f1380,
               this.f1381,
               0.0F,
               12.0F,
               0.0F,
               18.0F,
               0.0F,
               0.0F,
               6.0F,
               0.25F,
               40.0F
            );
            if (this.f1382) {
               this.f1361.renderOutline(this.f1370, v1.method_71639(), this.f1371, this.f1372, this.f1383, this.f1386, this.f1387, this.f1385, this.f1384);
            }

            this.f1373 = false;
         } else {
            this.f1373 = false;
         }
      }
   }

   private void m1752() {
      if (!this.f1374) {
         if (this.f1358 != null) {

            this.f1358.close();
         }

         if (this.f1359 != null) {
            this.f1359.close();
         }

         if (this.f1360 != null) {
            this.f1360.close();
            
         }

         if (this.f1361 != null) {
            this.f1361.close();
         }

         this.f1358 = new ms();
         this.f1359 = new cs();

         this.f1360 = new os();
         this.f1361 = new es();
         this.f1371 = 0;
         this.f1372 = 0;
         this.f1374 = true;
      }
   }

   private void m1753(int width, int height) {
      if (width != this.f1371 || height != this.f1372 || this.f1362 == null) {
         this.m1754();
         this.f1362 = RenderSystem.getDevice().createTexture(() -> {

            return "minecraft:glass_block_scene_before";
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1363 = RenderSystem.getDevice().createTextureView(this.f1362);
         this.f1364 = RenderSystem.getDevice().createTexture(() -> {
            int var10001x = -1402609119;
            int v0 = -1795174609;
            int var10002x = v0 * (v0 + 1);

            return StrDec.d("74tWc1Tl1OXcMkVgvM6s5jRaI0Go/LI0GTITh4ivnygY", var10001x);
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1365 = RenderSystem.getDevice().createTextureView(this.f1364);
         GpuDevice temp1 = RenderSystem.getDevice();
         Supplier temp2 = () -> "minecraft:glass_block_depth_before";
         int temp3 = 13;

         this.f1366 = temp1.createTexture(temp2, temp3, TextureFormat.DEPTH32, width, height, 1, 1);
         this.f1367 = RenderSystem.getDevice().createTextureView(this.f1366);
         temp1 = RenderSystem.getDevice();

         this.f1368 = temp1.createTexture(() -> {

            return "minecraft:glass_block_depth_after";
         }, 13, TextureFormat.DEPTH32, width, height, 1, 1);
         this.f1369 = RenderSystem.getDevice().createTextureView(this.f1368);
         this.maskTexture = RenderSystem.getDevice().createTexture(() -> {
            String result = "minecraft:glass_block_mask";

            return result;
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1370 = RenderSystem.getDevice().createTextureView(this.maskTexture);
         this.f1371 = width;
         this.f1372 = height;
      }
   }

   public void setTintIntensity(float intensity) {
      this.f1381 = intensity;
   }

   public void setBlurEnabled(boolean enabled) {
      this.f1375 = enabled;
   }

   public void setShimmerWidth(float width) {
      int v2 = 1946368625;
      int temp1 = v2 * (v2 + 1) % 2;
      int v3 = 1707939108;

      if (temp1 != 0) {
         }

      if (this.f1361 != null) {
         this.f1361.setShimmerWidth(width);
      }
   }

   public void setBlurRadius(float radius) {

      this.f1376 = radius;
   }

   public void setOutlineEnabled(boolean enabled) {
      this.f1382 = enabled;
   }

   public void setOutlineWidth(int width) {
      this.f1386 = width;
   }

   public void setShimmerEnabled(boolean enabled) {
      int v2 = 1436531683;

      if (this.f1361 != null) {

         this.f1361.setShimmerEnabled(enabled);
      }
   }

   public void captureSceneAfterBlock() {
      if (this.enabled && this.f1373) {
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null) {
            CommandEncoder v2 = RenderSystem.getDevice().createCommandEncoder();
            GpuTexture temp1 = v1.method_30277();

            v2.copyTextureToTexture(temp1, this.f1364, 0, 0, 0, 0, 0, this.f1371, this.f1372);
            if (v1.method_30278() != null) {
               v2.copyTextureToTexture(v1.method_30278(), this.f1368, 0, 0, 0, 0, 0, this.f1371, this.f1372);
            }
         }
      }
   }

   public void setReflect(boolean reflect) {
      this.f1379 = reflect;
   }

   public static bs getInstance() {
      if (instance == null) {
         bs result = new bs();
         int v0 = -617227023;
         int temp2 = v0 + 1;

         instance = result;
      }

      return instance;
   }

   public boolean captureSceneBeforeBlock() {
      if (!this.enabled) {
         return false;
      } else {
         this.m1752();

         Framebuffer result = this.client.method_1522();

         Framebuffer v1 = result;

         if (v1 != null && v1.method_30277() != null) {
            int v2 = v1.field_1482;
            int v3 = v1.field_1481;
            this.m1753(v2, v3);
            CommandEncoder v4 = RenderSystem.getDevice().createCommandEncoder();
            v4.copyTextureToTexture(v1.method_30277(), this.f1362, 0, 0, 0, 0, 0, v2, v3);
            if (v1.method_30278() != null) {
               v4.copyTextureToTexture(v1.method_30278(), this.f1366, 0, 0, 0, 0, 0, v2, v3);
            }

            this.f1373 = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public void setOutlineGlowMode(int mode) {
      this.f1387 = mode;
      
   }

   public void close() {
      this.m1754();
      if (this.f1358 != null) {
         this.f1358.close();
         this.f1358 = null;
      }

      if (this.f1359 != null) {
         this.f1359.close();
         this.f1359 = null;
      }

      if (this.f1360 != null) {
         this.f1360.close();
         this.f1360 = null;
      }

      if (this.f1361 != null) {
         this.f1361.close();
         this.f1361 = null;
      }

      this.f1371 = 0;
      this.f1372 = 0;
      this.f1374 = false;
      this.f1373 = false;
   }

   public void setOutlineGlowStrength(float strength) {
      this.f1383 = strength;
      
   }

   public void invalidate() {
      this.m1754();
      if (this.f1358 != null) {
         this.f1358.close();
      }

      if (this.f1359 != null) {
         this.f1359.close();
      }

      if (this.f1360 != null) {
         this.f1360.close();
      }

      if (this.f1361 != null) {
         this.f1361.close();
      }

      this.f1358 = null;
      this.f1359 = null;
      this.f1360 = null;
      this.f1361 = null;
      this.f1371 = 0;
      this.f1372 = 0;

      this.f1374 = false;
      this.f1373 = false;
      
   }

   
}
