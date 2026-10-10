package ru.white.utils.misc;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Op {
   private long f1254;

   @Generated
   public long getStartTime() {

      return this.f1254;
   }

   public void setMs(long ms) {
      long temp1 = System.currentTimeMillis();

      this.f1254 = temp1 - ms;
   }

   public boolean finished(double delay) {
      double v9;
      int result = (v9 = (double)System.currentTimeMillis() - delay - (double)this.f1254) == 0.0 ? 0 : (v9 < 0.0 ? -1 : 1);

      boolean v6;
      if (result >= 0) {
         v6 = true;
         
      } else {
         v6 = false;
      }

      return v6;
   }

   public boolean every(double delay) {
      boolean result = this.finished(delay);
      int v5 = 1822924037;
      int temp1 = v5 * (v5 + 1);

      boolean v3 = result;
      if (v3) {

         this.reset();
      }

      return v3;
   }

   public void reset() {

      long v3 = System.currentTimeMillis();

      this.f1254 = v3;
   }

   public long elapsedTime() {
      return System.currentTimeMillis() - this.f1254;
   }

   public Op() {
      this.reset();
   }

   
}
