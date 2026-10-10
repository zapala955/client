package ru.white.command.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class CfgCommand extends Command {
   private static final List<String> SUBCOMMANDS = List.of("save", "load", "list", "dir");

   private void m23() {

      zp.addChatMessage("§7.cfg save/load §f<name> §8| §7.cfg list §8| §7.cfg dir");
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.m23();
      } else {
         String v2 = args[0].toLowerCase();
         switch (v2) {
            case "save":
               if (args.length < 2) {
                  zp.addChatMessage("Использование: .cfg save <название>");
                  return;
               }

               Client.get().configManager().save(args[1]);
               String v6 = args[1];
               zp.addChatMessage("Конфиг §a" + v6 + "§r сохранён");
               break;
            case "load":
               if (args.length < 2) {
                  zp.addChatMessage("Использование: .cfg load <название>");
                  return;
               }

               boolean v15 = Client.get().configManager().load(args[1]);
               if (v15) {
                  String v7 = args[1];
                  zp.addChatMessage("Конфиг §a" + v7 + "§r загружен");
               } else {
                  String v8 = args[1];
                  zp.addChatMessage("§cКонфиг §r" + v8 + "§c не найден");
               }
               break;
            case "list":
               List v14 = Client.get().configManager().listConfigs();
               if (v14.isEmpty()) {
                  zp.addChatMessage("§7Нет сохранённых конфигов");
               } else {
                  int v16 = v14.size();
                  String v9 = String.join("§7, §r", v14);
                  int v10 = v16;
                  zp.addChatMessage("§7Конфиги §8[" + v10 + "§8]§r: " + v9);
               }
               break;
            case "dir":
               Path result = Client.get().configManager().getConfigDir();

               String v4 = result.toAbsolutePath().toString();

               try {
                  new ProcessBuilder("explorer.exe", v4).start();
               } catch (IOException v13) {
                  zp.addChatMessage("§cНе удалось открыть папку: " + v4);
               }
               break;
            default:
               this.m23();
         }
      }
   }

   public CfgCommand() {
      super("cfg", ".cfg <save|load|list|dir> [name]", "Конфиги");
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      Object result = SUBCOMMANDS.stream().filter(s -> {
         String var10001x = subPrefix.toLowerCase();

         return s.startsWith(var10001x);
      }).collect(Collectors.toList());

      return (List<String>)result;
   }

   
}
