package ru.white.utils.discord;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Dn {
   private final String url;
   private String content;
   private String username;
   private final List<dn.en> embeds = new ArrayList<>();
   private boolean tts;
   private String f1008;

   public void setAvatarUrl(String avatarUrl) {
      this.f1008 = avatarUrl;
   }

   public void execute() throws IOException {
      if (this.content == null && this.embeds.isEmpty()) {
         throw new IllegalArgumentException("Set content or add at least one EmbedObject");
      } else {
         dn.kn v1 = new dn.kn();
         v1.put("content", this.content);
         v1.put("username", this.username);
         v1.put("avatar_url", this.f1008);
         v1.put("tts", this.tts);
         if (!this.embeds.isEmpty()) {
            ArrayList v2 = new ArrayList();

            for (dn.en v4 : this.embeds) {
               dn.kn v5 = new dn.kn();
               v5.put("title", v4.title);
               v5.put("description", v4.description);
               v5.put("url", v4.url);
               if (v4.color != null) {
                  Color v6 = v4.color;
                  int v7 = v6.getRed();
                  v7 = (v7 << 8) + v6.getGreen();
                  v7 = (v7 << 8) + v6.getBlue();
                  v5.put("color", v7);
               }

               dn$hn v18 = v4.footer;
               dn$in v21 = v4.image;
               dn$jn v8 = v4.thumbnail;
               dn$fn v9 = v4.author;
               List v10 = v4.fields;
               if (v18 != null) {
                  dn.kn v11 = new dn.kn();
                  v11.put("text", v18.m1341());
                  v11.put("icon_url", v18.m1342());
                  v5.put("footer", v11);
               }

               if (v21 != null) {
                  dn.kn v22 = new dn.kn();
                  v22.put("url", v21.m1343());
                  v5.put("image", v22);
               }

               if (v8 != null) {
                  dn.kn v23 = new dn.kn();
                  v23.put("url", v8.m1344());
                  v5.put("thumbnail", v23);
               }

               if (v9 != null) {
                  dn.kn v24 = new dn.kn();
                  v24.put("name", v9.m1335());
                  v24.put("url", v9.m1336());
                  v24.put("icon_url", v9.m1337());
                  v5.put("author", v24);
               }

               ArrayList v25 = new ArrayList();

               for (dn$gn v13 : v10) {
                  dn.kn v14 = new dn.kn();
                  v14.put("name", v13.m1338());
                  v14.put("value", v13.m1339());

                  v14.put("inline", v13.m1340());
                  v25.add(v14);
               }

               v5.put("fields", v25.toArray());
               v2.add(v5);
            }

            v1.put("embeds", v2.toArray());
         }

         HttpsURLConnection v16 = (HttpsURLConnection)new URL(this.url).openConnection();
         v16.addRequestProperty("Content-Type", "application/json");
         v16.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
         v16.setDoOutput(true);
         v16.setRequestMethod("POST");
         OutputStream v17 = v16.getOutputStream();
         v17.write(v1.toString().getBytes(StandardCharsets.UTF_8));
         v17.flush();
         v17.close();
         v16.getInputStream().close();
         v16.disconnect();
      }
   }

   public void setContent(String content) {

      this.content = content;
   }

   public void setUsername(String username) {

      this.username = username;
   }

   public Dn(String url) {
      this.url = url;
   }

   public void addEmbed(dn.en embed) {

      this.embeds.add(embed);
   }

   public void setTts(boolean tts) {
      int v2 = -394573962;

      this.tts = tts;
   }

   @Environment(EnvType.CLIENT)
   public static class en {
      private dn$in image;
      private dn$fn author;
      private final List<dn$gn> fields = new ArrayList<>();
      private String description;
      private dn$jn thumbnail;
      private String title;
      private Color color;
      private dn$hn footer;
      private String url;

      public dn.en setUrl(String url) {
         int v2 = 1255898329;

         this.url = url;
         return this;
      }

      public dn.en setImage(String url) {
         dn$in temp1 = new dn$in;

         temp1./* $VF: Unable to resugar constructor */<init>(url);
         this.image = temp1;

         return this;
      }

      public dn.en setDescription(String description) {
         this.description = description;

         return this;
      }

      public dn.en addField(String name, String value, boolean inline) {
         List result = this.fields;
         dn$gn temp1 = new dn$gn(name, value, inline);

         result.add(temp1);
         return this;
      }

      public dn.en setThumbnail(String url) {
         this.thumbnail = new dn$jn(url);

         return this;
      }

      public dn.en setFooter(String text, String icon) {
         dn$hn temp1 = new dn$hn;
         int v3 = 955428063;

         temp1./* $VF: Unable to resugar constructor */<init>(text, icon);
         this.footer = temp1;
         return this;
      }

      public dn.en setAuthor(String name, String url, String icon) {
         dn$fn temp1 = new dn$fn;

         temp1./* $VF: Unable to resugar constructor */<init>(name, url, icon);
         this.author = temp1;
         return this;
      }

      public dn.en setColor(Color color) {
         int v3 = 1227360329;

         this.color = color;
         return this;
      }

      public dn.en setTitle(String title) {
         this.title = title;
         int v2 = -658892825;

         return this;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class kn {
      private final HashMap<String, Object> f1007 = new HashMap<>();

      void put(String key, Object value) {
         if (value != null) {
            this.f1007.put(key, value);
            
         }
      }

      private String m1345(String string) {
         StringBuilder result = new StringBuilder().append("\"").append((Object)string);
         int v3 = -938301580;

         int temp2 = v3 * (v3 + 1);

         return result.append("\"").toString();
      }

      @Override
      public String toString() {
         StringBuilder v1 = new StringBuilder();
         Set v2 = this.f1007.entrySet();
         v1.append("{");
         int v3 = 0;

         for (Entry v5 : v2) {
            Object v6 = v5.getValue();
            v1.append(this.m1345((String)v5.getKey())).append(":");
            if (v6 instanceof String) {
               v1.append(this.m1345(String.valueOf(v6)));
            } else if (v6 instanceof Integer) {
               v1.append(Integer.valueOf(String.valueOf(v6)));
            } else if (v6 instanceof Boolean) {
               v1.append(v6);
            } else if (v6 instanceof dn.kn) {
               v1.append(v6);
            } else if (v6.getClass().isArray()) {
               v1.append("[");

               int v7 = Array.getLength(v6);

               for (int v8 = 0; v8 < v7; v8++) {
                  v1.append(Array.get(v6, v8).toString()).append(v8 != v7 - 1 ? "," : "");
               }

               v1.append("]");
            }

            v3++;
            v1.append(v3 == v2.size() ? "}" : ",");
         }

         return v1.toString();
      }

      
   }
}
