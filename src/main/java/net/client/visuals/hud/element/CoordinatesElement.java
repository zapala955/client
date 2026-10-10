package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Block coordinates, facing, and optionally the biome and the matching Nether / Overworld position.
 * Strings are rebuilt in {@link #tick} only when the block, facing or dimension changes.
 */
public final class CoordinatesElement extends HudModule {
	private static final float PAD = 6f;
	private static final float ROW = 11f;
	private static final float ICON = 10f;

	public final ModeSetting layout = add(new ModeSetting("layout", "liquidclient.hud.coords.layout", new String[]{"line", "stacked"}, 0));
	public final BooleanSetting facing = add(new BooleanSetting("facing", "liquidclient.hud.coords.facing", true));
	public final BooleanSetting biome = add(new BooleanSetting("biome", "liquidclient.hud.coords.biome", false));
	public final BooleanSetting nether = add(new BooleanSetting("nether", "liquidclient.hud.coords.nether", false));

	private final TextLabel keyX = new TextLabel("X", LiquidText.TextStyle.LABEL);
	private final TextLabel keyY = new TextLabel("Y", LiquidText.TextStyle.LABEL);
	private final TextLabel keyZ = new TextLabel("Z", LiquidText.TextStyle.LABEL);
	private final TextLabel valueX = new TextLabel("128", LiquidText.TextStyle.LABEL);
	private final TextLabel valueY = new TextLabel("64", LiquidText.TextStyle.LABEL);
	private final TextLabel valueZ = new TextLabel("-256", LiquidText.TextStyle.LABEL);
	private final TextLabel facingLabel = new TextLabel("North", LiquidText.TextStyle.BODY);
	private final TextLabel biomeLabel = new TextLabel("Plains", LiquidText.TextStyle.BODY);
	private final TextLabel netherLabel = new TextLabel("Nether 16, -32", LiquidText.TextStyle.BODY);

	private int lastX = Integer.MIN_VALUE;
	private int lastY;
	private int lastZ;
	private Direction lastFacing;
	private boolean lastNether;
	private long lastBiomeKey = Long.MIN_VALUE;

	public CoordinatesElement() {
		super("hud_coords", true, 0.005f, 0.092f);
	}

	@Override
	public void tick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		BlockPos pos = player.blockPosition();
		Direction dir = player.getDirection();
		boolean inNether = player.level().dimension() == Level.NETHER;
		if (pos.getX() != lastX || pos.getY() != lastY || pos.getZ() != lastZ || dir != lastFacing || inNether != lastNether) {
			lastX = pos.getX();
			lastY = pos.getY();
			lastZ = pos.getZ();
			lastFacing = dir;
			lastNether = inNether;
			valueX.setText(Integer.toString(lastX));
			valueY.setText(Integer.toString(lastY));
			valueZ.setText(Integer.toString(lastZ));
			facingLabel.setText(facingText(dir));
			netherLabel.setText(inNether
					? "Overworld " + lastX * 8 + ", " + lastZ * 8
					: "Nether " + Math.floorDiv(lastX, 8) + ", " + Math.floorDiv(lastZ, 8));
		}
		if (biome.get()) {
			long key = pos.asLong();
			if (key != lastBiomeKey) {
				lastBiomeKey = key;
				player.level().getBiome(pos).unwrapKey().ifPresent(k -> biomeLabel.setText(pretty(k.identifier().getPath())));
			}
		}
	}

	private static String facingText(Direction dir) {
		return switch (dir) {
			case NORTH -> "North −Z";
			case SOUTH -> "South +Z";
			case EAST -> "East +X";
			case WEST -> "West −X";
			default -> pretty(dir.getName());
		};
	}

	private static String pretty(String id) {
		StringBuilder out = new StringBuilder(id.length());
		boolean upper = true;
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (c == '_') {
				out.append(' ');
				upper = true;
			} else {
				out.append(upper ? Character.toUpperCase(c) : c);
				upper = false;
			}
		}
		return out.toString();
	}

	private boolean stacked() {
		return layout.index() == 1;
	}

	private int extraRows() {
		return (facing.get() && stacked() ? 1 : 0) + (biome.get() ? 1 : 0) + (nether.get() ? 1 : 0);
	}

	private float pairWidth(TextLabel key, TextLabel value) {
		return key.width() + 3.5f + value.width();
	}

	@Override
	public void layout(HudContext ctx) {
		float lead = ctx.labels() == 0 ? ICON + 4f : 0f;
		float w;
		if (stacked()) {
			float col = Math.max(pairWidth(keyX, valueX), Math.max(pairWidth(keyY, valueY), pairWidth(keyZ, valueZ)));
			w = col;
			if (facing.get()) {
				w = Math.max(w, facingLabel.width());
			}
			height = PAD * 2f - 2f + ROW * (3 + extraRows());
		} else {
			w = lead + pairWidth(keyX, valueX) + 7f + pairWidth(keyY, valueY) + 7f + pairWidth(keyZ, valueZ);
			if (facing.get()) {
				w += 8f + facingLabel.width();
			}
			height = 18f + ROW * extraRows();
		}
		if (biome.get()) {
			w = Math.max(w, biomeLabel.width());
		}
		if (nether.get()) {
			w = Math.max(w, netherLabel.width());
		}
		width = PAD * 2f + w;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		ctx.background(g, this, width, height);
		int accent = ctx.accent(phase());
		float x = PAD;
		float y;
		if (stacked()) {
			y = PAD - 1f + ROW * 0.5f;
			pair(g, ctx, keyX, valueX, x, y, accent);
			pair(g, ctx, keyY, valueY, x, y + ROW, accent);
			pair(g, ctx, keyZ, valueZ, x, y + ROW * 2f, accent);
			y += ROW * 3f;
			if (facing.get()) {
				ctx.textMid(g, facingLabel, x, y, HudContext.TEXT_DIM);
				y += ROW;
			}
		} else {
			y = 9f;
			if (ctx.labels() == 0) {
				ctx.icon(g, 0xE111, x + ICON * 0.5f, y, ICON, accent);
				x += ICON + 4f;
			}
			x = pair(g, ctx, keyX, valueX, x, y, accent) + 7f;
			x = pair(g, ctx, keyY, valueY, x, y, accent) + 7f;
			x = pair(g, ctx, keyZ, valueZ, x, y, accent);
			if (facing.get()) {
				ctx.textMid(g, facingLabel, x + 8f, y, HudContext.TEXT_DIM);
			}
			y = 18f + ROW * 0.5f - 3f;
		}
		if (biome.get()) {
			ctx.textMid(g, biomeLabel, PAD, y, HudContext.TEXT_DIM);
			y += ROW;
		}
		if (nether.get()) {
			ctx.textMid(g, netherLabel, PAD, y, HudContext.TEXT_DIM);
		}
	}

	private float pair(GuiGraphicsExtractor g, HudContext ctx, TextLabel key, TextLabel value, float x, float cy, int accent) {
		ctx.textMid(g, key, x, cy, accent);
		float vx = x + key.width() + 3.5f;
		ctx.textMid(g, value, vx, cy, HudContext.TEXT);
		return vx + value.width();
	}
}
