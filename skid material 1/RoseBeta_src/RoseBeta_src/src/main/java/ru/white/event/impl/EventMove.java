package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventMove extends qc {
   private double yaw;
   private double y;
   private double z;
   private boolean f85;
   private double pitch;
   private double x;

   @Generated
   public double pitch() {

      return this.pitch;
   }

   @Generated
   public double yaw() {
      return this.yaw;
   }

   @Generated
   public EventMove y(double y) {

      this.y = y;
      return this;
   }

   @Generated
   public double x() {
      return this.x;
   }

   @Generated
   public EventMove yaw(double yaw) {

      this.yaw = yaw;

      return this;
   }

   @Generated
   public EventMove pitch(double pitch) {

      this.pitch = pitch;
      return this;
   }

   @Generated
   public EventMove ground(boolean ground) {

      this.f85 = ground;
      return this;
   }

   @Generated
   public EventMove(double x, double y, double z, double yaw, double pitch, boolean ground) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.f85 = ground;
   }

   @Generated
   public double y() {
      return this.y;
   }

   @Generated
   public double z() {
      return this.z;
   }

   @Generated
   public EventMove z(double z) {
      this.z = z;

      return this;
   }

   @Generated
   public boolean ground() {
      return this.f85;
   }

   @Generated
   public EventMove x(double x) {
      this.x = x;

      return this;
   }

   
}
