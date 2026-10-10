package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;
import ru.white.theme.Theme;
import ru.white.theme.xo;
import ru.white.event.impl.EventPostMotion;

@ModuleAnnotation(
   name = "Click Gui",
   category = Category.RENDER,
   key = 344,
   desc = "Позволяет настроить вид GUI клиента",
   autoEnabled = true,
   allowDisable = false
)
@Environment(EnvType.CLIENT)
public class ClickGui extends Module {
   public ModeSetting guiType = new ModeSetting(this, "Вид меню", "Обычное", "Drop Down", "Test");
   public sf openThemeEditor;
   public SliderSetting size;
   public SliderSetting blurStrength;
   public MultiSetting effect;
   public BooleanSetting smoothAnimations;
   public BooleanSetting singleCategory;
   public SliderSetting glowIntensity;
   public BooleanSetting distortion;
   public BooleanSetting sound;

   @EventTarget
   public void onKey(EventPostMotion event) {
      if (event.getKey() == this.getKey()) {

         mc.method_1507(Client.get.getClickGuiScreen());
      }
   }

   public ClickGui() {
      BooleanSetting[] temp5 = new BooleanSetting[8];
      temp5[0] = new BooleanSetting("Серый фон", false);
      temp5[1] = new BooleanSetting("Затемнять фон", true);
      temp5[2] = new BooleanSetting("Размывать фон", true);
      temp5[3] = new BooleanSetting("Шейдер", false);
      temp5[4] = new BooleanSetting("Частицы", true);
      temp5[5] = new BooleanSetting("Скан линии", true);
      temp5[6] = new BooleanSetting("Свечение", true);
      temp5[7] = new BooleanSetting("Точки", true);
      this.effect = new MultiSetting(this, "Эффекты", temp5);
      this.size = new SliderSetting(this, "Размер", 1.0F, 0.5F, 1.5F, 0.05F);
      this.sound = new BooleanSetting(this, "Звуки интерфейса", true);
      this.blurStrength = new SliderSetting(this, "Сила размытия", 15.0F, 5.0F, 30.0F, 1.0F);
      this.glowIntensity = new SliderSetting(this, "Яркость свечения", 1.0F, 0.0F, 2.0F, 0.1F);
      this.distortion = new BooleanSetting(this, "Стеклянное искажение", false);
      this.smoothAnimations = new BooleanSetting(this, "Плавные анимации", true);
      this.singleCategory = new BooleanSetting(this, "Одиночная категория", true);
      this.openThemeEditor = new sf(
         this, "Открыть редактор кастом темы", () -> {
            if (mc.field_1755 instanceof ko v0) {
               v0.openThemeEditor();
            } else {
               Screen v4 = mc.field_1755;
               boolean result = v4 instanceof TestGui;

               if (result) {
                  TestGui v1 = (TestGui)v4;
                  v1.openThemeEditor();
               }
            }
         }
      );
      this.blurStrength.onAction(() -> {
         try {
            float result = this.blurStrength.getValue();

            float v1 = result;
            Theme v5 = xo.get().getActive();
            float temp2 = v1 / 5.0F;

            v5.setSlider(1, temp2);
            xo.get().saveTheme(xo.get().getActive());
         } catch (Exception v4) {
         }
      });
      this.distortion.onAction(() -> {
         try {
            xo.get().getActive().setBool(1, this.distortion.getValue());
            xo result = xo.get();
            Theme temp1 = xo.get().getActive();

            result.saveTheme(temp1);
         } catch (Exception v5) {
         }
      });
   }

   
}
