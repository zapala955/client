package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.AutoTool;
import ru.white.module.impl.render.HoldMyItems;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class HeldItemRendererHmiMixin {
   @Unique
   private boolean hmi$repPower = false;
   @Unique
   private float hmi$prevAge = 0.0F;
   @Unique
   private double hmi$previousRotation = 0.0;
   @Unique
   private float hmi$swingAngleY = 0.0F;
   @Unique
   private float hmi$swingAngleX = 0.0F;
   @Unique
   private float hmi$swingVelocityY = 0.0F;
   @Unique
   private float hmi$swingVelocityX = 0.0F;
   @Unique
   private float hmi$swingVelocityZ = 0.0F;
   @Unique
   private float hmi$vertAngleY = 0.0F;
   @Unique
   private float hmi$vertVelocityY = 0.0F;
   @Unique
   private float hmi$vertVelocityYSlime = 0.0F;
   @Unique
   private float hmi$vertAngleYSlime = 0.0F;
   @Unique
   private float hmi$riptideCounter = 0.0F;
   @Unique
   private float hmi$netherCounter = 0.0F;
   @Unique
   private float hmi$fallCounter = 0.0F;
   @Unique
   private float hmi$inWaterCounter = 0.0F;
   @Unique
   private float hmi$inspect = 0.0F;
   @Unique
   private float hmi$tilt = 0.0F;
   @Unique
   private float hmi$freezeCounter = 0.0F;
   @Unique
   private float hmi$clCount = 0.0F;
   @Unique
   private float hmi$crawlCount = 0.0F;
   @Unique
   private float hmi$directionalCrawlCount = 0.0F;
   @Unique
   private float hmi$climbCount = 0.0F;
   @Unique
   private float hmi$mouseHolding = 1.0F;
   @Unique
   private boolean hmi$isSwinging = false;
   @Unique
   private float hmi$swingProgress = 0.0F;
   @Unique
   private boolean hmi$isForward = false;
   @Unique
   private boolean hmi$isAttacking = false;
   @Unique
   private boolean hmi$left = false;
   @Shadow
   private ItemStack field_4047;
   @Shadow
   @Final
   private MinecraftClient field_4050;

   @Shadow
   protected abstract void method_3216(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, Arm v4);

   @Shadow
   protected abstract void method_3219(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, float v4, float v5, Arm v6);

   @Shadow
   protected abstract void method_3222(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, float v4, Arm v5, float v6, ItemStack v7);

   @Shadow
   protected abstract void method_3231(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, float v4, float v5, float v6);

   @Shadow
   protected abstract void method_3224(MatrixStack v1, Arm v2, float v3);

   @Shadow
   protected abstract void method_65816(float v1, MatrixStack v2, int v3, Arm v4);

   @Shadow
   public abstract void method_3233(LivingEntity v1, ItemStack v2, ItemDisplayContext v3, MatrixStack v4, OrderedRenderCommandQueue v5, int v6);

   @Unique
   private float hmiEaseInOutBack(float x) {
      float c1 = 1.70158F;
      float c2 = c1 * 1.525F;
      return (float)(
         (double)x < 0.5
            ? Math.pow(2.0 * (double)x, 2.0) * (double)((c2 + 1.0F) * 2.0F * x - c2) / 2.0
            : (Math.pow(2.0 * (double)x - 2.0, 2.0) * (double)((c2 + 1.0F) * (x * 2.0F - 2.0F) + c2) + 2.0) / 2.0
      );
   }

   @Unique
   private float hmiGetAttackDamage(ItemStack stack) {
      AttributeModifiersComponent modifiers = (AttributeModifiersComponent)stack.method_57353().method_58694(DataComponentTypes.field_49636);
      if (modifiers == null) {
         return 0.0F;
      } else {
         float totalDamage = 0.0F;

         for (Entry entry : modifiers.comp_2393()) {
            if (entry.comp_2395().comp_349() == EntityAttributes.field_23721.comp_349()) {
               totalDamage += (float)entry.comp_2396().comp_2449();
            }
         }

         return totalDamage;
      }
   }

   @Unique
   private boolean hmiIsSharpAnimation(qj config) {
      return config != null && config.hmiAnimationType.getValue().equals("Шарп");
   }

   @Unique
   private void hmiAltSwing(MatrixStack matrices, Arm arm, float swingProgress, ItemStack item) {
      int i = arm == Arm.field_6183 ? 1 : -1;
      float f = MathHelper.method_15374((double)(swingProgress * 3.14F));
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)i * (45.0F + f * 0.0F)));
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)i * -45.0F));
   }

   @Unique
   private boolean hmiIsSkull(ItemStack item) {
      return item.method_31573(ItemTags.field_48298) || Block.method_9503(item.method_7909()) instanceof AbstractSkullBlock;
   }

   @Inject(
      method = {"method_3228"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hmiOnRenderFirstPersonItem(
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
      qj swings = qj.getInstance();
      if (swings != null && swings.isEnabled() && swings.hmiEnable.getValue()) {
         boolean isMainHand = hand == Hand.field_5808;
         if (isMainHand && ci.isSilentActive()) {
            ItemStack visual = ci.getVisualItem();
            if (visual != null) {
               item = visual;
            }

            equipProgress = 1.0F;
         }

         Arm arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
         float sideFactor = isMainHand ? 1.0F : -1.0F;
         if (swings.swapHands.getValue()) {
            arm = arm.method_5928();
            sideFactor *= -1.0F;
         }

         this.hmiRenderCustomFirstPersonItem(
            player, tickDelta, pitch, hand, arm, sideFactor, swingProgress, item, equipProgress, matrices, orderedRenderCommandQueue, light
         );
         ci.cancel();
      }
   }

   @Unique
   private void hmiRenderCustomFirstPersonItem(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      Arm arm,
      float sideFactor,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light
   ) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
      // java.lang.OutOfMemoryError: Java heap space
      //   at java.base/jdk.internal.misc.Unsafe.allocateUninitializedArray(Unsafe.java:1380)
      //   at java.base/java.lang.StringConcatHelper.newArray(StringConcatHelper.java:511)
      //   at java.base/java.lang.invoke.DirectMethodHandle$Holder.invokeStatic(DirectMethodHandle$Holder)
      //   at java.base/java.lang.invoke.LambdaForm$MH/0x0000023b190dd800.invoke(LambdaForm$MH)
      //   at java.base/java.lang.invoke.Invokers$Holder.linkToTargetMethod(Invokers$Holder)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.getAllExprents(Exprent.java:131)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.getAllExprents(Exprent.java:135)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.getAllExprents(Exprent.java:135)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.getAllExprents(Exprent.java:118)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.getAllExprents(Exprent.java:113)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.getAllVersions(StackVarsProcessor.java:623)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.iterateExprent(StackVarsProcessor.java:439)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.iterateStatements(StackVarsProcessor.java:189)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:72)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:224)
      //
      // Bytecode:
      // 0000: invokestatic ru/white/module/impl/render/qj.getInstance ()Lru/white/module/impl/render/qj;
      // 0003: astore 13
      // 0005: aload 13
      // 0007: ifnull 2fdb
      // 000a: aload 13
      // 000c: invokevirtual ru/white/module/impl/render/qj.isEnabled ()Z
      // 000f: ifeq 2fdb
      // 0012: aload 13
      // 0014: getfield ru/white/module/impl/render/qj.hmiEnable Lq2/z/BooleanSetting;
      // 0017: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 001a: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 001d: ifeq 2fdb
      // 0020: aload 1
      // 0021: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 0024: ifeq 0038
      // 0027: aload 1
      // 0028: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6030 ()Lnet/minecraft/item/ItemStack;
      // 002b: getstatic net/minecraft/item/Items.field_27070 Lnet/minecraft/item/Item;
      // 002e: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0031: ifeq 0038
      // 0034: bipush 1
      // 0035: goto 0039
      // 0038: bipush 0
      // 0039: istore 14
      // 003b: iload 14
      // 003d: ifne 2fdb
      // 0040: invokestatic ru/white/module/impl/render/qj.getInstance ()Lru/white/module/impl/render/qj;
      // 0043: astore 15
      // 0045: aload 1
      // 0046: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36454 ()F
      // 0049: fstore 16
      // 004b: fload 16
      // 004d: f2d
      // 004e: invokestatic java/lang/Math.toRadians (D)D
      // 0051: dstore 17
      // 0053: dload 17
      // 0055: invokestatic java/lang/Math.sin (D)D
      // 0058: dneg
      // 0059: dstore 19
      // 005b: dload 17
      // 005d: invokestatic java/lang/Math.cos (D)D
      // 0060: dstore 21
      // 0062: aload 1
      // 0063: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0066: astore 23
      // 0068: aload 23
      // 006a: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 006d: dload 19
      // 006f: dmul
      // 0070: aload 23
      // 0072: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 0075: dload 21
      // 0077: dmul
      // 0078: dadd
      // 0079: dstore 24
      // 007b: aload 1
      // 007c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 007f: invokevirtual net/minecraft/util/math/Vec3d.method_61890 ()Lnet/minecraft/util/math/Vec3d;
      // 0082: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0085: dload 21
      // 0087: dmul
      // 0088: aload 23
      // 008a: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 008d: dload 19
      // 008f: dmul
      // 0090: dsub
      // 0091: dstore 26
      // 0093: aload 1
      // 0094: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0097: fconst_0
      // 0098: fcmpl
      // 0099: ifeq 00ad
      // 009c: ldc_w 90.0
      // 009f: aload 1
      // 00a0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 00a3: fdiv
      // 00a4: ldc_w 10.0
      // 00a7: fdiv
      // 00a8: fstore 28
      // 00aa: goto 00b0
      // 00ad: fconst_1
      // 00ae: fstore 28
      // 00b0: fload 28
      // 00b2: fconst_1
      // 00b3: fcmpl
      // 00b4: ifle 00ba
      // 00b7: fconst_1
      // 00b8: fstore 28
      // 00ba: fload 28
      // 00bc: fconst_0
      // 00bd: fcmpg
      // 00be: ifge 00c4
      // 00c1: fconst_1
      // 00c2: fstore 28
      // 00c4: aload 4
      // 00c6: getstatic net/minecraft/util/Hand.field_5808 Lnet/minecraft/util/Hand;
      // 00c9: if_acmpne 00d0
      // 00EventPacket: bipush 1
      // 00cd: goto 00d1
      // 00d0: bipush 0
      // 00d1: istore 29
      // 00d3: aload 10
      // 00d5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 00d8: aload 10
      // 00ConfigManager: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 00dd: aload 15
      // 00df: aload 10
      // 00e1: aload 5
      // 00e3: invokevirtual ru/white/module/impl/render/qj.applyHandTranslation (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;)V
      // 00e6: dconst_1
      // 00e7: invokestatic net/minecraft/client/MinecraftClient.method_1551 ()Lnet/minecraft/client/MinecraftClient;
      // 00ea: invokevirtual net/minecraft/client/MinecraftClient.method_47599 ()I
      // 00ed: bipush 1
      // 00ee: invokestatic java/lang/Math.max (II)I
      // 00f1: i2d
      // 00f2: ddiv
      // 00f3: ldc2_w 30.0
      // 00f6: dmul
      // 00f7: dstore 30
      // 00f9: aload 15
      // 00fb: getfield ru/white/module/impl/render/qj.hmiSmoothness Lq2/z/SliderSetting;
      // 00fe: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0101: invokevirtual java/lang/Float.floatValue ()F
      // 0104: ldc_w 0.1
      // 0107: ldc_w 6.0
      // 010a: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 010d: fstore 32
      // 010f: fload 7
      // 0111: fconst_0
      // 0112: fconst_1
      // 0113: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 0116: f2d
      // 0117: fload 32
      // 0119: f2d
      // 011a: invokestatic java/lang/Math.pow (DD)D
      // 011d: d2f
      // 011e: fstore 33
      // 0120: fload 33
      // 0122: f2d
      // 0123: ldc2_w 0.6
      // 0126: dcmpg
      // 0127: ifge 013e
      // 012a: fload 33
      // 012c: fconst_0
      // 012d: ldc_w 0.12506
      // 0130: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 0133: ldc_w 12.56
      // 0136: fmul
      // 0137: f2d
      // 0138: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 013b: goto 0151
      // 013e: fload 33
      // 0140: ldc_w 0.62532
      // 0143: ldc_w 0.75038
      // 0146: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 0149: ldc_w 12.56
      // 014c: fmul
      // 014d: f2d
      // 014e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 0151: fstore 34
      // 0153: fload 33
      // 0155: ldc_w 3.14
      // 0158: fmul
      // 0159: f2d
      // 015a: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 015d: fstore 35
      // 015f: aload 0
      // 0160: fload 35
      // 0162: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiEaseInOutBack (F)F
      // 0165: fstore 35
      // 0167: aload 8
      // 0169: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 016c: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 016f: ifeq 017f
      // 0172: aload 0
      // 0173: aload 15
      // 0175: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiIsSharpAnimation (Lru/white/module/impl/render/qj;)Z
      // 0178: ifeq 017f
      // 017b: bipush 1
      // 017c: goto 0180
      // 017f: bipush 0
      // 0180: istore 36
      // 0182: aload 8
      // 0184: getstatic net/minecraft/item/Items.field_8287 Lnet/minecraft/item/Item;
      // 0187: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 018a: ifne 01cf
      // 018d: aload 8
      // 018f: getstatic net/minecraft/item/Items.field_49098 Lnet/minecraft/item/Item;
      // 0192: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0195: ifne 01cf
      // 0198: aload 8
      // 019a: getstatic net/minecraft/item/Items.field_8803 Lnet/minecraft/item/Item;
      // 019d: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 01a0: ifne 01cf
      // 01a3: aload 8
      // 01a5: getstatic net/minecraft/item/Items.field_8449 Lnet/minecraft/item/Item;
      // 01a8: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 01ab: ifne 01cf
      // 01ae: aload 8
      // 01b0: getstatic net/minecraft/item/Items.field_8543 Lnet/minecraft/item/Item;
      // 01b3: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 01b6: ifne 01cf
      // 01b9: aload 8
      // 01DraggableWidget: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 01be: instanceof net/minecraft/item/SplashPotionItem
      // 01c1: ifne 01cf
      // 01c4: aload 8
      // 01c6: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 01c9: instanceof net/minecraft/item/LingeringPotionItem
      // 01EventPacket: ifeq 02EventMotion
      // 01cf: aload 1
      // 01d0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6079 ()Lnet/minecraft/item/ItemStack;
      // 01d3: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 01d6: ifeq 02EventMotion
      // 01d9: aload 8
      // 01db: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 01de: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 01e1: if_acmpeq 02EventMotion
      // 01e4: aload 8
      // 01e6: getstatic net/minecraft/item/Items.field_8814 Lnet/minecraft/item/Item;
      // 01e9: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 01EventAttack: ifne 02EventMotion
      // 01ef: aload 1
      // 01f0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 01f3: ifne 02EventMotion
      // 01f6: aload 1
      // 01f7: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 01fa: ifne 02EventMotion
      // 01fd: aload 1
      // 01fe: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 0201: ifne 02EventMotion
      // 0204: aload 1
      // 0205: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6068 ()Lnet/minecraft/util/Arm;
      // 0208: getstatic net/minecraft/util/Arm.field_6182 Lnet/minecraft/util/Arm;
      // 020b: if_acmpne 021a
      // 020e: iload 29
      // 0210: ifne 0217
      // 0213: bipush 1
      // 0214: goto 0218
      // 0217: bipush 0
      // 0218: istore 29
      // 021a: aload 10
      // 021c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 021f: aload 10
      // 0221: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0224: ldc_w -25.0
      // 0227: fload 6
      // 0229: fmul
      // 022a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 022f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0232: aload 10
      // 0234: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0237: ldc_w -10.0
      // 023a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 023f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0242: aload 10
      // 0244: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0247: ldc_w 25.0
      // 024a: fload 6
      // 024c: fmul
      // 024d: fload 35
      // 024f: fmul
      // 0250: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0255: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0258: aload 10
      // 025a: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 025d: ldc_w 30.0
      // 0260: fload 35
      // 0262: fmul
      // 0263: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0268: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 026b: aload 10
      // 026d: ldc2_w -0.15
      // 0270: fload 6
      // 0272: f2d
      // 0273: dmul
      // 0274: ldc2_w 0.1
      // 0277: ldc2_w 0.1
      // 027a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 027d: aload 10
      // 027f: dconst_0
      // 0280: ldc2_w -0.55
      // 0283: fload 35
      // 0285: f2d
      // 0286: dmul
      // 0287: ldc2_w 0.4
      // 028a: fload 35
      // 028c: f2d
      // 028d: dmul
      // 028e: ldc2_w 3.140000104904175
      // 0291: dmul
      // 0292: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0295: aload 0
      // 0296: aload 10
      // 0298: aload 11
      // 029a: iload 12
      // 029c: fload 9
      // 029e: fconst_0
      // 029f: aload 5
      // 02a1: invokevirtual net/minecraft/util/Arm.method_5928 ()Lnet/minecraft/util/Arm;
      // 02a4: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 02a7: aload 10
      // 02a9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 02EventMotion: aload 0
      // 02ad: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 02b0: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 02b3: getfield net/minecraft/client/option/GameOptions.field_1886 Lnet/minecraft/client/option/KeyBinding;
      // 02b6: invokevirtual net/minecraft/client/option/KeyBinding.method_1434 ()Z
      // 02b9: ifeq 02db
      // 02bc: aload 0
      // 02bd: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$isAttacking Z
      // 02c0: ifne 02db
      // 02c3: fload 7
      // 02c5: f2d
      // 02c6: dconst_0
      // 02c7: dcmpl
      // 02c8: ifne 02db
      // 02cb: aload 0
      // 02EventPacket: aload 0
      // 02cd: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$left Z
      // 02d0: ifne 02d7
      // 02d3: bipush 1
      // 02d4: goto 02d8
      // 02d7: bipush 0
      // 02d8: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$left Z
      // 02db: aload 8
      // 02dd: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 02e0: ifne 0829
      // 02e3: aload 1
      // 02e4: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6068 ()Lnet/minecraft/util/Arm;
      // 02e7: getstatic net/minecraft/util/Arm.field_6182 Lnet/minecraft/util/Arm;
      // 02ea: if_acmpne 02f9
      // 02ed: iload 29
      // 02ef: ifne 02f6
      // 02f2: bipush 1
      // 02f3: goto 02f7
      // 02f6: bipush 0
      // 02f7: istore 29
      // 02f9: aload 0
      // 02fa: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$left Z
      // 02fd: ifne 0321
      // 0300: aload 8
      // 0302: getstatic net/minecraft/registry/tag/ItemTags.field_42612 Lnet/minecraft/registry/tag/TagKey;
      // 0305: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0308: ifne 0321
      // 030b: aload 8
      // 030d: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0310: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 0313: if_acmpeq 0321
      // 0316: aload 8
      // 0318: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 031b: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 031e: if_acmpne 0605
      // 0321: aload 8
      // 0323: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 0326: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0329: ifne 0605
      // 032c: iload 36
      // 032e: ifeq 038e
      // 0331: aload 10
      // 0333: ldc2_w 0.1
      // 0336: fload 6
      // 0338: f2d
      // 0339: dmul
      // 033a: fload 34
      // 033c: f2d
      // 033d: dmul
      // 033e: ldc2_w 0.1
      // 0341: fload 34
      // 0343: f2d
      // 0344: dmul
      // 0345: ldc2_w -0.5
      // 0348: fload 35
      // 034a: f2d
      // 034b: dmul
      // 034c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 034f: aload 10
      // 0351: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0354: ldc_w -30.0
      // 0357: fload 34
      // 0359: fmul
      // 035a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 035f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0362: aload 10
      // 0364: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0367: ldc_w -20.0
      // 036a: fload 34
      // 036c: fmul
      // 036d: fload 6
      // 036f: fmul
      // 0370: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0375: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0378: aload 10
      // 037a: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 037d: ldc_w 40.0
      // 0380: fload 35
      // 0382: fmul
      // 0383: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0388: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 038b: goto 0902
      // 038e: aload 8
      // 0390: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 0393: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0396: ifne 0571
      // 0399: aload 8
      // 039b: getstatic net/minecraft/registry/tag/ItemTags.field_42612 Lnet/minecraft/registry/tag/TagKey;
      // 039e: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 03a1: ifne 0571
      // 03a4: aload 8
      // 03a6: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 03a9: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 03EventMotion: if_acmpne 0418
      // 03af: aload 10
      // 03b1: dconst_0
      // 03b2: dconst_0
      // 03b3: ldc2_w 0.45
      // 03b6: fload 34
      // 03b8: f2d
      // 03b9: dmul
      // 03ba: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 03bd: aload 10
      // 03bf: ldc2_w -0.25
      // 03c2: fload 6
      // 03c4: f2d
      // 03c5: dmul
      // 03c6: fload 35
      // 03c8: f2d
      // 03c9: dmul
      // 03ConfigFile: ldc2_w -0.35
      // 03cd: fload 34
      // 03cf: f2d
      // 03d0: dmul
      // 03d1: ldc2_w -0.6
      // 03d4: fload 35
      // 03d6: f2d
      // 03d7: dmul
      // 03d8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 03db: aload 10
      // 03dd: dconst_0
      // 03de: ldc2_w 0.1
      // 03e1: fload 35
      // 03e3: f2d
      // 03e4: dmul
      // 03e5: dconst_0
      // 03e6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 03e9: aload 10
      // 03eb: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 03ee: ldc_w 15.0
      // 03f1: fload 34
      // 03f3: fmul
      // 03f4: fload 6
      // 03f6: fmul
      // 03f7: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 03EventMove: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 03Category: aload 10
      // 0401: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0404: ldc_w 30.0
      // 0407: fload 34
      // 0409: fmul
      // 040a: fload 6
      // 040c: fmul
      // 040d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0412: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0415: goto 0902
      // 0418: aload 8
      // 041a: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.TOOLS Lnet/minecraft/registry/tag/TagKey;
      // 041d: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0420: ifeq 0496
      // 0423: aload 8
      // 0425: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0428: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 042b: if_acmpeq 0496
      // 042e: aload 8
      // 0430: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 0433: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0436: ifne 0496
      // 0439: aload 10
      // 043b: ldc2_w 0.1
      // 043e: fload 6
      // 0440: f2d
      // 0441: dmul
      // 0442: fload 34
      // 0444: f2d
      // 0445: dmul
      // 0446: ldc2_w 0.1
      // 0449: fload 34
      // 044b: f2d
      // 044c: dmul
      // 044d: ldc2_w -0.5
      // 0450: fload 35
      // 0452: f2d
      // 0453: dmul
      // 0454: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0457: aload 10
      // 0459: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 045c: ldc_w -30.0
      // 045f: fload 34
      // 0461: fmul
      // 0462: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0467: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 046a: aload 10
      // 046c: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 046f: ldc_w -20.0
      // 0472: fload 34
      // 0474: fmul
      // 0475: fload 6
      // 0477: fmul
      // 0478: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 047d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0480: aload 10
      // 0482: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0485: ldc_w 40.0
      // 0488: fload 35
      // 048a: fmul
      // 048b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0490: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0493: goto 0902
      // 0496: aload 8
      // 0498: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 049b: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 049e: if_acmpeq 0514
      // 04a1: aload 10
      // 04a3: ldc2_w 0.1
      // 04a6: fload 6
      // 04a8: f2d
      // 04a9: dmul
      // 04aa: fload 34
      // 04EventMotion: f2d
      // 04ad: dmul
      // 04ae: ldc2_w 0.1
      // 04b1: fload 34
      // 04b3: f2d
      // 04b4: dmul
      // 04b5: ldc2_w -0.1
      // 04b8: fload 35
      // 04ba: f2d
      // 04DraggableWidget: dmul
      // 04bc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 04bf: aload 10
      // 04c1: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 04c4: ldc_w -30.0
      // 04c7: fload 34
      // 04c9: fmul
      // 04ConfigFile: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 04cf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 04d2: aload 10
      // 04d4: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 04d7: ldc_w -10.0
      // 04ConfigManager: fload 34
      // 04EventKey: fmul
      // 04dd: fload 6
      // 04df: fmul
      // 04e0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 04e5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 04e8: aload 10
      // 04ea: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 04ed: ldc_w 40.0
      // 04f0: fload 35
      // 04f2: fmul
      // 04f3: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 04f8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 04fb: aload 10
      // 04fd: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0500: ldc_w 10.0
      // 0503: fload 35
      // 0505: fmul
      // 0506: fload 6
      // 0508: fmul
      // 0509: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 050e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0511: goto 0902
      // 0514: aload 10
      // 0516: ldc2_w 0.1
      // 0519: fload 6
      // 051b: f2d
      // 051c: dmul
      // 051d: fload 34
      // 051f: f2d
      // 0520: dmul
      // 0521: ldc2_w 0.1
      // 0524: fload 34
      // 0526: f2d
      // 0527: dmul
      // 0528: ldc2_w -0.2
      // 052b: fload 35
      // 052d: f2d
      // 052e: dmul
      // 052f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0532: aload 10
      // 0534: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0537: ldc_w -10.0
      // 053a: fload 34
      // 053c: fmul
      // 053d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0542: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0545: aload 10
      // 0547: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 054a: ldc_w -10.0
      // 054d: fload 34
      // 054f: fmul
      // 0550: fload 6
      // 0552: fmul
      // 0553: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0558: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 055b: aload 10
      // 055d: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0560: ldc_w 20.0
      // 0563: fload 35
      // 0565: fmul
      // 0566: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 056b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 056e: goto 0902
      // 0571: aload 10
      // 0573: ldc2_w 0.8
      // 0576: fload 6
      // 0578: f2d
      // 0579: dmul
      // 057a: fload 34
      // 057c: f2d
      // 057d: dmul
      // 057e: ldc2_w 0.3
      // 0581: fload 34
      // 0583: f2d
      // 0584: dmul
      // 0585: ldc2_w -0.5
      // 0588: fload 35
      // 058a: f2d
      // 058b: dmul
      // 058c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 058f: aload 10
      // 0591: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0594: ldc_w 15.0
      // 0597: fload 34
      // 0599: fmul
      // 059a: fload 6
      // 059c: fmul
      // 059d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 05a2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 05a5: aload 10
      // 05a7: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 05aa: ldc_w -20.0
      // 05ad: fload 34
      // 05af: fmul
      // 05b0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 05b5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 05b8: aload 10
      // 05ba: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 05bd: ldc_w -70.0
      // 05c0: fload 34
      // 05c2: fmul
      // 05c3: fload 6
      // 05c5: fmul
      // 05c6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 05cb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 05ce: aload 8
      // 05d0: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 05d3: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 05d6: ifeq 05ef
      // 05d9: aload 10
      // 05db: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 05de: ldc_w 40.0
      // 05e1: fload 35
      // 05e3: fmul
      // 05e4: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 05e9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 05EventAttack: goto 0902
      // 05ef: aload 10
      // 05f1: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 05f4: ldc_w 30.0
      // 05f7: fload 35
      // 05f9: fmul
      // 05fa: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 05Category: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0602: goto 0902
      // 0605: aload 8
      // 0607: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 060a: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 060d: ifne 07c0
      // 0610: iload 36
      // 0612: ifeq 0672
      // 0615: aload 10
      // 0617: ldc2_w 0.1
      // 061a: fload 6
      // 061c: f2d
      // 061d: dmul
      // 061e: fload 34
      // 0620: f2d
      // 0621: dmul
      // 0622: ldc2_w 0.1
      // 0625: fload 34
      // 0627: f2d
      // 0628: dmul
      // 0629: ldc2_w -0.5
      // 062c: fload 35
      // 062e: f2d
      // 062f: dmul
      // 0630: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0633: aload 10
      // 0635: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0638: ldc_w -30.0
      // 063b: fload 34
      // 063d: fmul
      // 063e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0643: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0646: aload 10
      // 0648: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 064b: ldc_w -20.0
      // 064e: fload 34
      // 0650: fmul
      // 0651: fload 6
      // 0653: fmul
      // 0654: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0659: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 065c: aload 10
      // 065e: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0661: ldc_w 40.0
      // 0664: fload 35
      // 0666: fmul
      // 0667: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 066c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 066f: goto 0902
      // 0672: aload 8
      // 0674: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 0677: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 067a: ifeq 06ConfigManager
      // 067d: aload 10
      // 067f: ldc2_w 0.1
      // 0682: fload 6
      // 0684: f2d
      // 0685: dmul
      // 0686: fload 34
      // 0688: f2d
      // 0689: dmul
      // 068a: ldc2_w 0.1
      // 068d: fload 34
      // 068f: f2d
      // 0690: dmul
      // 0691: ldc2_w -0.5
      // 0694: fload 35
      // 0696: f2d
      // 0697: dmul
      // 0698: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 069b: aload 10
      // 069d: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 06a0: ldc_w -30.0
      // 06a3: fload 34
      // 06a5: fmul
      // 06a6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 06ab: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 06ae: aload 10
      // 06b0: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 06b3: ldc_w -20.0
      // 06b6: fload 34
      // 06b8: fmul
      // 06b9: fload 6
      // 06DraggableWidget: fmul
      // 06bc: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 06c1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 06c4: aload 10
      // 06c6: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 06c9: ldc_w 40.0
      // 06EventPacket: fload 35
      // 06ce: fmul
      // 06cf: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 06d4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 06d7: goto 0902
      // 06ConfigManager: aload 8
      // 06EventKey: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.TOOLS Lnet/minecraft/registry/tag/TagKey;
      // 06df: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 06e2: ifeq 074d
      // 06e5: aload 8
      // 06e7: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 06ea: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 06ed: ifne 074d
      // 06f0: aload 10
      // 06f2: ldc2_w 0.1
      // 06f5: fload 6
      // 06f7: f2d
      // 06f8: dmul
      // 06f9: fload 34
      // 06fb: f2d
      // 06EventMove: dmul
      // 06fd: ldc2_w 0.1
      // 0700: fload 34
      // 0702: f2d
      // 0703: dmul
      // 0704: ldc2_w -0.5
      // 0707: fload 35
      // 0709: f2d
      // 070a: dmul
      // 070b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 070e: aload 10
      // 0710: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0713: ldc_w -30.0
      // 0716: fload 34
      // 0718: fmul
      // 0719: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 071e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0721: aload 10
      // 0723: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0726: ldc_w -20.0
      // 0729: fload 34
      // 072b: fmul
      // 072c: fload 6
      // 072e: fmul
      // 072f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0734: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0737: aload 10
      // 0739: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 073c: ldc_w 40.0
      // 073f: fload 35
      // 0741: fmul
      // 0742: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0747: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 074a: goto 0902
      // 074d: aload 10
      // 074f: ldc2_w 0.1
      // 0752: fload 6
      // 0754: f2d
      // 0755: dmul
      // 0756: fload 34
      // 0758: f2d
      // 0759: dmul
      // 075a: ldc2_w 0.1
      // 075d: fload 34
      // 075f: f2d
      // 0760: dmul
      // 0761: ldc2_w -0.1
      // 0764: fload 35
      // 0766: f2d
      // 0767: dmul
      // 0768: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 076b: aload 10
      // 076d: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0770: ldc_w -30.0
      // 0773: fload 34
      // 0775: fmul
      // 0776: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 077b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 077e: aload 10
      // 0780: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0783: ldc_w -10.0
      // 0786: fload 34
      // 0788: fmul
      // 0789: fload 6
      // 078b: fmul
      // 078c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0791: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0794: aload 10
      // 0796: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 0799: ldc_w 40.0
      // 079c: fload 35
      // 079e: fmul
      // 079f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 07a4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 07a7: aload 10
      // 07a9: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 07EventMotion: ldc_w 10.0
      // 07af: fload 35
      // 07b1: fmul
      // 07b2: fload 6
      // 07b4: fmul
      // 07b5: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 07ba: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 07bd: goto 0902
      // 07c0: aload 8
      // 07c2: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 07c5: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 07c8: ifeq 0902
      // 07cb: aload 10
      // 07cd: dconst_0
      // 07ce: ldc2_w 0.15
      // 07d1: fload 34
      // 07d3: f2d
      // 07d4: dmul
      // 07d5: ldc2_w -0.25
      // 07d8: fload 34
      // 07ConfigManager: f2d
      // 07db: dmul
      // 07EventKey: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 07df: aload 10
      // 07e1: dconst_0
      // 07e2: dconst_0
      // 07e3: ldc2_w -0.2
      // 07e6: fload 35
      // 07e8: f2d
      // 07e9: dmul
      // 07ea: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 07ed: aload 10
      // 07ef: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 07f2: ldc_w 15.0
      // 07f5: fload 34
      // 07f7: fmul
      // 07f8: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 07fd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0800: aload 10
      // 0802: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0805: ldc_w -35.0
      // 0808: fload 34
      // 080a: fmul
      // 080b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0810: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0813: aload 10
      // 0815: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0818: ldc_w 30.0
      // 081b: fload 35
      // 081d: fmul
      // 081e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0823: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0826: goto 0902
      // 0829: aload 8
      // 082b: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 082e: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 0831: getstatic net/minecraft/block/Blocks.field_10124 Lnet/minecraft/block/Block;
      // 0834: if_acmpeq 08e3
      // 0837: aload 0
      // 0838: aload 8
      // 083a: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiIsSkull (Lnet/minecraft/item/ItemStack;)Z
      // 083d: ifne 08e3
      // 0840: aload 8
      // 0842: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.TOOLS Lnet/minecraft/registry/tag/TagKey;
      // 0845: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0848: ifeq 0874
      // 084b: aload 8
      // 084d: getstatic net/minecraft/registry/tag/ItemTags.field_41890 Lnet/minecraft/registry/tag/TagKey;
      // 0850: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0853: ifne 0874
      // 0856: aload 8
      // 0858: getstatic net/minecraft/registry/tag/ItemTags.field_40109 Lnet/minecraft/registry/tag/TagKey;
      // 085b: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 085e: ifne 0874
      // 0861: aload 8
      // 0863: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0866: getstatic net/minecraft/item/consume/UseAction.field_8950 Lnet/minecraft/item/consume/UseAction;
      // 0869: if_acmpeq 0874
      // 086c: aload 8
      // 086e: invokevirtual net/minecraft/item/ItemStack.method_7923 ()Z
      // 0871: ifne 08e3
      // 0874: aload 8
      // 0876: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0879: getstatic net/minecraft/item/consume/UseAction.field_8953 Lnet/minecraft/item/consume/UseAction;
      // 087c: if_acmpeq 08e3
      // 087f: aload 8
      // 0881: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0884: getstatic net/minecraft/item/consume/UseAction.field_27079 Lnet/minecraft/item/consume/UseAction;
      // 0887: if_acmpeq 08e3
      // 088a: aload 0
      // 088b: aload 8
      // 088d: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiGetAttackDamage (Lnet/minecraft/item/ItemStack;)F
      // 0890: fconst_0
      // 0891: fcmpl
      // 0892: ifne 08e3
      // 0895: aload 8
      // 0897: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 089a: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 089d: if_acmpeq 08e3
      // 08a0: aload 8
      // 08a2: getstatic net/minecraft/item/Items.field_23254 Lnet/minecraft/item/Item;
      // 08a5: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 08a8: ifne 08e3
      // 08ab: aload 8
      // 08ad: getstatic net/minecraft/item/Items.field_8184 Lnet/minecraft/item/Item;
      // 08b0: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 08b3: ifne 08e3
      // 08b6: aload 8
      // 08b8: getstatic net/minecraft/item/Items.field_8378 Lnet/minecraft/item/Item;
      // 08DraggableWidget: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 08be: ifne 08e3
      // 08c1: aload 8
      // 08c3: getstatic net/minecraft/item/Items.field_8868 Lnet/minecraft/item/Item;
      // 08c6: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 08c9: ifne 08e3
      // 08EventPacket: fload 7
      // 08ce: f2d
      // 08cf: ldc2_w 1.2
      // 08d2: dmul
      // 08d3: d2f
      // 08d4: fstore 7
      // 08d6: fload 7
      // 08d8: fconst_1
      // 08d9: fcmpl
      // 08ConfigManager: ifle 0902
      // 08dd: fconst_0
      // 08de: fstore 7
      // 08e0: goto 0902
      // 08e3: aload 8
      // 08e5: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 08e8: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 08eb: ifne 0902
      // 08ee: fload 7
      // 08f0: f2d
      // 08f1: ldc2_w 1.5
      // 08f4: dmul
      // 08f5: d2f
      // 08f6: fstore 7
      // 08f8: fload 7
      // 08fa: fconst_1
      // 08fb: fcmpl
      // 08EventMove: ifle 0902
      // 08Category: fconst_0
      // 0900: fstore 7
      // 0902: aload 1
      // 0903: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0906: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 0909: ldc2_w 0.08
      // 090c: dcmpl
      // 090d: iflt 097e
      // 0910: aload 0
      // 0911: aload 0
      // 0912: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$crawlCount F
      // 0915: f2d
      // 0916: ldc2_w 0.1
      // 0919: aload 1
      // 091a: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 091d: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 0920: dmul
      // 0921: ldc2_w 2.0
      // 0924: dmul
      // 0925: dload 30
      // 0927: dmul
      // 0928: dadd
      // 0929: d2f
      // 092a: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$crawlCount F
      // 092d: aload 0
      // 092e: aload 0
      // 092f: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 0932: f2d
      // 0933: ldc2_w 0.1
      // 0936: dload 24
      // 0938: dmul
      // 0939: ldc2_w 4.0
      // 093c: dmul
      // 093d: dload 30
      // 093f: dmul
      // 0940: dadd
      // 0941: d2f
      // 0942: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 0945: aload 0
      // 0946: aload 0
      // 0947: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 094a: f2d
      // 094b: dload 24
      // 094d: dconst_0
      // 094e: dcmpl
      // 094f: ifle 0965
      // 0952: ldc2_w 0.1
      // 0955: dload 26
      // 0957: invokestatic java/lang/Math.abs (D)D
      // 095a: dmul
      // 095b: ldc2_w 4.0
      // 095e: dmul
      // 095f: dload 30
      // 0961: dmul
      // 0962: goto 0979
      // 0965: ldc2_w 0.1
      // 0968: dload 26
      // 096a: invokestatic java/lang/Math.abs (D)D
      // 096d: dmul
      // 096e: ldc2_w -1.0
      // 0971: dmul
      // 0972: ldc2_w 4.0
      // 0975: dmul
      // 0976: dload 30
      // 0978: dmul
      // 0979: dadd
      // 097a: d2f
      // 097b: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 097e: aload 1
      // 097f: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0982: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 0985: dconst_0
      // 0986: dcmpl
      // 0987: ifle 099b
      // 098a: aload 0
      // 098b: aload 0
      // 098c: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$climbCount F
      // 098f: f2d
      // 0990: ldc2_w 0.1
      // 0993: dload 30
      // 0995: dmul
      // 0996: dadd
      // 0997: d2f
      // 0998: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$climbCount F
      // 099b: aload 1
      // 099c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 099f: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 09a2: dconst_0
      // 09a3: dcmpg
      // 09a4: ifge 09b8
      // 09a7: aload 0
      // 09a8: aload 0
      // 09a9: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$climbCount F
      // 09EventMotion: f2d
      // 09ad: ldc2_w 0.1
      // 09b0: dload 30
      // 09b2: dmul
      // 09b3: dsub
      // 09b4: d2f
      // 09b5: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$climbCount F
      // 09b8: aload 1
      // 09b9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 09bc: ifeq 09cd
      // 09bf: aload 15
      // 09c1: getfield ru/white/module/impl/render/qj.climbAndCrawl Lq2/z/BooleanSetting;
      // 09c4: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 09c7: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 09ConfigFile: ifne 09f8
      // 09cd: aload 1
      // 09ce: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 09d1: ifeq 0a53
      // 09d4: aload 1
      // 09d5: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 09d8: ifne 0a53
      // 09db: aload 1
      // 09EventKey: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 09df: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 09e2: invokestatic java/lang/Math.abs (D)D
      // 09e5: dconst_0
      // 09e6: dcmpl
      // 09e7: ifle 0a53
      // 09ea: aload 15
      // 09EventAttack: getfield ru/white/module/impl/render/qj.climbAndCrawl Lq2/z/BooleanSetting;
      // 09ef: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 09f2: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 09f5: ifeq 0a53
      // 09f8: aload 1
      // 09f9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 09EventMove: ifne 0a53
      // 09Category: fload 7
      // 0a01: fconst_0
      // 0a02: fcmpl
      // 0a03: ifne 0a53
      // 0a06: aload 0
      // 0a07: aload 0
      // 0a08: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a0b: f2d
      // 0a0c: ldc2_w 0.1
      // 0a0f: dload 30
      // 0a11: dmul
      // 0a12: dadd
      // 0a13: d2f
      // 0a14: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a17: aload 0
      // 0a18: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a1b: fconst_1
      // 0a1c: fcmpl
      // 0a1d: ifle 0a25
      // 0a20: aload 0
      // 0a21: fconst_1
      // 0a22: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a25: aload 8
      // 0a27: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 0a2a: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0a2d: ifne 0a66
      // 0a30: aload 8
      // 0a32: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 0a35: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0a38: ifne 0a66
      // 0a3b: aload 10
      // 0a3d: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0a40: ldc_w -20.0
      // 0a43: aload 0
      // 0a44: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a47: fmul
      // 0a48: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0a4d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0a50: goto 0a66
      // 0a53: aload 0
      // 0a54: aload 0
      // 0a55: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a58: f2d
      // 0a59: ldc2_w 0.8799999952316284
      // 0a5c: dload 30
      // 0a5e: invokestatic java/lang/Math.pow (DD)D
      // 0a61: dmul
      // 0a62: d2f
      // 0a63: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a66: fload 7
      // 0a68: fconst_0
      // 0a69: fcmpl
      // 0a6a: ifne 0ab0
      // 0a6d: aload 10
      // 0a6f: iload 29
      // 0a71: ifeq 0a88
      // 0a74: aload 1
      // 0a75: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0a78: ldc_w 650.0
      // 0a7b: fdiv
      // 0a7c: aload 0
      // 0a7d: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a80: fmul
      // 0a81: ldc_w -1.0
      // 0a84: fmul
      // 0a85: goto 0a95
      // 0a88: aload 1
      // 0a89: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0a8c: ldc_w 650.0
      // 0a8f: fdiv
      // 0a90: aload 0
      // 0a91: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0a94: fmul
      // 0a95: fconst_0
      // 0a96: fconst_0
      // 0a97: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 0a9a: aload 10
      // 0a9c: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0a9f: aload 1
      // 0aa0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0aa3: aload 0
      // 0aa4: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0aa7: fmul
      // 0aa8: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0aad: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0ab0: aload 8
      // 0ab2: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 0ab5: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0ab8: ifne 0add
      // 0abb: aload 8
      // 0abd: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 0ac0: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0ac3: ifne 0add
      // 0ac6: aload 10
      // 0ac8: fconst_0
      // 0ac9: fconst_0
      // 0aca: aload 1
      // 0acb: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0ace: ldc_w 120.0
      // 0ad1: fdiv
      // 0ad2: aload 0
      // 0ad3: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0ad6: fmul
      // 0ad7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 0ada: goto 0af8
      // 0add: fload 7
      // 0adf: fconst_0
      // 0ae0: fcmpl
      // 0ae1: ifne 0af8
      // 0ae4: aload 10
      // 0ae6: fconst_0
      // 0ae7: fconst_0
      // 0ae8: aload 1
      // 0ae9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 0aec: ldc_w 80.0
      // 0aef: fdiv
      // 0af0: aload 0
      // 0af1: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$clCount F
      // 0af4: fmul
      // 0af5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 0af8: aload 1
      // 0af9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 0afc: ifeq 0b3d
      // 0aff: aload 15
      // 0b01: getfield ru/white/module/impl/render/qj.climbAndCrawl Lq2/z/BooleanSetting;
      // 0b04: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 0b07: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0b0a: ifeq 0b3d
      // 0b0d: aload 1
      // 0b0e: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 0b11: ifne 0b3d
      // 0b14: aload 8
      // 0b16: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 0b19: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0b1c: ifne 0b3d
      // 0b1f: aload 8
      // 0b21: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 0b24: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0b27: ifne 0b3d
      // 0b2a: aload 1
      // 0b2b: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 0b2e: ifne 0b3d
      // 0b31: aload 10
      // 0b33: dconst_0
      // 0b34: ldc2_w 0.1
      // 0b37: ldc2_w -0.2
      // 0b3a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0b3d: aload 1
      // 0b3e: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_52535 ()Z
      // 0b41: ifeq 0b74
      // 0b44: aload 1
      // 0b45: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 0b48: ifne 0b74
      // 0b4b: aload 1
      // 0b4c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5869 ()Z
      // 0b4f: ifne 0b74
      // 0b52: aload 0
      // 0b53: aload 0
      // 0b54: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b57: f2d
      // 0b58: ldc2_w 0.1
      // 0b5b: dload 30
      // 0b5d: dmul
      // 0b5e: dadd
      // 0b5f: d2f
      // 0b60: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b63: aload 0
      // 0b64: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b67: fconst_1
      // 0b68: fcmpl
      // 0b69: iflt 0b87
      // 0b6c: aload 0
      // 0b6d: fconst_1
      // 0b6e: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b71: goto 0b87
      // 0b74: aload 0
      // 0b75: aload 0
      // 0b76: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b79: f2d
      // 0b7a: ldc2_w 0.8799999952316284
      // 0b7d: dload 30
      // 0b7f: invokestatic java/lang/Math.pow (DD)D
      // 0b82: dmul
      // 0b83: d2f
      // 0b84: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0b87: aload 0
      // 0b88: aload 0
      // 0b89: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$freezeCounter F
      // 0b8c: f2d
      // 0b8d: ldc2_w 0.8799999952316284
      // 0b90: dload 30
      // 0b92: invokestatic java/lang/Math.pow (DD)D
      // 0b95: dmul
      // 0b96: d2f
      // 0b97: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$freezeCounter F
      // 0b9a: aload 10
      // 0b9c: dconst_0
      // 0b9d: ldc2_w 0.02
      // 0ba0: aload 0
      // 0ba1: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0ba4: f2d
      // 0ba5: dmul
      // 0ba6: dconst_0
      // 0ba7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0baa: aload 10
      // 0bac: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0baf: ldc_w 8.0
      // 0bb2: fload 6
      // 0bb4: fmul
      // 0bb5: aload 0
      // 0bb6: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$inWaterCounter F
      // 0bb9: fmul
      // 0bba: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0bbf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0bc2: aload 10
      // 0bc4: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0bc7: ldc_w 0.3
      // 0bca: aload 0
      // 0bcb: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$freezeCounter F
      // 0bce: ldc_w 5.0
      // 0bd1: fmul
      // 0bd2: f2d
      // 0bd3: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 0bd6: fmul
      // 0bd7: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0bdc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0bdf: aload 1
      // 0be0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0be3: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 0be6: ldc2_w -0.85
      // 0be9: dcmpg
      // 0bea: ifge 0c23
      // 0bed: aload 8
      // 0bef: getstatic net/minecraft/item/Items.field_49814 Lnet/minecraft/item/Item;
      // 0bf2: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0bf5: ifeq 0c23
      // 0bf8: aload 1
      // 0bf9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6047 ()Lnet/minecraft/item/ItemStack;
      // 0bfc: aload 8
      // 0bfe: if_acmpne 0c23
      // 0c01: aload 0
      // 0c02: aload 0
      // 0c03: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c06: f2d
      // 0c07: ldc2_w 0.1
      // 0c0a: dload 30
      // 0c0c: dmul
      // 0c0d: dadd
      // 0c0e: d2f
      // 0c0f: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c12: aload 0
      // 0c13: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c16: fconst_1
      // 0c17: fcmpl
      // 0c18: iflt 0c36
      // 0c1b: aload 0
      // 0c1c: fconst_1
      // 0c1d: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c20: goto 0c36
      // 0c23: aload 0
      // 0c24: aload 0
      // 0c25: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c28: f2d
      // 0c29: ldc2_w 0.8799999952316284
      // 0c2c: dload 30
      // 0c2e: invokestatic java/lang/Math.pow (DD)D
      // 0c31: dmul
      // 0c32: d2f
      // 0c33: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c36: iload 29
      // 0c38: ifeq 0c60
      // 0c3b: aload 10
      // 0c3d: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0c40: ldc_w 45.0
      // 0c43: aload 0
      // 0c44: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c47: fmul
      // 0c48: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0c4d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0c50: aload 10
      // 0c52: dconst_0
      // 0c53: ldc2_w -0.2
      // 0c56: aload 0
      // 0c57: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$fallCounter F
      // 0c5a: f2d
      // 0c5b: dmul
      // 0c5c: dconst_0
      // 0c5d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0c60: aload 0
      // 0c61: aload 0
      // 0c62: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c65: f2d
      // 0c66: aload 1
      // 0c67: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0c6a: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 0c6d: ldc2_w 0.014999999664723873
      // 0c70: dmul
      // 0c71: dload 30
      // 0c73: dmul
      // 0c74: dadd
      // 0c75: d2f
      // 0c76: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c79: aload 0
      // 0c7a: aload 0
      // 0c7b: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c7e: f2d
      // 0c7f: ldc_w 0.1
      // 0c82: aload 0
      // 0c83: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c86: fmul
      // 0c87: f2d
      // 0c88: dload 30
      // 0c8a: dmul
      // 0c8b: dsub
      // 0c8c: d2f
      // 0c8d: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c90: aload 0
      // 0c91: aload 0
      // 0c92: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0c95: f2d
      // 0c96: ldc2_w 0.8799999952316284
      // 0c99: dload 30
      // 0c9b: invokestatic java/lang/Math.pow (DD)D
      // 0c9e: dmul
      // 0c9f: d2f
      // 0ca0: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0ca3: aload 0
      // 0ca4: aload 0
      // 0ca5: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0ca8: f2d
      // 0ca9: aload 1
      // 0caa: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0cad: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 0cb0: ldc2_w 0.014999999664723873
      // 0cb3: dmul
      // 0cb4: dload 30
      // 0cb6: dmul
      // 0cb7: dadd
      // 0cb8: d2f
      // 0cb9: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0cbc: aload 0
      // 0cbd: aload 0
      // 0cbe: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0cc1: f2d
      // 0cc2: ldc_w 0.1
      // 0cc5: aload 0
      // 0cc6: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 0cc9: fmul
      // 0cca: f2d
      // 0ccb: dload 30
      // 0ccd: dmul
      // 0cce: dsub
      // 0ccf: d2f
      // 0cd0: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0cd3: aload 0
      // 0cd4: aload 0
      // 0cd5: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0cd8: f2d
      // 0cd9: ldc2_w 0.8799999952316284
      // 0cdc: dload 30
      // 0cde: invokestatic java/lang/Math.pow (DD)D
      // 0ce1: dmul
      // 0ce2: d2f
      // 0ce3: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0ce6: aload 0
      // 0ce7: aload 0
      // 0ce8: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 0ceb: f2d
      // 0cec: aload 0
      // 0ced: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 0cf0: f2d
      // 0cf1: dload 30
      // 0cf3: dmul
      // 0cf4: dadd
      // 0cf5: d2f
      // 0cf6: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 0cf9: aload 10
      // 0cfb: fconst_0
      // 0cfc: aload 0
      // 0cfd: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleY F
      // 0d00: ldc_w -1.0
      // 0d03: fmul
      // 0d04: fconst_0
      // 0d05: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 0d08: aload 10
      // 0d0a: dconst_0
      // 0d0b: aload 1
      // 0d0c: getfield net/minecraft/client/network/AbstractClientPlayerEntity.field_6012 I
      // 0d0f: i2d
      // 0d10: ldc2_w 0.1
      // 0d13: dmul
      // 0d14: invokestatic java/lang/Math.sin (D)D
      // 0d17: ldc2_w 0.007
      // 0d1a: dmul
      // 0d1b: fload 6
      // 0d1d: f2d
      // 0d1e: dmul
      // 0d1f: dconst_0
      // 0d20: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0d23: aload 10
      // 0d25: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0d28: ldc_w 0.15
      // 0d2b: aload 1
      // 0d2c: getfield net/minecraft/client/network/AbstractClientPlayerEntity.field_6012 I
      // 0d2f: i2f
      // 0d30: ldc_w 0.15
      // 0d33: fmul
      // 0d34: f2d
      // 0d35: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 0d38: fmul
      // 0d39: fload 6
      // 0d3b: fmul
      // 0d3c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0d41: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0d44: aload 8
      // 0d46: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 0d49: ifeq 0d68
      // 0d4c: aload 1
      // 0d4d: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 0d50: ifne 0d68
      // 0d53: aload 1
      // 0d54: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 0d57: ifeq 0d61
      // 0d5a: aload 1
      // 0d5b: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 0d5e: ifeq 0d68
      // 0d61: aload 1
      // 0d62: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 0d65: ifeq 0da0
      // 0d68: aload 1
      // 0d69: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6068 ()Lnet/minecraft/util/Arm;
      // 0d6c: getstatic net/minecraft/util/Arm.field_6182 Lnet/minecraft/util/Arm;
      // 0d6f: if_acmpne 0d7e
      // 0d72: iload 29
      // 0d74: ifne 0d7b
      // 0d77: bipush 1
      // 0d78: goto 0d7c
      // 0d7b: bipush 0
      // 0d7c: istore 29
      // 0d7e: aload 8
      // 0d80: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 0d83: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 0d86: if_acmpne 0d94
      // 0d89: aload 10
      // 0d8b: fconst_0
      // 0d8c: fconst_0
      // 0d8d: fconst_0
      // 0d8e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 0d91: goto 0da0
      // 0d94: aload 10
      // 0d96: dconst_0
      // 0d97: ldc2_w -0.1
      // 0d9a: ldc2_w 0.1
      // 0d9d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0da0: aload 8
      // 0da2: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 0da5: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0da8: ifne 0dc1
      // 0dab: aload 8
      // 0dad: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 0db0: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0db3: ifne 0dc1
      // 0db6: aload 8
      // 0db8: getstatic net/minecraft/registry/tag/ItemTags.field_40108 Lnet/minecraft/registry/tag/TagKey;
      // 0dbb: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 0dbe: ifeq 0dde
      // 0dc1: aload 10
      // 0dc3: dconst_0
      // 0dc4: ldc2_w 0.1
      // 0dc7: dconst_0
      // 0dc8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0dcb: aload 1
      // 0dcc: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 0dcf: ifeq 0dde
      // 0dd2: aload 10
      // 0dd4: dconst_0
      // 0dd5: ldc2_w -0.1
      // 0dd8: ldc2_w 0.1
      // 0ddb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0dde: aload 1
      // 0ddf: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 0de2: ifeq 0f45
      // 0de5: fload 7
      // 0de7: fconst_0
      // 0de8: fcmpl
      // 0de9: ifne 0f45
      // 0dec: aload 15
      // 0dee: getfield ru/white/module/impl/render/qj.swimmingAnimation Lq2/z/BooleanSetting;
      // 0df1: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 0df4: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0df7: ifeq 0f45
      // 0dfa: aload 0
      // 0dfb: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$crawlCount F
      // 0dfe: f2d
      // 0dff: dstore 37
      // 0e01: ldc2_w 1.5
      // 0e04: dstore 39
      // 0e06: ldc2_w 2.0
      // 0e09: dstore 41
      // 0e0b: dload 37
      // 0e0d: dload 41
      // 0e0f: dmul
      // 0e10: dstore 43
      // 0e12: dload 43
      // 0e14: invokestatic java/lang/Math.sin (D)D
      // 0e17: dload 39
      // 0e19: dmul
      // 0e1a: dstore 45
      // 0e1c: dload 45
      // 0e1e: ldc2_w 0.8
      // 0e21: dmul
      // 0e22: aload 0
      // 0e23: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$previousRotation D
      // 0e26: ldc2_w 0.2
      // 0e29: dmul
      // 0e2a: dadd
      // 0e2b: dstore 47
      // 0e2d: aload 10
      // 0e2f: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0e32: iload 29
      // 0e34: ifeq 0e3c
      // 0e37: dload 47
      // 0e39: goto 0e3f
      // 0e3c: dload 47
      // 0e3e: dneg
      // 0e3f: d2f
      // 0e40: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0e45: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0e48: aload 10
      // 0e4a: dconst_0
      // 0e4b: dconst_0
      // 0e4c: dload 47
      // 0e4e: ldc2_w 0.20000000298023224
      // 0e51: dmul
      // 0e52: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0e55: aload 0
      // 0e56: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$crawlCount F
      // 0e59: fconst_2
      // 0e5a: fmul
      // 0e5b: f2d
      // 0e5c: dstore 49
      // 0e5e: dload 49
      // 0e60: invokestatic java/lang/Math.cos (D)D
      // 0e63: dstore 51
      // 0e65: dload 51
      // 0e67: dstore 53
      // 0e69: dload 51
      // 0e6b: dconst_0
      // 0e6c: dcmpg
      // 0e6d: ifgt 0e78
      // 0e70: dload 51
      // 0e72: ldc2_w 0.5
      // 0e75: dmul
      // 0e76: dstore 53
      // 0e78: aload 10
      // 0e7a: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 0e7d: iload 29
      // 0e7f: ifeq 0e8b
      // 0e82: dload 53
      // 0e84: ldc2_w 30.0
      // 0e87: dmul
      // 0e88: goto 0e95
      // 0e8b: dload 53
      // 0e8d: ldc2_w 30.0
      // 0e90: dmul
      // 0e91: ldc2_w -1.0
      // 0e94: dmul
      // 0e95: d2f
      // 0e96: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0e9b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0e9e: aload 10
      // 0ea0: dconst_0
      // 0ea1: dconst_0
      // 0ea2: dload 51
      // 0ea4: ldc2_w 0.20000000298023224
      // 0ea7: dmul
      // 0ea8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0eab: aload 8
      // 0ead: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 0eb0: ifeq 0f3f
      // 0eb3: iload 29
      // 0eb5: ifne 0f3f
      // 0eb8: aload 1
      // 0eb9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5767 ()Z
      // 0ebc: ifne 0f3f
      // 0ebf: aload 10
      // 0ec1: fconst_1
      // 0ec2: fload 6
      // 0ec4: fmul
      // 0ec5: f2d
      // 0ec6: dconst_0
      // 0ec7: fload 9
      // 0ec9: f2d
      // 0eca: ldc2_w 0.3
      // 0ecd: dmul
      // 0ece: dsub
      // 0ecf: ldc2_w 0.3
      // 0ed2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0ed5: aload 10
      // 0ed7: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0eda: ldc_w 45.0
      // 0edd: fload 6
      // 0edf: fmul
      // 0ee0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0ee5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0ee8: aload 10
      // 0eea: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 0eed: ldc_w -40.0
      // 0ef0: fload 6
      // 0ef2: fmul
      // 0ef3: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0ef8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0efb: aload 10
      // 0efd: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0f00: ldc_w 30.0
      // 0f03: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0f08: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0f0b: aload 0
      // 0f0c: aload 10
      // 0f0e: aload 5
      // 0f10: fload 7
      // 0f12: aload 8
      // 0f14: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 0f17: fload 9
      // 0f19: ldc_w 3.14
      // 0f1c: fmul
      // 0f1d: f2d
      // 0f1e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 0f21: fstore 55
      // 0f23: aload 10
      // 0f25: ldc_w 0.9
      // 0f28: ldc_w 0.9
      // 0f2b: ldc_w 0.9
      // 0f2e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 0f31: aload 0
      // 0f32: aload 10
      // 0f34: aload 11
      // 0f36: iload 12
      // 0f38: fconst_0
      // 0f39: fconst_0
      // 0f3a: aload 5
      // 0f3c: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 0f3f: aload 0
      // 0f40: dload 47
      // 0f42: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$previousRotation D
      // 0f45: aload 1
      // 0f46: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 0f49: ifeq 0f53
      // 0f4c: aload 1
      // 0f4d: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 0f50: ifeq 0f61
      // 0f53: aload 1
      // 0f54: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 0f57: ifeq 1122
      // 0f5a: fload 7
      // 0f5c: fconst_0
      // 0f5d: fcmpl
      // 0f5e: ifne 1122
      // 0f61: aload 1
      // 0f62: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 0f65: ifne 1122
      // 0f68: aload 0
      // 0f69: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$climbCount F
      // 0f6c: f2d
      // 0f6d: dstore 37
      // 0f6f: aload 1
      // 0f70: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 0f73: invokevirtual net/minecraft/util/math/Vec3d.method_10214 ()D
      // 0f76: d2f
      // 0f77: fstore 39
      // 0f79: dload 37
      // 0f7b: d2f
      // 0f7c: fconst_2
      // 0f7d: fmul
      // 0f7e: f2d
      // 0f7f: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 0f82: fstore 40
      // 0f84: aload 1
      // 0f85: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 0f88: ifeq 0fce
      // 0f8b: aload 8
      // 0f8d: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 0f90: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0f93: ifne 0fba
      // 0f96: aload 8
      // 0f98: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 0f9b: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 0f9e: ifne 0fba
      // 0fa1: aload 10
      // 0fa3: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0fa6: ldc_w 20.0
      // 0fa9: fload 40
      // 0fab: fmul
      // 0fac: fload 6
      // 0fae: fmul
      // 0faf: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0fb4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0fb7: goto 0fce
      // 0fba: aload 10
      // 0fbc: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0fbf: fconst_1
      // 0fc0: fload 40
      // 0fc2: fmul
      // 0fc3: fload 6
      // 0fc5: fmul
      // 0fc6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0fcb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0fce: aload 1
      // 0fcf: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 0fd2: ifeq 1085
      // 0fd5: aload 1
      // 0fd6: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 0fd9: ifne 1085
      // 0fdc: fload 7
      // 0fde: fconst_0
      // 0fdf: fcmpl
      // 0fe0: ifne 1085
      // 0fe3: aload 0
      // 0fe4: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 0fe7: ldc_w 4.0
      // 0fea: fmul
      // 0feb: aload 0
      // 0fec: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$mouseHolding F
      // 0fef: fmul
      // 0ff0: f2d
      // 0ff1: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 0ff4: fstore 41
      // 0ff6: aload 0
      // 0ff7: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$directionalCrawlCount F
      // 0ffa: ldc_w 4.0
      // 0ffd: fmul
      // 0ffe: aload 0
      // 0fff: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$mouseHolding F
      // 1002: fmul
      // 1003: f2d
      // 1004: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 1007: fstore 42
      // 1009: aload 8
      // 100b: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 100e: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 1011: ifne 101f
      // 1014: aload 8
      // 1016: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 1019: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 101c: ifeq 102f
      // 101f: fload 41
      // 1021: ldc_w 0.14
      // 1024: fmul
      // 1025: fstore 41
      // 1027: fload 42
      // 1029: ldc_w 0.14
      // 102c: fmul
      // 102d: fstore 42
      // 102f: aload 10
      // 1031: ldc2_w 0.2
      // 1034: fload 41
      // 1036: f2d
      // 1037: dmul
      // 1038: ldc2_w 0.3
      // 103b: fload 41
      // 103d: f2d
      // 103e: dmul
      // 103f: fload 6
      // 1041: f2d
      // 1042: dmul
      // 1043: ldc2_w -0.2
      // 1046: fload 41
      // 1048: f2d
      // 1049: dmul
      // 104a: fload 6
      // 104c: f2d
      // 104d: dmul
      // 104e: fload 28
      // 1050: f2d
      // 1051: dmul
      // 1052: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1055: aload 10
      // 1057: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 105a: ldc_w 25.0
      // 105d: fload 41
      // 105f: fmul
      // 1060: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1065: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1068: aload 10
      // 106a: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 106d: ldc_w 20.0
      // 1070: fload 42
      // 1072: fmul
      // 1073: fload 6
      // 1075: fmul
      // 1076: fconst_0
      // 1077: ldc_w 20.0
      // 107a: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 107d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1082: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1085: aload 8
      // 1087: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 108a: ifeq 1122
      // 108d: iload 29
      // 108f: ifne 1122
      // 1092: aload 1
      // 1093: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5767 ()Z
      // 1096: ifne 1122
      // 1099: aload 1
      // 109a: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 109d: ifne 10a7
      // 10a0: aload 1
      // 10a1: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 10a4: ifne 10ae
      // 10a7: aload 1
      // 10a8: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 10ab: ifeq 1122
      // 10ae: aload 10
      // 10b0: fconst_1
      // 10b1: fload 6
      // 10b3: fmul
      // 10b4: f2d
      // 10b5: dconst_0
      // 10b6: fload 9
      // 10b8: f2d
      // 10b9: ldc2_w 0.3
      // 10bc: dmul
      // 10bd: dsub
      // 10be: ldc2_w 0.3
      // 10c1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 10c4: aload 10
      // 10c6: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 10c9: ldc_w 45.0
      // 10EventPacket: fload 6
      // 10ce: fmul
      // 10cf: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 10d4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 10d7: aload 10
      // 10d9: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 10EventKey: ldc_w -40.0
      // 10df: fload 6
      // 10e1: fmul
      // 10e2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 10e7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 10ea: aload 10
      // 10EventAttack: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 10ef: ldc_w 30.0
      // 10f2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 10f7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 10fa: aload 0
      // 10fb: aload 10
      // 10fd: aload 5
      // 10Category: fload 7
      // 1101: aload 8
      // 1103: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 1106: aload 10
      // 1108: ldc_w 0.9
      // 110b: ldc_w 0.9
      // 110e: ldc_w 0.9
      // 1111: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 1114: aload 0
      // 1115: aload 10
      // 1117: aload 11
      // 1119: iload 12
      // 111b: fconst_0
      // 111c: fconst_0
      // 111d: aload 5
      // 111f: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1122: aload 8
      // 1124: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 1127: ifeq 1280
      // 112a: iload 29
      // 112c: ifeq 2fc0
      // 112f: aload 1
      // 1130: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5767 ()Z
      // 1133: ifne 2fc0
      // 1136: aload 1
      // 1137: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 113a: ifne 1144
      // 113d: aload 1
      // 113e: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 1141: ifne 11fd
      // 1144: aload 1
      // 1145: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 1148: ifne 11fd
      // 114b: aload 1
      // 114c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 114f: ifne 11fd
      // 1152: aload 1
      // 1153: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6068 ()Lnet/minecraft/util/Arm;
      // 1156: getstatic net/minecraft/util/Arm.field_6182 Lnet/minecraft/util/Arm;
      // 1159: if_acmpne 1168
      // 115c: iload 29
      // 115e: ifne 1165
      // 1161: bipush 1
      // 1162: goto 1166
      // 1165: bipush 0
      // 1166: istore 29
      // 1168: aload 10
      // 116a: dconst_0
      // 116b: ldc2_w 0.2
      // 116e: fload 34
      // 1170: f2d
      // 1171: dmul
      // 1172: ldc2_w 0.15
      // 1175: fload 34
      // 1177: f2d
      // 1178: dmul
      // 1179: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 117c: aload 10
      // 117e: ldc2_w 0.1
      // 1181: fload 6
      // 1183: f2d
      // 1184: dmul
      // 1185: fload 35
      // 1187: f2d
      // 1188: dmul
      // 1189: ldc2_w 0.15
      // 118c: fload 35
      // 118e: f2d
      // 118f: dmul
      // 1190: ldc2_w -0.45
      // 1193: fload 35
      // 1195: f2d
      // 1196: dmul
      // 1197: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 119a: aload 10
      // 119c: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 119f: ldc_w 35.0
      // 11a2: fload 35
      // 11a4: fmul
      // 11a5: fload 6
      // 11a7: fmul
      // 11a8: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 11ad: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 11b0: aload 10
      // 11b2: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 11b5: ldc_w -30.0
      // 11b8: fload 35
      // 11ba: fmul
      // 11DraggableWidget: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 11c0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 11c3: aload 10
      // 11c5: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 11c8: ldc_w -10.0
      // 11cb: fload 34
      // 11cd: fmul
      // 11ce: fload 6
      // 11d0: fmul
      // 11d1: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 11d6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 11d9: aload 10
      // 11db: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 11de: ldc_w 10.0
      // 11e1: fload 34
      // 11e3: fmul
      // 11e4: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 11e9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 11EventAttack: aload 0
      // 11ed: aload 10
      // 11ef: aload 11
      // 11f1: iload 12
      // 11f3: fconst_0
      // 11f4: fconst_0
      // 11f5: aload 5
      // 11f7: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 11fa: goto 2fc0
      // 11fd: aload 10
      // 11Category: fconst_1
      // 1200: fload 6
      // 1202: fmul
      // 1203: f2d
      // 1204: dconst_0
      // 1205: fload 9
      // 1207: f2d
      // 1208: ldc2_w 0.3
      // 120b: dmul
      // 120c: dsub
      // 120d: ldc2_w 0.3
      // 1210: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1213: aload 10
      // 1215: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1218: ldc_w 45.0
      // 121b: fload 6
      // 121d: fmul
      // 121e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1223: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1226: aload 10
      // 1228: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 122b: ldc_w -40.0
      // 122e: fload 6
      // 1230: fmul
      // 1231: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1236: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1239: aload 10
      // 123b: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 123e: ldc_w 30.0
      // 1241: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1246: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1249: aload 0
      // 124a: aload 10
      // 124c: aload 5
      // 124e: fload 7
      // 1250: aload 8
      // 1252: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 1255: fload 9
      // 1257: ldc_w 3.14
      // 125a: fmul
      // 125b: f2d
      // 125c: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 125f: fstore 37
      // 1261: aload 10
      // 1263: ldc_w 0.9
      // 1266: ldc_w 0.9
      // 1269: ldc_w 0.9
      // 126c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 126f: aload 0
      // 1270: aload 10
      // 1272: aload 11
      // 1274: iload 12
      // 1276: fconst_0
      // 1277: fconst_0
      // 1278: aload 5
      // 127a: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 127d: goto 2fc0
      // 1280: aload 8
      // 1282: getstatic net/minecraft/component/DataComponentTypes.field_49646 Lnet/minecraft/component/ComponentType;
      // 1285: invokevirtual net/minecraft/item/ItemStack.method_57826 (Lnet/minecraft/component/ComponentType;)Z
      // 1288: ifeq 12e2
      // 128b: iload 29
      // 128d: ifeq 12b6
      // 1290: aload 0
      // 1291: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4047 Lnet/minecraft/item/ItemStack;
      // 1294: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 1297: ifeq 12b6
      // 129a: aload 10
      // 129c: dconst_0
      // 129d: ldc2_w 0.1
      // 12a0: dconst_0
      // 12a1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 12a4: aload 0
      // 12a5: aload 10
      // 12a7: aload 11
      // 12a9: iload 12
      // 12ab: fload 3
      // 12EventMotion: fload 9
      // 12ae: fload 7
      // 12b0: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3231 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFF)V
      // 12b3: goto 2fc0
      // 12b6: aload 10
      // 12b8: iload 29
      // 12ba: ifeq 12c3
      // 12bd: ldc2_w -0.1
      // 12c0: goto 12c6
      // 12c3: ldc2_w 0.1
      // 12c6: ldc2_w 0.1
      // 12c9: dconst_0
      // 12ConfigFile: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 12cd: aload 0
      // 12ce: aload 10
      // 12d0: aload 11
      // 12d2: iload 12
      // 12d4: fload 9
      // 12d6: aload 5
      // 12d8: fload 7
      // 12ConfigManager: aload 8
      // 12EventKey: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3222 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFLnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 12df: goto 2fc0
      // 12e2: aload 8
      // 12e4: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 12e7: getstatic net/minecraft/item/consume/UseAction.field_8947 Lnet/minecraft/item/consume/UseAction;
      // 12ea: if_acmpne 1685
      // 12ed: aload 10
      // 12ef: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 12f2: aload 8
      // 12f4: invokestatic net/minecraft/item/CrossbowItem.method_7781 (Lnet/minecraft/item/ItemStack;)Z
      // 12f7: istore 37
      // 12f9: aload 5
      // 12fb: getstatic net/minecraft/util/Arm.field_6183 Lnet/minecraft/util/Arm;
      // 12fe: if_acmpne 1305
      // 1301: bipush 1
      // 1302: goto 1306
      // 1305: bipush 0
      // 1306: istore 38
      // 1308: iload 38
      // 130a: ifeq 1311
      // 130d: bipush 1
      // 130e: goto 1312
      // 1311: bipush -1
      // 1312: istore 39
      // 1314: aload 1
      // 1315: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 1318: ifeq 1415
      // 131b: aload 1
      // 131c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 131f: ifle 1415
      // 1322: aload 1
      // 1323: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6058 ()Lnet/minecraft/util/Hand;
      // 1326: aload 4
      // 1328: if_acmpne 1415
      // 132b: aload 0
      // 132c: aload 10
      // 132e: aload 5
      // 1330: fload 9
      // 1332: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3224 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V
      // 1335: aload 10
      // 1337: iload 39
      // 1339: i2f
      // 133a: ldc_w -0.4785682
      // 133d: fmul
      // 133e: ldc_w -0.24387
      // 1341: ldc_w 0.05731531
      // 1344: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1347: aload 10
      // 1349: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 134c: ldc_w -11.935
      // 134f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1354: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1357: aload 10
      // 1359: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 135c: iload 39
      // 135e: i2f
      // 135f: ldc_w 65.3
      // 1362: fmul
      // 1363: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1368: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 136b: aload 10
      // 136d: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1370: iload 39
      // 1372: i2f
      // 1373: ldc_w 9.785
      // 1376: fmul
      // 1377: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 137c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 137f: aload 8
      // 1381: aload 1
      // 1382: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 1385: i2f
      // 1386: aload 1
      // 1387: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 138a: i2f
      // 138b: fload 2
      // 138c: fsub
      // 138d: fconst_1
      // 138e: fadd
      // 138f: fsub
      // 1390: fstore 40
      // 1392: fload 40
      // 1394: aload 8
      // 1396: aload 1
      // 1397: invokestatic net/minecraft/item/CrossbowItem.method_7775 (Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)I
      // 139a: i2f
      // 139b: fdiv
      // 139c: fstore 41
      // 139e: fload 41
      // 13a0: fconst_1
      // 13a1: fcmpl
      // 13a2: ifle 13a8
      // 13a5: fconst_1
      // 13a6: fstore 41
      // 13a8: fload 41
      // 13aa: ldc_w 0.1
      // 13ad: fcmpl
      // 13ae: ifle 13e3
      // 13b1: fload 40
      // 13b3: ldc_w 0.1
      // 13b6: fsub
      // 13b7: ldc_w 1.3
      // 13ba: fmul
      // 13DraggableWidget: f2d
      // 13bc: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 13bf: fstore 42
      // 13c1: fload 41
      // 13c3: ldc_w 0.1
      // 13c6: fsub
      // 13c7: fstore 43
      // 13c9: fload 42
      // 13cb: fload 43
      // 13cd: fmul
      // 13ce: fstore 44
      // 13d0: aload 10
      // 13d2: fload 44
      // 13d4: fconst_0
      // 13d5: fmul
      // 13d6: fload 44
      // 13d8: ldc_w 0.004
      // 13db: fmul
      // 13EventKey: fload 44
      // 13de: fconst_0
      // 13df: fmul
      // 13e0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 13e3: aload 10
      // 13e5: fload 41
      // 13e7: fconst_0
      // 13e8: fmul
      // 13e9: fload 41
      // 13eb: fconst_0
      // 13EventAttack: fmul
      // 13ed: fload 41
      // 13ef: ldc_w 0.04
      // 13f2: fmul
      // 13f3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 13f6: aload 10
      // 13f8: fconst_1
      // 13f9: fconst_1
      // 13fa: fconst_1
      // 13fb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 13fe: aload 10
      // 1400: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 1403: iload 39
      // 1405: i2f
      // 1406: ldc_w 45.0
      // 1409: fmul
      // 140a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 140f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1412: goto 1456
      // 1415: aload 0
      // 1416: fload 7
      // 1418: aload 10
      // 141a: iload 39
      // 141c: aload 5
      // 141e: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_65816 (FLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V
      // 1421: iload 37
      // 1423: ifeq 1456
      // 1426: fload 7
      // 1428: ldc_w 0.001
      // 142b: fcmpg
      // 142c: ifge 1456
      // 142f: iload 29
      // 1431: ifeq 1456
      // 1434: aload 10
      // 1436: iload 39
      // 1438: i2f
      // 1439: ldc_w -0.341864
      // 143c: fmul
      // 143d: fconst_0
      // 143e: fconst_0
      // 143f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1442: aload 10
      // 1444: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1447: iload 39
      // 1449: i2f
      // 144a: ldc_w 10.0
      // 144d: fmul
      // 144e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1453: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1456: aload 10
      // 1458: fconst_0
      // 1459: fconst_0
      // 145a: ldc_w -1.0
      // 145d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1460: aload 10
      // 1462: ldc2_w -0.45
      // 1465: iload 39
      // 1467: i2d
      // 1468: dmul
      // 1469: ldc2_w 0.45
      // 146c: ldc2_w 1.7
      // 146f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1472: aload 10
      // 1474: fconst_1
      // 1475: fload 6
      // 1477: fmul
      // 1478: f2d
      // 1479: dconst_0
      // 147a: fload 9
      // 147c: f2d
      // 147d: ldc2_w 0.3
      // 1480: dmul
      // 1481: dsub
      // 1482: ldc2_w 0.3
      // 1485: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1488: aload 10
      // 148a: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 148d: ldc_w 45.0
      // 1490: fload 6
      // 1492: fmul
      // 1493: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1498: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 149b: aload 10
      // 149d: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 14a0: ldc_w -40.0
      // 14a3: fload 6
      // 14a5: fmul
      // 14a6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 14ab: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 14ae: aload 10
      // 14b0: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 14b3: ldc_w 30.0
      // 14b6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 14DraggableWidget: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 14be: aload 0
      // 14bf: aload 10
      // 14c1: aload 5
      // 14c3: fload 7
      // 14c5: aload 8
      // 14c7: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 14ConfigFile: fload 9
      // 14EventPacket: ldc_w 3.14
      // 14cf: fmul
      // 14d0: f2d
      // 14d1: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 14d4: fstore 40
      // 14d6: aload 10
      // 14d8: ldc_w 0.9
      // 14db: ldc_w 0.9
      // 14de: ldc_w 0.9
      // 14e1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 14e4: aload 0
      // 14e5: aload 10
      // 14e7: aload 11
      // 14e9: iload 12
      // 14eb: fconst_0
      // 14EventAttack: fconst_0
      // 14ed: aload 5
      // 14ef: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 14f2: aload 10
      // 14f4: ldc2_w -0.25
      // 14f7: iload 39
      // 14f9: i2d
      // 14fa: dmul
      // 14fb: ldc2_w 1.25
      // 14fe: ldc2_w 0.05
      // 1501: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1504: aload 10
      // 1506: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1509: bipush -90
      // 150b: iload 39
      // 150d: imul
      // 150e: i2f
      // 150f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1514: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1517: aload 10
      // 1519: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 151c: ldc_w 77.0
      // 151f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1524: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1527: aload 10
      // 1529: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 152c: bipush 85
      // 152e: iload 39
      // 1530: imul
      // 1531: i2f
      // 1532: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1537: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 153a: aload 10
      // 153c: ldc_w 1.2
      // 153f: ldc_w 1.2
      // 1542: ldc_w 1.2
      // 1545: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 1548: aload 10
      // 154a: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 154d: ldc_w -10.0
      // 1550: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1555: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1558: aload 10
      // 155a: dconst_0
      // 155b: ldc2_w -0.15
      // 155e: ldc2_w 0.15
      // 1561: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1564: aload 15
      // 1566: aload 10
      // 1568: aload 5
      // 156a: invokevirtual ru/white/module/impl/render/qj.applyHandScale (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;)V
      // 156d: aload 0
      // 156e: aload 1
      // 156f: aload 8
      // 1571: iload 38
      // 1573: ifeq 157c
      // 1576: getstatic net/minecraft/item/ItemDisplayContext.field_4322 Lnet/minecraft/item/ItemDisplayContext;
      // 1579: goto 157f
      // 157c: getstatic net/minecraft/item/ItemDisplayContext.field_4321 Lnet/minecraft/item/ItemDisplayContext;
      // 157f: aload 10
      // 1581: aload 11
      // 1583: iload 12
      // 1585: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3233 (Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V
      // 1588: aload 10
      // 158a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 158d: aload 1
      // 158e: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 1591: ifeq 1682
      // 1594: aload 1
      // 1595: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1598: ifle 1682
      // 159b: aload 1
      // 159c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6058 ()Lnet/minecraft/util/Hand;
      // 159f: aload 4
      // 15a1: if_acmpne 1682
      // 15a4: aload 8
      // 15a6: aload 1
      // 15a7: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 15aa: i2f
      // 15ab: aload 1
      // 15EventMotion: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 15af: i2f
      // 15b0: fload 2
      // 15b1: fsub
      // 15b2: fconst_1
      // 15b3: fadd
      // 15b4: fsub
      // 15b5: fstore 41
      // 15b7: fload 41
      // 15b9: aload 8
      // 15DraggableWidget: aload 1
      // 15bc: invokestatic net/minecraft/item/CrossbowItem.method_7775 (Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)I
      // 15bf: i2f
      // 15c0: fdiv
      // 15c1: fstore 42
      // 15c3: fload 42
      // 15c5: fconst_1
      // 15c6: fcmpl
      // 15c7: ifle 15cd
      // 15ConfigFile: fconst_1
      // 15cb: fstore 42
      // 15cd: fload 42
      // 15cf: ldc_w 0.1
      // 15d2: fcmpl
      // 15d3: ifle 1608
      // 15d6: fload 41
      // 15d8: ldc_w 0.1
      // 15db: fsub
      // 15EventKey: ldc_w 1.3
      // 15df: fmul
      // 15e0: f2d
      // 15e1: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 15e4: fstore 43
      // 15e6: fload 42
      // 15e8: ldc_w 0.1
      // 15eb: fsub
      // 15EventAttack: fstore 44
      // 15ee: fload 43
      // 15f0: fload 44
      // 15f2: fmul
      // 15f3: fstore 45
      // 15f5: aload 10
      // 15f7: fload 45
      // 15f9: fconst_0
      // 15fa: fmul
      // 15fb: fload 45
      // 15fd: ldc_w 0.004
      // 1600: fmul
      // 1601: fload 45
      // 1603: fconst_0
      // 1604: fmul
      // 1605: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1608: aload 10
      // 160a: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 160d: fload 42
      // 160f: f2d
      // 1610: ldc2_w 0.2
      // 1613: dcmpg
      // 1614: ifgt 1628
      // 1617: ldc_w 75.0
      // 161a: fload 42
      // 161c: fmul
      // 161d: ldc_w 5.0
      // 1620: fmul
      // 1621: iload 39
      // 1623: i2f
      // 1624: fmul
      // 1625: goto 162e
      // 1628: bipush 75
      // 162a: iload 39
      // 162c: imul
      // 162d: i2f
      // 162e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1633: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1636: aload 10
      // 1638: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 163b: ldc_w 10.0
      // 163e: fload 42
      // 1640: fmul
      // 1641: ldc_w 1.5
      // 1644: fmul
      // 1645: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 164a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 164d: aload 10
      // 164f: ldc2_w -0.37
      // 1652: iload 39
      // 1654: i2d
      // 1655: dmul
      // 1656: dconst_0
      // 1657: ldc2_w 0.6
      // 165a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 165d: aload 10
      // 165f: ldc2_w 0.15
      // 1662: fload 42
      // 1664: f2d
      // 1665: dmul
      // 1666: iload 39
      // 1668: i2d
      // 1669: dmul
      // 166a: dconst_0
      // 166b: dconst_0
      // 166c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 166f: aload 0
      // 1670: aload 10
      // 1672: aload 11
      // 1674: iload 12
      // 1676: fload 9
      // 1678: fload 7
      // 167a: aload 5
      // 167c: invokevirtual net/minecraft/util/Arm.method_5928 ()Lnet/minecraft/util/Arm;
      // 167f: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1682: goto 2fc0
      // 1685: aload 5
      // 1687: getstatic net/minecraft/util/Arm.field_6183 Lnet/minecraft/util/Arm;
      // 168a: if_acmpne 1691
      // 168d: bipush 1
      // 168e: goto 1692
      // 1691: bipush 0
      // 1692: istore 37
      // 1694: iload 37
      // 1696: ifeq 169d
      // 1699: bipush 1
      // 169a: goto 169e
      // 169d: bipush -1
      // 169e: istore 38
      // 16a0: aload 1
      // 16a1: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 16a4: ifeq 1e50
      // 16a7: aload 1
      // 16a8: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 16ab: ifle 1e50
      // 16ae: aload 1
      // 16af: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6058 ()Lnet/minecraft/util/Hand;
      // 16b2: aload 4
      // 16b4: if_acmpne 1e50
      // 16b7: getstatic ru/white/mixin/HeldItemRendererHmiMixin$1.$SwitchMap$net$minecraft$item$consume$UseAction [I
      // 16ba: aload 8
      // 16bc: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 16bf: invokevirtual net/minecraft/item/consume/UseAction.ordinal ()I
      // 16c2: iaload
      // 16c3: tableswitch 1930 1 8 45 58 58 342 764 1310 1627 1811
      // 16f0: aload 0
      // 16f1: aload 10
      // 16f3: aload 5
      // 16f5: fload 9
      // 16f7: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3224 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V
      // 16fa: goto 2040
      // 16fd: aload 8
      // 16Category: aload 1
      // 1700: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 1703: i2f
      // 1704: aload 1
      // 1705: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1708: i2f
      // 1709: fload 2
      // 170a: fsub
      // 170b: fconst_1
      // 170c: fadd
      // 170d: fsub
      // 170e: fstore 39
      // 1710: fload 39
      // 1712: ldc_w 5.0
      // 1715: fdiv
      // 1716: fstore 40
      // 1718: fload 40
      // 171a: fconst_1
      // 171b: fcmpl
      // 171c: ifle 1722
      // 171f: fconst_1
      // 1720: fstore 40
      // 1722: fload 39
      // 1724: fconst_2
      // 1725: fdiv
      // 1726: ldc_w 3.14
      // 1729: fmul
      // 172a: f2d
      // 172b: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 172e: fstore 41
      // 1730: fload 41
      // 1732: ldc_w 10.0
      // 1735: fdiv
      // 1736: fstore 41
      // 1738: aload 10
      // 173a: bipush 1
      // 173b: iload 38
      // 173d: imul
      // 173e: i2d
      // 173f: ldc2_w 0.1
      // 1742: ldc2_w 0.3
      // 1745: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1748: aload 10
      // 174a: ldc2_w 0.2
      // 174d: iload 38
      // 174f: i2d
      // 1750: dmul
      // 1751: fload 40
      // 1753: f2d
      // 1754: dmul
      // 1755: ldc2_w -0.7
      // 1758: fload 40
      // 175a: f2d
      // 175b: dmul
      // 175c: ldc2_w -0.2
      // 175f: fload 40
      // 1761: f2d
      // 1762: dmul
      // 1763: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1766: aload 10
      // 1768: dconst_0
      // 1769: ldc2_w -0.2
      // 176c: fload 41
      // 176e: f2d
      // 176f: dmul
      // 1770: ldc2_w -0.2
      // 1773: fload 41
      // 1775: f2d
      // 1776: dmul
      // 1777: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 177a: aload 10
      // 177c: dconst_0
      // 177d: ldc2_w 0.1
      // 1780: aload 0
      // 1781: fload 40
      // 1783: ldc_w 3.14
      // 1786: fmul
      // 1787: f2d
      // 1788: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 178b: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiEaseInOutBack (F)F
      // 178e: f2d
      // 178f: dmul
      // 1790: dconst_0
      // 1791: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1794: aload 10
      // 1796: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1799: bipush 45
      // 179b: iload 38
      // 179d: imul
      // 179e: i2f
      // 179f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 17a4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 17a7: aload 10
      // 17a9: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 17EventMotion: bipush -40
      // 17ae: iload 38
      // 17b0: imul
      // 17b1: i2f
      // 17b2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 17b7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 17ba: aload 10
      // 17bc: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 17bf: ldc_w 30.0
      // 17c2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 17c7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 17ConfigFile: aload 0
      // 17cb: aload 10
      // 17cd: aload 5
      // 17cf: fload 7
      // 17d1: aload 8
      // 17d3: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 17d6: fload 9
      // 17d8: ldc_w 3.14
      // 17db: fmul
      // 17EventKey: f2d
      // 17dd: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 17e0: fstore 42
      // 17e2: aload 10
      // 17e4: ldc_w 0.9
      // 17e7: ldc_w 0.9
      // 17ea: ldc_w 0.9
      // 17ed: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 17f0: aload 10
      // 17f2: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 17f5: ldc_w 45.0
      // 17f8: fload 40
      // 17fa: fmul
      // 17fb: iload 38
      // 17fd: i2f
      // 17fe: fmul
      // 17Category: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1804: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1807: aload 0
      // 1808: aload 10
      // 180a: aload 11
      // 180c: iload 12
      // 180e: fconst_0
      // 180f: fload 7
      // 1811: aload 5
      // 1813: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1816: goto 2040
      // 1819: aload 8
      // 181b: aload 1
      // 181c: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 181f: i2f
      // 1820: aload 1
      // 1821: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1824: i2f
      // 1825: fload 2
      // 1826: fsub
      // 1827: fconst_1
      // 1828: fadd
      // 1829: fsub
      // 182a: fstore 39
      // 182c: fload 39
      // 182e: ldc_w 4.0
      // 1831: fdiv
      // 1832: fstore 40
      // 1834: fload 39
      // 1836: ldc_w 6.0
      // 1839: fdiv
      // 183a: fstore 41
      // 183c: fload 40
      // 183e: fconst_1
      // 183f: fcmpl
      // 1840: ifle 1846
      // 1843: fconst_1
      // 1844: fstore 40
      // 1846: fload 41
      // 1848: fconst_1
      // 1849: fcmpl
      // 184a: ifle 1850
      // 184d: fconst_1
      // 184e: fstore 41
      // 1850: aload 10
      // 1852: dconst_0
      // 1853: ldc2_w -0.2
      // 1856: dconst_0
      // 1857: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 185a: aload 10
      // 185c: bipush 1
      // 185d: iload 38
      // 185f: imul
      // 1860: i2d
      // 1861: dconst_0
      // 1862: ldc2_w 0.3
      // 1865: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1868: aload 10
      // 186a: ldc2_w 0.7
      // 186d: fload 40
      // 186f: f2d
      // 1870: dmul
      // 1871: iload 38
      // 1873: i2d
      // 1874: dmul
      // 1875: dconst_0
      // 1876: ldc2_w -1.3
      // 1879: fload 40
      // 187b: f2d
      // 187c: dmul
      // 187d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1880: aload 10
      // 1882: ldc2_w -0.2
      // 1885: iload 38
      // 1887: i2d
      // 1888: dmul
      // 1889: fload 41
      // 188b: f2d
      // 188c: dmul
      // 188d: dconst_0
      // 188e: dconst_0
      // 188f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1892: aload 10
      // 1894: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1897: ldc2_w 10.0
      // 189a: fload 41
      // 189c: f2d
      // 189d: ldc2_w 3.14
      // 18a0: dmul
      // 18a1: invokestatic java/lang/Math.sin (D)D
      // 18a4: dmul
      // 18a5: d2f
      // 18a6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 18ab: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 18ae: aload 10
      // 18b0: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 18b3: ldc_w 70.0
      // 18b6: fload 40
      // 18b8: fmul
      // 18b9: iload 38
      // 18DraggableWidget: i2f
      // 18bc: fmul
      // 18bd: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 18c2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 18c5: aload 10
      // 18c7: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 18ConfigFile: bipush 45
      // 18EventPacket: iload 38
      // 18ce: imul
      // 18cf: i2f
      // 18d0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 18d5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 18d8: aload 10
      // 18ConfigManager: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 18dd: bipush -40
      // 18df: iload 38
      // 18e1: imul
      // 18e2: i2f
      // 18e3: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 18e8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 18eb: aload 10
      // 18ed: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 18f0: ldc_w 30.0
      // 18f3: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 18f8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 18fb: aload 10
      // 18fd: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1900: bipush 5
      // 1901: iload 38
      // 1903: imul
      // 1904: i2f
      // 1905: fload 40
      // 1907: fmul
      // 1908: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 190d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1910: aload 10
      // 1912: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1915: ldc_w -10.0
      // 1918: fload 40
      // 191a: fmul
      // 191b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1920: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1923: aload 10
      // 1925: dconst_0
      // 1926: dconst_0
      // 1927: ldc2_w -0.2
      // 192a: fload 40
      // 192c: f2d
      // 192d: dmul
      // 192e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1931: aload 0
      // 1932: aload 10
      // 1934: aload 5
      // 1936: fload 7
      // 1938: aload 8
      // 193a: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 193d: aload 10
      // 193f: ldc_w 0.9
      // 1942: ldc_w 0.9
      // 1945: ldc_w 0.9
      // 1948: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 194b: aload 0
      // 194c: aload 10
      // 194e: aload 11
      // 1950: iload 12
      // 1952: fconst_0
      // 1953: fload 7
      // 1955: aload 5
      // 1957: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 195a: aload 10
      // 195c: ldc2_w 0.35
      // 195f: iload 38
      // 1961: i2d
      // 1962: dmul
      // 1963: ldc2_w -0.13
      // 1966: ldc2_w -0.12
      // 1969: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 196c: aload 10
      // 196e: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1971: ldc_w 10.0
      // 1974: iload 38
      // 1976: i2f
      // 1977: fmul
      // 1978: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 197d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1980: aload 10
      // 1982: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1985: ldc_w 10.0
      // 1988: iload 38
      // 198a: i2f
      // 198b: fmul
      // 198c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1991: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1994: aload 10
      // 1996: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1999: fconst_0
      // 199a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 199f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 19a2: aload 10
      // 19a4: ldc2_w -0.2
      // 19a7: iload 38
      // 19a9: i2d
      // 19aa: dmul
      // 19ab: ldc2_w -0.04
      // 19ae: ldc2_w 0.15
      // 19b1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 19b4: aload 10
      // 19b6: fconst_1
      // 19b7: fconst_1
      // 19b8: fconst_1
      // 19b9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 19bc: goto 2040
      // 19bf: aload 10
      // 19c1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 19c4: aload 1
      // 19c5: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6068 ()Lnet/minecraft/util/Arm;
      // 19c8: getstatic net/minecraft/util/Arm.field_6182 Lnet/minecraft/util/Arm;
      // 19cb: if_acmpne 19ConfigManager
      // 19ce: iload 29
      // 19d0: ifne 19d7
      // 19d3: bipush 1
      // 19d4: goto 19d8
      // 19d7: bipush 0
      // 19d8: istore 29
      // 19ConfigManager: aload 8
      // 19EventKey: aload 1
      // 19dd: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 19e0: i2f
      // 19e1: aload 1
      // 19e2: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 19e5: i2f
      // 19e6: fload 2
      // 19e7: fsub
      // 19e8: fconst_1
      // 19e9: fadd
      // 19ea: fsub
      // 19eb: fstore 39
      // 19ed: fload 39
      // 19ef: ldc_w 20.0
      // 19f2: fdiv
      // 19f3: fstore 40
      // 19f5: fload 40
      // 19f7: fload 40
      // 19f9: fmul
      // 19fa: fload 40
      // 19EventMove: fconst_2
      // 19fd: fmul
      // 19fe: fadd
      // 19Category: ldc_w 3.0
      // 1a02: fdiv
      // 1a03: fstore 41
      // 1a05: fload 40
      // 1a07: fconst_1
      // 1a08: fcmpl
      // 1a09: ifle 1a0f
      // 1a0c: fconst_1
      // 1a0d: fstore 40
      // 1a0f: fload 40
      // 1a11: ldc_w 0.1
      // 1a14: fcmpl
      // 1a15: ifle 1a42
      // 1a18: fload 39
      // 1a1a: ldc_w 0.1
      // 1a1d: fsub
      // 1a1e: ldc_w 1.3
      // 1a21: fmul
      // 1a22: f2d
      // 1a23: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 1a26: fstore 42
      // 1a28: fload 42
      // 1a2a: fload 40
      // 1a2c: fmul
      // 1a2d: fstore 43
      // 1a2f: aload 10
      // 1a31: fload 43
      // 1a33: fconst_0
      // 1a34: fmul
      // 1a35: fload 43
      // 1a37: ldc_w 0.004
      // 1a3a: fmul
      // 1a3b: fload 43
      // 1a3d: fconst_0
      // 1a3e: fmul
      // 1a3f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1a42: aload 10
      // 1a44: iload 29
      // 1a46: ifeq 1a4f
      // 1a49: ldc2_w -0.1
      // 1a4c: goto 1a52
      // 1a4f: ldc2_w 0.1
      // 1a52: dconst_0
      // 1a53: fload 40
      // 1a55: f2d
      // 1a56: ldc2_w 0.15
      // 1a59: dmul
      // 1a5a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1a5d: aload 0
      // 1a5e: aload 10
      // 1a60: aload 11
      // 1a62: iload 12
      // 1a64: fload 9
      // 1a66: fload 7
      // 1a68: aload 5
      // 1a6a: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1a6d: aload 10
      // 1a6f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1a72: aload 10
      // 1a74: iload 29
      // 1a76: ifeq 1a7f
      // 1a79: ldc2_w -0.5
      // 1a7c: goto 1a82
      // 1a7f: ldc2_w 0.5
      // 1a82: ldc2_w -0.45
      // 1a85: ldc2_w 0.1
      // 1a88: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1a8b: aload 10
      // 1a8d: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1a90: ldc_w 0.3
      // 1a93: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1a98: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1a9b: iload 29
      // 1a9d: ifeq 1ac1
      // 1aa0: aload 10
      // 1aa2: getstatic net/minecraft/util/math/RotationAxis.field_40717 Lnet/minecraft/util/math/RotationAxis;
      // 1aa5: ldc_w -0.3
      // 1aa8: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1aad: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1ab0: aload 10
      // 1ab2: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 1ab5: fconst_1
      // 1ab6: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1abb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1abe: goto 1adf
      // 1ac1: aload 10
      // 1ac3: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1ac6: ldc_w -0.3
      // 1ac9: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1ace: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1ad1: aload 10
      // 1ad3: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1ad6: fconst_1
      // 1ad7: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1adc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1adf: aload 0
      // 1ae0: aload 10
      // 1ae2: aload 11
      // 1ae4: iload 12
      // 1ae6: fload 9
      // 1ae8: fload 7
      // 1aea: aload 5
      // 1aec: invokevirtual net/minecraft/util/Arm.method_5928 ()Lnet/minecraft/util/Arm;
      // 1aef: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1af2: iload 29
      // 1af4: ifeq 1b0a
      // 1af7: aload 10
      // 1af9: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 1afc: ldc_w 2.5
      // 1aff: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1b04: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1b07: goto 1b1a
      // 1b0a: aload 10
      // 1b0c: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1b0f: ldc_w 2.5
      // 1b12: invokeinterface net/minecraft/util/math/RotationAxis.rotation (F)Lorg/joml/Quaternionf; 2
      // 1b17: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1b1a: aload 10
      // 1b1c: iload 29
      // 1b1e: ifeq 1b27
      // 1b21: ldc2_w -0.65
      // 1b24: goto 1b2a
      // 1b27: ldc2_w 0.65
      // 1b2a: ldc2_w -0.35
      // 1b2d: ldc2_w 0.27
      // 1b30: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1b33: fload 40
      // 1b35: fconst_1
      // 1b36: fcmpl
      // 1b37: ifle 1b3d
      // 1b3a: fconst_1
      // 1b3b: fstore 40
      // 1b3d: aload 10
      // 1b3f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1b42: aload 15
      // 1b44: getfield ru/white/module/impl/render/qj.mb3DCompat Lq2/z/BooleanSetting;
      // 1b47: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 1b4a: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 1b4d: ifeq 1b63
      // 1b50: aload 10
      // 1b52: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1b55: bipush 10
      // 1b57: iload 38
      // 1b59: imul
      // 1b5a: i2f
      // 1b5b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1b60: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1b63: aload 10
      // 1b65: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 1b68: ldc_w 75.0
      // 1b6b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1b70: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1b73: aload 10
      // 1b75: getstatic net/minecraft/util/math/RotationAxis.field_40717 Lnet/minecraft/util/math/RotationAxis;
      // 1b78: bipush -15
      // 1b7a: iload 38
      // 1b7c: imul
      // 1b7d: i2f
      // 1b7e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1b83: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1b86: aload 10
      // 1b88: ldc2_w 0.8
      // 1b8b: iload 38
      // 1b8d: i2d
      // 1b8e: dmul
      // 1b8f: fconst_0
      // 1b90: fload 9
      // 1b92: ldc_w 0.3
      // 1b95: fmul
      // 1b96: fsub
      // 1b97: f2d
      // 1b98: ldc2_w -0.1
      // 1b9b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1b9e: fload 41
      // 1ba0: ldc_w 0.1
      // 1ba3: fcmpl
      // 1ba4: ifle 1bd9
      // 1ba7: fload 39
      // 1ba9: ldc_w 0.1
      // 1bac: fsub
      // 1bad: ldc_w 1.3
      // 1bb0: fmul
      // 1bb1: f2d
      // 1bb2: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 1bb5: fstore 42
      // 1bb7: fload 40
      // 1bb9: ldc_w 0.1
      // 1bbc: fsub
      // 1bbd: fstore 43
      // 1bbf: fload 42
      // 1bc1: fload 43
      // 1bc3: fmul
      // 1bc4: fstore 44
      // 1bc6: aload 10
      // 1bc8: fload 44
      // 1bca: fconst_0
      // 1bcb: fmul
      // 1bcc: fload 44
      // 1bce: ldc_w 0.004
      // 1bd1: fmul
      // 1bd2: fload 44
      // 1bd4: fconst_0
      // 1bd5: fmul
      // 1bd6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1bd9: aload 10
      // 1bdb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1bde: goto 2040
      // 1be1: aload 1
      // 1be2: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6079 ()Lnet/minecraft/item/ItemStack;
      // 1be5: invokevirtual net/minecraft/item/ItemStack.method_7960 ()Z
      // 1be8: ifeq 1c42
      // 1beb: aload 1
      // 1bec: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 1bef: ifne 1c42
      // 1bf2: aload 1
      // 1bf3: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 1bf6: ifne 1c42
      // 1bf9: aload 1
      // 1bfa: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 1bfd: ifne 1c42
      // 1c00: aload 10
      // 1c02: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1c05: aload 10
      // 1c07: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1c0a: bipush -25
      // 1c0c: iload 38
      // 1c0e: imul
      // 1c0f: i2f
      // 1c10: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1c15: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1c18: aload 10
      // 1c1a: ldc2_w -0.15
      // 1c1d: iload 38
      // 1c1f: i2d
      // 1c20: dmul
      // 1c21: ldc2_w 0.1
      // 1c24: ldc2_w 0.1
      // 1c27: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1c2a: aload 0
      // 1c2b: aload 10
      // 1c2d: aload 11
      // 1c2f: iload 12
      // 1c31: fload 9
      // 1c33: fload 7
      // 1c35: aload 5
      // 1c37: invokevirtual net/minecraft/util/Arm.method_5928 ()Lnet/minecraft/util/Arm;
      // 1c3a: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1c3d: aload 10
      // 1c3f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1c42: aload 8
      // 1c44: aload 1
      // 1c45: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 1c48: i2f
      // 1c49: aload 1
      // 1c4a: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1c4d: i2f
      // 1c4e: fload 2
      // 1c4f: fsub
      // 1c50: fconst_1
      // 1c51: fadd
      // 1c52: fsub
      // 1c53: fstore 39
      // 1c55: fload 39
      // 1c57: ldc_w 10.0
      // 1c5a: fdiv
      // 1c5b: fstore 40
      // 1c5d: fload 40
      // 1c5f: fconst_1
      // 1c60: fcmpl
      // 1c61: ifle 1c67
      // 1c64: fconst_1
      // 1c65: fstore 40
      // 1c67: fload 40
      // 1c69: ldc_w 0.1
      // 1c6c: fcmpl
      // 1c6d: ifle 1ca2
      // 1c70: fload 39
      // 1c72: ldc_w 0.1
      // 1c75: fsub
      // 1c76: ldc_w 1.3
      // 1c79: fmul
      // 1c7a: f2d
      // 1c7b: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 1c7e: fstore 41
      // 1c80: fload 40
      // 1c82: ldc_w 0.1
      // 1c85: fsub
      // 1c86: fstore 42
      // 1c88: fload 41
      // 1c8a: fload 42
      // 1c8c: fmul
      // 1c8d: fstore 43
      // 1c8f: aload 10
      // 1c91: fload 43
      // 1c93: fconst_0
      // 1c94: fmul
      // 1c95: fload 43
      // 1c97: ldc_w 0.004
      // 1c9a: fmul
      // 1c9b: fload 43
      // 1c9d: fconst_0
      // 1c9e: fmul
      // 1c9f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1ca2: aload 10
      // 1ca4: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1ca7: ldc_w 45.0
      // 1caa: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1caf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1cb2: aload 10
      // 1cb4: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1cb7: bipush 25
      // 1cb9: iload 38
      // 1cbb: imul
      // 1cbc: i2f
      // 1cbd: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1cc2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1cc5: aload 10
      // 1cc7: ldc2_w 0.2
      // 1cca: iload 38
      // 1ccc: i2d
      // 1ccd: dmul
      // 1cce: dconst_0
      // 1ccf: ldc2_w 0.8
      // 1cd2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1cd5: aload 0
      // 1cd6: aload 10
      // 1cd8: aload 11
      // 1cda: iload 12
      // 1cdc: fload 9
      // 1cde: fload 7
      // 1ce0: aload 5
      // 1ce2: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1ce5: aload 10
      // 1ce7: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1cea: ldc_w 135.0
      // 1ced: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1cf2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1cf5: aload 10
      // 1cf7: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1cfa: bipush -65
      // 1cfc: iload 38
      // 1cfe: imul
      // 1cff: i2f
      // 1d00: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1d05: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1d08: aload 10
      // 1d0a: ldc_w 0.65
      // 1d0d: iload 38
      // 1d0f: i2f
      // 1d10: fmul
      // 1d11: f2d
      // 1d12: ldc2_w -1.0
      // 1d15: ldc2_w -0.6
      // 1d18: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1d1b: goto 2040
      // 1d1e: aload 1
      // 1d1f: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1d22: bipush 10
      // 1d24: irem
      // 1d25: i2f
      // 1d26: fstore 39
      // 1d28: fload 39
      // 1d2a: fload 2
      // 1d2b: fsub
      // 1d2c: fconst_1
      // 1d2d: fadd
      // 1d2e: fstore 40
      // 1d30: fconst_1
      // 1d31: fload 40
      // 1d33: ldc_w 10.0
      // 1d36: fdiv
      // 1d37: fsub
      // 1d38: fstore 41
      // 1d3a: ldc_w -15.0
      // 1d3d: ldc_w 75.0
      // 1d40: fload 41
      // 1d42: fconst_2
      // 1d43: fmul
      // 1d44: ldc_w 3.1415927
      // 1d47: fmul
      // 1d48: f2d
      // 1d49: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 1d4c: fmul
      // 1d4d: fadd
      // 1d4e: fstore 42
      // 1d50: aload 8
      // 1d52: aload 1
      // 1d53: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 1d56: i2f
      // 1d57: aload 1
      // 1d58: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1d5b: i2f
      // 1d5c: fload 2
      // 1d5d: fsub
      // 1d5e: fconst_1
      // 1d5f: fadd
      // 1d60: fsub
      // 1d61: fstore 43
      // 1d63: fload 43
      // 1d65: ldc_w 4.0
      // 1d68: fdiv
      // 1d69: fstore 44
      // 1d6b: fload 44
      // 1d6d: fconst_1
      // 1d6e: fcmpl
      // 1d6f: ifle 1d75
      // 1d72: fconst_1
      // 1d73: fstore 44
      // 1d75: aload 10
      // 1d77: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1d7a: bipush 25
      // 1d7c: iload 38
      // 1d7e: imul
      // 1d7f: i2f
      // 1d80: fload 44
      // 1d82: fmul
      // 1d83: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1d88: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1d8b: aload 10
      // 1d8d: ldc_w 0.3
      // 1d90: iload 38
      // 1d92: i2f
      // 1d93: fmul
      // 1d94: fload 44
      // 1d96: fmul
      // 1d97: f2d
      // 1d98: ldc2_w 0.3
      // 1d9b: fload 44
      // 1d9d: f2d
      // 1d9e: dmul
      // 1d9f: ldc2_w 0.1
      // 1da2: fload 44
      // 1da4: f2d
      // 1da5: dmul
      // 1da6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1da9: fload 44
      // 1dab: fconst_1
      // 1dac: fcmpl
      // 1dad: ifne 1dc3
      // 1db0: aload 10
      // 1db2: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1db5: fload 42
      // 1db7: ldc_w 20.0
      // 1dba: fdiv
      // 1dbb: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1dc0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1dc3: aload 0
      // 1dc4: aload 10
      // 1dc6: aload 11
      // 1dc8: iload 12
      // 1dca: fload 9
      // 1dcc: fload 7
      // 1dce: aload 5
      // 1dd0: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1dd3: goto 2040
      // 1dd6: aload 10
      // 1dd8: bipush 1
      // 1dd9: iload 38
      // 1ddb: imul
      // 1ddc: i2d
      // 1ddd: dconst_0
      // 1dde: fload 9
      // 1de0: f2d
      // 1de1: ldc2_w 0.3
      // 1de4: dmul
      // 1de5: dsub
      // 1de6: ldc2_w 0.3
      // 1de9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1dec: aload 10
      // 1dee: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1df1: bipush 45
      // 1df3: iload 38
      // 1df5: imul
      // 1df6: i2f
      // 1df7: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1dfc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1dff: aload 10
      // 1e01: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1e04: bipush -40
      // 1e06: iload 38
      // 1e08: imul
      // 1e09: i2f
      // 1e0a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1e0f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1e12: aload 10
      // 1e14: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1e17: ldc_w 30.0
      // 1e1a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1e1f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1e22: aload 0
      // 1e23: aload 10
      // 1e25: aload 5
      // 1e27: fload 7
      // 1e29: aload 8
      // 1e2b: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 1e2e: aload 10
      // 1e30: ldc_w 0.9
      // 1e33: ldc_w 0.9
      // 1e36: ldc_w 0.9
      // 1e39: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 1e3c: aload 0
      // 1e3d: aload 10
      // 1e3f: aload 11
      // 1e41: iload 12
      // 1e43: fconst_0
      // 1e44: fconst_0
      // 1e45: aload 5
      // 1e47: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1e4a: goto 2040
      // 1e4d: goto 2040
      // 1e50: aload 1
      // 1e51: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6123 ()Z
      // 1e54: ifeq 1f6e
      // 1e57: aload 8
      // 1e59: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 1e5c: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 1e5f: if_acmpne 1f6e
      // 1e62: aload 0
      // 1e63: aload 0
      // 1e64: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$riptideCounter F
      // 1e67: f2d
      // 1e68: ldc2_w 0.15
      // 1e6b: dload 30
      // 1e6d: dmul
      // 1e6e: dadd
      // 1e6f: d2f
      // 1e70: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$riptideCounter F
      // 1e73: aload 8
      // 1e75: aload 1
      // 1e76: invokevirtual net/minecraft/item/ItemStack.method_7935 (Lnet/minecraft/entity/LivingEntity;)I
      // 1e79: i2f
      // 1e7a: aload 1
      // 1e7b: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6014 ()I
      // 1e7e: i2f
      // 1e7f: fload 2
      // 1e80: fsub
      // 1e81: fconst_1
      // 1e82: fadd
      // 1e83: fsub
      // 1e84: fstore 39
      // 1e86: fload 39
      // 1e88: ldc_w 10.0
      // 1e8b: fdiv
      // 1e8c: fstore 40
      // 1e8e: fload 40
      // 1e90: fconst_1
      // 1e91: fcmpl
      // 1e92: ifle 1e98
      // 1e95: fconst_1
      // 1e96: fstore 40
      // 1e98: fload 40
      // 1e9a: ldc_w 0.1
      // 1e9d: fcmpl
      // 1e9e: ifle 1ed3
      // 1ea1: fload 39
      // 1ea3: ldc_w 0.1
      // 1ea6: fsub
      // 1ea7: ldc_w 1.3
      // 1eaa: fmul
      // 1eab: f2d
      // 1eac: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 1eaf: fstore 41
      // 1eb1: fload 40
      // 1eb3: ldc_w 0.1
      // 1eb6: fsub
      // 1eb7: fstore 42
      // 1eb9: fload 41
      // 1ebb: fload 42
      // 1ebd: fmul
      // 1ebe: fstore 43
      // 1ec0: aload 10
      // 1ec2: fload 43
      // 1ec4: fconst_0
      // 1ec5: fmul
      // 1ec6: fload 43
      // 1ec8: ldc_w 0.004
      // 1ecb: fmul
      // 1ecc: fload 43
      // 1ece: fconst_0
      // 1ecf: fmul
      // 1ed0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 1ed3: aload 10
      // 1ed5: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1ed8: ldc_w 45.0
      // 1edb: aload 0
      // 1edc: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$riptideCounter F
      // 1edf: fconst_2
      // 1ee0: fmul
      // 1ee1: fsub
      // 1ee2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1ee7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1eea: aload 10
      // 1eec: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1eef: bipush 25
      // 1ef1: iload 38
      // 1ef3: imul
      // 1ef4: i2f
      // 1ef5: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1efa: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1efd: aload 10
      // 1eff: ldc2_w 0.2
      // 1f02: iload 38
      // 1f04: i2d
      // 1f05: dmul
      // 1f06: dconst_0
      // 1f07: ldc2_w 0.75
      // 1f0a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1f0d: aload 10
      // 1f0f: dconst_0
      // 1f10: dconst_0
      // 1f11: ldc2_w 0.01
      // 1f14: aload 0
      // 1f15: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$riptideCounter F
      // 1f18: ldc_w 6.28
      // 1f1b: fmul
      // 1f1c: f2d
      // 1f1d: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 1f20: f2d
      // 1f21: dmul
      // 1f22: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1f25: aload 0
      // 1f26: aload 10
      // 1f28: aload 11
      // 1f2a: iload 12
      // 1f2c: fload 9
      // 1f2e: fload 7
      // 1f30: aload 5
      // 1f32: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 1f35: aload 10
      // 1f37: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1f3a: ldc_w 135.0
      // 1f3d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1f42: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1f45: aload 10
      // 1f47: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1f4a: bipush -65
      // 1f4c: iload 38
      // 1f4e: imul
      // 1f4f: i2f
      // 1f50: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1f55: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1f58: aload 10
      // 1f5a: ldc_w 0.65
      // 1f5d: iload 38
      // 1f5f: i2f
      // 1f60: fmul
      // 1f61: f2d
      // 1f62: ldc2_w -1.0
      // 1f65: ldc2_w -0.6
      // 1f68: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1f6b: goto 2040
      // 1f6e: aload 0
      // 1f6f: fconst_0
      // 1f70: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$riptideCounter F
      // 1f73: aload 8
      // 1f75: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 1f78: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 1f7b: ifne 1fac
      // 1f7e: aload 8
      // 1f80: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 1f83: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 1f86: ifne 1fac
      // 1f89: aload 8
      // 1f8b: getstatic net/minecraft/registry/tag/ItemTags.field_40108 Lnet/minecraft/registry/tag/TagKey;
      // 1f8e: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 1f91: ifne 1fac
      // 1f94: aload 8
      // 1f96: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 1f99: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 1f9c: if_acmpne 1fcc
      // 1f9f: aload 10
      // 1fa1: dconst_0
      // 1fa2: ldc2_w -0.2
      // 1fa5: dconst_0
      // 1fa6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1fa9: goto 1fcc
      // 1fac: aload 10
      // 1fae: ldc2_w 0.1
      // 1fb1: iload 38
      // 1fb3: i2d
      // 1fb4: dmul
      // 1fb5: dconst_0
      // 1fb6: ldc2_w -0.1
      // 1fb9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1fbc: aload 10
      // 1fbe: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1fc1: ldc_w 10.0
      // 1fc4: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1fc9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1fcc: aload 10
      // 1fce: bipush 1
      // 1fcf: iload 38
      // 1fd1: imul
      // 1fd2: i2d
      // 1fd3: dconst_0
      // 1fd4: fload 9
      // 1fd6: f2d
      // 1fd7: ldc2_w 0.3
      // 1fda: dmul
      // 1fdb: dsub
      // 1fdc: ldc2_w 0.3
      // 1fdf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1fe2: aload 10
      // 1fe4: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1fe7: bipush 45
      // 1fe9: iload 38
      // 1feb: imul
      // 1fec: i2f
      // 1fed: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1ff2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1ff5: aload 10
      // 1ff7: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1ffa: bipush -40
      // 1ffc: iload 38
      // 1ffe: imul
      // 1fff: i2f
      // 2000: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2005: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2008: aload 10
      // 200a: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 200d: ldc_w 30.0
      // 2010: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2015: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2018: aload 0
      // 2019: aload 10
      // 201b: aload 5
      // 201d: fload 7
      // 201f: aload 8
      // 2021: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiAltSwing (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;FLnet/minecraft/item/ItemStack;)V
      // 2024: aload 10
      // 2026: ldc_w 0.9
      // 2029: ldc_w 0.9
      // 202c: ldc_w 0.9
      // 202f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2032: aload 0
      // 2033: aload 10
      // 2035: aload 11
      // 2037: iload 12
      // 2039: fconst_0
      // 203a: fconst_0
      // 203b: aload 5
      // 203d: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3219 (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V
      // 2040: aload 10
      // 2042: ldc2_w -0.3
      // 2045: iload 38
      // 2047: i2d
      // 2048: dmul
      // 2049: ldc2_w 0.65
      // 204c: ldc2_w -0.1
      // 204f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2052: aload 10
      // 2054: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2057: bipush -65
      // 2059: iload 38
      // 205b: imul
      // 205c: i2f
      // 205d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2062: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2065: aload 10
      // 2067: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 206a: ldc_w 10.0
      // 206d: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2072: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2075: aload 8
      // 2077: getstatic net/minecraft/registry/tag/ItemTags.field_15542 Lnet/minecraft/registry/tag/TagKey;
      // 207a: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 207d: ifeq 2090
      // 2080: aload 10
      // 2082: ldc2_w 0.2
      // 2085: iload 38
      // 2087: i2d
      // 2088: dmul
      // 2089: ldc2_w -0.1
      // 208c: dconst_0
      // 208d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2090: aload 8
      // 2092: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2095: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2098: getstatic net/minecraft/block/Blocks.field_10124 Lnet/minecraft/block/Block;
      // 209b: if_acmpeq 24d2
      // 209e: aload 8
      // 20a0: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 20a3: getstatic net/minecraft/item/consume/UseAction.field_8950 Lnet/minecraft/item/consume/UseAction;
      // 20a6: if_acmpeq 24d2
      // 20a9: aload 8
      // 20ab: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.BUCKETS Lnet/minecraft/registry/tag/TagKey;
      // 20ae: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 20b1: ifne 24d2
      // 20b4: aload 0
      // 20b5: aload 8
      // 20b7: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiIsSkull (Lnet/minecraft/item/ItemStack;)Z
      // 20ba: ifne 24d2
      // 20bd: aload 8
      // 20bf: invokevirtual net/minecraft/item/ItemStack.method_7964 ()Lnet/minecraft/text/Text;
      // 20c2: invokeinterface net/minecraft/text/Text.toString ()Ljava/lang/String; 1
      // 20c7: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 20ConfigFile: ldc_w "TORCH"
      // 20cd: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 20d0: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 20d3: ifeq 212f
      // 20d6: aload 10
      // 20d8: ldc_w 1.5
      // 20db: ldc_w 1.5
      // 20de: ldc_w 1.5
      // 20e1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 20e4: aload 10
      // 20e6: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 20e9: bipush 25
      // 20eb: iload 38
      // 20ed: imul
      // 20ee: i2f
      // 20ef: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 20f4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 20f7: aload 10
      // 20f9: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 20EventMove: ldc_w 5.0
      // 20Category: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2104: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2107: aload 10
      // 2109: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 210c: bipush 75
      // 210e: iload 38
      // 2110: imul
      // 2111: i2f
      // 2112: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2117: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 211a: aload 10
      // 211c: ldc2_w 0.2
      // 211f: iload 38
      // 2121: i2d
      // 2122: dmul
      // 2123: ldc2_w 0.2
      // 2126: ldc2_w 0.05
      // 2129: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 212c: goto 28ea
      // 212f: aload 8
      // 2131: getstatic net/minecraft/item/Items.field_8276 Lnet/minecraft/item/Item;
      // 2134: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2137: ifne 21a2
      // 213a: aload 8
      // 213c: getstatic net/minecraft/item/Items.field_8725 Lnet/minecraft/item/Item;
      // 213f: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2142: ifne 21a2
      // 2145: aload 8
      // 2147: getstatic net/minecraft/item/Items.field_8865 Lnet/minecraft/item/Item;
      // 214a: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 214d: ifne 21a2
      // 2150: aload 8
      // 2152: getstatic net/minecraft/item/Items.field_8366 Lnet/minecraft/item/Item;
      // 2155: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2158: ifne 21a2
      // 215b: aload 8
      // 215d: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2160: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2163: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2166: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalBlockTags.GLASS_PANES Lnet/minecraft/registry/tag/TagKey;
      // 2169: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 216c: ifne 21a2
      // 216f: aload 8
      // 2171: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2174: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2177: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 217a: getstatic net/minecraft/registry/tag/BlockTags.field_15463 Lnet/minecraft/registry/tag/TagKey;
      // 217d: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2180: ifne 21a2
      // 2183: aload 8
      // 2185: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2188: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 218b: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 218e: getstatic net/minecraft/registry/tag/BlockTags.field_22414 Lnet/minecraft/registry/tag/TagKey;
      // 2191: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2194: ifne 21a2
      // 2197: aload 8
      // 2199: getstatic net/minecraft/registry/tag/ItemTags.field_15553 Lnet/minecraft/registry/tag/TagKey;
      // 219c: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 219f: ifeq 2220
      // 21a2: aload 8
      // 21a4: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 21a7: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 21aa: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 21ad: getstatic net/minecraft/registry/tag/BlockTags.field_15503 Lnet/minecraft/registry/tag/TagKey;
      // 21b0: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 21b3: ifne 2220
      // 21b6: aload 8
      // 21b8: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 21DraggableWidget: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 21be: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 21c1: getstatic net/minecraft/registry/tag/BlockTags.field_43170 Lnet/minecraft/registry/tag/TagKey;
      // 21c4: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 21c7: ifne 2220
      // 21ConfigFile: aload 8
      // 21EventPacket: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 21cf: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 21d2: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 21d5: getstatic net/minecraft/registry/tag/BlockTags.field_15501 Lnet/minecraft/registry/tag/TagKey;
      // 21d8: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 21db: ifne 2220
      // 21de: aload 10
      // 21e0: dconst_0
      // 21e1: dconst_0
      // 21e2: ldc2_w -0.1
      // 21e5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 21e8: aload 10
      // 21ea: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 21ed: bipush 5
      // 21ee: iload 38
      // 21f0: imul
      // 21f1: i2f
      // 21f2: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 21f7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 21fa: aload 10
      // 21EventMove: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 21Category: ldc_w 15.0
      // 2202: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2207: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 220a: aload 10
      // 220c: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 220f: bipush 75
      // 2211: iload 38
      // 2213: imul
      // 2214: i2f
      // 2215: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 221a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 221d: goto 28ea
      // 2220: aload 8
      // 2222: getstatic net/minecraft/item/Items.field_16539 Lnet/minecraft/item/Item;
      // 2225: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2228: ifne 22bc
      // 222b: aload 8
      // 222d: getstatic net/minecraft/item/Items.field_22016 Lnet/minecraft/item/Item;
      // 2230: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2233: ifne 22bc
      // 2236: aload 8
      // 2238: getstatic net/minecraft/registry/tag/ItemTags.field_40108 Lnet/minecraft/registry/tag/TagKey;
      // 223b: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 223e: ifne 22bc
      // 2241: aload 10
      // 2243: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 2246: bipush 25
      // 2248: iload 38
      // 224a: imul
      // 224b: i2f
      // 224c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2251: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2254: aload 10
      // 2256: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2259: ldc_w 5.0
      // 225c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2261: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2264: aload 10
      // 2266: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2269: bipush 75
      // 226b: iload 38
      // 226d: imul
      // 226e: i2f
      // 226f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2274: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2277: aload 10
      // 2279: ldc2_w 0.2
      // 227c: iload 38
      // 227e: i2d
      // 227f: dmul
      // 2280: ldc2_w 0.2
      // 2283: ldc2_w 0.05
      // 2286: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2289: aload 8
      // 228b: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 228e: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2291: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2294: getstatic net/minecraft/registry/tag/BlockTags.field_15501 Lnet/minecraft/registry/tag/TagKey;
      // 2297: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 229a: ifeq 28ea
      // 229d: aload 10
      // 229f: ldc2_w -0.2
      // 22a2: iload 38
      // 22a4: i2d
      // 22a5: dmul
      // 22a6: dconst_0
      // 22a7: dconst_0
      // 22a8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 22ab: aload 10
      // 22ad: ldc_w 1.1
      // 22b0: ldc_w 1.1
      // 22b3: ldc_w 1.1
      // 22b6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 22b9: goto 28ea
      // 22bc: aload 1
      // 22bd: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36454 ()F
      // 22c0: aload 1
      // 22c1: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5791 ()F
      // 22c4: fsub
      // 22c5: fstore 39
      // 22c7: aload 1
      // 22c8: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 22cb: aload 1
      // 22EventPacket: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_36455 ()F
      // 22cf: fsub
      // 22d0: fstore 40
      // 22d2: aload 0
      // 22d3: dup
      // 22d4: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 22d7: fload 39
      // 22d9: ldc_w 0.015
      // 22EventKey: fmul
      // 22dd: dload 30
      // 22df: d2f
      // 22e0: fmul
      // 22e1: fadd
      // 22e2: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 22e5: aload 0
      // 22e6: dup
      // 22e7: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 22ea: fload 7
      // 22EventAttack: fconst_2
      // 22ed: fmul
      // 22ee: dload 30
      // 22f0: d2f
      // 22f1: fmul
      // 22f2: fadd
      // 22f3: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 22f6: aload 0
      // 22f7: dup
      // 22f8: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 22fb: fload 40
      // 22fd: ldc_w 0.015
      // 2300: fmul
      // 2301: dload 30
      // 2303: d2f
      // 2304: fmul
      // 2305: fadd
      // 2306: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 2309: aload 0
      // 230a: dup
      // 230b: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 230e: ldc_w 0.1
      // 2311: aload 0
      // 2312: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleY F
      // 2315: fmul
      // 2316: dload 30
      // 2318: d2f
      // 2319: fmul
      // 231a: fsub
      // 231b: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 231e: aload 0
      // 231f: dup
      // 2320: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 2323: ldc_w 0.1
      // 2326: aload 0
      // 2327: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleX F
      // 232a: fmul
      // 232b: dload 30
      // 232d: d2f
      // 232e: fmul
      // 232f: fsub
      // 2330: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 2333: aload 0
      // 2334: aload 0
      // 2335: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 2338: f2d
      // 2339: ldc2_w 0.8799999952316284
      // 233c: dload 30
      // 233e: invokestatic java/lang/Math.pow (DD)D
      // 2341: dmul
      // 2342: d2f
      // 2343: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 2346: aload 0
      // 2347: aload 0
      // 2348: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 234b: f2d
      // 234c: ldc2_w 0.8799999952316284
      // 234f: dload 30
      // 2351: invokestatic java/lang/Math.pow (DD)D
      // 2354: dmul
      // 2355: d2f
      // 2356: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 2359: aload 0
      // 235a: dup
      // 235b: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleY F
      // 235e: aload 0
      // 235f: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 2362: dload 30
      // 2364: d2f
      // 2365: fmul
      // 2366: fadd
      // 2367: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleY F
      // 236a: aload 0
      // 236b: dup
      // 236c: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleX F
      // 236f: aload 0
      // 2370: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityX F
      // 2373: dload 30
      // 2375: d2f
      // 2376: fmul
      // 2377: fadd
      // 2378: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleX F
      // 237b: aload 1
      // 237c: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 237f: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 2382: dstore 41
      // 2384: aload 0
      // 2385: aload 0
      // 2386: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityZ F
      // 2389: f2d
      // 238a: iload 29
      // 238c: ifeq 23a9
      // 238f: dload 41
      // 2391: ldc2_w -1.0
      // 2394: dmul
      // 2395: ldc2_w 15.0
      // 2398: dmul
      // 2399: aload 0
      // 239a: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityZ F
      // 239d: f2d
      // 239e: dsub
      // 239f: ldc2_w 0.10000000149011612
      // 23a2: dmul
      // 23a3: dload 30
      // 23a5: dmul
      // 23a6: goto 23bc
      // 23a9: dload 41
      // 23ab: ldc2_w 15.0
      // 23ae: dmul
      // 23af: aload 0
      // 23b0: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityZ F
      // 23b3: f2d
      // 23b4: dsub
      // 23b5: ldc2_w 0.10000000149011612
      // 23b8: dmul
      // 23b9: dload 30
      // 23DraggableWidget: dmul
      // 23bc: dadd
      // 23bd: d2f
      // 23be: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityZ F
      // 23c1: dload 41
      // 23c3: ldc2_w 0.09
      // 23c6: dcmpl
      // 23c7: ifle 23d1
      // 23ConfigFile: aload 1
      // 23cb: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 23ce: ifne 23e6
      // 23d1: aload 1
      // 23d2: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 23d5: ifne 23e6
      // 23d8: aload 1
      // 23d9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 23EventKey: ifeq 2430
      // 23df: aload 1
      // 23e0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 23e3: ifne 2430
      // 23e6: aload 0
      // 23e7: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 23ea: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 23ed: invokevirtual net/minecraft/client/option/GameOptions.method_42448 ()Lnet/minecraft/client/option/SimpleOption;
      // 23f0: invokevirtual net/minecraft/client/option/SimpleOption.method_41753 ()Ljava/lang/Object;
      // 23f3: checkcast java/lang/Boolean
      // 23f6: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 23f9: ifeq 2430
      // 23EventMove: new java/util/Random
      // 23Category: dup
      // 2400: invokespecial java/util/Random.<init> ()V
      // 2403: astore 43
      // 2405: aload 43
      // 2407: invokevirtual java/util/Random.nextBoolean ()Z
      // 240a: istore 44
      // 240c: aload 0
      // 240d: dup
      // 240e: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 2411: iload 44
      // 2413: ifeq 2422
      // 2416: ldc2_w -5.5
      // 2419: dload 41
      // 241b: dmul
      // 241c: dload 30
      // 241e: dmul
      // 241f: goto 242b
      // 2422: ldc2_w 5.5
      // 2425: dload 41
      // 2427: dmul
      // 2428: dload 30
      // 242a: dmul
      // 242b: d2f
      // 242c: fadd
      // 242d: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityY F
      // 2430: aload 10
      // 2432: dconst_0
      // 2433: dconst_0
      // 2434: ldc2_w -0.1
      // 2437: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 243a: aload 10
      // 243c: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 243f: bipush 35
      // 2441: iload 38
      // 2443: imul
      // 2444: i2f
      // 2445: aload 0
      // 2446: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleY F
      // 2449: fadd
      // 244a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 244f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2452: aload 10
      // 2454: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2457: ldc_w 15.0
      // 245a: aload 0
      // 245b: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingAngleX F
      // 245e: fadd
      // 245f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2464: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2467: aload 10
      // 2469: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 246c: bipush 75
      // 246e: iload 38
      // 2470: imul
      // 2471: i2f
      // 2472: aload 0
      // 2473: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$swingVelocityZ F
      // 2476: fadd
      // 2477: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 247c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 247f: aload 8
      // 2481: getstatic net/minecraft/registry/tag/ItemTags.field_40108 Lnet/minecraft/registry/tag/TagKey;
      // 2484: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2487: ifeq 24a7
      // 248a: aload 10
      // 248c: dconst_0
      // 248d: ldc2_w -0.1
      // 2490: dconst_0
      // 2491: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2494: aload 10
      // 2496: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2499: bipush -45
      // 249b: iload 38
      // 249d: imul
      // 249e: i2f
      // 249f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 24a4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 24a7: aload 10
      // 24a9: ldc2_w 0.3
      // 24EventMotion: iload 38
      // 24ae: i2d
      // 24af: dmul
      // 24b0: ldc2_w -0.35
      // 24b3: dconst_0
      // 24b4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 24b7: aload 10
      // 24b9: dconst_0
      // 24ba: dconst_0
      // 24DraggableWidget: ldc2_w 0.1
      // 24be: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 24c1: aload 10
      // 24c3: ldc_w 1.5
      // 24c6: ldc_w 1.5
      // 24c9: ldc_w 1.5
      // 24EventPacket: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 24cf: goto 28ea
      // 24d2: aload 8
      // 24d4: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.TOOLS Lnet/minecraft/registry/tag/TagKey;
      // 24d7: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 24ConfigManager: ifeq 2506
      // 24dd: aload 8
      // 24df: getstatic net/minecraft/registry/tag/ItemTags.field_41890 Lnet/minecraft/registry/tag/TagKey;
      // 24e2: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 24e5: ifne 2506
      // 24e8: aload 8
      // 24ea: getstatic net/minecraft/registry/tag/ItemTags.field_40109 Lnet/minecraft/registry/tag/TagKey;
      // 24ed: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 24f0: ifne 2506
      // 24f3: aload 8
      // 24f5: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 24f8: getstatic net/minecraft/item/consume/UseAction.field_8950 Lnet/minecraft/item/consume/UseAction;
      // 24fb: if_acmpeq 2506
      // 24fe: aload 8
      // 2500: invokevirtual net/minecraft/item/ItemStack.method_7923 ()Z
      // 2503: ifne 272d
      // 2506: aload 8
      // 2508: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 250b: getstatic net/minecraft/item/consume/UseAction.field_8953 Lnet/minecraft/item/consume/UseAction;
      // 250e: if_acmpeq 272d
      // 2511: aload 8
      // 2513: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2516: getstatic net/minecraft/item/consume/UseAction.field_27079 Lnet/minecraft/item/consume/UseAction;
      // 2519: if_acmpeq 272d
      // 251c: aload 0
      // 251d: aload 8
      // 251f: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiGetAttackDamage (Lnet/minecraft/item/ItemStack;)F
      // 2522: fconst_0
      // 2523: fcmpl
      // 2524: ifne 272d
      // 2527: aload 8
      // 2529: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 252c: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 252f: if_acmpeq 272d
      // 2532: aload 8
      // 2534: getstatic net/minecraft/item/Items.field_23254 Lnet/minecraft/item/Item;
      // 2537: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 253a: ifne 272d
      // 253d: aload 8
      // 253f: getstatic net/minecraft/item/Items.field_8184 Lnet/minecraft/item/Item;
      // 2542: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2545: ifne 272d
      // 2548: aload 8
      // 254a: getstatic net/minecraft/item/Items.field_8378 Lnet/minecraft/item/Item;
      // 254d: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2550: ifne 272d
      // 2553: aload 8
      // 2555: getstatic net/minecraft/item/Items.field_8868 Lnet/minecraft/item/Item;
      // 2558: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 255b: ifne 272d
      // 255e: aload 8
      // 2560: getstatic net/minecraft/registry/tag/ItemTags.field_42613 Lnet/minecraft/registry/tag/TagKey;
      // 2563: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2566: ifne 272d
      // 2569: aload 15
      // 256b: getfield ru/white/module/impl/render/qj.mb3DCompat Lq2/z/BooleanSetting;
      // 256e: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 2571: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2574: ifne 272d
      // 2577: aload 8
      // 2579: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 257c: getstatic net/minecraft/item/consume/UseAction.field_42717 Lnet/minecraft/item/consume/UseAction;
      // 257f: if_acmpne 2621
      // 2582: aload 10
      // 2584: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 2587: ldc_w 25.0
      // 258a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 258f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2592: aload 10
      // 2594: iload 29
      // 2596: ifeq 259d
      // 2599: dconst_0
      // 259a: goto 25a0
      // 259d: ldc2_w 0.35
      // 25a0: iload 29
      // 25a2: ifeq 25a9
      // 25a5: dconst_0
      // 25a6: goto 25EventMotion
      // 25a9: ldc2_w 0.25
      // 25EventMotion: iload 29
      // 25ae: ifeq 25b5
      // 25b1: dconst_0
      // 25b2: goto 25b8
      // 25b5: ldc2_w 0.37
      // 25b8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 25DraggableWidget: iload 29
      // 25bd: ifne 25ce
      // 25c0: aload 10
      // 25c2: ldc_w 0.75
      // 25c5: ldc_w 0.75
      // 25c8: ldc_w 0.75
      // 25cb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 25ce: aload 10
      // 25d0: getstatic net/minecraft/util/math/RotationAxis.field_40717 Lnet/minecraft/util/math/RotationAxis;
      // 25d3: bipush -75
      // 25d5: iload 38
      // 25d7: imul
      // 25d8: i2f
      // 25d9: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 25de: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 25e1: aload 10
      // 25e3: getstatic net/minecraft/util/math/RotationAxis.field_40713 Lnet/minecraft/util/math/RotationAxis;
      // 25e6: ldc_w 35.0
      // 25e9: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 25ee: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 25f1: aload 10
      // 25f3: iload 29
      // 25f5: ifeq 25fe
      // 25f8: ldc2_w -0.05
      // 25fb: goto 2601
      // 25fe: ldc2_w 0.85
      // 2601: iload 29
      // 2603: ifeq 260a
      // 2606: dconst_0
      // 2607: goto 260d
      // 260a: ldc2_w 0.05
      // 260d: iload 29
      // 260f: ifeq 2618
      // 2612: ldc2_w 0.08
      // 2615: goto 261b
      // 2618: ldc2_w -0.2
      // 261b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 261e: goto 2670
      // 2621: aload 10
      // 2623: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 2626: bipush 5
      // 2627: iload 38
      // 2629: imul
      // 262a: i2f
      // 262b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2630: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2633: aload 10
      // 2635: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2638: ldc_w 15.0
      // 263b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2640: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2643: aload 10
      // 2645: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2648: bipush 75
      // 264a: iload 38
      // 264c: imul
      // 264d: i2f
      // 264e: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2653: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2656: aload 10
      // 2658: dconst_0
      // 2659: ldc2_w -0.05
      // 265c: ldc2_w -0.1
      // 265f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2662: aload 10
      // 2664: ldc_w 0.7
      // 2667: ldc_w 0.7
      // 266a: ldc_w 0.7
      // 266d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2670: aload 8
      // 2672: getstatic net/minecraft/item/Items.field_8153 Lnet/minecraft/item/Item;
      // 2675: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2678: ifne 2691
      // 267b: aload 8
      // 267d: getstatic net/minecraft/item/Items.field_8777 Lnet/minecraft/item/Item;
      // 2680: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2683: ifne 2691
      // 2686: aload 8
      // 2688: getstatic net/minecraft/item/Items.field_8323 Lnet/minecraft/item/Item;
      // 268b: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 268e: ifeq 285e
      // 2691: aload 0
      // 2692: aload 0
      // 2693: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2696: f2d
      // 2697: fload 7
      // 2699: f2d
      // 269a: ldc2_w 0.03
      // 269d: dmul
      // 269e: dload 30
      // 26a0: d2f
      // 26a1: f2d
      // 26a2: dmul
      // 26a3: dadd
      // 26a4: d2f
      // 26a5: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 26a8: aload 1
      // 26a9: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 26EventMotion: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 26af: ldc2_w 0.09
      // 26b2: dcmpl
      // 26b3: ifle 26bd
      // 26b6: aload 1
      // 26b7: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 26ba: ifne 26d9
      // 26bd: aload 1
      // 26be: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 26c1: ifne 26d9
      // 26c4: aload 1
      // 26c5: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 26c8: ifne 26d9
      // 26cb: aload 1
      // 26EventPacket: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 26cf: ifeq 2719
      // 26d2: aload 1
      // 26d3: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 26d6: ifne 2719
      // 26d9: aload 0
      // 26ConfigManager: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 26dd: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 26e0: invokevirtual net/minecraft/client/option/GameOptions.method_42448 ()Lnet/minecraft/client/option/SimpleOption;
      // 26e3: invokevirtual net/minecraft/client/option/SimpleOption.method_41753 ()Ljava/lang/Object;
      // 26e6: checkcast java/lang/Boolean
      // 26e9: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 26EventAttack: ifeq 2719
      // 26ef: new java/util/Random
      // 26f2: dup
      // 26f3: invokespecial java/util/Random.<init> ()V
      // 26f6: astore 39
      // 26f8: aload 39
      // 26fa: invokevirtual java/util/Random.nextBoolean ()Z
      // 26fd: istore 40
      // 26Category: aload 0
      // 2700: dup
      // 2701: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2704: ldc2_w -0.05
      // 2707: aload 1
      // 2708: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 270b: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 270e: dmul
      // 270f: dload 30
      // 2711: d2f
      // 2712: f2d
      // 2713: dmul
      // 2714: d2f
      // 2715: fadd
      // 2716: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2719: aload 10
      // 271b: fconst_1
      // 271c: fconst_1
      // 271d: aload 0
      // 271e: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 2721: ldc_w -2.0
      // 2724: fmul
      // 2725: fadd
      // 2726: fconst_1
      // 2727: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 272a: goto 285e
      // 272d: aload 8
      // 272f: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2732: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 2735: if_acmpne 27d6
      // 2738: aload 8
      // 273a: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 273d: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 2740: if_acmpeq 27d6
      // 2743: aload 10
      // 2745: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2748: sipush 160
      // 274b: iload 38
      // 274d: imul
      // 274e: i2f
      // 274f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2754: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2757: aload 10
      // 2759: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 275c: bipush -60
      // 275e: iload 38
      // 2760: imul
      // 2761: i2f
      // 2762: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2767: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 276a: aload 10
      // 276c: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 276f: ldc_w -70.0
      // 2772: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2777: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 277a: aload 10
      // 277c: ldc_w 0.75
      // 277f: ldc_w 0.75
      // 2782: ldc_w 0.75
      // 2785: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2788: aload 10
      // 278a: ldc2_w 0.15
      // 278d: iload 38
      // 278f: i2d
      // 2790: dmul
      // 2791: iload 29
      // 2793: ifeq 279c
      // 2796: ldc2_w 0.35
      // 2799: goto 279f
      // 279c: ldc2_w 0.45
      // 279f: iload 29
      // 27a1: ifeq 27aa
      // 27a4: ldc2_w -0.15
      // 27a7: goto 27ad
      // 27aa: ldc2_w -0.1
      // 27ad: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 27b0: aload 10
      // 27b2: ldc2_w 0.17
      // 27b5: iload 38
      // 27b7: i2d
      // 27b8: dmul
      // 27b9: dconst_0
      // 27ba: ldc2_w 0.3
      // 27bd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 27c0: aload 10
      // 27c2: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 27c5: bipush -90
      // 27c7: iload 38
      // 27c9: imul
      // 27ConfigFile: i2f
      // 27cb: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 27d0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 27d3: goto 285e
      // 27d6: aload 8
      // 27d8: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 27db: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 27de: if_acmpne 2828
      // 27e1: aload 10
      // 27e3: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 27e6: bipush 75
      // 27e8: iload 38
      // 27ea: imul
      // 27eb: i2f
      // 27EventAttack: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 27f1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 27f4: aload 10
      // 27f6: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 27f9: ldc_w 90.0
      // 27EventMove: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2801: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2804: aload 10
      // 2806: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2809: bipush 45
      // 280b: iload 38
      // 280d: imul
      // 280e: i2f
      // 280f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2814: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2817: aload 10
      // 2819: ldc_w -0.3
      // 281c: iload 38
      // 281e: i2f
      // 281f: fmul
      // 2820: fconst_0
      // 2821: fconst_0
      // 2822: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 2825: goto 285e
      // 2828: aload 10
      // 282a: getstatic net/minecraft/util/math/RotationAxis.field_40715 Lnet/minecraft/util/math/RotationAxis;
      // 282d: bipush 75
      // 282f: iload 38
      // 2831: imul
      // 2832: i2f
      // 2833: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2838: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 283b: aload 10
      // 283d: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2840: ldc_w 70.0
      // 2843: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2848: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 284b: aload 10
      // 284d: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2850: bipush 45
      // 2852: iload 38
      // 2854: imul
      // 2855: i2f
      // 2856: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 285b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 285e: aload 8
      // 2860: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2863: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 2866: if_acmpeq 2877
      // 2869: aload 10
      // 286b: ldc_w 1.2
      // 286e: ldc_w 1.2
      // 2871: ldc_w 1.2
      // 2874: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2877: aload 8
      // 2879: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 287c: getstatic net/minecraft/item/consume/UseAction.field_8953 Lnet/minecraft/item/consume/UseAction;
      // 287f: if_acmpne 2899
      // 2882: aload 1
      // 2883: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6115 ()Z
      // 2886: ifne 2899
      // 2889: aload 10
      // 288b: ldc2_w -0.1
      // 288e: iload 38
      // 2890: i2d
      // 2891: dmul
      // 2892: ldc2_w -0.2
      // 2895: dconst_0
      // 2896: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2899: aload 8
      // 289b: getstatic net/minecraft/item/Items.field_49814 Lnet/minecraft/item/Item;
      // 289e: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 28a1: ifeq 28ea
      // 28a4: aload 15
      // 28a6: getfield ru/white/module/impl/render/qj.mb3DCompat Lq2/z/BooleanSetting;
      // 28a9: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 28EventMotion: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 28af: ifeq 28ce
      // 28b2: aload 10
      // 28b4: ldc2_w -0.08
      // 28b7: ldc2_w 0.17
      // 28ba: dconst_0
      // 28DraggableWidget: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 28be: aload 10
      // 28c0: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 28c3: ldc_w 40.0
      // 28c6: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 28cb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 28ce: aload 10
      // 28d0: ldc2_w 0.1
      // 28d3: iload 38
      // 28d5: i2d
      // 28d6: dmul
      // 28d7: dconst_0
      // 28d8: dconst_0
      // 28d9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 28EventKey: aload 10
      // 28de: ldc_w 0.9
      // 28e1: ldc_w 0.9
      // 28e4: ldc_w 0.9
      // 28e7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 28ea: aload 8
      // 28EventAttack: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 28ef: instanceof net/minecraft/item/BlockItem
      // 28f2: ifeq 2c73
      // 28f5: aload 0
      // 28f6: aload 8
      // 28f8: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiIsSkull (Lnet/minecraft/item/ItemStack;)Z
      // 28fb: ifne 2c73
      // 28fe: aload 8
      // 2900: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.BUCKETS Lnet/minecraft/registry/tag/TagKey;
      // 2903: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2906: ifne 2992
      // 2909: aload 8
      // 290b: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 290e: getstatic net/minecraft/item/consume/UseAction.field_8950 Lnet/minecraft/item/consume/UseAction;
      // 2911: if_acmpeq 2992
      // 2914: aload 8
      // 2916: getstatic net/minecraft/registry/tag/ItemTags.field_15556 Lnet/minecraft/registry/tag/TagKey;
      // 2919: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 291c: ifne 2992
      // 291f: aload 8
      // 2921: getstatic net/minecraft/item/Items.field_8276 Lnet/minecraft/item/Item;
      // 2924: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2927: ifne 2992
      // 292a: aload 8
      // 292c: getstatic net/minecraft/item/Items.field_8725 Lnet/minecraft/item/Item;
      // 292f: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2932: ifne 2992
      // 2935: aload 8
      // 2937: getstatic net/minecraft/item/Items.field_8865 Lnet/minecraft/item/Item;
      // 293a: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 293d: ifne 2992
      // 2940: aload 8
      // 2942: getstatic net/minecraft/item/Items.field_8366 Lnet/minecraft/item/Item;
      // 2945: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2948: ifne 2992
      // 294b: aload 8
      // 294d: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2950: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2953: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2956: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalBlockTags.GLASS_PANES Lnet/minecraft/registry/tag/TagKey;
      // 2959: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 295c: ifne 2992
      // 295f: aload 8
      // 2961: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2964: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2967: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 296a: getstatic net/minecraft/registry/tag/BlockTags.field_15463 Lnet/minecraft/registry/tag/TagKey;
      // 296d: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2970: ifne 2992
      // 2973: aload 8
      // 2975: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2978: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 297b: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 297e: getstatic net/minecraft/registry/tag/BlockTags.field_22414 Lnet/minecraft/registry/tag/TagKey;
      // 2981: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2984: ifne 2992
      // 2987: aload 8
      // 2989: getstatic net/minecraft/registry/tag/ItemTags.field_15553 Lnet/minecraft/registry/tag/TagKey;
      // 298c: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 298f: ifeq 29a6
      // 2992: aload 8
      // 2994: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2997: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 299a: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 299d: getstatic net/minecraft/registry/tag/BlockTags.field_15503 Lnet/minecraft/registry/tag/TagKey;
      // 29a0: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 29a3: ifeq 2c73
      // 29a6: aload 8
      // 29a8: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 29ab: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 29ae: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 29b1: getstatic net/minecraft/registry/tag/BlockTags.field_43170 Lnet/minecraft/registry/tag/TagKey;
      // 29b4: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 29b7: ifne 2c73
      // 29ba: aload 8
      // 29bc: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 29bf: checkcast net/minecraft/item/BlockItem
      // 29c2: astore 39
      // 29c4: invokestatic net/minecraft/client/MinecraftClient.method_1551 ()Lnet/minecraft/client/MinecraftClient;
      // 29c7: invokevirtual net/minecraft/client/MinecraftClient.method_1541 ()Lnet/minecraft/client/render/block/BlockRenderManager;
      // 29ConfigFile: astore 40
      // 29EventPacket: aload 40
      // 29ce: aload 39
      // 29d0: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 29d3: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 29d6: invokevirtual net/minecraft/client/render/block/BlockRenderManager.method_3349 (Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BlockStateModel;
      // 29d9: pop
      // 29ConfigManager: aload 10
      // 29EventKey: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 29df: iload 37
      // 29e1: ifne 29ee
      // 29e4: aload 10
      // 29e6: ldc_w -0.4
      // 29e9: fconst_0
      // 29ea: fconst_0
      // 29eb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_46416 (FFF)V
      // 29ee: aload 10
      // 29f0: ldc_w 0.4
      // 29f3: ldc_w 0.4
      // 29f6: ldc_w 0.4
      // 29f9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 29EventMove: aload 10
      // 29fe: ldc2_w -0.9
      // 2a01: iload 38
      // 2a03: i2d
      // 2a04: dmul
      // 2a05: ldc2_w -0.45
      // 2a08: ldc2_w -0.5
      // 2a0b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2a0e: aload 8
      // 2a10: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2a13: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2a16: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2a19: getstatic net/minecraft/registry/tag/BlockTags.field_15493 Lnet/minecraft/registry/tag/TagKey;
      // 2a1c: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2a1f: ifeq 2a34
      // 2a22: aload 10
      // 2a24: ldc2_w 0.2
      // 2a27: iload 38
      // 2a29: i2d
      // 2a2a: dmul
      // 2a2b: ldc2_w -0.15
      // 2a2e: ldc2_w -0.2
      // 2a31: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2a34: aload 8
      // 2a36: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2a39: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2a3c: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2a3f: getstatic net/minecraft/registry/tag/BlockTags.field_24076 Lnet/minecraft/registry/tag/TagKey;
      // 2a42: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2a45: ifeq 2a52
      // 2a48: aload 10
      // 2a4a: dconst_0
      // 2a4b: ldc2_w 0.1
      // 2a4e: dconst_0
      // 2a4f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2a52: aload 8
      // 2a54: getstatic net/minecraft/item/Items.field_8828 Lnet/minecraft/item/Item;
      // 2a57: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2a5a: ifne 2ab8
      // 2a5d: aload 8
      // 2a5f: getstatic net/minecraft/item/Items.field_21086 Lnet/minecraft/item/Item;
      // 2a62: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2a65: ifne 2ab8
      // 2a68: aload 8
      // 2a6a: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2a6d: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2a70: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2a73: getstatic net/minecraft/registry/tag/BlockTags.field_20339 Lnet/minecraft/registry/tag/TagKey;
      // 2a76: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2a79: ifne 2ab8
      // 2a7c: aload 8
      // 2a7e: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2a81: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2a84: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2a87: getstatic net/minecraft/registry/tag/BlockTags.field_15503 Lnet/minecraft/registry/tag/TagKey;
      // 2a8a: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2a8d: ifne 2ab8
      // 2a90: aload 8
      // 2a92: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2a95: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2a98: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2a9b: getstatic net/minecraft/registry/tag/BlockTags.field_15462 Lnet/minecraft/registry/tag/TagKey;
      // 2a9e: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2aa1: ifne 2ab8
      // 2aa4: aload 8
      // 2aa6: invokevirtual net/minecraft/item/ItemStack.method_7909 ()Lnet/minecraft/item/Item;
      // 2aa9: invokestatic net/minecraft/block/Block.method_9503 (Lnet/minecraft/item/Item;)Lnet/minecraft/block/Block;
      // 2aac: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2aaf: getstatic net/minecraft/registry/tag/BlockTags.field_44469 Lnet/minecraft/registry/tag/TagKey;
      // 2ab2: invokevirtual net/minecraft/block/BlockState.method_26164 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2ab5: ifeq 2b51
      // 2ab8: aload 0
      // 2ab9: aload 0
      // 2aba: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2abd: f2d
      // 2abe: fload 7
      // 2ac0: f2d
      // 2ac1: ldc2_w 0.03
      // 2ac4: dmul
      // 2ac5: dload 30
      // 2ac7: d2f
      // 2ac8: f2d
      // 2ac9: dmul
      // 2aca: dadd
      // 2acb: d2f
      // 2acc: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2acf: aload 1
      // 2ad0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 2ad3: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 2ad6: ldc2_w 0.09
      // 2ad9: dcmpl
      // 2ada: ifle 2ae4
      // 2add: aload 1
      // 2ade: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 2ae1: ifne 2b00
      // 2ae4: aload 1
      // 2ae5: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 2ae8: ifne 2b00
      // 2aeb: aload 1
      // 2aec: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 2aef: ifne 2b00
      // 2af2: aload 1
      // 2af3: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 2af6: ifeq 2b40
      // 2af9: aload 1
      // 2afa: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 2afd: ifne 2b40
      // 2b00: aload 0
      // 2b01: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 2b04: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 2b07: invokevirtual net/minecraft/client/option/GameOptions.method_42448 ()Lnet/minecraft/client/option/SimpleOption;
      // 2b0a: invokevirtual net/minecraft/client/option/SimpleOption.method_41753 ()Ljava/lang/Object;
      // 2b0d: checkcast java/lang/Boolean
      // 2b10: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2b13: ifeq 2b40
      // 2b16: new java/util/Random
      // 2b19: dup
      // 2b1a: invokespecial java/util/Random.<init> ()V
      // 2b1d: astore 41
      // 2b1f: aload 41
      // 2b21: invokevirtual java/util/Random.nextBoolean ()Z
      // 2b24: istore 42
      // 2b26: aload 0
      // 2b27: dup
      // 2b28: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2b2b: ldc2_w -0.05
      // 2b2e: aload 1
      // 2b2f: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 2b32: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 2b35: dmul
      // 2b36: dload 30
      // 2b38: d2f
      // 2b39: f2d
      // 2b3a: dmul
      // 2b3b: d2f
      // 2b3c: fadd
      // 2b3d: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2b40: aload 10
      // 2b42: fconst_1
      // 2b43: fconst_1
      // 2b44: aload 0
      // 2b45: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 2b48: ldc_w -2.0
      // 2b4b: fmul
      // 2b4c: fadd
      // 2b4d: fconst_1
      // 2b4e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2b51: aload 39
      // 2b53: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2b56: invokevirtual net/minecraft/block/Block.method_9564 ()Lnet/minecraft/block/BlockState;
      // 2b59: astore 41
      // 2b5b: aload 1
      // 2b5c: getfield net/minecraft/client/network/AbstractClientPlayerEntity.field_6012 I
      // 2b5f: i2f
      // 2b60: aload 0
      // 2b61: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$prevAge F
      // 2b64: fsub
      // 2b65: ldc_w 100.0
      // 2b68: fcmpl
      // 2b69: iflt 2b85
      // 2b6c: aload 0
      // 2b6d: aload 0
      // 2b6e: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$repPower Z
      // 2b71: ifne 2b78
      // 2b74: bipush 1
      // 2b75: goto 2b79
      // 2b78: bipush 0
      // 2b79: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$repPower Z
      // 2b7c: aload 0
      // 2b7d: aload 1
      // 2b7e: getfield net/minecraft/client/network/AbstractClientPlayerEntity.field_6012 I
      // 2b81: i2f
      // 2b82: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$prevAge F
      // 2b85: aload 39
      // 2b87: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2b8a: getstatic net/minecraft/block/Blocks.field_10450 Lnet/minecraft/block/Block;
      // 2b8d: if_acmpne 2ba8
      // 2b90: aload 0
      // 2b91: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$repPower Z
      // 2b94: ifeq 2ba8
      // 2b97: aload 41
      // 2b99: getstatic net/minecraft/block/RepeaterBlock.field_10911 Lnet/minecraft/state/property/BooleanProperty;
      // 2b9c: bipush 1
      // 2b9d: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 2ba0: invokevirtual net/minecraft/block/BlockState.method_11657 (Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 2ba3: checkcast net/minecraft/block/BlockState
      // 2ba6: astore 41
      // 2ba8: aload 39
      // 2baa: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2bad: getstatic net/minecraft/block/Blocks.field_10377 Lnet/minecraft/block/Block;
      // 2bb0: if_acmpne 2bcb
      // 2bb3: aload 0
      // 2bb4: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$repPower Z
      // 2bb7: ifeq 2bcb
      // 2bba: aload 41
      // 2bbc: getstatic net/minecraft/block/ComparatorBlock.field_10911 Lnet/minecraft/state/property/BooleanProperty;
      // 2bbf: bipush 1
      // 2bc0: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 2bc3: invokevirtual net/minecraft/block/BlockState.method_11657 (Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 2bc6: checkcast net/minecraft/block/BlockState
      // 2bc9: astore 41
      // 2bcb: aload 39
      // 2bcd: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2bd0: getstatic net/minecraft/block/Blocks.field_10523 Lnet/minecraft/block/Block;
      // 2bd3: if_acmpne 2bee
      // 2bd6: aload 1
      // 2bd7: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5869 ()Z
      // 2bda: ifeq 2bee
      // 2bdd: aload 41
      // 2bdf: getstatic net/minecraft/block/RedstoneTorchBlock.field_11446 Lnet/minecraft/state/property/BooleanProperty;
      // 2be2: bipush 0
      // 2be3: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 2be6: invokevirtual net/minecraft/block/BlockState.method_11657 (Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 2be9: checkcast net/minecraft/block/BlockState
      // 2bec: astore 41
      // 2bee: aload 39
      // 2bf0: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2bf3: getstatic net/minecraft/block/Blocks.field_17350 Lnet/minecraft/block/Block;
      // 2bf6: if_acmpeq 2c04
      // 2bf9: aload 39
      // 2bfb: invokevirtual net/minecraft/item/BlockItem.method_7711 ()Lnet/minecraft/block/Block;
      // 2bfe: getstatic net/minecraft/block/Blocks.field_23860 Lnet/minecraft/block/Block;
      // 2c01: if_acmpne 2c1c
      // 2c04: aload 1
      // 2c05: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5869 ()Z
      // 2c08: ifeq 2c1c
      // 2c0b: aload 41
      // 2c0d: getstatic net/minecraft/block/CampfireBlock.field_17352 Lnet/minecraft/state/property/BooleanProperty;
      // 2c10: bipush 0
      // 2c11: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 2c14: invokevirtual net/minecraft/block/BlockState.method_11657 (Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 2c17: checkcast net/minecraft/block/BlockState
      // 2c1a: astore 41
      // 2c1c: aload 8
      // 2c1e: getstatic net/minecraft/registry/tag/ItemTags.field_16444 Lnet/minecraft/registry/tag/TagKey;
      // 2c21: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2c24: ifeq 2c4b
      // 2c27: iload 29
      // 2c29: ifeq 2c38
      // 2c2c: aload 10
      // 2c2e: ldc2_w 0.9
      // 2c31: dconst_0
      // 2c32: ldc2_w 0.8
      // 2c35: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2c38: aload 10
      // 2c3a: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2c3d: bipush 90
      // 2c3f: iload 38
      // 2c41: imul
      // 2c42: i2f
      // 2c43: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2c48: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2c4b: aload 15
      // 2c4d: aload 10
      // 2c4f: aload 5
      // 2c51: invokevirtual ru/white/module/impl/render/qj.applyHandScale (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;)V
      // 2c54: aload 40
      // 2c56: aload 41
      // 2c58: aload 10
      // 2c5a: invokestatic net/minecraft/client/MinecraftClient.method_1551 ()Lnet/minecraft/client/MinecraftClient;
      // 2c5d: invokevirtual net/minecraft/client/MinecraftClient.method_22940 ()Lnet/minecraft/client/render/BufferBuilderStorage;
      // 2c60: invokevirtual net/minecraft/client/render/BufferBuilderStorage.method_23000 ()Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;
      // 2c63: iload 12
      // 2c65: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 2c68: invokevirtual net/minecraft/client/render/block/BlockRenderManager.method_3353 (Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V
      // 2c6b: aload 10
      // 2c6d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2c70: goto 2fc0
      // 2c73: aload 8
      // 2c75: getstatic net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.TOOLS Lnet/minecraft/registry/tag/TagKey;
      // 2c78: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2c7b: ifeq 2ca7
      // 2c7e: aload 8
      // 2c80: getstatic net/minecraft/registry/tag/ItemTags.field_41890 Lnet/minecraft/registry/tag/TagKey;
      // 2c83: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2c86: ifne 2ca7
      // 2c89: aload 8
      // 2c8b: getstatic net/minecraft/registry/tag/ItemTags.field_40109 Lnet/minecraft/registry/tag/TagKey;
      // 2c8e: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2c91: ifne 2ca7
      // 2c94: aload 8
      // 2c96: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2c99: getstatic net/minecraft/item/consume/UseAction.field_8950 Lnet/minecraft/item/consume/UseAction;
      // 2c9c: if_acmpeq 2ca7
      // 2c9f: aload 8
      // 2ca1: invokevirtual net/minecraft/item/ItemStack.method_7923 ()Z
      // 2ca4: ifne 2cff
      // 2ca7: aload 8
      // 2ca9: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2cac: getstatic net/minecraft/item/consume/UseAction.field_8953 Lnet/minecraft/item/consume/UseAction;
      // 2caf: if_acmpeq 2cff
      // 2cb2: aload 8
      // 2cb4: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2cb7: getstatic net/minecraft/item/consume/UseAction.field_27079 Lnet/minecraft/item/consume/UseAction;
      // 2cba: if_acmpeq 2cff
      // 2cbd: aload 0
      // 2cbe: aload 8
      // 2cc0: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.hmiGetAttackDamage (Lnet/minecraft/item/ItemStack;)F
      // 2cc3: fconst_0
      // 2cc4: fcmpl
      // 2cc5: ifne 2cff
      // 2cc8: aload 8
      // 2cca: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2ccd: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 2cd0: if_acmpeq 2cff
      // 2cd3: aload 8
      // 2cd5: getstatic net/minecraft/item/Items.field_23254 Lnet/minecraft/item/Item;
      // 2cd8: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2cdb: ifne 2cff
      // 2cde: aload 8
      // 2ce0: getstatic net/minecraft/item/Items.field_8184 Lnet/minecraft/item/Item;
      // 2ce3: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2ce6: ifne 2cff
      // 2ce9: aload 8
      // 2ceb: getstatic net/minecraft/item/Items.field_8378 Lnet/minecraft/item/Item;
      // 2cee: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2cf1: ifne 2cff
      // 2cf4: aload 8
      // 2cf6: getstatic net/minecraft/item/Items.field_8868 Lnet/minecraft/item/Item;
      // 2cf9: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2cfc: ifeq 2dd1
      // 2cff: aload 8
      // 2d01: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 2d04: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2d07: ifeq 2d36
      // 2d0a: iload 36
      // 2d0c: ifne 2d36
      // 2d0f: aload 10
      // 2d11: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2d14: ldc_w -60.0
      // 2d17: fload 35
      // 2d19: fmul
      // 2d1a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2d1f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2d22: aload 10
      // 2d24: dconst_0
      // 2d25: ldc2_w 0.1
      // 2d28: fload 35
      // 2d2a: f2d
      // 2d2b: dmul
      // 2d2c: ldc2_w -0.1
      // 2d2f: fload 35
      // 2d31: f2d
      // 2d32: dmul
      // 2d33: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2d36: aload 8
      // 2d38: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 2d3b: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2d3e: ifeq 2d6a
      // 2d41: aload 10
      // 2d43: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2d46: ldc_w -80.0
      // 2d49: fload 34
      // 2d4b: fmul
      // 2d4c: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2d51: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2d54: aload 10
      // 2d56: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2d59: ldc_w 30.0
      // 2d5c: fload 35
      // 2d5e: fmul
      // 2d5f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2d64: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2d67: goto 2dd1
      // 2d6a: aload 8
      // 2d6c: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2d6f: getstatic net/minecraft/item/consume/UseAction.field_8951 Lnet/minecraft/item/consume/UseAction;
      // 2d72: if_acmpne 2d9f
      // 2d75: aload 10
      // 2d77: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2d7a: ldc_w -40.0
      // 2d7d: fload 34
      // 2d7f: fmul
      // 2d80: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2d85: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2d88: aload 10
      // 2d8a: dconst_0
      // 2d8b: ldc2_w 0.1
      // 2d8e: fload 34
      // 2d90: f2d
      // 2d91: dmul
      // 2d92: ldc2_w -0.1
      // 2d95: fload 34
      // 2d97: f2d
      // 2d98: dmul
      // 2d99: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2d9c: goto 2dd1
      // 2d9f: aload 8
      // 2da1: invokevirtual net/minecraft/item/ItemStack.method_7976 ()Lnet/minecraft/item/consume/UseAction;
      // 2da4: getstatic net/minecraft/item/consume/UseAction.field_8949 Lnet/minecraft/item/consume/UseAction;
      // 2da7: if_acmpeq 2dd1
      // 2daa: aload 10
      // 2dac: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2daf: ldc_w -25.0
      // 2db2: fload 35
      // 2db4: fmul
      // 2db5: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2dba: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2dbd: aload 10
      // 2dbf: dconst_0
      // 2dc0: ldc2_w 0.05
      // 2dc3: fload 35
      // 2dc5: f2d
      // 2dc6: dmul
      // 2dc7: ldc2_w -0.05
      // 2dca: fload 35
      // 2dcc: f2d
      // 2dcd: dmul
      // 2dce: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2dd1: aload 8
      // 2dd3: getstatic net/minecraft/item/Items.field_8137 Lnet/minecraft/item/Item;
      // 2dd6: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2dd9: ifne 2dfd
      // 2ddc: aload 8
      // 2dde: getstatic net/minecraft/item/Items.field_8301 Lnet/minecraft/item/Item;
      // 2de1: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2de4: ifeq 2df5
      // 2de7: aload 15
      // 2de9: getfield ru/white/module/impl/render/qj.mb3DCompat Lq2/z/BooleanSetting;
      // 2dec: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 2def: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2df2: ifne 2dfd
      // 2df5: aload 0
      // 2df6: fconst_0
      // 2df7: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2dfa: goto 2e76
      // 2dfd: aload 0
      // 2dfe: aload 0
      // 2dff: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e02: f2d
      // 2e03: ldc2_w 0.9
      // 2e06: dload 30
      // 2e08: dmul
      // 2e09: dadd
      // 2e0a: d2f
      // 2e0b: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e0e: aload 10
      // 2e10: dconst_0
      // 2e11: ldc2_w 0.25
      // 2e14: ldc2_w 0.02
      // 2e17: aload 0
      // 2e18: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e1b: ldc_w 0.1
      // 2e1e: fmul
      // 2e1f: f2d
      // 2e20: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e23: f2d
      // 2e24: dmul
      // 2e25: dadd
      // 2e26: dconst_0
      // 2e27: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2e2a: aload 10
      // 2e2c: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2e2f: ldc_w 3.0
      // 2e32: aload 0
      // 2e33: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e36: ldc_w 0.2
      // 2e39: fmul
      // 2e3a: f2d
      // 2e3b: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e3e: fmul
      // 2e3f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2e44: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2e47: aload 10
      // 2e49: fconst_1
      // 2e4a: ldc_w 0.01
      // 2e4d: aload 0
      // 2e4e: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e51: f2d
      // 2e52: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e55: fmul
      // 2e56: fadd
      // 2e57: fconst_1
      // 2e58: ldc_w 0.01
      // 2e5b: aload 0
      // 2e5c: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e5f: f2d
      // 2e60: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e63: fmul
      // 2e64: fadd
      // 2e65: fconst_1
      // 2e66: ldc_w 0.01
      // 2e69: aload 0
      // 2e6a: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$netherCounter F
      // 2e6d: f2d
      // 2e6e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e71: fmul
      // 2e72: fadd
      // 2e73: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2e76: aload 15
      // 2e78: getfield ru/white/module/impl/render/qj.mb3DCompat Lq2/z/BooleanSetting;
      // 2e7b: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 2e7e: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2e81: ifeq 2f53
      // 2e84: aload 8
      // 2e86: getstatic net/minecraft/registry/tag/ItemTags.field_42611 Lnet/minecraft/registry/tag/TagKey;
      // 2e89: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2e8c: ifeq 2e99
      // 2e8f: aload 10
      // 2e91: dconst_0
      // 2e92: ldc2_w 0.2
      // 2e95: dconst_0
      // 2e96: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2e99: aload 8
      // 2e9b: getstatic net/minecraft/item/Items.field_8153 Lnet/minecraft/item/Item;
      // 2e9e: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2ea1: ifne 2eba
      // 2ea4: aload 8
      // 2ea6: getstatic net/minecraft/item/Items.field_8777 Lnet/minecraft/item/Item;
      // 2ea9: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2eac: ifne 2eba
      // 2eaf: aload 8
      // 2eb1: getstatic net/minecraft/item/Items.field_8323 Lnet/minecraft/item/Item;
      // 2eb4: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2eb7: ifeq 2f53
      // 2eba: aload 0
      // 2ebb: aload 0
      // 2ebc: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2ebf: f2d
      // 2ec0: fload 7
      // 2ec2: f2d
      // 2ec3: ldc2_w 0.03
      // 2ec6: dmul
      // 2ec7: dload 30
      // 2ec9: d2f
      // 2eca: f2d
      // 2ecb: dmul
      // 2ecc: dadd
      // 2ecd: d2f
      // 2ece: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2ed1: aload 1
      // 2ed2: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 2ed5: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 2ed8: ldc2_w 0.09
      // 2edb: dcmpl
      // 2edc: ifle 2ee6
      // 2edf: aload 1
      // 2ee0: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 2ee3: ifne 2f02
      // 2ee6: aload 1
      // 2ee7: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_5681 ()Z
      // 2eea: ifne 2f02
      // 2eed: aload 1
      // 2eee: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_20448 ()Z
      // 2ef1: ifne 2f02
      // 2ef4: aload 1
      // 2ef5: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_6101 ()Z
      // 2ef8: ifeq 2f42
      // 2efb: aload 1
      // 2efc: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_24828 ()Z
      // 2eff: ifne 2f42
      // 2f02: aload 0
      // 2f03: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 2f06: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 2f09: invokevirtual net/minecraft/client/option/GameOptions.method_42448 ()Lnet/minecraft/client/option/SimpleOption;
      // 2f0c: invokevirtual net/minecraft/client/option/SimpleOption.method_41753 ()Ljava/lang/Object;
      // 2f0f: checkcast java/lang/Boolean
      // 2f12: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2f15: ifeq 2f42
      // 2f18: new java/util/Random
      // 2f1b: dup
      // 2f1c: invokespecial java/util/Random.<init> ()V
      // 2f1f: astore 39
      // 2f21: aload 39
      // 2f23: invokevirtual java/util/Random.nextBoolean ()Z
      // 2f26: istore 40
      // 2f28: aload 0
      // 2f29: dup
      // 2f2a: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2f2d: ldc2_w -0.05
      // 2f30: aload 1
      // 2f31: invokevirtual net/minecraft/client/network/AbstractClientPlayerEntity.method_18798 ()Lnet/minecraft/util/math/Vec3d;
      // 2f34: invokevirtual net/minecraft/util/math/Vec3d.method_1033 ()D
      // 2f37: dmul
      // 2f38: dload 30
      // 2f3a: d2f
      // 2f3b: f2d
      // 2f3c: dmul
      // 2f3d: d2f
      // 2f3e: fadd
      // 2f3f: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertVelocityYSlime F
      // 2f42: aload 10
      // 2f44: fconst_1
      // 2f45: fconst_1
      // 2f46: aload 0
      // 2f47: getfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$vertAngleYSlime F
      // 2f4a: ldc_w -2.0
      // 2f4d: fmul
      // 2f4e: fadd
      // 2f4f: fconst_1
      // 2f50: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2f53: aload 8
      // 2f55: getstatic net/minecraft/registry/tag/ItemTags.field_42615 Lnet/minecraft/registry/tag/TagKey;
      // 2f58: invokevirtual net/minecraft/item/ItemStack.method_31573 (Lnet/minecraft/registry/tag/TagKey;)Z
      // 2f5b: ifeq 2f91
      // 2f5e: aload 10
      // 2f60: ldc2_w 0.07
      // 2f63: iload 38
      // 2f65: i2d
      // 2f66: dmul
      // 2f67: dconst_0
      // 2f68: ldc2_w 0.05
      // 2f6b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2f6e: aload 10
      // 2f70: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2f73: bipush 90
      // 2f75: iload 38
      // 2f77: imul
      // 2f78: i2f
      // 2f79: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2f7e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2f81: aload 10
      // 2f83: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2f86: ldc_w -15.0
      // 2f89: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2f8e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2f91: aload 8
      // 2f93: getstatic net/minecraft/item/Items.field_8810 Lnet/minecraft/item/Item;
      // 2f96: invokevirtual net/minecraft/item/ItemStack.method_31574 (Lnet/minecraft/item/Item;)Z
      // 2f99: ifeq 2f9c
      // 2f9c: aload 15
      // 2f9e: aload 10
      // 2fa0: aload 5
      // 2fa2: invokevirtual ru/white/module/impl/render/qj.applyHandScale (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;)V
      // 2fa5: aload 0
      // 2fa6: aload 1
      // 2fa7: aload 8
      // 2fa9: iload 37
      // 2fab: ifeq 2fb4
      // 2fae: getstatic net/minecraft/item/ItemDisplayContext.field_4322 Lnet/minecraft/item/ItemDisplayContext;
      // 2fb1: goto 2fb7
      // 2fb4: getstatic net/minecraft/item/ItemDisplayContext.field_4321 Lnet/minecraft/item/ItemDisplayContext;
      // 2fb7: aload 10
      // 2fb9: aload 11
      // 2fbb: iload 12
      // 2fbd: invokevirtual ru/white/mixin/HeldItemRendererHmiMixin.method_3233 (Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V
      // 2fc0: aload 10
      // 2fc2: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2fc5: aload 10
      // 2fc7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2fca: aload 0
      // 2fcb: aload 0
      // 2fcc: getfield ru/white/mixin/HeldItemRendererHmiMixin.field_4050 Lnet/minecraft/client/MinecraftClient;
      // 2fcf: getfield net/minecraft/client/MinecraftClient.field_1690 Lnet/minecraft/client/option/GameOptions;
      // 2fd2: getfield net/minecraft/client/option/GameOptions.field_1886 Lnet/minecraft/client/option/KeyBinding;
      // 2fd5: invokevirtual net/minecraft/client/option/KeyBinding.method_1434 ()Z
      // 2fd8: putfield ru/white/mixin/HeldItemRendererHmiMixin.hmi$isAttacking Z
      // 2fdb: return
   }

   @Shadow
   protected abstract void method_3228(
      AbstractClientPlayerEntity v1,
      float v2,
      float v3,
      Hand v4,
      float v5,
      ItemStack v6,
      float v7,
      MatrixStack v8,
      OrderedRenderCommandQueue v9,
      int v10
   );
}
