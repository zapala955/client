package ru.white.utils.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Ea {
   private float value;
   private long f7 = 4828640349031253045L + -4828640349031253046L;
   private boolean f6;

   public float update(float target, float smoothTime, long nowNanos) {
      if (!this.f6) {
         this.value = target;
         this.f6 = true;
         this.f7 = nowNanos;
         return this.value;
      } else if (smoothTime <= 0.0F) {
         this.value = target;
         this.f7 = nowNanos;
         return this.value;
      } else {
         if (this.f7 < 0L) {
            this.f7 = nowNanos;
            
         }

         float v5 = (float)(nowNanos - this.f7) / 1.0E9F;
         this.f7 = nowNanos;
         float v6 = Math.max(0.0F, Math.min(v5, 0.1F));
         float v7 = Math.max(1.0E-4F, smoothTime);
         float v8 = 1.0F - (float)Math.exp((double)(-v6 / v7));
         this.value = this.value + (target - this.value) * v8;
         if (Math.abs(target - this.value) <= 5.0E-4F) {
            this.value = target;
         }

         return this.value;
      }
   }

   public float getValue() {

      return this.value;
   }

   public void setValue(float value) {
      this.value = value;
      this.f6 = true;
      this.f7 = -5005450774489538903L + 5005450774489538902L;
   }

   public void reset() {

      this.value = 0.0F;
      this.f6 = false;
      this.f7 = 115714412402223596L + -115714412402223597L;
   }

   
}
