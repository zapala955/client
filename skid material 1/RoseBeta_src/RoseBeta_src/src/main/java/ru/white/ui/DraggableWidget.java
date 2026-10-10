package ru.white.ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class DraggableWidget {
   private static final Path LANG_FILE = Path.of("rose/client1_21_11/language.txt");
   private static final Map<String, String> DICT = db.build();
   private static bb.cb f46 = m119();

   public static String pick(String ru, String en) {

      return f46 == bb.cb.ENGLISH ? en : ru;
   }

   public static void reloadDict() {
   }

   public static boolean isEnglish() {
      boolean v3;
      if (f46 == bb.cb.ENGLISH) {

         v3 = true;
      } else {
         v3 = false;
      }

      return v3;
   }

   private static void m120() {
      try {
         Path result = LANG_FILE.getParent();
         FileAttribute[] temp1 = new FileAttribute[0];
         int v1 = -1224391939;
         int temp2 = v1 * (v1 + 1);

         Files.createDirectories(result, temp1);
         Files.writeString(LANG_FILE, f46 == bb.cb.ENGLISH ? "EN" : "RU", StandardCharsets.UTF_8);
      } catch (Exception v4) {
      }
   }

   public static String tag() {
      String result;
      if (f46 == bb.cb.ENGLISH) {
         result = "EN";

         int v0 = 21123198;
         int v3 = v0 + 1;

         
      } else {
         result = "RU";
      }

      return result;
   }

   public static void toggle() {
      bb.cb v4;
      if (f46 == bb.cb.ENGLISH) {
         int v0 = 1058777040;
         int v1 = 130369982;

         v4 = bb.cb.RUSSIAN;
      } else {
         v4 = bb.cb.ENGLISH;
      }

      setLanguage(v4);
   }

   public static void setLanguage(bb.cb language) {

      if (language != null && language != f46) {
         f46 = language;
         m120();
      }
   }

   public static String tr(String ru) {
      if (ru != null && f46 == bb.cb.ENGLISH) {
         String v1 = DICT.get(ru);
         String result;
         if (v1 != null) {
            result = v1;
         } else {
            result = ru;
            int v2 = -2146771625;
            int temp1 = v2 * (v2 + 1);

            
         }

         return result;
      } else {
         return ru;
      }
   }

   public static void init() {
      PrintStream result = System.out;
      int temp1 = DICT.size();
      String v0 = tag();

      int v1 = temp1;
      result.println("[Lang] переводов в словаре: " + v1 + ", язык: " + v0);
   }

   private static bb.cb m119() {
      try {
         if (Files.exists(LANG_FILE)) {

            String v0 = Files.readString(LANG_FILE, StandardCharsets.UTF_8).trim();
            if (v0.equalsIgnoreCase("EN") || v0.equalsIgnoreCase("ENGLISH")) {
               return bb.cb.ENGLISH;
            }
         }
      } catch (Exception v2) {
      }

      return bb.cb.RUSSIAN;
   }

   private DraggableWidget() {
   }

   public static bb.cb getLanguage() {

      return f46;
   }

   @Environment(EnvType.CLIENT)
   public static enum cb {
      RUSSIAN,
      ENGLISH;
      // $VF: synthetic field
      private static final bb.cb[] $VALUES = m118();
   }
}
