package net.client.visuals.browser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.client.visuals.LiquidClient;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Minimal Chrome DevTools Protocol client over Java's built-in WebSocket – no dependencies.
 *
 * <p>JSON-RPC: {@link #call} returns a future completed with the response object; messages without
 * an {@code id} are events and go to the event handler (on the WebSocket's thread). The browser runs
 * in "flatten" session mode, so page commands carry a {@code sessionId} on this one connection.</p>
 *
 * <p>Java's WebSocket forbids overlapping sends, so sends are chained; large messages (screencast
 * frames are base64 JPEGs) arrive in parts and are reassembled before parsing.</p>
 */
final class CdpClient implements WebSocket.Listener {
	private final AtomicInteger ids = new AtomicInteger();
	private final Map<Integer, CompletableFuture<JsonObject>> pending = new ConcurrentHashMap<>();
	private final StringBuilder partial = new StringBuilder();
	private final Consumer<JsonObject> events;
	private final Runnable onClosed;
	private final Object sendLock = new Object();
	private @Nullable WebSocket socket;
	private CompletableFuture<?> sendChain = CompletableFuture.completedFuture(null);
	private volatile boolean closed;

	private CdpClient(Consumer<JsonObject> events, Runnable onClosed) {
		this.events = events;
		this.onClosed = onClosed;
	}

	static CdpClient connect(URI uri, Consumer<JsonObject> events, Runnable onClosed) {
		CdpClient client = new CdpClient(events, onClosed);
		client.socket = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
				.newWebSocketBuilder()
				.buildAsync(uri, client)
				.join();
		return client;
	}

	CompletableFuture<JsonObject> call(String method, JsonObject params, @Nullable String sessionId) {
		int id = ids.incrementAndGet();
		JsonObject message = new JsonObject();
		message.addProperty("id", id);
		message.addProperty("method", method);
		message.add("params", params);
		if (sessionId != null) {
			message.addProperty("sessionId", sessionId);
		}
		CompletableFuture<JsonObject> future = new CompletableFuture<>();
		if (closed || socket == null) {
			future.completeExceptionally(new IllegalStateException("DevTools connection closed"));
			return future;
		}
		pending.put(id, future);
		String text = message.toString();
		synchronized (sendLock) {
			WebSocket ws = socket;
			sendChain = sendChain.handle((ignored, error) -> null).thenCompose(ignored -> ws.sendText(text, true));
		}
		return future;
	}

	/** Fire-and-forget variant for high-frequency input (mouse moves) and acks. */
	void send(String method, JsonObject params, @Nullable String sessionId) {
		call(method, params, sessionId).exceptionally(error -> null);
	}

	boolean isOpen() {
		return !closed;
	}

	void close() {
		if (closed) {
			return;
		}
		closed = true;
		WebSocket ws = socket;
		if (ws != null) {
			ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye").exceptionally(error -> null);
			ws.abort();
		}
		failPending();
	}

	private void failPending() {
		IllegalStateException error = new IllegalStateException("DevTools connection closed");
		pending.values().forEach(future -> future.completeExceptionally(error));
		pending.clear();
	}

	// ---- WebSocket.Listener ------------------------------------------------------------------

	@Override
	public void onOpen(WebSocket webSocket) {
		webSocket.request(1);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		partial.append(data);
		if (last) {
			String text = partial.toString();
			partial.setLength(0);
			try {
				dispatch(JsonParser.parseString(text).getAsJsonObject());
			} catch (RuntimeException e) {
				LiquidClient.LOGGER.warn("Browser: bad DevTools message", e);
			}
		}
		webSocket.request(1);
		return null;
	}

	private void dispatch(JsonObject message) {
		if (message.has("id")) {
			CompletableFuture<JsonObject> future = pending.remove(message.get("id").getAsInt());
			if (future != null) {
				future.complete(message);
			}
			return;
		}
		events.accept(message);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		markClosed();
		return null;
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		if (!closed) {
			LiquidClient.LOGGER.warn("Browser: DevTools connection error", error);
		}
		markClosed();
	}

	private void markClosed() {
		if (!closed) {
			closed = true;
			failPending();
			onClosed.run();
		}
	}
}
