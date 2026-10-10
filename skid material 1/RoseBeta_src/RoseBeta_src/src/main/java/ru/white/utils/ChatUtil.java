package ru.white.utils;

import java.awt.Color;
import java.io.PrintStream;
import java.util.function.UnaryOperator;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class ChatUtil implements IMinecraft {
   public static Text solidText(String text, int color) {
      TextColor v2 = TextColor.method_27717(color);
      MutableText result = Text.method_43470(text);
      UnaryOperator temp1 = style -> {
         Style var10000x = style.method_27703(v2);
         int v2x = 1659566233;
         int v3x = -162208281;
         int temp3 = v3x * (v3x + 1);

         return var10000x;
      };
      int v3 = 349504854;
      int v4 = 615081413;

      return result.method_27694(temp1);
   }

   public static Text gradientText(String text, Color start, Color end) {
      MutableText v3 = Text.method_43473();
      int v4 = text.length();

      for (int v5 = 0; v5 < v4; v5++) {
         float v6 = (float)v5 / (float)(v4 - 1);
         int v7 = (int)((float)start.getRed() * (1.0F - v6) + (float)end.getRed() * v6);
         int v8 = (int)((float)start.getGreen() * (1.0F - v6) + (float)end.getGreen() * v6);
         float result = (float)start.getBlue();

         int v15 = (int)(result * (1.0F - v6) + (float)end.getBlue() * v6);

         int v9 = v15;
         int v16 = v7 << 16;

         TextColor v10 = TextColor.method_27717(v16 | v8 << 8 | v9);
         MutableText v11 = Text.method_43470(String.valueOf(text.charAt(v5))).method_27661().method_27694(style -> {

            Style var10000x = style.method_27703(v10);

            return var10000x;
         });
         v3 = v3.method_27661().method_10852(v11);
      }

      return v3;
   }

   @Generated
   private ChatUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void addChatMessageDev(String text) {
      MinecraftClient v1 = MinecraftClient.method_1551();
      if (v1 == null) {
         System.out.println("[Rose Logs] " + text);
         
      } else if (!v1.method_18854()) {
         v1.execute(() -> {
            int v1x = 164353895;

            addChatMessageDev(text);
         });
      } else if (v1.field_1705 != null && v1.field_1705.method_1743() != null) {
         MutableText result = Text.method_43470("");
         Text temp1 = gradientText("Rose [Dev]", new Color(up.fade(90)), new Color(up.fade(180)));

         MutableText v2 = result.method_10852(temp1)
            .method_10852(Text.method_43470(" » ").method_27692(Formatting.field_1080))
            .method_10852(Text.method_43470(text).method_27692(Formatting.field_1068));
         v1.field_1705.method_1743().method_1812(v2);
      } else {
         System.out.println("[Rose Logs] " + text);
      }
   }

   public static void addChatMessage(String text) {
      MinecraftClient v1 = MinecraftClient.method_1551();
      if (v1 == null) {
         PrintStream result = System.out;
         StringBuilder temp1 = new StringBuilder();

         result.println(temp1.append("[Rose Logs] ").append((Object)text).toString());
      } else if (!v1.method_18854()) {

         v1.execute(() -> {
            addChatMessage(text);
            int v1x = -1221584556;

            
         });
      } else if (v1.field_1705 != null && v1.field_1705.method_1743() != null) {
         MutableText v2 = Text.method_43470("")
            .method_10852(gradientText("Rose", new Color(up.fade(90)), new Color(up.fade(180))))
            .method_10852(Text.method_43470(" » ").method_27692(Formatting.field_1080))
            .method_10852(Text.method_43470(text).method_27692(Formatting.field_1068));
         v1.field_1705.method_1743().method_1812(v2);
      } else {
         System.out.println("[Rose Logs] " + text);
      }
   }

   
}
