package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.render.NameTag;

@Environment(EnvType.CLIENT)
@Mixin({EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(
      method = {"method_3926"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderLabelIfPresent(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled()) {
         if (state.field_58171 == EntityType.field_6097 && esp.player.getValue()) {
            if (state.field_53337 == null) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"method_62426"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookNametag(T entity, CallbackInfoReturnable<Text> cir) {
      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled()) {
         if (entity instanceof PlayerEntity p && esp.player.getValue()) {
            if (esp.ignoreNaked.getValue() && !NameTag.hasArmor(p)) {
               return;
            }

            cir.setReturnValue(null);
         }
      }
   }
}
