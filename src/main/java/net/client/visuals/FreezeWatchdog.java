package net.client.visuals;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Diagnostics for freezes: a daemon thread that notices when the client thread stops ticking and
 * writes where it's stuck to the log – its stack trace (and the integrated server's, which the
 * client waits on while joining a world) – after {@value #STALL_SECONDS} s, again every
 * {@value #REPEAT_SECONDS} s while it stays stuck, and a line when it recovers. A freeze that would
 * otherwise leave nothing in the log then names its cause.
 *
 * <p>Started on the first client tick, so the game's own start-up never counts. One volatile write
 * per tick; the watchdog never touches game state.</p>
 */
public final class FreezeWatchdog {
	private static final int STALL_SECONDS = 10;
	private static final int REPEAT_SECONDS = 20;
	private static final int MAX_FRAMES = 60;

	private static volatile long lastTick;
	private static @Nullable Thread client;

	private FreezeWatchdog() {
	}

	/** End of every client tick (client thread). */
	public static void tick() {
		lastTick = System.nanoTime();
		if (client == null) {
			client = Thread.currentThread();
			Thread watchdog = new Thread(FreezeWatchdog::run, "Liquid freeze watchdog");
			watchdog.setDaemon(true);
			watchdog.setPriority(Thread.MIN_PRIORITY);
			watchdog.start();
		}
	}

	private static void run() {
		long reported = 0L;
		boolean stuck = false;
		while (true) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				return;
			}
			long now = System.nanoTime();
			long since = now - lastTick;
			if (since > STALL_SECONDS * 1_000_000_000L) {
				if (!stuck || now - reported > REPEAT_SECONDS * 1_000_000_000L) {
					report(since);
					reported = now;
					stuck = true;
				}
			} else if (stuck) {
				stuck = false;
				LiquidClient.LOGGER.warn("Freeze watchdog: the client is ticking again");
			}
		}
	}

	private static void report(long sinceNanos) {
		Thread thread = client;
		if (thread == null) {
			return;
		}
		StringBuilder out = new StringBuilder(4096);
		out.append("Freeze watchdog: the client hasn't ticked for ").append(sinceNanos / 1_000_000_000L)
				.append(" s - if the game looks frozen, this is where it's stuck.");
		append(out, thread, thread.getStackTrace());
		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			if ("Server thread".equals(entry.getKey().getName())) {
				append(out, entry.getKey(), entry.getValue());
			}
		}
		LiquidClient.LOGGER.warn(out.toString());
	}

	private static void append(StringBuilder out, Thread thread, StackTraceElement[] stack) {
		out.append("\n\"").append(thread.getName()).append("\" ").append(thread.getState());
		int frames = Math.min(stack.length, MAX_FRAMES);
		for (int i = 0; i < frames; i++) {
			out.append("\n\tat ").append(stack[i]);
		}
		if (stack.length > frames) {
			out.append("\n\t... ").append(stack.length - frames).append(" more");
		}
	}
}
