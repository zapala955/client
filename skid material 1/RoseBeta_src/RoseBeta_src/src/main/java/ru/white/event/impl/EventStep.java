package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventStep extends qc {
   private EventStep.rb type;
   private Packet<?> f65;

   @Generated
   public void setPacket(Packet<?> packet) {
      this.f65 = packet;
   }

   @Generated
   public EventStep(Packet<?> packet, qb.rb type) {
      this.f65 = packet;
      this.type = type;
   }

   @Generated
   public Packet<?> getPacket() {

      return this.f65;
   }

   @Generated
   public void setType(qb.rb type) {

      this.type = type;
   }

   public boolean isSend() {

      boolean result = this.type.equals(qb.rb.SEND);

      return result;
   }

   @Generated
   public EventStep.rb getType() {

      int v4 = v1 * (v1 + 1);

      return this.type;
   }

   @Environment(EnvType.CLIENT)
   public static enum rb {
      SEND,
      RECEIVE;
      // $VF: synthetic field
      private static final EventStep.rb[] $VALUES = m138();
   }
}
