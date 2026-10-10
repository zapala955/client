package net.client.visuals.mixin.access;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Photo Mode holds the player's movement input ({@code PhotoPlayerInputMixin}). */
@Mixin(ClientInput.class)
public interface ClientInputAccessor {
	@Accessor("moveVector")
	void liquid$setMoveVector(Vec2 value);
}
