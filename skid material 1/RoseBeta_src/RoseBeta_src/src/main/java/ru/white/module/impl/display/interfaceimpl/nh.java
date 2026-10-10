package ru.white.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public enum nh {
   SOUNDCLOUD(
      "SoundCloud",
      Identifier.method_60655("client", "textures/music/soundcloud.png"),
      new Color(255, 85, 0)
   ),
   OTHER("Музыка", null, new Color(255, 125, 165)),
   YANDEX_MUSIC(
      "Yandex Music",
      Identifier.method_60655("client", "textures/music/yandex.png"),
      new Color(255, 204, 0)
   ),
   TELEGRAM(
      "Telegram",
      Identifier.method_60655("client", "textures/music/telegram.png"),
      new Color(42, 171, 238)
   ),
   SPOTIFY(
      "Spotify",
      Identifier.method_60655("client", "textures/music/spotify.png"),
      new Color(30, 215, 96)
   );

   private final Identifier f505;
   // $VF: synthetic field
   private static final nh[] $VALUES = m494();
   private static Boolean f507 = null;
   private final String displayName;
   private final Color f506;

   public 

      int v2 = (int)Math.max(0.0F, Math.min(255.0F, 32.0F * alpha));
      return up.getColor(this.f506.getRed(), this.f506.getGreen(), this.f506.getBlue(), v2);
   }

   public String getDisplayName() {

      return this.displayName;
   }

   public int getBorderColor(float alpha) {

      int v2 = (int)Math.max(0.0F, Math.min(255.0F, 75.0F * alpha));
      return up.getColor(this.f506.getRed(), this.f506.getGreen(), this.f506.getBlue(), v2);
   }

   public int getColor(float alpha) {
      int v2 = (int)Math.max(0.0F, Math.min(255.0F, (float)this.f506.getAlpha() * alpha));

      return up.getColor(this.f506.getRed(), this.f506.getGreen(), this.f506.getBlue(), v2);
   }

   private nh(String displayName, Identifier icon, Color brandColor) {
      this.displayName = displayName;
      this.f505 = icon;
      this.f506 = brandColor;
   }

   private static boolean m493() {
      if (f507 == null) {
         try {
            String v0 = System.getProperty("user.home", "");
            Path v1 = Paths.get(
               v0,
               "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Brave Apps/SoundCloud.lnk"
            );
            String[] temp1 = new String[1];
            String temp4 = "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Chrome Apps/SoundCloud.lnk";

            temp1[0] = temp4;
            Path v2 = Paths.get(v0, temp1);
            temp1 = new String[1];

            temp1[0] = "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Edge Apps/SoundCloud.lnk";
            Path v3 = Paths.get(v0, temp1);
            f507 = Files.exists(v1) || Files.exists(v2) || Files.exists(v3);
         } catch (Throwable v7) {
            f507 = false;
         }
      }

      return f507;
   }

   public Identifier getIcon() {
      return this.f505;
   }

   public Color getBrandColor() {

      int temp2 = v2 + 1;

      return this.f506;
   }

   public static nh detect(IMediaSession session, MediaInfo info) {
      String v2 = session != null && session.getOwner() != null ? session.getOwner().toLowerCase().trim() : "";
      String v3 = info != null && info.getTitle() != null ? info.getTitle().toLowerCase().trim() : "";
      String v4 = info != null && info.getArtist() != null ? info.getArtist().toLowerCase().trim() : "";
      if (!v2.contains("spotify") && !v3.contains("spotify") && !v4.contains("spotify")) {
         if (!v2.contains("yandex")
            && !v2.contains("яндекс")
            && !v3.contains("yandex music")
            && !v3.contains("яндекс музыка")
            && !v3.contains("яндекс.музыка")) {

            if (!v3.contains("music.yandex")
               && !v4.contains("yandex music")
               && !v4.contains("яндекс музыка")
               && !v4.contains("yandex")
               && !v4.contains("яндекс")) {
               if (!v2.contains("telegram") && !v3.contains("telegram") && !v4.contains("telegram")) {
                  if (!v2.contains("soundcloud")
                     && !v3.contains("soundcloud")
                     && !v3.contains("on soundcloud")
                     && !v4.contains("soundcloud")
                     && !v2.contains("ypvlskddcnazfc23nngp4pv5c4")
                     && !v2.contains("eikjhbkpemdappjfcmdeeeamdpkgabmk")) {
                     boolean v5 = v2.contains("brave")
                        || v2.contains("chrome")
                        || v2.contains("msedge")
                        || v2.contains("edge")
                        || v2.contains("firefox")
                        || v2.contains("opera")
                        || v2.contains("vivaldi")
                        || v2.contains("browser");
                     if (v5) {
                        boolean v6 = v3.contains("youtube")
                           || v4.contains("youtube")
                           || v3.contains("youtu.be")
                           || v3.contains("vk.com")
                           || v3.contains("вконтакте")
                           || v4.contains("вконтакте");
                        if (!v6 && (m493() || v2.contains("brave") || v2.contains("ypvl") || !v3.isEmpty() || !v4.isEmpty())) {
                           return SOUNDCLOUD;
                        }
                     }

                     return OTHER;
                  }

                  return SOUNDCLOUD;
               }

               return TELEGRAM;
            }
         }

         return YANDEX_MUSIC;
      } else {
         return SPOTIFY;
      }
   }
}
