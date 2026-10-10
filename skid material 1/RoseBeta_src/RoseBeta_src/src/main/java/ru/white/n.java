package ru.white;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class n {
   public static final String CHARSET = "\"\\\" ¡‰·₴≠¿×ØøАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюяє–—‘’“”„…←↑→↓ʻˌ;⁰¹³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁱ™ʔʕ¤¥©®µ¶¼½¾·‐‚†‡•′″‴‹›‽⁂℗−∞Є♠♣♥♦♭♮♯⚀⚁⚂⚃⚄⚅ʬ❄⏏⏻⏼⏽⭘▲▶▼◀●◦�¦ᴀʙᴄᴅᴇꜰɢʜᴊᴋʟᴍɴᴏᴘꞯʀꜱᴛᴜᴠᴡʏᴢ§ʡʢʘǀǃǂǁ☂♤♧♡♢↔∑□△▷▽◁○☆★₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎∫⌀⌘⚠⓪①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ☑☒!#$%&'()*+,-./0123456789:;<=>[\\\\]^_`?@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz{|}~£ƒªº¬«»≡±≥≤⌠⌡÷≈°∙√ⁿ²■\"";

   private static void m0(String fontFolderPath, Path outputPath, String fontPath) {
      File v3 = new File(fontFolderPath + fontPath);
      File[] v4 = v3.listFiles((dir, name) -> {
         String var10000x = name.toLowerCase();

         return var10000x.endsWith(".ttf");
      });

      if (v4 != null && v4.length > 0) {
         ExecutorService v5 = Executors.newFixedThreadPool(v4.length);
         ArrayList v6 = new ArrayList();

         for (File v10 : v4) {

            v5.execute(
               () -> {
                  try {
                     String v4x = v10.getName().replaceFirst("[.][^.]+$", "");
                     String v5x = String.format(
                        "%s/atlas-gen.exe -font %s -charset %s/charset.txt -type mtsdf -format png -imageout %s.png -json %s.json -size 64 -square4 -pxrange 12",
                        fontFolderPath,
                        v10.getAbsolutePath(),
                        fontFolderPath,
                        outputPath.resolve(v4x.toLowerCase().replaceAll("-", "_")),
                        outputPath.resolve(v4x.toLowerCase().replaceAll("-", "_"))
                     );
                     Runtime var15x = Runtime.getRuntime();

                     Process v6x = var15x.exec(v5x);
                     v6.add(v6x);
                     int v7 = v6x.waitFor();
                     if (v7 == 0) {
                        System.out
                           .println(
                              "Атлас для шрифта "
                                 + v4x
                                 + " успешно создан."
                           );
                     } else {
                        System.out
                           .println(
                              "Ошибка при создании атласа для шрифта "
                                 + v4x
                           );
                     }
                  } catch (InterruptedException | IOException var14x) {
                     PrintStream var10000x = System.err;
                     String var10001x = v10.getName();
                     String var10x = var14x.getMessage();

                     String v11 = var10001x;
                     var10000x.println(
                        "Ошибка при выполнении команды для шрифта "
                           + v11
                           + ": "
                           + var10x
                     );
                  }
               }
            );
         }

         v5.shutdown();

         try {
            if (v5.awaitTermination(-8609937271138771357L + -613434765716004452L, TimeUnit.NANOSECONDS)) {
               System.out.println("Процесс завершён.");
            }
         } catch (InterruptedException v16) {
            PrintStream v19 = System.err;
            String v13 = v16.getMessage();
            v19.println(
               "Ошибка при ожидании завершения потоков: "
                  + v13
            );
         }

         for (Process v18 : v6) {
            v18.destroy();
         }
      } else {
         System.out
            .println(
               "Не найдены файлы шрифтов в указанной папке."
            );
      }
   }

   public static void main(String[] args) {
      String v1 = "mtsdf-font/";

      Path v2 = m1(v1);

      if (v2 == null) {
      }

      m0(v1, v2, "test");
   }

   private static Path m1(String fontFolderPath) {
      Path v1 = Path.of(fontFolderPath + "out");
      if (Files.notExists(v1)) {
         try {
            Files.createDirectories(v1);
         } catch (IOException v11) {
            PrintStream result = System.err;
            String v5 = v11.getMessage();
            result.println("Ошибка при создании папки: " + v5);
            return null;
         }
      }

      String v12 = fontFolderPath + "charset.txt";

      Path v2 = Path.of(v12);
      if (Files.notExists(v2)) {
         try {
            Files.createFile(v2);

            Files.write(
               v2,
               "\"\\\" ¡‰·₴≠¿×ØøАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюяє–—‘’“”„…←↑→↓ʻˌ;⁰¹³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁱ™ʔʕ¤¥©®µ¶¼½¾·‐‚†‡•′″‴‹›‽⁂℗−∞Є♠♣♥♦♭♮♯⚀⚁⚂⚃⚄⚅ʬ❄⏏⏻⏼⏽⭘▲▶▼◀●◦�¦ᴀʙᴄᴅᴇꜰɢʜᴊᴋʟᴍɴᴏᴘꞯʀꜱᴛᴜᴠᴡʏᴢ§ʡʢʘǀǃǂǁ☂♤♧♡♢↔∑□△▷▽◁○☆★₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎∫⌀⌘⚠⓪①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ☑☒!#$%&'()*+,-./0123456789:;<=>[\\\\]^_`?@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz{|}~£ƒªº¬«»≡±≥≤⌠⌡÷≈°∙√ⁿ²■\""
                  .getBytes()
            );
         } catch (IOException v10) {
            PrintStream v13 = System.err;
            String v7 = v10.getMessage();
            v13.println("Ошибка при создании charset.txt: " + v7);
            return null;
         }
      }

      return v1;
   }

   
}
