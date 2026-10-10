package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.AutoTool;
import ru.white.module.impl.render.GlassHands;
import ru.white.module.impl.render.NoRender;
import ru.white.module.impl.render.Hands;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.screen.GuiCheckBox;
import ru.white.event.impl.EventMotion;
import ru.white.event.impl.EventPacket;
import ru.white.event.impl.EventEntityRender;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class HeldItemRendererMixin {
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;

   @WrapWithCondition(
      method = {"method_3222"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 1
      )}
   )
   private boolean cancelMapRotationX(MatrixStack matrices, Quaternionfc quaternion) {
      return false;
   }

   @WrapWithCondition(
      method = {"method_3222"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 2
      )}
   )
   private boolean cancelMapRotationY(MatrixStack matrices, Quaternionfc quaternion) {
      return false;
   }

   @WrapOperation(
      method = {"method_22976"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 0
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_759;method_33303(Lnet/minecraft/class_746;)Lnet/minecraft/class_759$class_5773;"
         ),
         to = @At(
            value = "FIELD",
            target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
         )
      )}
   )
   private void replacePitchRotation(MatrixStack matrices, Quaternionfc quaternion, Operation<Void> original, @Local(ordinal = 3) float h) {
      if (!dk.getInstance().removeCamreZalupa.getValue()) {
         float cameraPitch = MinecraftClient.method_1551().field_1773.method_19418().method_19329();
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees((cameraPitch - h) * 0.1F));
      }
   }

   @WrapOperation(
      method = {"method_22976"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V",
         ordinal = 1
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_759;method_33303(Lnet/minecraft/class_746;)Lnet/minecraft/class_759$class_5773;"
         ),
         to = @At(
            value = "FIELD",
            target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
         )
      )}
   )
   private void replaceYawRotation(MatrixStack matrices, Quaternionfc quaternion, Operation<Void> original, @Local(ordinal = 4) float i) {
      if (!dk.getInstance().removeCamreZalupa.getValue()) {
         float cameraYaw = MinecraftClient.method_1551().field_1773.method_19418().method_19330();
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((cameraYaw - i) * 0.1F));
      }
   }

   @ModifyExpressionValue(
      method = {"method_22976"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_759$class_5773;field_28387:Z"
      )}
   )
   private boolean white$showEditorMainHand(boolean original) {
      return go.getInstance().isActive() || original;
   }

   @ModifyExpressionValue(
      method = {"method_22976"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_759$class_5773;field_28388:Z"
      )}
   )
   private boolean white$showEditorOffHand(boolean original) {
      return go.getInstance().isActive() || original;
   }

   @ModifyArg(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 0
      ),
      index = 5
   )
   private ItemStack white$useEditorMainHandItem(ItemStack original) {
      if (!go.getInstance().isActive()) {
         if (ci.isSilentActive()) {
            ItemStack spoof = ci.getVisualItem();
            if (spoof != null) {
               return spoof;
            }
         }

         return original;
      } else {
         ClientPlayerEntity player = MinecraftClient.method_1551().field_1724;
         return player == null ? original : go.getInstance().previewStackFor(player.method_6068());
      }
   }

   @ModifyArg(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 0
      ),
      index = 6
   )
   private float white$fixEquipProgressForAutoTool(float original) {
      return ci.isSilentActive() ? 1.0F : original;
   }

   @Inject(
      method = {"method_3220"},
      at = {@At("TAIL")}
   )
   private void white$keepEquipProgressOnSilentAutoTool(CallbackInfo ci) {
      if (ci.isSilentActive()) {
         this.field_4043 = 1.0F;
         this.field_4053 = 1.0F;
      }
   }

   @ModifyArg(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 1
      ),
      index = 5
   )
   private ItemStack white$useEditorOffHandItem(ItemStack original) {
      if (!go.getInstance().isActive()) {
         return original;
      } else {
         ClientPlayerEntity player = MinecraftClient.method_1551().field_1724;
         return player == null ? original : go.getInstance().previewStackFor(player.method_6068().method_5928());
      }
   }

   @Inject(
      method = {"method_22976"},
      at = {@At("HEAD")}
   )
   private void onRenderItemPre(
      float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue queue, ClientPlayerEntity player, int light, CallbackInfo ci
   ) {
      go editor = go.getInstance();
      if (editor.isActive()) {
         editor.captureBeforeHands();
      } else {
         GlassHands glassHands = GlassHands.getInstance();
         if (glassHands != null && glassHands.isEnabled()) {
            new EventMotion(ac.bc.PRE, matrices, tickProgress).hook();
         }
      }
   }

   @Inject(
      method = {"method_22976"},
      at = {@At("TAIL")}
   )
   private void onRenderItemPost(
      float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue queue, ClientPlayerEntity player, int light, CallbackInfo ci
   ) {
      if (!go.getInstance().isActive()) {
         GlassHands glassHands = GlassHands.getInstance();
         if (glassHands != null && glassHands.isEnabled()) {
            new EventMotion(ac.bc.POST, matrices, tickProgress).hook();
         }
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22903()V",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void onRenderFirstPersonItem(
      AbstractClientPlayerEntity player,
      float tickProgress,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      Arm arm = hand == Hand.field_5808 ? player.method_6068() : player.method_6068().method_5928();
      EventEntityRender event = new EventEntityRender(matrices, hand, arm);
      event.hook();
      if (!item.method_7960()) {
         oj hands = oj.get();
         if (hands != null) {
            hands.applyHandTranslation(matrices, arm);
         }
      }
   }

   @WrapWithCondition(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3224(Lnet/minecraft/class_4587;Lnet/minecraft/class_1306;F)V",
         ordinal = 4
      )}
   )
   private boolean cancelEquipOffsetForCustomAnim(HeldItemRenderer instance, MatrixStack matrices, Arm arm, float equipProgress) {
      if (arm != MinecraftClient.method_1551().field_1724.method_6068()) {
         return true;
      } else {
         xk hands = xk.get();
         return hands != null && hands.isEnabled() && hands.auraCheck() ? hands.swingMode.is("Отключено") : true;
      }
   }

   @WrapOperation(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_65816(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V"
      )}
   )
   private void handAnimationHook(
      HeldItemRenderer instance,
      float swingProgress,
      MatrixStack matrices,
      int armX,
      Arm arm,
      Operation<Void> original,
      @Local(ordinal = 0,argsOnly = true) AbstractClientPlayerEntity player,
      @Local(ordinal = 0,argsOnly = true) Hand hand,
      @Local(ordinal = 3,argsOnly = true) float equipProgress,
      @Local(ordinal = 0,argsOnly = true) ItemStack item
   ) {
      if (item.method_7909() instanceof CrossbowItem) {
         original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
      } else {
         EventPacket event = new EventPacket(matrices, hand, swingProgress, equipProgress, arm);
         event.hook();
         if (!event.isCancelled()) {
            original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
         }
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 0
      )}
   )
   public void injectBeforeRenderCrossBowItem(
      AbstractClientPlayerEntity player,
      float tickProgress,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      oj viewModel = oj.get();
      if (viewModel.shouldApplyTransforms()) {
         boolean isMainHand = hand == Hand.field_5808;
         Arm arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
         viewModel.applyHandScale(matrices, arm);
         go.getInstance().updateHandBounds(arm, matrices);
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
         ordinal = 1
      )}
   )
   public void injectBeforeRenderItem(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      oj viewModel = oj.get();
      if (viewModel.shouldApplyTransforms()) {
         boolean isMainHand = hand == Hand.field_5808;
         Arm arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
         viewModel.applyHandScale(matrices, arm);
         go.getInstance().updateHandBounds(arm, matrices);
      }
   }
}
