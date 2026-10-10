package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "China Hat",
   desc = "Китайская шляпа",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class ChinaHat extends Module {
   public static vi getInstance() {
      vi result = uq.get(vi.class);

      return result;
   }

   
}
