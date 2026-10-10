package net.client.visuals.mixin.access;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

/** The repository and result callback a pack screen was opened with. */
@Mixin(PackSelectionModel.class)
public interface PackSelectionModelAccessor {
	@Accessor("repository")
	PackRepository liquid$repository();

	@Accessor("output")
	Consumer<PackRepository> liquid$output();
}
