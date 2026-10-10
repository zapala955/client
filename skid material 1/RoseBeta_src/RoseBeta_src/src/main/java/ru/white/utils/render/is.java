package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface is {
   boolean isOnGround();

   void setOnGround(boolean v1);
}
