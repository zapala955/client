package net.client.visuals.hud;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * The HUD itself: its switch shows / hides every element, its settings are the shared theme
 * (style, accent, opacity, corners, labels). Elements can override the style individually.
 */
public final class HudTheme extends Module {
	public static final String[] ACCENT_MODES = {"custom", "chroma", "album"};
	public static final String[] LABEL_MODES = {"icon", "text", "none"};

	public final ModeSetting style = add(new ModeSetting("look", "liquidclient.hud.style", HudStyle.IDS, HudStyle.FROSTED));
	public final NumberSetting blur = add(new NumberSetting("blur", "liquidclient.hud.blur", 0, 16, 0.5, 7));
	public final ModeSetting accentMode = add(new ModeSetting("accent_mode", "liquidclient.hud.accent_mode", ACCENT_MODES, 0));
	public final NumberSetting accentHue = add(new NumberSetting("accent_hue", "liquidclient.hud.accent_hue", 0, 360, 1, 211)
			.suffix("°").track(NumberSetting.Track.HUE));
	public final NumberSetting opacity = add(new NumberSetting("opacity", "liquidclient.hud.opacity", 0, 100, 1, 72).suffix("%"));
	public final NumberSetting radius = add(new NumberSetting("corners", "liquidclient.hud.radius", 0, 14, 0.5, 9));
	public final ModeSetting labels = add(new ModeSetting("labels", "liquidclient.hud.labels", LABEL_MODES, 0));
	public final BooleanSetting textShadow = add(new BooleanSetting("text_shadow", "liquidclient.hud.text_shadow", true));
	public final BooleanSetting hideInDebug = add(new BooleanSetting("hide_debug", "liquidclient.hud.hide_debug", true));

	public HudTheme() {
		super("hud", Category.HUD);
		setEnabled(true);
	}
}
