package net.client.visuals.gui.widget;

import net.client.visuals.render.IconFont;
import net.client.visuals.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Menu icons. {@link #glyph} draws the Lucide line icon through {@link IconFont}; the SDF versions in
 * {@link #draw} (circles, rings, capsules, arcs) are the fallback when the icon font is unavailable.
 *
 * <p>Every icon is drawn centred on ({@code cx}, {@code cy}) inside a box of {@code s} GUI units.</p>
 */
public final class DockIcons {
	public enum Icon {
		SINGLEPLAYER(0xE19F), // user
		MULTIPLAYER(0xE0E8),  // globe
		OPTIONS(0xE154),      // settings
		QUIT(0xE140),         // power
		MODS(0xE29C),         // puzzle
		RELOG(0xE149),        // rotate-cw
		CLICKGUI(0xE29A);     // sliders-horizontal

		final int codepoint;

		Icon(int codepoint) {
			this.codepoint = codepoint;
		}
	}

	private DockIcons() {
	}

	/** Line icon (consistent stroke, soft shadow); falls back to the SDF glyph. */
	public static void glyph(GuiGraphicsExtractor g, Icon icon, float cx, float cy, float s, int color, int shadowColor) {
		if (!IconFont.draw(g, icon.codepoint, cx, cy, s, color, shadowColor)) {
			draw(g, icon, cx, cy, s * 0.85f, color);
		}
	}

	public static void draw(GuiGraphicsExtractor g, Icon icon, float cx, float cy, float s, int color) {
		float stroke = Math.max(1.2f, s * 0.085f);
		switch (icon) {
			case SINGLEPLAYER -> person(g, cx, cy, s, color);
			case MULTIPLAYER -> globe(g, cx, cy, s, stroke, color);
			case OPTIONS -> gear(g, cx, cy, s, stroke, color);
			case QUIT -> power(g, cx, cy, s, stroke, color);
			case MODS -> grid(g, cx, cy, s, color);
			case RELOG -> refresh(g, cx, cy, s, stroke, color);
			case CLICKGUI -> sliders(g, cx, cy, s, stroke, color);
		}
	}

	private static void person(GuiGraphicsExtractor g, float cx, float cy, float s, int color) {
		float head = s * 0.19f;
		RenderUtils.circle(g, cx, cy - s * 0.17f, head, color);
		float bodyW = s * 0.66f;
		float bodyH = s * 0.34f;
		RenderUtils.fill(g, cx - bodyW * 0.5f, cy + s * 0.1f, bodyW, bodyH, bodyH * 0.55f, color);
	}

	private static void globe(GuiGraphicsExtractor g, float cx, float cy, float s, float stroke, int color) {
		float r = s * 0.43f;
		RenderUtils.ring(g, cx, cy, r, stroke, color);
		RenderUtils.line(g, cx - r, cy, cx + r, cy, stroke * 0.9f, color);
		// Meridian: a ring squashed horizontally through the pose.
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.scale(0.45f, 1f);
		RenderUtils.ring(g, 0f, 0f, r, stroke / 0.45f * 0.9f, color);
		pose.popMatrix();
	}

	private static void gear(GuiGraphicsExtractor g, float cx, float cy, float s, float stroke, int color) {
		float inner = s * 0.23f;
		RenderUtils.ring(g, cx, cy, inner, s * 0.12f, color);
		Matrix3x2fStack pose = g.pose();
		for (int i = 0; i < 8; i++) {
			pose.pushMatrix();
			pose.translate(cx, cy);
			pose.rotate((float) (Math.PI / 4.0 * i));
			RenderUtils.fill(g, -s * 0.07f, -s * 0.45f, s * 0.14f, s * 0.2f, s * 0.035f, color);
			pose.popMatrix();
		}
		RenderUtils.ring(g, cx, cy, s * 0.33f, s * 0.1f, color);
	}

	private static void power(GuiGraphicsExtractor g, float cx, float cy, float s, float stroke, int color) {
		float r = s * 0.36f;
		float gap = (float) Math.toRadians(38);
		RenderUtils.arc(g, cx, cy + s * 0.04f, r, gap, (float) (Math.PI * 2.0) - gap, stroke, color);
		RenderUtils.line(g, cx, cy - s * 0.42f, cx, cy - s * 0.02f, stroke, color);
	}

	private static void grid(GuiGraphicsExtractor g, float cx, float cy, float s, int color) {
		float cell = s * 0.34f;
		float gap = s * 0.1f;
		float start = -(cell + gap * 0.5f);
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 2; col++) {
				RenderUtils.fill(g, cx + start + col * (cell + gap), cy + start + row * (cell + gap), cell, cell, cell * 0.3f, color);
			}
		}
	}

	private static void refresh(GuiGraphicsExtractor g, float cx, float cy, float s, float stroke, int color) {
		float r = s * 0.36f;
		float start = (float) Math.toRadians(40);
		float end = (float) Math.toRadians(320);
		RenderUtils.arc(g, cx, cy, r, start, end, stroke, color);
		// Arrowhead at the end of the arc, pointing along the direction of travel.
		float ex = cx + (float) Math.sin(end) * r;
		float ey = cy - (float) Math.cos(end) * r;
		float head = s * 0.17f;
		RenderUtils.line(g, ex, ey, ex + head, ey - head * 0.2f, stroke, color);
		RenderUtils.line(g, ex, ey, ex + head * 0.25f, ey + head, stroke, color);
	}

	private static void sliders(GuiGraphicsExtractor g, float cx, float cy, float s, float stroke, int color) {
		float half = s * 0.4f;
		float[] knobs = {0.3f, 0.7f, 0.45f};
		for (int i = 0; i < 3; i++) {
			float y = cy + (i - 1) * s * 0.3f;
			RenderUtils.line(g, cx - half, y, cx + half, y, stroke, color);
			float kx = cx - half + half * 2f * knobs[i];
			RenderUtils.circle(g, kx, y, s * 0.11f, color);
		}
	}
}
