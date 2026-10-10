package net.client.visuals.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.LiquidClient;
import net.minecraft.client.renderer.oit.OitPipelineSet;

/**
 * Decides which custom pipelines can be used.
 *
 * <p>Custom pipelines are compiled lazily from the current resource set. If compilation fails
 * (broken resource pack, driver bug, unsupported backend) the GUI renderer would throw mid-frame,
 * so we probe – outside the render pass – and fall back when it fails:</p>
 * <ul>
 *   <li>SDF primitives unavailable → vanilla fills (chamfered corners, layered shadows)</li>
 *   <li>backdrop shaders unavailable → vanilla panorama + engine blur</li>
 *   <li>particle shader unavailable → vanilla particle pipeline with vanilla sprites</li>
 *   <li>a post-processing shader unavailable → that effect (haze, bloom, grading) is skipped</li>
 *   <li>weather shader unavailable → vanilla rain and snow</li>
 * </ul>
 *
 * <p>The probe re-runs on every screen {@code init()} (cheap: a map lookup once compiled), which
 * also covers resource reloads that swap the pipeline cache. Compiled pipelines are owned by the
 * engine's pipeline cache – mods must never close them.</p>
 */
public final class ShaderSupport {
	private static boolean sdf;
	private static boolean backdrops;
	/** Each live wallpaper is checked on its own, so one failing shader only drops that one. */
	private static final java.util.Map<RenderPipeline, Boolean> LIVE = new java.util.HashMap<>();
	private static boolean warned;
	private static boolean validated;
	private static boolean glassText;
	private static boolean hudBlur;
	private static boolean island;
	private static boolean islandGlass;
	private static boolean particles;
	private static boolean haze;
	private static boolean hazeTranslucent;
	private static boolean bloom;
	private static boolean grade;
	private static boolean weather;
	private static boolean rays;
	private static boolean lens;
	private static boolean rainbow;
	private static boolean wet;
	private static boolean handChams;
	private static boolean motion;
	private static boolean ao;
	private static boolean dof;

	private ShaderSupport() {
	}

	/** Render thread, outside a frame (e.g. {@code Screen#init}). */
	public static void revalidate() {
		validated = true;
		sdf = compiles(LiquidPipelines.ROUNDED_RECT) && compiles(LiquidPipelines.ROUNDED_RECT_ADDITIVE);
		backdrops = sdf && compiles(LiquidPipelines.BACKDROP_AURORA) && compiles(LiquidPipelines.BACKDROP_IMAGE);
		LIVE.clear();
		for (RenderPipeline live : new RenderPipeline[]{LiquidPipelines.BACKDROP_AURORA, LiquidPipelines.BACKDROP_NEBULA,
				LiquidPipelines.BACKDROP_SILK, LiquidPipelines.BACKDROP_GLOW, LiquidPipelines.BACKDROP_LIQUID,
				LiquidPipelines.BACKDROP_RIBBONS, LiquidPipelines.BACKDROP_DUNES}) {
			LIVE.put(live, backdrops && compiles(live));
		}
		glassText = sdf && compiles(LiquidPipelines.GLASS_TEXT);
		hudBlur = sdf && compiles(LiquidPipelines.HUD_BLUR);
		island = sdf && compiles(LiquidPipelines.ISLAND) && compiles(LiquidPipelines.ISLAND_SHADOW);
		islandGlass = island && compiles(LiquidPipelines.ISLAND_GLASS);
		particles = compiles(LiquidPipelines.PARTICLE_GLOW) && compiles(LiquidPipelines.PARTICLE_SOFT)
				&& compiles(LiquidPipelines.OIT_PARTICLE_GLOW) && compiles(LiquidPipelines.OIT_PARTICLE_SOFT);
		haze = compiles(LiquidPipelines.POST_HAZE);
		hazeTranslucent = haze && compiles(LiquidPipelines.POST_HAZE_TRANSLUCENT);
		grade = compiles(LiquidPipelines.POST_GRADE);
		weather = compiles(LiquidPipelines.WEATHER) && compiles(LiquidPipelines.OIT_WEATHER);
		rays = compiles(LiquidPipelines.POST_RAYS_MASK) && compiles(LiquidPipelines.POST_RAYS_BLUR_FINE)
				&& compiles(LiquidPipelines.POST_RAYS_BLUR_COARSE) && compiles(LiquidPipelines.POST_RAYS_COMPOSITE);
		lens = compiles(LiquidPipelines.POST_LENS);
		rainbow = compiles(LiquidPipelines.POST_RAINBOW);
		wet = compiles(LiquidPipelines.POST_WET);
		handChams = compiles(LiquidPipelines.HAND_CHAMS) && compiles(LiquidPipelines.OIT_HAND_CHAMS);
		motion = compiles(LiquidPipelines.POST_MOTION);
		ao = compiles(LiquidPipelines.POST_AO) && compiles(LiquidPipelines.POST_AO_BLUR_H) && compiles(LiquidPipelines.POST_AO_BLUR_V)
				&& compiles(LiquidPipelines.POST_AO_APPLY) && compiles(LiquidPipelines.POST_AO_PREVIEW);
		dof = compiles(LiquidPipelines.POST_DOF_FOCUS) && compiles(LiquidPipelines.POST_DOF_PREPARE) && compiles(LiquidPipelines.POST_DOF_TILE)
				&& compiles(LiquidPipelines.POST_DOF_DILATE) && compiles(LiquidPipelines.POST_DOF_PREFILTER)
				&& compiles(LiquidPipelines.POST_DOF_GATHER) && compiles(LiquidPipelines.POST_DOF_FILTER)
				&& compiles(LiquidPipelines.POST_DOF_COMPOSITE);
		bloom = grade && compiles(LiquidPipelines.POST_BLOOM_PREFILTER) && compiles(LiquidPipelines.POST_BLOOM_DOWNSAMPLE)
				&& compiles(LiquidPipelines.POST_BLOOM_UPSAMPLE);
		if ((!sdf || !backdrops) && !warned) {
			warned = true;
			LiquidClient.LOGGER.warn("Liquid Glass shaders partially unavailable (sdf={}, backdrops={}) – using fallbacks", sdf, backdrops);
		}
	}

	/** Whether this live wallpaper's pipeline compiled (checked in {@link #revalidate()}). */
	public static boolean liveWallpaper(RenderPipeline pipeline) {
		return LIVE.getOrDefault(pipeline, false);
	}

	private static boolean compiles(OitPipelineSet set) {
		return compiles(set.depthBoundsPipeline()) && compiles(set.transmittancePipeline()) && compiles(set.accumulatePipeline());
	}

	private static boolean compiles(RenderPipeline pipeline) {
		try {
			CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
			return compiled != null && !compiled.isClosed();
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.error("Failed to compile {}", pipeline.getLocation(), e);
			return false;
		}
	}

	/**
	 * Probes once if no Liquid screen has done it yet – e.g. quick-play straight into a world, where
	 * HUD elements are the first thing we draw.
	 */
	public static void ensureValidated() {
		if (!validated) {
			revalidate();
		}
	}

	/** SDF rounded rectangles can be drawn. */
	public static boolean sdf() {
		return sdf;
	}

	/** The Dynamic Island's melted shape (fill and shadow) can be drawn. */
	public static boolean island() {
		return island;
	}

	/** ...and its Liquid Glass variant. */
	public static boolean islandGlass() {
		return islandGlass;
	}

	/** The glass-text (clock) shader can be used. */
	public static boolean glassText() {
		return glassText;
	}

	/** The ambient-particle shader (all its blend and OIT variants) can be used. */
	public static boolean particles() {
		return particles;
	}

	/** The Atmosphere haze pass can be drawn. */
	public static boolean haze() {
		return haze;
	}

	/** The translucent-only haze pass can be drawn (needed to haze before translucents). */
	public static boolean hazeTranslucent() {
		return hazeTranslucent;
	}

	/** The grade / composite pass can be drawn (Color Grading, and Bloom's composite). */
	public static boolean grade() {
		return grade;
	}

	/** The bloom mip-chain passes (and the composite) can be drawn. */
	public static boolean bloom() {
		return bloom;
	}

	/** Liquid Weather (rain, snow, splashes and every OIT variant) can be drawn. */
	public static boolean weather() {
		return weather;
	}

	/** Sun Rays (mask, both blurs and the composite) can be drawn. */
	public static boolean rays() {
		return rays;
	}

	/** Wet Lens can be drawn. */
	public static boolean lens() {
		return lens;
	}

	/** Rainbows can be drawn. */
	public static boolean rainbow() {
		return rainbow;
	}

	/** Wet World can be drawn. */
	public static boolean wet() {
		return wet;
	}

	/** Hand Chams can be drawn. */
	public static boolean handChams() {
		return handChams;
	}

	/** Motion Blur can be drawn. */
	public static boolean motion() {
		return motion;
	}

	/** Ambient Occlusion (every pass) can be drawn. */
	public static boolean ao() {
		return ao;
	}

	/** Depth of Field (every pass) can be drawn. */
	public static boolean dof() {
		return dof;
	}

	/** HUD panels can blur the game behind them. */
	public static boolean hudBlur() {
		return hudBlur;
	}

	/** Procedural / image wallpapers can be drawn. */
	public static boolean backdrops() {
		return backdrops;
	}
}
