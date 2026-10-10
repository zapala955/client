package ru.white.utils.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Gu {
   private final cu f1650;
   private boolean f1652 = false;
   private static final Logger LOGGER = LoggerFactory.getLogger("client/FontRenderer");
   private final Map<String, zt> f1651;

   public void close() {

      this.f1650.close();
      this.f1651.clear();
      this.f1652 = false;
   }

   public void drawText(String fontName, String text, float x, float y, float size, int color) {
      int v8 = -963456459;
      int v9 = -1177533720;
      int temp4 = v9 + 1;

      zt v7 = this.f1651.get(fontName);
      if (v7 != null) {
         this.f1650.drawText(v7, text, x, y, size, color, 0.0F, 0, 0.0F);
      }
   }

   public float getLineHeight(String fontName, float size) {

      zt v3 = this.f1651.get(fontName);
      return v3 == null ? size : v3.getLineHeight() / v3.getFontSize() * size;
   }

   public void drawCenteredText(String fontName, String text, float x, float y, float size, int color) {
      zt v7 = this.f1651.get(fontName);
      if (v7 != null) {
         float v8 = this.f1650.getTextWidth(v7, text, size);
         float temp3 = x - v8 / 2.0F;

         this.f1650.drawText(v7, text, temp3, y, size, color, 0.0F, 0, 0.0F);
      }
   }

   public void drawTextRightAlignFadeLeft(String fontName, String text, float rightX, float y, float size, float fadeStartX, 

      zt v8 = this.f1651.get(fontName);
      if (v8 != null) {
         this.f1650.drawTextRightAlignFadeLeft(v8, text, rightX, y, size, fadeStartX, color);

         
      }
   }

   public void drawCenteredText(String fontName, String text, float x, float y, float size, int color, float rotation) {
      zt v8 = this.f1651.get(fontName);
      if (v8 != null) {
         float v9 = this.f1650.getTextWidth(v8, text, size);
         float v10 = this.f1650.getTextHeight(v8, text, size);

         float v12 = y + v10 / 2.0F;
         this.f1650.drawTextRotatedAroundPoint(v8, text, x - v9 / 2.0F, y, size, color, 0.0F, 0, rotation, x, v12);
      }
   }

   public void drawTextFading(String fontName, String text, float x, float y, float size, float maxWidth, 

      zt v8 = this.f1651.get(fontName);
      if (v8 != null) {

         this.f1650.drawTextFading(v8, text, x, y, size, maxWidth, color);
      }
   }

   public boolean isInitialized() {
      return this.f1652;
   }

   public void drawTextWithOutline(String fontName, String text, float x, float y, float size, int color, float outlineWidth, 

      zt v9 = this.f1651.get(fontName);
      if (v9 != null) {
         this.f1650.drawText(v9, text, x, y, size, color, outlineWidth, outlineColor, 0.0F);
      }
   }

   public float getTextWidth(String fontName, String text, float size) {
      zt v4 = this.f1651.get(fontName);

      return v4 == null ? 0.0F : this.f1650.getTextWidth(v4, text, size);
   }

   public void initialize() {
      if (!this.f1652) {
         int v5 = -1109158361;
         int temp2 = v5 * (v5 + 1);

         LOGGER.info("Initializing {} fonts...", this.f1651.size());
         long v1 = System.currentTimeMillis();

         for (Entry v4 : this.f1651.entrySet()) {
            ((zt)v4.getValue()).forceLoad();
         }

         this.f1652 = true;
         LOGGER.info("All fonts initialized in {}ms", System.currentTimeMillis() - v1);
      }
   }

   public void drawText(String fontName, String text, float x, float y, float size, int color, float rotation) {
      zt v8 = this.f1651.get(fontName);
      if (v8 != null) {

         this.f1650.drawText(v8, text, x, y, size, color, 0.0F, 0, rotation);
      }
   }

   public void loadFont(String name, String path) {
      StringBuilder temp1 = new StringBuilder;

      temp1./* $VF: Unable to resugar constructor */<init>();
      Identifier v3 = Identifier.method_60655("client", temp1.append("fonts/").append((Object)path).append(".json").toString());
      Identifier v4 = Identifier.method_60655("client", "fonts/" + path + ".png");
      zt result = new zt;

      result./* $VF: Unable to resugar constructor */<init>(v3, v4);
      zt v5 = result;
      this.f1651.put(name, v5);

      LOGGER.info("Registered font: {} -> {}", name, path);
   }

   public void flush() {
      this.f1650.flush();
      
   }

   public zt getFont(String name) {
      return this.f1651.get(name);
   }

   public void loadAllFonts(Map<String, String> registry) {
      for (Entry v3 : registry.entrySet()) {
         this.loadFont((String)v3.getKey(), (String)v3.getValue());
         
      }
   }

   public void drawTextWithOutline(String fontName, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {

      zt v10 = this.f1651.get(fontName);
      if (v10 != null) {
         int v11 = 1388111747;
         int var10008 = v11 + 1;

         this.f1650.drawText(v10, text, x, y, size, color, outlineWidth, outlineColor, rotation);
      }
   }

   public Gu() {
      this.f1650 = new cu();
      this.f1651 = new HashMap<>();
   }

   public void drawTextFadingReverse(String fontName, String text, float x, float y, float size, float maxWidth, int color) {
      zt v8 = this.f1651.get(fontName);
      if (v8 != null) {
         this.f1650.drawTextFadingReverse(v8, text, x, y, size, maxWidth, color);
      }
   }

   
}
