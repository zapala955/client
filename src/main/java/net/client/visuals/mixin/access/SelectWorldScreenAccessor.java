package net.client.visuals.mixin.access;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Where the replaced Singleplayer screen would have returned to. */
@Mixin(SelectWorldScreen.class)
public interface SelectWorldScreenAccessor {
	@Accessor("lastScreen")
	Screen liquid$lastScreen();
}
