package net.client.visuals.mixin.access;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

/**
 * Read access to an option's caption, value formatter and tooltip, so the Liquid settings can draw
 * vanilla options with their own controls (labels and descriptions stay exactly vanilla's).
 */
@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {
	@Accessor("caption")
	Component liquid$caption();

	/** Formats a value as vanilla's button message ("Caption: Value"). */
	@Accessor("toString")
	Function<Object, Component> liquid$valueText();

	@Accessor("tooltip")
	OptionInstance.TooltipSupplier<Object> liquid$tooltip();
}
