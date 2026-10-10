package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Target HUD: the entity you're fighting – face (players) or spawn egg (mobs), name, an animated
 * health bar with a trailing "damage taken" segment and absorption in gold, health, distance and
 * armour. Follows the last entity you hit for a few seconds, optionally whatever living thing
 * you're looking at.
 *
 * <p>Target resolution and every string happen in {@link #tick} (20 Hz, rebuilt only when a value
 * changes); the frame loop only animates and draws.</p>
 */
public final class TargetHudElement extends HudModule {
	private static final float W = 152f;
	private static final float H = 44f;
	private static final float PAD = 6f;
	private static final float FACE = 32f;
	private static final int HIT_TICKS = 100;
	private static final int DEATH_TICKS = 16;

	private static TargetHudElement instance;

	public final BooleanSetting onLook = add(new BooleanSetting("on_look", "liquidclient.hud.target.on_look", true));
	public final BooleanSetting playersOnly = add(new BooleanSetting("players_only", "liquidclient.hud.target.players_only", false));

	private final TextLabel name = new TextLabel("", LiquidText.TextStyle.LABEL);
	private final TextLabel health = new TextLabel("", LiquidText.TextStyle.LABEL);
	private final TextLabel distance = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel armor = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final Smooth bar = new Smooth(1f, 9f);
	private final Smooth trail = new Smooth(1f, 2.6f);
	private final Smooth absorb = new Smooth(0f, 9f);

	private int hitId = -1;
	private int ticksSinceHit = Integer.MAX_VALUE / 2;
	private int deadTicks;
	private @Nullable LivingEntity target;
	private @Nullable LivingEntity labelled;
	private @Nullable ClientLevel level;
	private boolean active;
	private float healthFraction = 1f;
	private float absorbFraction;
	private int shownHealth = Integer.MIN_VALUE;
	private int shownDistance = Integer.MIN_VALUE;
	private int shownArmor = Integer.MIN_VALUE;
	private @Nullable EntityType<?> eggType;
	private ItemStack egg = ItemStack.EMPTY;
	private float trailHold;

	public TargetHudElement() {
		super("hud_target", false, 0.5f, 0.62f);
		instance = this;
	}

	/** The local player attacked {@code entity} (client thread). */
	public static void onAttack(Entity entity) {
		TargetHudElement t = instance;
		if (t != null && entity instanceof LivingEntity && (!t.playersOnly.get() || entity instanceof Player)) {
			t.hitId = entity.getId();
			t.ticksSinceHit = 0;
		}
	}

	@Override
	public void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel currentLevel = minecraft.level;
		if (currentLevel != level) {
			level = currentLevel;
			target = null;
			labelled = null;
			hitId = -1;
		}
		ticksSinceHit++;
		LivingEntity next = null;
		if (currentLevel != null && player != null) {
			if (hitId >= 0 && ticksSinceHit < HIT_TICKS && currentLevel.getEntity(hitId) instanceof LivingEntity hit) {
				next = hit;
			} else if (onLook.get() && minecraft.crosshairPickEntity instanceof LivingEntity looked && !looked.isInvisible()
					&& (!playersOnly.get() || looked instanceof Player)) {
				next = looked;
			}
		}
		if (next != null && next.isDeadOrDying()) {
			deadTicks++;
			if (deadTicks > DEATH_TICKS) {
				hitId = -1;
				next = null;
			}
		} else {
			deadTicks = 0;
		}
		active = next != null;
		if (next == null) {
			return; // keep drawing the last target while the element fades out
		}
		target = next;
		refreshLabels(next, player);
	}

	private void refreshLabels(LivingEntity entity, @Nullable LocalPlayer player) {
		if (entity != labelled) {
			labelled = entity;
			name.setText(entity.getDisplayName().getString());
			shownHealth = Integer.MIN_VALUE;
			EntityType<?> type = entity.getType();
			if (type != eggType) {
				eggType = type;
				egg = entity instanceof AbstractClientPlayer ? ItemStack.EMPTY
						: SpawnEggItem.byId(type).map(ItemStack::new).orElse(ItemStack.EMPTY);
			}
			bar.snap(fraction(entity));
			trail.snap(bar.get());
		}
		float max = Math.max(1f, entity.getMaxHealth());
		float hp = Math.max(0f, entity.getHealth());
		healthFraction = Math.clamp(hp / max, 0f, 1f);
		absorbFraction = Math.clamp(entity.getAbsorptionAmount() / max, 0f, 1f);
		int hpTenths = Math.round((hp + entity.getAbsorptionAmount()) * 10f);
		if (hpTenths != shownHealth) {
			shownHealth = hpTenths;
			health.setText(hpTenths % 10 == 0 ? Integer.toString(hpTenths / 10) : String.format(Locale.ROOT, "%.1f", hpTenths / 10f));
		}
		int dist = player == null ? 0 : Math.round(player.distanceTo(entity) * 10f);
		if (dist != shownDistance) {
			shownDistance = dist;
			distance.setText(String.format(Locale.ROOT, "%.1f m", dist / 10f));
		}
		int armorValue = entity.getArmorValue();
		if (armorValue != shownArmor) {
			shownArmor = armorValue;
			armor.setText(Integer.toString(armorValue));
		}
	}

	private static float fraction(LivingEntity entity) {
		return Math.clamp(Math.max(0f, entity.getHealth()) / Math.max(1f, entity.getMaxHealth()), 0f, 1f);
	}

	@Override
	public void layout(HudContext ctx) {
		width = W;
		height = H;
		if (ctx.editing && target == null) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				refreshLabels(player, null);
				target = player;
			}
		}
	}

	@Override
	public boolean visible(HudContext ctx) {
		return ctx.editing || active;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		LivingEntity entity = target;
		if (entity == null) {
			return;
		}
		float dt = ctx.dt;
		ctx.background(g, this, W, H);

		// Health animation: the bar follows quickly; the trail waits a beat, then drains.
		float current = healthFraction;
		if (current >= trail.get()) {
			trail.snap(current);
			trailHold = 0f;
		} else if (bar.target() - current > 0.001f) {
			trailHold = 0.35f; // a fresh hit restarts the hold
		}
		float shown = bar.target(current).update(dt);
		trailHold = Math.max(0f, trailHold - dt);
		float trailed = trailHold > 0f ? trail.get() : trail.target(current).update(dt);
		float absorbed = absorb.target(absorbFraction).update(dt);

		// Face / icon tile
		float fx = PAD;
		float fy = (H - FACE) * 0.5f;
		RenderUtils.fill(g, fx, fy, FACE, FACE, 8f, 0x40000000);
		int white = ColorUtil.fade(0xFFFFFFFF, RenderUtils.alpha());
		if (entity instanceof AbstractClientPlayer playerTarget) {
			float inset = 3f;
			PlayerFaceExtractor.extractRenderState(g, playerTarget.getSkin(), Math.round(fx + inset), Math.round(fy + inset),
					Math.round(FACE - inset * 2f), white);
		} else if (!egg.isEmpty()) {
			if (RenderUtils.alpha() > 0.6f) {
				g.item(egg, Math.round(fx + FACE * 0.5f - 8f), Math.round(fy + FACE * 0.5f - 8f));
			}
		} else {
			IconFont.draw(g, 0xE221, fx + FACE * 0.5f, fy + FACE * 0.5f, 15f, HudContext.TEXT, 0x40000000);
		}
		if (entity.hurtTime > 0) {
			RenderUtils.fill(g, fx, fy, FACE, FACE, 8f, ColorUtil.fade(0xFFFF3B30, entity.hurtTime / 10f * 0.45f));
		}
		RenderUtils.stroke(g, fx, fy, FACE, FACE, 8f, HighDpiEngine.px(), 0x40FFFFFF, 0x0DFFFFFF);

		// Name + health
		float tx = fx + FACE + 8f;
		float right = W - PAD - 2f;
		float heartSize = 8f;
		float healthX = right - health.width();
		int hpColor = healthColor(shown);
		ctx.text(g, name.maxWidth(healthX - heartSize - 6f - tx), tx, PAD + 3f, HudContext.TEXT);
		ctx.text(g, health, healthX, PAD + 3f, hpColor);
		ctx.icon(g, 0xE0F2, healthX - heartSize * 0.5f - 3f, PAD + 3f + health.capHeight() * 0.5f, heartSize, hpColor);

		// Bar: track, trail, health, absorption
		float bx = tx;
		float by = PAD + 15f;
		float bw = right - tx;
		float bh = 5f;
		RenderUtils.fill(g, bx, by, bw, bh, bh * 0.5f, 0x33FFFFFF);
		if (trailed > shown + 0.002f) {
			RenderUtils.fill(g, bx, by, bw * trailed, bh, bh * 0.5f, 0x99FFFFFF);
		}
		if (shown > 0.002f) {
			RenderUtils.outerGlow(g, bx, by, bw * shown, bh, bh * 0.5f, 3f, ColorUtil.fade(hpColor, 0.35f));
			RenderUtils.gradientH(g, bx, by, Math.max(bh, bw * shown), bh, bh * 0.5f, ColorUtil.lerpRgb(hpColor, 0xFFFFFFFF, 0.2f), hpColor);
		}
		if (absorbed > 0.002f) {
			float aw = Math.max(bh, bw * Math.min(absorbed, 1f));
			RenderUtils.fill(g, bx, by + bh + 1.5f, aw, 1.5f, 0.75f, 0xFFFFD60A);
		}

		// Caption: distance · armour
		float cy = PAD + 27f;
		ctx.text(g, distance, tx, cy, HudContext.TEXT_DIM);
		if (shownArmor > 0) {
			float ax = tx + distance.width() + 9f;
			ctx.icon(g, 0xE158, ax + 3.5f, cy + distance.capHeight() * 0.5f, 7.5f, HudContext.TEXT_DIM);
			ctx.text(g, armor, ax + 9f, cy, HudContext.TEXT_DIM);
		}
	}

	private static int healthColor(float fraction) {
		return fraction > 0.5f
				? ColorUtil.lerpRgb(HudContext.WARN, HudContext.GOOD, (fraction - 0.5f) * 2f)
				: ColorUtil.lerpRgb(HudContext.BAD, HudContext.WARN, fraction * 2f);
	}
}
