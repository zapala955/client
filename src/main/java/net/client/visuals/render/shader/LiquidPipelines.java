package net.client.visuals.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.client.visuals.LiquidClient;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;

import java.util.Optional;

/**
 * Custom Blaze3D / RenderPearl pipelines.
 *
 * <p>Since 26.2 raw OpenGL calls are unsupported (the Vulkan backend may be active), so every
 * custom draw goes through a {@link RenderPipeline}. The GLSL is compiled to SPIR-V by the engine
 * and runs on both backends. <b>Vertex inputs are bound by name</b> (SPIR-V reflection), so shader
 * input names must match the attribute names below.</p>
 *
 * <p>{@code BufferBuilder} can only write a fixed set of semantics (Position, Color, UV0-3, Normal,
 * LineWidth), so per-primitive parameters are packed into those slots.</p>
 */
public final class LiquidPipelines {
	// ---- GUI: SDF rounded rectangles ----------------------------------------------------------

	/**
	 * <pre>
	 *  Position  RGB32_FLOAT  screen position (after the 2D pose)
	 *  Color     RGBA8_UNORM  colour (per vertex → free gradients)
	 *  UV0       RG32_FLOAT   fragment position relative to the rect centre
	 *  UV3       RG32_FLOAT   half width / half height
	 *  LineWidth R32_FLOAT    corner radius
	 *  UV2       RG16_SINT    stroke * 16 (negative = outer glow), softness * 16
	 * </pre>
	 */
	public static final VertexFormat ROUNDED_RECT_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV3", GpuFormat.RG32_FLOAT)
			.addAttribute("LineWidth", GpuFormat.R32_FLOAT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.build();

	/** Standard alpha-blended glass surfaces, borders and shadows. */
	public static final RenderPipeline ROUNDED_RECT = gui("pipeline/rounded_rect", "core/rounded_rect", ROUNDED_RECT_FORMAT)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	/** Additive (src-alpha, one): accent glows, specular sheen, ambient light. */
	public static final RenderPipeline ROUNDED_RECT_ADDITIVE = gui("pipeline/rounded_rect_additive", "core/rounded_rect", ROUNDED_RECT_FORMAT)
			.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
			.build();

	// ---- GUI: main-menu backdrops -------------------------------------------------------------

	/**
	 * Full-screen wallpapers and their frosted copies inside glass panels.
	 * <pre>
	 *  Position  RGB32_FLOAT  GUI position
	 *  Color     RGBA8_UNORM  rgb = tint, a = frost amount (0 crisp … 1 fully frosted)
	 *  UV0       RG32_FLOAT   position relative to the mask centre (GUI units)
	 *  UV3       RG32_FLOAT   procedural: (time, screen height) · image: texture uv
	 *  LineWidth R32_FLOAT    mask corner radius, &lt; 0 = no mask
	 *  UV1       RG16_SINT    mask half size * 8
	 *  UV2       RG16_SINT    mask centre * 8
	 * </pre>
	 */
	public static final VertexFormat BACKDROP_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV3", GpuFormat.RG32_FLOAT)
			.addAttribute("LineWidth", GpuFormat.R32_FLOAT)
			.addAttribute("UV1", GpuFormat.RG16_SINT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.build();

	public static final RenderPipeline BACKDROP_AURORA = gui("pipeline/backdrop_aurora", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("AURORA")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_NEBULA = gui("pipeline/backdrop_nebula", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("NEBULA")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_SILK = gui("pipeline/backdrop_silk", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("SILK")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_GLOW = gui("pipeline/backdrop_glow", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("GLOW")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_LIQUID = gui("pipeline/backdrop_liquid", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("LIQUID")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_RIBBONS = gui("pipeline/backdrop_ribbons", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("RIBBONS")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_DUNES = gui("pipeline/backdrop_dunes", "core/backdrop_procedural", BACKDROP_FORMAT)
			.withShaderDefine("DUNES")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline BACKDROP_IMAGE = gui("pipeline/backdrop_image", "core/backdrop_image", BACKDROP_FORMAT)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	// ---- GUI: glass text (lock-screen clock) ---------------------------------------------------

	/**
	 * <pre>
	 *  Position  RGB32_FLOAT  GUI position
	 *  Color     RGBA8_UNORM  rgb = tint, a = opacity
	 *  UV0       RG32_FLOAT   glyph-mask uv (Sampler1: r coverage, g bevel height, b shadow)
	 *  UV3       RG32_FLOAT   screen-aligned wallpaper uv (Sampler0: frosted wallpaper)
	 * </pre>
	 */
	public static final VertexFormat GLASS_TEXT_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV3", GpuFormat.RG32_FLOAT)
			.build();

	public static final RenderPipeline GLASS_TEXT = gui("pipeline/glass_text", "core/glass_text", GLASS_TEXT_FORMAT)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	// ---- GUI: HUD glass over the game ----------------------------------------------------------

	/**
	 * Rounded panels that blur a copy of the frame behind them ({@code BackdropCapture}).
	 * <pre>
	 *  Position  RGB32_FLOAT  GUI position (after the 2D pose)
	 *  Color     RGBA8_UNORM  rgb = tint, a = tint amount
	 *  UV0       RG32_FLOAT   position relative to the panel centre
	 *  UV3       RG32_FLOAT   half width / half height
	 *  LineWidth R32_FLOAT    corner radius
	 *  UV1       RG16_SINT    blur radius (device px) * 16, opacity * 10000
	 *  UV2       RG16_SINT    saturation * 1000, refraction * 1000 (negative = brightness lift)
	 * </pre>
	 */
	public static final VertexFormat HUD_BLUR_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV3", GpuFormat.RG32_FLOAT)
			.addAttribute("LineWidth", GpuFormat.R32_FLOAT)
			.addAttribute("UV1", GpuFormat.RG16_SINT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.build();

	public static final RenderPipeline HUD_BLUR = gui("pipeline/hud_blur", "core/hud_blur", HUD_BLUR_FORMAT)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	// ---- World: skybox ------------------------------------------------------------------------

	/** Per-frame sky parameters; layout mirrored by {@code SkyUniforms} and {@code liquid_sky.fsh}. */
	public static final BindGroupLayout SKY_UNIFORMS = BindGroupLayout.builder()
			.withUniform("LiquidSky", UniformType.UNIFORM_BUFFER)
			.build();

	public static final RenderPipeline SKY = sky("pipeline/sky").build();

	public static final RenderPipeline SKY_GALAXY = sky("pipeline/sky_galaxy")
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withShaderDefine("GALAXY_TEXTURE")
			.build();

	// ---- World: ambient particles -------------------------------------------------------------

	/**
	 * Particles module, drawn by vanilla's particle renderer through custom {@code SingleQuadParticle}
	 * layers ({@code AmbientParticle}). Vanilla's particle bind groups and vertex format; our shader
	 * ({@code ambient_particle}: distance-field shapes) with no depth write and no culling, since
	 * petals, leaves and confetti tumble in 3D. Additive light (src·a + dst) for glows.
	 */
	public static final RenderPipeline PARTICLE_GLOW = particle("pipeline/ambient_particle_glow")
			.withShaderDefine("ADDITIVE")
			.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
			.build();

	/** Alpha-blended particles (snow, petals, leaves, bubbles, dark motes). */
	public static final RenderPipeline PARTICLE_SOFT = particle("pipeline/ambient_particle_soft")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	/**
	 * Order-independent-transparency variants; the renderer requires them for translucent particle
	 * layers whenever OIT is active. The glow contributes light only, like vanilla's lightning.
	 */
	public static final OitPipelineSet OIT_PARTICLE_GLOW = OitPipelineSet.builder("liquidclient_ambient_particle_glow",
			oitParticle().withShaderDefine("ADDITIVE").withShaderDefine("OIT_ADDITIVE")).build();

	public static final OitPipelineSet OIT_PARTICLE_SOFT = OitPipelineSet.builder("liquidclient_ambient_particle_soft",
			oitParticle()).build();

	// ---- World: Liquid Weather (render.weather.LiquidWeather) ---------------------------------

	/**
	 * A static buffer of quads whose vertices carry only seeds; the shader places them.
	 * <pre>
	 *  Position  RGB32_FLOAT  seeds x, y, z in [0, 1)
	 *  Color     RGBA8_UNORM  corner x, corner y, layer / 255, seed w
	 * </pre>
	 */
	public static final VertexFormat WEATHER_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.build();

	/** Weather parameters, the per-column data around the camera and the lightmap. */
	public static final BindGroupLayout WEATHER_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidWeather", UniformType.UNIFORM_BUFFER)
			.withUniform("ColumnSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("Sampler2", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Rain, snow and splashes in place of vanilla's weather (same depth test, no depth write). */
	public static final RenderPipeline WEATHER = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
			.withLocation(LiquidClient.id("pipeline/liquid_weather"))
			.withVertexShader(LiquidClient.id("core/liquid_weather"))
			.withFragmentShader(LiquidClient.id("core/liquid_weather"))
			.withBindGroupLayout(WEATHER_INPUTS)
			.withVertexBinding(0, WEATHER_FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withCull(false)
			.build();

	/** The order-independent-transparency variants, used whenever vanilla's weather would use OIT. */
	public static final OitPipelineSet OIT_WEATHER = OitPipelineSet.builder("liquidclient_weather", RenderPipeline.builder()
			.withVertexShader(LiquidClient.id("core/liquid_weather"))
			.withFragmentShader(LiquidClient.id("core/liquid_weather"))
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.withBindGroupLayout(WEATHER_INPUTS)
			.withVertexBinding(0, WEATHER_FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)).build();

	// ---- World: post-processing (render.post.PostFx) ------------------------------------------

	/** Atmosphere: its parameters and the world's depth. */
	public static final BindGroupLayout HAZE_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidHaze", UniformType.UNIFORM_BUFFER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout BLOOM_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidBloom", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout GRADE_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidGrade", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("BloomSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Atmosphere's second pass: also the opaque world's depth, to haze only what was drawn after it. */
	public static final BindGroupLayout HAZE_TRANSLUCENT_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidHaze", UniformType.UNIFORM_BUFFER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("OpaqueDepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Haze and mist alpha-blended over the frame (the opaque world, or everything as a fallback). */
	public static final RenderPipeline POST_HAZE = post("pipeline/post_haze", "core/post_haze", HAZE_INPUTS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	/** Haze over translucents (water, glass, clouds) only, after the world. */
	public static final RenderPipeline POST_HAZE_TRANSLUCENT = post("pipeline/post_haze_translucent", "core/post_haze", HAZE_TRANSLUCENT_INPUTS)
			.withShaderDefine("TRANSLUCENT_ONLY")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final RenderPipeline POST_BLOOM_PREFILTER = post("pipeline/post_bloom_prefilter", "core/post_bloom", BLOOM_INPUTS)
			.withShaderDefine("PREFILTER")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	public static final RenderPipeline POST_BLOOM_DOWNSAMPLE = post("pipeline/post_bloom_downsample", "core/post_bloom", BLOOM_INPUTS)
			.withShaderDefine("DOWNSAMPLE")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	/** Added onto the next larger mip (which already holds its own downsample). */
	public static final RenderPipeline POST_BLOOM_UPSAMPLE = post("pipeline/post_bloom_upsample", "core/post_bloom", BLOOM_INPUTS)
			.withShaderDefine("UPSAMPLE")
			.withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.ADDITIVE), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	/** Color Grading + Bloom composite: rewrites the frame from a copy of itself. */
	public static final RenderPipeline POST_GRADE = post("pipeline/post_grade", "core/post_grade", GRADE_INPUTS)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	// ---- World: Sun Rays and Wet Lens (render.post.PostFx) -------------------------------------

	/** Sun Rays mask and composite: its parameters, a colour source and the world's depth. */
	public static final BindGroupLayout RAYS_DEPTH_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidRays", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Sun Rays radial blurs. */
	public static final BindGroupLayout RAYS_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidRays", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Where light reaches the sky around the sun (half resolution). */
	public static final RenderPipeline POST_RAYS_MASK = post("pipeline/post_rays_mask", "core/post_rays", RAYS_DEPTH_INPUTS)
			.withShaderDefine("MASK")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	public static final RenderPipeline POST_RAYS_BLUR_FINE = post("pipeline/post_rays_blur_fine", "core/post_rays", RAYS_INPUTS)
			.withShaderDefine("BLUR_FINE")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	public static final RenderPipeline POST_RAYS_BLUR_COARSE = post("pipeline/post_rays_blur_coarse", "core/post_rays", RAYS_INPUTS)
			.withShaderDefine("BLUR_COARSE")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	/** Rays and lens flare, added onto the frame. */
	public static final RenderPipeline POST_RAYS_COMPOSITE = post("pipeline/post_rays_composite", "core/post_rays", RAYS_DEPTH_INPUTS)
			.withShaderDefine("COMPOSITE")
			.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
			.build();

	public static final BindGroupLayout LENS_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidLens", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Wet Lens: rewrites the frame from a copy of itself. */
	public static final RenderPipeline POST_LENS = post("pipeline/post_lens", "core/post_lens", LENS_INPUTS)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	// ---- World: Rainbows and Wet World (render.post.PostFx) -------------------------------------

	public static final BindGroupLayout RAINBOW_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidRainbow", UniformType.UNIFORM_BUFFER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** The bow's light added, Alexander's dark band subtracted: premultiplied (dst·(1 - a) + rgb). */
	public static final RenderPipeline POST_RAINBOW = post("pipeline/post_rainbow", "core/post_rainbow", RAINBOW_INPUTS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
			.build();

	public static final BindGroupLayout WET_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidWet", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("ColumnSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Wet World: rewrites wet pixels of the opaque world from a copy of it (dry ones are discarded). */
	public static final RenderPipeline POST_WET = post("pipeline/post_wet", "core/post_wet", WET_INPUTS)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	// ---- World: Hand Chams (render.hand.HandChams) ----------------------------------------------

	/**
	 * The first-person arm in vanilla's entity vertex format with our shader: skin (Sampler0), the
	 * frame behind the hand for Glass (Sampler1), the lightmap (Sampler2). Front faces only, so a
	 * translucent arm shows no inner faces.
	 */
	public static final RenderPipeline HAND_CHAMS = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
			.withLocation(LiquidClient.id("pipeline/hand_chams"))
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
			.withVertexShader(LiquidClient.id("core/hand_chams"))
			.withFragmentShader(LiquidClient.id("core/hand_chams"))
			.withVertexBinding(0, DefaultVertexFormat.ENTITY)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(true)
			.build();

	/**
	 * The same for improved transparency in the world (your player in third person): the alpha-only
	 * stages need only the texture's shape; accumulation also the frame copy, lightmap and globals.
	 */
	public static final OitPipelineSet OIT_HAND_CHAMS = OitPipelineSet.builder("liquidclient_hand_chams", RenderPipeline.builder()
					.withVertexShader(LiquidClient.id("core/hand_chams"))
					.withFragmentShader(LiquidClient.id("core/hand_chams"))
					.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
					.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
					.withVertexBinding(0, DefaultVertexFormat.ENTITY)
					.withPrimitiveTopology(PrimitiveTopology.QUADS)
					.withCull(true))
			.withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.GLOBALS)
					.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
					.withBindGroupLayout(BindGroupLayouts.SAMPLER2))
			.build();

	// ---- World: Motion Blur (render.post.PostFx) ------------------------------------------------

	public static final BindGroupLayout MOTION_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidMotion", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Rewrites the frame from a copy of itself, blurred along each pixel's motion. */
	public static final RenderPipeline POST_MOTION = post("pipeline/post_motion", "core/post_motion", MOTION_INPUTS)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	// ---- World: Ambient Occlusion (render.post.PostFx) -----------------------------------------

	public static final BindGroupLayout AO_DEPTH_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidAo", UniformType.UNIFORM_BUFFER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout AO_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidAo", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout AO_APPLY_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidAo", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Horizon search: visibility and view distance into a half-float texture. */
	public static final RenderPipeline POST_AO = post("pipeline/post_ao", "core/post_ao", AO_DEPTH_INPUTS)
			.withShaderDefine("AO")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	public static final RenderPipeline POST_AO_BLUR_H = post("pipeline/post_ao_blur_h", "core/post_ao", AO_INPUTS)
			.withShaderDefine("BLUR_H")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	public static final RenderPipeline POST_AO_BLUR_V = post("pipeline/post_ao_blur_v", "core/post_ao", AO_INPUTS)
			.withShaderDefine("BLUR_V")
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.build();

	/** Multiplied into the frame: dst · ao. */
	public static final RenderPipeline POST_AO_APPLY = post("pipeline/post_ao_apply", "core/post_ao", AO_APPLY_INPUTS)
			.withShaderDefine("APPLY")
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.DST_COLOR, BlendFactor.ZERO)))
			.build();

	/** The AO alone, replacing the frame (tuning view). */
	public static final RenderPipeline POST_AO_PREVIEW = post("pipeline/post_ao_preview", "core/post_ao", AO_APPLY_INPUTS)
			.withShaderDefine("APPLY")
			.withShaderDefine("PREVIEW")
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	// ---- World: Depth of Field (render.post.PostFx) --------------------------------------------

	public static final BindGroupLayout DOF_FOCUS_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout DOF_PREPARE_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("FocusSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout DOF_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout DOF_GATHER_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("TileSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout DOF_FILTER_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("CocSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout DOF_COMPOSITE_INPUTS = BindGroupLayout.builder()
			.withUniform("LiquidDof", UniformType.UNIFORM_BUFFER)
			.withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("BlurSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("CocSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("FocusSampler", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	/** Autofocus into a 1×1 half-float texture (ping-ponged with last frame's). */
	public static final RenderPipeline POST_DOF_FOCUS = dof("focus", "FOCUS", DOF_FOCUS_INPUTS);
	/** Half resolution: linear, highlight-expanded colour and the signed circle of confusion. */
	public static final RenderPipeline POST_DOF_PREPARE = dof("prepare", "PREPARE", DOF_PREPARE_INPUTS);
	public static final RenderPipeline POST_DOF_TILE = dof("tile", "TILE", DOF_INPUTS);
	public static final RenderPipeline POST_DOF_DILATE = dof("dilate", "DILATE", DOF_INPUTS);
	public static final RenderPipeline POST_DOF_PREFILTER = dof("prefilter", "PREFILTER", DOF_INPUTS);
	/** The bokeh: scatter-as-gather, premultiplied over the sharp frame. */
	public static final RenderPipeline POST_DOF_GATHER = dof("gather", "GATHER", DOF_GATHER_INPUTS);
	public static final RenderPipeline POST_DOF_FILTER = dof("filter", "FILTER", DOF_FILTER_INPUTS);

	/** Rewrites the frame from a copy of itself with the blur laid over it. */
	public static final RenderPipeline POST_DOF_COMPOSITE = post("pipeline/post_dof_composite", "core/post_dof", DOF_COMPOSITE_INPUTS)
			.withShaderDefine("COMPOSITE")
			.withColorTargetState(ColorTargetState.DEFAULT)
			.build();

	private LiquidPipelines() {
	}

	/** A Depth of Field pass into one of its half-float textures. */
	private static RenderPipeline dof(String pass, String define, BindGroupLayout inputs) {
		return post("pipeline/post_dof_" + pass, "core/post_dof", inputs)
				.withShaderDefine(define)
				.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
				.build();
	}

	/** Vanilla's full-screen triangle ({@code core/screenquad}, no vertex buffer) with our fragment stage. */
	private static RenderPipeline.Builder post(String location, String fragment, BindGroupLayout inputs) {
		return RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
				.withLocation(LiquidClient.id(location))
				.withVertexShader("core/screenquad")
				.withFragmentShader(LiquidClient.id(fragment))
				.withBindGroupLayout(inputs);
	}

	private static RenderPipeline.Builder particle(String location) {
		return RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
				.withLocation(LiquidClient.id(location))
				.withVertexShader(LiquidClient.id("core/ambient_particle"))
				.withFragmentShader(LiquidClient.id("core/ambient_particle"))
				.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
				.withCull(false);
	}

	private static RenderPipeline.Builder oitParticle() {
		return RenderPipeline.builder(RenderPipelines.OIT_PARTICLE_SNIPPET)
				.withVertexShader(LiquidClient.id("core/ambient_particle"))
				.withFragmentShader(LiquidClient.id("core/ambient_particle"))
				.withCull(false);
	}

	/** Same bind groups as vanilla's (private) GUI snippet so the GUI renderer's uniforms apply. */
	private static RenderPipeline.Builder gui(String location, String shader, VertexFormat format) {
		return RenderPipeline.builder()
				.withLocation(LiquidClient.id(location))
				.withBindGroupLayout(BindGroupLayouts.GLOBALS)
				.withBindGroupLayout(BindGroupLayouts.PROJECTION)
				.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
				.withVertexShader(LiquidClient.id(shader))
				.withFragmentShader(LiquidClient.id(shader))
				.withVertexBinding(0, format)
				.withPrimitiveTopology(PrimitiveTopology.QUADS)
				.withCull(false);
	}

	/**
	 * A camera-centred cube drawn with vanilla's own Projection / DynamicTransforms, so FOV changes,
	 * view bobbing and nausea distortion match the vanilla sky exactly. The fragment shader turns the
	 * interpolated cube position into a view ray. No depth test: the sky is the background.
	 */
	private static RenderPipeline.Builder sky(String location) {
		return RenderPipeline.builder()
				.withLocation(LiquidClient.id(location))
				.withBindGroupLayout(BindGroupLayouts.GLOBALS)
				.withBindGroupLayout(BindGroupLayouts.PROJECTION)
				.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
				.withBindGroupLayout(BindGroupLayouts.FOG)
				.withBindGroupLayout(SKY_UNIFORMS)
				.withVertexShader(LiquidClient.id("core/liquid_sky"))
				.withFragmentShader(LiquidClient.id("core/liquid_sky"))
				.withVertexBinding(0, DefaultVertexFormat.POSITION)
				.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withCull(false);
	}
}
