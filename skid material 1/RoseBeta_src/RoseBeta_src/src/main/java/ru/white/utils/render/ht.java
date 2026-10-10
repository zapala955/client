package ru.white.utils.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class ht {
   private static ht instance;
   private ft f1558;
   public final ft.gt params = new ft.gt();

   public void close() {

      this.invalidate();
   }

   public static ht getInstance() {
      if (instance == null) {
         instance = new ht();
      }

      return instance;
   }

   private void m1887() {
      int v1 = 473308726;
      int temp1 = v1 * (v1 + 1);

      if (this.f1558 == null) {
         ft v5 = new ft();

         this.f1558 = v5;
      }
   }

   public ht() {
      instance = this;
   }

   public void invalidate() {
      if (this.f1558 != null) {
         this.f1558.close();
         this.f1558 = null;
         int v1 = -342698618;

         
      }
   }

   public void renderFromFbo(Framebuffer maskFbo, int width, int height) {
      if (maskFbo != null) {
         this.m1887();

         GpuTextureView v4 = maskFbo.method_71639();
         if (v4 != null) {
            Framebuffer v5 = MinecraftClient.method_1551().method_1522();
            if (v5 != null) {
               GpuTextureView result = v5.method_71639();

               if (result != null) {
                  this.f1558.render(v4, v5.method_71639(), width, height, this.params);
                  return;
               }
            }
         }
      }
   }

   
}
