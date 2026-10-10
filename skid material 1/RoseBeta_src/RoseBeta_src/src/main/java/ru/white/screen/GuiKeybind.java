package ru.white.screen;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.notification.INotification;
import ru.white.utils.misc.To;
import ru.white.utils.misc.Tq;
import ru.white.module.Category;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.theme.Theme;
import ru.white.theme.xo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.ps;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class GuiKeybind implements ro, IMinecraft {
   private static final no INSTANCE = new no();
   private int f1211;
   private int f1208;
   private String f1212;
   private float f1217;
   private int f1206;
   private final Animation f1205 = new Animation();
   private to f1210;
   private String f1213;
   private float f1216;
   private int f1209;
   private boolean active;
   private int f1207;
   private boolean f1214;
   private long f1215;
   private static final int[] QUICK_PALETTES;

   @Override
   public boolean isActive() {

      return this.active;
   }

   @Override
   public boolean mouseDragged(float deltaX, float deltaY) {
      if (!this.active) {
         return false;
      } else {
         float v3 = (float)(mc.field_1729.method_1603() * (double)mc.method_22683().method_4486() / (double)mc.method_22683().method_4480());
         double result = mc.field_1729.method_1604() * (double)mc.method_22683().method_4502();

         float v4 = (float)(result / (double)mc.method_22683().method_4507());
         Theme v5 = xo.get().getCustomTheme();
         if (this.f1209 != -1) {

            this.m1462(v3, this.f1209, v5);
            return true;
         } else {

            if (this.f1208 != 0) {
               float v6 = Math.min(this.f1216 - 32.0F, 530.0F);
               float v7 = Math.min(this.f1217 - 32.0F, 335.0F);
               float v8 = this.f1216 / 2.0F - v6 / 2.0F;
               float v9 = this.f1217 / 2.0F - v7 / 2.0F;
               float v10 = v9 + 36.0F;
               float v11 = 238.0F;
               float v12 = v8 + 10.0F;
               float v13 = 18.0F;
               float v14 = v10 + v13 + 9.0F;
               float v15 = 14.0F;
               float v16 = 3.0F;
               float v17 = v14 + 4.0F * (v15 + v16) + 5.0F;
               float v18 = v10 + (v7 - 36.0F - 34.0F) - v17 - 6.0F;
               float v19 = v11 - 12.0F;
               float v20 = v12 + 6.0F;
               float v21 = v17 + 16.0F;
               float v22 = v18 - 22.0F;
               float v23 = v19 - 32.0F;
               float v24 = 10.0F;
               float v25 = 4.0F;
               float v26 = v20 + v23 + v25 + 4.0F;
               float v27 = v26 + v24 + v25;
               float v28 = v20 + 4.0F;
               if (this.f1208 == 1) {
                  this.m1463(v3, v4, v28, v21, v23, v22, v5, this.f1206);
               } else if (this.f1208 == 2) {
                  this.m1464(v4, v21, v22, v5, this.f1206);
               } else if (this.f1208 == 3) {
                  this.m1465(v4, v21, v22, v5, this.f1206);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   private void m1462(float mouseX, int sliderIndex, Theme theme) {
      float v4 = Theme.SLIDER_MIN[sliderIndex];
      float v5 = Theme.SLIDER_MAX[sliderIndex];
      float v6 = Math.max(1.0F, this.f1210.width());
      float v7 = MathHelper.method_15363((mouseX - this.f1210.x()) / v6, 0.0F, 1.0F);
      float v8 = v4 + (v5 - v4) * v7;

      theme.setSlider(sliderIndex, v8);
   }

   @Override
   public void closeFromMenuRemoval() {
      this.saveAndExit();
      int result = v1 * (v1 + 1) % 2;

      if (result != 0) {
         }
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {
      return false;
   }

   private void m1459(float x, float y, float w, float h, float mouseX, float mouseY, float alpha, Theme theme, int colorIndex) {
      yt v10 = hu.sf_bold;
      int v11 = theme.get(colorIndex);
      float[] v12 = Color.RGBtoHSB(
         v11 >> 16 & 255,
         v11 >> 8 & 255,
         v11 & 255,
         null
      );
      int v13 = v11 >> 24 & 255;
      vs.ys.rect(x, y, w, h, up.getColor(12, 11, 16, (int)(220.0F * alpha)), 6.0F);

      vs.ys.outline(x, y, w, h, 0.5F, up.getColor(255, (int)(18.0F * alpha)), 6.0F);
      String v14 = Theme.NAMES[colorIndex];
      v10.draw(v14, x + 6.0F, y + 5.5F, 6.5F, up.getColor(255, (int)(240.0F * alpha)));
      String v15 = String.format("#%06X", v11 & 16777215);

      float v16 = v10.getWidth(v15, 6.2F);
      v10.draw(v15, x + w - v16 - 6.0F, y + 5.5F, 6.2F, up.multAlpha(theme.get(0), alpha));
      float v17 = y + 16.0F;
      float v18 = h - 22.0F;
      float v19 = w - 32.0F;
      float v20 = 10.0F;
      float v21 = 4.0F;
      float v22 = x + v19 + v21 + 4.0F;
      float v23 = v22 + v20 + v21;
      float v24 = x + 4.0F;
      int v25 = Color.HSBtoRGB(v12[0], 1.0F, 1.0F);
      vs.ys.rect(v24, v17, v19, v18, v25, 3.0F);
      int[] v26 = new int[]{
         up.getColor(255, 255, 255, (int)(255.0F * alpha)),
         up.getColor(255, 255, 255, 0),
         up.getColor(255, 255, 255, 0),
         up.getColor(255, 255, 255, (int)(255.0F * alpha))
      };
      vs.ys.gradientRect(v24, v17, v19, v18, v26, 3.0F);
      int[] v27 = new int[]{
         up.getColor(0, 0, 0, 0), up.getColor(0, 0, 0, 0), up.getColor(0, 0, 0, (int)(255.0F * alpha)), up.getColor(0, 0, 0, (int)(255.0F * alpha))
      };
      vs.ys.gradientRect(v24, v17, v19, v18, v27, 3.0F);
      vs.ys.outline(v24, v17, v19, v18, 0.5F, up.getColor(255, (int)(35.0F * alpha)), 3.0F);
      float v28 = v24 + v12[1] * v19;
      float v29 = v17 + (1.0F - v12[2]) * v18;
      vs.ys.rect(v28 - 3.5F, v29 - 3.5F, 7.0F, 7.0F, up.multAlpha(v11, alpha), 3.5F);
      vs.ys.outline(v28 - 3.5F, v29 - 3.5F, 7.0F, 7.0F, 1.0F, up.getColor(255, (int)(255.0F * alpha)), 3.5F);

      for (int v30 = 0; v30 < (int)v18; v30++) {
         float v31 = (float)v30 / v18;
         int v32 = Color.HSBtoRGB(v31, 1.0F, 1.0F);
         vs.ys.rect(v22, v17 + (float)v30, v20, 1.2F, up.multAlpha(v32, alpha), 0.0F);
      }

      vs.ys.outline(v22, v17, v20, v18, 0.5F, up.getColor(255, (int)(40.0F * alpha)), 2.0F);
      float v36 = v17 + v12[0] * v18;
      vs.ys.rect(v22 - 0.5F, v36 - 1.2F, v20 + 1.0F, 2.4F, up.getColor(255, (int)(255.0F * alpha)), 1.0F);
      int v37 = v11 | -16777216;
      int[] v38 = new int[]{up.getColor(0, 0, 0, 0), up.getColor(0, 0, 0, 0), up.multAlpha(v37, alpha), up.multAlpha(v37, alpha)};
      vs.ys.rect(v23, v17, v20, v18, up.getColor(25, 25, 30, (int)(200.0F * alpha)), 2.0F);
      vs.ys.gradientRect(v23, v17, v20, v18, v38, 2.0F);
      vs.ys.outline(v23, v17, v20, v18, 0.5F, up.getColor(255, (int)(40.0F * alpha)), 2.0F);
      float v33 = v17 + (1.0F - (float)v13 / 255.0F) * v18;
      vs.ys.rect(v23 - 0.5F, v33 - 1.2F, v20 + 1.0F, 2.4F, up.getColor(255, (int)(255.0F * alpha)), 1.0F);
   }

   private void m1465(float mouseY, float py, float ph, Theme theme, int idx) {
      int v6 = theme.get(idx);
      int v7 = Math.round(MathHelper.method_15363(1.0F - (mouseY - py) / ph, 0.0F, 1.0F) * 255.0F);
      int temp2 = v7 << 24;
      int temp3 = v6 & 16777215;

      theme.set(idx, temp2 | temp3);
   }

   private void m1460(float rx, float ry, float rw, float rh, float mouseX, float mouseY, float alpha, Theme theme) {
      yt v9 = hu.sf_bold;
      yt v10 = hu.sf_regular;
      int v11 = theme.get(0);
      int v12 = theme.get(5);
      int v13 = theme.get(1);
      int v14 = theme.get(7);
      int v15 = theme.get(2);
      int v16 = theme.get(6);
      int v17 = theme.get(3);
      int v18 = theme.get(4);
      float v19 = theme.getSlider(0);
      int v20 = Math.round(theme.getSlider(1));
      boolean v21 = theme.getBool(0);
      if (v21) {
         vs.ys.glow(rx, ry, rw, rh, up.getColor(0, (int)(70.0F * alpha)), 8.0F, 16.0F, 1.0F);
      }

      if (v20 > 0) {
         vs.ws.blur(rx, ry, rw, rh, alpha * v19, 8.0F, up.multAlpha(v13, alpha * v19));
      } else {
         vs.ys.rect(rx, ry, rw, rh, up.multAlpha(v13, alpha * v19), 8.0F);
      }

      vs.ys.outline(rx, ry, rw, rh, 0.7F, up.multAlpha(v15, alpha), 8.0F);
      float v22 = (float)(Math.sin((double)System.currentTimeMillis() * 0.005) * 0.5 + 0.5);
      vs.ys.rect(rx + 8.0F, ry + 9.5F, 5.0F, 5.0F, up.multAlpha(v11, (0.6F + 0.4F * v22) * alpha), 2.5F);
      vs.ys.glow(rx + 8.0F, ry + 9.5F, 5.0F, 5.0F, up.multAlpha(v11, 0.4F * v22 * alpha), 2.5F, 4.0F, 1.0F);
      v9.draw(
         "Живой предпросмотр (Live HUD)",
         rx + 18.0F,
         ry + 7.5F,
         8.0F,
         up.multAlpha(v17, alpha)
      );
      v10.draw(
         "Реагирует на настройки темы",
         rx + rw - v10.getWidth("Реагирует на настройки темы", 5.5F) - 8.0F,
         ry + 8.5F,
         5.5F,
         up.multAlpha(v18, alpha)
      );
      vs.ys.rect(rx + 6.0F, ry + 20.0F, rw - 12.0F, 0.5F, up.multAlpha(v16, alpha), 0.25F);
      float v23 = rx + 8.0F;
      float v24 = ry + 26.0F;
      float v25 = 118.0F;
      float v26 = 22.0F;
      vs.ys.rect(v23, v24, v25, v26, up.multAlpha(v14, alpha), 5.0F);
      vs.ys.outline(v23, v24, v25, v26, 0.5F, up.multAlpha(v15, alpha), 5.0F);
      hu.mainmenuicons.draw("e", v23 + 6.0F, v24 + 7.0F, 8.0F, up.multAlpha(v11, alpha));
      v9.draw("Rose", v23 + 18.0F, v24 + 6.5F, 7.5F, up.multAlpha(v17, alpha));
      v10.draw("Solution", v23 + 41.0F, v24 + 6.5F, 7.5F, up.multAlpha(v11, alpha));
      v10.draw("240 FPS", v23 + 80.0F, v24 + 7.0F, 6.0F, up.multAlpha(v18, alpha));
      Category[] v27 = ff.values();
      if (this.f1211 < 0 || this.f1211 >= v27.length) {
         this.f1211 = 0;
      }

      Category v28 = v27[this.f1211];
      float v29 = rx + 132.0F;
      float v30 = ry + 26.0F;
      float v31 = rw - 140.0F;
      float v32 = 50.0F;
      vs.ys.rect(v29, v30, v31, v32, up.multAlpha(v14, alpha), 6.0F);
      vs.ys.outline(v29, v30, v31, v32, 0.5F, up.multAlpha(v15, alpha), 6.0F);
      boolean v33 = fq.isHovered(mouseX, mouseY, v29 + 4.0F, v30 + 4.0F, v31 - 8.0F, 12.0F);
      String v60 = v28.getName();
      v9.draw("Binds · " + v60, v29 + 7.0F, v30 + 5.5F, 7.0F, v33 ? up.multAlpha(v11, alpha) : up.multAlpha(v17, alpha));
      hu.nightix_2.draw(v28.getIcon(), v29 + v31 - 13.0F, v30 + 5.8F, 6.5F, up.multAlpha(v12, alpha));
      vs.ys.rect(v29 + 5.0F, v30 + 16.0F, v31 - 10.0F, 0.4F, up.multAlpha(v16, alpha), 0.2F);
      v10.draw("Aura", v29 + 7.0F, v30 + 21.0F, 6.2F, up.multAlpha(v17, alpha));
      v9.draw("R", v29 + v31 - 14.0F, v30 + 21.0F, 6.2F, up.multAlpha(v12, alpha));
      v10.draw("TargetStrafe", v29 + 7.0F, v30 + 34.0F, 6.2F, up.multAlpha(v18, alpha));
      v9.draw(
         "V",
         v29 + v31 - 14.0F,
         v30 + 34.0F,
         6.2F,
         up.getColor(160, 160, 175, (int)(200.0F * alpha))
      );
      float v34 = rx + 8.0F;
      float v35 = ry + 54.0F;
      float v36 = 118.0F;
      float v37 = 22.0F;
      boolean v38 = fq.isHovered(mouseX, mouseY, v34, v35, v36, v37);
      vs.ys.glow(v34, v35, v36, v37, up.multAlpha(v11, (v38 ? 0.4F : 0.2F) * alpha), 4.0F, 8.0F, 1.0F);
      vs.ys.rect(v34, v35, v36, v37, up.multAlpha(v11, (v38 ? 1.0F : 0.9F) * alpha), 4.5F);
      v9.drawCentered(
         "Активная кнопка",
         v34 + v36 / 2.0F,
         v35 + 6.5F,
         7.2F,
         up.getColor(20, 20, 20, (int)(245.0F * alpha))
      );
      float v39 = rx + 8.0F;
      float v40 = ry + 82.0F;
      float v41 = rw - 16.0F;
      float v42 = 36.0F;
      vs.ys.rect(v39, v40, v41, v42, up.multAlpha(v14, alpha), 6.0F);
      vs.ys.outline(v39, v40, v41, v42, 0.5F, up.multAlpha(v15, alpha), 6.0F);
      if (this.f1214) {
         vr.texture(
            ps.ARTWORK_ID,
            v39 + 6.0F,
            v40 + 6.0F,
            24.0F,
            24.0F,
            0.0F,
            0.0F,
            1.0F,
            1.0F,
            up.getColor(255, 255, 255, (int)(255.0F * alpha)),
            0.5F,
            4.0F
         );
         vr.outline(
            v39 + 6.0F,
            v40 + 6.0F,
            24.0F,
            24.0F,
            0.5F,
            up.getColor(255, 255, 255, (int)(50.0F * alpha)),
            4.0F
         );
      } else {
         vr.rect(v39 + 6.0F, v40 + 6.0F, 24.0F, 24.0F, up.multAlpha(v11, alpha), 4.0F);
         hu.icon
            .draw(
               "M",
               v39 + 13.5F,
               v40 + 13.5F,
               9.0F,
               up.getColor(255, 255, 255, (int)(255.0F * alpha))
            );
      }

      float v43 = v41 - 75.0F;
      v9.drawFadingText(this.f1212, v39 + 36.0F, v40 + 7.0F, v43, up.multAlpha(v17, alpha), 7.8F);
      v10.drawFadingText(this.f1213, v39 + 36.0F, v40 + 20.0F, v43, up.multAlpha(v18, alpha), 6.2F);
      double v44 = (double)(System.currentTimeMillis() % (6251823436867160680L + -6251823436857160680L)) * 0.001;
      float v46 = v39 + v41 - 28.0F;
      float v47 = v40 + v42 / 2.0F;
      float v48 = 2.2F;
      float v49 = 2.2F;

      for (int v50 = 0; v50 < 4; v50++) {
         double v51 = 4.8 + (v50 == 0 ? 0.9 : (v50 == 1 ? 2.6 : (v50 == 2 ? 1.7 : 3.1)));
         double v53 = Math.sin(v44 * v51 + (double)v50 * 1.85) * 0.5 + 0.5;
         float v55 = (float)(4.0 + v53 * 14.0);
         float v56 = v46 + (float)v50 * (v48 + v49);
         float v57 = v47 - v55 / 2.0F;
         vr.rect(v56, v57, v48, v55, up.multAlpha(v12, alpha), v48 / 2.0F);
      }

      float v66 = rx + 8.0F;
      float v67 = ry + 124.0F;
      float v52 = rw - 16.0F;
      float v68 = 20.0F;
      vs.ys.rect(v66, v67, v52, v68, up.getColor(0, (int)(45.0F * alpha)), 5.0F);
      vs.ys.outline(v66, v67, v52, v68, 0.4F, up.multAlpha(v16, alpha), 5.0F);
      v9.draw("Палитра:", v66 + 6.0F, v67 + 6.0F, 6.2F, up.multAlpha(v17, alpha));
      float v54 = v66 + 45.0F;
      float v69 = (v52 - 50.0F) / 8.0F;

      for (int v70 = 0; v70 < 8; v70++) {
         float v72 = v54 + (float)v70 * v69;
         vs.ys.rect(v72 + 1.0F, v67 + 4.0F, v69 - 2.0F, v68 - 8.0F, up.multAlpha(theme.get(v70), alpha), 2.0F);
      }

      float v71 = rx + 8.0F;
      float v73 = ry + 150.0F;
      float v58 = rw - 16.0F;
      float v59 = rh - (v73 - ry) - 6.0F;
      vs.ys.rect(v71, v73, v58, v59, up.getColor(0, (int)(45.0F * alpha)), 5.0F);
      vs.ys.outline(v71, v73, v58, v59, 0.4F, up.multAlpha(v16, alpha), 5.0F);
      v9.draw(
         "Параметры кастомной темы:",
         v71 + 6.0F,
         v73 + 5.5F,
         6.2F,
         up.multAlpha(v17, alpha)
      );
      int v62 = (int)(v19 * 100.0F);
      v10.draw(
         "Прозрачность: "
            + v62
            + "%   •   Размытие (Blur): "
            + v20
            + "px",
         v71 + 6.0F,
         v73 + 16.5F,
         5.8F,
         up.multAlpha(v18, alpha)
      );
      String temp1 = v21 ? "Включена" : "Выключена";
      String v63 = theme.getBool(1) ? "Включено" : "Выключено";
      String v64 = temp1;
      v10.draw(
         "Тень: " + v64 + "   •   Искажение: " + v63,
         v71 + 6.0F,
         v73 + 26.5F,
         5.8F,
         up.multAlpha(v18, alpha)
      );
   }

   private GuiKeybind() {
      this.f1206 = 0;
      this.f1207 = 0;
      this.f1208 = 0;
      this.f1209 = -1;
      this.f1210 = to.EMPTY;
      this.f1211 = 0;
      this.f1212 = "Нет трека";
      this.f1213 = "Музыка не играет";
      this.f1214 = false;
      this.f1215 = 0L;
   }

   @Override
   public boolean mouseReleased(int button) {
      if (button == 0) {

         this.f1208 = 0;
         this.f1209 = -1;
      }

      return false;
   }

   private void m1461(float sx, float sy, float sw, float sh, float mouseX, float mouseY, float alpha, Theme theme) {
      yt v9 = hu.sf_bold;
      int v10 = theme.get(0);
      vs.ys.rect(
         sx + 8.0F, sy, sw - 16.0F, 0.5F, up.getColor(255, 255, 255, (int)(20.0F * alpha)), 0.25F
      );
      float v11 = 20.0F;
      float v12 = 8.0F;
      float v13 = 120.0F;
      float v14 = 105.0F;
      float v15 = 115.0F;
      float v16 = v13 + v14 + v15 + v12 * 2.0F;
      float v17 = sx + sw / 2.0F - v16 / 2.0F;
      float v18 = sy + (sh - v11) / 2.0F + 1.0F;
      boolean v19 = fq.isHovered(mouseX, mouseY, v17, v18, v13, v11);
      vs.ys.glow(v17, v18, v13, v11, up.multAlpha(v10, (v19 ? 0.45F : 0.2F) * alpha), 4.0F, 8.0F, 1.0F);
      vs.ys.rect(v17, v18, v13, v11, up.multAlpha(v10, (v19 ? 1.0F : 0.88F) * alpha), 4.0F);
      hu.icon
         .draw(
            "S",
            v17 + 10.0F,
            v18 + 5.5F,
            7.5F,
            up.getColor(20, 20, 20, (int)(240.0F * alpha))
         );
      v9.draw(
         "Сохранить и выйти",
         v17 + 22.0F,
         v18 + 5.5F,
         6.8F,
         up.getColor(20, 20, 20, (int)(240.0F * alpha))
      );
      float v20 = v17 + v13 + v12;
      boolean v21 = fq.isHovered(mouseX, mouseY, v20, v18, v14, v11);
      vs.ys.rect(
         v20,
         v18,
         v14,
         v11,
         v21 ? up.getColor(255, (int)(30.0F * alpha)) : up.getColor(255, (int)(15.0F * alpha)),
         4.0F
      );
      vs.ys.outline(v20, v18, v14, v11, 0.5F, v21 ? up.multAlpha(v10, alpha) : up.getColor(255, (int)(30.0F * alpha)), 4.0F);
      v9.drawCentered(
         "⚡ Применить тему",
         v20 + v14 / 2.0F,
         v18 + 5.5F,
         6.8F,
         up.getColor(235, (int)(240.0F * alpha))
      );
      float v22 = v20 + v14 + v12;
      boolean v23 = fq.isHovered(mouseX, mouseY, v22, v18, v15, v11);
      int v25;
      if (v23) {
         v25 = 255;
         float temp5 = 30.0F * alpha;

         v25 = up.getColor(v25, (int)temp5);
      } else {
         v25 = up.getColor(255, (int)(15.0F * alpha));
      }

      vs.ys.rect(v22, v18, v15, v11, v25, 4.0F);
      vs.ys.outline(
         v22,
         v18,
         v15,
         v11,
         0.5F,
         v23 ? up.getColor(255, (int)(60.0F * alpha)) : up.getColor(255, (int)(30.0F * alpha)),
         4.0F
      );
      v9.drawCentered(
         "🔄 Сброс настроек",
         v22 + v15 / 2.0F,
         v18 + 5.5F,
         6.8F,
         up.getColor(220, (int)(220.0F * alpha))
      );
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else {
         Theme result = xo.get().getCustomTheme();

         Theme v4 = result;
         float v5 = Math.min(this.f1216 - 32.0F, 530.0F);
         float v6 = Math.min(this.f1217 - 32.0F, 335.0F);
         float v7 = this.f1216 / 2.0F - v5 / 2.0F;
         float v8 = this.f1217 / 2.0F - v6 / 2.0F;
         float v9 = 38.0F;
         float v10 = 14.0F;
         float v11 = v7 + v5 - v9 - 10.0F;
         float v12 = v8 + 9.0F;
         if (fq.isHovered(mouseX, mouseY, v11, v12, v9, v10) && button == 0) {
            this.saveAndExit();
            tq.editor();
            return true;
         } else {
            float v13 = v7 + 225.0F;
            float v14 = v8 + 9.0F;
            float v15 = 13.0F;
            float v16 = 4.0F;

            for (int v17 = 0; v17 < QUICK_PALETTES.length; v17++) {
               float v18 = v13 + (float)v17 * (v15 + v16);
               if (fq.isHovered(mouseX, mouseY, v18, v14, v15, v15) && button == 0) {
                  int v19 = QUICK_PALETTES[v17];
                  v4.set(0, v19);
                  v4.set(5, v19);
                  tq.button();
                  return true;
               }
            }

            float v46 = v8 + 36.0F;
            float v47 = 238.0F;
            float v48 = v7 + 10.0F;
            float v20 = 18.0F;
            float v21 = (v47 - 12.0F) / 2.0F;
            float v22 = v48 + 4.0F;
            float v23 = v22 + v21 + 4.0F;
            float v24 = v46 + 4.0F;
            if (fq.isHovered(mouseX, mouseY, v22, v24, v21, v20) && button == 0) {
               this.f1207 = 0;
               tq.button();
               return true;
            } else if (fq.isHovered(mouseX, mouseY, v23, v24, v21, v20) && button == 0) {
               this.f1207 = 1;
               tq.button();
               return true;
            } else {
               float v25 = v46 + v20 + 9.0F;
               if (this.f1207 == 0) {
                  float v26 = 5.0F;
                  float v27 = (v47 - v26 * 2.0F - 4.0F) / 2.0F;
                  float v28 = 14.0F;
                  float v29 = 3.0F;

                  for (int v30 = 0; v30 < 8; v30++) {
                     int v31 = v30 % 2;
                     int v32 = v30 / 2;
                     float v33 = v48 + v26 + (float)v31 * (v27 + 4.0F);
                     float v34 = v25 + (float)v32 * (v28 + v29);
                     if (fq.isHovered(mouseX, mouseY, v33, v34, v27, v28) && button == 0) {
                        this.f1206 = v30;
                        tq.button();
                        return true;
                     }
                  }

                  float v59 = v25 + 4.0F * (v28 + v29) + 5.0F;
                  float v61 = v46 + (v6 - 36.0F - 34.0F) - v59 - 6.0F;
                  float v63 = v47 - 12.0F;
                  float v65 = v48 + 6.0F;
                  float v67 = v59 + 16.0F;

                  float v35 = v61 - 22.0F;
                  float v36 = v63 - 32.0F;
                  float v37 = 10.0F;
                  float v38 = 4.0F;
                  float v39 = v65 + v36 + v38 + 4.0F;
                  float v40 = v39 + v37 + v38;
                  float v41 = v65 + 4.0F;
                  if (fq.isHovered(mouseX, mouseY, v41, v67, v36, v35) && button == 0) {
                     this.f1208 = 1;
                     this.m1463(mouseX, mouseY, v41, v67, v36, v35, v4, this.f1206);
                     return true;
                  }

                  if (fq.isHovered(mouseX, mouseY, v39, v67, v37, v35) && button == 0) {
                     this.f1208 = 2;
                     this.m1464(mouseY, v67, v35, v4, this.f1206);
                     return true;
                  }

                  if (fq.isHovered(mouseX, mouseY, v40, v67, v37, v35) && button == 0) {
                     this.f1208 = 3;
                     this.m1465(mouseY, v67, v35, v4, this.f1206);
                     return true;
                  }
               } else {
                  float v49 = v25 + 4.0F;

                  for (int v51 = 0; v51 < v4.sliders.length; v51++) {
                     float v54 = 26.0F;
                     if (fq.isHovered(mouseX, mouseY, v48 + 12.0F, v49 + 12.0F, v47 - 24.0F, 12.0F) && button == 0) {
                        this.f1209 = v51;
                        this.f1210 = new to(v48 + 12.0F, v49 + 16.0F, v47 - 24.0F, 4.5F);
                        this.m1462(mouseX, v51, v4);
                        return true;
                     }

                     v49 += v54 + 7.0F;
                  }

                  for (int v52 = 0; v52 < Theme.BOOL_NAMES.length; v52++) {
                     float v55 = 22.0F;
                     if (fq.isHovered(mouseX, mouseY, v48 + 6.0F, v49, v47 - 12.0F, v55) && button == 0) {
                        boolean v57 = !v4.getBool(v52);
                        v4.setBool(v52, v57);
                        tq.toggle(v57);
                        return true;
                     }

                     v49 += v55 + 7.0F;
                  }
               }

               float v50 = v48 + v47 + 8.0F;
               float v53 = v5 - v47 - 18.0F;
               float v56 = v50 + 132.0F;
               float v58 = v46 + 26.0F;
               float v60 = v53 - 140.0F;
               float v62 = 50.0F;
               if (fq.isHovered(mouseX, mouseY, v56, v58, v60, v62) && button == 0) {
                  this.f1211 = (this.f1211 + 1) % ff.values().length;
                  tq.category(this.f1211, ff.values().length);
                  return true;
               } else {
                  float v64 = v8 + v6 - 34.0F;
                  float v66 = 20.0F;
                  float v68 = 8.0F;
                  float v69 = 120.0F;
                  float v70 = 105.0F;
                  float v71 = 115.0F;
                  float v72 = v69 + v70 + v71 + v68 * 2.0F;
                  float v73 = v7 + v5 / 2.0F - v72 / 2.0F;
                  float v74 = v64 + (34.0F - v66) / 2.0F + 1.0F;
                  if (fq.isHovered(mouseX, mouseY, v73, v74, v69, v66) && button == 0) {
                     this.saveAndExit();
                     tq.editor();
                     return true;
                  } else {
                     float v75 = v73 + v69 + v68;
                     if (fq.isHovered(mouseX, mouseY, v75, v74, v70, v66) && button == 0) {
                        ko.selectedTheme = ru.white.manager.Theme.CUSTOM;
                        ko.preSelectedTheme = ru.white.manager.Theme.CUSTOM;
                        Client.get().guiManager().setGuiTheme(ru.white.manager.Theme.CUSTOM);
                        xo.get().setActive(v4);
                        xo v77 = xo.get();

                        v77.saveTheme(v4);
                        tq.editor();
                        return true;
                     } else {
                        float v42 = v75 + v70 + v68;
                        if (fq.isHovered(mouseX, mouseY, v42, v74, v71, v66) && button == 0) {
                           this.m1466(v4);
                           tq.editor();
                           return true;
                        } else {
                           return fq.isHovered(mouseX, mouseY, v7, v8, v5, v6);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active) {
         this.f1216 = width;
         this.f1217 = height;
         this.f1205.update();
         float v6 = this.f1205.get() * parentAlpha;
         if (!(v6 <= 0.01F)) {
            this.m1456();
            Theme v7 = xo.get().getCustomTheme();
            vs.ys.rect(0.0F, 0.0F, width, height, up.getColor(0, 0, 0, (int)(185.0F * v6)));
            float result = width - 32.0F;

            float v8 = Math.min(result, 530.0F);
            float v9 = Math.min(height - 32.0F, 335.0F);
            float v10 = width / 2.0F - v8 / 2.0F;
            float v11 = height / 2.0F - v9 / 2.0F;
            int v12 = v7.get(0);
            vs.ys.glow(v10, v11, v8, v9, up.multAlpha(v12, 0.18F * v6), 12.0F, 28.0F, 1.0F);
            vs.ys.glow(v10, v11, v8, v9, up.getColor(0, (int)(120.0F * v6)), 10.0F, 16.0F, 1.0F);
            vs.ws.blur(
               v10,
               v11,
               v8,
               v9,
               v6,
               10.0F,
               up.getColor(12, 11, 16, (int)(235.0F * v6))
            );
            vs.ys.rect(
               v10, v11, v8, v9, up.getColor(15, 14, 20, (int)(245.0F * v6)), 10.0F
            );
            vs.ys.outline(v10, v11, v8, v9, 0.7F, up.multAlpha(v12, 0.5F * v6), 10.0F);
            vs.ys.outline(v10 + 0.5F, v11 + 0.5F, v8 - 1.0F, v9 - 1.0F, 0.4F, up.getColor(255, (int)(25.0F * v6)), 9.5F);
            this.m1457(v10, v11, v8, mouseX, mouseY, v6, v7);
            float v13 = v11 + 36.0F;
            float v14 = 34.0F;
            float v15 = v9 - 36.0F - v14;
            float v16 = 238.0F;
            float v17 = v8 - v16 - 18.0F;
            float v18 = v10 + 10.0F;
            float v19 = v18 + v16 + 8.0F;
            this.m1458(v18, v13, v16, v15, mouseX, mouseY, v6, v7);
            this.m1460(v19, v13, v17, v15, mouseX, mouseY, v6, v7);
            this.m1461(v10, v11 + v9 - v14, v8, v14, mouseX, mouseY, v6, v7);
         }
      }
   }

   public void open() {
      if (!this.active) {
         this.active = true;
         this.f1206 = 0;
         this.f1207 = 0;
         this.f1208 = 0;
         this.f1209 = -1;
         this.f1205.set(0.0);
         this.f1205.run(1.0, 0.26F, ap.EXPO_OUT);

         
      }
   }

   private void m1458(float lx, float ly, float lw, float lh, float mouseX, float mouseY, float alpha, Theme theme) {
      yt v9 = hu.sf_bold;
      yt v10 = hu.sf_regular;
      int result = theme.get(0);

      int v11 = result;
      vs.ys.rect(lx, ly, lw, lh, up.getColor(20, 19, 26, (int)(220.0F * alpha)), 7.0F);
      vs.ys.outline(lx, ly, lw, lh, 0.5F, up.getColor(255, 255, 255, (int)(18.0F * alpha)), 7.0F);
      float v12 = 18.0F;
      float v13 = (lw - 12.0F) / 2.0F;
      float v14 = lx + 4.0F;
      float v15 = v14 + v13 + 4.0F;
      float v16 = ly + 4.0F;
      boolean v17 = fq.isHovered(mouseX, mouseY, v14, v16, v13, v12);
      boolean v18 = fq.isHovered(mouseX, mouseY, v15, v16, v13, v12);
      int v19 = this.f1207 == 0
         ? up.multAlpha(v11, 0.25F * alpha)
         : (v17 ? up.getColor(255, (int)(18.0F * alpha)) : up.getColor(255, (int)(6.0F * alpha)));
      vs.ys.rect(v14, v16, v13, v12, v19, 4.0F);
      if (this.f1207 == 0) {
         vs.ys.outline(v14, v16, v13, v12, 0.6F, up.multAlpha(v11, 0.7F * alpha), 4.0F);
      }

      v9.drawCentered(
         "🎨 Цвета темы",
         v14 + v13 / 2.0F,
         v16 + 5.0F,
         6.8F,
         this.f1207 == 0 ? up.multAlpha(v11, alpha) : up.getColor(200, (int)(220.0F * alpha))
      );
      int v20 = this.f1207 == 1
         ? up.multAlpha(v11, 0.25F * alpha)
         : (v18 ? up.getColor(255, (int)(18.0F * alpha)) : up.getColor(255, (int)(6.0F * alpha)));
      vs.ys.rect(v15, v16, v13, v12, v20, 4.0F);
      if (this.f1207 == 1) {
         vs.ys.outline(v15, v16, v13, v12, 0.6F, up.multAlpha(v11, 0.7F * alpha), 4.0F);
      }

      v9.drawCentered(
         "⚙ Эффекты & Блюр",
         v15 + v13 / 2.0F,
         v16 + 5.0F,
         6.8F,
         this.f1207 == 1 ? up.multAlpha(v11, alpha) : up.getColor(200, (int)(220.0F * alpha))
      );
      float v21 = ly + v12 + 9.0F;
      if (this.f1207 == 0) {
         float v22 = 5.0F;
         float v23 = (lw - v22 * 2.0F - 4.0F) / 2.0F;
         float v24 = 14.0F;
         float v25 = 3.0F;

         for (int v26 = 0; v26 < 8; v26++) {
            int v27 = v26 % 2;
            int v28 = v26 / 2;
            float v29 = lx + v22 + (float)v27 * (v23 + 4.0F);
            float v30 = v21 + (float)v28 * (v24 + v25);
            boolean v31 = this.f1206 == v26;
            boolean v32 = fq.isHovered(mouseX, mouseY, v29, v30, v23, v24);
            int v33 = v31
               ? up.multAlpha(v11, 0.22F * alpha)
               : (v32 ? up.getColor(255, (int)(20.0F * alpha)) : up.getColor(255, (int)(8.0F * alpha)));
            vs.ys.rect(v29, v30, v23, v24, v33, 3.5F);
            if (v31) {
               vs.ys.outline(v29, v30, v23, v24, 0.6F, up.multAlpha(v11, 0.85F * alpha), 3.5F);
            }

            int v34 = theme.get(v26);
            float v35 = 7.5F;
            float v36 = v29 + 4.0F;
            float v37 = v30 + (v24 - v35) / 2.0F;
            vs.ys.rect(v36, v37, v35, v35, up.multAlpha(v34, alpha), 2.5F);
            vs.ys.outline(v36, v37, v35, v35, 0.5F, up.getColor(255, (int)(80.0F * alpha)), 2.5F);
            v10.draw(
               Theme.NAMES[v26],
               v36 + v35 + 3.5F,
               v30 + 4.2F,
               5.5F,
               v31 ? up.getColor(255, (int)(255.0F * alpha)) : up.getColor(210, (int)(210.0F * alpha))
            );
         }

         float v48 = v21 + 4.0F * (v24 + v25) + 5.0F;
         float v51 = ly + lh - v48 - 6.0F;
         this.m1459(lx + 6.0F, v48, lw - 12.0F, v51, mouseX, mouseY, alpha, theme, this.f1206);
      } else {
         float v41 = v21 + 4.0F;

         for (int v42 = 0; v42 < theme.sliders.length; v42++) {
            float v44 = theme.getSlider(v42);
            float v46 = Theme.SLIDER_MIN[v42];
            float v49 = Theme.SLIDER_MAX[v42];
            String v52 = Theme.SLIDER_NAMES[v42];
            float v54 = 26.0F;
            vs.ys.rect(lx + 6.0F, v41, lw - 12.0F, v54, up.getColor(255, (int)(7.0F * alpha)), 4.5F);
            v9.draw(v52, lx + 12.0F, v41 + 4.5F, 6.5F, up.getColor(235, (int)(240.0F * alpha)));
            String v68;
            if (v42 == 0) {
               int v38 = (int)(v44 * 100.0F);
               v68 = v38 + "%";
            } else {
               int v39 = (int)v44;
               v68 = v39 + "px";
            }

            String v56 = v68;
            v9.draw(v56, lx + lw - 12.0F - v9.getWidth(v56, 6.5F) - 6.0F, v41 + 4.5F, 6.5F, up.multAlpha(v11, alpha));
            float v58 = lx + 12.0F;
            float v60 = v41 + 16.0F;
            float v62 = lw - 24.0F;
            float v64 = 4.5F;
            float v66 = MathHelper.method_15363((v44 - v46) / (v49 - v46), 0.0F, 1.0F);
            vs.ys.rect(v58, v60, v62, v64, up.getColor(255, (int)(22.0F * alpha)), 2.2F);
            if (v66 > 0.01F) {
               vs.ys.rect(v58, v60, v62 * v66, v64, up.multAlpha(v11, alpha), 2.2F);
               vs.ys.glow(v58, v60, v62 * v66, v64, up.multAlpha(v11, 0.3F * alpha), 2.2F, 5.0F, 1.0F);
            }

            float v67 = v58 + v62 * v66;
            vs.ys.rect(v67 - 2.5F, v60 - 1.5F, 5.0F, v64 + 3.0F, up.getColor(255, (int)(255.0F * alpha)), 2.0F);
            v41 += v54 + 7.0F;
         }

         for (int v43 = 0; v43 < Theme.BOOL_NAMES.length; v43++) {
            boolean v45 = theme.getBool(v43);
            String v47 = Theme.BOOL_NAMES[v43];
            float v50 = 22.0F;
            vs.ys.rect(lx + 6.0F, v41, lw - 12.0F, v50, up.getColor(255, (int)(7.0F * alpha)), 4.5F);
            v9.draw(v47, lx + 12.0F, v41 + 7.0F, 6.5F, up.getColor(235, (int)(240.0F * alpha)));
            float v53 = 20.0F;
            float v55 = 11.0F;
            float v57 = lx + lw - 12.0F - v53 - 6.0F;
            float v59 = v41 + (v50 - v55) / 2.0F;
            int v61 = v45 ? up.multAlpha(v11, alpha) : up.getColor(255, (int)(35.0F * alpha));
            vs.ys.rect(v57, v59, v53, v55, v61, v55 / 2.0F);
            if (v45) {
               vs.ys.glow(v57, v59, v53, v55, up.multAlpha(v11, 0.35F * alpha), v55 / 2.0F, 6.0F, 1.0F);
            }

            float v63 = (v55 - 2.5F) / 2.0F;
            float v65 = v45 ? v57 + v53 - v63 * 2.0F - 1.25F : v57 + 1.25F;
            vs.ys.rect(v65, v59 + 1.25F, v63 * 2.0F, v63 * 2.0F, up.getColor(255, (int)(255.0F * alpha)), v63);
            v41 += v50 + 7.0F;
         }
      }
   }

   public static no getInstance() {

      return INSTANCE;
   }

   private void m1457(float sx, float sy, float sw, float mouseX, float mouseY, float alpha, Theme theme) {
      yt v8 = hu.sf_bold;
      yt v9 = hu.sf_regular;
      int v10 = theme.get(0);
      int v11 = theme.get(3);
      hu.icon.draw("S", sx + 12.0F, sy + 11.0F, 10.0F, up.multAlpha(v10, alpha));
      v8.draw("THEME STUDIO", sx + 28.0F, sy + 10.0F, 9.5F, up.multAlpha(v11, alpha));
      v9.draw(
         "Настройка кастомного стиля",
         sx + 105.0F,
         sy + 11.5F,
         6.2F,
         up.getColor(150, 150, 165, (int)(200.0F * alpha))
      );
      float v12 = sx + 225.0F;
      float v13 = sy + 9.0F;
      float v14 = 13.0F;
      float v15 = 4.0F;

      for (int v16 = 0; v16 < QUICK_PALETTES.length; v16++) {
         int v17 = QUICK_PALETTES[v16];

         float v18 = v12 + (float)v16 * (v14 + v15);
         boolean v19 = fq.isHovered(mouseX, mouseY, v18, v13, v14, v14);
         vs.ys.rect(v18, v13, v14, v14, up.multAlpha(v17, alpha), 3.5F);
         float temp4 = v19 ? 0.9F : 0.4F;
         int temp5 = v19 ? up.getColor(255, (int)(255.0F * alpha)) : up.getColor(255, (int)(50.0F * alpha));

         vs.ys.outline(v18, v13, v14, v14, temp4, temp5, 3.5F);
         if (v19) {
            vs.ys.glow(v18, v13, v14, v14, up.multAlpha(v17, 0.45F * alpha), 3.5F, 6.0F, 1.0F);
         }
      }

      float v23 = 38.0F;
      float v24 = 14.0F;
      float v25 = sx + sw - v23 - 10.0F;
      float v26 = sy + 9.0F;
      boolean v20 = fq.isHovered(mouseX, mouseY, v25, v26, v23, v24);
      vs.ys.rect(
         v25,
         v26,
         v23,
         v24,
         v20
            ? up.getColor(255, 60, 60, (int)(80.0F * alpha))
            : up.getColor(255, 255, 255, (int)(15.0F * alpha)),
         4.0F
      );
      v8.drawCentered(
         "ESC [X]",
         v25 + v23 / 2.0F,
         v26 + 3.5F,
         6.0F,
         v20
            ? up.getColor(255, 120, 120, (int)(255.0F * alpha))
            : up.getColor(200, (int)(220.0F * alpha))
      );
      vs.ys.rect(
         sx + 8.0F,
         sy + 30.0F,
         sw - 16.0F,
         0.5F,
         up.getColor(255, 255, 255, (int)(20.0F * alpha)),
         0.25F
      );
   }

   private void m1464(float mouseY, float py, float ph, Theme theme, int idx) {
      int v6 = theme.get(idx);
      int result = v6 >> 16 & 255;

      float[] v7 = Color.RGBtoHSB(result, v6 >> 8 & 255, v6 & 255, null);
      result = v6 >> 24;

      int v8 = result & 255;
      float v9 = MathHelper.method_15363((mouseY - py) / ph, 0.0F, 1.0F);
      int v10 = Color.HSBtoRGB(v9, v7[1], v7[2]);
      theme.set(idx, v8 << 24 | v10 & 16777215);
   }

   @Override
   public void saveAndExit() {
      if (this.active) {
         Theme v1 = xo.get().getCustomTheme();
         xo.get().saveTheme(v1);
         if (ko.selectedTheme == ru.white.manager.Theme.CUSTOM) {
            xo.get().setActive(v1);
         }

         this.active = false;
         this.f1208 = 0;
         this.f1209 = -1;
      }
   }

   static {
      int[] result = new int[8];
      result[0] = Theme.rgb(246, 124, 180);
      result[1] = Theme.rgb(139, 162, 255);
      result[2] = Theme.rgb(151, 255, 139);
      result[3] = Theme.rgb(255, 100, 100);
      result[4] = Theme.rgb(216, 68, 234);
      result[5] = Theme.rgb(96, 255, 198);
      result[6] = Theme.rgb(255, 190, 60);
      result[7] = Theme.rgb(255, 255, 255);
      QUICK_PALETTES = result;
   }

   private void m1463(float mouseX, float mouseY, float px, float py, float pw, float ph, Theme theme, 

      int v9 = theme.get(idx);
      float[] v10 = Color.RGBtoHSB(
         v9 >> 16 & 255,
         v9 >> 8 & 255,
         v9 & 255,
         null
      );
      int v11 = v9 >> 24 & 255;
      float v12 = MathHelper.method_15363((mouseX - px) / pw, 0.0F, 1.0F);
      float result = 1.0F - (mouseY - py) / ph;

      float v13 = MathHelper.method_15363(result, 0.0F, 1.0F);
      int v14 = Color.HSBtoRGB(v10[0], v12, v13);
      theme.set(idx, v11 << 24 | v14 & 16777215);
   }

   private void m1466(Theme theme) {
      int temp2 = 246;
      int temp3 = 124;

      theme.colors[0] = Theme.rgb(temp2, temp3, 180);
      theme.colors[5] = Theme.rgb(246, 124, 180);
      theme.colors[1] = Theme.rgb(15, 15, 18);
      theme.colors[7] = up.getColor(255, 255, 255, 10);
      theme.colors[2] = up.getColor(255, 255, 255, 15);
      int temp1 = 6;

      theme.colors[temp1] = up.getColor(255, 255, 255, 15);
      theme.colors[3] = Theme.rgb(255, 255, 255);
      int[] result = theme.colors;
      temp2 = Theme.rgb(177, 177, 177);

      result[4] = temp2;
      theme.setSlider(0, 0.9F);
      theme.setSlider(1, 4.0F);
      theme.setBool(0, true);
      theme.setBool(1, false);
   }

   private void m1456() {
      long result = System.currentTimeMillis();

      long v1 = result;
      if (v1 - this.f1215 >= -8816413769532980181L + 8816413769532981681L) {
         this.f1215 = v1;
         CompletableFuture.runAsync(
            () -> {
               try {
                  List v1x = MediaPlayerInfo.Instance.getMediaSessions();
                  if (v1x != null) {
                     IMediaSession v2 = v1x.stream().filter(s -> {
                        if (s.getMedia() != null) {
                           boolean var10000xx = s.getMedia().getTitle().isEmpty();

                           if (!var10000xx || !s.getMedia().getArtist().isEmpty()) {
                              return true;
                           }
                        }

                        return false;
                     }).max(Comparator.comparingInt(s -> {
                        boolean var10000xx = s.getMedia().getPlaying();

                        return var10000xx ? 1 : 0;
                     })).orElse(null);
                     if (v2 != null) {
                        MediaInfo v3x = v2.getMedia();
                        this.f1212 = v3x.getTitle().isEmpty() ? "Нет трека" : v3x.getTitle();
                        this.f1213 = v3x.getArtist().isEmpty()
                           ? "Музыка не играет"
                           : v3x.getArtist();
                        this.f1214 = ps.hasArtwork();
                     } else {
                        this.f1212 = "Нет трека";

                        this.f1213 = "Музыка не играет";
                        this.f1214 = false;
                        
                     }
                  }
               } catch (Throwable v6x) {
               }
            }
         );
      }
   }

   
}
