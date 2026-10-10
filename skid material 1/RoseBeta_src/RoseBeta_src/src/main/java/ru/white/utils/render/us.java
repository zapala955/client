package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import ru.white.auth.StrDec;
import ru.white.utils.system.Gu;
import ru.white.utils.system.Hu;

@Environment(EnvType.CLIENT)
public class us {
   private boolean f1530 = false;
   private final tr f1526;
   private final rs f1527;
   private static boolean f1533 = false;
   private final gu f1524 = new gu();
   private final ts f1525 = new ts();
   private final pt f1528;
   private static boolean f1534 = false;
   private static boolean f1532 = false;
   private final qr f1529;
   private static boolean f1531 = false;

   public rs getOutlinePipeline() {

      return this.f1527;
   }

   public us() {
      this.f1526 = new tr();
      this.f1527 = new rs();
      this.f1529 = new qr();
      this.f1528 = new pt();
   }

   private void m1865() {
      if (!this.f1530) {

         this.f1530 = true;
         this.f1524.loadAllFonts(hu.getRegistry());
      }
   }

   public gu getFontRenderer() {
      this.m1865();
      return this.f1524;
   }

   public static void beginOverlay() {
      f1531 = true;
      wr.setEnabled(true);
      f1532 = GL11.glIsEnabled(2929);
      f1533 = GL11.glGetBoolean(2930);
      f1534 = GL11.glIsEnabled(3042);
      GL11.glDisable(2929);
      int v0 = 526746622;
      int result = v0 * (v0 + 1) % 2;

      if (result != 0) {
         }

      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 771);
   }

   public static void drawSprite(Sprite sprite, float x, float y, float width, float height, int color, boolean pixelPerfect) {
      if (sprite != null && width != 0.0F && height != 0.0F) {
         float v7 = pixelPerfect ? 1.0F : 0.0F;
         Identifier result = sprite.method_45852();

         vs.xs.texture(result, x, y, width, height, sprite.method_4594(), sprite.method_4593(), sprite.method_4577(), sprite.method_4575(), color, v7, 0.0F);
      }
   }

   public tr getCircleProgressPipeline() {
      return this.f1526;
   }

   public pt getTexturePipeline() {
      int v1 = -190441185;

      return this.f1528;
   }

   public void flushAll() {
      wr.flushPending();
      int v1 = 1105549694;

      this.f1529.flush();
      this.f1525.flush();
      this.f1526.flush();
      this.f1524.flush();
   }

   public void close() {
      this.f1525.close();
      this.f1526.close();
      this.f1524.close();
      this.f1528.close();
      this.f1529.close();
      this.f1527.close();
      et.close();
   }

   public ts getRectPipeline() {
      return this.f1525;
   }

   public qr getBlurPipeline() {

      return this.f1529;
   }

   public static void endOverlay() {
      wr.setEnabled(false);
      if (f1533) {
         GL11.glDepthMask(true);
      }

      if (f1532) {
         int result = 2929;

         GL11.glEnable(result);
      } else {
         GL11.glDisable(2929);
         
      }

      if (!f1534) {
         GL11.glDisable(3042);
      }

      f1531 = false;
   }

   
}
