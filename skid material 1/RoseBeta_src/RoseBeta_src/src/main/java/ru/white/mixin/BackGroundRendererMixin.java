package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.white.utils.misc.Up;
import ru.white.event.impl.EventRender2D;

@Environment(EnvType.CLIENT)
@Mixin({FogRenderer.class})
public class BackGroundRendererMixin {
   @Unique
   private static final ThreadLocal<EventRender2D> FOG_EVENT = new ThreadLocal<>();

   @Inject(
      method = {"method_3211"},
      at = {@At("HEAD")}
   )
   private void onApplyFogHead(
      Camera camera, int fogType, RenderTickCounter tickCounter, float viewDistance, ClientWorld world, CallbackInfoReturnable<Vector4f> cir
   ) {
      EventRender2D event = new EventRender2D();
      event.hook();
      if (event.isCancelled()) {
         FOG_EVENT.set(event);
      } else {
         FOG_EVENT.remove();
      }
   }

   @ModifyArgs(
      method = {"method_3211"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
      )
   )
   private void modifyFogArgs(Args args) {
      EventRender2D event = FOG_EVENT.get();
      if (event != null && event.isCancelled()) {
         int color = event.getColor();
         Vector4f fogColor = (Vector4f)args.get(2);
         fogColor.set(up.redf(color), up.greenf(color), up.bluef(color), up.alphaf(color));
         float start = 2.0F;
         float end = event.getDistance();
         args.set(3, start);
         args.set(4, end);
         args.set(7, end);
         args.set(8, end);
      }
   }

   @Inject(
      method = {"method_3211"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onApplyFogReturn(
      Camera camera, int fogType, RenderTickCounter tickCounter, float viewDistance, ClientWorld world, CallbackInfoReturnable<Vector4f> cir
   ) {
      EventRender2D event = FOG_EVENT.get();
      if (event != null && event.isCancelled()) {
         int color = event.getColor();
         cir.setReturnValue(new Vector4f(up.redf(color), up.greenf(color), up.bluef(color), up.alphaf(color)));
      }

      FOG_EVENT.remove();
   }
}
