package ru.white.utils.system;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Sp;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.render.us;

@Environment(EnvType.CLIENT)
public class Yt {
   private final String name;

   public void drawGradient(String text, float x, float y, int color1, int color2, float size, int speed) {
      StringBuilder v8 = new StringBuilder(text.length() * (12));

      int v11 = -2127755265;
      int temp2 = v11 + 1;

      for (int v9 = 0; v9 < text.length(); v9++) {
         char v10 = text.charAt(v9);
         v8.append(sp.getColor(up.fade(speed, v9 * speed, color1, color2))).append(v10);
      }

      this.draw(v8.toString(), x, y, size, -1);
   }

   public void drawGradientB(String text, float x, float y, int color1, int color2, float size) {
      if (text != null && !text.isEmpty()) {
         int v7 = text.length();
         if (v7 == 1) {
            String v26 = sp.getColor(color1);
            this.draw(v26 + text, x, y, size, -1);
         } else {
            int v8 = color1 >> 24 & 255;
            int temp1 = 16;

            int v9 = color1 >> temp1 & 255;
            int v10 = color1 >> 8 & 255;
            int v11 = color1 & 255;
            int v12 = color2 >> 24 & 255;
            int v13 = color2 >> 16 & 255;
            int v14 = color2 >> 8 & 255;
            int v15 = color2 & 255;
            float v16 = 1.0F / (float)(v7 - 1);
            StringBuilder v17 = new StringBuilder(v7 * (12));
            int v18 = 0;

            while (v18 < v7) {
               float v19 = (float)v18 * v16;
               double v31 = Math.cos(Math.PI * (double)v19);

               v19 = 0.5F - 0.5F * (float)v31;
               int v20 = v8 + Math.round((float)(v12 - v8) * v19);
               int v21 = v9 + Math.round((float)(v13 - v9) * v19);
               int v22 = v10 + Math.round((float)(v14 - v10) * v19);
               int v23 = v11 + Math.round((float)(v15 - v11) * v19);
               int v24 = v20 << 24 | v21 << 16 | v22 << 8 | v23;
               v17.append(sp.getColor(v24)).append(text.charAt(v18));
               v18++;
               
            }

            this.draw(v17.toString(), x, y, size, -1);
         }
      }
   }

   public Yt(String name) {
      this.name = name;
   }

   public void drawGRS(CharSequence text, double x, double y, int c1, int c2, float size) {
      if (text != null) {
         int v9 = text.length();
         if (v9 != 0) {
            float v10 = 0.0F;
            boolean v11 = v9 == 1;
            float v12 = v11 ? 0.0F : 1.0F / (float)(v9 - 1);
            int v13 = c1 >>> 24 & 255;

            int v14 = c1 >>> 16 & 255;
            int v15 = c1 >>> 8 & 255;
            int v16 = c1 & 255;
            int v17 = c2 >>> 24 & 255;
            int v18 = c2 >>> 16 & 255;
            int v19 = c2 >>> 8 & 255;
            int v20 = c2 & 255;
            StringBuilder v21 = new StringBuilder(1);
            v21.append('\u0000');

            for (int v22 = 0; v22 < v9; v22++) {
               char v23 = text.charAt(v22);
               float v24 = v11 ? 0.0F : (float)v22 * v12;
               int v25 = v13 + Math.round((float)(v17 - v13) * v24);
               int v26 = v14 + Math.round((float)(v18 - v14) * v24);
               float v36 = (float)(v19 - v15) * v24;

               int v27 = v15 + Math.round(v36);
               int v28 = v16 + Math.round((float)(v20 - v16) * v24);
               int v29 = (v25 & 255) << 24
                  | (v26 & 255) << 16
                  | (v27 & 255) << 8
                  | v28 & 255;
               v21.setCharAt(0, v23);
               String result = sp.getColor(v29);
               String v31 = String.valueOf(v21);
               String v32 = result;
               String v30 = v32 + v31;
               this.draw(v30, (float)(x + (double)v10), (float)y, size, -1);
               v10 += this.getWidth(v30, size);
            }
         }
      }
   }

   public void drawRightAlignFadeLeft(String text, float rightX, float y, float fadeStartX, int color, float size) {
      gu result = Client.get().render2D().getFontRenderer();

      result.drawTextRightAlignFadeLeft(this.name, text, rightX, y, size, fadeStartX, color);
   }

   public void drawWrappedText(String text, float x, float y, float maxWidth, int color, float size) {
      String[] v7 = text.split(" ");
      StringBuilder v8 = new StringBuilder();
      float v9 = y;

      for (String v13 : v7) {
         String v19;
         if (v8.length() == 0) {

            v19 = v13;
         } else {
            String v17 = String.valueOf(v8);
            v19 = v17 + " " + v13;
         }

         String v14 = v19;
         float v15 = this.getWidth(v14, size);
         if (v15 > maxWidth) {
            this.draw(v8.toString(), x, v9, size, color);
            v8 = new StringBuilder(v13);
            v9 += this.getHeight(size);
         } else {
            v8 = new StringBuilder(v14);
         }
      }

      if (v8.length() > 0) {
         this.draw(v8.toString(), x, v9, size, color);
      }
   }

   public void drawGradient(String text, float x, float y, int color1, int color2, float size) {
      int v7 = 1566049898;
      int temp3 = v7 * (v7 + 1);

      this.drawGradient(text, x, y, color1, color2, size, 8);
   }

   public void drawCentered(String text, float x, float y, float size, int color) {
      gu result = Client.get().render2D().getFontRenderer();
      int v6 = -1079468372;

      result.drawCenteredText(this.name, text, x, y, size, color);
   }

   public float getWidth(String text, float size) {
      float result = Client.get().render2D().getFontRenderer().getTextWidth(this.name, text, size);

      return result;
   }

   public void drawCenterGradient(String text, float x, float y, float size, int color, int color2) {
      float temp2 = x - this.getWidth(text, size) / 2.0F;
      int v7 = 781592544;
      int var10006 = v7 * (v7 + 1) % 2;

      if (var10006 != 0) {

         }

      this.drawGradient(text, temp2, y, color, color2, size);
   }

   public void drawFadingTextReverse(String text, float x, float y, float maxWidth, int color, float size) {
      Client.get().render2D().getFontRenderer().drawTextFadingReverse(this.name, text, x, y, size, maxWidth, color);

      int v8 = -61435085;
      int v10 = v8 * (v8 + 1);

      
   }

   public String getName() {
      return this.name;
   }

   public float getHeight(float size) {
      float result = Client.get().render2D().getFontRenderer().getLineHeight(this.name, size);

      return result;
   }

   public void drawFadingText(String text, float x, float y, float maxWidth, int color, float size) {
      us result = Client.get().render2D();

      result.getFontRenderer().drawTextFading(this.name, text, x, y, size, maxWidth, color);
   }

   public float getWrappedHeight(String text, float maxWidth, float size) {
      if (text != null && !text.isEmpty()) {
         String[] v4 = text.split(" ");
         StringBuilder v5 = new StringBuilder();
         int v6 = 1;

         for (String v10 : v4) {
            String result;
            if (v5.length() == 0) {
               result = v10;
            } else {
               String v13 = String.valueOf(v5);
               StringBuilder v15 = new StringBuilder().append((Object)v13).append(" ").append((Object)v10);

               result = v15.toString();
            }

            String v11 = result;
            if (this.getWidth(v11, size) > maxWidth) {
               v5 = new StringBuilder(v10);
               v6++;
            } else {
               v5 = new StringBuilder(v11);
            }
         }

         return (float)v6 * this.getHeight(size);
      } else {
         return 0.0F;
      }
   }

   public void draw(String text, float x, float y, float size, int color) {
      Client result = Client.get();
      int v6 = -853892840;

      result.render2D().getFontRenderer().drawText(this.name, text, x, y, size, color);
   }

   
}
