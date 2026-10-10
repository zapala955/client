package ru.white.screen;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.notification.NotificationType;
import ru.white.notification.NotificationRenderer;
import ru.white.notification.INotification;
import ru.white.utils.misc.To;
import ru.white.notification.Notification;
import ru.white.ui.DraggableWidget;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.vs;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.utils.IMinecraft;
import ru.white.manager.ConfigManager;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class GuiColorPicker implements ro, IMinecraft {
   private static final mo INSTANCE = new mo();
   private double f1203;
   private final uo f1186;
   private final oo f1189;
   private double f1202;
   private boolean f1196;
   private float f1199;
   private to f1197;
   private OtherClientPlayerEntity f1194;
   private static final UUID DUMMY_UUID = UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000101");
   private jf f1192;
   private float f1204;
   private Vec3d f1201;
   private boolean active;
   private final oo f1188;
   private boolean f1193;
   private final Animation f1190;
   private final qo f1185 = new qo();
   private boolean f1198;
   private final kf f1187;
   private static final String DUMMY_NAME = "ROSE_PREVIEW";
   private float f1200;
   private Module f1191;
   private long f1195;

   private GuiColorPicker() {
      this.f1186 = new uo();
      this.f1187 = new kf();
      this.f1188 = new oo("Выйти и сохранить", true);
      this.f1189 = new oo("Показать сейчас", false);
      this.f1190 = new Animation();
      this.f1197 = to.EMPTY;
   }

   @Override
   public boolean mouseReleased(int button) {
      if (!this.active) {
         return false;
      } else {
         if (button == 0) {
            this.f1186.releaseDrag();

            this.f1198 = false;
         }

         return true;
      }
   }

   private void m1453(float width, float height, boolean repeats) {
      float v4 = 18.0F;
      float v5 = 120.0F;
      float v6 = 118.0F;
      float v7 = height - 30.0F;
      this.f1188.set(width / 2.0F - v5 / 2.0F, v7, v5, v4);
      this.f1189.set(width / 2.0F - v6 / 2.0F, v7 - 24.0F, v6, repeats ? v4 : 0.0F);
      float result = v7 - 24.0F;

      float v8 = result - (repeats ? 22.0F : 0.0F);
      this.f1197 = new to(width / 2.0F - 80.0F, v8, 160.0F, 13.0F);
   }

   private void m1450(float mouseX, float mouseY) {
      if (mc.field_1724 != null) {

         float v3 = (mouseX - this.f1199) * 0.6F;
         float v4 = (mouseY - this.f1200) * 0.6F;
         this.f1199 = mouseX;
         this.f1200 = mouseY;
         float v5 = mc.field_1724.method_36454() + v3;

         float v6 = MathHelper.method_15363(mc.field_1724.method_36455() + v4, -90.0F, 90.0F);
         mc.field_1724.method_36456(v5);
         mc.field_1724.method_36457(v6);
         mc.field_1724.field_5982 = v5;

         mc.field_1724.field_6004 = v6;
         mc.field_1724.method_5847(v5);
      }
   }

   @Override
   public void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
      if (this.active) {
         if (!pressed) {
            if (button == 0) {

               this.f1186.releaseDrag();
               this.f1198 = false;
            }
         } else {
            if (this.f1196 && button == 0 && !this.m1454(mouseX, mouseY)) {

               this.f1198 = true;
               this.f1199 = mouseX;
               this.f1200 = mouseY;
            }
         }
      }
   }

   @Override
   public boolean mouseScrolled(float mouseX, float mouseY, double verticalAmount) {

      if (!this.active) {
         return false;
      } else {
         this.f1186.handleScroll(mouseX, mouseY, verticalAmount);
         return true;
      }
   }

   @Override
   public boolean isActive() {

      return this.active;
   }

   public Module module() {

      return this.f1191;
   }

   private void m1444() {
      if (this.f1192 != null) {
         this.f1192.previewStop();
      }

      this.m1449(false);
      this.m1446();
      if (this.f1191 != null && this.f1191.isEnabled() != this.f1193) {
         this.f1191.setEnabled(this.f1193, false);
      }

      this.f1186.releaseDrag();
      this.f1186.closePickers();

      this.active = false;
      this.f1191 = null;
      this.f1192 = null;
      this.f1187.reset();
   }

   private float m1447() {

      if (this.f1201 != null) {
         return this.f1204;
      } else {
         return mc.field_1724 == null ? 0.0F : MathHelper.method_15393(mc.field_1724.method_36454() + 180.0F);
      }
   }

   private void m1449(boolean enabled) {
      this.f1196 = enabled;

      this.f1198 = false;
      if (enabled) {
         if (mc.field_1724 != null) {
            double v2 = Math.toRadians((double)mc.field_1724.method_36454());
            this.f1201 = mc.field_1724.method_73189();
            this.f1202 = -Math.sin(v2);
            this.f1203 = Math.cos(v2);
            this.f1204 = MathHelper.method_15393(mc.field_1724.method_36454() + 180.0F);
            return;
         }

         
      }

      this.f1201 = null;
   }

   public to panelBounds() {

      int v4 = v1 * (v1 + 1) % 2;

      if (v4 != 0) {
         }

      return this.f1186.bounds();
   }

   @Override
   public void rawMouseMoved(float mouseX, float mouseY) {
      if (this.active) {

         if (!this.f1186.rawMouseMoved(mouseX, mouseY)) {
            if (this.f1198) {
               this.m1450(mouseX, mouseY);
            }
         }
      }
   }

   public boolean isEditing(Module candidate) {
      boolean result;
      if (this.active && this.f1191 == candidate) {
         result = true;
      } else {
         result = false;
         
      }

      return result;
   }

   private void m1451() {
      if (this.active && this.f1192 != null) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            this.m1448();
            if (this.f1192.previewNeedsDummy()) {
               if (this.f1194 != null && !this.f1194.method_31481()) {

                  if (!this.f1192.previewControlsDummy()) {
                     Vec3d v1 = this.f1187.anchor();
                     this.f1194.method_5814(v1.field_1352, v1.field_1351, v1.field_1350);
                     float v2 = this.m1447();
                     this.f1194.method_36456(v2);
                     this.f1194.method_5636(v2);
                     this.f1194.method_5847(v2);

                     this.f1194.method_36457(0.0F);
                  }
               } else {
                  this.m1445();
               }
            }

            this.f1192.previewTick(this.f1187);
            if (this.f1192.previewSettings().repeats()) {
               long v5 = System.currentTimeMillis();
               if (v5 >= this.f1195) {
                  this.f1192.previewSpawn(this.f1187);
                  this.f1187.nextPhase();
                  this.f1195 = v5 + Math.max(-4279777210350235553L + 4279777210350235753L, this.f1192.previewSettings().intervalMs());
               }
            }
         }
      }
   }

   private void m1446() {
      if (this.f1194 != null) {
         this.f1194.method_31472();
         this.f1194 = null;
      }

      this.f1187.setDummy(null);
   }

   private void m1445() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.m1446();
         OtherClientPlayerEntity result = new OtherClientPlayerEntity;

         result./* $VF: Unable to resugar constructor */<init>(
            mc.field_1687, new GameProfile(DUMMY_UUID, "ROSE_PREVIEW")
         );
         OtherClientPlayerEntity v1 = result;
         Vec3d v2 = this.f1187.anchor();
         v1.method_5808(v2.field_1352, v2.field_1351, v2.field_1350, this.m1447(), 0.0F);
         Hand temp1 = Hand.field_5808;
         ItemStack temp2 = new ItemStack;

         temp2./* $VF: Unable to resugar constructor */<init>(Items.field_22022);
         v1.method_6122(temp1, temp2);
         v1.method_6122(Hand.field_5810, new ItemStack(Items.field_8288));
         v1.method_6033(20.0F);
         v1.method_6073(0.0F);
         mc.field_1687.method_53875(v1);
         this.f1194 = v1;
         this.f1187.setDummy(v1);
      }
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (!this.active) {
         return false;
      } else if ((button == 0 || button == 1) && this.f1186.handleClick(mouseX, mouseY, button)) {
         return true;
      } else {
         if (button == 0) {
            if (this.f1197.contains(mouseX, mouseY)) {
               this.m1449(!this.f1196);
            } else if (this.f1188.contains(mouseX, mouseY)) {
               this.f1188.press();
               this.saveAndExit();
            } else {

               if (this.f1189.contains(mouseX, mouseY)) {
                  this.f1189.press();
                  this.m1455();
               }
            }
         }

         return true;
      }
   }

   private boolean m1454(float mouseX, float mouseY) {
      boolean result = this.f1186.bounds().contains(mouseX, mouseY);

      return result || this.f1197.contains(mouseX, mouseY) || this.f1188.contains(mouseX, mouseY) || this.f1189.contains(mouseX, mouseY);
   }

   @Override
   public void closeFromMenuRemoval() {

      this.saveAndExit();
      
   }

   @Override
   public void saveAndExit() {
      if (this.active) {
         Client result = Client.get();

         if (result != null && Client.get().configManager() != null) {
            ConfigManager v3 = Client.get().configManager();

            v3.autoSave();
         }

         this.m1444();
      }
   }

   public void open(Module target) {
      if (target instanceof jf v2) {
         if (this.active) {
            this.m1444();
         }

         this.active = true;
         this.f1191 = target;
         this.f1192 = v2;
         this.f1193 = target.isEnabled();
         if (!target.isEnabled()) {
            target.setEnabled(true, false);
         }

         this.f1187.reset();
         this.f1186.reset();
         this.f1186.unplace();
         this.m1449(false);
         this.m1448();
         if (v2.previewNeedsDummy()) {
            this.m1445();
         }

         this.f1187.setDummy(this.f1194);
         v2.previewStart(this.f1187);
         this.f1195 = System.currentTimeMillis() + 4301664688222090556L + -4301664688222090306L;
         this.f1190.set(0.0);

         this.f1190.run(1.0, 0.24F, ap.EXPO_OUT);
         
      }
   }

   public static mo getInstance() {

      return INSTANCE;
   }

   private void m1448() {
      if (this.f1191 != null && this.f1192 != null && mc.field_1724 != null) {
         lf v1 = this.f1192.previewSettings();
         float v2 = v1.distance(4.0F);
         float v3 = v1.height(0.0F);
         Vec3d v4 = this.f1201 != null ? this.f1201 : mc.field_1724.method_73189();

         double v5;
         double v7;
         if (this.f1201 != null) {
            v5 = this.f1202;
            v7 = this.f1203;
         } else {
            double v9 = Math.toRadians((double)mc.field_1724.method_36454());
            v5 = -Math.sin(v9);
            v7 = Math.cos(v9);
         }

         this.f1187.setDistance(v2);
         this.f1187.setHeight(v3);
         this.f1187.setAnchor(new Vec3d(v4.field_1352 + v5 * (double)v2, v4.field_1351 + (double)v3, v4.field_1350 + v7 * (double)v2));
      }
   }

   @Override
   public void render(float width, float height, float mouseX, float mouseY, float parentAlpha) {
      if (this.active && this.f1191 != null) {
         this.f1185.setMouse(mouseX, mouseY);
         this.m1451();
         this.f1186.layout(this.f1191, this.f1191.getBigName(), width - 168.0F - 16.0F, 14.0F, width, height, this.f1185);
         boolean v6 = this.f1192.previewSettings().repeats();
         this.m1453(width, height, v6);
         this.f1188.update(mouseX, mouseY);
         if (v6) {
            this.f1189.update(mouseX, mouseY);
         }

         this.f1190.update();
         float v7 = this.f1190.get() * parentAlpha;
         this.m1452(0.0F, 0.0F, width, 62.0F, v7, true);
         this.m1452(0.0F, height - 62.0F, width, 62.0F, v7, false);
         Client.get().render2D().flushAll();
         yt result = hu.sf_regular;
         String temp1 = bb.tr("Предпоказ");

         String v8 = this.f1191.getBigName();
         String v9 = temp1;
         result.drawCentered(v9 + " — " + v8, width / 2.0F, 16.0F, 8.0F, up.getColor(240, v7));
         hu.sf_regular
            .drawCentered(
               bb.tr(
                  v6
                     ? "Эффект повторяется сам — крутите настройки справа"
                     : "Эффект виден постоянно — крутите настройки справа"
               ),
               width / 2.0F,
               29.0F,
               6.0F,
               up.getColor(255, v7 * 0.45F)
            );
         result = hu.sf_regular;
         temp1 = bb.tr(
            this.f1196
               ? "Тащите по пустому месту, чтобы осмотреться"
               : "Настройки предпоказа — в самом низу панели"
         );
         String v10 = bb.tr("Esc — выйти");
         String v11 = temp1;
         result.drawCentered(v11 + "   •   " + v10, width / 2.0F, 39.0F, 5.5F, up.getColor(255, v7 * 0.3F));

         this.f1185
            .drawToggleRow(
               this.f1197,
               bb.tr("Свободная камера"),
               this.f1196,
               "preview:freecamera",
               v7
            );
         if (v6) {
            this.f1185.drawButton(this.f1189, v7);
         }

         this.f1185.drawButton(this.f1188, v7);
         this.f1186.draw(this.f1185, v7);
      }
   }

   private void m1455() {
      if (this.f1192 != null) {
         if (mc.field_1724 != null) {
            if (!this.f1192.previewSettings().repeats()) {
               return;
            }

            this.m1448();
            this.f1192.previewSpawn(this.f1187);
            this.f1187.nextPhase();
            long temp1 = System.currentTimeMillis();
            long temp2 = -8063876610323430122L + 8063876610323430322L;

            this.f1195 = temp1 + Math.max(temp2, this.f1192.previewSettings().intervalMs());
            return;
         }

         
      }
   }

   private void m1452(float x, float y, float width, float height, float alpha, boolean fadeDown) {

      int v7 = up.getColor(0, 0.55F * alpha);
      int v8 = up.getColor(0, 0.0F);
      int result;
      if (fadeDown) {
         result = v7;
      } else {
         result = v8;
         
      }

      int v9 = result;
      int v10 = fadeDown ? v8 : v7;
      vs.ys.gradientRect(x, y, width, height, new int[]{v9, v9, v10, v10}, 0.0F);
   }

   
}
