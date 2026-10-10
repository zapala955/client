package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;

@Environment(EnvType.CLIENT)
public class Rc {
   public String getName() {
      return this.getClass().getSimpleName().toLowerCase();
   }

   public void hook() {
      Client.eventHandler().post(this);
   }

   
}
