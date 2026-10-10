package ru.white.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class EventBus {
   public static final int HIGH = 250;
   public static final int LOW = -250;
   public static final int LOWEST = -500;
   public static final int HIGHEST = 500;
   public static final int MEDIUM = 0;

   
}
