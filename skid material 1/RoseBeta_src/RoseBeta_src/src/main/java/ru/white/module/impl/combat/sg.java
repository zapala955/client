package ru.white.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.js;
import ru.white.utils.render.vs;
import ru.white.utils.IMinecraft;
import ru.white.utils.system.Hu;

@Environment(EnvType.CLIENT)
public class sg extends Screen {
   private final hg f341;
   private final int f342;
   private static final float PAD = 5.0F;
   private final List<ItemStack> f344;
   private static final float BTN_H = 14.0F;
   private static final float CELL_GAP = 4.0F;
   private static final float TITLE_H = 14.0F;
   private static final int MAX_COLS = 7;
   private static final float CELL_SIZE = 20.0F;
   private float f340 = 1.0F;
   private final Screen f343;
   private static final float BTN_W = 44.0F;

   private float m429(int i) {
      float result = this.m427();

      return result + (float)(i / this.m418()) * 24.0F;
   }

   private float m425() {
      float result = (float)this.field_22790 / this.f340 - this.m423();

      return result / 2.0F;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.f340 = 2.0F / (float)hp.mc.method_22683().method_4495();
      mouseX = (int)((float)mouseX / this.f340);
      mouseY = (int)((float)mouseY / this.f340);
      this.m417();
      float v5 = this.m424();
      float v6 = this.m425();
      float v7 = this.m422();
      float v8 = this.m423();
      vs.ws.blur(v5, v6, v7, v8, 1.0F, 8.0F, 0);
      vs.ys.rect(v5, v6, v7, v8, up.getColor(0, 0.7F), 8.0F);
      int v21 = this.f342 + 1;
      String v9 = "Слот " + v21 + " — выберите предмет";
      float v10 = hu.sf_regular.getWidth(v9, 7.0F);
      hu.sf_regular.draw(v9, v5 + (v7 - v10) / 2.0F, v6 + 5.0F + 2.0F, 7.0F, up.getColor(255, 1.0F));
      if (this.f344.isEmpty()) {
         String v11 = "Инвентарь пуст";
         float v12 = hu.sf_regular.getWidth(v11, 7.0F);
         hu.sf_regular.draw(v11, v5 + (v7 - v12) / 2.0F, this.m427() + 4.0F, 7.0F, up.getColor(255, 1.0F));
      } else {
         ItemStack v25 = this.f341.tripleSlotItems[this.f342];

         for (int v27 = 0; v27 < this.f344.size(); v27++) {
            ItemStack v13 = this.f344.get(v27);
            float v14 = this.m428(v27);
            float v15 = this.m429(v27);
            boolean v16 = (float)mouseX >= v14 && (float)mouseX <= v14 + 20.0F && (float)mouseY >= v15 && (float)mouseY <= v15 + 20.0F;
            boolean v17 = v25 != null && ItemStack.method_31577(v13, v25);
            int v18 = v17
               ? up.getColor(255, 255, 255, 0.15F)
               : (
                  v16
                     ? up.getColor(255, 255, 255, 0.12F)
                     : up.getColor(255, 255, 255, 0.05F)
               );
            vs.ys.rect(v14, v15, 20.0F, 20.0F, v18, 4.0F);
            if (v17 || v16) {
               vs.ys.outline(
                  v14,
                  v15,
                  20.0F,
                  20.0F,
                  v17 ? 0.6F : 0.5F,
                  up.getColor(255, 255, 255, v17 ? 0.3F : 0.2F),
                  4.0F
               );
            }

            js.drawItemCenteredWithContext(context, v13, (v14 + 10.0F) * this.f340, (v15 + 10.0F) * this.f340, 0.85F, 1.0F);
            if (v16) {
               String v19 = v13.method_7964().getString();
               float v20 = hu.sf_bold.getWidth(v19, 7.0F);
               hu.sf_bold.draw(v19, v5 + (v7 - v20) / 2.0F, this.m427() + this.m421() + 30.0F, 7.0F, up.getColor(255, 1.0F));
            }
         }
      }

      float v26 = v5 + (v7 - 44.0F) / 2.0F;
      float v28 = this.m427() + (this.f344.isEmpty() ? 20.0F : this.m421()) + 6.0F;
      boolean v29 = (float)mouseX >= v26 && (float)mouseX <= v26 + 44.0F && (float)mouseY >= v28 && (float)mouseY <= v28 + 14.0F;
      int temp4 = 255;

      vs.ys.rect(v26, v28, 44.0F, 14.0F, up.getColor(temp4, 255, 255, v29 ? 0.12F : 0.06F), 4.0F);
      vs.ys.outline(
         v26, v28, 44.0F, 14.0F, 0.6F, up.getColor(255, 255, 255, v29 ? 0.3F : 0.1F), 4.0F
      );
      float v30 = hu.sf_regular.getWidth("Сброс", 6.0F);
      hu.sf_regular.draw("Сброс", v26 + (44.0F - v30) / 2.0F, v28 + 3.5F, 6.0F, up.getColor(255, v29 ? 0.9F : 0.45F));
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private float m424() {
      return ((float)this.field_22789 / this.f340 - this.m422()) / 2.0F;
   }

   private int m419() {
      int result;
      if (this.f344.isEmpty()) {
         result = 0;
      } else {

         double v4 = (double)this.f344.size();

         result = (int)Math.ceil(v4 / (double)this.m418());
      }

      return result;
   }

   private float m420() {
      return (float)this.m418() * 20.0F + (float)(this.m418() - 1) * 4.0F;
   }

   private float m421() {
      float result = (float)this.m419() * 20.0F;

      int v3 = Math.max(0, this.m419() - 1);

      return result + (float)v3 * 4.0F;
   }

   private float m426() {

      return this.m424() + (this.m422() - this.m420()) / 2.0F;
   }

   public void method_25419() {

      hp.mc.method_1507(this.f343);
   }

   private float m423() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return 23.0F + (this.f344.isEmpty() ? 20.0F : this.m421()) + 6.0F + 14.0F + 5.0F;
   }

   public boolean method_25402(Click click, boolean doubled) {
      double v3 = click.comp_4798() / (double)this.f340;
      double v5 = click.comp_4799() / (double)this.f340;
      if (click.method_74245() != 0) {
         return super.method_25402(click, doubled);
      } else {
         float v7 = this.m424();
         float v8 = this.m425();

         for (int v9 = 0; v9 < this.f344.size(); v9++) {
            float v10 = this.m428(v9);
            float v11 = this.m429(v9);
            if (v3 >= (double)v10 && v3 <= (double)(v10 + 20.0F) && v5 >= (double)v11 && v5 <= (double)(v11 + 20.0F)) {
               this.f341.tripleSlotItems[this.f342] = this.f344.get(v9).method_7972();
               this.method_25419();
               return true;
            }
         }

         float v13 = v7 + (this.m422() - 44.0F) / 2.0F;
         float v14 = this.m427() + (this.f344.isEmpty() ? 20.0F : this.m421()) + 6.0F;
         if (v3 >= (double)v13 && v3 <= (double)(v13 + 44.0F) && v5 >= (double)v14 && v5 <= (double)(v14 + 14.0F)) {
            this.f341.tripleSlotItems[this.f342] = null;
            this.method_25419();
            return true;
         } else {
            if (v3 < (double)v7 || v3 > (double)(v7 + this.m422()) || v5 < (double)v8 || v5 > (double)(v8 + this.m423())) {
               this.method_25419();
            }

            return super.method_25402(click, doubled);
         }
      }
   }

   private int m418() {
      int result = Math.min(7, Math.max(1, this.f344.size()));

      return result;
   }

   private float m427() {
      float result = this.m425();

      return result + 5.0F + 14.0F + 4.0F;
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private void m417() {
      this.f344.clear();

      while (v1 < 36) {
         ItemStack result = hp.mc.field_1724.method_31548().method_5438(v1);

         ItemStack v2 = result;
         if (!v2.method_7960()) {
            this.f344.add(v2);
         }

         v1++;
         
      }
   }

   public sg(hg module, int slotIndex, Screen parent) {
      super(Text.method_43470("Выбор предмета"));
      this.f344 = new ArrayList<>();
      this.f341 = module;
      this.f342 = slotIndex;
      this.f343 = parent;
   }

   private float m428(int i) {
      float result = this.m426();
      float temp1 = (float)(i % this.m418());

      return result + temp1 * 24.0F;
   }

   public boolean method_25421() {

      return false;
   }

   public sg(hg module, int slotIndex) {
      this(module, slotIndex, null);
   }

   private float m422() {

      return Math.max(this.m420(), 44.0F) + 10.0F;
   }

   
}
