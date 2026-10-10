package ru.white.notification;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class NotificationManager {
   public static final float CHIP_H = 9.0F;
   public static final float VALUE_TEXT = 5.5F;
   public static final float PICKER_H = 27.0F;
   public static final float PANEL_W = 168.0F;
   public static final float HEADER_H = 16.0F;
   public static final float DELIMITER_H = 11.0F;
   public static final float ROW_H = 13.0F;
   public static final float CHIP_STEP = 10.5F;
   public static final float ROW_GAP = 3.0F;
   public static final float PANEL_PAD = 6.0F;
   public static final float SLIDER_H = 20.0F;
   public static final float TEXT = 6.0F;

   private NotificationManager() {
   }

   
}
