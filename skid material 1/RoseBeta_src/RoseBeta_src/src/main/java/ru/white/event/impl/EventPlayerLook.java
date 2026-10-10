package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PlayerInput;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventPlayerLook extends qc {
   private float f64;
   private float f63;
   private PlayerInput input;

   @Generated
   public float getStrafe() {

      return this.f64;
   }

   @Generated
   public void setForward(float forward) {

      this.f63 = forward;
   }

   @Generated
   public void setInput(PlayerInput input) {

      this.input = input;
   }

   @Generated
   public EventPlayerLook(PlayerInput input, float forward, float strafe) {
      this.input = input;
      this.f63 = forward;
      this.f64 = strafe;
   }

   public void setDirectionalLow(boolean forward, boolean backward, boolean left, boolean right) {
      PlayerInput temp1 = new PlayerInput(forward, backward, left, right, this.input.comp_3163(), this.input.comp_3164(), this.input.comp_3165());

      this.input = temp1;
   }

   @Generated
   public float getForward() {

      return this.f63;
   }

   @Generated
   public void setStrafe(float strafe) {
      this.f64 = strafe;
   }

   @Generated
   public PlayerInput getInput() {

      return this.input;
   }

   
}
