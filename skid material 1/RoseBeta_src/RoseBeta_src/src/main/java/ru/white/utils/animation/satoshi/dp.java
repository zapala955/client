package ru.white.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class dp extends Animation {
   public dp(int ms, double endPoint) {
      super(ms, endPoint);
   }

   @Override
   protected double getEquation(double x1) {
      double v3 = x1 / (double)this.duration;
      double temp2 = Math.pow(v3 - 1.0, 3.0);

      double result = 1.0 + 2.70158 * temp2;
      temp2 = v3 - 1.0;

      return result + 1.70158 * Math.pow(temp2, 2.0);
   }

   public dp(int ms, double endPoint, cp direction) {
      super(ms, endPoint, direction);
   }

   
}
