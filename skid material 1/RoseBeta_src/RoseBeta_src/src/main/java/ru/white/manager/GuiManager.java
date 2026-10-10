package ru.white.manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class GuiManager {
   private Theme f57 = Theme.NIGHT;
   public static final File file = new File(
      "rose/client1_21_11/config", "theme/theme.json"
   );

   private void m136() {
      try (FileWriter v1 = new FileWriter(file)) {
         Properties v2 = new Properties();
         v2.setProperty("theme", this.f57.name());
         v2.store(v1, "GUI Settings");
      } catch (IOException v7) {
         v7.printStackTrace();
      }
   }

   public void init() {
      try {
         File v1 = file.getParentFile();
         int result = v3 * (v3 + 1);

         if (v1 != null) {
            boolean v7 = v1.exists();

            if (!v7) {
               v1.mkdirs();
            }
         }

         if (!file.exists()) {
            file.createNewFile();
            this.m136();
         } else {
            this.m137();
         }
      } catch (Exception v5) {
         v5.printStackTrace();
      }
   }

   private void m137() {
      try {
         FileReader v1 = new FileReader(file);

         try {
            Properties v2 = new Properties();
            v2.load(v1);
            this.f57 = Theme.valueOf(v2.getProperty("theme", Theme.NIGHT.name()));
         } catch (Throwable v6) {
            try {
               v1.close();
            } catch (Throwable v5) {
               v6.addSuppressed(v5);
            }

            throw v6;
         }

         v1.close();
      } catch (IllegalArgumentException | IOException v7) {
         v7.printStackTrace();
      }
   }

   public Theme getCurrentTheme() {
      return this.f57;
   }

   public void setGuiTheme(Theme theme) {
      this.f57 = theme;
      this.m136();
   }

   
}
