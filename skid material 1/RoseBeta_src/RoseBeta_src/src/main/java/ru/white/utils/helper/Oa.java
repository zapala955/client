package ru.white.utils.helper;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Oa {
   LEFT_ARM,
   HEAD,
   LEFT_LEG,
   RIGHT_LEG,
   RIGHT_ARM,
   BODY;
   // $VF: synthetic field
   private static final oa[] $VALUES = m74();

   public static oa fromJsonKey(String key) {
      if (key == null) {
         return null;
      } else {
         String v1 = key.toLowerCase(Locale.ROOT);
         int v2 = -1;
         switch (v1.hashCode()) {
            case -1436077376:
               if (v1.equals("rightarm")) {
                  v2 = 3;
                  
               }
               break;
            case -1436067214:
               if (v1.equals("rightleg")) {
                  v2 = 5;
               }
               break;
            case 3029410:
               if (v1.equals("body")) {
                  v2 = 2;
               }
               break;
            case 3198432:
               if (v1.equals("head")) {
                  v2 = 0;
               }
               break;
            case 55445749:
               if (v1.equals("leftarm")) {
                  v2 = 4;
               }
               break;
            case 55455911:
               if (v1.equals("leftleg")) {
                  v2 = 6;
               }
               break;
            case 110548467:
               if (v1.equals("torso")) {
                  v2 = 1;
               }
         }
         return switch (v2) {
            case 0 -> HEAD;
            case 1, 2 -> BODY;
            case 3 -> RIGHT_ARM;
            case 4 -> LEFT_ARM;
            case 5 -> RIGHT_LEG;
            case 6 -> LEFT_LEG;
            default -> null;
         };
      }
   }
}
