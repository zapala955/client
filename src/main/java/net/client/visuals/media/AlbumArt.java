package net.client.visuals.media;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GpuImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Album art for the current track.
 *
 * <p>The Windows media session only sometimes carries a thumbnail (and PowerShell can't read its
 * stream reliably), so artwork is looked up by title + artist from the public, key-less iTunes
 * search API, falling back to Deezer's. Only that search text is sent; lookups can be turned off
 * in the Music element's settings.</p>
 *
 * <p>Downloads and decoding run on a worker; the render thread uploads three things per track: the
 * 256² square art, a tiny pre-blurred, darkened copy (sampled LINEAR it's a smooth "ambient"
 * backdrop, like Apple Music's) and a vibrant accent colour picked from the art. Encoded images of
 * recent tracks are cached, so skipping back is instant.</p>
 */
public final class AlbumArt {
	private static final int SIZE = 256;
	private static final int AMBIENT = 24;
	private static final Identifier ART_ID = LiquidClient.id("dynamic/album_art");
	private static final Identifier AMBIENT_ID = LiquidClient.id("dynamic/album_ambient");
	private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Liquid album art");
		thread.setDaemon(true);
		return thread;
	});
	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(6))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	/** Encoded images by track key (null value = known to have no art). */
	private static final Map<String, byte @Nullable []> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, byte @Nullable []> eldest) {
			return size() > 24;
		}
	};

	private static String requestedKey = "";
	private static @Nullable CompletableFuture<@Nullable Decoded> pending;
	private static String pendingKey = "";
	private static @Nullable TextureSetup art;
	private static @Nullable TextureSetup ambient;
	private static int accent;
	private static long shownAt;

	private AlbumArt() {
	}

	private record Decoded(NativeImage art, NativeImage ambient, int accent) implements AutoCloseable {
		@Override
		public void close() {
			art.close();
			ambient.close();
		}
	}

	// ---- Render thread ------------------------------------------------------------------------

	/** Call every frame with the current track; uploads finished lookups. */
	public static void update(NowPlaying track, boolean online) {
		String key = track.active && online ? track.key : "";
		if (!key.equals(requestedKey)) {
			requestedKey = key;
			clear();
			CompletableFuture<@Nullable Decoded> stale = pending;
			if (stale != null) {
				// Track changed mid-lookup: free that result whenever it lands.
				stale.thenAccept(decoded -> {
					if (decoded != null) {
						decoded.close();
					}
				});
				pending = null;
			}
			if (!key.isEmpty()) {
				String title = track.title;
				String artist = track.primaryArtist();
				pendingKey = key;
				pending = CompletableFuture.supplyAsync(() -> load(key, title, artist), WORKER);
			}
		}
		CompletableFuture<@Nullable Decoded> job = pending;
		if (job != null && job.isDone()) {
			pending = null;
			Decoded decoded = job.exceptionally(e -> null).join();
			if (decoded != null) {
				try (decoded) {
					if (pendingKey.equals(requestedKey)) {
						upload(decoded);
					}
				}
			}
		}
	}

	/** A lookup for the current track is still running. */
	public static boolean loading() {
		return pending != null;
	}

	/** Square album art, or null while loading / when there is none. */
	public static @Nullable TextureSetup art() {
		return art;
	}

	/** Blurred, darkened copy for backdrops. */
	public static @Nullable TextureSetup ambient() {
		return ambient;
	}

	/** Vibrant colour from the art (opaque), or {@code fallback} when there's no art. */
	public static int accent(int fallback) {
		return art != null ? accent : fallback;
	}

	/** 0 → 1 over the first 300 ms after new art appeared (cross-fade in). */
	public static float reveal() {
		if (art == null) {
			return 0f;
		}
		return Math.min(1f, (System.currentTimeMillis() - shownAt) / 300f);
	}

	private static void upload(Decoded decoded) {
		var textures = Minecraft.getInstance().getTextureManager();
		GpuImage artImage = new GpuImage("Liquid album art", decoded.art());
		GpuImage ambientImage = new GpuImage("Liquid album ambient", decoded.ambient());
		textures.register(ART_ID, artImage);
		textures.register(AMBIENT_ID, ambientImage);
		art = TextureSetup.singleTexture(artImage.view(), artImage.linearSampler());
		ambient = TextureSetup.singleTexture(ambientImage.view(), ambientImage.linearSampler());
		accent = decoded.accent();
		shownAt = System.currentTimeMillis();
	}

	/** Frees the textures (track changed, no art, or nothing shows media any more). */
	public static void clear() {
		if (art != null) {
			var textures = Minecraft.getInstance().getTextureManager();
			textures.release(ART_ID);
			textures.release(AMBIENT_ID);
			art = null;
			ambient = null;
		}
	}

	/** Drops the textures and forgets the request, e.g. when no screen shows media. */
	public static void release() {
		requestedKey = "";
		clear();
	}

	// ---- Worker -------------------------------------------------------------------------------

	private static @Nullable Decoded load(String key, String title, String artist) {
		byte[] encoded;
		boolean cached;
		synchronized (CACHE) {
			cached = CACHE.containsKey(key);
			encoded = CACHE.get(key);
		}
		if (!cached) {
			encoded = lookup(title, artist);
			synchronized (CACHE) {
				CACHE.put(key, encoded);
			}
		}
		if (encoded == null) {
			return null;
		}
		try {
			return decode(encoded);
		} catch (IOException | RuntimeException e) {
			LiquidClient.LOGGER.warn("Album art for '{}' couldn't be decoded: {}", title, e.toString());
			return null;
		}
	}

	private static byte @Nullable [] lookup(String title, String artist) {
		String term = searchTitle(title) + " " + artist;
		try {
			String url = iTunes(term, artist);
			if (url == null) {
				url = deezer(term, artist);
			}
			return url == null ? null : download(url);
		} catch (IOException e) {
			LiquidClient.LOGGER.info("Album art lookup failed: {}", e.toString());
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	/** "Song - Remastered 2011 (feat. X) [Live]" → "Song": better search hits. */
	private static String searchTitle(String title) {
		String t = title.replaceAll("\\s*[(\\[][^)\\]]*[)\\]]", "");
		int dash = t.indexOf(" - ");
		if (dash > 0) {
			t = t.substring(0, dash);
		}
		return t.isBlank() ? title : t.trim();
	}

	private static @Nullable String iTunes(String term, String artist) throws IOException, InterruptedException {
		JsonObject json = getJson("https://itunes.apple.com/search?media=music&entity=song&limit=6&term=" + encode(term));
		JsonArray results = json != null && json.has("results") ? json.getAsJsonArray("results") : null;
		String url = pick(results, "artistName", artist, "artworkUrl100");
		return url == null ? null : url.replace("100x100bb", "600x600bb");
	}

	private static @Nullable String deezer(String term, String artist) throws IOException, InterruptedException {
		JsonObject json = getJson("https://api.deezer.com/search?limit=6&q=" + encode(term));
		JsonArray data = json != null && json.has("data") ? json.getAsJsonArray("data") : null;
		if (data == null) {
			return null;
		}
		String fallback = null;
		for (JsonElement element : data) {
			JsonObject item = element.getAsJsonObject();
			JsonObject album = item.has("album") ? item.getAsJsonObject("album") : null;
			if (album == null || !album.has("cover_big")) {
				continue;
			}
			String cover = album.get("cover_big").getAsString();
			JsonObject artistObject = item.has("artist") ? item.getAsJsonObject("artist") : null;
			String name = artistObject != null && artistObject.has("name") ? artistObject.get("name").getAsString() : "";
			if (matches(name, artist)) {
				return cover;
			}
			if (fallback == null) {
				fallback = cover;
			}
		}
		return fallback;
	}

	/** Prefers a result by the same artist; otherwise the top hit. */
	private static @Nullable String pick(@Nullable JsonArray results, String artistField, String artist, String urlField) {
		if (results == null) {
			return null;
		}
		String fallback = null;
		for (JsonElement element : results) {
			JsonObject item = element.getAsJsonObject();
			if (!item.has(urlField)) {
				continue;
			}
			String url = item.get(urlField).getAsString();
			String name = item.has(artistField) ? item.get(artistField).getAsString() : "";
			if (matches(name, artist)) {
				return url;
			}
			if (fallback == null) {
				fallback = url;
			}
		}
		return fallback;
	}

	private static boolean matches(String candidate, String artist) {
		String a = candidate.toLowerCase(Locale.ROOT);
		String b = artist.toLowerCase(Locale.ROOT);
		return !b.isEmpty() && (a.contains(b) || b.contains(a));
	}

	private static @Nullable JsonObject getJson(String url) throws IOException, InterruptedException {
		HttpResponse<String> response = HTTP.send(request(url), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() != 200) {
			return null;
		}
		JsonElement json = JsonParser.parseString(response.body());
		return json.isJsonObject() ? json.getAsJsonObject() : null;
	}

	private static byte @Nullable [] download(String url) throws IOException, InterruptedException {
		HttpResponse<byte[]> response = HTTP.send(request(url), HttpResponse.BodyHandlers.ofByteArray());
		byte[] body = response.body();
		return response.statusCode() == 200 && body.length > 0 && body.length < 8_000_000 ? body : null;
	}

	private static HttpRequest request(String url) {
		return HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(8))
				.header("User-Agent", "LiquidClient/0.1 (Minecraft mod; album art)")
				.GET()
				.build();
	}

	private static String encode(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	// ---- Decoding -----------------------------------------------------------------------------

	private static Decoded decode(byte[] encoded) throws IOException {
		ByteBuffer file = MemoryUtil.memAlloc(encoded.length);
		ByteBuffer pixels;
		int w;
		int h;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			file.put(encoded).flip();
			IntBuffer pw = stack.mallocInt(1);
			IntBuffer ph = stack.mallocInt(1);
			IntBuffer pc = stack.mallocInt(1);
			pixels = STBImage.stbi_load_from_memory(file, pw, ph, pc, 4);
			if (pixels == null) {
				throw new IOException(STBImage.stbi_failure_reason());
			}
			w = pw.get(0);
			h = ph.get(0);
		} finally {
			MemoryUtil.memFree(file);
		}
		NativeImage art = null;
		try {
			// Centre square crop, resampled in sRGB space.
			int side = Math.min(w, h);
			long start = MemoryUtil.memAddress(pixels) + ((long) ((h - side) / 2) * w + (w - side) / 2) * 4L;
			ByteBuffer square = MemoryUtil.memByteBuffer(start, (side - 1) * w * 4 + side * 4);
			art = new NativeImage(SIZE, SIZE, false);
			ByteBuffer artPixels = MemoryUtil.memByteBuffer(art.getPointer(), SIZE * SIZE * 4);
			if (STBImageResize.stbir_resize_uint8_srgb(square, side, side, w * 4, artPixels, SIZE, SIZE, SIZE * 4,
					STBImageResize.STBIR_RGBA) == null) {
				throw new IOException("resize failed");
			}
			for (int i = 3; i < SIZE * SIZE * 4; i += 4) {
				artPixels.put(i, (byte) 0xFF); // album art is opaque
			}

			ByteBuffer small = MemoryUtil.memAlloc(AMBIENT * AMBIENT * 4);
			try {
				STBImageResize.stbir_resize_uint8_srgb(artPixels, SIZE, SIZE, SIZE * 4, small, AMBIENT, AMBIENT, AMBIENT * 4,
						STBImageResize.STBIR_RGBA);
				int accentColor = pickAccent(small);
				NativeImage ambientImage = ambient(small);
				NativeImage result = art;
				art = null;
				return new Decoded(result, ambientImage, accentColor);
			} finally {
				MemoryUtil.memFree(small);
			}
		} finally {
			if (art != null) {
				art.close();
			}
			STBImage.stbi_image_free(pixels);
		}
	}

	/** Saturation- and brightness-weighted average, then pushed to a vivid, readable tone. */
	private static int pickAccent(ByteBuffer rgba) {
		double r = 0;
		double g = 0;
		double b = 0;
		double total = 0;
		for (int i = 0; i < AMBIENT * AMBIENT; i++) {
			int pr = rgba.get(i * 4) & 0xFF;
			int pg = rgba.get(i * 4 + 1) & 0xFF;
			int pb = rgba.get(i * 4 + 2) & 0xFF;
			int max = Math.max(pr, Math.max(pg, pb));
			int min = Math.min(pr, Math.min(pg, pb));
			double saturation = max == 0 ? 0 : (max - min) / (double) max;
			double weight = saturation * saturation * (max / 255.0) + 0.002;
			r += pr * weight;
			g += pg * weight;
			b += pb * weight;
			total += weight;
		}
		float[] hsv = toHsv((float) (r / total), (float) (g / total), (float) (b / total));
		if (hsv[1] < 0.12f) {
			return 0xFFE8EBF0; // greyscale art: soft white accent
		}
		return ColorUtil.hsv(hsv[0], Math.max(hsv[1], 0.55f), Math.max(hsv[2], 0.92f));
	}

	private static float[] toHsv(float r, float g, float b) {
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float hue;
		if (delta < 1e-4f) {
			hue = 0f;
		} else if (max == r) {
			hue = 60f * (((g - b) / delta) % 6f);
		} else if (max == g) {
			hue = 60f * ((b - r) / delta + 2f);
		} else {
			hue = 60f * ((r - g) / delta + 4f);
		}
		return new float[]{hue < 0f ? hue + 360f : hue, max <= 0f ? 0f : delta / max, max / 255f};
	}

	/** Box-blurred (3 passes ≈ gaussian) and darkened so white text reads on top of it. */
	private static NativeImage ambient(ByteBuffer rgba) {
		int n = AMBIENT;
		float[] channels = new float[n * n * 3];
		for (int i = 0; i < n * n; i++) {
			channels[i * 3] = rgba.get(i * 4) & 0xFF;
			channels[i * 3 + 1] = rgba.get(i * 4 + 1) & 0xFF;
			channels[i * 3 + 2] = rgba.get(i * 4 + 2) & 0xFF;
		}
		float[] tmp = new float[channels.length];
		for (int pass = 0; pass < 3; pass++) {
			blur(channels, tmp, n, true);
			blur(tmp, channels, n, false);
		}
		NativeImage image = new NativeImage(n, n, false);
		for (int y = 0; y < n; y++) {
			for (int x = 0; x < n; x++) {
				int i = (y * n + x) * 3;
				int r = Math.clamp(Math.round(channels[i] * 0.62f), 0, 255);
				int g = Math.clamp(Math.round(channels[i + 1] * 0.62f), 0, 255);
				int b = Math.clamp(Math.round(channels[i + 2] * 0.62f), 0, 255);
				image.setPixel(x, y, 0xFF000000 | r << 16 | g << 8 | b);
			}
		}
		return image;
	}

	private static void blur(float[] src, float[] dst, int n, boolean horizontal) {
		final int radius = 2;
		for (int a = 0; a < n; a++) {
			for (int b = 0; b < n; b++) {
				float r = 0;
				float g = 0;
				float bl = 0;
				for (int k = -radius; k <= radius; k++) {
					int c = Math.clamp(b + k, 0, n - 1);
					int i = (horizontal ? a * n + c : c * n + a) * 3;
					r += src[i];
					g += src[i + 1];
					bl += src[i + 2];
				}
				int o = (horizontal ? a * n + b : b * n + a) * 3;
				float inv = 1f / (radius * 2 + 1);
				dst[o] = r * inv;
				dst[o + 1] = g * inv;
				dst[o + 2] = bl * inv;
			}
		}
	}
}
