package ru.white.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.white.notification.NotificationType;
import ru.white.notification.NotificationRenderer;
import ru.white.notification.INotification;
import ru.white.utils.misc.To;
import ru.white.notification.Notification;
import ru.white.module.setting.SliderSetting;
import ru.white.ui.DraggableWidget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.GlassHands;
import ru.white.module.impl.render.Hands;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.ds;
import ru.white.utils.render.vs;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class GuiCheckBox implements ro {
   private final oo f1104;
   private static final ItemStack LEFT_PREVIEW_STACK = new ItemStack(Items.field_8288);
   private final oo f1107;
   private static final float[][] HAND_SAMPLE_POINTS;
   private float f1116;
   private final uo f1121;
   private final oo f1106;
   private static final go INSTANCE = new go();
   private final go.ho f1099 = new go.ho();
   private float f1118;
   private Arm f1108;
   private float f1115;
   private static final long AUTO_SWING_INTERVAL_MS = 500L;
   private final go.ho f1100 = new go.ho();
   private int f1110;
   private to f1119;
   private final Animation f1101 = new Animation();
   private boolean f1111;
   private long f1112;
   private float f1113;
   private static final long BOUNDS_TTL_MS = 250L;
   private final Animation f1103;
   private float f1114;
   private Arm f1109;
   private boolean active;
   private final qo f1120;
   private final Animation f1102 = new Animation();
   private float f1117;
   private static final ItemStack RIGHT_PREVIEW_STACK = new ItemStack(Items.field_22022);
   private final uo f1122;
   private final oo f1105;

   private void m1366() {
      GlassHands v1 = GlassHands.getInstance();
      if (v1 != null) {
         v1.tintColor.pickerOpen = false;
      }

      this.f1121.closePickers();
      this.f1122.closePickers();
   }

   private void m1370(float mouseX, float mouseY) {
      if (this.active && this.f1109 != null) {
         oj v3 = oj.get();
         if (v3 != null) {
            float v4 = 1.0F / this.m1376();
            float temp1 = mouseX - this.f1113;

            float v5 = this.f1115 + temp1 * v4;
            float v6 = this.f1116 - (mouseY - this.f1114) * v4;
            SliderSetting v7 = this.f1109 == Arm.field_6183 ? v3.rx : v3.lx;
            SliderSetting v8 = this.f1109 == Arm.field_6183 ? v3.ry : v3.ly;
            v7.set(MathHelper.method_15363(v5, v7.min, v7.max));
            v8.set(MathHelper.method_15363(v6, v8.min, v8.max));
         }
      }
   }

   public static go getInstance() {
      return INSTANCE;
   }

   static {
      float[][] result = new float[8][];
      result[0] = new float[]{-0.55F, -0.55F, -0.35F};
      result[1] = new float[]{-0.55F, -0.55F, 0.35F};
      result[2] = new float[]{-0.55F, 0.55F, -0.35F};
      result[3] = new float[]{-0.55F, 0.55F, 0.35F};
      result[4] = new float[]{0.55F, -0.55F, -0.35F};
      result[5] = new float[]{0.55F, -0.55F, 0.35F};
      result[6] = new float[]{0.55F, 0.55F, -0.35F};
      result[7] = new float[]{0.55F, 0.55F, 0.35F};
      HAND_SAMPLE_POINTS = result;
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active) {
         this.f1117 = width;
         this.f1118 = height;
         this.f1120.setMouse(mouseX, mouseY);
         this.m1368(width, height);
         this.f1121.layout(xk.get(), "Swing Animation", width - 168.0F - 16.0F, 14.0F, width, height, this.f1120);
         this.f1122.layout(GlassHands.getInstance(), "GlassHands", 16.0F, 14.0F, width, height, this.f1120);
         this.m1373();
         this.f1101.update();
         float v6 = this.f1101.get() * parentAlpha;
         this.f1108 = this.f1109 != null ? this.f1109 : this.m1374(mouseX, mouseY);
         this.m1378(this.f1102, this.f1108 == Arm.field_6182);
         this.m1378(this.f1103, this.f1108 == Arm.field_6183);
         this.f1104.update(mouseX, mouseY);
         this.f1105.update(mouseX, mouseY);
         this.f1106.update(mouseX, mouseY);
         this.f1107.update(mouseX, mouseY);
         vs.ys.rect(0.0F, 0.0F, width, height, up.getColor(0, 0.3F * v6));
         Client.get().render2D().flushAll();
         ds.getInstance().restoreHandsAfterOverlay();
         this.m1367(width, v6);
         this.f1120.drawButton(this.f1106, v6);
         this.f1120.drawButton(this.f1107, v6);
         this.f1120.drawButton(this.f1105, v6);
         this.f1120.drawButton(this.f1104, v6);

         this.f1120
            .drawToggleRow(
               this.f1119,
               bb.tr("Авто-взмах правой руки"),
               this.f1111,
               "editor:autoswing",
               v6
            );
         this.f1121.draw(this.f1120, v6);

         this.f1122.draw(this.f1120, v6);
      }
   }

   public void open() {
      if (!this.active) {
         this.active = true;
         this.f1108 = null;
         this.f1109 = null;
         this.f1110 = -1;
         this.f1111 = false;
         this.f1112 = 0L;
         this.f1121.reset();
         this.f1122.reset();
         this.m1366();
         this.f1099.invalidate();

         this.f1100.invalidate();

         this.f1101.set(0.0);
         this.f1101.run(1.0, 0.24F, ap.EXPO_OUT);
         oj v1 = oj.get();
         if (v1 != null) {
            v1.setEditorActive(true);
         }
      }
   }

   public void captureAfterHands() {
      if (!this.active) {
         
      } else {
         ds v1 = ds.getInstance();
         v1.captureSceneAfterHands();
         v1.renderGlassEffect();
      }
   }

   public boolean isAutoSwingPreview() {
      if (this.active) {
         int temp1 = v1 + 1;

         if (this.f1111) {
            return true;
         }
      }

      return false;
   }

   private float m1376() {

      return Math.max(1.0F, this.f1118 / 1.05F);
   }

   @Override
   public void saveAndExit() {
      if (this.active) {
         if (Client.get() != null && Client.get().configManager() != null) {

            Client.get().configManager().autoSave();
         }

         this.m1365();
      }
   }

   public ItemStack previewStackFor(Arm arm) {
      ItemStack result;
      if (arm == Arm.field_6183) {
         result = RIGHT_PREVIEW_STACK;
         
      } else {
         result = LEFT_PREVIEW_STACK;
      }

      return result;
   }

   @Override
   public void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
      if (this.active) {
         if (button == 0 || button == 1) {
            if (!pressed) {
               if (button == 0) {
                  int result = v6 * (v6 + 1);

                  this.f1121.releaseDrag();
                  this.f1122.releaseDrag();
               }

               if (button == this.f1110) {

                  this.f1109 = null;
                  this.f1110 = -1;
               }
            } else if (!this.m1377(mouseX, mouseY)) {
               Arm v5 = this.m1371(mouseX, mouseY);
               if (v5 != null) {
                  this.m1369(v5, mouseX, mouseY, button);
               }
            }
         }
      }
   }

   @Override
   public void closeFromMenuRemoval() {
      if (this.active) {
         Client result = Client.get();

         if (result != null) {

            if (Client.get().configManager() != null) {
               Client.get().configManager().autoSave();
            }
         }

         this.m1365();
      }
   }

   private void m1368(float width, float height) {
      float v3 = 18.0F;
      float v4 = 100.0F;
      float v5 = 120.0F;
      float v6 = 112.0F;
      float v7 = height - 30.0F;
      this.f1104.set(width / 2.0F - v5 / 2.0F, v7, v5, v3);
      float temp1 = width / 2.0F;

      this.f1105.set(temp1 - v6 / 2.0F, v7 - 25.0F, v6, v3);
      this.f1106.set(20.0F, v7, v4, v3);
      this.f1107.set(width - 20.0F - v4, v7, v4, v3);
      to v10 = new to(width / 2.0F - 80.0F, v7 - 52.0F, 160.0F, 13.0F);

      this.f1119 = v10;
   }

   public void updateHandBounds(Arm arm, MatrixStack matrices) {
      if (this.active && matrices != null) {
         MinecraftClient v3 = MinecraftClient.method_1551();
         if (v3 != null && v3.field_1773 != null && v3.method_22683() != null) {
            Matrix4f v4 = new Matrix4f(matrices.method_23760().method_23761());
            Matrix4f v5 = new Matrix4f(RenderSystem.getModelViewMatrix());
            Matrix4f v6 = v3.field_1773.method_22973(70.0F);
            float v7 = (float)v3.method_22683().method_4489() / 2.0F;
            float v8 = (float)v3.method_22683().method_4506() / 2.0F;
            float v9 = Float.POSITIVE_INFINITY;
            float v10 = Float.POSITIVE_INFINITY;
            float v11 = Float.NEGATIVE_INFINITY;
            float v12 = Float.NEGATIVE_INFINITY;
            Vector4f v14 = new Vector4f();

            for (float[] v18 : HAND_SAMPLE_POINTS) {
               v14.set(v18[0], v18[1], v18[2], 1.0F);
               v14.mul(v4);
               v14.mul(v5);
               v14.mul(v6);
               if (!(v14.w() <= 0.001F) && Float.isFinite(v14.w())) {
                  float v19 = v14.x() / v14.w();
                  float v20 = v14.y() / v14.w();
                  if (Float.isFinite(v19) && Float.isFinite(v20)) {
                     float v21 = (v19 * 0.5F + 0.5F) * v7;
                     float v22 = (0.5F - v20 * 0.5F) * v8;
                     v9 = Math.min(v9, v21);
                     v10 = Math.min(v10, v22);
                     v11 = Math.max(v11, v21);
                     v12 = Math.max(v12, v22);
                     v13++;
                  }
               }
            }

            if (v13 >= 2) {
               float v24 = (v9 + v11) / 2.0F;
               float v25 = (v10 + v12) / 2.0F;
               float v26 = MathHelper.method_15363(v11 - v9 + 34.0F, 76.0F, 260.0F);
               float v27 = MathHelper.method_15363(v12 - v10 + 42.0F, 90.0F, 280.0F);
               if (Float.isFinite(v24) && Float.isFinite(v25)) {
                  go.ho result = this.m1381(arm);

                  result.set(v24, v25, v26, v27);
               }
            }
         }
      }
   }

   private Arm m1374(float mouseX, float mouseY) {
      if (!this.m1377(mouseX, mouseY) && !(this.f1117 <= 0.0F) && !(this.f1118 <= 0.0F)) {
         to v3 = this.m1375(Arm.field_6182);
         to v4 = this.m1375(Arm.field_6183);
         boolean v5 = v3.contains(mouseX, mouseY);
         boolean v6 = v4.contains(mouseX, mouseY);
         if (v5 && v6) {
            float v7 = v3.distanceSquaredToCenter(mouseX, mouseY);
            float v8 = v4.distanceSquaredToCenter(mouseX, mouseY);
            return v7 <= v8 ? Arm.field_6182 : Arm.field_6183;
         } else if (v5) {
            return Arm.field_6182;
         } else if (v6) {
            return Arm.field_6183;
         } else if (mouseY >= this.f1118 * 0.32F) {
            return mouseX < this.f1117 / 2.0F ? Arm.field_6182 : Arm.field_6183;
         } else {

            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean isActive() {

      return this.active;
   }

   private Arm m1371(float mouseX, float mouseY) {
      Arm v3 = this.m1374(mouseX, mouseY);
      if (v3 == null) {
         float temp1 = this.f1118 * 0.32F;

         if (mouseY >= temp1) {

            v3 = mouseX < this.f1117 / 2.0F ? Arm.field_6182 : Arm.field_6183;
         }
      }

      return v3;
   }

   private go.ho m1381(Arm arm) {
      return arm == Arm.field_6183 ? this.f1100 : this.f1099;
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if (button != 0 && button != 1 || !this.f1122.handleClick(mouseX, mouseY, button) && !this.f1121.handleClick(mouseX, mouseY, button)) {
         if (button == 0) {
            if (this.f1119.contains(mouseX, mouseY)) {
               this.f1111 = !this.f1111;
               this.f1112 = 0L;
               return true;
            } else {
               oj v7 = oj.get();
               if (this.f1104.contains(mouseX, mouseY)) {
                  this.f1104.press();
                  this.saveAndExit();
               } else if (v7 != null && this.f1105.contains(mouseX, mouseY)) {
                  this.f1105.press();
                  v7.resetAll();
               } else if (v7 != null && this.f1106.contains(mouseX, mouseY)) {
                  this.f1106.press();

                  v7.resetLeft();
               } else if (v7 != null && this.f1107.contains(mouseX, mouseY)) {
                  this.f1107.press();
                  v7.resetRight();
               } else {
                  Arm v5 = this.m1371(mouseX, mouseY);
                  if (v5 != null) {
                     this.m1369(v5, mouseX, mouseY, button);
                  }
               }

               return true;
            }
         } else if (button != 1) {
            return true;
         } else if (this.m1377(mouseX, mouseY)) {
            return true;
         } else {
            Arm v4 = this.m1371(mouseX, mouseY);
            if (v4 != null) {
               this.m1369(v4, mouseX, mouseY, button);
            }

            return true;
         }
      } else {
         return true;
      }
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {
      if (!this.f1122.rawMouseMoved(mouseX, mouseY)) {
         if (!this.f1121.rawMouseMoved(mouseX, mouseY)) {
            this.m1370(mouseX, mouseY);
         }
      }
   }

   private void m1373() {
      if (this.f1111) {
         MinecraftClient v1 = MinecraftClient.method_1551();
         if (v1 != null && v1.field_1724 != null && !v1.field_1724.field_6252) {
            long v2 = System.currentTimeMillis();
            if (v2 >= this.f1112) {
               Hand v4 = v1.field_1724.method_6068() == Arm.field_6183 ? Hand.field_5808 : Hand.field_5810;
               v1.field_1724.method_23667(v4, false);

               this.f1112 = v2 + 2503110380468750492L + -2503110380468749992L;
            }
         }
      }
   }

   public void captureBeforeHands() {
      if (!this.active) {
         
      } else {
         ds v1 = ds.getInstance();
         v1.setKeepHandsResult(true);
         GlassHands result = GlassHands.getInstance();

         GlassHands v2 = result;
         if (v2 != null && v2.isEnabled()) {
            v1.setEnabled(true);
            v2.applyRendererSettings();
         } else {
            this.m1379(v1);
         }

         v1.captureSceneBeforeHands();
         
      }
   }

   @Override
   public boolean mouseDragged(float deltaX, float deltaY) {
      return this.active;
   }

   private void m1369(Arm arm, float mouseX, float mouseY, int button) {
      oj v5 = oj.get();
      if (v5 != null) {
         this.f1109 = arm;
         this.f1110 = button;
         this.f1113 = mouseX;
         this.f1114 = mouseY;
         this.f1115 = arm == Arm.field_6183 ? v5.rx.getValue() : v5.lx.getValue();
         Float temp1;
         if (arm == Arm.field_6183) {
            temp1 = v5.ry.getValue();

            
         } else {
            temp1 = v5.ly.getValue();
         }

         this.f1116 = temp1;
      }
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {
         if (button == 0) {
            this.f1121.releaseDrag();

            this.f1122.releaseDrag();
         }

         if (button == this.f1110) {
            this.f1109 = null;
            this.f1110 = -1;
         }

         return true;
      }
   }

   private void m1380() {
      ds v1 = ds.getInstance();
      v1.setKeepHandsResult(false);
      GlassHands v2 = GlassHands.getInstance();

      if (v2 != null && v2.isEnabled()) {
         v1.setEnabled(true);
         v2.applyRendererSettings();
      } else {
         v1.setEnabled(false);
      }
   }

   private void m1365() {
      this.active = false;
      this.f1108 = null;
      this.f1109 = null;
      this.f1110 = -1;
      this.f1121.releaseDrag();

      this.f1122.releaseDrag();
      this.f1111 = false;
      this.f1112 = 0L;
      this.m1366();
      oj v1 = oj.get();
      if (v1 != null) {
         v1.setEditorActive(false);
      }

      this.m1380();
   }

   private GuiCheckBox() {
      this.f1103 = new Animation();
      this.f1104 = new oo("Выйти и сохранить", true);
      this.f1105 = new oo("Сбросить обе руки", false);
      this.f1106 = new oo("Сбросить левую", false);
      this.f1107 = new oo("Сбросить правую", false);
      this.f1110 = -1;
      this.f1119 = to.EMPTY;
      this.f1120 = new qo();
      this.f1121 = new uo();
      this.f1122 = new uo();
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {
      if (!this.active) {
         return false;
      } else if (this.f1122.handleScroll(mouseX, mouseY, verticalAmount) || this.f1121.handleScroll(mouseX, mouseY, verticalAmount)) {
         return true;
      } else if (this.f1119.contains(mouseX, mouseY)) {
         return true;
      } else {
         Arm v5 = this.m1374(mouseX, mouseY);
         oj v6 = oj.get();
         if (v5 != null && v6 != null && verticalAmount != 0.0) {
            SliderSetting v7 = v5 == Arm.field_6183 ? v6.rScale : v6.lScale;
            float v8 = verticalAmount > 0.0 ? 1.0F : -1.0F;
            float v9 = v7.getValue();
            float v10 = this.m1372(v9 + v8 * v7.increment, v7);
            if (Math.abs(v10 - v9) < 1.0E-4F) {
               return true;
            } else {
               to v11 = this.m1381(v5).freshRect();
               to v12 = v11 != null ? v11 : this.m1375(v5);
               float v13 = v10 / v9;
               float v14 = (1.0F - v13) * (mouseX - v12.centerX());
               float v15 = (1.0F - v13) * (mouseY - v12.centerY());
               SliderSetting v16 = v5 == Arm.field_6183 ? v6.rx : v6.lx;
               SliderSetting result;
               if (v5 == Arm.field_6183) {
                  result = v6.ry;
                  
               } else {
                  result = v6.ly;
               }

               SliderSetting v17 = result;
               float v18 = this.m1376();
               v16.set(MathHelper.method_15363(v16.getValue() + v14 / v18, v16.min, v16.max));
               Float v21 = MathHelper.method_15363(v17.getValue() - v15 / v18, v17.min, v17.max);

               v17.set(v21);
               v7.set(v10);
               return true;
            }
         } else {
            return true;
         }
      }
   }

   private to m1375(Arm arm) {
      oj v2 = oj.get();
      float v3 = 0.0F;
      float v4 = 0.0F;
      float v5 = 1.0F;
      if (v2 != null) {
         v3 = arm == Arm.field_6183 ? v2.rx.getValue() : v2.lx.getValue();
         v4 = arm == Arm.field_6183 ? v2.ry.getValue() : v2.ly.getValue();
         v5 = arm == Arm.field_6183 ? v2.rScale.getValue() : v2.lScale.getValue();
      }

      float v6 = this.m1376();
      float v7 = this.f1117 * (arm == Arm.field_6182 ? 0.16F : 0.84F);
      float v8 = this.f1118 * 0.78F;
      float v9 = MathHelper.method_15363(Math.min(this.f1117, this.f1118) * 0.38F, 150.0F, 240.0F);
      float v10 = MathHelper.method_15363(v9 * (0.88F + v5 * 0.12F), 145.0F, 275.0F);
      float v11 = v7 + v3 * v6;
      float v12 = v8 - v4 * v6;
      v11 = MathHelper.method_15363(v11, v10 / 2.0F + 8.0F, this.f1117 - v10 / 2.0F - 8.0F);
      v12 = MathHelper.method_15363(v12, v10 / 2.0F + 52.0F, this.f1118 - v10 / 2.0F - 54.0F);
      to result = new to;

      result./* $VF: Unable to resugar constructor */<init>(v11 - v10 / 2.0F, v12 - v10 / 2.0F, v10, v10);
      return result;
   }

   private void m1379(ds renderer) {

      renderer.setEnabled(true);
      renderer.setBlurEnabled(false);
      renderer.setSaturation(1.0F);
      renderer.setReflect(false);
      renderer.setTintColor(0);
      renderer.setTintIntensity(0.0F);

      renderer.setIceIntensity(0.0F);
      renderer.setSmokeAmount(0.0F);
      renderer.setOutlineEnabled(true);
      float temp1;
      if (this.f1108 == null) {
         temp1 = 12.0F;
         
      } else {
         temp1 = 20.0F;
      }

      renderer.setOutlineGlowStrength(temp1);
      renderer.setOutlineWidth(1);
      renderer.setOutlineGlowMode(2);
      renderer.setOutlineFillColor(0);
      renderer.setOutlineColor(up.getClientColor1(1));
      renderer.setShimmerEnabled(true);
      renderer.setShimmerWidth(0.045F);
      renderer.setShimmerPeriodSec(2.8F);
   }

   private void m1378(Animation animation, boolean hovered) {
      animation.update();
      animation.run(hovered ? 1.0 : 0.0, 0.18F, ap.QUAD_OUT, true);

      
   }

   private boolean m1377(float mouseX, float mouseY) {
      if (!this.f1104.contains(mouseX, mouseY)
         && !this.f1105.contains(mouseX, mouseY)
         && !this.f1106.contains(mouseX, mouseY)
         && !this.f1107.contains(mouseX, mouseY)
         && !this.f1119.contains(mouseX, mouseY)) {

         if (!this.f1121.bounds().contains(mouseX, mouseY) && !this.f1122.bounds().contains(mouseX, mouseY)) {
            return false;
         }
      }

      return true;
   }

   private float m1372(float value, SliderSetting setting) {
      float result = fq.round(value, setting.increment);

      return MathHelper.method_15363(result, setting.min, setting.max);
   }

   private void m1367(float width, float alpha) {
      Client.get().render2D().flushAll();

      hu.sf_regular
         .drawCentered(
            bb.tr("Редактор рук"), width / 2.0F, 16.0F, 8.0F, up.getColor(240, alpha)
         );
      yt result = hu.sf_regular;
      String temp1 = bb.tr("Наведите курсор на руку");
      float v10 = width / 2.0F;

      result.drawCentered(temp1, v10, 29.0F, 6.0F, up.getColor(255, alpha * 0.45F));
      result = hu.sf_regular;
      temp1 = bb.tr("ЛКМ / ПКМ + движение — переместить");
      String v3 = bb.tr("Колесо — изменить размер");
      String v4 = temp1;
      result.drawCentered(v4 + "   •   " + v3, width / 2.0F, 39.0F, 5.5F, up.getColor(255, alpha * 0.3F));
   }

   @Environment(EnvType.CLIENT)
   private static final class ho {
      private float f1097;
      private float width;
      private float f1096;
      private long f1098;
      private float height;

      void invalidate() {

         this.f1098 = 0L;
      }

      void set(float centerX, float centerY, float width, float height) {
         this.f1096 = centerX;
         this.f1097 = centerY;
         int temp2 = v5 + 1;

         this.width = width;
         this.height = height;
         this.f1098 = System.currentTimeMillis();
      }

      to freshRect() {

         boolean result;
         if (this.f1098 > 0L && System.currentTimeMillis() - this.f1098 <= 1656108775573975496L + -1656108775573975246L) {
            result = true;
            
         } else {
            result = false;
         }

         boolean v1 = result;
         to v5;
         if (v1) {
            v5 = new to;

            v5./* $VF: Unable to resugar constructor */<init>(this.f1096 - this.width / 2.0F, this.f1097 - this.height / 2.0F, this.width, this.height);
         } else {
            v5 = null;
         }

         return v5;
      }

      
   }
}
