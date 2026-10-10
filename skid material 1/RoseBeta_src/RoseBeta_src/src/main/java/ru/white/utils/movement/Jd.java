package ru.white.utils.movement;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Jd {
   private static final int MIN_BINARY_SEARCH_THRESHOLD = 8;

   public static cd remove(List<cd> listeners, cd listener) {
      int v2 = listener.getPriority();

      for (int v3 = 0; v3 < listeners.size(); v3++) {
         cd v4 = (cd)listeners.get(v3);
         if (v4 == listener) {
            return (cd)listeners.remove(v3);
         }

         if (v4.getPriority() < v2) {
            int v6 = 1028368002;
            int result = v6 * (v6 + 1);

            
            break;
         }
      }

      return null;
   }

   private static int m155(List<cd> listeners, cd listener) {
      int v2 = 0;
      int v3 = listeners.size() - 1;

      while (v2 <= v3) {
         int v4 = v2 + (v3 - v2) / 2;
         cd v5 = (cd)listeners.get(v4);
         int v6 = Integer.compare(listener.getPriority(), v5.getPriority());
         if (v6 == 0) {
            return v4;
         }

         if (v6 < 0) {
            v2 = v4 + 1;
         } else {

            v3 = v4 - 1;
         }
      }

      return v2;
   }

   public static boolean contains(List<cd> listeners, cd listener) {
      int v2 = listener.getPriority();

      for (Object result : listeners) {

         cd v4 = (cd)result;
         if (v4 == listener) {
            return true;
         }

         int v7 = v4.getPriority();

         if (v7 < v2) {
            break;
         }
      }

      return false;
   }

   private static int m154(List<cd> listeners, cd listener) {
      int v2 = listeners.size();

      for (int v3 = 0; v3 < v2; v3++) {
         int result = listener.getPriority();

         if (result > ((cd)listeners.get(v3)).getPriority()) {
            return v3;
         }
      }

      return v2;
   }

   public static void insert(List<cd> listeners, cd listener) {
      int v2 = listeners.size() < 8 ? m154(listeners, listener) : m155(listeners, listener);

      listeners.add(v2, listener);
   }

   
}
