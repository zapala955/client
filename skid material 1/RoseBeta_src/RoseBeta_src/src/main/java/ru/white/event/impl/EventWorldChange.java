package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventWorldChange extends rc {
   private Vec3d f96;

   @Generated
   protected boolean canEqual(Object other) {
      boolean result = other instanceof EventWorldChange;

      return result;
   }

   @Generated
   public void setMovement(Vec3d movement) {

      this.f96 = movement;
   }

   @Generated
   @Override
   public int hashCode() {
      byte v2 = 1;
      Vec3d result = this.getMovement();

      Vec3d v3 = result;
      return v2 * (59) + (v3 == null ? 43 : v3.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      String result = String.valueOf(this.getMovement());

      String v1 = result;
      StringBuilder v4 = new StringBuilder().append("MoveEvent(movement=").append((Object)v1);

      return v4.append(")").toString();
   }

   @Generated
   public EventWorldChange(Vec3d movement) {
      this.f96 = movement;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof EventWorldChange v2)) {
         return false;
      } else if (!v2.canEqual(this)) {
         return false;
      } else {
         Vec3d v3 = this.getMovement();
         Vec3d v4 = v2.getMovement();
         if (v3 == null) {
            if (v4 != null) {
               return false;
            }
         } else {
            boolean result = v3.equals(v4);

            if (!result) {
               return false;
            }
         }

         return true;
      }
   }

   @Generated
   public Vec3d getMovement() {
      return this.f96;
   }

   
}
