package ru.white.utils.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Zt {
   private final AtomicBoolean f1632;
   final Map<String, Float> widthCache;
   private float f1630;
   private final Identifier f1627;
   private float f1628 = 512.0F;
   private boolean f1631;
   private final Map<Integer, iu> glyphs;
   private float lineHeight;
   private static final Logger LOGGER = LoggerFactory.getLogger("client/Font");
   private final Identifier f1626;
   private float distanceRange;
   private float f1629 = 512.0F;

   public float getFontSize() {
      return this.f1630;
   }

   public Identifier getTextureId() {

      return this.f1627;
   }

   private float m1959(JsonObject obj, String key, float def) {

      int v4 = -890999822;
      int v7 = v4 * (v4 + 1);

      return obj.has(key) ? obj.get(key).getAsFloat() : def;
   }

   public iu getGlyph(int codePoint) {
      Map result = this.glyphs;
      Integer temp1 = codePoint;

      return (iu)result.get(temp1);
   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   private void m1957(JsonObject root) {
      float v2 = 1.0F;
      if (root.has("atlas")) {
         JsonObject v3 = root.getAsJsonObject("atlas");
         this.f1628 = this.m1959(v3, "width", 512.0F);
         this.f1629 = this.m1959(v3, "height", 512.0F);
         this.f1630 = this.m1959(v3, "size", 32.0F);
         this.distanceRange = this.m1959(v3, "distanceRange", 4.0F);
         if (v3.has("yOrigin")) {
            String v4 = v3.get("yOrigin").getAsString();
            this.f1631 = v4.equalsIgnoreCase("bottom");
         }

         LOGGER.info(
            "Atlas: {}x{}, size={}, distanceRange={}, yOrigin={}",
            new Object[]{this.f1628, this.f1629, this.f1630, this.distanceRange, this.f1631 ? "bottom" : "top"}
         );
      }

      if (root.has("metrics")) {
         JsonObject v9 = root.getAsJsonObject("metrics");
         v2 = this.m1959(v9, "emSize", 1.0F);
         float v11 = this.m1959(v9, "lineHeight", 1.2F);
         this.lineHeight = v11 * this.f1630;
      }

      if (root.has("glyphs")) {
         for (JsonElement v5 : root.getAsJsonArray("glyphs")) {

            JsonObject v6 = v5.getAsJsonObject();
            this.m1958(v6, v2);
         }
      }
   }

   public Zt(Identifier jsonId, Identifier textureId) {
      this.f1630 = 32.0F;
      this.lineHeight = 40.0F;
      this.distanceRange = 4.0F;
      this.f1631 = false;
      this.f1632 = new AtomicBoolean(false);
      this.widthCache = new LinkedHashMap<String, Float>(512, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<String, Float> eldest) {
            boolean result;
            if (this.size() > 2048) {
               result = true;
               
            } else {
               result = false;
            }

            return result;
         }

         
      };
      this.f1626 = jsonId;
      this.f1627 = textureId;
      this.glyphs = new HashMap<>();
   }

   public boolean hasGlyph(int codePoint) {

      return this.glyphs.containsKey(codePoint);
   }

   private void m1958(JsonObject g, float emSize) {
      int v3 = -1;
      if (g.has("unicode")) {
         v3 = g.get("unicode").getAsInt();
      } else if (g.has("char")) {
         String v4 = g.get("char").getAsString();
         if (!v4.isEmpty()) {
            v3 = v4.codePointAt(0);
         }
      } else if (g.has("id")) {
         v3 = g.get("id").getAsInt();
      }

      if (v3 >= 0) {
         float v19 = this.m1959(g, "advance", 0.0F) * this.f1630;
         if (v19 == 0.0F) {
            v19 = this.m1959(g, "xadvance", 0.0F);
         }

         float v5 = 0.0F;
         float v6 = 0.0F;
         float v7 = 0.0F;
         float v8 = 0.0F;
         float v9 = 0.0F;
         float v10 = 0.0F;
         if (g.has("atlasBounds")) {
            JsonObject v11 = g.getAsJsonObject("atlasBounds");
            float v12 = this.m1959(v11, "left", 0.0F);
            float v13 = this.m1959(v11, "bottom", 0.0F);
            float v14 = this.m1959(v11, "right", 0.0F);
            float v15 = this.m1959(v11, "top", 0.0F);
            v5 = v12;
            v7 = v14 - v12;
            v8 = v15 - v13;
            if (this.f1631) {
               v6 = this.f1629 - v15;
               
            } else {
               v6 = v13;
            }
         } else if (g.has("x") && g.has("y") && g.has("width") && g.has("height")) {
            v5 = this.m1959(g, "x", 0.0F);
            v6 = this.m1959(g, "y", 0.0F);
            v7 = this.m1959(g, "width", 0.0F);
            v8 = this.m1959(g, "height", 0.0F);
         }

         if (g.has("planeBounds")) {
            JsonObject v20 = g.getAsJsonObject("planeBounds");
            float v21 = this.m1959(v20, "left", 0.0F);
            float v22 = this.m1959(v20, "bottom", 0.0F);
            float v23 = this.m1959(v20, "right", 0.0F);
            float v24 = this.m1959(v20, "top", 0.0F);
            v9 = v21 * this.f1630;
            float v16 = 0.95F;
            v10 = (v16 - v24) * this.f1630;
         } else if (g.has("xoffset") && g.has("yoffset")) {
            v9 = this.m1959(g, "xoffset", 0.0F);
            v10 = this.m1959(g, "yoffset", 0.0F);
         }

         this.glyphs.put(v3, new iu(v3, v5, v6, v7, v8, v9, v10, v19, this.f1628, this.f1629));
      }
   }

   public float getAtlasHeight() {

      return this.f1629;
   }

   private void m1956() {
      try {
         Optional v1 = MinecraftClient.method_1551().method_1478().method_14486(this.f1626);
         if (v1.isEmpty()) {
            LOGGER.warn("Font JSON not found: {}", this.f1626);
            this.f1632.set(true);
            return;
         }

         try (
            InputStream v2 = ((Resource)v1.get()).method_14482();
            InputStreamReader v3 = new InputStreamReader(v2, StandardCharsets.UTF_8);
         ) {
            JsonObject v4 = JsonParser.parseReader(v3).getAsJsonObject();
            this.m1957(v4);
            int v6 = 1644397233;

            this.f1632.set(true);
            LOGGER.info("Loaded font: {} with {} glyphs", this.f1626, this.glyphs.size());
         }
      } catch (Exception v13) {
         LOGGER.error("Failed to load font: {}", this.f1626, v13);
         this.f1632.set(true);
      }
   }

   public 

      return this.glyphs.size();
   }

   public float getAtlasWidth() {

      return this.f1628;
   }

   public void ensureLoaded() {
      if (!this.f1632.get()) {

         synchronized (this) {
            if (!this.f1632.get()) {
               this.m1956();
            }
         }
      }
   }

   public void forceLoad() {
      if (!this.f1632.get()) {
         synchronized (this) {

            if (!this.f1632.get()) {
               this.m1956();
            }
         }
      }
   }

   public float getDistanceRange() {

      return this.distanceRange;
   }

   public boolean isLoaded() {
      boolean result = this.f1632.get();

      return result;
   }

   
}
