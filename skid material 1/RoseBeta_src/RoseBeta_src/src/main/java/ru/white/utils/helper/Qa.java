package ru.white.utils.helper;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Qa {
   public static final List<ka> ALL;

   private Qa() {
   }

   private static Identifier m75(String id) {

      StringBuilder v5 = new StringBuilder().append("textures/emotes/");

      return Identifier.method_60655("white", v5.append((Object)id).append(".png").toString());
   }

   static {
      ka[] result = new ka[11];
      result[0] = new ka("waving", "Waving", m75("waving"));
      result[1] = new ka("clap", "Clap", m75("clap"));
      result[2] = new ka("point", "Point", m75("point"));
      result[3] = new ka("here", "Here", m75("here"));
      result[4] = new ka("palm", "Facepalm", m75("palm"));
      result[5] = new ka("crying", "Crying", m75("crying"));
      result[6] = new ka("backflip", "Backflip", m75("backflip"));
      result[7] = new ka("twerk", "Twerk", m75("twerk"));
      result[8] = new ka(
         "kazotsky_kick",
         "Kazotsky Kick",
         m75("kazotsky_kick")
      );
      result[9] = new ka(
         "club_penguin_dance",
         "Club Penguin",
         m75("club_penguin_dance")
      );
      result[10] = new ka(
         "roblox_potion_dance",
         "Potion Dance",
         m75("roblox_potion_dance")
      );
      ALL = List.of(result);
   }

   public static ka byId(String id) {
      Iterator v1 = ALL.iterator();

      ka v2;
      do {

         if (!v1.hasNext()) {
            return null;
         }

         v2 = (ka)v1.next();
      } while (!v2.id().equals(id));

      return v2;
   }

   
}
