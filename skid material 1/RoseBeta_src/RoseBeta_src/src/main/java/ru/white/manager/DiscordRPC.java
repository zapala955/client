package ru.white.manager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class DiscordRPC implements IMinecraft {
   public static boolean started;
   public static an presence = new an();

   public void stopRpc() {
      if (Client.get() != null && Client.get().getDiscordManager() != null) {
         Client result = Client.get();

         result.getDiscordManager().stopRPC();
      }

      started = false;
   }

   public void startRpc() {
      if (Client.get() != null && Client.get().getDiscordManager() != null) {
         Client.get().getDiscordManager().init();
      } else {
         new DiscordManager().init();
      }

      started = true;
      int result = v1 * (v1 + 1);

      
   }

   
}
