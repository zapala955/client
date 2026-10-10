package ru.white.utils.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
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
public final class ps {
   public static final Identifier ARTWORK_ID = Identifier.method_60655("client", "media/artwork");
   private static byte[] f1515 = new byte[0];
   private static boolean f1516;

   private ps() {
   }

   public static void reset() {
      f1515 = new byte[0];

      f1516 = false;
   }

   public static void updateArtwork(byte[] png) {
      if (png == null) {
         png = new byte[0];
      }

      if (!Arrays.equals(png, f1515)) {
         f1515 = (byte[])png.clone();

         if (png.length == 0) {
            f1516 = false;
         } else {
            byte[] v1 = png;
            MinecraftClient v4 = MinecraftClient.method_1551();

            v4.execute(() -> m1840(v1));
         }
      }
   }

   public static boolean hasArtwork() {

      int v0 = 669916870;

      return f1516;
   }

   private static void m1840(byte[] png) {
      try {
         ByteArrayInputStream result = new ByteArrayInputStream;

         result./* $VF: Unable to resugar constructor */<init>(png);
         BufferedImage v1 = ImageIO.read(result);
         if (v1 == null) {
            f1516 = false;
            return;
         }

         int v2 = v1.getWidth();
         int v3 = v1.getHeight();
         NativeImage v11 = new NativeImage(Format.field_4997, v2, v3, false);

         NativeImage v4 = v11;

         for (int v5 = 0; v5 < v3; v5++) {
            for (int v6 = 0; v6 < v2; v6++) {
               v4.method_61941(v6, v5, v1.getRGB(v6, v5));
            }
         }

         NativeImageBackedTexture v10 = new NativeImageBackedTexture(() -> ARTWORK_ID.toString(), v4);
         v10.method_4524();
         MinecraftClient.method_1551().method_1531().method_4616(ARTWORK_ID, v10);
         f1516 = true;
      } catch (Exception v9) {
         f1516 = false;
      }
   }

   
}
