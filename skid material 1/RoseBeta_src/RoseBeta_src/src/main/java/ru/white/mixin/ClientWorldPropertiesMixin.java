package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.white.module.impl.render.WorldTweaks;

@Environment(EnvType.CLIENT)
@Mixin({Properties.class})
public abstract class ClientWorldPropertiesMixin {
   @Shadow
   private long field_24438;
   @Shadow
   private long field_24439;

   @ModifyReturnValue(
      method = {"method_217"},
      at = {@At("RETURN")}
   )
   private long hookGetTime(long original) {
      return ml.get().isEnabled() && ml.get().times.getValue() ? (long)(ml.get().time.getValue() * 1000.0F) : this.field_24439;
   }
}
