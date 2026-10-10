package net.client.visuals.media;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * "Now playing" from the PC: Spotify (or any player) via the Windows media session, the same one the
 * volume flyout and lock screen show. No Spotify account, app key or login involved.
 *
 * <p>A small PowerShell script ({@code assets/liquidclient/media/now_playing.ps1}, copied to
 * {@code config/liquidclient/}) reads the session through WinRT and prints a JSON line per change;
 * play / pause / skip go back over its stdin. It exits as soon as its stdin closes, so it can't
 * outlive the game. The bridge only runs while something on screen wants it: callers
 * {@link #request} every frame, and it shuts down after {@link #IDLE_NANOS} without requests.</p>
 */
public final class MediaBridge {
	private static final long IDLE_NANOS = 10_000_000_000L;
	private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");

	private static volatile NowPlaying current = NowPlaying.NONE;
	private static volatile long lastRequest;
	private static volatile boolean anyPlayer;
	private static volatile boolean running;
	private static @Nullable Thread worker;

	private static final Object IO = new Object();
	private static @Nullable Process process;
	private static @Nullable BufferedWriter input;
	private static String sentSource = "";
	private static String lastError = "";

	private MediaBridge() {
	}

	public static boolean supported() {
		return WINDOWS;
	}

	public static NowPlaying current() {
		return current;
	}

	/**
	 * Keeps the bridge alive (call every frame while showing media).
	 *
	 * @param any any player's session instead of only Spotify's
	 */
	public static void request(boolean any) {
		lastRequest = System.nanoTime();
		anyPlayer = any;
	}

	/** Client tick: starts / stops the helper and forwards the source preference. */
	public static void tick() {
		boolean wanted = WINDOWS && lastRequest != 0L && System.nanoTime() - lastRequest < IDLE_NANOS;
		if (wanted && !running) {
			start();
		} else if (!wanted && running) {
			stop();
			AlbumArt.release(); // nothing shows media any more
		}
		if (running) {
			syncSource();
		}
	}

	public static void togglePlayPause() {
		send("toggle");
	}

	public static void next() {
		send("next");
	}

	public static void previous() {
		send("prev");
	}

	/** Jumps to {@code millis} into the track (players that don't support seeking ignore it). */
	public static void seek(long millis) {
		send("seek " + Math.max(0L, millis));
	}

	public static void shutdown() {
		lastRequest = 0L;
		stop();
	}

	// ---- Process ------------------------------------------------------------------------------

	private static void start() {
		running = true;
		if (worker != null && worker.isAlive()) {
			return; // a worker that's still winding down just keeps going
		}
		Thread thread = new Thread(MediaBridge::run, "Liquid media bridge");
		thread.setDaemon(true);
		worker = thread;
		thread.start();
	}

	private static void stop() {
		running = false;
		synchronized (IO) {
			closeProcess();
		}
		current = NowPlaying.NONE;
	}

	private static void run() {
		int failures = 0;
		while (running) {
			long started = System.nanoTime();
			try {
				runOnce();
			} catch (IOException | RuntimeException e) {
				LiquidClient.LOGGER.warn("Media bridge failed: {}", e.toString());
			}
			current = NowPlaying.NONE;
			if (!running) {
				break;
			}
			// A helper that ran for a while and then died gets restarted quickly; one that can't start backs off.
			failures = System.nanoTime() - started > 60_000_000_000L ? 1 : failures + 1;
			try {
				Thread.sleep(Math.min(60_000L, 2_000L * failures));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private static void runOnce() throws IOException {
		Path script = extractScript();
		ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
				"-ExecutionPolicy", "Bypass", "-File", script.toString());
		builder.redirectError(ProcessBuilder.Redirect.DISCARD);
		Process started = builder.start();
		synchronized (IO) {
			if (!running) {
				started.destroy();
				return;
			}
			process = started;
			input = new BufferedWriter(new OutputStreamWriter(started.getOutputStream(), StandardCharsets.UTF_8));
			sentSource = "";
		}
		LiquidClient.LOGGER.info("Media bridge started");
		syncSource();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(started.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				handle(line);
			}
		} finally {
			synchronized (IO) {
				if (process == started) {
					closeProcess();
				}
			}
		}
	}

	private static void closeProcess() {
		if (input != null) {
			try {
				input.close(); // the script exits when its stdin closes
			} catch (IOException ignored) {
				// already gone
			}
			input = null;
		}
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	private static void syncSource() {
		synchronized (IO) {
			String wanted = anyPlayer ? "any" : "spotify";
			if (input != null && !wanted.equals(sentSource) && write("source " + wanted)) {
				sentSource = wanted;
			}
		}
	}

	private static void send(String command) {
		synchronized (IO) {
			write(command);
		}
	}

	private static boolean write(String line) {
		if (input == null) {
			return false;
		}
		try {
			input.write(line);
			input.write('\n');
			input.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	// ---- Protocol -----------------------------------------------------------------------------

	private static void handle(String line) {
		String text = line.startsWith("﻿") ? line.substring(1) : line;
		if (text.isBlank()) {
			return;
		}
		JsonObject json;
		try {
			json = JsonParser.parseString(text).getAsJsonObject();
		} catch (RuntimeException e) {
			return;
		}
		String type = string(json, "type");
		if ("state".equals(type)) {
			if (!bool(json, "active")) {
				current = NowPlaying.NONE;
				return;
			}
			current = new NowPlaying(true, string(json, "app"), string(json, "title"), string(json, "artist"),
					string(json, "album"), "Playing".equals(string(json, "status")), number(json, "position"),
					number(json, "duration"), number(json, "updated"));
		} else if ("error".equals(type)) {
			String message = string(json, "message");
			if (!message.equals(lastError)) {
				lastError = message;
				LiquidClient.LOGGER.warn("Media bridge: {}", message);
			}
		}
	}

	private static String string(JsonObject json, String key) {
		JsonElement e = json.get(key);
		return e != null && e.isJsonPrimitive() ? e.getAsString() : "";
	}

	private static boolean bool(JsonObject json, String key) {
		JsonElement e = json.get(key);
		return e != null && e.isJsonPrimitive() && e.getAsBoolean();
	}

	private static long number(JsonObject json, String key) {
		JsonElement e = json.get(key);
		try {
			return e != null && e.isJsonPrimitive() ? e.getAsLong() : 0L;
		} catch (NumberFormatException ex) {
			return 0L;
		}
	}

	/** Copies the script into the config folder (only rewriting it when the bundled one changed). */
	private static Path extractScript() throws IOException {
		byte[] bundled;
		try (InputStream in = MediaBridge.class.getResourceAsStream("/assets/liquidclient/media/now_playing.ps1")) {
			if (in == null) {
				throw new IOException("now_playing.ps1 missing from the mod jar");
			}
			bundled = in.readAllBytes();
		}
		Path target = FabricLoader.getInstance().getConfigDir().resolve("liquidclient").resolve("now_playing.ps1");
		Files.createDirectories(target.getParent());
		if (!Files.isRegularFile(target) || !Arrays.equals(Files.readAllBytes(target), bundled)) {
			Files.write(target, bundled);
		}
		return target;
	}
}
