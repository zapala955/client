package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.render.ItemPhysics;
import ru.white.utils.render.is;

@Environment(EnvType.CLIENT)
@Mixin({ItemEntityRenderer.class})
public abstract class ItemEntityRendererMixin {
   private static final float TUMBLE_SPIN_DEGREES = 300.0F;
   private static final float FLAT_ROTATION_DEGREES = 90.0F;
   private static final float GROUND_EPSILON = 0.002F;

   @Inject(
      method = {"method_62470"},
      at = {@At("TAIL")}
   )
   private void captureOnGround(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
      if (state instanceof is access) {
         access.setOnGround(entity.method_24828());
      }
   }

   @Redirect(
      method = {"method_3996"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_46416(FFF)V"
      )
   )
   private void skipHoverTranslate(
      MatrixStack matrices,
      float x,
      float y,
      float z,
      ItemEntityRenderState state,
      MatrixStack matricesArg,
      OrderedRenderCommandQueue queue,
      CameraRenderState cameraState
   ) {
      if (!physicsApplies(state)) {
         matrices.method_46416(x, y, z);
      }
   }

   @Redirect(
      method = {"method_3996"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V"
      )
   )
   private void applyPhysicsTransform(
      MatrixStack matrices,
      Quaternionfc spinRotation,
      ItemEntityRenderState state,
      MatrixStack matricesArg,
      OrderedRenderCommandQueue queue,
      CameraRenderState cameraState
   ) {
      if (!physicsApplies(state)) {
         matrices.method_22907(spinRotation);
      } else {
         Box box;
         float yaw;
         boolean result;
         label20: {
            box = state.field_55310.method_72173();
            yaw = (float)((state.field_55312 % 720) % 360);
            if (state instanceof is access && access.isOnGround()) {
               result = true;
               break label20;
            }

            result = false;
         }

         boolean onGround = result;
         if (onGround) {
            matrices.method_46416(0.0F, (float)box.field_1324 + 0.002F, 0.0F);
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(yaw));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(90.0F));
         } else {
            float centerY = (float)((box.field_1322 + box.field_1325) * 0.5);
            float halfHeight = (float)((box.field_1325 - box.field_1322) * 0.5);
            float spin = ItemEntity.method_27314(state.field_53328, state.field_53435);
            matrices.method_46416(0.0F, halfHeight, 0.0F);
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(yaw));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(spin * 300.0F));
            matrices.method_46416(0.0F, -centerY, 0.0F);
         }
      }
   }

   @Unique
   private static boolean physicsApplies(ItemEntityRenderState state) {
      uj itemPhysics = uj.get();
      return itemPhysics != null && itemPhysics.isEnabled() && !state.field_55310.method_65606();
   }
}
