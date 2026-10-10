package ru.white.utils.helper;

import java.lang.runtime.ObjectMethods;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class La {
   private static final float TICKS_PER_SECOND = 20.0F;
   private final Map<UUID, la.ma> active = new ConcurrentHashMap<>();
   public static final la INSTANCE = new la();

   public void stop(UUID entity) {
      if (entity != null) {

         this.active.remove(entity);
      }
   }

   public EnumMap<oa, float[]> poseFor(UUID entity) {
      if (entity == null) {
         return null;
      } else {
         la.ma v2 = this.active.get(entity);
         if (v2 == null) {

            return null;
         } else {
            ha v3 = v2.animation();
            float v4 = (float)(System.nanoTime() - v2.startNanos()) / 1.0E9F * 20.0F;
            float v5 = v3.beginTick() + v4;
            if (v5 >= v3.stopTick()) {
               if (v3.loop() && v3.endTick() > v3.beginTick()) {
                  float v6 = v3.endTick() - v3.beginTick();
                  float v7 = v3.beginTick() + (v5 - v3.beginTick()) % v6;
                  return v3.sample(v7);
               } else {
                  this.active.remove(entity);
                  return null;
               }
            } else {
               return v3.sample(v5);
            }
         }
      }
   }

   public boolean isPlaying(UUID entity) {

      return entity != null && this.active.containsKey(entity);
   }

   public void play(UUID entity, ka definition) {
      if (entity != null && definition != null) {
         Map result = this.active;
         la.ma temp2 = new la.ma;
         ha temp4 = definition.animation();

         long v5 = System.nanoTime();

         temp2./* $VF: Unable to resugar constructor */<init>(temp4, v5);
         result.put(entity, temp2);
      }
   }

   private La() {
   }

   @Environment(EnvType.CLIENT)
   private static record ma(ha animation, long startNanos) {
      private final long f17;
      private final ha f16;

      private ma(ha animation, long startNanos) {
         this.f16 = animation;
         this.f17 = startNanos;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",la.ma,"animation;startNanos",la.ma::f16,la.ma::f17>(this);
         int v1 = -1518371522;
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return result;
      }

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",la.ma,"animation;startNanos",la.ma::f16,la.ma::f17>(this);
      }

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",la.ma,"animation;startNanos",la.ma::f16,la.ma::f17>(this, o);

         return result;
      }

      public ha animation() {
         int v1 = -1455560140;
         int temp2 = v1 + 1;

         return this.f16;
      }

      public long startNanos() {

         return this.f17;
      }

      
   }
}
