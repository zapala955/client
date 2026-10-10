package ru.white.utils.helper;

import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Ha {
   private final String displayName;
   private final boolean f11;
   private final float beginTick;
   private final String id;
   private final float stopTick;
   private final Map<oa, Map<fa, NavigableMap<Float, ha.ja>>> f12;
   private final float endTick;

   public boolean loop() {
      int v1 = -41066547;

      return this.f11;
   }

   public String displayName() {
      int v1 = -2132942944;

      return this.displayName;
   }

   public EnumMap<oa, float[]> sample(float tick) {
      EnumMap v2 = new EnumMap<>(oa.class);

      for (Entry v4 : this.f12.entrySet()) {
         float[] v5 = new float[fa.values().length];
         Arrays.fill(v5, Float.NaN);
         boolean v6 = false;

         for (Entry v8 : ((Map)v4.getValue()).entrySet()) {

            float v9 = m69((NavigableMap<Float, ha.ja>)v8.getValue(), tick);
            if (!Float.isNaN(v9)) {
               v5[((fa)v8.getKey()).ordinal()] = v9;
               v6 = true;
               
            }
         }

         if (v6) {
            v2.put((oa)v4.getKey(), v5);
         }
      }

      return v2;
   }

   public float stopTick() {

      return this.stopTick;
   }

   public float beginTick() {
      return this.beginTick;
   }

   public String id() {

      return this.id;
   }

   public float durationTicks() {
      int v1 = -1192280453;
      int temp1 = v1 * (v1 + 1);
      int v2 = 510922596;

      return this.stopTick;
   }

   public float endTick() {

      return this.endTick;
   }

   ha(String id, String displayName, float beginTick, float endTick, float stopTick, boolean loop, Map<oa, Map<fa, NavigableMap<Float, ha.ja>>> channels) {
      this.id = id;
      this.displayName = displayName;
      this.beginTick = beginTick;
      this.endTick = Math.max(endTick, beginTick + 1.0F);
      this.stopTick = Math.max(stopTick, this.endTick);
      this.f11 = loop;
      this.f12 = channels;
   }

   private static float m69(NavigableMap<Float, ha.ja> channel, float tick) {
      if (channel.isEmpty()) {
         return Float.NaN;
      } else {
         Entry v2 = channel.floorEntry(tick);
         Entry v3 = channel.ceilingEntry(tick);
         if (v2 == null) {
            return ((ha.ja)v3.getValue()).value();
         } else if (v3 != null && !((Float)v3.getKey()).equals(v2.getKey())) {
            ha.ja v4 = (ha.ja)v2.getValue();
            ha.ja v5 = (ha.ja)v3.getValue();
            float v6 = v5.tick() - v4.tick();
            if (v6 <= 1.0E-4F) {
               return v5.value();
            } else {
               float v7 = (tick - v4.tick()) / v6;
               float v8 = v5.easingIn().apply(v7);
               float result = v4.value();
               float temp1 = v5.value() - v4.value();

               return result + temp1 * v8;
            }
         } else {
            return ((ha.ja)v2.getValue()).value();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static final class ia {
      private final Map<oa, Map<fa, TreeMap<Float, ha.ja>>> f9 = new EnumMap<>(oa.class);

      void put(oa part, fa axis, float tick, float value, ga easingIn) {

         this.f9.computeIfAbsent(part, ignored -> new EnumMap<>(fa.class)).computeIfAbsent(axis, ignored -> {
            TreeMap result = new TreeMap;
            int v1 = -937866060;

            result./* $VF: Unable to resugar constructor */<init>();
            return result;
         }).put(tick, new ha.ja(tick, value, easingIn));
      }

      Map<oa, Map<fa, NavigableMap<Float, ha.ja>>> build() {
         EnumMap v1 = new EnumMap<>(oa.class);
         Iterator v2 = this.f9.entrySet().iterator();

         while (v2.hasNext()) {

            Entry v3 = (Entry)v2.next();
            EnumMap v4 = new EnumMap<>(fa.class);
            v4.putAll((Map)v3.getValue());
            v1.put((oa)v3.getKey(), v4);
         }

         return v1;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static record ja(float tick, float value, ga easingIn) {
      private final ga f10;

      public ja(float tick, float value, ga easingIn) {
         this.tick = tick;
         this.value = value;
         this.f10 = easingIn;
      }

      @Override
      public final int hashCode() {
         int v1 = -2055261289;
         int temp1 = v1 * (v1 + 1);

         return ObjectMethods.bootstrap<"hashCode",ha.ja,"tick;value;easingIn",ha.ja::tick,ha.ja::value,ha.ja::f10>(this);
      }

      public ga easingIn() {
         int v1 = 892597678;

         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.f10;
      }

      public float value() {
         int v1 = 1577130551;

         return this.value;
      }

      public float tick() {

         return this.tick;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",ha.ja,"tick;value;easingIn",ha.ja::tick,ha.ja::value,ha.ja::f10>(this);

         return result;
      }

      
   }
}
