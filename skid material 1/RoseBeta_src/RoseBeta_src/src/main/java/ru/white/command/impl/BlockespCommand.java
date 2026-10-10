package ru.white.command.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;
import ru.white.command.Command;
import ru.white.module.impl.render.BlockESP;
import ru.white.utils.ChatUtil;

@Environment(EnvType.CLIENT)
public class BlockespCommand extends Command {
   public BlockespCommand() {
      super(
         "blockesp",
         ".blockesp <add|remove|list|clear> [блок]",
         "Управление Block ESP"
      );
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 1) {
         this.m20();
      } else {
         ui v2 = ui.getInstance();
         if (v2 == null) {
            zp.addChatMessage("§cBlock ESP модуль не найден");
         } else {
            String v3 = args[0].toLowerCase();
            byte v4 = -1;
            switch (v3.hashCode()) {
               case -934610812:
                  if (v3.equals("remove")) {
                     v4 = 1;
                  }
                  break;
               case 96417:
                  if (v3.equals("add")) {
                     v4 = 0;
                  }
                  break;
               case 3322014:
                  if (v3.equals("list")) {
                     v4 = 2;
                  }
                  break;
               case 94746189:
                  if (v3.equals("clear")) {

                     v4 = 3;
                  }
            }

            switch (v4) {
               case 0:
                  if (args.length < 2) {
                     zp.addChatMessage("§7Использование: .blockesp add §f<блок>");
                     return;
                  }

                  Block v18 = this.m19(args[1]);
                  if (v18 == null) {
                     String v6 = args[1];
                     zp.addChatMessage("§cБлок §f" + v6 + "§c не найден");
                     return;
                  }

                  if (v2.addBlock(v18)) {
                     v2.rescan();
                     String v7 = args[1];
                     zp.addChatMessage("§a" + v7 + "§r добавлен в Block ESP");
                  } else {
                     String v8 = args[1];
                     zp.addChatMessage("§7" + v8 + " уже добавлен");
                  }
                  break;
               case 1:
                  if (args.length < 2) {
                     zp.addChatMessage(
                        "§7Использование: .blockesp remove §f<блок>"
                     );
                     return;
                  }

                  Block v17 = this.m19(args[1]);
                  if (v17 == null) {
                     String v9 = args[1];
                     zp.addChatMessage("§cБлок §f" + v9 + "§c не найден");
                     return;
                  }

                  if (v2.removeBlock(v17)) {
                     String v10 = args[1];
                     zp.addChatMessage("§c" + v10 + "§r удалён из Block ESP");
                  } else {
                     String v11 = args[1];
                     zp.addChatMessage("§7" + v11 + " не был добавлен");
                  }
                  break;
               case 2:
                  if (v2.getTargetBlocks().isEmpty()) {
                     zp.addChatMessage("§7Список блоков пуст");
                  } else {
                     String v16 = v2.getTargetBlocks().stream().map(b -> {
                        Identifier result = Registries.field_41175.method_10221(b);
                        int temp2 = v1 + 1;

                        return result.method_12832();
                     }).collect(Collectors.joining("§7, §a"));
                     int v13 = v2.getTargetBlocks().size();
                     zp.addChatMessage("§7Блоки §8[" + v13 + "§8]§r: §a" + v16);
                  }
                  break;
               case 3:
                  int v5 = v2.clearBlocks();
                  zp.addChatMessage(
                     "§7Удалено §f"
                        + v5
                        + "§7 блоков из Block ESP"
                  );
                  break;
               default:
                  this.m20();
            }
         }
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String[] v2 = subPrefix.split(" ", 2);
      String v3 = v2[0].toLowerCase();
      if (v2.length <= 1) {
         return List.of("add", "remove", "list", "clear").stream().filter(s -> {
            boolean var10000x = s.startsWith(v3);

            return var10000x;
         }).collect(Collectors.toList());
      } else if (!v3.equals("add") && !v3.equals("remove")) {
         return List.of();
      } else {
         String v4 = v2[1].toLowerCase();
         ArrayList v5 = new ArrayList();
         Iterator v6 = Registries.field_41175.method_10235().iterator();

         while (true) {
            boolean result = v6.hasNext();

            if (!result) {
               break;
            }

            Identifier v7 = (Identifier)v6.next();
            String v8 = v7.method_12832();
            if (v8.startsWith(v4)) {
               v5.add(v3 + " " + v8);
               if (v5.size() >= 30) {
                  break;
               }
            }
         }

         return v5;
      }
   }

   private void m20() {
      zp.addChatMessage(
         "§7.blockesp add/remove §f<блок> §8| §7.blockesp list §8| §7.blockesp clear"
      );
      int result = v1 * (v1 + 1);

      
   }

   private Block m19(String name) {
      String v2 = name.toLowerCase();
      Identifier v3 = Identifier.method_12829(v2.contains(":") ? v2 : "minecraft:" + v2);
      if (v3 != null && Registries.field_41175.method_10250(v3)) {

         Block v4 = (Block)Registries.field_41175.method_63535(v3);
         if (v4 == Blocks.field_10124 && !v2.equals("air")) {

            if (!v2.equals("minecraft:air")) {
               return null;
            }
         }

         return v4;
      } else {
         return null;
      }
   }

   
}
