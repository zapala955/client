package ru.white.utils.misc;

import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Sp {
   private static final HashMap<String, String> FORMATS = new HashMap<>();
   public static final Pattern PATTERN = Pattern.compile(
      "\\$\\{(rgba|rgb)\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})(?:,(\\d{1,3}))?\\)}|\\$\\{reset}", 2
   );

   public static String replaceColor(String text, int red, int green, int blue) {
      Matcher result = PATTERN.matcher(text);

      return result.replaceAll(Matcher.quoteReplacement(getColor(red, green, blue)));
   }

   public static String get(String input) {
      if (input != null) {
         boolean result = input.isEmpty();

         if (!result) {
            int v4 = 108575744;

            if (input.indexOf("${") < 0) {
               return input;
            }

            String v1 = input;

            for (Entry v3 : FORMATS.entrySet()) {
               v1 = v1.replace((CharSequence)v3.getKey(), (CharSequence)v3.getValue());
            }

            return v1;
         }
      }

      return input;
   }

   public static String getColor(int red, int green, int blue, int alpha) {
      StringBuilder result = new StringBuilder().append("${rgba(").append(red).append(",");

      result = result.append(green).append(",").append(blue);

      return result.append(",").append(alpha).append(")}").toString();
   }

   public static int overColor(int firstColor, int secondColor, float factorPercent) {

      int result = up.overCol(firstColor, secondColor, factorPercent);

      return result;
   }

   static {
      FORMATS.put("${black}", Formatting.field_1074.toString());
      FORMATS.put("${dark_blue}", Formatting.field_1058.toString());
      FORMATS.put("${dark_green}", Formatting.field_1077.toString());
      FORMATS.put("${dark_aqua}", Formatting.field_1062.toString());
      FORMATS.put("${dark_red}", Formatting.field_1079.toString());
      FORMATS.put("${dark_purple}", Formatting.field_1064.toString());
      FORMATS.put("${orange}", Formatting.field_1065.toString());
      FORMATS.put("${gray}", Formatting.field_1080.toString());
      FORMATS.put("${dark_gray}", Formatting.field_1063.toString());
      FORMATS.put("${blue}", Formatting.field_1078.toString());
      FORMATS.put("${green}", Formatting.field_1060.toString());
      FORMATS.put("${aqua}", Formatting.field_1075.toString());
      FORMATS.put("${red}", Formatting.field_1061.toString());
      FORMATS.put("${purple}", Formatting.field_1076.toString());
      FORMATS.put("${yellow}", Formatting.field_1054.toString());
      FORMATS.put("${white}", Formatting.field_1068.toString());
      FORMATS.put("${bold}", Formatting.field_1067.toString());
      FORMATS.put("${reset}", Formatting.field_1070.toString());
   }

   public static String typeRGBA() {
      return "rgba";
   }

   public static String stripForWidth(String text) {
      if (text != null) {

         if (!text.isEmpty()) {
            return removeFormatting(get(text));
         }
      }

      return text;
   }

   public static String getColor(int color) {
      int result = up.red(color);
      int temp1 = up.green(color);
      int temp2 = up.blue(color);

      return getColor(result, temp1, temp2, up.alpha(color));
   }

   public static String reset() {

      return "${reset}";
   }

   public static String typeRGB() {

      return "rgb";
   }

   public static Text gradient(String message) {
      MutableText v1 = Text.method_43473();

      int v2 = 0;

      while (true) {
         int v6 = message.length();

         if (v2 >= v6) {
            return v1;
         }

         int v3 = up.fade(v2);
         v1.method_10852(Text.method_43470(String.valueOf(message.charAt(v2))).method_27694(style -> {

            return style.method_36139(v3);
         }));
         v2++;
      }
   }

   public static String removeFormatting(String text) {
      if (text != null && !text.isEmpty()) {
         int result = text.indexOf("${");

         return result < 0 ? text : PATTERN.matcher(text).replaceAll("");
      } else {
         return text;
      }
   }

   private static int m1574(int value) {
      int v1 = -723896598;

      return Math.max(0, Math.min(255, value));
   }

   public static String replaceColor(String text, int red, int green, int blue, int alpha) {
      Matcher result = PATTERN.matcher(text);
      String temp1 = Matcher.quoteReplacement(getColor(red, green, blue, alpha));
      int v5 = 1286664974;

      return result.replaceAll(temp1);
   }

   public static String getColor(int red, int green, int blue) {
      StringBuilder result = new StringBuilder().append("${rgb(").append(red).append(",");

      return result.append(green).append(",").append(blue).append(")}").toString();
   }

   @Generated
   private Sp() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static sp.tp parseTag(String text, int index, int defaultColor) {
      if (text == null || index < 0 || index >= text.length() - 1) {
         return null;
      } else if (text.charAt(index) == 36 && text.charAt(index + 1) == 123) {
         Matcher v3 = PATTERN.matcher(text);
         if (v3.find(index) && v3.start() == index) {
            String result = v3.group();
            String temp1 = reset();

            if (result.equalsIgnoreCase(temp1)) {
               return new sp.tp(defaultColor, v3.end() - index);
            } else {
               String v4 = v3.group(1).toLowerCase();
               int v5 = m1574(Integer.parseInt(v3.group(2)));
               int v6 = m1574(Integer.parseInt(v3.group(3)));

               int v7 = m1574(Integer.parseInt(v3.group(4)));
               int v8 = v4.equals(typeRGBA()) && v3.group(5) != null ? m1574(Integer.parseInt(v3.group(5))) : up.alpha(defaultColor);
               return new sp.tp(up.getColor(v5, v6, v7, v8), v3.end() - index);
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record tp(int color, int length) {
      private final int f1259;

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",sp.tp,"color;length",sp.tp::color,sp.tp::f1259>(this);

         return result;
      }

      public int length() {
         int v1 = -2073543913;

         return this.f1259;
      }

      public tp(int color, int length) {
         this.color = color;
         this.f1259 = length;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",sp.tp,"color;length",sp.tp::color,sp.tp::f1259>(this);

         return result;
      }

      public int color() {
         int v1 = 1740207068;

         return this.color;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",sp.tp,"color;length",sp.tp::color,sp.tp::f1259>(this, o);
      }

      
   }
}
