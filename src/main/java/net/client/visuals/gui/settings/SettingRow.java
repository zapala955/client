package net.client.visuals.gui.settings;

import net.client.visuals.gui.window.Ui;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * One row of a grouped settings list (macOS System Settings): the label on the left, a control on
 * the right. Disabled rows dim and ignore input; descriptions appear as a tooltip after a short
 * rest. Subclasses draw the control and handle its input.
 */
public abstract class SettingRow {
	public static final float H = 30f;
	protected static final float PAD = 12f;

	protected final SettingsScreen host;
	protected final TextLabel label;
	private final String searchKey;
	private BooleanSupplier enabled = () -> true;
	private BooleanSupplier visible = () -> true;
	protected final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final Smooth active = new Smooth(1f, Motion.TOGGLE);
	protected float x;
	protected float y;
	protected float w;
	protected float h = H;

	protected SettingRow(SettingsScreen host, String label) {
		this.host = host;
		this.label = new TextLabel(label, LiquidText.TextStyle.BODY);
		this.searchKey = label.toLowerCase(Locale.ROOT);
	}

	public SettingRow enabledWhen(BooleanSupplier condition) {
		this.enabled = condition;
		return this;
	}

	public SettingRow visibleWhen(BooleanSupplier condition) {
		this.visible = condition;
		return this;
	}

	public boolean enabled() {
		return enabled.getAsBoolean();
	}

	public boolean visible() {
		return visible.getAsBoolean();
	}

	public boolean matches(String query) {
		return searchKey.contains(query);
	}

	/** Row identity for de-duplicating search results (the same vanilla option on two pages). */
	public Object identity() {
		return this;
	}

	public float height() {
		return H;
	}

	/** Whether the whole row reacts to the pointer (links); controls only highlight themselves. */
	protected boolean clickableRow() {
		return false;
	}

	/** Width the control takes at the right edge (the label truncates before it). */
	protected abstract float controlWidth();

	/** Draws the control right-aligned at {@code right}, vertically centred on {@code cy}. */
	protected abstract void extractControl(GuiGraphicsExtractor g, float right, float cy, int mouseX, int mouseY, float hover);

	/** Tooltip lines (description); built lazily by subclasses. */
	protected List<TextLabel> description() {
		return List.of();
	}

	public final void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = height();
		boolean on = enabled();
		boolean over = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
		float hv = hover.target(on && over).update(UiFrame.dt);
		float a = active.target(on ? 1f : 0.4f).update(UiFrame.dt);
		RenderUtils.pushAlpha(a);
		if (clickableRow() && hv > 0.01f) {
			RenderUtils.fill(g, x + 3f, y + 2f, w - 6f, h - 4f, Ui.ROW_RADIUS - 3f, ColorUtil.fade(Ui.FILL_HOVER, hv * 1.4f));
		}
		extractLabel(g, x, y, w, h);
		extractControl(g, x + w - PAD, y + h * 0.5f, on ? mouseX : -10000, on ? mouseY : -10000, hv);
		RenderUtils.popAlpha();
		if (over) {
			List<TextLabel> lines = description();
			if (!lines.isEmpty()) {
				host.tooltip(this, lines);
			}
		}
	}

	protected void extractLabel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		label.maxWidth(Math.max(20f, w - PAD * 2f - controlWidth() - 10f));
		LiquidText.draw(g, label, x + PAD, y + (h - label.capHeight()) * 0.5f, Ui.TEXT, false);
	}

	public boolean mouseClicked(double mx, double my, int button) {
		return false;
	}

	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		return false;
	}

	public boolean mouseReleased(double mx, double my, int button) {
		return false;
	}

	protected boolean over(double mx, double my) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
