package net.client.visuals.render.particle;

import net.client.visuals.render.shader.LiquidPipelines;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

/**
 * One particle of the Particles module.
 *
 * <p><b>Tick (20 Hz)</b> – the motion steers the velocity towards a target (inertia keeps every
 * change smooth) made of the motion's own path, a coherent flow field (neighbouring particles drift
 * together, like real air currents), wind and optionally the player's movement. Positions are written
 * directly instead of through {@code Particle#move}, which allocates bounding boxes; the optional
 * collision is one cached block-shape lookup, and particles that land rest and fade.</p>
 *
 * <p><b>Extract (every frame)</b> – size, colour, opacity, glow and twinkle are computed from the
 * live settings, so slider changes show on particles already in the air. Procedural shapes go to the
 * client's distance-field layers with the shape encoded in the uv (see {@code ambient_particle.fsh});
 * without that shader they fall back to vanilla's translucent layer and sprites. Trails are one
 * tapered streak quad aligned with the on-screen motion. Nothing is allocated: two scratch
 * quaternions and a mutable block position, all confined to the client thread.</p>
 */
final class AmbientParticle extends SingleQuadParticle {
	/** Same atlas as vanilla's translucent particles (Sampler0: sprite shapes sample it). */
	static final Layer GLOW = new Layer(true, Layer.TRANSLUCENT.textureAtlasLocation(), LiquidPipelines.PARTICLE_GLOW, LiquidPipelines.OIT_PARTICLE_GLOW);
	static final Layer SOFT = new Layer(true, Layer.TRANSLUCENT.textureAtlasLocation(), LiquidPipelines.PARTICLE_SOFT, LiquidPipelines.OIT_PARTICLE_SOFT);

	private static final Quaternionf ROTATION = new Quaternionf();
	private static final Quaternionf STREAK_ROTATION = new Quaternionf();
	private static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
	private static final float TAU = Mth.TWO_PI;
	private static final float FADE_IN = 0.15f;
	private static final float FADE_OUT = 0.25f;
	private static final float MIN_ALPHA = 0.004f;
	/** Fallback trail: sprite copies (the vanilla pipeline has no streak shape). */
	private static final int TRAIL = 5;
	/** Ticks of motion a trail covers; shooting stars draw longer tails. */
	private static final float TRAIL_TICKS = 5f;
	private static final float STREAK_TICKS = 7f;
	/** Ticks a landed particle rests before it's gone. */
	private static final int SETTLE_TICKS = 40;

	private final ParticleStyle style;
	private final int shape;
	private final int motion;
	private final int generation;
	private final int variant;
	private final int paletteIndex;
	private final float seed;
	private final float phase;
	private final float sizeFactor;
	private final float swayX;
	private final float swayZ;
	private final float swing;
	private final float spinDirection;
	private final boolean rolls;

	/** Motion clock: advances with the Speed setting, so oscillations never jump when it changes. */
	private float motionTime;
	private float spinAngle;
	private float flip;
	private float oFlip;
	/** Spiral centre; wander / swirl home height (absolute for wander, above the player for swirl and orbit). */
	private double centerX;
	private double centerZ;
	private double homeY;
	private float radius;
	private float angle;
	private float headingX;
	private float headingY;
	private float headingZ;
	private int light;
	/** Ticks left of a fade-out, -1 while none is running. */
	private int fade = -1;
	private float fadeTotal = 1f;
	private boolean settled;

	AmbientParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, ParticleStyle style,
			int shape, int motion, int generation, Entity player) {
		super(level, x, y, z, sprite);
		this.style = style;
		this.shape = shape;
		this.motion = motion;
		this.generation = generation;
		this.hasPhysics = false;
		this.gravity = 0f;
		this.friction = 1f;

		variant = random.nextInt(4);
		paletteIndex = random.nextInt(style.palette.length);
		seed = random.nextFloat();
		phase = random.nextFloat() * TAU;
		sizeFactor = 1f - style.sizeVariation * 0.5f + random.nextFloat() * style.sizeVariation;
		float sway = random.nextFloat() * TAU;
		swayX = Mth.cos(sway);
		swayZ = Mth.sin(sway);
		swing = 0.07f + random.nextFloat() * 0.05f;
		spinDirection = random.nextBoolean() ? 1f : -1f;
		lifetime = Math.max(10, Math.round((style.life + random.nextInt(style.lifeVariation + 1)) * AmbientParticles.lifetimeScale));
		// Hearts, notes, runes, bubbles (their highlight) and flares (their streak) stay upright.
		rolls = shape == ParticleShape.SPARKLE || shape == ParticleShape.STAR || shape == ParticleShape.SNOWFLAKE
				|| shape == ParticleShape.PETAL || shape == ParticleShape.LEAF || shape == ParticleShape.CONFETTI
				|| shape == ParticleShape.DIAMOND || shape == ParticleShape.RING;
		if (rolls) {
			spinAngle = random.nextFloat() * TAU;
			roll = oRoll = spinAngle;
		}

		double dx = x - player.getX();
		double dz = z - player.getZ();
		centerX = x;
		centerZ = z;
		homeY = y;
		switch (motion) {
			case ParticleMotion.SWIRL -> {
				radius = (float) Math.max(2.0, Math.sqrt(dx * dx + dz * dz));
				homeY = y - player.getY();
			}
			case ParticleMotion.ORBIT -> {
				radius = 0.9f + AmbientParticles.area * 0.04f + random.nextFloat() * 0.5f;
				angle = random.nextFloat() * TAU;
				homeY = 0.15 + random.nextFloat() * 1.9;
				orbit(player);
				xo = this.x;
				yo = this.y;
				zo = this.z;
			}
			case ParticleMotion.SPIRAL -> {
				radius = 0.35f + random.nextFloat() * 0.8f;
				angle = random.nextFloat() * TAU;
				centerX = x - Mth.cos(angle) * radius;
				centerZ = z - Mth.sin(angle) * radius;
			}
			case ParticleMotion.METEOR -> {
				boolean windy = AmbientParticles.windX != 0f || AmbientParticles.windZ != 0f;
				float heading = windy ? (float) Mth.atan2(AmbientParticles.windZ, AmbientParticles.windX) + (random.nextFloat() - 0.5f) * 0.6f
						: random.nextFloat() * TAU;
				float v = style.speed * (0.8f + random.nextFloat() * 0.4f) * AmbientParticles.speedScale;
				xd = Mth.cos(heading) * v;
				yd = -v * 0.42f;
				zd = Mth.sin(heading) * v;
			}
			default -> {
			}
		}
		light = computeLight();
	}

	// ---- Simulation (client thread, 20 Hz) -----------------------------------------------------

	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;
		oRoll = roll;
		oFlip = flip;
		Entity player = Minecraft.getInstance().player;
		if (age++ >= lifetime || fade == 0 || AmbientParticles.presence <= 0f || player == null) {
			remove();
			return;
		}
		if (fade > 0) {
			fade--;
		}
		if (generation != AmbientParticles.generation) {
			fadeOut(10);
		}
		if (!settled) {
			simulate(player);
		}
		if (motion != ParticleMotion.ORBIT) {
			double dx = x - player.getX();
			double dz = z - player.getZ();
			float limit = AmbientParticles.area * 1.3f + 4f;
			if (dx * dx + dz * dz > limit * limit) {
				fadeOut(20); // left the area: make room for one nearer the player
			}
		}
		if (y < level.getMinY()) {
			remove();
			return;
		}
		if ((age & 3) == 0) {
			light = computeLight();
		}
		if (fade < 0) {
			AmbientParticles.ticked++; // fading particles are already being replaced
		}
	}

	/** Starts (or shortens) a fade-out; shortening keeps the current opacity, so nothing flashes. */
	private void fadeOut(int ticks) {
		if (fade < 0) {
			fade = ticks;
			fadeTotal = ticks;
		} else if (ticks < fade) {
			fadeTotal = fadeTotal * ticks / fade;
			fade = ticks;
		}
	}

	private void simulate(Entity player) {
		float speed = AmbientParticles.speedScale;
		float v = style.speed * speed;
		float turbulence = style.turbulence * AmbientParticles.turbulenceScale;
		motionTime += speed;
		float t = motionTime;

		// Coherent flow field: smooth in space and time, shared by every particle.
		float fx = 0f;
		float fy = 0f;
		float fz = 0f;
		if (turbulence > 0f) {
			float T = AmbientParticles.flowTime;
			float px = (float) (x % 4096.0);
			float py = (float) y;
			float pz = (float) (z % 4096.0);
			fx = Mth.sin(py * 0.31f + T * 0.021f + Mth.cos(pz * 0.27f + T * 0.013f)) + 0.5f * Mth.sin(pz * 0.17f - T * 0.03f);
			fz = Mth.cos(px * 0.29f - T * 0.025f + Mth.sin(py * 0.23f)) + 0.5f * Mth.cos(px * 0.13f + T * 0.019f);
			fy = 0.5f * Mth.sin(px * 0.21f + pz * 0.19f + T * 0.024f);
		}

		if (rolls) {
			spinAngle += spinDirection * style.spin * speed * AmbientParticles.spinScale;
			roll = spinAngle;
		}

		double followX = 0.0;
		double followY = 0.0;
		double followZ = 0.0;
		if (AmbientParticles.follow) {
			followX = player.getX() - player.xo;
			followY = player.getY() - player.yo;
			followZ = player.getZ() - player.zo;
			centerX += followX;
			centerZ += followZ;
			homeY += motion == ParticleMotion.WANDER ? followY : 0.0;
		}

		float tx;
		float ty;
		float tz;
		float inertia = 0.12f;
		float windScale = 1f;
		switch (motion) {
			case ParticleMotion.ORBIT -> {
				angle += 1.6f * v / radius;
				orbit(player);
				return;
			}
			case ParticleMotion.SPIRAL -> {
				angle += (0.07f + seed * 0.05f) * speed;
				radius += 0.002f * speed;
				centerX += AmbientParticles.windX * 0.5f;
				centerZ += AmbientParticles.windZ * 0.5f;
				x = centerX + Mth.cos(angle) * radius;
				z = centerZ + Mth.sin(angle) * radius;
				y += v * 0.8f + followY;
				return;
			}
			case ParticleMotion.METEOR -> {
				x += xd + followX;
				y += yd + followY;
				z += zd + followZ;
				return;
			}
			case ParticleMotion.FALL -> {
				tx = fx * v * 0.8f * turbulence;
				ty = -v;
				tz = fz * v * 0.8f * turbulence;
			}
			case ParticleMotion.FLUTTER -> {
				// A falling leaf: pendulum sway, sinking fastest at the bottom of each swing.
				float s = Mth.sin(t * swing + phase);
				float c = Mth.cos(t * swing + phase);
				tx = swayX * s * v * 1.3f + fx * v * 0.5f * turbulence;
				tz = swayZ * s * v * 1.3f + fz * v * 0.5f * turbulence;
				ty = -v * (0.65f + 0.45f * Math.abs(c));
				if (rolls) {
					roll = spinAngle + 0.45f * s;
				}
				flip = style.tumble * Mth.sin(t * swing * 1.3f + phase * 1.7f);
				inertia = 0.15f;
			}
			case ParticleMotion.RISE -> {
				tx = Mth.sin(t * 0.09f + phase) * v * 0.35f + fx * v * 0.7f * turbulence;
				ty = v;
				tz = Mth.cos(t * 0.07f + phase) * v * 0.35f + fz * v * 0.7f * turbulence;
				windScale = 0.6f;
			}
			case ParticleMotion.WANDER -> {
				if (age % (20 + (int) (seed * 25f)) == 1) {
					float heading = random.nextFloat() * TAU;
					headingX = Mth.cos(heading);
					headingZ = Mth.sin(heading);
					headingY = (random.nextFloat() - 0.5f) * 0.5f;
				}
				tx = headingX * v + fx * v * 0.4f * turbulence;
				ty = headingY * v + (float) (homeY - y) * 0.02f;
				tz = headingZ * v + fz * v * 0.4f * turbulence;
				inertia = 0.06f;
				windScale = 0.5f;
			}
			case ParticleMotion.SWIRL -> {
				// One shared vortex around the player, springing back to each particle's own radius.
				double dx = x - player.getX();
				double dz = z - player.getZ();
				double r = Math.sqrt(dx * dx + dz * dz) + 1.0e-3;
				double pull = (radius - r) * 0.02;
				tx = (float) (-dz / r * v * 2.2 + dx / r * pull);
				tz = (float) (dx / r * v * 2.2 + dz / r * pull);
				ty = (float) ((player.getY() + homeY - y) * 0.02) + Mth.sin(t * 0.05f + phase) * v * 0.4f;
				inertia = 0.1f;
				windScale = 0.4f;
			}
			case ParticleMotion.STILL -> {
				tx = Mth.sin(t * 0.13f + phase) * v * 0.25f + fx * v * 0.35f * turbulence;
				ty = Mth.cos(t * 0.11f + phase) * v * 0.25f + fy * v * 0.35f * turbulence;
				tz = Mth.sin(t * 0.09f + phase * 1.7f) * v * 0.25f + fz * v * 0.35f * turbulence;
				windScale = 0.3f;
			}
			default -> {
				tx = swayX * v * 0.6f + fx * v * 1.2f * turbulence;
				ty = Mth.sin(t * 0.045f + phase) * v * 0.5f + fy * v * 0.6f * turbulence;
				tz = swayZ * v * 0.6f + fz * v * 1.2f * turbulence;
			}
		}
		tx += AmbientParticles.windX * windScale;
		tz += AmbientParticles.windZ * windScale;
		xd += (tx - xd) * inertia;
		yd += (ty - yd) * inertia;
		zd += (tz - zd) * inertia;

		double nx = x + xd + followX;
		double ny = y + yd + followY;
		double nz = z + zd + followZ;
		if (AmbientParticles.collide && land(nx, ny, nz)) {
			return;
		}
		x = nx;
		y = ny;
		z = nz;
	}

	/** Places an orbiting particle on its ring around the player. */
	private void orbit(Entity player) {
		float t = motionTime;
		double r = radius + Mth.sin(t * 0.05f + phase) * 0.12f;
		x = player.getX() + Mth.cos(angle) * r;
		z = player.getZ() + Mth.sin(angle) * r;
		y = player.getY() + homeY + Mth.sin(t * 0.06f + phase * 1.3f) * 0.15f;
	}

	/**
	 * Whether the step to (nx, ny, nz) enters a block's collision box. Particles falling onto it come to
	 * rest on top (snow on the ground, petals on leaves), others stop where they are; both fade.
	 */
	private boolean land(double nx, double ny, double nz) {
		POS.set(nx, ny, nz);
		BlockState state = level.getBlockState(POS);
		if (state.isAir()) {
			return false;
		}
		VoxelShape box = state.getCollisionShape(level, POS);
		if (box.isEmpty()) {
			return false;
		}
		double lx = nx - POS.getX();
		double ly = ny - POS.getY();
		double lz = nz - POS.getZ();
		if (ly < box.min(Direction.Axis.Y) || ly > box.max(Direction.Axis.Y) || lx < box.min(Direction.Axis.X)
				|| lx > box.max(Direction.Axis.X) || lz < box.min(Direction.Axis.Z) || lz > box.max(Direction.Axis.Z)) {
			return false;
		}
		double top = POS.getY() + box.max(Direction.Axis.Y);
		if (yd < 0.0 && y >= top - 0.05) {
			x = nx;
			z = nz;
			y = top + 0.01;
		}
		xd = yd = zd = 0.0;
		flip *= 0.2f; // lying almost flat
		settled = true;
		fadeOut(SETTLE_TICKS);
		return true;
	}

	private int computeLight() {
		POS.set(x, y, z);
		return level.isLoaded(POS) ? LightCoordsUtil.getLightCoords(level, POS) : LightCoordsUtil.FULL_SKY;
	}

	// ---- Rendering (render thread, every frame) ------------------------------------------------

	@Override
	public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
		float presence = Mth.lerp(partialTick, AmbientParticles.presenceO, AmbientParticles.presence);
		float alpha = AmbientParticles.opacity * style.alpha * presence;
		float life = Math.min(1f, (age + partialTick) / lifetime);
		float envelope = AmbientParticles.smoothstep(0f, FADE_IN, life) * (1f - AmbientParticles.smoothstep(1f - FADE_OUT, 1f, life));
		if (fade >= 0) {
			envelope *= Math.max(0f, fade - partialTick) / fadeTotal;
		}
		alpha *= envelope;
		if (alpha <= MIN_ALPHA) {
			return;
		}
		// Particles bloom in from 60 % of their size and shrink away again.
		float size = style.size * sizeFactor * AmbientParticles.sizeScale * (0.6f + 0.4f * envelope);

		float amount = Math.min(1f, style.twinkleAmount * AmbientParticles.twinkleScale);
		if (amount > 0f) {
			float t = age + partialTick;
			switch (style.twinkle) {
				case ParticleStyle.TWINKLE_BLINK -> {
					float pulse = AmbientParticles.smoothstep(0.15f, 0.95f, Mth.sin(t * 0.06f + phase));
					alpha *= 1f - amount * (1f - (0.06f + 0.94f * pulse));
					size *= 0.85f + 0.15f * pulse;
				}
				case ParticleStyle.TWINKLE_FLICKER -> {
					float flicker = 0.7f + 0.3f * Mth.sin(t * 0.9f + phase) * Mth.sin(t * 1.37f + phase * 1.7f);
					alpha *= 1f - amount * (1f - flicker);
				}
				case ParticleStyle.TWINKLE_GLINT -> {
					float s = Math.max(0f, Mth.sin(t * 0.11f + phase));
					float s2 = s * s;
					float s4 = s2 * s2;
					float glint = s4 * s4 * s4 * s4 * s4 * s4; // s^24: rare, sharp
					alpha *= 1f - amount * 0.6f * (1f - glint);
					size *= 1f + 0.7f * glint * amount;
				}
				default -> {
					float shimmer = 0.5f + 0.5f * Mth.sin(t * 0.19f + phase) * Mth.sin(t * 0.071f + phase * 2.3f);
					alpha *= 1f - amount * (1f - shimmer);
				}
			}
		}

		double ix = Mth.lerp(partialTick, xo, x);
		double iy = Mth.lerp(partialTick, yo, y);
		double iz = Mth.lerp(partialTick, zo, z);
		Vec3 cam = camera.position();
		float rx = (float) (ix - cam.x());
		float ry = (float) (iy - cam.y());
		float rz = (float) (iz - cam.z());
		if (AmbientParticles.cameraFade) {
			alpha *= AmbientParticles.smoothstep(0.4f, 1.8f, Mth.sqrt(rx * rx + ry * ry + rz * rz));
		}
		if (alpha <= MIN_ALPHA) {
			return;
		}

		boolean custom = AmbientParticles.customShaders;
		Layer layer = custom ? (AmbientParticles.additive ? GLOW : SOFT) : Layer.TRANSLUCENT;
		int rgb = custom || !ParticleShape.coloredSprite(shape)
				? AmbientParticles.color(style, paletteIndex, seed, life, AmbientParticles.clock + partialTick, ix, iz)
				: 0xFFFFFF;
		int color = (Math.round(Math.min(alpha, 1f) * 255f) << 24) | (rgb & 0xFFFFFF);
		float u0;
		float u1;
		float v0;
		float v1;
		if (custom && !ParticleShape.isSprite(shape)) {
			u0 = 4f * (shape + 1) + 1.5f;
			v0 = 4f * (AmbientParticles.glowLevel * 4 + variant) + 1.5f;
			u1 = u0 + 1f;
			v1 = v0 + 1f;
		} else {
			u0 = sprite.getU0();
			u1 = sprite.getU1();
			v0 = sprite.getV0();
			v1 = sprite.getV1();
		}
		int lightCoords = AmbientParticles.emissive ? LightCoordsUtil.FULL_BRIGHT : light;

		Quaternionf q = ROTATION.set(camera.rotation());
		float r = Mth.lerp(partialTick, oRoll, roll);
		if (r != 0f) {
			q.rotateZ(r);
		}
		float f = Mth.lerp(partialTick, oFlip, flip);
		if (f != 0f) {
			q.rotateX(f);
		}
		state.add(layer, rx, ry, rz, q.x, q.y, q.z, q.w, size, u0, u1, v0, v1, color, lightCoords);

		if (AmbientParticles.trails || style.streak) {
			float vx = (float) (x - xo);
			float vy = (float) (y - yo);
			float vz = (float) (z - zo);
			if (vx * vx + vy * vy + vz * vz > 1.0e-6f) {
				if (custom) {
					streak(state, layer, camera.rotation(), rx, ry, rz, vx, vy, vz, size, alpha, rgb, lightCoords);
				} else {
					copies(state, layer, q, rx, ry, rz, vx, vy, vz, size, u0, u1, v0, v1, alpha, rgb, lightCoords);
				}
			}
		}
	}

	/**
	 * One tapered streak behind the particle, aligned with its motion as seen on screen (only the part
	 * of the velocity across the view shows; its length is that projection).
	 */
	private void streak(QuadParticleRenderState state, Layer layer, Quaternionf view, float rx, float ry, float rz,
			float vx, float vy, float vz, float size, float alpha, int rgb, int lightCoords) {
		// The quad's on-screen axes: the camera rotation applied to +X and +Y.
		float qx = view.x;
		float qy = view.y;
		float qz = view.z;
		float qw = view.w;
		float rightX = 1f - 2f * (qy * qy + qz * qz);
		float rightY = 2f * (qx * qy + qz * qw);
		float rightZ = 2f * (qx * qz - qy * qw);
		float upX = 2f * (qx * qy - qz * qw);
		float upY = 1f - 2f * (qx * qx + qz * qz);
		float upZ = 2f * (qy * qz + qx * qw);
		float across = vx * rightX + vy * rightY + vz * rightZ;
		float upward = vx * upX + vy * upY + vz * upZ;
		float perpendicular = Mth.sqrt(across * across + upward * upward);
		float length = perpendicular * (style.streak ? STREAK_TICKS : TRAIL_TICKS);
		if (length < size * 0.3f) {
			return;
		}
		float half = (length + size) * 0.5f;
		float back = (half - size * 0.5f) / perpendicular;
		int level = Math.clamp(Math.round(0.4f * size / half * 64f) - 1, 0, 15);
		float u0 = 4f * (ParticleShape.STREAK + level + 1) + 1.5f;
		float v0 = 4f * (AmbientParticles.glowLevel * 4) + 1.5f;
		int a = Math.round(Math.min(alpha, 1f) * 0.85f * 255f);
		Quaternionf q = STREAK_ROTATION.set(view).rotateZ((float) Mth.atan2(upward, across));
		state.add(layer, rx - vx * back, ry - vy * back, rz - vz * back, q.x, q.y, q.z, q.w, half,
				u0, u0 + 1f, v0, v0 + 1f, (a << 24) | (rgb & 0xFFFFFF), lightCoords);
	}

	/** Fading, shrinking copies back along the velocity. */
	private static void copies(QuadParticleRenderState state, Layer layer, Quaternionf q, float rx, float ry, float rz,
			float vx, float vy, float vz, float size, float u0, float u1, float v0, float v1, float alpha, int rgb, int lightCoords) {
		for (int i = 1; i <= TRAIL; i++) {
			float k = 1f - i / (TRAIL + 1f);
			int a = Math.round(Math.min(alpha, 1f) * k * k * 0.8f * 255f);
			if (a < 2) {
				break;
			}
			float back = i * 1.5f;
			state.add(layer, rx - vx * back, ry - vy * back, rz - vz * back, q.x, q.y, q.z, q.w, size * (0.4f + 0.6f * k),
					u0, u1, v0, v1, (a << 24) | (rgb & 0xFFFFFF), lightCoords);
		}
	}

	@Override
	protected Layer getLayer() {
		if (!AmbientParticles.customShaders) {
			return Layer.TRANSLUCENT;
		}
		return AmbientParticles.additive ? GLOW : SOFT;
	}

	@Override
	protected int getLightCoords(float partialTick) {
		return AmbientParticles.emissive ? LightCoordsUtil.FULL_BRIGHT : light;
	}
}
