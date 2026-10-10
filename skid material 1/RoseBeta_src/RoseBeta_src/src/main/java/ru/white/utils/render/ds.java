package ru.white.utils.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class ds {
   private GpuTexture f1406;
   private GpuTexture f1403;
   private final GpuTextureView[] f1409;
   private final MinecraftClient client;
   private float f1439;
   private boolean f1433;
   private GpuTextureView f1405;
   private int f1445;
   private float f1438;
   private float f1423;
   private float f1425;
   private float f1430;
   private float f1444;
   private es f1395;
   private int f1412;
   private cs f1393;
   private boolean f1443;
   private GpuTexture f1397;
   private os f1394;
   private static ds instance;
   private float f1420;
   private boolean f1442;
   private float f1424;
   private float f1431;
   private int f1422;
   private GpuTextureView f1402;
   private float f1426;
   private GpuTexture f1399;
   private int f1447;
   private rr f1396;
   private GpuTextureView f1404;
   private boolean f1414;
   private ms f1392;
   private float f1429;
   private int f1410;
   private float f1441;
   private float f1437;
   private float f1436;
   private boolean f1416;
   private float f1427;
   private int f1440;
   private float f1435;
   private boolean enabled;
   private int f1446;
   private final GpuTexture[] f1408 = new GpuTexture[2];
   private GpuTextureView f1407;
   private int f1411;
   private boolean f1417;
   private boolean f1449;
   private int f1448;
   private GpuTexture f1401;
   private GpuTextureView f1400;
   private boolean f1413;
   private float f1418;
   private float f1428;
   private int f1419;
   private boolean f1415;
   private boolean f1421;
   private float f1432;
   private GpuTexture maskTexture;
   private float f1434;
   private GpuTextureView f1398;

   public void captureSceneAfterHands() {
      if (this.enabled && this.f1413) {
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null) {
            CommandEncoder v2 = RenderSystem.getDevice().createCommandEncoder();
            GpuTexture temp1 = v1.method_30277();

            v2.copyTextureToTexture(temp1, this.f1399, 0, 0, 0, 0, 0, this.f1411, this.f1412);
            if (v1.method_30278() != null) {
               v2.copyTextureToTexture(v1.method_30278(), this.f1403, 0, 0, 0, 0, 0, this.f1411, this.f1412);
            }
         }
      }
   }

   public void setSmokeAmount(float amount) {
      int v2 = 1865415455;
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      this.f1429 = amount;
   }

   public void setOutlineFillColor(int color) {
      this.f1446 = color;
   }

   public void setFireFillMode(boolean fill) {

      this.f1442 = fill;
   }

   public void setTintIntensity(float intensity) {

      this.f1423 = intensity;
   }

   public void setSaturation(float saturation) {
      this.f1420 = saturation;
      
   }

   public void setFireStrength(float strength) {

      this.f1435 = strength;
   }

   public void setFireSpeed(float speed) {

      this.f1436 = speed;
      
   }

   public void setBlurIterations(int iterations) {
      int v2 = -1404662568;

      int v5 = v2 * (v2 + 1);

      this.f1419 = Math.max(1, Math.min(8, iterations));
   }

   public void setEnabled(boolean enabled) {
      int v2 = 977390997;
      int temp1 = v2 * (v2 + 1);

      this.enabled = enabled;
      if (enabled) {
         this.m1765();
      }
   }

   public void invalidate() {
      this.m1767();
      if (this.f1392 != null) {
         this.f1392.close();
      }

      if (this.f1393 != null) {
         this.f1393.close();
      }

      if (this.f1394 != null) {
         this.f1394.close();
      }

      if (this.f1395 != null) {
         this.f1395.close();
      }

      if (this.f1396 != null) {
         this.f1396.close();
      }

      this.f1392 = null;
      this.f1393 = null;
      this.f1394 = null;
      this.f1395 = null;
      this.f1396 = null;
      this.f1411 = 0;
      this.f1412 = 0;
      this.f1416 = false;
      this.f1413 = false;
   }

   public void setOutlineGlowStrength(float strength) {
      int v2 = 1190748799;

      this.f1444 = strength;
   }

   public void setReflect(boolean reflect) {

      this.f1421 = reflect;
   }

   public void setOutlineColor(int color) {
      this.f1445 = color;

      this.f1446 = color;
   }

   public void setIceIntensity(float intensity) {
      int v2 = -628396987;

      this.f1426 = intensity;
   }

   public void setShimmerPeriodSec(float sec) {

      if (this.f1395 != null) {
         this.f1395.setShimmerPeriodSec(sec);
      }
   }

   public void setEdgeSoftness(float px) {

      this.f1425 = Math.max(0.0F, px);
   }

   public void setSmokeScale(float scale) {

      this.f1430 = scale;
   }

   public static void resetInstance() {
      if (instance != null) {
         instance.close();
         instance.f1416 = false;
      }
   }

   public void setOutlineUseItemColor(boolean use) {
      this.f1449 = use;
   }

   public void setCrackIntensity(float intensity) {
      int v3 = -977443952;

      this.f1428 = intensity;
   }

   public void close() {
      this.m1767();
      if (this.f1392 != null) {
         this.f1392.close();
         this.f1392 = null;
      }

      if (this.f1393 != null) {
         this.f1393.close();
         this.f1393 = null;
      }

      if (this.f1394 != null) {
         this.f1394.close();
         this.f1394 = null;
      }

      if (this.f1395 != null) {
         this.f1395.close();
         this.f1395 = null;
      }

      if (this.f1396 != null) {
         this.f1396.close();
         this.f1396 = null;
      }

      this.f1411 = 0;
      this.f1412 = 0;

      this.f1416 = false;
   }

   public void setOutlineEnabled(boolean enabled) {
      this.f1443 = enabled;
   }

   public void setFireEnabled(boolean enabled) {

      this.f1433 = enabled;
   }

   public void setFireRadius(float radius) {
      this.f1434 = radius;
   }

   public void setBlurEnabled(boolean enabled) {
      this.f1417 = enabled;
   }

   public void setFrostScale(float scale) {
      this.f1427 = scale;
   }

   public void setOutlineWidth(int width) {

      this.f1447 = width;
   }

   public void setEdgeGlowIntensity(float intensity) {
      this.f1424 = intensity;
   }

   public void setFireHeight(float height) {

      this.f1437 = height;
   }

   private void m1766(int width, int height) {
      if (width != this.f1411 || height != this.f1412 || this.f1397 == null) {
         this.m1767();
         this.f1397 = RenderSystem.getDevice().createTexture(() -> {
            int var10001x = -1358813409;
            int v0 = -212759250;

            return StrDec.d("0U4xcGogs+bi9yJjggvL5RuQTk+YOcQxJPZjvg==", var10001x);
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1398 = RenderSystem.getDevice().createTextureView(this.f1397);
         this.f1399 = RenderSystem.getDevice()
            .createTexture(
               () -> "minecraft:glass_scene_after",
               13,
               TextureFormat.RGBA8,
               width,
               height,
               1,
               1
            );
         this.f1400 = RenderSystem.getDevice().createTextureView(this.f1399);
         this.f1401 = RenderSystem.getDevice()
            .createTexture(
               () -> "minecraft:glass_depth_before",
               13,
               TextureFormat.DEPTH32,
               width,
               height,
               1,
               1
            );
         this.f1402 = RenderSystem.getDevice().createTextureView(this.f1401);
         this.f1403 = RenderSystem.getDevice()
            .createTexture(
               () -> "minecraft:glass_depth_after",
               13,
               TextureFormat.DEPTH32,
               width,
               height,
               1,
               1
            );
         this.f1404 = RenderSystem.getDevice().createTextureView(this.f1403);
         this.maskTexture = RenderSystem.getDevice().createTexture(() -> {
            String result = "minecraft:glass_mask";

            return result;
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1405 = RenderSystem.getDevice().createTextureView(this.maskTexture);
         this.f1406 = RenderSystem.getDevice().createTexture(() -> {

            return "minecraft:burn_glow";
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         this.f1407 = RenderSystem.getDevice().createTextureView(this.f1406);

         for (int v3 = 0; v3 < 2; v3++) {
            int v4 = v3;
            this.f1408[v3] = RenderSystem.getDevice().createTexture(() -> {

               StringBuilder v4x = new StringBuilder();

               return v4x.append("minecraft:burn_trail_").append(v4).toString();
            }, 13, TextureFormat.RGBA8, width, height, 1, 1);

            this.f1409[v3] = RenderSystem.getDevice().createTextureView(this.f1408[v3]);
            CommandEncoder v5 = RenderSystem.getDevice().createCommandEncoder();
            RenderPass v6 = v5.createRenderPass(() -> {

               int v0 = 1224540402;

               return "minecraft:burn_trail_clear";
            }, this.f1409[v3], OptionalInt.of(0));
            if (v6 != null) {
               v6.close();
            }
         }

         this.f1410 = 0;
         this.f1411 = width;
         this.f1412 = height;
      }
   }

   public boolean isCapturing() {

      return this.f1413;
   }

   public void setBlurRadius(float radius) {

      this.f1418 = radius;
   }

   public void setFireTrailStrength(float strength) {
      this.f1439 = strength;
   }

   public boolean isEnabled() {

      return this.enabled;
   }

   public void restoreHandsAfterOverlay() {
      if (this.enabled && this.f1414 && this.f1397 != null && this.f1400 != null && this.f1405 != null && this.f1393 != null) {
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null && v1.method_71639() != null) {
            wr.flushPending();
            CommandEncoder v2 = RenderSystem.getDevice().createCommandEncoder();
            v2.copyTextureToTexture(v1.method_30277(), this.f1397, 0, 0, 0, 0, 0, this.f1411, this.f1412);
            cs result = this.f1393;
            GpuTextureView temp1 = v1.method_71639();

            result.composite(
               temp1,
               this.f1398,
               this.f1400,
               this.f1405,
               this.f1411,
               this.f1412,
               1.0F,
               false,
               0,
               0.0F,
               0.0F,
               this.f1425,
               0.0F,
               this.f1427,
               0.0F,
               0.0F,
               this.f1430,
               this.f1431,
               this.f1432
            );
            if (this.f1443 && this.f1395 != null) {
               es v6 = this.f1395;
               temp1 = this.f1405;
               GpuTextureView temp2 = v1.method_71639();

               v6.renderOutline(
                  temp1, temp2, this.f1411, this.f1412, this.f1444, this.f1447, this.f1448, this.f1445, this.f1446, this.f1400, this.f1449
               );
            }

            this.f1414 = false;
         } else {
            this.f1414 = false;
         }
      }
   }

   public void renderGlassEffect() {
      if (this.enabled && this.f1413) {
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null) {
            this.f1394.createMask(this.f1405, this.f1398, this.f1400, this.f1402, this.f1404, this.f1411, this.f1412);
            this.f1414 = true;
            boolean v2 = this.f1426 > 0.001F;
            boolean v3 = this.f1429 > 0.001F;
            if (this.f1417 || v2 || v3) {
               GpuTextureView v4;
               if (this.f1417) {

                  v4 = this.f1392.blur(this.f1397, this.f1398, this.f1411, this.f1412, this.f1419, this.f1418);
               } else {
                  v4 = this.f1398;
               }

               if (v4 == null) {
                  this.f1413 = false;
                  return;
               }

               float v5 = this.f1417 ? this.f1420 : 1.0F;
               int v6 = this.f1417 ? this.f1422 : 0;
               float v7 = this.f1417 ? this.f1423 : 0.0F;
               this.f1393
                  .composite(
                     v1.method_71639(),
                     this.f1398,
                     v4,
                     this.f1405,
                     this.f1411,
                     this.f1412,
                     v5,
                     this.f1421,
                     v6,
                     v7,
                     0.0F,
                     this.f1425,
                     this.f1426,
                     this.f1427,
                     this.f1428,
                     this.f1429,
                     this.f1430,
                     this.f1431,
                     this.f1432
                  );
            }

            if (this.f1443) {
               this.f1395
                  .renderOutline(
                     this.f1405,
                     v1.method_71639(),
                     this.f1411,
                     this.f1412,
                     this.f1444,
                     this.f1447,
                     this.f1448,
                     this.f1445,
                     this.f1446,
                     this.f1400,
                     this.f1449
                  );
            }

            if (this.f1433) {
               this.f1396
                  .updateUniforms(
                     this.f1411,
                     this.f1412,
                     this.f1442,
                     this.f1434,
                     this.f1435,
                     this.f1436,
                     this.f1441,
                     this.f1438,
                     this.f1437,
                     this.f1436,
                     this.f1439,
                     this.f1440,
                     this.f1435
                  );
               this.f1396.renderGlow(this.f1407, this.f1400, this.f1405);
               int v10 = this.f1410;
               int v11 = 1 - this.f1410;
               this.f1396.renderTrail(this.f1409[v11], this.f1409[v10], this.f1407, this.f1400, this.f1405);
               this.f1410 = v11;
               rr result = this.f1396;
               GpuTextureView temp1 = v1.method_71639();

               result.composite(temp1, this.f1405, this.f1407, this.f1409[v11]);
            }

            if (this.f1415 && this.f1399 != null) {
               RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(v1.method_30277(), this.f1399, 0, 0, 0, 0, 0, this.f1411, this.f1412);
            }

            this.f1413 = false;
         } else {
            this.f1413 = false;
         }
      }
   }

   public void setKeepHandsResult(boolean keep) {

      this.f1415 = keep;
   }

   public void setFireColor(int rgb) {

      this.f1440 = rgb;
   }

   public static ds getInstance() {

      if (instance == null) {
         instance = new ds();
      }

      return instance;
   }

   public ds() {
      this.f1409 = new GpuTextureView[2];
      this.f1410 = 0;
      this.f1411 = 0;
      this.f1412 = 0;
      this.f1413 = false;
      this.f1414 = false;
      this.f1415 = false;
      this.enabled = false;
      this.f1416 = false;
      this.f1417 = true;
      this.f1418 = 6.0F;
      this.f1419 = 4;
      this.f1420 = 1.0F;
      this.f1421 = true;
      this.f1422 = 0;
      this.f1423 = 0.1F;
      this.f1424 = 0.3F;
      this.f1425 = 0.0F;
      this.f1426 = 0.0F;
      this.f1427 = 18.0F;
      this.f1428 = 0.5F;
      this.f1429 = 0.0F;
      this.f1430 = 6.0F;
      this.f1431 = 0.25F;
      this.f1432 = 40.0F;
      this.f1433 = false;
      this.f1434 = 15.0F;
      this.f1435 = 1.18F;
      this.f1436 = 1.1F;
      this.f1437 = 28.0F;
      this.f1438 = 0.94F;
      this.f1439 = 1.16F;
      this.f1440 = 16744486;
      this.f1441 = 0.0F;
      this.f1442 = false;
      this.f1443 = true;
      this.f1444 = 20.0F;
      this.f1445 = -1;
      this.f1446 = -1;
      this.f1447 = 1;
      this.f1448 = 2;
      this.f1449 = false;
      this.client = MinecraftClient.method_1551();
      instance = this;
   }

   public void setSmokeReach(float reach) {
      this.f1432 = reach;
   }

   public void setShimmerEnabled(boolean enabled) {

      if (this.f1395 != null) {

         this.f1395.setShimmerEnabled(enabled);
      }
   }

   public void setTintColor(int color) {

      this.f1422 = color;
   }

   private void m1765() {
      if (!this.f1416) {
         if (this.f1392 != null) {
            this.f1392.close();
         }

         if (this.f1393 != null) {
            this.f1393.close();
         }

         if (this.f1394 != null) {
            this.f1394.close();
         }

         if (this.f1395 != null) {
            this.f1395.close();
         }

         if (this.f1396 != null) {
            this.f1396.close();
         }

         this.f1392 = new ms();
         this.f1393 = new cs();
         this.f1394 = new os();
         this.f1395 = new es();
         this.f1396 = new rr();
         this.f1411 = 0;
         this.f1412 = 0;
         this.f1416 = true;
      }
   }

   public void setSmokeSpeed(float speed) {
      this.f1431 = speed;

      
   }

   public void captureSceneBeforeHands() {
      if (this.enabled) {
         this.m1765();
         Framebuffer v1 = this.client.method_1522();
         if (v1 != null && v1.method_30277() != null) {
            int v2 = v1.field_1482;
            int v3 = v1.field_1481;

            this.m1766(v2, v3);
            CommandEncoder v4 = RenderSystem.getDevice().createCommandEncoder();
            v4.copyTextureToTexture(v1.method_30277(), this.f1397, 0, 0, 0, 0, 0, v2, v3);
            if (v1.method_30278() != null) {
               v4.copyTextureToTexture(v1.method_30278(), this.f1401, 0, 0, 0, 0, 0, v2, v3);
            }

            this.f1413 = true;
         }
      }
   }

   public void setOutlineGlowMode(int mode) {
      this.f1448 = mode;
      int v2 = 573467319;

      
   }

   public void setFireColorMix(float mix) {
      this.f1441 = mix;
   }

   public void setShimmerWidth(float width) {
      if (this.f1395 != null) {

         this.f1395.setShimmerWidth(width);
      }
   }

   private void m1767() {
      if (this.f1398 != null) {
         this.f1398.close();
         this.f1398 = null;
      }

      if (this.f1397 != null) {
         this.f1397.close();
         this.f1397 = null;
      }

      if (this.f1400 != null) {
         this.f1400.close();
         this.f1400 = null;
      }

      if (this.f1399 != null) {
         this.f1399.close();
         this.f1399 = null;
      }

      if (this.f1402 != null) {
         this.f1402.close();
         this.f1402 = null;
      }

      if (this.f1401 != null) {
         this.f1401.close();
         this.f1401 = null;
      }

      if (this.f1404 != null) {
         this.f1404.close();
         this.f1404 = null;
      }

      if (this.f1403 != null) {
         this.f1403.close();
         this.f1403 = null;
      }

      if (this.f1405 != null) {
         this.f1405.close();
         this.f1405 = null;
      }

      if (this.maskTexture != null) {
         this.maskTexture.close();
         this.maskTexture = null;
      }

      if (this.f1407 != null) {
         this.f1407.close();
         this.f1407 = null;
      }

      if (this.f1406 != null) {
         this.f1406.close();

         this.f1406 = null;
      }

      for (int v1 = 0; v1 < 2; v1++) {
         if (this.f1409[v1] != null) {
            this.f1409[v1].close();
            this.f1409[v1] = null;
         }

         if (this.f1408[v1] != null) {
            this.f1408[v1].close();

            this.f1408[v1] = null;
         }
      }
   }

   
}
