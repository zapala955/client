package ru.white.utils.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Vq {
   private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
   private static final Pattern INTRO_CHORUS_BRACKET_PATTERN = Pattern.compile(
      "(?i)(?U)[\\(\\[][^\\]\\)]*?(?:интро|припев|куплет|аутро|бридж|скит|intro|chorus|verse|outro|bridge|hook|текст\\s*песни)[^\\]\\)]*?[\\)\\]]"
   );
   private static final Map<String, vq.xq> CACHE = new ConcurrentHashMap<>();
   private static final Pattern URL_PROMO_PATTERN = Pattern.compile(
      "(?i)^.*(?:https?://|www\\.[a-z0-9\\-]+\\.[a-z]+|[a-z0-9\\-]+\\.(?:com|ru|net|org|io|me|fm|EventPacket|to|ly|gg|app|xyz)|vk\\.com/|t\\.me/|instagram\\.com/).*$"
   );
   private static final String LRCLIB_BASE = "https://lrclib.net/api";
   private static final Pattern INTRO_CHORUS_WORD_PATTERN = Pattern.compile(
      "(?i)(?U)\\b(?:интро|припев)\\b"
   );
   private static final Pattern CYRILLIC_PATTERN = Pattern.compile("[а-яА-ЯёЁ]");
   private static final Pattern OFFSET_PATTERN = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\]", 2);
   public static final float LEAD_OFFSET_SECONDS = 0.2F;
   private static final String GENIUS_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(-6604361192701233262L + 6604361192701233266L))
      .build();
   private static final Pattern METADATA_LINE_PATTERN = Pattern.compile(
      "(?i)^\\s*(?:(?:written|produced|composed|arranged|mixed|mastered|lyrics|music)\\s*by|author|composer|producer|sound\\s*producer|beatmaker|битмейкер|автор\\s*(?:слов|музыки|текста)?|текст\\s*песни|слова\\s*песни|сведение|мастеринг|релиз|перевод|транскрипция|(?:lrc|sync|synced|timed|encoded|subtitles|karaoke)\\s*by|source|источник|сайт|tg|vk|inst|instagram|telegram|twitter|discord|credit|credits)\\s*[:：\\-–—].*"
   );
   private static final String USER_AGENT = "RoseSolution/1.0 (https://github.com/RoseSolution)";
   private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2}(?:\\.\\d+)?)\\]");
   private static final Pattern INTRO_CHORUS_SUFFIX_PATTERN = Pattern.compile(
      "(?i)(?U)\\s*[:\\-–—]?\\s*(?:интро|припев|куплет|постприпев|предприпев|пост-припев|пред-припев|аутро|бридж|скит|intro|chorus|verse|outro|bridge)(?:\\s*\\d+)?\\s*$"
   );
   private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
      Thread result = new Thread;

      result./* $VF: Unable to resugar constructor */<init>(r, "rose-lyrics-fetcher");
      Thread v1 = result;
      v1.setDaemon(true);
      return v1;
   });
   private static final Pattern SECTION_HEADER_LINE_PATTERN = Pattern.compile(
      "(?i)^\\s*\\[\\s*(?:текст\\s*песни|слова\\s*песни|куплет|припев|интро|аутро|бридж|скит|переход|финал|verse|chorus|intro|outro|bridge|hook|drop|pre-chorus|post-chorus|part|часть|interlude|skit).*?\\]\\s*$"
   );
   private static final Pattern INTRO_CHORUS_PREFIX_PATTERN = Pattern.compile(
      "(?i)(?U)^\\s*(?:интро|припев|куплет|постприпев|предприпев|пост-припев|пред-припев|аутро|бридж|скит|intro|chorus|verse|outro|bridge)(?:\\s*\\d+)?(?:\\s*x\\d+)?(?:\\s*[:\\-–—])?\\s*"
   );

   private static String m1638(String s) {
      if (s != null && !s.isEmpty()) {
         String v1 = s.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&#8217;", "'")
            .replace("&#8216;", "'")
            .replace("&#8220;", "\"")
            .replace("&#8221;", "\"")
            .replace("&#8212;", "—")
            .replace("&#8211;", "–");
         Pattern v2 = Pattern.compile("&#(x[0-9a-fA-F]+|\\d+);");
         Matcher v3 = v2.matcher(v1);
         StringBuilder v4 = new StringBuilder();

         while (v3.find()) {
            String v5 = v3.group(1);

            try {
               int v6 = !v5.startsWith("x") && !v5.startsWith("X")
                  ? Integer.parseInt(v5)
                  : Integer.parseInt(v5.substring(1), 16);
               String temp2 = Character.toString((char)v6);

               v3.appendReplacement(v4, Matcher.quoteReplacement(temp2));
            } catch (Exception v8) {
               v3.appendReplacement(v4, Matcher.quoteReplacement(v3.group(0)));
            }
         }

         v3.appendTail(v4);
         return v4.toString();
      } else {
         return "";
      }
   }

   private static int m1627(String a, String b) {
      if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
         return 0;
      } else if (a.equals(b)) {
         return 100;
      } else if (!a.contains(b) && !b.contains(a)) {
         String[] v2 = a.split("\\s+");
         String[] v3 = b.split("\\s+");
         HashSet v4 = new HashSet();

         for (String v8 : v2) {
            if (v8.length() > 1) {
               v4.add(v8);
            }
         }

         if (v4.isEmpty()) {
            return 0;
         } else {
            int v11 = 0;

            for (String v9 : v3) {
               if (v9.length() > 1 && v4.contains(v9)) {
                  v11++;
               }
            }

            if (v11 == 0) {
               return 0;
            } else {
               int v13 = Math.max(v4.size(), (int)Arrays.stream(v3).filter(t -> {
                  int result = t.length();

                  return result > 1;
               }).count());
               return v13 == 0 ? 0 : Math.min(100, v11 * (100) / v13);
            }
         }
      } else {
         return 85;
      }
   }

   private static vq.xq m1619(String rawArtist, String rawTitle, int duration) {
      String v3 = rawArtist != null ? rawArtist.trim() : "";
      String v4 = rawTitle != null ? rawTitle.trim() : "";
      v4 = m1631(v4);
      v3 = m1631(v3);
      boolean v5 = !v3.isEmpty() && !v3.equals("—") && !m1620(v3);
      String v6;
      String v7;
      if (v5) {
         v6 = v3;
         String v8 = v4.toLowerCase();
         String v9 = v3.toLowerCase();
         if (v8.startsWith(v9 + " - ")) {
            v7 = v4.substring(v3.length() + 3).trim();
         } else if (v8.startsWith(v9 + " — ")) {
            v7 = v4.substring(v3.length() + 3).trim();
         } else if (v8.startsWith(v9 + " – ")) {
            v7 = v4.substring(v3.length() + 3).trim();
         } else {
            v7 = v4;
         }
      } else {
         String v33 = "";
         String v35 = v4;
         int v10 = v4.indexOf(" - ");
         if (v10 == -1) {
            v10 = v4.indexOf(" — ");
         }

         if (v10 == -1) {
            v10 = v4.indexOf(" – ");
         }

         if (v10 != -1) {
            v33 = v4.substring(0, v10).trim();
            v35 = v4.substring(v10 + 3).trim();
         }

         if (m1620(v33)) {
            v33 = "";
         }

         v6 = v33;
         v7 = v35;
      }

      String v34 = m1632(v6);
      String v36 = clean(v7);
      String v37 = clean(v4);
      String v11 = clean(v6);
      String v12 = clean(v34);
      boolean v13 = CYRILLIC_PATTERN.matcher(v36).find() || CYRILLIC_PATTERN.matcher(v11).find() || CYRILLIC_PATTERN.matcher(v37).find();
      vq.xq v14 = null;
      if (duration > 0 && !v11.isEmpty() && !v36.isEmpty()) {
         vq.xq v15 = m1621(v11, v36, duration, v13);
         if (isValidSynced(v15, duration) && v15.matchesTrack(v11, v36)) {
            return v15;
         }

         if (v14 == null) {

            if (v15 != null && !v15.isEmpty() && v15.matchesTrack(v11, v36)) {
               v14 = v15;
            }
         }
      }

      if (duration > 0 && !v12.isEmpty() && !v12.equalsIgnoreCase(v11) && !v36.isEmpty()) {
         vq.xq v38 = m1621(v12, v36, duration, v13);
         if (isValidSynced(v38, duration) && v38.matchesTrack(v12, v36)) {
            return v38;
         }

         if (v14 == null && v38 != null && !v38.isEmpty() && v38.matchesTrack(v12, v36)) {
            v14 = v38;
         }
      }

      if (!v11.isEmpty() && !v36.isEmpty()) {
         vq.xq v39 = m1622(v11, v36, v13, duration);
         if (isValidSynced(v39, duration) && v39.matchesTrack(v11, v36)) {
            return v39;
         }

         if (v14 == null && v39 != null && !v39.isEmpty() && v39.matchesTrack(v11, v36)) {
            v14 = v39;
         }
      }

      if (!v11.isEmpty() && !v36.isEmpty()) {
         vq.xq v40 = m1623(v11, v36, v11 + " " + v36, v13, duration);
         if (isValidSynced(v40, duration) && v40.matchesTrack(v11, v36)) {
            return v40;
         }

         if (v14 == null && v40 != null && !v40.isEmpty() && v40.matchesTrack(v11, v36)) {
            v14 = v40;
         }
      }

      if (!v12.isEmpty() && !v12.equalsIgnoreCase(v11) && !v36.isEmpty()) {
         vq.xq v41 = m1623(v12, v36, v12 + " " + v36, v13, duration);
         if (isValidSynced(v41, duration) && v41.matchesTrack(v12, v36)) {
            return v41;
         }

         if (v14 == null && v41 != null && !v41.isEmpty() && v41.matchesTrack(v12, v36)) {
            v14 = v41;
         }
      }

      if (v13) {
         String v42 = transliterate(v11);
         String v16 = transliterate(v36);
         if (!v42.isEmpty() && !v16.isEmpty()) {
            vq.xq v17 = m1623(v11, v36, v42 + " " + v16, v13, duration);
            if (isValidSynced(v17, duration) && v17.matchesTrack(v11, v36)) {
               return v17;
            }

            if (v14 == null && v17 != null && !v17.isEmpty() && v17.matchesTrack(v11, v36)) {
               v14 = v17;
            }
         }
      }

      if (!v11.isEmpty() && !v36.isEmpty()) {
         vq.xq v43 = m1621(v11, v36, 0, v13);
         if (isValidSynced(v43, duration) && v43.matchesTrack(v11, v36)) {
            return v43;
         }

         if (v14 == null && v43 != null && !v43.isEmpty() && v43.matchesTrack(v11, v36)) {
            v14 = v43;
         }
      }

      if (!v36.isEmpty() && !v11.isEmpty()) {
         vq.xq v44 = m1623(v11, v36, v11 + " " + v36, v13, duration);
         if (isValidSynced(v44, duration) && v44.matchesTrack(v11, v36)) {
            return v44;
         }

         if (v14 == null && v44 != null && !v44.isEmpty() && v44.matchesTrack(v11, v36)) {
            v14 = v44;
         }
      }

      if (v14 != null && v14.matchesTrack(v11, v36)) {
         return v14;
      } else {
         vq.xq v45 = m1633(v11, v36, v12, v37, v13);
         if (v45 != null && !v45.isEmpty()) {

            if (v45.matchesTrack(v11, v36)) {
               return v45;
            }
         }

         if (!v11.isEmpty() && !v36.isEmpty()) {
            vq.xq v46 = m1622(v11, v36, v13, duration);
            if (v46 != null && !v46.isEmpty() && !v46.isNotFound() && v46.matchesTrack(v11, v36)) {
               return v46;
            }
         }

         return v14 != null ? v14 : vq.xq.NOT_FOUND;
      }
   }

   private static vq.xq m1623(String rawArtist, String rawTitle, String query, boolean preferCyrillic, int duration) {
      try {
         String v8 = URLEncoder.encode(query, StandardCharsets.UTF_8);
         String v5 = "https://lrclib.net/api/search?q=" + v8;
         HttpRequest v6 = HttpRequest.newBuilder()
            .uri(URI.create(v5))
            .header("User-Agent", "RoseSolution/1.0 (https://github.com/RoseSolution)")
            .timeout(Duration.ofSeconds(-1560118333306080547L + 1560118333306080551L))
            .GET()
            .build();
         HttpResponse v7 = HTTP_CLIENT.send(v6, BodyHandlers.ofString(StandardCharsets.UTF_8));

         if (v7.statusCode() == 200) {
            return m1624((String)v7.body(), rawArtist, rawTitle, preferCyrillic, duration);
         }
      } catch (Throwable v10) {
      }

      return null;
   }

   private static String m1637(String html) {
      String v1 = "data-exclude-from-selection=\"true\"";
      StringBuilder v2 = new StringBuilder();
      int v3 = 0;

      while (true) {
         int v4 = html.indexOf(v1, v3);
         if (v4 == -1) {
            v2.append(html.substring(v3));
            break;
         }

         int v5 = html.lastIndexOf(60, v4);
         if (v5 != -1 && v5 >= v3) {
            v2.append(html.substring(v3, v5));
            int v6 = html.indexOf(32, v5);
            int v7 = html.indexOf(62, v5);
            if (v7 == -1) {
               break;
            }

            int v8 = v6 != -1 && v6 < v7 ? v6 : v7;
            String v9 = html.substring(v5 + 1, v8).trim().toLowerCase();
            String v10 = "<" + v9;
            String v11 = "</" + v9 + ">";
            int v12 = 1;
            int v13 = v7 + 1;

            while (true) {
               if (v13 < html.length() && v12 > 0) {
                  int v14 = html.indexOf(v10, v13);
                  int v15 = html.indexOf(v11, v13);
                  if (v15 != -1) {
                     if (v14 != -1 && v14 < v15) {

                        v12++;
                        v13 = v14 + v10.length();
                        continue;
                     }

                     v12--;
                     v13 = v15 + v11.length();
                     continue;
                  }
               }

               v3 = v13;
               break;
            }
         } else {
            v2.append(html.substring(v3, v4 + v1.length()));
            v3 = v4 + v1.length();
         }
      }

      return v2.toString();
   }

   private static vq.xq m1634(String query, String expectedArtist, String expectedTitle, String expectedPrimaryArtist) {
      try {
         String v24 = URLEncoder.encode(query, StandardCharsets.UTF_8);
         String v4 = "https://genius.com/api/search/song?q=" + v24;
         HttpRequest v5 = HttpRequest.newBuilder()
            .uri(URI.create(v4))
            .header(
               "User-Agent",
               "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(4259714252644198408L + -4259714252644198403L))
            .GET()
            .build();
         HttpResponse v6 = HTTP_CLIENT.send(v5, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (v6.statusCode() != 200) {
            return null;
         }

         JsonElement v7 = JsonParser.parseString((String)v6.body());
         if (!v7.isJsonObject()) {
            return null;
         }

         JsonObject v8 = v7.getAsJsonObject();
         if (!v8.has("response") || !v8.get("response").isJsonObject()) {
            return null;
         }

         JsonObject v9 = v8.getAsJsonObject("response");
         if (!v9.has("sections") || !v9.get("sections").isJsonArray()) {
            return null;
         }

         for (JsonElement v12 : v9.getAsJsonArray("sections")) {
            if (v12.isJsonObject()) {
               JsonObject v13 = v12.getAsJsonObject();
               if (v13.has("type") && "song".equals(v13.get("type").getAsString()) && v13.has("hits") && v13.get("hits").isJsonArray()) {
                  for (JsonElement v16 : v13.getAsJsonArray("hits")) {
                     if (v16.isJsonObject()) {
                        JsonObject v17 = v16.getAsJsonObject();
                        if (v17.has("result") && v17.get("result").isJsonObject()) {
                           JsonObject v18 = v17.getAsJsonObject("result");
                           if (v18.has("instrumental")
                              && v18.get("instrumental").getAsBoolean()) {
                              return new vq.xq(Collections.emptyList(), Collections.emptyList(), true, false, "");
                           }

                           String v19 = v18.has("title") && !v18.get("title").isJsonNull() ? clean(v18.get("title").getAsString()).toLowerCase() : "";
                           String v20 = "";
                           if (v18.has("primary_artist")
                              && v18.get("primary_artist").isJsonObject()) {
                              JsonObject v21 = v18.getAsJsonObject("primary_artist");
                              if (v21.has("name") && !v21.get("name").isJsonNull()) {
                                 v20 = clean(v21.get("name").getAsString()).toLowerCase();
                              }
                           }

                           if (!expectedTitle.isEmpty() && !v19.isEmpty()) {
                              int v27 = Math.max(m1627(expectedTitle.toLowerCase(), v19), m1627(transliterate(expectedTitle).toLowerCase(), v19));
                              if (v27 < 40) {
                                 continue;
                              }
                           }

                           if (!expectedArtist.isEmpty() && !v20.isEmpty()) {
                              int v28 = Math.max(
                                 m1627(expectedArtist.toLowerCase(), v20),
                                 Math.max(m1627(transliterate(expectedArtist).toLowerCase(), v20), m1627(expectedPrimaryArtist.toLowerCase(), v20))
                              );
                              if (v28 < 30
                                 && !v20.contains(expectedArtist.toLowerCase())
                                 && !expectedArtist.toLowerCase().contains(v20)) {
                                 continue;
                              }
                           }

                           if (v18.has("url") && !v18.get("url").isJsonNull()) {
                              String v29 = v18.get("url").getAsString();
                              if (v29 != null && !v29.isEmpty() && v29.contains("genius.com")) {
                                 String v22 = v29.toLowerCase();
                                 if (!v22.contains("-translation")
                                    && !v22.contains("-traduccion")
                                    && !v22.contains("-traduction")) {
                                    if (v22.contains("-perevod")) {
                                       
                                    } else {
                                       vq.xq v23 = m1635(v29);
                                       if (v23 != null && !v23.isEmpty()) {
                                          return v23;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable v26) {
      }

      return null;
   }

   private static vq.xq m1633(String cleanArtist, String cleanTitle, String cleanPrimaryArtist, String cleanRawTitle, boolean isCyrillicQuery) {
      ArrayList v5 = new ArrayList();
      if (!cleanArtist.isEmpty()) {

         if (!cleanTitle.isEmpty()) {
            v5.add(cleanArtist + " " + cleanTitle);
         }
      }

      if (!cleanPrimaryArtist.isEmpty() && !cleanPrimaryArtist.equalsIgnoreCase(cleanArtist) && !cleanTitle.isEmpty()) {
         v5.add(cleanPrimaryArtist + " " + cleanTitle);
      }

      if (isCyrillicQuery) {
         String v6 = transliterate(cleanArtist);
         String v7 = transliterate(cleanTitle);
         if (!v6.isEmpty() && !v7.isEmpty()) {
            StringBuilder v24 = new StringBuilder().append((Object)v6);

            v5.add(v24.append(" ").append((Object)v7).toString());
         }
      }

      if (!cleanArtist.isEmpty() && !cleanTitle.isEmpty()) {
         v5.add(cleanTitle + " " + cleanArtist);
      }

      HashSet v22 = new HashSet();

      for (String v8 : v5) {
         String v9 = v8.trim();
         if (!v9.isEmpty()) {
            boolean result = v22.add(v9.toLowerCase());

            if (result) {
               vq.xq v10 = m1634(v9, cleanArtist, cleanTitle, cleanPrimaryArtist);
               if (v10 != null && !v10.isEmpty()) {
                  return v10;
               }
            }
         }
      }

      return null;
   }

   public static String transliterate(String input) {
      if (input != null && !input.isEmpty()) {
         StringBuilder v1 = new StringBuilder();
         String v2 = input.toLowerCase();

         for (int v3 = 0; v3 < v2.length(); v3++) {
            char v4 = v2.charAt(v3);
            switch (v4) {
               case 'а':
                  v1.append("a");
                  break;
               case 'б':
                  v1.append("b");
                  break;
               case 'в':
                  v1.append("v");
                  break;
               case 'г':
                  v1.append("g");
                  break;
               case 'д':
                  v1.append("d");
                  break;
               case 'е':
               case 'э':
               case 'ё':
                  v1.append("e");
                  break;
               case 'ж':
                  v1.append("zh");
                  break;
               case 'з':
                  v1.append("z");
                  break;
               case 'и':
               case 'й':
               case 'ы':
                  v1.append("i");
                  break;
               case 'к':
                  v1.append("k");
                  break;
               case 'л':
                  v1.append("l");
                  break;
               case 'м':
                  v1.append("m");
                  break;
               case 'н':
                  v1.append("n");
                  break;
               case 'о':
                  v1.append("o");
                  break;
               case 'п':
                  v1.append("p");
                  break;
               case 'р':
                  v1.append("r");
                  break;
               case 'с':
                  v1.append("s");
                  break;
               case 'т':
                  v1.append("t");
                  break;
               case 'у':
                  v1.append("u");
                  break;
               case 'ф':
                  v1.append("f");
                  break;
               case 'х':
                  v1.append("h");
                  break;
               case 'ц':
                  v1.append("ts");
                  break;
               case 'ч':
                  v1.append("ch");
                  break;
               case 'ш':
                  v1.append("sh");
                  break;
               case 'щ':
                  v1.append("shch");
               case 'ъ':
               case 'ь':
                  break;
               case 'ю':
                  v1.append("yu");
                  break;
               case 'я':
                  v1.append("ya");
                  break;
               case 'ѐ':
               default:
                  v1.append(v4);
            }
         }

         return v1.toString();
      } else {
         return "";
      }
   }

   private static String m1630(String line) {
      if (line == null) {
         return "";
      } else {
         String v1 = line.trim();
         if (m1618(v1)) {
            return "";
         } else {
            v1 = INTRO_CHORUS_BRACKET_PATTERN.matcher(v1).replaceAll("");
            v1 = INTRO_CHORUS_PREFIX_PATTERN.matcher(v1).replaceAll("");
            v1 = INTRO_CHORUS_SUFFIX_PATTERN.matcher(v1).replaceAll("");
            v1 = INTRO_CHORUS_WORD_PATTERN.matcher(v1).replaceAll("");
            String result = v1.replaceAll("\\s+([\\'\\’\\`])", "$1");

            v1 = result.replaceAll("([\\'\\’\\`])\\s+", "$1").replaceAll("\\s+", " ").trim();
            return v1.replaceAll("[\\p{P}\\p{Z}\\s]", "").isEmpty() ? "" : v1;
         }
      }
   }

   private static vq.xq m1622(String artist, String title, boolean preferCyrillic, int duration) {
      try {
         String result = URLEncoder.encode(title, StandardCharsets.UTF_8);

         String v7 = URLEncoder.encode(artist, StandardCharsets.UTF_8);
         String v8 = result;
         StringBuilder v12 = new StringBuilder();

         String v4 = v12.append("https://lrclib.net/api/search?track_name=")
            .append((Object)v8)
            .append("&artist_name=")
            .append((Object)v7)
            .toString();
         HttpRequest v5 = HttpRequest.newBuilder()
            .uri(URI.create(v4))
            .header("User-Agent", "RoseSolution/1.0 (https://github.com/RoseSolution)")
            .timeout(Duration.ofSeconds(8536633420874856403L + -8536633420874856399L))
            .GET()
            .build();
         HttpResponse v6 = HTTP_CLIENT.send(v5, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (v6.statusCode() == 200) {
            return m1624((String)v6.body(), artist, title, preferCyrillic, duration);
         }
      } catch (Throwable v11) {
      }

      return null;
   }

   private static String m1631(String str) {
      if (str == null) {
         return "";
      } else {
         String result = str.replaceAll(
            "(?i)\\s*-\\s*(YouTube|YouTube Music|Яндекс Музыка|Яндекс\\.Музыка|Spotify|VK Музыка|VK|SoundCloud|Apple Music).*",
            ""
         );

         return result.replaceAll(
               "(?i)\\|\\s*(Official Video|Music Video|Visualizer|Mood Video|Audio|Клип).*",
               ""
            )
            .trim();
      }
   }

   private static boolean m1618(String line) {
      if (line != null && !line.isBlank()) {
         String v1 = line.trim();
         if (METADATA_LINE_PATTERN.matcher(v1).matches()) {
            return true;
         } else if (URL_PROMO_PATTERN.matcher(v1).matches()) {
            return true;
         } else if (SECTION_HEADER_LINE_PATTERN.matcher(v1).matches()) {
            return true;
         } else {
            int temp2 = -1268997228;

            return v1.matches(StrDec.d("af9aJva/8PpvNXp4FJ6f3YwRE0kTloQ9rGE5pSDktzu4f8HPEJAOL+gF", temp2));
         }
      } else {
         return true;
      }
   }

   public static void clearCache() {
      CACHE.clear();
      int v0 = 465193515;
      int temp1 = v0 * (v0 + 1);
      int v1 = 1212592529;

      PENDING.clear();
   }

   private static vq.xq m1624(String jsonStr, String queryArtist, String queryTitle, boolean preferCyrillic, int duration) {
      try {
         JsonElement v5 = JsonParser.parseString(jsonStr);
         if (!v5.isJsonArray()) {
            return null;
         }

         JsonArray v6 = v5.getAsJsonArray();
         String result = clean(queryArtist);

         String v7 = result.toLowerCase();
         String v8 = clean(queryTitle).toLowerCase();
         String v9 = (v7 + " " + v8).trim();
         vq.xq v10 = null;
         int v11 = -1;
         vq.xq v12 = null;
         int v13 = -1;
         int v14 = 65;

         for (JsonElement v16 : v6) {
            if (v16.isJsonObject()) {
               JsonObject v17 = v16.getAsJsonObject();
               String v18 = v17.has("artistName") && !v17.get("artistName").isJsonNull() ? v17.get("artistName").getAsString() : "";
               String v19 = v17.has("trackName") && !v17.get("trackName").isJsonNull() ? v17.get("trackName").getAsString() : "";
               int v20 = v17.has("duration") && !v17.get("duration").isJsonNull() ? v17.get("duration").getAsInt() : 0;
               vq.xq v21 = m1629(v17);
               if (v21 != null) {
                  if (v21.instrumental) {
                     return v21;
                  }

                  boolean v22 = !v21.f1283.isEmpty();
                  boolean v23 = !v21.f1284.isEmpty();
                  if (v22 || v23) {
                     int v24 = m1625(v7, v8, v9, v18, v19, v20, duration, v21, preferCyrillic);
                     if (v24 > 0) {
                        if (v22 && v24 > v11) {
                           v10 = v21;
                           v11 = v24;
                        }

                        if (v23 && v24 > v13) {
                           v12 = v21;
                           v13 = v24;
                        }
                     }
                  }
               }
            }
         }

         if (v10 != null && v11 >= v14) {
            return v10;
         }

         if (v12 != null && v13 >= v14) {
            return v12;
         }
      } catch (Throwable v30) {
      }

      return null;
   }

   public static vq.yq getLyricsView(String artist, String title, float position, float duration) {
      if (title != null && !title.isBlank() && !title.equals("Нет трека")) {

         if (!title.equals("—")) {
            String v4 = artist != null ? artist.trim() : "";
            String v5 = title.trim();
            String v13 = v4.toLowerCase();
            String v8 = v5.toLowerCase();
            String v9 = v13;
            String v6 = v9 + "|" + v8;
            vq.xq v7 = CACHE.get(v6);

            if (v7 != null) {
               if (!v7.isNotFound() && !v7.isEmpty()) {
                  return v7.getView(position, duration);
               }

               vq.yq v15 = new vq.yq;

               v15./* $VF: Unable to resugar constructor */<init>("", "", "", "", false, false, true, false, false, 0.0F);
               return v15;
            }

            if (PENDING.add(v6)) {
               EXECUTOR.submit(() -> {
                  try {
                     vq.xq v4x = m1619(v4, v5, (int)duration);
                     CACHE.put(v6, v4x != null ? v4x : vq.xq.NOT_FOUND);
                     
                  } catch (Throwable v9x) {
                     CACHE.put(v6, vq.xq.NOT_FOUND);
                  } finally {
                     PENDING.remove(v6);
                  }
               });
            }

            return new vq.yq(
               "", "", "Загрузка текста...", "", false, true, false, false, false, 0.0F
            );
         }
      }

      return new vq.yq("", "", "Нет трека", "", false, false, false, false, false, 0.0F);
   }

   private static List<String> m1636(String html) {
      ArrayList v1 = new ArrayList();
      String v2 = "data-lyrics-container=\"true\"";
      int v3 = 0;

      while ((v3 = html.indexOf(v2, v3)) != -1) {
         int v4 = html.lastIndexOf("<div", v3);
         if (v4 == -1) {
            v3 += v2.length();
         } else {
            int v5 = html.indexOf(62, v3);
            if (v5 == -1) {
               break;
            }

            v5++;
            int v6 = 1;
            int v7 = v5;

            while (true) {
               if (v7 < html.length() && v6 > 0) {

                  int v8 = html.indexOf("<div", v7);
                  int v9 = html.indexOf("</div>", v7);
                  if (v9 != -1) {
                     if (v8 != -1 && v8 < v9) {
                        v6++;
                        v7 = v8 + 4;
                        continue;
                     }

                     if (--v6 != 0) {
                        v7 = v9 + 6;
                        continue;
                     }

                     v1.add(html.substring(v5, v9));
                     v7 = v9 + 6;
                     v3 = v7;
                  }
               }

               if (v6 != 0) {
                  v3 = v5;
               }
               break;
            }
         }
      }

      return v1;
   }

   private static vq.xq m1629(JsonObject obj) {
      float v1 = obj.has("duration") && !obj.get("duration").isJsonNull() ? obj.get("duration").getAsFloat() : 0.0F;
      String v2 = obj.has("artistName") && !obj.get("artistName").isJsonNull() ? obj.get("artistName").getAsString() : "";
      String v3 = obj.has("trackName") && !obj.get("trackName").isJsonNull() ? obj.get("trackName").getAsString() : "";
      boolean v4 = obj.has("instrumental")
         && obj.get("instrumental").getAsBoolean();
      if (v4) {
         return new vq.xq(Collections.emptyList(), Collections.emptyList(), true, false, "", v1, v3, v2);
      } else {
         String v5 = obj.has("syncedLyrics")
               && !obj.get("syncedLyrics").isJsonNull()
            ? obj.get("syncedLyrics").getAsString()
            : null;
         String v6 = obj.has("plainLyrics") && !obj.get("plainLyrics").isJsonNull() ? obj.get("plainLyrics").getAsString() : null;
         ArrayList v7 = new ArrayList();
         StringBuilder v8 = new StringBuilder();
         float v9 = 0.0F;
         if (v5 != null && !v5.isBlank()) {
            Matcher v10 = OFFSET_PATTERN.matcher(v5);
            if (v10.find()) {
               try {
                  int v11 = Integer.parseInt(v10.group(1).trim());
                  v11 = Math.max(-15000, Math.min(15000, v11));
                  v9 = (float)v11 / 1000.0F;
               } catch (Exception v25) {
               }
            }

            v8.append(v5).append("\n");
            String[] v28 = v5.split("\\r?\\n");

            for (String v15 : v28) {
               if (!v15.isBlank()) {
                  Matcher v16 = TIMESTAMP_PATTERN.matcher(v15);
                  ArrayList v17 = new ArrayList();

                  int v18;
                  for (v18 = 0; v16.find(); v18 = v16.end()) {
                     float v19 = Float.parseFloat(v16.group(1));
                     float v20 = Float.parseFloat(v16.group(2));
                     v17.add(v19 * 60.0F + v20);
                  }

                  String result = m1630(v15.substring(v18).trim());

                  String v35 = result;
                  if (!v35.isEmpty()) {
                     for (Float v21 : v17) {
                        float v22 = Math.max(0.0F, v21 - v9);
                        v7.add(new vq.wq(v22, v35));
                     }
                  }
               }
            }

            Collections.sort(v7);
         }

         ArrayList v26 = new ArrayList();
         if (v6 != null && !v6.isBlank()) {
            v8.append(v6);
            String[] v29 = v6.split("\\r?\\n");
            String[] v30 = v29;
            int v31 = v29.length;
            int v32 = 0;

            for (; v32 < v31; v32++) {
               String v33 = v30[v32];
               String v34 = m1630(v33.trim());
               if (!v34.isEmpty()) {
                  v26.add(v34);
               }
            }
         }

         return v7.isEmpty() && v26.isEmpty() ? null : new vq.xq(v7, v26, false, false, v8.toString(), v1, v3, v2);
      }
   }

   public static String clean(String str) {
      if (str == null) {
         return "";
      } else {

         int temp2 = -143609224;

         return str.replaceAll(
               StrDec.d(
                  "841EZDK0ioOtA2t/2cX12GkDOA3G3etwQ3g/rY/liX9HcvnVtMBsbHcP48+eKkZqBbfNvpQ9VQfkhaeXdxcwF+iLnHEedgXsyOebaDJb89G212UJPErguZogXiYToM3mlSMxEPmDtJl7CSxPyvyWf1hiE+TP6p9AOEbnya3MZ10Pd/+Iqht2ETWd+ItFOA4rwLqVoEg6F9/5xKdfJFIixPbXTH0aec3iivlQcTA1ODp8vqmh7YdMe/zEu/5obSdS+/eMPU4H+XNfdBr73cAQSSNL9aC3",
                  temp2
               ),
               ""
            )
            .replaceAll(
               "(?i)\\s*-\\s*(remastered|remaster|remix|edit|single|live|radio edit|deluxe|version|sped up|slowed).*",
               ""
            )
            .replaceAll("[\\\"\\'\\`«»]", "")
            .replaceAll("\\s+", " ")
            .trim();
      }
   }

   private static boolean m1626(String text) {
      if (text == null) {
         return false;
      } else {
         String v1 = text.toLowerCase();
         if (!v1.contains("romanized")
            && !v1.contains("romaji")
            && !v1.contains("transliteration")
            && !v1.contains("english translation")) {

            if (!v1.contains("english version")
               && !v1.contains("eng ver")
               && !v1.contains("traduction")
               && !v1.contains("tradução")) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean m1620(String s) {
      if (s != null && !s.isEmpty()) {
         String v1 = s.toLowerCase();
         if (!v1.contains("chrome")
            && !v1.contains("firefox")
            && !v1.contains("opera")
            && !v1.contains("edge")
            && !v1.contains("brave")
            && !v1.contains("яндекс")) {
            boolean result = v1.contains("yandex");

            if (!result
               && !v1.contains("youtube")
               && !v1.contains("vk")
               && !v1.contains("вконтакте")
               && !v1.contains("soundcloud")
               && !v1.contains("browser")) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static vq.xq m1628(String jsonStr) {
      try {
         JsonElement v1 = JsonParser.parseString(jsonStr);
         if (v1.isJsonObject()) {

            return m1629(v1.getAsJsonObject());
         }
      } catch (Throwable v3) {
      }

      return null;
   }

   private static int m1625(
      String cleanArtist,
      String cleanTitle,
      String cleanJoined,
      String resArtist,
      String resTitle,
      int resDur,
      int queryDur,
      vq.xq data,
      boolean preferCyrillic
   ) {
      if (resTitle == null && resArtist == null) {
         return 0;
      } else {
         String v9 = resArtist == null ? "" : resArtist;
         String v10 = resTitle == null ? "" : resTitle;
         String v11 = clean(v9).toLowerCase();
         String v12 = clean(v10).toLowerCase();
         boolean v13 = m1626(v10) || m1626(v9);
         int v14 = Math.max(m1627(cleanTitle, v12), Math.max(m1627(transliterate(cleanTitle), v12), m1627(cleanTitle, transliterate(v12))));
         if (cleanTitle.equalsIgnoreCase(v12) || transliterate(cleanTitle).equalsIgnoreCase(v12)) {
            v14 = 100;
         }

         if (v14 < 45) {
            return 0;
         } else {
            int v15 = 0;
            if (!cleanArtist.isEmpty() && !v11.isEmpty()) {
               v15 = Math.max(m1627(cleanArtist, v11), Math.max(m1627(transliterate(cleanArtist), v11), m1627(cleanArtist, transliterate(v11))));
               if (v15 < 35 && !v11.contains(cleanArtist) && !cleanArtist.contains(v11)) {
                  return 0;
               }
            } else if (!cleanArtist.isEmpty()) {
               v15 = m1627(cleanArtist, v12) / 2;
               if (v15 < 30) {
                  return 0;
               }
            } else {
               v15 = v14;
            }

            int v16 = (int)((double)v14 * 0.6 + (double)v15 * 0.4);
            if (data != null && !data.f1283.isEmpty()) {
               v16 += 25;
               if (queryDur > 0) {
                  List v17 = data.f1283;
                  float v18 = ((vq.wq)v17.get(0)).timeSeconds();
                  float v19 = ((vq.wq)v17.get(v17.size() - 1)).timeSeconds();
                  if (v18 >= (float)queryDur) {
                     return 0;
                  }

                  if (v19 > (float)queryDur + 3.5F) {

                     return 0;
                  }
               }
            }

            if (queryDur > 0 && resDur > 0) {
               int v27 = Math.abs(queryDur - resDur);
               if (v27 > 4) {
                  return 0;
               }

               if (v27 <= 2) {
                  v16 += 35;
               } else {
                  v16 += 15;
               }
            }

            if (data != null && !data.isEmpty()) {
               String v28 = data.getRawText().toLowerCase();
               String[] v29 = cleanTitle.split("\\s+");
               int v30 = 0;
               int v20 = 0;

               for (String v24 : v29) {
                  if (v24.length() >= 3) {
                     v20++;
                     if (v28.contains(v24.toLowerCase()) || v28.contains(transliterate(v24.toLowerCase()))) {
                        v30++;
                     }
                  }
               }

               if (v20 > 0 && v30 > 0) {
                  v16 += Math.min(30, v30 * (30) / v20);
               }
            }

            if (preferCyrillic) {
               if (data == null || !data.hasCyrillic()) {
                  return 0;
               }

               v16 += 35;
            } else if (v13) {
               v16 -= 40;
            }

            return Math.max(0, v16);
         }
      }
   }

   private static vq.xq m1621(String artist, String title, int duration, boolean preferCyrillic) {
      try {
         StringBuilder v4 = new StringBuilder("https://lrclib.net/api").append("/get?");
         v4.append("artist_name=").append(URLEncoder.encode(artist, StandardCharsets.UTF_8));
         v4.append("&track_name=").append(URLEncoder.encode(title, StandardCharsets.UTF_8));
         if (duration > 0) {

            v4.append("&duration=").append(duration);
         }

         HttpRequest v5 = HttpRequest.newBuilder()
            .uri(URI.create(v4.toString()))
            .header("User-Agent", "RoseSolution/1.0 (https://github.com/RoseSolution)")
            .timeout(Duration.ofSeconds(7857536299070196064L + -7857536299070196060L))
            .GET()
            .build();
         HttpResponse v6 = HTTP_CLIENT.send(v5, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (v6.statusCode() == 200) {
            vq.xq v7 = m1628((String)v6.body());
            if (v7 != null) {
               if (preferCyrillic) {
                  boolean result = v7.hasCyrillic();

                  if (!result) {
                     return null;
                  }
               }

               if (m1626((String)v6.body())) {
                  return null;
               }

               return v7;
            }
         }
      } catch (Throwable v10) {
      }

      return null;
   }

   private static String m1632(String artist) {
      if (artist == null) {
         return "";
      } else {
         String v1 = artist;
         int v2 = artist.indexOf(44);
         if (v2 != -1) {
            v1 = artist.substring(0, v2);
         }

         v2 = v1.indexOf(38);
         if (v2 != -1) {

            v1 = v1.substring(0, v2);
         }

         v2 = v1.indexOf(47);
         if (v2 != -1) {
            v1 = v1.substring(0, v2);
         }

         v2 = v1.toLowerCase().indexOf(" feat");
         if (v2 != -1) {
            v1 = v1.substring(0, v2);
         }

         v2 = v1.toLowerCase().indexOf(" ft");

         if (v2 != -1) {
            v1 = v1.substring(0, v2);
         }

         return v1.trim();
      }
   }

   private Vq() {
   }

   public static boolean isValidSynced(vq.xq data, int duration) {
      if (data == null || data.isEmpty() || data.isNotFound()) {
         return false;
      } else if (data.isInstrumental()) {
         return true;
      } else {
         List v2 = data.getSyncedLines();
         if (v2 != null && v2.size() >= 3) {
            if (duration > 0 && data.getTrackDuration() > 0.0F) {
               float v3 = Math.abs((float)duration - data.getTrackDuration());
               if (v3 > 4.0F) {
                  return false;
               }
            }

            float v9 = ((vq.wq)v2.get(0)).timeSeconds();
            float v4 = ((vq.wq)v2.get(v2.size() - 1)).timeSeconds();
            if (v4 - v9 < 3.0F) {
               return false;
            } else {
               if (duration > 0) {
                  if (v9 >= (float)duration || duration > 30 && v9 > (float)duration * 0.75F) {
                     return false;
                  }

                  if (v4 > (float)duration + 3.5F) {
                     return false;
                  }

                  if (duration > 45 && v4 < (float)duration * 0.4F) {
                     return false;
                  }
               }

               int v5 = 0;

               for (vq.wq v7 : v2) {
                  if (v7.text() != null && !v7.text().isBlank()) {
                     v5++;
                  }
               }

               return v5 >= 3;
            }
         } else {
            return false;
         }
      }
   }

   private static vq.xq m1635(String pageUrl) {
      try {
         Builder result = HttpRequest.newBuilder().uri(URI.create(pageUrl));

         HttpRequest v1 = result.header(
               "User-Agent",
               "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            .timeout(Duration.ofSeconds(1838295694729697780L + -1838295694729697774L))
            .GET()
            .build();
         HttpResponse v2 = HTTP_CLIENT.send(v1, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (v2.statusCode() != 200) {
            return null;
         } else {
            String v3 = (String)v2.body();
            List v4 = m1636(v3);
            if (v4.isEmpty()) {
               return null;
            } else {
               ArrayList v23 = new ArrayList;

               v23./* $VF: Unable to resugar constructor */<init>();
               ArrayList v5 = v23;

               for (String v7 : v4) {
                  v7 = m1637(v7);
                  v7 = v7.replaceAll("(?i)<ClientUtil\\s*/?>", "\n");
                  v7 = v7.replaceAll("(?i)</p>", "\n");
                  v7 = v7.replaceAll("<[^>]+>", "");
                  v7 = m1638(v7);

                  for (String v11 : v7.split("\\r?\\n")) {
                     String v12 = v11.trim();
                     if (!v12.isEmpty() && !v12.matches("(?i)^\\d+\\s+Contributors.*")) {
                        String v25 = "(?i)^Translations?.*";

                        if (!v12.matches(v25)
                           && !v12.matches("(?i).*Lyrics\\s*\\[.*?\\]$")
                           && !v12.matches("(?i)^\\[\\s*(?:текст песни|lyrics).*?\\]$")
                           && (!v12.matches("(?i).*Lyrics$") || v12.length() >= 60)) {
                           String v13 = m1630(v12);
                           if (!v13.isEmpty()) {
                              v5.add(v13);
                           }
                        }
                     }
                  }
               }

               return v5.isEmpty() ? null : new vq.xq(Collections.emptyList(), v5, false, false, String.join("\n", v5));
            }
         }
      } catch (Throwable v17) {
         return null;
      }
   }

   public static vq.xq getLyricsData(String artist, String title, float duration) {
      if (title != null && !title.isBlank() && !title.equals("Нет трека") && !title.equals("—")) {
         String v3 = artist != null ? artist.trim() : "";
         String v4 = title.trim();
         String result = v3.toLowerCase();
         String v7 = v4.toLowerCase();
         String v8 = result;
         String v5 = v8 + "|" + v7;
         vq.xq v6 = CACHE.get(v5);
         if (v6 == null) {

            getLyricsView(artist, title, 0.0F, duration);
            return null;
         } else {
            if (!v6.isEmpty()) {
               if (!v6.isNotFound()) {
                  return v6;
               }

               
            }

            return null;
         }
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record wq(float timeSeconds, String text) implements Comparable<vq.wq> {
      private final float f1282;

      @Override
      public final String toString() {

         String result = ObjectMethods.bootstrap<"toString",vq.wq,"timeSeconds;text",vq.wq::f1282,vq.wq::text>(this);

         return result;
      }

      public wq(float timeSeconds, String text) {
         this.f1282 = timeSeconds;
         this.text = text;
      }

      public 

         return Float.compare(this.f1282, o.f1282);
      }

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",vq.wq,"timeSeconds;text",vq.wq::f1282,vq.wq::text>(this, o);

         return result;
      }

      public float timeSeconds() {

         return this.f1282;
      }

      public String text() {

         return this.text;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",vq.wq,"timeSeconds;text",vq.wq::f1282,vq.wq::text>(this);

         return result;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static class xq {
      private final String trackName;
      private final String artistName;
      public static final vq.xq NOT_FOUND = new vq.xq(Collections.emptyList(), Collections.emptyList(), false, true, "", 0.0F, "", "");
      private final boolean f1285;
      private final float f1287;
      private final List<vq.wq> f1283;
      private final List<String> f1284;
      private final String f1286;
      private final boolean instrumental;

      public xq(
         List<vq.wq> syncedLines,
         List<String> plainLines,
         boolean instrumental,
         boolean isNotFound,
         String rawText,
         float trackDuration,
         String trackName,
         String artistName
      ) {
         this.f1283 = syncedLines != null ? syncedLines : Collections.emptyList();
         this.f1284 = plainLines != null ? plainLines : Collections.emptyList();
         this.instrumental = instrumental;
         this.f1285 = isNotFound;
         this.f1286 = rawText != null ? rawText : "";
         this.f1287 = trackDuration;
         this.trackName = trackName != null ? trackName : "";
         this.artistName = artistName != null ? artistName : "";
      }

      public String getTrackName() {

         return this.trackName;
      }

      public String getArtistName() {

         return this.artistName;
      }

      public boolean isEmpty() {
         boolean result;
         if (this.f1283.isEmpty() && this.f1284.isEmpty() && !this.instrumental) {
            result = true;
         } else {
            result = false;
            
         }

         return result;
      }

      public boolean isSynced() {
         boolean result;
         if (!this.f1283.isEmpty()) {
            result = true;
            int v1 = -5484819;
            int temp1 = v1 * (v1 + 1);

            
         } else {
            result = false;
         }

         return result;
      }

      public List<String> getPlainLines() {

         return this.f1284;
      }

      public boolean isNotFound() {

         return this.f1285;
      }

      public vq.yq getView(float rawPosition, float duration) {
         if (this.instrumental) {
            return new vq.yq("", "", "♫ [Инструментал]", "", true, false, false, false, true, 0.0F);
         } else {
            float v3 = rawPosition + 0.2F;
            if (this.f1283.isEmpty()) {
               if (!this.f1284.isEmpty()) {
                  float v20 = duration > 0.0F ? duration : 180.0F;
                  float v22 = Math.max(0.0F, Math.min(1.0F, v3 / v20));
                  int v23 = (int)Math.min((float)(this.f1284.size() - 1), Math.max(0.0F, v22 * (float)this.f1284.size()));
                  String v24 = this.f1284.get(v23).trim();
                  String v25 = v23 - 1 >= 0 ? this.f1284.get(v23 - 1).trim() : "";

                  String v26 = v23 - 2 >= 0 ? this.f1284.get(v23 - 2).trim() : "";
                  String v27 = v23 + 1 < this.f1284.size() ? this.f1284.get(v23 + 1).trim() : "";
                  boolean v28 = false;
                  float v29 = 0.0F;
                  return new vq.yq(v26, v25, v24, v27, false, false, false, v28, false, v29);
               } else {
                  vq.yq result = new vq.yq;

                  result./* $VF: Unable to resugar constructor */<init>("", "", "", "", false, false, true, false, false, 0.0F);
                  return result;
               }
            } else if (v3 < this.f1283.get(0).timeSeconds()) {

               String v19 = this.f1283.get(0).text().trim();
               return new vq.yq("", "", "· · ·", v19, false, false, false, false, true, 0.0F);
            } else {
               int v4 = 0;
               int v5 = 0;

               while (v5 < this.f1283.size() && this.f1283.get(v5).timeSeconds() <= v3) {
                  v4 = v5++;
               }

               vq.wq v21 = this.f1283.get(v4);
               float v6 = v21.timeSeconds();
               float v7 = v4 + 1 < this.f1283.size() ? this.f1283.get(v4 + 1).timeSeconds() : (duration > 0.0F ? duration : v6 + 8.0F);
               String v8 = v21.text().trim();
               String v9 = v4 - 1 >= 0 ? this.f1283.get(v4 - 1).text().trim() : "";
               String v10 = v4 - 2 >= 0 ? this.f1283.get(v4 - 2).text().trim() : "";
               String v11 = v4 + 1 < this.f1283.size() ? this.f1283.get(v4 + 1).text().trim() : "";
               float v12 = v7 - v6;
               float v13;
               if (v12 > 7.5F) {
                  float v14 = Math.max(2.8F, (float)v8.length() * 0.12F);
                  v13 = Math.min(v12, v14);
               } else {
                  v13 = Math.max(0.1F, v12);
               }

               float v30 = Math.max(0.0F, Math.min(1.0F, (v3 - v6) / v13));
               boolean v15 = !v8.isEmpty() && !v8.startsWith("♫");
               return new vq.yq(v10, v9, v8, v11, false, false, false, v15, true, v30);
            }
         }
      }

      public List<vq.wq> getSyncedLines() {

         return this.f1283;
      }

      public float getTrackDuration() {
         return this.f1287;
      }

      public String getRawText() {

         return this.f1286;
      }

      public boolean isInstrumental() {

         return this.instrumental;
      }

      public boolean hasCyrillic() {

         boolean result = vq.CYRILLIC_PATTERN.matcher(this.f1286).find();

         return result;
      }

      public xq(List<vq.wq> syncedLines, List<String> plainLines, boolean instrumental, boolean isNotFound, String rawText) {
         this(syncedLines, plainLines, instrumental, isNotFound, rawText, 0.0F, "", "");
      }

      public boolean matchesTrack(String artist, String title) {
         if (this.instrumental) {
            return true;
         } else if (this.isEmpty()) {
            return false;
         } else {
            boolean v3 = vq.CYRILLIC_PATTERN.matcher(title != null ? title : "").find() || vq.CYRILLIC_PATTERN.matcher(artist != null ? artist : "").find();
            if (v3 && !this.hasCyrillic()) {
               return false;
            } else {
               if (!this.f1283.isEmpty()) {
                  float v4 = this.f1283.get(0).timeSeconds();
                  vq.wq result = this.f1283.get(this.f1283.size() - 1);

                  float v5 = result.timeSeconds();
                  if (v5 <= v4) {
                     return false;
                  }
               }

               return true;
            }
         }
      }

      public int getActiveIndex(float rawPosition) {
         float v2 = rawPosition + 0.2F;
         if (this.f1283.isEmpty()) {
            return !this.f1284.isEmpty() ? 0 : -1;
         } else {
            int v3 = 0;
            int v4 = 0;

            while (v4 < this.f1283.size() && this.f1283.get(v4).timeSeconds() <= v2) {
               v3 = v4++;
            }

            return v3;
         }
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static record yq(
      String prevLine2,
      String prevLine1,
      String currentLine,
      String nextLine1,
      boolean isInstrumental,
      boolean isLoading,
      boolean isNotFound,
      boolean isSinging,
      boolean isSynced,
      float lineProgress
   ) {
      private final String f1289;
      private final boolean f1293;
      private final boolean f1294;
      private final float f1297;
      private final boolean f1295;
      private final String f1288;
      private final boolean f1296;
      private final boolean f1292;
      private final String f1290;
      private final String f1291;

      public boolean isLoading() {
         int v1 = 265420586;

         return this.f1293;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",vq.yq,"prevLine2;prevLine1;currentLine;nextLine1;isInstrumental;isLoading;isNotFound;isSinging;isSynced;lineProgress",vq.yq::f1288,vq.yq::f1289,vq.yq::f1290,vq.yq::f1291,vq.yq::f1292,vq.yq::f1293,vq.yq::f1294,vq.yq::f1295,vq.yq::f1296,vq.yq::f1297>(
            this, o
         );
      }

      public String prevLine2() {
         return this.f1288;
      }

      public boolean isNotFound() {

         return this.f1294;
      }

      public yq(
         String prevLine2,
         String prevLine1,
         String currentLine,
         String nextLine1,
         boolean isInstrumental,
         boolean isLoading,
         boolean isNotFound,
         boolean isSinging,
         boolean isSynced,
         float lineProgress
      ) {
         this.f1288 = prevLine2;
         this.f1289 = prevLine1;
         this.f1290 = currentLine;
         this.f1291 = nextLine1;
         this.f1292 = isInstrumental;
         this.f1293 = isLoading;
         this.f1294 = isNotFound;
         this.f1295 = isSinging;
         this.f1296 = isSynced;
         this.f1297 = lineProgress;
      }

      public String prevLine1() {
         return this.f1289;
      }

      public boolean isSynced() {

         return this.f1296;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",vq.yq,"prevLine2;prevLine1;currentLine;nextLine1;isInstrumental;isLoading;isNotFound;isSinging;isSynced;lineProgress",vq.yq::f1288,vq.yq::f1289,vq.yq::f1290,vq.yq::f1291,vq.yq::f1292,vq.yq::f1293,vq.yq::f1294,vq.yq::f1295,vq.yq::f1296,vq.yq::f1297>(
            this
         );

         return result;
      }

      public float lineProgress() {
         int v1 = 734945495;

         return this.f1297;
      }

      public boolean isInstrumental() {
         int v1 = 1827326662;
         int temp2 = v1 + 1;

         return this.f1292;
      }

      public boolean isSinging() {
         int v1 = 1203889003;

         return this.f1295;
      }

      @Override
      public final String toString() {

         int v1 = -210854040;

         return ObjectMethods.bootstrap<"toString",vq.yq,"prevLine2;prevLine1;currentLine;nextLine1;isInstrumental;isLoading;isNotFound;isSinging;isSynced;lineProgress",vq.yq::f1288,vq.yq::f1289,vq.yq::f1290,vq.yq::f1291,vq.yq::f1292,vq.yq::f1293,vq.yq::f1294,vq.yq::f1295,vq.yq::f1296,vq.yq::f1297>(
            this
         );
      }

      public String currentLine() {
         return this.f1290;
      }

      public String nextLine1() {
         int v1 = -1227932053;
         int temp2 = v1 + 1;

         return this.f1291;
      }

      
   }
}
