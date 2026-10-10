package ru.white.utils.discord;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
class Dn$fn {
   private final String name;
   private final String f1005;
   private final String url;

   private String m1336() {

      return this.url;
   }

   private String m1335() {

      return this.name;
   }

   private String m1337() {

      return this.f1005;
   }

   private Dn$fn(String name, String url, String iconUrl) {
      this.name = name;
      this.url = url;
      this.f1005 = iconUrl;
   }

   
}
