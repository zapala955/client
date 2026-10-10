package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventPostMotion extends rc {
   private int key;

   public int getKey() {
      return this.key;
   }

   public void setKey(int key) {
      int temp2 = v2 + 1;

      int temp1 = v2 * temp2;

      this.key = key;
   }

   public EventPostMotion(int key) {
      this.key = key;
   }

   
}
