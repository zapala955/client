package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.discord.Mn;

@Environment(EnvType.CLIENT)
public class M extends mn {
   protected M(String label, String url) {
      super(label, url);
   }

   public static m create(String label, String url) {
      label = label.substring(0, Math.min(label.length(), 31));

      return new m(label, url);
   }

   
}
