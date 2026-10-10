package ru.white.utils.misc;

import com.google.common.collect.Maps;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class Rq implements IMinecraft {
   private static final Map<String, Integer> dynamicIdCounters = Maps.newHashMap();

   public static Identifier registerDynamicTexture(String prefix, NativeImageBackedTexture texture) {
      Integer v2 = dynamicIdCounters.get(prefix);
      if (v2 == null) {
         v2 = 1;
      } else {
         v2 = v2 + 1;
      }

      dynamicIdCounters.put(prefix, v2);
      Locale result = Locale.ROOT;
      String temp1 = "dynamic/%s_%d";
      Object[] temp2 = new Object[2];

      temp2[0] = prefix;
      int v5 = -1005099462;

      temp2[1] = v2;
      Identifier v3 = Identifier.method_60656(String.format(result, temp1, temp2));
      mc.method_1531().method_4616(v3, texture);
      return v3;
   }

   public static void registerTexture(Identifier i, byte[] content) {
      try {
         ByteBuffer v2 = BufferUtils.createByteBuffer(content.length).put(content);
         v2.flip();
         NativeImageBackedTexture result = new NativeImageBackedTexture;

         result./* $VF: Unable to resugar constructor */<init>(() -> i.toString(), NativeImage.method_4324(v2));
         NativeImageBackedTexture v3 = result;
         mc.execute(() -> {
            TextureManager var10000x = mc.method_1531();

            var10000x.method_4616(i, v3);
            
         });
      } catch (Exception v6) {
      }
   }

   public static void setWindowIcon(InputStream img16x16, InputStream img32x32) {
      try {
         MemoryStack v2 = MemoryStack.stackPush();

         try {
            Buffer v3 = GLFWImage.malloc(2, v2);
            List v4 = List.of(img16x16, img32x32);
            ArrayList v5 = new ArrayList();

            for (int v6 = 0; v6 < v4.size(); v6++) {
               Object result = v4.get(v6);

               NativeImage v7 = NativeImage.method_4309((InputStream)result);
               ByteBuffer v8 = MemoryUtil.memAlloc(v7.method_4307() * v7.method_4323() * 4);
               v8.asIntBuffer().put(v7.method_48463());
               v3.position(v6);
               v3.width(v7.method_4307());
               int v14 = v7.method_4323();

               v3.height(v14);
               v3.pixels(v8);
               v5.add(v8);
            }

            if (GLFW.glfwGetPlatform() != 393219) {
               GLFW.glfwSetWindowIcon(MinecraftClient.method_1551().method_22683().method_4490(), v3);
            }

            v5.forEach(MemoryUtil::memFree);
         } catch (Throwable v12) {
            if (v2 != null) {
               try {
                  v2.close();
               } catch (Throwable v11) {
                  v12.addSuppressed(v11);
               }
            }

            throw v12;
         }

         if (v2 != null) {
            v2.close();
         }
      } catch (Exception v13) {
      }
   }

   @Generated
   private Rq() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void registerBufferedImageTexture(Identifier i, BufferedImage bi) {
      try {
         ByteArrayOutputStream v2 = new ByteArrayOutputStream();
         ImageIO.write(bi, "png", v2);
         byte[] v3 = v2.toByteArray();
         int v4 = -1066611882;

         registerTexture(i, v3);
      } catch (Exception v7) {
      }
   }

   public static NativeImage parseHead(NativeImage image) {
      if (image == null) {
         return null;
      } else {
         int v1 = 22;
         int v2 = 22;
         int v3 = image.method_4307();
         int v4 = image.method_4323();

         for (int v5 = image.method_4323(); v1 < v3 || v2 < v5; v2 *= 2) {
            v1 *= 2;
         }

         NativeImage v9 = new NativeImage(v1, v2, true);

         for (int v6 = 0; v6 < v3; v6++) {
            for (int v7 = 0; v7 < v4; v7++) {

               v9.method_61941(v6, v7, image.method_61940(v6, v7));
            }
         }

         image.close();
         return v9;
      }
   }

   public static NativeImageBackedTexture getHeadFromURL(String url) throws IOException {
      if (url != null && !url.isEmpty()) {
         NativeImageBackedTexture result = new NativeImageBackedTexture;
         int v1 = -2118717125;
         int temp1 = v1 * (v1 + 1);

         temp1 %= 2;

         if (temp1 != 0) {
            }

         result./* $VF: Unable to resugar constructor */<init>(() -> {

            return "discord_avatar";
         }, parseHead(NativeImage.method_4309(new URL(url).openStream())));
         return result;
      } else {
         return null;
      }
   }

   
}
