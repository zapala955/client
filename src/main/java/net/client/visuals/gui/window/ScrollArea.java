package net.client.visuals.gui.window;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Smooth vertical scrolling for a clipped region: eased offset, clamping to the content, and a thin
 * overlay scrollbar that shows while scrolling or hovering (macOS style).
 */
public final class ScrollArea {
	private final Smooth offset = new Smooth(0f, Motion.SCROLL);
	private final Smooth bar = new Smooth(0f, 8f);
	private float x;
	private float y;
	private float w;
	private float h;
	private float content;

	/** Sets the viewport and content height for this frame; returns the snapped scroll offset. */
	public float begin(float x, float y, float w, float h, float contentHeight) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.content = contentHeight;
		offset.target(Math.clamp(offset.target(), 0f, max()));
		return HighDpiEngine.snap(offset.update(UiFrame.dt));
	}

	/** The current snapped offset (as returned by the last {@link #begin}), without advancing it. */
	public float offset() {
		return HighDpiEngine.snap(offset.get());
	}

	public float max() {
		return Math.max(0f, content - h);
	}

	public boolean contains(double mx, double my) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public boolean scroll(double mx, double my, double amount) {
		if (!contains(mx, my) || max() <= 0f) {
			return false;
		}
		offset.target(Math.clamp(offset.target() - (float) amount * 32f, 0f, max()));
		bar.snap(1f);
		return true;
	}

	/** Moves the scroll target by {@code delta} (drag auto-scroll). */
	public void nudge(float delta) {
		offset.target(Math.clamp(offset.target() + delta, 0f, max()));
	}

	/** Makes the band [top, bottom] (content coordinates) visible. */
	public void reveal(float top, float bottom) {
		float target = offset.target();
		if (top < target) {
			target = top;
		} else if (bottom > target + h) {
			target = bottom - h;
		}
		offset.target(Math.clamp(target, 0f, max()));
	}

	public void reset() {
		offset.snap(0f);
		offset.target(0f);
	}

	public void scissor(GuiGraphicsExtractor g) {
		g.enableScissor((int) Math.floor(x), (int) Math.ceil(y), (int) Math.ceil(x + w), (int) Math.floor(y + h));
	}

	public void scrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float max = max();
		if (max <= 0.5f) {
			return;
		}
		boolean active = contains(mouseX, mouseY) || !offset.settled();
		float a = bar.target(active ? 1f : 0f).update(UiFrame.dt);
		if (a < 0.02f) {
			return;
		}
		float trackH = h - 8f;
		float thumbH = Math.max(18f, trackH * h / content);
		float thumbY = y + 4f + (trackH - thumbH) * (offset.get() / max);
		RenderUtils.fill(g, x + w - 6f, thumbY, 3.5f, thumbH, 1.75f, ColorUtil.fade(0x73FFFFFF, a));
	}
}
