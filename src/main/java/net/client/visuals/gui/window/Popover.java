package net.client.visuals.gui.window;

import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * A pop-up menu (macOS pop-up button menu): dark vibrant panel under / over its anchor, rows with a
 * checkmark on the current value and an accent highlight under the pointer. Scrolls when long.
 * Owned by {@link WindowScreen}, which draws it above everything and routes input to it first.
 */
public final class Popover {
	private static final float ROW = 18f;
	private static final float PAD = 5f;
	private static final float MAX_H = 210f;
	private static final int ICON_CHECK = 0xE06C;

	private final TextLabel[] labels;
	private final int selected;
	private final IntConsumer onPick;
	private final Smooth open = new Smooth(0f, 18f);
	private final Smooth scroll = new Smooth(0f, 18f);
	private float x;
	private float y;
	private final float w;
	private final float h;
	private boolean closing;

	public Popover(List<String> items, int selected, IntConsumer onPick, float anchorX, float anchorY, float anchorW, float anchorH,
			float screenW, float screenH) {
		this.labels = new TextLabel[items.size()];
		float widest = 0f;
		for (int i = 0; i < items.size(); i++) {
			labels[i] = new TextLabel(items.get(i), LiquidText.TextStyle.BODY);
			widest = Math.max(widest, labels[i].width());
		}
		this.selected = selected;
		this.onPick = onPick;
		this.w = Math.min(screenW - 16f, Math.max(anchorW, widest + 40f));
		this.h = Math.min(MAX_H, items.size() * ROW + PAD * 2f);
		this.x = Math.clamp(anchorX + anchorW - w, 8f, screenW - w - 8f);
		float below = anchorY + anchorH + 4f;
		this.y = below + h <= screenH - 8f ? below : Math.max(8f, anchorY - h - 4f);
		// Start scrolled so the current value is visible.
		float target = Math.max(0f, (selected + 0.5f) * ROW - h * 0.5f);
		scroll.snap(Math.clamp(target, 0f, maxScroll()));
	}

	private float maxScroll() {
		return Math.max(0f, labels.length * ROW + PAD * 2f - h);
	}

	public boolean finished() {
		return closing && open.get() < 0.02f;
	}

	public void close() {
		closing = true;
	}

	public boolean contains(double mx, double my) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float a = open.target(closing ? 0f : 1f).update(dt);
		float s = scroll.update(dt);
		RenderUtils.pushAlpha(a);
		float yy = y - (1f - a) * 4f;
		RenderUtils.shadow(g, x, yy + 6f, w, h, 9f, 14f, 0x8C000000);
		RenderUtils.fill(g, x, yy, w, h, 9f, 0xF2232326);
		RenderUtils.light(g, x, yy, w, Math.min(h, 40f), 9f, 0x0DFFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, yy, w, h, 9f, HighDpiEngine.px(), 0x33FFFFFF, 0x14FFFFFF);
		g.enableScissor((int) x, (int) Math.ceil(yy + 2f), (int) (x + w), (int) Math.floor(yy + h - 2f));
		for (int i = 0; i < labels.length; i++) {
			float ry = yy + PAD + i * ROW - s;
			if (ry + ROW < yy || ry > yy + h) {
				continue;
			}
			boolean over = !closing && mouseX >= x + PAD && mouseX < x + w - PAD && mouseY >= ry && mouseY < ry + ROW && contains(mouseX, mouseY);
			if (over) {
				RenderUtils.fill(g, x + PAD, ry + 1f, w - PAD * 2f, ROW - 2f, 5f, Ui.ACCENT);
			}
			if (i == selected) {
				IconFont.draw(g, ICON_CHECK, x + PAD + 9f, ry + ROW * 0.5f, 9f, over ? 0xFFFFFFFF : Ui.TEXT, 0);
			}
			TextLabel label = labels[i];
			LiquidText.draw(g, label, x + PAD + 18f, ry + (ROW - label.capHeight()) * 0.5f, over ? 0xFFFFFFFF : Ui.TEXT, false);
		}
		g.disableScissor();
		RenderUtils.popAlpha();
	}

	/** @return true if the click was consumed (inside: pick; outside: close) */
	public boolean mouseClicked(double mx, double my) {
		if (closing) {
			return false;
		}
		if (!contains(mx, my)) {
			close();
			return true;
		}
		int index = (int) Math.floor((my - y - PAD + scroll.get()) / ROW);
		if (index >= 0 && index < labels.length) {
			UiSounds.click();
			onPick.accept(index);
			close();
		}
		return true;
	}

	public boolean mouseScrolled(double mx, double my, double amount) {
		if (!contains(mx, my)) {
			return false;
		}
		scroll.target(Math.clamp(scroll.target() - (float) amount * ROW * 2f, 0f, maxScroll()));
		return true;
	}

	public static int indexOf(List<?> values, Object value) {
		for (int i = 0; i < values.size(); i++) {
			if (java.util.Objects.equals(values.get(i), value)) {
				return i;
			}
		}
		return -1;
	}

	/** Shared pop-up button look: capsule with the current value and a chevron pair. */
	public static void button(GuiGraphicsExtractor g, TextLabel value, float bx, float by, float bw, float bh, float hover) {
		float r = 6f;
		RenderUtils.fill(g, bx, by, bw, bh, r, ColorUtil.lerp(Ui.FILL_CONTROL, Ui.FILL_CONTROL_HOVER, hover));
		RenderUtils.stroke(g, bx, by, bw, bh, r, HighDpiEngine.px(), 0x26FFFFFF, 0x0AFFFFFF);
		value.maxWidth(bw - 26f);
		LiquidText.draw(g, value, bx + 8f, by + (bh - value.capHeight()) * 0.5f, Ui.TEXT, false);
		IconFont.draw(g, 0xE211, bx + bw - 10f, by + bh * 0.5f, 9f, Ui.TEXT_2, 0);
	}
}
