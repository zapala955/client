package ru.white.utils.discord;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Cn extends Structure {
   public String username;
   public String userId;
   public String avatar;
   @Deprecated
   public String discriminator;

   protected List<String> getFieldOrder() {
      String[] result = new String[4];

      result[0] = "userId";
      result[1] = "username";
      result[2] = "discriminator";
      result[3] = "avatar";
      return Arrays.asList(result);
   }

   
}
