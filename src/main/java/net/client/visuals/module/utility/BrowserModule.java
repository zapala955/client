package net.client.visuals.module.utility;

import net.client.visuals.browser.BrowserManager;
import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.module.setting.StringSetting;

/**
 * Browser: a real, interactive web browser on a floating glass screen in the world.
 *
 * <p>While enabled, right-click spawns the screen in front of you. Left-click on it clicks the page
 * where your crosshair points; right-click enters interaction mode (free cursor raycast onto the
 * screen + keyboard). The page is rendered by a headless Chromium (Edge / Chrome / Brave already
 * installed on the system) driven over the DevTools protocol – see {@code browser.BrowserEngine}.</p>
 *
 * <p>Disabling the module despawns the screen and shuts the browser process down.</p>
 */
public final class BrowserModule extends Module {
	public static final String[] HOMES = {"duckduckgo", "google", "youtube", "wikipedia", "bing"};
	public static final String[] HOME_URLS = {
			"https://duckduckgo.com", "https://www.google.com", "https://www.youtube.com", "https://www.wikipedia.org", "https://www.bing.com"
	};
	public static final String[] ENGINES = {"auto", "edge", "chrome", "brave"};
	public static final String[] RESOLUTIONS = {"720p", "1080p"};

	public final ModeSetting home = add(new ModeSetting("home", "liquidclient.browser.home", HOMES, 0));
	public final ModeSetting engine = add(new ModeSetting("engine", "liquidclient.browser.engine", ENGINES, 0));
	public final ModeSetting resolution = add(new ModeSetting("resolution", "liquidclient.browser.resolution", RESOLUTIONS, 0));
	public final NumberSetting size = add(new NumberSetting("size", "liquidclient.browser.size", 1.5, 8, 0.1, 3.6).suffix(" m"));
	public final NumberSetting distance = add(new NumberSetting("distance", "liquidclient.browser.distance", 1.2, 6, 0.1, 2.6).suffix(" m"));
	public final BooleanSetting darkPages = add(new BooleanSetting("dark_pages", "liquidclient.browser.dark_pages", true));
	public final StringSetting lastUrl = add(new StringSetting("last_url", "liquidclient.browser.last_url", ""));

	public BrowserModule() {
		super("browser", Category.UTILITY);
	}

	public String homeUrl() {
		return HOME_URLS[home.index()];
	}

	public int viewportWidth() {
		return resolution.index() == 1 ? 1920 : 1280;
	}

	public int viewportHeight() {
		return resolution.index() == 1 ? 1080 : 720;
	}

	@Override
	protected void onDisable() {
		BrowserManager.shutdown();
	}
}
