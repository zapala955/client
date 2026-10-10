package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventBlockBreak extends qc {
   byte type;

   @Generated
   public EventBlockBreak(byte type) {
      this.type = type;
   }

   @Generated
   public void setType(byte type) {
      this.type = type;
      
   }

   @Generated
   public byte getType() {

      return this.type;
   }

   
}
