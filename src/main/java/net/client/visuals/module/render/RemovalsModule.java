package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;

/**
 * Removals: hides distracting screen effects – each one separately. Purely cosmetic: the effects'
 * gameplay (damage, freezing, the totem itself) is untouched; only what's drawn changes.
 *
 * <p>Hooks: {@code GameRendererEffectsMixin} (hurt shake, nausea / portal warp),
 * {@code ScreenEffectRendererMixin} (fire, water, in-wall, totem) and {@code HudOverlaysMixin}
 * (pumpkin, powder snow, vignette, portal / nausea overlays, boss bars, scoreboard).</p>
 */
public final class RemovalsModule extends Module {
	public static final String[] FIRE = {"normal", "low", "hidden"};
	private static final int FIRE_LOW = 1;
	private static final int FIRE_HIDDEN = 2;

	private static RemovalsModule instance;

	public final BooleanSetting hurtCam = add(new BooleanSetting("hurt_cam", "liquidclient.removals.hurt_cam", true));
	public final ModeSetting fire = add(new ModeSetting("fire", "liquidclient.removals.fire", FIRE, FIRE_LOW));
	public final BooleanSetting pumpkin = add(new BooleanSetting("pumpkin", "liquidclient.removals.pumpkin", true));
	public final BooleanSetting powderSnow = add(new BooleanSetting("powder_snow", "liquidclient.removals.powder_snow", false));
	public final BooleanSetting nausea = add(new BooleanSetting("nausea", "liquidclient.removals.nausea", true));
	public final BooleanSetting water = add(new BooleanSetting("water", "liquidclient.removals.water", true));
	public final BooleanSetting inWall = add(new BooleanSetting("in_wall", "liquidclient.removals.in_wall", false));
	public final BooleanSetting totem = add(new BooleanSetting("totem", "liquidclient.removals.totem", false));
	public final BooleanSetting vignette = add(new BooleanSetting("vignette", "liquidclient.removals.vignette", false));
	public final BooleanSetting bossBar = add(new BooleanSetting("boss_bar", "liquidclient.removals.boss_bar", false));
	public final BooleanSetting scoreboard = add(new BooleanSetting("scoreboard", "liquidclient.removals.scoreboard", false));

	public RemovalsModule() {
		super("removals", Category.RENDER, 0xE0BB);
		instance = this;
	}

	private static boolean on(BooleanSetting setting) {
		RemovalsModule m = instance;
		return m != null && m.isEnabled() && setting.get();
	}

	public static boolean hurtCamera() {
		return instance != null && on(instance.hurtCam);
	}

	public static boolean fireHidden() {
		return instance != null && instance.isEnabled() && instance.fire.index() == FIRE_HIDDEN;
	}

	public static boolean fireLow() {
		return instance != null && instance.isEnabled() && instance.fire.index() == FIRE_LOW;
	}

	public static boolean pumpkinOverlay() {
		return instance != null && on(instance.pumpkin);
	}

	public static boolean powderSnowOverlay() {
		return instance != null && on(instance.powderSnow);
	}

	public static boolean nauseaEffects() {
		return instance != null && on(instance.nausea);
	}

	public static boolean waterOverlay() {
		return instance != null && on(instance.water);
	}

	public static boolean inWallOverlay() {
		return instance != null && on(instance.inWall);
	}

	public static boolean totemAnimation() {
		return instance != null && on(instance.totem);
	}

	public static boolean vignetteOverlay() {
		return instance != null && on(instance.vignette);
	}

	public static boolean bossBars() {
		return instance != null && on(instance.bossBar);
	}

	public static boolean scoreboardSidebar() {
		return instance != null && on(instance.scoreboard);
	}
}
