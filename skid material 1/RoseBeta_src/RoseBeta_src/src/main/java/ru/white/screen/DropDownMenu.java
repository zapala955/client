package ru.white.screen;

import java.awt.Color;
import java.util.ArrayList;
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
import net.minecraft.client.util.Window;
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
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.dt;
import ru.white.utils.render.et;
import ru.white.utils.render.ss;
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
public class DropDownMenu extends Screen implements IMinecraft {
   private boolean hasDragged;
   public final Animation globalAnim = new Animation();
   public static float S = 1.0F;
   private long searchTypeTime;
   private float dragStartX;
   private float dragOffsetY;
   private double lastMouseY;
   private String stringBuffer;
   public static boolean searchActive = false;
   private tf draggingColor;
   private static final List<DropDownMenu.DropDownPanel> PANELS = new ArrayList<>();
   private SettingVisibility activeBind;
   private StringSetting activeString;
   private SliderSetting draggingSlider;
   private DropDownMenu.DropDownPanel draggingPanel;
   private boolean exit = false;
   private Module bindingModule;
   private double lastMouseX;
   private float dragOffsetX;
   private float dragStartY;
   private float scaleFix = 1.0F;
   public static String searchQuery = "";
   private int draggingColorBar;

   public boolean method_25406(Click click) {
      ro v2 = so.active();
      if (v2 != null) {
         return v2.mouseReleased(click.method_74245());
      } else {
         if (this.draggingPanel != null) {
            if (!this.hasDragged && click.method_74245() == 0) {
               this.togglePanel(this.draggingPanel, this.isShiftDown());
            }

            this.draggingPanel = null;
            this.hasDragged = false;
         }

         label26: {
            if (this.draggingSlider == null) {

               if (this.draggingColor == null) {
                  break label26;
               }
            }

            tq.sliderRelease();
         }

         this.draggingSlider = null;
         this.draggingColor = null;
         return super.method_25406(click);
      }
   }

   private static boolean moduleMatchesSearch(Module m) {
      if (searchQuery.trim().isEmpty()) {
         return true;
      } else {
         String v1 = searchQuery.toLowerCase().trim();

         String result = m.getName().toLowerCase();

         if (!result.contains(v1) && !m.getBigName().toLowerCase().contains(v1) && !m.getDesc().toLowerCase().contains(v1)) {

            if (!m.getCategory().getName().toLowerCase().contains(v1)) {
               return false;
            }
         }

         return true;
      }
   }

   public void openHandsEditor() {
      this.beforeEditorOpen();
      go.getInstance().open();

      tq.editor();
   }

   private boolean isSingleCategoryMode() {
      Client result = Client.get();

      ClickGui v1 = result.moduleManager().get(ClickGui.class);

      if (v1 != null) {
         boolean v5 = v1.singleCategory.getValue();

         if (v5) {
            return true;
         }
      }

      return false;
   }

   private void renderPanel(DropDownMenu.DropDownPanel panel, float mouseX, float mouseY, float globalAnim, yt font, int screenHeight) {
      float v7 = panel.x;
      float v8 = panel.y;
      float v9 = panel.width * S;
      float v10 = 20.0F * S;
      float v11 = panel.collapseAnim.getOutput();
      float v12 = panel.calculateContentHeight(font, v9);
      float v13 = Math.max(60.0F * S, (float)screenHeight - v8 - v10 - 36.0F * S);
      float v14 = Math.min(v12, v13);
      panel.lastVisibleBodyH = v14;
      panel.maxScroll = Math.max(0.0F, v12 - v14);
      panel.scrollAnim = panel.scrollAnim + (panel.scrollTarget - panel.scrollAnim) * 0.25F;
      if (panel.smoothBodyH <= 0.01F) {
         panel.smoothBodyH = v14;
      } else {
         float temp1 = panel.smoothBodyH + (v14 - panel.smoothBodyH) * 0.22F;

         panel.smoothBodyH = temp1;
      }

      float v15 = panel.smoothBodyH * v11;
      panel.currentHeight = v10 + v15;
      float v16 = 5.0F * S;
      boolean v17 = fq.isHovered(mouseX, mouseY, v7, v8, v9, v10);
      float v18 = v10 + v15;
      vr.blur(v7, v8, v9, v18, globalAnim, v16, up.replAlpha(up.background(), 0.85F * globalAnim));
      int v19 = v17
         ? up.getColor(24, 24, 30, (int)(175.0F * globalAnim))
         : up.getColor(16, 16, 21, (int)(165.0F * globalAnim));
      vr.rect(v7, v8, v9, v10, v19, v16, v16, v15 > 1.0F ? 0.0F : v16, v15 > 1.0F ? 0.0F : v16);
      if (v15 > 1.0F) {
         vr.rect(
            v7,
            v8 + v10,
            v9,
            v15,
            up.getColor(13, 13, 17, (int)(155.0F * globalAnim)),
            0.0F,
            0.0F,
            v16,
            v16
         );
      }

      int v20 = v11 > 0.1F
         ? up.replAlpha(up.client(), 0.35F * globalAnim)
         : up.getColor(255, 255, 255, (int)(14.0F * globalAnim));
      vr.outline(v7, v8, v9, v18, 0.5F * S, v20, v16);
      if (this.effect("Свечение")) {
         ss.render(v7, v8, v9, v18, v16, S, globalAnim);
      }

      int v21 = up.replAlpha(up.client(), globalAnim);
      if (panel.isIconFontNightix()) {
         hu.nightix_2.draw(panel.getIcon(), v7 + 7.0F * S, v8 + 5.5F * S, 8.0F * S, v21);
         
      } else {
         hu.icon.draw(panel.getIcon(), v7 + 7.0F * S, v8 + 5.5F * S, 8.0F * S, v21);
      }

      font.draw(
         panel.getTitle(),
         v7 + 18.0F * S,
         v8 + 5.2F * S,
         7.5F * S,
         up.getColor(245, 245, 250, (int)(255.0F * globalAnim))
      );
      String v22 = v11 > 0.5F ? "-" : "+";
      int v23 = v11 > 0.5F
         ? up.replAlpha(up.client(), 0.9F * globalAnim)
         : up.getColor(140, 140, 150, (int)(200.0F * globalAnim));
      font.draw(v22, v7 + v9 - 10.0F * S, v8 + 5.0F * S, 7.0F * S, v23);
      if (v11 > 0.03F && v15 > 2.0F) {
         dt.enable(v7, v8 + v10, v9, v15, 2.0F);
         float v24 = v8 + v10 + 3.0F * S + panel.scrollAnim;
         panel.renderContent(this, font, v7, v24, v9, mouseX, mouseY, globalAnim);
         dt.disable();
         if (panel.maxScroll > 1.0F) {
            float v25 = v15 - 6.0F * S;
            float v26 = Math.max(12.0F * S, v14 / v12 * v25);
            float v27 = -panel.scrollAnim / panel.maxScroll;
            float v28 = v8 + v10 + 3.0F * S + v27 * (v25 - v26);
            float v29 = v7 + v9 - 2.5F * S;
            vr.rect(v29, v28, 1.5F * S, v26, up.replAlpha(up.client(), 0.6F * globalAnim), 0.75F * S);
         }
      }
   }

   private boolean effect(String name) {
      ClickGui v2 = Client.get().moduleManager().get(ClickGui.class);
      return v2 != null && v2.effect.getValue(name);
   }

   private void renderSearchBar(int screenWidth, int screenHeight, float mouseX, float mouseY, float globalAnim, yt font) {
      float v7 = 140.0F * S;
      float v8 = 18.0F * S;
      float result = (float)screenWidth / 2.0F;

      float v9 = result - v7 / 2.0F;
      float v10 = (float)screenHeight - 24.0F * S;
      boolean v11 = fq.isHovered(mouseX, mouseY, v9, v10, v7, v8);
      int v12 = up.getColor(16, 16, 21, (int)(240.0F * globalAnim));
      int v13 = searchActive
         ? up.replAlpha(up.client(), 0.8F * globalAnim)
         : (v11 ? up.getColor(255, (int)(40.0F * globalAnim)) : up.getColor(255, (int)(16.0F * globalAnim)));
      vr.rect(v9, v10, v7, v8, v12, 4.0F * S);
      vr.outline(v9, v10, v7, v8, 0.5F * S, v13, 4.0F * S);
      hu.gui.draw("A", v9 + 6.0F * S, v10 + 5.5F * S, 6.0F * S, up.replAlpha(up.client(), globalAnim));
      float v14 = v9 + 16.0F * S;
      if (searchQuery.isEmpty() && !searchActive) {
         font.draw(
            "Search: Ctrl + F",
            v14,
            v10 + 4.5F * S,
            6.8F * S,
            up.getColor(150, 150, 160, (int)(200.0F * globalAnim))
         );
      } else {
         String v15 = searchQuery;
         font.draw(
            v15,
            v14,
            v10 + 4.5F * S,
            6.8F * S,
            up.getColor(245, 245, 250, (int)(255.0F * globalAnim))
         );
         if (searchActive && System.currentTimeMillis() / (8375468432440091503L + -8375468432440091053L) % (-5551127896160063228L + 5551127896160063230L) == 0L
            )
          {
            float v16 = font.getWidth(v15, 6.8F * S);
            vr.rect(v14 + v16 + 1.0F * S, v10 + 3.5F * S, 0.8F * S, 9.0F * S, up.replAlpha(up.client(), globalAnim), 0.4F * S);
         }

         if (!searchQuery.isEmpty()) {
            float v22 = v9 + v7 - 12.0F * S;
            float temp2 = v22 - 2.0F * S;

            boolean v17 = fq.isHovered(mouseX, mouseY, temp2, v10 + 2.0F * S, 12.0F * S, 14.0F * S);
            int v18 = v17
               ? up.getColor(255, (int)(255.0F * globalAnim))
               : up.getColor(160, (int)(180.0F * globalAnim));
            float v23 = v10 + 3.8F * S;

            font.draw("x", v22, v23, 6.5F * S, v18);
         }
      }
   }

   private boolean isControlDown() {
      Window result = mc.method_22683();

      return InputUtil.method_15987(result, 341) || InputUtil.method_15987(mc.method_22683(), 345);
   }

   public boolean method_25421() {

      return false;
   }

   public void method_25432() {
      so.closeAll();
      super.method_25432();
      
   }

   protected void method_25426() {
      this.exit = false;
      searchActive = false;
      this.draggingPanel = null;
      this.hasDragged = false;

      this.bindingModule = null;
      this.activeBind = null;
      this.activeString = null;
      float v1 = 2.0F;
      float v2 = (float)mc.method_22683().method_4495();
      this.scaleFix = v1 / v2;
      int v3 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
      initPanelsIfNeeded(v3);
      tq.open();
      sq.start(0.15F);
      this.globalAnim.run(1.0, 0.25, ap.SINE_OUT);
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.closeCheck();
      this.globalAnim.update();
      mouseX = (int)((float)mouseX / this.scaleFix);
      float result = (float)mouseY / this.scaleFix;

      mouseY = (int)result;

      this.lastMouseX = (double)mouseX;
      this.lastMouseY = (double)mouseY;
   }

   public void openThemeEditor() {
      this.beforeEditorOpen();
      no.getInstance().open();
      tq.editor();
      
   }

   public boolean method_25400(CharInput input) {
      ro v2 = so.active();
      if (v2 != null) {
         return true;
      } else if (this.activeString == null) {
         if (searchActive) {
            if (input.method_74227()) {
               String result = searchQuery;
               String v6 = input.method_74226();
               String v7 = result;
               StringBuilder v10 = new StringBuilder;

               v10./* $VF: Unable to resugar constructor */<init>();
               searchQuery = v10.append((Object)v7).append((Object)v6).toString();
               onSearchQueryChanged();
               this.searchTypeTime = System.currentTimeMillis();
               tq.type();
            }

            return true;
         } else {
            return super.method_25400(input);
         }
      } else {
         if (input.method_74227()) {
            String v3 = input.method_74226();
            if (!this.activeString.isOnlyNumber() || v3.matches("[0-9.,-]+")) {

               String v5 = this.stringBuffer;
               this.stringBuffer = v5 + v3;
               tq.type();
            }
         }

         return true;
      }
   }

   public void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
      float v3 = 2.0F;
      float v4 = (float)mc.method_22683().method_4495();
      this.scaleFix = v3 / v4;
      int v5 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
      int v6 = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
      initPanelsIfNeeded(v5);
      float v7 = this.globalAnim.get();
      if (!(v7 <= 0.005F)) {
         ClickGui v8 = Client.get().moduleManager().get(ClickGui.class);
         S = v8 != null ? v8.size.getValue() : 1.0F;
         if (context != null) {
            context.method_51448().pushMatrix();
         }

         ro v9 = so.active();
         if (v9 != null) {
            double result = mc.field_1729.method_68879(mc.method_22683());

            float v16 = (float)(result / (double)this.scaleFix);
            float v17 = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.scaleFix);
            v9.render((float)v5, (float)v6, v16, v17, v7);
            us.endOverlay();
            if (context != null) {
               context.method_51448().popMatrix();
            }
         } else {
            et.capture(1);
            if (this.effect("Размывать фон")) {
               float temp2 = (float)v5;
               float temp3 = (float)v6;

               vs.ws.blur(0.0F, 0.0F, temp2, temp3, v7, 0.0F, up.getColor(0, 0));
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

            if (this.draggingPanel != null) {
               if (Math.hypot(this.lastMouseX - (double)this.dragStartX, this.lastMouseY - (double)this.dragStartY) > 3.5) {
                  this.hasDragged = true;
               }

               if (this.hasDragged) {
                  this.draggingPanel.x = (float)this.lastMouseX - this.dragOffsetX;
                  this.draggingPanel.y = (float)this.lastMouseY - this.dragOffsetY;
               }
            }

            yt v10 = hu.sf_regular;

            for (DropDownMenu.DropDownPanel v12 : PANELS) {
               this.renderPanel(v12, (float)this.lastMouseX, (float)this.lastMouseY, v7, v10, v6);
            }

            float v18 = (float)this.lastMouseX;
            float temp4 = (float)this.lastMouseY;

            this.renderSearchBar(v5, v6, v18, temp4, v7, v10);
            us.endOverlay();
            if (context != null) {
               context.method_51448().popMatrix();
            }
         }
      }
   }

   private void closeCheck() {

      if (this.exit && this.globalAnim.get() <= 0.01F) {
         this.exit = false;
         sq.stop();
         mc.method_1507(null);
      }
   }

   private boolean isShiftDown() {
      Window result = mc.method_22683();
      int temp1 = 340;

      return InputUtil.method_15987(result, temp1) || InputUtil.method_15987(mc.method_22683(), 344);
   }

   public boolean method_25402(Click click, boolean doubled) {
      float v3 = (float)(click.comp_4798() / (double)this.scaleFix);
      float v4 = (float)(click.comp_4799() / (double)this.scaleFix);
      ro v5 = so.active();
      if (v5 != null) {
         return v5.mouseClicked(v3, v4, click.method_74245());
      } else {
         yt v6 = hu.sf_regular;
         if (this.bindingModule != null) {
            this.bindingModule = null;
            tq.bindReset();
            return true;
         } else if (this.activeBind == null) {
            if (this.activeString != null) {
               this.activeString.set(this.stringBuffer);
               this.activeString = null;
               tq.editCommit();
            }

            int v7 = (int)((float)mc.method_22683().method_4486() / this.scaleFix);
            int v8 = (int)((float)mc.method_22683().method_4502() / this.scaleFix);
            float v9 = 140.0F * S;
            float v10 = 18.0F * S;
            float v11 = (float)v7 / 2.0F - v9 / 2.0F;
            float result = (float)v8;

            float v12 = result - 24.0F * S;
            if (fq.isHovered(v3, v4, v11, v12, v9, v10)) {
               if (!searchQuery.isEmpty() && fq.isHovered(v3, v4, v11 + v9 - 14.0F * S, v12, 14.0F * S, v10)) {
                  searchQuery = "";
                  tq.erase();
                  return true;
               } else {
                  searchActive = true;
                  this.searchTypeTime = System.currentTimeMillis();
                  tq.editStart();
                  return true;
               }
            } else {
               if (searchActive && click.method_74245() == 0) {
                  searchActive = false;
               }

               for (int v13 = PANELS.size() - 1; v13 >= 0; v13--) {
                  DropDownMenu.DropDownPanel v14 = PANELS.get(v13);
                  float v15 = v14.x;
                  float v16 = v14.y;
                  float v17 = v14.width * S;
                  float v18 = 20.0F * S;
                  if (fq.isHovered(v3, v4, v15, v16, v17, v18)) {
                     if (click.method_74245() == 0) {
                        this.draggingPanel = v14;
                        this.dragStartX = v3;
                        this.dragStartY = v4;
                        this.dragOffsetX = v3 - v14.x;
                        this.dragOffsetY = v4 - v14.y;
                        this.hasDragged = false;
                        PANELS.remove(v13);
                        PANELS.add(v14);
                        return true;
                     }

                     if (click.method_74245() == 1) {
                        v14.collapsed = !v14.collapsed;
                        v14.collapseAnim.setDirection(v14.collapsed ? cp.BACKWARDS : cp.FORWARDS);
                        tq.expand(!v14.collapsed);
                        return true;
                     }
                  }

                  if (v14.collapseAnim.getOutput() > 0.05F) {
                     float v19 = v14.lastVisibleBodyH;
                     if (v4 >= v16 + v18 && v4 <= v16 + v18 + v19 && v3 >= v15 && v3 <= v15 + v17) {
                        result = v16 + v18 + 3.0F * S + v14.scrollAnim;

                        float v20 = result;
                        if (v14.mouseClickedContent(this, v6, v15, v20, v17, v3, v4, click)) {
                           return true;
                        }
                     }
                  }
               }

               return super.method_25402(click, doubled);
            }
         } else {
            if (this.activeBind.allowMouse && click.method_74245() != 0 && click.method_74245() != 1) {
               this.activeBind.set(click.method_74245());

               tq.bindSet();
            } else {
               tq.bindReset();
            }

            this.activeBind = null;
            return true;
         }
      }
   }

   private void togglePanel(DropDownMenu.DropDownPanel panel, boolean shiftHeld) {
      boolean v3 = this.isSingleCategoryMode() && !shiftHeld;
      if (panel.collapsed) {
         if (v3) {
            for (DropDownMenu.DropDownPanel v5 : PANELS) {
               if (v5 != panel && !v5.collapsed) {
                  v5.collapsed = true;
                  v5.collapseAnim.setDirection(cp.BACKWARDS);
               }
            }
         }

         panel.collapsed = false;

         panel.collapseAnim.setDirection(cp.FORWARDS);
         tq.expand(true);
      } else {
         panel.collapsed = true;
         panel.collapseAnim.setDirection(cp.BACKWARDS);
         tq.expand(false);
      }
   }

   public void openCrosshairEditor() {
      this.beforeEditorOpen();
      eo result = eo.getInstance();

      result.open();
      tq.editor();
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
         boolean v13 = v2 == 256 || v2 == 261;
         this.bindingModule.setKey(v13 ? -1 : v2);
         this.bindingModule = null;
         if (v13) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (this.activeBind != null) {
         boolean v12 = v2 == 256 || v2 == 261;
         this.activeBind.set(v12 ? -1 : v2);
         this.activeBind = null;
         if (v12) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (this.activeString != null) {
         if (!this.isControlDown() || v2 != 86 && v2 != 86) {
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
            String v11 = mc.field_1774.method_1460();
            if (v11 != null && !v11.isEmpty()) {
               String v6 = this.stringBuffer;
               this.stringBuffer = v6 + v11;
               tq.type();
            }

            return true;
         }
      } else if (!this.isControlDown() || v2 != 70 && v2 != 70) {
         if (searchActive) {
            if (this.isControlDown()) {

               if (v2 == 86 || v2 == 86) {
                  String v4 = mc.field_1774.method_1460();
                  if (v4 != null && !v4.isEmpty()) {
                     String v8 = searchQuery;
                     searchQuery = v8 + v4;
                     onSearchQueryChanged();
                     tq.type();
                  }

                  return true;
               }

               if (v2 == 65 || v2 == 65) {
                  searchQuery = "";
                  onSearchQueryChanged();
                  tq.erase();
                  return true;
               }
            }

            if (v2 == 256) {
               if (!searchQuery.isEmpty()) {
                  searchQuery = "";
                  onSearchQueryChanged();
                  tq.erase();
               } else {
                  searchActive = false;
               }

               return true;
            } else if (v2 == 257 || v2 == 335) {
               searchActive = false;
               tq.editCommit();
               return true;
            } else if (v2 == 259 && !searchQuery.isEmpty()) {
               searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
               onSearchQueryChanged();
               tq.erase();
               return true;
            } else {
               return true;
            }
         } else if (v2 == 256) {
            if (!this.exit) {
               this.globalAnim.run(0.0, 0.3F, ap.SINE_IN);
               this.exit = true;
               tq.close();
            }

            return true;
         } else {

            return super.method_25404(input);
         }
      } else {
         searchActive = true;
         this.searchTypeTime = System.currentTimeMillis();
         tq.editStart();
         return true;
      }
   }

   public void openPreviewEditor(Module target) {
      int temp2 = v3 + 1;

      this.beforeEditorOpen();
      mo.getInstance().open(target);
      tq.editor();
   }

   public DropDownMenu() {
      super(Text.method_43470("DropDownMenu"));
      this.draggingPanel = null;
      this.hasDragged = false;
      this.draggingSlider = null;
      this.draggingColor = null;
      this.draggingColorBar = 0;
      this.bindingModule = null;
      this.activeBind = null;
      this.activeString = null;
      this.stringBuffer = "";
      this.searchTypeTime = System.currentTimeMillis();
   }

   public static void onSearchQueryChanged() {
      String v0 = searchQuery.trim().toLowerCase();
      if (!v0.isEmpty()) {
         for (DropDownMenu.DropDownPanel v2 : PANELS) {
            if (v2 instanceof DropDownMenu.CategoryPanel) {

               DropDownMenu.CategoryPanel v3 = (DropDownMenu.CategoryPanel)v2;
               boolean v4 = Client.get().moduleManager().values().stream().filter(m -> {
                  boolean v5x;
                  if (m.getCategory() == v3.category) {
                     int v2x = 1551864258;

                     v5x = true;
                  } else {
                     v5x = false;
                  }

                  return v5x;
               }).anyMatch(DropDownMenu::moduleMatchesSearch);
               if (v4) {

                  if (v3.collapsed) {
                     v3.collapsed = false;
                     v3.collapseAnim.setDirection(cp.FORWARDS);
                     tq.expand(true);
                  }
               }
            }
         }
      }
   }

   private static void initPanelsIfNeeded(int screenWidth) {
      float v1 = 118.0F;
      float v2 = 8.0F;

      float v4 = (float)v3 * v1 + (float)(v3 - 1) * v2;
      float v5 = Math.max(16.0F, ((float)screenWidth - v4) / 2.0F);
      float v6 = 32.0F;
      if (PANELS.isEmpty()) {
         List result = PANELS;
         DropDownMenu.ThemePanel temp1 = new DropDownMenu.ThemePanel;

         temp1./* $VF: Unable to resugar constructor */<init>(v5, v6, v1);
         result.add(temp1);
         Category[] v12 = new Category[]{Category.COMBAT, Category.MOVEMENT, Category.PLAYER, Category.OTHER, Category.RENDER};

         for (int v13 = 0; v13 < v12.length; v13++) {
            result = PANELS;
            DropDownMenu.CategoryPanel v15 = new DropDownMenu.CategoryPanel;
            Category v16 = v12[v13];
            float temp5 = (float)(v13 + 1);

            v15./* $VF: Unable to resugar constructor */<init>(v16, v5 + temp5 * (v1 + v2), v6, v1);
            result.add(v15);
         }
      } else {
         for (DropDownMenu.DropDownPanel v8 : PANELS) {
            v8.width = v1;
         }
      }
   }

   public boolean method_25401(double mouseXRaw, double mouseYRaw, double horizontalAmount, double verticalAmount) {
      float v9 = (float)(mouseXRaw / (double)this.scaleFix);
      float v10 = (float)(mouseYRaw / (double)this.scaleFix);
      ro v11 = so.active();
      if (v11 != null) {
         return v11.mouseScrolled(v9, v10, verticalAmount);
      } else {
         int result = PANELS.size();

         for (int v12 = result - 1; v12 >= 0; v12--) {
            DropDownMenu.DropDownPanel v13 = PANELS.get(v12);
            if (v13.collapseAnim.getOutput() > 0.05F) {
               float v14 = v13.x;
               float v15 = v13.y;
               float v16 = v13.width * S;
               float v17 = 20.0F * S;
               float v18 = v13.lastVisibleBodyH;
               if (fq.isHovered(v9, v10, v14, v15, v16, v17 + v18)) {
                  if (v13.maxScroll > 0.0F) {
                     float v19 = v13.scrollTarget;
                     v13.scrollTarget = v13.scrollTarget + (float)(verticalAmount * 22.0 * (double)S);
                     v13.scrollTarget = MathHelper.method_15363(v13.scrollTarget, -v13.maxScroll, 0.0F);
                     if (v13.scrollTarget != v19) {
                        tq.scroll();
                     }
                  }

                  return true;
               }
            }
         }

         return super.method_25401(mouseXRaw, mouseYRaw, horizontalAmount, verticalAmount);
      }
   }

   public boolean method_25422() {
      ro v1 = so.active();

      if (v1 != null) {
         v1.saveAndExit();
         return false;
      } else {
         if (!this.exit) {

            this.globalAnim.run(0.0, 0.3F, ap.SINE_IN);

            this.exit = true;
            tq.close();
         }

         return false;
      }
   }

   private void beforeEditorOpen() {
      this.exit = false;

      this.globalAnim.run(1.0, 0.2F, ap.QUAD_OUT);

      
   }

   public boolean method_25403(Click click, double deltaX, double deltaY) {
      ro v6 = so.active();
      if (v6 != null) {
         return v6.mouseDragged((float)(deltaX / (double)this.scaleFix), (float)(deltaY / (double)this.scaleFix));
      } else {

         return super.method_25403(click, deltaX, deltaY);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CategoryPanel extends DropDownMenu.DropDownPanel {
      public final Category category;

      private float renderSettings(DropDownMenu menu, Module m, yt font, float px, float curY, float pw, float mouseX, float mouseY, float anim) {
         float v10 = px + 8.0F * DropDownMenu.S;
         float v11 = pw - 16.0F * DropDownMenu.S;

         for (Setting v13 : m.getSettings()) {
            if (v13.getVisible().get() && !(v13 instanceof vf)) {
               if (v13 instanceof BooleanSetting) {
                  BooleanSetting v14 = (BooleanSetting)v13;
                  float v63 = 14.5F * DropDownMenu.S;
                  float v70 = 14.0F * DropDownMenu.S;
                  float v77 = 7.5F * DropDownMenu.S;
                  float v84 = v10 + v11 - v70 - 2.0F * DropDownMenu.S;
                  float v90 = curY + 3.5F * DropDownMenu.S;
                  float v97 = 2.7F * DropDownMenu.S;
                  int var103 = v14.getValue()
                     ? up.replAlpha(up.client(), 0.85F * anim)
                     : up.getColor(35, 35, 44, (int)(220.0F * anim));
                  vr.rect(v84, v90, v70, v77, var103, v77 / 2.0F);
                  float var107 = v14.getValue() ? v84 + v70 - v97 - 1.0F * DropDownMenu.S : v84 + v97 + 1.0F * DropDownMenu.S;
                  float var111 = v90 + v77 / 2.0F;
                  if (v14.getValue()) {
                     vr.glow(
                        var107 - v97, var111 - v97, v97 * 2.0F, v97 * 2.0F, up.replAlpha(up.client(), 0.4F * anim), v97, 2.5F * DropDownMenu.S, 0.5F
                     );
                  }

                  vr.rect(
                     var107 - v97,
                     var111 - v97,
                     v97 * 2.0F,
                     v97 * 2.0F,
                     up.getColor(255, 255, 255, (int)(255.0F * anim)),
                     v97
                  );
                  int var114 = v14.getValue()
                     ? up.getColor(240, 240, 245, (int)(250.0F * anim))
                     : up.getColor(150, 150, 160, (int)(210.0F * anim));
                  float var117 = v84 - (v10 + 3.0F * DropDownMenu.S) - 3.0F * DropDownMenu.S;
                  String var120 = trimToWidth(font, v14.getName(), var117, 6.5F * DropDownMenu.S);
                  font.draw(var120, v10 + 3.0F * DropDownMenu.S, curY + 3.5F * DropDownMenu.S, 6.5F * DropDownMenu.S, var114);
                  curY += v63;
               } else if (v13 instanceof SliderSetting v15) {
                  float v62 = 24.0F * DropDownMenu.S;
                  float v69 = v11 - 6.0F * DropDownMenu.S;
                  String v76 = trimToWidth(font, v15.getName(), v69, 6.5F * DropDownMenu.S);
                  font.draw(
                     v76,
                     v10 + 3.0F * DropDownMenu.S,
                     curY + 1.5F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(170, 170, 180, (int)(230.0F * anim))
                  );
                  String v83 = v15.increment >= 1.0F ? String.valueOf((int)v15.min) : String.format("%.1f", v15.min);
                  String v89 = v15.increment >= 1.0F ? String.valueOf((int)v15.max) : String.format("%.1f", v15.max);
                  String v96 = v15.increment >= 1.0F ? String.valueOf((int)v15.getValue().floatValue()) : String.format("%.1f", v15.getValue());
                  float var102 = curY + 9.5F * DropDownMenu.S;
                  font.draw(
                     v83,
                     v10 + 3.0F * DropDownMenu.S,
                     var102,
                     5.5F * DropDownMenu.S,
                     up.getColor(125, 125, 135, (int)(200.0F * anim))
                  );
                  float var106 = font.getWidth(v96, 6.0F * DropDownMenu.S);
                  font.draw(v96, v10 + v11 / 2.0F - var106 / 2.0F, var102 - 0.3F * DropDownMenu.S, 6.0F * DropDownMenu.S, up.replAlpha(up.client(), anim));
                  float var110 = font.getWidth(v89, 5.5F * DropDownMenu.S);
                  font.draw(
                     v89,
                     v10 + v11 - 3.0F * DropDownMenu.S - var110,
                     var102,
                     5.5F * DropDownMenu.S,
                     up.getColor(125, 125, 135, (int)(200.0F * anim))
                  );
                  float var113 = curY + 18.0F * DropDownMenu.S;
                  float var116 = 2.5F * DropDownMenu.S;
                  float var119 = v11 - 6.0F * DropDownMenu.S;
                  float var121 = v10 + 3.0F * DropDownMenu.S;
                  float var122 = MathHelper.method_15363((v15.getValue() - v15.min) / (v15.max - v15.min), 0.0F, 1.0F);
                  vr.rect(
                     var121,
                     var113,
                     var119,
                     var116,
                     up.getColor(32, 32, 40, (int)(220.0F * anim)),
                     var116 / 2.0F
                  );
                  if (var122 > 0.005F) {
                     float var123 = var119 * var122;
                     vr.rect(var121, var113, var123, var116, up.replAlpha(up.client(), anim), var116 / 2.0F);
                  }

                  float var124 = var121 + var119 * var122;
                  float var125 = var113 + var116 / 2.0F;
                  float var127 = 3.2F * DropDownMenu.S;
                  vr.glow(
                     var124 - var127,
                     var125 - var127,
                     var127 * 2.0F,
                     var127 * 2.0F,
                     up.replAlpha(up.client(), 0.45F * anim),
                     var127,
                     3.0F * DropDownMenu.S,
                     0.5F
                  );
                  vr.rect(var124 - var127, var125 - var127, var127 * 2.0F, var127 * 2.0F, up.replAlpha(up.client(), anim), var127);
                  if (menu.draggingSlider == v15) {
                     float var128 = MathHelper.method_15363((mouseX - var121) / var119, 0.0F, 1.0F);
                     float var129 = v15.min + var128 * (v15.max - v15.min);
                     float var131 = (float)Math.round(var129 / v15.increment) * v15.increment;
                     v15.set(MathHelper.method_15363(var131, v15.min, v15.max));
                  }

                  curY += v62;
               } else if (v13 instanceof ModeSetting v16) {
                  float v61 = 16.0F * DropDownMenu.S;
                  String v68 = v16.getValue();
                  float v75 = font.getWidth(v68, 6.2F * DropDownMenu.S);
                  float v82 = v75 + 6.0F * DropDownMenu.S;
                  float v88 = 11.0F * DropDownMenu.S;
                  float v95 = v10 + v11 - 2.0F * DropDownMenu.S - v82;
                  float var101 = curY + 2.0F * DropDownMenu.S;
                  vr.rect(
                     v95,
                     var101,
                     v82,
                     v88,
                     up.getColor(25, 25, 32, (int)(220.0F * anim)),
                     2.5F * DropDownMenu.S
                  );
                  vr.outline(
                     v95,
                     var101,
                     v82,
                     v88,
                     0.5F * DropDownMenu.S,
                     up.getColor(65, 65, 75, (int)(200.0F * anim)),
                     2.5F * DropDownMenu.S
                  );
                  font.draw(v68, v95 + 3.0F * DropDownMenu.S, var101 + 2.2F * DropDownMenu.S, 6.2F * DropDownMenu.S, up.replAlpha(up.client(), anim));
                  float var105 = v95 - (v10 + 3.0F * DropDownMenu.S) - 3.0F * DropDownMenu.S;
                  String var109 = trimToWidth(font, v16.getName(), var105, 6.5F * DropDownMenu.S);
                  font.draw(
                     var109,
                     v10 + 3.0F * DropDownMenu.S,
                     curY + 3.5F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(170, 170, 180, (int)(230.0F * anim))
                  );
                  curY += v61;
               } else if (v13 instanceof MultiSetting v17) {
                  font.draw(
                     v17.getName(),
                     v10 + 3.0F * DropDownMenu.S,
                     curY + 2.0F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(170, 170, 180, (int)(230.0F * anim))
                  );
                  curY += 11.0F * DropDownMenu.S;
                  float v60 = v10 + 2.0F * DropDownMenu.S;
                  float v67 = v10 + v11 - 2.0F * DropDownMenu.S;

                  for (BooleanSetting v81 : v17.getValues()) {
                     float v87 = font.getWidth(v81.getName(), 6.2F * DropDownMenu.S);
                     float v94 = v87 + 7.0F * DropDownMenu.S;
                     float var100 = 11.0F * DropDownMenu.S;
                     if (v60 + v94 > v67 && v60 > v10 + 2.0F * DropDownMenu.S) {
                        v60 = v10 + 2.0F * DropDownMenu.S;
                        curY += var100 + 2.5F * DropDownMenu.S;
                     }

                     if (v81.getValue()) {
                        vr.rect(v60, curY, v94, var100, up.replAlpha(up.client(), 0.85F * anim), 2.5F * DropDownMenu.S);
                        font.draw(
                           v81.getName(),
                           v60 + 3.5F * DropDownMenu.S,
                           curY + 2.3F * DropDownMenu.S,
                           6.2F * DropDownMenu.S,
                           up.getColor(16, 16, 20, (int)(250.0F * anim))
                        );
                     } else {
                        int temp4 = up.getColor(24, 24, 30, (int)(210.0F * anim));

                        vr.rect(v60, curY, v94, var100, temp4, 2.5F * DropDownMenu.S);
                        vr.outline(
                           v60,
                           curY,
                           v94,
                           var100,
                           0.5F * DropDownMenu.S,
                           up.getColor(60, 60, 68, (int)(180.0F * anim)),
                           2.5F * DropDownMenu.S
                        );
                        font.draw(
                           v81.getName(),
                           v60 + 3.5F * DropDownMenu.S,
                           curY + 2.3F * DropDownMenu.S,
                           6.2F * DropDownMenu.S,
                           up.getColor(155, 155, 160, (int)(220.0F * anim))
                        );
                     }

                     v60 += v94 + 3.0F * DropDownMenu.S;
                  }

                  curY += 13.5F * DropDownMenu.S;
               } else if (v13 instanceof StringSetting v18) {
                  font.draw(
                     v18.getName(),
                     v10 + 3.0F * DropDownMenu.S,
                     curY + 2.0F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(170, 170, 180, (int)(230.0F * anim))
                  );
                  float v59 = curY + 9.5F * DropDownMenu.S;
                  float v66 = 13.0F * DropDownMenu.S;
                  float v73 = v11 - 4.0F * DropDownMenu.S;
                  float v80 = v10 + 2.0F * DropDownMenu.S;
                  vr.rect(
                     v80,
                     v59,
                     v73,
                     v66,
                     up.getColor(20, 20, 26, (int)(230.0F * anim)),
                     2.5F * DropDownMenu.S
                  );
                  int v86 = menu.activeString == v18
                     ? up.replAlpha(up.client(), anim)
                     : up.getColor(55, 55, 65, (int)(200.0F * anim));
                  vr.outline(v80, v59, v73, v66, 0.5F * DropDownMenu.S, v86, 2.5F * DropDownMenu.S);
                  String v93 = menu.activeString == v18 ? menu.stringBuffer : v18.getValue();
                  if (v93.isEmpty() && menu.activeString != v18) {
                     v93 = "...";
                  }

                  font.draw(
                     v93,
                     v80 + 4.0F * DropDownMenu.S,
                     v59 + 3.2F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(235, 235, 240, (int)(250.0F * anim))
                  );
                  if (menu.activeString == v18
                     && System.currentTimeMillis() / (-7677314499136924637L + 7677314499136925087L) % (-255349884031419683L + 255349884031419685L) == 0L) {
                     float v99 = font.getWidth(v93, 6.5F * DropDownMenu.S);
                     vr.rect(
                        v80 + 4.0F * DropDownMenu.S + v99 + 1.0F * DropDownMenu.S,
                        v59 + 2.5F * DropDownMenu.S,
                        0.8F * DropDownMenu.S,
                        8.0F * DropDownMenu.S,
                        up.replAlpha(up.client(), anim),
                        0.4F * DropDownMenu.S
                     );
                  }

                  curY += 25.0F * DropDownMenu.S;
               } else if (!(v13 instanceof tf v19)) {
                  if (v13 instanceof SettingVisibility v20) {
                     float v56 = 16.0F * DropDownMenu.S;
                     String v64 = v20.getName();
                     String v71 = v64.startsWith("Клавиша ") ? v64.substring(8) : v64;
                     if (!v71.isEmpty() && Character.isLowerCase(v71.charAt(0))) {
                        char var137 = Character.toUpperCase(v71.charAt(0));
                        String v51 = v71.substring(1);
                        char v52 = var137;
                        v71 = v52 + v51;
                     }

                     String var138;
                     if (menu.activeBind == v20) {
                        var138 = "[...]";
                     } else {
                        String v53 = eq.keyName(v20.getValue()).replace("NONE", "n/a");
                        var138 = "[" + v53 + "]";
                     }

                     String v78 = var138;
                     float v85 = font.getWidth(v78, 6.0F * DropDownMenu.S);
                     float v92 = v85 + 6.0F * DropDownMenu.S;
                     float v98 = 11.0F * DropDownMenu.S;
                     float var104 = v10 + v11 - 2.0F * DropDownMenu.S - v92;
                     float var108 = curY + 2.0F * DropDownMenu.S;
                     vr.rect(
                        var104,
                        var108,
                        v92,
                        v98,
                        up.getColor(25, 25, 32, (int)(220.0F * anim)),
                        2.5F * DropDownMenu.S
                     );
                     vr.outline(
                        var104,
                        var108,
                        v92,
                        v98,
                        0.5F * DropDownMenu.S,
                        up.getColor(65, 65, 75, (int)(200.0F * anim)),
                        2.5F * DropDownMenu.S
                     );
                     int var112 = menu.activeBind == v20
                        ? up.replAlpha(up.client(), anim)
                        : up.getColor(160, 160, 175, (int)(230.0F * anim));
                     font.drawCentered(v78, var104 + v92 / 2.0F, var108 + 2.2F * DropDownMenu.S, 6.0F * DropDownMenu.S, var112);
                     float var115 = var104 - (v10 + 3.0F * DropDownMenu.S) - 3.0F * DropDownMenu.S;
                     String var118 = trimToWidth(font, v71, var115, 6.5F * DropDownMenu.S);
                     font.draw(
                        var118,
                        v10 + 3.0F * DropDownMenu.S,
                        curY + 3.5F * DropDownMenu.S,
                        6.5F * DropDownMenu.S,
                        up.getColor(170, 170, 180, (int)(230.0F * anim))
                     );
                     curY += v56;
                  } else if (v13 instanceof sf v21) {
                     float v57 = 16.0F * DropDownMenu.S;
                     boolean v65 = fq.isHovered(mouseX, mouseY, v10, curY, v11, v57);
                     int v72 = v65
                        ? up.getColor(35, 35, 45, (int)(230.0F * anim))
                        : up.getColor(25, 25, 32, (int)(210.0F * anim));
                     vr.rect(
                        v10 + 2.0F * DropDownMenu.S,
                        curY + 1.5F * DropDownMenu.S,
                        v11 - 4.0F * DropDownMenu.S,
                        v57 - 3.0F * DropDownMenu.S,
                        v72,
                        2.5F * DropDownMenu.S
                     );
                     vr.outline(
                        v10 + 2.0F * DropDownMenu.S,
                        curY + 1.5F * DropDownMenu.S,
                        v11 - 4.0F * DropDownMenu.S,
                        v57 - 3.0F * DropDownMenu.S,
                        0.5F * DropDownMenu.S,
                        up.getColor(65, 65, 75, (int)(190.0F * anim)),
                        2.5F * DropDownMenu.S
                     );
                     String v79 = trimToWidth(font, v21.getName(), v11 - 10.0F * DropDownMenu.S, 6.5F * DropDownMenu.S);
                     font.drawCentered(
                        v79,
                        v10 + v11 / 2.0F,
                        curY + 4.5F * DropDownMenu.S,
                        6.5F * DropDownMenu.S,
                        up.getColor(235, 235, 240, (int)(240.0F * anim))
                     );
                     curY += v57;
                  } else if (v13 instanceof uf v22) {
                     float v58 = 13.0F * DropDownMenu.S;
                     font.draw(
                        v22.getName(),
                        v10 + 3.0F * DropDownMenu.S,
                        curY + 3.0F * DropDownMenu.S,
                        6.0F * DropDownMenu.S,
                        up.getColor(120, 120, 130, (int)(200.0F * anim))
                     );
                     curY += v58;
                  }
               } else {
                  float v23 = 16.0F * DropDownMenu.S;
                  float v24 = 16.0F * DropDownMenu.S;
                  float v25 = 10.0F * DropDownMenu.S;
                  float v26 = v10 + v11 - 2.0F * DropDownMenu.S - v24;
                  float v27 = curY + 2.5F * DropDownMenu.S;
                  int v28 = v19.getValue();
                  vr.rect(v26, v27, v24, v25, v28, 2.5F * DropDownMenu.S);
                  vr.outline(
                     v26, v27, v24, v25, 0.5F * DropDownMenu.S, up.getColor(255, (int)(40.0F * anim)), 2.5F * DropDownMenu.S
                  );
                  float v29 = v26 - (v10 + 3.0F * DropDownMenu.S) - 3.0F * DropDownMenu.S;
                  String v30 = trimToWidth(font, v19.getName(), v29, 6.5F * DropDownMenu.S);
                  font.draw(
                     v30,
                     v10 + 3.0F * DropDownMenu.S,
                     curY + 3.5F * DropDownMenu.S,
                     6.5F * DropDownMenu.S,
                     up.getColor(170, 170, 180, (int)(230.0F * anim))
                  );
                  curY += v23;
                  if (v19.pickerOpen) {
                     float[] v31 = v19.getHsb();
                     float v32 = v10 + 4.0F * DropDownMenu.S;
                     float v33 = v11 - 8.0F * DropDownMenu.S;
                     float v34 = 4.5F * DropDownMenu.S;
                     float v36 = v33 / (float)v35;

                     for (int v37 = 0; v37 < 3; v37++) {
                        float v38 = curY + 4.0F * DropDownMenu.S + (float)v37 * 12.0F * DropDownMenu.S;
                        if (menu.draggingColor == v19 && menu.draggingColorBar == v37) {
                           float v39 = MathHelper.method_15363((mouseX - v32) / v33, 0.0F, 1.0F);
                           v19.updateHsbBar(v37, v39);
                           v28 = v19.getValue();
                           v31 = v19.getHsb();
                        }

                        float var126 = v31[0];
                        float v40 = v31[1];
                        float v41 = v31[2];
                        vr.rect(
                           v32,
                           v38,
                           v33,
                           v34,
                           up.getColor(20, 20, 26, (int)(240.0F * anim)),
                           2.25F * DropDownMenu.S
                        );

                        for (int v42 = 0; v42 < v35; v42++) {
                           float v43 = (float)v42 / (float)v35;
                           float v44 = (float)(v42 + 1) / (float)v35;

                           int v45 = switch (v37) {
                              case 0 -> Color.HSBtoRGB(v43, 1.0F, 1.0F);
                              case 1 -> Color.HSBtoRGB(var126, v43, v41);
                              default -> Color.HSBtoRGB(var126, v40, v43);
                           };

                           int v46 = switch (v37) {
                              case 0 -> Color.HSBtoRGB(v44, 1.0F, 1.0F);
                              case 1 -> Color.HSBtoRGB(var126, v44, v41);
                              default -> Color.HSBtoRGB(var126, v40, v44);
                           };
                           int v47 = up.replAlpha(v45, anim);
                           int v48 = up.replAlpha(v46, anim);
                           float v49 = v42 == 0 ? 2.25F * DropDownMenu.S : 0.0F;
                           float v50 = v42 == v35 - 1 ? 2.25F * DropDownMenu.S : 0.0F;
                           vr.gradientRect(
                              v32 + (float)v42 * v36,
                              v38,
                              v36 + (v42 == v35 - 1 ? 0.0F : 0.5F * DropDownMenu.S),
                              v34,
                              new int[]{v47, v48, v48, v47},
                              v49,
                              v50,
                              v50,
                              v49
                           );
                        }

                        vr.outline(
                           v32,
                           v38,
                           v33,
                           v34,
                           0.5F * DropDownMenu.S,
                           up.getColor(255, (int)(20.0F * anim)),
                           2.25F * DropDownMenu.S
                        );
                        float var130 = v32 + v33 * v31[v37];

                        int var132 = switch (v37) {
                           case 0 -> Color.HSBtoRGB(var126, 1.0F, 1.0F);
                           case 1 -> Color.HSBtoRGB(var126, v40, v41);
                           default -> Color.HSBtoRGB(var126, v40, v41);
                        };
                        float var133 = 3.2F * DropDownMenu.S;
                        float var134 = v38 + v34 / 2.0F;
                        vr.glow(
                           var130 - var133,
                           var134 - var133,
                           var133 * 2.0F,
                           var133 * 2.0F,
                           up.getColor(0, 0, 0, (int)(130.0F * anim)),
                           var133,
                           3.0F * DropDownMenu.S,
                           0.4F
                        );
                        vr.rect(
                           var130 - var133,
                           var134 - var133,
                           var133 * 2.0F,
                           var133 * 2.0F,
                           up.getColor(250, 250, 255, (int)(255.0F * anim)),
                           var133
                        );
                        vr.rect(
                           var130 - (var133 - 1.2F * DropDownMenu.S),
                           var134 - (var133 - 1.2F * DropDownMenu.S),
                           (var133 - 1.2F * DropDownMenu.S) * 2.0F,
                           (var133 - 1.2F * DropDownMenu.S) * 2.0F,
                           up.replAlpha(var132, anim),
                           var133 - 1.2F * DropDownMenu.S
                        );
                     }

                     curY += 41.0F * DropDownMenu.S;
                  }
               }
            }
         }

         return curY + 2.0F * DropDownMenu.S;
      }

      @Override
      public String getIcon() {
         return this.category.getIcon();
      }

      private float calculateSettingsHeight(Module m, yt font, float pw) {
         float v4 = 3.0F * DropDownMenu.S;
         float v5 = 10.0F * DropDownMenu.S;
         float v6 = pw - 20.0F * DropDownMenu.S;

         for (Setting v8 : m.getSettings()) {
            if (v8.getVisible().get() && !(v8 instanceof vf)) {
               if (v8 instanceof BooleanSetting) {
                  v4 += 14.5F * DropDownMenu.S;
               } else if (v8 instanceof SliderSetting) {
                  v4 += 24.0F * DropDownMenu.S;
               } else if (v8 instanceof ModeSetting) {
                  v4 += 16.0F * DropDownMenu.S;
               } else if (!(v8 instanceof MultiSetting v9)) {
                  if (v8 instanceof StringSetting) {
                     v4 += 24.0F * DropDownMenu.S;
                  } else if (v8 instanceof tf v10) {
                     v4 += 16.0F * DropDownMenu.S;
                     if (v10.pickerOpen) {
                        v4 += 41.0F * DropDownMenu.S;
                     }
                  } else if (v8 instanceof SettingVisibility) {
                     v4 += 16.0F * DropDownMenu.S;
                  } else if (v8 instanceof sf) {
                     v4 += 16.0F * DropDownMenu.S;
                  } else if (v8 instanceof uf) {
                     v4 += 13.0F * DropDownMenu.S;
                  }
               } else {
                  v4 += 12.0F * DropDownMenu.S;
                  float v11 = v5;
                  float v12 = 1.0F;

                  for (BooleanSetting v14 : v9.getValues()) {
                     float v15 = font.getWidth(v14.getName(), 6.2F * DropDownMenu.S) + 7.0F * DropDownMenu.S;
                     float result = v11 + v15;
                     float temp1 = v5 + v6;

                     if (result > temp1 && v11 > v5) {
                        v11 = v5;
                        v12++;
                     }

                     v11 += v15 + 3.0F * DropDownMenu.S;
                  }

                  v4 += v12 * 13.0F * DropDownMenu.S + 3.0F * DropDownMenu.S;
               }
            }
         }

         return v4 + 4.0F * DropDownMenu.S;
      }

      @Override
      public String getTitle() {

         String result = this.category.getName();

         return result;
      }

      public CategoryPanel(Category category, float x, float y, float width) {
         super(x, y, width);
         this.category = category;
      }

      private static String trimToWidth(yt font, String text, float maxW, float size) {
         if (maxW <= 12.0F * DropDownMenu.S) {
            return text;
         } else {

            if (font.getWidth(text, size) <= maxW) {
               return text;
            } else {

               String v4 = "...";
               float v5 = font.getWidth(v4, size);
               if (v5 >= maxW) {
                  return "";
               } else {
                  while (text.length() > 1 && font.getWidth(text, size) + v5 > maxW) {
                     text = text.substring(0, text.length() - 1);
                  }

                  return text + v4;
               }
            }
         }
      }

      @Override
      public boolean mouseClickedContent(DropDownMenu menu, yt font, float px, float startY, float pw, float mouseX, float mouseY, Click click) {
         // $VF: Couldn't be decompiled
         // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
         // java.lang.OutOfMemoryError: Java heap space
         //   at org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory$FastSparseSet.getCopy(FastSparseSetFactory.java:98)
         //   at org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect.getCopy(SFormsFastMapDirect.java:67)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.getFilteredOutMap(SFormsConstructor.java:279)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.mergeInVarMaps(SFormsConstructor.java:241)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.ssaStatements(SFormsConstructor.java:107)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.splitVariables(SFormsConstructor.java:94)
         //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:52)
         //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
         //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:224)
         //
         // Bytecode:
         // 000: invokestatic ru/white/Client.get ()Lru/white/Client;
         // 003: invokevirtual ru/white/Client.moduleManager ()Lq4/z/if;
         // 006: invokevirtual q4/z/if.values ()Ljava/util/Collection;
         // 009: invokeinterface java/util/Collection.stream ()Ljava/util/stream/Stream; 1
         // 00e: aload 0
         // 00f: invokedynamic test (Lru/white/screen/DropDownMenu$CategoryPanel;)Ljava/util/function/Predicate; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)Z, ru/white/screen/DropDownMenu$CategoryPanel.lambda$mouseClickedContent$2 (Lq4/z/Module;)Z, (Lq4/z/Module;)Z ]
         // 014: invokeinterface java/util/stream/Stream.filter (Ljava/util/function/Predicate;)Ljava/util/stream/Stream; 2
         // 019: invokeinterface java/util/stream/Stream.toList ()Ljava/util/List; 1
         // 01e: astore 9
         // 020: fload 4
         // 022: fstore 10
         // 024: ldc 16.0
         // 026: getstatic ru/white/screen/DropDownMenu.S F
         // 029: fmul
         // 02a: fstore 11
         // 02c: ldc 2.5
         // 02e: getstatic ru/white/screen/DropDownMenu.S F
         // 031: fmul
         // 032: fstore 12
         // 034: fload 3
         // 035: ldc_w 4.0
         // 038: getstatic ru/white/screen/DropDownMenu.S F
         // 03b: fmul
         // 03c: fadd
         // 03d: fstore 13
         // 03f: fload 5
         // 041: ldc 8.0
         // 043: getstatic ru/white/screen/DropDownMenu.S F
         // 046: fmul
         // 047: fsub
         // 048: fstore 14
         // 04a: aload 9
         // 04c: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
         // 051: astore 15
         // 053: aload 15
         // 055: invokeinterface java/util/Iterator.hasNext ()Z 1
         // 05a: ifeq 62f
         // 05d: aload 15
         // 05f: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
         // 064: checkcast q4/z/Module
         // 067: astore 16
         // 069: getstatic ru/white/screen/DropDownMenu.searchQuery Ljava/lang/String;
         // 06c: invokevirtual java/lang/String.trim ()Ljava/lang/String;
         // 06f: invokevirtual java/lang/String.isEmpty ()Z
         // 072: ifne 080
         // 075: aload 16
         // 077: invokestatic ru/white/screen/DropDownMenu.moduleMatchesSearch (Lq4/z/Module;)Z
         // 07a: ifne 080
         // 07d: goto 053
         // 080: fload 6
         // 082: fload 7
         // 084: fload 13
         // 086: fload 10
         // 088: fload 14
         // 08a: fload 11
         // 08c: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 08f: ifeq 0e2
         // 092: aload 8
         // 094: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 097: ifne 0a9
         // 09a: aload 16
         // 09c: invokevirtual q4/z/gf.toggle ()V
         // 09f: aload 16
         // 0a1: invokevirtual q4/z/gf.isEnabled ()Z
         // 0a4: invokestatic q2/z/tq.toggle (Z)V
         // 0a7: bipush 1
         // 0a8: ireturn
         // 0a9: aload 8
         // 0ab: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 0ae: bipush 1
         // 0af: if_icmpne 0ce
         // 0b2: aload 16
         // 0b4: aload 16
         // 0b6: invokevirtual q4/z/gf.isExpanded ()Z
         // 0b9: ifne 0c0
         // 0bc: bipush 1
         // 0bd: goto 0c1
         // 0c0: bipush 0
         // 0c1: invokevirtual q4/z/gf.setExpanded (Z)V
         // 0c4: aload 16
         // 0c6: invokevirtual q4/z/gf.isExpanded ()Z
         // 0c9: invokestatic q2/z/tq.expand (Z)V
         // 0EventPacket: bipush 1
         // 0cd: ireturn
         // 0ce: aload 8
         // 0d0: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 0d3: bipush 2
         // 0d4: if_icmpne 0e2
         // 0d7: aload 1
         // 0d8: aload 16
         // 0ConfigManager: putfield ru/white/screen/DropDownMenu.bindingModule Lq4/z/Module;
         // 0dd: invokestatic q2/z/tq.bindStart ()V
         // 0e0: bipush 1
         // 0e1: ireturn
         // 0e2: fload 10
         // 0e4: fload 11
         // 0e6: fload 12
         // 0e8: fadd
         // 0e9: fadd
         // 0ea: fstore 10
         // 0EventAttack: aload 16
         // 0ee: getfield q4/z/gf.expandedAnimation Lru/white/utils/animation/Animation;
         // 0f1: invokevirtual ru/white/utils/animation/Animation.get ()F
         // 0f4: fstore 17
         // 0f6: fload 17
         // 0f8: ldc_w 0.05
         // 0fb: fcmpl
         // 0EventMove: ifle 62c
         // 0Category: aload 0
         // 100: aload 16
         // 102: aload 2
         // 103: fload 5
         // 105: invokevirtual ru/white/screen/DropDownMenu$CategoryPanel.calculateSettingsHeight (Lq4/z/Module;Lx6/y34/yt;F)F
         // 108: fstore 18
         // 10a: fload 18
         // 10c: fload 17
         // 10e: fmul
         // 10f: fstore 19
         // 111: aload 16
         // 113: invokevirtual q4/z/gf.isExpanded ()Z
         // 116: ifeq 625
         // 119: fload 17
         // 11b: ldc_w 0.35
         // 11e: fcmpl
         // 11f: ifle 625
         // 122: fload 7
         // 124: fload 10
         // 126: fcmpl
         // 127: iflt 625
         // 12a: fload 7
         // 12c: fload 10
         // 12e: fload 19
         // 130: fadd
         // 131: fcmpg
         // 132: ifgt 625
         // 135: fload 3
         // 136: ldc 8.0
         // 138: getstatic ru/white/screen/DropDownMenu.S F
         // 13b: fmul
         // 13c: fadd
         // 13d: fstore 20
         // 13f: fload 5
         // 141: ldc 16.0
         // 143: getstatic ru/white/screen/DropDownMenu.S F
         // 146: fmul
         // 147: fsub
         // 148: fstore 21
         // 14a: aload 16
         // 14c: invokevirtual q4/z/gf.getSettings ()Ljava/util/List;
         // 14f: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
         // 154: astore 22
         // 156: aload 22
         // 158: invokeinterface java/util/Iterator.hasNext ()Z 1
         // 15d: ifeq 625
         // 160: aload 22
         // 162: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
         // 167: checkcast x2/y16/Setting
         // 16a: astore 23
         // 16c: aload 23
         // 16e: invokevirtual x2/y16/nf.getVisible ()Ljava/util/function/Supplier;
         // 171: invokeinterface java/util/function/Supplier.get ()Ljava/lang/Object; 1
         // 176: checkcast java/lang/Boolean
         // 179: invokevirtual java/lang/Boolean.booleanValue ()Z
         // 17c: ifeq 156
         // 17f: aload 23
         // 181: instanceof q2/z/vf
         // 184: ifeq 18a
         // 187: goto 156
         // 18a: aload 23
         // 18c: instanceof q2/z/BooleanSetting
         // 18f: ifeq 1eb
         // 192: aload 23
         // 194: checkcast q2/z/BooleanSetting
         // 197: astore 24
         // 199: ldc 14.5
         // 19b: getstatic ru/white/screen/DropDownMenu.S F
         // 19e: fmul
         // 19f: fstore 32
         // 1a1: fload 6
         // 1a3: fload 7
         // 1a5: fload 20
         // 1a7: fload 10
         // 1a9: fload 21
         // 1ab: fload 32
         // 1ad: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 1b0: ifeq 1e1
         // 1b3: aload 8
         // 1b5: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 1b8: ifne 1e1
         // 1DraggableWidget: aload 24
         // 1bd: aload 24
         // 1bf: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
         // 1c2: invokevirtual java/lang/Boolean.booleanValue ()Z
         // 1c5: ifne 1EventPacket
         // 1c8: bipush 1
         // 1c9: goto 1cd
         // 1EventPacket: bipush 0
         // 1cd: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
         // 1d0: invokevirtual q2/z/qf.set (Ljava/lang/Boolean;)Lq2/z/BooleanSetting;
         // 1d3: pop
         // 1d4: aload 24
         // 1d6: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
         // 1d9: invokevirtual java/lang/Boolean.booleanValue ()Z
         // 1EventKey: invokestatic q2/z/tq.toggle (Z)V
         // 1df: bipush 1
         // 1e0: ireturn
         // 1e1: fload 10
         // 1e3: fload 32
         // 1e5: fadd
         // 1e6: fstore 10
         // 1e8: goto 622
         // 1eb: aload 23
         // 1ed: instanceof q2/z/SliderSetting
         // 1f0: ifeq 253
         // 1f3: aload 23
         // 1f5: checkcast q2/z/SliderSetting
         // 1f8: astore 25
         // 1fa: ldc 24.0
         // 1EventMove: getstatic ru/white/screen/DropDownMenu.S F
         // 1Category: fmul
         // 200: fstore 32
         // 202: fload 10
         // 204: ldc 16.0
         // 206: getstatic ru/white/screen/DropDownMenu.S F
         // 209: fmul
         // 20a: fadd
         // 20b: fstore 33
         // 20d: ldc_w 7.0
         // 210: getstatic ru/white/screen/DropDownMenu.S F
         // 213: fmul
         // 214: fstore 34
         // 216: fload 6
         // 218: fload 7
         // 21a: fload 20
         // 21c: fload 33
         // 21e: fconst_2
         // 21f: getstatic ru/white/screen/DropDownMenu.S F
         // 222: fmul
         // 223: fsub
         // 224: fload 21
         // 226: fload 34
         // 228: ldc_w 4.0
         // 22b: getstatic ru/white/screen/DropDownMenu.S F
         // 22e: fmul
         // 22f: fadd
         // 230: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 233: ifeq 249
         // 236: aload 8
         // 238: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 23b: ifne 249
         // 23e: aload 1
         // 23f: aload 25
         // 241: putfield ru/white/screen/DropDownMenu.draggingSlider Lq2/z/SliderSetting;
         // 244: invokestatic q2/z/tq.sliderGrab ()V
         // 247: bipush 1
         // 248: ireturn
         // 249: fload 10
         // 24b: fload 32
         // 24d: fadd
         // 24e: fstore 10
         // 250: goto 622
         // 253: aload 23
         // 255: instanceof q2/z/ModeSetting
         // 258: ifeq 2d3
         // 25b: aload 23
         // 25d: checkcast q2/z/ModeSetting
         // 260: astore 26
         // 262: ldc 16.0
         // 264: getstatic ru/white/screen/DropDownMenu.S F
         // 267: fmul
         // 268: fstore 32
         // 26a: fload 6
         // 26c: fload 7
         // 26e: fload 20
         // 270: fload 10
         // 272: fload 21
         // 274: fload 32
         // 276: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 279: ifeq 2c9
         // 27c: aload 8
         // 27e: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 281: ifne 2c9
         // 284: aload 26
         // 286: getfield q2/z/xf.values Ljava/util/List;
         // 289: ifnull 2c9
         // 28c: aload 26
         // 28e: getfield q2/z/xf.values Ljava/util/List;
         // 291: invokeinterface java/util/List.isEmpty ()Z 1
         // 296: ifne 2c9
         // 299: aload 26
         // 29b: invokevirtual q2/z/xf.getIndex ()I
         // 29e: bipush 1
         // 29f: iadd
         // 2a0: aload 26
         // 2a2: getfield q2/z/xf.values Ljava/util/List;
         // 2a5: invokeinterface java/util/List.size ()I 1
         // 2aa: irem
         // 2ab: istore 33
         // 2ad: aload 26
         // 2af: aload 26
         // 2b1: getfield q2/z/xf.values Ljava/util/List;
         // 2b4: iload 33
         // 2b6: invokeinterface java/util/List.get (I)Ljava/lang/Object; 2
         // 2DraggableWidget: checkcast java/lang/String
         // 2be: invokevirtual q2/z/xf.set (Ljava/lang/String;)Lq2/z/ModeSetting;
         // 2c1: pop
         // 2c2: bipush 0
         // 2c3: bipush 1
         // 2c4: invokestatic q2/z/tq.chip (II)V
         // 2c7: bipush 1
         // 2c8: ireturn
         // 2c9: fload 10
         // 2cb: fload 32
         // 2cd: fadd
         // 2ce: fstore 10
         // 2d0: goto 622
         // 2d3: aload 23
         // 2d5: instanceof q2/z/MultiSetting
         // 2d8: ifeq 3d9
         // 2db: aload 23
         // 2dd: checkcast q2/z/MultiSetting
         // 2e0: astore 27
         // 2e2: fload 10
         // 2e4: ldc_w 11.0
         // 2e7: getstatic ru/white/screen/DropDownMenu.S F
         // 2ea: fmul
         // 2eb: fadd
         // 2EventAttack: fstore 10
         // 2ee: fload 20
         // 2f0: fconst_2
         // 2f1: getstatic ru/white/screen/DropDownMenu.S F
         // 2f4: fmul
         // 2f5: fadd
         // 2f6: fstore 32
         // 2f8: fload 20
         // 2fa: fload 21
         // 2EventMove: fadd
         // 2fd: fconst_2
         // 2fe: getstatic ru/white/screen/DropDownMenu.S F
         // 301: fmul
         // 302: fsub
         // 303: fstore 33
         // 305: aload 27
         // 307: invokevirtual q2/z/zf.getValues ()Ljava/util/Collection;
         // 30a: invokeinterface java/util/Collection.iterator ()Ljava/util/Iterator; 1
         // 30f: astore 34
         // 311: aload 34
         // 313: invokeinterface java/util/Iterator.hasNext ()Z 1
         // 318: ifeq 3ConfigFile
         // 31b: aload 34
         // 31d: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
         // 322: checkcast q2/z/BooleanSetting
         // 325: astore 35
         // 327: aload 2
         // 328: aload 35
         // 32a: invokevirtual q2/z/qf.getName ()Ljava/lang/String;
         // 32d: ldc_w 6.2
         // 330: getstatic ru/white/screen/DropDownMenu.S F
         // 333: fmul
         // 334: invokevirtual x6/y34/yt.getWidth (Ljava/lang/String;F)F
         // 337: fstore 36
         // 339: fload 36
         // 33b: ldc_w 7.0
         // 33e: getstatic ru/white/screen/DropDownMenu.S F
         // 341: fmul
         // 342: fadd
         // 343: fstore 37
         // 345: ldc_w 11.0
         // 348: getstatic ru/white/screen/DropDownMenu.S F
         // 34b: fmul
         // 34c: fstore 38
         // 34e: fload 32
         // 350: fload 37
         // 352: fadd
         // 353: fload 33
         // 355: fcmpl
         // 356: ifle 37f
         // 359: fload 32
         // 35b: fload 20
         // 35d: fconst_2
         // 35e: getstatic ru/white/screen/DropDownMenu.S F
         // 361: fmul
         // 362: fadd
         // 363: fcmpl
         // 364: ifle 37f
         // 367: fload 20
         // 369: fconst_2
         // 36a: getstatic ru/white/screen/DropDownMenu.S F
         // 36d: fmul
         // 36e: fadd
         // 36f: fstore 32
         // 371: fload 10
         // 373: fload 38
         // 375: ldc 2.5
         // 377: getstatic ru/white/screen/DropDownMenu.S F
         // 37a: fmul
         // 37b: fadd
         // 37c: fadd
         // 37d: fstore 10
         // 37f: fload 6
         // 381: fload 7
         // 383: fload 32
         // 385: fload 10
         // 387: fload 37
         // 389: fload 38
         // 38b: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 38e: ifeq 3b9
         // 391: aload 8
         // 393: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 396: ifne 3b9
         // 399: aload 35
         // 39b: aload 35
         // 39d: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
         // 3a0: invokevirtual java/lang/Boolean.booleanValue ()Z
         // 3a3: ifne 3aa
         // 3a6: bipush 1
         // 3a7: goto 3ab
         // 3aa: bipush 0
         // 3ab: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
         // 3ae: invokevirtual q2/z/qf.set (Ljava/lang/Boolean;)Lq2/z/BooleanSetting;
         // 3b1: pop
         // 3b2: bipush 0
         // 3b3: bipush 1
         // 3b4: invokestatic q2/z/tq.chip (II)V
         // 3b7: bipush 1
         // 3b8: ireturn
         // 3b9: fload 32
         // 3DraggableWidget: fload 37
         // 3bd: ldc 3.0
         // 3bf: getstatic ru/white/screen/DropDownMenu.S F
         // 3c2: fmul
         // 3c3: fadd
         // 3c4: fadd
         // 3c5: fstore 32
         // 3c7: goto 311
         // 3ConfigFile: fload 10
         // 3EventPacket: ldc_w 13.5
         // 3cf: getstatic ru/white/screen/DropDownMenu.S F
         // 3d2: fmul
         // 3d3: fadd
         // 3d4: fstore 10
         // 3d6: goto 622
         // 3d9: aload 23
         // 3db: instanceof q2/z/StringSetting
         // 3de: ifeq 44f
         // 3e1: aload 23
         // 3e3: checkcast q2/z/StringSetting
         // 3e6: astore 28
         // 3e8: fload 10
         // 3ea: ldc 9.5
         // 3EventAttack: getstatic ru/white/screen/DropDownMenu.S F
         // 3ef: fmul
         // 3f0: fadd
         // 3f1: fstore 32
         // 3f3: ldc_w 13.0
         // 3f6: getstatic ru/white/screen/DropDownMenu.S F
         // 3f9: fmul
         // 3fa: fstore 33
         // 3EventMove: fload 21
         // 3fe: ldc_w 4.0
         // 401: getstatic ru/white/screen/DropDownMenu.S F
         // 404: fmul
         // 405: fsub
         // 406: fstore 34
         // 408: fload 20
         // 40a: fconst_2
         // 40b: getstatic ru/white/screen/DropDownMenu.S F
         // 40e: fmul
         // 40f: fadd
         // 410: fstore 35
         // 412: fload 6
         // 414: fload 7
         // 416: fload 35
         // 418: fload 32
         // 41a: fload 34
         // 41c: fload 33
         // 41e: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 421: ifeq 440
         // 424: aload 8
         // 426: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 429: ifne 440
         // 42c: aload 1
         // 42d: aload 28
         // 42f: putfield ru/white/screen/DropDownMenu.activeString Lq2/z/StringSetting;
         // 432: aload 1
         // 433: aload 28
         // 435: invokevirtual q2/z/cg.getValue ()Ljava/lang/String;
         // 438: putfield ru/white/screen/DropDownMenu.stringBuffer Ljava/lang/String;
         // 43b: invokestatic q2/z/tq.editStart ()V
         // 43e: bipush 1
         // 43f: ireturn
         // 440: fload 10
         // 442: ldc_w 25.0
         // 445: getstatic ru/white/screen/DropDownMenu.S F
         // 448: fmul
         // 449: fadd
         // 44a: fstore 10
         // 44c: goto 622
         // 44f: aload 23
         // 451: instanceof q2/z/tf
         // 454: ifeq 573
         // 457: aload 23
         // 459: checkcast q2/z/tf
         // 45c: astore 29
         // 45e: ldc 16.0
         // 460: getstatic ru/white/screen/DropDownMenu.S F
         // 463: fmul
         // 464: fstore 32
         // 466: fload 6
         // 468: fload 7
         // 46a: fload 20
         // 46c: fload 10
         // 46e: fload 21
         // 470: fload 32
         // 472: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 475: ifeq 49c
         // 478: aload 8
         // 47a: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 47d: ifne 49c
         // 480: aload 29
         // 482: aload 29
         // 484: getfield q2/z/tf.pickerOpen Z
         // 487: ifne 48e
         // 48a: bipush 1
         // 48b: goto 48f
         // 48e: bipush 0
         // 48f: putfield q2/z/tf.pickerOpen Z
         // 492: aload 29
         // 494: getfield q2/z/tf.pickerOpen Z
         // 497: invokestatic q2/z/tq.picker (Z)V
         // 49a: bipush 1
         // 49b: ireturn
         // 49c: fload 10
         // 49e: fload 32
         // 4a0: fadd
         // 4a1: fstore 10
         // 4a3: aload 29
         // 4a5: getfield q2/z/tf.pickerOpen Z
         // 4a8: ifeq 570
         // 4ab: fload 20
         // 4ad: ldc_w 4.0
         // 4b0: getstatic ru/white/screen/DropDownMenu.S F
         // 4b3: fmul
         // 4b4: fadd
         // 4b5: fstore 33
         // 4b7: fload 21
         // 4b9: ldc 8.0
         // 4DraggableWidget: getstatic ru/white/screen/DropDownMenu.S F
         // 4be: fmul
         // 4bf: fsub
         // 4c0: fstore 34
         // 4c2: bipush 0
         // 4c3: istore 35
         // 4c5: iload 35
         // 4c7: bipush 3
         // 4c8: if_icmpge 564
         // 4cb: fload 10
         // 4cd: ldc_w 4.0
         // 4d0: getstatic ru/white/screen/DropDownMenu.S F
         // 4d3: fmul
         // 4d4: fadd
         // 4d5: iload 35
         // 4d7: i2f
         // 4d8: ldc_w 12.0
         // 4db: fmul
         // 4EventKey: getstatic ru/white/screen/DropDownMenu.S F
         // 4df: fmul
         // 4e0: fadd
         // 4e1: fstore 36
         // 4e3: fload 6
         // 4e5: fload 7
         // 4e7: fload 33
         // 4e9: fconst_2
         // 4ea: getstatic ru/white/screen/DropDownMenu.S F
         // 4ed: fmul
         // 4ee: fsub
         // 4ef: fload 36
         // 4f1: ldc 3.0
         // 4f3: getstatic ru/white/screen/DropDownMenu.S F
         // 4f6: fmul
         // 4f7: fsub
         // 4f8: fload 34
         // 4fa: ldc_w 4.0
         // 4fd: getstatic ru/white/screen/DropDownMenu.S F
         // 500: fmul
         // 501: fadd
         // 502: ldc_w 10.5
         // 505: getstatic ru/white/screen/DropDownMenu.S F
         // 508: fmul
         // 509: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 50c: ifeq 55e
         // 50f: aload 8
         // 511: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 514: ifne 55e
         // 517: aload 1
         // 518: aload 29
         // 51a: putfield ru/white/screen/DropDownMenu.draggingColor Lq2/z/tf;
         // 51d: aload 1
         // 51e: iload 35
         // 520: putfield ru/white/screen/DropDownMenu.draggingColorBar I
         // 523: fload 6
         // 525: fload 33
         // 527: fsub
         // 528: fload 34
         // 52a: fdiv
         // 52b: fconst_0
         // 52c: fconst_1
         // 52d: invokestatic net/minecraft/util/math/MathHelper.method_15363 (FFF)F
         // 530: fstore 37
         // 532: aload 29
         // 534: iload 35
         // 536: fload 37
         // 538: invokevirtual q2/z/tf.updateHsbBar (IF)Z
         // 53b: pop
         // 53c: invokestatic q2/z/tq.sliderGrab ()V
         // 53f: bipush 1
         // 540: ldc_w -838289256
         // 543: istore 40
         // 545: iload 40
         // 547: dup
         // 548: bipush 1
         // 549: iadd
         // 54a: imul
         // 54b: bipush 2
         // 54c: irem
         // 54d: ifeq 55c
         // 550: aconst_null
         // 551: pop
         // 552: ldc_w "2S/ZojMMEzG0yYC8kScoW0327cCIIjA="
         // 555: ldc_w 2113355784
         // 558: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
         // 55b: pop
         // 55c: nop
         // 55d: ireturn
         // 55e: iinc 35 1
         // 561: goto 4c5
         // 564: fload 10
         // 566: ldc_w 41.0
         // 569: getstatic ru/white/screen/DropDownMenu.S F
         // 56c: fmul
         // 56d: fadd
         // 56e: fstore 10
         // 570: goto 622
         // 573: aload 23
         // 575: instanceof q2/z/SettingVisibility
         // 578: ifeq 5b9
         // 57b: aload 23
         // 57d: checkcast q2/z/SettingVisibility
         // 580: astore 30
         // 582: ldc 16.0
         // 584: getstatic ru/white/screen/DropDownMenu.S F
         // 587: fmul
         // 588: fstore 32
         // 58a: fload 6
         // 58c: fload 7
         // 58e: fload 20
         // 590: fload 10
         // 592: fload 21
         // 594: fload 32
         // 596: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 599: ifeq 5af
         // 59c: aload 8
         // 59e: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 5a1: ifne 5af
         // 5a4: aload 1
         // 5a5: aload 30
         // 5a7: putfield ru/white/screen/DropDownMenu.activeBind Lq2/z/SettingVisibility;
         // 5aa: invokestatic q2/z/tq.bindStart ()V
         // 5ad: bipush 1
         // 5ae: ireturn
         // 5af: fload 10
         // 5b1: fload 32
         // 5b3: fadd
         // 5b4: fstore 10
         // 5b6: goto 622
         // 5b9: aload 23
         // 5DraggableWidget: instanceof q2/z/sf
         // 5be: ifeq 60e
         // 5c1: aload 23
         // 5c3: checkcast q2/z/sf
         // 5c6: astore 31
         // 5c8: ldc 16.0
         // 5ConfigFile: getstatic ru/white/screen/DropDownMenu.S F
         // 5cd: fmul
         // 5ce: fstore 32
         // 5d0: ldc_w -132810077
         // 5d3: istore 39
         // 5d5: iload 39
         // 5d7: dup
         // 5d8: bipush 1
         // 5d9: iadd
         // 5ConfigManager: imul
         // 5db: bipush 2
         // 5EventKey: irem
         // 5dd: ifeq 5e2
         // 5e0: aconst_null
         // 5e1: pop
         // 5e2: nop
         // 5e3: fload 6
         // 5e5: fload 7
         // 5e7: fload 20
         // 5e9: fload 10
         // 5eb: fload 21
         // 5ed: fload 32
         // 5ef: invokestatic x30/fq.isHovered (FFFFFF)Z
         // 5f2: ifeq 604
         // 5f5: aload 8
         // 5f7: invokevirtual net/minecraft/client/gui/Click.method_74245 ()I
         // 5fa: ifne 604
         // 5fd: aload 31
         // 5Category: invokevirtual q2/z/sf.press ()V
         // 602: bipush 1
         // 603: ireturn
         // 604: fload 10
         // 606: fload 32
         // 608: fadd
         // 609: fstore 10
         // 60b: goto 622
         // 60e: aload 23
         // 610: instanceof q2/z/uf
         // 613: ifeq 622
         // 616: fload 10
         // 618: ldc_w 13.0
         // 61b: getstatic ru/white/screen/DropDownMenu.S F
         // 61e: fmul
         // 61f: fadd
         // 620: fstore 10
         // 622: goto 156
         // 625: fload 10
         // 627: fload 19
         // 629: fadd
         // 62a: fstore 10
         // 62c: goto 053
         // 62f: bipush 0
         // 630: ireturn
      }

      @Override
      public boolean isIconFontNightix() {

         return true;
      }

      @Override
      public void renderContent(DropDownMenu menu, yt font, float px, float startY, float pw, float mouseX, float mouseY, float globalAnim) {
         List v9 = Client.get().moduleManager().values().stream().filter(m -> {
            Category var10000x = m.getCategory();

            return var10000x == this.category;
         }).toList();
         float v10 = startY;
         float v11 = 16.0F * DropDownMenu.S;
         float v12 = 2.5F * DropDownMenu.S;
         float v13 = px + 4.0F * DropDownMenu.S;
         float v14 = pw - 8.0F * DropDownMenu.S;

         for (Module v16 : v9) {
            boolean v17 = DropDownMenu.moduleMatchesSearch(v16);

            if (DropDownMenu.searchQuery.trim().isEmpty() || v17) {
               float v18 = v17 ? 1.0F : 0.25F;
               boolean v19 = fq.isHovered(mouseX, mouseY, v13, v10, v14, v11);
               int v20;
               int v21;
               if (v16.isEnabled()) {
                  v20 = up.replAlpha(up.client(), (v19 ? 0.28F : 0.2F) * globalAnim * v18);
                  v21 = up.replAlpha(up.client(), (v19 ? 0.85F : 0.6F) * globalAnim * v18);
               } else {
                  v20 = v19
                     ? up.getColor(28, 28, 35, (int)(230.0F * globalAnim * v18))
                     : up.getColor(18, 18, 24, (int)(215.0F * globalAnim * v18));
                  v21 = v19
                     ? up.getColor(255, (int)(30.0F * globalAnim * v18))
                     : up.getColor(255, (int)(12.0F * globalAnim * v18));
               }

               vr.rect(v13, v10, v14, v11, v20, 3.5F * DropDownMenu.S);
               vr.outline(v13, v10, v14, v11, 0.5F * DropDownMenu.S, v21, 3.5F * DropDownMenu.S);
               int v22 = v16.isEnabled()
                  ? up.getColor(255, 255, 255, (int)(255.0F * globalAnim * v18))
                  : up.getColor(155, 155, 165, (int)(220.0F * globalAnim * v18));
               font.draw(v16.getBigName(), v13 + 6.0F * DropDownMenu.S, v10 + 4.2F * DropDownMenu.S, 6.8F * DropDownMenu.S, v22);
               float v23 = v13 + v14 - 7.0F * DropDownMenu.S;
               if (v16.isEnabled()) {
                  float v24 = 1.6F * DropDownMenu.S;
                  vr.glow(
                     v23 - v24 - 1.0F * DropDownMenu.S,
                     v10 + v11 / 2.0F - v24 - 1.0F * DropDownMenu.S,
                     (v24 + 1.0F * DropDownMenu.S) * 2.0F,
                     (v24 + 1.0F * DropDownMenu.S) * 2.0F,
                     up.replAlpha(up.client(), 0.45F * globalAnim * v18),
                     v24 + 1.0F * DropDownMenu.S,
                     3.0F * DropDownMenu.S,
                     0.5F
                  );
                  vr.rect(v23 - v24, v10 + v11 / 2.0F - v24, v24 * 2.0F, v24 * 2.0F, up.replAlpha(up.client(), globalAnim * v18), v24);
               } else if (!v16.getSettings().isEmpty()) {
                  float v32 = 1.3F * DropDownMenu.S;
                  float result = v23 - v32;
                  float v36 = v10 + v11 / 2.0F - v32;
                  float temp2 = v32 * 2.0F;
                  float temp3 = v32 * 2.0F;

                  vr.rect(
                     result,
                     v36,
                     temp2,
                     temp3,
                     up.getColor(v37, 70, 80, (int)(180.0F * globalAnim * v18)),
                     0.6F * DropDownMenu.S
                  );
               }

               String v33 = null;
               if (menu.bindingModule == v16) {
                  v33 = "[...]";
               } else if (v16.getKey() > 0) {
                  String v28 = eq.keyName(v16.getKey()).replace("NONE", "n/a");
                  v33 = "[" + v28 + "]";
               }

               if (v33 != null) {
                  float v25 = font.getWidth(v33, 6.0F * DropDownMenu.S);
                  int v26 = menu.bindingModule == v16
                     ? up.replAlpha(up.client(), globalAnim)
                     : up.getColor(125, 125, 135, (int)(210.0F * globalAnim * v18));
                  font.draw(v33, v23 - 6.0F * DropDownMenu.S - v25, v10 + 4.5F * DropDownMenu.S, 6.0F * DropDownMenu.S, v26);
               }

               v10 += v11 + v12;
               v16.expandedAnimation.update();
               v16.expandedAnimation.run(v16.isExpanded() ? 1.0 : 0.0, 0.22F, ap.CUBIC_OUT);
               float v34 = v16.expandedAnimation.get();
               if (v34 > 0.005F) {
                  float v35 = this.calculateSettingsHeight(v16, font, pw);
                  float v27 = v35 * v34;
                  dt.enable(px, v10, pw, v27, 2.0F);
                  this.renderSettings(menu, v16, font, px, v10, pw, mouseX, mouseY, globalAnim * v18 * v34);
                  dt.disable();
                  v10 += v27;
               }
            }
         }
      }

      @Override
      public float calculateContentHeight(yt font, float pw) {
         float v3 = 3.0F * DropDownMenu.S;

         for (Object result : Client.get().moduleManager().values().stream().filter(m -> {
            boolean v5;
            if (m.getCategory() == this.category) {

               v5 = true;
            } else {
               v5 = false;
            }

            return v5;
         }).toList()) {

            Module v6 = (Module)result;
            if (DropDownMenu.searchQuery.trim().isEmpty() || DropDownMenu.moduleMatchesSearch(v6)) {
               v3 += 16.0F * DropDownMenu.S + 2.5F * DropDownMenu.S;
               v6.expandedAnimation.update();

               v6.expandedAnimation.run(v6.isExpanded() ? 1.0 : 0.0, 0.22F, ap.CUBIC_OUT);
               float v7 = v6.expandedAnimation.get();
               if (v7 > 0.005F) {

                  v3 += this.calculateSettingsHeight(v6, font, pw) * v7;
               }
            }
         }

         return v3 + 4.0F * DropDownMenu.S;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public abstract static class DropDownPanel {
      public float scrollTarget;
      public float width;
      public final ep collapseAnim;
      public float smoothBodyH;
      public float x;
      public float y;
      public float lastVisibleBodyH = 0.0F;
      public boolean collapsed;
      public float scrollAnim;
      public float currentHeight;
      public float maxScroll;

      public abstract boolean mouseClickedContent(DropDownMenu v1, yt v2, float v3, float v4, float v5, float v6, float v7, Click v8);

      public abstract String getIcon();

      public abstract void renderContent(DropDownMenu v1, yt v2, float v3, float v4, float v5, float v6, float v7, float v8);

      public abstract boolean isIconFontNightix();

      public DropDownPanel(float x, float y, float width) {
         this.smoothBodyH = 0.0F;
         this.collapsed = true;
         this.collapseAnim = new ep(250, 1.0);
         this.scrollTarget = 0.0F;
         this.scrollAnim = 0.0F;
         this.maxScroll = 0.0F;
         this.x = x;
         this.y = y;
         this.width = width;
         this.collapseAnim.setDirection(cp.BACKWARDS);
      }

      public abstract float calculateContentHeight(yt v1, float v2);

      public abstract String getTitle();

      
   }

   @Environment(EnvType.CLIENT)
   public static class ThemePanel extends DropDownMenu.DropDownPanel {
      @Override
      public String getIcon() {
         return "F";
      }

      @Override
      public float calculateContentHeight(yt font, float pw) {
         Theme[] v3 = Theme.values();
         float v4 = 15.5F * DropDownMenu.S;
         float v5 = 2.5F * DropDownMenu.S;
         float result = 3.0F * DropDownMenu.S + (float)v3.length * (v4 + v5);

         return result + 3.0F * DropDownMenu.S;
      }

      @Override
      public boolean isIconFontNightix() {
         return false;
      }

      @Override
      public String getTitle() {
         return "Theme";
      }

      public ThemePanel(float x, float y, float width) {
         super(x, y, width);
      }

      @Override
      public boolean mouseClickedContent(DropDownMenu menu, yt font, float px, float startY, float pw, float mouseX, float mouseY, Click click) {
         Theme[] v9 = Theme.values();

         float v10 = startY;
         float v11 = 15.5F * DropDownMenu.S;
         float v12 = 2.5F * DropDownMenu.S;
         float v13 = px + 4.0F * DropDownMenu.S;
         float v14 = pw - 8.0F * DropDownMenu.S;

         for (Theme v18 : v9) {
            if (fq.isHovered(mouseX, mouseY, v13, v10, v14, v11) && click.method_74245() == 0) {
               Client.get().guiManager().setGuiTheme(v18);
               ko.selectedTheme = v18;
               ko.preSelectedTheme = v18;
               tq.chip(0, 1);
               return true;
            }

            v10 += v11 + v12;
         }

         return false;
      }

      @Override
      public void renderContent(DropDownMenu menu, yt font, float px, float startY, float pw, float mouseX, float mouseY, float globalAnim) {
         Theme[] v9 = Theme.values();
         Theme v10 = Client.get().guiManager().getCurrentTheme();
         float v11 = startY;
         float v12 = 15.5F * DropDownMenu.S;
         float v13 = 2.5F * DropDownMenu.S;
         float v14 = px + 4.0F * DropDownMenu.S;
         float v15 = pw - 8.0F * DropDownMenu.S;

         for (Theme result : v9) {

            Theme v19 = result;
            boolean v20 = v19 == v10;
            boolean v21 = fq.isHovered(mouseX, mouseY, v14, v11, v15, v12);
            int v22 = v20
               ? up.replAlpha(v19.getClient(), (0.24F + (v21 ? 0.08F : 0.0F)) * globalAnim)
               : (
                  v21
                     ? up.getColor(28, 28, 35, (int)(230.0F * globalAnim))
                     : up.getColor(18, 18, 24, (int)(215.0F * globalAnim))
               );
            int v31;
            if (v20) {
               v31 = up.replAlpha(v19.getClient(), 0.65F * globalAnim);
            } else if (v21) {

               v31 = up.getColor(255, (int)(25.0F * globalAnim));
            } else {
               v31 = up.getColor(255, (int)(10.0F * globalAnim));
            }

            int v23 = v31;
            vr.rect(v14, v11, v15, v12, v22, 3.5F * DropDownMenu.S);
            vr.outline(v14, v11, v15, v12, 0.5F * DropDownMenu.S, v23, 3.5F * DropDownMenu.S);
            int v24 = v20
               ? up.getColor(255, 255, 255, (int)(255.0F * globalAnim))
               : up.getColor(165, 165, 175, (int)(220.0F * globalAnim));
            font.draw(v19.getName(), v14 + 6.0F * DropDownMenu.S, v11 + 4.0F * DropDownMenu.S, 6.8F * DropDownMenu.S, v24);

            float v25 = 3.0F * DropDownMenu.S;
            float v26 = v14 + v15 - 7.5F * DropDownMenu.S;
            float v27 = v11 + v12 / 2.0F;
            if (v20) {
               vr.glow(
                  v26 - v25 - 1.0F * DropDownMenu.S,
                  v27 - v25 - 1.0F * DropDownMenu.S,
                  (v25 + 1.0F * DropDownMenu.S) * 2.0F,
                  (v25 + 1.0F * DropDownMenu.S) * 2.0F,
                  up.replAlpha(v19.getClient(), 0.4F * globalAnim),
                  v25 + 1.0F * DropDownMenu.S,
                  3.0F * DropDownMenu.S,
                  0.5F
               );
            }

            vr.rect(v26 - v25, v27 - v25, v25 * 2.0F, v25 * 2.0F, v19.getClient(), v25);
            v11 += v12 + v13;
         }
      }

      
   }
}
