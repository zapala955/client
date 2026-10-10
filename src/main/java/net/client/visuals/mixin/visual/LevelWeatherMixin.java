package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.module.render.AmbienceModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Ambience weather: the rain / thunder levels the client sees (rain rendering, sounds, sky
 * darkening and the weather environment layers all read these). Server levels of the integrated
 * server are untouched.
 */
@Mixin(Level.class)
public abstract class LevelWeatherMixin {
	@ModifyReturnValue(method = "getRainLevel", at = @At("RETURN"))
	private float liquid$rain(float original) {
		return (Object) this instanceof ClientLevel ? AmbienceModule.rainLevel(original) : original;
	}

	@ModifyReturnValue(method = "getThunderLevel", at = @At("RETURN"))
	private float liquid$thunder(float original) {
		return (Object) this instanceof ClientLevel ? AmbienceModule.thunderLevel(original) : original;
	}
}
