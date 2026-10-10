package net.client.visuals.hud.island;

import net.client.visuals.hud.HudContext;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.media.MusicView;
import net.client.visuals.media.NowPlaying;
import net.client.visuals.module.utility.ZoomModule;
import net.client.visuals.render.ColorUtil;
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
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

/**
 * The island's live activities – things that last, shown in the compact pill (the most important one)
 * and the bubble (the next one):
 * <ul>
 *   <li>{@link Music} – album art and a level meter; the expanded "Now Playing" card on a new song.</li>
 *   <li>{@link Zoom} – the zoom factor while the zoom key is held.</li>
 *   <li>{@link Combat} – the face of what you're fighting and its health.</li>
 *   <li>{@link Health} – a beating heart when your own health is low.</li>
 *   <li>{@link Breath} – your air underwater, as a ring.</li>
 * </ul>
 */
final class LiveActivities {
	private LiveActivities() {
	}

	static int healthColor(float fraction) {
		return fraction > 0.5f
				? ColorUtil.lerpRgb(IslandDraw.YELLOW, IslandDraw.GREEN, (fraction - 0.5f) * 2f)
				: ColorUtil.lerpRgb(IslandDraw.RED, IslandDraw.YELLOW, fraction * 2f);
	}

	/** "7.5": half-heart steps, the way health is counted in Minecraft. */
	static String hearts(float health) {
		int halves = Math.max(0, Math.round(health));
		return (halves / 2) + (halves % 2 == 0 ? ".0" : ".5");
	}

	// ---- Music --------------------------------------------------------------------------------

	static final class Music extends IslandActivity {
		/** A paused song stays in the island this long, like iOS's Now Playing activity. */
		private static final float PAUSED_GRACE = 30f;
		private static final float BAR_W = 1.8f;
		private static final float BAR_GAP = 1.4f;
		private static final int BARS = 5;
		private static final float CARD_W = 236f;
		private static final float CARD_H = 74f;
		private static final float PAD = 12f;
		private static final float ART = 40f;

		final MusicView view = new MusicView();
		private String announcedKey = "";
		private float pausedFor = Float.MAX_VALUE;
		private boolean active;
		private boolean newSong;

		/** Every frame. */
		void update(float dt, boolean anyPlayer, boolean onlineArt) {
			view.update(dt, anyPlayer, onlineArt, "Not playing", "");
			NowPlaying track = view.track;
			pausedFor = track.active && track.playing ? 0f : Math.min(pausedFor + dt, Float.MAX_VALUE / 2f);
			active = track.active && pausedFor < PAUSED_GRACE;
			if (track.active && track.playing && !track.key.equals(announcedKey)) {
				announcedKey = track.key;
				newSong = true;
			}
		}

		boolean active() {
			return active;
		}

		/** A new song started since the last call. */
		boolean takeNewSong() {
			boolean n = newSong;
			newSong = false;
			return n;
		}

		private static int accent(HudContext ctx) {
			return AlbumArt.accent(ctx.accent());
		}

		@Override
		public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
			view.art(g, x, y, size, size * 0.28f, accent(ctx));
		}

		@Override
		public float trailingWidth() {
			return BARS * BAR_W + (BARS - 1) * BAR_GAP;
		}

		@Override
		public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
			view.bars(g, right - trailingWidth(), cy + 5f, 10f, BAR_W, BAR_GAP, BARS, accent(ctx), ctx.dt, ctx.time);
		}

		@Override
		public float expandedWidth() {
			return CARD_W;
		}

		@Override
		public float expandedHeight() {
			return CARD_H;
		}

		@Override
		public float expandedRadius() {
			return 22f;
		}

		@Override
		public float duration() {
			return 4.5f;
		}

		/**
		 * <pre>
		 *  ╭──────────────────────────────────────────╮
		 *  │ ┌────┐ Midnight City                ▂▅▇▃ │
		 *  │ │ art│ M83                               │
		 *  │ └────┘                                   │
		 *  │ 1:24 ━━━━━━━━━━━━━━──────────────── -2:39│
		 *  ╰──────────────────────────────────────────╯
		 * </pre>
		 */
		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			int accent = accent(ctx);
			RenderUtils.shadow(g, PAD, PAD + 1.5f, ART, ART, 9f, 4f, 0x99000000);
			view.art(g, PAD, PAD, ART, 9f, accent);
			float tx = PAD + ART + 11f;
			float meter = trailingWidth();
			float textW = w - tx - PAD - meter - 8f;
			view.clipped(g, view.headline, tx, PAD + 6f, textW, IslandDraw.WHITE, false, true);
			view.clipped(g, view.artist, tx, PAD + 19.5f, w - tx - PAD, IslandDraw.DIM, false, true);
			view.bars(g, w - PAD - meter, PAD + 14f, 13f, BAR_W, BAR_GAP, BARS, accent, ctx.dt, ctx.time);

			float cy = h - PAD;
			TextLabel elapsed = view.elapsedSmall;
			TextLabel remaining = view.remainingSmall;
			IslandDraw.textMid(g, elapsed, PAD, cy, IslandDraw.DIM);
			IslandDraw.textMid(g, remaining, w - PAD - remaining.width(), cy, IslandDraw.DIM);
			float bx = PAD + elapsed.width() + 7f;
			float bw = w - PAD - remaining.width() - 7f - bx;
			float bh = 3.5f;
			RenderUtils.fill(g, bx, cy - bh * 0.5f, bw, bh, bh * 0.5f, IslandDraw.TRACK);
			if (view.track.active) {
				float filled = Math.max(bh, bw * Math.clamp(view.smoothProgress(), 0f, 1f));
				RenderUtils.fill(g, bx, cy - bh * 0.5f, filled, bh, bh * 0.5f, IslandDraw.WHITE);
			}
		}
	}

	// ---- Zoom ---------------------------------------------------------------------------------

	static final class Zoom extends IslandActivity {
		private final TextLabel factor = new TextLabel("", LiquidText.TextStyle.LABEL);
		private int shownTenths = -1;
		private boolean active;

		void update() {
			float level = ZoomModule.level();
			active = level > 1.03f;
			int tenths = Math.round(level * 10f);
			if (active && tenths != shownTenths) {
				shownTenths = tenths;
				factor.setText((tenths / 10) + "." + (tenths % 10) + "×");
			}
		}

		boolean active() {
			return active;
		}

		@Override
		public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
			IconFont.draw(g, 0xE1B6, x + size * 0.5f, y + size * 0.5f, size * 0.8f, IslandDraw.YELLOW, 0);
		}

		@Override
		public float trailingWidth() {
			return factor.width();
		}

		@Override
		public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
			IslandDraw.textMid(g, factor, right - factor.width(), cy, IslandDraw.YELLOW);
		}
	}

	// ---- Combat -------------------------------------------------------------------------------

	static final class Combat extends IslandActivity {
		/** Ticks after the last hit the fight stays in the island. */
		private static final int FIGHT_TICKS = 80;
		private static final float BAR_W = 22f;

		private final TextLabel health = new TextLabel("", LiquidText.TextStyle.CAPTION);
		private final Smooth bar = new Smooth(1f, 12f);
		private int hitId = -1;
		private int ticksSinceHit = Integer.MAX_VALUE / 2;
		private @Nullable LivingEntity target;
		private @Nullable ClientLevel level;
		private boolean active;
		private float fraction = 1f;
		private int shownHalves = -1;
		/** Set when the target died this tick: the victim, for the elimination alert. */
		private @Nullable LivingEntity eliminated;

		void onAttack(Entity entity) {
			if (entity instanceof LivingEntity living && !living.isDeadOrDying()) {
				if (entity.getId() != hitId) {
					bar.snap(fraction(living));
				}
				hitId = entity.getId();
				ticksSinceHit = 0;
			}
		}

		/** Client tick. */
		void tick(Minecraft minecraft) {
			ClientLevel current = minecraft.level;
			if (current != level) {
				level = current;
				hitId = -1;
				target = null;
			}
			ticksSinceHit++;
			LivingEntity next = null;
			if (current != null && hitId >= 0 && ticksSinceHit < FIGHT_TICKS && current.getEntity(hitId) instanceof LivingEntity hit) {
				next = hit;
			}
			if (next != null && next.isDeadOrDying()) {
				eliminated = next;
				hitId = -1;
				next = null;
			}
			active = next != null;
			if (next != null) {
				target = next;
				fraction = fraction(next);
				int halves = Math.round(Math.max(0f, next.getHealth()));
				if (halves != shownHalves) {
					shownHalves = halves;
					health.setText(hearts(next.getHealth()));
				}
			}
		}

		boolean active() {
			return active;
		}

		@Nullable LivingEntity takeEliminated() {
			LivingEntity e = eliminated;
			eliminated = null;
			return e;
		}

		private static float fraction(LivingEntity entity) {
			return Math.clamp(Math.max(0f, entity.getHealth()) / Math.max(1f, entity.getMaxHealth()), 0f, 1f);
		}

		@Override
		public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
			face(g, target, x, y, size);
		}

		@Override
		public float trailingWidth() {
			return health.width() + 5f + BAR_W;
		}

		@Override
		public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
			float shown = bar.target(fraction).update(ctx.dt);
			int color = healthColor(shown);
			float bx = right - BAR_W;
			float bh = 4f;
			RenderUtils.fill(g, bx, cy - bh * 0.5f, BAR_W, bh, bh * 0.5f, IslandDraw.TRACK);
			RenderUtils.fill(g, bx, cy - bh * 0.5f, Math.max(bh, BAR_W * shown), bh, bh * 0.5f, color);
			IslandDraw.textMid(g, health, bx - 5f - health.width(), cy, color);
		}
	}

	/** A player's face, or crossed swords for anything else. */
	static void face(GuiGraphicsExtractor g, @Nullable LivingEntity entity, float x, float y, float size) {
		if (entity instanceof AbstractClientPlayer player) {
			int s = Math.round(size);
			int white = ColorUtil.fade(0xFFFFFFFF, RenderUtils.alpha());
			PlayerFaceExtractor.extractRenderState(g, player.getSkin(), Math.round(x), Math.round(y), s, white);
			if (entity.hurtTime > 0) {
				RenderUtils.fill(g, Math.round(x), Math.round(y), s, s, 1.5f, ColorUtil.fade(0xFFFF3B30, entity.hurtTime / 10f * 0.5f));
			}
		} else {
			IconFont.draw(g, 0xE2B4, x + size * 0.5f, y + size * 0.5f, size * 0.8f, IslandDraw.WHITE, 0);
		}
	}

	// ---- Low health ---------------------------------------------------------------------------

	static final class Health extends IslandActivity {
		/** At or below this share of max health the heart starts beating. */
		private static final float LOW = 0.3f;

		private final TextLabel hearts = new TextLabel("", LiquidText.TextStyle.LABEL);
		private int shownHalves = -1;
		private boolean active;
		private float fraction = 1f;

		void tick(@Nullable LocalPlayer player) {
			if (player == null) {
				active = false;
				return;
			}
			float hp = player.getHealth();
			float max = Math.max(1f, player.getMaxHealth());
			fraction = hp / max;
			active = hp > 0f && fraction <= LOW;
			int halves = Math.round(Math.max(0f, hp));
			if (halves != shownHalves) {
				shownHalves = halves;
				hearts.setText(hearts(hp));
			}
		}

		boolean active() {
			return active;
		}

		/** Faster as health drops: 90 bpm at the threshold, 150 near death. */
		private float bpm() {
			return 90f + 60f * (1f - Math.clamp(fraction / LOW, 0f, 1f));
		}

		/** A red glow around the island, pulsing with the heart. */
		int glow(HudContext ctx) {
			float beat = IslandDraw.heartbeat(ctx.time, bpm());
			return ColorUtil.fade(IslandDraw.RED, 0.16f + 0.3f * beat);
		}

		@Override
		public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
			// Scaled through the pose: icons are baked per pixel size, so animating the size would re-bake.
			float beat = IslandDraw.heartbeat(ctx.time, bpm());
			Matrix3x2fStack pose = g.pose();
			pose.pushMatrix();
			pose.translate(x + size * 0.5f, y + size * 0.5f);
			pose.scale(1f + 0.16f * beat, 1f + 0.16f * beat);
			IconFont.draw(g, 0xE0F2, 0f, 0f, size * 0.82f, IslandDraw.RED, 0);
			pose.popMatrix();
		}

		@Override
		public float trailingWidth() {
			return hearts.width();
		}

		@Override
		public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
			IslandDraw.textMid(g, hearts, right - hearts.width(), cy, IslandDraw.RED);
		}
	}

	// ---- Breath -------------------------------------------------------------------------------

	static final class Breath extends IslandActivity {
		private final Smooth air = new Smooth(1f, 10f);
		private boolean active;
		private float fraction = 1f;

		void tick(@Nullable LocalPlayer player) {
			if (player == null) {
				active = false;
				return;
			}
			int max = Math.max(1, player.getMaxAirSupply());
			fraction = Math.clamp(player.getAirSupply() / (float) max, 0f, 1f);
			active = fraction < 0.999f;
		}

		boolean active() {
			return active;
		}

		private int color() {
			return fraction < 0.25f ? IslandDraw.RED : IslandDraw.TEAL;
		}

		@Override
		public void leading(GuiGraphicsExtractor g, HudContext ctx, float x, float y, float size) {
			IconFont.draw(g, 0xE0B4, x + size * 0.5f, y + size * 0.5f, size * 0.8f, color(), 0);
		}

		@Override
		public float trailingWidth() {
			return 13f;
		}

		@Override
		public void trailing(GuiGraphicsExtractor g, HudContext ctx, float right, float cy) {
			float shown = air.target(fraction).update(ctx.dt);
			IslandDraw.ring(g, right - 6.5f, cy, 6.5f, 2.2f, shown, color());
		}
	}
}
