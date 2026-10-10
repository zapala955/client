package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.Module;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import org.joml.Matrix3x2fStack;

/**
 * Toasts for modules toggled with their keybind: icon tile, module name, new state and a small
 * switch that animates to it. Newest toasts sit at the element's anchored edge and push the older
 * ones away; each lives {@value #LIFETIME} s, then folds away.
 *
 * <p>A fixed pool of entries is recycled (oldest first), so posting allocates nothing but the
 * label strings of a module name it hasn't shown before.</p>
 */
public final class NotificationsElement extends HudModule {
	private static final int POOL = 4;
	private static final float LIFETIME = 2.2f;
	private static final float W = 156f;
	private static final float TOAST_H = 30f;
	private static final float GAP = 5f;
	private static final float TILE = 20f;

	private static NotificationsElement instance;

	private final Toast[] toasts = new Toast[POOL];
	private final Toast preview = new Toast();
	private final String enabledText;
	private final String disabledText;
	private long sequence;

	private static final class Toast {
		final TextLabel title = new TextLabel("", LiquidText.TextStyle.LABEL);
		final TextLabel state = new TextLabel("", LiquidText.TextStyle.CAPTION);
		final Smooth appear = new Smooth(0f, 13f);
		final Smooth knob = new Smooth(0f, 16f);
		int icon;
		int color;
		boolean on;
		float age = LIFETIME;
		long order;
		boolean live;
		/** Current fold-in height factor (eased appear), computed in layout. */
		float fold;
	}

	public NotificationsElement() {
		super("hud_notifications", true, 0.995f, 0.80f);
		for (int i = 0; i < POOL; i++) {
			toasts[i] = new Toast();
		}
		enabledText = I18n.get("liquidclient.notification.enabled");
		disabledText = I18n.get("liquidclient.notification.disabled");
		instance = this;
	}

	/** A module was toggled by its keybind. */
	public static void post(Module module) {
		NotificationsElement n = instance;
		if (n == null || !n.isEnabled()) {
			return;
		}
		Toast slot = null;
		for (Toast t : n.toasts) {
			if (t.live && t.title.text().equals(module.displayName()) && t.age < LIFETIME) {
				slot = t; // same module again: update it in place
				break;
			}
		}
		if (slot == null) {
			slot = n.toasts[0];
			for (Toast t : n.toasts) {
				if (!t.live) {
					slot = t;
					break;
				}
				if (t.order < slot.order) {
					slot = t;
				}
			}
			slot.appear.snap(0f);
			slot.knob.snap(module.isEnabled() ? 0f : 1f);
		}
		slot.title.setText(module.displayName());
		slot.on = module.isEnabled();
		slot.state.setText(slot.on ? n.enabledText : n.disabledText);
		slot.icon = module.icon();
		slot.color = module.category().color;
		slot.age = 0f;
		slot.order = ++n.sequence;
		slot.live = true;
	}

	@Override
	public void layout(HudContext ctx) {
		float h = 0f;
		for (Toast t : toasts) {
			if (!t.live) {
				continue;
			}
			t.age += ctx.dt;
			float a = t.appear.target(t.age < LIFETIME).update(ctx.dt);
			if (t.age >= LIFETIME && a < 0.01f) {
				t.live = false;
				continue;
			}
			t.fold = 1f - (1f - a) * (1f - a);
			h += (TOAST_H + GAP) * t.fold;
		}
		if (ctx.editing && h == 0f) {
			if (preview.title.text().isEmpty()) {
				preview.title.setText(I18n.get("liquidclient.module.zoom"));
				preview.state.setText(enabledText);
				preview.icon = 0xE1B6;
				preview.color = 0xFF64D2FF;
				preview.on = true;
			}
			preview.fold = 1f;
			preview.knob.snap(1f);
			h = TOAST_H + GAP;
		}
		width = W;
		height = Math.max(TOAST_H, h - GAP);
	}

	@Override
	public boolean visible(HudContext ctx) {
		if (ctx.editing) {
			return true;
		}
		for (Toast t : toasts) {
			if (t.live) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		boolean bottom = anchoredBottom();
		boolean any = false;
		// Newest first, stacked away from the anchored edge.
		float offset = 0f;
		long newerThan = Long.MAX_VALUE;
		for (int n = 0; n < POOL; n++) {
			Toast next = null;
			for (Toast t : toasts) {
				if (t.live && t.order < newerThan && (next == null || t.order > next.order)) {
					next = t;
				}
			}
			if (next == null) {
				break;
			}
			newerThan = next.order;
			any = true;
			float slotH = (TOAST_H + GAP) * next.fold;
			float y = bottom ? height - offset - TOAST_H : offset;
			drawToast(g, ctx, next, y);
			offset += slotH;
		}
		if (!any && ctx.editing) {
			drawToast(g, ctx, preview, 0f);
		}
	}

	private void drawToast(GuiGraphicsExtractor g, HudContext ctx, Toast t, float y) {
		float a = t.fold;
		if (a < 0.01f) {
			return;
		}
		float knob = t.knob.target(t.on).update(ctx.dt);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		float slide = (1f - a) * 14f * (anchoredRight() ? 1f : -1f);
		pose.translate(slide, y);
		RenderUtils.pushAlpha(a);
		ctx.background(g, this, W, TOAST_H);

		// Icon tile: category-coloured gradient with the module glyph.
		float tx = 6f;
		float ty = (TOAST_H - TILE) * 0.5f;
		RenderUtils.gradient(g, tx, ty, TILE, TILE, 6f, ColorUtil.lerpRgb(t.color, 0xFFFFFFFF, 0.18f), ColorUtil.lerpRgb(t.color, 0xFF000000, 0.22f));
		RenderUtils.stroke(g, tx, ty, TILE, TILE, 6f, 0.5f, 0x4DFFFFFF, 0x0DFFFFFF);
		IconFont.draw(g, t.icon, tx + TILE * 0.5f, ty + TILE * 0.5f, 11f, 0xFFFFFFFF, 0x40000000);

		float textX = tx + TILE + 7f;
		ctx.text(g, t.title.maxWidth(W - textX - 34f), textX, 7.5f, HudContext.TEXT);
		ctx.text(g, t.state, textX, 18.5f, ColorUtil.lerpRgb(HudContext.TEXT_DIM, HudContext.GOOD, knob));

		// Switch mirroring the new state.
		float sw = 20f;
		float sh = 11f;
		float sx = W - 8f - sw;
		float sy = (TOAST_H - sh) * 0.5f;
		RenderUtils.fill(g, sx, sy, sw, sh, sh * 0.5f, ColorUtil.lerp(0x40FFFFFF, 0xFF30D158, knob));
		RenderUtils.circle(g, sx + sh * 0.5f + (sw - sh) * knob, sy + sh * 0.5f, sh * 0.5f - 1.5f, 0xFFFFFFFF);
		RenderUtils.popAlpha();
		pose.popMatrix();
	}
}
