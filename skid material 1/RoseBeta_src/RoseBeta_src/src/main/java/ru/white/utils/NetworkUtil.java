package ru.white.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class NetworkUtil {
   public static float noise(float x) {
      int result = MathHelper.method_15375(x);

      int v1 = result;
      int v2 = v1 + 1;
      float v3 = x - (float)v1;
      float v4 = v3 * v3 * (3.0F - 2.0F * v3);
      return MathHelper.method_16439(v4, m1598(v1), m1598(v2));
   }

   private NetworkUtil() {
   }

   private static float m1598(int hash) {
      int v1 = hash * (1103515245) + 12345;
      float result = (float)(v1 >> 16 & 255);

      return result / 127.5F - 1.0F;
   }

   
}
