package ru.white.notification;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Uf;
import ru.white.module.setting.ModeSetting;
import ru.white.ui.DraggableWidget;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.render.dt;
import ru.white.utils.render.vs;
import ru.white.module.setting.Setting;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class Notification {
   private SliderSetting f1244;
   private to f1232;
   private int f1247;
   private to f1245;
   private Module f1230;
   private tf f1246;
   private String title = "";
   private float y;
   private final List<uo.vo> f1235;
   private float f1239;
   private float f1238;
   private float f1242;
   private float x;
   private to f1248;
   private boolean f1241;
   private to f1233;
   private boolean f1240;
   private boolean f1231;
   private to f1234;
   private float f1243;
   private float f1237;
   private float f1236;

   public boolean handleScroll(float mouseX, float mouseY, double verticalAmount) {
      if (this.f1230 != null) {
         boolean result = this.f1232.contains(mouseX, mouseY);

         if (result) {
            float v8 = (float)((double)this.f1237 - verticalAmount * 18.0);
            int temp4 = v6 * (v6 + 1) % 2;

            if (temp4 != 0) {
               }

            this.f1237 = fq.clamp(v8, 0.0F, this.f1238);
            return true;
         }
      }

      return false;
   }

   public void draw(qo widgets, float alpha) {
      if (this.f1230 != null && !(this.f1232.width() <= 0.0F)) {
         to v3 = this.f1232;
         vs.ys.glow(v3.x(), v3.y(), v3.width(), v3.height() - 0.5F, up.getColor(0, 0.15F * alpha), 8.0F, 15.0F, 1.0F);
         vs.ws.blur(v3.x(), v3.y(), v3.width(), v3.height(), alpha, 8.0F, up.multAlpha(up.multDark(up.background(), 0.6F), alpha));
         vs.ys.outline(v3.x(), v3.y(), v3.width(), v3.height(), 0.5F, up.replAlpha(up.client(), alpha * 0.35F), 8.0F);
         to v4 = this.f1233;
         String v7 = this.f1230.getName();
         widgets.drawRowBackground(v4, widgets.rowHover(v7 + ":header", v4), alpha);

         hu.category.draw("U", v4.x() + 6.0F, v4.y() + 5.2F, 6.5F, up.multAlpha(up.client(), alpha));
         yt result = hu.sf_regular;
         String v12 = this.title;
         float temp2 = v4.x() + 17.0F;
         float v15 = v4.y() + 4.4F;

         result.draw(v12, temp2, v15, 6.5F, up.getColor(200, alpha));
         dt.enable(v3.x(), v3.y() + 5.0F + 16.0F + 3.0F, v3.width(), v3.height() - 24.0F - 5.0F, 2.0F);
         if (this.f1240) {
            to v13 = this.f1234;
            String v14 = bb.tr("Вкл/Выкл");
            String v8 = this.f1230.getName();
            widgets.drawToggleRow(v13, v14, this.f1230.isEnabled(), v8 + ":enabled", alpha);
         }

         for (uo.vo v6 : this.f1235) {
            this.m1474(widgets, v6, alpha);
         }

         dt.disable();
      }
   }

   public void unplace() {
      this.f1231 = false;
      
   }

   private void m1474(qo widgets, uo.vo row, float parentAlpha) {
      String result = this.f1230.getName();
      String v35 = row.f1226;
      String v36 = result;
      String v4 = v36 + ":" + v35;
      Setting v5 = row.f1225;
      to v6 = row.f1228;
      float v7 = parentAlpha * row.f1229;
      if (!(v7 <= 0.01F)) {
         if (v5 instanceof uf) {
            yt v53 = hu.sf_regular;
            String v57 = bb.tr(row.f1226);
            float v61 = v53.getWidth(v57, 5.5F);
            v53.draw(v57, v6.x() + 2.0F, v6.y() + 4.0F, 5.5F, up.replAlpha(up.client(), v7 * 0.85F));
            vs.ys.rect(
               v6.x() + v61 + 6.0F,
               v6.y() + 5.75F,
               Math.max(0.0F, v6.width() - v61 - 8.0F),
               0.5F,
               up.getColor(255, v7 * 0.12F),
               0.25F
            );
         } else if (v5 instanceof BooleanSetting v52) {
            widgets.drawToggleRow(v6, bb.tr(row.f1226), v52.getValue(), v4, v7);
         } else if (v5 instanceof SliderSetting v51) {
            float v56 = widgets.rowHover(v4, v6);
            widgets.drawRowBackground(v6, v56, v7);
            yt v60 = hu.sf_regular;
            v60.draw(bb.tr(row.f1226), v6.x() + 6.0F, v6.y() + 3.2F, 6.0F, up.getColor(255, v7 * (0.5F + 0.5F * v56)));
            String v64 = String.valueOf((double)Math.round((double)v51.getValue().floatValue() * 100.0) / 100.0);
            v60.draw(
               v64,
               v6.x() + v6.width() - 6.0F - v60.getWidth(v64, 5.5F),
               v6.y() + 3.5F,
               5.5F,
               up.replAlpha(up.client(), v7 * (0.6F + 0.4F * v56))
            );
            float v66 = v6.width() - 12.0F;
            float v68 = MathHelper.method_15363((v51.getValue() - v51.min) / (v51.max - v51.min), 0.0F, 1.0F);
            float v70 = widgets.smooth(v4 + ":EventSync", v68);
            vs.ys.rect(v6.x() + 6.0F, v6.y() + 13.0F, v66, 2.5F, up.getColor(0, v7 * 0.15F), 1.25F);
            vs.ys.rect(v6.x() + 6.0F, v6.y() + 13.0F, v66 * v70, 2.5F, up.replAlpha(up.client(), v7 * (0.5F + 0.5F * v56)), 1.25F);
            vs.ys.rect(v6.x() + 6.0F + v66 * v70 - 2.5F, v6.y() + 11.75F, 5.0F, 5.0F, up.getColor((int)(200.0F + 55.0F * v56), v7), 5.0F);
            vs.ys.rect(v6.x() + 6.0F + v66 * v70 - 1.75F, v6.y() + 12.5F, 3.5F, 3.5F, up.replAlpha(up.client(), v7 * (0.7F + 0.3F * v56)), 5.0F);
         } else if (v5 instanceof ModeSetting v50) {
            float v55 = widgets.rowHover(v4, v6);
            widgets.drawRowBackground(v6, v55, v7);
            yt v59 = hu.sf_regular;
            String v63 = bb.tr(v50.getValue());
            v59.drawFadingText(
               bb.tr(row.f1226),
               v6.x() + 6.0F,
               v6.y() + 3.2F,
               v6.width() - v59.getWidth(v63, 5.5F) - 16.0F,
               up.getColor(255, v7 * (0.5F + 0.5F * v55)),
               6.0F
            );
            float temp2 = v6.x() + v6.width() - 6.0F;
            float temp3 = v59.getWidth(v63, 5.5F);

            v59.draw(v63, temp2 - temp3, v6.y() + 3.5F, 5.5F, up.replAlpha(up.client(), v7 * (0.6F + 0.4F * v55)));

            for (int v65 = 0; v65 < row.f1227.size(); v65++) {
               to v67 = row.f1227.get(v65);
               String v69 = v50.values.get(v65);
               Animation v71 = widgets.anim(v4 + ":chip:" + v69);
               cp temp1;
               if (v69.equals(v50.getValue())) {
                  temp1 = cp.FORWARDS;
                  
               } else {
                  temp1 = cp.BACKWARDS;
               }

               v71.setDirection(temp1);
               float v72 = v71.getOutput();
               float v77 = v67.x();
               float v78 = v67.y();
               temp2 = v67.width();
               temp3 = v67.height();

               vs.ys.rect(
                  v77,
                  v78,
                  temp2,
                  temp3,
                  up.overCol(up.getColor(0, 0.25F * v7), up.replAlpha(up.client(), v7 * (0.5F + 0.5F * v55)), v72),
                  2.5F
               );
               v59.drawCentered(
                  bb.tr(v69),
                  v67.centerX(),
                  v67.y() + 1.4F,
                  5.5F,
                  up.getColor(255, v7 * (0.35F + 0.65F * v72) * (0.7F + 0.3F * v55))
               );
            }
         } else if (!(v5 instanceof tf v8)) {
            if (v5 instanceof sf v49) {
               float v54 = widgets.rowHover(v4, v6);
               v49.pressAnim.update();
               float v58 = v49.pressAnim.get();
               float v62 = Math.max(v54, v58);
               vs.ys.glow(v6.x(), v6.y(), v6.width(), v6.height(), up.getColor(0, 0.06F * v7), 5.0F, 7.0F, 1.0F);
               vs.ys.rect(
                  v6.x(),
                  v6.y(),
                  v6.width(),
                  v6.height(),
                  up.overCol(up.getColor(40, 0.15F * v7), up.replAlpha(up.client(), 0.25F * v7), v62),
                  5.0F
               );
               vs.ys.outline(v6.x(), v6.y(), v6.width(), v6.height(), 0.5F, up.replAlpha(up.client(), v7 * v62), 4.0F);
               hu.sf_regular
                  .drawCentered(bb.tr(row.f1226), v6.centerX(), v6.y() + 3.2F, 6.0F, up.getColor(255, v7 * (0.5F + 0.5F * v54)));
            }
         } else {
            float v9 = widgets.rowHover(v4, v6);
            widgets.drawRowBackground(v6, v9, v7);
            yt v10 = hu.sf_regular;
            v10.drawFadingText(
               bb.tr(row.f1226),
               v6.x() + 6.0F,
               v6.y() + 3.2F,
               v6.width() - 30.0F,
               up.getColor(255, v7 * (0.5F + 0.5F * v9)),
               6.0F
            );
            int v11 = v8.getValue();
            int v12 = (int)widgets.smooth(v4 + ":r", (float)(v11 >> 16 & 255));
            int v13 = (int)widgets.smooth(v4 + ":g", (float)(v11 >> 8 & 255));
            int v14 = (int)widgets.smooth(v4 + ":b", (float)(v11 & 255));
            float v15 = v6.x() + v6.width() - 6.0F - 7.0F;
            float v16 = v6.y() + 3.0F;
            vs.ys.rect(v15, v16, 7.0F, 7.0F, up.getColor(v12, v13, v14, v7), 7.0F);
            vs.ys.outline(v15 - 0.75F, v16 - 0.75F, 8.5F, 8.5F, 0.25F, up.getColor(v12, v13, v14, v7 * (0.4F + 0.6F * v9)), 7.0F);
            vs.ys.glow(v15, v16, 7.0F, 7.0F, up.getColor(v12, v13, v14, 0.1F * v7), 7.0F, 6.0F, 1.0F);
            if (v8.pickerOpen) {
               float[] v17 = Color.RGBtoHSB(
                  v11 >> 16 & 255,
                  v11 >> 8 & 255,
                  v11 & 255,
                  null
               );
               float v18 = widgets.smooth(v4 + ":h", v17[0]);
               float v19 = widgets.smooth(v4 + ":s", v17[1]);
               float v20 = widgets.smooth(v4 + ":v", v17[2]);
               float[] v21 = new float[]{v18, v19, v20};
               float v22 = v6.x() + 6.0F;
               float v23 = v6.width() - 12.0F;
               float v25 = v23 / (float)v24;

               for (int v26 = 0; v26 < 3; v26++) {
                  float v27 = v6.y() + 13.0F + 2.0F + (float)v26 * 10.0F;

                  for (int v28 = 0; v28 < v24; v28++) {
                     float v29 = (float)v28 / (float)v24;
                     float v30 = (float)(v28 + 1) / (float)v24;

                     int v31 = switch (v26) {
                        case 0 -> Color.HSBtoRGB(v29, 1.0F, 1.0F);
                        case 1 -> Color.HSBtoRGB(v18, v29, v20);
                        default -> Color.HSBtoRGB(v18, v19, v29);
                     };

                     int v32 = switch (v26) {
                        case 0 -> Color.HSBtoRGB(v30, 1.0F, 1.0F);
                        case 1 -> Color.HSBtoRGB(v18, v30, v20);
                        default -> Color.HSBtoRGB(v18, v19, v30);
                     };
                     int v33 = up.replAlpha(v31, v7);
                     int v34 = up.replAlpha(v32, v7);
                     vs.ys.gradientRect(
                        v22 + (float)v28 * v25,
                        v27,
                        v25 + (v28 == v24 - 1 ? 0.0F : 0.5F),
                        3.5F,
                        new int[]{v33, v34, v34, v33},
                        v28 == 0 ? 2.0F : 0.0F,
                        v28 == v24 - 1 ? 2.0F : 0.0F,
                        v28 == v24 - 1 ? 2.0F : 0.0F,
                        v28 == 0 ? 2.0F : 0.0F
                     );
                  }

                  float v73 = v22 + v23 * v21[v26];
                  int v74 = v26 == 0 ? Color.HSBtoRGB(v18, 1.0F, 1.0F) : Color.HSBtoRGB(v18, v19, v20);
                  vs.ys.rect(v73 - 2.5F, v27 - 0.75F, 5.0F, 5.0F, up.getColor(255, v7), 5.0F);
                  vs.ys.rect(v73 - 1.75F, v27, 3.5F, 3.5F, up.replAlpha(v74, v7), 5.0F);
               }
            }
         }
      }
   }

   public to bounds() {

      return this.f1232;
   }

   public boolean isDragging() {

      if (!this.f1241 && this.f1244 == null) {

         if (this.f1246 == null) {
            return false;
         }
      }

      return true;
   }

   public void layout(Module module, String title, float defaultX, float defaultY, float screenW, float screenH, qo widgets) {
      this.f1236 = this.f1236 + (this.f1237 - this.f1236) * 0.2F;
      this.f1235.clear();
      this.f1230 = module;
      this.title = title;
      if (module == null) {
         this.f1232 = to.EMPTY;
         this.f1234 = to.EMPTY;
         this.f1233 = to.EMPTY;
      } else {
         if (!this.f1231) {
            this.x = defaultX;
            this.y = defaultY;
            this.f1231 = true;
         }

         this.x = fq.clamp(this.x, 4.0F, Math.max(4.0F, screenW - 168.0F - 4.0F));
         this.y = fq.clamp(this.y, 4.0F, Math.max(4.0F, screenH - 16.0F - 30.0F));
         float v8 = this.x + 6.0F;
         float v9 = 156.0F;
         float v10 = this.y + 5.0F + 16.0F + 3.0F;
         this.f1233 = new to(v8, this.y + 5.0F, v9, 16.0F);
         float v11 = screenH - this.f1239;
         float v12 = Math.max(60.0F, v11 - v10);
         float v13 = v10 - this.f1236;
         if (this.f1240) {
            this.f1234 = new to(v8, v13, v9, 13.0F);
            v13 += 16.0F;
         } else {
            this.f1234 = to.EMPTY;
         }

         for (Setting v15 : module.getSettings()) {
            boolean v16 = v15 instanceof BooleanSetting
               || v15 instanceof SliderSetting
               || v15 instanceof ModeSetting
               || v15 instanceof tf
               || v15 instanceof sf
               || v15 instanceof uf;
            if (v16) {
               String temp1 = module.getName();
               String v23 = v15.getName();
               String v24 = temp1;
               Animation v17 = widgets.anim(v24 + ":" + v23 + ":vis");
               v17.setDirection(v15.getVisible().get() ? cp.FORWARDS : cp.BACKWARDS);
               float v18 = v17.getOutput();
               if (!(v18 <= 0.01F)) {
                  uo.vo v19 = new uo.vo(v15, v15.getName());
                  v19.f1229 = v18;
                  float v20 = 13.0F;
                  if (v15 instanceof SliderSetting) {
                     v20 = 20.0F;
                  } else if (v15 instanceof uf) {
                     v20 = 11.0F;
                  } else if (v15 instanceof ModeSetting v21) {
                     v20 = 15.0F + this.m1473(v21.values, v9 - 12.0F) + 2.0F;
                  } else if (v15 instanceof tf) {
                     tf v22 = (tf)v15;
                     if (v22.pickerOpen) {
                        v20 = 40.0F;
                     }
                  }

                  v19.f1228 = new to(v8, v13, v9, v20);
                  if (v15 instanceof ModeSetting v28) {
                     this.m1472(v19, v28, v8 + 6.0F, v13 + 13.0F + 2.0F, v9 - 12.0F);
                  }

                  this.f1235.add(v19);
                  v13 += (v20 + 3.0F) * v18;
               }
            }
         }

         float v26 = v13 + this.f1236 - v10;
         this.f1238 = Math.max(0.0F, v26 - v12);
         this.f1237 = fq.clamp(this.f1237, 0.0F, this.f1238);
         float v27 = 24.0F + Math.min(v26, v12) + 5.0F;
         this.f1232 = new to(this.x, this.y, 168.0F, v27);
      }
   }

   private void m1472(uo.vo row, ModeSetting mode, float chipX, float chipY, float width) {
      row.f1227.clear();
      yt v6 = hu.sf_regular;
      float v7 = 0.0F;
      float v8 = 0.0F;

      for (String v10 : mode.values) {
         float v11 = v6.getWidth(bb.tr(v10), 5.5F) + 7.0F;
         if (v7 + v11 > width && v7 > 0.0F) {
            v7 = 0.0F;
            v8 += 10.5F;
         }

         List result = row.f1227;
         to temp1 = new to(chipX + v7, chipY + v8, v11, 9.0F);

         result.add(temp1);
         v7 += v11 + 2.5F;
      }
   }

   public boolean rawMouseMoved(float mouseX, float mouseY) {

      if (this.f1241) {
         this.x = mouseX - this.f1242;
         this.y = mouseY - this.f1243;
         return true;
      } else if (this.f1244 != null) {
         this.m1475(this.f1244, this.f1245, mouseX);
         return true;
      } else if (this.f1246 != null) {
         this.m1476(mouseX);
         return true;
      } else {
         return false;
      }
   }

   public void setBottomInset(float bottomInset) {

      int temp2 = v2 * (v2 + 1) % 2;

      if (temp2 != 0) {
         }

      this.f1239 = bottomInset;
   }

   private float m1473(List<String> values, float width) {
      yt v3 = hu.sf_regular;
      float v4 = 0.0F;
      float v5 = 0.0F;

      for (String v7 : values) {
         float v8 = v3.getWidth(bb.tr(v7), 5.5F) + 7.0F;
         if (v4 + v8 > width) {
            float v15;
            int v12 = (v15 = v4 - 0.0F) == 0.0F ? 0 : (v15 < 0.0F ? -1 : 1);

            if (v12 > 0) {
               v4 = 0.0F;
               v5 += 10.5F;
            }
         }

         float v13 = v4 + v8 + 2.5F;

         v4 = v13;
      }

      return v5 + 9.0F;
   }

   private void m1475(SliderSetting setting, to sliderBounds, float mouseX) {

      float v4 = sliderBounds.x() + 6.0F;
      float v10 = sliderBounds.width() - 12.0F;

      float v5 = Math.max(1.0F, v10);
      float result = mouseX - v4;

      float v6 = MathHelper.method_15363(result / v5, 0.0F, 1.0F);
      setting.set(MathHelper.method_15363(fq.round(setting.min + (setting.max - setting.min) * v6, setting.increment), setting.min, setting.max));
   }

   public Module module() {

      return this.f1230;
   }

   public Notification() {
      this.f1232 = to.EMPTY;
      this.f1233 = to.EMPTY;
      this.f1234 = to.EMPTY;
      this.f1235 = new ArrayList<>();
      this.f1239 = 82.0F;
      this.f1240 = true;
      this.f1245 = to.EMPTY;
      this.f1248 = to.EMPTY;
   }

   public void setShowEnableRow(boolean showEnableRow) {

      this.f1240 = showEnableRow;
   }

   private void m1476(float mouseX) {
      if (this.f1246 != null) {
         int v2 = this.f1246.getValue();
         int result = v2 >> 16;
         int temp1 = 255;

         float[] v3 = Color.RGBtoHSB(result & temp1, v2 >> 8 & 255, v2 & 255, null);
         float v9 = mouseX - (this.f1248.x() + 6.0F);

         float v4 = fq.clamp(v9 / Math.max(1.0F, this.f1248.width() - 12.0F), 0.0F, 1.0F);
         float[] v5 = new float[]{v3[0], v3[1], v3[2]};
         v5[this.f1247] = v4;
         int v6 = Color.HSBtoRGB(v5[0], v5[1], v5[2]);
         this.f1246.set(v2 & -16777216 | v6 & 16777215);
      }
   }

   public void closePickers() {
      for (uo.vo v2 : this.f1235) {
         Setting v4 = v2.f1225;
         if (v4 instanceof tf) {
            tf v3 = (tf)v4;
            v3.pickerOpen = false;
            int result = v5 * (v5 + 1) % 2;

            if (result != 0) {
               }
         }
      }

      this.f1246 = null;
   }

   public boolean handleClick(float mouseX, float mouseY, int button) {
      if (this.f1230 != null && this.f1232.contains(mouseX, mouseY)) {
         if (this.f1233.contains(mouseX, mouseY)) {
            if (button == 0) {
               this.f1241 = true;
               this.f1242 = mouseX - this.x;
               this.f1243 = mouseY - this.y;
            }

            return true;
         } else if (this.f1240 && this.f1234.contains(mouseX, mouseY)) {
            if (button == 0) {
               this.f1230.setEnabled(!this.f1230.isEnabled(), false);
            }

            return true;
         } else {
            for (uo.vo v5 : this.f1235) {
               if (v5.f1228.contains(mouseX, mouseY)) {
                  Setting v6 = v5.f1225;
                  if (v6 instanceof BooleanSetting v7) {
                     if (button == 0) {
                        v7.set(!v7.getValue());
                     }
                  } else if (v6 instanceof SliderSetting v8) {
                     if (button == 0) {
                        this.f1244 = v8;
                        this.f1245 = v5.f1228;

                        this.m1475(v8, v5.f1228, mouseX);
                     }
                  } else if (v6 instanceof ModeSetting v9) {
                     if (button == 0 || button == 1) {
                        for (int v12 = 0; v12 < v5.f1227.size(); v12++) {
                           if (v5.f1227.get(v12).contains(mouseX, mouseY)) {
                              v9.set(v9.values.get(v12));
                              break;
                           }
                        }
                     }
                  } else if (v6 instanceof tf v10) {
                     if (button == 0) {
                        if (mouseY <= v5.f1228.y() + 13.0F) {
                           v10.pickerOpen = !v10.pickerOpen;
                        } else if (v10.pickerOpen) {
                           for (int v17 = 0; v17 < 3; v17++) {
                              float result = v5.f1228.y() + 13.0F;

                              float v13 = result + 2.0F + (float)v17 * 10.0F;
                              if (fq.isHovered(mouseX, mouseY, v5.f1228.x() + 6.0F, v13 - 2.5F, v5.f1228.width() - 12.0F, 9.0F)) {
                                 this.f1246 = v10;
                                 this.f1247 = v17;
                                 this.f1248 = v5.f1228;
                                 this.m1476(mouseX);
                              }
                           }
                        }
                     }
                  } else if (v6 instanceof sf v11 && button == 0) {
                     v11.press();
                  }

                  return true;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void reset() {
      this.f1236 = 0.0F;
      this.f1237 = 0.0F;
      this.releaseDrag();
      int temp2 = v2 + 1;

      this.closePickers();
   }

   public void releaseDrag() {
      this.f1241 = false;
      this.f1244 = null;

      this.f1246 = null;
   }

   @Environment(EnvType.CLIENT)
   private static final class vo {
      private final Setting<?> f1225;
      private float f1229;
      private to f1228;
      private final List<to> f1227 = new ArrayList<>();
      private final String f1226;

      private vo(Setting<?> setting, String label) {
         this.f1228 = to.EMPTY;
         this.f1229 = 1.0F;
         this.f1225 = setting;
         this.f1226 = label;
      }

      
   }
}
