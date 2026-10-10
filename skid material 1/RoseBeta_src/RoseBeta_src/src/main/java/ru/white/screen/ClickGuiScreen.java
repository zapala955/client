package ru.white.screen;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.resource.Resource;
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
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.Animation;
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
import ru.white.utils.render.zr;
import ru.white.module.setting.Setting;
import ru.white.utils.IMinecraft;
import ru.white.utils.SoundUtil;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class ClickGuiScreen extends Screen implements IMinecraft {
   private final go f1139;
   public static Animation animation14 = new ep(300, 1.0);
   private final fs f1168;
   private int f1145;
   private final HashMap<String, Animation> f1167;
   double lastMouseY;
   private float f1149;
   private float f1140;
   private final Animation f1166;
   private int f1158;
   public static Animation animCategoryReset = new ep(300, 1.0);
   private long f1175;
   private SettingVisibility f1153;
   private float f1143;
   public Module select;
   public static final Animation animThemeMenu = new ep(250, 1.0);
   private final IdentityHashMap<Module, Animation> f1182;
   public static Animation animation4 = new ep(300, 1.0);
   private final ct f1170;
   public static Animation animation2 = new ep(300, 1.0);
   private SliderSetting f1152;
   public static Theme preSelectedTheme;
   boolean exit = false;
   public static Animation animUserInfo = new ep(300, 1.0);
   private final HashMap<String, float[]> f1177;
   private Module f1154;
   private static final Identifier ANIME_GUI_TEX = Identifier.method_60655("client", "textures/anime_gui.png");
   public ru.white.utils.animation.Animation glomalAnim = new ru.white.utils.animation.Animation();
   public Category active;
   private final IdentityHashMap<Module, Long> f1181;
   private static final Identifier DONT_ACTIVE_TEX = Identifier.method_60655("client", "textures/dont_active.png");
   private final Animation f1165;
   private long f1174;
   private final Animation f1164;
   public static Animation animation1 = new ep(300, 1.0);
   private static String f1162 = "";
   private static final long THEME_WAVE_MS = 650L;
   private final HashMap<String, Float> f1176;
   private boolean f1161;
   private final IdentityHashMap<Setting<?>, Animation> f1183;
   private static final Identifier ROSE_LOGO = Identifier.method_60655(
      "client", "textures/rose__logo_watermark.png"
   );
   public static boolean searchActive = false;
   double lastMouseX;
   private final List<ko.lo> f1173;
   private final IdentityHashMap<Setting<?>, float[]> f1184;
   public static Theme selectedTheme;
   private float f1159;
   private tf f1157;
   private float f1147;
   private float f1148;
   private float f1144;
   private long f1163;
   public static Animation animation3 = new ep(300, 1.0);
   private static final Identifier AVATAR_TEX = Identifier.method_60655(
      "client", "textures/user_clickgui_avatar.png"
   );
   private final Set<Module> f1180;
   private String f1179;
   private final ur f1169;
   private static final int SEARCH_LIMIT = 24;
   private float f1151;
   private StringSetting f1155;
   private final gs f1172;
   private boolean f1141;
   private final Map<Setting<?>, Boolean> f1178;
   public static float S = 1.0F;
   private zr gif;
   public static boolean themeMenuOpen = false;
   private String f1156;
   private float f1160;
   private final qs f1171;
   public static Theme[] themes;
   private float f1150;
   private long f1142;
   private float f1146;

   private void m1394(int screenWidth, int screenHeight) {
      if (System.currentTimeMillis() - this.f1174 >= this.f1175) {

         this.f1174 = System.currentTimeMillis();
         this.f1175 = -2540303484785464066L + 2540303484785464136L + (long)(Math.random() * 90.0);

         if (this.f1173.size() <= 60) {
            this.f1173.add(new ko.lo((float)(Math.random() * (double)screenWidth), (float)(Math.random() * (double)screenHeight)));
         }
      }
   }

   private float m1398(yt font, String desc) {
      float v3 = S;
      Float result = this.f1176.computeIfAbsent(desc + "_" + v3, d -> {
         int v3x = -471376899;
         int temp4 = v3x + 1;

         return font.getWrappedHeight(desc, 125.0F * S, 6.0F * S);
      });

      return result;
   }

   public void method_25432() {
      so.closeAll();

      super.method_25432();
   }

   private float m1413(Module f, float innerW, yt draw) {
      float v4 = 0.0F;
      Iterator v5 = f.getSettings().iterator();

      while (v5.hasNext()) {

         Setting v6 = (Setting)v5.next();

         v4 += this.m1412(f, v6, innerW, draw);
      }

      return v4 > 0.0F ? v4 + 8.0F * S : 0.0F;
   }

   private void m1430() {
      if (!f1162.isEmpty()) {
         f1162 = "";

         int v4 = v1 + 1;

         this.m1428();
      }
   }

   private void m1438(float mainX, float mainY, float wMain, float globalAnim, double mouseX, double mouseY, yt draw, yt regular) {
      animThemeMenu.setDirection(themeMenuOpen ? cp.FORWARDS : cp.BACKWARDS);
      float v11 = animThemeMenu.getOutput();
      float v12 = 16.0F * S;
      float v13 = mainY + 5.5F * S;
      float v14 = mainX + wMain - 14.0F * S;
      float v15 = 54.0F * S;
      float v16 = 200.0F * S;
      float v17 = v15 + (v16 - v15) * v11;
      float v18 = v14 - v17;
      vs.ys.glow(v18, v13, v17, v12, up.getColor(0, 0.18F * globalAnim), 5.0F * S, 8.0F, 1.0F);
      vs.ws.blur(v18, v13, v17, v12, globalAnim, 5.0F * S, up.multAlpha(up.multDark(up.background(), 0.7F), globalAnim));
      vs.ys.rect(
         v18,
         v13,
         v17,
         v12,
         up.getColor(20, 18, 28, (int)(230.0F * globalAnim)),
         5.0F * S
      );
      vs.ys.outline(v18, v13, v17, v12, 0.5F * S, up.replAlpha(up.client(), (0.35F + 0.35F * v11) * globalAnim), 5.0F * S);
      dt.enable(v18 - 1.0F * S, v13 - 1.0F * S, v17 + 2.0F * S, v12 + 2.0F * S, 2.0F);
      if (v11 > 0.05F) {
         float v19 = v11 * globalAnim;
         float v20 = v18 + 6.0F * S;
         float v21 = 3.0F * S;
         float v22 = 12.0F * S;

         for (int v23 = 0; v23 < themes.length; v23++) {
            Theme v24 = themes[v23];
            float v25 = v20 + (float)v23 * (v22 + v21);
            float v26 = v25 + v22 / 2.0F;
            float v27 = v13 + v12 / 2.0F;
            float v28 = 4.2F * S;
            boolean v29 = v24 == selectedTheme;
            boolean v30 = fq.isHovered((float)mouseX, (float)mouseY, v25 - 1.0F * S, v13 + 1.0F * S, v22 + 2.0F * S, v12 - 2.0F * S);
            if (v29) {
               float result = v26 - v28 - 2.5F * S;
               float temp1 = v27 - v28 - 2.5F * S;

               vr.glow(
                  result,
                  temp1,
                  (v28 + 2.5F * S) * 2.0F,
                  (v28 + 2.5F * S) * 2.0F,
                  up.replAlpha(v24.getClient(), 0.55F * v19),
                  v28 + 2.5F * S,
                  3.0F * S,
                  0.8F
               );
               vs.ys.outline(
                  v26 - v28 - 1.8F * S,
                  v27 - v28 - 1.8F * S,
                  (v28 + 1.8F * S) * 2.0F,
                  (v28 + 1.8F * S) * 2.0F,
                  0.8F * S,
                  up.replAlpha(v24.getClient(), 0.95F * v19),
                  v28 + 1.8F * S
               );
               vs.ys.rect(v26 - v28, v27 - v28, v28 * 2.0F, v28 * 2.0F, up.replAlpha(v24.getClient(), v19), v28);
               vs.ys.rect(v26 - 1.2F * S, v27 - 1.2F * S, 2.4F * S, 2.4F * S, up.getColor(255, v19), 1.2F * S);
            } else {
               if (v30) {
                  vr.glow(
                     v26 - v28 - 1.5F * S,
                     v27 - v28 - 1.5F * S,
                     (v28 + 1.5F * S) * 2.0F,
                     (v28 + 1.5F * S) * 2.0F,
                     up.replAlpha(v24.getClient(), 0.4F * v19),
                     v28 + 1.5F * S,
                     2.0F * S,
                     0.7F
                  );
                  vs.ys.outline(
                     v26 - v28 - 1.2F * S,
                     v27 - v28 - 1.2F * S,
                     (v28 + 1.2F * S) * 2.0F,
                     (v28 + 1.2F * S) * 2.0F,
                     0.5F * S,
                     up.getColor(255, (int)(180.0F * v19)),
                     v28 + 1.2F * S
                  );
                  String v31 = v24.getName();
                  this.f1179 = "Тема: " + v31;
               }

               vs.ys.rect(v26 - v28, v27 - v28, v28 * 2.0F, v28 * 2.0F, up.replAlpha(v24.getClient(), (v30 ? 1.0F : 0.85F) * v19), v28);
            }
         }

         float v38 = v20 + (float)themes.length * (v22 + v21) + 2.0F * S;
         vs.ys.rect(v38, v13 + 3.5F * S, 0.5F * S, v12 - 7.0F * S, up.getColor(255, (int)(25.0F * v19)), 0.25F * S);
         float v39 = v38 + 4.0F * S;
         float v40 = 44.0F * S;
         boolean v41 = fq.isHovered((float)mouseX, (float)mouseY, v39, v13 + 2.0F * S, v40, v12 - 4.0F * S);
         vs.ys.rect(v39, v13 + 2.0F * S, v40, v12 - 4.0F * S, up.replAlpha(up.client(), (v41 ? 0.3F : 0.15F) * v19), 3.5F * S);
         vs.ys.outline(v39, v13 + 2.0F * S, v40, v12 - 4.0F * S, 0.4F * S, up.replAlpha(up.client(), (v41 ? 0.9F : 0.35F) * v19), 3.5F * S);
         hu.icon.draw("S", v39 + 4.5F * S, v13 + 4.5F * S, 5.5F * S, up.replAlpha(up.client(), v19));
         hu.sf_bold.draw("Редактор", v39 + 13.0F * S, v13 + 4.2F * S, 5.2F * S, up.getColor(255, (v41 ? 1.0F : 0.85F) * v19));
         float v42 = v39 + v40 + 4.0F * S;
         float v43 = 12.0F * S;
         boolean v44 = fq.isHovered((float)mouseX, (float)mouseY, v42, v13 + 2.0F * S, v43, v12 - 4.0F * S);
         vs.ys.rect(
            v42,
            v13 + 2.0F * S,
            v43,
            v12 - 4.0F * S,
            up.getColor(255, (int)((float)(v44 ? 25 : 10) * v19)),
            3.5F * S
         );
         hu.sf_bold.drawCentered("✕", v42 + v43 / 2.0F, v13 + 4.0F * S, 5.0F * S, up.getColor(255, (v44 ? 0.95F : 0.45F) * v19));
      }

      if (v11 < 0.95F) {
         float v34 = (1.0F - v11) * globalAnim;
         float v35 = 3.5F * S;
         float v36 = v18 + 9.0F * S;
         float v37 = v13 + v12 / 2.0F;
         vr.glow(
            v36 - v35 - 1.0F * S,
            v37 - v35 - 1.0F * S,
            (v35 + 1.0F * S) * 2.0F,
            (v35 + 1.0F * S) * 2.0F,
            up.replAlpha(selectedTheme.getClient(), 0.45F * v34),
            v35 + 1.0F * S,
            2.0F * S,
            0.7F
         );
         float v45 = v36 - v35;
         float v46 = v37 - v35;
         float temp2 = v35 * 2.0F;
         float temp3 = v35 * 2.0F;

         vs.ys.rect(v45, v46, temp2, temp3, up.replAlpha(selectedTheme.getClient(), v34), v35);
         regular.draw(selectedTheme.getName(), v18 + 17.0F * S, v13 + 4.5F * S, 5.8F * S, up.getColor(255, 0.88F * v34));
         regular.draw("v", v14 - 10.0F * S, v13 + 4.5F * S, 5.5F * S, up.getColor(255, 0.45F * v34));
      }

      dt.disable();
   }

   private void m1432(float ox, float oy, int toColor) {
      this.f1143 = ox;
      this.f1144 = oy;
      this.f1145 = toColor;
      this.f1142 = System.currentTimeMillis();
      this.f1141 = true;
      int temp1 = 650;

      animation14.setDuration(temp1);
      animation14.reset();
   }

   private void m1416(Module f, float cx, float cy, float colW, float globalAnim, yt draw, yt regular, float clipTop, float clipBottom) {
      float v10 = this.m1414(f, colW, draw);
      Animation result = f.getAnimation16();

      result.setDirection(f.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);
      float v11 = f.getAnimation16().getOutput();
      boolean v12 = cy + 22.0F * S >= clipTop - 4.0F * S && cy <= clipBottom + 4.0F * S;
      boolean v13 = v12 && fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, cx, cy, colW, 22.0F * S);
      f.getAnimation12().setDirection(v13 ? cp.FORWARDS : cp.BACKWARDS);
      float v14 = f.getAnimation12().getOutput();
      f.animation1.setDirection(f == this.select ? cp.FORWARDS : cp.BACKWARDS);
      float v15 = f.animation1.getOutput();
      long v16 = this.f1181.getOrDefault(f, Long.valueOf(0L));
      long v18 = System.currentTimeMillis() - v16;
      float v20 = v18 < -5288449316209867024L + 5288449316209867724L ? 1.0F - (float)v18 / 700.0F : 0.0F;
      if (v20 > 0.001F) {
         v20 = (float)Math.sin((double)v20 * Math.PI / 2.0);
         if (v18 < -131469113555985606L + 131469113555985886L) {
            float v21 = (float)(Math.sin((double)((float)v18 * 0.045F)) * 2.5 * (double)S * (double)v20);
            cx += v21;
         }
      }

      vs.ys.glow(cx, cy, colW, v10, up.getColor(0, 0.05F * globalAnim), 7.0F * S, 8.0F, 1.0F);
      vs.ys.rect(cx, cy, colW, v10, up.getColor(255, (int)((10.0F + 14.0F * v11 + 8.0F * v14) * globalAnim)), 7.0F * S);
      if (v20 > 0.01F) {
         int v46 = up.getColor(255, 55, 55, (int)(245.0F * v20 * globalAnim));
         vs.ys.outline(cx, cy, colW, v10, 0.8F * S, v46, 7.0F * S);
         vs.ys.glow(cx, cy, colW, v10, up.replAlpha(v46, 0.45F * v20 * globalAnim), 7.0F * S, 10.0F, 1.0F);
      } else if (v15 > 0.01F) {
         vs.ys.outline(cx, cy, colW, v10, 0.5F * S, up.replAlpha(up.client(), 0.35F * globalAnim * v15), 7.0F * S);
      }

      float v47 = cx + colW - 8.0F * S;
      float v22 = this.m1401(f) ? this.m1404(f) : 0.0F;
      if (this.m1401(f)) {
         float v23 = (v22 > 0.5F ? 1.0F - v22 : v22) * 1.5F * S;
         String v24 = v22 > 0.5F ? "^" : "v";
         draw.draw(
            v24,
            v47 - draw.getWidth(v24, 6.0F * S),
            cy + 7.0F * S + v23,
            6.0F * S,
            up.getColor(255, (0.35F + 0.4F * v22) * globalAnim)
         );
      }

      v47 -= 12.0F * S;
      float v51 = 22.0F * S;
      float v52 = 11.0F * S;
      this.m1415(v47 - v51, cy + 5.5F * S, v51, v52, v11, v14, globalAnim);
      v47 -= v51 + 5.0F * S;
      float v25 = 11.0F * S;
      float v26 = v47 - v25;
      float v27 = cy + 5.5F * S;
      boolean v28 = v12
         && fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v26 - 2.0F * S, v27 - 2.0F * S, v25 + 4.0F * S, v25 + 4.0F * S);
      if (v28) {
         this.f1179 = f.getDesc();
      }

      vr.glow(v26, v27, v25, v25, up.replAlpha(up.client(), (v28 ? 0.45F : 0.2F) * globalAnim), v25 / 2.0F, 3.0F, 0.8F);
      vs.ys.rect(v26, v27, v25, v25, up.getColor(255, (int)(18.0F * globalAnim)), v25 / 2.0F);
      vr.outline(
         v26,
         v27,
         v25,
         v25,
         0.5F * S,
         v28 ? up.replAlpha(up.client(), 0.9F * globalAnim) : up.getColor(255, (int)(65.0F * globalAnim)),
         v25 / 2.0F
      );
      regular.drawCentered("i", v26 + v25 / 2.0F, v27 + 2.5F * S, 5.5F * S, up.getColor(255, (v28 ? 0.9F : 0.45F) * globalAnim));
      v47 = v26 - 5.0F * S;
      boolean v29 = this.f1154 == f;
      float v30 = v47 - (cx + 8.0F * S) - 4.0F * S;
      if (f.getKey() > 0 || v29) {
         String v31 = v29 ? "..." : eq.keyName(f.getKey()).replace("NONE", "n/a");
         float v32 = draw.getWidth(v31, 5.5F * S) + 10.0F * S;
         float v33 = v47 - v32;
         vs.ys.rect(v33, cy + 6.0F * S, v32, 10.0F * S, up.getColor(255, (int)(14.0F * globalAnim)), 3.0F * S);
         if (v29) {
            vs.ys.outline(v33, cy + 6.0F * S, v32, 10.0F * S, 0.5F * S, up.replAlpha(up.client(), globalAnim), 3.0F * S);
         }

         regular.drawCentered(v31, v33 + v32 / 2.0F, cy + 7.5F * S, 5.5F * S, up.getColor(255, 0.6F * globalAnim));
         v30 = v33 - (cx + 8.0F * S) - 4.0F * S;
      }

      if (v30 > 20.0F * S) {
         draw.drawFadingText(
            f.getBigName(),
            cx + 8.0F * S,
            cy + 6.5F * S,
            v30,
            up.getColor(255, globalAnim * (0.55F + 0.37F * Math.max(v11, v14))),
            7.0F * S
         );
      } else {
         draw.draw(
            f.getBigName(), cx + 8.0F * S, cy + 6.5F * S, 7.0F * S, up.getColor(255, globalAnim * (0.55F + 0.37F * Math.max(v11, v14)))
         );
      }

      if (!(v22 <= 0.005F)) {
         float v53 = cy + 22.0F * S;
         float v54 = v10 - 22.0F * S;
         if (v54 > 0.5F * S) {
            dt.enable(cx - 1.0F * S, v53, colW + 2.0F * S, v54 + 1.0F * S, 2.0F);
            float v55 = cx + 8.0F * S;
            float v34 = colW - 16.0F * S;
            float v35 = (1.0F - v22) * 7.0F * S;
            float v36 = v53 + 2.0F * S - v35;
            float v37 = globalAnim * v22;
            vs.ys.rect(v55, v53 - 0.5F * S, v34, 0.5F * S, up.getColor(255, (int)(14.0F * v37)), 0.25F * S);

            for (Setting v39 : f.getSettings()) {
               float v40 = this.m1412(f, v39, v34, draw);
               if (!(v40 <= 0.0F)) {
                  if (v36 + v40 >= clipTop - 30.0F * S && v36 <= clipBottom + 30.0F * S) {
                     this.m1417(f, v39, v55, v36, v34, v37, draw, regular);
                  }

                  if (v39 instanceof SliderSetting) {
                     SliderSetting v41 = (SliderSetting)v39;
                     if (this.f1152 == v41) {
                        float v42 = v34 - 4.0F * S;
                        float v43 = fq.clamp(((float)this.lastMouseX - (v55 + 2.0F * S)) / v42, 0.0F, 1.0F);
                        float v44 = fq.clamp(fq.round(v41.min + (v41.max - v41.min) * v43, v41.increment), v41.min, v41.max);
                        if (v41.getValue() != v44) {
                           v41.set(v44);
                           tq.sliderTick(v43);
                        }
                     }
                  }

                  if (v39 instanceof tf) {
                     tf v56 = (tf)v39;
                     if (this.f1157 == v56 && v56.pickerOpen) {
                        float v57 = v55 + 2.0F * S;
                        float v58 = v34 - 4.0F * S;
                        float v59 = fq.clamp(((float)this.lastMouseX - v57) / v58, 0.0F, 1.0F);
                        v56.updateHsbBar(this.f1158, v59);
                        tq.colorTick(v59);
                     }
                  }

                  v36 += v40;
               }
            }

            dt.disable();
         }
      }
   }

   private float m1423(MultiSetting s, float width) {
      float[] v3 = this.f1184.get(s);
      if (v3 != null && v3[0] == width) {
         return v3[1];
      } else {
         List temp2 = this.m1431(s);

         return this.m1421(s, temp2, width);
      }
   }

   protected void method_25426() {
      this.exit = false;
      searchActive = false;
      this.f1163 = System.currentTimeMillis();
      this.f1154 = null;
      this.f1141 = false;
      tq.open();
      sq.start(0.15F);

      this.glomalAnim.run(1.0, 0.25, ap.SINE_OUT);
      selectedTheme = Client.get().guiManager().getCurrentTheme();
      preSelectedTheme = Client.get().guiManager().getCurrentTheme();
      themes = Theme.values();
   }

   private void m1397(int screenWidth, int screenHeight, float globalAnim) {
      if (!(globalAnim <= 0.01F)) {

         float v6 = (float)screenWidth;
         float temp2 = (float)screenHeight;

         this.f1170.draw(v6, temp2, globalAnim * 0.18F, up.getColor(255), 4.0F * S, 1.0F, 50.0F, 3000.0F, 100.0F);
      }
   }

   private int m1405(Category c) {

      try {
         for (Module result : Client.get().moduleManager().values()) {

            Module v4 = result;
            if (v4.getCategory() == c) {
               v2++;
            }
         }
      } catch (Exception v7) {
      }

      return v2;
   }

   private boolean m1400(Setting<?> s) {

      Object result = this.f1178.getOrDefault(s, false);

      return (Boolean)result;
   }

   private void m1434(float x, float y, float w, float globalAnim) {
      if (!(globalAnim <= 0.01F)) {
         float v5 = 50.0F * S;
         float v6 = v5 * 1.4354838F;
         float v7 = (float)(Math.abs(Math.sin((double)System.currentTimeMillis() * 0.0022)) * -2.5 * (double)S);
         float result = x + w;

         float v8 = result - v5 - 14.0F * S;
         float v9 = y - v6 + v7;
         vs.xs.texture(ANIME_GUI_TEX, v8, v9, v5, v6, up.replAlpha(up.getColor(255), globalAnim));
      }
   }

   private boolean m1393(String name) {
      ClickGui v2 = Client.get().moduleManager().get(ClickGui.class);
      boolean result;
      if (v2 != null && v2.effect.getValue(name)) {
         result = true;
         
      } else {
         result = false;
      }

      return result;
   }

   private List<String> m1431(MultiSetting s) {
      ArrayList v2 = new ArrayList();

      for (BooleanSetting v4 : s.getValues()) {

         v2.add(v4.getName());
         
      }

      return v2;
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private void m1437() {
      if (this.exit) {

         if (this.glomalAnim.isFinished()) {
            this.method_25419();
            sq.stop();
            this.exit = false;
         }
      }
   }

   private float m1399(String key, float target) {
      HashMap result = this.f1177;
      Function temp2 = k -> {
         float[] var10000x = new float[1];

         var10000x[0] = target;
         return var10000x;
      };

      float[] v3 = (float[])result.computeIfAbsent(key, temp2);
      v3[0] += (target - v3[0]) * 0.2F;
      return v3[0];
   }

   public boolean method_25400(CharInput input) {
      if (searchActive) {
         if (input.method_74227() && f1162.length() < 24) {
            String v9 = f1162;
            String v3 = input.method_74226();
            String v4 = v9;
            f1162 = v4 + v3;
            this.m1428();
            tq.type();
         }

         return true;
      } else if (this.f1155 == null) {
         return super.method_25400(input);
      } else {
         if (input.method_74227()) {
            String v2 = input.method_74226();
            if (this.f1155.isOnlyNumber()) {
               boolean result = v2.matches("[0-9.,-]+");

               if (!result) {
                  return true;
               }
            }

            String v6 = this.f1156;
            this.f1156 = v6 + v2;
            tq.type();
         }

         return true;
      }
   }

   public boolean method_25421() {

      return false;
   }

   public void method_25394(DrawContext context, int mouseX, 

      this.m1437();
      this.glomalAnim.update();
      mouseX = (int)((float)mouseX / this.f1140);
      mouseY = (int)((float)mouseY / this.f1140);
      this.lastMouseX = (double)mouseX;
      this.lastMouseY = (double)mouseY;
   }

   private boolean m1401(Module f) {

      if (f != null && f.getSettings() != null) {

         if (!f.getSettings().isEmpty()) {
            for (Setting v3 : f.getSettings()) {
               if (v3.getVisible().get()) {
                  return true;
               }
            }

            return false;
         }
      }

      return false;
   }

   private boolean m1439(float mainX, float mainY, float wMain, double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         float v9 = animThemeMenu.getOutput();
         float v10 = 16.0F * S;
         float v11 = mainY + 5.5F * S;
         float v12 = mainX + wMain - 14.0F * S;
         float v13 = 54.0F * S;
         float v14 = 200.0F * S;
         float v15 = v13 + (v14 - v13) * v9;
         float v16 = v12 - v15;
         if (!fq.isHovered((float)mouseX, (float)mouseY, v16, v11, v15, v10)) {
            return false;
         } else if (v9 < 0.5F) {
            themeMenuOpen = true;
            tq.category(0, 1);
            return true;
         } else {
            float v17 = v16 + 6.0F * S;
            float v18 = 3.0F * S;
            float v19 = 12.0F * S;

            for (int v20 = 0; v20 < themes.length; v20++) {
               Theme v21 = themes[v20];
               float v22 = v17 + (float)v20 * (v19 + v18);
               if (fq.isHovered((float)mouseX, (float)mouseY, v22 - 1.0F * S, v11 + 1.0F * S, v19 + 2.0F * S, v10 - 2.0F * S)) {
                  if (v21 != selectedTheme) {
                     int v23 = v21.getClient();
                     preSelectedTheme = selectedTheme;
                     selectedTheme = v21;
                     Client.get().guiManager().setGuiTheme(v21);
                     if (v21 == Theme.CUSTOM) {
                        xo.get().setActive(xo.get().getCustomTheme());
                        animation14.setDuration(300);
                        animation14.reset();
                     } else {
                        this.m1432((float)mouseX, (float)mouseY, v23);
                     }
                  } else {
                     Client.get().guiManager().setGuiTheme(v21);
                     if (v21 == Theme.CUSTOM) {
                        xo.get().setActive(xo.get().getCustomTheme());
                     }
                  }

                  tq.theme(v20, themes.length);
                  return true;
               }
            }

            float v26 = v17 + (float)themes.length * (v19 + v18) + 2.0F * S;
            float v27 = v26 + 4.0F * S;
            float v28 = 44.0F * S;
            if (fq.isHovered((float)mouseX, (float)mouseY, v27, v11 + 2.0F * S, v28, v10 - 4.0F * S)) {
               this.openThemeEditor();
               return true;
            } else {
               float v29 = v27 + v28 + 4.0F * S;
               float v24 = 12.0F * S;
               if (fq.isHovered((float)mouseX, (float)mouseY, v29, v11 + 2.0F * S, v24, v10 - 4.0F * S)) {
                  themeMenuOpen = false;

                  tq.category(0, 1);
                  return true;
               } else {
                  return true;
               }
            }
         }
      }
   }

   private float m1429(int idx, int total) {
      float v3 = animCategoryReset.getOutput();
      if (v3 >= 1.0F) {
         return 1.0F;
      } else {
         float v4 = 0.07F;
         int temp1 = Math.max(0, total - 1);

         float v5 = 1.0F + (float)temp1 * v4;
         return MathHelper.method_15363(v3 * v5 - (float)idx * v4, 0.0F, 1.0F);
      }
   }

   private void m1433(float x, float y, float w, float h, float globalAnim) {
      if (this.f1141) {
         long v6 = System.currentTimeMillis();
         float v8 = (float)(v6 - this.f1142) / 650.0F;
         if (v8 >= 1.0F) {
            this.f1141 = false;
         } else {
            if (v8 < 0.0F) {
               v8 = 0.0F;
            }

            if (!(globalAnim <= 0.01F)) {
               float v9 = 1.0F - (1.0F - v8) * (1.0F - v8) * (1.0F - v8);
               float v10 = (float)Math.hypot((double)(this.f1143 - x), (double)(this.f1144 - y));
               float v11 = (float)Math.hypot((double)(this.f1143 - (x + w)), (double)(this.f1144 - y));
               float v12 = (float)Math.hypot((double)(this.f1143 - x), (double)(this.f1144 - (y + h)));
               float v13 = (float)Math.hypot((double)(this.f1143 - (x + w)), (double)(this.f1144 - (y + h)));
               float v14 = Math.max(Math.max(v10, v11), Math.max(v12, v13)) + 24.0F * S;
               float v15 = Math.max(1.0F, v14 * v9);
               float result = 1.0F - v8;

               float v16 = result * globalAnim;
               if (!(v16 <= 0.004F)) {
                  int v17 = this.f1145;
                  dt.enable(x, y, w, h, 2.0F);
                  vr.rect(this.f1143 - v15, this.f1144 - v15, v15 * 2.0F, v15 * 2.0F, up.replAlpha(v17, 0.14F * v16), v15);
                  vr.glow(this.f1143 - v15, this.f1144 - v15, v15 * 2.0F, v15 * 2.0F, up.replAlpha(v17, 0.3F * v16), v15, 14.0F * S, 1.0F);
                  vr.outline(this.f1143 - v15, this.f1144 - v15, v15 * 2.0F, v15 * 2.0F, 1.5F * S, up.replAlpha(v17, 0.85F * v16), v15);
                  float v18 = v15 - 16.0F * S * v9 - 6.0F * S;
                  if (v18 > 2.0F) {
                     vr.outline(this.f1143 - v18, this.f1144 - v18, v18 * 2.0F, v18 * 2.0F, 1.0F * S, up.replAlpha(v17, 0.35F * v16), v18);
                  }

                  float v20 = (float)v6 * 0.008F;

                  for (int v21 = 0; v21 < v19; v21++) {
                     float v22 = (float)((double)((float)v21 / (float)v19) * Math.PI * 2.0);
                     float v23 = (float)Math.sin((double)(v22 * 3.0F + v20)) * 4.0F * S * (0.3F + 0.7F * (1.0F - v8));
                     float v24 = v15 + v23;
                     float v25 = this.f1143 + (float)Math.cos((double)v22) * v24;
                     float v26 = this.f1144 + (float)Math.sin((double)v22) * v24;
                     float v27 = 2.2F * S * (1.0F - v8 * 0.5F);
                     vr.rect(v25 - v27 / 2.0F, v26 - v27 / 2.0F, v27, v27, up.replAlpha(v17, 0.55F * v16), v27 / 2.0F);
                  }

                  dt.disable();
               }
            }
         }
      }
   }

   public boolean method_25406(Click click) {
      ro v2 = so.active();
      if (v2 != null) {
         boolean result = v2.mouseReleased(click.method_74245());

         return result;
      } else {
         if (this.f1152 != null || this.f1157 != null) {
            tq.sliderRelease();
         }

         this.f1152 = null;
         this.f1157 = null;
         return super.method_25406(click);
      }
   }

   private static float m1395(float edge0, float edge1, float value) {

      float v3 = MathHelper.method_15363((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
      return v3 * v3 * (3.0F - 2.0F * v3);
   }

   private float m1411(List<String> values, float innerW, yt draw) {
      float v4 = 0.0F;
      float v5 = 0.0F;

      for (String v7 : values) {
         float temp2 = 6.0F * S;

         float v8 = Math.max(draw.getWidth(v7, temp2) + 12.0F * S, 34.0F * S);

         if (v4 + v8 > innerW && v4 > 0.0F) {
            v4 = 0.0F;
            v5 += 15.0F * S;
         }

         v4 += v8 + 4.0F * S;
      }

      return v5 + 12.0F * S;
   }

   private float m1420(Iterable<String> values, float width) {
      yt v3 = hu.sf_regular;
      float v4 = 0.0F;
      float v5 = 0.0F;

      for (String v7 : values) {
         float v8 = Math.max(v3.getWidth(v7, 6.0F * S) + 10.0F * S, 26.0F * S);
         if (v4 + v8 > width && v4 > 0.0F) {
            v4 = 0.0F;
            float result = v5 + 12.0F * S;

            v5 = result;
         }

         v4 += v8 + 3.0F * S;
      }

      return v5 + 10.0F * S;
   }

   private float m1419(Module f, Setting<?> setting) {
      Animation v3 = this.f1183.computeIfAbsent(setting, k -> {
         ep result = new ep;

         result./* $VF: Unable to resugar constructor */<init>(300, 1.0);
         return result;
      });
      v3.setDirection(setting.getVisible().get() ? cp.FORWARDS : cp.BACKWARDS);

      return v3.getOutput();
   }

   public boolean method_25422() {
      ro v1 = so.active();
      if (v1 != null) {
         v1.saveAndExit();
         return false;
      } else {
         if (!this.exit) {
            this.glomalAnim.run(0.0, 0.3F, ap.SINE_IN);
            int result = v2 * (v2 + 1) % 2;

            if (result != 0) {
               }

            this.exit = true;
            tq.close();
         }

         return false;
      }
   }

   private void m1409(yt draw, String text, float tx, float ty, float size, float globalAnim) {
      int result = 255;

      int v7 = up.getColor(result, 0.92F * globalAnim);
      int v8 = up.replAlpha(up.client(), 0.95F * globalAnim);
      float v9 = (float)(System.currentTimeMillis() % (-4793078086392305072L + 4793078086392307272L)) / 2200.0F;
      float v10 = tx;

      for (int v11 = 0; v11 < text.length(); v11++) {

         String v12 = String.valueOf(text.charAt(v11));
         float v13 = v9 * (float)(text.length() + 3) - 1.5F;
         float v14 = Math.abs((float)v11 - v13);

         float v15 = Math.max(0.0F, 1.0F - v14 / 1.6F);
         v15 *= v15;
         draw.draw(v12, v10, ty, size, up.overCol(v7, v8, v15));
         v10 += draw.getWidth(v12, size);
      }
   }

   private boolean m1425() {
      boolean result;
      if (!this.m1424().isEmpty()) {
         result = true;
      } else {
         result = false;
         
      }

      return result;
   }

   private float m1421(Setting<?> key, Iterable<String> values, float width) {
      float[] v4 = this.f1184.get(key);

      if (v4 != null && v4[0] == width) {
         return v4[1];
      } else {
         float v5 = this.m1420(values, width);
         float[] temp2 = new float[]{width, 0.0F};

         temp2[1] = v5;
         this.f1184.put(key, temp2);
         return v5;
      }
   }

   private void m1435() {
      if (this.gif == null) {
         try {
            Optional v1 = mc.method_1478().method_14486(Identifier.method_60655("client", "textures/gui.gif"));
            if (v1.isPresent()) {
               InputStream v2 = ((Resource)v1.get()).method_14482();

               try {
                  this.gif = new zr(v2);
               } catch (Throwable v9) {
                  if (v2 != null) {
                     try {
                        v2.close();
                        
                     } catch (Throwable v8) {
                        v9.addSuppressed(v8);
                     }
                  }

                  throw v9;
               }

               if (v2 != null) {
                  v2.close();
               }
            }
         } catch (Exception v10) {
            v10.printStackTrace();
         }
      }
   }

   public void openThemeEditor() {
      this.m1436();

      int v4 = v1 * (v1 + 1);

      no.getInstance().open();
      tq.editor();
   }

   private void m1436() {
      this.exit = false;
      this.glomalAnim.run(1.0, 0.2F, ap.QUAD_OUT);
   }

   private float m1412(Module f, Setting<?> setting, float innerW, yt draw) {
      if (!setting.getVisible().get()) {
         return 0.0F;
      } else if (setting instanceof vf) {
         return 0.0F;
      } else if (setting instanceof SliderSetting) {
         return 30.0F * S;
      } else if (setting instanceof ModeSetting v11) {
         return 15.0F * S + this.m1411(v11.values, innerW, draw) + 5.0F * S;
      } else if (setting instanceof MultiSetting v10) {
         if (this.m1431(v10).size() > 3) {
            float v6 = this.m1400(v10) ? (float)(v10.getValues().size() * (15)) * S + 4.0F * S : 0.0F;
            float v12 = 12.0F * S;

            return v12 + 17.0F * S + 4.0F * S + v6;
         } else {
            return 15.0F * S + this.m1411(this.m1431(v10), innerW, draw) + 5.0F * S;
         }
      } else if (setting instanceof tf v5) {
         v5.pickerAnim.setDirection(v5.pickerOpen ? cp.FORWARDS : cp.BACKWARDS);
         float result = 18.0F * S;
         float temp1 = v5.pickerAnim.getOutput() * 40.0F;

         return result + temp1 * S;
      } else if (setting instanceof BooleanSetting) {
         return 19.0F * S;
      } else if (setting instanceof SettingVisibility) {
         return 19.0F * S;
      } else if (setting instanceof StringSetting) {
         return 20.0F * S;
      } else if (setting instanceof sf) {
         return 20.0F * S;
      } else {
         return setting instanceof uf ? 14.0F * S : 0.0F;
      }
   }

   public void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
      float v3 = 2.0F;
      float v4 = (float)mc.method_22683().method_4495();
      this.f1140 = v3 / v4;
      int v5 = (int)((float)mc.method_22683().method_4486() / this.f1140);
      int v6 = (int)((float)mc.method_22683().method_4502() / this.f1140);
      float v7 = this.glomalAnim.get();
      if (context != null) {
         context.method_51448().pushMatrix();
      }

      ro v8 = so.active();
      if (v8 != null) {
         float var112 = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.f1140);
         float var113 = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.f1140);
         v8.render((float)v5, (float)v6, var112, var113, v7);
         us.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }
      } else {
         if (this.m1393("Серый фон")) {
            this.f1168.draw(v7);
         }

         et.capture(1);
         if (this.m1393("Размывать фон")) {
            vs.ws.blur(0.0F, 0.0F, (float)v5, (float)v6, v7, 0.0F, up.getColor(0, 0));
         }

         if (this.m1393("Затемнять фон")) {
            vs.xs.texture(
               Identifier.method_60655("client", "textures/frame/background.png"),
               0.0F,
               0.0F,
               (float)v5,
               (float)v6,
               up.multAlpha(up.client(), v7)
            );
         }

         float v9 = (float)(mc.field_1729.method_68879(mc.method_22683()) / (double)this.f1140);
         float v10 = (float)(mc.field_1729.method_68883(mc.method_22683()) / (double)this.f1140);
         if (this.m1393("Шейдер")) {
            int v11 = up.client();
            this.f1171.draw(v7, up.replAlpha(up.multDark(v11, 0.5F), 0.9F * v7), up.replAlpha(v11, 0.9F * v7), 0.1F, 0.08F, 0.26F);
         }

         if (this.m1393("Точки")) {
            this.f1172.draw((float)v5, (float)v6, v9, v10, v7 * 0.1F, up.getColor(255), 6.0F, 0.7F, 3.0F, 100.0F);
         }

         if (this.m1393("Скан линии")) {
            this.m1397(v5, v6, v7);
         }

         if (this.m1393("Свечение")) {
            int var114 = up.replAlpha(up.client(), v7 * 0.5F);
            vs.xs.texture(
               Identifier.method_60655("client", "textures/effects/circles_effect.png"),
               0.0F,
               30.0F - 30.0F * v7,
               (float)v5,
               (float)v6,
               var114
            );
            vs.xs.texture(
               Identifier.method_60655("client", "textures/effects/top_glow.png"),
               0.0F,
               -30.0F + 30.0F * v7,
               (float)v5,
               (float)v6,
               var114
            );
            if (!this.exit && v7 > 0.01F && v7 < 0.99F) {
               float v12 = (v7 > 0.5F ? 1.0F - v7 : v7) * 2.0F;
               float v13 = (float)v5 / 1.75F * v7;
               float v14 = (float)v5 / 2.0F;
               float v15 = (float)v6 / 2.0F;
               vs.ys.rect(v14 - v13, v15 - v13, v13 * 2.0F, v13 * 2.0F, up.getColor(255, 0.2F * v12), v13);
               vs.ys.glow(v14 - v13, v15 - v13, v13 * 2.0F, v13 * 2.0F, up.replAlpha(up.client(), 0.15F * v12), v13, 12.0F, 1.0F);
            }
         }

         if (this.m1393("Частицы")) {
            this.m1394(v5, v6);
            this.m1396(v7);
         } else if (!this.f1173.isEmpty()) {
            this.f1173.clear();
         }

         et.capture();
         S = Client.get().moduleManager().get(ClickGui.class).size.getValue();
         if (this.f1157 != null && this.f1157.pickerOpen && this.f1160 > 1.0F) {
            float var115 = fq.clamp(((float)this.lastMouseX - this.f1159) / this.f1160, 0.0F, 1.0F);
            this.f1157.updateHsbBar(this.f1158, var115);
            tq.colorTick(var115);
         }

         float var116 = 132.0F * S;
         float var117 = 392.0F * S;
         float var118 = 8.0F * S;
         float var119 = var116 + var118 + var117;
         float var120 = 296.0F * S;
         float v16 = (float)v5 / 2.0F - var119 / 2.0F;
         float v17 = (float)v6 / 2.0F - var120 / 2.0F + (this.exit ? 60.0F * S - 60.0F * S * v7 : -60.0F * S + 60.0F * S * v7);
         float v20 = v16 + var116 + var118;
         yt v22 = hu.sf_regular;
         yt v23 = hu.sf_regular;
         yt v24 = hu.icon;
         yt v25 = hu.gui;
         et.capture();
         vs.ys.glow(v16, v17, var116, var120, up.getColor(0, 0.15F * v7), 10.0F * S, 15.0F, 1.0F);
         vs.ws.blur(v16, v17, var116, var120, v7, 10.0F * S, up.multAlpha(up.multDark(up.background(), 0.55F), v7));
         vs.ys.rect(v16, v17, var116, var120, up.getColor(0, 0.1F * v7), 10.0F * S);
         vs.xs.texture(
            Identifier.method_60655("client", "textures/frame/rectgui.png"),
            v16,
            v17,
            var116,
            var120,
            up.multAlpha(up.client(), v7 * 0.5F)
         );
         vs.ys.glow(v20, v17, var117, var120, up.getColor(0, 0.15F * v7), 10.0F * S, 15.0F, 1.0F);
         vs.ws.blur(v20, v17, var117, var120, v7, 10.0F * S, up.multAlpha(up.multDark(up.background(), 0.55F), v7));
         vs.ys.rect(v20, v17, var117, var120, up.getColor(0, 0.1F * v7), 10.0F * S);
         vs.xs.texture(
            Identifier.method_60655("client", "textures/frame/rectgui.png"),
            v20,
            v17,
            var117,
            var120,
            up.multAlpha(up.client(), v7 * 0.5F)
         );
         if (this.m1393("Свечение")) {
            ss.render(v16, v17, var116, var120, 10.0F * S, S, v7);
            ss.render(v20, v17, var117, var120, 10.0F * S, S, v7);
         }

         float v26 = 30.0F * S;
         float v27 = v17 + var120 - 6.0F * S - v26;
         float v28 = v16 + 6.0F * S;
         float v29 = var116 - 12.0F * S;
         float v30 = 8.0F * S;
         float v31 = v17 + v30;
         float v32 = 20.0F * S;
         animThemeMenu.setDirection(themeMenuOpen ? cp.FORWARDS : cp.BACKWARDS);
         float v33 = animThemeMenu.getOutput();
         float v34 = 68.0F * S;
         float v35 = v16 + v30;
         boolean v36 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v35 - 2.0F * S, v31 - 2.0F * S, v34 + 4.0F * S, v32 + 4.0F * S);
         int v37 = !v36 && !(v33 > 0.05F)
            ? up.getColor(255, (int)(22.0F * v7))
            : up.replAlpha(up.client(), (0.12F + 0.15F * v33) * v7);
         vs.ys.rect(v35, v31, v34, v32, v37, 6.0F * S);
         if (v33 > 0.01F || v36) {
            vs.ys.outline(v35, v31, v34, v32, 0.5F * S, up.replAlpha(up.client(), (v36 ? 0.45F : 0.25F * v33) * v7), 6.0F * S);
         }

         vs.xs.texture(ROSE_LOGO, v35 + 4.0F * S, v31 + 2.5F * S, 15.0F * S, 15.0F * S, up.replAlpha(up.client(), v7));
         this.m1409(v22, "Rose", v35 + 23.0F * S, v31 + 5.5F * S, 7.5F * S, v7);
         v22.draw(
            v33 > 0.5F ? "^" : "v",
            v35 + v34 - 9.0F * S,
            v31 + 7.0F * S,
            5.0F * S,
            up.getColor(255, (0.35F + 0.45F * v33) * v7)
         );
         String v38 = "1.21.11";
         float v39 = v22.getWidth(v38, 6.5F * S) + 14.0F * S;
         float v40 = v16 + var116 - v30 - v39;
         vs.ys.rect(v40, v31, v39, v32, up.getColor(255, (int)(18.0F * v7)), 6.0F * S);
         v22.draw(v38, v40 + 7.0F * S, v31 + 6.0F * S, 6.5F * S, up.getColor(255, 0.65F * v7));
         float v41 = v31 + v32 + 10.0F * S;
         v41 = this.m1410(
            "COMBAT",
            new Category[]{Category.COMBAT, Category.MOVEMENT, Category.PLAYER},
            new String[]{"Combat", "Movement", "Player"},
            v16 + v30,
            v41,
            var116 - v30 * 2.0F,
            v7,
            v22
         );
         v41 = this.m1410("VISUAL", new Category[]{Category.RENDER}, new String[]{"Render"}, v16 + v30, v41, var116 - v30 * 2.0F, v7, v22);
         v41 = this.m1410("OTHER", new Category[]{Category.OTHER}, new String[]{"Misc"}, v16 + v30, v41, var116 - v30 * 2.0F, v7, v22);
         float v42 = var116 - v30 * 2.0F;
         float v43 = 30.0F * S;
         float v44 = v17 + var120 - v30 - v43;
         float v45 = 20.0F * S;
         float v46 = v44 - 6.0F * S - v45;
         float v47 = v16 + v30;
         boolean v49 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, v47, v46, v42, v45);
         animation3.setDirection(!v49 && !searchActive ? cp.BACKWARDS : cp.FORWARDS);
         this.f1164.setDirection(searchActive ? cp.FORWARDS : cp.BACKWARDS);
         this.f1165.setDirection(!f1162.isEmpty() ? cp.FORWARDS : cp.BACKWARDS);
         float v50 = animation3.getOutput();
         float v51 = this.f1164.getOutput();
         float v52 = this.f1165.getOutput();
         vs.ys.rect(v47, v46, v42, v45, up.getColor(255, (int)(14.0F * v7)), 6.0F * S);
         vs.ys.outline(v47, v46, v42, v45, 0.5F * S, up.replAlpha(up.client(), v7 * v51), 6.0F * S);
         v25.draw("A", v47 + 7.0F * S, v46 + 7.0F * S, 6.0F * S, up.getColor(255, v7 * (0.3F + 0.5F * v50)));
         float v53 = v47 + 6.5F * S + 11.0F * S;
         float v54 = v42 - (v53 - v47) - 22.0F * S;
         float v55 = Math.max(v52, v51);
         if (v55 < 0.99F) {
            v23.draw(
               "Search",
               v53,
               v46 + 5.5F * S + 4.0F * S * v55,
               7.0F * S,
               up.getColor(255, v7 * (1.0F - v55) * (0.3F + 0.5F * v50))
            );
         }

         if (v52 > 0.01F) {
            v23.drawFadingTextReverse(
               f1162, v53, v46 + 5.5F * S - 4.0F * S * (1.0F - v52), v54, up.getColor(255, v7 * v52), 7.0F * S
            );
         }

         boolean v56 = System.currentTimeMillis() - this.f1163 < 976300972134259189L + -976300972134258689L;
         float v57 = v51
            * (
               !v56 && System.currentTimeMillis() / (-947855517753011110L + 947855517753011610L) % (335082742638003225L + -335082742638003223L) != 0L
                  ? 0.0F
                  : 1.0F
            );
         float v58 = this.m1399("search:caret", v57);
         float v59 = this.m1399(
            "search:caretx", v53 + Math.min(v23.getWidth(f1162, 7.0F * S), v54) + 1.5F * S
         );
         vs.ys.rect(v59, v46 + 6.5F * S, 0.6F * S, 6.5F * S, up.replAlpha(up.client(), v7 * v58), 0.3F * S);
         vs.ys.rect(v47 + v42 - 12.0F * S, v46 + v45 / 2.0F - 3.0F * S, 6.0F * S, 6.0F * S, up.replAlpha(up.client(), 0.85F * v7), 6.0F * S);
         String v60 = System.getProperty("user.name");
         if (v60 == null || v60.isEmpty()) {
            try {
               v60 = mc.method_1548().method_1676();
            } catch (Exception var111) {
               v60 = "Player";
            }
         }

         if (v60 == null || v60.isEmpty()) {
            v60 = "Player";
         }

         float v61 = 18.0F * S;
         float v62 = v16 + v30 + 4.0F * S;
         float v63 = v44 + v43 / 2.0F - v61 / 2.0F;
         vr.glow(v62 - 1.0F * S, v63 - 1.0F * S, v61 + 2.0F * S, v61 + 2.0F * S, up.replAlpha(up.client(), 0.18F * v7), v61 / 2.0F, 8.0F, 0.75F);
         vs.xs.texture(AVATAR_TEX, v62, v63, v61, v61, 1.0F, v61 / 2.0F, up.replAlpha(up.getColor(255), v7));
         vr.outline(v62, v63, v61, v61, 1.0F * S, up.replAlpha(up.client(), 0.75F * v7), v61 / 2.0F);
         float v64 = v62 + v61 + 6.0F * S;
         float v65 = v42 - v61 - 44.0F * S;
         float v66 = v22.getWidth(v60, 7.0F * S);
         String v67 = v60;
         if (v66 > v65) {
            while (v67.length() > 2 && v22.getWidth(v67 + "...", 7.0F * S) > v65) {
               v67 = v67.substring(0, v67.length() - 1);
            }

            v67 = v67 + "...";
         }

         float v68 = v44 + v43 / 2.0F - 3.5F * S;
         v22.draw(v67, v64, v68, 7.0F * S, up.getColor(255, 0.88F * v7));
         String v69 = "BETA";
         float v70 = v22.getWidth(v69, 5.0F * S) + 8.0F * S;
         float v71 = v64 + Math.min(v22.getWidth(v67, 7.0F * S), v65) + 4.0F * S;
         vs.ys.rect(v71, v68 + 0.5F * S, v70, 9.0F * S, up.replAlpha(up.client(), 0.35F * v7), 3.0F * S);
         v22.draw(v69, v71 + 4.0F * S, v68 + 2.0F * S, 5.0F * S, up.getColor(255, 0.9F * v7));
         float v72 = v17 + 10.0F * S;
         float v73 = v20 + 14.0F * S;
         float v74 = this.m1407(v22, "Rose", v73, v72, 7.0F * S, up.getColor(255, 0.45F * v7));
         v74 = this.m1408(v22, v74, v72, 7.0F * S, v7);
         v74 = this.m1407(v22, "Categories", v74, v72, 7.0F * S, up.getColor(255, 0.45F * v7));
         v74 = this.m1408(v22, v74, v72, 7.0F * S, v7);
         String v75 = this.m1425() ? "Search" : this.m1406(this.active);
         if (!this.m1425()) {
            try {
               String v76 = this.active.getIcon();
               hu.nightix_2.draw(v76, v74, v72 + 1.0F * S, 7.0F * S, up.replAlpha(up.client(), v7));
               v74 += hu.nightix_2.getWidth(v76, 7.0F * S) + 5.0F * S;
            } catch (Exception var110) {
            }
         }

         v22.draw(v75, v74, v72, 7.0F * S, up.getColor(255, 0.9F * v7));
         if (this.m1425()) {
            int var127 = this.m1427();
            String var107 = var127 == 1 ? " result" : " results";
            String v77 = var127 + var107;
            v22.draw(v77, v20 + var117 - 14.0F * S - v22.getWidth(v77, 6.5F * S), v72, 6.5F * S, up.replAlpha(up.client(), v7));
         } else {
            this.m1438(v20, v17, var117, v7, this.lastMouseX, this.lastMouseY, v22, v23);
         }

         float var128 = v17 + 26.0F * S;
         vs.ys.rect(v20 + 14.0F * S, var128, var117 - 28.0F * S, 0.5F * S, up.getColor(255, (int)(16.0F * v7)), 0.25F * S);
         float var129 = v20 + 12.0F * S;
         float v78 = var128 + 8.0F * S;
         float v79 = var117 - 24.0F * S;
         float v80 = v17 + var120 - 10.0F * S - v78;
         float v81 = 10.0F * S;
         float v82 = (v79 - v81) / 2.0F;
         float v83 = var129 + v82 + v81;
         this.f1147 = this.f1147 + (this.f1146 - this.f1147) * 0.2F;
         dt.enable(var129 - 2.0F * S, v78 - 2.0F * S, v79 + 4.0F * S, v80 + 4.0F * S, 2.0F);

         this.f1147 = this.f1147 + (this.f1146 - this.f1147) * 0.2F;
         ArrayList v84 = new ArrayList();

         for (Module v86 : Client.get().moduleManager().values()) {
            if (this.m1426(v86)) {
               v84.add(v86);
            }
         }

         int var130 = (v84.size() + 1) / 2;
         List var131 = v84.subList(0, Math.min(var130, v84.size()));
         List v87 = v84.size() > var130 ? v84.subList(var130, v84.size()) : Collections.emptyList();
         float v88 = v78;
         float v89 = v78 + v80;
         float v90 = 8.0F * S;
         this.f1179 = null;
         float v91 = 0.0F;

         for (Module v93 : var131) {
            v91 += this.m1414(v93, v82, v22) + v90;
         }

         float var132 = 0.0F;

         for (Module v94 : v87) {
            var132 += this.m1414(v94, v82, v22) + v90;
         }

         this.f1148 = Math.max(0.0F, Math.max(v91, var132) - v80);
         if (this.f1146 > this.f1148) {
            this.f1146 = this.f1148;
         }

         if (this.f1146 < 0.0F) {
            this.f1146 = 0.0F;
         }

         float var134 = v78 - this.f1147;
         int var135 = 0;
         int v95 = v84.size();

         for (Module v97 : var131) {
            float v98 = this.m1414(v97, v82, v22);
            float v99 = this.m1429(var135, v95);
            if (v99 > 0.02F && var134 + v98 >= v88 - 20.0F * S && var134 <= v89 + 20.0F * S) {
               this.m1416(v97, var129, var134 + (1.0F - v99) * 12.0F * S, v82, v7 * v99, v22, v23, v88, v89);
            }

            var134 += v98 + v90;
            var135++;
         }

         float var136 = v78 - this.f1147;

         for (Module var139 : v87) {
            float var142 = this.m1414(var139, v82, v22);
            float var100 = this.m1429(var135, v95);
            if (var100 > 0.02F && var136 + var142 >= v88 - 20.0F * S && var136 <= v89 + 20.0F * S) {
               this.m1416(var139, v83, var136 + (1.0F - var100) * 12.0F * S, v82, v7 * var100, v22, v23, v88, v89);
            }

            var136 += var142 + v90;
            var135++;
         }

         this.f1166.setDirection(this.m1425() && v84.isEmpty() ? cp.FORWARDS : cp.BACKWARDS);
         float var138 = this.f1166.getOutput();
         if (var138 > 0.01F) {
            v22.drawCentered(
               "Ничего не найдено",
               var129 + v79 / 2.0F,
               v78 + v80 / 2.0F,
               7.0F * S,
               up.getColor(255, v7 * var138 * 0.5F)
            );
         }

         dt.disable();
         if (this.f1179 != null && !this.f1179.isEmpty() && v7 > 0.05F) {
            float var140 = 6.0F * S;
            float var143 = 190.0F * S;
            float var144 = Math.min(v22.getWidth(this.f1179, var140), var143 - 16.0F * S);
            float var101 = var144 + 16.0F * S;
            float var102 = 18.0F * S;
            float var103 = (float)this.lastMouseX + 12.0F * S;
            float var104 = (float)this.lastMouseY + 14.0F * S;
            if (var103 + var101 > (float)v5 - 4.0F * S) {
               var103 = (float)this.lastMouseX - var101 - 10.0F * S;
            }

            if (var104 + var102 > (float)v6 - 4.0F * S) {
               var104 = (float)this.lastMouseY - var102 - 8.0F * S;
            }

            vs.ys.glow(var103, var104, var101, var102, up.getColor(0, 0.25F * v7), 5.0F * S, 8.0F, 1.0F);
            vs.ws.blur(var103, var104, var101, var102, v7, 5.0F * S, up.multAlpha(up.multDark(up.background(), 0.7F), v7));
            vs.ys.rect(var103, var104, var101, var102, up.getColor(0, 0.35F * v7), 5.0F * S);
            vs.ys.outline(var103, var104, var101, var102, 0.5F * S, up.replAlpha(up.client(), 0.45F * v7), 5.0F * S);
            v22.drawFadingText(
               this.f1179, var103 + 8.0F * S, var104 + 5.5F * S, var101 - 14.0F * S, up.getColor(255, 0.85F * v7), var140
            );
         }

         this.m1434(v20, v17, var117, v7);
         this.m1433(v16, v17, var119, var120, v7);
         us.endOverlay();
         if (context != null) {
            context.method_51448().popMatrix();
         }

         int var141 = v84.size();
      }
   }

   public boolean method_25404(KeyInput input) {
      int v2 = input.comp_4795();
      ro v3 = so.active();
      if (v3 != null) {
         if (v2 == 256) {
            v3.saveAndExit();
         }

         return true;
      } else if (this.f1154 != null) {
         boolean v7 = v2 == 256 || v2 == 261;
         this.f1154.setKey(v7 ? -1 : v2);
         this.f1154 = null;
         if (v7) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (this.f1153 != null) {
         boolean v4 = v2 == 256 || v2 == 261;
         int temp1;
         if (v4) {
            temp1 = -1;
         } else {
            temp1 = v2;
            
         }

         this.f1153.set(temp1);
         this.f1153 = null;
         if (v4) {
            tq.bindReset();
         } else {
            tq.bindSet();
         }

         return true;
      } else if (searchActive) {
         if (v2 == 256) {
            if (!f1162.isEmpty()) {
               this.m1430();
            } else {
               searchActive = false;
               
            }

            tq.searchClear();
         } else if (v2 == 257 || v2 == 335) {
            searchActive = false;
            tq.editCommit();
         } else if (v2 == 259 && !f1162.isEmpty()) {
            f1162 = f1162.substring(0, f1162.length() - 1);
            this.m1428();
            tq.erase();
         }

         return true;
      } else if (this.f1155 == null) {
         return super.method_25404(input);
      } else {
         if (v2 == 257 || v2 == 335) {
            this.f1155.set(this.f1156);
            this.f1155 = null;
            tq.editCommit();
         } else if (v2 == 256) {
            this.f1155 = null;
            tq.editCancel();
         } else if (v2 == 259 && !this.f1156.isEmpty()) {
            this.f1156 = this.f1156.substring(0, this.f1156.length() - 1);
            tq.erase();
         }

         return true;
      }
   }

   private float m1414(Module f, float colW, yt draw) {
      float v4 = colW - 16.0F * S;
      float v5 = 22.0F * S;
      float v6 = this.m1404(f);
      if (v6 > 0.001F) {

         v5 += this.m1413(f, v4, draw) * v6;
      }

      return v5;
   }

   private boolean m1426(Module f) {
      String v2 = this.m1424();
      return v2.isEmpty()
         ? f.getCategory() == this.active
         : f.getName().toLowerCase().contains(v2)
            || f.getBigName().toLowerCase().contains(v2)
            || f.getDesc().toLowerCase().contains(v2)
            || f.getCategory().getName().toLowerCase().contains(v2);
   }

   private float m1408(yt draw, float cx, float cy, float size, float globalAnim) {
      int temp5 = 255;

      draw.draw("/", cx, cy, size, up.getColor(temp5, 0.3F * globalAnim));
      return cx + draw.getWidth("/", size) + 5.0F * S;
   }

   private Animation m1392(String key) {

      Object result = this.f1167.computeIfAbsent(key, k -> {
         ep var10000x = new ep;
         int temp2 = 300;

         var10000x./* $VF: Unable to resugar constructor */<init>(temp2, 1.0);
         return var10000x;
      });

      return (Animation)result;
   }

   private float m1404(Module f) {
      boolean result = this.m1402(f);

      boolean v2 = result;
      Animation v3 = this.f1182.get(f);
      if (v3 == null) {
         ep v6 = new ep(240, 1.0, v2 ? cp.FORWARDS : cp.BACKWARDS);
         if (v2) {
            v6.timerUtil.setTime(0L);
         }

         this.f1182.put(f, v6);
         return v2 ? 1.0F : 0.0F;
      } else {
         v3.setDirection(v2 ? cp.FORWARDS : cp.BACKWARDS);
         return v3.getOutput();
      }
   }

   public void openCrosshairEditor() {
      this.m1436();
      eo.getInstance().open();
      tq.editor();
      
   }

   private void m1415(float tx, float ty, float tw, float th, float on, float hov, float globalAnim) {
      vs.ys.rect(
         tx, ty, tw, th, up.overCol(up.getColor(255, 0.1F * globalAnim), up.replAlpha(up.client(), 0.75F * globalAnim), on), th / 2.0F
      );
      float v8 = th - 3.0F * S;
      float v9 = tx + 1.5F * S + (tw - v8 - 3.0F * S) * on;
      float temp1 = ty + 1.5F * S;
      int temp4 = 255;
      float temp5 = globalAnim * (0.55F + 0.45F * Math.max(on, hov));

      vs.ys.rect(v9, temp1, v8, v8, up.getColor(temp4, temp5), v8 / 2.0F);
   }

   private void m1396(float globalAnim) {
      for (int v2 = this.f1173.size() - 1; v2 >= 0; v2--) {
         ko.lo v3 = this.f1173.get(v2);
         float v4 = v3.progress();
         if (v4 >= 1.0F) {
            this.f1173.remove(v2);
         } else {
            float v5 = 1.0F - (1.0F - v4) * (1.0F - v4) * (1.0F - v4);

            float v6 = v3.maxRadius * v5 * S;
            if (!(v6 < 0.4F)) {
               float v7 = m1395(0.0F, 0.18F, v4) * (1.0F - m1395(0.45F, 1.0F, v4));
               float v8 = v7 * globalAnim * 0.75F;
               if (!(v8 < 0.004F)) {
                  float v9 = 1.1F + 2.6F * (1.0F - v5);
                  float v10 = v3.y - v3.drift * v5 * S;
                  vs.ys.outline(v3.x - v6, v10 - v6, v6 * 2.0F, v6 * 2.0F, v9, up.replAlpha(up.client(), v8), v6);
               }
            }
         }
      }
   }

   private void m1417(Module f, Setting<?> setting, float sx, float sy, float sw, float globalAnim, yt draw, yt regular) {
      if (setting instanceof SliderSetting v70) {
         float v77 = MathHelper.method_15363((v70.getValue() - v70.min) / (v70.max - v70.min), 0.0F, 1.0F);
         v70.getAnimation().update();
         v70.getAnimation().run((double)v77, 0.06F, ap.LINEAR);
         boolean v85 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, sx, sy, sw, 26.0F * S);
         v70.animation.setDirection(v85 ? cp.FORWARDS : cp.BACKWARDS);
         float v92 = v70.animation.getOutput();
         String var100 = String.valueOf((double)Math.round((double)v70.getValue().floatValue() * 100.0) / 100.0);
         float var108 = draw.getWidth(var100, 6.0F * S);
         draw.drawFadingText(v70.getName(), sx, sy, sw - var108 - 8.0F * S, up.getColor(255, 0.6F * globalAnim), 6.5F * S);
         draw.draw(var100, sx + sw - var108, sy, 6.0F * S, up.getColor(255, (0.55F + 0.35F * v92) * globalAnim));
         float var114 = sy + 14.0F * S;
         float var119 = sw - 4.0F * S;
         vs.ys.rect(sx + 2.0F * S, var114, var119, 2.0F * S, up.getColor(255, (int)(22.0F * globalAnim)), 1.0F * S);
         float var124 = var119 * v70.getAnimation().get();
         if (var124 > 0.5F) {
            float var129 = this.f1152 == v70 ? 1.4F : 1.0F;
            vr.glow(sx + 2.0F * S, var114, var124, 2.0F * S, up.replAlpha(up.client(), 0.45F * var129 * globalAnim), 1.0F * S, 3.0F, 0.8F);
            vs.ys.rect(sx + 2.0F * S, var114, var124, 2.0F * S, up.replAlpha(up.client(), 0.85F * globalAnim), 1.0F * S);
         }

         float var130 = sx + 2.0F * S + var124;
         float var135 = var114 + 1.0F * S;
         vr.glow(var130 - 3.0F * S, var135 - 3.0F * S, 6.0F * S, 6.0F * S, up.replAlpha(up.client(), 0.45F * globalAnim), 3.0F * S, 3.0F, 0.8F);
         vs.ys.rect(var130 - 2.0F * S, var135 - 2.0F * S, 4.0F * S, 4.0F * S, up.getColor(255, globalAnim), 4.0F * S);
      } else if (setting instanceof ModeSetting v69) {
         String v76 = v69.getValue();
         float v84 = draw.getWidth(v76, 6.0F * S);
         draw.drawFadingText(v69.getName(), sx, sy, sw - v84 - 8.0F * S, up.getColor(255, 0.5F * globalAnim), 6.0F * S);
         draw.draw(v76, sx + sw - v84, sy, 6.0F * S, up.replAlpha(up.client(), 0.95F * globalAnim));
         float v91 = 0.0F;
         float v99 = 13.0F * S;

         for (String var113 : v69.values) {
            float var118 = Math.max(draw.getWidth(var113, 6.0F * S) + 12.0F * S, 34.0F * S);
            if (v91 + var118 > sw && v91 > 0.0F) {
               v91 = 0.0F;
               v99 += 15.0F * S;
            }

            boolean var123 = var113.equals(v76);
            String var165 = f.getName();
            String v37 = v69.getName();
            String v38 = var165;
            Animation var128 = this.m1392(v38 + ":" + v37 + ":" + var113);
            var128.setDirection(var123 ? cp.FORWARDS : cp.BACKWARDS);
            float var134 = var128.getOutput();
            if (var134 > 0.02F) {
               vr.glow(sx + v91, sy + v99, var118, 12.0F * S, up.replAlpha(up.client(), 0.3F * globalAnim * var134), 6.0F * S, 5.0F, 0.8F);
            }

            vs.ys.rect(
               sx + v91,
               sy + v99,
               var118,
               12.0F * S,
               up.overCol(up.getColor(255, (int)(14.0F * globalAnim)), up.replAlpha(up.client(), 0.85F * globalAnim), var134),
               6.0F * S
            );
            regular.drawCentered(
               var113, sx + v91 + var118 / 2.0F, sy + v99 + 2.5F * S, 6.0F * S, up.getColor(255, globalAnim * (0.45F + 0.5F * var134))
            );
            v91 += var118 + 4.0F * S;
         }
      } else if (setting instanceof MultiSetting v68) {
         List v75 = this.m1431(v68);
         if (v75.size() <= 3) {
            int v83 = v68.getValues().size();

            for (BooleanSetting var105 : v68.getValues()) {
               if (var105.getValue()) {
                  v90++;
               }
            }

            String v98 = v90 + " / " + v83;
            float var106 = draw.getWidth(v98, 6.0F * S);
            draw.drawFadingText(v68.getName(), sx, sy, sw - var106 - 8.0F * S, up.getColor(255, 0.5F * globalAnim), 6.0F * S);
            draw.draw(v98, sx + sw - var106, sy, 6.0F * S, up.replAlpha(up.client(), 0.9F * globalAnim));
            float var112 = 0.0F;
            float var117 = 13.0F * S;

            for (BooleanSetting var127 : v68.getValues()) {
               float var133 = Math.max(draw.getWidth(var127.getName(), 6.0F * S) + 12.0F * S, 34.0F * S);
               if (var112 + var133 > sw && var112 > 0.0F) {
                  var112 = 0.0F;
                  var117 += 15.0F * S;
               }

               String var164 = f.getName();
               String var169 = v68.getName();
               String v49 = var127.getName();
               String v50 = var169;
               String v51 = var164;
               Animation var138 = this.m1392(v51 + ":" + v50 + ":" + v49);
               var138.setDirection(var127.getValue() ? cp.FORWARDS : cp.BACKWARDS);
               float var141 = var138.getOutput();
               if (var141 > 0.02F) {
                  vr.glow(sx + var112, sy + var117, var133, 12.0F * S, up.replAlpha(up.client(), 0.3F * globalAnim * var141), 6.0F * S, 5.0F, 0.8F);
               }

               vs.ys.rect(
                  sx + var112,
                  sy + var117,
                  var133,
                  12.0F * S,
                  up.overCol(up.getColor(255, (int)(14.0F * globalAnim)), up.replAlpha(up.client(), 0.85F * globalAnim), var141),
                  6.0F * S
               );
               regular.drawCentered(
                  var127.getName(),
                  sx + var112 + var133 / 2.0F,
                  sy + var117 + 2.5F * S,
                  6.0F * S,
                  up.getColor(255, globalAnim * (0.45F + 0.5F * var141))
               );
               var112 += var133 + 4.0F * S;
            }
         } else {
            draw.draw(v68.getName(), sx, sy, 6.0F * S, up.getColor(255, 0.6F * globalAnim));
            float v82 = sy + 11.0F * S;
            boolean v89 = this.m1400(v68);
            int v96 = up.overCol(up.getColor(255, (int)(30.0F * globalAnim)), up.client(), v89 ? 0.65F : 0.0F);
            vs.ys.rect(sx, v82, sw, 15.0F * S, up.getColor(255, (int)(14.0F * globalAnim)), 4.0F * S);
            vs.ys.outline(sx, v82, sw, 15.0F * S, 0.5F * S, v96, 4.0F * S);
            String var104 = v68.getNames();
            if (var104.isEmpty()) {
               var104 = "Ничего";
            }

            float var111 = sw - 20.0F * S;

            while (var104.length() > 4 && draw.getWidth(var104 + "...", 6.0F * S) > var111) {
               var104 = var104.substring(0, var104.length() - 1);
            }

            if (draw.getWidth(v68.getNames().isEmpty() ? "Ничего" : v68.getNames(), 6.0F * S) > var111) {
               var104 = var104 + "...";
            }

            regular.drawFadingText(var104, sx + 6.0F * S, v82 + 4.0F * S, var111, up.getColor(255, 0.75F * globalAnim), 6.0F * S);
            draw.draw(
               v89 ? "^" : "v", sx + sw - 10.0F * S, v82 + 4.0F * S, 6.0F * S, up.getColor(255, (v89 ? 0.85F : 0.45F) * globalAnim)
            );
            if (v89) {
               float var116 = v82 + 17.0F * S;
               float var121 = (float)(v68.getValues().size() * (15)) * S + 4.0F * S;
               int var126 = up.overCol(
                  up.getColor(16, 17, 24, (int)(245.0F * globalAnim)), up.background(), 0.45F
               );
               int var132 = up.overCol(
                  up.getColor(42, 45, 58, (int)(180.0F * globalAnim)), up.client(), 0.3F
               );
               vr.glow(sx, var116, sw, var121, up.getColor(0, 0.2F * globalAnim), 4.0F * S, 6.0F * S, 0.7F);
               vs.ys.rect(sx, var116, sw, var121, var126, 4.0F * S);
               vs.ys.outline(sx, var116, sw, var121, 0.5F * S, var132, 4.0F * S);
               float var137 = var116 + 2.0F * S;
               float var140 = 15.0F * S;

               for (BooleanSetting var144 : v68.getValues()) {
                  String var162 = f.getName();
                  String var167 = v68.getName();
                  String v41 = var144.getName();
                  String v42 = var167;
                  String v43 = var162;
                  Animation var145 = this.m1392(v43 + ":" + v42 + ":" + v41);
                  var145.setDirection(var144.getValue() ? cp.FORWARDS : cp.BACKWARDS);
                  float var146 = var145.getOutput();
                  boolean var147 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, sx + 2.0F * S, var137, sw - 4.0F * S, var140);
                  var162 = f.getName();
                  var167 = v68.getName();
                  String v44 = var144.getName();
                  String v45 = var167;
                  String v46 = var162;
                  Animation var148 = this.m1392(v46 + ":" + v45 + ":" + v44 + ":hov");
                  var148.setDirection(var147 ? cp.FORWARDS : cp.BACKWARDS);
                  float v28 = var148.getOutput();
                  if (var146 > 0.01F || v28 > 0.01F) {
                     int v29 = up.overCol(
                        up.getColor(255, (int)(14.0F * globalAnim * v28)),
                        up.replAlpha(up.client(), (0.12F + 0.08F * v28) * globalAnim),
                        var146
                     );
                     vs.ys.rect(sx + 3.0F * S, var137 + 0.5F * S, sw - 6.0F * S, var140 - 1.0F * S, v29, 3.0F * S);
                  }

                  float var149 = 6.5F * S;
                  float v30 = sx + 8.0F * S;
                  float v31 = var137 + (var140 - var149) / 2.0F;
                  int v32 = up.overCol(
                     up.getColor(255, (int)((80.0F + 60.0F * v28) * globalAnim)), up.replAlpha(up.client(), globalAnim), var146
                  );
                  vs.ys.outline(v30, v31, var149, var149, 0.6F * S, v32, var149 / 2.0F);
                  if (var146 > 0.02F) {
                     vr.glow(v30, v31, var149, var149, up.replAlpha(up.client(), 0.5F * globalAnim * var146), var149 / 2.0F, 3.0F * S, 0.8F);
                     float v33 = 3.0F * S;
                     float v34 = v30 + (var149 - v33) / 2.0F;
                     float v35 = v31 + (var149 - v33) / 2.0F;
                     vs.ys.rect(v34, v35, v33, v33, up.replAlpha(up.client(), globalAnim * var146), v33 / 2.0F);
                  }

                  float var150 = v30 + var149 + 6.0F * S;
                  float var151 = var137 + (var140 - 6.2F * S) / 2.0F + 0.5F * S;
                  int var152 = up.overCol(
                     up.getColor(255, (int)((130.0F + 50.0F * v28) * globalAnim)),
                     up.getColor(255, (int)(245.0F * globalAnim)),
                     var146
                  );
                  regular.draw(var144.getName(), var150, var151, 6.2F * S, var152);
                  var137 += var140;
               }
            }
         }
      } else if (setting instanceof BooleanSetting v67) {
         v67.animation.setDirection(fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, sx, sy, sw, 16.0F * S) ? cp.FORWARDS : cp.BACKWARDS);
         float v74 = v67.animation.getOutput();
         v67.animation2.setDirection(v67.getValue() ? cp.FORWARDS : cp.BACKWARDS);
         float v81 = v67.animation2.getOutput();
         draw.drawFadingText(
            v67.getName(), sx, sy + 1.0F * S, sw - 34.0F * S, up.getColor(255, (0.55F + 0.3F * v74) * globalAnim), 6.5F * S
         );
         this.m1415(sx + sw - 20.0F * S, sy, 20.0F * S, 10.0F * S, v81, v74, globalAnim);
      } else if (setting instanceof SettingVisibility v66) {
         boolean v73 = this.f1153 == v66;
         String var160 = f.getName();
         String v52 = v66.getName();
         String v53 = var160;
         Animation v80 = this.m1392(v53 + ":" + v52 + ":act");
         cp var161;
         if (v73) {
            var161 = cp.FORWARDS;
            
         } else {
            var161 = cp.BACKWARDS;
         }

         v80.setDirection(var161);
         float v88 = v80.getOutput();
         String v95 = v73 ? "..." : (v66.get() == -1 ? "Press key" : eq.keyName(v66.get()));
         float var103 = draw.getWidth(v95, 6.0F * S) + 12.0F * S;
         draw.drawFadingText(v66.getName(), sx, sy + 1.0F * S, sw - var103 - 10.0F * S, up.getColor(255, 0.6F * globalAnim), 6.5F * S);
         float var110 = sx + sw - var103;
         vs.ys.rect(
            var110,
            sy,
            var103,
            12.0F * S,
            up.overCol(up.getColor(255, (int)(14.0F * globalAnim)), up.replAlpha(up.client(), 0.25F * globalAnim), v88),
            3.0F * S
         );
         vs.ys.outline(var110, sy, var103, 12.0F * S, 0.5F * S, up.replAlpha(up.client(), globalAnim * v88), 3.0F * S);
         if (v88 > 0.02F) {
            vs.ys.glow(var110, sy, var103, 12.0F * S, up.replAlpha(up.client(), 0.15F * globalAnim * v88), 3.0F * S, 6.0F, 1.0F);
         }

         regular.drawCentered(v95, var110 + var103 / 2.0F, sy + 2.5F * S, 6.0F * S, up.getColor(255, 0.65F * globalAnim));
      } else if (setting instanceof StringSetting v65) {
         boolean v72 = this.f1155 == v65;
         String var159 = f.getName();
         String v54 = v65.getName();
         String v55 = var159;
         Animation v79 = this.m1392(v55 + ":" + v54 + ":act");
         v79.setDirection(v72 ? cp.FORWARDS : cp.BACKWARDS);
         float v87 = v79.getOutput();
         String var157;
         if (v72) {
            var157 = this.f1156;
            String v56 = System.currentTimeMillis() / (-956984213875541175L + 956984213875541575L) % (-5655870090472305355L + 5655870090472305357L) == 0L
               ? "_"
               : "";
            String v57 = var157;
            var157 = v57 + v56;
         } else {
            var157 = v65.getValue();
         }

         String v94 = var157;
         boolean var102 = v94.isEmpty();
         String var109 = var102 ? "None" : v94;
         float var115 = Math.min(draw.getWidth(var109, 6.0F * S) + 14.0F * S, sw * 0.55F);
         draw.drawFadingText(v65.getName(), sx, sy + 1.0F * S, sw - var115 - 10.0F * S, up.getColor(255, 0.6F * globalAnim), 6.5F * S);
         float var120 = sx + sw - var115;
         boolean var125 = !var102;
         if (var125) {
            vr.glow(var120, sy, var115, 12.0F * S, up.replAlpha(up.client(), 0.22F * globalAnim), 6.0F * S, 5.0F, 0.8F);
         }

         vs.ys.rect(
            var120,
            sy,
            var115,
            12.0F * S,
            var125 ? up.replAlpha(up.client(), 0.3F * globalAnim) : up.getColor(255, (int)(14.0F * globalAnim)),
            3.0F * S
         );
         vs.ys.outline(var120, sy, var115, 12.0F * S, 0.5F * S, up.replAlpha(up.client(), globalAnim * v87), 3.0F * S);
         regular.drawFadingText(
            var109,
            var120 + 7.0F * S,
            sy + 2.5F * S,
            var115 - 12.0F * S,
            var125 ? up.replAlpha(up.client(), 0.95F * globalAnim) : up.getColor(255, 0.55F * globalAnim),
            6.0F * S
         );
      } else if (setting instanceof sf v64) {
         String temp1 = f.getName();
         String v58 = v64.getName();
         String v59 = temp1;
         Animation var155 = this.m1392(v59 + ":" + v58 + ":btn");

         Animation v71 = var155;
         v71.setDirection(fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, sx, sy, sw, 16.0F * S) ? cp.FORWARDS : cp.BACKWARDS);
         float v78 = v71.getOutput();
         v64.pressAnim.update();
         float v86 = v64.pressAnim.get();
         draw.draw("Button", sx, sy + 2.0F * S, 6.5F * S, up.getColor(255, 0.6F * globalAnim));
         float v93 = Math.max(draw.getWidth(v64.getName(), 6.0F * S) + 16.0F * S, 44.0F * S);
         float var101 = sx + sw - v93;
         vr.glow(var101, sy, v93, 13.0F * S, up.replAlpha(up.client(), (0.2F + 0.25F * v78) * globalAnim), 6.0F * S, 5.0F, 0.8F);
         vs.ys.rect(var101, sy, v93, 13.0F * S, up.replAlpha(up.client(), (0.55F + 0.3F * v78) * globalAnim), 6.0F * S);
         regular.drawCentered(v64.getName(), var101 + v93 / 2.0F, sy + 2.5F * S, 6.0F * S, up.getColor(255, 0.95F * globalAnim));
      } else if (setting instanceof uf v63) {
         draw.draw(v63.getName(), sx, sy + 1.0F * S, 6.0F * S, up.getColor(255, 0.45F * globalAnim));
         vs.ys.rect(sx, sy + 10.0F * S, sw, 0.5F * S, up.getColor(255, (int)(18.0F * globalAnim)), 0.25F * S);
      } else if (setting instanceof tf v9) {
         draw.draw(v9.getName(), sx, sy + 1.0F * S, 6.5F * S, up.getColor(255, 0.6F * globalAnim));
         int v10 = v9.getValue();
         vs.ys.rect(sx + sw - 14.0F * S, sy, 10.0F * S, 10.0F * S, up.replAlpha(v10, globalAnim), 5.0F * S);
         vs.ys.outline(sx + sw - 14.0F * S, sy, 10.0F * S, 10.0F * S, 0.5F * S, up.getColor(255, (int)(60.0F * globalAnim)), 5.0F * S);
         if (v9.pickerOpen) {
            float v11 = sy + 16.0F * S;
            float v12 = sx + 2.0F * S;
            float v13 = sw - 4.0F * S;
            float[] v14 = v9.getHsb();
            float v16 = v13 / (float)v15;

            for (int v17 = 0; v17 < 3; v17++) {
               float v18 = v11 + (float)(v17 * (12)) * S;
               vs.ys.rect(
                  v12,
                  v18,
                  v13,
                  4.0F * S,
                  up.getColor(18, 19, 26, (int)(220.0F * globalAnim)),
                  2.0F * S
               );

               for (int v19 = 0; v19 < v15; v19++) {
                  float v20 = (float)v19 / (float)v15;
                  float v21 = (float)(v19 + 1) / (float)v15;

                  int v22 = switch (v17) {
                     case 0 -> Color.HSBtoRGB(v20, 1.0F, 1.0F);
                     case 1 -> Color.HSBtoRGB(v14[0], v20, Math.max(0.2F, v14[2]));
                     default -> Color.HSBtoRGB(v14[0], v14[1], v20);
                  };

                  int v23 = switch (v17) {
                     case 0 -> Color.HSBtoRGB(v21, 1.0F, 1.0F);
                     case 1 -> Color.HSBtoRGB(v14[0], v21, Math.max(0.2F, v14[2]));
                     default -> Color.HSBtoRGB(v14[0], v14[1], v21);
                  };
                  int v24 = up.replAlpha(v22, globalAnim);
                  int v25 = up.replAlpha(v23, globalAnim);
                  float v26 = v19 == 0 ? 2.0F * S : 0.0F;
                  float v27 = v19 == v15 - 1 ? 2.0F * S : 0.0F;
                  vs.ys.gradientRect(
                     v12 + (float)v19 * v16,
                     v18,
                     v16 + (v19 == v15 - 1 ? 0.0F : 0.5F * S),
                     4.0F * S,
                     new int[]{v24, v25, v25, v24},
                     v26,
                     v27,
                     v27,
                     v26
                  );
               }

               vs.ys.outline(
                  v12,
                  v18,
                  v13,
                  4.0F * S,
                  0.5F * S,
                  up.getColor(40, 43, 56, (int)(150.0F * globalAnim)),
                  2.0F * S
               );
               float var131 = v17 == 0 ? v14[0] : (v17 == 1 ? v14[1] : v14[2]);
               float var136 = v12 + v13 * var131;
               float var139 = 3.2F * S;

               int var142 = switch (v17) {
                  case 0 -> Color.HSBtoRGB(v14[0], 1.0F, 1.0F);
                  default -> v10;
               };
               vr.glow(
                  var136 - var139, v18 + 2.0F * S - var139, var139 * 2.0F, var139 * 2.0F, up.replAlpha(var142, 0.45F * globalAnim), var139, 3.0F * S, 0.7F
               );
               vs.ys.rect(var136 - var139, v18 + 2.0F * S - var139, var139 * 2.0F, var139 * 2.0F, up.getColor(255, globalAnim), var139);
               vs.ys.rect(
                  var136 - var139 + 1.0F * S,
                  v18 + 2.0F * S - var139 + 1.0F * S,
                  (var139 - 1.0F * S) * 2.0F,
                  (var139 - 1.0F * S) * 2.0F,
                  up.replAlpha(var142, globalAnim),
                  var139 - 1.0F * S
               );
            }
         }
      }
   }

   private boolean m1402(Module f) {

      if (this.m1401(f)) {

         if (f == this.select || f.isEnabled() && !this.f1180.contains(f)) {
            return true;
         }
      }

      return false;
   }

   private float m1407(yt draw, String text, float cx, float cy, float size, int color) {
      draw.draw(text, cx, cy, size, color);

      return cx + draw.getWidth(text, size) + 5.0F * S;
   }

   private String m1406(Category c) {

      return c == Category.OTHER ? "Misc" : c.getName();
   }

   public boolean method_25402(Click click, boolean doubled) {
      float v3 = (float)((int)(click.comp_4798() / (double)this.f1140));
      float v4 = (float)((int)(click.comp_4799() / (double)this.f1140));
      ro v5 = so.active();
      if (v5 != null) {
         return v5.mouseClicked(v3, v4, click.method_74245());
      } else if (this.f1154 != null) {
         this.f1154 = null;
         tq.bindReset();
         return true;
      } else if (this.f1153 != null) {
         if (this.f1153.allowMouse && click.method_74245() != 0 && click.method_74245() != 1) {
            this.f1153.set(click.method_74245());
            tq.bindSet();
         } else {
            tq.bindReset();
         }

         this.f1153 = null;
         return true;
      } else {
         if (this.f1155 != null) {
            this.f1155.set(this.f1156);
            this.f1155 = null;
            tq.editCommit();
         }

         int v6 = (int)((float)mc.method_22683().method_4486() / this.f1140);
         int v7 = (int)((float)mc.method_22683().method_4502() / this.f1140);
         float v8 = 132.0F * S;
         float v9 = 392.0F * S;
         float v10 = 8.0F * S;
         float v11 = v8 + v10 + v9;
         float v12 = 296.0F * S;
         float v13 = (float)v6 / 2.0F - v11 / 2.0F;
         float v14 = (float)v7 / 2.0F - v12 / 2.0F;
         float v15 = v13;
         float v17 = v13 + v8 + v10;
         float v19 = 8.0F * S;
         yt v20 = hu.sf_regular;
         if (!this.m1425() && this.m1439(v17, v14, v9, (double)v3, (double)v4, click.method_74245())) {
            return true;
         } else {
            float v21 = v14 + v19;
            float v22 = 20.0F * S;
            float v23 = 68.0F * S;
            float v24 = v13 + v19;
            boolean v25 = fq.isHovered(v3, v4, v24 - 2.0F * S, v21 - 2.0F * S, v23 + 4.0F * S, v22 + 4.0F * S);
            if (v25 && click.method_74245() == 0) {
               themeMenuOpen = !themeMenuOpen;
               tq.category(0, 1);
               return true;
            } else {
               if (themeMenuOpen && click.method_74245() == 0) {
                  themeMenuOpen = false;
               }

               float v26 = v14 + v19 + 20.0F * S + 10.0F * S;
               Category[][] v27 = new Category[][]{{Category.COMBAT, Category.MOVEMENT, Category.PLAYER}, {Category.RENDER}, {Category.OTHER}};

               for (Category[] v32 : v27) {
                  v26 += 11.0F * S;

                  for (Category v36 : v32) {
                     float v37 = 19.0F * S;
                     if (click.method_74245() == 0
                        && fq.isHovered(v3, v4, v15 + v19, v26, v8 - v19 * 2.0F, v37)
                        && (this.active != v36 || this.m1425())) {
                        this.m1430();
                        if (this.active != v36) {
                           this.active = v36;
                           animCategoryReset.reset();
                        }

                        this.f1146 = 0.0F;
                        tq.category(v28, ff.values().length);
                        return true;
                     }

                     v26 += v37 + 1.0F * S;
                     v28++;
                  }

                  v26 += 7.0F * S;
               }

               float v58 = v8 - v19 * 2.0F;
               float v59 = 30.0F * S;
               float v60 = v14 + v12 - v19 - v59;
               float v61 = 20.0F * S;
               float v62 = v60 - 6.0F * S - v61;
               if (fq.isHovered(v3, v4, v15 + v19, v62, v58, v61)) {
                  if (click.method_74245() == 1) {
                     this.m1430();
                     searchActive = false;
                     tq.searchClear();
                  } else if (click.method_74245() == 0) {
                     if (!searchActive) {
                        tq.editStart();
                     }

                     searchActive = true;
                     this.f1163 = System.currentTimeMillis();
                  }

                  return true;
               } else {
                  searchActive = false;
                  float v63 = v14 + 26.0F * S;
                  float v64 = v17 + 12.0F * S;
                  float v65 = v63 + 8.0F * S;
                  float v66 = v9 - 24.0F * S;
                  float v38 = v14 + v12 - 10.0F * S - v65;
                  float v39 = 10.0F * S;
                  float v40 = (v66 - v39) / 2.0F;
                  float v41 = v64 + v40 + v39;
                  float v42 = 8.0F * S;
                  ArrayList v43 = new ArrayList();

                  for (Module v45 : Client.get().moduleManager().values()) {
                     if (this.m1426(v45)) {
                        v43.add(v45);
                     }
                  }

                  int v67 = (v43.size() + 1) / 2;
                  boolean v68 = fq.isHovered(v3, v4, v64, v65, v66, v38);
                  if (v68) {
                     for (int v46 = 0; v46 < 2; v46++) {
                        float v47 = v46 == 0 ? v64 : v41;
                        List v48 = v46 == 0
                           ? v43.subList(0, Math.min(v67, v43.size()))
                           : (v43.size() > v67 ? v43.subList(v67, v43.size()) : Collections.emptyList());
                        float v49 = v65 - this.f1147;

                        for (Module v51 : v48) {
                           float v52 = this.m1414(v51, v40, v20);
                           if (fq.isHovered(v3, v4, v47, v49, v40, 22.0F * S)) {
                              float v53 = v47 + v40 - 8.0F * S;
                              if (v3 >= v47 + v40 - 16.0F * S && (click.method_74245() == 0 || click.method_74245() == 1)) {
                                 if (!this.m1401(v51)) {
                                    this.m1403(v51);
                                    return true;
                                 }

                                 if (this.m1402(v51)) {
                                    if (v51 == this.select) {
                                       this.select = null;
                                    }

                                    this.f1180.add(v51);
                                    tq.expand(false);
                                 } else {
                                    this.f1180.remove(v51);
                                    this.select = v51;
                                    tq.expand(true);
                                 }

                                 return true;
                              }

                              v53 -= 12.0F * S;
                              v53 -= 22.0F * S + 5.0F * S;
                              v53 -= 11.0F * S + 5.0F * S;
                              if (v51.getKey() > 0 || this.f1154 == v51) {
                                 String v54 = this.f1154 == v51 ? "..." : eq.keyName(v51.getKey()).replace("NONE", "n/a");
                                 float v55 = v20.getWidth(v54, 5.5F * S) + 10.0F * S;
                                 if (click.method_74245() == 0 && fq.isHovered(v3, v4, v53 - v55, v49, v55 + 4.0F * S, 20.0F * S)) {
                                    this.f1154 = v51;
                                    tq.bindStart();
                                    return true;
                                 }
                              }

                              if (click.method_74245() == 2) {
                                 this.f1154 = v51;
                                 tq.bindStart();
                                 return true;
                              }

                              if (click.method_74245() == 0) {
                                 v51.setEnabled(!v51.isEnabled());
                                 if (v51.isEnabled()) {
                                    if (this.m1401(v51)) {
                                       this.f1180.remove(v51);
                                       this.select = v51;
                                       tq.expand(true);
                                    } else {
                                       tq.toggle(true);
                                    }
                                 } else {
                                    tq.toggle(false);
                                 }

                                 return true;
                              }

                              if (click.method_74245() == 1) {
                                 if (!this.m1401(v51)) {
                                    this.m1403(v51);
                                    return true;
                                 }

                                 if (this.m1402(v51)) {
                                    if (v51 == this.select) {
                                       this.select = null;
                                    }

                                    this.f1180.add(v51);
                                    tq.expand(false);
                                 } else {
                                    this.f1180.remove(v51);
                                    this.select = v51;
                                    tq.expand(true);
                                 }

                                 return true;
                              }
                           }

                           float v74 = this.m1404(v51);
                           label207:
                           if (v74 > 0.35F) {
                              if (click.method_74245() != 0) {

                                 if (click.method_74245() != 1) {
                                    break label207;
                                 }
                              }

                              if (fq.isHovered(v3, v4, v47, v49 + 22.0F * S, v40, v52 - 22.0F * S)) {
                                 float v75 = (1.0F - v74) * 7.0F * S;
                                 if (this.m1418(v51, v47 + 8.0F * S, v49 + 24.0F * S - v75, v40 - 16.0F * S, v3, v4, click.method_74245(), v20)
                                    )
                                  {
                                    return true;
                                 }
                              }
                           }

                           v49 += v52 + v42;
                        }
                     }
                  }

                  float v69 = 0.0F;
                  float v70 = 0.0F;
                  return super.method_25402(click, doubled);
               }
            }
         }
      }
   }

   public ClickGuiScreen() {
      super(Text.method_43470("MenuNight"));
      this.f1139 = go.getInstance();
      this.f1140 = 1.0F;
      this.f1141 = false;
      this.f1142 = 0L;
      this.f1143 = 0.0F;
      this.f1144 = 0.0F;
      this.f1145 = 0;
      this.select = null;
      this.active = Category.COMBAT;
      this.f1146 = 0.0F;
      this.f1147 = 0.0F;
      this.f1148 = 0.0F;
      this.f1149 = 0.0F;
      this.f1150 = 0.0F;
      this.f1151 = 0.0F;
      this.f1152 = null;
      this.f1153 = null;
      this.f1154 = null;
      this.f1155 = null;
      this.f1156 = "";
      this.f1157 = null;
      this.f1158 = 0;
      this.f1159 = 0.0F;
      this.f1160 = 0.0F;
      this.f1161 = false;
      this.f1163 = System.currentTimeMillis();
      this.f1164 = new ep(300, 1.0, cp.BACKWARDS);
      this.f1165 = new ep(300, 1.0, cp.BACKWARDS);
      this.f1166 = new ep(300, 1.0, cp.BACKWARDS);
      this.f1167 = new HashMap<>();
      this.f1168 = new fs();
      this.f1169 = new ur();
      this.f1170 = new ct();
      this.f1171 = new qs();
      this.f1172 = new gs();
      this.f1173 = new ArrayList<>();
      this.f1175 = -5278364759087856865L + 5278364759087856955L;
      this.f1176 = new HashMap<>();
      this.f1177 = new HashMap<>();
      this.f1178 = new HashMap<>();
      this.f1179 = null;
      this.f1180 = Collections.newSetFromMap(new IdentityHashMap<>());
      this.f1181 = new IdentityHashMap<>();
      this.f1182 = new IdentityHashMap<>();
      this.f1183 = new IdentityHashMap<>();
      this.f1184 = new IdentityHashMap<>();
   }

   public void openHandsEditor() {

      this.m1436();
      this.f1139.open();
      tq.editor();
   }

   private String m1424() {
      int temp2 = v1 + 1;

      return f1162.trim().toLowerCase();
   }

   private int m1427() {
      Iterator v2 = Client.get().moduleManager().values().iterator();

      while (v2.hasNext()) {
         Module v3 = (Module)v2.next();
         if (this.m1426(v3)) {
            v1++;
         }
      }

      return v1;
   }

   public void openPreviewEditor(Module target) {
      this.m1436();
      mo.getInstance().open(target);
      tq.editor();

      
   }

   private void m1428() {
      this.f1163 = System.currentTimeMillis();
      this.f1146 = 0.0F;
      animCategoryReset.reset();
      
   }

   public boolean method_25403(Click click, double deltaX, double deltaY) {
      ro v6 = so.active();
      if (v6 != null) {
         double temp1 = deltaX / (double)this.f1140;

         return v6.mouseDragged((float)temp1, (float)(deltaY / (double)this.f1140));
      } else {
         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public boolean method_25401(double mouseXRaw, double mouseYRaw, double horizontalAmount, double verticalAmount) {
      float v9 = (float)(mouseXRaw / (double)this.f1140);

      float v10 = (float)(mouseYRaw / (double)this.f1140);
      ro v11 = so.active();
      if (v11 != null) {
         return v11.mouseScrolled(v9, v10, verticalAmount);
      } else {
         int v12 = (int)((float)mc.method_22683().method_4486() / this.f1140);
         int v13 = (int)((float)mc.method_22683().method_4502() / this.f1140);
         float v14 = 132.0F * S;
         float v15 = 392.0F * S;
         float v16 = 8.0F * S;
         float v17 = v14 + v16 + v15;
         float v18 = 296.0F * S;
         float v19 = (float)v12 / 2.0F - v17 / 2.0F;
         float v20 = (float)v13 / 2.0F - v18 / 2.0F;
         float v21 = v19 + v14 + v16;
         float v23 = v20 + 26.0F * S;
         float v24 = v21 + 12.0F * S;
         float v25 = v23 + 8.0F * S;
         float v26 = v15 - 24.0F * S;
         float result = v20 + v18 - 10.0F * S;

         float v27 = result - v25;
         if (fq.isHovered(v9, v10, v24, v25, v26, v27)) {
            float v28 = this.f1146;
            this.f1146 = fq.clamp((float)((double)this.f1146 - verticalAmount * 25.0 * (double)S), 0.0F, this.f1148);
            if (this.f1146 != v28) {
               tq.scroll();
            }

            return true;
         } else {
            return super.method_25401(mouseXRaw, mouseYRaw, horizontalAmount, verticalAmount);
         }
      }
   }

   private float m1422(ModeSetting s, float width) {

      return this.m1421(s, s.values, width);
   }

   private boolean m1418(Module f, float sx, float sy, float sw, float mx, float my, int button, yt draw) {
      for (Setting v10 : f.getSettings()) {
         float v11 = this.m1412(f, v10, sw, draw);
         if (!(v11 <= 0.0F) && v10.getVisible().get()) {
            if (v10 instanceof SliderSetting v12) {
               if (button == 0 && fq.isHovered(mx, my, sx, sy, sw, 26.0F * S)) {
                  this.f1152 = v12;
                  tq.sliderGrab();
                  return true;
               }
            } else if (v10 instanceof ModeSetting v13) {
               float v34 = 0.0F;
               float v39 = 13.0F * S;

               for (String v48 : v13.values) {
                  float result = draw.getWidth(v48, 6.0F * S);
                  float temp1 = 12.0F * S;

                  float v50 = Math.max(result + temp1, 34.0F * S);
                  if (v34 + v50 > sw && v34 > 0.0F) {
                     v34 = 0.0F;
                     v39 += 15.0F * S;
                  }

                  if ((button == 0 || button == 1) && fq.isHovered(mx, my, sx + v34, sy + v39, v50 + 2.0F * S, 14.0F * S)) {
                     if (!v48.equals(v13.getValue())) {
                        tq.chip(v42, v13.values.size());
                     }

                     v13.set(v48);
                     return true;
                  }

                  v34 += v50 + 4.0F * S;
                  v42++;
               }
            } else if (v10 instanceof MultiSetting v14) {
               if (this.m1431(v14).size() > 3) {
                  float v33 = sy + 11.0F * S;
                  if (fq.isHovered(mx, my, sx, v33, sw, 15.0F * S)) {
                     this.f1178.put(v14, !this.m1400(v14));
                     tq.expand(this.m1400(v14));
                     return true;
                  }

                  if (this.m1400(v14)) {
                     float v38 = v33 + 17.0F * S;
                     float v41 = v38 + 2.0F * S;
                     float v44 = 15.0F * S;

                     for (BooleanSetting v49 : v14.getValues()) {
                        if (fq.isHovered(mx, my, sx + 2.0F * S, v41, sw - 4.0F * S, v44)) {
                           v49.set(!v49.getValue());
                           tq.chipMulti(v49.getValue());
                           return true;
                        }

                        v41 += v44;
                     }

                     if (fq.isHovered(mx, my, sx, v38, sw, (float)(v14.getValues().size() * (15)) * S + 4.0F * S)) {
                        return true;
                     }
                  }
               } else {
                  float v32 = 0.0F;
                  float v37 = 13.0F * S;

                  for (BooleanSetting v43 : v14.getValues()) {
                     float v46 = Math.max(draw.getWidth(v43.getName(), 6.0F * S) + 12.0F * S, 34.0F * S);
                     if (v32 + v46 > sw) {

                        if (v32 > 0.0F) {
                           v32 = 0.0F;
                           v37 += 15.0F * S;
                        }
                     }

                     if ((button == 0 || button == 1) && fq.isHovered(mx, my, sx + v32, sy + v37, v46 + 2.0F * S, 14.0F * S)) {
                        v43.set(!v43.getValue());
                        tq.chipMulti(v43.getValue());
                        return true;
                     }

                     v32 += v46 + 4.0F * S;
                  }
               }
            } else if (v10 instanceof BooleanSetting v15) {
               if ((button == 0 || button == 1) && fq.isHovered(mx, my, sx, sy, sw, 14.0F * S)) {
                  v15.set(!v15.getValue());
                  tq.toggle(v15.getValue());
                  return true;
               }
            } else if (v10 instanceof sf v16) {
               float v31 = Math.max(draw.getWidth(v16.getName(), 6.0F * S) + 16.0F * S, 44.0F * S);
               if ((button == 0 || button == 1) && fq.isHovered(mx, my, sx + sw - v31 - 2.0F * S, sy - 2.0F * S, v31 + 4.0F * S, 17.0F * S)) {
                  v16.press();
                  tq.button();
                  return true;
               }
            } else if (v10 instanceof SettingVisibility v17) {
               if (button == 0) {
                  String v30 = v17.get() == -1 ? "Press key" : eq.keyName(v17.get());
                  float v36 = draw.getWidth(v30, 6.0F * S) + 12.0F * S;
                  if (fq.isHovered(mx, my, sx + sw - v36 - 2.0F * S, sy - 2.0F * S, v36 + 4.0F * S, 16.0F * S)) {
                     this.f1153 = v17;
                     tq.bindStart();
                     return true;
                  }
               }
            } else if (v10 instanceof StringSetting v18) {
               if (button == 0) {
                  String v29 = v18.getValue().isEmpty() ? "None" : v18.getValue();
                  float v35 = Math.min(draw.getWidth(v29, 6.0F * S) + 14.0F * S, sw * 0.55F);
                  float temp2 = sx + sw - v35 - 2.0F * S;
                  float temp3 = sy - 2.0F * S;

                  if (fq.isHovered(mx, my, temp2, temp3, v35 + 4.0F * S, 16.0F * S)) {
                     this.f1155 = v18;
                     this.f1156 = v18.getValue();
                     tq.editStart();
                     return true;
                  }
               }
            } else if (!(v10 instanceof uf) && v10 instanceof tf v19) {
               if (button == 0 && fq.isHovered(mx, my, sx, sy, sw, 14.0F * S)) {
                  v19.pickerOpen = !v19.pickerOpen;
                  tq.picker(v19.pickerOpen);
                  return true;
               }

               if (v19.pickerOpen && button == 0) {
                  float v20 = sy + 16.0F * S;
                  float v21 = sx + 2.0F * S;
                  float v22 = sw - 4.0F * S;

                  for (int v23 = 0; v23 < 3; v23++) {
                     float v24 = v20 + (float)(v23 * (12)) * S;
                     if (fq.isHovered(mx, my, v21 - 2.0F * S, v24 - 3.0F * S, v22 + 4.0F * S, 10.0F * S)) {
                        this.f1157 = v19;
                        this.f1158 = v23;
                        this.f1159 = v21;
                        this.f1160 = v22;
                        float v25 = fq.clamp((mx - v21) / v22, 0.0F, 1.0F);
                        v19.updateHsbBar(v23, v25);
                        tq.sliderGrab();
                        return true;
                     }
                  }

                  if (fq.isHovered(mx, my, sx, sy, sw, 54.0F * S)) {
                     return true;
                  }
               }
            }

            sy += v11;
         }
      }

      return false;
   }

   private float m1410(String header, Category[] cats, String[] labels, float gx, float gy, float gw, float globalAnim, yt draw) {

      for (Category v13 : cats) {
         v9 += this.m1405(v13);
      }

      draw.draw(header, gx, gy, 6.0F * S, up.getColor(255, 0.38F * globalAnim));
      String v29 = String.valueOf(v9);
      float temp2 = 6.0F * S;

      float v30 = draw.getWidth(v29, temp2);
      temp2 = gx + gw;

      draw.draw(v29, temp2 - v30, gy, 6.0F * S, up.getColor(255, 0.38F * globalAnim));
      draw.draw("v", gx + gw - v30 - 8.0F * S, gy, 6.0F * S, up.getColor(255, 0.3F * globalAnim));
      gy += 11.0F * S;

      for (int v31 = 0; v31 < cats.length; v31++) {
         Category v32 = cats[v31];
         boolean v14 = this.active == v32;
         v32.alphaS.setDirection(v14 ? cp.FORWARDS : cp.BACKWARDS);
         float v15 = v32.alphaS.getOutput();
         float v16 = 19.0F * S;
         boolean v17 = fq.isHovered((float)this.lastMouseX, (float)this.lastMouseY, gx, gy, gw, v16);
         v32.alphaS2.setDirection(v17 ? cp.FORWARDS : cp.BACKWARDS);
         float v18 = v32.alphaS2.getOutput();
         float v19 = Math.max(v15, v18 * 0.5F);
         if (v19 > 0.01F) {
            vs.ys.rect(gx, gy, gw, v16, up.replAlpha(up.client(), 0.14F * globalAnim * v19), 5.0F * S);
         }

         if (v14) {
            float v21 = gy + 4.0F * S;
            float v22 = 2.0F * S;
            float v23 = v16 - 8.0F * S;
            vs.ys.rect(gx, v21, v22, v23, up.replAlpha(up.client(), 0.9F * globalAnim), 1.0F * S);
            vr.glow(gx, v21, v22, v23, up.replAlpha(up.client(), 0.3F * globalAnim), 1.0F * S, 4.0F, 0.7F);
         }

         float v20 = 0.45F + 0.45F * Math.max(v15, v18);

         try {
            hu.nightix_2.draw(v32.getIcon(), gx + 8.0F * S, gy + 6.5F * S, 7.0F * S, up.getColor(255, globalAnim * v20));
         } catch (Exception v27) {
         }

         String temp1 = labels[v31];
         temp2 = gx + 22.0F * S;
         float v36 = gy + 5.5F * S;
         float temp4 = 7.0F * S;

         draw.draw(temp1, temp2, v36, temp4, up.getColor(255, globalAnim * (v14 ? 0.92F : 0.45F + 0.3F * v18)));
         gy += v16 + 1.0F * S;
      }

      return gy + 7.0F * S;
   }

   private void m1403(Module f) {

      this.f1181.put(f, System.currentTimeMillis());
      tq.deny();
   }

   @Environment(EnvType.CLIENT)
   private static final class lo {
      final float lifeMs;
      final float y;
      final float drift;
      final float maxRadius;
      final float x;
      final long born = System.currentTimeMillis();

      float progress() {
         long result = System.currentTimeMillis();

         return (float)(result - this.born) / this.lifeMs;
      }

      lo(float x, float y) {
         this.x = x;
         this.y = y;
         this.maxRadius = 10.0F + (float)Math.random() * 7.0F;
         this.lifeMs = 1400.0F + (float)Math.random() * 700.0F;
         this.drift = 4.0F + (float)Math.random() * 6.0F;
      }

      
   }
}
