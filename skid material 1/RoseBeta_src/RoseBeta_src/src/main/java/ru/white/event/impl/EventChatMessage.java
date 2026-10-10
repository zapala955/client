package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventChatMessage extends rc {
   private final int f94;
   private final Screen f95;
   private final double f93;
   private final double f92;

   @Generated
   public int getButton() {
      return this.f94;
   }

   @Generated
   public double getMouseY() {

      return this.f93;
   }

   public EventChatMessage(double mouseX, double mouseY, int button, Screen screen) {
      this.f92 = mouseX;
      this.f93 = mouseY;
      this.f94 = button;
      this.f95 = screen;
   }

   @Generated
   public double getMouseX() {

      return this.f92;
   }

   @Generated
   public Screen getScreen() {
      int temp2 = v1 + 1;

      return this.f95;
   }

   
}
