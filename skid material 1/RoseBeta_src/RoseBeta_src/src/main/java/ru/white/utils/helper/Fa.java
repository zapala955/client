package ru.white.utils.helper;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public enum Fa {
   POS_Z,
   ROT_PITCH,
   ROT_ROLL,
   POS_Y,
   ROT_YAW,
   POS_X;
   // $VF: synthetic field
   private static final fa[] $VALUES = m45();

   public static fa fromJsonKey(String key) {
      if (key == null) {
         return null;
      } else {
         String v1 = key.toLowerCase(Locale.ROOT);
         byte v2 = -1;
         switch (v1.hashCode()) {
            case 120:
               if (v1.equals("x")) {
                  v2 = 0;
               }
               break;
            case 121:
               if (v1.equals("y")) {
                  v2 = 1;
               }
               break;
            case 122:
               if (v1.equals("z")) {
                  v2 = 2;
               }
               break;
            case 119407:
               if (v1.equals("yaw")) {
                  v2 = 4;
               }
               break;
            case 3506301:
               if (v1.equals("roll")) {

                  v2 = 5;
               }
               break;
            case 106677056:
               if (v1.equals("pitch")) {
                  v2 = 3;
               }
         }
         return switch (v2) {
            case 0 -> POS_X;
            case 1 -> POS_Y;
            case 2 -> POS_Z;
            case 3 -> ROT_PITCH;
            case 4 -> ROT_YAW;
            case 5 -> ROT_ROLL;
            default -> null;
         };
      }
   }

   public boolean isRotation() {
      return this == ROT_PITCH || this == ROT_YAW || this == ROT_ROLL;
   }
}
