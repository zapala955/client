package ru.white.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class BlockUtil {
   private long f1273 = System.currentTimeMillis();

   public void reset() {
      this.f1273 = System.currentTimeMillis();
   }

   public boolean hasTimePassed(long ms) {
      return System.currentTimeMillis() - this.f1273 >= ms;
   }

   public long getPassedTime() {
      long result = System.currentTimeMillis() - this.f1273;

      return result;
   }

   
}
