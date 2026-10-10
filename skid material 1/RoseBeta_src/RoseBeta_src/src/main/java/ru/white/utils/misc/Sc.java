package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;

@Environment(EnvType.CLIENT)
public abstract class Sc {
   public Sc() {
      Client.eventHandler().subscribe(this);
   }

   
}
