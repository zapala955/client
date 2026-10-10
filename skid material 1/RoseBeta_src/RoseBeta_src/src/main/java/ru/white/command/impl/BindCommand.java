package ru.white.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;
import ru.white.module.Module;
import ru.white.manager.ModuleManager;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class BindCommand extends Command {
   private static final List<String> KEY_SUGGESTIONS = new ArrayList<>();

   private static int m11(String name) {
      try {
         StringBuilder result = new StringBuilder();
         String temp1 = "key.keyboard.";

         result = result.append(temp1).append((Object)name);

         Key v1 = InputUtil.method_15981(result.toString());
         return v1.method_1444();
      } catch (Exception v5) {
         return -1;
      }
   }

   private static String m12(int code) {
      try {

         return Type.field_1668.method_1447(code).method_27445().getString();
      } catch (Exception v3) {
         return "?";
      }
   }

   static {
      KEY_SUGGESTIONS.add("none");

      for (int v0 = 97; v0 <= 122; v0 = (char)(v0 + 1)) {
         KEY_SUGGESTIONS.add(String.valueOf((char)v0));
      }

      for (int v2 = 48; v2 <= 57; v2 = (char)(v2 + 1)) {
         KEY_SUGGESTIONS.add(String.valueOf((char)v2));
      }

      KEY_SUGGESTIONS.add("space");
      KEY_SUGGESTIONS.add("left.shift");
      KEY_SUGGESTIONS.add("right.shift");
      KEY_SUGGESTIONS.add("left.control");
      KEY_SUGGESTIONS.add("left.alt");

      for (int v3 = 1; v3 <= 12; v3++) {
         KEY_SUGGESTIONS.add("f" + v3);
      }
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.m13();
      } else if (args[0].equalsIgnoreCase("list")) {
         if v22 = Client.get().moduleManager();

         List v18 = v22.values().stream().filter(m -> {

            return m.getKey() > 0;
         }).collect(Collectors.toList());
         if (v18.isEmpty()) {
            zp.addChatMessage("§7Ни один модуль не забинжен");
         } else {
            String v19 = v18.stream().map(m -> {
               String v1 = m12(m.getKey());
               String v2x = m.getName();
               StringBuilder var10000x = new StringBuilder;

               var10000x./* $VF: Unable to resugar constructor */<init>();
               var10000x = var10000x.append("§a").append((Object)v2x).append("§7[").append((Object)v1);
               int v3x = 322540027;
               int var10002x = v3x * (v3x + 1) % 2;

               if (var10002x != 0) {
                  }

               return var10000x.append("§7]").toString();
            }).collect(Collectors.joining("§7, "));
            int v6 = v18.size();
            zp.addChatMessage("§7Бинды §8[" + v6 + "§8]§r: " + v19);
         }
      } else {
         Module v2 = Client.get().moduleManager().getModule(args[0]);
         if (v2 == null) {
            String v7 = args[0];
            zp.addChatMessage("§cМодуль §f" + v7 + "§c не найден");
         } else if (args.length < 2) {
            if (v2.getKey() > 0) {
               String v8 = m12(v2.getKey());
               String v9 = v2.getName();
               StringBuilder v21 = new StringBuilder()
                  .append("§7")
                  .append((Object)v9)
                  .append(" забинжен на §a");

               zp.addChatMessage(v21.append((Object)v8).toString());
            } else {
               String v10 = v2.getName();
               zp.addChatMessage("§7" + v10 + " не забинжен");
            }
         } else {
            String v3 = args[1].toLowerCase();
            boolean result = v3.equals("none");

            if (!result && !v3.equals("reset") && !v3.equals("-")) {
               int v4 = m11(v3);
               if (v4 <= 0) {
                  String v12 = args[1];
                  zp.addChatMessage("§cНеизвестная клавиша: §f" + v12);
               } else {
                  v2.setKey(v4);
                  String v20 = v2.getName();
                  String v13 = m12(v4);
                  String v14 = v20;
                  zp.addChatMessage("§a" + v14 + "§r забинжен на §a" + v13);
               }
            } else {
               v2.setKey(-1);
               String v11 = v2.getName();
               zp.addChatMessage("§7Бинд §f" + v11 + "§7 сброшен");
            }
         }
      }
   }

   private void m13() {
      zp.addChatMessage(
         "§7.bind §f<модуль> <клавиша>§8 | §7.bind §f<модуль> none§8 | §7.bind list"
      );
      int result = v1 * (v1 + 1);

      
   }

   public BindCommand() {
      super(
         "bind",
         ".bind <модуль> <клавиша|none>",
         "Биндить модуль на клавишу"
      );
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] v2 = subPrefix.split(" ", 2);
      if (v2.length <= 1) {
         String v8 = v2[0].toLowerCase();
         ArrayList v9 = new ArrayList();
         if ("list".startsWith(v8)) {
            v9.add("list");
         }

         Client v10 = Client.get();

         v10.moduleManager().values().stream().map(Module::getName).filter(n -> {
            int v3x = -958148762;
            int var10001x = v3x * (v3x + 1);

            String var10000x = n.toLowerCase();

            return var10000x.startsWith(v8);
         }).forEach(v9::add);
         return v9;
      } else {
         String v3 = v2[0];
         String v4 = v2[1].toLowerCase();
         Stream result = KEY_SUGGESTIONS.stream();

         return result.filter(k -> {

            return k.startsWith(v4);
         }).map(k -> {
            StringBuilder var10000x = new StringBuilder;

            var10000x./* $VF: Unable to resugar constructor */<init>();
            var10000x = var10000x.append((Object)v3);

            return var10000x.append(" ").append((Object)k).toString();
         }).collect(Collectors.toList());
      }
   }

   
}
