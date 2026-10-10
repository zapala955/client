package net.client.visuals.mixin.access;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Where the replaced Multiplayer screen would have returned to. */
@Mixin(JoinMultiplayerScreen.class)
public interface JoinMultiplayerScreenAccessor {
	@Accessor("lastScreen")
	Screen liquid$lastScreen();
}
