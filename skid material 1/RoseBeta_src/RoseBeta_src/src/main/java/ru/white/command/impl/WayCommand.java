package ru.white.command.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Zq;
import ru.white.utils.misc.Sp;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.theme.wo;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;
import ru.white.utils.ChatUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
public class WayCommand extends Command implements IMinecraft {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = Path.of("rose/client1_21_11/waypoints.json");
   private final Map<String, aa.ba> f4 = new ConcurrentHashMap<>();

   @Override
   public List<String> getSuggestions(String subPrefix) {
      String v2 = subPrefix.toLowerCase();
      if (!v2.startsWith("delete ") && !v2.startsWith("del ") && !v2.startsWith("remove ")) {
         return Stream.of("add", "delete", "clear", "list").filter(s -> {
            boolean result = s.startsWith(v2);
            int v2x = 2120905246;

            return result;
         }).toList();
      } else {
         String v3 = v2.substring(0, v2.indexOf(32));

         String v4 = v2.substring(v3.length() + 1);
         return this.f4.values().stream().filter(w -> {

            return w.name().toLowerCase().startsWith(v4);
         }).map(w -> {
            String v2x = w.name();
            return v3 + " " + v2x;
         }).toList();
      }
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.m39();
      } else {
         String v2 = args[0].toLowerCase();
         switch (v2) {
            case "add":
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.way add <имя> [x y z]");
                  return;
               }

               String v24 = args[1];
               Vec3d v25;
               if (args.length >= 5) {
                  try {
                     v25 = new Vec3d(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
                  } catch (NumberFormatException v22) {
                     zp.addChatMessage("§cОшибка: неверные координаты");
                     return;
                  }
               } else {
                  if (mc.field_1724 == null) {

                     zp.addChatMessage(
                        "§cВы не в мире, укажите координаты: §f.way add <имя> <x> <y> <z>"
                     );
                     return;
                  }

                  v25 = mc.field_1724.method_73189();
               }

               boolean v6 = this.f4.put(v24.toLowerCase(), new aa.ba(v24, v25)) != null;
               this.save();
               String v27 = v6
                  ? "§aВейпоинт обновлён: §f"
                  : "§aВейпоинт добавлен: §f";
               int v29 = (int)v25.field_1352;
               int temp3 = (int)v25.field_1351;
               int v7 = (int)v25.field_1350;
               int v8 = temp3;
               int v9 = v29;
               String v11 = v27;
               zp.addChatMessage(v11 + v24 + " §7(" + v9 + ", " + v8 + ", " + v7 + ")");
               break;
            case "delete":
            case "del":
            case "remove":
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.way delete <имя>");
                  return;
               }

               if (this.f4.remove(args[1].toLowerCase()) != null) {
                  this.save();
                  String v12 = args[1];
                  zp.addChatMessage("§aВейпоинт удалён: §f" + v12);
               } else {
                  String v13 = args[1];
                  StringBuilder v26 = new StringBuilder();

                  zp.addChatMessage(
                     v26.append("§cВейпоинт не найден: §f")
                        .append((Object)v13)
                        .toString()
                  );
               }
               break;
            case "clear":
               int v23 = this.f4.size();
               this.f4.clear();
               this.save();
               zp.addChatMessage("§aУдалено вейпоинтов: §f" + v23);
               break;
            case "list":
               if (this.f4.isEmpty()) {
                  zp.addChatMessage("§7Вейпоинтов нет");
                  return;
               }

               int v15 = this.f4.size();
               zp.addChatMessage("§7Вейпоинты (§f" + v15 + "§7):");

               for (aa.ba v5 : this.f4.values()) {
                  String result = v5.name();
                  int temp1 = (int)v5.pos().field_1352;
                  int temp2 = (int)v5.pos().field_1351;
                  int v16 = (int)v5.pos().field_1350;
                  int v17 = temp2;
                  int v18 = temp1;
                  String v19 = result;
                  zp.addChatMessage("§8• §f" + v19 + " §7— §f" + v18 + ", " + v17 + ", " + v16);
               }
               break;
            default:
               this.m39();
         }
      }
   }

   public WayCommand() {
      super(
         "way",
         ".way <add|delete|clear|list>",
         "Вейпоинты — метки на координатах"
      );
      this.load();
      Client.eventHandler().subscribe(this);
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.f4.isEmpty()) {
         if (!mc.field_1690.field_1842) {
            yt v2 = hu.sf_medium;
            float v3 = 8.0F;
            Vec3d v4 = mc.field_1773.method_19418().method_71156();
            float v5 = (float)Math.max(
               64, (Integer)mc.field_1690.method_42503().method_41753() * (16) - (16)
            );

            for (aa.ba v7 : this.f4.values()) {
               float v8 = (float)v4.method_1022(v7.pos());
               Vec3d v9 = v7.pos();
               boolean v10 = v8 > v5;
               if (v10) {
                  v9 = v4.method_1019(v7.pos().method_1020(v4).method_1029().method_1021((double)v5));
               }

               Vec3d v11 = zq.worldSpaceToScreenSpace(v9);
               if (!(v11.field_1350 <= 0.0) && !(v11.field_1350 >= 1.0)) {
                  String result = v7.name();
                  String temp1 = sp.getColor(wo.getDarkTextColor(1.0F));
                  int v17 = (int)v8;
                  String v18 = temp1;
                  String v19 = result;
                  String v12 = v19 + " " + v18 + v17 + "м";
                  float v13 = v2.getWidth(v12, v3) + 10.0F;
                  float v14 = 13.0F;

                  float v15 = (float)v11.field_1352 - v13 / 2.0F;
                  float v16 = (float)v11.field_1351 - v14 - 6.0F;

                  vs.ws.blur(v15, v16, v13, v14, 1.0F, 4.0F, up.getRectColor(1.0F));
                  vs.ys.outline(v15, v16, v13, v14, 0.5F, wo.getOutlineColor(1.0F), 4.0F);
                  float temp2 = v15 + v13 / 2.0F;

                  v2.drawCentered(v12, temp2, v16 + 1.8F, v3, wo.getTextColor(1.0F));
                  if (!v10) {
                     vs.ys.rect((float)v11.field_1352 - 1.5F, (float)v11.field_1351 - 1.5F, 3.0F, 3.0F, up.getClientColor1(1), 1.5F);
                  }
               }
            }
         }
      }
   }

   private void save() {
      JsonArray v1 = new JsonArray();

      for (aa.ba v3 : this.f4.values()) {
         JsonObject v4 = new JsonObject();
         v4.addProperty("name", v3.name());

         v4.addProperty("x", v3.pos().field_1352);
         v4.addProperty("y", v3.pos().field_1351);
         v4.addProperty("z", v3.pos().field_1350);
         v1.add(v4);
      }

      try {
         Files.createDirectories(FILE.getParent());

         try (OutputStreamWriter v9 = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(v1, v9);
         }
      } catch (IOException v8) {
         v8.printStackTrace();
      }
   }

   private void m39() {
      zp.addChatMessage(
         "§7.way §fadd §7<имя> [x y z] §8| §7.way §fdelete §7<имя> §8| §7.way §fclear §8| §7.way §flist"
      );

      
   }

   private void load() {
      if (Files.exists(FILE)) {
         try (InputStreamReader v1 = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            JsonArray v2 = JsonParser.parseReader(v1).getAsJsonArray();
            this.f4.clear();

            for (JsonElement v4 : v2) {
               JsonObject v5 = v4.getAsJsonObject();
               String v6 = v5.get("name").getAsString();
               Vec3d v7 = new Vec3d(v5.get("x").getAsDouble(), v5.get("y").getAsDouble(), v5.get("z").getAsDouble());
               this.f4.put(v6.toLowerCase(), new aa.ba(v6, v7));
            }
         } catch (Exception v11) {

            v11.printStackTrace();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static record ba(String name, Vec3d pos) {
      private final Vec3d f3;

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",aa.ba,"name;pos",aa.ba::name,aa.ba::f3>(this);
      }

      private ba(String name, Vec3d pos) {
         this.name = name;
         this.f3 = pos;
      }

      public Vec3d pos() {
         return this.f3;
      }

      @Override
      public final 

         return ObjectMethods.bootstrap<"hashCode",aa.ba,"name;pos",aa.ba::name,aa.ba::f3>(this);
      }

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",aa.ba,"name;pos",aa.ba::name,aa.ba::f3>(this, o);

         return result;
      }

      public String name() {

         return this.name;
      }

      
   }
}
