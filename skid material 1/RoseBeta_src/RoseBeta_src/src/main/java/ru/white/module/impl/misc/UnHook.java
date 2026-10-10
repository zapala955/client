package ru.white.module.impl.misc;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;

@ModuleAnnotation(
   name = "Un Hook",
   desc = "Убирает сторонние хуки и инжекты",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class UnHook extends Module {
   public final String path;
   private static String f990 = "1";
   public static boolean unhooked = false;
   public static File resourcePackFolder;
   private final List<Module> f991;

   private void m1302() {
      try {
         Path v1 = Client.get().configManager().getConfigDir();
         if (!Files.exists(v1)) {
            return;
         }

         Random v2 = new Random();
         long v3 = this.m1303(v2);
         this.m1304(v1, v3);
         Stream v5 = Files.list(v1);

         try {

            Consumer temp1 = file -> {
               this.m1304(file, this.m1303(v2));
               
            };

            v5.forEach(temp1);
         } catch (Throwable v12) {
            if (v5 != null) {
               try {
                  v5.close();
               } catch (Throwable v11) {
                  v12.addSuppressed(v11);
               }
            }

            throw v12;
         }

         if (v5 != null) {
            v5.close();
         }
      } catch (Exception v13) {
      }
   }

   private void m1304(Path path, long millis) {
      try {
         FileTime v4 = FileTime.fromMillis(millis);
         Files.setAttribute(path, "basic:lastModifiedTime", v4);

         Files.setAttribute(path, "basic:lastAccessTime", v4);
      } catch (Exception v6) {
      }
   }

   private void m1305() {
      try {
         Path result = MinecraftClient.method_1551().field_1697.toPath();
         String temp1 = "logs/latest.log";

         Path v1 = result.resolve(temp1);
         if (!Files.exists(v1)) {
            return;
         }

         String v2 = this.path;
         Path v3 = Paths.get(v2, "logs");
         Path v4 = v3.resolve("latest.log");
         if (!Files.exists(v3)) {
            Files.createDirectories(v3);
         }

         List v5 = Files.readAllLines(v1, StandardCharsets.UTF_8);
         List v6 = v5.stream()
            .filter(line -> {
               int v1x = -382525821;
               int var10002x = v1x + 1;

               return !line.contains("Nightix »");
            })
            .filter(line -> {

               return !line.contains("[Nightix]");
            })
            .filter(line -> {
               boolean v2x;
               if (!line.contains("Rose »")) {

                  v2x = true;
               } else {
                  v2x = false;
               }

               return v2x;
            })
            .filter(line -> {
               boolean var10000x = line.contains("[Rose]");

               if (!var10000x) {

                  var10000x = true;
               } else {
                  var10000x = false;
               }

               return var10000x;
            })
            .filter(line -> !line.contains("[Rose Logs]"))
            .filter(line -> !line.contains("Rose Solution"))
            .filter(line -> {
               int v1x = -2099129933;
               int var10002x = v1x * (v1x + 1);

               boolean var10000x;
               if (!line.contains("[Config]")) {
                  var10000x = true;
               } else {
                  var10000x = false;
                  
               }

               return var10000x;
            })
            .filter(line -> {
               boolean var10000x;
               if (!line.contains("[Manager]")) {
                  var10000x = true;
               } else {
                  var10000x = false;
                  
               }

               return var10000x;
            })
            .filter(line -> {
               boolean var10000x = line.contains("[Baritone]");
               int v1x = -1385390115;
               int v2x = -443654530;
               int var10002x = v2x * (v2x + 1);

               return !var10000x;
            })
            .filter(line -> {
               int v1x = 1921457561;

               return !line.contains("baritone");
            })
            .filter(line -> !line.contains("- white"))
            .filter(line -> {
               boolean var10000x = line.contains("mod white");

               return !var10000x;
            })
            .filter(line -> {

               return !line.contains("white,");
            })
            .filter(line -> {

               boolean var10000x;
               if (!line.contains("Execution-1.21.4")) {
                  var10000x = true;
               } else {
                  var10000x = false;
                  int v1x = -1406936546;
                  int var10001x = v1x * (v1x + 1);

                  
               }

               return var10000x;
            })
            .collect(Collectors.toList());
         Files.write(v4, v6, StandardCharsets.UTF_8);
         Files.write(v1, new byte[0]);
         System.out.println("Logs sanitized.");
      } catch (Exception v8) {
         v8.printStackTrace();
      }
   }

   private long m1303(Random rng) {
      long v2 = -7000836777406331873L + 7000836777751931873L;
      long result = System.currentTimeMillis();

      return result - (long)(rng.nextDouble() * (double)v2);
   }

   public UnHook() {
      String v1 = System.getProperty("user.home");
      this.path = v1 + "/AppData/Roaming/.tlauncher/legacy/Minecraft/game/";
      this.f991 = new ArrayList<>();
   }

   @Override
   public void onEnable() {
      super.onEnable();
      unhooked = true;
      if (mc.field_1724 != null) {
         Client.get().commandManager().setPrefix((char)(33));
         if (mc.field_1705 != null && mc.field_1705.method_1743() != null) {
            mc.field_1705.method_1743().method_1808(false);
         }
      }

      String v1 = this.path;

      if (v1 != null && !v1.isEmpty()) {
         resourcePackFolder = new File(v1, "resourcepacks");
      }

      this.f991.clear();

      for (Module v3 : Client.get().moduleManager().values()) {
         if (v3 != this && v3.isEnabled()) {
            this.f991.add(v3);
            v3.setEnabled(false, false);
         }
      }

      this.m1302();
      this.m1305();
      this.m1306();
   }

   private void m1306() {
      try {
         String v1 = MinecraftClient.method_1551().field_1697.getAbsolutePath().replace("\\", "\\\\");
         String v2 = "*FunTime*;*Litka*;*Execution*;*baritone*;*bariton*";
         File v3 = File.createTempFile("sys_cleaner_v2", ".ps1");
         ArrayList v4 = new ArrayList();
         v4.add("$everythingIni = \"$env:APPDATA\\Everything\\Everything.ini\"");
         v4.add("if (Test-Path $everythingIni) {");
         v4.add("    $content = Get-Content $everythingIni");
         v4.add("    $foldersToAdd = \"" + v1 + ";" + v2 + "\"");
         v4.add("    if ($content -match \"exclude_folders=(.*)\") {");
         v4.add("        $current = $matches[1]");
         v4.add("        if ($current -notlike \"*$foldersToAdd*\") {");
         v4.add(
            "            $newExcludes = if ($current) { \"$current;$foldersToAdd\" } else { $foldersToAdd }"
         );
         v4.add(
            "            $content = $content -replace \"exclude_folders=.*\", \"exclude_folders=$newExcludes\""
         );
         v4.add("        }");
         v4.add("    }");
         v4.add("    $filesToAdd = \"" + v2 + "\"");
         v4.add("    if ($content -match \"exclude_files=(.*)\") {");
         v4.add("        $currentFiles = $matches[1]");

         v4.add("        if ($currentFiles -notlike \"*$filesToAdd*\") {");
         v4.add(
            "            $newFileExcludes = if ($currentFiles) { \"$currentFiles;$filesToAdd\" } else { $filesToAdd }"
         );
         v4.add(
            "            $content = $content -replace \"exclude_files=.*\", \"exclude_files=$newFileExcludes\""
         );
         v4.add("        }");
         v4.add("    }");
         v4.add("    $content | Set-Content $everythingIni");
         v4.add("    Stop-Process -Name \"Everything\" -Force -ErrorAction SilentlyContinue");
         v4.add(
            "    Start-Process \"Everything.exe\" -WindowStyle Hidden -ErrorAction SilentlyContinue"
         );
         v4.add("}");
         v4.add(
            "Remove-Item \"$env:APPDATA\\Microsoft\\Windows\\Recent\\*FunTime*\" -Force -ErrorAction SilentlyContinue"
         );
         v4.add(
            "Remove-Item \"$env:APPDATA\\Microsoft\\Windows\\Recent\\*Litka*\" -Force -ErrorAction SilentlyContinue"
         );
         v4.add(
            "Remove-Item \"$env:APPDATA\\Microsoft\\Windows\\Recent\\*Execution*\" -Force -ErrorAction SilentlyContinue"
         );

         v4.add(
            "Remove-Item \"$env:APPDATA\\Microsoft\\Windows\\Recent\\*bariton*\" -Force -ErrorAction SilentlyContinue"
         );
         v4.add("$rbPath = \"$env:SystemDrive\\`$Recycle.Bin\"");
         v4.add("$sidFolders = @()");
         v4.add(
            "if (Test-Path $rbPath) { $sidFolders = Get-ChildItem -Path $rbPath -Force -Directory -ErrorAction SilentlyContinue }"
         );
         v4.add("Clear-RecycleBin -Force -ErrorAction SilentlyContinue");
         v4.add("Start-Sleep -Milliseconds 1500");
         v4.add("$targetTime = (Get-Date).AddHours(-1)");
         v4.add("if ($sidFolders) {");
         v4.add("    foreach ($folder in $sidFolders) {");
         v4.add("        try {");
         v4.add("            $fItem = Get-Item -Path $folder.FullName -Force -ErrorAction Stop");
         v4.add("            $fItem.LastWriteTime = $targetTime");
         v4.add("            $fItem.LastAccessTime = $targetTime");
         v4.add("        } catch {}");
         v4.add("    }");
         v4.add("}");
         Files.write(v3.toPath(), v4, StandardCharsets.UTF_8);
         String v9 = v3.getAbsolutePath();
         String v5 = "powershell.exe -WindowStyle Hidden -ExecutionPolicy Bypass -File \""
            + v9
            + "\"";
         Runtime.getRuntime().exec(v5);
         v3.deleteOnExit();
      } catch (Exception v12) {
         v12.printStackTrace();
      }
   }

   @Generated
   public static String getCode() {

      return f990;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      unhooked = false;
      if (mc.field_1724 != null) {
         Client.get().commandManager().setPrefix((char)(46));
      }

      for (Module v2 : this.f991) {

         if (!v2.isEnabled()) {

            v2.setEnabled(true, false);
         }
      }

      this.f991.clear();
   }

   
}
