package ru.white.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class EventException extends RuntimeException {
   public EventException(Class<?> klass) {
      String v2 = klass.getName();
      super("No registered lambda listener for '" + v2 + "'.");
   }

   
}
