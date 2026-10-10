package net.client.visuals.browser;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The floating browser screen in the world: placement, raycasting and geometry.
 *
 * <p>The screen is an upright rectangle facing the player who spawned it. Coordinates on the page are
 * found by intersecting a ray with its plane, which gives both crosshair clicks (player look ray) and
 * free-cursor interaction (a ray through the mouse pixel, see {@code BrowserScreen}).</p>
 *
 * <pre>
 *   ╭──────────────────────────────────╮  ← generated glass bezel texture (rounded, rim light)
 *   │ ● ● ●                            │  ← title strip with traffic lights
 *   │ ┌──────────────────────────────┐ │
 *   │ │          web page            │ │  ← live browser texture, 16:9
 *   │ └──────────────────────────────┘ │
 *   ╰──────────────────────────────────╯
 * </pre>
 */
public final class BrowserPanel {
	/** Bezel margins in units of the page width. */
	static final float SIDE = 0.03f;
	static final float TOP = 0.075f;
	static final float BOTTOM = 0.03f;
	static final float RADIUS = 0.035f;
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Hit on the page: u/v in 0..1 (top-left origin), distance along the ray. */
	public record Hit(double u, double v, double distance) {
	}

	private final double centerX;
	private final double centerY;
	private final double centerZ;
	/** Unit vectors: right (along the page's x), up, normal (towards the viewer). */
	private final double rx;
	private final double rz;
	private final double nx;
	private final double nz;
	private float width;
	private float aspect;

	private final Quad bezelFront = new Quad();
	private final Quad bezelBack = new Quad();
	private final Quad page = new Quad();

	private BrowserPanel(double cx, double cy, double cz, double fx, double fz, float width, float aspect) {
		this.centerX = cx;
		this.centerY = cy;
		this.centerZ = cz;
		double len = Math.sqrt(fx * fx + fz * fz);
		double dx = len > 1.0e-6 ? fx / len : 0.0;
		double dz = len > 1.0e-6 ? fz / len : -1.0;
		// right = forward × up = (-dz, 0, dx); normal points back at the viewer.
		this.rx = -dz;
		this.rz = dx;
		this.nx = -dx;
		this.nz = -dz;
		this.width = width;
		this.aspect = aspect;
	}

	/** Upright screen {@code distance} blocks in front of the entity's eyes, facing it. */
	public static BrowserPanel inFrontOf(Entity entity, float distance, float width, float aspect) {
		Vec3 eye = entity.getEyePosition();
		Vec3 look = entity.getViewVector(1f);
		double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
		double fx = horizontal > 1.0e-4 ? look.x : -Math.sin(Math.toRadians(entity.getYRot()));
		double fz = horizontal > 1.0e-4 ? look.z : Math.cos(Math.toRadians(entity.getYRot()));
		Vec3 center = eye.add(look.scale(distance));
		return new BrowserPanel(center.x, center.y, center.z, fx, fz, width, aspect);
	}

	public void setSize(float width, float aspect) {
		this.width = width;
		this.aspect = aspect;
	}

	public float width() {
		return width;
	}

	public float height() {
		return width / aspect;
	}

	public Vec3 center() {
		return new Vec3(centerX, centerY, centerZ);
	}

	/** Ray (origin, direction) against the page rectangle, front side only. */
	public @Nullable Hit raycast(double ox, double oy, double oz, double dx, double dy, double dz) {
		double denom = dx * nx + dz * nz;
		if (denom > -1.0e-6) {
			return null; // parallel or hitting the back
		}
		double t = ((centerX - ox) * nx + (centerZ - oz) * nz) / denom;
		if (t <= 0.0) {
			return null;
		}
		double px = ox + dx * t - centerX;
		double py = oy + dy * t - centerY;
		double pz = oz + dz * t - centerZ;
		double u = (px * rx + pz * rz) / width + 0.5;
		double v = 0.5 - py / height();
		if (u < 0.0 || u > 1.0 || v < 0.0 || v > 1.0) {
			return null;
		}
		return new Hit(u, v, t);
	}

	// ---- Geometry -----------------------------------------------------------------------------

	/** Submits bezel + page, translated relative to the camera. Render thread. */
	void submit(SubmitNodeCollector collector, PoseStack poseStack, double camX, double camY, double camZ,
			net.minecraft.client.renderer.rendertype.RenderType bezelType, net.minecraft.client.renderer.rendertype.RenderType pageType,
			boolean hovered) {
		float w = width;
		float h = height();
		float left = -w * 0.5f - SIDE * w;
		float right = w * 0.5f + SIDE * w;
		float top = h * 0.5f + TOP * w;
		float bottom = -h * 0.5f - BOTTOM * w;

		poseStack.pushPose();
		poseStack.translate((float) (centerX - camX), (float) (centerY - camY), (float) (centerZ - camZ));

		int bezelColor = hovered ? 0xFFFFFFFF : 0xFFD6DCE6;
		bezelFront.set(this, left, right, top, bottom, -0.004f, 0f, 0f, 1f, 1f, bezelColor, false);
		bezelBack.set(this, left, right, top, bottom, -0.006f, 0.5f, 0.97f, 0.5f, 0.97f, 0xFFB0B8C4, true);
		page.set(this, -w * 0.5f, w * 0.5f, h * 0.5f, -h * 0.5f, 0f, 0f, 0f, 1f, 1f, 0xFFFFFFFF, false);
		collector.submitCustomGeometry(poseStack, bezelType, bezelFront);
		collector.submitCustomGeometry(poseStack, bezelType, bezelBack);
		collector.submitCustomGeometry(poseStack, pageType, page);
		poseStack.popPose();
	}

	/** One textured quad in the panel's plane. Mutable and reused – nothing is allocated per frame. */
	private static final class Quad implements SubmitNodeCollector.CustomGeometryRenderer {
		private final float[] xyz = new float[12];
		private float u0;
		private float v0;
		private float u1;
		private float v1;
		private int color;

		/** Corners TL, BL, BR, TR (reversed for the back face), {@code depth} along the normal. */
		void set(BrowserPanel panel, float left, float right, float top, float bottom, float depth,
				float u0, float v0, float u1, float v1, int color, boolean back) {
			float rxf = (float) panel.rx;
			float rzf = (float) panel.rz;
			float nxf = (float) panel.nx * depth;
			float nzf = (float) panel.nz * depth;
			float l = back ? right : left;
			float r = back ? left : right;
			corner(0, l, top, rxf, rzf, nxf, nzf);
			corner(1, l, bottom, rxf, rzf, nxf, nzf);
			corner(2, r, bottom, rxf, rzf, nxf, nzf);
			corner(3, r, top, rxf, rzf, nxf, nzf);
			this.u0 = u0;
			this.v0 = v0;
			this.u1 = u1;
			this.v1 = v1;
			this.color = color;
		}

		private void corner(int i, float x, float y, float rxf, float rzf, float nxf, float nzf) {
			xyz[i * 3] = rxf * x + nxf;
			xyz[i * 3 + 1] = y;
			xyz[i * 3 + 2] = rzf * x + nzf;
		}

		@Override
		public void render(PoseStack.Pose pose, VertexConsumer buffer) {
			vertex(pose, buffer, 0, u0, v0);
			vertex(pose, buffer, 1, u0, v1);
			vertex(pose, buffer, 2, u1, v1);
			vertex(pose, buffer, 3, u1, v0);
		}

		private void vertex(PoseStack.Pose pose, VertexConsumer buffer, int i, float u, float v) {
			buffer.addVertex(pose, xyz[i * 3], xyz[i * 3 + 1], xyz[i * 3 + 2])
					.setColor(color)
					.setUv(u, v)
					.setLight(FULL_BRIGHT);
		}
	}

	// ---- Bezel texture ------------------------------------------------------------------------

	/** Glass bezel with rounded corners, rim light and traffic lights, drawn with an SDF on the CPU. */
	static NativeImage createBezel(float aspect) {
		float totalW = 1f + SIDE * 2f;
		float totalH = 1f / aspect + TOP + BOTTOM;
		int pw = 1024;
		int ph = Math.round(pw * totalH / totalW);
		float scale = pw / totalW; // px per page-width unit
		float radius = RADIUS * scale;
		NativeImage image = new NativeImage(pw, ph, true);
		float halfW = pw * 0.5f;
		float halfH = ph * 0.5f;
		float lightY = TOP * scale * 0.5f;
		float lightR = 0.0085f * scale;
		int[] lights = {0xFFFF5F57, 0xFFFEBC2E, 0xFF28C840};
		for (int y = 0; y < ph; y++) {
			for (int x = 0; x < pw; x++) {
				float px = x + 0.5f - halfW;
				float py = y + 0.5f - halfH;
				float d = roundedBox(px, py, halfW, halfH, radius);
				float coverage = clamp01(0.5f - d);
				if (coverage <= 0f) {
					continue;
				}
				float t = (float) y / ph;
				// Slate glass: slightly lighter at the top, rim light on the upper edge.
				float r = lerp(0x2A, 0x10, t);
				float g = lerp(0x36, 0x17, t);
				float b = lerp(0x4B, 0x2A, t);
				float rim = clamp01(1f - Math.abs(d + 1.5f) / 1.5f) * lerp(0.55f, 0.12f, t);
				r = lerp(r, 255f, rim);
				g = lerp(g, 255f, rim);
				b = lerp(b, 255f, rim);
				int argb = (Math.round(coverage * 0xF2) << 24) | (Math.round(r) << 16) | (Math.round(g) << 8) | Math.round(b);
				// Traffic lights in the title strip.
				for (int i = 0; i < 3; i++) {
					float cx = (SIDE + 0.022f + i * 0.026f) * scale;
					float dl = (float) Math.hypot(x + 0.5f - cx, y + 0.5f - lightY) - lightR;
					float lc = clamp01(0.5f - dl);
					if (lc > 0f) {
						argb = mix(argb, lights[i] | 0xFF000000, lc);
					}
				}
				image.setPixel(x, y, argb);
			}
		}
		return image;
	}

	private static float roundedBox(float px, float py, float hw, float hh, float r) {
		float qx = Math.abs(px) - hw + r;
		float qy = Math.abs(py) - hh + r;
		float outside = (float) Math.hypot(Math.max(qx, 0f), Math.max(qy, 0f));
		return Math.min(Math.max(qx, qy), 0f) + outside - r;
	}

	private static int mix(int a, int b, float t) {
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;
		return (a & 0xFF000000) | Math.round(lerp(ar, br, t)) << 16 | Math.round(lerp(ag, bg, t)) << 8 | Math.round(lerp(ab, bb, t));
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
}
