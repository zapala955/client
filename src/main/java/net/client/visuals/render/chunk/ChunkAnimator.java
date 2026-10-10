package net.client.visuals.render.chunk;

import net.client.visuals.LiquidClient;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.ChunkAnimationsModule;
import net.client.visuals.render.shader.ShaderPacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Drives {@link ChunkAnimationsModule}: the animation itself runs in the terrain vertex shader
 * ({@code chunk_animation.glsl}), this class only gets the inputs there without adding any GPU
 * resources or new uniforms.
 *
 * <h2>Sodium (the usual case)</h2>
 * Sodium already keeps a per-section "first meshed" timestamp for its fade-in and passes the current
 * time to the shader. The settings are packed into the one float we can safely repurpose – Sodium's
 * {@code u_FadePeriodInv} – as raw bits, shared with Waving Plants' wind (see
 * {@link #sodiumFadeUniform}); the patched shader recognises the marker bits and computes progress
 * from the timestamps.
 *
 * <h2>Vanilla</h2>
 * Each section's {@code ChunkVisibility} (normally its fade-in in [0, 1]) carries
 * {@code -(1 + bits)} including the section's own progress while it animates.
 *
 * <p>Nothing is encoded unless the matching shader patch actually applied, and nothing while an Iris
 * shader pack replaces the terrain shaders – a stock shader never sees these values.</p>
 */
public final class ChunkAnimator {
	/** Squared distance (blocks) inside which Sodium normally skips the fade – 16√3. */
	private static final double SODIUM_NEAR_SQ = 768.0;

	/** Bits 31 and 30: "Liquid data" (Sodium's own value is positive) and a normal float; bit 29 stays clear (never Inf / NaN). */
	private static final int SODIUM_MARKER = 0xC0000000;
	/** Animation mode meaning "no animation" (bits 5–20 then carry Sodium's own fade period). */
	private static final int SODIUM_NO_ANIMATION = 31;

	private static volatile boolean sodiumPatched;
	private static volatile boolean vanillaPatched;
	private static @Nullable ChunkAnimationsModule module;
	private static boolean enabled;
	private static boolean nearby;
	private static int sodiumBits;
	private static int vanillaBits;
	private static int durationMs = 900;

	private ChunkAnimator() {
	}

	// ---- Shader source patching (shader-reload thread) -----------------------------------------

	/** Called for every shader include the shader manager loads; enables the wave code in Sodium's vertex include. */
	public static String patchInclude(Identifier location, String contents) {
		if ("sodium".equals(location.getNamespace()) && "shaders/include/chunk_vertex.glsl".equals(location.getPath())) {
			String patched = TerrainShaderPatch.patchSodiumVertexInclude(contents);
			if (patched == null) {
				LiquidClient.LOGGER.warn("Waving plants: Sodium's vertex include has an unexpected layout, waving disabled");
			}
			return patched != null ? patched : contents;
		}
		return contents;
	}

	/** Called for every shader source the shader manager loads; patches the two terrain shaders. */
	public static String patchShader(Identifier location, String contents) {
		String path = location.getPath();
		if ("sodium".equals(location.getNamespace()) && "shaders/blocks/block_layer_opaque.vsh".equals(path)) {
			String patched = TerrainShaderPatch.patchSodium(contents);
			sodiumPatched = patched != null;
			log("Sodium", "chunk animations, waving plants", patched != null);
			return patched != null ? patched : contents;
		}
		if ("minecraft".equals(location.getNamespace()) && "shaders/core/terrain.vsh".equals(path)) {
			String patched = TerrainShaderPatch.patchVanilla(contents);
			vanillaPatched = patched != null;
			log("vanilla", "chunk animations", patched != null);
			return patched != null ? patched : contents;
		}
		return contents;
	}

	private static void log(String renderer, String features, boolean ok) {
		if (ok) {
			LiquidClient.LOGGER.info("Patched {} terrain shader ({})", renderer, features);
		} else {
			LiquidClient.LOGGER.warn("{} terrain shader has an unexpected layout, {} disabled for it", renderer, features);
		}
	}

	// ---- Settings (render / client thread) -----------------------------------------------------

	/** Re-reads the module once per frame / tick; cheap. */
	private static void refresh() {
		ChunkAnimationsModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(ChunkAnimationsModule.class);
		}
		enabled = m.isEnabled() && !ShaderPacks.inUse();
		if (!enabled) {
			return;
		}
		int mode = m.mode.index() & 31;
		int easing = m.easing.index() & 15;
		int fadeBit = m.fade.get() ? 1 : 0;
		int distance = (int) Math.round(m.distance.get());
		int tenths = Math.clamp(Math.round(m.duration.get() * 10.0), 1, 63);
		durationMs = tenths * 100;
		nearby = m.nearby.get();
		sodiumBits = mode | easing << 5 | tenths << 9 | Math.clamp(distance / 2, 1, 63) << 15 | fadeBit << 21;
		vanillaBits = mode | easing << 5 | Math.clamp(distance / 2, 1, 63) << 9 | fadeBit << 15;
	}

	public static void tick() {
		refresh();
	}

	// ---- Sodium hooks --------------------------------------------------------------------------

	/**
	 * Replaces Sodium's {@code u_FadePeriodInv} with packed bits while chunk animations or waving
	 * plants are on (layout in {@code TerrainShaderPatch}): the animation settings, or Sodium's own fade
	 * period when only the wind is needed, plus the wind from {@link FoliageWaving#sodiumBits}. Bits are
	 * written as a float's raw pattern and read back with {@code floatBitsToUint}; the marker keeps it a
	 * normal (never NaN / Inf / denormal) value, so nothing between here and the shader can alter it.
	 */
	public static float sodiumFadeUniform(float fadePeriodInverse) {
		refresh();
		if (!sodiumPatched) {
			return fadePeriodInverse;
		}
		boolean animate = enabled;
		int wind = FoliageWaving.sodiumBits();
		if (!animate && wind == 0) {
			return fadePeriodInverse;
		}
		int word = SODIUM_MARKER | wind << 22;
		if (animate) {
			word |= sodiumBits;
		} else {
			int period = fadePeriodInverse > 0f && Float.isFinite(fadePeriodInverse) ? Math.round(1f / fadePeriodInverse / 4f) : 0;
			word |= SODIUM_NO_ANIMATION | Math.clamp(period, 0, 0xFFFF) << 5;
		}
		return Float.intBitsToFloat(word);
	}

	/** Sodium skips the fade (and so our animation) inside this squared distance. */
	public static double sodiumNearDistanceSq(double distanceSq) {
		return enabled && sodiumPatched && nearby ? 0.0 : distanceSq;
	}

	// ---- Vanilla hook --------------------------------------------------------------------------

	/**
	 * Per-section {@code ChunkVisibility}: the vanilla value, or the packed animation state while the
	 * section is still animating.
	 */
	public static float vanillaVisibility(SectionRenderDispatcher.RenderSection section, long now, float vanilla) {
		if (!enabled || !vanillaPatched) {
			return vanilla;
		}
		float t = section.getVisibility(now, durationMs); // linear 0..1 since the first upload
		if (t >= 1f) {
			return vanilla;
		}
		if (!nearby) {
			BlockPos origin = section.getRenderOrigin();
			Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
			double dx = origin.getX() + 8 - camera.x;
			double dy = origin.getY() + 8 - camera.y;
			double dz = origin.getZ() + 8 - camera.z;
			if (dx * dx + dy * dy + dz * dz < SODIUM_NEAR_SQ) {
				return vanilla;
			}
		}
		int progress = Math.clamp(Math.round(t * 255f), 0, 254);
		return -(1f + (vanillaBits | progress << 16)); // < 2^24: exact as a float
	}
}
