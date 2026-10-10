package ru.white.mixin;

import java.util.EnumMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.white.utils.helper.Oa;
import ru.white.utils.helper.Pa;

@Environment(EnvType.CLIENT)
@Mixin({PlayerEntityRenderState.class})
public abstract class PlayerEntityRenderStateMixin implements pa {
   @Unique
   private EnumMap<oa, float[]> rose$emotePose;

   @Override
   public EnumMap<oa, float[]> rose$getEmotePose() {
      return this.rose$emotePose;
   }

   @Override
   public void rose$setEmotePose(EnumMap<oa, float[]> pose) {
      this.rose$emotePose = pose;
   }
}
