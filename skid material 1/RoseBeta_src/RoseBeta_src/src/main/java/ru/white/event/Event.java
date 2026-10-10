package ru.white.event;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class Event extends qc {
   private Entity f59;

   @Generated
   public Event(Entity target) {
      this.f59 = target;
   }

   @Generated
   public void setTarget(Entity target) {
      this.f59 = target;

      
   }

   @Generated
   public Entity getTarget() {

      return this.f59;
   }

   
}
