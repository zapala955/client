package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventSync;

@Environment(EnvType.CLIENT)
@Mixin({ClientWorld.class})
public class ClientWorldMixin implements IMinecraft {
   @Shadow
   @Final
   private Properties field_24430;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   public void initHook(CallbackInfo info) {
      new EventSync().hook();
   }
}
