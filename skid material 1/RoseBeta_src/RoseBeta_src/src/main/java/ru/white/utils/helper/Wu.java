package ru.white.utils.helper;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Wu {
   private long f1691;

   public long elapsedTime() {
      long result = System.currentTimeMillis();
      int v1 = -1719773609;
      int temp2 = v1 * (v1 + 1);

      return result - this.f1691;
   }

   @Generated
   public long getStartTime() {

      int v1 = -747700014;

      return this.f1691;
   }

   public boolean finished(double delay) {
      double result = (double)System.currentTimeMillis() - delay;
      double temp1 = (double)this.f1691;
      int v4 = -1828836623;

      double v8;
      int v6 = (v8 = result - temp1) == 0.0 ? 0 : (v8 < 0.0 ? -1 : 1);

      return v6 >= 0;
   }

   public Wu() {
      this.reset();
   }

   public void reset() {
      this.f1691 = System.currentTimeMillis();
      int v1 = 1918840978;

      int result = v1 * (v1 + 1);

      
   }

   public void setMs(long ms) {
      int v3 = -346127422;
      int temp1 = v3 * (v3 + 1) % 2;

      if (temp1 != 0) {

         }

      this.f1691 = System.currentTimeMillis() - ms;
   }

   public boolean every(double delay) {
      boolean v3 = this.finished(delay);
      if (v3) {

         this.reset();
      }

      return v3;
   }

   
}
