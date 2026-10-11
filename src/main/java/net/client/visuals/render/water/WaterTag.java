package net.client.visuals.render.water;

import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;

/**
 * Whether – and how – the terrain shaders tag water for {@link LiquidWater} this frame.
 *
 * <p>Tagging takes three things, all checked here, so water is never left in its G-buffer form
 * without the pass that shades it:</p>
 * <ol>
 *   <li>the shader sources patched ({@link #patchShader}, from {@code ShaderManagerMixin});</li>
 *   <li>the translucent terrain pipeline built with the {@code LIQUID_WATER} define (Sodium:
 *       {@code SodiumShaderChunkRendererMixin}; vanilla: {@code RenderPipelinesMixin});</li>
 *   <li>the switch: on Sodium, material bit 5 on water quads, written while chunks are meshed
 *       ({@code SodiumFluidRendererMixin} marks the meshing thread inside water,
 *       {@code SodiumMeshBufferMixin} adds the bit) – so switching re-meshes; on vanilla, each
 *       section's visibility raised by 2 every frame ({@link #vanillaVisibility}).</li>
 * </ol>
 * Plus the pass actually running: {@code PostFx#splitMainPass} reports in ({@link #passSeen}), which
 * only happens with classic transparency – with Improved Transparency the OIT pipelines never tag,
 * and water stays vanilla's.
 */
public final class WaterTag {
	/** Sodium material bit for water quads (Sodium uses bits 0–2, Waving Plants 3–4). */
	public static final int SODIUM_WATER_BIT = 1 << 5;
	/** The define that turns tagging on in a pipeline's shaders. */
	public static final String DEFINE = "LIQUID_WATER";

	/** Meshing threads: inside a water block's fluid render, while tagging is on. */
	private static final ThreadLocal<boolean[]> IN_WATER = ThreadLocal.withInitial(() -> new boolean[1]);

	private static final boolean SODIUM = FabricLoader.getInstance().isModLoaded("sodium");

	private static volatile boolean sodiumVertex;
	private static volatile boolean sodiumFragment;
	private static volatile boolean sodiumPipeline;
	private static volatile boolean vanillaVertex;
	private static volatile boolean vanillaFragment;
	private static volatile boolean vanillaPipeline;
	/** Sodium: quads are being meshed with the water bit. */
	private static volatile boolean meshTagging;
	/** Sodium: when tagging was switched off – meshes keep the bit until they're rebuilt, so shading goes on a while. */
	private static long offNanos;
	/** How long tagged meshes may linger after switching off (re-meshing everything takes a moment). */
	private static final long LINGER_NANOS = 6_000_000_000L;
	/** Vanilla: sections are being drawn with the raised visibility (set per frame). */
	private static boolean vanillaGate;
	private static boolean visibilitySeen;
	private static boolean passSeen;

	private WaterTag() {
	}

	// ---- Shader sources (shader-reload thread) --------------------------------------------------

	/** Every shader source the shader manager loads, after Chunk Animations' patch: adds the water tag to the terrain shaders. */
	public static String patchShader(Identifier location, String contents) {
		String namespace = location.getNamespace();
		String path = location.getPath();
		if ("sodium".equals(namespace) && "shaders/blocks/block_layer_opaque.vsh".equals(path)) {
			String patched = WaterShaderPatch.patchSodiumVertex(contents);
			sodiumVertex = patched != null;
			log("Sodium vertex", patched != null);
			return patched != null ? patched : contents;
		}
		if ("sodium".equals(namespace) && "shaders/blocks/block_layer_opaque.fsh".equals(path)) {
			String patched = WaterShaderPatch.patchSodiumFragment(contents);
			sodiumFragment = patched != null;
			log("Sodium fragment", patched != null);
			return patched != null ? patched : contents;
		}
		if ("minecraft".equals(namespace) && "shaders/core/terrain.vsh".equals(path)) {
			String patched = WaterShaderPatch.patchVanillaVertex(contents);
			vanillaVertex = patched != null;
			log("vanilla vertex", patched != null);
			return patched != null ? patched : contents;
		}
		if ("minecraft".equals(namespace) && "shaders/core/terrain.fsh".equals(path)) {
			String patched = WaterShaderPatch.patchVanillaFragment(contents);
			vanillaFragment = patched != null;
			log("vanilla fragment", patched != null);
			return patched != null ? patched : contents;
		}
		return contents;
	}

	private static void log(String shader, boolean ok) {
		if (ok) {
			LiquidClient.LOGGER.info("Liquid Water: patched the {} terrain shader", shader);
		} else {
			LiquidClient.LOGGER.warn("Liquid Water: the {} terrain shader has an unexpected layout – water stays vanilla's", shader);
		}
	}

	// ---- Pipelines (class init / render thread) -------------------------------------------------

	/** Both of Sodium's terrain shaders carry the patch (checked before the pipeline gets the define). */
	public static boolean sodiumShadersPatched() {
		return sodiumVertex && sodiumFragment;
	}

	/** Sodium built its translucent terrain pipeline with the define. */
	public static void sodiumPipelineDefined() {
		if (!sodiumPipeline) {
			sodiumPipeline = true;
			LiquidClient.LOGGER.info("Liquid Water: Sodium's translucent terrain pipeline tags water");
		}
	}

	/** Vanilla's translucent terrain pipeline was built with the define (from RenderPipelines' static init: no logging there). */
	public static void vanillaPipelineDefined() {
		vanillaPipeline = true;
	}

	// ---- Sodium meshing (meshing threads) ---------------------------------------------------------

	/** Start of a fluid block's render: marks the thread while it meshes water (and tagging is on). */
	public static void beginFluid(FluidState fluid) {
		IN_WATER.get()[0] = meshTagging && fluid.is(FluidTags.WATER);
	}

	public static void endFluid() {
		IN_WATER.get()[0] = false;
	}

	/** Extra material bits for the quad being pushed. */
	public static int fluidBits() {
		return IN_WATER.get()[0] ? SODIUM_WATER_BIT : 0;
	}

	// ---- Vanilla sections (render thread) ---------------------------------------------------------

	/**
	 * A section's {@code ChunkVisibility}: raised by 2 while water is tagged. Only plain values
	 * (0..1) are raised – a section still in Chunk Animations' packed state (≤ -1) isn't tagged for
	 * that moment and simply shows vanilla water.
	 */
	public static float vanillaVisibility(float value) {
		visibilitySeen = true;
		return vanillaGate && value >= 0f && value <= 1.0001f ? value + 2f : value;
	}

	// ---- Per tick / frame -------------------------------------------------------------------------

	/** The shading pass ran (the classic-transparency hook is in place). */
	public static void passSeen() {
		passSeen = true;
	}

	/**
	 * Client tick: follows whether Liquid Water wants tagging. On Sodium a change re-meshes the
	 * chunks (the bit lives in the meshes); vanilla follows within the frame.
	 */
	public static void tick(Minecraft minecraft, boolean wanted) {
		if (SODIUM) {
			boolean mesh = wanted && sodiumVertex && sodiumFragment && sodiumPipeline && passSeen;
			if (mesh != meshTagging) {
				meshTagging = mesh;
				if (!mesh) {
					offNanos = System.nanoTime();
				}
				if (minecraft.level != null && minecraft.levelExtractor != null) {
					minecraft.levelExtractor.allChanged();
				}
			}
		} else {
			vanillaGate = wanted && vanillaVertex && vanillaFragment && vanillaPipeline && passSeen;
		}
	}

	/**
	 * Whether water drawn this frame may arrive tagged (and so must be shaded): on Sodium also for a
	 * few seconds after switching off, while the meshes are rebuilt without the bit.
	 */
	public static boolean active() {
		if (SODIUM) {
			return sodiumPipeline && sodiumFragment && (meshTagging || offNanos != 0L && System.nanoTime() - offNanos < LINGER_NANOS);
		}
		return vanillaGate && visibilitySeen;
	}

	/** Whether this frame's water will be tagged on purpose (not just lingering). */
	public static boolean tagging() {
		return SODIUM ? meshTagging : vanillaGate;
	}

	/** Forget the per-session state that depends on the world (on disconnect). */
	public static void reset() {
		vanillaGate = false;
	}
}
