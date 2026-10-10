package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * W A S D, mouse buttons (with CPS) and space – each key a small box in the element's style that
 * fills with the accent while held, easing in and out.
 */
public final class KeystrokesElement extends HudModule {
	private static final float GAP = 2f;
	private static final int UP = 0;
	private static final int LEFT = 1;
	private static final int DOWN = 2;
	private static final int RIGHT = 3;
	private static final int LMB = 4;
	private static final int RMB = 5;
	private static final int JUMP = 6;

	public final NumberSetting keySize = add(new NumberSetting("key_size", "liquidclient.hud.keys.size", 14, 26, 1, 18));
	public final BooleanSetting mouse = add(new BooleanSetting("mouse", "liquidclient.hud.keys.mouse", true));
	public final BooleanSetting cps = add(new BooleanSetting("cps", "liquidclient.hud.keys.cps", true));
	public final BooleanSetting space = add(new BooleanSetting("space", "liquidclient.hud.keys.space", true));

	private final TextLabel[] labels = new TextLabel[7];
	private final Smooth[] press = new Smooth[7];
	private final String[] boundNames = new String[7];
	private int ticks;
	private int shownLeftCps = -1;
	private int shownRightCps = -1;

	public KeystrokesElement() {
		super("hud_keystrokes", true, 0.005f, 0.42f);
		for (int i = 0; i < 7; i++) {
			labels[i] = new TextLabel("", i >= LMB ? LiquidText.TextStyle.BODY : LiquidText.TextStyle.LABEL);
			press[i] = new Smooth(0f, 18f);
			boundNames[i] = "";
		}
		labels[LMB].setText("LMB");
		labels[RMB].setText("RMB");
	}

	private static KeyMapping key(Options options, int index) {
		return switch (index) {
			case UP -> options.keyUp;
			case LEFT -> options.keyLeft;
			case DOWN -> options.keyDown;
			case RIGHT -> options.keyRight;
			case LMB -> options.keyAttack;
			case RMB -> options.keyUse;
			default -> options.keyJump;
		};
	}

	@Override
	public void tick() {
		// Re-read key names now and then (rebinding is rare; this avoids per-frame strings).
		if (ticks++ % 40 != 0) {
			return;
		}
		Options options = Minecraft.getInstance().options;
		for (int i = UP; i <= RIGHT; i++) {
			String name = key(options, i).getTranslatedKeyMessage().getString();
			if (name.length() > 3) {
				name = name.substring(0, 3);
			}
			if (!name.equals(boundNames[i])) {
				boundNames[i] = name;
				labels[i].setText(name);
			}
		}
	}

	private float size() {
		return (float) keySize.get();
	}

	@Override
	public void layout(HudContext ctx) {
		if (labels[UP].text().isEmpty()) {
			labels[UP].setText("W");
			labels[LEFT].setText("A");
			labels[DOWN].setText("S");
			labels[RIGHT].setText("D");
		}
		float k = size();
		width = k * 3f + GAP * 2f;
		height = k * 2f + GAP;
		if (mouse.get()) {
			height += GAP + k;
		}
		if (space.get()) {
			height += GAP + k * 0.55f;
		}
		if (cps.get()) {
			int left = HudManager.leftCps();
			int right = HudManager.rightCps();
			if (left != shownLeftCps) {
				shownLeftCps = left;
				labels[LMB].setText(left > 0 ? left + " CPS" : "LMB");
			}
			if (right != shownRightCps) {
				shownRightCps = right;
				labels[RMB].setText(right > 0 ? right + " CPS" : "RMB");
			}
		} else if (shownLeftCps != -1) {
			shownLeftCps = -1;
			shownRightCps = -1;
			labels[LMB].setText("LMB");
			labels[RMB].setText("RMB");
		}
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		Options options = Minecraft.getInstance().options;
		int style = ctx.style(this);
		int accent = ctx.accent(phase());
		float k = size();
		float y = 0f;
		key(g, ctx, options, UP, k + GAP, y, k, k, style, accent);
		y += k + GAP;
		key(g, ctx, options, LEFT, 0f, y, k, k, style, accent);
		key(g, ctx, options, DOWN, k + GAP, y, k, k, style, accent);
		key(g, ctx, options, RIGHT, (k + GAP) * 2f, y, k, k, style, accent);
		y += k + GAP;
		if (mouse.get()) {
			float half = (width - GAP) * 0.5f;
			key(g, ctx, options, LMB, 0f, y, half, k, style, accent);
			key(g, ctx, options, RMB, half + GAP, y, half, k, style, accent);
			y += k + GAP;
		}
		if (space.get()) {
			key(g, ctx, options, JUMP, 0f, y, width, k * 0.55f, style, accent);
		}
	}

	private void key(GuiGraphicsExtractor g, HudContext ctx, Options options, int index, float x, float y, float w, float h,
			int style, int accent) {
		boolean down = index == LMB ? HudManager.leftDown() : index == RMB ? HudManager.rightDown() : key(options, index).isDown();
		float p = press[index].target(down ? 1f : 0f).update(ctx.dt);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		// Pressed keys sink a hair, like a real keycap.
		float sink = 1f - 0.06f * p;
		pose.translate(w * 0.5f, h * 0.5f);
		pose.scale(sink, sink);
		pose.translate(-w * 0.5f, -h * 0.5f);
		ctx.box(g, style, w, h, phase());
		if (p > 0.01f) {
			float r = Math.min((float) ctx.theme.radius.get(), h * 0.5f);
			RenderUtils.fill(g, 0f, 0f, w, h, r, ColorUtil.fade(accent, 0.85f * p));
		}
		if (index == JUMP) {
			float barW = Math.min(w * 0.35f, 22f);
			RenderUtils.fill(g, (w - barW) * 0.5f, h * 0.5f - 0.75f, barW, 1.5f, 0.75f, ColorUtil.lerp(0xCCFFFFFF, 0xFFFFFFFF, p));
		} else {
			TextLabel label = labels[index];
			int color = ColorUtil.lerp(HudContext.TEXT, 0xFFFFFFFF, p);
			LiquidText.draw(g, label, (w - label.width()) * 0.5f, (h - label.capHeight()) * 0.5f, color, false);
		}
		pose.popMatrix();
	}
}
