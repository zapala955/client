package ru.white.utils.misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.manager.ModuleManager;
import ru.white.auth.StrDec;
import ru.white.Client;

@Environment(EnvType.CLIENT)
public final class Uq {
   private static final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();

   @Generated
   private Uq() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static <T extends Module> T get(String module) {
      Module result = Client.get().moduleManager().get(module);

      return (T)result;
   }

   public static <T extends Module> T get(Class<T> clazz) {
      Module result = (Module)clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> {
         if var10000x = Client.get().moduleManager();
         int v2 = 1838591040;

         return var10000x.get((Class<? extends Module>)instance);
      }));

      return (T)result;
   }

   
}
