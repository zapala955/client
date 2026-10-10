package ru.white.theme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.ClickGui;

@Environment(EnvType.CLIENT)
public class xo {
   private Theme active;
   private static final Path THEME_DIR = Path.of("rose/client1_21_11/theme");
   public static final int MAX_THEMES = 9;
   private final List<Theme> f1249 = new ArrayList<>();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static xo instance;
   private static final Path ACTIVE_FILE = THEME_DIR.resolve(".active");

   private String m1480() {
      try {
         int v2 = -708229480;
         int temp2 = v2 * (v2 + 1);

         if (Files.exists(ACTIVE_FILE)) {
            return new String(Files.readAllBytes(ACTIVE_FILE), StandardCharsets.UTF_8).trim();
         }
      } catch (IOException v4) {
      }

      return null;
   }

   public void deleteTheme(Theme theme) {
      if (theme != null && !theme.builtin && this.f1249.size() > 1) {
         this.f1249.remove(theme);

         try {
            String v3 = theme.name;
            Files.deleteIfExists(THEME_DIR.resolve(v3 + ".json"));
         } catch (IOException v6) {
         }

         if (this.active == theme) {
            this.active = this.f1249.get(0);
            this.m1479();
         }
      }
   }

   private static String m1486(String fileName) {

      int v1 = fileName.lastIndexOf(46);
      return v1 > 0 ? fileName.substring(0, v1) : fileName;
   }

   public void renameTheme(Theme theme, String newName) {
      if (theme != null && !theme.builtin) {
         newName = newName == null ? "" : newName.trim();
         if (!newName.isEmpty() && !newName.equalsIgnoreCase(theme.name)) {
            try {
               Path result = THEME_DIR;
               String v4 = theme.name;
               StringBuilder temp1 = new StringBuilder();

               Files.deleteIfExists(result.resolve(temp1.append((Object)v4).append(".json").toString()));
            } catch (IOException v8) {
            }

            theme.name = this.m1481(newName, theme);
            this.saveTheme(theme);
            if (this.active == theme) {
               this.m1479();
            }
         }
      }
   }

   private String m1481(String base, Theme exclude) {
      if (base == null || base.isBlank()) {
         base = "Theme";
      }

      base = base.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
      if (base.isEmpty()) {
         base = "Theme";
      }

      String v3 = base;
      int v4 = 1;

      while (this.m1482(v3, exclude)) {
         String result = base + " " + ++v4;

         v3 = result;
      }

      return v3;
   }

   public void syncWithClickGui() {
      try {
         if (Client.get() != null && Client.get().moduleManager() != null) {
            ClickGui v1 = Client.get().moduleManager().get(ClickGui.class);
            if (v1 != null && this.active != null) {
               if (v1.distortion != null) {
                  v1.distortion.set(this.active.getBool(1));
               }

               if (v1.blurStrength != null) {

                  float v2 = this.active.getSlider(1);
                  if (v2 > 0.0F) {

                     v1.blurStrength.set(MathHelper.method_15363(v2 * 5.0F, 5.0F, 30.0F));
                  }
               }
            }
         }
      } catch (Exception v6) {
      }
   }

   private boolean m1482(String name, Theme exclude) {
      Stream result = this.f1249.stream();

      return result.anyMatch(t -> {
         if (t != exclude) {

            if (t.name.equalsIgnoreCase(name)) {
               return true;
            }
         }

         return false;
      });
   }

   public List<Theme> getThemes() {
      return this.f1249;
   }

   public static xo get() {
      if (instance == null) {
         xo result = new xo;
         int v0 = 1194536487;

         result./* $VF: Unable to resugar constructor */<init>();
         instance = result;
         instance.load();
      }

      return instance;
   }

   public Theme createTheme(String name) {
      if (!this.canCreate()) {
         return this.getActive();
      } else {
         name = this.m1481(name, null);
         Theme v2 = this.getActive();
         Theme v3 = v2.copy(name);
         this.f1249.add(v3);
         int v4 = -2042359369;
         int temp1 = v4 * (v4 + 1);

         this.setActive(v3);
         this.saveTheme(v3);
         return v3;
      }
   }

   public void saveActive() {

      this.saveTheme(this.getActive());
   }

   private static Theme m1477(String name, int visual, int hud) {
      Theme v3 = new Theme(name);
      v3.builtin = true;
      v3.colors[0] = visual;
      v3.colors[5] = hud;
      v3.colors[3] = Theme.rgb(255, 255, 255);

      v3.colors[4] = Theme.rgb(177, 177, 177);
      v3.colors[1] = Theme.rgb(15, 15, 15);
      v3.colors[2] = up.getColor(255, 255, 255, 15);

      v3.colors[6] = up.getColor(255, 255, 255, 15);
      int v8 = 7;

      v3.colors[v8] = up.getColor(255, 255, 255, 10);
      v3.setSlider(0, 0.9F);
      v3.setSlider(1, 4.0F);
      v3.setBool(0, true);
      v3.setBool(1, false);
      return v3;
   }

   private Theme m1478(Path path) {
      try {
         Theme v14;
         try (InputStreamReader v2 = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
            JsonObject v3 = JsonParser.parseReader(v2).getAsJsonObject();
            String v4 = v3.has("name") ? v3.get("name").getAsString() : m1486(path.getFileName().toString());
            Theme v5 = new Theme(v4);

            for (int v6 = 0; v6 < 8; v6++) {
               String v7 = m1483(v6);
               if (v3.has(v7)) {
                  v5.colors[v6] = v3.get(v7).getAsInt();
               }
            }

            for (int v12 = 0; v12 < v5.sliders.length; v12++) {
               String v15 = m1484(v12);
               if (v3.has(v15)) {
                  v5.setSlider(v12, v3.get(v15).getAsFloat());
               }
            }

            for (int v13 = 0; v13 < v5.booleans.length; v13++) {
               String v16 = m1485(v13);
               if (v3.has(v16)) {
                  v5.setBool(v13, v3.get(v16).getAsBoolean());
               }
            }

            v14 = v5;
         }

         return v14;
      } catch (Exception v11) {
         v11.printStackTrace();
         return null;
      }
   }

   public boolean canCreate() {
      int result = this.f1249.size();

      return result < 9;
   }

   public void setActive(Theme theme) {
      if (theme != null) {
         this.active = theme;
         this.m1479();
         this.syncWithClickGui();
      }
   }

   private static String m1484(int i) {
      return switch (i) {
         case 0 -> "opacity";
         case 1 -> "blur";
         default -> {
            StringBuilder result = new StringBuilder().append("s");

            yield result.append(i).toString();
         }
      };
   }

   public void saveTheme(Theme theme) {
      if (theme != null && !theme.builtin) {
         try {
            Files.createDirectories(THEME_DIR);
            JsonObject v2 = new JsonObject();
            v2.addProperty("name", theme.name);

            for (int v3 = 0; v3 < 8; v3++) {
               String temp1 = m1483(v3);

               v2.addProperty(temp1, theme.colors[v3]);
            }

            for (int v14 = 0; v14 < theme.sliders.length; v14++) {
               v2.addProperty(m1484(v14), theme.sliders[v14]);
            }

            for (int v15 = 0; v15 < theme.booleans.length; v15++) {

               v2.addProperty(m1485(v15), theme.booleans[v15]);
            }

            Path result = THEME_DIR;
            String v7 = theme.name;
            StringBuilder v18 = new StringBuilder;

            v18./* $VF: Unable to resugar constructor */<init>();
            Path v16 = result.resolve(v18.append((Object)v7).append(".json").toString());

            try (OutputStreamWriter v4 = new OutputStreamWriter(new FileOutputStream(v16.toFile()), StandardCharsets.UTF_8)) {
               GSON.toJson(v2, v4);
            }
         } catch (IOException v13) {
            v13.printStackTrace();
         }
      }
   }

   public Theme getCustomTheme() {
      Theme v1 = this.f1249.stream().filter(t -> {
         if (!t.name.equalsIgnoreCase("Custom")) {

            if (t.builtin) {
               return false;
            }
         }

         return true;
      }).findFirst().orElse(null);
      if (v1 == null) {
         Theme result = new Theme;

         result./* $VF: Unable to resugar constructor */<init>("Custom");
         v1 = result;
         v1.builtin = false;
         v1.colors[0] = Theme.rgb(246, 124, 180);
         v1.colors[5] = Theme.rgb(246, 124, 180);
         v1.colors[1] = Theme.rgb(15, 15, 18);
         v1.colors[7] = up.getColor(255, 255, 255, 10);

         v1.colors[2] = up.getColor(255, 255, 255, 15);
         v1.colors[6] = up.getColor(255, 255, 255, 15);
         v1.colors[3] = Theme.rgb(255, 255, 255);
         v1.colors[4] = Theme.rgb(177, 177, 177);
         v1.setSlider(0, 0.9F);
         v1.setSlider(1, 4.0F);
         v1.setBool(0, true);
         v1.setBool(1, false);
         this.f1249.add(v1);
         this.saveTheme(v1);
      }

      return v1;
   }

   private void m1479() {
      try {
         Path result = THEME_DIR;
         FileAttribute[] temp1 = new FileAttribute[0];

         Files.createDirectories(result, temp1);
         result = ACTIVE_FILE;
         Theme v7 = this.getActive();
         int v3 = 1537059509;

         Files.write(result, v7.name.getBytes(StandardCharsets.UTF_8));
      } catch (IOException v5) {
      }
   }

   private static String m1483(int i) {
      String result;
      switch (i) {
         case 0:
            result = "accent";
            break;
         case 1:
            result = "background";
            break;
         case 2:
            result = "outline";
            break;
         case 3:
            result = "text";
            break;
         case 4:
            result = "text2";
            break;
         case 5:
            result = "element";
            break;
         case 6:
            result = "separator";
            int v2 = -1143632898;

            
            break;
         case 7:
            result = "lightbg";
            break;
         default:
            result = "c" + i;
      }

      return result;
   }

   private static String m1485(int i) {
      return switch (i) {
         case 0 -> "shadow";
         case 1 -> "distortion";
         default -> {
            StringBuilder result = new StringBuilder();
            int v2 = 555859108;
            int v3 = -1856526054;

            yield result.append("b").append(i).toString();
         }
      };
   }

   public Theme getActive() {
      if (this.active == null) {
         Theme temp1 = this.getCustomTheme();

         this.active = temp1;
      }

      return this.active;
   }

   public void load() {
      this.f1249.clear();
      List result = this.f1249;
      Theme temp1 = m1477(
         "Blue",
         Theme.rgb(124, 143, 246),
         Theme.rgb(143, 161, 250)
      );

      result.add(temp1);

      this.f1249
         .add(
            m1477(
               "Red",
               Theme.rgb(246, 124, 126),
               Theme.rgb(246, 124, 126)
            )
         );

      try {
         Files.createDirectories(THEME_DIR);

         try (DirectoryStream v1 = Files.newDirectoryStream(THEME_DIR, "*.json")) {
            for (Path v3 : v1) {
               Theme v12 = this.m1478(v3);

               Theme v4 = v12;
               if (v4 != null && !this.m1482(v4.name, null)) {
                  this.f1249.add(v4);
               }
            }
         }
      } catch (IOException v10) {
         v10.printStackTrace();
      }

      this.getCustomTheme();
      String v11 = this.m1480();
      this.active = this.f1249.stream().filter(t -> t.name.equals(v11)).findFirst().orElse(this.getCustomTheme());
   }

   
}
