package ru.white.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class ep extends Animation {
   public ep(int ms, double endPoint, cp direction) {
      super(ms, endPoint, direction);
   }

   @Override
   protected double getEquation(double x1) {
      double v3 = x1 / (double)this.duration;

      double result;
      if (v3 < 0.5) {
         result = 2.0 * Math.pow(v3, 2.0);
      } else {

         result = 1.0 - Math.pow(-2.0 * v3 + 2.0, 2.0) / 2.0;
      }

      return result;
   }

   public ep(int ms, double endPoint) {
      super(ms, endPoint);
   }

   
}
