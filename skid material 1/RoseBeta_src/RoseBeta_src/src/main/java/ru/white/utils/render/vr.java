package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.theme.wo;

@Environment(EnvType.CLIENT)
public final class vr {
   private static final float GLASS_DISTORTION = 125.0F;
   private static final float GLASS_WAVE_SIZE = 75.0F;

   public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
      int[] v9 = up.solid(color);

      float[] v10 = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      m1732().getRectPipeline().drawRect(x, y, width, height, v9, v10);
   }

   public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength) {

      int v9 = -1612945011;

      glow(x, y, width, height, color, radius, radius, radius, radius, glowSize, strength, 1.0F);
   }

   public static void texture(
      Identifier id,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int color,
      float smoothness,
      float radius,
      float rotation,
      float shearX
   ) {
      int[] v14 = new int[]{color, color, color, color};
      float[] result = new float[]{radius, radius, 0.0F, 0.0F};

      result[2] = radius;
      result[3] = radius;
      float[] v15 = result;
      m1732().getTexturePipeline().drawTexture(id, x, y, width, height, u0, v0, u1, v1, v14, v15, smoothness, rotation, shearX);
   }

   public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
      int v7 = 769542020;
      int var10007 = v7 * (v7 + 1) % 2;

      if (var10007 != 0) {
         }

      outline(x, y, width, height, thickness, color, radius, radius, radius, radius);
   }

   public static void flush() {
      m1732().flushAll();
      int v0 = 1782782277;

      
   }

   public static void blur(float x, float y, float width, float height, float alpha, float radius, 

      blur(x, y, width, height, alpha, radius, radius, radius, radius, tintColor);
   }

   public static void glowTexture(
      Identifier id, float x, float y, float width, float height, float smoothness, float radius, int color, float rotation, float shearX
   ) {
      int[] result = new int[]{color, color, 0, 0};

      result[2] = color;
      result[3] = color;
      int[] v10 = result;
      float[] v15 = new float[]{radius, 0.0F, 0.0F, 0.0F};
      int v12 = 954103887;

      v15[1] = radius;
      v15[2] = radius;
      v15[3] = radius;
      float[] v11 = v15;
      m1732().getTexturePipeline().drawGlowTexture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, v10, v11, smoothness, rotation, shearX);
   }

   public static void texture(
      Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius
   ) {

      texture(id, x, y, width, height, u0, v0, u1, v1, color, smoothness, radius, 0.0F, 0.0F);
   }

   public static void texture(
      Identifier id, float x, float y, float width, float height, float smoothness, float radius, int color, float rotation, float shearX
   ) {

      texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius, rotation, shearX);
   }

   public static void outline(float x, float y, float width, float height, float thickness, 

      outline(x, y, width, height, thickness, color, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, float radius, int color) {
      int v8 = 1942593289;
      int var10006 = v8 * (v8 + 1);
      int v9 = 1681622076;

      texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius);
   }

   public static void outline(
      float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft
   ) {
      int[] v10 = up.solid8(color);
      float[] result = new float[8];
      result[0] = thickness;
      result[1] = thickness;
      result[2] = thickness;
      result[3] = thickness;
      result[4] = thickness;
      result[5] = thickness;
      result[6] = thickness;
      result[7] = thickness;
      float[] v11 = result;
      float[] v12 = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      rs v15 = m1732().getOutlinePipeline();
      int v13 = -502440037;
      int temp2 = v13 + 1;

      v15.drawOutline(x, y, width, height, v10, v11, v12, 1.0F);
   }

   public static void gradientRect(
      float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft
   ) {
      float[] result = new float[4];

      result[0] = topLeft;
      result[1] = topRight;
      result[2] = bottomRight;
      result[3] = bottomLeft;
      float[] v9 = result;
      m1732().getRectPipeline().drawRect(x, y, width, height, colors, v9);
   }

   public static void blur(
      float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
   ) {
      float[] v10 = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      qr v11 = m1732().getBlurPipeline();

      if (wo.getDistortion()) {
         v11.drawGlassBlur(x, y, width, height, alpha, v10, tintColor, 125.0F, 75.0F, 0.0F, 0.0F);
      } else {
         v11.drawBlur(x, y, width, height, alpha, v10, tintColor);
      }
   }

   public static void glow(
      float x,
      float y,
      float width,
      float height,
      int color,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float glowSize,
      float strength,
      float softness
   ) {
      int v13 = 1206533820;

      float[] result = new float[4];

      result[0] = topLeft;
      result[1] = topRight;
      result[2] = bottomRight;
      result[3] = bottomLeft;
      float[] v12 = result;
      m1732().getRectPipeline().drawGlow(x, y, width, height, color, v12, glowSize, strength, softness);
   }

   public static void glass(
      float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
   ) {
      float[] v10 = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      qr result = m1732().getBlurPipeline();

      result.drawGlassBlur(x, y, width, height, alpha, v10, tintColor, 125.0F, 75.0F, 0.0F, 0.0F);
   }

   public static void glassOutline(float x, float y, float width, float height, float thickness, float radius, float alpha, float shine) {
      int v8 = -1279361415;

      glassOutline(x, y, width, height, thickness, radius, radius, radius, radius, alpha, shine);
   }

   public static void blur(float x, float y, float width, float height, float alpha, int tintColor) {
      int v6 = -636852901;

      blur(x, y, width, height, alpha, 0.0F, 0.0F, 0.0F, 0.0F, tintColor);
   }

   public static void rect(float x, float y, float width, float height, 

      short v6 = -17315;

      rect(x, y, width, height, color, radius, radius, radius, radius);
   }

   private static us m1732() {
      us result = Client.get().render2D();
      int v0 = -937700231;

      return result;
   }

   public static void glassOutline(
      float x,
      float y,
      float width,
      float height,
      float thickness,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float alpha,
      float shine
   ) {
      float v11 = Math.max(0.0F, alpha);
      float v12 = Math.max(0.0F, shine);
      int[] result = new int[8];
      result[0] = m1733(v11 * 0.42F);
      float temp3 = v11 * (0.55F + v12 * 0.3F);

      result[1] = m1733(temp3);
      result[2] = m1733(v11 * (0.28F + v12 * 0.1F));
      result[3] = m1733(v11 * 0.24F);
      result[4] = m1733(v11 * (0.34F + v12 * 0.16F));
      result[5] = m1733(v11 * (0.48F + v12 * 0.24F));
      result[6] = m1733(v11 * 0.3F);
      result[7] = m1733(v11 * (0.62F + v12 * 0.38F));
      int[] v13 = result;
      float[] v19 = new float[8];
      v19[0] = thickness;

      v19[1] = thickness + v12 * 0.18F;
      v19[2] = thickness * 0.86F;
      v19[3] = thickness * 0.78F;

      v19[4] = thickness * 0.9F;
      v19[5] = thickness + v12 * 0.16F;
      v19[6] = thickness * 0.84F;
      v19[7] = thickness + v12 * 0.24F;
      float[] v14 = v19;
      float[] v15 = new float[]{topLeft, topRight, bottomRight, bottomLeft};
      m1732().getOutlinePipeline().drawOutline(x, y, width, height, v13, v14, v15, 1.35F);
   }

   public static void rect(float x, float y, float width, float height, 

      rect(x, y, width, height, color, 0.0F);
   }

   public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength, float softness) {
      int v9 = -910560702;

      glow(x, y, width, height, color, radius, radius, radius, radius, glowSize, strength, softness);
   }

   private static int m1733(float alpha) {
      int result = 255;

      return up.getColor(result, Math.min(alpha, 1.0F));
   }

   public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {

      gradientRect(x, y, width, height, colors, radius, radius, radius, radius);
   }

   private vr() {
   }

   public static void texture(Identifier id, float x, float y, float width, float height, 

      texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 1.0F, 0.0F);
   }

   
}
