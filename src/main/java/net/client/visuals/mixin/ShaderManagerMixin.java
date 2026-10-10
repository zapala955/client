package net.client.visuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.client.visuals.render.chunk.ChunkAnimator;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patches the terrain vertex shaders (Sodium's and vanilla's) as their sources are loaded, adding
 * Chunk Animations and Waving Plants, plus Sodium's vertex include (which unlocks the wave code).
 * Source-level, so it doesn't depend on resource-pack order and re-applies on every shader reload;
 * unknown layouts are left untouched (see {@code TerrainShaderPatch}).
 */
@Mixin(ShaderManager.class)
public abstract class ShaderManagerMixin {
	@WrapOperation(method = "loadShader", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/packs/resources/Resource;readAllAsString()Ljava/lang/String;"), require = 0)
	private static String liquid$patchTerrain(Resource resource, Operation<String> original, @Local(argsOnly = true) Identifier location) {
		return ChunkAnimator.patchShader(location, original.call(resource));
	}

	@WrapOperation(method = "loadInclude", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/packs/resources/Resource;readAllAsString()Ljava/lang/String;"), require = 0)
	private static String liquid$patchInclude(Resource resource, Operation<String> original, @Local(argsOnly = true) Identifier location) {
		return ChunkAnimator.patchInclude(location, original.call(resource));
	}
}
