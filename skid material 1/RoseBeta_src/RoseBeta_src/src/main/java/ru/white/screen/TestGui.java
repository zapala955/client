package ru.white.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.white.notification.INotification;
import ru.white.notification.NotificationTask;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.StringSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Sq;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Tq;
import ru.white.utils.misc.Uf;
import ru.white.utils.misc.Vf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.Theme;
import ru.white.module.impl.render.ClickGui;
import ru.white.theme.xo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.ct;
import ru.white.utils.render.dt;
import ru.white.utils.render.et;
import ru.white.utils.render.fs;
import ru.white.utils.render.gs;
import ru.white.utils.render.qs;
import ru.white.utils.render.ss;
import ru.white.utils.render.ur;
import ru.white.utils.render.us;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.module.setting.Setting;
import ru.white.utils.IMinecraft;
import ru.white.utils.SoundUtil;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class TestGui extends Screen implements IMinecraft {
   public boolean searchActive;
   private Module lastSelect;
   private static final long THEME_WAVE_MS = 650L;
   private SliderSetting draggingSlider;
   private int draggingColorBar;
   boolean exit = false;
   private float listScrollTarget;
   private final ct scanLinesPipeline;
   private final ep gearHoverAnim;
   private final ep themeSheetAnim;
   private String hoverCategoryTip;
   private float themeWaveOX;
   private int themeWaveTo;
   double lastMouseX;
   private Module bindingModule;
   private final ep editorBtnAnim;
   private float draggingColorX;
   public Category active;
   private static final Identifier ROSE_LOGO = Identifier.method_60655(
      "client", "textures/rose__logo_watermark.png"
   );
   public int currentTab;
   private float listScrollAnim;
   private float themeWaveOY;
   public static float S = 1.0F;
   private float draggingColorW;
   double lastMouseY;
   private String stringBuffer;
   private final fs grayscalePipeline;
   private long particleDelay;
   private long lastParticle;
   private boolean themeWaveActive;
   private float setMaxScroll;
   private SettingVisibility activeBind;
   private String searchQuery;
   private tf draggingColor;
   private StringSetting activeString;
   private final ep animListReset;
   private final ep animContentReset;
   private float setScrollTarget;
   private final gs halftonePipeline;
   private static final Category[] CAT_ORDER = new Category[]{Category.COMBAT, Category.MOVEMENT, Category.PLAYER, Category.RENDER, Category.OTHER};
   public Animation glomalAnim = new Animation();
   private float scaleFix = 1.0F;
   private long themeWaveStart;
   private final qs raysPipeline;
   private final ur dotsPipeline;
   private final List<TestGui.GuiParticle> particles;
   private boolean themeSheetOpen;
   private long searchTypeTime;
   private float setScrollAnim;
   public Module select;
   private float listMaxScroll;

   public boolean method_25400(CharInput input) {
      ro v2 = so.active();
      if (v2 != null) {
         return true;
      } else if (this.activeString == null) {
         if (this.searchActive) {
            if (input.method_74227() && this.searchQuery.length() < 24) {
               String v11 = this.searchQuery;
               String v6 = input.method_74226();
               String v7 = v11;
               this.searchQuery = v7 + v6;
               this.searchTypeTime = System.currentTimeMillis();
               this.listScrollTarget = 0.0F;
               this.animListReset.reset();
               this.ensureSelect();
               tq.type();
            }

            return true;
         } else {
            return super.method_25400(input);
         }
      } else {
         if (input.method_74227()) {
            String v3 = input.method_74226();

            if (this.activeString.isOnlyNumber()) {
               boolean v10 = v3.matches("[0-9.,-]+");

               if (!v10) {
                  return true;
               }
            }

            String v5 = this.stringBuffer;
            this.stringBuffer = v5 + v3;
            tq.type();
         }

         return true;
      }
   }

   private static String fmtSlider(SliderSetting s) {
      float v1 = s.getValue();
      if (s.increment >= 1.0F) {
         return String.valueOf((int)v1);
      } else {
         Object[] temp1 = new Object[1];

         temp1[0] = v1;
         return String.format("%.1f", temp1);
      }
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private float[] sheetSwatch(float[] sg, int i) {
      int v3 = 8;
      float v4 = 8.0F * S;
      float v5 = 4.0F * S;

      float v10 = sg[2] - v4 * 2.0F;
      float temp1 = v5 * (float)(v3 - 1);

      float v6 = (v10 - temp1) / (float)v3;
      float v7 = 18.0F * S;
      return new float[]{sg[0] + v4 + (float)i * (v6 + v5), sg[1] + 22.0F * S, v6, v7};
   }

   public void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
      float v3 = 2.0F;
      float v4 = (float)mc.method_22683().method_4495();
      this.scaleFix = v3 / v4;
      int v5 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
      int v6 = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
      float v7 = this.glomalAnim.get();
      if (context != null) {
         context.method_51448().pushMatrix();
      }

      ro v8 = so.active();
      if (v8 != null) {
         float v76 = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.scaleFix);
         float v78 = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
         v8.render((float)v5, (float)v6, v76, v78, v7);
         us.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }
      } else {
         if (this.effect("Серый фон")) {
            this.grayscalePipeline.draw(v7);
         }

         et.capture(1);
         if (this.effect("Размывать фон")) {
            vs.ws.blur(0.0F, 0.0F, (float)v5, (float)v6, v7, 0.0F, up.getColor(0, 0));
         }

         if (this.effect("Затемнять фон")) {
            vs.xs.texture(
               Identifier.method_60655("client", "textures/frame/background.png"),
               0.0F,
               0.0F,
               (float)v5,
               (float)v6,
               up.multAlpha(up.client(), v7)
            );
         }

         if (this.effect("Шейдер")) {
            int v9 = up.client();
            this.raysPipeline.draw(v7, up.replAlpha(up.multDark(v9, 0.5F), 0.9F * v7), up.replAlpha(v9, 0.9F * v7), 0.1F, 0.08F, 0.26F);
         }

         if (this.effect("Точки")) {
            float v73 = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.scaleFix);
            float v10 = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
            this.halftonePipeline.draw((float)v5, (float)v6, v73, v10, v7 * 0.1F, up.getColor(255), 6.0F, 0.7F, 3.0F, 100.0F);
         }

         if (this.effect("Скан линии") && v7 > 0.01F) {
            this.scanLinesPipeline.draw((float)v5, (float)v6, v7 * 0.18F, up.getColor(255), 4.0F * S, 1.0F, 50.0F, 3000.0F, 100.0F);
         }

         if (this.effect("Свечение")) {
            int v74 = up.replAlpha(up.client(), v7 * 0.5F);
            vs.xs.texture(
               Identifier.method_60655("client", "textures/effects/circles_effect.png"),
               0.0F,
               0.0F,
               (float)v5,
               (float)v6,
               v74
            );
            vs.xs.texture(
               Identifier.method_60655("client", "textures/effects/top_glow.png"),
               0.0F,
               0.0F,
               (float)v5,
               (float)v6,
               v74
            );
         }

         if (this.effect("Частицы")) {
            this.spawnParticle(v5, v6);
            this.renderParticles(v7);
         } else if (!this.particles.isEmpty()) {
            this.particles.clear();
         }

         et.capture();

         try {
            S = Client.get().moduleManager().get(ClickGui.class).size.getValue();
         } catch (Exception v72) {
            S = 1.0F;
         }

         this.ensureSelect();
         TestGui.Layout v75 = this.computeLayout(v5, v6);
         yt v77 = hu.sf_regular;
         yt v11 = hu.sf_bold;
         float v12 = this.exit ? 60.0F * S - 60.0F * S * v7 : -60.0F * S + 60.0F * S * v7;
         v75.iconY += v12;
         v75.listY += v12;
         v75.mainY += v12;
         v75.logoY += v12;
         v75.catTopY += v12;
         v75.listContentY += v12;
         v75.gearY += v12;
         v75.crumbY += v12;
         v75.tabY += v12;
         v75.colsY += v12;
         v75.searchY += v12;
         this.hoverCategoryTip = null;
         this.panel(v75.iconX, v75.iconY, v75.iconW + v75.listW + v75.mainW, v75.iconH, v7);
         vr.rect(v75.listX, v75.iconY, 0.5F * S, v75.iconH, up.getColor(255, (int)(14.0F * v7)), 0.0F);
         vr.rect(v75.mainX, v75.iconY, 0.5F * S, v75.iconH, up.getColor(255, (int)(14.0F * v7)), 0.0F);
         vs.xs.texture(ROSE_LOGO, v75.logoX, v75.logoY, v75.logoS, v75.logoS, up.replAlpha(up.getColor(255), v7));
         float v13 = v75.catTopY;

         for (Category v17 : CAT_ORDER) {
            boolean v18 = this.active == v17 && !this.searching();
            v17.alphaS.setDirection(v18 ? cp.FORWARDS : cp.BACKWARDS);
            float v19 = v17.alphaS.getOutput();
            boolean v20 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v75.iconX, v13, v75.iconW, v75.catBtnH);
            v17.alphaS2.setDirection(v20 ? cp.FORWARDS : cp.BACKWARDS);
            float v21 = v17.alphaS2.getOutput();
            if (v20) {
               this.hoverCategoryTip = v17.getName();
            }

            if (v19 > 0.01F) {
               vr.rect(v75.iconX, v13 + 2.0F * S, 2.5F * S, v75.catBtnH - 4.0F * S, up.replAlpha(up.client(), v7 * v19), 0.0F);
            }

            int v22 = up.overCol(up.getColor(255, (0.35F + 0.35F * v21) * v7), up.replAlpha(up.client(), v7), v19);

            try {
               hu.nightix_2
                  .draw(
                     v17.getIcon(),
                     v75.iconX + v75.iconW / 2.0F - hu.nightix_2.getWidth(v17.getIcon(), 8.0F * S) / 2.0F,
                     v13 + v75.catBtnH / 2.0F - 4.0F * S,
                     8.0F * S,
                     v22
                  );
            } catch (Exception v71) {
            }

            v13 += v75.catBtnH + 4.0F * S;
         }

         boolean v79 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v75.gearX, v75.gearY, v75.gearS, v75.gearS);
         this.gearHoverAnim.setDirection(!v79 && !this.themeSheetOpen ? cp.BACKWARDS : cp.FORWARDS);
         float v80 = this.gearHoverAnim.getOutput();
         int v81 = this.themeSheetOpen ? up.replAlpha(up.client(), v7) : up.getColor(255, v7 * (0.35F + 0.4F * v80));
         hu.icon
            .draw("l", v75.gearX + v75.gearS / 2.0F - hu.icon.getWidth("l", 9.0F * S) / 2.0F, v75.gearY + v75.gearS / 2.0F - 4.5F * S, 9.0F * S, v81);
         String v82 = this.searching() ? "Поиск" : this.active.getName();
         hu.sf_medium.draw(v82, v75.listX + 12.0F * S, v75.listY + 10.0F * S, 7.5F * S, up.getColor(255, 0.88F * v7));
         List v83 = this.visibleModules();
         float v84 = 21.0F * S;
         float v85 = 2.5F * S;
         float v86 = (float)v83.size() * (v84 + v85);
         this.listMaxScroll = Math.max(0.0F, v86 - v75.listContentH);
         this.listScrollTarget = MathHelper.method_15363(this.listScrollTarget, -this.listMaxScroll, 0.0F);
         this.listScrollAnim = this.listScrollAnim + (this.listScrollTarget - this.listScrollAnim) * 0.2F;
         dt.enable(v75.listX, v75.listContentY, v75.listW, v75.listContentH, 2.0F);
         float v87 = v75.listContentY + 2.0F * S + this.listScrollAnim;
         int v23 = 0;
         int v24 = v83.size();

         for (Module v26 : v83) {
            float v27 = this.enterT(v23, v24);
            float v28 = v7 * v27;
            float v29 = v87 + (1.0F - v27) * 10.0F * S;
            boolean v30 = v26 == this.select;
            v26.getAnimation12()
               .setDirection(
                  fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v75.listX + 4.0F * S, v29, v75.listW - 8.0F * S, v84)
                     ? cp.FORWARDS
                     : cp.BACKWARDS
               );
            float v31 = v26.getAnimation12().getOutput();
            v26.getAnimation16().setDirection(v26.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);
            float v32 = v26.getAnimation16().getOutput();
            if (v27 > 0.02F) {
               if (v30) {
                  vr.rect(v75.listX + 4.0F * S, v29, v75.listW - 8.0F * S, v84, up.getColor(255, (int)(10.0F * v28)), 3.0F * S);
               } else if (v31 > 0.01F) {
                  vr.rect(
                     v75.listX + 4.0F * S, v29, v75.listW - 8.0F * S, v84, up.getColor(255, (int)(8.0F * v28 * v31)), 3.0F * S
                  );
               }

               float v33 = v75.listX + 11.0F * S;
               float v34 = v29 + v84 / 2.0F - 3.25F * S;
               float v35 = 6.5F * S;
               int v36 = v30
                  ? up.replAlpha(up.client(), v28)
                  : (
                     v32 > 0.05F
                        ? up.replAlpha(up.client(), v28 * (0.45F + 0.55F * v32))
                        : up.getColor(255, v28 * (0.35F + 0.35F * v31))
                  );
               float temp4 = 0.65F * S;
               float var10006 = v35 / 2.0F;

               vr.outline(v33, v34, v35, v35, temp4, v36, var10006);
               vr.rect(v33 + 2.25F * S, v34 + 2.25F * S, 2.0F * S, 2.0F * S, v36, 1.0F * S);
               int v37 = v30
                  ? up.replAlpha(up.client(), v28)
                  : up.getColor(255, v28 * (0.46F + 0.4F * Math.max(v32 * 0.35F, v31)));
               v77.drawFadingText(v26.getBigName(), v75.listX + 23.0F * S, v29 + 6.5F * S, v75.listW - 32.0F * S, v37, 6.8F * S);
            }

            v87 += v84 + v85;
            v23++;
         }

         if (v83.isEmpty()) {
            v77.drawCentered(
               "Ничего не найдено",
               v75.listX + v75.listW / 2.0F,
               v75.listContentY + v75.listContentH / 2.0F - 4.0F * S,
               7.0F * S,
               up.getColor(255, 0.45F * v7)
            );
         }

         dt.disable();
         if (this.listMaxScroll > 1.0F) {
            float v88 = v75.listContentH - 6.0F * S;
            float v92 = Math.max(14.0F * S, v75.listContentH / Math.max(1.0F, v86) * v88);
            float v94 = this.listMaxScroll <= 0.0F ? 0.0F : -this.listScrollAnim / this.listMaxScroll;
            float v97 = v75.listContentY + 3.0F * S + v94 * (v88 - v92);
            vr.rect(v75.listX + v75.listW - 3.5F * S, v97, 1.5F * S, v92, up.replAlpha(up.client(), 0.55F * v7), 0.75F * S);
         }

         float v89 = v75.mainX + 14.0F * S;
         float v93 = v75.searchX - v89 - 44.0F * S;
         if (this.select != null) {
            String v95 = this.searching() ? "Поиск" : this.select.getCategory().getName();
            v77.draw(v95, v89, v75.crumbY, 7.0F * S, up.getColor(255, 0.45F * v7));
            v89 += v77.getWidth(v95, 7.0F * S) + 5.0F * S;
            v77.draw("/", v89, v75.crumbY, 7.0F * S, up.getColor(255, 0.25F * v7));
            v89 += v77.getWidth("/", 7.0F * S) + 5.0F * S;
            float v98 = Math.min(v77.getWidth(this.select.getBigName(), 7.5F * S), Math.max(20.0F * S, v93 - (v89 - (v75.mainX + 14.0F * S))));
            v77.drawFadingText(this.select.getBigName(), v89, v75.crumbY, v98, up.replAlpha(up.client(), v7), 7.5F * S);
         }

         float v96 = v75.searchY + 1.0F * S;
         float v99 = v75.searchX - 38.0F * S;
         boolean var100 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v99, v96, 16.0F * S, 16.0F * S);
         hu.icon
            .draw("F", v99 + 3.0F * S, v96 + 4.0F * S, 6.5F * S, up.getColor(255, (0.35F + 0.35F * (var100 ? 1.0F : 0.0F)) * v7));
         float var101 = v75.searchX - 20.0F * S;
         boolean var102 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, var101, v96, 16.0F * S, 16.0F * S);
         hu.nightix_2
            .draw("N", var101 + 3.0F * S, v96 + 4.0F * S, 6.5F * S, up.getColor(255, (0.35F + 0.35F * (var102 ? 1.0F : 0.0F)) * v7));
         boolean var103 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v75.searchX, v75.searchY, v75.searchW, v75.searchH);
         vr.rect(
            v75.searchX,
            v75.searchY,
            v75.searchW,
            v75.searchH,
            up.getColor(16, 17, 23, (int)(220.0F * v7)),
            4.0F * S
         );
         if (this.searchActive) {
            vr.outline(v75.searchX, v75.searchY, v75.searchW, v75.searchH, 0.5F * S, up.replAlpha(up.client(), 0.8F * v7), 4.0F * S);
         } else if (var103) {
            vr.outline(
               v75.searchX,
               v75.searchY,
               v75.searchW,
               v75.searchH,
               0.5F * S,
               up.getColor(42, 45, 56, (int)(160.0F * v7)),
               4.0F * S
            );
         }

         hu.gui.draw("A", v75.searchX + 6.0F * S, v75.searchY + 5.5F * S, 6.0F * S, up.getColor(255, 0.45F * v7));
         float var104 = v75.searchX + 17.0F * S;
         float var105 = v75.searchW - 24.0F * S;
         if (this.searchQuery.isEmpty() && !this.searchActive) {
            v77.draw("Поиск", var104, v75.searchY + 4.5F * S, 6.5F * S, up.getColor(255, 0.35F * v7));
         } else {
            v77.drawFadingTextReverse(
               this.searchQuery, var104, v75.searchY + 4.5F * S, var105, up.getColor(255, 0.9F * v7), 6.5F * S
            );
            if (this.searchActive
               && System.currentTimeMillis() / (7765085069003318361L + -7765085069003317911L) % (-4861671295992916278L + 4861671295992916280L) == 0L) {
               float var106 = Math.min(v77.getWidth(this.searchQuery, 6.5F * S), var105);
               vr.rect(var104 + var106 + 1.0F * S, v75.searchY + 4.0F * S, 0.8F * S, 9.0F * S, up.replAlpha(up.client(), v7), 0.4F * S);
            }
         }

         float var107 = v77.getWidth("Основной", 6.8F * S) + 18.0F * S;
         float var108 = v77.getWidth("Debug", 6.8F * S) + 18.0F * S;
         float var109 = v75.mainX + 12.0F * S;
         if (this.currentTab == 0) {
            vr.rect(var109, v75.tabY, var107, v75.tabH, up.replAlpha(up.client(), 0.16F * v7), 3.0F * S);
            vr.outline(var109, v75.tabY, var107, v75.tabH, 0.5F * S, up.replAlpha(up.client(), 0.4F * v7), 3.0F * S);
            v77.draw("Основной", var109 + 9.0F * S, v75.tabY + 4.5F * S, 6.8F * S, up.replAlpha(up.client(), v7));
         } else {
            v77.draw("Основной", var109 + 9.0F * S, v75.tabY + 4.5F * S, 6.8F * S, up.getColor(255, 0.45F * v7));
         }

         float v38 = var109 + var107 + 6.0F * S;
         if (this.currentTab == 1) {
            vr.rect(v38, v75.tabY, var108, v75.tabH, up.replAlpha(up.client(), 0.16F * v7), 3.0F * S);
            vr.outline(v38, v75.tabY, var108, v75.tabH, 0.5F * S, up.replAlpha(up.client(), 0.4F * v7), 3.0F * S);
            v77.draw("Debug", v38 + 9.0F * S, v75.tabY + 4.5F * S, 6.8F * S, up.replAlpha(up.client(), v7));
         } else {
            v77.draw("Debug", v38 + 9.0F * S, v75.tabY + 4.5F * S, 6.8F * S, up.getColor(255, 0.45F * v7));
         }

         if (this.select != this.lastSelect) {
            this.animContentReset.reset();
            this.lastSelect = this.select;
         }

         float v39 = this.animContentReset.getOutput();
         if (v39 < 0.01F) {
            v39 = 0.01F;
         }

         if (this.select != null) {
            List v40 = this.visibleSettings(this.select);
            int v41 = (v40.size() + 1) / 2;
            List v42 = v41 > 0 ? v40.subList(0, Math.min(v41, v40.size())) : Collections.emptyList();
            List v43 = v40.size() > v41 ? v40.subList(v41, v40.size()) : Collections.emptyList();
            float v44 = 22.0F * S + this.colHeaderH() + 19.0F * S + 19.0F * S;

            for (Setting v46 : v42) {
               v44 += this.settingHeight(v46, v75.colW - 16.0F * S, v77) + 4.0F * S;
            }

            float var115 = 22.0F * S + this.colHeaderH();

            for (Setting v47 : v43) {
               var115 += this.settingHeight(v47, v75.colW - 16.0F * S, v77) + 4.0F * S;
            }

            float var118 = Math.max(v44, var115);
            this.setMaxScroll = Math.max(0.0F, var118 - v75.colsH);
            this.setScrollTarget = MathHelper.method_15363(this.setScrollTarget, -this.setMaxScroll, 0.0F);
            this.setScrollAnim = this.setScrollAnim + (this.setScrollTarget - this.setScrollAnim) * 0.2F;
            int var119 = up.getColor(16, 17, 23, (int)(220.0F * v7));
            int v48 = up.getColor(35, 38, 48, (int)(140.0F * v7));
            vr.rect(v75.leftX, v75.colsY, v75.colW, v75.colsH, var119, 4.0F * S);
            vr.rect(v75.rightX, v75.colsY, v75.colW, v75.colsH, var119, 4.0F * S);
            vr.outline(v75.leftX, v75.colsY, v75.colW, v75.colsH, 0.5F * S, v48, 4.0F * S);
            vr.outline(v75.rightX, v75.colsY, v75.colW, v75.colsH, 0.5F * S, v48, 4.0F * S);
            float v49 = v7 * v39;
            float v50 = (1.0F - v39) * 8.0F * S;
            dt.enable(v75.leftX, v75.colsY, v75.colW, v75.colsH, 2.0F);
            float v51 = v75.colsY + 6.0F * S + this.setScrollAnim + v50;
            v51 = this.renderColHeader(
               v77, v11, "Глобальные настройки", v75.leftX, v51, v75.colW, v49
            );
            if (this.currentTab == 1) {
               String v57 = this.select.getName();
               v77.draw("Имя: " + v57, v75.leftX + 8.0F * S, v51 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               v51 += 16.0F * S;
               String v58 = this.select.getClass().getSimpleName();
               v77.draw("Класс: " + v58, v75.leftX + 8.0F * S, v51 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               v51 += 16.0F * S;
               String v59 = this.select.getCategory().getName();
               v77.draw("Категория: " + v59, v75.leftX + 8.0F * S, v51 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               v51 += 16.0F * S;
               boolean v60 = this.select.isEnabled();
               v77.draw(
                  "Включен: " + v60,
                  v75.leftX + 8.0F * S,
                  v51 + 2.0F * S,
                  6.5F * S,
                  this.select.isEnabled() ? up.replAlpha(up.client(), v49) : up.getColor(255, 0.45F * v49)
               );
               v51 += 16.0F * S;
               int v61 = this.select.getSettings().size();
               v77.draw("Настроек: " + v61, v75.leftX + 8.0F * S, v51 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               v51 += 16.0F * S;
               String v62 = this.select.getKey() > 0 ? eq.keyName(this.select.getKey()) : "NONE";
               v77.draw("Клавиша: " + v62, v75.leftX + 8.0F * S, v51 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               v51 += 16.0F * S;
            } else {
               v51 = this.renderEnableRow(v77, v75.leftX, v51, v75.colW, v49);
               v51 = this.renderKeyRow(v77, v75.leftX, v51, v75.colW, v49);

               for (Setting v53 : v42) {
                  v51 = this.renderSetting(v77, v53, v75.leftX + 8.0F * S, v51, v75.colW - 16.0F * S, v49) + 4.0F * S;
               }
            }

            dt.disable();
            dt.enable(v75.rightX, v75.colsY, v75.colW, v75.colsH, 2.0F);
            float var129 = v75.colsY + 6.0F * S + this.setScrollAnim + v50;
            var129 = this.renderColHeader(
               v77,
               v11,
               this.currentTab == 1
                  ? "Системные данные"
                  : "Настройка визуала",
               v75.rightX,
               var129,
               v75.colW,
               v49
            );
            if (this.currentTab == 1) {
               int v63 = mc.method_47599();
               v77.draw("FPS: " + v63, v75.rightX + 8.0F * S, var129 + 2.0F * S, 6.5F * S, up.getColor(255, 0.75F * v49));
               var129 += 16.0F * S;
               String v64 = String.format("%.2f", S);
               v77.draw(
                  "GUI масштаб: " + v64,
                  v75.rightX + 8.0F * S,
                  var129 + 2.0F * S,
                  6.5F * S,
                  up.getColor(255, 0.75F * v49)
               );
               var129 += 16.0F * S;
               long var136 = Runtime.getRuntime().totalMemory()
                  / (5106278566476907088L + -5106278566476906064L)
                  / (4908065280120143474L + -4908065280120142450L);
               long v55 = Runtime.getRuntime().freeMemory() / (-1361188264588256036L + 1361188264588257060L) / (3987076444437397477L + -3987076444437396453L);
               long v67 = var136 - v55;
               v77.draw(
                  "Память: " + v67 + "MB / " + var136 + "MB",
                  v75.rightX + 8.0F * S,
                  var129 + 2.0F * S,
                  6.5F * S,
                  up.getColor(255, 0.75F * v49)
               );
               var129 += 16.0F * S;
               String v69 = ko.selectedTheme != null ? ko.selectedTheme.name() : "DEFAULT";
               v77.draw("Тема: " + v69, v75.rightX + 8.0F * S, var129 + 2.0F * S, 6.5F * S, up.replAlpha(up.client(), v49));
               var129 += 16.0F * S;
            } else if (v43.isEmpty()) {
               v77.drawCentered(
                  "Нет настроек",
                  v75.rightX + v75.colW / 2.0F,
                  var129 + 20.0F * S,
                  6.5F * S,
                  up.getColor(255, 0.35F * v49)
               );
            } else {
               for (Setting v54 : v43) {
                  var129 = this.renderSetting(v77, v54, v75.rightX + 8.0F * S, var129, v75.colW - 16.0F * S, v49) + 4.0F * S;
               }
            }

            dt.disable();
            if (this.setMaxScroll > 1.0F) {
               float var137 = v75.colsH - 8.0F * S;
               float var138 = Math.max(16.0F * S, v75.colsH / Math.max(1.0F, var118) * var137);
               float var139 = -this.setScrollAnim / this.setMaxScroll;
               float v56 = v75.colsY + 4.0F * S + var139 * (var137 - var138);
               vr.rect(v75.mainX + v75.mainW - 6.0F * S, v56, 1.5F * S, var138, up.replAlpha(up.client(), 0.55F * v7), 0.75F * S);
            }

            if (this.draggingSlider != null) {
            }

            if (this.draggingColor != null) {
               this.applyColorDrag(this.draggingColor);
            }
         }

         this.renderThemeWave(v75.iconX, v75.iconY, v75.iconW + v75.listW + v75.mainW, v75.iconH, v7);
         this.themeSheetAnim.setDirection(this.themeSheetOpen ? cp.FORWARDS : cp.BACKWARDS);
         float var110 = this.themeSheetAnim.getOutput();
         if (var110 > 0.01F) {
            this.renderThemeSheet(v75, v7 * var110, var110);
         }

         if (this.hoverCategoryTip != null && v7 > 0.05F) {
            float var111 = 6.5F * S;
            float var112 = v77.getWidth(this.hoverCategoryTip, var111) + 14.0F * S;
            float var113 = 16.0F * S;
            float var114 = (float)this.lastMouseX + 12.0F * S;
            float var116 = (float)this.lastMouseY + 10.0F * S;
            if (var114 + var112 > (float)v5 - 4.0F * S) {
               var114 = (float)this.lastMouseX - var112 - 10.0F * S;
            }

            vr.rect(var114, var116, var112, var113, up.getColor(10, (int)(230.0F * v7)), 4.0F * S);
            vr.outline(var114, var116, var112, var113, 0.5F * S, up.replAlpha(up.client(), 0.5F * v7), 4.0F * S);
            v77.draw(this.hoverCategoryTip, var114 + 7.0F * S, var116 + 4.0F * S, var111, up.getColor(255, 0.9F * v7));
         }

         us.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }
      }
   }

   public void method_25432() {
      so.closeAll();
      int v1 = 1164246014;
      int result = v1 * (v1 + 1);

      super.method_25432();
   }

   private boolean clickSetting(Setting<?> s, float x, float y, float w, float mx, float my, int button, yt draw) {
      if (s instanceof BooleanSetting v9) {
         if (fq.isHovered(mx, my, x, y, w, 19.0F * S)) {
            v9.set(!v9.getValue());
            tq.toggle(v9.getValue());
            return true;
         }
      } else if (s instanceof SliderSetting v10) {
         float v19 = y + 15.0F * S;
         if (button == 0 && fq.isHovered(mx, my, x, y, w, 26.0F * S)) {
            this.draggingSlider = v10;
            float v20 = MathHelper.method_15363((mx - x) / w, 0.0F, 1.0F);
            float v21 = v10.min + v20 * (v10.max - v10.min);
            v10.set(MathHelper.method_15363((float)Math.round(v21 / v10.increment) * v10.increment, v10.min, v10.max));
            tq.sliderGrab();
            return true;
         }
      } else if (s instanceof ModeSetting v11) {
         String v17 = v11.getValue();
         float v18 = Math.min(draw.getWidth(v17, 6.0F * S) + 22.0F * S, w * 0.5F);
         float v29 = x + w - v18;
         if (fq.isHovered(mx, my, v29, y, v18, 14.0F * S)) {
            v11.opened = !v11.opened;
            tq.expand(v11.opened);
            return true;
         }

         if (v11.opened) {
            float v32 = y + 18.0F * S;

            for (String v22 : v11.values) {
               if (fq.isHovered(mx, my, x, v32, w, 15.0F * S)) {
                  if (!v22.equals(v17)) {
                     v11.set(v22);
                     tq.chip(v11.values.indexOf(v22), v11.values.size());
                  }

                  v11.opened = false;
                  return true;
               }

               v32 += 15.0F * S;
            }

            if (fq.isHovered(mx, my, x, y + 16.0F * S, w, (float)(v11.values.size() * (15)) * S + 6.0F * S)) {
               return true;
            }
         }
      } else if (s instanceof MultiSetting v12) {
         String v24 = v12.getNames();
         if (v24.isEmpty()) {
            v24 = "Ничего";
         }

         float v27 = Math.min(draw.getWidth(v24, 6.0F * S) + 22.0F * S, w * 0.5F);
         float v30 = x + w - v27;
         if (fq.isHovered(mx, my, v30, y, v27, 14.0F * S)) {
            v12.opened = !v12.opened;
            tq.expand(v12.opened);
            return true;
         }

         if (v12.opened) {
            float v33 = y + 18.0F * S;

            for (BooleanSetting v36 : v12.getValues()) {
               if (fq.isHovered(mx, my, x, v33, w, 15.0F * S)) {
                  v36.set(!v36.getValue());
                  tq.chipMulti(v36.getValue());

                  return true;
               }

               v33 += 15.0F * S;
            }

            if (fq.isHovered(mx, my, x, y + 16.0F * S, w, (float)(v12.getValues().size() * (15)) * S + 6.0F * S)) {
               return true;
            }
         }
      } else if (s instanceof SettingVisibility v13) {
         if (button == 0 && fq.isHovered(mx, my, x, y, w, 19.0F * S)) {
            this.activeBind = v13;
            tq.bindStart();
            return true;
         }
      } else if (s instanceof StringSetting v14) {
         if (button == 0 && fq.isHovered(mx, my, x, y, w, 14.0F * S)) {
            this.activeString = v14;
            this.stringBuffer = v14.getValue();
            tq.editStart();
            return true;
         }
      } else if (s instanceof tf v15) {
         if (button == 0 && fq.isHovered(mx, my, x, y, w, 13.0F * S)) {
            v15.pickerOpen = !v15.pickerOpen;
            tq.picker(v15.pickerOpen);
            return true;
         }

         if (v15.pickerOpen) {
            for (int v25 = 0; v25 < 3; v25++) {
               float v28 = y + 18.0F * S + (float)(v25 * (12)) * S;
               if (fq.isHovered(mx, my, x, v28 - 3.0F * S, w, 10.0F * S)) {
                  this.draggingColor = v15;
                  this.draggingColorBar = v25;
                  this.draggingColorX = x;
                  this.draggingColorW = w;
                  float v31 = fq.clamp((mx - x) / w, 0.0F, 1.0F);
                  if (v15.updateHsbBar(v25, v31)) {
                     tq.colorTick(v31);
                  }

                  tq.sliderGrab();
                  return true;
               }
            }
         }
      } else if (s instanceof sf v16) {
         float v26 = Math.max(draw.getWidth(v16.getName(), 6.0F * S) + 16.0F * S, 60.0F * S);
         if (fq.isHovered(mx, my, x + w - v26 - 2.0F * S, y - 2.0F * S, v26 + 4.0F * S, 17.0F * S)) {
            v16.press();
            tq.button();
            return true;
         }
      }

      return false;
   }

   private void spawnParticle(int w, int h) {
      long result = System.currentTimeMillis();

      if (result - this.lastParticle >= this.particleDelay) {
         this.lastParticle = System.currentTimeMillis();
         this.particleDelay = -3240853321066636498L + 3240853321066636568L + (long)(Math.random() * 90.0);
         if (this.particles.size() <= 60) {
            List v6 = this.particles;
            TestGui.GuiParticle temp1 = new TestGui.GuiParticle;
            double temp3 = Math.random();

            float v7 = (float)(temp3 * (double)w);
            float temp4 = (float)(Math.random() * (double)h);

            temp1./* $VF: Unable to resugar constructor */<init>(v7, temp4);
            v6.add(temp1);
         }
      }
   }

   private void drawToggle(float x, float y, float w, float h, float onA, float g) {
      int v7 = up.getColor(33, 35, 44, (int)(240.0F * g));
      int v8 = up.replAlpha(up.client(), 0.95F * g);

      int v9 = up.overCol(v7, v8, onA);
      vr.rect(x, y, w, h, v9, h / 2.0F);
      float v10 = h - 3.0F * S;
      float v11 = x + 1.5F * S + (w - v10 - 3.0F * S) * onA;
      int v12 = up.getColor(65, 68, 82, (int)(255.0F * g));
      int v13 = up.getColor(255, (int)(255.0F * g));
      int v14 = up.overCol(v12, v13, onA);
      vr.rect(v11, y + 1.5F * S, v10, v10, v14, v10 / 2.0F);
   }

   private void beforeEditorOpen() {
      this.exit = false;
      int v1 = 1417985956;

      this.glomalAnim.run(1.0, 0.2F, ap.QUAD_OUT);
      
   }

   private boolean effect(String name) {
      ClickGui v2 = Client.get().moduleManager().get(ClickGui.class);
      if (v2 != null) {
         int v3 = 1645006499;

         int temp1 = v3 * (v3 + 1);

         if (v2.effect.getValue(name)) {
            return true;
         }
      }

      return false;
   }

   private boolean isControlDown() {
      boolean result;
      if (!InputUtil.method_15987(mc.method_22683(), 341) && !InputUtil.method_15987(mc.method_22683(), 345)) {
         result = false;
      } else {
         result = true;

         int v1 = -299360338;
         int v4 = v1 * (v1 + 1);

         
      }

      return result;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.closeCheck();
      this.glomalAnim.update();
      float result = (float)mouseX;

      mouseX = (int)(result / this.scaleFix);
      mouseY = (int)((float)mouseY / this.scaleFix);
      this.lastMouseX = (double)mouseX;
      this.lastMouseY = (double)mouseY;
   }

   private void panel(float x, float y, float w, float h, float g) {

      vs.ys.glow(x, y, w, h, up.getColor(0, 0.15F * g), 8.0F * S, 15.0F, 1.0F);
      vs.ws.blur(x, y, w, h, g, 8.0F * S, up.multAlpha(up.multDark(up.background(), 0.55F), g));
      vs.ys.rect(x, y, w, h, up.getColor(12, 13, 17, (int)(160.0F * g)), 6.0F * S);
      Identifier result = Identifier.method_60655("client", "textures/frame/rectgui.png");

      vs.xs.texture(result, x, y, w, h, up.multAlpha(up.client(), g * 0.3F));
      vr.outline(x, y, w, h, 0.5F * S, up.getColor(35, 38, 48, (int)(160.0F * g)), 6.0F * S);
      if (this.effect("Свечение")) {
         ss.render(x, y, w, h, 6.0F * S, S, g);
      }
   }

   private float enterT(int idx, 

      float v3 = this.animListReset.getOutput();
      if (v3 >= 1.0F) {
         return 1.0F;
      } else {
         float v4 = 0.07F;
         float v5 = 1.0F + (float)Math.max(0, total - 1) * v4;
         float result = MathHelper.method_15363(v3 * v5 - (float)idx * v4, 0.0F, 1.0F);

         return result;
      }
   }

   public boolean method_25401(double mxRaw, double myRaw, double hAmount, double vAmount) {
      float v9 = (float)(mxRaw / (double)this.scaleFix);
      float v10 = (float)(myRaw / (double)this.scaleFix);
      ro v11 = so.active();
      if (v11 != null) {
         return v11.mouseScrolled(v9, v10, vAmount);
      } else {
         int v12 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
         int v13 = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         TestGui.Layout v14 = this.computeLayout(v12, v13);

         float v15 = this.exit ? 60.0F * S - 60.0F * S * this.glomalAnim.get() : -60.0F * S + 60.0F * S * this.glomalAnim.get();
         v14.listContentY += v15;
         v14.colsY += v15;
         if (fq.isHovered(v9, v10, v14.listX, v14.listContentY, v14.listW, v14.listContentH)) {
            if (this.listMaxScroll > 0.0F) {
               float v18 = this.listScrollTarget;
               this.listScrollTarget = this.listScrollTarget + (float)(vAmount * 22.0 * (double)S);
               this.listScrollTarget = MathHelper.method_15363(this.listScrollTarget, -this.listMaxScroll, 0.0F);
               if (v18 != this.listScrollTarget) {
                  tq.scroll();
               }
            }

            return true;
         } else if (!fq.isHovered(v9, v10, v14.leftX, v14.colsY, v14.colW, v14.colsH)
            && !fq.isHovered(v9, v10, v14.rightX, v14.colsY, v14.colW, v14.colsH)) {
            return super.method_25401(mxRaw, myRaw, hAmount, vAmount);
         } else {
            if (this.setMaxScroll > 0.0F) {
               float v16 = this.setScrollTarget;
               this.setScrollTarget = this.setScrollTarget + (float)(vAmount * 22.0 * (double)S);
               this.setScrollTarget = MathHelper.method_15363(this.setScrollTarget, -this.setMaxScroll, 0.0F);
               if (v16 != this.setScrollTarget) {
                  tq.scroll();
               }
            }

            return true;
         }
      }
   }

   private void renderParticles(float g) {
      for (int v2 = this.particles.size() - 1; v2 >= 0; v2--) {
         TestGui.GuiParticle v3 = this.particles.get(v2);
         float v4 = v3.progress();
         if (v4 >= 1.0F) {
            this.particles.remove(v2);
         } else {
            float v5 = 1.0F - (1.0F - v4) * (1.0F - v4) * (1.0F - v4);
            float v6 = v3.maxRadius * v5 * S;
            if (!(v6 < 0.4F)) {
               float v7 = smoothstep(0.0F, 0.18F, v4) * (1.0F - smoothstep(0.45F, 1.0F, v4));
               float v8 = v7 * g * 0.75F;
               if (!(v8 < 0.004F)) {
                  float v9 = 1.1F + 2.6F * (1.0F - v5);

                  float v10 = v3.y - v3.drift * v5 * S;
                  float result = v3.x - v6;
                  float temp1 = v10 - v6;
                  float v13 = v6 * 2.0F;
                  float temp3 = v6 * 2.0F;

                  vs.ys.outline(result, temp1, v13, temp3, v9, up.replAlpha(up.client(), v8), v6);
               }
            }
         }
      }
   }

   public void openPreviewEditor(Module target) {
      int v2 = -1952839125;

      this.beforeEditorOpen();
      mo.getInstance().open(target);
      tq.editor();
   }

   public void openCrosshairEditor() {
      this.beforeEditorOpen();
      eo result = eo.getInstance();

      result.open();
      tq.editor();
   }

   public boolean method_25421() {

      return false;
   }

   public boolean method_25404(KeyInput input) {
      int v2 = input.comp_4795();
      ro v3 = so.active();
      if (v3 != null) {
         if (v2 == 256) {
            v3.saveAndExit();
         }

         return true;
      } else if (this.bindingModule != null) {
         boolean v12 = v2 == 256 || v2 == 261;
         this.bindingModule.setKey(v12 ? -1 : v2);
         this.bindingModule = null;
         if (v12) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (this.activeBind != null) {
         boolean v11 = v2 == 256 || v2 == 261;
         this.activeBind.set(v11 ? -1 : v2);
         this.activeBind = null;
         if (v11) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (this.activeString == null) {
         if (this.searchActive) {
            if (this.isControlDown() && v2 == 86) {
               String v10 = mc.field_1774.method_1460();
               if (v10 != null && !v10.isEmpty()) {

                  String v8 = this.searchQuery;
                  this.searchQuery = v8 + v10;
                  this.searchTypeTime = System.currentTimeMillis();
                  this.listScrollTarget = 0.0F;
                  this.animListReset.reset();
                  this.ensureSelect();
                  tq.type();
               }

               return true;
            } else if (v2 == 256) {
               if (!this.searchQuery.isEmpty()) {
                  this.searchQuery = "";
                  this.listScrollTarget = 0.0F;
                  this.animListReset.reset();
                  this.ensureSelect();
                  tq.searchClear();
               } else {
                  this.searchActive = false;
               }

               return true;
            } else if (v2 == 257 || v2 == 335) {
               this.searchActive = false;
               tq.editCommit();
               return true;
            } else if (v2 == 259 && !this.searchQuery.isEmpty()) {
               this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
               this.searchTypeTime = System.currentTimeMillis();
               this.listScrollTarget = 0.0F;
               this.animListReset.reset();
               this.ensureSelect();
               tq.erase();
               return true;
            } else {
               return true;
            }
         } else if (v2 == 256) {
            if (!this.exit) {
               this.glomalAnim.run(0.0, 0.3F, ap.SINE_IN);
               this.exit = true;
               tq.close();
            }

            return true;
         } else {
            return super.method_25404(input);
         }
      } else if (!this.isControlDown() || v2 != 86 && v2 != 118) {
         if (v2 == 257 || v2 == 335) {
            this.activeString.set(this.stringBuffer);
            this.activeString = null;
            tq.editCommit();
            return true;
         } else if (v2 == 256) {
            this.activeString = null;
            tq.editCancel();
            return true;
         } else if (v2 == 259 && !this.stringBuffer.isEmpty()) {
            this.stringBuffer = this.stringBuffer.substring(0, this.stringBuffer.length() - 1);
            tq.erase();
            return true;
         } else {
            return true;
         }
      } else {
         String v4 = mc.field_1774.method_1460();
         if (v4 != null && !v4.isEmpty()) {
            String v6 = this.stringBuffer;
            this.stringBuffer = v6 + v4;
            tq.type();
         }

         return true;
      }
   }

   private void closeCheck() {
      if (this.exit) {

         if (this.glomalAnim.isFinished()) {
            this.exit = false;
            sq.stop();
            this.method_25419();
         }
      }
   }

   private List<Module> visibleModules() {
      ArrayList result = new ArrayList();
      int v4 = -1479320907;

      ArrayList v1 = result;

      try {
         for (Module v3 : Client.get().moduleManager().values()) {
            if (this.moduleVisible(v3)) {
               v1.add(v3);
            }
         }
      } catch (Exception v6) {
      }

      return v1;
   }

   public void openThemeEditor() {
      this.beforeEditorOpen();

      try {
         no.getInstance().open();
      } catch (Exception v5) {
      }

      tq.editor();
   }

   private float renderColHeader(yt draw, yt bold, String text, float x, float y, float w, float g) {
      float temp2 = x + 8.0F * S;
      float temp3 = y + 2.0F * S;

      bold.draw(text, temp2, temp3, 6.8F * S, up.getColor(255, 0.65F * g));
      return y + 18.0F * S;
   }

   private float[] sheetGeom(TestGui.Layout l) {

      float v2 = l.iconW + l.listW + l.mainW;

      float v3 = 68.0F * S;
      float[] v7 = new float[]{l.iconX, l.iconY + l.iconH + 4.0F * S, 0.0F, 0.0F};

      v7[2] = v2;
      v7[3] = v3;
      return v7;
   }

   private void renderThemeSheet(TestGui.Layout l, float g, float openK) {
      float[] v4 = this.sheetGeom(l);
      float v5 = v4[0];
      float v6 = v4[1];
      float v7 = v4[2];
      float v8 = v4[3];
      yt v9 = hu.sf_regular;
      yt v10 = hu.sf_bold;
      dt.enable(v5, v6, v7, Math.max(1.0F, v8 * openK), 2.0F);
      vs.ys.glow(v5, v6, v7, v8, up.getColor(0, 0.15F * g), 8.0F * S, 15.0F, 1.0F);
      vs.ws.blur(v5, v6, v7, v8, g, 8.0F * S, up.multAlpha(up.multDark(up.background(), 0.55F), g));
      vs.ys.rect(v5, v6, v7, v8, up.getColor(0, 0.1F * g), 8.0F * S);
      Identifier result = Identifier.method_60655("client", "textures/frame/rectgui.png");

      vs.xs.texture(result, v5, v6, v7, v8, up.multAlpha(up.client(), g * 0.5F));
      vs.ys.outline(v5, v6, v7, v8, 0.5F * S, up.getColor(255, (int)(18.0F * g)), 8.0F * S);
      vr.rect(v5 + 14.0F * S, v6, v7 - 28.0F * S, 1.0F * S, up.replAlpha(up.client(), 0.45F * g), 0.5F * S);
      Theme[] v11 = ko.themes != null ? ko.themes : Theme.values();
      v10.draw("Оформление", v5 + 8.0F * S, v6 + 7.0F * S, 6.5F * S, up.getColor(255, 0.8F * g));
      int v22 = v11.length;
      String v12 = v22 + " тем";
      v9.draw(v12, v5 + v7 - 8.0F * S - v9.getWidth(v12, 5.0F * S), v6 + 8.0F * S, 5.0F * S, up.getColor(255, 0.38F * g));
      vr.rect(v5 + 8.0F * S, v6 + 18.0F * S, v7 - 16.0F * S, 0.5F * S, up.getColor(255, (int)(14.0F * g)), 0.25F * S);

      for (int v13 = 0; v13 < v11.length; v13++) {
         Theme v14 = v11[v13];
         float[] v15 = this.sheetSwatch(v4, v13);
         boolean v16 = v14 == ko.selectedTheme;
         boolean v17 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v15[0], v15[1], v15[2], v15[3]);
         v14.animation.setDirection(!v17 && !v16 ? cp.BACKWARDS : cp.FORWARDS);
         float v18 = v14.animation.getOutput();
         if (v16) {
            vr.glow(v15[0], v15[1], v15[2], v15[3], up.replAlpha(up.client(), 0.22F * g), 5.0F * S, 6.0F * S, 0.8F);
            vr.rect(v15[0], v15[1], v15[2], v15[3], up.replAlpha(up.client(), 0.24F * g), 5.0F * S);
            vr.outline(v15[0], v15[1], v15[2], v15[3], 0.75F * S, up.replAlpha(up.client(), 0.85F * g), 5.0F * S);
         } else {
            vr.rect(v15[0], v15[1], v15[2], v15[3], up.getColor(255, (int)((7.0F + 14.0F * v18) * g)), 5.0F * S);
            if (v18 > 0.02F) {
               vr.outline(v15[0], v15[1], v15[2], v15[3], 0.5F * S, up.getColor(255, (int)(34.0F * g * v18)), 5.0F * S);
            }
         }

         float v19 = 6.0F * S;
         float v20 = v15[0] + 5.0F * S;
         float v21 = v15[1] + (v15[3] - v19) / 2.0F;
         vr.glow(
            v20 - 1.0F * S,
            v21 - 1.0F * S,
            v19 + 2.0F * S,
            v19 + 2.0F * S,
            up.replAlpha(v14.getClient(), (v16 ? 0.55F : 0.25F + 0.25F * v18) * g),
            v19,
            3.0F * S,
            0.7F
         );
         vr.rect(v20, v21, v19, v19, up.replAlpha(v14.getClient(), g), v19);
         vr.outline(v20, v21, v19, v19, 0.5F * S, up.getColor(255, (int)(70.0F * g)), v19);
         v9.drawFadingText(
            v14.getName(),
            v20 + v19 + 4.0F * S,
            v15[1] + 5.5F * S,
            v15[2] - v19 - 14.0F * S,
            up.getColor(255, (v16 ? 0.95F : 0.5F + 0.3F * v18) * g),
            5.5F * S
         );
      }

      float[] v25 = this.sheetEditorBtn(v4);
      boolean v26 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v25[0], v25[1], v25[2], v25[3]);
      this.editorBtnAnim.setDirection(v26 ? cp.FORWARDS : cp.BACKWARDS);
      float v27 = this.editorBtnAnim.getOutput();
      if (v27 > 0.02F) {
         vr.glow(v25[0], v25[1], v25[2], v25[3], up.replAlpha(up.client(), 0.25F * g * v27), 4.0F * S, 6.0F * S, 0.8F);
      }

      vr.rect(v25[0], v25[1], v25[2], v25[3], up.replAlpha(up.client(), (0.16F + 0.22F * v27) * g), 4.0F * S);
      vr.outline(v25[0], v25[1], v25[2], v25[3], 0.5F * S, up.replAlpha(up.client(), (0.35F + 0.45F * v27) * g), 4.0F * S);
      String v28 = "Редактор темы";
      float v29 = 6.0F * S;
      float v30 = v9.getWidth(v28, 6.0F * S);
      float v31 = v29 + 4.0F * S + v30;
      float v32 = v25[0] + v25[2] / 2.0F - v31 / 2.0F;
      hu.icon.draw("S", v32, v25[1] + 3.0F * S, v29, up.replAlpha(up.client(), g));

      v10.draw(v28, v32 + v29 + 4.0F * S, v25[1] + 2.5F * S, 6.0F * S, up.getColor(255, (0.75F + 0.2F * v27) * g));
      dt.disable();
   }

   private static float smoothstep(float e0, float e1, float v) {
      float result = (v - e0) / (e1 - e0);

      float v3 = MathHelper.method_15363(result, 0.0F, 1.0F);
      return v3 * v3 * (3.0F - 2.0F * v3);
   }

   private float[] sheetEditorBtn(float[] sg) {
      float[] result = new float[]{sg[0] + 8.0F * S, sg[1] + sg[3] - 19.0F * S, 0.0F, 0.0F};
      float temp3 = sg[2];

      result[2] = temp3 - 16.0F * S;
      result[3] = 12.0F * S;
      return result;
   }

   private String query() {
      return this.searchQuery.trim().toLowerCase();
   }

   public boolean method_25403(Click click, double deltaX, double deltaY) {
      ro v6 = so.active();
      if (v6 != null) {

         return v6.mouseDragged((float)(deltaX / (double)this.scaleFix), (float)(deltaY / (double)this.scaleFix));
      } else {
         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public TestGui() {
      super(Text.method_43470("TestGui"));
      this.active = Category.COMBAT;
      this.select = null;
      this.listScrollTarget = 0.0F;
      this.listScrollAnim = 0.0F;
      this.listMaxScroll = 0.0F;
      this.setScrollTarget = 0.0F;
      this.setScrollAnim = 0.0F;
      this.setMaxScroll = 0.0F;
      this.draggingSlider = null;
      this.activeBind = null;
      this.bindingModule = null;
      this.activeString = null;
      this.stringBuffer = "";
      this.draggingColor = null;
      this.draggingColorBar = 0;
      this.draggingColorX = 0.0F;
      this.draggingColorW = 0.0F;
      this.searchActive = false;
      this.searchQuery = "";
      this.searchTypeTime = System.currentTimeMillis();
      this.themeSheetOpen = false;
      this.themeSheetAnim = new ep(280, 1.0, cp.BACKWARDS);
      this.gearHoverAnim = new ep(250, 1.0);
      this.animListReset = new ep(320, 1.0);
      this.animContentReset = new ep(280, 1.0);
      this.lastSelect = null;
      this.editorBtnAnim = new ep(220, 1.0);
      this.hoverCategoryTip = null;
      this.currentTab = 0;
      this.themeWaveActive = false;
      this.themeWaveStart = 0L;
      this.themeWaveOX = 0.0F;
      this.themeWaveOY = 0.0F;
      this.themeWaveTo = 0;
      this.grayscalePipeline = new fs();
      this.dotsPipeline = new ur();
      this.scanLinesPipeline = new ct();
      this.raysPipeline = new qs();
      this.halftonePipeline = new gs();
      this.particles = new ArrayList<>();
      this.particleDelay = -7274084783040271003L + 7274084783040271093L;
   }

   public void openHandsEditor() {
      this.beforeEditorOpen();
      go.getInstance().open();
      tq.editor();
   }

   protected void method_25426() {
      this.exit = false;

      this.searchActive = false;
      this.searchTypeTime = System.currentTimeMillis();
      this.bindingModule = null;

      this.activeBind = null;
      this.activeString = null;
      this.draggingSlider = null;
      this.draggingColor = null;
      if (ko.selectedTheme == null) {
         ko.selectedTheme = Client.get().guiManager().getCurrentTheme();
         ko.preSelectedTheme = ko.selectedTheme;
      }

      if (ko.themes == null) {
         ko.themes = Theme.values();
      }

      this.ensureSelect();
      tq.open();
      sq.start(0.15F);
      this.glomalAnim.run(1.0, 0.25, ap.SINE_OUT);
   }

   private List<Setting<?>> visibleSettings(Module m) {
      ArrayList v2 = new ArrayList();
      if (m == null) {

         return v2;
      } else {
         Iterator v3 = m.getSettings().iterator();

         while (v3.hasNext()) {
            int v6 = 1687246605;
            int result = v6 * (v6 + 1);

            Setting v4 = (Setting)v3.next();
            if (!(v4 instanceof vf)) {
               try {
                  if (v4.getVisible().get()) {
                     v2.add(v4);
                  }
               } catch (Exception v9) {
               }
            }
         }

         return v2;
      }
   }

   private float renderSetting(yt draw, Setting<?> s, float x, float y, float w, float g) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
      // java.lang.OutOfMemoryError: Java heap space
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode.<init>(DirectNode.java:24)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode.forStat(DirectNode.java:39)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.createDirectNode(FlattenStatementsHelper.java:92)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.createDirectNode(FlattenStatementsHelper.java:75)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:478)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:257)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:478)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:474)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:458)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:474)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.buildDirectGraph(FlattenStatementsHelper.java:43)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.splitVariables(SFormsConstructor.java:72)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:52)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:224)
      //
      // Bytecode:
      // 0000: aload 2
      // 0001: instanceof q2/z/BooleanSetting
      // 0004: ifeq 0154
      // 0007: aload 2
      // 0008: checkcast q2/z/BooleanSetting
      // 000b: astore 7
      // 000d: aload 7
      // 000f: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 0012: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0015: istore 8
      // 0017: aload 7
      // 0019: getfield q2/z/qf.animation Lru/white/utils/animation/satoshi/Animation;
      // 001c: aload 0
      // 001d: getfield ru/white/screen/TestGui.lastMouseX D
      // 0020: d2f
      // 0021: aload 0
      // 0022: getfield ru/white/screen/TestGui.lastMouseY D
      // 0025: d2f
      // 0026: fload 3
      // 0027: fload 4
      // 0029: fload 5
      // 002b: ldc_w 14.0
      // 002e: getstatic ru/white/screen/TestGui.S F
      // 0031: fmul
      // 0032: invokestatic x30/fq.isHovered (FFFFFF)Z
      // 0035: ifeq 003e
      // 0038: getstatic ru/white/utils/animation/satoshi/cp.FORWARDS Lru/white/utils/animation/satoshi/cp;
      // 003b: goto 0041
      // 003e: getstatic ru/white/utils/animation/satoshi/cp.BACKWARDS Lru/white/utils/animation/satoshi/cp;
      // 0041: invokevirtual ru/white/utils/animation/satoshi/Animation.setDirection (Lru/white/utils/animation/satoshi/cp;)V
      // 0044: aload 7
      // 0046: getfield q2/z/qf.animation2 Lru/white/utils/animation/satoshi/Animation;
      // 0049: iload 8
      // 004b: ifeq 0054
      // 004e: getstatic ru/white/utils/animation/satoshi/cp.FORWARDS Lru/white/utils/animation/satoshi/cp;
      // 0051: goto 0057
      // 0054: getstatic ru/white/utils/animation/satoshi/cp.BACKWARDS Lru/white/utils/animation/satoshi/cp;
      // 0057: invokevirtual ru/white/utils/animation/satoshi/Animation.setDirection (Lru/white/utils/animation/satoshi/cp;)V
      // 005a: aload 7
      // 005c: getfield q2/z/qf.animation2 Lru/white/utils/animation/satoshi/Animation;
      // 005f: invokevirtual ru/white/utils/animation/satoshi/Animation.getOutput ()F
      // 0062: fstore 9
      // 0064: ldc_w 5.0
      // 0067: getstatic ru/white/screen/TestGui.S F
      // 006a: fmul
      // 006b: fstore 10
      // 006d: ldc_w 187799032
      // 0070: ldc_w -187798777
      // 0073: iadd
      // 0074: ldc_w 0.3
      // 0077: fload 6
      // 0079: fmul
      // 007a: invokestatic q4/z/up.getColor (IF)I
      // 007d: invokestatic q4/z/up.client ()I
      // 0080: fload 6
      // 0082: invokestatic q4/z/up.replAlpha (IF)I
      // 0085: fload 9
      // 0087: invokestatic q4/z/up.overCol (IIF)I
      // 008a: istore 11
      // 008c: fload 3
      // 008d: fload 4
      // 008f: ldc_w 4.5
      // 0092: getstatic ru/white/screen/TestGui.S F
      // 0095: fmul
      // 0096: fadd
      // 0097: fload 10
      // 0099: fload 10
      // 009b: ldc_w 0.6
      // 009e: getstatic ru/white/screen/TestGui.S F
      // 00a1: fmul
      // 00a2: iload 11
      // 00a4: fload 10
      // 00a6: fconst_2
      // 00a7: fdiv
      // 00a8: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 00ab: fload 9
      // 00ad: ldc_w 0.05
      // 00b0: fcmpl
      // 00b1: ifle 00db
      // 00b4: fload 3
      // 00b5: ldc_w 1.5
      // 00b8: getstatic ru/white/screen/TestGui.S F
      // 00DraggableWidget: fmul
      // 00bc: fadd
      // 00bd: fload 4
      // 00bf: ldc_w 6.0
      // 00c2: getstatic ru/white/screen/TestGui.S F
      // 00c5: fmul
      // 00c6: fadd
      // 00c7: fconst_2
      // 00c8: getstatic ru/white/screen/TestGui.S F
      // 00cb: fmul
      // 00EventPacket: fconst_2
      // 00cd: getstatic ru/white/screen/TestGui.S F
      // 00d0: fmul
      // 00d1: iload 11
      // 00d3: fconst_1
      // 00d4: getstatic ru/white/screen/TestGui.S F
      // 00d7: fmul
      // 00d8: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 00db: aload 1
      // 00EventKey: aload 7
      // 00de: invokevirtual q2/z/qf.getName ()Ljava/lang/String;
      // 00e1: fload 3
      // 00e2: ldc_w 9.0
      // 00e5: getstatic ru/white/screen/TestGui.S F
      // 00e8: fmul
      // 00e9: fadd
      // 00ea: fload 4
      // 00EventAttack: fconst_2
      // 00ed: getstatic ru/white/screen/TestGui.S F
      // 00f0: fmul
      // 00f1: fadd
      // 00f2: fload 5
      // 00f4: ldc_w 35.0
      // 00f7: getstatic ru/white/screen/TestGui.S F
      // 00fa: fmul
      // 00fb: fsub
      // 00EventMove: ldc_w -425074561
      // 00Category: ldc_w 425074816
      // 0102: iadd
      // 0103: ldc_w 0.6
      // 0106: ldc_w 0.3
      // 0109: fload 9
      // 010b: fmul
      // 010c: fadd
      // 010d: fload 6
      // 010f: fmul
      // 0110: invokestatic q4/z/up.getColor (IF)I
      // 0113: ldc_w 6.8
      // 0116: getstatic ru/white/screen/TestGui.S F
      // 0119: fmul
      // 011a: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 011d: aload 0
      // 011e: fload 3
      // 011f: fload 5
      // 0121: fadd
      // 0122: ldc_w 22.0
      // 0125: getstatic ru/white/screen/TestGui.S F
      // 0128: fmul
      // 0129: fsub
      // 012a: fload 4
      // 012c: ldc_w 1.5
      // 012f: getstatic ru/white/screen/TestGui.S F
      // 0132: fmul
      // 0133: fadd
      // 0134: ldc_w 20.0
      // 0137: getstatic ru/white/screen/TestGui.S F
      // 013a: fmul
      // 013b: ldc_w 10.0
      // 013e: getstatic ru/white/screen/TestGui.S F
      // 0141: fmul
      // 0142: fload 9
      // 0144: fload 6
      // 0146: invokevirtual ru/white/screen/TestGui.drawToggle (FFFFFF)V
      // 0149: fload 4
      // 014b: ldc_w 19.0
      // 014e: getstatic ru/white/screen/TestGui.S F
      // 0151: fmul
      // 0152: fadd
      // 0153: freturn
      // 0154: aload 2
      // 0155: instanceof q2/z/SliderSetting
      // 0158: ifeq 03c7
      // 015b: aload 2
      // 015c: checkcast q2/z/SliderSetting
      // 015f: astore 7
      // 0161: aload 7
      // 0163: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0166: invokevirtual java/lang/Float.floatValue ()F
      // 0169: aload 7
      // 016b: getfield q2/z/bg.min F
      // 016e: fsub
      // 016f: ldc_w 1.0E-4
      // 0172: aload 7
      // 0174: getfield q2/z/bg.max F
      // 0177: aload 7
      // 0179: getfield q2/z/bg.min F
      // 017c: fsub
      // 017d: invokestatic java/lang/Math.max (FF)F
      // 0180: fdiv
      // 0181: fconst_0
      // 0182: fconst_1
      // 0183: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 0186: fstore 8
      // 0188: aload 7
      // 018a: invokevirtual q2/z/bg.getAnimation ()Lru/white/utils/animation/Animation;
      // 018d: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 0190: pop
      // 0191: aload 7
      // 0193: invokevirtual q2/z/bg.getAnimation ()Lru/white/utils/animation/Animation;
      // 0196: fload 8
      // 0198: f2d
      // 0199: ldc2_w 0.05999999865889549
      // 019c: getstatic ru/white/utils/animation/ap.LINEAR Lru/white/utils/animation/zo;
      // 019f: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 01a2: pop
      // 01a3: aload 7
      // 01a5: invokevirtual q2/z/bg.getAnimation ()Lru/white/utils/animation/Animation;
      // 01a8: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 01ab: fconst_0
      // 01EventMotion: fconst_1
      // 01ad: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 01b0: fstore 9
      // 01b2: ldc_w 5.5
      // 01b5: getstatic ru/white/screen/TestGui.S F
      // 01b8: fmul
      // 01b9: fstore 10
      // 01DraggableWidget: fload 3
      // 01bc: fload 4
      // 01be: ldc_w 2.5
      // 01c1: getstatic ru/white/screen/TestGui.S F
      // 01c4: fmul
      // 01c5: fadd
      // 01c6: fload 10
      // 01c8: fload 10
      // 01ConfigFile: ldc_w 0.65
      // 01cd: getstatic ru/white/screen/TestGui.S F
      // 01d0: fmul
      // 01d1: invokestatic q4/z/up.client ()I
      // 01d4: fload 6
      // 01d6: invokestatic q4/z/up.replAlpha (IF)I
      // 01d9: fload 10
      // 01db: fconst_2
      // 01EventKey: fdiv
      // 01dd: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 01e0: fload 3
      // 01e1: ldc_w 1.75
      // 01e4: getstatic ru/white/screen/TestGui.S F
      // 01e7: fmul
      // 01e8: fadd
      // 01e9: fload 4
      // 01eb: ldc_w 4.25
      // 01ee: getstatic ru/white/screen/TestGui.S F
      // 01f1: fmul
      // 01f2: fadd
      // 01f3: fconst_2
      // 01f4: getstatic ru/white/screen/TestGui.S F
      // 01f7: fmul
      // 01f8: fconst_2
      // 01f9: getstatic ru/white/screen/TestGui.S F
      // 01EventMove: fmul
      // 01fd: invokestatic q4/z/up.client ()I
      // 0200: fload 6
      // 0202: invokestatic q4/z/up.replAlpha (IF)I
      // 0205: fconst_1
      // 0206: getstatic ru/white/screen/TestGui.S F
      // 0209: fmul
      // 020a: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 020d: aload 1
      // 020e: aload 7
      // 0210: invokevirtual q2/z/bg.getName ()Ljava/lang/String;
      // 0213: fload 3
      // 0214: ldc_w 10.0
      // 0217: getstatic ru/white/screen/TestGui.S F
      // 021a: fmul
      // 021b: fadd
      // 021c: fload 4
      // 021e: fconst_1
      // 021f: getstatic ru/white/screen/TestGui.S F
      // 0222: fmul
      // 0223: fadd
      // 0224: fload 5
      // 0226: ldc_w 50.0
      // 0229: getstatic ru/white/screen/TestGui.S F
      // 022c: fmul
      // 022d: fsub
      // 022e: ldc_w 2027980480
      // 0231: ldc_w -2027980225
      // 0234: iadd
      // 0235: ldc_w 0.85
      // 0238: fload 6
      // 023a: fmul
      // 023b: invokestatic q4/z/up.getColor (IF)I
      // 023e: ldc_w 7.0
      // 0241: getstatic ru/white/screen/TestGui.S F
      // 0244: fmul
      // 0245: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0248: aload 7
      // 024a: invokestatic ru/white/screen/TestGui.fmtSlider (Lq2/z/SliderSetting;)Ljava/lang/String;
      // 024d: astore 11
      // 024f: aload 1
      // 0250: aload 11
      // 0252: fload 3
      // 0253: fload 5
      // 0255: fadd
      // 0256: aload 1
      // 0257: aload 11
      // 0259: ldc_w 6.5
      // 025c: getstatic ru/white/screen/TestGui.S F
      // 025f: fmul
      // 0260: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 0263: fsub
      // 0264: fload 4
      // 0266: fconst_1
      // 0267: getstatic ru/white/screen/TestGui.S F
      // 026a: fmul
      // 026b: fadd
      // 026c: ldc_w 6.5
      // 026f: getstatic ru/white/screen/TestGui.S F
      // 0272: fmul
      // 0273: ldc_w 1531256974
      // 0276: ldc_w -1531256719
      // 0279: iadd
      // 027a: ldc_w 0.65
      // 027d: fload 6
      // 027f: fmul
      // 0280: invokestatic q4/z/up.getColor (IF)I
      // 0283: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 0286: fload 4
      // 0288: ldc_w 15.0
      // 028b: getstatic ru/white/screen/TestGui.S F
      // 028e: fmul
      // 028f: fadd
      // 0290: fstore 12
      // 0292: ldc_w 2.5
      // 0295: getstatic ru/white/screen/TestGui.S F
      // 0298: fmul
      // 0299: fstore 13
      // 029b: fload 3
      // 029c: fstore 14
      // 029e: fload 5
      // 02a0: fstore 15
      // 02a2: fload 14
      // 02a4: fload 12
      // 02a6: fload 15
      // 02a8: fload 13
      // 02aa: ldc_w 2043276073
      // 02ad: ldc_w -2043276043
      // 02b0: iadd
      // 02b1: ldc_w -1602311649
      // 02b4: ldc_w 1602311681
      // 02b7: iadd
      // 02b8: ldc_w 1954785964
      // 02DraggableWidget: ldc_w -1954785924
      // 02be: iadd
      // 02bf: ldc_w 240.0
      // 02c2: fload 6
      // 02c4: fmul
      // 02c5: f2i
      // 02c6: invokestatic q4/z/up.getColor (IIII)I
      // 02c9: fload 13
      // 02cb: fconst_2
      // 02EventPacket: fdiv
      // 02cd: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 02d0: fload 9
      // 02d2: ldc_w 0.003
      // 02d5: fcmpl
      // 02d6: ifle 02f3
      // 02d9: fload 14
      // 02db: fload 12
      // 02dd: fload 15
      // 02df: fload 9
      // 02e1: fmul
      // 02e2: fload 13
      // 02e4: invokestatic q4/z/up.client ()I
      // 02e7: fload 6
      // 02e9: invokestatic q4/z/up.replAlpha (IF)I
      // 02EventAttack: fload 13
      // 02ee: fconst_2
      // 02ef: fdiv
      // 02f0: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 02f3: fload 14
      // 02f5: fload 15
      // 02f7: fload 9
      // 02f9: fmul
      // 02fa: fadd
      // 02fb: fstore 16
      // 02fd: fload 12
      // 02Category: fload 13
      // 0301: fconst_2
      // 0302: fdiv
      // 0303: fadd
      // 0304: fstore 17
      // 0306: ldc_w 3.2
      // 0309: getstatic ru/white/screen/TestGui.S F
      // 030c: fmul
      // 030d: fstore 18
      // 030f: fload 16
      // 0311: fload 18
      // 0313: fsub
      // 0314: fload 17
      // 0316: fload 18
      // 0318: fsub
      // 0319: fload 18
      // 031b: fconst_2
      // 031c: fmul
      // 031d: fload 18
      // 031f: fconst_2
      // 0320: fmul
      // 0321: invokestatic q4/z/up.client ()I
      // 0324: ldc_w 0.5
      // 0327: fload 6
      // 0329: fmul
      // 032a: invokestatic q4/z/up.replAlpha (IF)I
      // 032d: fload 18
      // 032f: ldc_w 3.0
      // 0332: getstatic ru/white/screen/TestGui.S F
      // 0335: fmul
      // 0336: ldc_w 0.6
      // 0339: invokestatic ru/white/utils/render/vr.glow (FFFFIFFF)V
      // 033c: fload 16
      // 033e: fload 18
      // 0340: fsub
      // 0341: fload 17
      // 0343: fload 18
      // 0345: fsub
      // 0346: fload 18
      // 0348: fconst_2
      // 0349: fmul
      // 034a: fload 18
      // 034c: fconst_2
      // 034d: fmul
      // 034e: ldc_w -1358200061
      // 0351: ldc_w 1358200316
      // 0354: iadd
      // 0355: fload 6
      // 0357: invokestatic q4/z/up.getColor (IF)I
      // 035a: fload 18
      // 035c: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 035f: aload 0
      // 0360: getfield ru/white/screen/TestGui.draggingSlider Lq2/z/SliderSetting;
      // 0363: aload 7
      // 0365: if_acmpne 03bc
      // 0368: aload 0
      // 0369: getfield ru/white/screen/TestGui.lastMouseX D
      // 036c: d2f
      // 036d: fload 14
      // 036f: fsub
      // 0370: fload 15
      // 0372: fdiv
      // 0373: fconst_0
      // 0374: fconst_1
      // 0375: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 0378: fstore 19
      // 037a: aload 7
      // 037c: getfield q2/z/bg.min F
      // 037f: fload 19
      // 0381: aload 7
      // 0383: getfield q2/z/bg.max F
      // 0386: aload 7
      // 0388: getfield q2/z/bg.min F
      // 038b: fsub
      // 038c: fmul
      // 038d: fadd
      // 038e: fstore 20
      // 0390: fload 20
      // 0392: aload 7
      // 0394: getfield q2/z/bg.increment F
      // 0397: fdiv
      // 0398: invokestatic java/lang/Math.round (F)I
      // 039b: i2f
      // 039c: aload 7
      // 039e: getfield q2/z/bg.increment F
      // 03a1: fmul
      // 03a2: fstore 21
      // 03a4: aload 7
      // 03a6: fload 21
      // 03a8: aload 7
      // 03aa: getfield q2/z/bg.min F
      // 03ad: aload 7
      // 03af: getfield q2/z/bg.max F
      // 03b2: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
      // 03b5: invokestatic java/lang/Float.valueOf (F)Ljava/lang/Float;
      // 03b8: invokevirtual q2/z/bg.set (Ljava/lang/Float;)Lq2/z/SliderSetting;
      // 03DraggableWidget: pop
      // 03bc: fload 4
      // 03be: ldc_w 26.0
      // 03c1: getstatic ru/white/screen/TestGui.S F
      // 03c4: fmul
      // 03c5: fadd
      // 03c6: freturn
      // 03c7: aload 2
      // 03c8: instanceof q2/z/ModeSetting
      // 03cb: ifeq 0806
      // 03ce: aload 2
      // 03cf: checkcast q2/z/ModeSetting
      // 03d2: astore 7
      // 03d4: ldc_w 5.0
      // 03d7: getstatic ru/white/screen/TestGui.S F
      // 03ConfigManager: fmul
      // 03db: fstore 8
      // 03dd: fload 3
      // 03de: fload 4
      // 03e0: ldc_w 4.5
      // 03e3: getstatic ru/white/screen/TestGui.S F
      // 03e6: fmul
      // 03e7: fadd
      // 03e8: fload 8
      // 03ea: fload 8
      // 03EventAttack: ldc_w 0.6
      // 03ef: getstatic ru/white/screen/TestGui.S F
      // 03f2: fmul
      // 03f3: ldc_w -1544080397
      // 03f6: ldc_w 1544080652
      // 03f9: iadd
      // 03fa: ldc_w 0.35
      // 03fd: fload 6
      // 03Category: fmul
      // 0400: invokestatic q4/z/up.getColor (IF)I
      // 0403: fload 8
      // 0405: fconst_2
      // 0406: fdiv
      // 0407: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 040a: aload 1
      // 040b: aload 7
      // 040d: invokevirtual q2/z/xf.getName ()Ljava/lang/String;
      // 0410: fload 3
      // 0411: ldc_w 9.0
      // 0414: getstatic ru/white/screen/TestGui.S F
      // 0417: fmul
      // 0418: fadd
      // 0419: fload 4
      // 041b: fconst_2
      // 041c: getstatic ru/white/screen/TestGui.S F
      // 041f: fmul
      // 0420: fadd
      // 0421: fload 5
      // 0423: ldc_w 0.45
      // 0426: fmul
      // 0427: ldc_w 193605431
      // 042a: ldc_w -193605176
      // 042d: iadd
      // 042e: ldc_w 0.65
      // 0431: fload 6
      // 0433: fmul
      // 0434: invokestatic q4/z/up.getColor (IF)I
      // 0437: ldc_w 6.8
      // 043a: getstatic ru/white/screen/TestGui.S F
      // 043d: fmul
      // 043e: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0441: aload 7
      // 0443: invokevirtual q2/z/xf.getValue ()Ljava/lang/String;
      // 0446: astore 9
      // 0448: aload 1
      // 0449: aload 9
      // 044b: ldc_w 6.0
      // 044e: getstatic ru/white/screen/TestGui.S F
      // 0451: fmul
      // 0452: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 0455: ldc_w 22.0
      // 0458: getstatic ru/white/screen/TestGui.S F
      // 045b: fmul
      // 045c: fadd
      // 045d: fload 5
      // 045f: ldc_w 0.5
      // 0462: fmul
      // 0463: invokestatic java/lang/Math.min (FF)F
      // 0466: fstore 10
      // 0468: fload 3
      // 0469: fload 5
      // 046b: fadd
      // 046c: fload 10
      // 046e: fsub
      // 046f: fstore 11
      // 0471: fload 11
      // 0473: fload 4
      // 0475: fload 10
      // 0477: ldc_w 14.0
      // 047a: getstatic ru/white/screen/TestGui.S F
      // 047d: fmul
      // 047e: ldc_w 2140121577
      // 0481: ldc_w -2140121553
      // 0484: iadd
      // 0485: ldc_w -1129508637
      // 0488: ldc_w 1129508663
      // 048b: iadd
      // 048c: ldc_w 595153896
      // 048f: ldc_w -595153863
      // 0492: iadd
      // 0493: ldc_w 220.0
      // 0496: fload 6
      // 0498: fmul
      // 0499: f2i
      // 049a: invokestatic q4/z/up.getColor (IIII)I
      // 049d: ldc_w 3.0
      // 04a0: getstatic ru/white/screen/TestGui.S F
      // 04a3: fmul
      // 04a4: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 04a7: fload 11
      // 04a9: fload 4
      // 04ab: fload 10
      // 04ad: ldc_w 14.0
      // 04b0: getstatic ru/white/screen/TestGui.S F
      // 04b3: fmul
      // 04b4: ldc_w 0.5
      // 04b7: getstatic ru/white/screen/TestGui.S F
      // 04ba: fmul
      // 04DraggableWidget: aload 7
      // 04bd: getfield q2/z/xf.opened Z
      // 04c0: ifeq 04d2
      // 04c3: invokestatic q4/z/up.client ()I
      // 04c6: ldc_w 0.7
      // 04c9: fload 6
      // 04cb: fmul
      // 04EventPacket: invokestatic q4/z/up.replAlpha (IF)I
      // 04cf: goto 04f1
      // 04d2: ldc_w -520617231
      // 04d5: ldc_w 520617273
      // 04d8: iadd
      // 04d9: ldc_w -1250684886
      // 04EventKey: ldc_w 1250684931
      // 04df: iadd
      // 04e0: ldc_w -1081454651
      // 04e3: ldc_w 1081454707
      // 04e6: iadd
      // 04e7: ldc_w 160.0
      // 04ea: fload 6
      // 04EventAttack: fmul
      // 04ed: f2i
      // 04ee: invokestatic q4/z/up.getColor (IIII)I
      // 04f1: ldc_w 3.0
      // 04f4: getstatic ru/white/screen/TestGui.S F
      // 04f7: fmul
      // 04f8: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 04fb: aload 1
      // 04EventMove: aload 9
      // 04fe: fload 11
      // 0500: ldc_w 6.0
      // 0503: getstatic ru/white/screen/TestGui.S F
      // 0506: fmul
      // 0507: fadd
      // 0508: fload 4
      // 050a: ldc_w 3.0
      // 050d: getstatic ru/white/screen/TestGui.S F
      // 0510: fmul
      // 0511: fadd
      // 0512: fload 10
      // 0514: ldc_w 18.0
      // 0517: getstatic ru/white/screen/TestGui.S F
      // 051a: fmul
      // 051b: fsub
      // 051c: ldc_w -1008136921
      // 051f: ldc_w 1008137176
      // 0522: iadd
      // 0523: ldc_w 0.85
      // 0526: fload 6
      // 0528: fmul
      // 0529: invokestatic q4/z/up.getColor (IF)I
      // 052c: ldc_w 6.2
      // 052f: getstatic ru/white/screen/TestGui.S F
      // 0532: fmul
      // 0533: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0536: aload 1
      // 0537: aload 7
      // 0539: getfield q2/z/xf.opened Z
      // 053c: ifeq 0545
      // 053f: ldc_w "∧"
      // 0542: goto 0548
      // 0545: ldc_w "∨"
      // 0548: fload 11
      // 054a: fload 10
      // 054c: fadd
      // 054d: ldc_w 10.0
      // 0550: getstatic ru/white/screen/TestGui.S F
      // 0553: fmul
      // 0554: fsub
      // 0555: fload 4
      // 0557: ldc_w 3.5
      // 055a: getstatic ru/white/screen/TestGui.S F
      // 055d: fmul
      // 055e: fadd
      // 055f: ldc_w 5.5
      // 0562: getstatic ru/white/screen/TestGui.S F
      // 0565: fmul
      // 0566: ldc_w -755736036
      // 0569: ldc_w 755736291
      // 056c: iadd
      // 056d: ldc_w 0.45
      // 0570: fload 6
      // 0572: fmul
      // 0573: invokestatic q4/z/up.getColor (IF)I
      // 0576: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 0579: fload 4
      // 057b: ldc_w 18.0
      // 057e: getstatic ru/white/screen/TestGui.S F
      // 0581: fmul
      // 0582: fadd
      // 0583: fstore 12
      // 0585: aload 7
      // 0587: getfield q2/z/xf.opened Z
      // 058a: ifeq 07fb
      // 058d: invokestatic q4/z/up.background ()I
      // 0590: istore 13
      // 0592: ldc_w -499305073
      // 0595: ldc_w 499305081
      // 0598: iadd
      // 0599: iload 13
      // 059b: ldc_w 897696801
      // 059e: ldc_w -897696785
      // 05a1: iadd
      // 05a2: ishr
      // 05a3: ldc_w -1008512014
      // 05a6: ldc_w 1008512269
      // 05a9: iadd
      // 05aa: iand
      // 05ab: i2f
      // 05EventMotion: ldc_w 0.75
      // 05af: fmul
      // 05b0: f2i
      // 05b1: invokestatic java/lang/Math.max (II)I
      // 05b4: ldc_w -1650081488
      // 05b7: ldc_w 1650081496
      // 05ba: iadd
      // 05DraggableWidget: iload 13
      // 05bd: ldc_w 1444761182
      // 05c0: ldc_w -1444761174
      // 05c3: iadd
      // 05c4: ishr
      // 05c5: ldc_w -1504225043
      // 05c8: ldc_w 1504225298
      // 05cb: iadd
      // 05EventPacket: iand
      // 05cd: i2f
      // 05ce: ldc_w 0.75
      // 05d1: fmul
      // 05d2: f2i
      // 05d3: invokestatic java/lang/Math.max (II)I
      // 05d6: ldc_w 1717421949
      // 05d9: ldc_w -1717421939
      // 05EventKey: iadd
      // 05dd: iload 13
      // 05df: ldc_w 136492687
      // 05e2: ldc_w -136492432
      // 05e5: iadd
      // 05e6: iand
      // 05e7: i2f
      // 05e8: ldc_w 0.75
      // 05eb: fmul
      // 05EventAttack: f2i
      // 05ed: invokestatic java/lang/Math.max (II)I
      // 05f0: ldc_w 245.0
      // 05f3: fload 6
      // 05f5: fmul
      // 05f6: f2i
      // 05f7: invokestatic q4/z/up.getColor (IIII)I
      // 05fa: istore 14
      // 05EventMove: ldc_w 759841266
      // 05Category: ldc_w -759841011
      // 0602: iadd
      // 0603: iload 13
      // 0605: ldc_w 1295523211
      // 0608: ldc_w -1295523195
      // 060b: iadd
      // 060c: ishr
      // 060d: ldc_w 1116300296
      // 0610: ldc_w -1116300041
      // 0613: iadd
      // 0614: iand
      // 0615: i2f
      // 0616: ldc_w 1.35
      // 0619: fmul
      // 061a: ldc_w 20.0
      // 061d: fadd
      // 061e: f2i
      // 061f: invokestatic java/lang/Math.min (II)I
      // 0622: ldc_w 375768333
      // 0625: ldc_w -375768078
      // 0628: iadd
      // 0629: iload 13
      // 062b: ldc_w -965903117
      // 062e: ldc_w 965903125
      // 0631: iadd
      // 0632: ishr
      // 0633: ldc_w -585007646
      // 0636: ldc_w 585007901
      // 0639: iadd
      // 063a: iand
      // 063b: i2f
      // 063c: ldc_w 1.35
      // 063f: fmul
      // 0640: ldc_w 20.0
      // 0643: fadd
      // 0644: f2i
      // 0645: invokestatic java/lang/Math.min (II)I
      // 0648: ldc_w -1721384401
      // 064b: ldc_w 1721384656
      // 064e: iadd
      // 064f: iload 13
      // 0651: ldc_w -44774734
      // 0654: ldc_w 44774989
      // 0657: iadd
      // 0658: iand
      // 0659: i2f
      // 065a: ldc_w 1.35
      // 065d: fmul
      // 065e: ldc_w 26.0
      // 0661: fadd
      // 0662: f2i
      // 0663: invokestatic java/lang/Math.min (II)I
      // 0666: ldc_w 160.0
      // 0669: fload 6
      // 066b: fmul
      // 066c: f2i
      // 066d: invokestatic q4/z/up.getColor (IIII)I
      // 0670: istore 15
      // 0672: aload 7
      // 0674: getfield q2/z/xf.values Ljava/util/List;
      // 0677: invokeinterface java/util/List.size ()I 1
      // 067c: ldc_w 1812083399
      // 067f: ldc_w -1812083384
      // 0682: iadd
      // 0683: imul
      // 0684: i2f
      // 0685: getstatic ru/white/screen/TestGui.S F
      // 0688: fmul
      // 0689: ldc_w 6.0
      // 068c: getstatic ru/white/screen/TestGui.S F
      // 068f: fmul
      // 0690: fadd
      // 0691: fstore 16
      // 0693: fload 3
      // 0694: fload 12
      // 0696: fconst_2
      // 0697: getstatic ru/white/screen/TestGui.S F
      // 069a: fmul
      // 069b: fsub
      // 069c: fload 5
      // 069e: fload 16
      // 06a0: iload 14
      // 06a2: ldc_w 4.0
      // 06a5: getstatic ru/white/screen/TestGui.S F
      // 06a8: fmul
      // 06a9: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 06EventMotion: fload 3
      // 06ad: fload 12
      // 06af: fconst_2
      // 06b0: getstatic ru/white/screen/TestGui.S F
      // 06b3: fmul
      // 06b4: fsub
      // 06b5: fload 5
      // 06b7: fload 16
      // 06b9: ldc_w 0.6
      // 06bc: getstatic ru/white/screen/TestGui.S F
      // 06bf: fmul
      // 06c0: iload 15
      // 06c2: ldc_w 4.0
      // 06c5: getstatic ru/white/screen/TestGui.S F
      // 06c8: fmul
      // 06c9: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 06EventPacket: aload 7
      // 06ce: getfield q2/z/xf.values Ljava/util/List;
      // 06d1: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 06d6: astore 17
      // 06d8: aload 17
      // 06ConfigManager: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 06df: ifeq 07EventAttack
      // 06e2: aload 17
      // 06e4: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 06e9: checkcast java/lang/String
      // 06EventAttack: astore 18
      // 06ee: aload 18
      // 06f0: aload 9
      // 06f2: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 06f5: istore 19
      // 06f7: aload 0
      // 06f8: getfield ru/white/screen/TestGui.lastMouseX D
      // 06fb: d2f
      // 06EventMove: aload 0
      // 06fd: getfield ru/white/screen/TestGui.lastMouseY D
      // 0700: d2f
      // 0701: fload 3
      // 0702: fconst_2
      // 0703: getstatic ru/white/screen/TestGui.S F
      // 0706: fmul
      // 0707: fadd
      // 0708: fload 12
      // 070a: fload 5
      // 070c: ldc_w 4.0
      // 070f: getstatic ru/white/screen/TestGui.S F
      // 0712: fmul
      // 0713: fsub
      // 0714: ldc_w 14.0
      // 0717: getstatic ru/white/screen/TestGui.S F
      // 071a: fmul
      // 071b: invokestatic x30/fq.isHovered (FFFFFF)Z
      // 071e: istore 20
      // 0720: iload 19
      // 0722: ifeq 0758
      // 0725: fload 3
      // 0726: fconst_2
      // 0727: getstatic ru/white/screen/TestGui.S F
      // 072a: fmul
      // 072b: fadd
      // 072c: fload 12
      // 072e: fload 5
      // 0730: ldc_w 4.0
      // 0733: getstatic ru/white/screen/TestGui.S F
      // 0736: fmul
      // 0737: fsub
      // 0738: ldc_w 14.0
      // 073b: getstatic ru/white/screen/TestGui.S F
      // 073e: fmul
      // 073f: invokestatic q4/z/up.client ()I
      // 0742: ldc_w 0.18
      // 0745: fload 6
      // 0747: fmul
      // 0748: invokestatic q4/z/up.replAlpha (IF)I
      // 074b: ldc_w 3.0
      // 074e: getstatic ru/white/screen/TestGui.S F
      // 0751: fmul
      // 0752: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0755: goto 0792
      // 0758: iload 20
      // 075a: ifeq 0792
      // 075d: fload 3
      // 075e: fconst_2
      // 075f: getstatic ru/white/screen/TestGui.S F
      // 0762: fmul
      // 0763: fadd
      // 0764: fload 12
      // 0766: fload 5
      // 0768: ldc_w 4.0
      // 076b: getstatic ru/white/screen/TestGui.S F
      // 076e: fmul
      // 076f: fsub
      // 0770: ldc_w 14.0
      // 0773: getstatic ru/white/screen/TestGui.S F
      // 0776: fmul
      // 0777: ldc_w 1002526921
      // 077a: ldc_w -1002526666
      // 077d: iadd
      // 077e: ldc_w 14.0
      // 0781: fload 6
      // 0783: fmul
      // 0784: f2i
      // 0785: invokestatic q4/z/up.getColor (II)I
      // 0788: ldc_w 3.0
      // 078b: getstatic ru/white/screen/TestGui.S F
      // 078e: fmul
      // 078f: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0792: aload 1
      // 0793: aload 18
      // 0795: fload 3
      // 0796: ldc_w 8.0
      // 0799: getstatic ru/white/screen/TestGui.S F
      // 079c: fmul
      // 079d: fadd
      // 079e: fload 12
      // 07a0: ldc_w 3.5
      // 07a3: getstatic ru/white/screen/TestGui.S F
      // 07a6: fmul
      // 07a7: fadd
      // 07a8: ldc_w 6.2
      // 07ab: getstatic ru/white/screen/TestGui.S F
      // 07ae: fmul
      // 07af: iload 19
      // 07b1: ifeq 07bf
      // 07b4: invokestatic q4/z/up.client ()I
      // 07b7: fload 6
      // 07b9: invokestatic q4/z/up.replAlpha (IF)I
      // 07bc: goto 07ConfigManager
      // 07bf: ldc_w -1753712768
      // 07c2: ldc_w 1753713023
      // 07c5: iadd
      // 07c6: iload 20
      // 07c8: ifeq 07d1
      // 07cb: ldc_w 0.9
      // 07ce: goto 07d4
      // 07d1: ldc_w 0.6
      // 07d4: fload 6
      // 07d6: fmul
      // 07d7: invokestatic q4/z/up.getColor (IF)I
      // 07ConfigManager: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 07dd: fload 12
      // 07df: ldc_w 15.0
      // 07e2: getstatic ru/white/screen/TestGui.S F
      // 07e5: fmul
      // 07e6: fadd
      // 07e7: fstore 12
      // 07e9: goto 06d8
      // 07EventAttack: fload 12
      // 07ee: ldc_w 4.0
      // 07f1: getstatic ru/white/screen/TestGui.S F
      // 07f4: fmul
      // 07f5: fadd
      // 07f6: fstore 12
      // 07f8: fload 12
      // 07fa: freturn
      // 07fb: fload 4
      // 07fd: ldc_w 19.0
      // 0800: getstatic ru/white/screen/TestGui.S F
      // 0803: fmul
      // 0804: fadd
      // 0805: freturn
      // 0806: aload 2
      // 0807: instanceof q2/z/MultiSetting
      // 080a: ifeq 0d0d
      // 080d: aload 2
      // 080e: checkcast q2/z/MultiSetting
      // 0811: astore 7
      // 0813: ldc_w 5.0
      // 0816: getstatic ru/white/screen/TestGui.S F
      // 0819: fmul
      // 081a: fstore 8
      // 081c: fload 3
      // 081d: fload 4
      // 081f: ldc_w 4.5
      // 0822: getstatic ru/white/screen/TestGui.S F
      // 0825: fmul
      // 0826: fadd
      // 0827: fload 8
      // 0829: fload 8
      // 082b: ldc_w 1028114954
      // 082e: istore 32
      // 0830: iload 32
      // 0832: dup
      // 0833: bipush 1
      // 0834: iadd
      // 0835: imul
      // 0836: bipush 2
      // 0837: irem
      // 0838: ifeq 083d
      // 083b: aconst_null
      // 083c: pop
      // 083d: nop
      // 083e: ldc_w 0.6
      // 0841: getstatic ru/white/screen/TestGui.S F
      // 0844: fmul
      // 0845: ldc_w 1057626046
      // 0848: ldc_w -1057625791
      // 084b: iadd
      // 084c: ldc_w 0.35
      // 084f: fload 6
      // 0851: fmul
      // 0852: invokestatic q4/z/up.getColor (IF)I
      // 0855: fload 8
      // 0857: fconst_2
      // 0858: fdiv
      // 0859: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 085c: aload 1
      // 085d: aload 7
      // 085f: invokevirtual q2/z/zf.getName ()Ljava/lang/String;
      // 0862: fload 3
      // 0863: ldc_w 9.0
      // 0866: getstatic ru/white/screen/TestGui.S F
      // 0869: fmul
      // 086a: fadd
      // 086b: fload 4
      // 086d: fconst_2
      // 086e: getstatic ru/white/screen/TestGui.S F
      // 0871: fmul
      // 0872: fadd
      // 0873: fload 5
      // 0875: ldc_w 0.45
      // 0878: fmul
      // 0879: ldc_w -1177435577
      // 087c: ldc_w 1177435832
      // 087f: iadd
      // 0880: ldc_w 0.65
      // 0883: fload 6
      // 0885: fmul
      // 0886: invokestatic q4/z/up.getColor (IF)I
      // 0889: ldc_w 6.8
      // 088c: getstatic ru/white/screen/TestGui.S F
      // 088f: fmul
      // 0890: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0893: aload 7
      // 0895: invokevirtual q2/z/zf.getNames ()Ljava/lang/String;
      // 0898: astore 9
      // 089a: aload 9
      // 089c: invokevirtual java/lang/String.isEmpty ()Z
      // 089f: ifeq 08a7
      // 08a2: ldc_w "Ничего"
      // 08a5: astore 9
      // 08a7: aload 1
      // 08a8: aload 9
      // 08aa: ldc_w 6.0
      // 08ad: getstatic ru/white/screen/TestGui.S F
      // 08b0: fmul
      // 08b1: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 08b4: ldc_w 22.0
      // 08b7: getstatic ru/white/screen/TestGui.S F
      // 08ba: fmul
      // 08DraggableWidget: fadd
      // 08bc: fload 5
      // 08be: ldc_w 0.5
      // 08c1: fmul
      // 08c2: invokestatic java/lang/Math.min (FF)F
      // 08c5: fstore 10
      // 08c7: fload 3
      // 08c8: fload 5
      // 08ConfigFile: fadd
      // 08cb: fload 10
      // 08cd: fsub
      // 08ce: fstore 11
      // 08d0: fload 11
      // 08d2: fload 4
      // 08d4: fload 10
      // 08d6: ldc_w 14.0
      // 08d9: getstatic ru/white/screen/TestGui.S F
      // 08EventKey: fmul
      // 08dd: ldc_w 1742117036
      // 08e0: ldc_w -1742117012
      // 08e3: iadd
      // 08e4: ldc_w 608267342
      // 08e7: ldc_w -608267316
      // 08ea: iadd
      // 08eb: ldc_w 2078896620
      // 08ee: ldc_w -2078896587
      // 08f1: iadd
      // 08f2: ldc_w 220.0
      // 08f5: fload 6
      // 08f7: fmul
      // 08f8: f2i
      // 08f9: invokestatic q4/z/up.getColor (IIII)I
      // 08EventMove: ldc_w 3.0
      // 08Category: getstatic ru/white/screen/TestGui.S F
      // 0902: fmul
      // 0903: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0906: fload 11
      // 0908: fload 4
      // 090a: fload 10
      // 090c: ldc_w 14.0
      // 090f: getstatic ru/white/screen/TestGui.S F
      // 0912: fmul
      // 0913: ldc_w 0.5
      // 0916: getstatic ru/white/screen/TestGui.S F
      // 0919: fmul
      // 091a: aload 7
      // 091c: getfield q2/z/zf.opened Z
      // 091f: ifeq 0931
      // 0922: invokestatic q4/z/up.client ()I
      // 0925: ldc_w 0.7
      // 0928: fload 6
      // 092a: fmul
      // 092b: invokestatic q4/z/up.replAlpha (IF)I
      // 092e: goto 0950
      // 0931: ldc_w 2122658229
      // 0934: ldc_w -2122658187
      // 0937: iadd
      // 0938: ldc_w 376451689
      // 093b: ldc_w -376451644
      // 093e: iadd
      // 093f: ldc_w 557726404
      // 0942: ldc_w -557726348
      // 0945: iadd
      // 0946: ldc_w 160.0
      // 0949: fload 6
      // 094b: fmul
      // 094c: f2i
      // 094d: invokestatic q4/z/up.getColor (IIII)I
      // 0950: ldc_w 3.0
      // 0953: getstatic ru/white/screen/TestGui.S F
      // 0956: fmul
      // 0957: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 095a: aload 1
      // 095b: aload 9
      // 095d: fload 11
      // 095f: ldc_w 6.0
      // 0962: getstatic ru/white/screen/TestGui.S F
      // 0965: fmul
      // 0966: fadd
      // 0967: fload 4
      // 0969: ldc_w 3.0
      // 096c: getstatic ru/white/screen/TestGui.S F
      // 096f: fmul
      // 0970: fadd
      // 0971: fload 10
      // 0973: ldc_w 18.0
      // 0976: getstatic ru/white/screen/TestGui.S F
      // 0979: fmul
      // 097a: fsub
      // 097b: ldc_w 1290504187
      // 097e: ldc_w -1290503932
      // 0981: iadd
      // 0982: ldc_w 0.85
      // 0985: fload 6
      // 0987: fmul
      // 0988: invokestatic q4/z/up.getColor (IF)I
      // 098b: ldc_w 6.2
      // 098e: getstatic ru/white/screen/TestGui.S F
      // 0991: fmul
      // 0992: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0995: aload 1
      // 0996: aload 7
      // 0998: getfield q2/z/zf.opened Z
      // 099b: ifeq 09a4
      // 099e: ldc_w "∧"
      // 09a1: goto 09a7
      // 09a4: ldc_w "∨"
      // 09a7: fload 11
      // 09a9: fload 10
      // 09ab: fadd
      // 09EventMotion: ldc_w 10.0
      // 09af: getstatic ru/white/screen/TestGui.S F
      // 09b2: fmul
      // 09b3: fsub
      // 09b4: fload 4
      // 09b6: ldc_w 3.5
      // 09b9: getstatic ru/white/screen/TestGui.S F
      // 09bc: fmul
      // 09bd: fadd
      // 09be: ldc_w 5.5
      // 09c1: getstatic ru/white/screen/TestGui.S F
      // 09c4: fmul
      // 09c5: ldc_w 1832777198
      // 09c8: ldc_w -1832776943
      // 09cb: iadd
      // 09EventPacket: ldc_w 0.45
      // 09cf: fload 6
      // 09d1: fmul
      // 09d2: invokestatic q4/z/up.getColor (IF)I
      // 09d5: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 09d8: fload 4
      // 09ConfigManager: ldc_w 18.0
      // 09dd: getstatic ru/white/screen/TestGui.S F
      // 09e0: fmul
      // 09e1: fadd
      // 09e2: fstore 12
      // 09e4: aload 7
      // 09e6: getfield q2/z/zf.opened Z
      // 09e9: ifeq 0d02
      // 09EventAttack: invokestatic q4/z/up.background ()I
      // 09ef: istore 13
      // 09f1: ldc_w 1340255867
      // 09f4: ldc_w -1340255859
      // 09f7: iadd
      // 09f8: iload 13
      // 09fa: ldc_w 2094787388
      // 09fd: ldc_w -2094787372
      // 0a00: iadd
      // 0a01: ishr
      // 0a02: ldc_w -114386583
      // 0a05: ldc_w 114386838
      // 0a08: iadd
      // 0a09: iand
      // 0a0a: i2f
      // 0a0b: ldc_w 0.75
      // 0a0e: fmul
      // 0a0f: f2i
      // 0a10: invokestatic java/lang/Math.max (II)I
      // 0a13: ldc_w 60258523
      // 0a16: ldc_w -60258515
      // 0a19: iadd
      // 0a1a: iload 13
      // 0a1c: ldc_w 1210753113
      // 0a1f: ldc_w -1210753105
      // 0a22: iadd
      // 0a23: ishr
      // 0a24: ldc_w -1814584879
      // 0a27: ldc_w 1814585134
      // 0a2a: iadd
      // 0a2b: iand
      // 0a2c: i2f
      // 0a2d: ldc_w 0.75
      // 0a30: fmul
      // 0a31: f2i
      // 0a32: invokestatic java/lang/Math.max (II)I
      // 0a35: ldc_w 612587137
      // 0a38: ldc_w -612587127
      // 0a3b: iadd
      // 0a3c: iload 13
      // 0a3e: ldc_w 1754665773
      // 0a41: ldc_w -1754665518
      // 0a44: iadd
      // 0a45: iand
      // 0a46: i2f
      // 0a47: ldc_w 0.75
      // 0a4a: fmul
      // 0a4b: f2i
      // 0a4c: invokestatic java/lang/Math.max (II)I
      // 0a4f: ldc_w 245.0
      // 0a52: fload 6
      // 0a54: fmul
      // 0a55: f2i
      // 0a56: invokestatic q4/z/up.getColor (IIII)I
      // 0a59: istore 14
      // 0a5b: ldc_w 1223995424
      // 0a5e: ldc_w -1223995169
      // 0a61: iadd
      // 0a62: iload 13
      // 0a64: ldc_w 713298226
      // 0a67: ldc_w -713298210
      // 0a6a: iadd
      // 0a6b: ishr
      // 0a6c: ldc_w 177814680
      // 0a6f: ldc_w -177814425
      // 0a72: iadd
      // 0a73: iand
      // 0a74: i2f
      // 0a75: ldc_w 1.35
      // 0a78: fmul
      // 0a79: ldc_w 20.0
      // 0a7c: fadd
      // 0a7d: f2i
      // 0a7e: invokestatic java/lang/Math.min (II)I
      // 0a81: ldc_w -2145424055
      // 0a84: ldc_w 2145424310
      // 0a87: iadd
      // 0a88: iload 13
      // 0a8a: ldc_w 468327023
      // 0a8d: ldc_w -468327015
      // 0a90: iadd
      // 0a91: ishr
      // 0a92: ldc_w 141739705
      // 0a95: ldc_w -141739450
      // 0a98: iadd
      // 0a99: iand
      // 0a9a: i2f
      // 0a9b: ldc_w 1.35
      // 0a9e: fmul
      // 0a9f: ldc_w 20.0
      // 0aa2: fadd
      // 0aa3: f2i
      // 0aa4: invokestatic java/lang/Math.min (II)I
      // 0aa7: ldc_w 1524494994
      // 0aaa: ldc_w -1524494739
      // 0aad: iadd
      // 0aae: iload 13
      // 0ab0: ldc_w -136086657
      // 0ab3: ldc_w 136086912
      // 0ab6: iadd
      // 0ab7: iand
      // 0ab8: i2f
      // 0ab9: ldc_w 1.35
      // 0abc: fmul
      // 0abd: ldc_w 26.0
      // 0ac0: fadd
      // 0ac1: f2i
      // 0ac2: invokestatic java/lang/Math.min (II)I
      // 0ac5: ldc_w 160.0
      // 0ac8: fload 6
      // 0aca: fmul
      // 0acb: f2i
      // 0acc: invokestatic q4/z/up.getColor (IIII)I
      // 0acf: istore 15
      // 0ad1: aload 7
      // 0ad3: invokevirtual q2/z/zf.getValues ()Ljava/util/Collection;
      // 0ad6: invokeinterface java/util/Collection.size ()I 1
      // 0adb: ldc_w 1953155729
      // 0ade: ldc_w -1953155714
      // 0ae1: iadd
      // 0ae2: imul
      // 0ae3: i2f
      // 0ae4: getstatic ru/white/screen/TestGui.S F
      // 0ae7: fmul
      // 0ae8: ldc_w 6.0
      // 0aeb: getstatic ru/white/screen/TestGui.S F
      // 0aee: fmul
      // 0aef: fadd
      // 0af0: fstore 16
      // 0af2: fload 3
      // 0af3: fload 12
      // 0af5: fconst_2
      // 0af6: getstatic ru/white/screen/TestGui.S F
      // 0af9: fmul
      // 0afa: fsub
      // 0afb: fload 5
      // 0afd: fload 16
      // 0aff: iload 14
      // 0b01: ldc_w 4.0
      // 0b04: getstatic ru/white/screen/TestGui.S F
      // 0b07: fmul
      // 0b08: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0b0b: fload 3
      // 0b0c: fload 12
      // 0b0e: fconst_2
      // 0b0f: getstatic ru/white/screen/TestGui.S F
      // 0b12: fmul
      // 0b13: fsub
      // 0b14: fload 5
      // 0b16: fload 16
      // 0b18: ldc_w 0.6
      // 0b1b: getstatic ru/white/screen/TestGui.S F
      // 0b1e: fmul
      // 0b1f: iload 15
      // 0b21: ldc_w 4.0
      // 0b24: getstatic ru/white/screen/TestGui.S F
      // 0b27: fmul
      // 0b28: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 0b2b: aload 7
      // 0b2d: invokevirtual q2/z/zf.getValues ()Ljava/util/Collection;
      // 0b30: invokeinterface java/util/Collection.iterator ()Ljava/util/Iterator; 1
      // 0b35: astore 17
      // 0b37: aload 17
      // 0b39: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 0b3e: ifeq 0cf3
      // 0b41: aload 17
      // 0b43: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 0b48: checkcast q2/z/BooleanSetting
      // 0b4b: astore 18
      // 0b4d: aload 18
      // 0b4f: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 0b52: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0b55: istore 19
      // 0b57: aload 0
      // 0b58: getfield ru/white/screen/TestGui.lastMouseX D
      // 0b5b: d2f
      // 0b5c: aload 0
      // 0b5d: getfield ru/white/screen/TestGui.lastMouseY D
      // 0b60: d2f
      // 0b61: fload 3
      // 0b62: fconst_2
      // 0b63: getstatic ru/white/screen/TestGui.S F
      // 0b66: fmul
      // 0b67: fadd
      // 0b68: fload 12
      // 0b6a: fload 5
      // 0b6c: ldc_w 4.0
      // 0b6f: getstatic ru/white/screen/TestGui.S F
      // 0b72: fmul
      // 0b73: fsub
      // 0b74: ldc_w 14.0
      // 0b77: getstatic ru/white/screen/TestGui.S F
      // 0b7a: fmul
      // 0b7b: invokestatic x30/fq.isHovered (FFFFFF)Z
      // 0b7e: istore 20
      // 0b80: iload 19
      // 0b82: ifeq 0bb8
      // 0b85: fload 3
      // 0b86: fconst_2
      // 0b87: getstatic ru/white/screen/TestGui.S F
      // 0b8a: fmul
      // 0b8b: fadd
      // 0b8c: fload 12
      // 0b8e: fload 5
      // 0b90: ldc_w 4.0
      // 0b93: getstatic ru/white/screen/TestGui.S F
      // 0b96: fmul
      // 0b97: fsub
      // 0b98: ldc_w 14.0
      // 0b9b: getstatic ru/white/screen/TestGui.S F
      // 0b9e: fmul
      // 0b9f: invokestatic q4/z/up.client ()I
      // 0ba2: ldc_w 0.14
      // 0ba5: fload 6
      // 0ba7: fmul
      // 0ba8: invokestatic q4/z/up.replAlpha (IF)I
      // 0bab: ldc_w 3.0
      // 0bae: getstatic ru/white/screen/TestGui.S F
      // 0bb1: fmul
      // 0bb2: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0bb5: goto 0bf2
      // 0bb8: iload 20
      // 0bba: ifeq 0bf2
      // 0bbd: fload 3
      // 0bbe: fconst_2
      // 0bbf: getstatic ru/white/screen/TestGui.S F
      // 0bc2: fmul
      // 0bc3: fadd
      // 0bc4: fload 12
      // 0bc6: fload 5
      // 0bc8: ldc_w 4.0
      // 0bcb: getstatic ru/white/screen/TestGui.S F
      // 0bce: fmul
      // 0bcf: fsub
      // 0bd0: ldc_w 14.0
      // 0bd3: getstatic ru/white/screen/TestGui.S F
      // 0bd6: fmul
      // 0bd7: ldc_w -2124569669
      // 0bda: ldc_w 2124569924
      // 0bdd: iadd
      // 0bde: ldc_w 14.0
      // 0be1: fload 6
      // 0be3: fmul
      // 0be4: f2i
      // 0be5: invokestatic q4/z/up.getColor (II)I
      // 0be8: ldc_w 3.0
      // 0beb: getstatic ru/white/screen/TestGui.S F
      // 0bee: fmul
      // 0bef: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0bf2: ldc_w 14.0
      // 0bf5: getstatic ru/white/screen/TestGui.S F
      // 0bf8: fmul
      // 0bf9: fstore 21
      // 0bfb: ldc_w 6.5
      // 0bfe: getstatic ru/white/screen/TestGui.S F
      // 0c01: fmul
      // 0c02: fstore 22
      // 0c04: fload 12
      // 0c06: fload 21
      // 0c08: fload 22
      // 0c0a: fsub
      // 0c0b: fconst_2
      // 0c0c: fdiv
      // 0c0d: fadd
      // 0c0e: fstore 23
      // 0c10: fload 3
      // 0c11: ldc_w 6.0
      // 0c14: getstatic ru/white/screen/TestGui.S F
      // 0c17: fmul
      // 0c18: fadd
      // 0c19: fload 23
      // 0c1b: fload 22
      // 0c1d: fload 22
      // 0c1f: ldc_w 0.6
      // 0c22: getstatic ru/white/screen/TestGui.S F
      // 0c25: fmul
      // 0c26: iload 19
      // 0c28: ifeq 0c3a
      // 0c2b: invokestatic q4/z/up.client ()I
      // 0c2e: ldc_w 0.9
      // 0c31: fload 6
      // 0c33: fmul
      // 0c34: invokestatic q4/z/up.replAlpha (IF)I
      // 0c37: goto 0c4b
      // 0c3a: ldc_w -128489045
      // 0c3d: ldc_w 128489300
      // 0c40: iadd
      // 0c41: ldc_w 90.0
      // 0c44: fload 6
      // 0c46: fmul
      // 0c47: f2i
      // 0c48: invokestatic q4/z/up.getColor (II)I
      // 0c4b: fload 22
      // 0c4d: fconst_2
      // 0c4e: fdiv
      // 0c4f: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 0c52: iload 19
      // 0c54: ifeq 0c8a
      // 0c57: fload 3
      // 0c58: ldc_w 7.75
      // 0c5b: getstatic ru/white/screen/TestGui.S F
      // 0c5e: fmul
      // 0c5f: fadd
      // 0c60: fload 23
      // 0c62: ldc_w 1.75
      // 0c65: getstatic ru/white/screen/TestGui.S F
      // 0c68: fmul
      // 0c69: fadd
      // 0c6a: ldc_w 3.0
      // 0c6d: getstatic ru/white/screen/TestGui.S F
      // 0c70: fmul
      // 0c71: ldc_w 3.0
      // 0c74: getstatic ru/white/screen/TestGui.S F
      // 0c77: fmul
      // 0c78: invokestatic q4/z/up.client ()I
      // 0c7b: fload 6
      // 0c7d: invokestatic q4/z/up.replAlpha (IF)I
      // 0c80: ldc_w 1.5
      // 0c83: getstatic ru/white/screen/TestGui.S F
      // 0c86: fmul
      // 0c87: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0c8a: fload 12
      // 0c8c: fload 21
      // 0c8e: ldc_w 6.2
      // 0c91: getstatic ru/white/screen/TestGui.S F
      // 0c94: fmul
      // 0c95: fsub
      // 0c96: fconst_2
      // 0c97: fdiv
      // 0c98: fadd
      // 0c99: ldc_w 0.5
      // 0c9c: getstatic ru/white/screen/TestGui.S F
      // 0c9f: fmul
      // 0ca0: fadd
      // 0ca1: fstore 24
      // 0ca3: aload 1
      // 0ca4: aload 18
      // 0ca6: invokevirtual q2/z/qf.getName ()Ljava/lang/String;
      // 0ca9: fload 3
      // 0caa: ldc_w 16.0
      // 0cad: getstatic ru/white/screen/TestGui.S F
      // 0cb0: fmul
      // 0cb1: fadd
      // 0cb2: fload 24
      // 0cb4: ldc_w 6.2
      // 0cb7: getstatic ru/white/screen/TestGui.S F
      // 0cba: fmul
      // 0cbb: ldc_w 492603778
      // 0cbe: ldc_w -492603523
      // 0cc1: iadd
      // 0cc2: iload 19
      // 0cc4: ifeq 0ccd
      // 0cc7: ldc_w 0.95
      // 0cca: goto 0cdb
      // 0ccd: iload 20
      // 0ccf: ifeq 0cd8
      // 0cd2: ldc_w 0.8
      // 0cd5: goto 0cdb
      // 0cd8: ldc_w 0.55
      // 0cdb: fload 6
      // 0cdd: fmul
      // 0cde: invokestatic q4/z/up.getColor (IF)I
      // 0ce1: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 0ce4: fload 12
      // 0ce6: ldc_w 15.0
      // 0ce9: getstatic ru/white/screen/TestGui.S F
      // 0cec: fmul
      // 0ced: fadd
      // 0cee: fstore 12
      // 0cf0: goto 0b37
      // 0cf3: fload 12
      // 0cf5: ldc_w 4.0
      // 0cf8: getstatic ru/white/screen/TestGui.S F
      // 0cfb: fmul
      // 0cfc: fadd
      // 0cfd: fstore 12
      // 0cff: fload 12
      // 0d01: freturn
      // 0d02: fload 4
      // 0d04: ldc_w 19.0
      // 0d07: getstatic ru/white/screen/TestGui.S F
      // 0d0a: fmul
      // 0d0b: fadd
      // 0d0c: freturn
      // 0d0d: aload 2
      // 0d0e: instanceof q2/z/SettingVisibility
      // 0d11: ifeq 0e33
      // 0d14: aload 2
      // 0d15: checkcast q2/z/SettingVisibility
      // 0d18: astore 7
      // 0d1a: aload 0
      // 0d1b: getfield ru/white/screen/TestGui.activeBind Lq2/z/SettingVisibility;
      // 0d1e: aload 7
      // 0d20: if_acmpne 0d27
      // 0d23: bipush 1
      // 0d24: goto 0d28
      // 0d27: bipush 0
      // 0d28: istore 8
      // 0d2a: iload 8
      // 0d2c: ifeq 0d35
      // 0d2f: ldc_w "..."
      // 0d32: goto 0d52
      // 0d35: aload 7
      // 0d37: invokevirtual q2/z/of.get ()Ljava/lang/Integer;
      // 0d3a: invokevirtual java/lang/Integer.intValue ()I
      // 0d3d: bipush -1
      // 0d3e: if_icmpne 0d47
      // 0d41: ldc_w "NONE"
      // 0d44: goto 0d52
      // 0d47: aload 7
      // 0d49: invokevirtual q2/z/of.get ()Ljava/lang/Integer;
      // 0d4c: invokevirtual java/lang/Integer.intValue ()I
      // 0d4f: invokestatic x30/eq.keyName (I)Ljava/lang/String;
      // 0d52: astore 9
      // 0d54: aload 1
      // 0d55: aload 7
      // 0d57: invokevirtual q2/z/of.getName ()Ljava/lang/String;
      // 0d5a: fload 3
      // 0d5b: fload 4
      // 0d5d: fconst_2
      // 0d5e: getstatic ru/white/screen/TestGui.S F
      // 0d61: fmul
      // 0d62: fadd
      // 0d63: fload 5
      // 0d65: ldc_w 50.0
      // 0d68: getstatic ru/white/screen/TestGui.S F
      // 0d6b: fmul
      // 0d6c: fsub
      // 0d6d: ldc_w 1053586145
      // 0d70: ldc_w -1053585890
      // 0d73: iadd
      // 0d74: ldc_w 0.6
      // 0d77: fload 6
      // 0d79: fmul
      // 0d7a: invokestatic q4/z/up.getColor (IF)I
      // 0d7d: ldc_w 6.5
      // 0d80: getstatic ru/white/screen/TestGui.S F
      // 0d83: fmul
      // 0d84: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0d87: aload 1
      // 0d88: aload 9
      // 0d8a: ldc_w 6.0
      // 0d8d: getstatic ru/white/screen/TestGui.S F
      // 0d90: fmul
      // 0d91: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 0d94: ldc_w 12.0
      // 0d97: getstatic ru/white/screen/TestGui.S F
      // 0d9a: fmul
      // 0d9b: fadd
      // 0d9c: fstore 10
      // 0d9e: fload 3
      // 0d9f: fload 5
      // 0da1: fadd
      // 0da2: fload 10
      // 0da4: fsub
      // 0da5: fstore 11
      // 0da7: fload 11
      // 0da9: fload 4
      // 0dab: fload 10
      // 0dad: ldc_w 12.0
      // 0db0: getstatic ru/white/screen/TestGui.S F
      // 0db3: fmul
      // 0db4: ldc_w 387848225
      // 0db7: ldc_w -387847970
      // 0dba: iadd
      // 0dbb: ldc_w 14.0
      // 0dbe: fload 6
      // 0dc0: fmul
      // 0dc1: f2i
      // 0dc2: invokestatic q4/z/up.getColor (II)I
      // 0dc5: ldc_w 3.0
      // 0dc8: getstatic ru/white/screen/TestGui.S F
      // 0dcb: fmul
      // 0dcc: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0dcf: iload 8
      // 0dd1: ifeq 0dfa
      // 0dd4: fload 11
      // 0dd6: fload 4
      // 0dd8: fload 10
      // 0dda: ldc_w 12.0
      // 0ddd: getstatic ru/white/screen/TestGui.S F
      // 0de0: fmul
      // 0de1: ldc_w 0.5
      // 0de4: getstatic ru/white/screen/TestGui.S F
      // 0de7: fmul
      // 0de8: invokestatic q4/z/up.client ()I
      // 0deb: fload 6
      // 0ded: invokestatic q4/z/up.replAlpha (IF)I
      // 0df0: ldc_w 3.0
      // 0df3: getstatic ru/white/screen/TestGui.S F
      // 0df6: fmul
      // 0df7: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 0dfa: aload 1
      // 0dfb: aload 9
      // 0dfd: fload 11
      // 0dff: fload 10
      // 0e01: fconst_2
      // 0e02: fdiv
      // 0e03: fadd
      // 0e04: fload 4
      // 0e06: ldc_w 2.5
      // 0e09: getstatic ru/white/screen/TestGui.S F
      // 0e0c: fmul
      // 0e0d: fadd
      // 0e0e: ldc_w 6.0
      // 0e11: getstatic ru/white/screen/TestGui.S F
      // 0e14: fmul
      // 0e15: ldc_w 1727579764
      // 0e18: ldc_w -1727579509
      // 0e1b: iadd
      // 0e1c: ldc_w 0.65
      // 0e1f: fload 6
      // 0e21: fmul
      // 0e22: invokestatic q4/z/up.getColor (IF)I
      // 0e25: invokevirtual x6/y34/yt.drawCentered (Ljava/lang/String;FFFI)V
      // 0e28: fload 4
      // 0e2a: ldc_w 19.0
      // 0e2d: getstatic ru/white/screen/TestGui.S F
      // 0e30: fmul
      // 0e31: fadd
      // 0e32: freturn
      // 0e33: aload 2
      // 0e34: instanceof q2/z/StringSetting
      // 0e37: ifeq 0f9e
      // 0e3a: aload 2
      // 0e3b: checkcast q2/z/StringSetting
      // 0e3e: astore 7
      // 0e40: aload 0
      // 0e41: getfield ru/white/screen/TestGui.activeString Lq2/z/StringSetting;
      // 0e44: aload 7
      // 0e46: if_acmpne 0e4d
      // 0e49: bipush 1
      // 0e4a: goto 0e4e
      // 0e4d: bipush 0
      // 0e4e: istore 8
      // 0e50: iload 8
      // 0e52: ifeq 0e95
      // 0e55: aload 0
      // 0e56: getfield ru/white/screen/TestGui.stringBuffer Ljava/lang/String;
      // 0e59: invokestatic java/lang/System.currentTimeMillis ()J
      // 0e5c: ldc2_w -288220394735253654
      // 0e5f: ldc2_w 288220394735254054
      // 0e62: ladd
      // 0e63: ldiv
      // 0e64: ldc2_w -2934954737064323558
      // 0e67: ldc2_w 2934954737064323560
      // 0e6a: ladd
      // 0e6b: lrem
      // 0e6c: lconst_0
      // 0e6d: lcmp
      // 0e6e: ifne 0e77
      // 0e71: ldc_w "_"
      // 0e74: goto 0e7a
      // 0e77: ldc_w ""
      // 0e7a: astore 29
      // 0e7c: astore 30
      // 0e7e: new java/lang/StringBuilder
      // 0e81: dup
      // 0e82: invokespecial java/lang/StringBuilder.<init> ()V
      // 0e85: aload 30
      // 0e87: invokevirtual java/lang/StringBuilder.append (Ljava/lang/Object;)Ljava/lang/StringBuilder;
      // 0e8a: aload 29
      // 0e8c: invokevirtual java/lang/StringBuilder.append (Ljava/lang/Object;)Ljava/lang/StringBuilder;
      // 0e8f: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 0e92: goto 0e9a
      // 0e95: aload 7
      // 0e97: invokevirtual q2/z/cg.getValue ()Ljava/lang/String;
      // 0e9a: astore 9
      // 0e9c: aload 9
      // 0e9e: invokevirtual java/lang/String.isEmpty ()Z
      // 0ea1: ifeq 0ea9
      // 0ea4: ldc_w "NONE"
      // 0ea7: astore 9
      // 0ea9: aload 1
      // 0eaa: aload 7
      // 0eac: invokevirtual q2/z/cg.getName ()Ljava/lang/String;
      // 0eaf: fload 3
      // 0eb0: fload 4
      // 0eb2: fconst_2
      // 0eb3: getstatic ru/white/screen/TestGui.S F
      // 0eb6: fmul
      // 0eb7: fadd
      // 0eb8: fload 5
      // 0eba: ldc_w 60.0
      // 0ebd: getstatic ru/white/screen/TestGui.S F
      // 0ec0: fmul
      // 0ec1: fsub
      // 0ec2: ldc_w -1373562880
      // 0ec5: ldc_w 1373563135
      // 0ec8: iadd
      // 0ec9: ldc_w 0.6
      // 0ecc: fload 6
      // 0ece: fmul
      // 0ecf: invokestatic q4/z/up.getColor (IF)I
      // 0ed2: ldc_w 6.5
      // 0ed5: getstatic ru/white/screen/TestGui.S F
      // 0ed8: fmul
      // 0ed9: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0edc: aload 1
      // 0edd: aload 9
      // 0edf: ldc_w 6.0
      // 0ee2: getstatic ru/white/screen/TestGui.S F
      // 0ee5: fmul
      // 0ee6: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 0ee9: ldc_w 12.0
      // 0eec: getstatic ru/white/screen/TestGui.S F
      // 0eef: fmul
      // 0ef0: fadd
      // 0ef1: fload 5
      // 0ef3: ldc_w 0.55
      // 0ef6: fmul
      // 0ef7: invokestatic java/lang/Math.min (FF)F
      // 0efa: fstore 10
      // 0efc: fload 3
      // 0efd: fload 5
      // 0eff: fadd
      // 0f00: fload 10
      // 0f02: fsub
      // 0f03: fstore 11
      // 0f05: fload 11
      // 0f07: fload 4
      // 0f09: fload 10
      // 0f0b: ldc_w 12.0
      // 0f0e: getstatic ru/white/screen/TestGui.S F
      // 0f11: fmul
      // 0f12: ldc_w -986235187
      // 0f15: ldc_w 986235442
      // 0f18: iadd
      // 0f19: ldc_w 14.0
      // 0f1c: fload 6
      // 0f1e: fmul
      // 0f1f: f2i
      // 0f20: invokestatic q4/z/up.getColor (II)I
      // 0f23: ldc_w 3.0
      // 0f26: getstatic ru/white/screen/TestGui.S F
      // 0f29: fmul
      // 0f2a: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 0f2d: iload 8
      // 0f2f: ifeq 0f58
      // 0f32: fload 11
      // 0f34: fload 4
      // 0f36: fload 10
      // 0f38: ldc_w 12.0
      // 0f3b: getstatic ru/white/screen/TestGui.S F
      // 0f3e: fmul
      // 0f3f: ldc_w 0.5
      // 0f42: getstatic ru/white/screen/TestGui.S F
      // 0f45: fmul
      // 0f46: invokestatic q4/z/up.client ()I
      // 0f49: fload 6
      // 0f4b: invokestatic q4/z/up.replAlpha (IF)I
      // 0f4e: ldc_w 3.0
      // 0f51: getstatic ru/white/screen/TestGui.S F
      // 0f54: fmul
      // 0f55: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 0f58: aload 1
      // 0f59: aload 9
      // 0f5b: fload 11
      // 0f5d: ldc_w 6.0
      // 0f60: getstatic ru/white/screen/TestGui.S F
      // 0f63: fmul
      // 0f64: fadd
      // 0f65: fload 4
      // 0f67: ldc_w 2.5
      // 0f6a: getstatic ru/white/screen/TestGui.S F
      // 0f6d: fmul
      // 0f6e: fadd
      // 0f6f: fload 10
      // 0f71: ldc_w 10.0
      // 0f74: getstatic ru/white/screen/TestGui.S F
      // 0f77: fmul
      // 0f78: fsub
      // 0f79: ldc_w 750704579
      // 0f7c: ldc_w -750704324
      // 0f7f: iadd
      // 0f80: ldc_w 0.65
      // 0f83: fload 6
      // 0f85: fmul
      // 0f86: invokestatic q4/z/up.getColor (IF)I
      // 0f89: ldc_w 6.0
      // 0f8c: getstatic ru/white/screen/TestGui.S F
      // 0f8f: fmul
      // 0f90: invokevirtual x6/y34/yt.drawFadingText (Ljava/lang/String;FFFIF)V
      // 0f93: fload 4
      // 0f95: ldc_w 20.0
      // 0f98: getstatic ru/white/screen/TestGui.S F
      // 0f9b: fmul
      // 0f9c: fadd
      // 0f9d: freturn
      // 0f9e: aload 2
      // 0f9f: instanceof q2/z/tf
      // 0fa2: ifeq 1310
      // 0fa5: aload 2
      // 0fa6: checkcast q2/z/tf
      // 0fa9: astore 7
      // 0fab: aload 1
      // 0fac: aload 7
      // 0fae: invokevirtual q2/z/tf.getName ()Ljava/lang/String;
      // 0fb1: fload 3
      // 0fb2: fload 4
      // 0fb4: fconst_1
      // 0fb5: getstatic ru/white/screen/TestGui.S F
      // 0fb8: fmul
      // 0fb9: fadd
      // 0fba: ldc_w 6.5
      // 0fbd: getstatic ru/white/screen/TestGui.S F
      // 0fc0: fmul
      // 0fc1: ldc_w 1331500014
      // 0fc4: ldc_w -1331499759
      // 0fc7: iadd
      // 0fc8: ldc_w 0.6
      // 0fcb: fload 6
      // 0fcd: fmul
      // 0fce: invokestatic q4/z/up.getColor (IF)I
      // 0fd1: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 0fd4: aload 7
      // 0fd6: invokevirtual q2/z/tf.getValue ()Ljava/lang/Integer;
      // 0fd9: invokevirtual java/lang/Integer.intValue ()I
      // 0fdc: istore 8
      // 0fde: fload 3
      // 0fdf: fload 5
      // 0fe1: fadd
      // 0fe2: ldc_w 14.0
      // 0fe5: getstatic ru/white/screen/TestGui.S F
      // 0fe8: fmul
      // 0fe9: fsub
      // 0fea: fload 4
      // 0fec: ldc_w 10.0
      // 0fef: getstatic ru/white/screen/TestGui.S F
      // 0ff2: fmul
      // 0ff3: ldc_w 10.0
      // 0ff6: getstatic ru/white/screen/TestGui.S F
      // 0ff9: fmul
      // 0ffa: iload 8
      // 0ffc: fload 6
      // 0ffe: invokestatic q4/z/up.replAlpha (IF)I
      // 1001: ldc_w 3.0
      // 1004: getstatic ru/white/screen/TestGui.S F
      // 1007: fmul
      // 1008: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 100b: fload 3
      // 100c: fload 5
      // 100e: fadd
      // 100f: ldc_w 14.0
      // 1012: getstatic ru/white/screen/TestGui.S F
      // 1015: fmul
      // 1016: fsub
      // 1017: fload 4
      // 1019: ldc_w 10.0
      // 101c: getstatic ru/white/screen/TestGui.S F
      // 101f: fmul
      // 1020: ldc_w 10.0
      // 1023: getstatic ru/white/screen/TestGui.S F
      // 1026: fmul
      // 1027: ldc_w 0.5
      // 102a: getstatic ru/white/screen/TestGui.S F
      // 102d: fmul
      // 102e: ldc_w 1033957709
      // 1031: ldc_w -1033957454
      // 1034: iadd
      // 1035: ldc_w 60.0
      // 1038: fload 6
      // 103a: fmul
      // 103b: f2i
      // 103c: invokestatic q4/z/up.getColor (II)I
      // 103f: ldc_w 3.0
      // 1042: getstatic ru/white/screen/TestGui.S F
      // 1045: fmul
      // 1046: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 1049: fload 4
      // 104b: ldc_w 18.0
      // 104e: getstatic ru/white/screen/TestGui.S F
      // 1051: fmul
      // 1052: fadd
      // 1053: fstore 9
      // 1055: aload 7
      // 1057: getfield q2/z/tf.pickerOpen Z
      // 105a: ifeq 130d
      // 105d: fload 3
      // 105e: fstore 10
      // 1060: fload 5
      // 1062: fstore 11
      // 1064: aload 0
      // 1065: getfield ru/white/screen/TestGui.draggingColor Lq2/z/tf;
      // 1068: aload 7
      // 106a: if_acmpne 1079
      // 106d: aload 0
      // 106e: fload 10
      // 1070: putfield ru/white/screen/TestGui.draggingColorX F
      // 1073: aload 0
      // 1074: fload 11
      // 1076: putfield ru/white/screen/TestGui.draggingColorW F
      // 1079: aload 7
      // 107b: invokevirtual q2/z/tf.getHsb ()[F
      // 107e: astore 12
      // 1080: bipush 0
      // 1081: istore 13
      // 1083: iload 13
      // 1085: bipush 3
      // 1086: if_icmpge 1301
      // 1089: fload 9
      // 108b: iload 13
      // 108d: ldc_w -53019289
      // 1090: ldc_w 53019301
      // 1093: iadd
      // 1094: imul
      // 1095: i2f
      // 1096: getstatic ru/white/screen/TestGui.S F
      // 1099: fmul
      // 109a: fadd
      // 109b: fstore 14
      // 109d: ldc_w 4.0
      // 10a0: getstatic ru/white/screen/TestGui.S F
      // 10a3: fmul
      // 10a4: fstore 15
      // 10a6: fload 10
      // 10a8: fload 14
      // 10aa: fload 11
      // 10EventMotion: fload 15
      // 10ae: ldc_w -266421159
      // 10b1: ldc_w 266421174
      // 10b4: iadd
      // 10b5: ldc_w 416711456
      // 10b8: ldc_w -416711441
      // 10DraggableWidget: iadd
      // 10bc: ldc_w -1432830544
      // 10bf: ldc_w 1432830564
      // 10c2: iadd
      // 10c3: ldc_w 180.0
      // 10c6: fload 6
      // 10c8: fmul
      // 10c9: f2i
      // 10ConfigFile: invokestatic q4/z/up.getColor (IIII)I
      // 10cd: fconst_2
      // 10ce: getstatic ru/white/screen/TestGui.S F
      // 10d1: fmul
      // 10d2: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 10d5: ldc_w 1825558194
      // 10d8: ldc_w -1825558178
      // 10db: iadd
      // 10EventKey: istore 16
      // 10de: fload 11
      // 10e0: iload 16
      // 10e2: i2f
      // 10e3: fdiv
      // 10e4: fstore 17
      // 10e6: bipush 0
      // 10e7: istore 18
      // 10e9: iload 18
      // 10eb: iload 16
      // 10ed: if_icmpge 1233
      // 10f0: iload 18
      // 10f2: i2f
      // 10f3: iload 16
      // 10f5: i2f
      // 10f6: fdiv
      // 10f7: fstore 19
      // 10f9: iload 18
      // 10fb: bipush 1
      // 10EventMove: iadd
      // 10fd: i2f
      // 10fe: iload 16
      // 1100: i2f
      // 1101: fdiv
      // 1102: fstore 20
      // 1104: iload 13
      // 1106: lookupswitch 52 2 0 26 1 36
      // 1120: fload 19
      // 1122: fconst_1
      // 1123: fconst_1
      // 1124: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 1127: goto 1147
      // 112a: aload 12
      // 112c: bipush 0
      // 112d: faload
      // 112e: fload 19
      // 1130: aload 12
      // 1132: bipush 2
      // 1133: faload
      // 1134: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 1137: goto 1147
      // 113a: aload 12
      // 113c: bipush 0
      // 113d: faload
      // 113e: aload 12
      // 1140: bipush 1
      // 1141: faload
      // 1142: fload 19
      // 1144: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 1147: istore 21
      // 1149: iload 13
      // 114b: lookupswitch 51 2 0 25 1 35
      // 1164: fload 20
      // 1166: fconst_1
      // 1167: fconst_1
      // 1168: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 116b: goto 118b
      // 116e: aload 12
      // 1170: bipush 0
      // 1171: faload
      // 1172: fload 20
      // 1174: aload 12
      // 1176: bipush 2
      // 1177: faload
      // 1178: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 117b: goto 118b
      // 117e: aload 12
      // 1180: bipush 0
      // 1181: faload
      // 1182: aload 12
      // 1184: bipush 1
      // 1185: faload
      // 1186: fload 20
      // 1188: invokestatic java/awt/Color.HSBtoRGB (FFF)I
      // 118b: istore 22
      // 118d: iload 21
      // 118f: fload 6
      // 1191: invokestatic q4/z/up.replAlpha (IF)I
      // 1194: istore 23
      // 1196: iload 22
      // 1198: fload 6
      // 119a: invokestatic q4/z/up.replAlpha (IF)I
      // 119d: istore 24
      // 119f: iload 18
      // 11a1: ifne 11EventMotion
      // 11a4: fconst_2
      // 11a5: getstatic ru/white/screen/TestGui.S F
      // 11a8: fmul
      // 11a9: goto 11ad
      // 11EventMotion: fconst_0
      // 11ad: fstore 25
      // 11af: iload 18
      // 11b1: ifne 11bc
      // 11b4: fconst_2
      // 11b5: getstatic ru/white/screen/TestGui.S F
      // 11b8: fmul
      // 11b9: goto 11bd
      // 11bc: fconst_0
      // 11bd: fstore 26
      // 11bf: iload 18
      // 11c1: iload 16
      // 11c3: bipush 1
      // 11c4: isub
      // 11c5: if_icmpne 11d0
      // 11c8: fconst_2
      // 11c9: getstatic ru/white/screen/TestGui.S F
      // 11EventPacket: fmul
      // 11cd: goto 11d1
      // 11d0: fconst_0
      // 11d1: fstore 27
      // 11d3: iload 18
      // 11d5: iload 16
      // 11d7: bipush 1
      // 11d8: isub
      // 11d9: if_icmpne 11e4
      // 11EventKey: fconst_2
      // 11dd: getstatic ru/white/screen/TestGui.S F
      // 11e0: fmul
      // 11e1: goto 11e5
      // 11e4: fconst_0
      // 11e5: fstore 28
      // 11e7: fload 10
      // 11e9: iload 18
      // 11eb: i2f
      // 11EventAttack: fload 17
      // 11ee: fmul
      // 11ef: fadd
      // 11f0: fload 14
      // 11f2: fload 17
      // 11f4: iload 18
      // 11f6: iload 16
      // 11f8: bipush 1
      // 11f9: isub
      // 11fa: if_icmpne 1201
      // 11fd: fconst_0
      // 11fe: goto 1208
      // 1201: ldc_w 0.5
      // 1204: getstatic ru/white/screen/TestGui.S F
      // 1207: fmul
      // 1208: fadd
      // 1209: fload 15
      // 120b: bipush 4
      // 120c: newarray 10
      // 120e: dup
      // 120f: bipush 0
      // 1210: iload 23
      // 1212: iastore
      // 1213: dup
      // 1214: bipush 1
      // 1215: iload 24
      // 1217: iastore
      // 1218: dup
      // 1219: bipush 2
      // 121a: iload 24
      // 121c: iastore
      // 121d: dup
      // 121e: bipush 3
      // 121f: iload 23
      // 1221: iastore
      // 1222: fload 25
      // 1224: fload 27
      // 1226: fload 28
      // 1228: fload 26
      // 122a: invokestatic ru/white/utils/render/vs$ys.gradientRect (FFFF[IFFFF)V
      // 122d: iinc 18 1
      // 1230: goto 10e9
      // 1233: fload 10
      // 1235: fload 14
      // 1237: fload 11
      // 1239: fload 15
      // 123b: ldc_w 0.5
      // 123e: getstatic ru/white/screen/TestGui.S F
      // 1241: fmul
      // 1242: ldc_w -2132219666
      // 1245: ldc_w 2132219921
      // 1248: iadd
      // 1249: ldc_w 22.0
      // 124c: fload 6
      // 124e: fmul
      // 124f: f2i
      // 1250: invokestatic q4/z/up.getColor (II)I
      // 1253: fconst_2
      // 1254: getstatic ru/white/screen/TestGui.S F
      // 1257: fmul
      // 1258: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 125b: iload 13
      // 125d: ifne 1267
      // 1260: aload 12
      // 1262: bipush 0
      // 1263: faload
      // 1264: goto 1278
      // 1267: iload 13
      // 1269: bipush 1
      // 126a: if_icmpne 1274
      // 126d: aload 12
      // 126f: bipush 1
      // 1270: faload
      // 1271: goto 1278
      // 1274: aload 12
      // 1276: bipush 2
      // 1277: faload
      // 1278: fstore 18
      // 127a: fload 10
      // 127c: fload 11
      // 127e: fload 18
      // 1280: fmul
      // 1281: fadd
      // 1282: fstore 19
      // 1284: fload 19
      // 1286: ldc_w 3.0
      // 1289: getstatic ru/white/screen/TestGui.S F
      // 128c: fmul
      // 128d: fsub
      // 128e: fload 14
      // 1290: fconst_1
      // 1291: getstatic ru/white/screen/TestGui.S F
      // 1294: fmul
      // 1295: fsub
      // 1296: ldc_w 6.0
      // 1299: getstatic ru/white/screen/TestGui.S F
      // 129c: fmul
      // 129d: ldc_w 6.0
      // 12a0: getstatic ru/white/screen/TestGui.S F
      // 12a3: fmul
      // 12a4: ldc_w -1735794482
      // 12a7: ldc_w 1735794737
      // 12aa: iadd
      // 12ab: ldc_w 240.0
      // 12ae: fload 6
      // 12b0: fmul
      // 12b1: f2i
      // 12b2: invokestatic q4/z/up.getColor (II)I
      // 12b5: ldc_w 3.0
      // 12b8: getstatic ru/white/screen/TestGui.S F
      // 12DraggableWidget: fmul
      // 12bc: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 12bf: fload 19
      // 12c1: ldc_w 3.0
      // 12c4: getstatic ru/white/screen/TestGui.S F
      // 12c7: fmul
      // 12c8: fsub
      // 12c9: fload 14
      // 12cb: fconst_1
      // 12EventPacket: getstatic ru/white/screen/TestGui.S F
      // 12cf: fmul
      // 12d0: fsub
      // 12d1: ldc_w 6.0
      // 12d4: getstatic ru/white/screen/TestGui.S F
      // 12d7: fmul
      // 12d8: ldc_w 6.0
      // 12db: getstatic ru/white/screen/TestGui.S F
      // 12de: fmul
      // 12df: ldc_w 0.5
      // 12e2: getstatic ru/white/screen/TestGui.S F
      // 12e5: fmul
      // 12e6: bipush 0
      // 12e7: ldc_w 140.0
      // 12ea: fload 6
      // 12EventAttack: fmul
      // 12ed: f2i
      // 12ee: invokestatic q4/z/up.getColor (II)I
      // 12f1: ldc_w 3.0
      // 12f4: getstatic ru/white/screen/TestGui.S F
      // 12f7: fmul
      // 12f8: invokestatic ru/white/utils/render/vr.outline (FFFFFIF)V
      // 12fb: iinc 13 1
      // 12fe: goto 1083
      // 1301: fload 9
      // 1303: ldc_w 42.0
      // 1306: getstatic ru/white/screen/TestGui.S F
      // 1309: fmul
      // 130a: fadd
      // 130b: fstore 9
      // 130d: fload 9
      // 130f: freturn
      // 1310: aload 2
      // 1311: instanceof q2/z/sf
      // 1314: ifeq 13ed
      // 1317: aload 2
      // 1318: checkcast q2/z/sf
      // 131b: astore 7
      // 131d: aload 1
      // 131e: aload 7
      // 1320: invokevirtual q2/z/sf.getName ()Ljava/lang/String;
      // 1323: ldc_w 6.0
      // 1326: getstatic ru/white/screen/TestGui.S F
      // 1329: fmul
      // 132a: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
      // 132d: ldc_w 16.0
      // 1330: getstatic ru/white/screen/TestGui.S F
      // 1333: fmul
      // 1334: fadd
      // 1335: ldc_w 60.0
      // 1338: getstatic ru/white/screen/TestGui.S F
      // 133b: fmul
      // 133c: invokestatic java/lang/Math.max (FF)F
      // 133f: fstore 8
      // 1341: fload 3
      // 1342: fload 5
      // 1344: fadd
      // 1345: ldc_w -1906584852
      // 1348: istore 31
      // 134a: iload 31
      // 134c: dup
      // 134d: bipush 1
      // 134e: iadd
      // 134f: imul
      // 1350: bipush 2
      // 1351: irem
      // 1352: ifeq 1361
      // 1355: aconst_null
      // 1356: pop
      // 1357: ldc_w "LY8bgses0RFAaUKcZYfqe7lWL+B8gqWU"
      // 135a: ldc_w 1563812092
      // 135d: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 1360: pop
      // 1361: nop
      // 1362: fload 8
      // 1364: fsub
      // 1365: fstore 9
      // 1367: aload 1
      // 1368: ldc_w "Кнопка"
      // 136b: fload 3
      // 136c: fload 4
      // 136e: fconst_2
      // 136f: getstatic ru/white/screen/TestGui.S F
      // 1372: fmul
      // 1373: fadd
      // 1374: ldc_w 6.5
      // 1377: getstatic ru/white/screen/TestGui.S F
      // 137a: fmul
      // 137b: ldc_w 552302402
      // 137e: ldc_w -552302147
      // 1381: iadd
      // 1382: ldc_w 0.6
      // 1385: fload 6
      // 1387: fmul
      // 1388: invokestatic q4/z/up.getColor (IF)I
      // 138b: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 138e: fload 9
      // 1390: fload 4
      // 1392: fload 8
      // 1394: ldc_w 13.0
      // 1397: getstatic ru/white/screen/TestGui.S F
      // 139a: fmul
      // 139b: invokestatic q4/z/up.client ()I
      // 139e: ldc_w 0.7
      // 13a1: fload 6
      // 13a3: fmul
      // 13a4: invokestatic q4/z/up.replAlpha (IF)I
      // 13a7: ldc_w 4.0
      // 13aa: getstatic ru/white/screen/TestGui.S F
      // 13ad: fmul
      // 13ae: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 13b1: aload 1
      // 13b2: aload 7
      // 13b4: invokevirtual q2/z/sf.getName ()Ljava/lang/String;
      // 13b7: fload 9
      // 13b9: fload 8
      // 13DraggableWidget: fconst_2
      // 13bc: fdiv
      // 13bd: fadd
      // 13be: fload 4
      // 13c0: ldc_w 2.5
      // 13c3: getstatic ru/white/screen/TestGui.S F
      // 13c6: fmul
      // 13c7: fadd
      // 13c8: ldc_w 6.0
      // 13cb: getstatic ru/white/screen/TestGui.S F
      // 13ce: fmul
      // 13cf: ldc_w 810963180
      // 13d2: ldc_w -810962925
      // 13d5: iadd
      // 13d6: ldc_w 0.95
      // 13d9: fload 6
      // 13db: fmul
      // 13EventKey: invokestatic q4/z/up.getColor (IF)I
      // 13df: invokevirtual x6/y34/yt.drawCentered (Ljava/lang/String;FFFI)V
      // 13e2: fload 4
      // 13e4: ldc_w 20.0
      // 13e7: getstatic ru/white/screen/TestGui.S F
      // 13ea: fmul
      // 13eb: fadd
      // 13EventAttack: freturn
      // 13ed: aload 2
      // 13ee: instanceof q2/z/uf
      // 13f1: ifeq 145d
      // 13f4: aload 2
      // 13f5: checkcast q2/z/uf
      // 13f8: astore 7
      // 13fa: aload 1
      // 13fb: aload 7
      // 13fd: invokevirtual q2/z/uf.getName ()Ljava/lang/String;
      // 1400: fload 3
      // 1401: fload 4
      // 1403: fconst_1
      // 1404: getstatic ru/white/screen/TestGui.S F
      // 1407: fmul
      // 1408: fadd
      // 1409: ldc_w 6.0
      // 140c: getstatic ru/white/screen/TestGui.S F
      // 140f: fmul
      // 1410: ldc_w 1691248810
      // 1413: ldc_w -1691248555
      // 1416: iadd
      // 1417: ldc_w 0.4
      // 141a: fload 6
      // 141c: fmul
      // 141d: invokestatic q4/z/up.getColor (IF)I
      // 1420: invokevirtual x6/y34/yt.draw (Ljava/lang/String;FFFI)V
      // 1423: fload 3
      // 1424: fload 4
      // 1426: ldc_w 11.0
      // 1429: getstatic ru/white/screen/TestGui.S F
      // 142c: fmul
      // 142d: fadd
      // 142e: fload 5
      // 1430: ldc_w 0.5
      // 1433: getstatic ru/white/screen/TestGui.S F
      // 1436: fmul
      // 1437: ldc_w 2135421501
      // 143a: ldc_w -2135421246
      // 143d: iadd
      // 143e: ldc_w 14.0
      // 1441: fload 6
      // 1443: fmul
      // 1444: f2i
      // 1445: invokestatic q4/z/up.getColor (II)I
      // 1448: ldc_w 0.25
      // 144b: getstatic ru/white/screen/TestGui.S F
      // 144e: fmul
      // 144f: invokestatic ru/white/utils/render/vr.rect (FFFFIF)V
      // 1452: fload 4
      // 1454: ldc_w 14.0
      // 1457: getstatic ru/white/screen/TestGui.S F
      // 145a: fmul
      // 145b: fadd
      // 145c: freturn
      // 145d: fload 4
      // 145f: ldc_w 19.0
      // 1462: getstatic ru/white/screen/TestGui.S F
      // 1465: fmul
      // 1466: fadd
      // 1467: freturn
   }

   public boolean method_25422() {
      ro v1 = so.active();
      if (v1 != null) {

         v1.saveAndExit();
         return false;
      } else {
         if (!this.exit) {
            this.glomalAnim.run(0.0, 0.3F, ap.SINE_IN);
            this.exit = true;
            tq.close();
         }

         return false;
      }
   }

   private boolean moduleVisible(Module m) {
      String v2 = this.query();
      if (v2.isEmpty()) {

         boolean result;
         if (m.getCategory() == this.active) {
            result = true;
            
         } else {
            result = false;
         }

         return result;
      } else {
         return m.getName().toLowerCase().contains(v2)
            || m.getBigName().toLowerCase().contains(v2)
            || m.getDesc().toLowerCase().contains(v2)
            || m.getCategory().getName().toLowerCase().contains(v2);
      }
   }

   private float renderEnableRow(yt draw, float colX, float y, float colW, float g) {
      float v6 = colX + 8.0F * S;
      float v7 = colW - 16.0F * S;
      boolean v8 = this.select != null && this.select.isEnabled();
      this.select.getAnimation16().setDirection(v8 ? cp.FORWARDS : cp.BACKWARDS);
      float v9 = this.select.getAnimation16().getOutput();
      int v10 = up.overCol(up.getColor(255, 0.4F * g), up.replAlpha(up.client(), g), v9);
      draw.draw("✓", v6, y + 2.5F * S, 6.5F * S, v10);
      float temp2 = v6 + 11.0F * S;
      float temp3 = y + 2.0F * S;
      float temp4 = 7.0F * S;

      draw.draw("Включить", temp2, temp3, temp4, up.getColor(255, (0.65F + 0.3F * v9) * g));
      this.drawToggle(v6 + v7 - 22.0F * S, y + 1.5F * S, 20.0F * S, 10.0F * S, v9, g);
      return y + 19.0F * S;
   }

   private TestGui.Layout computeLayout(int sw, int sh) {
      TestGui.Layout v3 = new TestGui.Layout();
      float v4 = 32.0F * S;
      float v5 = 128.0F * S;
      float v6 = 372.0F * S;
      float v7 = v4 + v5 + v6;
      float v8 = 302.0F * S;
      float v9 = (float)sw / 2.0F - v7 / 2.0F;
      float v10 = (float)sh / 2.0F - v8 / 2.0F;
      v3.iconX = v9;
      v3.iconY = v10;
      v3.iconW = v4;
      v3.iconH = v8;
      v3.listX = v9 + v4;
      v3.listY = v10;
      v3.listW = v5;
      v3.listH = v8;
      v3.mainX = v9 + v4 + v5;
      v3.mainY = v10;
      v3.mainW = v6;
      v3.mainH = v8;
      v3.logoS = 18.0F * S;
      v3.logoX = v3.iconX + (v4 - v3.logoS) / 2.0F;
      v3.logoY = v3.iconY + 8.0F * S;
      v3.catTopY = v3.logoY + v3.logoS + 10.0F * S;
      v3.catBtnH = 28.0F * S;
      v3.gearS = 22.0F * S;
      v3.gearX = v3.iconX + (v4 - v3.gearS) / 2.0F;
      v3.gearY = v3.iconY + v8 - 8.0F * S - v3.gearS;
      v3.listHeadH = 34.0F * S;
      v3.listContentY = v3.listY + v3.listHeadH;
      v3.listContentH = v3.listH - v3.listHeadH - 8.0F * S;

      v3.crumbY = v3.mainY + 10.0F * S;
      v3.tabY = v3.mainY + 28.0F * S;
      v3.tabH = 18.0F * S;
      float temp1 = v3.tabY + v3.tabH;

      v3.colsY = temp1 + 8.0F * S;
      v3.colsH = v3.mainY + v3.mainH - 10.0F * S - v3.colsY;
      v3.colGap = 8.0F * S;
      v3.colW = (v6 - 24.0F * S - v3.colGap) / 2.0F;
      v3.leftX = v3.mainX + 12.0F * S;
      v3.rightX = v3.leftX + v3.colW + v3.colGap;
      v3.searchW = 104.0F * S;
      v3.searchH = 18.0F * S;
      v3.searchX = v3.mainX + v6 - 12.0F * S - v3.searchW;
      v3.searchY = v3.mainY + 8.0F * S;
      return v3;
   }

   private float colHeaderH() {
      int v2 = -1379748812;
      int temp1 = v2 * (v2 + 1);

      return 18.0F * S;
   }

   private boolean clickInCols(float mx, float my, TestGui.Layout l) {
      return fq.isHovered(mx, my, l.leftX, l.colsY, l.colW, l.colsH) || fq.isHovered(mx, my, l.rightX, l.colsY, l.colW, l.colsH);
   }

   private void renderThemeWave(float x, float y, float w, float h, float g) {
      if (this.themeWaveActive) {
         long v6 = System.currentTimeMillis();
         float v8 = (float)(v6 - this.themeWaveStart) / 650.0F;
         if (v8 >= 1.0F) {
            this.themeWaveActive = false;
         } else {
            if (v8 < 0.0F) {
               v8 = 0.0F;
            }

            if (!(g <= 0.01F)) {
               float v9 = 1.0F - (1.0F - v8) * (1.0F - v8) * (1.0F - v8);
               float v10 = (float)Math.hypot((double)(this.themeWaveOX - x), (double)(this.themeWaveOY - y));
               float v11 = (float)Math.hypot((double)(this.themeWaveOX - (x + w)), (double)(this.themeWaveOY - y));
               float v12 = (float)Math.hypot((double)(this.themeWaveOX - x), (double)(this.themeWaveOY - (y + h)));
               float v13 = (float)Math.hypot((double)(this.themeWaveOX - (x + w)), (double)(this.themeWaveOY - (y + h)));
               float v14 = Math.max(Math.max(v10, v11), Math.max(v12, v13)) + 24.0F * S;
               float v15 = Math.max(1.0F, v14 * v9);
               float v16 = (1.0F - v8) * g;
               if (!(v16 <= 0.004F)) {
                  int v17 = this.themeWaveTo;
                  dt.enable(x, y, w, h, 2.0F);
                  vr.rect(this.themeWaveOX - v15, this.themeWaveOY - v15, v15 * 2.0F, v15 * 2.0F, up.replAlpha(v17, 0.14F * v16), v15);
                  vr.glow(
                     this.themeWaveOX - v15, this.themeWaveOY - v15, v15 * 2.0F, v15 * 2.0F, up.replAlpha(v17, 0.3F * v16), v15, 14.0F * S, 1.0F
                  );
                  vr.outline(
                     this.themeWaveOX - v15, this.themeWaveOY - v15, v15 * 2.0F, v15 * 2.0F, 1.5F * S, up.replAlpha(v17, 0.85F * v16), v15
                  );
                  float v18 = v15 - 16.0F * S * v9 - 6.0F * S;
                  if (v18 > 2.0F) {
                     float result = this.themeWaveOX - v18;
                     float temp1 = this.themeWaveOY - v18;
                     float temp2 = v18 * 2.0F;
                     float temp3 = v18 * 2.0F;
                     float temp4 = 1.0F * S;

                     vr.outline(result, temp1, temp2, temp3, temp4, up.replAlpha(v17, 0.35F * v16), v18);
                  }

                  int v19 = 28;
                  float v20 = (float)v6 * 0.008F;

                  for (int v21 = 0; v21 < v19; v21++) {
                     float v22 = (float)((double)((float)v21 / (float)v19) * Math.PI * 2.0);
                     float v23 = (float)Math.sin((double)(v22 * 3.0F + v20)) * 4.0F * S * (0.3F + 0.7F * (1.0F - v8));
                     float v24 = v15 + v23;
                     float v25 = this.themeWaveOX + (float)Math.cos((double)v22) * v24;
                     float v30 = this.themeWaveOY + (float)Math.sin((double)v22) * v24;

                     float v26 = v30;
                     float v27 = 2.2F * S * (1.0F - v8 * 0.5F);
                     vr.rect(v25 - v27 / 2.0F, v26 - v27 / 2.0F, v27, v27, up.replAlpha(v17, 0.55F * v16), v27 / 2.0F);
                  }

                  dt.disable();
               }
            }
         }
      }
   }

   private void applyColorDrag(tf cs) {
      float v8;
      int result = (v8 = this.draggingColorW - 0.0F) == 0.0F ? 0 : (v8 < 0.0F ? -1 : 1);
      int v4 = -997368478;
      int temp2 = v4 + 1;

      if (result > 0) {
         float v6 = (float)this.lastMouseX - this.draggingColorX;

         float v2 = fq.clamp(v6 / this.draggingColorW, 0.0F, 1.0F);
         if (cs.updateHsbBar(this.draggingColorBar, v2)) {
            tq.colorTick(v2);
         }
      }
   }

   public boolean method_25406(Click click) {
      ro v2 = so.active();
      if (v2 != null) {
         return v2.mouseReleased(click.method_74245());
      } else {
         if (this.draggingSlider != null || this.draggingColor != null) {
            tq.sliderRelease();
         }

         this.draggingSlider = null;
         this.draggingColor = null;

         return super.method_25406(click);
      }
   }

   private float renderKeyRow(yt draw, float colX, float y, float colW, float g) {
      float v6 = colX + 8.0F * S;
      float v7 = colW - 16.0F * S;
      float v8 = 5.0F * S;
      float temp1 = y + 4.5F * S;
      float temp4 = 0.6F * S;
      int temp5 = up.getColor(255, 0.35F * g);

      vr.outline(v6, temp1, v8, v8, temp4, temp5, v8 / 2.0F);
      draw.draw("Клавиша", v6 + 9.0F * S, y + 2.0F * S, 7.0F * S, up.getColor(255, 0.65F * g));
      String v9 = this.bindingModule == this.select ? "..." : (this.select != null && this.select.getKey() > 0 ? eq.keyName(this.select.getKey()) : "NONE");
      float v10 = Math.max(draw.getWidth(v9, 6.0F * S) + 14.0F * S, 28.0F * S);
      float v11 = v6 + v7 - v10;
      vr.rect(
         v11,
         y + 1.0F * S,
         v10,
         13.0F * S,
         up.getColor(24, 26, 33, (int)(220.0F * g)),
         3.0F * S
      );
      vr.outline(
         v11,
         y + 1.0F * S,
         v10,
         13.0F * S,
         0.5F * S,
         this.bindingModule == this.select
            ? up.replAlpha(up.client(), g)
            : up.getColor(42, 45, 56, (int)(160.0F * g)),
         3.0F * S
      );
      draw.drawCentered(v9, v11 + v10 / 2.0F, y + 3.5F * S, 6.0F * S, up.getColor(255, 0.75F * g));
      return y + 19.0F * S;
   }

   private void ensureSelect() {

      if (this.select == null || !this.moduleVisible(this.select)) {
         List v1 = this.visibleModules();
         this.select = v1.isEmpty() ? null : (Module)v1.get(0);
         this.setScrollTarget = 0.0F;
      }
   }

   private float settingHeight(Setting<?> s, float innerW, yt f) {
      if (s instanceof vf) {
         return 0.0F;
      } else if (s instanceof SliderSetting) {
         return 26.0F * S;
      } else if (s instanceof ModeSetting v8) {
         return 19.0F * S + (v8.opened ? (float)(v8.values.size() * (15)) * S + 6.0F * S : 0.0F);
      } else if (s instanceof MultiSetting v7) {
         return 19.0F * S + (v7.opened ? (float)(v7.getValues().size() * (15)) * S + 6.0F * S : 0.0F);
      } else if (s instanceof tf v4) {
         return 18.0F * S + (v4.pickerOpen ? 42.0F * S : 0.0F);
      } else if (s instanceof StringSetting) {
         return 20.0F * S;
      } else if (s instanceof uf) {

         return 14.0F * S;
      } else {
         return s instanceof sf ? 20.0F * S : 19.0F * S;
      }
   }

   private boolean searching() {
      boolean result;
      if (!this.query().isEmpty()) {
         result = true;
         
      } else {
         result = false;
      }

      return result;
   }

   private void startThemeWave(float ox, float oy, int toColor) {
      this.themeWaveOX = ox;
      this.themeWaveOY = oy;
      this.themeWaveTo = toColor;
      this.themeWaveStart = System.currentTimeMillis();
      this.themeWaveActive = true;
      ko.animation14.setDuration(650);
      ko.animation14.reset();
      
   }

   public boolean method_25402(Click click, boolean doubled) {
      float v3 = (float)(click.comp_4798() / (double)this.scaleFix);
      double result = click.comp_4799();

      float v4 = (float)(result / (double)this.scaleFix);
      ro v5 = so.active();
      if (v5 != null) {
         return v5.mouseClicked(v3, v4, click.method_74245());
      } else if (this.bindingModule != null) {
         this.bindingModule = null;
         tq.bindReset();
         return true;
      } else if (this.activeBind != null) {
         if (this.activeBind.allowMouse && click.method_74245() != 0 && click.method_74245() != 1) {
            this.activeBind.set(click.method_74245());
            tq.bindSet();
         } else {
            tq.bindReset();
         }

         this.activeBind = null;
         return true;
      } else {
         if (this.activeString != null) {
            this.activeString.set(this.stringBuffer);
            this.activeString = null;
            tq.editCommit();
         }

         int v52 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);

         int v6 = v52;
         int v7 = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
         TestGui.Layout v8 = this.computeLayout(v6, v7);
         float v9 = this.exit ? 60.0F * S - 60.0F * S * this.glomalAnim.get() : -60.0F * S + 60.0F * S * this.glomalAnim.get();
         v8.iconY += v9;
         v8.listY += v9;
         v8.mainY += v9;
         v8.logoY += v9;
         v8.catTopY += v9;
         v8.listContentY += v9;
         v8.gearY += v9;
         v8.crumbY += v9;
         v8.tabY += v9;
         v8.colsY += v9;
         v8.searchY += v9;
         yt v10 = hu.sf_regular;
         if (this.themeSheetOpen) {
            float[] v11 = this.sheetGeom(v8);
            Theme[] v12 = ko.themes != null ? ko.themes : Theme.values();

            for (int v13 = 0; v13 < v12.length; v13++) {
               float[] v14 = this.sheetSwatch(v11, v13);
               if (fq.isHovered(v3, v4, v14[0], v14[1], v14[2], v14[3]) && click.method_74245() == 0) {
                  Theme v15 = v12[v13];
                  if (v15 != ko.selectedTheme) {
                     ko.preSelectedTheme = ko.selectedTheme;
                     ko.selectedTheme = v15;
                     Client.get().guiManager().setGuiTheme(v15);
                     if (v15 == Theme.CUSTOM) {
                        xo.get().setActive(xo.get().getCustomTheme());
                        ko.animation14.setDuration(300);
                        ko.animation14.reset();
                     } else {
                        this.startThemeWave(v3, v4, v15.getClient());
                     }
                  } else {
                     Client.get().guiManager().setGuiTheme(v15);
                     if (v15 == Theme.CUSTOM) {
                        xo.get().setActive(xo.get().getCustomTheme());
                     }
                  }

                  tq.theme(v13, v12.length);
                  return true;
               }
            }

            float[] v38 = this.sheetEditorBtn(v11);
            if (fq.isHovered(v3, v4, v38[0], v38[1], v38[2], v38[3]) && click.method_74245() == 0) {
               this.themeSheetOpen = false;
               this.openThemeEditor();
               return true;
            }

            if (fq.isHovered(v3, v4, v11[0], v11[1], v11[2], v11[3])) {
               return true;
            }

            this.themeSheetOpen = false;
         }

         if (fq.isHovered(v3, v4, v8.gearX, v8.gearY, v8.gearS, v8.gearS)) {
            if (click.method_74245() == 0) {
               this.themeSheetOpen = !this.themeSheetOpen;
               tq.expand(this.themeSheetOpen);
            }

            return true;
         } else {
            float v36 = v8.catTopY;
            int v37 = 0;

            for (Category v16 : CAT_ORDER) {
               if (fq.isHovered(v3, v4, v8.iconX, v36, v8.iconW, v8.catBtnH) && click.method_74245() == 0) {
                  if (this.active != v16 || this.searching()) {
                     this.searchQuery = "";
                     this.searchActive = false;
                     this.active = v16;
                     List v17 = this.visibleModules();
                     this.select = v17.isEmpty() ? null : (Module)v17.get(0);
                     this.listScrollTarget = 0.0F;
                     this.setScrollTarget = 0.0F;
                     this.animListReset.reset();
                     tq.category(v37, CAT_ORDER.length);
                  }

                  return true;
               }

               v36 += v8.catBtnH + 4.0F * S;
               v37++;
            }

            float v40 = v10.getWidth("Основной", 6.8F * S) + 18.0F * S;
            float v42 = v10.getWidth("Debug", 6.8F * S) + 18.0F * S;
            float v44 = v8.mainX + 12.0F * S;
            float v45 = v44 + v40 + 6.0F * S;
            if (fq.isHovered(v3, v4, v44, v8.tabY, v40, v8.tabH) && click.method_74245() == 0) {
               if (this.currentTab != 0) {
                  this.currentTab = 0;
                  tq.chip(0, 2);
               }

               return true;
            } else if (fq.isHovered(v3, v4, v45, v8.tabY, v42, v8.tabH) && click.method_74245() == 0) {
               if (this.currentTab != 1) {
                  this.currentTab = 1;
                  tq.chip(1, 2);
               }

               return true;
            } else if (fq.isHovered(v3, v4, v8.searchX, v8.searchY, v8.searchW, v8.searchH)) {
               if (click.method_74245() == 1) {
                  this.searchQuery = "";
                  this.searchActive = false;
                  tq.searchClear();
                  this.animListReset.reset();
                  this.ensureSelect();
               } else {
                  if (!this.searchActive) {
                     tq.editStart();
                  }

                  this.searchActive = true;
                  this.searchTypeTime = System.currentTimeMillis();
               }

               return true;
            } else {
               if (this.searchActive && click.method_74245() == 0) {
                  this.searchActive = false;
               }

               List v46 = this.visibleModules();
               float v18 = 21.0F * S;
               float v19 = 2.5F * S;
               float v20 = v8.listContentY + 2.0F * S + this.listScrollAnim;

               for (Module v22 : v46) {
                  if (v4 >= v8.listContentY
                     && v4 <= v8.listContentY + v8.listContentH
                     && fq.isHovered(v3, v4, v8.listX + 4.0F * S, v20, v8.listW - 8.0F * S, v18)) {
                     if (click.method_74245() == 2) {
                        this.bindingModule = v22;
                        tq.bindStart();
                        return true;
                     }

                     if (click.method_74245() == 0) {
                        if (this.select == v22) {
                           v22.setEnabled(!v22.isEnabled());
                           tq.toggle(v22.isEnabled());
                        } else {
                           this.select = v22;
                           this.setScrollTarget = 0.0F;
                           tq.expand(true);
                        }

                        return true;
                     }

                     if (click.method_74245() == 1) {
                        v22.setEnabled(!v22.isEnabled());
                        tq.toggle(v22.isEnabled());
                        return true;
                     }
                  }

                  v20 += v18 + v19;
               }

               if (this.select != null && (click.method_74245() == 0 || click.method_74245() == 1)) {
                  List v47 = this.visibleSettings(this.select);
                  int v48 = (v47.size() + 1) / 2;
                  List v23 = v48 > 0 ? v47.subList(0, Math.min(v48, v47.size())) : Collections.emptyList();
                  List v24 = v47.size() > v48 ? v47.subList(v48, v47.size()) : Collections.emptyList();
                  if (this.clickInCols(v3, v4, v8)) {
                     float v25 = v8.colsY + 6.0F * S + this.setScrollAnim + this.colHeaderH();
                     if (fq.isHovered(v3, v4, v8.leftX + 8.0F * S, v25, v8.colW - 16.0F * S, 14.0F * S)) {
                        this.select.setEnabled(!this.select.isEnabled());
                        tq.toggle(this.select.isEnabled());
                        return true;
                     }

                     float v26 = v25 + 19.0F * S;
                     String v27 = this.bindingModule == this.select ? "..." : (this.select.getKey() > 0 ? eq.keyName(this.select.getKey()) : "NONE");
                     float v28 = v10.getWidth(v27, 6.0F * S) + 12.0F * S;
                     if (click.method_74245() == 0
                        && fq.isHovered(
                           v3, v4, v8.leftX + 8.0F * S + (v8.colW - 16.0F * S) - v28 - 2.0F * S, v26 - 2.0F * S, v28 + 4.0F * S, 16.0F * S
                        )) {
                        this.bindingModule = this.select;
                        tq.bindStart();
                        return true;
                     }

                     float v29 = v26 + 19.0F * S;

                     for (Setting v31 : v23) {
                        float v32 = this.settingHeight(v31, v8.colW - 16.0F * S, v10);
                        if (v4 >= v8.colsY
                           && v4 <= v8.colsY + v8.colsH
                           && this.clickSetting(v31, v8.leftX + 8.0F * S, v29, v8.colW - 16.0F * S, v3, v4, click.method_74245(), v10)) {
                           return true;
                        }

                        v29 += v32 + 4.0F * S;
                     }

                     float v49 = v8.colsY + 6.0F * S + this.setScrollAnim + this.colHeaderH();

                     for (Setting v51 : v24) {
                        float v33 = this.settingHeight(v51, v8.colW - 16.0F * S, v10);
                        if (v4 >= v8.colsY
                           && v4 <= v8.colsY + v8.colsH
                           && this.clickSetting(v51, v8.rightX + 8.0F * S, v49, v8.colW - 16.0F * S, v3, v4, click.method_74245(), v10)) {
                           return true;
                        }

                        v49 += v33 + 4.0F * S;
                     }
                  }
               }

               return super.method_25402(click, doubled);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class GuiParticle {
      final long born = System.currentTimeMillis();
      final float x;
      final float maxRadius;
      final float lifeMs;
      final float y;
      final float drift;

      float progress() {
         float result = (float)(System.currentTimeMillis() - this.born);

         return result / this.lifeMs;
      }

      GuiParticle(float x, float y) {
         this.x = x;
         this.y = y;
         this.maxRadius = 10.0F + (float)Math.random() * 7.0F;
         this.lifeMs = 1400.0F + (float)Math.random() * 700.0F;
         this.drift = 4.0F + (float)Math.random() * 6.0F;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class Layout {
      float logoS;
      float tabY;
      float colsH;
      float colW;
      float iconH;
      float gearX;
      float colGap;
      float catTopY;
      float leftX;
      float listContentH;
      float iconW;
      float mainX;
      float catBtnH;
      float tabH;
      float mainH;
      float searchH;
      float listHeadH;
      float rightX;
      float listY;
      float searchX;
      float iconY;
      float gearY;
      float mainY;
      float listH;
      float searchW;
      float logoY;
      float listW;
      float colsY;
      float listContentY;
      float iconX;
      float listX;
      float crumbY;
      float gearS;
      float searchY;
      float logoX;
      float mainW;

      
   }
}
