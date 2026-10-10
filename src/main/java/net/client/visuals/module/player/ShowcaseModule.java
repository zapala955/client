package net.client.visuals.module.player;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Showcase: the camera glides out of your eyes into a cinematic shot of your character – a slow
 * turntable orbit, a hero portrait, a sweeping drone shot or a gentle dolly – through a longer
 * lens, with letterbox bars and a glass name card, then glides back when you turn it off. Drag the
 * mouse to orbit, scroll to zoom. Toggle with its key (Controls → Liquid Client, F6 by default).
 *
 * <p>State only; {@code render.camera.Showcase} drives the camera ({@code CameraShowcaseMixin}).
 * Purely a camera: your character keeps standing (or walking) where it is.</p>
 */
public final class ShowcaseModule extends Module {
	public static final String[] MODES = {"orbit", "portrait", "cinematic", "dolly"};
	public static final int ORBIT = 0;
	public static final int PORTRAIT = 1;
	public static final int CINEMATIC = 2;
	public static final int DOLLY = 3;

	public final ModeSetting mode = add(new ModeSetting("mode", "liquidclient.showcase.mode", MODES, ORBIT));
	public final NumberSetting distance = add(new NumberSetting("distance", "liquidclient.showcase.distance", 1.5, 8, 0.1, 3.6)
			.suffix(" blocks"));
	public final NumberSetting height = add(new NumberSetting("height", "liquidclient.showcase.height", -1, 3, 0.05, 0.55)
			.suffix(" blocks"));
	public final NumberSetting focus = add(new NumberSetting("focus", "liquidclient.showcase.focus", 20, 100, 1, 62).suffix("%"));
	public final NumberSetting speed = add(new NumberSetting("speed", "liquidclient.showcase.speed", 0, 60, 1, 14).suffix("°/s"));
	public final BooleanSetting reverse = add(new BooleanSetting("reverse", "liquidclient.showcase.reverse", false));
	public final NumberSetting lens = add(new NumberSetting("lens", "liquidclient.showcase.lens", 25, 110, 1, 52).suffix("°"));
	public final NumberSetting transition = add(new NumberSetting("transition", "liquidclient.showcase.transition", 0.4, 3, 0.05, 1.3)
			.suffix(" s"));
	public final BooleanSetting handheld = add(new BooleanSetting("handheld", "liquidclient.showcase.handheld", true));
	public final BooleanSetting mouseOrbit = add(new BooleanSetting("mouse_orbit", "liquidclient.showcase.mouse_orbit", true));
	public final BooleanSetting hideHud = add(new BooleanSetting("hide_hud", "liquidclient.showcase.hide_hud", true));
	public final BooleanSetting letterbox = add(new BooleanSetting("letterbox", "liquidclient.showcase.letterbox", true));
	public final BooleanSetting nameCard = add(new BooleanSetting("name_card", "liquidclient.showcase.name_card", true));
	public final BooleanSetting exitOnMove = add(new BooleanSetting("exit_on_move", "liquidclient.showcase.exit_on_move", false));

	public ShowcaseModule() {
		super("showcase", Category.PLAYER, 0xE064);
	}
}
