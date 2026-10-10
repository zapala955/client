package ru.white.utils;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class EntityUtil {
   public long lastMS = System.currentTimeMillis();

   public EntityUtil() {
      this.reset();
   }

   public boolean isReached(long time) {
      return System.currentTimeMillis() - this.lastMS > time;
   }

   public boolean hasTimeElapsed(long time, boolean reset) {
      boolean v4 = System.currentTimeMillis() - this.lastMS >= time;

      if (v4) {

         if (reset) {
            this.reset();
         }
      }

      return v4;
   }

   public void setTime(long time) {
      int v4 = -388126641;
      int temp1 = v4 * (v4 + 1);

      this.lastMS = time;
   }

   public long getTime() {
      long result = System.currentTimeMillis() - this.lastMS;
      int v1 = 256172415;

      return result;
   }

   public boolean hasTimeElapsed(long l) {
      long result = System.currentTimeMillis();
      int v3 = 1884704746;
      int temp1 = v3 * (v3 + 1) % 2;

      if (temp1 != 0) {
         }

      return result - this.lastMS > l;
   }

   public void setLastMS(long newValue) {
      long temp1 = System.currentTimeMillis() + newValue;

      this.lastMS = temp1;
   }

   public boolean hasTimeElapsed() {
      return this.lastMS < System.currentTimeMillis();
   }

   public boolean isRunning() {
      long result = System.currentTimeMillis();

      return result - this.lastMS <= 0L;
   }

   public void reset() {
      this.lastMS = System.currentTimeMillis();
      
   }

   @Generated
   public long getLastMS() {

      return this.lastMS;
   }

   
}
