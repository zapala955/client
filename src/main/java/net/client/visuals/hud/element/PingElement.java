package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.StatElement;
import net.client.visuals.module.setting.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

/** Latency to the server, as the tab list reports it. */
public final class PingElement extends StatElement {
	public final BooleanSetting colorize = add(new BooleanSetting("colorize", "liquidclient.hud.colorize", true));
	private int shown = Integer.MIN_VALUE;
	private int latency;

	public PingElement() {
		super("hud_ping", true, 0.005f, 0.05f, 0xE1AE, "Ping", true);
		unit.setText("ms");
	}

	@Override
	public void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener connection = minecraft.getConnection();
		latency = 0;
		if (connection != null && minecraft.player != null) {
			PlayerInfo info = connection.getPlayerInfo(minecraft.player.getUUID());
			if (info != null) {
				latency = info.getLatency();
			}
		}
	}

	@Override
	protected void refresh(HudContext ctx) {
		if (latency != shown) {
			shown = latency;
			setValue(Integer.toString(latency));
		}
		valueColor = !colorize.get() ? 0 : latency < 80 ? HudContext.GOOD : latency < 160 ? HudContext.WARN : HudContext.BAD;
	}
}
