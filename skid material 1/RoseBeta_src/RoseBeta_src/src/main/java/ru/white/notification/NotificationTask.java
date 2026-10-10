package ru.white.notification;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.screen.GuiSettingElement;
import ru.white.screen.GuiCheckBox;
import ru.white.screen.GuiColorPicker;
import ru.white.screen.GuiKeybind;

@Environment(EnvType.CLIENT)
public final class NotificationTask {
   private static final ro[] ALL = new ro[]{go.getInstance(), mo.getInstance(), eo.getInstance(), no.getInstance()};

   public static void closeAll() {
      ro[] v0 = all();

      for (ro v3 : v0) {
         if (v3.isActive()) {

            v3.closeFromMenuRemoval();
         }
      }
   }

   public static ro[] all() {

      return ALL;
   }

   public static boolean anyActive() {
      ro result = active();

      return result != null;
   }

   public static ro active() {
      ro[] v0 = ALL;

      int v1 = v0.length;

      while (v2 < v1) {
         ro v3 = v0[v2];
         if (v3.isActive()) {
            return v3;
         }

         v2++;
         
      }

      return null;
   }

   private NotificationTask() {
   }

   
}
