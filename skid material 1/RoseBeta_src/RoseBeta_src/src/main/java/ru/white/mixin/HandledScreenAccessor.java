package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({HandledScreen.class})
public interface HandledScreenAccessor {
   @Accessor("field_2776")
   int getScreenX();

   @Accessor("field_2800")
   int getScreenY();
}
