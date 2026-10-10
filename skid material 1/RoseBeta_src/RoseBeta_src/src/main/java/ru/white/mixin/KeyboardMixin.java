package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.utils.helper.Ra;
import ru.white.module.impl.render.Emotions;
import ru.white.event.impl.EventPostMotion;

@Environment(EnvType.CLIENT)
@Mixin({Keyboard.class})
public class KeyboardMixin {
   @Shadow
   @Final
   private MinecraftClient field_1678;

   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")}
   )
   private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
      Screen screen = this.field_1678.field_1755;
      int key = input.comp_4795();
      if (key != -1 && key != 0) {
         if (key < 0 || key > 7) {
            if (action == 1 && key == 256 && ra.isOpen()) {
               ra.requestCancel();
               ra.close(this.field_1678);
               ci.cancel();
            } else {
               Emotions emotions = Emotions.get();
               if (emotions != null && emotions.isEnabled() && screen == null && key == emotions.wheelBind.getValue()) {
                  if (action == 1) {
                     ra.open();
                  } else if (action == 0) {
                     ra.close(this.field_1678);
                  }
               }

               if (action == 1 && screen == null) {
                  EventPostMotion event = new EventPostMotion(key);
                  event.hook();
               }
            }
         }
      }
   }
}
