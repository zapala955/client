package ru.white.theme;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Theme {
   public static final float[] SLIDER_MAX = new float[]{1.0F, 6.0F};
   public String name;
   public static final int OPACITY = 0;
   public final int[] colors = new int[8];
   public static final int LIGHT_BG = 7;
   public final float[] sliders = new float[2];
   public static final float[] SLIDER_DEF = new float[]{1.0F, 0.0F};
   public static final String[] NAMES;
   public static final int SHADOW = 0;
   public static final int SEPARATOR = 6;
   public static final int BLUR = 1;
   public static final int TEXT2 = 4;
   public static final int ACCENT = 0;
   public static final int BACKGROUND = 1;
   public final boolean[] booleans;
   public static final String[] SLIDER_NAMES = new String[]{
      "Прозрачность", "Сила размытия"
   };
   public boolean builtin;
   public static final int OUTLINE = 2;
   public static final float[] SLIDER_MIN = new float[]{0.1F, 0.0F};
   public static final String[] BOOL_NAMES = new String[]{"Эффект тени", "Эффект искажения"};
   public static final int ELEMENT = 5;
   public static final int DISTORTION = 1;
   public static final int TEXT = 3;
   public static final int COLOR_COUNT = 8;
   public static final float[] SLIDER_STEP = new float[]{0.0F, 1.0F};
   public static final boolean[] BOOL_DEF = new boolean[]{true, false};

   public Theme(String name) {
      this.booleans = new boolean[BOOL_NAMES.length];
      this.builtin = false;
      this.name = name;
      this.colors[0] = -1;
      this.colors[1] = -15856110;
      this.colors[2] = -1;
      this.colors[3] = -1;
      this.colors[4] = -1;
      this.colors[5] = -1;
      this.colors[6] = -1;
      this.colors[7] = -15066592;
      System.arraycopy(SLIDER_DEF, 0, this.sliders, 0, this.sliders.length);
      System.arraycopy(BOOL_DEF, 0, this.booleans, 0, this.booleans.length);
   }

   public float getSlider(int index) {

      return clampSlider(index, this.sliders[index]);
   }

   public Theme copy(String newName) {
      Theme v2 = new Theme(newName);
      System.arraycopy(this.colors, 0, v2.colors, 0, 8);
      System.arraycopy(this.sliders, 0, v2.sliders, 0, this.sliders.length);

      System.arraycopy(this.booleans, 0, v2.booleans, 0, this.booleans.length);
      return v2;
   }

   public int get(int index) {
      return this.colors[index];
   }

   public static int rgb(int r, int g, int b) {
      int result = -16777216 | (r & 255) << 16;

      return result | (g & 255) << 8 | b & 255;
   }

   public boolean getBool(int index) {

      return this.booleans[index];
   }

   public void set(int index, 

      this.colors[index] = argb;
   }

   public void setSlider(int index, float value) {
      float[] result = this.sliders;
      float temp2 = clampSlider(index, value);

      result[index] = temp2;
   }

   static {
      String[] result = new String[8];
      result[0] = "Цвет визуалов";
      result[1] = "Цвет фона";
      result[2] = "Обводка";
      result[3] = "Светлый текст";
      result[4] = "Тёмный текст";
      result[5] = "Цвет худа";
      result[6] = "Цвет разделителей";
      result[7] = "Цвет светлого фона";
      NAMES = result;
   }

   private static float clampSlider(int index, float value) {
      float v2 = SLIDER_STEP[index];
      if (v2 > 0.0F) {
         float result = (float)Math.round(value / v2) * v2;

         value = result;
      }

      if (value < SLIDER_MIN[index]) {
         return SLIDER_MIN[index];
      } else if (value > SLIDER_MAX[index]) {
         return SLIDER_MAX[index];
      } else {

         return value;
      }
   }

   public void setBool(int index, boolean value) {

      this.booleans[index] = value;
   }

   
}
