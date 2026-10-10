package ru.white.mixin;

import com.mojang.blaze3d.shaders.ShaderType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderLoader.Cache;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.utils.render.shader.nu;

@Environment(EnvType.CLIENT)
@Mixin({Cache.class})
public class ShaderLoaderMixin {
   @Inject(
      method = {"method_68498"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void white$provideEmbeddedSource(Identifier id, ShaderType type, CallbackInfoReturnable<String> cir) {
      String source = nu.getSource(id, type);
      if (source != null) {
         cir.setReturnValue(source);
      }
   }
}
