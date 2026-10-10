package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventRaytrace extends rc {
   public float pitch;
   public float partialTicks;
   public float yaw;

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPartialTicks() {

      return this.partialTicks;
   }

   public void setPitch(float pitch) {

      this.pitch = pitch;
   }

   public void setPartialTicks(float partialTicks) {

      this.partialTicks = partialTicks;
   }

   public float getYaw() {

      int v4 = v1 * (v1 + 1);

      return this.yaw;
   }

   public EventRaytrace(float yaw, float pitch, float partialTicks) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.partialTicks = partialTicks;
   }

   public float getPitch() {
      return this.pitch;
   }

   
}
