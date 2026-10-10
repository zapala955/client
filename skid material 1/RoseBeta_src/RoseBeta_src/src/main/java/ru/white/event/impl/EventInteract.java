package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventInteract extends qc {
   private float yaw;
   private float f103;
   private float pitch;

   @Generated
   public float getPitch() {

      return this.pitch;
   }

   @Generated
   public void setBodyaw(float bodyaw) {
      this.f103 = bodyaw;
      
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {

      this.pitch = pitch;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public EventInteract(float yaw, float pitch, float bodyaw) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.f103 = bodyaw;
   }

   @Generated
   public float getBodyaw() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.f103;
   }

   
}
