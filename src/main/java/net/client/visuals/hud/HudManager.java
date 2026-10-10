package net.client.visuals.hud;

import net.client.visuals.render.camera.Showcase;
import net.client.visuals.gui.hud.HudEditorScreen;
import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.anim.DeltaTimer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owns the HUD elements (every {@link HudModule} registered in the HUD category) and draws them
 * from a single Fabric HUD layer. Also samples the shared inputs some elements need: clicks per
 * second (sampled every frame, so even fast clicks aren't missed between ticks) and speed.
 */
public final class HudManager {
	private static final List<HudModule> ELEMENTS = new ArrayList<>();
	private static final List<HudModule> VIEW = Collections.unmodifiableList(ELEMENTS);
	private static final DeltaTimer TIMER = new DeltaTimer();
	private static final long[] LEFT_CLICKS = new long[64];
	private static final long[] RIGHT_CLICKS = new long[64];
	private static HudTheme theme;
	private static float time;
	private static int leftHead;
	private static int rightHead;
	private static boolean leftDown;
	private static boolean rightDown;
	private static double lastX;
	private static double lastZ;
	private static boolean hasLast;
	private static float speed;

	private HudManager() {
	}

	/** After {@code ModuleManager.init()}. */
	public static void init() {
		ELEMENTS.clear();
		for (Module module : ModuleManager.byCategory(Category.HUD)) {
			if (module instanceof HudModule element) {
				ELEMENTS.add(element);
			} else if (module instanceof HudTheme hudTheme) {
				theme = hudTheme;
			}
		}
	}

	public static List<HudModule> elements() {
		return VIEW;
	}

	public static HudTheme theme() {
		return theme;
	}

	// ---- Rendering ----------------------------------------------------------------------------

	/** Fabric HUD layer. */
	public static void extract(GuiGraphicsExtractor g, DeltaTracker delta) {
		float dt = TIMER.tick();
		Minecraft minecraft = Minecraft.getInstance();
		sampleClicks(minecraft);
		if (minecraft.gui.screen() instanceof HudEditorScreen) {
			return; // the editor draws (and times) the elements itself
		}
		time = (time + dt) % 3600f;
		if (theme == null || !theme.isEnabled() || minecraft.player == null || minecraft.gui.hud.isHidden() || Showcase.hudHidden()) {
			return;
		}
		if (theme.hideInDebug.get() && minecraft.getDebugOverlay().showDebugScreen()) {
			return;
		}
		HudContext ctx = HudContext.begin(theme, dt, time, false, delta.getGameTimeDeltaPartialTick(true));
		float screenW = g.guiWidth();
		float screenH = g.guiHeight();
		for (int i = 0; i < ELEMENTS.size(); i++) {
			HudModule element = ELEMENTS.get(i);
			boolean enabled = element.isEnabled();
			if (!enabled && element.appear.get() < 0.005f) {
				continue;
			}
			element.layout(ctx);
			boolean show = enabled && element.visible(ctx);
			float a = element.appear.target(show).update(dt);
			if (a >= 0.005f) {
				drawAppearing(g, ctx, element, screenW, screenH, a);
			}
		}
	}

	/** Like {@link #draw}, scaled up from 85 % and faded in by {@code appear} (eased). */
	private static void drawAppearing(GuiGraphicsExtractor g, HudContext ctx, HudModule element, float screenW, float screenH, float appear) {
		if (appear >= 0.995f) {
			draw(g, ctx, element, screenW, screenH);
			return;
		}
		float e = 1f - (1f - appear) * (1f - appear) * (1f - appear); // ease-out cubic
		float s = element.scaleFactor();
		float x = element.screenX(screenW);
		float y = element.screenY(screenH);
		float w = element.width() * s;
		float h = element.height() * s;
		float grow = 0.85f + 0.15f * e;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x + w * 0.5f, y + h * 0.5f);
		pose.scale(s * grow, s * grow);
		pose.translate(-element.width() * 0.5f, -element.height() * 0.5f);
		RenderUtils.pushAlpha(e);
		element.render(g, ctx);
		RenderUtils.popAlpha();
		pose.popMatrix();
	}

	/** Context for the editor, which draws the elements itself. */
	public static HudContext editorContext(float dt) {
		time = (time + dt) % 3600f;
		return HudContext.begin(theme, dt, time, true, 1f);
	}

	/** Draws a laid-out element at its position and scale. */
	public static void draw(GuiGraphicsExtractor g, HudContext ctx, HudModule element, float screenW, float screenH) {
		float s = element.scaleFactor();
		float x = HighDpiEngine.snap(element.screenX(screenW));
		float y = HighDpiEngine.snap(element.screenY(screenH));
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(s, s);
		element.render(g, ctx);
		pose.popMatrix();
	}

	// ---- Shared inputs ------------------------------------------------------------------------

	private static void sampleClicks(Minecraft minecraft) {
		boolean left = minecraft.options.keyAttack.isDown();
		boolean right = minecraft.options.keyUse.isDown();
		long now = System.currentTimeMillis();
		if (left && !leftDown) {
			LEFT_CLICKS[leftHead] = now;
			leftHead = (leftHead + 1) % LEFT_CLICKS.length;
		}
		if (right && !rightDown) {
			RIGHT_CLICKS[rightHead] = now;
			rightHead = (rightHead + 1) % RIGHT_CLICKS.length;
		}
		leftDown = left;
		rightDown = right;
	}

	private static int countRecent(long[] clicks) {
		long cutoff = System.currentTimeMillis() - 1000L;
		int count = 0;
		for (long click : clicks) {
			if (click > cutoff) {
				count++;
			}
		}
		return count;
	}

	public static int leftCps() {
		return countRecent(LEFT_CLICKS);
	}

	public static int rightCps() {
		return countRecent(RIGHT_CLICKS);
	}

	public static boolean leftDown() {
		return leftDown;
	}

	public static boolean rightDown() {
		return rightDown;
	}

	/** Horizontal blocks per second, smoothed over a few ticks. */
	public static float speed() {
		return speed;
	}

	/** Client tick. */
	public static void tick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			hasLast = false;
			speed = 0f;
			return;
		}
		double x = player.getX();
		double z = player.getZ();
		if (hasLast) {
			double dx = x - lastX;
			double dz = z - lastZ;
			float instant = (float) Math.sqrt(dx * dx + dz * dz) * 20f;
			speed += (instant - speed) * 0.35f;
		}
		lastX = x;
		lastZ = z;
		hasLast = true;
		if (theme != null && theme.isEnabled()) {
			for (int i = 0; i < ELEMENTS.size(); i++) {
				HudModule element = ELEMENTS.get(i);
				if (element.isEnabled()) {
					element.tick();
				}
			}
		}
	}
}
