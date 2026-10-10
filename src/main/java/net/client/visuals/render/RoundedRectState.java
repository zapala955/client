package net.client.visuals.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.shader.LiquidPipelines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

/**
 * A mutable, pooled {@link GuiElementRenderState} for one SDF rounded rectangle.
 *
 * <p>Vanilla allocates a record plus a {@code Matrix3x2f} copy for every {@code fill()}. Because
 * {@code GuiRenderState} is reset every frame (see {@code GuiRenderStateMixin}), these states are
 * recycled from a pool instead: in steady state, submitting a primitive allocates nothing. The
 * cached {@link ScreenRectangle} bounds are only replaced when the integer bounds change, i.e.
 * while something is actually moving.</p>
 *
 * <p>Shape modes (decoded in {@code rounded_rect.fsh}):</p>
 * <ul>
 *   <li>stroke = 0, softness = 0 – crisp fill</li>
 *   <li>stroke &gt; 0 – ring of that thickness inside the edge</li>
 *   <li>stroke = 0, softness &gt; 0 – soft blob / drop shadow</li>
 *   <li>stroke &lt; 0, softness &gt; 0 – <b>outer</b> glow only (the inside stays untouched)</li>
 * </ul>
 */
public final class RoundedRectState implements GuiElementRenderState {
	private static final ArrayList<RoundedRectState> POOL = new ArrayList<>(256);
	private static int cursor;

	private final Matrix3x2f pose = new Matrix3x2f();
	private RenderPipeline pipeline = LiquidPipelines.ROUNDED_RECT;
	private @Nullable ScreenRectangle scissor;

	private float centerX;
	private float centerY;
	private float halfWidth;
	private float halfHeight;
	private float radius;
	/** Extra geometry around the shape so AA fringes and soft tails aren't clipped. */
	private float pad;
	private int stroke16;
	private int softness16;
	/** Start / end colours of the gradient (top→bottom, or left→right when horizontal). */
	private int colorA;
	private int colorB;
	private boolean horizontal;

	private @Nullable ScreenRectangle bounds;
	private int boundsX0 = Integer.MIN_VALUE;
	private int boundsY0;
	private int boundsX1;
	private int boundsY1;

	private RoundedRectState() {
	}

	/** Takes a state from the pool. Must be followed by {@link #configure} (or {@link #recycleLast}). */
	static RoundedRectState obtain() {
		if (cursor == POOL.size()) {
			POOL.add(new RoundedRectState());
		}
		return POOL.get(cursor++);
	}

	/** Returns the most recently obtained state when it ended up not being submitted. */
	static void recycleLast() {
		cursor--;
	}

	/** Called when vanilla resets the frame's GuiRenderState: every state is free again. */
	public static void resetPool() {
		cursor = 0;
	}

	/**
	 * @return {@code false} when the shape is fully clipped and must not be submitted
	 */
	boolean configure(RenderPipeline pipeline, Matrix3x2fc pose, @Nullable ScreenRectangle scissor,
			float x, float y, float width, float height, float radius,
			float stroke, float softness, int colorA, int colorB, boolean horizontal) {
		this.pipeline = pipeline;
		this.pose.set(pose);
		this.scissor = scissor;
		this.halfWidth = width * 0.5f;
		this.halfHeight = height * 0.5f;
		this.centerX = x + halfWidth;
		this.centerY = y + halfHeight;
		this.radius = Math.max(0f, radius);
		this.pad = softness + 1.5f;
		this.stroke16 = Math.round(Math.clamp(stroke, -2047f, 2047f) * 16f);
		this.softness16 = Math.round(Math.clamp(softness, 0f, 2047f) * 16f);
		this.colorA = colorA;
		this.colorB = colorB;
		this.horizontal = horizontal;
		return updateBounds();
	}

	@Override
	public void buildVertices(VertexConsumer consumer) {
		float ex = halfWidth + pad;
		float ey = halfHeight + pad;
		// Same corner order as vanilla's ColoredRectangleRenderState (TL, BL, BR, TR).
		vertex(consumer, -ex, -ey, colorA);
		vertex(consumer, -ex, ey, horizontal ? colorA : colorB);
		vertex(consumer, ex, ey, colorB);
		vertex(consumer, ex, -ey, horizontal ? colorB : colorA);
	}

	private void vertex(VertexConsumer consumer, float localX, float localY, int color) {
		consumer.addVertexWith2DPose(pose, centerX + localX, centerY + localY)
				.setColor(color)
				.setUv(localX, localY)
				.setUv3(halfWidth, halfHeight)
				.setLineWidth(radius)
				.setUv2(stroke16, softness16);
	}

	/** Axis-aligned bounds of the transformed quad, intersected with the scissor. */
	private boolean updateBounds() {
		float ex = halfWidth + pad;
		float ey = halfHeight + pad;
		float l = centerX - ex;
		float r = centerX + ex;
		float t = centerY - ey;
		float b = centerY + ey;

		float m00 = pose.m00();
		float m01 = pose.m01();
		float m10 = pose.m10();
		float m11 = pose.m11();
		float m20 = pose.m20();
		float m21 = pose.m21();
		// Extremes of an affine transform of a box are found per-axis from the matrix signs.
		float minX = m20 + Math.min(m00 * l, m00 * r) + Math.min(m10 * t, m10 * b);
		float maxX = m20 + Math.max(m00 * l, m00 * r) + Math.max(m10 * t, m10 * b);
		float minY = m21 + Math.min(m01 * l, m01 * r) + Math.min(m11 * t, m11 * b);
		float maxY = m21 + Math.max(m01 * l, m01 * r) + Math.max(m11 * t, m11 * b);

		int x0 = (int) Math.floor(minX);
		int y0 = (int) Math.floor(minY);
		int x1 = (int) Math.ceil(maxX);
		int y1 = (int) Math.ceil(maxY);
		if (scissor != null) {
			x0 = Math.max(x0, scissor.left());
			y0 = Math.max(y0, scissor.top());
			x1 = Math.min(x1, scissor.right());
			y1 = Math.min(y1, scissor.bottom());
		}
		if (x1 <= x0 || y1 <= y0) {
			return false;
		}
		if (bounds == null || x0 != boundsX0 || y0 != boundsY0 || x1 != boundsX1 || y1 != boundsY1) {
			boundsX0 = x0;
			boundsY0 = y0;
			boundsX1 = x1;
			boundsY1 = y1;
			bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
		}
		return true;
	}

	@Override
	public RenderPipeline pipeline() {
		return pipeline;
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.noTexture();
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return scissor;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		return bounds;
	}
}
