package ru.white.utils.misc;

import com.sun.jna.Callback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.discord.Cn;

@Environment(EnvType.CLIENT)
public interface Xn extends Callback {
   void apply(cn v1);
}
