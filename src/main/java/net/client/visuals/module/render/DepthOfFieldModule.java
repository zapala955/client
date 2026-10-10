package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

import java.util.Locale;

/**
 * Depth of Field: the world through a real camera lens. What you look at is sharp, everything nearer
 * or further melts into soft blur, and lights become bokeh – round, hexagonal, anamorphic ovals or
 * cat's eyes towards the edges of the frame. Autofocus follows the crosshair (or a fixed distance, or
 * a tilt-shift miniature band); aperture and focal length behave like a lens's (zooming in lengthens
 * it). The hand and the HUD stay sharp.
 *
 * <p>State only; {@code render.post.PostFx} draws it after the world ({@code post_dof.fsh}).</p>
 */
public final class DepthOfFieldModule extends Module {
	public static final String[] FOCUS = {"auto", "manual", "tilt_shift"};
	public static final String[] BOKEH = {"circle", "hexagon", "anamorphic", "cats_eye"};
	public static final String[] QUALITY = {"performance", "balanced", "ultra"};
	public static final String[] ASSIST = {"off", "peaking", "coc_map"};

	public final ModeSetting focusMode = add(new ModeSetting("focus_mode", "liquidclient.depth_of_field.focus_mode", FOCUS, 0));
	public final NumberSetting focusDistance = add(new NumberSetting("focus_distance", "liquidclient.depth_of_field.focus_distance", 0.5, 128, 0.5, 8)
			.suffix(" blocks"));
	public final NumberSetting focusSpeed = add(new NumberSetting("focus_speed", "liquidclient.depth_of_field.focus_speed", 1, 20, 0.5, 6));
	public final NumberSetting aperture = add(new NumberSetting("aperture", "liquidclient.depth_of_field.aperture", 1, 16, 0.1, 1.8)
			.format(v -> String.format(Locale.ROOT, "f/%.1f", v)));
	public final NumberSetting focalLength = add(new NumberSetting("focal_length", "liquidclient.depth_of_field.focal_length", 18, 200, 1, 50)
			.suffix(" mm"));
	public final NumberSetting maxBlur = add(new NumberSetting("max_blur", "liquidclient.depth_of_field.max_blur", 0.5, 5, 0.1, 3)
			.suffix("%"));
	public final BooleanSetting nearBlur = add(new BooleanSetting("near_blur", "liquidclient.depth_of_field.near_blur", true));
	public final ModeSetting bokeh = add(new ModeSetting("bokeh", "liquidclient.depth_of_field.bokeh", BOKEH, 0));
	public final NumberSetting highlights = add(new NumberSetting("highlights", "liquidclient.depth_of_field.highlights", 0, 100, 5, 50)
			.suffix("%"));
	public final NumberSetting fringing = add(new NumberSetting("fringing", "liquidclient.depth_of_field.fringing", 0, 100, 5, 25)
			.suffix("%"));
	public final NumberSetting tiltPosition = add(new NumberSetting("tilt_position", "liquidclient.depth_of_field.tilt_position", 10, 90, 1, 50)
			.suffix("%"));
	public final NumberSetting tiltWidth = add(new NumberSetting("tilt_width", "liquidclient.depth_of_field.tilt_width", 5, 60, 1, 18)
			.suffix("%"));
	public final ModeSetting quality = add(new ModeSetting("quality", "liquidclient.depth_of_field.quality", QUALITY, 1));
	public final ModeSetting focusAssist = add(new ModeSetting("focus_assist", "liquidclient.depth_of_field.focus_assist", ASSIST, 0));

	public DepthOfFieldModule() {
		super("depth_of_field", Category.RENDER, 0xE180);
	}
}
