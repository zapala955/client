package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.render.fog.FogRenderer.FogType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.Client;
import ru.white.module.impl.render.NoRender;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.GuiCheckBox;
import ru.white.screen.ClickGuiScreen;
import ru.white.utils.render.vs;
import ru.white.utils.misc.Ab;
import ru.white.utils.media.Nr;
import ru.white.event.impl.EventRender3D;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public abstract class GameRenderMixin {
   @Shadow
   @Final
   private MinecraftClient field_4015;
   @Shadow
   @Final
   private GuiRenderer field_59965;
   @Shadow
   @Final
   private FogRenderer field_60793;
   @Shadow
   @Final
   GuiRenderState field_59966;
   @Unique
   private static final ThreadLocal<EventRender3D> FOV_EVENT = new ThreadLocal<>();
   @Shadow
   @Final
   private Camera field_18765;

   @Inject(
      method = {"close"},
      at = {@At("RETURN")}
   )
   private void onClose(CallbackInfo ci) {
      if (Client.get() != null && Client.get().render2D() != null) {
         Client.get().render2D().close();
      }
   }

   @Inject(
      method = {"method_3192"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
         shift = Shift.AFTER
      )}
   )
   private void afterGuiRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      if (this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
         if (!this.isLoadingScreen(this.field_4015.field_1755)) {
            if (this.field_4015.method_18506() == null) {
               if (this.shouldRenderOnTop(this.field_4015.field_1755)) {
                  this.field_59966.method_70926();
                  int mouseX = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
                  int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
                  float tickDelta = tickCounter.method_60637(false);
                  DrawContext context = new DrawContext(this.field_4015, this.field_59966, mouseX, mouseY);
                  if (this.field_4015.field_1755 instanceof ko menu) {
                     menu.renderOverlay(context, tickCounter);
                  } else if (this.field_4015.field_1755 instanceof DropDownMenu dropDownMenu) {
                     dropDownMenu.renderOverlay(context, tickCounter);
                  } else if (this.field_4015.field_1755 instanceof TestGui testGui) {
                     testGui.renderOverlay(context, tickCounter);
                  }

                  if (this.field_4015.field_1755 instanceof InventoryScreen) {
                     ab.render(context, mouseX, mouseY);
                  }

                  this.field_59965.method_70890(this.field_60793.method_71109(FogType.field_60101));
               }
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/lang/Math;max(FF)F",
         ordinal = 0
      )}
   )
   private float onNauseaDistortion(float original) {
      dk noRender = dk.getInstance();
      return noRender != null && noRender.isEnabled() && noRender.ignoreZalupa.getValue() ? 0.0F : original;
   }

   @Inject(
      method = {"method_3189"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShowFloatingItem(ItemStack stack, CallbackInfo ci) {
      dk noRender = dk.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.ignoreTotemPop.getValue()) {
         ci.cancel();
      }
   }

   @Unique
   private boolean shouldRenderOnTop(Screen screen) {
      if (screen == null) {
         return true;
      } else if (screen instanceof ko) {
         return true;
      } else if (screen instanceof DropDownMenu) {
         return true;
      } else if (screen instanceof TestGui) {
         return true;
      } else {
         return screen instanceof ChatScreen ? true : screen instanceof InventoryScreen;
      }
   }

   @Unique
   private boolean isLoadingScreen(Screen screen) {
      if (screen == null) {
         return false;
      } else {
         String className = screen.getClass().getSimpleName().toLowerCase();
         String fullName = screen.getClass().getName().toLowerCase();
         if (className.contains("loading")) {
            return true;
         } else if (className.contains("progress")) {
            return true;
         } else if (className.contains("connecting")) {
            return true;
         } else if (className.contains("downloading")) {
            return true;
         } else if (className.contains("terrain")) {
            return true;
         } else if (className.contains("generating")) {
            return true;
         } else if (className.contains("saving")) {
            return true;
         } else if (className.contains("reload")) {
            return true;
         } else if (className.contains("resource")) {
            return true;
         } else {
            return className.contains("pack") ? true : fullName.contains("mojang");
         }
      }
   }

   @Shadow
   public abstract float method_3196(Camera v1, float v2, boolean v3);

   @ModifyExpressionValue(
      method = {"method_3196"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/lang/Integer;intValue()I",
         remap = false
      )}
   )
   private int hookGetFov(int original) {
      EventRender3D event = new EventRender3D();
      event.hook();
      if (event.isCancelled()) {
         FOV_EVENT.set(event);
         return event.getFov();
      } else {
         FOV_EVENT.remove();
         return original;
      }
   }

   @ModifyExpressionValue(
      method = {"method_3196"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3532;method_16439(FFF)F",
         ordinal = 0
      )}
   )
   private float hookFovMultiplier(float original) {
      EventRender3D event = FOV_EVENT.get();
      return event != null && event.isCancelled() && event.isOverrideSprint() ? 1.0F : original;
   }

   @Inject(
      method = {"method_3196"},
      at = {@At("RETURN")}
   )
   private void afterGetFov(CallbackInfoReturnable<Float> cir) {
      FOV_EVENT.remove();
   }

   @Inject(
      method = {"method_3172"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void spectator$hideHand(CallbackInfo ci) {
      if (nr.isActive()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4597$class_4598;method_22993()V",
         shift = Shift.AFTER
      )}
   )
   private void captureHandsForEditor(RenderTickCounter tickCounter, CallbackInfo ci) {
      go editor = go.getInstance();
      if (editor.isActive()) {
         editor.captureAfterHands();
      }
   }

   @Inject(
      method = {"method_3188"},
      at = {@At(
         value = "INVOKE_STRING",
         target = "Lnet/minecraft/class_3695;method_15405(Ljava/lang/String;)V",
         args = {"ldc=hand"}
      )}
   )
   public void hookWorldRender(
      RenderTickCounter tickCounter,
      CallbackInfo ci,
      @Local(ordinal = 0) Matrix4f projection,
      @Local(ordinal = 1) Matrix4f view,
      @Local(ordinal = 0) float tickDelta,
      @Local MatrixStack matrixStack
   ) {
      if (this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
         MatrixStack worldSpaceStack = new MatrixStack();
         worldSpaceStack.method_22907(RotationAxis.field_40714.rotationDegrees(this.field_18765.method_19329()));
         worldSpaceStack.method_22907(RotationAxis.field_40716.rotationDegrees(this.field_18765.method_19330() + 180.0F));
         vs.zs.lastProjMat.set(this.field_4015.field_1773.method_22973(this.method_3196(this.field_18765, tickDelta, true)));
         vs.zs.lastModMat.set(RenderSystem.getModelViewMatrix());
         vs.zs.lastWorldSpaceMatrix.set(worldSpaceStack.method_23760().method_23761());
      }
   }
}
