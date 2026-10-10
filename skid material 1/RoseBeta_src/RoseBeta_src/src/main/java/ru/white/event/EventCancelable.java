package ru.white.event;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventCancelable extends rc {
   private boolean f58;

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof EventCancelable v2)) {
         return false;
      } else if (!v2.canEqual(this)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else if (this.isSprintState() != v2.isSprintState()) {
         return false;
      } else {

         return true;
      }
   }

   @Generated
   public EventCancelable(boolean sprintState) {
      this.f58 = sprintState;
   }

   @Generated
   public void setSprintState(boolean sprintState) {

      this.f58 = sprintState;
   }

   @Generated
   @Override
   public int hashCode() {

      int v2 = super.hashCode();
      int result = v2 * (59);
      if (this.isSprintState()) {

         temp1 = 79;
      } else {
         temp1 = 97;
      }

      return result + temp1;
   }

   @Generated
   protected boolean canEqual(Object other) {
      boolean result = other instanceof EventCancelable;

      return result;
   }

   @Generated
   @Override
   public String toString() {
      boolean v1 = this.isSprintState();
      StringBuilder result = new StringBuilder();

      return result.append("ActionEvent(sprintState=").append(v1).append(")").toString();
   }

   @Generated
   public boolean isSprintState() {

      return this.f58;
   }

   
}
