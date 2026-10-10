package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.StatElement;
import net.client.visuals.module.setting.BooleanSetting;

/** Clicks per second over the last second (left, optionally | right). */
public final class CpsElement extends StatElement {
	public final BooleanSetting showRight = add(new BooleanSetting("show_right", "liquidclient.hud.cps.right", true));
	private int shownLeft = -1;
	private int shownRight = -1;
	private boolean shownBoth;

	public CpsElement() {
		super("hud_cps", false, 0.005f, 0.134f, 0xE120, "CPS", false);
		unit.setText("cps");
	}

	@Override
	protected void refresh(HudContext ctx) {
		int left = HudManager.leftCps();
		int right = HudManager.rightCps();
		boolean both = showRight.get();
		if (left != shownLeft || right != shownRight || both != shownBoth) {
			shownLeft = left;
			shownRight = right;
			shownBoth = both;
			setValue(both ? left + " | " + right : Integer.toString(left));
		}
	}
}
