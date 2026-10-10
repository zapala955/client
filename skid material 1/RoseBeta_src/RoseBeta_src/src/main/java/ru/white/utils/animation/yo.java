package ru.white.utils.animation;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public enum yo {
   CUBIC_OUT(ap.CUBIC_OUT),
   QUART_OUT(ap.QUART_OUT),
   QUINT_OUT(ap.QUINT_OUT),
   BACK_OUT(ap.BACK_OUT),
   QUAD_OUT(ap.QUAD_OUT),
   ELASTIC_OUT(ap.ELASTIC_OUT),
   CIRC_OUT(ap.CIRC_OUT),
   SINE_OUT(ap.SINE_OUT),
   EXPO_OUT(ap.EXPO_OUT),
   LINEAR(ap.LINEAR),
   BOUNCE_OUT(ap.BOUNCE_OUT);
   private final zo easing;

   @Generated
   public zo getEasing() {

      return this.easing;
   }

   @Generated
   private yo(final zo easing) {
      this.easing = easing;
   }

   @Override
   public String toString() {
      String v1 = this.name().toLowerCase();
      String[] v2 = v1.split("_");

      StringBuilder v3 = new StringBuilder();

      for (String v7 : v2) {
         int v8 = 272547638;
         int v11 = v8 * (v8 + 1);

         v3.append(Character.toUpperCase(v7.charAt(0))).append(v7.substring(1)).append(" ");
      }

      return v3.toString().trim();
   }
}
