package ru.white.event.impl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Qc;

@Environment(EnvType.CLIENT)
public class EventSound extends qc {
   private Screen f100;
   private int f101;

   @Generated
   public EventSound(Screen screen, int windowId) {
      this.f100 = screen;
      this.f101 = windowId;
   }

   @Generated
   public int getWindowId() {
      return this.f101;
   }

   @Generated
   public Screen getScreen() {

      return this.f100;
   }

   
}
