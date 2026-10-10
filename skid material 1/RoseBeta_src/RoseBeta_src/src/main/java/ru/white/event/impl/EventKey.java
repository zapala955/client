package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PlayerInput;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventKey extends qc {
   private PlayerInput input;

   @Generated
   public PlayerInput getInput() {

      return this.input;
   }

   @Generated
   public void setInput(PlayerInput input) {
      this.input = input;
      
   }

   public float sideways() {
      return this.input.comp_3161() ? 1.0F : (this.input.comp_3162() ? -1.0F : 0.0F);
   }

   public int forward() {
      byte result;
      if (this.input.comp_3159()) {
         result = 1;
      } else if (this.input.comp_3160()) {

         result = -1;
      } else {
         result = 0;
      }

      return result;
   }

   @Generated
   public EventKey(PlayerInput input) {
      this.input = input;
   }

   public void inputNone() {
      int temp2 = v1 + 1;

      this.input = new PlayerInput(false, false, false, false, false, false, false);
   }

   public void setDirectional(boolean forward, boolean backward, boolean left, boolean right) {

      this.input = new PlayerInput(forward, backward, left, right, this.input.comp_3163(), this.input.comp_3164(), this.input.comp_3165());
   }

   public void setJumping(boolean jump) {
      PlayerInput temp1 = new PlayerInput;
      boolean temp3 = this.input.comp_3159();
      boolean temp4 = this.input.comp_3160();
      boolean temp5 = this.input.comp_3161();
      boolean var10006 = this.input.comp_3162();

      temp1./* $VF: Unable to resugar constructor */<init>(temp3, temp4, temp5, var10006, jump, this.input.comp_3164(), this.input.comp_3165());
      this.input = temp1;

      
   }

   
}
