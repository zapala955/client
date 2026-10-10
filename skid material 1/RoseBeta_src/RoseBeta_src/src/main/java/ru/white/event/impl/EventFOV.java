package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventFOV extends qc {
   private float f102;

   @Generated
   public void setAnimation(float animation) {
      this.f102 = animation;

      
   }

   @Generated
   public float getAnimation() {
      return this.f102;
   }

   
}
