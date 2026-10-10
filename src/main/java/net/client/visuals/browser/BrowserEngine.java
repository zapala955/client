package net.client.visuals.browser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A headless Chromium (Edge / Chrome / Brave from the system) driven over the DevTools protocol.
 *
 * <h2>Why this design</h2>
 * Embedding Chromium (JCEF/MCEF) means shipping ~150 MB of natives and uploading pixels with raw
 * OpenGL, which 26.2+ no longer allows. Driving the browser the system already has keeps the mod
 * small: pixels arrive as a JPEG screencast, input goes back as synthetic DOM events, and the page
 * runs in its own process (a crashing page can't take Minecraft down).
 *
 * <h2>Threads</h2>
 * Startup runs on a private worker thread. DevTools events (frames, URL/title changes) arrive on the
 * WebSocket thread; frames are decoded there with stb straight into a {@link NativeImage} and handed
 * to the render thread through {@link #takeFrame()} (latest-wins, older undelivered frames are freed).
 *
 * <h2>Lifecycle</h2>
 * {@link #stop()} closes the connection and kills the whole browser process tree; a JVM shutdown hook
 * does the same if the game exits abruptly. Its profile (cookies, logins) lives in
 * {@code config/liquidclient/browser}, isolated from the user's normal browser profile.
 */
public final class BrowserEngine {
	public enum Status { STOPPED, STARTING, READY, FAILED }

	private static final Path PROFILE = FabricLoader.getInstance().getConfigDir().resolve("liquidclient/browser");

	private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r, "Liquid Browser");
		thread.setDaemon(true);
		return thread;
	});
	private final AtomicReference<NativeImage> latestFrame = new AtomicReference<>();

	private volatile Status status = Status.STOPPED;
	private volatile String error = "";
	private volatile String url = "";
	private volatile String title = "";
	private volatile boolean loading;
	private volatile String engineName = "";
	private volatile @Nullable Process process;
	private volatile @Nullable CdpClient cdp;
	private volatile @Nullable String sessionId;
	private volatile @Nullable String targetId;
	private volatile int width = 1280;
	private volatile int height = 720;
	private volatile int generation;

	BrowserEngine() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::killProcess, "Liquid Browser shutdown"));
	}

	// ---- Lifecycle ----------------------------------------------------------------------------

	/** Starts the browser if it isn't running. Returns immediately; watch {@link #status()}. */
	void start(int enginePreference, int width, int height, String startUrl, boolean darkPages) {
		if (status == Status.STARTING || status == Status.READY) {
			return;
		}
		this.width = width;
		this.height = height;
		status = Status.STARTING;
		error = "";
		int token = ++generation;
		worker.execute(() -> {
			try {
				launch(token, enginePreference, startUrl, darkPages);
			} catch (Exception e) {
				if (token == generation) {
					LiquidClient.LOGGER.warn("Browser failed to start", e);
					error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
					status = Status.FAILED;
					killProcess();
				}
			}
		});
	}

	private void launch(int token, int enginePreference, String startUrl, boolean darkPages) throws Exception {
		Path exe = BrowserExecutable.find(enginePreference);
		if (exe == null) {
			throw new IOException("No Chromium browser found (install Edge, Chrome or Brave)");
		}
		engineName = exe.getFileName().toString();
		Files.createDirectories(PROFILE);
		Path portFile = PROFILE.resolve("DevToolsActivePort");
		Files.deleteIfExists(portFile);

		List<String> command = new ArrayList<>(List.of(
				exe.toString(),
				"--headless=new",
				"--remote-debugging-port=0",
				"--user-data-dir=" + PROFILE.toAbsolutePath(),
				"--no-first-run",
				"--no-default-browser-check",
				"--disable-features=Translate",
				"--autoplay-policy=no-user-gesture-required",
				"--force-device-scale-factor=1",
				"--window-size=" + width + "," + height,
				"about:blank"));
		LiquidClient.LOGGER.info("Browser: launching {}", exe);
		Process started = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
		process = started;

		// The browser writes "<port>\n<browser ws path>" once DevTools is listening.
		long deadline = System.currentTimeMillis() + 20_000L;
		List<String> lines = List.of();
		while (System.currentTimeMillis() < deadline) {
			if (token != generation) {
				killProcess(); // despawned while starting
				return;
			}
			if (!started.isAlive()) {
				throw new IOException(engineName + " exited during startup (code " + started.exitValue() + ")");
			}
			if (Files.isRegularFile(portFile)) {
				lines = Files.readAllLines(portFile);
				if (lines.size() >= 2) {
					break;
				}
			}
			Thread.sleep(50L);
		}
		if (lines.size() < 2) {
			throw new IOException(engineName + " didn't open DevTools in time");
		}
		if (token != generation) {
			killProcess();
			return;
		}
		URI endpoint = URI.create("ws://127.0.0.1:" + lines.get(0).trim() + lines.get(1).trim());
		CdpClient client = CdpClient.connect(endpoint, this::onEvent, this::onDisconnected);
		if (token != generation) {
			client.close();
			killProcess();
			return;
		}
		cdp = client;

		JsonObject version = await(client.call("Browser.getVersion", new JsonObject(), null));
		String userAgent = result(version).get("userAgent").getAsString().replace("HeadlessChrome", "Chrome");

		JsonObject discover = new JsonObject();
		discover.addProperty("discover", true);
		await(client.call("Target.setDiscoverTargets", discover, null));

		JsonObject create = new JsonObject();
		create.addProperty("url", "about:blank");
		String target = result(await(client.call("Target.createTarget", create, null))).get("targetId").getAsString();
		targetId = target;
		JsonObject attach = new JsonObject();
		attach.addProperty("targetId", target);
		attach.addProperty("flatten", true);
		String session = result(await(client.call("Target.attachToTarget", attach, null))).get("sessionId").getAsString();
		sessionId = session;

		await(client.call("Page.enable", new JsonObject(), session));
		JsonObject ua = new JsonObject();
		ua.addProperty("userAgent", userAgent);
		await(client.call("Emulation.setUserAgentOverride", ua, session));
		JsonObject metrics = new JsonObject();
		metrics.addProperty("width", width);
		metrics.addProperty("height", height);
		metrics.addProperty("deviceScaleFactor", 1);
		metrics.addProperty("mobile", false);
		await(client.call("Emulation.setDeviceMetricsOverride", metrics, session));
		JsonObject focus = new JsonObject();
		focus.addProperty("enabled", true);
		await(client.call("Emulation.setFocusEmulationEnabled", focus, session));
		setDarkPages(darkPages);

		JsonObject cast = new JsonObject();
		cast.addProperty("format", "jpeg");
		cast.addProperty("quality", 82);
		cast.addProperty("maxWidth", width);
		cast.addProperty("maxHeight", height);
		cast.addProperty("everyNthFrame", 1);
		await(client.call("Page.startScreencast", cast, session));

		if (token != generation) {
			return; // stopped while we were setting up – stop() already tore everything down
		}
		status = Status.READY;
		LiquidClient.LOGGER.info("Browser: ready ({}, {}x{})", engineName, width, height);
		navigate(startUrl);
	}

	/** Closes the session and kills the process tree. Safe to call from any thread, repeatedly. */
	void stop() {
		generation++;
		CdpClient client = cdp;
		cdp = null;
		sessionId = null;
		targetId = null;
		if (client != null) {
			client.send("Browser.close", new JsonObject(), null);
			client.close();
		}
		worker.execute(this::killProcess);
		NativeImage frame = latestFrame.getAndSet(null);
		if (frame != null) {
			frame.close();
		}
		status = Status.STOPPED;
		url = "";
		title = "";
		loading = false;
	}

	private void killProcess() {
		Process running = process;
		process = null;
		if (running == null) {
			return;
		}
		try {
			running.waitFor(800, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
		running.descendants().forEach(ProcessHandle::destroyForcibly);
		running.destroyForcibly();
	}

	private void onDisconnected() {
		if (status == Status.READY) {
			error = "Browser process closed";
			status = Status.FAILED;
		}
	}

	// ---- Events (WebSocket thread) ------------------------------------------------------------

	private void onEvent(JsonObject message) {
		String method = message.get("method").getAsString();
		JsonObject params = message.has("params") ? message.getAsJsonObject("params") : new JsonObject();
		switch (method) {
			case "Page.screencastFrame" -> onFrame(params);
			case "Page.frameNavigated" -> {
				JsonObject frame = params.getAsJsonObject("frame");
				if (!frame.has("parentId")) {
					url = frame.get("url").getAsString();
				}
			}
			case "Page.navigatedWithinDocument" -> url = params.get("url").getAsString();
			case "Page.frameStartedLoading" -> loading = true;
			case "Page.frameStoppedLoading", "Page.loadEventFired" -> loading = false;
			case "Target.targetInfoChanged" -> {
				JsonObject info = params.getAsJsonObject("targetInfo");
				if (info.get("targetId").getAsString().equals(targetId)) {
					title = info.get("title").getAsString();
					url = info.get("url").getAsString();
				}
			}
			case "Target.targetCreated" -> adoptPopup(params.getAsJsonObject("targetInfo"));
			default -> {
			}
		}
	}

	/** Popups / target=_blank links open in the same screen instead of an invisible new tab. */
	private void adoptPopup(JsonObject info) {
		CdpClient client = cdp;
		String ours = targetId;
		if (client == null || ours == null || !"page".equals(info.get("type").getAsString())) {
			return;
		}
		if (!info.has("openerId") || !ours.equals(info.get("openerId").getAsString())) {
			return;
		}
		String popupUrl = info.get("url").getAsString();
		JsonObject close = new JsonObject();
		close.addProperty("targetId", info.get("targetId").getAsString());
		client.send("Target.closeTarget", close, null);
		if (!popupUrl.isEmpty() && !popupUrl.equals("about:blank")) {
			navigate(popupUrl);
		}
	}

	private void onFrame(JsonObject params) {
		CdpClient client = cdp;
		String session = sessionId;
		try {
			NativeImage image = decodeJpeg(Base64.getDecoder().decode(params.get("data").getAsString()));
			if (image != null) {
				NativeImage replaced = latestFrame.getAndSet(image);
				if (replaced != null) {
					replaced.close(); // render thread never saw it
				}
			}
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.debug("Browser: bad frame", e);
		}
		if (client != null && session != null) {
			// Ack after decoding: the browser only sends the next frame once we've kept up.
			JsonObject ack = new JsonObject();
			ack.addProperty("sessionId", params.get("sessionId").getAsInt());
			client.send("Page.screencastFrameAck", ack, session);
		}
	}

	/** stb decodes straight into native memory that the NativeImage then owns (no Java heap copy). */
	private static @Nullable NativeImage decodeJpeg(byte[] jpeg) {
		ByteBuffer encoded = MemoryUtil.memAlloc(jpeg.length);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			encoded.put(jpeg).flip();
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, w, h, comp, 4);
			if (pixels == null) {
				return null;
			}
			return new NativeImage(NativeImage.Format.RGBA, w.get(0), h.get(0), true, MemoryUtil.memAddress(pixels));
		} finally {
			MemoryUtil.memFree(encoded);
		}
	}

	/** Render thread: the newest decoded frame, now owned by the caller (close it after upload). */
	@Nullable NativeImage takeFrame() {
		return latestFrame.getAndSet(null);
	}

	// ---- Navigation ---------------------------------------------------------------------------

	void navigate(String target) {
		JsonObject params = new JsonObject();
		params.addProperty("url", target);
		page("Page.navigate", params);
		url = target;
		loading = true;
	}

	void reload() {
		page("Page.reload", new JsonObject());
	}

	void back() {
		history(-1);
	}

	void forward() {
		history(1);
	}

	private void history(int delta) {
		CdpClient client = cdp;
		String session = sessionId;
		if (client == null || session == null) {
			return;
		}
		client.call("Page.getNavigationHistory", new JsonObject(), session).thenAccept(response -> {
			JsonObject r = result(response);
			int index = r.get("currentIndex").getAsInt() + delta;
			JsonArray entries = r.getAsJsonArray("entries");
			if (index >= 0 && index < entries.size()) {
				JsonObject go = new JsonObject();
				go.addProperty("entryId", entries.get(index).getAsJsonObject().get("id").getAsInt());
				client.send("Page.navigateToHistoryEntry", go, session);
			}
		}).exceptionally(error -> null);
	}

	void setDarkPages(boolean dark) {
		JsonObject feature = new JsonObject();
		feature.addProperty("name", "prefers-color-scheme");
		feature.addProperty("value", dark ? "dark" : "light");
		JsonArray features = new JsonArray();
		features.add(feature);
		JsonObject params = new JsonObject();
		params.add("features", features);
		page("Emulation.setEmulatedMedia", params);
	}

	/** The page's current text selection (for Ctrl+C). */
	CompletableFuture<String> selection() {
		CdpClient client = cdp;
		String session = sessionId;
		if (client == null || session == null) {
			return CompletableFuture.completedFuture("");
		}
		JsonObject params = new JsonObject();
		params.addProperty("expression", "String(window.getSelection())");
		params.addProperty("returnByValue", true);
		return client.call("Runtime.evaluate", params, session).thenApply(response -> {
			JsonObject r = result(response).getAsJsonObject("result");
			return r != null && r.has("value") ? r.get("value").getAsString() : "";
		});
	}

	// ---- Input --------------------------------------------------------------------------------

	/**
	 * @param type    mousePressed / mouseReleased / mouseMoved
	 * @param button  none / left / middle / right
	 * @param buttons bitmask of held buttons (1 left, 2 right, 4 middle) – needed for drag-select
	 */
	void mouse(String type, double x, double y, String button, int clickCount, int buttons, int modifiers) {
		JsonObject params = new JsonObject();
		params.addProperty("type", type);
		params.addProperty("x", x);
		params.addProperty("y", y);
		params.addProperty("button", button);
		params.addProperty("buttons", buttons);
		params.addProperty("clickCount", clickCount);
		params.addProperty("modifiers", modifiers);
		page("Input.dispatchMouseEvent", params);
	}

	void wheel(double x, double y, double deltaX, double deltaY) {
		JsonObject params = new JsonObject();
		params.addProperty("type", "mouseWheel");
		params.addProperty("x", x);
		params.addProperty("y", y);
		params.addProperty("deltaX", deltaX);
		params.addProperty("deltaY", deltaY);
		page("Input.dispatchMouseEvent", params);
	}

	/** rawKeyDown / keyUp for a physical key. */
	void key(String type, BrowserKeys.Key key, int modifiers) {
		JsonObject params = new JsonObject();
		params.addProperty("type", type);
		params.addProperty("key", key.key());
		params.addProperty("code", key.code());
		params.addProperty("windowsVirtualKeyCode", key.virtualKey());
		params.addProperty("nativeVirtualKeyCode", key.virtualKey());
		params.addProperty("modifiers", modifiers);
		page("Input.dispatchKeyEvent", params);
	}

	/** Typed text (a {@code char} event per call – keyboard layout and IME independent). */
	void text(String text) {
		JsonObject params = new JsonObject();
		params.addProperty("type", "char");
		params.addProperty("text", text);
		params.addProperty("unmodifiedText", text);
		page("Input.dispatchKeyEvent", params);
	}

	/** Pasting longer text is faster and more reliable as one insertion. */
	void insertText(String text) {
		JsonObject params = new JsonObject();
		params.addProperty("text", text);
		page("Input.insertText", params);
	}

	private void page(String method, JsonObject params) {
		CdpClient client = cdp;
		String session = sessionId;
		if (client != null && session != null) {
			client.send(method, params, session);
		}
	}

	// ---- State --------------------------------------------------------------------------------

	public Status status() {
		return status;
	}

	public String error() {
		return error;
	}

	public String url() {
		return url;
	}

	public String title() {
		return title;
	}

	public boolean loading() {
		return loading;
	}

	public String engineName() {
		return engineName;
	}

	int width() {
		return width;
	}

	int height() {
		return height;
	}

	private static JsonObject await(CompletableFuture<JsonObject> future) throws Exception {
		JsonObject response = future.get(10, TimeUnit.SECONDS);
		if (response.has("error")) {
			throw new IOException("DevTools: " + response.getAsJsonObject("error").get("message").getAsString());
		}
		return response;
	}

	private static JsonObject result(JsonObject response) {
		return response.has("result") ? response.getAsJsonObject("result") : new JsonObject();
	}
}
