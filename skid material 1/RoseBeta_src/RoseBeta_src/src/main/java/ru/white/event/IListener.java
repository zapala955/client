package ru.white.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public interface IListener {
   boolean isCancelled();

   default void cancel() {
      this.setCancelled(true);

      int result = v1 * (v1 + 1);

      
   }

   void setCancelled(boolean v1);
}
