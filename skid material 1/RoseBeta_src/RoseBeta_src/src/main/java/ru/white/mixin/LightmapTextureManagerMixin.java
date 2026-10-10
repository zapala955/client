package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.utils.misc.Up;
import ru.white.Client;
import ru.white.module.impl.render.NoRender;
import ru.white.module.impl.render.Gamma;
import ru.white.module.impl.render.WorldTweaks;

@Environment(EnvType.CLIENT)
@Mixin({LightmapTextureManager.class})
public class LightmapTextureManagerMixin {
   @Shadow
   private boolean field_4135;
   @Unique
   private boolean prevUsesWorldColor = false;

   @Inject(
      method = {"method_3313"},
      at = {@At("HEAD")}
   )
   private void onUpdateHead(float tickDelta, CallbackInfo ci) {
      ml worldTweaks = ml.get();
      boolean active = worldTweaks != null && worldTweaks.usesWorldColor();
      if (active || this.prevUsesWorldColor) {
         this.field_4135 = true;
      }

      this.prevUsesWorldColor = active;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;",
         ordinal = 0
      )
   )
   private Vector3fc modifySkyLightColor(Vector3fc original) {
      ml worldTweaks = ml.get();
      if (worldTweaks != null && worldTweaks.usesWorldColor()) {
         int color = worldTweaks.resolvedWorldColor();
         Vector3f tint = new Vector3f((float)up.red(color) / 255.0F, (float)up.green(color) / 255.0F, (float)up.blue(color) / 255.0F);
         return new Vector3f(original).mul(tint);
      } else {
         return original;
      }
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;",
         ordinal = 1
      )
   )
   private Vector3fc modifyAmbientColor(Vector3fc original) {
      ml worldTweaks = ml.get();
      if (worldTweaks != null && worldTweaks.usesWorldColor()) {
         int color = worldTweaks.resolvedWorldColor();
         Vector3f tint = new Vector3f((float)up.red(color) / 255.0F, (float)up.green(color) / 255.0F, (float)up.blue(color) / 255.0F);
         return new Vector3f(original).mul(tint);
      } else {
         return original;
      }
   }

   @Redirect(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Ljava/lang/Double;floatValue()F",
         ordinal = 1
      )
   )
   private float night$getGammaValue(Double instance) {
      if (Client.get() != null) {
         jj module = Client.get().moduleManager().get(jj.class);
         if (module != null && module.isEnabled()) {
            return 200.0F;
         }
      }

      return instance.floatValue();
   }

   @Inject(
      method = {"method_42596"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         cir.setReturnValue(0.0F);
      }
   }
}
