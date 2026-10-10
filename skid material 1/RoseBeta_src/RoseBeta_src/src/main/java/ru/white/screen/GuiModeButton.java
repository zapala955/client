package ru.white.screen;

import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.dt;
import ru.white.utils.render.us;
import ru.white.utils.render.vs;
import ru.white.manager.InventoryPreset;
import ru.white.manager.InventoryPresetManager;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class GuiModeButton extends Screen implements IMinecraft {
   private static final float TEXT = 6.5F;
   private final Animation f1128;
   private float f1134;
   private static final float PANEL_H = 250.0F;
   private static final float PANEL_W = 400.0F;
   private static final float CELL = 19.0F;
   private float f1132;
   private static final float SMALL = 6.0F;
   private static final float LEFT_W = 160.0F;
   private InventoryPreset f1138;
   private static final float ROW_GAP = 3.0F;
   private boolean f1129;
   private float f1136;
   private float f1131;
   private float f1133;
   private final Screen f1125;
   private float f1130;
   private static final float ROW_H = 18.0F;
   private final Map<String, ru.white.utils.animation.satoshi.Animation> f1127;
   private float f1135;
   private final List<io.jo> f1126 = new ArrayList<>();
   private float f1137;
   private String name;

   public boolean method_25400(CharInput input) {
      if (this.f1129 && input.method_74227()) {
         int temp1 = v4 + 1;

         int result = v4 * temp1 % 2;

         if (result != 0) {
            }

         if (this.name.length() < 32) {
            String v9 = this.name;
            String v2 = input.method_74226();
            String v3 = v9;
            this.name = v3 + v2;
            return true;
         }
      }

      return super.method_25400(input);
   }

   public GuiModeButton(Screen parent) {
      super(Text.method_43470("Менеджер пресетов"));
      this.f1127 = new HashMap<>();
      this.f1128 = new Animation();
      this.name = "";
      this.f1133 = 1.0F;
      this.f1125 = parent;
   }

   public void method_25394(DrawContext context, int rawMouseX, int rawMouseY, float delta) {
      float v5 = 2.0F;
      float v6 = (float)mc.method_22683().method_4495();
      this.f1133 = v5 / v6;
      float v7 = (float)mc.method_22683().method_4486() / this.f1133;
      float v8 = (float)mc.method_22683().method_4502() / this.f1133;
      this.f1136 = (float)rawMouseX / this.f1133;
      float temp1 = (float)rawMouseY;

      this.f1137 = temp1 / this.f1133;
      this.f1128.update();
      float v9 = this.f1128.get();
      this.f1130 = this.f1130 + (this.f1131 - this.f1130) * 0.2F;
      this.f1134 = v7 / 2.0F - 200.0F;
      this.f1135 = v8 / 2.0F - 125.0F + (1.0F - v9) * 30.0F;
      us.beginOverlay();
      vs.ys.rect(0.0F, 0.0F, v7, v8, up.getColor(0, 0.6F * v9));
      vs.ys.glow(this.f1134, this.f1135, 400.0F, 249.5F, up.getColor(0, 0.15F * v9), 8.0F, 15.0F, 1.0F);
      vs.ws.blur(this.f1134, this.f1135, 400.0F, 250.0F, v9, 8.0F, up.multAlpha(up.multDark(up.background(), 0.6F), v9));
      vs.ys.outline(this.f1134, this.f1135, 400.0F, 250.0F, 0.5F, up.replAlpha(up.client(), v9 * 0.35F), 8.0F);
      yt v10 = hu.sf_regular;
      vs.ys.rect(this.f1134 + 160.0F, this.f1135 + 8.0F, 0.5F, 234.0F, up.getColor(255, 0.05F * v9), 1.0F);
      hu.icon.draw("P", this.f1134 + 10.0F, this.f1135 + 12.5F, 7.0F, up.multAlpha(up.client(), v9));
      v10.draw(
         "Пресеты инвентаря",
         this.f1134 + 22.0F,
         this.f1135 + 11.5F,
         7.0F,
         up.getColor(200, v9)
      );
      v10.draw(
         "Предпросмотр",
         this.f1134 + 160.0F + 12.0F,
         this.f1135 + 11.5F,
         7.0F,
         up.getColor(200, v9)
      );
      float v11 = this.f1134 + 10.0F;
      float v12 = 140.0F;
      float v13 = this.f1135 + 28.0F;
      this.m1383(v10, v11, v13, v12, v9);
      float v14 = v13 + 16.0F + 3.0F;
      boolean v15 = this.m1384(v10, "Сохранить текущий", v11, v14, v12, v9, true);
      this.f1126.clear();
      List v16 = this.m1382().presets();
      float v17 = v14 + 16.0F + 6.0F;
      float v18 = this.f1135 + 250.0F - 32.0F - v17;
      this.f1132 = Math.max(0.0F, (float)v16.size() * 21.0F - v18);
      this.f1131 = fq.clamp(this.f1131, 0.0F, this.f1132);
      dt.enable(v11, v17, v12, v18, 2.0F);
      InventoryPreset v19 = null;
      float v20 = v17 - this.f1130;

      for (InventoryPreset v22 : v16) {
         boolean v23 = v22.name().equalsIgnoreCase(this.m1382().activeName());
         boolean v24 = m1390(this.f1136, this.f1137, v11, v20, v12, 18.0F);
         if (v24) {
            v19 = v22;
         }

         String v29 = v22.name();
         float v25 = this.m1388("row:" + v29, v24);
         String v30 = v22.name();
         ru.white.utils.animation.satoshi.Animation v26 = this.m1387("active:" + v30);
         v26.setDirection(v23 ? cp.FORWARDS : cp.BACKWARDS);
         float v27 = v26.getOutput();
         vs.ys.rect(
            v11, v20, v12, 18.0F, up.overCol(up.getColor(40, 0.15F * v9), up.replAlpha(up.client(), 0.25F * v9), v27), 5.0F
         );
         vs.ys.outline(v11, v20, v12, 18.0F, 0.5F, up.replAlpha(up.client(), v9 * Math.max(v25, v27)), 5.0F);
         v10.drawFadingText(
            v22.name(), v11 + 6.0F, v20 + 5.5F, v12 - 26.0F, up.getColor(255, v9 * (0.5F + 0.5F * Math.max(v25, v27))), 6.5F
         );
         boolean v28 = m1390(this.f1136, this.f1137, v11 + v12 - 18.0F, v20, 18.0F, 18.0F);
         hu.icon
            .drawCentered(
               "Q",
               v11 + v12 - 9.0F,
               v20 + 7.0F,
               4.0F,
               v28
                  ? up.getColor(235, 70, 70, v9)
                  : up.getColor(255, v9 * 0.35F)
            );
         this.f1126.add(new io.jo(v22, v11, v20, v12, 18.0F));
         v20 += 21.0F;
      }

      dt.disable();
      boolean v32 = this.m1382().activePreset().isPresent();
      this.m1384(
         v10,
         v32
            ? "Отключить пресет"
            : "Пресет не выбран",
         v11,
         this.f1135 + 250.0F - 26.0F,
         v12,
         v9,
         false
      );
      InventoryPreset v33 = v19 != null ? v19 : this.f1138;
      if (v33 == null) {
         v33 = this.m1382().activePreset().orElse(v16.isEmpty() ? null : (InventoryPreset)v16.get(0));
      }

      this.m1385(context, v10, v33, v9, rawMouseX, rawMouseY);
      us.endOverlay();
   }

   public boolean method_25404(KeyInput input) {
      if (this.f1129) {

         if (input.comp_4795() == 257) {
            this.m1389();
         } else if (input.comp_4795() == 259 && !this.name.isEmpty()) {

            this.name = this.name.substring(0, this.name.length() - 1);
         } else if (input.comp_4795() == 256) {
            this.f1129 = false;
         }

         return true;
      } else if (input.comp_4795() == 256) {
         this.field_22787.method_1507(this.f1125);
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   private static boolean m1390(float mouseX, float mouseY, float x, float y, float w, float h) {

      return fq.isHovered(mouseX, mouseY, x, y, w, h);
   }

   private void m1389() {
      InventoryPreset v1 = this.m1382().saveCurrent(this.name);
      if (v1 != null) {
         this.f1138 = v1;
         this.name = "";

         this.f1129 = false;
      }
   }

   public boolean method_25402(Click click, boolean doubled) {
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         float v3 = (float)(click.comp_4798() / (double)this.f1133);
         double result = click.comp_4799();

         float v4 = (float)(result / (double)this.f1133);
         float v5 = this.f1134 + 10.0F;
         float v6 = 140.0F;
         float v7 = this.f1135 + 28.0F;
         this.f1129 = m1390(v3, v4, v5, v7, v6, 16.0F);
         if (m1390(v3, v4, v5, v7 + 16.0F + 3.0F, v6, 16.0F)) {
            this.m1389();
            return true;
         } else {
            for (io.jo v9 : this.f1126) {
               if (m1390(v3, v4, v9.x + v9.f1124 - 18.0F, v9.y, 18.0F, v9.h)) {
                  this.m1382().delete(v9.f1123);
                  if (this.f1138 == v9.f1123) {
                     this.f1138 = null;
                  }

                  return true;
               }

               if (m1390(v3, v4, v9.x, v9.y, v9.f1124 - 18.0F, v9.h)) {
                  this.m1382().activate(v9.f1123);
                  this.field_22787.method_1507(this.f1125);
                  return true;
               }
            }

            if (m1390(v3, v4, v5, this.f1135 + 250.0F - 26.0F, v6, 16.0F) && this.m1382().activePreset().isPresent()) {
               this.m1382().deactivate();
               return true;
            } else {
               return true;
            }
         }
      }
   }

   private InventoryPresetManager m1382() {
      InventoryPresetManager result = Client.get().inventoryPresetManager();

      return result;
   }

   private boolean m1384(yt font, String label, float x, float y, float w, float alpha, boolean accent) {
      boolean v8 = m1390(this.f1136, this.f1137, x, y, w, 16.0F);
      float v9 = this.m1388("btn:" + label, v8);
      vs.ys.glow(x, y, w, 16.0F, up.getColor(0, 0.06F * alpha), 5.0F, 7.0F, 1.0F);
      int temp4 = 40;

      vs.ys.rect(x, y, w, 16.0F, up.overCol(up.getColor(temp4, 0.15F * alpha), up.replAlpha(up.client(), (accent ? 0.3F : 0.2F) * alpha), v9), 5.0F);
      vs.ys.outline(x, y, w, 16.0F, 0.5F, up.replAlpha(up.client(), alpha * (accent ? 0.4F + 0.6F * v9 : v9)), 5.0F);
      font.drawCentered(label, x + w / 2.0F, y + 4.5F, 6.5F, up.getColor(255, alpha * (0.55F + 0.45F * v9)));
      return v8;
   }

   private boolean m1386(DrawContext context, ItemStack stack, float x, float y, float alpha) {
      boolean v6 = m1390(this.f1136, this.f1137, x, y, 17.0F, 17.0F);

      vs.ys.rect(x, y, 17.0F, 17.0F, up.getColor(40, 0.15F * alpha), 4.0F);
      if (v6) {
         vs.ys.outline(x, y, 17.0F, 17.0F, 0.5F, up.replAlpha(up.client(), alpha), 4.0F);
      }

      if (!stack.method_7960()) {
         Matrix3x2fStack v7 = context.method_51448();
         v7.pushMatrix();
         v7.translate((x + 8.5F) * this.f1133, (y + 8.5F) * this.f1133);

         v7.scale(this.f1133, this.f1133);
         context.method_51427(stack, -8, -8);
         context.method_51431(this.field_22793, stack, -8, -8);
         v7.popMatrix();
      }

      return v6 && !stack.method_7960();
   }

   private void m1383(yt font, float x, float y, float w, float alpha) {
      float v6 = this.m1388("input", m1390(this.f1136, this.f1137, x, y, w, 16.0F) || this.f1129);
      ru.white.utils.animation.satoshi.Animation v7 = this.m1387("input:focus");
      v7.setDirection(this.f1129 ? cp.FORWARDS : cp.BACKWARDS);
      float v8 = v7.getOutput();
      vs.ys.rect(
         x,
         y,
         w,
         16.0F,
         up.overCol(up.getColor(40, 0.15F * alpha), up.getColor(25, 0.3F * alpha), Math.max(v6, v8)),
         5.0F
      );
      vs.ys.outline(x, y, w, 16.0F, 0.5F, up.replAlpha(up.client(), alpha * v8), 5.0F);
      boolean v9 = this.name.isEmpty() && v8 < 0.99F;
      if (v9) {
         font.draw(
            "Название пресета",
            x + 6.0F,
            y + 4.5F + 4.0F * v8,
            6.5F,
            up.getColor(255, alpha * (1.0F - v8) * 0.35F)
         );
      }

      if (!this.name.isEmpty()) {
         float temp2 = x + 6.0F;
         float temp3 = y + 4.5F;
         float temp4 = w - 14.0F;
         int temp5 = 255;

         font.drawFadingTextReverse(this.name, temp2, temp3, temp4, up.getColor(temp5, alpha * 0.85F), 6.5F);
      }

      if (v8 > 0.01F) {
         long result = System.currentTimeMillis() / (1622127246888916032L + -1622127246888915582L) % (4287120421635506352L + -4287120421635506350L);

         if (result == 0L) {
            vs.ys.rect(x + 6.5F + font.getWidth(this.name, 6.5F), y + 4.5F, 0.6F, 8.0F, up.replAlpha(up.client(), alpha * v8), 0.3F);
         }
      }
   }

   private ru.white.utils.animation.satoshi.Animation m1387(String key) {
      return this.f1127.computeIfAbsent(key, k -> new ep(300, 1.0));
   }

   public void method_25419() {

      this.field_22787.method_1507(this.f1125);
   }

   protected void method_25426() {

      this.f1128.set(0.0);
      this.f1128.run(1.0, 0.25, ap.SINE_OUT);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      double temp1 = (double)this.f1131;

      int temp5 = v9 + 1;

      this.f1131 = fq.clamp((float)(temp1 - vertical * 24.0), 0.0F, this.f1132);
      return true;
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private void m1385(DrawContext context, yt font, InventoryPreset preset, float alpha, int rawMouseX, int rawMouseY) {
      float v7 = this.f1134 + 160.0F + 12.0F;
      float v8 = this.f1135 + 34.0F;
      if (preset == null) {
         font.draw(
            "Сохранённых пресетов пока нет",
            v7,
            v8,
            6.5F,
            up.getColor(255, alpha * 0.35F)
         );
      } else {
         font.draw(preset.name(), v7, v8, 6.5F, up.replAlpha(up.client(), alpha));
         float v9 = v7 + 26.0F;
         float v10 = v8 + 24.0F;
         ItemStack v11 = ItemStack.field_8037;
         float temp3 = v8 + 14.0F;

         font.draw("Броня", v7, temp3, 6.0F, up.getColor(255, alpha * 0.35F));

         for (int v12 = 0; v12 < 4; v12++) {
            ItemStack v13 = preset.slot(36 + v12).toStack();
            if (this.m1386(context, v13, v7, v10 + (float)v12 * 19.0F, alpha)) {
               v11 = v13;
            }
         }

         font.draw("Оффхенд", v7, v10 + 76.0F + 4.0F, 6.0F, up.getColor(255, alpha * 0.35F));
         ItemStack v18 = preset.slot(40).toStack();
         if (this.m1386(context, v18, v7, v10 + 76.0F + 15.0F, alpha)) {
            v11 = v18;
         }

         font.draw("Инвентарь", v9, v8 + 14.0F, 6.0F, up.getColor(255, alpha * 0.35F));

         for (int v19 = 0; v19 < 3; v19++) {
            for (int v14 = 0; v14 < 9; v14++) {
               ItemStack v15 = preset.slot(9 + v19 * (9) + v14).toStack();
               float v22 = (float)v14;

               if (this.m1386(context, v15, v9 + v22 * 19.0F, v10 + (float)v19 * 19.0F, alpha)) {
                  v11 = v15;
               }
            }
         }

         font.draw("Хотбар", v9, v10 + 57.0F + 4.0F, 6.0F, up.getColor(255, alpha * 0.35F));

         for (int v20 = 0; v20 < 9; v20++) {
            ItemStack v21 = preset.slot(v20).toStack();
            if (this.m1386(context, v21, v9 + (float)v20 * 19.0F, v10 + 57.0F + 15.0F, alpha)) {
               v11 = v21;
            }
         }

         if (!v11.method_7960()) {
            us.endOverlay();
            context.method_51446(this.field_22793, v11, rawMouseX, rawMouseY);
            us.beginOverlay();
         }
      }
   }

   public boolean method_25421() {
      return false;
   }

   private float m1388(String key, boolean hovered) {

      ru.white.utils.animation.satoshi.Animation v3 = this.m1387(key + ":hover");
      v3.setDirection(hovered ? cp.FORWARDS : cp.BACKWARDS);

      return v3.getOutput();
   }

   @Environment(EnvType.CLIENT)
   private static record jo(InventoryPreset preset, float x, float y, float w, float h) {
      private final InventoryPreset f1123;
      private final float f1124;

      public float h() {

         return this.h;
      }

      public float y() {

         return this.y;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",io.jo,"preset;x;y;w;h",io.jo::f1123,io.jo::x,io.jo::y,io.jo::f1124,io.jo::h>(this);

         return result;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",io.jo,"preset;x;y;w;h",io.jo::f1123,io.jo::x,io.jo::y,io.jo::f1124,io.jo::h>(this, o);
      }

      public InventoryPreset preset() {
         return this.f1123;
      }

      private jo(InventoryPreset preset, float x, float y, float w, float h) {
         this.f1123 = preset;
         this.x = x;
         this.y = y;
         this.f1124 = w;
         this.h = h;
      }

      public float w() {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.f1124;
      }

      public float x() {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.x;
      }

      
   }
}
