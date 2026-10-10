package net.client.visuals.render.storm;

import net.client.visuals.LiquidClient;
import net.client.visuals.module.render.StormChaserModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The storm's sound (synthesised by {@code tools/storm_sounds.py}, registered in {@code sounds.json}).
 *
 * <ul>
 *   <li><b>Thunder</b> – every strike and cloud flash schedules its thunder for when the sound gets
 *       here: 343 m/s from the nearest part of the channel ({@link Lightning}). A close strike is a
 *       sharp crack with a rumble behind it, a nearer one a heavy clap, far ones a long low roll
 *       (several variants each, pitch varied). Played from the strike's direction on the Weather
 *       channel, so the Master and Weather sliders apply, and scaled by the Thunder Volume setting.
 *       The queue runs on the storm clock: in a frozen world thunder waits.</li>
 *   <li><b>Rain and wind</b> – two looping beds (stereo, around you) whose volume follows the storm:
 *       the roar of heavy rain on every surface (muffled under a roof) and a wind bed that swells
 *       with the gusts.</li>
 * </ul>
 * Client-only: the events are played by identifier and needn't be in the (synced) sound registry.
 */
public final class StormSounds {
	public static final SoundEvent THUNDER_CRACK = event("storm.thunder.crack");
	public static final SoundEvent THUNDER_NEAR = event("storm.thunder.near");
	public static final SoundEvent THUNDER_FAR = event("storm.thunder.far");
	public static final SoundEvent RAIN = event("storm.rain");
	public static final SoundEvent WIND = event("storm.wind");

	private static final int QUEUE = 16;
	/** Thunder is placed this far from you in its direction, so the sound comes from there. */
	private static final float EMITTER = 6f;
	private static final double[] AT = new double[QUEUE];
	private static final float[] DISTANCE = new float[QUEUE];
	private static final float[] DX = new float[QUEUE];
	private static final float[] DY = new float[QUEUE];
	private static final float[] DZ = new float[QUEUE];
	private static final boolean[] CLOUD = new boolean[QUEUE];
	private static final boolean[] PENDING = new boolean[QUEUE];

	private static @Nullable Bed rain;
	private static @Nullable Bed wind;
	private static float rainTarget;
	private static float windTarget;
	private static float windPitch = 1f;
	private static boolean covered;

	private StormSounds() {
	}

	private static SoundEvent event(String path) {
		return SoundEvent.createVariableRangeEvent(LiquidClient.id(path));
	}

	/** Thunder from a stroke or flash at camera-relative (rx, ry, rz), to be heard at storm time {@code at}. */
	static void thunder(double at, float distance, float rx, float ry, float rz, boolean cloud) {
		int slot = -1;
		for (int i = 0; i < QUEUE; i++) {
			if (!PENDING[i]) {
				slot = i;
				break;
			}
		}
		if (slot < 0) {
			// Full: the one that would come last makes room.
			slot = 0;
			for (int i = 1; i < QUEUE; i++) {
				if (AT[i] > AT[slot]) {
					slot = i;
				}
			}
		}
		float length = Math.max(1.0e-3f, (float) Math.sqrt(rx * rx + ry * ry + rz * rz));
		AT[slot] = at;
		DISTANCE[slot] = distance;
		DX[slot] = rx / length;
		DY[slot] = ry / length;
		DZ[slot] = rz / length;
		CLOUD[slot] = cloud;
		PENDING[slot] = true;
	}

	/** Once per frame (render thread): thunder that has arrived, and the beds' volumes. */
	public static void frame(ClientLevel level, double cx, double cy, double cz) {
		StormChaserModule m = Storm.module();
		double now = Storm.time();
		for (int i = 0; i < QUEUE; i++) {
			if (PENDING[i] && now >= AT[i]) {
				PENDING[i] = false;
				play(level, m, i, cx, cy, cz);
			}
		}
		float presence = Storm.presence();
		float strength = Math.min(1.2f, m.intensity.getFloat() / 100f);
		float muffle = covered ? 0.38f : 1f;
		rainTarget = Storm.rain() * presence * 0.85f * strength * muffle;
		float gusts = Math.clamp(Storm.wind(), 0f, 1.6f);
		windTarget = Math.clamp(gusts * 0.55f + Storm.influence() * 0.12f, 0f, 1f) * presence;
		windPitch = 0.85f + 0.25f * Math.min(1f, gusts);
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.isPaused()) {
			// The sound engine doesn't tick paused sounds: that's not losing them.
			if (rain != null) {
				rain.touch();
			}
			if (wind != null) {
				wind.touch();
			}
		} else {
			rain = keep(minecraft, rain, RAIN, rainTarget);
			wind = keep(minecraft, wind, WIND, windTarget);
		}
	}

	/** Client tick: whether there's a roof over you (the rain bed is muffled under one). */
	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		if (level == null || !Storm.active()) {
			covered = false;
			return;
		}
		var camera = minecraft.gameRenderer.mainCamera().position();
		int x = (int) Math.floor(camera.x);
		int z = (int) Math.floor(camera.z);
		covered = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) > camera.y + 1.0;
	}

	private static void play(ClientLevel level, StormChaserModule m, int i, double cx, double cy, double cz) {
		float volume = m.thunderVolume.getFloat() / 100f;
		if (volume <= 0f || !Storm.active()) {
			return;
		}
		ThreadLocalRandom random = ThreadLocalRandom.current();
		float d = DISTANCE[i];
		SoundEvent event;
		float pitch;
		if (!CLOUD[i] && d < 380f) {
			event = THUNDER_CRACK;
			volume *= 1f;
			pitch = 0.92f + random.nextFloat() * 0.14f;
		} else if (d < 2600f) {
			event = THUNDER_NEAR;
			volume *= 0.95f - 0.35f * (d - 380f) / 2220f;
			pitch = 0.88f + random.nextFloat() * 0.16f;
		} else {
			event = THUNDER_FAR;
			volume *= 0.1f + 0.55f * (1f - smoothstep(2600f, 18000f, d));
			pitch = 0.78f + random.nextFloat() * 0.16f;
		}
		if (CLOUD[i]) {
			volume *= 0.7f;
		}
		level.playLocalSound(cx + DX[i] * EMITTER, cy + DY[i] * EMITTER, cz + DZ[i] * EMITTER, event, SoundSource.WEATHER, volume, pitch, false);
	}

	/** Keeps a bed playing while it's wanted; starts a new one if the sound engine dropped it. */
	private static @Nullable Bed keep(Minecraft minecraft, @Nullable Bed bed, SoundEvent event, float target) {
		if (bed != null && (bed.isStopped() || bed.lost())) {
			bed = null;
		}
		if (bed == null && target > 0.02f) {
			bed = new Bed(event);
			minecraft.getSoundManager().play(bed);
		}
		if (bed != null) {
			bed.target = target;
			bed.pitchTarget = event == WIND ? windPitch : 1f;
		}
		return bed;
	}

	/** World left: the beds stop, nothing waits to be heard. */
	public static void stop() {
		for (int i = 0; i < QUEUE; i++) {
			PENDING[i] = false;
		}
		if (rain != null) {
			rain.target = 0f;
			rain.end();
			rain = null;
		}
		if (wind != null) {
			wind.target = 0f;
			wind.end();
			wind = null;
		}
	}

	private static float smoothstep(float edge0, float edge1, float v) {
		float t = Math.clamp((v - edge0) / (edge1 - edge0), 0f, 1f);
		return t * t * (3f - 2f * t);
	}

	/** A looping bed around the listener whose volume eases towards a target, ticked by the sound engine. */
	private static final class Bed extends AbstractTickableSoundInstance {
		float target;
		float pitchTarget = 1f;
		private long lastTick = System.nanoTime();
		private boolean ended;

		Bed(SoundEvent event) {
			super(event, SoundSource.WEATHER, RandomSource.create());
			looping = true;
			delay = 0;
			volume = 0.02f;
			relative = true;
			attenuation = SoundInstance.Attenuation.NONE;
			x = 0.0;
			y = 0.0;
			z = 0.0;
		}

		@Override
		public void tick() {
			lastTick = System.nanoTime();
			if (ended) {
				stop();
				return;
			}
			volume += (target - volume) * 0.08f;
			pitch += (pitchTarget - pitch) * 0.05f;
			if (target <= 0f && volume < 0.006f) {
				stop();
			}
		}

		void end() {
			ended = true;
		}

		void touch() {
			lastTick = System.nanoTime();
		}

		/** Not ticked for a while although the game runs: the sound engine let it go. */
		boolean lost() {
			return System.nanoTime() - lastTick > 3_000_000_000L;
		}
	}
}
