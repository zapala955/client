package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Chunk Animations: newly loaded chunk sections arrive with an animation instead of popping in.
 *
 * <p>State only. The animation runs entirely in the terrain vertex shader
 * ({@code shaders/include/chunk_animation.glsl}, injected by {@code ShaderManagerMixin}); see
 * {@code render.chunk.ChunkAnimator} for how settings and per-section progress reach it. Nothing is
 * moved on the CPU, so it costs no frame time and collisions are unaffected.</p>
 */
public final class ChunkAnimationsModule extends Module {
	/** Stable ids; the index is the shader's mode number (see chunk_animation.glsl). */
	public static final String[] MODES = {
			"rise", "drop", "horizon", "slide", "burst", "grow", "sprout", "spin", "flip", "swirl",
			"assemble", "zoom", "jelly", "glitch", "ripple", "spiral", "fade", "random"
	};
	/** Stable ids; the index is the shader's easing number. */
	public static final String[] EASINGS = {
			"linear", "sine", "quad", "cubic", "quart", "quint", "expo", "circ", "back", "elastic",
			"bounce", "in_out_cubic", "smooth"
	};

	public final ModeSetting mode = add(new ModeSetting("mode", "liquidclient.chunks.mode", MODES, 0));
	public final ModeSetting easing = add(new ModeSetting("easing", "liquidclient.chunks.easing", EASINGS, 8));
	public final NumberSetting duration = add(new NumberSetting("duration", "liquidclient.chunks.duration", 0.2, 4.0, 0.1, 0.9)
			.suffix(" s"));
	public final NumberSetting distance = add(new NumberSetting("distance", "liquidclient.chunks.distance", 4, 96, 2, 24)
			.suffix(" blocks"));
	public final BooleanSetting fade = add(new BooleanSetting("fade", "liquidclient.chunks.fade", true));
	public final BooleanSetting nearby = add(new BooleanSetting("nearby", "liquidclient.chunks.nearby", false));

	public ChunkAnimationsModule() {
		super("chunk_animations", Category.RENDER);
	}
}
