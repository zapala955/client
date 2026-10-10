package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.event.Event;
import ru.white.event.impl.EventBlockBreak;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"method_2919"},
      at = {@At("RETURN")}
   )
   public void interactItemHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (cir.getReturnValue() instanceof Success success && !success.comp_2909().equals(SwingSource.field_52427)) {
         EventBlockBreak event = new EventBlockBreak((byte)0);
         event.hook();
      }
   }

   @Inject(
      method = {"method_2918"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
      Event event = new Event(target);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2897"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void stopUsingItemHook(CallbackInfo ci) {
      EventBlockBreak event = new EventBlockBreak((byte)2);
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2919"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void gameModeHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      EventBlockBreak event = new EventBlockBreak((byte)-1);
      event.hook();
      if (event.isCancelled()) {
         cir.setReturnValue(ActionResult.field_5811);
      }
   }
}
