package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.StatElement;
import net.client.visuals.module.setting.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Frames per second (Minecraft's own once-a-second count). */
public final class FpsElement extends StatElement {
	public final BooleanSetting colorize = add(new BooleanSetting("colorize", "liquidclient.hud.colorize", false));
	private int shown = -1;

	public FpsElement() {
		super("hud_fps", true, 0.005f, 0.008f, 0xE1BF, "FPS", false);
		unit.setText("fps");
	}

	@Override
	protected void refresh(HudContext ctx) {
		int fps = Minecraft.getInstance().getFps();
		if (fps != shown) {
			shown = fps;
			setValue(Integer.toString(fps));
		}
		valueColor = !colorize.get() ? 0 : fps >= 60 ? HudContext.GOOD : fps >= 30 ? HudContext.WARN : HudContext.BAD;
	}
}
