package net.client.visuals.gui.photo;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.post.GradingLook;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;

/**
 * The film filters as a grid of swatches, like a photo app: each tile is a small sunset-to-shadow
 * gradient through the look's own colours (its gain over its highlight tone at the top, its lift
 * over its shadow tone at the bottom), its name below; the chosen one wears a yellow ring.
 */
final class FilterGridComponent extends UiComponent {
	private static final int COLUMNS = 3;
	private static final float GAP = 6f;
	private static final float SWATCH_H = 26f;
	private static final float TILE_H = SWATCH_H + 15f;
	private static final int YELLOW = 0xFFFFD60A;

	private final ModeSetting setting;
	private final TextLabel[] names;
	private final int[] tops;
	private final int[] bottoms;
	private final Smooth[] hover;

	FilterGridComponent(ModeSetting setting) {
		this.setting = setting;
		int n = GradingLook.IDS.length;
		names = new TextLabel[n];
		tops = new int[n];
		bottoms = new int[n];
		hover = new Smooth[n];
		for (int i = 0; i < n; i++) {
			names[i] = new TextLabel(I18n.get(i == 0 ? "liquidclient.photo.filter.original" : "liquidclient.grading.look." + GradingLook.IDS[i]),
					LiquidText.TextStyle.CAPTION);
			float[] look = GradingLook.get(i);
			// A sky over ground, seen through the look: gain × (a bright warm-white mixed with the
			// highlight tone) on top, lift + a dark slate mixed with the shadow tone at the bottom.
			float split = look[18];
			float mono = look[11];
			tops[i] = swatch(0.95f * look[6], 0.9f * look[7], 0.86f * look[8], look[15], look[16], look[17], split, mono, 0f, 0f, 0f);
			bottoms[i] = swatch(0.22f * look[6], 0.27f * look[7], 0.34f * look[8], look[12], look[13], look[14], split, mono, look[0], look[1], look[2]);
			hover[i] = new Smooth(0f, 14f);
		}
	}

	private static int swatch(float r, float g, float b, float tr, float tg, float tb, float split, float mono, float lr, float lg, float lb) {
		float k = Math.min(1f, split * 1.6f);
		float level = (r + g + b) / 1.5f;
		r = r * (1f - k) + tr * k * level + lr;
		g = g * (1f - k) + tg * k * level + lg;
		b = b * (1f - k) + tb * k * level + lb;
		float l = 0.2126f * r + 0.7152f * g + 0.0722f * b;
		r += (l - r) * mono;
		g += (l - g) * mono;
		b += (l - b) * mono;
		return 0xFF000000 | to8(r) << 16 | to8(g) << 8 | to8(b);
	}

	private static int to8(float v) {
		return Math.clamp(Math.round(v * 255f), 0, 255);
	}

	@Override
	public float preferredHeight(float width) {
		int rows = (names.length + COLUMNS - 1) / COLUMNS;
		return rows * TILE_H + (rows - 1) * GAP;
	}

	private float tileW() {
		return (width - GAP * (COLUMNS - 1)) / COLUMNS;
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float tw = tileW();
		int selected = setting.index();
		for (int i = 0; i < names.length; i++) {
			float tx = x + (i % COLUMNS) * (tw + GAP);
			float ty = y + (i / COLUMNS) * (TILE_H + GAP);
			boolean over = mouseX >= tx && mouseX < tx + tw && mouseY >= ty && mouseY < ty + TILE_H;
			float hv = hover[i].target(over ? 1f : 0f).update(UiFrame.dt);
			float r = 7f;
			RenderUtils.gradient(g, tx, ty, tw, SWATCH_H, r, tops[i], bottoms[i]);
			RenderUtils.stroke(g, tx, ty, tw, SWATCH_H, r, HighDpiEngine.px(), ColorUtil.fade(0x40FFFFFF, 0.6f + 0.4f * hv), 0x14FFFFFF);
			if (i == selected) {
				RenderUtils.stroke(g, tx - 1.5f, ty - 1.5f, tw + 3f, SWATCH_H + 3f, r + 1.5f, 1.5f, YELLOW, YELLOW);
			}
			TextLabel name = names[i];
			name.maxWidth(tw);
			int color = i == selected ? YELLOW : ColorUtil.lerp(0xA6EBEBF5, 0xFFF5F5F7, hv);
			LiquidText.draw(g, name, tx + (tw - name.width()) * 0.5f, ty + SWATCH_H + 4.5f, color, false);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
			return false;
		}
		float tw = tileW();
		int column = (int) ((mouseX - x) / (tw + GAP));
		int row = (int) ((mouseY - y) / (TILE_H + GAP));
		int index = row * COLUMNS + column;
		if (column >= 0 && column < COLUMNS && index >= 0 && index < names.length && index != setting.index()) {
			setting.set(index);
			UiSounds.click();
		}
		return true;
	}
}
