package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.white.event.impl.EventItemUse;

@Environment(EnvType.CLIENT)
@Mixin({TextVisitFactory.class})
public class TextVisitFactoryMixin {
   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_5223;method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z",
         ordinal = 0
      ),
      method = {"method_27472"},
      index = 0
   )
   private static String adjustText(String text) {
      EventItemUse event = new EventItemUse(text);
      event.hook();
      return event.getText();
   }
}
