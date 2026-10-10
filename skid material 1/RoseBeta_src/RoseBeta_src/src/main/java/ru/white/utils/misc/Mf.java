package ru.white.utils.misc;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Mf {
   Setting<?> setVisible(Supplier<Boolean> v1);
}
