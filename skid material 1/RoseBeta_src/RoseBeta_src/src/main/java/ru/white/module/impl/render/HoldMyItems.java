package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Uf;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "HoldMyItems",
   desc = "Кастомная анимация и отрисовка предметов в руках",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class HoldMyItems extends Module {
   public final BooleanSetting swimmingAnimation;
   public final ModeSetting hmiAnimationType;
   public SliderSetting rightZ;
   public sf resetLeftButton;
   public SliderSetting rightX;
   public SliderSetting rightScale;
   public sf resetRightButton;
   public uf handsPosTitle;
   public uf rightTitle;
   public sf resetAllButton;
   public SliderSetting leftZ;
   public SliderSetting leftX;
   public final BooleanSetting swapHands;
   public SliderSetting rightY;
   public final BooleanSetting mb3DCompat;
   public final BooleanSetting climbAndCrawl;
   public SliderSetting leftY;
   public uf leftTitle;
   public SliderSetting leftScale;
   public final SliderSetting hmiSmoothness;
   public final BooleanSetting hmiEnable = new BooleanSetting(this, "Включить", true);

   public void resetRight() {
      this.rightX.set(0.0F);

      this.rightY.set(0.0F);

      this.rightZ.set(0.0F);
      this.rightScale.set(1.0F);
   }

   public static qj getInstance() {
      Module result = uq.get(qj.class);

      return (qj)result;
   }

   public boolean shouldApplyTransforms() {
      if (this.isEnabled()) {

         boolean result = this.hmiEnable.getValue();

         if (result) {
            return true;
         }
      }

      return false;
   }

   public static qj get() {
      qj result = uq.get(qj.class);

      return result;
   }

   public void resetLeft() {
      this.leftX.set(0.0F);
      this.leftY.set(0.0F);
      this.leftZ.set(0.0F);

      this.leftScale.set(1.0F);
   }

   public void applyHandScale(MatrixStack matrices, Arm arm) {
      boolean result = this.isEnabled();

      if (result && this.hmiEnable.getValue()) {
         float v3 = arm == Arm.field_6183 ? this.rightScale.getValue() : this.leftScale.getValue();
         if (v3 != 1.0F) {

            matrices.method_22905(v3, v3, v3);
         }
      }
   }

   public HoldMyItems() {
      this.hmiAnimationType = new ModeSetting(this, "Тип анимации", "Шарп", "Обычный");
      this.hmiSmoothness = new SliderSetting(this, "Плавность", 1.0F, 0.1F, 6.0F, 0.05F);
      this.swapHands = new BooleanSetting(this, "Поменять руки", false);
      this.climbAndCrawl = new BooleanSetting(this, "Ползание/Лазание", true);
      this.swimmingAnimation = new BooleanSetting(this, "Анимация плавания", true);
      this.mb3DCompat = new BooleanSetting(this, "3D Совместимость", false);
      this.handsPosTitle = new uf(this, "Положение рук");
      this.rightTitle = new uf(this, "Правая рука");
      this.rightX = new SliderSetting(this, "Правая X", 0.0F, -2.0F, 2.0F, 0.05F);
      this.rightY = new SliderSetting(this, "Правая Y", 0.0F, -2.0F, 2.0F, 0.05F);
      this.rightZ = new SliderSetting(this, "Правая Z", 0.0F, -2.0F, 2.0F, 0.05F);
      this.rightScale = new SliderSetting(this, "Правая размер", 1.0F, 0.1F, 3.0F, 0.05F);
      this.resetRightButton = new sf(this, "Сбросить правую", this::resetRight);
      this.leftTitle = new uf(this, "Левая рука");
      this.leftX = new SliderSetting(this, "Левая X", 0.0F, -2.0F, 2.0F, 0.05F);
      this.leftY = new SliderSetting(this, "Левая Y", 0.0F, -2.0F, 2.0F, 0.05F);
      this.leftZ = new SliderSetting(this, "Левая Z", 0.0F, -2.0F, 2.0F, 0.05F);
      this.leftScale = new SliderSetting(this, "Левая размер", 1.0F, 0.1F, 3.0F, 0.05F);
      this.resetLeftButton = new sf(this, "Сбросить левую", this::resetLeft);
      this.resetAllButton = new sf(this, "Сбросить всё", this::resetAll);
   }

   public void resetAll() {
      this.resetRight();

      this.resetLeft();
   }

   public void applyHandTranslation(MatrixStack matrices, Arm arm) {
      if (this.isEnabled() && this.hmiEnable.getValue()) {
         float result = arm == Arm.field_6183 ? this.rightX.getValue() : this.leftX.getValue();

         float v3 = result;
         float v4 = arm == Arm.field_6183 ? this.rightY.getValue() : this.leftY.getValue();
         float v5 = arm == Arm.field_6183 ? this.rightZ.getValue() : this.leftZ.getValue();
         if (v3 != 0.0F || v4 != 0.0F || v5 != 0.0F) {
            matrices.method_46416(v3, v4, v5);
         }
      }
   }

   
}
