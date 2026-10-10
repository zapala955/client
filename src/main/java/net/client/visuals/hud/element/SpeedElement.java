package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.StatElement;

/** Horizontal speed in blocks per second (smoothed). */
public final class SpeedElement extends StatElement {
	private int shownTenths = Integer.MIN_VALUE;

	public SpeedElement() {
		super("hud_speed", false, 0.005f, 0.176f, 0xE1B4, "Speed", true);
		unit.setText("b/s");
	}

	@Override
	protected void refresh(HudContext ctx) {
		int tenths = Math.round(HudManager.speed() * 10f);
		if (tenths != shownTenths) {
			shownTenths = tenths;
			setValue(tenths / 10 + "." + Math.abs(tenths % 10));
		}
	}
}
