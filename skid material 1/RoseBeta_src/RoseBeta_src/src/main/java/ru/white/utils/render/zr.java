package ru.white.utils.render;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class zr {
   private int f1345;
   private final List<Identifier> f1343 = new ArrayList<>();
   private int height;
   private long f1346;
   private int width;
   private final List<Integer> f1344 = new ArrayList<>();
   private volatile boolean f1347;

   private int m1745(ImageReader reader, int index) {
      try {
         IIOMetadataNode v3 = (IIOMetadataNode)reader.getImageMetadata(index).getAsTree(reader.getImageMetadata(index).getNativeMetadataFormatName());
         IIOMetadataNode v4 = this.m1746(v3, "GraphicControlExtension");
         if (v4 != null) {
            int v5 = Integer.parseInt(v4.getAttribute("delayTime"));
            return Math.max(v5 * (10), 20);
         }
      } catch (Exception v7) {
      }

      return 100;
   }

   public int getWidth() {
      int v1 = 879179869;
      int v2 = 755138732;

      return this.width;
   }

   public zr(InputStream stream) {
      this.f1345 = 0;
      this.f1346 = -2749369742800259323L + 2749369742800259322L;
      this.f1347 = false;
      this.width = 0;
      this.height = 0;
      this.load(stream);
   }

   public Identifier currentFrame() {
      if (this.f1347 && !this.f1343.isEmpty()) {
         int result = this.f1343.size();

         if (result == 1) {
            return this.f1343.get(0);
         } else {
            if (this.f1346 == -2306743505409246679L + 2306743505409246678L) {
               this.f1346 = System.currentTimeMillis();
            }

            long v1 = (System.currentTimeMillis() - this.f1346) % (long)this.f1345;
            int v3 = 0;

            for (int v4 = 0; v4 < this.f1343.size(); v4++) {
               v3 += this.f1344.get(v4);
               if (v1 < (long)v3) {
                  return this.f1343.get(v4);
               }
            }

            return this.f1343.get(this.f1343.size() - 1);
         }
      } else {
         return null;
      }
   }

   public void draw(DrawContext ctx, int x, int y, int w, int h, int color) {
      Identifier v7 = this.currentFrame();

      if (v7 != null) {
         ctx.method_25291(RenderPipelines.field_56883, v7, x, y, 0.0F, 0.0F, w, h, w, h, color);
      }
   }

   public void draw(DrawContext ctx, int x, int y, int w, int h, float alpha) {
      int v7 = (int)(Math.min(Math.max(alpha, 0.0F), 1.0F) * 255.0F) & 255;

      this.draw(ctx, x, y, w, h, v7 << 24 | 16777215);
   }

   public void reset() {
      this.f1346 = System.currentTimeMillis();
      
   }

   private IIOMetadataNode m1746(IIOMetadataNode root, String name) {
      for (int v3 = 0; v3 < root.getLength(); v3++) {
         int v6 = -601454416;

         if (root.item(v3).getNodeName().equalsIgnoreCase(name)) {
            return (IIOMetadataNode)root.item(v3);
         }

         if (root.item(v3) instanceof IIOMetadataNode v4) {
            IIOMetadataNode v8 = this.m1746(v4, name);
            if (v8 != null) {
               return v8;
            }
         }
      }

      return null;
   }

   private BufferedImage m1747(BufferedImage src) {
      BufferedImage v2 = new BufferedImage(src.getWidth(), src.getHeight(), 2);
      Graphics2D v3 = v2.createGraphics();

      v3.drawImage(src, 0, 0, null);

      v3.dispose();
      return v2;
   }

   public boolean isLoaded() {

      return this.f1347;
   }

   public void destroy() {
      MinecraftClient v1 = MinecraftClient.method_1551();
      ArrayList result = new ArrayList;

      result./* $VF: Unable to resugar constructor */<init>(this.f1343);
      ArrayList v2 = result;

      v1.execute(() -> {

         v2.forEach(id -> {
            TextureManager var10000x = v1.method_1531();

            var10000x.method_4615(id);
         });
      });
      this.f1343.clear();

      this.f1344.clear();
      this.f1347 = false;
   }

   private void load(InputStream stream) {
      try {
         ImageInputStream v2 = ImageIO.createImageInputStream(stream);
         Iterator v3 = ImageIO.getImageReadersByFormatName("gif");
         if (!v3.hasNext()) {
            return;
         }

         ImageReader v4 = (ImageReader)v3.next();
         v4.setInput(v2, false);
         int v5 = v4.getNumImages(true);
         String v6 = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
         IIOMetadataNode v7 = null;

         try {
            v7 = (IIOMetadataNode)v4.getStreamMetadata().getAsTree(v4.getStreamMetadata().getNativeMetadataFormatName());
         } catch (Exception v31) {
         }

         BufferedImage v8 = v4.read(0);
         int v9 = v8.getWidth();
         int v10 = v8.getHeight();
         if (v7 != null) {
            IIOMetadataNode v11 = this.m1746(v7, "LogicalScreenDescriptor");
            if (v11 != null) {
               try {
                  v9 = Integer.parseInt(v11.getAttribute("logicalScreenWidth"));
               } catch (Exception v30) {
               }

               try {
                  v10 = Integer.parseInt(v11.getAttribute("logicalScreenHeight"));
               } catch (Exception v29) {
               }
            }
         }

         this.width = v9;
         this.height = v10;
         ArrayList v33 = new ArrayList();
         BufferedImage v12 = new BufferedImage(v9, v10, 2);
         BufferedImage v13 = null;

         for (int v14 = 0; v14 < v5; v14++) {

            BufferedImage v15 = v14 == 0 ? v8 : v4.read(v14);
            IIOMetadataNode v16 = null;

            try {
               v16 = (IIOMetadataNode)v4.getImageMetadata(v14).getAsTree(v4.getImageMetadata(v14).getNativeMetadataFormatName());
            } catch (Exception v28) {
            }

            int v17 = 0;
            int v18 = 0;
            String v19 = "none";
            if (v16 != null) {
               IIOMetadataNode v20 = this.m1746(v16, "ImageDescriptor");
               if (v20 != null) {
                  try {
                     v17 = Integer.parseInt(v20.getAttribute("imageLeftPosition"));
                  } catch (Exception v27) {
                  }

                  try {
                     v18 = Integer.parseInt(v20.getAttribute("imageTopPosition"));
                  } catch (Exception v26) {
                  }
               }

               IIOMetadataNode v21 = this.m1746(v16, "GraphicControlExtension");
               if (v21 != null) {
                  v19 = v21.getAttribute("disposalMethod");
               }
            }

            if ("restoreToPrevious".equalsIgnoreCase(v19)) {
               v13 = this.m1747(v12);
            }

            Graphics2D v38 = v12.createGraphics();
            v38.drawImage(v15, v17, v18, null);
            v38.dispose();
            v33.add(v12.getRGB(0, 0, v9, v10, null, 0, v9));
            this.f1344.add(this.m1745(v4, v14));
            this.f1343.add(Identifier.method_60655("white", "gif/" + v6 + "/" + v14));

            Graphics2D v39 = v12.createGraphics();
            v39.setComposite(AlphaComposite.Clear);
            if ("restoreToBackgroundColor".equalsIgnoreCase(v19)) {
               v39.fillRect(v17, v18, v15.getWidth(), v15.getHeight());
            } else if ("restoreToPrevious".equalsIgnoreCase(v19) && v13 != null) {
               v39.setComposite(AlphaComposite.Src);
               v39.drawImage(v13, 0, 0, null);
            }

            v39.dispose();
         }

         v4.dispose();

         for (int v36 : this.f1344) {
            this.f1345 += v36;
         }

         int v35 = v9;
         int v37 = v10;
         MinecraftClient.method_1551().execute(() -> {
            for (int v5x = 0; v5x < v33.size(); v5x++) {
               int[] v6x = (int[])v33.get(v5x);
               NativeImage var10000x = new NativeImage;

               var10000x./* $VF: Unable to resugar constructor */<init>(Format.field_4997, v35, v37, false);
               NativeImage v7x = var10000x;

               for (int v8x = 0; v8x < v37; v8x++) {
                  int v9x = 0;

                  while (true) {

                     if (v9x >= v35) {
                        break;
                     }

                     v7x.method_61941(v9x, v8x, v6x[v8x * v35 + v9x]);
                     v9x++;
                  }
               }

               int var13x = v5x;
               NativeImageBackedTexture var14x = new NativeImageBackedTexture(() -> {
                  StringBuilder var10000xx = new StringBuilder;

                  var10000xx./* $VF: Unable to resugar constructor */<init>();
                  return var10000xx.append("white:gif_").append((Object)v6).append("_").append(var13x).toString();
               }, v7x);
               var14x.method_4524();
               MinecraftClient.method_1551().method_1531().method_4616(this.f1343.get(var13x), var14x);
            }

            this.f1347 = true;
         });
      } catch (Exception v32) {
         v32.printStackTrace();
      }
   }

   public int getHeight() {
      return this.height;
   }

   public void draw(DrawContext ctx, int x, int y, int w, int h) {
      int v7 = 851001943;

      this.draw(ctx, x, y, w, h, 1.0F);
   }

   
}
