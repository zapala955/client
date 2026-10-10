package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.PvpSafe;

@Environment(EnvType.CLIENT)
@Mixin({GameMenuScreen.class})
public class GameMenuScreenMixin {
   @Shadow
   private ButtonWidget field_40792;

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   private void pvpSafe$disableExit(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.field_40792 != null) {
         this.field_40792.field_22763 = !oi.isActive();
      }
   }
}
