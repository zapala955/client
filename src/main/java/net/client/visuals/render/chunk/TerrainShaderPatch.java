package net.client.visuals.render.chunk;

import org.jspecify.annotations.Nullable;

/**
 * Source patches for the terrain vertex shaders: Chunk Animations ({@code liquid_chunk_animate},
 * assets/liquidclient/shaders/include/chunk_animation.glsl) in Sodium's and vanilla's, and Waving
 * Plants ({@code liquid_foliage_wave}, foliage_wave.glsl) in Sodium's. Applied when the shader
 * manager loads sources, so they work regardless of resource-pack order and survive F3+T reloads.
 *
 * <p>Each patch matches exact anchor lines; if any anchor is missing (a Sodium / Minecraft update
 * changed the shader) the patch returns null, the original source is kept, and the animator stays
 * off for that renderer – it never writes encoded values a stock shader would misread.</p>
 *
 * <p>No Minecraft types, so the patched output can be validated offline with shaderc.</p>
 */
public final class TerrainShaderPatch {
	private static final String INCLUDE = "#include <liquidclient:chunk_animation.glsl>\n\n";
	private static final String SODIUM_INCLUDE = "#include <liquidclient:chunk_animation.glsl>\n#include <liquidclient:foliage_wave.glsl>\n\n";
	private static final String MAIN = "void main() {";

	// ---- Sodium: sodium:shaders/blocks/block_layer_opaque.vsh ---------------------------------

	private static final String SODIUM_PROJECT = "    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);";
	/**
	 * Both features share Sodium's {@code u_FadePeriodInv}, reinterpreted as raw bits (see
	 * {@code ChunkAnimator#sodiumFadeUniform}): bits 31 and 30 set mark our data (Sodium's own value is
	 * positive). Bits 0-4 animation mode (31 = none), then either the animation settings (5-8 easing,
	 * 9-14 duration in tenths, 15-20 distance / 2, 21 fade) or, without an animation, Sodium's own fade
	 * period in 4 ms units (5-20); bits 22-28 the wind. Waving is applied in every variant (depth-only
	 * passes must move vertices identically); the fade and animation only where fog exists. The wave
	 * code needs {@code _material_params} from Sodium's {@code chunk_vertex.glsl}, so it only compiles
	 * when {@link #patchSodiumVertexInclude} confirmed that include and defined LIQUID_SODIUM_MATERIAL.
	 */
	private static final String SODIUM_ANIMATE = """
			    uint liquidWord = floatBitsToUint(u_FadePeriodInv);
			    bool liquidPacked = (liquidWord >> 30u) == 3u;
			#ifdef LIQUID_SODIUM_MATERIAL
			    if (liquidPacked && ((liquidWord >> 22u) & 7u) != 0u) {
			        uint liquidWaveMode = (_material_params >> 3u) & 3u;
			        if (liquidWaveMode != 0u) {
			            position += liquid_foliage_wave(_vert_position + _get_draw_translation(_draw_id), int(liquidWaveMode),
			                    _vert_tex_diffuse_coord_bias.y > 0.0, liquidWord, float(u_CurrentTime) * 0.001);
			        }
			    }
			#endif
			#ifdef USE_FOG
			    if (liquidPacked) {
			        int liquidStart = texelFetch(u_SectionTimeInfo, int((u_RegionID * 256u) + _draw_id)).r;
			        int liquidMode = int(liquidWord & 31u);
			        if (liquidMode != 31) {
			            float liquidDuration = float(max(1u, (liquidWord >> 9u) & 63u)) * 100.0;
			            float liquidT = liquidStart < 0 ? 1.0 : float(u_CurrentTime - liquidStart) / liquidDuration;
			            float liquidVisibility = 1.0;
			            position = liquid_chunk_animate(position, translation, liquidT, liquidMode, int((liquidWord >> 5u) & 15u),
			                    float(((liquidWord >> 15u) & 63u) * 2u), ((liquidWord >> 21u) & 1u) == 1u, (u_RegionID * 256u) + _draw_id,
			                    liquidVisibility);
			            fadeFactor = liquidVisibility;
			        } else {
			            float liquidPeriod = float((liquidWord >> 5u) & 65535u) * 4.0;
			            fadeFactor = (liquidStart < 0 || liquidPeriod <= 0.0) ? 1.0
			                    : clamp(float(u_CurrentTime - liquidStart) / liquidPeriod, 0.0, 1.0);
			        }
			        v_FragDistance = getFragDistance(position);
			    }
			#endif
			""";

	// ---- Vanilla: minecraft:shaders/core/terrain.vsh ------------------------------------------

	private static final String VANILLA_POSITION = "    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;";
	private static final String VANILLA_VISIBILITY = "    chunkVisibility = mix(1.0, ChunkVisibility,";
	/**
	 * The per-section {@code ChunkVisibility} carries {@code -(1 + bits)} while a section animates
	 * (see {@code ChunkAnimator#vanillaVisibility}); vanilla values are always in [0, 1].
	 */
	private static final String VANILLA_ANIMATE = """
			    float liquidVisibility = 1.0;
			    bool liquidAnimated = ChunkVisibility < -0.5;
			    if (liquidAnimated) {
			        int liquidPacked = int(-ChunkVisibility + 0.5) - 1;
			        float liquidT = float((liquidPacked >> 16) & 255) / 255.0;
			        uvec3 liquidCell = uvec3(ChunkPosition);
			        uint liquidSeed = (liquidCell.x * 73856093u) ^ (liquidCell.y * 19349663u) ^ (liquidCell.z * 83492791u);
			        pos = liquid_chunk_animate(pos, vec3(ChunkPosition - CameraBlockPos) + CameraOffset, liquidT, liquidPacked & 31,
			                (liquidPacked >> 5) & 15, float(((liquidPacked >> 9) & 63) * 2), ((liquidPacked >> 15) & 1) == 1, liquidSeed,
			                liquidVisibility);
			    }
			""";

	/** Names the Sodium patch relies on; if any is gone, the shader stays stock. */
	private static final String[] SODIUM_ANCHORS = {
			"u_SectionTimeInfo", "u_CurrentTime", "u_RegionID", "u_FadePeriodInv", "_vert_position",
			"_vert_tex_diffuse_coord_bias", "_get_draw_translation", "vec3 translation", "fadeFactor", "v_FragDistance"
	};

	private TerrainShaderPatch() {
	}

	public static @Nullable String patchSodium(String source) {
		String s = normalize(source);
		if (!s.contains(MAIN) || !s.contains(SODIUM_PROJECT) || s.contains("liquid_chunk_animate")) {
			return null;
		}
		for (String anchor : SODIUM_ANCHORS) {
			if (!s.contains(anchor)) {
				return null;
			}
		}
		s = s.replaceFirst(java.util.regex.Pattern.quote(MAIN), java.util.regex.Matcher.quoteReplacement(SODIUM_INCLUDE + MAIN));
		return s.replace(SODIUM_PROJECT, SODIUM_ANIMATE + "\n" + SODIUM_PROJECT);
	}

	/**
	 * Sodium's {@code chunk_vertex.glsl}: defines LIQUID_SODIUM_MATERIAL when it still exposes the
	 * per-quad material byte as {@code uint _material_params} (read from the vertex), enabling the
	 * wave code. Includes load separately from (and in no fixed order with) the vertex shader, so this
	 * flag – not a Java-side check – is what keeps the patched shader compiling if Sodium changes.
	 */
	public static @Nullable String patchSodiumVertexInclude(String source) {
		String s = normalize(source);
		if (!s.contains("uint _material_params;") || !s.contains("_material_params = ") || s.contains("LIQUID_SODIUM_MATERIAL")) {
			return null;
		}
		return s + "\n\n#define LIQUID_SODIUM_MATERIAL 1\n";
	}

	public static @Nullable String patchVanilla(String source) {
		String s = normalize(source);
		if (!s.contains(MAIN) || !s.contains(VANILLA_POSITION) || !s.contains(VANILLA_VISIBILITY) || s.contains("liquid_chunk_animate")) {
			return null;
		}
		s = s.replaceFirst(java.util.regex.Pattern.quote(MAIN), java.util.regex.Matcher.quoteReplacement(INCLUDE + MAIN));
		s = s.replace(VANILLA_POSITION, VANILLA_POSITION + "\n" + VANILLA_ANIMATE);
		return s.replace(VANILLA_VISIBILITY, "    chunkVisibility = liquidAnimated ? liquidVisibility : mix(1.0, ChunkVisibility,");
	}

	private static String normalize(String source) {
		return source.replace("\r\n", "\n");
	}
}
