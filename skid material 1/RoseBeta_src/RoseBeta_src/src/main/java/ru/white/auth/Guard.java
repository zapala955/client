package ru.white.auth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Guard {
   private static volatile boolean started;
   private static final List<String> LOG = new ArrayList<>();

   private Guard() {
   }

   public static synchronized void init() {
      if (!started) {
         started = true;

         try {
            System.setProperty("jdk.attach.allowAttachSelf", "false");
         } catch (Throwable v4) {
         }

         Properties v0 = loadCfg();
         if (hasAgentArgs()) {
            die();
         }

         if (!checkExpiry(v0)) {
            die();
         }

         if (!checkHwid(v0)) {
            die();
         }

         if (!checkIntegrity(v0)) {
            die();
         }

         Thread v1 = new Thread(() -> watchLoop(), "rose-guard");
         v1.setDaemon(true);
         v1.setPriority(1);

         try {
            v1.start();
         } catch (Throwable v3) {
         }
      }
   }

   private static Properties loadCfg() {
      Properties v0 = new Properties();

      try (InputStream v1 = Guard.class.getResourceAsStream("guard.dat")) {
         if (v1 != null) {
            v0.load(v1);
         }
      } catch (Throwable v6) {
      }

      return v0;
   }

   private static boolean hasAgentArgs() {
      try {
         for (String v2 : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            String v3 = v2.toLowerCase(Locale.ROOT);
            if (v3.contains("-javaagent")
               || v3.contains("-agentpath")
               || v3.contains("-agentlib")
               || v3.contains("jdwp")
               || v3.contains("xrunjdwp")
               || v3.contains("xdebug")) {
               return true;
            }
         }
      } catch (Throwable v4) {
      }

      return false;
   }

   private static boolean checkExpiry(Properties v0) {
      try {
         String v1 = v0.getProperty("expiry", "").trim();
         if (v1.isEmpty()) {
            return true;
         } else {
            LocalDate v2 = LocalDate.parse(v1);
            return !LocalDate.now().isAfter(v2);
         }
      } catch (Throwable v3) {
         return true;
      }
   }

   private static boolean checkHwid(Properties v0) {
      try {
         String v1 = v0.getProperty("hwid", "").trim();
         if (v1.isEmpty()) {
            return true;
         } else {
            String v2 = hwid();

            for (String v6 : v1.split(",")) {
               if (!v6.trim().isEmpty() && v6.trim().equalsIgnoreCase(v2)) {
                  return true;
               }
            }

            return false;
         }
      } catch (Throwable v7) {
         return true;
      }
   }

   private static boolean checkIntegrity(Properties v0) {
      try {
         String v1 = v0.getProperty("int_hash", "").trim();
         if (v1.isEmpty()) {
            return true;
         } else {
            byte[] v2 = readOwnEntry("fabric.mod.json");
            byte[] v3 = readOwnEntry("white.mixins.json");
            byte[] v4 = readOwnEntry("ru/white/Client.class");
            if (v2 != null && v3 != null && v4 != null) {
               MessageDigest v5 = MessageDigest.getInstance("SHA-256");
               v5.update(v2);
               v5.update(v3);
               v5.update(v4);
               return hex(v5.digest()).equalsIgnoreCase(v1);
            } else {
               return true;
            }
         }
      } catch (Throwable v6) {
         return true;
      }
   }

   private static byte[] readOwnEntry(String v0) {
      try {
         URI v1 = Guard.class.getProtectionDomain().getCodeSource().getLocation().toURI();

         byte[] v5;
         try (ZipFile v2 = new ZipFile(new File(v1))) {
            ZipEntry v3 = v2.getEntry(v0);
            if (v3 == null) {
               return null;
            }

            try (InputStream v4 = v2.getInputStream(v3)) {
               v5 = readAll(v4);
            }
         }

         return v5;
      } catch (Throwable v11) {
         return null;
      }
   }

   private static void watchLoop() {
      String[] v0 = new String[]{
         "procdump",
         "procdump64",
         "windbg",
         "windbgx",
         "x64dbg",
         "x32dbg",
         "cheatengine",
         "processhacker",
         "systeminformer",
         "ksdumper",
         "ksdumperclient",
         "scylla",
         "ollydbg",
         "ida64",
         "ida32",
         "ida",
         "ghidra",
         "reclass",
         "httpdebugger",
         "fiddler",
         "wireshark",
         "mimidump",
         "nanodump",
         "procmon",
         "procexp",
         "procexp64",
         "exeinfo",
         "die"
      };

      while (true) {
         try {
            Thread.sleep(15000L);
         } catch (Throwable v12) {
            return;
         }

         try {
            for (ProcessHandle v2 : ProcessHandle.allProcesses()::iterator) {
               String v3 = "";

               try {
                  String v4 = v2.info().command().orElse("");
                  v4 = v4.replace('\\', '/');
                  int v5 = v4.lastIndexOf(47);
                  v3 = (v5 >= 0 ? v4.substring(v5 + 1) : v4).toLowerCase(Locale.ROOT);
                  if (v3.endsWith(".exe")) {
                     v3 = v3.substring(0, v3.length() - 4);
                  }
               } catch (Throwable v11) {
               }

               if (!v3.isEmpty()) {
                  for (String v7 : v0) {
                     if (v3.equals(v7)
                        || v3.startsWith(v7 + "-")
                        || v3.startsWith(v7 + "_")
                        || v3.startsWith(v7 + ".")
                        || v3.startsWith(v7) && v3.length() > v7.length() && Character.isDigit(v3.charAt(v7.length()))) {
                        die();
                     }
                  }
               }
            }
         } catch (Throwable v13) {
         }
      }
   }

   public static String hwid() {
      try {
         String result = System.getProperty("user.name", "?");
         String temp1 = System.getProperty("os.name", "?");
         String temp2 = System.getProperty("os.arch", "?");
         int temp3 = Runtime.getRuntime().availableProcessors();
         String v2 = boardUuid();
         int v3 = temp3;
         String v4 = temp2;
         String v5 = temp1;
         String v6 = result;
         String v0 = v6 + ";" + v5 + ";" + v4 + ";" + v3 + ";" + v2;
         MessageDigest v1 = MessageDigest.getInstance("SHA-256");
         return hex(v1.digest(v0.getBytes(StandardCharsets.UTF_8)));
      } catch (Throwable v7) {
         return "?";
      }
   }

   private static String boardUuid() {
      String v0 = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

      try {
         if (v0.contains("win")) {
            Process v1 = new ProcessBuilder("wmic", "csproduct", "get", "uuid").redirectErrorStream(true).start();
            if (v1.waitFor(1500L, TimeUnit.MILLISECONDS)) {
               String v2 = new String(readAll(v1.getInputStream()), StandardCharsets.UTF_8);

               for (String v6 : v2.split("\\R")) {
                  v6 = v6.trim();
                  if (v6.length() > 8 && !v6.toLowerCase(Locale.ROOT).contains("uuid")) {
                     return v6;
                  }
               }
            }
         } else {
            Path[] v9 = new Path[]{
               Paths.get("/etc/machine-id"), Paths.get("/var/lib/dbus/machine-id")
            };

            for (Path v13 : v9) {
               try {
                  String v15 = new String(Files.readAllBytes(v13), StandardCharsets.UTF_8).trim();
                  if (!v15.isEmpty()) {
                     return v15;
                  }
               } catch (Throwable v7) {
               }
            }
         }
      } catch (Throwable v8) {
      }

      return "?";
   }

   private static byte[] readAll(InputStream v0) throws IOException {
      ByteArrayOutputStream v1 = new ByteArrayOutputStream();
      byte[] v2 = new byte[8192];

      int v3;
      while ((v3 = v0.read(v2)) > 0) {
         v1.write(v2, 0, v3);
      }

      return v1.toByteArray();
   }

   private static String hex(byte[] v0) {
      StringBuilder v1 = new StringBuilder(v0.length * 2);

      for (byte v5 : v0) {
         v1.append(Character.forDigit(v5 >> 4 & 15, 16));
         v1.append(Character.forDigit(v5 & 15, 16));
      }

      return v1.toString();
   }

   private static void die() {
      try {
         System.out.println("[Rose] Failed to initialize audio engine (code 187)");
      } catch (Throwable v3) {
      }

      try {
         Thread.sleep(500L);
      } catch (Throwable v2) {
      }

      try {
         Runtime.getRuntime().halt(187);
      } catch (Throwable v1) {
      }

      throw new Error("audio engine init failed (187)");
   }
}
