package ru.white.utils.system;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.white.auth.StrDec;
import ru.white.Client;

@Environment(EnvType.CLIENT)
public class Bu {
   private static boolean f1634 = false;
   private static boolean f1633 = false;
   private static final Logger LOGGER = LoggerFactory.getLogger("client/FontInitializer");

   public static void register() {
      if (!f1633) {

         f1633 = true;
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            if (!f1634 && client.method_1478() != null && client.method_22683() != null) {
               try {
                  gu v1x = Client.get().render2D().getFontRenderer();
                  if (v1x != null && !v1x.isInitialized()) {
                     v1x.initialize();
                     f1634 = true;

                     LOGGER.info("Fonts initialized successfully");
                  }
               } catch (Exception v4) {
                  Logger result = LOGGER;
                  String var10001x = "Failed to initialize fonts";

                  result.error(var10001x, v4);
               }
            }
         });
      }
   }

   public static boolean isInitialized() {
      return f1634;
   }

   
}
