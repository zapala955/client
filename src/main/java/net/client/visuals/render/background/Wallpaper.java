package net.client.visuals.render.background;

import org.jspecify.annotations.Nullable;

/**
 * Every main-menu wallpaper, in the order the setting lists them. Ids are what the config stores,
 * so the order can change freely.
 *
 * <ul>
 *   <li><b>Photos</b> – bundled 3840 px photos (Unsplash License), cover-cropped and resampled to the
 *       exact framebuffer, with the vertical anchor that keeps each subject in frame.</li>
 *   <li><b>Live</b> – animated GLSL wallpapers evaluated per device pixel, so they're as sharp at 4K
 *       (or 8K) as at 1080p; each has an analytic "frosted" version for glass panels.</li>
 *   <li><b>Custom</b> – the user's own image from {@code config/liquidclient/backgrounds/};
 *       <b>Panorama</b> – vanilla's rotating panorama.</li>
 * </ul>
 */
public enum Wallpaper {
	LAKE("lake", Kind.PHOTO, "wallpapers/lake.jpg", 0.42f),
	DUSK("dusk", Kind.PHOTO, "wallpapers/dusk.jpg", 0.40f),
	HALF_DOME("halfdome", Kind.PHOTO, "wallpapers/halfdome.jpg", 0.50f),
	BIG_SUR("bigsur", Kind.PHOTO, "wallpapers/bigsur.jpg", 0.50f),
	DESERT("desert", Kind.PHOTO, "wallpapers/desert.jpg", 0.50f),
	WHITE_SANDS("whitesands", Kind.PHOTO, "wallpapers/whitesands.jpg", 0.60f),
	NORTHERN_LIGHTS("northernlights", Kind.PHOTO, "wallpapers/northernlights.jpg", 0.45f),
	MILKY_WAY("milkyway", Kind.PHOTO, "wallpapers/milkyway.jpg", 0.40f),
	MIST("mist", Kind.PHOTO, "wallpapers/mist.jpg", 0.50f),
	TOKYO("tokyo", Kind.PHOTO, "wallpapers/tokyo.jpg", 0.50f),
	ICELAND("iceland", Kind.PHOTO, "wallpapers/iceland.jpg", 0.50f),
	AURORA("aurora", Kind.LIVE, null, 0f),
	NEBULA("nebula", Kind.LIVE, null, 0f),
	SILK("silk", Kind.LIVE, null, 0f),
	GLOW("glow", Kind.LIVE, null, 0f),
	LIQUID("liquid", Kind.LIVE, null, 0f),
	RIBBONS("ribbons", Kind.LIVE, null, 0f),
	DUNES("dunes", Kind.LIVE, null, 0f),
	IMAGE("image", Kind.CUSTOM, null, 0.5f),
	PANORAMA("panorama", Kind.PANORAMA, null, 0f);

	public enum Kind { PHOTO, LIVE, CUSTOM, PANORAMA }

	private static final Wallpaper[] VALUES = values();

	public final String id;
	public final Kind kind;
	/** Photo resource path (namespace liquidclient), or null. */
	public final @Nullable String resource;
	/** Vertical crop anchor for photos (0 top … 1 bottom). */
	public final float anchorY;

	Wallpaper(String id, Kind kind, @Nullable String resource, float anchorY) {
		this.id = id;
		this.kind = kind;
		this.resource = resource;
		this.anchorY = anchorY;
	}

	/** Shows a photo texture (bundled or the user's). */
	public boolean isImage() {
		return kind == Kind.PHOTO || kind == Kind.CUSTOM;
	}

	public boolean isLive() {
		return kind == Kind.LIVE;
	}

	/** The boot screen's pre-rendered frosted first frame of a live wallpaper (see tools). */
	public @Nullable String bootFrame() {
		return isLive() ? "/assets/liquidclient/textures/gui/loading/live/" + id + ".jpg" : null;
	}

	public static Wallpaper byIndex(int index) {
		return VALUES[Math.floorMod(index, VALUES.length)];
	}

	public static String[] ids() {
		String[] ids = new String[VALUES.length];
		for (int i = 0; i < VALUES.length; i++) {
			ids[i] = VALUES[i].id;
		}
		return ids;
	}
}
