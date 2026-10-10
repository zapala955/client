package ru.white.manager;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.theme.xo;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;

@Environment(EnvType.CLIENT)
public enum Theme {
   AKAR(
      "Red",
      new Color(16747403).getRGB(),
      new Color(-1726280692, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(16747403).getRGB()
   ),
   Turquoise(
      "Turquoise",
      new Color(96, 255, 198).getRGB(),
      new Color(-1726797010, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(96, 255, 198).getRGB()
   ),
   Purple(
      "Purple",
      new Color(216, 68, 234).getRGB(),
      new Color(-1725690581, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(216, 68, 234).getRGB()
   ),
   VIOLKA(
      "Violet",
      new Color(10521599).getRGB(),
      new Color(-1726672860, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(10521599).getRGB()
   ),
   White(
      "White",
      new Color(14671839).getRGB(),
      new Color(-1728053248, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(14671839).getRGB()
   ),
   CUSTOM(
      "Custom",
      new Color(246, 124, 180).getRGB(),
      new Color(-1726410208, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(246, 124, 180).getRGB()
   ),
   TOXIS(
      "Toxis",
      new Color(9961355).getRGB(),
      new Color(-1726996979, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(12189579).getRGB()
   ),
   NIGHT(
      "Blue",
      new Color(9151231).getRGB(),
      new Color(-1726867157, true).getRGB(),
      up.getColor(240),
      up.getColor(160),
      up.getColor(24, 24, 27),
      new Color(9151231).getRGB()
   );

   private final String name;
   private final int rect_two;
   private final int client;
   private final int text_dark;
   private final int text;
   // $VF: synthetic field
   private static final Theme[] $VALUES = $values();
   private final int rect;
   private final int text_client_c;
   public Animation animation = new ep(300, 1.0);

   public int getText_dark() {
      if (this == CUSTOM) {
         return xo.get().getActive().get(4);
      } else {

         return this.text_dark;
      }
   }

   public int getText_client_c() {
      if (this == CUSTOM) {
         return xo.get().getActive().get(5);
      } else {

         return this.text_client_c;
      }
   }

   public int getRect() {
      if (this == CUSTOM) {
         int result = xo.get().getActive().get(1);

         return result;
      } else {
         return this.rect;
      }
   }

   public 

      if (this == CUSTOM) {
         ru.white.theme.Theme result = xo.get().getActive();
         int temp1 = 7;

         return result.get(temp1);
      } else {
         return this.rect_two;
      }
   }

   public 

      return this == CUSTOM ? xo.get().getActive().get(0) : this.client;
   }

   public String getName() {

      return this.name;
   }

   private Theme(String name, int client, int rect, int text, int text_dark, int rect_two, int text_client_c) {
      this.name = name;
      this.client = client;
      this.rect = rect;
      this.text = text;
      this.text_dark = text_dark;
      this.rect_two = rect_two;
      this.text_client_c = text_client_c;
   }

   public 

      return this == CUSTOM ? xo.get().getActive().get(3) : this.text;
   }
}
