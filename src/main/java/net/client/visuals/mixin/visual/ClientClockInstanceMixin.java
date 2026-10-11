package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.client.visuals.module.render.AmbienceModule;
import net.client.visuals.render.camera.WorldIntro;
import net.minecraft.client.ClientClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Ambience time: every timeline the client evaluates (sun angle, sky colours, daylight, moon phase)
 * samples its world clock through these two getters. The day count is kept, only the time within
 * the day is replaced, and the partial tick is frozen so the sky holds still.
 * {@link AmbienceModule#dayTicks()} returns -1 (no change) unless the override is on.
 */
@Mixin(ClientClockManager.ClientClockInstance.class)
public abstract class ClientClockInstanceMixin {
	@ModifyReturnValue(method = "totalTicks", at = @At("RETURN"))
	private long liquid$customTime(long original) {
		WorldIntro.observeClock(original); // the title card's day count and time of day
		long day = AmbienceModule.dayTicks();
		return day < 0L ? original : Math.floorDiv(original, 24000L) * 24000L + day;
	}

	@ModifyReturnValue(method = "partialTick", at = @At("RETURN"))
	private float liquid$freezeTime(float original) {
		return AmbienceModule.dayTicks() < 0L ? original : 0f;
	}
}
