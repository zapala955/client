package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.IListener;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Qc extends rc implements IListener {
   private boolean f104;

   @Override
   public boolean isCancelled() {
      return this.f104;
   }

   @Override
   public void cancel() {
      xc.super.cancel();
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.f104 = cancelled;
      int v2 = 648469146;
      int temp1 = v2 + 1;

      
   }

   
}
