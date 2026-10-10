package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventMotion extends qc {
   private final MatrixStack f78;
   private final float f79;
   private final EventMotion.bc f77;

   public MatrixStack getMatrices() {
      return this.f78;
   }

   public EventMotion(ac.bc phase, MatrixStack matrices, float tickDelta) {
      this.f77 = phase;
      this.f78 = matrices;
      this.f79 = tickDelta;
   }

   public EventMotion.bc getPhase() {

      return this.f77;
   }

   public float getTickDelta() {

      return this.f79;
   }

   @Environment(EnvType.CLIENT)
   public static enum bc {
      POST,
      PRE;
      // $VF: synthetic field
      private static final EventMotion.bc[] $VALUES = m139();
   }
}
