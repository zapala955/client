package ru.white.utils.render;

import java.lang.reflect.Method;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class hs {
   private static boolean f1478 = false;
   private static Object f1480;
   private static Method f1481;
   private static boolean f1479 = false;

   private hs() {
   }

   private static void m1800() {
      if (!f1478) {
         f1478 = true;
         boolean v0 = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");
         if (v0) {
            String[] v1 = new String[]{
               "net.irisshaders.iris.api.v0.IrisApi",
               "net.coderbot.iris.api.v0.IrisApi"
            };

            for (String v5 : v1) {
               try {
                  Class v6 = Class.forName(v5);

                  Method v7 = v6.getMethod("getInstance");
                  f1480 = v7.invoke(null);
                  String v11 = "isShaderPackInUse";

                  f1481 = v6.getMethod(v11);
                  f1479 = true;
                  return;
               } catch (Throwable v10) {
               }
            }
         }
      }
   }

   public static boolean shadersActive() {
      m1800();
      if (!f1479) {
         return false;
      } else {
         try {
            Object v0 = f1481.invoke(f1480);
            boolean result;
            if (v0 instanceof Boolean && (Boolean)v0) {
               result = true;
            } else {
               result = false;
               
            }

            return result;
         } catch (Throwable v4) {
            return false;
         }
      }
   }

   
}
