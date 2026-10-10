package net.client.visuals.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

/**
 * The Dynamic Island's shape ({@code island.fsh}): a rounded body and an optional bubble beside it,
 * melted together while they're close. Drawn as one quad per layer – shadow, then the fill (black)
 * or Liquid Glass (the frame behind, blurred) – from pooled render states that are reset every frame
 * like {@link RoundedRectState}, so drawing it allocates nothing.
 *
 * <p>Coordinates are GUI units in the current pose: the body is centred on ({@code cx}, {@code cy})
 * with half size {@code hw} × {@code hh}; the bubble's centre is {@code bubbleX} to the right of the
 * body's centre (same height), radius {@code bubbleR} (0 = no bubble).</p>
 *
 * <p>Without the island shader the shape degrades to separate rounded primitives (no melting).</p>
 */
public final class IslandShape {
	private static final int FILL = 0;
	private static final int SHADOW = 1;
	private static final int GLASS = 2;

	private IslandShape() {
	}

	/** Solid island; {@code rim} 0..1 lights a hairline just inside the edge, brightest along the top. */
	public static void fill(GuiGraphicsExtractor g, float cx, float cy, float hw, float hh, float radius, float bubbleX, float bubbleR,
			int color, float rim) {
		if (!ShaderSupport.island()) {
			RenderUtils.fill(g, cx - hw, cy - hh, hw * 2f, hh * 2f, radius, color);
			if (bubbleR > 0.25f) {
				RenderUtils.circle(g, cx + bubbleX, cy, bubbleR, color);
			}
			return;
		}
		submit(g, FILL, null, cx, cy, hw, hh, radius, bubbleX, bubbleR, color, 0f, rim);
	}

	/** Soft shadow of the same shape; {@code softness} in GUI units. */
	public static void shadow(GuiGraphicsExtractor g, float cx, float cy, float hw, float hh, float radius, float bubbleX, float bubbleR,
			float softness, int color) {
		if (!ShaderSupport.island()) {
			RenderUtils.shadow(g, cx - hw, cy - hh, hw * 2f, hh * 2f, radius, softness, color);
			return;
		}
		submit(g, SHADOW, null, cx, cy, hw, hh, radius, bubbleX, bubbleR, color, softness, 0f);
	}

	/**
	 * Liquid Glass island: the game behind it, refracted at the rim, blurred by {@code blurPx} device
	 * pixels, with vibrancy and {@code tint} mixed in by {@code tintAmount}.
	 *
	 * @return false when glass can't be drawn this frame (no shader, no world behind): draw {@link #fill} instead
	 */
	public static boolean glass(GuiGraphicsExtractor g, float cx, float cy, float hw, float hh, float radius, float bubbleX, float bubbleR,
			int tint, float tintAmount, float blurPx) {
		if (!ShaderSupport.islandGlass() || Minecraft.getInstance().level == null) {
			return false;
		}
		TextureSetup capture = BackdropCapture.request();
		if (capture == null) {
			return false;
		}
		submit(g, GLASS, capture, cx, cy, hw, hh, radius, bubbleX, bubbleR, tint, blurPx, tintAmount);
		return true;
	}

	/** Frame boundary (from {@code GuiRenderStateMixin}). */
	public static void resetPool() {
		State.cursor = 0;
	}

	private static void submit(GuiGraphicsExtractor g, int mode, @Nullable TextureSetup textures, float cx, float cy, float hw, float hh,
			float radius, float bubbleX, float bubbleR, int color, float a, float b) {
		if (hw <= 0f || hh <= 0f) {
			return;
		}
		color = RenderUtils.applyAlpha(color);
		if ((color >>> 24) == 0) {
			return;
		}
		State state = State.obtain();
		if (state.configure(mode, textures, g.pose(), g.scissorStack.peek(), cx, cy, hw, hh, radius, bubbleX, bubbleR, color, a, b)) {
			g.guiRenderState.addGuiElement(state);
		} else {
			State.recycleLast();
		}
	}

	private static final class State implements GuiElementRenderState {
		private static final ArrayList<State> POOL = new ArrayList<>(8);
		private static int cursor;

		private final Matrix3x2f pose = new Matrix3x2f();
		private int mode;
		private TextureSetup textures = TextureSetup.noTexture();
		private @Nullable ScreenRectangle scissor;
		private float cx;
		private float cy;
		private float hw;
		private float hh;
		private float radius;
		private short bubbleX16;
		private short bubbleR16;
		private short a16;
		private short b1000;
		private int color;
		/** Quad extents around the body's centre (body, bubble, soft tail and an AA fringe). */
		private float left;
		private float right;
		private float extentY;
		private @Nullable ScreenRectangle bounds;
		private int bx0 = Integer.MIN_VALUE;
		private int by0;
		private int bx1;
		private int by1;

		static State obtain() {
			if (cursor == POOL.size()) {
				POOL.add(new State());
			}
			return POOL.get(cursor++);
		}

		static void recycleLast() {
			cursor--;
		}

		boolean configure(int mode, @Nullable TextureSetup textures, Matrix3x2fc pose, @Nullable ScreenRectangle scissor, float cx, float cy,
				float hw, float hh, float radius, float bubbleX, float bubbleR, int color, float a, float b) {
			this.mode = mode;
			this.textures = textures != null ? textures : TextureSetup.noTexture();
			this.pose.set(pose);
			this.scissor = scissor;
			this.cx = cx;
			this.cy = cy;
			this.hw = hw;
			this.hh = hh;
			this.radius = Math.max(0f, radius);
			boolean bubble = bubbleR > 0.05f;
			this.bubbleX16 = (short) Math.clamp(Math.round(bubbleX * 16f), -32000, 32000);
			this.bubbleR16 = (short) (bubble ? Math.clamp(Math.round(bubbleR * 16f), 0, 32000) : 0);
			this.a16 = (short) Math.clamp(Math.round(a * 16f), 0, 32000);
			this.b1000 = (short) Math.clamp(Math.round(b * 1000f), 0, 32000);
			this.color = color;
			float pad = (mode == SHADOW ? a : 0f) + 1.5f;
			// The melted neck never reaches beyond the two shapes' bounding box.
			this.left = Math.min(-hw, bubble ? bubbleX - bubbleR : -hw) - pad;
			this.right = Math.max(hw, bubble ? bubbleX + bubbleR : hw) + pad;
			this.extentY = Math.max(hh, bubble ? bubbleR : 0f) + pad;
			return updateBounds();
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			// Same corner order as vanilla's ColoredRectangleRenderState (TL, BL, BR, TR).
			vertex(consumer, left, -extentY);
			vertex(consumer, left, extentY);
			vertex(consumer, right, extentY);
			vertex(consumer, right, -extentY);
		}

		private void vertex(VertexConsumer consumer, float lx, float ly) {
			consumer.addVertexWith2DPose(pose, cx + lx, cy + ly)
					.setColor(color)
					.setUv(lx, ly)
					.setUv3(hw, hh)
					.setLineWidth(radius)
					.setUv1(bubbleX16, bubbleR16)
					.setUv2(a16, b1000);
		}

		private boolean updateBounds() {
			float x0 = cx + left;
			float y0 = cy - extentY;
			float x1 = cx + right;
			float y1 = cy + extentY;
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
			return switch (mode) {
				case SHADOW -> LiquidPipelines.ISLAND_SHADOW;
				case GLASS -> LiquidPipelines.ISLAND_GLASS;
				default -> LiquidPipelines.ISLAND;
			};
		}

		@Override
		public TextureSetup textureSetup() {
			return textures;
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
}
