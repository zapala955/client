package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.render.WorldTweaks;

@Environment(EnvType.CLIENT)
@Mixin({AtmosphericFogModifier.class})
public class AtmosphericFogModifierMixin {
   @Inject(
      method = {"method_71654"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetFogColor(ClientWorld world, Camera camera, int viewDistance, float tickProgress, CallbackInfoReturnable<Integer> cir) {
      ml worldTweaks = ml.get();
      if (worldTweaks != null && worldTweaks.isEnabled() && worldTweaks.fogs.getValue()) {
         cir.setReturnValue(worldTweaks.getColor());
      }
   }

   @Inject(
      method = {"method_42591"},
      at = {@At("RETURN")}
   )
   private void onApplyStartEnd(FogData fogData, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
      ml worldTweaks = ml.get();
      if (worldTweaks != null && worldTweaks.isEnabled() && worldTweaks.fogs.getValue()) {
         float end = worldTweaks.fog.getValue();
         fogData.field_60582 = Math.min(2.0F, end * 0.25F);
         fogData.field_60584 = end;
         fogData.field_60099 = end;
         fogData.field_60100 = end;
      }
   }
}
