package net.client.visuals.render.storm;

import net.client.visuals.module.render.StormChaserModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The storm's lightning (render thread; prototyped offline – tools of the session, not shipped).
 *
 * <h2>Cloud-to-ground strikes</h2>
 * Strikes come as a Poisson process whose rate follows the storm's activity and the frequency
 * setting, mostly somewhere in the rain core – often kilometres away, flickering on the horizon while
 * the storm is still coming – and, under the core, sometimes close by. The strike point is found in
 * the world: the tallest of a few spots around the chosen one (lightning goes for trees and towers),
 * then a ray cast along the bolt's last stretch down to whatever it meets (a roof, a tree's crown, the
 * ground). The channel ({@link BoltGenerator}) runs from inside the cloud base to that point.
 *
 * <h2>Real flicker</h2>
 * A faint stepped leader works its way down (branches and all) over 60–120 ms, then 2–4 return
 * strokes re-light the main channel – brilliant, decaying in ~45 ms, 40–160 ms apart – with a
 * continuing current glowing on after the last. Branches only light with the first stroke, as in
 * reality (later strokes follow the main channel). The ground point flashes with every stroke.
 *
 * <h2>Cloud flashes</h2>
 * More often the lightning stays in the cloud: a few pulses light the deck from inside
 * ({@link StormSky}); overhead, some send spider lightning crawling along the cloud base.
 *
 * <h2>Everything a flash touches</h2>
 * Each frame the active strokes become point lights for the sky (cloud interiors), for
 * {@link StormRenderer}'s screen-space relighting of the world and for the fog colour; thunder is
 * scheduled at 343 m/s from the nearest part of the channel ({@link StormSounds}). While the game is
 * paused (Photo Mode's freeze) the storm clock stops and a strike holds its brightest stroke, so it can
 * be photographed mid-strike. Fixed slots, no allocation per frame (a ray cast per strike).
 */
public final class Lightning {
	static final int MAX_BOLTS = 4;
	/** Segments per bolt (the shader's array holds as many). */
	static final int SEGMENTS = 448;
	static final int MAX_FLASHES = 4;
	/** Point lights handed to the relighting pass. */
	static final int MAX_LIGHTS = 6;
	private static final float SOUND_SPEED = 343f;

	/** Cloud-to-ground strikes and cloud flashes per minute at full activity and 100 % frequency. */
	private static final float STRIKES_PER_MINUTE = 9f;
	private static final float FLASHES_PER_MINUTE = 16f;

	private static final BoltGenerator GENERATOR = new BoltGenerator();
	private static final BoltGenerator.Style[] STYLES = {
			new BoltGenerator.Style(0.17f, 0.075f, 0.16f, 0.42f, false),  // natural
			new BoltGenerator.Style(0.18f, 0.1f, 0.22f, 0.55f, false),    // cinematic
			new BoltGenerator.Style(0.2f, 0.12f, 0.22f, 0.48f, true),     // forked
			new BoltGenerator.Style(0.17f, 0.085f, 0.18f, 0.48f, false)   // neon
	};

	static final Bolt[] BOLTS = new Bolt[MAX_BOLTS];
	private static final Flash[] FLASHES = new Flash[MAX_FLASHES];

	static {
		for (int i = 0; i < MAX_BOLTS; i++) {
			BOLTS[i] = new Bolt();
		}
		for (int i = 0; i < MAX_FLASHES; i++) {
			FLASHES[i] = new Flash();
		}
	}

	// This frame's light, camera-relative.
	private static final float[] SKY_X = new float[4];
	private static final float[] SKY_Y = new float[4];
	private static final float[] SKY_Z = new float[4];
	private static final float[] SKY_I = new float[4];
	private static final float[] LIGHT_X = new float[MAX_LIGHTS];
	private static final float[] LIGHT_Y = new float[MAX_LIGHTS];
	private static final float[] LIGHT_Z = new float[MAX_LIGHTS];
	private static final float[] LIGHT_I = new float[MAX_LIGHTS];
	private static final float[] LIGHT_X1 = new float[MAX_LIGHTS];
	private static final float[] LIGHT_Y1 = new float[MAX_LIGHTS];
	private static final float[] LIGHT_Z1 = new float[MAX_LIGHTS];
	private static int lights;
	private static float globalFlash;
	private static long seeds = System.nanoTime();

	/** A channel: a strike, or a crawler under the deck. */
	static final class Bolt {
		boolean alive;
		boolean crawler;
		/** The origin in the world (the strike point / the crawler's start); segments are relative to it. */
		double x;
		double y;
		double z;
		double birth;
		final float[] segments = new float[SEGMENTS * 8];
		int count;
		int strokes;
		final float[] strokeAt = new float[4];
		final float[] strokePower = new float[4];
		float leader;
		float life;
		/** Channel top and a point low on the channel, relative to the origin. */
		float topX;
		float topY;
		float topZ;
		float lowX;
		float lowY;
		float lowZ;
		float power;
		boolean grounded;
		int struck;
		// This frame.
		float channel;
		float branches;
		float progress;
		float glow;
	}

	/** A flash inside the cloud: a few pulses. */
	static final class Flash {
		boolean alive;
		double x;
		double y;
		double z;
		double birth;
		int pulses;
		final float[] at = new float[5];
		final float[] power = new float[5];
		final float[] width = new float[5];
		float life;
		boolean thundered;
		float now;
	}

	private Lightning() {
	}

	// ---- Frame -------------------------------------------------------------------------------------

	/**
	 * Once per frame after {@link Storm#frame}: new strikes and flashes, envelopes, and this frame's
	 * lights relative to the camera at (cx, cy, cz).
	 */
	public static void frame(ClientLevel level, double cx, double cy, double cz) {
		StormChaserModule m = Storm.module();
		double now = Storm.time();
		float dt = Storm.dt();
		if (Storm.active() && dt > 0f) {
			float rate = Storm.activity() * m.frequency.getFloat() / 100f;
			ThreadLocalRandom random = ThreadLocalRandom.current();
			if (random.nextFloat() < 1f - (float) Math.exp(-rate * STRIKES_PER_MINUTE / 60f * dt)) {
				strike(level, m, cx, cy, cz, now);
			}
			if (random.nextFloat() < 1f - (float) Math.exp(-rate * FLASHES_PER_MINUTE / 60f * dt)) {
				flash(level, m, cx, cy, cz, now);
			}
		}
		boolean paused = Minecraft.getInstance().isPaused();
		float brightness = m.brightness.getFloat() / 100f;
		globalFlash = 0f;
		lights = 0;
		int sky = 0;
		for (int i = 0; i < 4; i++) {
			SKY_I[i] = 0f;
		}
		for (Bolt bolt : BOLTS) {
			if (!bolt.alive) {
				continue;
			}
			float t = (float) (now - bolt.birth);
			if (t > bolt.life) {
				bolt.alive = false;
				continue;
			}
			envelope(bolt, t, paused);
			if (bolt.struck < bolt.strokes && t >= bolt.strokeAt[bolt.struck]) {
				onStroke(level, bolt, bolt.struck);
				bolt.struck++;
			}
			float rx = (float) (bolt.x - cx);
			float ry = (float) (bolt.y - cy);
			float rz = (float) (bolt.z - cz);
			float light = bolt.channel * bolt.power * brightness;
			if (light > 0.003f) {
				float distance = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
				globalFlash += light * (bolt.crawler ? 0.55f : 0.45f) / (1f + distance / 3000f);
				// The cloud lit around the channel's top.
				if (sky < 4) {
					SKY_X[sky] = rx + bolt.topX;
					SKY_Y[sky] = ry + bolt.topY;
					SKY_Z[sky] = rz + bolt.topZ;
					SKY_I[sky] = light * 1.1f;
					sky++;
				}
				// The channel as a line light: from the ground (or the crawler's start) to the cloud.
				if (!bolt.crawler) {
					addLight(rx, ry + 2f, rz, rx + bolt.topX, ry + bolt.topY, rz + bolt.topZ, light * 2.4f);
				} else {
					addLight(rx, ry, rz, rx + bolt.topX * 2f, ry - 10f, rz + bolt.topZ * 2f, light * 1.6f);
				}
			}
		}
		for (Flash flash : FLASHES) {
			if (!flash.alive) {
				continue;
			}
			float t = (float) (now - flash.birth);
			if (t > flash.life) {
				flash.alive = false;
				continue;
			}
			flash.now = pulses(flash, t, paused);
			float rx = (float) (flash.x - cx);
			float ry = (float) (flash.y - cy);
			float rz = (float) (flash.z - cz);
			if (!flash.thundered && t >= flash.at[0]) {
				flash.thundered = true;
				float distance = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
				StormSounds.thunder(now + distance / SOUND_SPEED, distance, rx, ry, rz, true);
			}
			float light = flash.now * brightness;
			if (light > 0.003f) {
				float distance = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
				globalFlash += light * 0.4f / (1f + distance / 3000f);
				if (sky < 4) {
					SKY_X[sky] = rx;
					SKY_Y[sky] = ry;
					SKY_Z[sky] = rz;
					SKY_I[sky] = light;
					sky++;
				}
				addLight(rx, ry, rz, rx, ry, rz, light * 1.2f);
			}
		}
		globalFlash = Math.min(globalFlash, 2.5f);
	}

	/** A line light from (x, y, z) to (x1, y1, z1) (a point when they're the same), camera-relative. */
	private static void addLight(float x, float y, float z, float x1, float y1, float z1, float intensity) {
		int slot = lights;
		if (lights < MAX_LIGHTS) {
			lights++;
		} else {
			// Full: replace the weakest, if this one is stronger.
			slot = 0;
			for (int i = 1; i < MAX_LIGHTS; i++) {
				if (LIGHT_I[i] < LIGHT_I[slot]) {
					slot = i;
				}
			}
			if (intensity <= LIGHT_I[slot]) {
				return;
			}
		}
		LIGHT_X[slot] = x;
		LIGHT_Y[slot] = y;
		LIGHT_Z[slot] = z;
		LIGHT_X1[slot] = x1;
		LIGHT_Y1[slot] = y1;
		LIGHT_Z1[slot] = z1;
		LIGHT_I[slot] = intensity;
	}

	/** A bolt's brightness at t seconds after its birth (paused: held at its latest stroke's peak). */
	private static void envelope(Bolt bolt, float t, boolean paused) {
		if (paused && t >= bolt.strokeAt[0]) {
			// Frozen mid-strike: the brightest moment of the latest stroke, not the dark gap after it.
			float held = bolt.strokeAt[0];
			for (int k = 0; k < bolt.strokes; k++) {
				if (bolt.strokeAt[k] <= t) {
					held = bolt.strokeAt[k];
				}
			}
			t = held + 0.004f;
		}
		bolt.progress = t < bolt.leader ? (float) Math.floor(t / bolt.leader * 14f) / 14f : 2f;
		float leaderGlow = t < bolt.strokeAt[0] ? 0.1f : 0f;
		float channel = 0f;
		for (int k = 0; k < bolt.strokes; k++) {
			float since = t - bolt.strokeAt[k];
			if (since >= 0f) {
				channel += bolt.strokePower[k] * ((float) Math.exp(-since / 0.045f) * 0.82f + (float) Math.exp(-since / 0.22f) * 0.18f);
			}
		}
		float first = t - bolt.strokeAt[0];
		float branches = first >= 0f ? bolt.strokePower[0] * (float) Math.exp(-first / 0.035f) : 0f;
		bolt.channel = Math.max(channel, leaderGlow);
		bolt.branches = Math.max(branches, leaderGlow * 0.8f);
		bolt.glow = bolt.grounded ? channel : 0f;
		// The last of it fades smoothly instead of popping off.
		float tail = Math.clamp((bolt.life - t) / 0.12f, 0f, 1f);
		bolt.channel *= tail;
		bolt.branches *= tail;
		bolt.glow *= tail;
	}

	private static float pulses(Flash flash, float t, boolean paused) {
		if (paused) {
			// Held at the brightest pulse so far.
			float best = 0f;
			for (int k = 0; k < flash.pulses; k++) {
				if (flash.at[k] <= t) {
					best = Math.max(best, flash.power[k]);
				}
			}
			return best;
		}
		float sum = 0f;
		for (int k = 0; k < flash.pulses; k++) {
			float d = (t - flash.at[k]) / flash.width[k];
			sum += flash.power[k] * (float) Math.exp(-d * d);
		}
		return sum;
	}

	/** A return stroke has just hit: thunder (first stroke), sparks, and the vanilla fallback bolt. */
	private static void onStroke(ClientLevel level, Bolt bolt, int stroke) {
		if (bolt.crawler || stroke != 0) {
			return;
		}
		Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
		// Thunder starts from the nearest part of the channel: the ground, its low part or its top.
		float best = Float.MAX_VALUE;
		float bx = 0f;
		float by = 0f;
		float bz = 0f;
		for (int i = 0; i < 3; i++) {
			float ox = i == 0 ? 0f : (i == 1 ? bolt.lowX : bolt.topX);
			float oy = i == 0 ? 0f : (i == 1 ? bolt.lowY : bolt.topY);
			float oz = i == 0 ? 0f : (i == 1 ? bolt.lowZ : bolt.topZ);
			float rx = (float) (bolt.x + ox - camera.x);
			float ry = (float) (bolt.y + oy - camera.y);
			float rz = (float) (bolt.z + oz - camera.z);
			float d = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
			if (d < best) {
				best = d;
				bx = rx;
				by = ry;
				bz = rz;
			}
		}
		StormSounds.thunder(Storm.time() + best / SOUND_SPEED, best, bx, by, bz, false);
		double dx = bolt.x - camera.x;
		double dz = bolt.z - camera.z;
		double flat = dx * dx + dz * dz;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (flat < 64.0 * 64.0) {
			for (int i = 0; i < 26; i++) {
				double vx = random.nextGaussian() * 0.35;
				double vy = 0.15 + random.nextDouble() * 0.45;
				double vz = random.nextGaussian() * 0.35;
				level.addParticle(ParticleTypes.ELECTRIC_SPARK, bolt.x, bolt.y + 0.3, bolt.z, vx, vy, vz);
			}
			for (int i = 0; i < 8; i++) {
				level.addParticle(ParticleTypes.END_ROD, bolt.x, bolt.y + 0.2, bolt.z, random.nextGaussian() * 0.12, 0.08,
						random.nextGaussian() * 0.12);
			}
		}
		// Without the bolt shader: vanilla's lightning bolt (visual only, silent – our thunder plays).
		if (!StormRenderer.canDrawBolts() && flat < 192.0 * 192.0) {
			LightningBolt entity = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
			if (entity != null) {
				entity.setId(StormRenderer.nextFallbackId());
				entity.snapTo(bolt.x, bolt.y, bolt.z);
				entity.setVisualOnly(true);
				entity.setSilent(true);
				level.addEntity(entity);
			}
		}
	}

	// ---- New strikes ---------------------------------------------------------------------------------

	private static Bolt freeBolt() {
		Bolt oldest = BOLTS[0];
		for (Bolt bolt : BOLTS) {
			if (!bolt.alive) {
				return bolt;
			}
			if (bolt.birth < oldest.birth) {
				oldest = bolt;
			}
		}
		return oldest;
	}

	/** A cloud-to-ground strike somewhere in the rain core (or close by, under it). */
	private static void strike(ClientLevel level, StormChaserModule m, double cx, double cy, double cz, double now) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		float front = Storm.front();
		float back = Storm.back();
		float from = back + Storm.TRAIL * 0.5f;
		float to = front - 300f;
		if (to <= from) {
			return;
		}
		double s;
		double q;
		if (Storm.rain() > 0.4f && random.nextFloat() < 0.32f) {
			// Close by: 80 m to a kilometre away.
			double r = 80.0 + 950.0 * sq(random.nextDouble());
			double a = random.nextDouble() * Math.PI * 2.0;
			s = Math.cos(a) * r;
			q = Math.sin(a) * r;
		} else {
			s = from + random.nextDouble() * (to - from);
			double u = random.nextDouble() * 2.0 - 1.0;
			q = u * 8000.0 * (0.35 + 0.65 * Math.abs(u)) - Storm.across() * 0.5;
		}
		double ax = Storm.axisX();
		double az = Storm.axisZ();
		double x = cx + ax * s - az * q;
		double z = cz + az * s + ax * q;
		spawnStrike(level, m, cx, cy, cz, x, z, now, false);
	}

	/** Photo Mode: a strike a few hundred blocks out where the camera looks (held mid-strike if frozen). */
	public static boolean strikeInView(double cx, double cy, double cz, float yaw, float pitch) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null || !Storm.active()) {
			return false;
		}
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double distance = 260.0 + random.nextDouble() * 420.0;
		double yr = Math.toRadians(yaw + (random.nextDouble() - 0.5) * 14.0);
		double x = cx - Math.sin(yr) * distance;
		double z = cz + Math.cos(yr) * distance;
		double now = Storm.time();
		Bolt bolt = spawnStrike(level, Storm.module(), cx, cy, cz, x, z, now, true);
		if (bolt != null && minecraft.isPaused()) {
			// The world stands still: show it at its first return stroke and keep it there.
			bolt.birth = now - bolt.strokeAt[0] - 0.004;
		}
		return bolt != null;
	}

	private static Bolt spawnStrike(ClientLevel level, StormChaserModule m, double cx, double cy, double cz, double x, double z,
			double now, boolean forced) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double groundY = cy - 6.0;
		double gx = x;
		double gz = z;
		boolean loaded = level.hasChunk(SectionPos.blockToSectionCoord((int) Math.floor(x)), SectionPos.blockToSectionCoord((int) Math.floor(z)));
		if (loaded) {
			// Lightning goes for the tallest thing around: a few spots, the highest wins.
			int best = Integer.MIN_VALUE;
			for (int i = 0; i < 6; i++) {
				int px = (int) Math.floor(x + (i == 0 ? 0.0 : random.nextGaussian() * 14.0));
				int pz = (int) Math.floor(z + (i == 0 ? 0.0 : random.nextGaussian() * 14.0));
				if (!level.hasChunk(SectionPos.blockToSectionCoord(px), SectionPos.blockToSectionCoord(pz))) {
					continue;
				}
				int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, px, pz);
				if (h > best) {
					best = h;
					gx = px + 0.5;
					gz = pz + 0.5;
				}
			}
			if (best != Integer.MIN_VALUE) {
				groundY = best;
			}
		}
		double baseY = Storm.baseY(cy);
		// The channel's top: inside the cloud base, offset so the bolt leans.
		double topX = gx + random.nextGaussian() * 140.0;
		double topY = baseY + 40.0 + random.nextDouble() * 120.0;
		double topZ = gz + random.nextGaussian() * 140.0;
		if (loaded) {
			// Ray cast along the bolt's last stretch onto whatever it meets first.
			double lx = (topX - gx) * 0.08;
			double lz = (topZ - gz) * 0.08;
			Vec3 from = new Vec3(gx + lx, groundY + 40.0, gz + lz);
			Vec3 to = new Vec3(gx, groundY - 8.0, gz);
			HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
			if (hit.getType() != HitResult.Type.MISS) {
				Vec3 at = hit.getLocation();
				gx = at.x;
				groundY = at.y;
				gz = at.z;
			}
		}
		if (topY - groundY < 60.0) {
			return null;
		}
		double dx = gx - cx;
		double dy = groundY - cy;
		double dz = gz - cz;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance < 40.0 && !forced) {
			return null;
		}
		Bolt bolt = freeBolt();
		bolt.alive = true;
		bolt.crawler = false;
		bolt.grounded = true;
		bolt.x = gx;
		bolt.y = groundY;
		bolt.z = gz;
		bolt.birth = now;
		bolt.struck = 0;
		int styleIndex = m.boltStyle.index();
		BoltGenerator.Style style = STYLES[styleIndex];
		int levels = distance < 1200.0 ? 7 : (distance < 4000.0 ? 6 : 5);
		float tx = (float) (topX - gx);
		float ty = (float) (topY - groundY);
		float tz = (float) (topZ - gz);
		bolt.count = GENERATOR.generate(bolt.segments, SEGMENTS - 1, seeds++, style, levels, false, tx, ty, tz, 0f, 0f, 0f);
		bolt.count = GENERATOR.sprite(bolt.segments, bolt.count, 0f, 0.6f, 0f, 9f);
		bolt.topX = tx;
		bolt.topY = ty;
		bolt.topZ = tz;
		bolt.lowX = tx * 0.25f;
		bolt.lowY = ty * 0.25f;
		bolt.lowZ = tz * 0.25f;
		schedule(bolt, random, styleIndex == StormChaserModule.BOLT_CINEMATIC);
		// Far strikes are dimmer: the rain between swallows them.
		bolt.power = (float) (1.0 / (1.0 + distance / 4500.0));
		return bolt;
	}

	/** Leader, 2–4 return strokes and the continuing current. */
	private static void schedule(Bolt bolt, ThreadLocalRandom random, boolean cinematic) {
		bolt.leader = (cinematic ? 0.1f : 0.06f) + random.nextFloat() * 0.05f;
		float roll = random.nextFloat();
		bolt.strokes = roll < 0.35f ? 2 : (roll < 0.75f ? 3 : 4);
		float t = bolt.leader;
		for (int k = 0; k < bolt.strokes; k++) {
			bolt.strokeAt[k] = t;
			bolt.strokePower[k] = k == 0 ? 1f : 0.55f + random.nextFloat() * 0.4f;
			t += (cinematic ? 0.07f : 0.04f) + random.nextFloat() * (cinematic ? 0.1f : 0.08f);
		}
		bolt.life = bolt.strokeAt[bolt.strokes - 1] + 0.42f;
	}

	/** A flash inside the cloud – sometimes, overhead, with spider lightning crawling along the base. */
	private static void flash(ClientLevel level, StormChaserModule m, double cx, double cy, double cz, double now) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Flash flash = null;
		for (Flash f : FLASHES) {
			if (!f.alive) {
				flash = f;
				break;
			}
		}
		if (flash == null) {
			return;
		}
		float front = Storm.front();
		float back = Storm.back();
		double s = back + Storm.TRAIL * 0.3 + random.nextDouble() * (front - back - Storm.TRAIL * 0.3 + 600.0);
		double q = (random.nextDouble() * 2.0 - 1.0) * 9000.0 - Storm.across() * 0.5;
		if (Storm.under() > 0.5f && random.nextFloat() < 0.45f) {
			// Overhead.
			double r = 200.0 + random.nextDouble() * 2500.0;
			double a = random.nextDouble() * Math.PI * 2.0;
			s = Math.cos(a) * r;
			q = Math.sin(a) * r;
		}
		double ax = Storm.axisX();
		double az = Storm.axisZ();
		flash.alive = true;
		flash.thundered = false;
		flash.x = cx + ax * s - az * q;
		flash.z = cz + az * s + ax * q;
		flash.y = Storm.baseY(cy) + 150.0 + random.nextDouble() * 700.0;
		flash.birth = now;
		flash.pulses = 2 + random.nextInt(4);
		float t = 0.02f;
		for (int k = 0; k < flash.pulses; k++) {
			flash.at[k] = t;
			flash.power[k] = 0.35f + random.nextFloat() * 0.65f;
			flash.width[k] = 0.03f + random.nextFloat() * 0.08f;
			t += 0.06f + random.nextFloat() * 0.22f;
		}
		flash.life = t + 0.3f;
		double dx = flash.x - cx;
		double dz = flash.z - cz;
		if (dx * dx + dz * dz < 4500.0 * 4500.0 && Storm.under() > 0.3f && random.nextFloat() < 0.4f) {
			crawler(m, flash, cy, random);
		}
	}

	/** Spider lightning under the base, flickering with its flash. */
	private static void crawler(StormChaserModule m, Flash flash, double cy, ThreadLocalRandom random) {
		Bolt bolt = freeBolt();
		double a = random.nextDouble() * Math.PI * 2.0;
		float length = 1400f + random.nextFloat() * 2600f;
		bolt.alive = true;
		bolt.crawler = true;
		bolt.grounded = false;
		bolt.x = flash.x;
		bolt.y = Storm.baseY(cy) - 25.0;
		bolt.z = flash.z;
		bolt.birth = flash.birth;
		bolt.struck = 0;
		float ex = (float) Math.cos(a) * length;
		float ez = (float) Math.sin(a) * length;
		bolt.count = GENERATOR.generate(bolt.segments, SEGMENTS, seeds++, STYLES[m.boltStyle.index()], 6, true, 0f, 0f, 0f, ex, -15f, ez);
		bolt.topX = ex * 0.5f;
		bolt.topY = 60f;
		bolt.topZ = ez * 0.5f;
		bolt.lowX = ex * 0.5f;
		bolt.lowY = -10f;
		bolt.lowZ = ez * 0.5f;
		bolt.leader = 0.1f;
		bolt.strokes = Math.min(4, flash.pulses);
		for (int k = 0; k < bolt.strokes; k++) {
			bolt.strokeAt[k] = Math.max(bolt.leader, flash.at[k]);
			bolt.strokePower[k] = flash.power[k];
		}
		bolt.life = flash.life;
		bolt.power = 0.7f;
	}

	// ---- What the rest reads -----------------------------------------------------------------------

	/** Light inside the cloud: camera-relative position and intensity of up to four flashes. */
	static float skyX(int i) {
		return SKY_X[i];
	}

	static float skyY(int i) {
		return SKY_Y[i];
	}

	static float skyZ(int i) {
		return SKY_Z[i];
	}

	static float skyIntensity(int i) {
		return SKY_I[i];
	}

	/** 0..2.5: how much the flashes light the whole sky, the fog and the air. */
	public static float globalFlash() {
		return globalFlash;
	}

	static int lights() {
		return lights;
	}

	static float lightX(int i) {
		return LIGHT_X[i];
	}

	static float lightY(int i) {
		return LIGHT_Y[i];
	}

	static float lightZ(int i) {
		return LIGHT_Z[i];
	}

	static float lightIntensity(int i) {
		return LIGHT_I[i];
	}

	static float lightEndX(int i) {
		return LIGHT_X1[i];
	}

	static float lightEndY(int i) {
		return LIGHT_Y1[i];
	}

	static float lightEndZ(int i) {
		return LIGHT_Z1[i];
	}

	/** Whether any bolt is alive (the bolt pass runs). */
	static boolean anyBolt() {
		for (Bolt bolt : BOLTS) {
			if (bolt.alive) {
				return true;
			}
		}
		return false;
	}

	/** World left: nothing in flight. */
	public static void reset() {
		for (Bolt bolt : BOLTS) {
			bolt.alive = false;
		}
		for (Flash flash : FLASHES) {
			flash.alive = false;
		}
		lights = 0;
		globalFlash = 0f;
		for (int i = 0; i < 4; i++) {
			SKY_I[i] = 0f;
		}
	}

	private static double sq(double v) {
		return v * v;
	}
}
