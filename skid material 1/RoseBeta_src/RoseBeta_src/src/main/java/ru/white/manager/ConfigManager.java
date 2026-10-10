package ru.white.manager;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventManager;
import ru.white.event.EventTarget;
import ru.white.module.setting.MatrixSetting;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.StringSetting;
import ru.white.utils.misc.Of;
import ru.white.utils.misc.Pf;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Rf;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Vf;
import ru.white.module.setting.ModeSetting;
import ru.white.utils.misc.Yf;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.setting.Setting;
import ru.white.module.impl.misc.UnHook;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public class ConfigManager {
   public static final String AUTO_CONFIG = "auto";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_DIR = Path.of("rose/client1_21_11/config");
   private int f5 = 0;

   public void save(String name) {
      JsonObject v2 = new JsonObject();
      JsonObject v3 = new JsonObject();
      Iterator v4 = Client.get().moduleManager().values().iterator();

      while (v4.hasNext()) {

         Module v5 = (Module)v4.next();

         JsonObject v6 = new JsonObject();
         v6.addProperty("enabled", v5.isEnabled());
         v6.addProperty("key", v5.getKey());
         JsonObject v7 = new JsonObject();

         for (Setting v9 : v5.getSettings()) {
            this.m43(v7, v9);
         }

         v6.add("settings", v7);
         v3.add(v5.getName(), v6);
      }

      v2.add("modules", v3);
      Path v16 = CONFIG_DIR.resolve(name + ".json");

      try (OutputStreamWriter v17 = new OutputStreamWriter(new FileOutputStream(v16.toFile()), StandardCharsets.UTF_8)) {
         GSON.toJson(v2, v17);
      } catch (IOException v15) {
         v15.printStackTrace();
      }
   }

   private void m43(JsonObject obj, Setting<?> setting) {
      if (setting instanceof vf v14) {
         JsonObject v18 = new JsonObject();
         v18.addProperty("x", v14.position.x);
         v18.addProperty("y", v14.position.y);
         obj.add(setting.getName(), v18);
      } else if (setting instanceof MultiSetting v13) {
         JsonObject v17 = new JsonObject();
         Iterator result = v13.getValues().iterator();

         Iterator v21 = result;

         while (v21.hasNext()) {
            BooleanSetting v24 = (BooleanSetting)v21.next();

            v17.addProperty(v24.getName().toLowerCase(), v24.getValue());
         }

         obj.add(setting.getName(), v17);
      } else if (setting instanceof pf v12) {
         JsonArray v16 = new JsonArray();

         for (String v23 : v12.getIds()) {
            v16.add(v23);
         }

         obj.add(setting.getName(), v16);
      } else {

         if (!(setting instanceof MatrixSetting v3)) {
            Object v11 = setting.getValue();
            if (v11 instanceof Boolean v15) {
               obj.addProperty(setting.getName(), v15);
            } else if (v11 instanceof Float v19) {
               obj.addProperty(setting.getName(), v19);
            } else if (v11 instanceof String v22) {
               obj.addProperty(setting.getName(), v22);
            } else if (v11 instanceof Integer v7) {
               obj.addProperty(setting.getName(), v7);
            }
         } else {
            JsonArray v4 = new JsonArray();

            for (String v6 : v3.toRows()) {
               v4.add(v6);
            }

            obj.add(setting.getName(), v4);
         }
      }
   }

   public Path getConfigDir() {

      return CONFIG_DIR;
   }

   public void autoSave() {
      if (!pm.unhooked) {

         this.save("auto");
      }
   }

   public void init() {

      this.load("auto");
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
   }

   @EventTarget
   public void onTick(EventRenderModel event) {

      int result = ++this.f5;

      if (result >= 1200) {
         this.f5 = 0;
         if (!pm.unhooked) {
            this.save("auto");
         }
      }
   }

   public boolean load(String name) {
      Path v2 = CONFIG_DIR.resolve(name + ".json");
      if (!Files.exists(v2)) {
         return false;
      } else {
         try {
            boolean v19;
            try (InputStreamReader v3 = new InputStreamReader(new FileInputStream(v2.toFile()), StandardCharsets.UTF_8)) {
               JsonObject v4 = JsonParser.parseReader(v3).getAsJsonObject();

               JsonObject v5 = v4.getAsJsonObject("modules");

               for (Module v7 : Client.get().moduleManager().values()) {
                  if (v5.has(v7.getName())) {
                     JsonObject v8 = v5.getAsJsonObject(v7.getName());
                     if (v8.has("enabled")) {
                        v7.setEnabled(v8.get("enabled").getAsBoolean(), false);
                     }

                     if (v8.has("key")) {
                        v7.setKey(v8.get("key").getAsInt());
                     }

                     if (v8.has("settings")) {
                        JsonObject v9 = v8.getAsJsonObject("settings");
                        if (v7 instanceof ConfigFile v10) {
                           v10.onPreLoad(v9);
                        }

                        for (Setting v11 : v7.getSettings()) {
                           try {
                              this.m44(v9, v11);
                           } catch (Exception v16) {
                           }
                        }

                        if (v7 instanceof ConfigFile v21) {
                           v21.onPostLoad(v9);
                        }
                     }
                  }
               }

               v19 = true;
            }

            return v19;
         } catch (IOException v18) {
            v18.printStackTrace();
            return false;
         }
      }
   }

   private void m44(JsonObject obj, Setting<?> setting) {
      if (obj.has(setting.getName())) {
         JsonElement v3 = obj.get(setting.getName());
         if (setting instanceof vf v4 && v3.isJsonObject()) {
            JsonObject v25 = v3.getAsJsonObject();

            v4.position.set(v25.get("x").getAsFloat(), v25.get("y").getAsFloat());
            v4.targetPosition.set(v4.position);
            return;
         }

         if (setting instanceof MultiSetting v5 && v3.isJsonObject()) {
            JsonObject v24 = v3.getAsJsonObject();

            for (BooleanSetting v29 : v5.getValues()) {
               String v19 = v29.getName().toLowerCase();
               if (v24.has(v19)) {
                  v29.set(v24.get(v19).getAsBoolean());
               }
            }

            return;
         }

         if (setting instanceof pf v6 && v3.isJsonArray()) {
            v6.getIds().clear();

            for (JsonElement v26 : v3.getAsJsonArray()) {
               String v28 = v26.getAsString();
               if (!v6.getIds().contains(v28)) {
                  v6.getIds().add(v28);
               }
            }

            return;
         }

         if (setting instanceof MatrixSetting v7 && v3.isJsonArray()) {
            ArrayList v16 = new ArrayList();

            for (JsonElement v18 : v3.getAsJsonArray()) {
               v16.add(v18.getAsString());
            }

            v7.fromRows(v16);
            return;
         }

         if (setting instanceof BooleanSetting v8) {
            v8.set(v3.getAsBoolean());
         } else if (setting instanceof rf v9) {
            v9.set(v3.getAsBoolean());
         } else if (setting instanceof SliderSetting v10) {
            v10.set(v3.getAsFloat());
         } else if (setting instanceof ModeSetting v11) {
            v11.set(v3.getAsString());
         } else if (setting instanceof yf v12) {
            v12.set(v3.getAsString());
         } else if (setting instanceof SettingVisibility v13) {
            try {
               v13.set(v3.getAsInt());
            } catch (Exception v21) {
            }
         } else if (setting instanceof tf v14) {
            try {
               if (v3.isJsonPrimitive() && v3.getAsJsonPrimitive().isNumber()) {
                  v14.set(v3.getAsInt());
               } else if (v3.isJsonPrimitive()) {
                  v14.set((int)Long.parseLong(v3.getAsString()));
               }
            } catch (Exception v22) {
            }
         } else if (setting instanceof StringSetting v15) {
            v15.set(v3.getAsString());
         }
      }
   }

   public void setup() {
      try {
         Files.createDirectories(CONFIG_DIR);
      } catch (IOException v5) {
         v5.printStackTrace();
      }

      EventManager result = Client.eventHandler();
      int temp2 = v2 * (v2 + 1) % 2;

      if (temp2 != 0) {

         }

      result.subscribe(this);
   }

   public List<String> listConfigs() {
      ArrayList v1 = new ArrayList();

      try (DirectoryStream v2 = Files.newDirectoryStream(CONFIG_DIR, "*.json")) {
         Iterator v3 = v2.iterator();

         while (v3.hasNext()) {

            Path v4 = (Path)v3.next();

            String v5 = v4.getFileName().toString();
            v1.add(v5.substring(0, v5.length() - 5));
         }
      } catch (IOException v11) {
         v11.printStackTrace();
      }

      return v1;
   }

   
}
