package ru.white.utils.discord;

import java.io.Serializable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Mn implements Serializable {
   private final String url;
   private final String f1009;

   public String getUrl() {
      return this.url;
   }

   protected Mn(String label, String url) {
      this.f1009 = label;
      this.url = url;
   }

   public String getLabel() {
      int v1 = -1563203405;

      return this.f1009;
   }

   public static mn create(String label, String url) {
      int temp2 = label.length();

      label = label.substring(0, Math.min(temp2, 31));
      return new mn(label, url);
   }

   
}
