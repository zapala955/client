package ru.white.utils.animation;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class ap {
   public static final zo CIRC_OUT = value -> {
      double temp1 = Math.pow(value - 1.0, 2.0);

      return Math.sqrt(1.0 - temp1);
   };
   public static final zo ELASTIC_IN = value -> {
      double result;
      if (value != 0.0 && value != 1.0) {
         result = Math.pow(-2.0, 10.0 * value - 10.0);
         double temp1 = value * 10.0 - 10.75;

         result *= Math.sin(temp1 * (Math.PI * 2.0 / 3.0));
      } else {
         result = value;
      }

      return result;
   };
   public static final zo SINE_IN = value -> 1.0 - Math.cos(value * Math.PI / 2.0);
   public static final double c3 = 2.70158;
   public static final zo EXPO_IN = value -> {
      double result;
      if (value != 0.0) {
         double temp1 = 10.0 * value;

         result = Math.pow(2.0, temp1 - 10.0);
      } else {
         result = value;
      }

      return result;
   };
   public static final zo QUINT_IN = powIn(5);
   public static final zo CUBIC_OUT = powOut(3);
   public static final zo EXPO_OUT = value -> {

      double result;
      if (value != 1.0) {

         result = 1.0 - Math.pow(2.0, -10.0 * value);
      } else {
         result = value;
      }

      return result;
   };
   public static final zo BACK_OUT = value -> {
      double result = 1.0 + 2.70158 * Math.pow(value - 1.0, 3.0);

      return result + 1.70158 * Math.pow(value - 1.0, 2.0);
   };
   public static final zo QUAD_IN_OUT = powIN_OUT(2.0);
   public static final zo SINE_OUT = value -> {
      double result = value * Math.PI;

      return Math.sin(result / 2.0);
   };
   public static final double c5 = Math.PI * 4.0 / 9.0;
   public static final zo QUINT_IN_OUT = powIN_OUT(5.0);
   public static final zo LINEAR = value -> {

      return value;
   };
   public static final zo CUBIC_IN_OUT = powIN_OUT(3.0);
   public static final zo QUART_OUT = powOut(4);
   public static final zo QUINT_OUT = powOut(5);
   public static final zo BACK_IN = value -> {
      int v2 = -1257414025;

      return 2.70158 * Math.pow(value, 3.0) - 1.70158 * Math.pow(value, 2.0);
   };
   public static final zo CUBIC_IN = powIn(3);
   public static final zo EXPO_IN_OUT = value -> {
      double v4;
      int result = (v4 = value - 0.0) == 0.0 ? 0 : (v4 < 0.0 ? -1 : 1);

      if (result) {

         if (value != 1.0) {
            return value < 0.5 ? Math.pow(2.0, 20.0 * value - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * value + 10.0)) / 2.0;
         }
      }

      return value;
   };
   public static final zo CIRC_IN_OUT = value -> {
      double v3;
      int result = (v3 = value - 0.5) == 0.0 ? 0 : (v3 < 0.0 ? -1 : 1);

      return result < 0 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * value, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * value + 2.0, 2.0)) + 1.0) / 2.0;
   };
   public static final zo QUAD_IN = powIn(2);
   public static final double c4 = Math.PI * 2.0 / 3.0;
   public static final zo BOUNCE_OUT = x -> {
      double v2 = 7.5625;
      double v4 = 2.75;
      if (x < 1.0 / v4) {
         return v2 * Math.pow(x, 2.0);
      } else if (x < 2.0 / v4) {
         double result = v2 * Math.pow(x - 1.5 / v4, 2.0);

         return result + 0.75;
      } else {

         return x < 2.5 / v4 ? v2 * Math.pow(x - 2.25 / v4, 2.0) + 0.9375 : v2 * Math.pow(x - 2.625 / v4, 2.0) + 0.984375;
      }
   };
   public static final zo QUART_IN_OUT = powIN_OUT(4.0);
   public static final double c2 = 2.5949095;
   public static final zo BACK_OUT_SMOOTH = value -> {

      double result = 1.0 + 2.70158 * Math.pow(value - 1.0, 3.0);
      double temp2 = value - 1.0;

      return result + 1.70158 * Math.pow(temp2, 2.0);
   };
   public static final zo BOUNCE_IN = value -> {
      int v2 = 521414056;

      return 1.0 - BOUNCE_OUT.ease(1.0 - value);
   };
   public static final zo QUART_IN = powIn(4);
   public static final zo BACK_IN_OUT = value -> {
      double v4;
      if (value < 0.5) {
         v4 = Math.pow(2.0 * value, 2.0) * (7.189819 * value - 2.5949095);

         v4 /= 2.0;
      } else {
         v4 = (Math.pow(2.0 * value - 2.0, 2.0) * (3.5949095 * (value * 2.0 - 2.0) + 2.5949095) + 2.0) / 2.0;
      }

      return v4;
   };
   public static final zo ELASTIC_IN_OUT = value -> {
      if (value != 0.0 && value != 1.0) {
         double result;
         if (value < 0.5) {
            int v2 = 1757458338;

            result = -(Math.pow(2.0, 20.0 * value - 10.0) * Math.sin((20.0 * value - 11.125) * (Math.PI * 4.0 / 9.0))) / 2.0;
         } else {
            result = Math.pow(2.0, -20.0 * value + 10.0) * Math.sin((20.0 * value - 11.125) * (Math.PI * 4.0 / 9.0)) / 2.0 + 1.0;
         }

         return result;
      } else {
         return value;
      }
   };
   public static final zo SINE_IN_OUT = value -> {
      double result = -(Math.cos(Math.PI * value) - 1.0);
      int v2 = 770878884;

      return result / 2.0;
   };
   public static final double c1 = 1.70158;
   public static final zo QUAD_OUT = powOut(2);
   public static final zo BOUNCE_IN_OUT = value -> {
      double v5;
      if (value < 0.5) {

         v5 = 1.0 - BOUNCE_OUT.ease(1.0 - 2.0 * value);

         v5 /= 2.0;
      } else {
         v5 = (1.0 + BOUNCE_OUT.ease(2.0 * value - 1.0)) / 2.0;
      }

      return v5;
   };
   public static final zo CIRC_IN = value -> {
      double temp1 = 1.0 - Math.pow(value, 2.0);

      return 1.0 - Math.sqrt(temp1);
   };
   public static final zo ELASTIC_OUT = value -> {
      double result;
      if (value != 0.0 && value != 1.0) {
         result = Math.pow(2.0, -10.0 * value);
         double temp1 = Math.sin((value * 10.0 - 0.75) * (Math.PI * 2.0 / 3.0));

         result *= temp1;

         result += 1.0;
      } else {
         result = value;
      }

      return result;
   };

   public static zo powIn(double n) {

      return value -> Math.pow(value, n);
   }

   @Generated
   private ap() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static zo powOut(double n) {
      int v2 = -1575132228;
      int temp2 = v2 + 1;

      return value -> {

         return 1.0 - Math.pow(1.0 - value, n);
      };
   }

   public static zo powIn(int n) {

      return powIn((double)n);
   }

   public static zo powIN_OUT(double n) {
      return value -> {
         double v7;
         if (value < 0.5) {

            v7 = Math.pow(2.0, n - 1.0) * Math.pow(value, n);
         } else {
            v7 = 1.0 - Math.pow(-2.0 * value + 2.0, n) / 2.0;
         }

         return v7;
      };
   }

   public static zo powOut(int n) {
      zo result = powOut((double)n);

      return result;
   }

   
}
