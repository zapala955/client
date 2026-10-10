package ru.white.screen;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.white.utils.ClientUtil;
import ru.white.utils.misc.Tq;
import ru.white.ui.DraggableWidget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.alt.AltManager;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.et;
import ru.white.utils.render.us;
import ru.white.utils.render.vr;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class MainMenuScreen extends Screen implements IMinecraft {
   private static int prevBgIndex = 0;
   private static final Animation bgFadeAnim = new Animation();
   private final Animation transition;
   private static final Path MENU_TYPE_FILE = Path.of("rose/client1_21_11/mainmenu_type.txt");
   public static final Identifier[] BACKGROUND_TEXTURES = new Identifier[]{
      Identifier.method_60655("godweer", "images/ui/title/title.png"),
      Identifier.method_60655("godweer", "images/ui/title/bg1.png"),
      Identifier.method_60655("godweer", "images/ui/title/bg2.png"),
      Identifier.method_60655("godweer", "images/ui/title/bg3.png")
   };
   private static boolean customMenuEnabled = true;
   private static final Path BG_CONFIG_FILE = Path.of("rose/client1_21_11/mainmenu_bg.txt");
   private static final float QUIT_FADE_SPEED = 0.018F;
   private boolean isTransitioning;
   private co bgModal;
   public static final String[] BACKGROUND_NAMES = new String[]{"Оригинал", "Фон 1", "Фон 2", "Фон 3"};
   private static boolean menuTypeLoaded = false;
   private float fadeInProgress = 0.0F;
   private static final Identifier AVATAR_TEXTURE = Identifier.method_60655(
      "godweer", "images/ui/title/avatar.png"
   );
   private final Animation bgBtnHoverAnim;
   private static final float FADE_IN_SPEED = 0.055F;
   private float parallaxX;
   private static int selectedBgIndex = 0;
   private static final Identifier ROSE_WATERMARK = Identifier.method_60655(
      "client", "textures/rose__logo_watermark.png"
   );
   private float quitFadeProgress;
   private final Animation vanillaBtnHoverAnim;
   double lastMouseY;
   private boolean quitting;
   private float scaleFix;
   private final Animation alphaAnim;
   private static boolean bgLoaded = false;
   private float parallaxY;
   private static final float CARD_HEIGHT = 186.0F;
   private boolean fadingIn = true;
   double lastMouseX;
   private Screen pendingScreen;
   private final List<MainMenuScreen.GodButton> buttons;
   private static final float CARD_WIDTH = 264.0F;

   private void addButton(float x, float y, float w, float h, String label, boolean isRed, Runnable action) {
      MainMenuScreen.GodButton v8 = new MainMenuScreen.GodButton(label, action, isRed);

      v8.x = x;
      v8.y = y;
      v8.baseY = y;
      v8.w = w;

      v8.h = h;
      this.buttons.add(v8);
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.alphaAnim.update();
      this.updateAnimations(1.0F);
      if (this.bgModal != null) {
         this.bgModal.update();
      }

      float v5 = 2.0F;
      float v6 = (float)mc.method_22683().method_4495();
      if (v6 <= 0.001F || Float.isNaN(v6) || Float.isInfinite(v6)) {
         v6 = 1.0F;
      }

      this.scaleFix = v5 / v6;
      if (this.scaleFix <= 0.001F || Float.isNaN(this.scaleFix) || Float.isInfinite(this.scaleFix)) {
         this.scaleFix = 1.0F;
      }

      int v7 = mc.method_22683().method_4486();
      int v8 = mc.method_22683().method_4502();
      if (v7 <= 0) {
         v7 = Math.max(1, mc.method_22683().method_4480());
      }

      if (v8 <= 0) {
         v8 = Math.max(1, mc.method_22683().method_4507());
      }

      float v9 = (float)v7 / this.scaleFix;
      float v10 = (float)v8 / this.scaleFix;
      float v17;
      int result = (v17 = v9 - 10.0F) == 0.0F ? 0 : (v17 < 0.0F ? -1 : 1);

      if (result <= 0 || Float.isNaN(v9)) {
         v9 = 960.0F;
      }

      if (v10 <= 10.0F || Float.isNaN(v10)) {
         v10 = 540.0F;
      }

      float v11 = (float)mouseX;
      float v12 = (float)mouseY;
      if (Float.isNaN(v11) || Float.isInfinite(v11) || v11 < -5000.0F || v11 > 10000.0F) {
         v11 = v9 * this.scaleFix / 2.0F;
      }

      if (Float.isNaN(v12) || Float.isInfinite(v12) || v12 < -5000.0F || v12 > 10000.0F) {
         v12 = v10 * this.scaleFix / 2.0F;
      }

      this.lastMouseX = (double)(v11 / this.scaleFix);

      this.lastMouseY = (double)(v12 / this.scaleFix);
      if (Float.isNaN((float)this.lastMouseX) || Float.isInfinite((float)this.lastMouseX)) {
         this.lastMouseX = (double)(v9 / 2.0F);
      }

      if (Float.isNaN((float)this.lastMouseY) || Float.isInfinite((float)this.lastMouseY)) {
         this.lastMouseY = (double)(v10 / 2.0F);
      }

      this.renderOverlay(context);
      this.renderScreenOverlays(context);
      if (this.isTransitioning) {
         this.transition.update();
         if (this.transition.isFinished()) {
            this.isTransitioning = false;
            if (this.pendingScreen != null) {
               mc.method_1507(this.pendingScreen);
               this.pendingScreen = null;
            }
         }
      }
   }

   private static void saveSelectedBackground(int index) {
      try {
         Path result = BG_CONFIG_FILE.getParent();

         if (result != null && !Files.exists(BG_CONFIG_FILE.getParent())) {
            Files.createDirectories(BG_CONFIG_FILE.getParent());
         }

         Files.writeString(BG_CONFIG_FILE, String.valueOf(index), StandardCharsets.UTF_8);
      } catch (Throwable v4) {
      }
   }

   private void startQuitFade() {
      if (!this.quitting && !this.isTransitioning) {
         this.quitting = true;
         this.quitFadeProgress = 0.0F;
      }
   }

   private boolean handleVanillaSwitchClick(double mx, double my, int button) {
      if (button != 0) {
         return false;
      } else {
         int v6 = mc.method_22683().method_4486();
         if (v6 <= 0) {
            v6 = Math.max(1, mc.method_22683().method_4480());
         }

         float v7 = (float)v6 / this.scaleFix;
         if (v7 <= 10.0F || Float.isNaN(v7)) {
            v7 = 960.0F;
         }

         float v8 = 122.0F;
         float v9 = 24.0F;

         float v10 = v7 - v8 - 16.0F;
         float v11 = 15.0F;
         if (fq.isHovered((float)mx, (float)my, v10, v11, v8, v9)) {
            tq.button();
            setCustomMenuEnabled(false);
            this.startTransition(new TitleScreen());
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean handleBgButtonOpenClick(double mx, double my, int button) {
      if (button != 0) {
         return false;
      } else if (this.bgModal != null && this.bgModal.isOpen()) {
         return false;
      } else {
         int v6 = mc.method_22683().method_4486();
         int v7 = mc.method_22683().method_4502();
         if (v6 <= 0) {
            v6 = Math.max(1, mc.method_22683().method_4480());
         }

         if (v7 <= 0) {
            v7 = Math.max(1, mc.method_22683().method_4507());
         }

         float v8 = (float)v6 / this.scaleFix;
         float v9 = (float)v7 / this.scaleFix;
         float v10 = 132.0F;
         float v11 = 34.0F;
         float v12 = v8 - v10 - 16.0F;
         float v13 = v9 - v11 - 16.0F;
         float result = (float)mx;
         float temp1 = (float)my;

         if (fq.isHovered(result, temp1, v12, v13, v10, v11)) {
            if (this.bgModal != null) {
               this.bgModal.setSelectedIndex(selectedBgIndex);

               this.bgModal.open();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private void renderAccountWidget(float currentAlphaNorm, int textAlpha, int sw, int sh) {
      if (currentAlphaNorm <= 0.01F) {
         
      } else {
         float v5 = 15.0F;
         float v6 = 15.0F;
         float v7 = 24.0F;
         float v8 = 14.0F;
         String v9 = mc.method_1548() != null ? mc.method_1548().method_1676() : "Игрок";
         float v10 = hu.sf_medium.getWidth(v9, 7.0F);
         float v11 = hu.sf_medium.getWidth("Текущий аккаунт", 4.5F);
         float v12 = Math.max(v10, v11);
         float v13 = 5.0F + v8 + 5.0F + v12 + 8.0F;
         vr.blur(v5, v6, v13, v7, 1.0F, 6.0F, up.getColor(12, (int)(currentAlphaNorm * 70.0F)));
         vr.rect(v5, v6, v13, v7, up.getColor(12, (int)(currentAlphaNorm * 40.0F)), 6.0F);
         vr.outline(
            v5,
            v6,
            v13,
            v7,
            0.5F,
            up.getColor(255, 255, 255, (int)((float)textAlpha * 0.08F)),
            6.0F
         );
         float v14 = v5 + 5.0F;
         float v15 = v6 + (v7 - v8) / 2.0F;
         int v16 = up.getColor(255, 255, 255, textAlpha);
         vr.texture(AVATAR_TEXTURE, v14, v15, v8, v8, 1.0F, 7.0F, v16);
         float v17 = v14 + v8 + 5.0F;

         this.drawText(
            "Текущий аккаунт",
            v17,
            v6 + 4.5F,
            hu.sf_medium,
            4.5F,
            up.getColor(150, 150, 155, textAlpha)
         );
         this.drawText(
            v9, v17, v6 + 12.0F, hu.sf_medium, 7.0F, up.getColor(255, 255, 255, textAlpha)
         );
      }
   }

   public static void setCustomMenuEnabled(boolean enabled) {
      customMenuEnabled = enabled;
      menuTypeLoaded = true;

      try {
         if (MENU_TYPE_FILE.getParent() != null && !Files.exists(MENU_TYPE_FILE.getParent())) {
            Files.createDirectories(MENU_TYPE_FILE.getParent());
            
         }

         Files.writeString(MENU_TYPE_FILE, enabled ? "custom" : "vanilla", StandardCharsets.UTF_8);
      } catch (Throwable v3) {
      }
   }

   private void renderBackgroundButton(float currentAlphaNorm, int textAlpha, float sw, float sh) {
      if (!(currentAlphaNorm <= 0.01F)) {
         float v5 = 132.0F;
         float v6 = 34.0F;
         float v7 = sw - v5 - 16.0F;
         float v8 = sh - v6 - 16.0F;
         boolean v9 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v7, v8, v5, v6);
         this.bgBtnHoverAnim.update();
         this.bgBtnHoverAnim.run(v9 ? 1.0 : 0.0, 0.18F, ap.QUAD_OUT);
         float v10 = this.bgBtnHoverAnim.get();
         vr.blur(v7, v8, v5, v6, 1.0F, 8.0F, up.getColor(12, (int)(currentAlphaNorm * 70.0F)));
         int v11 = up.getColor(14, 14, 20, (int)(currentAlphaNorm * 190.0F));
         int v12 = up.getColor(19, 255, 174, (int)(currentAlphaNorm * 28.0F));
         vr.rect(v7, v8, v5, v6, up.interpolate(v11, v12, (double)v10), 8.0F);
         int v13 = up.getColor(255, 255, 255, (int)((float)textAlpha * 0.1F));
         int v14 = up.getColor(19, 255, 174, (int)((float)textAlpha * 0.85F));
         if (v10 > 0.01F) {
            vr.glow(
               v7,
               v8,
               v5,
               v6,
               up.getColor(19, 255, 174, (int)(55.0F * currentAlphaNorm * v10)),
               8.0F,
               5.0F,
               1.0F
            );
         }

         vr.outline(v7, v8, v5, v6, 0.5F + 0.3F * v10, up.interpolate(v13, v14, (double)v10), 8.0F);
         float v15 = 34.0F;
         float v16 = 22.0F;
         float v17 = v7 + 7.0F;
         float v18 = v8 + (v6 - v16) / 2.0F;
         Identifier result = BACKGROUND_TEXTURES[selectedBgIndex];

         vr.texture(result, v17, v18, v15, v16, 1.0F, 4.0F, up.getColor(255, textAlpha), 0.0F, -0.22F);
         vr.texture(
            BACKGROUND_TEXTURES[selectedBgIndex],
            v17 - 0.5F,
            v18 - 0.5F,
            v15 + 1.0F,
            v16 + 1.0F,
            1.0F,
            4.5F,
            up.getColor(19, 255, 174, (int)((float)textAlpha * (0.3F + 0.5F * v10))),
            0.0F,
            -0.22F
         );
         vr.texture(
            BACKGROUND_TEXTURES[selectedBgIndex], v17, v18, v15, v16, 1.0F, 4.0F, up.getColor(255, textAlpha), 0.0F, -0.22F
         );
         float v19 = v17 + v15 + 8.0F;
         hu.sf_bold
            .draw("Обои меню", v19, v8 + 7.5F, 6.8F, up.getColor(245, 245, 250, textAlpha));
         String v20 = BACKGROUND_NAMES[selectedBgIndex];
         int v21 = up.interpolate(
            up.getColor(19, 255, 174, textAlpha),
            up.getColor(255, 255, 255, textAlpha),
            0.2F
         );
         hu.sf_regular.draw(v20, v19, v8 + 17.0F, 5.2F, v21);
      }
   }

   public MainMenuScreen() {
      super(Text.method_43470("MainMenuScreen"));
      this.quitFadeProgress = 0.0F;
      this.quitting = false;
      this.parallaxX = 0.0F;
      this.parallaxY = 0.0F;
      this.buttons = new ArrayList<>();
      this.alphaAnim = new Animation();
      this.transition = new Animation();
      this.vanillaBtnHoverAnim = new Animation();
      this.bgBtnHoverAnim = new Animation();
      this.pendingScreen = null;
      this.isTransitioning = false;
      this.scaleFix = 1.0F;
   }

   private float easeInQuad(float x) {
      return x * x;
   }

   public static boolean isCustomMenuEnabled() {
      if (!menuTypeLoaded) {
         menuTypeLoaded = true;

         try {
            if (Files.exists(MENU_TYPE_FILE)) {

               String v0 = Files.readString(MENU_TYPE_FILE, StandardCharsets.UTF_8).trim();
               if (!v0.equalsIgnoreCase("vanilla") && !v0.equalsIgnoreCase("false")) {
                  customMenuEnabled = true;
               } else {
                  customMenuEnabled = false;
               }
            }
         } catch (Throwable v4) {
            customMenuEnabled = true;
         }
      }

      return customMenuEnabled;
   }

   protected void method_25426() {
      ensureBackgroundLoaded();
      br.playWelcomeSound();
      this.alphaAnim.set(0.0);
      this.alphaAnim.run(1.0, 0.38F, ap.CUBIC_OUT);
      this.fadeInProgress = 0.0F;
      this.fadingIn = true;
      AltManager.get().bootstrap();
      this.quitting = false;
      this.quitFadeProgress = 0.0F;
      this.parallaxX = 0.0F;
      this.parallaxY = 0.0F;
      this.isTransitioning = false;
      this.pendingScreen = null;
      this.transition.set(0.0);
      this.vanillaBtnHoverAnim.set(0.0);
      this.bgBtnHoverAnim.set(0.0);
      this.bgModal = new co(BACKGROUND_TEXTURES, BACKGROUND_NAMES, selectedBgIndex, newIndex -> {
         if (selectedBgIndex != newIndex) {
            prevBgIndex = selectedBgIndex;

            selectedBgIndex = newIndex;
            bgFadeAnim.set(0.0);

            bgFadeAnim.run(1.0, 0.35F, ap.CUBIC_OUT);
            saveSelectedBackground(newIndex);
         }
      });
      this.buttons.clear();

      this.addButton(0.0F, 0.0F, 100.0F, 25.0F, bb.pick("Сетевая игра", "Multiplayer"), false, () -> {
         MultiplayerScreen temp1 = new MultiplayerScreen;

         temp1./* $VF: Unable to resugar constructor */<init>(this);
         this.startTransition(temp1);
         int v1x = 283086300;

         
      });
      this.addButton(
         0.0F,
         0.0F,
         100.0F,
         25.0F,
         bb.pick("Одиночная игра", "Singleplayer"),
         false,
         () -> {
            this.startTransition(new SelectWorldScreen(this));
            int v1x = -1731761341;

            
         }
      );
      this.addButton(
         0.0F,
         0.0F,
         100.0F,
         25.0F,
         bb.pick("Смена аккаунта", "Account Switcher"),
         false,
         () -> {
            ao temp1 = new ao(this);

            this.startTransition(temp1);
         }
      );
      this.addButton(0.0F, 0.0F, 50.0F, 25.0F, bb.pick("Настройки", "Options"), false, () -> {
         this.startTransition(new OptionsScreen(this, mc.field_1690));
         
      });
      String temp5 = bb.pick("Выход", "Quit");

      this.addButton(0.0F, 0.0F, 50.0F, 25.0F, temp5, true, this::startQuitFade);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.bgModal != null && this.bgModal.isOpen()) {
         double v9 = mouseX / (double)this.scaleFix;

         double v11 = mouseY / (double)this.scaleFix;
         if (this.bgModal.mouseScrolled(v9, v11, horizontalAmount, verticalAmount)) {
            return true;
         }
      }

      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   private void updateAnimations(float delta) {
      if (this.fadingIn) {
         this.fadeInProgress += 0.055F * delta;
         if (this.fadeInProgress >= 1.0F) {
            this.fadeInProgress = 1.0F;
            this.fadingIn = false;

            for (MainMenuScreen.GodButton v3 : this.buttons) {
               v3.y = v3.baseY;
               v3.alpha = 1.0F;
            }
         }
      }

      if (this.quitting) {

         this.quitFadeProgress += 0.018F * delta;
         if (this.quitFadeProgress >= 1.0F) {
            this.quitFadeProgress = 1.0F;
            mc.method_1592();
         }
      }
   }

   public boolean method_25421() {

      return false;
   }

   public void renderOverlay(DrawContext context) {
      int v2 = mc.method_22683().method_4486();
      int v3 = mc.method_22683().method_4502();
      if (v2 <= 0) {
         v2 = Math.max(1, mc.method_22683().method_4480());
      }

      if (v3 <= 0) {
         v3 = Math.max(1, mc.method_22683().method_4507());
      }

      float v4 = (float)v2 / this.scaleFix;
      float v5 = (float)v3 / this.scaleFix;
      if (v4 <= 10.0F || Float.isNaN(v4)) {
         v4 = 960.0F;
      }

      if (v5 <= 10.0F || Float.isNaN(v5)) {
         v5 = 540.0F;
      }

      if (context != null) {
         context.method_51448().pushMatrix();
      }

      us.beginOverlay();
      float v6 = (v4 / 2.0F - (float)this.lastMouseX) * 0.045F;
      float v7 = (v5 / 2.0F - (float)this.lastMouseY) * 0.045F;
      if (Float.isNaN(v6) || Float.isInfinite(v6)) {
         v6 = 0.0F;
      }

      if (Float.isNaN(v7) || Float.isInfinite(v7)) {
         v7 = 0.0F;
      }

      v6 = fq.clamp(v6, -v4 * 0.06F, v4 * 0.06F);
      v7 = fq.clamp(v7, -v5 * 0.06F, v5 * 0.06F);
      if (Float.isNaN(this.parallaxX) || Float.isInfinite(this.parallaxX)) {
         this.parallaxX = 0.0F;
      }

      if (Float.isNaN(this.parallaxY) || Float.isInfinite(this.parallaxY)) {
         this.parallaxY = 0.0F;
      }

      this.parallaxX = this.parallaxX + (v6 - this.parallaxX) * 0.06F;
      this.parallaxY = this.parallaxY + (v7 - this.parallaxY) * 0.06F;
      this.parallaxX = fq.clamp(this.parallaxX, -v4 * 0.08F, v4 * 0.08F);
      this.parallaxY = fq.clamp(this.parallaxY, -v5 * 0.08F, v5 * 0.08F);
      float v8 = 1.0F + 0.015F * (float)Math.sin((double)System.currentTimeMillis() / 1800.0);
      if (Float.isNaN(v8) || Float.isInfinite(v8)) {
         v8 = 1.0F;
      }

      float v9 = 1.15F * v8;
      float v10 = v4 * v9;
      float v11 = v5 * v9;
      float v12 = (v4 - v10) / 2.0F + this.parallaxX;
      float v13 = (v5 - v11) / 2.0F + this.parallaxY;
      v12 = Math.min(0.0F, Math.max(v4 - v10, v12));
      v13 = Math.min(0.0F, Math.max(v5 - v11, v13));
      bgFadeAnim.update();
      float v14 = bgFadeAnim.get();
      if (prevBgIndex != selectedBgIndex && v14 < 1.0F) {
         vr.texture(BACKGROUND_TEXTURES[prevBgIndex], v12, v13, v10, v11, 1.0F, 0.0F, up.getColor(255, 255));
         vr.texture(BACKGROUND_TEXTURES[selectedBgIndex], v12, v13, v10, v11, 1.0F, 0.0F, up.getColor(255, (int)(255.0F * v14)));
      } else {
         vr.texture(BACKGROUND_TEXTURES[selectedBgIndex], v12, v13, v10, v11, 1.0F, 0.0F, up.getColor(255, 255));
      }

      et.capture(2);
      int v15 = this.fadingIn ? (int)(this.fadeInProgress * 255.0F) : 255;
      if (this.quitting) {
         v15 = (int)((1.0F - this.quitFadeProgress) * 255.0F);
      }

      if (this.isTransitioning) {
         float v16 = this.transition.get();
         v15 = (int)((float)v15 * (1.0F - v16 * 0.5F));
      }

      v15 = Math.max(0, Math.min(255, v15));
      float v57 = (float)v15 / 255.0F;
      float v17 = this.bgModal != null ? this.bgModal.getOpenProgress() : 0.0F;
      float v18 = Math.max(0.0F, 1.0F - v17);
      float v19 = v57 * v18;
      this.renderAccountWidget(v19, (int)((float)v15 * v18), (int)v4, (int)v5);
      this.renderBackgroundButton(v19, (int)((float)v15 * v18), v4, v5);
      this.renderVanillaSwitchButton(v19, (int)((float)v15 * v18), v4, v5);
      if (v19 > 0.005F) {
         float v20 = this.fadingIn ? (1.0F - this.easeOutCubic(this.fadeInProgress)) * 25.0F : 0.0F;
         if (this.isTransitioning) {
            v20 += this.transition.get() * 14.0F;
         }

         float v21 = v17 * 22.0F;
         float v22 = v20 + v21;
         float v23 = v5 / 2.0F + 20.0F + v22;
         float v24 = 8.0F;
         float v25 = 30.0F;
         float v26 = 0.0F;

         for (MainMenuScreen.GodButton v28 : this.buttons) {
            float v29 = hu.sfsemi.getWidth(v28.label, 7.5F);
            v28.w = v29 + 32.0F;
            v28.h = v25;
            v26 += v28.w;
         }

         v26 += v24 * (float)(this.buttons.size() - 1);
         float v59 = (v4 - v26) / 2.0F;

         for (MainMenuScreen.GodButton v62 : this.buttons) {
            v62.x = v59;
            v62.baseY = v23;
            v59 += v62.w + v24;
         }

         float v61 = v23 - 55.0F;
         hu.sf_bold
            .drawCentered(
               "ROSE SOLUTION",
               v4 / 2.0F,
               v61,
               16.5F,
               up.getColor(255, 255, 255, (int)(v19 * 255.0F))
            );
         String v63 = mc.method_1548() != null ? mc.method_1548().method_1676() : "Player";
         String v49 = bb.pick(
            "Добро пожаловать, ", "Welcome back, "
         );
         String v30 = v49 + v63;
         hu.sf_medium
            .drawCentered(
               v30,
               v4 / 2.0F,
               v61 + 22.0F,
               7.0F,
               up.getColor(180, 185, 195, (int)(v19 * 190.0F))
            );

         for (int v31 = 0; v31 < this.buttons.size(); v31++) {
            MainMenuScreen.GodButton v32 = this.buttons.get(v31);
            v32.alpha = v19;
            v32.update(this.lastMouseX, this.lastMouseY);
            float v33 = fq.clamp(v32.getHovered(), 0.0F, 1.0F);
            v32.y = v32.baseY - v33 * 3.5F;
            float v34 = v32.x;
            float v35 = v32.y;
            float v36 = v32.w;
            float v37 = v32.h;
            int v38 = v32.isRed
               ? up.getColor(35, 12, 14, (int)(v19 * 140.0F))
               : up.getColor(14, 15, 20, (int)(v19 * 140.0F));
            int v39 = v32.isRed
               ? up.getColor(220, 45, 45, (int)(v19 * 60.0F))
               : up.getColor(19, 255, 174, (int)(v19 * 50.0F));
            int v40 = up.interpolate(v38, v39, (double)v33);
            int v41 = up.getColor(255, 255, 255, (int)(v19 * 14.0F));
            int result;
            if (v32.isRed) {

               result = up.getColor(255, 80, 80, (int)(v19 * 180.0F));
            } else {
               result = up.getColor(19, 255, 174, (int)(v19 * 190.0F));
            }

            int v42 = result;
            int v43 = up.interpolate(v41, v42, (double)v33);
            int v44 = up.getColor(225, 228, 235, (int)(v19 * 210.0F));
            int v45 = v32.isRed
               ? up.getColor(255, 130, 130, (int)(v19 * 255.0F))
               : up.getColor(19, 255, 174, (int)(v19 * 255.0F));
            int v46 = up.interpolate(v44, v45, (double)v33);
            vr.blur(v34, v35, v36, v37, 1.0F, 8.0F, up.getColor(10, (int)(v19 * 40.0F)));
            vr.rect(v34, v35, v36, v37, v40, 8.0F);
            if (v33 > 0.01F) {
               int v47 = v32.isRed
                  ? up.getColor(255, 60, 60, (int)(40.0F * v19 * v33))
                  : up.getColor(19, 255, 174, (int)(50.0F * v19 * v33));
               vr.glow(v34, v35, v36, v37, v47, 8.0F, 5.0F, 1.0F);
            }

            vr.outline(v34, v35, v36, v37, 0.5F + 0.3F * v33, v43, 8.0F);
            hu.sfsemi.drawCentered(v32.label, v34 + v36 / 2.0F, v35 + v37 / 2.0F - 3.2F, 7.5F, v46);
         }

         int v64 = up.getColor(19, 255, 174, (int)(v19 * 220.0F));
         vr.rect(16.0F, v5 - 18.0F, 3.5F, 3.5F, v64, 1.75F);
         hu.sf_medium
            .draw(
               "Rose Solution Reborn",
               23.0F,
               v5 - 20.0F,
               6.0F,
               up.getColor(200, 205, 215, (int)(v19 * 180.0F))
            );
         hu.sf_regular
            .draw(
               "1.21.11",
               23.0F + hu.sf_medium.getWidth("Rose Solution Reborn", 6.0F) + 6.0F,
               v5 - 20.0F,
               6.0F,
               up.getColor(19, 255, 174, (int)(v19 * 180.0F))
            );
      }

      if (this.bgModal != null && this.bgModal.isOpen()) {
         this.bgModal.render(v4, v5, this.lastMouseX, this.lastMouseY);
      }

      us.endOverlay();
      if (context != null) {
         context.method_51448().popMatrix();
      }
   }

   private void renderVanillaSwitchButton(float currentAlphaNorm, int textAlpha, float sw, float sh) {
      if (!(currentAlphaNorm <= 0.01F)) {
         float v5 = 122.0F;
         float v6 = 24.0F;
         float v7 = sw - v5 - 16.0F;
         float v8 = 15.0F;
         boolean v9 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v7, v8, v5, v6);
         this.vanillaBtnHoverAnim.update();
         this.vanillaBtnHoverAnim.run(v9 ? 1.0 : 0.0, 0.18F, ap.QUAD_OUT);
         float v10 = fq.clamp(this.vanillaBtnHoverAnim.get(), 0.0F, 1.0F);
         vr.blur(v7, v8, v5, v6, 1.0F, 6.0F, up.getColor(12, (int)(currentAlphaNorm * 70.0F)));
         int result = up.getColor(14, 14, 20, (int)(currentAlphaNorm * 180.0F));

         int v11 = result;
         int v12 = up.getColor(19, 255, 174, (int)(currentAlphaNorm * 35.0F));
         int v13 = up.interpolate(v11, v12, (double)v10);
         vr.rect(v7, v8, v5, v6, v13, 6.0F);
         int v14 = up.getColor(255, 255, 255, (int)((float)textAlpha * 0.1F));
         int v15 = up.getColor(19, 255, 174, (int)((float)textAlpha * 0.85F));
         int v16 = up.interpolate(v14, v15, (double)v10);
         if (v10 > 0.01F) {
            vr.glow(
               v7,
               v8,
               v5,
               v6,
               up.getColor(19, 255, 174, (int)(60.0F * currentAlphaNorm * v10)),
               6.0F,
               5.0F,
               1.0F
            );
         }

         vr.outline(v7, v8, v5, v6, 0.5F + 0.3F * v10, v16, 6.0F);
         int v17 = up.getColor(220, 220, 225, textAlpha);
         int v18 = up.getColor(19, 255, 174, textAlpha);
         int v19 = up.interpolate(v17, v18, (double)v10);
         String v20 = bb.pick("Ванильное меню", "Vanilla Menu");
         hu.sfsemi.drawCentered(v20, v7 + v5 / 2.0F, v8 + v6 / 2.0F - 3.2F, 7.0F, v19);
      }
   }

   private void startTransition(Screen screen) {
      if (!this.isTransitioning && !this.quitting) {

         this.pendingScreen = screen;
         this.isTransitioning = true;
         this.transition.set(0.0);
         this.transition.run(1.0, 0.22, ap.CUBIC_OUT);
      }
   }

   private float easeOutCubic(float x) {
      float temp1 = (float)Math.pow((double)(1.0F - x), 3.0);
      int temp3 = v2 + 1;

      float result = 1.0F - temp1;

      return result;
   }

   private float easeInOutQuad(float x) {
      float result;
      if (x < 0.5F) {
         result = 2.0F * x * x;
      } else {

         float temp1 = -2.0F * x + 2.0F;

         result = 1.0F - (float)Math.pow((double)temp1, 2.0) / 2.0F;
      }

      return result;
   }

   private void closeCheck() {
   }

   private void renderScreenOverlays(DrawContext context) {
      if (this.quitting) {
         int v2 = this.field_22789;
         int v3 = this.field_22790;
         int v4 = (int)(this.easeInOutQuad(this.quitFadeProgress) * 255.0F);

         v4 = Math.min(v9, Math.max(0, v4));
         context.method_25294(0, 0, v2, v3, v4 << 24);
      }
   }

   public boolean method_25404(KeyInput keyInput) {
      if (this.bgModal != null && this.bgModal.isOpen() && this.bgModal.keyPressed(keyInput.comp_4795())) {
         return true;
      } else {

         return super.method_25404(keyInput);
      }
   }

   public boolean method_25422() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return false;
   }

   public boolean method_25403(Click click, double deltaX, double deltaY) {
      if (this.bgModal != null && this.bgModal.isOpen()) {
         double v6 = click.comp_4798() / (double)this.scaleFix;
         double v8 = click.comp_4799() / (double)this.scaleFix;
         co result = this.bgModal;
         int temp3 = click.method_74245();
         double temp5 = (double)this.scaleFix;
         int var10007 = v10 + 1;

         if (result.mouseDragged(v6, v8, temp3, deltaX / temp5, deltaY / (double)this.scaleFix)) {
            return true;
         }
      }

      return super.method_25403(click, deltaX, deltaY);
   }

   public boolean method_25406(Click click) {

      if (this.bgModal != null) {
         int v5 = v2 + 1;

         if (this.bgModal.isOpen() && this.bgModal.mouseReleased(click.method_74245())) {
            return true;
         }
      }

      return super.method_25406(click);
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private static void ensureBackgroundLoaded() {
      if (!bgLoaded) {
         bgLoaded = true;

         try {
            if (Files.exists(BG_CONFIG_FILE)) {
               String v0 = Files.readString(BG_CONFIG_FILE, StandardCharsets.UTF_8).trim();
               selectedBgIndex = Math.max(0, Math.min(BACKGROUND_TEXTURES.length - 1, Integer.parseInt(v0)));
               prevBgIndex = selectedBgIndex;
            }
         } catch (Throwable v3) {
            selectedBgIndex = 0;

            prevBgIndex = 0;
         }

         bgFadeAnim.set(1.0);
      }
   }

   private void drawText(String text, float x, float y, yt font, float size, int color) {
      if (text != null && !text.isEmpty()) {
         font.draw(text, x, y, size, color);
         
      }
   }

   public boolean method_25402(Click click, boolean doubled) {
      if (!this.quitting && !this.isTransitioning) {
         double v3 = click.comp_4798() / (double)this.scaleFix;
         double v5 = click.comp_4799() / (double)this.scaleFix;
         int v7 = click.method_74245();
         int v8 = mc.method_22683().method_4486();
         int v9 = mc.method_22683().method_4502();
         if (v8 <= 0) {
            v8 = Math.max(1, mc.method_22683().method_4480());
         }

         if (v9 <= 0) {
            v9 = Math.max(1, mc.method_22683().method_4507());
         }

         float v10 = (float)v8 / this.scaleFix;
         float v11 = (float)v9 / this.scaleFix;
         if (this.bgModal == null || !this.bgModal.isOpen()) {
            float v12 = this.bgModal != null ? this.bgModal.getOpenProgress() : 0.0F;
            if (v12 < 0.15F) {
               for (MainMenuScreen.GodButton v14 : this.buttons) {
                  if (v14.click(v3, v5, v7)) {
                     return true;
                  }
               }

               if (this.handleBgButtonOpenClick(v3, v5, v7)) {
                  return true;
               }

               if (this.handleVanillaSwitchClick(v3, v5, v7)) {
                  return true;
               }
            }

            return super.method_25402(click, doubled);
         } else {
            return this.bgModal.mouseClicked(v3, v5, v7, v10, v11) ? true : true;
         }
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class GodButton {
      final Runnable action;
      final String label;
      float x;
      final Animation hoverAnim = new Animation();
      float alpha = 1.0F;
      float h;
      float w;
      float y;
      final boolean isRed;
      float baseY;

      void update(double mx, double my) {
         float result = (float)mx;

         float temp1 = (float)my;

         boolean v5 = fq.isHovered(result, temp1, this.x, this.y, this.w, this.h);
         this.hoverAnim.update();
         this.hoverAnim.run(v5 ? 1.0 : 0.0, 0.18F, ap.QUAD_OUT);
      }

      GodButton(String label, Runnable action, boolean isRed) {
         this.label = label;
         this.action = action;
         this.isRed = isRed;
         this.hoverAnim.set(0.0);
      }

      boolean click(double mx, double my, int button) {
         if (button != 0) {
            return false;
         } else if (this.alpha <= 0.05F) {
            return false;
         } else {
            float result = (float)mx;

            float v10 = (float)my;

            if (fq.isHovered(result, v10, this.x, this.y, this.w, this.h)) {
               if (this.action != null) {
                  this.action.run();
               }

               return true;
            } else {
               return false;
            }
         }
      }

      float getHovered() {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.hoverAnim.get();
      }

      
   }
}
