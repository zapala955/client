package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.player.NoDelay;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventFOV;
import ru.white.event.impl.EventPreMotion;

@Environment(EnvType.CLIENT)
@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin implements IMinecraft {
   @Shadow
   public abstract boolean method_6059(RegistryEntry<StatusEffect> v1);

   @Shadow
   @Nullable
   public abstract StatusEffectInstance method_6112(RegistryEntry<StatusEffect> v1);

   @ModifyConstant(
      method = {"method_6007"},
      constant = {@Constant(
         intValue = 10
      )}
   )
   private int modifyJumpTicks(int original) {
      LivingEntity self = (LivingEntity)this;
      if (self instanceof ClientPlayerEntity) {
         mi noDelay = mi.get();
         if (noDelay != null && noDelay.isEnabled() && noDelay.jump.getValue()) {
            return fq.randomInt(0, 3);
         }
      }

      return original;
   }

   @Inject(
      method = {"method_6028"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
      if (this == mc.field_1724) {
         EventFOV event = new EventFOV();
         event.hook();
         if (event.isCancelled()) {
            float animation = event.getAnimation();
            if (StatusEffectUtil.method_5576(mc.field_1724)) {
               animation *= (float)(6 - (1 + StatusEffectUtil.method_5575(mc.field_1724)));
            } else {
               animation *= (float)(this.method_6059(StatusEffects.field_5901) ? 6 + (1 + this.method_6112(StatusEffects.field_5901).method_5578()) * 2 : 6);
            }

            cir.setReturnValue((int)animation);
         }
      }
   }

   @Inject(
      method = {"method_6043"},
      at = {@At("HEAD")}
   )
   public void jumpYo(CallbackInfo ci) {
      LivingEntity self = (LivingEntity)this;
      if (self instanceof ClientPlayerEntity) {
         new EventPreMotion().hook();
      }
   }
}
