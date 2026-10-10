package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.player.NoPush;
import ru.white.Client;
import ru.white.module.impl.combat.HitBoxes;
import ru.white.module.impl.render.ShaderESP;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
@Mixin({Entity.class})
public class EntityMixin implements IMinecraft {
   @ModifyExpressionValue(
      method = {"method_5784"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_66247()Z",
         ordinal = 1
      )}
   )
   public boolean fixFalldistanceValue(boolean original) {
      return this == mc.field_1724 ? true : original;
   }

   @Inject(
      method = {"method_5871"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void client$getTargetingMargin(CallbackInfoReturnable<Float> cir) {
      Entity self = (Entity)this;
      if (self instanceof PlayerEntity) {
         if (lg.get().isEnabled()) {
            if (self instanceof PlayerEntity player && Client.get().friendManager().isFriend(player.method_5477().getString())) {
               return;
            }

            float base = (Float)cir.getReturnValue();
            float extra = lg.get().size.getValue();
            cir.setReturnValue(base + extra);
         }
      }
   }

   @Inject(
      method = {"method_5851"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void espMarkGlowing(CallbackInfoReturnable<Boolean> cir) {
      if (!(Boolean)cir.getReturnValue()) {
         ok esp = ok.getInstance();
         if (esp != null && esp.isEnabled() && ok.isEspTarget((Entity)this)) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(
      method = {"method_22861"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void espFriendTeamColor(CallbackInfoReturnable<Integer> cir) {
      ok esp = ok.getInstance();
      if (esp != null && esp.isEnabled()) {
         Entity self = (Entity)this;
         if (self instanceof PlayerEntity p) {
            if (Client.get().friendManager().isFriend(p.method_5477().getString())) {
               cir.setReturnValue(5635925);
            }
         }
      }
   }

   @Inject(
      method = {"method_5697"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
      Entity self = (Entity)this;
      if (self instanceof ClientPlayerEntity) {
         ni noPush = ni.get();
         if (noPush != null && noPush.isEnabled()) {
            if (entity instanceof PlayerEntity && noPush.entity.getValue()) {
               ci.cancel();
            } else if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity) && noPush.entity.getValue()) {
               ci.cancel();
            }
         }
      }
   }
}
