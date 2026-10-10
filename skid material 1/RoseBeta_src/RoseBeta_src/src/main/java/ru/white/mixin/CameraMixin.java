package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.render.NoRender;
import ru.white.event.impl.EventSendPacket;
import ru.white.event.impl.EventRaytrace;

@Environment(EnvType.CLIENT)
@Mixin({Camera.class})
public abstract class CameraMixin {
   @Unique
   private EventRaytrace night$rotationEvent;
   @Unique
   private float night$originalYaw;
   @Unique
   private float night$originalPitch;
   @Unique
   private boolean night$inverseView;
   @Shadow
   private Vec3d field_18712;
   @Shadow
   @Final
   private Mutable field_18713;

   @Inject(
      method = {"method_19321"},
      at = {@At("HEAD")}
   )
   private void onUpdateHead(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
      this.night$inverseView = thirdPerson && inverseView;
      if (focusedEntity != null) {
         this.night$originalYaw = focusedEntity.method_5705(tickProgress);
         this.night$originalPitch = focusedEntity.method_5695(tickProgress);
         this.night$rotationEvent = new EventRaytrace(this.night$originalYaw, this.night$originalPitch, tickProgress);
         this.night$rotationEvent.hook();
      } else {
         this.night$rotationEvent = null;
      }
   }

   @Inject(
      method = {"method_19318"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.noCameraClip.getValue()) {
         cir.setReturnValue(desiredCameraDistance);
      }
   }

   @Shadow
   public abstract void method_19325(float v1, float v2);

   @Redirect(
      method = {"method_19321"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19325(FF)V"
      )
   )
   private void redirectSetRotation(Camera instance, float yaw, float pitch) {
      if (this.night$rotationEvent != null
         && (this.night$rotationEvent.getYaw() != this.night$originalYaw || this.night$rotationEvent.getPitch() != this.night$originalPitch)) {
         if (this.night$inverseView) {
            this.method_19325(this.night$rotationEvent.getYaw() + 180.0F, -this.night$rotationEvent.getPitch());
         } else {
            this.method_19325(this.night$rotationEvent.getYaw(), this.night$rotationEvent.getPitch());
         }
      } else {
         this.method_19325(yaw, pitch);
      }
   }

   @Inject(
      method = {"method_19322"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void posHook(Vec3d pos, CallbackInfo ci) {
      EventSendPacket event = new EventSendPacket(pos);
      event.hook();
      this.field_18712 = pos = event.getPos();
      this.field_18713.method_10102(pos.field_1352, pos.field_1351, pos.field_1350);
      ci.cancel();
   }
}
