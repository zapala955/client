package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.render.NoRender;

@Environment(EnvType.CLIENT)
@Mixin({LavaFogModifier.class})
public class LavaFogModifierMixin {
   @Inject(
      method = {"method_42591"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApplyStartEnd(FogData fogData, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreLava.getValue()) {
         fogData.field_60582 = Float.MAX_VALUE;
         fogData.field_60584 = Float.MAX_VALUE;
         ci.cancel();
      }
   }
}
