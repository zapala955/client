package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "ItemPhysics",
   category = Category.RENDER,
   desc = "Выброшенные предметы лежат на земле"
)
@Environment(EnvType.CLIENT)
public class ItemPhysics extends Module {
   public static uj get() {

      return uq.get(uj.class);
   }

   
}
