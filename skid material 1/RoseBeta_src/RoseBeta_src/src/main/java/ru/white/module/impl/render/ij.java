package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.yr;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Fog Blur",
   category = Category.RENDER,
   desc = "Размывает мир за порогом дистанции тумана"
)
@Environment(EnvType.CLIENT)
public final class ij extends Module {
   private final SliderSetting f655;
   private final BooleanSetting f656;
   private final SliderSetting f654 = new SliderSetting(this, "Дистанция", 0.05F, 0.001F, 0.5F, 0.001F);

   @EventTarget(
      priority = 500
   )
   public void onRender(EventTrace e) {
      if (this.isEnabled()) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            int v2 = up.getClientColor1(1);
            int v3 = up.getClientColor1(1);
            int result = 270;

            int v4 = up.getClientColor1(result);
            int v5 = up.getClientColor1(270);
            yr.draw(this.f654.getValue(), Math.max(0.0F, Math.min(1.0F, 1.0F - this.f655.getValue())), this.f656.getValue(), v2, v3, v4, v5);
         }
      }
   }

   public ij() {
      this.f655 = new SliderSetting(this, "Насыщенность", 0.5F, 0.05F, 0.95F, 0.05F);
      this.f656 = new BooleanSetting(this, "Цвет клиента", false);
   }

   
}
