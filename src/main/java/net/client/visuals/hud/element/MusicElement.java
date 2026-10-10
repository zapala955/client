package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.HudModule;
import net.client.visuals.hud.HudStyle;
import net.client.visuals.media.AlbumArt;
import net.client.visuals.media.MediaBridge;
import net.client.visuals.media.MusicView;
import net.client.visuals.module.setting.BooleanSetting;
import net.client.visuals.module.setting.ModeSetting;
import net.client.visuals.module.setting.NumberSetting;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2fStack;

/**
 * What's playing on the PC (Spotify by default, or any player) – see {@code media.MediaBridge}.
 *
 * <p>Six layouts: <b>Card</b> (art, title, artist, progress, times), <b>Compact</b> (one line),
 * <b>Island</b> (black pill with a live level meter, like the iPhone's Dynamic Island),
 * <b>Vinyl</b> (the cover spins on a record while playing), <b>Ambient</b> (the card is lit by a
 * blurred copy of the cover, like Apple Music) and <b>Minimal</b> (text only).</p>
 */
public final class MusicElement extends HudModule {
	public static final String[] LAYOUTS = {"card", "compact", "island", "vinyl", "ambient", "minimal"};
	private static final int CARD = 0;
	private static final int COMPACT = 1;
	private static final int ISLAND = 2;
	private static final int VINYL = 3;
	private static final int AMBIENT = 4;
	private static final int MINIMAL = 5;

	public final ModeSetting layout = add(new ModeSetting("layout", "liquidclient.hud.music.layout", LAYOUTS, CARD));
	public final ModeSetting source = add(new ModeSetting("source", "liquidclient.hud.music.source", new String[]{"spotify", "any"}, 0));
	public final NumberSetting cardWidth = add(new NumberSetting("card_width", "liquidclient.hud.music.width", 140, 280, 5, 175));
	public final BooleanSetting progress = add(new BooleanSetting("progress", "liquidclient.hud.music.progress", true));
	public final BooleanSetting times = add(new BooleanSetting("times", "liquidclient.hud.music.times", true));
	public final BooleanSetting visualizer = add(new BooleanSetting("level_bars", "liquidclient.hud.music.visualizer", false));
	public final BooleanSetting albumAccent = add(new BooleanSetting("album_accent", "liquidclient.hud.music.album_accent", true));
	public final BooleanSetting marquee = add(new BooleanSetting("marquee", "liquidclient.hud.music.marquee", true));
	public final BooleanSetting onlineArt = add(new BooleanSetting("online_art", "liquidclient.hud.music.online_art", true));
	public final BooleanSetting hideIdle = add(new BooleanSetting("hide_idle", "liquidclient.hud.music.hide_idle", true));

	private final MusicView view = new MusicView();

	public MusicElement() {
		super("hud_music", true, 0.995f, 0.01f);
	}

	private String idleTitle() {
		return MediaBridge.supported() ? "Not playing" : "Music";
	}

	private String idleArtist() {
		if (!MediaBridge.supported()) {
			return "Needs Windows";
		}
		return source.index() == 0 ? "Play something on Spotify" : "Play some music";
	}

	@Override
	public void layout(HudContext ctx) {
		view.update(ctx.dt, source.index() == 1, onlineArt.get(), idleTitle(), idleArtist());
		float w = (float) cardWidth.get();
		switch (layout.index()) {
			case COMPACT -> {
				width = w;
				height = 22f;
			}
			case ISLAND -> {
				width = w * 0.9f;
				height = 28f;
			}
			case VINYL -> {
				width = w;
				height = progress.get() ? 50f : 44f;
			}
			case AMBIENT -> {
				width = w;
				height = 62f;
			}
			case MINIMAL -> {
				float text = view.title.width() + 10f + view.artist.width();
				width = Math.min(w, 8f + 12f + 5f + text + 8f);
				height = progress.get() ? 20f : 16f;
			}
			default -> {
				width = w;
				height = progress.get() ? 44f : 38f;
			}
		}
	}

	@Override
	public boolean visible(HudContext ctx) {
		return ctx.editing || view.track.active || !hideIdle.get();
	}

	private int accent(HudContext ctx) {
		int base = ctx.accent(phase());
		return albumAccent.get() ? AlbumArt.accent(base) : base;
	}

	@Override
	public void render(GuiGraphicsExtractor g, HudContext ctx) {
		int accent = accent(ctx);
		switch (layout.index()) {
			case COMPACT -> compact(g, ctx, accent);
			case ISLAND -> island(g, ctx, accent);
			case VINYL -> vinyl(g, ctx, accent);
			case AMBIENT -> ambient(g, ctx, accent);
			case MINIMAL -> minimal(g, ctx, accent);
			default -> card(g, ctx, accent);
		}
	}

	private boolean shadow(HudContext ctx) {
		return ctx.theme.textShadow.get() || HudStyle.needsShadow(ctx.style(this));
	}

	// ---- Layouts ------------------------------------------------------------------------------

	/**
	 * The card: 32-unit cover, semibold title, small artist with the "1:16 / 3:26" timeline on the
	 * same row, and a thin bar that glides with the song.
	 */
	private void card(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		ctx.background(g, this, width, height);
		final float pad = 6f;
		final float cover = 32f;
		view.cover(g, pad, pad, cover, 6f, ctx.time);
		float tx = pad + cover + 7f;
		float textW = width - tx - pad;
		boolean shadow = shadow(ctx);
		float titleW = textW - (visualizer.get() ? 14f : 0f);
		view.clipped(g, view.title, tx, pad + 2.5f, titleW, HudContext.TEXT, shadow, marquee.get());
		if (visualizer.get()) {
			view.bars(g, width - pad - 10f, pad + 10f, 8f, 1.6f, 1.2f, 4, ColorUtil.fade(0xFFFFFFFF, 0.85f), ctx.dt, ctx.time);
		}
		boolean showTime = times.get() && view.track.active;
		float timeW = showTime ? view.timeline.width() : 0f;
		view.clipped(g, view.artistSmall, tx, pad + 14f, textW - (showTime ? timeW + 8f : 0f), HudContext.TEXT_DIM, shadow, marquee.get());
		if (showTime) {
			ctx.text(g, view.timeline, tx + textW - timeW, pad + 14f, HudContext.TEXT_DIM);
		}
		if (progress.get()) {
			float by = height - pad - 3f - 5f;
			float fill = Math.max(3f, textW * Math.clamp(view.smoothProgress(), 0f, 1f));
			RenderUtils.fill(g, tx, by, textW, 3f, 1.5f, 0x40FFFFFF);
			if (view.track.active) {
				RenderUtils.fill(g, tx, by, fill, 3f, 1.5f, 0xDCFFFFFF);
			}
		}
	}

	private void compact(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		ctx.background(g, this, width, height);
		float art = 14f;
		view.art(g, 4f, 4f, art, 3.5f, accent);
		float tx = 4f + art + 6f;
		float avail = width - tx - 7f;
		boolean shadow = shadow(ctx);
		float titleW = Math.min(view.title.width(), avail * 0.62f);
		view.clipped(g, view.title, tx, 11f - view.title.capHeight() * 0.5f, titleW, HudContext.TEXT, shadow, marquee.get());
		float ax = tx + titleW + 6f;
		if (ax < width - 12f) {
			view.clipped(g, view.artist, ax, 11f - view.artist.capHeight() * 0.5f, width - ax - 7f, HudContext.TEXT_DIM, shadow, marquee.get());
		}
		if (progress.get() && view.track.active) {
			float r = ctx.radius(height);
			float inset = Math.max(r, 4f);
			RenderUtils.fill(g, inset, height - 2f, (width - inset * 2f) * Math.clamp(view.progress, 0f, 1f), 1f, 0.5f, accent);
		}
	}

	private void island(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		HudStyle.background(g, HudStyle.ISLAND, width, height, height * 0.5f, accent, 1f, ctx.time);
		float art = 20f;
		float pad = 4f;
		view.art(g, pad, pad, art, art * 0.5f, accent);
		float tx = pad + art + 7f;
		float meter = visualizer.get() ? 22f : 6f;
		float textW = width - tx - meter - 8f;
		view.clipped(g, view.title, tx, 6f, textW, HudContext.TEXT, false, marquee.get());
		view.clipped(g, view.artist, tx, 16f, textW, HudContext.TEXT_DIM, false, marquee.get());
		if (visualizer.get()) {
			view.bars(g, width - 23f, height * 0.5f + 5.5f, 11f, 2f, 1.6f, 5, accent, ctx.dt, ctx.time);
		}
		if (progress.get() && view.track.active) {
			// A hairline of progress along the bottom curve.
			float inset = height * 0.5f;
			RenderUtils.fill(g, inset, height - 1.5f, (width - inset * 2f) * Math.clamp(view.progress, 0f, 1f), 1f, 0.5f,
					ColorUtil.fade(accent, 0.9f));
		}
	}

	private void vinyl(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		ctx.background(g, this, width, height);
		float disc = height - 8f;
		float cx = 4f + disc * 0.5f;
		float cy = height * 0.5f;
		float r = disc * 0.5f;
		RenderUtils.shadow(g, cx - r, cy - r + 1f, disc, disc, r, 3f, 0x55000000);
		RenderUtils.circle(g, cx, cy, r, 0xFF0C0C0F);
		RenderUtils.ring(g, cx, cy, r * 0.86f, HighDpiEngine.px(), 0x1AFFFFFF);
		RenderUtils.ring(g, cx, cy, r * 0.74f, HighDpiEngine.px(), 0x14FFFFFF);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate(view.angle());
		float label = r * 1.16f;
		view.art(g, -label * 0.5f, -label * 0.5f, label, label * 0.5f, accent);
		pose.popMatrix();
		RenderUtils.circle(g, cx, cy, 1.6f, 0xFF0C0C0F);
		// Fixed sheen across the record.
		RenderUtils.light(g, cx - r, cy - r, disc, disc * 0.5f, r, 0x14FFFFFF, 0x00FFFFFF);

		float tx = 4f + disc + 8f;
		float textW = width - tx - 6f - (visualizer.get() ? 14f : 0f);
		boolean shadow = shadow(ctx);
		view.clipped(g, view.title, tx, 9f, textW, HudContext.TEXT, shadow, marquee.get());
		view.clipped(g, view.artist, tx, 21f, textW + (visualizer.get() ? 14f : 0f), HudContext.TEXT_DIM, shadow, marquee.get());
		if (visualizer.get()) {
			view.bars(g, width - 16f, 16f, 8f, 2f, 1.5f, 4, accent, ctx.dt, ctx.time);
		}
		if (progress.get()) {
			view.progressBar(g, tx, height - 10f, width - tx - 7f, 2.5f, accent, false);
		}
	}

	private void ambient(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		float r = Math.max(6f, Math.min((float) ctx.theme.radius.get() + 2f, 14f));
		TextureSetup glow = AlbumArt.ambient();
		RenderUtils.shadow(g, 0f, 2f, width, height, r, 6f, 0x66000000);
		if (glow != null) {
			float aspect = height / width;
			ImageDraw.rounded(g, glow, 0f, 0f, width, height, r, 0xFFFFFFFF, 0f, 0.5f - aspect * 0.5f, 1f, 0.5f + aspect * 0.5f);
			RenderUtils.gradient(g, 0f, 0f, width, height, r, 0x14000000, 0x73000000);
		} else {
			RenderUtils.gradient(g, 0f, 0f, width, height, r, ColorUtil.lerpRgb(accent, 0xFF111827, 0.62f), 0xF20B0F1A);
		}
		RenderUtils.stroke(g, 0f, 0f, width, height, r, HighDpiEngine.px(), 0x40FFFFFF, 0x0DFFFFFF);

		float pad = 7f;
		float art = height - pad * 2f;
		RenderUtils.shadow(g, pad, pad + 1.5f, art, art, 5f, 4f, 0x80000000);
		view.art(g, pad, pad, art, 5f, accent);
		float tx = pad + art + 9f;
		float textW = width - tx - pad;
		view.clipped(g, view.title, tx, pad + 5f, textW, HudContext.TEXT, true, marquee.get());
		view.clipped(g, view.artist, tx, pad + 17f, textW, 0xD9FFFFFF, true, marquee.get());
		if (progress.get()) {
			float by = height - pad - 12f;
			view.progressBar(g, tx, by, textW, 2.5f, 0xF2FFFFFF, true);
			if (times.get()) {
				ctx.text(g, view.elapsed, tx, by + 6f, 0xB3FFFFFF);
				ctx.text(g, view.remaining, width - pad - view.remaining.width(), by + 6f, 0xB3FFFFFF);
			}
		}
		if (visualizer.get() && !progress.get()) {
			view.bars(g, width - pad - 12f, height - pad - 2f, 10f, 2f, 1.5f, 4, 0xE6FFFFFF, ctx.dt, ctx.time);
		}
	}

	private void minimal(GuiGraphicsExtractor g, HudContext ctx, int accent) {
		ctx.background(g, this, width, height);
		float cy = 8f;
		float x = 8f;
		IconFont.draw(g, MusicView.ICON_MUSIC, x + 6f, cy, 11f, accent, 0x40000000);
		x += 12f + 5f;
		boolean shadow = true;
		float avail = width - x - 8f;
		float titleW = Math.min(view.title.width(), avail * 0.65f);
		view.clipped(g, view.title, x, cy - view.title.capHeight() * 0.5f, titleW, HudContext.TEXT, shadow, marquee.get());
		float ax = x + titleW + 6f;
		if (ax < width - 14f) {
			RenderUtils.circle(g, ax - 3f, cy, 0.9f, HudContext.TEXT_DIM);
			view.clipped(g, view.artist, ax + 1f, cy - view.artist.capHeight() * 0.5f, width - ax - 9f, HudContext.TEXT_DIM, shadow, marquee.get());
		}
		if (progress.get() && view.track.active) {
			RenderUtils.fill(g, x, 15.5f, avail, 1.5f, 0.75f, 0x33FFFFFF);
			RenderUtils.fill(g, x, 15.5f, avail * Math.clamp(view.progress, 0f, 1f), 1.5f, 0.75f, accent);
		}
	}
}
