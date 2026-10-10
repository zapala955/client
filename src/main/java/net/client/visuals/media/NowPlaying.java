package net.client.visuals.media;

/**
 * An immutable snapshot of what the media session reports. Replaced as a whole (never mutated), so
 * the render thread can read it while the bridge thread publishes new ones.
 */
public final class NowPlaying {
	public static final NowPlaying NONE = new NowPlaying(false, "", "", "", "", false, 0L, 0L, 0L);

	public final boolean active;
	public final String app;
	public final String title;
	public final String artist;
	public final String album;
	public final boolean playing;
	/** Position at {@link #updatedAt} (ms). */
	public final long position;
	public final long duration;
	/** Wall-clock time (unix ms) the player last reported its position. */
	public final long updatedAt;
	/** Identifies the track (title + artist); changes exactly when the song does. */
	public final String key;

	public NowPlaying(boolean active, String app, String title, String artist, String album, boolean playing, long position,
			long duration, long updatedAt) {
		this.active = active;
		this.app = app;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.playing = playing;
		this.position = position;
		this.duration = duration;
		this.updatedAt = updatedAt;
		this.key = active ? title + '\u0000' + artist : "";
	}

	/** Position now: players report it every few seconds, so it's extrapolated while playing. */
	public long positionAt(long nowMillis) {
		long p = playing && updatedAt > 0L ? position + Math.max(0L, nowMillis - updatedAt) : position;
		return duration > 0L ? Math.clamp(p, 0L, duration) : Math.max(0L, p);
	}

	/** 0..1 progress through the track, or 0 when the duration is unknown. */
	public float progressAt(long nowMillis) {
		return duration > 0L ? positionAt(nowMillis) / (float) duration : 0f;
	}

	/** Spotify (and others) give "Artist A, Artist B"; searches and compact layouts want the first. */
	public String primaryArtist() {
		int comma = artist.indexOf(", ");
		return comma > 0 ? artist.substring(0, comma) : artist;
	}
}
