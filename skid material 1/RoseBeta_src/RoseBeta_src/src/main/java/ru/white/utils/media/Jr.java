package ru.white.utils.media;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public interface Jr {
   void setSpeed(float v1);

   default void resetSpeed() {
      this.setSpeed(1.0F);
      int v1 = -1200704428;

      
   }

   float getSpeed();
}
