package net.client.visuals.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GlassRenderer;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.Palette;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import org.joml.Matrix3x2fStack;

/**
 * Dropdown bound to a {@link ModeSetting}. Opens in place (the option list pushes the following
 * content down) instead of as a floating popup, so there are no z-order or input-capture surprises
 * inside the scrolling ClickGUI. The list height animates; rows are scissored to it.
 */
public final class DropdownComponent extends UiComponent {
	private static final float HEADER = 18f;
	private static final float ROW = 16f;
	private static final float PILL_H = 15f;

	private final ModeSetting setting;
	private final int accent;
	private final TextLabel label;
	private final TextLabel[] options;
	private final Smooth open = new Smooth(0f, Motion.TOGGLE);
	private final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth[] rowHover;
	private boolean expanded;

	public DropdownComponent(ModeSetting setting, int accent) {
		this.setting = setting;
		this.accent = accent;
		this.label = new TextLabel(I18n.get(setting.translationKey()), LiquidText.TextStyle.BODY);
		this.options = new TextLabel[setting.count()];
		this.rowHover = new Smooth[setting.count()];
		for (int i = 0; i < options.length; i++) {
			options[i] = new TextLabel(I18n.get(setting.modeKey(i)), LiquidText.TextStyle.BODY);
			rowHover[i] = new Smooth(0f, Motion.HOVER);
		}
	}

	private float listHeight() {
		return options.length * ROW + 4f;
	}

	@Override
	public float preferredHeight(float width) {
		return HEADER + listHeight() * open.get();
	}

	@Override
	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		open.speed(Motion.speed(Motion.TOGGLE));
		hover.speed(Motion.speed(Motion.HOVER));
		float o = open.target(expanded).update(dt);
		boolean overHeader = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEADER;
		float h = hover.target(overHeader || expanded).update(dt);

		// Header: label left, current value in a glass pill right.
		TextLabel current = options[setting.index()];
		float pillW = Math.min(width * 0.62f, current.width() + 24f);
		float px = x + width - pillW;
		float py = y + (HEADER - PILL_H) * 0.5f;
		label.maxWidth(px - x - 6f);
		LiquidText.draw(g, label, x, y + (HEADER - label.capHeight()) * 0.5f, ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, h * 0.6f), false);
		GlassRenderer.surface(g, px, py, pillW, PILL_H, 7f, 0.5f,
				ColorUtil.lerp(0x24FFFFFF, 0x38FFFFFF, h), ColorUtil.lerp(0x14FFFFFF, 0x24FFFFFF, h),
				ColorUtil.lerp(Palette.GLASS_BORDER_HIGHLIGHT, Palette.GLASS_BORDER_HOVER, h), Palette.GLASS_BORDER);
		current.maxWidth(pillW - 24f);
		LiquidText.draw(g, current, px + 8f, py + (PILL_H - current.capHeight()) * 0.5f, Palette.TEXT_PRIMARY, false);
		extractChevron(g, px + pillW - 9f, py + PILL_H * 0.5f, o);

		if (o <= 0.01f) {
			return;
		}
		// Option list, revealed by an animated scissor.
		float top = y + HEADER + 2f;
		float visible = listHeight() * o;
		g.enableScissor((int) Math.floor(x - 2f), (int) Math.floor(top - 1f), (int) Math.ceil(x + width + 2f), (int) Math.ceil(top + visible));
		RenderUtils.pushAlpha(o);
		RenderUtils.fill(g, x, top, width, listHeight() - 2f, 8f, 0x40020617);
		RenderUtils.hairline(g, x, top, width, listHeight() - 2f, 8f, 0x26FFFFFF, 0x0DFFFFFF);
		for (int i = 0; i < options.length; i++) {
			float ry = top + 1f + i * ROW;
			boolean over = mouseX >= x && mouseX < x + width && mouseY >= ry && mouseY < ry + ROW && mouseY < top + visible;
			float rh = rowHover[i].target(over).update(dt);
			boolean selected = i == setting.index();
			if (selected) {
				RenderUtils.fill(g, x + 2f, ry + 1f, width - 4f, ROW - 2f, 6f, ColorUtil.fade(accent, 0.28f));
			} else if (rh > 0.01f) {
				RenderUtils.fill(g, x + 2f, ry + 1f, width - 4f, ROW - 2f, 6f, ColorUtil.fade(Palette.HOVER_FILL, rh));
			}
			TextLabel option = options[i].maxWidth(width - 28f);
			LiquidText.draw(g, option, x + 8f, ry + (ROW - option.capHeight()) * 0.5f,
					selected ? Palette.TEXT_PRIMARY : ColorUtil.lerp(Palette.TEXT_SECONDARY, Palette.TEXT_PRIMARY, rh), false);
			if (selected) {
				extractCheck(g, x + width - 12f, ry + ROW * 0.5f, accent);
			}
		}
		RenderUtils.popAlpha();
		g.disableScissor();
	}

	/** Chevron that rotates from ⌄ to ⌃ as the list opens. */
	private static void extractChevron(GuiGraphicsExtractor g, float cx, float cy, float open) {
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.PI * open);
		float thick = Math.max(1.1f, HighDpiEngine.px() * 2f);
		RenderUtils.line(g, -2.5f, -1f, 0f, 1.5f, thick, Palette.TEXT_SECONDARY);
		RenderUtils.line(g, 0f, 1.5f, 2.5f, -1f, thick, Palette.TEXT_SECONDARY);
		pose.popMatrix();
	}

	private static void extractCheck(GuiGraphicsExtractor g, float cx, float cy, int color) {
		float thick = 1.3f;
		RenderUtils.line(g, cx - 3f, cy, cx - 1f, cy + 2f, thick, color);
		RenderUtils.line(g, cx - 1f, cy + 2f, cx + 3f, cy - 2.5f, thick, color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !contains(mouseX, mouseY)) {
			return false;
		}
		if (mouseY < y + HEADER) {
			expanded = !expanded;
			UiSounds.click();
			return true;
		}
		if (expanded) {
			int index = (int) Math.floor((mouseY - (y + HEADER + 3f)) / ROW);
			if (index >= 0 && index < options.length) {
				setting.set(index);
				expanded = false;
				UiSounds.toggle(true);
			}
		}
		return true;
	}
}
