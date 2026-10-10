package ru.white.utils.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class tt {
   private final String url;
   private volatile boolean f1616;
   private volatile boolean loading = false;
   private final Identifier id;
   private int height;
   private volatile boolean f1617;
   private static final Map<String, tt> CACHE = new ConcurrentHashMap<>();
   private int width;

   public boolean isFailed() {
      return this.f1617;
   }

   public static tt get(String url) {
      Object result = CACHE.computeIfAbsent(url, tt::new);

      return (tt)result;
   }

   public boolean isLoaded() {
      return this.f1616;
   }

   public static Identifier getIdentifier(String url) {

      tt v1 = get(url);
      v1.loadAsync();
      return v1.isLoaded() ? v1.id : null;
   }

   public void loadAsync() {
      if (!this.f1616 && !this.loading && !this.f1617) {
         this.loading = true;
         Thread result = new Thread;
         Runnable temp2 = () -> {
            try {
               if (m1940(this.url)) {
                  URL v1x = URI.create(this.url).toURL();
                  HttpURLConnection v2x = (HttpURLConnection)v1x.openConnection();
                  v2x.setConnectTimeout(5000);
                  v2x.setReadTimeout(8000);
                  v2x.setRequestProperty("User-Agent", "WhiteClient/1.0");

                  try {
                     InputStream v3x = v2x.getInputStream();

                     label189: {
                        try {
                           BufferedImage v4x = ImageIO.read(v3x);
                           if (v4x != null) {
                              int v5 = v4x.getWidth();
                              int v6 = v4x.getHeight();
                              int[] v7 = v4x.getRGB(0, 0, v5, v6, null, 0, v5);
                              MinecraftClient.method_1551().execute(() -> {

                                 this.m1939(v5, v6, v7);
                              });
                              break label189;
                           }

                           this.f1617 = true;
                        } catch (Throwable v22) {
                           if (v3x != null) {
                              try {
                                 v3x.close();
                              } catch (Throwable v21) {
                                 v22.addSuppressed(v21);
                              }
                           }

                           throw v22;
                        }

                        if (v3x != null) {
                           v3x.close();
                        }

                        return;
                     }

                     if (v3x != null) {
                        v3x.close();
                     }

                     return;
                  } finally {
                     v2x.disconnect();
                  }
               }

               this.f1617 = true;
            } catch (Exception v24) {
               this.f1617 = true;
               return;
            } finally {
               this.loading = false;
            }
         };

         String temp3 = "url-image-loader";

         result./* $VF: Unable to resugar constructor */<init>(temp2, temp3);
         Thread v1 = result;
         v1.setDaemon(true);
         v1.start();
      }
   }

   public 

      return this.width;
   }

   public static void clearCache() {

      Collection result = CACHE.values();

      result.forEach(tt::destroy);
      CACHE.clear();
   }

   public int getHeight() {
      return this.height;
   }

   private tt(String url) {
      this.f1616 = false;
      this.f1617 = false;
      this.width = 0;
      this.height = 0;
      this.url = url;
      String v2 = Integer.toHexString(url.hashCode());
      this.id = Identifier.method_60655("white", "url_images/" + v2);
   }

   public void destroy() {
      MinecraftClient.method_1551().execute(() -> {
         MinecraftClient result = MinecraftClient.method_1551();

         result.method_1531().method_4615(this.id);
      });

      this.f1616 = false;
      this.f1617 = false;
   }

   public Identifier getId() {
      this.loadAsync();
      return this.f1616 ? this.id : null;
   }

   private void m1939(int uploadWidth, int uploadHeight, int[] pixels) {
      if (!this.f1616) {
         NativeImage result = new NativeImage(Format.field_4997, uploadWidth, uploadHeight, false);

         NativeImage v4 = result;

         for (int v5 = 0; v5 < uploadHeight; v5++) {
            for (int v6 = 0; v6 < uploadWidth; v6++) {
               v4.method_61941(v6, v5, pixels[v5 * uploadWidth + v6]);
            }
         }

         NativeImageBackedTexture v9 = new NativeImageBackedTexture(() -> this.id.toString(), v4);
         v9.method_4524();
         MinecraftClient.method_1551().method_1531().method_4616(this.id, v9);
         this.width = uploadWidth;
         this.height = uploadHeight;

         this.f1616 = true;
         this.f1617 = false;
      }
   }

   private static boolean m1940(String url) {
      String v1 = url.toLowerCase(Locale.ROOT);
      int v2 = v1.indexOf(63);
      if (v2 >= 0) {
         v1 = v1.substring(0, v2);
      }

      boolean result;
      if (!v1.endsWith(".png") && !v1.endsWith(".jpg") && !v1.endsWith(".jpeg")) {
         result = false;
      } else {
         result = true;
         
      }

      return result;
   }

   
}
