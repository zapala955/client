package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Active status effects: icon, name + level, time left (blinking in the last ten seconds).
 * Rows are refreshed in {@link #tick} (strings only change once a second).
 */
public final class PotionsElement extends HudModule {
	private static final int MAX_ROWS = 10;
	private static final float PAD = 5f;
	private static final float ROW = 14f;
	private static final float ICON = 11f;
	private static final String[] ROMAN = {"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};

	public final BooleanSetting icons = add(new BooleanSetting("icons", "liquidclient.hud.potions.icons", true));

	private final TextLabel[] names = new TextLabel[MAX_ROWS];
	private final TextLabel[] times = new TextLabel[MAX_ROWS];
	@SuppressWarnings("unchecked")
	private final Holder<MobEffect>[] effects = new Holder[MAX_ROWS];
	private final int[] nameKey = new int[MAX_ROWS];
	private final int[] secondsLeft = new int[MAX_ROWS];
	private final int[] colors = new int[MAX_ROWS];
	private int rows;

	public PotionsElement() {
		super("hud_potions", false, 0.995f, 0.40f);
		for (int i = 0; i < MAX_ROWS; i++) {
			names[i] = new TextLabel("", LiquidText.TextStyle.LABEL);
			times[i] = new TextLabel("", LiquidText.TextStyle.BODY);
			nameKey[i] = Integer.MIN_VALUE;
			secondsLeft[i] = Integer.MIN_VALUE;
		}
	}

	@Override
	public void tick() {
		LocalPlayer player = Minecraft.getInstance().player;
		rows = 0;
		if (player == null) {
			return;
		}
		for (MobEffectInstance instance : player.getActiveEffects()) {
			if (rows == MAX_ROWS) {
				break;
			}
			if (!instance.showIcon()) {
				continue;
			}
			setRow(rows++, instance.getEffect(), instance.getAmplifier(),
					instance.isInfiniteDuration() ? -1 : instance.getDuration() / 20);
		}
	}

	private void setRow(int row, Holder<MobEffect> effect, int amplifier, int seconds) {
		int key = System.identityHashCode(effect) * 31 + amplifier;
		if (key != nameKey[row] || effects[row] != effect) {
			nameKey[row] = key;
			effects[row] = effect;
			names[row].setText(effect.value().getDisplayName().getString() + (amplifier < ROMAN.length ? ROMAN[amplifier] : " " + (amplifier + 1)));
			colors[row] = 0xFF000000 | effect.value().getColor();
		}
		if (seconds != secondsLeft[row]) {
			secondsLeft[row] = seconds;
			times[row].setText(seconds < 0 ? "∞" : seconds / 60 + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60);
		}
	}

	@Override
	public void layout(HudContext ctx) {
		if (rows == 0 && ctx.editing) {
			setRow(0, MobEffects.SPEED, 1, 90);
			setRow(1, MobEffects.STRENGTH, 0, 45);
			rows = 2;
		}
		float lead = icons.get() ? ICON + 4f : 0f;
		float w = 0f;
		for (int i = 0; i < rows; i++) {
			w = Math.max(w, lead + names[i].width() + 6f + times[i].width());
		}
		width = PAD * 2f + Math.max(w, 40f);
		height = PAD * 2f + Math.max(1, rows) * ROW - 2f;
	}

	@Override
	public boolean visible(HudContext ctx) {
		return rows > 0 || ctx.editing;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		ctx.background(g, this, width, height);
		boolean showIcons = icons.get();
		for (int i = 0; i < rows; i++) {
			float cy = PAD + i * ROW + (ROW - 2f) * 0.5f;
			float x = PAD;
			float blink = secondsLeft[i] >= 0 && secondsLeft[i] <= 10 ? 0.55f + 0.45f * (float) Math.cos(ctx.time * 6f) : 1f;
			RenderUtils.pushAlpha(blink);
			if (showIcons && effects[i] != null) {
				g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(effects[i]), Math.round(x), Math.round(cy - ICON * 0.5f),
						Math.round(ICON), Math.round(ICON), ColorUtil.fade(0xFFFFFFFF, RenderUtils.alpha()));
				x += ICON + 4f;
			}
			ctx.textMid(g, names[i], x, cy, HudContext.TEXT);
			ctx.textMid(g, times[i], width - PAD - times[i].width(), cy, ColorUtil.lerpRgb(HudContext.TEXT_DIM, colors[i], 0.35f));
			RenderUtils.popAlpha();
		}
	}
}
