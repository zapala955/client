package net.client.visuals.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Primitive drawing API. Everything is in float GUI units and honours the current pose (so scale /
 * translate / rotate animations just work) and the current scissor.
 *
 * <p>Shapes are signed-distance-field quads ({@code rounded_rect.fsh}): edges are anti-aliased to
 * exactly one device pixel at any GUI scale, and borders drawn with {@link #hairline} are one device
 * pixel thick (see {@link HighDpiEngine}).</p>
 *
 * <p>When the SDF pipeline is unavailable every primitive degrades to vanilla fills: chamfered corners
 * instead of true radii, layered rects instead of soft shadows. The UI stays usable and on-brand.</p>
 *
 * <p>An opacity stack ({@link #pushAlpha}) multiplies into every colour, including text drawn through
 * {@link LiquidText}, so a whole window can fade in/out with one call.</p>
 */
public final class RenderUtils {
	private static final float[] ALPHA_STACK = new float[16];
	private static int alphaDepth;
	private static float alpha = 1f;

	private RenderUtils() {
	}

	// ---- Opacity stack --------------------------------------------------------------------

	public static void pushAlpha(float factor) {
		ALPHA_STACK[alphaDepth++] = alpha;
		alpha *= Math.clamp(factor, 0f, 1f);
	}

	public static void popAlpha() {
		alpha = ALPHA_STACK[--alphaDepth];
	}

	public static float alpha() {
		return alpha;
	}

	static int applyAlpha(int argb) {
		return alpha >= 1f ? argb : ColorUtil.fade(argb, alpha);
	}

	/** One device pixel in GUI units. */
	public static float pixel() {
		return HighDpiEngine.px();
	}

	// ---- Fills ----------------------------------------------------------------------------

	public static void fill(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
		gradient(g, x, y, w, h, radius, color, color);
	}

	/** Square-cornered, sub-pixel positioned rect (dividers). */
	public static void rect(GuiGraphicsExtractor g, float x, float y, float w, float h, int color) {
		gradient(g, x, y, w, h, 0f, color, color);
	}

	/** Vertical gradient fill. */
	public static void gradient(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT, x, y, w, h, radius, 0f, 0f, top, bottom, false);
		} else {
			fallbackFill(g, x, y, w, h, radius, top, bottom);
		}
	}

	/** Horizontal gradient fill (left → right). */
	public static void gradientH(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int left, int right) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT, x, y, w, h, radius, 0f, 0f, left, right, true);
		} else {
			fallbackFill(g, x, y, w, h, radius, ColorUtil.lerp(left, right, 0.5f), ColorUtil.lerp(left, right, 0.5f));
		}
	}

	public static void circle(GuiGraphicsExtractor g, float cx, float cy, float r, int color) {
		fill(g, cx - r, cy - r, r * 2f, r * 2f, r, color);
	}

	/**
	 * A rounded line segment of the given thickness – the building block of vector icons.
	 * Arbitrary angles go through the pose, so the result is still a crisp SDF capsule.
	 */
	public static void line(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float thickness, int color) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len < 1.0e-4f) {
			circle(g, x0, y0, thickness * 0.5f, color);
			return;
		}
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x0, y0);
		pose.rotate((float) Math.atan2(dy, dx));
		fill(g, -thickness * 0.5f, -thickness * 0.5f, len + thickness, thickness, thickness * 0.5f, color);
		pose.popMatrix();
	}

	/**
	 * Circular arc built from overlapping capsules (angles in radians, 0 = up, clockwise).
	 * Draw with an opaque colour – overlaps are invisible then.
	 */
	public static void arc(GuiGraphicsExtractor g, float cx, float cy, float r, float start, float end, float thickness, int color) {
		int segments = Math.max(6, (int) Math.ceil(Math.abs(end - start) / (Math.PI / 14.0)));
		float step = (end - start) / segments;
		float px = cx + (float) Math.sin(start) * r;
		float py = cy - (float) Math.cos(start) * r;
		for (int i = 1; i <= segments; i++) {
			float a = start + step * i;
			float nx = cx + (float) Math.sin(a) * r;
			float ny = cy - (float) Math.cos(a) * r;
			line(g, px, py, nx, ny, thickness, color);
			px = nx;
			py = ny;
		}
	}

	// ---- Strokes --------------------------------------------------------------------------

	/** Ring of {@code thickness} drawn inside the rect's edge, with a vertical colour gradient. */
	public static void stroke(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float thickness,
			int top, int bottom) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT, x, y, w, h, radius, Math.max(thickness, 0.0625f), 0f, top, bottom, false);
		} else {
			fallbackOutline(g, x, y, w, h, top, bottom);
		}
	}

	/** A razor-thin border: exactly one device pixel at every GUI scale. */
	public static void hairline(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
		stroke(g, x, y, w, h, radius, HighDpiEngine.px(), top, bottom);
	}

	public static void ring(GuiGraphicsExtractor g, float cx, float cy, float r, float thickness, int color) {
		stroke(g, cx - r, cy - r, r * 2f, r * 2f, r, thickness, color, color);
	}

	// ---- Light & shadow -------------------------------------------------------------------

	/** Soft shadow; {@code softness} is the blur extent in GUI units. */
	public static void shadow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float softness, int color) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT, x, y, w, h, radius, 0f, Math.max(softness, 0.0625f), color, color, false);
		} else {
			// Three stacked translucent rects approximate a falloff.
			int layer = ColorUtil.fade(color, 0.33f);
			for (int i = 3; i >= 1; i--) {
				float grow = softness * i / 3f;
				fallbackFill(g, x - grow, y - grow, w + grow * 2f, h + grow * 2f, radius, layer, layer);
			}
		}
	}

	/** Additive soft light blob centred on the rect (ambient light, bloom). Skipped in fallback mode. */
	public static void glow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float softness, int color) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT_ADDITIVE, x, y, w, h, radius, 0f, Math.max(softness, 0.0625f), color, color, false);
		}
	}

	/** Additive glow that starts at the shape's edge and only extends outward. */
	public static void outerGlow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float softness, int color) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT_ADDITIVE, x, y, w, h, radius, -1f, Math.max(softness, 0.0625f), color, color, false);
		}
	}

	/** Additive crisp fill (specular sheen, inner glow). Gradient from {@code top} to {@code bottom}. */
	public static void light(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
		if (ShaderSupport.sdf()) {
			submit(g, LiquidPipelines.ROUNDED_RECT_ADDITIVE, x, y, w, h, radius, 0f, 0f, top, bottom, false);
		}
	}

	// ---- Internals ------------------------------------------------------------------------

	private static void submit(GuiGraphicsExtractor g, RenderPipeline pipeline, float x, float y, float w, float h,
			float radius, float stroke, float softness, int colorA, int colorB, boolean horizontal) {
		if (w <= 0f || h <= 0f) {
			return;
		}
		colorA = applyAlpha(colorA);
		colorB = applyAlpha(colorB);
		if ((colorA >>> 24) == 0 && (colorB >>> 24) == 0) {
			return;
		}
		RoundedRectState state = RoundedRectState.obtain();
		if (state.configure(pipeline, g.pose(), g.scissorStack.peek(), x, y, w, h, radius, stroke, softness, colorA, colorB, horizontal)) {
			g.guiRenderState.addGuiElement(state);
		} else {
			RoundedRectState.recycleLast();
		}
	}

	private static void fallbackFill(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
		top = applyAlpha(top);
		bottom = applyAlpha(bottom);
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + w);
		int y1 = Math.round(y + h);
		if (x1 <= x0 || y1 <= y0) {
			return;
		}
		int inset = radius >= 2f ? Math.min(3, Math.max(1, Math.round(radius / 4f))) : 0;
		if (inset == 0 || y1 - y0 <= inset * 2 || x1 - x0 <= inset * 2) {
			g.fillGradient(x0, y0, x1, y1, top, bottom);
			return;
		}
		// Chamfered corners: body + inset top/bottom strips.
		g.fillGradient(x0, y0 + inset, x1, y1 - inset, top, bottom);
		g.fill(x0 + inset, y0, x1 - inset, y0 + inset, top);
		g.fill(x0 + inset, y1 - inset, x1 - inset, y1, bottom);
	}

	private static void fallbackOutline(GuiGraphicsExtractor g, float x, float y, float w, float h, int top, int bottom) {
		top = applyAlpha(top);
		bottom = applyAlpha(bottom);
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + w);
		int y1 = Math.round(y + h);
		if (x1 - x0 < 2 || y1 - y0 < 2) {
			return;
		}
		g.fill(x0 + 1, y0, x1 - 1, y0 + 1, top);
		g.fill(x0 + 1, y1 - 1, x1 - 1, y1, bottom);
		g.fillGradient(x0, y0 + 1, x0 + 1, y1 - 1, top, bottom);
		g.fillGradient(x1 - 1, y0 + 1, x1, y1 - 1, top, bottom);
	}
}
