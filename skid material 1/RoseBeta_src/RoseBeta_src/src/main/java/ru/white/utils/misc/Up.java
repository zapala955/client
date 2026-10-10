package ru.white.utils.misc;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.white.auth.StrDec;
import ru.white.manager.Theme;
import ru.white.module.impl.render.Hud;
import ru.white.screen.ClickGuiScreen;
import ru.white.theme.wo;
import ru.white.utils.Cq;

@Environment(EnvType.CLIENT)
public final class Up {
   private static final DelayQueue<up.wp> cleanupQueue = new DelayQueue<>();
   public static final int WHITE = getColor(255);
   private static final ConcurrentHashMap<up.xp, up.wp> colorCache = new ConcurrentHashMap<>();
   public static final int BLACK = getColor(0);
   public static final int GREEN = getColor(0, 255, 0);
   public static final int BLUE = getColor(0, 0, 255);
   public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");
   public static final int RED = getColor(255, 0, 0);
   private static final long CACHE_EXPIRATION_TIME = 60000L;
   public static final int YELLOW = getColor(255, 255, 0);
   public static Char2IntArrayMap colorCodes = new Char2IntArrayMap() {
      {
         this.put((char)(48), 0);
         this.put((char)(49), 170);
         this.put((char)(50), 43520);
         this.put((char)(51), 43690);
         this.put((char)(52), 11141120);
         this.put((char)(53), 11141290);
         this.put((char)(54), 16755200);
         this.put((char)(55), 11184810);
         this.put((char)(56), 5592405);
         this.put((char)(57), 5592575);
         this.put((char)(65), 5635925);
         this.put((char)(66), 5636095);
         this.put((char)(67), 16733525);
         this.put((char)(68), 16733695);
         this.put((char)(69), 16777045);
         this.put((char)(70), 16777215);
      }

      
   };
   private static final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);

   public static int interpolateColor(int color1, int color2, float amount, boolean trueColor) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      Color v4 = new Color(color1, true);
      Color v5 = new Color(color2, true);
      Color result = new Color;
      int temp2 = (int)((float)v4.getRed() + (float)(v5.getRed() - v4.getRed()) * amount);
      int temp3 = (int)((float)v4.getGreen() + (float)(v5.getGreen() - v4.getGreen()) * amount);
      int temp4 = v4.getBlue();

      result./* $VF: Unable to resugar constructor */<init>(
         temp2,
         temp3,
         (int)((float)temp4 + (float)(v5.getBlue() - v4.getBlue()) * amount),
         (int)((float)v4.getAlpha() + (float)(v5.getAlpha() - v4.getAlpha()) * amount)
      );
      return result.getRGB();
   }

   private static String m1583(int red, int green, int blue, int alpha) {
      StringBuilder result = new StringBuilder().append(red).append(",").append(green);
      int v8 = 911210187;
      int temp3 = v8 + 1;

      return result.append(",").append(blue).append(",").append(alpha).toString();
   }

   public static int[] getRGB(int c) {
      int[] result = new int[]{red(c), 0, 0};
      int temp3 = green(c);

      result[1] = temp3;
      result[2] = blue(c);
      return result;
   }

   public static int replAlpha(int color, int alpha) {
      int result = red(color);

      return getColorRaw(result, green(color), blue(color), alpha);
   }

   public static int getHealthColor(float health, float maxHealth) {
      float v2 = MathHelper.method_15363(health / maxHealth, 0.0F, 1.0F);
      if (v2 <= 0.5F) {
         Color v5 = new Color;

         v5./* $VF: Unable to resugar constructor */<init>(255, 0, 0);
         return interpolateColor2(v5.getRGB(), new Color(255, 255, 0).getRGB(), v2 / 0.5F);
      } else {
         Color result = new Color;
         int temp2 = 255;

         result./* $VF: Unable to resugar constructor */<init>(temp2, 255, 0);
         return interpolateColor2(result.getRGB(), new Color(0, 255, 0).getRGB(), (v2 - 0.5F) / 0.5F);
      }
   }

   public static int getColor(int brightness) {
      int v1 = 1524202818;
      int temp1 = v1 * (v1 + 1);

      temp1 %= 2;

      if (temp1 != 0) {
         }

      return getColor(brightness, brightness, brightness);
   }

   public static float greenf(int c) {
      float result = (float)green(c);

      return result / 255.0F;
   }

   public static int getClientColor1(int index) {
      int result = eh.getVisualsColor();

      return result;
   }

   public static int client() {
      Theme v0 = m1576();
      Theme v1 = m1577();
      int result = v0.getClient();
      int temp1 = v1.getClient();
      float temp3 = ko.animation14.getOutput();

      double temp2 = (double)(1.0F - temp3);

      return interpolate(result, temp1, temp2);
   }

   public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static int[] unpack(int color) {
      int[] result = new int[4];

      result[0] = color >> 16 & 255;
      result[1] = color >> 8 & 255;
      result[2] = color & 255;
      int temp3 = color >> 24;

      result[3] = temp3 & 255;
      return result;
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      double result = (double)oldValue;
      double temp1 = (double)newValue;
      float temp2 = (float)interpolationValue;

      return interpolateD(result, temp1, (double)temp2).intValue();
   }

   public static int getColor(int red, int green, int blue, float alpha) {
      int v4 = 1660141733;

      return getColor(red, green, blue, Math.round(alpha * 255.0F));
   }

   private static float m1578(float v) {
      if (v < 0.0F) {
         return 0.0F;
      } else {
         return v > 1.0F ? 1.0F : v;
      }
   }

   public static String removeFormatting(String text) {
      String result;
      if (text != null && !text.isEmpty()) {
         result = FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
         int v1 = 747062859;

         
      } else {
         result = null;
      }

      return result;
   }

   public static Color TwoColoreffect(Color color, Color color2, double n) {
      float v4 = MathHelper.method_15363((float)Math.sin((Math.PI * 6) * (n / 4.0 % 1.0)) / 2.0F + 0.5F, 0.0F, 1.0F);
      Color result = new Color;
      int v6 = -1726216510;
      int temp2 = v6 * (v6 + 1);

      float v9 = MathHelper.method_16439((float)color.getRed() / 255.0F, (float)color2.getRed() / 255.0F, v4);
      float temp3 = (float)color.getGreen() / 255.0F;
      int v10 = color2.getGreen();

      result./* $VF: Unable to resugar constructor */<init>(
         v9,
         MathHelper.method_16439(temp3, (float)v10 / 255.0F, v4),
         MathHelper.method_16439((float)color.getBlue() / 255.0F, (float)color2.getBlue() / 255.0F, v4),
         MathHelper.method_16439((float)color.getAlpha() / 255.0F, (float)color2.getAlpha() / 255.0F, v4)
      );
      return result;
   }

   public static int[] rgbas(int color) {
      int[] result = new int[4];

      result[0] = (int)((float)(color >> 16 & 255) / 255.0F);
      int temp3 = color >> 8 & 255;

      result[1] = (int)((float)temp3 / 255.0F);
      result[2] = (int)((float)(color & 255) / 255.0F);
      result[3] = (int)((float)(color >> 24 & 255) / 255.0F);
      return result;
   }

   public static float[] normalize(int color) {
      int[] v1 = unpack(color);
      float[] result = new float[4];
      float temp3 = (float)v1[0];
      int v2 = 60165466;

      result[0] = temp3 / 255.0F;
      result[1] = (float)v1[1] / 255.0F;
      result[2] = (float)v1[2] / 255.0F;
      result[3] = (float)v1[3] / 255.0F;
      return result;
   }

   public static int applyOpacity(int color, float opacity) {
      int v2 = color >> 24 & 255;
      int v3 = (int)((float)v2 * opacity);
      v3 = Math.max(0, Math.min(255, v3));
      int result = color & 16777215 | v3 << 24;

      return result;
   }

   public static int[] getRGBA(int c) {

      int[] result = new int[]{red(c), 0, 0, 0};

      result[1] = green(c);
      result[2] = blue(c);
      result[3] = alpha(c);
      return result;
   }

   private static int m1582(int red, int green, int blue, int alpha) {
      int result = MathHelper.method_15340(alpha, 0, 255) << 24
         | MathHelper.method_15340(red, 0, 255) << 16;
      int v4 = -1476730748;

      return result
         | MathHelper.method_15340(green, 0, 255) << 8
         | MathHelper.method_15340(blue, 0, 255);
   }

   private static float m1580(float f1, float f2, float ratio) {
      if ((double)Math.abs(f2 - f1) > 0.5) {

         if (f2 > f1) {
            f1++;
         } else {
            f2++;
         }
      }

      float v3 = f1 + (f2 - f1) * ratio;
      return v3 > 1.0F ? v3 - 1.0F : v3;
   }

   public static int overCol(int color1, 

      return overCol(color1, color2, 0.5F);
   }

   public static int green(int c) {
      int result = c >> 8;

      return result & 255;
   }

   public static int gradient(int first, int second, int third, int index, int speed) {
      float v5 = ((float)System.currentTimeMillis() / (float)speed + (float)index) % 360.0F / 360.0F;
      int v6;
      if (v5 < 0.5F) {
         v6 = interpolate(first, second, (double)(v5 * 2.0F));
      } else {
         v6 = interpolate(second, third, (double)((v5 - 0.5F) * 2.0F));
      }

      float[] v7 = rgba(v6);
      int result = (int)(v7[0] * 255.0F);
      int temp1 = (int)(v7[1] * 255.0F);
      float temp2 = v7[2] * 255.0F;

      float[] v8 = Color.RGBtoHSB(result, temp1, (int)temp2, null);
      v8[1] *= 1.5F;
      v8[1] = Math.min(v8[1], 1.0F);
      return Color.HSBtoRGB(v8[0], v8[1], v8[2]);
   }

   public static int getColorRectMain(float alpha) {

      return replAlpha(getClientColor(1), alpha);
   }

   public static 

      float v1 = (float)(color >> 24 & 255) / 255.0F;
      float v2 = (float)(color >> 16 & 255) / 255.0F;

      float v3 = (float)(color >> 8 & 255) / 255.0F;
      float v4 = (float)(color & 255) / 255.0F;

      GL11.glColor4f(v2, v3, v4, v1);
      return color;
   }

   public static float[] getRGBf(int c) {
      float[] result = new float[]{redf(c), greenf(c), 0.0F};

      result[2] = bluef(c);
      return result;
   }

   public static float alphaf(int c) {
      float result = (float)alpha(c);

      return result / 255.0F;
   }

   public static int multRed(int color, float percent01) {
      int result = red(color);
      int temp1 = 255;
      float temp2 = (float)green(color);

      int v5 = Math.round(temp2 / percent01);

      return getColorRaw(result, Math.min(temp1, v5), Math.min(255, Math.round((float)blue(color) / percent01)), alpha(color));
   }

   public static int alpha(int c) {
      int result = c >> 24;

      return result & 255;
   }

   public static int getColorRaw(int red, int green, int blue, int alpha) {
      int result = MathHelper.method_15340(alpha, 0, 255) << 24
         | MathHelper.method_15340(red, 0, 255) << 16;
      int temp1 = MathHelper.method_15340(green, 0, 255);

      return result | temp1 << 8 | MathHelper.method_15340(blue, 0, 255);
   }

   public static int multDark(int color, float percent01) {
      int result = Math.round((float)red(color) * percent01);
      int temp1 = Math.round((float)green(color) * percent01);

      int v5 = Math.round((float)blue(color) * percent01);
      int temp3 = alpha(color);

      return getColorRaw(result, temp1, v5, temp3);
   }

   public static float bluef(int c) {
      return (float)blue(c) / 255.0F;
   }

   public static int getColor(int brightness, 

      return getColor(brightness, brightness, brightness, alpha);
   }

   public static float[] getRGBAf(int c) {
      float[] result = new float[]{redf(c), greenf(c), 0.0F, 0.0F};

      result[2] = bluef(c);
      result[3] = alphaf(c);
      return result;
   }

   public static int toGray(int color, float percent01) {
      int v2 = red(color);
      int v3 = green(color);
      int v4 = blue(color);
      int v5 = alpha(color);
      int v6 = 128;
      v2 = Math.round((float)v2 + (float)(v6 - v2) * percent01);
      float result = (float)v3;
      float temp1 = (float)(v6 - v3);

      v3 = Math.round(result + temp1 * percent01);
      v4 = Math.round((float)v4 + (float)(v6 - v4) * percent01);
      float v7 = percent01 / 2.0F;
      v2 = Math.round((float)v2 * v7);
      result = (float)v3;

      v3 = Math.round(result * v7);
      v4 = Math.round((float)v4 * v7);
      return getColorRaw(v2, v3, v4, v5);
   }

   public static int getColor2(int red, int green, int blue, int alpha) {
      int v4 = 0;
      int temp2 = 24;

      v4 |= alpha << temp2;

      v4 |= red << 16;
      v4 |= green << 8;

      int v11;
      return v11 = v4 | blue;
   }

   public static int interpolateColorsBackAndForth(int speed, int index, int startColor, int endColor, boolean trueColor) {
      long result = System.currentTimeMillis() / (long)speed + (long)index;

      int v5 = (int)(result % (-1737165077716205949L + 1737165077716206309L));
      int v10 = v5 >= 180 ? 360 - v5 : v5;
      int v6 = 131393042;

      v5 = v10 * 2;
      return interpolateColor(startColor, endColor, (float)v5 / 360.0F, trueColor);
   }

   private static int m1579(int color1, int color2, float ratio) {
      float[] v3 = Color.RGBtoHSB(
         color1 >> 16 & 255,
         color1 >> 8 & 255,
         color1 & 255,
         null
      );

      float[] v4 = Color.RGBtoHSB(
         color2 >> 16 & 255,
         color2 >> 8 & 255,
         color2 & 255,
         null
      );
      float result = (float)(1.0 - Math.cos((double)ratio * Math.PI)) / 2.0F;

      float v5 = result;
      float v6 = m1580(v3[0], v4[0], v5);
      float v7 = m1580(v3[1], v4[1], v5);
      float v8 = m1580(v3[2], v4[2], v5);

      return Color.HSBtoRGB(v6, v7, v8);
   }

   public static int replAlpha(int color, float alpha) {
      int result = red(color);
      int temp1 = green(color);

      return getColorRaw(result, temp1, blue(color), Math.round(alpha * 255.0F));
   }

   public static int overCol(int color1, int color2, float percent01) {
      float v3 = MathHelper.method_15363(percent01, 0.0F, 1.0F);
      int result = cq.lerp(red(color1), red(color2), (double)v3);
      int temp1 = cq.lerp(green(color1), green(color2), (double)v3);
      int temp2 = cq.lerp(blue(color1), blue(color2), (double)v3);
      Integer temp3 = alpha(color1);
      Integer temp4 = alpha(color2);
      double temp5 = (double)v3;

      return getColorRaw(result, temp1, temp2, cq.lerp(temp3, temp4, temp5));
   }

   static {
      cacheCleaner.scheduleWithFixedDelay(() -> {
         Delayed result = cleanupQueue.poll();

         up.wp v0 = (up.wp)result;

         for (; v0 != null; v0 = cleanupQueue.poll()) {
            if (v0.isExpired()) {
               colorCache.remove(v0.getKey());
            }
         }
      }, 0L, 1L, TimeUnit.SECONDS);
   }

   public static int interpolateColor(int color1, int color2, float factor) {
      factor = Math.min(1.0F, Math.max(0.0F, factor));
      int temp1 = 24;

      int v3 = color1 >> temp1 & 255;
      int v4 = color1 >> 16 & 255;
      int result = color1 >> 8;

      int v5 = result & 255;
      int v6 = color1 & 255;
      int v7 = color2 >> 24 & 255;
      int v8 = color2 >> 16 & 255;
      int v9 = color2 >> 8 & 255;
      int v10 = color2 & 255;
      int v11 = (int)((float)v3 + (float)(v7 - v3) * factor);
      float v19 = (float)v4;
      float v21 = (float)(v8 - v4);

      int v12 = (int)(v19 + v21 * factor);
      int v13 = (int)((float)v5 + (float)(v9 - v5) * factor);
      int v14 = (int)((float)v6 + (float)(v10 - v6) * factor);
      return v11 << 24 | v12 << 16 | v13 << 8 | v14;
   }

   public static int getClientColor(int index) {
      int result = eh.getHudColor();

      return result;
   }

   @Generated
   private Up() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static int getColor(int brightness, float alpha) {

      int v4 = Math.round(alpha * 255.0F);

      return getColor(brightness, v4);
   }

   public static 

      return c & 255;
   }

   public static int getRectColor(float alpha) {
      return replAlpha(wo.getBackgroundColor(), alpha * eh.getAlpha());
   }

   public static int getColor(float red, float green, float blue, float alpha) {
      int result = Math.round(red * 255.0F);
      int temp1 = Math.round(green * 255.0F);
      int temp2 = Math.round(blue * 255.0F);

      int temp3 = Math.round(alpha * 255.0F);

      return getColor(result, temp1, temp2, temp3);
   }

   public static float[] normalize(Color color) {

      float[] result = new float[]{(float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, 0.0F, 0.0F};
      float temp3 = (float)color.getBlue() / 255.0F;

      result[2] = temp3;
      result[3] = (float)color.getAlpha() / 255.0F;
      return result;
   }

   public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int v5 = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (8648222934103395860L + -8648222934103395500L));
      float v6 = (float)v5 / 360.0F;
      int v7 = Color.HSBtoRGB(v6, saturation, brightness);
      int result = red(v7);
      int temp1 = green(v7);
      int temp2 = blue(v7);

      return getColor(result, temp1, temp2, Math.round(opacity * 255.0F));
   }

   public static int interpolate(int color1, int color2, double amount) {
      amount = (double)((float)MathHelper.method_15350(amount, 0.0, 1.0));
      int result = cq.lerp(red(color1), red(color2), amount);
      int temp1 = cq.lerp(green(color1), green(color2), amount);
      Integer temp2 = blue(color1);

      int v10 = cq.lerp(temp2, blue(color2), amount);
      Integer v11 = alpha(color1);
      int temp4 = alpha(color2);

      result = getColorRaw(result, temp1, v10, cq.lerp(v11, temp4, amount));

      return result;
   }

   public static int background() {
      Theme v0 = m1576();
      Theme v1 = m1577();
      int result = v0.getRect();
      int temp1 = v1.getRect();

      return interpolate(result, temp1, (double)(1.0F - ko.animation14.getOutput()));
   }

   public static int getColor(float red, float green, float blue) {
      int result = getColor(red, green, blue, 1.0F);

      return result;
   }

   private static Theme m1577() {
      return ko.preSelectedTheme != null ? ko.preSelectedTheme : Theme.NIGHT;
   }

   public static int reAlphaInt(int color, 

      return MathHelper.method_15340(alpha, 0, 255) << 24 | color & 16777215;
   }

   public static int[] solid8(int color) {
      int[] result = new int[8];
      result[0] = color;
      result[1] = color;
      result[2] = color;

      result[3] = color;
      result[4] = color;
      result[5] = color;
      result[6] = color;
      result[7] = color;
      return result;
   }

   public static int getColorRaw(float red, float green, float blue, float alpha) {
      int result = MathHelper.method_15340(Math.round(alpha), 0, 255) << 24;
      int temp1 = MathHelper.method_15340(Math.round(red), 0, 255);
      int v4 = -453330255;

      return result
         | temp1 << 16
         | MathHelper.method_15340(Math.round(green), 0, 255) << 8
         | MathHelper.method_15340(Math.round(blue), 0, 255);
   }

   public static int fade(int speed, int index, int first, int second) {
      int v4 = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (-5263899060831928459L + 5263899060831928819L));
      v4 = v4 >= 180 ? 360 - v4 : v4;
      int v5 = -380864365;

      return overCol(first, second, (float)v4 / 180.0F);
   }

   public static int gradient(int start, int end, int index, int speed) {
      int v4 = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (-40504978881963125L + 40504978881963485L));
      v4 = (v4 > 180 ? 360 - v4 : v4) + 180;

      int v5 = interpolate(start, end, (double)MathHelper.method_15363((float)v4 / 180.0F - 1.0F, 0.0F, 1.0F));
      float[] v6 = rgba(v5);
      int v11 = (int)(v6[0] * 255.0F);

      float[] v7 = Color.RGBtoHSB(v11, (int)(v6[1] * 255.0F), (int)(v6[2] * 255.0F), null);
      v7[1] *= 1.5F;
      v7[1] = Math.min(v7[1], 1.0F);
      return Color.HSBtoRGB(v7[0], v7[1], v7[2]);
   }

   private static Theme m1576() {
      Theme v1;
      if (ko.selectedTheme != null) {

         v1 = ko.selectedTheme;
      } else {
         v1 = Theme.NIGHT;
      }

      return v1;
   }

   public static int[] solid(int color) {
      int[] result = new int[9];
      result[0] = color;
      result[1] = color;

      result[2] = color;
      result[3] = color;

      result[4] = color;
      result[5] = color;
      result[6] = color;

      result[7] = color;
      result[8] = color;
      return result;
   }

   public static int getColor(int red, 

      return getColor(red, green, blue, 255);
   }

   public static int interpolateColor2(int color1, 

      amount = MathHelper.method_15363(amount, 0.0F, 1.0F);
      Color v3 = new Color(color1, true);
      Color v4 = new Color(color2, true);
      int v5 = (int)((float)v3.getRed() + (float)(v4.getRed() - v3.getRed()) * amount);
      int v6 = (int)((float)v3.getGreen() + (float)(v4.getGreen() - v3.getGreen()) * amount);
      float result = (float)v3.getBlue() + (float)(v4.getBlue() - v3.getBlue()) * amount;

      int v7 = (int)result;
      result = (float)v3.getAlpha();
      int v15 = v4.getAlpha() - v3.getAlpha();

      int v8 = (int)(result + (float)v15 * amount);
      return new Color(v5, v6, v7, v8).getRGB();
   }

   public static int fadeBetween(int from, int to, float fraction) {
      fraction = m1578(fraction);
      int v3 = from >> 24 & 255;
      int v4 = from >> 16 & 255;
      int v5 = from >> 8 & 255;
      int v6 = from & 255;
      int v7 = to >> 24 & 255;
      int result = to >> 16;

      int v8 = result & 255;
      int v9 = to >> 8 & 255;
      int v10 = to & 255;
      int v11 = (int)((float)v3 + (float)(v7 - v3) * fraction);
      float v18 = (float)v4;
      int v19 = v8 - v4;

      int v12 = (int)(v18 + (float)v19 * fraction);
      int v13 = (int)((float)v5 + (float)(v9 - v5) * fraction);
      int v14 = (int)((float)v6 + (float)(v10 - v6) * fraction);
      return (v11 & 255) << 24
         | (v12 & 255) << 16
         | (v13 & 255) << 8
         | v14 & 255;
   }

   public static int fadeBetween(float speed, int offset, int color1, int color2) {
      long v4 = System.currentTimeMillis() + (long)offset;
      double v6 = (Math.sin((double)v4 * 0.001 * (double)speed) + 1.0) / 2.0;
      int v8 = color1 >> 16 & 255;
      int v9 = color1 >> 8 & 255;
      int v10 = color1 & 255;
      int v11 = color2 >> 16 & 255;

      int v12 = color2 >> 8 & 255;
      int v13 = color2 & 255;
      int v14 = (int)((double)v8 + (double)(v11 - v8) * v6);
      int v15 = (int)((double)v9 + (double)(v12 - v9) * v6);
      int v16 = (int)((double)v10 + (double)(v13 - v10) * v6);
      return v14 << 16 | v15 << 8 | v16;
   }

   public static int red(int c) {
      int result = c >> 16;

      return result & 255;
   }

   public static float[] rgba(int color) {
      float[] result = new float[]{(float)(color >> 16 & 255) / 255.0F, 0.0F, 0.0F, 0.0F};

      result[1] = (float)(color >> 8 & 255) / 255.0F;
      result[2] = (float)(color & 255) / 255.0F;

      result[3] = (float)(color >> 24 & 255) / 255.0F;
      return result;
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      up.xp v4 = new up.xp(red, green, blue, alpha);

      Object result = colorCache.computeIfAbsent(v4, k -> {
         up.wp var10000x = new up.wp;

         var10000x./* $VF: Unable to resugar constructor */<init>(k, m1582(red, green, blue, alpha), -5342437888248514593L + 5342437888248574593L);
         up.wp v5x = var10000x;
         cleanupQueue.offer(v5x);
         return v5x;
      });

      result = (up.wp)result;

      up.wp v5 = (up.wp)result;
      return v5.getColor();
   }

   public static int multBright(int color, float percent01) {
      int result = Math.min(255, Math.round((float)red(color) / percent01));
      int temp1 = 255;
      float temp2 = (float)green(color);

      temp1 = Math.min(temp1, Math.round(temp2 / percent01));
      int v5 = 255;
      float v6 = (float)blue(color);

      return getColorRaw(result, temp1, Math.min(v5, Math.round(v6 / percent01)), alpha(color));
   }

   public static int[] genGradientForText(int color1, int color2, int length) {
      int[] v3 = new int[length];

      for (int v4 = 0; v4 < length; v4++) {
         float v5 = (float)v4 / (float)(length - 1);
         v3[v4] = overCol(color1, color2, v5);
      }

      return v3;
   }

   public static 

      int result = 10;

      return fade(result, index, getClientColor1(index), multBright(getClientColor1(index), 0.75F));
   }

   private static int m1581(int b1, int b2, float fraction) {
      float result = (float)b1;
      float temp1 = (float)(b2 - b1) * fraction;
      int v3 = -803189254;

      int temp2 = v3 * (v3 + 1) % 2;

      if (temp2 != 0) {
         }

      return (int)(result + temp1);
   }

   public static int swapAlpha(int color, float alpha) {
      int result = color >> 16 & 255;

      int v2 = result;
      int v3 = color >> 8 & 255;
      int v4 = color & 255;
      return getColor2(v2, v3, v4, (int)alpha);
   }

   public static int gradient(int c1, int c2, int c3, int c4, int c5, int index, int speed) {
      double v7 = (double)((System.currentTimeMillis() + (long)index * (-430244798433923851L + 430244798433923901L)) % (long)speed);
      float v9 = (float)(v7 / (double)speed);
      int v10;
      int v11;
      float v12;
      if (v9 < 0.2F) {
         v10 = c1;

         v11 = c2;

         v12 = v9 / 0.2F;
      } else if (v9 < 0.4F) {
         v10 = c2;
         v11 = c3;
         v12 = (v9 - 0.2F) / 0.2F;
      } else if (v9 < 0.6F) {
         v10 = c3;
         v11 = c4;
         v12 = (v9 - 0.4F) / 0.2F;
      } else if (v9 < 0.8F) {
         v10 = c4;
         v11 = c5;
         v12 = (v9 - 0.6F) / 0.2F;
      } else {
         v10 = c5;
         v11 = c1;
         v12 = (v9 - 0.8F) / 0.2F;
      }

      return m1579(v10, v11, v12);
   }

   public static int multAlpha(int color, float percent01) {
      int result = red(color);
      int temp1 = green(color);
      int v2 = 46544770;

      int v5 = blue(color);
      float temp3 = (float)alpha(color) * percent01;

      return getColorRaw(result, temp1, v5, Math.round(temp3));
   }

   public static float redf(int c) {

      return (float)red(c) / 255.0F;
   }

   public static int skyRainbow(int speed, int index) {
      long result = System.currentTimeMillis();

      double v8 = (double)((int)((result / (long)speed + (long)index) % (-7948062359058853215L + 7948062359058853575L)));

      double v2 = v8;
      double v7;
      double v12;
      int v9 = (v12 = (v7 = v2 % 360.0) / 360.0 - 0.5) == 0.0 ? 0 : (v12 < 0.0 ? -1 : 1);

      return Color.getHSBColor(v9 < 0 ? -((float)(v7 / 360.0)) : (float)(v7 / 360.0), 0.5F, 1.0F).hashCode();
   }

   public static void shutdownCacheCleaner() {
      cacheCleaner.shutdown();
   }

   @Environment(EnvType.CLIENT)
   private static class wp implements Delayed {
      private final int color;
      private final up.xp key;
      private final long f1260;

      public boolean isExpired() {
         long v7;
         int result = (v7 = System.currentTimeMillis() - this.f1260) == 0L ? 0 : (v7 < 0L ? -1 : 1);

         boolean v4;
         if (result > 0) {
            v4 = true;
         } else {
            v4 = false;
            int v2 = 18430934;
            int v5 = v2 * (v2 + 1);

            
         }

         return v4;
      }

      @Override
      public long getDelay(TimeUnit unit) {
         long result = this.f1260;
         long temp1 = System.currentTimeMillis();

         long v2 = result - temp1;
         return unit.convert(v2, TimeUnit.MILLISECONDS);
      }

      wp(up.xp key, int color, long ttl) {
         this.key = key;
         this.color = color;
         this.f1260 = System.currentTimeMillis() + ttl;
      }

      @Generated
      public up.xp getKey() {

         return this.key;
      }

      public int compareTo(Delayed other) {
         if (other instanceof up.wp) {

            return Long.compare(this.f1260, ((up.wp)other).f1260);
         } else {
            return 0;
         }
      }

      @Generated
      public long getExpirationTime() {

         return this.f1260;
      }

      @Generated
      public int getColor() {
         return this.color;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class xp {
      final int green;
      final int alpha;
      final int red;
      final int blue;

      @Generated
      public xp(int red, int green, int blue, int alpha) {
         this.red = red;
         this.green = green;
         this.blue = blue;
         this.alpha = alpha;
      }

      @Generated
      public int getGreen() {
         int v1 = -1584063536;
         int temp1 = v1 * (v1 + 1);
         int v2 = 1341121467;

         return this.green;
      }

      @Generated
      protected boolean canEqual(Object other) {

         return other instanceof up.xp;
      }

      @Generated
      public int getBlue() {
         return this.blue;
      }

      @Generated
      public int getAlpha() {
         return this.alpha;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else {
            int result = o instanceof up.xp;

            if (!result) {
               return false;
            } else {
               up.xp v2 = (up.xp)o;
               if (!v2.canEqual(this)) {
                  return false;
               } else if (this.getRed() != v2.getRed()) {
                  return false;
               } else if (this.getGreen() != v2.getGreen()) {
                  return false;
               } else {
                  result = this.getBlue();

                  return result != v2.getBlue() ? false : this.getAlpha() == v2.getAlpha();
               }
            }
         }
      }

      @Generated
      @Override
      public int hashCode() {
         int v1 = 59;
         int v2 = 1;
         v2 = v2 * (59) + this.getRed();
         v2 = v2 * (59) + this.getGreen();
         int result = v2 * (59);

         v2 = result + this.getBlue();
         result = v2 * (59) + this.getAlpha();

         return result;
      }

      @Generated
      public 

         int v1 = 1454349906;

         return this.red;
      }

      
   }
}
