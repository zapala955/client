package ru.white.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class bp {
   private long f1250 = -6512740745302051088L + 6512740745302051087L;

   public boolean hasReached(boolean active, double delay) {
      if (!active) {

         if (!this.hasReached(delay)) {
            return false;
         }
      }

      return true;
   }

   public long getLastMS() {

      return this.f1250;
   }

   public void setTime(long time) {
      this.f1250 = time;
      int v3 = -1028122360;
      int temp1 = v3 + 1;

      
   }

   public long getTimePassed() {
      long result = System.currentTimeMillis();

      return result - this.f1250;
   }

   public long getCurrentTime() {
      long result = System.nanoTime();

      long v5 = 2750742948098365528L + -2750742948097365528L;

      return result / v5;
   }

   public boolean hasReached(double delay) {
      long result = System.currentTimeMillis();

      result -= this.f1250;

      return (double)result >= delay;
   }

   public void reset() {
      int v1 = 1029703478;
      int temp1 = v1 * (v1 + 1);

      this.f1250 = System.currentTimeMillis();
   }

   public bp() {
      this.f1250 = System.currentTimeMillis();
   }

   
}
