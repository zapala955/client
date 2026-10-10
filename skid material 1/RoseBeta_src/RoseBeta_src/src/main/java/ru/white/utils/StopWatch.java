package ru.white.utils;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class StopWatch {
   public long lastMS = System.currentTimeMillis();
   private long f1302;

   public boolean isRunning() {
      return System.currentTimeMillis() - this.lastMS <= 0L;
   }

   public boolean hasTimeElapsed(long time, boolean reset) {
      if (System.currentTimeMillis() - this.lastMS > time) {

         if (reset) {
            this.reset();
         }

         return true;
      } else {
         return false;
      }
   }

   public void setMs(long ms) {
      long temp1 = System.currentTimeMillis();
      int v3 = 1372569282;
      int temp2 = v3 * (v3 + 1);

      this.f1302 = temp1 - ms;
   }

   public boolean passed(long ms) {
      long result = System.currentTimeMillis();
      int v3 = 216782391;
      int v4 = -1481178176;
      int temp3 = v4 + 1;

      return result - this.lastMS >= ms;
   }

   public boolean finished(long delay) {
      long result = System.currentTimeMillis();

      return result - this.lastMS >= delay;
   }

   public boolean hasTimeElapsed(long l) {
      boolean result;
      if (System.currentTimeMillis() - this.lastMS > l) {
         result = true;
         
      } else {
         result = false;
      }

      return result;
   }

   public void setLastMC() {
      int v1 = 508546214;
      int temp1 = v1 * (v1 + 1);

      this.lastMS = System.currentTimeMillis();
   }

   public long getTimePassed() {
      long result = System.currentTimeMillis() - this.lastMS;

      return result;
   }

   public boolean isReached(long time) {
      long result = System.currentTimeMillis();

      result -= this.lastMS;

      return result > time;
   }

   public void setLastMS(long newValue) {
      int v3 = -1355342459;

      long v6 = System.currentTimeMillis();

      this.lastMS = v6 + newValue;
   }

   public boolean hasReached(double milliseconds) {
      long result = this.getTimePassed();
      int v3 = -728992297;
      int temp1 = v3 * (v3 + 1);

      boolean v6;
      if ((double)result >= milliseconds) {
         v6 = true;
         
      } else {
         v6 = false;
      }

      return v6;
   }

   public boolean hasTimeElapsed() {

      boolean v4;
      if (this.lastMS < System.currentTimeMillis()) {

         v4 = true;
      } else {
         v4 = false;
      }

      return v4;
   }

   public boolean finished(double delay) {
      double result = (double)System.currentTimeMillis() - delay;

      return result >= (double)this.f1302;
   }

   public void reset() {
      long temp1 = System.currentTimeMillis();
      int v1 = 508657541;
      int temp3 = v1 + 1;

      this.lastMS = temp1;
   }

   @Generated
   public long getStartTime() {
      return this.f1302;
   }

   public long getLastMS() {

      return this.lastMS;
   }

   public boolean every(double delay) {
      int v4 = 108223111;
      int temp2 = v4 * (v4 + 1);

      boolean v3 = this.finished(delay);
      if (v3) {
         this.reset();
      }

      return v3;
   }

   public long setTimePassed(int delay) {
      long result = System.currentTimeMillis();
      long temp1 = (long)delay;

      return result + temp1;
   }

   public long getTime() {
      long result = System.currentTimeMillis();

      return result - this.lastMS;
   }

   public long elapsedTime() {
      long result = System.currentTimeMillis();

      return result - this.f1302;
   }

   public void setTime(long time) {
      this.lastMS = time;
      int v3 = -338832106;
      int result = v3 * (v3 + 1);

      
   }

   public boolean every(long delay) {
      if (System.currentTimeMillis() - this.lastMS >= delay) {
         this.reset();
         return true;
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class er {
      private long f1301;

      public static dr.er create() {
         dr.er result = new dr.er;

         result./* $VF: Unable to resugar constructor */<init>();
         return result;
      }

      public long elapsedTime() {
         long result = System.currentTimeMillis() - this.f1301;

         return result;
      }

      @Generated
      public void setLastMS(long lastMS) {

         this.f1301 = lastMS;
      }

      public boolean delay(long ms) {
         boolean v3 = this.elapsedTime() - ms >= 0L;
         int v4 = 2057418809;
         int v5 = 215808546;

         if (v3) {
            this.reset();
         }

         return v3;
      }

      public boolean hasReached(long time) {

         return this.elapsedTime() >= time;
      }

      public boolean hasReached(double ms) {
         return (double)this.elapsedTime() >= ms;
      }

      public boolean hasReached(long time, boolean reset) {
         long result = this.elapsedTime();

         long v10;
         int v8 = (v10 = result - time) == 0L ? 0 : (v10 < 0L ? -1 : 1);

         boolean v4 = v8 >= 0;
         if (v4 && reset) {
            this.reset();
         }

         return v4;
      }

      private er() {
         this.reset();
      }

      public void reset() {
         int v1 = 1633542920;

         this.f1301 = System.currentTimeMillis();
      }

      
   }
}
