package ru.white.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.notification.NotificationType;
import ru.white.notification.NotificationRenderer;
import ru.white.notification.INotification;
import ru.white.utils.misc.To;
import ru.white.notification.Notification;
import ru.white.module.setting.MatrixSetting;
import ru.white.ui.DraggableWidget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.CrossHair;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class GuiSettingElement implements ro, IMinecraft {
   private to f1094;
   private static final eo INSTANCE = new eo();
   private final oo f1084;
   private to f1095;
   private float f1093;
   private float f1092;
   private int f1089;
   private final qo f1081 = new qo();
   private final Animation f1086;
   private boolean active;
   private final oo f1085;
   private float f1091;
   private final uo f1082 = new uo();
   private boolean f1088;
   private final oo f1083 = new oo("Выйти и сохранить", true);
   private float f1090;
   private boolean f1087;

   private int[][] m1363(int gx, int gy) {
      if (this.f1087 && this.f1088) {
         int[][] result = new int[][]{{gx, gy}, null, null, null};
         int[] temp3 = new int[]{v3 - gx, gy};

         result[1] = temp3;
         result[2] = new int[]{gx, v3 - gy};

         temp3 = new int[]{v3 - gx, 0};

         temp3[1] = v3 - gy;
         result[3] = temp3;
         return result;
      } else if (this.f1087) {
         return new int[][]{{gx, gy}, {v3 - gx, gy}};
      } else {
         return this.f1088 ? new int[][]{{gx, gy}, {gx, v3 - gy}} : new int[][]{{gx, gy}};
      }
   }

   private boolean m1362(float mouseX, float mouseY) {
      if (mouseX >= this.f1090 && mouseY >= this.f1091) {

         if (mouseX < this.f1090 + this.f1092 && mouseY < this.f1091 + this.f1092) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {
      if (!this.active) {
         return false;
      } else {
         this.f1082.handleScroll(mouseX, mouseY, verticalAmount);

         return true;
      }
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {

      if (!this.active) {
         
      } else if (!this.f1082.rawMouseMoved(mouseX, mouseY)) {
         this.m1364(mouseX, mouseY);
      }
   }

   @Override
   public void saveAndExit() {

      if (this.active) {
         if (Client.get() != null && Client.get().configManager() != null) {
            Client.get().configManager().autoSave();
         }

         this.m1358();
      }
   }

   @Override
   public void closeFromMenuRemoval() {
      this.saveAndExit();
      
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {

      if (this.active) {
         xi v6 = xi.getInstance();
         if (v6 != null && v6.isDrawing()) {
            this.f1081.setMouse(mouseX, mouseY);
            this.m1359(width, height);
            this.f1083.update(mouseX, mouseY);
            this.f1084.update(mouseX, mouseY);
            this.f1085.update(mouseX, mouseY);
            this.f1082.layout(v6, "Cross Hair", width - 168.0F - 16.0F, 14.0F, width, height, this.f1081);
            this.f1086.update();
            float v7 = this.f1086.get() * parentAlpha;
            vs.ys.rect(0.0F, 0.0F, width, height, up.getColor(0, 0.55F * v7));
            Client.get().render2D().flushAll();
            hu.sf_regular
               .drawCentered(
                  bb.tr("Редактор прицела"),
                  width / 2.0F,
                  16.0F,
                  8.0F,
                  up.getColor(240, v7)
               );
            yt result = hu.sf_regular;
            String v14 = bb.tr("ЛКМ — рисовать");
            String temp2 = bb.tr("ПКМ — стирать");
            String v8 = bb.tr("Esc — выйти");
            String v9 = temp2;
            String v10 = v14;
            StringBuilder v15 = new StringBuilder();

            v14 = v15.append((Object)v10).append("   •   ").append((Object)v9).append("   •   ").append((Object)v8).toString();
            float v18 = width / 2.0F;
            int temp5 = 255;

            result.drawCentered(v14, v18, 29.0F, 6.0F, up.getColor(temp5, v7 * 0.45F));
            this.m1360(v6.grid, v7);
            this.f1081
               .drawToggleRow(
                  this.f1094,
                  bb.tr("Зеркало по X"),
                  this.f1087,
                  "crosshair:mirrorx",
                  v7
               );
            this.f1081
               .drawToggleRow(
                  this.f1095,
                  bb.tr("Зеркало по Y"),
                  this.f1088,
                  "crosshair:mirrory",
                  v7
               );
            this.f1081.drawButton(this.f1084, v7);
            this.f1081.drawButton(this.f1085, v7);
            this.f1081.drawButton(this.f1083, v7);
            this.f1082.draw(this.f1081, v7);
         } else {
            this.saveAndExit();
         }
      }
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {

         this.f1089 = -1;
         if (button == 0) {

            this.f1082.releaseDrag();
         }

         return true;
      }
   }

   private void m1358() {
      this.active = false;

      this.f1089 = -1;
      this.f1082.releaseDrag();
      this.f1082.closePickers();
   }

   private void m1359(float width, float height) {
      float v3 = Math.max(200.0F, width - 168.0F - 40.0F);
      float temp1 = Math.min(v3 * 0.75F, height - 150.0F);

      this.f1092 = fq.clamp(temp1, 110.0F, 340.0F);

      this.f1090 = v3 / 2.0F - this.f1092 / 2.0F;
      this.f1091 = 50.0F;
      this.f1093 = this.f1092 / 21.0F;
      float v4 = this.f1091 + this.f1092 + 8.0F;
      float v5 = this.f1092 / 2.0F - 3.0F;
      this.f1094 = new to(this.f1090, v4, v5, 13.0F);
      to v14 = new to;
      float v15 = this.f1090 + this.f1092 / 2.0F + 3.0F;

      v14./* $VF: Unable to resugar constructor */<init>(v15, v4, v5, 13.0F);
      this.f1095 = v14;
      float v6 = 18.0F;
      float v7 = 120.0F;
      float v8 = 88.0F;
      float v9 = height - 30.0F;
      this.f1083.set(this.f1090 + this.f1092 / 2.0F - v7 / 2.0F, v9, v7, v6);
      this.f1084.set(this.f1090 + this.f1092 / 2.0F - v8 - 4.0F, v9 - 24.0F, v8, v6);
      this.f1085.set(this.f1090 + this.f1092 / 2.0F + 4.0F, v9 - 24.0F, v8, v6);
   }

   private int m1361(float mouse, float origin) {
      if (this.f1093 <= 0.0F) {

         return -1;
      } else {
         float result = (mouse - origin) / this.f1093;

         return (int)Math.floor((double)result);
      }
   }

   private void m1364(float mouseX, float mouseY) {
      if (this.f1089 >= 0 && this.m1362(mouseX, mouseY)) {
         xi v3 = xi.getInstance();
         if (v3 != null) {
            int v4 = this.m1361(mouseX, this.f1090);
            int v5 = this.m1361(mouseY, this.f1091);
            if (v3.grid.inBounds(v4, v5)) {

               for (int[] v9 : this.m1363(v4, v5)) {
                  int temp2 = v10 + 1;

                  v3.grid.put(v9[0], v9[1], this.f1089 == 1);
               }
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if ((button == 0 || button == 1) && this.f1082.handleClick(mouseX, mouseY, button)) {
         return true;
      } else if (this.m1362(mouseX, mouseY)) {

         this.f1089 = button == 1 ? 0 : 1;
         this.m1364(mouseX, mouseY);
         return true;
      } else if (button != 0) {
         return true;
      } else {
         xi v4 = xi.getInstance();
         if (this.f1094.contains(mouseX, mouseY)) {
            this.f1087 = !this.f1087;
         } else if (this.f1095.contains(mouseX, mouseY)) {
            this.f1088 = !this.f1088;
         } else if (this.f1083.contains(mouseX, mouseY)) {

            this.f1083.press();
            this.saveAndExit();
         } else if (v4 != null && this.f1084.contains(mouseX, mouseY)) {
            this.f1084.press();
            v4.grid.clear();
         } else if (v4 != null && this.f1085.contains(mouseX, mouseY)) {
            this.f1085.press();
            v4.grid.reset();
         }

         return true;
      }
   }

   private GuiSettingElement() {
      this.f1084 = new oo("Очистить", false);
      this.f1085 = new oo("Сбросить", false);
      this.f1086 = new Animation();
      this.f1087 = true;
      this.f1088 = true;
      this.f1089 = -1;
      this.f1094 = to.EMPTY;
      this.f1095 = to.EMPTY;
   }

   public void open() {
      if (!this.active) {
         this.active = true;
         this.f1089 = -1;
         this.f1082.reset();
         this.f1082.unplace();
         this.f1086.set(0.0);
         this.f1086.run(1.0, 0.24F, ap.EXPO_OUT);
         
      }
   }

   public static eo getInstance() {
      return INSTANCE;
   }

   private void m1360(MatrixSetting grid, float alpha) {
      vs.ys.glow(this.f1090, this.f1091, this.f1092, this.f1092, up.getColor(0, 0.2F * alpha), 4.0F, 12.0F, 1.0F);
      vs.ys.rect(this.f1090, this.f1091, this.f1092, this.f1092, up.getColor(18, 0.85F * alpha), 4.0F);
      int v3 = grid.center();
      int v4 = up.replAlpha(up.client(), alpha);
      int v5 = up.getColor(255, 0.07F * alpha);
      int v6 = up.getColor(255, 0.18F * alpha);

      for (int v7 = 1; v7 < 21; v7++) {
         float v8 = (float)v7 * this.f1093;
         boolean v9 = v7 == v3 || v7 == v3 + 1;
         int v10 = v9 ? v6 : v5;
         vs.ys.rect(this.f1090 + v8, this.f1091, 0.5F, this.f1092, v10);
         vs.ys.rect(this.f1090, this.f1091 + v8, this.f1092, 0.5F, v10);
      }

      for (int v16 = 0; v16 < 21; v16++) {
         for (int v18 = 0; v18 < 21; v18++) {

            if (grid.get(v18, v16)) {
               int v20 = xi.getInstance() != null ? xi.getInstance().resolvePixelColor(v18, v16, alpha) : up.replAlpha(up.client(), alpha);
               vs.ys.rect(
                  this.f1090 + (float)v18 * this.f1093 + 0.5F,
                  this.f1091 + (float)v16 * this.f1093 + 0.5F,
                  this.f1093 - 1.0F,
                  this.f1093 - 1.0F,
                  v20,
                  1.0F
               );
            }
         }
      }

      int v17 = this.m1361(this.f1081.mouseX(), this.f1090);
      int v19 = this.m1361(this.f1081.mouseY(), this.f1091);
      if (grid.inBounds(v17, v19)) {
         int v21 = xi.getInstance() != null ? xi.getInstance().resolvePixelColor(v17, v19, alpha * 0.4F) : up.replAlpha(up.client(), alpha * 0.35F);
         int[][] v22 = this.m1363(v17, v19);
         int v11 = v22.length;

         while (v12 < v11) {
            int[] v13 = v22[v12];
            vs.ys.rect(
               this.f1090 + (float)v13[0] * this.f1093 + 0.5F,
               this.f1091 + (float)v13[1] * this.f1093 + 0.5F,
               this.f1093 - 1.0F,
               this.f1093 - 1.0F,
               v21,
               1.0F
            );
            v12++;
         }
      }

      vs.ys.outline(this.f1090, this.f1091, this.f1092, this.f1092, 0.5F, up.replAlpha(up.client(), alpha * 0.35F), 4.0F);
   }

   @Override
   public void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
      if (this.active) {
         if (!pressed) {
            this.f1089 = -1;
            if (button == 0) {
               this.f1082.releaseDrag();
            }
         } else {
            boolean result = this.m1362(mouseX, mouseY);

            if (result) {
               this.f1089 = button == 1 ? 0 : 1;
               this.m1364(mouseX, mouseY);
            }
         }
      }
   }

   
}
