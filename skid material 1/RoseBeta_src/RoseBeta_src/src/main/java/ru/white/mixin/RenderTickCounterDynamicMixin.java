package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.utils.media.Jr;

@Environment(EnvType.CLIENT)
@Mixin(
   targets = {"net/minecraft/class_9779$class_9781"}
)
public class RenderTickCounterDynamicMixin implements jr {
   @Unique
   private float speed = 1.0F;
   @Shadow
   private float field_51958;

   @Override
   public float getSpeed() {
      return this.speed;
   }

   @Override
   public void setSpeed(float speed) {
      this.speed = speed;
   }

   @Inject(
      method = {"method_60639"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_9779$class_9781;field_51962:J",
         shift = Shift.AFTER
      )}
   )
   private void applySpeedToDelta(long timeMillis, CallbackInfoReturnable<Integer> cir) {
      this.field_51958 = this.field_51958 * this.speed;
   }
}
