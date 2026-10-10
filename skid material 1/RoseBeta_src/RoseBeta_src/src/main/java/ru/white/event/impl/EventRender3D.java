package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventRender3D extends qc {
   private int f74;
   private boolean f75;
   private boolean f76;

   @Generated
   public void setModifySensitivity(boolean modifySensitivity) {

      this.f76 = modifySensitivity;
      
   }

   @Generated
   public void setFov(int fov) {
      int temp2 = v2 * (v2 + 1) % 2;

      if (temp2 != 0) {
         }

      this.f74 = fov;
   }

   @Generated
   public boolean isModifySensitivity() {

      return this.f76;
   }

   @Generated
   public void setOverrideSprint(boolean overrideSprint) {

      this.f75 = overrideSprint;
   }

   @Generated
   public boolean isOverrideSprint() {
      return this.f75;
   }

   @Generated
   public 

      return this.f74;
   }

   
}
