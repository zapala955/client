package ru.white.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.command.impl.WayCommand;
import ru.white.command.impl.BaritoneCommand;
import ru.white.command.impl.BindCommand;
import ru.white.command.impl.BlockespCommand;
import ru.white.command.impl.CfgCommand;
import ru.white.command.impl.FriendCommand;
import ru.white.command.impl.GpsCommand;
import ru.white.command.impl.NeuroCommand;
import ru.white.command.impl.PrefixCommand;
import ru.white.command.impl.SpecCommand;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class CommandManager {
   private final Map<String, Command> f0;
   private char prefix = (char)(46);

   public void setPrefix(char prefix) {

      this.prefix = prefix;
   }

   public boolean handleMessage(String message) {
      if (!message.isEmpty() && message.charAt(0) == this.prefix) {

         String[] v2 = message.substring(1).trim().split("\\s+");
         if (v2.length != 0 && !v2[0].isEmpty()) {
            String v3 = v2[0].toLowerCase();
            Command v4 = this.f0.get(v3);
            if (v4 == null) {

               zp.addChatMessage("§cНеизвестная команда: §r" + v3);
               this.m5();
               return true;
            } else {
               v4.execute(Arrays.copyOfRange(v2, 1, v2.length));
               return true;
            }
         } else {
            this.m5();
            return true;
         }
      } else {
         return false;
      }
   }

   public CommandManager() {
      this.f0 = new LinkedHashMap<>();
   }

   public void registerAlias(String alias, Command command) {
      this.f0.put(alias.toLowerCase(), command);

      
   }

   public List<String> getSuggestions(String input) {
      if (!input.isEmpty() && input.charAt(0) == this.prefix) {
         String v2 = input.substring(1);
         if (!v2.contains(" ")) {
            return this.f0.entrySet().stream().filter(e -> {
               boolean var10000x = e.getKey().startsWith(v2.toLowerCase());
               int v2x = -1717630413;
               int temp2 = v2x + 1;

               return var10000x;
            }).map(e -> {
               char var10000x = this.prefix;
               String v2x = e.getKey();
               char v3x = var10000x;
               String v5x = v3x + v2x;

               return v5x;
            }).collect(Collectors.toList());
         } else {
            String[] v3 = v2.split(" ", 2);
            String v4 = v3[0].toLowerCase();
            String v5 = v3.length > 1 ? v3[1] : "";
            Command v6 = this.f0.get(v4);
            if (v6 == null) {
               return Collections.emptyList();
            } else {
               Stream result = v6.getSuggestions(v5).stream().map(s -> {

                  char v5x = this.prefix;
                  StringBuilder var10000x = new StringBuilder().append(v5x).append((Object)v4);

                  return var10000x.append(" ").append((Object)s).toString();
               });

               return result.collect(Collectors.toList());
            }
         }
      } else {
         return Collections.emptyList();
      }
   }

   public void register(Command command) {
      this.f0.put(command.getName().toLowerCase(), command);

      
   }

   public char getPrefix() {
      return this.prefix;
   }

   public void init() {
      this.register(new u());
      this.register(new v());

      this.register(new y());

      this.register(new t());
      this.register(new s());
      this.register(new w());
      this.register(new aa());
      this.register(new z());
      this.register(new x());
      r v1 = new r();
      this.register(v1);
      this.registerAlias("b", v1);
   }

   private void m5() {
      String v1 = this.f0.keySet().stream().map(n -> {

         char v3x = this.prefix;
         StringBuilder var10000x = new StringBuilder().append("§a").append(v3x).append((Object)n);
         int v4x = -1982863181;
         int temp2 = v4x + 1;

         return var10000x.toString();
      }).collect(Collectors.joining("§7, "));

      zp.addChatMessage("§7Команды: " + v1);
   }

   
}
