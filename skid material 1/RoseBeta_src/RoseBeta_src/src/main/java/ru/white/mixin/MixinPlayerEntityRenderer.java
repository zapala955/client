package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.AutoTool;
import ru.white.utils.helper.La;
import ru.white.utils.helper.Pa;
import ru.white.module.impl.render.NameTag;
import ru.white.utils.render.sr;

@Environment(EnvType.CLIENT)
@Mixin({PlayerEntityRenderer.class})
public class MixinPlayerEntityRenderer {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void onInit(Context ctx, boolean slim, CallbackInfo ci) {
      PlayerEntityRenderer renderer = (PlayerEntityRenderer)this;
      renderer.method_4046(new sr(renderer));
   }

   @Inject(
      method = {"method_62604"},
      at = {@At("TAIL")}
   )
   private void onUpdateRenderState(PlayerLikeEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
      if (state instanceof pa carrier) {
         carrier.rose$setEmotePose(la.INSTANCE.poseFor(player.method_5667()));
      }

      if (player == MinecraftClient.method_1551().field_1724 && ci.isSilentActive()) {
         ItemStack visual = ci.getVisualItem();
         if (visual != null) {
            Arm mainArm = player.method_6068();
            if (mainArm == Arm.field_6183) {
               state.field_63601 = visual;
               MinecraftClient.method_1551().method_65386().method_65597(state.field_55305, visual, ItemDisplayContext.field_4320, player);
            } else {
               state.field_63602 = visual;
               MinecraftClient.method_1551().method_65386().method_65597(state.field_55307, visual, ItemDisplayContext.field_4323, player);
            }
         }
      }

      NameTag esp = NameTag.get();
      if (esp != null && esp.isEnabled() && esp.player.getValue()) {
         if (esp.ignoreNaked.getValue() && player instanceof PlayerEntity p && !NameTag.hasArmor(p)) {
            return;
         }

         state.field_53525 = null;
         state.field_53337 = null;
      }
   }
}
