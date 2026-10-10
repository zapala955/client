package ru.white.command.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class BaritoneCommand extends Command {
   private static final List<String> COMMON_SUBCOMMANDS;

   static {
      String[] result = new String[21];
      result[0] = "help";
      result[1] = "goto";
      result[2] = "mine";
      result[3] = "stop";
      result[4] = "pause";
      result[5] = "resume";
      result[6] = "follow";
      result[7] = "path";
      result[8] = "explore";
      result[9] = "farm";
      result[10] = "sel";
      result[11] = "build";
      result[12] = "come";
      result[13] = "surface";
      result[14] = "tunnel";
      result[15] = "settings";
      result[16] = "version";
      result[17] = "cancel";
      result[18] = "invert";
      result[19] = "repack";
      result[20] = "render";
      COMMON_SUBCOMMANDS = Arrays.asList(result);
   }

   public BaritoneCommand() {
      super(
         "baritone",
         ".baritone <команда> [аргументы] (или #команда)",
         "Управление ботом Baritone"
      );
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String v2 = subPrefix.toLowerCase();

      return COMMON_SUBCOMMANDS.stream().filter(s -> {
         boolean var10000x = s.startsWith(v2);

         return var10000x;
      }).collect(Collectors.toList());
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         char v17 = Client.get().commandManager().getPrefix();
         zp.addChatMessage(
            "§7Использование: §a"
               + v17
               + "baritone <команда> §7или §a#<команда>"
         );
         zp.addChatMessage(
            "§7Примеры: §f"
               + v17
               + "baritone goto 100 64 100§7, §f"
               + v17
               + "baritone mine diamond_ore§7, §f"
               + v17
               + "baritone stop§7, §f#help"
         );
      } else {
         String v2 = String.join(" ", args);

         try {
            Class v3 = Class.forName("baritone.api.BaritoneAPI");
            Object v18 = v3.getMethod("getProvider").invoke(null);
            if (v18 == null) {
               zp.addChatMessage(
                  "§cBaritone ещё не инициализирован (войдите в мир)"
               );
               return;
            }

            Object v5 = v18.getClass().getMethod("getPrimaryBaritone").invoke(v18);
            if (v5 == null) {
               zp.addChatMessage(
                  "§cBaritone ещё не инициализирован (войдите в мир)"
               );
               return;
            }

            Object v6 = v5.getClass().getMethod("getCommandManager").invoke(v5);
            Method v7 = v6.getClass().getMethod("execute", String.class);
            v7.invoke(v6, v2);
         } catch (ClassNotFoundException v15) {
            zp.addChatMessage("§cBaritone не найден (установите мод Baritone)");
         } catch (Throwable v16) {
            Throwable v4 = v16.getCause() != null ? v16.getCause() : v16;
            String v12 = v4.getMessage();
            StringBuilder result = new StringBuilder();
            String temp1 = "§cОшибка выполнения Baritone: ";

            result = result.append(temp1);

            zp.addChatMessage(result.append((Object)v12).toString());
         }
      }
   }

   
}
