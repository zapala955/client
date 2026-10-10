package net.client.visuals.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

/**
 * Rounded glass that blurs the game behind it (in-game HUD). Uses {@link BackdropCapture} and the
 * {@code hud_blur} shader; pooled render states, reset every frame like {@code RoundedRectState}.
 */
public final class BlurPanel {
	private BlurPanel() {
	}

	/** Whether real blur can be drawn this frame (shader compiled and a frame to sample). */
	public static boolean available() {
		ShaderSupport.ensureValidated();
		return ShaderSupport.hudBlur();
	}

	/**
	 * @param tint       rgb tint, alpha = how much of it replaces the blurred image
	 * @param blurPx     blur radius in device pixels (0 = plain refraction / tint)
	 * @param saturation 1 = unchanged, &gt; 1 = vibrancy
	 * @param refraction lens strength at the rim (0 = flat glass); negative = brightness lift
	 * @return false if nothing could be drawn (caller should draw a fallback)
	 */
	public static boolean draw(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int tint, float blurPx,
			float saturation, float refraction) {
		if (!available() || net.minecraft.client.Minecraft.getInstance().level == null) {
			return false; // no world → the captured frame would be empty (menus draw their own backdrop)
		}
		TextureSetup capture = BackdropCapture.request();
		if (capture == null) {
			return false;
		}
		float opacity = RenderUtils.alpha();
		if (opacity <= 0.004f) {
			return true;
		}
		State state = State.obtain();
		if (state.configure(capture, g.pose(), g.scissorStack.peek(), x, y, w, h, radius, tint, blurPx, opacity, saturation, refraction)) {
			g.guiRenderState.addGuiElement(state);
		} else {
			State.recycleLast();
		}
		return true;
	}

	/** Frame boundary (from {@code GuiRenderStateMixin}). */
	public static void resetPool() {
		State.cursor = 0;
	}

	private static final class State implements GuiElementRenderState {
		private static final ArrayList<State> POOL = new ArrayList<>(32);
		private static int cursor;

		private final Matrix3x2f pose = new Matrix3x2f();
		private TextureSetup textures = TextureSetup.noTexture();
		private @Nullable ScreenRectangle scissor;
		private float cx;
		private float cy;
		private float hw;
		private float hh;
		private float radius;
		private int tint;
		private short blur16;
		private short opacity;
		private short saturation;
		private short refraction;
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

		boolean configure(TextureSetup textures, Matrix3x2fc pose, @Nullable ScreenRectangle scissor, float x, float y, float w, float h,
				float radius, int tint, float blurPx, float opacity, float saturation, float refraction) {
			this.textures = textures;
			this.pose.set(pose);
			this.scissor = scissor;
			this.hw = w * 0.5f;
			this.hh = h * 0.5f;
			this.cx = x + hw;
			this.cy = y + hh;
			this.radius = Math.max(0f, radius);
			this.tint = tint;
			this.blur16 = (short) Math.clamp(Math.round(blurPx * 16f), 0, 32000);
			this.opacity = (short) Math.clamp(Math.round(opacity * 10000f), 0, 10000);
			this.saturation = (short) Math.clamp(Math.round(saturation * 1000f), 0, 32000);
			this.refraction = (short) Math.clamp(Math.round(refraction * 1000f), -32000, 32000);
			return updateBounds();
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			float ex = hw + 1f;
			float ey = hh + 1f;
			vertex(consumer, -ex, -ey);
			vertex(consumer, -ex, ey);
			vertex(consumer, ex, ey);
			vertex(consumer, ex, -ey);
		}

		private void vertex(VertexConsumer consumer, float lx, float ly) {
			consumer.addVertexWith2DPose(pose, cx + lx, cy + ly)
					.setColor(tint)
					.setUv(lx, ly)
					.setUv3(hw, hh)
					.setLineWidth(radius)
					.setUv1(blur16, opacity)
					.setUv2(saturation, refraction);
		}

		private boolean updateBounds() {
			float x0 = cx - hw - 1f;
			float y0 = cy - hh - 1f;
			float x1 = cx + hw + 1f;
			float y1 = cy + hh + 1f;
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
			return LiquidPipelines.HUD_BLUR;
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
