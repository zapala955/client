package net.client.visuals.hud;

import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A one-line readout – {@code [icon | name] value unit} – the shape of most HUD elements (FPS, ping,
 * CPS…). Subclasses only update the value text in {@link #refresh}, and only when it changed, so a
 * steady HUD formats no strings.
 */
public abstract class StatElement extends HudModule {
	protected static final float HEIGHT = 18f;
	protected static final float PAD = 6f;
	protected static final float GAP = 4f;
	protected static final float ICON = 10f;

	private final int icon;
	private final TextLabel name;
	private final boolean unitWithName;
	protected final TextLabel value = new TextLabel("", LiquidText.TextStyle.LABEL);
	protected final TextLabel unit = new TextLabel("", LiquidText.TextStyle.CAPTION);
	/** Value colour, or 0 for the default text colour. */
	protected int valueColor;
	private boolean showUnit;

	/**
	 * @param icon         Lucide codepoint shown in "icon" label mode
	 * @param name         shown in "text" label mode
	 * @param unitWithName keep the unit in "text" mode ("Ping 23 ms" vs "FPS 144")
	 */
	protected StatElement(String id, boolean enabledByDefault, float x, float y, int icon, String name, boolean unitWithName) {
		super(id, enabledByDefault, x, y);
		this.icon = icon;
		this.name = new TextLabel(name, LiquidText.TextStyle.LABEL);
		this.unitWithName = unitWithName;
	}

	/** Updates {@link #value} / {@link #unit} / {@link #valueColor}. */
	protected abstract void refresh(HudContext ctx);

	@Override
	public void layout(HudContext ctx) {
		refresh(ctx);
		int labels = ctx.labels();
		float lead = labels == 0 ? ICON + GAP : labels == 1 ? name.width() + GAP : 0f;
		showUnit = !unit.text().isEmpty() && (labels != 1 || unitWithName);
		width = PAD + lead + value.width() + (showUnit ? 2.5f + unit.width() : 0f) + PAD;
		height = HEIGHT;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		ctx.background(g, this, width, height);
		float cy = height * 0.5f;
		float x = PAD;
		int labels = ctx.labels();
		if (labels == 0) {
			ctx.icon(g, icon, x + ICON * 0.5f, cy, ICON, ctx.accent(phase()));
			x += ICON + GAP;
		} else if (labels == 1) {
			ctx.textMid(g, name, x, cy, ctx.accent(phase()));
			x += name.width() + GAP;
		}
		ctx.textMid(g, value, x, cy, valueColor != 0 ? valueColor : HudContext.TEXT);
		if (showUnit) {
			ctx.textMid(g, unit, x + value.width() + 2.5f, cy, HudContext.TEXT_DIM);
		}
	}

	/** Sets the value text if it differs (no allocation when steady beyond the caller's own). */
	protected void setValue(String text) {
		value.setText(text);
	}
}
