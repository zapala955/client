package ru.white.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.IntConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.white.utils.misc.Tq;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.vr;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;

@Environment(EnvType.CLIENT)
public class GuiModuleButton {
   private static final float CARD_STEP = 58.0F;
   private boolean f1073 = false;
   private float f1076;
   private static final float BASE_CARD_W = 160.0F;
   private float f1077;
   private final String[] f1071;
   private final IntConsumer f1072;
   private static final float CARD_RADIUS = 7.0F;
   public static final float SHEAR_X = -0.22F;
   private final Animation f1074 = new Animation();
   private static final float BASE_CARD_H = 100.0F;
   private int f1078;
   private final Animation f1075 = new Animation();
   private double f1080;
   private boolean f1079;
   private final Identifier[] f1070;

   public void open() {
      this.f1073 = true;
      this.f1076 = (float)this.f1078;
      this.f1077 = (float)this.f1078;
      this.f1079 = false;
      this.f1074.set(0.0);

      this.f1074.run(1.0, 0.28F, ap.CUBIC_OUT);
      tq.button();
   }

   public boolean mouseReleased(int button) {
      if (button == 0) {

         if (this.f1079) {
            this.f1079 = false;
            this.f1076 = (float)Math.round(this.f1076);
            this.f1076 = Math.max(0.0F, Math.min((float)(this.f1070.length - 1), this.f1076));
            int v2 = (int)this.f1076;
            if (v2 != this.f1078) {
               this.m1355(v2);
            }

            return true;
         }
      }

      return false;
   }

   public int getSelectedIndex() {
      return this.f1078;
   }

   public void close() {
      if (this.f1073) {
         this.f1073 = false;
         this.f1079 = false;

         this.f1074.run(0.0, 0.2F, ap.CUBIC_IN);
         tq.button();

         
      }
   }

   public boolean mouseDragged(double mx, double my, int button, double deltaX, double deltaY) {
      if (this.isOpen() && this.f1079 && button == 0) {
         double v10 = mx - this.f1080;
         this.f1080 = mx;
         double temp2 = v10 / 58.0;

         this.f1076 -= (float)temp2;

         this.f1076 = Math.max(0.0F, Math.min((float)(this.f1070.length - 1), this.f1076));
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode) {
      return this.keyPressed(keyCode, 0, 0);
   }

   public float getOpenProgress() {

      return this.f1074.get();
   }

   public boolean isOpen() {
      if (!this.f1073) {
         float v5;
         int result = (v5 = this.f1074.get() - 0.005F) == 0.0F ? 0 : (v5 < 0.0F ? -1 : 1);

         if (result <= 0) {
            return false;
         }
      }

      return true;
   }

   public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
      if (!this.isOpen()) {
         return false;
      } else {

         if (verticalAmount != 0.0) {
            if (verticalAmount > 0.0) {
               int v9 = Math.max(0, Math.round(this.f1076) - 1);
               this.m1355(v9);
            } else {
               int v11 = Math.min(this.f1070.length - 1, Math.round(this.f1076) + 1);
               this.m1355(v11);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void render(float sw, float sh, double mouseX, double mouseY) {
      float v7 = this.f1074.get();
      if (!(v7 <= 0.005F)) {
         int v8 = up.getColor(4, 4, 8, (int)(140.0F * v7));
         vr.rect(0.0F, 0.0F, sw, sh, v8);
         float v9 = Math.min(sw - 60.0F, 600.0F);
         float v10 = Math.min(sh - 50.0F, 205.0F);
         float v11 = 0.94F + 0.06F * v7;
         float v12 = v9 * v11;
         float v13 = v10 * v11;
         float v14 = (sw - v12) / 2.0F;
         float v15 = (sh - v13) / 2.0F;
         float v16 = v7;

         int v17 = (int)(255.0F * v7);
         vr.blur(v14, v15, v12, v13, 1.0F, 14.0F, up.getColor(10, (int)(v7 * 80.0F)));
         vr.rect(v14, v15, v12, v13, up.getColor(12, 13, 18, (int)(v7 * 230.0F)), 14.0F);
         vr.outline(
            v14,
            v15,
            v12,
            v13,
            0.6F,
            up.getColor(255, 255, 255, (int)((float)v17 * 0.12F)),
            14.0F
         );
         vr.glow(
            v14,
            v15,
            v12,
            v13,
            up.getColor(19, 255, 174, (int)(v7 * 22.0F)),
            14.0F,
            16.0F,
            1.0F
         );
         float v18 = v15 + 12.0F;
         float v51 = v14 + 18.0F;
         float temp3 = v18 + 1.0F;
         int temp5 = 255;
         int var10006 = 255;

         hu.sf_bold.draw("Выбор фона", v51, temp3, 8.5F, up.getColor(temp5, var10006, 255, v17));
         int result = this.f1078 + 1;
         int v38 = this.f1070.length;
         int v39 = result;
         String v19 = v39 + " / " + v38;
         float v20 = hu.sf_bold.getWidth("Выбор фона", 8.5F);
         hu.sf_medium
            .draw(
               v19,
               v14 + 22.0F + v20,
               v18 + 2.0F,
               6.5F,
               up.getColor(19, 255, 174, (int)((float)v17 * 0.85F))
            );
         hu.sf_regular
            .draw(
               "Колёсико мыши или клик по карточке для выбора",
               v14 + 18.0F,
               v18 + 12.0F,
               5.2F,
               up.getColor(150, 150, 160, (int)((float)v17 * 0.65F))
            );
         float v21 = 16.0F;
         float v22 = v14 + v12 - v21 - 14.0F;
         float v23 = v15 + 13.0F;
         boolean v24 = fq.isHovered((float)mouseX, (float)mouseY, v22 - 3.0F, v23 - 3.0F, v21 + 6.0F, v21 + 6.0F);
         this.f1075.run(v24 ? 1.0 : 0.0, 0.16F, ap.QUAD_OUT);
         float v25 = this.f1075.get();
         if (v25 > 0.01F) {
            vr.rect(
               v22 - 2.0F,
               v23 - 2.0F,
               v21 + 4.0F,
               v21 + 4.0F,
               up.getColor(255, 60, 60, (int)(v25 * v7 * 45.0F)),
               5.0F
            );
            vr.outline(
               v22 - 2.0F,
               v23 - 2.0F,
               v21 + 4.0F,
               v21 + 4.0F,
               0.5F,
               up.getColor(255, 80, 80, (int)(v25 * (float)v17 * 0.5F)),
               5.0F
            );
         }

         int v26 = up.interpolate(
            up.getColor(180, 180, 190, (int)((float)v17 * 0.8F)),
            up.getColor(255, 90, 90, v17),
            (double)v25
         );
         hu.sfsemi.drawCentered("✕", v22 + v21 / 2.0F, v23 + v21 / 2.0F - 3.2F, 8.0F, v26);
         vr.rect(
            v14 + 16.0F,
            v15 + 34.0F,
            v12 - 32.0F,
            0.5F,
            up.getColor(255, 255, 255, (int)((float)v17 * 0.07F)),
            0.25F
         );
         float v27 = v15 + 36.0F + (v13 - 36.0F - 24.0F) / 2.0F;
         float v28 = v14 + v12 / 2.0F;
         ArrayList v29 = new ArrayList(this.f1070.length);

         for (int v30 = 0; v30 < this.f1070.length; v30++) {
            float v31 = (float)v30 - this.f1077;
            float v32 = Math.abs(v31);
            float v33 = Math.max(0.68F, 1.0F - 0.11F * v32);
            float v34 = 160.0F * v33;
            float v35 = 100.0F * v33;
            float v36 = v28 + v31 * 58.0F - v34 / 2.0F;
            float v37 = v27 - v35 / 2.0F;
            v29.add(new co.do(v30, v31, v32, v36, v37, v34, v35, v33));
         }

         v29.sort(Comparator.comparingDouble(c -> {
            double var10000x = (double)(-c.absDist);

            return var10000x;
         }));

         for (co.do v45 : v29) {
            this.m1353(v45, v28, v27, mouseX, mouseY, v16, v17);
         }

         if (this.f1078 >= 0 && this.f1078 < this.f1071.length) {
            String v44 = this.f1071[this.f1078];
            float v46 = v27 + 50.0F + 7.0F;
            float v47 = hu.sfsemi.getWidth(v44, 6.8F);
            float v48 = 3.5F;
            float v49 = v48 + 5.0F + v47;
            float v50 = v28 - v49 / 2.0F;
            vr.rect(
               v50, v46 + 1.5F, v48, v48, up.getColor(19, 255, 174, v17), v48 / 2.0F
            );
            v51 = v50 + v48 + 5.0F;
            temp3 = v46 - 0.5F;
            temp5 = 255;
            var10006 = 255;

            hu.sfsemi.draw(v44, v51, temp3, 6.8F, up.getColor(temp5, var10006, v56, v17));
         }
      }
   }

   private boolean m1354(double mx, double my, co.do c) {
      if (!(my < (double)c.y) && !(my > (double)(c.y + c.h))) {
         float v6 = (float)(my - (double)(c.y + c.h / 2.0F));
         float v7 = v6 * -0.22F;
         float v8 = c.x + v7;

         float v9 = v8 + c.w;
         double temp1 = (double)v8;

         return mx >= temp1 && mx <= (double)v9;
      } else {
         return false;
      }
   }

   public GuiModuleButton(Identifier[] textures, String[] names, int initialIndex, IntConsumer onSelectBackground) {
      this.f1076 = 0.0F;
      this.f1077 = 0.0F;
      this.f1078 = 0;
      this.f1079 = false;
      this.f1080 = 0.0;
      this.f1070 = textures;
      this.f1071 = names;
      this.f1078 = Math.max(0, Math.min(textures.length - 1, initialIndex));
      this.f1076 = (float)this.f1078;
      this.f1077 = (float)this.f1078;
      this.f1072 = onSelectBackground;
      this.f1074.set(0.0);
   }

   public void update() {
      this.f1074.update();
      this.f1075.update();
      int result = this.isOpen();

      if (result) {
         float v1 = this.f1076 - this.f1077;
         this.f1077 += v1 * 0.2F;
         float v6;
         result = (v6 = Math.abs(v1) - 0.001F) == 0.0F ? 0 : (v6 < 0.0F ? -1 : 1);

         if (result < 0) {
            this.f1077 = this.f1076;
         }
      }
   }

   private void m1355(int index) {
      index = Math.max(0, Math.min(this.f1070.length - 1, index));
      this.f1076 = (float)index;
      if (this.f1078 != index) {
         this.f1078 = index;
         tq.button();
         if (this.f1072 != null) {

            this.f1072.accept(index);
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.isOpen()) {
         return false;
      } else if (keyCode == 256) {
         this.close();
         return true;
      } else if (keyCode == 263) {
         int v7 = Math.max(0, this.f1078 - 1);
         this.m1355(v7);
         return true;
      } else {

         if (keyCode == 262) {
            int v4 = Math.min(this.f1070.length - 1, this.f1078 + 1);
            this.m1355(v4);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean mouseClicked(double mx, double my, int button, float sw, float sh) {
      if (this.isOpen() && button == 0) {
         float v8 = Math.min(sw - 60.0F, 600.0F);
         float v9 = Math.min(sh - 50.0F, 205.0F);
         float v10 = (sw - v8) / 2.0F;
         float v11 = (sh - v9) / 2.0F;
         float v12 = 16.0F;
         float v13 = v10 + v8 - v12 - 14.0F;
         float v14 = v11 + 13.0F;
         if (fq.isHovered((float)mx, (float)my, v13 - 4.0F, v14 - 4.0F, v12 + 8.0F, v12 + 8.0F)) {
            this.close();
            return true;
         } else {
            float v15 = v11 + 36.0F + (v9 - 36.0F - 24.0F) / 2.0F;
            float v16 = v10 + v8 / 2.0F;
            ArrayList v17 = new ArrayList(this.f1070.length);

            for (int v18 = 0; v18 < this.f1070.length; v18++) {
               float v19 = (float)v18 - this.f1077;
               float v20 = Math.abs(v19);
               float v21 = Math.max(0.68F, 1.0F - 0.11F * v20);
               float v22 = 160.0F * v21;
               float v23 = 100.0F * v21;
               float v24 = v16 + v19 * 58.0F - v22 / 2.0F;
               float v25 = v15 - v23 / 2.0F;
               v17.add(new co.do(v18, v19, v20, v24, v25, v22, v23, v21));
            }

            v17.sort(Comparator.comparingDouble(c -> (double)c.absDist));

            for (co.do v29 : v17) {
               if (this.m1354(mx, my, v29)) {
                  this.m1355(v29.index);

                  return true;
               }
            }

            if (fq.isHovered((float)mx, (float)my, v10, v11, v8, v9)) {
               this.f1079 = true;
               this.f1080 = mx;
               return true;
            } else {
               this.close();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void m1353(co.do c, float centerX, float centerY, double mx, double my, float alphaNorm, int textAlpha) {
      boolean v10 = c.index == this.f1078;
      boolean v11 = c.absDist < 0.45F;
      boolean v12 = this.m1354(mx, my, c);
      float v13 = Math.max(0.32F, 1.0F - 0.22F * c.absDist);
      int v14 = (int)(255.0F * alphaNorm * Math.max(0.4F, 1.0F - 0.18F * c.absDist));
      int v15 = up.getColor((int)(255.0F * v13), (int)(255.0F * v13), (int)(255.0F * v13), v14);
      if (v10 || v11) {
         Identifier result = this.f1070[c.index];
         float temp1 = c.x - 3.0F;

         vr.glowTexture(
            result,
            temp1,
            c.y - 3.0F,
            c.w + 6.0F,
            c.h + 6.0F,
            1.0F,
            9.0F,
            up.getColor(19, 255, 174, (int)(70.0F * alphaNorm)),
            0.0F,
            -0.22F
         );
      } else if (v12) {
         vr.glowTexture(
            this.f1070[c.index],
            c.x - 2.0F,
            c.y - 2.0F,
            c.w + 4.0F,
            c.h + 4.0F,
            1.0F,
            8.0F,
            up.getColor(255, 255, 255, (int)(35.0F * alphaNorm)),
            0.0F,
            -0.22F
         );
      }

      vr.texture(this.f1070[c.index], c.x, c.y, c.w, c.h, 1.0F, 7.0F, v15, 0.0F, -0.22F);
      if (v10 || v11) {
         int v16 = up.getColor(19, 255, 174, textAlpha);
         vr.texture(
            this.f1070[c.index],
            c.x - 1.2F,
            c.y - 1.2F,
            c.w + 2.4F,
            c.h + 2.4F,
            1.0F,
            8.0F,
            up.getColor(19, 255, 174, (int)((float)textAlpha * 0.9F)),
            0.0F,
            -0.22F
         );
         vr.texture(this.f1070[c.index], c.x, c.y, c.w, c.h, 1.0F, 7.0F, v15, 0.0F, -0.22F);
      } else if (v12) {
         vr.texture(
            this.f1070[c.index],
            c.x - 0.8F,
            c.y - 0.8F,
            c.w + 1.6F,
            c.h + 1.6F,
            1.0F,
            7.5F,
            up.getColor(255, 255, 255, (int)((float)textAlpha * 0.45F)),
            0.0F,
            -0.22F
         );
         vr.texture(this.f1070[c.index], c.x, c.y, c.w, c.h, 1.0F, 7.0F, v15, 0.0F, -0.22F);
      }
   }

   public boolean mouseReleased(double mx, double my, 

      return this.mouseReleased(button);
   }

   public void setSelectedIndex(int index) {
      this.f1078 = Math.max(0, Math.min(this.f1070.length - 1, index));
      this.f1076 = (float)this.f1078;
      
   }

   @Environment(EnvType.CLIENT)
   private static class do {
      final float x;
      final float w;
      final int index;
      final float y;
      final float absDist;
      final float dist;
      final float scale;
      final float h;

      do(int index, float dist, float absDist, float x, float y, float w, float h, float scale) {
         this.index = index;
         this.dist = dist;
         this.absDist = absDist;
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
         this.scale = scale;
      }

      
   }
}
