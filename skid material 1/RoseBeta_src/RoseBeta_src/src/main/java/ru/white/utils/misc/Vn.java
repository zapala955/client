package ru.white.utils.misc;

import com.sun.jna.Callback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Vn extends Callback {
   void apply(int v1, String v2);
}
