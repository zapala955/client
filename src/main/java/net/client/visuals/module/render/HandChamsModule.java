package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;

/**
 * Hand Chams: your first-person arm and held items – and in third person the items you hold – drawn
 * with a shader instead of their textures: Liquid Glass (refracting the
 * world behind your hand, see-through crystal in the world), a glitching hologram, a flowing rainbow,
 * a window into a galaxy, glowing wireframe, plasma, flat colour, a tinted skin or a two-colour
 * gradient. Each with a rim glow in a second colour, opacity, texture detail, animation speed and
 * pattern scale; emissive (Bloom makes it glow) or lit by the world.
 *
 * <p>State only; {@code render.hand.HandChams} and the {@code Chams*Mixin}s draw it. Only your own
 * view changes – nobody else sees it.</p>
 */
public final class HandChamsModule extends Module {
	public static final String[] MODES = {"glass", "hologram", "chroma", "galaxy", "wireframe", "flat", "plasma", "tint", "gradient"};
	public static final int MODE_GLASS = 0;

	public final ModeSetting mode = add(new ModeSetting("mode", "liquidclient.hand_chams.mode", MODES, MODE_GLASS));
	public final NumberSetting hue = add(new NumberSetting("hue", "liquidclient.hand_chams.hue", 0, 360, 1, 205).suffix("°")
			.track(NumberSetting.Track.HUE));
	public final NumberSetting saturation = add(new NumberSetting("saturation", "liquidclient.hand_chams.saturation", 0, 100, 1, 75)
			.suffix("%"));
	public final NumberSetting hue2 = add(new NumberSetting("hue2", "liquidclient.hand_chams.hue2", 0, 359, 1, 290).suffix("°")
			.track(NumberSetting.Track.HUE));
	public final BooleanSetting accent = add(new BooleanSetting("accent", "liquidclient.hand_chams.accent", false));
	public final NumberSetting opacity = add(new NumberSetting("opacity", "liquidclient.hand_chams.opacity", 10, 100, 1, 90).suffix("%"));
	public final NumberSetting rim = add(new NumberSetting("rim", "liquidclient.hand_chams.rim", 0, 200, 5, 100).suffix("%"));
	public final NumberSetting detail = add(new NumberSetting("detail", "liquidclient.hand_chams.detail", 0, 100, 5, 20).suffix("%"));
	public final NumberSetting refraction = add(new NumberSetting("refraction", "liquidclient.hand_chams.refraction", 0, 200, 5, 100)
			.suffix("%"));
	public final NumberSetting speed = add(new NumberSetting("speed", "liquidclient.hand_chams.speed", 0, 300, 5, 100).suffix("%"));
	public final NumberSetting scale = add(new NumberSetting("scale", "liquidclient.hand_chams.scale", 25, 400, 25, 100).suffix("%"));
	public final BooleanSetting glow = add(new BooleanSetting("glow", "liquidclient.hand_chams.glow", true));
	public final BooleanSetting sleeves = add(new BooleanSetting("sleeves", "liquidclient.hand_chams.sleeves", true));
	public final BooleanSetting heldItems = add(new BooleanSetting("held_items", "liquidclient.hand_chams.held_items", true));
	public final BooleanSetting thirdPerson = add(new BooleanSetting("third_person", "liquidclient.hand_chams.third_person", true));

	public HandChamsModule() {
		super("hand_chams", Category.RENDER, 0xE357);
	}
}
