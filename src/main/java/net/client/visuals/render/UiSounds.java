package net.client.visuals.render;

import net.client.visuals.LiquidClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Interface sounds defined in {@code assets/liquidclient/sounds.json}.
 *
 * <p>The sounds are Google's Material Design sound resources (CC BY 4.0, see
 * {@code MATERIAL-SOUNDS-LICENSE.txt}) – soft woody taps, two-note plucks for open / close and
 * confirm chimes for switches – prepared by {@code tools/ui_sounds.py}. Clicks rotate between four
 * tap variants and get a slight random pitch so repeats never sound mechanical, and the rapid-fire
 * sounds (hover, slider detents) are rate-limited. Playback is client-only and keyed by the sound's identifier, so these
 * events don't need to be in the (synced) sound registry.</p>
 */
public final class UiSounds {
	public static final SoundEvent CLICK = event("ui.click");
	public static final SoundEvent HOVER = event("ui.hover");
	public static final SoundEvent TOGGLE_ON = event("ui.toggle_on");
	public static final SoundEvent TOGGLE_OFF = event("ui.toggle_off");
	public static final SoundEvent OPEN = event("ui.open");
	public static final SoundEvent CLOSE = event("ui.close");
	public static final SoundEvent TICK = event("ui.tick");
	public static final SoundEvent WAKE = event("ui.wake");
	/** Photo Mode: a DSLR shutter (mirror slap, then the second curtain), synthesised by {@code tools/camera_sounds.py}. */
	public static final SoundEvent SHUTTER = event("camera.shutter");
	/** Photo Mode: the focus-confirm "bip-bip". */
	public static final SoundEvent FOCUS = event("camera.focus");

	private static final long HOVER_GAP_NANOS = 45_000_000L;
	private static final long TICK_GAP_NANOS = 28_000_000L;
	private static long lastHover;
	private static long lastTick;
	private static boolean woke;

	private UiSounds() {
	}

	private static SoundEvent event(String path) {
		return SoundEvent.createVariableRangeEvent(LiquidClient.id(path));
	}

	public static void play(SoundEvent sound, float pitch) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 1f));
	}

	/** {@code pitch} ± {@code spread}, so repeats are never identical. */
	private static void playVaried(SoundEvent sound, float pitch, float spread) {
		float r = ThreadLocalRandom.current().nextFloat() * 2f - 1f;
		play(sound, pitch * (1f + r * spread));
	}

	public static void click() {
		playVaried(CLICK, 1f, 0.025f);
	}

	public static void hover() {
		long now = System.nanoTime();
		if (now - lastHover >= HOVER_GAP_NANOS) {
			lastHover = now;
			playVaried(HOVER, 1f, 0.04f);
		}
	}

	/** Rising pop when switching on, falling when switching off – like iOS. */
	public static void toggle(boolean on) {
		playVaried(on ? TOGGLE_ON : TOGGLE_OFF, 1f, 0.015f);
	}

	public static void open() {
		play(OPEN, 1f);
	}

	public static void close() {
		play(CLOSE, 1f);
	}

	/**
	 * Slider detent; {@code position} (0..1) raises the pitch along the track, so dragging "plays" a
	 * gentle scale instead of one repeated tick.
	 */
	public static void tick(double position) {
		long now = System.nanoTime();
		if (now - lastTick >= TICK_GAP_NANOS) {
			lastTick = now;
			play(TICK, 0.85f + 0.4f * (float) Math.clamp(position, 0.0, 1.0));
		}
	}

	public static void shutter() {
		playVaried(SHUTTER, 1f, 0.02f);
	}

	public static void focus() {
		play(FOCUS, 1f);
	}

	/** The main menu's wake chime – once per game launch. */
	public static void wake() {
		if (!woke) {
			woke = true;
			play(WAKE, 1f);
		}
	}
}
