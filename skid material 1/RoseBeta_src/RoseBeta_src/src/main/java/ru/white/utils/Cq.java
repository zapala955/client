package ru.white.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Cq {
   public static <T extends Number> T lerp(T input, T target, double step) {
      double v4 = input.doubleValue();
      double v6 = target.doubleValue();
      double temp1 = step * (v6 - v4);

      double v8 = v4 + temp1;
      if (input instanceof Integer) {
         long result = Math.round(v8);

         return (T)(int)result;
      } else if (input instanceof Double) {
         return (T)v8;
      } else if (input instanceof Float) {
         return (T)(float)v8;
      } else if (input instanceof Long) {
         return (T)Math.round(v8);
      } else if (input instanceof Short) {
         return (T)(short)((int)Math.round(v8));
      } else if (input instanceof Byte) {
         return (T)(byte)((int)Math.round(v8));
      } else {
         String v10 = input.getClass().getSimpleName();
         throw new IllegalArgumentException("Unsupported type: " + v10);
      }
   }

   
}
