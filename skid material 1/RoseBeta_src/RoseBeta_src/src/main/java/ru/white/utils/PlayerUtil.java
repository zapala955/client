package ru.white.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class PlayerUtil {
   private float speed;
   private float f1271;
   float barHeight;
   private final MinecraftClient f1268 = MinecraftClient.method_1551();
   private float f1270;
   private double f1272;
   private float f1269;
   private boolean enabled;

   public void setMax(float max) {

      this.f1271 = max;
   }

   public PlayerUtil() {
      this.speed = 8.0F;
      this.setEnabled(true);
      GLFW.glfwSetScrollCallback(this.f1268.method_22683().method_4490(), (window, xOffset, yOffset) -> {
         int v7 = -1504946737;
         int temp2 = v7 * (v7 + 1);

         this.f1272 += yOffset;
      });
   }

   public float lerp(float a, float b, double f) {
      float result = (float)((double)a + f * (double)(b - a));

      int v5 = -1621508562;

      return result;
   }

   public void update() {
      float result = (float)this.f1272;

      float v1 = result * this.speed * 10.0F;
      float v2 = 0.0F;
      this.f1270 = this.lerp(this.f1270, this.f1269, (double)(this.speed / 100.0F));
      if (!this.enabled) {

         this.f1272 = 0.0;
      } else {
         float temp1 = Math.max(this.f1269 + v1 / 2.0F, this.f1271 - (v1 == 0.0F ? 0.0F : v2));

         this.f1269 = Math.min(temp1, v1 == 0.0F ? 0.0F : v2);
         this.f1272 = 0.0;
      }
   }

   public float getScroll() {

      return this.f1270;
   }

   public void reset() {

      this.f1270 = 0.0F;
      this.f1269 = 0.0F;
   }

   public float getMax() {
      return this.f1271;
   }

   public void setMax(float max, float height) {
      int v3 = 1857973927;
      int v4 = 2031645905;

      this.f1271 = -max + height;
   }

   public void render(float x, float y, float width, float height, float alpha) {
      if (this.f1271 != 0.0F) {
         float v6 = this.getScroll() / this.getMax();
         float temp3 = this.getMax();
         float temp4 = this.getMax();

         this.barHeight = this.lerp(height - temp3 / (temp4 - height) * height, this.barHeight, 0.9F);
         boolean v12;
         if (this.barHeight < height) {

            v12 = true;
         } else {
            v12 = false;
         }

         boolean v7 = v12;
         if (v7) {
            float v9 = y + height * v6 - this.barHeight * v6;
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public float clamp(float val, float min, float max) {
      float result = Math.max(min, Math.min(max, val));
      int v4 = 720931419;

      return result;
   }

   
}
