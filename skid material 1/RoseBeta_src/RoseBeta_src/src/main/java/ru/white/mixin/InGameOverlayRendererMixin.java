package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.render.NoRender;

@Environment(EnvType.CLIENT)
@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"method_23070"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderFireOverlayHook(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender.isEnabled() && noRender.ignoreFire.getValue()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_23068"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderInWallOverlayHook(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender.isEnabled() && noRender.ignoreZalupa.getValue()) {
         ci.cancel();
      }
   }
}
