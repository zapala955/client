package net.client.visuals.media;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.client.visuals.LiquidClient;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time-synced lyrics for the current track.
 *
 * <p>Looked up by title, artist, album and length in LRCLIB (lrclib.net), a free, key-less public
 * lyrics database with line-synced LRC. Only that track information is sent; lookups can be turned
 * off in the Lyrics element. Lyrics with per-word timing ({@code <mm:ss.xx>} tags) use it; otherwise
 * each word's time is estimated within its line from its length.</p>
 *
 * <p>Requests run on a worker; the render thread polls the result ({@link #update}). Results of recent
 * tracks are cached, so skipping back is instant.</p>
 */
public final class Lyrics {
	public enum Status {
		/** Nothing playing, or lookups off. */
		IDLE,
		LOADING,
		READY,
		NOT_FOUND,
		INSTRUMENTAL,
		/** Lyrics exist but have no timing. */
		UNSYNCED,
		/** Network or server error. */
		ERROR
	}

	/** Parsed, timed lyrics. Immutable; times in milliseconds into the track. */
	public static final class Track {
		public final String[] lines;
		public final long[] starts;
		/** When each line's singing is over (next line's start, capped). */
		public final long[] ends;
		public final String[][] words;
		public final long[][] wordStarts;
		/** Whether the source carried per-word timing. */
		public final boolean wordSynced;

		Track(String[] lines, long[] starts, long[] ends, String[][] words, long[][] wordStarts, boolean wordSynced) {
			this.lines = lines;
			this.starts = starts;
			this.ends = ends;
			this.words = words;
			this.wordStarts = wordStarts;
			this.wordSynced = wordSynced;
		}

		public int size() {
			return lines.length;
		}

		/** Index of the line being sung at {@code ms} (the last one that started), -1 before the first. */
		public int lineAt(long ms) {
			int i = Arrays.binarySearch(starts, ms);
			return i >= 0 ? lastWithStart(i) : -i - 2;
		}

		private int lastWithStart(int i) {
			while (i + 1 < starts.length && starts[i + 1] == starts[i]) {
				i++;
			}
			return i;
		}

		/** Whether line {@code i} is an instrumental gap (no words). */
		public boolean blank(int i) {
			return words[i].length == 0;
		}
	}

	private record Result(Status status, @Nullable Track track) {
	}

	private static final Result NONE = new Result(Status.IDLE, null);
	private static final Pattern TIME_TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2}(?:[.:]\\d{1,3})?)]");
	private static final Pattern WORD_TAG = Pattern.compile("<(\\d{1,3}):(\\d{1,2}(?:[.:]\\d{1,3})?)>");
	private static final Pattern OFFSET_TAG = Pattern.compile("^\\[offset:\\s*([+-]?\\d+)\\s*]", Pattern.CASE_INSENSITIVE);
	/** "(Remastered 2009)", "[Live]", "(feat. X)" … */
	private static final Pattern TITLE_NOISE = Pattern.compile(
			"\\s*[(\\[][^)\\]]*\\b(remaster(ed)?|live|version|edit|mix|feat\\.?|ft\\.|with|mono|stereo|deluxe|bonus|explicit|clean)\\b[^)\\]]*[)\\]]",
			Pattern.CASE_INSENSITIVE);
	/** "Song - Remastered 2009", "Song - Live at …" */
	private static final Pattern TITLE_SUFFIX = Pattern.compile(
			"\\s+-\\s+.*\\b(remaster(ed)?|live|version|edit|mix|mono|stereo|recorded|acoustic)\\b.*$", Pattern.CASE_INSENSITIVE);
	/** Section markers some lyrics carry as lines: "{chorus}", "[Verse 2]", "(Instrumental)". */
	private static final Pattern SECTION = Pattern.compile(
			"^(\\{[^}]*}|\\[[^\\]]*]|\\((instrumental|music|solo|intro|outro|interlude|bridge|chorus|verse[^)]*|hook|break)\\)|[♪♫\\s]+)$",
			Pattern.CASE_INSENSITIVE);
	/** Longest a line counts as "being sung" before it's treated as a pause. */
	private static final long MAX_LINE_MS = 7000L;
	private static final long LAST_LINE_MS = 5000L;

	private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Liquid lyrics");
		thread.setDaemon(true);
		return thread;
	});
	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(6))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	private static final Map<String, Result> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Result> eldest) {
			return size() > 32;
		}
	};

	private static String requestedKey = "";
	private static @Nullable CompletableFuture<Result> pending;
	private static Result current = NONE;

	private Lyrics() {
	}

	// ---- Render thread ------------------------------------------------------------------------

	/** Call every frame with the current track; starts lookups and picks up finished ones. */
	public static void update(NowPlaying track, boolean online) {
		String key = track.active && online && !track.title.isEmpty() ? track.key : "";
		if (!key.equals(requestedKey)) {
			requestedKey = key;
			pending = null;
			if (key.isEmpty()) {
				current = NONE;
			} else {
				Result cached;
				synchronized (CACHE) {
					cached = CACHE.get(key);
				}
				if (cached != null) {
					current = cached;
				} else {
					current = new Result(Status.LOADING, null);
					String title = track.title;
					String artist = track.artist;
					String primary = track.primaryArtist();
					String album = track.album;
					long duration = track.duration;
					pending = CompletableFuture.supplyAsync(() -> lookup(key, title, artist, primary, album, duration), WORKER);
				}
			}
		}
		CompletableFuture<Result> job = pending;
		if (job != null && job.isDone()) {
			pending = null;
			current = job.exceptionally(e -> new Result(Status.ERROR, null)).join();
		}
	}

	public static Status status() {
		return current.status;
	}

	/** The current track's lyrics, or null while there are none. */
	public static @Nullable Track track() {
		return current.track;
	}

	// ---- Worker -------------------------------------------------------------------------------

	private static Result lookup(String key, String title, String artist, String primaryArtist, String album, long durationMs) {
		Result result;
		try {
			result = fetch(title, artist, primaryArtist, album, durationMs);
		} catch (IOException e) {
			LiquidClient.LOGGER.warn("Lyrics lookup for '{}' failed: {}", title, e.toString());
			return new Result(Status.ERROR, null); // not cached: try again next time the song plays
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new Result(Status.ERROR, null);
		}
		synchronized (CACHE) {
			CACHE.put(key, result);
		}
		return result;
	}

	private static Result fetch(String title, String artist, String primaryArtist, String album, long durationMs)
			throws IOException, InterruptedException {
		long seconds = Math.round(durationMs / 1000.0);
		// 1) Exact match on everything the player reports.
		StringBuilder url = new StringBuilder("https://lrclib.net/api/get?track_name=").append(encode(title))
				.append("&artist_name=").append(encode(artist));
		if (!album.isEmpty()) {
			url.append("&album_name=").append(encode(album));
		}
		if (seconds > 0) {
			url.append("&duration=").append(seconds);
		}
		JsonElement exact = get(url.toString());
		if (exact != null && exact.isJsonObject()) {
			Result result = toResult(exact.getAsJsonObject());
			if (result.status != Status.NOT_FOUND) {
				return result;
			}
		}
		// 2) Search with a cleaned-up title and the main artist; prefer synced lyrics of the same length.
		String cleanTitle = cleanTitle(title);
		JsonElement found = get("https://lrclib.net/api/search?track_name=" + encode(cleanTitle) + "&artist_name=" + encode(primaryArtist));
		JsonObject best = null;
		long bestScore = Long.MAX_VALUE;
		boolean anyPlain = false;
		if (found != null && found.isJsonArray()) {
			for (JsonElement element : found.getAsJsonArray()) {
				if (!element.isJsonObject()) {
					continue;
				}
				JsonObject candidate = element.getAsJsonObject();
				if (string(candidate, "syncedLyrics").isEmpty()) {
					anyPlain |= !string(candidate, "plainLyrics").isEmpty() || bool(candidate, "instrumental");
					continue;
				}
				long length = Math.round(number(candidate, "duration"));
				long score = seconds > 0 && length > 0 ? Math.abs(length - seconds) : 1000;
				if (score < bestScore) {
					bestScore = score;
					best = candidate;
				}
			}
		}
		if (best != null && bestScore <= 12) {
			return toResult(best);
		}
		return new Result(anyPlain ? Status.UNSYNCED : Status.NOT_FOUND, null);
	}

	private static Result toResult(JsonObject json) {
		if (bool(json, "instrumental")) {
			return new Result(Status.INSTRUMENTAL, null);
		}
		String synced = string(json, "syncedLyrics");
		if (synced.isEmpty()) {
			return new Result(string(json, "plainLyrics").isEmpty() ? Status.NOT_FOUND : Status.UNSYNCED, null);
		}
		Track track = parse(synced, Math.round(number(json, "duration") * 1000.0));
		return track != null ? new Result(Status.READY, track) : new Result(Status.NOT_FOUND, null);
	}

	private static @Nullable JsonElement get(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(10))
				.header("User-Agent", "LiquidClient/0.1 (Minecraft mod; lyrics)")
				.header("Accept", "application/json")
				.GET()
				.build();
		HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() == 404) {
			return null;
		}
		if (response.statusCode() / 100 != 2) {
			throw new IOException("HTTP " + response.statusCode());
		}
		return JsonParser.parseString(response.body());
	}

	static String cleanTitle(String title) {
		String clean = TITLE_SUFFIX.matcher(TITLE_NOISE.matcher(title).replaceAll("")).replaceAll("").trim();
		return clean.isEmpty() ? title : clean;
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String string(JsonObject json, String key) {
		JsonElement value = json.get(key);
		return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
	}

	private static boolean bool(JsonObject json, String key) {
		JsonElement value = json.get(key);
		return value != null && value.isJsonPrimitive() && value.getAsBoolean();
	}

	private static double number(JsonObject json, String key) {
		JsonElement value = json.get(key);
		try {
			return value != null && value.isJsonPrimitive() ? value.getAsDouble() : 0.0;
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	// ---- LRC ----------------------------------------------------------------------------------

	private record Entry(long start, String text) {
	}

	/** Parses synced LRC ({@code [mm:ss.xx] text}, several stamps per line allowed, optional word tags). */
	static @Nullable Track parse(String lrc, long durationMs) {
		long offset = 0L;
		List<Entry> entries = new ArrayList<>();
		for (String raw : lrc.split("\\r?\\n")) {
			String line = raw.trim();
			Matcher offsetTag = OFFSET_TAG.matcher(line);
			if (offsetTag.find()) {
				offset = Long.parseLong(offsetTag.group(1));
				continue;
			}
			Matcher tag = TIME_TAG.matcher(line);
			List<Long> stamps = new ArrayList<>(1);
			int textStart = 0;
			while (tag.find() && tag.start() == textStart) {
				stamps.add(millis(tag.group(1), tag.group(2)));
				textStart = tag.end();
			}
			if (stamps.isEmpty()) {
				continue; // metadata like [ar: …] or plain text
			}
			String text = line.substring(textStart).trim();
			for (long stamp : stamps) {
				entries.add(new Entry(stamp, text));
			}
		}
		if (entries.isEmpty()) {
			return null;
		}
		entries.sort((a, b) -> Long.compare(a.start, b.start));
		int n = entries.size();
		String[] lines = new String[n];
		long[] starts = new long[n];
		long[] ends = new long[n];
		String[][] words = new String[n][];
		long[][] wordStarts = new long[n][];
		boolean wordSynced = false;
		long shift = -offset; // a positive [offset:] makes lyrics appear sooner
		for (int i = 0; i < n; i++) {
			starts[i] = Math.max(0L, entries.get(i).start + shift);
		}
		for (int i = 0; i < n; i++) {
			long next = i + 1 < n ? starts[i + 1] : (durationMs > 0 ? Math.max(starts[i] + 1000L, durationMs) : starts[i] + LAST_LINE_MS);
			ends[i] = Math.min(next, starts[i] + (i + 1 < n ? MAX_LINE_MS : LAST_LINE_MS));
			String text = entries.get(i).text;
			if (SECTION.matcher(text).matches()) {
				text = ""; // a section label is a gap, not something sung
			}
			if (WORD_TAG.matcher(text).find()) {
				wordSynced = true;
				splitTimed(text, starts[i], ends[i], shift, i, lines, words, wordStarts);
			} else {
				lines[i] = text;
				words[i] = text.isEmpty() ? new String[0] : text.split("\\s+");
				wordStarts[i] = estimate(words[i], starts[i], ends[i]);
			}
		}
		return new Track(lines, starts, ends, words, wordStarts, wordSynced);
	}

	/** Enhanced LRC: {@code <mm:ss.xx>word <mm:ss.xx>word}. */
	private static void splitTimed(String text, long lineStart, long lineEnd, long shift, int index, String[] lines, String[][] words,
			long[][] wordStarts) {
		List<String> parts = new ArrayList<>();
		List<Long> times = new ArrayList<>();
		Matcher m = WORD_TAG.matcher(text);
		int last = 0;
		long time = lineStart;
		while (m.find()) {
			addWords(text.substring(last, m.start()), time, parts, times);
			time = Math.max(lineStart, millis(m.group(1), m.group(2)) + shift);
			last = m.end();
		}
		addWords(text.substring(last), time, parts, times);
		lines[index] = String.join(" ", parts);
		words[index] = parts.toArray(new String[0]);
		long[] starts = new long[times.size()];
		for (int i = 0; i < starts.length; i++) {
			starts[i] = Math.min(times.get(i), lineEnd);
		}
		wordStarts[index] = starts;
	}

	private static void addWords(String chunk, long time, List<String> parts, List<Long> times) {
		for (String word : chunk.trim().split("\\s+")) {
			if (!word.isEmpty()) {
				parts.add(word);
				times.add(time);
			}
		}
	}

	/**
	 * Word times within a line, by length. The line is sung at a natural pace (~70 ms per letter plus
	 * ~110 ms per word), kept between 35 % and 85 % of the time until the next line: a line followed
	 * by an instrumental break isn't stretched across the break, a slow ballad line isn't rushed.
	 */
	private static long[] estimate(String[] words, long start, long end) {
		long[] times = new long[words.length];
		if (words.length == 0) {
			return times;
		}
		long gap = Math.max(1L, end - start);
		long natural = 0L;
		for (String word : words) {
			natural += 70L * word.codePointCount(0, word.length()) + 110L;
		}
		long singing = Math.max(250L, Math.clamp(natural, Math.round(gap * 0.35), Math.round(gap * 0.85)));
		int total = 0;
		for (String word : words) {
			total += weight(word);
		}
		int done = 0;
		for (int i = 0; i < words.length; i++) {
			times[i] = start + singing * done / Math.max(1, total);
			done += weight(words[i]);
		}
		return times;
	}

	private static int weight(String word) {
		return 2 + word.codePointCount(0, word.length());
	}

	private static long millis(String minutes, String seconds) {
		String s = seconds.replace(':', '.');
		return Long.parseLong(minutes) * 60_000L + Math.round(Double.parseDouble(s) * 1000.0);
	}

	private static @Nullable Track demo;

	/** A short looping sample for the HUD editor (repeats every {@link #DEMO_LENGTH} ms). */
	public static Track demo() {
		Track track = demo;
		if (track == null) {
			track = demo = parse("[00:00.40] Lyrics float in\n[00:03.20] as the music plays\n[00:06.00] word by word", DEMO_LENGTH);
		}
		return track;
	}

	public static final long DEMO_LENGTH = 9000L;
}
