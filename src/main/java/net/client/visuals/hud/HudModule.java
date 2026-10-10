package net.client.visuals.hud;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A HUD element: a module in the HUD category with a position, a scale and a style override.
 *
 * <h2>Position</h2>
 * Stored as screen fractions of an anchor point that depends on which third of the screen the
 * element sits in – left third: its left edge, middle: its centre, right third: its right edge (same
 * vertically). So an element in the top-right corner stays glued to that corner and grows towards
 * the centre when its content gets wider, at every window size and GUI scale.
 *
 * <p>Subclasses compute their size in {@link #layout} and draw at local (0, 0) in
 * {@link #render}; {@code HudManager} applies position and scale through the pose.</p>
 */
public abstract class HudModule extends Module {
	public static final String[] ELEMENT_STYLES = withTheme(HudStyle.IDS);

	public final ModeSetting style = add(new ModeSetting("style", "liquidclient.hud.style", ELEMENT_STYLES, 0));
	public final NumberSetting scale = add(new NumberSetting("scale", "liquidclient.hud.scale", 50, 200, 5, 100).suffix("%"));
	private final NumberSetting posX;
	private final NumberSetting posY;
	private final float defaultX;
	private final float defaultY;
	private final float phase;
	/** 0 → 1 as the element appears (scale + fade), back to 0 when it hides or is disabled. */
	final Smooth appear = new Smooth(0f, 11f);
	/** Unscaled content size, set by {@link #layout}. */
	protected float width = 40f;
	protected float height = 18f;

	protected HudModule(String id, boolean enabledByDefault, float defaultX, float defaultY) {
		super(id, Category.HUD);
		this.defaultX = defaultX;
		this.defaultY = defaultY;
		this.posX = addHidden(new NumberSetting("x", "liquidclient.hud.x", 0, 1, 0, defaultX));
		this.posY = addHidden(new NumberSetting("y", "liquidclient.hud.y", 0, 1, 0, defaultY));
		this.phase = (id.hashCode() & 0xFF) / 255f;
		if (enabledByDefault) {
			setEnabled(true);
		}
	}

	private static String[] withTheme(String[] ids) {
		String[] out = new String[ids.length + 1];
		out[0] = "theme";
		System.arraycopy(ids, 0, out, 1, ids.length);
		return out;
	}

	/** Computes {@link #width} / {@link #height}; called every frame before drawing. */
	public abstract void layout(HudContext ctx);

	/** Draws the element at local (0, 0) with size {@link #width} × {@link #height}. */
	public abstract void render(GuiGraphicsExtractor g, HudContext ctx);

	/** In game, elements may hide themselves (e.g. music when nothing plays). The editor shows all. */
	public boolean visible(HudContext ctx) {
		return true;
	}

	/** Client tick (20 Hz) for state that shouldn't be sampled per frame. */
	public void tick() {
	}

	public float width() {
		return width;
	}

	public float height() {
		return height;
	}

	public float scaleFactor() {
		return (float) scale.get() / 100f;
	}

	/** Hue offset so chroma accents differ between elements. */
	public float phase() {
		return phase;
	}

	// ---- Placement ----------------------------------------------------------------------------

	private static float anchor(float fraction) {
		return fraction < 1f / 3f ? 0f : fraction > 2f / 3f ? 1f : 0.5f;
	}

	/** Whether the element hangs from the right edge (stacks should grow towards the left). */
	protected boolean anchoredRight() {
		return anchor(posX.getFloat()) == 1f;
	}

	/** Whether the element sits on its bottom edge (stacks should grow upwards). */
	protected boolean anchoredBottom() {
		return anchor(posY.getFloat()) == 1f;
	}

	/** Left edge on screen (GUI units), kept fully on screen. */
	public float screenX(float screenWidth) {
		float w = width * scaleFactor();
		float fx = posX.getFloat();
		return Math.clamp(fx * screenWidth - anchor(fx) * w, 0f, Math.max(0f, screenWidth - w));
	}

	public float screenY(float screenHeight) {
		float h = height * scaleFactor();
		float fy = posY.getFloat();
		return Math.clamp(fy * screenHeight - anchor(fy) * h, 0f, Math.max(0f, screenHeight - h));
	}

	/** Places the element's top-left corner at ({@code x}, {@code y}) and re-picks its anchor. */
	public void moveTo(float x, float y, float screenWidth, float screenHeight) {
		float w = width * scaleFactor();
		float h = height * scaleFactor();
		float ax = anchor((x + w * 0.5f) / screenWidth);
		float ay = anchor((y + h * 0.5f) / screenHeight);
		posX.set(Math.clamp((x + ax * w) / screenWidth, 0f, 1f));
		posY.set(Math.clamp((y + ay * h) / screenHeight, 0f, 1f));
	}

	public void resetLayout() {
		posX.set(defaultX);
		posY.set(defaultY);
		scale.set(100);
		style.set(0);
	}
}
