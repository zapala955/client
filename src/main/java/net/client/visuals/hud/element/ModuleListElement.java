package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.Module;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Module List: the modules that are on, as a stack of glass capsules sorted longest first (or A–Z),
 * aligned to whichever screen edge the element is anchored to. Modules slide in from that edge
 * when enabled and fold away when disabled; an accent bar marks the outer edge.
 *
 * <p>Per-module labels and animations are created once (the module set is fixed after init); the
 * frame loop only re-sorts an index array in place.</p>
 */
public final class ModuleListElement extends HudModule {
	public static final String[] SORT = {"length", "alphabetical"};
	private static final float ROW = 14f;
	private static final float GAP = 2f;
	private static final float PAD_X = 6f;

	public final BooleanSetting icons = add(new BooleanSetting("icons", "liquidclient.hud.module_list.icons", true));
	public final BooleanSetting bar = add(new BooleanSetting("bar", "liquidclient.hud.module_list.bar", true));
	public final ModeSetting sort = add(new ModeSetting("sort", "liquidclient.hud.module_list.sort", SORT, 0));

	private final List<Module> modules = new ArrayList<>();
	private TextLabel[] names = new TextLabel[0];
	private Smooth[] shown = new Smooth[0];
	private int[] order = new int[0];
	private float[] rowWidth = new float[0];

	public ModuleListElement() {
		super("hud_module_list", false, 0.995f, 0.16f);
	}

	private void ensureModules() {
		if (!modules.isEmpty()) {
			return;
		}
		for (Module module : ModuleManager.all()) {
			if (module.listed()) {
				modules.add(module);
			}
		}
		int n = modules.size();
		names = new TextLabel[n];
		shown = new Smooth[n];
		order = new int[n];
		rowWidth = new float[n];
		for (int i = 0; i < n; i++) {
			names[i] = new TextLabel(modules.get(i).displayName(), LiquidText.TextStyle.LABEL);
			shown[i] = new Smooth(modules.get(i).isEnabled() ? 1f : 0f, 12f);
			order[i] = i;
		}
	}

	@Override
	public void layout(HudContext ctx) {
		ensureModules();
		float lead = icons.get() ? 12f : 0f;
		float w = 0f;
		float h = 0f;
		for (int i = 0; i < modules.size(); i++) {
			boolean on = modules.get(i).isEnabled() || ctx.editing && i < 3;
			float a = shown[i].target(on).update(ctx.dt);
			rowWidth[i] = PAD_X * 2f + lead + names[i].width();
			if (a > 0.005f) {
				w = Math.max(w, rowWidth[i]);
				h += (ROW + GAP) * ease(a);
			}
		}
		sortRows();
		width = Math.max(40f, w);
		height = Math.max(ROW, h - GAP);
	}

	private static float ease(float a) {
		return 1f - (1f - a) * (1f - a) * (1f - a);
	}

	/** Insertion sort of {@link #order}: stable, in place, ~free for a dozen rows. */
	private void sortRows() {
		boolean byLength = sort.index() == 0;
		for (int i = 1; i < order.length; i++) {
			int item = order[i];
			int j = i - 1;
			while (j >= 0 && before(item, order[j], byLength)) {
				order[j + 1] = order[j];
				j--;
			}
			order[j + 1] = item;
		}
	}

	private boolean before(int a, int b, boolean byLength) {
		if (byLength) {
			return rowWidth[a] > rowWidth[b] + 0.01f;
		}
		return names[a].text().compareToIgnoreCase(names[b].text()) < 0;
	}

	@Override
	public boolean visible(HudContext ctx) {
		if (ctx.editing) {
			return true;
		}
		for (int i = 0; i < shown.length; i++) {
			if (shown[i].get() > 0.005f) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		boolean right = anchoredRight();
		boolean bottom = anchoredBottom();
		boolean showIcons = icons.get();
		float y = bottom ? height : 0f;
		for (int k = 0; k < order.length; k++) {
			int i = order[k];
			float a = shown[i].get();
			if (a <= 0.005f) {
				continue;
			}
			float e = ease(a);
			float rw = rowWidth[i];
			float slot = (ROW + GAP) * e;
			float rowY = bottom ? y - slot : y;
			y = bottom ? y - slot : y + slot;
			float slide = (1f - e) * (rw + 6f);
			float x = right ? width - rw + slide : -slide;
			int accent = ctx.accent(phase() + k * 0.07f);

			Matrix3x2fStack pose = g.pose();
			pose.pushMatrix();
			pose.translate(HighDpiEngine.snap(x), HighDpiEngine.snap(rowY));
			RenderUtils.pushAlpha(e);
			ctx.background(g, this, rw, ROW);
			if (bar.get()) {
				float bx = right ? rw - 2f : 0f;
				RenderUtils.fill(g, bx, 3f, 2f, ROW - 6f, 1f, accent);
			}
			float tx = PAD_X;
			if (showIcons) {
				ctx.icon(g, modules.get(i).icon(), tx + 4.5f, ROW * 0.5f, 8.5f, ColorUtil.lerpRgb(accent, 0xFFFFFFFF, 0.25f));
				tx += 12f;
			}
			ctx.textMid(g, names[i], tx, ROW * 0.5f, HudContext.TEXT);
			RenderUtils.popAlpha();
			pose.popMatrix();
		}
	}
}
