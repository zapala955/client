package net.client.visuals.render.sky;

import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.IntStream;

/**
 * The equirectangular texture behind the "4K Night Galaxy Texture" mode.
 *
 * <p>Source, in order: {@code config/liquidclient/sky/galaxy.png} (any 2:1 equirectangular PNG,
 * e.g. 4096×2048 or 8192×4096), otherwise a 4096×2048 galaxy is generated procedurally. Decoding /
 * generation runs on a worker thread; only the GPU upload happens on the render thread.</p>
 *
 * <p>Memory: the CPU copy is freed right after upload, and {@link #release()} returns the GPU texture.
 * A generation counter discards results that arrive after a release, so a late worker can never leak
 * an image or resurrect a texture.</p>
 *
 * <p>Threads: never Minecraft's background executor – shader compiles and chunk work wait in that
 * queue, and a minute of galaxy generation in front of them froze world loading. Work runs on our
 * own low-priority thread (generation on a small pool of its own), stops as soon as it's released,
 * and the generated galaxy is cached as a PNG so it's only ever computed once.</p>
 */
final class GalaxyTexture {
	private static final Identifier ID = LiquidClient.id("dynamic/sky_galaxy");
	private static final Path USER_FILE = FabricLoader.getInstance().getConfigDir().resolve("liquidclient/sky/galaxy.png");
	/** The generated galaxy, saved after the first run (bump the version when {@link #shade} changes). */
	private static final Path CACHE_FILE = FabricLoader.getInstance().getConfigDir().resolve("liquidclient/sky/generated-galaxy-v1.png");
	private static final int WIDTH = 4096;
	private static final int HEIGHT = 2048;
	private static final ExecutorService LOADER = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Liquid galaxy loader");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		return thread;
	});
	/** Half the cores at most, low priority: the galaxy is never more urgent than the game. */
	private static final ForkJoinPool GENERATOR = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2), pool -> {
		ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		thread.setName("Liquid galaxy generator " + thread.getPoolIndex());
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		return thread;
	}, null, false);

	private @Nullable DynamicTexture texture;
	private boolean requested;
	/** Read by the worker to stop early once released. */
	private volatile int generation;

	/** Starts loading if needed. Render thread. */
	void request() {
		if (requested) {
			return;
		}
		requested = true;
		int token = ++generation;
		LiquidClient.LOGGER.info("Galaxy texture: preparing");
		CompletableFuture.supplyAsync(() -> produce(token), LOADER)
				.whenComplete((image, error) -> Minecraft.getInstance().execute(() -> accept(token, image, error)));
	}

	private void accept(int token, @Nullable NativeImage image, @Nullable Throwable error) {
		Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
		if (cause instanceof CancellationException) {
			return; // released while it was being made
		}
		if (cause != null || image == null) {
			LiquidClient.LOGGER.error("Galaxy texture failed", cause);
			return;
		}
		if (token != generation) {
			image.close(); // released while we were working
			return;
		}
		DynamicTexture created = new DynamicTexture(() -> "Liquid galaxy sky", image);
		// The GPU now owns the pixels – drop the 32 MB CPU copy.
		created.setPixels(new NativeImage(1, 1, false));
		Minecraft.getInstance().getTextureManager().register(ID, created);
		texture = created;
		LiquidClient.LOGGER.info("Galaxy texture: uploaded {}x{}", created.getTexture().getWidth(0), created.getTexture().getHeight(0));
	}

	boolean ready() {
		return texture != null;
	}

	@Nullable DynamicTexture texture() {
		return texture;
	}

	/** Render thread. Safe to call repeatedly. */
	void release() {
		generation++;
		requested = false;
		if (texture != null) {
			Minecraft.getInstance().getTextureManager().release(ID);
			texture = null;
		}
	}

	// ---- Worker thread ------------------------------------------------------------------------

	private NativeImage produce(int token) {
		if (Files.isRegularFile(USER_FILE)) {
			try (InputStream in = Files.newInputStream(USER_FILE)) {
				return NativeImage.read(in);
			} catch (IOException | RuntimeException e) {
				LiquidClient.LOGGER.warn("Couldn't read {}, generating a galaxy instead", USER_FILE, e);
			}
		}
		if (Files.isRegularFile(CACHE_FILE)) {
			try (InputStream in = Files.newInputStream(CACHE_FILE)) {
				return NativeImage.read(in);
			} catch (IOException | RuntimeException e) {
				LiquidClient.LOGGER.warn("Couldn't read the cached galaxy {}, generating it again", CACHE_FILE, e);
			}
		}
		NativeImage image = GENERATOR.submit(() -> generate(token)).join();
		try {
			Files.createDirectories(CACHE_FILE.getParent());
			image.writeToFile(CACHE_FILE);
			LiquidClient.LOGGER.info("Galaxy texture: generated and cached as {}", CACHE_FILE.getFileName());
		} catch (IOException | RuntimeException e) {
			LiquidClient.LOGGER.warn("Couldn't cache the generated galaxy", e);
		}
		return image;
	}

	/** Procedural galaxy: deep-space gradient, a nebulous Milky Way band, dust lanes and stars. */
	private NativeImage generate(int token) {
		NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
		try {
			IntStream.range(0, HEIGHT).parallel().forEach(y -> {
				if (token != generation) {
					throw new CancellationException();
				}
				generateRow(image, y);
			});
		} catch (RuntimeException e) {
			image.close();
			throw e;
		}
		return image;
	}

	private static void generateRow(NativeImage image, int y) {
		double theta = (y + 0.5) / HEIGHT * Math.PI;
		double sinT = Math.sin(theta);
		double cosT = Math.cos(theta);
		for (int x = 0; x < WIDTH; x++) {
			double phi = ((x + 0.5) / WIDTH - 0.5) * 2.0 * Math.PI;
			double dx = sinT * Math.cos(phi);
			double dy = cosT;
			double dz = sinT * Math.sin(phi);
			image.setPixel(x, y, shade(dx, dy, dz));
		}
	}

	private static int shade(double x, double y, double z) {
		// Band around a tilted great circle.
		double bn = x * 0.35 + y * 0.25 + z * 0.9;
		bn /= 1.0137;
		double band = Math.exp(-Math.pow(bn / 0.28, 2));
		double n = fbm(x * 3.0, y * 3.0, z * 3.0);
		double tone = fbm(x * 6.0 - 3.1, y * 6.0 + 1.3, z * 6.0 + 7.7);
		double dust = smooth(0.45, 0.75, fbm(x * 11.0 + 2.0, y * 11.0, z * 11.0 - 4.0));
		double glow = band * (0.15 + 0.95 * n) * (1.0 - 0.7 * dust);

		double r = 0.010 + glow * lerp(0.32, 0.14, tone) + smooth(0.6, 0.9, n) * band * 0.45;
		double g = 0.012 + glow * lerp(0.16, 0.30, tone) + smooth(0.6, 0.9, n) * band * 0.22;
		double b = 0.035 + glow * lerp(0.55, 0.80, tone) + smooth(0.6, 0.9, n) * band * 0.35;

		// Faint background nebulosity everywhere.
		double haze = fbm(x * 1.5 + 9.0, y * 1.5, z * 1.5) * 0.05;
		r += haze * 0.6;
		g += haze * 0.4;
		b += haze;

		// Stars: hashed cells on a fine grid, denser inside the band.
		double s = 520.0;
		double cx = Math.floor(x * s);
		double cy = Math.floor(y * s);
		double cz = Math.floor(z * s);
		double h = hash(cx, cy, cz);
		if (h < 0.004 + band * 0.012) {
			double bright = 0.4 + 0.6 * hash(cx + 7.0, cy, cz);
			double warm = hash(cx, cy + 3.0, cz);
			r += bright * lerp(0.75, 1.0, warm);
			g += bright * lerp(0.85, 0.9, warm);
			b += bright * lerp(1.0, 0.75, warm);
		}
		return 0xFF000000 | channel(r) << 16 | channel(g) << 8 | channel(b);
	}

	private static int channel(double v) {
		// Simple filmic-ish curve keeps bright stars from clipping harshly.
		double mapped = v / (1.0 + v * 0.35);
		return (int) Math.round(Math.clamp(mapped, 0.0, 1.0) * 255.0);
	}

	private static double hash(double x, double y, double z) {
		double px = frac(x * 0.1031);
		double py = frac(y * 0.1031);
		double pz = frac(z * 0.1031);
		double d = px * (pz + 31.32) + py * (py + 31.32) + pz * (px + 31.32);
		px += d;
		py += d;
		pz += d;
		return frac((px + py) * pz);
	}

	private static double noise(double x, double y, double z) {
		double ix = Math.floor(x);
		double iy = Math.floor(y);
		double iz = Math.floor(z);
		double fx = x - ix;
		double fy = y - iy;
		double fz = z - iz;
		fx = fx * fx * (3 - 2 * fx);
		fy = fy * fy * (3 - 2 * fy);
		fz = fz * fz * (3 - 2 * fz);
		double a = hash(ix, iy, iz);
		double b = hash(ix + 1, iy, iz);
		double c = hash(ix, iy + 1, iz);
		double d = hash(ix + 1, iy + 1, iz);
		double e = hash(ix, iy, iz + 1);
		double f = hash(ix + 1, iy, iz + 1);
		double g = hash(ix, iy + 1, iz + 1);
		double h = hash(ix + 1, iy + 1, iz + 1);
		return lerp(lerp(lerp(a, b, fx), lerp(c, d, fx), fy), lerp(lerp(e, f, fx), lerp(g, h, fx), fy), fz);
	}

	private static double fbm(double x, double y, double z) {
		double v = 0;
		double amp = 0.5;
		for (int i = 0; i < 5; i++) {
			v += amp * noise(x, y, z);
			x = x * 2.03 + 1.7;
			y = y * 2.03 + 9.2;
			z = z * 2.03 + 3.4;
			amp *= 0.5;
		}
		return v;
	}

	private static double frac(double v) {
		return v - Math.floor(v);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	private static double smooth(double e0, double e1, double v) {
		double t = Math.clamp((v - e0) / (e1 - e0), 0.0, 1.0);
		return t * t * (3 - 2 * t);
	}
}
