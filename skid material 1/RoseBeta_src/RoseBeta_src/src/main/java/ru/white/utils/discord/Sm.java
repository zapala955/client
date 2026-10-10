package ru.white.utils.discord;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Un;
import ru.white.utils.misc.Vn;
import ru.white.utils.misc.Wn;
import ru.white.utils.misc.Xn;
import ru.white.utils.misc.Yn;
import ru.white.utils.render.FontRegistry;

@Environment(EnvType.CLIENT)
public class Sm extends Structure {
   public yn ready;
   public wn joinGame;
   public zn spectateGame;
   public un disconnected;
   public vn errored;
   public xn joinRequest;

   protected List<String> getFieldOrder() {

      String[] result = new String[6];
      result[0] = "ready";
      result[1] = "disconnected";

      result[2] = "errored";
      result[3] = "joinGame";
      result[4] = "spectateGame";
      result[5] = "joinRequest";

      return Arrays.asList(result);
   }

   @Environment(EnvType.CLIENT)
   public static class tm {
      private final sm f996 = new sm();

      public sm.tm disconnected(un v1) {

         this.f996.disconnected = v1;

         return this;
      }

      public sm.tm joinRequest(xn v1) {
         this.f996.joinRequest = v1;
         return this;
      }

      public sm.tm spectateGame(zn v1) {
         this.f996.spectateGame = v1;
         return this;
      }

      public sm.tm errored(vn v1) {
         this.f996.errored = v1;

         int v2 = 772209997;

         return this;
      }

      public sm.tm ready(yn v1) {
         this.f996.ready = v1;

         return this;
      }

      public sm build() {

         return this.f996;
      }

      public sm.tm joinGame(wn v1) {
         int v2 = 85862211;
         int temp1 = v2 * (v2 + 1);

         this.f996.joinGame = v1;
         return this;
      }

      
   }
}
