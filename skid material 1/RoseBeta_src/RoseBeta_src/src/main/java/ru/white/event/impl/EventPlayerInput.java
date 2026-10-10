package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventPlayerInput extends qc {
   public double yaw;
   public double pitch;

   public EventPlayerInput(double yaw, double pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public double getPitch() {
      return this.pitch;
   }

   public void setYaw(double yaw) {

      this.yaw = yaw;
   }

   public double getYaw() {

      return this.yaw;
   }

   public void setPitch(double pitch) {

      this.pitch = pitch;
   }

   
}
