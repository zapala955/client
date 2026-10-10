package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.discord.Mn;

@Environment(EnvType.CLIENT)
public class Tn extends mn {
   public static tn create(String substring, String s) {

      int v6 = substring.length();

      substring = substring.substring(0, Math.min(v6, 31));
      return new tn(substring, s);
   }

   protected Tn(String label, String url) {
      super(label, url);
   }

   
}
