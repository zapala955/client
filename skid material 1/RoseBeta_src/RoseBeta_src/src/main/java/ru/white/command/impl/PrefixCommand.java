package ru.white.command.impl;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class PrefixCommand extends Command {
   public PrefixCommand() {
      super(
         "prefix",
         ".prefix <символ>",
         "Изменить префикс команд"
      );
   }

   @Override
   public void execute(String[] args) {
      if (args.length != 0) {

         if (!args[0].isEmpty()) {
            char v9 = args[0].charAt(0);
            Client.get().commandManager().setPrefix(v9);

            StringBuilder result = new StringBuilder();

            zp.addChatMessage(
               result.append("§7Префикс изменён на: §a").append(v9).toString()
            );
            return;
         }
      }

      char v2 = Client.get().commandManager().getPrefix();
      zp.addChatMessage("§7Текущий префикс: §a" + v2);
      zp.addChatMessage(
         "§7Использование: "
            + v2
            + "prefix <символ>"
      );
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      return List.of();
   }

   
}
