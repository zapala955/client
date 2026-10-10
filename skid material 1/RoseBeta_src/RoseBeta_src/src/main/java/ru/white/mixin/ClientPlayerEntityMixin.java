package ru.white.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.player.LockSlot;
import ru.white.module.impl.player.NoPush;
import ru.white.Client;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventMove;
import ru.white.event.EventCancelable;
import ru.white.event.impl.EventWorldChange;
import ru.white.event.impl.EventBlockBreak;
import ru.white.event.impl.EventTick;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
   @Shadow
   public float field_3916;
   @Shadow
   public float field_3932;
   @Shadow
   public float field_3914;
   @Shadow
   public float field_3931;

   @Inject(
      method = {"method_5773"},
      at = {@At("HEAD")}
   )
   private void onTickHead(CallbackInfo ci) {
      new EventTick().hook();
   }

   @Shadow
   public abstract boolean method_6115();

   public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
      super(world, profile);
   }

   @Redirect(
      method = {"method_67270"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_241;method_35582(F)Lnet/minecraft/class_241;",
         ordinal = 1
      )
   )
   private Vec2f cancelItemSlowdown(Vec2f vec2f, float multiplier) {
      EventBlockBreak event = new EventBlockBreak((byte)1);
      event.hook();
      return event.isCancelled() && this.method_6115() && !this.method_5765() ? vec2f.method_35582(1.0F) : vec2f.method_35582(multiplier);
   }

   @ModifyVariable(
      method = {"method_46742"},
      at = @At(
         value = "STORE",
         ordinal = 0
      ),
      ordinal = 0
   )
   private boolean modifySprintingFlag(boolean flag) {
      EventCancelable actionEvent = new EventCancelable(flag);
      actionEvent.hook();
      return actionEvent.isSprintState();
   }

   @Inject(
      method = {"method_66282"},
      at = {@At("TAIL")}
   )
   private void fixCameraRenderAngles(CallbackInfo ci) {
      MinecraftClient EventItemUse = MinecraftClient.method_1551();
      if (mc.field_1724 != null && mc.field_1724 == this) {
         Camera camera = mc.field_1773.method_19418();
         this.field_3916 = this.field_3914 + (camera.method_19329() - this.field_3914) * 0.5F;
         this.field_3932 = this.field_3931 + (camera.method_19330() - this.field_3931) * 0.5F;
      }
   }

   @Inject(
      method = {"method_3136"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preMotion(CallbackInfo ci) {
      EventMove event = new EventMove(
         this.method_23317(), this.method_23318(), this.method_23321(), (double)this.method_5705(1.0F), (double)this.method_5695(1.0F), this.method_24828()
      );
      event.hook();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_30673"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      ni noPush = Client.get().moduleManager().get(ni.class);
      if (noPush.block.getValue() && noPush.isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_7290"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      li lockSlot = Client.get().moduleManager().get(li.class);
      if (lockSlot != null && lockSlot.isEnabled()) {
         int slot = hp.mc.field_1724.method_31548().method_67532();
         if (lockSlot.isSlotLocked(slot) || lockSlot.isDropProtected(hp.mc.field_1724.method_6047(), slot)) {
            cir.setReturnValue(false);
         }
      }
   }

   @Shadow
   protected abstract void method_3148(float v1, float v2);

   @Inject(
      method = {"method_5784"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_742;method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"
      )},
      cancellable = true
   )
   public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      EventWorldChange event = new EventWorldChange(movement);
      event.hook();
      double d = this.method_23317();
      double e = this.method_23321();
      super.method_5784(movementType, event.getMovement());
      this.method_3148((float)(this.method_23317() - d), (float)(this.method_23321() - e));
      ci.cancel();
   }
}
