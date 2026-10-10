package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.StatElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/** The server address, or "Singleplayer". */
public final class ServerElement extends StatElement {
	private String shown = "";

	public ServerElement() {
		super("hud_server", false, 0.005f, 0.218f, 0xE153, "Server", false);
	}

	@Override
	protected void refresh(HudContext ctx) {
		Minecraft minecraft = Minecraft.getInstance();
		ServerData server = minecraft.getCurrentServer();
		String text = server != null ? server.ip : minecraft.isLocalServer() ? "Singleplayer" : ctx.editing ? "play.example.net" : "—";
		if (!text.equals(shown)) {
			shown = text;
			setValue(text);
		}
	}
}
