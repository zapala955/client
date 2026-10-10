package net.client.visuals.render.shader;

import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Whether an Iris shader pack is active. Iris then replaces the world's shaders with the pack's,
 * which know neither our terrain encoding (chunk animations) nor our world pipelines (particles),
 * so those features step aside or fall back to vanilla pipelines. Queried through Iris' public API by
 * reflection, so Iris stays optional.
 */
public final class ShaderPacks {
	private static boolean checked;
	private static @Nullable Object api;
	private static @Nullable Method inUse;

	private ShaderPacks() {
	}

	/** Client / render thread. */
	public static boolean inUse() {
		if (!checked) {
			checked = true;
			if (FabricLoader.getInstance().isModLoaded("iris")) {
				try {
					Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
					api = type.getMethod("getInstance").invoke(null);
					inUse = type.getMethod("isShaderPackInUse");
				} catch (ReflectiveOperationException | LinkageError e) {
					LiquidClient.LOGGER.warn("Couldn't query Iris, assuming no shader pack", e);
				}
			}
		}
		if (api == null || inUse == null) {
			return false;
		}
		try {
			return (boolean) inUse.invoke(api);
		} catch (ReflectiveOperationException | ClassCastException e) {
			return false;
		}
	}
}
