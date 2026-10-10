package ru.white.utils.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface zo {
   double ease(double v1);
}
