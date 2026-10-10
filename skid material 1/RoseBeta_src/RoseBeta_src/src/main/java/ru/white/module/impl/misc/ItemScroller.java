package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "Item Scroller",
   desc = "Помогает скролить предметы",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class ItemScroller extends Module {
   public SliderSetting delay = new SliderSetting(this, "Задержка", 50.0F, 0.0F, 100.0F, 1.0F);

   public static km getInstance() {
      Module result = uq.get(km.class);

      return (km)result;
   }

   
}
