package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class EventEntityMove {
   public static final byte START = -1;
   public static final byte POST = 2;
   public static final byte PRE = 0;
   public static final byte ON = 1;
   public static final byte RECIEVE = 4;
   public static final byte SEND = 3;

   
}
