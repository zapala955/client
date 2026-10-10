package ru.white.utils.discord;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public interface Xm extends Library {
   xm INSTANCE = create();

   void Discord_Register(String v1, String v2);

   void Discord_Initialize(String v1, sm v2, boolean v3, String v4);

   void Discord_Respond(String v1, int v2);

   void Discord_UpdatePresence(an v1);

   void Discord_UpdateConnection();

   void Discord_Shutdown();

   void Discord_RunCallbacks();

   void Discord_ClearPresence();

   void Discord_UpdateHandlers(sm v1);

   private static xm create() {
      if (!Platform.isWindows()) {
         return null;
      } else {
         try {
            Library result = Native.load("discord-rpc", xm.class);

            return (xm)result;
         } catch (Throwable v19) {
            String[] v0 = new String[]{
               "src/client/resources/win32-x86-64/discord-rpc.dll",
               "win32-x86-64/discord-rpc.dll",
               "discord-rpc.dll",
               "build/resources/client/win32-x86-64/discord-rpc.dll"
            };

            for (String v4 : v0) {
               File v5 = new File(v4);
               if (v5.exists()) {
                  try {
                     return (xm)Native.load(v5.getAbsolutePath(), xm.class);
                  } catch (Throwable v18) {
                  }
               }
            }

            String[] v20 = new String[]{
               "/win32-x86-64/discord-rpc.dll",
               "/assets/win32-x86-64/discord-rpc.dll",
               "/discord-rpc.dll"
            };

            for (String v24 : v20) {
               try {
                  xm v25;
                  try (InputStream v6 = xm.class.getResourceAsStream(v24)) {

                     if (v6 == null) {
                        continue;
                     }

                     File v7 = new File(
                        System.getProperty("java.io.tmpdir"),
                        "discord-rpc-rose.dll"
                     );

                     try (FileOutputStream v8 = new FileOutputStream(v7)) {
                        v6.transferTo(v8);
                     }

                     v7.deleteOnExit();
                     v25 = (xm)Native.load(v7.getAbsolutePath(), xm.class);
                  }

                  return v25;
               } catch (Throwable v17) {
               }
            }

            System.err
               .println(
                  "[Rose] Не удалось найти или загрузить discord-rpc.dll"
               );
            return null;
         }
      }
   }

   void Discord_RegisterSteamGame(String v1, String v2);

   @Environment(EnvType.CLIENT)
   public static enum ym {
      IGNORE(2),
      NO(0),
      YES(1);
      // $VF: synthetic field
      private static final xm.ym[] $VALUES = m1334();
      public final int reply;

      private ym(int reply) {
         this.reply = reply;
      }

      private static xm.ym[] m1333() {
         xm.ym[] result = new xm.ym[3];
         int v0 = 2024065941;
         int temp2 = v0 * (v0 + 1);

         result[0] = NO;
         result[1] = YES;
         result[2] = IGNORE;
         return result;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class zm {
      public static xm getInstance() {
         int v1 = -778426857;
         int temp1 = v1 * (v1 + 1);

         return xm.INSTANCE;
      }

      public static boolean isAvailable() {
         return xm.INSTANCE != null;
      }

      
   }
}
