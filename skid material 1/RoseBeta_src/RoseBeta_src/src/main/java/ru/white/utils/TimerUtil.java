package ru.white.utils;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class TimerUtil {
   private long f1274;
   public long lastMS = System.currentTimeMillis();

   public boolean hasTimeElapsed(long l) {
      long result = System.currentTimeMillis();

      boolean v5;
      if (result - this.lastMS > l) {
         v5 = true;
      } else {
         v5 = false;
         
      }

      return v5;
   }

   public void setLastMS(long newValue) {
      long temp1 = System.currentTimeMillis();
      int v3 = 352992572;
      int temp3 = v3 * (v3 + 1);

      this.lastMS = temp1 + newValue;
   }

   public void reset() {
      this.lastMS = System.currentTimeMillis();
   }

   public long getTime() {
      long result = System.currentTimeMillis();
      int v1 = -836396558;
      int temp3 = v1 + 1;

      return result - this.lastMS;
   }

   @Generated
   public long getStartTime() {
      return this.f1274;
   }

   public TimerUtil() {
      this.reset();
   }

   public boolean hasTimeElapsed(long time, boolean reset) {
      long v6;
      int result = (v6 = System.currentTimeMillis() - this.lastMS - time) == 0L ? 0 : (v6 < 0L ? -1 : 1);

      boolean v4 = result >= 0;
      if (v4 && reset) {
         this.reset();
      }

      return v4;
   }

   public boolean isReached(long time) {
      boolean result;
      if (System.currentTimeMillis() - this.lastMS > time) {
         result = true;
         
      } else {
         result = false;
         int v3 = -1967671598;

         
      }

      return result;
   }

   public boolean hasTimeElapsed() {
      long v5;
      int result = (v5 = this.lastMS - System.currentTimeMillis()) == 0L ? 0 : (v5 < 0L ? -1 : 1);

      boolean v3;
      if (result < 0) {
         v3 = true;
      } else {
         v3 = false;
         
      }

      return v3;
   }

   @Generated
   public long getLastMS() {

      return this.lastMS;
   }

   public void setTime(long time) {

      this.lastMS = time;
   }

   public boolean finished(double delay) {
      double result = (double)System.currentTimeMillis();

      return result - delay >= (double)this.f1274;
   }

   public boolean isRunning() {
      long v2;
      int result = (v2 = System.currentTimeMillis() - this.lastMS - 0L) == 0L ? 0 : (v2 < 0L ? -1 : 1);

      return result <= 0;
   }

   
}
