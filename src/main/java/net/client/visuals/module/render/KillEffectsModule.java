package net.client.visuals.module.render;

import net.client.visuals.module.Category;
import net.client.visuals.module.Module;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;

/**
 * Kill Effects: a client-side flourish where something you hit dies – a lightning strike or a burst
 * of particles, with a sound. Only you see and hear it.
 *
 * <p>Entities you attack are remembered for ten seconds (ids only, fixed arrays – nothing is held
 * onto); a tick check fires the effect when one of them reaches zero health. The lightning bolt is
 * a visual-only entity added to the client level under a negative id, so it can never collide with
 * an id the server assigns.</p>
 */
public final class KillEffectsModule extends Module {
	public static final String[] MODES = {"lightning", "totem", "hearts", "flames", "souls", "sparks"};
	private static final int LIGHTNING = 0;
	private static final int TRACKED = 8;
	private static final int TRACK_TICKS = 200;

	private static KillEffectsModule instance;
	private static int nextBoltId = -0x40000000;

	public final ModeSetting mode = add(new ModeSetting("mode", "liquidclient.kill_effects.mode", MODES, 0));
	public final BooleanSetting sound = add(new BooleanSetting("sound", "liquidclient.kill_effects.sound", true));
	public final BooleanSetting playersOnly = add(new BooleanSetting("players_only", "liquidclient.kill_effects.players_only", false));

	private final int[] ids = new int[TRACKED];
	private final int[] ticksLeft = new int[TRACKED];

	public KillEffectsModule() {
		super("kill_effects", Category.RENDER, 0xE08C);
		instance = this;
	}

	/** The local player attacked {@code entity} (client thread). */
	public static void onAttack(Entity entity) {
		KillEffectsModule m = instance;
		if (m == null || !m.isEnabled() || !(entity instanceof LivingEntity) || m.playersOnly.get() && !(entity instanceof Player)) {
			return;
		}
		int slot = 0;
		for (int i = 0; i < TRACKED; i++) {
			if (m.ticksLeft[i] > 0 && m.ids[i] == entity.getId()) {
				slot = i;
				break;
			}
			if (m.ticksLeft[i] < m.ticksLeft[slot]) {
				slot = i;
			}
		}
		m.ids[slot] = entity.getId();
		m.ticksLeft[slot] = TRACK_TICKS;
	}

	@Override
	public void clientTick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		for (int i = 0; i < TRACKED; i++) {
			if (ticksLeft[i] <= 0) {
				continue;
			}
			ticksLeft[i]--;
			Entity entity = level != null ? level.getEntity(ids[i]) : null;
			if (entity == null) {
				ticksLeft[i] = 0;
			} else if (entity instanceof LivingEntity living && living.isDeadOrDying()) {
				ticksLeft[i] = 0;
				play(level, living);
			}
		}
	}

	@Override
	protected void onDisable() {
		Arrays.fill(ticksLeft, 0);
	}

	private void play(ClientLevel level, LivingEntity victim) {
		double x = victim.getX();
		double y = victim.getY();
		double z = victim.getZ();
		double cy = y + victim.getBbHeight() * 0.5;
		RandomSource random = level.getRandom();
		int m = mode.index();
		if (m == LIGHTNING) {
			LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
			if (bolt != null) {
				bolt.setId(nextBoltId--);
				bolt.snapTo(x, y, z);
				bolt.setVisualOnly(true);
				bolt.setSilent(true); // vanilla's thunder is heard across the whole world
				level.addEntity(bolt);
			}
			sound(level, x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.9f, 0.9f + random.nextFloat() * 0.2f);
			sound(level, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.45f, 0.9f + random.nextFloat() * 0.2f);
			return;
		}
		switch (m) {
			case 1 -> {
				burst(level, random, ParticleTypes.TOTEM_OF_UNDYING, x, cy, z, 46, 0.55);
				sound(level, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f, 1.2f);
			}
			case 2 -> {
				ring(level, random, ParticleTypes.HEART, x, y + victim.getBbHeight() + 0.2, z, 12, 0.7);
				sound(level, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 0.7f);
			}
			case 3 -> {
				spiral(level, ParticleTypes.FLAME, x, y, z, victim.getBbHeight(), 36);
				burst(level, random, ParticleTypes.FLAME, x, cy, z, 18, 0.18);
				sound(level, x, y, z, SoundEvents.FIREWORK_ROCKET_BLAST, 0.8f, 0.8f);
			}
			case 4 -> {
				spiral(level, ParticleTypes.SOUL_FIRE_FLAME, x, y, z, victim.getBbHeight(), 36);
				for (int i = 0; i < 6; i++) {
					level.addParticle(ParticleTypes.SOUL, x + random.nextGaussian() * 0.3, cy + random.nextDouble() * 0.5,
							z + random.nextGaussian() * 0.3, 0.0, 0.06 + random.nextDouble() * 0.04, 0.0);
				}
				sound(level, x, y, z, SoundEvents.SOUL_ESCAPE.value(), 1.4f, 0.9f);
			}
			default -> {
				burst(level, random, ParticleTypes.END_ROD, x, cy, z, 28, 0.22);
				burst(level, random, ParticleTypes.ELECTRIC_SPARK, x, cy, z, 24, 0.6);
				sound(level, x, y, z, SoundEvents.FIREWORK_ROCKET_TWINKLE, 0.9f, 1.1f);
			}
		}
	}

	private void sound(ClientLevel level, double x, double y, double z, SoundEvent event, float volume, float pitch) {
		if (sound.get()) {
			level.playLocalSound(x, y, z, event, SoundSource.PLAYERS, volume, pitch, false);
		}
	}

	private static void burst(ClientLevel level, RandomSource random, ParticleOptions particle, double x, double y, double z, int count, double speed) {
		for (int i = 0; i < count; i++) {
			double vx = random.nextGaussian();
			double vy = random.nextGaussian();
			double vz = random.nextGaussian();
			double len = Math.max(1e-4, Math.sqrt(vx * vx + vy * vy + vz * vz));
			double s = speed * (0.5 + random.nextDouble() * 0.5) / len;
			level.addParticle(particle, x, y, z, vx * s, vy * s + speed * 0.25, vz * s);
		}
	}

	private static void ring(ClientLevel level, RandomSource random, ParticleOptions particle, double x, double y, double z, int count, double radius) {
		for (int i = 0; i < count; i++) {
			double a = i * (Math.PI * 2.0 / count) + random.nextDouble() * 0.2;
			level.addParticle(particle, x + Math.cos(a) * radius, y + random.nextDouble() * 0.2, z + Math.sin(a) * radius, 0.0, 0.05, 0.0);
		}
	}

	/** A rising double helix around the body. */
	private static void spiral(ClientLevel level, ParticleOptions particle, double x, double y, double z, float height, int count) {
		for (int i = 0; i < count; i++) {
			double t = i / (double) count;
			double a = t * Math.PI * 6.0 + (i % 2) * Math.PI;
			double r = 0.55 - t * 0.25;
			level.addParticle(particle, x + Math.cos(a) * r, y + t * (height + 0.4), z + Math.sin(a) * r, 0.0, 0.015, 0.0);
		}
	}
}
