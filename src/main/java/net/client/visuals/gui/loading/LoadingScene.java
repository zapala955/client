package net.client.visuals.gui.loading;

import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.GpuImage;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.BackgroundEngine;
import net.client.visuals.render.background.ImageDraw;
import net.client.visuals.render.loading.BootArt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

/**
 * The card of every "please wait" screen – connecting, loading terrain, saving, reading world data:
 * frosted wallpaper behind a smoked-glass card with the client's mark (or the server's icon, or a
 * live map of the chunks being generated), a title, a status line and a capsule progress bar or a
 * spinner, plus the screen's cancel button if it has one.
 *
 * <p>It's one shared scene, so consecutive steps (Connecting → Loading terrain → in game) morph
 * the same card – size, texts and progress glide – instead of popping a new screen each time.</p>
 */
public final class LoadingScene {
	public static final LoadingScene INSTANCE = new LoadingScene();
	/** The server being joined (captured in {@code ConnectScreen.startConnecting}), for its name and icon. */
	public static @Nullable ServerData connecting;

	private static final float W = 268f;
	private static final float PAD = 20f;
	private static final float TILE = 46f;
	private static final float BAR_H = 5f;
	private static final long CONTINUITY_NANOS = 400_000_000L;
	private static final Identifier SERVER_ICON = LiquidClient.id("dynamic/loading_server_icon");
	/** Chunk status colours by status index: not started → deep blue → accent → white (full). */
	private static final int[] STATUS_COLORS = buildStatusColors();

	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth appear = new Smooth(0f, 7f);
	private final Smooth height = new Smooth(0f, 12f);
	private final Smooth progress = new Smooth(0f, 5f);
	private final Smooth barShown = new Smooth(0f, 9f);
	private final Smooth textIn = new Smooth(1f, 9f);
	private final Smooth buttonHover = new Smooth(0f, Motion.HOVER);
	private final TextLabel title = new TextLabel("", LiquidText.TextStyle.HEADLINE);
	private final TextLabel status = new TextLabel("", LiquidText.TextStyle.BODY);
	private final TextLabel percent = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel buttonLabel = new TextLabel("", LiquidText.TextStyle.LABEL);
	private long lastFrameNanos;
	private @Nullable String titleText;
	private @Nullable Component titleSource;
	private @Nullable Component statusSource;
	private @Nullable Component buttonSource;
	private int shownPercent = -1;
	private byte @Nullable [] iconBytes;
	private @Nullable TextureSetup icon;

	// Per-frame content, set by the screen before extract().
	private float wantedProgress = -1f;
	private @Nullable ChunkLoadStatusView chunks;
	private @Nullable AbstractWidget button;
	private boolean hasServerIcon;

	private LoadingScene() {
	}

	// ---- Content -------------------------------------------------------------------------------

	/** Title from a component; resolved to text only when the component changes. */
	public LoadingScene title(Component text) {
		if (text != titleSource) {
			titleSource = text;
			title(text.getString());
		}
		return this;
	}

	public LoadingScene title(String text) {
		if (!text.equals(titleText)) {
			if (titleText != null) {
				textIn.snap(0f);
			}
			titleText = text;
			title.setText(text);
		}
		return this;
	}

	public LoadingScene status(@Nullable Component text) {
		if (text != statusSource) {
			statusSource = text;
			status.setText(text == null ? "" : text.getString());
		}
		return this;
	}

	/** 0..1, or negative for an indeterminate wait (spinner). */
	public LoadingScene progress(float value) {
		wantedProgress = value;
		return this;
	}

	public LoadingScene chunks(@Nullable ChunkLoadStatusView view) {
		chunks = view;
		return this;
	}

	public LoadingScene button(@Nullable AbstractWidget widget) {
		button = widget;
		return this;
	}

	/** The server's favicon (PNG bytes from the server list), or null for the client's mark. */
	public LoadingScene serverIcon(byte @Nullable [] bytes) {
		if (bytes != iconBytes) {
			if (iconBytes != null) {
				Minecraft.getInstance().getTextureManager().release(SERVER_ICON);
			}
			iconBytes = bytes;
			icon = null;
			if (bytes != null) {
				try (NativeImage image = NativeImage.read(bytes)) {
					GpuImage texture = new GpuImage("Liquid server icon", image);
					Minecraft.getInstance().getTextureManager().register(SERVER_ICON, texture);
					icon = TextureSetup.singleTexture(texture.view(), texture.linearSampler());
				} catch (Exception e) {
					LiquidClient.LOGGER.debug("Unreadable server icon", e);
				}
			}
		}
		hasServerIcon = icon != null;
		return this;
	}

	// ---- Rendering -----------------------------------------------------------------------------

	/** Frosted wallpaper, darkened. Call from the screen's background pass. */
	public void background(GuiGraphicsExtractor g, int width, int height) {
		long now = System.nanoTime();
		if (now - lastFrameNanos > CONTINUITY_NANOS) {
			timer.reset();
			appear.snap(0f);
			this.height.snap(0f);
			progress.snap(Math.max(0f, wantedProgress));
			barShown.snap(wantedProgress >= 0f ? 1f : 0f);
			textIn.snap(1f);
		}
		lastFrameNanos = now;
		UiFrame.begin(timer);
		BackgroundEngine.INSTANCE.extract(g, width, height);
		BackgroundEngine.INSTANCE.frostOverlay(g, 1f);
		RenderUtils.gradient(g, 0f, 0f, width, height, 0f, 0x4D000000, 0x8C000000);
	}

	public void extract(GuiGraphicsExtractor g, int width, int height, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float a = appear.target(1f).update(dt);
		float eased = 1f - (1f - a) * (1f - a) * (1f - a);
		boolean determinate = wantedProgress >= 0f;
		float shownBar = barShown.target(determinate).update(dt);
		float value = progress.target(Math.clamp(wantedProgress, 0f, 1f)).update(dt);
		float textAlpha = textIn.target(1f).update(dt);

		// Layout (card-relative).
		float visual = visualSize();
		float bodyH = visual + 14f + title.capHeight() + (status.text().isEmpty() ? 0f : 7f + status.capHeight())
				+ 16f + 16f + (button != null ? 16f + Ui.CONTROL_H : 0f);
		float targetH = PAD * 2f + bodyH;
		if (this.height.get() < 1f) {
			this.height.snap(targetH);
		}
		float h = this.height.target(targetH).update(dt);
		float x = HighDpiEngine.snap((width - W) * 0.5f);
		float y = HighDpiEngine.snap((height - h) * 0.5f);

		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		float s = 0.96f + 0.04f * eased;
		pose.translate(width * 0.5f, height * 0.5f + (1f - eased) * 8f);
		pose.scale(s, s);
		pose.translate(-width * 0.5f, -height * 0.5f);
		RenderUtils.pushAlpha(Math.min(1f, a * 1.3f));

		card(g, x, y, W, h);
		float cy = y + PAD;
		float cx = x + W * 0.5f;
		extractVisual(g, cx, cy, visual);
		cy += visual + 14f;

		RenderUtils.pushAlpha(textAlpha);
		title.maxWidth(W - PAD * 2f);
		LiquidText.drawCentered(g, title, cx, cy, Ui.TEXT, false);
		cy += title.capHeight();
		if (!status.text().isEmpty()) {
			cy += 7f;
			status.maxWidth(W - PAD * 2f);
			LiquidText.drawCentered(g, status, cx, cy, Ui.TEXT_2, false);
			cy += status.capHeight();
		}
		RenderUtils.popAlpha();
		cy += 16f;

		// Progress: capsule bar (with percent) cross-fading with a spinner.
		float barW = W - PAD * 2f - 24f;
		float bx = cx - barW * 0.5f;
		if (shownBar > 0.01f) {
			RenderUtils.pushAlpha(shownBar);
			RenderUtils.fill(g, bx, cy, barW, BAR_H, BAR_H * 0.5f, 0x29FFFFFF);
			RenderUtils.fill(g, bx, cy, Math.max(BAR_H, barW * value), BAR_H, BAR_H * 0.5f, 0xF2FFFFFF);
			int pct = Math.round(value * 100f);
			if (pct != shownPercent) {
				shownPercent = pct;
				percent.setText(pct + "%");
			}
			LiquidText.drawCentered(g, percent, cx, cy + BAR_H + 6f, Ui.TEXT_3, false);
			RenderUtils.popAlpha();
		}
		if (shownBar < 0.99f) {
			RenderUtils.pushAlpha(1f - shownBar);
			Ui.spinner(g, cx, cy + 6f, 7f, 1.6f, 0xE6FFFFFF);
			RenderUtils.popAlpha();
		}
		cy += 16f;

		if (button != null) {
			cy += 16f;
			extractButton(g, button, cx, cy, mouseX, mouseY);
		}
		RenderUtils.popAlpha();
		pose.popMatrix();
	}

	private float visualSize() {
		if (chunks != null) {
			return mapCell(chunks) * (chunks.radius() * 2 + 1) + 16f;
		}
		return TILE;
	}

	private static float mapCell(ChunkLoadStatusView view) {
		int diameter = view.radius() * 2 + 1;
		return Math.clamp(96f / diameter, 2f, 5f);
	}

	/** Smoked glass over the frosted wallpaper (the media player's material). */
	private static void card(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		float r = 22f;
		RenderUtils.shadow(g, x, y + 14f, w, h, r, 30f, 0x8C000000);
		RenderUtils.shadow(g, x, y + 2f, w, h, r, 6f, 0x33000000);
		boolean frosted = BackgroundEngine.INSTANCE.frost(g, x, y, w, h, r);
		RenderUtils.fill(g, x, y, w, h, r, frosted ? 0x9E0C0D11 : 0xE00C0D11);
		RenderUtils.light(g, x, y, w, Math.min(h, 80f), r, 0x14FFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), 0x40FFFFFF, 0x0FFFFFFF);
	}

	private void extractVisual(GuiGraphicsExtractor g, float cx, float top, float size) {
		ChunkLoadStatusView view = chunks;
		if (view != null) {
			extractMap(g, view, cx, top, size);
			return;
		}
		float x = cx - TILE * 0.5f;
		RenderUtils.shadow(g, x, top + 3f, TILE, TILE, TILE * 0.28f, 8f, 0x66000000);
		if (hasServerIcon && icon != null) {
			ImageDraw.rounded(g, icon, x, top, TILE, TILE, TILE * 0.24f, 0xFFFFFFFF);
			RenderUtils.stroke(g, x, top, TILE, TILE, TILE * 0.24f, HighDpiEngine.px(), 0x33FFFFFF, 0x0DFFFFFF);
			return;
		}
		appTile(g, x, top, TILE);
	}

	/** The app icon, drawn live: gradient squircle and the white drop at exact device size. */
	public static void appTile(GuiGraphicsExtractor g, float x, float y, float size) {
		float r = size * 0.28f;
		RenderUtils.gradient(g, x, y, size, size, r, 0xFF3D8BFF, 0xFF8E5CF7);
		RenderUtils.light(g, x, y, size, size * 0.5f, r, 0x26FFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, size, size, r, HighDpiEngine.px(), 0x4DFFFFFF, 0x14FFFFFF);
		int scale = HighDpiEngine.scale();
		int px = Math.round(size * 0.56f * scale);
		if (BootArt.drop(px)) {
			int dx = Math.round((x + size * 0.5f) * scale) - px / 2;
			int dy = Math.round((y + size * 0.5f) * scale) - px / 2;
			int alpha = Math.round(RenderUtils.alpha() * 255f);
			Matrix3x2fStack pose = g.pose();
			pose.pushMatrix();
			pose.scale(1f / scale, 1f / scale);
			g.blit(RenderPipelines.GUI_TEXTURED, BootArt.DROP, dx, dy, 0f, 0f, px, px, px, px, alpha << 24 | 0xF4F6FF);
			pose.popMatrix();
		}
	}

	/** Live chunk map of the generating spawn area, as small rounded cells in a recessed well. */
	private static void extractMap(GuiGraphicsExtractor g, ChunkLoadStatusView view, float cx, float top, float size) {
		float x = cx - size * 0.5f;
		RenderUtils.fill(g, x, top, size, size, 12f, 0x40000000);
		RenderUtils.stroke(g, x, top, size, size, 12f, HighDpiEngine.px(), 0x0DFFFFFF, 0x1FFFFFFF);
		int diameter = view.radius() * 2 + 1;
		float cell = mapCell(view);
		float dot = Math.max(1f, cell - Math.max(0.5f, cell * 0.22f));
		float ox = x + 8f;
		float oy = top + 8f;
		for (int i = 0; i < diameter; i++) {
			for (int j = 0; j < diameter; j++) {
				ChunkStatus status = view.get(i, j);
				int color = status == null ? 0x14FFFFFF : STATUS_COLORS[Math.min(STATUS_COLORS.length - 1, status.getIndex())];
				RenderUtils.fill(g, ox + i * cell, oy + j * cell, dot, dot, dot * 0.3f, color);
			}
		}
	}

	private static int[] buildStatusColors() {
		int count = ChunkStatus.FULL.getIndex() + 1;
		int[] colors = new int[count];
		for (int i = 0; i < count; i++) {
			float t = count == 1 ? 1f : i / (float) (count - 1);
			int c;
			if (t < 0.45f) {
				c = ColorUtil.lerp(0x2E3A5BA8, 0xFF2456B8, t / 0.45f);
			} else if (t < 0.85f) {
				c = ColorUtil.lerp(0xFF2456B8, Ui.ACCENT, (t - 0.45f) / 0.4f);
			} else {
				c = ColorUtil.lerp(Ui.ACCENT, 0xFFEAF3FF, (t - 0.85f) / 0.15f);
			}
			colors[i] = c;
		}
		return colors;
	}

	/** Positions the screen's vanilla button under the card and draws it as a glass pill. */
	private void extractButton(GuiGraphicsExtractor g, AbstractWidget widget, float cx, float y, int mouseX, int mouseY) {
		Component message = widget.getMessage();
		if (message != buttonSource) {
			buttonSource = message;
			buttonLabel.setText(message.getString());
		}
		int bw = Math.round(Math.max(110f, buttonLabel.width() + 36f));
		int bh = Math.round(Ui.CONTROL_H);
		int bx = Math.round(cx - bw * 0.5f);
		int by = Math.round(y);
		widget.setRectangle(bw, bh, bx, by);
		boolean over = widget.active && mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
		float hv = buttonHover.target(over || widget.isFocused()).update(UiFrame.dt);
		float r = bh * 0.5f;
		RenderUtils.pushAlpha(widget.active ? 1f : 0.4f);
		RenderUtils.fill(g, bx, by, bw, bh, r, ColorUtil.lerp(Ui.FILL_CONTROL, Ui.FILL_CONTROL_HOVER, hv));
		RenderUtils.stroke(g, bx, by, bw, bh, r, HighDpiEngine.px(), 0x29FFFFFF, 0x0AFFFFFF);
		LiquidText.drawCentered(g, buttonLabel, bx + bw * 0.5f, by + (bh - buttonLabel.capHeight()) * 0.5f, Ui.TEXT, false);
		RenderUtils.popAlpha();
	}

	/** Clears per-screen content (call at the start of each screen's frame). */
	public LoadingScene reset() {
		wantedProgress = -1f;
		chunks = null;
		button = null;
		hasServerIcon = false;
		return this;
	}
}
