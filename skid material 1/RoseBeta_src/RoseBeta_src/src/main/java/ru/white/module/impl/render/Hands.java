package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Uf;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;

@ModuleAnnotation(
   name = "Hands",
   desc = "Положение и размер рук от первого лица",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Hands extends Module {
   public SliderSetting ry;
   public sf openEditor = new sf(this, "Редактор рук", () -> {
      Screen v3 = mc.field_1755;
      if (v3 instanceof ko) {

         ko v0 = (ko)v3;

         v0.openHandsEditor();
      } else if (mc.field_1755 instanceof DropDownMenu v1) {
         v1.openHandsEditor();
      } else if (mc.field_1755 instanceof TestGui v2) {
         v2.openHandsEditor();
      }
   });
   public sf resetRightButton;
   private boolean f673;
   public sf resetLeftButton;
   public SliderSetting lz;
   public SliderSetting rz;
   public SliderSetting rx;
   public SliderSetting rScale;
   public uf rightTitle = new uf(this, "Правая рука");
   public SliderSetting ly;
   public sf resetAllButton;
   public SliderSetting lScale;
   public SliderSetting lx;
   public uf leftTitle;
   private boolean f674;
   private boolean f672;

   public void resetRight() {
      this.rx.set(0.0F);
      this.ry.set(0.0F);

      this.rz.set(0.0F);
      this.rScale.set(1.0F);
   }

   public void applyEmptyHandAlignment(MatrixStack matrices, Arm arm) {
      if (this.shouldApplyTransforms()) {
         float temp1 = arm == Arm.field_6183 ? -0.08F : 0.08F;

         matrices.method_46416(temp1, 0.08F, 0.0F);
      }
   }

   public void applyHandScale(MatrixStack matrices, Arm arm) {
      if (this.shouldApplyTransforms()) {
         Float result;
         if (arm == Arm.field_6183) {
            result = this.rScale.getValue();
            int temp1 = v4 * (v4 + 1) % 2;

            if (temp1 != 0) {
               }
         } else {
            result = this.lScale.getValue();
         }

         float v3 = result;

         matrices.method_22905(v3, v3, v3);
      }
   }

   public boolean isEditorActive() {

      return this.f672;
   }

   public void applyHandTranslation(MatrixStack matrices, Arm arm) {
      if (this.shouldApplyTransforms()) {
         float v3 = arm == Arm.field_6183 ? this.rx.getValue() : this.lx.getValue();
         Float result;
         if (arm == Arm.field_6183) {

            result = this.ry.getValue();
         } else {
            result = this.ly.getValue();
         }

         float v4 = result;
         float v5 = arm == Arm.field_6183 ? this.rz.getValue() : this.lz.getValue();

         matrices.method_46416(v3, v4, v5);
      }
   }

   @Override
   protected void onEnable() {
      xk v1 = xk.get();
      if (this.f673 && v1 != null && !v1.isEnabled()) {
         v1.setEnabled(true, false);
      }

      GlassHands v2 = GlassHands.getInstance();
      if (this.f674 && v2 != null) {

         if (!v2.isEnabled()) {

            v2.setEnabled(true, false);
         }
      }
   }

   @Override
   protected void onDisable() {
      xk v1 = xk.get();
      this.f673 = v1 != null && v1.isEnabled();
      if (v1 != null && v1.isEnabled()) {
         v1.setEnabled(false, false);
      }

      GlassHands v2 = GlassHands.getInstance();
      this.f674 = v2 != null && v2.isEnabled();
      if (v2 != null) {

         if (v2.isEnabled()) {
            v2.setEnabled(false, false);
         }
      }
   }

   public boolean shouldApplyTransforms() {
      if (!this.isEnabled()) {

         if (!this.f672) {
            return false;
         }
      }

      return true;
   }

   public void resetLeft() {
      this.lx.set(0.0F);

      this.ly.set(0.0F);
      this.lz.set(0.0F);
      this.lScale.set(1.0F);
   }

   public Hands() {
      this.rx = new SliderSetting(this, "Правая X", 0.0F, -2.0F, 2.0F, 0.05F);
      this.ry = new SliderSetting(this, "Правая Y", 0.0F, -2.0F, 2.0F, 0.05F);
      this.rz = new SliderSetting(this, "Правая Z", 0.0F, -2.0F, 2.0F, 0.05F);
      this.rScale = new SliderSetting(this, "Правая размер", 1.0F, 0.1F, 3.0F, 0.05F);
      this.resetRightButton = new sf(this, "Сбросить правую", this::resetRight);
      this.leftTitle = new uf(this, "Левая рука");
      this.lx = new SliderSetting(this, "Левая X", 0.0F, -2.0F, 2.0F, 0.05F);
      this.ly = new SliderSetting(this, "Левая Y", 0.0F, -2.0F, 2.0F, 0.05F);
      this.lz = new SliderSetting(this, "Левая Z", 0.0F, -2.0F, 2.0F, 0.05F);
      this.lScale = new SliderSetting(this, "Левая размер", 1.0F, 0.1F, 3.0F, 0.05F);
      this.resetLeftButton = new sf(this, "Сбросить левую", this::resetLeft);
      this.resetAllButton = new sf(this, "Сбросить всё", this::resetAll);
   }

   public static oj get() {
      Module result = uq.get(oj.class);

      return (oj)result;
   }

   public void resetAll() {
      int temp2 = v1 + 1;

      this.resetRight();
      this.resetLeft();
   }

   public void setEditorActive(boolean editorActive) {

      this.f672 = editorActive;
   }

   
}
