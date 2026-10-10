package ru.white.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.render.NoRender;
import ru.white.module.impl.render.ShaderESP;
import ru.white.module.impl.render.ShaderSky;
import ru.white.module.impl.render.Atmosphere;
import ru.white.utils.render.it;
import ru.white.utils.render.kt;
import ru.white.utils.render.pr;
import ru.white.event.impl.EventTrace;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public class WorldRendererMixin {
   @Shadow
   @Nullable
   private Framebuffer field_53080;

   @Inject(
      method = {"method_3254"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void espCancelOutlineDraw(CallbackInfo ci) {
      ok esp = ok.getInstance();
      if (esp != null && esp.isEnabled() && !ok.getAllTargets().isEmpty()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_62215"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_9975;method_62302(I)V",
         shift = Shift.AFTER
      )},
      require = 0
   )
   private static void renderShaderSkyLayer(GpuBufferSlice fogBuffer, SkyRenderState skyRenderState, SkyRendering skyRendering, CallbackInfo ci) {
      pr.getInstance().setSkyRenderState(skyRenderState);
      pk shaderSky = pk.getInstance();
      if (shaderSky != null && shaderSky.isEnabled() && !shaderSky.mode.is("Blur")) {
         it.getInstance().renderCelestialShader();
      }
   }

   @Inject(
      method = {"method_22710"},
      at = {@At("RETURN")}
   )
   private void publishWorldRenderEvent(
      ObjectAllocator allocator,
      RenderTickCounter tickCounter,
      boolean renderBlockOutline,
      Camera camera,
      Matrix4f positionMatrix,
      Matrix4f basicProjectionMatrix,
      Matrix4f projectionMatrix,
      GpuBufferSlice fogBuffer,
      Vector4f fogColor,
      boolean renderSky,
      CallbackInfo ci
   ) {
      MatrixStack stack = new MatrixStack();
      Matrix4f basePositionMatrix = new Matrix4f(positionMatrix);
      stack.method_34425(new Matrix4f(basePositionMatrix));
      new EventTrace(stack, tickCounter.method_60637(true), projectionMatrix).hook();
      ok.getInstance().customFboReference = this.field_53080;
      if (ok.getInstance().isEnabled()) {
         ok.getInstance().onRender3D();
      }

      pk shaderSky = pk.getInstance();
      if (shaderSky != null && shaderSky.isEnabled()) {
         it.getInstance().renderSky();
      }

      qi atmosphere = qi.getInstance();
      if (atmosphere != null && (atmosphere.usesAtmosphere() || kt.INSTANCE.hasVisibleStrike())) {
         pr.getInstance().render(atmosphere, camera, positionMatrix, projectionMatrix);
      }
   }

   @Inject(
      method = {"method_43788"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled()) {
         if (camera.method_19331() instanceof LivingEntity livingEntity) {
            boolean hasBlindness = livingEntity.method_6059(StatusEffects.field_5919);
            boolean hasDarkness = livingEntity.method_6059(StatusEffects.field_38092);
            if (noRender.ignoreZalupa.getValue() && hasBlindness && !hasDarkness) {
               cir.setReturnValue(false);
            }

            if (noRender.ignoreZalupa.getValue() && hasDarkness && !hasBlindness) {
               cir.setReturnValue(false);
            }

            if (noRender.ignoreZalupa.getValue()) {
               cir.setReturnValue(false);
            }
         }
      }
   }
}
