package ru.white.module.impl.display.interfaceimpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Vf;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public interface element extends IMinecraft {
   void onRender(vf v1, hh v2);
}
