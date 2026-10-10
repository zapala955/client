package ru.white.utils.render;

import java.awt.Color;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class vs implements IMinecraft {
   @Generated
   private vs() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Environment(EnvType.CLIENT)
   public static final class at {
      @Generated
      private at() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static final class ws {
      public static void blur(float x, float y, float width, float height, float alpha, int tintColor) {
         vr.blur(x, y, width, height, alpha, tintColor);
      }

      public static void blur(
         float x, float y, float width, float height, float alpha, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor
      ) {
         int v10 = 655104121;
         int var10007 = v10 + 1;

         vr.blur(x, y, width, height, alpha, topLeft, topRight, bottomRight, bottomLeft, tintColor);
      }

      public static void blur(float x, float y, float width, float height, float alpha, float cornerRadius, 

         vr.blur(x, y, width, height, alpha, cornerRadius, tintColor);
         int v7 = -1892542030;
         int result = v7 * (v7 + 1) % 2;

         if (result != 0) {
            }
      }

      @Generated
      private ws() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }

      public static void glass(
         float x,
         float y,
         float width,
         float height,
         float alpha,
         float topLeft,
         float topRight,
         float bottomRight,
         float bottomLeft,
         int tintColor,
         float distortion,
         float waveSize,
         float edgeLight,
         float shine
      ) {

         vr.glass(x, y, width, height, alpha, topLeft, topRight, bottomRight, bottomLeft, tintColor);
         
      }

      public static void glass(
         float x, float y, float width, float height, float alpha, float radius, int tintColor, float distortion, float waveSize, float edgeLight, float shine
      ) {
         int v11 = -1074377109;
         int temp1 = v11 * (v11 + 1);

         vr.blur(x, y, width, height, alpha, radius, tintColor);
      }

      public static void glass(float x, float y, float width, float height, float alpha, float radius, float distortion, float waveSize) {
         vr.blur(x, y, width, height, alpha, radius, 0);
         
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static final class xs {
      public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, float radius, 

         vr.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius);
      }

      public static Identifier urlIdentifier(String url) {

         return tt.getIdentifier(url);
      }

      public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, 

         vr.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, 0.0F);
      }

      public static void texture(
         Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius
      ) {

         vr.texture(id, x, y, width, height, u0, v0, u1, v1, color, smoothness, radius);
      }

      public static boolean isUrlLoaded(String url) {
         tt result = tt.get(url);

         return result.isLoaded();
      }

      public static void texture(Identifier id, float x, float y, float width, float height, 

         vr.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 1.0F, 0.0F);
      }

      public static void url(String url, float x, float y, float width, float height, float smoothness, float radius, int color) {
         Identifier v8 = tt.getIdentifier(url);
         int v9 = 1932972976;
         int result = v9 * (v9 + 1) % 2;

         if (result != 0) {
            }

         if (v8 != null) {
            texture(v8, x, y, width, height, smoothness, radius, color);
         }
      }

      public static void url(String url, float x, float y, float width, float height, float smoothness, int color) {
         int v7 = 230446588;
         int temp1 = v7 * (v7 + 1);

         url(url, x, y, width, height, smoothness, 0.0F, color);
      }

      public static void drawTexture(
         DrawContext context,
         Identifier id,
         float x,
         float y,
         float width,
         float height,
         float u,
         float v,
         float regionWidth,
         float regionHeight,
         float textureWidth,
         float textureHeight,
         int color,
         float radius
      ) {
         float v14 = u / textureWidth;
         float v15 = v / textureHeight;
         float v16 = (u + regionWidth) / textureWidth;
         float v17 = (v + regionHeight) / textureHeight;

         vr.texture(id, x, y, width, height, v14, v15, v16, v17, color, 1.0F, radius);
      }

      @Generated
      private xs() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }

      public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, int color) {
         vr.texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, 0.0F);
         
      }

      public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, 

         vr.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, radius);
      }

      public static void drawTexture(
         DrawContext context,
         Identifier id,
         float x,
         float y,
         float width,
         float height,
         float u,
         float v,
         float regionWidth,
         float regionHeight,
         float textureWidth,
         float textureHeight,

         drawTexture(context, id, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, color, 0.0F);
      }

      public static void url(String url, float x, float y, float width, float height, int color) {
         int v6 = 1597722504;

         url(url, x, y, width, height, 1.0F, 0.0F, color);
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static final class ys {
      public static void outline(float x, float y, float width, float height, float thickness, 

         vr.outline(x, y, width, height, thickness, color);
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
         int v12 = -974466317;
         int var10010 = v12 * (v12 + 1) % 2;

         if (var10010 != 0) {
            }

         vr.glow(x, y, width, height, color, topLeft, topRight, bottomRight, bottomLeft, glowSize, strength, softness);
      }

      public static void clientRect(float x, float y, float width, float height, float alphaPC, float radius) {
         int v6 = up.getColor(255, alphaPC);
         int[] temp4 = new int[4];

         temp4[0] = new Color(24, 25, 31, up.alpha(v6)).getRGB();
         temp4[1] = new Color(30, 32, 37, up.alpha(v6)).getRGB();
         Color var10007 = new Color;
         int var10009 = 14;
         int var10010 = 15;

         var10007./* $VF: Unable to resugar constructor */<init>(var10009, var10010, 21, up.alpha(v6));
         temp4[2] = var10007.getRGB();
         temp4[3] = new Color(13, 14, 20, up.alpha(v6)).getRGB();
         gradientRect(x, y, width, height, temp4, radius);
      }

      public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength, float softness) {

         vr.glow(x, y, width, height, color, radius, glowSize, strength, softness);
      }

      public static void gradientRect(
         float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft
      ) {
         vr.gradientRect(x, y, width, height, colors, topLeft, topRight, bottomRight, bottomLeft);
      }

      public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {

         vr.gradientRect(x, y, width, height, colors, radius);
      }

      public static void rect(float x, float y, float width, float height, int color, float radius) {
         int v6 = 1172507298;

         vr.rect(x, y, width, height, color, radius);
      }

      public static void glow(float x, float y, float width, float height, int color, float radius, float glowSize, float strength) {

         vr.glow(x, y, width, height, color, radius, glowSize, strength);
      }

      public static void rect(float x, float y, float width, float height, int color) {
         int v5 = -398475663;

         int temp4 = v5 * (v5 + 1) % 2;

         if (temp4 != 0) {
            }

         vr.rect(x, y, width, height, color);
      }

      @Generated
      private ys() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }

      public static void glassOutline(float x, float y, float width, float height, float thickness, float radius, float alpha, float shine) {

         vr.glassOutline(x, y, width, height, thickness, radius, alpha, shine);
      }

      public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
         vr.outline(x, y, width, height, thickness, color, radius);
         
      }

      public static void outline(
         float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft
      ) {
         vr.outline(x, y, width, height, thickness, color, topLeft, topRight, bottomRight, bottomLeft);
         int v10 = 1022836365;

         
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

         vr.glassOutline(x, y, width, height, thickness, topLeft, topRight, bottomRight, bottomLeft, alpha, shine);
      }

      public static void roundedCircleProgress(
         DrawContext context, float centerX, float centerY, float radius, float thickness, float progress, int backgroundColor, int startColor, int endColor
      ) {
         progress = Math.max(0.0F, Math.min(1.0F, progress));
         if (!(radius <= 0.0F) && !(thickness <= 0.0F)) {
            us v9 = Client.get().render2D();

            v9.flushAll();
            v9.getCircleProgressPipeline().draw(centerX, centerY, radius, thickness, 1.0F, backgroundColor);
            if (progress > 0.0F) {

               tr v14 = v9.getCircleProgressPipeline();

               v14.draw(centerX, centerY, radius, thickness, progress, up.overCol(startColor, endColor, 0.65F));
            }

            v9.getCircleProgressPipeline().flush();
         }
      }

      public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {

         vr.rect(x, y, width, height, color, topLeft, topRight, bottomRight, bottomLeft);
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static final class zs {
      public static final Matrix4f lastProjMat = new Matrix4f();
      public static final Matrix4f lastModMat = new Matrix4f();
      public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

      @Generated
      private zs() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }

      
   }
}
