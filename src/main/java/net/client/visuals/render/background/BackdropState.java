package net.client.visuals.render.background;

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
 * Pooled render state for one wallpaper quad – the full-screen backdrop, or its frosted copy
 * masked to a glass panel. Recycled every frame like {@code RoundedRectState}.
 */
final class BackdropState implements GuiElementRenderState {
	private static final ArrayList<BackdropState> POOL = new ArrayList<>(16);
	private static int cursor;

	private final Matrix3x2f pose = new Matrix3x2f();
	private RenderPipeline pipeline = LiquidPipelines.BACKDROP_AURORA;
	private TextureSetup textureSetup = TextureSetup.noTexture();
	private @Nullable ScreenRectangle scissor;

	private float x0;
	private float y0;
	private float x1;
	private float y1;
	private int color;
	private float maskCenterX;
	private float maskCenterY;
	private float maskHalfW;
	private float maskHalfH;
	private float maskRadius;
	/** Procedural: time + screen height. Image: uv rect of the quad. */
	private boolean procedural;
	private float time;
	private float screenHeight;
	private float u0;
	private float v0;
	private float u1;
	private float v1;

	private @Nullable ScreenRectangle bounds;
	private int bx0 = Integer.MIN_VALUE;
	private int by0;
	private int bx1;
	private int by1;

	private BackdropState() {
	}

	static BackdropState obtain() {
		if (cursor == POOL.size()) {
			POOL.add(new BackdropState());
		}
		return POOL.get(cursor++);
	}

	static void recycleLast() {
		cursor--;
	}

	static void resetPool() {
		cursor = 0;
	}

	/** Quad geometry + mask. {@code maskRadius < 0} disables the mask. */
	BackdropState geometry(Matrix3x2fc pose, @Nullable ScreenRectangle scissor, float x, float y, float w, float h, float maskRadius) {
		this.pose.set(pose);
		this.scissor = scissor;
		this.x0 = x;
		this.y0 = y;
		this.x1 = x + w;
		this.y1 = y + h;
		this.maskHalfW = w * 0.5f;
		this.maskHalfH = h * 0.5f;
		this.maskCenterX = x + maskHalfW;
		this.maskCenterY = y + maskHalfH;
		this.maskRadius = maskRadius;
		return this;
	}

	BackdropState procedural(RenderPipeline pipeline, int color, float time, float screenHeight) {
		this.pipeline = pipeline;
		this.textureSetup = TextureSetup.noTexture();
		this.color = color;
		this.procedural = true;
		this.time = time;
		this.screenHeight = screenHeight;
		return this;
	}

	BackdropState image(TextureSetup textureSetup, int color, float u0, float v0, float u1, float v1) {
		this.pipeline = LiquidPipelines.BACKDROP_IMAGE;
		this.textureSetup = textureSetup;
		this.color = color;
		this.procedural = false;
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
		return this;
	}

	@Override
	public void buildVertices(VertexConsumer consumer) {
		vertex(consumer, x0, y0, u0, v0);
		vertex(consumer, x0, y1, u0, v1);
		vertex(consumer, x1, y1, u1, v1);
		vertex(consumer, x1, y0, u1, v0);
	}

	private void vertex(VertexConsumer consumer, float x, float y, float u, float v) {
		consumer.addVertexWith2DPose(pose, x, y)
				.setColor(color)
				.setUv(x - maskCenterX, y - maskCenterY)
				.setUv3(procedural ? time : u, procedural ? screenHeight : v)
				.setLineWidth(maskRadius)
				.setUv1(Math.round(maskHalfW * 8f), Math.round(maskHalfH * 8f))
				.setUv2(Math.round(maskCenterX * 8f), Math.round(maskCenterY * 8f));
	}

	/** Transformed AABB ∩ scissor; {@code false} when nothing is visible. */
	boolean updateBounds() {
		float m00 = pose.m00();
		float m01 = pose.m01();
		float m10 = pose.m10();
		float m11 = pose.m11();
		float m20 = pose.m20();
		float m21 = pose.m21();
		int ix0 = (int) Math.floor(m20 + Math.min(m00 * x0, m00 * x1) + Math.min(m10 * y0, m10 * y1));
		int ix1 = (int) Math.ceil(m20 + Math.max(m00 * x0, m00 * x1) + Math.max(m10 * y0, m10 * y1));
		int iy0 = (int) Math.floor(m21 + Math.min(m01 * x0, m01 * x1) + Math.min(m11 * y0, m11 * y1));
		int iy1 = (int) Math.ceil(m21 + Math.max(m01 * x0, m01 * x1) + Math.max(m11 * y0, m11 * y1));
		if (scissor != null) {
			ix0 = Math.max(ix0, scissor.left());
			iy0 = Math.max(iy0, scissor.top());
			ix1 = Math.min(ix1, scissor.right());
			iy1 = Math.min(iy1, scissor.bottom());
		}
		if (ix1 <= ix0 || iy1 <= iy0) {
			return false;
		}
		if (bounds == null || ix0 != bx0 || iy0 != by0 || ix1 != bx1 || iy1 != by1) {
			bx0 = ix0;
			by0 = iy0;
			bx1 = ix1;
			by1 = iy1;
			bounds = new ScreenRectangle(ix0, iy0, ix1 - ix0, iy1 - iy0);
		}
		return true;
	}

	@Override
	public RenderPipeline pipeline() {
		return pipeline;
	}

	@Override
	public TextureSetup textureSetup() {
		return textureSetup;
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
