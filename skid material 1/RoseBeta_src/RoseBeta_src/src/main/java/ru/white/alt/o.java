package ru.white.alt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class o {
   public long created;
   public o.p type;
   public boolean favorite;
   public String name;

   public String headUrl() {
      String v1 = this.name;
      StringBuilder result = new StringBuilder().append("https://minotar.net/helm/");

      return result.append((Object)v1).append("/64.png").toString();
   }

   public o(String name, o.p type, long created) {
      this.name = name;
      this.type = type;
      this.created = created;
   }

   public UUID offlineUuid() {
      String v1 = this.name;
      StringBuilder result = new StringBuilder;

      result./* $VF: Unable to resugar constructor */<init>();
      return UUID.nameUUIDFromBytes(
         result.append("OfflinePlayer:").append((Object)v1).toString().getBytes(StandardCharsets.UTF_8)
      );
   }

   public o(String name) {
      this(name, o.p.OFFLINE, System.currentTimeMillis());
   }

   @Environment(EnvType.CLIENT)
   public static enum p {
      OFFLINE;
      // $VF: synthetic field
      private static final o.p[] $VALUES = m4();
   }
}
