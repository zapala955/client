package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventJump extends rc {
   private double f90;
   private final int f86;
   private final int f87;
   private double f91;
   private final Screen f89;
   private final int f88;

   public int getAction() {
      return this.f87;
   }

   public 

      return this.f88;
   }

   public 

      return this.f86;
   }

   public double getMouseX() {
      return this.f90;
   }

   public Screen getScreen() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.f89;
   }

   public int getButton() {
      return this.f86;
   }

   public double getMouseY() {
      return this.f91;
   }

   public EventJump(int button, int action, int modifiers, Screen screen, double mouseX, double mouseY) {
      this.f86 = button;
      this.f87 = action;
      this.f88 = modifiers;
      this.f89 = screen;
      this.f90 = mouseX;
      this.f91 = mouseY;
   }

   
}
