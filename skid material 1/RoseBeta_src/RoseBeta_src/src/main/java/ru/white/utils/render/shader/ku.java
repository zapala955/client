package ru.white.utils.render.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.awt.Color;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class ku {
   private GpuTexture f1662;
   private int f1665;
   private float f1667;
   private int f1664;
   private static final int MAX_SCANS = 12;
   private Color f1672;
   private GpuTextureView f1663;
   private final ju f1661;
   private Color f1669;
   private Color f1670;
   private Color f1671;
   private static ku instance;
   private float duration;
   private final MinecraftClient f1660 = MinecraftClient.method_1551();
   private final List<ku.lu> f1666;
   private float width;
   private float f1668;

   private void m1984(int width, int height) {
      if (this.f1662 != null && width == this.f1664 && height == this.f1665) {
         int v3 = 727356551;
         int result = v3 * (v3 + 1) % 2;

         if (result != 0) {
            }
      } else {
         this.m1985();
         this.f1662 = RenderSystem.getDevice()
            .createTexture(() -> "client:scanworld_depth_copy", 5, TextureFormat.DEPTH32, width, height, 1, 1);
         this.f1663 = RenderSystem.getDevice().createTextureView(this.f1662);
         this.f1664 = width;
         this.f1665 = height;
      }
   }

   public void setDuration(float duration) {

      this.duration = duration;
   }

   public void setOuterColor(Color color) {

      this.f1669 = color;
   }

   public void stopScan() {
      this.f1666.clear();
   }

   public void close() {
      this.m1985();
      this.f1661.close();

      this.f1666.clear();
   }

   public void startScan(Vec3d center) {
      if (this.f1666.size() >= 12) {

         this.f1666.remove(0);
      }

      this.f1666.add(new ku.lu(center, System.currentTimeMillis()));
   }

   private void m1985() {
      if (this.f1663 != null) {
         this.f1663.close();
         int v1 = -160020982;
         int temp2 = v1 * (v1 + 1);

         this.f1663 = null;
      }

      if (this.f1662 != null) {
         this.f1662.close();
         this.f1662 = null;
      }
   }

   public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
      if (!this.f1666.isEmpty()) {
         long v3 = System.currentTimeMillis();
         float v5 = Math.max(this.duration, 0.001F);
         List result = this.f1666;
         Predicate temp1 = s -> {
            float var10000x = (float)(v3 - s.startTime());

            return var10000x / 1000.0F > v5;
         };

         result.removeIf(temp1);
         if (!this.f1666.isEmpty()) {
            Framebuffer v6 = this.f1660.method_1522();
            if (v6 != null && v6.method_30278() != null && v6.method_71639() != null) {
               int v7 = v6.field_1482;
               int v8 = v6.field_1481;
               this.m1984(v7, v8);
               CommandEncoder v9 = RenderSystem.getDevice().createCommandEncoder();
               GpuTexture v27 = v6.method_30278();

               v9.copyTextureToTexture(v27, this.f1662, 0, 0, 0, 0, 0, v7, v8);
               Matrix4f v10 = new Matrix4f(viewMatrix).invert();
               Matrix4f v11 = new Matrix4f(projectionMatrix).invert();
               Vec3d v12 = this.f1660.field_1773.method_19418().method_71156();

               float v13 = 1.0F - Math.max(0.01F, Math.min(this.f1668, 0.95F));

               for (ku.lu v15 : this.f1666) {
                  float v16 = (float)(v3 - v15.startTime()) / 1000.0F;
                  float v17 = v16 / v5;
                  float v18 = v17 * this.f1667;
                  float v19 = 1.0F;
                  if (v17 >= v13) {
                     float v20 = (v17 - v13) / (1.0F - v13);
                     v20 = Math.max(0.0F, Math.min(v20, 1.0F));
                     v19 = 1.0F - v20;
                     v19 *= v19;
                  }

                  this.f1661
                     .render(
                        v6.method_71639(),
                        this.f1663,
                        v7,
                        v8,
                        v12,
                        v15.center(),
                        v10,
                        v11,
                        v18,
                        this.width,
                        v19,
                        this.f1669,
                        this.f1670,
                        this.f1671,
                        this.f1672
                     );
               }
            }
         }
      }
   }

   public void setWidth(float width) {

      this.width = width;
      
   }

   public void setInnerColor(Color color) {

      this.f1671 = color;
   }

   public static ku getInstance() {
      int v0 = -344962412;
      int v1 = -1428525172;

      if (instance == null) {
         instance = new ku();
      }

      return instance;
   }

   public void setScanlineColor(Color color) {
      this.f1672 = color;
      
   }

   public void setFadeOutPortion(float fadeOutPortion) {

      this.f1668 = fadeOutPortion;
   }

   private ku() {
      this.f1661 = new ju();
      this.f1666 = new CopyOnWriteArrayList<>();
      this.duration = 2.5F;
      this.width = 10.0F;
      this.f1667 = 80.0F;
      this.f1668 = 0.25F;
      this.f1669 = new Color(0, 0, 0, 255);
      this.f1670 = new Color(0, 0, 0, 255);
      this.f1671 = new Color(80, 220, 255, 180);
      this.f1672 = new Color(120, 255, 255, 120);
   }

   public void setMaxRadius(float maxRadius) {
      this.f1667 = maxRadius;
   }

   public void setMidColor(Color color) {

      this.f1670 = color;
   }

   @Environment(EnvType.CLIENT)
   private static record lu(Vec3d center, long startTime) {
      private final Vec3d f1658;
      private final long f1659;

      public long startTime() {

         return this.f1659;
      }

      public Vec3d center() {
         int v1 = 137861899;

         int temp2 = v1 + 1;

         return this.f1658;
      }

      private lu(Vec3d center, long startTime) {
         this.f1658 = center;
         this.f1659 = startTime;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",ku.lu,"center;startTime",ku.lu::f1658,ku.lu::f1659>(this);

         return result;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",ku.lu,"center;startTime",ku.lu::f1658,ku.lu::f1659>(this);
         int v1 = -1315970909;
         int temp2 = v1 + 1;

         return result;
      }

      
   }
}
