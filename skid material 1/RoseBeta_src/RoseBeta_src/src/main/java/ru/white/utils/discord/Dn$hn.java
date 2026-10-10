package ru.white.utils.discord;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
class Dn$hn {
   private final String text;
   private final String f1006;

   private Dn$hn(String text, String iconUrl) {
      this.text = text;
      this.f1006 = iconUrl;
   }

   private String m1341() {

      return this.text;
   }

   private String m1342() {
      return this.f1006;
   }

   
}
