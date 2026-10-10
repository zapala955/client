package ru.white.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum cp {
   BACKWARDS,
   FORWARDS;
   // $VF: synthetic field
   private static final cp[] $VALUES = m1514();

   public cp opposite() {

      return this == FORWARDS ? BACKWARDS : FORWARDS;
   }
}
