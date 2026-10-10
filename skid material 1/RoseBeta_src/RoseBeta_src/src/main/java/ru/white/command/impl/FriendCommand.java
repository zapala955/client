package ru.white.command.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;
import ru.white.manager.FriendManager;

@Environment(EnvType.CLIENT)
public class FriendCommand extends Command {
   private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list");

   public FriendCommand() {
      super(
         "friend",
         ".friend <add|remove|list> [name]",
         "Список друзей"
      );
   }

   private void m25() {
      zp.addChatMessage("§7.friend add/remove §f<ник> §8| §7.friend list");
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      Stream result = SUBCOMMANDS.stream();
      Predicate temp1 = s -> {
         int v2x = 466603612;

         return s.startsWith(subPrefix.toLowerCase());
      };

      return result.filter(temp1).collect(Collectors.toList());
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.m25();
      } else {
         String v2 = args[0].toLowerCase();
         switch (v2) {
            case "add":
               if (args.length < 2) {
                  zp.addChatMessage("Использование: .friend add <ник>");
                  return;
               }

               String v16 = args[1];
               boolean v18 = Client.get().friendManager().add(v16);
               if (v18) {
                  zp.addChatMessage("§a" + v16 + "§r добавлен в друзья");
               } else {
                  zp.addChatMessage("§7" + v16 + " уже в друзьях");
               }
               break;
            case "remove":
               if (args.length < 2) {

                  zp.addChatMessage("Использование: .friend remove <ник>");
                  return;
               }

               String v15 = args[1];
               boolean v17 = Client.get().friendManager().remove(v15);
               if (v17) {
                  zp.addChatMessage("§c" + v15 + "§r удалён из друзей");
               } else {
                  zp.addChatMessage("§7" + v15 + " не в друзьях");
               }
               break;
            case "list":
               FriendManager result = Client.get().friendManager();

               Set v4 = result.getFriends();
               if (v4.isEmpty()) {
                  zp.addChatMessage("§7Список друзей пуст");
               } else {
                  String v5 = v4.stream().collect(Collectors.joining("§7, §a"));
                  int v11 = v4.size();
                  zp.addChatMessage("§7Друзья §8[" + v11 + "§8]§r: §a" + v5);
               }
               break;
            default:
               this.m25();
         }
      }
   }

   
}
