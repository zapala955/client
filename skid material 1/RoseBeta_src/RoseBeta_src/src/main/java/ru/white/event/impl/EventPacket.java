package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventPacket extends qc {
   private float f83;
   private float f82;
   private MatrixStack f80;
   private Hand f81;
   private Arm f84;

   @Generated
   public Arm getArm() {
      return this.f84;
   }

   @Generated
   public void setMatrices(MatrixStack matrices) {
      this.f80 = matrices;
      int result = v2 * (v2 + 1) % 2;

      if (result != 0) {
         }
   }

   @Generated
   public EventPacket(MatrixStack matrices, Hand hand, float swingProgress, float equipProgress, Arm arm) {
      this.f80 = matrices;
      this.f81 = hand;
      this.f82 = swingProgress;
      this.f83 = equipProgress;
      this.f84 = arm;
   }

   @Generated
   public void setHand(Hand hand) {
      this.f81 = hand;
   }

   @Generated
   public Hand getHand() {
      return this.f81;
   }

   @Generated
   public MatrixStack getMatrices() {

      return this.f80;
   }

   @Generated
   public float getSwingProgress() {

      return this.f82;
   }

   @Generated
   public void setEquipProgress(float equipProgress) {

      this.f83 = equipProgress;
   }

   @Generated
   public void setArm(Arm arm) {

      this.f84 = arm;
   }

   @Generated
   public void setSwingProgress(float swingProgress) {
      this.f82 = swingProgress;
      
   }

   @Generated
   public float getEquipProgress() {

      return this.f83;
   }

   
}
