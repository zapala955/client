package net.client.visuals.module.player;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * World Intro: when you join a world or a server, a cinematic drone shot reveals where you are, then
 * glides into your eyes – like a game's opening. It waits (behind black) until the chunks around you
 * have loaded and rendered, flies only through what's loaded and never through blocks, shows a glass
 * title card (the world or server, biome, time of day, weather, day), letterbox bars, hides the HUD,
 * and pulls focus onto you at the end. Any key or click skips it instantly; so does taking damage or
 * a hostile mob nearby. Input is never held – you can always act.
 *
 * <p>State only; {@code render.camera.WorldIntro} plans the shot and drives the camera
 * ({@code CameraShowcaseMixin}).</p>
 */
public final class WorldIntroModule extends Module {
	public static final String[] PLAY = {"join", "join_dimension", "off"};
	public static final int PLAY_JOIN = 0;
	public static final int PLAY_JOIN_AND_DIMENSION = 1;
	public static final int PLAY_OFF = 2;
	public static final String[] STYLE = {"orbit", "descent", "dolly"};
	public static final int STYLE_ORBIT = 0;
	public static final int STYLE_DESCENT = 1;
	public static final int STYLE_DOLLY = 2;

	public final ModeSetting play = add(new ModeSetting("play", "liquidclient.intro.play", PLAY, PLAY_JOIN));
	public final ModeSetting style = add(new ModeSetting("style", "liquidclient.intro.style", STYLE, STYLE_ORBIT));
	public final NumberSetting duration = add(new NumberSetting("duration", "liquidclient.intro.duration", 5, 20, 0.5, 10).suffix(" s"));
	public final BooleanSetting titleCard = add(new BooleanSetting("title_card", "liquidclient.intro.title_card", true));
	public final BooleanSetting letterbox = add(new BooleanSetting("letterbox", "liquidclient.intro.letterbox", true));

	public WorldIntroModule() {
		super("world_intro", Category.PLAYER, 0xE110);
	}
}
