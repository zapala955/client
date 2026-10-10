package net.client.visuals.hud.island;

import net.client.visuals.hud.HudContext;
import net.client.visuals.module.Module;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The island's alerts: things that happen, shown expanded for a few seconds. Each is a wide pill –
 * an app-icon tile (or a face, an item, a picture), a title and a caption, and sometimes an
 * accessory on the right: the module's switch, signal bars, a progress ring, a streak badge.
 */
final class Alerts {
	private Alerts() {
	}

	private static TextLabel title() {
		return new TextLabel("", LiquidText.TextStyle.LABEL);
	}

	private static TextLabel caption() {
		return new TextLabel("", LiquidText.TextStyle.CAPTION);
	}

	private static float textWidth(TextLabel a, TextLabel b) {
		return Math.max(a.width(), b.width());
	}

	private static float tileY(float h) {
		return (h - IslandDraw.TILE) * 0.5f;
	}

	private static float textX() {
		return IslandDraw.ALERT_PAD + IslandDraw.TILE + IslandDraw.ALERT_GAP;
	}

	// ---- Module toggled -----------------------------------------------------------------------

	/** A module switched by its keybind: its tile in the category colour, its name and its switch flipping. */
	static final class Toggle extends IslandActivity {
		private static final float SWITCH_W = 26f;
		private final TextLabel name = title();
		private final TextLabel state = caption();
		private final Smooth knob = new Smooth(0f, Motion.TOGGLE);
		private @Nullable Module module;
		private int icon;
		private int color;
		private boolean on;

		void set(Module m, String enabled, String disabled) {
			if (module != m) {
				// The switch starts where it was, so it visibly flips when the island opens.
				knob.snap(m.isEnabled() ? 0f : 1f);
			}
			module = m;
			on = m.isEnabled();
			name.setText(m.displayName());
			state.setText(on ? enabled : disabled);
			icon = m.icon();
			color = m.category().color;
			float room = IslandDraw.alertTextRoom(IslandDraw.TILE, SWITCH_W);
			name.maxWidth(room);
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(name, state), SWITCH_W);
		}

		@Override
		public float duration() {
			return 1.8f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			float k = knob.target(on).update(ctx.dt);
			IslandDraw.tile(g, IslandDraw.ALERT_PAD, tileY(h), IslandDraw.TILE, color, icon);
			IslandDraw.lines(g, name, state, textX(), h, ColorUtil.lerpRgb(IslandDraw.DIM, IslandDraw.GREEN, k));
			IslandDraw.toggle(g, w - IslandDraw.ALERT_RIGHT - SWITCH_W, h * 0.5f, k);
		}
	}

	// ---- Plain notice (dimension changes) -----------------------------------------------------

	static final class Notice extends IslandActivity {
		private final TextLabel heading = title();
		private final TextLabel detail = caption();
		private int color;
		private int icon;
		private float seconds = 3f;

		void set(String title, String subtitle, int tileColor, int tileIcon, float duration) {
			heading.setText(title);
			detail.setText(subtitle);
			heading.maxWidth(IslandDraw.alertTextRoom(IslandDraw.TILE, 0f));
			detail.maxWidth(IslandDraw.alertTextRoom(IslandDraw.TILE, 0f));
			color = tileColor;
			icon = tileIcon;
			seconds = duration;
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(heading, detail), 0f);
		}

		@Override
		public float duration() {
			return seconds;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			IslandDraw.tile(g, IslandDraw.ALERT_PAD, tileY(h), IslandDraw.TILE, color, icon);
			IslandDraw.lines(g, heading, detail, textX(), h, IslandDraw.DIM);
		}
	}

	// ---- Joined a server ----------------------------------------------------------------------

	/** The server's name and address, and its latency as signal bars (live while it shows). */
	static final class Server extends IslandActivity {
		private static final float BARS_W = 14f;
		private final TextLabel name = title();
		private final TextLabel address = caption();
		private String host = "";
		private String connected = "";
		private boolean local;
		private int latency = -1;
		private int shownLatency = Integer.MIN_VALUE;

		void set(String serverName, String serverHost, boolean singleplayer, String connectedText) {
			name.setText(serverName);
			host = serverHost;
			local = singleplayer;
			connected = connectedText;
			latency = -1;
			shownLatency = Integer.MIN_VALUE;
			address.setText(host);
			float room = IslandDraw.alertTextRoom(IslandDraw.TILE, local ? 0f : BARS_W);
			name.maxWidth(room);
			address.maxWidth(room);
		}

		/** Client tick while it may show: the ping, once the server has reported one. */
		void refresh(Minecraft minecraft) {
			if (local) {
				return;
			}
			ClientPacketListener connection = minecraft.getConnection();
			LocalPlayer player = minecraft.player;
			int ms = -1;
			if (connection != null && player != null) {
				PlayerInfo info = connection.getPlayerInfo(player.getUUID());
				if (info != null && info.getLatency() > 0) {
					ms = info.getLatency();
				}
			}
			latency = ms;
			if (ms != shownLatency) {
				shownLatency = ms;
				address.setText(ms > 0 ? host + " · " + ms + " ms" : host.isEmpty() ? connected : host);
			}
		}

		private int bars() {
			return latency <= 0 ? 0 : latency < 60 ? 4 : latency < 120 ? 3 : latency < 220 ? 2 : 1;
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(name, address), local ? 0f : BARS_W);
		}

		@Override
		public float duration() {
			return 3.5f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			IslandDraw.tile(g, IslandDraw.ALERT_PAD, tileY(h), IslandDraw.TILE, local ? IslandDraw.GREEN : IslandDraw.BLUE,
					local ? 0xE0F5 : 0xE0E8);
			IslandDraw.lines(g, name, address, textX(), h, IslandDraw.DIM);
			if (!local) {
				int lit = bars();
				int color = lit >= 3 ? IslandDraw.GREEN : lit == 2 ? IslandDraw.YELLOW : IslandDraw.RED;
				if (lit == 0) {
					// No ping yet: the bars light up one after another while it's measured.
					lit = 1 + (int) (ctx.time * 4f) % 4;
					color = IslandDraw.DIM;
				}
				IslandDraw.signal(g, w - IslandDraw.ALERT_RIGHT - BARS_W, h * 0.5f + 5.5f, lit, color);
			}
		}
	}

	// ---- Level up -----------------------------------------------------------------------------

	static final class LevelUp extends IslandActivity {
		private static final float RING = 18f;
		private final TextLabel level = title();
		private final TextLabel caption = caption();
		private final Smooth progress = new Smooth(0f, 5f);
		private float target;

		void set(String levelText, String captionText, float progressToNext) {
			level.setText(levelText);
			caption.setText(captionText);
			// The ring fills from empty up to where the bar is now.
			progress.snap(0f);
			target = progressToNext;
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(level, caption), RING);
		}

		@Override
		public float duration() {
			return 2.6f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			IslandDraw.tile(g, IslandDraw.ALERT_PAD, tileY(h), IslandDraw.TILE, 0xFF32D74B, 0xE412);
			IslandDraw.lines(g, level, caption, textX(), h, IslandDraw.DIM);
			float p = progress.target(target).update(ctx.dt);
			IslandDraw.ring(g, w - IslandDraw.ALERT_RIGHT - RING * 0.5f, h * 0.5f, RING * 0.5f, 2.6f, p, IslandDraw.GREEN);
		}
	}

	// ---- Elimination --------------------------------------------------------------------------

	/** Something you fought died: its face, its name in white, "Eliminated" in red, and a streak badge. */
	static final class Elimination extends IslandActivity {
		private final TextLabel victim = title();
		private final TextLabel caption = caption();
		private final TextLabel streakLabel = new TextLabel("", LiquidText.TextStyle.LABEL);
		private @Nullable LivingEntity entity;
		private int streak;

		void set(LivingEntity who, String name, String eliminated, int killStreak) {
			entity = who;
			victim.setText(name);
			caption.setText(eliminated);
			streak = killStreak;
			streakLabel.setText("×" + killStreak);
			float room = IslandDraw.alertTextRoom(IslandDraw.TILE, badgeWidth());
			victim.maxWidth(room);
		}

		private float badgeWidth() {
			return streak >= 2 ? streakLabel.width() + 12f : 0f;
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(victim, caption), badgeWidth());
		}

		@Override
		public float duration() {
			return 2.6f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			float tx = IslandDraw.ALERT_PAD;
			float ty = tileY(h);
			float tile = IslandDraw.TILE;
			LivingEntity who = entity;
			if (who instanceof net.minecraft.client.player.AbstractClientPlayer) {
				IslandDraw.plainTile(g, tx, ty, tile);
				LiveActivities.face(g, who, tx + 4f, ty + 4f, tile - 8f);
			} else {
				IslandDraw.tile(g, tx, ty, tile, IslandDraw.RED, 0xE221);
			}
			IslandDraw.lines(g, victim, caption, textX(), h, IslandDraw.RED);
			float badge = badgeWidth();
			if (badge > 0f) {
				float bh = 15f;
				float bx = w - IslandDraw.ALERT_RIGHT - badge;
				RenderUtils.fill(g, bx, h * 0.5f - bh * 0.5f, badge, bh, bh * 0.5f, ColorUtil.fade(IslandDraw.RED, 0.22f));
				IslandDraw.textMid(g, streakLabel, bx + (badge - streakLabel.width()) * 0.5f, h * 0.5f, IslandDraw.RED);
			}
		}
	}

	// ---- Low durability -----------------------------------------------------------------------

	static final class Durability extends IslandActivity {
		private static final float RING = 16f;
		private final TextLabel item = title();
		private final TextLabel left = caption();
		private ItemStack stack = ItemStack.EMPTY;
		private float fraction;

		void set(ItemStack copy, String name, String leftText, float remaining) {
			stack = copy;
			item.setText(name);
			left.setText(leftText);
			fraction = remaining;
			item.maxWidth(IslandDraw.alertTextRoom(IslandDraw.TILE, RING));
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(IslandDraw.TILE, textWidth(item, left), RING);
		}

		@Override
		public float duration() {
			return 3f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			float tx = IslandDraw.ALERT_PAD;
			float ty = tileY(h);
			IslandDraw.plainTile(g, tx, ty, IslandDraw.TILE);
			// Items ignore the fade, so they only appear once the alert is mostly in.
			if (!stack.isEmpty() && RenderUtils.alpha() > 0.55f) {
				g.item(stack, Math.round(tx + IslandDraw.TILE * 0.5f - 8f), Math.round(ty + IslandDraw.TILE * 0.5f - 8f));
			}
			int color = fraction < 0.05f ? IslandDraw.RED : IslandDraw.ORANGE;
			IslandDraw.lines(g, item, left, textX(), h, color);
			IslandDraw.ring(g, w - IslandDraw.ALERT_RIGHT - RING * 0.5f, h * 0.5f, RING * 0.5f, 2.4f, fraction, IslandDraw.RED);
		}
	}

	// ---- Screenshot ---------------------------------------------------------------------------

	static final class Screenshot extends IslandActivity {
		private static final float THUMB_H = 24f;
		private final TextLabel heading = title();
		private final TextLabel file = caption();
		private final ScreenshotThumbnail thumbnail;

		Screenshot(ScreenshotThumbnail thumbnail) {
			this.thumbnail = thumbnail;
		}

		void set(String title, String fileName) {
			heading.setText(title);
			file.setText(fileName);
			file.maxWidth(IslandDraw.alertTextRoom(thumbWidth(), 0f));
		}

		private float thumbWidth() {
			return Math.clamp(THUMB_H * thumbnail.aspect(), THUMB_H, THUMB_H * 2.2f);
		}

		@Override
		public float expandedWidth() {
			return IslandDraw.alertWidth(thumbWidth(), textWidth(heading, file), 0f);
		}

		@Override
		public float duration() {
			return 3f;
		}

		@Override
		public void expanded(GuiGraphicsExtractor g, HudContext ctx, float w, float h) {
			float tw = thumbWidth();
			float x = IslandDraw.ALERT_PAD + 1f;
			float y = (h - THUMB_H) * 0.5f;
			float r = 6f;
			TextureSetup texture = thumbnail.texture();
			float reveal = thumbnail.reveal();
			if (texture == null || reveal < 1f) {
				RenderUtils.fill(g, x, y, tw, THUMB_H, r, 0xFF2C2C2E);
				if (thumbnail.loading()) {
					float head = ctx.time * 5.2f;
					RenderUtils.arc(g, x + tw * 0.5f, y + THUMB_H * 0.5f, 4.5f, head, head + 1.4f + 1.1f * (float) Math.sin(ctx.time * 2.6f),
							1.2f, 0xFFAEAEB2);
				}
			}
			if (texture != null) {
				ImageDraw.rounded(g, texture, x, y, tw, THUMB_H, r, ColorUtil.fade(0xFFFFFFFF, reveal));
			}
			RenderUtils.stroke(g, x, y, tw, THUMB_H, r, HighDpiEngine.px(), 0x33FFFFFF, 0x0DFFFFFF);
			float lx = x + tw + IslandDraw.ALERT_GAP;
			IslandDraw.lines(g, heading, file, lx, h, IslandDraw.DIM);
		}
	}
}
