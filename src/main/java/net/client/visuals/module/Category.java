package net.client.visuals.module;

import net.client.visuals.render.Palette;

/**
 * Module categories (also the ClickGUI sidebar sections). Colours are Apple system colours so each
 * category reads at a glance; the icon is a Lucide codepoint. {@link #SETTINGS} holds interface
 * preferences, not modules. Categories without modules are hidden from the sidebar.
 */
public enum Category {
	COMBAT("liquidclient.category.combat", Palette.ACCENT_PINK, 0xE2B4),
	MOVEMENT("liquidclient.category.movement", Palette.ACCENT_GREEN, 0xE3B9),
	RENDER("liquidclient.category.render", Palette.ACCENT_PURPLE, 0xE0BA),
	PLAYER("liquidclient.category.player", Palette.ACCENT_ORANGE, 0xE19F),
	UTILITY("liquidclient.category.utility", Palette.ACCENT_TEAL, 0xE1B1),
	HUD("liquidclient.category.hud", Palette.ACCENT_BLUE, 0xE1C1),
	SETTINGS("liquidclient.category.settings", 0xFF8E8E93, 0xE154);

	private static final Category[] VALUES = values();

	public final String translationKey;
	public final int color;
	public final int icon;

	Category(String translationKey, int color, int icon) {
		this.translationKey = translationKey;
		this.color = color;
		this.icon = icon;
	}

	public static Category byIndex(int index) {
		return VALUES[Math.floorMod(index, VALUES.length)];
	}

	public static int count() {
		return VALUES.length;
	}
}
