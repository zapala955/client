package ru.white.utils.render;

import com.sun.jna.Callback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FontRegistry extends Callback {
   void apply(String v1);
}
