package ru.white.utils;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class ClientUtil {
   private static boolean f1300 = false;
   private static final CopyOnWriteArrayList<Clip> CLIPS_LIST = new CopyOnWriteArrayList<>();

   public static void playSound_wav(String location, float volume) {
      int v2 = 1610276621;

      playSound_wav(location, volume, 1.0F);
   }

   public static synchronized void playWelcomeSound() {
      if (!f1300) {
         f1300 = true;

         playMp3("welcome", 0.2F);
      }
   }

   public static void playMp3(String soundName, float volume) {
      int v2 = -662864318;
      int temp2 = v2 * (v2 + 1) % 2;

      if (temp2 != 0) {
         }

      CompletableFuture.runAsync(
         () -> {
            try {
               File v2x = null;
               File v3x = new File("Дабро пожаловееет.mp3");
               if (v3x.exists()) {
                  v2x = v3x;
               } else {
                  File v4x = new File(soundName.endsWith(".mp3") ? soundName : soundName + ".mp3");
                  if (v4x.exists()) {
                     v2x = v4x;
                  }
               }

               if (v2x == null || !v2x.exists()) {
                  String v9 = soundName.endsWith(".mp3") ? soundName : soundName + ".mp3";
                  String v17 = "/assets/client/sound/" + v9;
                  InputStream v5 = br.class.getResourceAsStream(v17);
                  if (v5 != null) {
                     Path v6 = Path.of("rose/client1_21_11/cache_" + soundName + ".mp3");
                     if (v6.getParent() != null && !Files.exists(v6.getParent())) {
                        Files.createDirectories(v6.getParent());
                     }

                     Files.copy(v5, v6, StandardCopyOption.REPLACE_EXISTING);
                     v2x = v6.toFile();
                  }
               }

               if (v2x != null && v2x.exists()) {
                  if (Platform.isWindows() && br.cr.INSTANCE != null) {
                     String v18 = v2x.getAbsolutePath().replace((char)(92), (char)(47));
                     int v19 = Math.max(0, Math.min(1000, Math.round(volume * 1000.0F)));
                     int v13 = 948433639;

                     br.cr.INSTANCE.mciSendStringA("close welcome_alias", null, 0, 0);
                     br.cr.INSTANCE
                        .mciSendStringA("open \"" + v18 + "\" type mpegvideo alias welcome_alias", null, 0, 0);
                     br.cr.INSTANCE.mciSendStringA("setaudio welcome_alias volume to " + v19, null, 0, 0);
                     br.cr.INSTANCE.mciSendStringA("play welcome_alias", null, 0, 0);
                  } else if (Platform.isMac()) {
                     new ProcessBuilder("afplay", "-v", String.valueOf(volume), v2x.getAbsolutePath()).start();
                  }
               }
            } catch (Throwable v16) {
               v16.printStackTrace();
               
            }
         }
      );
   }

   private static void m1650() {

      CLIPS_LIST.removeIf(clip -> {
         boolean result;
         if (!clip.isOpen()) {
            result = true;
            
         } else {
            result = false;
         }

         return result;
      });
      int v0 = 1678684993;
      int v3 = v0 * (v0 + 1) % 2;

      if (v3 != 0) {
         }
   }

   public static void playSound_wav(String location, float volume, float pitch) {
      CompletableFuture.runAsync(() -> {
         try {
            m1650();
            StringBuilder var10000x = new StringBuilder();

            String v3x = var10000x.append("/assets/client/sound/").append((Object)location).append(".wav").toString();
            InputStream v4x = br.class.getResourceAsStream(v3x);
            if (v4x == null) {
               return;
            }

            AudioInputStream v5 = AudioSystem.getAudioInputStream(new BufferedInputStream(v4x));

            try {
               Clip v6 = AudioSystem.getClip();
               if (Math.abs(pitch - 1.0F) < 0.001F) {
                  v6.open(v5);
               } else {
                  m1649(v6, v5, pitch);
               }

               if (v6.isControlSupported(Type.MASTER_GAIN)) {
                  FloatControl v7 = (FloatControl)v6.getControl(Type.MASTER_GAIN);
                  float v8 = Math.max(0.0F, Math.min(1.0F, volume));
                  float v9 = (float)(Math.log10(v8 <= 0.0F ? 1.0E-4 : (double)v8) * 20.0);
                  v7.setValue(v9);
               }

               v6.addLineListener(event -> {
                  javax.sound.sampled.LineEvent.Type var10000xx = event.getType();

                  if (var10000xx == javax.sound.sampled.LineEvent.Type.STOP) {
                     v6.close();
                     CLIPS_LIST.remove(v6);
                     int v2 = 1496815595;
                     int v5x = v2 * (v2 + 1);

                     
                  }
               });
               CLIPS_LIST.add(v6);
               v6.start();
            } catch (Throwable v14) {
               if (v5 != null) {
                  try {
                     v5.close();
                  } catch (Throwable v13) {
                     v14.addSuppressed(v13);
                  }
               }

               throw v14;
            }

            if (v5 != null) {
               v5.close();
            }
         } catch (Exception v15) {
            v15.printStackTrace();
         }
      });
      int v3 = -1832289951;

      
   }

   private static void m1649(Clip clip, AudioInputStream stream, float pitch) throws Exception {
      float v3 = Math.max(0.5F, Math.min(2.0F, pitch));
      AudioInputStream v4 = stream;
      AudioFormat v5 = stream.getFormat();

      if (v5.getEncoding() != Encoding.PCM_SIGNED) {
         v4 = AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, stream);
         v5 = v4.getFormat();
      }

      byte[] v6 = v4.readAllBytes();
      if (v4 != stream) {
         v4.close();
      }

      int v7 = v5.getFrameSize();
      if (v7 <= 0) {
         clip.open(v5, v6, 0, v6.length);
      } else {
         int v8 = v6.length / v7;
         int v9 = (int)((float)v8 / v3);
         if (v9 <= 0) {
            clip.open(v5, v6, 0, v6.length);
         } else {
            byte[] v10 = new byte[v9 * v7];

            for (int v11 = 0; v11 < v9; v11++) {
               int v12 = Math.min(v8 - 1, (int)((float)v11 * v3));
               System.arraycopy(v6, v12 * v7, v10, v11 * v7, v7);
            }

            clip.open(v5, v10, 0, v10.length);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public interface cr extends StdCallLibrary {
      br.cr INSTANCE = Platform.isWindows() ? (br.cr)Native.load("winmm", br.cr.class) : null;

      int mciSendStringA(String v1, byte[] v2, int v3, int v4);
   }
}
