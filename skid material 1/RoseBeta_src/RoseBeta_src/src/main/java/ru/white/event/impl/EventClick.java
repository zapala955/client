package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventClick extends rc {
   private int f71;
   private DrawContext f69;
   private HandledScreen<?> f70;
   private int f72;

   @Generated
   public DrawContext getDrawContext() {

      return this.f69;
   }

   @Generated
   public int getMouseX() {
      return this.f71;
   }

   @Generated
   public 

      return this.f72;
   }

   @Generated
   public HandledScreen<?> getScreen() {

      return this.f70;
   }

   @Generated
   public EventClick(DrawContext drawContext, HandledScreen<?> screen, int mouseX, int mouseY) {
      this.f69 = drawContext;
      this.f70 = screen;
      this.f71 = mouseX;
      this.f72 = mouseY;
   }

   
}
