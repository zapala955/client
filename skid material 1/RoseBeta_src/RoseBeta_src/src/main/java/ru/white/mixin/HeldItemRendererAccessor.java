package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public interface HeldItemRendererAccessor {
   @Invoker("method_3216")
   void invokeRenderArm(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, Arm v4);

   @Invoker("method_3231")
   void invokeRenderMapInBothHands(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, float v4, float v5, float v6);

   @Invoker("method_3222")
   void invokeRenderMapInOneHand(MatrixStack v1, OrderedRenderCommandQueue v2, int v3, float v4, Arm v5, float v6, ItemStack v7);

   @Invoker("method_3224")
   void invokeApplyEquipOffset(MatrixStack v1, Arm v2, float v3);

   @Invoker("method_65816")
   void invokeSwingArm(float v1, MatrixStack v2, int v3, Arm v4);

   @Invoker("method_3233")
   void invokeRenderItem(LivingEntity v1, ItemStack v2, ItemDisplayContext v3, MatrixStack v4, OrderedRenderCommandQueue v5, int v6);
}
