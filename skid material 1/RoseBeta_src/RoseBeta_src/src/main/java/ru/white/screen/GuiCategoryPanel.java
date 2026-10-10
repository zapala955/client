package ru.white.screen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.white.ui.DraggableWidget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.alt.AltManager;
import ru.white.alt.o;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.dt;
import ru.white.utils.render.et;
import ru.white.utils.render.us;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class GuiCategoryPanel extends Screen implements IMinecraft {
   private float f1065;
   private float f1048;
   private static final float ROW_GAP = 4.0F;
   private float f1033;
   private final Animation f1027;
   private float f1034;
   private float f1032;
   private float f1051;
   private float f1040;
   private float f1038;
   private final Animation f1025;
   private final List<float[]> f1066;
   private float f1054;
   private final Animation f1024;
   private float f1056;
   private float f1053;
   private final Animation f1023;
   private float f1060;
   private final List<float[]> f1067;
   private final Map<o, ao.bo> f1043;
   private float f1052;
   private final Animation f1011 = new Animation();
   private float f1061;
   private String f1020;
   private Screen f1030;
   private final Animation f1022;
   private boolean f1016;
   private final Screen f1010;
   private float f1035;
   private float f1017;
   private float f1036;
   private final List<o> f1069;
   private float f1058;
   private final List<float[]> f1068;
   private final Animation f1026;
   private float f1012 = 1.0F;
   private float f1050;
   private float f1046;
   private float f1041;
   private float f1037;
   private float f1062;
   private float f1042;
   private float f1014;
   private float f1049;
   private float f1055;
   private float f1047;
   private float f1013;
   private float f1018;
   private boolean f1019;
   private boolean f1021;
   private String f1015 = "";
   private final Animation f1028;
   private float f1031;
   private float f1059;
   private static final float ROW_H = 20.0F;
   private boolean f1029;
   private final SimpleDateFormat f1045;
   private float f1039;
   private float f1064;
   private final Set<o> f1044;
   private float f1057;
   private float f1063;

   public boolean method_25421() {

      return false;
   }

   public boolean method_25400(CharInput input) {
      if (this.f1021 && input.method_74227() && this.f1020.length() < 12) {
         String v10 = input.method_74226();

         for (int v11 = 0; v11 < v10.length(); v11++) {
            char v12 = v10.charAt(v11);
            if (Character.isLetterOrDigit(v12) || v12 == 95) {
               String v6 = this.f1020;
               StringBuilder temp1 = new StringBuilder;

               temp1./* $VF: Unable to resugar constructor */<init>();
               this.f1020 = temp1.append((Object)v6).append(v12).toString();
            }
         }

         return true;
      } else if (this.f1016 && input.method_74227() && this.f1015.length() < 16) {
         String v2 = input.method_74226();

         for (int v3 = 0; v3 < v2.length(); v3++) {
            char v4 = v2.charAt(v3);
            if (Character.isLetterOrDigit(v4) || v4 == 95) {
               String v8 = this.f1015;
               this.f1015 = v8 + v4;
            }
         }

         return true;
      } else {
         return super.method_25400(input);
      }
   }

   private void m1351() {
      AltManager result = AltManager.get();

      o v1 = result.add(this.f1015);
      if (v1 != null) {
         this.f1015 = "";
         this.f1018 = Float.MAX_VALUE;
         
      }
   }

   private void m1346(Screen screen) {

      if (!this.f1029) {
         this.f1030 = screen;
         this.f1029 = true;
         this.f1028.set(0.0);
         this.f1028.run(1.0, 0.22, ap.CUBIC_OUT);
         
      }
   }

   private void m1347(float x, float y, float w, float h, String label, float a, float IMinecraft, boolean accent) {
      int v9 = up.getColor(19, 255, 174, 255);
      int result;
      if (accent) {
         result = up.replAlpha(v9, a * (0.22F + 0.14F * IMinecraft));
      } else {
         result = 12;

         result = up.interpolate(
            up.getColor(result, 12, 14, (int)(a * 22.0F)),
            up.getColor(19, 255, 174, (int)(a * 28.0F)),
            (double)IMinecraft
         );
      }

      int v10 = result;
      v10 = up.replAlpha(v10, (int)((float)up.alpha(v10) * (0.7F + 0.3F * IMinecraft)));
      vs.ys.rect(x, y, w, h, v10, 6.0F);
      int v11 = up.getColor(255, 255, 255, 1.0F);

      hu.sf_regular.drawCentered(label, x + w / 2.0F, y + h / 2.0F - 4.75F, 7.0F, up.getColor(175, a));
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25401(double mx, double my, double horizontal, double vertical) {
      float temp2 = (float)vertical * 22.0F;

      this.f1018 -= temp2;
      return true;
   }

   private float m1349() {
      return this.f1013;
   }

   public boolean method_25402(Click click, boolean doubled) {
      if (this.f1029) {
         return true;
      } else {
         float v3 = (float)(click.comp_4798() / (double)this.f1012);
         float v4 = (float)(click.comp_4799() / (double)this.f1012);
         if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
         } else {
            boolean v5 = this.f1019 && fq.isHovered(v3, v4, this.f1035, this.f1036, this.f1037, this.f1038);
            if (!fq.isHovered(v3, v4, this.f1046, this.f1047, this.f1048, this.f1049) && !v5) {
               this.m1346(this.f1010);
               return true;
            } else if (v5) {
               if (fq.isHovered(v3, v4, this.f1039, this.f1040, this.f1041, this.f1042)) {
                  this.f1021 = true;
                  this.f1016 = false;
               } else {
                  this.f1021 = false;
               }

               return true;
            } else if (fq.isHovered(v3, v4, this.f1031, this.f1032, this.f1033, this.f1034)) {
               this.f1019 = !this.f1019;
               this.f1021 = this.f1019;
               this.f1016 = false;
               return true;
            } else if (fq.isHovered(v3, v4, this.f1050, this.f1051, this.f1052, this.f1053)) {
               this.f1016 = true;
               this.f1021 = false;
               return true;
            } else {
               this.f1016 = false;
               if (fq.isHovered(v3, v4, this.f1054, this.f1055, this.f1056, this.f1057)) {
                  this.m1351();
                  return true;
               } else if (fq.isHovered(v3, v4, this.f1058, this.f1059, this.f1060, this.f1061)) {
                  this.f1015 = AltManager.get().randomName(this.f1020);
                  this.f1016 = true;
                  this.f1021 = false;
                  return true;
               } else {
                  for (int v6 = 0; v6 < this.f1068.size(); v6++) {
                     float[] v7 = this.f1068.get(v6);
                     if (fq.isHovered(v3, v4, v7[0], v7[1], v7[2], v7[3])) {
                        AltManager.get().toggleFavorite(this.f1069.get(v6));
                        return true;
                     }
                  }

                  for (int v9 = 0; v9 < this.f1067.size(); v9++) {
                     float[] v11 = this.f1067.get(v9);
                     float temp2 = v11[0];
                     float temp3 = v11[1];
                     float temp4 = v11[2];
                     float temp5 = v11[3];

                     if (fq.isHovered(v3, v4, temp2, temp3, temp4, temp5)) {
                        this.f1044.add(this.f1069.get(v9));
                        return true;
                     }
                  }

                  for (int v10 = 0; v10 < this.f1066.size(); v10++) {
                     float[] v12 = this.f1066.get(v10);
                     if (fq.isHovered(v3, v4, v12[0], v12[1], v12[2], v12[3])) {
                        AltManager.get().login(this.f1069.get(v10));
                        return true;
                     }
                  }

                  return super.method_25402(click, doubled);
               }
            }
         }
      }
   }

   public boolean method_25404(KeyInput keyInput) {
      if (this.f1029) {
         return true;
      } else {
         int v2 = keyInput.comp_4795();
         if (!this.f1021) {
            if (this.f1016) {
               if (v2 == 257) {
                  this.m1351();
               } else if (v2 == 256) {
                  this.f1016 = false;
               } else if (v2 == 259 && !this.f1015.isEmpty()) {
                  this.f1015 = this.f1015.substring(0, this.f1015.length() - 1);
               }

               return true;
            } else if (v2 == 256) {

               if (this.f1019) {
                  this.f1019 = false;
                  return true;
               } else {

                  this.m1346(this.f1010);
                  return true;
               }
            } else {
               return super.method_25404(keyInput);
            }
         } else {
            if (v2 == 257 || v2 == 256) {
               this.f1021 = false;
            } else if (v2 == 259 && !this.f1020.isEmpty()) {
               this.f1020 = this.f1020.substring(0, this.f1020.length() - 1);
            }

            return true;
         }
      }
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private void m1348(float a, yt font) {
      this.f1026.run(this.f1019 ? 1.0 : 0.0, 0.25, ap.SINE_OUT);
      float v3 = this.f1026.get();
      float v4 = a * v3;
      this.f1037 = 168.0F;
      this.f1038 = 96.0F;
      this.f1036 = this.f1051 + this.f1053 - this.f1038;
      float v5 = this.f1046 + this.f1048 - this.f1037 + 24.0F;
      float v6 = this.f1046 + this.f1048 + 8.0F;
      this.f1035 = v5 + (v6 - v5) - 8.0F + 8.0F * v4;
      vs.ws.blur(this.f1035, this.f1036, this.f1037, this.f1038, v4, 6.0F, up.getColor(20, v4 * 0.3F));
      float v7 = 12.0F;
      font.draw(
         bb.pick("Настройки рандома", "Random settings"),
         this.f1035 + v7,
         this.f1036 + 9.0F,
         8.0F,
         up.getColor(185, v4)
      );
      font.draw(
         bb.pick("Начальный ник", "Initial nickname"),
         this.f1035 + v7,
         this.f1036 + 26.0F,
         6.5F,
         up.getColor(185, v4)
      );

      this.f1039 = this.f1035 + v7;
      this.f1040 = this.f1036 + 37.0F;
      this.f1041 = this.f1037 - v7 * 2.0F;
      this.f1042 = 20.0F;
      this.f1027.run(this.f1021 ? 1.0 : 0.0, 0.2, ap.QUAD_OUT, true);
      this.f1027.update();
      float v8 = fq.clamp(this.f1027.get(), 0.0F, 1.0F);
      vs.ys.rect(this.f1039, this.f1040, this.f1041, this.f1042, up.getColor(255, v4 * (0.015F + 0.02F * v8)), 5.0F);
      boolean v9 = this.f1021
         && System.currentTimeMillis() / (1831202759810790975L + -1831202759810790525L) % (-7142294930404032722L + 7142294930404032724L) == 0L;
      String v15;
      if (this.f1020.isEmpty() && !this.f1021) {
         v15 = bb.pick("напр. night", "e.g. night");
      } else {
         String v11 = v9 ? "_" : "";
         String v12 = this.f1020;

         v15 = v12 + v11;
      }

      String v10 = v15;
      font.draw(
         v10,
         this.f1039 + 8.0F,
         this.f1040 + 6.0F,
         6.5F,
         this.f1020.isEmpty() && !this.f1021 ? up.getColor(140, v4) : up.getColor(185, v4)
      );
      font.draw(
         bb.pick(
            "+ 4-12 случайных символов",
            "+ 4-12 random chars"
         ),
         this.f1035 + v7,
         this.f1036 + this.f1038 - 16.0F,
         7.0F,
         up.getColor(185, v4 * 0.5F)
      );
   }

   private float m1350() {

      return this.f1014;
   }

   protected void method_25426() {
      this.f1011.set(0.0);
      this.f1011.run(1.0, 0.42F, ap.CUBIC_OUT);

      this.f1029 = false;
      this.f1030 = null;
      this.f1028.set(0.0);
   }

   public GuiCategoryPanel(Screen parent) {
      super(Text.method_43470("AltManager"));
      this.f1016 = false;
      this.f1017 = 0.0F;
      this.f1018 = 0.0F;
      this.f1019 = false;
      this.f1020 = "";
      this.f1021 = false;
      this.f1022 = new Animation();
      this.f1023 = new Animation();
      this.f1024 = new Animation();
      this.f1025 = new Animation();
      this.f1026 = new Animation();
      this.f1027 = new Animation();
      this.f1028 = new Animation();
      this.f1029 = false;
      this.f1030 = null;
      this.f1043 = new IdentityHashMap<>();
      this.f1044 = Collections.newSetFromMap(new IdentityHashMap<>());
      this.f1045 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      this.f1066 = new ArrayList<>();
      this.f1067 = new ArrayList<>();
      this.f1068 = new ArrayList<>();
      this.f1069 = new ArrayList<>();
      this.f1010 = parent;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      if (this.f1029) {
         this.f1028.update();
         if (this.f1028.isFinished() && this.f1030 != null) {
            mc.method_1507(this.f1030);
            this.f1030 = null;
            this.f1029 = false;
            return;
         }
      }

      float v5 = 2.0F;
      float v6 = (float)mc.method_22683().method_4495();
      if (v6 <= 0.001F || Float.isNaN(v6) || Float.isInfinite(v6)) {
         v6 = 1.0F;
      }

      this.f1012 = v5 / v6;
      if (this.f1012 <= 0.001F || Float.isNaN(this.f1012) || Float.isInfinite(this.f1012)) {
         this.f1012 = 1.0F;
      }

      int v7 = mc.method_22683().method_4486();
      int v8 = mc.method_22683().method_4502();
      if (v7 <= 0) {
         v7 = Math.max(1, mc.method_22683().method_4480());
      }

      if (v8 <= 0) {
         v8 = Math.max(1, mc.method_22683().method_4507());
      }

      float v9 = (float)v7 / this.f1012;
      float v10 = (float)v8 / this.f1012;
      if (v9 <= 10.0F || Float.isNaN(v9)) {
         v9 = 960.0F;
      }

      if (v10 <= 10.0F || Float.isNaN(v10)) {
         v10 = 540.0F;
      }

      float v11 = (float)mouseX;
      float v12 = (float)mouseY;
      if (Float.isNaN(v11) || Float.isInfinite(v11) || v11 < -5000.0F || v11 > 10000.0F) {
         v11 = v9 * this.f1012 / 2.0F;
      }

      if (Float.isNaN(v12) || Float.isInfinite(v12) || v12 < -5000.0F || v12 > 10000.0F) {
         v12 = v10 * this.f1012 / 2.0F;
      }

      this.f1013 = v11 / this.f1012;
      this.f1014 = v12 / this.f1012;
      if (Float.isNaN(this.f1013) || Float.isInfinite(this.f1013)) {
         this.f1013 = v9 / 2.0F;
      }

      if (Float.isNaN(this.f1014) || Float.isInfinite(this.f1014)) {
         this.f1014 = v10 / 2.0F;
      }

      this.f1011.update();
      this.f1026.update();
      if (context != null) {
         context.method_51448().pushMatrix();
      }

      us.beginOverlay();
      float v13 = this.f1011.get();
      if (this.f1029) {
         float v14 = fq.clamp(this.f1028.get(), 0.0F, 1.0F);
         v13 *= 1.0F - v14;
      }

      float v74 = (v9 / 2.0F - this.f1013) * 0.045F;
      float result = v10 / 2.0F;

      float v15 = (result - this.f1014) * 0.045F;
      if (Float.isNaN(v74) || Float.isInfinite(v74)) {
         v74 = 0.0F;
      }

      if (Float.isNaN(v15) || Float.isInfinite(v15)) {
         v15 = 0.0F;
      }

      v74 = fq.clamp(v74, -v9 * 0.06F, v9 * 0.06F);
      v15 = fq.clamp(v15, -v10 * 0.06F, v10 * 0.06F);
      float v16 = 1.0F + 0.012F * (float)Math.sin((double)System.currentTimeMillis() / 1700.0);
      if (Float.isNaN(v16) || Float.isInfinite(v16)) {
         v16 = 1.0F;
      }

      float v17 = 1.15F * v16;
      float v18 = v9 * v17;
      float v19 = v10 * v17;
      float v20 = (v9 - v18) / 2.0F + v74;
      float v21 = (v10 - v19) / 2.0F + v15;
      v20 = Math.min(0.0F, Math.max(v9 - v18, v20));
      v21 = Math.min(0.0F, Math.max(v10 - v19, v21));
      vs.xs.texture(
         Identifier.method_60655("godweer", "images/ui/title/title.png"),
         v20,
         v21,
         v18,
         v19,
         up.getColor(255, v13)
      );
      et.capture(2);
      vs.ws.blur(0.0F, 0.0F, v9, v10, v13, up.getColor(0, v13 * 0.2F));
      et.capture(4);
      yt v22 = hu.sf_regular;
      this.f1048 = 280.0F;
      this.f1049 = 274.0F;
      this.f1046 = v9 / 2.0F - this.f1048 / 2.0F;
      this.f1047 = v10 / 2.0F - this.f1049 / 2.0F + 16.0F - 16.0F * v13;
      float v23 = 5.0F;
      float v24 = 20.0F;
      float v25 = 5.0F;
      this.f1055 = this.f1059 = this.f1047 + this.f1049 - v23 - v24;
      this.f1057 = this.f1061 = v24;
      this.f1056 = this.f1060 = (this.f1048 - v23 * 2.0F - v25) / 2.0F;
      this.f1054 = this.f1046 + v23;
      this.f1058 = this.f1054 + this.f1056 + v25;
      this.f1053 = 20.0F;
      float temp1 = this.f1053;

      this.f1034 = this.f1053;
      this.f1033 = temp1;
      this.f1050 = this.f1046 + v23;
      this.f1052 = this.f1048 - v23 * 2.0F - this.f1033 - 6.0F;
      this.f1051 = this.f1055 - v25 - this.f1053;
      this.f1031 = this.f1050 + this.f1052 + 6.0F;
      this.f1032 = this.f1051;
      this.f1062 = this.f1046 + v23;
      this.f1063 = this.f1047 + v23;
      this.f1064 = this.f1048 - v23 * 2.0F;
      this.f1065 = this.f1051 - v25 - this.f1063;
      this.m1348(v13, v22);
      vs.ws.blur(this.f1046, this.f1047, this.f1048, this.f1049, v13, 6.0F, up.getColor(12, v13 * 0.52F));
      vs.ys.rect(this.f1046, this.f1047, this.f1048, this.f1049, up.getColor(12, v13 * 0.38F), 6.0F);

      try {
         Identifier v26 = Identifier.method_60655("client", "textures/rose__logo_watermark.png");
         float v27 = 26.0F;
         float v28 = v9 / 2.0F - v27 / 2.0F;
         float v29 = this.f1047 - 42.0F;
         vr.texture(v26, v28, v29, v27, v27, 1.0F, 4.0F, up.getColor(255, v13));
      } catch (Throwable v73) {
         hu.sfsemi
            .drawCentered(
               "Rose", v9 / 2.0F, this.f1047 - 32.0F, 14.0F, up.getColor(19, 255, 174, v13)
            );
      }

      hu.sf_medium.drawCentered("ALTS", v9 / 2.0F, this.f1047 - 13.0F, 7.5F, up.getColor(255, v13 * 0.5F));
      hu.sf_regular
         .drawCentered(
            "Чтобы выйти, нажмите ESC или кликните по пустому месту.",
            v9 / 2.0F,
            this.f1047 + this.f1049 + 5.0F,
            9.0F,
            up.getColor(255, v13 * 0.06F)
         );
      this.f1066.clear();
      this.f1067.clear();
      this.f1068.clear();
      this.f1069.clear();
      List v79 = AltManager.get().getAccounts();
      List v80 = AltManager.get().getSorted();
      float v81 = (float)v80.size() * 24.0F;
      float v82 = Math.max(0.0F, v81 - this.f1065);
      this.f1018 = fq.clamp(this.f1018, 0.0F, v82);
      this.f1017 = this.f1017 + (this.f1018 - this.f1017) * 0.4F;
      this.f1043.keySet().retainAll(v79);
      dt.enable(this.f1062, this.f1063, this.f1064, this.f1065, 2.0F);
      float v30 = 0.0F;
      String v31 = AltManager.get().currentName();
      ArrayList v32 = null;

      for (o v34 : v80) {
         ao.bo v35 = this.f1043.computeIfAbsent(v34, k -> {
            ao.bo var10000x = new ao.bo;

            var10000x./* $VF: Unable to resugar constructor */<init>();
            return var10000x;
         });
         boolean v36 = this.f1044.contains(v34);
         v35.appear.run(v36 ? 0.0 : 1.0, v36 ? 0.22 : 0.35, ap.QUAD_OUT, true);
         float v37 = fq.clamp(v35.appear.get(), 0.0F, 1.0F);
         float v38 = 24.0F * v37;
         if (!v35.hasOff) {
            v35.curOff = v30;
            v35.hasOff = true;
         } else {
            v35.curOff = v35.curOff + (v30 - v35.curOff) * 0.35F;
         }

         float v39 = this.f1063 - this.f1017 + v35.curOff;
         if (vs.xs.isUrlLoaded(v34.headUrl())) {
            v35.headFade.run(1.0, 0.3, ap.QUAD_OUT, true);
         }

         boolean v40 = v39 + v38 >= this.f1063 && v39 <= this.f1063 + this.f1065;
         boolean v41 = this.m1350() >= this.f1063 && this.m1350() <= this.f1063 + this.f1065;
         float v42 = 16.0F;
         float v43 = 16.0F;
         float v44 = this.f1062 + this.f1064 - v42 - 4.0F;
         float v45 = v39 + (20.0F - v43) / 2.0F;
         float v46 = v44 - v42 - 4.0F;
         boolean v48 = !v36 && v40 && v41 && fq.isHovered(this.m1349(), this.m1350(), this.f1062, v39, this.f1064, 20.0F);
         boolean v49 = !v36 && v40 && v41 && fq.isHovered(this.m1349(), this.m1350(), v44, v45, v42, v43);
         boolean v50 = !v36 && v40 && v41 && fq.isHovered(this.m1349(), this.m1350(), v46, v45, v42, v43);
         boolean v51 = v34.name.equalsIgnoreCase(v31);
         v35.hover.run(v48 && !v49 && !v50 ? 1.0 : 0.0, 0.18, ap.QUAD_OUT, true);
         v35.del.run(v49 ? 1.0 : 0.0, 0.18, ap.QUAD_OUT, true);
         v35.favHover.run(v50 ? 1.0 : 0.0, 0.18, ap.QUAD_OUT, true);
         v35.fav.run(v34.favorite ? 1.0 : 0.0, 0.25, ap.QUAD_OUT, true);
         v35.active.run(v51 ? 1.0 : 0.0, 0.25, ap.QUAD_OUT, true);
         v35.hover.update();
         v35.del.update();
         v35.favHover.update();
         v35.fav.update();
         v35.active.update();
         v35.appear.update();
         v35.headFade.update();
         float v52 = fq.clamp(v35.hover.get(), 0.0F, 1.0F);
         float v53 = fq.clamp(v35.del.get(), 0.0F, 1.0F);
         float v54 = fq.clamp(v35.favHover.get(), 0.0F, 1.0F);
         float v55 = fq.clamp(v35.fav.get(), 0.0F, 1.0F);
         float v56 = fq.clamp(v35.active.get(), 0.0F, 1.0F);
         float v57 = fq.clamp(v35.headFade.get(), 0.0F, 1.0F);
         if (v40) {
            float v58 = v13 * v37;
            float v59 = (1.0F - v37) * 10.0F;
            float v60 = this.f1062 + v59;
            float v61 = this.f1064;
            v44 = v60 + v61 - v42 - 3.0F;
            int v62 = up.getColor(255, v58 * (0.015F + 0.05F * v52 + 0.03F * v56));
            vs.ys.rect(v60, v39, v61, 20.0F, v62, 4.0F);
            float v63 = 12.0F;
            float v64 = v60 + 5.0F;
            float v65 = v39 + (20.0F - v63) / 2.0F;
            vs.ys.rect(v64, v65, v63, v63, up.getColor(255, v58 * 0.06F), 3.0F);
            vs.xs.url(v34.headUrl(), v64, v65, v63, v63, 1.0F, 3.0F, up.getColor(255, v58 * v57));
            float v66 = v64 + v63 + 4.0F;
            v22.draw(v34.name, v66, v39 + 5.0F, 8.0F, up.getColor(255, v58 * (0.7F + 0.3F * v56)));
            hu.sf_regular
               .draw(
                  this.f1045.format(new Date(v34.created)),
                  v66 + 1.0F + v22.getWidth(v34.name, 8.0F),
                  v39 + 5.0F,
                  5.0F,
                  up.getColor(255, v58 * (0.15F + 0.15F * v56))
               );
            v45 = v39 + (20.0F - v43) / 2.0F;
            v46 = v44 - v42 - 2.0F;
            vs.ys.rect(v46, v45, v42, v43, up.getColor(255, v58 * (0.015F + 0.025F * v54)), 3.0F);
            int v67 = up.getColor(19, 255, 174, 255);
            int v68 = up.overCol(
               up.getColor(255, 255, 255, v58 * (0.45F + 0.25F * v54)),
               up.replAlpha(v67, v58),
               v55
            );
            hu.icon.drawCentered("o", v46 + v42 / 2.0F, v45 + v43 / 2.0F - 2.7F, 6.0F, v68);
            vs.ys.rect(v44, v45, v42, v43, up.getColor(255, v58 * (0.015F + 0.025F * v53)), 3.0F);
            hu.icon
               .drawCentered(
                  "W",
                  v44 + v42 / 2.0F + 0.15F,
                  v45 + 4.6F,
                  7.0F,
                  up.overCol(
                     up.getColor(255, 255, 255, v58 * 0.5F),
                     up.getColor(255, 125, 125, v58 * 0.9F),
                     v53
                  )
               );
            if (!v36) {
               this.f1066.add(new float[]{v60, v39, v61 - v42 * 2.0F - 16.0F, 20.0F});
               this.f1067.add(new float[]{v44, v45, v42, v43});
               this.f1068.add(new float[]{v46, v45, v42, v43});
               this.f1069.add(v34);
            }
         }

         if (v36 && v37 <= 0.01F) {
            if (v32 == null) {
               v32 = new ArrayList();
            }

            v32.add(v34);
         }

         v30 += v38;
      }

      if (v32 != null) {
         for (o v85 : v32) {
            AltManager.get().remove(v85);
            this.f1043.remove(v85);
            this.f1044.remove(v85);
         }
      }

      if (v80.isEmpty()) {
         v22.drawCentered(
            bb.pick(
               "Пока нет аккаунтов",
               "Пока нет аккаунтов"
            ),
            this.f1062 + this.f1064 / 2.0F,
            this.f1063 + this.f1065 / 2.0F - 4.0F,
            12.0F,
            up.getColor(255, v13 * 0.15F)
         );
      }

      dt.disable();
      this.f1024.run(this.f1016 ? 1.0 : 0.0, 0.2, ap.QUAD_OUT, true);
      this.f1024.update();
      float v84 = fq.clamp(this.f1024.get(), 0.0F, 1.0F);
      vs.ys.rect(this.f1050, this.f1051, this.f1052, this.f1053, up.getColor(255, v13 * (0.015F + 0.03F * v84)), 4.0F);
      boolean v86 = this.f1016
         && System.currentTimeMillis() / (-1412733506503587214L + 1412733506503587664L) % (-3553000088471314350L + 3553000088471314352L) == 0L;
      String v92;
      if (this.f1015.isEmpty() && !this.f1016) {
         v92 = "Введите ник";
      } else {
         String v69 = v86 ? "_" : "";
         String v70 = this.f1015;
         v92 = v70 + v69;
      }

      String v87 = v92;
      v22.draw(
         v87,
         this.f1050 + 10.0F,
         this.f1051 + 5.4F,
         7.0F,
         this.f1015.isEmpty() && !this.f1016 ? up.getColor(140, v13) : up.getColor(185, v13)
      );
      this.f1025
         .run(!fq.isHovered(this.m1349(), this.m1350(), this.f1031, this.f1032, this.f1033, this.f1034) && !this.f1019 ? 0.0 : 1.0, 0.18, ap.QUAD_OUT, true);
      this.f1025.update();
      float v88 = fq.clamp(this.f1025.get(), 0.0F, 1.0F);
      vs.ys.rect(this.f1031, this.f1032, this.f1033, this.f1034, up.getColor(255, v13 * (0.015F + 0.04F * v88)), 4.0F);
      hu.icon.draw("l", this.f1031 + 6.25F, this.f1032 + 6.3F, 8.0F, up.getColor(255, v13 * (0.35F + 0.3F * v88)));
      this.f1022.run(fq.isHovered(this.m1349(), this.m1350(), this.f1054, this.f1055, this.f1056, this.f1057) ? 1.0 : 0.0, 0.18, ap.QUAD_OUT, true);
      this.f1023.run(fq.isHovered(this.m1349(), this.m1350(), this.f1058, this.f1059, this.f1060, this.f1061) ? 1.0 : 0.0, 0.18, ap.QUAD_OUT, true);
      this.f1022.update();
      this.f1023.update();
      this.m1347(
         this.f1054,
         this.f1055,
         this.f1056,
         this.f1057,
         bb.pick(
            "Добавить аккаунт",
            "Добавить аккаунт"
         ),
         v13,
         fq.clamp(this.f1022.get(), 0.0F, 1.0F),
         false
      );
      this.m1347(
         this.f1058,
         this.f1059,
         this.f1060,
         this.f1061,
         bb.pick(
            "Случайный аккаунт",
            "Случайный аккаунт"
         ),
         v13,
         fq.clamp(this.f1023.get(), 0.0F, 1.0F),
         false
      );
      us.endOverlay();
      if (context != null) {
         context.method_51448().popMatrix();
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class bo {
      boolean hasOff;
      final Animation fav;
      final Animation del;
      final Animation headFade;
      final Animation hover = new Animation();
      final Animation active;
      float curOff;
      final Animation favHover;
      final Animation appear;

      bo() {
         this.del = new Animation();
         this.active = new Animation();
         this.appear = new Animation();
         this.headFade = new Animation();
         this.fav = new Animation();
         this.favHover = new Animation();
         this.appear.set(0.0);
         this.headFade.set(0.0);
      }

      
   }
}
