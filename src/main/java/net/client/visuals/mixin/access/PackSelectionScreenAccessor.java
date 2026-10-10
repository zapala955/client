package net.client.visuals.mixin.access;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

/**
 * What the Liquid pack window needs from a vanilla pack screen it replaces: its model (repository
 * and output), its folder, and a way to stop the folder watcher the constructor already started.
 */
@Mixin(PackSelectionScreen.class)
public interface PackSelectionScreenAccessor {
	@Accessor("model")
	PackSelectionModel liquid$model();

	@Accessor("packDir")
	Path liquid$packDir();

	@Invoker("closeWatcher")
	void liquid$closeWatcher();
}
