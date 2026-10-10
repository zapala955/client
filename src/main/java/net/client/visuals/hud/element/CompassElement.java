package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * A heading strip, like a game compass: ticks every 15°, cardinal letters, the accent marker in the
 * middle and edges that fade out. Shows ±90° around the view direction.
 */
public final class CompassElement extends HudModule {
	private static final String[] POINTS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

	public final NumberSetting stripWidth = add(new NumberSetting("width", "liquidclient.hud.compass.width", 100, 260, 5, 160));
	public final BooleanSetting degrees = add(new BooleanSetting("degrees", "liquidclient.hud.compass.degrees", true));

	private final TextLabel[] points = new TextLabel[POINTS.length];
	private final TextLabel heading = new TextLabel("0°", LiquidText.TextStyle.BODY);
	private int shownHeading = -1;

	public CompassElement() {
		super("hud_compass", false, 0.5f, 0.008f);
		for (int i = 0; i < POINTS.length; i++) {
			points[i] = new TextLabel(POINTS[i], i % 2 == 0 ? LiquidText.TextStyle.LABEL : LiquidText.TextStyle.BODY);
		}
	}

	@Override
	public void layout(HudContext ctx) {
		width = (float) stripWidth.get();
		height = degrees.get() ? 26f : 18f;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		ctx.background(g, this, width, height);
		LocalPlayer player = Minecraft.getInstance().player;
		float yaw = player != null ? player.getViewYRot(ctx.partialTick) : 180f + ctx.time * 20f;
		float facing = ((yaw + 180f) % 360f + 360f) % 360f;
		int accent = ctx.accent(phase());
		float center = width * 0.5f;
		float pxPerDegree = (width - 12f) / 180f;
		float stripY = 9f;

		g.enableScissor(0, 0, (int) Math.ceil(width), (int) Math.ceil(height));
		for (int tick = 0; tick < 360; tick += 15) {
			float offset = tick - facing;
			offset = ((offset + 180f) % 360f + 360f) % 360f - 180f;
			if (Math.abs(offset) > 92f) {
				continue;
			}
			float x = center + offset * pxPerDegree;
			float fade = 1f - (offset / 92f) * (offset / 92f);
			if (tick % 45 == 0) {
				TextLabel label = points[tick / 45];
				int color = tick == 0 ? accent : tick % 90 == 0 ? HudContext.TEXT : HudContext.TEXT_DIM;
				RenderUtils.pushAlpha(fade);
				LiquidText.draw(g, label, x - label.width() * 0.5f, stripY - label.capHeight() * 0.5f, color, false);
				RenderUtils.popAlpha();
			} else {
				float tickH = 3f;
				RenderUtils.fill(g, x - HighDpiEngine.px() * 0.5f - 0.25f, stripY - tickH * 0.5f, 0.5f + HighDpiEngine.px(), tickH, 0.3f,
						ColorUtil.fade(0x99FFFFFF, fade));
			}
		}
		g.disableScissor();

		// Centre marker: a small accent capsule above the strip.
		RenderUtils.fill(g, center - 3f, 1.5f, 6f, 1.5f, 0.75f, accent);

		if (degrees.get()) {
			int rounded = Math.round(facing) % 360;
			if (rounded != shownHeading) {
				shownHeading = rounded;
				heading.setText(rounded + "°");
			}
			ctx.text(g, heading, center - heading.width() * 0.5f, 17.5f, HudContext.TEXT_DIM);
		}
	}
}
