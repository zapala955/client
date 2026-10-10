package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventSendPacket extends rc {
   private Vec3d f60;

   @Generated
   public Vec3d getPos() {
      int temp2 = v1 + 1;

      return this.f60;
   }

   @Generated
   public void setPos(Vec3d pos) {

      this.f60 = pos;
   }

   @Generated
   public EventSendPacket(Vec3d pos) {
      this.f60 = pos;
   }

   
}
