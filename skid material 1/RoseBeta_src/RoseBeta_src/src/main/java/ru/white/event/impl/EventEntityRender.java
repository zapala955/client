package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventEntityRender extends rc {
   private final Hand f98;
   private final Arm f99;
   private final MatrixStack f97;

   public Hand getHand() {

      int v4 = v1 * (v1 + 1);

      return this.f98;
   }

   public boolean isRightHand() {
      boolean v4;
      if (this.f99 == Arm.field_6183) {

         v4 = true;
      } else {
         v4 = false;
      }

      return v4;
   }

   public Arm getArm() {
      return this.f99;
   }

   public EventEntityRender(MatrixStack matrix, Hand hand, Arm arm) {
      this.f97 = matrix;
      this.f98 = hand;
      this.f99 = arm;
   }

   public MatrixStack getMatrix() {
      return this.f97;
   }

   
}
