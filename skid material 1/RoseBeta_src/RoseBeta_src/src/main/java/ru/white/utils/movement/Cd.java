package ru.white.utils.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Cd {
   boolean isStatic();

   void call(Object v1);

   int getPriority();

   Class<?> getTarget();
}
