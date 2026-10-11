package net.client.visuals.render.water;

import org.jspecify.annotations.Nullable;

/**
 * Source patches that let the terrain shaders (Sodium's and vanilla's) draw water as a little
 * G-buffer for {@link LiquidWater} – applied when the shader manager loads sources, like
 * {@code render.chunk.TerrainShaderPatch}, so they survive resource-pack order and F3+T.
 *
 * <p>Only the translucent terrain pipeline gets the {@code LIQUID_WATER} define (Sodium:
 * {@code SodiumShaderChunkRendererMixin}; vanilla: {@code RenderPipelinesMixin}), and only its
 * classic (non-OIT) variant writes anything different. There, a water fragment</p>
 * <ul>
 *   <li>writes its lit tint (biome water colour × light × 0.6) as an opaque colour, and</li>
 *   <li>writes its depth with the six lowest bits replaced: {@code 0b11} (water) and the sky light
 *       (0–15) above them – only ever made smaller (further, with reverse-Z), so Sodium's shader can
 *       declare {@code depth_less} and keep the early depth test (vanilla's GLSL 330 can't declare it,
 *       so its translucent pass runs the test late); the change is at most 63 ulp (micrometres).</li>
 * </ul>
 * Every other translucent fragment clears the two tag bits (at most 3 ulp), so glass, ice, stained
 * glass, slime and honey are never mistaken for water. Which fragments are water: on Sodium, quads
 * that {@code SodiumFluidRendererMixin} tagged with material bit 5 while meshing; on vanilla, the one
 * tinted translucent block – a vertex colour with real chroma (glass and ice are grey) – in sections
 * whose visibility arrives raised by 2 (the per-frame on switch, {@link WaterTag#vanillaVisibility}).
 *
 * <p>Each patch matches exact anchor lines and returns null if one is missing (the shader stays
 * stock, and so does the water). No Minecraft types, so the output can be compiled offline
 * ({@code tools/ShaderCheck.java}).</p>
 */
public final class WaterShaderPatch {
	private static final String MAIN = "void main() {";
	private static final String FINAL_COLOR = "    fragColor = calculateFinalColor(color);";

	// ---- Sodium: sodium:shaders/blocks/block_layer_opaque.{vsh,fsh} -----------------------------

	private static final String SODIUM_OUTPUTS_ANCHOR = "uniform isamplerBuffer u_SectionTimeInfo;";
	private static final String SODIUM_OUTPUTS = """
			// Liquid Water: whether this quad is water (material bit 5) and the sky light, for the fragment stage.
			layout(location = 4) flat out uint liquid_Water;
			layout(location = 5) out float liquid_Sky;

			""";
	private static final String SODIUM_INIT = "    _vert_init();";
	private static final String SODIUM_VERTEX = """
			    _vert_init();
			#ifdef LIQUID_SODIUM_MATERIAL
			    liquid_Water = (_material_params >> 5u) & 1u;
			#else
			    liquid_Water = 0u;
			#endif
			    liquid_Sky = _vert_tex_light_coord.y;""";
	private static final String SODIUM_INPUTS_ANCHOR = "layout(location = 3) in float fadeFactor;";
	private static final String SODIUM_INPUTS = """

			#if defined(LIQUID_WATER) && !defined(OIT_ALPHA_ONLY) && !defined(OIT_ACCUMULATE)
			#define LIQUID_WATER_TAG 1
			layout(location = 4) flat in uint liquid_Water;
			layout(location = 5) in float liquid_Sky;
			layout(depth_less) out float gl_FragDepth;
			#endif""";
	/** Sodium's light coordinate is (level · 16 + 8) / 256. */
	private static final String SODIUM_SKY = "uint(clamp(floor((liquid_Sky * 256.0 - 8.0) / 16.0 + 0.5), 0.0, 15.0))";

	// ---- Vanilla: minecraft:shaders/core/terrain.{vsh,fsh} -----------------------------------

	private static final String VANILLA_OUTPUTS_ANCHOR = "layout(location = 4) out float chunkVisibility;";
	private static final String VANILLA_OUTPUTS = """

			// Liquid Water: whether this is water (a tinted vertex in a tagged section) and the sky light.
			layout(location = 5) flat out uint liquid_Water;
			layout(location = 6) out float liquid_Sky;""";
	/**
	 * Right at the start of main: the section's visibility arrives raised by 2 while water is tagged
	 * (vanilla values are 0..1, Chunk Animations' are ≤ -1); the rest of main sees the plain value
	 * through the macro.
	 */
	private static final String VANILLA_VERTEX = """

			    float liquidChunkVisibility = ChunkVisibility;
			    bool liquidWaterGate = liquidChunkVisibility > 1.5;
			    if (liquidWaterGate) {
			        liquidChunkVisibility -= 2.0;
			    }
			#define ChunkVisibility liquidChunkVisibility
			    // Water is the one tinted translucent block: its vertex colour has real chroma (glass and ice are grey).
			    float liquidMax = max(Color.r, max(Color.g, Color.b));
			    float liquidChroma = liquidMax > 0.02 ? (liquidMax - min(Color.r, min(Color.g, Color.b))) / liquidMax : 0.0;
			    liquid_Water = liquidWaterGate && liquidChroma > 0.08 ? 1u : 0u;
			    liquid_Sky = float(UV2.y) / 240.0;""";
	private static final String VANILLA_INPUTS_ANCHOR = "layout(location = 4) in float chunkVisibility;";
	private static final String VANILLA_INPUTS = """

			#if defined(LIQUID_WATER) && !defined(OIT_ALPHA_ONLY) && !defined(OIT_ACCUMULATE)
			#define LIQUID_WATER_TAG 1
			layout(location = 5) flat in uint liquid_Water;
			layout(location = 6) in float liquid_Sky;
			#endif""";
	/** Vanilla's sky light coordinate is 0..240 in steps of 16, passed on / 240. */
	private static final String VANILLA_SKY = "uint(clamp(floor(liquid_Sky * 15.0 + 0.5), 0.0, 15.0))";

	private WaterShaderPatch() {
	}

	/** The tag written in place of the plain colour: water → lit tint + tagged depth; anything else → tag bits cleared. */
	private static String tagBlock(String litColor, String sky) {
		return """
				    #ifdef LIQUID_WATER_TAG
				    uint liquidDepth = floatBitsToUint(gl_FragCoord.z);
				    if (liquid_Water != 0u) {
				        // Liquid Water: the lit tint as a G-buffer; the tag (0b11) and sky light in the depth's lowest bits.
				        fragColor = vec4(%s.rgb * 0.6, 1.0);
				        uint liquidTagged = (liquidDepth & ~63u) | 3u | (%s << 2u);
				        gl_FragDepth = uintBitsToFloat(liquidTagged > liquidDepth ? liquidTagged - 64u : liquidTagged);
				    } else {
				        fragColor = calculateFinalColor(color);
				        gl_FragDepth = uintBitsToFloat(liquidDepth & ~3u);
				    }
				    #else
				    fragColor = calculateFinalColor(color);
				    #endif""".formatted(litColor, sky);
	}

	public static @Nullable String patchSodiumVertex(String source) {
		String s = normalize(source);
		if (!s.contains(SODIUM_OUTPUTS_ANCHOR) || !s.contains(SODIUM_INIT) || !s.contains("_vert_tex_light_coord") || s.contains("liquid_Water")) {
			return null;
		}
		s = s.replace(SODIUM_OUTPUTS_ANCHOR, SODIUM_OUTPUTS + SODIUM_OUTPUTS_ANCHOR);
		return s.replace(SODIUM_INIT, SODIUM_VERTEX);
	}

	public static @Nullable String patchSodiumFragment(String source) {
		String s = normalize(source);
		if (!s.contains(SODIUM_INPUTS_ANCHOR) || count(s, FINAL_COLOR) != 1 || !s.contains("v_Color") || s.contains("liquid_Water")) {
			return null;
		}
		s = s.replace(SODIUM_INPUTS_ANCHOR, SODIUM_INPUTS_ANCHOR + SODIUM_INPUTS);
		return s.replace(FINAL_COLOR, tagBlock("v_Color", SODIUM_SKY));
	}

	public static @Nullable String patchVanillaVertex(String source) {
		String s = normalize(source);
		if (count(s, MAIN) != 1 || !s.contains(VANILLA_OUTPUTS_ANCHOR) || !s.contains("ChunkVisibility") || !s.contains("UV2")
				|| !s.contains("Color") || s.contains("liquid_Water")) {
			return null;
		}
		s = s.replace(VANILLA_OUTPUTS_ANCHOR, VANILLA_OUTPUTS_ANCHOR + VANILLA_OUTPUTS);
		return s.replace(MAIN, MAIN + VANILLA_VERTEX);
	}

	public static @Nullable String patchVanillaFragment(String source) {
		String s = normalize(source);
		if (!s.contains(VANILLA_INPUTS_ANCHOR) || count(s, FINAL_COLOR) != 1 || !s.contains("vertexColor") || s.contains("liquid_Water")) {
			return null;
		}
		s = s.replace(VANILLA_INPUTS_ANCHOR, VANILLA_INPUTS_ANCHOR + VANILLA_INPUTS);
		return s.replace(FINAL_COLOR, tagBlock("vertexColor", VANILLA_SKY));
	}

	private static int count(String s, String what) {
		int n = 0;
		for (int i = s.indexOf(what); i >= 0; i = s.indexOf(what, i + what.length())) {
			n++;
		}
		return n;
	}

	private static String normalize(String source) {
		return source.replace("\r\n", "\n");
	}
}
