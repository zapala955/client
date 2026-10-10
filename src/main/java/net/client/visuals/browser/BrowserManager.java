package net.client.visuals.browser;

import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.browser.BrowserScreen;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.utility.BrowserModule;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.shader.ShaderSupport;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Glue between the Browser module, the world and the input system.
 *
 * <ul>
 *   <li><b>Spawn</b> – module on + no screen: the HUD says "Press RMB to spawn"; the next right-click
 *       (consumed before vanilla sees it) places the screen in front of the player and starts the
 *       browser.</li>
 *   <li><b>Crosshair</b> – looking at the screen: left-click clicks the page at the crosshair,
 *       right-click opens {@link BrowserScreen} (free cursor + keyboard). Both are consumed, so you
 *       don't break / use the block behind the screen.</li>
 *   <li><b>Render</b> – {@code COLLECT_SUBMITS}: uploads the newest frame and submits the screen.</li>
 *   <li><b>Teardown</b> – module off, world exit and client stop despawn the screen, free the
 *       textures and kill the browser process.</li>
 * </ul>
 */
public final class BrowserManager {
	private static final Identifier PAGE_ID = LiquidClient.id("dynamic/browser_page");
	private static final Identifier BEZEL_ID = LiquidClient.id("dynamic/browser_bezel");
	private static final double REACH = 64.0;

	private static final BrowserEngine ENGINE = new BrowserEngine();
	private static @Nullable BrowserPanel panel;
	private static @Nullable BrowserTexture pageTexture;
	private static @Nullable BrowserTexture bezelTexture;
	private static @Nullable RenderType pageType;
	private static @Nullable RenderType bezelType;
	private static float bezelAspect;
	private static boolean hovered;
	private static String lastSavedUrl = "";

	// HUD hint state
	private static final DeltaTimer HUD_TIMER = new DeltaTimer();
	private static final Smooth HINT = new Smooth(0f, Motion.HOVER);
	private static final TextLabel HINT_SPAWN = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel HINT_AIM = new TextLabel("", LiquidText.TextStyle.LABEL);
	private static final TextLabel HINT_STATUS = new TextLabel("", LiquidText.TextStyle.BODY);
	private static boolean hintsLocalized;

	private BrowserManager() {
	}

	public static BrowserEngine engine() {
		return ENGINE;
	}

	public static @Nullable BrowserPanel panel() {
		return panel;
	}

	private static BrowserModule module() {
		return ModuleManager.get(BrowserModule.class);
	}

	// ---- Tick (START_CLIENT_TICK: before vanilla processes key clicks) -------------------------

	public static void tick(Minecraft minecraft) {
		BrowserModule module = module();
		Player player = minecraft.player;
		if (!module.isEnabled() || player == null || minecraft.level == null) {
			hovered = false;
			return;
		}
		if (panel != null) {
			panel.setSize(module.size.getFloat(), module.viewportWidth() / (float) module.viewportHeight());
			rememberUrl(module);
		}
		if (minecraft.gui.screen() != null) {
			return;
		}

		if (panel == null) {
			if (minecraft.options.keyUse.consumeClick()) {
				drainClicks(minecraft);
				spawn(minecraft, player, module);
			}
			hovered = false;
			return;
		}

		BrowserPanel.Hit hit = crosshairHit(minecraft, player);
		hovered = hit != null;
		if (hit == null) {
			return;
		}
		// The screen is in front of whatever vanilla would hit: take the clicks.
		boolean clicked = false;
		while (minecraft.options.keyAttack.consumeClick()) {
			clickAt(hit, "left");
			clicked = true;
		}
		minecraft.options.keyAttack.setDown(false);
		if (minecraft.options.keyUse.consumeClick()) {
			drainClicks(minecraft);
			minecraft.options.keyUse.setDown(false);
			minecraft.gui.setScreen(new BrowserScreen());
			return;
		}
		if (clicked) {
			UiSounds.click();
		}
	}

	private static void drainClicks(Minecraft minecraft) {
		while (minecraft.options.keyUse.consumeClick()) {
			// swallow queued repeats so nothing is placed
		}
	}

	private static void spawn(Minecraft minecraft, Player player, BrowserModule module) {
		float aspect = module.viewportWidth() / (float) module.viewportHeight();
		panel = BrowserPanel.inFrontOf(player, module.distance.getFloat(), module.size.getFloat(), aspect);
		String start = module.lastUrl.get().isBlank() ? module.homeUrl() : module.lastUrl.get();
		ENGINE.start(module.engine.index(), module.viewportWidth(), module.viewportHeight(), start, module.darkPages.get());
		UiSounds.open();
	}

	/** Moves the screen in front of the player (toolbar "Move here"). */
	public static void moveToPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		if (panel != null && minecraft.player != null) {
			BrowserModule module = module();
			panel = BrowserPanel.inFrontOf(minecraft.player, module.distance.getFloat(), module.size.getFloat(),
					module.viewportWidth() / (float) module.viewportHeight());
		}
	}

	/** Removes the screen and stops the browser (module stays enabled: RMB spawns again). */
	public static void despawn() {
		rememberUrl(module());
		ModuleManager.saveIfDirty();
		panel = null;
		hovered = false;
		ENGINE.stop();
		releaseTextures();
	}

	/** Module disabled / world left / client stopping. */
	public static void shutdown() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.gui != null && minecraft.gui.screen() instanceof BrowserScreen) {
			minecraft.gui.setScreen(null);
		}
		despawn();
	}

	private static void rememberUrl(BrowserModule module) {
		String url = ENGINE.url();
		if (!url.isBlank() && !url.equals(lastSavedUrl) && !url.startsWith("about:") && !url.startsWith("chrome")) {
			lastSavedUrl = url;
			module.lastUrl.set(url);
		}
	}

	// ---- Hit testing --------------------------------------------------------------------------

	private static BrowserPanel.@Nullable Hit crosshairHit(Minecraft minecraft, Player player) {
		BrowserPanel current = panel;
		if (current == null) {
			return null;
		}
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1f);
		BrowserPanel.Hit hit = current.raycast(eye.x, eye.y, eye.z, look.x, look.y, look.z);
		if (hit == null || hit.distance() > REACH) {
			return null;
		}
		HitResult vanilla = minecraft.hitResult;
		if (vanilla != null && vanilla.getType() != HitResult.Type.MISS && vanilla.getLocation().distanceTo(eye) < hit.distance()) {
			return null; // something is in front of the screen
		}
		return hit;
	}

	private static void clickAt(BrowserPanel.Hit hit, String button) {
		double x = hit.u() * ENGINE.width();
		double y = hit.v() * ENGINE.height();
		ENGINE.mouse("mouseMoved", x, y, "none", 0, 0, 0);
		ENGINE.mouse("mousePressed", x, y, button, 1, 1, 0);
		ENGINE.mouse("mouseReleased", x, y, button, 1, 0, 0);
	}

	// ---- Rendering (COLLECT_SUBMITS, render thread) -------------------------------------------

	public static void collect(LevelRenderContext context) {
		BrowserPanel current = panel;
		if (current == null) {
			return;
		}
		ensureTextures(current);
		NativeImage frame = ENGINE.takeFrame();
		if (frame != null) {
			try {
				BrowserTexture page = pageTexture;
				if (page != null && (page.width() != frame.getWidth() || page.height() != frame.getHeight())) {
					createPageTexture(frame.getWidth(), frame.getHeight());
				}
				if (pageTexture != null) {
					pageTexture.upload(frame);
				}
			} finally {
				frame.close();
			}
		}
		if (pageType == null || bezelType == null) {
			return;
		}
		CameraRenderState camera = context.levelState().cameraRenderState;
		current.submit(context.submitNodeCollector(), context.poseStack(), camera.pos.x, camera.pos.y, camera.pos.z,
				bezelType, pageType, hovered || Minecraft.getInstance().gui.screen() instanceof BrowserScreen);
	}

	private static void ensureTextures(BrowserPanel current) {
		if (pageTexture == null) {
			createPageTexture(ENGINE.width(), ENGINE.height());
		}
		float aspect = current.width() / current.height();
		if (bezelTexture == null || Math.abs(aspect - bezelAspect) > 1.0e-3f) {
			bezelAspect = aspect;
			try (NativeImage bezel = BrowserPanel.createBezel(aspect)) {
				BrowserTexture texture = new BrowserTexture("Liquid browser bezel", bezel.getWidth(), bezel.getHeight());
				texture.upload(bezel);
				Minecraft.getInstance().getTextureManager().register(BEZEL_ID, texture);
				bezelTexture = texture;
				bezelType = RenderTypes.text(BEZEL_ID);
			}
		}
	}

	private static void createPageTexture(int width, int height) {
		BrowserTexture texture = new BrowserTexture("Liquid browser page", width, height);
		try (NativeImage blank = new NativeImage(width, height, false)) {
			blank.fillRect(0, 0, width, height, 0xFF0B1120);
			texture.upload(blank);
		}
		Minecraft.getInstance().getTextureManager().register(PAGE_ID, texture);
		pageTexture = texture;
		pageType = RenderTypes.text(PAGE_ID);
	}

	private static void releaseTextures() {
		Minecraft minecraft = Minecraft.getInstance();
		if (pageTexture != null) {
			minecraft.getTextureManager().release(PAGE_ID);
			pageTexture = null;
		}
		if (bezelTexture != null) {
			minecraft.getTextureManager().release(BEZEL_ID);
			bezelTexture = null;
		}
		pageType = null;
		bezelType = null;
	}

	// ---- HUD hint -----------------------------------------------------------------------------

	public static void extractHud(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft minecraft = Minecraft.getInstance();
		BrowserModule module = module();
		float dt = HUD_TIMER.tick();
		if (!module.isEnabled() && HINT.get() <= 0.01f) {
			return;
		}
		ShaderSupport.ensureValidated();
		boolean starting = ENGINE.status() == BrowserEngine.Status.STARTING;
		boolean failed = ENGINE.status() == BrowserEngine.Status.FAILED;
		boolean show = module.isEnabled() && minecraft.gui.screen() == null && minecraft.player != null
				&& (panel == null || hovered || starting || failed);
		float a = HINT.target(show).update(dt);
		if (a <= 0.01f) {
			return;
		}
		if (!hintsLocalized) {
			hintsLocalized = true;
			HINT_SPAWN.setText(I18n.get("liquidclient.browser.hint.spawn"));
			HINT_AIM.setText(I18n.get("liquidclient.browser.hint.aim"));
		}
		TextLabel label = panel == null ? HINT_SPAWN : HINT_AIM;
		if (failed) {
			HINT_STATUS.setText(I18n.get("liquidclient.browser.failed", ENGINE.error()));
		} else if (starting) {
			HINT_STATUS.setText(I18n.get("liquidclient.browser.starting"));
		} else {
			HINT_STATUS.setText("");
		}

		GlassRenderer.beginFrame(false);
		float w = Math.max(label.width(), HINT_STATUS.width()) + 34f;
		float h = HINT_STATUS.text().isEmpty() ? 22f : 34f;
		float x = HighDpiEngine.snap((g.guiWidth() - w) * 0.5f);
		float y = HighDpiEngine.snap(g.guiHeight() - 68f - h - (1f - a) * 6f);
		RenderUtils.pushAlpha(a);
		GlassRenderer.panel(g, x, y, w, h, 11f, 2f);
		RenderUtils.circle(g, x + 13f, y + 11f, 3f, failed ? Palette.ACCENT_PINK : Palette.ACCENT_TEAL);
		RenderUtils.outerGlow(g, x + 10f, y + 8f, 6f, 6f, 3f, 4f, ColorUtil.fade(failed ? Palette.ACCENT_PINK : Palette.ACCENT_TEAL, 0.6f));
		LiquidText.draw(g, label, x + 22f, y + 11f - label.capHeight() * 0.5f, Palette.TEXT_PRIMARY, true);
		if (!HINT_STATUS.text().isEmpty()) {
			LiquidText.draw(g, HINT_STATUS, x + 22f, y + 23f, failed ? 0xFFFF8FA3 : Palette.TEXT_TERTIARY, false);
		}
		RenderUtils.popAlpha();
	}
}
