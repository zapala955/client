package ru.white.utils;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Mq {
   private long f1276;

   public long getElapsedTime() {
      long result = System.currentTimeMillis();

      return result - this.f1276;
   }

   public void reset() {
      long temp1 = System.currentTimeMillis();

      int v1 = 1513283172;

      this.f1276 = temp1;
   }

   @Generated
   public void setMillis(long millis) {
      this.f1276 = millis;
      int v3 = -733479417;

      
   }

   @Generated
   public long getMillis() {
      return this.f1276;
   }

   public Mq() {
      this.reset();
   }

   public boolean finished(long delay) {
      return System.currentTimeMillis() - delay >= this.f1276;
   }

   @Environment(EnvType.CLIENT)
   public static class nq {
      private long f1275;

      public long getMs(long time) {
         int v3 = -332761441;

         return time / (-2489585175571064833L + 2489585175572064833L);
      }

      public boolean passedMs(long ms) {
         boolean v7;
         if (this.getMs(System.nanoTime() - this.f1275) >= ms) {
            int v3 = -1977715436;

            v7 = v3 * (v3 + 1) % 2;

            if (v7 != 0) {
               }

            v7 = 1;
         } else {
            v7 = 0;
         }

         return (boolean)v7;
      }

      public long getPassedTimeMs() {
         long temp1 = System.nanoTime();

         return this.getMs(temp1 - this.f1275);
      }

      public nq() {
         this.reset();
      }

      public void reset() {

         this.f1275 = System.nanoTime();
      }

      public boolean every(long ms) {
         long temp1 = System.nanoTime();

         boolean result;
         if (this.getMs(temp1 - this.f1275) >= ms) {
            result = true;
         } else {
            result = false;
            
         }

         boolean v3 = result;
         if (v3) {

            this.reset();
         }

         return v3;
      }

      public boolean passedS(double s) {

         return this.getMs(System.nanoTime() - this.f1275) >= (long)(s * 1000.0);
      }

      public void setMs(long ms) {
         long temp1 = System.nanoTime() - ms * (-401620674328774946L + 401620674329774946L);

         this.f1275 = temp1;
      }

      public long getTimeMs() {
         long temp1 = System.nanoTime();
         int v1 = 279809233;
         int temp3 = v1 + 1;

         int temp2 = v1 * temp3;

         return this.getMs(temp1 - this.f1275);
      }

      
   }
}
