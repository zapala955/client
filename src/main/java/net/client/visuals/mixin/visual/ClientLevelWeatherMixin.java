package net.client.visuals.mixin.visual;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.client.visuals.module.render.AmbienceModule;
import net.client.visuals.render.weather.LiquidWeather;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Ambience precipitation on the client:
 * <ul>
 *   <li>{@code getPrecipitationAt} – only weather visuals and weather sounds ask it (vanilla's and
 *       Liquid Weather's), so "Always Rain" / "Always Snow" change what falls (and its sound)
 *       everywhere, deserts included, without touching gameplay.</li>
 *   <li>{@code tickWeatherEffects} – vanilla's rain splash particles are skipped while Liquid
 *       Weather draws its own splashes (smoke on lava and campfires stays).</li>
 * </ul>
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelWeatherMixin {
	@ModifyReturnValue(method = "getPrecipitationAt", at = @At("RETURN"))
	private Biome.Precipitation liquid$precipitation(Biome.Precipitation original, @Local(argsOnly = true) BlockPos pos) {
		Biome.Precipitation forced = AmbienceModule.forcedPrecipitation();
		if (forced == null) {
			return original;
		}
		ClientLevel level = (ClientLevel) (Object) this;
		return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())) ? forced : original;
	}

	@WrapWithCondition(method = "tickWeatherEffects", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private boolean liquid$rainSplash(ClientLevel level, ParticleOptions options, double x, double y, double z, double dx, double dy, double dz) {
		return options != ParticleTypes.RAIN || !LiquidWeather.drawsSplashes();
	}
}
