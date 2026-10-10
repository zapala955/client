package ru.white.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventManager;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.event.impl.EventSync;

@Environment(EnvType.CLIENT)
public class FriendManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = Path.of("rose/client1_21_11/friends.json");
   private final Set<String> f23 = new HashSet<>();

   public void init() {
      this.load();
      EventManager result = Client.eventHandler();

      result.subscribe(this);
   }

   public Set<String> getFriends() {

      return Collections.unmodifiableSet(this.f23);
   }

   private void save() {
      JsonArray v1 = new JsonArray();
      this.f23.forEach(v1::add);

      try {
         Files.createDirectories(FILE.getParent());

         try (OutputStreamWriter v2 = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(v1, v2);
         }
      } catch (IOException v9) {

         v9.printStackTrace();
      }
   }

   public boolean remove(String name) {

      boolean v2 = this.f23.remove(name.toLowerCase());
      if (v2) {
         this.save();
      }

      return v2;
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {

      this.load();
   }

   public boolean add(String name) {
      boolean v2 = this.f23.add(name.toLowerCase());
      if (v2) {

         this.save();
      }

      return v2;
   }

   public boolean isFriend(String name) {
      boolean result = this.f23.contains(name.toLowerCase());

      return result;
   }

   private void load() {
      if (Files.exists(FILE)) {
         try (InputStreamReader v1 = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            JsonArray v2 = JsonParser.parseReader(v1).getAsJsonArray();
            this.f23.clear();
            v2.forEach(el -> this.f23.add(el.getAsString().toLowerCase()));
         } catch (IOException v7) {
            v7.printStackTrace();
         }
      }
   }

   
}
