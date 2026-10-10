package ru.white.utils.misc;

import com.sun.jna.Callback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Wn extends Callback {
   void apply(String v1);
}
