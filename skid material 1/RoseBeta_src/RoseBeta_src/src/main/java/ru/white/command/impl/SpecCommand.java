package ru.white.command.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.IMinecraft;
import ru.white.utils.ChatUtil;
import ru.white.utils.media.Nr;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public class SpecCommand extends Command implements IMinecraft {
   private boolean f2 = true;
   private static final List<String> ACTIONS = List.of("off", "next", "prev", "info");
   private static final int CHECK_RADIUS = 2;

   private Entity m32(String query) {
      String v2 = query.toLowerCase(Locale.ROOT);

      for (AbstractClientPlayerEntity v4 : mc.field_1687.method_18456()) {
         if (v4 != mc.field_1724 && v4.method_5477().getString().equalsIgnoreCase(query)) {
            return v4;
         }
      }

      for (AbstractClientPlayerEntity v11 : mc.field_1687.method_18456()) {
         if (v11 != mc.field_1724 && v11.method_5477().getString().toLowerCase(Locale.ROOT).startsWith(v2)) {
            return v11;
         }
      }

      try {
         Entity v9 = mc.field_1687.method_8469(Integer.parseInt(query));
         if (v9 != null && v9 != mc.field_1724) {
            return v9;
         }
      } catch (NumberFormatException v7) {
      }

      Iterable result = mc.field_1687.method_18112();

      for (Entity v12 : result) {
         if (v12 != mc.field_1724 && v12.method_5477().getString().toLowerCase(Locale.ROOT).startsWith(v2)) {
            return v12;
         }
      }

      return null;
   }

   private void stop() {
      Entity v1 = nr.getTarget();
      nr.stop();
      String v7;
      if (v1 != null) {
         String v2 = v1.method_5477().getString();
         StringBuilder result = new StringBuilder().append(" от §f");

         v7 = result.append((Object)v2).toString();
      } else {
         v7 = "";
      }

      String v3 = v7;
      v7 = "§7Камера вернулась" + v3;

      zp.addChatMessage(v7);
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      nr.reset();

      this.f2 = true;
   }

   private String m31(Entity entity) {
      int v2 = nr.chunksInSquare(2);
      int v3 = nr.loadedChunksAround(entity, 2);

      String result;
      if (v3 == v2) {
         result = "§a";
      } else if (v3 == 0) {

         result = "§c";
      } else {
         result = "§e";
      }

      String v4 = result;
      StringBuilder v12 = new StringBuilder().append((Object)v4).append(v3).append("§7/§f");

      return v12.append(v2).append(" §7чанков").toString();
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (nr.validate()) {

         zp.addChatMessage("§7Цель пропала — камера вернулась");
         this.f2 = true;
      } else if (nr.isActive()) {
         Entity result = nr.getTarget();

         boolean v2 = nr.isTerrainLoaded(result);
         if (v2 != this.f2) {
            zp.addChatMessage(
               v2
                  ? "§aТерра вокруг цели прогрузилась"
                  : "§eТерра вокруг цели не прогружена — сервер не шлёт эти чанки"
            );
            this.f2 = v2;
         }
      }
   }

   public SpecCommand() {
      super(
         "spec",
         ".spec [ник|id|off|next|prev|info]",
         "Вселиться в сущность (только клиент)"
      );
      Client.eventHandler().subscribe(this);
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String v2 = subPrefix.toLowerCase(Locale.ROOT);
      Stream v3 = mc.field_1687 == null ? Stream.empty() : mc.field_1687.method_18456().stream().filter(p -> {
         boolean var10000x;
         if (p != mc.field_1724) {
            var10000x = true;

            
         } else {
            var10000x = false;
         }

         return var10000x;
      }).map(p -> {
         String var10000x = p.method_5477().getString();

         return var10000x;
      });
      Stream result = Stream.concat(v3, ACTIONS.stream());

      result = result.filter(s -> {
         String var10000x = s.toLowerCase(Locale.ROOT);

         return var10000x.startsWith(v2);
      });

      return result.distinct().collect(Collectors.toList());
   }

   private void m30(Entity entity) {
      if (entity == mc.field_1724) {
         zp.addChatMessage("§7В себя вселяться незачем");
      } else if (!nr.start(entity)) {
         zp.addChatMessage("§cНе получилось вселиться");
      } else {
         this.f2 = nr.isTerrainLoaded(entity);
         String result = entity.method_5477().getString();
         int temp1 = entity.method_5628();
         int temp2 = (int)Math.sqrt(mc.field_1724.method_5858(entity));
         String temp3 = this.m31(entity);

         String v2 = temp3;
         int v3 = temp2;
         int v4 = temp1;
         String v5 = result;
         StringBuilder v9 = new StringBuilder();

         StringBuilder v10 = v9.append("§7Вселился в §f")
            .append((Object)v5)
            .append(" §8[")
            .append(v4)
            .append("]§7, §f")
            .append(v3)
            .append("§7 блоков §8| §7терра: ")
            .append((Object)v2);

         zp.addChatMessage(v10.toString());
         if (!this.f2) {
            zp.addChatMessage(
               "§7Сервер не шлёт чанки в той точке — подгрузить их с клиента нельзя"
            );
         }
      }
   }

   private void m33(int direction) {
      ArrayList v2 = new ArrayList(mc.field_1687.method_18456().stream().filter(p -> {
         boolean var10000x;
         if (p != mc.field_1724) {
            var10000x = true;
            
         } else {
            var10000x = false;
         }

         return var10000x;
      }).sorted(Comparator.comparingDouble(p -> p.method_5858(mc.field_1724))).collect(Collectors.toList()));
      if (v2.isEmpty()) {
         zp.addChatMessage("§7Рядом никого нет");
      } else {
         int v3 = v2.indexOf(nr.getTarget());

         int v4 = v3 < 0 ? (direction > 0 ? 0 : v2.size() - 1) : Math.floorMod(v3 + direction, v2.size());
         this.m30((Entity)v2.get(v4));
      }
   }

   @Override
   public void execute(String[] args) {

      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (args.length != 0) {
            String v9 = args[0].toLowerCase(Locale.ROOT);
            byte v4 = -1;
            switch (v9.hashCode()) {
               case 109935:
                  if (v9.equals("off")) {
                     v4 = 0;
                  }
                  break;
               case 3127582:
                  if (v9.equals("exit")) {

                     v4 = 2;
                  }
                  break;
               case 3237038:
                  if (v9.equals("info")) {
                     v4 = 5;
                  }
                  break;
               case 3377907:
                  if (v9.equals("next")) {
                     v4 = 3;
                  }
                  break;
               case 3449395:
                  if (v9.equals("prev")) {
                     v4 = 4;
                  }
                  break;
               case 3540994:
                  if (v9.equals("stop")) {
                     v4 = 1;
                  }
            }

            switch (v4) {
               case 0:
               case 1:
               case 2:
                  if (!nr.isActive()) {
                     zp.addChatMessage("§7Ты никого не спектуешь");
                     return;
                  }

                  this.stop();
                  break;
               case 3:
                  this.m33(1);
                  break;
               case 4:
                  this.m33(-1);
                  break;
               case 5:
                  this.info();
                  break;
               default:
                  Entity v5 = this.m32(args[0]);
                  if (v5 == null) {
                     String v6 = args[0];
                     zp.addChatMessage("§cНе найдено: §r" + v6);
                     return;
                  }

                  this.m30(v5);
            }
         } else if (nr.isActive()) {
            this.stop();
         } else {
            Entity v2 = mc.field_1692;
            if (v2 != null && v2 != mc.field_1724) {
               this.m30(v2);
            } else {
               zp.addChatMessage(
                  "§7Наведись на сущность или укажи ник: §f.spec <ник>"
               );
            }
         }
      }
   }

   private void info() {
      Entity v1 = nr.getTarget();
      if (v1 == null) {
         zp.addChatMessage("§7Ты никого не спектуешь");
      } else {
         String result = v1.method_5477().getString();
         int v2 = v1.method_5628();
         String v3 = result;
         zp.addChatMessage("§7Цель: §f" + v3 + " §8[" + v2 + "]");
         int v10 = (int)Math.sqrt(mc.field_1724.method_5858(v1));

         String v4 = this.m31(v1);
         int v5 = v10;
         zp.addChatMessage(
            "§7Дистанция: §f"
               + v5
               + " §8| §7терра: "
               + v4
         );
         int v11 = mc.field_1687.method_2935().method_14151();
         int v6 = (int)mc.field_1769.method_34812();
         int v7 = v11;
         zp.addChatMessage(
            "§7Всего чанков у клиента: §f"
               + v7
               + " §8| §7прорисовка: §f"
               + v6
         );
      }
   }

   
}
