package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.ClickGui;

@Environment(EnvType.CLIENT)
public final class ss {
   private static float f1521;
   private static final float[] segB = new float[8];
   private static final float HEAD_WHITEN = 0.6F;
   private static final float[] segC = new float[8];
   private static final float[] PASS_STEP = new float[]{5.0F, 3.0F, 1.5F};
   private static final float[] PASS_THICKNESS = new float[]{13.0F, 6.0F, 2.0F};
   private static final float TRAIL_FRACTION = 0.17F;
   private static final float HEAD_GLOW_ALPHA = 0.5F;
   private static final float[] segD = new float[8];
   private static final float[] segA = new float[8];
   private static final float[] segOffset = new float[8];
   private static final float HEAD_GLOW = 15.0F;
   private static final float HEAD_SIZE = 3.4F;
   private static final int SEGMENTS = 8;
   private static long f1522 = -137122241495071768L + 137122241495071767L;
   private static final boolean[] isArc = new boolean[8];
   private static final float LOOP_SECONDS = 4.2F;
   private static final float[] segLength = new float[8];
   private static final float[] PASS_WHITEN = new float[]{0.0F, 0.12F, 0.45F};
   private static final float TRAIL_FALLOFF = 1.7F;
   private static final float[] PASS_ALPHA = new float[]{0.1F, 0.24F, 1.0F};
   private static final int WHITE = -1;

   private static float m1855(float distanceBehindHead, float trail) {
      float v2 = 1.0F - distanceBehindHead / trail;
      return v2 <= 0.0F ? 0.0F : (float)Math.pow((double)v2, 1.7F);
   }

   public static void render(float x, float y, float width, float height, float radius, float scale, float alpha) {
      float v7 = m1851();
      if (!(alpha <= 0.01F) && !(v7 <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F)) {
         float v8 = Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F));
         float v9 = Math.max(0.0F, width - v8 * 2.0F);
         float v10 = Math.max(0.0F, height - v8 * 2.0F);
         float v11 = (float)(Math.PI * (double)v8 * 0.5);
         float v12 = (v9 + v10) * 2.0F + v11 * 4.0F;
         if (!(v12 <= 1.0F)) {

            m1859(x, y, width, height, v8, v9, v10, v11);
            m1850();
            float v13 = f1521 * v12;
            float v14 = v12 * 0.17F;
            int v15 = up.client();

            for (int v16 = 0; v16 < PASS_THICKNESS.length; v16++) {
               float v17 = Math.max(0.5F, PASS_THICKNESS[v16] * scale);
               float v18 = Math.max(0.6F, PASS_STEP[v16] * scale);
               int v19 = up.interpolate(v15, -1, (double)PASS_WHITEN[v16]);
               float v20 = PASS_ALPHA[v16] * alpha * v7;

               for (int v21 = 0; v21 < 8; v21++) {
                  if (!(segLength[v21] <= 0.001F)) {
                     for (int v22 = 0; v22 <= 1; v22++) {
                        float v23 = segOffset[v21] - (float)v22 * v12;
                        float v24 = Math.max(v23, v13 - v14);
                        float v25 = Math.min(v23 + segLength[v21], v13);
                        if (!(v25 - v24 <= 0.001F)) {
                           if (isArc[v21]) {
                              m1854(v21, v23, v24 - v23, v25 - v23, v13, v14, v17, v18, v19, v20);
                           } else {
                              m1853(v21, v23, v24 - v23, v25 - v23, v13, v14, v17, v19, v20);
                           }
                        }
                     }
                  }
               }
            }

            m1852(v13, v15, scale, alpha * v7);
         }
      }
   }

   private static void m1854(int segment, float base, float from, float to, float head, float trail, float thickness, float step, int color, float passAlpha) {
      double temp1 = (double)((to - from) / step);

      int v10 = Math.max(1, (int)Math.ceil(temp1));

      for (int v11 = 0; v11 <= v10; v11++) {
         float v18 = (to - from) * (float)v11;

         float v12 = from + v18 / (float)v10;
         float v13 = m1855(head - (base + v12), trail);
         if (!(v13 <= 0.002F)) {
            float v14 = m1857(segment, v12) - thickness / 2.0F;
            float v15 = m1858(segment, v12) - thickness / 2.0F;
            vr.rect(v14, v15, thickness, thickness, up.replAlpha(color, passAlpha * v13), thickness / 2.0F);
         }
      }
   }

   private static float m1861(int segment, float offset, float length, float centerX, float centerY, float startAngle, float radius) {
      isArc[segment] = true;
      segOffset[segment] = offset;
      segLength[segment] = radius <= 0.001F ? 0.0F : length;
      segA[segment] = centerX;
      segB[segment] = centerY;

      segC[segment] = startAngle;

      segD[segment] = radius;
      return offset + segLength[segment];
   }

   private static float m1858(int segment, float local) {
      if (isArc[segment]) {
         return segB[segment] + segD[segment] * (float)Math.sin((double)(segC[segment] + local / segD[segment]));
      } else {
         float result = segB[segment];

         return result + segD[segment] * local;
      }
   }

   private static float m1860(int segment, float offset, float length, float originX, float originY, float dirX, float dirY) {
      isArc[segment] = false;
      segOffset[segment] = offset;
      segLength[segment] = length;
      segA[segment] = originX;
      segB[segment] = originY;
      segC[segment] = dirX;
      segD[segment] = dirY;
      float result = offset + length;
      int v7 = -36391232;

      return result;
   }

   private static void m1853(int segment, float base, float from, float to, float head, float trail, float thickness, int color, float passAlpha) {
      float v9 = m1857(segment, from);
      float v10 = m1858(segment, from);
      float v11 = m1857(segment, to);
      float v12 = m1858(segment, to);
      int v13 = up.replAlpha(color, passAlpha * m1855(head - (base + from), trail));
      int v14 = up.replAlpha(color, passAlpha * m1855(head - (base + to), trail));
      if (segD[segment] == 0.0F) {
         boolean v15 = v9 <= v11;
         int v16 = v15 ? v13 : v14;
         int v17 = v15 ? v14 : v13;
         vr.gradientRect(Math.min(v9, v11), v10 - thickness / 2.0F, Math.abs(v11 - v9), thickness, new int[]{v16, v17, v17, v16}, 0.0F);
      } else {
         boolean v20 = v10 <= v12;
         int result;
         if (v20) {
            result = v13;
            int v18 = -2028120458;

            
         } else {
            result = v14;
         }

         int v21 = result;
         int v22 = v20 ? v14 : v13;
         vr.gradientRect(v9 - thickness / 2.0F, Math.min(v10, v12), thickness, Math.abs(v12 - v10), new int[]{v21, v21, v22, v22}, 0.0F);
      }
   }

   private ss() {
   }

   private static void m1859(float x, float y, float width, float height, float radius, float edgeX, float edgeY, float arc) {
      float v8 = x + width;
      float v9 = y + height;
      float v10 = 0.0F;
      v10 = m1860(0, v10, edgeX, x + radius, y, 1.0F, 0.0F);
      v10 = m1861(1, v10, arc, v8 - radius, y + radius, (float) (-Math.PI / 2), radius);
      v10 = m1860(2, v10, edgeY, v8, y + radius, 0.0F, 1.0F);
      v10 = m1861(3, v10, arc, v8 - radius, v9 - radius, 0.0F, radius);
      v10 = m1860(4, v10, edgeX, v8 - radius, v9, -1.0F, 0.0F);
      v10 = m1861(5, v10, arc, x + radius, v9 - radius, (float) (Math.PI / 2), radius);
      v10 = m1860(6, v10, edgeY, x, v9 - radius, 0.0F, -1.0F);
      int result = 7;
      float temp3 = x + radius;

      m1861(result, v10, arc, temp3, y + radius, (float) Math.PI, radius);
   }

   private static float m1851() {
      try {
         Client result = Client.get();
         int v1 = -2097474785;

         ClickGui v0 = result.moduleManager().get(ClickGui.class);
         if (v0 != null) {
            return v0.glowIntensity.getValue();
         }
      } catch (Exception v4) {
      }

      return 1.0F;
   }

   private static int m1856(float position) {
      for (int v1 = 7; v1 >= 0; v1--) {
         if (segLength[v1] > 0.001F && position >= segOffset[v1]) {

            return v1;
         }
      }

      return -1;
   }

   public static void render(float x, float y, float width, float height, float radius, float scale, float alpha, float delta) {
      float v12;
      int result = (v12 = delta - 0.0F) == 0.0F ? 0 : (v12 < 0.0F ? -1 : 1);

      if (result > 0 && delta < 0.25F) {

         f1521 += delta / 4.2F;
         f1521 = f1521 - (float)Math.floor((double)f1521);
         f1522 = System.nanoTime();
         m1849(x, y, width, height, radius, scale, alpha);
      } else {
         render(x, y, width, height, radius, scale, alpha);
      }
   }

   private static void m1849(float x, float y, float width, float height, float radius, float scale, float alpha) {
      float v7 = m1851();
      if (!(alpha <= 0.01F) && !(v7 <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F)) {
         float v8 = Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F));
         float v9 = Math.max(0.0F, width - v8 * 2.0F);
         float v10 = Math.max(0.0F, height - v8 * 2.0F);
         float v11 = (float)(Math.PI * (double)v8 * 0.5);
         float v12 = (v9 + v10) * 2.0F + v11 * 4.0F;
         if (!(v12 <= 1.0F)) {
            m1859(x, y, width, height, v8, v9, v10, v11);
            float v13 = f1521 * v12;
            float v14 = v12 * 0.17F;
            int v15 = up.client();

            for (int v16 = 0; v16 < PASS_THICKNESS.length; v16++) {
               float v17 = Math.max(0.5F, PASS_THICKNESS[v16] * scale);
               float v18 = Math.max(0.6F, PASS_STEP[v16] * scale);
               int v19 = up.interpolate(v15, -1, (double)PASS_WHITEN[v16]);
               float v20 = PASS_ALPHA[v16] * alpha * v7;

               for (int v21 = 0; v21 < 8; v21++) {
                  if (!(segLength[v21] <= 0.001F)) {
                     for (int v22 = 0; v22 <= 1; v22++) {

                        float v23 = segOffset[v21] - (float)v22 * v12;
                        float v24 = Math.max(v23, v13 - v14);
                        float v25 = Math.min(v23 + segLength[v21], v13);
                        if (!(v25 - v24 <= 0.001F)) {
                           if (isArc[v21]) {
                              m1854(v21, v23, v24 - v23, v25 - v23, v13, v14, v17, v18, v19, v20);
                           } else {
                              m1853(v21, v23, v24 - v23, v25 - v23, v13, v14, v17, v19, v20);
                           }
                        }
                     }
                  }
               }
            }

            m1852(v13, v15, scale, alpha * v7);
         }
      }
   }

   private static void m1852(float head, int accent, float scale, float alpha) {
      int v4 = m1856(head);
      if (v4 >= 0) {
         float v5 = head - segOffset[v4];
         float v6 = 3.4F * scale;
         float v7 = m1857(v4, v5) - v6 / 2.0F;
         float v8 = m1858(v4, v5) - v6 / 2.0F;
         int temp4 = up.replAlpha(accent, 0.5F * alpha);

         vr.glow(v7, v8, v6, v6, temp4, v6 / 2.0F, 15.0F * scale, 0.85F);
         temp4 = up.interpolate(accent, -1, 0.6F);

         vr.rect(v7, v8, v6, v6, up.replAlpha(temp4, alpha), v6 / 2.0F);
      }
   }

   private static float m1857(int segment, float local) {
      if (isArc[segment]) {
         return segA[segment] + segD[segment] * (float)Math.cos((double)(segC[segment] + local / segD[segment]));
      } else {
         float result = segA[segment];
         float temp1 = segC[segment] * local;

         return result + temp1;
      }
   }

   private static void m1850() {
      long v0 = System.nanoTime();
      if (f1522 < 0L) {
         f1522 = v0;
      } else {
         float v2 = (float)(v0 - f1522) / 1.0E9F;
         f1522 = v0;
         if (!(v2 < 0.0F)) {
            if (v2 > 0.25F) {
               v2 = 0.025F;
            }

            if (!(v2 < 0.002F)) {

               f1521 += v2 / 4.2F;

               f1521 = f1521 - (float)Math.floor((double)f1521);
            }
         }
      }
   }

   
}
