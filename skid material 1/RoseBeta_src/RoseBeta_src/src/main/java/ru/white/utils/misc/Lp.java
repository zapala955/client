package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class Lp implements IMinecraft {
   public static float getGCDValue() {
      float result = (float)((double)getGCD() * 0.15);

      return result;
   }

   public static int toMouseDelta(float deltaAngle) {
      float v1 = getGCDValue();
      if (v1 <= 1.0E-6F) {
         return 0;
      } else {

         return Math.round(deltaAngle / v1);
      }
   }

   public static float getSensitivity(float rot) {

      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCD() {
      double v0;
      try {
         v0 = (Double)mc.field_1690.method_42495().method_41753();
      } catch (Exception v6) {
         v0 = 0.5;
      }

      double v2 = v0 * 0.6F + 0.2F;
      return (float)(v2 * v2 * v2 * 8.0);
   }

   public static float fromMouseDelta(int mouseDelta) {
      float result = (float)mouseDelta;
      float temp1 = getGCDValue();

      return result * temp1;
   }

   public static float getDeltaMouse(float delta) {
      float v1 = getGCDValue();

      return v1 <= 1.0E-6F ? 0.0F : (float)Math.round(delta / v1);
   }

   public static float applyGCD(float targetRotation, float currentRotation) {
      float v2 = getGCDValue();
      if (v2 <= 1.0E-6F) {
         return targetRotation;
      } else {
         float v3 = targetRotation - currentRotation;

         float v4 = (float)Math.round(v3 / v2) * v2;

         return currentRotation + v4;
      }
   }

   
}
