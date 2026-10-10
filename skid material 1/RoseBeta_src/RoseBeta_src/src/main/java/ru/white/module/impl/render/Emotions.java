package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import ru.white.utils.helper.La;
import ru.white.utils.helper.Ra;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventReceivePacket;

@ModuleAnnotation(
   name = "Emotions",
   category = Category.RENDER,
   desc = "Колесо эмоций и кастомных анимаций"
)
@Environment(EnvType.CLIENT)
public class Emotions extends Module {
   public final SettingVisibility wheelBind = new SettingVisibility(this, "Кнопка колеса", 66);

   @EventTarget
   public void onDisplay(EventReceivePacket event) {

      if (this.isEnabled()) {
         ra.render(event.getDrawContext());
      }
   }

   public static Emotions get() {
      Module result = uq.get(Emotions.class);

      return (Emotions)result;
   }

   @Override
   public void onDisable() {
      boolean result = ra.isOpen();

      if (result) {
         ra.requestCancel();
         ra.close(MinecraftClient.method_1551());
      }

      if (mc.field_1724 != null) {
         la.INSTANCE.stop(mc.field_1724.method_5667());
      }
   }

   
}
