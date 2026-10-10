package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Worn armour (and optionally the held items) with durability as percent or remaining uses,
 * coloured from green to red.
 */
public final class ArmorElement extends HudModule {
	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
	};
	private static final float PAD = 4f;
	private static final float ITEM = 16f;

	public final ModeSetting layout = add(new ModeSetting("layout", "liquidclient.hud.armor.layout", new String[]{"vertical", "horizontal"}, 0));
	public final ModeSetting durability = add(new ModeSetting("durability", "liquidclient.hud.armor.durability",
			new String[]{"percent", "remaining", "none"}, 0));
	public final BooleanSetting held = add(new BooleanSetting("held", "liquidclient.hud.armor.held", true));

	private final TextLabel[] texts = new TextLabel[SLOTS.length];
	private final int[] shownKey = new int[SLOTS.length];
	private final int[] colors = new int[SLOTS.length];
	private final ItemStack[] stacks = new ItemStack[SLOTS.length];
	private int count;
	private final int[] order = new int[SLOTS.length];
	private final TextLabel placeholder = new TextLabel("Armor", LiquidText.TextStyle.BODY);

	public ArmorElement() {
		super("hud_armor", false, 0.995f, 0.62f);
		for (int i = 0; i < SLOTS.length; i++) {
			texts[i] = new TextLabel("", LiquidText.TextStyle.BODY);
			shownKey[i] = Integer.MIN_VALUE;
			stacks[i] = ItemStack.EMPTY;
		}
	}

	@Override
	public void layout(HudContext ctx) {
		LocalPlayer player = Minecraft.getInstance().player;
		count = 0;
		int slots = held.get() ? SLOTS.length : 4;
		for (int i = 0; i < slots; i++) {
			ItemStack stack = player != null ? player.getItemBySlot(SLOTS[i]) : ItemStack.EMPTY;
			stacks[i] = stack;
			if (stack.isEmpty()) {
				continue;
			}
			order[count++] = i;
			updateText(i, stack);
		}
		boolean vertical = layout.index() == 0;
		if (count == 0) {
			width = PAD * 2f + placeholder.width() + 20f;
			height = 20f;
			return;
		}
		float textW = 0f;
		for (int n = 0; n < count; n++) {
			textW = Math.max(textW, texts[order[n]].width());
		}
		boolean showText = durability.index() != 2;
		if (vertical) {
			width = PAD * 2f + ITEM + (showText && textW > 0f ? 4f + textW : 0f);
			height = PAD * 2f + count * (ITEM + 2f) - 2f;
		} else {
			float cell = Math.max(ITEM, showText ? textW : 0f) + 4f;
			width = PAD * 2f + count * cell - 4f;
			height = PAD * 2f + ITEM + (showText ? 9f : 0f);
		}
	}

	private void updateText(int i, ItemStack stack) {
		int mode = durability.index();
		int key;
		String text;
		if (stack.isDamageableItem()) {
			int max = stack.getMaxDamage();
			int left = max - stack.getDamageValue();
			float fraction = max > 0 ? left / (float) max : 1f;
			colors[i] = fraction > 0.5f ? ColorUtil.lerp(0xFFFBBF24, 0xFF34D399, (fraction - 0.5f) * 2f)
					: ColorUtil.lerp(0xFFF87171, 0xFFFBBF24, fraction * 2f);
			key = mode << 24 | left;
			if (key == shownKey[i]) {
				return;
			}
			text = mode == 0 ? Math.round(fraction * 100f) + "%" : mode == 1 ? Integer.toString(left) : "";
		} else {
			colors[i] = HudContext.TEXT;
			int c = stack.getCount();
			key = mode << 24 | 1 << 23 | c;
			if (key == shownKey[i]) {
				return;
			}
			text = c > 1 && mode != 2 ? Integer.toString(c) : "";
		}
		shownKey[i] = key;
		texts[i].setText(text);
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		ctx.background(g, this, width, height);
		if (count == 0) {
			// Editor / empty: a hint so the element can still be found and placed.
			RenderUtils.ring(g, PAD + 7f, height * 0.5f, 6f, 1f, 0x66FFFFFF);
			ctx.textMid(g, placeholder, PAD + 18f, height * 0.5f, HudContext.TEXT_DIM);
			return;
		}
		boolean vertical = layout.index() == 0;
		boolean showText = durability.index() != 2;
		float textW = 0f;
		for (int n = 0; n < count; n++) {
			textW = Math.max(textW, texts[order[n]].width());
		}
		float cell = Math.max(ITEM, showText ? textW : 0f) + 4f;
		for (int n = 0; n < count; n++) {
			int i = order[n];
			float x = vertical ? PAD : PAD + n * cell + (cell - 4f - ITEM) * 0.5f;
			float y = vertical ? PAD + n * (ITEM + 2f) : PAD;
			g.item(stacks[i], Math.round(x), Math.round(y));
			TextLabel text = texts[i];
			if (showText && !text.text().isEmpty()) {
				if (vertical) {
					ctx.textMid(g, text, x + ITEM + 4f, y + ITEM * 0.5f, colors[i]);
				} else {
					ctx.text(g, text, PAD + n * cell + (cell - 4f - text.width()) * 0.5f, y + ITEM + 2f, colors[i]);
				}
			}
		}
	}
}
