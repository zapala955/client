package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.HitResult.Type;
import ru.white.event.EventTarget;
import ru.white.module.setting.MatrixSetting;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.GuiSettingElement;
import ru.white.screen.ClickGuiScreen;
import ru.white.theme.wo;
import ru.white.utils.animation.Animation;
import ru.white.utils.render.vs;
import ru.white.event.impl.EventReceivePacket;

@ModuleAnnotation(
   name = "Cross Hair",
   desc = "Кастомный прицел",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class CrossHair extends Module {
   public final SliderSetting pixelSize;
   public final BooleanSetting targetColorToggle;
   public final tf gradientColor1;
   public final tf targetGradient1;
   public sf openEditor = new sf(this, "Редактор прицела", () -> {
      if (mc.field_1755 instanceof ko v0) {
         v0.openCrosshairEditor();
      } else if (mc.field_1755 instanceof DropDownMenu v1) {
         v1.openCrosshairEditor();

         
      } else {
         Screen v8 = mc.field_1755;
         if (v8 instanceof TestGui) {

            TestGui v2 = (TestGui)v8;
            v2.openCrosshairEditor();
         }
      }
   }).setVisible(() -> {
      boolean result;
      if (this.isDrawing() && !this.isEditorOpen()) {
         result = true;
      } else {
         result = false;
         
      }

      return result;
   });
   public final ModeSetting targetColorMode;
   public final MatrixSetting grid;
   public final SliderSetting gradientSpeed;
   public final ModeSetting gradientMode;
   public final tf pixelTint;
   public final tf gradientColor2;
   public final SliderSetting targetGradientSpeed;
   public final SliderSetting silaAnim;
   public final tf targetTint;
   public static final int GRID_SIZE = 21;
   public Animation animation;
   public final tf targetGradient2;
   public final ModeSetting pixelColorMode;
   public final ModeSetting targetGradientMode;
   public final BooleanSetting pixelOutline;
   public ModeSetting type = new ModeSetting(this, "Режим", "Кастомный", "Точка", "Кружочек");

   public boolean isEditorSettingVisible() {
      if (!this.isDrawing()) {
         return false;
      } else {
         boolean v5;
         label36: {
            label35: {
               if (!(mc.field_1755 instanceof ko) && !(mc.field_1755 instanceof DropDownMenu)) {
                  if (!(mc.field_1755 instanceof TestGui)) {
                     break label35;
                  }

                  
               }

               if (!this.isEditorOpen()) {
                  v5 = true;
                  break label36;
               }
            }

            v5 = false;
         }

         boolean v1 = v5;
         return !v1;
      }
   }

   public CrossHair() {
      this.grid = new MatrixSetting(this, "Рисунок", 21);
      this.pixelSize = new SliderSetting(this, "Размер пикселя", 1.0F, 0.5F, 4.0F, 0.5F)
         .setVisible(this::isEditorSettingVisible);
      this.pixelOutline = new BooleanSetting(this, "Обводка рисунка", true)
         .setVisible(this::isEditorSettingVisible);
      this.pixelColorMode = new ModeSetting(this, "Цвет рисунка", "Тема", "Свой", "Градиент")
         .setVisible(this::isEditorSettingVisible);
      this.pixelTint = new tf(this, "Свой цвет", -1).setVisible(() -> {
         boolean result;
         if (this.isEditorSettingVisible() && "Свой".equalsIgnoreCase(this.pixelColorMode.getValue())) {

            result = true;
            int temp2 = v1 + 1;

            
         } else {
            result = false;
         }

         return result;
      });
      this.gradientMode = new ModeSetting(
            this,
            "Режим градиента",
            "Статический",
            "Анимированный"
         )
         .setVisible(() -> {
            boolean result = this.isEditorSettingVisible();

            if (result) {

               if ("Градиент".equalsIgnoreCase(this.pixelColorMode.getValue())) {
                  return true;
               }
            }

            return false;
         });
      this.gradientColor1 = new tf(this, "Цвет градиента 1", -65536)
         .setVisible(() -> {
            if (this.isEditorSettingVisible()) {
               boolean result = "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue());

               if (result) {

                  return true;
               }
            }

            return false;
         });
      this.gradientColor2 = new tf(this, "Цвет градиента 2", -16776961)
         .setVisible(() -> {
            if (this.isEditorSettingVisible()) {
               int temp3 = v2 + 1;

               if ("Градиент".equalsIgnoreCase(this.pixelColorMode.getValue())) {
                  return true;
               }
            }

            return false;
         });
      this.gradientSpeed = new SliderSetting(this, "Скорость градиента", 1.0F, 0.1F, 4.0F, 0.1F)
         .setVisible(() -> {
            if (this.isEditorSettingVisible() && "Градиент".equalsIgnoreCase(this.pixelColorMode.getValue())) {
               String result = "Анимированный";
               String temp1 = this.gradientMode.getValue();

               if (result.equalsIgnoreCase(temp1)) {
                  return true;
               }
            }

            return false;
         });
      this.targetColorToggle = new BooleanSetting(this, "Цвет при наведении", false)
         .setVisible(this::isEditorSettingVisible);
      this.targetColorMode = new ModeSetting(this, "Цвет сущности", "Тема", "Свой", "Градиент")
         .setVisible(() -> {
            if (this.isEditorSettingVisible()) {
               Boolean result = this.targetColorToggle.getValue();
               int temp1 = v1 * (v1 + 1) % 2;

               if (temp1 != 0) {
                  }

               if (result) {
                  return true;
               }
            }

            return false;
         });
      this.targetTint = new tf(this, "Свой цвет сущности", -65536)
         .setVisible(() -> {
            if (this.isEditorSettingVisible() && this.targetColorToggle.getValue()) {
               String temp1 = this.targetColorMode.getValue();

               if ("Свой".equalsIgnoreCase(temp1)) {
                  return true;
               }
            }

            return false;
         });
      this.targetGradientMode = new ModeSetting(
            this,
            "Градиент сущности",
            "Статический",
            "Анимированный"
         )
         .setVisible(() -> {
            if (this.isEditorSettingVisible() && this.targetColorToggle.getValue()) {

               if ("Градиент".equalsIgnoreCase(this.targetColorMode.getValue())) {

                  return true;
               }
            }

            return false;
         });
      this.targetGradient1 = new tf(this, "Цвет сущности 1", -65536).setVisible(() -> {
         if (this.isEditorSettingVisible()) {

            if (this.targetColorToggle.getValue() && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue())) {
               return true;
            }
         }

         return false;
      });
      this.targetGradient2 = new tf(this, "Цвет сущности 2", -256)
         .setVisible(() -> {
            if (this.isEditorSettingVisible() && this.targetColorToggle.getValue()) {

               if ("Градиент".equalsIgnoreCase(this.targetColorMode.getValue())) {
                  return true;
               }
            }

            return false;
         });
      this.targetGradientSpeed = new SliderSetting(this, "Скорость сущности", 1.0F, 0.1F, 4.0F, 0.1F)
         .setVisible(
            () -> this.isEditorSettingVisible()
                  && this.targetColorToggle.getValue()
                  && "Градиент".equalsIgnoreCase(this.targetColorMode.getValue())
                  && "Анимированный".equalsIgnoreCase(this.targetGradientMode.getValue())
         );
      this.silaAnim = new SliderSetting(this, "Сила анимации при ударе", 5.0F, 0.0F, 10.0F, 1.0F)
         .setVisible(this::isEditorSettingVisible);
      this.animation = new Animation();
   }

   public boolean isDrawing() {
      boolean result = this.type.is("Кастомный");

      return result;
   }

   public static xi getInstance() {

      return uq.get(xi.class);
   }

   public int resolvePixelColor(float alphaMul) {
      int temp1 = this.grid.center();
      int temp2 = this.grid.center();

      return this.resolvePixelColor(temp1, temp2, alphaMul);
   }

   private void m680(float centerX, float centerY, float ps, int c, float expand, xi.yi consumer) {
      for (int v7 = 0; v7 < this.grid.size; v7++) {

         while (true) {

            if (v8 >= this.grid.size) {
               break;
            }

            if (this.grid.get(v8, v7)) {
               int v9 = v8 - c;
               int v10 = v7 - c;
               float v11 = centerX + (float)v9 * ps + Math.signum((float)v9) * expand - ps / 2.0F;
               float v12 = centerY + (float)v10 * ps + Math.signum((float)v10) * expand - ps / 2.0F;

               consumer.accept(v11, v12, v8, v7);
            }

            v8++;
         }
      }
   }

   @EventTarget
   public void onRender(EventReceivePacket eventDisplay) {
      float v2 = 2.0F;
      float v3 = (float)mc.method_22683().method_4495();
      float v4 = v2 / v3;
      int v5 = (int)((float)mc.method_22683().method_4486() / v4);
      int v6 = (int)((float)mc.method_22683().method_4502() / v4);
      float v7 = (float)(v5 / 2);
      float v8 = (float)(v6 / 2);
      if (this.type.is("Кастомный")) {
         this.animation.update();
         float v11 = 1.0F - mc.field_1724.method_7261(0.0F);
         this.animation.run((double)v11, 0.001F);
         this.drawPixelCrosshair(v7, v8, this.expandFor(this.animation.get()), 1.0F);
      } else {
         if (this.type.is("Кружочек")) {
            this.animation.update();
            float v9 = 1.0F - mc.field_1724.method_7261(0.0F);
            this.animation.run((double)v9, 0.01F);
            vs.ys.roundedCircleProgress(
               eventDisplay.getDrawContext(), v7, v8, 4.0F, 1.0F, this.animation.get(), wo.getHudColor(), up.getColor(0), up.getColor(0)
            );
         }

         if (this.type.is("Точка")) {
            float result = v7 - 1.5F;
            float temp1 = v8 - 1.5F;
            int[] temp4 = new int[]{up.getColor(255), up.getColor(255), 0, 0};

            temp4[2] = up.getColor(255);
            temp4[3] = up.getColor(255);
            vs.ys.gradientRect(result, temp1, 3.0F, 3.0F, temp4, 4.0F);
            vs.ys.outline(v7 - 2.0F, v8 - 2.0F, 4.0F, 4.0F, 0.5F, up.getColor(0), 4.0F);
         }
      }
   }

   public boolean isEditorOpen() {
      if (eo.getInstance() != null) {
         boolean result = eo.getInstance().isActive();

         if (result) {
            return true;
         }
      }

      return false;
   }

   public float expandFor(float animValue) {

      return this.silaAnim.getValue() * animValue;
   }

   public int resolvePixelColor(int gx, int gy, float alphaMul) {
      boolean result;
      if (this.targetColorToggle.getValue() && mc.field_1765 != null && mc.field_1765.method_17783() == Type.field_1331) {
         result = true;
      } else {
         result = false;
         
      }

      boolean v4 = result;
      String v5 = v4 ? this.targetColorMode.getValue() : this.pixelColorMode.getValue();

      int v6 = switch (v5) {
         case "Свой" -> v4 ? this.targetTint.getValue() : this.pixelTint.getValue();
         case "Градиент" -> {
            float v21 = (float)(gx + gy);

            float v9 = v21 / (float)Math.max(1, this.grid.size * 2 - 2);
            String v11 = v4 ? this.targetGradientMode.getValue() : this.gradientMode.getValue();
            float v10;
            if ("Анимированный".equalsIgnoreCase(v11)) {
               double v12 = (v4 ? this.targetGradientSpeed : this.gradientSpeed).getValue().doubleValue();
               double v14 = (double)System.currentTimeMillis() * v12 / 1000.0;
               double v22 = Math.sin(((double)v9 + v14) * Math.PI * 2.0);

               v10 = (float)((v22 + 1.0) / 2.0);
            } else {
               v10 = v9;
            }

            int v19 = v4 ? this.targetGradient1.getValue() : this.gradientColor1.getValue();
            int v13 = v4 ? this.targetGradient2.getValue() : this.gradientColor2.getValue();
            yield up.interpolateColor(v19, v13, v10);
         }
         default -> up.getClientColor1(1);
      };
      return up.replAlpha(v6, alphaMul);
   }

   public void drawPixelCrosshair(float centerX, float centerY, float expand, float alphaMul) {
      if (!(alphaMul <= 0.01F)) {

         float v5 = Math.max(0.5F, this.pixelSize.getValue());
         int v6 = this.grid.center();
         if (this.pixelOutline.getValue()) {
            int result = up.getColor(0, alphaMul * 0.85F);

            int v7 = result;
            this.m680(centerX, centerY, v5, v6, expand, (px, py, gx, gy) -> {
               float var10000x = px - 0.5F;
               float var10001x = py - 0.5F;
               int v6x = 1741069839;
               int temp4 = v6x + 1;

               vs.ys.rect(var10000x, var10001x, v5 + 1.0F, v5 + 1.0F, v7);
            });
         }

         this.m680(centerX, centerY, v5, v6, expand, (px, py, gx, gy) -> {
            int v7x = this.resolvePixelColor(gx, gy, alphaMul);
            int v8x = 650640821;
            int var10000x = v8x * (v8x + 1);

            vs.ys.rect(px, py, v5, v5, v7x);
         });
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   private interface yi {
      void accept(float v1, float v2, int v3, int v4);
   }
}
