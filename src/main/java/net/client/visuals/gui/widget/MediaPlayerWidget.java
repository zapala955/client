package net.client.visuals.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.hud.element.MusicElement;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.media.MusicView;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.background.BackgroundEngine;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * The lock-screen "Now Playing" card of the main menu, in smoked glass: the frosted wallpaper under
 * a neutral dark tint (so it never picks up the photo's colour cast), a lit rim, the cover with its
 * own shadow, a semibold title over a dimmed artist, a hairline scrubber you can click to seek, and
 * solid transport glyphs. It only appears while a session exists and slides away when it ends.
 *
 * <p>Shares the bridge, artwork and settings (source, online artwork) with the Music HUD element.</p>
 */
public final class MediaPlayerWidget {
	public static final float WIDTH = 236f;
	public static final float HEIGHT = 76f;
	private static final float RADIUS = 20f;
	private static final float PAD = 12f;
	private static final float ART = 52f;
	private static final float BUTTON_R = 12f;
	private static final float SCRUB_Y = 43f;
	private static final int ICON_PLAY = IconFont.FILLED | 0xE037;
	private static final int ICON_PAUSE = IconFont.FILLED | 0xE034;
	private static final int ICON_NEXT = IconFont.FILLED | 0xE044;
	private static final int ICON_PREV = IconFont.FILLED | 0xE045;

	private final MusicView view = new MusicView();
	private final Smooth visible = new Smooth(0f, 6f);
	private final Smooth[] hover = {new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER), new Smooth(0f, Motion.HOVER)};
	private final Smooth[] press = {new Smooth(0f, Motion.PRESS), new Smooth(0f, Motion.PRESS), new Smooth(0f, Motion.PRESS)};
	private final Smooth scrubHover = new Smooth(0f, Motion.HOVER);
	private final float[] buttonX = new float[3];
	private float buttonY;
	private float left;
	private float top;
	private float textX;
	private float textW;
	private float shownAlpha;
	private float time;
	private int pressedButton = -1;
	/** Optimistic play / pause icon right after a click, until the player confirms. */
	private long optimisticUntil;
	private boolean optimisticPlaying;
	/** Optimistic scrubber position after a seek. */
	private long seekUntil;
	private float seekFraction;

	/** Whether a track was showing last frame (layouts that make room for the card). */
	public boolean hasTrack() {
		return view.track.active;
	}

	public void extract(GuiGraphicsExtractor g, float x, float y, int mouseX, int mouseY, float dt, float appear) {
		MusicElement settings = ModuleManager.get(MusicElement.class);
		view.update(dt, settings.source.index() == 1, settings.onlineArt.get(), "", "");
		time += dt;
		float v = visible.target(view.track.active).update(dt) * appear;
		shownAlpha = v;
		if (v < 0.01f) {
			return;
		}
		left = HighDpiEngine.snap(x);
		top = HighDpiEngine.snap(y + (1f - v) * 8f);
		float px = HighDpiEngine.px();
		RenderUtils.pushAlpha(v);

		// ---- Material: shadow, frosted wallpaper, neutral smoke, sheen, rim ----
		RenderUtils.shadow(g, left, top + 4f, WIDTH, HEIGHT, RADIUS, 12f, 0x6B000000);
		// Frosted wallpaper in menus; in game the engine already blurred the world behind us.
		boolean frosted = BackgroundEngine.INSTANCE.frost(g, left, top, WIDTH, HEIGHT, RADIUS) || GlassRenderer.frosted();
		RenderUtils.fill(g, left, top, WIDTH, HEIGHT, RADIUS, frosted ? 0x800A0B0F : 0xE60A0B0F);
		RenderUtils.light(g, left, top, WIDTH, HEIGHT * 0.5f, RADIUS, 0x14FFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, left, top, WIDTH, HEIGHT, RADIUS, px, 0x47FFFFFF, 0x0DFFFFFF);

		// ---- Cover ----
		float ax = left + PAD;
		float ay = top + PAD;
		RenderUtils.shadow(g, ax, ay + 2f, ART, ART, 11f, 6f, 0x80000000);
		view.cover(g, ax, ay, ART, 11f, time);
		RenderUtils.stroke(g, ax, ay, ART, ART, 11f, px, 0x2EFFFFFF, 0x0AFFFFFF);

		// ---- Text ----
		textX = ax + ART + 12f;
		textW = left + WIDTH - PAD - textX;
		view.clipped(g, view.headline, textX, top + 14f, textW, 0xFFFFFFFF, false, true);
		view.clipped(g, view.artist, textX, top + 27f, textW, 0x99FFFFFF, false, true);

		// ---- Scrubber + times ----
		boolean overScrub = mouseX >= textX - 2f && mouseX <= textX + textW + 2f && mouseY >= top + SCRUB_Y - 5f && mouseY <= top + SCRUB_Y + 8f;
		float sh = scrubHover.target(overScrub).update(dt);
		float barH = 3f + 2f * sh;
		float barY = top + SCRUB_Y + 1.5f - barH * 0.5f;
		float fraction = System.currentTimeMillis() < seekUntil ? seekFraction : Math.clamp(view.smoothProgress(), 0f, 1f);
		RenderUtils.fill(g, textX, barY, textW, barH, barH * 0.5f, 0x38FFFFFF);
		RenderUtils.fill(g, textX, barY, Math.max(barH, textW * fraction), barH, barH * 0.5f, ColorUtil.lerp(0xE6FFFFFF, 0xFFFFFFFF, sh));
		if (sh > 0.01f) {
			RenderUtils.circle(g, textX + textW * fraction, barY + barH * 0.5f, 3.5f * sh, 0xFFFFFFFF);
		}
		LiquidText.draw(g, view.elapsedSmall, textX, top + 50f, 0x8CFFFFFF, false);
		LiquidText.draw(g, view.remainingSmall, textX + textW - view.remainingSmall.width(), top + 50f, 0x8CFFFFFF, false);

		// ---- Transport ----
		float cx = textX + textW * 0.5f;
		buttonY = top + 61f;
		buttonX[0] = cx - 29f;
		buttonX[1] = cx;
		buttonX[2] = cx + 29f;
		boolean playing = System.currentTimeMillis() < optimisticUntil ? optimisticPlaying : view.isPlaying();
		int[] icons = {ICON_PREV, playing ? ICON_PAUSE : ICON_PLAY, ICON_NEXT};
		for (int i = 0; i < 3; i++) {
			boolean over = over(i, mouseX, mouseY);
			float h = hover[i].target(over).update(dt);
			float p = press[i].target(pressedButton == i && over).update(dt);
			float scale = Motion.interactionScale(h, p, 0.08f);
			if (h > 0.01f) {
				RenderUtils.circle(g, buttonX[i], buttonY, BUTTON_R * scale, ColorUtil.fade(0x1FFFFFFF, h));
			}
			Matrix3x2fStack pose = g.pose();
			pose.pushMatrix();
			pose.translate(buttonX[i], buttonY);
			pose.scale(scale, scale);
			IconFont.draw(g, icons[i], 0f, 0f, i == 1 ? 22f : 17f, ColorUtil.lerp(0xEBFFFFFF, 0xFFFFFFFF, h), 0);
			pose.popMatrix();
		}
		RenderUtils.popAlpha();
	}

	private boolean over(int button, double mouseX, double mouseY) {
		double dx = mouseX - buttonX[button];
		double dy = mouseY - buttonY;
		return dx * dx + dy * dy <= BUTTON_R * BUTTON_R;
	}

	private boolean contains(double mouseX, double mouseY) {
		return shownAlpha > 0.5f && mouseX >= left && mouseX < left + WIDTH && mouseY >= top && mouseY < top + HEIGHT;
	}

	private boolean overScrubber(double mouseX, double mouseY) {
		return mouseX >= textX - 2f && mouseX <= textX + textW + 2f && mouseY >= top + SCRUB_Y - 5f && mouseY <= top + SCRUB_Y + 8f;
	}

	/** @return whether the click landed on the card (consumed) */
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!contains(mouseX, mouseY)) {
			return false;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			for (int i = 0; i < 3; i++) {
				if (over(i, mouseX, mouseY)) {
					pressedButton = i;
					return true;
				}
			}
			if (overScrubber(mouseX, mouseY) && view.track.active && view.track.duration > 0L) {
				seekFraction = (float) Math.clamp((mouseX - textX) / Math.max(1f, textW), 0.0, 1.0);
				seekUntil = System.currentTimeMillis() + 1500L;
				MediaBridge.seek(Math.round(seekFraction * view.track.duration));
				UiSounds.click();
			}
		}
		return true;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int pressed = pressedButton;
		pressedButton = -1;
		if (pressed < 0 || !over(pressed, mouseX, mouseY)) {
			return pressed >= 0;
		}
		UiSounds.click();
		switch (pressed) {
			case 0 -> MediaBridge.previous();
			case 1 -> {
				optimisticPlaying = !view.isPlaying();
				optimisticUntil = System.currentTimeMillis() + 1500L;
				MediaBridge.togglePlayPause();
			}
			default -> MediaBridge.next();
		}
		return true;
	}
}
