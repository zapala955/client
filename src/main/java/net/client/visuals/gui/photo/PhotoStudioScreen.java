package net.client.visuals.gui.photo;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import net.client.visuals.gui.LiquidScreen;
import net.client.visuals.gui.component.SettingComponents;
import net.client.visuals.gui.component.UiComponent;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.ScrollArea;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.module.player.PhotoModeModule;
import net.client.visuals.module.setting.Setting;
import net.client.visuals.render.BlurPanel;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.DeltaTimer;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.camera.PhotoMode;
import net.client.visuals.render.post.PhotoCapture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.joml.Matrix3x2fStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Photo Mode's studio (Tab): every camera setting in a glass sidebar, the live view beside it.
 *
 * <p>The world isn't dimmed or blurred behind it – it's what you're framing. Click anywhere in the
 * view to focus there (tap to focus), scroll over it to zoom; the viewfinder's lens chips, shutter
 * and thumbnail (the latter opens the screenshots folder) take clicks too. Sections: Lens, Focus,
 * Exposure, Color (filters as swatches), Scene, Frame, Capture. Tab, Esc or Done return to the
 * camera; F2 or Enter take the photo from here.</p>
 */
public final class PhotoStudioScreen extends Screen implements LiquidScreen {
	public static final float PANEL_W = 236f;
	public static final float MARGIN = 12f;
	private static final float PAD = 12f;
	private static final float HEADER_H = 46f;
	private static final float FOOTER_H = 42f;
	private static final float GROUP_PAD = 10f;
	private static final float ROW_GAP = 9f;
	private static final float SECTION_GAP = 14f;
	private static final int ACCENT = 0xFFFFD60A;

	private record Section(TextLabel title, List<UiComponent> rows) {
	}

	private final DeltaTimer timer = new DeltaTimer();
	private final Smooth open = new Smooth(0f, 11f);
	private final ScrollArea scroll = new ScrollArea();
	private final List<Section> sections = new ArrayList<>();
	private final TextLabel title = new TextLabel("", LiquidText.TextStyle.TITLE);
	private final TextLabel subtitle = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final TextLabel freezeNote = new TextLabel("", LiquidText.TextStyle.CAPTION);
	private final PillButton shoot;
	private final PillButton done;
	private float panelX;
	private float panelY;
	private float panelH;
	private boolean draggingRow;
	private boolean opened;

	public PhotoStudioScreen() {
		super(Component.translatable("liquidclient.photo.studio"));
		PhotoModeModule m = PhotoMode.module();
		title.setText(I18n.get("liquidclient.photo.studio"));
		subtitle.setText(I18n.get("liquidclient.photo.studio_hint"));
		freezeNote.setText(I18n.get("liquidclient.photo.freeze_note"));
		section("liquidclient.photo.section.lens",
				new LogSliderComponent(m.focal, ACCENT, 14, 24, 35, 50, 85, 135),
				row(m.speed), row(m.smoothing), row(m.range));
		section("liquidclient.photo.section.focus",
				row(m.depth), new LogSliderComponent(m.aperture, ACCENT, 1.4, 2, 2.8, 4, 5.6, 8, 11, 16),
				row(m.focusMode), new LogSliderComponent(m.focusDistance, ACCENT, 0.5, 1, 2, 5, 10, 20, 50, 100), row(m.bokeh));
		section("liquidclient.photo.section.exposure", row(m.exposure), row(m.contrast));
		section("liquidclient.photo.section.color",
				new FilterGridComponent(m.filter), row(m.filterStrength), row(m.warmth), row(m.saturation), row(m.vignette), row(m.grain));
		section("liquidclient.photo.section.scene", row(m.freeze), new NoteComponent(freezeNote), row(m.customTime), row(m.time),
				row(m.weather), row(m.hidePlayer));
		section("liquidclient.photo.section.frame", row(m.aspect), row(m.grid), row(m.level), row(m.histogram));
		section("liquidclient.photo.section.capture", row(m.quality), row(m.sounds),
				new PillButton(I18n.get("liquidclient.photo.open_folder"), 0xE0D7, PillButton.Kind.SECONDARY, PhotoStudioScreen::openFolder));
		shoot = new PillButton(I18n.get("liquidclient.photo.take"), 0xE064, PillButton.Kind.PRIMARY, PhotoMode::shoot);
		done = new PillButton(I18n.get("gui.done"), 0, PillButton.Kind.SECONDARY, this::onClose);
	}

	private UiComponent row(Setting setting) {
		return SettingComponents.create(setting, ACCENT);
	}

	private void section(String key, UiComponent... rows) {
		List<UiComponent> list = new ArrayList<>(rows.length);
		for (UiComponent row : rows) {
			if (row != null) {
				list.add(row);
			}
		}
		sections.add(new Section(Ui.sectionLabel(I18n.get(key)), list));
	}

	private static void openFolder() {
		File folder = PhotoCapture.lastFile() != null ? PhotoCapture.lastFile().getParentFile()
				: new File(net.minecraft.client.Minecraft.getInstance().gameDirectory, "screenshots");
		folder.mkdirs();
		Blaze3D.openPath(folder.toPath());
	}

	@Override
	public boolean isPauseScreen() {
		return false; // the world freezes (or not) by Photo Mode's own rule
	}

	@Override
	protected void init() {
		if (!opened) {
			opened = true; // init also runs on resize
			UiSounds.open();
		}
	}

	@Override
	public void removed() {
		// Closing a screen resumes every sound; a frozen world stays quiet.
		if (PhotoMode.freezesWorld()) {
			minecraft.execute(() -> minecraft.getSoundManager().pauseAllExcept(SoundSource.MUSIC, SoundSource.UI));
		}
	}

	@Override
	public void onClose() {
		UiSounds.close();
		minecraft.gui.setScreen(null);
	}

	// ---- Rendering ----------------------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		// Nothing: the live view is the background.
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		UiFrame.begin(timer);
		if (!PhotoMode.active()) {
			minecraft.execute(() -> {
				if (minecraft.gui.screen() == this) {
					minecraft.gui.setScreen(null);
				}
			});
			return;
		}
		float t = open.target(1f).update(UiFrame.dt);
		float w = PANEL_W;
		float x = width - w - MARGIN + (1f - t) * 30f;
		float y = MARGIN;
		float h = height - MARGIN * 2f;
		panelX = x;
		panelY = y;
		panelH = h;
		RenderUtils.pushAlpha(Math.min(1f, t * 1.4f));
		panel(g, x, y, w, h);

		// Header: a camera tile, the title, a hint.
		float hx = x + PAD;
		float hy = y + PAD;
		RenderUtils.gradient(g, hx, hy, 24f, 24f, 7.5f, 0xFFFFE066, 0xFFFFB800);
		IconFont.draw(g, 0xE064, hx + 12f, hy + 12f, 13f, 0xFF1C1500, 0);
		LiquidText.draw(g, title, hx + 32f, hy + 3f, 0xFFF5F5F7, false);
		subtitle.maxWidth(w - PAD * 2f - 32f);
		LiquidText.draw(g, subtitle, hx + 32f, hy + 16f, 0x99EBEBF5, false);
		Ui.separator(g, x + PAD, y + HEADER_H, w - PAD * 2f);

		// Settings, scrolling.
		float top = y + HEADER_H + 1f;
		float viewH = h - HEADER_H - FOOTER_H - 1f;
		float innerW = w - PAD * 2f;
		float content = layoutHeight(innerW);
		float offset = scroll.begin(x, top, w, viewH, content);
		scroll.scissor(g);
		float cy = top + PAD - offset;
		for (Section section : sections) {
			LiquidText.draw(g, section.title(), x + PAD + 4f, cy, 0x80EBEBF5, false);
			cy += 13f;
			float groupH = groupHeight(section, innerW);
			Ui.group(g, x + PAD, cy, innerW, groupH);
			float ry = cy + GROUP_PAD;
			for (UiComponent row : section.rows()) {
				float rw = innerW - GROUP_PAD * 2f;
				float rh = rowHeight(row, rw);
				if (rh <= 0f) {
					row.setBounds(x + PAD + GROUP_PAD, -10000f, rw, 0f);
					continue;
				}
				row.setBounds(x + PAD + GROUP_PAD, ry, rw, rh);
				if (ry + rh >= top - 4f && ry <= top + viewH + 4f) {
					row.extract(g, mouseX, mouseY);
				}
				ry += rh + ROW_GAP;
			}
			cy += groupH + SECTION_GAP;
		}
		g.disableScissor();
		scroll.scrollbar(g, mouseX, mouseY);

		// Footer: Take Photo, Done.
		float fy = y + h - FOOTER_H;
		Ui.separator(g, x + PAD, fy, w - PAD * 2f);
		float bh = 24f;
		float doneW = Math.max(56f, done.naturalWidth());
		done.setBounds(x + w - PAD - doneW, fy + (FOOTER_H - bh) * 0.5f, doneW, bh);
		shoot.setBounds(x + PAD, fy + (FOOTER_H - bh) * 0.5f, w - PAD * 3f - doneW, bh);
		shoot.extract(g, mouseX, mouseY);
		done.extract(g, mouseX, mouseY);
		RenderUtils.popAlpha();

		// Where a click in the view would focus.
		if (mouseX < x - 4f && !PhotoOverlay.overShutter(mouseX, mouseY) && PhotoOverlay.lensAt(mouseX, mouseY) < 0
				&& !PhotoOverlay.overThumbnail(mouseX, mouseY)) {
			focusCursor(g, mouseX, mouseY);
		}
		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	private void panel(GuiGraphicsExtractor g, float x, float y, float w, float h) {
		float r = Ui.WINDOW_RADIUS;
		RenderUtils.shadow(g, x, y + 10f, w, h, r, 24f, 0x73000000);
		if (!BlurPanel.draw(g, x, y, w, h, r, 0xA60C0D11, 16f * HighDpiEngine.scale(), 1.25f, 0f)) {
			RenderUtils.fill(g, x, y, w, h, r, 0xE60C0D11);
		}
		RenderUtils.light(g, x, y, w, Math.min(h, 70f), r, 0x12FFFFFF, 0x00FFFFFF);
		RenderUtils.stroke(g, x, y, w, h, r, HighDpiEngine.px(), 0x40FFFFFF, 0x10FFFFFF);
	}

	/** A small focus square following the cursor over the view. */
	private static void focusCursor(GuiGraphicsExtractor g, float mx, float my) {
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(mx, my);
		float s = 9f;
		int color = 0xCCFFD60A;
		RenderUtils.line(g, -s, -s, -s + 4f, -s, 1.25f, color);
		RenderUtils.line(g, -s, -s, -s, -s + 4f, 1.25f, color);
		RenderUtils.line(g, s, -s, s - 4f, -s, 1.25f, color);
		RenderUtils.line(g, s, -s, s, -s + 4f, 1.25f, color);
		RenderUtils.line(g, -s, s, -s + 4f, s, 1.25f, color);
		RenderUtils.line(g, -s, s, -s, s - 4f, 1.25f, color);
		RenderUtils.line(g, s, s, s - 4f, s, 1.25f, color);
		RenderUtils.line(g, s, s, s, s - 4f, 1.25f, color);
		pose.popMatrix();
	}

	private float rowHeight(UiComponent row, float width) {
		if (row instanceof NoteComponent && PhotoMode.canFreeze()) {
			return 0f; // only shown where freezing can't work
		}
		return row.preferredHeight(width);
	}

	private float groupHeight(Section section, float innerW) {
		float rw = innerW - GROUP_PAD * 2f;
		float hgt = GROUP_PAD * 2f;
		int shown = 0;
		for (UiComponent row : section.rows()) {
			float rh = rowHeight(row, rw);
			if (rh > 0f) {
				hgt += rh;
				shown++;
			}
		}
		return hgt + Math.max(0, shown - 1) * ROW_GAP;
	}

	private float layoutHeight(float innerW) {
		float total = PAD * 2f;
		for (Section section : sections) {
			total += 13f + groupHeight(section, innerW) + SECTION_GAP;
		}
		return total - SECTION_GAP;
	}

	private boolean inPanel(double mx, double my) {
		return mx >= panelX && mx < panelX + PANEL_W && my >= panelY && my < panelY + panelH;
	}

	// ---- Input --------------------------------------------------------------------------------

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = event.key();
		if (event.isEscape() || key == InputConstants.KEY_TAB) {
			onClose();
			return true;
		}
		if (minecraft.options.keyScreenshot.matches(event) || key == InputConstants.KEY_RETURN) {
			PhotoMode.shoot();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		if (inPanel(mx, my)) {
			if (shoot.mouseClicked(mx, my, button) || done.mouseClicked(mx, my, button)) {
				return true;
			}
			if (scroll.contains(mx, my)) {
				for (Section section : sections) {
					for (UiComponent row : section.rows()) {
						if (row.mouseClicked(mx, my, button)) {
							draggingRow = true;
							return true;
						}
					}
				}
			}
			return true;
		}
		if (button != InputConstants.MOUSE_BUTTON_LEFT) {
			if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
				PhotoMode.shoot();
			}
			return true;
		}
		if (PhotoOverlay.overShutter(mx, my)) {
			PhotoMode.shoot();
			return true;
		}
		int lens = PhotoOverlay.lensAt(mx, my);
		if (lens >= 0) {
			PhotoMode.setFocal(PhotoMode.LENSES[lens]);
			UiSounds.tick(lens / (PhotoMode.LENSES.length - 1.0));
			return true;
		}
		if (PhotoOverlay.overThumbnail(mx, my)) {
			openFolder();
			return true;
		}
		// Tap to focus.
		PhotoMode.focusAt((float) (mx / Math.max(1, width)), (float) (my / Math.max(1, height)));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		shoot.mouseReleased(mx, my, button);
		done.mouseReleased(mx, my, button);
		if (draggingRow) {
			draggingRow = false;
			for (Section section : sections) {
				for (UiComponent row : section.rows()) {
					row.mouseReleased(mx, my, button);
				}
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (draggingRow) {
			for (Section section : sections) {
				for (UiComponent row : section.rows()) {
					if (row.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) {
						return true;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (inPanel(mouseX, mouseY)) {
			scroll.scroll(mouseX, mouseY, scrollY);
		} else {
			PhotoMode.scroll(scrollY);
		}
		return true;
	}

	/** A short dim line of text inside a section (shown only when it applies). */
	private static final class NoteComponent extends UiComponent {
		private final TextLabel text;

		NoteComponent(TextLabel text) {
			this.text = text;
		}

		@Override
		public float preferredHeight(float width) {
			return 9f;
		}

		@Override
		public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			text.maxWidth(width);
			LiquidText.draw(g, text, x, y, ColorUtil.fade(0xFFFF9F0A, 0.9f), false);
		}
	}
}
