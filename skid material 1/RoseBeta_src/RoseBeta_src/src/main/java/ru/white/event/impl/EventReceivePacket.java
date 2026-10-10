package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventReceivePacket extends rc {
   private float f62;
   private DrawContext f61;

   @Generated
   public DrawContext getDrawContext() {
      return this.f61;
   }

   @Generated
   public EventReceivePacket(DrawContext drawContext, float partialTicks) {
      this.f61 = drawContext;
      this.f62 = partialTicks;
   }

   @Generated
   public float getPartialTicks() {
      return this.f62;
   }

   
}
