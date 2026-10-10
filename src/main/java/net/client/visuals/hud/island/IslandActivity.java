package net.client.visuals.hud.island;

import net.client.visuals.hud.HudContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Something the Dynamic Island can show. Two presentations, like the iPhone's:
 *
 * <ul>
 *   <li><b>Compact</b> – a live activity in the pill: a leading glyph at the left end and a trailing
 *       accessory at the right, the middle left empty. The leading glyph alone also fills the bubble
 *       when this is the second live activity.</li>
 *   <li><b>Expanded</b> – an alert or card: the island grows to {@link #expandedWidth()} ×
 *       {@link #expandedHeight()} and the content is drawn in that box at (0, 0).</li>
 * </ul>
 *
 * <p>Instances are long-lived (one per kind) and refilled in place, so the island allocates nothing
 * while it runs; labels only rebuild when their text changes.</p>
 */
public abstract class IslandActivity {
	/** The leading glyph, centred in a {@code size} × {@code size} box at ({@code x}, {@code y}). */
	public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
	}

	/** Width of the trailing accessory in the compact pill (GUI units). */
	public float trailingWidth() {
		return 0f;
	}

	/** The trailing accessory, right-aligned to {@code right} and centred on {@code cy}. */
	public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
	}

	public float expandedWidth() {
		return IslandDraw.ALERT_MIN_W;
	}

	public float expandedHeight() {
		return IslandDraw.ALERT_H;
	}

	/** Corner radius when expanded (alerts are pills). */
	public float expandedRadius() {
		return expandedHeight() * 0.5f;
	}

	/** The expanded content in a {@code w} × {@code h} box at (0, 0). */
	public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
	}

	/** Seconds an alert stays expanded. */
	public float duration() {
		return 2.5f;
	}
}
