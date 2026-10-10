package ru.white.manager;

import com.sun.jna.Platform;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.runtime.ObjectMethods;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import ru.white.utils.misc.Rq;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class DiscordManager implements IMinecraft {
   private Identifier f1003;
   private long startTimestamp;
   private boolean f1002;
   private DiscordManager.vm f999;
   private DiscordManager.wm info;
   private boolean f1001 = false;
   private sm f1000;

   @Generated
   public void setHandlers(sm handlers) {

      this.f1000 = handlers;
   }

   @Generated
   public long getStartTimestamp() {

      return this.startTimestamp;
   }

   public DiscordManager() {
      this.f1002 = false;
      this.info = new um.wm("Unknown", "", "");
      this.startTimestamp = 0L;
   }

   @Generated
   public void setInfo(um.wm info) {

      this.info = info;
      
   }

   @Generated
   public void setInitialized(boolean initialized) {

      this.f1002 = initialized;
   }

   @Generated
   public boolean isInitialized() {
      return this.f1002;
   }

   @Generated
   public boolean isRunning() {

      return this.f1001;
   }

   public void load() throws IOException {
      if (this.f1003 == null && this.info != null && this.info.avatarUrl() != null) {
         String result = this.info.avatarUrl();

         if (!result.isEmpty()) {
            NativeImageBackedTexture v1 = rq.getHeadFromURL(this.info.avatarUrl());
            if (v1 != null) {

               this.f1003 = rq.registerDynamicTexture("avatar-", v1);
            }
         }
      }
   }

   @Generated
   public void setDiscordDaemonThread(um.vm discordDaemonThread) {

      this.f999 = discordDaemonThread;
   }

   @Generated
   public void setStartTimestamp(long startTimestamp) {
      this.startTimestamp = startTimestamp;
   }

   public void stopRPC() {
      if (!this.f1002) {
         System.out.println("[Rose Discord] Discord RPC не был инициализирован");
      } else {
         this.f1001 = false;
         this.f1002 = false;
         this.startTimestamp = 0L;
         if (this.f999 != null && this.f999.isAlive()) {
            this.f999.interrupt();
         }

         if (xm.INSTANCE != null) {
            try {
               xm.INSTANCE.Discord_ClearPresence();
               xm.INSTANCE.Discord_Shutdown();
            } catch (Exception v5) {
               PrintStream result = System.err;
               String v2 = v5.getMessage();
               StringBuilder temp1 = new StringBuilder();
               int temp3 = 1660827261;

               result.println(
                  temp1.append(StrDec.d("hWTMqw5jaiSHv9aw5TRkp5QzXzwnp+tJmlhdNmWioQPmJivgffoEdeZkqvtnECsP15qClE8PCtHaMAZEPQ==", temp3))
                     .append((Object)v2)
                     .toString()
               );
               
            }
         }

         System.out.println("[Rose Discord] Discord RPC остановлен");
      }
   }

   @Generated
   public DiscordManager.vm getDiscordDaemonThread() {

      return this.f999;
   }

   @Generated
   public Identifier getAvatarId() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.f1003;
   }

   public void updatePresence() {
      if (this.f1002 && xm.INSTANCE != null) {
         String v1 = "Player";
         MinecraftClient v2 = MinecraftClient.method_1551();
         if (v2 != null && v2.method_1548() != null) {
            v1 = v2.method_1548().method_1676();
         }

         an.bn result = new an.bn().setStartTimestamp(this.startTimestamp);
         String temp1;
         if (Client.build != null) {
            temp1 = Client.build;
            
         } else {
            temp1 = "1.21.11";
         }

         String v4 = temp1;
         int v10 = v6 + 1;

         an v3 = result.setDetails("Rose client Build " + v4 + " Solution")
            .setLargeImage("rose_logo", "Спасибо что играете!")
            .setState("User: " + v1)
            .setButtons(
               mn.create("Telegram", "https://t.me/RoseClients"),
               mn.create("Discord", "https://discord.gg/Egn5puSMEh")
            )
            .build();
         xm.INSTANCE.Discord_UpdatePresence(v3);
      }
   }

   @Generated
   public void setRunning(boolean running) {
      this.f1001 = running;
   }

   @Generated
   public DiscordManager.wm getInfo() {

      return this.info;
   }

   @Generated
   public void setAvatarId(Identifier avatarId) {

      this.f1003 = avatarId;
      
   }

   public void init() {

      if (this.f1002) {
         System.out.println("[Rose Discord] Discord RPC уже инициализирован");
      } else if (xm.INSTANCE == null) {
         if (Platform.isWindows()) {
            System.err
               .println(
                  "[Rose Discord] Библиотека Discord RPC недоступна (INSTANCE == null)"
               );
         } else {
            PrintStream v4 = System.out;
            String v1 = System.getProperty("os.name");
            v4.println(
               "[Rose Discord] Discord RPC не поддерживается на данной ОС ("
                  + v1
                  + ")"
            );
         }
      } else {
         if (this.startTimestamp == 0L) {
            this.startTimestamp = System.currentTimeMillis() / (8688024653587873391L + -8688024653587872391L);
         }

         this.f1000 = new sm.tm()
            .ready(
               user -> {
                  PrintStream var10000x = System.out;

                  String v2x = user.userId;
                  String v3x = user.discriminator;
                  String v4x = user.username;
                  String var10001x = "[Rose Discord] Ready callback! User: "
                     + v4x
                     + "#"
                     + v3x
                     + " (ID: "
                     + v2x
                     + ")";

                  var10000x.println(var10001x);
                  String v5x = user.avatar;
                  String v6 = user.userId;
                  this.setInfo(
                     new um.wm(
                        user.username,
                        "https://cdn.discordapp.com/avatars/" + v6 + "/" + v5x + ".png",
                        user.userId
                     )
                  );
                  this.updatePresence();
               }
            )
            .errored((code, msg) -> {
               PrintStream var10000x = System.err;
               StringBuilder var10001x = new StringBuilder().append("[Rose Discord] Error: ");

               var10000x.println(var10001x.append(code).append(" - ").append((Object)msg).toString());
            })
            .disconnected(
               (code, msg) -> {
                  PrintStream var10000x = System.out;
                  StringBuilder var10001x = new StringBuilder();

                  var10000x.println(
                     var10001x.append("[Rose Discord] Disconnected: ")
                        .append(code)
                        .append(" - ")
                        .append((Object)msg)
                        .toString()
                  );
               }
            )
            .build();
         xm result = xm.INSTANCE;
         String v5 = "1503855259590594570";

         result.Discord_Initialize(v5, this.f1000, true, "");
         this.f1001 = true;
         this.f1002 = true;
         this.updatePresence();
         this.f999 = new um.vm();
         this.f999.start();
         System.out
            .println(
               "[Rose Discord] Discord RPC успешно инициализирован и запущен"
            );
      }
   }

   @Generated
   public sm getHandlers() {

      int v4 = v1 * (v1 + 1) % 2;

      if (v4 != 0) {
         }

      return this.f1000;
   }

   @Environment(EnvType.CLIENT)
   private class vm extends Thread {
      @Override
      public void run() {
         this.setName("Rose-Discord-RPC");

         try {
            for (; um.this.f1001 && !this.isInterrupted(); Thread.sleep(8364340476614295571L + -8364340476614293571L)) {
               if (xm.INSTANCE != null) {

                  xm.INSTANCE.Discord_RunCallbacks();

                  um.this.updatePresence();
               }

               try {
                  um.this.load();
               } catch (Exception v5) {
               }
            }
         } catch (InterruptedException v6) {
            System.out.println("[Rose Discord] Поток RPC прерван");
         } catch (Exception v7) {
            PrintStream result = System.err;
            String v2 = v7.getMessage();
            result.println("[Rose Discord] Ошибка в потоке RPC: " + v2);
            um.this.stopRPC();
         }

         System.out.println("[Rose Discord] Поток RPC завершен");
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static record wm(String userName, String avatarUrl, String userId) {
      private final String f998;
      private final String f997;

      @Override
      public final 

         int result = ObjectMethods.bootstrap<"hashCode",um.wm,"userName;avatarUrl;userId",um.wm::f997,um.wm::f998,um.wm::userId>(this);

         return result;
      }

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",um.wm,"userName;avatarUrl;userId",um.wm::f997,um.wm::f998,um.wm::userId>(this, o);

         return result;
      }

      public String avatarUrl() {

         return this.f998;
      }

      public wm(String userName, String avatarUrl, String userId) {
         this.f997 = userName;
         this.f998 = avatarUrl;
         this.userId = userId;
      }

      public String userName() {

         return this.f997;
      }

      public String userId() {

         return this.userId;
      }

      
   }
}
