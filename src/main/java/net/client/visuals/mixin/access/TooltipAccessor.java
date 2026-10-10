package net.client.visuals.mixin.access;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The text of a vanilla tooltip, shown as the description of a setting. */
@Mixin(Tooltip.class)
public interface TooltipAccessor {
	@Accessor("message")
	Component liquid$message();
}
