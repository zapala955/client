package ru.white.utils.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.LoggerFactory;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
final class Na {
   private Na() {
   }

   private static String m73(JsonObject object, String key, String fallback) {
      String result;
      if (object != null && object.has(key) && !object.get(key).isJsonNull()) {
         result = object.get(key).getAsString();
         
      } else {
         result = fallback;
      }

      return result;
   }

   private static boolean m71(JsonObject object, String key, boolean fallback) {
      if (object != null) {
         boolean result = object.has(key);

         if (result && !object.get(key).isJsonNull()) {
            return object.get(key).getAsBoolean();
         }
      }

      return fallback;
   }

   static ha load(String id, String displayName, String resourcePath) {
      try {
         ha v37;
         try (InputStream v3 = na.class.getResourceAsStream(resourcePath)) {
            if (v3 == null) {
               throw new IOException("Missing bundled resource: " + resourcePath);
            }

            InputStreamReader v5 = new InputStreamReader(v3, StandardCharsets.UTF_8);

            JsonObject v4;
            try {
               v4 = JsonParser.parseReader(v5).getAsJsonObject();
            } catch (Throwable v33) {
               try {
                  v5.close();
               } catch (Throwable v32) {
                  v33.addSuppressed(v32);
               }

               throw v33;
            }

            v5.close();
            JsonObject v36 = v4.getAsJsonObject("emote");
            boolean v6 = m71(v36, "degrees", true);
            boolean v7 = m71(v36, "isLoop", false);
            float v8 = m72(v36, "beginTick", 0.0F);
            float v9 = m72(v36, "endTick", v8);
            float v10 = m72(v36, "stopTick", v9);
            ha.ia v11 = new ha.ia();
            JsonElement v12 = v36.get("moves");
            if (v12 != null && v12.isJsonArray()) {
               for (JsonElement v14 : v12.getAsJsonArray()) {
                  if (v14.isJsonObject()) {
                     JsonObject v15 = v14.getAsJsonObject();
                     float v16 = m72(v15, "tick", 0.0F);
                     ga v17 = ga.parse(m73(v15, "easing", "linear"));

                     for (Entry v19 : v15.entrySet()) {
                        String result = (String)v19.getKey();

                        if (!result.equals("tick") && !((String)v19.getKey()).equals("easing")) {
                           oa v20 = oa.fromJsonKey((String)v19.getKey());
                           if (v20 != null && ((JsonElement)v19.getValue()).isJsonObject()) {
                              JsonObject v21 = ((JsonElement)v19.getValue()).getAsJsonObject();

                              for (Entry v23 : v21.entrySet()) {
                                 fa v38 = fa.fromJsonKey((String)v23.getKey());

                                 fa v24 = v38;
                                 if (v24 != null && ((JsonElement)v23.getValue()).isJsonPrimitive()) {
                                    float v25 = ((JsonElement)v23.getValue()).getAsFloat();
                                    float v26 = v24.isRotation() && v6 ? (float)Math.toRadians((double)v25) : v25;
                                    v11.put(v20, v24, v16, v26, v17);
                                    
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            v37 = new ha(id, displayName, v8, v9, v10, v7, v11.build());
         }

         return v37;
      } catch (Exception v35) {
         LoggerFactory.getLogger(na.class)
            .error("Failed to load bundled emote '{}' from {}", new Object[]{id, resourcePath, v35});
         return new ha(id, displayName, 0.0F, 1.0F, 1.0F, false, Map.of());
      }
   }

   private static float m72(JsonObject object, String key, float fallback) {
      if (object != null && object.has(key)) {

         if (!object.get(key).isJsonNull()) {
            return object.get(key).getAsFloat();
         }
      }

      return fallback;
   }

   
}
