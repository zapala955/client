package ru.white.utils.helper;

import java.util.Locale;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public enum Ga {
   EASE_IN_QUART(t -> {
      float result = t * t;
      float temp1 = t;

      return result * temp1 * t;
   }),
   EASE_IN_QUAD(t -> {
      Float result = t * t;

      return result;
   }),
   EASE_OUT_QUAD(t -> {
      float temp2 = t;

      return 1.0F - (1.0F - temp2) * (1.0F - t);
   }),
   EASE_IN_OUT_EXPO(t -> {
      if (t <= 0.0F) {
         return 0.0F;
      } else if (t >= 1.0F) {
         return 1.0F;
      } else {
         float result;
         if (t < 0.5F) {
            double temp1 = 20.0 * (double)t.floatValue();
            int v1 = -1080126859;

            result = (float)Math.pow(2.0, temp1 - 10.0) / 2.0F;
         } else {
            result = (2.0F - (float)Math.pow(2.0, -20.0 * (double)t.floatValue() + 10.0)) / 2.0F;
         }

         return result;
      }
   }),
   EASE_IN_OUT_CUBIC(t -> {

      float v3;
      if (t < 0.5F) {
         v3 = 4.0F * t;

         v3 = v3 * t * t;
      } else {
         v3 = 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 3.0) / 2.0F;
      }

      return v3;
   }),
   EASE_IN_OUT_CIRC(t -> {
      float result;
      if (t < 0.5F) {
         result = (1.0F - (float)Math.sqrt(1.0 - Math.pow(2.0 * (double)t.floatValue(), 2.0))) / 2.0F;
      } else {
         double temp1 = Math.pow(-2.0 * (double)t.floatValue() + 2.0, 2.0);

         result = ((float)Math.sqrt(1.0 - temp1) + 1.0F) / 2.0F;
      }

      return result;
   }),
   EASE_OUT_SINE(t -> {
      double result = (double)t.floatValue();

      return (float)Math.sin(result * Math.PI / 2.0);
   }),
   EASE_IN_CIRC(t -> {
      double temp2 = Math.pow((double)t.floatValue(), 2.0);

      return 1.0F - (float)Math.sqrt(1.0 - temp2);
   }),
   EASE_IN_OUT_SINE(t -> {
      float temp1 = t;

      double result = Math.PI * (double)temp1;

      return -((float)Math.cos(result) - 1.0F) / 2.0F;
   }),
   LINEAR(t -> {

      return t;
   }),
   EASE_OUT_CUBIC(t -> {

      float result = 1.0F - (float)Math.pow((double)(1.0F - t), 3.0);
      int v2 = -987947114;

      return result;
   }),
   EASE_OUT_CIRC(t -> {
      double temp1 = (double)t.floatValue() - 1.0;
      int v1 = 2130755597;
      int temp2 = v1 * (v1 + 1);

      Float result = (float)Math.sqrt(1.0 - Math.pow(temp1, 2.0));

      return result;
   }),
   EASE_IN_EXPO(t -> {
      float v5;
      int result = (v5 = t - 0.0F) == 0.0F ? 0 : (v5 < 0.0F ? -1 : 1);

      float v3;
      if (result <= 0) {
         v3 = 0.0F;
      } else {
         double v4 = 10.0 * (double)t.floatValue();

         v3 = (float)Math.pow(2.0, v4 - 10.0);
      }

      return v3;
   }),
   EASE_OUT_EXPO(t -> {
      float result;
      if (t >= 1.0F) {
         result = 1.0F;
      } else {
         double temp2 = -10.0 * (double)t.floatValue();

         result = 1.0F - (float)Math.pow(2.0, temp2);
      }

      return result;
   }),
   EASE_IN_SINE(t -> {
      float temp1 = t;

      return 1.0F - (float)Math.cos((double)temp1 * Math.PI / 2.0);
   }),
   EASE_IN_CUBIC(t -> {
      float result = t;
      float temp1 = t;

      return result * temp1 * t;
   }),
   EASE_IN_OUT_QUAD(t -> {
      float v4;
      if (t < 0.5F) {

         v4 = 2.0F * t * t;
      } else {

         float temp1 = (float)Math.pow((double)(-2.0F * t + 2.0F), 2.0);

         v4 = 1.0F - temp1 / 2.0F;
      }

      return v4;
   }),
   CONSTANT(t -> 0.0F),
   EASE_IN_OUT_QUART(t -> {
      float result;
      if (t < 0.5F) {
         result = 8.0F * t * t * t * t;
      } else {
         double temp1 = (double)(-2.0F * t + 2.0F);
         int v1 = -1849339513;
         int temp3 = v1 + 1;

         result = 1.0F - (float)Math.pow(temp1, 4.0) / 2.0F;
      }

      return result;
   }),
   EASE_OUT_QUART(t -> {
      float temp2 = t;

      return 1.0F - (float)Math.pow((double)(1.0F - temp2), 4.0);
   });
   // $VF: synthetic field
   private static final ga[] $VALUES = m66();
   private final Function<Float, Float> f8;

   public float apply(float t) {
      float result;
      if (t < 0.0F) {
         result = 0.0F;
      } else if (t > 1.0F) {
         int v3 = -1581690118;
         int temp1 = v3 + 1;

         result = 1.0F;
      } else {
         result = t;
      }

      float v2 = result;
      return this.f8.apply(v2);
   }

   private Ga(Function<Float, Float> curve) {
      this.f8 = curve;
   }

   public static ga parse(String raw) {
      if (raw != null && !raw.isBlank()) {
         String v1 = raw.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
         int v3 = -1;
         switch (v1.hashCode()) {
            case -1708368049:
               if (v1.equals("easeinoutcubic")) {
                  v3 = 14;
               }
               break;
            case -1695439420:
               if (v1.equals("easeinoutquart")) {
                  v3 = 26;
               }
               break;
            case -1565596233:
               if (v1.equals("easeoutcirc")) {
                  v3 = 36;
               }
               break;
            case -1565522286:
               if (v1.equals("easeoutexpo")) {
                  v3 = 30;
               }
               break;
            case -1565168153:
               if (v1.equals("easeoutquad")) {
                  v3 = 6;
               }
               break;
            case -1565119699:
               if (v1.equals("easeoutsine")) {
                  v3 = 18;
               }
               break;
            case -1288500566:
               if (v1.equals("easeoutcubic")) {
                  v3 = 12;
               }
               break;
            case -1275571937:
               if (v1.equals("easeoutquart")) {
                  v3 = 24;
               }
               break;
            case -1184265284:
               if (v1.equals("incirc")) {

                  v3 = 33;
               }
               break;
            case -1184191337:
               if (v1.equals("inexpo")) {
                  v3 = 27;
               }
               break;
            case -1183837204:
               if (v1.equals("inquad")) {
                  v3 = 3;
               }
               break;
            case -1183788750:
               if (v1.equals("insine")) {
                  v3 = 15;
               }
               break;
            case -1106513563:
               if (v1.equals("outcirc")) {
                  v3 = 35;
               }
               break;
            case -1106439616:
               if (v1.equals("outexpo")) {
                  v3 = 29;
               }
               break;
            case -1106085483:
               if (v1.equals("outquad")) {
                  v3 = 5;
               }
               break;
            case -1106037029:
               if (v1.equals("outsine")) {
                  v3 = 17;
               }
               break;
            case -1102672091:
               if (v1.equals("linear")) {
                  v3 = 0;
               }
               break;
            case -783432406:
               if (v1.equals("easeincirc")) {
                  v3 = 34;
               }
               break;
            case -783358459:
               if (v1.equals("easeinexpo")) {
                  v3 = 28;
               }
               break;
            case -783004326:
               if (v1.equals("easeinquad")) {
                  v3 = 4;
               }
               break;
            case -782955872:
               if (v1.equals("easeinsine")) {
                  v3 = 16;
               }
               break;
            case -567811164:
               if (v1.equals("constant")) {
                  v3 = 1;
                  
               }
               break;
            case -352416543:
               if (v1.equals("inoutcubic")) {
                  v3 = 13;
               }
               break;
            case -339487914:
               if (v1.equals("inoutquart")) {
                  v3 = 25;
               }
               break;
            case 3540684:
               if (v1.equals("step")) {
                  v3 = 2;
                  
               }
               break;
            case 58160316:
               if (v1.equals("outcubic")) {
                  v3 = 11;
               }
               break;
            case 71088945:
               if (v1.equals("outquart")) {
                  v3 = 23;
               }
               break;
            case 819904672:
               if (v1.equals("inoutcirc")) {
                  v3 = 37;
               }
               break;
            case 819978619:
               if (v1.equals("inoutexpo")) {
                  v3 = 31;
               }
               break;
            case 820332752:
               if (v1.equals("inoutquad")) {
                  v3 = 7;
               }
               break;
            case 820381206:
               if (v1.equals("inoutsine")) {
                  v3 = 19;
               }
               break;
            case 1483741591:
               if (v1.equals("easeincubic")) {
                  v3 = 10;
               }
               break;
            case 1496670220:
               if (v1.equals("easeinquart")) {
                  v3 = 22;
               }
               break;
            case 1942824261:
               if (v1.equals("incubic")) {
                  v3 = 9;
               }
               break;
            case 1955752890:
               if (v1.equals("inquart")) {
                  v3 = 21;
               }
               break;
            case 2023090290:
               if (v1.equals("easeinoutcirc")) {
                  v3 = 38;
               }
               break;
            case 2023164237:
               if (v1.equals("easeinoutexpo")) {
                  v3 = 32;
               }
               break;
            case 2023518370:
               if (v1.equals("easeinoutquad")) {
                  v3 = 8;
               }
               break;
            case 2023566824:
               if (v1.equals("easeinoutsine")) {
                  v3 = 20;
               }
         }
         return switch (v3) {
            case 0 -> LINEAR;
            case 1, 2 -> CONSTANT;
            case 3, 4 -> EASE_IN_QUAD;
            case 5, 6 -> EASE_OUT_QUAD;
            case 7, 8 -> EASE_IN_OUT_QUAD;
            case 9, 10 -> EASE_IN_CUBIC;
            case 11, 12 -> EASE_OUT_CUBIC;
            case 13, 14 -> EASE_IN_OUT_CUBIC;
            case 15, 16 -> EASE_IN_SINE;
            case 17, 18 -> EASE_OUT_SINE;
            case 19, 20 -> EASE_IN_OUT_SINE;
            case 21, 22 -> EASE_IN_QUART;
            case 23, 24 -> EASE_OUT_QUART;
            case 25, 26 -> EASE_IN_OUT_QUART;
            case 27, 28 -> EASE_IN_EXPO;
            case 29, 30 -> EASE_OUT_EXPO;
            case 31, 32 -> EASE_IN_OUT_EXPO;
            case 33, 34 -> EASE_IN_CIRC;
            case 35, 36 -> EASE_OUT_CIRC;
            case 37, 38 -> EASE_IN_OUT_CIRC;
            default -> LINEAR;
         };
      } else {
         return LINEAR;
      }
   }
}
