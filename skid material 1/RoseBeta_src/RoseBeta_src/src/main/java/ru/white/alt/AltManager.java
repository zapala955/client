package ru.white.alt;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import ru.white.auth.StrDec;
import ru.white.mixin.MinecraftClientAccessor;

@Environment(EnvType.CLIENT)
public class AltManager {
   private static AltManager instance;
   private String activeName;
   private static final Path FILE = Path.of("rose/client1_21_11/alts.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private boolean booted;
   private final List<o> accounts = new ArrayList<>();

   public boolean exists(String name) {
      Stream result = this.accounts.stream();
      Predicate temp1 = a -> {

         return a.name.equalsIgnoreCase(name);
      };

      return result.anyMatch(temp1);
   }

   public void login(o account) {
      if (account == null) {
         
      } else {
         this.applySession(account);
         this.activeName = account.name;
         this.save();
      }
   }

   public String randomName(String base) {
      base = base == null ? "" : base.trim();
      StringBuilder v2 = new StringBuilder();

      for (char v6 : base.toCharArray()) {
         if (Character.isLetterOrDigit(v6) || v6 == 95) {
            v2.append(v6);
         }
      }

      if (v2.length() > 12) {
         v2.setLength(12);
         
      }

      base = v2.toString();
      String v16 = "abcdefghijklmnopqrstuvwxyz0123456789";
      Random v17 = new Random();

      for (int v18 = 0; v18 < 60; v18++) {
         int v19 = Math.max(1, Math.min(12, 16 - base.length()));
         int v7 = Math.min(4, v19);
         int v8 = v7 + v17.nextInt(v19 - v7 + 1);
         StringBuilder v9 = new StringBuilder(base);

         for (int v10 = 0; v10 < v8; v10++) {
            v9.append(
               "abcdefghijklmnopqrstuvwxyz0123456789"
                  .charAt(v17.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length()))
            );
         }

         String v20 = v9.toString();
         if (!v20.isEmpty() && v20.length() <= 16 && !this.exists(v20)) {
            return v20;
         }
      }

      String v21 = base.isEmpty() ? "Player" : base;
      int v11 = v17.nextInt(10000);
      String v12 = v21;
      return v12 + v11;
   }

   public List<o> getAccounts() {

      return this.accounts;
   }

   public void toggleFavorite(o account) {
      if (account != null) {
         account.favorite = !account.favorite;
         this.save();
         
      }
   }

   public o add(String name) {
      name = name == null ? "" : name.trim();

      if (!name.isEmpty() && !this.exists(name)) {
         o v2 = new o(name);
         this.accounts.add(v2);
         this.save();
         return v2;
      } else {
         return null;
      }
   }

   public AltManager() {
      this.activeName = null;
      this.booted = false;
   }

   public static AltManager get() {
      if (instance == null) {
         instance = new AltManager();
         instance.load();
      }

      return instance;
   }

   public void load() {
      this.accounts.clear();
      if (Files.exists(FILE)) {
         try {
            InputStreamReader result = new InputStreamReader;

            result./* $VF: Unable to resugar constructor */<init>(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8);

            try (InputStreamReader v1 = result) {
               JsonObject v2 = JsonParser.parseReader(v1).getAsJsonObject();
               boolean v17 = v2.has("active");

               if (v17) {
                  this.activeName = v2.get("active").getAsString();
               }

               if (v2.has("accounts")) {
                  for (JsonElement v4 : v2.getAsJsonArray("accounts")) {
                     JsonObject v5 = v4.getAsJsonObject();
                     String v6 = v5.has("name") ? v5.get("name").getAsString() : null;
                     if (v6 != null && !v6.isBlank()) {
                        o.p v7 = o.p.OFFLINE;
                        if (v5.has("type")) {
                           try {
                              v7 = o.p.valueOf(v5.get("type").getAsString());
                           } catch (IllegalArgumentException v14) {
                           }
                        }

                        long v8 = v5.has("created") ? v5.get("created").getAsLong() : System.currentTimeMillis();
                        o v10 = new o(v6, v7, v8);
                        if (v5.has("favorite")) {
                           v10.favorite = v5.get("favorite").getAsBoolean();
                        }

                        this.accounts.add(v10);
                     }
                  }

                  return;
               }
            }
         } catch (Exception v16) {
            v16.printStackTrace();
         }
      }
   }

   public String randomName() {

      return this.randomName("");
   }

   public String currentName() {
      String result = MinecraftClient.method_1551().method_1548().method_1676();

      return result;
   }

   public void save() {
      JsonObject v1 = new JsonObject();
      JsonArray v2 = new JsonArray();

      for (o v4 : this.accounts) {
         JsonObject result = new JsonObject;

         result./* $VF: Unable to resugar constructor */<init>();
         JsonObject v5 = result;
         v5.addProperty("name", v4.name);
         v5.addProperty("type", v4.type.name());
         v5.addProperty("created", v4.created);
         v5.addProperty("favorite", v4.favorite);
         v2.add(v5);
      }

      v1.add("accounts", v2);
      if (this.activeName != null) {
         v1.addProperty("active", this.activeName);
      }

      try {
         Files.createDirectories(FILE.getParent());

         try (OutputStreamWriter v10 = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(v1, v10);
         }
      } catch (IOException v9) {
         v9.printStackTrace();
      }
   }

   public void bootstrap() {
      if (!this.booted) {
         MinecraftClient v1 = MinecraftClient.method_1551();
         if (v1 != null) {
            if (v1.method_1548() != null) {
               this.booted = true;
               if (this.activeName == null) {
                  return;
               }

               for (Object v6 : this.accounts) {

                  o v3 = (o)v6;
                  if (v3.name.equalsIgnoreCase(this.activeName)) {
                     this.applySession(v3);
                     return;
                  }
               }

               return;
            }

            
         }
      }
   }

   public void remove(o account) {

      if (this.accounts.remove(account)) {
         int result = v3 * (v3 + 1);

         this.save();
      }
   }

   private void applySession(o account) {
      MinecraftClient v2 = MinecraftClient.method_1551();
      if (v2 != null) {
         Session result = new Session;

         result./* $VF: Unable to resugar constructor */<init>(account.name, account.offlineUuid(), "0", Optional.empty(), Optional.empty());
         Session v3 = result;
         ((MinecraftClientAccessor)v2).setSession(v3);
      }
   }

   public List<o> getSorted() {
      ArrayList v1 = new ArrayList();

      for (o v3 : this.accounts) {

         if (v3.favorite) {
            v1.add(v3);
         }
      }

      for (o v7 : this.accounts) {
         if (!v7.favorite) {
            v1.add(v7);
         }
      }

      return v1;
   }

   
}
