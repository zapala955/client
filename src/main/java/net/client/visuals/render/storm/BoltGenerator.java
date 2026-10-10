package net.client.visuals.render.storm;

/**
 * Builds a lightning channel's geometry (prototyped offline first, see {@code Lightning}).
 *
 * <ul>
 *   <li><b>Main channel</b> – recursive midpoint displacement of the segment from the cloud to the
 *       ground: every level halves each segment and pushes its midpoint sideways by a fraction of its
 *       length (more at fine levels, so it's jagged at every scale, like a real channel's fractal
 *       tortuosity; vertical offsets are damped – channels zig-zag sideways).</li>
 *   <li><b>Branches</b> – along the upper channel, steeply down-and-out, their length a share of the
 *       height left; displaced the same way, thinner and dimmer, tapering to their tips. Branches
 *       sprout sub-branches. "Forked" lets one branch reach the ground at full strength.</li>
 *   <li><b>Crawlers</b> – the same tree laid sideways under the cloud base (spider lightning).</li>
 * </ul>
 *
 * <p>Output per segment, as the bolt shader reads it ({@code storm_bolt.vsh}): two vec4s –
 * (A.xyz, width at A) and (B.xyz, packed: order &lt;&lt; 22 | arrival · 1023 &lt;&lt; 12 | brightness · 255
 * &lt;&lt; 4 | width ratio B / A · 15). Arrival is when the stepped leader reaches the segment (0 top ..
 * 1 ground). Positions are relative to the bolt's origin. Fixed arrays and a seeded xorshift: no
 * allocation, and the same seed always gives the same bolt (Photo Mode can hold one).</p>
 */
final class BoltGenerator {
	/** Order 3 marks the ground-glow sprite (A = centre, width = radius). */
	static final int ORDER_SPRITE = 3;
	private static final float[] SCHEDULE = {0.13f, 0.15f, 0.17f, 0.19f, 0.2f, 0.2f, 0.19f, 0.19f};
	private static final int MAX_POINTS = (1 << 7) + 1;
	private static final int QUEUE = 96;

	private final float[] px = new float[MAX_POINTS];
	private final float[] py = new float[MAX_POINTS];
	private final float[] pz = new float[MAX_POINTS];
	private final float[] cum = new float[MAX_POINTS];
	private final float[] mainX = new float[MAX_POINTS];
	private final float[] mainY = new float[MAX_POINTS];
	private final float[] mainZ = new float[MAX_POINTS];
	private final float[] mainCum = new float[MAX_POINTS];
	// Branches waiting to be built: root, tip, order, arrival at the root, length.
	private final float[] qa = new float[QUEUE * 3];
	private final float[] qb = new float[QUEUE * 3];
	private final int[] qOrder = new int[QUEUE];
	private final float[] qArrival = new float[QUEUE];
	private final float[] qLength = new float[QUEUE];
	private final boolean[] qGround = new boolean[QUEUE];
	private int queued;
	private long state;
	// scratch for the perpendicular basis
	private float ux;
	private float uy;
	private float uz;
	private float vx;
	private float vy;
	private float vz;

	/** Tunables of a bolt style. */
	record Style(float tortuosity, float branching, float subBranching, float width, boolean forked) {
	}

	/**
	 * Builds a bolt from {@code (tx, ty, tz)} (top) to {@code (bx, by, bz)} (bottom), relative to the
	 * bolt's origin, into {@code out} (8 floats per segment). Returns the number of segments.
	 *
	 * @param levels  main-channel detail (2^levels segments): fewer for distant bolts
	 * @param budget  segment limit (the output's capacity, one kept free for the ground glow)
	 */
	int generate(float[] out, int budget, long seed, Style style, int levels, boolean crawler,
			float tx, float ty, float tz, float bx, float by, float bz) {
		state = seed * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L;
		if (state == 0L) {
			state = 1L;
		}
		queued = 0;
		int n = displace(tx, ty, tz, bx, by, bz, levels, style.tortuosity(), crawler ? 1f : 0.55f);
		// Keep the main channel: the branches reuse the working arrays.
		mainCum[0] = 0f;
		for (int i = 0; i <= n; i++) {
			mainX[i] = px[i];
			mainY[i] = py[i];
			mainZ[i] = pz[i];
			if (i > 0) {
				mainCum[i] = mainCum[i - 1] + dist(px[i - 1], py[i - 1], pz[i - 1], px[i], py[i], pz[i]);
			}
		}
		float total = Math.max(1f, mainCum[n]);
		float width = style.width();
		int count = 0;
		for (int i = 0; i < n && count < budget; i++) {
			count = put(out, count, mainX[i], mainY[i], mainZ[i], mainX[i + 1], mainY[i + 1], mainZ[i + 1], width, 1f, 0,
					mainCum[i] / total, 1f);
		}
		float ax = bx - tx;
		float ay = by - ty;
		float az = bz - tz;
		float height = (float) Math.sqrt(ax * ax + ay * ay + az * az);
		ax /= height;
		ay /= height;
		az /= height;

		// Branches off the upper channel.
		boolean forkUsed = !style.forked();
		for (int i = 2; i < n - 3; i++) {
			float f = mainCum[i] / total;
			if (f > 0.9f) {
				break;
			}
			if (uniform() >= style.branching() * (1.3f - f)) {
				continue;
			}
			float lx = mainX[i + 1] - mainX[i - 1];
			float ly = mainY[i + 1] - mainY[i - 1];
			float lz = mainZ[i + 1] - mainZ[i - 1];
			float ll = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
			if (ll < 1.0e-4f) {
				continue;
			}
			lx /= ll;
			ly /= ll;
			lz /= ll;
			basis(ax, ay, az);
			if (crawler) {
				// Spread out along the cloud base, never up into it or down to the ground.
				float hx = ux;
				float hz = uz;
				if (Math.abs(uy) > 0.99f) {
					hx = vx;
					hz = vz;
				}
				float hl = (float) Math.sqrt(hx * hx + hz * hz);
				ux = hx / hl;
				uy = 0f;
				uz = hz / hl;
				vx = 0f;
				vy = 0f;
				vz = 0f;
			}
			float angle = uniform() * (float) (Math.PI * 2.0);
			float c = (float) Math.cos(angle);
			float s = (float) Math.sin(angle);
			float ox = ux * c + vx * s;
			float oy = uy * c + vy * s;
			float oz = uz * c + vz * s;
			if (crawler && uniform() < 0.5f) {
				ox = -ox;
				oy = -oy;
				oz = -oz;
			}
			float spread = 0.45f + uniform() * 0.6f;
			float along = lx * ax + ly * ay + lz * az;
			float fx = along > 0.4f ? lx : ax;
			float fy = along > 0.4f ? ly : ay;
			float fz = along > 0.4f ? lz : az;
			float dx = fx * 0.6f + ox * spread + ax * 0.45f;
			float dy = fy * 0.6f + oy * spread + ay * 0.45f;
			float dz = fz * 0.6f + oz * spread + az * 0.45f;
			float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			dx /= dl;
			dy /= dl;
			dz /= dl;
			float remaining = height * (1f - f);
			float u1 = uniform();
			float u2 = uniform();
			float length = remaining * (0.12f + u1 * u2 * 0.55f) / Math.max(0.4f, dx * ax + dy * ay + dz * az);
			float ex = mainX[i] + dx * length;
			float ey = mainY[i] + dy * length;
			float ez = mainZ[i] + dz * length;
			boolean ground = false;
			if (!forkUsed && !crawler && f > 0.25f && f < 0.6f && uniform() < 0.5f) {
				// A fork: this branch carries on to the ground, as bright as the main channel.
				forkUsed = true;
				ground = true;
				float side = 0.35f * height * (1f - f);
				ex = bx + ox * side;
				ey = by;
				ez = bz + oz * side;
				length = dist(mainX[i], mainY[i], mainZ[i], ex, ey, ez);
			} else if (!crawler && ey < by + height * 0.04f) {
				continue;
			}
			enqueue(mainX[i], mainY[i], mainZ[i], ex, ey, ez, 1, f, length, ground);
		}

		// Build the queued branches (sub-branches join the queue as they sprout).
		for (int k = 0; k < queued; k++) {
			int order = qOrder[k];
			float length = qLength[k];
			boolean ground = qGround[k];
			int branchLevels = ground ? Math.max(4, levels - 1) : Math.clamp(Math.round((float) (Math.log(Math.max(length, 4f) / 5f) / Math.log(2.0))), 2, 5);
			if (count + (1 << branchLevels) > budget) {
				continue;
			}
			int m = displace(qa[k * 3], qa[k * 3 + 1], qa[k * 3 + 2], qb[k * 3], qb[k * 3 + 1], qb[k * 3 + 2], branchLevels,
					style.tortuosity() * 1.1f, crawler ? 0.4f : (ground ? 0.55f : 0.8f));
			float wb = ground ? width * 0.8f : width * (order == 1 ? 0.42f : (order == 2 ? 0.24f : 0.15f));
			float bright = ground ? 0.9f : (order == 1 ? 0.55f : (order == 2 ? 0.32f : 0.2f));
			float taper = ground ? 0.2f : 0.85f;
			for (int j = 0; j < m && count < budget; j++) {
				float t0 = j / (float) m;
				float t1 = (j + 1) / (float) m;
				float w0 = wb * (1f - t0 * taper);
				float w1 = wb * (1f - t1 * taper);
				count = put(out, count, px[j], py[j], pz[j], px[j + 1], py[j + 1], pz[j + 1], w0, w1 / w0, ground ? 0 : Math.min(order, 2),
						qArrival[k] + t0 * length / total, bright * (1f - (ground ? 0.1f : 0.7f) * t0));
				if (!ground && order < 3 && j > 0 && j < m - 1 && uniform() < style.subBranching() / order) {
					float angle = uniform() * (float) (Math.PI * 2.0);
					float c = (float) Math.cos(angle);
					float s = (float) Math.sin(angle);
					float dx;
					float dy;
					float dz;
					if (crawler) {
						dx = c;
						dy = -0.1f - uniform() * 0.15f;
						dz = s;
					} else {
						dx = c * 0.8f;
						dy = -0.6f - uniform() * 0.5f;
						dz = s * 0.8f;
					}
					float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
					float sl = length * (0.18f + uniform() * 0.3f) * (1f - t0);
					enqueue(px[j], py[j], pz[j], px[j] + dx / dl * sl, py[j] + dy / dl * sl, pz[j] + dz / dl * sl, order + 1,
							qArrival[k] + t0 * length / total, sl, false);
				}
			}
		}
		return count;
	}

	/** Appends the ground-glow sprite at (x, y, z) with the given radius; returns the new count. */
	int sprite(float[] out, int count, float x, float y, float z, float radius) {
		return put(out, count, x, y, z, x, y, z, radius, 1f, ORDER_SPRITE, 1f, 1f);
	}

	private void enqueue(float ax, float ay, float az, float bx, float by, float bz, int order, float arrival, float length, boolean ground) {
		if (queued >= QUEUE) {
			return;
		}
		int k = queued++;
		qa[k * 3] = ax;
		qa[k * 3 + 1] = ay;
		qa[k * 3 + 2] = az;
		qb[k * 3] = bx;
		qb[k * 3 + 1] = by;
		qb[k * 3 + 2] = bz;
		qOrder[k] = order;
		qArrival[k] = arrival;
		qLength[k] = length;
		qGround[k] = ground;
	}

	/**
	 * Midpoint displacement of the segment a → b into {@code 2^levels} segments in {@link #px} /
	 * {@link #py} / {@link #pz}; returns the segment count. Each level expands the polyline in place,
	 * from the back, so nothing is copied.
	 */
	private int displace(float ax, float ay, float az, float bx, float by, float bz, int levels, float tortuosity, float verticalDamping) {
		px[0] = ax;
		py[0] = ay;
		pz[0] = az;
		px[1] = bx;
		py[1] = by;
		pz[1] = bz;
		int n = 1;
		for (int level = 0; level < levels; level++) {
			float scale = SCHEDULE[Math.min(level, SCHEDULE.length - 1)] * tortuosity / 0.17f;
			for (int i = n; i >= 1; i--) {
				float x0 = px[i - 1];
				float y0 = py[i - 1];
				float z0 = pz[i - 1];
				float x1 = px[i];
				float y1 = py[i];
				float z1 = pz[i];
				float dx = x1 - x0;
				float dy = y1 - y0;
				float dz = z1 - z0;
				float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
				float mx;
				float my;
				float mz;
				if (length > 1.0e-5f) {
					basis(dx / length, dy / length, dz / length);
					float g1 = gauss();
					float g2 = gauss();
					float t = 0.5f + signed() * 0.12f;
					mx = x0 + dx * t + (ux * g1 + vx * g2) * scale * length;
					my = y0 + dy * t + (uy * g1 + vy * g2) * scale * length * verticalDamping;
					mz = z0 + dz * t + (uz * g1 + vz * g2) * scale * length;
				} else {
					mx = x0;
					my = y0;
					mz = z0;
				}
				px[2 * i] = x1;
				py[2 * i] = y1;
				pz[2 * i] = z1;
				px[2 * i - 1] = mx;
				py[2 * i - 1] = my;
				pz[2 * i - 1] = mz;
			}
			n *= 2;
		}
		return n;
	}

	/** Two unit vectors perpendicular to the unit vector d, into (ux, uy, uz) and (vx, vy, vz). */
	private void basis(float dx, float dy, float dz) {
		float ax = Math.abs(dy) < 0.9f ? 0f : 1f;
		float ay = Math.abs(dy) < 0.9f ? 1f : 0f;
		// u = normalize(d × a)
		float cx = dy * 0f - dz * ay;
		float cy = dz * ax - dx * 0f;
		float cz = dx * ay - dy * ax;
		float cl = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
		ux = cx / cl;
		uy = cy / cl;
		uz = cz / cl;
		// v = d × u
		vx = dy * uz - dz * uy;
		vy = dz * ux - dx * uz;
		vz = dx * uy - dy * ux;
	}

	private static int put(float[] out, int count, float ax, float ay, float az, float bx, float by, float bz, float width,
			float ratio, int order, float arrival, float brightness) {
		int o = count * 8;
		if (o + 8 > out.length) {
			return count;
		}
		out[o] = ax;
		out[o + 1] = ay;
		out[o + 2] = az;
		out[o + 3] = width;
		out[o + 4] = bx;
		out[o + 5] = by;
		out[o + 6] = bz;
		int a = Math.clamp(Math.round(arrival * 1023f), 0, 1023);
		int b = Math.clamp(Math.round(brightness * 255f), 0, 255);
		int r = Math.clamp(Math.round(ratio * 15f), 0, 15);
		out[o + 7] = (float) (order << 22 | a << 12 | b << 4 | r);
		return count + 1;
	}

	private static float dist(float ax, float ay, float az, float bx, float by, float bz) {
		float dx = bx - ax;
		float dy = by - ay;
		float dz = bz - az;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private long next() {
		long x = state;
		x ^= x << 13;
		x ^= x >>> 7;
		x ^= x << 17;
		state = x;
		return x;
	}

	/** [0, 1). */
	float uniform() {
		return (next() >>> 40) * (1f / (1 << 24));
	}

	private float signed() {
		return uniform() * 2f - 1f;
	}

	/** A cheap bounded "gaussian": four uniforms, unit variance. */
	private float gauss() {
		return (uniform() + uniform() + uniform() + uniform() - 2f) * 1.7320508f;
	}
}
