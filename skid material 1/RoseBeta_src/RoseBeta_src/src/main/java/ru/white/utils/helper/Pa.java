package ru.white.utils.helper;

import java.util.EnumMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Pa {
   EnumMap<oa, float[]> rose$getEmotePose();

   void rose$setEmotePose(EnumMap<oa, float[]> v1);
}
