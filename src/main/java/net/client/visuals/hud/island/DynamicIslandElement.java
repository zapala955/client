package net.client.visuals.hud.island;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudManager;
import net.client.visuals.hud.HudModule;
import net.client.visuals.hud.element.ClockElement;
import net.client.visuals.hud.element.MusicElement;
import net.client.visuals.module.Module;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IslandShape;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.Spring;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * The Dynamic Island: a black pill at the top of the screen that comes alive, like the iPhone's.
 *
 * <h2>What it shows</h2>
 * <ul>
 *   <li><b>Live activities</b> (compact: a glyph at the left end, an accessory at the right) – the
 *       zoom factor while zooming, the face and health of what you're fighting, a beating heart on
 *       low health, your air underwater, and the song that's playing. The most important one fills
 *       the pill; the next one splits off into a bubble beside it.</li>
 *   <li><b>Alerts</b> (expanded for a few seconds, queued) – module toggles, a new song (the Now
 *       Playing card), joining a server, changing dimension, levelling up, eliminations with a
 *       streak count, gear about to break, screenshots with a thumbnail.</li>
 *   <li>Otherwise the idle pill (or a clock, or nothing).</li>
 * </ul>
 *
 * <h2>How it moves</h2>
 * Width, height, corner radius and the bubble are damped springs ({@link Spring}): growing
 * overshoots a little and settles, shrinking is snappier. The bubble splits off with a melted neck
 * that thins and snaps ({@code island.fsh}). Content cross-fades – the old fades out fast while the
 * new one waits for the shape to get going, then scales in – and is clipped to the shape, so it's
 * revealed as the island grows. A new alert for what's already showing bumps the island instead.
 *
 * <p>Everything lives in fixed fields and pooled render states; labels only rebuild when their
 * text changes. Events are polled on the client tick (no extra hooks), except module toggles
 * ({@code ModuleManager}) and attacks ({@code AttackEntityCallback}).</p>
 */
public final class DynamicIslandElement extends HudModule {
	public static final String[] LOOKS = {"black", "glass"};
	public static final String[] IDLE = {"hidden", "pill", "clock"};
	private static final int LOOK_GLASS = 1;
	private static final int IDLE_HIDDEN = 0;
	private static final int IDLE_CLOCK = 2;

	private static final int MODE_HIDDEN = 0;
	private static final int MODE_IDLE = 1;
	private static final int MODE_COMPACT = 2;
	private static final int MODE_EXPANDED = 3;

	private static final EquipmentSlot[] GEAR = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
	};
	/** Durability share at which gear is reported, and above which (repaired) it can be again. */
	private static final float WORN = 0.1f;
	private static final float REPAIRED = 0.15f;
	/** Kills within this many ticks of each other count as a streak. */
	private static final int STREAK_TICKS = 200;

	private static @Nullable DynamicIslandElement instance;

	public final ModeSetting look = add(new ModeSetting("look", "liquidclient.island.look", LOOKS, 0));
	public final ModeSetting idle = add(new ModeSetting("idle", "liquidclient.island.idle", IDLE, 1));
	public final BooleanSetting rim = add(new BooleanSetting("rim", "liquidclient.island.rim", true));
	public final BooleanSetting split = add(new BooleanSetting("split", "liquidclient.island.split", true));
	public final NumberSetting alertTime = add(new NumberSetting("alert_time", "liquidclient.island.alert_time", 50, 200, 5, 100)
			.suffix("%"));
	public final BooleanSetting musicOn = add(new BooleanSetting("music", "liquidclient.island.music", true));
	public final BooleanSetting songs = add(new BooleanSetting("songs", "liquidclient.island.songs", true));
	public final BooleanSetting toggles = add(new BooleanSetting("toggles", "liquidclient.island.toggles", true));
	public final BooleanSetting zoomOn = add(new BooleanSetting("zoom", "liquidclient.island.zoom", true));
	public final BooleanSetting combatOn = add(new BooleanSetting("combat", "liquidclient.island.combat", true));
	public final BooleanSetting lowHealth = add(new BooleanSetting("low_health", "liquidclient.island.low_health", true));
	public final BooleanSetting breathOn = add(new BooleanSetting("breath", "liquidclient.island.breath", true));
	public final BooleanSetting world = add(new BooleanSetting("world", "liquidclient.island.world", true));
	public final BooleanSetting levels = add(new BooleanSetting("level_up", "liquidclient.island.level_up", true));
	public final BooleanSetting gear = add(new BooleanSetting("durability", "liquidclient.island.durability", true));
	public final BooleanSetting screenshots = add(new BooleanSetting("screenshots", "liquidclient.island.screenshots", true));

	// Live activities, most important first.
	private final LiveActivities.Zoom zoom = new LiveActivities.Zoom();
	private final LiveActivities.Combat combat = new LiveActivities.Combat();
	private final LiveActivities.Health health = new LiveActivities.Health();
	private final LiveActivities.Breath breath = new LiveActivities.Breath();
	private final LiveActivities.Music music = new LiveActivities.Music();

	// Alerts (one instance each, refilled in place).
	private final Alerts.Toggle toggle = new Alerts.Toggle();
	private final Alerts.Notice dimension = new Alerts.Notice();
	private final Alerts.Server server = new Alerts.Server();
	private final Alerts.LevelUp levelUp = new Alerts.LevelUp();
	private final Alerts.Elimination elimination = new Alerts.Elimination();
	private final Alerts.Durability durability = new Alerts.Durability();
	private final ScreenshotThumbnail thumbnail = new ScreenshotThumbnail();
	private final Alerts.Screenshot screenshot = new Alerts.Screenshot(thumbnail);

	private final IslandActivity[] queue = new IslandActivity[8];
	private int queued;
	private @Nullable IslandActivity showing;
	private float showingLeft;
	private float showingAge;

	// What's on the island now, and what it's fading out from.
	private int mode = MODE_IDLE;
	private @Nullable IslandActivity content;
	private int previousMode = MODE_IDLE;
	private @Nullable IslandActivity previous;
	private float previousAlpha;
	private float contentAge = 10f;
	private @Nullable IslandActivity bubbleContent;

	// The shape.
	private final Spring bodyW = new Spring(IslandDraw.IDLE_W, 0.5f, 0.74f);
	private final Spring bodyH = new Spring(IslandDraw.COMPACT_H, 0.5f, 0.74f);
	private final Spring radius = new Spring(IslandDraw.COMPACT_H * 0.5f, 0.5f, 0.8f);
	private final Spring bubble = new Spring(0f, 0.55f, 0.78f);
	private final Spring pulse = new Spring(0f, 0.32f, 0.42f);
	private final Smooth opacity = new Smooth(1f, 12f);

	// Event tracking (client tick).
	private @Nullable ClientLevel lastLevel;
	private @Nullable ResourceKey<Level> lastDimension;
	private int lastXpLevel = -1;
	private int graceTicks;
	private long ticks;
	/**
	 * Set when the island is switched on (it isn't ticked while off): the next tick takes the world as
	 * it is instead of announcing it. No initializer on purpose – {@link #onEnable} can run from the
	 * super constructor, before field initializers would reset it.
	 */
	private boolean resync;
	private long lastKillTick = Long.MIN_VALUE / 2;
	private int streak;
	private final @Nullable Item[] worn = new Item[GEAR.length];

	private final TextLabel clock = new TextLabel("", LiquidText.TextStyle.LABEL);
	/** The Music element's source and artwork settings are shared, so both ask the media bridge the same thing. */
	private @Nullable MusicElement musicElement;
	private @Nullable ClockElement clockElement;
	private long shownMinute = -1L;
	private int shownClockFormat = -1;

	public DynamicIslandElement() {
		super("hud_island", true, 0.5f, 0.012f);
		style.markHidden(); // the island has its own looks
		instance = this;
	}

	@Override
	protected void onEnable() {
		resync = true;
	}

	@Override
	protected void onDisable() {
		// Nothing stale when it comes back; the screenshot texture goes now (client thread = render thread).
		if (queue != null) {
			Arrays.fill(queue, null);
			queued = 0;
			showing = null;
		}
		if (thumbnail != null) {
			thumbnail.release();
		}
	}

	// ---- Events -------------------------------------------------------------------------------

	/**
	 * A module was toggled by its keybind. Returns true when the island shows it (then the
	 * Notifications toasts don't, so it isn't announced twice).
	 */
	public static boolean onModuleToggled(Module module) {
		DynamicIslandElement island = instance;
		if (island == null || !island.isEnabled() || !island.toggles.get() || HudManager.theme() == null || !HudManager.theme().isEnabled()) {
			return false;
		}
		island.toggle.set(module, I18n.get("liquidclient.notification.enabled"), I18n.get("liquidclient.notification.disabled"));
		island.post(island.toggle);
		return true;
	}

	/** The local player attacked {@code entity} (client thread). */
	public static void onAttack(Entity entity) {
		DynamicIslandElement island = instance;
		if (island != null && island.isEnabled() && island.combatOn.get()) {
			island.combat.onAttack(entity);
		}
	}

	/** Queues an alert; one that's already showing is extended and bumps the island instead. */
	private void post(IslandActivity alert) {
		if (alert == showing) {
			showingLeft = Math.max(showingLeft, alert.duration() * alertScale());
			pulse.kick(1.2f);
			return;
		}
		for (int i = 0; i < queued; i++) {
			if (queue[i] == alert) {
				return; // refilled in place while it waits
			}
		}
		if (queued == queue.length) {
			System.arraycopy(queue, 1, queue, 0, queued - 1);
			queued--;
		}
		queue[queued++] = alert;
	}

	private boolean pending(IslandActivity alert) {
		if (alert == showing) {
			return true;
		}
		for (int i = 0; i < queued; i++) {
			if (queue[i] == alert) {
				return true;
			}
		}
		return false;
	}

	private float alertScale() {
		return (float) alertTime.get() / 100f;
	}

	private void advanceAlerts(float dt) {
		IslandActivity current = showing;
		if (current != null) {
			showingAge += dt;
			showingLeft -= dt;
			if (queued > 0) {
				// Something's waiting: give this one a moment to be read, then move on.
				showingLeft = Math.min(showingLeft, Math.max(0.35f, 1.1f - showingAge));
			}
			if (showingLeft <= 0f) {
				showing = null;
			}
		}
		if (showing == null && queued > 0) {
			IslandActivity next = queue[0];
			System.arraycopy(queue, 1, queue, 0, queued - 1);
			queue[--queued] = null;
			showing = next;
			showingLeft = next.duration() * alertScale();
			showingAge = 0f;
		}
	}

	@Override
	public void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;
		ticks++;
		if (resync) {
			resync = false;
			lastLevel = level;
			lastDimension = level != null ? level.dimension() : null;
			lastXpLevel = -1;
			graceTicks = 40;
		}

		combat.tick(minecraft);
		LivingEntity victim = combat.takeEliminated();
		if (victim != null && combatOn.get()) {
			streak = ticks - lastKillTick <= STREAK_TICKS ? streak + 1 : 1;
			lastKillTick = ticks;
			elimination.set(victim, victim.getDisplayName().getString(), I18n.get("liquidclient.island.eliminated"), streak);
			post(elimination);
		}
		health.tick(player);
		breath.tick(player);

		if (level != lastLevel) {
			ResourceKey<Level> dim = level != null ? level.dimension() : null;
			if (lastLevel == null && level != null) {
				graceTicks = 60; // the server sends experience and gear just after joining
				if (world.get()) {
					postServer(minecraft);
				}
			} else if (level != null && lastDimension != null && dim != lastDimension && world.get()) {
				postDimension(dim, lastDimension);
			}
			lastLevel = level;
			lastDimension = dim;
			lastXpLevel = -1;
		}
		if (graceTicks > 0) {
			graceTicks--;
		}
		if (pending(server)) {
			server.refresh(minecraft);
		}

		if (player != null) {
			int xp = player.experienceLevel;
			if (lastXpLevel >= 0 && xp > lastXpLevel && graceTicks == 0 && levels.get()) {
				levelUp.set(I18n.get("liquidclient.island.level", xp), I18n.get("liquidclient.island.level_up_caption"), player.experienceProgress);
				post(levelUp);
			}
			lastXpLevel = xp;
		}

		if (ticks % 10L == 0L) {
			if (player != null && gear.get()) {
				checkGear(player);
			}
			File shot = thumbnail.poll(minecraft);
			if (shot != null && screenshots.get()) {
				thumbnail.load(shot);
				screenshot.set(I18n.get("liquidclient.island.screenshot"), shot.getName());
				post(screenshot);
			}
		}
	}

	private void postServer(Minecraft minecraft) {
		ServerData data = minecraft.getCurrentServer();
		if (minecraft.isLocalServer()) {
			server.set(I18n.get("liquidclient.island.singleplayer"), I18n.get("liquidclient.island.local_world"), true, "");
		} else if (data != null) {
			String name = data.name == null || data.name.isBlank() ? data.ip : data.name;
			server.set(name, data.ip, false, I18n.get("liquidclient.island.connected"));
		} else {
			server.set(I18n.get("liquidclient.island.connected"), "", false, I18n.get("liquidclient.island.connected"));
		}
		post(server);
	}

	private void postDimension(@Nullable ResourceKey<Level> to, ResourceKey<Level> from) {
		int color;
		int icon;
		if (to == Level.NETHER) {
			color = 0xFFFF6B2C;
			icon = 0xE0D2;
		} else if (to == Level.END) {
			color = IslandDraw.PURPLE;
			icon = 0xE412;
		} else if (to == Level.OVERWORLD) {
			color = IslandDraw.GREEN;
			icon = 0xE1F3;
		} else {
			color = IslandDraw.INDIGO;
			icon = 0xE0E8;
		}
		dimension.set(dimensionName(to), I18n.get("liquidclient.island.dimension.left", dimensionName(from)), color, icon, 3f);
		post(dimension);
	}

	private static String dimensionName(@Nullable ResourceKey<Level> key) {
		if (key == Level.NETHER) {
			return I18n.get("liquidclient.island.dimension.nether");
		}
		if (key == Level.END) {
			return I18n.get("liquidclient.island.dimension.end");
		}
		if (key == Level.OVERWORLD) {
			return I18n.get("liquidclient.island.dimension.overworld");
		}
		return I18n.get("liquidclient.island.dimension.other");
	}

	/** Armour and held items: each is reported once when it's nearly broken, again only after a repair. */
	private void checkGear(LocalPlayer player) {
		for (int i = 0; i < GEAR.length; i++) {
			ItemStack stack = player.getItemBySlot(GEAR[i]);
			if (stack.isEmpty() || !stack.isDamageableItem() || stack.getMaxDamage() < 20) {
				worn[i] = null;
				continue;
			}
			int max = stack.getMaxDamage();
			float left = (max - stack.getDamageValue()) / (float) max;
			if (left <= WORN) {
				if (worn[i] != stack.getItem() && graceTicks == 0) {
					worn[i] = stack.getItem();
					int percent = Math.max(1, Math.round(left * 100f));
					durability.set(stack.copy(), stack.getHoverName().getString(), I18n.get("liquidclient.island.durability_left", percent), left);
					post(durability);
				}
			} else if (left > REPAIRED) {
				worn[i] = null;
			}
		}
	}

	// ---- Layout -------------------------------------------------------------------------------

	@Override
	public void layout(HudContext ctx) {
		float dt = ctx.dt;
		thumbnail.update();

		MusicElement musicElement = this.musicElement;
		if (musicElement == null) {
			musicElement = this.musicElement = ModuleManager.get(MusicElement.class);
		}
		if (musicOn.get() || ctx.editing) {
			music.update(dt, musicElement.source.index() == 1, musicElement.onlineArt.get());
			if (music.takeNewSong() && songs.get() && musicOn.get() && !ctx.editing) {
				post(music);
			}
		}
		zoom.update();
		advanceAlerts(dt);
		if (!pending(screenshot) && previous != screenshot && content != screenshot) {
			thumbnail.release();
		}

		// The two most important live activities: one for the pill, one for the bubble.
		IslandActivity primary = null;
		IslandActivity secondary = null;
		for (int i = 0; i < 5; i++) {
			IslandActivity a = live(i);
			if (a != null) {
				if (primary == null) {
					primary = a;
				} else if (secondary == null) {
					secondary = a;
				}
			}
		}

		int nextMode;
		IslandActivity next;
		if (ctx.editing) {
			nextMode = MODE_COMPACT;
			next = music;
			secondary = null;
		} else if (Minecraft.getInstance().options.keyPlayerList.isDown()) {
			nextMode = MODE_HIDDEN; // out of the way of the player list
			next = null;
		} else if (showing != null) {
			nextMode = MODE_EXPANDED;
			next = showing;
		} else if (primary != null) {
			nextMode = MODE_COMPACT;
			next = primary;
		} else {
			int idleMode = idle.index();
			nextMode = idleMode == IDLE_HIDDEN ? MODE_HIDDEN : MODE_IDLE;
			next = null;
		}
		if (nextMode != mode || next != content) {
			previousAlpha = contentAlpha();
			previousMode = mode;
			previous = content;
			mode = nextMode;
			content = next;
			contentAge = 0f;
		}
		contentAge = Math.min(contentAge + dt, 10f);

		// Target shape.
		float tw;
		float th;
		float tr;
		switch (mode) {
			case MODE_HIDDEN -> {
				// Shrinks into the top of the screen while it fades.
				tw = IslandDraw.IDLE_W * 0.6f;
				th = IslandDraw.COMPACT_H * 0.6f;
				tr = th * 0.5f;
			}
			case MODE_COMPACT -> {
				tw = IslandDraw.compactWidth(content != null ? content.trailingWidth() : 0f);
				th = IslandDraw.COMPACT_H;
				tr = th * 0.5f;
			}
			case MODE_EXPANDED -> {
				tw = content != null ? content.expandedWidth() : IslandDraw.ALERT_MIN_W;
				th = content != null ? content.expandedHeight() : IslandDraw.ALERT_H;
				tr = content != null ? content.expandedRadius() : th * 0.5f;
			}
			default -> {
				tw = idle.index() == IDLE_CLOCK ? Math.max(IslandDraw.IDLE_W, refreshClock() + 28f) : IslandDraw.IDLE_W;
				th = IslandDraw.COMPACT_H;
				tr = th * 0.5f;
			}
		}
		retarget(bodyW, tw);
		retarget(bodyH, th);
		radius.target(tr);
		IslandActivity bubbleTarget = mode == MODE_COMPACT && split.get() ? secondary : null;
		if (bubbleTarget != null) {
			bubbleContent = bubbleTarget;
		}
		bubble.target(bubbleTarget != null ? 1f : 0f);
		bodyW.update(dt);
		bodyH.update(dt);
		radius.update(dt);
		bubble.update(dt);
		pulse.update(dt);
		opacity.target(mode != MODE_HIDDEN).update(dt);
		if (bubbleTarget == null && bubble.get() < 0.02f) {
			bubbleContent = null;
		}

		float span = (IslandDraw.BUBBLE_GAP + IslandDraw.COMPACT_H) * Math.clamp(bubble.get(), 0f, 1f);
		width = Math.max(1f, bodyW.get() + span);
		height = Math.max(1f, Math.max(bodyH.get(), IslandDraw.COMPACT_H));
	}

	/** Growing is springy (a little overshoot), shrinking quicker and calmer – like iOS. */
	private static void retarget(Spring spring, float target) {
		if (target != spring.target()) {
			if (target > spring.get()) {
				spring.tune(0.5f, 0.72f);
			} else {
				spring.tune(0.42f, 0.86f);
			}
			spring.target(target);
		}
	}

	private @Nullable IslandActivity live(int rank) {
		return switch (rank) {
			case 0 -> zoomOn.get() && zoom.active() ? zoom : null;
			case 1 -> combatOn.get() && combat.active() ? combat : null;
			case 2 -> lowHealth.get() && health.active() ? health : null;
			case 3 -> breathOn.get() && breath.active() ? breath : null;
			default -> musicOn.get() && music.active() ? music : null;
		};
	}

	/** Fade of the current content: it waits for the shape to get moving, then comes in. */
	private float contentAlpha() {
		return smoothstep(0.07f, 0.3f, contentAge);
	}

	/** Updates the idle clock once a minute; returns its width. */
	private float refreshClock() {
		long minute = System.currentTimeMillis() / 60_000L;
		ClockElement clockElement = this.clockElement;
		if (clockElement == null) {
			clockElement = this.clockElement = ModuleManager.get(ClockElement.class);
		}
		int format = clockElement.twentyFour.get() ? 1 : 0;
		if (minute != shownMinute || format != shownClockFormat) {
			shownMinute = minute;
			shownClockFormat = format;
			LocalTime now = LocalTime.now();
			int hour = now.getHour();
			int shownHour = format == 1 ? hour : hour % 12 == 0 ? 12 : hour % 12;
			int m = now.getMinute();
			clock.setText(shownHour + (m < 10 ? ":0" : ":") + m);
		}
		return clock.width();
	}

	@Override
	public boolean visible(HudContext ctx) {
		return ctx.editing || mode != MODE_HIDDEN || opacity.get() > 0.01f;
	}

	// ---- Drawing ------------------------------------------------------------------------------

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		float o = opacity.get();
		if (o < 0.004f) {
			return;
		}
		RenderUtils.pushAlpha(o);
		float w = bodyW.get();
		float h = bodyH.get();
		float p = pulse.get();
		// A touch of squash and stretch: quick widening flattens the island a little.
		float stretch = Math.clamp(bodyW.velocity() / 2600f, -0.05f, 0.05f);
		float hw = Math.max(1f, w * 0.5f * (1f + p));
		float hh = Math.max(1f, h * 0.5f * (1f + p) * (1f - stretch));
		float r = Math.clamp(radius.get(), 0f, Math.min(hw, hh));
		float cx = w * 0.5f;
		float cy = h * 0.5f;

		// The bubble starts inside the body's right end and travels out (overshooting a little).
		float s = bubble.get();
		float bubbleR = 0f;
		float bubbleX = 0f;
		if (s > 0.01f && bubbleContent != null) {
			float full = IslandDraw.COMPACT_H * 0.5f;
			float start = w * 0.5f - full;
			float end = w * 0.5f + IslandDraw.BUBBLE_GAP + full;
			bubbleX = start + (end - start) * s;
			bubbleR = full * Math.clamp(0.55f + 0.45f * s, 0f, 1f);
		}

		if (mode == MODE_COMPACT && content == health) {
			RenderUtils.outerGlow(g, cx - hw, cy - hh, hw * 2f, hh * 2f, r, 7f, health.glow(ctx));
		}
		IslandShape.shadow(g, cx, cy + 2f, hw, hh, r, bubbleX, bubbleR, 7f, 0x59000000);
		boolean glass = look.index() == LOOK_GLASS
				&& IslandShape.glass(g, cx, cy, hw, hh, r, bubbleX, bubbleR, 0xFF0C0D10, 0.42f, blurPx(ctx));
		if (!glass) {
			IslandShape.fill(g, cx, cy, hw, hh, r, bubbleX, bubbleR, 0xFF000000, rim.get() ? 0.14f : 0f);
		}

		// Content, clipped to the body so it's revealed as the island grows.
		g.enableScissor((int) Math.floor(cx - hw), (int) Math.floor(cy - hh), (int) Math.ceil(cx + hw), (int) Math.ceil(cy + hh));
		float out = previousAlpha * (1f - smoothstep(0f, 0.13f, contentAge));
		drawContent(g, ctx, previousMode, previous, out, cx, cy, hw, hh);
		drawContent(g, ctx, mode, content, contentAlpha(), cx, cy, hw, hh);
		g.disableScissor();

		IslandActivity second = bubbleContent;
		if (second != null && bubbleR > 0.5f) {
			float a = smoothstep(0.6f, 1f, s);
			if (a > 0.01f) {
				RenderUtils.pushAlpha(a);
				float size = 12f;
				second.leading(g, ctx, cx + bubbleX - size * 0.5f, cy - size * 0.5f, size);
				RenderUtils.popAlpha();
			}
		}
		RenderUtils.popAlpha();
	}

	private void drawContent(GuiGraphicsExtractor g, HudContext ctx, int m, @Nullable IslandActivity activity, float alpha, float cx,
			float cy, float hw, float hh) {
		if (alpha < 0.01f || m == MODE_HIDDEN || activity == null && m != MODE_IDLE) {
			return;
		}
		RenderUtils.pushAlpha(alpha);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		// Scales in from 94 % around the island's centre as it fades in.
		float grow = 0.94f + 0.06f * alpha;
		pose.translate(cx, cy);
		pose.scale(grow, grow);
		pose.translate(-cx, -cy);
		switch (m) {
			case MODE_IDLE -> {
				if (idle.index() == IDLE_CLOCK) {
					IslandDraw.textMid(g, clock, cx - clock.width() * 0.5f, cy, IslandDraw.WHITE);
				}
			}
			case MODE_COMPACT -> {
				float lead = IslandDraw.LEAD;
				activity.leading(g, ctx, cx - hw + IslandDraw.LEAD_INSET, cy - lead * 0.5f, lead);
				activity.trailing(g, ctx, cx + hw - IslandDraw.TRAIL_INSET, cy);
			}
			default -> {
				// Laid out at its full size, scaled to fit the island while it's still growing.
				float tw = activity.expandedWidth();
				float th = activity.expandedHeight();
				float fit = Math.clamp(Math.min(hw * 2f / tw, hh * 2f / th), 0.5f, 1f);
				pose.translate(cx, cy - hh);
				pose.scale(fit, fit);
				pose.translate(-tw * 0.5f, 0f);
				activity.expanded(g, ctx, tw, th);
			}
		}
		pose.popMatrix();
		RenderUtils.popAlpha();
	}

	/** Device-pixel blur for the glass look, from the HUD theme's blur. */
	private static float blurPx(HudContext ctx) {
		return Math.min(40f, (float) ctx.theme.blur.get() * HighDpiEngine.scale());
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}
}
