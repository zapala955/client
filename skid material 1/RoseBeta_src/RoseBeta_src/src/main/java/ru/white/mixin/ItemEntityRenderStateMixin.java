package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.white.utils.render.is;

@Environment(EnvType.CLIENT)
@Mixin({ItemEntityRenderState.class})
public abstract class ItemEntityRenderStateMixin implements is {
   @Unique
   private boolean onGround;

   @Override
   public boolean isOnGround() {
      return this.onGround;
   }

   @Override
   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
