package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import net.minecraft.util.AssetInfo.TextureAssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.Client;

@Environment(EnvType.CLIENT)
@Mixin({AbstractClientPlayerEntity.class})
public abstract class PlayerListEntryMixin {
   @Unique
   private static final Identifier CAPE_ID = Identifier.method_60655("client", "cape");
   @Unique
   private static final TextureAssetInfo CAPE_ASSET = new TextureAssetInfo(CAPE_ID);

   @Inject(
      method = {"method_52814"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void replaceCape(CallbackInfoReturnable<SkinTextures> cir) {
      AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)this;
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null) {
         boolean isLocal = player.method_5667().equals(client.field_1724.method_5667());
         boolean isFriend = Client.get().friendManager().isFriend(player.method_5477().getString());
         if (isLocal || isFriend) {
            SkinTextures old = (SkinTextures)cir.getReturnValue();
            cir.setReturnValue(new SkinTextures(old.comp_1626(), CAPE_ASSET, CAPE_ASSET, old.comp_1629(), old.comp_1630()));
         }
      }
   }
}
