package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;

@Environment(EnvType.CLIENT)
public interface Ip extends IMinecraft {
   Window mw = mc.method_22683();
}
